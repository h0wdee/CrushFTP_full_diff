/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.NqException;
import com.visuality.nq.samples.InternalSync;
import com.visuality.nq.samples.UserContext;

public class AsyncExample {
    private File.Params fileParams = new File.Params(75, 7, 3, false);
    private String filePath;
    private File file = null;
    private final int ONE_KB = 1024;
    private final int BUFFER_SIZE = 0x100000;
    private final int FILE_SIZE = 524288000;
    private final int MAX_TIME_TO_WAIT = 6000;
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";
    String fileName = "myCreatedFile.txt";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public AsyncExample() throws Exception {
        Mount mount = null;
        try {
            int bufferCounter;
            PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
            mount = new Mount(this.smbServer, this.share, credentials);
            this.filePath = this.fileName;
            this.file = new File(mount, this.filePath, this.fileParams);
            Buffer writeData = new Buffer(0x100000);
            for (int i = 0; i < 0x100000; ++i) {
                writeData.data[i] = (byte)(i * i + i);
            }
            int bufferSize = 0x100000;
            int writeSize = 524288000;
            int writeNow = 0;
            InternalSync sync = new InternalSync();
            UserContext userContext = new UserContext("writer", 524288000L);
            long startTime = System.currentTimeMillis() / 1000L;
            for (bufferCounter = 0; bufferCounter < 524288000; bufferCounter += writeNow) {
                writeData.dataLen = writeNow = bufferSize < writeSize ? bufferSize : writeSize;
                try {
                    this.file.write(writeData, sync, userContext);
                }
                catch (NqException e) {
                    System.out.println(e.getMessage() + ": 0x" + Long.toHexString(e.getErrCode()));
                    throw e;
                }
                writeSize -= writeNow;
            }
            long endTime = System.currentTimeMillis() / 1000L;
            InternalSync internalSync = sync;
            synchronized (internalSync) {
                sync.wait(6000L);
            }
            long avg = this.timeaverage(startTime, endTime, bufferCounter);
            System.out.println(String.format("Average Upload Speed is %s.%s MB/s \n", avg / 1024L, avg % 1024L / 10L));
            System.out.println("Total number of bytes written is " + userContext.numberOfBytes);
            this.file.close();
            this.file = new File(mount, this.filePath, this.fileParams);
            Buffer readData = new Buffer(0x100000);
            readData.dataLen = readData.data.length;
            bufferCounter = 0;
            long readNow = 0L;
            long readSize = 524288000L;
            InternalSync syncRead = new InternalSync();
            syncRead.status = null;
            userContext = new UserContext("reader");
            startTime = System.currentTimeMillis() / 1000L;
            while (0 == userContext.errCode) {
                readNow = (long)bufferSize < readSize ? (long)bufferSize : readSize;
                this.file.read(readData, syncRead, userContext);
                readSize -= readNow;
                bufferCounter = (int)((long)bufferCounter + readNow);
            }
            endTime = System.currentTimeMillis() / 1000L;
            InternalSync internalSync2 = syncRead;
            synchronized (internalSync2) {
                syncRead.wait(6000L);
            }
            avg = this.timeaverage(startTime, endTime, bufferCounter);
            System.out.println(String.format("Average Download Speed is %d.%d MB/s \n", avg / 1024L, avg % 1024L / 10L));
            System.out.println("Total number of bytes read is " + userContext.numberOfBytes);
            this.file.deleteOnClose();
            this.file.close();
        }
        catch (NqException e) {
            System.err.println("Error in reading. Error = " + e.getMessage() + ", error code = " + e.getErrCode());
        }
        finally {
            if (null != mount) {
                mount.close();
            }
        }
        try {
            Client.stop();
        }
        catch (NqException e) {
            System.err.println("Error in Client stop. Error = " + e.getMessage() + ", error code = " + e.getErrCode());
        }
    }

    private long timeaverage(long start, long end, long bytes) {
        long timeDiff = end - start;
        if (timeDiff == 0L) {
            return 1L;
        }
        long ans = bytes;
        ans /= timeDiff;
        return ans /= 1024L;
    }

    public static void main(String[] args) throws Exception {
        new AsyncExample();
    }
}

