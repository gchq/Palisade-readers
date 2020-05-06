/*
 * Copyright 2020 Crown Copyright
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

import uk.gov.gchq.palisade.Generated;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.ResourceService;
import uk.gov.gchq.palisade.service.resource.util.HadoopResourceDetails;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

    private Configuration config;
    private FileSystem fileSystem;

    private List<ConnectionDetail> dataServices = new ArrayList<>();

    public HadoopResourceService(final Configuration config) throws IOException {
        requireNonNull(config, "service");
        this.config = config;
        this.fileSystem = FileSystem.get(config);
    }

    public HadoopResourceService(@JsonProperty("conf") final Map<String, String> conf) throws IOException {
        this(createConfig(conf));
    }

    public HadoopResourceService() {
    }

    protected static Stream<URI> getPaths(final RemoteIterator<LocatedFileStatus> remoteIterator) {
        return Stream.generate(() -> null)
                .takeWhile(x -> {
                    try {
                        return remoteIterator.hasNext();
                    } catch (IOException e) {
                        return false;
                    }
                })
                .map(n -> {
                    try {
                        return remoteIterator.next();
                    } catch (IOException e) {
                        return null;
                    }
                })
                .map(locatedFileStatus -> locatedFileStatus.getPath().toUri());
    }

    private static Configuration createConfig(final Map<String, String> conf) {
        final Configuration config = new Configuration();
        if (nonNull(conf)) {
            for (final Entry<String, String> entry : conf.entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
        }
        return config;
    }

    @Override
    public Stream<LeafResource> getResourcesByResource(final Resource resource) {
        requireNonNull(resource, "resource");
        LOGGER.debug("Invoking getResourcesByResource with request: {}", resource);
        return getResourcesById(resource.getId());
    }

    @Override
    public Stream<LeafResource> getResourcesById(final String resourceId) {
        requireNonNull(resourceId, "resourceId");
        LOGGER.debug("Invoking getResourcesById with id: {}", resourceId);
        final String path = getInternalConf().get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        if (!resourceId.startsWith(path)) {
            throw new UnsupportedOperationException(java.lang.String.format(ERROR_OUT_SCOPE, resourceId, path));
        }
        return getMappings(resourceId, ignore -> true);
    }

    @Override
    public Stream<LeafResource> getResourcesByType(final String type) {
        requireNonNull(type, "type");
        LOGGER.debug("Invoking getResourcesByType with type: {}", type);
        final String pathString = getInternalConf().get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        final Predicate<HadoopResourceDetails> predicate = detail -> type.equals(detail.getType());
        return getMappings(pathString, predicate);
    }

    @Override
    public Stream<LeafResource> getResourcesBySerialisedFormat(final String serialisedFormat) {
        requireNonNull(serialisedFormat, "serialisedFormat");
        LOGGER.debug("Invoking getResourcesBySerialisedFormat with serialisedFormat: {}", serialisedFormat);
        final String pathString = getInternalConf().get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        final Predicate<HadoopResourceDetails> predicate = detail -> serialisedFormat.equals(detail.getFormat());
        return getMappings(pathString, predicate);
    }

    @Override
    public Boolean addResource(final LeafResource leafResource) {
        LOGGER.error(ERROR_ADD_RESOURCE);
        return false;
    }

    private Stream<LeafResource> getMappings(final String pathString, final Predicate<HadoopResourceDetails> predicate) {
        final RemoteIterator<LocatedFileStatus> remoteIterator;
        try {
            remoteIterator = this.getFileSystem().listFiles(new Path(pathString), true);

            return getPaths(remoteIterator)
                    .filter(HadoopResourceDetails::isValidResourceName)
                    .map(HadoopResourceDetails::getResourceDetailsFromFileName)
                    .filter(predicate)
                    .map(this::addConnectionDetail);
        } catch (IOException | IllegalStateException e) {
            LOGGER.error("Error while listing files: ", e);
            return Stream.empty();
        }
    }

    protected LeafResource addConnectionDetail(final HadoopResourceDetails hadoopResourceDetails) {
        if (this.dataServices.isEmpty()) {
            throw new IllegalStateException(ERROR_NO_DATA_SERVICES);
        }
        int serviceNum = ThreadLocalRandom.current().nextInt(this.dataServices.size());
        ConnectionDetail dataService = this.dataServices.get(serviceNum);

        return hadoopResourceDetails.getResource()
                .connectionDetail(dataService);
    }

    @Generated
    public HadoopResourceService conf(final Configuration conf) throws IOException {
        requireNonNull(conf, "conf");
        this.config = conf;
        this.fileSystem = FileSystem.get(conf);
        return this;
    }

    @Generated
    public HadoopResourceService addDataService(final ConnectionDetail detail) {
        requireNonNull(detail, "detail");
        dataServices.add(detail);
        return this;
    }

    @Generated
    protected Configuration getInternalConf() {
        return this.config;
    }

    @Generated
    protected FileSystem getFileSystem() {
        return this.fileSystem;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    public Map<String, String> getConf() {
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

    @Generated
    public void setConf(final Map<String, String> conf) throws IOException {
        requireNonNull(conf);
        this.conf(createConfig(conf));
    }

    @JsonIgnore
    @Generated
    public void setConf(final Configuration conf) throws IOException {
        requireNonNull(conf);
        this.conf(conf);
    }

    private Map<String, String> getPlainJobConfWithoutResolvingValues() {
        Map<String, String> plainMapWithoutResolvingValues = new HashMap<>();
        for (Entry<String, String> entry : new Configuration()) {
            plainMapWithoutResolvingValues.put(entry.getKey(), entry.getValue());
        }
        return plainMapWithoutResolvingValues;
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HadoopResourceService)) {
            return false;
        }
        final HadoopResourceService that = (HadoopResourceService) o;
        return Objects.equals(config, that.config) &&
                Objects.equals(fileSystem, that.fileSystem) &&
                Objects.equals(dataServices, that.dataServices);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(config, fileSystem, dataServices);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", HadoopResourceService.class.getSimpleName() + "[", "]")
                .add("config=" + config)
                .add("fileSystem=" + fileSystem)
                .add("dataServices=" + dataServices)
                .toString();
    }

    /**
     * Make Jackson interpret the deserialised list correctly.
     */
    private static class ConnectionDetailType extends TypeReference<List<ConnectionDetail>> {
    }
}
