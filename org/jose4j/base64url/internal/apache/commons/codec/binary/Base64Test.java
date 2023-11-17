/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bouncycastle.util.encoders.Hex
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.base64url.internal.apache.commons.codec.binary;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import org.bouncycastle.util.encoders.Hex;
import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64TestData;
import org.jose4j.lang.StringUtil;
import org.junit.Assert;
import org.junit.Test;

public class Base64Test {
    private final Random random = new Random();

    public Random getRandom() {
        return this.random;
    }

    @Test
    public void testIsStringBase64() {
        String nullString = null;
        String emptyString = "";
        String validString = "abc===defg\n\r123456\r789\r\rABC\n\nDEF==GHI\r\nJKL==============";
        String invalidString = "abc===defg\n\r123456\r789\r\rABC\n\nDEF==GHI\r\nJKL==============\u0000";
        try {
            Base64.isBase64(nullString);
            Assert.fail((String)"Base64.isStringBase64() should not be null-safe.");
        }
        catch (NullPointerException npe) {
            Assert.assertNotNull((String)"Base64.isStringBase64() should not be null-safe.", (Object)npe);
        }
        Assert.assertTrue((String)"Base64.isStringBase64(empty-string) is true", (boolean)Base64.isBase64(""));
        Assert.assertTrue((String)"Base64.isStringBase64(valid-string) is true", (boolean)Base64.isBase64("abc===defg\n\r123456\r789\r\rABC\n\nDEF==GHI\r\nJKL=============="));
        Assert.assertFalse((String)"Base64.isStringBase64(invalid-string) is false", (boolean)Base64.isBase64("abc===defg\n\r123456\r789\r\rABC\n\nDEF==GHI\r\nJKL==============\u0000"));
    }

    @Test
    public void testBase64() {
        String content = "Hello World";
        byte[] encodedBytes = Base64.encodeBase64(StringUtil.getBytesUtf8("Hello World"));
        String encodedContent = StringUtil.newStringUtf8(encodedBytes);
        Assert.assertEquals((String)"encoding hello world", (Object)"SGVsbG8gV29ybGQ=", (Object)encodedContent);
        Base64 b64 = new Base64(76, null);
        encodedBytes = b64.encode(StringUtil.getBytesUtf8("Hello World"));
        encodedContent = StringUtil.newStringUtf8(encodedBytes);
        Assert.assertEquals((String)"encoding hello world", (Object)"SGVsbG8gV29ybGQ=", (Object)encodedContent);
        b64 = new Base64(0, null);
        encodedBytes = b64.encode(StringUtil.getBytesUtf8("Hello World"));
        encodedContent = StringUtil.newStringUtf8(encodedBytes);
        Assert.assertEquals((String)"encoding hello world", (Object)"SGVsbG8gV29ybGQ=", (Object)encodedContent);
        byte[] decode = b64.decode("SGVsbG{\u00e9\u00e9\u00e9\u00e9\u00e9\u00e9}8gV29ybGQ=");
        String decodeString = StringUtil.newStringUtf8(decode);
        Assert.assertEquals((String)"decode hello world", (Object)"Hello World", (Object)decodeString);
    }

    @Test
    public void testDecodeWithInnerPad() {
        String content = "SGVsbG8gV29ybGQ=SGVsbG8gV29ybGQ=";
        byte[] result = Base64.decodeBase64("SGVsbG8gV29ybGQ=SGVsbG8gV29ybGQ=");
        byte[] shouldBe = StringUtil.getBytesUtf8("Hello World");
        Assert.assertTrue((String)"decode should halt at pad (=)", (boolean)Arrays.equals(result, shouldBe));
    }

    @Test
    public void testChunkedEncodeMultipleOf76() {
        byte[] expectedEncode = Base64.encodeBase64(Base64TestData.DECODED, true);
        String actualResult = "9IPNKwUvdLiIAp6ctz12SiQmOGstWyYvSPeevufDhrzaws65voykKjbIj33YWTa9xA7c/FHypWcl\nrZhQ7onfc3JE93BJ5fT4R9zAEdjbjy1hv4ZYNnET4WJeXMLJ/5p+qBpTsPpepW8DNVYy1c02/1wy\nC+kgA6CvRUd9cSr/lt88AEdsTV4GMCn1+EwuAiYdivxuzn+cLM8q2jewqlI52tP9J7Cs8vqG71s6\n+WAELKvm/UovvyaOi+OdMUfjQ0JLiLkHu6p9OwUgvQqiDKzEv/Augo0dTPZzYGEyCP5GVrle3QQd\ngciIHnpdd4VUTPGRUbXeKbh++U3fbJIng/sQXM3IYByMZ7xt9HWS1LUcRdQ7Prwn/IlQWxOMeq+K\nZJSoAviWtdserXyHbIEa//hmr4p/j80k0g9q35hq1ayGM9984ALTSaZ8WeyFbZx1CxC/Qoqf92UH\n/ylBRnSJNn4sS0oa3uUbNvOnpkB4D9V7Ut9atinCJrw+wiJcMl+9kp251IUxBGA4cUxh0eaxk3OD\nWnwI95EktmWOKwCSP0xjWwIMxDjygwAG5R8fk9H9bVi1thMavm4nDc4vaNoSE1RnZNYwbiUVlVPM\n9EclvJWTWd6igWeA0MxHAA8iOM5Vnmqp/WGM7UDq59rBIdNQCoeTJaAkEtAuLL5zogOa5e+MzVjv\nB5MYQlOlaaTtQrRApXa5Z4VfEanu9UK2fi1T8jJPFC2PmXebxp0bnO+VW+bgyEdIIkIQCaZq1MKW\nC3KuiOS9BJ1t7O0A2JKJKvoE4UNulzV2TGCC+KAnmjRqQBqXlJmgjHQAoHNZKOma/uIQOsvfDnqi\ncYdDmfyCYuV89HjA1H8tiDJ85VfsrFHdcbPAoNCpi65awJSHfdPO1NDONOK++S7Y0VXUgoYYrBV4\nY7YbC8wg/nqcimr3lm3tRyp+QsgKzdREbfNRk0F5PLyLfsUElepjs1QdV3fEV1LJtiywA3ubVNQJ\nRxhbYxa/C/Xy2qxpm6vvdL92l3q1ccev35IcaOiSx7Im+/GxV2lVKdaOvYVGDD1zBRe6Y2CwQb9p\n088l3/93qGR5593NCiuPPWcsDWwUShM1EyW0FNX1F8bnzHnYijoyE/jf4s/l9bBd7yJdRWRCyih2\nWcypAiOIEkBsH+dCTgalu8sRDoMh4ZIBBdgHfoZUycLqReQFLZZ4Sl4zSmzt5vQxQFhEKb9+ff/4\nrb1KAo6wifengxVfIsa2b5ljXzAqXs7JkPvmC6fa7X4ZZndRokaxYlu3cg8OV+uG/6YAHZilo8at\n0OpkkNdNFuhwuGlkBqrZKNUj/gSiYYc06gF/r/z6iWAjpXJRW1qq3CLZXdZFZ/VrqXeVjtOAu2A=\n".replaceAll("\n", "\r\n");
        byte[] actualEncode = StringUtil.getBytesUtf8(actualResult);
        Assert.assertTrue((String)"chunkedEncodeMultipleOf76", (boolean)Arrays.equals(expectedEncode, actualEncode));
    }

    @Test
    public void testCodec68() {
        byte[] x = new byte[]{110, 65, 61, 61, -100};
        Base64.decodeBase64(x);
    }

    @Test
    public void testCodeInteger1() {
        String encodedInt1 = "li7dzDacuo67Jg7mtqEm2TRuOMU=";
        BigInteger bigInt1 = new BigInteger("857393771208094202104259627990318636601332086981");
        Assert.assertEquals((Object)"li7dzDacuo67Jg7mtqEm2TRuOMU=", (Object)new String(Base64.encodeInteger(bigInt1)));
        Assert.assertEquals((Object)bigInt1, (Object)Base64.decodeInteger("li7dzDacuo67Jg7mtqEm2TRuOMU=".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testCodeInteger2() {
        String encodedInt2 = "9B5ypLY9pMOmtxCeTDHgwdNFeGs=";
        BigInteger bigInt2 = new BigInteger("1393672757286116725466646726891466679477132949611");
        Assert.assertEquals((Object)"9B5ypLY9pMOmtxCeTDHgwdNFeGs=", (Object)new String(Base64.encodeInteger(bigInt2)));
        Assert.assertEquals((Object)bigInt2, (Object)Base64.decodeInteger("9B5ypLY9pMOmtxCeTDHgwdNFeGs=".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testCodeInteger3() {
        String encodedInt3 = "FKIhdgaG5LGKiEtF1vHy4f3y700zaD6QwDS3IrNVGzNp2rY+1LFWTK6D44AyiC1n8uWz1itkYMZF0/aKDK0Yjg==";
        BigInteger bigInt3 = new BigInteger("1080654815409387346195174854511969891364164488058190793635243098977490449581124171362405574495062430572478766856090958495998158114332651671116876320938126");
        Assert.assertEquals((Object)"FKIhdgaG5LGKiEtF1vHy4f3y700zaD6QwDS3IrNVGzNp2rY+1LFWTK6D44AyiC1n8uWz1itkYMZF0/aKDK0Yjg==", (Object)new String(Base64.encodeInteger(bigInt3)));
        Assert.assertEquals((Object)bigInt3, (Object)Base64.decodeInteger("FKIhdgaG5LGKiEtF1vHy4f3y700zaD6QwDS3IrNVGzNp2rY+1LFWTK6D44AyiC1n8uWz1itkYMZF0/aKDK0Yjg==".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testCodeInteger4() {
        String encodedInt4 = "ctA8YGxrtngg/zKVvqEOefnwmViFztcnPBYPlJsvh6yKI4iDm68fnp4Mi3RrJ6bZAygFrUIQLxLjV+OJtgJAEto0xAs+Mehuq1DkSFEpP3oDzCTOsrOiS1DwQe4oIb7zVk/9l7aPtJMHW0LVlMdwZNFNNJoqMcT2ZfCPrfvYvQ0=";
        BigInteger bigInt4 = new BigInteger("80624726256040348115552042320696813500187275370942441977258669395023235020055564647117594451929708788598704081077890850726227289270230377442285367559774800853404089092381420228663316324808605521697655145608801533888071381819208887705771753016938104409283940243801509765453542091716518238707344493641683483917");
        Assert.assertEquals((Object)"ctA8YGxrtngg/zKVvqEOefnwmViFztcnPBYPlJsvh6yKI4iDm68fnp4Mi3RrJ6bZAygFrUIQLxLjV+OJtgJAEto0xAs+Mehuq1DkSFEpP3oDzCTOsrOiS1DwQe4oIb7zVk/9l7aPtJMHW0LVlMdwZNFNNJoqMcT2ZfCPrfvYvQ0=", (Object)new String(Base64.encodeInteger(bigInt4)));
        Assert.assertEquals((Object)bigInt4, (Object)Base64.decodeInteger("ctA8YGxrtngg/zKVvqEOefnwmViFztcnPBYPlJsvh6yKI4iDm68fnp4Mi3RrJ6bZAygFrUIQLxLjV+OJtgJAEto0xAs+Mehuq1DkSFEpP3oDzCTOsrOiS1DwQe4oIb7zVk/9l7aPtJMHW0LVlMdwZNFNNJoqMcT2ZfCPrfvYvQ0=".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testCodeIntegerNull() {
        try {
            Base64.encodeInteger(null);
            Assert.fail((String)"Exception not thrown when passing in null to encodeInteger(BigInteger)");
        }
        catch (NullPointerException nullPointerException) {
        }
        catch (Exception e) {
            Assert.fail((String)"Incorrect Exception caught when passing in null to encodeInteger(BigInteger)");
        }
    }

    @Test
    public void testConstructors() {
        Base64 base64 = new Base64();
        base64 = new Base64(-1);
        base64 = new Base64(-1, new byte[0]);
        base64 = new Base64(64, new byte[0]);
        try {
            base64 = new Base64(-1, new byte[]{65});
            Assert.fail((String)"Should have rejected attempt to use 'A' as a line separator");
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        try {
            base64 = new Base64(64, new byte[]{65});
            Assert.fail((String)"Should have rejected attempt to use 'A' as a line separator");
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        try {
            base64 = new Base64(64, new byte[]{61});
            Assert.fail((String)"Should have rejected attempt to use '=' as a line separator");
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        base64 = new Base64(64, new byte[]{36});
        try {
            base64 = new Base64(64, new byte[]{65, 36});
            Assert.fail((String)"Should have rejected attempt to use 'A$' as a line separator");
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        base64 = new Base64(64, new byte[]{32, 36, 10, 13, 9});
        Assert.assertNotNull((Object)base64);
    }

    @Test
    public void testConstructor_Int_ByteArray_Boolean() {
        Base64 base64 = new Base64(65, new byte[]{9}, false);
        byte[] encoded = base64.encode(Base64TestData.DECODED);
        String expectedResult = "9IPNKwUvdLiIAp6ctz12SiQmOGstWyYvSPeevufDhrzaws65voykKjbIj33YWTa9\nxA7c/FHypWclrZhQ7onfc3JE93BJ5fT4R9zAEdjbjy1hv4ZYNnET4WJeXMLJ/5p+\nqBpTsPpepW8DNVYy1c02/1wyC+kgA6CvRUd9cSr/lt88AEdsTV4GMCn1+EwuAiYd\nivxuzn+cLM8q2jewqlI52tP9J7Cs8vqG71s6+WAELKvm/UovvyaOi+OdMUfjQ0JL\niLkHu6p9OwUgvQqiDKzEv/Augo0dTPZzYGEyCP5GVrle3QQdgciIHnpdd4VUTPGR\nUbXeKbh++U3fbJIng/sQXM3IYByMZ7xt9HWS1LUcRdQ7Prwn/IlQWxOMeq+KZJSo\nAviWtdserXyHbIEa//hmr4p/j80k0g9q35hq1ayGM9984ALTSaZ8WeyFbZx1CxC/\nQoqf92UH/ylBRnSJNn4sS0oa3uUbNvOnpkB4D9V7Ut9atinCJrw+wiJcMl+9kp25\n1IUxBGA4cUxh0eaxk3ODWnwI95EktmWOKwCSP0xjWwIMxDjygwAG5R8fk9H9bVi1\nthMavm4nDc4vaNoSE1RnZNYwbiUVlVPM9EclvJWTWd6igWeA0MxHAA8iOM5Vnmqp\n/WGM7UDq59rBIdNQCoeTJaAkEtAuLL5zogOa5e+MzVjvB5MYQlOlaaTtQrRApXa5\nZ4VfEanu9UK2fi1T8jJPFC2PmXebxp0bnO+VW+bgyEdIIkIQCaZq1MKWC3KuiOS9\nBJ1t7O0A2JKJKvoE4UNulzV2TGCC+KAnmjRqQBqXlJmgjHQAoHNZKOma/uIQOsvf\nDnqicYdDmfyCYuV89HjA1H8tiDJ85VfsrFHdcbPAoNCpi65awJSHfdPO1NDONOK+\n+S7Y0VXUgoYYrBV4Y7YbC8wg/nqcimr3lm3tRyp+QsgKzdREbfNRk0F5PLyLfsUE\nlepjs1QdV3fEV1LJtiywA3ubVNQJRxhbYxa/C/Xy2qxpm6vvdL92l3q1ccev35Ic\naOiSx7Im+/GxV2lVKdaOvYVGDD1zBRe6Y2CwQb9p088l3/93qGR5593NCiuPPWcs\nDWwUShM1EyW0FNX1F8bnzHnYijoyE/jf4s/l9bBd7yJdRWRCyih2WcypAiOIEkBs\nH+dCTgalu8sRDoMh4ZIBBdgHfoZUycLqReQFLZZ4Sl4zSmzt5vQxQFhEKb9+ff/4\nrb1KAo6wifengxVfIsa2b5ljXzAqXs7JkPvmC6fa7X4ZZndRokaxYlu3cg8OV+uG\n/6YAHZilo8at0OpkkNdNFuhwuGlkBqrZKNUj/gSiYYc06gF/r/z6iWAjpXJRW1qq\n3CLZXdZFZ/VrqXeVjtOAu2A=\n";
        expectedResult = expectedResult.replace('\n', '\t');
        String result = StringUtil.newStringUtf8(encoded);
        Assert.assertEquals((String)"new Base64(65, \\t, false)", (Object)expectedResult, (Object)result);
    }

    @Test
    public void testConstructor_Int_ByteArray_Boolean_UrlSafe() {
        Base64 base64 = new Base64(64, new byte[]{9}, true);
        byte[] encoded = base64.encode(Base64TestData.DECODED);
        String expectedResult = "9IPNKwUvdLiIAp6ctz12SiQmOGstWyYvSPeevufDhrzaws65voykKjbIj33YWTa9\nxA7c/FHypWclrZhQ7onfc3JE93BJ5fT4R9zAEdjbjy1hv4ZYNnET4WJeXMLJ/5p+\nqBpTsPpepW8DNVYy1c02/1wyC+kgA6CvRUd9cSr/lt88AEdsTV4GMCn1+EwuAiYd\nivxuzn+cLM8q2jewqlI52tP9J7Cs8vqG71s6+WAELKvm/UovvyaOi+OdMUfjQ0JL\niLkHu6p9OwUgvQqiDKzEv/Augo0dTPZzYGEyCP5GVrle3QQdgciIHnpdd4VUTPGR\nUbXeKbh++U3fbJIng/sQXM3IYByMZ7xt9HWS1LUcRdQ7Prwn/IlQWxOMeq+KZJSo\nAviWtdserXyHbIEa//hmr4p/j80k0g9q35hq1ayGM9984ALTSaZ8WeyFbZx1CxC/\nQoqf92UH/ylBRnSJNn4sS0oa3uUbNvOnpkB4D9V7Ut9atinCJrw+wiJcMl+9kp25\n1IUxBGA4cUxh0eaxk3ODWnwI95EktmWOKwCSP0xjWwIMxDjygwAG5R8fk9H9bVi1\nthMavm4nDc4vaNoSE1RnZNYwbiUVlVPM9EclvJWTWd6igWeA0MxHAA8iOM5Vnmqp\n/WGM7UDq59rBIdNQCoeTJaAkEtAuLL5zogOa5e+MzVjvB5MYQlOlaaTtQrRApXa5\nZ4VfEanu9UK2fi1T8jJPFC2PmXebxp0bnO+VW+bgyEdIIkIQCaZq1MKWC3KuiOS9\nBJ1t7O0A2JKJKvoE4UNulzV2TGCC+KAnmjRqQBqXlJmgjHQAoHNZKOma/uIQOsvf\nDnqicYdDmfyCYuV89HjA1H8tiDJ85VfsrFHdcbPAoNCpi65awJSHfdPO1NDONOK+\n+S7Y0VXUgoYYrBV4Y7YbC8wg/nqcimr3lm3tRyp+QsgKzdREbfNRk0F5PLyLfsUE\nlepjs1QdV3fEV1LJtiywA3ubVNQJRxhbYxa/C/Xy2qxpm6vvdL92l3q1ccev35Ic\naOiSx7Im+/GxV2lVKdaOvYVGDD1zBRe6Y2CwQb9p088l3/93qGR5593NCiuPPWcs\nDWwUShM1EyW0FNX1F8bnzHnYijoyE/jf4s/l9bBd7yJdRWRCyih2WcypAiOIEkBs\nH+dCTgalu8sRDoMh4ZIBBdgHfoZUycLqReQFLZZ4Sl4zSmzt5vQxQFhEKb9+ff/4\nrb1KAo6wifengxVfIsa2b5ljXzAqXs7JkPvmC6fa7X4ZZndRokaxYlu3cg8OV+uG\n/6YAHZilo8at0OpkkNdNFuhwuGlkBqrZKNUj/gSiYYc06gF/r/z6iWAjpXJRW1qq\n3CLZXdZFZ/VrqXeVjtOAu2A=\n";
        expectedResult = expectedResult.replaceAll("=", "");
        expectedResult = expectedResult.replace('\n', '\t');
        expectedResult = expectedResult.replace('+', '-');
        expectedResult = expectedResult.replace('/', '_');
        String result = StringUtil.newStringUtf8(encoded);
        Assert.assertEquals((String)"new Base64(64, \\t, true)", (Object)result, (Object)expectedResult);
    }

    @Test
    public void testDecodePadMarkerIndex2() {
        Assert.assertEquals((Object)"A", (Object)new String(Base64.decodeBase64("QQ==".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void testDecodePadMarkerIndex3() {
        Assert.assertEquals((Object)"AA", (Object)new String(Base64.decodeBase64("QUE=".getBytes(StandardCharsets.UTF_8))));
        Assert.assertEquals((Object)"AAA", (Object)new String(Base64.decodeBase64("QUFB".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void testDecodePadOnly() {
        Assert.assertEquals((long)0L, (long)Base64.decodeBase64("====".getBytes(StandardCharsets.UTF_8)).length);
        Assert.assertEquals((Object)"", (Object)new String(Base64.decodeBase64("====".getBytes(StandardCharsets.UTF_8))));
        Assert.assertEquals((long)0L, (long)Base64.decodeBase64("===".getBytes(StandardCharsets.UTF_8)).length);
        Assert.assertEquals((long)0L, (long)Base64.decodeBase64("==".getBytes(StandardCharsets.UTF_8)).length);
        Assert.assertEquals((long)0L, (long)Base64.decodeBase64("=".getBytes(StandardCharsets.UTF_8)).length);
        Assert.assertEquals((long)0L, (long)Base64.decodeBase64("".getBytes(StandardCharsets.UTF_8)).length);
    }

    @Test
    public void testDecodePadOnlyChunked() {
        Assert.assertEquals((long)0L, (long)Base64.decodeBase64("====\n".getBytes(StandardCharsets.UTF_8)).length);
        Assert.assertEquals((Object)"", (Object)new String(Base64.decodeBase64("====\n".getBytes(StandardCharsets.UTF_8))));
        Assert.assertEquals((long)0L, (long)Base64.decodeBase64("===\n".getBytes(StandardCharsets.UTF_8)).length);
        Assert.assertEquals((long)0L, (long)Base64.decodeBase64("==\n".getBytes(StandardCharsets.UTF_8)).length);
        Assert.assertEquals((long)0L, (long)Base64.decodeBase64("=\n".getBytes(StandardCharsets.UTF_8)).length);
        Assert.assertEquals((long)0L, (long)Base64.decodeBase64("\n".getBytes(StandardCharsets.UTF_8)).length);
    }

    @Test
    public void testDecodeWithWhitespace() throws Exception {
        String orig = "I am a late night coder.";
        byte[] encodedArray = Base64.encodeBase64("I am a late night coder.".getBytes(StandardCharsets.UTF_8));
        StringBuilder intermediate = new StringBuilder(new String(encodedArray));
        intermediate.insert(2, ' ');
        intermediate.insert(5, '\t');
        intermediate.insert(10, '\r');
        intermediate.insert(15, '\n');
        byte[] encodedWithWS = intermediate.toString().getBytes(StandardCharsets.UTF_8);
        byte[] decodedWithWS = Base64.decodeBase64(encodedWithWS);
        String dest = new String(decodedWithWS);
        Assert.assertEquals((String)"Dest string doesn't equal the original", (Object)"I am a late night coder.", (Object)dest);
    }

    @Test
    public void testEmptyBase64() {
        byte[] empty = new byte[]{};
        byte[] result = Base64.encodeBase64(empty);
        Assert.assertEquals((String)"empty base64 encode", (long)0L, (long)result.length);
        Assert.assertEquals((String)"empty base64 encode", null, (Object)Base64.encodeBase64(null));
        empty = new byte[]{};
        result = Base64.decodeBase64(empty);
        Assert.assertEquals((String)"empty base64 decode", (long)0L, (long)result.length);
        Assert.assertEquals((String)"empty base64 encode", null, (Object)Base64.decodeBase64(null));
    }

    @Test
    public void testEncodeDecodeRandom() {
        int i = 1;
        while (i < 5) {
            byte[] data = new byte[this.getRandom().nextInt(10000) + 1];
            this.getRandom().nextBytes(data);
            byte[] enc = Base64.encodeBase64(data);
            Assert.assertTrue((boolean)Base64.isBase64(enc));
            byte[] data2 = Base64.decodeBase64(enc);
            Assert.assertTrue((boolean)Arrays.equals(data, data2));
            ++i;
        }
    }

    @Test
    public void testEncodeDecodeSmall() {
        int i = 0;
        while (i < 12) {
            byte[] data = new byte[i];
            this.getRandom().nextBytes(data);
            byte[] enc = Base64.encodeBase64(data);
            Assert.assertTrue((String)("\"" + new String(enc) + "\" is Base64 data."), (boolean)Base64.isBase64(enc));
            byte[] data2 = Base64.decodeBase64(enc);
            Assert.assertTrue((String)(String.valueOf(this.toString(data)) + " equals " + this.toString(data2)), (boolean)Arrays.equals(data, data2));
            ++i;
        }
    }

    @Test
    public void testEncodeOverMaxSize() throws Exception {
        this.testEncodeOverMaxSize(-1);
        this.testEncodeOverMaxSize(0);
        this.testEncodeOverMaxSize(1);
        this.testEncodeOverMaxSize(2);
    }

    @Test
    public void testCodec112() {
        byte[] in = new byte[1];
        byte[] out = Base64.encodeBase64(in);
        Base64.encodeBase64(in, false, false, out.length);
    }

    private void testEncodeOverMaxSize(int maxSize) throws Exception {
        try {
            Base64.encodeBase64(Base64TestData.DECODED, true, false, maxSize);
            Assert.fail((String)("Expected " + IllegalArgumentException.class.getName()));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
    }

    @Test
    public void testIgnoringNonBase64InDecode() throws Exception {
        Assert.assertEquals((Object)"The quick brown fox jumped over the lazy dogs.", (Object)new String(Base64.decodeBase64("VGhlIH@$#$@%F1aWN@#@#@@rIGJyb3duIGZve\n\r\t%#%#%#%CBqd##$#$W1wZWQgb3ZlciB0aGUgbGF6eSBkb2dzLg==".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void testIsArrayByteBase64() {
        Assert.assertFalse((boolean)Base64.isBase64(new byte[]{-128}));
        Assert.assertFalse((boolean)Base64.isBase64(new byte[]{-125}));
        Assert.assertFalse((boolean)Base64.isBase64(new byte[]{-10}));
        Assert.assertFalse((boolean)Base64.isBase64(new byte[1]));
        Assert.assertFalse((boolean)Base64.isBase64(new byte[]{64, 127}));
        Assert.assertFalse((boolean)Base64.isBase64(new byte[]{127}));
        Assert.assertTrue((boolean)Base64.isBase64(new byte[]{65}));
        Assert.assertFalse((boolean)Base64.isBase64(new byte[]{65, -128}));
        Assert.assertTrue((boolean)Base64.isBase64(new byte[]{65, 90, 97}));
        Assert.assertTrue((boolean)Base64.isBase64(new byte[]{47, 61, 43}));
        Assert.assertFalse((boolean)Base64.isBase64(new byte[]{36}));
    }

    @Test
    public void testIsUrlSafe() {
        Base64 base64Standard = new Base64(false);
        Base64 base64URLSafe = new Base64(true);
        Assert.assertFalse((String)"Base64.isUrlSafe=false", (boolean)base64Standard.isUrlSafe());
        Assert.assertTrue((String)"Base64.isUrlSafe=true", (boolean)base64URLSafe.isUrlSafe());
        byte[] whiteSpace = new byte[]{32, 10, 13, 9};
        Assert.assertTrue((String)"Base64.isBase64(whiteSpace)=true", (boolean)Base64.isBase64(whiteSpace));
    }

    @Test
    public void testKnownDecodings() {
        Assert.assertEquals((Object)"The quick brown fox jumped over the lazy dogs.", (Object)new String(Base64.decodeBase64("VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wZWQgb3ZlciB0aGUgbGF6eSBkb2dzLg==".getBytes(StandardCharsets.UTF_8))));
        Assert.assertEquals((Object)"It was the best of times, it was the worst of times.", (Object)new String(Base64.decodeBase64("SXQgd2FzIHRoZSBiZXN0IG9mIHRpbWVzLCBpdCB3YXMgdGhlIHdvcnN0IG9mIHRpbWVzLg==".getBytes(StandardCharsets.UTF_8))));
        Assert.assertEquals((Object)"http://jakarta.apache.org/commmons", (Object)new String(Base64.decodeBase64("aHR0cDovL2pha2FydGEuYXBhY2hlLm9yZy9jb21tbW9ucw==".getBytes(StandardCharsets.UTF_8))));
        Assert.assertEquals((Object)"AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz", (Object)new String(Base64.decodeBase64("QWFCYkNjRGRFZUZmR2dIaElpSmpLa0xsTW1Obk9vUHBRcVJyU3NUdFV1VnZXd1h4WXlaeg==".getBytes(StandardCharsets.UTF_8))));
        Assert.assertEquals((Object)"{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }", (Object)new String(Base64.decodeBase64("eyAwLCAxLCAyLCAzLCA0LCA1LCA2LCA3LCA4LCA5IH0=".getBytes(StandardCharsets.UTF_8))));
        Assert.assertEquals((Object)"xyzzy!", (Object)new String(Base64.decodeBase64("eHl6enkh".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void testKnownEncodings() {
        Assert.assertEquals((Object)"VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wZWQgb3ZlciB0aGUgbGF6eSBkb2dzLg==", (Object)new String(Base64.encodeBase64("The quick brown fox jumped over the lazy dogs.".getBytes(StandardCharsets.UTF_8))));
        Assert.assertEquals((Object)"YmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJs\r\nYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFo\r\nIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBibGFoIGJsYWggYmxhaCBi\r\nbGFoIGJsYWg=\r\n", (Object)new String(Base64.encodeBase64Chunked("blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah".getBytes(StandardCharsets.UTF_8))));
        Assert.assertEquals((Object)"SXQgd2FzIHRoZSBiZXN0IG9mIHRpbWVzLCBpdCB3YXMgdGhlIHdvcnN0IG9mIHRpbWVzLg==", (Object)new String(Base64.encodeBase64("It was the best of times, it was the worst of times.".getBytes(StandardCharsets.UTF_8))));
        Assert.assertEquals((Object)"aHR0cDovL2pha2FydGEuYXBhY2hlLm9yZy9jb21tbW9ucw==", (Object)new String(Base64.encodeBase64("http://jakarta.apache.org/commmons".getBytes(StandardCharsets.UTF_8))));
        Assert.assertEquals((Object)"QWFCYkNjRGRFZUZmR2dIaElpSmpLa0xsTW1Obk9vUHBRcVJyU3NUdFV1VnZXd1h4WXlaeg==", (Object)new String(Base64.encodeBase64("AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz".getBytes(StandardCharsets.UTF_8))));
        Assert.assertEquals((Object)"eyAwLCAxLCAyLCAzLCA0LCA1LCA2LCA3LCA4LCA5IH0=", (Object)new String(Base64.encodeBase64("{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }".getBytes(StandardCharsets.UTF_8))));
        Assert.assertEquals((Object)"eHl6enkh", (Object)new String(Base64.encodeBase64("xyzzy!".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void testNonBase64Test() throws Exception {
        byte[] bArray = new byte[]{37};
        Assert.assertFalse((String)"Invalid Base64 array was incorrectly validated as an array of Base64 encoded data", (boolean)Base64.isBase64(bArray));
        try {
            Base64 b64 = new Base64();
            byte[] result = b64.decode(bArray);
            Assert.assertEquals((String)"The result should be empty as the test encoded content did not contain any valid base 64 characters", (long)0L, (long)result.length);
        }
        catch (Exception e) {
            Assert.fail((String)"Exception was thrown when trying to decode invalid base64 encoded data - RFC 2045 requires that all non base64 character be discarded, an exception should not have been thrown");
        }
    }

    @Test
    public void testObjectEncode() throws Exception {
        Base64 b64 = new Base64();
        Assert.assertEquals((Object)"SGVsbG8gV29ybGQ=", (Object)new String(b64.encode("Hello World".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void testPairs() {
        Assert.assertEquals((Object)"AAA=", (Object)new String(Base64.encodeBase64(new byte[2])));
        int i = -128;
        while (i <= 127) {
            byte[] test = new byte[]{(byte)i, (byte)i};
            Assert.assertTrue((boolean)Arrays.equals(test, Base64.decodeBase64(Base64.encodeBase64(test))));
            ++i;
        }
    }

    @Test
    public void testRfc2045Section2Dot1CrLfDefinition() {
        Assert.assertTrue((boolean)Arrays.equals(new byte[]{13, 10}, Base64.CHUNK_SEPARATOR));
    }

    @Test
    public void testRfc2045Section6Dot8ChunkSizeDefinition() {
        Assert.assertEquals((long)76L, (long)76L);
    }

    @Test
    public void testRfc1421Section6Dot8ChunkSizeDefinition() {
        Assert.assertEquals((long)64L, (long)64L);
    }

    @Test
    public void testRfc4648Section10Decode() {
        Assert.assertEquals((Object)"", (Object)StringUtil.newStringUsAscii(Base64.decodeBase64("")));
        Assert.assertEquals((Object)"f", (Object)StringUtil.newStringUsAscii(Base64.decodeBase64("Zg==")));
        Assert.assertEquals((Object)"fo", (Object)StringUtil.newStringUsAscii(Base64.decodeBase64("Zm8=")));
        Assert.assertEquals((Object)"foo", (Object)StringUtil.newStringUsAscii(Base64.decodeBase64("Zm9v")));
        Assert.assertEquals((Object)"foob", (Object)StringUtil.newStringUsAscii(Base64.decodeBase64("Zm9vYg==")));
        Assert.assertEquals((Object)"fooba", (Object)StringUtil.newStringUsAscii(Base64.decodeBase64("Zm9vYmE=")));
        Assert.assertEquals((Object)"foobar", (Object)StringUtil.newStringUsAscii(Base64.decodeBase64("Zm9vYmFy")));
    }

    @Test
    public void testRfc4648Section10DecodeWithCrLf() {
        String CRLF = StringUtil.newStringUsAscii(Base64.CHUNK_SEPARATOR);
        Assert.assertEquals((Object)"", (Object)StringUtil.newStringUsAscii(Base64.decodeBase64(CRLF)));
        Assert.assertEquals((Object)"f", (Object)StringUtil.newStringUsAscii(Base64.decodeBase64("Zg==" + CRLF)));
        Assert.assertEquals((Object)"fo", (Object)StringUtil.newStringUsAscii(Base64.decodeBase64("Zm8=" + CRLF)));
        Assert.assertEquals((Object)"foo", (Object)StringUtil.newStringUsAscii(Base64.decodeBase64("Zm9v" + CRLF)));
        Assert.assertEquals((Object)"foob", (Object)StringUtil.newStringUsAscii(Base64.decodeBase64("Zm9vYg==" + CRLF)));
        Assert.assertEquals((Object)"fooba", (Object)StringUtil.newStringUsAscii(Base64.decodeBase64("Zm9vYmE=" + CRLF)));
        Assert.assertEquals((Object)"foobar", (Object)StringUtil.newStringUsAscii(Base64.decodeBase64("Zm9vYmFy" + CRLF)));
    }

    @Test
    public void testRfc4648Section10Encode() {
        Assert.assertEquals((Object)"", (Object)Base64.encodeBase64String(StringUtil.getBytesUtf8("")));
        Assert.assertEquals((Object)"Zg==", (Object)Base64.encodeBase64String(StringUtil.getBytesUtf8("f")));
        Assert.assertEquals((Object)"Zm8=", (Object)Base64.encodeBase64String(StringUtil.getBytesUtf8("fo")));
        Assert.assertEquals((Object)"Zm9v", (Object)Base64.encodeBase64String(StringUtil.getBytesUtf8("foo")));
        Assert.assertEquals((Object)"Zm9vYg==", (Object)Base64.encodeBase64String(StringUtil.getBytesUtf8("foob")));
        Assert.assertEquals((Object)"Zm9vYmE=", (Object)Base64.encodeBase64String(StringUtil.getBytesUtf8("fooba")));
        Assert.assertEquals((Object)"Zm9vYmFy", (Object)Base64.encodeBase64String(StringUtil.getBytesUtf8("foobar")));
    }

    @Test
    public void testRfc4648Section10DecodeEncode() {
        this.testDecodeEncode("");
        this.testDecodeEncode("Zg==");
        this.testDecodeEncode("Zm8=");
        this.testDecodeEncode("Zm9v");
        this.testDecodeEncode("Zm9vYg==");
        this.testDecodeEncode("Zm9vYmE=");
        this.testDecodeEncode("Zm9vYmFy");
    }

    private void testDecodeEncode(String encodedText) {
        String decodedText = StringUtil.newStringUsAscii(Base64.decodeBase64(encodedText));
        String encodedText2 = Base64.encodeBase64String(StringUtil.getBytesUtf8(decodedText));
        Assert.assertEquals((Object)encodedText, (Object)encodedText2);
    }

    @Test
    public void testRfc4648Section10EncodeDecode() {
        this.testEncodeDecode("");
        this.testEncodeDecode("f");
        this.testEncodeDecode("fo");
        this.testEncodeDecode("foo");
        this.testEncodeDecode("foob");
        this.testEncodeDecode("fooba");
        this.testEncodeDecode("foobar");
    }

    private void testEncodeDecode(String plainText) {
        String encodedText = Base64.encodeBase64String(StringUtil.getBytesUtf8(plainText));
        String decodedText = StringUtil.newStringUsAscii(Base64.decodeBase64(encodedText));
        Assert.assertEquals((Object)plainText, (Object)decodedText);
    }

    @Test
    public void testSingletons() {
        Assert.assertEquals((Object)"AA==", (Object)new String(Base64.encodeBase64(new byte[1])));
        Assert.assertEquals((Object)"AQ==", (Object)new String(Base64.encodeBase64(new byte[]{1})));
        Assert.assertEquals((Object)"Ag==", (Object)new String(Base64.encodeBase64(new byte[]{2})));
        Assert.assertEquals((Object)"Aw==", (Object)new String(Base64.encodeBase64(new byte[]{3})));
        Assert.assertEquals((Object)"BA==", (Object)new String(Base64.encodeBase64(new byte[]{4})));
        Assert.assertEquals((Object)"BQ==", (Object)new String(Base64.encodeBase64(new byte[]{5})));
        Assert.assertEquals((Object)"Bg==", (Object)new String(Base64.encodeBase64(new byte[]{6})));
        Assert.assertEquals((Object)"Bw==", (Object)new String(Base64.encodeBase64(new byte[]{7})));
        Assert.assertEquals((Object)"CA==", (Object)new String(Base64.encodeBase64(new byte[]{8})));
        Assert.assertEquals((Object)"CQ==", (Object)new String(Base64.encodeBase64(new byte[]{9})));
        Assert.assertEquals((Object)"Cg==", (Object)new String(Base64.encodeBase64(new byte[]{10})));
        Assert.assertEquals((Object)"Cw==", (Object)new String(Base64.encodeBase64(new byte[]{11})));
        Assert.assertEquals((Object)"DA==", (Object)new String(Base64.encodeBase64(new byte[]{12})));
        Assert.assertEquals((Object)"DQ==", (Object)new String(Base64.encodeBase64(new byte[]{13})));
        Assert.assertEquals((Object)"Dg==", (Object)new String(Base64.encodeBase64(new byte[]{14})));
        Assert.assertEquals((Object)"Dw==", (Object)new String(Base64.encodeBase64(new byte[]{15})));
        Assert.assertEquals((Object)"EA==", (Object)new String(Base64.encodeBase64(new byte[]{16})));
        Assert.assertEquals((Object)"EQ==", (Object)new String(Base64.encodeBase64(new byte[]{17})));
        Assert.assertEquals((Object)"Eg==", (Object)new String(Base64.encodeBase64(new byte[]{18})));
        Assert.assertEquals((Object)"Ew==", (Object)new String(Base64.encodeBase64(new byte[]{19})));
        Assert.assertEquals((Object)"FA==", (Object)new String(Base64.encodeBase64(new byte[]{20})));
        Assert.assertEquals((Object)"FQ==", (Object)new String(Base64.encodeBase64(new byte[]{21})));
        Assert.assertEquals((Object)"Fg==", (Object)new String(Base64.encodeBase64(new byte[]{22})));
        Assert.assertEquals((Object)"Fw==", (Object)new String(Base64.encodeBase64(new byte[]{23})));
        Assert.assertEquals((Object)"GA==", (Object)new String(Base64.encodeBase64(new byte[]{24})));
        Assert.assertEquals((Object)"GQ==", (Object)new String(Base64.encodeBase64(new byte[]{25})));
        Assert.assertEquals((Object)"Gg==", (Object)new String(Base64.encodeBase64(new byte[]{26})));
        Assert.assertEquals((Object)"Gw==", (Object)new String(Base64.encodeBase64(new byte[]{27})));
        Assert.assertEquals((Object)"HA==", (Object)new String(Base64.encodeBase64(new byte[]{28})));
        Assert.assertEquals((Object)"HQ==", (Object)new String(Base64.encodeBase64(new byte[]{29})));
        Assert.assertEquals((Object)"Hg==", (Object)new String(Base64.encodeBase64(new byte[]{30})));
        Assert.assertEquals((Object)"Hw==", (Object)new String(Base64.encodeBase64(new byte[]{31})));
        Assert.assertEquals((Object)"IA==", (Object)new String(Base64.encodeBase64(new byte[]{32})));
        Assert.assertEquals((Object)"IQ==", (Object)new String(Base64.encodeBase64(new byte[]{33})));
        Assert.assertEquals((Object)"Ig==", (Object)new String(Base64.encodeBase64(new byte[]{34})));
        Assert.assertEquals((Object)"Iw==", (Object)new String(Base64.encodeBase64(new byte[]{35})));
        Assert.assertEquals((Object)"JA==", (Object)new String(Base64.encodeBase64(new byte[]{36})));
        Assert.assertEquals((Object)"JQ==", (Object)new String(Base64.encodeBase64(new byte[]{37})));
        Assert.assertEquals((Object)"Jg==", (Object)new String(Base64.encodeBase64(new byte[]{38})));
        Assert.assertEquals((Object)"Jw==", (Object)new String(Base64.encodeBase64(new byte[]{39})));
        Assert.assertEquals((Object)"KA==", (Object)new String(Base64.encodeBase64(new byte[]{40})));
        Assert.assertEquals((Object)"KQ==", (Object)new String(Base64.encodeBase64(new byte[]{41})));
        Assert.assertEquals((Object)"Kg==", (Object)new String(Base64.encodeBase64(new byte[]{42})));
        Assert.assertEquals((Object)"Kw==", (Object)new String(Base64.encodeBase64(new byte[]{43})));
        Assert.assertEquals((Object)"LA==", (Object)new String(Base64.encodeBase64(new byte[]{44})));
        Assert.assertEquals((Object)"LQ==", (Object)new String(Base64.encodeBase64(new byte[]{45})));
        Assert.assertEquals((Object)"Lg==", (Object)new String(Base64.encodeBase64(new byte[]{46})));
        Assert.assertEquals((Object)"Lw==", (Object)new String(Base64.encodeBase64(new byte[]{47})));
        Assert.assertEquals((Object)"MA==", (Object)new String(Base64.encodeBase64(new byte[]{48})));
        Assert.assertEquals((Object)"MQ==", (Object)new String(Base64.encodeBase64(new byte[]{49})));
        Assert.assertEquals((Object)"Mg==", (Object)new String(Base64.encodeBase64(new byte[]{50})));
        Assert.assertEquals((Object)"Mw==", (Object)new String(Base64.encodeBase64(new byte[]{51})));
        Assert.assertEquals((Object)"NA==", (Object)new String(Base64.encodeBase64(new byte[]{52})));
        Assert.assertEquals((Object)"NQ==", (Object)new String(Base64.encodeBase64(new byte[]{53})));
        Assert.assertEquals((Object)"Ng==", (Object)new String(Base64.encodeBase64(new byte[]{54})));
        Assert.assertEquals((Object)"Nw==", (Object)new String(Base64.encodeBase64(new byte[]{55})));
        Assert.assertEquals((Object)"OA==", (Object)new String(Base64.encodeBase64(new byte[]{56})));
        Assert.assertEquals((Object)"OQ==", (Object)new String(Base64.encodeBase64(new byte[]{57})));
        Assert.assertEquals((Object)"Og==", (Object)new String(Base64.encodeBase64(new byte[]{58})));
        Assert.assertEquals((Object)"Ow==", (Object)new String(Base64.encodeBase64(new byte[]{59})));
        Assert.assertEquals((Object)"PA==", (Object)new String(Base64.encodeBase64(new byte[]{60})));
        Assert.assertEquals((Object)"PQ==", (Object)new String(Base64.encodeBase64(new byte[]{61})));
        Assert.assertEquals((Object)"Pg==", (Object)new String(Base64.encodeBase64(new byte[]{62})));
        Assert.assertEquals((Object)"Pw==", (Object)new String(Base64.encodeBase64(new byte[]{63})));
        Assert.assertEquals((Object)"QA==", (Object)new String(Base64.encodeBase64(new byte[]{64})));
        Assert.assertEquals((Object)"QQ==", (Object)new String(Base64.encodeBase64(new byte[]{65})));
        Assert.assertEquals((Object)"Qg==", (Object)new String(Base64.encodeBase64(new byte[]{66})));
        Assert.assertEquals((Object)"Qw==", (Object)new String(Base64.encodeBase64(new byte[]{67})));
        Assert.assertEquals((Object)"RA==", (Object)new String(Base64.encodeBase64(new byte[]{68})));
        Assert.assertEquals((Object)"RQ==", (Object)new String(Base64.encodeBase64(new byte[]{69})));
        Assert.assertEquals((Object)"Rg==", (Object)new String(Base64.encodeBase64(new byte[]{70})));
        Assert.assertEquals((Object)"Rw==", (Object)new String(Base64.encodeBase64(new byte[]{71})));
        Assert.assertEquals((Object)"SA==", (Object)new String(Base64.encodeBase64(new byte[]{72})));
        Assert.assertEquals((Object)"SQ==", (Object)new String(Base64.encodeBase64(new byte[]{73})));
        Assert.assertEquals((Object)"Sg==", (Object)new String(Base64.encodeBase64(new byte[]{74})));
        Assert.assertEquals((Object)"Sw==", (Object)new String(Base64.encodeBase64(new byte[]{75})));
        Assert.assertEquals((Object)"TA==", (Object)new String(Base64.encodeBase64(new byte[]{76})));
        Assert.assertEquals((Object)"TQ==", (Object)new String(Base64.encodeBase64(new byte[]{77})));
        Assert.assertEquals((Object)"Tg==", (Object)new String(Base64.encodeBase64(new byte[]{78})));
        Assert.assertEquals((Object)"Tw==", (Object)new String(Base64.encodeBase64(new byte[]{79})));
        Assert.assertEquals((Object)"UA==", (Object)new String(Base64.encodeBase64(new byte[]{80})));
        Assert.assertEquals((Object)"UQ==", (Object)new String(Base64.encodeBase64(new byte[]{81})));
        Assert.assertEquals((Object)"Ug==", (Object)new String(Base64.encodeBase64(new byte[]{82})));
        Assert.assertEquals((Object)"Uw==", (Object)new String(Base64.encodeBase64(new byte[]{83})));
        Assert.assertEquals((Object)"VA==", (Object)new String(Base64.encodeBase64(new byte[]{84})));
        Assert.assertEquals((Object)"VQ==", (Object)new String(Base64.encodeBase64(new byte[]{85})));
        Assert.assertEquals((Object)"Vg==", (Object)new String(Base64.encodeBase64(new byte[]{86})));
        Assert.assertEquals((Object)"Vw==", (Object)new String(Base64.encodeBase64(new byte[]{87})));
        Assert.assertEquals((Object)"WA==", (Object)new String(Base64.encodeBase64(new byte[]{88})));
        Assert.assertEquals((Object)"WQ==", (Object)new String(Base64.encodeBase64(new byte[]{89})));
        Assert.assertEquals((Object)"Wg==", (Object)new String(Base64.encodeBase64(new byte[]{90})));
        Assert.assertEquals((Object)"Ww==", (Object)new String(Base64.encodeBase64(new byte[]{91})));
        Assert.assertEquals((Object)"XA==", (Object)new String(Base64.encodeBase64(new byte[]{92})));
        Assert.assertEquals((Object)"XQ==", (Object)new String(Base64.encodeBase64(new byte[]{93})));
        Assert.assertEquals((Object)"Xg==", (Object)new String(Base64.encodeBase64(new byte[]{94})));
        Assert.assertEquals((Object)"Xw==", (Object)new String(Base64.encodeBase64(new byte[]{95})));
        Assert.assertEquals((Object)"YA==", (Object)new String(Base64.encodeBase64(new byte[]{96})));
        Assert.assertEquals((Object)"YQ==", (Object)new String(Base64.encodeBase64(new byte[]{97})));
        Assert.assertEquals((Object)"Yg==", (Object)new String(Base64.encodeBase64(new byte[]{98})));
        Assert.assertEquals((Object)"Yw==", (Object)new String(Base64.encodeBase64(new byte[]{99})));
        Assert.assertEquals((Object)"ZA==", (Object)new String(Base64.encodeBase64(new byte[]{100})));
        Assert.assertEquals((Object)"ZQ==", (Object)new String(Base64.encodeBase64(new byte[]{101})));
        Assert.assertEquals((Object)"Zg==", (Object)new String(Base64.encodeBase64(new byte[]{102})));
        Assert.assertEquals((Object)"Zw==", (Object)new String(Base64.encodeBase64(new byte[]{103})));
        Assert.assertEquals((Object)"aA==", (Object)new String(Base64.encodeBase64(new byte[]{104})));
        int i = -128;
        while (i <= 127) {
            byte[] test = new byte[]{(byte)i};
            Assert.assertTrue((boolean)Arrays.equals(test, Base64.decodeBase64(Base64.encodeBase64(test))));
            ++i;
        }
    }

    @Test
    public void testSingletonsChunked() {
        Assert.assertEquals((Object)"AA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[1])));
        Assert.assertEquals((Object)"AQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{1})));
        Assert.assertEquals((Object)"Ag==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{2})));
        Assert.assertEquals((Object)"Aw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{3})));
        Assert.assertEquals((Object)"BA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{4})));
        Assert.assertEquals((Object)"BQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{5})));
        Assert.assertEquals((Object)"Bg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{6})));
        Assert.assertEquals((Object)"Bw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{7})));
        Assert.assertEquals((Object)"CA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{8})));
        Assert.assertEquals((Object)"CQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{9})));
        Assert.assertEquals((Object)"Cg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{10})));
        Assert.assertEquals((Object)"Cw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{11})));
        Assert.assertEquals((Object)"DA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{12})));
        Assert.assertEquals((Object)"DQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{13})));
        Assert.assertEquals((Object)"Dg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{14})));
        Assert.assertEquals((Object)"Dw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{15})));
        Assert.assertEquals((Object)"EA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{16})));
        Assert.assertEquals((Object)"EQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{17})));
        Assert.assertEquals((Object)"Eg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{18})));
        Assert.assertEquals((Object)"Ew==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{19})));
        Assert.assertEquals((Object)"FA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{20})));
        Assert.assertEquals((Object)"FQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{21})));
        Assert.assertEquals((Object)"Fg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{22})));
        Assert.assertEquals((Object)"Fw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{23})));
        Assert.assertEquals((Object)"GA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{24})));
        Assert.assertEquals((Object)"GQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{25})));
        Assert.assertEquals((Object)"Gg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{26})));
        Assert.assertEquals((Object)"Gw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{27})));
        Assert.assertEquals((Object)"HA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{28})));
        Assert.assertEquals((Object)"HQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{29})));
        Assert.assertEquals((Object)"Hg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{30})));
        Assert.assertEquals((Object)"Hw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{31})));
        Assert.assertEquals((Object)"IA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{32})));
        Assert.assertEquals((Object)"IQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{33})));
        Assert.assertEquals((Object)"Ig==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{34})));
        Assert.assertEquals((Object)"Iw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{35})));
        Assert.assertEquals((Object)"JA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{36})));
        Assert.assertEquals((Object)"JQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{37})));
        Assert.assertEquals((Object)"Jg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{38})));
        Assert.assertEquals((Object)"Jw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{39})));
        Assert.assertEquals((Object)"KA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{40})));
        Assert.assertEquals((Object)"KQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{41})));
        Assert.assertEquals((Object)"Kg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{42})));
        Assert.assertEquals((Object)"Kw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{43})));
        Assert.assertEquals((Object)"LA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{44})));
        Assert.assertEquals((Object)"LQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{45})));
        Assert.assertEquals((Object)"Lg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{46})));
        Assert.assertEquals((Object)"Lw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{47})));
        Assert.assertEquals((Object)"MA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{48})));
        Assert.assertEquals((Object)"MQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{49})));
        Assert.assertEquals((Object)"Mg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{50})));
        Assert.assertEquals((Object)"Mw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{51})));
        Assert.assertEquals((Object)"NA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{52})));
        Assert.assertEquals((Object)"NQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{53})));
        Assert.assertEquals((Object)"Ng==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{54})));
        Assert.assertEquals((Object)"Nw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{55})));
        Assert.assertEquals((Object)"OA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{56})));
        Assert.assertEquals((Object)"OQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{57})));
        Assert.assertEquals((Object)"Og==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{58})));
        Assert.assertEquals((Object)"Ow==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{59})));
        Assert.assertEquals((Object)"PA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{60})));
        Assert.assertEquals((Object)"PQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{61})));
        Assert.assertEquals((Object)"Pg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{62})));
        Assert.assertEquals((Object)"Pw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{63})));
        Assert.assertEquals((Object)"QA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{64})));
        Assert.assertEquals((Object)"QQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{65})));
        Assert.assertEquals((Object)"Qg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{66})));
        Assert.assertEquals((Object)"Qw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{67})));
        Assert.assertEquals((Object)"RA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{68})));
        Assert.assertEquals((Object)"RQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{69})));
        Assert.assertEquals((Object)"Rg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{70})));
        Assert.assertEquals((Object)"Rw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{71})));
        Assert.assertEquals((Object)"SA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{72})));
        Assert.assertEquals((Object)"SQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{73})));
        Assert.assertEquals((Object)"Sg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{74})));
        Assert.assertEquals((Object)"Sw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{75})));
        Assert.assertEquals((Object)"TA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{76})));
        Assert.assertEquals((Object)"TQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{77})));
        Assert.assertEquals((Object)"Tg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{78})));
        Assert.assertEquals((Object)"Tw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{79})));
        Assert.assertEquals((Object)"UA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{80})));
        Assert.assertEquals((Object)"UQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{81})));
        Assert.assertEquals((Object)"Ug==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{82})));
        Assert.assertEquals((Object)"Uw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{83})));
        Assert.assertEquals((Object)"VA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{84})));
        Assert.assertEquals((Object)"VQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{85})));
        Assert.assertEquals((Object)"Vg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{86})));
        Assert.assertEquals((Object)"Vw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{87})));
        Assert.assertEquals((Object)"WA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{88})));
        Assert.assertEquals((Object)"WQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{89})));
        Assert.assertEquals((Object)"Wg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{90})));
        Assert.assertEquals((Object)"Ww==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{91})));
        Assert.assertEquals((Object)"XA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{92})));
        Assert.assertEquals((Object)"XQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{93})));
        Assert.assertEquals((Object)"Xg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{94})));
        Assert.assertEquals((Object)"Xw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{95})));
        Assert.assertEquals((Object)"YA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{96})));
        Assert.assertEquals((Object)"YQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{97})));
        Assert.assertEquals((Object)"Yg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{98})));
        Assert.assertEquals((Object)"Yw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{99})));
        Assert.assertEquals((Object)"ZA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{100})));
        Assert.assertEquals((Object)"ZQ==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{101})));
        Assert.assertEquals((Object)"Zg==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{102})));
        Assert.assertEquals((Object)"Zw==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{103})));
        Assert.assertEquals((Object)"aA==\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[]{104})));
    }

    @Test
    public void testTriplets() {
        Assert.assertEquals((Object)"AAAA", (Object)new String(Base64.encodeBase64(new byte[3])));
        byte[] byArray = new byte[3];
        byArray[2] = 1;
        Assert.assertEquals((Object)"AAAB", (Object)new String(Base64.encodeBase64(byArray)));
        byte[] byArray2 = new byte[3];
        byArray2[2] = 2;
        Assert.assertEquals((Object)"AAAC", (Object)new String(Base64.encodeBase64(byArray2)));
        byte[] byArray3 = new byte[3];
        byArray3[2] = 3;
        Assert.assertEquals((Object)"AAAD", (Object)new String(Base64.encodeBase64(byArray3)));
        byte[] byArray4 = new byte[3];
        byArray4[2] = 4;
        Assert.assertEquals((Object)"AAAE", (Object)new String(Base64.encodeBase64(byArray4)));
        byte[] byArray5 = new byte[3];
        byArray5[2] = 5;
        Assert.assertEquals((Object)"AAAF", (Object)new String(Base64.encodeBase64(byArray5)));
        byte[] byArray6 = new byte[3];
        byArray6[2] = 6;
        Assert.assertEquals((Object)"AAAG", (Object)new String(Base64.encodeBase64(byArray6)));
        byte[] byArray7 = new byte[3];
        byArray7[2] = 7;
        Assert.assertEquals((Object)"AAAH", (Object)new String(Base64.encodeBase64(byArray7)));
        byte[] byArray8 = new byte[3];
        byArray8[2] = 8;
        Assert.assertEquals((Object)"AAAI", (Object)new String(Base64.encodeBase64(byArray8)));
        byte[] byArray9 = new byte[3];
        byArray9[2] = 9;
        Assert.assertEquals((Object)"AAAJ", (Object)new String(Base64.encodeBase64(byArray9)));
        byte[] byArray10 = new byte[3];
        byArray10[2] = 10;
        Assert.assertEquals((Object)"AAAK", (Object)new String(Base64.encodeBase64(byArray10)));
        byte[] byArray11 = new byte[3];
        byArray11[2] = 11;
        Assert.assertEquals((Object)"AAAL", (Object)new String(Base64.encodeBase64(byArray11)));
        byte[] byArray12 = new byte[3];
        byArray12[2] = 12;
        Assert.assertEquals((Object)"AAAM", (Object)new String(Base64.encodeBase64(byArray12)));
        byte[] byArray13 = new byte[3];
        byArray13[2] = 13;
        Assert.assertEquals((Object)"AAAN", (Object)new String(Base64.encodeBase64(byArray13)));
        byte[] byArray14 = new byte[3];
        byArray14[2] = 14;
        Assert.assertEquals((Object)"AAAO", (Object)new String(Base64.encodeBase64(byArray14)));
        byte[] byArray15 = new byte[3];
        byArray15[2] = 15;
        Assert.assertEquals((Object)"AAAP", (Object)new String(Base64.encodeBase64(byArray15)));
        byte[] byArray16 = new byte[3];
        byArray16[2] = 16;
        Assert.assertEquals((Object)"AAAQ", (Object)new String(Base64.encodeBase64(byArray16)));
        byte[] byArray17 = new byte[3];
        byArray17[2] = 17;
        Assert.assertEquals((Object)"AAAR", (Object)new String(Base64.encodeBase64(byArray17)));
        byte[] byArray18 = new byte[3];
        byArray18[2] = 18;
        Assert.assertEquals((Object)"AAAS", (Object)new String(Base64.encodeBase64(byArray18)));
        byte[] byArray19 = new byte[3];
        byArray19[2] = 19;
        Assert.assertEquals((Object)"AAAT", (Object)new String(Base64.encodeBase64(byArray19)));
        byte[] byArray20 = new byte[3];
        byArray20[2] = 20;
        Assert.assertEquals((Object)"AAAU", (Object)new String(Base64.encodeBase64(byArray20)));
        byte[] byArray21 = new byte[3];
        byArray21[2] = 21;
        Assert.assertEquals((Object)"AAAV", (Object)new String(Base64.encodeBase64(byArray21)));
        byte[] byArray22 = new byte[3];
        byArray22[2] = 22;
        Assert.assertEquals((Object)"AAAW", (Object)new String(Base64.encodeBase64(byArray22)));
        byte[] byArray23 = new byte[3];
        byArray23[2] = 23;
        Assert.assertEquals((Object)"AAAX", (Object)new String(Base64.encodeBase64(byArray23)));
        byte[] byArray24 = new byte[3];
        byArray24[2] = 24;
        Assert.assertEquals((Object)"AAAY", (Object)new String(Base64.encodeBase64(byArray24)));
        byte[] byArray25 = new byte[3];
        byArray25[2] = 25;
        Assert.assertEquals((Object)"AAAZ", (Object)new String(Base64.encodeBase64(byArray25)));
        byte[] byArray26 = new byte[3];
        byArray26[2] = 26;
        Assert.assertEquals((Object)"AAAa", (Object)new String(Base64.encodeBase64(byArray26)));
        byte[] byArray27 = new byte[3];
        byArray27[2] = 27;
        Assert.assertEquals((Object)"AAAb", (Object)new String(Base64.encodeBase64(byArray27)));
        byte[] byArray28 = new byte[3];
        byArray28[2] = 28;
        Assert.assertEquals((Object)"AAAc", (Object)new String(Base64.encodeBase64(byArray28)));
        byte[] byArray29 = new byte[3];
        byArray29[2] = 29;
        Assert.assertEquals((Object)"AAAd", (Object)new String(Base64.encodeBase64(byArray29)));
        byte[] byArray30 = new byte[3];
        byArray30[2] = 30;
        Assert.assertEquals((Object)"AAAe", (Object)new String(Base64.encodeBase64(byArray30)));
        byte[] byArray31 = new byte[3];
        byArray31[2] = 31;
        Assert.assertEquals((Object)"AAAf", (Object)new String(Base64.encodeBase64(byArray31)));
        byte[] byArray32 = new byte[3];
        byArray32[2] = 32;
        Assert.assertEquals((Object)"AAAg", (Object)new String(Base64.encodeBase64(byArray32)));
        byte[] byArray33 = new byte[3];
        byArray33[2] = 33;
        Assert.assertEquals((Object)"AAAh", (Object)new String(Base64.encodeBase64(byArray33)));
        byte[] byArray34 = new byte[3];
        byArray34[2] = 34;
        Assert.assertEquals((Object)"AAAi", (Object)new String(Base64.encodeBase64(byArray34)));
        byte[] byArray35 = new byte[3];
        byArray35[2] = 35;
        Assert.assertEquals((Object)"AAAj", (Object)new String(Base64.encodeBase64(byArray35)));
        byte[] byArray36 = new byte[3];
        byArray36[2] = 36;
        Assert.assertEquals((Object)"AAAk", (Object)new String(Base64.encodeBase64(byArray36)));
        byte[] byArray37 = new byte[3];
        byArray37[2] = 37;
        Assert.assertEquals((Object)"AAAl", (Object)new String(Base64.encodeBase64(byArray37)));
        byte[] byArray38 = new byte[3];
        byArray38[2] = 38;
        Assert.assertEquals((Object)"AAAm", (Object)new String(Base64.encodeBase64(byArray38)));
        byte[] byArray39 = new byte[3];
        byArray39[2] = 39;
        Assert.assertEquals((Object)"AAAn", (Object)new String(Base64.encodeBase64(byArray39)));
        byte[] byArray40 = new byte[3];
        byArray40[2] = 40;
        Assert.assertEquals((Object)"AAAo", (Object)new String(Base64.encodeBase64(byArray40)));
        byte[] byArray41 = new byte[3];
        byArray41[2] = 41;
        Assert.assertEquals((Object)"AAAp", (Object)new String(Base64.encodeBase64(byArray41)));
        byte[] byArray42 = new byte[3];
        byArray42[2] = 42;
        Assert.assertEquals((Object)"AAAq", (Object)new String(Base64.encodeBase64(byArray42)));
        byte[] byArray43 = new byte[3];
        byArray43[2] = 43;
        Assert.assertEquals((Object)"AAAr", (Object)new String(Base64.encodeBase64(byArray43)));
        byte[] byArray44 = new byte[3];
        byArray44[2] = 44;
        Assert.assertEquals((Object)"AAAs", (Object)new String(Base64.encodeBase64(byArray44)));
        byte[] byArray45 = new byte[3];
        byArray45[2] = 45;
        Assert.assertEquals((Object)"AAAt", (Object)new String(Base64.encodeBase64(byArray45)));
        byte[] byArray46 = new byte[3];
        byArray46[2] = 46;
        Assert.assertEquals((Object)"AAAu", (Object)new String(Base64.encodeBase64(byArray46)));
        byte[] byArray47 = new byte[3];
        byArray47[2] = 47;
        Assert.assertEquals((Object)"AAAv", (Object)new String(Base64.encodeBase64(byArray47)));
        byte[] byArray48 = new byte[3];
        byArray48[2] = 48;
        Assert.assertEquals((Object)"AAAw", (Object)new String(Base64.encodeBase64(byArray48)));
        byte[] byArray49 = new byte[3];
        byArray49[2] = 49;
        Assert.assertEquals((Object)"AAAx", (Object)new String(Base64.encodeBase64(byArray49)));
        byte[] byArray50 = new byte[3];
        byArray50[2] = 50;
        Assert.assertEquals((Object)"AAAy", (Object)new String(Base64.encodeBase64(byArray50)));
        byte[] byArray51 = new byte[3];
        byArray51[2] = 51;
        Assert.assertEquals((Object)"AAAz", (Object)new String(Base64.encodeBase64(byArray51)));
        byte[] byArray52 = new byte[3];
        byArray52[2] = 52;
        Assert.assertEquals((Object)"AAA0", (Object)new String(Base64.encodeBase64(byArray52)));
        byte[] byArray53 = new byte[3];
        byArray53[2] = 53;
        Assert.assertEquals((Object)"AAA1", (Object)new String(Base64.encodeBase64(byArray53)));
        byte[] byArray54 = new byte[3];
        byArray54[2] = 54;
        Assert.assertEquals((Object)"AAA2", (Object)new String(Base64.encodeBase64(byArray54)));
        byte[] byArray55 = new byte[3];
        byArray55[2] = 55;
        Assert.assertEquals((Object)"AAA3", (Object)new String(Base64.encodeBase64(byArray55)));
        byte[] byArray56 = new byte[3];
        byArray56[2] = 56;
        Assert.assertEquals((Object)"AAA4", (Object)new String(Base64.encodeBase64(byArray56)));
        byte[] byArray57 = new byte[3];
        byArray57[2] = 57;
        Assert.assertEquals((Object)"AAA5", (Object)new String(Base64.encodeBase64(byArray57)));
        byte[] byArray58 = new byte[3];
        byArray58[2] = 58;
        Assert.assertEquals((Object)"AAA6", (Object)new String(Base64.encodeBase64(byArray58)));
        byte[] byArray59 = new byte[3];
        byArray59[2] = 59;
        Assert.assertEquals((Object)"AAA7", (Object)new String(Base64.encodeBase64(byArray59)));
        byte[] byArray60 = new byte[3];
        byArray60[2] = 60;
        Assert.assertEquals((Object)"AAA8", (Object)new String(Base64.encodeBase64(byArray60)));
        byte[] byArray61 = new byte[3];
        byArray61[2] = 61;
        Assert.assertEquals((Object)"AAA9", (Object)new String(Base64.encodeBase64(byArray61)));
        byte[] byArray62 = new byte[3];
        byArray62[2] = 62;
        Assert.assertEquals((Object)"AAA+", (Object)new String(Base64.encodeBase64(byArray62)));
        byte[] byArray63 = new byte[3];
        byArray63[2] = 63;
        Assert.assertEquals((Object)"AAA/", (Object)new String(Base64.encodeBase64(byArray63)));
    }

    @Test
    public void testTripletsChunked() {
        Assert.assertEquals((Object)"AAAA\r\n", (Object)new String(Base64.encodeBase64Chunked(new byte[3])));
        byte[] byArray = new byte[3];
        byArray[2] = 1;
        Assert.assertEquals((Object)"AAAB\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray)));
        byte[] byArray2 = new byte[3];
        byArray2[2] = 2;
        Assert.assertEquals((Object)"AAAC\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray2)));
        byte[] byArray3 = new byte[3];
        byArray3[2] = 3;
        Assert.assertEquals((Object)"AAAD\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray3)));
        byte[] byArray4 = new byte[3];
        byArray4[2] = 4;
        Assert.assertEquals((Object)"AAAE\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray4)));
        byte[] byArray5 = new byte[3];
        byArray5[2] = 5;
        Assert.assertEquals((Object)"AAAF\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray5)));
        byte[] byArray6 = new byte[3];
        byArray6[2] = 6;
        Assert.assertEquals((Object)"AAAG\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray6)));
        byte[] byArray7 = new byte[3];
        byArray7[2] = 7;
        Assert.assertEquals((Object)"AAAH\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray7)));
        byte[] byArray8 = new byte[3];
        byArray8[2] = 8;
        Assert.assertEquals((Object)"AAAI\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray8)));
        byte[] byArray9 = new byte[3];
        byArray9[2] = 9;
        Assert.assertEquals((Object)"AAAJ\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray9)));
        byte[] byArray10 = new byte[3];
        byArray10[2] = 10;
        Assert.assertEquals((Object)"AAAK\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray10)));
        byte[] byArray11 = new byte[3];
        byArray11[2] = 11;
        Assert.assertEquals((Object)"AAAL\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray11)));
        byte[] byArray12 = new byte[3];
        byArray12[2] = 12;
        Assert.assertEquals((Object)"AAAM\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray12)));
        byte[] byArray13 = new byte[3];
        byArray13[2] = 13;
        Assert.assertEquals((Object)"AAAN\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray13)));
        byte[] byArray14 = new byte[3];
        byArray14[2] = 14;
        Assert.assertEquals((Object)"AAAO\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray14)));
        byte[] byArray15 = new byte[3];
        byArray15[2] = 15;
        Assert.assertEquals((Object)"AAAP\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray15)));
        byte[] byArray16 = new byte[3];
        byArray16[2] = 16;
        Assert.assertEquals((Object)"AAAQ\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray16)));
        byte[] byArray17 = new byte[3];
        byArray17[2] = 17;
        Assert.assertEquals((Object)"AAAR\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray17)));
        byte[] byArray18 = new byte[3];
        byArray18[2] = 18;
        Assert.assertEquals((Object)"AAAS\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray18)));
        byte[] byArray19 = new byte[3];
        byArray19[2] = 19;
        Assert.assertEquals((Object)"AAAT\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray19)));
        byte[] byArray20 = new byte[3];
        byArray20[2] = 20;
        Assert.assertEquals((Object)"AAAU\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray20)));
        byte[] byArray21 = new byte[3];
        byArray21[2] = 21;
        Assert.assertEquals((Object)"AAAV\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray21)));
        byte[] byArray22 = new byte[3];
        byArray22[2] = 22;
        Assert.assertEquals((Object)"AAAW\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray22)));
        byte[] byArray23 = new byte[3];
        byArray23[2] = 23;
        Assert.assertEquals((Object)"AAAX\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray23)));
        byte[] byArray24 = new byte[3];
        byArray24[2] = 24;
        Assert.assertEquals((Object)"AAAY\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray24)));
        byte[] byArray25 = new byte[3];
        byArray25[2] = 25;
        Assert.assertEquals((Object)"AAAZ\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray25)));
        byte[] byArray26 = new byte[3];
        byArray26[2] = 26;
        Assert.assertEquals((Object)"AAAa\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray26)));
        byte[] byArray27 = new byte[3];
        byArray27[2] = 27;
        Assert.assertEquals((Object)"AAAb\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray27)));
        byte[] byArray28 = new byte[3];
        byArray28[2] = 28;
        Assert.assertEquals((Object)"AAAc\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray28)));
        byte[] byArray29 = new byte[3];
        byArray29[2] = 29;
        Assert.assertEquals((Object)"AAAd\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray29)));
        byte[] byArray30 = new byte[3];
        byArray30[2] = 30;
        Assert.assertEquals((Object)"AAAe\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray30)));
        byte[] byArray31 = new byte[3];
        byArray31[2] = 31;
        Assert.assertEquals((Object)"AAAf\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray31)));
        byte[] byArray32 = new byte[3];
        byArray32[2] = 32;
        Assert.assertEquals((Object)"AAAg\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray32)));
        byte[] byArray33 = new byte[3];
        byArray33[2] = 33;
        Assert.assertEquals((Object)"AAAh\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray33)));
        byte[] byArray34 = new byte[3];
        byArray34[2] = 34;
        Assert.assertEquals((Object)"AAAi\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray34)));
        byte[] byArray35 = new byte[3];
        byArray35[2] = 35;
        Assert.assertEquals((Object)"AAAj\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray35)));
        byte[] byArray36 = new byte[3];
        byArray36[2] = 36;
        Assert.assertEquals((Object)"AAAk\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray36)));
        byte[] byArray37 = new byte[3];
        byArray37[2] = 37;
        Assert.assertEquals((Object)"AAAl\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray37)));
        byte[] byArray38 = new byte[3];
        byArray38[2] = 38;
        Assert.assertEquals((Object)"AAAm\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray38)));
        byte[] byArray39 = new byte[3];
        byArray39[2] = 39;
        Assert.assertEquals((Object)"AAAn\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray39)));
        byte[] byArray40 = new byte[3];
        byArray40[2] = 40;
        Assert.assertEquals((Object)"AAAo\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray40)));
        byte[] byArray41 = new byte[3];
        byArray41[2] = 41;
        Assert.assertEquals((Object)"AAAp\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray41)));
        byte[] byArray42 = new byte[3];
        byArray42[2] = 42;
        Assert.assertEquals((Object)"AAAq\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray42)));
        byte[] byArray43 = new byte[3];
        byArray43[2] = 43;
        Assert.assertEquals((Object)"AAAr\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray43)));
        byte[] byArray44 = new byte[3];
        byArray44[2] = 44;
        Assert.assertEquals((Object)"AAAs\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray44)));
        byte[] byArray45 = new byte[3];
        byArray45[2] = 45;
        Assert.assertEquals((Object)"AAAt\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray45)));
        byte[] byArray46 = new byte[3];
        byArray46[2] = 46;
        Assert.assertEquals((Object)"AAAu\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray46)));
        byte[] byArray47 = new byte[3];
        byArray47[2] = 47;
        Assert.assertEquals((Object)"AAAv\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray47)));
        byte[] byArray48 = new byte[3];
        byArray48[2] = 48;
        Assert.assertEquals((Object)"AAAw\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray48)));
        byte[] byArray49 = new byte[3];
        byArray49[2] = 49;
        Assert.assertEquals((Object)"AAAx\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray49)));
        byte[] byArray50 = new byte[3];
        byArray50[2] = 50;
        Assert.assertEquals((Object)"AAAy\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray50)));
        byte[] byArray51 = new byte[3];
        byArray51[2] = 51;
        Assert.assertEquals((Object)"AAAz\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray51)));
        byte[] byArray52 = new byte[3];
        byArray52[2] = 52;
        Assert.assertEquals((Object)"AAA0\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray52)));
        byte[] byArray53 = new byte[3];
        byArray53[2] = 53;
        Assert.assertEquals((Object)"AAA1\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray53)));
        byte[] byArray54 = new byte[3];
        byArray54[2] = 54;
        Assert.assertEquals((Object)"AAA2\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray54)));
        byte[] byArray55 = new byte[3];
        byArray55[2] = 55;
        Assert.assertEquals((Object)"AAA3\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray55)));
        byte[] byArray56 = new byte[3];
        byArray56[2] = 56;
        Assert.assertEquals((Object)"AAA4\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray56)));
        byte[] byArray57 = new byte[3];
        byArray57[2] = 57;
        Assert.assertEquals((Object)"AAA5\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray57)));
        byte[] byArray58 = new byte[3];
        byArray58[2] = 58;
        Assert.assertEquals((Object)"AAA6\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray58)));
        byte[] byArray59 = new byte[3];
        byArray59[2] = 59;
        Assert.assertEquals((Object)"AAA7\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray59)));
        byte[] byArray60 = new byte[3];
        byArray60[2] = 60;
        Assert.assertEquals((Object)"AAA8\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray60)));
        byte[] byArray61 = new byte[3];
        byArray61[2] = 61;
        Assert.assertEquals((Object)"AAA9\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray61)));
        byte[] byArray62 = new byte[3];
        byArray62[2] = 62;
        Assert.assertEquals((Object)"AAA+\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray62)));
        byte[] byArray63 = new byte[3];
        byArray63[2] = 63;
        Assert.assertEquals((Object)"AAA/\r\n", (Object)new String(Base64.encodeBase64Chunked(byArray63)));
    }

    @Test
    public void testUrlSafe() {
        int i = 0;
        while (i <= 150) {
            byte[][] randomData = Base64TestData.randomData(i, true);
            byte[] encoded = randomData[1];
            byte[] decoded = randomData[0];
            byte[] result = Base64.decodeBase64(encoded);
            Assert.assertTrue((String)("url-safe i=" + i), (boolean)Arrays.equals(decoded, result));
            Assert.assertFalse((String)("url-safe i=" + i + " no '='"), (boolean)Base64TestData.bytesContain(encoded, (byte)61));
            Assert.assertFalse((String)("url-safe i=" + i + " no '\\'"), (boolean)Base64TestData.bytesContain(encoded, (byte)92));
            Assert.assertFalse((String)("url-safe i=" + i + " no '+'"), (boolean)Base64TestData.bytesContain(encoded, (byte)43));
            ++i;
        }
    }

    @Test
    public void testUUID() {
        byte[][] ids = new byte[][]{Hex.decode((String)"94ed8d0319e4493399560fb67404d370"), Hex.decode((String)"2bf7cc2701fe4397b49ebeed5acc7090"), Hex.decode((String)"64be154b6ffa40258d1a01288e7c31ca"), Hex.decode((String)"ff7f8fc01cdb471a8c8b5a9306183fe8")};
        byte[][] standard = new byte[][]{StringUtil.getBytesUtf8("lO2NAxnkSTOZVg+2dATTcA=="), StringUtil.getBytesUtf8("K/fMJwH+Q5e0nr7tWsxwkA=="), StringUtil.getBytesUtf8("ZL4VS2/6QCWNGgEojnwxyg=="), StringUtil.getBytesUtf8("/3+PwBzbRxqMi1qTBhg/6A==")};
        byte[][] urlSafe1 = new byte[][]{StringUtil.getBytesUtf8("lO2NAxnkSTOZVg-2dATTcA=="), StringUtil.getBytesUtf8("K_fMJwH-Q5e0nr7tWsxwkA=="), StringUtil.getBytesUtf8("ZL4VS2_6QCWNGgEojnwxyg=="), StringUtil.getBytesUtf8("_3-PwBzbRxqMi1qTBhg_6A==")};
        byte[][] urlSafe2 = new byte[][]{StringUtil.getBytesUtf8("lO2NAxnkSTOZVg-2dATTcA="), StringUtil.getBytesUtf8("K_fMJwH-Q5e0nr7tWsxwkA="), StringUtil.getBytesUtf8("ZL4VS2_6QCWNGgEojnwxyg="), StringUtil.getBytesUtf8("_3-PwBzbRxqMi1qTBhg_6A=")};
        byte[][] urlSafe3 = new byte[][]{StringUtil.getBytesUtf8("lO2NAxnkSTOZVg-2dATTcA"), StringUtil.getBytesUtf8("K_fMJwH-Q5e0nr7tWsxwkA"), StringUtil.getBytesUtf8("ZL4VS2_6QCWNGgEojnwxyg"), StringUtil.getBytesUtf8("_3-PwBzbRxqMi1qTBhg_6A")};
        int i = 0;
        while (i < 4) {
            byte[] encodedStandard = Base64.encodeBase64(ids[i]);
            byte[] encodedUrlSafe = Base64.encodeBase64URLSafe(ids[i]);
            byte[] decodedStandard = Base64.decodeBase64(standard[i]);
            byte[] decodedUrlSafe1 = Base64.decodeBase64(urlSafe1[i]);
            byte[] decodedUrlSafe2 = Base64.decodeBase64(urlSafe2[i]);
            byte[] decodedUrlSafe3 = Base64.decodeBase64(urlSafe3[i]);
            Assert.assertTrue((String)"standard encode uuid", (boolean)Arrays.equals(encodedStandard, standard[i]));
            Assert.assertTrue((String)"url-safe encode uuid", (boolean)Arrays.equals(encodedUrlSafe, urlSafe3[i]));
            Assert.assertTrue((String)"standard decode uuid", (boolean)Arrays.equals(decodedStandard, ids[i]));
            Assert.assertTrue((String)"url-safe1 decode uuid", (boolean)Arrays.equals(decodedUrlSafe1, ids[i]));
            Assert.assertTrue((String)"url-safe2 decode uuid", (boolean)Arrays.equals(decodedUrlSafe2, ids[i]));
            Assert.assertTrue((String)"url-safe3 decode uuid", (boolean)Arrays.equals(decodedUrlSafe3, ids[i]));
            ++i;
        }
    }

    @Test
    public void testByteToStringVariations() {
        Base64 base64 = new Base64(0);
        byte[] b1 = StringUtil.getBytesUtf8("Hello World");
        byte[] b2 = new byte[]{};
        byte[] b3 = null;
        byte[] b4 = Hex.decode((String)"2bf7cc2701fe4397b49ebeed5acc7090");
        Assert.assertEquals((String)"byteToString Hello World", (Object)"SGVsbG8gV29ybGQ=", (Object)base64.encodeToString(b1));
        Assert.assertEquals((String)"byteToString static Hello World", (Object)"SGVsbG8gV29ybGQ=", (Object)Base64.encodeBase64String(b1));
        Assert.assertEquals((String)"byteToString \"\"", (Object)"", (Object)base64.encodeToString(b2));
        Assert.assertEquals((String)"byteToString static \"\"", (Object)"", (Object)Base64.encodeBase64String(b2));
        Assert.assertEquals((String)"byteToString null", null, (Object)base64.encodeToString(b3));
        Assert.assertEquals((String)"byteToString static null", null, (Object)Base64.encodeBase64String(b3));
        Assert.assertEquals((String)"byteToString UUID", (Object)"K/fMJwH+Q5e0nr7tWsxwkA==", (Object)base64.encodeToString(b4));
        Assert.assertEquals((String)"byteToString static UUID", (Object)"K/fMJwH+Q5e0nr7tWsxwkA==", (Object)Base64.encodeBase64String(b4));
        Assert.assertEquals((String)"byteToString static-url-safe UUID", (Object)"K_fMJwH-Q5e0nr7tWsxwkA", (Object)Base64.encodeBase64URLSafeString(b4));
    }

    @Test
    public void testStringToByteVariations() {
        Base64 base64 = new Base64();
        String s1 = "SGVsbG8gV29ybGQ=\r\n";
        String s2 = "";
        String s3 = null;
        String s4a = "K/fMJwH+Q5e0nr7tWsxwkA==\r\n";
        String s4b = "K_fMJwH-Q5e0nr7tWsxwkA";
        byte[] b4 = Hex.decode((String)"2bf7cc2701fe4397b49ebeed5acc7090");
        Assert.assertEquals((String)"StringToByte Hello World", (Object)"Hello World", (Object)StringUtil.newStringUtf8(base64.decode("SGVsbG8gV29ybGQ=\r\n")));
        Assert.assertEquals((String)"StringToByte static Hello World", (Object)"Hello World", (Object)StringUtil.newStringUtf8(Base64.decodeBase64("SGVsbG8gV29ybGQ=\r\n")));
        Assert.assertEquals((String)"StringToByte \"\"", (Object)"", (Object)StringUtil.newStringUtf8(base64.decode("")));
        Assert.assertEquals((String)"StringToByte static \"\"", (Object)"", (Object)StringUtil.newStringUtf8(Base64.decodeBase64("")));
        Assert.assertEquals((String)"StringToByte null", null, (Object)StringUtil.newStringUtf8(base64.decode(s3)));
        Assert.assertEquals((String)"StringToByte static null", null, (Object)StringUtil.newStringUtf8(Base64.decodeBase64(s3)));
        Assert.assertTrue((String)"StringToByte UUID", (boolean)Arrays.equals(b4, base64.decode("K_fMJwH-Q5e0nr7tWsxwkA")));
        Assert.assertTrue((String)"StringToByte static UUID", (boolean)Arrays.equals(b4, Base64.decodeBase64("K/fMJwH+Q5e0nr7tWsxwkA==\r\n")));
        Assert.assertTrue((String)"StringToByte static-url-safe UUID", (boolean)Arrays.equals(b4, Base64.decodeBase64("K_fMJwH-Q5e0nr7tWsxwkA")));
    }

    private String toString(byte[] data) {
        StringBuilder buf = new StringBuilder();
        int i = 0;
        while (i < data.length) {
            buf.append(data[i]);
            if (i != data.length - 1) {
                buf.append(",");
            }
            ++i;
        }
        return buf.toString();
    }
}

