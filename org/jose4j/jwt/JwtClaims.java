/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jose4j.base64url.Base64Url;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;

public class JwtClaims {
    private Map<String, Object> claimsMap;
    private String rawJson;

    public JwtClaims() {
        this.claimsMap = new LinkedHashMap<String, Object>();
    }

    private JwtClaims(String jsonClaims) throws InvalidJwtException {
        this.rawJson = jsonClaims;
        try {
            Map<String, Object> parsed = JsonUtil.parseJson(jsonClaims);
            this.claimsMap = new LinkedHashMap<String, Object>(parsed);
        }
        catch (JoseException e) {
            throw new InvalidJwtException("Unable to parse JWT Claim Set JSON: " + jsonClaims, e);
        }
    }

    public static JwtClaims parse(String jsonClaims) throws InvalidJwtException {
        return new JwtClaims(jsonClaims);
    }

    public String getIssuer() throws MalformedClaimException {
        return this.getClaimValue("iss", String.class);
    }

    public void setIssuer(String issuer) {
        this.claimsMap.put("iss", issuer);
    }

    public String getSubject() throws MalformedClaimException {
        return this.getClaimValue("sub", String.class);
    }

    public void setSubject(String subject) {
        this.claimsMap.put("sub", subject);
    }

    public void setAudience(String audience) {
        this.claimsMap.put("aud", audience);
    }

    public void setAudience(String ... audience) {
        this.setAudience(Arrays.asList(audience));
    }

    public void setAudience(List<String> audiences) {
        if (audiences.size() == 1) {
            this.setAudience(audiences.get(0));
        } else {
            this.claimsMap.put("aud", audiences);
        }
    }

    public boolean hasAudience() {
        return this.hasClaim("aud");
    }

    public List<String> getAudience() throws MalformedClaimException {
        Object audienceObject = this.claimsMap.get("aud");
        if (audienceObject instanceof String) {
            return Collections.singletonList((String)audienceObject);
        }
        if (audienceObject instanceof List || audienceObject == null) {
            List audienceList = (List)audienceObject;
            String claimName = "aud";
            return this.toStringList(audienceList, claimName);
        }
        throw new MalformedClaimException("The value of the 'aud' claim is not an array of strings or a single string value.");
    }

    private List<String> toStringList(List list, String claimName) throws MalformedClaimException {
        if (list == null) {
            return Collections.emptyList();
        }
        ArrayList<String> values = new ArrayList<String>();
        for (Object object : list) {
            try {
                values.add((String)object);
            }
            catch (ClassCastException e) {
                throw new MalformedClaimException("The array value of the '" + claimName + "' claim contains non string values " + this.classCastMsg(e, object), e);
            }
        }
        return values;
    }

    public NumericDate getExpirationTime() throws MalformedClaimException {
        return this.getNumericDateClaimValue("exp");
    }

    public void setExpirationTime(NumericDate expirationTime) {
        this.setNumericDateClaim("exp", expirationTime);
    }

    public void setExpirationTimeMinutesInTheFuture(float minutes) {
        this.setExpirationTime(this.offsetFromNow(minutes));
    }

    private NumericDate offsetFromNow(float offsetMinutes) {
        NumericDate numericDate = NumericDate.now();
        float secondsOffset = offsetMinutes * 60.0f;
        numericDate.addSeconds((long)secondsOffset);
        return numericDate;
    }

    public NumericDate getNotBefore() throws MalformedClaimException {
        return this.getNumericDateClaimValue("nbf");
    }

    public void setNotBefore(NumericDate notBefore) {
        this.setNumericDateClaim("nbf", notBefore);
    }

    public void setNotBeforeMinutesInThePast(float minutes) {
        this.setNotBefore(this.offsetFromNow(-1.0f * minutes));
    }

    public NumericDate getIssuedAt() throws MalformedClaimException {
        return this.getNumericDateClaimValue("iat");
    }

    public void setIssuedAt(NumericDate issuedAt) {
        this.setNumericDateClaim("iat", issuedAt);
    }

    public void setIssuedAtToNow() {
        this.setIssuedAt(NumericDate.now());
    }

    public String getJwtId() throws MalformedClaimException {
        return this.getClaimValue("jti", String.class);
    }

    public void setJwtId(String jwtId) {
        this.claimsMap.put("jti", jwtId);
    }

    public void setGeneratedJwtId(int numberOfBytes) {
        byte[] rndbytes = ByteUtil.randomBytes(numberOfBytes);
        String jti = Base64Url.encode(rndbytes);
        this.setJwtId(jti);
    }

    public void setGeneratedJwtId() {
        this.setGeneratedJwtId(16);
    }

    public void unsetClaim(String claimName) {
        this.claimsMap.remove(claimName);
    }

    public <T> T getClaimValue(String claimName, Class<T> type) throws MalformedClaimException {
        Object o = this.claimsMap.get(claimName);
        try {
            return type.cast(o);
        }
        catch (ClassCastException e) {
            throw new MalformedClaimException("The value of the '" + claimName + "' claim is not the expected type " + this.classCastMsg(e, o), e);
        }
    }

    public Object getClaimValue(String claimName) {
        return this.claimsMap.get(claimName);
    }

    public boolean hasClaim(String claimName) {
        return this.getClaimValue(claimName) != null;
    }

    private String classCastMsg(ClassCastException e, Object o) {
        return "(" + o + " - " + e.getMessage() + ")";
    }

    public NumericDate getNumericDateClaimValue(String claimName) throws MalformedClaimException {
        Number number = this.getClaimValue(claimName, Number.class);
        return number != null ? NumericDate.fromSeconds(number.longValue()) : null;
    }

    public String getStringClaimValue(String claimName) throws MalformedClaimException {
        return this.getClaimValue(claimName, String.class);
    }

    public List<String> getStringListClaimValue(String claimName) throws MalformedClaimException {
        List listClaimValue = this.getClaimValue(claimName, List.class);
        return this.toStringList(listClaimValue, claimName);
    }

    public void setNumericDateClaim(String claimName, NumericDate value) {
        this.claimsMap.put(claimName, value != null ? Long.valueOf(value.getValue()) : null);
    }

    public void setStringClaim(String claimName, String value) {
        this.claimsMap.put(claimName, value);
    }

    public void setStringListClaim(String claimName, List<String> values) {
        this.claimsMap.put(claimName, values);
    }

    public void setStringListClaim(String claimName, String ... values) {
        this.claimsMap.put(claimName, Arrays.asList(values));
    }

    public void setClaim(String claimName, Object value) {
        this.claimsMap.put(claimName, value);
    }

    public boolean isClaimValueOfType(String claimName, Class type) {
        try {
            return this.getClaimValue(claimName, type) != null;
        }
        catch (MalformedClaimException e) {
            return false;
        }
    }

    public boolean isClaimValueString(String claimName) {
        return this.isClaimValueOfType(claimName, String.class);
    }

    public boolean isClaimValueStringList(String claimName) {
        try {
            return this.hasClaim(claimName) && this.getStringListClaimValue(claimName) != null;
        }
        catch (MalformedClaimException e) {
            return false;
        }
    }

    public Map<String, List<Object>> flattenClaims() {
        return this.flattenClaims(null);
    }

    public Map<String, List<Object>> flattenClaims(Set<String> omittedClaims) {
        omittedClaims = omittedClaims == null ? Collections.emptySet() : omittedClaims;
        LinkedHashMap<String, List<Object>> flattenedClaims = new LinkedHashMap<String, List<Object>>();
        for (Map.Entry<String, Object> e : this.claimsMap.entrySet()) {
            String key = e.getKey();
            if (omittedClaims.contains(key)) continue;
            this.dfs(null, key, e.getValue(), flattenedClaims);
        }
        return flattenedClaims;
    }

    private void dfs(String baseName, String name, Object value, Map<String, List<Object>> flattenedClaims) {
        String key = String.valueOf(baseName == null ? "" : String.valueOf(baseName) + ".") + name;
        if (value instanceof List) {
            ArrayList newList = new ArrayList();
            for (Object item : (List)value) {
                if (item instanceof Map) {
                    Map mv = (Map)item;
                    for (Map.Entry e : mv.entrySet()) {
                        this.dfs(key, e.getKey().toString(), e.getValue(), flattenedClaims);
                    }
                    continue;
                }
                newList.add(item);
            }
            flattenedClaims.put(key, newList);
        } else if (value instanceof Map) {
            Map mapValue = (Map)value;
            for (Map.Entry e : mapValue.entrySet()) {
                this.dfs(key, e.getKey().toString(), e.getValue(), flattenedClaims);
            }
        } else {
            flattenedClaims.put(key, Collections.singletonList(value));
        }
    }

    public Map<String, Object> getClaimsMap(Set<String> omittedClaims) {
        omittedClaims = omittedClaims != null ? omittedClaims : Collections.emptySet();
        LinkedHashMap<String, Object> claims = new LinkedHashMap<String, Object>(this.claimsMap);
        for (String omittedClaim : omittedClaims) {
            claims.remove(omittedClaim);
        }
        return claims;
    }

    public Map<String, Object> getClaimsMap() {
        return this.getClaimsMap(null);
    }

    public Collection<String> getClaimNames(Set<String> omittedClaims) {
        return this.getClaimsMap(omittedClaims).keySet();
    }

    public Collection<String> getClaimNames() {
        return this.getClaimNames(null);
    }

    public String toJson() {
        return JsonUtil.toJson(this.claimsMap);
    }

    public String getRawJson() {
        return this.rawJson;
    }

    public String toString() {
        return "JWT Claims Set:" + this.claimsMap;
    }
}

