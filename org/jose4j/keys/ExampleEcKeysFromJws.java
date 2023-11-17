/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.keys;

import java.math.BigInteger;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import org.jose4j.keys.BigEndianBigInteger;
import org.jose4j.keys.EcKeyUtil;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;
import org.slf4j.LoggerFactory;

public class ExampleEcKeysFromJws {
    public static final int[] X_INTS_256 = new int[]{127, 205, 206, 39, 112, 246, 196, 93, 65, 131, 203, 238, 111, 219, 75, 123, 88, 7, 51, 53, 123, 233, 239, 19, 186, 207, 110, 60, 123, 209, 84, 69};
    public static final int[] Y_INTS_256 = new int[]{199, 241, 68, 205, 27, 189, 155, 126, 135, 44, 223, 237, 185, 238, 185, 244, 179, 105, 93, 110, 169, 11, 36, 173, 138, 70, 35, 40, 133, 136, 229, 173};
    public static final int[] D_INTS_256 = new int[]{142, 155, 16, 158, 113, 144, 152, 191, 152, 4, 135, 223, 31, 93, 119, 233, 203, 41, 96, 110, 190, 210, 38, 59, 95, 87, 194, 19, 223, 132, 244, 178};
    public static final byte[] X_BYTES_256 = ByteUtil.convertUnsignedToSignedTwosComp(X_INTS_256);
    public static final byte[] Y_BYTES_256 = ByteUtil.convertUnsignedToSignedTwosComp(Y_INTS_256);
    public static final byte[] D_BYTES_256 = ByteUtil.convertUnsignedToSignedTwosComp(D_INTS_256);
    public static final BigInteger X_256 = BigEndianBigInteger.fromBytes(X_BYTES_256);
    public static final BigInteger Y_256 = BigEndianBigInteger.fromBytes(Y_BYTES_256);
    public static final BigInteger D_256 = BigEndianBigInteger.fromBytes(D_BYTES_256);
    public static ECPrivateKey PRIVATE_256 = null;
    public static ECPublicKey PUBLIC_256 = null;
    public static final int[] X_INTS_521 = new int[]{1, 233, 41, 5, 15, 18, 79, 198, 188, 85, 199, 213, 57, 51, 101, 223, 157, 239, 74, 176, 194, 44, 178, 87, 152, 249, 52, 235, 4, 227, 198, 186, 227, 112, 26, 87, 167, 145, 14, 157, 129, 191, 54, 49, 89, 232, 235, 203, 21, 93, 99, 73, 244, 189, 182, 204, 248, 169, 76, 92, 89, 199, 170, 193, 1, 164};
    public static final int[] Y_INTS_521;
    public static final int[] D_INTS_521;
    public static final byte[] X_BYTES_521;
    public static final byte[] Y_BYTES_521;
    public static final byte[] D_BYTES_521;
    public static final BigInteger X_521;
    public static final BigInteger Y_521;
    public static final BigInteger D_521;
    public static ECPrivateKey PRIVATE_521;
    public static ECPublicKey PUBLIC_521;

    static {
        int[] nArray = new int[66];
        nArray[1] = 52;
        nArray[2] = 166;
        nArray[3] = 68;
        nArray[4] = 14;
        nArray[5] = 55;
        nArray[6] = 103;
        nArray[7] = 80;
        nArray[8] = 210;
        nArray[9] = 55;
        nArray[10] = 31;
        nArray[11] = 209;
        nArray[12] = 189;
        nArray[13] = 194;
        nArray[14] = 200;
        nArray[15] = 243;
        nArray[16] = 183;
        nArray[17] = 29;
        nArray[18] = 47;
        nArray[19] = 78;
        nArray[20] = 229;
        nArray[21] = 234;
        nArray[22] = 52;
        nArray[23] = 50;
        nArray[24] = 200;
        nArray[25] = 21;
        nArray[26] = 204;
        nArray[27] = 163;
        nArray[28] = 21;
        nArray[29] = 96;
        nArray[30] = 254;
        nArray[31] = 93;
        nArray[32] = 147;
        nArray[33] = 135;
        nArray[34] = 236;
        nArray[35] = 119;
        nArray[36] = 75;
        nArray[37] = 85;
        nArray[38] = 131;
        nArray[39] = 134;
        nArray[40] = 48;
        nArray[41] = 229;
        nArray[42] = 203;
        nArray[43] = 191;
        nArray[44] = 90;
        nArray[45] = 140;
        nArray[46] = 190;
        nArray[47] = 10;
        nArray[48] = 145;
        nArray[49] = 221;
        nArray[51] = 100;
        nArray[52] = 198;
        nArray[53] = 153;
        nArray[54] = 154;
        nArray[55] = 31;
        nArray[56] = 110;
        nArray[57] = 110;
        nArray[58] = 103;
        nArray[59] = 250;
        nArray[60] = 221;
        nArray[61] = 237;
        nArray[62] = 228;
        nArray[63] = 200;
        nArray[64] = 200;
        nArray[65] = 246;
        Y_INTS_521 = nArray;
        D_INTS_521 = new int[]{1, 142, 105, 111, 176, 52, 80, 88, 129, 221, 17, 11, 72, 62, 184, 125, 50, 206, 73, 95, 227, 107, 55, 69, 237, 242, 216, 202, 228, 240, 242, 83, 159, 70, 21, 160, 233, 142, 171, 82, 179, 192, 197, 234, 196, 206, 7, 81, 133, 168, 231, 187, 71, 222, 172, 29, 29, 231, 123, 204, 246, 97, 53, 230, 61, 130};
        X_BYTES_521 = ByteUtil.convertUnsignedToSignedTwosComp(X_INTS_521);
        Y_BYTES_521 = ByteUtil.convertUnsignedToSignedTwosComp(Y_INTS_521);
        D_BYTES_521 = ByteUtil.convertUnsignedToSignedTwosComp(D_INTS_521);
        X_521 = BigEndianBigInteger.fromBytes(X_BYTES_521);
        Y_521 = BigEndianBigInteger.fromBytes(Y_BYTES_521);
        D_521 = BigEndianBigInteger.fromBytes(D_BYTES_521);
        PRIVATE_521 = null;
        PUBLIC_521 = null;
        EcKeyUtil ecKeyUtil = new EcKeyUtil();
        try {
            PRIVATE_256 = ecKeyUtil.privateKey(D_256, EllipticCurves.P256);
            PUBLIC_256 = ecKeyUtil.publicKey(X_256, Y_256, EllipticCurves.P256);
            PRIVATE_521 = ecKeyUtil.privateKey(D_521, EllipticCurves.P521);
            PUBLIC_521 = ecKeyUtil.publicKey(X_521, Y_521, EllipticCurves.P521);
        }
        catch (JoseException e) {
            LoggerFactory.getLogger(ExampleEcKeysFromJws.class).warn("Unable to initialize Example EC keys.", e);
        }
    }
}

