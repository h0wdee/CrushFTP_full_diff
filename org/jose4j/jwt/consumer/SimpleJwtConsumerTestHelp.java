/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.junit.Assert
 */
package org.jose4j.jwt.consumer;

import java.util.Collections;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtContext;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleJwtConsumerTestHelp {
    private static final Logger log = LoggerFactory.getLogger(SimpleJwtConsumerTestHelp.class);

    static void expectProcessingFailure(String jwt, JwtConsumer jwtConsumer) {
        SimpleJwtConsumerTestHelp.expectProcessingFailure(jwt, null, jwtConsumer);
    }

    static void expectProcessingFailure(String jwt, JwtContext jwtContext, JwtConsumer jwtConsumer) {
        try {
            jwtConsumer.process(jwt);
            Assert.fail((String)"jwt process/validation should have thrown an exception");
        }
        catch (InvalidJwtException e) {
            log.debug("Expected exception: {}", (Object)e.toString());
        }
        if (jwtContext != null) {
            try {
                jwtConsumer.processContext(jwtContext);
                Assert.fail((String)"jwt context process/validation should have thrown an exception");
            }
            catch (InvalidJwtException e) {
                log.debug("Expected exception: {}", (Object)e.toString());
            }
        }
    }

    static void goodValidate(JwtClaims jwtClaims, JwtConsumer jwtConsumer) throws InvalidJwtException {
        jwtConsumer.validate(new JwtContext(jwtClaims, Collections.emptyList()));
    }

    static void expectValidationFailure(JwtClaims jwtClaims, JwtConsumer jwtConsumer) {
        try {
            jwtConsumer.validate(new JwtContext(jwtClaims, Collections.emptyList()));
            Assert.fail((String)"claims validation should have thrown an exception");
        }
        catch (InvalidJwtException e) {
            log.debug("Expected exception: {}", (Object)e.toString());
        }
    }
}

