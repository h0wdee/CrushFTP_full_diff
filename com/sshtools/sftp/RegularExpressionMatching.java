/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.sftp;

import com.maverick.sftp.SftpFile;
import com.maverick.sftp.SftpStatusException;
import com.maverick.ssh.SshException;
import java.io.File;

public interface RegularExpressionMatching {
    public SftpFile[] matchFilesWithPattern(SftpFile[] var1, String var2) throws SftpStatusException, SshException;

    public String[] matchFileNamesWithPattern(File[] var1, String var2) throws SftpStatusException, SshException;
}

