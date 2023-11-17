/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.SmbInputStream;
import com.visuality.nq.client.SmbOutputStream;
import com.visuality.nq.common.NqException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileCopyExample {
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";
    final int DATA_BUFFER_SIZE = 0x100000;
    final int FILE_SIZE = 0x6400000;
    final File.Params fileParamsSource = new File.Params(9, 7, 3, false);
    final File.Params fileParamsDest = new File.Params(11, 7, 3, false);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public FileCopyExample() {
        try {
            PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
            Mount mount = new Mount(this.smbServer, this.share, credentials);
            this.createLargeFile(mount, "JoesLargeFile");
            long startTime = System.currentTimeMillis();
            this.copy(mount, "JoesLargeFile", "JoesNewLargeFile");
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
        File file = new File(mount, path, this.fileParamsDest);
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

    boolean copy(Mount mount, String pathSource, String pathDestination) throws NqException {
        if (null == mount || null == pathSource || null == pathDestination) {
            return false;
        }
        int amountOfDataRead = 0;
        long bytesCopied = 0L;
        byte[] data = new byte[0x100000];
        File source = new File(mount, pathSource, this.fileParamsSource);
        File dest = new File(mount, pathDestination, this.fileParamsDest);
        SmbInputStream inputStream = null;
        SmbOutputStream outputStream = null;
        try {
            inputStream = new SmbInputStream(source);
            outputStream = new SmbOutputStream(dest);
            while ((amountOfDataRead = ((InputStream)inputStream).read(data, 0, 0x100000)) > 0) {
                ((OutputStream)outputStream).write(data, 0, amountOfDataRead);
                bytesCopied += (long)amountOfDataRead;
            }
            System.out.println("Bytes copied: " + bytesCopied);
        }
        catch (IOException e) {
            NqException nqe = new NqException("IOException error", -23);
            nqe.initCause(e);
            throw nqe;
        }
        finally {
            try {
                if (null != inputStream) {
                    ((InputStream)inputStream).close();
                }
                if (null != outputStream) {
                    ((OutputStream)outputStream).close();
                }
            }
            catch (IOException e) {}
        }
        return true;
    }

    public static void main(String[] args) {
        new FileCopyExample();
    }
}

