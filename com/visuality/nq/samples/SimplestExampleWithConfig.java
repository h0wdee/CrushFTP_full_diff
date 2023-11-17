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

public class SimplestExampleWithConfig {
    String dirName = "myCreatedDir";
    String fileName = "myCreatedFile.txt";

    public SimplestExampleWithConfig() {
        try {
            Config config = new Config();
            String user = config.getString("USER1");
            String password = config.getString("PASS1");
            String domain = config.getString("DOMAIN1");
            String host = config.getString("HOST1");
            String share = config.getString("SHARENAME1");
            PasswordCredentials credentials = new PasswordCredentials(user, password, domain);
            Mount mount = new Mount(host, share, credentials);
            File.Params dirParams = new File.Params(11, 7, 3, true);
            File dir = new File(mount, this.dirName, dirParams);
            dir.close();
            File.Params fileParams = new File.Params(11, 7, 3, false);
            File file = new File(mount, this.dirName + "\\" + this.fileName, fileParams);
            String someData = "This is some data";
            Buffer buffer = new Buffer(someData.getBytes(), 0, someData.getBytes().length);
            file.write(buffer);
            file.close();
            mount.close();
            Client.stop();
        }
        catch (NqException e) {
            System.err.println("Unable to create file. Error = " + e.getMessage() + ", error code = " + e.getErrCode());
        }
    }

    public static void main(String[] args) {
        new SimplestExampleWithConfig();
    }
}

