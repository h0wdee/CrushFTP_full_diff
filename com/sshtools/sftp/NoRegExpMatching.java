/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.sftp;

import com.maverick.sftp.SftpFile;
import com.maverick.sftp.SftpStatusException;
import com.maverick.ssh.SshException;
import com.sshtools.sftp.RegularExpressionMatching;
import java.io.File;

public class NoRegExpMatching
implements RegularExpressionMatching {
    @Override
    public String[] matchFileNamesWithPattern(File[] files, String fileNameRegExp) throws SshException, SftpStatusException {
        String[] thefile = new String[files.length];
        for (int i = 0; i < files.length; ++i) {
            thefile[i] = files[i].getAbsolutePath();
        }
        return thefile;
    }

    @Override
    public SftpFile[] matchFilesWithPattern(SftpFile[] files, String fileNameRegExp) throws SftpStatusException, SshException {
        return files;
    }
}

