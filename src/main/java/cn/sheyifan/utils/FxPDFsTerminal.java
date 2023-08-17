package cn.sheyifan.utils;

import org.apache.log4j.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class FxPDFsTerminal {
    private Logger logger = Logger.getLogger(FxPDFsTerminal.class);

    private AtomicBoolean processContinue = new AtomicBoolean(true);

    private Process process;

    private Thread readCmdOutThread;

    private Thread readCmdErrThread;

    private int lastCommandRetValue = 0;

    private Runtime runtime = Runtime.getRuntime();

    private Thread commandLineReaderThread(InputStream inputStream) {
        Thread thread = new Thread(() -> {
            var bufReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while (true) {
                try {
                    if (!processContinue.get()) {
                        break;
                    }

                    line = bufReader.readLine();
                    if (line != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    logger.error("Fail to read from command output");
                }
            }
        });
        thread.setDaemon(true);
        return thread;
    }

    public int exec(String... command) {
        try {
            process = runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Execute command (PID: " + process.pid() + "): " + String.join(" ", command));

        readCmdOutThread = commandLineReaderThread(process.getInputStream());
        readCmdErrThread = commandLineReaderThread(process.getErrorStream());
        readCmdOutThread.start();
        readCmdErrThread.start();

        try {
            lastCommandRetValue = process.waitFor();
        } catch (InterruptedException e) {
            logger.error("Interrupted while executing command: (PID: " + process.pid() + "): " + String.join(" ", command));
        }
        processContinue.set(false);
        logger.info("process " + process.pid() + " finish with exit code " + lastCommandRetValue);
        return lastCommandRetValue;
    }

    public void halt() {
        process.destroy();
        readCmdErrThread.interrupt();
        readCmdOutThread.interrupt();
    }
}