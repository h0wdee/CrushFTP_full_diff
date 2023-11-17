/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt.consumer;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.AudValidator;
import org.jose4j.jwt.consumer.IssValidator;
import org.jose4j.jwt.consumer.JtiValidator;
import org.jose4j.jwt.consumer.JweCustomizer;
import org.jose4j.jwt.consumer.JwsCustomizer;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.NumericDateValidator;
import org.jose4j.jwt.consumer.SimpleKeyResolver;
import org.jose4j.jwt.consumer.SubValidator;
import org.jose4j.jwt.consumer.Validator;
import org.jose4j.keys.resolvers.DecryptionKeyResolver;
import org.jose4j.keys.resolvers.VerificationKeyResolver;

public class JwtConsumerBuilder {
    private VerificationKeyResolver verificationKeyResolver = new SimpleKeyResolver(null);
    private DecryptionKeyResolver decryptionKeyResolver = new SimpleKeyResolver(null);
    private AlgorithmConstraints jwsAlgorithmConstraints;
    private AlgorithmConstraints jweAlgorithmConstraints;
    private AlgorithmConstraints jweContentEncryptionAlgorithmConstraints;
    private boolean skipDefaultAudienceValidation;
    private AudValidator audValidator;
    private IssValidator issValidator;
    private boolean requireSubject;
    private String expectedSubject;
    private boolean requireJti;
    private NumericDateValidator dateClaimsValidator = new NumericDateValidator();
    private List<Validator> customValidators = new ArrayList<Validator>();
    private boolean requireSignature = true;
    private boolean requireEncryption;
    private boolean skipSignatureVerification = false;
    private boolean relaxVerificationKeyValidation;
    private boolean relaxDecryptionKeyValidation;
    private boolean skipAllValidators = false;
    private boolean skipAllDefaultValidators = false;
    private boolean liberalContentTypeHandling;
    private ProviderContext jwsProviderContext;
    private ProviderContext jweProviderContext;
    private JwsCustomizer jwsCustomizer;
    private JweCustomizer jweCustomizer;

    public JwtConsumerBuilder setEnableRequireEncryption() {
        this.requireEncryption = true;
        return this;
    }

    public JwtConsumerBuilder setDisableRequireSignature() {
        this.requireSignature = false;
        return this;
    }

    public JwtConsumerBuilder setEnableLiberalContentTypeHandling() {
        this.liberalContentTypeHandling = true;
        return this;
    }

    public JwtConsumerBuilder setSkipSignatureVerification() {
        this.skipSignatureVerification = true;
        return this;
    }

    public JwtConsumerBuilder setSkipAllValidators() {
        this.skipAllValidators = true;
        return this;
    }

    public JwtConsumerBuilder setSkipAllDefaultValidators() {
        this.skipAllDefaultValidators = true;
        return this;
    }

    public JwtConsumerBuilder setJwsAlgorithmConstraints(AlgorithmConstraints constraints) {
        this.jwsAlgorithmConstraints = constraints;
        return this;
    }

    public JwtConsumerBuilder setJweAlgorithmConstraints(AlgorithmConstraints constraints) {
        this.jweAlgorithmConstraints = constraints;
        return this;
    }

    public JwtConsumerBuilder setJweContentEncryptionAlgorithmConstraints(AlgorithmConstraints constraints) {
        this.jweContentEncryptionAlgorithmConstraints = constraints;
        return this;
    }

    public JwtConsumerBuilder setVerificationKey(Key verificationKey) {
        return this.setVerificationKeyResolver(new SimpleKeyResolver(verificationKey));
    }

    public JwtConsumerBuilder setVerificationKeyResolver(VerificationKeyResolver verificationKeyResolver) {
        this.verificationKeyResolver = verificationKeyResolver;
        return this;
    }

    public JwtConsumerBuilder setDecryptionKey(Key decryptionKey) {
        return this.setDecryptionKeyResolver(new SimpleKeyResolver(decryptionKey));
    }

    public JwtConsumerBuilder setDecryptionKeyResolver(DecryptionKeyResolver decryptionKeyResolver) {
        this.decryptionKeyResolver = decryptionKeyResolver;
        return this;
    }

    public JwtConsumerBuilder setExpectedAudience(String ... audience) {
        return this.setExpectedAudience(true, audience);
    }

    public JwtConsumerBuilder setExpectedAudience(boolean requireAudienceClaim, String ... audience) {
        HashSet<String> acceptableAudiences = new HashSet<String>(Arrays.asList(audience));
        this.audValidator = new AudValidator(acceptableAudiences, requireAudienceClaim);
        return this;
    }

    public JwtConsumerBuilder setSkipDefaultAudienceValidation() {
        this.skipDefaultAudienceValidation = true;
        return this;
    }

    public JwtConsumerBuilder setExpectedIssuers(boolean requireIssuer, String ... expectedIssuers) {
        this.issValidator = new IssValidator(requireIssuer, expectedIssuers);
        return this;
    }

    public JwtConsumerBuilder setExpectedIssuer(boolean requireIssuer, String expectedIssuer) {
        this.issValidator = new IssValidator(expectedIssuer, requireIssuer);
        return this;
    }

    public JwtConsumerBuilder setExpectedIssuer(String expectedIssuer) {
        return this.setExpectedIssuer(true, expectedIssuer);
    }

    public JwtConsumerBuilder setRequireSubject() {
        this.requireSubject = true;
        return this;
    }

    public JwtConsumerBuilder setExpectedSubject(String subject) {
        this.expectedSubject = subject;
        return this.setRequireSubject();
    }

    public JwtConsumerBuilder setRequireJwtId() {
        this.requireJti = true;
        return this;
    }

    public JwtConsumerBuilder setRequireExpirationTime() {
        this.dateClaimsValidator.setRequireExp(true);
        return this;
    }

    public JwtConsumerBuilder setRequireIssuedAt() {
        this.dateClaimsValidator.setRequireIat(true);
        return this;
    }

    public JwtConsumerBuilder setRequireNotBefore() {
        this.dateClaimsValidator.setRequireNbf(true);
        return this;
    }

    public JwtConsumerBuilder setEvaluationTime(NumericDate evaluationTime) {
        this.dateClaimsValidator.setEvaluationTime(evaluationTime);
        return this;
    }

    public JwtConsumerBuilder setAllowedClockSkewInSeconds(int secondsOfAllowedClockSkew) {
        this.dateClaimsValidator.setAllowedClockSkewSeconds(secondsOfAllowedClockSkew);
        return this;
    }

    public JwtConsumerBuilder setMaxFutureValidityInMinutes(int maxFutureValidityInMinutes) {
        this.dateClaimsValidator.setMaxFutureValidityInMinutes(maxFutureValidityInMinutes);
        return this;
    }

    public JwtConsumerBuilder setRelaxVerificationKeyValidation() {
        this.relaxVerificationKeyValidation = true;
        return this;
    }

    public JwtConsumerBuilder setRelaxDecryptionKeyValidation() {
        this.relaxDecryptionKeyValidation = true;
        return this;
    }

    public JwtConsumerBuilder registerValidator(Validator validator) {
        this.customValidators.add(validator);
        return this;
    }

    public JwtConsumerBuilder setJwsCustomizer(JwsCustomizer jwsCustomizer) {
        this.jwsCustomizer = jwsCustomizer;
        return this;
    }

    public JwtConsumerBuilder setJweCustomizer(JweCustomizer jweCustomizer) {
        this.jweCustomizer = jweCustomizer;
        return this;
    }

    public JwtConsumerBuilder setJwsProviderContext(ProviderContext jwsProviderContext) {
        this.jwsProviderContext = jwsProviderContext;
        return this;
    }

    public JwtConsumerBuilder setJweProviderContext(ProviderContext jweProviderContext) {
        this.jweProviderContext = jweProviderContext;
        return this;
    }

    public JwtConsumer build() {
        ArrayList<Validator> validators = new ArrayList<Validator>();
        if (!this.skipAllValidators) {
            if (!this.skipAllDefaultValidators) {
                if (!this.skipDefaultAudienceValidation) {
                    if (this.audValidator == null) {
                        this.audValidator = new AudValidator(Collections.emptySet(), false);
                    }
                    validators.add(this.audValidator);
                }
                if (this.issValidator == null) {
                    this.issValidator = new IssValidator(null, false);
                }
                validators.add(this.issValidator);
                validators.add(this.dateClaimsValidator);
                SubValidator subValidator = this.expectedSubject == null ? new SubValidator(this.requireSubject) : new SubValidator(this.expectedSubject);
                validators.add(subValidator);
                validators.add(new JtiValidator(this.requireJti));
            }
            validators.addAll(this.customValidators);
        }
        JwtConsumer jwtConsumer = new JwtConsumer();
        jwtConsumer.setValidators(validators);
        jwtConsumer.setVerificationKeyResolver(this.verificationKeyResolver);
        jwtConsumer.setDecryptionKeyResolver(this.decryptionKeyResolver);
        jwtConsumer.setJwsAlgorithmConstraints(this.jwsAlgorithmConstraints);
        jwtConsumer.setJweAlgorithmConstraints(this.jweAlgorithmConstraints);
        jwtConsumer.setJweContentEncryptionAlgorithmConstraints(this.jweContentEncryptionAlgorithmConstraints);
        jwtConsumer.setRequireSignature(this.requireSignature);
        jwtConsumer.setRequireEncryption(this.requireEncryption);
        jwtConsumer.setLiberalContentTypeHandling(this.liberalContentTypeHandling);
        jwtConsumer.setSkipSignatureVerification(this.skipSignatureVerification);
        jwtConsumer.setRelaxVerificationKeyValidation(this.relaxVerificationKeyValidation);
        jwtConsumer.setRelaxDecryptionKeyValidation(this.relaxDecryptionKeyValidation);
        jwtConsumer.setJwsCustomizer(this.jwsCustomizer);
        jwtConsumer.setJweCustomizer(this.jweCustomizer);
        jwtConsumer.setJwsProviderContext(this.jwsProviderContext);
        jwtConsumer.setJweProviderContext(this.jweProviderContext);
        return jwtConsumer;
    }
}

