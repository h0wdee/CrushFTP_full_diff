/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.SmbInputStream;
import com.visuality.nq.common.NqException;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamExample {
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";
    String fileName = "myCreatedFile.txt";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public InputStreamExample() {
        Mount mount = null;
        try {
            PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
            mount = new Mount(this.smbServer, this.share, credentials);
            File.Params fileParams = new File.Params(9, 7, 3, false);
            File file = new File(mount, this.fileName, fileParams);
            SmbInputStream inputStream = new SmbInputStream(file);
            int dataBufferSize = 128;
            byte[] data = new byte[dataBufferSize];
            int amountOfDataRead = 0;
            try {
                while ((amountOfDataRead = ((InputStream)inputStream).read(data, 0, dataBufferSize)) > 0) {
                    String stringData = new String(data, 0, amountOfDataRead);
                    System.out.println("data read = " + stringData);
                }
                ((InputStream)inputStream).close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            file.close();
        }
        catch (NqException e) {
            System.err.println("Unable to create file. Error = " + e.getMessage() + ", error code = " + e.getErrCode());
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

    public static void main(String[] args) {
        new InputStreamExample();
    }
}

