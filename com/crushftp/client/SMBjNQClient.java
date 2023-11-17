/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import com.visuality.nq.auth.Credentials;
import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.Directory;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.MountParams;
import com.visuality.nq.client.SmbInputStream;
import com.visuality.nq.client.SmbOutputStream;
import com.visuality.nq.common.Capture;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.config.Config;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class SMBjNQClient
extends GenericClient {
    String root_path = null;
    StringBuffer die_now = new StringBuffer();
    Credentials credentials = null;
    Mount mount = null;
    String share = null;
    static boolean JNQ_IS_LOGGING = false;
    static TraceLog customLogger = new JNQLogger();

    public SMBjNQClient(String url, String header, Vector log) {
        super(header, log);
        VRL vrl;
        if (!System.getProperty("crushftp.smb3_kerberos_kdc", "").equals("")) {
            try {
                Config.jnq.set("KDC", System.getProperty("crushftp.smb3_kerberos_kdc", ""));
                Config.jnq.set("REALM", System.getProperty("crushftp.smb3_kerberos_realm", ""));
            }
            catch (NqException e) {
                this.log(e);
            }
        }
        this.fields = new String[]{"username", "password", "clientid", "domain", "smb_path", "use_dmz", "timeout", "count_dir_items"};
        if (url.indexOf("@\\") >= 0) {
            url = url.replace('\\', '/');
        }
        if (url.indexOf("@//") >= 0) {
            url = Common.replace_str(url, "@//", "@");
        }
        if (url.toUpperCase().startsWith("SMB3:/") && !url.toUpperCase().startsWith("SMB3://")) {
            url = "SMB3://" + url.substring(6);
        }
        if ((vrl = new VRL(url)) != null && vrl.getPath().length() > 0 && !vrl.getPath().equals("/")) {
            String[] url_parts = url.split("@");
            String temp_url = new VRL(String.valueOf(url_parts[0]) + "@" + Common.url_decode(url_parts[1])).toString();
            this.root_path = String.valueOf(Common.first(vrl.getPath())) + "/";
            url = String.valueOf(temp_url.substring(0, temp_url.indexOf(this.root_path))) + this.root_path;
        }
        this.config.put("protocol", "SMB3");
        this.url = url;
        Enumeration<Object> keys = System.getProperties().keys();
        while (keys.hasMoreElements()) {
            String key = "" + keys.nextElement();
            String val = String.valueOf(System.getProperty(key));
            if (key.startsWith("crushftp.jnq.DNS")) {
                if (val.equals("true")) {
                    Config.jnq.setNE(key.substring("crushftp.jnq.".length()), true);
                    continue;
                }
                if (val.equals("false")) {
                    Config.jnq.setNE(key.substring("crushftp.jnq.".length()), false);
                    continue;
                }
                if (val.matches("\\d+")) {
                    Config.jnq.setNE(key.substring("crushftp.jnq.".length()), Integer.parseInt(val));
                    continue;
                }
                Config.jnq.setNE("DNS", val);
                continue;
            }
            if (!key.startsWith("crushftp.jnq.")) continue;
            try {
                if (val.equals("true")) {
                    Config.jnq.set(key.substring("crushftp.jnq.".length()), true);
                    continue;
                }
                if (val.equals("false")) {
                    Config.jnq.set(key.substring("crushftp.jnq.".length()), false);
                    continue;
                }
                if (val.matches("\\d+")) {
                    Config.jnq.set(key.substring("crushftp.jnq.".length()), Integer.parseInt(val));
                    continue;
                }
                Config.jnq.set(key.substring("crushftp.jnq.".length()), val);
            }
            catch (NqException e) {
                this.log(e);
            }
        }
        try {
            if (System.getProperty("crushftp.smb3_kerberos_realm", "").equalsIgnoreCase("OFF")) {
                Config.jnq.set("MAXSECURITYLEVEL", 3);
            }
        }
        catch (NqException e) {
            this.log(e);
        }
        try {
            if (System.getProperty("crushftp.dfs_default_enabled", "true").equals("true")) {
                Config.jnq.set("DFSENABLE", true);
            }
            if (new java.io.File("jnq.debug").exists()) {
                if (!JNQ_IS_LOGGING) {
                    JNQ_IS_LOGGING = true;
                    Config.jnq.set("LOGFILE", "jnq.log");
                    Config.jnq.set("LOGTHRESHOLD", 2000);
                    Config.jnq.set("LOGMAXRECORDSINFILE", 1000000);
                    Config.jnq.set("CAPTUREFILE", "jnq.pcap");
                    Config.jnq.set("CAPTUREMAXRECORDSINFILE", 1000000);
                    Config.jnq.set("ENABLECAPTUREPACKETS", true);
                    TraceLog.set(customLogger);
                    Config.jnq.set("LOGTOCONSOLE", false);
                    Config.jnq.set("LOGTOFILE", true);
                    TraceLog.get().start();
                    Capture.start();
                }
            } else if (JNQ_IS_LOGGING) {
                JNQ_IS_LOGGING = false;
                Config.jnq.set("LOGTOFILE", false);
                Config.jnq.set("ENABLECAPTUREPACKETS", false);
                Capture.stop();
                TraceLog.get().stop();
            }
        }
        catch (NqException e) {
            this.log(e);
        }
    }

    public String simpleUrl(String s) {
        if (s.indexOf("@") >= 0) {
            s = "SMB3://" + s.substring(s.indexOf("@") + 1);
        }
        return s;
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        int loops;
        String domain = null;
        username = VRL.vrlDecode(username);
        password = VRL.vrlDecode(password);
        if (username.indexOf("\\") >= 0) {
            domain = username.substring(0, username.indexOf("\\"));
            username = username.substring(username.indexOf("\\") + 1);
        }
        this.config.put("username", username);
        this.config.put("password", password);
        if (domain != null) {
            this.config.put("domain", domain);
        }
        if (clientid != null) {
            this.config.put("clientid", clientid);
        }
        if (domain == null && this.config.containsKey("domain")) {
            domain = this.config.getProperty("domain");
        }
        if ((loops = 0) < 5) {
            try {
                this.credentials = new PasswordCredentials(username, password, domain);
                this.connect(username, password, domain);
            }
            catch (Exception e) {
                if (loops >= 4) {
                    throw new Exception("login failed:" + e);
                }
                this.log(e);
                throw new Exception("login failed:" + e);
            }
        }
        this.config.put("logged_out", "false");
        return "Success";
    }

    private void connect(String username, String password, String domain) throws Exception {
        String current_name = Thread.currentThread().getName();
        if (new VRL(this.url).getPath().equals("/")) {
            if (!this.config.getProperty("smb_path", "").equals("")) {
                if (!this.url.endsWith("/")) {
                    this.url = String.valueOf(this.url) + "/";
                }
                this.url = String.valueOf(this.url) + (this.config.getProperty("smb_path", "").startsWith("/") ? this.config.getProperty("smb_path", "").substring(1) : this.config.getProperty("smb_path", ""));
                this.root_path = new VRL(this.url).getPath();
            } else {
                throw new IOException("Share name is required for SMB3 but none found on the URL.");
            }
        }
        final VRL u = new VRL(this.url);
        this.share = Common.first(u.getPath()).substring(1);
        MountParams mountParams = new MountParams();
        String mount_host = null;
        if (!(this.config.getProperty("use_dmz", "false").equals("false") || this.config.getProperty("use_dmz", "no").equals("no") || this.config.getProperty("use_dmz", "no").equals("null") || this.config.getProperty("use_dmz", "").equals(""))) {
            this.log("SMB_CLIENT:Connecting to:" + u.getHost() + ":" + u.getPort());
            final ServerSocket ss = new ServerSocket(0, 10, InetAddress.getByName("127.0.0.1"));
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        StringBuffer die_now2 = SMBjNQClient.this.die_now;
                        ss.setSoTimeout(5000);
                        while (die_now2.length() == 0) {
                            try {
                                Socket sock2 = ss.accept();
                                Socket sock1 = Common.getSocket("SMB3", u, SMBjNQClient.this.config.getProperty("use_dmz", "false"), "", Integer.parseInt(SMBjNQClient.this.config.getProperty("timeout", "20000")));
                                sock1.setSoTimeout(600000);
                                if (Integer.parseInt(SMBjNQClient.this.config.getProperty("timeout", "0")) > 0) {
                                    sock1.setSoTimeout(Integer.parseInt(SMBjNQClient.this.config.getProperty("timeout", "0")));
                                }
                                SMBjNQClient.this.log("SMB_CLIENT:Socket connected to:" + u.getHost() + ":" + u.getPort());
                                Common.streamCopier(sock1.getInputStream(), sock2.getOutputStream(), true, true, true);
                                Common.streamCopier(sock2.getInputStream(), sock1.getOutputStream(), true, true, true);
                            }
                            catch (SocketTimeoutException socketTimeoutException) {
                                // empty catch block
                            }
                        }
                        ss.close();
                    }
                    catch (Exception e) {
                        SMBjNQClient.this.log("SMB_CLIENT", 1, e);
                    }
                }
            }, "SMBv3 proxy thread for " + u.getHost() + ":" + u.getPort());
            mount_host = "127.0.0.1";
            mountParams.port = ss.getLocalPort();
        } else {
            mount_host = u.getHost();
            mountParams.port = u.getPort();
        }
        Exception ee = null;
        int x = 0;
        while (x < 2) {
            Thread.currentThread().setName(String.valueOf(current_name) + ":" + u.safe());
            try {
                this.mount = new Mount(mount_host, this.share, this.credentials, mountParams);
                this.mount.setRetryCount(5);
                this.mount.setRetryTimeout(100);
                this.log("SMB_CLIENT:Authenticating to:" + u.safe() + " params:" + "getRetryCount=" + this.mount.getRetryCount() + " getRetryTimeout=" + this.mount.getRetryTimeout() + " getMaxSmbTimeout=" + Client.getMaxSmbTimeout() + " getSmbTimeout=" + Client.getSmbTimeout() + " getBackupListTimeout=" + Client.getBackupListTimeout());
                Client.checkCredentials(mount_host, this.credentials);
                ee = null;
                break;
            }
            catch (Exception e) {
                ee = e;
                mountParams.maxDialect = 770;
            }
            finally {
                Thread.currentThread().setName(current_name);
            }
            ++x;
        }
        if (ee != null) {
            throw ee;
        }
    }

    @Override
    public void logout() {
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        if (SMBjNQClient.this.mount != null) {
                            SMBjNQClient.this.mount.close();
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            });
        }
        catch (Exception e) {
            this.log(e);
        }
        this.die_now.append("die");
        this.die_now = new StringBuffer();
        this.config.put("logged_out", "true");
        try {
            this.close();
        }
        catch (Exception e) {
            this.log(e);
        }
    }

    private String getSharePath(VRL vrl, String path) {
        String path2 = vrl.getPath().substring(this.share.length() + 1);
        if (path2.startsWith("/")) {
            path2 = path2.substring(1);
        }
        if (path2.endsWith("/")) {
            path2 = path2.substring(0, path2.length() - 1);
        }
        return path2;
    }

    @Override
    public Properties stat(String path) throws Exception {
        VRL vrl;
        String share_path;
        if (!(path = path.replace('\\', '/')).startsWith("/")) {
            path = "/" + path;
        }
        if (File.isExist(this.mount, share_path = this.getSharePath(vrl = new VRL(String.valueOf(this.url) + path.substring(this.root_path.length())), path), false)) {
            File.Info info = File.getInfo(this.mount, share_path);
            return this.stat(path, info);
        }
        if (System.getProperty("crushftp.isTestCall" + Thread.currentThread().getId(), "false").equals("true")) {
            throw new Exception("Item not found...");
        }
        return null;
    }

    public Properties stat(String path, File.Info info) throws Exception {
        if (!(path = path.replace('\\', '/')).startsWith("/")) {
            path = "/" + path;
        }
        VRL vrl = new VRL(String.valueOf(this.url) + path.substring(this.root_path.length()));
        String share_path = this.getSharePath(vrl, path);
        String name = vrl.getName();
        Properties dir_item = new Properties();
        dir_item.put("name", name);
        dir_item.put("type", info.isDirectory() ? "DIR" : "FILE");
        dir_item.put("permissions", String.valueOf(info.isDirectory() ? "d" : "-") + "rwxrwxrwx");
        dir_item.put("size", String.valueOf(info.getEof()));
        this.log("SMB_CLIENT", 2, "Got stat name:path:" + name + ":" + path + ":Dir=" + info.isDirectory());
        if (this.config.getProperty("count_dir_items", "false").equals("true") && info.isDirectory()) {
            int i = 0;
            Directory dir = new Directory(this.mount, share_path);
            Directory.Entry item = null;
            while ((item = dir.next()) != null) {
                if (item.name.startsWith(".")) continue;
                ++i;
            }
            dir.close();
            dir_item.put("size", String.valueOf(i));
        }
        dir_item.put("url", "" + vrl);
        dir_item.put("link", "false");
        dir_item.put("num_items", "1");
        dir_item.put("owner", "user");
        dir_item.put("group", "group");
        dir_item.put("protocol", "file");
        dir_item.put("root_dir", Common.all_but_last(vrl.getPath()));
        this.setFileDateInfo(info, dir_item);
        return dir_item;
    }

    private void setFileDateInfo(File.Info info, Properties dir_item) {
        Date itemDate = info.getLastWriteTime();
        dir_item.put("modified", String.valueOf(itemDate.getTime()));
        dir_item.put("month", months[Integer.parseInt(this.mm.format(itemDate))]);
        dir_item.put("day", this.dd.format(itemDate));
        String time_or_year = this.hhmm.format(itemDate);
        if (!this.yyyy.format(itemDate).equals(this.yyyy.format(new Date())) || System.getProperty("crushftp.ls.year", "false").equals("true")) {
            time_or_year = this.yyyy.format(itemDate);
        }
        dir_item.put("time_or_year", time_or_year);
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        if (!(path = path.replace('\\', '/')).startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        VRL vrl = new VRL(String.valueOf(this.url) + path.substring(this.root_path.length()));
        try {
            String share_path = this.getSharePath(vrl, path);
            if (share_path.endsWith("/")) {
                share_path = share_path.substring(0, share_path.length() - 1);
            }
            this.log("SMB_CLIENT:Getting list for:" + path);
            this.log("SMB_CLIENT", 2, "Getting list for VRL:" + vrl.safe());
            Directory dir = new Directory(this.mount, share_path);
            Directory.Entry item = null;
            int x = 0;
            while ((item = dir.next()) != null) {
                try {
                    if (this.config.getProperty("logged_out", "false").equals("true")) {
                        throw new Exception("Error: Cancel dir listing. The client is already closed.");
                    }
                    String tempName = item.name;
                    String tempPath = String.valueOf(path) + item.name;
                    if (item.info.isDirectory()) {
                        tempPath = String.valueOf(tempPath) + "/";
                    }
                    this.log("SMB_CLIENT", 2, "Got list item:" + x + ":" + tempName + " isHidden:" + item.info.isHidden());
                    if (item.info.isHidden() || tempName.equals(".") || tempName.equals("..")) continue;
                    list.addElement(this.stat(tempPath, item.info));
                }
                catch (Exception e) {
                    this.log("SMB_CLIENT", 1, String.valueOf(x + 1) + ":Invalid file, or dead alias:" + item.name + ":" + e);
                    this.log("SMB_CLIENT", 1, e);
                    this.log(String.valueOf(x + 1) + ":Invalid file, or dead alias:" + item.name + ":" + e);
                }
                ++x;
            }
            dir.close();
        }
        catch (Exception e) {
            if (System.getProperty("crushftp.file_client_not_found_error", "true").equals("false")) {
                this.log("SMB_CLIENT", 0, "Ignoring file_client_not_found_error for folder:" + path + ":" + e);
                return list;
            }
            throw e;
        }
        return list;
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        if (!(path = path.replace('\\', '/')).startsWith("/")) {
            path = "/" + path;
        }
        VRL vrl = new VRL(String.valueOf(this.url) + path.substring(this.root_path.length()));
        String share_path = this.getSharePath(vrl, path);
        this.log("SMB_CLIENT:Downloading :" + path);
        File.Params fileParams = new File.Params(1, 7, 1, false);
        File f = new File(this.mount, share_path, fileParams);
        SmbInputStream fin = new SmbInputStream(f);
        try {
            if (startPos > 0L) {
                fin.skip(startPos);
            }
        }
        catch (Exception e) {
            ((InputStream)fin).close();
            throw e;
        }
        class InputWrapper
        extends InputStream {
            InputStream in3 = null;
            boolean closed = false;
            long pos;
            File f;
            private final /* synthetic */ long val$endPos;
            private final /* synthetic */ String val$share_path;
            private final /* synthetic */ File.Params val$fileParams;

            public InputWrapper(InputStream in3, File f, long l, long l2, String string, File.Params params) {
                this.val$endPos = l2;
                this.val$share_path = string;
                this.val$fileParams = params;
                this.pos = l;
                this.f = null;
                this.in3 = in3;
                this.f = f;
            }

            @Override
            public int read() throws IOException {
                if (this.pos == this.val$endPos && this.val$endPos >= 0L) {
                    return -1;
                }
                int i = this.in3.read();
                ++this.pos;
                return i;
            }

            @Override
            public int read(byte[] b) throws IOException {
                return this.read(b, 0, b.length);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (this.pos >= this.val$endPos && this.val$endPos >= 0L) {
                    return -1;
                }
                int i = 0;
                int x = 0;
                while (x < 3) {
                    try {
                        i = this.in3.read(b, off, len);
                        break;
                    }
                    catch (Exception e) {
                        SMBjNQClient.this.log("SMB_CLIENT", 1, e);
                        if (("" + e).contains("File is not open") || ("" + e).contains("Unable to reconnect")) {
                            try {
                                if (this.in3 != null) {
                                    this.in3.close();
                                }
                                try {
                                    this.f.close();
                                }
                                catch (NqException efc) {
                                    SMBjNQClient.this.log("SMB_CLIENT", 1, efc);
                                }
                                this.f = new File(SMBjNQClient.this.mount, this.val$share_path, this.val$fileParams);
                                this.in3 = new SmbInputStream(this.f);
                                this.in3.skip(this.pos);
                            }
                            catch (NqException e1) {
                                if (this.in3 != null) {
                                    this.in3.close();
                                }
                                if (this.f != null) {
                                    try {
                                        this.f.close();
                                    }
                                    catch (NqException e2) {
                                        SMBjNQClient.this.log("SMB_CLIENT", 1, e2);
                                    }
                                }
                                throw new IOException("" + e1);
                            }
                        }
                        throw new IOException("" + e);
                        ++x;
                    }
                }
                if (i > 0) {
                    this.pos += (long)i;
                }
                if (this.val$endPos > 0L && this.pos > this.val$endPos) {
                    i = (int)((long)i - (this.pos - this.val$endPos));
                }
                return i;
            }

            @Override
            public void close() throws IOException {
                if (this.closed) {
                    return;
                }
                this.in3.close();
                try {
                    this.f.close();
                }
                catch (NqException e) {
                    SMBjNQClient.this.log("SMB_CLIENT", 1, e);
                }
                this.closed = true;
            }
        }
        this.in = new InputWrapper(fin, f, startPos, endPos, share_path, fileParams);
        return this.in;
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        if (!(path = path.replace('\\', '/')).startsWith("/")) {
            path = "/" + path;
        }
        VRL vrl = new VRL(String.valueOf(this.url) + path.substring(this.root_path.length()));
        String share_path = this.getSharePath(vrl, path);
        this.log("SMB_CLIENT:mdtm:" + path + ":" + modified);
        File.getInfo(this.mount, share_path).setLastWriteTime(modified);
        return true;
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) {
        if (rnfr.equals(rnto)) {
            return true;
        }
        if (!(rnfr = rnfr.replace('\\', '/')).startsWith("/")) {
            rnfr = "/" + rnfr;
        }
        String share_path1 = this.getSharePath(new VRL(String.valueOf(this.url) + rnfr.substring(this.root_path.length())), rnfr);
        if (!(rnto = rnto.replace('\\', '/')).startsWith("/")) {
            rnto = "/" + rnto;
        }
        String share_path2 = this.getSharePath(new VRL(String.valueOf(this.url) + rnto.substring(this.root_path.length())), rnto);
        try {
            File.rename(this.mount, share_path1, share_path2);
            return true;
        }
        catch (Exception e) {
            this.log(e);
            return false;
        }
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        if (!(path = path.replace('\\', '/')).startsWith("/")) {
            path = "/" + path;
        }
        VRL vrl = new VRL(String.valueOf(this.url) + path.substring(this.root_path.length()));
        String share_path = this.getSharePath(vrl, path);
        this.log("SMB_CLIENT:Uploading:" + path);
        File.Params fileParams = new File.Params(11, 7, 5, false);
        if (startPos > 0L) {
            fileParams = new File.Params(13, 7, 3, false);
        }
        File f = new File(this.mount, share_path, fileParams);
        f.setPosition(startPos < 0L ? 0L : startPos);
        try {
            class OutputWrapper
            extends OutputStream {
                File f = null;
                OutputStream smb_out = null;
                boolean closed = false;
                private final /* synthetic */ String val$share_path;

                public OutputWrapper(File f, OutputStream smb_out, String string) {
                    this.val$share_path = string;
                    this.f = f;
                    this.smb_out = smb_out;
                }

                @Override
                public void write(int i) throws IOException {
                    this.write(new byte[]{(byte)i}, 0, 1);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    this.write(b, 0, b.length);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    int x = 0;
                    while (x < 3) {
                        try {
                            this.smb_out.write(b, off, len);
                            break;
                        }
                        catch (Exception e) {
                            SMBjNQClient.this.log("SMB_CLIENT", 1, e);
                            if (("" + e).contains("File is not open") || ("" + e).contains("Unable to reconnect")) {
                                try {
                                    if (this.smb_out != null) {
                                        this.smb_out.close();
                                    }
                                    try {
                                        this.f.close();
                                    }
                                    catch (NqException efc) {
                                        SMBjNQClient.this.log("SMB_CLIENT", 1, efc);
                                    }
                                    File.Params fileParams = new File.Params(13, 7, 3, false);
                                    this.f = new File(SMBjNQClient.this.mount, this.val$share_path, fileParams);
                                    this.smb_out = new SmbOutputStream(this.f);
                                }
                                catch (NqException e1) {
                                    if (this.smb_out != null) {
                                        this.smb_out.close();
                                    }
                                    if (this.f != null) {
                                        try {
                                            this.f.close();
                                        }
                                        catch (NqException e2) {
                                            SMBjNQClient.this.log("SMB_CLIENT", 1, e2);
                                        }
                                    }
                                    throw new IOException("" + e1);
                                }
                            }
                            throw new IOException(e);
                            ++x;
                        }
                    }
                }

                @Override
                public void close() throws IOException {
                    if (this.closed) {
                        return;
                    }
                    this.smb_out.close();
                    try {
                        this.f.close();
                    }
                    catch (NqException e) {
                        SMBjNQClient.this.log("SMB_CLIENT", 1, e);
                    }
                    this.closed = true;
                }
            }
            this.out = new OutputWrapper(f, new SmbOutputStream(f), share_path);
        }
        catch (Exception e) {
            this.log(e);
            try {
                f.close();
            }
            catch (NqException ee) {
                this.log("SMB_CLIENT", 1, ee);
            }
        }
        return this.out;
    }

    @Override
    public boolean delete(String path) {
        if (!(path = path.replace('\\', '/')).startsWith("/")) {
            path = "/" + path;
        }
        VRL vrl = new VRL(String.valueOf(this.url) + path.substring(this.root_path.length()));
        String share_path = this.getSharePath(vrl, path);
        Properties p = null;
        try {
            p = this.stat(path);
        }
        catch (Exception e) {
            this.log(e);
        }
        if (p == null) {
            return true;
        }
        this.log("SMB_CLIENT:Delete:" + path);
        try {
            int x = 0;
            while (x < 2) {
                try {
                    File.delete(this.mount, share_path);
                    break;
                }
                catch (NqException e) {
                    if (x > 0) {
                        throw e;
                    }
                    File file = new File(this.mount, share_path, new File.Params(40, 7, 1, false));
                    File.Info info = file.getInfo();
                    info.setAttributes(info.getAttributes() & 0xFFFFFFFE);
                    file.setInfo(info);
                    file.close();
                    ++x;
                }
            }
        }
        catch (Exception e) {
            this.log(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean makedir(String path) throws Exception {
        if (!(path = path.replace('\\', '/')).startsWith("/")) {
            path = "/" + path;
        }
        VRL vrl = new VRL(String.valueOf(this.url) + path.substring(this.root_path.length()));
        String share_path = this.getSharePath(vrl, path);
        this.log("SMB_CLIENT:makedir:" + path);
        File.Params fileParams = new File.Params(11, 7, 2, true);
        try {
            File file = new File(this.mount, share_path, fileParams);
            file.close();
        }
        catch (NqException e) {
            this.log(e);
            throw e;
        }
        return true;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public boolean makedirs(String path) throws Exception {
        String root_path2 = this.root_path;
        if (root_path2.equals(path)) {
            root_path2 = root_path2.substring(0, root_path2.lastIndexOf("/", root_path2.length() - 2) + 1);
        }
        this.log("SMB_CLIENT:makedirs:" + path);
        path = path.replace('\\', '/');
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        try {
            File.mkdirs(this.mount, path.substring(root_path2.length()));
            if (this.stat(path) == null) return false;
            return true;
        }
        catch (Exception e) {
            this.log(e);
            this.log("Trying alternate mkdirs method..." + path);
            try {
                boolean ok = true;
                String[] parts = path.substring(root_path2.length()).split("/");
                String path2 = root_path2;
                int x = 0;
                while (true) {
                    block15: {
                        if (x >= parts.length || !ok) {
                            if (!ok) return false;
                            return true;
                        }
                        path2 = String.valueOf(path2) + parts[x] + "/";
                        Properties stat2 = null;
                        try {
                            stat2 = this.stat(path2);
                        }
                        catch (Exception stat_e) {
                            this.log(stat_e);
                        }
                        if (stat2 == null) {
                            try {
                                ok = this.makedir(path2);
                            }
                            catch (Exception mkde) {
                                if (("" + mkde).contains("(0xc0000035)") && x < parts.length - 1) {
                                    ok = true;
                                    break block15;
                                }
                                ok = false;
                                throw mkde;
                            }
                        }
                    }
                    ++x;
                }
            }
            catch (Exception ee) {
                this.log(ee);
            }
        }
        return false;
    }

    @Override
    public String doCommand(String command) throws Exception {
        return "";
    }

    static class JNQLogger
    extends TraceLog {
        final String[] filesToIgnore = new String[]{"SMBjNQClient.java", "CustomLogger.java", "InternalTraceLog.java", "TraceLog.java", "NqException.java", "SmbException.java", "ClientException.java", "NetbiosException.java"};

        JNQLogger() {
        }

        @Override
        public void message(String text, int level) {
            this.logit("MESSAGE => level=" + level + ", " + text);
        }

        @Override
        public void error(String text, int level, int status) {
            this.logit("ERROR => level=" + level + ", " + text);
        }

        @Override
        public void enter(int level) {
            this.logit("ENTER => level=" + level);
        }

        @Override
        public void exit(int level) {
            this.logit("EXIT => level=" + level);
        }

        @Override
        public void start(int level) {
            this.logit("START => level=" + level);
        }

        @Override
        public void stop(int level) {
            this.logit("STOP => level=" + level);
        }

        @Override
        public void caught(Exception ex, int level) {
            if (!JNQ_IS_LOGGING) {
                return;
            }
            this.logit("EXCEPTION CAUGHT => level=" + level + ", " + ex);
            Common.log("SMB_CLIENT", 0, ex);
        }

        private void logit(String messageToLog) {
            int depth;
            if (!JNQ_IS_LOGGING) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            StackTraceElement[] stack = new Throwable().getStackTrace();
            if (stack.length > (depth = this.calculateDepth(stack)) && depth >= 0) {
                sb.append(Thread.currentThread().getId());
                sb.append(";");
                sb.append(new SimpleDateFormat("dd/MM/yyyy-HH.mm.ss.SSS").format(new Date()));
                sb.append(";");
                sb.append(stack[depth].getFileName());
                sb.append(";");
                sb.append(stack[depth].getMethodName());
                sb.append(";");
                sb.append(stack[depth].getLineNumber());
                sb.append(";");
                sb.append(messageToLog);
            }
            if (!sb.toString().equals("")) {
                Common.log("SMB_CLIENT", 0, sb.toString());
            }
        }

        @Override
        public boolean canLog(int level) {
            return JNQ_IS_LOGGING;
        }

        public int calculateDepth(StackTraceElement[] stack) {
            int depth = 1;
            while (this.fileNameMatch(stack[depth].getFileName())) {
                ++depth;
            }
            return depth;
        }

        boolean fileNameMatch(String fname) {
            String[] stringArray = this.filesToIgnore;
            int n = this.filesToIgnore.length;
            int n2 = 0;
            while (n2 < n) {
                String f = stringArray[n2];
                if (fname.equals(f)) {
                    return true;
                }
                ++n2;
            }
            return false;
        }
    }
}

