/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.Layout
 *  org.apache.logging.log4j.core.LogEvent
 *  org.apache.logging.log4j.core.layout.ByteBufferDestination
 */
package org.apache.log4j.bridge;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Layout;
import org.apache.log4j.bridge.LogEventAdapter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;

public class LayoutAdapter
implements org.apache.logging.log4j.core.Layout<String> {
    private Layout layout;

    public LayoutAdapter(Layout layout) {
        this.layout = layout;
    }

    public byte[] getFooter() {
        return this.layout.getFooter() == null ? null : this.layout.getFooter().getBytes();
    }

    public byte[] getHeader() {
        return this.layout.getHeader() == null ? null : this.layout.getHeader().getBytes();
    }

    public byte[] toByteArray(LogEvent event) {
        String result = this.layout.format(new LogEventAdapter(event));
        return result == null ? null : result.getBytes();
    }

    public String toSerializable(LogEvent event) {
        return this.layout.format(new LogEventAdapter(event));
    }

    public String getContentType() {
        return this.layout.getContentType();
    }

    public Map<String, String> getContentFormat() {
        return new HashMap<String, String>();
    }

    public void encode(LogEvent event, ByteBufferDestination destination) {
        byte[] data = this.toByteArray(event);
        destination.writeBytes(data, 0, data.length);
    }
}

