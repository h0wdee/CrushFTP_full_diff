/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshCertificate;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.components.Utils;
import com.maverick.ssh.components.jce.CertificateExtension;
import com.maverick.ssh.components.jce.CriticalOption;
import com.maverick.ssh.components.jce.OpenSshCertificate;
import com.maverick.ssh.components.jce.OpenSshEcdsaSha2Nist256Certificate;
import com.maverick.ssh.components.jce.OpenSshEcdsaSha2Nist384Certificate;
import com.maverick.ssh.components.jce.OpenSshEcdsaSha2Nist521Certificate;
import com.maverick.ssh.components.jce.OpenSshEd25519Certificate;
import com.maverick.ssh.components.jce.OpenSshRsaCertificate;
import com.maverick.util.UnsignedInteger64;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SshCertificateAuthority {
    public static SshCertificate generateUserCertificate(SshKeyPair key, long serial, String principalName, int validityDays, SshKeyPair signedBy) throws SshException, IOException {
        return SshCertificateAuthority.generateCertificate(key, serial, 1, principalName, principalName, validityDays, signedBy);
    }

    public static SshCertificate generateHostCertificate(SshKeyPair key, long serial, String hostname, int validityDays, SshKeyPair signedBy) throws SshException, IOException {
        return SshCertificateAuthority.generateCertificate(key, serial, 2, hostname, Arrays.asList(hostname), validityDays, new HashMap<String, String>(), new ArrayList<String>(), signedBy);
    }

    public static SshCertificate generateCertificate(SshKeyPair key, long serial, int type, String keyId, String principal, int validityDays, SshKeyPair signedBy) throws SshException, IOException {
        return SshCertificateAuthority.generateCertificate(key, serial, type, keyId, Arrays.asList(principal), validityDays, Collections.emptyList(), new CertificateExtension.Builder().defaultExtensions().build(), signedBy);
    }

    public static SshCertificate generateCertificate(SshKeyPair key, long serial, int type, String keyId, String principal, int validityDays, List<CertificateExtension> extensions, SshKeyPair signedBy) throws SshException, IOException {
        return SshCertificateAuthority.generateCertificate(key, serial, type, keyId, Arrays.asList(principal), validityDays, Collections.emptyList(), extensions, signedBy);
    }

    public static SshCertificate generateCertificate(SshKeyPair key, long serial, int type, String keyId, List<String> validPrincipals, int validityDays, List<CriticalOption> criticalOptions, List<CertificateExtension> extensions, SshKeyPair signedBy) throws SshException, IOException {
        OpenSshCertificate cert;
        switch (type) {
            case 1: 
            case 2: {
                break;
            }
            default: {
                throw new SshException(String.format("Invalid certificate type %d", type), 4);
            }
        }
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(10, 0);
        c.set(12, 0);
        c.set(13, 0);
        c.set(14, 0);
        UnsignedInteger64 validAfter = new UnsignedInteger64(c.getTimeInMillis() / 1000L);
        c.add(5, validityDays);
        UnsignedInteger64 validBefore = new UnsignedInteger64(c.getTimeInMillis() / 1000L);
        String reserved = "";
        switch (key.getPublicKey().getAlgorithm()) {
            case "ssh-rsa": {
                cert = new OpenSshRsaCertificate();
                break;
            }
            case "ssh-ed25519": {
                cert = new OpenSshEd25519Certificate();
                break;
            }
            case "ecdsa-sha2-nistp256": {
                cert = new OpenSshEcdsaSha2Nist256Certificate();
                break;
            }
            case "ecdsa-sha2-nistp384": {
                cert = new OpenSshEcdsaSha2Nist384Certificate();
                break;
            }
            case "ecdsa-sha2-nistp521": {
                cert = new OpenSshEcdsaSha2Nist521Certificate();
                break;
            }
            default: {
                throw new SshException(4, String.format("Unsupported certificate type %s", key.getPublicKey().getAlgorithm()));
            }
        }
        cert.sign(key.getPublicKey(), new UnsignedInteger64(serial), type, keyId, validPrincipals, validAfter, validBefore, criticalOptions, extensions, signedBy);
        return new SshCertificate(key, cert);
    }

    @Deprecated
    public static SshCertificate generateCertificate(SshKeyPair key, long serial, int type, String keyId, List<String> validPrincipals, int validityDays, Map<String, String> criticalOptions, List<String> extensions, SshKeyPair signedBy) throws SshException, IOException {
        return SshCertificateAuthority.generateCertificate(key, serial, type, keyId, validPrincipals, validityDays, SshCertificateAuthority.createCriticalOptions(criticalOptions), SshCertificateAuthority.createExtensions(extensions), signedBy);
    }

    private static List<CertificateExtension> createExtensions(List<String> extensions) {
        ArrayList<CertificateExtension> tmp = new ArrayList<CertificateExtension>();
        for (String key : extensions) {
            if (!Utils.isNotBlank(key)) continue;
            tmp.add(CertificateExtension.createKnownExtension(key, new byte[0]));
        }
        return tmp;
    }

    private static List<CriticalOption> createCriticalOptions(Map<String, String> options) {
        ArrayList<CriticalOption> tmp = new ArrayList<CriticalOption>();
        for (String key : options.keySet()) {
            if (!Utils.isNotBlank(key)) continue;
            tmp.add(new CriticalOption(key, options.get(key), true));
        }
        return tmp;
    }
}

