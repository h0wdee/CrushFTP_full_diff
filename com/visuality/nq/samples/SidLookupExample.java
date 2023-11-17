/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.rpc.Dcerpc;
import com.visuality.nq.client.rpc.Lsar;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.Sid;

public class SidLookupExample {
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SidLookupExample() throws NqException {
        try {
            PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
            Lsar lsar = new Lsar(this.smbServer, credentials);
            Dcerpc.Handle handle = lsar.openPolicy(2049);
            Sid sid = lsar.lookupName(handle, this.user);
            System.out.println("sid = " + sid);
            String sidName = lsar.lookupSid(handle, sid);
            System.out.println("sidName = " + sidName);
            sidName = lsar.lookupSid(handle, sid, true);
            System.out.println("sidName = " + sidName);
            lsar.close(handle);
            lsar.close();
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
        new SidLookupExample();
    }
}

