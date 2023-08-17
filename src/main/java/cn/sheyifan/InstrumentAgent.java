package cn.sheyifan;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import static java.nio.file.Files.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class InstrumentAgent {
    public static Instrumentation INST;

    public static final String LIB_COMMONS = "lib/commons";

    public static void premain(String args, Instrumentation inst) {
        INST = inst;
        Path cwd = Paths.get(System.getProperty("user.dir"));
        Path commonLib = cwd.resolve(LIB_COMMONS);
        loadLib(commonLib);
    }

    private static void loadLib(Path libPath) {
        if (exists(libPath) && isDirectory(libPath)) {
            try (Stream<Path> libs = list(libPath)) {
                libs.filter(lib -> lib.toString().toLowerCase().endsWith(".jar")).forEach(jarLib -> {
                    try {
                        INST.appendToSystemClassLoaderSearch(new JarFile(jarLib.toString()));
                    } catch (IOException e) {
                        System.err.println("Fail to load library: " + jarLib);
                    }
                });
            } catch (IOException e) {
                System.err.println("Fail to load library from " + libPath);
            }
        }
        else {
            System.err.println("Fail to find library from " + libPath);
        }
    }
}