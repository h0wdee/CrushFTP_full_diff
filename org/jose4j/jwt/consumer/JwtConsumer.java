/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt.consumer;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.jose4j.jca.ProviderContext;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.InvalidJwtSignatureException;
import org.jose4j.jwt.consumer.JweCustomizer;
import org.jose4j.jwt.consumer.JwsCustomizer;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwt.consumer.Validator;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.resolvers.DecryptionKeyResolver;
import org.jose4j.keys.resolvers.VerificationKeyResolver;
import org.jose4j.lang.ExceptionHelp;
import org.jose4j.lang.JoseException;

public class JwtConsumer {
    private VerificationKeyResolver verificationKeyResolver;
    private DecryptionKeyResolver decryptionKeyResolver;
    private List<Validator> validators;
    private AlgorithmConstraints jwsAlgorithmConstraints;
    private AlgorithmConstraints jweAlgorithmConstraints;
    private AlgorithmConstraints jweContentEncryptionAlgorithmConstraints;
    private boolean requireSignature = true;
    private boolean requireEncryption;
    private boolean liberalContentTypeHandling;
    private boolean skipSignatureVerification;
    private boolean relaxVerificationKeyValidation;
    private boolean relaxDecryptionKeyValidation;
    private ProviderContext jwsProviderContext;
    private ProviderContext jweProviderContext;
    private JwsCustomizer jwsCustomizer;
    private JweCustomizer jweCustomizer;

    JwtConsumer() {
    }

    void setJwsAlgorithmConstraints(AlgorithmConstraints constraints) {
        this.jwsAlgorithmConstraints = constraints;
    }

    void setJweAlgorithmConstraints(AlgorithmConstraints constraints) {
        this.jweAlgorithmConstraints = constraints;
    }

    void setJweContentEncryptionAlgorithmConstraints(AlgorithmConstraints constraints) {
        this.jweContentEncryptionAlgorithmConstraints = constraints;
    }

    void setVerificationKeyResolver(VerificationKeyResolver verificationKeyResolver) {
        this.verificationKeyResolver = verificationKeyResolver;
    }

    void setDecryptionKeyResolver(DecryptionKeyResolver decryptionKeyResolver) {
        this.decryptionKeyResolver = decryptionKeyResolver;
    }

    void setValidators(List<Validator> validators) {
        this.validators = validators;
    }

    void setRequireSignature(boolean requireSignature) {
        this.requireSignature = requireSignature;
    }

    void setRequireEncryption(boolean requireEncryption) {
        this.requireEncryption = requireEncryption;
    }

    void setLiberalContentTypeHandling(boolean liberalContentTypeHandling) {
        this.liberalContentTypeHandling = liberalContentTypeHandling;
    }

    void setSkipSignatureVerification(boolean skipSignatureVerification) {
        this.skipSignatureVerification = skipSignatureVerification;
    }

    void setRelaxVerificationKeyValidation(boolean relaxVerificationKeyValidation) {
        this.relaxVerificationKeyValidation = relaxVerificationKeyValidation;
    }

    void setRelaxDecryptionKeyValidation(boolean relaxDecryptionKeyValidation) {
        this.relaxDecryptionKeyValidation = relaxDecryptionKeyValidation;
    }

    void setJwsProviderContext(ProviderContext jwsProviderContext) {
        this.jwsProviderContext = jwsProviderContext;
    }

    void setJweProviderContext(ProviderContext jweProviderContext) {
        this.jweProviderContext = jweProviderContext;
    }

    void setJwsCustomizer(JwsCustomizer jwsCustomizer) {
        this.jwsCustomizer = jwsCustomizer;
    }

    void setJweCustomizer(JweCustomizer jweCustomizer) {
        this.jweCustomizer = jweCustomizer;
    }

    public JwtClaims processToClaims(String jwt) throws InvalidJwtException {
        return this.process(jwt).getJwtClaims();
    }

    public void processContext(JwtContext jwtContext) throws InvalidJwtException {
        boolean hasSignature = false;
        boolean hasEncryption = false;
        ArrayList<JsonWebStructure> originalJoseObjects = new ArrayList<JsonWebStructure>(jwtContext.getJoseObjects());
        int idx = originalJoseObjects.size() - 1;
        while (idx >= 0) {
            StringBuilder sb;
            List<JsonWebStructure> joseObjects = originalJoseObjects.subList(idx + 1, originalJoseObjects.size());
            List<JsonWebStructure> nestingContext = Collections.unmodifiableList(joseObjects);
            JsonWebStructure currentJoseObject = originalJoseObjects.get(idx);
            try {
                Key key;
                if (currentJoseObject instanceof JsonWebSignature) {
                    JsonWebSignature jws = (JsonWebSignature)currentJoseObject;
                    if (!this.skipSignatureVerification) {
                        if (this.jwsProviderContext != null) {
                            jws.setProviderContext(this.jwsProviderContext);
                        }
                        if (this.relaxVerificationKeyValidation) {
                            jws.setDoKeyValidation(false);
                        }
                        if (this.jwsAlgorithmConstraints != null) {
                            jws.setAlgorithmConstraints(this.jwsAlgorithmConstraints);
                        }
                        key = this.verificationKeyResolver.resolveKey(jws, nestingContext);
                        jws.setKey(key);
                        if (this.jwsCustomizer != null) {
                            this.jwsCustomizer.customize(jws, nestingContext);
                        }
                        if (!jws.verifySignature()) {
                            throw new InvalidJwtSignatureException("JWS signature is invalid: " + jws);
                        }
                    }
                    if (!currentJoseObject.getAlgorithmHeaderValue().equals("none")) {
                        hasSignature = true;
                    }
                } else {
                    JsonWebEncryption jwe = (JsonWebEncryption)currentJoseObject;
                    key = this.decryptionKeyResolver.resolveKey(jwe, nestingContext);
                    if (key != null && !key.equals(jwe.getKey())) {
                        throw new InvalidJwtException("The resolved decryption key is different than the one originally used to decrypt the JWE.");
                    }
                    if (this.jweAlgorithmConstraints != null) {
                        this.jweAlgorithmConstraints.checkConstraint(jwe.getAlgorithmHeaderValue());
                    }
                    if (this.jweContentEncryptionAlgorithmConstraints != null) {
                        this.jweContentEncryptionAlgorithmConstraints.checkConstraint(jwe.getEncryptionMethodHeaderParameter());
                    }
                    hasEncryption = true;
                }
            }
            catch (JoseException e) {
                sb = new StringBuilder();
                sb.append("Unable to process");
                if (!joseObjects.isEmpty()) {
                    sb.append(" nested");
                }
                sb.append(" JOSE object (cause: ").append(e).append("): ").append(currentJoseObject);
                throw new InvalidJwtException(sb.toString(), e);
            }
            catch (InvalidJwtException e) {
                throw e;
            }
            catch (Exception e) {
                sb = new StringBuilder();
                sb.append("Unexpected exception encountered while processing");
                if (!joseObjects.isEmpty()) {
                    sb.append(" nested");
                }
                sb.append(" JOSE object (").append(e).append("): ").append(currentJoseObject);
                throw new InvalidJwtException(sb.toString(), e);
            }
            --idx;
        }
        if (this.requireSignature && !hasSignature) {
            throw new InvalidJwtException("The JWT has no signature but the JWT Consumer is configured to require one: " + jwtContext.getJwt());
        }
        if (this.requireEncryption && !hasEncryption) {
            throw new InvalidJwtException("The JWT has no encryption but the JWT Consumer is configured to require it: " + jwtContext.getJwt());
        }
        this.validate(jwtContext);
    }

    public JwtContext process(String jwt) throws InvalidJwtException {
        String workingJwt = jwt;
        JwtClaims jwtClaims = null;
        LinkedList<JsonWebStructure> joseObjects = new LinkedList<JsonWebStructure>();
        while (jwtClaims == null) {
            StringBuilder sb;
            try {
                String payload;
                JsonWebStructure joseObject = JsonWebStructure.fromCompactSerialization(workingJwt);
                if (joseObject instanceof JsonWebSignature) {
                    JsonWebSignature jws = (JsonWebSignature)joseObject;
                    payload = jws.getUnverifiedPayload();
                } else {
                    JsonWebEncryption jwe = (JsonWebEncryption)joseObject;
                    if (this.jweProviderContext != null) {
                        jwe.setProviderContext(this.jweProviderContext);
                    }
                    if (this.relaxDecryptionKeyValidation) {
                        jwe.setDoKeyValidation(false);
                    }
                    if (this.jweContentEncryptionAlgorithmConstraints != null) {
                        jwe.setContentEncryptionAlgorithmConstraints(this.jweContentEncryptionAlgorithmConstraints);
                    }
                    List<JsonWebStructure> nestingContext = Collections.unmodifiableList(joseObjects);
                    Key key = this.decryptionKeyResolver.resolveKey(jwe, nestingContext);
                    jwe.setKey(key);
                    if (this.jweAlgorithmConstraints != null) {
                        jwe.setAlgorithmConstraints(this.jweAlgorithmConstraints);
                    }
                    if (this.jweCustomizer != null) {
                        this.jweCustomizer.customize(jwe, nestingContext);
                    }
                    payload = jwe.getPayload();
                }
                if (this.isNestedJwt(joseObject)) {
                    workingJwt = payload;
                } else {
                    try {
                        jwtClaims = JwtClaims.parse(payload);
                    }
                    catch (InvalidJwtException ije) {
                        if (this.liberalContentTypeHandling) {
                            try {
                                JsonWebStructure.fromCompactSerialization(jwt);
                                workingJwt = payload;
                            }
                            catch (JoseException je) {
                                throw ije;
                            }
                        }
                        throw ije;
                    }
                }
                joseObjects.addFirst(joseObject);
            }
            catch (JoseException e) {
                sb = new StringBuilder();
                sb.append("Unable to process");
                if (!joseObjects.isEmpty()) {
                    sb.append(" nested");
                }
                sb.append(" JOSE object (cause: ").append(e).append("): ").append(workingJwt);
                throw new InvalidJwtException(sb.toString(), e);
            }
            catch (InvalidJwtException e) {
                throw e;
            }
            catch (Exception e) {
                sb = new StringBuilder();
                sb.append("Unexpected exception encountered while processing");
                if (!joseObjects.isEmpty()) {
                    sb.append(" nested");
                }
                sb.append(" JOSE object (").append(e).append("): ").append(workingJwt);
                throw new InvalidJwtException(sb.toString(), e);
            }
        }
        JwtContext jwtContext = new JwtContext(jwt, jwtClaims, Collections.unmodifiableList(joseObjects));
        this.processContext(jwtContext);
        return jwtContext;
    }

    void validate(JwtContext jwtCtx) throws InvalidJwtException {
        ArrayList<String> issues = new ArrayList<String>();
        for (Validator validator : this.validators) {
            String validationResult;
            try {
                validationResult = validator.validate(jwtCtx);
            }
            catch (MalformedClaimException e) {
                validationResult = e.getMessage();
            }
            catch (Exception e) {
                validationResult = "Unexpected exception thrown from validator " + validator.getClass().getName() + ": " + ExceptionHelp.toStringWithCausesAndAbbreviatedStack(e, this.getClass());
            }
            if (validationResult == null) continue;
            issues.add(validationResult);
        }
        if (!issues.isEmpty()) {
            InvalidJwtException invalidJwtException = new InvalidJwtException("JWT (claims->" + jwtCtx.getJwtClaims().getRawJson() + ") rejected due to invalid claims.");
            invalidJwtException.setDetails(issues);
            throw invalidJwtException;
        }
    }

    private boolean isNestedJwt(JsonWebStructure joseObject) {
        String cty = joseObject.getContentTypeHeaderValue();
        return cty != null && (cty.equalsIgnoreCase("jwt") || cty.equalsIgnoreCase("application/jwt"));
    }
}

