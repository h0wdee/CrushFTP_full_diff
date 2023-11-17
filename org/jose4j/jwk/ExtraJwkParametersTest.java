/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jwk;

import java.security.Key;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.keys.ExampleRsaKeyFromJws;
import org.junit.Assert;
import org.junit.Test;

public class ExtraJwkParametersTest {
    @Test
    public void parseWithCustomParams() throws Exception {
        String json = "{\"kty\":\"EC\",\"x\":\"14PCFt8uuLb6mbfn1XTOHzcSfZk0nU_AGe2hq91Gvl4\",\"y\":\"U0rLlwB8be5YM2ajGyactlplFol7FKJrN83mNAOpuss\",\"crv\":\"P-256\",\"meh\":\"just some value\",\"number\":860}";
        JsonWebKey jwk = JsonWebKey.Factory.newJwk(json);
        String meh = jwk.getOtherParameterValue("meh", String.class);
        Assert.assertThat((Object)meh, (Matcher)CoreMatchers.equalTo((Object)"just some value"));
        Number number = jwk.getOtherParameterValue("number", Number.class);
        Assert.assertThat((Object)number.intValue(), (Matcher)CoreMatchers.equalTo((Object)860));
        json = jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        Assert.assertTrue((boolean)json.contains("\"meh\""));
        Assert.assertTrue((boolean)json.contains("\"just some value\""));
        Assert.assertTrue((boolean)json.contains("\"number\""));
        Assert.assertTrue((boolean)json.contains("860"));
    }

    @Test
    public void fromKeyWithCustomParams() throws Exception {
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk(ExampleRsaKeyFromJws.PUBLIC_KEY);
        String name = "artisanal";
        String value = "parameter";
        jsonWebKey.setOtherParameter("artisanal", "parameter");
        Assert.assertThat((Object)jsonWebKey.getOtherParameterValue("artisanal", String.class), (Matcher)CoreMatchers.equalTo((Object)"parameter"));
        String json = jsonWebKey.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        Assert.assertTrue((boolean)json.contains("\"artisanal\""));
        Assert.assertTrue((boolean)json.contains("\"parameter\""));
        jsonWebKey = JsonWebKey.Factory.newJwk(json);
        Assert.assertThat((Object)"parameter", (Matcher)CoreMatchers.equalTo((Object)jsonWebKey.getOtherParameterValue("artisanal", String.class)));
        Assert.assertThat((Object)ExampleRsaKeyFromJws.PUBLIC_KEY, (Matcher)CoreMatchers.equalTo((Object)jsonWebKey.getKey()));
    }

    @Test
    public void roundTripOctKey() throws Exception {
        String name = "artisanal";
        String value = "parameter";
        String json = "{\"kty\":\"oct\",\"k\":\"jr-TRYPvKkOxw_cBB5y4plEX5cEUT1AawUU7G3id7u4\",\"artisanal\":\"parameter\"}";
        JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk(json);
        Key key = jsonWebKey.getKey();
        Assert.assertThat((Object)"parameter", (Matcher)CoreMatchers.equalTo((Object)jsonWebKey.getOtherParameterValue("artisanal", String.class)));
        String publicOnlyJson = jsonWebKey.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        Assert.assertFalse((boolean)publicOnlyJson.contains("\"k\""));
        Assert.assertTrue((boolean)publicOnlyJson.contains("\"artisanal\""));
        Assert.assertTrue((boolean)publicOnlyJson.contains("\"parameter\""));
        String includeSymmetricJson = jsonWebKey.toJson(JsonWebKey.OutputControlLevel.INCLUDE_SYMMETRIC);
        Assert.assertTrue((boolean)includeSymmetricJson.contains("\"k\""));
        Assert.assertTrue((boolean)includeSymmetricJson.contains("\"artisanal\""));
        Assert.assertTrue((boolean)includeSymmetricJson.contains("\"parameter\""));
        jsonWebKey = JsonWebKey.Factory.newJwk(includeSymmetricJson);
        Assert.assertThat((Object)"parameter", (Matcher)CoreMatchers.equalTo((Object)jsonWebKey.getOtherParameterValue("artisanal", String.class)));
        Assert.assertThat((Object)key, (Matcher)CoreMatchers.equalTo((Object)jsonWebKey.getKey()));
    }
}

