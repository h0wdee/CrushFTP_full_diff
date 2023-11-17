/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.net;

import com.maverick.util.Base64;
import com.sshtools.net.HttpHeader;

public class HttpRequest
extends HttpHeader {
    public void setHeaderBegin(String begin) {
        this.begin = begin;
    }

    public void setBasicAuthentication(String username, String password) {
        String str = username + ":" + password;
        this.setHeaderField("Proxy-Authorization", "Basic " + Base64.encodeBytes(str.getBytes(), true));
    }
}

