/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh2;

import com.maverick.ssh.PasswordAuthentication;
import com.maverick.ssh.SshException;
import com.maverick.ssh2.AuthenticationClient;
import com.maverick.ssh2.AuthenticationProtocol;
import com.maverick.ssh2.AuthenticationResult;
import com.maverick.util.ByteArrayWriter;
import java.io.IOException;

public class Ssh2PasswordAuthentication
extends PasswordAuthentication
implements AuthenticationClient {
    String newpassword;
    boolean passwordChangeRequired = false;
    static final int SSH_MSG_USERAUTH_PASSWD_CHANGEREQ = 60;

    public Ssh2PasswordAuthentication() {
    }

    public Ssh2PasswordAuthentication(String password) {
        this.setPassword(password);
    }

    public void setNewPassword(String newpassword) {
        this.newpassword = newpassword;
        this.passwordChangeRequired = true;
    }

    public boolean requiresPasswordChange() {
        return this.passwordChangeRequired;
    }

    @Override
    public void authenticate(AuthenticationProtocol authentication, String servicename) throws SshException, AuthenticationResult {
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            try {
                if (this.getUsername() == null || this.getPassword() == null) {
                    throw new SshException("Username or password not set!", 4);
                }
                if (this.passwordChangeRequired && this.newpassword == null) {
                    throw new SshException("You must set a new password!", 4);
                }
                msg.writeBoolean(this.passwordChangeRequired);
                msg.writeString(this.getPassword());
                if (this.passwordChangeRequired) {
                    msg.writeString(this.newpassword);
                }
                authentication.sendRequest(this.getUsername(), servicename, "password", msg.toByteArray());
                byte[] response = authentication.readMessage();
                if (response[0] != 60) {
                    authentication.transport.disconnect(2, "Unexpected message received");
                    throw new SshException("Unexpected response from Authentication Protocol", 3);
                }
                this.passwordChangeRequired = true;
                throw new AuthenticationResult(2);
            }
            catch (IOException ex) {
                throw new SshException(ex, 5);
            }
        }
        catch (Throwable throwable) {
            try {
                msg.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            throw throwable;
        }
    }
}

