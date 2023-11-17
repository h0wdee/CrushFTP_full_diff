/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt.consumer;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwt.consumer.Validator;

public class NumericDateValidator
implements Validator {
    private boolean requireExp;
    private boolean requireIat;
    private boolean requireNbf;
    private NumericDate staticEvaluationTime;
    private int allowedClockSkewSeconds = 0;
    private int maxFutureValidityInMinutes = 0;

    public void setRequireExp(boolean requireExp) {
        this.requireExp = requireExp;
    }

    public void setRequireIat(boolean requireIat) {
        this.requireIat = requireIat;
    }

    public void setRequireNbf(boolean requireNbf) {
        this.requireNbf = requireNbf;
    }

    public void setEvaluationTime(NumericDate evaluationTime) {
        this.staticEvaluationTime = evaluationTime;
    }

    public void setAllowedClockSkewSeconds(int allowedClockSkewSeconds) {
        this.allowedClockSkewSeconds = allowedClockSkewSeconds;
    }

    public void setMaxFutureValidityInMinutes(int maxFutureValidityInMinutes) {
        this.maxFutureValidityInMinutes = maxFutureValidityInMinutes;
    }

    @Override
    public String validate(JwtContext jwtContext) throws MalformedClaimException {
        NumericDate evaluationTime;
        JwtClaims jwtClaims = jwtContext.getJwtClaims();
        NumericDate expirationTime = jwtClaims.getExpirationTime();
        NumericDate issuedAt = jwtClaims.getIssuedAt();
        NumericDate notBefore = jwtClaims.getNotBefore();
        if (this.requireExp && expirationTime == null) {
            return "No Expiration Time (exp) claim present.";
        }
        if (this.requireIat && issuedAt == null) {
            return "No Issued At (iat) claim present.";
        }
        if (this.requireNbf && notBefore == null) {
            return "No Not Before (nbf) claim present.";
        }
        NumericDate numericDate = evaluationTime = this.staticEvaluationTime == null ? NumericDate.now() : this.staticEvaluationTime;
        if (expirationTime != null) {
            long deltaInSeconds;
            if (evaluationTime.getValue() - (long)this.allowedClockSkewSeconds >= expirationTime.getValue()) {
                return "The JWT is no longer valid - the evaluation time " + evaluationTime + " is on or after the Expiration Time (exp=" + expirationTime + ") claim value" + this.skewMessage();
            }
            if (issuedAt != null && expirationTime.isBefore(issuedAt)) {
                return "The Expiration Time (exp=" + expirationTime + ") claim value cannot be before the Issued At (iat=" + issuedAt + ") claim value.";
            }
            if (notBefore != null && expirationTime.isBefore(notBefore)) {
                return "The Expiration Time (exp=" + expirationTime + ") claim value cannot be before the Not Before (nbf=" + notBefore + ") claim value.";
            }
            if (this.maxFutureValidityInMinutes > 0 && (deltaInSeconds = expirationTime.getValue() - (long)this.allowedClockSkewSeconds - evaluationTime.getValue()) > (long)(this.maxFutureValidityInMinutes * 60)) {
                return "The Expiration Time (exp=" + expirationTime + ") claim value cannot be more than " + this.maxFutureValidityInMinutes + " minutes in the future relative to the evaluation time " + evaluationTime + this.skewMessage();
            }
        }
        if (notBefore != null && evaluationTime.getValue() + (long)this.allowedClockSkewSeconds < notBefore.getValue()) {
            return "The JWT is not yet valid as the evaluation time " + evaluationTime + " is before the Not Before (nbf=" + notBefore + ") claim time" + this.skewMessage();
        }
        return null;
    }

    private String skewMessage() {
        if (this.allowedClockSkewSeconds > 0) {
            return " (even when providing " + this.allowedClockSkewSeconds + " seconds of leeway to account for clock skew).";
        }
        return ".";
    }
}

