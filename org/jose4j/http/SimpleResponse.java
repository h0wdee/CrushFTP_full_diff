/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.http;

import java.util.Collection;
import java.util.List;

public interface SimpleResponse {
    public int getStatusCode();

    public String getStatusMessage();

    public Collection<String> getHeaderNames();

    public List<String> getHeaderValues(String var1);

    public String getBody();
}

