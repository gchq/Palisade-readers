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

package uk.gov.gchq.palisade.service.resource.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.service.resource.common.resource.LeafResource;

import java.io.IOException;

/**
 * Configuration class to create a Hadoop Resource Service
 */
public class ConfiguredHadoopResourceService extends HadoopResourceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfiguredHadoopResourceService.class);

    /**
     * Configuration constructor, taking in the hadoop config used to connect to hadoop
     *
     * @param configuration {@link org.apache.hadoop.conf.Configuration} configuration
     * @throws IOException the {@link Exception} thrown when there is an issue getting the {@link FileSystem} from the {@link Configuration}
     */
    public ConfiguredHadoopResourceService(final Configuration configuration) throws IOException {
        super(configuration);
    }

    @Override
    public Boolean addResource(final LeafResource leafResource) {
        LOGGER.info("Adding connectionDetail {} for leafResource {}", leafResource.getConnectionDetail(), leafResource);
        this.addDataService(leafResource.getConnectionDetail());
        return true;
    }
}
