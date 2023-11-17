/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.ActivationDataFlavor
 */
package com.sun.mail.handlers;

import com.sun.mail.handlers.image_gif;
import java.awt.Image;
import javax.activation.ActivationDataFlavor;

public class image_jpeg
extends image_gif {
    private static ActivationDataFlavor[] myDF = new ActivationDataFlavor[]{new ActivationDataFlavor(Image.class, "image/jpeg", "JPEG Image")};

    protected ActivationDataFlavor[] getDataFlavors() {
        return myDF;
    }
}

