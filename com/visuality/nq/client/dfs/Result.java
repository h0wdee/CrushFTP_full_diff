/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.dfs;

import com.visuality.nq.client.Server;
import com.visuality.nq.client.Share;

public class Result {
    public Server server = null;
    public Share share = null;
    public String path = "";

    public Result(Server server, Share share, String path) {
        this.server = server;
        this.share = share;
        this.path = path;
    }

    public Result() {
        this.server = null;
        this.share = null;
        this.path = "";
    }

    public String toString() {
        return "Result [server=" + this.server + ", share=" + this.share + ", path=" + this.path + "]";
    }
}

