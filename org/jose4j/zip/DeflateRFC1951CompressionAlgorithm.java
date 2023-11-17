/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import org.jose4j.keys.KeyPersuasion;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UncheckedJoseException;
import org.jose4j.zip.CompressionAlgorithm;

public class DeflateRFC1951CompressionAlgorithm
implements CompressionAlgorithm {
    /*
     * Loose catch block
     */
    @Override
    public byte[] compress(byte[] data) {
        Deflater deflater = new Deflater(8, true);
        try {
            Throwable throwable = null;
            Object var4_6 = null;
            try {
                byte[] byArray;
                DeflaterOutputStream deflaterOutputStream;
                ByteArrayOutputStream byteArrayOutputStream;
                block18: {
                    block17: {
                        byteArrayOutputStream = new ByteArrayOutputStream();
                        deflaterOutputStream = new DeflaterOutputStream((OutputStream)byteArrayOutputStream, deflater);
                        deflaterOutputStream.write(data);
                        deflaterOutputStream.finish();
                        byArray = byteArrayOutputStream.toByteArray();
                        if (deflaterOutputStream == null) break block17;
                        deflaterOutputStream.close();
                    }
                    if (byteArrayOutputStream == null) break block18;
                    byteArrayOutputStream.close();
                }
                return byArray;
                {
                    catch (Throwable throwable2) {
                        try {
                            if (deflaterOutputStream != null) {
                                deflaterOutputStream.close();
                            }
                            throw throwable2;
                        }
                        catch (Throwable throwable3) {
                            if (throwable == null) {
                                throwable = throwable3;
                            } else if (throwable != throwable3) {
                                throwable.addSuppressed(throwable3);
                            }
                            if (byteArrayOutputStream != null) {
                                byteArrayOutputStream.close();
                            }
                            throw throwable;
                        }
                    }
                }
            }
            catch (Throwable throwable4) {
                if (throwable == null) {
                    throwable = throwable4;
                } else if (throwable != throwable4) {
                    throwable.addSuppressed(throwable4);
                }
                throw throwable;
            }
        }
        catch (IOException e) {
            throw new UncheckedJoseException("Problem compressing data.", e);
        }
    }

    @Override
    public byte[] decompress(byte[] compressedData) throws JoseException {
        Inflater inflater = new Inflater(true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            Throwable throwable = null;
            Object var5_7 = null;
            try (InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(compressedData), inflater);){
                int bytesRead;
                byte[] buff = new byte[256];
                while ((bytesRead = iis.read(buff)) != -1) {
                    byteArrayOutputStream.write(buff, 0, bytesRead);
                }
                return byteArrayOutputStream.toByteArray();
            }
            catch (Throwable throwable2) {
                if (throwable == null) {
                    throwable = throwable2;
                } else if (throwable != throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
        }
        catch (IOException e) {
            throw new JoseException("Problem decompressing data.", e);
        }
    }

    @Override
    public String getJavaAlgorithm() {
        return null;
    }

    @Override
    public String getAlgorithmIdentifier() {
        return "DEF";
    }

    @Override
    public KeyPersuasion getKeyPersuasion() {
        return KeyPersuasion.NONE;
    }

    @Override
    public String getKeyType() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}

