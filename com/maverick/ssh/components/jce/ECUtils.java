/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.Utils;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.util.SimpleASNWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ECUtils {
    static Logger log = LoggerFactory.getLogger(ECUtils.class);

    public static byte[] toByteArray(ECPoint e, EllipticCurve curve) {
        int len;
        int i;
        byte[] x = e.getAffineX().toByteArray();
        byte[] y = e.getAffineY().toByteArray();
        int xoff = 0;
        int yoff = 0;
        for (i = 0; i < x.length - 1; ++i) {
            if (x[i] == 0) continue;
            xoff = i;
            break;
        }
        for (i = 0; i < y.length - 1; ++i) {
            if (y[i] == 0) continue;
            yoff = i;
            break;
        }
        if (x.length - xoff > (len = (curve.getField().getFieldSize() + 7) / 8) || y.length - yoff > len) {
            return null;
        }
        byte[] ret = new byte[len * 2 + 1];
        ret[0] = 4;
        System.arraycopy(x, xoff, ret, 1 + len - (x.length - xoff), x.length - xoff);
        System.arraycopy(y, yoff, ret, ret.length - (y.length - yoff), y.length - yoff);
        return ret;
    }

    public static ECPoint fromByteArray(byte[] b, EllipticCurve curve) {
        int size = curve.getField().getFieldSize();
        int len = (size + 7) / 8;
        int expectedLength = 2 * len + 1;
        if (b.length != expectedLength || b[0] != 4) {
            if (log.isDebugEnabled()) {
                log.debug("Detected invalid EC point len=", (Object)b.length);
                log.debug(Utils.bytesToHex(b, 0, b.length, 16, true, false));
            }
            throw new IllegalStateException("Invalid EC Point!");
        }
        byte[] x = new byte[len];
        byte[] y = new byte[len];
        System.arraycopy(b, 1, x, 0, len);
        System.arraycopy(b, len + 1, y, 0, len);
        return new ECPoint(new BigInteger(1, x), new BigInteger(1, y));
    }

    public static byte[] ensureLeadingZero(byte[] x) {
        if (x[0] != 0) {
            byte[] tmp = new byte[x.length + 1];
            System.arraycopy(x, 0, tmp, 1, x.length);
            return tmp;
        }
        return x;
    }

    public static String getNameFromEncodedKey(PrivateKey prv) {
        byte[] encoded = prv.getEncoded();
        byte[] secp256r1 = new byte[]{42, -122, 72, -50, 61, 3, 1, 7};
        if (ECUtils.contains(encoded, secp256r1)) {
            return "secp256r1";
        }
        byte[] secp384r1 = new byte[]{43, -127, 4, 0, 34};
        if (ECUtils.contains(encoded, secp384r1)) {
            return "secp384r1";
        }
        byte[] secp521r1 = new byte[]{43, -127, 4, 0, 35};
        if (ECUtils.contains(encoded, secp521r1)) {
            return "secp521r1";
        }
        throw new IllegalArgumentException("Unable to determine EC curve type.");
    }

    private static boolean contains(byte[] source, byte[] find) {
        for (int i = 0; i < source.length; ++i) {
            if (source[i] != find[0]) continue;
            boolean matched = true;
            int numBytes = 0;
            for (int x = 0; x < find.length && x + i < source.length; ++x) {
                if (source[i + x] != find[x]) {
                    matched = false;
                    break;
                }
                ++numBytes;
            }
            if (!matched || numBytes != find.length) continue;
            return true;
        }
        return false;
    }

    private static byte[] createHeadForNamedCurve(String name, byte[] encoded) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException {
        SimpleASNWriter seq1 = new SimpleASNWriter();
        seq1.writeByte(6);
        seq1.writeData(new byte[]{42, -122, 72, -50, 61, 2, 1});
        switch (name) {
            case "secp256r1": 
            case "nistp256": {
                seq1.writeByte(6);
                seq1.writeData(new byte[]{42, -122, 72, -50, 61, 3, 1, 7});
                break;
            }
            case "secp384r1": 
            case "nistp384": {
                seq1.writeByte(6);
                seq1.writeData(new byte[]{43, -127, 4, 0, 34});
                break;
            }
            case "secp521r1": 
            case "nistp521": {
                seq1.writeByte(6);
                seq1.writeData(new byte[]{43, -127, 4, 0, 35});
                break;
            }
            default: {
                throw new IllegalStateException(String.format("Unsupported named curve %s", name));
            }
        }
        SimpleASNWriter seq2 = new SimpleASNWriter();
        seq2.writeByte(48);
        seq2.writeData(seq1.toByteArray());
        seq2.writeByte(3);
        byte[] k = new byte[encoded.length + 1];
        if (encoded[0] != 0) {
            System.arraycopy(encoded, 0, k, 1, encoded.length);
            seq2.writeData(k);
        } else {
            seq2.writeData(encoded);
        }
        SimpleASNWriter seq = new SimpleASNWriter();
        seq.writeByte(48);
        seq.writeData(seq2.toByteArray());
        return seq.toByteArray();
    }

    public static ECPublicKey convertKey(byte[] encodedKey) throws InvalidKeySpecException {
        KeyFactory eckf;
        try {
            eckf = JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()) == null ? KeyFactory.getInstance(JCEProvider.getECDSAAlgorithmName()) : KeyFactory.getInstance(JCEProvider.getECDSAAlgorithmName(), JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()));
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("EC key factory not present in runtime");
        }
        X509EncodedKeySpec ecpks = new X509EncodedKeySpec(encodedKey);
        return (ECPublicKey)eckf.generatePublic(ecpks);
    }

    public static ECPrivateKey decodePrivateKey(byte[] key, ECPublicKey pub) throws InvalidKeySpecException {
        KeyFactory eckf;
        BigInteger bi = new BigInteger(1, key);
        ECPrivateKeySpec spec = new ECPrivateKeySpec(bi, pub.getParams());
        try {
            eckf = JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()) == null ? KeyFactory.getInstance(JCEProvider.getECDSAAlgorithmName()) : KeyFactory.getInstance(JCEProvider.getECDSAAlgorithmName(), JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()));
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("EC key factory not present in runtime");
        }
        return (ECPrivateKey)eckf.generatePrivate(spec);
    }

    public static byte[] stripLeadingZeros(byte[] b) {
        int count = 0;
        for (int i = 0; i < b.length && b[i] == 0; ++i) {
            ++count;
        }
        byte[] tmp = new byte[b.length - count];
        System.arraycopy(b, count, tmp, 0, tmp.length);
        return tmp;
    }

    public static ECPublicKey decodeKey(byte[] encoded, String namedCurve) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        return ECUtils.convertKey(ECUtils.createHeadForNamedCurve(namedCurve, encoded));
    }

    public static ECPublicKey decodeJCEKey(byte[] encoded) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyFactory eckf;
        try {
            eckf = JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()) == null ? KeyFactory.getInstance(JCEProvider.getECDSAAlgorithmName()) : KeyFactory.getInstance(JCEProvider.getECDSAAlgorithmName(), JCEProvider.getProviderForAlgorithm(JCEProvider.getECDSAAlgorithmName()));
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("EC key factory not present in runtime");
        }
        X509EncodedKeySpec ecpks = new X509EncodedKeySpec(encoded);
        return (ECPublicKey)eckf.generatePublic(ecpks);
    }

    public static byte[] getOidBytes(String curve) {
        switch (curve) {
            case "secp256r1": {
                return new byte[]{42, -122, 72, -50, 61, 3, 1, 7};
            }
            case "secp384r1": {
                return new byte[]{43, -127, 4, 0, 34};
            }
            case "secp521r1": {
                return new byte[]{43, -127, 4, 0, 35};
            }
        }
        throw new IllegalStateException(String.format("Unsupported named curve %s", curve));
    }
}

