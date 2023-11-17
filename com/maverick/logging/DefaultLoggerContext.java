/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.logging;

import com.maverick.logging.ConsoleLoggingContext;
import com.maverick.logging.FileLoggingContext;
import com.maverick.logging.IOUtils;
import com.maverick.logging.Log;
import com.maverick.logging.LoggerContext;
import com.maverick.logging.RootLoggerContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultLoggerContext
implements RootLoggerContext {
    Collection<LoggerContext> contexts = new ArrayList<LoggerContext>();
    static DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm:ss,SSS");
    Properties props;
    File propertiesFile = new File(System.getProperty("maverick.log.config", "logging.properties"));
    FileWatcher watcher;

    public DefaultLoggerContext() {
        this.loadFile();
        if ("true".equalsIgnoreCase(this.getProperty("maverick.log.nothread", "false"))) {
            return;
        }
        this.watcher = new FileWatcher(this.propertiesFile);
        this.watcher.start();
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return this.processTokenReplacements(this.props.getProperty(key, defaultValue), System.getProperties());
    }

    @Override
    public void shutdown() {
        this.watcher.stopThread();
    }

    public String processTokenReplacements(String value, Properties tokenResolver) {
        Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(value);
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (matcher.find()) {
            String attributeName = matcher.group(1);
            String attributeValue = tokenResolver.getProperty(attributeName);
            if (attributeValue == null) continue;
            builder.append(value.substring(i, matcher.start()));
            builder.append(attributeValue);
            i = matcher.end();
        }
        builder.append(value.substring(i, value.length()));
        return builder.toString();
    }

    private synchronized void loadFile() {
        for (LoggerContext ctx : this.contexts) {
            ctx.close();
        }
        this.contexts.clear();
        if (this.propertiesFile.exists()) {
            this.props = new Properties();
            try (FileInputStream in = new FileInputStream(this.propertiesFile);){
                this.props.load(in);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.props = System.getProperties();
        }
        if ("true".equalsIgnoreCase(this.getProperty("maverick.log.console", "false"))) {
            this.enableConsole(Log.Level.valueOf(this.getProperty("maverick.log.console.level", "INFO")));
        }
        if ("true".equalsIgnoreCase(this.getProperty("maverick.log.file", "false"))) {
            this.enableFile(Log.Level.valueOf(this.getProperty("maverick.log.file.level", "INFO")), new File(this.getProperty("maverick.log.file.path", "synergy.log")), Integer.parseInt(this.getProperty("maverick.log.file.maxFiles", "10")), IOUtils.fromByteSize(this.getProperty("maverick.log.file.maxSize", "20MB")));
        }
        this.log(Log.Level.INFO, "Reloaded logging configuration {} [{}]", null, this.propertiesFile.getName(), this.propertiesFile.getAbsolutePath());
    }

    @Override
    public synchronized void enableConsole(Log.Level level) {
        boolean enable = true;
        for (LoggerContext ctx : this.contexts) {
            if (!(ctx instanceof ConsoleLoggingContext)) continue;
            enable = false;
        }
        if (enable) {
            this.contexts.add(new ConsoleLoggingContext(level));
            this.log(Log.Level.INFO, "Console logging enabled", null, new Object[0]);
        }
    }

    public void enableFile(Log.Level level, String logFile) {
        this.enableFile(level, new File(logFile));
    }

    public synchronized void enableFile(Log.Level level, File logFile) {
        try {
            this.contexts.add(new FileLoggingContext(level, logFile));
        }
        catch (IOException e) {
            System.err.println("Error logging to file");
            e.printStackTrace();
        }
    }

    public synchronized void enableFile(Log.Level level, File logFile, int maxFiles, long maxSize) {
        try {
            this.contexts.add(new FileLoggingContext(level, logFile, maxFiles, maxSize));
        }
        catch (IOException e) {
            System.err.println("Error logging to file");
            e.printStackTrace();
        }
    }

    @Override
    public synchronized boolean isLogging(Log.Level level) {
        for (LoggerContext context : this.contexts) {
            if (!context.isLogging(level)) continue;
            return true;
        }
        return false;
    }

    public static String prepareLog(Log.Level level, String msg, Throwable e, Object ... args) {
        int idx = 0;
        int idx2 = 0;
        StringBuffer buffer = new StringBuffer();
        buffer.append(String.format("%s [%20s] %6s - ", df.format(new Date()), Thread.currentThread().getName(), level.name()));
        if (args.length > 0 && msg.indexOf("{}") > -1) {
            for (int i = 0; i < args.length && (idx2 = msg.indexOf("{}", idx)) > -1; ++i) {
                buffer.append(msg.substring(idx, idx2));
                buffer.append(args[i]);
                idx = idx2 + 2;
            }
            if (msg.length() > idx + 2) {
                buffer.append(msg.substring(idx2 + 2));
            }
        } else {
            buffer.append(msg);
        }
        buffer.append(System.lineSeparator());
        if (!IOUtils.isNull(e)) {
            StringWriter s = new StringWriter();
            PrintWriter w = new PrintWriter(s);
            e.printStackTrace(w);
            buffer.append(s.toString());
            buffer.append(System.lineSeparator());
        }
        return buffer.toString();
    }

    @Override
    public synchronized void log(Log.Level level, String msg, Throwable e, Object ... args) {
        for (LoggerContext context : this.contexts) {
            context.log(level, msg, e, args);
        }
    }

    @Override
    public synchronized void raw(Log.Level level, String msg) {
        for (LoggerContext context : this.contexts) {
            context.raw(level, msg);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public synchronized void newline() {
        for (LoggerContext context : this.contexts) {
            context.newline();
        }
    }

    public class FileWatcher
    extends Thread {
        private final File file;
        private AtomicBoolean stop = new AtomicBoolean(false);

        public FileWatcher(File file) {
            this.file = file;
            this.setDaemon(true);
        }

        public boolean isStopped() {
            return this.stop.get();
        }

        public void stopThread() {
            this.stop.set(true);
        }

        public void doOnChange() {
            DefaultLoggerContext.this.loadFile();
        }

        @Override
        public void run() {
            try (WatchService watcher = FileSystems.getDefault().newWatchService();){
                Path path = this.file.getAbsoluteFile().toPath().getParent();
                path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
                while (!this.isStopped()) {
                    WatchKey key;
                    try {
                        key = watcher.poll(25L, TimeUnit.MILLISECONDS);
                    }
                    catch (InterruptedException e) {
                        if (watcher != null) {
                            if (var2_3 != null) {
                                try {
                                    watcher.close();
                                }
                                catch (Throwable throwable) {
                                    var2_3.addSuppressed(throwable);
                                }
                            } else {
                                watcher.close();
                            }
                        }
                        return;
                    }
                    if (key == null) {
                        Thread.yield();
                        continue;
                    }
                    for (WatchEvent<?> event : key.pollEvents()) {
                        boolean valid;
                        WatchEvent.Kind<?> kind = event.kind();
                        WatchEvent<?> ev = event;
                        Path filename = (Path)ev.context();
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            Thread.yield();
                            continue;
                        }
                        if (kind == StandardWatchEventKinds.ENTRY_MODIFY && filename.toString().equals(this.file.getName())) {
                            this.doOnChange();
                        }
                        if (valid = key.reset()) continue;
                        break;
                    }
                    Thread.yield();
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }
}

