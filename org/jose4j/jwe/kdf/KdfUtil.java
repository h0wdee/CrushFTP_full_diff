/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe.kdf;

import org.jose4j.base64url.Base64Url;
import org.jose4j.jwe.kdf.ConcatKeyDerivationFunction;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.StringUtil;

public class KdfUtil {
    private Base64Url base64Url = new Base64Url();
    private ConcatKeyDerivationFunction kdf;

    public KdfUtil() {
        this.kdf = new ConcatKeyDerivationFunction("SHA-256");
    }

    public KdfUtil(String provider) {
        this.kdf = new ConcatKeyDerivationFunction("SHA-256", provider);
    }

    public byte[] kdf(byte[] sharedSecret, int keydatalen, String algorithmId, String partyUInfo, String partyVInfo) {
        byte[] algorithmIdBytes = this.prependDatalen(StringUtil.getBytesUtf8(algorithmId));
        byte[] partyUInfoBytes = this.getDatalenDataFormat(partyUInfo);
        byte[] partyVInfoBytes = this.getDatalenDataFormat(partyVInfo);
        byte[] suppPubInfo = ByteUtil.getBytes(keydatalen);
        byte[] suppPrivInfo = ByteUtil.EMPTY_BYTES;
        return this.kdf.kdf(sharedSecret, keydatalen, algorithmIdBytes, partyUInfoBytes, partyVInfoBytes, suppPubInfo, suppPrivInfo);
    }

    byte[] prependDatalen(byte[] data) {
        if (data == null) {
            data = ByteUtil.EMPTY_BYTES;
        }
        byte[] datalen = ByteUtil.getBytes(data.length);
        return ByteUtil.concat(datalen, data);
    }

    byte[] getDatalenDataFormat(String encodedValue) {
        byte[] data = this.base64Url.base64UrlDecode(encodedValue);
        return this.prependDatalen(data);
    }
}

