package cn.sheyifan;

import cn.sheyifan.utils.FxPDFsTerminal;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public class FxPDFsInstaller {
    private static final String BUILD = "target/build/";
    private static final String JRE_HOME = "target/build/runtime/";
    private static final Logger logger = Logger.getLogger(FxPDFsInstaller.class);

    public static void main(String... args) {
        if (generateMinimumJre()) {
            customJre();
        }
        copyNativeTools();
    }

    private static boolean generateMinimumJre() {
        try {
            if (Runtime.getRuntime().exec("jlink.exe --version").waitFor() != 0) {
                return false;
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        }


        int jlinkRet = new FxPDFsTerminal().exec("jlink",
                "--module-path",
                "target/build/lib/commons",
                "--add-modules",
                "javafx.fxml,javafx.controls,javafx.graphics," +
                        "jdk.charsets,java.sql,java.instrument",
                "--output", JRE_HOME);

        return jlinkRet == 0;
    }

    private static boolean customJre() {
        final Path userDir = Paths.get(System.getProperty("user.dir"));
        Path javaExe = userDir.resolve(JRE_HOME).resolve("bin/java.exe");
        boolean renameStatus = javaExe.toFile().renameTo(
                userDir.resolve(JRE_HOME).resolve("bin/fxpdfs_java.exe").toFile()
        );
        if (!renameStatus) {
            logger.error("Fail to rename java.exe in jre.");
            return false;
        }

        logger.info("Successfully renamed java.exe in jre.");
        return true;
    }

    private static boolean removeDirectoryRecursively(Path path) {
        try {
            if (Files.isDirectory(path)) {
                try (Stream<Path> files = Files.list(path)) {
                    files.forEach(FxPDFsInstaller::removeDirectoryRecursively);
                } catch (IOException e) {
                    logger.error("Fail to list files in " + path);
                }
            }

            Files.delete(path);
        } catch (IOException ioe) {
            logger.error("Fail to remove directory " + path);
            return false;
        }

        logger.info("Successfully removed directory " + path);
        return true;
    }

    private static boolean removeOldBuild() {
        Path buildDes = Paths.get(System.getProperty("user.dir")).resolve(BUILD);
        if (Files.exists(buildDes)) {
            boolean removeBuildRet = removeDirectoryRecursively(buildDes);
            if (removeBuildRet) {
                logger.info("Successfully clean build.");
                return true;
            }
            else {
                logger.error("Fail to clean build.");
                return false;
            }
        }
        return true;
    }

    private static void copyFolder(Path source, Path target, CopyOption... options)
            throws IOException {
        source = source.toAbsolutePath();
        target = target.toAbsolutePath();
        Path finalSource = source;
        Path finalTarget = target;
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                Files.createDirectories(finalTarget.resolve(finalSource.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.copy(file, finalTarget.resolve(finalSource.relativize(file)), options);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static boolean copyNativeTools() {
        try {
            copyFolder(Paths.get("tools/build/"), Paths.get("target/build/"));
        } catch (IOException e) {
            logger.error("Fail to copy native tools in " + "lib/win");
            return false;
        }

        return true;
    }
}