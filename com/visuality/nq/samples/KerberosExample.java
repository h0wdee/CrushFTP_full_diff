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
import com.visuality.nq.config.Config;

public class KerberosExample {
    String smbServer = "server";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";
    String fileName = "myCreatedFile.txt";
    String kdc = "kdc";
    String realm = "realm";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public KerberosExample() {
        Mount mount = null;
        try {
            Config.jnq.set("KDC", this.kdc);
            Config.jnq.set("REALM", this.realm);
            PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
            mount = new Mount(this.smbServer, this.share, credentials);
            File.Params fileParams = new File.Params(11, 7, 3, false);
            File file = new File(mount, this.fileName, fileParams);
            String someData = "This is some data";
            Buffer buffer = new Buffer(someData.getBytes(), 0, someData.getBytes().length);
            file.write(buffer);
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
        new KerberosExample();
    }
}

