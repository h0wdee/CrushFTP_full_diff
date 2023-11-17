/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.dsn;

import com.sun.mail.dsn.DeliveryStatus;
import com.sun.mail.dsn.Report;
import com.sun.mail.util.LineOutputStream;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;

public class DispositionNotification
extends Report {
    private static MailLogger logger = new MailLogger(DeliveryStatus.class, "DEBUG DSN", PropUtil.getBooleanSystemProperty("mail.dsn.debug", false), System.out);
    protected InternetHeaders notifications;

    public DispositionNotification() throws MessagingException {
        super("disposition-notification");
        this.notifications = new InternetHeaders();
    }

    public DispositionNotification(InputStream is) throws MessagingException, IOException {
        super("disposition-notification");
        this.notifications = new InternetHeaders(is);
        logger.fine("got MDN notification content");
    }

    public InternetHeaders getNotifications() {
        return this.notifications;
    }

    public void setNotifications(InternetHeaders notifications) {
        this.notifications = notifications;
    }

    public void writeTo(OutputStream os) throws IOException {
        LineOutputStream los = null;
        los = os instanceof LineOutputStream ? (LineOutputStream)os : new LineOutputStream(os);
        DispositionNotification.writeInternetHeaders(this.notifications, los);
        los.writeln();
    }

    private static void writeInternetHeaders(InternetHeaders h, LineOutputStream los) throws IOException {
        Enumeration e = h.getAllHeaderLines();
        while (e.hasMoreElements()) {
            los.writeln((String)e.nextElement());
        }
    }

    public String toString() {
        return "DispositionNotification: Reporting-UA=" + this.notifications.getHeader("Reporting-UA", null);
    }
}

