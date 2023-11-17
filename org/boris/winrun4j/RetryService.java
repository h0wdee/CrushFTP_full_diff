/*
 * Decompiled with CFR 0.152.
 */
package org.boris.winrun4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import org.boris.winrun4j.Environment;
import org.boris.winrun4j.INI;
import org.boris.winrun4j.Log;
import org.boris.winrun4j.Service;
import org.boris.winrun4j.ServiceException;

public class RetryService
implements Service {
    private volatile boolean init = false;
    private volatile boolean shutdown = false;
    private String[] cmd;
    private String[] env;
    private File startDir;
    private int maxRetries = 16;
    private int backoff = 1;
    private int backoffMultiplierSeconds = 1;
    private Random rand = new Random();

    public int serviceMain(String[] args) throws ServiceException {
        this.init();
        int result = 0;
        while (true) {
            result = this.exec();
            if (this.shutdown) {
                Log.warning("Shutting down due to request from service manager");
                break;
            }
            if (this.maxRetries <= 0) {
                Log.warning("Shutting down as max retry limit reached");
                break;
            }
            this.backoff();
        }
        return result;
    }

    private void backoff() {
        --this.maxRetries;
        int sleepSeconds = this.rand.nextInt(this.backoffMultiplierSeconds * (int)Math.pow(2.0, this.backoff++));
        Log.warning(String.format("Retry backoff: will sleep for %d seconds and then restart", sleepSeconds));
        try {
            Thread.sleep(sleepSeconds);
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    public int serviceRequest(int control) throws ServiceException {
        switch (control) {
            case 1: 
            case 5: {
                this.shutdown = true;
                break;
            }
        }
        return 0;
    }

    private synchronized void init() throws ServiceException {
        String backoffSecs;
        String maxR;
        String startDir;
        if (this.init) {
            return;
        }
        this.cmd = INI.getNumberedEntries("RetryService:cmd");
        String[] env = INI.getNumberedEntries("RetryService:env");
        boolean envAppend = Boolean.parseBoolean(INI.getProperty("RetryService:env.append", "false"));
        if (envAppend) {
            if (env != null && env.length > 0) {
                ArrayList<String> envs = new ArrayList<String>();
                Properties p = Environment.getEnvironmentVariables();
                for (String e : env) {
                    String[] kv = e.split("=");
                    if (kv.length != 2) continue;
                    p.setProperty(kv[0], kv[1]);
                }
                for (Object o : p.keySet()) {
                    envs.add(o + "=" + p.get(o));
                }
                this.env = envs.toArray(new String[envs.size()]);
            }
        } else if (env != null && env.length > 0) {
            this.env = env;
        }
        if ((startDir = INI.getProperty("RetryService:start.dir")) != null) {
            this.startDir = new File(startDir);
        }
        if ((maxR = INI.getProperty("RetryService:max.retries")) != null) {
            this.maxRetries = Integer.parseInt(maxR);
        }
        if ((backoffSecs = INI.getProperty("RetryService:backoff.seconds")) != null) {
            this.backoffMultiplierSeconds = Integer.parseInt(backoffSecs);
        }
        this.init = true;
    }

    private int exec() throws ServiceException {
        try {
            Process p = Runtime.getRuntime().exec(this.cmd, this.env, this.startDir);
            return p.waitFor();
        }
        catch (IOException e) {
            throw new ServiceException(e);
        }
        catch (InterruptedException e) {
            throw new ServiceException(e);
        }
    }
}

