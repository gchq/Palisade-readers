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
package uk.gov.gchq.palisade.service.data.s3;

import uk.gov.gchq.palisade.Generated;

import java.util.StringJoiner;

public class S3Bucket {

    private final String bucketName;
    private final String bucketKey;

    public S3Bucket(final String bucketName, final String bucketKey) {
        this.bucketName = bucketName;
        this.bucketKey = bucketKey;
    }

    @Generated
    public String getBucketName() {
        return bucketName;
    }

    @Generated
    public String getBucketKey() {
        return bucketKey;
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", S3Bucket.class.getSimpleName() + "[", "]")
                .add("bucketName'" + bucketName + "'")
                .add("bucketKey='" + bucketKey + "'")
                .toString();
    }
}
