/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.config.file.entry.type;

public class FileEntryType {

    public static abstract class NonValidatingFileEntry
    extends SshdConfigFileEntry {
    }

    public static class BlankEntry
    extends NonValidatingFileEntry {
        @Override
        public String getFormattedLine() {
            return "";
        }
    }

    public static class CommentEntry
    extends NonValidatingFileEntry {
        String comment;
        boolean loaded;

        public CommentEntry(String comment) {
            this.comment = comment;
            this.loaded = false;
        }

        public boolean isLoaded() {
            return this.loaded;
        }

        public boolean isNotLoaded() {
            return !this.isLoaded();
        }

        @Override
        public String getFormattedLine() {
            return String.format("#%s", this.comment);
        }
    }

    public static class SshdConfigKeyValueEntry
    extends SshdConfigFileEntry {
        private String key;
        private String value;

        public SshdConfigKeyValueEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getFormattedLine() {
            return String.format("%s %s", this.key, this.value);
        }

        public String toString() {
            return this.getFormattedLine();
        }

        public String getKey() {
            return this.key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean hasParts() {
            return this.value.contains(",") || this.value.contains(" ");
        }

        public boolean hasCommaSV() {
            return this.value.contains(",");
        }

        public boolean hasSpaceSV() {
            return this.value.contains(" ");
        }

        public String[] getValueParts() {
            if (this.value.contains(",")) {
                return this.value.split(",");
            }
            if (this.value.contains(" ")) {
                return this.value.split("\\s");
            }
            return new String[]{this.value};
        }
    }

    public static abstract class SshdConfigFileEntry {
        public abstract String getFormattedLine();
    }
}

