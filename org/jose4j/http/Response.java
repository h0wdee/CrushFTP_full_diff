/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.http;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jose4j.http.SimpleResponse;

public class Response
implements SimpleResponse {
    private int statusCode;
    private String statusMessage;
    private Map<String, List<String>> headers;
    private String body;

    public Response(int statusCode, String statusMessage, Map<String, List<String>> headers, String body) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.headers = new HashMap<String, List<String>>();
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            String name = this.normalizeHeaderName(header.getKey());
            this.headers.put(name, header.getValue());
        }
        this.body = body;
    }

    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public String getStatusMessage() {
        return this.statusMessage;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return this.headers.keySet();
    }

    @Override
    public List<String> getHeaderValues(String name) {
        name = this.normalizeHeaderName(name);
        return this.headers.get(name);
    }

    @Override
    public String getBody() {
        return this.body;
    }

    private String normalizeHeaderName(String name) {
        return name != null ? name.toLowerCase().trim() : null;
    }

    public String toString() {
        return "SimpleResponse{statusCode=" + this.statusCode + ", statusMessage='" + this.statusMessage + '\'' + ", headers=" + this.headers + ", body='" + this.body + '\'' + '}';
    }
}

