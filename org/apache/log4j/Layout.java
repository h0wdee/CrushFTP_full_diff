/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.util.Strings
 */
package org.apache.log4j;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.logging.log4j.util.Strings;

public abstract class Layout {
    public static final String LINE_SEP = Strings.LINE_SEPARATOR;
    public static final int LINE_SEP_LEN = Strings.LINE_SEPARATOR.length();

    public abstract String format(LoggingEvent var1);

    public String getContentType() {
        return "text/plain";
    }

    public String getHeader() {
        return null;
    }

    public String getFooter() {
        return null;
    }

    public abstract boolean ignoresThrowable();
}

