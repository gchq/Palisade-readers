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
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.util.AbstractResourceBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;

import static uk.gov.gchq.palisade.service.resource.s3.S3Properties.S3_PATH_SEP;
import static uk.gov.gchq.palisade.service.resource.s3.S3Properties.S3_PREFIX;

/**
 * Builds an S3Resource, by extending the {@link AbstractResourceBuilder}
 */
public class S3ResourceBuilder extends AbstractResourceBuilder {
    private static final String S3_PATH_SEP = "/";
    private static final String S3_PREFIX = "s3";
    private static final int NO_COMPONENTS = 0;
    private static final int SCHEME_ONLY = 1;
    private static final int SCHEME_AND_PATH_NO_AUTH = 2;
    private static final int SCHEME_AND_AUTHORITY_ONLY = 3;

    public S3ResourceBuilder() {
        // Empty Constructor
    }

    private static LinkedList<String> splitComponents(final String path) {
        return new LinkedList<>(Arrays.asList(path.split(S3_PATH_SEP)));
    }

    private static FileResource s3ObjectResource(final String key) {
        return new FileResource().id(key);
    }

    private static ParentResource s3ParentResource(final String prefix) {
        switch (splitComponents(prefix).size()) {
            case NO_COMPONENTS: // ''
            case SCHEME_ONLY: // 's3:'
            case SCHEME_AND_PATH_NO_AUTH: // 's3:/something'
                // If we got an invalid uri
                throw new IllegalArgumentException("Prefix '" + prefix + "' was too short, expected at least 's3://<bucketname>'");
            case SCHEME_AND_AUTHORITY_ONLY: // 's3://bucket'
                // If we got the root resource
                return new SystemResource().id(prefix);
            default: // 's3://bucket/dir[/other-dir]'
                // If we got a directory
                return new DirectoryResource().id(prefix);
        }
    }

    private static Resource s3Scheme(final URI uri) {
        // Things in S3 are only really files, even if the PATH_SEP or prefixes makes it look like directories
        // However, for parsing resources within Palisade, they are all treated as if directories exist
        if (uri.toString().endsWith(S3_PATH_SEP)) {
            return s3ParentResource(uri.toString());
        } else {
            return s3ObjectResource(uri.toString());
        }
    }

    @Override
    protected Resource build(final URI resourceUri) {
        return s3Scheme(resourceUri);
    }

    @Override
    public boolean accepts(final URI resourceUri) {
        return resourceUri.getScheme().equals(S3_PREFIX);
    }
}
