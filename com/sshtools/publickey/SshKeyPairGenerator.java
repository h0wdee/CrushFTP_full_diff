/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.util.SshKeyUtils;
import java.io.IOException;

public class SshKeyPairGenerator {
    public static final String SSH1_RSA = "rsa1";
    public static final String SSH2_RSA = "ssh-rsa";
    public static final String RSA_SHA2_256 = "rsa-sha2-256";
    public static final String RSA_SHA2_512 = "rsa-sha2-512";
    public static final String SSH2_DSA = "ssh-dss";
    public static final String ECDSA = "ecdsa";
    public static final String ED25519 = "ed25519";
    public static final String ED448 = "ed448";

    public static SshKeyPair generateKeyPair(String algorithm, int bits) throws IOException, SshException {
        SshKeyPair pair = new SshKeyPair();
        if (ECDSA.equalsIgnoreCase(algorithm)) {
            pair = ComponentManager.getInstance().generateEcdsaKeyPair(bits);
        } else if (SSH1_RSA.equalsIgnoreCase(algorithm)) {
            pair = ComponentManager.getInstance().generateRsaKeyPair(bits, 1);
        } else if (SSH2_RSA.equalsIgnoreCase(algorithm)) {
            pair = ComponentManager.getInstance().generateRsaKeyPair(bits, 2);
        } else {
            if (RSA_SHA2_256.equalsIgnoreCase(algorithm)) {
                if (bits < 1024) {
                    throw new IllegalArgumentException(algorithm + " key must be at least 1024 bits");
                }
                pair = ComponentManager.getInstance().generateRsaKeyPair(bits, 2);
                return SshKeyUtils.makeRSAWithSHA256Signature(pair);
            }
            if (RSA_SHA2_512.equalsIgnoreCase(algorithm)) {
                if (bits < 1024) {
                    throw new IllegalArgumentException(algorithm + " key must be at least 1024 bits");
                }
                pair = ComponentManager.getInstance().generateRsaKeyPair(bits, 2);
                return SshKeyUtils.makeRSAWithSHA512Signature(pair);
            }
            if (SSH2_DSA.equals(algorithm)) {
                pair = ComponentManager.getInstance().generateDsaKeyPair(bits);
            } else if (ED25519.equals(algorithm)) {
                pair = ComponentManager.getInstance().generateEd25519KeyPair();
            } else if (ED448.equals(algorithm)) {
                pair = ComponentManager.getInstance().generateEd448KeyPair();
            } else {
                throw new IOException(algorithm + " is not a supported key algorithm!");
            }
        }
        return pair;
    }

    public static SshKeyPair generateKeyPair() throws IOException, SshException {
        return SshKeyPairGenerator.generateKeyPair(ED25519);
    }

    public static SshKeyPair generateKeyPair(String algorithm) throws IOException, SshException {
        switch (algorithm) {
            case "ssh-rsa": {
                return SshKeyPairGenerator.generateKeyPair(SSH2_RSA, 3192);
            }
            case "ed25519": {
                return SshKeyPairGenerator.generateKeyPair(ED25519, 256);
            }
            case "ed448": {
                return SshKeyPairGenerator.generateKeyPair(ED448, 448);
            }
            case "ecdsa": {
                return SshKeyPairGenerator.generateKeyPair(ECDSA, 521);
            }
            case "ssh-dss": {
                return SshKeyPairGenerator.generateKeyPair(SSH2_DSA, 1024);
            }
            case "rsa1": {
                return SshKeyPairGenerator.generateKeyPair(SSH1_RSA, 3192);
            }
        }
        throw new IOException(algorithm + " is not a supported key algorithm!");
    }
}

