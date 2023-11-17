/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.samples;

import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.Mount;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.config.Config;
import com.visuality.nq.samples.customlogger.CustomLogger;

public class CustomLoggerExample {
    String smbServer = "192.168.1.1";
    String user = "user";
    String password = "password";
    String domain = "domain";
    String share = "share";

    public CustomLoggerExample() throws NqException {
        CustomLogger customLogger = new CustomLogger();
        Config.jnq.set("LOGFILE", "jnq.log");
        Config.jnq.set("LOGTHRESHOLD", 2000);
        Config.jnq.set("LOGMAXRECORDSINFILE", 10000);
        Config.jnq.set("CAPTUREFILE", "jnq.pcap");
        Config.jnq.set("CAPTUREMAXRECORDSINFILE", 10000);
        TraceLog.set(customLogger);
        Config.jnq.set("LOGTOFILE", true);
        PasswordCredentials credentials = new PasswordCredentials(this.user, this.password, this.domain);
        Mount mount = new Mount(this.smbServer, this.share, credentials);
        mount.close();
        Client.stop();
    }

    public static void main(String[] args) throws NqException {
        new CustomLoggerExample();
    }
}

