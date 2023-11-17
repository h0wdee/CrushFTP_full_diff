/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.Vector;

public class CustomClient
extends GenericClient {
    public String client_class_str = "";
    Class c = null;
    Object c_o = null;
    static final String[] fields = new String[]{"username", "password", "*"};

    public CustomClient(String url, String header, Vector log) {
        super(header, log);
        this.url = url;
        this.client_class_str = new VRL(url).getProtocol().substring("custom.".length());
        try {
            try {
                this.c = Thread.currentThread().getContextClassLoader().loadClass(this.client_class_str);
            }
            catch (Throwable e) {
                this.c = new URLClassLoader(new URL[]{new File_S(String.valueOf(System.getProperty("crushftp.plugins")) + "plugins/lib/" + this.client_class_str + ".jar").toURI().toURL()}, Thread.currentThread().getContextClassLoader()).loadClass(this.client_class_str);
            }
            Constructor cons = this.c.getConstructor(String.class, String.class, Vector.class);
            this.c_o = cons.newInstance(url, header, log);
            this.setConfig(this.config);
        }
        catch (Exception e) {
            Common.log("SERVER", 0, e);
        }
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        this.config.put("username", username.trim());
        this.config.put("password", VRL.vrlDecode(password.trim()));
        Method wrapper = this.c_o.getClass().getMethod("login2", String.class, String.class, String.class);
        return (String)wrapper.invoke(this.c_o, username, password, clientid);
    }

    public void setConfig(Properties config) throws Exception {
        Method wrapper = this.c_o.getClass().getMethod("setConfig", Properties.class);
        wrapper.invoke(this.c_o, config);
    }

    @Override
    public void logout() throws Exception {
        Method wrapper = this.c_o.getClass().getMethod("logout", null);
        wrapper.invoke(this.c_o, null);
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        Method wrapper = this.c_o.getClass().getMethod("list", String.class, Vector.class);
        return (Vector)wrapper.invoke(this.c_o, path, list);
    }

    @Override
    public Properties stat(String path) throws Exception {
        Method wrapper = this.c_o.getClass().getMethod("stat", String.class);
        return (Properties)wrapper.invoke(this.c_o, path);
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        Method wrapper = this.c_o.getClass().getMethod("download3", String.class, Long.TYPE, Long.TYPE, Boolean.TYPE);
        return (InputStream)wrapper.invoke(this.c_o, path, new Long(startPos), new Long(endPos), new Boolean(binary));
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        Method wrapper = this.c_o.getClass().getMethod("upload3", String.class, Long.TYPE, Boolean.TYPE, Boolean.TYPE);
        return (OutputStream)wrapper.invoke(this.c_o, path, new Long(startPos), new Boolean(truncate), new Boolean(binary));
    }

    @Override
    public boolean delete(String path) throws Exception {
        Method wrapper = this.c_o.getClass().getMethod("delete", String.class);
        return (Boolean)wrapper.invoke(this.c_o, path);
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        Method wrapper = this.c_o.getClass().getMethod("makedirs", String.class);
        return (Boolean)wrapper.invoke(this.c_o, path);
    }

    @Override
    public boolean makedir(String path) throws Exception {
        Method wrapper = this.c_o.getClass().getMethod("makedir", String.class);
        return (Boolean)wrapper.invoke(this.c_o, path);
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        Method wrapper = this.c_o.getClass().getMethod("mdtm", String.class, Long.TYPE);
        return (Boolean)wrapper.invoke(this.c_o, path, new Long(modified));
    }

    @Override
    public boolean rename(String rnfr, String rnto) throws Exception {
        Method wrapper = this.c_o.getClass().getMethod("rename", String.class, String.class);
        return (Boolean)wrapper.invoke(this.c_o, rnfr, rnto);
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        Method wrapper = this.c_o.getClass().getMethod("rename", String.class, String.class);
        return (Boolean)wrapper.invoke(this.c_o, rnfr, rnto);
    }
}

