/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.common.Capture;
import com.visuality.nq.common.NqException;
import com.visuality.nq.config.Config;

public class LoggerExample {
    String host = "192.168.1.1";
    String share = "share";
    String user = "user";
    String pwd = "password";
    String domain = "mydomain";
    String dirName = "TestDir";

    public LoggerExample() throws NqException {
        Config.jnq.set("LOGFILE", "jnq.log");
        Config.jnq.set("LOGTHRESHOLD", 2000);
        Config.jnq.set("LOGMAXRECORDSINFILE", 10000);
        Config.jnq.set("CAPTUREFILE", "jnq.pcap");
        Config.jnq.set("CAPTUREMAXRECORDSINFILE", 10000);
    }

    public void someActivity() throws NqException {
        System.out.println("Do some SMB work.");
        PasswordCredentials creds = new PasswordCredentials(this.user, this.pwd, this.domain);
        Mount mount = new Mount(this.host, this.share, creds);
        File.Params params = new File.Params(3, 7, 3, true);
        File file = new File(mount, this.dirName, params);
        file.close();
        params = new File.Params(8, 7, 1, true);
        file = new File(mount, this.dirName, params);
        file.deleteOnClose();
        file.close();
    }

    public void startLogging() throws NqException {
        Config.jnq.set("LOGTOFILE", true);
    }

    public void stopLogging() throws NqException {
        Config.jnq.set("LOGTOFILE", false);
    }

    public void startCapture() {
        Capture.start();
    }

    public void stopCapture() {
        Capture.stop();
    }

    public void run() throws NqException {
        this.someActivity();
        this.startLogging();
        this.startCapture();
        this.someActivity();
        this.stopLogging();
        this.stopCapture();
        this.someActivity();
        this.startLogging();
        this.startCapture();
        this.someActivity();
    }

    public static void main(String[] args) throws NqException {
        new LoggerExample().run();
        System.out.println("Stopping jNQ.");
        Client.stop();
    }
}

