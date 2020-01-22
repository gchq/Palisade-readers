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

import com.google.common.collect.Maps;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.test.PathUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.ParentResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.resource.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.resource.request.GetResourcesByResourceRequest;
import uk.gov.gchq.palisade.resource.request.GetResourcesBySerialisedFormatRequest;
import uk.gov.gchq.palisade.resource.request.GetResourcesByTypeRequest;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class HadoopResourceServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HadoopResourceServiceTest.class);
    private static final String TEST_RESOURCE_ID = "/home/user/other/thing_file.json";
    private static final String TEST_SERIALISED_FORMAT = "json";
    private static final String TEST_DATA_TYPE = "thing";
    private static final String TEST_CONNECTION_CLASS = "class of the connection";
    private static final String FORMAT_VALUE = "txt";
    private static final String TYPE_VALUE = "bob";
    private static final String FILE_NAME_VALUE_00001 = "00001";
    private static final String FILE_NAME_VALUE_00002 = "00002";
    private static final String FILE = System.getProperty("os.name").toLowerCase().startsWith("win") ? "file:///" : "file://";
    private static final String HDFS = "hdfs:///";
    private static File TMP_DIRECTORY;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder(TMP_DIRECTORY);

    private SimpleConnectionDetail simpleConnection;
    private String inputPathString;
    private FileSystem fs;
    private HashMap<uk.gov.gchq.palisade.resource.Resource, ConnectionDetail> expected;
    private Configuration config = new Configuration();
    private HadoopResourceService resourceService;

    static {
        TMP_DIRECTORY = PathUtils.getTestDir(HadoopResourceServiceTest.class);
    }

    @Before
    public void setup() throws IOException {
        // Windows hadoop
        System.setProperty("hadoop.home.dir", Paths.get(".").toAbsolutePath().normalize().toString() + "/src/test/resources/hadoop-bin");
        config = createConf();
        inputPathString = testFolder.getRoot().getAbsolutePath() + "/inputDir";
        fs = FileSystem.get(config);
        fs.mkdirs(new Path(inputPathString));
        expected = Maps.newHashMap();
        simpleConnection = new SimpleConnectionDetail().uri("localhost:8080");

        resourceService = new HadoopResourceService(config);
        resourceService.addDataService(simpleConnection);
    }

    @Test
    public void getResourcesByIdTest() throws Exception {

        //given
        final String id = inputPathString.replace("\\", "/") + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource().id(FILE + id).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(FILE + inputPathString.replace("\\", "/")).parent(
                        new SystemResource().id(FILE + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);

        //when
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = resourceService.getResourcesById(new GetResourcesByIdRequest().resourceId(FILE + id));

        //then
        assertEquals(expected, resourcesById.get());
    }

    @Test
    public void shouldGetResourcesOutsideOfScope() throws Exception {
        //given
        final String id = inputPathString.replace("\\", "/") + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);

        //when
        final String found = HDFS + "/unknownDir/" + id;
        try {
            resourceService.getResourcesById(new GetResourcesByIdRequest().resourceId(found));
            fail("exception expected");
        } catch (Exception e) {
            //then
            assertEquals(String.format(HadoopResourceService.ERROR_OUT_SCOPE, found, config.get(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY)), e.getMessage());
        }
    }

    @Test
    public void shouldGetResourcesByIdOfAFolder() throws Exception {
        //given
        final String id = inputPathString.replace("\\", "/");
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource().id(FILE + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(FILE + inputPathString.replace("\\", "/")).parent(
                        new SystemResource().id(FILE + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);
        expected.put(new FileResource().id(FILE + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(FILE + inputPathString.replace("\\", "/")).parent(
                        new SystemResource().id(FILE + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);

        //when
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = resourceService.getResourcesById(new GetResourcesByIdRequest().resourceId(FILE + id));

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldFilterOutIllegalFileName() throws Exception {
        //given
        final String id = inputPathString.replace("\\", "/");
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString + "/I AM AN ILLEGAL FILENAME");
        expected.put(new FileResource().id(FILE + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(FILE + inputPathString.replace("\\", "/")).parent(
                        new SystemResource().id(FILE + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);
        expected.put(new FileResource().id(FILE + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(FILE + inputPathString.replace("\\", "/")).parent(
                        new SystemResource().id(FILE + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);

        //when
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = resourceService.getResourcesById(new GetResourcesByIdRequest().resourceId(FILE + id));

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldGetResourcesByType() throws Exception {
        //given
        final String id = inputPathString.replace("\\", "/");
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, "00003", FORMAT_VALUE, TYPE_VALUE + 2);
        expected.put(new FileResource().id(FILE + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(FILE + inputPathString.replace("\\", "/")).parent(
                        new SystemResource().id(FILE + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);
        expected.put(new FileResource().id(FILE + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(FILE + inputPathString.replace("\\", "/")).parent(
                        new SystemResource().id(FILE + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);

        //when
        GetResourcesByTypeRequest getResourcesByTypeRequest = new GetResourcesByTypeRequest().type(TYPE_VALUE);
        getResourcesByTypeRequest.setOriginalRequestId(new RequestId().id("test shouldGetResourcesByType"));
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = resourceService.getResourcesByType(getResourcesByTypeRequest);

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldGetResourcesByFormat() throws Exception {
        //given
        final String id = inputPathString.replace("\\", "/");
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, "00003", FORMAT_VALUE + 2, TYPE_VALUE);
        expected.put(new FileResource().id(FILE + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(FILE + inputPathString.replace("\\", "/")).parent(
                        new SystemResource().id(FILE + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);
        expected.put(new FileResource().id(FILE + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(FILE + inputPathString.replace("\\", "/")).parent(
                        new SystemResource().id(FILE + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);

        //when
        GetResourcesBySerialisedFormatRequest getResourcesBySerialisedFormatRequest = new GetResourcesBySerialisedFormatRequest().serialisedFormat(FORMAT_VALUE);
        getResourcesBySerialisedFormatRequest.setOriginalRequestId(new RequestId().id("test shouldGetResourcesByFormat"));
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = resourceService.getResourcesBySerialisedFormat(getResourcesBySerialisedFormatRequest);

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldGetResourcesByResource() throws Exception {
        //given
        final String id = inputPathString.replace("\\", "/");
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource().id(FILE + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(FILE + inputPathString.replace("\\", "/")).parent(
                        new SystemResource().id(FILE + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);
        expected.put(new FileResource().id(FILE + id + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00002, TYPE_VALUE, FORMAT_VALUE)).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(FILE + inputPathString.replace("\\", "/")).parent(
                        new SystemResource().id(FILE + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);
        //when
        GetResourcesByResourceRequest getResourcesByResourceRequest = new GetResourcesByResourceRequest().resource(new DirectoryResource().id(FILE + id));
        getResourcesByResourceRequest.setOriginalRequestId(new RequestId().id("test shouldGetResourcesByResource"));
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = resourceService.getResourcesByResource(getResourcesByResourceRequest);

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void addResourceTest() throws Exception {
        try {
            resourceService.addResource(null);
            fail("exception expected");
        } catch (UnsupportedOperationException e) {
            assertEquals(HadoopResourceService.ERROR_ADD_RESOURCE, e.getMessage());
        }
    }

    @Test
    public void shouldJSONSerialiser() throws Exception {
        //use local copy for this test
        final HadoopResourceService service = new HadoopResourceService(config);

        final byte[] serialise = JSONSerialiser.serialise(service, true);
        final String expected = String.format("{%n" +
                "  \"@id\" : 1,%n" +
                "  \"class\" : \"uk.gov.gchq.palisade.service.resource.service.HadoopResourceService\",%n" +
                "  \"conf\" : {%n" +
                "  }%n" +
                "}%n");

        final String stringOfSerialised = new String(serialise);
        final String[] split = stringOfSerialised.split(System.lineSeparator());
        final StringBuilder modified = new StringBuilder();
        for (String s : split) {
            if (!s.startsWith("    \"fs.defaultFS")) {
                modified.append(s).append(System.lineSeparator());
            }
        }

        final String modifiedActual = modified.toString();
        assertEquals(stringOfSerialised, expected, modifiedActual);
        assertEquals(service, JSONSerialiser.deserialise(serialise, HadoopResourceService.class));
    }

    @Test
    public void shouldErrorWithNoConnectionDetails() throws Exception {
        //given
        final String id = inputPathString.replace("\\", "/") + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource().id(id).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE), simpleConnection);

        //when
        try {
            //this test needs a local HDFS resource service
            final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = new HadoopResourceService(config)
                    .getResourcesById(new GetResourcesByIdRequest().resourceId(FILE + id));
            resourcesById.get();
            fail("exception expected");
        } catch (ExecutionException e) {
            //then
            assertEquals(HadoopResourceService.ERROR_NO_DATA_SERVICES, e.getCause().getMessage());
        }
    }

    @Test
    public void shouldGetFormatConnectionWhenNoTypeConnection() throws Exception {
        //given
        final String id = inputPathString.replace("\\", "/") + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00001, FORMAT_VALUE, TYPE_VALUE);
        writeFile(fs, inputPathString, FILE_NAME_VALUE_00002, FORMAT_VALUE, TYPE_VALUE);
        expected.put(new FileResource().id(FILE + id).type(TYPE_VALUE).serialisedFormat(FORMAT_VALUE).parent(
                new DirectoryResource().id(FILE + inputPathString.replace("\\", "/")).parent(
                        new SystemResource().id(FILE + testFolder.getRoot().getAbsolutePath())
                )
        ), simpleConnection);

        //when
        final CompletableFuture<Map<LeafResource, ConnectionDetail>> resourcesById = resourceService.getResourcesById(new GetResourcesByIdRequest().resourceId(FILE + id));

        //then
        assertEquals(expected, resourcesById.join());
    }

    @Test
    public void shouldResolveParents() throws Exception {
        final String parent = testFolder.getRoot().getAbsolutePath().replace("\\", "/") + "/inputDir" + "/" + "folder1" + "/" + "folder2/";
        final String id = parent + "/" + getFileNameFromResourceDetails(FILE_NAME_VALUE_00001, TYPE_VALUE, FORMAT_VALUE);
        final FileResource fileResource = new FileResource().id(id);
        HadoopResourceService.resolveParents(fileResource, config);

        final ParentResource parent1 = fileResource.getParent();
        assertEquals(parent, parent1.getId());

        assertTrue(parent1 instanceof ChildResource);
        assertTrue(parent1 instanceof DirectoryResource);
        final ChildResource child = (ChildResource) parent1;
        HadoopResourceService.resolveParents(child, config);
        final ParentResource parent2 = child.getParent();
        assertEquals(testFolder.getRoot().getAbsolutePath().replace("\\", "/") + "/inputDir" + "/" + "folder1/", parent2.getId());

        assertTrue(parent2 instanceof ChildResource);
        assertTrue(parent2 instanceof DirectoryResource);
        final ChildResource child2 = (ChildResource) parent2;
        HadoopResourceService.resolveParents(child2, config);
        final ParentResource parent3 = child2.getParent();
        assertEquals(testFolder.getRoot().getAbsolutePath().replace("\\", "/") + "/inputDir/", parent3.getId());

        assertTrue(parent3 instanceof ChildResource);
        assertTrue(parent3 instanceof DirectoryResource);
        final ChildResource child3 = (ChildResource) parent3;
        HadoopResourceService.resolveParents(child3, config);
        final ParentResource parent4 = child3.getParent();
        assertEquals(testFolder.getRoot().getAbsolutePath().replace("\\", "/") + "/", parent4.getId());

        assertTrue(parent4 instanceof SystemResource);
        assertFalse(parent4 instanceof DirectoryResource);
    }

    private LeafResource mockResource() {
        final LeafResource leafResource = Mockito.mock(LeafResource.class);
        when(leafResource.getId()).thenReturn(TEST_RESOURCE_ID);
        when(leafResource.getType()).thenReturn(TEST_DATA_TYPE);
        when(leafResource.getSerialisedFormat()).thenReturn(TEST_SERIALISED_FORMAT);
        return leafResource;
    }

    private ConnectionDetail mockConnection() {
        final ConnectionDetail connectionDetail = Mockito.mock(ConnectionDetail.class);
        when(connectionDetail._getClass()).thenReturn(TEST_CONNECTION_CLASS);
        return connectionDetail;
    }

    private CompletableFuture<Map<LeafResource, ConnectionDetail>> mockCompletableFuture() {

        final CompletableFuture<Map<LeafResource, ConnectionDetail>> future = new CompletableFuture<Map<LeafResource, ConnectionDetail>>();
        final Map<LeafResource, ConnectionDetail> map = new HashMap<>();
        map.put(mockResource(), mockConnection());
        future.complete(map);
        return future;
    }

    private Configuration createConf() {
        // Set up local conf
        final Configuration conf = new Configuration();
        conf.set(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY, FILE + testFolder.getRoot().getAbsolutePath().replace("\\", "/"));
        return conf;
    }

    private void writeFile(final FileSystem fs, final String parentPath, final String name, final String format, final String type) throws IOException {
        writeFile(fs, parentPath + "/" + getFileNameFromResourceDetails(name, type, format));
    }

    private void writeFile(final FileSystem fs, final String filePathString) throws IOException {
        //Write Some file
        final Path filePath = new Path(filePathString);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fs.create(filePath, true)))) {
            writer.write("myContents");
        }
    }

    private static String getFileNameFromResourceDetails(final String name, final String type, final String format) {
        //Type, Id, Format
        return type + "_" + name + "." + format;
    }
}
