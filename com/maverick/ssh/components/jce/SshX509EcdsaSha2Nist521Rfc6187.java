/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.components.jce.SshX509EcdsaSha2NistPublicKeyRfc6187;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.interfaces.ECPublicKey;

public class SshX509EcdsaSha2Nist521Rfc6187
extends SshX509EcdsaSha2NistPublicKeyRfc6187 {
    public SshX509EcdsaSha2Nist521Rfc6187(ECPublicKey pk) throws IOException {
        super(pk, "secp521r1");
    }

    public SshX509EcdsaSha2Nist521Rfc6187() {
        super("ecdsa-sha2-nistp521", "SHA512withECDSA", "secp521r1", "nistp521");
    }

    public SshX509EcdsaSha2Nist521Rfc6187(Certificate[] chain) throws IOException {
        super(chain, "secp521r1");
    }

    @Override
    public String getAlgorithm() {
        return "x509v3-ecdsa-sha2-nistp521";
    }
}

