/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh1;

import com.maverick.ssh.SshContext;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshTransport;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.Digest;
import com.maverick.ssh.components.SshCipher;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.ssh.compression.SshCompression;
import com.maverick.ssh.message.SshMessage;
import com.maverick.ssh.message.SshMessageReader;
import com.maverick.ssh1.CRC32;
import com.maverick.ssh1.Ssh1Context;
import com.maverick.ssh1.Ssh1ProtocolListener;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.math.BigInteger;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Ssh1Protocol
implements SshMessageReader {
    static Logger log = LoggerFactory.getLogger(Ssh1Protocol.class);
    static final int SSH_MSG_DISCONNECT = 1;
    static final int SSH_SMSG_PUBLIC_KEY = 2;
    static final int SSH_CMSG_SESSION_KEY = 3;
    static final int SSH_CMSG_USER = 4;
    static final int SSH_SMSG_SUCCESS = 14;
    static final int SSH_SMSG_FAILURE = 15;
    static final int SSH_MSG_IGNORE = 32;
    static final int SSH_MSG_DEBUG = 36;
    static final int NOT_CONNECTED = 1;
    static final int CONNECTED = 2;
    static final int DISCONNECTED = 3;
    DataInputStream in;
    DataOutputStream out;
    SshTransport transport;
    SshCipher decryption;
    SshCipher encryption;
    SshCompression incomingCompression;
    SshCompression outgoingCompression;
    SshRsaPublicKey hostKey;
    SshRsaPublicKey serverKey;
    static final int SSH_PROTOFLAG_SCREEN_NUMBER = 1;
    static final int SSH_PROTOFLAG_HOST_IN_FWD_OPEN = 2;
    int serverProtocolFlags;
    int clientProtocolFlags = 2;
    int supportedCiphers;
    int supportedAuthentications;
    byte[] sessionKey;
    byte[] sessionId;
    byte[] cookie;
    long sequenceNo = 0L;
    Ssh1Context context;
    int state = 1;
    boolean closing = false;
    Ssh1ProtocolListener listener;
    String ident;
    UUID uuid = UUID.randomUUID();

    Ssh1Protocol(SshTransport transport, SshContext context, String ident, Ssh1ProtocolListener listener) throws SshException {
        try {
            if (!(context instanceof Ssh1Context)) {
                throw new SshException("Invalid SshContext!", 4);
            }
            this.in = new DataInputStream(transport.getInputStream());
            this.out = new DataOutputStream(transport.getOutputStream());
            this.transport = transport;
            this.ident = ident;
            this.listener = listener;
            this.context = (Ssh1Context)context;
        }
        catch (IOException ex) {
            this.close();
            throw new SshException(ex, 10);
        }
    }

    @Override
    public boolean isConnected() {
        return this.state == 2;
    }

    @Override
    public String getHostname() {
        return this.transport.getHost();
    }

    @Override
    public String getIdent() {
        return this.ident;
    }

    @Override
    public byte[] nextMessage(long timeout) throws SshException {
        byte[] msg;
        while (this.processMessage(msg = this.readMessage(timeout)) && this.state == 2) {
        }
        if (this.state == 3) {
            throw new SshException("The remote host disconnected", 2);
        }
        return msg;
    }

    void close() {
        block4: {
            try {
                this.transport.close();
            }
            catch (IOException ex) {
                if (!log.isDebugEnabled()) break block4;
                log.debug("RECIEVED IOException IN Ssh1Protocol.close:" + ex.getMessage());
            }
        }
        this.state = 3;
        try {
            this.listener.disconnected();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void disconnect(String reason) {
        try {
            this.closing = true;
            try (ByteArrayWriter msg = new ByteArrayWriter(reason.length() + 5);){
                msg.write(1);
                msg.writeString(reason);
                if (log.isDebugEnabled()) {
                    log.debug("Sending SSH_MSG_DISCONNECT");
                }
                this.sendMessage(msg.toByteArray());
            }
        }
        catch (Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug("RECIEVED THROWABLE EXCEPTION IN Ssh1Protocol.disconnect:" + t.getMessage());
            }
        }
        finally {
            this.close();
        }
    }

    int getState() {
        return this.state;
    }

    boolean processMessage(byte[] msg) {
        switch (msg[0]) {
            case 1: {
                if (log.isDebugEnabled()) {
                    log.debug("Received SSH_MSG_DISCONNECT");
                }
                this.close();
                return true;
            }
            case 32: {
                if (log.isDebugEnabled()) {
                    log.debug("Received SSH_MSG_IGNORE");
                }
                return true;
            }
            case 36: {
                if (log.isDebugEnabled()) {
                    log.debug("Received SSH_MSG_DEBUG");
                }
                return true;
            }
        }
        return false;
    }

    boolean declareUsername(String username) throws SshException {
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            msg.write(4);
            msg.writeString(username);
            if (log.isDebugEnabled()) {
                log.debug("Sending SSH_CMSG_USER");
            }
            this.sendMessage(msg.toByteArray());
            boolean result = this.hasSucceeded();
            this.state = 2;
            boolean bl = result;
            return bl;
        }
        catch (IOException ex) {
            this.close();
            throw new SshException(ex, 1);
        }
        finally {
            try {
                msg.close();
            }
            catch (IOException iOException) {}
        }
    }

    void setupSession() throws SshException {
        block7: {
            try {
                block8: {
                    int i;
                    int cipherType = this.context.getCipherType(this.supportedCiphers);
                    if (log.isDebugEnabled()) {
                        log.debug("Selected cipher 0x" + Integer.toHexString(cipherType));
                    }
                    SshCipher decryption = this.context.createCipher(cipherType);
                    SshCipher encryption = this.context.createCipher(cipherType);
                    this.generateSessionId();
                    this.sessionKey = new byte[32];
                    ComponentManager.getInstance().getRND().nextBytes(this.sessionKey);
                    this.sendSessionKey(cipherType);
                    byte[] iv = new byte[decryption.getBlockSize()];
                    for (i = 0; i < iv.length; ++i) {
                        iv[i] = 0;
                    }
                    decryption.init(1, iv, this.sessionKey);
                    iv = new byte[encryption.getBlockSize()];
                    for (i = 0; i < iv.length; ++i) {
                        iv[i] = 0;
                    }
                    encryption.init(0, iv, this.sessionKey);
                    this.decryption = decryption;
                    this.encryption = encryption;
                    if (this.hasSucceeded()) break block7;
                    try {
                        this.transport.close();
                    }
                    catch (IOException ex) {
                        if (!log.isDebugEnabled()) break block8;
                        log.debug("RECIEVED IOException IN Ssh1Protocol.setupSession:" + ex.getMessage());
                    }
                }
                throw new SshException("The session failed to initialize!", 9);
            }
            catch (IOException ex) {
                this.close();
                throw new SshException(ex, 9);
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    boolean hasSucceeded() throws SshException {
        try {
            block16: while (true) {
                SshMessage msg = new SshMessage(this.nextMessage(0L));
                try {
                    switch (msg.getMessageId()) {
                        case 14: {
                            if (log.isDebugEnabled()) {
                                log.debug("Received SSH_SMSG_SUCCESS");
                            }
                            boolean bl = true;
                            return bl;
                        }
                        case 15: {
                            if (log.isDebugEnabled()) {
                                log.debug("Received SSH_SMSG_FAILURE");
                            }
                            boolean bl = false;
                            return bl;
                        }
                        case 1: {
                            if (!log.isDebugEnabled()) throw new SshException("The server disconnected! " + msg.readString(), 2);
                            log.debug("Received SSH_MSG_DISCONNECT");
                            throw new SshException("The server disconnected! " + msg.readString(), 2);
                        }
                        case 36: {
                            if (!log.isDebugEnabled()) continue block16;
                            log.debug("Received SSH_MSG_DEBUG");
                            continue block16;
                        }
                        case 32: {
                            if (!log.isDebugEnabled()) continue block16;
                            log.debug("Received SSH_MSG_IGNORE");
                            continue block16;
                        }
                    }
                    throw new SshException("Invalid message type " + String.valueOf(msg.getMessageId()) + " received", 3);
                }
                finally {
                    msg.close();
                    continue;
                }
                break;
            }
        }
        catch (IOException ex) {
            this.close();
            throw new SshException(ex, 1);
        }
    }

    private String makeReadable(byte[] b) {
        String s = "";
        for (int i = 0; i < b.length; ++i) {
            s = s + " " + Integer.toHexString(b[i] & 0xFF);
        }
        return s.trim();
    }

    void sendSessionKey(int cipherType) throws SshException {
        block21: {
            ByteArrayWriter msg = new ByteArrayWriter();
            try {
                byte[] key = new byte[this.sessionKey.length + 1];
                key[0] = 0;
                System.arraycopy(this.sessionKey, 0, key, 1, this.sessionKey.length);
                for (int i = 0; i < this.sessionId.length; ++i) {
                    int n = i + 1;
                    key[n] = (byte)(key[n] ^ this.sessionId[i]);
                }
                BigInteger encoded = new BigInteger(key);
                if (this.serverKey.getModulus().bitLength() <= this.hostKey.getModulus().bitLength() - 16 || this.hostKey.getModulus().bitLength() <= this.serverKey.getModulus().bitLength() - 16) {
                    int slen = (this.serverKey.getModulus().bitLength() + 7) / 8;
                    int hlen = (this.hostKey.getModulus().bitLength() + 7) / 8;
                    if (log.isDebugEnabled()) {
                        log.debug("Encoding session key with server host key slen=" + slen + " hlen=" + hlen);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Host key is    : " + this.makeReadable(this.hostKey.getEncoded()));
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Server key is  : " + this.makeReadable(this.serverKey.getEncoded()));
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Cookie is      : " + this.makeReadable(this.cookie));
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Session key is : " + this.makeReadable(this.sessionKey));
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("XORd key is    : " + this.makeReadable(key));
                    }
                    if (hlen < slen) {
                        encoded = this.hostKey.doPublic(encoded);
                        encoded = this.serverKey.doPublic(encoded);
                    } else {
                        encoded = this.serverKey.doPublic(encoded);
                        encoded = this.hostKey.doPublic(encoded);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Encoded key is : " + this.makeReadable(encoded.toByteArray()));
                    }
                    msg.write(3);
                    msg.write(cipherType);
                    msg.write(this.cookie);
                    msg.writeMPINT(encoded);
                    msg.writeInt(this.clientProtocolFlags);
                    if (log.isDebugEnabled()) {
                        log.debug("Sending SSH_CMSG_SESSION_KEY");
                    }
                    this.sendMessage(msg.toByteArray());
                    break block21;
                }
                throw new SshException("SSH 1.5 protocol violation: Server key and host key lengths do not match protocol requirements. serverbits=" + String.valueOf(this.serverKey.getModulus().bitLength()) + " hostbits=" + String.valueOf(this.hostKey.getModulus().bitLength()), 3);
            }
            catch (IOException ex) {
                this.close();
                throw new SshException("", 1);
            }
            finally {
                try {
                    msg.close();
                }
                catch (IOException iOException) {}
            }
        }
    }

    void generateSessionId() throws SshException {
        byte[] h = this.hostKey.getModulus().toByteArray();
        byte[] s = this.serverKey.getModulus().toByteArray();
        int length = h.length + s.length + this.cookie.length;
        if (h[0] == 0) {
            --length;
        }
        if (s[0] == 0) {
            --length;
        }
        byte[] tmp = new byte[length];
        if (h[0] == 0) {
            System.arraycopy(h, h[0] == 0 ? 1 : 0, tmp, 0, h.length - 1);
            length = h.length - 1;
        } else {
            System.arraycopy(h, h[0] == 0 ? 1 : 0, tmp, 0, h.length);
            length = h.length;
        }
        if (s[0] == 0) {
            System.arraycopy(s, s[0] == 0 ? 1 : 0, tmp, length, s.length - 1);
            length += s.length - 1;
        } else {
            System.arraycopy(s, s[0] == 0 ? 1 : 0, tmp, length, s.length);
            length += s.length;
        }
        System.arraycopy(this.cookie, 0, tmp, length, this.cookie.length);
        Digest hash = (Digest)ComponentManager.getInstance().supportedDigests().getInstance("MD5");
        hash.putBytes(tmp);
        this.sessionId = hash.doFinal();
    }

    void readServersPublicKey() throws SshException {
        SshMessage msg = new SshMessage(this.nextMessage(0L));
        try {
            if (msg.getMessageId() != 2) {
                throw new SshException("SSH_SMSG_PUBLIC_KEY message expected but received type " + String.valueOf(msg.getMessageId()) + " instead!", 3);
            }
            if (log.isDebugEnabled()) {
                log.debug("Received SSH_SMSG_PUBLIC_KEY");
            }
            this.cookie = new byte[8];
            msg.read(this.cookie);
            int bits = (int)msg.readInt();
            BigInteger e = msg.readMPINT();
            BigInteger n = msg.readMPINT();
            this.serverKey = ComponentManager.getInstance().createRsaPublicKey(n, e, 1);
            bits = (int)msg.readInt();
            e = msg.readMPINT();
            n = msg.readMPINT();
            this.hostKey = ComponentManager.getInstance().createRsaPublicKey(n, e, 1);
            this.serverProtocolFlags = (int)msg.readInt();
            this.supportedCiphers = (int)msg.readInt();
            this.supportedAuthentications = (int)msg.readInt();
            if (log.isDebugEnabled()) {
                log.debug("Server Protocol Flags   : 0x" + Integer.toHexString(this.serverProtocolFlags));
            }
            if (log.isDebugEnabled()) {
                log.debug("Supported Ciphers       : 0x" + Integer.toHexString(this.supportedCiphers));
            }
            if (log.isDebugEnabled()) {
                log.debug("Supported Authentication: 0x" + Integer.toHexString(this.supportedAuthentications));
            }
            if (this.context.getHostKeyVerification() != null && !this.context.getHostKeyVerification().verifyHost(this.transport.getHost(), this.hostKey)) {
                throw new SshException("The host key was not accepted.", 57349);
            }
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    byte[] readMessage(long timeout) throws SshException {
        try {
            DataInputStream dataInputStream = this.in;
            synchronized (dataInputStream) {
                int calculated;
                int checksum;
                int len = this.in.readInt();
                int total = len + 8 & 0xFFFFFFF8;
                byte[] buf = new byte[total];
                this.in.readFully(buf);
                if (this.decryption != null) {
                    this.decryption.transform(buf);
                }
                if ((checksum = (int)ByteArrayReader.readInt(buf, buf.length - 4)) != (calculated = (int)CRC32.getValue(buf, 0, total - 4))) {
                    throw new SshException("Invalid checksum detected! Received:" + checksum + " Expected:" + calculated, 3);
                }
                if (this.incomingCompression != null) {
                    throw new SshException("Compression not yet supported", 4);
                }
                byte[] msg = new byte[len - 4];
                System.arraycopy(buf, 8 - len % 8, msg, 0, msg.length);
                return msg;
            }
        }
        catch (InterruptedIOException ex) {
            throw new SshException("Interrupted IO; possible socket timeout detected?", 19);
        }
        catch (IOException ex) {
            this.close();
            throw new SshException(ex, 1);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void sendMessage(byte[] msg) throws SshException {
        try {
            DataOutputStream dataOutputStream = this.out;
            synchronized (dataOutputStream) {
                if (this.outgoingCompression != null) {
                    throw new SshException("Compression not supported yet!", 4);
                }
                int padding = 8 - (msg.length + 4) % 8;
                byte[] pad = new byte[padding];
                try (ByteArrayWriter buf = new ByteArrayWriter(msg.length + 4 + padding);){
                    if (this.encryption != null) {
                        ComponentManager.getInstance().getRND().nextBytes(pad);
                    }
                    buf.write(pad);
                    buf.write(msg);
                    byte[] tmp = buf.toByteArray();
                    buf.writeInt((int)CRC32.getValue(tmp, 0, tmp.length));
                    msg = buf.toByteArray();
                    if (this.encryption != null) {
                        this.encryption.transform(msg);
                    }
                    buf.reset();
                    buf.writeInt(msg.length - padding);
                    buf.write(msg);
                    this.out.write(buf.toByteArray());
                }
            }
        }
        catch (IOException ex) {
            this.close();
            throw new SshException(ex, 1);
        }
    }

    @Override
    public ExecutorService getExecutorService() {
        return this.context.getExecutorService();
    }

    @Override
    public String getUuid() {
        return this.uuid.toString();
    }
}

