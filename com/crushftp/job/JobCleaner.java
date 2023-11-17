/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.job;

import com.crushftp.client.Common;
import com.crushftp.client.VRL;
import java.util.Properties;

public class JobCleaner {
    public static Properties fix(Properties in) {
        String url = in.getProperty("url");
        try {
            if (url != null) {
                VRL vrl = new VRL(url);
                vrl.getProtocol().equalsIgnoreCase("FILE");
            }
        }
        catch (Exception e) {
            Common.log("SERVER", 0, e);
        }
        return in;
    }
}

