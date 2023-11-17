/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.net;

import com.sshtools.net.HttpHeader;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class HttpResponse
extends HttpHeader {
    private String version;
    private int status;
    private String reason;

    public HttpResponse(InputStream input) throws IOException {
        this.begin = this.readLine(input);
        while (this.begin.trim().length() == 0) {
            this.begin = this.readLine(input);
        }
        this.processResponse();
        this.processHeaderFields(input);
    }

    public String getVersion() {
        return this.version;
    }

    public int getStatus() {
        return this.status;
    }

    public String getReason() {
        return this.reason;
    }

    private void processResponse() throws IOException {
        StringTokenizer tokens = new StringTokenizer(this.begin, " \t\r", false);
        try {
            this.version = tokens.nextToken();
            this.status = Integer.parseInt(tokens.nextToken());
            this.reason = tokens.nextToken();
        }
        catch (NoSuchElementException e) {
            throw new IOException("Failed to read HTTP repsonse header");
        }
        catch (NumberFormatException e) {
            throw new IOException("Failed to read HTTP resposne header");
        }
    }

    public String getAuthenticationMethod() {
        String auth = this.getHeaderField("Proxy-Authenticate");
        String method = null;
        if (auth != null) {
            int n = auth.indexOf(32);
            method = auth.substring(0, n);
        }
        return method;
    }

    public String getAuthenticationRealm() {
        String auth = this.getHeaderField("Proxy-Authenticate");
        String realm = "";
        if (auth != null) {
            int r = auth.indexOf(61);
            while (r >= 0) {
                int l = auth.lastIndexOf(32, r);
                if (l <= -1) continue;
                String val = auth.substring(l + 1, r);
                if (val.equalsIgnoreCase("realm")) {
                    l = r + 2;
                    r = auth.indexOf(34, l);
                    realm = auth.substring(l, r);
                    break;
                }
                r = auth.indexOf(61, r + 1);
            }
        }
        return realm;
    }
}

