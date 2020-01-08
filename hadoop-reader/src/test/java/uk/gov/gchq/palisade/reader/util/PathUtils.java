package uk.gov.gchq.palisade.reader.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hadoop.fs.Path;

import java.io.File;

// PathUtils has been removed (?) from Hadoop at some point
// It used to be available under hadoop-hdfs, it is now included here
// This was previously presenting some windows/unix compatibility problems
public class PathUtils {
    public static Path getTestPath(Class<?> caller) {
        return getTestPath(caller, true);
    }

    public static Path getTestPath(Class<?> caller, boolean create) {
        return new Path(getTestDirName(caller));
    }

    public static File getTestDir(Class<?> caller) {
        return getTestDir(caller, true);
    }

    public static File getTestDir(Class<?> caller, boolean create) {
        File dir = new File(System.getProperty("test.build.data", "target/test/data")
                + "/" + RandomStringUtils.randomAlphanumeric(10),
                caller.getSimpleName());
        if (create) {
            dir.mkdirs();
        }
        return dir;
    }

    public static String getTestDirName(Class<?> caller) {
        return getTestDirName(caller, true);
    }

    public static String getTestDirName(Class<?> caller, boolean create) {
        return getTestDir(caller, create).getAbsolutePath();
    }
}
