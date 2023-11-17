/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jwt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.jwt.GeneralJwtException;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.ReservedClaimNames;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.junit.Assert;
import org.junit.Test;

public class JwtClaimsTest {
    public static final int DEFAULT_JTI_LENGTH = 22;

    @Test(expected=MalformedClaimException.class)
    public void testGetBadIssuer() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"iss\":{\"name\":\"value\"}}");
        claims.getIssuer();
    }

    @Test
    public void testGetNullIssuer() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"exp\":123456781}");
        Assert.assertNull((Object)claims.getIssuer());
    }

    @Test
    public void testGetIssuer() throws InvalidJwtException, MalformedClaimException {
        String issuer = "https//idp.example.com";
        JwtClaims claims = JwtClaims.parse("{\"iss\":\"" + issuer + "\"}");
        Assert.assertThat((Object)issuer, (Matcher)CoreMatchers.equalTo((Object)claims.getIssuer()));
    }

    @Test
    public void testGetAudienceWithNoAudience() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"iss\":\"some-issuer\"}");
        Assert.assertFalse((boolean)claims.hasAudience());
        Assert.assertTrue((boolean)claims.getAudience().isEmpty());
    }

    @Test
    public void testGetAudienceSingleInArray() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"aud\":[\"one\"]}");
        List<String> audiences = claims.getAudience();
        Assert.assertThat((Object)1, (Matcher)CoreMatchers.equalTo((Object)audiences.size()));
        Assert.assertThat((Object)"one", (Matcher)CoreMatchers.equalTo((Object)audiences.get(0)));
    }

    @Test
    public void testGetAudienceSingleValue() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"aud\":\"one\"}");
        List<String> audiences = claims.getAudience();
        Assert.assertThat((Object)1, (Matcher)CoreMatchers.equalTo((Object)audiences.size()));
        Assert.assertThat((Object)"one", (Matcher)CoreMatchers.equalTo((Object)audiences.get(0)));
    }

    @Test
    public void testGetAudienceMultipleInArray() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"aud\":[\"one\",\"two\",\"three\"]}");
        List<String> audiences = claims.getAudience();
        Assert.assertThat((Object)3, (Matcher)CoreMatchers.equalTo((Object)audiences.size()));
        Iterator<String> iterator = audiences.iterator();
        Assert.assertThat((Object)"one", (Matcher)CoreMatchers.equalTo((Object)iterator.next()));
        Assert.assertThat((Object)"two", (Matcher)CoreMatchers.equalTo((Object)iterator.next()));
        Assert.assertThat((Object)"three", (Matcher)CoreMatchers.equalTo((Object)iterator.next()));
    }

    @Test
    public void testGetAudienceArray() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"aud\":[]}");
        List<String> audiences = claims.getAudience();
        Assert.assertThat((Object)0, (Matcher)CoreMatchers.equalTo((Object)audiences.size()));
    }

    @Test(expected=MalformedClaimException.class)
    public void testGetBadAudience1() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"aud\":1996}");
        claims.getAudience();
    }

    @Test(expected=MalformedClaimException.class)
    public void testGetBadAudience2() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"aud\":[\"value\", \"other\", 2, \"value\"]}");
        claims.getAudience();
    }

    @Test
    public void testGetNullSubject() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"exp\":123456781}");
        Assert.assertNull((Object)claims.getSubject());
    }

    @Test
    public void testGetSubject() throws InvalidJwtException, MalformedClaimException {
        String sub = "subject@example.com";
        JwtClaims claims = JwtClaims.parse("{\"sub\":\"" + sub + "\"}");
        Assert.assertThat((Object)sub, (Matcher)CoreMatchers.equalTo((Object)claims.getSubject()));
    }

    @Test(expected=MalformedClaimException.class)
    public void testGetBadSubject() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"sub\":[\"nope\", \"not\", \"good\"]}");
        claims.getSubject();
    }

    @Test
    public void testGetNullJti() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"whatever\":123456781}");
        Assert.assertNull((Object)claims.getJwtId());
    }

    @Test
    public void testGetJti() throws InvalidJwtException, MalformedClaimException {
        String jti = "Xk9c2inNN8fFs60epZil3";
        JwtClaims claims = JwtClaims.parse("{\"jti\":\"" + jti + "\"}");
        Assert.assertThat((Object)jti, (Matcher)CoreMatchers.equalTo((Object)claims.getJwtId()));
    }

    @Test(expected=MalformedClaimException.class)
    public void testGetBadJti() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"jti\":[\"nope\", \"not\", \"good\"]}");
        claims.getJwtId();
    }

    @Test
    public void generateAndGetJwt() throws MalformedClaimException {
        JwtClaims claims = new JwtClaims();
        claims.setGeneratedJwtId();
        String jwtId = claims.getJwtId();
        Assert.assertThat((Object)22, (Matcher)CoreMatchers.equalTo((Object)jwtId.length()));
        claims.setJwtId("igotyourjtirighthere");
        jwtId = claims.getJwtId();
        Assert.assertThat((Object)jwtId, (Matcher)CoreMatchers.equalTo((Object)"igotyourjtirighthere"));
    }

    @Test
    public void testGetNullExp() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"right\":123456781}");
        Assert.assertNull((Object)claims.getExpirationTime());
    }

    @Test
    public void testGetExp() throws InvalidJwtException, MalformedClaimException {
        long exp = 1418823169L;
        JwtClaims claims = JwtClaims.parse("{\"exp\":" + exp + "}");
        Assert.assertThat((Object)exp, (Matcher)CoreMatchers.equalTo((Object)claims.getExpirationTime().getValue()));
    }

    @Test(expected=MalformedClaimException.class)
    public void testGetBadExp() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"exp\":\"nope\"}");
        claims.getExpirationTime();
    }

    @Test
    public void testGetNullNbf() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"right\":123456781}");
        Assert.assertNull((Object)claims.getNotBefore());
    }

    @Test
    public void testGetNbf() throws InvalidJwtException, MalformedClaimException {
        long nbf = 1418823109L;
        JwtClaims claims = JwtClaims.parse("{\"nbf\":" + nbf + "}");
        Assert.assertThat((Object)nbf, (Matcher)CoreMatchers.equalTo((Object)claims.getNotBefore().getValue()));
    }

    @Test(expected=MalformedClaimException.class)
    public void testGetBadNbf() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"nbf\":[\"nope\", \"not\", \"good\"]}");
        claims.getNotBefore();
    }

    @Test
    public void testGetNullIat() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"right\":123456781, \"wrong\":123452781}");
        Assert.assertNull((Object)claims.getIssuedAt());
    }

    @Test
    public void testGetIat() throws InvalidJwtException, MalformedClaimException {
        long nbf = 1418823119L;
        JwtClaims claims = JwtClaims.parse("{\"iat\":" + nbf + "}");
        Assert.assertThat((Object)nbf, (Matcher)CoreMatchers.equalTo((Object)claims.getIssuedAt().getValue()));
    }

    @Test(expected=MalformedClaimException.class)
    public void testGetBadIat() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = JwtClaims.parse("{\"iat\":\"not\"}");
        claims.getIssuedAt();
    }

    @Test
    public void testBasicCreate() throws GeneralJwtException {
        JwtClaims claims = new JwtClaims();
        claims.setSubject("subject");
        claims.setAudience("audience");
        claims.setIssuer("issuer");
        claims.setJwtId("id");
        claims.setExpirationTime(NumericDate.fromSeconds(231458800L));
        claims.setIssuedAt(NumericDate.fromSeconds(231459000L));
        claims.setNotBefore(NumericDate.fromSeconds(231459600L));
        String jsonClaims = claims.toJson();
        Assert.assertThat((Object)jsonClaims, (Matcher)CoreMatchers.containsString((String)"\"iss\":\"issuer\""));
        Assert.assertThat((Object)jsonClaims, (Matcher)CoreMatchers.containsString((String)"\"aud\":\"audience\""));
        Assert.assertThat((Object)jsonClaims, (Matcher)CoreMatchers.containsString((String)"\"sub\":\"subject\""));
        Assert.assertThat((Object)jsonClaims, (Matcher)CoreMatchers.containsString((String)"\"jti\":\"id\""));
        Assert.assertThat((Object)jsonClaims, (Matcher)CoreMatchers.containsString((String)"\"exp\":231458800"));
        Assert.assertThat((Object)jsonClaims, (Matcher)CoreMatchers.containsString((String)"\"iat\":231459000"));
        Assert.assertThat((Object)jsonClaims, (Matcher)CoreMatchers.containsString((String)"\"nbf\":231459600"));
    }

    @Test
    public void testSettingAud() throws GeneralJwtException {
        JwtClaims claims = new JwtClaims();
        claims.setAudience("audience");
        Assert.assertThat((Object)claims.toJson(), (Matcher)CoreMatchers.containsString((String)"\"aud\":\"audience\""));
        claims.setAudience("audience1", "audience2", "outlier");
        Assert.assertThat((Object)claims.toJson(), (Matcher)CoreMatchers.containsString((String)"\"aud\":[\"audience1\",\"audience2\",\"outlier\"]"));
        claims.setAudience(Collections.singletonList("audience"));
        Assert.assertThat((Object)claims.toJson(), (Matcher)CoreMatchers.containsString((String)"\"aud\":\"audience\""));
        ArrayList<String> list = new ArrayList<String>();
        list.add("one");
        list.add("two");
        list.add("three");
        claims.setAudience(list);
        Assert.assertThat((Object)claims.toJson(), (Matcher)CoreMatchers.containsString((String)"\"aud\":[\"one\",\"two\",\"three\"]"));
        Assert.assertFalse((boolean)claims.getClaimsMap().isEmpty());
        claims.unsetClaim("aud");
        Assert.assertThat((Object)claims.toJson(), (Matcher)CoreMatchers.equalTo((Object)"{}"));
        Assert.assertTrue((boolean)claims.getClaimsMap().isEmpty());
    }

    @Test
    public void testCreateWithHelpers() throws InvalidJwtException, MalformedClaimException {
        JwtClaims claims = new JwtClaims();
        claims.setSubject("subject");
        claims.setAudience("audience");
        claims.setIssuer("issuer");
        claims.setGeneratedJwtId();
        claims.setExpirationTimeMinutesInTheFuture(10.0f);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(5.0f);
        String jsonClaims = claims.toJson();
        Assert.assertThat((Object)jsonClaims, (Matcher)CoreMatchers.containsString((String)"\"iss\":\"issuer\""));
        Assert.assertThat((Object)jsonClaims, (Matcher)CoreMatchers.containsString((String)"\"aud\":\"audience\""));
        Assert.assertThat((Object)jsonClaims, (Matcher)CoreMatchers.containsString((String)"\"sub\":\"subject\""));
        Assert.assertThat((Object)jsonClaims, (Matcher)CoreMatchers.containsString((String)"\"jti\":\""));
        Assert.assertThat((Object)jsonClaims, (Matcher)CoreMatchers.containsString((String)"\"exp\":"));
        Assert.assertThat((Object)jsonClaims, (Matcher)CoreMatchers.containsString((String)"\"iat\":"));
        Assert.assertThat((Object)jsonClaims, (Matcher)CoreMatchers.containsString((String)"\"nbf\":"));
        JwtClaims parsedClaims = JwtClaims.parse(jsonClaims);
        Assert.assertThat((Object)22, (Matcher)CoreMatchers.equalTo((Object)parsedClaims.getJwtId().length()));
        long nowMillis = System.currentTimeMillis();
        long nbfMillis = parsedClaims.getNotBefore().getValueInMillis();
        Assert.assertTrue((nbfMillis <= nowMillis - 300000L ? 1 : 0) != 0);
        Assert.assertTrue((nbfMillis > nowMillis - 302000L ? 1 : 0) != 0);
        long iatMIllis = parsedClaims.getIssuedAt().getValueInMillis();
        Assert.assertTrue((iatMIllis < nowMillis + 100L ? 1 : 0) != 0);
        Assert.assertTrue((nowMillis - 2000L < iatMIllis ? 1 : 0) != 0);
        long expMillis = parsedClaims.getExpirationTime().getValueInMillis();
        Assert.assertTrue((expMillis > nowMillis + 598000L ? 1 : 0) != 0);
        Assert.assertTrue((expMillis < nowMillis + 600000L ? 1 : 0) != 0);
    }

    @Test
    public void testSetExpirationTimeMinutesInTheFuturePartOfMinute() throws InvalidJwtException, MalformedClaimException {
        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setExpirationTimeMinutesInTheFuture(0.167f);
        jwtClaims = JwtClaims.parse(jwtClaims.toJson());
        NumericDate expirationTime = jwtClaims.getExpirationTime();
        NumericDate checker = NumericDate.now();
        Assert.assertTrue((boolean)checker.isBefore(expirationTime));
        checker.addSeconds(9L);
        Assert.assertTrue((boolean)checker.isBefore(expirationTime));
        checker.addSeconds(2L);
        Assert.assertFalse((boolean)checker.isBefore(expirationTime));
    }

    @Test
    public void testGetClaimsMap() throws InvalidJwtException, MalformedClaimException {
        String json = "{\"sub\":\"subject\",\"aud\":\"audience\",\"iss\":\"issuer\",\"jti\":\"mz3uxaCcLmQ2cwAV3oJxEQ\",\"exp\":1418906607,\"email\":\"user@somewhere.io\", \"name\":\"Joe User\", \"someclaim\":\"yup\"}";
        JwtClaims jwtClaims = JwtClaims.parse(json);
        Map<String, Object> claimsMap = jwtClaims.getClaimsMap(ReservedClaimNames.INITIAL_REGISTERED_CLAIM_NAMES);
        Assert.assertThat((Object)3, (Matcher)CoreMatchers.equalTo((Object)claimsMap.size()));
        claimsMap = jwtClaims.getClaimsMap();
        Assert.assertThat((Object)8, (Matcher)CoreMatchers.equalTo((Object)claimsMap.size()));
        Collection<String> claimNames = jwtClaims.getClaimNames(ReservedClaimNames.INITIAL_REGISTERED_CLAIM_NAMES);
        Assert.assertThat((Object)3, (Matcher)CoreMatchers.equalTo((Object)claimNames.size()));
        claimNames = jwtClaims.getClaimNames(Collections.singleton("aud"));
        Assert.assertThat((Object)7, (Matcher)CoreMatchers.equalTo((Object)claimNames.size()));
        claimNames = jwtClaims.getClaimNames();
        Assert.assertThat((Object)8, (Matcher)CoreMatchers.equalTo((Object)claimNames.size()));
        Assert.assertThat((Object)json, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)jwtClaims.getRawJson())));
    }

    @Test
    public void testGettingHelpers() throws InvalidJwtException, MalformedClaimException {
        String stringClaimName = "string";
        String stringClaimValue = "a value";
        String stringArrayClaimName = "array";
        String json = "{\"" + stringClaimName + "\":\"" + stringClaimValue + "\", \"" + stringArrayClaimName + "\":[\"one\", \"two\", \"three\"]}";
        JwtClaims claims = JwtClaims.parse(json);
        Assert.assertTrue((boolean)claims.isClaimValueOfType(stringClaimName, String.class));
        Assert.assertTrue((boolean)claims.isClaimValueString(stringClaimName));
        Assert.assertFalse((boolean)claims.isClaimValueStringList(stringClaimName));
        Assert.assertFalse((boolean)claims.isClaimValueOfType(stringClaimName, Number.class));
        Assert.assertFalse((boolean)claims.isClaimValueOfType(stringClaimName, Long.class));
        Assert.assertFalse((boolean)claims.isClaimValueOfType(stringClaimName, List.class));
        Assert.assertFalse((boolean)claims.isClaimValueOfType(stringClaimName, Boolean.class));
        Assert.assertTrue((boolean)claims.isClaimValueStringList(stringArrayClaimName));
        Assert.assertTrue((boolean)claims.isClaimValueOfType(stringArrayClaimName, List.class));
        Assert.assertFalse((boolean)claims.isClaimValueString(stringArrayClaimName));
        Assert.assertFalse((boolean)claims.isClaimValueOfType(stringArrayClaimName, String.class));
        Assert.assertFalse((boolean)claims.isClaimValueOfType(stringArrayClaimName, Number.class));
        Assert.assertFalse((boolean)claims.isClaimValueOfType(stringArrayClaimName, Long.class));
        Assert.assertTrue((boolean)claims.isClaimValueOfType(stringArrayClaimName, List.class));
        Assert.assertFalse((boolean)claims.isClaimValueOfType(stringArrayClaimName, Boolean.class));
        String nonexistentClaimName = "nope";
        Assert.assertFalse((boolean)claims.isClaimValueOfType(nonexistentClaimName, String.class));
        Assert.assertFalse((boolean)claims.isClaimValueOfType(nonexistentClaimName, List.class));
        Assert.assertFalse((boolean)claims.isClaimValueOfType(nonexistentClaimName, Boolean.class));
        Assert.assertFalse((boolean)claims.isClaimValueOfType(nonexistentClaimName, Number.class));
        Assert.assertFalse((boolean)claims.isClaimValueString(nonexistentClaimName));
        Assert.assertFalse((boolean)claims.isClaimValueStringList(nonexistentClaimName));
        Assert.assertThat((Object)stringClaimValue, (Matcher)CoreMatchers.equalTo((Object)claims.getStringClaimValue(stringClaimName)));
        Assert.assertNull((Object)claims.getStringClaimValue(nonexistentClaimName));
        Assert.assertFalse((boolean)claims.hasClaim(nonexistentClaimName));
        Assert.assertTrue((boolean)claims.getStringListClaimValue(nonexistentClaimName).isEmpty());
    }

    @Test
    public void testSomeSettingAndGettingHelpers() throws InvalidJwtException, MalformedClaimException {
        JwtClaims jcs = new JwtClaims();
        Assert.assertNull((Object)jcs.getRawJson());
        jcs.setStringClaim("s", "value");
        jcs.setStringListClaim("sa1", "a", "b", "c");
        jcs.setStringListClaim("sa2", Arrays.asList("1", "2"));
        jcs.setStringListClaim("sa3", "single");
        jcs.setStringListClaim("sa4", Collections.singletonList("single"));
        jcs.setStringListClaim("sa5", new String[0]);
        jcs.setStringListClaim("sa6", Collections.emptyList());
        jcs.setClaim("n", 16);
        jcs.setClaim("n2", 2314596000L);
        ArrayList<Object> ml = new ArrayList<Object>();
        ml.add("string");
        ml.add(47);
        ml.add("meh");
        ml.add(new String[]{"a", "B"});
        jcs.setClaim("mixed-list", ml);
        JwtClaims parsedJcs = JwtClaims.parse(jcs.toJson());
        Assert.assertThat((Object)parsedJcs.getStringClaimValue("s"), (Matcher)CoreMatchers.equalTo((Object)"value"));
        Assert.assertTrue((boolean)parsedJcs.hasClaim("s"));
        Assert.assertNotNull((Object)parsedJcs.getClaimValue("s"));
        Assert.assertThat(parsedJcs.getStringListClaimValue("sa1"), (Matcher)CoreMatchers.equalTo(Arrays.asList("a", "b", "c")));
        Assert.assertThat(parsedJcs.getStringListClaimValue("sa2"), (Matcher)CoreMatchers.equalTo(Arrays.asList("1", "2")));
        Assert.assertThat(parsedJcs.getStringListClaimValue("sa3"), (Matcher)CoreMatchers.equalTo(Collections.singletonList("single")));
        Assert.assertThat(parsedJcs.getStringListClaimValue("sa4"), (Matcher)CoreMatchers.equalTo(Collections.singletonList("single")));
        Assert.assertThat(parsedJcs.getStringListClaimValue("sa5"), (Matcher)CoreMatchers.equalTo(Collections.emptyList()));
        Assert.assertThat(parsedJcs.getStringListClaimValue("sa6"), (Matcher)CoreMatchers.equalTo(Collections.emptyList()));
        Assert.assertTrue((boolean)parsedJcs.getStringListClaimValue("nope").isEmpty());
        Assert.assertNull((Object)parsedJcs.getStringClaimValue("nope"));
        Assert.assertNull((Object)parsedJcs.getClaimValue("nope", Boolean.class));
        Assert.assertFalse((boolean)parsedJcs.hasClaim("nope"));
        Assert.assertNull((Object)parsedJcs.getClaimValue("nope"));
        Assert.assertTrue((boolean)parsedJcs.isClaimValueOfType("n", Number.class));
        Number n = jcs.getClaimValue("n", Number.class);
        Assert.assertThat((Object)16, (Matcher)CoreMatchers.equalTo((Object)n.intValue()));
        Assert.assertThat((Object)16L, (Matcher)CoreMatchers.equalTo((Object)n.longValue()));
        Assert.assertTrue((boolean)parsedJcs.isClaimValueOfType("n2", Number.class));
        Number n2 = jcs.getClaimValue("n2", Number.class);
        Assert.assertThat((Object)2314596000L, (Matcher)CoreMatchers.equalTo((Object)n2.longValue()));
        Assert.assertFalse((boolean)parsedJcs.isClaimValueStringList("mixed-list"));
        Assert.assertTrue((boolean)parsedJcs.isClaimValueOfType("mixed-list", List.class));
        Assert.assertThat((Object)4, (Matcher)CoreMatchers.equalTo((Object)parsedJcs.getClaimValue("mixed-list", List.class).size()));
    }

    @Test
    public void testFlattenClaims() throws InvalidJwtException, MalformedClaimException {
        String j = "{\n\"a\":\"av\",\n\"b\":false,\n\"c\":{\"cc1\":\"ccv1\",\"cc2\":\"ccv2\",\"cc3\":[\"a\",\"b\",\"c\"],\"cc4\":true},\n\"d\":[\"dv1\",\"dv2\",{\"dx\":\"123\"},{\"dxx\":\"abc\"},\"dvlast\"],\n\"e\":2.71828,\n\"f\":{\"fa\":{\"fb\":{\"fc\":\"value\", \"fc2\":{\"fd\":\"ddd\"}, \"fc3\":\"value3\"}}},\n}";
        JwtClaims jcs = JwtClaims.parse(j);
        Map<String, List<Object>> claims = jcs.flattenClaims(Collections.emptySet());
        Assert.assertThat((Object)"av", (Matcher)CoreMatchers.equalTo((Object)claims.get("a").get(0)));
        Assert.assertThat((Object)1, (Matcher)CoreMatchers.equalTo((Object)claims.get("a").size()));
        Assert.assertThat((Object)false, (Matcher)CoreMatchers.equalTo((Object)claims.get("b").get(0)));
        Assert.assertThat((Object)1, (Matcher)CoreMatchers.equalTo((Object)claims.get("b").size()));
        Assert.assertNull(claims.get("c"));
        Assert.assertThat((Object)"ccv1", (Matcher)CoreMatchers.equalTo((Object)claims.get("c.cc1").get(0)));
        Assert.assertThat((Object)1, (Matcher)CoreMatchers.equalTo((Object)claims.get("c.cc1").size()));
        Assert.assertThat((Object)"ccv2", (Matcher)CoreMatchers.equalTo((Object)claims.get("c.cc2").get(0)));
        Assert.assertThat((Object)1, (Matcher)CoreMatchers.equalTo((Object)claims.get("c.cc2").size()));
        Assert.assertThat((Object)"a", (Matcher)CoreMatchers.equalTo((Object)claims.get("c.cc3").get(0)));
        Assert.assertThat((Object)"b", (Matcher)CoreMatchers.equalTo((Object)claims.get("c.cc3").get(1)));
        Assert.assertThat((Object)"c", (Matcher)CoreMatchers.equalTo((Object)claims.get("c.cc3").get(2)));
        Assert.assertThat((Object)3, (Matcher)CoreMatchers.equalTo((Object)claims.get("c.cc3").size()));
        Assert.assertThat((Object)true, (Matcher)CoreMatchers.equalTo((Object)claims.get("c.cc4").get(0)));
        Assert.assertThat((Object)1, (Matcher)CoreMatchers.equalTo((Object)claims.get("c.cc4").size()));
        Assert.assertThat((Object)"123", (Matcher)CoreMatchers.equalTo((Object)claims.get("d.dx").get(0)));
        Assert.assertThat((Object)"abc", (Matcher)CoreMatchers.equalTo((Object)claims.get("d.dxx").get(0)));
        Assert.assertThat((Object)"dv1", (Matcher)CoreMatchers.equalTo((Object)claims.get("d").get(0)));
        Assert.assertThat((Object)"dv2", (Matcher)CoreMatchers.equalTo((Object)claims.get("d").get(1)));
        Assert.assertThat((Object)"dvlast", (Matcher)CoreMatchers.equalTo((Object)claims.get("d").get(2)));
        Assert.assertThat((Object)2.71828, (Matcher)CoreMatchers.equalTo((Object)claims.get("e").get(0)));
        Assert.assertThat((Object)"value", (Matcher)CoreMatchers.equalTo((Object)claims.get("f.fa.fb.fc").get(0)));
        Assert.assertThat((Object)"ddd", (Matcher)CoreMatchers.equalTo((Object)claims.get("f.fa.fb.fc2.fd").get(0)));
        Assert.assertThat((Object)"value3", (Matcher)CoreMatchers.equalTo((Object)claims.get("f.fa.fb.fc3").get(0)));
    }

    @Test
    public void testFlattenClaimsOpenIdAddress() throws InvalidJwtException, MalformedClaimException {
        String j = "  {\n   \"address\": {\n     \"street_address\": \"1234 Hollywood Blvd.\",\n     \"locality\": \"Los Angeles\",\n     \"region\": \"CA\",\n     \"postal_code\": \"90210\",\n     \"country\": \"US\"},\n   \"phone_number\": \"+1 (310) 123-4567\"\n  }";
        JwtClaims jcs = JwtClaims.parse(j);
        Map<String, List<Object>> claims = jcs.flattenClaims();
        for (String k : claims.keySet()) {
            Assert.assertThat((Object)1, (Matcher)CoreMatchers.equalTo((Object)claims.get(k).size()));
        }
        Assert.assertThat((Object)"1234 Hollywood Blvd.", (Matcher)CoreMatchers.equalTo((Object)claims.get("address.street_address").get(0)));
        Assert.assertThat((Object)"Los Angeles", (Matcher)CoreMatchers.equalTo((Object)claims.get("address.locality").get(0)));
        Assert.assertThat((Object)"CA", (Matcher)CoreMatchers.equalTo((Object)claims.get("address.region").get(0)));
        Assert.assertThat((Object)"90210", (Matcher)CoreMatchers.equalTo((Object)claims.get("address.postal_code").get(0)));
        Assert.assertThat((Object)"US", (Matcher)CoreMatchers.equalTo((Object)claims.get("address.country").get(0)));
        Assert.assertThat((Object)"+1 (310) 123-4567", (Matcher)CoreMatchers.equalTo((Object)claims.get("phone_number").get(0)));
    }

    @Test
    public void testSimpleClaimsExampleFromDraft() throws InvalidJwtException, MalformedClaimException {
        String json = "     {\"iss\":\"joe\",\n      \"exp\":1300819380,\n      \"http://example.com/is_root\":true}";
        JwtClaims jcs = JwtClaims.parse(json);
        Assert.assertThat((Object)"joe", (Matcher)CoreMatchers.equalTo((Object)jcs.getIssuer()));
        Assert.assertThat((Object)NumericDate.fromSeconds(1300819380L), (Matcher)CoreMatchers.equalTo((Object)jcs.getExpirationTime()));
        Assert.assertTrue((boolean)jcs.getClaimValue("http://example.com/is_root", Boolean.class));
    }

    @Test
    public void testNonIntegerNumericDates() throws InvalidJwtException, MalformedClaimException {
        JwtClaims jcs = JwtClaims.parse("{\"sub\":\"brian.d.campbell\", \"nbf\":1430602000.173, \"iat\":1430602060.5, \"exp\":1430602600.77}");
        Assert.assertThat((Object)NumericDate.fromSeconds(1430602600L), (Matcher)CoreMatchers.equalTo((Object)jcs.getExpirationTime()));
        Assert.assertThat((Object)NumericDate.fromSeconds(1430602060L), (Matcher)CoreMatchers.equalTo((Object)jcs.getIssuedAt()));
        Assert.assertThat((Object)NumericDate.fromSeconds(1430602000L), (Matcher)CoreMatchers.equalTo((Object)jcs.getNotBefore()));
    }
}

