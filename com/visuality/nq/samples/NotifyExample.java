/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.AsyncConsumer;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.Directory;
import com.visuality.nq.client.File;
import com.visuality.nq.client.FileNotifyInformation;
import com.visuality.nq.client.Mount;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SmbException;

public class NotifyExample {
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";
    String directoryName = "NotifyDirectory";
    Mount mount;
    File dirBeingMonitored;
    NotifyChangesListener listener = new NotifyChangesListener();
    File.Params fileParams = new File.Params(11, 7, 3, false);
    File.Params dirParams = new File.Params(11, 7, 3, true);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public NotifyExample() {
        try {
            PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
            this.mount = new Mount(this.smbServer, this.share, credentials);
            this.dirBeingMonitored = new File(this.mount, this.directoryName, this.dirParams);
            this.dirBeingMonitored.notifyChanges(2047, this.listener);
            File file1 = new File(this.mount, this.directoryName + "/file1", this.fileParams);
            file1.close();
            File file2 = new File(this.mount, this.directoryName + "/file2", this.fileParams);
            File.Info info = file2.getInfo();
            info.setAllocationSize(1000L);
            file2.setInfo(info);
            file2.close();
            File.delete(this.mount, this.directoryName + "/file2");
            file1 = new File(this.mount, this.directoryName + "/file1", this.fileParams);
            file1.deleteOnClose();
            file1.close();
            this.displayDirectoryContents(this.mount, this.directoryName);
            this.dirBeingMonitored.deleteOnClose();
            this.dirBeingMonitored.close();
            this.mount.close();
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

    void displayDirectoryContents(Mount mount, String path) throws NqException {
        Directory.Entry entry;
        Directory dir = new Directory(mount, path);
        System.out.println("Contents of " + path + "...");
        while ((entry = dir.next()) != null) {
            System.out.println("\t" + entry.name);
        }
        System.out.println("---");
    }

    public static void main(String[] args) throws NqException {
        new NotifyExample();
        Client.stop();
    }

    public class NotifyChangesListener
    implements AsyncConsumer {
        public void complete(Throwable status, long length, Object context) throws NqException {
            System.out.println("\n\nSomething has changed in the directory.");
            if (status instanceof SmbException) {
                FileNotifyInformation[] fileInfo;
                SmbException smbStatus = (SmbException)status;
                if (268 == smbStatus.getErrCode()) {
                    System.out.println("*** Received STATUS_ENUMDIR ***");
                    NotifyExample.this.displayDirectoryContents(NotifyExample.this.mount, NotifyExample.this.directoryName);
                    if (null != NotifyExample.this.dirBeingMonitored) {
                        NotifyExample.this.dirBeingMonitored.notifyChanges(2047, NotifyExample.this.listener);
                    }
                    return;
                }
                block11: for (FileNotifyInformation fni : fileInfo = (FileNotifyInformation[])context) {
                    int actualNotification = fni.getAction();
                    switch (actualNotification) {
                        case 1: {
                            System.out.println("*** File was added: " + fni.getFileName());
                            System.out.println(fni);
                            continue block11;
                        }
                        case 4: {
                            System.out.println("*** File was renamed: " + fni.getFileName());
                            System.out.println(fni);
                            continue block11;
                        }
                        case 5: {
                            System.out.println("*** File's new name: " + fni.getFileName());
                            System.out.println(fni);
                            continue block11;
                        }
                        case 2: {
                            System.out.println("*** File was removed: " + fni.getFileName());
                            System.out.println(fni);
                            continue block11;
                        }
                        case 3: {
                            System.out.println("*** File was modified: " + fni.getFileName());
                            System.out.println(fni);
                            continue block11;
                        }
                        default: {
                            System.out.println("*** Something happened: " + fni);
                        }
                    }
                }
            } else if (status instanceof ClientException) {
                ClientException ce = (ClientException)status;
                switch (ce.getErrCode()) {
                    case -1073741738: {
                        System.out.println("... DELETE_PENDING");
                        break;
                    }
                    case -1073741528: {
                        System.out.println("... STATUS_FILE_CLOSED");
                        break;
                    }
                    default: {
                        System.out.println("We got a status of " + ce.getErrCode());
                    }
                }
            }
            if (null != NotifyExample.this.dirBeingMonitored) {
                NotifyExample.this.dirBeingMonitored.notifyChanges(2047, NotifyExample.this.listener);
            }
        }
    }
}

