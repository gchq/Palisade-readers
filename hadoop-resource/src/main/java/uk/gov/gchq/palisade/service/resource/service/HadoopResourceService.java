/*
 * Copyright 2018-2021 Crown Copyright
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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.Generated;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.ResourceService;
import uk.gov.gchq.palisade.service.resource.util.FunctionalIterator;
import uk.gov.gchq.palisade.service.resource.util.HadoopResourceDetails;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * An implementation of the ResourceService.
 * <p>
 * This service is for the retrieval of Resources only. Resources cannot be added via this service, they should be added
 * to the backing hadoop filesystem.
 */

public class HadoopResourceService implements ResourceService {

    public static final String ERROR_ADD_RESOURCE = "AddResource is not supported by the Resource Service, resources should be added/created via regular file system behaviour.";
    public static final String ERROR_OUT_SCOPE = "resource ID is out of scope of the this resource Service. Found: %s expected: %s";
    public static final String ERROR_NO_DATA_SERVICES = "No Hadoop data services known about in Hadoop resource service";
    private static final Logger LOGGER = LoggerFactory.getLogger(HadoopResourceService.class);

    private Configuration config;
    private FileSystem fileSystem;

    private final List<ConnectionDetail> dataServices = new ArrayList<>();

    /**
     * Creates a new {@link HadoopResourceService} object from a {@link Configuration} object.
     *
     * @param config        A Hadoop {@link Configuration} object
     * @throws IOException  the {@link Exception} thrown when there is an issue getting the {@link FileSystem} from the {@link Configuration}
     */
    public HadoopResourceService(final Configuration config) throws IOException {
        requireNonNull(config, "Hadoop Configuration");
        this.config = config;
        this.fileSystem = FileSystem.get(config);
    }

    /**
     * Creates a new {@link HadoopResourceService} object from a {@link Map} of {@link String}s.
     *
     * @param conf A {@link Map} of {@link String}s used as the configuration
     * @throws IOException the {@link Exception} thrown when there is an issue creating a {@link HadoopResourceService} using the provided {@link Map} of {@link String}s.
     */
    public HadoopResourceService(final Map<String, String> conf) throws IOException {
        this(createConfig(conf));
    }

    public HadoopResourceService() {
    }

    protected static URI getPaths(final LocatedFileStatus fileStatus) {
        return fileStatus.getPath().toUri();
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

    /**
     * Get a list of resources based on a specific resource. This allows for the retrieval of the appropriate {@link
     * ConnectionDetail}s for a given resource. It may also be used to retrieve the details all the resources that are
     * notionally children of another resource. For example, in a standard hierarchical filing system, the files in a
     * directory could be considered child resources and calling this method on the directory resource would fetch the
     * details on the contained files.
     *
     * @param resource the resource to request
     * @return an {@link Iterator} of resources, each with an appropriate {@link ConnectionDetail}
     */
    @Override
    public Iterator<LeafResource> getResourcesByResource(final Resource resource) {
        requireNonNull(resource, "resource");
        LOGGER.debug("Invoking getResourcesByResource with request: {}", resource);
        return getResourcesById(resource.getId());
    }

    /**
     * Retrieve resource and connection details by resource ID. The request object allows the client to specify the
     * resource ID and obtain the connection details once the returned future has completed.
     *
     * @param resourceId the ID to request
     * @return an {@link Iterator} of resources, each with an appropriate {@link ConnectionDetail}
     */
    @Override
    public Iterator<LeafResource> getResourcesById(final String resourceId) {
        requireNonNull(resourceId, "resourceId");
        LOGGER.debug("Invoking getResourcesById with id: {}", resourceId);
        final String path = getInternalConf().get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        if (!resourceId.startsWith(path)) {
            throw new UnsupportedOperationException(java.lang.String.format(ERROR_OUT_SCOPE, resourceId, path));
        }
        return getMappings(resourceId, ignore -> true);
    }

    /**
     * Obtain a list of resources that match a specific resource type. This method allows a client to obtain potentially
     * large collections of resources by requesting all the resources of one particular type. For example, a client may
     * request all "employee contact card" records. Please note the warning in the class documentation above, that just
     * because a resource is available does not guarantee that the requesting client has the right to access it.
     *
     * @param type the type of resource to retrieve.
     * @return an {@link Iterator} of resources, each with an appropriate {@link ConnectionDetail}
     */
    @Override
    public Iterator<LeafResource> getResourcesByType(final String type) {
        requireNonNull(type, "type");
        LOGGER.debug("Invoking getResourcesByType with type: {}", type);
        final String pathString = getInternalConf().get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        final Predicate<HadoopResourceDetails> predicate = detail -> type.equals(detail.getType());
        return getMappings(pathString, predicate);
    }

    /**
     * Find all resources that match a particular data format. Resources of a particular data format may not share a
     * type, e.g. not all CSV format records will contain employee contact details. This method allows clients to
     * retrieve all the resources Palisade knows about that conform to one particular format. Note that this method can
     * potentially return large ${@code Map}s with many mappings.
     *
     * @param serialisedFormat the specific format for retrieval
     * @return an {@link Iterator} of resources, each with an appropriate {@link ConnectionDetail}
     */
    @Override
    public Iterator<LeafResource> getResourcesBySerialisedFormat(final String serialisedFormat) {
        requireNonNull(serialisedFormat, "serialisedFormat");
        LOGGER.debug("Invoking getResourcesBySerialisedFormat with serialisedFormat: {}", serialisedFormat);
        final String pathString = getInternalConf().get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
        final Predicate<HadoopResourceDetails> predicate = detail -> serialisedFormat.equals(detail.getFormat());
        return getMappings(pathString, predicate);
    }

    /**
     * Informs Palisade about a specific resource that it may return to users. This lets Palisade clients request access
     * to a resource and allows Palisade to provide policy controlled access to it via the other methods in this interface.
     * This is not permitted by the HadoopResourceService, so it will always return failure (false).
     *
     * @param leafResource         the resource that Palisade can manage access to
     * @return whether or not the addResource call completed successfully, always false
     */
    @Override
    public Boolean addResource(final LeafResource leafResource) {
        LOGGER.error(ERROR_ADD_RESOURCE);
        return false;
    }

    private FunctionalIterator<LeafResource> getMappings(final String pathString, final Predicate<HadoopResourceDetails> predicate) {
        try {
            return FunctionalIterator.fromIterator(this.getFileSystem().listFiles(new Path(pathString), true))
                    .map(HadoopResourceService::getPaths)
                    .filter(HadoopResourceDetails::isValidResourceName)
                    .map(HadoopResourceDetails::getResourceDetailsFromFileName)
                    .filter(predicate)
                    .map(this::addConnectionDetail);
        } catch (IOException | IllegalStateException e) {
            LOGGER.error("Error while listing files: ", e);
            return FunctionalIterator.fromIterator(Collections.emptyIterator());
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

    /**
     * Sets the {@link Configuration} and {@link FileSystem} values.
     *
     * @param conf          A Hadoop {@link Configuration} object
     * @return              the current {@link HadoopResourceService} object
     * @throws IOException  the {@link Exception} thrown when there is an issue getting the {@link FileSystem} from the {@link Configuration}
     */
    @Generated
    public HadoopResourceService conf(final Configuration conf) throws IOException {
        requireNonNull(conf, "conf");
        this.config = conf;
        this.fileSystem = FileSystem.get(conf);
        return this;
    }

    /**
     * Adds a {@link ConnectionDetail} value to the {@link List} of Data Services.
     *
     * @param detail    A {@link ConnectionDetail} object to be added
     * @return          the current {@link HadoopResourceService} object
     */
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
}
