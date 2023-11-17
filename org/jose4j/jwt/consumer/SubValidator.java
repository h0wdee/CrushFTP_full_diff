/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt.consumer;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwt.consumer.Validator;

public class SubValidator
implements Validator {
    private boolean requireSubject;
    private String expectedSubject;

    public SubValidator(boolean requireSubject) {
        this.requireSubject = requireSubject;
    }

    public SubValidator(String expectedSubject) {
        this(true);
        this.expectedSubject = expectedSubject;
    }

    @Override
    public String validate(JwtContext jwtContext) throws MalformedClaimException {
        JwtClaims jwtClaims = jwtContext.getJwtClaims();
        String subject = jwtClaims.getSubject();
        if (subject == null && this.requireSubject) {
            return "No Subject (sub) claim is present.";
        }
        if (this.expectedSubject != null && !this.expectedSubject.equals(subject)) {
            return "Subject (sub) claim value (" + subject + ") doesn't match expected value of " + this.expectedSubject;
        }
        return null;
    }
}

