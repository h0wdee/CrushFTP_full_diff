/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.sftp;

import com.maverick.events.Event;
import com.maverick.events.EventServiceImplementation;
import com.maverick.sftp.FileTransferProgress;
import com.maverick.sftp.SftpFile;
import com.maverick.sftp.SftpFileAttributes;
import com.maverick.sftp.SftpMessage;
import com.maverick.sftp.SftpStatusException;
import com.maverick.sftp.TransferCancelledException;
import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.Packet;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.SshSession;
import com.maverick.ssh.SubsystemChannel;
import com.maverick.ssh.components.Utils;
import com.maverick.ssh.message.MessageHolder;
import com.maverick.ssh2.TransportProtocol;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import com.maverick.util.IOUtil;
import com.maverick.util.UnsignedInteger32;
import com.maverick.util.UnsignedInteger64;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SftpSubsystemChannel
extends SubsystemChannel {
    static Logger log = LoggerFactory.getLogger(SftpSubsystemChannel.class);
    private String CHARSET_ENCODING = "ISO-8859-1";
    public static final int OPEN_READ = 1;
    public static final int OPEN_WRITE = 2;
    public static final int OPEN_APPEND = 4;
    public static final int OPEN_CREATE = 8;
    public static final int OPEN_TRUNCATE = 16;
    public static final int OPEN_EXCLUSIVE = 32;
    public static final int OPEN_TEXT = 64;
    static final int STATUS_FX_OK = 0;
    static final int STATUS_FX_EOF = 1;
    static final int SSH_FXP_INIT = 1;
    static final int SSH_FXP_VERSION = 2;
    static final int SSH_FXP_OPEN = 3;
    static final int SSH_FXP_CLOSE = 4;
    static final int SSH_FXP_READ = 5;
    static final int SSH_FXP_WRITE = 6;
    static final int SSH_FXP_LSTAT = 7;
    static final int SSH_FXP_FSTAT = 8;
    static final int SSH_FXP_SETSTAT = 9;
    static final int SSH_FXP_FSETSTAT = 10;
    static final int SSH_FXP_OPENDIR = 11;
    static final int SSH_FXP_READDIR = 12;
    static final int SSH_FXP_REMOVE = 13;
    static final int SSH_FXP_MKDIR = 14;
    static final int SSH_FXP_RMDIR = 15;
    static final int SSH_FXP_REALPATH = 16;
    static final int SSH_FXP_STAT = 17;
    static final int SSH_FXP_RENAME = 18;
    static final int SSH_FXP_READLINK = 19;
    static final int SSH_FXP_SYMLINK = 20;
    static final int SSH_FXP_LINK = 21;
    static final int SSH_FXP_BLOCK = 22;
    static final int SSH_FXP_UNBLOCK = 23;
    static final int SSH_FXP_STATUS = 101;
    static final int SSH_FXP_HANDLE = 102;
    static final int SSH_FXP_DATA = 103;
    static final int SSH_FXP_NAME = 104;
    static final int SSH_FXP_ATTRS = 105;
    static final int SSH_FXP_EXTENDED = 200;
    static final int SSH_FXP_EXTENDED_REPLY = 201;
    public static final int MAX_VERSION = 4;
    Long supportedAttributeMask;
    Long supportedAttributeBits;
    Long supportedOpenFileFlags;
    Long supportedAccessMask;
    short supportedOpenBlockVector;
    short supportedBlockVector;
    Integer maxReadSize;
    Set<String> supportedExtensions = new HashSet<String>();
    Set<String> supportedAttrExtensions = new HashSet<String>();
    int version = 4;
    int serverVersion = -1;
    UnsignedInteger32 requestId = new UnsignedInteger32(0L);
    Map<UnsignedInteger32, SftpMessage> responses = new HashMap<UnsignedInteger32, SftpMessage>();
    SftpThreadSynchronizer sync = new SftpThreadSynchronizer();
    Map<String, byte[]> extensions = new HashMap<String, byte[]>();
    public static final int SSH_FXF_ACCESS_DISPOSITION = 7;
    public static final int SSH_FXF_CREATE_NEW = 0;
    public static final int SSH_FXF_CREATE_TRUNCATE = 1;
    public static final int SSH_FXF_OPEN_EXISTING = 2;
    public static final int SSH_FXF_OPEN_OR_CREATE = 3;
    public static final int SSH_FXF_TRUNCATE_EXISTING = 4;
    public static final int SSH_FXF_ACCESS_APPEND_DATA = 8;
    public static final int SSH_FXF_ACCESS_APPEND_DATA_ATOMIC = 16;
    public static final int SSH_FXF_ACCESS_TEXT_MODE = 32;
    public static final int SSH_FXF_ACCESS_BLOCK_READ = 64;
    public static final int SSH_FXF_ACCESS_BLOCK_WRITE = 128;
    public static final int SSH_FXF_ACCESS_BLOCK_DELETE = 256;
    public static final int SSH_FXF_ACCESS_BLOCK_ADVISORY = 512;
    public static final int SSH_FXF_NOFOLLOW = 1024;
    public static final int SSH_FXF_DELETE_ON_CLOSE = 2048;
    public static final int SSH_FXF_ACCESS_AUDIT_ALARM_INFO = 4096;
    public static final int SSH_FXF_ACCESS_BACKUP = 8192;
    public static final int SSH_FXF_BACKUP_STREAM = 16384;
    public static final int SSH_FXF_OVERRIDE_OWNER = 32768;
    public static final int SSH_FXP_RENAME_OVERWRITE = 1;
    public static final int SSH_FXP_RENAME_ATOMIC = 2;
    public static final int SSH_FXP_RENAME_NATIVE = 4;
    SshClient client;
    boolean performVerification = false;

    public SftpSubsystemChannel(SshSession session) throws SshException {
        this(session, 4);
    }

    public SftpSubsystemChannel(SshSession session, int maximumVersion) throws SshException {
        this(session, maximumVersion, 0);
    }

    public SftpSubsystemChannel(SshSession session, int maximumVersion, int timeout) throws SshException {
        super(session, timeout);
        this.client = session.getClient();
        this.version = maximumVersion;
    }

    public int getVersion() {
        return this.version;
    }

    public void enableVerification() {
        if (log.isDebugEnabled()) {
            this.debug(log, "Enabled remote verification", new Object[0]);
        }
        this.performVerification = true;
    }

    public void disableVerification() {
        if (log.isDebugEnabled()) {
            this.debug(log, "Disabled remote verification", new Object[0]);
        }
        this.performVerification = false;
    }

    public TransportProtocol getTransport() {
        try {
            this.getClass();
            Class<?> clz = Class.forName("com.maverick.ssh2.Ssh2Client");
            Field transportField = clz.getDeclaredField("transport");
            transportField.setAccessible(true);
            return (TransportProtocol)transportField.get(this.client);
        }
        catch (Throwable e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public byte[] getCanonicalNewline() throws SftpStatusException {
        if (this.version <= 3) {
            throw new SftpStatusException(8, "Newline setting not available for SFTP versions <= 3");
        }
        if (!this.extensions.containsKey("newline")) {
            return "\r\n".getBytes();
        }
        return this.extensions.get("newline");
    }

    @Override
    public void close() throws IOException {
        this.responses.clear();
        super.close();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void initialize() throws SshException, UnsupportedEncodingException {
        try {
            Packet packet = this.createPacket();
            packet.write(1);
            packet.writeInt(this.version);
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_INIT channel=%d version=%d", this.channel.getChannelId(), this.version), new Object[0]);
            }
            this.sendMessage(packet);
            byte[] msg = this.nextMessage();
            if (msg[0] != 2) {
                this.close();
                throw new SshException("Unexpected response from SFTP subsystem.", 6);
            }
            try (ByteArrayReader bar = new ByteArrayReader(msg);){
                bar.skip(1L);
                this.serverVersion = (int)bar.readInt();
                int requestedVersion = this.version;
                this.version = Math.min(this.serverVersion, this.version);
                if (log.isDebugEnabled()) {
                    this.debug(log, "Version is " + this.version + " [Server=" + this.serverVersion + " Client=" + requestedVersion + "]", new Object[0]);
                }
                try {
                    while (bar.available() > 0) {
                        String name = bar.readString();
                        byte[] data = bar.readBinaryString();
                        this.extensions.put(name, data);
                        if (!log.isDebugEnabled()) continue;
                        this.debug(log, "Processed extension '" + name + "'", new Object[0]);
                    }
                }
                catch (Throwable name) {
                    // empty catch block
                }
                if (this.version == 5) {
                    if (this.extensions.containsKey("supported")) {
                        this.processSupported(this.extensions.get("supported"));
                    }
                } else if (this.version >= 6 && this.extensions.containsKey("supported2")) {
                    this.processSupported2(this.extensions.get("supported2"));
                }
                if (this.version <= 3) {
                    this.setCharsetEncoding("ISO-8859-1");
                } else if (this.extensions.containsKey("filename-charset")) {
                    String newCharset = new String(this.extensions.get("filename-charset"), "UTF-8");
                    try {
                        this.setCharsetEncoding(newCharset);
                        this.sendExtensionMessage("filename-translation-control", new byte[]{0});
                    }
                    catch (Exception e) {
                        this.setCharsetEncoding("UTF8");
                        this.sendExtensionMessage("filename-translation-control", new byte[]{1});
                    }
                } else {
                    this.setCharsetEncoding("UTF8");
                }
            }
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(6, (Throwable)ex);
        }
        catch (Throwable t) {
            throw new SshException(6, t);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void processSupported2(byte[] data) throws IOException {
        try (ByteArrayReader supportedStructure = new ByteArrayReader(data);){
            String ext;
            int i;
            int count;
            this.supportedAttributeMask = supportedStructure.readInt();
            this.supportedAttributeBits = supportedStructure.readInt();
            this.supportedOpenFileFlags = supportedStructure.readInt();
            this.supportedAccessMask = supportedStructure.readInt();
            this.maxReadSize = (int)supportedStructure.readInt();
            this.supportedOpenBlockVector = supportedStructure.readShort();
            this.supportedBlockVector = supportedStructure.readShort();
            if (supportedStructure.available() >= 4) {
                count = (int)supportedStructure.readInt();
                for (i = 0; i < count; ++i) {
                    ext = supportedStructure.readString();
                    if (log.isDebugEnabled()) {
                        this.debug(log, "Server supports '" + ext + "' attribute extension", new Object[0]);
                    }
                    this.supportedAttrExtensions.add(ext);
                }
            }
            if (supportedStructure.available() >= 4) {
                count = (int)supportedStructure.readInt();
                for (i = 0; i < count; ++i) {
                    ext = supportedStructure.readString();
                    if (log.isDebugEnabled()) {
                        this.debug(log, "Server supports '" + ext + "' extension", new Object[0]);
                    }
                    this.supportedExtensions.add(ext);
                }
            }
            if (log.isDebugEnabled()) {
                this.debug(log, "supported-attribute-mask: " + this.supportedAttributeMask.toString(), new Object[0]);
                this.debug(log, "supported-attribute-bits: " + this.supportedAttributeBits.toString(), new Object[0]);
                this.debug(log, "supported-open-flags: " + this.supportedOpenFileFlags.toString(), new Object[0]);
                this.debug(log, "supported-access-mask: " + this.supportedAccessMask.toString(), new Object[0]);
                this.debug(log, "max-read-size: " + this.maxReadSize.toString(), new Object[0]);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void processSupported(byte[] data) throws IOException {
        try (ByteArrayReader supportedStructure = new ByteArrayReader(data);){
            this.supportedAttributeMask = supportedStructure.readInt();
            this.supportedAttributeBits = supportedStructure.readInt();
            this.supportedOpenFileFlags = supportedStructure.readInt();
            this.supportedAccessMask = supportedStructure.readInt();
            this.maxReadSize = (int)supportedStructure.readInt();
            if (supportedStructure.available() >= 4) {
                int count = (int)supportedStructure.readInt();
                for (int i = 0; i < count; ++i) {
                    String ext = supportedStructure.readString();
                    if (log.isDebugEnabled()) {
                        this.debug(log, "Server supports '" + ext + "' extension", new Object[0]);
                    }
                    this.supportedExtensions.add(ext);
                }
            }
            if (log.isDebugEnabled()) {
                this.debug(log, "supported-attribute-mask: " + this.supportedAttributeMask.toString(), new Object[0]);
                this.debug(log, "supported-attribute-bits: " + this.supportedAttributeBits.toString(), new Object[0]);
                this.debug(log, "supported-open-flags: " + this.supportedOpenFileFlags.toString(), new Object[0]);
                this.debug(log, "supported-access-mask: " + this.supportedAccessMask.toString(), new Object[0]);
                this.debug(log, "max-read-size: " + this.maxReadSize.toString(), new Object[0]);
            }
        }
    }

    public void setCharsetEncoding(String charset) throws SshException, UnsupportedEncodingException {
        if (this.version == -1) {
            throw new SshException("SFTP Channel must be initialized before setting character set encoding", 4);
        }
        String test = "123456890";
        test.getBytes(charset);
        this.CHARSET_ENCODING = charset;
    }

    public int getServerVersion() {
        return this.serverVersion;
    }

    public String getCharsetEncoding() {
        return this.CHARSET_ENCODING;
    }

    public boolean supportsExtension(String name) {
        return this.extensions.containsKey(name);
    }

    public byte[] getExtension(String name) {
        return this.extensions.get(name);
    }

    public UnsignedInteger32 sendExtensionMessage(String request, byte[] requestData) throws SshException, SftpStatusException {
        try {
            UnsignedInteger32 id = this.nextRequestId();
            Packet packet = this.createPacket();
            packet.write(200);
            packet.writeUINT32(id);
            packet.writeString(request);
            packet.write(requestData);
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_EXTENDED channel=%d requestId=%s request=%s", this.channel.getChannelId(), id.toString(), request), new Object[0]);
            }
            this.sendMessage(packet);
            return id;
        }
        catch (IOException ex) {
            throw new SshException(5, (Throwable)ex);
        }
    }

    public void changePermissions(SftpFile file, int permissions) throws SftpStatusException, SshException {
        SftpFileAttributes attrs = new SftpFileAttributes(this, 5);
        attrs.setPermissions(new UnsignedInteger32(permissions));
        this.setAttributes(file, attrs);
    }

    public void changePermissions(String filename, int permissions) throws SftpStatusException, SshException {
        SftpFileAttributes attrs = new SftpFileAttributes(this, 5);
        attrs.setPermissions(new UnsignedInteger32(permissions));
        this.setAttributes(filename, attrs);
    }

    public void changePermissions(String filename, String permissions) throws SftpStatusException, SshException {
        SftpFileAttributes attrs = new SftpFileAttributes(this, 5);
        attrs.setPermissions(permissions);
        this.setAttributes(filename, attrs);
    }

    public void setAttributes(String path, SftpFileAttributes attrs) throws SftpStatusException, SshException {
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(9);
            msg.writeInt(requestId.longValue());
            msg.writeString(path, this.CHARSET_ENCODING);
            msg.write(attrs.toByteArray());
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_SETSTAT channel=%d requestId=%s", this.channel.getChannelId(), requestId.toString()), new Object[0]);
            }
            this.sendMessage(msg);
            this.getOKRequestStatus(requestId);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
    }

    public void setAttributes(SftpFile file, SftpFileAttributes attrs) throws SftpStatusException, SshException {
        if (!this.isValidHandle(file.getHandle())) {
            throw new SftpStatusException(100, "The handle is not an open file handle!");
        }
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(10);
            msg.writeInt(requestId.longValue());
            msg.writeBinaryString(file.getHandle());
            msg.write(attrs.toByteArray());
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_SETSTAT channel=%d requestId=%s", this.channel.getChannelId(), requestId.toString()), new Object[0]);
            }
            this.sendMessage(msg);
            this.getOKRequestStatus(requestId);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public UnsignedInteger32 postWriteRequest(byte[] handle, long position, byte[] data, int off, int len) throws SftpStatusException, SshException {
        if (data.length - off < len) {
            throw new IndexOutOfBoundsException("Incorrect data array size!");
        }
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(6);
            msg.writeInt(requestId.longValue());
            msg.writeBinaryString(handle);
            msg.writeUINT64(position);
            msg.writeBinaryString(data, off, len);
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_WRITE channel=%d requestId=%s offset=%d blocksize=%d", this.channel.getChannelId(), requestId.toString(), position, len), new Object[0]);
            }
            this.sendMessage(msg);
            return requestId;
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public void writeFile(byte[] handle, UnsignedInteger64 offset, byte[] data, int off, int len) throws SftpStatusException, SshException {
        this.getOKRequestStatus(this.postWriteRequest(handle, offset.longValue(), data, off, len));
    }

    public void performOptimizedWrite(byte[] handle, int blocksize, int outstandingRequests, InputStream in, int buffersize, FileTransferProgress progress) throws SftpStatusException, SshException, TransferCancelledException {
        this.performOptimizedWrite(handle, blocksize, outstandingRequests, in, buffersize, progress, 0L);
    }

    public void performOptimizedWrite(byte[] handle, int blocksize, int outstandingRequests, InputStream in, int buffersize, FileTransferProgress progress, long position) throws SftpStatusException, SshException, TransferCancelledException {
        try {
            int overhead;
            int adaptiveAsyncRequests;
            int adaptiveBlockSize = AdaptiveConfiguration.getInt("sftpBlockSize", -1, this.client.getTransport().getHost(), this.client.getIdent());
            if (adaptiveBlockSize > -1) {
                blocksize = adaptiveBlockSize;
            }
            if (blocksize < 4096) {
                log.warn("Block size {} cannot be less than 4096", (Object)blocksize);
                blocksize = 4096;
            }
            if ((adaptiveAsyncRequests = AdaptiveConfiguration.getInt("maxAsyncRequests", -1, this.client.getTransport().getHost(), this.client.getIdent())) > -1) {
                outstandingRequests = adaptiveAsyncRequests;
            }
            if (!AdaptiveConfiguration.getBoolean("disableBlocksizeOptimization", false, this.client.getTransport().getHost(), this.client.getIdent()) && blocksize + (overhead = 13 + handle.length + 8 + 4) > this.channel.getMaximumRemotePacketLength()) {
                blocksize = this.channel.getMaximumRemotePacketLength() - overhead;
            }
            if (log.isDebugEnabled()) {
                this.debug(log, "Performing optimized write length=" + in.available() + " postion=" + position + " blocksize=" + blocksize + " outstandingRequests=" + outstandingRequests, new Object[0]);
            }
            if (position < 0L) {
                throw new SshException("Position value must be greater than zero!", 4);
            }
            if (buffersize <= 0) {
                buffersize = blocksize;
            }
            byte[] buf = new byte[blocksize];
            long transfered = 0L;
            int buffered = 0;
            Vector<UnsignedInteger32> requests = new Vector<UnsignedInteger32>();
            in = new BufferedInputStream(in, buffersize);
            while ((buffered = IOUtil.readyFully(in, buf)) != -1) {
                requests.addElement(this.postWriteRequest(handle, position, buf, 0, buffered));
                transfered += (long)buffered;
                position += (long)buffered;
                if (progress != null) {
                    if (progress.isCancelled()) {
                        throw new TransferCancelledException();
                    }
                    progress.progressed(transfered);
                }
                if (requests.size() <= outstandingRequests) continue;
                UnsignedInteger32 requestId = (UnsignedInteger32)requests.elementAt(0);
                requests.removeElementAt(0);
                this.getOKRequestStatus(requestId);
            }
            Enumeration e = requests.elements();
            while (e.hasMoreElements()) {
                this.getOKRequestStatus((UnsignedInteger32)e.nextElement());
            }
            requests.removeAllElements();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
        catch (OutOfMemoryError ex) {
            throw new SshException("Resource Shortage: try reducing the local file buffer size", 4);
        }
    }

    public void performOptimizedRead(byte[] handle, long length, int blocksize, OutputStream out, int outstandingRequests, FileTransferProgress progress) throws SftpStatusException, SshException, TransferCancelledException {
        this.performOptimizedRead(handle, length, blocksize, out, outstandingRequests, progress, 0L);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void performOptimizedRead(byte[] handle, long length, int blocksize, OutputStream out, int outstandingRequests, FileTransferProgress progress, long position) throws SftpStatusException, SshException, TransferCancelledException {
        int overhead;
        int adaptiveAsyncRequests;
        int adaptiveBlockSize = AdaptiveConfiguration.getInt("sftpBlockSize", -1, this.client.getTransport().getHost(), this.client.getIdent());
        if (adaptiveBlockSize > -1) {
            blocksize = adaptiveBlockSize;
        }
        if (blocksize < 4096) {
            log.warn("Block size {} cannot be less than 4096", (Object)blocksize);
            blocksize = 4096;
        }
        if ((adaptiveAsyncRequests = AdaptiveConfiguration.getInt("maxAsyncRequests", -1, this.client.getTransport().getHost(), this.client.getIdent())) > -1) {
            outstandingRequests = adaptiveAsyncRequests;
        }
        if (!AdaptiveConfiguration.getBoolean("disableBlocksizeOptimization", false, this.client.getTransport().getHost(), this.client.getIdent()) && blocksize + (overhead = 13) > this.channel.getMaximumRemotePacketLength()) {
            blocksize = this.channel.getMaximumRemotePacketLength() - overhead;
        }
        System.setProperty("maverick.optimizedBlock", String.valueOf(blocksize));
        if (log.isDebugEnabled()) {
            this.debug(log, "Performing optimized read length=" + length + " postion=" + position + " blocksize=" + blocksize + " outstandingRequests=" + outstandingRequests, new Object[0]);
        }
        if (length <= 0L) {
            length = Long.MAX_VALUE;
        }
        if (length / (long)blocksize < (long)outstandingRequests) {
            int tmp = (int)(length / (long)blocksize) + 2;
            if (log.isDebugEnabled()) {
                this.debug(log, "Resetting async requests as it would generate excessive requests for size of file length=" + length + " maxAsync=" + outstandingRequests + " newValue=" + tmp, new Object[0]);
            }
            outstandingRequests = tmp;
        }
        MessageDigest md5 = null;
        OutputStream originalStream = null;
        if (this.performVerification) {
            try {
                md5 = MessageDigest.getInstance("MD5");
            }
            catch (NoSuchAlgorithmException ex) {
                throw new SshException(ex);
            }
            originalStream = out;
            out = new DigestOutputStream(out, md5);
        }
        if (position < 0L) {
            throw new SshException("Position value must be greater than zero!", 4);
        }
        long transfered = 0L;
        boolean reachedEOF = false;
        try {
            byte[] tmp = new byte[blocksize];
            long time = System.currentTimeMillis();
            int i = this.readFile(handle, new UnsignedInteger64(0L), tmp, 0, tmp.length);
            time = System.currentTimeMillis() - time;
            System.setProperty("maverick.blockRoundtrip", String.valueOf(time));
            if (log.isDebugEnabled()) {
                this.debug(log, "Block roundtrip time was " + time + "ms channel=" + this.channel.getChannelId(), new Object[0]);
            }
            if (i == -1) {
                return;
            }
            if (i < blocksize && length > (long)i) {
                blocksize = i;
            }
            if ((long)i > position) {
                try {
                    out.write(tmp, (int)position, (int)((long)i - position));
                }
                catch (IOException e) {
                    throw new TransferCancelledException();
                }
                length -= (long)i - position;
                transfered += (long)i - position;
                if (progress != null) {
                    progress.progressed(transfered);
                }
                position = i;
            }
            if (length <= 0L) {
                length = Long.MAX_VALUE;
            }
            System.setProperty("maverick.finalBlock", String.valueOf(blocksize));
            tmp = null;
            long osr = 1L;
            Vector<UnsignedInteger32> requests = new Vector<UnsignedInteger32>(outstandingRequests);
            long offset = position;
            boolean enableSFTPReadWindowSpaceFix = AdaptiveConfiguration.getBoolean("enableSFTPReadWindowSpaceFix", false, this.client.getTransport().getHost(), this.client.getIdent());
            while (true) {
                block70: {
                    int status;
                    SftpMessage bar;
                    block71: {
                        if ((long)requests.size() < osr) {
                            if (enableSFTPReadWindowSpaceFix && i > 0 && this.channel.getRemoteWindow() < 29L) {
                                if (log.isDebugEnabled()) {
                                    this.debug(log, "Deferring post requests due to lack of remote window", new Object[0]);
                                }
                            } else {
                                requests.addElement(this.postReadRequest(handle, offset, blocksize));
                                offset += (long)blocksize;
                                if (progress == null || !progress.isCancelled()) continue;
                                throw new TransferCancelledException();
                            }
                        }
                        UnsignedInteger32 requestId = (UnsignedInteger32)requests.elementAt(0);
                        requests.removeElementAt(0);
                        bar = this.getResponse(requestId);
                        try {
                            if (bar.getType() == 103) {
                                int dataLen = (int)bar.readInt();
                                if (log.isDebugEnabled()) {
                                    this.debug(log, "Received SSH_FXP_DATA channel=" + this.channel.getChannelId() + " len=" + dataLen + " request=" + requestId.toString(), new Object[0]);
                                }
                                try {
                                    out.write(bar.array(), bar.getPosition(), dataLen);
                                }
                                catch (IOException e) {
                                    throw new TransferCancelledException();
                                }
                                transfered += (long)dataLen;
                                if (progress != null) {
                                    progress.progressed(transfered);
                                }
                                break block70;
                            }
                            if (bar.getType() != 101) throw new SshException("The server responded with an unexpected message", 6);
                            status = (int)bar.readInt();
                            if (status != 1) break block71;
                            if (log.isDebugEnabled()) {
                                this.debug(log, "Received SSH_FXP_STATUS with SSH_FX_EOF channel=" + this.channel.getChannelId(), new Object[0]);
                            }
                            reachedEOF = true;
                            return;
                        }
                        catch (IOException ex) {
                            throw new SshException("Failed to read expected data from server response", 6);
                        }
                    }
                    if (log.isDebugEnabled()) {
                        this.debug(log, "Received SSH_FXP_STATUS channel=" + this.channel.getChannelId() + " status=" + status, new Object[0]);
                    }
                    if (this.version < 3) throw new SftpStatusException(status);
                    throw new SftpStatusException(status, SftpSubsystemChannel.getStatusDescription(bar));
                }
                if (osr >= (long)outstandingRequests) continue;
                ++osr;
            }
        }
        finally {
            block72: {
                if (reachedEOF && this.performVerification && transfered > 0L) {
                    try {
                        out.flush();
                        out.close();
                        try {
                            originalStream.close();
                        }
                        catch (IOException iOException) {}
                        byte[] digest = md5.digest();
                        try (ByteArrayWriter baw = new ByteArrayWriter();){
                            baw.writeBinaryString(handle);
                            baw.writeUINT64(0L);
                            baw.writeUINT64(transfered);
                            baw.writeBinaryString(new byte[0]);
                            SftpMessage reply = this.getExtensionResponse(this.sendExtensionMessage("md5-hash-handle", baw.toByteArray()));
                            reply.readString();
                            byte[] remoteDigest = reply.readBinaryString();
                            if (!Arrays.equals(digest, remoteDigest)) {
                                throw new SshException("Remote file digest does not match local digest", 17);
                            }
                        }
                    }
                    catch (IOException e) {
                        log.error("Error processing remote digest", (Throwable)e);
                    }
                    catch (SftpStatusException e) {
                        if (e.getStatus() == 8) {
                            this.performVerification = false;
                        } else {
                            log.error("Could not verify file", (Throwable)e);
                        }
                    }
                    catch (SshException e) {
                        if (reachedEOF && e.getReason() == 17) {
                            throw e;
                        }
                        if (reachedEOF) break block72;
                        throw e;
                    }
                }
            }
        }
    }

    public void performSynchronousRead(byte[] handle, int blocksize, OutputStream out, FileTransferProgress progress, long position) throws SftpStatusException, SshException, TransferCancelledException {
        if (log.isDebugEnabled()) {
            this.debug(log, "Performing synchronous read postion=" + position + " blocksize=" + blocksize, new Object[0]);
        }
        if (blocksize < 1 || blocksize > 32768) {
            if (log.isDebugEnabled()) {
                this.debug(log, "Blocksize to large for some SFTP servers, reseting to 32K", new Object[0]);
            }
            blocksize = 32768;
        }
        if (position < 0L) {
            throw new SshException("Position value must be greater than zero!", 4);
        }
        byte[] tmp = new byte[blocksize];
        UnsignedInteger64 offset = new UnsignedInteger64(position);
        if (position > 0L && progress != null) {
            progress.progressed(position);
        }
        try {
            int read;
            while ((read = this.readFile(handle, offset, tmp, 0, tmp.length)) > -1) {
                if (progress != null && progress.isCancelled()) {
                    throw new TransferCancelledException();
                }
                out.write(tmp, 0, read);
                offset = UnsignedInteger64.add(offset, read);
                if (progress == null) continue;
                progress.progressed(offset.longValue());
            }
        }
        catch (IOException e) {
            throw new SshException(e);
        }
    }

    public UnsignedInteger32 postReadRequest(byte[] handle, long offset, int len) throws SftpStatusException, SshException {
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            if (log.isDebugEnabled()) {
                this.debug(log, "Sending SSH_FXP_READ channel=" + this.channel.getChannelId() + " offset=" + offset + " blocksize=" + len + " request=" + requestId.toString(), new Object[0]);
            }
            Packet msg = this.createPacket();
            msg.write(5);
            msg.writeInt(requestId.longValue());
            msg.writeBinaryString(handle);
            msg.writeUINT64(offset);
            msg.writeInt(len);
            this.sendMessage(msg);
            return requestId;
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public int readFile(byte[] handle, UnsignedInteger64 offset, byte[] output, int off, int len) throws SftpStatusException, SshException {
        try {
            if (output.length - off < len) {
                throw new IndexOutOfBoundsException("Output array size is smaller than read length!");
            }
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(5);
            msg.writeInt(requestId.longValue());
            msg.writeBinaryString(handle);
            msg.write(offset.toByteArray());
            msg.writeInt(len);
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_READ channel=%d requestId=%s offset=%s blocksize=%d", this.channel.getChannelId(), requestId.toString(), offset.toString(), len), new Object[0]);
            }
            this.sendMessage(msg);
            SftpMessage bar = this.getResponse(requestId);
            if (bar.getType() == 103) {
                byte[] msgdata = bar.readBinaryString();
                System.arraycopy(msgdata, 0, output, off, msgdata.length);
                if (log.isDebugEnabled()) {
                    this.debug(log, String.format("Received SSH_FXP_DATA channel=%d requestId=%s offset=%s blocksize=%d", this.channel.getChannelId(), requestId.toString(), offset.toString(), msgdata.length), new Object[0]);
                }
                return msgdata.length;
            }
            if (bar.getType() == 101) {
                int status = (int)bar.readInt();
                if (log.isDebugEnabled()) {
                    this.debug(log, String.format("Received SSH_FXP_STATUS channel=%d requestId=%s", this.channel.getChannelId(), requestId.toString()), new Object[0]);
                }
                if (status == 1) {
                    return -1;
                }
                if (this.version >= 3) {
                    throw new SftpStatusException(status, SftpSubsystemChannel.getStatusDescription(bar));
                }
                throw new SftpStatusException(status);
            }
            this.close();
            throw new SshException("The server responded with an unexpected message", 6);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public SftpFile getFile(String path) throws SftpStatusException, SshException {
        String absolute = this.getAbsolutePath(path);
        SftpFile file = new SftpFile(absolute, this.getAttributes(absolute));
        file.sftp = this;
        return file;
    }

    public String getAbsolutePath(SftpFile file) throws SftpStatusException, SshException {
        return this.getAbsolutePath(file.getFilename());
    }

    public void lockFile(byte[] handle, long offset, long length, int lockFlags) throws SftpStatusException, SshException {
        if (this.version < 6) {
            throw new SftpStatusException(8, "Locks are not supported by the server SFTP version " + String.valueOf(this.version));
        }
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(22);
            msg.writeInt(requestId.longValue());
            msg.writeBinaryString(handle);
            msg.writeUINT64(offset);
            msg.writeUINT64(length);
            msg.writeInt(lockFlags);
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_BLOCK channel=%d requestId=%s offset=%d length=%d", this.channel.getChannelId(), requestId.toString(), offset, length), new Object[0]);
            }
            this.sendMessage(msg);
            this.getOKRequestStatus(requestId);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public void unlockFile(byte[] handle, long offset, long length) throws SftpStatusException, SshException {
        if (this.version < 6) {
            throw new SftpStatusException(8, "Locks are not supported by the server SFTP version " + String.valueOf(this.version));
        }
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(23);
            msg.writeInt(requestId.longValue());
            msg.writeBinaryString(handle);
            msg.writeUINT64(offset);
            msg.writeUINT64(length);
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_UNBLOCK channel=%d requestId=%s offset=%d length=%d", this.channel.getChannelId(), requestId.toString(), offset, length), new Object[0]);
            }
            this.sendMessage(msg);
            this.getOKRequestStatus(requestId);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public void createSymbolicLink(String targetpath, String linkpath) throws SftpStatusException, SshException {
        if (this.version < 3) {
            throw new SftpStatusException(8, "Symbolic links are not supported by the server SFTP version " + String.valueOf(this.version));
        }
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(this.version >= 6 ? 21 : 20);
            msg.writeInt(requestId.longValue());
            msg.writeString(linkpath, this.CHARSET_ENCODING);
            msg.writeString(targetpath, this.CHARSET_ENCODING);
            if (this.version >= 6) {
                msg.writeBoolean(true);
            }
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_SYMLINK channel=%d requestId=%s linkpath=%s target=%s symbolic=%s", this.channel.getChannelId(), requestId.toString(), linkpath, targetpath, "true"), new Object[0]);
            }
            this.sendMessage(msg);
            this.getOKRequestStatus(requestId);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public void createLink(String targetpath, String linkpath, boolean symbolic) throws SftpStatusException, SshException {
        if (this.version < 6 && !symbolic) {
            throw new SftpStatusException(8, "Hard links are not supported by the server SFTP version " + String.valueOf(this.version));
        }
        if (this.version < 6 && symbolic) {
            this.createSymbolicLink(targetpath, linkpath);
            return;
        }
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(21);
            msg.writeInt(requestId.longValue());
            msg.writeString(linkpath, this.CHARSET_ENCODING);
            msg.writeString(targetpath, this.CHARSET_ENCODING);
            msg.writeBoolean(symbolic);
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_LINK channel=%d requestId=%s linkpath=%s target=%s symbolic=%s", this.channel.getChannelId(), requestId.toString(), linkpath, targetpath, String.valueOf(symbolic)), new Object[0]);
            }
            this.sendMessage(msg);
            this.getOKRequestStatus(requestId);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public String getSymbolicLinkTarget(String linkpath) throws SftpStatusException, SshException {
        if (this.version < 3) {
            throw new SftpStatusException(8, "Symbolic links are not supported by the server SFTP version " + String.valueOf(this.version));
        }
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(19);
            msg.writeInt(requestId.longValue());
            msg.writeString(linkpath, this.CHARSET_ENCODING);
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_READLINK channel=%d requestId=%s linkpath=%s", this.channel.getChannelId(), requestId.toString(), linkpath), new Object[0]);
            }
            this.sendMessage(msg);
            SftpFile[] files = this.extractFiles(this.getResponse(requestId), null);
            return files[0].getAbsolutePath();
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public String getDefaultDirectory() throws SftpStatusException, SshException {
        return this.getAbsolutePath("");
    }

    protected static String getStatusDescription(ByteArrayReader msg) throws IOException {
        String reason = "The server did not provide a descritive reason.";
        if (msg.available() >= 4) {
            reason = msg.readString();
        }
        return reason;
    }

    public String getAbsolutePath(String path) throws SftpStatusException, SshException {
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(16);
            msg.writeInt(requestId.longValue());
            msg.writeString(path, this.CHARSET_ENCODING);
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_REALPATH channel=%d requestId=%s path=%s", this.channel.getChannelId(), requestId.toString(), path), new Object[0]);
            }
            this.sendMessage(msg);
            return this.getSingleFileResponse(this.getResponse(requestId), "SSH_FXP_REALPATH").getAbsolutePath();
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public int listChildren(SftpFile file, Vector<SftpFile> children) throws SftpStatusException, SshException {
        return this.listChildren(file, children, AdaptiveConfiguration.getBoolean("disableDirectoryCheck", false, this.client.getTransport().getHost(), this.client.getIdent()));
    }

    public int listChildren(SftpFile file, Vector<SftpFile> children, boolean disableAttributeCheck) throws SftpStatusException, SshException {
        if (!disableAttributeCheck) {
            if (file.isDirectory()) {
                if (!this.isValidHandle(file.getHandle()) && !this.isValidHandle((file = this.openDirectory(file.getAbsolutePath(), disableAttributeCheck)).getHandle())) {
                    throw new SftpStatusException(4, "Failed to open directory");
                }
            } else {
                throw new SshException("Cannot list children for this file object", 4);
            }
        }
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(12);
            msg.writeInt(requestId.longValue());
            msg.writeBinaryString(file.getHandle());
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_READDIR channel=%d requestId=%s path=%s", this.channel.getChannelId(), requestId.toString(), file.getAbsolutePath()), new Object[0]);
            }
            this.sendMessage(msg);
            SftpMessage bar = this.getResponse(requestId);
            if (bar.getType() == 104) {
                if (log.isDebugEnabled()) {
                    this.debug(log, String.format("Received SSH_FXP_NAME channel=%d requestId=%s path=%s", this.channel.getChannelId(), requestId.toString(), file.getAbsolutePath()), new Object[0]);
                }
                SftpFile[] files = this.extractFiles(bar, file.getAbsolutePath());
                for (int i = 0; i < files.length; ++i) {
                    children.addElement(files[i]);
                }
                return files.length;
            }
            if (bar.getType() == 101) {
                int status = (int)bar.readInt();
                if (status == 1) {
                    if (log.isDebugEnabled()) {
                        this.debug(log, String.format("Received SSH_FX_EOF channel=%d requestId=%s", this.channel.getChannelId(), requestId.toString()), new Object[0]);
                    }
                    return -1;
                }
                if (log.isDebugEnabled()) {
                    this.debug(log, String.format("Received SSH_FXP_STATUS channel=%d requestId=%s status=%d", this.channel.getChannelId(), requestId.toString(), status), new Object[0]);
                }
                if (this.version >= 3) {
                    throw new SftpStatusException(status, SftpSubsystemChannel.getStatusDescription(bar));
                }
                throw new SftpStatusException(status);
            }
            this.close();
            throw new SshException("The server responded with an unexpected message", 6);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    SftpFile[] extractFiles(SftpMessage bar, String parent) throws SshException {
        try {
            if (parent != null && !parent.endsWith("/")) {
                parent = parent + "/";
            }
            int count = (int)bar.readInt();
            SftpFile[] files = new SftpFile[count];
            String longname = null;
            for (int i = 0; i < files.length; ++i) {
                String shortname = bar.readString(this.CHARSET_ENCODING);
                if (this.version <= 3) {
                    longname = bar.readString(this.CHARSET_ENCODING);
                }
                files[i] = new SftpFile(parent != null ? parent + shortname : shortname, new SftpFileAttributes(this, bar));
                files[i].longname = longname;
                if (longname != null && this.version <= 3) {
                    try {
                        StringTokenizer t = new StringTokenizer(longname);
                        t.nextToken();
                        t.nextToken();
                        String username = t.nextToken();
                        String group = t.nextToken();
                        files[i].getAttributes().setUsername(username);
                        files[i].getAttributes().setGroup(group);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                files[i].setSFTPSubsystem(this);
            }
            return files;
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public void recurseMakeDirectory(String path) throws SftpStatusException, SshException {
        if (path.trim().length() > 0) {
            try {
                SftpFile file = this.openDirectory(path);
                file.close();
            }
            catch (SshException ioe) {
                int idx = 0;
                do {
                    String tmp = (idx = path.indexOf(47, idx)) > -1 ? path.substring(0, idx + 1) : path;
                    try {
                        SftpFile file = this.openDirectory(tmp);
                        file.close();
                    }
                    catch (SshException ioe7) {
                        this.makeDirectory(tmp);
                    }
                } while (idx > -1);
            }
        }
    }

    public SftpFile openFile(String absolutePath, int flags) throws SftpStatusException, SshException {
        return this.openFile(absolutePath, flags, new SftpFileAttributes(this, 5));
    }

    public SftpFile openFile(String absolutePath, int flags, SftpFileAttributes attrs) throws SftpStatusException, SshException {
        if (this.version >= 5) {
            if (log.isDebugEnabled()) {
                this.debug(log, "Converting openFile request to version 5+ format", new Object[0]);
            }
            int accessFlags = 0;
            int newFlags = 0;
            if ((flags & 1) == 1) {
                accessFlags |= 0x81;
                if (log.isDebugEnabled()) {
                    this.debug(log, "OPEN_READ present, adding ACE4_READ_DATA, ACE4_READ_ATTRIBUTES", new Object[0]);
                }
            }
            if ((flags & 2) == 2) {
                accessFlags |= 2;
                accessFlags |= 0x100;
                if (log.isDebugEnabled()) {
                    this.debug(log, "OPEN_WRITE present, adding ACE4_WRITE_DATA, ACE4_WRITE_ATTRIBUTES ", new Object[0]);
                }
            }
            if ((flags & 4) == 4) {
                accessFlags |= 4;
                accessFlags |= 2;
                accessFlags |= 0x100;
                newFlags |= 8;
                if (log.isDebugEnabled()) {
                    this.debug(log, "OPEN_APPEND present, adding ACE4_APPEND_DATA,ACE4_WRITE_DATA, ACE4_WRITE_ATTRIBUTES", new Object[0]);
                }
            }
            if ((flags & 8) == 8) {
                if ((flags & 0x10) == 16) {
                    newFlags |= 1;
                    if (log.isDebugEnabled()) {
                        this.debug(log, "OPEN_CREATE and OPEN_TRUNCATE present, adding SSH_FXF_CREATE_TRUNCATE", new Object[0]);
                    }
                }
            } else {
                newFlags |= 2;
                if (log.isDebugEnabled()) {
                    this.debug(log, "OPEN_CREATE not present, adding SSH_FXF_OPEN_EXISTING", new Object[0]);
                }
            }
            if ((flags & 0x40) == 64) {
                newFlags |= 0x20;
                if (log.isDebugEnabled()) {
                    this.debug(log, "", new Object[0]);
                }
            }
            return this.openFileVersion5(absolutePath, newFlags, accessFlags, attrs);
        }
        if (attrs == null) {
            attrs = new SftpFileAttributes(this, 5);
        }
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(3);
            msg.writeInt(requestId.longValue());
            msg.writeString(absolutePath, this.CHARSET_ENCODING);
            msg.writeInt(flags);
            msg.write(attrs.toByteArray());
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_OPEN channel=%d requestId=%s path=%s", this.channel.getChannelId(), requestId.toString(), absolutePath), new Object[0]);
            }
            this.sendMessage(msg);
            byte[] handle = this.getHandleResponse(requestId);
            SftpFile file = new SftpFile(absolutePath, null);
            file.setHandle(handle);
            file.setSFTPSubsystem(this);
            EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 26, true, this.client.getUuid()).addAttribute("CLIENT", this.channel.getClient()).addAttribute("FILE_NAME", file.getAbsolutePath()));
            return file;
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public SftpFile openFileVersion5(String absolutePath, int flags, int accessFlags, SftpFileAttributes attrs) throws SftpStatusException, SshException {
        if (attrs == null) {
            attrs = new SftpFileAttributes(this, 5);
        }
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(3);
            msg.writeInt(requestId.longValue());
            msg.writeString(absolutePath, this.CHARSET_ENCODING);
            msg.writeInt(accessFlags);
            msg.writeInt(flags);
            msg.write(attrs.toByteArray());
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_OPEN channel=%d requestId=%s path=%s", this.channel.getChannelId(), requestId.toString(), absolutePath), new Object[0]);
            }
            this.sendMessage(msg);
            byte[] handle = this.getHandleResponse(requestId);
            SftpFile file = new SftpFile(absolutePath, null);
            file.setHandle(handle);
            file.setSFTPSubsystem(this);
            EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 26, true, this.client.getUuid()).addAttribute("CLIENT", this.channel.getClient()).addAttribute("FILE_NAME", file.getAbsolutePath()));
            return file;
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public SftpFile openDirectory(String path) throws SftpStatusException, SshException {
        return this.openDirectory(path, AdaptiveConfiguration.getBoolean("disableDirectoryCheck", false, this.client.getTransport().getHost(), this.client.getIdent()));
    }

    public SftpFile openDirectory(String path, boolean disableAttributeCheck) throws SftpStatusException, SshException {
        String absolutePath = this.getAbsolutePath(path);
        SftpFileAttributes attrs = null;
        if (!disableAttributeCheck && !(attrs = this.getAttributes(absolutePath)).isDirectory()) {
            throw new SftpStatusException(4, path + " is not a directory");
        }
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(11);
            msg.writeInt(requestId.longValue());
            msg.writeString(path, this.CHARSET_ENCODING);
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_OPENDIR channel=%d requestId=%s path=%s", this.channel.getChannelId(), requestId.toString(), absolutePath), new Object[0]);
            }
            this.sendMessage(msg);
            byte[] handle = this.getHandleResponse(requestId);
            SftpFile file = new SftpFile(absolutePath, attrs);
            file.setHandle(handle);
            file.setSFTPSubsystem(this);
            return file;
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    void closeHandle(byte[] handle) throws SftpStatusException, SshException {
        if (!this.isValidHandle(handle)) {
            throw new SftpStatusException(100, "The handle is invalid!");
        }
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(4);
            msg.writeInt(requestId.longValue());
            msg.writeBinaryString(handle);
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_CLOSE channel=%d requestId=%s handle=%s", this.channel.getChannelId(), requestId.toString(), Utils.bytesToHex(handle)), new Object[0]);
            }
            this.sendMessage(msg);
            if (!AdaptiveConfiguration.getBoolean("disableSftpCloseStatus", false, this.client.getTransport().getHost(), this.client.getIdent())) {
                this.getOKRequestStatus(requestId);
            }
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public void closeFile(SftpFile file) throws SftpStatusException, SshException {
        if (file.getHandle() != null) {
            this.closeHandle(file.getHandle());
            EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 25, true, this.client.getUuid()).addAttribute("FILE_NAME", file.getAbsolutePath()).addAttribute("CLIENT", this.channel.getClient()));
            file.setHandle(null);
        }
    }

    boolean isValidHandle(byte[] handle) {
        return handle != null;
    }

    public void removeDirectory(String path) throws SftpStatusException, SshException {
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(15);
            msg.writeInt(requestId.longValue());
            msg.writeString(path, this.CHARSET_ENCODING);
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_RMDIR channel=%d requestId=%s path=%s", this.channel.getChannelId(), requestId.toString(), path), new Object[0]);
            }
            this.sendMessage(msg);
            this.getOKRequestStatus(requestId);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
        EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 29, true, this.client.getUuid()).addAttribute("CLIENT", this.channel.getClient()).addAttribute("DIRECTORY_PATH", path));
    }

    public void removeFile(String filename) throws SftpStatusException, SshException {
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(13);
            msg.writeInt(requestId.longValue());
            msg.writeString(filename, this.CHARSET_ENCODING);
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_REMOVE channel=%d requestId=%s path=%s", this.channel.getChannelId(), requestId.toString(), filename), new Object[0]);
            }
            this.sendMessage(msg);
            this.getOKRequestStatus(requestId);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
        EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 28, true, this.client.getUuid()).addAttribute("CLIENT", this.channel.getClient()).addAttribute("FILE_NAME", filename));
    }

    public void renameFile(String oldpath, String newpath) throws SftpStatusException, SshException {
        this.renameFile(oldpath, newpath, 0);
    }

    public void renameFile(String oldpath, String newpath, int flags) throws SftpStatusException, SshException {
        if (this.version < 2) {
            throw new SftpStatusException(8, "Renaming files is not supported by the server SFTP version " + String.valueOf(this.version));
        }
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(18);
            msg.writeInt(requestId.longValue());
            msg.writeString(oldpath, this.CHARSET_ENCODING);
            msg.writeString(newpath, this.CHARSET_ENCODING);
            if (this.version >= 5) {
                msg.writeInt(flags);
            }
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_RENAME channel=%d requestId=%s path=%s to=%s", this.channel.getChannelId(), requestId.toString(), oldpath, newpath), new Object[0]);
            }
            this.sendMessage(msg);
            this.getOKRequestStatus(requestId);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
        EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.client, 27, true, this.client.getUuid()).addAttribute("CLIENT", this.channel.getClient()).addAttribute("FILE_NAME", oldpath).addAttribute("FILE_NEW_NAME", newpath));
    }

    public SftpFileAttributes getAttributes(String path) throws SftpStatusException, SshException {
        return this.getAttributes(path, 17, "SSH_FXP_STAT");
    }

    public SftpFileAttributes getLinkAttributes(String path) throws SftpStatusException, SshException {
        return this.getAttributes(path, 7, "SSH_FXP_LSTAT");
    }

    protected SftpFileAttributes getAttributes(String path, int messageId, String messageName) throws SftpStatusException, SshException {
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(messageId);
            msg.writeInt(requestId.longValue());
            msg.writeString(path, this.CHARSET_ENCODING);
            if (this.version > 3) {
                long flags = 509L;
                if (this.version > 4) {
                    flags |= 0x200L;
                }
                msg.writeInt(flags);
            }
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending %s channel=%d requestId=%s path=%s", messageName, this.channel.getChannelId(), requestId.toString(), path), new Object[0]);
            }
            this.sendMessage(msg);
            SftpMessage bar = this.getResponse(requestId);
            return this.extractAttributes(requestId, bar);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    SftpFileAttributes extractAttributes(UnsignedInteger32 requestId, SftpMessage bar) throws SftpStatusException, SshException {
        try {
            if (bar.getType() == 105) {
                if (log.isDebugEnabled()) {
                    this.debug(log, String.format("Received SSH_FXP_ATTRS channel=%d requestId=%s", this.channel.getChannelId(), requestId.toString()), new Object[0]);
                }
                return new SftpFileAttributes(this, bar);
            }
            if (bar.getType() == 101) {
                int status = (int)bar.readInt();
                if (log.isDebugEnabled()) {
                    this.debug(log, String.format("Received SSH_FXP_STATUS channel=%d requestId=%s status=%d", this.channel.getChannelId(), requestId.toString(), status), new Object[0]);
                }
                if (this.version >= 3) {
                    throw new SftpStatusException(status, SftpSubsystemChannel.getStatusDescription(bar));
                }
                throw new SftpStatusException(status);
            }
            this.close();
            throw new SshException("The server responded with an unexpected message.", 6);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public SftpFileAttributes getAttributes(SftpFile file) throws SftpStatusException, SshException {
        try {
            if (!this.isValidHandle(file.getHandle())) {
                return this.getAttributes(file.getAbsolutePath());
            }
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(8);
            msg.writeInt(requestId.longValue());
            msg.writeBinaryString(file.getHandle());
            if (this.version > 3) {
                msg.writeInt(-2147483139L);
            }
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_FSTAT channel=%d requestId=%s handle=%s", this.channel.getChannelId(), requestId.toString(), Utils.bytesToHex(file.getHandle())), new Object[0]);
            }
            this.sendMessage(msg);
            return this.extractAttributes(requestId, this.getResponse(requestId));
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public void makeDirectory(String path) throws SftpStatusException, SshException {
        this.makeDirectory(path, new SftpFileAttributes(this, 2));
    }

    public void makeDirectory(String path, SftpFileAttributes attrs) throws SftpStatusException, SshException {
        try {
            UnsignedInteger32 requestId = this.nextRequestId();
            Packet msg = this.createPacket();
            msg.write(14);
            msg.writeInt(requestId.longValue());
            msg.writeString(path, this.CHARSET_ENCODING);
            msg.write(attrs.toByteArray());
            if (log.isDebugEnabled()) {
                this.debug(log, String.format("Sending SSH_FXP_MKDIR channel=%d requestId=%s path=%s", this.channel.getChannelId(), requestId.toString(), path), new Object[0]);
            }
            this.sendMessage(msg);
            this.getOKRequestStatus(requestId);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public byte[] getHandleResponse(UnsignedInteger32 requestId) throws SftpStatusException, SshException {
        return this.getHandleResponse(this.getResponse(requestId), requestId);
    }

    public byte[] getHandleResponse(SftpMessage bar, UnsignedInteger32 requestId) throws SftpStatusException, SshException {
        try {
            if (bar.getType() == 102) {
                byte[] handle = bar.readBinaryString();
                if (log.isDebugEnabled()) {
                    this.debug(log, String.format("Received SSH_FXP_HANDLE channel=%d requestId=%s handle=%s", this.channel.getChannelId(), requestId.toString(), Utils.bytesToHex(handle)), new Object[0]);
                }
                return handle;
            }
            if (bar.getType() == 101) {
                int status = (int)bar.readInt();
                if (log.isDebugEnabled()) {
                    this.debug(log, String.format("Received SSH_FXP_STATUS channel=%d requestId=%s status=%d", this.channel.getChannelId(), requestId.toString(), status), new Object[0]);
                }
                if (this.version >= 3) {
                    throw new SftpStatusException(status, SftpSubsystemChannel.getStatusDescription(bar));
                }
                throw new SftpStatusException(status);
            }
            this.close();
            throw new SshException("The server responded with an unexpected message!", 6);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public SftpMessage getExtensionResponse(UnsignedInteger32 requestId) throws SftpStatusException, SshException {
        try {
            SftpMessage bar = this.getResponse(requestId);
            if (bar.getType() == 201) {
                return bar;
            }
            if (bar.getType() == 101) {
                int status = (int)bar.readInt();
                if (this.version >= 3) {
                    throw new SftpStatusException(status, SftpSubsystemChannel.getStatusDescription(bar));
                }
                throw new SftpStatusException(status);
            }
            this.close();
            throw new SshException("The server responded with an unexpected message!", 6);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    public void getOKRequestStatus(UnsignedInteger32 requestId) throws SftpStatusException, SshException {
        try {
            SftpMessage bar = this.getResponse(requestId);
            if (bar.getType() == 101) {
                int status = (int)bar.readInt();
                if (status == 0) {
                    if (log.isDebugEnabled()) {
                        this.debug(log, String.format("Received SSH_FX_OK channel=%d requestId=%s", this.channel.getChannelId(), requestId.toString()), new Object[0]);
                    }
                    return;
                }
                if (log.isDebugEnabled()) {
                    this.debug(log, String.format("Received SSH_FXP_STATUS channel=%d requestId=%s status=%d", this.channel.getChannelId(), requestId.toString(), status), new Object[0]);
                }
                if (this.version >= 3) {
                    throw new SftpStatusException(status, SftpSubsystemChannel.getStatusDescription(bar));
                }
                throw new SftpStatusException(status);
            }
            this.close();
            throw new SshException("The server responded with an unexpected message!", 6);
        }
        catch (SshIOException ex) {
            throw ex.getRealException();
        }
        catch (IOException ex) {
            throw new SshException(ex);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SftpMessage getResponse(UnsignedInteger32 requestId) throws SshException {
        MessageHolder holder = new MessageHolder();
        while (holder.msg == null) {
            try {
                if (!this.sync.requestBlock(requestId, holder)) continue;
                try {
                    SftpMessage msg = new SftpMessage(this.nextMessage());
                    this.responses.put(new UnsignedInteger32(msg.getMessageId()), msg);
                }
                finally {
                    this.sync.releaseBlock();
                }
            }
            catch (InterruptedException e) {
                try {
                    this.close();
                }
                catch (SshIOException ex) {
                    throw ex.getRealException();
                }
                catch (IOException ex1) {
                    throw new SshException(ex1.getMessage(), 6);
                }
                throw new SshException("The thread was interrupted", 6);
            }
            catch (IOException ex) {
                throw new SshException(5, (Throwable)ex);
            }
        }
        return this.responses.remove(requestId);
    }

    public SftpFile getSingleFileResponse(SftpMessage bar, String messageName) throws SshException, SftpStatusException {
        try {
            if (bar.getType() == 104) {
                SftpFile[] files = this.extractFiles(bar, null);
                if (files.length != 1) {
                    this.close();
                    throw new SshException("Server responded to " + messageName + " with too many files!", 6);
                }
                return files[0];
            }
            if (bar.getType() == 101) {
                int status = (int)bar.readInt();
                if (this.version >= 3) {
                    String desc = bar.readString();
                    throw new SftpStatusException(status, desc);
                }
                throw new SftpStatusException(status);
            }
            this.close();
            throw new SshException("The server responded with an unexpected message", 6);
        }
        catch (IOException e) {
            throw new SshException(e);
        }
    }

    public int getMaximumRemotePacketLength() {
        return this.channel.getMaximumRemotePacketLength();
    }

    public int getMaximumLocalPacketLength() {
        return this.channel.getMaximumLocalPacketLength();
    }

    public long getMaximumLocalWindowSize() {
        return this.channel.getMaximumLocalWindowSize();
    }

    public long getMaximumRemoteWindowSize() {
        return this.channel.getMaximumRemoteWindowSize();
    }

    private UnsignedInteger32 nextRequestId() {
        this.requestId = UnsignedInteger32.add(this.requestId, 1);
        return this.requestId;
    }

    class SftpThreadSynchronizer {
        boolean isBlocking = false;

        SftpThreadSynchronizer() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public boolean requestBlock(UnsignedInteger32 requestId, MessageHolder holder) throws InterruptedException {
            if (SftpSubsystemChannel.this.responses.containsKey(requestId)) {
                holder.msg = SftpSubsystemChannel.this.responses.get(requestId);
                return false;
            }
            SftpThreadSynchronizer sftpThreadSynchronizer = this;
            synchronized (sftpThreadSynchronizer) {
                boolean canBlock;
                boolean bl = canBlock = !this.isBlocking;
                if (canBlock) {
                    this.isBlocking = true;
                } else {
                    this.wait();
                }
                return canBlock;
            }
        }

        public synchronized void releaseBlock() {
            this.isBlocking = false;
            this.notifyAll();
        }
    }
}

