/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshClientAdapter;
import com.maverick.ssh.components.ComponentFactory;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.jce.JCEComponentManager;
import com.maverick.ssh2.Ssh2Client;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationCollector
extends SshClientAdapter {
    SshPublicKey key = null;
    Ssh2Client client;

    @Override
    public void keyExchangeComplete(SshClient client, SshPublicKey hostkey, String keyExchange, String cipherCS, String cipherSC, String macCS, String macSC, String compressionCS, String compressionSC) {
        this.client = (Ssh2Client)client;
        this.key = hostkey;
        client.disconnect();
    }

    public String getRemoteIdentification() {
        return this.client.getRemoteIdentification();
    }

    public Set<String> getSupportedHostKeys() {
        return new HashSet<String>(Arrays.asList(this.client.getRemotePublicKeys()));
    }

    public Set<String> getSupportedKeyExchanges() {
        return new HashSet<String>(Arrays.asList(this.client.getRemoteKeyExchanges()));
    }

    public Set<String> getSupportedCompressions() {
        HashSet<String> tmp = new HashSet<String>();
        tmp.addAll(Arrays.asList(this.client.getRemoteCompressionsCS()));
        tmp.addAll(Arrays.asList(this.client.getRemoteCompressionsSC()));
        return tmp;
    }

    public Set<String> getSupportedCiphers() {
        HashSet<String> tmp = new HashSet<String>();
        tmp.addAll(Arrays.asList(this.client.getRemoteCiphersCS()));
        tmp.addAll(Arrays.asList(this.client.getRemoteCiphersSC()));
        return tmp;
    }

    public Set<String> getSupportedMacs() {
        HashSet<String> tmp = new HashSet<String>();
        tmp.addAll(Arrays.asList(this.client.getRemoteMacsCS()));
        tmp.addAll(Arrays.asList(this.client.getRemoteMacsSC()));
        return tmp;
    }

    public SshPublicKey getKey() {
        return this.key;
    }

    public SecurityLevel getSecurityLevel() {
        SecurityLevel securityLevel = null;
        HashSet<SecurityLevel> tmp = new HashSet<SecurityLevel>();
        if (!this.checkNone(this.getNegotiatedCipherCS())) {
            tmp.add(this.getComponentSecurityLevel(JCEComponentManager.getInstance().supportedSsh2CiphersCS(), this.getNegotiatedCipherCS()));
        }
        if (!this.checkNone(this.getNegotiatedCipherSC())) {
            tmp.add(this.getComponentSecurityLevel(JCEComponentManager.getInstance().supportedSsh2CiphersSC(), this.getNegotiatedCipherSC()));
        }
        if (!this.checkNone(this.getNegotiatedMacCS())) {
            tmp.add(this.getComponentSecurityLevel(JCEComponentManager.getInstance().supportedHMacsCS(), this.getNegotiatedMacCS()));
        }
        if (!this.checkNone(this.getNegotiatedMacSC())) {
            tmp.add(this.getComponentSecurityLevel(JCEComponentManager.getInstance().supportedHMacsSC(), this.getNegotiatedMacSC()));
        }
        tmp.add(this.getComponentSecurityLevel(JCEComponentManager.getInstance().supportedKeyExchanges(false), this.getNegotiatedKeyExchange()));
        tmp.add(this.getComponentSecurityLevel(JCEComponentManager.getInstance().supportedPublicKeys(), this.getNegotiatedHostKey()));
        for (SecurityLevel t : tmp) {
            if (securityLevel == null) {
                securityLevel = t;
            }
            if (t.ordinal() >= securityLevel.ordinal()) continue;
            securityLevel = t;
        }
        return securityLevel;
    }

    public SecurityLevel getMaximumSecurity() {
        SecurityLevel securityLevel = null;
        HashSet<SecurityLevel> tmp = new HashSet<SecurityLevel>();
        tmp.add(JCEComponentManager.getInstance().supportedSsh2CiphersCS().getMaximumSecurity());
        tmp.add(JCEComponentManager.getInstance().supportedSsh2CiphersSC().getMaximumSecurity());
        tmp.add(JCEComponentManager.getInstance().supportedHMacsCS().getMaximumSecurity());
        tmp.add(JCEComponentManager.getInstance().supportedHMacsSC().getMaximumSecurity());
        tmp.add(JCEComponentManager.getInstance().supportedKeyExchanges(false).getMaximumSecurity());
        tmp.add(JCEComponentManager.getInstance().supportedPublicKeys().getMaximumSecurity());
        for (SecurityLevel t : tmp) {
            if (securityLevel == null) {
                securityLevel = t;
            }
            if (t.ordinal() >= securityLevel.ordinal()) continue;
            securityLevel = t;
        }
        return securityLevel;
    }

    public SecurityLevel getMinimumSecurity() {
        SecurityLevel securityLevel = null;
        HashSet<SecurityLevel> tmp = new HashSet<SecurityLevel>();
        tmp.add(JCEComponentManager.getInstance().supportedSsh2CiphersCS().getMinimumSecurity());
        tmp.add(JCEComponentManager.getInstance().supportedSsh2CiphersSC().getMinimumSecurity());
        tmp.add(JCEComponentManager.getInstance().supportedHMacsCS().getMinimumSecurity());
        tmp.add(JCEComponentManager.getInstance().supportedHMacsSC().getMinimumSecurity());
        tmp.add(JCEComponentManager.getInstance().supportedKeyExchanges(false).getMinimumSecurity());
        tmp.add(JCEComponentManager.getInstance().supportedPublicKeys().getMinimumSecurity());
        for (SecurityLevel t : tmp) {
            if (securityLevel == null) {
                securityLevel = t;
            }
            if (t.ordinal() >= securityLevel.ordinal()) continue;
            securityLevel = t;
        }
        return securityLevel;
    }

    private boolean checkNone(String algorithm) {
        return "none".equals(algorithm);
    }

    public String getNegotiatedKeyExchange() {
        return this.client.getKeyExchangeInUse();
    }

    public String getNegotiatedHostKey() {
        return this.client.getHostKeyInUse();
    }

    public String getNegotiatedCipherCS() {
        return this.client.getCipherInUseCS();
    }

    public String getNegotiatedCipherSC() {
        return this.client.getCipherInUseSC();
    }

    public String getNegotiatedMacCS() {
        return this.client.getMacInUseCS();
    }

    public String getNegotiatedMacSC() {
        return this.client.getMacInUseSC();
    }

    public String getNegotiatedCompressionCS() {
        return this.client.getCompressionInUseCS();
    }

    public String getNegotiatedCompressionSC() {
        return this.client.getCompressionInUseSC();
    }

    private SecurityLevel getComponentSecurityLevel(ComponentFactory<?> components, String algorithm) {
        return components.getSecurityLevel(algorithm);
    }
}

