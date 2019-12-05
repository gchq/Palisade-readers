/*
 * Copyright 2019 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.service.resource.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.resource.request.AddResourceRequest;
import uk.gov.gchq.palisade.resource.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.request.GetResourcesBySerialisedFormatRequest;
import uk.gov.gchq.palisade.resource.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.ResourceService;
import uk.gov.gchq.palisade.service.request.ResourceDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * An implementation of the ResourceService.
 * <p>
 * This service is for the retrieval of Resources only. Resources cannot be added via this service, they should be added
 * through the actual real filing system.
 */

public class HadoopResourceService implements ResourceService {

    public static final String ERROR_ADD_RESOURCE = "AddResource is not supported by the Resource Service, resources should be added/created via regular file system behaviour.";
    public static final String ERROR_OUT_SCOPE = "resource ID is out of scope of the this resource Service. Found: %s expected: %s";
    public static final String ERROR_NO_DATA_SERVICES = "No Hadoop data services known about in Hadoop resource service";
    private static final Logger LOGGER = LoggerFactory.getLogger(HadoopResourceService.class);
    /**
     * A regular expression that matches URIs that have the file:/ scheme with a single slash but not any more slashes.
     */
    private static final Pattern FILE_PAT = Pattern.compile("(?i)(?<=^file:)/(?=([^/]|$))");
    private static final String ERROR_RESOLVING_PARENTS = "Error occurred while resolving resourceParents";
    private static final String HADOOP_CONF_STRING = "hadoop.init.conf";
    private static final String DATA_SERVICE_LIST = "hadoop.data.svc.list";

    private Configuration config;
    private FileSystem fileSystem;

    private List<ConnectionDetail> dataServices = new ArrayList<>();

    public HadoopResourceService(final Configuration config) throws IOException {
        requireNonNull(config, "service");
        this.config = config;
        this.fileSystem = FileSystem.get(config);
        LOGGER.debug("Loaded Hadoop configuration: \n{}", config.toString());
    }

    public HadoopResourceService(@JsonProperty("conf") final Map<String, String> conf) throws IOException {
        this(createConfig(conf));
    }

    public HadoopResourceService() {
    }

    public static void resolveParents(final ChildResource resource, final Configuration configuration) {
        LOGGER.debug("Resolving parents for resource: {}", resource);
        try {
            final String connectionDetail = resource.getId();
            final Path path = new Path(connectionDetail);
            final int fileDepth = path.depth();
            final int fsDepth = new Path(configuration.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY)).depth();

            if (fileDepth > fsDepth + 1) {
                DirectoryResource parent = new DirectoryResource().id(fixURIScheme(path.getParent().toUri().toString()));
                resource.setParent(parent);
                LOGGER.debug("Resource {} has parent {}", resource, parent);
                resolveParents(parent, configuration);
            } else {
                LOGGER.debug("Resource {} has parent ROOT", resource);
                resource.setParent(new SystemResource().id(fixURIScheme(path.getParent().toUri().toString())));
            }
        } catch (Exception e) {
            LOGGER.error("Error encountered while resolving resource parents: {}", e.getMessage());
            throw new RuntimeException(ERROR_RESOLVING_PARENTS, e);
        }
    }

    private static String fixURIScheme(final String uri) {
        requireNonNull(uri, "uri");
        Matcher match = FILE_PAT.matcher(uri);
        if (match.find()) {
            String fixed = match.replaceFirst("///");
            LOGGER.debug("Fixing uri scheme: {} -> {}", uri, fixed);
            return fixed;
        } else {
            LOGGER.debug("Not fixing ri scheme: {}", uri);
            return uri;
        }
    }

    protected static Collection<String> getPaths(final RemoteIterator<LocatedFileStatus> remoteIterator) throws IOException {
        LOGGER.debug("Getting paths for iterator...");
        final ArrayList<String> paths = new ArrayList<>();
        while (remoteIterator.hasNext()) {
            final LocatedFileStatus next = remoteIterator.next();
            final String pathWithoutFSName = next.getPath().toUri().toString();
            paths.add(pathWithoutFSName);
            LOGGER.debug("Got path: {}", pathWithoutFSName);
        }
        return paths;
    }

    private static Configuration createConfig(final Map<String, String> conf) {
        LOGGER.debug("Creating config from map: {}", conf.toString());
        final Configuration config = new Configuration();
        if (nonNull(conf)) {
            for (final Entry<String, String> entry : conf.entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
        }
        return config;
    }

    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesByResource(final GetResourcesByResourceRequest request) {
        requireNonNull(request, "request");
        LOGGER.info("Invoking getResourcesByResource request: {}", request);
        GetResourcesByIdRequest getResourcesByIdRequest = new GetResourcesByIdRequest().resourceId(request.getResource().getId());
        getResourcesByIdRequest.setOriginalRequestId(request.getOriginalRequestId());
        CompletableFuture<Map<LeafResource, ConnectionDetail>> response = getResourcesById(getResourcesByIdRequest);
        LOGGER.info("Got resources {} for request {}", response, request);
        return response;
    }

    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesById(final GetResourcesByIdRequest request) {
        requireNonNull(request, "request");
        LOGGER.info("Invoking getResourcesById request: {}", request);
        final String resourceId = request.getResourceId();
        final String path = getInternalConf().get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        if (!resourceId.startsWith(path)) {
            throw new UnsupportedOperationException(java.lang.String.format(ERROR_OUT_SCOPE, resourceId, path));
        }
        CompletableFuture<Map<LeafResource, ConnectionDetail>> response = getFutureMappings(resourceId, ignore -> true);
        LOGGER.info("Got resources {} for request {}", response, request);
        return response;
    }

    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesByType(final GetResourcesByTypeRequest request) {
        requireNonNull(request, "request");
        LOGGER.info("Invoking getResourcesByType request: {}", request);
        final String pathString = getInternalConf().get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        final Predicate<ResourceDetails> predicate = detail -> request.getType().equals(detail.getType());
        CompletableFuture<Map<LeafResource, ConnectionDetail>> response = getFutureMappings(pathString, predicate);
        LOGGER.info("Got resources {} for request {}", response, request);
        return response;
    }

    @Override
    public CompletableFuture<Map<LeafResource, ConnectionDetail>> getResourcesBySerialisedFormat(final GetResourcesBySerialisedFormatRequest request) {
        requireNonNull(request, "request");
        LOGGER.info("Invoking getResourcesBySerialisedFormat request: {}", request);
        final String pathString = getInternalConf().get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        final Predicate<ResourceDetails> predicate = detail -> request.getSerialisedFormat().equals(detail.getFormat());
        CompletableFuture<Map<LeafResource, ConnectionDetail>> response = getFutureMappings(pathString, predicate);
        LOGGER.info("Got resources {} for request {}", response, request);
        return response;
    }

    @Override
    public CompletableFuture<Boolean> addResource(final AddResourceRequest request) {
        throw new UnsupportedOperationException(ERROR_ADD_RESOURCE);
    }

    private CompletableFuture<Map<LeafResource, ConnectionDetail>> getFutureMappings(final String pathString, final Predicate<ResourceDetails> predicate) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                //pull latest connection details
                final RemoteIterator<LocatedFileStatus> remoteIterator = this.getFileSystem().listFiles(new Path(pathString), true);
                Map<LeafResource, ConnectionDetail> completedFuture = getPaths(remoteIterator)
                        .stream()
                        .filter(ResourceDetails::isValidResourceName)
                        .map(ResourceDetails::getResourceDetailsFromFileName)
                        .filter(predicate)
                        .collect(Collectors.toMap(
                                resourceDetails -> {
                                    final String fileName = resourceDetails.getFileName();
                                    final FileResource fileFileResource = new FileResource().id(fileName).type(resourceDetails.getType()).serialisedFormat(resourceDetails.getFormat());
                                    resolveParents(fileFileResource, getInternalConf());
                                    return fileFileResource;
                                },
                                resourceDetails -> {
                                    if (this.dataServices.size() < 1) {
                                        throw new IllegalStateException(ERROR_NO_DATA_SERVICES);
                                    }
                                    int service = ThreadLocalRandom.current().nextInt(this.dataServices.size());
                                    return this.dataServices.get(service);
                                }
                                )
                        );
                LOGGER.info("Completed future request for predicate {} with resources {}", predicate, completedFuture);
                return completedFuture;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    public HadoopResourceService conf(final Configuration conf) throws IOException {
        requireNonNull(conf, "conf");
        LOGGER.debug("Updated config: {}", conf);
        this.config = conf;
        this.fileSystem = FileSystem.get(conf);
        return this;
    }


    public HadoopResourceService addDataService(final ConnectionDetail detail) {
        requireNonNull(detail, "detail");
        LOGGER.debug("Added new data service: {}", detail);
        dataServices.add(detail);
        return this;
    }

    protected Configuration getInternalConf() {
        requireNonNull(config, "configuration must be set");
        LOGGER.debug("Got request for internal config");
        return config;
    }

    protected FileSystem getFileSystem() {
        requireNonNull(fileSystem, "configuration must be set");
        LOGGER.debug("Got request for filesystem");
        return fileSystem;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    public Map<String, String> getConf() {
        LOGGER.debug("Got request for config map");
        Map<String, String> rtn = new HashMap<>();
        Map<String, String> plainJobConfWithoutResolvingValues = getPlainJobConfWithoutResolvingValues();

        for (Entry<String, String> entry : getInternalConf()) {
            final String plainValue = plainJobConfWithoutResolvingValues.get(entry.getKey());
            final String thisValue = entry.getValue();
            if (isNull(plainValue) || !plainValue.equals(thisValue)) {
                rtn.put(entry.getKey(), entry.getValue());
            }
        }
        return rtn;
    }

    public void setConf(final Map<String, String> conf) throws IOException {
        LOGGER.debug("Updated config from map: {}", conf);
        setConf(createConfig(conf));
    }

    @JsonIgnore
    public void setConf(final Configuration conf) throws IOException {
        LOGGER.debug("Updated config: {}", conf);
        conf(conf);
    }

    private Map<String, String> getPlainJobConfWithoutResolvingValues() {
        Map<String, String> plainMapWithoutResolvingValues = new HashMap<>();
        for (Entry<String, String> entry : new Configuration()) {
            plainMapWithoutResolvingValues.put(entry.getKey(), entry.getValue());
        }
        return plainMapWithoutResolvingValues;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final HadoopResourceService that = (HadoopResourceService) o;
        return Objects.equals(fileSystem, that.fileSystem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileSystem);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("config", config)
                .append("fileSystem", fileSystem)
                .toString();
    }

    /**
     * Make Jackson interpret the deserialised list correctly.
     */
    private static class ConnectionDetailType extends TypeReference<List<ConnectionDetail>> {
    }
}
