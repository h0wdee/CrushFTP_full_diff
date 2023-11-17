/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt.consumer;

import java.util.List;
import java.util.Set;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwt.consumer.Validator;

public class AudValidator
implements Validator {
    private Set<String> acceptableAudiences;
    private boolean requireAudience;

    public AudValidator(Set<String> acceptableAudiences, boolean requireAudience) {
        this.acceptableAudiences = acceptableAudiences;
        this.requireAudience = requireAudience;
    }

    @Override
    public String validate(JwtContext jwtContext) throws MalformedClaimException {
        JwtClaims jwtClaims = jwtContext.getJwtClaims();
        if (!jwtClaims.hasAudience()) {
            return this.requireAudience ? "No Audience (aud) claim present." : null;
        }
        List<String> audiences = jwtClaims.getAudience();
        boolean ok = false;
        for (String audience : audiences) {
            if (!this.acceptableAudiences.contains(audience)) continue;
            ok = true;
        }
        if (!ok) {
            StringBuilder sb = new StringBuilder();
            sb.append("Audience (aud) claim ").append(audiences);
            if (this.acceptableAudiences.isEmpty()) {
                sb.append(" present in the JWT but no expected audience value(s) were provided to the JWT Consumer.");
            } else {
                sb.append(" doesn't contain an acceptable identifier.");
            }
            sb.append(" Expected ");
            if (this.acceptableAudiences.size() == 1) {
                sb.append(this.acceptableAudiences.iterator().next());
            } else {
                sb.append("one of ").append(this.acceptableAudiences);
            }
            sb.append(" as an aud value.");
            return sb.toString();
        }
        return null;
    }
}

