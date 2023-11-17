/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client.test;

import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.HADownload;
import com.crushftp.client.HAUpload;
import com.crushftp.client.Worker;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class GenericClientTest
implements Runnable {
    final long MB4 = 0x400000L;

    public static void main(String[] args) {
        new GenericClientTest().run();
    }

    @Override
    public void run() {
        Common.trustEverything();
        this.performTest("file:///Users/spinkb/Desktop/ftp_testing/");
        this.performTest("ftp://127.0.0.1:2121/");
        this.performTest("ftps://127.0.0.1:9990/");
        this.performTest("ftpes://127.0.0.1:2121/");
        this.performTest("sftp://127.0.0.1:2222/");
        this.performTest("http://127.0.0.1:8080/");
        this.performTest("https://127.0.0.1:4443/");
        this.performTest("webdav://127.0.0.1:8080/");
        this.performTest("webdavs://127.0.0.1:4443/");
        System.out.println("#################################################################################");
        System.out.println("All tests completed.");
    }

    public void performTest(String url) {
        System.out.println("Performing test with url:" + url);
        GenericClient client = Common.getClient(url, "", null);
        try {
            client.login("test2", "test2", null);
            client.setConfig("pasv", "false");
            try {
                client.delete("/testDir");
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                client.delete("/MB5.txt");
            }
            catch (Exception exception) {
                // empty catch block
            }
            OutputStream out = client.upload("/MB4.txt", 0L, true, true);
            GenericClientTest.copyFile("/A_TestFiles/MB4.txt", 0L, out);
            Date date = new Date(1310000000000L);
            client.mdtm("/MB4.txt", date.getTime());
            out = client.upload("/MB5.txt", 0L, true, true);
            GenericClientTest.copyFile("/A_TestFiles/MB4.txt", 0x200000L, out);
            Vector names = this.getItemNames(client);
            if (names.size() == 0) {
                throw new Exception("Failed to get proper dir listing, size was 0.");
            }
            if (names.indexOf("MB4.txt") < 0) {
                throw new Exception("Transfer must have failed as a dir listing doesn't show the file.");
            }
            if (names.indexOf("testDir") >= 0) {
                throw new Exception("Delete must have failed as listing shows the testDir still exists.");
            }
            Properties itemInfo = client.stat("/MB4.txt");
            if (Long.parseLong(itemInfo.getProperty("size")) != 0x400000L) {
                throw new Exception("Server reported a different file size received:4194304 vs. " + itemInfo.getProperty("size"));
            }
            long diff = Math.abs(Long.parseLong(itemInfo.getProperty("modified")) - date.getTime());
            if (diff > 14000000L) {
                throw new Exception("Server reported a different file time:" + diff + "    " + date.getTime() + " vs. " + itemInfo.getProperty("modified") + "   " + date + " vs. " + new Date(Long.parseLong(itemInfo.getProperty("modified"))));
            }
            itemInfo = client.stat("/MB5.txt");
            if (Long.parseLong(itemInfo.getProperty("size")) != 0x200000L) {
                throw new Exception("Server reported a different file size received:2097152 vs. " + itemInfo.getProperty("size"));
            }
            out = client.upload("/MB5.txt", 0x200000L, false, true);
            GenericClientTest.copyFile("/A_TestFiles/MB4.txt", 0x200000L, out);
            itemInfo = client.stat("/MB5.txt");
            if (Long.parseLong(itemInfo.getProperty("size")) != 0x400000L) {
                throw new Exception("Server reported a different file size received:4194304 vs. " + itemInfo.getProperty("size"));
            }
            new File("test.txt").delete();
            InputStream in = client.download("/MB4.txt", 0L, -1L, true);
            Common.streamCopier(in, new FileOutputStream("test.txt"), false);
            if (new File("test.txt").length() != 0x400000L) {
                throw new Exception("We got a file that was not the same size as expected:4194304 vs. " + new File("test.txt").length());
            }
            new File("test.txt").delete();
            in = client.download("/MB4.txt", 0x200000L, -1L, true);
            Common.streamCopier(in, new FileOutputStream("test.txt"), false);
            if (new File("test.txt").length() != 0x200000L) {
                throw new Exception("We got a file that was not the same size as expected:2097152 vs. " + new File("test.txt").length());
            }
            in = client.download("/MB4.txt", 0x200000L, -1L, true);
            Common.streamCopier(in, new FileOutputStream("test.txt", true), false);
            if (new File("test.txt").length() != 0x400000L) {
                throw new Exception("We got a file that was not the same size as expected:4194304 vs. " + new File("test.txt").length());
            }
            try {
                client.delete("/MB5.txt");
            }
            catch (Exception exception) {
                // empty catch block
            }
            client.rename("/MB4.txt", "/MB5.txt", false);
            names = this.getItemNames(client);
            if (names.indexOf("MB4.txt") >= 0) {
                throw new Exception("Rename failed, found MB4.txt still.");
            }
            if (names.indexOf("MB5.txt") < 0) {
                throw new Exception("Rename failed, didn't find MB5.txt.");
            }
            client.delete("/MB5.txt");
            client.makedir("/testDir");
            names = this.getItemNames(client);
            if (names.indexOf("MB5.txt") >= 0) {
                throw new Exception("Delete must have failed as a dir listing shows the file still exists.");
            }
            if (names.indexOf("testDir") < 0) {
                throw new Exception("Make dir must have failed as testDir does not exist.");
            }
            System.out.println("Performing HA tests.");
            this.killClientDelayed(client, 6);
            out = new HAUpload(client, "/GB1_2.txt", -1L, false, true, 30, 1);
            GenericClientTest.copyFile("/A_TestFiles/GB1.txt", 0L, out);
            this.killClientDelayed(client, 4);
            in = new HADownload(client, "/GB1_2.txt", 0L, -1L, true, 1);
            Common.streamCopier(in, new FileOutputStream("test.txt", false), false);
            if (new File("test.txt").length() != new File("/A_TestFiles/GB1.txt").length()) {
                throw new Exception("We got a file that was not the same size as expected:" + new File("/A_TestFiles/GB1.txt").length() + " vs. " + new File("test.txt").length());
            }
            client.logout();
            System.out.println("Test succeeded for url:" + url);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(500L);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void killClientDelayed(final GenericClient c, final int delay) {
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        Thread.sleep(delay * 1000);
                        System.out.println("Killing connection midstream to verify HA.");
                        c.logout();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            });
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public Vector getItemNames(GenericClient client) throws Exception {
        Vector<String> itemNames = new Vector<String>();
        Vector v = client.list("/", new Vector());
        int x = 0;
        while (x < v.size()) {
            Properties p = (Properties)v.elementAt(x);
            itemNames.addElement(p.getProperty("name"));
            ++x;
        }
        return itemNames;
    }

    public static void copyFile(String path, long startPos, OutputStream out) throws Exception {
        RandomAccessFile in = new RandomAccessFile(path, "r");
        in.seek(startPos);
        byte[] b = new byte[32768];
        int bytesRead = 0;
        while (bytesRead >= 0) {
            bytesRead = in.read(b);
            if (bytesRead <= 0) continue;
            out.write(b, 0, bytesRead);
        }
        in.close();
        out.close();
    }
}

