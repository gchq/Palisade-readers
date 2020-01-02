/*
 * Copyright 2019 Crown Copyright
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

package uk.gov.gchq.palisade.service.resource.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.request.GetResourcesBySerialisedFormatRequest;
import uk.gov.gchq.palisade.resource.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.service.ConnectionDetail;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Unittests for HadoopResourceService here test that messages are logged appropriately.
 * These tests are valid and equivalent tests are used in palisade-services
 * However, Hadoop includes Log4j, which clashes with Logback
 **/
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class HadoopResourceServiceTest {
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @Spy
    private HadoopResourceService hadoopService;
    private Map<LeafResource, ConnectionDetail> resourceMap;

    @Before
    public void setUp() {
        logger = (Logger) LoggerFactory.getLogger(HadoopResourceService.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        hadoopService = new HadoopResourceService();
        resourceMap = Mockito.mock(Map.class);
        Mockito.when(hadoopService.getFutureMappings(Mockito.anyString(), Mockito.any())).thenReturn(CompletableFuture.supplyAsync(() -> resourceMap));
    }

    @After
    public void tearDown() {
        logger.detachAppender(appender);
        appender.stop();
    }

    private List<String> getMessages(Predicate<ILoggingEvent> predicate) {
        return appender.list.stream()
                .filter(predicate)
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList());
    }

    @Test
    public void infoOnGetByIdRequest() {
        // Given - hadoopService, mocked hadoop
        GetResourcesByIdRequest request = Mockito.mock(GetResourcesByIdRequest.class);

        // When
        hadoopService.getResourcesById(request);

        // Then
        List<String> infoMessages = getMessages(event -> event.getLevel() == Level.INFO);

        MatcherAssert.assertThat(infoMessages, Matchers.hasItems(
                Matchers.containsString(request.toString()),
                Matchers.containsString(resourceMap.toString())
        ));
    }

    @Test
    public void infoOnGetByTypeRequest() {
        // Given - hadoopService, mocked hadoop
        GetResourcesByTypeRequest request = Mockito.mock(GetResourcesByTypeRequest.class);

        // When
        hadoopService.getResourcesByType(request);

        // Then
        List<String> infoMessages = getMessages(event -> event.getLevel() == Level.INFO);

        MatcherAssert.assertThat(infoMessages, Matchers.hasItems(
                Matchers.containsString(request.toString()),
                Matchers.containsString(resourceMap.toString())
        ));
    }

    @Test
    public void infoOnGetByResourceRequest() {
        // Given - hadoopService, mocked hadoop
        GetResourcesByResourceRequest request = Mockito.mock(GetResourcesByResourceRequest.class);

        // When
        hadoopService.getResourcesByResource(request);

        // Then
        List<String> infoMessages = getMessages(event -> event.getLevel() == Level.INFO);

        MatcherAssert.assertThat(infoMessages, Matchers.hasItems(
                Matchers.containsString(request.toString()),
                Matchers.containsString(resourceMap.toString())
        ));
    }

    @Test
    public void infoOnGetBySerialisedFormatRequest() {
        // Given - hadoopService, mocked hadoop
        GetResourcesBySerialisedFormatRequest request = Mockito.mock(GetResourcesBySerialisedFormatRequest.class);

        // When
        hadoopService.getResourcesBySerialisedFormat(request);

        // Then
        List<String> infoMessages = getMessages(event -> event.getLevel() == Level.INFO);

        MatcherAssert.assertThat(infoMessages, Matchers.hasItems(
                Matchers.containsString(request.toString()),
                Matchers.containsString(resourceMap.toString())
        ));
    }
}
