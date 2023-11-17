/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.sftp;

import com.maverick.sftp.SftpFileAttributes;
import com.maverick.sftp.SftpStatusException;
import com.maverick.sftp.SftpSubsystemChannel;
import com.maverick.ssh.SshException;

public class SftpFile {
    String filename;
    byte[] handle;
    SftpFileAttributes attrs;
    SftpSubsystemChannel sftp;
    String absolutePath;
    String longname;

    public SftpFile(String path, SftpFileAttributes attrs) {
        this.absolutePath = path;
        this.attrs = attrs;
        if (this.absolutePath.equals("/")) {
            this.filename = "/";
        } else {
            int i;
            if (this.absolutePath.endsWith("/")) {
                this.absolutePath = this.absolutePath.substring(0, this.absolutePath.length() - 1);
            }
            this.filename = (i = this.absolutePath.lastIndexOf(47)) > -1 ? this.absolutePath.substring(i + 1) : this.absolutePath;
        }
    }

    public SftpFile getParent() throws SshException, SftpStatusException {
        if (this.absolutePath.lastIndexOf(47) == -1) {
            String dir = this.sftp.getDefaultDirectory();
            return this.sftp.getFile(dir);
        }
        String path = this.sftp.getAbsolutePath(this.absolutePath);
        if (path.equals("/")) {
            return null;
        }
        if (this.filename.equals(".") || this.filename.equals("..")) {
            return this.sftp.getFile(path).getParent();
        }
        int idx = path.lastIndexOf(47);
        String parent = path.substring(0, idx);
        if (parent.equals("")) {
            parent = "/";
        }
        return this.sftp.getFile(parent);
    }

    public String toString() {
        return this.absolutePath;
    }

    public int hashCode() {
        return this.absolutePath.hashCode();
    }

    public String getLongname() {
        return this.longname;
    }

    public boolean equals(Object obj) {
        if (obj instanceof SftpFile) {
            boolean match = ((SftpFile)obj).getAbsolutePath().equals(this.absolutePath);
            if (this.handle == null && ((SftpFile)obj).handle == null) {
                return match;
            }
            if (this.handle != null && ((SftpFile)obj).handle != null) {
                for (int i = 0; i < this.handle.length; ++i) {
                    if (((SftpFile)obj).handle[i] == this.handle[i]) continue;
                    return false;
                }
            }
            return match;
        }
        return false;
    }

    public void delete() throws SftpStatusException, SshException {
        if (this.sftp == null) {
            throw new SshException("Instance not connected to SFTP subsystem", 4);
        }
        if (this.isDirectory()) {
            this.sftp.removeDirectory(this.getAbsolutePath());
        } else {
            this.sftp.removeFile(this.getAbsolutePath());
        }
    }

    public boolean canWrite() throws SftpStatusException, SshException {
        return (this.getAttributes().getPermissions().longValue() & 0x80L) == 128L;
    }

    public boolean canRead() throws SftpStatusException, SshException {
        return (this.getAttributes().getPermissions().longValue() & 0x100L) == 256L;
    }

    public boolean isOpen() {
        if (this.sftp == null) {
            return false;
        }
        return this.sftp.isValidHandle(this.handle);
    }

    public void setHandle(byte[] handle) {
        this.handle = handle;
    }

    public byte[] getHandle() {
        return this.handle;
    }

    public void setSFTPSubsystem(SftpSubsystemChannel sftp) {
        this.sftp = sftp;
    }

    public SftpSubsystemChannel getSFTPChannel() {
        return this.sftp;
    }

    public String getFilename() {
        return this.filename;
    }

    public SftpFileAttributes getAttributes() throws SftpStatusException, SshException {
        if (this.attrs == null) {
            this.attrs = this.sftp.getAttributes(this.getAbsolutePath());
        }
        return this.attrs;
    }

    public String getAbsolutePath() {
        return this.absolutePath;
    }

    public void close() throws SftpStatusException, SshException {
        this.sftp.closeFile(this);
    }

    public boolean isDirectory() throws SftpStatusException, SshException {
        return this.getAttributes().isDirectory();
    }

    public boolean isFile() throws SftpStatusException, SshException {
        return this.getAttributes().isFile();
    }

    public boolean isLink() throws SftpStatusException, SshException {
        return this.getAttributes().isLink();
    }

    public boolean isFifo() throws SftpStatusException, SshException {
        return (this.getAttributes().getPermissions().longValue() & 0x1000L) == 4096L;
    }

    public boolean isBlock() throws SftpStatusException, SshException {
        return (this.getAttributes().getPermissions().longValue() & 0x6000L) == 24576L;
    }

    public boolean isCharacter() throws SftpStatusException, SshException {
        return (this.getAttributes().getPermissions().longValue() & 0x2000L) == 8192L;
    }

    public boolean isSocket() throws SftpStatusException, SshException {
        return (this.getAttributes().getPermissions().longValue() & 0xC000L) == 49152L;
    }
}

