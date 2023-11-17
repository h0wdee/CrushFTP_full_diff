/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.smtp;

import com.sun.mail.smtp.SMTPTransport;
import javax.mail.Session;
import javax.mail.URLName;

public class SMTPSSLTransport
extends SMTPTransport {
    public SMTPSSLTransport(Session session, URLName urlname) {
        super(session, urlname, "smtps", true);
    }
}

