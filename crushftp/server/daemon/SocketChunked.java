/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server.daemon;

import com.crushftp.client.Worker;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

public class SocketChunked
extends Socket {
    Socket sock = null;
    DataOutputStream out = null;
    DataInputStream in = null;
    long id1 = 0L;
    static final int DATA_SOCK = 0;
    static final int CREATE_SOCK = 1;
    static final int CLOSE_SOCK = 2;
    static final int PING = 3;
    static final int PONG = 4;
    static final int NOOP = 5;

    public SocketChunked(Socket sock) throws IOException {
        this.sock = sock;
        this.in = new DataInputStream(sock.getInputStream());
        this.out = new DataOutputStream(sock.getOutputStream());
    }

    public void init(long id1) {
        this.id1 = id1;
    }

    public SocketChunked clearId() throws IOException {
        this.id1 = 0L;
        this.writeChunk(new byte[0], 0, 0, 2);
        return this;
    }

    public synchronized void writeChunk(final byte[] b, final int offset, final int len1, final int command) throws IOException {
        final Properties status = new Properties();
        Thread t = new Thread(new Runnable(){

            @Override
            public void run() {
                try {
                    SocketChunked.this.out.writeLong(SocketChunked.this.id1);
                    SocketChunked.this.out.writeInt(command);
                    SocketChunked.this.out.writeInt(len1);
                    if (b != null) {
                        SocketChunked.this.out.write(b, offset, len1);
                    }
                    SocketChunked.this.out.writeLong(SocketChunked.this.id1);
                    SocketChunked.this.out.flush();
                    status.put("done", "");
                }
                catch (IOException e) {
                    status.put("e", e);
                }
            }
        });
        t.start();
        try {
            t.join(30000L);
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
        if (status.containsKey("error")) {
            throw (IOException)status.remove("e");
        }
        if (!status.containsKey("done")) {
            t.interrupt();
            this.out.close();
        }
    }

    public Properties readChunk(int timeout) throws IOException {
        byte[] b;
        int command;
        long id2;
        while (true) {
            this.sock.setSoTimeout(timeout);
            id2 = this.in.readLong();
            if (this.id1 == 0L || id2 == 0L) {
                this.id1 = id2;
            }
            if (this.id1 != id2) {
                throw new IOException("mis-match1 on configured id.  id1:" + this.id1 + " id2:" + id2);
            }
            command = this.in.readInt();
            int len = this.in.readInt();
            b = new byte[len];
            int pos = 0;
            while (pos < b.length) {
                int i = this.in.read(b, pos, b.length - pos);
                if (i >= 0) {
                    pos += i;
                }
                if (i >= 0) continue;
                throw new IOException("mis-match2 on bytes received.  Expected:" + len + " but got:" + pos);
            }
            long id3 = this.in.readLong();
            if (this.id1 != id3) {
                throw new IOException("mis-match3 on configured id.  id1:" + this.id1 + " id3:" + id3);
            }
            if (command != 3) break;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        SocketChunked.this.writeChunk(new byte[0], 0, 0, 4);
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                }
            });
        }
        Properties p = new Properties();
        p.put("c", String.valueOf(command));
        p.put("b", b);
        p.put("id", String.valueOf(id2));
        return p;
    }

    @Override
    public InputStream getInputStream() {
        throw new RuntimeException("Not allowed to access SocketChunked inputstream.  Use readChunk()");
    }

    @Override
    public OutputStream getOutputStream() {
        throw new RuntimeException("Not allowed to access SocketChunked outputstream.  Use writeChunk(byte[],len)");
    }

    @Override
    public boolean isClosed() {
        return this.sock.isClosed();
    }

    @Override
    public void close() {
        try {
            this.in.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.out.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.sock.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

