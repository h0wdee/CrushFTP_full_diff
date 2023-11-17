/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.SmbOutputStream;
import com.visuality.nq.common.NqException;
import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamExample {
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";
    String fileName = "myCreatedFile.txt";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public OutputStreamExample() {
        Mount mount = null;
        try {
            PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
            mount = new Mount(this.smbServer, this.share, credentials);
            File.Params fileParams = new File.Params(11, 7, 3, false);
            File file = new File(mount, this.fileName, fileParams);
            SmbOutputStream outputStream = new SmbOutputStream(file);
            byte[] data = this.createByteBuffer();
            try {
                int amountToWrite;
                int bufferSize = 128;
                for (int currentPosition = 0; currentPosition < data.length; currentPosition += amountToWrite) {
                    amountToWrite = data.length - currentPosition > bufferSize ? bufferSize : data.length - currentPosition;
                    ((OutputStream)outputStream).write(data, currentPosition, amountToWrite);
                }
                ((OutputStream)outputStream).close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            file.close();
        }
        catch (NqException e) {
            System.err.println("An error occurred. Error = " + e.getMessage() + ", error code = " + e.getErrCode());
        }
        finally {
            if (null != mount) {
                mount.close();
            }
        }
        try {
            Client.stop();
        }
        catch (NqException e) {
            System.err.println("Calling Client.stop() failed. Error = " + e.getMessage() + ", error code = " + e.getErrCode());
        }
    }

    private final byte[] createByteBuffer() {
        String data = "Fourscore and seven years ago our fathers brought forth on this continent, a new nation, conceived in Liberty, and dedicated to the proposition that all men are created equal.Now we are engaged in a great civil war, testing whether that nation, or any nation so conceived and so dedicated, can long endure. We are met on a great battle-field of that war. We have come to dedicate a portion of that field, as a final resting place for those who here gave their lives that that nation might live. It is altogether fitting and proper that we should do this.But, in a larger sense, we can not dedicate-we can not consecrate-we can not hallow-this ground. The brave men, living and dead, who struggled here, have consecrated it, far above our poor power to add or detract. The world will little note, nor long remember what we say here, but it can never forget what they did here. It is for us the living, rather, to be dedicated here to the unfinished work which they who fought here have thus far so nobly advanced. It is rather for us to be here dedicated to the great task remaining before us-that from these honored dead we take increased devotion to that cause for which they gave the last full measure of devotion-that we here highly resolve that these dead shall not have died in vain-that this nation, under God, shall have a new birth of freedom-and that government of the people, by the people, for the people shall not perish from the earth.";
        return data.getBytes();
    }

    public static void main(String[] args) {
        new OutputStreamExample();
    }
}

