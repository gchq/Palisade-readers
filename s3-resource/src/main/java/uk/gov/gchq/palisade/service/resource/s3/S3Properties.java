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

@ConfigurationProperties(prefix = "s3")
public class S3Properties {
    private String bucketName;
    private String region;
    private String connectionDetail = "s3-data-service";
    private String palisadeTypeHeader = "x-pal-type";
    private String userMetaPrefix = "x-amz-meta-";
    public static final String S3_PREFIX = "s3";
    public static final String S3_PATH_SEP = "/";

    @Generated
    public String getBucketName() {
        return bucketName;
    }

    @Generated
    public void setBucketName(final String bucketName) {
        this.bucketName = bucketName;
    }

    @Generated
    public String getRegion() {
        return region;
    }

    @Generated
    public void setRegion(final String region) {
        this.region = region;
    }

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
    public String getUserMetaPrefix() {
        return userMetaPrefix;
    }

    @Generated
    public void setUserMetaPrefix(final String userMetaPrefix) {
        this.userMetaPrefix = userMetaPrefix;
    }
}