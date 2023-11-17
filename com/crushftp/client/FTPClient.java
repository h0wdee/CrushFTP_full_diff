/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.FTPClientSSLSessionProxy;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class FTPClient
extends GenericClient {
    InputStream in2 = null;
    OutputStream out2 = null;
    Socket sock = null;
    BufferedReader is = null;
    BufferedWriter os = null;
    static int curPort = -1;
    static Object activePortLock = new Object();
    SimpleDateFormat msdf = new SimpleDateFormat("MM-dd-yyyy hh:mmaa", Locale.US);
    SimpleDateFormat msdf2 = new SimpleDateFormat("MM-dd-yy hh:mm:ssaa", Locale.US);
    Vector recent_mkdirs = new Vector();
    SSLContext ssl_context = null;
    SSLSocketFactory ssl_factory = null;
    static Properties stat_cache = new Properties();
    FTPClientSSLSessionProxy fcssp = null;

    public FTPClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"proxy_item", "secure", "implicit", "secure_data", "username", "password", "clientid", "keystore_path", "trustore_path", "acceptAnyCert", "keystore_pass", "key_pass", "disabled_ciphers", "secure_mode", "account", "ccc", "allow_ccc_ssl_close", "cwd_list", "no_stat", "no_mkd", "no_mdtm", "ascii", "default_dir", "default_pwd", "server_type", "simple", "mfmt_ok", "no_os400", "stat_cache", "star_stat", "pasv", "*_script", "config_cwd_list", "listCommand", "permanently_windows", "7_token_proxy", "proxyActivePorts", "autoPasvIpSubstitution", "rfc_proxy", "error"};
        this.url = url;
        if (url.toUpperCase().startsWith("FTPES:")) {
            this.config.put("secure", "true");
        } else if (url.toUpperCase().startsWith("FTPS:")) {
            this.config.put("implicit", "true");
        }
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        String server_announce;
        block44: {
            Socket sockOriginal;
            String userMessage;
            block43: {
                this.fcssp = new FTPClientSSLSessionProxy(this.url, this.config);
                this.config.put("username", username);
                this.config.put("password", password);
                if (clientid != null) {
                    this.config.put("clientid", clientid);
                }
                userMessage = "";
                VRL u = new VRL(this.url);
                this.log("Connecting to:" + u.getHost() + ":" + u.getPort());
                this.sock = this.fcssp.getSock(u.getHost(), u.getPort());
                this.active();
                sockOriginal = this.sock;
                if (this.config.getProperty("implicit", "false").equals("true") || this.config.getProperty("secure", "false").equals("true") && !this.config.getProperty("implicit", "false").equals("true")) {
                    this.log(this.in + ":using SSL parameters:client cert keystore=" + this.config.getProperty("keystore_path") + ", truststore=" + this.config.getProperty("trustore_path", "builtin") + ", clientAuth=" + !this.config.getProperty("keystore_path", "").equals("") + ", trustAll=" + this.config.getProperty("acceptAnyCert", "true"));
                }
                if (this.config.getProperty("implicit", "false").equals("true")) {
                    if (this.ssl_context == null) {
                        this.ssl_context = new Common().getSSLContext(!this.config.getProperty("keystore_path", "").equals("") ? this.config.getProperty("keystore_path") : "builtin", this.config.getProperty("trustore_path", "builtin"), Common.encryptDecrypt(this.config.getProperty("keystore_pass"), false), Common.encryptDecrypt(this.config.getProperty("key_pass"), false), "TLS", !this.config.getProperty("keystore_path", "").equals(""), this.config.getProperty("acceptAnyCert", "true").equalsIgnoreCase("true"));
                    }
                    if (this.ssl_factory == null) {
                        this.ssl_factory = this.ssl_context.getSocketFactory();
                    }
                    SSLSocket ss = (SSLSocket)this.ssl_factory.createSocket(this.sock, this.sock.getInetAddress().getHostName(), this.sock.getPort(), true);
                    Common.configureSSLTLSSocket(ss, System.getProperty("crushftp.tls_version_client", "SSLv2Hello,TLSv1,TLSv1.1,TLSv1.2,TLSv1.3"));
                    Common.setEnabledCiphers(this.config.getProperty("disabled_ciphers", ""), ss, null);
                    ss.setUseClientMode(true);
                    ss.startHandshake();
                    this.sock = ss;
                }
                this.os = new BufferedWriter(new OutputStreamWriter(this.sock.getOutputStream(), "UTF8"));
                server_announce = this.send_data_raw(220, "", this.sock.getInputStream());
                if ((this.config.getProperty("secure", "false").equals("true") || u.getProtocol().equalsIgnoreCase("FTPES")) && !this.config.getProperty("implicit", "false").equals("true")) {
                    this.send_data_raw(234, "AUTH " + this.config.getProperty("secure_mode", "TLS").toUpperCase(), this.sock.getInputStream());
                    if (this.ssl_context == null) {
                        this.ssl_context = new Common().getSSLContext(!this.config.getProperty("keystore_path", "").equals("") ? this.config.getProperty("keystore_path") : "builtin", this.config.getProperty("trustore_path", "builtin"), Common.encryptDecrypt(this.config.getProperty("keystore_pass"), false), Common.encryptDecrypt(this.config.getProperty("key_pass"), false), "TLS", !this.config.getProperty("keystore_path", "").equals(""), this.config.getProperty("acceptAnyCert", "true").equalsIgnoreCase("true"));
                    }
                    if (this.ssl_factory == null) {
                        this.ssl_factory = this.ssl_context.getSocketFactory();
                    }
                    SSLSocket ss = (SSLSocket)this.ssl_factory.createSocket(this.sock, this.sock.getInetAddress().getHostName(), this.sock.getPort(), false);
                    Common.configureSSLTLSSocket(ss, System.getProperty("crushftp.tls_version_client", "SSLv2Hello,TLSv1,TLSv1.1,TLSv1.2,TLSv1.3"));
                    Common.setEnabledCiphers(this.config.getProperty("disabled_ciphers", ""), ss, null);
                    ss.startHandshake();
                    this.sock = ss;
                }
                this.os = new BufferedWriter(new OutputStreamWriter(this.sock.getOutputStream(), "UTF8"));
                this.executeScript(this.config.getProperty("before_login_script", ""), "");
                try {
                    String result = this.send_data_raw(331, "USER " + username, this.sock.getInputStream());
                    if (result != null && result.indexOf("230 ") >= 0) break block43;
                    if (result != null && result.startsWith("5")) {
                        throw new IOException(result);
                    }
                    try {
                        userMessage = this.send_data_raw(230, "PASS " + password, this.sock.getInputStream());
                    }
                    catch (Exception e2) {
                        if (e2.toString().indexOf("332 ") >= 0) {
                            userMessage = this.send_data_raw(230, "ACCT " + this.config.getProperty("account", ""), this.sock.getInputStream());
                            break block43;
                        }
                        throw e2;
                    }
                }
                catch (Exception e) {
                    try {
                        this.send_data_raw(221, "QUIT", this.sock.getInputStream());
                    }
                    catch (Exception e2) {
                        // empty catch block
                    }
                    if (this.is != null) {
                        this.is.close();
                    }
                    if (this.os != null) {
                        this.os.close();
                    }
                    if (this.sock != null) {
                        this.sock.close();
                    }
                    this.is = null;
                    this.os = null;
                    try {
                        this.fcssp.close();
                    }
                    catch (Exception eee) {
                        this.log("FTP_CLIENT", 1, eee);
                    }
                    throw e;
                }
            }
            if (this.config.getProperty("secure", "false").equals("true") || this.config.getProperty("implicit", "false").equals("true")) {
                this.send_data_raw(-1, "PBSZ 0", this.sock.getInputStream());
            }
            if ((this.config.getProperty("secure", "false").equals("true") || this.config.getProperty("implicit", "false").equals("true")) && this.config.getProperty("secure_data", "true").equals("true")) {
                this.send_data_raw(-1, "PROT P", this.sock.getInputStream());
            } else if ((this.config.getProperty("secure", "false").equals("true") || this.config.getProperty("implicit", "false").equals("true")) && this.config.getProperty("secure_data", "true").equals("false")) {
                this.send_data_raw(-1, "PROT C", this.sock.getInputStream());
            }
            if (this.config.getProperty("ccc", "false").equals("true")) {
                try {
                    this.send_data_raw(200, "CCC", this.sock.getInputStream());
                    if (this.config.getProperty("allow_ccc_ssl_close", "false").equals("true")) {
                        Thread t = new Thread(new Runnable(){

                            @Override
                            public void run() {
                                try {
                                    FTPClient.this.sock.close();
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                        });
                        t.start();
                        t.join(3000L);
                        if (t.isAlive()) {
                            t.interrupt();
                        }
                    }
                    this.is = new BufferedReader(new InputStreamReader(sockOriginal.getInputStream(), "UTF8"));
                    this.os = new BufferedWriter(new OutputStreamWriter(sockOriginal.getOutputStream(), "UTF8"));
                }
                catch (Exception t) {}
            } else {
                this.is = new BufferedReader(new InputStreamReader(this.sock.getInputStream(), "UTF8"));
            }
            if (this.config.getProperty("implicit", "false").equals("true")) {
                this.setConfig("secure", "true");
            }
            this.setConfig("userMessage", userMessage);
            try {
                this.send_data(-1, "PWD").toString();
                this.config.put("server_type", this.send_data(215, "SYST").toString().toUpperCase());
            }
            catch (Exception e) {
                this.log(e);
                this.config.put("server_type", "UNKNOWN:" + e);
                String data2 = this.send_data(-1, "SYST").toString();
                if (!data2.startsWith("25")) break block44;
                this.config.put("server_type", this.send_data(215, "").toString().toUpperCase());
            }
        }
        if (this.config.getProperty("server_type").indexOf("OS/400") >= 0) {
            this.send_data(-1, "SITE NAMEFMT 1").toString();
        }
        if (this.config.getProperty("server_type").indexOf("NONSTOP") >= 0) {
            this.config.put("cwd_list", "true");
            this.config.put("no_stat", "true");
            this.config.put("no_mkd", "true");
            this.config.put("no_mdtm", "true");
            this.config.put("ascii", "true");
        }
        if (this.config.getProperty("server_type").indexOf("TYPSOFT") >= 0 || server_announce.toUpperCase().indexOf("TYPSOFT") >= 0) {
            this.config.put("cwd_list", "true");
            this.config.put("no_stat", "true");
            this.config.put("no_mkd", "true");
            this.config.put("no_mdtm", "true");
        }
        if (this.config.getProperty("server_type").indexOf("WINDOW") >= 0) {
            this.config.put("no_stat", "true");
        }
        this.config.put("default_dir", "/");
        String pwdStr = this.send_data(-1, "PWD").toString();
        this.config.put("default_pwd", pwdStr);
        if (pwdStr != null && pwdStr.indexOf("\"") >= 0 && pwdStr.indexOf("\"") < pwdStr.lastIndexOf("\"")) {
            String defaultDir = pwdStr.substring(pwdStr.indexOf("\"") + 1);
            if (!(defaultDir = defaultDir.substring(0, defaultDir.indexOf("\""))).startsWith("/")) {
                defaultDir = "/" + defaultDir;
            }
            if (!defaultDir.endsWith("/")) {
                defaultDir = String.valueOf(defaultDir) + "/";
            }
            if (this.config.getProperty("server_type").indexOf("OS/400") >= 0) {
                defaultDir = "/";
            }
            this.config.put("default_dir", defaultDir);
        }
        this.executeScript(this.config.getProperty("after_login_script", ""), "");
        if (this.config.getProperty("simple", "false").equals("false") && this.config.getProperty("server_type").indexOf("OS/400") < 0) {
            this.send_data(-1, "OPTS UTF8 ON").toString();
        }
        this.active();
        return "";
    }

    @Override
    public void logout() throws Exception {
        block7: {
            try {
                this.close();
            }
            catch (Exception e) {
                if (this.in2 != null) {
                    this.in2.close();
                }
                if (this.out2 == null) break block7;
                this.out2.close();
            }
        }
        this.executeScript(this.config.getProperty("before_logout_script", ""), "");
        try {
            this.send_data(221, "QUIT");
        }
        catch (Exception e) {
            this.log("FTP_CLIENT", 1, e);
        }
        try {
            this.fcssp.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public boolean delete(String path) throws Exception {
        block19: {
            this.recent_mkdirs.removeAllElements();
            if (this.config.getProperty("simple", "false").equals("true") && path.indexOf("/") >= 0) {
                path = Common.last(path);
            }
            if (this.config.getProperty("server_type").indexOf("OS/400") >= 0 && this.config.getProperty("no_os400", "false").equalsIgnoreCase("false")) {
                new_path = "/";
                x = 0;
                while (x < path.split("\\/").length) {
                    s = path.split("\\/")[x].toUpperCase();
                    if (!s.equals("")) {
                        if (s.indexOf(".") >= 0 && x == 3 && s.indexOf(".FILE") < 0 && s.indexOf(".MBR") < 0 && s.indexOf(".LIB") < 0) {
                            s = String.valueOf(Common.replace_str(s, ".", ".FILE/")) + ".MBR";
                            new_path = String.valueOf(new_path) + s;
                        } else {
                            if (x < 3 && !s.toUpperCase().endsWith(".LIB")) {
                                s = String.valueOf(s) + ".LIB";
                            }
                            if (x == 3 && !s.toUpperCase().endsWith(".FILE")) {
                                s = String.valueOf(s) + ".FILE";
                            }
                            if (x == 4 && !s.toUpperCase().endsWith(".MBR")) {
                                s = String.valueOf(s) + ".MBR";
                            }
                            new_path = String.valueOf(new_path) + s;
                            if (x < path.split("\\/").length - 1) {
                                new_path = String.valueOf(new_path) + "/";
                            } else if (s.indexOf(".") >= 0) {
                                s = s.replace('.', '/');
                            }
                        }
                    }
                    ++x;
                }
                path = new_path;
            }
            try {
                this.send_data(250, "DELE " + path);
                break block19;
            }
            catch (Exception e1) {
                code = 550;
                ** while (code < 560)
            }
lbl-1000:
            // 1 sources

            {
                if (("" + e1).indexOf(String.valueOf(code)) >= 0) {
                    try {
                        this.send_data(250, "RMD " + path);
                        break;
                    }
                    catch (Exception e2) {
                        if (("" + e2).indexOf("not empty") >= 0) {
                            this.log("FTP_CLIENT", 1, e2);
                            return false;
                        }
                        throw e1;
                    }
                }
                Thread.sleep(500L);
                ++code;
                continue;
            }
        }
        return true;
    }

    @Override
    public boolean makedir(String path) throws Exception {
        if (this.config.getProperty("no_mkd", "false").equals("true") || this.config.getProperty("simple", "false").equals("true")) {
            return true;
        }
        if (this.config.getProperty("simple", "false").equals("true") && path.indexOf("/") >= 0) {
            path = Common.last(path);
        }
        try {
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            this.send_data(257, "MKD " + path);
        }
        catch (IOException e) {
            if ((e.toString().indexOf("550") >= 0 || e.toString().indexOf("521") >= 0) && e.toString().toLowerCase().indexOf("exists") >= 0) {
                return true;
            }
            throw e;
        }
        return true;
    }

    public String quote(String command) throws Exception {
        return this.send_data(-1, command.trim());
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public boolean makedirs(String path) throws Exception {
        block8: {
            ok = false;
            try {
                ok = this.makedir(path);
                break block8;
            }
            catch (Exception e) {
                this.log("FTP_CLIENT", 1, "MKDIR recursive failed:" + path + " so we will try recursive. (" + e + ")");
                parts = path.split("/");
                path2 = "";
                x = 0;
                ** while (x < parts.length)
            }
lbl-1000:
            // 1 sources

            {
                path2 = String.valueOf(path2) + parts[x] + "/";
                if (x >= 1 && this.recent_mkdirs.indexOf(path2) < 0) {
                    this.recent_mkdirs.addElement(path2);
                    if (this.stat(path2) == null) {
                        try {
                            ok = this.makedir(path2);
                        }
                        catch (Exception ee) {
                            this.log("FTP_CLIENT", 1, "MKDIR individual:" + path2 + " failed, moving to next. (" + ee + ")");
                        }
                    } else {
                        ok = true;
                    }
                }
                ++x;
                continue;
            }
        }
        return ok;
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        if (this.config.getProperty("no_mdtm", "false").equals("true") || this.config.getProperty("simple", "false").equals("true")) {
            return false;
        }
        boolean ok = this.config.getProperty("mfmt_ok", "true").equalsIgnoreCase("true");
        SimpleDateFormat sdf_yyyyMMddHHmmss2 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        try {
            if (ok) {
                this.send_data(213, "MFMT " + sdf_yyyyMMddHHmmss2.format(new Date(modified)) + " " + path);
                ok = true;
            }
        }
        catch (Exception e) {
            ok = false;
            this.setConfig("mfmt_ok", "false");
        }
        if (!ok) {
            this.send_data(213, "MDTM " + sdf_yyyyMMddHHmmss2.format(new Date(modified)) + " " + path);
        }
        return true;
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        this.recent_mkdirs.removeAllElements();
        if (this.config.getProperty("simple", "false").equals("true") && rnfr.indexOf("/") >= 0) {
            rnfr = Common.last(rnfr);
        }
        if (this.config.getProperty("simple", "false").equals("true") && rnto.indexOf("/") >= 0) {
            rnto = Common.last(rnto);
        }
        this.send_data(350, "RNFR " + rnfr);
        this.send_data(250, "RNTO " + rnto);
        return true;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    @Override
    public Properties stat(String path) throws Exception {
        Vector list;
        Properties dir_item;
        block51: {
            if (this.config.getProperty("server_type").indexOf("OS/400") >= 0 && this.config.getProperty("no_os400", "false").equalsIgnoreCase("false")) {
                String new_path = "/";
                int x = 0;
                while (x < path.split("\\/").length) {
                    String s = path.split("\\/")[x];
                    if (!s.equals("")) {
                        if (s.indexOf(".") >= 0 && x == 3 && s.indexOf(".FILE") < 0 && s.indexOf(".MBR") < 0 && s.indexOf(".LIB") < 0) {
                            s = String.valueOf(Common.replace_str(s, ".", ".FILE/")) + ".MBR";
                            new_path = String.valueOf(new_path) + s;
                        } else {
                            if (x < 3 && !s.toUpperCase().endsWith(".LIB")) {
                                s = String.valueOf(s) + ".LIB";
                            }
                            if (x == 3 && !s.toUpperCase().endsWith(".FILE")) {
                                s = String.valueOf(s) + ".FILE";
                            }
                            if (x == 4 && !s.toUpperCase().endsWith(".MBR")) {
                                s = String.valueOf(s) + ".MBR";
                            }
                            new_path = String.valueOf(new_path) + s;
                            if (x < path.split("\\/").length - 1) {
                                new_path = String.valueOf(new_path) + "/";
                            }
                        }
                    }
                    ++x;
                }
                path = new_path;
                boolean file = !Common.last(path).toUpperCase().endsWith(".LIB") && !Common.last(path).toUpperCase().endsWith(".LIB/");
                Properties dir_item2 = new Properties();
                dir_item2.put("name", Common.last(path));
                dir_item2.put("size", file ? "0" : "1");
                dir_item2.put("modified", "0");
                dir_item2.put("type", file ? "FILE" : "DIR");
                dir_item2.put("root_dir", "/");
                dir_item2.put("url", String.valueOf(this.url) + path.substring(1) + (file ? "" : "/"));
                return dir_item2;
            }
            if (this.config.getProperty("simple", "false").equals("true")) {
                boolean file = Common.last(path).indexOf(".") > 0;
                Properties dir_item3 = new Properties();
                dir_item3.put("name", Common.last(path));
                dir_item3.put("size", file ? "0" : "1");
                dir_item3.put("modified", "0");
                dir_item3.put("type", file ? "FILE" : "DIR");
                dir_item3.put("root_dir", "/");
                dir_item3.put("url", String.valueOf(this.url) + path.substring(1) + (file ? "" : "/"));
                return dir_item3;
            }
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            dir_item = null;
            list = new Vector();
            String searchPath = Common.all_but_last(path);
            if (searchPath.equals("")) {
                searchPath = "/";
            }
            try {
                String result = "";
                if (this.config.getProperty("stat_cache", "false").equals("true") && stat_cache.containsKey(String.valueOf(this.url) + path.substring(1))) {
                    return (Properties)stat_cache.get(String.valueOf(this.url) + path.substring(1));
                }
                if (this.config.getProperty("star_stat", "false").equals("true")) {
                    result = this.send_data_now("STAT " + path + "*", true);
                } else if (this.config.getProperty("no_stat", "false").equals("false")) {
                    result = this.send_data_now("STAT " + path, true);
                }
                if (result.startsWith("5") && result.toUpperCase().indexOf("RECOGNIZE") >= 0) {
                    result = "";
                    this.config.put("no_stat", "true");
                }
                if (result.toUpperCase().startsWith("211-STATUS FOR USER")) {
                    result = "";
                    this.config.put("no_stat", "true");
                }
                BufferedReader br = new BufferedReader(new StringReader(result));
                String data = "";
                Vector list2 = new Vector();
                int line_num = 0;
                SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss");
                while ((data = br.readLine()) != null) {
                    if (data.startsWith("2") && ++line_num == 1 || data.trim().equals("")) continue;
                    if (data.startsWith("550")) {
                        if (data.toUpperCase().indexOf("RECOGNIZE") >= 0) {
                            this.config.put("no_stat", "true");
                            continue;
                        }
                        if (System.getProperty("crushftp.isTestCall" + Thread.currentThread().getId(), "false").equals("true")) {
                            throw new Exception("Item not found..." + path);
                        }
                        return null;
                    }
                    Properties dir_item2 = new Properties();
                    dir_item2.put("root_dir", Common.all_but_last(path));
                    dir_item2.put("protocol", "ftp");
                    if (data.startsWith("211-")) {
                        data = data.substring("211-".length());
                    }
                    if (data.toUpperCase().indexOf("TYPE=") >= 0) {
                        dir_item2.put("size", "0");
                        dir_item2.put("num_items", "1");
                        dir_item2.put("owner", "owner");
                        dir_item2.put("group", "group");
                        dir_item2.put("proxy_item", "true");
                        String[] parts = data.split(";");
                        int x = 0;
                        while (x < parts.length - 1) {
                            if (parts[x].split("=")[0].trim().equalsIgnoreCase("Type")) {
                                if (parts[x].split("=")[1].trim().equalsIgnoreCase("dir")) {
                                    dir_item2.put("type", "DIR");
                                } else {
                                    dir_item2.put("type", "FILE");
                                }
                            } else if (parts[x].split("=")[0].trim().equalsIgnoreCase("UNIX.mode")) {
                                String r = dir_item2.getProperty("type", "").equalsIgnoreCase("DIR") ? "d" : "-";
                                r = String.valueOf(r) + this.numToStrPrivs(parts[x].split("=")[1].trim());
                                dir_item2.put("permissions", r);
                            } else if (parts[x].split("=")[0].trim().equalsIgnoreCase("Modify")) {
                                Date d = yyyyMMddHHmmss.parse(parts[x].split("=")[1].trim());
                                dir_item2.put("month", this.mmm.format(d));
                                dir_item2.put("day", this.dd.format(d));
                                dir_item2.put("time_or_year", this.yyyy.format(d));
                                dir_item2.put("modified", String.valueOf(d.getTime()));
                            } else if (parts[x].split("=")[0].trim().equalsIgnoreCase("Size")) {
                                dir_item2.put("size", parts[x].split("=")[1].trim());
                            } else if (parts[x].split("=")[0].trim().equalsIgnoreCase("UNIX.Owner")) {
                                dir_item2.put("owner", parts[x].split("=")[1].trim());
                            } else if (parts[x].split("=")[0].trim().equalsIgnoreCase("UNIX.Group")) {
                                dir_item2.put("group", parts[x].split("=")[1].trim());
                            }
                            ++x;
                        }
                        dir_item2.put("name", parts[parts.length - 1].substring(1));
                        if (dir_item2.getProperty("name").startsWith("/")) {
                            dir_item2.put("name", parts[parts.length - 1].substring(2));
                        }
                    } else {
                        this.parse_unix_line(dir_item2, data, 7, Common.all_but_last(path), list2);
                    }
                    dir_item2.put("url", String.valueOf(this.url) + path.substring(1));
                    if (this.config.getProperty("no_os400", "false").equalsIgnoreCase("true")) {
                        dir_item2.put("no_os400", "true");
                    }
                    this.log("FTP_CLIENT", 2, "path=" + path + "  data:" + data + "   dir_item2:" + dir_item2);
                    if (!dir_item2.getProperty("name").equals(Common.last(path))) continue;
                    list.addAll(list2);
                }
                if (list2.size() == 1) {
                    list.addAll(list2);
                } else if (list.size() == 0 && this.config.getProperty("star_stat", "false").equals("false")) {
                    this.list(Common.all_but_last(path), list);
                }
            }
            catch (Exception e) {
                this.log("FTP_CLIENT", 2, e);
                if (!System.getProperty("crushftp.isTestCall", "false").equals("true")) break block51;
                throw e;
            }
        }
        if (dir_item == null) {
            int x = 0;
            while (x < list.size()) {
                Properties p = (Properties)list.elementAt(x);
                if (p.getProperty("name", "").toUpperCase().equals(Common.last(path).toUpperCase())) {
                    dir_item = p;
                    break;
                }
                ++x;
            }
        }
        this.addModifiedItem(dir_item);
        if (dir_item != null && dir_item.getProperty("type").equals("DIR") && !dir_item.getProperty("url").endsWith("/")) {
            dir_item.put("url", String.valueOf(dir_item.getProperty("url", "")) + "/");
        }
        if (dir_item != null) {
            dir_item.put("pasv", this.config.getProperty("pasv", "true"));
        }
        return dir_item;
    }

    private String numToStrPrivs(String s) {
        String r = "";
        if (s.length() == 4) {
            s = s.substring(1);
        }
        int loop = 0;
        while (loop < 3) {
            if (s.charAt(loop) == '0') {
                r = String.valueOf(r) + "---";
            } else if (s.charAt(loop) == '1') {
                r = String.valueOf(r) + "--x";
            } else if (s.charAt(loop) == '2') {
                r = String.valueOf(r) + "-w-";
            } else if (s.charAt(loop) == '3') {
                r = String.valueOf(r) + "-wx";
            } else if (s.charAt(loop) == '4') {
                r = String.valueOf(r) + "r--";
            } else if (s.charAt(loop) == '5') {
                r = String.valueOf(r) + "r-x";
            } else if (s.charAt(loop) == '6') {
                r = String.valueOf(r) + "rw-";
            } else if (s.charAt(loop) == '7') {
                r = String.valueOf(r) + "rwx";
            }
            ++loop;
        }
        return r;
    }

    @Override
    protected InputStream download3(String path1, long startPos, long endPos, boolean binary) throws Exception {
        if (this.config.getProperty("simple", "false").equals("true") && path1.indexOf("/") >= 0) {
            path1 = Common.last(path1);
        }
        if (this.config.getProperty("server_type").indexOf("OS/400") >= 0 && this.config.getProperty("no_os400", "false").equalsIgnoreCase("false")) {
            String new_path = "/";
            int x = 0;
            while (x < path1.split("\\/").length) {
                String s = path1.split("\\/")[x].toUpperCase();
                if (!s.equals("")) {
                    if (s.indexOf(".") >= 0 && x == 3 && s.indexOf(".FILE") < 0 && s.indexOf(".MBR") < 0 && s.indexOf(".LIB") < 0) {
                        s = String.valueOf(Common.replace_str(s, ".", ".FILE/")) + ".MBR";
                        new_path = String.valueOf(new_path) + s;
                    } else {
                        if (x < 3 && !s.toUpperCase().endsWith(".LIB")) {
                            s = String.valueOf(s) + ".LIB";
                        }
                        if (x == 3 && !s.toUpperCase().endsWith(".FILE")) {
                            s = String.valueOf(s) + ".FILE";
                        }
                        if (x == 4 && !s.toUpperCase().endsWith(".MBR")) {
                            s = String.valueOf(s) + ".MBR";
                        }
                        new_path = String.valueOf(new_path) + s;
                        if (x < path1.split("\\/").length - 1) {
                            new_path = String.valueOf(new_path) + "/";
                        } else if (s.indexOf(".") >= 0) {
                            s = s.replace('.', '/');
                        }
                    }
                }
                ++x;
            }
            path1 = new_path;
        }
        String path2 = path1;
        this.executeScript(this.config.getProperty("before_download_script", ""), path2.trim());
        this.send_data(-1, "TYPE " + (binary && this.config.getProperty("ascii", "false").equals("false") ? "I" : "A"));
        if (startPos > 0L) {
            this.send_data(-1, "REST " + startPos);
        }
        Socket transfer_socket = null;
        if (this.config.getProperty("pasv", "true").equals("true")) {
            transfer_socket = (Socket)this.getTransferSocket(true);
            this.send_data(150, "RETR " + path2.trim());
            if (this.config.getProperty("secure", "false").equals("true") && this.config.getProperty("secure_data", "true").equals("true")) {
                ((SSLSocket)transfer_socket).setUseClientMode(true);
            }
        } else {
            ServerSocket ss = (ServerSocket)this.getTransferSocket(false);
            this.send_data(150, "RETR " + path2.trim());
            ss.setSoTimeout(Integer.parseInt(System.getProperty("crushftp.ftpcommand.timeout3", "120")) * 1000);
            transfer_socket = ss.accept();
            ss.close();
            if (this.config.getProperty("secure", "false").equals("true") && this.config.getProperty("secure_data", "true").equals("true")) {
                ((SSLSocket)transfer_socket).setUseClientMode(true);
            }
            if (this.config.getProperty("secure", "false").equals("true") && this.config.getProperty("secure_data", "true").equals("true")) {
                ((SSLSocket)transfer_socket).startHandshake();
            }
        }
        Properties byteCount = new Properties();
        this.setupTimeout(byteCount, transfer_socket);
        this.in2 = transfer_socket.getInputStream();
        class InputWrapper
        extends InputStream {
            InputStream in3 = null;
            Socket transfer_socket = null;
            boolean closed = false;
            long pos;
            private final /* synthetic */ long val$endPos;
            private final /* synthetic */ Properties val$byteCount;
            private final /* synthetic */ String val$path2;

            public InputWrapper(InputStream in3, Socket transfer_socket, long l, long l2, Properties properties, String string) {
                this.val$endPos = l2;
                this.val$byteCount = properties;
                this.val$path2 = string;
                this.pos = l;
                this.in3 = in3;
                this.transfer_socket = transfer_socket;
            }

            @Override
            public int read() throws IOException {
                FTPClient.this.active();
                if (this.pos == this.val$endPos && this.val$endPos >= 0L) {
                    return -1;
                }
                int i = this.in3.read();
                ++this.pos;
                this.val$byteCount.put("b", String.valueOf(Long.parseLong(this.val$byteCount.getProperty("b", "0")) + 1L));
                this.val$byteCount.put("t", String.valueOf(System.currentTimeMillis()));
                return i;
            }

            @Override
            public int read(byte[] b) throws IOException {
                return this.read(b, 0, b.length);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                FTPClient.this.active();
                if (this.pos >= this.val$endPos && this.val$endPos >= 0L) {
                    return -1;
                }
                int i = this.in3.read(b, off, len);
                if (i > 0) {
                    this.pos += (long)i;
                }
                if (this.val$endPos > 0L && this.pos > this.val$endPos) {
                    i = (int)((long)i - (this.pos - this.val$endPos));
                }
                this.val$byteCount.put("b", String.valueOf(Long.parseLong(this.val$byteCount.getProperty("b", "0")) + (long)i));
                this.val$byteCount.put("t", String.valueOf(System.currentTimeMillis()));
                return i;
            }

            @Override
            public void close() throws IOException {
                FTPClient.this.active();
                this.val$byteCount.put("status", "DONE");
                this.val$byteCount.put("t", String.valueOf(System.currentTimeMillis()));
                if (this.closed) {
                    return;
                }
                this.in3.close();
                FTPClient.this.send_data(226, "");
                this.closed = true;
                FTPClient.this.executeScript(FTPClient.this.config.getProperty("after_download_script", ""), this.val$path2.trim());
                this.transfer_socket.close();
                ftp_client_sockets.remove(this.val$byteCount);
            }
        }
        this.in = new InputWrapper(this.in2, transfer_socket, startPos, endPos, byteCount, path2);
        return this.in;
    }

    @Override
    protected OutputStream upload3(String path1, long startPos, boolean truncate, boolean binary) throws Exception {
        if (this.config.getProperty("simple", "false").equals("true") && path1.indexOf("/") >= 0) {
            path1 = Common.last(path1);
        }
        if (this.config.getProperty("server_type").indexOf("OS/400") >= 0 && this.config.getProperty("no_os400", "false").equalsIgnoreCase("false")) {
            String new_path = "/";
            int x = 0;
            while (x < path1.split("\\/").length) {
                String s = path1.split("\\/")[x];
                if (!s.equals("")) {
                    if (x < 3 && !s.toUpperCase().endsWith(".LIB")) {
                        s = String.valueOf(s) + ".LIB";
                    }
                    if (x == 3 && !s.toUpperCase().endsWith(".FILE")) {
                        s = String.valueOf(s) + ".FILE";
                    }
                    if (x == 4 && !s.toUpperCase().endsWith(".MBR")) {
                        s = Common.replace_str(s, ".FILE", "");
                        s = String.valueOf(s) + ".MBR";
                    }
                    new_path = String.valueOf(new_path) + s;
                    if (x < path1.split("\\/").length - 1) {
                        new_path = String.valueOf(new_path) + "/";
                    }
                }
                ++x;
            }
            path1 = new_path.toUpperCase();
        }
        String path2 = path1;
        this.executeScript(this.config.getProperty("before_upload_script", ""), path2.trim());
        this.send_data(-1, "TYPE " + (binary && this.config.getProperty("ascii", "false").equals("false") ? "I" : "A"));
        String stor_command = "STOR";
        if (startPos > 0L) {
            this.send_data(-1, "REST " + startPos);
        }
        if (startPos < -1L) {
            stor_command = "APPE";
        }
        Socket transfer_socket = null;
        if (this.config.getProperty("pasv", "true").equals("true")) {
            transfer_socket = (Socket)this.getTransferSocket(true);
            String response = this.send_data(150, String.valueOf(stor_command) + " " + path2.trim());
            this.config.put("upload_server_response", String.valueOf(this.config.getProperty("upload_server_response", "")) + " " + response);
            if (this.config.getProperty("secure", "false").equals("true") && this.config.getProperty("secure_data", "true").equals("true")) {
                ((SSLSocket)transfer_socket).setUseClientMode(true);
            }
        } else {
            ServerSocket ss = (ServerSocket)this.getTransferSocket(false);
            String response = this.send_data(150, String.valueOf(stor_command) + " " + path2.trim());
            this.config.put("upload_server_response", String.valueOf(this.config.getProperty("upload_server_response", "")) + " " + response);
            ss.setSoTimeout(Integer.parseInt(System.getProperty("crushftp.ftpcommand.timeout3", "120")) * 1000);
            transfer_socket = ss.accept();
            ss.close();
            if (this.config.getProperty("secure", "false").equals("true") && this.config.getProperty("secure_data", "true").equals("true")) {
                ((SSLSocket)transfer_socket).setUseClientMode(true);
            }
            if (this.config.getProperty("secure", "false").equals("true") && this.config.getProperty("secure_data", "true").equals("true")) {
                ((SSLSocket)transfer_socket).startHandshake();
            }
        }
        Properties byteCount = new Properties();
        this.setupTimeout(byteCount, transfer_socket);
        this.out2 = transfer_socket.getOutputStream();
        class OutputWrapper
        extends OutputStream {
            OutputStream out3 = null;
            boolean closed = false;
            Socket transfer_socket = null;
            boolean sent = false;
            private final /* synthetic */ Properties val$byteCount;
            private final /* synthetic */ String val$path2;

            public OutputWrapper(OutputStream out3, Socket transfer_socket, Properties properties, String string) {
                this.val$byteCount = properties;
                this.val$path2 = string;
                this.out3 = out3;
                this.transfer_socket = transfer_socket;
            }

            @Override
            public void write(int i) throws IOException {
                FTPClient.this.active();
                this.sent = true;
                this.out3.write(i);
                this.val$byteCount.put("b", String.valueOf(Long.parseLong(this.val$byteCount.getProperty("b", "0")) + 1L));
                this.val$byteCount.put("t", String.valueOf(System.currentTimeMillis()));
            }

            @Override
            public void write(byte[] b) throws IOException {
                this.write(b, 0, b.length);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                FTPClient.this.active();
                if (len > 0) {
                    this.sent = true;
                }
                this.out3.write(b, off, len);
                this.val$byteCount.put("b", String.valueOf(Long.parseLong(this.val$byteCount.getProperty("b", "0")) + (long)len));
                this.val$byteCount.put("t", String.valueOf(System.currentTimeMillis()));
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void close() throws IOException {
                FTPClient.this.active();
                this.val$byteCount.put("status", "DONE");
                this.val$byteCount.put("t", String.valueOf(System.currentTimeMillis()));
                if (this.closed) {
                    return;
                }
                if (this.transfer_socket instanceof SSLSocket) {
                    if (!this.sent) {
                        ((SSLSocket)this.transfer_socket).startHandshake();
                    }
                    ((SSLSocket)this.transfer_socket).shutdownOutput();
                }
                this.out3.close();
                this.closed = true;
                FTPClient.this.send_data(226, "");
                FTPClient.this.executeScript(FTPClient.this.config.getProperty("after_upload_script", ""), this.val$path2.trim());
                this.transfer_socket.close();
                Vector vector = ftp_client_sockets;
                synchronized (vector) {
                    ftp_client_sockets.remove(this.val$byteCount);
                }
            }
        }
        this.out = new OutputWrapper(this.out2, transfer_socket, byteCount, path2);
        return this.out;
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        String search = "";
        if (path.indexOf(":") >= 0) {
            search = " " + path.split(":")[1];
            path = path.split(":")[0];
        }
        if (path.equals("") || path.equals(".")) {
            path = "";
        }
        this.executeScript(this.config.getProperty("before_dir_script", ""), path.trim());
        this.send_data(-1, "TYPE A");
        Socket transfer_socket = null;
        String listCommand = "LIST";
        if (this.config.getProperty("simple", "false").equals("true")) {
            listCommand = "NLST";
            path = "";
        }
        try {
            String s;
            int x;
            String new_path;
            if (!this.config.getProperty("listCommand", "").equals("")) {
                listCommand = this.config.getProperty("listCommand", "");
            }
            if (this.config.getProperty("pasv", "true").equals("true")) {
                if (this.config.getProperty("cwd_list", "false").equals("true") || this.config.getProperty("config_cwd_list", "false").equals("true")) {
                    if (this.config.getProperty("server_type").indexOf("OS/400") >= 0 && this.config.getProperty("no_os400", "false").equalsIgnoreCase("false")) {
                        new_path = "/";
                        x = 0;
                        while (x < path.split("\\/").length) {
                            s = path.split("\\/")[x];
                            if (!s.equals("")) {
                                if (!s.toUpperCase().endsWith(".LIB")) {
                                    s = String.valueOf(s) + ".LIB";
                                }
                                new_path = String.valueOf(new_path) + s + "/";
                            }
                            ++x;
                        }
                        this.send_data(250, ("CWD " + new_path).trim());
                    } else {
                        this.send_data(250, ("CWD " + path).trim());
                    }
                    transfer_socket = (Socket)this.getTransferSocket(true);
                    this.send_data(150, String.valueOf(listCommand) + search);
                } else {
                    transfer_socket = (Socket)this.getTransferSocket(true);
                    if (!search.equals("")) {
                        search = search.substring(1);
                    }
                    this.send_data(150, (String.valueOf(listCommand) + " " + path + search).trim());
                }
                if (this.config.getProperty("secure", "false").equals("true") && this.config.getProperty("secure_data", "true").equals("true")) {
                    ((SSLSocket)transfer_socket).setUseClientMode(true);
                }
            } else {
                ServerSocket ss;
                if (this.config.getProperty("cwd_list", "false").equals("true") || this.config.getProperty("config_cwd_list", "false").equals("true")) {
                    if (this.config.getProperty("server_type").indexOf("OS/400") >= 0 && this.config.getProperty("no_os400", "false").equalsIgnoreCase("false")) {
                        new_path = "/";
                        x = 0;
                        while (x < path.split("\\/").length) {
                            s = path.split("\\/")[x];
                            if (!s.equals("")) {
                                if (!s.toUpperCase().endsWith(".LIB")) {
                                    s = String.valueOf(s) + ".LIB";
                                }
                                new_path = String.valueOf(new_path) + s + "/";
                            }
                            ++x;
                        }
                        this.send_data(250, ("CWD " + new_path).trim());
                    } else {
                        this.send_data(250, ("CWD " + path).trim());
                    }
                    ss = (ServerSocket)this.getTransferSocket(false);
                    this.send_data(150, String.valueOf(listCommand) + search);
                    ss.setSoTimeout(Integer.parseInt(System.getProperty("crushftp.ftpcommand.timeout2", "60")) * 1000);
                    transfer_socket = ss.accept();
                    ss.close();
                } else {
                    ss = (ServerSocket)this.getTransferSocket(false);
                    this.send_data(150, (String.valueOf(listCommand) + search + " " + path).trim());
                    ss.setSoTimeout(Integer.parseInt(System.getProperty("crushftp.ftpcommand.timeout2", "60")) * 1000);
                    transfer_socket = ss.accept();
                    ss.close();
                }
                if (this.config.getProperty("secure", "false").equals("true") && this.config.getProperty("secure_data", "true").equals("true")) {
                    ((SSLSocket)transfer_socket).setUseClientMode(true);
                }
                if (this.config.getProperty("secure", "false").equals("true") && this.config.getProperty("secure_data", "true").equals("true")) {
                    ((SSLSocket)transfer_socket).startHandshake();
                }
            }
        }
        catch (Exception e) {
            if (this.config.getProperty("server_type").indexOf("Z/OS") >= 0 && ("" + e).indexOf("UX-Path change not allowed") >= 0) {
                this.log("Ignoring z/OS error for invalid directory:" + e);
                return list;
            }
            throw e;
        }
        BufferedReader is_list = new BufferedReader(new InputStreamReader(transfer_socket.getInputStream(), "UTF8"));
        String data = "";
        int loops = 0;
        int max_list = Integer.parseInt(System.getProperty("crushftp.proxy.list.max", "0"));
        while (data != null) {
            this.active();
            ++loops;
            data = is_list.readLine();
            if (max_list > 0 && list.size() >= max_list || data == null || data.trim().length() <= 0 || data.toUpperCase().startsWith("TOTAL")) continue;
            if (System.getProperty("crushftp.ftpclient.list.log", "true").equals("true")) {
                this.log(this.is + ":" + data);
            }
            try {
                StringTokenizer get_em;
                Properties dir_item = new Properties();
                dir_item.put("root_dir", path);
                dir_item.put("protocol", "ftp");
                int tokenCount = 0;
                StringTokenizer get_em2 = new StringTokenizer(data, " ");
                while (get_em2.hasMoreElements()) {
                    String data2 = get_em2.nextElement().toString();
                    if (++tokenCount != 2 || !data2.trim().endsWith("AM") && !data2.trim().endsWith("PM")) continue;
                    this.config.put("permanently_windows", "true");
                    this.config.put("no_stat", "true");
                }
                if (data.indexOf("<DIR>") >= 0) {
                    this.config.put("permanently_windows", "true");
                    this.config.put("no_stat", "true");
                }
                if (this.config.getProperty("permanently_windows", "false").equals("false")) {
                    if (this.config.getProperty("server_type", "").indexOf("WINDOWS") >= 0 && data.indexOf("owner    group") >= 0) {
                        this.config.put("server_type", "UNIX");
                    }
                    if (this.config.getProperty("server_type", "").indexOf("WINDOWS") >= 0 && data.indexOf("          generic  ") >= 0) {
                        this.config.put("server_type", "UNIX");
                    }
                    if (this.config.getProperty("server_type", "").indexOf("WINDOWS") >= 0 && data.indexOf(" ") == 9) {
                        this.config.put("server_type", "UNIX");
                    }
                    if (this.config.getProperty("server_type", "").indexOf("WINDOWS") >= 0 && tokenCount >= 9) {
                        this.config.put("server_type", "UNIX");
                    }
                }
                if (listCommand.equals("NLST")) {
                    dir_item.put("permissions", "-rwxrwxrwx");
                    dir_item.put("type", "FILE");
                    dir_item.put("name", data.trim());
                    if (data.endsWith("/") || data.endsWith("\\")) {
                        dir_item.put("type", "DIR");
                        dir_item.put("permissions", "drwxrwxrwx");
                        dir_item.put("name", data.trim().substring(0, data.trim().length() - 1));
                    }
                    dir_item.put("owner", "user");
                    dir_item.put("group", "group");
                    dir_item.put("time_or_year", "00:00");
                    dir_item.put("modified", "0");
                    dir_item.put("link", "false");
                    dir_item.put("num_items", "1");
                    dir_item.put("is_virtual", "true");
                    dir_item.put("protocol", "FTP");
                    dir_item.put("day", "1");
                    dir_item.put("num_items", "1");
                    dir_item.put("month", "Jan");
                    dir_item.put("size", "0");
                    if (!dir_item.getProperty("name").equals(".") && !dir_item.getProperty("name").equals("..")) {
                        this.addModifiedItem(dir_item);
                        list.addElement(dir_item);
                    }
                } else if (this.config.getProperty("server_type", "").indexOf("NETWARE") >= 0) {
                    if (!data.toUpperCase().startsWith("TOTAL ")) {
                        if (data.toUpperCase().startsWith("D")) {
                            dir_item.put("type", "DIR");
                        } else {
                            dir_item.put("type", "FILE");
                        }
                        get_em = new StringTokenizer(data, " ");
                        get_em.nextToken();
                        get_em.nextToken();
                        if (dir_item.getProperty("type").equals("DIR")) {
                            dir_item.put("permissions", "drwxrwxrwx");
                        } else {
                            dir_item.put("permissions", "-rwxrwxrwx");
                        }
                        dir_item.put("num_items", "1");
                        String the_owner = get_em.nextToken().trim();
                        dir_item.put("owner", the_owner);
                        dir_item.put("group", the_owner);
                        dir_item.put("size", get_em.nextToken().trim());
                        dir_item.put("month", get_em.nextToken().trim());
                        dir_item.put("day", get_em.nextToken().trim());
                        dir_item.put("time_or_year", get_em.nextToken().trim());
                        String name_data = get_em.nextToken();
                        name_data = data.substring(data.indexOf(name_data));
                        dir_item.put("name", name_data);
                        dir_item.put("dir", path);
                        if (!dir_item.getProperty("name").equals(".") && !dir_item.getProperty("name").equals("..")) {
                            this.addModifiedItem(dir_item);
                            list.addElement(dir_item);
                        }
                    }
                } else if (this.config.getProperty("server_type", "").indexOf("MACOS") >= 0) {
                    if (!data.toUpperCase().startsWith("TOTAL ")) {
                        if (data.toUpperCase().startsWith("D")) {
                            dir_item.put("type", "DIR");
                        } else {
                            dir_item.put("type", "FILE");
                        }
                        get_em = new StringTokenizer(data, " ");
                        dir_item.put("permissions", get_em.nextToken().trim());
                        dir_item.put("owner", get_em.nextToken().trim());
                        if (dir_item.getProperty("owner").toUpperCase().equals("FOLDER")) {
                            dir_item.put("num_items", get_em.nextToken().trim());
                            dir_item.put("group", dir_item.getProperty("owner"));
                            dir_item.put("size", "0");
                        } else {
                            dir_item.put("num_items", "0");
                            dir_item.put("group", get_em.nextToken().trim());
                            dir_item.put("size", get_em.nextToken().trim());
                        }
                        dir_item.put("month", get_em.nextToken().trim());
                        dir_item.put("day", get_em.nextToken().trim());
                        dir_item.put("time_or_year", get_em.nextToken().trim());
                        String name_data = get_em.nextToken();
                        name_data = data.substring(data.indexOf(name_data));
                        dir_item.put("name", name_data);
                        dir_item.put("dir", path);
                        if (!dir_item.getProperty("name").equals(".") && !dir_item.getProperty("name").equals("..")) {
                            this.addModifiedItem(dir_item);
                            list.addElement(dir_item);
                        }
                    }
                } else if (this.config.getProperty("server_type", "").indexOf("OS/400") >= 0 && data.indexOf("*") >= 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
                    String owner = data.substring(0, 7).trim();
                    String size = data.substring(8, 21).trim();
                    String date = data.substring(22, 30).trim();
                    String time = data.substring(31, 39).trim();
                    String type = data.substring(40, 45).trim();
                    String name = data.substring(46).trim();
                    if (name.startsWith("/")) {
                        name = name.substring(1);
                    }
                    if (name.endsWith("/")) {
                        name = name.substring(0, name.length() - 1);
                    }
                    if (size.equals("")) {
                        size = "0";
                    }
                    if (date.equals("")) {
                        date = "01/01/1970";
                    }
                    if (time.equals("")) {
                        time = "12:00:00";
                    }
                    if (type.equals("*LIB") || name.toUpperCase().endsWith(".LIB")) {
                        dir_item.put("type", "DIR");
                    } else {
                        dir_item.put("type", "FILE");
                    }
                    dir_item.put("permissions", String.valueOf(name.toUpperCase().endsWith(".LIB") ? "d" : "-") + "rwxrwxrwx");
                    dir_item.put("owner", owner);
                    dir_item.put("num_items", "0");
                    dir_item.put("group", "group");
                    dir_item.put("size", size);
                    if (date.indexOf(".") >= 0) {
                        sdf = new SimpleDateFormat("MM.dd.yy");
                    }
                    dir_item.put("month", this.mm.format(sdf.parse(date)));
                    dir_item.put("day", this.dd.format(sdf.parse(date)));
                    dir_item.put("time_or_year", this.yyyy.format(sdf.parse(date)));
                    if (this.config.getProperty("no_os400", "false").equalsIgnoreCase("false")) {
                        name = Common.replace_str(name, ".FILE", "");
                        if ((name = Common.replace_str(name, ".MBR", "")).indexOf("/") >= 0) {
                            name = name.replace('/', '.');
                        }
                    } else if (name.indexOf("/") >= 0) {
                        name = Common.last(name);
                    }
                    dir_item.put("name", name);
                    dir_item.put("dir", path);
                    if (!dir_item.getProperty("name").equals(".") && !dir_item.getProperty("name").equals("..")) {
                        this.addModifiedItem(dir_item);
                        list.addElement(dir_item);
                    }
                } else if (this.config.getProperty("server_type", "").indexOf("WINDOWS") >= 0) {
                    if (!data.toUpperCase().startsWith("TOTAL ")) {
                        String d = this.getLineToken(data, 0, true).trim();
                        String t = this.getLineToken(data, 1, true).trim();
                        Date date = new Date();
                        try {
                            if (d.length() < 10) {
                                d = Integer.parseInt(d.substring(6)) < 95 ? String.valueOf(d.substring(0, 6)) + "20" + d.substring(6) : String.valueOf(d.substring(0, 6)) + "19" + d.substring(6);
                            }
                            date = this.msdf.parse(String.valueOf(d) + " " + t);
                        }
                        catch (ParseException e) {
                            this.log("FTP_CLIENT", 1, e);
                        }
                        dir_item.put("month", this.mmm.format(date));
                        dir_item.put("day", this.dd.format(date));
                        String time_or_year = this.hhmm.format(date);
                        if (!this.yyyy.format(date).equals(this.yyyy.format(new Date()))) {
                            time_or_year = this.yyyy.format(date);
                        }
                        dir_item.put("time_or_year", time_or_year);
                        String typeOrSize = this.getLineToken(data, 2, true).trim();
                        if (typeOrSize.toUpperCase().indexOf("DIR") >= 0) {
                            dir_item.put("type", "DIR");
                            dir_item.put("permissions", "drwxrwxrwx");
                            dir_item.put("size", "0");
                            typeOrSize = String.valueOf(typeOrSize) + "         ";
                        } else {
                            dir_item.put("type", "FILE");
                            dir_item.put("permissions", "-rwxrwxrwx");
                            dir_item.put("size", typeOrSize);
                        }
                        dir_item.put("owner", "user");
                        dir_item.put("group", "group");
                        dir_item.put("num_items", "0");
                        String name_data = this.getLineToken(data, 3, false);
                        dir_item.put("name", name_data);
                        dir_item.put("dir", path);
                        if (!dir_item.getProperty("name").equals(".") && !dir_item.getProperty("name").equals("..")) {
                            this.addModifiedItem(dir_item);
                            list.addElement(dir_item);
                        }
                    }
                    if (System.getProperty("crushftp.ftpclient.always_windows", "false").equals("true")) {
                        this.log("FTP_CLIENT", 2, "2:" + dir_item);
                    }
                } else if (this.config.getProperty("server_type", "").indexOf("NONSTOP") >= 0) {
                    if (data.endsWith("..")) continue;
                    this.parse_unix_line(dir_item, data, tokenCount, path, list);
                } else {
                    this.parse_unix_line(dir_item, data, tokenCount, path, list);
                }
                if (this.config.getProperty("no_os400", "false").equalsIgnoreCase("true")) {
                    dir_item.put("no_os400", "true");
                }
                if (this.url.endsWith("/") && path.startsWith("/")) {
                    dir_item.put("url", String.valueOf(this.url) + path.substring(1) + dir_item.getProperty("name"));
                } else {
                    dir_item.put("url", String.valueOf(this.url) + path + dir_item.getProperty("name"));
                }
                if (!this.config.getProperty("stat_cache", "false").equals("true")) continue;
                stat_cache.put(dir_item.getProperty("url"), dir_item);
            }
            catch (Exception eee) {
                this.log("FTP_CLIENT", 1, eee);
                if (("" + eee).indexOf("Interrupted") < 0) continue;
                throw eee;
            }
        }
        is_list.close();
        this.send_data(226, "");
        this.executeScript(this.config.getProperty("after_dir_script", ""), path.trim());
        return list;
    }

    public String getLineToken(String data, int i, boolean stop_on_white) {
        this.active();
        int i2 = -1;
        String s = "";
        boolean in_white = true;
        int x = 0;
        while (x < data.length()) {
            if (in_white && data.charAt(x) != ' ') {
                in_white = false;
                if (i == i2) break;
                ++i2;
            }
            if (!in_white && data.charAt(x) == ' ') {
                in_white = true;
            }
            if (!in_white && i == i2 && !stop_on_white) {
                return data.substring(x);
            }
            if (!in_white && i == i2) {
                s = String.valueOf(s) + data.charAt(x);
            }
            ++x;
        }
        return s;
    }

    public void addModifiedItem(Properties dir_item) {
        this.active();
        if (dir_item != null) {
            SimpleDateFormat mmddyyyy = new SimpleDateFormat("MMM dd yyyy HH:mm", Locale.US);
            SimpleDateFormat yyyy = new SimpleDateFormat("yyyy");
            Date modified = new Date();
            String time_or_year = dir_item.getProperty("time_or_year", "");
            String year = yyyy.format(new Date());
            String time = "00:00";
            if (time_or_year.indexOf(":") < 0) {
                year = time_or_year;
            } else {
                time = time_or_year;
            }
            try {
                modified = mmddyyyy.parse(String.valueOf(dir_item.getProperty("month", "")) + " " + dir_item.getProperty("day", "") + " " + year + " " + time);
                if (modified.getTime() > System.currentTimeMillis() + 172800000L) {
                    year = String.valueOf(Integer.parseInt(year) - 1);
                    modified = mmddyyyy.parse(String.valueOf(dir_item.getProperty("month", "")) + " " + dir_item.getProperty("day", "") + " " + year + " " + time);
                }
            }
            catch (Exception e) {
                this.log("FTP_CLIENT", 1, e);
            }
            dir_item.put("modified", String.valueOf(modified.getTime()));
        }
    }

    public void parse_unix_line(Properties dir_item, String data, int tokenCount, String path, Vector list) {
        this.active();
        if (!data.toUpperCase().startsWith("TOTAL ") && !data.toUpperCase().trim().startsWith("SIZE")) {
            if (data.toUpperCase().startsWith("D") || data.toUpperCase().startsWith("L")) {
                dir_item.put("type", "DIR");
            } else {
                dir_item.put("type", "FILE");
            }
            StringTokenizer get_em = new StringTokenizer(data, " ");
            int countTokens = 0;
            while (get_em.hasMoreElements()) {
                get_em.nextToken();
                ++countTokens;
            }
            get_em = new StringTokenizer(data, " ");
            dir_item.put("proxy_item", "true");
            if (countTokens < 6 || System.getProperty("crushftp.ftpclient.always_windows", "false").equals("true")) {
                this.log("FTP_CLIENT", 2, "tokens on ftp dir list line:" + countTokens);
                if (!data.toUpperCase().startsWith("TOTAL ")) {
                    String d = this.getLineToken(data, 0, true).trim();
                    String t = this.getLineToken(data, 1, true).trim();
                    Date date = new Date();
                    try {
                        if (d.length() < 10) {
                            d = Integer.parseInt(d.substring(6)) < 95 ? String.valueOf(d.substring(0, 6)) + "20" + d.substring(6) : String.valueOf(d.substring(0, 6)) + "19" + d.substring(6);
                        }
                        date = this.msdf.parse(String.valueOf(d) + " " + t);
                    }
                    catch (ParseException e) {
                        this.log("FTP_CLIENT", 1, e);
                    }
                    dir_item.put("month", this.mmm.format(date));
                    dir_item.put("day", this.dd.format(date));
                    String time_or_year = this.hhmm.format(date);
                    if (!this.yyyy.format(date).equals(this.yyyy.format(new Date()))) {
                        time_or_year = this.yyyy.format(date);
                    }
                    dir_item.put("time_or_year", time_or_year);
                    String typeOrSize = this.getLineToken(data, 2, true).trim();
                    if (typeOrSize.toUpperCase().indexOf("DIR") >= 0) {
                        dir_item.put("type", "DIR");
                        dir_item.put("permissions", "drwxrwxrwx");
                        dir_item.put("size", "0");
                        typeOrSize = String.valueOf(typeOrSize) + "         ";
                    } else {
                        dir_item.put("type", "FILE");
                        dir_item.put("permissions", "-rwxrwxrwx");
                        dir_item.put("size", typeOrSize);
                    }
                    dir_item.put("owner", "user");
                    dir_item.put("group", "group");
                    dir_item.put("num_items", "0");
                    String name_data = this.getLineToken(data, 3, false);
                    dir_item.put("name", name_data);
                    dir_item.put("dir", path);
                }
            } else {
                dir_item.put("permissions", get_em.nextToken().trim());
                dir_item.put("num_items", get_em.nextToken().trim());
                dir_item.put("owner", get_em.nextToken().trim().replace('\\', '_'));
                dir_item.put("size", "");
                while (!Common.isNumeric(dir_item.getProperty("size", ""))) {
                    String group = get_em.nextToken().trim();
                    this.log("FTP_CLIENT", 2, "tokens on ftp dir list line:" + tokenCount);
                    if (tokenCount == 8 || this.config.getProperty("7_token_proxy", "false").equalsIgnoreCase("true")) {
                        dir_item.put("group", "group");
                        dir_item.put("size", group);
                        continue;
                    }
                    dir_item.put("group", group.replace('\\', '_'));
                    dir_item.put("size", get_em.nextToken().trim());
                }
                dir_item.put("month", get_em.nextToken().trim());
                dir_item.put("day", get_em.nextToken().trim());
                dir_item.put("time_or_year", get_em.nextToken().trim());
                String name_data = get_em.nextToken();
                String searchName = String.valueOf(dir_item.getProperty("time_or_year")) + " " + name_data;
                name_data = data.substring(data.indexOf(name_data, data.indexOf(searchName) + dir_item.getProperty("time_or_year").length() + 1));
                if (name_data.startsWith("/")) {
                    name_data = Common.last(name_data);
                }
                dir_item.put("name", name_data);
                if (data.toUpperCase().startsWith("L")) {
                    dir_item.put("name", name_data.substring(0, name_data.indexOf(" ->")));
                    dir_item.put("permissions", "drwxrwxrwx");
                    dir_item.put("type", "DIR");
                }
            }
            if (dir_item.getProperty("type").equalsIgnoreCase("DIR")) {
                dir_item.put("size", "1");
            }
            dir_item.put("dir", path);
            if (this.config.getProperty("no_stat", "false").equals("true")) {
                dir_item.put("no_stat", "true");
            }
            if (!dir_item.getProperty("name").equals(".") && !dir_item.getProperty("name").equals("..")) {
                this.addModifiedItem(dir_item);
                list.addElement(dir_item);
            }
        }
        if (System.getProperty("crushftp.ftpclient.always_windows", "false").equals("true")) {
            this.log("FTP_CLIENT", 2, "1:" + dir_item);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Object getTransferSocket(boolean pasv) throws Exception {
        if (!pasv) {
            VRL u;
            String ip = this.fcssp.getDestControlSocket().getLocalAddress().getHostAddress();
            if (this.url.indexOf("~") >= 0 && (u = new VRL(this.url)).getHost().indexOf("~") >= 0) {
                ip = u.getHost().split("~")[0].trim();
            }
            ServerSocket ss = null;
            Object object = activePortLock;
            synchronized (object) {
                int startPort = Integer.parseInt(this.config.getProperty("proxyActivePorts", "1025-65535").split("-")[0]);
                int endPort = Integer.parseInt(this.config.getProperty("proxyActivePorts", "1025-65535").split("-")[1]);
                int loops = 0;
                while (ss == null && loops++ < 1000) {
                    if (curPort < 0 || curPort < startPort) {
                        curPort = startPort;
                    }
                    if (curPort > endPort) {
                        curPort = startPort;
                    }
                    try {
                        int listen_port = 0;
                        if (startPort != 1025 && endPort != 65535) {
                            listen_port = curPort;
                        }
                        if (this.config.getProperty("secure", "false").equalsIgnoreCase("true") && this.config.getProperty("secure_data", "true").equals("true")) {
                            String tempKeystore;
                            if (common_code == null) {
                                common_code = new Common();
                            }
                            if ((tempKeystore = this.config.getProperty("keystore_path", "builtin")).endsWith("dxserverpub")) {
                                tempKeystore = "builtin";
                            }
                            if (this.ssl_context == null) {
                                this.ssl_context = new Common().getSSLContext(!this.config.getProperty("keystore_path", "").equals("") ? this.config.getProperty("keystore_path") : "builtin", this.config.getProperty("trustore_path", "builtin"), Common.encryptDecrypt(this.config.getProperty("keystore_pass"), false), Common.encryptDecrypt(this.config.getProperty("key_pass"), false), "TLS", !this.config.getProperty("keystore_path", "").equals(""), this.config.getProperty("acceptAnyCert", "true").equalsIgnoreCase("true"));
                            }
                            ss = common_code.getServerSocket(this.ssl_context.getServerSocketFactory(), listen_port, null, this.config.getProperty("disabled_ciphers", ""), false);
                        } else {
                            ss = new ServerSocket(listen_port, 1, InetAddress.getByName(ip));
                        }
                    }
                    catch (Exception e) {
                        this.log("FTP_CLIENT", 0, String.valueOf(e.getMessage()) + ":ip=" + ip + " port=" + curPort);
                        this.log("FTP_CLIENT", 2, e);
                    }
                    ++curPort;
                }
            }
            if (ss == null) {
                throw new Exception("Could not build a server socket.");
            }
            int port = ss.getLocalPort();
            ip = ip.replace('.', ',');
            int port1 = port / 256;
            int port2 = port - port / 256 * 256;
            this.send_data(200, "PORT " + ip + "," + port1 + "," + port2);
            this.active();
            return ss;
        }
        String data = "";
        data = this.send_data(227, "PASV");
        int endPasv = data.indexOf(")");
        if (endPasv < 0) {
            endPasv = data.length();
        }
        data = data.substring(data.indexOf("(") + 1, endPasv).trim();
        int port1 = Integer.parseInt(data.substring(data.lastIndexOf(",", data.lastIndexOf(",") - 1) + 1, data.lastIndexOf(",")).trim());
        int port2 = Integer.parseInt(data.substring(data.lastIndexOf(",") + 1).trim());
        int port = port1 * 256 + port2;
        String ip = data.substring(0, data.lastIndexOf(",", data.lastIndexOf(",") - 1)).replace(',', '.').trim();
        if (this.config.getProperty("autoPasvIpSubstitution", "true").equalsIgnoreCase("true")) {
            VRL u = new VRL(this.url);
            ip = u.getHost();
        }
        Socket tempSock = null;
        int x = 0;
        while (x < 4) {
            try {
                tempSock = this.fcssp.getSock(ip, port);
                break;
            }
            catch (IOException e) {
                if (x == 3) {
                    throw e;
                }
                Thread.sleep(500L);
                ++x;
            }
        }
        this.active();
        if (this.config.getProperty("secure", "false").equalsIgnoreCase("true") && this.config.getProperty("secure_data", "true").equals("true")) {
            return (SSLSocket)this.ssl_factory.createSocket(tempSock, tempSock.getLocalAddress().getHostName(), tempSock.getPort(), true);
        }
        return tempSock;
    }

    private String send_data_raw(int expectedResponse, String data, InputStream in) throws IOException {
        String verb = null;
        String verb_data = null;
        if (data.trim().length() > 0) {
            verb = (String.valueOf(data) + " ").substring(0, (String.valueOf(data) + " ").indexOf(" ")).toLowerCase().trim();
            verb_data = (String.valueOf(data) + " ").substring((String.valueOf(data) + " ").indexOf(" ")).trim();
            this.executeScript(this.config.getProperty("before_" + verb + "_script", ""), verb_data);
        }
        int timeout = Integer.parseInt(System.getProperty("crushftp.ftpcommand.timeout1", "30")) * 1000;
        if (data.length() > 0) {
            this.send_data_raw(String.valueOf(data) + "\r\n");
        } else {
            timeout = Integer.parseInt(System.getProperty("crushftp.ftpcommand.timeout2", "60")) * 1000;
        }
        if (data.trim().equalsIgnoreCase("QUIT")) {
            timeout = 3000;
        }
        int current_timeout = 0;
        try {
            current_timeout = this.sock.getSoTimeout();
        }
        catch (Exception exception) {
            // empty catch block
        }
        boolean startsWithInt = false;
        try {
            this.sock.setSoTimeout(timeout);
        }
        catch (Exception exception) {
            // empty catch block
        }
        data = "";
        String dataTotal = "";
        while (data.length() < 3 || data.length() >= 4 && data.charAt(3) != ' ' || !startsWithInt) {
            String result = "";
            int lastByte = 0;
            while (result.indexOf("\r\n") < 0 && lastByte >= 0) {
                lastByte = in.read();
                if (lastByte <= 0) continue;
                result = String.valueOf(result) + (char)lastByte;
            }
            if (lastByte < 0) {
                return null;
            }
            data = result.trim();
            this.log(in + ":" + data);
            dataTotal = String.valueOf(dataTotal) + data + "\r\n";
            if (data.equals("500 Invalid command: try being more creative")) {
                data = "";
                this.log(in + ":PROFTPD bug fix, ignoring line and reading next line.");
                continue;
            }
            if (data.equals("500 Command not understood") && this.config.getProperty("server_type", "").toUpperCase().indexOf("WINDOWS_NT") >= 0) {
                data = "";
                this.log(in + ":WINDOWS_NT bug fix, ignoring line and reading next line.");
                continue;
            }
            try {
                startsWithInt = false;
                Integer.parseInt(data.substring(0, 3));
                startsWithInt = true;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        this.log("FTP_CLIENT", 2, "FTP RESPONSE:" + expectedResponse + ":" + dataTotal);
        try {
            this.sock.setSoTimeout(current_timeout);
        }
        catch (Exception result) {
            // empty catch block
        }
        int code = Integer.parseInt(data.substring(0, 3));
        if (expectedResponse > 0 && Math.abs(code - expectedResponse) >= 100 && expectedResponse != 331) {
            this.log("FTP_CLIENT", 1, "ERROR: Expected " + expectedResponse + " but got " + data);
            if (this.config.getProperty("rfc_proxy", "false").equals("true")) {
                throw new IOException(data);
            }
            throw new IOException("ERROR: Expected " + expectedResponse + " but got " + data);
        }
        if (verb != null) {
            this.executeScript(this.config.getProperty("after_" + verb + "_script", ""), verb_data);
        }
        return dataTotal;
    }

    private String send_data(int expectedResponse, String data) throws IOException {
        this.active();
        if (!data.toUpperCase().startsWith("PASS")) {
            this.log("FTP_CLIENT", 2, "FTP COMMAND:" + expectedResponse + ":" + data);
        }
        if (data.startsWith("TYPE ") && data.equals(this.config.getProperty("currentTYPE", ""))) {
            return "";
        }
        if (data.startsWith("TYPE ")) {
            this.setConfig("currentTYPE", data);
        }
        String rawData = data = this.send_data_now(data, true);
        String[] lines = data.split("\\r\\n");
        int code = Integer.parseInt(lines[lines.length - 1].substring(0, 3));
        if (code != expectedResponse && Math.abs(code - expectedResponse) < 75) {
            code = expectedResponse;
        }
        if (code != expectedResponse && expectedResponse > 0) {
            this.log("FTP_CLIENT", 1, "ERROR: Expected " + expectedResponse + " but got " + rawData);
            if (this.config.getProperty("rfc_proxy", "false").equals("true")) {
                throw new IOException(rawData);
            }
            throw new IOException("ERROR: Expected " + expectedResponse + " but got " + rawData);
        }
        return data;
    }

    private String send_data_now(String data, boolean returnAll) throws IOException {
        this.active();
        String verb = null;
        String verb_data = null;
        if (data.trim().length() > 0) {
            verb = (String.valueOf(data) + " ").substring(0, (String.valueOf(data) + " ").indexOf(" ")).toLowerCase().trim();
            verb_data = (String.valueOf(data) + " ").substring((String.valueOf(data) + " ").indexOf(" ")).trim();
            this.executeScript(this.config.getProperty("before_" + verb + "_script", ""), verb_data);
        }
        int timeout = Integer.parseInt(System.getProperty("crushftp.ftpcommand.timeout1", "30")) * 1000;
        if (data.length() > 0) {
            this.send_data_raw(String.valueOf(data) + "\r\n");
        } else {
            timeout = Integer.parseInt(System.getProperty("crushftp.ftpcommand.timeout2", "60")) * 1000;
        }
        if (data.trim().equalsIgnoreCase("QUIT")) {
            timeout = 3000;
        }
        int current_timeout = 0;
        try {
            current_timeout = this.sock.getSoTimeout();
        }
        catch (Exception exception) {
            // empty catch block
        }
        String sent_data = data;
        data = "-------------";
        boolean startsWithInt = false;
        String totalData = "";
        try {
            this.sock.setSoTimeout(timeout);
        }
        catch (Exception exception) {}
        while (data.length() < 3 || data.length() >= 4 && data.charAt(3) != ' ' || !startsWithInt) {
            if (this.is == null) {
                return null;
            }
            data = this.is.readLine();
            this.log(this.is + ":" + data);
            this.log("FTP_CLIENT", 2, this.is + ":" + data);
            if (data.equals("500 Invalid command: try being more creative")) {
                data = "";
                this.log(this.in + ":PROFTPD bug fix, ignoring line and reading next line.");
                continue;
            }
            if (data.equals("500 Command not understood") && this.config.getProperty("server_type", "").toUpperCase().indexOf("WINDOWS_NT") >= 0) {
                data = "";
                this.log(this.in + ":WINDOWS_NT bug fix, ignoring line and reading next line.");
                continue;
            }
            if (data == null) {
                throw new IOException("FTP control channel closed:" + Thread.currentThread().getName());
            }
            if (this.config.getProperty("server_type", "UNKNOWN").indexOf("NONSTOP") >= 0 && data.startsWith("500 '': command not understood.") || this.config.getProperty("server_type", "UNKNOWN").indexOf("OS/400") >= 0 && data.toUpperCase().indexOf("not valid".toUpperCase()) >= 0 || this.config.getProperty("server_type", "UNKNOWN").indexOf("WFTPD") >= 0 && data.toUpperCase().indexOf("Unidentified command".toUpperCase()) >= 0 || data.toUpperCase().indexOf("Syntax error, command unrecognized".toUpperCase()) >= 0 && sent_data.indexOf("OPTS UTF8") < 0) continue;
            totalData = String.valueOf(totalData) + data + "\r\n";
            try {
                startsWithInt = false;
                Integer.parseInt(data.substring(0, 4).trim());
                startsWithInt = true;
            }
            catch (Exception e) {
                this.log("FTP_CLIENT", 3, e);
            }
            if (data.startsWith("221")) break;
        }
        if (verb != null) {
            this.executeScript(this.config.getProperty("after_" + verb + "_script", ""), verb_data);
        }
        try {
            this.sock.setSoTimeout(current_timeout);
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (returnAll) {
            return totalData;
        }
        return data;
    }

    private void send_data_raw(String data) throws IOException {
        this.active();
        try {
            if (data.indexOf("PASS ") < 0) {
                this.log(this.os + ":" + data);
            } else {
                this.log(this.os + ":" + "PASS **********");
            }
            this.os.write(data);
            this.os.flush();
        }
        catch (SocketException e) {
            this.config.put("error", e.toString());
            throw e;
        }
    }

    @Override
    public void setMod(String path, String val, String param) {
    }

    @Override
    public void setOwner(String path, String val, String param) {
    }

    @Override
    public void setGroup(String path, String val, String param) {
    }

    @Override
    public String doCommand(String command) throws Exception {
        return this.send_data_now(String.valueOf(command) + "\r\n", false);
    }

    public void executeScript(String script, String verb_data) throws IOException {
        String command;
        if (script == null || script.trim().equals("")) {
            return;
        }
        script = Common.replace_str(script, "%data%", verb_data);
        script = Common.replace_str(script, "{data}", verb_data);
        BufferedReader br = new BufferedReader(new StringReader(script));
        while ((command = br.readLine()) != null) {
            String response = br.readLine();
            if (response == null) {
                throw new IOException("Script format error, missing required response pattern.");
            }
            command = command.trim();
            response = response.trim();
            boolean isNumber = false;
            try {
                Integer.parseInt(response);
                isNumber = true;
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (isNumber) {
                this.send_data(Integer.parseInt(response), command);
                continue;
            }
            String actualResponse = this.send_data_now(command, true);
            if (Common.do_search(response, actualResponse, false, 0)) continue;
            throw new IOException("Script validation failure:'" + response + "' does not match: '" + actualResponse + "'.");
        }
    }

    public void active() {
        if (this.fcssp != null) {
            this.fcssp.active();
        }
    }
}

