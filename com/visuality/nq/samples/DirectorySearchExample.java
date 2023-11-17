/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.Directory;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.common.NqException;

public class DirectorySearchExample {
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";
    Mount mount;
    String dirPath = "DirectorySearch";
    File.Params fileParams = new File.Params(11, 7, 3, false);
    File.Params dirParams = new File.Params(11, 7, 3, true);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DirectorySearchExample() throws NqException {
        try {
            PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
            this.mount = new Mount(this.smbServer, this.share, credentials);
            File dir = new File(this.mount, this.dirPath, this.dirParams);
            dir.close();
            this.createFile(this.dirPath + "/" + "aaadd.txt");
            this.createFile(this.dirPath + "/" + "bbb.txt");
            this.createFile(this.dirPath + "/" + "ccc.png");
            this.createFile(this.dirPath + "/" + "ddddd.stuff");
            this.createFile(this.dirPath + "/" + "eee.txt");
            this.createDir(this.dirPath + "/" + "fff.dir");
            this.createFile(this.dirPath + "/fff.dir/ggg.txt");
            System.out.println("\nTesting \\*.png");
            Directory myDir = new Directory(this.mount, this.dirPath + "\\*.png");
            Directory.Entry entry = null;
            while ((entry = myDir.next()) != null) {
                System.out.println("entry = " + entry.name);
            }
            myDir.close();
            System.out.println("\nTesting \\???dd.*");
            myDir = new Directory(this.mount, this.dirPath + "\\???dd.*");
            entry = null;
            while ((entry = myDir.next()) != null) {
                System.out.println("entry = " + entry.name);
            }
            myDir.close();
            System.out.println("\nTesting \\*.txt");
            myDir = new Directory(this.mount, this.dirPath + "\\*.txt");
            entry = null;
            while ((entry = myDir.next()) != null) {
                System.out.println("entry = " + entry.name);
            }
            myDir.close();
            this.deleteEverything(this.dirPath);
            this.mount.close();
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

    void createFile(String filePath) throws NqException {
        File file = new File(this.mount, filePath, this.fileParams);
        file.close();
    }

    void createDir(String dirPath) throws NqException {
        File file = new File(this.mount, dirPath, this.dirParams);
        file.close();
    }

    void deleteEverything(String name) throws NqException {
        Directory dir = new Directory(this.mount, name);
        Directory.Entry item = null;
        while ((item = dir.next()) != null) {
            if (item.info.isDirectory()) {
                this.deleteEverything(name + "\\" + item.name);
                continue;
            }
            File f = new File(this.mount, name + "\\" + item.name, this.fileParams);
            f.deleteOnClose();
            f.close();
        }
        dir.close();
        File dirFile = new File(this.mount, name, this.dirParams);
        dirFile.deleteOnClose();
        dirFile.close();
    }

    public static void main(String[] args) throws NqException {
        new DirectorySearchExample();
    }
}

