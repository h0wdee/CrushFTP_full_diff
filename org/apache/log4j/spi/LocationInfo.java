/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j.spi;

import java.io.Serializable;

public class LocationInfo
implements Serializable {
    private final StackTraceElement element;
    public String fullInfo;
    public static final String NA = "?";
    static final long serialVersionUID = -1325822038990805636L;

    public LocationInfo(StackTraceElement element) {
        this.element = element;
    }

    public String getClassName() {
        return this.element.getClassName();
    }

    public String getFileName() {
        return this.element.getFileName();
    }

    public String getLineNumber() {
        return Integer.toString(this.element.getLineNumber());
    }

    public String getMethodName() {
        return this.element.getMethodName();
    }
}

