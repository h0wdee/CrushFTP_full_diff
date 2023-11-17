/*
 * Decompiled with CFR 0.152.
 */
package javax.mail.search;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.search.StringTerm;

public abstract class AddressStringTerm
extends StringTerm {
    private static final long serialVersionUID = 3086821234204980368L;

    protected AddressStringTerm(String pattern) {
        super(pattern, true);
    }

    protected boolean match(Address a) {
        if (a instanceof InternetAddress) {
            InternetAddress ia = (InternetAddress)a;
            return super.match(ia.toUnicodeString());
        }
        return super.match(a.toString());
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof AddressStringTerm)) {
            return false;
        }
        return super.equals(obj);
    }
}

