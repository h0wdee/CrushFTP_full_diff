/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jwt.consumer;

import java.util.List;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwksVerificationKeyResolverUsingJwtConsumerTest {
    private static final Logger log = LoggerFactory.getLogger(JwksVerificationKeyResolverUsingJwtConsumerTest.class);

    @Test
    public void idtokenFromPf() throws Exception {
        String jwt = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjhhMDBrIn0.eyJzdWIiOiJoYWlsaWUiLCJhdWQiOiJhIiwianRpIjoiUXhSYjF2Z2tpSE90MlZoNVdST0pQUiIsImlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6OTAzMSIsImlhdCI6MTQyMTA5MzM4MiwiZXhwIjoxNDIxMDkzOTgyLCJub25jZSI6Im5hbmFuYW5hIiwiYWNyIjoidXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFjOmNsYXNzZXM6UGFzc3dvcmQiLCJhdXRoX3RpbWUiOjE0MjEwOTMzNzZ9.OlvyiduU_lZjcFHXchOzOptaBRt2XW_W2LATCPnfmi_mrfz5BsCvCGmTq6HCBBuOVF0BcbLA1h4ls3naPVu4YeWc1jkKFmlu5UwAdHP3fdUvAQdByyXDAxFgYIwl06EF-qpEX7r5_1D0OnrReq55n_SA-iqRync2nn5ZhkRoEj77E5yMFG93yRp4IP-WNZW3mZjkFPnSCEHfRU0IBURfWkPzSkt5bKx8Vr-Oc1I5hFUyKyap8Ky17q_PoF-bHZG7MZ8B5Q5RvweVbdudain_yH3VAujDtqN_gu-7m1Vt6WdQpFIOGsVSpCK0-wtV3MvXzSKLk-5qwdVSI4GH5K_Q9g";
        String jwt2 = "eyJhbGciOiJFUzI1NiIsImtpZCI6IjhhMDBsIn0.eyJzdWIiOiJoYWlsaWUiLCJhdWQiOiJhIiwianRpIjoiRmUwZ1h1UGpmcHoxSHEzdzRaaUZIQiIsImlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6OTAzMSIsImlhdCI6MTQyMTA5Mzg1OSwiZXhwIjoxNDIxMDk0NDU5LCJub25jZSI6ImZmcyIsImFjciI6InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphYzpjbGFzc2VzOlBhc3N3b3JkIiwiYXV0aF90aW1lIjoxNDIxMDkzMzc2fQ.gzJQZRErEHI_v6z6dZboTPzL7p9_wXrMJIWnYZFEENgq3E1InbrZuQM3wB-mJ5r33kwMibJY7Qi4y-jvk0IYqQ";
        String jwksJson = "{\"keys\":[{\"kty\":\"EC\",\"kid\":\"8a00r\",\"use\":\"sig\",\"x\":\"AZkOsR09YQeFcD6rhINHWAaAr8DMx9ndFzum50o5KLLUjqF7opKI7TxR5LP_4uUvG2jojF57xxWVwWi2otdETeI-\",\"y\":\"AadJxOSpjf_4VxRjTT_FdAtFX8Pw-CBpaoX-OQPPQ8o0kOrj5nzIltwnEORDldLFIkQQzgTXMzBnWyQurVHU5pUF\",\"crv\":\"P-521\"},{\"kty\":\"EC\",\"kid\":\"8a00q\",\"use\":\"sig\",\"x\":\"3n74sKXRbaBNw9qOGslnl-WcNCdC75cWo_UquiGUFKdDM3hudthywE5y0R6d2Li8\",\"y\":\"YbZ_0lregvTboKmUX7VE7eknQC1yETKUdHzt_YMX4zbTyOxgZL6A38AVfY8Q8HWd\",\"crv\":\"P-384\"},{\"kty\":\"EC\",\"kid\":\"8a00p\",\"use\":\"sig\",\"x\":\"S-EbFKVG-7pXjdgM9SPPw8rN3V8-2uX4bNg4y8R7EhA\",\"y\":\"KTtyNGz9B9_QrkFf7mP90YiH6F40fAYfqpzQh8HG7tc\",\"crv\":\"P-256\"},{\"kty\":\"RSA\",\"kid\":\"8a00o\",\"use\":\"sig\",\"n\":\"kM-83p_Qaq-1FuxLHH6Y7jQeBT5MK9iHGB75Blnit8rMIcsns72Ls1uhEYiyB3_icK7ibLr2_AHiIl7MnJBY2cCquuwiTccDM5AYUccdypliSlVeAL0MBa_0xfpvBJw8fB45wX6kJKftbQI8xjvFhqSIuGNyQOzFXnJ_mCBOLv-6Nzn79qWxh47mQ7NJk2wSYdFDsz0NNGjBA2VQ9U6weqL1viZ1sbzXr-bJWCjjEYmKC5k0sjGGXJuvMPEqBY2q68kFXD3kiuslQ3tNS1j4d-IraadxpNVtedQ44-xM7MC-WFm2f5eO0LmJRzyipGNPkTer66q6MSEESguyhsoLNQ\",\"e\":\"AQAB\"},{\"kty\":\"EC\",\"kid\":\"8a00n\",\"use\":\"sig\",\"x\":\"ADoTal4nAvVCgicprEBBFOzNKUKVJl1Ph8sISl3Z3tz7TJZlQB485LJ3xil-EmWvqW1-sKFl7dY2YtrGUZvjGp0O\",\"y\":\"AXVB58hIK7buMZmRgDU4hrGvcVQLXa-77_F755OKIkuWP5IJ6GdjFvaRHfIbbHMp-whqjmRrlwfYPN1xmyCGSzpT\",\"crv\":\"P-521\"},{\"kty\":\"EC\",\"kid\":\"8a00m\",\"use\":\"sig\",\"x\":\"5Y4xK9IBGJq5-E6QAVdpiqZb9Z-_tro_rX9TAUdWD3jiVS5N-blEnu5zWzoUoiJk\",\"y\":\"ZDFGBLBbiuvHLMOJ3DoOSRLU94uu5y3s03__HaaaLU04Efc4nGdY3vhTQ4kxEqVj\",\"crv\":\"P-384\"},{\"kty\":\"EC\",\"kid\":\"8a00l\",\"use\":\"sig\",\"x\":\"CWzKLukg4yQzi4oM-2m9M-ClxbU4e6P9G_HRn9A0edI\",\"y\":\"UB1OL_eziV6lA5J0PiAuzoKQU_YbXojbjh0sfxtVlOU\",\"crv\":\"P-256\"},{\"kty\":\"RSA\",\"kid\":\"8a00k\",\"use\":\"sig\",\"n\":\"ux8LdF-7g3X1BlqglZUw36mqjd9P0JWfWxJYvR6pCFSyqLrETc-fL9_lTG3orohkGnEPe7G-BO65ldF44pYEe3eZzcEuEFtiO5W4_Jap1Z430vdYgC_nZtENIJDWlsGM9ev-cOld7By-8l3-wAyuspOKZijWtx6K57VLajyUHBSmbUtaeCwHQOGyMOV1V-cskbTO2u_HrLOLLkSv9oZrznAwpx_paFHy-aAsdFhb7EiBzwqqHQButo3aT3DsR69gbW_Nmrf6tfkril6B3ePKV4od_5jowa6V3765K6v2L4NER7fuZ2hJVbIc0eJXY8tL3NlkBnjnmQ8DBWQR81Ayhw\",\"e\":\"AQAB\"}]}";
        JsonWebKeySet jwks = new JsonWebKeySet(jwksJson);
        JwksVerificationKeyResolver verificationKeyResolver = new JwksVerificationKeyResolver(jwks.getJsonWebKeys());
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1421093387L)).setExpectedAudience("a").setExpectedIssuer("https://localhost:9031").setRequireExpirationTime().setRequireJwtId().setRequireSubject().setVerificationKeyResolver(verificationKeyResolver).build();
        JwtContext ctx = jwtConsumer.process(jwt);
        JwtClaims jwtClaims = ctx.getJwtClaims();
        Assert.assertThat((Object)jwtClaims.getSubject(), (Matcher)CoreMatchers.equalTo((Object)"hailie"));
        ctx = jwtConsumer.process(jwt2);
        jwtClaims = ctx.getJwtClaims();
        Assert.assertThat((Object)jwtClaims.getSubject(), (Matcher)CoreMatchers.equalTo((Object)"hailie"));
        String badJwt = "eyJhbGciOiJFUzI1NiIsImtpZCI6IjhhMTBsIn0.eyJzdWIiOiJoYWlsaWUiLCJhdWQiOiJhIiwianRpIjoiRmUwZ1h1UGpmcHoxSHEzdzRaaUZIQiIsImlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6OTAzMSIsImlhdCI6MTQyMTA5Mzg1OSwiZXhwIjoxNDIxMDk0NDU5LCJub25jZSI6ImZmcyIsImFjciI6InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphYzpjbGFzc2VzOlBhc3N3b3JkIiwiYXV0aF90aW1lIjoxNDIxMDkzMzc2fQ.gzJQZRErEHI_v6z6dZboTPzL7p9_wXrMJIWnYZFEENgq3E1InbrZuQM3wB-mJ5r33kwMibJY7Qi4y-jvk0IYqQ";
        try {
            jwtClaims = jwtConsumer.processToClaims(badJwt);
            Assert.fail((String)("shouldn't have processed/validated but got " + jwtClaims));
        }
        catch (InvalidJwtException e) {
            log.debug("this was expected and is okay: {}", (Object)e.toString());
        }
    }

    @Test
    public void someHmacOnes() throws Exception {
        String json = "{\"keys\":[{\"kty\":\"oct\",\"kid\":\"uno\",  \"k\":\"i-41ccx6-7rPpCK0-i0Hi3K-jcDjt8V0aF9aWY8081d1i2c33pzq5H5eR_JbwmAojgUl727gGoKz7COz9cjic1\"},{\"kty\":\"oct\",\"kid\":\"two\",  \"k\":\"-v_lp7B__xRr-a90cIJqpNCo7u6cY2o9Lz6-P--_01j0aF9d8bcKdrPpCK0-i0Hi3K-jcDjt8V0aF9aWY8081d\"},{\"kty\":\"oct\",\"kid\":\"trois\",\"k\":\"i-41ccx6-7rPpCK0-i0Hi3K-jcDjt89Lz6-c_1_01ji-41ccx6-7rPpCK0-i0HiV0aF9d8bcKic10_aWY8081d\"}]}";
        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(json);
        List<JsonWebKey> jsonWebKeys = jsonWebKeySet.getJsonWebKeys();
        JwksVerificationKeyResolver verificationKeyResolver = new JwksVerificationKeyResolver(jsonWebKeys);
        String jwtWithTrios = "eyJhbGciOiJIUzUxMiIsImtpZCI6InRyb2lzIn0.eyJpc3MiOiJGUk9NIiwiYXVkIjoiVE8iLCJleHAiOjE0MjQyMTgyMDUsInN1YiI6IkFCT1VUIn0.FtkwFqyO7nH6_FNBa-1kMGS2yx8Qabi9kQJMW2jbFWhFHYrM3VTlFIUw1Qc6znJSzLnfveix3Hi5ukc6EgIvVg";
        String jwtWithUno = "eyJhbGciOiJIUzUxMiIsImtpZCI6InVubyJ9.eyJpc3MiOiJGUk9NIiwiYXVkIjoiVE8iLCJleHAiOjE0MjQyMTg0MzYsInN1YiI6IkFCT1VUIn0.pJIcOeLWixUfePKf2ob4Piac6NByJUFlaZ5dXPoVVS1_NHIZr_9oLpFCOAe8HSqc47yO_d3bQ6mOExh1MXA6nQ";
        JwtConsumer jwtConsumer = new JwtConsumerBuilder().setEvaluationTime(NumericDate.fromSeconds(1424218020L)).setExpectedAudience("TO").setExpectedIssuer("FROM").setRequireExpirationTime().setRequireSubject().setVerificationKeyResolver(verificationKeyResolver).build();
        JwtClaims claims = jwtConsumer.processToClaims(jwtWithTrios);
        Assert.assertThat((Object)"ABOUT", (Matcher)CoreMatchers.equalTo((Object)claims.getSubject()));
        claims = jwtConsumer.processToClaims(jwtWithUno);
        Assert.assertThat((Object)"ABOUT", (Matcher)CoreMatchers.equalTo((Object)claims.getSubject()));
        String jwtWithNope = "eyJhbGciOiJIUzUxMiIsImtpZCI6Im5vcGUifQ.eyJpc3MiOiJGUk9NIiwiYXVkIjoiVE8iLCJleHAiOjE0MjQyMTg2NzksInN1YiI6IkFCT1VUIn0.lZOnt-l4wIUl667laxBjZgyTZsebfitsKT1yBrEQ-DognQiqEafQaVrFTaV3dJrZDvgDqAKL9FzxOHfdBg8NXw";
        try {
            claims = jwtConsumer.processToClaims(jwtWithNope);
            Assert.fail((String)("shouldn't have processed/validated but got " + claims));
        }
        catch (InvalidJwtException e) {
            log.debug("this was expected and is okay: {}", (Object)e.toString());
        }
    }
}

