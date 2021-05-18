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

package uk.gov.gchq.palisade.service.resource.s3.loader;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import uk.gov.gchq.palisade.service.resource.ResourceApplication;
import uk.gov.gchq.palisade.service.resource.s3.S3Initializer;
import uk.gov.gchq.palisade.service.resource.service.ResourceService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An external requirement of the service is to connect to a pair of kafka topics.
 * The upstream "user" topic is written to by the user-service and read by this service.
 * The downstream "resource" topic is written to by this service and read by the policy-service.
 * Upon writing to the upstream topic, appropriate messages should be written to the downstream topic.
 */
@SpringBootTest(
        classes = ResourceApplication.class,
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {"akka.discovery.config.services.kafka.from-config=false"}
)
@ContextConfiguration(initializers = S3Initializer.class)
@Import(KafkaTestConfiguration.class)
@ActiveProfiles({"db-test", "akka-test", "test-resource", "testcontainers", "s3"})
class KafkaContractTest {
    @Autowired
    ResourceApplication app;
    @Autowired
    ResourceService svc;

    @Test
    void testContextLoads() {
        assertThat(app).isNotNull();
    }
}
