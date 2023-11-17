/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.config.file.writer;

import com.sshtools.config.file.SshdConfigFile;
import com.sshtools.config.file.entry.MatchEntry;
import com.sshtools.config.file.entry.type.FileEntryType;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.concurrent.Callable;

public class SshdConfigFileWriter {
    private OutputStream stream;

    public SshdConfigFileWriter(OutputStream stream) {
        this.stream = stream;
    }

    public void write(final SshdConfigFile sshdConfigFile, final boolean indentMatchEntries) throws IOException {
        if (this.stream == null) {
            throw new IllegalStateException("Stream not initiallized.");
        }
        if (sshdConfigFile == null) {
            throw new IllegalStateException("SshdConfigFile not initiallized.");
        }
        sshdConfigFile.executeRead(new Callable<Void>(){

            @Override
            public Void call() throws Exception {
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(SshdConfigFileWriter.this.stream));){
                    boolean start = false;
                    for (FileEntryType.SshdConfigFileEntry keyEntry : sshdConfigFile.getGlobalEntry().getKeyEntries().values()) {
                        if (start) {
                            bw.newLine();
                        }
                        bw.write(keyEntry.getFormattedLine());
                        start = true;
                    }
                    Iterator<MatchEntry> matchEntriesIterator = sshdConfigFile.getMatchEntriesIterator();
                    while (matchEntriesIterator.hasNext()) {
                        MatchEntry matchEntry = matchEntriesIterator.next();
                        if (matchEntry.matchEntryCriteriaAsString() == null || matchEntry.matchEntryCriteriaAsString().trim().length() == 0) {
                            throw new IllegalStateException("Match entry criteria string cannot be empty.");
                        }
                        Iterator<FileEntryType.CommentEntry> commentEntryIterator = matchEntry.getMatchCriteriaCommentEntriesIterator();
                        while (commentEntryIterator.hasNext()) {
                            FileEntryType.CommentEntry commentEntry = commentEntryIterator.next();
                            if (start) {
                                bw.newLine();
                            }
                            bw.write(commentEntry.getFormattedLine());
                            start = true;
                        }
                        if (start) {
                            bw.newLine();
                        }
                        bw.write(String.format("Match %s", matchEntry.matchEntryCriteriaAsString()));
                        start = true;
                        for (FileEntryType.SshdConfigFileEntry keyEntry : matchEntry.getKeyEntries().values()) {
                            if (start) {
                                bw.newLine();
                            }
                            if (indentMatchEntries && (keyEntry instanceof FileEntryType.SshdConfigKeyValueEntry || keyEntry instanceof FileEntryType.CommentEntry)) {
                                bw.write("\t");
                            }
                            bw.write(keyEntry.getFormattedLine());
                        }
                    }
                    bw.newLine();
                }
                return null;
            }
        });
    }

    public synchronized void write(SshdConfigFile sshdConfigFile) throws IOException {
        this.write(sshdConfigFile, true);
    }
}

