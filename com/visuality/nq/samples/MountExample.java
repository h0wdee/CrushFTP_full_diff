/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.Directory;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.MountParams;
import com.visuality.nq.common.NqException;

public class MountExample {
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";
    String fileName = "myCreatedFile.txt";
    File.Params fileParams = new File.Params(11, 7, 3, false);
    File.Params dirParams = new File.Params(11, 7, 3, true);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public MountExample() {
        try {
            PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
            MountParams mountParams = new MountParams();
            mountParams.minDialect = 514;
            Mount mount = new Mount(this.smbServer, this.share, (Credentials)credentials, mountParams);
            Directory dir = new Directory(mount, "");
            Directory.Entry item = null;
            while ((item = dir.next()) != null) {
                if (!item.info.isDirectory()) continue;
                System.out.println("name = " + item.name);
            }
            dir.close();
            mount.close();
        }
        catch (NqException e) {
            if (e.getErrCode() == -18) {
                System.out.println("Credentials error: " + e);
            } else {
                System.err.println("Unable to create file. Error = " + e.getMessage() + ", error code = " + e.getErrCode());
            }
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

    public static void main(String[] args) throws NqException {
        new MountExample();
        Client.stop();
    }
}

