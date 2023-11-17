/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hierynomus.msdtyp.AccessMask
 *  com.hierynomus.msdtyp.FileTime
 *  com.hierynomus.msfscc.FileAttributes
 *  com.hierynomus.msfscc.fileinformation.FileAllInformation
 *  com.hierynomus.msfscc.fileinformation.FileBasicInformation
 *  com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation
 *  com.hierynomus.msfscc.fileinformation.FileSettableInformation
 *  com.hierynomus.mssmb2.SMB2CreateDisposition
 *  com.hierynomus.mssmb2.SMB2ShareAccess
 *  com.hierynomus.protocol.commons.EnumWithValue
 *  com.hierynomus.protocol.commons.EnumWithValue$EnumUtils
 *  com.hierynomus.protocol.transport.TransportException
 *  com.hierynomus.smbj.SMBClient
 *  com.hierynomus.smbj.SmbConfig
 *  com.hierynomus.smbj.SmbConfig$Builder
 *  com.hierynomus.smbj.auth.AuthenticationContext
 *  com.hierynomus.smbj.connection.Connection
 *  com.hierynomus.smbj.session.Session
 *  com.hierynomus.smbj.share.Directory
 *  com.hierynomus.smbj.share.DiskShare
 *  com.hierynomus.smbj.share.File
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msdtyp.FileTime;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileBasicInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.FileSettableInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.protocol.commons.EnumWithValue;
import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.Directory;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class SMB4jClient
extends GenericClient {
    int lsBytesRead = 0;
    Properties dirCache = new Properties();
    StringBuffer die_now = new StringBuffer();
    SMBClient client = null;
    Session session = null;

    public SMB4jClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"username", "password", "clientid", "domain", "count_dir_items", "dfs_enabled", "read_buffer_size", "write_buffer_size", "timeout", "write_timeout", "read_timeout", "smb3_required_signing", "use_dmz", "recurse_delete"};
        if (url.indexOf("@\\") >= 0) {
            url = url.replace('\\', '/');
        }
        if (url.indexOf("@//") >= 0) {
            url = Common.replace_str(url, "@//", "@");
        }
        if (url.toUpperCase().startsWith("SMB3:/") && !url.toUpperCase().startsWith("SMB3://")) {
            url = "SMB3://" + url.substring(6);
        }
        this.url = url;
        this.config.put("protocol", "SMB3");
    }

    public String simpleUrl(String s) {
        if (s.indexOf("@") >= 0) {
            s = "SMB3://" + s.substring(s.indexOf("@") + 1);
        }
        return s;
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
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
        int loops = 0;
        while (loops < 5) {
            try {
                SmbConfig.Builder builder = SmbConfig.builder();
                builder = builder.withDfsEnabled(this.config.getProperty("dfs_enabled", System.getProperty("crushftp.dfs_default_enabled", "true")).equals("true"));
                if (!this.config.getProperty("read_buffer_size", "").equals("")) {
                    builder = builder.withReadBufferSize(Integer.parseInt(this.config.getProperty("read_buffer_size", "")));
                }
                if (!this.config.getProperty("write_buffer_size", "").equals("")) {
                    builder = builder.withWriteBufferSize(Integer.parseInt(this.config.getProperty("write_buffer_size", "")));
                }
                if (!this.config.getProperty("timeout", "").equals("")) {
                    builder = builder.withSoTimeout(Long.parseLong(this.config.getProperty("timeout")), TimeUnit.MILLISECONDS);
                    builder = builder.withTimeout(Long.parseLong(this.config.getProperty("timeout")), TimeUnit.MILLISECONDS);
                }
                if (!this.config.getProperty("write_timeout", "").equals("")) {
                    builder = builder.withWriteTimeout(Long.parseLong(this.config.getProperty("write_timeout")), TimeUnit.MILLISECONDS);
                }
                if (!this.config.getProperty("read_timeout", "").equals("")) {
                    builder = builder.withReadTimeout(Long.parseLong(this.config.getProperty("read_timeout")), TimeUnit.MILLISECONDS);
                }
                if (!this.config.getProperty("smb3_required_signing", "false").equals("true")) {
                    builder = builder.withSigningRequired(true);
                }
                this.client = new SMBClient(builder.withMultiProtocolNegotiate(true).build());
                this.reconnect();
                break;
            }
            catch (Exception e) {
                if (loops >= 4) {
                    throw new Exception("login failed:" + e);
                }
                if (!(e instanceof TransportException)) {
                    throw new Exception("login failed:" + e);
                }
                ++loops;
            }
        }
        return "";
    }

    private void reconnect() throws Exception {
        try {
            this.logout();
            this.session = null;
            this.connect(this.config.getProperty("username", ""), this.config.getProperty("password", ""), this.config.getProperty("domain", ""));
        }
        catch (Exception e) {
            throw new Exception("login failed:" + e);
        }
    }

    private void connect(String username, String password, String domain) throws Exception {
        Connection connection = null;
        final VRL u = new VRL(this.url);
        if (!(this.config.getProperty("use_dmz", "false").equals("false") || this.config.getProperty("use_dmz", "no").equals("no") || this.config.getProperty("use_dmz", "no").equals("null") || this.config.getProperty("use_dmz", "").equals(""))) {
            this.log("SMB_CLIENT:Connecting to:" + u.getHost() + ":" + u.getPort());
            final ServerSocket ss = new ServerSocket(0, 10, InetAddress.getByName("127.0.0.1"));
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        StringBuffer die_now2 = SMB4jClient.this.die_now;
                        ss.setSoTimeout(5000);
                        while (die_now2.length() == 0) {
                            try {
                                Socket sock2 = ss.accept();
                                Socket sock1 = Common.getSocket("SMB3", u, SMB4jClient.this.config.getProperty("use_dmz", "false"), "", Integer.parseInt(SMB4jClient.this.config.getProperty("timeout", "20000")));
                                sock1.setSoTimeout(600000);
                                if (Integer.parseInt(SMB4jClient.this.config.getProperty("timeout", "0")) > 0) {
                                    sock1.setSoTimeout(Integer.parseInt(SMB4jClient.this.config.getProperty("timeout", "0")));
                                }
                                SMB4jClient.this.log("SMB_CLIENT:Socket connected to:" + u.getHost() + ":" + u.getPort());
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
                        SMB4jClient.this.log("SMB_CLIENT", 1, e);
                    }
                }
            }, "SMBv3 proxy thread for " + u.getHost() + ":" + u.getPort());
            connection = this.client.connect("127.0.0.1", ss.getLocalPort());
        } else {
            connection = this.client.connect(u.getHost(), 445);
        }
        AuthenticationContext ac = new AuthenticationContext(username, password.toCharArray(), domain);
        if (this.session != null) {
            this.session.logoff();
            this.die_now.append("die");
            this.die_now = new StringBuffer();
        }
        this.session = connection.authenticate(ac);
        this.log("SMB_CLIENT:Authenticating to:" + username + "@" + u.getHost() + ":" + u.getPort());
    }

    @Override
    public void logout() {
        block8: {
            Thread t = null;
            try {
                try {
                    t = new Thread(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                if (SMB4jClient.this.session != null) {
                                    SMB4jClient.this.session.logoff();
                                }
                            }
                            catch (TransportException transportException) {
                                // empty catch block
                            }
                        }
                    });
                    t.start();
                    t.join(10000L);
                }
                catch (Exception e) {
                    this.log(e);
                    if (t.isAlive()) {
                        this.log("SMB3 logout took longer than 10 seconds:" + Thread.currentThread().getName());
                        t.interrupt();
                    }
                    break block8;
                }
            }
            catch (Throwable throwable) {
                if (t.isAlive()) {
                    this.log("SMB3 logout took longer than 10 seconds:" + Thread.currentThread().getName());
                    t.interrupt();
                }
                throw throwable;
            }
            if (t.isAlive()) {
                this.log("SMB3 logout took longer than 10 seconds:" + Thread.currentThread().getName());
                t.interrupt();
            }
        }
        this.die_now.append("die");
        this.die_now = new StringBuffer();
    }

    @Override
    public Properties stat(String path) throws Exception {
        return this.stat(path, false);
    }

    /*
     * Unable to fully structure code
     */
    public Properties stat(String path, boolean throw_errors) throws Exception {
        block27: {
            block28: {
                block25: {
                    block26: {
                        block23: {
                            block24: {
                                vrl = new VRL(String.valueOf(this.url) + path.substring(1));
                                if (!this.session.getConnection().isConnected()) {
                                    this.reconnect();
                                }
                                share = null;
                                final_error = null;
                                x = 0;
                                while (x < 5) {
                                    final_error = null;
                                    try {
                                        share = (DiskShare)this.session.connectShare(Common.replace_str(Common.first(vrl.getPath()), "/", ""));
                                        break;
                                    }
                                    catch (Exception e) {
                                        final_error = e;
                                        this.log("SMB_CLIENT", 1, e);
                                        try {
                                            Thread.sleep(3000L);
                                        }
                                        catch (Exception var8_11) {
                                            // empty catch block
                                        }
                                        this.reconnect();
                                        ++x;
                                    }
                                }
                                if (throw_errors && final_error != null) {
                                    throw final_error;
                                }
                                share_part = Common.first(path);
                                path_offset = share_part.length() + 1;
                                path2 = path;
                                if (path.endsWith("/")) {
                                    path2 = path2.substring(0, path2.length() - 1);
                                }
                                if (path_offset != path.length()) break block23;
                                name = share.getFileInformation("").getNameInformation();
                                dir_item = new Properties();
                                dir_item.put("type", "DIR");
                                dir_item.put("permissions", "drwxrwxrwx");
                                dir_item.put("size", "1");
                                this.log("SMB_CLIENT", 2, "Got stat name:path:" + name + ":" + path + ":Dir=true");
                                if (this.config.getProperty("count_dir_items", "false").equals("true")) {
                                    i = 0;
                                    list2 = share.list("");
                                    x = 0;
                                    while (list2 != null && x < list2.size()) {
                                        fibdi = (FileIdBothDirectoryInformation)list2.get(x);
                                        if (!fibdi.getFileName().startsWith(".")) {
                                            ++i;
                                        }
                                        ++x;
                                    }
                                    dir_item.put("size", String.valueOf(i));
                                }
                                dir_item.put("url", String.valueOf(this.url) + path.substring(1));
                                dir_item.put("link", "false");
                                dir_item.put("num_items", "1");
                                dir_item.put("owner", "user");
                                dir_item.put("group", "group");
                                dir_item.put("protocol", "file");
                                dir_item.put("root_dir", Common.all_but_last(new VRL(dir_item.getProperty("url")).getPath()));
                                this.setFileDateInfo(share.getFileInformation(""), dir_item);
                                this.log("SMB_CLIENT", 2, "Stat: Found. Folder: " + path);
                                var16_21 = dir_item;
                                if (share == null) break block24;
                                share.close();
                            }
                            return var16_21;
                        }
                        if (!share.folderExists(path2.substring(path_offset).replace('/', '\\'))) break block25;
                        this.log("SMB_CLIENT", 2, "Stat: Found. Folder: " + path);
                        test = share.getFileInformation(path2.substring(path_offset).replace('/', '\\'));
                        var16_22 = this.stat(test, path2, share);
                        if (share == null) break block26;
                        share.close();
                    }
                    return var16_22;
                }
                if (!share.fileExists(path2.substring(path_offset).replace('/', '\\'))) break block27;
                this.log("SMB_CLIENT", 2, "Stat: Found. File: " + path);
                test = share.getFileInformation(path2.substring(path_offset).replace('/', '\\'));
                var16_23 = this.stat(test, path2, share);
                if (share == null) break block28;
                share.close();
            }
            return var16_23;
        }
        try {
            try {
                this.log("SMB_CLIENT", 1, "Stat: Not found. Path: " + path);
            }
            catch (Exception e) {
                this.log("SMB_CLIENT", 1, e);
                if (throw_errors) {
                    throw e;
                }
                if (System.getProperty("crushftp.isTestCall" + Thread.currentThread().getId(), "false").equals("true")) {
                    throw new Exception("Item not found...");
                }
                if (share == null) ** GOTO lbl115
                share.close();
            }
        }
        catch (Throwable var15_24) {
            if (share != null) {
                share.close();
            }
            throw var15_24;
        }
        if (share != null) {
            share.close();
        }
lbl115:
        // 5 sources

        if (System.getProperty("crushftp.isTestCall" + Thread.currentThread().getId(), "false").equals("true")) {
            throw new Exception("Item not found...");
        }
        return null;
    }

    public Properties stat(FileAllInformation test, String path, DiskShare share) throws Exception {
        String share_part = Common.first(path);
        int path_offset = share_part.length() + 1;
        Properties dir_item = new Properties();
        String name = Common.last(test.getNameInformation());
        if (name.equals("")) {
            name = path;
            if (name.endsWith("/")) {
                name = name.substring(0, name.length() - 1);
            }
            name = Common.last(name);
        }
        dir_item.put("name", name.replaceAll("\r", "%0A").replaceAll("\n", "%0D"));
        dir_item.put("size", "0");
        if (test.getStandardInformation().isDirectory()) {
            if (!path.endsWith("/")) {
                path = String.valueOf(path) + "/";
            }
            dir_item.put("type", "DIR");
            dir_item.put("permissions", "drwxrwxrwx");
            dir_item.put("size", "1");
            this.log("SMB_CLIENT", 2, "Got stat name:path:" + name + ":" + path + ":Dir=true");
            if (this.config.getProperty("count_dir_items", "false").equals("true")) {
                int i = 0;
                List list2 = share.list(path.substring(path_offset, path.length() - 1).replace('/', '\\'));
                int x = 0;
                while (list2 != null && x < list2.size()) {
                    FileIdBothDirectoryInformation fibdi = (FileIdBothDirectoryInformation)list2.get(x);
                    if (!fibdi.getFileName().startsWith(".")) {
                        ++i;
                    }
                    ++x;
                }
                dir_item.put("size", String.valueOf(i));
            }
            if (!path.endsWith("/")) {
                path = String.valueOf(path) + "/";
            }
        } else {
            this.log("SMB_CLIENT", 2, "Got stat name:path:" + name + ":" + path + ":Dir=false");
            dir_item.put("type", "FILE");
            dir_item.put("permissions", "-rwxrwxrwx");
            dir_item.put("size", String.valueOf(this.getSize(test)));
        }
        dir_item.put("url", String.valueOf(this.url) + path.substring(1));
        dir_item.put("link", "false");
        dir_item.put("num_items", "1");
        dir_item.put("owner", "user");
        dir_item.put("group", "group");
        dir_item.put("protocol", "file");
        dir_item.put("root_dir", Common.all_but_last(new VRL(dir_item.getProperty("url")).getPath()));
        this.setFileDateInfo(test, dir_item);
        return dir_item;
    }

    private long getSize(FileAllInformation test) {
        return test.getStandardInformation().getEndOfFile();
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        if (!path.replace('\\', '/').endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        this.log("SMB_CLIENT:Getting list for:" + path);
        VRL vrl = new VRL(String.valueOf(this.url) + path.substring(1));
        String share_part = Common.first(vrl.getPath());
        this.log("SMB_CLIENT", 2, "Getting list for share_part:" + share_part);
        this.log("SMB_CLIENT", 2, "Getting list for VRL:" + vrl.safe());
        if (!this.session.getConnection().isConnected()) {
            this.reconnect();
        }
        DiskShare share = null;
        int x = 0;
        while (x < 5) {
            try {
                share = (DiskShare)this.session.connectShare(Common.replace_str(share_part, "/", ""));
                break;
            }
            catch (Exception e) {
                this.log("SMB_CLIENT", 1, e);
                this.log(e);
                this.log("Retrying in 3 seconds due to prior error...");
                try {
                    Thread.sleep(3000L);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                this.reconnect();
                ++x;
            }
        }
        String path2 = "";
        try {
            this.dirCache.clear();
            int path_offset = share_part.length() + 1;
            path2 = path.substring(path_offset);
            if (path2.endsWith("/")) {
                path2 = path2.substring(0, path2.length() - 1);
            }
            this.log("SMB_CLIENT", 2, "Getting list for path2:" + path2);
            List list2 = share.list(path2.replace('/', '\\'));
            int x2 = 0;
            while (x2 < list2.size()) {
                FileIdBothDirectoryInformation item = (FileIdBothDirectoryInformation)list2.get(x2);
                try {
                    String tempName = item.getFileName();
                    this.log("SMB_CLIENT", 2, "Got list item:" + x2 + ":" + tempName);
                    if (!(EnumWithValue.EnumUtils.isSet((long)item.getFileAttributes(), (EnumWithValue)FileAttributes.FILE_ATTRIBUTE_HIDDEN) || tempName.equals(".") || tempName.equals(".."))) {
                        String tempPath = String.valueOf(path.substring(path_offset).replace('/', '\\')) + tempName;
                        this.log("SMB_CLIENT", 2, "Getting info for path item:" + x2 + ":" + tempPath);
                        FileAllInformation fai = share.getFileInformation(tempPath);
                        if (fai.getStandardInformation().isDirectory() && tempName.endsWith("/")) {
                            tempName = tempName.substring(0, tempName.length() - 1);
                        }
                        tempPath = String.valueOf(path) + tempName;
                        this.log("SMB_CLIENT", 2, "Got list tempPath:" + x2 + ":" + tempPath);
                        list.add(this.stat(fai, tempPath, share));
                    }
                }
                catch (Exception e) {
                    this.log("SMB_CLIENT", 1, String.valueOf(x2 + 1) + " of " + list2.size() + ":Invalid file, or dead alias:" + item.getFileName() + ":" + e);
                    this.log("SMB_CLIENT", 1, e);
                    this.log(String.valueOf(x2 + 1) + " of " + list2.size() + ":Invalid file, or dead alias:" + item.getFileName() + ":" + e);
                }
                ++x2;
            }
            this.dirCache.clear();
            Vector vector = list;
            return vector;
        }
        catch (Exception e) {
            if (System.getProperty("crushftp.file_client_not_found_error", "true").equals("false")) {
                this.log("SMB_CLIENT", 0, "Ignoring file_client_not_found_error for folder:" + path2 + ":" + e);
                Vector vector = list;
                return vector;
            }
            throw e;
        }
        finally {
            if (share != null) {
                share.close();
            }
        }
    }

    private void setFileDateInfo(FileAllInformation test, Properties dir_item) {
        Date itemDate = new Date(test.getBasicInformation().getLastWriteTime().toEpochMillis());
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
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        this.log("SMB_CLIENT:Downloading :" + path);
        VRL vrl = new VRL(String.valueOf(this.url) + path.substring(1));
        String share_part = Common.first(vrl.getPath());
        int path_offset = share_part.length() + 1;
        if (!this.session.getConnection().isConnected()) {
            this.reconnect();
        }
        DiskShare share2 = null;
        int x = 0;
        while (x < 5) {
            try {
                share2 = (DiskShare)this.session.connectShare(Common.replace_str(share_part, "/", ""));
                break;
            }
            catch (Exception e) {
                this.log("SMB_CLIENT", 1, e);
                try {
                    Thread.sleep(3000L);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                this.reconnect();
                ++x;
            }
        }
        DiskShare share = share2;
        File f = share.openFile(path.substring(path_offset).replace('/', '\\'), new HashSet<Object>(Arrays.asList(AccessMask.GENERIC_READ)), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);
        InputStream fin = f.getInputStream();
        try {
            if (startPos > 0L) {
                fin.skip(startPos);
            }
        }
        catch (Exception e) {
            fin.close();
            throw e;
        }
        class InputWrapper
        extends InputStream {
            InputStream in3 = null;
            boolean closed = false;
            long pos;
            private final /* synthetic */ long val$endPos;
            private final /* synthetic */ File val$f;
            private final /* synthetic */ DiskShare val$share;

            public InputWrapper(InputStream in3, long l, long l2, File file, DiskShare diskShare) {
                this.val$endPos = l2;
                this.val$f = file;
                this.val$share = diskShare;
                this.pos = l;
                this.in3 = in3;
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
                int i = this.in3.read(b, off, len);
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
                this.val$f.close();
                this.val$share.close();
                this.closed = true;
            }
        }
        this.in = new InputWrapper(fin, startPos, endPos, f, share);
        return this.in;
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        this.log("SMB_CLIENT:mdtm:" + path + ":" + modified);
        VRL vrl1 = new VRL(String.valueOf(this.url) + path.substring(1));
        String share_part = Common.first(vrl1.getPath());
        int path_offset = share_part.length() + 1;
        if (!this.session.getConnection().isConnected()) {
            this.reconnect();
        }
        DiskShare share = null;
        int x = 0;
        while (x < 5) {
            try {
                share = (DiskShare)this.session.connectShare(Common.replace_str(share_part, "/", ""));
                break;
            }
            catch (Exception e) {
                this.log("SMB_CLIENT", 1, e);
                try {
                    Thread.sleep(3000L);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                this.reconnect();
                ++x;
            }
        }
        try {
            try {
                FileAllInformation current = share.getFileInformation(path.substring(path_offset).replace('/', '\\'));
                FileBasicInformation update = new FileBasicInformation(FileTime.fromDate((Date)new Date()), FileTime.fromDate((Date)new Date()), FileTime.fromDate((Date)new Date(modified)), FileTime.fromDate((Date)new Date(modified)), current.getBasicInformation().getFileAttributes());
                share.setFileInformation(path.substring(path_offset).replace('/', '\\'), (FileSettableInformation)update);
            }
            catch (Exception e) {
                this.log(e);
                try {
                    if (share != null) {
                        share.close();
                    }
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                return false;
            }
        }
        finally {
            try {
                if (share != null) {
                    share.close();
                }
            }
            catch (IOException iOException) {}
        }
        return true;
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) {
        if (rnfr.equals(rnto)) {
            return true;
        }
        Properties p = null;
        try {
            p = this.stat(rnfr);
        }
        catch (Exception e) {
            this.log(e);
        }
        VRL vrl1 = new VRL(String.valueOf(this.url) + rnfr.substring(1));
        String share_part = Common.first(vrl1.getPath());
        int path_offset = share_part.length() + 1;
        DiskShare share = null;
        try {
            if (!this.session.getConnection().isConnected()) {
                this.reconnect();
            }
            int x = 0;
            while (x < 5) {
                try {
                    share = (DiskShare)this.session.connectShare(Common.replace_str(share_part, "/", ""));
                    break;
                }
                catch (Exception e) {
                    this.log("SMB_CLIENT", 1, e);
                    try {
                        Thread.sleep(3000L);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    this.reconnect();
                    ++x;
                }
            }
            this.log("SMB_CLIENT", 1, "Checking if dest item exists:" + rnto.substring(path_offset).replace('/', '\\'));
            if (!share.fileExists(rnto.substring(path_offset).replace('/', '\\'))) {
                if (p.getProperty("type").equalsIgnoreCase("DIR")) {
                    this.log("SMB_CLIENT", 1, "Renaming from dir:" + rnfr.substring(path_offset).replace('/', '\\'));
                    this.log("SMB_CLIENT", 1, "Renaming to dir:" + rnto.substring(path_offset).replace('/', '\\'));
                    Directory f = share.openDirectory(rnfr.substring(path_offset).replace('/', '\\'), new HashSet<Object>(Arrays.asList(AccessMask.DELETE, AccessMask.GENERIC_WRITE)), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);
                    f.rename(rnto.substring(path_offset).replace('/', '\\'), false);
                    f.close();
                } else {
                    this.log("SMB_CLIENT", 1, "Renaming from file:" + rnfr.substring(path_offset).replace('/', '\\'));
                    this.log("SMB_CLIENT", 1, "Renaming to file:" + rnto.substring(path_offset).replace('/', '\\'));
                    File f = share.openFile(rnfr.substring(path_offset).replace('/', '\\'), new HashSet<Object>(Arrays.asList(AccessMask.DELETE, AccessMask.GENERIC_WRITE)), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);
                    f.rename(rnto.substring(path_offset).replace('/', '\\'), false);
                    f.close();
                }
                return true;
            }
        }
        catch (Exception e) {
            this.log(e);
        }
        finally {
            try {
                if (share != null) {
                    share.close();
                }
            }
            catch (IOException iOException) {}
        }
        return false;
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        this.log("SMB_CLIENT:Uploading:" + path);
        VRL vrl = new VRL(String.valueOf(this.url) + path.substring(1));
        String share_part = Common.first(vrl.getPath());
        int path_offset = share_part.length() + 1;
        if (!this.session.getConnection().isConnected()) {
            this.reconnect();
        }
        DiskShare share2 = null;
        int x = 0;
        while (x < 5) {
            try {
                share2 = (DiskShare)this.session.connectShare(Common.replace_str(share_part, "/", ""));
                break;
            }
            catch (Exception e) {
                this.log("SMB_CLIENT", 1, e);
                try {
                    Thread.sleep(3000L);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                this.reconnect();
                ++x;
            }
        }
        DiskShare share = share2;
        File f = null;
        f = startPos > 0L ? share.openFile(path.substring(path_offset).replace('/', '\\'), new HashSet<Object>(Arrays.asList(AccessMask.FILE_READ_ATTRIBUTES, AccessMask.GENERIC_WRITE, AccessMask.FILE_APPEND_DATA)), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null) : share.openFile(path.substring(path_offset).replace('/', '\\'), new HashSet<Object>(Arrays.asList(AccessMask.GENERIC_WRITE)), new HashSet<Object>(Arrays.asList(FileAttributes.FILE_ATTRIBUTE_NORMAL)), SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OVERWRITE_IF, null);
        try {
            class OutputWrapper
            extends OutputStream {
                File f = null;
                long pos = 0L;
                boolean closed = false;
                private final /* synthetic */ DiskShare val$share;

                public OutputWrapper(File f, long pos, DiskShare diskShare) {
                    this.val$share = diskShare;
                    this.f = f;
                    this.pos = pos;
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
                    this.f.write(b, this.pos, 0, len);
                    this.pos += (long)len;
                }

                @Override
                public void close() throws IOException {
                    if (this.closed) {
                        return;
                    }
                    this.f.close();
                    this.val$share.close();
                    this.closed = true;
                }
            }
            this.out = new OutputWrapper(f, startPos > 0L ? startPos : 0L, share);
        }
        catch (Exception e) {
            this.log(e);
            try {
                share.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        return this.out;
    }

    @Override
    public boolean delete(String path) {
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
        VRL vrl1 = new VRL(String.valueOf(this.url) + path.substring(1));
        String share_part = Common.first(vrl1.getPath());
        int path_offset = share_part.length() + 1;
        DiskShare share = null;
        try {
            try {
                if (!this.session.getConnection().isConnected()) {
                    this.reconnect();
                }
                int x = 0;
                while (x < 5) {
                    try {
                        share = (DiskShare)this.session.connectShare(Common.replace_str(share_part, "/", ""));
                        break;
                    }
                    catch (Exception e) {
                        this.log("SMB_CLIENT", 1, e);
                        try {
                            Thread.sleep(3000L);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        this.reconnect();
                        ++x;
                    }
                }
                if (p.getProperty("type").equalsIgnoreCase("DIR")) {
                    share.rmdir(path.substring(path_offset).replace('/', '\\'), this.config.getProperty("recurse_delete", "false").equals("true"));
                } else {
                    share.rm(path.substring(path_offset).replace('/', '\\'));
                }
            }
            catch (Exception e) {
                this.log(e);
                try {
                    if (share != null) {
                        share.close();
                    }
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                return false;
            }
        }
        finally {
            try {
                if (share != null) {
                    share.close();
                }
            }
            catch (IOException iOException) {}
        }
        return true;
    }

    @Override
    public boolean makedir(String path) {
        this.log("SMB_CLIENT:makedir:" + path);
        VRL vrl1 = new VRL(String.valueOf(this.url) + path.substring(1));
        String share_part = Common.first(vrl1.getPath());
        int path_offset = share_part.length() + 1;
        DiskShare share = null;
        try {
            try {
                if (!this.session.getConnection().isConnected()) {
                    this.reconnect();
                }
                int x = 0;
                while (x < 5) {
                    try {
                        share = (DiskShare)this.session.connectShare(Common.replace_str(share_part, "/", ""));
                        break;
                    }
                    catch (Exception e) {
                        this.log("SMB_CLIENT", 1, e);
                        try {
                            Thread.sleep(3000L);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        this.reconnect();
                        ++x;
                    }
                }
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
                share.mkdir(path.substring(path_offset).replace('/', '\\'));
            }
            catch (Exception e) {
                this.log(e);
                try {
                    if (share != null) {
                        share.close();
                    }
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                return false;
            }
        }
        finally {
            try {
                if (share != null) {
                    share.close();
                }
            }
            catch (IOException iOException) {}
        }
        return true;
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        this.log("SMB_CLIENT:makedirs:" + path);
        VRL vrl1 = new VRL(String.valueOf(this.url) + path.substring(1));
        String share_part = Common.first(vrl1.getPath());
        int path_offset = share_part.length() + 1;
        boolean ok = true;
        String[] parts = path.substring(path_offset).split("/");
        String path2 = "";
        Exception final_error = null;
        int x = 0;
        while (x < parts.length && ok) {
            block8: {
                final_error = null;
                path2 = String.valueOf(path2) + parts[x] + "/";
                Properties stat2 = null;
                try {
                    try {
                        stat2 = this.stat(String.valueOf(share_part) + "/" + path2, true);
                    }
                    catch (Exception e) {
                        this.log(e);
                        if (x != parts.length - 1) break block8;
                    }
                    if (stat2 == null) {
                        ok = this.makedir(String.valueOf(share_part) + "/" + path2);
                    }
                    {
                    }
                }
                catch (Exception e) {
                    final_error = e;
                    this.log(e);
                }
            }
            ++x;
        }
        if (final_error != null) {
            throw final_error;
        }
        return ok;
    }

    @Override
    public String doCommand(String command) throws Exception {
        return "";
    }
}

