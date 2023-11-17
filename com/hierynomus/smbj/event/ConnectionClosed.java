/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.event;

import com.hierynomus.smbj.event.SMBEvent;

public class ConnectionClosed
implements SMBEvent {
    private String hostname;
    private int port;

    public ConnectionClosed(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return this.hostname;
    }

    public int getPort() {
        return this.port;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ConnectionClosed that = (ConnectionClosed)o;
        if (this.port != that.port) {
            return false;
        }
        return this.hostname.equals(that.hostname);
    }

    public int hashCode() {
        int result = this.hostname.hashCode();
        result = 31 * result + this.port;
        return result;
    }
}

