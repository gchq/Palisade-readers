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

package uk.gov.gchq.palisade.service.resource.s3.util;

import uk.gov.gchq.palisade.resource.ParentResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.service.resource.s3.S3Resource;
import uk.gov.gchq.palisade.util.ResourceBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;

import static uk.gov.gchq.palisade.service.resource.s3.S3Properties.S3_PATH_SEP;
import static uk.gov.gchq.palisade.service.resource.s3.S3Properties.S3_PREFIX;

/**
 *
 */
public class S3ResourceBuilder {

    private S3ResourceBuilder() {
        // Empty Constructor
    }

    static {
        ResourceBuilder.registerBuilder(S3_PREFIX, S3ResourceBuilder::s3Scheme);
    }

    private static String parentPrefix(final String path) {
        var pathComponents = new LinkedList<>(Arrays.asList(path.split(S3_PATH_SEP)));
        pathComponents.removeLast();
        return String.join(S3_PATH_SEP, pathComponents);
    }

    private static S3Resource fileResource(final String key) {
        return (S3Resource) new S3Resource()
                .id(key)
                .parent(parentResource(parentPrefix(key)));
    }

    private static ParentResource parentResource(final String prefix) {
        if (prefix.isEmpty()) {
            return new SystemResource()
                    .id("/");
        } else {
            return new DirectoryResource()
                    .id(prefix)
                    .parent(parentResource(parentPrefix(prefix)));
        }
    }

    private static Resource s3Scheme(final URI uri) {
        // Things in S3 are only files, even if the PATH_SEP or prefixes makes it look like directories
        return fileResource(uri.getSchemeSpecificPart());
    }
}