package uk.gov.gchq.palisade.service.resource.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(JUnit4.class)
public class PathUtilsTest {

    File testDir = PathUtils.getTestDir(PathUtilsTest.class);

    @Test
    public void testDirIsReadWriteable() throws IOException {
        // Given
        String testFile = "testFile";
        String testString = "testString";

        // When
        boolean readable = testDir.canRead();
        boolean writeable = testDir.canWrite();

        // Then
        assertThat(readable, equalTo(true));
        assertThat(writeable, equalTo(true));

        // Given
        File child = new File(testDir.getAbsolutePath() + File.separator + testFile);

        // When
        BufferedWriter writer = new BufferedWriter(new FileWriter(child));
        writer.write(testString);
        writer.close();
        //
        BufferedReader reader = new BufferedReader(new FileReader(child));
        String fileContents = reader.readLine();

        // Then
        assertThat(fileContents, equalTo(testString));
    }
}
