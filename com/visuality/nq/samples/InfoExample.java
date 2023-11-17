/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.Share;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.NqException;

public class InfoExample {
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";
    String fileName = "myCreatedFile.txt";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public InfoExample() {
        try {
            PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
            Mount mount = new Mount(this.smbServer, this.share, credentials);
            Share.Info shareInfo = mount.getShare().getInfo();
            System.out.println(String.format("\n\tDisk free space information:\n\t\tsectors per cluster: %s\n\t\tbytes per sector:    %s\n\t\tfree clusters:       %s\n\t\ttotal clusters:      %s\n", shareInfo.sectorsPerCluster, shareInfo.bytesPerSector, shareInfo.freeClusters, shareInfo.totalClusters));
            File.Params fileParams = new File.Params(11, 7, 3, false);
            File file = new File(mount, "MyFile", fileParams);
            String someData = "This is some data";
            Buffer buffer = new Buffer(someData.getBytes(), 0, someData.getBytes().length);
            file.write(buffer);
            File.Info info = file.getInfo();
            System.out.println(String.format("\n\tFile information:\n\t\tfile create time: %s\n\t\tfile attribute bits (hex):    %x\n\t\tend of file pointer:    %s\n", info.getCreationTime(), info.getAttributes(), info.getEof()));
            if ((info.getAttributes() & 0x20) != 0) {
                System.out.println("This file is ready for archive.");
            }
            file.deleteOnClose();
            file.close();
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

    public static void main(String[] args) {
        new InfoExample();
    }
}

