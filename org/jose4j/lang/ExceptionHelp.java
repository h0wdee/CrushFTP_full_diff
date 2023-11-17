/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.lang;

public class ExceptionHelp {
    public static String toStringWithCauses(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(t);
        while (t.getCause() != null) {
            t = t.getCause();
            sb.append("; caused by: ").append(t);
        }
        return sb.toString();
    }

    public static String toStringWithCausesAndAbbreviatedStack(Throwable t, Class stopAt) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        while (t != null) {
            if (!first) {
                sb.append("; caused by: ");
            }
            sb.append(t).append(" at ");
            StackTraceElement[] stackTraceElementArray = t.getStackTrace();
            int n = stackTraceElementArray.length;
            int n2 = 0;
            while (n2 < n) {
                StackTraceElement ste = stackTraceElementArray[n2];
                if (ste.getClassName().equals(stopAt.getName())) {
                    sb.append("...omitted...");
                    break;
                }
                sb.append(ste).append("; ");
                ++n2;
            }
            t = t.getCause();
            first = false;
        }
        return sb.toString();
    }
}

