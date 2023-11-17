/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.ActivationDataFlavor
 */
package com.sun.mail.handlers;

import com.sun.mail.handlers.text_plain;
import javax.activation.ActivationDataFlavor;

public class text_html
extends text_plain {
    private static ActivationDataFlavor[] myDF = new ActivationDataFlavor[]{new ActivationDataFlavor(String.class, "text/html", "HTML String")};

    protected ActivationDataFlavor[] getDataFlavors() {
        return myDF;
    }
}

