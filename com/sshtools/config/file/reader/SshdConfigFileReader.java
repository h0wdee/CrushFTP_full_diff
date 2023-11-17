/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.config.file.reader;

import com.maverick.ssh.components.Utils;
import com.sshtools.config.file.SshdConfigFile;
import com.sshtools.config.file.entry.Entry;
import com.sshtools.config.file.entry.MatchEntry;
import com.sshtools.config.file.entry.type.FileEntryType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class SshdConfigFileReader {
    private InputStream stream;

    public SshdConfigFileReader(InputStream stream) {
        this.stream = stream;
    }

    public SshdConfigFile read() throws IOException {
        return this.readToBuilder().build();
    }

    public SshdConfigFile.SshdConfigFileBuilder readToBuilder() throws IOException {
        final SshdConfigFile.SshdConfigFileBuilder sshdConfigFileBuilder = SshdConfigFile.builder();
        return sshdConfigFileBuilder.executeWrite(new Callable<SshdConfigFile.SshdConfigFileBuilder>(){

            @Override
            public SshdConfigFile.SshdConfigFileBuilder call() throws Exception {
                Entry.AbstractEntryBuilder currentBuilder = sshdConfigFileBuilder;
                if (SshdConfigFileReader.this.stream == null) {
                    throw new IllegalStateException("Stream not initiallized.");
                }
                try (BufferedReader br = new BufferedReader(new InputStreamReader(SshdConfigFileReader.this.stream));){
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        if ((line = line.trim()).equals("")) {
                            currentBuilder.cursor().get().appendEntry(new FileEntryType.BlankEntry());
                            continue;
                        }
                        if (line.startsWith("#")) {
                            currentBuilder.cursor().get().appendEntry(new FileEntryType.CommentEntry(line.substring(1)));
                            continue;
                        }
                        String[] result = line.split("\\s");
                        if (result.length == 0 || result.length == 1) {
                            SshdConfigFileReader.this.onInvalidEntry(line);
                            continue;
                        }
                        String key = result[0];
                        String value = Utils.csv(" ", Arrays.copyOfRange(result, 1, result.length));
                        if (key.equalsIgnoreCase("match")) {
                            if (currentBuilder.cursor().get() instanceof MatchEntry) {
                                currentBuilder.end();
                            }
                            String[] matchValueSplit = value.split("\\s");
                            currentBuilder = sshdConfigFileBuilder.matchEntry().parse(matchValueSplit);
                            continue;
                        }
                        currentBuilder.cursor().get().appendEntry(new FileEntryType.SshdConfigKeyValueEntry(key, value));
                    }
                }
                return sshdConfigFileBuilder;
            }
        });
    }

    void onInvalidEntry(String entry) {
    }
}

