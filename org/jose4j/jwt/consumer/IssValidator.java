/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt.consumer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwt.consumer.Validator;

public class IssValidator
implements Validator {
    private Set<String> expectedIssuers;
    private boolean requireIssuer;

    public IssValidator(String expectedIssuer, boolean requireIssuer) {
        if (expectedIssuer != null) {
            this.expectedIssuers = Collections.singleton(expectedIssuer);
        }
        this.requireIssuer = requireIssuer;
    }

    public IssValidator(boolean requireIssuer, String ... expectedIssuers) {
        this.requireIssuer = requireIssuer;
        if (expectedIssuers != null && expectedIssuers.length > 0) {
            this.expectedIssuers = new HashSet<String>();
            Collections.addAll(this.expectedIssuers, expectedIssuers);
        }
    }

    @Override
    public String validate(JwtContext jwtContext) throws MalformedClaimException {
        String issuer = jwtContext.getJwtClaims().getIssuer();
        if (issuer == null) {
            return this.requireIssuer ? "No Issuer (iss) claim present but was expecting " + this.expectedValue() : null;
        }
        if (this.expectedIssuers != null && !this.expectedIssuers.contains(issuer)) {
            return "Issuer (iss) claim value (" + issuer + ") doesn't match expected value of " + this.expectedValue();
        }
        return null;
    }

    private String expectedValue() {
        return this.expectedIssuers.size() == 1 ? this.expectedIssuers.iterator().next() : "one of " + this.expectedIssuers;
    }
}

