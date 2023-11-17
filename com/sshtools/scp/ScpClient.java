/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.sshtools.scp;

import com.maverick.scp.ScpClientIO;
import com.maverick.sftp.FileTransferProgress;
import com.maverick.sftp.SftpStatusException;
import com.maverick.ssh.ChannelOpenException;
import com.maverick.ssh.Client;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.SshSession;
import com.sshtools.sftp.GlobRegExpMatching;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScpClient
extends ScpClientIO
implements Client {
    static Logger log = LoggerFactory.getLogger(ScpClient.class);
    File cwd;

    public ScpClient(SshClient ssh) {
        this(null, ssh);
    }

    public ScpClient(File cwd, SshClient ssh) {
        super(ssh);
        String homeDir = "";
        if (cwd == null) {
            try {
                homeDir = System.getProperty("user.home");
            }
            catch (SecurityException securityException) {
                // empty catch block
            }
            cwd = new File(homeDir);
        }
        this.cwd = cwd;
    }

    public void put(String localFile, String remoteFile, boolean recursive) throws SshException, ChannelOpenException, SftpStatusException {
        this.put(localFile, remoteFile, recursive, null);
    }

    public void putFile(String localFile, String remoteFile, boolean recursive, FileTransferProgress progress, boolean remoteIsDir) throws SshException, ChannelOpenException {
        File lf = new File(localFile);
        if (!lf.isAbsolute()) {
            lf = new File(this.cwd, localFile);
        }
        if (!lf.exists()) {
            throw new SshException(localFile + " does not exist", 6);
        }
        if (!lf.isFile() && !lf.isDirectory()) {
            throw new SshException(localFile + " is not a regular file or directory", 6);
        }
        if (lf.isDirectory() && !recursive) {
            throw new SshException(localFile + " is a directory, use recursive mode", 6);
        }
        if (remoteFile == null || remoteFile.equals("")) {
            remoteFile = ".";
        }
        ScpEngine scp = new ScpEngine("scp " + (lf.isDirectory() | remoteIsDir ? "-d " : "") + "-t " + (recursive ? "-r " : "") + remoteFile, this.ssh.openSessionChannel(this.windowSpace, this.packetSize, null));
        try {
            scp.waitForResponse();
            scp.writeFileToRemote(lf, recursive, progress);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException("localfile=" + localFile + " remotefile=" + remoteFile, 6, ex);
        }
        finally {
            try {
                scp.close();
            }
            catch (Throwable throwable) {}
        }
    }

    public void put(String localFileRegExp, String remoteFile, boolean recursive, FileTransferProgress progress) throws SshException, ChannelOpenException {
        File f;
        File[] fileListing;
        String[] matchedFiles;
        GlobRegExpMatching globMatcher = new GlobRegExpMatching();
        String parentDir = this.cwd.getAbsolutePath();
        String relativePath = "";
        int fileSeparatorIndex = localFileRegExp.lastIndexOf(System.getProperty("file.separator"));
        if (fileSeparatorIndex > -1 || (fileSeparatorIndex = localFileRegExp.lastIndexOf(47)) > -1) {
            relativePath = localFileRegExp.substring(0, fileSeparatorIndex + 1);
            File rel = new File(relativePath);
            parentDir = rel.isAbsolute() ? relativePath : parentDir + System.getProperty("file.separator") + relativePath;
        }
        if ((matchedFiles = globMatcher.matchFileNamesWithPattern(fileListing = (f = new File(parentDir)).listFiles(), localFileRegExp.substring(fileSeparatorIndex + 1))).length == 0) {
            throw new SshException(localFileRegExp + "No file matches/File does not exist", 6);
        }
        for (int i = 0; i < matchedFiles.length; ++i) {
            File tmp = new File(matchedFiles[i]);
            if (!tmp.isAbsolute()) {
                tmp = new File(parentDir, matchedFiles[i]);
            }
            matchedFiles[i] = tmp.getAbsolutePath();
        }
        if (matchedFiles.length > 1) {
            this.put(matchedFiles, remoteFile, recursive, progress);
        } else {
            this.putFile(matchedFiles[0], remoteFile, recursive, progress, false);
        }
    }

    public void put(String[] localFiles, String remoteFile, boolean recursive) throws SshException, ChannelOpenException {
        this.put(localFiles, remoteFile, recursive, null);
    }

    public void put(String[] localFiles, String remoteFile, boolean recursive, FileTransferProgress progress) throws SshException, ChannelOpenException {
        for (int i = 0; i < localFiles.length; ++i) {
            this.putFile(localFiles[i], remoteFile, recursive, progress, true);
        }
    }

    public void get(String localDir, String[] remoteFiles, boolean recursive) throws SshException, ChannelOpenException {
        this.get(localDir, remoteFiles, recursive, null);
    }

    public void get(String localFile, String[] remoteFiles, boolean recursive, FileTransferProgress progress) throws SshException, ChannelOpenException {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < remoteFiles.length; ++i) {
            buf.append("\"");
            buf.append(remoteFiles[i]);
            buf.append("\" ");
        }
        String remoteFile = buf.toString();
        this.get(localFile, remoteFile, recursive, progress);
    }

    public void get(String localFile, String remoteFile, boolean recursive) throws SshException, ChannelOpenException {
        this.get(localFile, remoteFile, recursive, null);
    }

    public void get(String localFile, String remoteFile, boolean recursive, FileTransferProgress progress) throws SshException, ChannelOpenException {
        File lf;
        if (localFile == null || localFile.equals("")) {
            localFile = ".";
        }
        if (!(lf = new File(localFile)).isAbsolute()) {
            lf = new File(this.cwd, localFile);
        }
        if (lf.exists() && !lf.isFile() && !lf.isDirectory()) {
            throw new SshException(localFile + " is not a regular file or directory", 6);
        }
        ScpEngine scp = new ScpEngine("scp -f " + (recursive ? "-r " : "") + remoteFile, this.ssh.openSessionChannel(this.windowSpace, this.packetSize, null));
        scp.readFromRemote(lf, progress, recursive);
        try {
            scp.close();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void exit() throws SshException, IOException {
    }

    protected class ScpEngine
    extends ScpClientIO.ScpEngineIO {
        protected ScpEngine(String cmd, SshSession session) throws SshException {
            super(cmd, session);
        }

        private boolean writeDirToRemote(File dir, boolean recursive, FileTransferProgress progress) throws SshException {
            try {
                if (!recursive) {
                    this.writeError("File " + dir.getName() + " is a directory, use recursive mode");
                    return false;
                }
                String cmd = "D0755 0 " + dir.getName() + "\n";
                this.out.write(cmd.getBytes());
                this.waitForResponse();
                String[] list = dir.list();
                for (int i = 0; i < list.length; ++i) {
                    File f = new File(dir, list[i]);
                    this.writeFileToRemote(f, recursive, progress);
                }
                this.out.write("E\n".getBytes());
                return true;
            }
            catch (IOException ex) {
                log.error("SCP write dir failed", (Throwable)ex);
                this.close();
                throw new SshException(ex, 6);
            }
        }

        private void writeFileToRemote(File file, boolean recursive, FileTransferProgress progress) throws SshException {
            try {
                if (file.isDirectory()) {
                    if (!this.writeDirToRemote(file, recursive, progress)) {
                        return;
                    }
                } else if (file.isFile()) {
                    String cmd = "C0644 " + file.length() + " " + file.getName() + "\n";
                    this.out.write(cmd.getBytes());
                    if (log.isDebugEnabled()) {
                        log.debug(cmd);
                    }
                    if (progress != null) {
                        progress.started(file.length(), file.getName());
                    }
                    this.waitForResponse();
                    FileInputStream fi = new FileInputStream(file);
                    this.writeCompleteFile(fi, file.length(), progress);
                    if (progress != null) {
                        progress.completed();
                    }
                    this.writeOk();
                } else {
                    throw new SshException(file.getName() + " not valid for SCP", 6);
                }
                this.waitForResponse();
            }
            catch (SshIOException ex) {
                throw ex.getRealException();
            }
            catch (IOException ex) {
                log.error("SCP write failed", (Throwable)ex);
                this.close();
                throw new SshException(ex, 6);
            }
        }

        private void readFromRemote(File file, FileTransferProgress progress, boolean isDir) throws SshException {
            try {
                String cmd;
                String[] cmdParts = new String[3];
                this.writeOk();
                block13: while (true) {
                    try {
                        cmd = this.readString();
                        if (log.isInfoEnabled()) {
                            log.info("SCP returned {}", (Object)cmd);
                        }
                    }
                    catch (EOFException e) {
                        return;
                    }
                    catch (SshIOException e2) {
                        return;
                    }
                    char cmdChar = cmd.charAt(0);
                    switch (cmdChar) {
                        case 'E': {
                            this.writeOk();
                            return;
                        }
                        case 'T': {
                            continue block13;
                        }
                        case 'C': 
                        case 'D': {
                            File targetFile;
                            String targetName = file.getAbsolutePath();
                            this.parseCommand(cmd, cmdParts);
                            if (file.isDirectory()) {
                                targetName = targetName + File.separator + cmdParts[2];
                            }
                            if (!(targetFile = new File(targetName)).toPath().startsWith(file.toPath())) {
                                throw new IOException(String.format("Unexpected path that is outside of the target directory %s", targetFile.getAbsolutePath()));
                            }
                            if (cmdChar == 'D') {
                                if (!isDir) {
                                    throw new IOException("Unexpected 'D' directive received from remote during file request");
                                }
                                if (targetFile.exists()) {
                                    if (!targetFile.isDirectory()) {
                                        String msg = "Invalid target " + targetFile.getName() + ", must be a directory";
                                        this.writeError(msg);
                                        throw new IOException(msg);
                                    }
                                } else if (!targetFile.mkdir()) {
                                    String msg = "Could not create directory: " + targetFile.getName();
                                    this.writeError(msg);
                                    throw new IOException(msg);
                                }
                                this.readFromRemote(targetFile, progress, true);
                                continue block13;
                            }
                            long len = Long.parseLong(cmdParts[1]);
                            FileOutputStream fo = new FileOutputStream(targetFile);
                            this.writeOk();
                            if (progress != null) {
                                progress.started(len, targetName);
                            }
                            this.readCompleteFile(fo, len, progress);
                            if (progress != null) {
                                progress.completed();
                            }
                            try {
                                this.waitForResponse();
                                this.writeOk();
                            }
                            catch (SshIOException ex) {
                                if (ex.getRealException().getReason() == 1 && !isDir) {
                                    return;
                                }
                                throw ex;
                            }
                        }
                        continue block13;
                    }
                    break;
                }
                if (log.isInfoEnabled()) {
                    log.error("Unexpected command {}", (Object)cmd);
                }
                this.writeError("Unexpected cmd: " + cmd);
                throw new IOException("SCP unexpected cmd: " + cmd);
            }
            catch (SshIOException ex) {
                throw ex.getRealException();
            }
            catch (IOException ex) {
                this.close();
                log.error("SCP write failed", (Throwable)ex);
                throw new SshException(ex, 6);
            }
        }
    }
}

