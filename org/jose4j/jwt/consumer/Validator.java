/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt.consumer;

import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;

public interface Validator {
    public String validate(JwtContext var1) throws MalformedClaimException;
}

