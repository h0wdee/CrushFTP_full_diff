/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh;

import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.Packet;
import com.maverick.ssh.SocketTimeoutSupport;
import com.maverick.ssh.SshChannel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubsystemChannel {
    static Logger log = LoggerFactory.getLogger(SubsystemChannel.class);
    DataInputStream in;
    DataOutputStream out;
    Vector<Packet> packets = new Vector();
    int maximumPacketSize;
    protected SshChannel channel;
    Reader reader = new Reader();
    Writer writer = new Writer();
    boolean startofStream = true;

    public SubsystemChannel(SshChannel channel, int timeout) throws SshException {
        this.channel = channel;
        this.maximumPacketSize = AdaptiveConfiguration.getInt("subsystemMaxPacketSize", 1024000, channel.getClient().getHost(), channel.getClient().getIdent());
        try {
            this.in = new DataInputStream(new BufferedInputStream(channel.getInputStream()));
            SocketTimeoutSupport s = (SocketTimeoutSupport)((Object)channel.getInputStream());
            s.setSoTimeout(timeout);
            this.out = new DataOutputStream(channel.getOutputStream());
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex.getMessage(), 6);
        }
    }

    protected void debug(Logger log, String msg, Object ... args) {
        log.debug(String.format("%s - %s", this.channel.getClient().getUuid(), msg), args);
    }

    protected void debug(Logger log, String msg, Throwable e, Object ... args) {
        log.debug(String.format("%s - %s", this.channel.getClient().getUuid(), msg), (Object)e, (Object)args);
    }

    public boolean isClosed() {
        return this.channel.isClosed();
    }

    public void close() throws IOException {
        this.packets.removeAllElements();
        this.channel.close();
    }

    public byte[] nextMessage() throws SshException {
        return this.reader.readMessage(this.in);
    }

    protected void sendMessage(Packet msg) throws SshException {
        this.writer.sendMessage(msg);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected Packet createPacket() throws IOException {
        Vector<Packet> vector = this.packets;
        synchronized (vector) {
            if (this.packets.size() == 0) {
                return new Packet();
            }
            Packet p = this.packets.elementAt(0);
            this.packets.removeElementAt(0);
            return p;
        }
    }

    class Reader {
        Reader() {
        }

        synchronized byte[] readMessage(DataInputStream in) throws SshException {
            int len = -1;
            try {
                if (SubsystemChannel.this.startofStream) {
                    do {
                        in.mark(1024);
                    } while (in.read() > 0);
                    in.reset();
                    SubsystemChannel.this.startofStream = false;
                }
                if ((len = in.readInt()) < 0) {
                    throw new SshException("Negative message length in SFTP protocol.", 3);
                }
                if (len > SubsystemChannel.this.maximumPacketSize) {
                    throw new SshException("Invalid message length in SFTP protocol [" + len + "]", 3);
                }
                byte[] msg = new byte[len];
                in.readFully(msg);
                return msg;
            }
            catch (OutOfMemoryError ex) {
                throw new SshException("Invalid message length in SFTP protocol [" + len + "]", 3);
            }
            catch (EOFException ex) {
                if (log.isDebugEnabled()) {
                    SubsystemChannel.this.debug(log, "Received EOF exception during subsystem message read", ex, new Object[0]);
                }
                try {
                    SubsystemChannel.this.close();
                }
                catch (SshIOException ex1) {
                    throw ex1.getRealException();
                }
                catch (IOException ex1) {
                    throw new SshException(ex1.getMessage(), 6);
                }
                throw new SshException("The channel unexpectedly terminated", 6);
            }
            catch (IOException ex) {
                if (log.isDebugEnabled()) {
                    SubsystemChannel.this.debug(log, "Received IO exception during subsystem message read", ex, new Object[0]);
                }
                if (ex instanceof SshIOException) {
                    throw ((SshIOException)ex).getRealException();
                }
                try {
                    SubsystemChannel.this.close();
                }
                catch (SshIOException ex2) {
                    throw ex2.getRealException();
                }
                catch (IOException ex1) {
                    throw new SshException(ex1.getMessage(), 6);
                }
                throw new SshException(6, (Throwable)ex);
            }
        }
    }

    class Writer {
        Writer() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        synchronized void sendMessage(Packet msg) throws SshException {
            try {
                msg.finish();
                SubsystemChannel.this.out.write(msg.array(), 0, msg.size());
            }
            catch (SshIOException ex) {
                if (log.isDebugEnabled()) {
                    SubsystemChannel.this.debug(log, "Received SSH exception during subsystem message write", ex.getRealException(), new Object[0]);
                }
                throw ex.getRealException();
            }
            catch (EOFException ex) {
                if (log.isDebugEnabled()) {
                    SubsystemChannel.this.debug(log, "Received EOF exception during subsystem message write", ex, new Object[0]);
                }
                try {
                    SubsystemChannel.this.close();
                }
                catch (SshIOException ex1) {
                    throw ex1.getRealException();
                }
                catch (IOException ex1) {
                    throw new SshException(ex1.getMessage(), 6);
                }
                throw new SshException("The channel unexpectedly terminated", 6);
            }
            catch (IOException ex) {
                if (log.isDebugEnabled()) {
                    SubsystemChannel.this.debug(log, "Received IO exception during subsystem message write", ex, new Object[0]);
                }
                try {
                    SubsystemChannel.this.close();
                }
                catch (SshIOException ex2) {
                    throw ex2.getRealException();
                }
                catch (IOException ex1) {
                    throw new SshException(ex1.getMessage(), 6);
                }
                throw new SshException("Unknown channel IO failure: " + ex.getMessage(), 6);
            }
            finally {
                msg.reset();
                Vector<Packet> vector = SubsystemChannel.this.packets;
                synchronized (vector) {
                    SubsystemChannel.this.packets.addElement(msg);
                }
            }
        }
    }
}

