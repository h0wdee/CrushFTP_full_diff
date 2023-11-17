/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt.consumer;

import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwt.consumer.Validator;

public class JtiValidator
implements Validator {
    private boolean requireJti;

    public JtiValidator(boolean requireJti) {
        this.requireJti = requireJti;
    }

    @Override
    public String validate(JwtContext jwtContext) throws MalformedClaimException {
        String subject = jwtContext.getJwtClaims().getJwtId();
        return subject == null && this.requireJti ? "The JWT ID (jti) claim is not present." : null;
    }
}

