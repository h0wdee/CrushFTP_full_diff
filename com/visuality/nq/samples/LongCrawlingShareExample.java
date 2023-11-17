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
import java.util.concurrent.ConcurrentLinkedQueue;

public class LongCrawlingShareExample {
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";
    ConcurrentLinkedQueue<String> folders = new ConcurrentLinkedQueue();
    ConcurrentLinkedQueue<String> files = new ConcurrentLinkedQueue();
    boolean doExit = false;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public LongCrawlingShareExample() throws NqException {
        try {
            Thread crawlFolders = new Thread((Runnable)new CrawlFolders(), "crawlFolders");
            Thread printFilesInfo = new Thread((Runnable)new PrintFilesInfo(), "printFilesInfo");
            this.folders.add("");
            crawlFolders.start();
            printFilesInfo.start();
            try {
                crawlFolders.join();
            }
            catch (InterruptedException e) {
                // empty catch block
            }
            this.doExit = true;
            try {
                printFilesInfo.join();
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
            System.out.println("Done.");
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
        new LongCrawlingShareExample();
    }

    class PrintFilesInfo
    implements Runnable {
        PrintFilesInfo() {
        }

        public void run() {
            Mount mount;
            PasswordCredentials credentials;
            try {
                credentials = new PasswordCredentials(LongCrawlingShareExample.this.user, LongCrawlingShareExample.this.password, LongCrawlingShareExample.this.domain);
                mount = new Mount(LongCrawlingShareExample.this.smbServer, LongCrawlingShareExample.this.share, credentials);
            }
            catch (NqException e) {
                System.out.println("Error in thread PrintFilesInfo: " + e + " Exit thread.");
                return;
            }
            while (!LongCrawlingShareExample.this.doExit) {
                while (!LongCrawlingShareExample.this.files.isEmpty()) {
                    String name = LongCrawlingShareExample.this.files.poll();
                    if (null == name) continue;
                    try {
                        this.printFileInfo(mount, name);
                    }
                    catch (NqException e) {
                        System.out.println("Error in thread PrintFilesInfo: " + e + " Try to Mount again.");
                        try {
                            mount = new Mount(LongCrawlingShareExample.this.smbServer, LongCrawlingShareExample.this.share, credentials);
                            this.printFileInfo(mount, name);
                        }
                        catch (NqException nqe) {
                            System.out.println("Error in thread PrintFilesInfo: " + nqe);
                        }
                    }
                }
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException interruptedException) {}
            }
            mount.close();
        }

        private void printFileInfo(Mount mount, String name) throws NqException {
            System.out.println("Getting info of file " + name);
            File.Params fileParams = new File.Params(1, 7, 1, false);
            File file = new File(mount, name, fileParams);
            File.Info info = file.getInfo();
            System.out.println(String.format("\n\tFile information:\n\t\tfile create time: %s\n\t\tfile attribute bits (hex):    %x\n\t\tend of file pointer:    %s\n", info.getCreationTime(), info.getAttributes(), info.getEof()));
            file.close();
        }
    }

    class CrawlFolders
    implements Runnable {
        CrawlFolders() {
        }

        public void run() {
            Mount mount;
            PasswordCredentials credentials;
            try {
                credentials = new PasswordCredentials(LongCrawlingShareExample.this.user, LongCrawlingShareExample.this.password, LongCrawlingShareExample.this.domain);
                mount = new Mount(LongCrawlingShareExample.this.smbServer, LongCrawlingShareExample.this.share, credentials);
            }
            catch (NqException e) {
                System.out.println("Error in thread CrawlFolders: " + e + " Exit thread.");
                return;
            }
            while (!LongCrawlingShareExample.this.folders.isEmpty()) {
                String name = LongCrawlingShareExample.this.folders.poll();
                System.out.println("Scanning directory " + name);
                if (null == name) continue;
                try {
                    this.crawlFolder(mount, name);
                }
                catch (NqException e) {
                    System.out.println("Error in thread CrawlFolders: " + e + " Try to Mount again.");
                    try {
                        mount = new Mount(LongCrawlingShareExample.this.smbServer, LongCrawlingShareExample.this.share, credentials);
                        this.crawlFolder(mount, name);
                    }
                    catch (NqException nqe) {
                        System.out.println("Error in thread CrawlFolders: " + nqe);
                    }
                }
            }
            mount.close();
        }

        private void crawlFolder(Mount mount, String name) throws NqException {
            Directory dir = new Directory(mount, name);
            Directory.Entry item = null;
            while ((item = dir.next()) != null) {
                String path = 0 < name.length() ? name + "\\" + item.name : item.name;
                System.out.println("name = " + path + ", is it a dir = " + item.info.isDirectory());
                if (item.info.isDirectory()) {
                    if (LongCrawlingShareExample.this.folders.contains(path)) continue;
                    LongCrawlingShareExample.this.folders.add(path);
                    continue;
                }
                if (LongCrawlingShareExample.this.files.contains(path)) continue;
                LongCrawlingShareExample.this.files.add(path);
            }
            dir.close();
        }
    }
}

