/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.Share;
import com.visuality.nq.client.rpc.Dcerpc;
import com.visuality.nq.client.rpc.Lsar;
import com.visuality.nq.client.rpc.Srvsvc;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SecurityDescriptor;
import com.visuality.nq.common.Sid;
import java.util.List;

public class PermissionsExample {
    String smbServer = "server";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";
    String fileName = "A.txt";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PermissionsExample() {
        try {
            PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
            Mount mount = new Mount(this.smbServer, this.share, credentials);
            File.Params fileParams = new File.Params(75, 7, 3, false);
            File file = new File(mount, this.fileName, fileParams);
            File.Info info = file.getInfo();
            System.out.println("allocation size = " + info.getAllocationSize());
            System.out.println("change time = " + info.getChangeTime());
            System.out.println("create time = " + info.getCreationTime());
            System.out.println("number of bytes in file = " + info.getEof());
            System.out.println("number of links to file = " + info.getNumberOfLinks());
            System.out.println("This is a directory? " + info.isDirectory());
            System.out.println("This file is read only? " + info.isReadOnly());
            System.out.println("file path = " + info.getPath());
            System.out.println("file attribute bits = 0x" + Integer.toHexString(info.getAttributes()));
            System.out.println("See https://docs.microsoft.com/en-us/windows/desktop/FileIO/file-attribute-constants for list of attribute constant meanings.");
            SecurityDescriptor sd = file.querySecurityDescriptor();
            System.out.println("\nFile's security info:");
            this.printSDInfo(sd, this.smbServer, credentials);
            Srvsvc pipe = new Srvsvc(this.smbServer, credentials);
            Share.Info shareInfo = pipe.shareGetInfo(this.share);
            System.out.println("\nShare's security info:");
            this.printSDInfo(shareInfo.sd, this.smbServer, credentials);
            mount.close();
            System.out.println("Done.");
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

    private void printSDInfo(SecurityDescriptor sd, String smbServer, Credentials credentials) throws NqException {
        Lsar lsar = new Lsar(smbServer, credentials);
        Dcerpc.Handle handle = lsar.openPolicy(2049);
        Sid owner = sd.getOwnerSid();
        Sid group = sd.getGroupSid();
        System.out.println("Owner Sid = " + owner + ", owner name = " + lsar.lookupSid(handle, owner));
        System.out.println("Group Sid = " + group + ", group name = " + lsar.lookupSid(handle, group));
        SecurityDescriptor.Dacl dacl = sd.getDacl();
        List aces = dacl.aces;
        if (null != aces) {
            System.out.println("\nThere are " + aces.size() + " Ace entries.");
            for (SecurityDescriptor.Ace ace : aces) {
                System.out.println("ace = " + ace);
                Sid sid = ace.sid;
                String user = lsar.lookupSid(handle, sid);
                System.out.println("user name = " + user);
            }
        }
        lsar.close();
    }

    public static void main(String[] args) {
        new PermissionsExample();
    }
}

