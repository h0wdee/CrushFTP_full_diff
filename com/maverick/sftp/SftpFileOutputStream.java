/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.sftp;

import com.maverick.sftp.SftpFile;
import com.maverick.sftp.SftpStatusException;
import com.maverick.sftp.SftpSubsystemChannel;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.util.UnsignedInteger32;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

public class SftpFileOutputStream
extends OutputStream {
    SftpFile file;
    SftpSubsystemChannel sftp;
    long position;
    Vector<UnsignedInteger32> outstandingRequests = new Vector();
    boolean error = false;

    public SftpFileOutputStream(SftpFile file) throws SftpStatusException, SshException {
        if (file.getHandle() == null) {
            throw new SftpStatusException(100, "The file does not have a valid handle!");
        }
        if (file.getSFTPChannel() == null) {
            throw new SshException("The file is not attached to an SFTP subsystem!", 4);
        }
        this.file = file;
        this.sftp = file.getSFTPChannel();
    }

    @Override
    public void write(byte[] buffer, int offset, int len) throws IOException {
        try {
            while (len > 0) {
                int count = Math.min(32768, len);
                this.outstandingRequests.addElement(this.sftp.postWriteRequest(this.file.getHandle(), this.position, buffer, offset, count));
                this.processNextResponse(100);
                offset += count;
                len -= count;
                this.position += (long)count;
            }
        }
        catch (SshException ex) {
            throw new SshIOException(ex);
        }
        catch (SftpStatusException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Override
    public void write(int b) throws IOException {
        try {
            byte[] array = new byte[]{(byte)b};
            this.outstandingRequests.addElement(this.sftp.postWriteRequest(this.file.getHandle(), this.position, array, 0, 1));
            this.processNextResponse(100);
            ++this.position;
        }
        catch (SshException ex) {
            throw new SshIOException(ex);
        }
        catch (SftpStatusException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    private boolean processNextResponse(int numOutstandingRequests) throws SftpStatusException, SshException {
        try {
            if (this.outstandingRequests.size() > numOutstandingRequests) {
                UnsignedInteger32 requestid = this.outstandingRequests.elementAt(0);
                this.sftp.getOKRequestStatus(requestid);
                this.outstandingRequests.removeElementAt(0);
            }
            return this.outstandingRequests.size() > 0;
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

    @Override
    public void close() throws IOException {
        try {
            while (!this.error && this.processNextResponse(0)) {
            }
            this.file.close();
        }
        catch (SshException ex) {
            throw new SshIOException(ex);
        }
        catch (SftpStatusException ex) {
            throw new IOException(ex.getMessage());
        }
        finally {
            this.outstandingRequests.clear();
        }
    }
}

