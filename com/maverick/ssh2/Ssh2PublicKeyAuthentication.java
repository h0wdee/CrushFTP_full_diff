/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh2;

import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.PublicKeyAuthentication;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.ssh.components.jce.OpenSshRsaSha256Certificate;
import com.maverick.ssh.components.jce.OpenSshRsaSha512Certificate;
import com.maverick.ssh.components.jce.Ssh2RsaPublicKey;
import com.maverick.ssh.components.jce.Ssh2RsaPublicKeySHA256;
import com.maverick.ssh.components.jce.Ssh2RsaPublicKeySHA512;
import com.maverick.ssh2.AuthenticationClient;
import com.maverick.ssh2.AuthenticationProtocol;
import com.maverick.ssh2.AuthenticationResult;
import com.maverick.ssh2.SignatureGenerator;
import com.maverick.util.ByteArrayWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ssh2PublicKeyAuthentication
extends PublicKeyAuthentication
implements AuthenticationClient {
    static Logger log = LoggerFactory.getLogger(Ssh2PublicKeyAuthentication.class);
    static final int SSH_MSG_USERAUTH_PK_OK = 60;
    SignatureGenerator generator;

    @Override
    public void authenticate(AuthenticationProtocol authentication, String servicename) throws SshException, AuthenticationResult {
        ByteArrayWriter baw = new ByteArrayWriter();
        try {
            try {
                byte[] encoded;
                boolean supported;
                String serverSigAlgs;
                if (this.getPublicKey() == null) {
                    throw new SshException("Public key not set!", 4);
                }
                if (this.getPrivateKey() == null && this.generator == null && this.isAuthenticating()) {
                    throw new SshException("Private key or signature generator not set!", 4);
                }
                if (this.getUsername() == null) {
                    throw new SshException("Username not set!", 4);
                }
                baw.writeBinaryString(authentication.getSessionIdentifier());
                baw.write(50);
                baw.writeString(this.getUsername());
                baw.writeString(servicename);
                baw.writeString("publickey");
                baw.writeBoolean(this.isAuthenticating());
                if (this.publickey.getAlgorithm().equals("ssh-rsa") && this.publickey.getBitLength() >= 1024) {
                    serverSigAlgs = (String)authentication.getClient().getAttribute("server-sig-algs");
                    if (serverSigAlgs != null) {
                        if (serverSigAlgs.contains("rsa-sha2-512")) {
                            if (log.isDebugEnabled()) {
                                log.debug("Upgrading RSA key signature to SHA512");
                            }
                            this.publickey = new Ssh2RsaPublicKeySHA512((SshRsaPublicKey)this.publickey);
                        } else if (serverSigAlgs.contains("rsa-sha2-256")) {
                            if (log.isDebugEnabled()) {
                                log.debug("Upgrading RSA key signature to SHA256");
                            }
                            this.publickey = new Ssh2RsaPublicKeySHA256((SshRsaPublicKey)this.publickey);
                        } else if (!serverSigAlgs.contains("ssh-rsa")) {
                            if (log.isErrorEnabled()) {
                                log.error("The server does not support ssh-rsa signatures");
                            }
                            throw new AuthenticationResult(2);
                        }
                    }
                } else if (this.publickey.getAlgorithm().equals("ssh-rsa-cert-v01@openssh.com") && this.publickey.getBitLength() >= 1024 && (serverSigAlgs = (String)authentication.getClient().getAttribute("server-sig-algs")) != null) {
                    if (serverSigAlgs.contains("rsa-sha2-256")) {
                        if (log.isDebugEnabled()) {
                            log.debug("Upgrading RSA certificate signature to SHA256");
                        }
                        this.publickey = new OpenSshRsaSha256Certificate().init(this.publickey.getEncoded());
                    } else if (serverSigAlgs.contains("rsa-sha2-512")) {
                        if (log.isDebugEnabled()) {
                            log.debug("Upgrading RSA certificate signature to SHA512");
                        }
                        this.publickey = new OpenSshRsaSha512Certificate().init(this.publickey.getEncoded());
                    } else if (!serverSigAlgs.contains("ssh-rsa")) {
                        if (log.isErrorEnabled()) {
                            log.error("The server does not support ssh-rsa-cert-v01@openssh.com  signatures");
                        }
                        throw new AuthenticationResult(2);
                    }
                }
                if (this.publickey.getSigningAlgorithm().equals("ssh-rsa") && !(supported = AdaptiveConfiguration.getBoolean("supportSHA1Signatures", authentication.getClient().getContext().isSHA1SignaturesSupported(), authentication.getClient().getHost(), authentication.getClient().getIdent()))) {
                    if (log.isErrorEnabled()) {
                        log.error("This client has disabled ssh-rsa SHA1 signatures");
                    }
                    throw new AuthenticationResult(2);
                }
                if (!AdaptiveConfiguration.getBoolean("disableRSARestrictions", false, authentication.getHost(), authentication.getClient().getIdent()) && this.publickey instanceof Ssh2RsaPublicKey && this.publickey.getBitLength() < 1024) {
                    throw new SshException("RSA keys under 1024 bits are no longer supported for use in Authentication", 4);
                }
                try (ByteArrayWriter baw2 = new ByteArrayWriter();){
                    if (log.isDebugEnabled()) {
                        log.debug("Authenticating with {} using signature {}", (Object)this.getPublicKey().getEncodingAlgorithm(), (Object)this.getPublicKey().getSigningAlgorithm());
                    }
                    baw.writeString(this.getPublicKey().getAlgorithm());
                    encoded = this.getPublicKey().getEncoded();
                    baw.writeBinaryString(encoded);
                }
                baw2 = new ByteArrayWriter();
                try {
                    baw2.writeBoolean(this.isAuthenticating());
                    baw2.writeString(this.getPublicKey().getAlgorithm());
                    baw2.writeBinaryString(encoded);
                    if (this.isAuthenticating()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Signing public key authentication packet with {} {}", (Object)this.getPublicKey().getSigningAlgorithm(), (Object)this.getPublicKey().getFingerprint());
                        }
                        byte[] signature = this.generator != null ? this.generator.sign(this.getPublicKey(), baw.toByteArray()) : this.getPrivateKey().sign(baw.toByteArray(), this.getPublicKey().getSigningAlgorithm());
                        baw2.writeBinaryString(this.encodeSignature(signature));
                    } else if (log.isDebugEnabled()) {
                        log.debug("Checking public key is acceptable {}", (Object)this.getPublicKey().getFingerprint());
                    }
                    authentication.sendRequest(this.getUsername(), servicename, "publickey", baw2.toByteArray());
                    byte[] response = authentication.readMessage();
                    if (response[0] == 60) {
                        if (log.isDebugEnabled()) {
                            log.debug("Server accepts public key {} {}", (Object)this.getPublicKey().getSigningAlgorithm(), (Object)this.getPublicKey().getFingerprint());
                        }
                        throw new AuthenticationResult(5);
                    }
                    authentication.transport.disconnect(2, "Unexpected message " + response[0] + " received");
                    throw new SshException("Unexpected message " + response[0] + " received", 3);
                }
                catch (Throwable throwable) {
                    baw2.close();
                    baw.close();
                    throw throwable;
                }
            }
            catch (IOException ex) {
                throw new SshException(ex, 5);
            }
        }
        catch (Throwable throwable) {
            try {
                baw.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            throw throwable;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected byte[] encodeSignature(byte[] signature) throws IOException {
        try (ByteArrayWriter sig = new ByteArrayWriter();){
            sig.writeString(this.getPublicKey().getSigningAlgorithm());
            sig.writeBinaryString(signature);
            byte[] byArray = sig.toByteArray();
            return byArray;
        }
    }

    public void setSignatureGenerator(SignatureGenerator generator) {
        this.generator = generator;
    }
}

