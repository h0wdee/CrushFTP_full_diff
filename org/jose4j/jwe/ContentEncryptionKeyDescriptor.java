/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

public class ContentEncryptionKeyDescriptor {
    private final int contentEncryptionKeyByteLength;
    private final String contentEncryptionKeyAlgorithm;

    public ContentEncryptionKeyDescriptor(int contentEncryptionKeyByteLength, String contentEncryptionKeyAlgorithm) {
        this.contentEncryptionKeyByteLength = contentEncryptionKeyByteLength;
        this.contentEncryptionKeyAlgorithm = contentEncryptionKeyAlgorithm;
    }

    public int getContentEncryptionKeyByteLength() {
        return this.contentEncryptionKeyByteLength;
    }

    public String getContentEncryptionKeyAlgorithm() {
        return this.contentEncryptionKeyAlgorithm;
    }
}

