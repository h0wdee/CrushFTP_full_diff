/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.config.file;

import com.sshtools.config.file.entry.Entry;
import java.util.Stack;

public class SshdConfigFileCursor {
    private Stack<Entry> currentEntryStack = new Stack();

    public void set(Entry currentEntry) {
        this.currentEntryStack.push(currentEntry);
    }

    public Entry get() {
        return this.currentEntryStack.peek();
    }

    public Entry remove() {
        if (!this.currentEntryStack.isEmpty()) {
            return this.currentEntryStack.pop();
        }
        return null;
    }
}

