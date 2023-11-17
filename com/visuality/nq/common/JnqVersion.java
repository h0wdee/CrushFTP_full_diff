/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.JnqVersionInterface;
import java.lang.reflect.Method;

public class JnqVersion
implements JnqVersionInterface {
    private String version = "not available";
    private String buildTime = "not available";
    private String misc = "not available";

    public JnqVersion() {
        Class<?> jnqClass = null;
        try {
            jnqClass = Class.forName("com.visuality.nq.common.JnqVersionDerived");
        }
        catch (ClassNotFoundException e) {
            try {
                jnqClass = Class.forName("com.visuality.nq.common.JnqVersionBase");
            }
            catch (ClassNotFoundException e1) {
                // empty catch block
            }
        }
        if (null != jnqClass) {
            try {
                Object jnqClassInstance = jnqClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                Method getVersion = jnqClass.getMethod("getJnqVersion", new Class[0]);
                Method getBuildTime = jnqClass.getMethod("getJnqBuildTime", new Class[0]);
                Method getMisc = jnqClass.getMethod("getJnqMiscInfo", new Class[0]);
                this.version = (String)getVersion.invoke(jnqClassInstance, new Object[0]);
                this.buildTime = (String)getBuildTime.invoke(jnqClassInstance, new Object[0]);
                this.misc = (String)getMisc.invoke(jnqClassInstance, new Object[0]);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public String getJnqVersion() {
        return this.version;
    }

    public String getJnqBuildTime() {
        return this.buildTime;
    }

    public String getJnqMiscInfo() {
        return this.misc;
    }
}

