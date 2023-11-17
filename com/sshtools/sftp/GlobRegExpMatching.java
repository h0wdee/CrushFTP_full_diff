/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.sftp;

import com.maverick.sftp.SftpFile;
import com.maverick.sftp.SftpStatusException;
import com.maverick.ssh.SshException;
import com.sshtools.sftp.RegularExpressionMatching;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;

public class GlobRegExpMatching
implements RegularExpressionMatching {
    @Override
    public String[] matchFileNamesWithPattern(File[] files, String pattern) throws SshException {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        ArrayList<String> matched = new ArrayList<String>();
        for (File file : files) {
            Path path = Paths.get(file.getName(), new String[0]);
            if (!matcher.matches(path)) continue;
            matched.add(file.getAbsolutePath());
        }
        return matched.toArray(new String[0]);
    }

    @Override
    public SftpFile[] matchFilesWithPattern(SftpFile[] files, String pattern) throws SftpStatusException, SshException {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        ArrayList<SftpFile> matched = new ArrayList<SftpFile>();
        for (SftpFile file : files) {
            if (!matcher.matches(Paths.get(file.getFilename(), new String[0]))) continue;
            matched.add(file);
        }
        return matched.toArray(new SftpFile[0]);
    }
}

