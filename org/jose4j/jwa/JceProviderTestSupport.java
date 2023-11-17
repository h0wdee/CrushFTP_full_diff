/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bouncycastle.jce.provider.BouncyCastleProvider
 */
package org.jose4j.jwa;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.jwa.AlgorithmFactory;
import org.jose4j.jwa.AlgorithmFactoryFactory;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.KeyManagementAlgorithm;
import org.jose4j.jws.JsonWebSignatureAlgorithm;

public class JceProviderTestSupport {
    private boolean putBouncyCastleFirst = true;
    private boolean useBouncyCastleRegardlessOfAlgs;
    private boolean doReinitialize = true;
    private Set<String> signatureAlgs = Collections.emptySet();
    private Set<String> keyManagementAlgs = Collections.emptySet();
    private Set<String> encryptionAlgs = Collections.emptySet();

    private void reinitialize() {
        AlgorithmFactoryFactory.getInstance().reinitialize();
    }

    public void runWithBouncyCastleProviderIfNeeded(RunnableTest test) throws Exception {
        AlgorithmFactory<ContentEncryptionAlgorithm> jweEncAlgFactory;
        AlgorithmFactory<KeyManagementAlgorithm> jweKeyMgmtAlgorithmFactory;
        BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
        boolean needBouncyCastle = this.useBouncyCastleRegardlessOfAlgs;
        AlgorithmFactoryFactory aff = AlgorithmFactoryFactory.getInstance();
        AlgorithmFactory<JsonWebSignatureAlgorithm> jwsAlgorithmFactory = aff.getJwsAlgorithmFactory();
        if (!jwsAlgorithmFactory.getSupportedAlgorithms().containsAll(this.signatureAlgs)) {
            needBouncyCastle = true;
        }
        if (!(jweKeyMgmtAlgorithmFactory = aff.getJweKeyManagementAlgorithmFactory()).getSupportedAlgorithms().containsAll(this.keyManagementAlgs)) {
            needBouncyCastle = true;
        }
        if (!(jweEncAlgFactory = aff.getJweContentEncryptionAlgorithmFactory()).getSupportedAlgorithms().containsAll(this.encryptionAlgs)) {
            needBouncyCastle = true;
        }
        boolean removeBouncyCastle = true;
        try {
            if (needBouncyCastle) {
                int targetPosition = this.putBouncyCastleFirst ? 1 : Security.getProviders().length + 1;
                int position = Security.insertProviderAt((Provider)bouncyCastleProvider, targetPosition);
                boolean bl = removeBouncyCastle = position != -1;
                if (this.doReinitialize) {
                    this.reinitialize();
                }
            }
            test.runTest();
        }
        finally {
            if (needBouncyCastle) {
                if (removeBouncyCastle) {
                    Security.removeProvider(bouncyCastleProvider.getName());
                }
                if (this.doReinitialize) {
                    this.reinitialize();
                }
            }
        }
    }

    public void setSignatureAlgsNeeded(String ... algs) {
        this.signatureAlgs = new HashSet<String>(Arrays.asList(algs));
    }

    public void setKeyManagementAlgsNeeded(String ... algs) {
        this.keyManagementAlgs = new HashSet<String>(Arrays.asList(algs));
    }

    public void setEncryptionAlgsNeeded(String ... algs) {
        this.encryptionAlgs = new HashSet<String>(Arrays.asList(algs));
    }

    public void setDoReinitialize(boolean doReinitialize) {
        this.doReinitialize = doReinitialize;
    }

    public void setUseBouncyCastleRegardlessOfAlgs(boolean useBouncyCastleRegardlessOfAlgs) {
        this.useBouncyCastleRegardlessOfAlgs = useBouncyCastleRegardlessOfAlgs;
    }

    public void setPutBouncyCastleFirst(boolean putBouncyCastleFirst) {
        this.putBouncyCastleFirst = putBouncyCastleFirst;
    }

    public static interface RunnableTest {
        public void runTest() throws Exception;
    }
}

