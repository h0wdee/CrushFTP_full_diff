/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.File_U;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import crushftp.gui.LOC;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.server.Lister;
import crushftp.server.ServerStatus;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.net.ssl.SSLSocket;

public class LIST_handler
implements Runnable {
    public static Properties md5Hash = new Properties();
    public boolean die_now = false;
    public boolean active = false;
    public String stop_message = LOC.G("Transfer failed!");
    public Thread thisThread = null;
    String the_dir;
    boolean names_only = false;
    boolean zlibing = false;
    public OutputStream data_osw = null;
    DeflaterOutputStream data_osz = null;
    String CRLF = "\r\n";
    String message_data = "";
    String search_file = "";
    SessionCrush thisSession = null;
    boolean showListing = true;
    boolean fullPaths = false;
    Thread fullPathLister = null;
    public boolean justStreamListData = false;
    int scanDepth = 9999;
    long end1 = 0L;
    long end2 = 0L;
    Socket data_sock = null;
    boolean mlstFormat = false;
    boolean was_star = false;

    public void init_vars(String the_dir, boolean names_only, SessionCrush thisSession, String search_file, boolean showListing, boolean mlstFormat) {
        this.the_dir = the_dir;
        this.names_only = names_only;
        this.thisSession = thisSession;
        this.fullPaths = search_file.startsWith("-Q");
        if (search_file.startsWith("-")) {
            search_file = "";
        }
        this.search_file = search_file;
        this.showListing = showListing;
        this.mlstFormat = mlstFormat;
        this.was_star = this.search_file.equals("*");
        if (this.search_file.equals("*")) {
            this.search_file = "";
        }
        if (this.search_file.equals(".")) {
            this.search_file = "";
        }
        this.scanDepth = 999;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void run() {
        block115: {
            this.thisThread = Thread.currentThread();
            try {
                try {
                    block116: {
                        block113: {
                            block112: {
                                if (this.thisSession == null) break block116;
                                this.active = true;
                                this.stop_message = "";
                                try {
                                    File_U temp_dir;
                                    this.message_data = "";
                                    String parentPath = this.thisSession.uVFS.getRootVFS(this.the_dir, -1);
                                    Properties dir_item = this.thisSession.uVFS.get_item(parentPath, -1);
                                    Properties check_item = null;
                                    if (this.the_dir.equals(this.thisSession.uVFS.getPrivPath(this.the_dir))) {
                                        check_item = this.thisSession.uVFS.get_fake_item(this.the_dir, "DIR");
                                    }
                                    if (dir_item.getProperty("protocol", "file").equals("file") && (temp_dir = new File_U(String.valueOf(new VRL(check_item.getProperty("url")).getPath()) + "/.message")).exists()) {
                                        RandomAccessFile message_is = new RandomAccessFile(new File_U(temp_dir), "r");
                                        byte[] temp_array = new byte[(int)message_is.length()];
                                        message_is.readFully(temp_array);
                                        message_is.close();
                                        this.message_data = new String(temp_array);
                                    }
                                    if (check_item != null && check_item.getProperty("privs").indexOf("(comment") >= 0) {
                                        String comment = ServerStatus.thisObj.change_vars_to_values(crushftp.handlers.Common.url_decode(check_item.getProperty("privs").substring(check_item.getProperty("privs").indexOf("(comment") + 8, check_item.getProperty("privs").indexOf(")", check_item.getProperty("privs").indexOf("(comment")))), this.thisSession);
                                        this.message_data = String.valueOf(this.message_data) + comment.trim();
                                    }
                                }
                                catch (Exception parentPath) {
                                    // empty catch block
                                }
                                if (this.message_data.length() > 0) {
                                    this.message_data = String.valueOf(this.message_data.trim()) + "\r\n.";
                                }
                                if (this.search_file.startsWith("-")) {
                                    this.search_file = "";
                                }
                                int loop_times = 0;
                                while (true) {
                                    if (this.justStreamListData || this.thisSession.data_socks.size() != 0 || loop_times++ >= 200) {
                                        if (this.justStreamListData || this.thisSession.data_socks.size() != 0) break block112;
                                        if (!ServerStatus.BG("disconnect_ftp_on_socket_error")) break;
                                        this.thisSession.do_kill();
                                        break block113;
                                    }
                                    Thread.sleep(100L);
                                }
                                this.thisSession.not_done = this.thisSession.uiBG("pasv_connect") ? this.thisSession.ftp_write_command("550", LOC.G("No connection received on PASV ip:port that was specified in 20 seconds.")) : this.thisSession.ftp_write_command("550", "%PORT-fail_question%" + this.CRLF + "%PORT-no_data_connection%");
                                this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                                break block113;
                            }
                            this.data_sock = (Socket)this.thisSession.data_socks.remove(0);
                            this.data_sock.setSoTimeout((this.thisSession.IG("max_idle_time") <= 0 ? 5 : this.thisSession.IG("max_idle_time")) * 1000 * 60);
                            long quota = this.thisSession.get_quota(this.the_dir);
                            String responseCode226 = "226";
                            try {
                                Properties item;
                                if (!this.justStreamListData) {
                                    this.data_osw = new BufferedOutputStream(this.data_sock.getOutputStream());
                                }
                                if (this.thisSession.uiBG("modez")) {
                                    Deflater def = new Deflater();
                                    def.setLevel(Integer.parseInt(this.thisSession.uiSG("zlibLevel")));
                                    this.data_osz = new DeflaterOutputStream(this.data_osw, def);
                                    this.zlibing = true;
                                }
                                boolean wrote150 = false;
                                if (!this.names_only) {
                                    this.write150(quota);
                                    wrote150 = true;
                                }
                                StringBuffer c = new StringBuffer();
                                final Vector<Object> listing = new Vector<Object>();
                                final Properties status = new Properties();
                                status.put("done", "false");
                                long start = new Date().getTime();
                                if (this.fullPaths) {
                                    this.fullPathLister = new Thread(new Lister(this.thisSession, this.the_dir, listing, this.scanDepth, status));
                                    this.fullPathLister.start();
                                } else if (ServerStatus.BG("listing_multithreaded")) {
                                    Runnable r = new Runnable(){

                                        @Override
                                        public void run() {
                                            try {
                                                LIST_handler.this.thisSession.uVFS.getListing(listing, LIST_handler.this.the_dir);
                                            }
                                            catch (Exception e) {
                                                Log.log("LIST", 0, e);
                                                LIST_handler.this.stop_message = "FAILED:" + e.getMessage();
                                            }
                                            LIST_handler.this.end1 = new Date().getTime();
                                            status.put("done", "true");
                                        }
                                    };
                                    Worker.startWorker(r, String.valueOf(Thread.currentThread().getName()) + ":Listing...");
                                } else {
                                    try {
                                        this.thisSession.uVFS.getListing(listing, this.the_dir);
                                    }
                                    catch (Exception e) {
                                        Log.log("LIST", 0, e);
                                        this.stop_message = "FAILED:" + e.getMessage();
                                    }
                                    this.end1 = new Date().getTime();
                                    if (ServerStatus.BG("sort_listings")) {
                                        crushftp.handlers.Common.do_sort(listing, "name");
                                    }
                                    status.put("done", "true");
                                }
                                StringBuffer item_str = new StringBuffer();
                                Properties name_hash = new Properties();
                                Properties pp = new Properties();
                                pp.put("listing", listing);
                                this.thisSession.runPlugin("list", pp);
                                int bufferCount = 0;
                                long totalListingItems = 0L;
                                boolean has_bytes = false;
                                Pattern pattern = null;
                                if (this.search_file.length() > 0) {
                                    pattern = Common.getPattern(this.search_file, ServerStatus.BG("case_sensitive_list_search"));
                                }
                                if (this.data_sock != null && this.data_sock instanceof SSLSocket) {
                                    if (ServerStatus.BG("send_dot_dot_list_secure")) {
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
                                        dir_item.put("month", "Jan");
                                        dir_item.put("day", "01");
                                        dir_item.put("time_or_year", "1970");
                                        listing.insertElementAt(dir_item.clone(), 0);
                                        dir_item.put("name", "..");
                                        dir_item.put("url", ".");
                                        dir_item.put("root_dir", ".");
                                        dir_item.put("sftp_path", "..");
                                        listing.insertElementAt(dir_item.clone(), 0);
                                    }
                                }
                                block25: while (status.getProperty("done", "false").equalsIgnoreCase("false") || listing.size() > 0) {
                                    block117: {
                                        block126: {
                                            block123: {
                                                String prefix;
                                                block125: {
                                                    String dir;
                                                    block124: {
                                                        block122: {
                                                            block118: {
                                                                block119: {
                                                                    block121: {
                                                                        block120: {
                                                                            while (true) {
                                                                                if (!status.getProperty("done", "false").equalsIgnoreCase("false") || listing.size() != 0) {
                                                                                    if (listing.size() != 0) break;
                                                                                    break block25;
                                                                                }
                                                                                Thread.sleep(100L);
                                                                            }
                                                                            ++bufferCount;
                                                                            item = (Properties)listing.remove(0);
                                                                            if (item.getProperty("hide_smb", "").equals("true")) continue;
                                                                            item_str.setLength(0);
                                                                            if (item == null) continue;
                                                                            LIST_handler.generateLineEntry(item, item_str, false, this.the_dir, this.fullPaths, this.thisSession, this.mlstFormat);
                                                                            if (this.fullPaths) {
                                                                                this.thisSession.uiPUT("list_filetree_status", String.valueOf(totalListingItems) + ":" + listing.size() + ":" + item.getProperty("root_dir") + item.getProperty("name"));
                                                                            }
                                                                            if (!this.fullPaths && name_hash.get(String.valueOf(item.getProperty("root_dir")) + item.getProperty("name")) != null) break block117;
                                                                            if (!this.fullPaths) {
                                                                                name_hash.put(String.valueOf(item.getProperty("root_dir")) + item.getProperty("name"), "DONE");
                                                                            }
                                                                            if (!LIST_handler.checkName(item, this.thisSession, this.fullPaths, false)) break block117;
                                                                            if (!this.search_file.equals("") && !this.search_file.equals("*") && !this.search_file.endsWith("/")) break block118;
                                                                            if (!this.names_only) break block119;
                                                                            prefix = "";
                                                                            if (ServerStatus.BG("include_ftp_nlst_path")) break block120;
                                                                            if (!ServerStatus.BG("include_ftp_nlst_path_for_all_pattern")) break block121;
                                                                        }
                                                                        boolean enable = this.search_file.endsWith("/") || this.was_star;
                                                                        if (ServerStatus.BG("include_ftp_nlst_path_for_all_pattern")) {
                                                                            boolean bl = enable = this.search_file.equals("") || this.search_file.equals("*") || this.search_file.endsWith("/") || this.was_star;
                                                                        }
                                                                        if (enable) {
                                                                            String dir2 = item.getProperty("root_dir");
                                                                            if (dir2.startsWith(this.thisSession.SG("root_dir"))) {
                                                                                dir2 = dir2.substring(this.thisSession.SG("root_dir").length() - 1);
                                                                            }
                                                                            prefix = dir2.substring(1);
                                                                            String tmp1 = this.thisSession.uiSG("the_command");
                                                                            String tmp2 = this.thisSession.uiSG("the_command_data");
                                                                            this.thisSession.uiPUT("the_command", "CWD");
                                                                            this.thisSession.uiPUT("the_command_data", dir2);
                                                                            String tmp3 = this.thisSession.uiSG("dont_write");
                                                                            this.thisSession.uiPUT("dont_write", "true");
                                                                            String current_dir = this.thisSession.get_PWD();
                                                                            this.thisSession.do_CWD();
                                                                            if (!current_dir.equals(dir2)) {
                                                                                this.thisSession.uiPUT("the_command", "CWD");
                                                                                this.thisSession.uiPUT("the_command_data", current_dir);
                                                                                this.thisSession.do_CWD();
                                                                            }
                                                                            this.thisSession.uiPUT("the_command", tmp1);
                                                                            this.thisSession.uiPUT("the_command_data", tmp2);
                                                                            this.thisSession.uiPUT("dont_write", tmp3);
                                                                            if (!ServerStatus.BG("include_ftp_nlst_path_for_all_pattern")) {
                                                                                prefix = "";
                                                                            }
                                                                        }
                                                                    }
                                                                    if (this.thisSession.uiBG("list_zip_app") && item.getProperty("type", "").equals("DIR") && item.getProperty("name", "").toUpperCase().endsWith(".APP") && !this.the_dir.equals("/")) {
                                                                        item.put("permissions", "-rwxrwxrwx");
                                                                        item.put("name", String.valueOf(item.getProperty("name")) + ".zip");
                                                                    } else if (this.thisSession.uiBG("list_zip_only") && item.getProperty("type", "").equals("DIR") && !this.the_dir.equals("/")) {
                                                                        item.put("permissions", "-rwxrwxrwx");
                                                                        item.put("name", String.valueOf(item.getProperty("name")) + ".zip");
                                                                    } else if (this.thisSession.uiBG("list_zip_dir") && item.getProperty("type", "").equals("DIR") && !this.the_dir.equals("/")) {
                                                                        item.put("permissions", "-rwxrwxrwx");
                                                                        item.put("name", String.valueOf(item.getProperty("name")) + ".zip");
                                                                        c.append(String.valueOf(item.getProperty("name")) + (item.getProperty("type", "").equals("DIR") && !ServerStatus.BG("disable_dir_filter") ? "/" : "") + this.CRLF);
                                                                    }
                                                                    if (this.thisSession.uiBG("list_zip_file") && item.getProperty("type", "").equals("FILE") && !this.the_dir.equals("/")) {
                                                                        item.put("name", String.valueOf(item.getProperty("name")) + ".zip");
                                                                    }
                                                                    ++totalListingItems;
                                                                    c.append(String.valueOf(prefix) + item.getProperty("name") + (item.getProperty("type", "").equals("DIR") && !ServerStatus.BG("disable_dir_filter") ? "/" : "") + this.CRLF);
                                                                    break block117;
                                                                }
                                                                if (this.thisSession.uiBG("list_zip_app") && item.getProperty("type", "").equals("DIR") && item.getProperty("name", "").toUpperCase().endsWith(".APP") && !this.the_dir.equals("/")) {
                                                                    item.put("permissions", "-rwxrwxrwx");
                                                                    item.put("name", String.valueOf(item.getProperty("name")) + ".zip");
                                                                    item_str.setLength(0);
                                                                    LIST_handler.generateLineEntry(item, item_str, true, this.the_dir, this.fullPaths, this.thisSession, this.mlstFormat);
                                                                } else if (this.thisSession.uiBG("list_zip_only") && item.getProperty("type", "").equals("DIR") && !this.the_dir.equals("/")) {
                                                                    item.put("permissions", "-rwxrwxrwx");
                                                                    item.put("name", String.valueOf(item.getProperty("name")) + ".zip");
                                                                    item_str.setLength(0);
                                                                    LIST_handler.generateLineEntry(item, item_str, true, this.the_dir, this.fullPaths, this.thisSession, this.mlstFormat);
                                                                } else if (this.thisSession.uiBG("list_zip_dir") && item.getProperty("type", "").equals("DIR") && !this.the_dir.equals("/")) {
                                                                    item.put("permissions", "-rwxrwxrwx");
                                                                    item.put("name", String.valueOf(item.getProperty("name")) + ".zip");
                                                                    LIST_handler.generateLineEntry(item, item_str, true, this.the_dir, this.fullPaths, this.thisSession, this.mlstFormat);
                                                                }
                                                                if (this.thisSession.uiBG("list_zip_file") && item.getProperty("type", "").equals("FILE") && !this.the_dir.equals("/")) {
                                                                    item.put("name", String.valueOf(item.getProperty("name")) + ".zip");
                                                                    item_str.setLength(0);
                                                                    LIST_handler.generateLineEntry(item, item_str, true, this.the_dir, this.fullPaths, this.thisSession, this.mlstFormat);
                                                                }
                                                                ++totalListingItems;
                                                                c.append(item_str.toString());
                                                                break block117;
                                                            }
                                                            if ((!this.search_file.equals(item.getProperty("name", "")) || !ServerStatus.BG("case_sensitive_list_search")) && (!this.search_file.equalsIgnoreCase(item.getProperty("name", "")) || ServerStatus.BG("case_sensitive_list_search"))) break block122;
                                                            if (this.names_only) {
                                                                c.append(String.valueOf(item.getProperty("name")) + (item.getProperty("type", "").equals("DIR") && !ServerStatus.BG("disable_dir_filter") ? "/" : "") + this.CRLF);
                                                            } else {
                                                                c.append(item_str.toString());
                                                            }
                                                            ++totalListingItems;
                                                            break block117;
                                                        }
                                                        if (this.search_file.indexOf("*") < 0 && this.search_file.indexOf("?") < 0 && this.search_file.indexOf("$") < 0 || !Common.doFilter(pattern, item.getProperty("name", ""))) break block117;
                                                        if (!this.names_only) break block123;
                                                        prefix = "";
                                                        if (ServerStatus.BG("include_ftp_nlst_path")) break block124;
                                                        if (!ServerStatus.BG("include_ftp_nlst_path_for_all_pattern")) break block125;
                                                    }
                                                    if ((dir = item.getProperty("root_dir")).startsWith(this.thisSession.SG("root_dir"))) {
                                                        dir = dir.substring(this.thisSession.SG("root_dir").length() - 1);
                                                    }
                                                    prefix = dir.substring(1);
                                                    String tmp1 = this.thisSession.uiSG("the_command");
                                                    String tmp2 = this.thisSession.uiSG("the_command_data");
                                                    this.thisSession.uiPUT("the_command", "CWD");
                                                    this.thisSession.uiPUT("the_command_data", dir);
                                                    String tmp3 = this.thisSession.uiSG("dont_write");
                                                    this.thisSession.uiPUT("dont_write", "true");
                                                    String current_dir = this.thisSession.get_PWD();
                                                    this.thisSession.do_CWD();
                                                    if (!current_dir.equals(dir)) {
                                                        this.thisSession.uiPUT("the_command", "CWD");
                                                        this.thisSession.uiPUT("the_command_data", current_dir);
                                                        this.thisSession.do_CWD();
                                                    }
                                                    this.thisSession.uiPUT("the_command", tmp1);
                                                    this.thisSession.uiPUT("the_command_data", tmp2);
                                                    this.thisSession.uiPUT("dont_write", tmp3);
                                                    if (!ServerStatus.BG("include_ftp_nlst_path_for_all_pattern")) {
                                                        prefix = "";
                                                    }
                                                }
                                                c.append(String.valueOf(prefix) + item.getProperty("name") + (item.getProperty("type", "").equals("DIR") && !ServerStatus.BG("disable_dir_filter") ? "/" : "") + this.CRLF);
                                                break block126;
                                            }
                                            c.append(item_str.toString());
                                        }
                                        ++totalListingItems;
                                    }
                                    if (bufferCount <= ServerStatus.IG("listing_buffer_count")) continue;
                                    if (!wrote150) {
                                        this.write150(quota);
                                    }
                                    wrote150 = true;
                                    bufferCount = 0;
                                    if (c.length() <= 0) continue;
                                    if (this.showListing) {
                                        if (this.zlibing) {
                                            this.data_osz.write(new String(c.toString()).getBytes(this.SG("char_encoding")));
                                        } else {
                                            this.data_osw.write(crushftp.handlers.Common.normalize2(new String(c.toString())).getBytes(this.SG("char_encoding")));
                                        }
                                        this.thisSession.uiPPUT("bytes_sent", c.length());
                                        ServerStatus.thisObj.total_server_bytes_sent += (long)c.length();
                                        if (this.thisSession.server_item.containsKey("bytes_sent")) {
                                            Properties properties = this.thisSession.server_item;
                                            synchronized (properties) {
                                                this.thisSession.server_item.put("bytes_sent", String.valueOf(Long.parseLong(this.thisSession.server_item.getProperty("bytes_sent", "0")) + (long)c.length()));
                                            }
                                        }
                                        has_bytes = true;
                                    }
                                    c.setLength(0);
                                }
                                if (status.containsKey("stop_message")) {
                                    this.stop_message = status.getProperty("stop_message");
                                }
                                if (this.names_only && c.length() == 0 && !ServerStatus.BG("allow_nlst_empty")) {
                                    responseCode226 = "450";
                                    this.stop_message = "No such file or directory: ";
                                    throw new Exception(this.stop_message);
                                }
                                if (!wrote150) {
                                    this.write150(quota);
                                }
                                wrote150 = true;
                                if (this.stop_message.toUpperCase().indexOf("FAILED") >= 0) {
                                    throw new Exception(this.stop_message);
                                }
                                if (ServerStatus.BG("write_session_logs")) {
                                    if (ServerStatus.SG("log_allow_str").indexOf("(DIR_LIST)") >= 0) {
                                        this.thisSession.add_log(new String(c.toString()), "DIR_LIST");
                                    }
                                }
                                if (this.fullPaths) {
                                    this.thisSession.uiPUT("list_filetree_status", String.valueOf(totalListingItems) + ":Finished");
                                }
                                if (c.length() > 0) {
                                    if (this.showListing) {
                                        if (this.zlibing) {
                                            this.data_osz.write(new String(c.toString()).getBytes(this.SG("char_encoding")));
                                        } else {
                                            this.data_osw.write(crushftp.handlers.Common.normalize2(new String(c.toString())).getBytes(this.SG("char_encoding")));
                                        }
                                        this.thisSession.uiPPUT("bytes_sent", c.length());
                                        ServerStatus.thisObj.total_server_bytes_sent += (long)c.length();
                                        if (this.thisSession.server_item.containsKey("bytes_sent")) {
                                            item = this.thisSession.server_item;
                                            synchronized (item) {
                                                this.thisSession.server_item.put("bytes_sent", String.valueOf(Long.parseLong(this.thisSession.server_item.getProperty("bytes_sent", "0")) + (long)c.length()));
                                            }
                                        }
                                        has_bytes = true;
                                    }
                                    c.setLength(0);
                                }
                                if (!this.justStreamListData) {
                                    try {
                                        if (!has_bytes && this.data_sock != null && this.data_sock instanceof SSLSocket) {
                                            crushftp.handlers.Common.debug(1, this.data_sock + ":" + "Forcing SSL handshake to start...");
                                            crushftp.handlers.Common.configureSSLTLSSocket((SSLSocket)this.data_sock);
                                            ((SSLSocket)this.data_sock).startHandshake();
                                            crushftp.handlers.Common.debug(1, this.data_sock + ":" + "Forced SSL handshake complete.");
                                        }
                                    }
                                    catch (Exception e) {
                                        Log.log("SERVER", 1, e);
                                    }
                                }
                                if (this.zlibing) {
                                    this.data_osz.finish();
                                    this.data_osz.close();
                                } else {
                                    this.data_osw.flush();
                                    if (this.data_sock instanceof SSLSocket) {
                                        try {
                                            ((SSLSocket)this.data_sock).shutdownOutput();
                                        }
                                        catch (Exception exception) {
                                            // empty catch block
                                        }
                                    }
                                    this.data_osw.close();
                                }
                                if (this.data_sock != null) {
                                    this.data_sock.close();
                                }
                                this.end2 = new Date().getTime();
                                this.thisSession.uiPUT("listing_files", "false");
                                if (this.data_sock != null) {
                                    this.thisSession.old_data_socks.remove(this.data_sock);
                                }
                                if (!this.justStreamListData) {
                                    if (quota != -12345L && !ServerStatus.BG("hide_ftp_quota_log")) {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command("226-" + LOC.G("Quota space available") + ": " + Common.format_bytes_short(quota));
                                    }
                                    if (this.thisSession.IG("max_download_amount") != 0) {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command("226-" + LOC.G("Max Download") + ".  " + LOC.G("Sent") + ":" + this.thisSession.uiLG("bytes_sent") / 1024L + "k.  " + LOC.G("Max") + ":" + this.thisSession.IG("max_download_amount") + "k.  " + LOC.G("Available") + ":" + ((long)this.thisSession.IG("max_download_amount") - this.thisSession.uiLG("bytes_sent") / 1024L) + "k.");
                                    }
                                    if (this.thisSession.IG("ratio") != 0) {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command("226-" + LOC.G("Ratio is") + " " + this.thisSession.IG("ratio") + " to 1. " + LOC.G("Received") + ":" + Common.format_bytes_short(this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) + " " + LOC.G("Sent") + ":" + Common.format_bytes_short(this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent")) + "  " + LOC.G("Available") + ":" + Common.format_bytes_short((this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) * this.thisSession.LG("ratio") - (this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent"))));
                                    }
                                    this.thisSession.not_done = ServerStatus.BG("generic_ftp_responses") ? this.thisSession.ftp_write_command("226", "Directory send OK.") : this.thisSession.ftp_write_command("226", "%LIST-end% " + LOC.G("(generate:$0ms)(send:$1ms)", String.valueOf(this.end1 - start), String.valueOf(this.end2 - start)));
                                }
                            }
                            catch (Exception e) {
                                Log.log("LIST", 1, e);
                                if (quota != -12345L && !ServerStatus.BG("hide_ftp_quota_log")) {
                                    this.thisSession.not_done = this.thisSession.ftp_write_command("226-" + LOC.G("Quota space available") + ": " + Common.format_bytes_short(quota));
                                }
                                if (this.thisSession.IG("max_download_amount") != 0) {
                                    this.thisSession.not_done = this.thisSession.ftp_write_command("226-" + LOC.G("Max Download") + ".  " + LOC.G("Sent") + ":" + this.thisSession.uiLG("bytes_sent") / 1024L + "k.  " + LOC.G("Max") + ":" + this.thisSession.IG("max_download_amount") + "k.  " + LOC.G("Available") + ":" + ((long)this.thisSession.IG("max_download_amount") - this.thisSession.uiLG("bytes_sent") / 1024L) + "k.");
                                }
                                if (this.thisSession.IG("ratio") != 0) {
                                    this.thisSession.not_done = this.thisSession.ftp_write_command("226-" + LOC.G("Ratio is") + " " + this.thisSession.IG("ratio") + " to 1. " + LOC.G("Received") + ":" + (this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) / 1024L + "k " + LOC.G("Sent") + ":" + (this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent")) / 1024L + "k.  " + LOC.G("Available") + ":" + ((this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) * (long)this.thisSession.IG("ratio") - (this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent"))) / 1024L + "k.");
                                }
                                this.stop_message = String.valueOf(this.stop_message) + LOC.G("Transfer failed") + "!";
                                responseCode226 = "550";
                                this.thisSession.not_done = this.thisSession.ftp_write_command(responseCode226, this.stop_message);
                            }
                        }
                        this.thisSession.start_idle_timer();
                    }
                    if (this.fullPathLister != null) {
                        try {
                            this.fullPathLister.interrupt();
                        }
                        catch (Exception loop_times) {
                            // empty catch block
                        }
                    }
                    this.fullPathLister = null;
                    try {
                        this.thisSession.uiPUT("last_action", "LIST-Done.");
                        if (!this.justStreamListData && this.data_sock != null) {
                            this.data_sock.close();
                        }
                        this.thisSession.uiPUT("listing_files", "false");
                    }
                    catch (Exception loop_times) {
                        // empty catch block
                    }
                    this.zlibing = false;
                    this.active = false;
                }
                catch (Exception e) {
                    Log.log("LIST", 1, e);
                    this.thisThread = null;
                    break block115;
                }
            }
            catch (Throwable throwable) {
                this.thisThread = null;
                throw throwable;
            }
            this.thisThread = null;
        }
        this.kill(null);
    }

    public void kill(Thread this_thread) {
        this.die_now = true;
        try {
            if (this.data_osw != null) {
                this.data_osw.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            if (this.data_osz != null) {
                this.data_osz.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            if (!this.justStreamListData && this.data_sock != null) {
                this.data_sock.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (this.data_sock != null) {
            this.thisSession.old_data_socks.remove(this.data_sock);
        }
    }

    public void write150(long quota) throws Exception {
        if (this.justStreamListData) {
            return;
        }
        if (this.message_data.length() > 0) {
            this.thisSession.not_done = this.thisSession.ftp_write_command(String.valueOf(ServerStatus.thisObj.common_code.format_message("150-", this.message_data)) + "150-" + this.CRLF);
        }
        if (quota != -12345L && !ServerStatus.BG("hide_ftp_quota_log")) {
            this.thisSession.not_done = this.thisSession.ftp_write_command("150-" + LOC.G("Quota space available") + ": " + Common.format_bytes_short(quota));
        }
        if (this.thisSession.IG("max_download_amount") != 0) {
            this.thisSession.not_done = this.thisSession.ftp_write_command("150-" + LOC.G("Max Download") + ".  " + LOC.G("Sent") + ":" + this.thisSession.uiLG("bytes_sent") / 1024L + "k.  " + LOC.G("Max") + ":" + this.thisSession.IG("max_download_amount") + "k.  " + LOC.G("Available") + ":" + ((long)this.thisSession.IG("max_download_amount") - this.thisSession.uiLG("bytes_sent") / 1024L) + "k.");
        }
        if (this.thisSession.IG("ratio") != 0) {
            this.thisSession.not_done = this.thisSession.ftp_write_command("150-" + LOC.G("Ratio is") + " " + this.thisSession.IG("ratio") + " to 1. " + LOC.G("Received") + ":" + (this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) / 1024L + "k " + LOC.G("Sent") + ":" + (this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent")) / 1024L + "k.  " + LOC.G("Available") + ":" + ((this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) * (long)this.thisSession.IG("ratio") - (this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent"))) / 1024L + "k.");
        }
        this.thisSession.not_done = this.thisSession.ftp_write_command("150", "%LIST-start%");
    }

    public static boolean checkName(Properties item, SessionCrush thisSession, boolean fullPaths, boolean ignoreFilter) throws Exception {
        return LIST_handler.checkName(item, fullPaths, ignoreFilter, thisSession.SG("file_filter"), thisSession);
    }

    public static boolean checkName(Properties item, boolean fullPaths, boolean ignoreFilter, String file_filter, SessionCrush thisSession) throws Exception {
        if (item.getProperty("name").equals(".") || item.getProperty("name").equals("..")) {
            return true;
        }
        String filter_str = String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + file_filter;
        if (thisSession != null && filter_str.indexOf("%") >= 0) {
            filter_str = ServerStatus.thisObj.change_vars_to_values(filter_str, thisSession);
        }
        String name = item.getProperty("name");
        if (ignoreFilter || crushftp.handlers.Common.filter_check("L", name, filter_str) && crushftp.handlers.Common.filter_check("F", String.valueOf(name) + (item.getProperty("type").equalsIgnoreCase("DIR") && !name.endsWith("/") ? "/" : ""), filter_str) && crushftp.handlers.Common.filter_check("DIR", String.valueOf(name) + (item.getProperty("type").equalsIgnoreCase("DIR") && !name.endsWith("/") ? "/" : ""), filter_str)) {
            boolean is_inherited;
            if (Log.log("LIST", 2, "")) {
                Log.log("LIST", 2, "" + VRL.safe(item));
            }
            boolean is_single_root = thisSession == null ? false : !thisSession.SG("root_dir").equals("/");
            boolean is_root_dir = thisSession == null ? false : item.getProperty("root_dir", "").equals(thisSession.SG("root_dir"));
            boolean is_invisible = item.getProperty("privs").toLowerCase().indexOf("(invisible)") >= 0;
            boolean bl = is_inherited = item.getProperty("privs").toLowerCase().indexOf("(inherited)") >= 0;
            if (fullPaths && (thisSession == null || !thisSession.check_access_privs(item.getProperty("root_dir"), "RETR", item))) {
                return false;
            }
            if ((item.getProperty("privs", "").indexOf("(view)") >= 0 && item.getProperty("privs", "").indexOf("*") <= 0 || item.getProperty("privs", "").indexOf("(view)") < 0 && !is_inherited || item.getProperty("privs", "").indexOf("(view)") >= 0 && !item.getProperty("name", "").equals("Icon\r")) && (!is_invisible || is_invisible && is_inherited && (!is_single_root || !is_root_dir))) {
                return true;
            }
        }
        return false;
    }

    public static void generateLineEntry(final Properties item, StringBuffer item_str, boolean makeupZipSize, final String the_dir, boolean fullPaths, final SessionCrush thisSession, boolean mlstFormat) {
        Date d;
        try {
            Vector inside_a_dir_list;
            if (item.getProperty("type", "").equals("DIR") && makeupZipSize && item.getProperty("protocol").equalsIgnoreCase("file")) {
                Vector v = new Vector();
                crushftp.handlers.Common.getAllFileListing(v, new VRL(item.getProperty("url")).getPath(), 20, false);
                long totalSize = 0L;
                int x = 0;
                while (x < v.size()) {
                    File_S f = (File_S)v.elementAt(x);
                    totalSize += f.length();
                    ++x;
                }
                item.put("size", String.valueOf(totalSize));
            } else if (item.getProperty("type", "").equals("DIR") && thisSession.BG("dir_calc")) {
                Log.log("SERVER", 1, "Calculating directory size..." + the_dir);
                inside_a_dir_list = new Vector();
                thisSession.uVFS.getListing(inside_a_dir_list, String.valueOf(the_dir) + item.getProperty("name") + "/");
                int xx = 0;
                while (xx < inside_a_dir_list.size()) {
                    Properties adder = (Properties)inside_a_dir_list.elementAt(xx);
                    item.put("size", "" + (Long.parseLong(item.getProperty("size", "")) + Long.parseLong(adder.getProperty("size"))));
                    ++xx;
                }
                item.put("num_items", "" + inside_a_dir_list.size());
            } else if (item.getProperty("type", "").equals("DIR") && thisSession.BG("dir_calc_count")) {
                Log.log("SERVER", 1, "Calculating directory count..." + the_dir);
                inside_a_dir_list = new Vector();
                final Properties status = new Properties();
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            thisSession.uVFS.getListing(inside_a_dir_list, String.valueOf(the_dir) + item.getProperty("name") + "/", 1, 1000, true);
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 1, e);
                        }
                        status.put("done", "true");
                    }
                });
                int count = 0;
                while (inside_a_dir_list.size() > 0 || !status.containsKey("done")) {
                    if (inside_a_dir_list.size() > 0) {
                        Properties p = (Properties)inside_a_dir_list.remove(0);
                        if (!LIST_handler.checkName(p, thisSession, fullPaths, false)) continue;
                        ++count;
                        continue;
                    }
                    Thread.sleep(100L);
                }
                item.put("num_items", "" + count);
            }
        }
        catch (Exception inside_a_dir_list) {
            // empty catch block
        }
        if (thisSession.BG("dos_ftp_listing")) {
            d = new Date(Long.parseLong(item.getProperty("modified", "0")));
            d = new Date(d.getTime() + (long)(thisSession.DG("timezone_offset") * 1000.0 * 60.0 * 60.0));
            SimpleDateFormat MM_dd_yy = new SimpleDateFormat("MM-dd-yy", Locale.US);
            SimpleDateFormat hh_mmaa = new SimpleDateFormat("hh:mmaa", Locale.US);
            item_str.append(MM_dd_yy.format(d));
            item_str.append("  ");
            item_str.append(hh_mmaa.format(d));
            if (item.getProperty("type", "").equals("DIR")) {
                item_str.append("       <DIR>          ");
            } else {
                item_str.append(("                     " + item.getProperty("size", "")).substring(20 - (20 - item.getProperty("size", "").length()))).append(" ");
            }
            item_str.append(String.valueOf(item.getProperty("name", "")) + "\r\n");
        } else if (!mlstFormat) {
            File_S f;
            VRL vrl;
            item_str.append(item.getProperty("permissions"));
            item_str.append(String.valueOf(crushftp.handlers.Common.lpad(item.getProperty("num_items", "1"), 4)) + " ");
            if (fullPaths && (thisSession == null || !thisSession.server_item.getProperty("serverType", "http").equalsIgnoreCase("SFTP")) && (vrl = new VRL(item.getProperty("url"))).getProtocol().equalsIgnoreCase("file") && (f = new File_S(vrl.getPath())).isFile() && f.length() < 0x3200000L) {
                item.put("owner", "MD5");
                try {
                    Properties p;
                    String md5str = "";
                    if (md5Hash.containsKey(f.getCanonicalPath()) && (p = (Properties)md5Hash.get(f.getCanonicalPath())).getProperty("modified").equals(String.valueOf(f.lastModified())) && p.getProperty("size").equals(String.valueOf(f.length()))) {
                        md5str = p.getProperty("md5");
                    }
                    if (md5str.equals("")) {
                        md5str = crushftp.handlers.Common.getMD5(new FileInputStream(f)).substring(24);
                        p = new Properties();
                        p.put("md5", md5str);
                        p.put("size", String.valueOf(f.length()));
                        p.put("modified", String.valueOf(f.lastModified()));
                        md5Hash.put(f.getCanonicalPath(), p);
                    }
                    item.put("group", md5str);
                    if (md5Hash.size() > 3000) {
                        md5Hash.clear();
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                    item.put("group", "0");
                }
            }
            item_str.append(String.valueOf(crushftp.handlers.Common.rpad(item.getProperty("owner", ""), 8)) + " ");
            item_str.append(String.valueOf(crushftp.handlers.Common.rpad(item.getProperty("group", ""), 8)) + " ");
            item_str.append(String.valueOf(crushftp.handlers.Common.lpad(item.getProperty("size", ""), 13)) + " ");
            if (fullPaths) {
                d = new Date(Long.parseLong(item.getProperty("modified", "0")));
                d = new Date(d.getTime() + (long)(thisSession.DG("timezone_offset") * 1000.0 * 60.0 * 60.0));
                item_str.append(String.valueOf(crushftp.handlers.Common.lpad(thisSession.sdf_yyyyMMddHHmmss.format(d), 15)) + " ");
                item_str.append(String.valueOf(crushftp.handlers.Common.lpad(item.getProperty("day", "").trim(), 2)) + " ");
                item_str.append(String.valueOf(crushftp.handlers.Common.lpad(item.getProperty("time_or_year", ""), 5)) + " ");
                String dir = item.getProperty("root_dir");
                if (dir.startsWith(thisSession.SG("root_dir"))) {
                    dir = dir.substring(thisSession.SG("root_dir").length() - 1);
                }
                item_str.append(String.valueOf(dir) + item.getProperty("name") + "\r\n");
            } else {
                item_str.append(String.valueOf(crushftp.handlers.Common.lpad(item.getProperty("month", "").trim(), 3)) + " ");
                item_str.append(String.valueOf(crushftp.handlers.Common.lpad(item.getProperty("day", "").trim(), 2)) + " ");
                item_str.append(String.valueOf(crushftp.handlers.Common.lpad(item.getProperty("time_or_year", ""), 5)) + " ");
                item_str.append(String.valueOf(item.getProperty("name", "")) + "\r\n");
            }
        } else {
            d = new Date(Long.parseLong(item.getProperty("modified", "0")));
            d = new Date((long)((double)d.getTime() + thisSession.DG("timezone_offset") * 1000.0 * 60.0 * 60.0));
            String privs = item.getProperty("privs", "");
            String newprivs = "";
            newprivs = String.valueOf(newprivs) + (privs.indexOf("(read)") >= 0 ? "r," : "");
            if (item.getProperty("type", "").equals("DIR")) {
                newprivs = String.valueOf(newprivs) + "e,l,";
                newprivs = String.valueOf(newprivs) + (privs.indexOf("(makedir)") >= 0 ? "m," : "");
                newprivs = String.valueOf(newprivs) + (privs.indexOf("(read)") >= 0 ? "c," : "");
                newprivs = String.valueOf(newprivs) + (privs.indexOf("(deletedir)") >= 0 ? "d," : "");
                newprivs = String.valueOf(newprivs) + (privs.indexOf("(rename)") >= 0 ? "f," : "");
                newprivs = String.valueOf(newprivs) + (privs.indexOf("(delete)") >= 0 ? "p," : "");
            } else {
                newprivs = String.valueOf(newprivs) + (privs.indexOf("(write)") >= 0 ? "w,a," : "");
                newprivs = String.valueOf(newprivs) + (privs.indexOf("(delete)") >= 0 ? "d," : "");
                newprivs = String.valueOf(newprivs) + (privs.indexOf("(rename)") >= 0 ? "f," : "");
            }
            if (newprivs.length() > 0) {
                newprivs = newprivs.substring(0, newprivs.length() - 1);
            }
            String[] mlst_format = thisSession.uiSG("mlst_format").split(";");
            int x = 0;
            while (x < mlst_format.length) {
                if (mlst_format[x].equalsIgnoreCase("Type*")) {
                    item_str.append("Type=").append(item.getProperty("type", "").toLowerCase()).append(";");
                } else if (mlst_format[x].equalsIgnoreCase("Modify*")) {
                    item_str.append("Modify=").append(thisSession.sdf_yyyyMMddHHmmssGMT.format(d)).append(";");
                } else if (mlst_format[x].equalsIgnoreCase("Perm*")) {
                    item_str.append("Perm=").append(newprivs).append(";");
                } else if (mlst_format[x].equalsIgnoreCase("Size*")) {
                    item_str.append("Size=").append(item.getProperty("size")).append(";");
                } else if (mlst_format[x].equalsIgnoreCase("UNIX.owner*")) {
                    item_str.append("UNIX.owner=").append(item.getProperty("owner").trim()).append(";");
                } else if (mlst_format[x].equalsIgnoreCase("UNIX.group*")) {
                    item_str.append("UNIX.group=").append(item.getProperty("group").trim()).append(";");
                }
                ++x;
            }
            String dir = the_dir;
            if (dir.startsWith(thisSession.SG("root_dir"))) {
                dir = dir.substring(thisSession.SG("root_dir").length() - 1);
            }
            item_str.append(" ").append(item.getProperty("name"));
            item_str.append("\r\n");
        }
    }

    public String SG(String data) {
        return this.thisSession.SG(data);
    }
}

