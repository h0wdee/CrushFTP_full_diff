/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.compress.archivers.zip.Zip64Mode
 */
package crushftp.server;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.GenericClient;
import com.crushftp.client.LineReader;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import com.crushftp.tunnel.FileArchiveEntry;
import com.crushftp.tunnel.FileArchiveOutputStream;
import crushftp.gui.LOC;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.TransferSpeedometer;
import crushftp.server.LIST_handler;
import crushftp.server.ServerStatus;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.net.ssl.SSLSocket;
import org.apache.commons.compress.archivers.zip.Zip64Mode;

public class RETR_handler
implements Runnable {
    public boolean die_now = false;
    public boolean pause_transfer = false;
    public float slow_transfer = 0.0f;
    public long startLoop = 0L;
    public long endLoop = 1000L;
    public Thread thisThread = null;
    public String the_dir;
    int packet_size = 32768;
    public OutputStream data_os = null;
    public GenericClient c = null;
    InputStream in = null;
    SessionCrush thisSession = null;
    static String CRLF = "\r\n";
    int user_down_count = 0;
    int new_user_down_count = 0;
    Properties item;
    boolean pasv_connect = false;
    String encode_on_fly = "";
    public Properties active2 = new Properties();
    public MessageDigest md5 = null;
    LIST_handler filetree_list = null;
    public long current_loc = 0L;
    public long max_loc = 0L;
    public ServerSocket s_sock = null;
    public Socket streamer = null;
    boolean resumed_file = false;
    public boolean zipping = false;
    public Vector activeZipThreads = new Vector();
    boolean zlibing = false;
    VRL otherFile = null;
    boolean runOnce = false;
    public boolean httpDownload = false;
    public Vector zipFiles = new Vector();
    public Vector zippedFiles = new Vector();
    private Vector zippedPaths = new Vector();
    SimpleDateFormat proxySDF = new SimpleDateFormat("MMddyyHHmmss");
    RandomAccessFile proxy = null;
    InputStream proxy_remote_in = null;
    Socket data_sock = null;
    public String stop_message = "";
    public boolean inError = false;
    String threadName = "";

    public RETR_handler() {
        this.active2.put("streamOpenStatus", "PENDING");
    }

    public void init_vars(String the_dir, long current_loc, long max_loc, SessionCrush thisSession, Properties item, boolean pasv_connect, String encode_on_fly, VRL otherFile, Socket data_sock) {
        this.data_sock = data_sock;
        this.the_dir = the_dir;
        this.thisSession = thisSession;
        this.current_loc = current_loc;
        this.max_loc = max_loc;
        this.item = item;
        this.pasv_connect = pasv_connect;
        this.encode_on_fly = encode_on_fly;
        this.otherFile = otherFile;
        try {
            if (this.md5 == null) {
                this.md5 = MessageDigest.getInstance(ServerStatus.SG("hash_algorithm"));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.md5.reset();
        thisSession.uiPUT("md5", "");
        thisSession.uiPUT("sfv", "");
        if (current_loc > 0L) {
            this.resumed_file = true;
        }
        try {
            this.user_down_count = ServerStatus.count_users_down();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.threadName = String.valueOf(Thread.currentThread().getName()) + ":" + thisSession.uiSG("user_name") + ":RETR";
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public void run() {
        this.thisThread = Thread.currentThread();
        this.thisThread.setName(this.threadName);
        Properties pre_event_info = null;
        try {
            block266: {
                this.stop_message = "";
                this.inError = false;
                String the_file_name = "";
                String the_file_path = "";
                if (this.thisSession != null) {
                    Properties current_download_item = null;
                    this.thisSession.uiPUT("sending_file", "true");
                    this.active2.put("active", "true");
                    this.active2.put("streamOpenStatus", "PENDING");
                    this.proxy_remote_in = null;
                    RETR_handler.updateTransferStats(this.thisSession, -1, false, null, this.md5, current_download_item);
                    the_file_path = this.the_dir;
                    the_file_name = the_file_path.substring(the_file_path.lastIndexOf("/") + 1, the_file_path.length()).trim();
                    the_file_path = the_file_path.substring(0, the_file_path.lastIndexOf("/") + 1);
                    boolean free_ratio_item = false;
                    if (this.item.getProperty("privs", "").indexOf("(ratio)") >= 0) {
                        free_ratio_item = true;
                    }
                    Properties stat = null;
                    VRL vrl = new VRL(this.item.getProperty("url"));
                    if (!vrl.getProtocol().equalsIgnoreCase("virtual")) {
                        this.c = this.thisSession.uVFS.getClient(this.item);
                    }
                    if (this.c != null || !this.zipping) {
                        stat = this.c.stat(vrl.getPath());
                    }
                    if (!vrl.getProtocol().equalsIgnoreCase("file") && ServerStatus.BG("proxyKeepDownloads")) {
                        new File_S(String.valueOf(ServerStatus.SG("proxyDownloadRepository")) + this.thisSession.uiSG("user_name") + the_file_path).mkdirs();
                        this.proxy = new RandomAccessFile(new File_S(String.valueOf(ServerStatus.SG("proxyDownloadRepository")) + this.thisSession.uiSG("user_name") + the_file_path + this.proxySDF.format(new Date()) + "_" + the_file_name), "rw");
                        if (this.proxy.length() > this.current_loc) {
                            this.proxy.setLength(this.current_loc);
                        }
                    }
                    the_file_name = crushftp.handlers.Common.url_decode(the_file_name);
                    this.thisSession.uiPUT("last_file_real_path", this.item.getProperty("url", ""));
                    this.thisSession.uiPUT("last_file_name", crushftp.handlers.Common.last(this.item.getProperty("url", "")));
                    long start_transfer_time = new Date().getTime();
                    long max_download_amount = this.thisSession.LG("max_download_amount");
                    long max_download_amount_day = this.thisSession.LG("max_download_amount_day");
                    long max_download_amount_month = this.thisSession.LG("max_download_amount_month");
                    long max_download_count = this.thisSession.LG("max_download_count");
                    long max_download_count_day = this.thisSession.LG("max_download_count_day");
                    long max_download_count_month = this.thisSession.LG("max_download_count_month");
                    long start_download_amount_day = 0L;
                    long start_download_amount_month = 0L;
                    long start_download_count_day = 0L;
                    long start_download_count_month = 0L;
                    if (max_download_amount_day > 0L) {
                        start_download_amount_day = ServerStatus.thisObj.statTools.getTransferAmountToday(this.thisSession.uiSG("user_ip"), this.thisSession.uiSG("user_name"), this.thisSession.uiPG("stat"), "downloads", this.thisSession);
                    }
                    if (max_download_amount_month > 0L) {
                        start_download_amount_month = ServerStatus.thisObj.statTools.getTransferAmountThisMonth(this.thisSession.uiSG("user_ip"), this.thisSession.uiSG("user_name"), this.thisSession.uiPG("stat"), "downloads", this.thisSession);
                    }
                    if (max_download_count_day > 0L) {
                        start_download_count_day = ServerStatus.thisObj.statTools.getTransferCountToday(this.thisSession.uiSG("user_ip"), this.thisSession.uiSG("user_name"), this.thisSession.uiPG("stat"), "downloads", this.thisSession);
                    }
                    if (max_download_count_month > 0L) {
                        start_download_count_month = ServerStatus.thisObj.statTools.getTransferCountThisMonth(this.thisSession.uiSG("user_ip"), this.thisSession.uiSG("user_name"), this.thisSession.uiPG("stat"), "downloads", this.thisSession);
                    }
                    if (ServerStatus.count_users_ip(this.thisSession, this.thisSession.uiSG("user_protocol")) > crushftp.handlers.Common.check_protocol(this.thisSession.uiSG("user_protocol"), this.SG("allowed_protocols"))) {
                        if (this.data_sock != null) {
                            this.data_sock.close();
                        }
                        this.thisSession.do_kill();
                    } else if (this.thisSession.IG("ratio") > 0 && !free_ratio_item && (this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) * (long)this.thisSession.IG("ratio") <= this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent")) {
                        this.stop_message = "550-" + LOC.G("WARNING!!! Ratio reached.") + "  " + LOC.G("Ratio is") + " " + this.thisSession.IG("ratio") + " to 1. " + LOC.G("Received") + ":" + Common.format_bytes_short(this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) + " " + LOC.G("Sent") + ":" + Common.format_bytes_short(this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent")) + "  " + LOC.G("Available") + ":" + Common.format_bytes_short((this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) * this.thisSession.LG("ratio") - (this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent")));
                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged(this.stop_message, "RETR");
                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged("550", "%RETR-ratio exceeded%", "RETR");
                        this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                        this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                    } else if (max_download_amount > 0L && this.thisSession.uiLG("bytes_sent") > max_download_amount * 1024L) {
                        this.stop_message = "550-" + LOC.G("WARNING!!! Maximum download amount reached.") + "  " + LOC.G("Sent") + ":" + this.thisSession.uiLG("bytes_sent") / 1024L + "k.  " + LOC.G("Max") + ":" + max_download_amount + "k.  " + LOC.G("Available") + ":" + (max_download_amount - this.thisSession.uiLG("bytes_sent") / 1024L) + "k.";
                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged(this.stop_message, "RETR");
                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged("550", "%RETR-max reached%", "RETR");
                        this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                        this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                        ServerStatus.thisObj.runAlerts("user_download_session", this.thisSession);
                    } else if (max_download_count > 0L && this.thisSession.uiLG("session_download_count") > max_download_count) {
                        this.stop_message = "550-" + LOC.G("WARNING!!! Maximum download count reached.") + "  " + LOC.G("Sent") + ":" + this.thisSession.uiLG("session_download_count") + ".  " + LOC.G("Max") + ":" + max_download_count + ".  " + LOC.G("Available") + ":" + (max_download_count - this.thisSession.uiLG("session_download_count")) + ".";
                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged(this.stop_message, "RETR");
                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged("550", "%RETR-max reached%", "RETR");
                        this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                        this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                        ServerStatus.thisObj.runAlerts("user_download_session", this.thisSession);
                    } else if (max_download_amount_day > 0L && start_download_amount_day > max_download_amount_day * 1024L || max_download_amount_month > 0L && start_download_amount_month > max_download_amount_month * 1024L) {
                        if (max_download_amount_day > 0L && start_download_amount_day > max_download_amount_day * 1024L) {
                            this.stop_message = "550-" + LOC.G("WARNING!!! Maximum download amount today reached.") + "  " + LOC.G("Sent") + ":" + start_download_amount_day / 1024L + "k.  " + LOC.G("Max") + ":" + max_download_amount_day + "k.  ";
                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged(this.stop_message, "RETR");
                            ServerStatus.thisObj.runAlerts("user_download_day", this.thisSession);
                        }
                        if (max_download_amount_month > 0L && start_download_amount_month > max_download_amount_month * 1024L) {
                            this.stop_message = "550-" + LOC.G("WARNING!!! Maximum download amount last 30 days reached.") + "  " + LOC.G("Sent") + ":" + start_download_amount_month / 1024L + "k.  " + LOC.G("Max") + ":" + max_download_amount_month + "k.  ";
                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged(this.stop_message, "RETR");
                            ServerStatus.thisObj.runAlerts("user_download_month", this.thisSession);
                        }
                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged("550", "%RETR-max reached%", "RETR");
                        this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                        this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                    } else if (max_download_count_day > 0L && start_download_count_day > max_download_count_day || max_download_count_month > 0L && start_download_count_month > max_download_count_month) {
                        if (max_download_count_day > 0L && start_download_count_day > max_download_count_day) {
                            this.stop_message = "550-" + LOC.G("WARNING!!! Maximum download count today reached.") + "  " + LOC.G("Sent") + ":" + start_download_count_day + ".  " + LOC.G("Max") + ":" + max_download_count_day + "k.  ";
                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged(this.stop_message, "RETR");
                            ServerStatus.thisObj.runAlerts("user_download_day", this.thisSession);
                        }
                        if (max_download_count_month > 0L && start_download_count_month > max_download_count_month) {
                            this.stop_message = "550-" + LOC.G("WARNING!!! Maximum download count last 30 days reached.") + "  " + LOC.G("Sent") + ":" + start_download_count_month + ".  " + LOC.G("Max") + ":" + max_download_count_month + ".  ";
                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged(this.stop_message, "RETR");
                            ServerStatus.thisObj.runAlerts("user_download_month", this.thisSession);
                        }
                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged("550", "%RETR-max reached%", "RETR");
                        this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                        this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                    } else {
                        if (stat != null) {
                            this.thisSession.uiPUT("file_length", stat.getProperty("size"));
                        }
                        if (this.thisSession.BG("partial_download") && this.thisSession.IG("ratio") > 0 && !free_ratio_item && this.thisSession.uiLG("file_length") - this.current_loc > (this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) * (long)this.thisSession.IG("ratio") - (this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent"))) {
                            String response_str = "%RETR-ratio will be exceeded%" + CRLF + LOC.G("Ratio is") + " " + this.thisSession.IG("ratio") + " to 1. " + LOC.G("Received") + ":" + Common.format_bytes_short(this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) + " " + LOC.G("Sent") + ":" + Common.format_bytes_short(this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent")) + "  " + LOC.G("Available") + ":" + Common.format_bytes_short((this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) * this.thisSession.LG("ratio") - (this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent"))) + CRLF;
                            this.stop_message = response_str = String.valueOf(response_str) + "%RETR-ratio will be exceeded abort%";
                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged("550", response_str, "RETR");
                            this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                            this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                        } else if (this.thisSession.BG("partial_download") && this.thisSession.LG("max_download_amount") > 0L && this.thisSession.uiLG("file_length") - this.current_loc > this.thisSession.LG("max_download_amount") * 1024L - this.thisSession.uiLG("bytes_sent")) {
                            String response_str = "%RETR-max download will be exceeded%" + CRLF + LOC.G("Sent") + ":" + this.thisSession.uiLG("bytes_sent") / 1024L + "k.  " + LOC.G("Max") + ":" + this.thisSession.LG("max_download_amount") + "k.  " + LOC.G("Available") + ":" + (this.thisSession.LG("max_download_amount") - this.thisSession.uiLG("bytes_sent") / 1024L) + "k.  " + LOC.G("Attempting to download") + ":" + (this.thisSession.uiLG("file_length") - this.current_loc) / 1024L + "k." + CRLF;
                            this.stop_message = response_str = String.valueOf(response_str) + "%RETR-max download will be exceeded abort%";
                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged("550", response_str, "RETR");
                            this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                            this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                        } else {
                            long resume_loc = this.current_loc;
                            int loop_times = 0;
                            while (this.data_sock == null && this.otherFile == null && this.thisSession.data_socks.size() == 0 && loop_times++ < 10000) {
                                Thread.sleep(1L);
                            }
                            if (this.data_sock == null && this.otherFile == null && this.thisSession.data_socks.size() == 0) {
                                this.thisSession.not_done = this.thisSession.ftp_write_command_logged("550", "%PORT-fail_question%" + CRLF + "%PORT-no_data_connection%", "RETR");
                                this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                                this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                            } else {
                                try {
                                    boolean binary_mode;
                                    boolean has_bytes = false;
                                    if (this.thisSession.data_socks.size() > 0 && this.data_sock == null) {
                                        this.data_sock = (Socket)this.thisSession.data_socks.remove(0);
                                    }
                                    if (this.data_sock != null) {
                                        this.data_sock.setSoTimeout((this.thisSession.IG("max_idle_time") <= 0 ? 5 : this.thisSession.IG("max_idle_time")) * 1000 * 60);
                                    }
                                    if (this.otherFile == null) {
                                        this.data_os = new BufferedOutputStream(this.data_sock.getOutputStream());
                                    }
                                    this.active2.put("streamOpenStatus", "OPEN");
                                    Properties pp = new Properties();
                                    String message_string = "";
                                    String responseNumber = "150";
                                    pp.put("message_string", message_string);
                                    pp.put("responseNumber", responseNumber);
                                    this.thisSession.runPlugin("before_download", pp);
                                    message_string = pp.getProperty("message_string", message_string);
                                    responseNumber = pp.getProperty("responseNumber", responseNumber);
                                    if (!responseNumber.equals("150")) {
                                        Thread.sleep(100L);
                                        throw new Exception(message_string);
                                    }
                                    if (!message_string.equals("")) {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-", "RETR");
                                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-" + message_string, "RETR");
                                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-", "RETR");
                                    }
                                    if (this.thisSession.LG("max_download_amount") != 0L) {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-" + LOC.G("Max Download") + ".  " + LOC.G("Sent") + ":" + this.thisSession.uiLG("bytes_sent") / 1024L + "k.  " + LOC.G("Max") + ":" + this.thisSession.LG("max_download_amount") + "k.  " + LOC.G("Available") + ":" + (this.thisSession.LG("max_download_amount") - this.thisSession.uiLG("bytes_sent") / 1024L) + "k.", "RETR");
                                    }
                                    if (this.thisSession.LG("max_download_amount_month") != 0L) {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-" + LOC.G("Max Download Month") + ".  " + LOC.G("Sent") + ":" + start_download_amount_month / 1024L + "k.  " + LOC.G("Max") + ":" + this.thisSession.LG("max_download_amount_month") + "k.  " + LOC.G("Available") + ":" + (this.thisSession.LG("max_download_amount_month") - start_download_amount_month / 1024L) + "k.", "RETR");
                                    }
                                    if (this.thisSession.LG("max_download_amount_day") != 0L) {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-" + LOC.G("Max Download Day") + ".  " + LOC.G("Sent") + ":" + start_download_amount_day / 1024L + "k.  " + LOC.G("Max") + ":" + this.thisSession.LG("max_download_amount_day") + "k.  " + LOC.G("Available") + ":" + (this.thisSession.LG("max_download_amount_day") - start_download_amount_day / 1024L) + "k.", "RETR");
                                    }
                                    if (this.thisSession.LG("max_upload_amount") != 0L) {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-" + LOC.G("Max Upload") + ".  " + LOC.G("Sent") + ":" + this.thisSession.uiLG("bytes_sent") / 1024L + "k.  " + LOC.G("Max") + ":" + this.thisSession.LG("max_upload_amount") + "k.  " + LOC.G("Available") + ":" + (this.thisSession.LG("max_upload_amount") - this.thisSession.uiLG("bytes_sent") / 1024L) + "k.", "RETR");
                                    }
                                    if (this.thisSession.IG("max_download_count") != 0) {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-" + LOC.G("Max Download") + ".  " + LOC.G("Sent") + ":" + this.thisSession.uiLG("session_download_count") + ".  " + LOC.G("Max") + ":" + this.thisSession.IG("max_download_count") + ".  " + LOC.G("Available") + ":" + ((long)this.thisSession.IG("max_download_count") - this.thisSession.uiLG("session_download_count")) + ".", "RETR");
                                    }
                                    if (this.thisSession.IG("max_upload_count") != 0) {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-" + LOC.G("Max Upload") + ".  " + LOC.G("Sent") + ":" + this.thisSession.uiLG("session_upload_count") + ".  " + LOC.G("Max") + ":" + this.thisSession.IG("max_upload_count") + ".  " + LOC.G("Available") + ":" + ((long)this.thisSession.IG("max_upload_count") - this.thisSession.uiLG("session_upload_count")) + ".", "RETR");
                                    }
                                    if (this.thisSession.IG("ratio") != 0) {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-" + LOC.G("Ratio is") + " " + this.thisSession.IG("ratio") + " to 1. " + LOC.G("Received") + ":" + Common.format_bytes_short(this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) + " " + LOC.G("Sent") + ":" + Common.format_bytes_short(this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent")) + "  " + LOC.G("Available") + ":" + Common.format_bytes_short((this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) * this.thisSession.LG("ratio") - (this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent"))), "RETR");
                                    }
                                    if (this.c != null && this.c.getConfig("pgpDecryptDownload", "").equals("true")) {
                                        this.thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-pgpDecryptDownload:" + new VRL(this.c.getConfig("pgpPrivateKeyDownloadPath", "")).safe(), "RETR");
                                    }
                                    if (this.c != null && this.c.getConfig("pgpEncryptDownload", "").equals("true")) {
                                        this.thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-pgpEncryptDownload:" + new VRL(this.c.getConfig("pgpPublicKeyDownloadPath", "")).safe(), "RETR");
                                    }
                                    if (this.c != null && this.c.getConfig("pgpDecryptUpload", "").equals("true")) {
                                        this.thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-pgpDecryptUpload:" + new VRL(this.c.getConfig("pgpPrivateKeyUploadPath", "")).safe(), "RETR");
                                    }
                                    if (this.c != null && this.c.getConfig("pgpEncryptUpload", "").equals("true")) {
                                        this.thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-pgpEncryptUpload:" + new VRL(this.c.getConfig("pgpPublicKeyUploadPath", "")).safe(), "RETR");
                                    }
                                    this.thisSession.uiPUT("start_transfer_time", String.valueOf(new Date().getTime()));
                                    this.thisSession.uiPUT("start_transfer_byte_amount", String.valueOf(this.thisSession.uiLG("bytes_sent")));
                                    TransferSpeedometer speedController = new TransferSpeedometer(this.thisSession, this, null);
                                    Worker.startWorker(speedController, String.valueOf(this.thisSession.uiSG("user_name")) + ":(" + this.thisSession.uiSG("user_number") + ")-" + this.thisSession.uiSG("user_ip") + " (speedometer retr)");
                                    VRL Zin = null;
                                    byte[] temp_array = new byte[this.packet_size];
                                    long start_file_size = this.thisSession.uiLG("file_length");
                                    long file_changing_loc = 0L;
                                    int file_changing_loop_intervals = 0;
                                    boolean bl = binary_mode = !this.thisSession.uiSG("file_transfer_mode").equals("ASCII");
                                    if (this.thisSession.uiBG("modez")) {
                                        Deflater def = new Deflater();
                                        def.setLevel(Integer.parseInt(this.thisSession.uiSG("zlibLevel")));
                                        this.data_os = this.httpDownload ? new DeflaterOutputStream(this.data_os, def) : new DeflaterOutputStream(this.data_os, def);
                                        this.zlibing = true;
                                    }
                                    if (this.zipFiles.size() > 0 || this.zipping) {
                                        this.data_os = new FileArchiveOutputStream(new BufferedOutputStream(this.data_os), !this.thisSession.uiBG("no_zip_compression"));
                                        ((FileArchiveOutputStream)((Object)this.data_os)).setEncoding(this.thisSession.SG("char_encoding"));
                                        if (ServerStatus.BG("zip64")) {
                                            ((FileArchiveOutputStream)((Object)this.data_os)).setUseZip64(Zip64Mode.AsNeeded);
                                        }
                                        if (ServerStatus.BG("zip64_always")) {
                                            ((FileArchiveOutputStream)((Object)this.data_os)).setUseZip64(Zip64Mode.Always);
                                        }
                                        if (this.thisSession.uiBG("zip64")) {
                                            ((FileArchiveOutputStream)((Object)this.data_os)).setUseZip64(Zip64Mode.Always);
                                        }
                                        if (ServerStatus.SG("zipCompressionLevel").equalsIgnoreCase("None")) {
                                            ((FileArchiveOutputStream)((Object)this.data_os)).setLevel(0);
                                        } else if (ServerStatus.SG("zipCompressionLevel").equalsIgnoreCase("Fast")) {
                                            ((FileArchiveOutputStream)((Object)this.data_os)).setLevel(1);
                                        } else {
                                            ((FileArchiveOutputStream)((Object)this.data_os)).setLevel(9);
                                        }
                                        if (this.thisSession.uiBG("no_zip_compression")) {
                                            ((FileArchiveOutputStream)((Object)this.data_os)).setLevel(0);
                                        }
                                        this.zipping = true;
                                    } else {
                                        if (!this.thisSession.uiSG("proxy_mode").equalsIgnoreCase("socket")) {
                                            if (the_file_name.startsWith(":filetree") && ServerStatus.BG("allow_filetree")) {
                                                int scanDepth = 999;
                                                if (!the_file_name.equals(":filetree")) {
                                                    try {
                                                        scanDepth = Integer.parseInt(the_file_name.substring(":filetree".length()));
                                                    }
                                                    catch (Exception exception) {
                                                        // empty catch block
                                                    }
                                                }
                                                this.thisSession.add_log("Building file tree listing..." + scanDepth, "RETR");
                                                if (this.filetree_list == null) {
                                                    this.filetree_list = new LIST_handler();
                                                }
                                                this.filetree_list.init_vars(the_file_path, false, this.thisSession, "", true, false);
                                                if (scanDepth < 0) {
                                                    scanDepth *= -1;
                                                    this.thisSession.uiPUT("modez", "true");
                                                }
                                                this.filetree_list.scanDepth = scanDepth;
                                                this.filetree_list.fullPaths = true;
                                                this.filetree_list.justStreamListData = true;
                                                Properties connectedSockets = crushftp.handlers.Common.getConnectedSockets();
                                                Socket sock1 = (Socket)connectedSockets.get("sock1");
                                                Socket sock2 = (Socket)connectedSockets.get("sock2");
                                                this.filetree_list.data_osw = sock1.getOutputStream();
                                                this.thisSession.data_socks.addElement(sock1);
                                                this.in = sock2.getInputStream();
                                                Worker.startWorker(this.filetree_list, String.valueOf(this.thisSession.uiSG("user_name")) + ":(" + this.thisSession.uiSG("user_number") + ")-" + this.thisSession.uiSG("user_ip") + " RETR_handler_filetree");
                                            } else {
                                                pre_event_info = this.thisSession.do_event5("PRE_DOWNLOAD", this.item);
                                                if (pre_event_info != null) {
                                                    long start = System.currentTimeMillis();
                                                    while (pre_event_info != null && pre_event_info.getProperty("event_status", "").equals("running")) {
                                                        Thread.sleep(100L);
                                                        if (pre_event_info.getProperty("info_download_event_status", "").equals("ready")) break;
                                                        if (System.currentTimeMillis() - start <= 600000L) continue;
                                                        throw new Exception("PRE_DOWNLOAD event times out while waiting to start...");
                                                    }
                                                    Properties item2 = this.item;
                                                    if (pre_event_info.get("newItems") != null && ((Vector)pre_event_info.get("newItems")).size() > 0) {
                                                        Vector v = (Vector)pre_event_info.get("newItems");
                                                        item2 = (Properties)v.elementAt(v.size() - 1);
                                                        VRL vrl2 = new VRL(item2.getProperty("url"));
                                                        if (!(String.valueOf(vrl.getProtocol().toUpperCase()) + "://" + vrl.getUsername() + ":" + vrl.getPassword() + "@" + vrl.getHost()).equals(String.valueOf(vrl2.getProtocol().toUpperCase()) + "://" + vrl2.getUsername() + ":" + vrl2.getPassword() + "@" + vrl2.getHost())) {
                                                            this.c.close();
                                                            this.thisSession.uVFS.releaseClient(this.c);
                                                            this.item = item2;
                                                            if (!vrl.getProtocol().equalsIgnoreCase("virtual")) {
                                                                this.c = this.thisSession.uVFS.getClient(this.item);
                                                            }
                                                            if (this.c != null) {
                                                                stat = this.c.stat(vrl.getPath());
                                                            }
                                                            if (stat != null) {
                                                                this.thisSession.uiPUT("file_length", stat.getProperty("size"));
                                                            }
                                                            vrl = new VRL(this.item.getProperty("url"));
                                                        } else {
                                                            this.item = item2;
                                                            vrl = new VRL(this.item.getProperty("url"));
                                                            if (this.c != null) {
                                                                stat = this.c.stat(vrl.getPath());
                                                            }
                                                            if (stat != null) {
                                                                this.thisSession.uiPUT("file_length", stat.getProperty("size"));
                                                            }
                                                        }
                                                    }
                                                }
                                                this.in = this.c.download(vrl.getPath(), this.current_loc, -1L, binary_mode);
                                                if (current_download_item != null) {
                                                    ServerStatus.siVG("outgoing_transfers").remove(current_download_item);
                                                }
                                                if (this.item != null) {
                                                    current_download_item = this.make_current_item(start_transfer_time, this.item);
                                                }
                                            }
                                        }
                                        if (!Common.dmz_mode) {
                                            if (!this.thisSession.user.getProperty("fileEncryptionKey", "").equals("") || !this.thisSession.user.getProperty("fileDecryptionKey", "").equals("")) {
                                                this.in = crushftp.handlers.Common.getDecryptedStream(this.in, this.thisSession.user.getProperty("fileEncryptionKey", ""), this.thisSession.user.getProperty("fileDecryptionKey", ""), this.thisSession.user.getProperty("fileDecryptionKeyPass", ""));
                                            } else if (!ServerStatus.SG("fileEncryptionKey").equals("") || ServerStatus.BG("fileDecryption")) {
                                                this.in = crushftp.handlers.Common.getDecryptedStream(this.in, ServerStatus.SG("fileEncryptionKey"), ServerStatus.SG("fileDecryptionKey"), ServerStatus.SG("fileDecryptionKeyPass"));
                                            }
                                        }
                                    }
                                    this.thisSession.not_done = this.thisSession.ftp_write_command_logged(responseNumber, "Opening %user_file_transfer_mode% mode data connection for " + this.thisSession.stripRoot(the_file_path) + the_file_name + " (%user_file_length% bytes). R E T R", "RETR");
                                    byte[] read_string = null;
                                    int data_read = 0;
                                    int ratio = this.thisSession.IG("ratio");
                                    int maxPackSize = 1000000;
                                    if (crushftp.handlers.Common.machine_is_mac()) {
                                        maxPackSize = 200000;
                                    }
                                    this.startLoop = 0L;
                                    this.endLoop = 1000L;
                                    int lesserSpeed = this.reloadBandwidthLimits();
                                    long speedometerCheckInterval = new Date().getTime();
                                    if (this.thisSession.uiSG("proxy_mode").equalsIgnoreCase("socket") && this.SG("site").toUpperCase().indexOf("(SITE_PROXY)") >= 0) {
                                        final Socket sock = new Socket(this.thisSession.uiSG("proxy_ip_address"), this.thisSession.uiIG("proxy_remote_port"));
                                        this.proxy_remote_in = sock.getInputStream();
                                        final BufferedOutputStream proxy_remote_out = new BufferedOutputStream(sock.getOutputStream());
                                        final InputStream proxy_in = this.data_sock.getInputStream();
                                        Worker.startWorker(new Runnable(){

                                            @Override
                                            public void run() {
                                                byte[] b = new byte[32768];
                                                int bytesRead = 0;
                                                try {
                                                    while (RETR_handler.this.active2.getProperty("active", "").equals("true") && bytesRead >= 0) {
                                                        bytesRead = proxy_in.read(b);
                                                        Log.log("DOWNLOAD", 2, "proxy bytes:" + bytesRead);
                                                        if (bytesRead > 0) {
                                                            proxy_remote_out.write(b, 0, bytesRead);
                                                        }
                                                        proxy_remote_out.flush();
                                                    }
                                                }
                                                catch (Exception e) {
                                                    Log.log("DOWNLOAD", 1, e);
                                                }
                                                try {
                                                    sock.close();
                                                    RETR_handler.this.data_sock.close();
                                                }
                                                catch (Exception e) {
                                                    Log.log("DOWNLOAD", 1, e);
                                                }
                                                if (RETR_handler.this.data_sock != null) {
                                                    RETR_handler.this.thisSession.old_data_socks.remove(RETR_handler.this.data_sock);
                                                }
                                            }
                                        }, String.valueOf(this.thisSession.uiSG("user_name")) + ":(" + this.thisSession.uiSG("user_number") + ")-" + this.thisSession.uiSG("user_ip") + " RETR_handler_proxy_connector");
                                    }
                                    if (lesserSpeed != 0) {
                                        this.thisSession.add_log("Bandwidth is being limited:" + lesserSpeed, "RETR");
                                    }
                                    boolean pgp = RETR_handler.checkPgp(this.thisSession, this.item);
                                    LineReader inASCII = null;
                                    boolean user_speed_notified = false;
                                    if (pre_event_info != null) {
                                        pre_event_info.put("info_download_event_status", "started");
                                    }
                                    while (data_read >= 0) {
                                        int newSize;
                                        Date new_date = new Date();
                                        if (new_date.getTime() - speedometerCheckInterval > 10000L) {
                                            lesserSpeed = this.reloadBandwidthLimits();
                                            speedometerCheckInterval = new_date.getTime();
                                        }
                                        if (lesserSpeed > 0 && !speedController.bandwidth_immune_server) {
                                            this.slow_transfer = speedController.getDelayAmount(data_read, this.startLoop, this.endLoop, temp_array.length, this.slow_transfer, lesserSpeed);
                                            speedController.reloadBandwidthLimits();
                                        }
                                        while (this.pause_transfer) {
                                            Thread.sleep(100L);
                                        }
                                        this.startLoop = System.currentTimeMillis();
                                        if (this.slow_transfer > 0.0f) {
                                            Thread.sleep((int)this.slow_transfer);
                                        }
                                        float packPercent = 0.0f;
                                        packPercent = (float)this.thisSession.uiLG("overall_transfer_speed") * 1024.0f / 10.0f / (float)temp_array.length;
                                        if (((double)packPercent > 1.5 || (double)packPercent < 0.5) && packPercent > 0.0f && (newSize = (int)this.thisSession.uiLG("overall_transfer_speed") * 1024 / 10) > 1000 && newSize < maxPackSize) {
                                            temp_array = new byte[newSize];
                                        }
                                        if (this.max_loc > 0L && this.max_loc - this.current_loc < (long)temp_array.length && (int)(this.max_loc - this.current_loc) >= 0) {
                                            temp_array = new byte[(int)(this.max_loc - this.current_loc)];
                                        }
                                        if (max_download_amount > 0L && this.thisSession.uiLG("bytes_sent") > max_download_amount * 1024L) {
                                            data_read = -1;
                                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged("226-%RETR-max reached%", "RETR");
                                            ServerStatus.thisObj.runAlerts("user_download_session", this.thisSession);
                                        } else if (max_download_count > 0L && this.thisSession.uiLG("session_download_count") > max_download_count) {
                                            data_read = -1;
                                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged("226-%RETR-max reached%", "RETR");
                                            ServerStatus.thisObj.runAlerts("user_download_session", this.thisSession);
                                        } else if (max_download_amount_day > 0L && this.thisSession.uiLG("bytes_sent") + start_download_amount_day > max_download_amount_day * 1024L) {
                                            data_read = -1;
                                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged("226-" + LOC.G("WARNING!!! Maximum download amount today reached.") + "  " + LOC.G("Sent") + ":" + (this.thisSession.uiLG("bytes_sent") + start_download_amount_day) / 1024L + "k.  " + LOC.G("Max") + ":" + max_download_amount_day + "k.  ", "RETR");
                                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged("226-%RETR-max reached%", "RETR");
                                            ServerStatus.thisObj.runAlerts("user_download_day", this.thisSession);
                                        } else if (max_download_count_day > 0L && this.thisSession.uiLG("session_download_count") + start_download_count_day > max_download_count_day) {
                                            data_read = -1;
                                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged("226-" + LOC.G("WARNING!!! Maximum download count today reached.") + "  " + LOC.G("Sent") + ":" + (this.thisSession.uiLG("session_download_count") + start_download_count_day) + ".  " + LOC.G("Max") + ":" + max_download_count_day + ".  ", "RETR");
                                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged("226-%RETR-max reached%", "RETR");
                                            ServerStatus.thisObj.runAlerts("user_download_day", this.thisSession);
                                        } else if (max_download_amount_month > 0L && this.thisSession.uiLG("bytes_sent") + start_download_amount_month > max_download_amount_month * 1024L) {
                                            data_read = -1;
                                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged("226-" + LOC.G("WARNING!!! Maximum download amount last 30 days reached.") + "  " + LOC.G("Sent") + ":" + (this.thisSession.uiLG("bytes_sent") + start_download_amount_month) / 1024L + "k.  " + LOC.G("Max") + ":" + max_download_amount_month + "k.  ", "RETR");
                                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged("226-%RETR-max reached%", "RETR");
                                            ServerStatus.thisObj.runAlerts("user_download_month", this.thisSession);
                                        } else if (max_download_count_month > 0L && this.thisSession.uiLG("session_download_count") + start_download_count_month > max_download_count_month) {
                                            data_read = -1;
                                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged("226-" + LOC.G("WARNING!!! Maximum download count last 30 days reached.") + "  " + LOC.G("Sent") + ":" + (this.thisSession.uiLG("session_download_count") + start_download_count_month) + ".  " + LOC.G("Max") + ":" + max_download_count_month + ".  ", "RETR");
                                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged("226-%RETR-max reached%", "RETR");
                                            ServerStatus.thisObj.runAlerts("user_download_month", this.thisSession);
                                        } else if (ratio == 0 || free_ratio_item || (this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) * (long)ratio > this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent")) {
                                            long cur_file_size;
                                            if (this.zipping) {
                                                if (Zin == null) {
                                                    if (this.zipFiles.size() > 0) {
                                                        int xx = 0;
                                                        Properties zipItem = (Properties)this.zipFiles.elementAt(xx);
                                                        if (zipItem.getProperty("type", "").equalsIgnoreCase("DIR") && zipItem.getProperty("privs").toUpperCase().indexOf("(VIEW)") < 0) {
                                                            this.zipFiles.removeElementAt(xx);
                                                        } else if (!(crushftp.handlers.Common.filter_check("L", zipItem.getProperty("name"), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter")) && crushftp.handlers.Common.filter_check("F", String.valueOf(zipItem.getProperty("name")) + (zipItem.getProperty("type").equalsIgnoreCase("DIR") && !zipItem.getProperty("name").endsWith("/") ? "/" : ""), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter")) && crushftp.handlers.Common.filter_check("DIR", String.valueOf(zipItem.getProperty("name")) + (zipItem.getProperty("type").equalsIgnoreCase("DIR") && !zipItem.getProperty("name").endsWith("/") ? "/" : ""), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter")))) {
                                                            this.zipFiles.removeElementAt(xx);
                                                        } else if (!this.thisSession.check_access_privs(zipItem.getProperty("root_dir"), "RETR", zipItem) || !this.thisSession.check_access_privs(String.valueOf(zipItem.getProperty("root_dir")) + zipItem.getProperty("name"), "RETR", zipItem)) {
                                                            this.zipFiles.removeElementAt(xx);
                                                        }
                                                    }
                                                    if (this.zipFiles.size() > 0) {
                                                        VRL vrl2;
                                                        Properties temp_zip_item;
                                                        if (current_download_item != null) {
                                                            if (current_download_item != null) {
                                                                ServerStatus.siVG("outgoing_transfers").remove(current_download_item);
                                                            }
                                                            current_download_item = null;
                                                        }
                                                        Properties zipItem = (Properties)this.zipFiles.elementAt(0);
                                                        this.zipFiles.removeElementAt(0);
                                                        if (zipItem.getProperty("type", "").equalsIgnoreCase("DIR") && zipItem.getProperty("privs").toUpperCase().indexOf("(VIEW)") < 0) {
                                                            zipItem = null;
                                                        } else if (!(crushftp.handlers.Common.filter_check("L", zipItem.getProperty("name"), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter")) && crushftp.handlers.Common.filter_check("F", String.valueOf(zipItem.getProperty("name")) + (zipItem.getProperty("type").equalsIgnoreCase("DIR") && !zipItem.getProperty("name").endsWith("/") ? "/" : ""), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter")) && crushftp.handlers.Common.filter_check("F", String.valueOf(zipItem.getProperty("name")) + (zipItem.getProperty("type").equalsIgnoreCase("DIR") && !zipItem.getProperty("name").endsWith("/") ? "/" : ""), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter")))) {
                                                            zipItem = null;
                                                        } else if (!this.thisSession.check_access_privs(zipItem.getProperty("root_dir"), "RETR", zipItem)) {
                                                            zipItem = null;
                                                        } else if (zipItem.getProperty("url").startsWith("virtual:") && (temp_zip_item = this.thisSession.uVFS.get_item((vrl2 = new VRL(zipItem.getProperty("url"))).getPath())) != null) {
                                                            zipItem.put("url", temp_zip_item.getProperty("url"));
                                                        }
                                                        long rest = 0L;
                                                        if (zipItem != null) {
                                                            this.zippedFiles.addElement(zipItem);
                                                            rest = Long.parseLong(zipItem.getProperty("rest", "-1"));
                                                            while (zipItem.getProperty("privs").indexOf("(read)") < 0 || zipItem.getProperty("privs").indexOf("(invisible)") >= 0 && zipItem.getProperty("privs").indexOf("(inherited)") < 0 || zipItem.getProperty("type", "").equalsIgnoreCase("DIR") && zipItem.getProperty("privs").indexOf("(view)") < 0) {
                                                                zipItem = (Properties)this.zipFiles.elementAt(0);
                                                                this.zipFiles.removeElementAt(0);
                                                                if (this.zipFiles.size() != 0 || zipItem.getProperty("privs").indexOf("(read)") >= 0 && zipItem.getProperty("privs").indexOf("(invisible)") < 0 && zipItem.getProperty("privs").indexOf("(view)") >= 0) continue;
                                                                zipItem = null;
                                                                break;
                                                            }
                                                        }
                                                        if (zipItem != null) {
                                                            Zin = new VRL(zipItem.getProperty("url"));
                                                            if (this.c != null) {
                                                                this.c.close();
                                                                this.thisSession.uVFS.releaseClient(this.c);
                                                            }
                                                            this.c = this.thisSession.uVFS.getClient(zipItem);
                                                            zipItem.put("url", Zin.toString());
                                                            while ((stat = this.c.stat(Zin.getPath())).getProperty("type").equals("DIR")) {
                                                                FileArchiveEntry ze = null;
                                                                zipItem.put("zipPath", String.valueOf(zipItem.getProperty("root_dir", "").substring(the_file_path.length())) + zipItem.getProperty("name") + "/");
                                                                if (!this.zippedPaths.contains(zipItem.getProperty("zipPath"))) {
                                                                    this.zippedPaths.add(zipItem.getProperty("zipPath"));
                                                                    ze = new FileArchiveEntry(zipItem.getProperty("zipPath"));
                                                                    ze.setTime(Long.parseLong(zipItem.getProperty("modified", String.valueOf(System.currentTimeMillis()))));
                                                                    ((FileArchiveOutputStream)((Object)this.data_os)).putArchiveEntry(ze);
                                                                    ((FileArchiveOutputStream)((Object)this.data_os)).closeArchiveEntry();
                                                                }
                                                                if (this.zipFiles.size() == 0 && this.activeZipThreads.size() == 0) {
                                                                    Thread.sleep(1000L);
                                                                    if (this.zipFiles.size() > 0) continue;
                                                                    Zin = null;
                                                                    break;
                                                                }
                                                                if (this.zipFiles.size() > 0) {
                                                                    zipItem = (Properties)this.zipFiles.elementAt(0);
                                                                    this.zipFiles.removeElementAt(0);
                                                                    Zin = new VRL(zipItem.getProperty("url"));
                                                                    stat = this.c.stat(Zin.getPath());
                                                                    if (this.zipFiles.size() != 0 || !stat.getProperty("type").equalsIgnoreCase("DIR")) continue;
                                                                    zipItem.put("zipPath", String.valueOf(zipItem.getProperty("root_dir", "").substring(the_file_path.length())) + zipItem.getProperty("name") + "/");
                                                                    if (!this.zippedPaths.contains(zipItem.getProperty("zipPath"))) {
                                                                        this.zippedPaths.add(zipItem.getProperty("zipPath"));
                                                                        ze = new FileArchiveEntry(zipItem.getProperty("zipPath"));
                                                                        ze.setTime(Long.parseLong(zipItem.getProperty("modified", String.valueOf(System.currentTimeMillis()))));
                                                                        ((FileArchiveOutputStream)((Object)this.data_os)).putArchiveEntry(ze);
                                                                        ((FileArchiveOutputStream)((Object)this.data_os)).closeArchiveEntry();
                                                                    }
                                                                    Zin = null;
                                                                    break;
                                                                }
                                                                Thread.sleep(100L);
                                                            }
                                                        }
                                                        if (zipItem == null || Zin == null || this.c.stat(Zin.getPath()) == null) {
                                                            Zin = null;
                                                            data_read = 0;
                                                        }
                                                        if (this.zipFiles.size() == 0 && Zin == null) {
                                                            data_read = -1;
                                                            Zin = null;
                                                        } else if (Zin != null) {
                                                            String extra = "";
                                                            if (rest != -1L) {
                                                                extra = ":REST=" + rest + ";";
                                                            }
                                                            zipItem.put("zipPath", String.valueOf(zipItem.getProperty("root_dir", "").substring(the_file_path.length())) + zipItem.getProperty("name") + extra);
                                                            if (!this.zippedPaths.contains(zipItem.getProperty("zipPath"))) {
                                                                this.zippedPaths.add(zipItem.getProperty("zipPath"));
                                                                FileArchiveEntry ze = new FileArchiveEntry(zipItem.getProperty("zipPath"));
                                                                ze.setTime(Long.parseLong(zipItem.getProperty("modified", String.valueOf(System.currentTimeMillis()))));
                                                                try {
                                                                    ((FileArchiveOutputStream)((Object)this.data_os)).putArchiveEntry(ze);
                                                                    this.in = this.c.download(Zin.getPath(), rest, -1L, binary_mode);
                                                                    if (current_download_item != null) {
                                                                        ServerStatus.siVG("outgoing_transfers").remove(current_download_item);
                                                                    }
                                                                    if (zipItem != null) {
                                                                        current_download_item = this.make_current_item(start_transfer_time, zipItem);
                                                                    }
                                                                    if (!Common.dmz_mode) {
                                                                        if (!this.thisSession.user.getProperty("fileEncryptionKey", "").equals("") || !this.thisSession.user.getProperty("fileDecryptionKey", "").equals("")) {
                                                                            this.in = crushftp.handlers.Common.getDecryptedStream(this.in, this.thisSession.user.getProperty("fileEncryptionKey", ""), this.thisSession.user.getProperty("fileDecryptionKey", ""), this.thisSession.user.getProperty("fileDecryptionKeyPass", ""));
                                                                        } else if (!ServerStatus.SG("fileEncryptionKey").equals("") || ServerStatus.BG("fileDecryption")) {
                                                                            this.in = crushftp.handlers.Common.getDecryptedStream(this.in, ServerStatus.SG("fileEncryptionKey"), ServerStatus.SG("fileDecryptionKey"), ServerStatus.SG("fileDecryptionKeyPass"));
                                                                        }
                                                                    }
                                                                    data_read = 0;
                                                                }
                                                                catch (IOException e) {
                                                                    if (e.toString().toLowerCase().indexOf("duplicate") >= 0) {
                                                                        Zin = null;
                                                                        data_read = 0;
                                                                    }
                                                                    throw e;
                                                                }
                                                            }
                                                        }
                                                    } else if (this.activeZipThreads.size() == 0) {
                                                        data_read = -1;
                                                        Zin = null;
                                                    } else {
                                                        Thread.sleep(100L);
                                                    }
                                                } else {
                                                    data_read = this.in.read(temp_array);
                                                    if (data_read < 0) {
                                                        this.in.close();
                                                        this.in = null;
                                                        Zin = null;
                                                        data_read = 0;
                                                        ((FileArchiveOutputStream)((Object)this.data_os)).closeArchiveEntry();
                                                        this.data_os.flush();
                                                    }
                                                }
                                            } else if (!binary_mode) {
                                                if (inASCII == null) {
                                                    inASCII = new LineReader(this.in);
                                                }
                                                if ((read_string = inASCII.readLineCRLF()) != null) {
                                                    data_read = read_string.length;
                                                    temp_array = read_string;
                                                } else {
                                                    data_read = -1;
                                                }
                                            } else {
                                                data_read = this.proxy_remote_in != null ? this.proxy_remote_in.read(temp_array) : this.in.read(temp_array);
                                            }
                                            if (!(data_read > 0 || this.zipping || the_file_path.indexOf("/WebInterface/") >= 0 || the_file_name.equals("CrushFTP.jar") || this.otherFile != null && this.otherFile.getPath().indexOf("/WebInterface/") >= 0 || !vrl.getProtocol().equalsIgnoreCase("file") || (cur_file_size = Long.parseLong((stat = this.c.stat(vrl.getPath())).getProperty("size"))) <= start_file_size || pgp || this.item.getProperty("privs", "").indexOf("(dynamic_size)") >= 0)) {
                                                data_read = 0;
                                                if (file_changing_loc == this.current_loc) {
                                                    ++file_changing_loop_intervals;
                                                }
                                                file_changing_loc = this.current_loc;
                                                if (file_changing_loop_intervals > 20) {
                                                    data_read = -1;
                                                }
                                                start_file_size = cur_file_size - 1L;
                                                Thread.sleep(1000L);
                                            }
                                            if (data_read > 0) {
                                                has_bytes = true;
                                                if (this.zlibing) {
                                                    ((DeflaterOutputStream)this.data_os).write(temp_array, 0, data_read);
                                                    if (!vrl.getProtocol().equalsIgnoreCase("file") && this.proxy != null) {
                                                        this.proxy.write(temp_array, 0, data_read);
                                                    }
                                                } else {
                                                    this.data_os.write(temp_array, 0, data_read);
                                                    if (this.proxy_remote_in != null) {
                                                        this.data_os.flush();
                                                    }
                                                    if (!vrl.getProtocol().equalsIgnoreCase("file") && this.proxy != null) {
                                                        this.proxy.write(temp_array, 0, data_read);
                                                    }
                                                }
                                                RETR_handler.updateTransferStats(this.thisSession, data_read, free_ratio_item, temp_array, this.md5, current_download_item);
                                                if (pre_event_info != null) {
                                                    pre_event_info.put("info_download_event_status", "downloading");
                                                    if (current_download_item != null) {
                                                        pre_event_info.put("info_download_loc", current_download_item.getProperty("current_loc"));
                                                        pre_event_info.put("info_download_speed", current_download_item.getProperty("the_file_speed"));
                                                    }
                                                }
                                                this.current_loc += (long)(data_read > 0 ? data_read : 0);
                                                file_changing_loop_intervals = 0;
                                            }
                                            if (this.max_loc > 0L && this.max_loc - this.current_loc == 0L) {
                                                data_read = -1;
                                            }
                                        } else {
                                            data_read = -1;
                                            this.thisSession.not_done = this.thisSession.ftp_write_command_logged("226-%RETR-ratio exceeded%", "RETR");
                                        }
                                        if (this.thisSession.uiLG("overall_transfer_speed") < (long)this.thisSession.IG("min_download_speed") && this.thisSession.IG("min_download_speed") > 0) {
                                            if (new_date.getTime() - this.thisSession.uiLG("start_transfer_time") > (long)(ServerStatus.IG("minimum_speed_warn_seconds") * 1000)) {
                                                throw new Exception(LOC.G("Transfer speed is less than minimum required after at least 10 seconds.") + "  " + this.thisSession.uiLG("overall_transfer_speed") + "K/sec < " + this.thisSession.IG("min_download_speed") + "K/sec.");
                                            }
                                        }
                                        if (this.thisSession.uiLG("overall_transfer_speed") < (long)Math.abs(this.thisSession.IG("min_download_speed")) && this.thisSession.IG("min_download_speed") < 0) {
                                            if (new_date.getTime() - this.thisSession.uiLG("start_transfer_time") > (long)(ServerStatus.IG("minimum_speed_alert_seconds") * 1000)) {
                                                if (!user_speed_notified) {
                                                    ServerStatus.thisObj.runAlerts("user_download_speed", this.thisSession);
                                                }
                                                user_speed_notified = true;
                                            }
                                        }
                                        this.endLoop = new Date().getTime();
                                    }
                                    if (current_download_item != null) {
                                        if (current_download_item != null) {
                                            ServerStatus.siVG("outgoing_transfers").remove(current_download_item);
                                        }
                                        current_download_item = null;
                                    }
                                    if (this.filetree_list != null && this.filetree_list.stop_message.toUpperCase().indexOf("FAILED") >= 0) {
                                        throw new Exception("Filetree list failed!");
                                    }
                                    try {
                                        this.thisSession.uiPUT("sending_file", "false");
                                    }
                                    catch (Exception new_date) {
                                        // empty catch block
                                    }
                                    if (this.zipping) {
                                        this.thisSession.uiPUT("file_length", String.valueOf(this.thisSession.uiLG("bytes_sent") - this.thisSession.uiLG("start_transfer_byte_amount")));
                                    }
                                    try {
                                        if (this.in != null) {
                                            this.in.close();
                                        }
                                    }
                                    catch (Exception new_date) {
                                        // empty catch block
                                    }
                                    if (this.c != null) {
                                        this.c.close();
                                    }
                                    try {
                                        this.proxy.close();
                                    }
                                    catch (Exception new_date) {
                                        // empty catch block
                                    }
                                    if (this.zipping) {
                                        ((FileArchiveOutputStream)((Object)this.data_os)).finish();
                                    }
                                    if (this.zlibing) {
                                        ((DeflaterOutputStream)this.data_os).finish();
                                    }
                                    try {
                                        this.data_os.flush();
                                    }
                                    catch (Exception new_date) {
                                        // empty catch block
                                    }
                                    try {
                                        if (!has_bytes && this.data_sock != null && this.data_sock instanceof SSLSocket) {
                                            crushftp.handlers.Common.debug(1, this.data_sock + ":" + "Forcing SSL handshake to start...");
                                            crushftp.handlers.Common.configureSSLTLSSocket((SSLSocket)this.data_sock);
                                            ((SSLSocket)this.data_sock).startHandshake();
                                            crushftp.handlers.Common.debug(1, this.data_sock + ":" + "Forced SSL handshake complete.");
                                        }
                                        if ((this.otherFile == null || this.zipping || this.zlibing) && !this.httpDownload) {
                                            try {
                                                if (this.data_sock instanceof SSLSocket) {
                                                    try {
                                                        ((SSLSocket)this.data_sock).shutdownOutput();
                                                    }
                                                    catch (Exception new_date) {
                                                        // empty catch block
                                                    }
                                                }
                                                this.data_os.close();
                                            }
                                            catch (Exception new_date) {
                                                // empty catch block
                                            }
                                        }
                                        if (this.data_sock != null) {
                                            this.data_sock.close();
                                        }
                                    }
                                    catch (Exception e) {
                                        Log.log("SERVER", 1, e);
                                    }
                                    if (this.data_sock != null) {
                                        this.thisSession.old_data_socks.remove(this.data_sock);
                                    }
                                    if (pre_event_info != null) {
                                        pre_event_info.put("info_download_event_status", "done");
                                    }
                                    RETR_handler.downloadFinishedSuccess(this.thisSession, the_file_path, the_file_name, this.item, this.thisSession.uiLG("start_resume_loc"), this.httpDownload, this.zippedFiles, start_transfer_time, this.md5, resume_loc, this.current_loc, binary_mode);
                                }
                                catch (Exception e) {
                                    this.stop_message = e.toString();
                                    this.inError = true;
                                    Log.log("DOWNLOAD", 1, e);
                                    if (current_download_item != null) {
                                        if (current_download_item != null) {
                                            ServerStatus.siVG("outgoing_transfers").remove(current_download_item);
                                        }
                                        current_download_item = null;
                                    }
                                    try {
                                        this.thisSession.uiPUT("overall_transfer_speed", "0");
                                    }
                                    catch (Exception pp) {
                                        // empty catch block
                                    }
                                    try {
                                        this.thisSession.uiPUT("current_transfer_speed", "0");
                                    }
                                    catch (Exception pp) {
                                        // empty catch block
                                    }
                                    try {
                                        this.thisSession.uiPUT("sending_file", "false");
                                    }
                                    catch (Exception pp) {
                                        // empty catch block
                                    }
                                    try {
                                        this.c.close();
                                    }
                                    catch (Exception pp) {
                                        // empty catch block
                                    }
                                    this.c = this.thisSession.uVFS.releaseClient(this.c);
                                    try {
                                        this.proxy.close();
                                    }
                                    catch (Exception pp) {
                                        // empty catch block
                                    }
                                    try {
                                        this.data_os.flush();
                                    }
                                    catch (Exception pp) {
                                        // empty catch block
                                    }
                                    try {
                                        this.data_os.close();
                                    }
                                    catch (Exception pp) {
                                        // empty catch block
                                    }
                                    try {
                                        this.data_sock.close();
                                    }
                                    catch (Exception pp) {
                                        // empty catch block
                                    }
                                    if (this.data_sock != null) {
                                        this.thisSession.old_data_socks.remove(this.data_sock);
                                    }
                                    if (pre_event_info != null) {
                                        pre_event_info.put("info_download_event_status", "done");
                                        pre_event_info.put("info_download_event_error", this.stop_message);
                                    }
                                    Properties fileItem = (Properties)this.item.clone();
                                    fileItem.put("url", this.item.getProperty("url", ""));
                                    fileItem.put("the_file_path", String.valueOf(the_file_path) + the_file_name);
                                    fileItem.put("the_file_name", the_file_name);
                                    fileItem.put("the_file_size", String.valueOf(this.thisSession.uiLG("file_length")));
                                    fileItem.put("the_file_speed", String.valueOf(this.thisSession.uiLG("overall_transfer_speed")));
                                    fileItem.put("the_file_error", this.stop_message);
                                    fileItem.put("the_file_type", this.item.getProperty("type", "FILE"));
                                    fileItem.put("the_file_status", "FAILED");
                                    this.thisSession.uiPUT("session_downloads", String.valueOf(this.thisSession.uiSG("session_downloads")) + the_file_path + the_file_name + ":" + this.thisSession.uiLG("file_length") + LOC.G("bytes") + " @ " + this.thisSession.uiLG("overall_transfer_speed") + "k/sec. " + LOC.G("FAILED") + CRLF);
                                    if (this.thisSession.LG("max_download_amount") != 0L) {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged("226-" + this.SG("Max Download") + ".  " + LOC.G("Sent") + ":" + this.thisSession.uiLG("bytes_sent") / 1024L + "k.  " + this.SG("Max") + ":" + this.thisSession.LG("max_download_amount") + "k.  " + LOC.G("Available") + ":" + (this.thisSession.LG("max_download_amount") - this.thisSession.uiLG("bytes_sent") / 1024L) + "k.", "RETR");
                                    }
                                    if (this.thisSession.LG("max_download_count") != 0L) {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged("226-" + this.SG("Max Download") + ".  " + LOC.G("Sent") + ":" + this.thisSession.uiLG("session_download_count") + ".  " + this.SG("Max") + ":" + this.thisSession.LG("max_download_count") + ".  " + LOC.G("Available") + ":" + (this.thisSession.LG("max_download_count") - this.thisSession.uiLG("session_download_count")) + ".", "RETR");
                                    }
                                    if (this.thisSession.IG("ratio") != 0) {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged("226-" + LOC.G("Ratio is") + " " + this.thisSession.IG("ratio") + " to 1. " + LOC.G("Received") + ":" + Common.format_bytes_short(this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) + " " + LOC.G("Sent") + ":" + Common.format_bytes_short(this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent")) + "  " + LOC.G("Available") + ":" + Common.format_bytes_short((this.thisSession.uiLG("bytes_received") + this.thisSession.uiLG("ratio_bytes_received")) * this.thisSession.LG("ratio") - (this.thisSession.uiLG("bytes_sent") + this.thisSession.uiLG("ratio_bytes_sent"))), "RETR");
                                    }
                                    if (ServerStatus.BG("rfc_proxy") && e.getMessage() != null && e.getMessage().length() > 3 && e.getMessage().charAt(3) == ' ') {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged(e.getMessage(), "RETR");
                                    } else {
                                        this.thisSession.not_done = this.thisSession.ftp_write_command_logged("550", String.valueOf(e.getMessage()) + " " + LOC.G("(\"$0$1\") RETR", the_file_path, the_file_name), "RETR");
                                        this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                                    }
                                    if (the_file_path.indexOf("/WebInterface/") >= 0) break block266;
                                    this.thisSession.do_event5("DOWNLOAD", fileItem);
                                }
                            }
                        }
                    }
                }
            }
            while (this.thisSession.uiSG("session_downloads").length() / 160 > ServerStatus.IG("user_log_buffer") && this.thisSession.uiSG("session_downloads").length() > 160) {
                this.thisSession.uiPUT("session_downloads", this.thisSession.uiSG("session_downloads").substring(160));
            }
            this.zipFiles.removeAllElements();
            this.zippedFiles.removeAllElements();
            this.zippedPaths.removeAllElements();
            this.resumed_file = false;
            this.zipping = false;
            this.zlibing = false;
            try {
                this.thisSession.uiPUT("last_time_remaining", "<" + LOC.G("None Active") + ">");
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                this.thisSession.uiPUT("last_action", "RETR-Done.");
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (this.otherFile == null) {
                try {
                    this.data_os.close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            try {
                if (this.data_sock != null) {
                    this.data_sock.close();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (this.data_sock != null) {
                this.thisSession.old_data_socks.remove(this.data_sock);
            }
            try {
                this.proxy_remote_in.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                this.thisSession.uiPUT("overall_transfer_speed", "0");
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                this.thisSession.uiPUT("current_transfer_speed", "0");
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                this.thisSession.uiPUT("sending_file", "false");
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.thisSession.uiPUT("sending_file", "false");
            this.thisSession.start_idle_timer();
            this.active2.put("streamOpenStatus", "CLOSED");
            try {
                this.c = this.thisSession.uVFS.releaseClient(this.c);
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (this.runOnce) {
                return;
            }
        }
        catch (Exception e) {
            Log.log("DOWNLOAD", 1, e);
        }
        finally {
            this.thisThread = null;
            this.active2.put("active", "false");
        }
        this.kill();
        while (this.thisSession.retr_files_pool_used.indexOf(this) >= 0) {
            this.thisSession.retr_files_pool_used.removeElement(this);
        }
        if (this.thisSession.retr_files_pool_free.indexOf(this) < 0) {
            this.thisSession.retr_files_pool_free.addElement(this);
        }
    }

    public void kill() {
        this.die_now = true;
        try {
            this.data_os.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.data_sock.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (this.data_sock != null) {
            this.thisSession.old_data_socks.remove(this.data_sock);
        }
        try {
            this.c.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.c = this.thisSession.uVFS.releaseClient(this.c);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.proxy.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.active2.put("active", "false");
    }

    public String SG(String data) {
        return this.thisSession.SG(data);
    }

    public int reloadBandwidthLimits() {
        int lesserSpeed = this.thisSession.IG("speed_limit_download");
        if (lesserSpeed > 0 && lesserSpeed > ServerStatus.IG("max_server_download_speed") && ServerStatus.IG("max_server_download_speed") > 0 || lesserSpeed == 0) {
            lesserSpeed = ServerStatus.IG("max_server_download_speed");
        }
        return lesserSpeed;
    }

    public static void downloadFinishedSuccess(SessionCrush thisSession, String the_file_path, String the_file_name, Properties item, long starting_loc, boolean httpDownload, Vector zippedFiles, long start_transfer_time, MessageDigest md5, long resume_loc, long current_loc, boolean binary_mode) throws Exception {
        String md5Str = new BigInteger(1, md5.digest()).toString(16).toLowerCase();
        while (md5Str.length() < 32) {
            md5Str = "0" + md5Str;
        }
        thisSession.uiPUT("md5", md5Str);
        thisSession.uiPUT("sfv", md5Str);
        if (thisSession.uiLG("overall_transfer_speed") == 0L || thisSession.uiLG("overall_transfer_speed") < 0L) {
            long speed = (thisSession.uiLG("bytes_sent") - thisSession.uiLG("start_transfer_byte_amount")) / 1024L;
            if (speed < 0L) {
                speed = 0L;
            }
            thisSession.uiPUT("overall_transfer_speed", String.valueOf((thisSession.uiLG("bytes_sent") - thisSession.uiLG("start_transfer_byte_amount")) / 1024L));
        }
        ServerStatus.thisObj.server_info.put("downloaded_files", "" + (ServerStatus.siIG("downloaded_files") + 1));
        Properties fileItem = (Properties)item.clone();
        fileItem.put("the_command", "RETR");
        String the_correct_path = String.valueOf(the_file_path) + the_file_name;
        if (!the_correct_path.toUpperCase().startsWith(thisSession.SG("root_dir").toUpperCase())) {
            the_correct_path = String.valueOf(thisSession.SG("root_dir")) + the_correct_path.substring(1);
        }
        fileItem.put("the_command_data", the_correct_path);
        fileItem.put("url", item.getProperty("url", ""));
        fileItem.put("the_file_path", String.valueOf(the_file_path) + the_file_name);
        fileItem.put("the_file_name", the_file_name);
        fileItem.put("the_file_size", String.valueOf(thisSession.uiLG("file_length")));
        fileItem.put("the_file_speed", String.valueOf(thisSession.uiLG("overall_transfer_speed")));
        fileItem.put("the_file_start", String.valueOf(start_transfer_time));
        fileItem.put("the_file_end", String.valueOf(new Date().getTime()));
        fileItem.put("the_file_error", "");
        fileItem.put("the_file_status", "SUCCESS");
        fileItem.put("the_file_resume_loc", String.valueOf(resume_loc));
        fileItem.put("the_file_type", item.getProperty("type", "FILE"));
        fileItem.put("the_file_md5", md5Str);
        Vector<Properties> downloadItems = new Vector<Properties>();
        downloadItems.addElement(fileItem);
        boolean skipEvent = false;
        if (fileItem.getProperty("the_file_path", "").indexOf("/WebInterface/") >= 0 || fileItem.getProperty("the_file_name", "").equals(".DS_Store") || fileItem.getProperty("the_file_name", "").equals("custom.js") || fileItem.getProperty("the_file_name", "").equals("custom.css") || resume_loc > 0L && httpDownload) {
            skipEvent = true;
        } else if (zippedFiles.size() > 0) {
            downloadItems.removeAllElements();
            int x = 0;
            while (x < zippedFiles.size()) {
                Properties p = (Properties)zippedFiles.elementAt(x);
                if (!p.getProperty("zipPath", "").equals("")) {
                    fileItem = (Properties)fileItem.clone();
                    fileItem.put("the_file_path", String.valueOf(the_file_path) + p.getProperty("zipPath"));
                    fileItem.put("the_file_name", crushftp.handlers.Common.last(p.getProperty("zipPath")));
                    fileItem.put("url", p.getProperty("url", ""));
                    fileItem.put("the_file_size", p.getProperty("size"));
                    fileItem.put("the_file_type", p.getProperty("type", "FILE"));
                    thisSession.uiPUT("session_downloads", String.valueOf(thisSession.uiSG("session_downloads")) + the_file_path + the_file_name + ":" + thisSession.uiLG("file_length") + LOC.G("bytes") + " @ " + thisSession.uiLG("overall_transfer_speed") + "k/sec." + CRLF);
                    thisSession.uiPUT("session_download_count", String.valueOf(thisSession.uiIG("session_download_count") + 1));
                    ServerStatus.thisObj.statTools.add_item_stat(thisSession, fileItem, "DOWNLOAD");
                    downloadItems.addElement(fileItem);
                }
                ++x;
            }
        } else {
            thisSession.uiPUT("session_downloads", String.valueOf(thisSession.uiSG("session_downloads")) + the_file_path + the_file_name + ":" + thisSession.uiLG("file_length") + LOC.G("bytes") + " @ " + thisSession.uiLG("overall_transfer_speed") + "k/sec." + CRLF);
            thisSession.uiPUT("session_download_count", String.valueOf(thisSession.uiIG("session_download_count") + 1));
            ServerStatus.thisObj.statTools.add_item_stat(thisSession, fileItem, "DOWNLOAD");
        }
        String responseNumber = "226";
        String message_string = "";
        Properties p = new Properties();
        p.put("responseNumber", responseNumber);
        p.put("message_string", message_string);
        if (!skipEvent) {
            thisSession.runPlugin("after_download", p);
        }
        responseNumber = p.getProperty("responseNumber", responseNumber);
        if (!(message_string = p.getProperty("message_string", message_string)).equals("") && ServerStatus.BG("log_transfer_speeds")) {
            thisSession.not_done = thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-", "RETR");
            thisSession.not_done = thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-" + message_string, "RETR");
            thisSession.not_done = thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-", "RETR");
        }
        String stop_message = "Failure!";
        if (!responseNumber.equals("226")) {
            stop_message = message_string;
        }
        if (thisSession.LG("max_download_amount") != 0L) {
            thisSession.not_done = thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-" + LOC.G("Max Download") + ".  " + LOC.G("Sent") + ":" + thisSession.uiLG("bytes_sent") / 1024L + "k.  " + LOC.G("Max") + ":" + thisSession.LG("max_download_amount") + "k.  " + LOC.G("Available") + ":" + (thisSession.LG("max_download_amount") - thisSession.uiLG("bytes_sent") / 1024L) + "k.", "RETR");
        }
        if (thisSession.IG("max_download_count") != 0) {
            thisSession.not_done = thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-" + LOC.G("Max Download") + ".  " + LOC.G("Sent") + ":" + thisSession.uiLG("session_download_count") + ".  " + LOC.G("Max") + ":" + thisSession.IG("max_download_count") + ".  " + LOC.G("Available") + ":" + ((long)thisSession.IG("max_download_count") - thisSession.uiLG("session_download_count")) + ".", "RETR");
        }
        if (thisSession.IG("ratio") != 0) {
            thisSession.not_done = thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-" + LOC.G("Ratio is $0 to 1.", String.valueOf(thisSession.IG("ratio"))) + " " + LOC.G("Received") + ":" + (thisSession.uiLG("bytes_received") + thisSession.uiLG("ratio_bytes_received")) / 1024L + "k " + LOC.G("Sent") + ":" + (thisSession.uiLG("bytes_sent") + thisSession.uiLG("ratio_bytes_sent")) / 1024L + "k.  " + LOC.G("Available") + ":" + ((thisSession.uiLG("bytes_received") + thisSession.uiLG("ratio_bytes_received")) * (long)thisSession.IG("ratio") - (thisSession.uiLG("bytes_sent") + thisSession.uiLG("ratio_bytes_sent"))) / 1024L + "k.", "RETR");
        }
        if (ServerStatus.BG("log_transfer_speeds")) {
            thisSession.not_done = thisSession.ftp_write_command_logged(String.valueOf(responseNumber) + "-%RETR-speed%", "RETR");
        }
        thisSession.not_done = responseNumber.equals("226") ? (ServerStatus.BG("generic_ftp_responses") ? thisSession.ftp_write_command_logged("226", "Transfer complete.", "RETR") : thisSession.ftp_write_command_logged(String.valueOf(responseNumber), "%RETR-end% " + LOC.G("(\"$0$1\") RETR", the_file_path, the_file_name), "RETR")) : thisSession.ftp_write_command_logged(String.valueOf(responseNumber), String.valueOf(stop_message) + " " + LOC.G("(\"$0$1\") RETR", the_file_path, the_file_name), "RETR");
        boolean pgp = RETR_handler.checkPgp(thisSession, item);
        if (!(current_loc != thisSession.uiLG("file_length") && !pgp && binary_mode || skipEvent)) {
            int x = 0;
            while (x < downloadItems.size()) {
                Properties fi = (Properties)downloadItems.elementAt(x);
                thisSession.do_event5("DOWNLOAD", fi);
                ++x;
            }
        } else {
            thisSession.add_log("[" + thisSession.uiSG("user_number") + ":" + thisSession.uiSG("user_name") + ":" + thisSession.uiSG("user_ip") + "] WROTE: *Event skipped since file download size didn't match:" + current_loc + "!=" + thisSession.uiLG("file_length"), "RETR");
        }
    }

    public static boolean checkPgp(SessionCrush thisSession, Properties item) {
        boolean pgp = false;
        if (!thisSession.user.getProperty("fileEncryptionKey", "").equals("") || !thisSession.user.getProperty("fileDecryptionKey", "").equals("")) {
            pgp = true;
        } else if (!ServerStatus.SG("fileEncryptionKey").equals("") || ServerStatus.BG("fileDecryption")) {
            pgp = true;
        } else if (item != null && (item.getProperty("privs", "").indexOf("(pgpDecryptDownload") >= 0 || item.getProperty("privs", "").indexOf("(pgpEncryptDownload") >= 0)) {
            pgp = true;
        }
        return pgp;
    }

    public Properties make_current_item(long start_transfer_time, Properties tmp) {
        Properties current_item = new Properties();
        current_item.put("name", tmp.getProperty("name"));
        current_item.put("root_dir", tmp.getProperty("root_dir"));
        current_item.put("modified", tmp.getProperty("modified"));
        current_item.put("the_file_size", tmp.getProperty("size", "0"));
        current_item.put("the_file_speed", "0");
        current_item.put("current_loc", String.valueOf(this.current_loc));
        current_item.put("user_name", this.thisSession.uiSG("user_name"));
        current_item.put("user_ip", this.thisSession.uiSG("user_ip"));
        current_item.put("user_protocol", this.thisSession.uiSG("user_protocol"));
        current_item.put("the_file_start", String.valueOf(start_transfer_time));
        current_item.put("the_file_type", tmp.getProperty("type", "FILE"));
        ServerStatus.siVG("outgoing_transfers").addElement(current_item);
        return current_item;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void updateTransferStats(SessionCrush thisSession, int data_read, boolean free_ratio_item, byte[] temp_array, MessageDigest md5, Properties current_download_item) {
        thisSession.active_transfer();
        if (data_read < 0) {
            return;
        }
        if (System.getProperty("crushftp.retr_md5", "true").equals("true")) {
            md5.update(temp_array, 0, data_read);
        }
        thisSession.uiPPUT("bytes_sent", data_read);
        thisSession.uiPUT("bytes_sent_formatted", Common.format_bytes_short(Long.parseLong(thisSession.uiSG("bytes_sent"))));
        ServerStatus.thisObj.total_server_bytes_sent += (long)data_read;
        if (free_ratio_item) {
            thisSession.uiPPUT("ratio_bytes_received", data_read);
        }
        if (current_download_item != null) {
            current_download_item.put("current_loc", String.valueOf(Long.parseLong(current_download_item.getProperty("current_loc", "0")) + (long)data_read));
            current_download_item.put("the_file_speed", String.valueOf(thisSession.uiLG("current_transfer_speed")));
            current_download_item.put("now", String.valueOf(System.currentTimeMillis()));
        }
        if (thisSession.server_item.containsKey("bytes_sent")) {
            Properties properties = thisSession.server_item;
            synchronized (properties) {
                thisSession.server_item.put("bytes_sent", String.valueOf(Long.parseLong(thisSession.server_item.getProperty("bytes_sent", "0")) + (long)data_read));
            }
        }
    }
}

