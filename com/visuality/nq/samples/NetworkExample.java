/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.Network;
import com.visuality.nq.client.Share;
import com.visuality.nq.common.NqException;
import java.util.Iterator;

public class NetworkExample {
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "password";
    String share = "share";
    String fileName = "myCreatedFile.txt";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public NetworkExample() {
        try {
            String serverInfo;
            PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
            System.out.println("Shares...");
            Iterator iterator = Network.enumerateShares(this.smbServer, credentials);
            if (!iterator.hasNext()) {
                System.out.println("No shares to display.");
            } else {
                while (iterator.hasNext()) {
                    Share.Info shareInfo = (Share.Info)iterator.next();
                    System.out.println("share name = " + shareInfo.name);
                }
            }
            System.out.println();
            System.out.println("Servers...");
            iterator = Network.enumerateServers(this.domain, credentials);
            if (!iterator.hasNext()) {
                System.out.println("No servers to display.");
            } else {
                while (iterator.hasNext()) {
                    serverInfo = (String)iterator.next();
                    System.out.println("server name = " + serverInfo);
                }
            }
            System.out.println();
            System.out.println("Domains...");
            iterator = Network.enumerateDomains(credentials);
            if (!iterator.hasNext()) {
                System.out.println("No domains to display.");
            } else {
                while (iterator.hasNext()) {
                    serverInfo = (String)iterator.next();
                    System.out.println("domain name = " + serverInfo);
                }
            }
        }
        catch (NqException e) {
            System.err.println("Unable to create file. Error = " + e.getMessage() + ", error code = " + e.getErrCode());
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

    public static void main(String[] args) {
        new NetworkExample();
    }
}

