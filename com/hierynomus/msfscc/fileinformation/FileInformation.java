/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msfscc.fileinformation;

import com.hierynomus.msfscc.FileInformationClass;
import com.hierynomus.protocol.commons.buffer.Buffer;

public interface FileInformation {

    public static interface Codec<F extends FileInformation>
    extends Encoder<F>,
    Decoder<F> {
    }

    public static interface Decoder<F extends FileInformation> {
        public FileInformationClass getInformationClass();

        public F read(Buffer var1) throws Buffer.BufferException;
    }

    public static interface Encoder<F extends FileInformation> {
        public FileInformationClass getInformationClass();

        public void write(F var1, Buffer var2);
    }
}

