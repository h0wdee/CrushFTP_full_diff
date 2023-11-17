/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.ActivationDataFlavor
 *  javax.activation.DataContentHandler
 *  javax.activation.DataSource
 */
package com.sun.mail.dsn;

import com.sun.mail.dsn.MultipartReport;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.OutputStream;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;

public class multipart_report
implements DataContentHandler {
    private ActivationDataFlavor myDF = new ActivationDataFlavor(MultipartReport.class, "multipart/report", "Multipart Report");

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{this.myDF};
    }

    public Object getTransferData(DataFlavor df, DataSource ds) throws IOException {
        if (this.myDF.equals(df)) {
            return this.getContent(ds);
        }
        return null;
    }

    public Object getContent(DataSource ds) throws IOException {
        try {
            return new MultipartReport(ds);
        }
        catch (MessagingException e) {
            IOException ioex = new IOException("Exception while constructing MultipartReport");
            ioex.initCause(e);
            throw ioex;
        }
    }

    public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException {
        if (obj instanceof MultipartReport) {
            try {
                ((MultipartReport)obj).writeTo(os);
            }
            catch (MessagingException e) {
                throw new IOException(e.toString());
            }
        }
    }
}

