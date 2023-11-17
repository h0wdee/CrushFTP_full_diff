/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.logging;

import com.maverick.logging.AbstractLoggingContext;
import com.maverick.logging.DefaultLoggerContext;
import com.maverick.logging.IOUtils;
import com.maverick.logging.Log;
import com.maverick.logging.RandomAccessOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;

public class FileLoggingContext
extends AbstractLoggingContext {
    BufferedWriter currentWriter = null;
    OutputStream currentOut = null;
    RandomAccessFile currentFile = null;
    long maxSize;
    int maxFiles;
    File logFile;
    boolean logging = true;

    public FileLoggingContext(Log.Level level, File logFile) throws IOException {
        this(level, logFile, 10, 0x1400000L);
    }

    public FileLoggingContext(Log.Level level, File logFile, int maxFiles, long maxSize) throws IOException {
        super(level);
        this.logFile = logFile;
        if (!logFile.exists()) {
            logFile.getAbsoluteFile().getParentFile().mkdirs();
        }
        this.maxFiles = maxFiles;
        this.maxSize = maxSize;
        this.createLogFile();
        Runtime.getRuntime().addShutdownHook(new Thread(){

            @Override
            public void run() {
                FileLoggingContext.this.closeLog();
            }
        });
    }

    private void createLogFile() throws IOException {
        this.currentFile = new RandomAccessFile(this.logFile, "rw");
        this.currentFile.seek(this.currentFile.length());
        this.currentWriter = new BufferedWriter(new OutputStreamWriter(new RandomAccessOutputStream(this.currentFile)), 65536);
        this.log(Log.Level.INFO, String.format("Logging file %s", this.logFile.getAbsolutePath()), null, new Object[0]);
    }

    @Override
    public boolean isLogging(Log.Level level) {
        return this.logging && super.isLogging(level);
    }

    @Override
    public void log(Log.Level level, String msg, Throwable e, Object ... args) {
        try {
            this.logToFile(DefaultLoggerContext.prepareLog(level, msg, e, args), true);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private synchronized void logToFile(String msg, boolean flush) {
        try {
            this.checkRollingLog();
            if (this.currentFile.getChannel().isOpen()) {
                this.currentWriter.write(msg);
                if (flush) {
                    this.currentWriter.flush();
                }
            }
        }
        catch (IOException e) {
            System.err.println(String.format("Failed to log to %s", this.logFile.getName()));
            e.printStackTrace();
            this.logging = false;
        }
    }

    private void closeLog() {
        IOUtils.closeStream(this.currentWriter);
        IOUtils.closeStream(this.currentOut);
    }

    private synchronized void checkRollingLog() throws IOException {
        if (this.currentFile.getChannel().isOpen() && this.currentFile.length() > this.maxSize) {
            this.closeLog();
            IOUtils.rollover(this.logFile, this.maxFiles);
            this.createLogFile();
        }
    }

    @Override
    public synchronized void close() {
        this.closeLog();
    }

    @Override
    public void raw(Log.Level level, String msg) {
        this.logToFile(DefaultLoggerContext.prepareLog(level, "", null, new Object[0]), false);
        this.logToFile(msg, true);
    }

    @Override
    public void newline() {
        this.logToFile(System.lineSeparator(), true);
    }
}

