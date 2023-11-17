/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.sftp;

import com.maverick.sftp.SftpMessage;
import com.maverick.util.UnsignedInteger64;
import java.io.IOException;

public class StatVfs {
    public static final int SSH_FXE_STATVFS_ST_RDONLY = 1;
    public static final int SSH_FXE_STATVFS_ST_NOSUID = 2;
    UnsignedInteger64 f_bsize;
    UnsignedInteger64 f_frsize;
    UnsignedInteger64 f_blocks;
    UnsignedInteger64 f_bfree;
    UnsignedInteger64 f_bavail;
    UnsignedInteger64 f_files;
    UnsignedInteger64 f_ffree;
    UnsignedInteger64 f_favail;
    UnsignedInteger64 f_fsid;
    UnsignedInteger64 f_flag;
    UnsignedInteger64 f_namemax;

    StatVfs(SftpMessage msg) throws IOException {
        this.f_bsize = msg.readUINT64();
        this.f_frsize = msg.readUINT64();
        this.f_blocks = msg.readUINT64();
        this.f_bfree = msg.readUINT64();
        this.f_bavail = msg.readUINT64();
        this.f_files = msg.readUINT64();
        this.f_ffree = msg.readUINT64();
        this.f_favail = msg.readUINT64();
        this.f_fsid = msg.readUINT64();
        this.f_flag = msg.readUINT64();
        this.f_namemax = msg.readUINT64();
    }

    public long getBlockSize() {
        return this.f_bsize.longValue();
    }

    public long getFragmentSize() {
        return this.f_frsize.longValue();
    }

    public long getBlocks() {
        return this.f_blocks.longValue();
    }

    public long getFreeBlocks() {
        return this.f_bfree.longValue();
    }

    public long getAvailBlocks() {
        return this.f_bavail.longValue();
    }

    public long getINodes() {
        return this.f_files.longValue();
    }

    public long getFreeINodes() {
        return this.f_ffree.longValue();
    }

    public long getAvailINodes() {
        return this.f_favail.longValue();
    }

    public long getFileSystemID() {
        return this.f_fsid.longValue();
    }

    public long getMountFlag() {
        return this.f_flag.longValue();
    }

    public long getMaximumFilenameLength() {
        return this.f_namemax.longValue();
    }

    public long getSize() {
        return this.getFragmentSize() * this.getBlocks() / 1024L;
    }

    public long getUsed() {
        return this.getFragmentSize() * (this.getBlocks() - this.getFreeBlocks()) / 1024L;
    }

    public long getAvailForNonRoot() {
        return this.getFragmentSize() * this.getAvailBlocks() / 1024L;
    }

    public long getAvail() {
        return this.getFragmentSize() * this.getFreeBlocks() / 1024L;
    }

    public int getCapacity() {
        return (int)(100L * (this.getBlocks() - this.getFreeBlocks()) / this.getBlocks());
    }
}

