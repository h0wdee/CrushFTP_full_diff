/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.sftp;

import com.maverick.sftp.SftpFile;
import com.maverick.sftp.SftpStatusException;
import com.maverick.ssh.SshException;
import com.sshtools.sftp.RegularExpressionMatching;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Perl5RegExpMatching
implements RegularExpressionMatching {
    @Override
    public String[] matchFileNamesWithPattern(File[] files, String pattern) throws SshException {
        Pattern p = Pattern.compile(pattern);
        ArrayList<String> matched = new ArrayList<String>();
        for (File file : files) {
            if (!p.matcher(file.getName()).matches()) continue;
            matched.add(file.getAbsolutePath());
        }
        return matched.toArray(new String[0]);
    }

    @Override
    public SftpFile[] matchFilesWithPattern(SftpFile[] files, String pattern) throws SftpStatusException, SshException {
        Pattern p = Pattern.compile(pattern);
        ArrayList<SftpFile> matched = new ArrayList<SftpFile>();
        for (SftpFile file : files) {
            if (!p.matcher(file.getFilename()).matches()) continue;
            matched.add(file);
        }
        return matched.toArray(new SftpFile[0]);
    }
}

