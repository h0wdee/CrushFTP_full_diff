/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.maverick.sshd.Connection
 *  com.maverick.sshd.SessionChannel
 *  com.maverick.sshd.SftpFile
 *  com.maverick.sshd.SftpFileAttributes
 *  com.maverick.sshd.SshContext
 *  com.maverick.sshd.platform.FileSystem
 *  com.maverick.sshd.platform.InvalidHandleException
 *  com.maverick.sshd.platform.PermissionDeniedException
 *  com.maverick.sshd.platform.UnsupportedFileOperationException
 */
package crushftp.server;

import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import com.maverick.events.Event;
import com.maverick.sshd.Connection;
import com.maverick.sshd.SessionChannel;
import com.maverick.sshd.SftpFile;
import com.maverick.sshd.SftpFileAttributes;
import com.maverick.sshd.SshContext;
import com.maverick.sshd.platform.FileSystem;
import com.maverick.sshd.platform.InvalidHandleException;
import com.maverick.sshd.platform.PermissionDeniedException;
import com.maverick.sshd.platform.UnsupportedFileOperationException;
import com.maverick.util.UnsignedInteger32;
import com.maverick.util.UnsignedInteger64;
import crushftp.gui.LOC;
import crushftp.handlers.Common;
import crushftp.handlers.IdleMonitor;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.server.LIST_handler;
import crushftp.server.RETR_handler;
import crushftp.server.STOR_handler;
import crushftp.server.ServerStatus;
import crushftp.server.ssh.ScpFileNamePattern;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class ServerSessionSSH
implements FileSystem {
    private Map openFiles = new HashMap<K, V>();
    private Map dirList = new HashMap<K, V>();
    private Map quickLookupDirItem = new HashMap<K, V>();
    private ScpFileNamePattern filename_pattern = null;
    private String used_protocol = "";
    SessionCrush thisSession = null;
    Common common_code = new Common();
    public static Properties sessionLookup = new Properties();
    public static Properties connectionLookup = new Properties();
    public transient Object statLock = new Object();
    public transient Object closeFileLock = new Object();
    private boolean log_dir_listings = false;
    public static Properties cross_session_lookup = new Properties();
    public String session_id = null;
    public final String RANDOM_CLASS_UID = Common.makeBoundary(20);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void init(byte[] sessionid, SessionChannel session, SshContext context, String protocolInUse) throws IOException {
        this.session_id = session.getConnection().getTransport().getConnection().getSessionId();
        this.thisSession = (SessionCrush)sessionLookup.get(sessionid);
        this.thisSession.active();
        Properties properties = cross_session_lookup;
        synchronized (properties) {
            Vector<String> connected_channels = (Vector<String>)cross_session_lookup.get(this.session_id);
            if (connected_channels == null) {
                connected_channels = new Vector<String>();
                cross_session_lookup.put(this.session_id, connected_channels);
            }
            connected_channels.addElement(this.RANDOM_CLASS_UID);
        }
        try {
            context.setSFTPCharsetEncoding(this.thisSession.SG("char_encoding"));
        }
        catch (Exception e) {
            Log.log("SSH_SERVER", 1, e);
        }
        int minutes = this.thisSession.IG("max_idle_time");
        if (minutes == 0) {
            minutes = 9999;
        }
        this.used_protocol = protocolInUse.trim();
        this.thisSession.thread_killer_item = new IdleMonitor(this.thisSession, new Date().getTime(), minutes, null);
        String remote_client_type = session.getConnection().getTransport().getRemoteIdentification();
        this.thisSession.add_log_formatted("CONNECT " + remote_client_type, "USER");
        if (ServerStatus.SG("blocked_ssh_clients").toUpperCase().indexOf(remote_client_type.toUpperCase()) >= 0) {
            throw new IOException("SSH client type blocked:" + remote_client_type);
        }
        if (ServerStatus.BG("synchronized_sftp")) {
            this.closeFileLock = this.statLock;
        }
        this.thisSession.uiPUT("sftp_login_complete", "true");
        this.thisSession.logLogin(true, "");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void init(Connection conn, String protocolInUse) throws IOException {
        this.session_id = conn.getSessionId();
        this.thisSession = (SessionCrush)sessionLookup.get(this.session_id);
        this.thisSession.active();
        Properties properties = cross_session_lookup;
        synchronized (properties) {
            Vector<String> connected_channels = (Vector<String>)cross_session_lookup.get(this.session_id);
            if (connected_channels == null) {
                connected_channels = new Vector<String>();
                cross_session_lookup.put(this.session_id, connected_channels);
            }
            connected_channels.addElement(this.RANDOM_CLASS_UID);
        }
        try {
            conn.getContext().setSFTPCharsetEncoding(this.thisSession.SG("char_encoding"));
        }
        catch (Exception e) {
            Log.log("SSH_SERVER", 1, e);
        }
        int minutes = this.thisSession.IG("max_idle_time");
        if (minutes == 0) {
            minutes = 9999;
        }
        this.thisSession.thread_killer_item = new IdleMonitor(this.thisSession, new Date().getTime(), minutes, null);
        this.used_protocol = protocolInUse;
        String remote_client_type = conn.getRemoteIdentification();
        this.thisSession.add_log_formatted("CONNECT " + remote_client_type + " Cipher:" + conn.getCipherCS() + "/" + conn.getCipherSC() + " KEX:" + conn.getKeyEchangeInUse() + " MAC:" + conn.getMacCS() + "/" + conn.getMacSC(), "USER");
        if (ServerStatus.SG("blocked_ssh_clients").toUpperCase().indexOf(remote_client_type.toUpperCase()) >= 0) {
            throw new IOException("SSH client type blocked:" + remote_client_type);
        }
        this.thisSession.uiPUT("user_cipher1", conn.getCipherCS());
        this.thisSession.uiPUT("user_cipher2", conn.getCipherSC());
        this.thisSession.uiPUT("user_mac1", conn.getMacCS());
        this.thisSession.uiPUT("user_mac2", conn.getMacSC());
        this.thisSession.uiPUT("user_kex1", conn.getKeyEchangeInUse());
        if (ServerStatus.BG("synchronized_sftp")) {
            this.closeFileLock = this.statLock;
        }
        this.thisSession.uiPUT("sftp_login_complete", "true");
        this.thisSession.logLogin(true, "");
    }

    public void init(SessionChannel arg0, SshContext arg1) throws IOException {
    }

    /*
     * Unable to fully structure code
     */
    public ServerSessionSSH() {
        super();
        if (!ServerStatus.BG("write_session_logs")) ** GOTO lbl-1000
        if (ServerStatus.SG("log_allow_str").indexOf("(DIR_LIST)") >= 0) {
            v0 = true;
        } else lbl-1000:
        // 2 sources

        {
            v0 = false;
        }
        this.log_dir_listings = v0;
    }

    public boolean makeDirectory(String path, SftpFileAttributes folderAttributes) throws PermissionDeniedException, FileNotFoundException, IOException {
        try {
            this.Tin();
            try {
                this.thisSession.stop_idle_timer();
                this.thisSession.start_idle_timer();
            }
            catch (Exception e) {
                Log.log("SSH_SERVER", 1, e);
            }
            this.delay();
            path = this.fixPath(path);
            this.thisSession.add_log_formatted("makeDirectory " + path, "MKD");
            Log.log("SSH_SERVER", 2, "SFTP:MakeDirectory:" + path);
            this.thisSession.uiPUT("the_command", "MKD");
            this.thisSession.uiPUT("the_command_data", path);
            this.thisSession.runPlugin("beforeCommand", null);
            String result = "error";
            try {
                result = this.thisSession.do_MKD(true, path);
                Properties p = new Properties();
                p.put("command_code", "0");
                p.put("command_data", result);
                this.thisSession.runPlugin("afterCommand", p);
                Log.log("SSH_SERVER", 2, "SFTP:MakeDirectory:" + result);
                if (result.equals("%MKD-exists%")) {
                    if (!ServerStatus.BG("sftp_mkdir_exist_silent")) {
                        throw new IOException("%MKD-exists%");
                    }
                }
                if (result.equals("%MKD-bad%")) {
                    throw new PermissionDeniedException("%MKD-bad%");
                }
            }
            catch (Exception e) {
                this.thisSession.doErrorEvent(e);
                throw new IOException(e.getMessage());
            }
            return true;
        }
        finally {
            this.Tout();
        }
    }

    public SftpFileAttributes getFileAttributes(byte[] handle) throws IOException, InvalidHandleException {
        try {
            this.Tin();
            String path_handle = new String(handle, "UTF8");
            String path = path_handle.substring(0, path_handle.lastIndexOf(":"));
            path = this.fixPath(path);
            this.thisSession.add_log_formatted("getFileAttributes " + path, "MDTM");
            Log.log("SSH_SERVER", 2, "SFTP:getFileAttributes1:" + path);
            this.thisSession.uiPUT("the_command_data", path);
            this.thisSession.uiPUT("the_command", "FILE_ATTRIBUTE");
            this.thisSession.runPlugin("beforeCommand", null);
            SftpFileAttributes attrs = null;
            try {
                attrs = this.getFileAttributes(path_handle.substring(0, path_handle.lastIndexOf(":")));
                Properties p = new Properties();
                p.put("command_code", "0");
                p.put("command_data", "" + attrs);
                this.thisSession.runPlugin("afterCommand", p);
            }
            catch (FileNotFoundException e) {
                throw new IOException(e.getMessage());
            }
            SftpFileAttributes sftpFileAttributes = attrs;
            return sftpFileAttributes;
        }
        finally {
            this.Tout();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SftpFileAttributes getFileAttributes(String path) throws IOException, FileNotFoundException {
        Properties item;
        block11: {
            path = this.fixPath(path);
            this.thisSession.add_log_formatted("getFileAttributes " + path, "MDTM");
            Log.log("SSH_SERVER", 2, "SFTP:getFileAttributes2:" + path);
            this.thisSession.uiPUT("the_command_data", path);
            this.thisSession.uiPUT("the_command", "FILE_ATTRIBUTE");
            this.thisSession.runPlugin("beforeCommand", null);
            item = null;
            item = (Properties)this.quickLookupDirItem.get(path);
            try {
                if (item != null) break block11;
                Object object = this.statLock;
                synchronized (object) {
                    if (item == null && (item = this.thisSession.uVFS.get_item(path)) != null) {
                        this.quickLookupDirItem.put(path, item);
                    }
                }
            }
            catch (Exception e) {
                Log.log("SSH_SERVER", 1, e);
            }
        }
        try {
            if (ServerStatus.IG("log_debug_level") >= 2 && item != null) {
                Log.log("SSH_SERVER", 2, "SFTP:getFileAttributes2:" + VRL.safe(item));
            }
            if (path.indexOf(":filetree") >= 0 && ServerStatus.BG("allow_filetree")) {
                SftpFileAttributes attrs = new SftpFileAttributes("rwxrwxrwx");
                return attrs;
            }
            if (item == null) {
                throw new FileNotFoundException(LOC.G("File not found3:" + path));
            }
            SftpFileAttributes sfa = this.getFileAttributes(item);
            Properties p = new Properties();
            p.put("command_code", "0");
            p.put("command_data", "" + sfa);
            this.thisSession.runPlugin("afterCommand", p);
            return sfa;
        }
        catch (Exception e) {
            Log.log("SSH_SERVER", 1, e);
            throw e;
        }
    }

    public SftpFileAttributes getFileAttributes(Properties item) {
        GregorianCalendar cal = new GregorianCalendar();
        SftpFileAttributes attrs = new SftpFileAttributes("rw-rw-rw-");
        try {
            attrs.setPermissions(item.getProperty("permissions", "-----------").substring(1, 10));
            attrs.setPermissions(new UnsignedInteger32(attrs.getPermissions().longValue() | (long)(item.getProperty("type").equals("DIR") ? 16384 : 32768)));
            if (Long.parseLong(item.getProperty("size", "0")) < 0L) {
                item.put("size", "0");
            }
            attrs.setSize(new UnsignedInteger64(item.getProperty("size", "0")));
            long lastMod = Long.parseLong(item.getProperty("modified", String.valueOf(System.currentTimeMillis())));
            if (this.thisSession.DG("timezone_offset") != 0.0) {
                Date d = new Date(lastMod);
                cal.setTime(d);
                cal.setTimeInMillis((long)((double)cal.getTimeInMillis() + this.thisSession.DG("timezone_offset") * 1000.0 * 60.0 * 60.0));
                lastMod = cal.getTime().getTime();
            }
            if (lastMod <= 0L) {
                lastMod = new Date().getTime();
            }
            UnsignedInteger64 t = null;
            t = ServerStatus.BG("sftp_round_seconds_up") ? new UnsignedInteger64(new BigDecimal(lastMod).divide(new BigDecimal(1000), 4).toBigInteger()) : new UnsignedInteger64(lastMod / 1000L);
            attrs.setTimes(t, t);
        }
        catch (Exception e) {
            Log.log("SSH_SERVER", 1, e);
            throw e;
        }
        return attrs;
    }

    public byte[] openDirectory(String path) throws PermissionDeniedException, FileNotFoundException, IOException {
        try {
            this.Tin();
            this.quickLookupDirItem.clear();
            this.delay();
            if (!path.endsWith("/")) {
                path = String.valueOf(path) + "/";
            }
            path = this.fixPath(path);
            this.thisSession.add_log_formatted("CWD " + path, "CWD");
            Log.log("SSH_SERVER", 2, "SFTP:openDirectory:" + path);
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            this.thisSession.uiPUT("the_command_data", path);
            this.thisSession.uiPUT("the_command", "CWD");
            this.thisSession.runPlugin("beforeCommand", null);
            Properties item = null;
            try {
                item = this.thisSession.uVFS.get_fake_item(path, "DIR");
                if (item == null && !ServerStatus.BG("jailproxy")) {
                    item = this.thisSession.uVFS.get_item_parent(path);
                }
            }
            catch (Exception e) {
                Log.log("SSH_SERVER", 2, e);
            }
            if (ServerStatus.IG("log_debug_level") >= 2) {
                Log.log("SSH_SERVER", 2, "SFTP:openDirectory:" + VRL.safe(item));
            }
            if (item == null) {
                throw new FileNotFoundException(LOC.G("File not found1:" + path));
            }
            if (item.getProperty("type").equals("FILE")) {
                this.thisSession.add_log_formatted("550 CWD " + path + ":" + LOC.G("Item is a file!"), "CWD");
                throw new IOException(LOC.G("Item is a file!"));
            }
            String result = "";
            try {
                try {
                    result = this.thisSession.do_CWD();
                }
                catch (Exception e) {
                    this.thisSession.add_log_formatted("550 CWD " + path + ":" + e.getMessage(), "CWD");
                    throw new IOException(e.getMessage());
                }
                Log.log("SSH_SERVER", 2, "SFTP:openDirectory:" + result);
                if (result.equals("%CWD-bad%")) {
                    this.thisSession.add_log_formatted("550 CWD " + path + ":%CWD-bad%", "CWD");
                    throw new PermissionDeniedException("%CWD-bad%");
                }
                if (result.equals("%CWD-not found%")) {
                    this.thisSession.add_log_formatted("550 CWD " + path + ":%CWD-not found%", "CWD");
                    throw new FileNotFoundException("%CWD-not found%");
                }
                this.dirList.put(String.valueOf(path) + "_status", "READY");
                this.dirList.remove(String.valueOf(path) + "_list");
                this.dirList.remove(String.valueOf(path) + "_hash");
            }
            finally {
                Properties p = new Properties();
                p.put("command_code", "0");
                p.put("command_data", String.valueOf(result));
                this.thisSession.runPlugin("afterCommand", p);
            }
            byte[] byArray = path.getBytes("UTF8");
            return byArray;
        }
        finally {
            this.Tout();
        }
    }

    public SftpFile[] readDirectory(byte[] handle) throws InvalidHandleException, EOFException {
        String original_thread_name = Thread.currentThread().getName();
        try {
            this.Tin();
            String path = new String(handle);
            Thread.currentThread().setName(String.valueOf(this.thisSession.uiSG("user_name")) + ":(" + this.thisSession.uiSG("user_number") + ")-" + this.thisSession.uiSG("user_ip") + " (LIST):" + path);
            try {
                path = new String(handle, "UTF8");
            }
            catch (Exception exception) {
                // empty catch block
            }
            SftpFile[] sftpFileArray = this.readDirectory2(path, true);
            return sftpFileArray;
        }
        finally {
            this.Tout();
            Thread.currentThread().setName(original_thread_name);
        }
    }

    public SftpFile[] readDirectory2(String path, boolean resolve_star) throws InvalidHandleException, EOFException {
        try {
            this.thisSession.stop_idle_timer();
            this.thisSession.start_idle_timer();
        }
        catch (Exception e) {
            Log.log("SSH_SERVER", 1, e);
        }
        path = this.fixPath(path);
        Log.log("SSH_SERVER", 2, "SFTP:readDirectory:" + path);
        Vector files = new Vector();
        if (this.dirList.get(String.valueOf(path) + "_status").toString().equals("READY")) {
            final Vector<Properties> list = new Vector<Properties>();
            this.dirList.put(String.valueOf(path) + "_status", "LISTING");
            this.dirList.put(String.valueOf(path) + "_list", list);
            this.dirList.put(String.valueOf(path) + "_hash", new Properties());
            this.thisSession.add_log_formatted("150 LIST " + path, "LIST");
            this.thisSession.uiPUT("the_command_data", path);
            this.thisSession.uiPUT("the_command", "LIST");
            this.thisSession.runPlugin("beforeCommand", null);
            if (ServerStatus.BG("send_dot_dot_list_sftp")) {
                Properties dir_item = new Properties();
                dir_item.put("name", ".");
                dir_item.put("type", "DIR");
                dir_item.put("permissions", "drwxrwxrwx");
                dir_item.put("size", "0");
                dir_item.put("url", ".");
                dir_item.put("root_dir", ".");
                dir_item.put("sftp_path", ".");
                dir_item.put("link", "false");
                dir_item.put("num_items", "1");
                dir_item.put("owner", "user");
                dir_item.put("group", "group");
                dir_item.put("protocol", "file");
                list.addElement(dir_item);
                dir_item = new Properties();
                dir_item.put("name", "..");
                dir_item.put("type", "DIR");
                dir_item.put("permissions", "drwxrwxrwx");
                dir_item.put("size", "0");
                dir_item.put("url", ".");
                dir_item.put("root_dir", ".");
                dir_item.put("sftp_path", "..");
                dir_item.put("link", "false");
                dir_item.put("num_items", "1");
                dir_item.put("owner", "user");
                dir_item.put("group", "group");
                dir_item.put("protocol", "file");
                list.addElement(dir_item);
            }
            Properties folder_item = new Properties();
            try {
                folder_item = this.thisSession.uVFS.get_item(path);
                final String path2 = path;
                Runnable r = new Runnable(){

                    @Override
                    public void run() {
                        try {
                            ServerSessionSSH.this.thisSession.uVFS.getListing(list, path2);
                            ServerSessionSSH.this.dirList.put(String.valueOf(path2) + "_status", "LISTING_DONE");
                        }
                        catch (Exception e) {
                            Log.log("LIST", 0, e);
                            ServerSessionSSH.this.dirList.put(String.valueOf(path2) + "_status", "ERROR:" + e.getMessage());
                        }
                    }
                };
                if (ServerStatus.BG("listing_multithreaded")) {
                    Worker.startWorker(r, String.valueOf(Thread.currentThread().getName()) + ":Listing..." + path2);
                } else {
                    r.run();
                    Properties ppp = new Properties();
                    ppp.put("listing", list);
                    this.thisSession.runPlugin("list", ppp);
                }
            }
            catch (Exception e) {
                Log.log("SSH_SERVER", 1, e);
            }
            if (Log.log("SSH_SERVER", 3, "")) {
                Log.log("SSH_SERVER", 3, "SFTP:readDirectory:" + list);
            }
            files = this.getListFiles(list, path, (Properties)this.dirList.get(String.valueOf(path) + "_hash"), resolve_star);
        } else if (this.dirList.get(String.valueOf(path) + "_status").toString().equals("LISTING") || this.dirList.get(String.valueOf(path) + "_status").toString().equals("LISTING_DONE")) {
            files = this.getListFiles((Vector)this.dirList.get(String.valueOf(path) + "_list"), path, (Properties)this.dirList.get(String.valueOf(path) + "_hash"), resolve_star);
        } else {
            if (this.dirList.get(String.valueOf(path) + "_status").toString().equals("DONE")) {
                this.thisSession.add_log_formatted("226 LIST " + path, "LIST");
                throw new EOFException(LOC.G("End of dir listing."));
            }
            if (this.dirList.get(String.valueOf(path) + "_status").toString().startsWith("ERROR:")) {
                this.dirList.remove(String.valueOf(path) + "_list");
                String error_message = this.dirList.get(String.valueOf(path) + "_status").toString();
                if (error_message.contains("No such item:")) {
                    error_message = "ERROR: No such item: " + path;
                }
                this.thisSession.add_log_formatted("550 ERROR " + path + ":" + error_message, "LIST");
                throw new InvalidHandleException("ERROR:" + error_message);
            }
        }
        SftpFile[] sftpfiles = new SftpFile[files.size()];
        boolean igonre_case_was_set = false;
        int i = 0;
        while (i < files.size()) {
            Properties p = (Properties)files.elementAt(i);
            try {
                Log.log("SSH_SERVER", 2, "SCP command : protocol " + this.used_protocol + " item : " + VRL.safe(p));
                if (!igonre_case_was_set && this.used_protocol.toUpperCase().equals("SCP") && ServerStatus.BG("lowercase_all_s3_paths") && new VRL(p.getProperty("url", "")).getProtocol().toUpperCase().equals("S3") && this.filename_pattern != null) {
                    Log.log("SSH_SERVER", 2, "SCP command : set ignore case FNP Session SSH : " + this);
                    this.filename_pattern.setIgnoreCase(true);
                    igonre_case_was_set = true;
                }
            }
            catch (Exception e) {
                Log.log("SSH_SERVER", 2, e);
            }
            sftpfiles[i] = new SftpFile(p.getProperty("name"), this.getFileAttributes(p));
            ++i;
        }
        Properties p = new Properties();
        p.put("command_code", "0");
        p.put("command_data", String.valueOf(path));
        this.thisSession.runPlugin("afterCommand", p);
        return sftpfiles;
    }

    public Vector getListFiles(Vector list, String path, Properties name_hash, boolean resolve_star) {
        Vector<Properties> files = new Vector<Properties>();
        while (list.size() > 0 || this.dirList.get(String.valueOf(path) + "_status").toString().equals("LISTING")) {
            String sftp_path;
            if (list.size() == 0) {
                try {
                    Thread.sleep(10L);
                }
                catch (InterruptedException interruptedException) {}
                continue;
            }
            Properties p = (Properties)list.remove(0);
            if (name_hash.containsKey(p.getProperty("name"))) continue;
            name_hash.put(p.getProperty("name"), "");
            if (!p.getProperty("name").equals(".") && !p.getProperty("name").equals("..")) {
                try {
                    if (!LIST_handler.checkName(p, this.thisSession, false, false)) {
                        continue;
                    }
                }
                catch (Exception e) {
                    Log.log("SSH_SERVER", 1, e);
                }
            }
            if ((sftp_path = String.valueOf(p.getProperty("root_dir")) + p.getProperty("name")).startsWith(this.thisSession.SG("root_dir"))) {
                sftp_path = sftp_path.substring(this.thisSession.SG("root_dir").length() - 1);
            }
            p.put("sftp_path", sftp_path);
            if (this.quickLookupDirItem.size() < 10000 && !p.getProperty("url").toLowerCase().startsWith("file:/")) {
                this.quickLookupDirItem.put(this.fixPath(sftp_path, resolve_star), p);
            }
            files.addElement(p);
            if (this.log_dir_listings) {
                this.thisSession.add_log_formatted(sftp_path, "DIR_LIST");
            }
            if (files.size() >= 100) break;
        }
        if (list.size() == 0 && this.dirList.get(String.valueOf(path) + "_status").toString().equals("LISTING_DONE")) {
            this.dirList.put(String.valueOf(path) + "_status", "DONE");
            this.dirList.remove(String.valueOf(path) + "_list");
            this.dirList.remove(String.valueOf(path) + "_hash");
        }
        return files;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public byte[] openFile(String path, UnsignedInteger32 flags, SftpFileAttributes attrs) throws PermissionDeniedException, FileNotFoundException, IOException {
        try {
            this.Tin();
            Object object = this.statLock;
            synchronized (object) {
                Properties p;
                this.delay();
                path = this.fixPath(path);
                boolean open_create = (flags.intValue() & 8) == 8;
                boolean open_write = (flags.intValue() & 2) == 2;
                boolean open_truncate = (flags.intValue() & 0x10) == 16;
                boolean open_append = (flags.intValue() & 4) == 4;
                boolean open_exclusive = (flags.intValue() & 0x20) == 32;
                boolean open_text = (flags.intValue() & 0x40) == 64;
                Log.log("SSH_SERVER", 0, "SFTP:openFile:" + path + "  create=" + open_create + ", write=" + open_write + ", truncate=" + open_truncate + ", append=" + open_append + ", exclusive=" + open_exclusive + ", text=" + open_text);
                Properties item = null;
                try {
                    if (open_create || open_write || open_truncate || open_append) {
                        this.thisSession.uiPUT("the_command_data", path);
                        this.thisSession.uiPUT("the_command", "STOR_INIT");
                        this.thisSession.add_log_formatted("STOR START " + path, "STOR");
                        p = new Properties();
                        p.put("actual_path", path);
                        p.put("message_string", "");
                        this.thisSession.runPlugin("beforeCommand", p);
                        if (!p.getProperty("message_string", "").equals("")) {
                            throw new IOException(p.getProperty("message_string"));
                        }
                        path = p.getProperty("actual_path");
                        this.thisSession.uiPUT("the_command", "STOR");
                        try {
                            item = this.thisSession.uVFS.get_item(path);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        if (ServerStatus.IG("log_debug_level") >= 2) {
                            Log.log("SSH_SERVER", 2, "SFTP:openFile1:" + VRL.safe(item));
                        }
                        if (!(this.thisSession.check_access_privs(Common.all_but_last(path), "STOR") && Common.filter_check("U", Common.last(path), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter")) && Common.filter_check("F", Common.last(path), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter")))) {
                            if (!Common.filter_check("U", Common.last(path), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter")) || !Common.filter_check("F", Common.last(path), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter"))) {
                                this.thisSession.add_log_formatted("550 STOR error: Upload attempt was rejected because the block matching names! File name :" + Common.last(path) + " Filters :" + ServerStatus.SG("filename_filters_str") + this.thisSession.SG("file_filter"), "STOR");
                                throw new PermissionDeniedException("Upload attempt was rejected because the block matching names! File name :" + Common.last(path) + " Filters :" + ServerStatus.SG("filename_filters_str") + this.thisSession.SG("file_filter"));
                            }
                            throw new PermissionDeniedException("Upload attempt was rejected because of your VFS permissions! File name :" + Common.last(path));
                        }
                        boolean fileExists = true;
                        if (open_create || open_truncate) {
                            if (item != null) {
                                item.put("open_text", String.valueOf(open_text));
                                if (open_exclusive) {
                                    throw new IOException("File exists, cannot be exclusive.");
                                }
                                GenericClient c = this.thisSession.uVFS.getClient(item);
                                try {
                                    VRL vrl;
                                    block53: {
                                        vrl = new VRL(item.getProperty("url"));
                                        Properties stat = c.stat(vrl.getPath());
                                        if (item.getProperty("privs").indexOf("(view)") < 0 && stat != null) {
                                            try {
                                                int fileNameInt = 1;
                                                String itemName = item.getProperty("url");
                                                String itemExt = "";
                                                if (itemName.lastIndexOf(".") > 0 && (itemName.lastIndexOf(".") == itemName.length() - 4 || itemName.lastIndexOf(".") == itemName.length() - 5)) {
                                                    itemExt = itemName.substring(itemName.lastIndexOf("."));
                                                    itemName = itemName.substring(0, itemName.lastIndexOf("."));
                                                }
                                                while (c.stat(String.valueOf(Common.all_but_last(vrl.getPath())) + itemName + fileNameInt + itemExt) != null) {
                                                    ++fileNameInt;
                                                }
                                                c.rename(vrl.getPath(), String.valueOf(Common.all_but_last(vrl.getPath())) + itemName + fileNameInt + itemExt, false);
                                                vrl = new VRL(String.valueOf(Common.all_but_last(vrl.toString())) + itemName + fileNameInt + itemExt);
                                            }
                                            catch (Exception e) {
                                                if (("" + e).indexOf("Interrupted") < 0) break block53;
                                                throw e;
                                            }
                                        }
                                    }
                                    if (c.stat(vrl.getPath()) != null) {
                                        c = this.thisSession.uVFS.releaseClient(c);
                                        if (!this.thisSession.check_access_privs(path, "DELE") && !open_append) {
                                            throw new PermissionDeniedException(p.getProperty("message_string", LOC.G("STOR Denied for " + path + "!")));
                                        }
                                    }
                                }
                                finally {
                                    c = this.thisSession.uVFS.releaseClient(c);
                                }
                                if (ServerStatus.IG("log_debug_level") >= 2) {
                                    Log.log("SSH_SERVER", 2, "SFTP:openFile:setZeroLength:" + VRL.safe(item));
                                }
                            } else {
                                fileExists = false;
                                try {
                                    item = this.thisSession.uVFS.get_item_parent(path);
                                    item.put("open_text", String.valueOf(open_text));
                                    item.put("type", "FILE");
                                    item.put("size", "0");
                                    item.put("modified", String.valueOf(new Date().getTime()));
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(new Date());
                                    item.put("time_or_year", String.valueOf(cal.get(1)));
                                }
                                catch (Exception cal) {
                                    // empty catch block
                                }
                            }
                            if (ServerStatus.BG("allow_ssh_0_byte_file")) {
                                if (ServerStatus.IG("log_debug_level") >= 2) {
                                    Log.log("SSH_SERVER", 2, "SFTP:openFile:zero:" + VRL.safe(item));
                                }
                                if (item != null && item.getProperty("privs", "").indexOf("(sync") < 0 && !path.endsWith("zipstream") && (fileExists || !fileExists && open_create)) {
                                    item.put("zero_wanted", "true");
                                }
                            }
                        }
                        if (open_create && ServerStatus.BG("allow_ssh_0_byte_file")) {
                            try {
                                String former_type = "FILE";
                                if (fileExists) {
                                    former_type = item.getProperty("type");
                                }
                                if ((item = this.thisSession.uVFS.get_item_parent(path)) != null) {
                                    item.put("type", former_type);
                                    item.put("open_text", String.valueOf(open_text));
                                    item.put("size", "0");
                                }
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            if (item != null && item.getProperty("privs", "").indexOf("(sync") < 0 && !path.endsWith("zipstream")) {
                                item.put("zero_wanted", "true");
                                item.put("size", "0");
                                if (!fileExists) {
                                    item.put("type", "FILE");
                                }
                                item.put("open_text", String.valueOf(open_text));
                                item.put("modified", String.valueOf(new Date().getTime()));
                                this.quickLookupDirItem.put(path, item);
                            }
                        }
                        if (item == null) {
                            item = this.thisSession.uVFS.get_item_parent(path);
                            item.put("type", "FILE");
                            item.put("open_text", String.valueOf(open_text));
                            item.put("size", "0");
                        }
                        item.put("allow_write", "true");
                    } else {
                        String temp_path = path;
                        if (path.indexOf(":filetree") >= 0 && ServerStatus.BG("allow_filetree")) {
                            temp_path = path.substring(0, path.indexOf(":filetree"));
                        }
                        this.thisSession.uiPUT("the_command_data", path);
                        this.thisSession.uiPUT("the_command", "RETR_INIT");
                        Properties p2 = new Properties();
                        p2.put("actual_path", temp_path);
                        this.thisSession.runPlugin("beforeCommand", p2);
                        temp_path = p2.getProperty("actual_path");
                        try {
                            item = this.thisSession.uVFS.get_item(temp_path);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        if (ServerStatus.IG("log_debug_level") >= 2) {
                            Log.log("SSH_SERVER", 2, "SFTP:openFile2:" + VRL.safe(item));
                        }
                        if (item == null) {
                            throw new FileNotFoundException("File not found2:" + path);
                        }
                        item.put("open_text", String.valueOf(open_text));
                        this.thisSession.uiPUT("the_command", "LIST");
                        if (!(this.thisSession.check_access_privs(path, "RETR", item) && Common.filter_check("D", Common.last(path), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter")) && Common.filter_check("F", String.valueOf(item.getProperty("name")) + (item.getProperty("type").equalsIgnoreCase("DIR") && !item.getProperty("name").endsWith("/") ? "/" : ""), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter")))) {
                            throw new PermissionDeniedException(p2.getProperty("message_string", "Denied!"));
                        }
                        this.thisSession.add_log_formatted("RETR START " + path, "RETR");
                    }
                    path = String.valueOf(path) + ":" + Common.makeBoundary(5);
                    this.openFiles.put(path, item);
                    if (ServerStatus.IG("log_debug_level") >= 2) {
                        Log.log("SSH_SERVER", 2, "SFTP:openedFile:" + path + ":" + VRL.safe(item));
                    }
                }
                catch (Exception e) {
                    Log.log("SSH_SERVER", 1, e);
                    this.thisSession.add_log_formatted("550 openFile error:" + e.getMessage(), "RETR");
                    Properties p3 = new Properties();
                    p3.put("command_code", "0");
                    p3.put("command_data", String.valueOf(e.getMessage()));
                    this.thisSession.runPlugin("afterCommand", p3);
                    this.thisSession.doErrorEvent(e);
                    if (("" + e).indexOf("not found") >= 0) {
                        throw new FileNotFoundException(e.getMessage());
                    }
                    throw new IOException(e.getMessage());
                }
                Log.log("SSH_SERVER", 2, "SFTP:openFile:reply:" + path);
                p = new Properties();
                p.put("command_code", "0");
                p.put("command_data", String.valueOf(path));
                this.thisSession.runPlugin("afterCommand", p);
                byte[] byArray = path.getBytes("UTF8");
                return byArray;
            }
        }
        finally {
            this.Tout();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void writeFile(byte[] handle, UnsignedInteger64 offset, byte[] data, int off, int len) throws InvalidHandleException, IOException {
        try {
            this.Tin();
            Object object = this.statLock;
            synchronized (object) {
                boolean buffered;
                STOR_handler stor_files;
                OutputStream outStream;
                Properties item;
                String path_handle;
                block40: {
                    try {
                        this.thisSession.stop_idle_timer();
                        this.thisSession.start_idle_timer();
                    }
                    catch (Exception e) {
                        Log.log("SSH_SERVER", 1, e);
                    }
                    path_handle = new String(handle, "UTF8");
                    String path = path_handle.substring(0, path_handle.lastIndexOf(":"));
                    path = this.fixPath(path);
                    if (ServerStatus.IG("log_debug_level") >= 2) {
                        Log.log("SSH_SERVER", 2, "SFTP:writeFile:" + path_handle);
                    }
                    item = (Properties)this.openFiles.get(path_handle);
                    outStream = (OutputStream)item.get("outputstream");
                    stor_files = (STOR_handler)item.get("stor_files");
                    if (ServerStatus.IG("log_debug_level") >= 2) {
                        Log.log("SSH_SERVER", 2, "SFTP:writeFile:" + VRL.safe(item));
                    }
                    buffered = false;
                    try {
                        if (!item.getProperty("allow_write", "false").equals("true")) break block40;
                        if (item.getProperty("open_text", "false").equals("true")) {
                            offset = new UnsignedInteger64(Long.parseLong(item.getProperty("size", "0")));
                        }
                        if (outStream == null) {
                            this.delay();
                            if (item.getProperty("type", "").equalsIgnoreCase("DIR") && !item.getProperty("name").toUpperCase().endsWith(".ZIPSTREAM")) {
                                throw new IOException("File cannot replace a directory.");
                            }
                            if (item.getProperty("socket_closed", "false").equals("true")) {
                                throw new IOException("Socket closed already.");
                            }
                            this.thisSession.uiPUT("the_command_data", path);
                            this.thisSession.uiPUT("the_command", "beforeUpload_SSH");
                            this.thisSession.add_log_formatted("STOR START " + path_handle + " pos:" + offset.longValue(), "STOR");
                            Properties p = new Properties();
                            p.put("actual_path", path);
                            p.put("message_string", "");
                            this.thisSession.runPlugin("beforeCommand", p);
                            if (!p.getProperty("message_string", "").equals("")) {
                                throw new IOException(p.getProperty("message_string"));
                            }
                            path = p.getProperty("actual_path");
                            this.thisSession.uiPUT("the_command", "STOR");
                            this.thisSession.add_log_formatted("STOR " + path_handle, "STOR");
                            long too_big = 0x640000000000L;
                            if (offset.longValue() > too_big) {
                                offset = new UnsignedInteger64(0L);
                            }
                            this.getOutputStream(offset.longValue(), path, item);
                            item.put("buffer", new Properties());
                            outStream = (OutputStream)item.get("outputstream");
                            stor_files = (STOR_handler)item.get("stor_files");
                            item.put("size", String.valueOf(offset.longValue()));
                            p = new Properties();
                            p.put("command_code", "0");
                            p.put("command_data", String.valueOf(path));
                            this.thisSession.runPlugin("afterCommand", p);
                            break block40;
                        }
                        if (stor_files.inError) {
                            throw new Exception(stor_files.stop_message);
                        }
                        if (Long.parseLong(item.getProperty("size", "0")) == offset.longValue()) break block40;
                        if (System.getProperty("crushftp.sftp_buffered_write", "true").equals("true")) {
                            if (!item.getProperty("allow_write", "false").equals("true")) break block40;
                            Properties buffer = (Properties)item.get("buffer");
                            Log.log("SSH_SERVER", 2, "Offset out of sequence during STOR of file:" + path_handle + " : " + item.getProperty("size", "0") + " vs." + (offset.longValue() - (long)off) + ":buffer count=" + buffer.size());
                            if (buffer.size() > ServerStatus.IG("sftp_chunk_buffer")) {
                                Log.log("SSH_SERVER", 0, "Offset out of sequence during STOR of file:" + path_handle + " : " + item.getProperty("size", "0") + " vs." + (offset.longValue() - (long)off) + ":buffer count=" + buffer.size());
                                throw new IOException("Streaming out of sequence buffer full:" + buffer.size());
                            }
                            byte[] b = new byte[len];
                            System.arraycopy(data, off, b, 0, len);
                            if (offset.longValue() >= Long.MAX_VALUE) break block40;
                            buffer.put(String.valueOf(offset.longValue()), b);
                            buffered = true;
                            if (ServerStatus.IG("log_debug_level") >= 2) {
                                Log.log("SSH_SERVER", 2, "SFTP:writeFile:BUFFERED:" + path_handle + ":size=" + item.getProperty("size") + ":offset=" + offset.longValue() + ":off=" + off + ":len=" + len + ":data_len=" + data.length + ":buffered=" + buffered);
                            }
                            item.put("zero_wanted", "false");
                            if (buffer.size() > 100) {
                                Thread.sleep(buffer.size());
                            }
                            if (stor_files.slow_transfer > 0.0f) {
                                Thread.sleep((int)stor_files.slow_transfer);
                            }
                            break block40;
                        }
                        Log.log("SSH_SERVER", 0, "Offset jump during STOR of file:" + path_handle + " : " + item.getProperty("size", "0") + " vs." + (offset.longValue() - (long)off));
                        item.remove("stor_files");
                        item.remove("outputstream");
                        outStream.close();
                        while (true) {
                            if (!stor_files.active2.getProperty("active", "").equals("true")) {
                                try {
                                    stor_files.c.close();
                                }
                                catch (Exception buffer) {
                                    // empty catch block
                                }
                                break;
                            }
                            Thread.sleep(1L);
                        }
                        if (stor_files.quota_exceeded) {
                            throw new IOException("Quota Exceeded.");
                        }
                        if (item.getProperty("socket_closed", "false").equals("true")) {
                            throw new IOException("Socket closed already.");
                        }
                        this.getOutputStream(offset.longValue(), path, item);
                        outStream = (OutputStream)item.get("outputstream");
                        item.put("size", String.valueOf(offset.longValue()));
                    }
                    catch (Exception e) {
                        Log.log("SSH_SERVER", 1, e);
                        this.thisSession.add_log_formatted("550 STOR error:" + e.getMessage(), "STOR");
                        this.thisSession.doErrorEvent(e);
                        throw new IOException(e.getMessage());
                    }
                }
                if (ServerStatus.IG("log_debug_level") >= 2) {
                    Log.log("SSH_SERVER", 2, "SFTP:writeFile:" + path_handle + ":size=" + item.getProperty("size") + ":offset=" + offset.longValue() + ":off=" + off + ":len=" + len + ":data_len=" + data.length + ":buffered=" + buffered);
                }
                try {
                    if (!item.getProperty("allow_write", "false").equals("true")) return;
                    if (buffered) return;
                    try {
                        outStream.write(data, off, len);
                        outStream.flush();
                        if (stor_files.inError) {
                            throw new IOException(stor_files.stop_message);
                        }
                        item.put("zero_wanted", "false");
                        if (offset.longValue() + (long)len > Long.parseLong(item.getProperty("size"))) {
                            item.put("size", String.valueOf(offset.longValue() + (long)len));
                        }
                        if (!System.getProperty("crushftp.sftp_buffered_write", "true").equals("true")) return;
                        long pos = Long.parseLong(item.getProperty("size"));
                        Properties buffer = (Properties)item.get("buffer");
                        while (buffer.containsKey(String.valueOf(pos))) {
                            byte[] b = (byte[])buffer.remove(String.valueOf(pos));
                            outStream.write(b);
                            outStream.flush();
                            item.put("size", String.valueOf(pos += (long)b.length));
                        }
                    }
                    catch (SocketException e) {
                        if (item.getProperty("socket_closed", "false").equals("true")) {
                            throw e;
                        }
                        if (stor_files == null) throw e;
                        if (stor_files.data_sock != null) {
                            if (!stor_files.data_sock.isClosed()) throw e;
                        }
                        Log.log("SSH_SERVER", 0, "Tried to write extra bytes after socket closed, " + len + "bytes discarded.");
                        item.put("socket_closed", "true");
                    }
                }
                catch (IOException e) {
                    Log.log("SSH_SERVER", 0, "Exception on write1:" + Thread.currentThread().getName() + ":" + len);
                    Log.log("SSH_SERVER", 0, e);
                    this.thisSession.add_log_formatted("550 STOR error:" + e.getMessage(), "STOR");
                    throw e;
                }
                return;
            }
        }
        finally {
            this.Tout();
        }
    }

    public int readFile(byte[] handle, UnsignedInteger64 offset, byte[] buf, int off, int numBytesToRead) throws InvalidHandleException, EOFException, IOException {
        try {
            Properties alreadyRead;
            boolean bufferedRead;
            InputStream inStream;
            Properties item;
            block26: {
                this.Tin();
                try {
                    this.thisSession.stop_idle_timer();
                    this.thisSession.start_idle_timer();
                }
                catch (Exception e) {
                    Log.log("SSH_SERVER", 1, e);
                }
                String path_handle = new String(handle, "UTF8");
                String path = path_handle.substring(0, path_handle.lastIndexOf(":"));
                path = this.fixPath(path);
                if (ServerStatus.IG("log_debug_level") >= 3) {
                    Log.log("SSH_SERVER", 3, "SFTP:readFile:" + path_handle);
                }
                item = (Properties)this.openFiles.get(path_handle);
                inStream = (InputStream)item.get("inputstream");
                RETR_handler retr_files = (RETR_handler)item.get("retr_files");
                if (ServerStatus.IG("log_debug_level") >= 3) {
                    Log.log("SSH_SERVER", 3, "SFTP:readFile:" + VRL.safe(item));
                }
                bufferedRead = false;
                try {
                    if (inStream == null) {
                        this.delay();
                        this.thisSession.add_log_formatted("RETR " + path_handle, "RETR");
                        long too_big = 0x640000000000L;
                        if (offset.longValue() > too_big) {
                            offset = new UnsignedInteger64(0L);
                        }
                        Log.log("SSH_SERVER", 1, "SFTP:readFile3:" + path_handle + ":offset=" + offset.longValue() + ":off=" + off + ":numbytes=" + numBytesToRead + ":" + VRL.safe(item));
                        this.getInputStream(offset.longValue(), path, item);
                        inStream = (InputStream)item.get("inputstream");
                        item.put("loc", String.valueOf(offset.longValue()));
                        break block26;
                    }
                    long loc = Long.parseLong(item.getProperty("loc", "0"));
                    alreadyRead = (Properties)item.get("alreadyRead");
                    while (alreadyRead.containsKey(String.valueOf(loc))) {
                        loc += Long.parseLong(alreadyRead.getProperty(String.valueOf(loc)));
                        item.put("loc", String.valueOf(loc));
                    }
                    boolean restartRETR = false;
                    if (loc < offset.longValue()) {
                        long bytesToSkip = offset.longValue() - loc;
                        if (bytesToSkip > 0x500000L) {
                            restartRETR = true;
                        } else {
                            bufferedRead = true;
                            inStream.mark(0x500000);
                            while (bytesToSkip > 0L) {
                                long bytes = inStream.skip(bytesToSkip);
                                if (bytes <= 0L) {
                                    bytesToSkip = 0L;
                                    continue;
                                }
                                if (bytes < 0L) continue;
                                bytesToSkip -= bytes;
                            }
                        }
                    }
                    if (loc <= offset.longValue() && !restartRETR) break block26;
                    Log.log("SSH_SERVER", 1, "SFTP:readFile4:" + path_handle + ": changing location in stream...offset:" + offset.longValue() + "  vs   lastPos:" + loc + ": openFiles size:" + this.openFiles.size());
                    if (offset.longValue() >= Long.parseLong(item.getProperty("size", "0"))) break block26;
                    item.remove("retr_files");
                    item.remove("inputstream");
                    inStream.close();
                    while (retr_files.active2.getProperty("active", "").equals("true")) {
                        Thread.sleep(1L);
                    }
                    try {
                        retr_files.c.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    this.getInputStream(offset.longValue(), path, item);
                    inStream = (InputStream)item.get("inputstream");
                    item.put("loc", String.valueOf(offset.longValue()));
                }
                catch (EOFException e) {
                    throw e;
                }
                catch (Exception e) {
                    Log.log("SSH_SERVER", 1, e);
                    if (e.toString().indexOf("EOF") >= 0) {
                        throw new EOFException(e.toString());
                    }
                    this.thisSession.add_log_formatted("550 RETR error:" + e.getMessage(), "RETR");
                    this.thisSession.doErrorEvent(e);
                    throw new IOException(e.getMessage());
                }
            }
            int totalBytesRead = 0;
            int read = 0;
            while (read >= 0 && totalBytesRead < numBytesToRead) {
                read = inStream.read(buf, off, numBytesToRead - totalBytesRead);
                if (read < 0) continue;
                totalBytesRead += read;
                off += read;
            }
            if (totalBytesRead >= 0 && !bufferedRead) {
                item.put("loc", String.valueOf(offset.longValue() + (long)totalBytesRead));
            }
            if (bufferedRead) {
                alreadyRead = (Properties)item.get("alreadyRead");
                alreadyRead.put(String.valueOf(offset.longValue()), String.valueOf(totalBytesRead));
                inStream.reset();
            }
            if (totalBytesRead == 0 && read == -1) {
                throw new EOFException(LOC.G("The file is EOF"));
            }
            int n = totalBytesRead;
            return n;
        }
        finally {
            this.Tout();
        }
    }

    public void closeFile(byte[] handle) throws InvalidHandleException, IOException {
        try {
            this.Tin();
            this.closeFileMsg(handle, null);
        }
        finally {
            this.Tout();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void closeFileMsg(byte[] handle, String error_msg) throws InvalidHandleException, IOException {
        String path_handle;
        String path = path_handle = new String(handle, "UTF8");
        if (path_handle.indexOf(":") > 0) {
            path = path_handle.substring(0, path_handle.lastIndexOf(":"));
        }
        this.delay();
        path = this.fixPath(path);
        Object object = this.closeFileLock;
        synchronized (object) {
            block48: {
                Log.log("SSH_SERVER", 0, "SFTP:closeFile:" + path_handle);
                Properties item = (Properties)this.openFiles.get(path_handle);
                if (item != null) {
                    if (item.get("inputstream") == null && item.get("outputstream") == null && ServerStatus.BG("event_empty_files") && item.getProperty("allow_write", "false").equals("true")) {
                        String sshRand = this.thisSession.uiSG("randomaccess");
                        this.thisSession.uiPUT("randomaccess", "true");
                        this.writeFile(handle, new UnsignedInteger64(String.valueOf(0)), new byte[0], 0, 0);
                        this.thisSession.uiPUT("randomaccess", sshRand);
                        try {
                            Thread.sleep(200L);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    item.put("socket_closed", "true");
                    this.openFiles.remove(path_handle);
                    STOR_handler stor_files = null;
                    RETR_handler retr_files = null;
                    if (item.get("outputstream") != null) {
                        Properties buffer = (Properties)item.remove("buffer");
                        if (buffer.size() > 0) {
                            String missing_parts = "";
                            Enumeration<Object> keys = buffer.keys();
                            while (keys.hasMoreElements()) {
                                String part = "" + keys.nextElement();
                                buffer.remove(part);
                                if (part.equals("9223372036854775807")) continue;
                                missing_parts = String.valueOf(missing_parts) + part + ",";
                                stor_files = (STOR_handler)item.get("stor_files");
                                if (stor_files != null) {
                                    stor_files.inError = true;
                                    stor_files.stop_message = "Missing file segements in upload:" + missing_parts;
                                    stor_files.thisThread.interrupt();
                                }
                                this.thisSession.add_log_formatted("STOR END   " + path_handle + ":ABORTED!", "STOR");
                            }
                        }
                        if (error_msg != null) {
                            this.thisSession.put("blockUploads", "true");
                            stor_files = (STOR_handler)item.get("stor_files");
                            if (stor_files != null) {
                                stor_files.inError = true;
                                stor_files.stop_message = error_msg;
                                try {
                                    stor_files.thisThread.interrupt();
                                }
                                catch (Exception missing_parts) {
                                    // empty catch block
                                }
                            }
                            this.thisSession.add_log_formatted("STOR END   " + path_handle + ":ABORTED! " + error_msg, "STOR");
                        }
                        try {
                            ((OutputStream)item.get("outputstream")).close();
                            this.thisSession.add_log_formatted("STOR END   " + path_handle, "STOR");
                        }
                        catch (Exception missing_parts) {
                            // empty catch block
                        }
                        stor_files = (STOR_handler)item.remove("stor_files");
                        try {
                            Socket sock_tmp = stor_files.data_sock;
                            int i = 1;
                            long start_time = System.currentTimeMillis();
                            while (!(sock_tmp == null || System.currentTimeMillis() - start_time >= 300000L || sock_tmp.isClosed() && stor_files.active2.getProperty("active", "").equals("false"))) {
                                Thread.sleep(i++);
                            }
                        }
                        catch (Exception sock_tmp) {
                            // empty catch block
                        }
                        if (stor_files != null && stor_files.stop_message != null && !stor_files.stop_message.equals("") && stor_files.inError) {
                            String msg = stor_files.stop_message;
                            stor_files.stop_message = "";
                            stor_files.freeStor();
                            if (ServerStatus.BG("ssh_runtime_exception")) {
                                throw new RuntimeException(msg);
                            }
                            this.thisSession.add_log_formatted("550 STOR close error:" + msg, "STOR");
                            this.thisSession.doErrorEvent(new IOException(msg));
                            throw new IOException(msg);
                        }
                        stor_files.freeStor();
                    }
                    if (item.get("inputstream") != null) {
                        try {
                            ((InputStream)item.get("inputstream")).close();
                            this.thisSession.add_log_formatted("RETR END   " + path_handle, "RETR");
                        }
                        catch (Exception buffer) {
                            // empty catch block
                        }
                        retr_files = (RETR_handler)item.remove("retr_files");
                        if (error_msg != null) {
                            retr_files.inError = true;
                            retr_files.stop_message = error_msg;
                            this.thisSession.add_log_formatted("RETR END   " + path_handle + ":ABORTED! " + error_msg, "STOR");
                            try {
                                retr_files.thisThread.interrupt();
                            }
                            catch (Exception buffer) {
                                // empty catch block
                            }
                        }
                        try {
                            int i = 1;
                            long start_time = System.currentTimeMillis();
                            while (retr_files.active2.getProperty("active", "").equals("true") && System.currentTimeMillis() - start_time < 300000L) {
                                Thread.sleep(i++);
                            }
                        }
                        catch (Exception i) {
                            // empty catch block
                        }
                        if (retr_files != null && retr_files.stop_message != null && !retr_files.stop_message.equals("") && retr_files.inError) {
                            String msg = retr_files.stop_message;
                            retr_files.stop_message = "";
                            if (!ServerStatus.BG("ignore_ssh_closefile_download_error")) {
                                if (ServerStatus.BG("ssh_runtime_exception")) {
                                    throw new RuntimeException(msg);
                                }
                                this.thisSession.add_log_formatted("550 RETR close error:" + msg, "RETR");
                                this.thisSession.doErrorEvent(new IOException(msg));
                                throw new IOException(msg);
                            }
                        }
                    }
                    if (item.getProperty("zero_wanted", "false").equals("true")) {
                        GenericClient c = null;
                        try {
                            try {
                                c = this.thisSession.uVFS.getClient(item);
                                c.upload_0_byte(new VRL(item.getProperty("url")).getPath());
                            }
                            catch (Exception e) {
                                this.thisSession.add_log_formatted("550 RETR close error:" + e.getMessage(), "RETR");
                                Log.log("SSH_SERVER", 1, e);
                                try {
                                    c = this.thisSession.uVFS.releaseClient(c);
                                }
                                catch (Exception e2) {
                                    Log.log("SSH_SERVER", 1, e2);
                                }
                                break block48;
                            }
                        }
                        catch (Throwable throwable) {
                            try {
                                c = this.thisSession.uVFS.releaseClient(c);
                            }
                            catch (Exception e) {
                                Log.log("SSH_SERVER", 1, e);
                            }
                            throw throwable;
                        }
                        try {
                            c = this.thisSession.uVFS.releaseClient(c);
                        }
                        catch (Exception e) {
                            Log.log("SSH_SERVER", 1, e);
                        }
                    }
                } else {
                    this.dirList.remove(String.valueOf(path) + "_status");
                    this.dirList.remove(String.valueOf(path) + "_list");
                    this.dirList.remove(String.valueOf(path) + "_hash");
                }
            }
        }
    }

    public void removeFile(String path) throws PermissionDeniedException, IOException, FileNotFoundException {
        try {
            this.Tin();
            try {
                this.thisSession.stop_idle_timer();
                this.thisSession.start_idle_timer();
            }
            catch (Exception e) {
                Log.log("SSH_SERVER", 1, e);
            }
            this.delay();
            if (path.endsWith("/*")) {
                this.quickLookupDirItem.clear();
                this.thisSession.add_log_formatted("550 DELE error:%DELE-error%", "DELE");
                this.thisSession.doErrorEvent(new FileNotFoundException("DELE error: There is no file in:" + path));
                throw new FileNotFoundException("%DELE-error There is no file.%");
            }
            if (!this.getFileAttributes(path = this.fixPath(path)).isFile()) {
                this.quickLookupDirItem.clear();
                this.thisSession.add_log_formatted("550 DELE error:%DELE-error%", "DELE");
                this.thisSession.doErrorEvent(new FileNotFoundException("DELE error: There is no file in:" + path));
                throw new FileNotFoundException("%DELE-error There is no file.%");
            }
            Log.log("SSH_SERVER", 2, "SFTP:removeFile:" + path);
            this.thisSession.uiPUT("the_command_data", path);
            this.thisSession.uiPUT("the_command", "DELE");
            this.thisSession.runPlugin("beforeCommand", null);
            String result = "";
            try {
                try {
                    result = this.thisSession.do_DELE(false, path);
                    Log.log("SSH_SERVER", 2, "SFTP:removeFile:" + result);
                }
                catch (Exception e) {
                    this.thisSession.add_log_formatted("Delete failed" + e.getMessage(), "DELE");
                    throw new IOException(e.getMessage());
                }
                if (result.equals("%DELE-error%")) {
                    this.thisSession.add_log_formatted("550 DELE error:%DELE-error%", "DELE");
                    this.thisSession.doErrorEvent(new IOException("DELE error:" + path));
                    throw new IOException("%DELE-error%");
                }
                if (result.equals("%DELE-not found%")) {
                    this.thisSession.add_log_formatted("550 DELE error:%DELE-not found%", "DELE");
                    throw new FileNotFoundException("%DELE-not found%");
                }
                if (result.equals("%DELE-bad%")) {
                    this.thisSession.add_log_formatted("550 DELE error:%DELE-bad%", "DELE");
                    throw new PermissionDeniedException("%DELE-bad%");
                }
            }
            finally {
                Properties p = new Properties();
                p.put("command_code", "0");
                p.put("command_data", String.valueOf(result));
                this.thisSession.runPlugin("afterCommand", p);
            }
            this.quickLookupDirItem.clear();
        }
        finally {
            this.Tout();
        }
    }

    public void renameFile(String oldpath, String newpath) throws PermissionDeniedException, FileNotFoundException, IOException {
        try {
            this.Tin();
            try {
                this.thisSession.stop_idle_timer();
                this.thisSession.start_idle_timer();
            }
            catch (Exception e) {
                Log.log("SSH_SERVER", 1, e);
            }
            this.delay();
            oldpath = this.fixPath(oldpath);
            newpath = this.fixPath(newpath);
            this.thisSession.add_log_formatted("RNFR " + oldpath, "RNFR");
            this.thisSession.add_log_formatted("RNTO " + newpath, "RNTO");
            Log.log("SSH_SERVER", 2, "SFTP:renameFile:" + oldpath);
            Log.log("SSH_SERVER", 2, "SFTP:renameFile:" + newpath);
            this.thisSession.uiPUT("the_command_data", oldpath);
            this.thisSession.uiPUT("the_command", "RNFR");
            this.thisSession.runPlugin("beforeCommand", null);
            String result = "";
            try {
                result = this.thisSession.do_RNFR(oldpath);
                Log.log("SSH_SERVER", 2, "SFTP:renameFile:" + result);
            }
            catch (Exception e) {
                this.thisSession.add_log_formatted("550 RNFR error:" + e.getMessage(), "RNFR");
                this.thisSession.doErrorEvent(e);
                throw new IOException(e.getMessage());
            }
            if (result.indexOf("%RNFR-not found%") >= 0) {
                this.thisSession.add_log_formatted("550 RNFR error:%RNFR-not found%", "RNFR");
                throw new FileNotFoundException("%RNFR-not found%");
            }
            if (result.indexOf("%RNFR-bad%") >= 0) {
                this.thisSession.add_log_formatted("550 RNFR error:%RNFR-bad%", "RNFR");
                this.thisSession.doErrorEvent(new IOException("RNFR bad:" + oldpath + " -> " + newpath));
                throw new PermissionDeniedException("%RNFR-bad%");
            }
            this.thisSession.uiPUT("the_command_data", newpath);
            this.thisSession.uiPUT("the_command", "RNTO");
            this.thisSession.runPlugin("beforeCommand", null);
            result = "";
            try {
                try {
                    boolean rnto_overwrite = ServerStatus.BG("ssh_rename_overwrite");
                    if (!this.thisSession.user.getProperty("rnto_overwrite", "").equals("")) {
                        rnto_overwrite = this.thisSession.BG("rnto_overwrite");
                    }
                    result = this.thisSession.do_RNTO(rnto_overwrite, oldpath, newpath);
                }
                catch (Exception e) {
                    this.thisSession.add_log_formatted("550 RNFR error:" + e.getMessage(), "RNFR");
                    throw new IOException(e.getMessage());
                }
                if (result.indexOf("%RNTO-error%") >= 0) {
                    this.thisSession.add_log_formatted("550 RNTO error:%RNTO-error%", "RNTO");
                    throw new IOException("%RNTO-error%");
                }
                if (result.indexOf("%RNTO-bad_ext%") >= 0) {
                    this.thisSession.add_log_formatted("550 RNTO error:%RNTO-bad_ext%", "RNTO");
                    throw new PermissionDeniedException("%RNTO-bad_ext%");
                }
                if (result.indexOf("%RNTO-bad%") >= 0) {
                    this.thisSession.add_log_formatted("550 RNTO error:%RNTO-bad%", "RNTO");
                    throw new PermissionDeniedException("%RNTO-bad%");
                }
            }
            finally {
                Properties p = new Properties();
                p.put("command_code", "0");
                p.put("command_data", String.valueOf(result));
                this.thisSession.runPlugin("afterCommand", p);
            }
            this.quickLookupDirItem.clear();
        }
        finally {
            this.Tout();
        }
    }

    public void removeDirectory(String path) throws PermissionDeniedException, FileNotFoundException, IOException {
        try {
            this.Tin();
            try {
                this.thisSession.stop_idle_timer();
                this.thisSession.start_idle_timer();
            }
            catch (Exception e) {
                Log.log("SSH_SERVER", 1, e);
            }
            this.delay();
            path = this.fixPath(path);
            Log.log("SSH_SERVER", 2, "SFTP:removeDirectory:" + path);
            this.thisSession.uiPUT("the_command_data", path);
            this.thisSession.uiPUT("the_command", "RMD");
            this.thisSession.runPlugin("beforeCommand", null);
            String result = "";
            try {
                try {
                    result = this.thisSession.do_RMD(path);
                    Log.log("SSH_SERVER", 2, "SFTP:removeDirectory:" + result);
                    if (result.equals("%RMD-not_empty%") && ServerStatus.BG("sftp_recurse_delete")) {
                        result = this.thisSession.do_DELE(true, path);
                        result = Common.replace_str(result, "DELE", "RMD");
                        this.thisSession.add_log_formatted("RMD (deleted) " + path + "    " + result, "RMD");
                        this.thisSession.uVFS.reset();
                    }
                }
                catch (Exception e) {
                    Log.log("SSH_SERVER", 1, e);
                    throw new IOException(e.getMessage());
                }
                if (result.equals("%RMD-not_empty%")) {
                    this.thisSession.add_log_formatted("550 RMD error:%RMD-not_empty%", "RMD");
                    throw new IOException("%RMD-not_empty%");
                }
                if (result.equals("%RMD-not_found%")) {
                    this.thisSession.add_log_formatted("550 RMD error:%RMD-not_found%", "RMD");
                    throw new FileNotFoundException("%RMD-not_found%");
                }
                if (result.equals("%RMD-bad%")) {
                    this.thisSession.add_log_formatted("550 RMD error:%RMD-bad%", "RMD");
                    this.thisSession.doErrorEvent((Exception)new PermissionDeniedException("RMD-bad:" + path));
                    throw new PermissionDeniedException("%RMD-bad%");
                }
            }
            finally {
                Properties p = new Properties();
                p.put("command_code", "0");
                p.put("command_data", String.valueOf(result));
                this.thisSession.runPlugin("afterCommand", p);
            }
            this.quickLookupDirItem.clear();
        }
        finally {
            this.Tout();
        }
    }

    public void setFileAttributes(String path, SftpFileAttributes attrs) throws PermissionDeniedException, IOException, FileNotFoundException {
        try {
            this.Tin();
            try {
                Log.log("SSH_SERVER", 2, "SFTP:setFileAttributes1:" + path);
                this.setFileAttributes(path.getBytes("UTF8"), attrs);
                Log.log("SSH_SERVER", 2, "SFTP:setFileAttributes2:" + path);
            }
            catch (Exception e) {
                this.thisSession.add_log_formatted("550 setFileAttributes error:" + e.getMessage(), "CHMOD");
                throw new IOException(e.getMessage());
            }
        }
        finally {
            this.Tout();
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void setFileAttributes(byte[] handle, SftpFileAttributes attrs) throws PermissionDeniedException, IOException, InvalidHandleException {
        try {
            String path_handle;
            this.Tin();
            String path = path_handle = new String(handle, "UTF8");
            if (path_handle.indexOf(":") > 0) {
                path = path_handle.substring(0, path_handle.lastIndexOf(":"));
            }
            this.delay();
            path = this.fixPath(path);
            this.thisSession.add_log_formatted("MDTM " + path + "  " + attrs.getModifiedTime().longValue() * 1000L, "MDTM");
            Log.log("SSH_SERVER", 2, "SFTP:setFileAttributes3:" + path);
            Properties item = null;
            try {
                int x = 0;
                while (x < 100 && (item = this.thisSession.uVFS.get_item(path)) == null && (item = (Properties)this.openFiles.get(path_handle)) == null) {
                    Thread.sleep(100L);
                    ++x;
                }
            }
            catch (Exception x) {
                // empty catch block
            }
            if (ServerStatus.IG("log_debug_level") >= 2) {
                Log.log("SSH_SERVER", 2, "SFTP:setFileAttributes3:" + VRL.safe(item));
            }
            try {
                VRL vrl;
                GenericClient c;
                block25: {
                    c = this.thisSession.uVFS.getClient(item);
                    vrl = new VRL(item.getProperty("url"));
                    this.thisSession.uiPUT("the_command", "STOR");
                    this.thisSession.add_log_formatted("STOR START " + path, "STOR");
                    if (!this.thisSession.check_access_privs(path, "STOR")) {
                        throw new PermissionDeniedException("MDTM " + LOC.G("Denied!"));
                    }
                    boolean disable_mdtm_modifications = ServerStatus.BG("disable_mdtm_modifications");
                    if (this.thisSession.user.containsKey("disable_mdtm_modifications")) {
                        disable_mdtm_modifications = this.thisSession.BG("disable_mdtm_modifications");
                    }
                    if (attrs.getModifiedTime().longValue() > 0L && !disable_mdtm_modifications) {
                        c.mdtm(vrl.getPath(), attrs.getModifiedTime().longValue() * 1000L);
                        if (this.quickLookupDirItem.containsKey(path)) {
                            this.quickLookupDirItem.remove(path);
                        }
                    }
                    if (this.thisSession.SG("site").toUpperCase().indexOf("(SITE_CHMOD)") >= 0) break block25;
                    c = this.thisSession.uVFS.releaseClient(c);
                    return;
                }
                try {
                    if (attrs.getPermissions() != null && attrs.getPermissions().intValue() > 0) {
                        int i = attrs.getPermissions().intValue();
                        StringBuffer buf = new StringBuffer();
                        buf.append('0');
                        buf.append(this.toOct(i, 6));
                        buf.append(this.toOct(i, 3));
                        buf.append(this.toOct(i, 0));
                        String filePath = vrl.getPath();
                        if (item.get("stor_files") != null && this.thisSession.SG("temp_upload_ext").length() > 0 && !filePath.endsWith(this.thisSession.SG("temp_upload_ext"))) {
                            c.setMod(String.valueOf(filePath) + this.thisSession.SG("temp_upload_ext"), buf.toString(), "");
                            if (this.quickLookupDirItem.containsKey(path)) {
                                this.quickLookupDirItem.remove(path);
                            }
                        } else {
                            c.setMod(filePath, buf.toString(), "");
                            if (this.quickLookupDirItem.containsKey(path)) {
                                this.quickLookupDirItem.remove(path);
                            }
                        }
                    }
                    Log.log("SSH_SERVER", 2, "SFTP:setFileAttributesModified:" + attrs.getModifiedTime().longValue() * 1000L);
                    return;
                }
                finally {
                    c = this.thisSession.uVFS.releaseClient(c);
                }
            }
            catch (Exception e) {
                this.thisSession.add_log_formatted("550 setFileAttributes error:" + e.getMessage(), "CHMOD");
                this.thisSession.doErrorEvent(e);
                throw new IOException(e.getMessage());
            }
        }
        finally {
            this.Tout();
        }
    }

    private int toOct(int v, int r) {
        return (((v >>>= r) & 4) != 0 ? 4 : 0) + ((v & 2) != 0 ? 2 : 0) + ((v & 1) != 0 ? 1 : 0);
    }

    public SftpFile readSymbolicLink(String path) throws UnsupportedFileOperationException, FileNotFoundException, IOException, PermissionDeniedException {
        throw new UnsupportedFileOperationException(LOC.G("Symbolic links are not supported by the Virtual File System"));
    }

    public void createSymbolicLink(String link, String target) throws UnsupportedFileOperationException, FileNotFoundException, IOException, PermissionDeniedException {
        throw new UnsupportedFileOperationException(LOC.G("Symbolic links are not supported by the Virtual File System"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void getOutputStream(long offset, String path, Properties item) throws Exception {
        this.thisSession.uiPUT("the_command_data", path);
        this.thisSession.uiPUT("the_command", "STOR");
        this.thisSession.runPlugin("beforeCommand", null);
        if (item.getProperty("url").length() > ServerStatus.IG("max_url_length")) {
            throw new IOException("File url length too long:" + item.getProperty("url").length() + " vs. " + ServerStatus.IG("max_url_length"));
        }
        if (!this.thisSession.uiBG("gotFirstSSHOutputStream")) {
            this.thisSession.uiPUT("gotFirstSSHOutputStream", "true");
            this.thisSession.uVFS.get_item(Common.all_but_last(path));
        }
        this.delay();
        this.thisSession.uiPUT("start_resume_loc", String.valueOf(offset));
        String tempCurrentDir = this.thisSession.uiSG("current_dir");
        this.thisSession.uiPUT("current_dir", path);
        STOR_handler stor_files = (STOR_handler)item.get("stor_files");
        if (stor_files == null) {
            Vector vector = this.thisSession.stor_files_pool_free;
            synchronized (vector) {
                stor_files = this.thisSession.stor_files_pool_free.size() > 0 ? (STOR_handler)this.thisSession.stor_files_pool_free.remove(0) : new STOR_handler();
            }
        }
        if (!ServerStatus.BG("filepart_silent_ignore") && path.endsWith(".filepart")) {
            stor_files.allowTempExtensions = false;
        }
        stor_files.wait_for_parent_free = true;
        this.thisSession.stor_files_pool_used.addElement(stor_files);
        item.put("stor_files", stor_files);
        stor_files.setThreadName(String.valueOf(this.thisSession.uiSG("user_name")) + ":(" + this.thisSession.uiSG("user_number") + ")-" + this.thisSession.uiSG("user_ip") + " (stor)");
        Socket local_s = Common.getSTORSocket(this.thisSession, stor_files, "", false, path, ServerStatus.BG("ssh_randomaccess"), offset, null, item.getProperty("open_text", "false").equals("false"));
        local_s.setSoTimeout((this.thisSession.IG("max_idle_time") <= 0 ? 60 : this.thisSession.IG("max_idle_time")) * 1000 * 60);
        this.thisSession.uiPUT("current_dir", tempCurrentDir);
        int loops = 0;
        while (loops++ < 10000 && (stor_files.active2.getProperty("streamOpenStatus", "").equals("STOPPED") || stor_files.active2.getProperty("streamOpenStatus", "").equals("PENDING")) && !stor_files.inError) {
            Thread.sleep(1L);
        }
        if (stor_files.inError) {
            Thread.sleep(1000L);
            throw new IOException(stor_files.stop_message);
        }
        item.put("outputstream", local_s.getOutputStream());
        Properties p = new Properties();
        p.put("command_code", "0");
        p.put("command_data", String.valueOf(path));
        this.thisSession.runPlugin("afterCommand", p);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void getInputStream(long offset, String path, Properties item) throws Exception {
        this.thisSession.uiPUT("the_command_data", path);
        this.thisSession.uiPUT("the_command", "RETR");
        this.thisSession.runPlugin("beforeCommand", null);
        this.delay();
        this.thisSession.uiPUT("start_resume_loc", String.valueOf(offset));
        String tempCurrentDir = this.thisSession.uiSG("current_dir");
        this.thisSession.uiPUT("current_dir", path);
        RETR_handler retr_files = (RETR_handler)item.get("retr_files");
        if (retr_files == null) {
            Vector vector = this.thisSession.retr_files_pool_free;
            synchronized (vector) {
                retr_files = this.thisSession.retr_files_pool_free.size() > 0 ? (RETR_handler)this.thisSession.retr_files_pool_free.remove(0) : new RETR_handler();
            }
        }
        this.thisSession.retr_files_pool_used.addElement(retr_files);
        item.put("retr_files", retr_files);
        retr_files.setThreadName(String.valueOf(this.thisSession.uiSG("user_name")) + ":(" + this.thisSession.uiSG("user_number") + ")-" + this.thisSession.uiSG("user_ip") + " (retr)");
        Socket local_s = Common.getRETRSocket(this.thisSession, retr_files, offset, "", false, item.getProperty("open_text", "false").equals("false"));
        local_s.setSoTimeout((this.thisSession.IG("max_idle_time") <= 0 ? 60 : this.thisSession.IG("max_idle_time")) * 1000 * 60);
        this.thisSession.uiPUT("current_dir", tempCurrentDir);
        int loops = 0;
        while (loops++ < 10000 && (retr_files.active2.getProperty("streamOpenStatus", "").equals("STOPPED") || retr_files.active2.getProperty("streamOpenStatus", "").equals("PENDING"))) {
            Thread.sleep(1L);
        }
        item.put("inputstream", new BufferedInputStream(local_s.getInputStream()));
        item.put("alreadyRead", new Properties());
        Properties p = new Properties();
        p.put("command_code", "0");
        p.put("command_data", String.valueOf(path));
        this.thisSession.runPlugin("afterCommand", p);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void closeFilesystem() {
        this.quickLookupDirItem.clear();
        this.dirList.clear();
        if (this.thisSession != null && this.thisSession.uiBG("didDisconnect")) {
            return;
        }
        Object object = this.thisSession.close_session_sync;
        synchronized (object) {
            Properties p = new Properties();
            p.putAll((Map<?, ?>)this.openFiles);
            Enumeration<Object> keys = p.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                Properties item = (Properties)this.openFiles.get(key);
                if (item == null) continue;
                try {
                    this.closeFileMsg(key.getBytes(), "SSH Session disconnected while transfer in progress!");
                }
                catch (Exception e) {
                    Log.log("SSH_SERVER", 1, e);
                }
            }
            Vector connected_channels = null;
            Properties properties = cross_session_lookup;
            synchronized (properties) {
                connected_channels = (Vector)cross_session_lookup.get(this.session_id);
                connected_channels.remove(this.RANDOM_CLASS_UID);
                if (connected_channels.size() == 0) {
                    cross_session_lookup.remove(this.session_id);
                }
            }
            if (this.thisSession != null && this.thisSession.uVFS != null && connected_channels.size() == 0) {
                this.thisSession.uVFS.disconnect();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean fileExists(String path) throws IOException {
        Properties item;
        block7: {
            path = this.fixPath(path);
            item = null;
            if (path.endsWith(":filetree")) {
                path = path.substring(0, path.lastIndexOf(":"));
            }
            item = (Properties)this.quickLookupDirItem.get(path);
            try {
                if (item != null) break block7;
                Object object = this.statLock;
                synchronized (object) {
                    if (item == null) {
                        item = this.thisSession.uVFS.get_item(path);
                    }
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        this.thisSession.add_log_formatted("fileExists " + path + ":" + (item != null), "MDTM");
        return item != null;
    }

    public String fixPath(String path) {
        return this.fixPath(path, true);
    }

    public String fixPath(String path, boolean resolve_star) {
        try {
            if (path.startsWith("\"") && path.endsWith("\"")) {
                path = path.substring(1, path.length() - 1);
            }
            path = com.crushftp.client.Common.dots(path);
        }
        catch (Exception exception) {}
        while (path.startsWith(".")) {
            path = path.substring(1);
        }
        if (path.equals("/")) {
            path = this.thisSession.SG("root_dir");
        }
        if (path.toUpperCase().startsWith("/") && !path.toUpperCase().startsWith(this.thisSession.SG("root_dir").toUpperCase())) {
            path = String.valueOf(this.thisSession.SG("root_dir")) + path.substring(1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.startsWith(this.thisSession.SG("root_dir"))) {
            path = String.valueOf(this.thisSession.SG("root_dir")) + path.substring(1);
        }
        if (path.endsWith("/*") && resolve_star) {
            if (!this.isEmptyFolder(path.substring(0, path.length() - 1), resolve_star)) {
                path = path.substring(0, path.length() - 1);
            }
        } else if (path.endsWith("*")) {
            path = path.substring(0, path.length() - 1);
        }
        while (path.indexOf("//") >= 0) {
            path = Common.replace_str(path, "//", "/");
        }
        return path;
    }

    private boolean isEmptyFolder(String path, boolean resolve_star) {
        boolean is_empty = false;
        try {
            this.openDirectory(path);
            SftpFile[] files = this.readDirectory2(path, false);
            if (files != null && files.length == 0) {
                is_empty = true;
            }
            if (files != null && files.length == 2 && files[0].getFilename().equals(".") && files[1].getFilename().equals("..")) {
                is_empty = true;
            }
        }
        catch (Exception e) {
            Log.log("SSH_SERVER", 2, e);
        }
        return is_empty;
    }

    public String getDefaultPath() throws FileNotFoundException {
        String path = "/";
        try {
            if (!ServerStatus.BG("jailproxy")) {
                path = this.thisSession.uVFS.user_info.getProperty("default_current_dir", "/");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return path;
    }

    public String getRealPath(String path) throws IOException, FileNotFoundException {
        if (path.equals(".")) {
            path = this.getDefaultPath();
        }
        if (path.endsWith("/..")) {
            path = String.valueOf(path) + "/";
        }
        String last_path = Common.last(path);
        if (this.used_protocol.toUpperCase().equals("SCP") && path.endsWith("/" + last_path + "/" + last_path)) {
            path = path.substring(0, path.length() - (last_path.length() + 1));
        }
        if ((path = this.fixPath(path)).startsWith(this.thisSession.SG("root_dir"))) {
            path = path.substring(this.thisSession.SG("root_dir").length() - 1);
        }
        return path;
    }

    private void delay() throws IOException {
        int delay = 0;
        try {
            delay = Integer.parseInt(this.thisSession.server_item.getProperty("commandDelayInterval", "0"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            Thread.sleep(delay);
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    public String getPathForHandle(byte[] handle) throws InvalidHandleException, IOException {
        String path_handle;
        String path = path_handle = new String(handle, "UTF8");
        if (path_handle.indexOf(":") > 0) {
            path = path_handle.substring(0, path_handle.lastIndexOf(":"));
        }
        return path;
    }

    public void populateEvent(Event arg0) {
    }

    public void setFileNamePattern(ScpFileNamePattern filename_pattern) {
        if (filename_pattern != null) {
            Log.log("SSH_SERVER", 2, "SCP command : FNP : " + filename_pattern.getPattern() + " : SSH : " + this);
        } else {
            Log.log("SSH_SERVER", 2, "SCP command : FNP : " + (Object)((Object)filename_pattern));
        }
        this.filename_pattern = filename_pattern;
    }

    public void Tin() {
        Worker.thread_lookup.put(Thread.currentThread(), this.thisSession);
    }

    public void Tout() {
        Worker.thread_lookup.remove(Thread.currentThread());
    }
}

