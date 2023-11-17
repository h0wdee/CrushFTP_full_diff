/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.base64url.internal.apache.commons.codec.binary;

import java.math.BigInteger;
import org.jose4j.base64url.internal.apache.commons.codec.binary.BaseNCodec;
import org.jose4j.lang.StringUtil;

public class Base64
extends BaseNCodec {
    private static final int BITS_PER_ENCODED_BYTE = 6;
    private static final int BYTES_PER_UNENCODED_BLOCK = 3;
    private static final int BYTES_PER_ENCODED_BLOCK = 4;
    static final byte[] CHUNK_SEPARATOR = new byte[]{13, 10};
    private static final byte[] STANDARD_ENCODE_TABLE = new byte[]{65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
    private static final byte[] URL_SAFE_ENCODE_TABLE = new byte[]{65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95};
    private static final byte[] DECODE_TABLE;
    private static final int MASK_6BITS = 63;
    private final byte[] encodeTable;
    private final byte[] decodeTable = DECODE_TABLE;
    private final byte[] lineSeparator;
    private final int decodeSize;
    private final int encodeSize;

    static {
        byte[] byArray = new byte[123];
        byArray[0] = -1;
        byArray[1] = -1;
        byArray[2] = -1;
        byArray[3] = -1;
        byArray[4] = -1;
        byArray[5] = -1;
        byArray[6] = -1;
        byArray[7] = -1;
        byArray[8] = -1;
        byArray[9] = -1;
        byArray[10] = -1;
        byArray[11] = -1;
        byArray[12] = -1;
        byArray[13] = -1;
        byArray[14] = -1;
        byArray[15] = -1;
        byArray[16] = -1;
        byArray[17] = -1;
        byArray[18] = -1;
        byArray[19] = -1;
        byArray[20] = -1;
        byArray[21] = -1;
        byArray[22] = -1;
        byArray[23] = -1;
        byArray[24] = -1;
        byArray[25] = -1;
        byArray[26] = -1;
        byArray[27] = -1;
        byArray[28] = -1;
        byArray[29] = -1;
        byArray[30] = -1;
        byArray[31] = -1;
        byArray[32] = -1;
        byArray[33] = -1;
        byArray[34] = -1;
        byArray[35] = -1;
        byArray[36] = -1;
        byArray[37] = -1;
        byArray[38] = -1;
        byArray[39] = -1;
        byArray[40] = -1;
        byArray[41] = -1;
        byArray[42] = -1;
        byArray[43] = 62;
        byArray[44] = -1;
        byArray[45] = 62;
        byArray[46] = -1;
        byArray[47] = 63;
        byArray[48] = 52;
        byArray[49] = 53;
        byArray[50] = 54;
        byArray[51] = 55;
        byArray[52] = 56;
        byArray[53] = 57;
        byArray[54] = 58;
        byArray[55] = 59;
        byArray[56] = 60;
        byArray[57] = 61;
        byArray[58] = -1;
        byArray[59] = -1;
        byArray[60] = -1;
        byArray[61] = -1;
        byArray[62] = -1;
        byArray[63] = -1;
        byArray[64] = -1;
        byArray[66] = 1;
        byArray[67] = 2;
        byArray[68] = 3;
        byArray[69] = 4;
        byArray[70] = 5;
        byArray[71] = 6;
        byArray[72] = 7;
        byArray[73] = 8;
        byArray[74] = 9;
        byArray[75] = 10;
        byArray[76] = 11;
        byArray[77] = 12;
        byArray[78] = 13;
        byArray[79] = 14;
        byArray[80] = 15;
        byArray[81] = 16;
        byArray[82] = 17;
        byArray[83] = 18;
        byArray[84] = 19;
        byArray[85] = 20;
        byArray[86] = 21;
        byArray[87] = 22;
        byArray[88] = 23;
        byArray[89] = 24;
        byArray[90] = 25;
        byArray[91] = -1;
        byArray[92] = -1;
        byArray[93] = -1;
        byArray[94] = -1;
        byArray[95] = 63;
        byArray[96] = -1;
        byArray[97] = 26;
        byArray[98] = 27;
        byArray[99] = 28;
        byArray[100] = 29;
        byArray[101] = 30;
        byArray[102] = 31;
        byArray[103] = 32;
        byArray[104] = 33;
        byArray[105] = 34;
        byArray[106] = 35;
        byArray[107] = 36;
        byArray[108] = 37;
        byArray[109] = 38;
        byArray[110] = 39;
        byArray[111] = 40;
        byArray[112] = 41;
        byArray[113] = 42;
        byArray[114] = 43;
        byArray[115] = 44;
        byArray[116] = 45;
        byArray[117] = 46;
        byArray[118] = 47;
        byArray[119] = 48;
        byArray[120] = 49;
        byArray[121] = 50;
        byArray[122] = 51;
        DECODE_TABLE = byArray;
    }

    public Base64() {
        this(0);
    }

    public Base64(boolean urlSafe) {
        this(76, CHUNK_SEPARATOR, urlSafe);
    }

    public Base64(int lineLength) {
        this(lineLength, CHUNK_SEPARATOR);
    }

    public Base64(int lineLength, byte[] lineSeparator) {
        this(lineLength, lineSeparator, false);
    }

    public Base64(int lineLength, byte[] lineSeparator, boolean urlSafe) {
        super(3, 4, lineLength, lineSeparator == null ? 0 : lineSeparator.length);
        if (lineSeparator != null) {
            if (this.containsAlphabetOrPad(lineSeparator)) {
                String sep = StringUtil.newStringUtf8(lineSeparator);
                throw new IllegalArgumentException("lineSeparator must not contain base64 characters: [" + sep + "]");
            }
            if (lineLength > 0) {
                this.encodeSize = 4 + lineSeparator.length;
                this.lineSeparator = new byte[lineSeparator.length];
                System.arraycopy(lineSeparator, 0, this.lineSeparator, 0, lineSeparator.length);
            } else {
                this.encodeSize = 4;
                this.lineSeparator = null;
            }
        } else {
            this.encodeSize = 4;
            this.lineSeparator = null;
        }
        this.decodeSize = this.encodeSize - 1;
        this.encodeTable = urlSafe ? URL_SAFE_ENCODE_TABLE : STANDARD_ENCODE_TABLE;
    }

    public boolean isUrlSafe() {
        return this.encodeTable == URL_SAFE_ENCODE_TABLE;
    }

    @Override
    void encode(byte[] in, int inPos, int inAvail, BaseNCodec.Context context) {
        if (context.eof) {
            return;
        }
        if (inAvail < 0) {
            context.eof = true;
            if (context.modulus == 0 && this.lineLength == 0) {
                return;
            }
            byte[] buffer = this.ensureBufferSize(this.encodeSize, context);
            int savedPos = context.pos;
            switch (context.modulus) {
                case 0: {
                    break;
                }
                case 1: {
                    buffer[context.pos++] = this.encodeTable[context.ibitWorkArea >> 2 & 0x3F];
                    buffer[context.pos++] = this.encodeTable[context.ibitWorkArea << 4 & 0x3F];
                    if (this.encodeTable != STANDARD_ENCODE_TABLE) break;
                    buffer[context.pos++] = 61;
                    buffer[context.pos++] = 61;
                    break;
                }
                case 2: {
                    buffer[context.pos++] = this.encodeTable[context.ibitWorkArea >> 10 & 0x3F];
                    buffer[context.pos++] = this.encodeTable[context.ibitWorkArea >> 4 & 0x3F];
                    buffer[context.pos++] = this.encodeTable[context.ibitWorkArea << 2 & 0x3F];
                    if (this.encodeTable != STANDARD_ENCODE_TABLE) break;
                    buffer[context.pos++] = 61;
                    break;
                }
                default: {
                    throw new IllegalStateException("Impossible modulus " + context.modulus);
                }
            }
            context.currentLinePos += context.pos - savedPos;
            if (this.lineLength > 0 && context.currentLinePos > 0) {
                System.arraycopy(this.lineSeparator, 0, buffer, context.pos, this.lineSeparator.length);
                context.pos += this.lineSeparator.length;
            }
        } else {
            int i = 0;
            while (i < inAvail) {
                int b;
                byte[] buffer = this.ensureBufferSize(this.encodeSize, context);
                context.modulus = (context.modulus + 1) % 3;
                if ((b = in[inPos++]) < 0) {
                    b += 256;
                }
                context.ibitWorkArea = (context.ibitWorkArea << 8) + b;
                if (context.modulus == 0) {
                    buffer[context.pos++] = this.encodeTable[context.ibitWorkArea >> 18 & 0x3F];
                    buffer[context.pos++] = this.encodeTable[context.ibitWorkArea >> 12 & 0x3F];
                    buffer[context.pos++] = this.encodeTable[context.ibitWorkArea >> 6 & 0x3F];
                    buffer[context.pos++] = this.encodeTable[context.ibitWorkArea & 0x3F];
                    context.currentLinePos += 4;
                    if (this.lineLength > 0 && this.lineLength <= context.currentLinePos) {
                        System.arraycopy(this.lineSeparator, 0, buffer, context.pos, this.lineSeparator.length);
                        context.pos += this.lineSeparator.length;
                        context.currentLinePos = 0;
                    }
                }
                ++i;
            }
        }
    }

    @Override
    void decode(byte[] in, int inPos, int inAvail, BaseNCodec.Context context) {
        if (context.eof) {
            return;
        }
        if (inAvail < 0) {
            context.eof = true;
        }
        int i = 0;
        while (i < inAvail) {
            byte result;
            byte b;
            byte[] buffer = this.ensureBufferSize(this.decodeSize, context);
            if ((b = in[inPos++]) == 61) {
                context.eof = true;
                break;
            }
            if (b >= 0 && b < DECODE_TABLE.length && (result = DECODE_TABLE[b]) >= 0) {
                context.modulus = (context.modulus + 1) % 4;
                context.ibitWorkArea = (context.ibitWorkArea << 6) + result;
                if (context.modulus == 0) {
                    buffer[context.pos++] = (byte)(context.ibitWorkArea >> 16 & 0xFF);
                    buffer[context.pos++] = (byte)(context.ibitWorkArea >> 8 & 0xFF);
                    buffer[context.pos++] = (byte)(context.ibitWorkArea & 0xFF);
                }
            }
            ++i;
        }
        if (context.eof && context.modulus != 0) {
            byte[] buffer = this.ensureBufferSize(this.decodeSize, context);
            switch (context.modulus) {
                case 1: {
                    break;
                }
                case 2: {
                    context.ibitWorkArea >>= 4;
                    buffer[context.pos++] = (byte)(context.ibitWorkArea & 0xFF);
                    break;
                }
                case 3: {
                    context.ibitWorkArea >>= 2;
                    buffer[context.pos++] = (byte)(context.ibitWorkArea >> 8 & 0xFF);
                    buffer[context.pos++] = (byte)(context.ibitWorkArea & 0xFF);
                    break;
                }
                default: {
                    throw new IllegalStateException("Impossible modulus " + context.modulus);
                }
            }
        }
    }

    @Deprecated
    public static boolean isArrayByteBase64(byte[] arrayOctet) {
        return Base64.isBase64(arrayOctet);
    }

    public static boolean isBase64(byte octet) {
        return octet == 61 || octet >= 0 && octet < DECODE_TABLE.length && DECODE_TABLE[octet] != -1;
    }

    public static boolean isBase64(String base64) {
        return Base64.isBase64(StringUtil.getBytesUtf8(base64));
    }

    public static boolean isBase64(byte[] arrayOctet) {
        int i = 0;
        while (i < arrayOctet.length) {
            if (!Base64.isBase64(arrayOctet[i]) && !Base64.isWhiteSpace(arrayOctet[i])) {
                return false;
            }
            ++i;
        }
        return true;
    }

    public static byte[] encodeBase64(byte[] binaryData) {
        return Base64.encodeBase64(binaryData, false);
    }

    public static String encodeBase64String(byte[] binaryData) {
        return StringUtil.newStringUtf8(Base64.encodeBase64(binaryData, false));
    }

    public static byte[] encodeBase64URLSafe(byte[] binaryData) {
        return Base64.encodeBase64(binaryData, false, true);
    }

    public static String encodeBase64URLSafeString(byte[] binaryData) {
        return StringUtil.newStringUtf8(Base64.encodeBase64(binaryData, false, true));
    }

    public static byte[] encodeBase64Chunked(byte[] binaryData) {
        return Base64.encodeBase64(binaryData, true);
    }

    public static byte[] encodeBase64(byte[] binaryData, boolean isChunked) {
        return Base64.encodeBase64(binaryData, isChunked, false);
    }

    public static byte[] encodeBase64(byte[] binaryData, boolean isChunked, boolean urlSafe) {
        return Base64.encodeBase64(binaryData, isChunked, urlSafe, Integer.MAX_VALUE);
    }

    public static byte[] encodeBase64(byte[] binaryData, boolean isChunked, boolean urlSafe, int maxResultSize) {
        if (binaryData == null || binaryData.length == 0) {
            return binaryData;
        }
        Base64 b64 = isChunked ? new Base64(urlSafe) : new Base64(0, CHUNK_SEPARATOR, urlSafe);
        long len = b64.getEncodedLength(binaryData);
        if (len > (long)maxResultSize) {
            throw new IllegalArgumentException("Input array too big, the output array would be bigger (" + len + ") than the specified maximum size of " + maxResultSize);
        }
        return b64.encode(binaryData);
    }

    public static byte[] decodeBase64(String base64String) {
        return new Base64().decode(base64String);
    }

    public static byte[] decodeBase64(byte[] base64Data) {
        return new Base64().decode(base64Data);
    }

    public static BigInteger decodeInteger(byte[] pArray) {
        return new BigInteger(1, Base64.decodeBase64(pArray));
    }

    public static byte[] encodeInteger(BigInteger bigInt) {
        if (bigInt == null) {
            throw new NullPointerException("encodeInteger called with null parameter");
        }
        return Base64.encodeBase64(Base64.toIntegerBytes(bigInt), false);
    }

    static byte[] toIntegerBytes(BigInteger bigInt) {
        int bitlen = bigInt.bitLength();
        bitlen = bitlen + 7 >> 3 << 3;
        byte[] bigBytes = bigInt.toByteArray();
        if (bigInt.bitLength() % 8 != 0 && bigInt.bitLength() / 8 + 1 == bitlen / 8) {
            return bigBytes;
        }
        int startSrc = 0;
        int len = bigBytes.length;
        if (bigInt.bitLength() % 8 == 0) {
            startSrc = 1;
            --len;
        }
        int startDst = bitlen / 8 - len;
        byte[] resizedBytes = new byte[bitlen / 8];
        System.arraycopy(bigBytes, startSrc, resizedBytes, startDst, len);
        return resizedBytes;
    }

    @Override
    protected boolean isInAlphabet(byte octet) {
        return octet >= 0 && octet < this.decodeTable.length && this.decodeTable[octet] != -1;
    }
}

