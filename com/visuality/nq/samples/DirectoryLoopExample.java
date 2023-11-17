/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.Directory;
import com.visuality.nq.client.Mount;
import com.visuality.nq.common.NqException;

public class DirectoryLoopExample {
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DirectoryLoopExample() throws NqException {
        try {
            PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
            Mount mount = new Mount(this.smbServer, this.share, credentials);
            Directory dir = new Directory(mount, "");
            String lastDirectory = null;
            Directory.Entry item = null;
            while ((item = dir.next()) != null) {
                System.out.println("name = " + item.name + ", is it a dir = " + item.info.isDirectory());
                if (!item.info.isDirectory()) continue;
                lastDirectory = item.name;
            }
            dir.close();
            if (null != lastDirectory) {
                System.out.println();
                System.out.println("Scanning directory " + lastDirectory);
                Directory drilledDir = new Directory(mount, lastDirectory);
                while ((item = drilledDir.next()) != null) {
                    System.out.println("Drilled down name = " + item.name + ", is it a dir = " + item.info.isDirectory());
                }
                drilledDir.close();
            }
            mount.close();
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
        new DirectoryLoopExample();
    }
}

