/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh2;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshDsaPublicKey;
import com.maverick.ssh.components.SshPrivateKey;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.ssh2.AuthenticationClient;
import com.maverick.ssh2.AuthenticationProtocol;
import com.maverick.ssh2.AuthenticationResult;
import com.maverick.util.ByteArrayWriter;
import java.io.IOException;

public class Ssh2HostbasedAuthentication
implements AuthenticationClient {
    String clientHostname;
    String username;
    String clientUsername;
    SshPrivateKey prv;
    SshPublicKey pub;

    @Override
    public void authenticate(AuthenticationProtocol authentication, String servicename) throws SshException, AuthenticationResult {
        if (this.username == null) {
            throw new SshException("Username not set!", 4);
        }
        if (this.clientHostname == null) {
            throw new SshException("Client hostname not set!", 4);
        }
        if (this.clientUsername == null) {
            this.clientUsername = this.username;
        }
        if (this.prv == null || this.pub == null) {
            throw new SshException("Client host keys not set!", 4);
        }
        if (!(this.pub instanceof SshRsaPublicKey) && !(this.pub instanceof SshDsaPublicKey)) {
            throw new SshException("Invalid public key type for SSH2 authentication!", 4);
        }
        ByteArrayWriter baw = new ByteArrayWriter();
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            try {
                msg.writeString(this.pub.getAlgorithm());
                msg.writeBinaryString(this.pub.getEncoded());
                msg.writeString(this.clientHostname);
                msg.writeString(this.clientUsername);
                baw.writeBinaryString(authentication.getSessionIdentifier());
                baw.write(50);
                baw.writeString(this.username);
                baw.writeString(servicename);
                baw.writeString("hostbased");
                baw.writeString(this.pub.getEncodingAlgorithm());
                baw.writeBinaryString(this.pub.getEncoded());
                baw.writeString(this.clientHostname);
                baw.writeString(this.clientUsername);
                ByteArrayWriter sig = new ByteArrayWriter();
                try {
                    sig.writeString(this.pub.getSigningAlgorithm());
                    sig.writeBinaryString(this.prv.sign(baw.toByteArray(), this.pub.getSigningAlgorithm()));
                    msg.writeBinaryString(sig.toByteArray());
                    authentication.sendRequest(this.getUsername(), servicename, "hostbased", msg.toByteArray());
                    byte[] reply = authentication.readMessage();
                    throw new SshException("Unexpected message returned from authentication protocol: " + reply[0], 3);
                }
                catch (Throwable throwable) {
                    sig.close();
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
            try {
                msg.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            throw throwable;
        }
    }

    @Override
    public String getMethod() {
        return "hostbased";
    }

    public void setClientHostname(String clientHostname) {
        this.clientHostname = clientHostname;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    public void setPublicKey(SshPublicKey pub) {
        this.pub = pub;
    }

    public void setPrivateKey(SshPrivateKey prv) {
        this.prv = prv;
    }

    public void setClientUsername(String clientUsername) {
        this.clientUsername = clientUsername;
    }

    public String getClientUsername() {
        return this.clientUsername;
    }

    public SshPrivateKey getPrivateKey() {
        return this.prv;
    }

    public SshPublicKey getPublicKey() {
        return this.pub;
    }
}

