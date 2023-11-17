/*
 * Decompiled with CFR 0.152.
 */
package javax.mail.search;

import java.util.Date;
import javax.mail.Message;
import javax.mail.search.DateTerm;

public final class SentDateTerm
extends DateTerm {
    private static final long serialVersionUID = 5647755030530907263L;

    public SentDateTerm(int comparison, Date date) {
        super(comparison, date);
    }

    public boolean match(Message msg) {
        Date d;
        try {
            d = msg.getSentDate();
        }
        catch (Exception e) {
            return false;
        }
        if (d == null) {
            return false;
        }
        return super.match(d);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SentDateTerm)) {
            return false;
        }
        return super.equals(obj);
    }
}

