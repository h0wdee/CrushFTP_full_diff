/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.map.ListOrderedMap
 */
package com.sshtools.config.file.entry;

import com.sshtools.config.file.SshdConfigFile;
import com.sshtools.config.file.SshdConfigFileCursor;
import com.sshtools.config.file.entry.type.FileEntryType;
import com.sshtools.publickey.authorized.Patterns;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.collections.map.ListOrderedMap;

public class Entry {
    AtomicInteger commentKey = new AtomicInteger(1);
    AtomicInteger blankKey = new AtomicInteger(1);
    protected SshdConfigFile sshdConfigFile;
    protected Map<String, FileEntryType.SshdConfigFileEntry> keyEntries = new ListOrderedMap();

    public Entry(SshdConfigFile sshdConfigFile) {
        this.sshdConfigFile = sshdConfigFile;
    }

    protected ListOrderedMap getKeyEntriesOrderedMap() {
        return (ListOrderedMap)this.keyEntries;
    }

    public FileEntryType.SshdConfigFileEntry getEntry(String key) {
        return this.getKeyEntries().get(key);
    }

    public Map<String, FileEntryType.SshdConfigFileEntry> getKeyEntries() {
        return this.executeRead(new Callable<Map<String, FileEntryType.SshdConfigFileEntry>>(){

            @Override
            public Map<String, FileEntryType.SshdConfigFileEntry> call() throws Exception {
                return Entry.this.keyEntries;
            }
        });
    }

    public FileEntryType.SshdConfigFileEntry findEntry(final String key) {
        return this.executeRead(new Callable<FileEntryType.SshdConfigFileEntry>(){

            @Override
            public FileEntryType.SshdConfigFileEntry call() throws Exception {
                return Entry.this.keyEntries.get(key);
            }
        });
    }

    public FileEntryType.SshdConfigKeyValueEntry findKeyValueEntry(final String key) {
        return this.executeRead(new Callable<FileEntryType.SshdConfigKeyValueEntry>(){

            @Override
            public FileEntryType.SshdConfigKeyValueEntry call() throws Exception {
                FileEntryType.SshdConfigFileEntry keyValueEntry = Entry.this.keyEntries.get(key);
                if (!(keyValueEntry instanceof FileEntryType.SshdConfigKeyValueEntry)) {
                    throw new IllegalArgumentException(String.format("Value with key `%s` is not Key Value entry of type SshdConfigKeyValueEntry", key));
                }
                return (FileEntryType.SshdConfigKeyValueEntry)keyValueEntry;
            }
        });
    }

    public int findEntryIndex(final String key) {
        return this.executeRead(new Callable<Integer>(){

            @Override
            public Integer call() throws Exception {
                return Entry.this.getKeyEntriesOrderedMap().indexOf((Object)key);
            }
        });
    }

    public FileEntryType.SshdConfigFileEntry findEntryAtIndex(final int index) {
        return this.executeRead(new Callable<FileEntryType.SshdConfigFileEntry>(){

            @Override
            public FileEntryType.SshdConfigFileEntry call() throws Exception {
                return (FileEntryType.SshdConfigFileEntry)Entry.this.getKeyEntriesOrderedMap().getValue(index);
            }
        });
    }

    public void addCommentForEntry(final String key, final FileEntryType.CommentEntry commentEntry) {
        this.executeWrite(new Callable<Void>(){

            @Override
            public Void call() throws Exception {
                int index = Entry.this.findEntryIndex(key);
                if (index == -1) {
                    throw new IllegalArgumentException(String.format("Entry with key `%s` not found.", key));
                }
                Entry.this.getKeyEntriesOrderedMap().put(index, (Object)Entry.this.getCommentEntryKey(), (Object)commentEntry);
                return null;
            }
        });
    }

    public void addBeginingComment(final FileEntryType.CommentEntry commentEntry) {
        this.executeWrite(new Callable<Void>(){

            @Override
            public Void call() throws Exception {
                Entry.this.getKeyEntriesOrderedMap().put(0, (Object)Entry.this.getCommentEntryKey(), (Object)commentEntry);
                return null;
            }
        });
    }

    public void updateEntry(final String key, final String value) {
        this.executeWrite(new Callable<Void>(){

            @Override
            public Void call() throws Exception {
                FileEntryType.SshdConfigFileEntry entry = (FileEntryType.SshdConfigFileEntry)Entry.this.getKeyEntriesOrderedMap().get((Object)key);
                if (entry == null) {
                    throw new IllegalArgumentException(String.format("Entry with key `%s` not found.", key));
                }
                if (entry instanceof FileEntryType.CommentEntry || entry instanceof FileEntryType.BlankEntry) {
                    throw new IllegalArgumentException("Entry is not a valid entry is Comment or Blank");
                }
                FileEntryType.SshdConfigKeyValueEntry keyValueEntry = (FileEntryType.SshdConfigKeyValueEntry)entry;
                keyValueEntry.setValue(value);
                return null;
            }
        });
    }

    public void deleteEntry(final String key) {
        this.executeWrite(new Callable<Void>(){

            @Override
            public Void call() throws Exception {
                int index = Entry.this.findEntryIndex(key);
                if (index == -1) {
                    throw new IllegalArgumentException(String.format("Entry with key `%s` not found.", key));
                }
                Entry.this.getKeyEntriesOrderedMap().remove(index);
                return null;
            }
        });
    }

    public int addEntry(final int index, final FileEntryType.SshdConfigFileEntry sshdConfigFileEntry) {
        return this.executeWrite(new Callable<Integer>(){

            @Override
            public Integer call() throws Exception {
                String key = Entry.this.resolveKey(sshdConfigFileEntry);
                if (Entry.this.keyEntries.isEmpty()) {
                    Entry.this.getKeyEntriesOrderedMap().put((Object)key, (Object)sshdConfigFileEntry);
                    return 0;
                }
                int indexToStartLookingFrom = index == -1 ? Entry.this.keyEntries.size() - 1 : index;
                int indexToEnter = Entry.this.findLastValidEntry(indexToStartLookingFrom) + 1;
                Entry.this.getKeyEntriesOrderedMap().put(indexToEnter, (Object)key, (Object)sshdConfigFileEntry);
                return indexToEnter;
            }
        });
    }

    public void appendEntry(final FileEntryType.SshdConfigFileEntry sshdConfigFileEntry) {
        this.executeWrite(new Callable<Void>(){

            @Override
            public Void call() throws Exception {
                String key = Entry.this.resolveKey(sshdConfigFileEntry);
                Entry.this.keyEntries.put(key, sshdConfigFileEntry);
                return null;
            }
        });
    }

    public Boolean entryMatches(final String key, final Collection<String> patterns) {
        return this.executeRead(new Callable<Boolean>(){

            @Override
            public Boolean call() throws Exception {
                String[] parts;
                FileEntryType.SshdConfigKeyValueEntry sshdConfigFileEntry = (FileEntryType.SshdConfigKeyValueEntry)Entry.this.getKeyEntries().get(key);
                if (sshdConfigFileEntry != null && (parts = sshdConfigFileEntry.getValueParts()) != null) {
                    for (String part : parts) {
                        if (!Patterns.matchesWithCIDR(patterns, part)) continue;
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private String resolveKey(FileEntryType.SshdConfigFileEntry sshdConfigFileEntry) {
        String key = null;
        if (sshdConfigFileEntry instanceof FileEntryType.SshdConfigKeyValueEntry) {
            key = ((FileEntryType.SshdConfigKeyValueEntry)sshdConfigFileEntry).getKey();
        } else if (sshdConfigFileEntry instanceof FileEntryType.BlankEntry) {
            key = this.getBlankEntryKey();
        } else if (sshdConfigFileEntry instanceof FileEntryType.CommentEntry) {
            key = this.getCommentEntryKey();
        }
        return key;
    }

    private int findLastValidEntry(final int index) {
        return this.executeRead(new Callable<Integer>(){

            @Override
            public Integer call() throws Exception {
                int localIndex = index;
                FileEntryType.SshdConfigFileEntry value = (FileEntryType.SshdConfigFileEntry)Entry.this.getKeyEntriesOrderedMap().getValue(localIndex);
                while (!(localIndex <= 0 || value instanceof FileEntryType.SshdConfigKeyValueEntry || value instanceof FileEntryType.CommentEntry && ((FileEntryType.CommentEntry)value).isNotLoaded())) {
                    value = (FileEntryType.SshdConfigFileEntry)Entry.this.getKeyEntriesOrderedMap().getValue(--localIndex);
                }
                return localIndex;
            }
        });
    }

    public String getBlankEntryKey() {
        return String.format("Blank%d", this.blankKey.getAndIncrement());
    }

    public String getCommentEntryKey() {
        return String.format("Comment%d", this.commentKey.getAndIncrement());
    }

    protected <T> T executeRead(Callable<T> callable) {
        return this.sshdConfigFile.executeRead(callable);
    }

    protected <T> T executeWrite(Callable<T> callable) {
        return this.sshdConfigFile.executeWrite(callable);
    }

    public static abstract class AbstractEntryBuilder<T> {
        protected SshdConfigFileCursor cursor = new SshdConfigFileCursor();
        protected int pointer = -1;
        protected SshdConfigFile file;

        protected abstract Entry getManagedInstance();

        public T updateEntry(String key, String value) {
            this.getManagedInstance().updateEntry(key, value);
            return (T)this;
        }

        public T deleteEntry(String key) {
            this.getManagedInstance().deleteEntry(key);
            return (T)this;
        }

        public T addComment(String comment) {
            this.addEntry(new FileEntryType.CommentEntry(comment));
            return (T)this;
        }

        public T addCommentForEntry(String key, String comment) {
            this.getManagedInstance().addCommentForEntry(key, new FileEntryType.CommentEntry(comment));
            return (T)this;
        }

        public T addCommentForEntry(String key, FileEntryType.CommentEntry commentEntry) {
            this.getManagedInstance().addCommentForEntry(key, commentEntry);
            return (T)this;
        }

        public T addBeginingComment(FileEntryType.CommentEntry commentEntry) {
            this.getManagedInstance().addBeginingComment(commentEntry);
            return (T)this;
        }

        public T addEntry(final FileEntryType.SshdConfigFileEntry sshdConfigFileEntry) {
            return this.file.executeWrite(new Callable<T>(){

                @Override
                public T call() throws Exception {
                    int indexToEnter = AbstractEntryBuilder.this.getManagedInstance().addEntry(AbstractEntryBuilder.this.pointer, sshdConfigFileEntry);
                    if (AbstractEntryBuilder.this.pointer != -1) {
                        AbstractEntryBuilder.this.pointer = indexToEnter;
                    }
                    return AbstractEntryBuilder.this;
                }
            });
        }

        public T appendEntry(FileEntryType.SshdConfigFileEntry sshdConfigFileEntry) {
            this.getManagedInstance().appendEntry(sshdConfigFileEntry);
            return (T)this;
        }

        public FileEntryType.SshdConfigFileEntry findEntry(String key) {
            return this.getManagedInstance().getKeyEntries().get(key);
        }

        public T findEntry(String key, Result<FileEntryType.SshdConfigKeyValueEntry> result) {
            FileEntryType.SshdConfigKeyValueEntry sshdConfigFileEntry = (FileEntryType.SshdConfigKeyValueEntry)this.getManagedInstance().getKeyEntries().get(key);
            result.set(sshdConfigFileEntry);
            return (T)this;
        }

        public T entryMatches(String key, Collection<String> patterns, Result<Boolean> result) {
            result.set(this.entryMatches(key, patterns));
            return (T)this;
        }

        public Boolean entryMatches(String key, Collection<String> patterns) {
            return this.getManagedInstance().entryMatches(key, patterns);
        }

        public int findEntryIndex(String key) {
            return this.getManagedInstance().findEntryIndex(key);
        }

        public FileEntryType.SshdConfigFileEntry findEntryAtIndex(int index) {
            return this.getManagedInstance().findEntryAtIndex(index);
        }

        public T findEntryIndex(String key, Result<Integer> result) {
            int index = this.getManagedInstance().findEntryIndex(key);
            result.set(index);
            return (T)this;
        }

        public T findEntryToEdit(final String key) {
            return this.file.executeWrite(new Callable<T>(){

                @Override
                public T call() throws Exception {
                    int index = AbstractEntryBuilder.this.getManagedInstance().getKeyEntriesOrderedMap().indexOf((Object)key);
                    if (index == -1) {
                        throw new IllegalArgumentException(String.format("Entry with key `%s` not found.", key));
                    }
                    AbstractEntryBuilder.this.pointer = index;
                    return AbstractEntryBuilder.this;
                }
            });
        }

        public T resetPointer() {
            return this.file.executeWrite(new Callable<T>(){

                @Override
                public T call() throws Exception {
                    AbstractEntryBuilder.this.pointer = -1;
                    return AbstractEntryBuilder.this;
                }
            });
        }

        public SshdConfigFileCursor cursor() {
            return this.cursor;
        }

        public static interface Result<T> {
            public T get();

            public void set(T var1);
        }
    }
}

