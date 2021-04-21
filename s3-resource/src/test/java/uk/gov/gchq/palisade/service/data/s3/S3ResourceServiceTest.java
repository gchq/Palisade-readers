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

import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
class S3ResourceServiceTest {

    @Container
    private final LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.12.9.1"))
            .withServices(S3);

    @Test
    void testV1() {
        // AWS SDK v1
        var s3 = AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(localstack.getEndpointConfiguration(S3))
                .withCredentials(localstack.getDefaultCredentialsProvider())
                .build();

        s3.createBucket("foo");
        s3.putObject("foo", "bar", "baz");

        assertThat(s3.listBuckets())
                .as("Check one bucket exists")
                .asList()
                .hasSize(1);

        assertThat(s3.listObjects("foo").getObjectSummaries())
                .as("Check one object is returned")
                .asList()
                .hasSize(1);
    }

    @Test
    void testV2() {
        // AWS SDK v2
        var s3 = S3Client
                .builder()
                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.S3))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        localstack.getAccessKey(), localstack.getSecretKey()
                )))
                .region(Region.of(localstack.getRegion()))
                .build();


        s3.createBucket(b -> b.bucket("foo"));
        s3.putObject(b -> b.bucket("foo").key("bar"), RequestBody.fromBytes("baz".getBytes()));

        assertThat(s3.listBuckets().buckets())
                .as("Check one bucket exists")
                .asList()
                .hasSize(1);

        var lOR = ListObjectsRequest.builder().bucket("foo").build();
        assertThat(s3.listObjects(lOR).contents())
                .as("Check one object is returned")
                .asList()
                .hasSize(1);
    }
}