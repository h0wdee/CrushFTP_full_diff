/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwk;

import java.math.BigInteger;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.util.HashMap;
import java.util.Map;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.keys.EcKeyUtil;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.lang.JoseException;

public class EllipticCurveJsonWebKey
extends PublicJsonWebKey {
    public static final String KEY_TYPE = "EC";
    public static final String CURVE_MEMBER_NAME = "crv";
    public static final String X_MEMBER_NAME = "x";
    public static final String Y_MEMBER_NAME = "y";
    public static final String PRIVATE_KEY_MEMBER_NAME = "d";
    private String curveName;

    public EllipticCurveJsonWebKey(ECPublicKey publicKey) {
        super(publicKey);
        ECParameterSpec spec = publicKey.getParams();
        EllipticCurve curve = spec.getCurve();
        this.curveName = EllipticCurves.getName(curve);
    }

    public EllipticCurveJsonWebKey(Map<String, Object> params) throws JoseException {
        this(params, null);
    }

    public EllipticCurveJsonWebKey(Map<String, Object> params, String jcaProvider) throws JoseException {
        super(params, jcaProvider);
        this.curveName = EllipticCurveJsonWebKey.getString(params, CURVE_MEMBER_NAME, true);
        ECParameterSpec curve = EllipticCurves.getSpec(this.curveName);
        BigInteger x = this.getBigIntFromBase64UrlEncodedParam(params, X_MEMBER_NAME, true);
        BigInteger y = this.getBigIntFromBase64UrlEncodedParam(params, Y_MEMBER_NAME, true);
        EcKeyUtil keyUtil = new EcKeyUtil(jcaProvider, null);
        this.key = keyUtil.publicKey(x, y, curve);
        this.checkForBareKeyCertMismatch();
        if (params.containsKey(PRIVATE_KEY_MEMBER_NAME)) {
            BigInteger d = this.getBigIntFromBase64UrlEncodedParam(params, PRIVATE_KEY_MEMBER_NAME, false);
            this.privateKey = keyUtil.privateKey(d, curve);
        }
        this.removeFromOtherParams(CURVE_MEMBER_NAME, X_MEMBER_NAME, Y_MEMBER_NAME, PRIVATE_KEY_MEMBER_NAME);
    }

    public ECPublicKey getECPublicKey() {
        return (ECPublicKey)this.key;
    }

    public ECPrivateKey getEcPrivateKey() {
        return (ECPrivateKey)this.privateKey;
    }

    @Override
    public String getKeyType() {
        return KEY_TYPE;
    }

    public String getCurveName() {
        return this.curveName;
    }

    private int getCoordinateByteLength() {
        ECParameterSpec spec = EllipticCurves.getSpec(this.getCurveName());
        return (int)Math.ceil((double)spec.getCurve().getField().getFieldSize() / 8.0);
    }

    @Override
    protected void fillPublicTypeSpecificParams(Map<String, Object> params) {
        ECPublicKey ecPublicKey = this.getECPublicKey();
        ECPoint w = ecPublicKey.getW();
        int coordinateByteLength = this.getCoordinateByteLength();
        this.putBigIntAsBase64UrlEncodedParam(params, X_MEMBER_NAME, w.getAffineX(), coordinateByteLength);
        this.putBigIntAsBase64UrlEncodedParam(params, Y_MEMBER_NAME, w.getAffineY(), coordinateByteLength);
        params.put(CURVE_MEMBER_NAME, this.getCurveName());
    }

    @Override
    protected void fillPrivateTypeSpecificParams(Map<String, Object> params) {
        ECPrivateKey ecPrivateKey = this.getEcPrivateKey();
        if (ecPrivateKey != null) {
            int coordinateByteLength = this.getCoordinateByteLength();
            this.putBigIntAsBase64UrlEncodedParam(params, PRIVATE_KEY_MEMBER_NAME, ecPrivateKey.getS(), coordinateByteLength);
        }
    }

    @Override
    protected String produceThumbprintHashInput() {
        String template = "{\"crv\":\"%s\",\"kty\":\"EC\",\"x\":\"%s\",\"y\":\"%s\"}";
        HashMap<String, Object> params = new HashMap<String, Object>();
        this.fillPublicTypeSpecificParams(params);
        Object crv = params.get(CURVE_MEMBER_NAME);
        Object x = params.get(X_MEMBER_NAME);
        Object y = params.get(Y_MEMBER_NAME);
        return String.format(template, crv, x, y);
    }
}

