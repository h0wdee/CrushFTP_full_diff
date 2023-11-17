/*
 * Decompiled with CFR 0.152.
 */
package javax.mail.internet;

import java.util.concurrent.atomic.AtomicInteger;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

class UniqueValue {
    private static AtomicInteger id = new AtomicInteger();

    UniqueValue() {
    }

    public static String getUniqueBoundaryValue() {
        StringBuffer s = new StringBuffer();
        long hash = s.hashCode();
        s.append("----=_Part_").append(id.getAndIncrement()).append("_").append(hash).append('.').append(System.currentTimeMillis());
        return s.toString();
    }

    public static String getUniqueMessageIDValue(Session ssn) {
        String suffix = null;
        InternetAddress addr = InternetAddress.getLocalAddress(ssn);
        suffix = addr != null ? addr.getAddress() : "javamailuser@localhost";
        int at = suffix.lastIndexOf(64);
        if (at >= 0) {
            suffix = suffix.substring(at);
        }
        StringBuffer s = new StringBuffer();
        s.append(s.hashCode()).append('.').append(id.getAndIncrement()).append('.').append(System.currentTimeMillis()).append(suffix);
        return s.toString();
    }
}

