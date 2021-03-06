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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.resource.stream.config.AkkaSystemConfig;
import uk.gov.gchq.palisade.util.AbstractResourceBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static uk.gov.gchq.palisade.service.resource.s3.S3Initialiser.localStackContainer;

@SpringBootTest(classes = {S3Configuration.class, AkkaSystemConfig.class})
@ContextConfiguration(initializers = {S3Initialiser.class})
@ActiveProfiles({"s3", "testcontainers"})
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
class S3ResourceServiceTest {

    @Autowired
    S3ResourceService service;
    @Autowired
    S3Properties s3Properties;

    // Use a 'default' bucket for S3ResourceService getByType/Format methods
    private static final String BUCKET_NAME = "default";

    FileResource s3ResourceText;
    FileResource s3ResourceAvro;
    FileResource s3ResourceFormatText;

    private S3Client s3;

    @BeforeAll
    void setup() {
        s3 = S3Client
                .builder()
                .endpointOverride(localStackContainer.getEndpointOverride(S3))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(localStackContainer.getAccessKey(), localStackContainer.getSecretKey())))
                .build();

        s3ResourceText = ((FileResource) ((LeafResource) AbstractResourceBuilder.create("s3://" + BUCKET_NAME + "/testFile.txt"))
                .type("text")
                .serialisedFormat("text/plain; charset=UTF-8")
                .connectionDetail(new SimpleConnectionDetail().serviceName(s3Properties.getConnectionDetail())))
                .attribute(S3ResourceService.USER_META_ATTR_PREFIX + s3Properties.getPalisadeTypeHeader(), "text")
                .attribute(S3ResourceService.USER_META_ATTR_PREFIX + s3Properties.getPalisadeFormatHeader(), "text/plain; charset=UTF-8");

        s3ResourceAvro = ((FileResource) ((LeafResource) AbstractResourceBuilder.create("s3://" + BUCKET_NAME + "/testAvroFile.avro"))
                .type("avro")
                .serialisedFormat("avro/plain; charset=UTF-8")
                .connectionDetail(new SimpleConnectionDetail().serviceName(s3Properties.getConnectionDetail())))
                .attribute(S3ResourceService.USER_META_ATTR_PREFIX + s3Properties.getPalisadeTypeHeader(), "avro")
                .attribute(S3ResourceService.USER_META_ATTR_PREFIX + s3Properties.getPalisadeFormatHeader(), "avro/plain; charset=UTF-8");

        s3ResourceFormatText = ((FileResource) ((LeafResource) AbstractResourceBuilder.create("s3://" + BUCKET_NAME + "/testFormatTextFile.txt"))
                .type("text")
                .serialisedFormat("randomFormat")
                .connectionDetail(new SimpleConnectionDetail().serviceName(s3Properties.getConnectionDetail())))
                .attribute(S3ResourceService.USER_META_ATTR_PREFIX + s3Properties.getPalisadeTypeHeader(), "avro")
                .attribute(S3ResourceService.USER_META_ATTR_PREFIX + s3Properties.getPalisadeFormatHeader(), "text/randomformat");

        // Build the bucket
        s3.createBucket(b -> b.bucket(BUCKET_NAME));
    }

    @BeforeEach
    void addFilesToBucket() {
        // Add a test file to the bucket
        s3.putObject(b -> b.acl(ObjectCannedACL.PUBLIC_READ_WRITE)
                .bucket(BUCKET_NAME)
                .metadata(Map.of(s3Properties.getUserMetaPrefix() + s3Properties.getPalisadeTypeHeader(), s3ResourceText.getType(),
                        s3Properties.getUserMetaPrefix() + s3Properties.getPalisadeFormatHeader(), s3ResourceText.getSerialisedFormat()))
                .key(URI.create(s3ResourceText.getId()).getPath().substring(1)), RequestBody.fromString("Test Body"));

        // Add an avro file to the bucket
        s3.putObject(b -> b.acl(ObjectCannedACL.PUBLIC_READ_WRITE)
                .bucket(BUCKET_NAME)
                .metadata(Map.of(s3Properties.getUserMetaPrefix() + s3Properties.getPalisadeTypeHeader(), s3ResourceAvro.getType(),
                        s3Properties.getUserMetaPrefix() + s3Properties.getPalisadeFormatHeader(), s3ResourceAvro.getSerialisedFormat()))
                .key(URI.create(s3ResourceAvro.getId()).getPath().substring(1)), RequestBody.fromString("Test Body"));

        // Add a text file with a different serialised format to the bucket
        s3.putObject(b -> b.acl(ObjectCannedACL.PUBLIC_READ_WRITE)
                .bucket(BUCKET_NAME)
                .metadata(Map.of(s3Properties.getUserMetaPrefix() + s3Properties.getPalisadeTypeHeader(), s3ResourceFormatText.getType(),
                        s3Properties.getUserMetaPrefix() + s3Properties.getPalisadeFormatHeader(), s3ResourceFormatText.getSerialisedFormat()))
                .key(URI.create(s3ResourceFormatText.getId()).getPath().substring(1)), RequestBody.fromString("Test Body"));
    }

    @Test
    @Order(1)
    void testAutowiring() {
        assertThat(service)
                .as("Check that the service has been started successfully")
                .isNotNull();

        assertThat(localStackContainer)
                .as("Check that the localstack container has been started successfully")
                .isNotNull();
    }

    @Test
    @Order(2)
    void testBucketExits() {
        var bucketsMatchingPropertiesBucketName = s3.listBuckets()
                .buckets()
                .stream()
                .filter(b -> b.name().equals(BUCKET_NAME))
                .collect(Collectors.toList());

        assertThat(bucketsMatchingPropertiesBucketName)
                .as("Check one the bucket has been created in the Initializer")
                .asList()
                .hasSize(1)
                .extracting("name")
                .containsOnly(BUCKET_NAME);

        assertThat(service.canAccess(BUCKET_NAME).toCompletableFuture().join())
                .as("Check that the S3ResourceService knows the bucket: %s, exists", BUCKET_NAME)
                .isTrue();
    }

    @Test
    @Order(3)
    void testGetResourcesById() {
        // Given an empty list
        var resultList = new ArrayList<>();

        // When getting the resource from the S3 Resource Service using the resources Id
        var resourcesById = service.getResourcesById(s3ResourceText.getId());
        resourcesById.forEachRemaining(resultList::add);

        assertThat(resultList)
                .as("Check that when I get a resource by its Id, the correct resource is returned")
                .hasSize(1)
                .first()
                .usingRecursiveComparison()
                .ignoringFields("attributes")
                .isEqualTo(s3ResourceText);
    }

    @Test
    @Order(4)
    void testGetResourcesByType() {
        // Given an empty list
        var resultList = new ArrayList<>();

        // When getting the resource from the S3 Resource Service using the resources Id
        var resourcesByType = service.getResourcesByType(s3ResourceAvro.getType());
        resourcesByType.forEachRemaining(resultList::add);

        assertThat(resultList)
                .as("Check that when I get a resource by its type, the correct resource is returned")
                .hasSize(1)
                .first()
                .usingRecursiveComparison()
                .ignoringFields("attributes")
                .isEqualTo(s3ResourceAvro);
    }

    @Test
    @Order(5)
    void testGetResourcesBySerialisedFormat() {
        // Given an empty list
        var resultList = new ArrayList<>();

        // When getting the resource from the S3 Resource Service using the resources Id
        var resourcesBySerialisedFormat = service.getResourcesBySerialisedFormat(s3ResourceFormatText.getSerialisedFormat());
        resourcesBySerialisedFormat.forEachRemaining(resultList::add);

        assertThat(resultList)
                .as("Check that when I get a resource by its serialisedFormat, the correct resource is returned")
                .hasSize(1)
                .first()
                .usingRecursiveComparison()
                .ignoringFields("attributes")
                .isEqualTo(s3ResourceFormatText);
    }

}
