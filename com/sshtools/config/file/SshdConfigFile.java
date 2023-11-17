/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.config.file;

import com.sshtools.config.file.EntryBuilder;
import com.sshtools.config.file.SshdConfigFileCursor;
import com.sshtools.config.file.entry.Entry;
import com.sshtools.config.file.entry.GlobalEntry;
import com.sshtools.config.file.entry.MatchEntry;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SshdConfigFile {
    private static final int TIME_OUT_SECONDS = 20;
    private GlobalEntry globalEntry;
    private List<MatchEntry> matchEntries = new LinkedList<MatchEntry>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = (ReentrantReadWriteLock.ReadLock)this.lock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = (ReentrantReadWriteLock.WriteLock)this.lock.writeLock();

    private SshdConfigFile() {
        this.globalEntry = new GlobalEntry(this);
    }

    public MatchEntry findMatchEntry(final Map<String, String> params) {
        return this.executeRead(new Callable<MatchEntry>(){

            @Override
            public MatchEntry call() throws Exception {
                block0: for (MatchEntry matchEntry : SshdConfigFile.this.matchEntries) {
                    for (String paramKey : params.keySet()) {
                        if (matchEntry.hasKey(paramKey) && matchEntry.matchValueExact(paramKey, params)) continue;
                        continue block0;
                    }
                    return matchEntry;
                }
                return null;
            }
        });
    }

    public MatchEntry findMatchEntryWithMatch(final Map<String, Collection<String>> params) {
        return this.executeRead(new Callable<MatchEntry>(){

            @Override
            public MatchEntry call() throws Exception {
                block0: for (MatchEntry matchEntry : SshdConfigFile.this.matchEntries) {
                    for (String paramKey : params.keySet()) {
                        if (matchEntry.hasKey(paramKey) && matchEntry.matchValueAgainstPattern(paramKey, (Collection)params.get(paramKey))) continue;
                        continue block0;
                    }
                    return matchEntry;
                }
                return null;
            }
        });
    }

    public GlobalEntry getGlobalEntry() {
        return this.globalEntry;
    }

    public Iterator<MatchEntry> getMatchEntriesIterator() {
        return this.matchEntries.iterator();
    }

    public MatchEntry addMatchEntry() {
        return this.executeWrite(new Callable<MatchEntry>(){

            @Override
            public MatchEntry call() throws Exception {
                MatchEntry matchEntry = new MatchEntry(SshdConfigFile.this);
                SshdConfigFile.this.matchEntries.add(matchEntry);
                return matchEntry;
            }
        });
    }

    public <T> T executeRead(Callable<T> callable) {
        try {
            this.readLock.tryLock(20L, TimeUnit.SECONDS);
            T t = callable.call();
            return t;
        }
        catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        finally {
            this.readLock.unlock();
        }
    }

    public <T> T executeWrite(Callable<T> callable) {
        try {
            this.writeLock.tryLock(20L, TimeUnit.SECONDS);
            T t = callable.call();
            return t;
        }
        catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        finally {
            this.writeLock.unlock();
        }
    }

    public static SshdConfigFileBuilder builder() {
        return new SshdConfigFileBuilder();
    }

    public static class SshdConfigFileBuilder
    extends Entry.AbstractEntryBuilder<SshdConfigFileBuilder>
    implements EntryBuilder<SshdConfigFileBuilder, SshdConfigFileBuilder> {
        private GlobalEntry managedInstance;

        public SshdConfigFileBuilder() {
            this.file = new SshdConfigFile();
            this.managedInstance = this.file.globalEntry;
            this.cursor.set(this.managedInstance);
        }

        public MatchEntry.MatchEntryBuilder matchEntry() {
            return new MatchEntry.MatchEntryBuilder(this, this.file, this.cursor);
        }

        public MatchEntry.MatchEntryBuilder findMatchEntry(Map<String, String> params) {
            MatchEntry matchEntry = this.file.findMatchEntry(params);
            if (matchEntry == null) {
                throw new IllegalArgumentException("Match entry not found, is null.");
            }
            return new MatchEntry.MatchEntryBuilder(this, this.file, this.cursor, matchEntry);
        }

        public MatchEntry.MatchEntryBuilder findMatchEntryWithMatch(Map<String, Collection<String>> params) {
            MatchEntry matchEntry = this.file.findMatchEntryWithMatch(params);
            if (matchEntry == null) {
                throw new IllegalArgumentException("Match entry not found, is null.");
            }
            return new MatchEntry.MatchEntryBuilder(this, this.file, this.cursor, matchEntry);
        }

        public SshdConfigFile build() {
            return this.file;
        }

        public <T> T executeRead(Callable<T> callable) {
            return this.file.executeRead(callable);
        }

        public <T> T executeWrite(Callable<T> callable) {
            return this.file.executeWrite(callable);
        }

        @Override
        public SshdConfigFileCursor cursor() {
            return this.cursor;
        }

        @Override
        public SshdConfigFileBuilder end() {
            return this;
        }

        @Override
        protected Entry getManagedInstance() {
            return this.managedInstance;
        }
    }
}

