/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.config.ConfigurationException
 *  org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder
 *  org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration
 *  org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder
 *  org.apache.logging.log4j.core.tools.BasicCommandLineArguments
 *  org.apache.logging.log4j.core.tools.picocli.CommandLine
 *  org.apache.logging.log4j.core.tools.picocli.CommandLine$Command
 *  org.apache.logging.log4j.core.tools.picocli.CommandLine$Option
 */
package org.apache.log4j.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.log4j.config.InputStreamWrapper;
import org.apache.log4j.config.Log4j1ConfigurationParser;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder;
import org.apache.logging.log4j.core.tools.BasicCommandLineArguments;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;

public final class Log4j1ConfigurationConverter {
    private static final String FILE_EXT_XML = ".xml";
    private final CommandLineArguments cla;

    public static void main(String[] args) {
        CommandLine.run((Runnable)new CommandLineArguments(), (PrintStream)System.err, (String[])args);
    }

    public static Log4j1ConfigurationConverter run(CommandLineArguments cla) {
        Log4j1ConfigurationConverter log4j1ConfigurationConverter = new Log4j1ConfigurationConverter(cla);
        log4j1ConfigurationConverter.run();
        return log4j1ConfigurationConverter;
    }

    private Log4j1ConfigurationConverter(CommandLineArguments cla) {
        this.cla = cla;
    }

    protected void convert(InputStream input, OutputStream output) throws IOException {
        ConfigurationBuilder<BuiltConfiguration> builder = new Log4j1ConfigurationParser().buildConfigurationBuilder(input);
        builder.writeXmlConfiguration(output);
    }

    InputStream getInputStream() throws IOException {
        Path pathIn = this.cla.getPathIn();
        return pathIn == null ? System.in : new InputStreamWrapper(Files.newInputStream(pathIn, new OpenOption[0]), pathIn.toString());
    }

    OutputStream getOutputStream() throws IOException {
        Path pathOut = this.cla.getPathOut();
        return pathOut == null ? System.out : Files.newOutputStream(pathOut, new OpenOption[0]);
    }

    private void run() {
        block32: {
            block31: {
                if (this.cla.getRecurseIntoPath() == null) break block31;
                final AtomicInteger countOKs = new AtomicInteger();
                final AtomicInteger countFails = new AtomicInteger();
                try {
                    Files.walkFileTree(this.cla.getRecurseIntoPath(), (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (Log4j1ConfigurationConverter.this.cla.getPathIn() == null || file.getFileName().equals(Log4j1ConfigurationConverter.this.cla.getPathIn())) {
                                Log4j1ConfigurationConverter.this.verbose("Reading %s", new Object[]{file});
                                String newFile = file.getFileName().toString();
                                int lastIndex = newFile.lastIndexOf(".");
                                newFile = lastIndex < 0 ? newFile + Log4j1ConfigurationConverter.FILE_EXT_XML : newFile.substring(0, lastIndex) + Log4j1ConfigurationConverter.FILE_EXT_XML;
                                Path resolvedPath = file.resolveSibling(newFile);
                                try (InputStreamWrapper input = new InputStreamWrapper(Files.newInputStream(file, new OpenOption[0]), file.toString());
                                     OutputStream output = Files.newOutputStream(resolvedPath, new OpenOption[0]);){
                                    try {
                                        ByteArrayOutputStream tmpOutput = new ByteArrayOutputStream();
                                        Log4j1ConfigurationConverter.this.convert(input, tmpOutput);
                                        tmpOutput.close();
                                        DefaultConfigurationBuilder.formatXml((Source)new StreamSource(new ByteArrayInputStream(tmpOutput.toByteArray())), (Result)new StreamResult(output));
                                        countOKs.incrementAndGet();
                                    }
                                    catch (IOException | ConfigurationException e) {
                                        countFails.incrementAndGet();
                                        if (Log4j1ConfigurationConverter.this.cla.isFailFast()) {
                                            throw e;
                                        }
                                        e.printStackTrace();
                                    }
                                    catch (TransformerException e) {
                                        countFails.incrementAndGet();
                                        if (Log4j1ConfigurationConverter.this.cla.isFailFast()) {
                                            throw new IOException(e);
                                        }
                                        e.printStackTrace();
                                    }
                                    Log4j1ConfigurationConverter.this.verbose("Wrote %s", new Object[]{resolvedPath});
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
                catch (IOException e) {
                    try {
                        throw new ConfigurationException((Throwable)e);
                    }
                    catch (Throwable throwable) {
                        this.verbose("OK = %,d, Failures = %,d, Total = %,d", countOKs.get(), countFails.get(), countOKs.get() + countFails.get());
                        throw throwable;
                    }
                }
                this.verbose("OK = %,d, Failures = %,d, Total = %,d", countOKs.get(), countFails.get(), countOKs.get() + countFails.get());
                break block32;
            }
            this.verbose("Reading %s", this.cla.getPathIn());
            try (InputStream input = this.getInputStream();
                 OutputStream output = this.getOutputStream();){
                this.convert(input, output);
            }
            catch (IOException e) {
                throw new ConfigurationException((Throwable)e);
            }
            this.verbose("Wrote %s", this.cla.getPathOut());
        }
    }

    private void verbose(String template, Object ... args) {
        if (this.cla.isVerbose()) {
            System.err.println(String.format(template, args));
        }
    }

    @CommandLine.Command(name="Log4j1ConfigurationConverter")
    public static class CommandLineArguments
    extends BasicCommandLineArguments
    implements Runnable {
        @CommandLine.Option(names={"--failfast", "-f"}, description={"Fails on the first failure in recurse mode."})
        private boolean failFast;
        @CommandLine.Option(names={"--in", "-i"}, description={"Specifies the input file."})
        private Path pathIn;
        @CommandLine.Option(names={"--out", "-o"}, description={"Specifies the output file."})
        private Path pathOut;
        @CommandLine.Option(names={"--recurse", "-r"}, description={"Recurses into this folder looking for the input file"})
        private Path recurseIntoPath;
        @CommandLine.Option(names={"--verbose", "-v"}, description={"Be verbose."})
        private boolean verbose;

        public Path getPathIn() {
            return this.pathIn;
        }

        public Path getPathOut() {
            return this.pathOut;
        }

        public Path getRecurseIntoPath() {
            return this.recurseIntoPath;
        }

        public boolean isFailFast() {
            return this.failFast;
        }

        public boolean isVerbose() {
            return this.verbose;
        }

        public void setFailFast(boolean failFast) {
            this.failFast = failFast;
        }

        public void setPathIn(Path pathIn) {
            this.pathIn = pathIn;
        }

        public void setPathOut(Path pathOut) {
            this.pathOut = pathOut;
        }

        public void setRecurseIntoPath(Path recurseIntoPath) {
            this.recurseIntoPath = recurseIntoPath;
        }

        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }

        @Override
        public void run() {
            if (this.isHelp()) {
                CommandLine.usage((Object)this, (PrintStream)System.err);
                return;
            }
            new Log4j1ConfigurationConverter(this).run();
        }

        public String toString() {
            return "CommandLineArguments [recurseIntoPath=" + this.recurseIntoPath + ", verbose=" + this.verbose + ", pathIn=" + this.pathIn + ", pathOut=" + this.pathOut + "]";
        }
    }
}

