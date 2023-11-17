/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.GssContextCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.Directory;
import com.visuality.nq.client.Mount;
import com.visuality.nq.common.NqException;
import com.visuality.nq.samples.auth.GssLogin;

public class GssContextCredentialsExample {
    String smbServer = "ServerName";
    String share = "share";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String kdc = "kdc";
    String realm = "realm";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void openDirWithGssContextCreds() throws NqException {
        Mount mount = null;
        try {
            Directory.Entry entry;
            GssContextCredentials gssCreds = GssLogin.login(this.smbServer, this.user, this.password, this.kdc, this.realm);
            mount = new Mount(this.smbServer, this.share, gssCreds);
            Directory dir = new Directory(mount, "");
            while ((entry = dir.next()) != null) {
                System.out.println("------> file = " + entry.name);
            }
            dir.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != mount) {
                mount.close();
            }
        }
    }

    public static void main(String[] args) throws NqException {
        GssContextCredentialsExample example = new GssContextCredentialsExample();
        example.openDirWithGssContextCreds();
        Client.stop();
    }
}

