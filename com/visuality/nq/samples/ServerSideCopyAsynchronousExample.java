/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.SmbOutputStream;
import com.visuality.nq.common.NqException;
import java.io.IOException;
import java.io.OutputStream;

public class ServerSideCopyAsynchronousExample {
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";
    String sourceFileName = "sourceFile.txt";
    String destFileName = "destFile.txt";
    final int DATA_BUFFER_SIZE = 0x100000;
    final int FILE_SIZE = 0x6400000;
    final File.Params fileParams = new File.Params(11, 7, 2, false);
    final File.Params fileParamsSource = new File.Params(9, 7, 3, false);
    final File.Params fileParamsDest = new File.Params(11, 7, 3, false);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ServerSideCopyAsynchronousExample() {
        try {
            PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
            Mount mount = new Mount(this.smbServer, this.share, credentials);
            this.createLargeFile(mount, this.sourceFileName);
            long startTime = System.currentTimeMillis();
            this.asynchronicServerSideCopy(mount, this.sourceFileName, this.destFileName);
            long endTime = System.currentTimeMillis();
            System.out.println("Total copy time = " + (endTime - startTime) + " milliseconds.");
            mount.close();
        }
        catch (NqException e) {
            System.err.println("Unable to create file. Error = " + e.getMessage() + ", error code = " + e.getErrCode());
        }
        finally {
            try {
                Client.stop();
            }
            catch (NqException e) {
                e.printStackTrace();
            }
        }
    }

    void createLargeFile(Mount mount, String path) throws NqException {
        File file = new File(mount, path, this.fileParams);
        SmbOutputStream outputStream = new SmbOutputStream(file);
        byte[] writeData = this.createByteBuffer(0x100000);
        try {
            for (long currentSize = 0L; currentSize < 0x6400000L; currentSize += 0x100000L) {
                file.setPosition(currentSize);
                ((OutputStream)outputStream).write(writeData, 0, 0x100000);
            }
            ((OutputStream)outputStream).close();
        }
        catch (IOException e) {
            e.printStackTrace();
            NqException nqe = new NqException("IOException error", -23);
            nqe.initCause(e);
            throw nqe;
        }
        file.close();
    }

    final byte[] createByteBuffer(int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; ++i) {
            data[i] = (byte)((i * i + i) % 255);
        }
        return data;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean asynchronicServerSideCopy(Mount mount, String pathSource, String pathDestination) throws NqException {
        if (null == mount || null == pathSource || null == pathDestination) {
            return false;
        }
        File source = new File(mount, pathSource, this.fileParamsSource);
        File dest = new File(mount, pathDestination, this.fileParamsDest);
        ServerSideDataCopyCallback callback = new ServerSideDataCopyCallback(pathSource, pathDestination);
        boolean result = File.serverSideDataCopy(source, dest, callback);
        Object object = callback.callbackCond;
        synchronized (object) {
            try {
                if (!callback.isNotifySent) {
                    callback.callbackCond.wait();
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        source.close();
        dest.close();
        return result;
    }

    public static void main(String[] args) {
        new ServerSideCopyAsynchronousExample();
    }

    class ServerSideDataCopyCallback
    implements File.ServerSideDataCopyCallback {
        public Object callbackCond = new Object();
        public boolean isNotifySent = false;
        private String pathSource;
        private String pathDestination;
        private long totalBytesWrittenInFile = 0L;

        public ServerSideDataCopyCallback(String pathSource, String pathDestination) {
            this.pathSource = pathSource;
            this.pathDestination = pathDestination;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void callback(Throwable status, long fileBytesSize, int totalBytesWritten) {
            boolean finished = false;
            if (null == status) {
                this.totalBytesWrittenInFile += (long)totalBytesWritten;
                System.out.println("Copied " + this.totalBytesWrittenInFile + " bytes from " + fileBytesSize + " total bytes");
                if (this.totalBytesWrittenInFile == fileBytesSize) {
                    System.out.println("Finished copy data from file " + this.pathSource + " to file " + this.pathDestination);
                    finished = true;
                }
            } else {
                NqException nqe = (NqException)status;
                System.out.println("Error in server side copy operation = " + Long.toHexString(nqe.getErrCode()));
                finished = true;
            }
            if (finished) {
                Object object = this.callbackCond;
                synchronized (object) {
                    this.callbackCond.notify();
                    this.isNotifySent = true;
                }
            }
        }
    }
}

