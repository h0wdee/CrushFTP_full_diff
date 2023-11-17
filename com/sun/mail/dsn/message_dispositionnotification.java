/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.ActivationDataFlavor
 *  javax.activation.DataContentHandler
 *  javax.activation.DataSource
 */
package com.sun.mail.dsn;

import com.sun.mail.dsn.DispositionNotification;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.OutputStream;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;

public class message_dispositionnotification
implements DataContentHandler {
    ActivationDataFlavor ourDataFlavor = new ActivationDataFlavor(DispositionNotification.class, "message/disposition-notification", "Disposition Notification");

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{this.ourDataFlavor};
    }

    public Object getTransferData(DataFlavor df, DataSource ds) throws IOException {
        if (this.ourDataFlavor.equals(df)) {
            return this.getContent(ds);
        }
        return null;
    }

    public Object getContent(DataSource ds) throws IOException {
        try {
            return new DispositionNotification(ds.getInputStream());
        }
        catch (MessagingException me) {
            throw new IOException("Exception creating DispositionNotification in message/disposition-notification DataContentHandler: " + me.toString());
        }
    }

    public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException {
        if (!(obj instanceof DispositionNotification)) {
            throw new IOException("unsupported object");
        }
        DispositionNotification dn = (DispositionNotification)obj;
        dn.writeTo(os);
    }
}

