/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey;

import com.maverick.ssh.Packet;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SubsystemChannel;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh2.Ssh2Session;
import com.maverick.util.ByteArrayReader;
import com.sshtools.publickey.PublicKeySubsystemException;
import com.sshtools.publickey.SshPublicKeyFileFactory;
import java.io.IOException;
import java.util.Vector;

public class PublicKeySubsystem
extends SubsystemChannel {
    static final int SSH_PUBLICKEY_SUCCESS = 0;
    static final int SSH_PUBLICKEY_ACCESS_DENIED = 1;
    static final int SSH_PUBLICKEY_STORAGE_EXCEEDED = 2;
    static final int SSH_PUBLICKEY_VERSION_NOT_SUPPORTED = 3;
    static final int SSH_PUBLICKEY_KEY_NOT_FOUND = 4;
    static final int SSH_PUBLICKEY_KEY_NOT_SUPPORTED = 5;
    static final int SSH_PUBLICKEY_KEY_ALREADY_PRESENT = 6;
    static final int SSH_PUBLICKEY_GENERAL_FAILURE = 7;
    static final int SSH_PUBLICKEY_REQUEST_NOT_SUPPORTED = 8;
    static final int VERSION_1 = 1;
    static final int VERSION_2 = 2;
    int version;

    public PublicKeySubsystem(Ssh2Session session) throws SshException {
        this(session, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PublicKeySubsystem(Ssh2Session session, int timeout) throws SshException {
        super(session, timeout);
        try {
            if (!session.startSubsystem("publickey@vandyke.com")) {
                throw new SshException("The remote side failed to start the publickey subsystem", 6);
            }
            Packet msg = this.createPacket();
            msg.writeString("version");
            msg.writeInt(1);
            this.sendMessage(msg);
            try (ByteArrayReader response = new ByteArrayReader(this.nextMessage());){
                response.readString();
                int serverVersion = (int)response.readInt();
                this.version = Math.min(serverVersion, 1);
            }
        }
        catch (IOException ex) {
            throw new SshException(5, (Throwable)ex);
        }
    }

    public void add(SshPublicKey key, String comment) throws SshException, PublicKeySubsystemException {
        try {
            Packet msg = this.createPacket();
            msg.writeString("add");
            msg.writeString(comment);
            msg.writeString(key.getAlgorithm());
            msg.writeBinaryString(key.getEncoded());
            this.sendMessage(msg);
            this.readStatusResponse();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public void remove(SshPublicKey key) throws SshException, PublicKeySubsystemException {
        try {
            Packet msg = this.createPacket();
            msg.writeString("remove");
            msg.writeString(key.getAlgorithm());
            msg.writeBinaryString(key.getEncoded());
            this.sendMessage(msg);
            this.readStatusResponse();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public SshPublicKey[] list() throws SshException, PublicKeySubsystemException {
        try {
            Packet msg = this.createPacket();
            msg.writeString("list");
            this.sendMessage(msg);
            Vector<SshPublicKey> keys = new Vector<SshPublicKey>();
            while (true) {
                ByteArrayReader response = new ByteArrayReader(this.nextMessage());
                try {
                    String type = response.readString();
                    if (type.equals("publickey")) {
                        String comment = response.readString();
                        String algorithm = response.readString();
                        keys.addElement(SshPublicKeyFileFactory.decodeSSH2PublicKey(algorithm, response.readBinaryString()));
                        continue;
                    }
                    if (!type.equals("status")) throw new SshException("The server sent an invalid response to a list command", 3);
                    int status = (int)response.readInt();
                    String desc = response.readString();
                    if (status != 0) {
                        throw new PublicKeySubsystemException(status, desc);
                    }
                    Object[] array = new SshPublicKey[keys.size()];
                    keys.copyInto(array);
                    Object[] objectArray = array;
                    return objectArray;
                }
                finally {
                    response.close();
                    continue;
                }
                break;
            }
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public void associateCommand(SshPublicKey key, String command) throws SshException, PublicKeySubsystemException {
        try {
            Packet msg = this.createPacket();
            msg.writeString("command");
            msg.writeString(key.getAlgorithm());
            msg.writeBinaryString(key.getEncoded());
            msg.writeString(command);
            this.sendMessage(msg);
            this.readStatusResponse();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    void readStatusResponse() throws SshException, PublicKeySubsystemException {
        ByteArrayReader msg = new ByteArrayReader(this.nextMessage());
        try {
            msg.readString();
            int status = (int)msg.readInt();
            String desc = msg.readString();
            if (status != 0) {
                throw new PublicKeySubsystemException(status, desc);
            }
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
        finally {
            try {
                msg.close();
            }
            catch (IOException iOException) {}
        }
    }
}

