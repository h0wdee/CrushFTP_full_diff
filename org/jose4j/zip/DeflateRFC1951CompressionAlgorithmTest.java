/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.zip;

import junit.framework.TestCase;
import org.jose4j.base64url.Base64Url;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.StringUtil;
import org.jose4j.zip.DeflateRFC1951CompressionAlgorithm;

public class DeflateRFC1951CompressionAlgorithmTest
extends TestCase {
    public void testRoundTrip() throws JoseException {
        DeflateRFC1951CompressionAlgorithm ca;
        byte[] compressed;
        String dataString = "test test test test test test test test test test test test test test test test and stuff";
        byte[] data = StringUtil.getBytesUtf8(dataString);
        DeflateRFC1951CompressionAlgorithmTest.assertTrue((data.length > (compressed = (ca = new DeflateRFC1951CompressionAlgorithm()).compress(data)).length ? 1 : 0) != 0);
        byte[] decompress = ca.decompress(compressed);
        String decompressedString = StringUtil.newStringUtf8(decompress);
        DeflateRFC1951CompressionAlgorithmTest.assertEquals((String)dataString, (String)decompressedString);
    }

    public void testSomeDataCompressedElsewhere() throws JoseException {
        String s = "q1bKLC5WslLKKCkpKLaK0Y/Rz0wp0EutSMwtyEnVS87PVdLhUkqtKFCyMjQ2NTcyNTW3sACKJJamoGgqRujJL0oH6ckqyQSqKMmNLIsMCzWqsPAp8zM3cjINjHdNTPbQizd1BClKTC4CKjICMYtLk4BMp6LMxDylWi4A";
        byte[] decoded = Base64Url.decode(s);
        DeflateRFC1951CompressionAlgorithm ca = new DeflateRFC1951CompressionAlgorithm();
        byte[] decompress = ca.decompress(decoded);
        String decompedString = StringUtil.newStringUtf8(decompress);
        String expected = "{\"iss\":\"https:\\/\\/idp.example.com\",\n\"exp\":1357255788,\n\"aud\":\"https:\\/\\/sp.example.org\",\n\"jti\":\"tmYvYVU2x8LvN72B5Q_EacH._5A\",\n\"acr\":\"2\",\n\"sub\":\"Brian\"}\n";
        DeflateRFC1951CompressionAlgorithmTest.assertEquals((String)expected, (String)decompedString);
    }

    public void testSomeMoreDataCompressedElsewhere() throws JoseException {
        byte[] byArray = new byte[18];
        byArray[0] = -13;
        byArray[1] = 72;
        byArray[2] = -51;
        byArray[3] = -55;
        byArray[4] = -55;
        byArray[5] = 87;
        byArray[6] = 40;
        byArray[7] = -49;
        byArray[8] = 47;
        byArray[9] = -54;
        byArray[10] = 73;
        byArray[11] = 81;
        byArray[12] = 84;
        byArray[13] = -16;
        byArray[14] = -96;
        byArray[15] = 38;
        byArray[16] = 7;
        byte[] compressed = byArray;
        DeflateRFC1951CompressionAlgorithm ca = new DeflateRFC1951CompressionAlgorithm();
        byte[] decompress = ca.decompress(compressed);
        String decompedString = StringUtil.newStringUtf8(decompress);
        DeflateRFC1951CompressionAlgorithmTest.assertTrue((boolean)decompedString.contains("Hello world!"));
    }
}

