/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.sftp;

import com.maverick.sftp.SftpFile;
import com.maverick.sftp.SftpMessage;
import com.maverick.sftp.SftpStatusException;
import com.maverick.sftp.SftpSubsystemChannel;
import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.util.UnsignedInteger32;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class SftpFileInputStream
extends InputStream {
    SftpFile file;
    SftpSubsystemChannel sftp;
    long position;
    Vector<UnsignedInteger32> outstandingRequests = new Vector();
    SftpMessage currentMessage;
    int currentMessageRemaining;
    boolean isEOF = false;
    boolean error = false;
    int maximumAsyncRequests = 100;

    public SftpFileInputStream(SftpFile file) throws SftpStatusException, SshException {
        this(file, 0L);
    }

    public void setMaximumAsyncRequests(int maximumAsyncRequests) {
        if (maximumAsyncRequests < 0) {
            throw new IllegalArgumentException("maximumAsyncRequests should not be negative!");
        }
        this.maximumAsyncRequests = maximumAsyncRequests;
    }

    public SftpFileInputStream(SftpFile file, long position) throws SftpStatusException, SshException {
        if (file.getHandle() == null) {
            throw new SftpStatusException(100, "The file does not have a valid handle!");
        }
        if (file.getSFTPChannel() == null) {
            throw new SshException("The file is not attached to an SFTP subsystem!", 4);
        }
        this.file = file;
        this.position = position;
        this.sftp = file.getSFTPChannel();
    }

    @Override
    public int read(byte[] buffer, int offset, int len) throws IOException {
        try {
            if (this.isEOF && this.currentMessageRemaining == 0) {
                return -1;
            }
            int read = 0;
            int wantsLength = len;
            while (read < wantsLength && !this.isEOF) {
                if (this.currentMessage == null || this.currentMessageRemaining == 0) {
                    this.bufferNextMessage();
                    if (this.isEOF && read == 0) {
                        return -1;
                    }
                }
                if (this.currentMessage == null) {
                    throw new IOException("Failed to obtain file data or status from the SFTP server!");
                }
                int count = Math.min(this.currentMessageRemaining, len);
                System.arraycopy(this.currentMessage.array(), this.currentMessage.getPosition(), buffer, offset, count);
                this.currentMessageRemaining -= count;
                this.currentMessage.skip(count);
                if (this.currentMessageRemaining == 0) {
                    this.bufferNextMessage();
                }
                read += count;
                len -= count;
                offset += count;
            }
            return read;
        }
        catch (SshException ex) {
            throw new SshIOException(ex);
        }
        catch (SftpStatusException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    private void bufferNextMessage() throws SshException, IOException, SftpStatusException {
        try {
            this.bufferMoreData();
            UnsignedInteger32 requestid = this.outstandingRequests.elementAt(0);
            this.currentMessage = this.sftp.getResponse(requestid);
            this.outstandingRequests.removeElementAt(0);
            if (this.currentMessage.getType() != 103) {
                if (this.currentMessage.getType() == 101) {
                    int status = (int)this.currentMessage.readInt();
                    if (status == 1) {
                        this.isEOF = true;
                        return;
                    }
                    if (this.sftp.getVersion() >= 3) {
                        throw new IOException(SftpSubsystemChannel.getStatusDescription(this.currentMessage));
                    }
                    throw new IOException("Unexpected status " + status);
                }
                this.close();
                throw new IOException("The server responded with an unexpected SFTP protocol message! type=" + this.currentMessage.getType());
            }
            this.currentMessageRemaining = (int)this.currentMessage.readInt();
        }
        catch (SshException e) {
            this.error = true;
            throw e;
        }
        catch (SftpStatusException e) {
            this.error = true;
            throw e;
        }
    }

    private void bufferMoreData() throws SftpStatusException, SshException {
        while (this.outstandingRequests.size() < this.maximumAsyncRequests) {
            this.outstandingRequests.addElement(this.sftp.postReadRequest(this.file.getHandle(), this.position, 32768));
            this.position += 32768L;
        }
    }

    @Override
    public int available() {
        return this.currentMessageRemaining;
    }

    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        if (this.read(b) == 1) {
            return b[0] & 0xFF;
        }
        return -1;
    }

    @Override
    public void close() throws IOException {
        try {
            this.file.close();
            if (!AdaptiveConfiguration.getBoolean("disableSftpCloseStatus", false, this.sftp.getTransport().getHost(), this.sftp.getTransport().getIdent())) {
                while (!this.error && this.outstandingRequests.size() > 0) {
                    UnsignedInteger32 requestid = this.outstandingRequests.elementAt(0);
                    this.outstandingRequests.removeElementAt(0);
                    this.sftp.getResponse(requestid);
                }
            }
        }
        catch (SshException ex) {
            throw new SshIOException(ex);
        }
        catch (SftpStatusException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    protected void finalize() throws IOException {
        if (this.file.getHandle() != null) {
            this.close();
        }
    }
}

