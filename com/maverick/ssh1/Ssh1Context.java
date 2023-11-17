/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh1;

import com.maverick.ssh.ForwardingRequestListener;
import com.maverick.ssh.HostKeyVerification;
import com.maverick.ssh.SshContext;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.SshCipher;
import java.util.concurrent.ExecutorService;

public final class Ssh1Context
implements SshContext {
    public static final int CIPHER_DES = 2;
    public static final int CIPHER_3DES = 3;
    String sftp_path = "/usr/libexec/sftp-server";
    int maxChannels = 10;
    HostKeyVerification verify;
    String xDisplay = null;
    byte[] x11FakeCookie = null;
    byte[] x11RealCookie = null;
    ForwardingRequestListener x11Listener = null;
    int preferredCipher = 3;
    int messageTimeout = 60000;
    boolean fipsMode = false;
    String jceProvider;
    ExecutorService executor;

    @Override
    public void setChannelLimit(int maxChannels) {
        this.maxChannels = maxChannels;
    }

    @Override
    public int getChannelLimit() {
        return this.maxChannels;
    }

    @Override
    public void setSFTPProvider(String sftp_path) {
        this.sftp_path = sftp_path;
    }

    @Override
    public String getSFTPProvider() {
        return this.sftp_path;
    }

    @Override
    public void setX11Display(String xDisplay) {
        this.xDisplay = xDisplay;
    }

    @Override
    public String getX11Display() {
        return this.xDisplay;
    }

    @Override
    public byte[] getX11AuthenticationCookie() throws SshException {
        if (this.x11FakeCookie == null) {
            this.x11FakeCookie = new byte[16];
            ComponentManager.getInstance().getRND().nextBytes(this.x11FakeCookie);
        }
        return this.x11FakeCookie;
    }

    @Override
    public void setX11AuthenticationCookie(byte[] x11FakeCookie) {
        this.x11FakeCookie = x11FakeCookie;
    }

    @Override
    public void setX11RealCookie(byte[] x11RealCookie) {
        this.x11RealCookie = x11RealCookie;
    }

    @Override
    public byte[] getX11RealCookie() throws SshException {
        if (this.x11RealCookie == null) {
            this.x11RealCookie = this.getX11AuthenticationCookie();
        }
        return this.x11RealCookie;
    }

    @Override
    public void setX11RequestListener(ForwardingRequestListener x11Listener) {
        this.x11Listener = x11Listener;
    }

    @Override
    public ForwardingRequestListener getX11RequestListener() {
        return this.x11Listener;
    }

    public int getCipherType(int supportedCiphers) throws SshException {
        if (this.fipsMode) {
            throw new SshException("FIPS mode is enabled but an attempt was made to access an SSH1 type cipher", 4);
        }
        int mask = 1 << this.preferredCipher;
        if ((supportedCiphers & mask) != 0) {
            return this.preferredCipher;
        }
        mask = 8;
        if ((supportedCiphers & mask) != 0) {
            return 3;
        }
        mask = 4;
        if ((supportedCiphers & mask) != 0) {
            return 2;
        }
        throw new SshException("Cipher could not be agreed!", 9);
    }

    public void setCipherType(int preferredCipher) {
        this.preferredCipher = preferredCipher;
    }

    public SshCipher createCipher(int cipherType) throws SshException {
        return (SshCipher)ComponentManager.getInstance().supportedSsh1CiphersCS().getInstance(String.valueOf(cipherType));
    }

    @Override
    public void setHostKeyVerification(HostKeyVerification verify) {
        this.verify = verify;
    }

    @Override
    public HostKeyVerification getHostKeyVerification() {
        return this.verify;
    }

    @Override
    public void setMessageTimeout(int messageTimeout) {
        this.messageTimeout = messageTimeout;
    }

    @Override
    public int getMessageTimeout() {
        return this.messageTimeout;
    }

    @Override
    @Deprecated
    public void enableFIPSMode() throws SshException {
        this.fipsMode = true;
    }

    @Override
    public ExecutorService getExecutorService() {
        return this.executor;
    }

    @Override
    public void setExecutorService(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public boolean isSHA1SignaturesSupported() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSHA1SignaturesSupported(boolean supportSHA1Signatures) {
        throw new UnsupportedOperationException();
    }
}

