/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.imap;

import com.sun.mail.imap.IMAPStore;
import javax.mail.Session;
import javax.mail.URLName;

public class IMAPSSLStore
extends IMAPStore {
    public IMAPSSLStore(Session session, URLName url) {
        super(session, url, "imaps", true);
    }
}

