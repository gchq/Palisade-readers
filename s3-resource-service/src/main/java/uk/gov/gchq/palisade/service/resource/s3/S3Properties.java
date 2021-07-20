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

package uk.gov.gchq.palisade.service.resource.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

import uk.gov.gchq.palisade.Generated;

/**
 * S3 Properties class, containing default information about resources added to S3.
 */
@ConfigurationProperties(prefix = "s3")
public class S3Properties {
    public static final String S3_PREFIX = "s3";
    public static final String S3_PATH_SEP = "/";
    private String connectionDetail = "data-service";
    private String palisadeTypeHeader = "x-pal-type";
    private String palisadeFormatHeader = "x-pal-format";
    private String userMetaPrefix = "x-amz-meta-";

    @Generated
    public String getConnectionDetail() {
        return connectionDetail;
    }

    @Generated
    public void setConnectionDetail(final String connectionDetail) {
        this.connectionDetail = connectionDetail;
    }

    @Generated
    public String getPalisadeTypeHeader() {
        return palisadeTypeHeader;
    }

    @Generated
    public void setPalisadeTypeHeader(final String palisadeTypeHeader) {
        this.palisadeTypeHeader = palisadeTypeHeader;
    }

    @Generated
    public String getPalisadeFormatHeader() {
        return palisadeFormatHeader;
    }

    @Generated
    public void setPalisadeFormatHeader(final String palisadeFormatHeader) {
        this.palisadeFormatHeader = palisadeFormatHeader;
    }

    @Generated
    public String getUserMetaPrefix() {
        return userMetaPrefix;
    }

    @Generated
    public void setUserMetaPrefix(final String userMetaPrefix) {
        this.userMetaPrefix = userMetaPrefix;
    }
}
