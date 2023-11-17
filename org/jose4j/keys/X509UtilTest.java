/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.keys;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.keys.X509Util;
import org.jose4j.lang.JoseException;

public class X509UtilTest
extends TestCase {
    public void testFromBase64DerAndBackAndMore() throws JoseException {
        String s = "MIICUTCCAfugAwIBAgIBADANBgkqhkiG9w0BAQQFADBXMQswCQYDVQQGEwJDTjEL\nMAkGA1UECBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMC\nVU4xFDASBgNVBAMTC0hlcm9uZyBZYW5nMB4XDTA1MDcxNTIxMTk0N1oXDTA1MDgx\nNDIxMTk0N1owVzELMAkGA1UEBhMCQ04xCzAJBgNVBAgTAlBOMQswCQYDVQQHEwJD\nTjELMAkGA1UEChMCT04xCzAJBgNVBAsTAlVOMRQwEgYDVQQDEwtIZXJvbmcgWWFu\nZzBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQCp5hnG7ogBhtlynpOS21cBewKE/B7j\nV14qeyslnr26xZUsSVko36ZnhiaO/zbMOoRcKK9vEcgMtcLFuQTWDl3RAgMBAAGj\ngbEwga4wHQYDVR0OBBYEFFXI70krXeQDxZgbaCQoR4jUDncEMH8GA1UdIwR4MHaA\nFFXI70krXeQDxZgbaCQoR4jUDncEoVukWTBXMQswCQYDVQQGEwJDTjELMAkGA1UE\nCBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMCVU4xFDAS\nBgNVBAMTC0hlcm9uZyBZYW5nggEAMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEE\nBQADQQA/ugzBrjjK9jcWnDVfGHlk3icNRq0oV7Ri32z/+HQX67aRfgZu7KWdI+Ju\nWm7DCfrPNGVwFWUQOmsPue9rZBgO\n";
        X509Util x5u = new X509Util();
        X509Certificate x509Certificate = x5u.fromBase64Der(s);
        X509UtilTest.assertTrue((boolean)x509Certificate.getSubjectDN().toString().contains("Yang"));
        String pem = x5u.toPem(x509Certificate);
        X509UtilTest.assertTrue((pem.charAt(64) == '\r' ? 1 : 0) != 0);
        X509UtilTest.assertTrue((pem.charAt(65) == '\n' ? 1 : 0) != 0);
        String encoded = x5u.toBase64(x509Certificate);
        X509UtilTest.assertEquals((int)-1, (int)encoded.indexOf(13));
        X509UtilTest.assertEquals((int)-1, (int)encoded.indexOf(10));
        PublicJsonWebKey jwk = PublicJsonWebKey.Factory.newPublicJwk(x509Certificate.getPublicKey());
        jwk.setCertificateChain(x509Certificate);
        String jsonJwk = jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        Map<String, Object> parsed = JsonUtil.parseJson(jsonJwk);
        List x5cStrings = (List)parsed.get("x5c");
        String x5cValue = (String)x5cStrings.get(0);
        X509UtilTest.assertEquals((int)-1, (int)x5cValue.indexOf(13));
        X509UtilTest.assertEquals((int)-1, (int)x5cValue.indexOf(10));
        PublicJsonWebKey jwkFromJson = PublicJsonWebKey.Factory.newPublicJwk(jsonJwk);
        X509UtilTest.assertEquals((Object)x509Certificate.getPublicKey(), (Object)jwkFromJson.getPublicKey());
        X509UtilTest.assertEquals((Object)x509Certificate, (Object)jwkFromJson.getLeafCertificate());
    }

    public void testFromGoogleEndpoint() throws JoseException {
        String bder = "MIICITCCAYqgAwIBAgIINulGhAa6BxUwDQYJKoZIhvcNAQEFBQAwNjE0MDIGA1UE\nAxMrZmVkZXJhdGVkLXNpZ25vbi5zeXN0ZW0uZ3NlcnZpY2VhY2NvdW50LmNvbTAe\nFw0xMzAyMjYwNTI4MzRaFw0xMzAyMjcxODI4MzRaMDYxNDAyBgNVBAMTK2ZlZGVy\nYXRlZC1zaWdub24uc3lzdGVtLmdzZXJ2aWNlYWNjb3VudC5jb20wgZ8wDQYJKoZI\nhvcNAQEBBQADgY0AMIGJAoGBAL9Q8ogQtQfHVzto3p1xiQjBXxcBceE/LTa9jxv4\nEEp0fkKP9bBz/uRlpGkNnP++qkPb6N6s4+mgF12JbTsyRxb4jfXGobfW2lx6HZkX\nRoCk4mAdu3axEVGlYQq0IIsgvNfFiks0Z2pRkovDshPqXBt0FUemM0M7bVODAsZn\ncE3xAgMBAAGjODA2MAwGA1UdEwEB/wQCMAAwDgYDVR0PAQH/BAQDAgeAMBYGA1Ud\nJQEB/wQMMAoGCCsGAQUFBwMCMA0GCSqGSIb3DQEBBQUAA4GBAA38HHhl0cddqDEd\nswuGUcIvPE1QDqlyfYZUZyZPfZ2JSuYj34DdLm31aq8SOAxNRorpyel/n1bxDUfI\nFueGAkh5AySoPsH7wnj/ZigsidGct9yllIcsqeIvFYkOW53rVwpriU3wcEmh+RzI\nLUYyJkbYf3pY8XHeE56dZqzU+E8Y";
        X509Util x5u = new X509Util();
        X509Certificate x509Certificate = x5u.fromBase64Der(bder);
        X509UtilTest.assertTrue((boolean)x509Certificate.getSubjectDN().toString().contains("federated-signon.system.gserviceaccount.com"));
    }
}

