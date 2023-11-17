/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh2;

import com.maverick.events.Event;
import com.maverick.events.EventServiceImplementation;
import com.maverick.ssh.SshException;
import com.maverick.ssh2.AuthenticationClient;
import com.maverick.ssh2.AuthenticationResult;
import com.maverick.ssh2.BannerDisplay;
import com.maverick.ssh2.Ssh2Client;
import com.maverick.ssh2.SshKeyExchangeClient;
import com.maverick.ssh2.TransportProtocol;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import java.io.IOException;

public class AuthenticationProtocol {
    public static final int SSH_MSG_USERAUTH_REQUEST = 50;
    static final int SSH_MSG_USERAUTH_FAILURE = 51;
    static final int SSH_MSG_USERAUTH_SUCCESS = 52;
    static final int SSH_MSG_USERAUTH_BANNER = 53;
    TransportProtocol transport;
    BannerDisplay display;
    int state = 2;
    public static final String SERVICE_NAME = "ssh-userauth";

    public SshKeyExchangeClient getKeyExchange() {
        return this.transport.getKeyExchange();
    }

    public AuthenticationProtocol(TransportProtocol transport) throws SshException {
        this.transport = transport;
        transport.startService(SERVICE_NAME);
    }

    public void setBannerDisplay(BannerDisplay display) {
        this.display = display;
    }

    public byte[] readMessage() throws SshException, AuthenticationResult {
        byte[] msg;
        while (this.processMessage(msg = this.transport.nextMessage(0L))) {
        }
        return msg;
    }

    public int authenticate(AuthenticationClient auth, String servicename) throws SshException {
        try {
            auth.authenticate(this, servicename);
            this.readMessage();
            this.transport.disconnect(2, "Unexpected response received from Authentication Protocol");
            throw new SshException("Unexpected response received from Authentication Protocol", 3);
        }
        catch (AuthenticationResult result) {
            this.state = result.getResult();
            if (this.state == 1) {
                this.transport.completedAuthentication();
            }
            return this.state;
        }
    }

    public String getAuthenticationMethods(String username, String servicename) throws SshException {
        this.sendRequest(username, servicename, "none", null);
        try {
            this.readMessage();
            this.transport.disconnect(2, "Unexpected response received from Authentication Protocol");
            throw new SshException("Unexpected response received from Authentication Protocol", 3);
        }
        catch (AuthenticationResult result) {
            this.state = result.getResult();
            EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.transport.getClient(), 11, true, this.transport.getUuid()).addAttribute("CLIENT", this.transport.getClient()).addAttribute("AUTHENTICATION_METHODS", result.getAuthenticationMethods()));
            return result.getAuthenticationMethods();
        }
    }

    public void sendRequest(String username, String servicename, String methodname, byte[] requestdata) throws SshException {
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            msg.write(50);
            msg.writeString(username);
            msg.writeString(servicename);
            msg.writeString(methodname);
            if (requestdata != null) {
                msg.write(requestdata);
            }
            this.transport.sendMessage(msg.toByteArray(), true);
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
        finally {
            try {
                msg.close();
            }
            catch (IOException iOException) {}
        }
    }

    public boolean isAuthenticated() {
        return this.state == 1;
    }

    public byte[] getSessionIdentifier() {
        return this.transport.getSessionIdentifier();
    }

    private boolean processMessage(byte[] msg) throws SshException, AuthenticationResult {
        ByteArrayReader bar = new ByteArrayReader(msg);
        try {
            switch (msg[0]) {
                case 51: {
                    bar.skip(1L);
                    String auths = bar.readString();
                    if (bar.read() == 0) {
                        EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.transport.getClient(), 14, true, this.transport.getUuid()).addAttribute("CLIENT", this.transport.getClient()));
                        throw new AuthenticationResult(2, auths);
                    }
                    EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.transport.getClient(), 15, true, this.transport.getUuid()).addAttribute("CLIENT", this.transport.getClient()));
                    throw new AuthenticationResult(3, auths);
                }
                case 52: {
                    EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.transport.getClient(), 13, true, this.transport.getUuid()).addAttribute("CLIENT", this.transport.getClient()));
                    throw new AuthenticationResult(1);
                }
                case 53: {
                    bar.skip(1L);
                    if (this.display != null) {
                        this.display.displayBanner(bar.readString());
                    }
                    boolean auths = true;
                    return auths;
                }
            }
            boolean auths = false;
            return auths;
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
        finally {
            try {
                bar.close();
            }
            catch (IOException iOException) {}
        }
    }

    public void sendMessage(byte[] messg) throws SshException {
        this.transport.sendMessage(messg, true);
    }

    public String getHost() {
        return this.transport.provider.getHost();
    }

    public Ssh2Client getClient() {
        return this.transport.client;
    }
}

