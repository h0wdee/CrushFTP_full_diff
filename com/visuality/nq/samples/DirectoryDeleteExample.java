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
import java.util.Scanner;

public class DirectoryDeleteExample {
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";
    String fileName = "myCreatedFile.txt";
    File.Params fileParams = new File.Params(11, 7, 1, false);
    File.Params dirParams = new File.Params(11, 7, 1, true);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DirectoryDeleteExample() {
        try {
            PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
            Mount mount = new Mount(this.smbServer, this.share, credentials);
            Directory dir = new Directory(mount, "");
            Directory.Entry item = null;
            while ((item = dir.next()) != null) {
                if (!item.info.isDirectory()) continue;
                System.out.println("name = " + item.name);
            }
            dir.close();
            System.out.print("Enter name of directory to be deleted: ");
            Scanner scanner = new Scanner(System.in);
            String dirName = scanner.nextLine();
            scanner.close();
            this.deleteEverything(mount, dirName);
            mount.close();
        }
        catch (NqException e) {
            if (e.getErrCode() == -18) {
                System.out.println("Credentials error.");
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

    private void deleteEverything(Mount mt, String name) throws NqException {
        Directory dir = new Directory(mt, name);
        Directory.Entry item = null;
        while ((item = dir.next()) != null) {
            if (item.info.isDirectory()) {
                this.deleteEverything(mt, name + "\\" + item.name);
                continue;
            }
            File f = new File(mt, name + "\\" + item.name, this.fileParams);
            f.deleteOnClose();
            f.close();
        }
        dir.close();
        File dirFile = new File(mt, name, this.dirParams);
        dirFile.deleteOnClose();
        dirFile.close();
    }

    public static void main(String[] args) throws NqException {
        new DirectoryDeleteExample();
        Client.stop();
    }
}

