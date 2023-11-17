/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.SubjectCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.NqException;
import com.visuality.nq.samples.auth.KerberosAuthenticator;
import javax.security.auth.Subject;

public class SubjectCredentialsExample {
    String smbServer = "server";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";
    String fileName = "myCreatedFile.txt";
    String kdc = "kdc";
    String realm = "realm";

    public SubjectCredentialsExample() throws NqException {
        try {
            KerberosAuthenticator kam = new KerberosAuthenticator();
            Subject subject = kam.login(this.user, this.password, this.kdc, this.realm);
            SubjectCredentials credentials = new SubjectCredentials(subject, this.realm);
            Mount mount = new Mount(this.smbServer, this.share, credentials);
            File.Params fileParams = new File.Params(11, 7, 3, false);
            File file = new File(mount, this.fileName, fileParams);
            String someData = "This is some data";
            Buffer buffer = new Buffer(someData.getBytes(), 0, someData.getBytes().length);
            file.write(buffer);
            file.close();
            mount.close();
            Client.stop();
            System.out.println("Done.");
        }
        catch (NqException e) {
            System.err.println("Unable to create file. Error = " + e.getMessage() + ", error code = " + e.getErrCode());
        }
    }

    public static void main(String[] args) throws NqException {
        new SubjectCredentialsExample();
    }
}

