/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.ActivationDataFlavor
 *  javax.activation.DataContentHandler
 *  javax.activation.DataSource
 */
package com.sun.mail.handlers;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;

public abstract class handler_base
implements DataContentHandler {
    protected abstract ActivationDataFlavor[] getDataFlavors();

    protected Object getData(ActivationDataFlavor aFlavor, DataSource ds) throws IOException {
        return this.getContent(ds);
    }

    public DataFlavor[] getTransferDataFlavors() {
        ActivationDataFlavor[] adf = this.getDataFlavors();
        if (adf.length == 1) {
            return new DataFlavor[]{adf[0]};
        }
        DataFlavor[] df = new DataFlavor[adf.length];
        System.arraycopy(adf, 0, df, 0, adf.length);
        return df;
    }

    public Object getTransferData(DataFlavor df, DataSource ds) throws IOException {
        ActivationDataFlavor[] adf = this.getDataFlavors();
        for (int i = 0; i < adf.length; ++i) {
            if (!adf[i].equals(df)) continue;
            return this.getData(adf[i], ds);
        }
        return null;
    }
}

