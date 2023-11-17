/*
 * Decompiled with CFR 0.152.
 */
package javax.mail.search;

import javax.mail.Message;
import javax.mail.search.StringTerm;

public final class MessageIDTerm
extends StringTerm {
    private static final long serialVersionUID = -2121096296454691963L;

    public MessageIDTerm(String msgid) {
        super(msgid);
    }

    public boolean match(Message msg) {
        String[] s;
        try {
            s = msg.getHeader("Message-ID");
        }
        catch (Exception e) {
            return false;
        }
        if (s == null) {
            return false;
        }
        for (int i = 0; i < s.length; ++i) {
            if (!super.match(s[i])) continue;
            return true;
        }
        return false;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof MessageIDTerm)) {
            return false;
        }
        return super.equals(obj);
    }
}

