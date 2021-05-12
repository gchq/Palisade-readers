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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class S3ReaderApplication {
    public static void main(final String[] args) {
        SpringApplication.run(S3ReaderApplication.class, args);
    }

    /**
     * Performs the tasks that need to be done after Spring initialisation and before running the service. This
     * includes the configuration of the serialiser and the starting of the Kafka sinks used for sending audit
     * messages to the Audit Service.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initPostConstruct() {
        //TODO later set-up initalise the connection to the S3

    }
}
