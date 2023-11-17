/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jca;

import java.security.SecureRandom;

public class ProviderContext {
    private SecureRandom secureRandom;
    private Context suppliedKeyProviderContext = new Context();
    private Context generalProviderContext = new Context();

    public Context getSuppliedKeyProviderContext() {
        return this.suppliedKeyProviderContext;
    }

    public Context getGeneralProviderContext() {
        return this.generalProviderContext;
    }

    public SecureRandom getSecureRandom() {
        return this.secureRandom;
    }

    public void setSecureRandom(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    public class Context {
        private String generalProvider;
        private String keyPairGeneratorProvider;
        private String keyAgreementProvider;
        private String cipherProvider;
        private String signatureProvider;
        private String macProvider;
        private String messageDigestProvider;
        private String keyFactoryProvider;

        public String getGeneralProvider() {
            return this.generalProvider;
        }

        public void setGeneralProvider(String generalProvider) {
            this.generalProvider = generalProvider;
        }

        public String getKeyPairGeneratorProvider() {
            return this.select(this.keyPairGeneratorProvider);
        }

        public void setKeyPairGeneratorProvider(String keyPairGeneratorProvider) {
            this.keyPairGeneratorProvider = keyPairGeneratorProvider;
        }

        public String getKeyAgreementProvider() {
            return this.select(this.keyAgreementProvider);
        }

        public void setKeyAgreementProvider(String keyAgreementProvider) {
            this.keyAgreementProvider = keyAgreementProvider;
        }

        public String getCipherProvider() {
            return this.select(this.cipherProvider);
        }

        public void setCipherProvider(String cipherProvider) {
            this.cipherProvider = cipherProvider;
        }

        public String getSignatureProvider() {
            return this.select(this.signatureProvider);
        }

        public void setSignatureProvider(String signatureProvider) {
            this.signatureProvider = signatureProvider;
        }

        public String getMacProvider() {
            return this.select(this.macProvider);
        }

        public void setMacProvider(String macProvider) {
            this.macProvider = macProvider;
        }

        public String getMessageDigestProvider() {
            return this.select(this.messageDigestProvider);
        }

        public void setMessageDigestProvider(String messageDigestProvider) {
            this.messageDigestProvider = messageDigestProvider;
        }

        public String getKeyFactoryProvider() {
            return this.select(this.keyFactoryProvider);
        }

        public void setKeyFactoryProvider(String keyFactoryProvider) {
            this.keyFactoryProvider = keyFactoryProvider;
        }

        private String select(String specificValue) {
            return specificValue == null ? this.generalProvider : specificValue;
        }
    }
}

