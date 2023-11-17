/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.sshtools.sftp;

import com.maverick.events.Event;
import com.maverick.events.EventServiceImplementation;
import com.maverick.sftp.FileTransferProgress;
import com.maverick.sftp.SftpFile;
import com.maverick.sftp.SftpFileAttributes;
import com.maverick.sftp.SftpFileInputStream;
import com.maverick.sftp.SftpFileOutputStream;
import com.maverick.sftp.SftpMessage;
import com.maverick.sftp.SftpStatusException;
import com.maverick.sftp.SftpSubsystemChannel;
import com.maverick.sftp.TransferCancelledException;
import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.ChannelEventListener;
import com.maverick.ssh.ChannelOpenException;
import com.maverick.ssh.Client;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.SshSession;
import com.maverick.ssh.components.Utils;
import com.maverick.ssh2.Ssh2Client;
import com.maverick.ssh2.Ssh2Context;
import com.maverick.ssh2.Ssh2Session;
import com.maverick.util.ByteArrayWriter;
import com.maverick.util.EOLProcessor;
import com.maverick.util.FileUtils;
import com.maverick.util.GlobSftpFileFilter;
import com.maverick.util.IOUtil;
import com.maverick.util.RegexSftpFileFilter;
import com.maverick.util.SftpFileFilter;
import com.maverick.util.UnsignedInteger32;
import com.maverick.util.UnsignedInteger64;
import com.sshtools.sftp.DirectoryOperation;
import com.sshtools.sftp.GlobRegExpMatching;
import com.sshtools.sftp.NoRegExpMatching;
import com.sshtools.sftp.NullOutputStream;
import com.sshtools.sftp.Perl5RegExpMatching;
import com.sshtools.sftp.RegularExpressionMatching;
import com.sshtools.sftp.RemoteHash;
import com.sshtools.sftp.StatVfs;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SftpClient
implements Client {
    static Logger log = LoggerFactory.getLogger(SftpClient.class);
    SftpSubsystemChannel sftp;
    SshSession session;
    String cwd;
    String lcwd;
    boolean disableDirectoryCheck = true;
    private int blocksize = 16384;
    private int asyncRequests = 100;
    private int buffersize = 1024000;
    int umask = 18;
    boolean applyUmask = false;
    public static final int MODE_BINARY = 1;
    public static final int MODE_TEXT = 2;
    public static final int EOL_CRLF = 1;
    public static final int EOL_LF = 2;
    public static final int EOL_CR = 3;
    private int outputEOL = 1;
    private int inputEOL = 0;
    private boolean stripEOL = false;
    private boolean forceRemoteEOL;
    private int transferMode = 1;
    private Vector<String> customRoots;
    public static final int NoSyntax = 0;
    public static final int GlobSyntax = 1;
    public static final int Perl5Syntax = 2;
    private int RegExpSyntax;

    public SftpClient(SshClient ssh) throws SftpStatusException, SshException, ChannelOpenException {
        this(ssh, 4);
    }

    public SftpClient(int timeout, SshClient ssh) throws SftpStatusException, SshException, ChannelOpenException {
        this(ssh, 4, timeout);
    }

    public SftpClient(SshClient ssh, ChannelEventListener listener) throws SftpStatusException, SshException, ChannelOpenException {
        this(ssh, 4, listener);
    }

    public SftpClient(SshClient ssh, ChannelEventListener listener, int timeout) throws SftpStatusException, SshException, ChannelOpenException {
        this(ssh, 4, timeout, listener);
    }

    public SftpClient(int timeout, SshSession session) throws SftpStatusException, SshException {
        this(session, 4, timeout);
    }

    public SftpClient(SshSession session) throws SftpStatusException, SshException {
        this(session, 4);
    }

    public SftpClient(SshSession session, int version) throws SftpStatusException, SshException {
        this(session, version, 0);
    }

    public SftpClient(SshSession session, int version, int timeout) throws SftpStatusException, SshException {
        this.customRoots = new Vector();
        this.RegExpSyntax = 1;
        this.initSftp(session, version, timeout);
    }

    public SftpClient(SshClient ssh, int version) throws SftpStatusException, SshException, ChannelOpenException {
        this(ssh, version, 0);
    }

    public SftpClient(SshClient ssh, int version, int timeout) throws SftpStatusException, SshException, ChannelOpenException {
        this(ssh, version, null);
    }

    public SftpClient(SshClient ssh, int version, ChannelEventListener listener) throws SftpStatusException, SshException, ChannelOpenException {
        this(ssh, version, 0, listener);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    public SftpClient(SshClient ssh, int version, int timeout, ChannelEventListener listener) throws SftpStatusException, SshException, ChannelOpenException {
        super();
        this.customRoots = new Vector<E>();
        this.RegExpSyntax = 1;
        session = null;
        v0 = this.disableDirectoryCheck = AdaptiveConfiguration.getBoolean("disableDirectoryCheck", false, new String[]{ssh.getTransport().getHost(), ssh.getIdent()}) == false;
        if (ssh.getClass().getName().equals("com.maverick.ssh1.Ssh1Client")) {
            try {
                session = ssh.openSessionChannel(listener);
                if (!session.executeCommand("find / -name 'sftp-server'")) ** GOTO lbl38
                reader = new BufferedReader(new InputStreamReader(session.getInputStream()));
                while ((provider = reader.readLine()) != null) {
                    if (!provider.startsWith("/") || !provider.endsWith("sftp-server")) continue;
                    ssh.getContext().setSFTPProvider(provider);
                    if (!SftpClient.log.isDebugEnabled()) continue;
                    this.debug(SftpClient.log, "Found SFTP provider " + provider, new Object[0]);
                }
                reader.close();
            }
            catch (Exception reader) {
            }
            finally {
                if (session != null) {
                    session.close();
                }
                session = ssh.openSessionChannel();
            }
        } else {
            ssh2Context = (Ssh2Context)ssh.getContext();
            session = ((Ssh2Client)ssh).openSessionChannel(AdaptiveConfiguration.getInt("sftpMaxWindowSpace", ssh2Context.getSftpMaxWindowSpace(), new String[]{ssh.getHost(), ssh.getIdent()}), AdaptiveConfiguration.getInt("sftpMaxPacketSize", ssh2Context.getSftpMaxPacketSize(), new String[]{ssh.getHost(), ssh.getIdent()}), null);
        }
lbl38:
        // 4 sources

        sftpCommand = AdaptiveConfiguration.getProperty("sftpProvider", ssh.getContext().getSFTPProvider(), new String[]{ssh.getHost(), ssh.getIdent()});
        if (session instanceof Ssh2Session) {
            ssh2 = (Ssh2Session)session;
            if (!ssh2.startSubsystem("sftp") && !ssh2.executeCommand(sftpCommand)) {
                ssh2.close();
                throw new SshException("Failed to start SFTP subsystem or SFTP provider " + ssh.getContext().getSFTPProvider(), 6);
            }
        } else if (!session.executeCommand(sftpCommand)) {
            session.close();
            throw new SshException("Failed to launch SFTP provider " + ssh.getContext().getSFTPProvider(), 6);
        }
        this.initSftp(session, version, timeout);
    }

    private void initSftp(SshSession session, int version, int timeout) throws SftpStatusException, SshException {
        this.session = session;
        this.sftp = new SftpSubsystemChannel(session, version, timeout);
        try {
            this.sftp.initialize();
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            // empty catch block
        }
        String defaultDirectory = AdaptiveConfiguration.getProperty("overrideSftpDefaultDirectory", "", session.getClient().getHost(), session.getClient().getIdent());
        if (Utils.isBlank(defaultDirectory)) {
            try {
                this.cwd = this.sftp.getDefaultDirectory();
            }
            catch (SftpStatusException ex) {
                this.cwd = this.sftp.getAbsolutePath(".");
            }
        } else {
            this.cwd = this.sftp.getAbsolutePath(defaultDirectory);
        }
        String homeDir = "";
        try {
            homeDir = System.getProperty("user.home");
        }
        catch (SecurityException securityException) {
            // empty catch block
        }
        this.lcwd = homeDir;
        EventServiceImplementation.getInstance().fireEvent(new Event((Object)session.getClient(), 22, true, session.getClient().getUuid()).addAttribute("CLIENT", session.getClient()));
    }

    protected void debug(Logger log, String msg, Object ... args) {
        log.debug(String.format("%s - %s", this.session.getClient().getUuid(), msg), args);
    }

    protected void warn(Logger log, String msg, Object ... args) {
        log.warn(String.format("%s - %s", this.session.getClient().getUuid(), msg), args);
    }

    protected void debug(Logger log, String msg, Throwable e, Object ... args) {
        log.debug(String.format("%s - %s", this.session.getClient().getUuid(), msg), (Object)e, (Object)args);
    }

    public void setBlockSize(int blocksize) {
        if (blocksize < 512) {
            throw new IllegalArgumentException("Block size must be greater than 512");
        }
        this.blocksize = blocksize;
    }

    public SftpSubsystemChannel getSubsystemChannel() {
        return this.sftp;
    }

    public void setTransferMode(int transferMode) {
        if (transferMode != 1 && transferMode != 2) {
            throw new IllegalArgumentException("Mode can only be either binary or text");
        }
        this.transferMode = transferMode;
        if (log.isDebugEnabled()) {
            this.debug(log, "Transfer mode set to " + (transferMode == 1 ? "binary" : "text"), new Object[0]);
        }
    }

    public void setDirectoryAttributeCheck(boolean directoryAttributeCheck) {
        this.disableDirectoryCheck = directoryAttributeCheck;
    }

    public void setStripEOL(boolean stripEOL) {
        this.stripEOL = stripEOL;
    }

    public void setRemoteEOL(int eolMode) {
        this.outputEOL = eolMode;
        if (log.isDebugEnabled()) {
            this.debug(log, "Remote EOL set to " + (eolMode == 1 ? "CRLF" : (eolMode == 3 ? "CR" : "LF")), new Object[0]);
        }
    }

    public void setLocalEOL(int eolMode) {
        this.inputEOL = eolMode;
        if (log.isDebugEnabled()) {
            this.debug(log, "Input EOL set to " + (eolMode == 1 ? "CRLF" : (eolMode == 3 ? "CR" : "LF")), new Object[0]);
        }
    }

    public void setForceRemoteEOL(boolean forceRemoteEOL) {
        this.forceRemoteEOL = forceRemoteEOL;
    }

    public int getTransferMode() {
        return this.transferMode;
    }

    public void setBufferSize(int buffersize) {
        this.buffersize = buffersize;
        if (log.isDebugEnabled()) {
            this.debug(log, "Buffer size set to " + buffersize, new Object[0]);
        }
    }

    public void setMaxAsyncRequests(int asyncRequests) {
        if (asyncRequests < 1) {
            throw new IllegalArgumentException("Maximum asynchronous requests must be greater or equal to 1");
        }
        this.asyncRequests = asyncRequests;
        if (log.isDebugEnabled()) {
            this.debug(log, "Max async requests set to " + asyncRequests, new Object[0]);
        }
    }

    public int umask(int umask) {
        this.applyUmask = true;
        int old = this.umask;
        this.umask = umask;
        if (log.isDebugEnabled()) {
            this.debug(log, "umask " + umask, new Object[0]);
        }
        return old;
    }

    public SftpFile openFile(String fileName) throws SftpStatusException, SshException {
        if (this.transferMode == 2 && this.sftp.getVersion() > 3) {
            return this.sftp.openFile(this.resolveRemotePath(fileName), 65);
        }
        return this.sftp.openFile(this.resolveRemotePath(fileName), 1);
    }

    public void cd(String dir) throws SftpStatusException, SshException {
        this.cd(dir, this.disableDirectoryCheck);
    }

    public void cd(String dir, boolean disableAttributeChecks) throws SftpStatusException, SshException {
        String actual;
        if (dir == null || dir.equals("")) {
            actual = this.sftp.getDefaultDirectory();
        } else {
            actual = this.resolveRemotePath(dir);
            if (!disableAttributeChecks) {
                actual = this.sftp.getAbsolutePath(actual);
            }
        }
        if (!actual.equals("") && !disableAttributeChecks) {
            SftpFileAttributes attr = null;
            try {
                attr = this.sftp.getAttributes(actual);
            }
            catch (SftpStatusException e) {
                this.warn(log, "Could not determine attributes of directory {}", actual);
                throw e;
            }
            if (attr != null && !attr.isDirectory()) {
                throw new SftpStatusException(4, dir + " is not a directory");
            }
        }
        if (log.isDebugEnabled()) {
            this.debug(log, "Changing dir from " + this.cwd + " to " + (actual.equals("") ? "user default dir" : actual), new Object[0]);
        }
        this.cwd = actual;
    }

    public String getDefaultDirectory() throws SftpStatusException, SshException {
        return this.sftp.getDefaultDirectory();
    }

    public void cdup() throws SftpStatusException, SshException {
        SftpFile cd = this.sftp.getFile(this.cwd);
        SftpFile parent = cd.getParent();
        if (parent != null) {
            this.cwd = parent.getAbsolutePath();
        }
    }

    private File resolveLocalPath(String path) {
        File f = new File(path);
        if (!f.isAbsolute()) {
            f = new File(this.lcwd, path);
        }
        return f;
    }

    private boolean isWindowsRoot(String path) {
        return path.length() > 2 && ((path.charAt(0) >= 'a' && path.charAt(0) <= 'z' || path.charAt(0) >= 'A' && path.charAt(0) <= 'Z') && path.charAt(1) == ':' && path.charAt(2) == '/' || path.charAt(2) == '\\');
    }

    public void addCustomRoot(String rootPath) {
        this.customRoots.addElement(rootPath);
    }

    public void removeCustomRoot(String rootPath) {
        this.customRoots.removeElement(rootPath);
    }

    private boolean startsWithCustomRoot(String path) {
        Enumeration<String> it = this.customRoots.elements();
        while (it != null && it.hasMoreElements()) {
            if (!path.startsWith(it.nextElement())) continue;
            return true;
        }
        return false;
    }

    private String resolveRemotePath(String path) throws SftpStatusException {
        this.verifyConnection();
        String actual = !path.startsWith("/") && !path.startsWith(this.cwd) && !this.isWindowsRoot(path) && !this.startsWithCustomRoot(path) ? this.cwd + (this.cwd.endsWith("/") ? "" : "/") + path : path;
        if (!actual.equals("/") && actual.endsWith("/")) {
            return actual.substring(0, actual.length() - 1);
        }
        return actual;
    }

    private void verifyConnection() throws SftpStatusException {
        if (this.sftp.isClosed()) {
            throw new SftpStatusException(7, "The SFTP connection has been closed");
        }
    }

    public void mkdir(String dir) throws SftpStatusException, SshException {
        String actual = this.resolveRemotePath(dir);
        if (log.isDebugEnabled()) {
            this.debug(log, "Creating dir " + dir, new Object[0]);
        }
        SftpFileAttributes attrs = null;
        try {
            attrs = this.sftp.getAttributes(actual);
        }
        catch (SftpStatusException ex) {
            SftpFileAttributes newattrs = new SftpFileAttributes(this.sftp, 2);
            if (this.applyUmask) {
                newattrs.setPermissions(new UnsignedInteger32(0x1FF ^ this.umask));
            }
            this.sftp.makeDirectory(actual, newattrs);
            return;
        }
        if (log.isDebugEnabled()) {
            this.debug(log, "File with name " + dir + " already exists!", new Object[0]);
        }
        throw new SftpStatusException(4, (attrs.isDirectory() ? "Directory" : "File") + " already exists named " + dir);
    }

    public void mkdirs(String dir) throws SftpStatusException, SshException {
        String path;
        StringTokenizer tokens = new StringTokenizer(dir, "/");
        String string = path = dir.startsWith("/") ? "/" : "";
        while (tokens.hasMoreElements()) {
            block5: {
                path = path + (String)tokens.nextElement();
                try {
                    this.stat(path);
                }
                catch (SftpStatusException ex) {
                    try {
                        this.mkdir(path);
                    }
                    catch (SftpStatusException ex2) {
                        if (ex2.getStatus() != 3) break block5;
                        throw ex2;
                    }
                }
            }
            path = path + "/";
        }
    }

    public boolean isDirectoryOrLinkedDirectory(SftpFile file) throws SftpStatusException, SshException {
        return file.isDirectory() || file.isLink() && this.stat(file.getAbsolutePath()).isDirectory();
    }

    public String pwd() {
        return this.cwd;
    }

    @Deprecated
    public SftpFile[] ls() throws SftpStatusException, SshException {
        return this.ls(this.cwd);
    }

    public SftpFile[] ls(int maximumFiles) throws SftpStatusException, SshException {
        return this.ls(this.cwd, maximumFiles);
    }

    @Deprecated
    public SftpFile[] ls(String path) throws SftpStatusException, SshException {
        return this.ls(path, 0);
    }

    public SftpFile[] ls(String path, int maximumFiles) throws SftpStatusException, SshException {
        int pageCount;
        String actual = this.resolveRemotePath(path);
        if (log.isDebugEnabled()) {
            this.debug(log, "Listing files for " + actual, new Object[0]);
        }
        SftpFile file = this.sftp.openDirectory(actual, this.disableDirectoryCheck);
        Vector<SftpFile> children = new Vector<SftpFile>();
        do {
            if ((pageCount = this.sftp.listChildren(file, children)) <= -1 || !log.isDebugEnabled()) continue;
            this.debug(log, "Got page of {} files for {}", pageCount, actual);
        } while (pageCount > -1 && (maximumFiles == 0 || children.size() < maximumFiles));
        file.close();
        SftpFile[] files = new SftpFile[children.size()];
        int index = 0;
        Enumeration<SftpFile> e = children.elements();
        while (e.hasMoreElements()) {
            files[index++] = e.nextElement();
        }
        return files;
    }

    public SftpFile[] ls(String filter, boolean regexFilter, int maximumFiles) throws SftpStatusException, SshException {
        return this.ls(this.cwd, filter, regexFilter, maximumFiles);
    }

    public SftpFile[] ls(String path, String filter, boolean regexFilter, int maximumFiles) throws SftpStatusException, SshException {
        String actual = this.resolveRemotePath(path);
        if (log.isDebugEnabled()) {
            this.debug(log, "Listing files for {} with filter {}", new Object[0]);
        }
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            int pageCount;
            byte[] handle;
            msg.writeString(actual);
            msg.writeString(filter);
            msg.writeBoolean(regexFilter);
            boolean localFiltering = false;
            try {
                handle = this.sftp.getHandleResponse(this.sftp.sendExtensionMessage("open-directory-with-filter@sshtools.com", msg.toByteArray()));
            }
            catch (SftpStatusException e) {
                if (AdaptiveConfiguration.getBoolean("disableLocalFiltering", false, this.sftp.getTransport().getHost(), this.sftp.getTransport().getIdent())) {
                    throw new SshException(57351, "Remote server does not support server side filtering");
                }
                handle = this.sftp.openDirectory(actual, this.disableDirectoryCheck).getHandle();
                localFiltering = true;
            }
            SftpFileFilter f = null;
            if (localFiltering) {
                f = regexFilter ? new RegexSftpFileFilter(filter) : new GlobSftpFileFilter(filter);
            }
            SftpFile file = new SftpFile(actual, this.sftp.getAttributes(actual));
            file.setHandle(handle);
            file.setSFTPSubsystem(this.sftp);
            Vector<SftpFile> children = new Vector<SftpFile>();
            Vector<SftpFile> tmp = new Vector<SftpFile>();
            do {
                if ((pageCount = this.sftp.listChildren(file, tmp)) <= -1) continue;
                if (!localFiltering) {
                    if (pageCount > -1 && log.isDebugEnabled()) {
                        this.debug(log, "Got page of {} files for {} with filter {}", pageCount, actual, filter, localFiltering);
                    }
                    children.addAll(tmp);
                    continue;
                }
                if (pageCount > -1 && log.isDebugEnabled()) {
                    this.debug(log, "Got page of {} files for {} before local filtering", pageCount, actual, filter, localFiltering);
                }
                int count = 0;
                for (SftpFile t : tmp) {
                    if (!f.matches(t.getFilename())) continue;
                    children.add(t);
                    ++count;
                }
                if (pageCount <= -1 || !log.isDebugEnabled()) continue;
                this.debug(log, "Got page of {} files for {} after local filtering", count, actual, filter, localFiltering);
            } while (pageCount > -1 && (maximumFiles == 0 || children.size() < maximumFiles));
            file.close();
            SftpFile[] files = new SftpFile[children.size()];
            int index = 0;
            Enumeration e = children.elements();
            while (e.hasMoreElements()) {
                files[index++] = (SftpFile)e.nextElement();
            }
            SftpFile[] sftpFileArray = files;
            return sftpFileArray;
        }
        catch (IOException e) {
            throw new SshException(5, (Throwable)e);
        }
        finally {
            try {
                msg.close();
            }
            catch (IOException iOException) {}
        }
    }

    public Iterator<SftpFile> lsIterator() throws SftpStatusException, SshException {
        return this.lsIterator(this.cwd);
    }

    public Iterator<SftpFile> lsIterator(String path) throws SftpStatusException, SshException {
        return new DirectoryIterator(path);
    }

    public void lcd(String path) throws SftpStatusException {
        File actual = !SftpClient.isLocalAbsolutePath(path) ? new File(this.lcwd, path) : new File(path);
        if (!actual.isDirectory()) {
            throw new SftpStatusException(4, path + " is not a directory");
        }
        if (log.isDebugEnabled()) {
            this.debug(log, "Changing local directory to " + path, new Object[0]);
        }
        try {
            this.lcwd = actual.getCanonicalPath();
        }
        catch (IOException ex) {
            throw new SftpStatusException(4, "Failed to canonicalize path " + path);
        }
    }

    private static boolean isLocalAbsolutePath(String path) {
        return new File(path).isAbsolute();
    }

    public String lpwd() {
        return this.lcwd;
    }

    public SftpFileAttributes get(String path, FileTransferProgress progress) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        return this.get(path, progress, false);
    }

    public SftpFileAttributes get(String path, FileTransferProgress progress, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        String localfile = path.lastIndexOf("/") > -1 ? path.substring(path.lastIndexOf("/") + 1) : path;
        return this.get(path, localfile, progress, resume);
    }

    public SftpFileAttributes get(String path, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        return this.get(path, (FileTransferProgress)null, resume);
    }

    public SftpFileAttributes get(String path) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        return this.get(path, (FileTransferProgress)null);
    }

    public SftpFileAttributes get(String remote, String local, FileTransferProgress progress) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        return this.get(remote, local, progress, false);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public SftpFileAttributes get(String remote, String local, FileTransferProgress progress, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        Object file;
        OutputStream out = null;
        SftpFileAttributes attrs = null;
        File localPath = this.resolveLocalPath(local);
        if (!localPath.exists()) {
            File parent = new File(localPath.getParent());
            parent.mkdirs();
        }
        if (localPath.isDirectory()) {
            int idx = remote.lastIndexOf(47);
            localPath = idx > -1 ? new File(localPath, remote.substring(idx)) : new File(localPath, remote);
        }
        this.stat(remote);
        long position = 0L;
        try {
            if (resume && localPath.exists()) {
                position = localPath.length();
                file = new RandomAccessFile(localPath, "rw");
                ((RandomAccessFile)file).seek(position);
                out = new RandomAccessFileOutputStream((RandomAccessFile)file);
            } else {
                out = new FileOutputStream(localPath);
            }
            attrs = this.get(remote, out, progress, position);
            file = attrs;
        }
        catch (IOException ex) {
            try {
                throw new SftpStatusException(4, "Failed to open outputstream to " + local);
            }
            catch (Throwable throwable) {
                try {
                    if (out != null) {
                        ((OutputStream)out).close();
                    }
                    if (attrs == null) throw throwable;
                    Method m = localPath.getClass().getMethod("setLastModified", Long.TYPE);
                    m.invoke(localPath, new Long(attrs.getModifiedTime().longValue() * 1000L));
                    throw throwable;
                }
                catch (Throwable throwable2) {
                    // empty catch block
                }
                throw throwable;
            }
        }
        try {
            if (out != null) {
                ((OutputStream)out).close();
            }
            if (attrs == null) return file;
            Method m = localPath.getClass().getMethod("setLastModified", Long.TYPE);
            m.invoke(localPath, new Long(attrs.getModifiedTime().longValue() * 1000L));
            return file;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return file;
    }

    public String getRemoteNewline() throws SftpStatusException {
        return new String(this.sftp.getCanonicalNewline());
    }

    public int getRemoteEOL() throws SftpStatusException {
        return this.getEOL(this.sftp.getCanonicalNewline());
    }

    public int getEOL(String line) throws SftpStatusException {
        byte[] nl = line.getBytes();
        return this.getEOL(nl);
    }

    public int getEOL(byte[] nl) throws SftpStatusException {
        switch (nl.length) {
            case 1: {
                if (nl[0] == 13) {
                    return 3;
                }
                if (nl[0] == 10) {
                    return 2;
                }
                throw new SftpStatusException(100, "Unsupported text mode: invalid newline character");
            }
            case 2: {
                if (nl[0] == 13 && nl[1] == 10) {
                    return 1;
                }
                throw new SftpStatusException(100, "Unsupported text mode: invalid newline characters");
            }
        }
        throw new SftpStatusException(100, "Unsupported text mode: newline length > 2");
    }

    public SftpFileAttributes get(String remote, String local, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        return this.get(remote, local, null, resume);
    }

    public SftpFileAttributes get(String remote, String local) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        return this.get(remote, local, false);
    }

    public SftpFileAttributes get(String remote, OutputStream local, FileTransferProgress progress) throws SftpStatusException, SshException, TransferCancelledException {
        return this.get(remote, local, progress, 0L);
    }

    public void setRegularExpressionSyntax(int syntax) {
        this.RegExpSyntax = syntax;
    }

    public SftpFile[] matchRemoteFiles(String remote) throws SftpStatusException, SshException {
        SftpFile[] files;
        RegularExpressionMatching matcher;
        String actualSearch;
        String actualDir;
        int fileSeparatorIndex = remote.lastIndexOf("/");
        if (fileSeparatorIndex > -1) {
            actualDir = remote.substring(0, fileSeparatorIndex);
            actualSearch = remote.length() > fileSeparatorIndex + 1 ? remote.substring(fileSeparatorIndex + 1) : "";
        } else {
            actualDir = this.cwd;
            actualSearch = remote;
        }
        switch (this.RegExpSyntax) {
            case 1: {
                matcher = new GlobRegExpMatching();
                files = this.ls(actualDir);
                break;
            }
            case 2: {
                matcher = new Perl5RegExpMatching();
                files = this.ls(actualDir);
                break;
            }
            default: {
                matcher = new NoRegExpMatching();
                files = new SftpFile[1];
                String actual = this.resolveRemotePath(remote);
                files[0] = this.getSubsystemChannel().getFile(actual);
            }
        }
        return matcher.matchFilesWithPattern(files, actualSearch);
    }

    private SftpFile[] getFileMatches(String remote, String local, FileTransferProgress progress, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        SftpFile[] matchedFiles = this.matchRemoteFiles(remote);
        Vector<SftpFile> retrievedFiles = new Vector<SftpFile>();
        for (int i = 0; i < matchedFiles.length; ++i) {
            this.get(matchedFiles[i].getAbsolutePath(), local, progress, resume);
            retrievedFiles.addElement(matchedFiles[i]);
        }
        Object[] retrievedSftpFiles = new SftpFile[retrievedFiles.size()];
        retrievedFiles.copyInto(retrievedSftpFiles);
        return retrievedSftpFiles;
    }

    private String[] matchLocalFiles(String local, boolean filesOnly) throws SftpStatusException, SshException {
        File[] files;
        RegularExpressionMatching matcher;
        String actualSearch;
        String actualDir;
        int fileSeparatorIndex = local.lastIndexOf(System.getProperty("file.separator"));
        if (fileSeparatorIndex > -1 || (fileSeparatorIndex = local.lastIndexOf(47)) > -1) {
            actualDir = this.resolveLocalPath(local.substring(0, fileSeparatorIndex)).getAbsolutePath();
            actualSearch = fileSeparatorIndex < local.length() - 1 ? local.substring(fileSeparatorIndex + 1) : "";
        } else {
            actualDir = this.lcwd;
            actualSearch = local;
        }
        switch (this.RegExpSyntax) {
            case 1: {
                File f = new File(actualDir);
                matcher = new GlobRegExpMatching();
                files = this.listFiles(f, filesOnly);
                break;
            }
            case 2: {
                File f = new File(actualDir);
                matcher = new Perl5RegExpMatching();
                files = this.listFiles(f, filesOnly);
                break;
            }
            default: {
                matcher = new NoRegExpMatching();
                files = new File[]{new File(local)};
            }
        }
        return matcher.matchFileNamesWithPattern(files, actualSearch);
    }

    private File[] listFiles(File f, boolean fileOnly) {
        String parentDir = f.getAbsolutePath();
        String[] fileNames = f.list();
        ArrayList<File> files = new ArrayList<File>();
        for (int i = 0; i < fileNames.length; ++i) {
            File tmp = new File(parentDir, fileNames[i]);
            if (!tmp.isFile() && fileOnly) continue;
            files.add(tmp);
        }
        return files.toArray(new File[0]);
    }

    private void putFileMatches(String local, String remote, FileTransferProgress progress, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        String remotePath = this.resolveRemotePath(remote);
        SftpFileAttributes attrs = null;
        try {
            attrs = this.stat(remotePath);
        }
        catch (SftpStatusException ex) {
            throw new SftpStatusException(ex.getStatus(), "Remote path '" + remote + "' does not exist. It must be a valid directory and must already exist!");
        }
        if (!attrs.isDirectory()) {
            throw new SftpStatusException(10, "Remote path '" + remote + "' is not a directory!");
        }
        String[] matchedFiles = this.matchLocalFiles(local, true);
        for (int i = 0; i < matchedFiles.length; ++i) {
            try {
                this.put(matchedFiles[i], remotePath, progress, resume);
                continue;
            }
            catch (SftpStatusException ex) {
                throw new SftpStatusException(ex.getStatus(), "Failed to put " + matchedFiles[i] + " to " + remote + " [" + ex.getMessage() + "]");
            }
        }
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public SftpFileAttributes get(String remote, OutputStream local, FileTransferProgress progress, long position) throws SftpStatusException, SshException, TransferCancelledException {
        String remotePath = this.resolveRemotePath(remote);
        SftpFileAttributes attrs = this.sftp.getAttributes(remotePath);
        if (position > attrs.getSize().longValue()) {
            throw new SftpStatusException(101, "The local file size is greater than the remote file");
        }
        if (progress != null) {
            progress.started(attrs.getSize().longValue() - position, remotePath);
        }
        SftpFile file = this.transferMode == 2 && this.sftp.getVersion() > 3 ? this.sftp.openFile(remotePath, 65) : this.sftp.openFile(remotePath, 1);
        try {
            if (this.transferMode == 2) {
                int inputStyle = this.outputEOL;
                int outputStyle = this.stripEOL ? 4 : this.inputEOL;
                byte[] nl = null;
                if (this.sftp.getVersion() <= 3 && this.sftp.getExtension("newline@vandyke.com") != null) {
                    nl = this.sftp.getExtension("newline@vandyke.com");
                } else if (this.sftp.getVersion() > 3) {
                    nl = this.sftp.getCanonicalNewline();
                }
                if (nl != null && !this.forceRemoteEOL) {
                    inputStyle = this.getEOL(new String(nl));
                }
                local = EOLProcessor.createOutputStream(inputStyle, outputStyle, local);
            }
            this.sftp.performOptimizedRead(file.getHandle(), attrs.getSize().longValue(), this.blocksize, local, this.asyncRequests, progress, position);
        }
        catch (IOException ex) {
            try {
                throw new SftpStatusException(4, "Failed to open text conversion outputstream");
                catch (TransferCancelledException tce) {
                    throw tce;
                }
            }
            catch (Throwable throwable) {
                try {
                    local.close();
                }
                catch (Throwable throwable2) {
                    // empty catch block
                }
                try {
                    this.sftp.closeFile(file);
                    throw throwable;
                }
                catch (SftpStatusException ex2) {
                    this.warn(log, String.format("Transfer complete but received status error during file close: %s: %d", ex2.getMessage(), ex2.getStatus()), new Object[0]);
                    throw throwable;
                }
                catch (SshException ex3) {
                    this.warn(log, String.format("Transfer complete but received error during file close: %s: %d", ex3.getMessage(), ex3.getReason()), new Object[0]);
                }
                throw throwable;
            }
        }
        try {
            local.close();
        }
        catch (Throwable inputStyle) {
            // empty catch block
        }
        try {
            this.sftp.closeFile(file);
        }
        catch (SftpStatusException ex) {
            this.warn(log, String.format("Transfer complete but received status error during file close: %s: %d", ex.getMessage(), ex.getStatus()), new Object[0]);
        }
        catch (SshException ex) {
            this.warn(log, String.format("Transfer complete but received error during file close: %s: %d", ex.getMessage(), ex.getReason()), new Object[0]);
        }
        if (progress == null) return attrs;
        progress.completed();
        return attrs;
    }

    public InputStream getInputStream(String remotefile, long position) throws SftpStatusException, SshException {
        String remotePath = this.resolveRemotePath(remotefile);
        this.sftp.getAttributes(remotePath);
        return new SftpFileInputStream(this.sftp.openFile(remotePath, 1), position);
    }

    public InputStream getInputStream(String remotefile) throws SftpStatusException, SshException {
        return this.getInputStream(remotefile, 0L);
    }

    public SftpFileAttributes get(String remote, OutputStream local, long position) throws SftpStatusException, SshException, TransferCancelledException {
        return this.get(remote, local, null, position);
    }

    public SftpFileAttributes get(String remote, OutputStream local) throws SftpStatusException, SshException, TransferCancelledException {
        return this.get(remote, local, null, 0L);
    }

    public boolean isClosed() {
        return this.sftp.isClosed();
    }

    public void put(String local, FileTransferProgress progress, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        File f = new File(local);
        this.put(local, f.getName(), progress, resume);
    }

    public void put(String local, FileTransferProgress progress) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        this.put(local, progress, false);
    }

    public void put(String local) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        this.put(local, false);
    }

    public void put(String local, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        this.put(local, (FileTransferProgress)null, resume);
    }

    public void put(String local, String remote, FileTransferProgress progress) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        this.put(local, remote, progress, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void put(String local, String remote, FileTransferProgress progress, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        File localPath = this.resolveLocalPath(local);
        FileInputStream in = new FileInputStream(localPath);
        try {
            long position = 0L;
            SftpFileAttributes attrs = null;
            try {
                attrs = this.stat(remote);
                if (attrs.isDirectory()) {
                    remote = remote + (remote.endsWith("/") ? "" : "/") + localPath.getName();
                    attrs = this.stat(remote);
                }
            }
            catch (SftpStatusException ex) {
                resume = false;
            }
            if (resume) {
                if (localPath.length() <= attrs.getSize().longValue()) {
                    throw new SftpStatusException(101, "The remote file size is greater than the local file");
                }
                try {
                    position = attrs.getSize().longValue();
                    ((InputStream)in).skip(position);
                }
                catch (IOException ex) {
                    throw new SftpStatusException(2, ex.getMessage());
                }
            }
            this.put(in, remote, progress, position);
        }
        finally {
            IOUtil.closeStream(in);
        }
    }

    public void append(String local, String remote) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        this.append(local, remote, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void append(String local, String remote, FileTransferProgress progress) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        File localPath = this.resolveLocalPath(local);
        String remotePath = this.resolveRemotePath(remote);
        this.stat(remotePath);
        FileInputStream in = new FileInputStream(localPath);
        try {
            this.append(in, remotePath, progress);
        }
        finally {
            try {
                ((InputStream)in).close();
            }
            catch (IOException iOException) {}
        }
    }

    public void put(String local, String remote, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        this.put(local, remote, null, resume);
    }

    public void put(String local, String remote) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        this.put(local, remote, null, false);
    }

    public void put(InputStream in, String remote, FileTransferProgress progress) throws SftpStatusException, SshException, TransferCancelledException {
        this.put(in, remote, progress, 0L);
    }

    public void append(InputStream in, String remote) throws SftpStatusException, SshException, TransferCancelledException {
        this.put(in, remote, null, -1L);
    }

    public void append(InputStream in, String remote, FileTransferProgress progress) throws SftpStatusException, SshException, TransferCancelledException {
        this.put(in, remote, progress, -1L);
    }

    public void put(InputStream in, String remote, FileTransferProgress progress, long position) throws SftpStatusException, SshException, TransferCancelledException {
        String remotePath = this.resolveRemotePath(remote);
        SftpFileAttributes attrs = null;
        if (this.transferMode == 2) {
            int inputStyle = this.stripEOL ? 4 : this.inputEOL;
            int outputStyle = this.outputEOL;
            byte[] nl = null;
            if (this.sftp.getVersion() <= 3 && this.sftp.getExtension("newline@vandyke.com") != null) {
                nl = this.sftp.getExtension("newline@vandyke.com");
            } else if (this.sftp.getVersion() > 3) {
                nl = this.sftp.getCanonicalNewline();
            }
            if (nl != null & !this.forceRemoteEOL) {
                outputStyle = this.getEOL(nl);
            }
            try {
                in = EOLProcessor.createInputStream(inputStyle, outputStyle, in);
            }
            catch (IOException ex) {
                throw new SshException("Failed to create EOL processing stream", 5);
            }
        }
        attrs = new SftpFileAttributes(this.sftp, 1);
        if (this.applyUmask) {
            attrs.setPermissions(new UnsignedInteger32(0x1B6 ^ this.umask));
        }
        if (position > 0L) {
            if (this.transferMode == 2 && this.sftp.getVersion() > 3) {
                throw new SftpStatusException(8, "Resume on text mode files is not supported");
            }
            this.internalPut(in, remotePath, progress, position, 2, attrs);
        } else if (position == 0L) {
            if (this.transferMode == 2 && this.sftp.getVersion() > 3) {
                this.internalPut(in, remotePath, progress, position, 90, attrs);
            } else {
                this.internalPut(in, remotePath, progress, position, 26, attrs);
            }
        } else if (this.transferMode == 2 && this.sftp.getVersion() > 3) {
            this.internalPut(in, remotePath, progress, position, 70, attrs);
        } else {
            this.internalPut(in, remotePath, progress, position, 6, attrs);
        }
    }

    private void internalPut(InputStream in, String remotePath, FileTransferProgress progress, long position, int flags, SftpFileAttributes attrs) throws SftpStatusException, SshException, TransferCancelledException {
        SftpFile file = this.sftp.openFile(remotePath, flags, attrs);
        if (progress != null) {
            try {
                progress.started(in.available(), remotePath);
            }
            catch (IOException ex1) {
                throw new SshException("Failed to determine local file size", 5);
            }
        }
        try {
            this.sftp.performOptimizedWrite(file.getHandle(), this.blocksize, this.asyncRequests, in, this.buffersize, progress, position < 0L ? 0L : position);
        }
        catch (SftpStatusException e) {
            log.error("SFTP status exception during transfer [" + e.getStatus() + "]", (Throwable)e);
            throw e;
        }
        catch (SshException e) {
            log.error("SSH exception during transfer [" + e.getReason() + "]", (Throwable)e);
            if (e.getCause() != null) {
                log.error("SSH exception cause", e.getCause());
            }
            throw e;
        }
        catch (TransferCancelledException e) {
            log.error("Transfer cancelled", (Throwable)e);
            throw e;
        }
        finally {
            try {
                in.close();
            }
            catch (Throwable throwable) {}
            this.sftp.closeFile(file);
        }
        if (progress != null) {
            progress.completed();
        }
    }

    public OutputStream getOutputStream(String remotefile) throws SftpStatusException, SshException {
        String remotePath = this.resolveRemotePath(remotefile);
        return new SftpFileOutputStream(this.sftp.openFile(remotePath, 26));
    }

    public void put(InputStream in, String remote, long position) throws SftpStatusException, SshException, TransferCancelledException {
        this.put(in, remote, null, position);
    }

    public void put(InputStream in, String remote) throws SftpStatusException, SshException, TransferCancelledException {
        this.put(in, remote, null, 0L);
    }

    public void chown(String uid, String path) throws SftpStatusException, SshException {
        String actual = this.resolveRemotePath(path);
        SftpFileAttributes attrs = this.sftp.getAttributes(actual);
        SftpFileAttributes newAttrs = new SftpFileAttributes(this.sftp, attrs.getType());
        newAttrs.setUID(uid);
        if (this.sftp.getVersion() <= 3) {
            newAttrs.setGID(attrs.getGID());
        }
        this.sftp.setAttributes(actual, newAttrs);
    }

    public void chgrp(String gid, String path) throws SftpStatusException, SshException {
        String actual = this.resolveRemotePath(path);
        SftpFileAttributes attrs = this.sftp.getAttributes(actual);
        SftpFileAttributes newAttrs = new SftpFileAttributes(this.sftp, attrs.getType());
        newAttrs.setGID(gid);
        if (this.sftp.getVersion() <= 3) {
            newAttrs.setUID(attrs.getUID());
        }
        this.sftp.setAttributes(actual, newAttrs);
    }

    public void chmod(int permissions, String path) throws SftpStatusException, SshException {
        String actual = this.resolveRemotePath(path);
        this.sftp.changePermissions(actual, permissions);
    }

    public void umask(String umask) throws SshException {
        try {
            this.umask = Integer.parseInt(umask, 8);
            this.applyUmask = true;
        }
        catch (NumberFormatException ex) {
            throw new SshException("umask must be 4 digit octal number e.g. 0022", 4);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void rename(String oldpath, String newpath, boolean posix) throws IOException, SftpStatusException, SshException {
        if (posix) {
            try (ByteArrayWriter msg = new ByteArrayWriter();){
                msg.writeString(this.resolveRemotePath(oldpath));
                msg.writeString(this.resolveRemotePath(newpath));
                this.sftp.getOKRequestStatus(this.sftp.sendExtensionMessage("posix-rename@openssh.com", msg.toByteArray()));
            }
        } else {
            this.rename(oldpath, newpath);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void copyRemoteFile(String sourceFile, String destinationFile, boolean overwriteDestination) throws SftpStatusException, SshException, IOException {
        try (ByteArrayWriter msg = new ByteArrayWriter();){
            msg.writeString(this.resolveRemotePath(sourceFile));
            msg.writeString(this.resolveRemotePath(destinationFile));
            msg.writeBoolean(overwriteDestination);
            this.sftp.getOKRequestStatus(this.sftp.sendExtensionMessage("copy-file", msg.toByteArray()));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void copyRemoteData(SftpFile sourceFile, UnsignedInteger64 fromOffset, UnsignedInteger64 length, SftpFile destinationFile, UnsignedInteger64 toOffset) throws SftpStatusException, SshException, IOException {
        if (!sourceFile.isOpen() || !destinationFile.isOpen()) {
            throw new SftpStatusException(9, "source and desintation files must be open");
        }
        try (ByteArrayWriter msg = new ByteArrayWriter();){
            msg.writeBinaryString(sourceFile.getHandle());
            msg.writeUINT64(fromOffset);
            msg.writeUINT64(length);
            msg.writeBinaryString(destinationFile.getHandle());
            msg.writeUINT64(toOffset);
            this.sftp.getOKRequestStatus(this.sftp.sendExtensionMessage("copy-data", msg.toByteArray()));
        }
    }

    public void rename(String oldpath, String newpath) throws SftpStatusException, SshException {
        String from = this.resolveRemotePath(oldpath);
        String to = this.resolveRemotePath(newpath);
        SftpFileAttributes attrs = null;
        try {
            attrs = this.sftp.getAttributes(to);
        }
        catch (SftpStatusException ex) {
            this.sftp.renameFile(from, to);
            return;
        }
        if (attrs == null || !attrs.isDirectory()) {
            throw new SftpStatusException(11, newpath + " already exists on the remote filesystem");
        }
        this.sftp.renameFile(from, FileUtils.checkEndsWithSlash(to) + FileUtils.lastPathElement(from));
    }

    public void rm(String path) throws SftpStatusException, SshException {
        String actual = this.resolveRemotePath(path);
        SftpFileAttributes attrs = this.sftp.getAttributes(actual);
        if (attrs.isDirectory()) {
            this.sftp.removeDirectory(actual);
        } else {
            this.sftp.removeFile(actual);
        }
    }

    public void rm(String path, boolean force, boolean recurse) throws SftpStatusException, SshException {
        String actual = this.resolveRemotePath(path);
        SftpFileAttributes attrs = null;
        attrs = this.sftp.getAttributes(actual);
        if (attrs.isDirectory()) {
            SftpFile[] list = this.ls(path);
            if (!force && list.length > 0) {
                throw new SftpStatusException(4, "You cannot delete non-empty directory, use force=true to overide");
            }
            for (int i = 0; i < list.length; ++i) {
                SftpFile file = list[i];
                if (file.isDirectory() && !file.getFilename().equals(".") && !file.getFilename().equals("..")) {
                    if (recurse) {
                        this.rm(file.getAbsolutePath(), force, recurse);
                        continue;
                    }
                    throw new SftpStatusException(4, "Directory has contents, cannot delete without recurse=true");
                }
                if (!file.isFile()) continue;
                this.sftp.removeFile(file.getAbsolutePath());
            }
            this.sftp.removeDirectory(actual);
        } else {
            this.sftp.removeFile(actual);
        }
    }

    public void symlink(String path, String link) throws SftpStatusException, SshException {
        String actualPath = this.resolveRemotePath(path);
        String actualLink = this.resolveRemotePath(link);
        this.sftp.createSymbolicLink(actualLink, actualPath);
    }

    public SftpFileAttributes stat(String path) throws SftpStatusException, SshException {
        String actual = this.resolveRemotePath(path);
        return this.sftp.getAttributes(actual);
    }

    public SftpFileAttributes statLink(String path) throws SftpStatusException, SshException {
        String actual = this.resolveRemotePath(path);
        return this.sftp.getLinkAttributes(actual);
    }

    public String getAbsolutePath(String path) throws SftpStatusException, SshException {
        String actual = this.resolveRemotePath(path);
        return this.sftp.getAbsolutePath(actual);
    }

    public boolean verifyFiles(String localFile, String remoteFile) throws SftpStatusException, SshException, IOException {
        return this.verifyFiles(localFile, remoteFile, 0L, 0L);
    }

    public boolean verifyFiles(String localFile, String remoteFile, RemoteHash algorithm) throws SftpStatusException, SshException, IOException {
        return this.verifyFiles(localFile, remoteFile, 0L, 0L, algorithm);
    }

    public boolean verifyFiles(String localFile, String remoteFile, long offset, long length) throws SftpStatusException, SshException, IOException {
        return this.verifyFiles(localFile, remoteFile, offset, length, RemoteHash.md5);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean verifyFiles(String localFile, String remoteFile, long offset, long length, RemoteHash algorithm) throws SftpStatusException, SshException, IOException {
        File local = this.resolveLocalPath(localFile);
        if (!local.exists()) {
            throw new IOException("Local file " + localFile + " does not exist!");
        }
        DigestInputStream dis = null;
        try {
            byte[] remoteHash;
            MessageDigest md;
            block16: {
                md = null;
                switch (algorithm) {
                    case md5: {
                        md = MessageDigest.getInstance("MD5");
                        break;
                    }
                    case sha1: {
                        md = MessageDigest.getInstance("SHA-1");
                        break;
                    }
                    case sha256: {
                        md = MessageDigest.getInstance("SHA-256");
                        break;
                    }
                    case sha512: {
                        md = MessageDigest.getInstance("SHA-512");
                    }
                }
                remoteHash = this.getRemoteHash(remoteFile, offset, length, algorithm);
                if (log.isDebugEnabled()) {
                    log.debug("Remote hash for {} is {}", (Object)remoteFile, (Object)Utils.bytesToHex(remoteHash));
                }
                try {
                    dis = new DigestInputStream(new FileInputStream(local), md);
                    if (offset > 0L) {
                        dis.skip(offset);
                    }
                    if (length > 0L) {
                        IOUtil.copy(dis, new NullOutputStream(), length);
                        break block16;
                    }
                    IOUtil.copy(dis, new NullOutputStream());
                }
                catch (Throwable throwable) {
                    IOUtil.closeStream(dis);
                    throw throwable;
                }
            }
            IOUtil.closeStream(dis);
            byte[] localHash = md.digest();
            if (log.isDebugEnabled()) {
                log.debug("Local hash for {} is {}", (Object)localFile, (Object)Utils.bytesToHex(localHash));
            }
            return Arrays.equals(remoteHash, localHash);
        }
        catch (NoSuchAlgorithmException e1) {
            throw new SshException(5, (Throwable)e1);
        }
        catch (IOException e1) {
            throw new SshException(5, (Throwable)e1);
        }
    }

    @Deprecated
    public byte[] getRemoteHash(String remoteFile) throws IOException, SftpStatusException, SshException {
        return this.getRemoteHash(remoteFile, 0L, 0L, new byte[0]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    public byte[] getRemoteHash(String remoteFile, long offset, long length, byte[] quickCheck) throws IOException, SftpStatusException, SshException {
        try (ByteArrayWriter msg = new ByteArrayWriter();){
            msg.writeString(this.resolveRemotePath(remoteFile));
            msg.writeUINT64(offset);
            msg.writeUINT64(length);
            msg.writeBinaryString(quickCheck);
            SftpMessage resp = this.sftp.getExtensionResponse(this.sftp.sendExtensionMessage("md5-hash", msg.toByteArray()));
            resp.readString();
            byte[] byArray = resp.readBinaryString();
            return byArray;
        }
    }

    @Deprecated
    public byte[] getRemoteHash(byte[] handle) throws IOException, SftpStatusException, SshException {
        return this.getRemoteHash(handle, 0L, 0L, new byte[0]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    public byte[] getRemoteHash(byte[] handle, long offset, long length, byte[] quickCheck) throws IOException, SftpStatusException, SshException {
        try (ByteArrayWriter msg = new ByteArrayWriter();){
            msg.writeBinaryString(handle);
            msg.writeUINT64(offset);
            msg.writeUINT64(length);
            msg.writeBinaryString(quickCheck);
            SftpMessage resp = this.sftp.getExtensionResponse(this.sftp.sendExtensionMessage("md5-hash-handle", msg.toByteArray()));
            resp.readString();
            byte[] byArray = resp.readBinaryString();
            return byArray;
        }
    }

    public byte[] getRemoteHash(byte[] handle, RemoteHash algorithm) throws IOException, SftpStatusException, SshException {
        return this.getRemoteHash(handle, 0L, 0L, algorithm);
    }

    public byte[] getRemoteHash(byte[] handle, long offset, long length, RemoteHash algorithm) throws IOException, SftpStatusException, SshException {
        return this.doCheckHashHandle(handle, offset, length, algorithm);
    }

    public byte[] getRemoteHash(String path, RemoteHash algorithm) throws IOException, SftpStatusException, SshException {
        return this.getRemoteHash(path, 0L, 0L, algorithm);
    }

    public byte[] getRemoteHash(String path, long offset, long length, RemoteHash algorithm) throws IOException, SftpStatusException, SshException {
        String actual = this.resolveRemotePath(path);
        return this.doCheckFileHandle(actual, offset, length, algorithm);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected byte[] doCheckHashHandle(byte[] handle, long offset, long length, RemoteHash algorithm) throws IOException, SftpStatusException, SshException {
        try (ByteArrayWriter msg = new ByteArrayWriter();){
            msg.writeBinaryString(handle);
            msg.writeString(algorithm.name());
            msg.writeUINT64(offset);
            msg.writeUINT64(length);
            msg.writeInt(0L);
            byte[] byArray = this.processCheckFileResponse(this.sftp.getExtensionResponse(this.sftp.sendExtensionMessage("check-file-handle", msg.toByteArray())), algorithm);
            return byArray;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected byte[] doCheckFileHandle(String filename, long offset, long length, RemoteHash algorithm) throws IOException, SftpStatusException, SshException {
        try (ByteArrayWriter msg = new ByteArrayWriter();){
            msg.writeString(filename);
            msg.writeString(algorithm.name());
            msg.writeUINT64(offset);
            msg.writeUINT64(length);
            msg.writeInt(0L);
            byte[] byArray = this.processCheckFileResponse(this.sftp.getExtensionResponse(this.sftp.sendExtensionMessage("check-file-name", msg.toByteArray())), algorithm);
            return byArray;
        }
    }

    protected byte[] processCheckFileResponse(SftpMessage resp, RemoteHash algorithm) throws IOException {
        int hashLength;
        String processedAlgorithm = resp.readString();
        if (!processedAlgorithm.equals(algorithm.name())) {
            throw new IOException("Remote server returned a hash in an unsupported algorithm");
        }
        switch (algorithm) {
            case md5: {
                hashLength = 16;
                break;
            }
            case sha1: {
                hashLength = 20;
                break;
            }
            case sha256: {
                hashLength = 32;
                break;
            }
            case sha512: {
                hashLength = 64;
                break;
            }
            default: {
                throw new IOException("Unsupported hash algorihm " + processedAlgorithm);
            }
        }
        byte[] hash = new byte[hashLength];
        if (resp.available() < hash.length) {
            throw new IOException("Unexpected hash length returned by remote server");
        }
        resp.readFully(hash);
        return hash;
    }

    public void quit() throws SshException {
        if (log.isDebugEnabled()) {
            this.debug(log, "Closing SftpClient", new Object[0]);
        }
        try {
            this.sftp.close();
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex1) {
            throw new SshException(ex1.getMessage(), 6);
        }
    }

    @Override
    public void exit() throws SshException {
        if (log.isDebugEnabled()) {
            this.debug(log, "Closing SftpClient", new Object[0]);
        }
        try {
            this.sftp.close();
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex1) {
            throw new SshException(ex1.getMessage(), 6);
        }
    }

    public DirectoryOperation copyLocalDirectory(String localdir, String remotedir, boolean recurse, boolean sync, boolean commit, FileTransferProgress progress) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        return this.putLocalDirectory(localdir, remotedir, recurse, sync, commit, progress);
    }

    public DirectoryOperation putLocalDirectory(String localdir, String remotedir, boolean recurse, boolean sync, boolean commit, FileTransferProgress progress) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        String[] ls;
        DirectoryOperation op = new DirectoryOperation();
        File local = this.resolveLocalPath(localdir);
        remotedir = this.resolveRemotePath(remotedir);
        remotedir = remotedir + (remotedir.endsWith("/") ? "" : "/");
        if (commit) {
            try {
                this.sftp.getAttributes(remotedir);
            }
            catch (SftpStatusException ex) {
                this.mkdirs(remotedir);
            }
        }
        if ((ls = local.list()) != null) {
            for (int i = 0; i < ls.length; ++i) {
                SftpFileAttributes attrs;
                File source = new File(local, ls[i]);
                if (source.isDirectory() && !source.getName().equals(".") && !source.getName().equals("..")) {
                    if (!recurse) continue;
                    op.addDirectoryOperation(this.putLocalDirectory(source.getAbsolutePath(), remotedir + source.getName(), recurse, sync, commit, progress), source);
                    continue;
                }
                if (!source.isFile()) continue;
                boolean newFile = false;
                boolean unchangedFile = false;
                try {
                    attrs = this.sftp.getAttributes(remotedir + source.getName());
                    unchangedFile = source.length() == attrs.getSize().longValue() && source.lastModified() / 1000L == attrs.getModifiedTime().longValue();
                }
                catch (SftpStatusException ex) {
                    newFile = true;
                }
                try {
                    if (commit && !unchangedFile) {
                        this.put(source.getAbsolutePath(), remotedir + source.getName(), progress);
                        attrs = this.sftp.getAttributes(remotedir + source.getName());
                        attrs.setTimes(new UnsignedInteger64(source.lastModified() / 1000L), new UnsignedInteger64(source.lastModified() / 1000L));
                        this.sftp.setAttributes(remotedir + source.getName(), attrs);
                    }
                    if (unchangedFile) {
                        op.addUnchangedFile(source);
                        continue;
                    }
                    if (!newFile) {
                        op.addUpdatedFile(source);
                        continue;
                    }
                    op.addNewFile(source);
                    continue;
                }
                catch (SftpStatusException ex) {
                    op.addFailedTransfer(source, ex);
                }
            }
        }
        if (sync) {
            try {
                SftpFile[] files = this.ls(remotedir);
                for (int i = 0; i < files.length; ++i) {
                    SftpFile file = files[i];
                    File f = new File(local, file.getFilename());
                    if (op.containsFile(f) || file.getFilename().equals(".") || file.getFilename().equals("..")) continue;
                    op.addDeletedFile(file);
                    if (!commit) continue;
                    if (file.isDirectory()) {
                        this.recurseMarkForDeletion(file, op);
                        if (!commit) continue;
                        this.rm(file.getAbsolutePath(), true, true);
                        continue;
                    }
                    if (!file.isFile()) continue;
                    this.rm(file.getAbsolutePath());
                }
            }
            catch (SftpStatusException sftpStatusException) {
                // empty catch block
            }
        }
        return op;
    }

    private void recurseMarkForDeletion(SftpFile file, DirectoryOperation op) throws SftpStatusException, SshException {
        SftpFile[] list = this.ls(file.getAbsolutePath());
        op.addDeletedFile(file);
        for (int i = 0; i < list.length; ++i) {
            file = list[i];
            if (file.isDirectory() && !file.getFilename().equals(".") && !file.getFilename().equals("..")) {
                this.recurseMarkForDeletion(file, op);
                continue;
            }
            if (!file.isFile()) continue;
            op.addDeletedFile(file);
        }
    }

    private void recurseMarkForDeletion(File file, DirectoryOperation op) throws SftpStatusException, SshException {
        String[] list = file.list();
        op.addDeletedFile(file);
        if (list != null) {
            for (int i = 0; i < list.length; ++i) {
                file = new File(list[i]);
                if (file.isDirectory() && !file.getName().equals(".") && !file.getName().equals("..")) {
                    this.recurseMarkForDeletion(file, op);
                    continue;
                }
                if (!file.isFile()) continue;
                op.addDeletedFile(file);
            }
        }
    }

    public static String formatLongname(SftpFile file) throws SftpStatusException, SshException {
        return SftpClient.formatLongname(file.getAttributes(), file.getFilename());
    }

    public static String formatLongname(SftpFileAttributes attrs, String filename) {
        StringBuffer str = new StringBuffer();
        str.append(SftpClient.pad(10 - attrs.getPermissionsString().length()) + attrs.getPermissionsString());
        str.append("    1 ");
        str.append(attrs.getUID() + SftpClient.pad(8 - attrs.getUID().length()));
        str.append(" ");
        str.append(attrs.getGID() + SftpClient.pad(8 - attrs.getGID().length()));
        str.append(" ");
        str.append(SftpClient.pad(12 - attrs.getSize().toString().length()) + attrs.getSize().toString());
        str.append(" ");
        str.append(SftpClient.pad(12 - SftpClient.getModTimeString(attrs.getModifiedTime()).length()) + SftpClient.getModTimeString(attrs.getModifiedTime()));
        str.append(" ");
        str.append(filename);
        return str.toString();
    }

    private static String getModTimeString(UnsignedInteger64 mtime) {
        if (mtime == null) {
            return "";
        }
        long mt = mtime.longValue() * 1000L;
        long now = System.currentTimeMillis();
        SimpleDateFormat df = now - mt > 15552000000L ? new SimpleDateFormat("MMM dd  yyyy") : new SimpleDateFormat("MMM dd hh:mm");
        return df.format(new Date(mt));
    }

    private static String pad(int num) {
        StringBuffer strBuf = new StringBuffer("");
        if (num > 0) {
            for (int i = 0; i < num; ++i) {
                strBuf.append(" ");
            }
        }
        return strBuf.toString();
    }

    @Deprecated
    public DirectoryOperation copyRemoteDirectory(String remotedir, String localdir, boolean recurse, boolean sync, boolean commit, FileTransferProgress progress) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        return this.getRemoteDirectory(remotedir, localdir, recurse, sync, commit, progress);
    }

    public DirectoryOperation getRemoteDirectory(String remotedir, String localdir, boolean recurse, boolean sync, boolean commit, FileTransferProgress progress) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        String[] contents;
        File local;
        int idx;
        DirectoryOperation op = new DirectoryOperation();
        String pwd = this.pwd();
        this.cd(remotedir);
        String base = remotedir;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if ((idx = base.lastIndexOf(47)) != -1) {
            base = base.substring(idx + 1);
        }
        if (!(local = new File(localdir)).isAbsolute()) {
            local = new File(this.lpwd(), localdir);
        }
        if (!local.exists() && commit) {
            local.mkdir();
        }
        SftpFile[] files = this.ls();
        for (int i = 0; i < files.length; ++i) {
            File f;
            SftpFile file = files[i];
            if (file.isDirectory() && !file.getFilename().equals(".") && !file.getFilename().equals("..")) {
                if (!recurse) continue;
                f = new File(local, file.getFilename());
                op.addDirectoryOperation(this.getRemoteDirectory(file.getFilename(), local.getAbsolutePath() + "/" + file.getFilename(), recurse, sync, commit, progress), f);
                continue;
            }
            if (!file.isFile()) continue;
            f = new File(local, file.getFilename());
            if (f.exists() && f.length() == file.getAttributes().getSize().longValue() && f.lastModified() / 1000L == file.getAttributes().getModifiedTime().longValue()) {
                if (commit) {
                    op.addUnchangedFile(f);
                    continue;
                }
                op.addUnchangedFile(file);
                continue;
            }
            try {
                if (f.exists()) {
                    if (commit) {
                        op.addUpdatedFile(f);
                    } else {
                        op.addUpdatedFile(file);
                    }
                } else if (commit) {
                    op.addNewFile(f);
                } else {
                    op.addNewFile(file);
                }
                if (!commit) continue;
                this.get(file.getFilename(), f.getAbsolutePath(), progress);
                continue;
            }
            catch (SftpStatusException ex) {
                op.addFailedTransfer(f, ex);
            }
        }
        if (sync && (contents = local.list()) != null) {
            for (int i = 0; i < contents.length; ++i) {
                File f2 = new File(local, contents[i]);
                if (op.containsFile(f2)) continue;
                op.addDeletedFile(f2);
                if (f2.isDirectory() && !f2.getName().equals(".") && !f2.getName().equals("..")) {
                    this.recurseMarkForDeletion(f2, op);
                    if (!commit) continue;
                    IOUtil.recurseDeleteDirectory(f2);
                    continue;
                }
                if (!commit) continue;
                f2.delete();
            }
        }
        this.cd(pwd);
        return op;
    }

    public SftpFile[] getFiles(String remote) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        return this.getFiles(remote, (FileTransferProgress)null);
    }

    public SftpFile[] getFiles(String remote, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        return this.getFiles(remote, (FileTransferProgress)null, resume);
    }

    public SftpFile[] getFiles(String remote, FileTransferProgress progress) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        return this.getFiles(remote, progress, false);
    }

    public SftpFile[] getFiles(String remote, FileTransferProgress progress, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        return this.getFiles(remote, this.lcwd, progress, resume);
    }

    public SftpFile[] getFiles(String remote, String local) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        return this.getFiles(remote, local, false);
    }

    public SftpFile[] getFiles(String remote, String local, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        return this.getFiles(remote, local, null, resume);
    }

    public SftpFile[] getFiles(String remote, String local, FileTransferProgress progress, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        return this.getFileMatches(remote, local, progress, resume);
    }

    public void putFiles(String local) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        this.putFiles(local, false);
    }

    public void putFiles(String local, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        this.putFiles(local, (FileTransferProgress)null, resume);
    }

    public void putFiles(String local, FileTransferProgress progress) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        this.putFiles(local, progress, false);
    }

    public void putFiles(String local, FileTransferProgress progress, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        this.putFiles(local, this.pwd(), progress, resume);
    }

    public void putFiles(String local, String remote) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        this.putFiles(local, remote, null, false);
    }

    public void putFiles(String local, String remote, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        this.putFiles(local, remote, null, resume);
    }

    public void putFiles(String local, String remote, FileTransferProgress progress) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        this.putFiles(local, remote, progress, false);
    }

    public void putFiles(String local, String remote, FileTransferProgress progress, boolean resume) throws FileNotFoundException, SftpStatusException, SshException, TransferCancelledException {
        this.putFileMatches(local, remote, progress, resume);
    }

    public StatVfs statVFS(String path) throws SshException, SftpStatusException {
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            msg.writeString(path);
            SftpSubsystemChannel channel = this.getSubsystemChannel();
            UnsignedInteger32 requestId = channel.sendExtensionMessage("statvfs@openssh.com", msg.toByteArray());
            StatVfs statVfs = new StatVfs(channel.getResponse(requestId));
            return statVfs;
        }
        catch (IOException e) {
            throw new SshException(e);
        }
        finally {
            try {
                msg.close();
            }
            catch (IOException iOException) {}
        }
    }

    public void hardlink(String src, String dst) throws SshException, SftpStatusException {
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            msg.writeString(src);
            msg.writeString(dst);
            SftpSubsystemChannel channel = this.getSubsystemChannel();
            UnsignedInteger32 requestId = channel.sendExtensionMessage("hardlink@openssh.com", msg.toByteArray());
            channel.getOKRequestStatus(requestId);
        }
        catch (IOException e) {
            throw new SshException(e);
        }
        finally {
            try {
                msg.close();
            }
            catch (IOException iOException) {}
        }
    }

    public String getHomeDirectory(String username) throws SshException, SftpStatusException {
        ByteArrayWriter msg = new ByteArrayWriter();
        try {
            msg.writeString(username);
            SftpSubsystemChannel channel = this.getSubsystemChannel();
            UnsignedInteger32 requestId = channel.sendExtensionMessage("home-directory", msg.toByteArray());
            String string = channel.getSingleFileResponse(channel.getResponse(requestId), "SSH_FXP_NAME").getAbsolutePath();
            return string;
        }
        catch (IOException e) {
            throw new SshException(e);
        }
        finally {
            try {
                msg.close();
            }
            catch (IOException iOException) {}
        }
    }

    public String makeTemporaryFolder() throws SshException, SftpStatusException {
        SftpSubsystemChannel channel = this.getSubsystemChannel();
        UnsignedInteger32 requestId = channel.sendExtensionMessage("make-temp-folder", null);
        return channel.getSingleFileResponse(channel.getResponse(requestId), "SSH_FXP_NAME").getAbsolutePath();
    }

    public String getTemporaryFolder() throws SshException, SftpStatusException {
        SftpSubsystemChannel channel = this.getSubsystemChannel();
        UnsignedInteger32 requestId = channel.sendExtensionMessage("get-temp-folder", null);
        return channel.getSingleFileResponse(channel.getResponse(requestId), "SSH_FXP_NAME").getAbsolutePath();
    }

    class DirectoryIterator
    implements Iterator<SftpFile> {
        SftpFile currentFolder;
        Vector<SftpFile> currentPage = new Vector();
        Iterator<SftpFile> currentIterator;

        DirectoryIterator(String path) throws SftpStatusException, SshException {
            String actual = SftpClient.this.resolveRemotePath(path);
            if (log.isDebugEnabled()) {
                SftpClient.this.debug(log, "Listing files for " + actual, new Object[0]);
            }
            this.currentFolder = SftpClient.this.sftp.openDirectory(actual);
            try {
                this.getNextPage();
            }
            catch (EOFException eOFException) {
                // empty catch block
            }
        }

        private void getNextPage() throws SftpStatusException, SshException, EOFException {
            this.currentPage.clear();
            int ret = SftpClient.this.sftp.listChildren(this.currentFolder, this.currentPage);
            if (ret == -1) {
                this.currentIterator = null;
                throw new EOFException();
            }
            this.currentIterator = this.currentPage.iterator();
        }

        @Override
        public boolean hasNext() {
            return this.currentIterator != null && this.currentIterator.hasNext();
        }

        @Override
        public SftpFile next() {
            if (this.currentIterator == null) {
                throw new NoSuchElementException();
            }
            SftpFile ret = null;
            if (this.currentIterator.hasNext()) {
                ret = this.currentIterator.next();
            }
            if (!this.currentIterator.hasNext()) {
                try {
                    this.getNextPage();
                }
                catch (EOFException e) {
                    if (ret == null) {
                        throw new NoSuchElementException();
                    }
                }
                catch (SftpStatusException | SshException e) {
                    throw new NoSuchElementException(e.getMessage());
                }
                if (ret == null) {
                    ret = this.currentIterator.next();
                }
            }
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    static class RandomAccessFileOutputStream
    extends OutputStream {
        RandomAccessFile file;

        RandomAccessFileOutputStream(RandomAccessFile file) {
            this.file = file;
        }

        @Override
        public void write(int b) throws IOException {
            this.file.write(b);
        }

        @Override
        public void write(byte[] buf, int off, int len) throws IOException {
            this.file.write(buf, off, len);
        }

        @Override
        public void close() throws IOException {
            this.file.close();
        }
    }
}

