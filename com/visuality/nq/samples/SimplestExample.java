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

public class SimplestExample {
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";
    String dirName = "myCreatedDir";
    String fileName = "myCreatedFile.txt";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SimplestExample() {
        Mount mount = null;
        try {
            PasswordCredentials credentials = new PasswordCredentials(this.domain + "\\" + this.user, this.password, null);
            mount = new Mount(this.smbServer, this.share, credentials);
            File.Params dirParams = new File.Params(11, 7, 3, true);
            File dir = new File(mount, this.dirName, dirParams);
            dir.close();
            File.Params fileParams = new File.Params(11, 7, 3, false);
            File file = new File(mount, this.dirName + "\\" + this.fileName, fileParams);
            String someData = "This is some data";
            Buffer buffer = new Buffer(someData.getBytes(), 0, someData.getBytes().length);
            file.write(buffer);
            file.close();
        }
        catch (NqException e) {
            System.err.println("An error occurred. Error = " + e.getMessage() + ", error code = " + e.getErrCode());
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
            System.err.println("Calling Client.stop() failed. Error = " + e.getMessage() + ", error code = " + e.getErrCode());
        }
    }

    public static void main(String[] args) {
        new SimplestExample();
    }
}

