/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.scp;

import com.maverick.sftp.FileTransferProgress;
import com.maverick.ssh.ChannelOpenException;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import com.maverick.ssh.SshSession;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScpClientIO {
    static Logger log = LoggerFactory.getLogger(ScpClientIO.class);
    protected SshClient ssh;
    boolean first = true;
    protected int windowSpace = Integer.MAX_VALUE;
    protected int packetSize = 32768;

    public ScpClientIO(SshClient ssh) {
        this.ssh = ssh;
    }

    public void setWindowSpace(int windowSpace) {
        this.windowSpace = windowSpace;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public void put(InputStream in, long length, String localFile, String remoteFile) throws SshException, ChannelOpenException {
        this.put(in, length, localFile, remoteFile, false, null);
    }

    public void put(InputStream in, long length, String localFile, String remoteFile, FileTransferProgress progress) throws SshException, ChannelOpenException {
        this.put(in, length, localFile, remoteFile, false, progress);
    }

    public void put(InputStream in, long length, String localFile, String remoteFile, boolean remoteIsDir, FileTransferProgress progress) throws SshException, ChannelOpenException {
        ScpEngineIO scp = new ScpEngineIO("scp " + (remoteIsDir ? "-d " : "") + "-t " + remoteFile, this.ssh.openSessionChannel(this.windowSpace, this.packetSize, null));
        try {
            scp.waitForResponse();
            if (progress != null) {
                progress.started(length, remoteFile);
            }
            scp.writeStreamToRemote(in, length, localFile, progress);
            if (progress != null) {
                progress.completed();
            }
            scp.close();
        }
        catch (IOException ex) {
            log.error("SCP put generated I/O error", (Throwable)ex);
            scp.close();
            throw new SshException(ex, 6);
        }
    }

    public InputStream get(String remoteFile) throws SshException, ChannelOpenException {
        return this.get(remoteFile, null);
    }

    public InputStream get(String remoteFile, FileTransferProgress progress) throws SshException, ChannelOpenException {
        ScpEngineIO scp = new ScpEngineIO("scp -f " + remoteFile, this.ssh.openSessionChannel(this.windowSpace, this.packetSize, null));
        try {
            return scp.readStreamFromRemote(remoteFile, progress);
        }
        catch (IOException ex) {
            scp.close();
            throw new SshException(ex, 6);
        }
    }

    static class ScpInputStream
    extends InputStream {
        long length;
        InputStream in;
        long count;
        ScpEngineIO engine;
        FileTransferProgress progress;
        String remoteFile;

        ScpInputStream(long length, InputStream in, ScpEngineIO engine, FileTransferProgress progress, String remoteFile) {
            this.length = length;
            this.in = in;
            this.engine = engine;
            this.progress = progress;
            this.remoteFile = remoteFile;
        }

        @Override
        public int read() throws IOException {
            if (this.count == this.length) {
                return -1;
            }
            if (this.count >= this.length) {
                throw new EOFException("End of file.");
            }
            int r = this.in.read();
            if (r == -1) {
                throw new EOFException("Unexpected EOF.");
            }
            ++this.count;
            if (this.count == this.length) {
                this.engine.waitForResponse();
                this.engine.writeOk();
                if (this.progress != null) {
                    this.progress.completed();
                }
            }
            if (this.progress != null) {
                if (this.progress.isCancelled()) {
                    throw new SshIOException(new SshException("SCP transfer was cancelled by user", 18));
                }
                this.progress.progressed(this.count);
            }
            return r;
        }

        @Override
        public int available() throws IOException {
            if (this.count == this.length) {
                return -1;
            }
            return (int)(this.length - this.count);
        }

        public long getFileSize() {
            return this.length;
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            if (this.count >= this.length) {
                return -1;
            }
            int r = this.in.read(buf, off, (int)(this.length - this.count > (long)len ? (long)len : this.length - this.count));
            if (r == -1) {
                throw new EOFException("Unexpected EOF.");
            }
            this.count += (long)r;
            if (this.count >= this.length) {
                this.engine.waitForResponse();
                this.engine.writeOk();
                if (this.progress != null) {
                    this.progress.completed();
                }
            }
            if (this.progress != null) {
                if (this.progress.isCancelled()) {
                    throw new SshIOException(new SshException("SCP transfer was cancelled by user", 18));
                }
                this.progress.progressed(this.count);
            }
            return r;
        }

        @Override
        public void close() throws IOException {
            try {
                this.engine.close();
            }
            catch (SshException ex) {
                throw new SshIOException(ex);
            }
        }
    }

    public class ScpEngineIO {
        protected byte[] buffer;
        protected String cmd;
        protected SshSession session;
        protected OutputStream out;
        protected InputStream in;

        protected ScpEngineIO(String cmd, SshSession session) throws SshException {
            try {
                int len = Math.min(Math.max(4096, ScpClientIO.this.packetSize), session.getMaximumRemotePacketLength());
                if (log.isDebugEnabled()) {
                    log.debug("{} - Calculated SCP block size of {}", (Object)session.getClient().getUuid(), (Object)len);
                }
                this.buffer = new byte[len];
                this.session = session;
                this.cmd = cmd;
                this.in = session.getInputStream();
                this.out = session.getOutputStream();
                if (!session.executeCommand(cmd)) {
                    session.close();
                    throw new SshException("Failed to execute the command " + cmd, 6);
                }
            }
            catch (SshIOException ex) {
                throw ex.getRealException();
            }
        }

        public void close() throws SshException {
            try {
                this.session.getOutputStream().close();
            }
            catch (IOException ex) {
                throw new SshException(ex);
            }
            try {
                Thread.sleep(500L);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.session.close();
        }

        protected void writeStreamToRemote(InputStream in, long length, String localName, FileTransferProgress progress) throws IOException {
            String cmd = "C0644 " + length + " " + localName + "\n";
            this.out.write(cmd.getBytes());
            if (log.isDebugEnabled()) {
                log.debug(cmd);
            }
            this.waitForResponse();
            this.writeCompleteFile(in, length, progress);
            this.writeOk();
            this.waitForResponse();
        }

        protected InputStream readStreamFromRemote(String remoteFile, FileTransferProgress progress) throws IOException {
            String cmd;
            String[] cmdParts = new String[3];
            this.writeOk();
            block9: while (true) {
                try {
                    cmd = this.readString();
                }
                catch (EOFException e) {
                    return null;
                }
                catch (SshIOException e2) {
                    return null;
                }
                char cmdChar = cmd.charAt(0);
                switch (cmdChar) {
                    case 'E': {
                        this.writeOk();
                        return null;
                    }
                    case 'T': {
                        continue block9;
                    }
                    case 'D': {
                        throw new IOException("Directories cannot be copied to a stream");
                    }
                    case 'C': {
                        this.parseCommand(cmd, cmdParts);
                        long len = Long.parseLong(cmdParts[1]);
                        this.writeOk();
                        if (progress != null) {
                            progress.started(len, remoteFile);
                        }
                        return new ScpInputStream(len, this.in, this, progress, remoteFile);
                    }
                }
                break;
            }
            this.writeError("Unexpected cmd: " + cmd);
            throw new IOException("SCP unexpected cmd: " + cmd);
        }

        protected void parseCommand(String cmd, String[] cmdParts) throws IOException {
            int l = cmd.indexOf(32);
            int r = cmd.indexOf(32, l + 1);
            if (l == -1 || r == -1) {
                this.writeError("Syntax error in cmd");
                throw new IOException("Syntax error in cmd");
            }
            cmdParts[0] = cmd.substring(1, l);
            cmdParts[1] = cmd.substring(l + 1, r);
            cmdParts[2] = cmd.substring(r + 1);
        }

        protected String readString() throws IOException {
            int ch;
            int i = 0;
            while ((ch = this.in.read()) != 10 && ch >= 0) {
                this.buffer[i++] = (byte)ch;
            }
            if (ch == -1) {
                throw new EOFException("Unexpected EOF");
            }
            if (this.buffer[0] == 10) {
                throw new IOException("Unexpected <NL>");
            }
            if (this.buffer[0] == 2 || this.buffer[0] == 1) {
                String msg = new String(this.buffer, 1, i - 1);
                if (this.buffer[0] == 2) {
                    throw new IOException(msg);
                }
                throw new IOException("SCP returned an unexpected error: " + msg);
            }
            if (this.buffer[0] == 0) {
                System.out.println("GOT ZERO AT 0 INDEX");
            }
            return new String(this.buffer, 0, i);
        }

        public void waitForResponse() throws IOException {
            if (log.isDebugEnabled()) {
                log.debug("Waiting for SCP acknowlegement");
            }
            int r = this.in.read();
            if (ScpClientIO.this.first) {
                ScpClientIO.this.first = false;
            }
            if (r == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Received SCP acknowlegement");
                }
                return;
            }
            if (r == -1) {
                throw new EOFException("SCP returned unexpected EOF");
            }
            String msg = this.readString();
            if (r == 2) {
                throw new IOException(msg);
            }
            throw new IOException("SCP returned an unexpected error: " + msg);
        }

        protected void writeOk() throws IOException {
            this.out.write(0);
        }

        protected void writeError(String reason) throws IOException {
            this.out.write(1);
            this.out.write(reason.getBytes());
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        protected void writeCompleteFile(InputStream in, long size, FileTransferProgress progress) throws IOException {
            try {
                int read;
                for (long count = 0L; count < size; count += (long)read) {
                    read = in.read(this.buffer, 0, (int)(size - count < (long)this.buffer.length ? size - count : (long)this.buffer.length));
                    if (read == -1) {
                        throw new EOFException("SCP received an unexpected EOF");
                    }
                    this.out.write(this.buffer, 0, read);
                    if (progress == null) continue;
                    if (progress.isCancelled()) {
                        throw new SshIOException(new SshException("SCP transfer was cancelled by user", 18));
                    }
                    progress.progressed(count);
                }
            }
            finally {
                in.close();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        protected void readCompleteFile(OutputStream out, long size, FileTransferProgress progress) throws IOException {
            try {
                int read;
                for (long count = 0L; count < size; count += (long)read) {
                    read = this.in.read(this.buffer, 0, (int)(size - count < (long)this.buffer.length ? size - count : (long)this.buffer.length));
                    if (read == -1) {
                        throw new EOFException("SCP received an unexpected EOF");
                    }
                    out.write(this.buffer, 0, read);
                    if (progress == null) continue;
                    if (progress.isCancelled()) {
                        throw new SshIOException(new SshException("SCP transfer was cancelled by user", 18));
                    }
                    progress.progressed(count);
                }
            }
            finally {
                out.close();
            }
        }
    }
}

