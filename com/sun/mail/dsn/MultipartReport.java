/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.DataSource
 */
package com.sun.mail.dsn;

import com.sun.mail.dsn.DeliveryStatus;
import com.sun.mail.dsn.MessageHeaders;
import com.sun.mail.dsn.Report;
import java.io.IOException;
import java.util.Vector;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MultipartReport
extends MimeMultipart {
    protected boolean constructed;

    public MultipartReport() throws MessagingException {
        super("report");
        MimeBodyPart mbp = new MimeBodyPart();
        this.setBodyPart(mbp, 0);
        mbp = new MimeBodyPart();
        this.setBodyPart(mbp, 1);
        this.constructed = true;
    }

    public MultipartReport(String text, Report report) throws MessagingException {
        super("report");
        ContentType ct = new ContentType(this.contentType);
        String reportType = report.getType();
        ct.setParameter("report-type", reportType);
        this.contentType = ct.toString();
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setText(text);
        this.setBodyPart(mbp, 0);
        mbp = new MimeBodyPart();
        ct = new ContentType("message", reportType, null);
        mbp.setContent(report, ct.toString());
        this.setBodyPart(mbp, 1);
        this.constructed = true;
    }

    public MultipartReport(String text, Report report, MimeMessage msg) throws MessagingException {
        this(text, report);
        if (msg != null) {
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setContent(msg, "message/rfc822");
            this.setBodyPart(mbp, 2);
        }
    }

    public MultipartReport(String text, Report report, InternetHeaders hdr) throws MessagingException {
        this(text, report);
        if (hdr != null) {
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setContent(new MessageHeaders(hdr), "text/rfc822-headers");
            this.setBodyPart(mbp, 2);
        }
    }

    public MultipartReport(DataSource ds) throws MessagingException {
        super(ds);
        this.parse();
        this.constructed = true;
    }

    public synchronized String getText() throws MessagingException {
        try {
            BodyPart bp = this.getBodyPart(0);
            if (bp.isMimeType("text/plain")) {
                return (String)bp.getContent();
            }
            if (bp.isMimeType("multipart/alternative")) {
                Multipart mp = (Multipart)bp.getContent();
                for (int i = 0; i < mp.getCount(); ++i) {
                    bp = mp.getBodyPart(i);
                    if (!bp.isMimeType("text/plain")) continue;
                    return (String)bp.getContent();
                }
            }
        }
        catch (IOException ex) {
            throw new MessagingException("Exception getting text content", ex);
        }
        return null;
    }

    public synchronized void setText(String text) throws MessagingException {
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setText(text);
        this.setBodyPart(mbp, 0);
    }

    public synchronized MimeBodyPart getTextBodyPart() throws MessagingException {
        return (MimeBodyPart)this.getBodyPart(0);
    }

    public synchronized void setTextBodyPart(MimeBodyPart mbp) throws MessagingException {
        this.setBodyPart(mbp, 0);
    }

    public synchronized Report getReport() throws MessagingException {
        if (this.getCount() < 2) {
            return null;
        }
        BodyPart bp = this.getBodyPart(1);
        try {
            Object content = bp.getContent();
            if (!(content instanceof Report)) {
                return null;
            }
            return (Report)content;
        }
        catch (IOException ex) {
            throw new MessagingException("IOException getting Report", ex);
        }
    }

    public synchronized void setReport(Report report) throws MessagingException {
        MimeBodyPart mbp = new MimeBodyPart();
        ContentType ct = new ContentType(this.contentType);
        String reportType = report.getType();
        ct.setParameter("report-type", reportType);
        this.contentType = ct.toString();
        ct = new ContentType("message", reportType, null);
        mbp.setContent(report, ct.toString());
        this.setBodyPart(mbp, 1);
    }

    @Deprecated
    public synchronized DeliveryStatus getDeliveryStatus() throws MessagingException {
        if (this.getCount() < 2) {
            return null;
        }
        BodyPart bp = this.getBodyPart(1);
        if (!bp.isMimeType("message/delivery-status")) {
            return null;
        }
        try {
            return (DeliveryStatus)bp.getContent();
        }
        catch (IOException ex) {
            throw new MessagingException("IOException getting DeliveryStatus", ex);
        }
    }

    public synchronized void setDeliveryStatus(DeliveryStatus status) throws MessagingException {
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setContent(status, "message/delivery-status");
        this.setBodyPart(mbp, 1);
        ContentType ct = new ContentType(this.contentType);
        ct.setParameter("report-type", "delivery-status");
        this.contentType = ct.toString();
    }

    public synchronized MimeMessage getReturnedMessage() throws MessagingException {
        if (this.getCount() < 3) {
            return null;
        }
        BodyPart bp = this.getBodyPart(2);
        if (!bp.isMimeType("message/rfc822") && !bp.isMimeType("text/rfc822-headers")) {
            return null;
        }
        try {
            return (MimeMessage)bp.getContent();
        }
        catch (IOException ex) {
            throw new MessagingException("IOException getting ReturnedMessage", ex);
        }
    }

    public synchronized void setReturnedMessage(MimeMessage msg) throws MessagingException {
        if (msg == null) {
            super.removeBodyPart(2);
            return;
        }
        MimeBodyPart mbp = new MimeBodyPart();
        if (msg instanceof MessageHeaders) {
            mbp.setContent(msg, "text/rfc822-headers");
        } else {
            mbp.setContent(msg, "message/rfc822");
        }
        this.setBodyPart(mbp, 2);
    }

    private synchronized void setBodyPart(BodyPart part, int index) throws MessagingException {
        if (this.parts == null) {
            this.parts = new Vector();
        }
        if (index < this.parts.size()) {
            super.removeBodyPart(index);
        }
        super.addBodyPart(part, index);
    }

    public synchronized void setSubType(String subtype) throws MessagingException {
        throw new MessagingException("Can't change subtype of MultipartReport");
    }

    public boolean removeBodyPart(BodyPart part) throws MessagingException {
        throw new MessagingException("Can't remove body parts from multipart/report");
    }

    public void removeBodyPart(int index) throws MessagingException {
        throw new MessagingException("Can't remove body parts from multipart/report");
    }

    public synchronized void addBodyPart(BodyPart part) throws MessagingException {
        if (this.constructed) {
            throw new MessagingException("Can't add body parts to multipart/report 1");
        }
        super.addBodyPart(part);
    }

    public synchronized void addBodyPart(BodyPart part, int index) throws MessagingException {
        throw new MessagingException("Can't add body parts to multipart/report 2");
    }
}

