/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.pool;

import com.maverick.pool.SshClientPool;
import com.maverick.pool.SshTransferListener;
import com.maverick.sftp.FileTransferProgress;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshException;
import com.sshtools.sftp.SftpClient;
import java.io.IOException;

public class SshTransferQueue {
    SshClientPool pool;

    public SshTransferQueue(SshClientPool pool) {
        this.pool = pool;
    }

    public void postGet(String host, int port, String username, String filename, SshTransferListener listener) throws SshException, IOException {
        this.pool.executor.execute(new GetTransfer(this.pool.checkout(host, port, username), filename, listener));
    }

    public void postPut(String host, int port, String username, String filename, SshTransferListener listener) throws SshException, IOException {
        this.pool.executor.execute(new GetTransfer(this.pool.checkout(host, port, username), filename, listener));
    }

    class PutTransfer
    extends Transfer {
        PutTransfer(SshClient client, String filename, SshTransferListener listener) {
            super(client, filename, listener);
        }

        @Override
        public void run() {
            try {
                SftpClient sftp = new SftpClient(this.client);
                sftp.put(this.filename, new FileTransferProgress(){

                    @Override
                    public void started(long bytesTotal, String remoteFile) {
                    }

                    @Override
                    public void progressed(long bytesSoFar) {
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }

                    @Override
                    public void completed() {
                        PutTransfer.this.listener.transferComplete();
                    }
                });
            }
            catch (Exception e) {
                this.listener.transferFailed();
            }
        }
    }

    class GetTransfer
    extends Transfer {
        GetTransfer(SshClient client, String filename, SshTransferListener listener) {
            super(client, filename, listener);
        }

        @Override
        public void run() {
            try {
                SftpClient sftp = new SftpClient(this.client);
                sftp.get(this.filename, new FileTransferProgress(){

                    @Override
                    public void started(long bytesTotal, String remoteFile) {
                    }

                    @Override
                    public void progressed(long bytesSoFar) {
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }

                    @Override
                    public void completed() {
                        GetTransfer.this.listener.transferComplete();
                    }
                });
            }
            catch (Exception e) {
                this.listener.transferFailed();
            }
            finally {
                SshTransferQueue.this.pool.checkin(this.client);
            }
        }
    }

    abstract class Transfer
    implements Runnable {
        SshClient client;
        String filename;
        SshTransferListener listener;

        Transfer(SshClient client, String filename, SshTransferListener listener) {
            this.client = client;
            this.filename = filename;
            this.listener = listener;
        }
    }
}

