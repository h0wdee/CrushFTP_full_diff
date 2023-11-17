/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.config.file.entry;

import com.maverick.ssh.components.Utils;
import com.sshtools.config.file.EntryBuilder;
import com.sshtools.config.file.SshdConfigFile;
import com.sshtools.config.file.SshdConfigFileCursor;
import com.sshtools.config.file.entry.Entry;
import com.sshtools.config.file.entry.GlobalEntry;
import com.sshtools.config.file.entry.type.FileEntryType;
import com.sshtools.publickey.authorized.Patterns;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class MatchEntry
extends GlobalEntry {
    private static final String MATCH_STRING_TEMPLATE = "%s %s";
    public static final String MATCH_ENTRY_CRITERIA_USER = "User";
    public static final String MATCH_ENTRY_CRITERIA_GROUP = "Group";
    public static final String MATCH_ENTRY_CRITERIA_HOST = "Host";
    public static final String MATCH_ENTRY_CRITERIA_LOCAL_ADDRESS = "LocalAddress";
    public static final String MATCH_ENTRY_CRITERIA_LOCAL_PORT = "LocalPort";
    public static final String MATCH_ENTRY_CRITERIA_ADDRESS = "Address";
    public static final String MATCH_ENTRY_CRITERIA_RDOMAIN = "RDomain";
    private Map<String, Set<String>> matchCriteriaMap = new LinkedHashMap<String, Set<String>>();
    private List<FileEntryType.CommentEntry> matchCriteriaCommentEntries = new ArrayList<FileEntryType.CommentEntry>();

    public MatchEntry(SshdConfigFile sshdConfigFile) {
        super(sshdConfigFile);
    }

    public void addMatchCriteriaComment(final FileEntryType.CommentEntry commentEntry) {
        this.executeWrite(new Callable<Void>(){

            @Override
            public Void call() throws Exception {
                MatchEntry.this.matchCriteriaCommentEntries.add(commentEntry);
                return null;
            }
        });
    }

    public String matchEntryCriteriaAsString() {
        return this.executeRead(new Callable<String>(){

            @Override
            public String call() throws Exception {
                StringBuilder string = new StringBuilder(256);
                Set keySet = MatchEntry.this.matchCriteriaMap.keySet();
                Iterator keySetIterator = keySet.iterator();
                String first = (String)keySetIterator.next();
                string.append(String.format(MatchEntry.MATCH_STRING_TEMPLATE, first, Utils.csv((Collection)MatchEntry.this.matchCriteriaMap.get(first))));
                if (!keySetIterator.hasNext()) {
                    return string.toString();
                }
                while (keySetIterator.hasNext()) {
                    string.append(" ");
                    String key = (String)keySetIterator.next();
                    string.append(String.format(MatchEntry.MATCH_STRING_TEMPLATE, key, Utils.csv((Collection)MatchEntry.this.matchCriteriaMap.get(key))));
                }
                return string.toString();
            }
        });
    }

    private void addCriteria(final String criteria, final String[] values) {
        this.executeWrite(new Callable<Void>(){

            @Override
            public Void call() throws Exception {
                LinkedHashSet<String> valuesSet = new LinkedHashSet<String>(Arrays.asList(values));
                MatchEntry.this.matchCriteriaMap.put(criteria, valuesSet);
                return null;
            }
        });
    }

    public void addCriteria(String criteria, String value) {
        this.addCriteria(criteria, value.split(","));
    }

    public boolean matchValueAgainstPattern(final String paramKey, final Collection<String> patterns) {
        return this.executeRead(new Callable<Boolean>(){

            @Override
            public Boolean call() throws Exception {
                Set values = (Set)MatchEntry.this.matchCriteriaMap.get(paramKey);
                for (String value : values) {
                    if (!Patterns.matchesWithCIDR(patterns, value)) continue;
                    return true;
                }
                return false;
            }
        });
    }

    public boolean matchValueExact(final String paramKey, final Map<String, String> params) {
        return this.executeRead(new Callable<Boolean>(){

            @Override
            public Boolean call() throws Exception {
                Set values = (Set)MatchEntry.this.matchCriteriaMap.get(paramKey);
                return values.contains(params.get(paramKey));
            }
        });
    }

    public Iterator<FileEntryType.CommentEntry> getMatchCriteriaCommentEntriesIterator() {
        return this.matchCriteriaCommentEntries.iterator();
    }

    private void pushCriteria(final String criteria, final String value) {
        this.executeWrite(new Callable<Void>(){

            @Override
            public Void call() throws Exception {
                if (!MatchEntry.this.matchCriteriaMap.containsKey(criteria)) {
                    MatchEntry.this.addCriteria(criteria, value);
                    return null;
                }
                ((Set)MatchEntry.this.matchCriteriaMap.get(criteria)).add(value);
                return null;
            }
        });
    }

    private void updateCriteria(final String criteria, final String value) {
        this.executeWrite(new Callable<Void>(){

            @Override
            public Void call() throws Exception {
                if (!MatchEntry.this.matchCriteriaMap.containsKey(criteria)) {
                    MatchEntry.this.addCriteria(criteria, value);
                    return null;
                }
                ((Set)MatchEntry.this.matchCriteriaMap.get(criteria)).clear();
                ((Set)MatchEntry.this.matchCriteriaMap.get(criteria)).addAll(Arrays.asList(value.split(",")));
                return null;
            }
        });
    }

    private void deleteCriteria(final String criteria) {
        this.executeWrite(new Callable<Void>(){

            @Override
            public Void call() throws Exception {
                ((Set)MatchEntry.this.matchCriteriaMap.get(criteria)).clear();
                MatchEntry.this.matchCriteriaMap.remove(criteria);
                return null;
            }
        });
    }

    public void addUserCriteria(String[] values) {
        this.addCriteria(MATCH_ENTRY_CRITERIA_USER, values);
    }

    public void addUserCriteria(String value) {
        this.addCriteria(MATCH_ENTRY_CRITERIA_USER, value.split(","));
    }

    public void pushUserCriteria(String value) {
        this.pushCriteria(MATCH_ENTRY_CRITERIA_USER, value);
    }

    public void updateUserCriteria(String value) {
        this.updateCriteria(MATCH_ENTRY_CRITERIA_USER, value);
    }

    public void deleteUserCriteria() {
        this.deleteCriteria(MATCH_ENTRY_CRITERIA_USER);
    }

    public void addGroupCriteria(String[] values) {
        this.addCriteria(MATCH_ENTRY_CRITERIA_GROUP, values);
    }

    public void addGroupCriteria(String value) {
        this.addCriteria(MATCH_ENTRY_CRITERIA_GROUP, value.split(","));
    }

    public void pushGroupCriteria(String value) {
        this.pushCriteria(MATCH_ENTRY_CRITERIA_GROUP, value);
    }

    public void updateGroupCriteria(String value) {
        this.updateCriteria(MATCH_ENTRY_CRITERIA_GROUP, value);
    }

    public void deleteGroupCriteria() {
        this.deleteCriteria(MATCH_ENTRY_CRITERIA_GROUP);
    }

    public void addHostCriteria(String[] values) {
        this.addCriteria(MATCH_ENTRY_CRITERIA_HOST, values);
    }

    public void addHostCriteria(String value) {
        this.addCriteria(MATCH_ENTRY_CRITERIA_HOST, value.split(","));
    }

    public void pushHostCriteria(String value) {
        this.pushCriteria(MATCH_ENTRY_CRITERIA_HOST, value);
    }

    public void updateHostCriteria(String value) {
        this.updateCriteria(MATCH_ENTRY_CRITERIA_HOST, value);
    }

    public void deleteHostCriteria() {
        this.deleteCriteria(MATCH_ENTRY_CRITERIA_HOST);
    }

    public void addLocalAddressCriteria(String[] values) {
        this.addCriteria(MATCH_ENTRY_CRITERIA_LOCAL_ADDRESS, values);
    }

    public void addLocalAddressCriteria(String value) {
        this.addCriteria(MATCH_ENTRY_CRITERIA_LOCAL_ADDRESS, value.split(","));
    }

    public void pushLocalAddressCriteria(String value) {
        this.pushCriteria(MATCH_ENTRY_CRITERIA_LOCAL_ADDRESS, value);
    }

    public void updateLocalAddressCriteria(String value) {
        this.updateCriteria(MATCH_ENTRY_CRITERIA_LOCAL_ADDRESS, value);
    }

    public void deleteLocalAddressCriteria() {
        this.deleteCriteria(MATCH_ENTRY_CRITERIA_LOCAL_ADDRESS);
    }

    public void addLocalPortCriteria(String[] values) {
        this.addCriteria(MATCH_ENTRY_CRITERIA_LOCAL_PORT, values);
    }

    public void addLocalPortCriteria(String value) {
        this.addCriteria(MATCH_ENTRY_CRITERIA_LOCAL_PORT, value.split(","));
    }

    public void pushLocalPortCriteria(String value) {
        this.pushCriteria(MATCH_ENTRY_CRITERIA_LOCAL_PORT, value);
    }

    public void updateLocalPortCriteria(String value) {
        this.updateCriteria(MATCH_ENTRY_CRITERIA_LOCAL_PORT, value);
    }

    public void deleteLocalPortCriteria() {
        this.deleteCriteria(MATCH_ENTRY_CRITERIA_LOCAL_PORT);
    }

    public void addAddressCriteria(String[] values) {
        this.addCriteria(MATCH_ENTRY_CRITERIA_ADDRESS, values);
    }

    public void addAddressCriteria(String value) {
        this.addCriteria(MATCH_ENTRY_CRITERIA_ADDRESS, value.split(","));
    }

    public void pushAddressCriteria(String value) {
        this.pushCriteria(MATCH_ENTRY_CRITERIA_ADDRESS, value);
    }

    public void updateAddressCriteria(String value) {
        this.updateCriteria(MATCH_ENTRY_CRITERIA_ADDRESS, value);
    }

    public void deleteAddressCriteria() {
        this.deleteCriteria(MATCH_ENTRY_CRITERIA_ADDRESS);
    }

    public void addRDomainCriteria(String[] values) {
        this.addCriteria(MATCH_ENTRY_CRITERIA_RDOMAIN, values);
    }

    public void addRDomainCriteria(String value) {
        this.addCriteria(MATCH_ENTRY_CRITERIA_RDOMAIN, value.split(","));
    }

    public void pushRDomainCriteria(String value) {
        this.pushCriteria(MATCH_ENTRY_CRITERIA_RDOMAIN, value);
    }

    public void updateRDomainCriteria(String value) {
        this.updateCriteria(MATCH_ENTRY_CRITERIA_RDOMAIN, value);
    }

    public void deleteRDomainCriteria() {
        this.deleteCriteria(MATCH_ENTRY_CRITERIA_RDOMAIN);
    }

    public void parse(final String[] matchValueSplit) {
        this.executeWrite(new Callable<Void>(){

            @Override
            public Void call() throws Exception {
                for (int i = 0; i < matchValueSplit.length - 1; i += 2) {
                    String mkey = matchValueSplit[i];
                    String mvalue = matchValueSplit[i + 1];
                    if (MatchEntry.isNotAllowedKey(mkey)) {
                        throw new IllegalStateException(String.format("Key %s not recognized for Match entry", mkey));
                    }
                    MatchEntry.this.matchCriteriaMap.put(mkey, new LinkedHashSet<String>(Arrays.asList(mvalue.split(","))));
                }
                return null;
            }
        });
    }

    public boolean hasKey(final String key) {
        return this.executeRead(new Callable<Boolean>(){

            @Override
            public Boolean call() throws Exception {
                return MatchEntry.this.matchCriteriaMap.containsKey(key);
            }
        });
    }

    public boolean hasUserEntry() {
        return this.executeRead(new Callable<Boolean>(){

            @Override
            public Boolean call() throws Exception {
                return MatchEntry.this.matchCriteriaMap.containsKey(MatchEntry.MATCH_ENTRY_CRITERIA_USER);
            }
        });
    }

    public boolean hasGroupEntry() {
        return this.executeRead(new Callable<Boolean>(){

            @Override
            public Boolean call() throws Exception {
                return MatchEntry.this.matchCriteriaMap.containsKey(MatchEntry.MATCH_ENTRY_CRITERIA_GROUP);
            }
        });
    }

    public boolean hasHostEntry() {
        return this.executeRead(new Callable<Boolean>(){

            @Override
            public Boolean call() throws Exception {
                return MatchEntry.this.matchCriteriaMap.containsKey(MatchEntry.MATCH_ENTRY_CRITERIA_HOST);
            }
        });
    }

    public boolean hasLocalAddressEntry() {
        return this.executeRead(new Callable<Boolean>(){

            @Override
            public Boolean call() throws Exception {
                return MatchEntry.this.matchCriteriaMap.containsKey(MatchEntry.MATCH_ENTRY_CRITERIA_LOCAL_ADDRESS);
            }
        });
    }

    public boolean hasLocalPortEntry() {
        return this.executeRead(new Callable<Boolean>(){

            @Override
            public Boolean call() throws Exception {
                return MatchEntry.this.matchCriteriaMap.containsKey(MatchEntry.MATCH_ENTRY_CRITERIA_LOCAL_PORT);
            }
        });
    }

    public boolean hasAddressEntry() {
        return this.executeRead(new Callable<Boolean>(){

            @Override
            public Boolean call() throws Exception {
                return MatchEntry.this.matchCriteriaMap.containsKey(MatchEntry.MATCH_ENTRY_CRITERIA_ADDRESS);
            }
        });
    }

    public boolean hasRDomainEntry() {
        return this.executeRead(new Callable<Boolean>(){

            @Override
            public Boolean call() throws Exception {
                return MatchEntry.this.matchCriteriaMap.containsKey(MatchEntry.MATCH_ENTRY_CRITERIA_RDOMAIN);
            }
        });
    }

    public static boolean isAllowedKey(String key) {
        return MATCH_ENTRY_CRITERIA_USER.equals(key) || MATCH_ENTRY_CRITERIA_GROUP.equals(key) || MATCH_ENTRY_CRITERIA_HOST.equals(key) || MATCH_ENTRY_CRITERIA_LOCAL_ADDRESS.equals(key) || MATCH_ENTRY_CRITERIA_LOCAL_PORT.equals(key) || MATCH_ENTRY_CRITERIA_ADDRESS.equals(key) || MATCH_ENTRY_CRITERIA_RDOMAIN.equals(key);
    }

    public static boolean isNotAllowedKey(String key) {
        return !MatchEntry.isAllowedKey(key);
    }

    public String toString() {
        return "MatchEntry [matchCriteriaMap=" + this.matchCriteriaMap + ", keyEntries=" + this.keyEntries + "]";
    }

    public static class MatchEntryBuilder
    extends Entry.AbstractEntryBuilder<MatchEntryBuilder>
    implements EntryBuilder<MatchEntryBuilder, SshdConfigFile.SshdConfigFileBuilder> {
        private MatchEntry managedInstance;
        private SshdConfigFile.SshdConfigFileBuilder parentBuilder;

        public MatchEntryBuilder(SshdConfigFile.SshdConfigFileBuilder parentBuilder, SshdConfigFile file, SshdConfigFileCursor cursor) {
            this.parentBuilder = parentBuilder;
            this.file = file;
            this.managedInstance = this.file.addMatchEntry();
            this.cursor = cursor;
            this.cursor.set(this.managedInstance);
        }

        public MatchEntryBuilder(SshdConfigFile.SshdConfigFileBuilder parentBuilder, SshdConfigFile file, SshdConfigFileCursor cursor, MatchEntry managedInstance) {
            this.managedInstance = managedInstance;
            this.parentBuilder = parentBuilder;
            this.file = file;
            this.cursor = cursor;
            this.cursor.set(this.managedInstance);
        }

        public MatchEntryBuilder addMatchCriteriaComment(FileEntryType.CommentEntry commentEntry) {
            this.managedInstance.addMatchCriteriaComment(commentEntry);
            return this;
        }

        public MatchEntryBuilder addUserCriteria(String[] values) {
            this.managedInstance.addUserCriteria(values);
            return this;
        }

        public MatchEntryBuilder addUserCriteria(String value) {
            return this.addUserCriteria(value.split(","));
        }

        public MatchEntryBuilder pushUserCriteria(String value) {
            this.managedInstance.pushUserCriteria(value);
            return this;
        }

        public MatchEntryBuilder updateUserCriteria(String value) {
            this.managedInstance.updateUserCriteria(value);
            return this;
        }

        public MatchEntryBuilder deleteUserCriteria() {
            this.managedInstance.deleteUserCriteria();
            return this;
        }

        public MatchEntryBuilder addGroupCriteria(String[] values) {
            this.managedInstance.addGroupCriteria(values);
            return this;
        }

        public MatchEntryBuilder addGroupCriteria(String value) {
            return this.addGroupCriteria(value.split(","));
        }

        public MatchEntryBuilder pushGroupCriteria(String value) {
            this.managedInstance.pushGroupCriteria(value);
            return this;
        }

        public MatchEntryBuilder updateGroupCriteria(String value) {
            this.managedInstance.updateGroupCriteria(value);
            return this;
        }

        public MatchEntryBuilder deleteGroupCriteria() {
            this.managedInstance.deleteGroupCriteria();
            return this;
        }

        public MatchEntryBuilder addHostCriteria(String[] values) {
            this.managedInstance.addHostCriteria(values);
            return this;
        }

        public MatchEntryBuilder addHostCriteria(String value) {
            return this.addHostCriteria(value.split(","));
        }

        public MatchEntryBuilder pushHostCriteria(String value) {
            this.managedInstance.pushHostCriteria(value);
            return this;
        }

        public MatchEntryBuilder updateHostCriteria(String value) {
            this.managedInstance.updateHostCriteria(value);
            return this;
        }

        public MatchEntryBuilder deleteHostCriteria() {
            this.managedInstance.deleteHostCriteria();
            return this;
        }

        public MatchEntryBuilder addLocalPortCriteria(String[] values) {
            this.managedInstance.addLocalPortCriteria(values);
            return this;
        }

        public MatchEntryBuilder addLocalPortCriteria(String value) {
            return this.addLocalPortCriteria(value.split(","));
        }

        public MatchEntryBuilder pushLocalPortCriteria(String value) {
            this.managedInstance.pushLocalPortCriteria(value);
            return this;
        }

        public MatchEntryBuilder updateLocalPortCriteria(String value) {
            this.managedInstance.updateLocalPortCriteria(value);
            return this;
        }

        public MatchEntryBuilder deleteLocalPortCriteria() {
            this.managedInstance.deleteLocalPortCriteria();
            return this;
        }

        public MatchEntryBuilder addLocalAddressCriteria(String[] values) {
            this.managedInstance.addLocalAddressCriteria(values);
            return this;
        }

        public MatchEntryBuilder addLocalAddressCriteria(String value) {
            return this.addLocalAddressCriteria(value.split(","));
        }

        public MatchEntryBuilder pushLocalAddressCriteria(String value) {
            this.managedInstance.pushLocalAddressCriteria(value);
            return this;
        }

        public MatchEntryBuilder updateLocalAddressCriteria(String value) {
            this.managedInstance.updateLocalAddressCriteria(value);
            return this;
        }

        public MatchEntryBuilder deleteLocalAddressCriteria() {
            this.managedInstance.deleteLocalAddressCriteria();
            return this;
        }

        public MatchEntryBuilder addAddressCriteria(String[] values) {
            this.managedInstance.addAddressCriteria(values);
            return this;
        }

        public MatchEntryBuilder addAddressCriteria(String value) {
            return this.addAddressCriteria(value.split(","));
        }

        public MatchEntryBuilder pushAddressCriteria(String value) {
            this.managedInstance.pushAddressCriteria(value);
            return this;
        }

        public MatchEntryBuilder updateAddressCriteria(String value) {
            this.managedInstance.updateAddressCriteria(value);
            return this;
        }

        public MatchEntryBuilder deleteAddressCriteria() {
            this.managedInstance.deleteAddressCriteria();
            return this;
        }

        public MatchEntryBuilder addRDomainCriteria(String[] values) {
            this.managedInstance.addRDomainCriteria(values);
            return this;
        }

        public MatchEntryBuilder addRDomainCriteria(String value) {
            return this.addRDomainCriteria(value.split(","));
        }

        public MatchEntryBuilder pushRDomainCriteria(String value) {
            this.managedInstance.pushRDomainCriteria(value);
            return this;
        }

        public MatchEntryBuilder updateRDomainCriteria(String value) {
            this.managedInstance.updateRDomainCriteria(value);
            return this;
        }

        public MatchEntryBuilder deleteRDomainCriteria() {
            this.managedInstance.deleteRDomainCriteria();
            return this;
        }

        public MatchEntryBuilder parse(String[] matchValueSplit) {
            this.managedInstance.parse(matchValueSplit);
            return this;
        }

        @Override
        public SshdConfigFile.SshdConfigFileBuilder end() {
            return this.file.executeWrite(new Callable<SshdConfigFile.SshdConfigFileBuilder>(){

                @Override
                public SshdConfigFile.SshdConfigFileBuilder call() throws Exception {
                    MatchEntryBuilder.this.cursor.remove();
                    return MatchEntryBuilder.this.parentBuilder;
                }
            });
        }

        @Override
        protected Entry getManagedInstance() {
            return this.managedInstance;
        }
    }
}

