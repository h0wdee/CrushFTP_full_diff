/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwk;

import java.math.BigInteger;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.keys.RsaKeyUtil;
import org.jose4j.lang.JoseException;

public class RsaJsonWebKey
extends PublicJsonWebKey {
    public static final String MODULUS_MEMBER_NAME = "n";
    public static final String EXPONENT_MEMBER_NAME = "e";
    public static final String PRIVATE_EXPONENT_MEMBER_NAME = "d";
    public static final String FIRST_PRIME_FACTOR_MEMBER_NAME = "p";
    public static final String SECOND_PRIME_FACTOR_MEMBER_NAME = "q";
    public static final String FIRST_FACTOR_CRT_EXPONENT_MEMBER_NAME = "dp";
    public static final String SECOND_FACTOR_CRT_EXPONENT_MEMBER_NAME = "dq";
    public static final String FIRST_CRT_COEFFICIENT_MEMBER_NAME = "qi";
    public static final String OTHER_PRIMES_INFO_MEMBER_NAME = "oth";
    public static final String PRIME_FACTOR_OTHER_MEMBER_NAME = "r";
    public static final String FACTOR_CRT_EXPONENT_OTHER_MEMBER_NAME = "d";
    public static final String FACTOR_CRT_COEFFICIENT = "t";
    public static final String KEY_TYPE = "RSA";

    public RsaJsonWebKey(RSAPublicKey publicKey) {
        super(publicKey);
    }

    public RsaJsonWebKey(Map<String, Object> params) throws JoseException {
        this(params, null);
    }

    public RsaJsonWebKey(Map<String, Object> params, String jcaProvider) throws JoseException {
        super(params, jcaProvider);
        BigInteger modulus = this.getBigIntFromBase64UrlEncodedParam(params, MODULUS_MEMBER_NAME, true);
        BigInteger publicExponent = this.getBigIntFromBase64UrlEncodedParam(params, EXPONENT_MEMBER_NAME, true);
        RsaKeyUtil rsaKeyUtil = new RsaKeyUtil(jcaProvider, null);
        this.key = rsaKeyUtil.publicKey(modulus, publicExponent);
        this.checkForBareKeyCertMismatch();
        if (params.containsKey("d")) {
            BigInteger d = this.getBigIntFromBase64UrlEncodedParam(params, "d", false);
            if (params.containsKey(FIRST_PRIME_FACTOR_MEMBER_NAME)) {
                BigInteger p = this.getBigIntFromBase64UrlEncodedParam(params, FIRST_PRIME_FACTOR_MEMBER_NAME, false);
                BigInteger q = this.getBigIntFromBase64UrlEncodedParam(params, SECOND_PRIME_FACTOR_MEMBER_NAME, false);
                BigInteger dp = this.getBigIntFromBase64UrlEncodedParam(params, FIRST_FACTOR_CRT_EXPONENT_MEMBER_NAME, false);
                BigInteger dq = this.getBigIntFromBase64UrlEncodedParam(params, SECOND_FACTOR_CRT_EXPONENT_MEMBER_NAME, false);
                BigInteger qi = this.getBigIntFromBase64UrlEncodedParam(params, FIRST_CRT_COEFFICIENT_MEMBER_NAME, false);
                this.privateKey = rsaKeyUtil.privateKey(modulus, publicExponent, d, p, q, dp, dq, qi);
            } else {
                this.privateKey = rsaKeyUtil.privateKey(modulus, d);
            }
        }
        this.removeFromOtherParams(MODULUS_MEMBER_NAME, EXPONENT_MEMBER_NAME, "d", FIRST_PRIME_FACTOR_MEMBER_NAME, SECOND_PRIME_FACTOR_MEMBER_NAME, FIRST_FACTOR_CRT_EXPONENT_MEMBER_NAME, SECOND_FACTOR_CRT_EXPONENT_MEMBER_NAME, FIRST_CRT_COEFFICIENT_MEMBER_NAME);
    }

    @Override
    public String getKeyType() {
        return KEY_TYPE;
    }

    public RSAPublicKey getRsaPublicKey() {
        return (RSAPublicKey)this.key;
    }

    public RSAPublicKey getRSAPublicKey() {
        return this.getRsaPublicKey();
    }

    public RSAPrivateKey getRsaPrivateKey() {
        return (RSAPrivateKey)this.privateKey;
    }

    @Override
    protected void fillPublicTypeSpecificParams(Map<String, Object> params) {
        RSAPublicKey rsaPublicKey = this.getRsaPublicKey();
        this.putBigIntAsBase64UrlEncodedParam(params, MODULUS_MEMBER_NAME, rsaPublicKey.getModulus());
        this.putBigIntAsBase64UrlEncodedParam(params, EXPONENT_MEMBER_NAME, rsaPublicKey.getPublicExponent());
    }

    @Override
    protected void fillPrivateTypeSpecificParams(Map<String, Object> params) {
        RSAPrivateKey rsaPrivateKey = this.getRsaPrivateKey();
        if (rsaPrivateKey != null) {
            this.putBigIntAsBase64UrlEncodedParam(params, "d", rsaPrivateKey.getPrivateExponent());
            if (rsaPrivateKey instanceof RSAPrivateCrtKey) {
                RSAPrivateCrtKey crt = (RSAPrivateCrtKey)rsaPrivateKey;
                this.putBigIntAsBase64UrlEncodedParam(params, FIRST_PRIME_FACTOR_MEMBER_NAME, crt.getPrimeP());
                this.putBigIntAsBase64UrlEncodedParam(params, SECOND_PRIME_FACTOR_MEMBER_NAME, crt.getPrimeQ());
                this.putBigIntAsBase64UrlEncodedParam(params, FIRST_FACTOR_CRT_EXPONENT_MEMBER_NAME, crt.getPrimeExponentP());
                this.putBigIntAsBase64UrlEncodedParam(params, SECOND_FACTOR_CRT_EXPONENT_MEMBER_NAME, crt.getPrimeExponentQ());
                this.putBigIntAsBase64UrlEncodedParam(params, FIRST_CRT_COEFFICIENT_MEMBER_NAME, crt.getCrtCoefficient());
            }
        }
    }

    @Override
    protected String produceThumbprintHashInput() {
        String template = "{\"e\":\"%s\",\"kty\":\"RSA\",\"n\":\"%s\"}";
        HashMap<String, Object> params = new HashMap<String, Object>();
        this.fillPublicTypeSpecificParams(params);
        return String.format(template, params.get(EXPONENT_MEMBER_NAME), params.get(MODULUS_MEMBER_NAME));
    }
}

