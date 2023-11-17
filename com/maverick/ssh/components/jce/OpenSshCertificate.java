/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh.components.jce;

import com.maverick.ssh.SshException;
import com.maverick.ssh.SshKeyFingerprint;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.jce.CertificateExtension;
import com.maverick.ssh.components.jce.CriticalOption;
import com.maverick.ssh.components.jce.JCEComponentManager;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import com.maverick.util.UnsignedInteger64;
import com.sshtools.publickey.SshPublicKeyFileFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OpenSshCertificate
implements SshPublicKey {
    static Logger log = LoggerFactory.getLogger(OpenSshCertificate.class);
    public static final int SSH_CERT_TYPE_USER = 1;
    public static final int SSH_CERT_TYPE_HOST = 2;
    public static final String PERMIT_X11_FORWARDING = "permit-x11-forwarding";
    public static final String PERMIT_PORT_FORWARDING = "permit-port-forwarding";
    public static final String PERMIT_AGENT_FORWARDING = "permit-agent-forwarding";
    public static final String PERMIT_USER_PTY = "permit-pty";
    public static final String PERMIT_USER_RC = "permit-user-rc";
    public static final String OPTION_FORCE_COMMAND = "force-command";
    public static final String OPTION_SOURCE_ADDRESS = "source-address";
    SshPublicKey publicKey;
    byte[] nonce;
    UnsignedInteger64 serial;
    int type;
    String keyId;
    List<String> validPrincipals = new ArrayList<String>();
    UnsignedInteger64 validAfter;
    UnsignedInteger64 validBefore;
    List<CriticalOption> criticalOptions = new ArrayList<CriticalOption>();
    List<CertificateExtension> extensions = new ArrayList<CertificateExtension>();
    String reserved;
    SshPublicKey signedBy;
    byte[] signature;

    @Override
    public String getEncodingAlgorithm() {
        return this.getAlgorithm();
    }

    @Override
    public String getSigningAlgorithm() {
        return this.publicKey.getSigningAlgorithm();
    }

    public boolean isUserCertificate() {
        return this.type == 1;
    }

    public boolean isHostCertificate() {
        return this.type == 2;
    }

    public SshPublicKey getSignedKey() {
        return this.publicKey;
    }

    @Override
    public final String getFingerprint() throws SshException {
        return SshKeyFingerprint.getFingerprint(this.getSignedKey().getEncoded());
    }

    public OpenSshCertificate init(byte[] blob) throws SshException {
        this.init(blob, 0, blob.length);
        return this;
    }

    @Override
    public void init(byte[] blob, int start, int len) throws SshException {
        ByteArrayReader bar = new ByteArrayReader(blob, start, len);
        try {
            String header = bar.readString();
            this.nonce = bar.readBinaryString();
            this.decodePublicKey(bar);
            this.decodeCertificate(bar);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new SshException("Failed to obtain certificate key instance from JCE", 5, ex);
        }
        finally {
            try {
                bar.close();
            }
            catch (IOException iOException) {}
        }
    }

    @Override
    public byte[] getEncoded() throws SshException {
        ByteArrayWriter blob = new ByteArrayWriter();
        try {
            blob.writeString(this.getEncodingAlgorithm());
            blob.writeBinaryString(this.nonce);
            ByteArrayReader reader = new ByteArrayReader(this.getSignedKey().getEncoded());
            reader.readString();
            blob.write(reader.array(), reader.getPosition(), reader.available());
            reader.close();
            this.encodeCertificate(blob);
            this.encodeSignature(blob);
            byte[] byArray = blob.toByteArray();
            return byArray;
        }
        catch (Throwable t) {
            t.printStackTrace();
            throw new SshException("Failed to encode public key", 5);
        }
        finally {
            try {
                blob.close();
            }
            catch (IOException iOException) {}
        }
    }

    private void encodeSignature(ByteArrayWriter writer) throws IOException {
        writer.writeBinaryString(this.signature);
    }

    protected abstract void decodePublicKey(ByteArrayReader var1) throws IOException, SshException;

    protected void encodeCertificate(ByteArrayWriter writer) throws IOException, SshException {
        writer.writeUINT64(this.serial);
        writer.writeInt(this.type);
        writer.writeString(this.keyId);
        ByteArrayWriter users = new ByteArrayWriter();
        for (String string : this.validPrincipals) {
            users.writeString(string);
        }
        writer.writeBinaryString(users.toByteArray());
        users.close();
        writer.writeUINT64(this.validAfter);
        writer.writeUINT64(this.validBefore);
        ByteArrayWriter options = new ByteArrayWriter();
        for (CriticalOption e : this.criticalOptions) {
            options.writeString(e.getName());
            options.writeBinaryString(e.getStoredValue());
        }
        writer.writeBinaryString(options.toByteArray());
        options.close();
        ByteArrayWriter byteArrayWriter = new ByteArrayWriter();
        for (CertificateExtension e : this.extensions) {
            byteArrayWriter.writeString(e.getName());
            byteArrayWriter.writeBinaryString(e.getStoredValue());
        }
        writer.writeBinaryString(byteArrayWriter.toByteArray());
        byteArrayWriter.close();
        writer.writeString(this.reserved);
        writer.writeBinaryString(this.getSignedBy().getEncoded());
    }

    public CertificateExtension getExtension(String key) {
        for (CertificateExtension ext : this.extensions) {
            if (!ext.getName().equals(key)) continue;
            return ext;
        }
        return null;
    }

    protected void decodeCertificate(ByteArrayReader reader) throws IOException, SshException {
        String name;
        this.serial = reader.readUINT64();
        this.type = (int)reader.readInt();
        this.keyId = reader.readString();
        byte[] buf = reader.readBinaryString();
        ByteArrayReader tmp = new ByteArrayReader(buf);
        this.validPrincipals = new ArrayList<String>();
        while (tmp.available() > 0) {
            this.validPrincipals.add(tmp.readString());
        }
        tmp.close();
        this.validAfter = reader.readUINT64();
        this.validBefore = reader.readUINT64();
        tmp = new ByteArrayReader(reader.readBinaryString());
        this.criticalOptions.clear();
        while (tmp.available() > 0) {
            name = tmp.readString();
            this.criticalOptions.add(CriticalOption.createKnownOption(name, tmp.readBinaryString()));
        }
        tmp.close();
        tmp = new ByteArrayReader(reader.readBinaryString());
        this.extensions.clear();
        while (tmp.available() > 0) {
            name = tmp.readString().trim();
            CertificateExtension ext = CertificateExtension.createKnownExtension(name, tmp.readBinaryString());
            this.extensions.add(ext);
        }
        tmp.close();
        this.reserved = reader.readString();
        this.signedBy = SshPublicKeyFileFactory.decodeSSH2PublicKey(reader.readBinaryString());
        this.signature = reader.readBinaryString();
        byte[] data = new byte[reader.array().length - (this.signature.length + 4)];
        System.arraycopy(reader.array(), 0, data, 0, data.length);
        if (!this.getSignedBy().verifySignature(this.signature, data)) {
            throw new SshException("Certificate file could not validate the signature supplied by the CA", 16);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void sign(SshPublicKey publicKey, UnsignedInteger64 serial, int type, String keyId, List<String> validPrincipals, UnsignedInteger64 validAfter, UnsignedInteger64 validBefore, List<CriticalOption> criticalOptions, List<CertificateExtension> extensions, SshKeyPair signingKey) throws SshException {
        this.publicKey = publicKey;
        this.nonce = new byte[32];
        JCEComponentManager.getSecureRandom().nextBytes(this.nonce);
        this.serial = serial;
        this.type = type;
        this.keyId = keyId;
        this.validPrincipals = validPrincipals;
        this.validAfter = validAfter;
        this.validBefore = validBefore;
        this.criticalOptions = new ArrayList<CriticalOption>(criticalOptions);
        this.extensions = new ArrayList<CertificateExtension>(extensions);
        this.reserved = "";
        this.signedBy = signingKey.getPublicKey();
        ByteArrayWriter blob = new ByteArrayWriter();
        try {
            blob.writeString(this.getEncodingAlgorithm());
            blob.writeBinaryString(this.nonce);
            try (ByteArrayReader reader = new ByteArrayReader(publicKey.getEncoded());){
                reader.readString();
                blob.write(reader.array(), reader.getPosition(), reader.available());
            }
            this.encodeCertificate(blob);
            byte[] encoded = blob.toByteArray();
            try (ByteArrayWriter sig = new ByteArrayWriter();){
                sig.writeString(signingKey.getPublicKey().getSigningAlgorithm());
                sig.writeBinaryString(signingKey.getPrivateKey().sign(encoded));
                this.signature = sig.toByteArray();
            }
            reader = new ByteArrayReader(this.getEncoded());
            try {
                String algortihm = reader.readString();
                if (!algortihm.equals(this.getAlgorithm())) {
                    throw new SshException(String.format("Unexpected encoding error generating signed certificate [%s] [%s]", algortihm, this.getAlgorithm()), 5);
                }
                byte[] n = reader.readBinaryString();
                if (!Arrays.equals(this.nonce, n)) {
                    throw new SshException("Unexpected encoding error generating signed certificate [nonce]", 5);
                }
                this.decodePublicKey(reader);
                this.decodeCertificate(reader);
            }
            finally {
                reader.close();
            }
        }
        catch (Throwable t) {
            log.error("Ssh certificate sign failed", t);
            t.printStackTrace();
            throw new SshException("Failed to encode public key", 5);
        }
        finally {
            try {
                blob.close();
            }
            catch (IOException reader) {}
        }
    }

    public void verify() throws SshException {
        ByteArrayWriter blob = new ByteArrayWriter();
        try {
            blob.writeString(this.getEncodingAlgorithm());
            blob.writeBinaryString(this.nonce);
            try (ByteArrayReader reader = new ByteArrayReader(this.publicKey.getEncoded());){
                reader.readString();
                blob.write(reader.array(), reader.getPosition(), reader.available());
            }
            this.encodeCertificate(blob);
            byte[] encoded = blob.toByteArray();
            if (!this.getSignedBy().verifySignature(this.signature, encoded)) {
                throw new SshException("Failed to verify signature of certificate", 5);
            }
        }
        catch (IOException t) {
            log.error("Ssh certificate sign failed", (Throwable)t);
            t.printStackTrace();
            throw new SshException("Failed to process signature verification", 5);
        }
        finally {
            try {
                blob.close();
            }
            catch (IOException iOException) {}
        }
    }

    public SshPublicKey getSignedBy() {
        return this.signedBy;
    }

    public int getType() {
        return this.type;
    }

    public List<String> getPrincipals() {
        return Collections.unmodifiableList(this.validPrincipals);
    }

    @Deprecated
    public List<String> getExtensions() {
        ArrayList<String> tmp = new ArrayList<String>();
        for (CertificateExtension ext : this.extensions) {
            tmp.add(ext.getName());
        }
        return Collections.unmodifiableList(tmp);
    }

    public List<CriticalOption> getCriticalOptionsList() {
        return Collections.unmodifiableList(this.criticalOptions);
    }

    public List<CertificateExtension> getExtensionsList() {
        return Collections.unmodifiableList(this.extensions);
    }

    @Deprecated
    public Map<String, String> getExtensionsMap() {
        HashMap<String, String> tmp = new HashMap<String, String>();
        for (CertificateExtension ext : this.extensions) {
            tmp.put(ext.getName(), ext.getValue());
        }
        return Collections.unmodifiableMap(tmp);
    }

    public boolean isForceCommand() {
        return this.getForcedCommand() != null;
    }

    public String getForcedCommand() {
        for (CriticalOption ext : this.criticalOptions) {
            if (!ext.getName().equals(OPTION_FORCE_COMMAND)) continue;
            return ext.getStringValue();
        }
        return null;
    }

    public Set<String> getSourceAddresses() {
        HashSet<String> tmp = new HashSet<String>();
        for (CriticalOption ext : this.criticalOptions) {
            if (!ext.getName().equals(OPTION_SOURCE_ADDRESS)) continue;
            StringTokenizer t = new StringTokenizer(ext.getStringValue(), ",");
            while (t.hasMoreTokens()) {
                tmp.add(t.nextToken());
            }
        }
        return Collections.unmodifiableSet(tmp);
    }

    public Date getValidBefore() {
        return new Date(this.validBefore.longValue() * 1000L);
    }

    public Date getValidAfter() {
        return new Date(this.validAfter.longValue() * 1000L);
    }

    public UnsignedInteger64 getSerial() {
        return this.serial;
    }

    public String getKeyId() {
        return this.keyId;
    }

    @Deprecated
    public Map<String, String> getCriticalOptions() {
        HashMap<String, String> tmp = new HashMap<String, String>();
        for (CriticalOption ext : this.criticalOptions) {
            tmp.put(ext.getName(), ext.getStringValue());
        }
        return Collections.unmodifiableMap(tmp);
    }
}

