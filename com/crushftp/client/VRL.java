/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.URLConnection;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;

public class VRL {
    String url = null;
    String original_url = null;
    String protocol = "file";
    URL u = null;
    public static Properties host_config_val = new Properties();
    Properties configs = new Properties();

    public VRL(String url2) {
        String port;
        int port_index;
        this.original_url = url2;
        if (url2.indexOf("{connect_start}") >= 0) {
            try {
                String inner = url2.substring(url2.indexOf("{connect_start}"), url2.indexOf("{connect_end}") + "{connect_end}".length());
                String profile = Common.dots(inner.substring(inner.indexOf("}") + 1, inner.indexOf("{", 2)));
                Vector v = (Vector)Common.readXMLObject(String.valueOf(System.getProperty("crushftp.users")) + "ConnectionProfiles/" + profile + "/VFS/server");
                Properties settings = (Properties)v.elementAt(0);
                String connect_url = (String)settings.remove("url");
                if (settings.getProperty("encrypted", "false").equals("true")) {
                    try {
                        connect_url = Common.encryptDecrypt(connect_url, false);
                    }
                    catch (Exception e) {
                        Common.log("SERVER", 2, e);
                    }
                }
                Common.putAllSafe(this.configs, settings);
                url2 = Common.replace_str(url2, inner, connect_url);
            }
            catch (Exception e) {
                Common.log("SERVER", 0, e);
            }
        }
        if (System.getProperty("crushftp.v10_beta", "false").equals("true") && url2.indexOf("@") > 0 && url2.indexOf(":", url2.indexOf("@")) > 0 && url2.indexOf("/", port_index = url2.indexOf(":", url2.indexOf("@"))) > 0 && (port = url2.substring(port_index, url2.indexOf("/", port_index))).contains("!!!") && port.contains("~")) {
            try {
                url2 = Common.replace_str(url2, port.startsWith(":!!!") ? port : port.substring(port.indexOf("!!!")), "");
                String[] url_configs = port.substring(port.indexOf("!!!")).split("!!!");
                int x = 0;
                while (x < url_configs.length) {
                    if (url_configs[x].contains("~")) {
                        String key = url_configs[x].substring(0, url_configs[x].indexOf("~")).trim();
                        String value = url_configs[x].substring(url_configs[x].indexOf("~") + 1, url_configs[x].length()).trim();
                        if (key.equals("ssh_private_key") || key.equals("sharepoint_site_path") || key.equals("sas_token")) {
                            value = VRL.vrlDecode(Common.url_decode(value));
                        }
                        if (key.equals("path")) {
                            value = VRL.vrlDecode(value);
                        }
                        if (value.equals("null")) {
                            value = "";
                        }
                        this.configs.put(key, value);
                    }
                    ++x;
                }
            }
            catch (Exception e) {
                Common.log("SERVER", 0, e);
            }
        }
        if (url2.indexOf(":") < 0) {
            if (url2.startsWith(".")) {
                try {
                    url2 = new File(url2).getCanonicalPath();
                }
                catch (Exception e1) {
                    Common.log("SERVER", 0, e1);
                }
            } else if (url2.startsWith("////")) {
                url2 = "FILE:" + url2;
            } else if (url2.startsWith("///")) {
                url2 = "FILE:/" + url2;
            } else if (url2.startsWith("//")) {
                url2 = "FILE://" + url2;
            }
        }
        if (Common.machine_is_windows() && url2.length() > 1 && url2.charAt(1) == ':') {
            url2 = "/" + url2.replace('\\', '/');
        }
        if (Common.machine_is_windows() && url2.length() > 0 && url2.charAt(0) == '\\' && url2.charAt(2) == ':') {
            url2 = url2.replace('\\', '/');
        }
        if (Common.machine_is_windows() && url2.length() > 9 && url2.startsWith("file:///") && url2.charAt(9) == ':') {
            url2 = url2.replace('\\', '/');
        }
        if (Common.machine_is_windows() && url2.length() > 8 && url2.startsWith("file://") && url2.charAt(8) == ':') {
            url2 = url2.replace('\\', '/');
        }
        if (Common.machine_is_windows() && url2.length() > 7 && url2.startsWith("file:/") && url2.charAt(7) == ':') {
            url2 = url2.replace('\\', '/');
        }
        if (!(url2.length() <= 11 || url2.indexOf(":", 11) <= 0 || url2.indexOf(":", 11) >= url2.indexOf("@") || url2.toUpperCase().startsWith("FILE:") || url2.toUpperCase().startsWith("CUSTOM.") || url2.toUpperCase().startsWith("SHAREPOINT2"))) {
            String pre_part = url2.substring(0, url2.indexOf("//") + 2);
            String user_part = url2.substring(pre_part.length(), url2.indexOf(":", 11));
            String pass_part = url2.substring(url2.indexOf(":", 11) + 1, url2.indexOf("@"));
            String post_part = url2.substring(url2.indexOf("@"));
            if (user_part.indexOf("#") >= 0 || pass_part.indexOf("#") >= 0 || user_part.indexOf("&") >= 0 || pass_part.indexOf("&") >= 0 || user_part.indexOf("?") >= 0 || pass_part.indexOf("?") >= 0) {
                url2 = String.valueOf(pre_part) + VRL.vrlEncode(user_part) + ":" + VRL.vrlEncode(pass_part) + post_part;
            }
        }
        this.url = url2;
        if (this.url.indexOf(":") > 2 || this.url.toUpperCase().startsWith("S3:") || this.url.toUpperCase().startsWith("GDRIVE:") || this.url.toUpperCase().startsWith("AZURE:") || this.url.toUpperCase().startsWith("B2:")) {
            this.protocol = this.url.substring(0, this.url.indexOf(":"));
        } else {
            this.url = "file:" + this.url;
        }
        int slashPos = this.url.indexOf("/", this.protocol.length() + 3);
        if (slashPos < 0) {
            slashPos = this.url.length() - 1;
        }
        boolean encoded_pass = false;
        if ((this.protocol.equalsIgnoreCase("S3") || this.protocol.equalsIgnoreCase("AZURE") || this.protocol.equalsIgnoreCase("GDRIVE") || this.protocol.equalsIgnoreCase("CITRIX") || this.protocol.equalsIgnoreCase("DROPBOX") || this.protocol.equalsIgnoreCase("B2") || this.protocol.equalsIgnoreCase("ONEDRIVE") || this.protocol.equalsIgnoreCase("SHAREPOINT") || this.protocol.equalsIgnoreCase("SHAREPOINT2")) && this.url.indexOf(":", this.protocol.length() + 2) >= 0 && this.url.indexOf("@") >= 0) {
            String pass = this.url.substring(this.url.indexOf(":", this.protocol.length() + 2) + 1, this.url.indexOf("@"));
            pass = Common.url_encode(pass);
            encoded_pass = true;
            this.url = String.valueOf(this.url.substring(0, this.url.indexOf(":", this.protocol.length() + 2) + 1)) + pass + this.url.substring(this.url.indexOf("@"));
        } else if (!(this.protocol.equalsIgnoreCase("file") || this.protocol.equalsIgnoreCase("http") || this.protocol.equalsIgnoreCase("https") || this.protocol.equalsIgnoreCase("sql") || slashPos >= this.url.indexOf("@") || this.protocol.startsWith("CUSTOM_"))) {
            this.url = String.valueOf(this.url.substring(0, this.url.indexOf("/", 8))) + "\\" + this.url.substring(this.url.indexOf("/", 8) + 1);
        } else if (this.protocol.toUpperCase().startsWith("VFS")) {
            this.url = "VFS://" + VRL.vrlEncode(this.url.substring(this.url.lastIndexOf("://") + 3, this.url.lastIndexOf(":"))) + this.url.substring(this.url.lastIndexOf(":"));
        }
        this.url = Common.replace_str(this.url, "\\", "%5C");
        this.url = Common.replace_str(this.url, "?", "%3F");
        if (this.url.toUpperCase().indexOf("TP://") < 0 && this.url.toUpperCase().indexOf("TP:/") >= 0) {
            this.url = Common.replace_str(this.url, ":/", "://");
        }
        if (this.url.toUpperCase().indexOf("TPS://") < 0 && this.url.toUpperCase().indexOf("TPS:/") >= 0) {
            this.url = Common.replace_str(this.url, ":/", "://");
        }
        String s = "ftp:" + this.url.substring(this.protocol.length() + 1);
        if (this.url.lastIndexOf(":-") >= 0 && this.url.indexOf("@") < this.url.lastIndexOf(":-")) {
            s = String.valueOf(s.substring(0, s.lastIndexOf(":-") + 1)) + s.substring(s.lastIndexOf(":-") + 2);
        }
        if (s.indexOf(":/") >= 0 && s.indexOf("://") < 0) {
            s = String.valueOf(s.substring(0, s.indexOf(":") + 1)) + "/" + s.substring(s.indexOf(":") + 1);
        }
        if (s.contains(",")) {
            s = VRL.getActiveUrl(s, false);
        }
        try {
            this.u = new URL(s);
        }
        catch (MalformedURLException e) {
            Common.log("SERVER", 0, e);
            Common.log("SERVER", 2, "url original =" + this.original_url);
            Common.log("SERVER", 0, "url1=" + this.url);
            Common.log("SERVER", 0, "url2=" + s);
        }
        if (encoded_pass) {
            this.url = url2;
        }
    }

    public String getProtocol() {
        return this.protocol;
    }

    public URLConnection openConnection() {
        return URLConnection.openConnection(this, new Properties());
    }

    public static String fileFix(String url2) {
        if (url2.toLowerCase().startsWith("file:/") && !url2.toLowerCase().startsWith("file://")) {
            url2 = "file://" + url2.substring("file:/".length());
        }
        if (url2.startsWith("FILE://")) {
            url2 = "file://" + url2.substring("file://".length());
        }
        return url2;
    }

    public String getPath() {
        String s = this.u.getPath();
        try {
            if (this.protocol.equalsIgnoreCase("file")) {
                if (this.url.toLowerCase().startsWith("file:/") && !this.url.toLowerCase().startsWith("file://")) {
                    s = this.url.substring("file:".length());
                } else if (this.url.toLowerCase().startsWith("file://") && !this.url.toLowerCase().startsWith("file:///")) {
                    s = this.url.substring("file:/".length());
                } else if (this.url.toLowerCase().startsWith("file:////")) {
                    s = this.url.substring("file://".length());
                } else if (this.url.indexOf("#") >= 0) {
                    s = String.valueOf(s) + this.url.substring(this.url.indexOf(s) + s.length());
                }
            } else if (this.url.indexOf("#") >= 0) {
                s = String.valueOf(s) + this.url.substring(this.url.indexOf("#"));
            }
            s = Common.url_decode(s);
        }
        catch (Exception e) {
            Common.log("SERVER", 1, s);
            Common.log("SERVER", 1, e);
            throw new RuntimeException(e);
        }
        return s;
    }

    public String getName() {
        String s = Common.last(this.getPath());
        if (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public String getCanonicalPath() throws IOException {
        if (this.protocol.equalsIgnoreCase("file")) {
            return new File(this.getPath()).getCanonicalPath();
        }
        return this.getPath();
    }

    public String getAuthority() {
        if (this.protocol.equalsIgnoreCase("file")) {
            return null;
        }
        return this.u.getAuthority();
    }

    public String getFile() {
        String s = this.u.getFile();
        if (this.protocol.equalsIgnoreCase("file")) {
            if (this.url.toLowerCase().startsWith("file:/") && !this.url.toLowerCase().startsWith("file://")) {
                s = this.url.substring("file:".length());
            } else if (this.url.toLowerCase().startsWith("file://") && !this.url.toLowerCase().startsWith("file:///")) {
                s = this.url.substring("file:/".length());
            }
        }
        if (this.url.indexOf("#") >= 0) {
            s = Common.last(this.url);
            s = Common.replace_str(s, "%5C", "\\");
            s = Common.replace_str(s, "%3F", "?");
        }
        return s;
    }

    public String getHost() {
        return VRL.vrlDecode(this.u.getHost());
    }

    public String getOriginalUrl() {
        return this.original_url;
    }

    public static String vrlDecode(String s) {
        s = Common.replace_str(s, "{at}", "@");
        s = Common.replace_str(s, "{quote}", "\"");
        s = Common.replace_str(s, "{apos}", "'");
        s = Common.replace_str(s, "{plus}", "+");
        s = Common.replace_str(s, "{colon}", ":");
        s = Common.replace_str(s, "{percent}", "%");
        s = Common.replace_str(s, "{amp}", "&");
        s = Common.replace_str(s, "{question}", "?");
        s = Common.replace_str(s, "{slash}", "/");
        s = Common.replace_str(s, "{backslash}", "\\");
        s = Common.replace_str(s, "{hash}", "#");
        s = Common.replace_str(s, "{pipe}", "|");
        s = Common.replace_str(s, "{lt}", "<");
        s = Common.replace_str(s, "{gt}", ">");
        s = Common.replace_str(s, "{AT}", "@");
        s = Common.replace_str(s, "{QUOTE}", "\"");
        s = Common.replace_str(s, "{APOS}", "'");
        s = Common.replace_str(s, "{PLUS}", "+");
        s = Common.replace_str(s, "{COLON}", ":");
        s = Common.replace_str(s, "{PERCENT}", "%");
        s = Common.replace_str(s, "{AMP}", "&");
        s = Common.replace_str(s, "{QUESTION}", "?");
        s = Common.replace_str(s, "{SLASH}", "/");
        s = Common.replace_str(s, "{BACKSLASH}", "\\");
        s = Common.replace_str(s, "{HASH}", "#");
        s = Common.replace_str(s, "{PIPE}", "|");
        s = Common.replace_str(s, "{LT}", "<");
        s = Common.replace_str(s, "{GT}", ">");
        return s;
    }

    public static String vrlEncode(String s) {
        s = Common.replace_str(s, "@", "{at}");
        s = Common.replace_str(s, "\"", "{quote}");
        s = Common.replace_str(s, "'", "{apos}");
        s = Common.replace_str(s, "+", "{plus}");
        s = Common.replace_str(s, ":", "{colon}");
        s = Common.replace_str(s, "%", "{percent}");
        s = Common.replace_str(s, "&", "{amp}");
        s = Common.replace_str(s, "?", "{question}");
        s = Common.replace_str(s, "/", "{slash}");
        s = Common.replace_str(s, "\\", "{backslash}");
        s = Common.replace_str(s, "#", "{hash}");
        s = Common.replace_str(s, "|", "{pipe}");
        s = Common.replace_str(s, "<", "{lt}");
        s = Common.replace_str(s, ">", "{gt}");
        return s;
    }

    public static String vrlEncodeRepair(String s) {
        s = Common.replace_str(s, "%at%", "{at}");
        s = Common.replace_str(s, "%quote%", "{quote}");
        s = Common.replace_str(s, "%apos%", "{apos}");
        s = Common.replace_str(s, "%plus%", "{plus}");
        s = Common.replace_str(s, "%colon%", "{colon}");
        s = Common.replace_str(s, "%percent%", "{percent}");
        s = Common.replace_str(s, "%amp%", "{amp}");
        s = Common.replace_str(s, "%question%", "{question}");
        s = Common.replace_str(s, "%slash%", "{slash}");
        s = Common.replace_str(s, "%backslash%", "{backslash}");
        s = Common.replace_str(s, "%hash%", "{hash}");
        s = Common.replace_str(s, "%pipe%", "{pipe}");
        s = Common.replace_str(s, "%lt%", "{lt}");
        s = Common.replace_str(s, "%gt%", "{gt}");
        return s;
    }

    public static String vrlUrlEncoded(String s) {
        s = Common.replace_str(s, "{at}", "%40");
        s = Common.replace_str(s, "{quote}", "%22");
        s = Common.replace_str(s, "{apos}", "%27");
        s = Common.replace_str(s, "{plus}", "%2B");
        s = Common.replace_str(s, "{colon}", "%3A");
        s = Common.replace_str(s, "{percent}", "%25");
        s = Common.replace_str(s, "{amp}", "%26");
        s = Common.replace_str(s, "{question}", "%3F");
        s = Common.replace_str(s, "{slash}", "%2F");
        s = Common.replace_str(s, "{backslash}", "%5C");
        s = Common.replace_str(s, "{hash}", "%23");
        s = Common.replace_str(s, "{pipe}", "%7C");
        s = Common.replace_str(s, "{lt}", "%3C");
        s = Common.replace_str(s, "{gt}", "%3E");
        return s;
    }

    public String getQuery() {
        return VRL.vrlDecode(this.u.getQuery());
    }

    public String getUserInfo() {
        return VRL.vrlDecode(this.u.getUserInfo());
    }

    public int getPort() {
        if (this.protocol.equalsIgnoreCase("FTP") && this.u.getPort() == -1) {
            return 21;
        }
        if (this.protocol.equalsIgnoreCase("FTPES") && this.u.getPort() == -1) {
            return 21;
        }
        if (this.protocol.equalsIgnoreCase("FTPS") && this.u.getPort() == -1) {
            return 990;
        }
        if (this.protocol.equalsIgnoreCase("SFTP") && this.u.getPort() == -1) {
            return 22;
        }
        if (this.protocol.equalsIgnoreCase("HTTP") && this.u.getPort() == -1) {
            return 80;
        }
        if (this.protocol.equalsIgnoreCase("HTTPS") && this.u.getPort() == -1) {
            return 443;
        }
        if (this.protocol.equalsIgnoreCase("WEBDAVS") && this.u.getPort() == -1) {
            return 443;
        }
        if (this.protocol.equalsIgnoreCase("WEBDAV") && this.u.getPort() == -1) {
            return 80;
        }
        if (this.protocol.equalsIgnoreCase("AS2") && this.u.getPort() == -1) {
            return 80;
        }
        if (this.protocol.equalsIgnoreCase("S3") && this.u.getPort() == -1) {
            return 443;
        }
        if (this.protocol.equalsIgnoreCase("S3CRUSH") && this.u.getPort() == -1) {
            return 443;
        }
        if (this.protocol.equalsIgnoreCase("SMB") && this.u.getPort() == -1) {
            return 445;
        }
        if (this.protocol.equalsIgnoreCase("GDRIVE") && this.u.getPort() == -1) {
            return 443;
        }
        if (this.protocol.equalsIgnoreCase("AZURE") && this.u.getPort() == -1) {
            return 443;
        }
        if (this.protocol.equalsIgnoreCase("SMB3") && this.u.getPort() == -1) {
            return 445;
        }
        return this.u.getPort();
    }

    public Properties getConfig() {
        return this.configs;
    }

    public String getUsername() {
        if (this.u == null || this.u.getUserInfo() == null) {
            return "";
        }
        return VRL.vrlDecode(Common.url_decode(this.u.getUserInfo().substring(0, this.u.getUserInfo().indexOf(":"))));
    }

    public String getPassword() {
        String password;
        String string = password = this.u == null || this.u.getUserInfo() == null ? "" : VRL.vrlDecode(Common.url_decode(this.u.getUserInfo().substring(this.u.getUserInfo().indexOf(":") + 1)));
        if (password.startsWith("DES:") || password.startsWith("DES~") || password.startsWith("DES_")) {
            try {
                password = Common.encryptDecrypt(password.substring(4), false);
            }
            catch (Exception e) {
                Common.log("SERVER", 0, e);
            }
        }
        return password;
    }

    public String toString() {
        return this.url;
    }

    public String safe() {
        try {
            return Common.replace_str(VRL.vrlDecode(Common.url_decode(this.url)), this.getPassword(), "****");
        }
        catch (Exception e) {
            return Common.replace_str(VRL.vrlDecode(this.url), this.getPassword(), "****");
        }
    }

    public static Properties safe(Properties item) {
        if (item == null) {
            return null;
        }
        Properties item2 = (Properties)item.clone();
        if (item2.containsKey("vItem")) {
            item2.remove("vItem");
        }
        if (item2.containsKey("url")) {
            item2.put("url", new VRL(item2.getProperty("url")).safe());
        }
        if (System.getProperty("crushftp.v10_beta", "false").equals("true") && item2.containsKey("privs")) {
            String path;
            int key_index;
            String privs = item2.getProperty("privs");
            if (privs.contains("pgpPublicKeyUploadPath")) {
                key_index = privs.indexOf("(pgpPublicKeyUploadPath=") + "(pgpPublicKeyUploadPath=".length();
                String pgpPublicKeyUploadPath = privs.substring(key_index, privs.indexOf(")", key_index));
                path = new VRL(pgpPublicKeyUploadPath).getPath();
                privs = Common.replace_str(privs, pgpPublicKeyUploadPath, path);
            }
            if (privs.contains("pgpPublicKeyDownloadPath")) {
                key_index = privs.indexOf("(pgpPublicKeyDownloadPath=") + "(pgpPublicKeyDownloadPath=".length();
                String pgpPublicKeyDownloadPath = privs.substring(key_index, privs.indexOf(")", key_index));
                path = new VRL(pgpPublicKeyDownloadPath).getPath();
                privs = Common.replace_str(privs, pgpPublicKeyDownloadPath, path);
            }
            if (privs.contains("pgpPrivateKeyDownloadPath")) {
                key_index = privs.indexOf("(pgpPrivateKeyDownloadPath=") + "(pgpPrivateKeyDownloadPath=".length();
                String pgpPrivateKeyDownloadPath = privs.substring(key_index, privs.indexOf(")", key_index));
                path = new VRL(pgpPrivateKeyDownloadPath).getPath();
                privs = Common.replace_str(privs, pgpPrivateKeyDownloadPath, path);
            }
            if (privs.contains("pgpPrivateKeyUploadPath")) {
                key_index = privs.indexOf("(pgpPrivateKeyUploadPath=") + "(pgpPrivateKeyUploadPath=".length();
                String pgpPrivateKeyUploadPath = privs.substring(key_index, privs.indexOf(")", key_index));
                path = new VRL(pgpPrivateKeyUploadPath).getPath();
                privs = Common.replace_str(privs, pgpPrivateKeyUploadPath, path);
            }
            item2.put("privs", privs);
        }
        if (System.getProperty("crushftp.v10_beta", "false").equals("true") && item2.containsKey("ssh_private_key") && !item2.getProperty("ssh_private_key", "").equals("")) {
            item2.put("ssh_private_key", new VRL(item.getProperty("ssh_private_key")).safe());
        }
        if (item2.containsKey("secretKeyID")) {
            item2.put("secretKeyID", "********");
        }
        if (item2.containsKey("secretKey")) {
            item2.put("secretKey", "********");
        }
        if (item2.containsKey("onedriveTenant")) {
            item2.put("onedriveTenant", "********");
        }
        return item2;
    }

    public static String getActiveUrl(String s, boolean inc) {
        String hosts = s;
        try {
            hosts = s.substring(s.indexOf("@") + 1, s.indexOf("/", s.indexOf("@") + 1)).trim();
        }
        catch (Exception e) {
            Common.log("SERVER", 3, e);
            return s;
        }
        if (hosts.indexOf(",") >= 0) {
            int index = Integer.parseInt(host_config_val.getProperty(hosts, "0"));
            if (inc) {
                if (index == 0 && hosts.split(",").length == 1) {
                    return null;
                }
                if (++index >= hosts.split(",").length) {
                    index = 0;
                }
                host_config_val.put(hosts, String.valueOf(index));
            }
            return Common.replace_str(s, hosts, hosts.split(",")[index]).trim();
        }
        if (inc) {
            return null;
        }
        return s;
    }
}

