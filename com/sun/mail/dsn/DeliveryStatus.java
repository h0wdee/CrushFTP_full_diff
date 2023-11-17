/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.dsn;

import com.sun.mail.dsn.Report;
import com.sun.mail.util.LineOutputStream;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;

public class DeliveryStatus
extends Report {
    private static MailLogger logger = new MailLogger(DeliveryStatus.class, "DEBUG DSN", PropUtil.getBooleanSystemProperty("mail.dsn.debug", false), System.out);
    protected InternetHeaders messageDSN;
    protected InternetHeaders[] recipientDSN;

    public DeliveryStatus() throws MessagingException {
        super("delivery-status");
        this.messageDSN = new InternetHeaders();
        this.recipientDSN = new InternetHeaders[0];
    }

    public DeliveryStatus(InputStream is) throws MessagingException, IOException {
        super("delivery-status");
        this.messageDSN = new InternetHeaders(is);
        logger.fine("got messageDSN");
        Vector<InternetHeaders> v = new Vector<InternetHeaders>();
        try {
            while (is.available() > 0) {
                InternetHeaders h = new InternetHeaders(is);
                logger.fine("got recipientDSN");
                v.addElement(h);
            }
        }
        catch (EOFException ex) {
            logger.log(Level.FINE, "got EOFException", ex);
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("recipientDSN size " + v.size());
        }
        this.recipientDSN = new InternetHeaders[v.size()];
        v.copyInto(this.recipientDSN);
    }

    public InternetHeaders getMessageDSN() {
        return this.messageDSN;
    }

    public void setMessageDSN(InternetHeaders messageDSN) {
        this.messageDSN = messageDSN;
    }

    public int getRecipientDSNCount() {
        return this.recipientDSN.length;
    }

    public InternetHeaders getRecipientDSN(int n) {
        return this.recipientDSN[n];
    }

    public void addRecipientDSN(InternetHeaders h) {
        InternetHeaders[] rh = new InternetHeaders[this.recipientDSN.length + 1];
        System.arraycopy(this.recipientDSN, 0, rh, 0, this.recipientDSN.length);
        this.recipientDSN = rh;
        this.recipientDSN[this.recipientDSN.length - 1] = h;
    }

    public void writeTo(OutputStream os) throws IOException {
        LineOutputStream los = null;
        los = os instanceof LineOutputStream ? (LineOutputStream)os : new LineOutputStream(os);
        DeliveryStatus.writeInternetHeaders(this.messageDSN, los);
        los.writeln();
        for (int i = 0; i < this.recipientDSN.length; ++i) {
            DeliveryStatus.writeInternetHeaders(this.recipientDSN[i], los);
            los.writeln();
        }
    }

    private static void writeInternetHeaders(InternetHeaders h, LineOutputStream los) throws IOException {
        Enumeration e = h.getAllHeaderLines();
        while (e.hasMoreElements()) {
            los.writeln((String)e.nextElement());
        }
    }

    public String toString() {
        return "DeliveryStatus: Reporting-MTA=" + this.messageDSN.getHeader("Reporting-MTA", null) + ", #Recipients=" + this.recipientDSN.length;
    }
}

