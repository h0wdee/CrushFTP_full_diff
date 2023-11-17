/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server;

import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import com.crushftp.ssl.sni.SNIReady;
import crushftp.gui.LOC;
import crushftp.handlers.Common;
import crushftp.handlers.IdleMonitor;
import crushftp.handlers.Log;
import crushftp.handlers.SSLKeyManager;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.UserTools;
import crushftp.server.LIST_handler;
import crushftp.server.QuickConnect;
import crushftp.server.RETR_handler;
import crushftp.server.STOR_handler;
import crushftp.server.ServerStatus;
import crushftp.server.daemon.TCPServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ServerSessionFTP
implements Runnable {
    public Socket sock = null;
    public Socket sockOriginal = null;
    SessionCrush thisSession = null;
    public BufferedReader is = null;
    public OutputStream os = null;
    public LIST_handler list_files = null;
    static transient Object portLocker = new Object();
    public Thread this_thread = null;
    int current_pasv_port = 0;
    public Properties thread_killer_items = new Properties();
    public StringBuffer sni_keystore_used = new StringBuffer();
    SSLSocketFactory factory = null;
    SSLContext ssl_context = null;

    public ServerSessionFTP(Socket sock, int user_number, String user_ip, int listen_port, String listen_ip, String listen_ip_port, Properties server_item) {
        this.sock = sock;
        this.thisSession = new SessionCrush(sock, user_number, user_ip, listen_port, listen_ip, listen_ip_port, server_item);
        this.thisSession.retr_files_pool_free.addElement(new RETR_handler());
        this.thisSession.stor_files_pool_free.addElement(new STOR_handler());
        this.thisSession.setFtp(this);
    }

    public void give_thread_pointer(Thread this_thread) {
        this.this_thread = this_thread;
    }

    @Override
    public void run() {
        this.this_thread = Thread.currentThread();
        try {
            this.thisSession.thread_killer_item = (IdleMonitor)this.thread_killer_items.get(this.sock.toString());
            if (this.thisSession.thread_killer_item == null) {
                this.thisSession.thread_killer_item = new IdleMonitor(this.thisSession, new Date().getTime(), Integer.parseInt(System.getProperty("crushftp.max_auth_time", "2")), Thread.currentThread(), this.sock);
            }
            this.thread_killer_items.put(this.sock.toString(), this.thisSession.thread_killer_item);
            ServerStatus.thisObj.hold_user_pointer(this.thisSession.user_info);
            this.sock.setTcpNoDelay(System.getProperty("crushftp.ftp.tcp_no_delay", "false").equals("true"));
            this.is = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
            this.os = this.sock.getOutputStream();
            this.thisSession.add_log("[" + this.thisSession.server_item.getProperty("serverType", "ftp") + ":" + this.thisSession.server_item.getProperty("ip", "0.0.0.0") + ":" + this.thisSession.server_item.getProperty("port", "21") + "][" + this.thisSession.uiSG("user_number") + "] " + LOC.G("Accepting connection from") + ": " + this.thisSession.uiSG("user_ip") + ":" + this.sock.getPort() + "\r\n", "ACCEPT");
            String wMsg = this.thisSession.server_item.getProperty("ftp_welcome_message", ServerStatus.SG("ftp_welcome_message")).trim();
            wMsg = Common.replace_str(wMsg, "\\r", "\r");
            wMsg = Common.replace_str(wMsg, "\\n", "\n");
            String sMsg = ServerStatus.SG("server_start_message").trim();
            sMsg = Common.replace_str(sMsg, "\\r", "\r\n");
            sMsg = Common.replace_str(sMsg, "\\n", "");
            this.write_command("220", String.valueOf(wMsg) + "\r\n" + sMsg);
            while (this.thisSession.not_done && !this.thisSession.uiBG("friendly_quit")) {
                try {
                    Thread.sleep(Integer.parseInt(this.thisSession.server_item.getProperty("commandDelayInterval", "0")));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (this.get_command() < 0) {
                    this.thisSession.not_done = false;
                }
                if (this.thisSession.uiBG("refresh_user")) {
                    this.thisSession.uiPUT("refresh_user", "false");
                    if (!this.thisSession.verify_user(this.thisSession.uiSG("user_name"), this.thisSession.uiSG("current_password"))) {
                        this.thisSession.not_done = false;
                        break;
                    }
                    this.thisSession.setupRootDir(null, true);
                }
                while (this.thisSession.uiBG("pause_now")) {
                    Thread.sleep(1000L);
                }
                if (this.thisSession.uiSG("the_command").equals("USER") && this.thisSession.uiSG("the_command_data").length() > 0) {
                    if (ServerStatus.BG("username_uppercase")) {
                        this.thisSession.uiPUT("the_command_data", this.thisSession.uiSG("the_command_data").toUpperCase());
                    }
                    if (ServerStatus.BG("lowercase_usernames")) {
                        this.thisSession.uiPUT("the_command_data", this.thisSession.uiSG("the_command_data").toLowerCase());
                    }
                    if (this.thisSession.uiSG("the_command_data").indexOf("/") >= 0) {
                        this.thisSession.uiPUT("the_command_data", this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").indexOf("/") + 1));
                    }
                    boolean require_encryptionUser = false;
                    try {
                        long time = Long.parseLong(ServerStatus.siPG("invalid_usernames").getProperty(this.thisSession.uiSG("the_command_data").toUpperCase(), "0:0").split(":")[0]);
                        int attempts = Integer.parseInt(ServerStatus.siPG("invalid_usernames").getProperty(this.thisSession.uiSG("the_command_data").toUpperCase(), "0:0").split(":")[1]);
                        if (time <= 0L || time <= new Date().getTime() - (long)(ServerStatus.IG("invalid_usernames_seconds") * 1000) || attempts <= ServerStatus.IG("invalid_usernames_seconds_attempts")) {
                            require_encryptionUser = UserTools.ut.getUser(this.thisSession.uiSG("listen_ip_port"), this.thisSession.uiSG("the_command_data"), true).getProperty("require_encryption", "false").toUpperCase().equals("TRUE");
                        }
                    }
                    catch (Exception time) {
                        // empty catch block
                    }
                    if (this.thisSession.uiBG("user_logged_in")) {
                        this.thisSession.uiPUT("user_logged_in", "false");
                        this.thisSession.not_done = this.write_command("503", LOC.G("%already_logged_in%"));
                        this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                        this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                    } else if (!QuickConnect.validate_ip(this.thisSession.uiSG("user_ip"), this.thisSession.server_item)) {
                        this.thisSession.not_done = this.write_command("550", LOC.G("IP banned."));
                        this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                        this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                        this.thisSession.not_done = false;
                        this.thisSession.logLogin(false, "Bad ip.");
                    } else if ((this.thisSession.uiBG("require_encryption") || require_encryptionUser) && !this.thisSession.uiBG("secure")) {
                        this.thisSession.not_done = this.write_command("550", LOC.G("This server requires encryption.") + "\r\n" + LOC.G("You must issue the AUTH command to change to an encrypted session before you can attempt to login."));
                        this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                        this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                    } else {
                        this.thisSession.uiPUT("user_name", this.thisSession.uiSG("the_command_data"));
                        this.thisSession.not_done = this.thisSession.uiSG("the_command_data").toUpperCase().equals("ANONYMOUS") ? this.write_command("331", LOC.G("Enter e-mail for password.") + "\r\n" + LOC.G("%USER%")) : this.write_command("331", LOC.G("%USER%"));
                        this.thisSession.runPlugin("beforeLogin", null);
                        this.thisSession.uiPUT("id", String.valueOf(this.thisSession.uiSG("user_number")) + "-" + this.thisSession.uiSG("user_name"));
                    }
                } else if (this.thisSession.uiSG("the_command").equals("PASS")) {
                    boolean needClientAuth;
                    String originalUsername = this.thisSession.uiSG("user_name");
                    this.thisSession.uiPUT("current_password", this.thisSession.uiSG("the_command_data"));
                    boolean bl = needClientAuth = ServerStatus.BG("needClientAuth") || this.thisSession.server_item.getProperty("needClientAuth", "").equals("true") || this.thisSession.user != null && this.thisSession.user.getProperty("needClientAuth", "false").equals("true");
                    if (needClientAuth && this.thisSession.uiBG("secure")) {
                        String subject = "";
                        try {
                            subject = ((SSLSocket)this.sock).getSession().getPeerCertificateChain()[0].getSubjectDN().toString();
                            this.thisSession.add_log("[" + this.thisSession.server_item.getProperty("serverType", "ftp") + ":" + this.thisSession.uiSG("user_number") + ":" + this.thisSession.uiSG("user_name") + ":" + this.thisSession.uiSG("user_ip") + "] " + this.thisSession.SG("WROTE") + ": *" + LOC.G("Found subject name in client certificate: $0", subject.trim()) + "*", "USER");
                            if (subject.toUpperCase().indexOf("CN=") >= 0) {
                                subject = subject.substring(subject.toUpperCase().indexOf("CN="));
                            }
                            this.thisSession.add_log("[" + this.thisSession.server_item.getProperty("serverType", "ftp") + ":" + this.thisSession.uiSG("user_number") + ":" + this.thisSession.uiSG("user_name") + ":" + this.thisSession.uiSG("user_ip") + "] " + this.thisSession.SG("WROTE") + ": *" + LOC.G("Found subject name in client certificate: $0", subject.trim()) + "*", "USER");
                        }
                        catch (Exception attempts) {
                            // empty catch block
                        }
                        if (subject.indexOf(", ") < 0) {
                            subject = String.valueOf(subject) + ", ";
                        }
                        String certUsername = subject.substring(subject.indexOf("=") + 1, subject.indexOf(", "));
                        this.thisSession.uiPUT("user_name", certUsername.trim());
                        this.thisSession.uiPUT("current_password", "");
                        this.thisSession.add_log("[" + this.thisSession.server_item.getProperty("serverType", "ftp") + ":" + this.thisSession.uiSG("user_number") + ":" + this.thisSession.uiSG("user_name") + ":" + this.thisSession.uiSG("user_ip") + "] " + this.thisSession.SG("WROTE") + ": *" + LOC.G("Attempting login based on client certificate username: $0", certUsername.trim()) + "*", "USER");
                    }
                    boolean good = false;
                    String tmp = this.thisSession.uiSG("dont_write");
                    this.thisSession.uiPUT("dont_write", "true");
                    boolean anypass = false;
                    if (this.thisSession.uiSG("user_name").startsWith("NOLOGIN_") && needClientAuth && this.thisSession.uiBG("secure")) {
                        good = false;
                    } else {
                        anypass = needClientAuth && this.thisSession.uiBG("secure");
                        good = this.thisSession.login_user_pass(anypass, true);
                        if (this.thisSession.uiSG("lastProxyError").equals("CHALLENGE_OTP:OTP invalid.")) {
                            this.thisSession.not_done = false;
                        }
                    }
                    if (!good && needClientAuth && this.thisSession.uiBG("secure")) {
                        if (!good) {
                            anypass = false;
                            if (com.crushftp.client.Common.dmz_mode) {
                                this.thisSession.uiPUT("current_password", com.crushftp.client.Common.System2.getProperty("crushftp.proxy.anyPassToken", ""));
                            }
                            if (!this.thisSession.uiSG("lastProxyError").equals("CHALLENGE_OTP:OTP invalid.")) {
                                good = this.thisSession.login_user_pass(anypass, true);
                            } else {
                                this.thisSession.not_done = false;
                            }
                        }
                        if (!good) {
                            this.thisSession.uiPUT("user_name", originalUsername);
                            this.thisSession.uiPUT("current_password", this.thisSession.uiSG("the_command_data"));
                            if (!this.thisSession.uiSG("lastProxyError").equals("CHALLENGE_OTP:OTP invalid.")) {
                                good = this.thisSession.login_user_pass(anypass, true);
                            } else {
                                this.thisSession.not_done = false;
                            }
                        }
                        if (!good) {
                            if (com.crushftp.client.Common.dmz_mode) {
                                this.thisSession.uiPUT("current_password", com.crushftp.client.Common.System2.getProperty("crushftp.proxy.anyPassToken", ""));
                            }
                            if (!this.thisSession.uiSG("lastProxyError").equals("CHALLENGE_OTP:OTP invalid.")) {
                                good = this.thisSession.login_user_pass(anypass, true);
                            } else {
                                this.thisSession.not_done = false;
                            }
                        }
                    }
                    this.thisSession.uiPUT("dont_write", tmp);
                    if (!good) {
                        if (this.thisSession.uVFS != null) {
                            this.thisSession.uVFS.disconnect();
                        }
                        if (!this.thisSession.uiSG("lastProxyError").equals("CHALLENGE_OTP:OTP invalid.")) {
                            if (com.crushftp.client.Common.dmz_mode) {
                                if (System.getProperty("crushftp.webapplication.enabled", "false").equals("false") && !this.thisSession.uiSG("lastProxyError").contains("WebApplication_Auth_Failed")) {
                                    good = this.thisSession.login_user_pass(anypass, true);
                                } else {
                                    this.thisSession.not_done = false;
                                }
                            } else {
                                good = this.thisSession.login_user_pass(anypass, true);
                            }
                        }
                    } else {
                        this.thisSession.ftp_write_command("230", this.thisSession.uiSG("last_login_message"));
                        if (this.thisSession.uiBG("secure")) {
                            String suite = ((SSLSocket)this.sock).getSession().getCipherSuite();
                            this.thisSession.uiPUT("user_cipher1", suite);
                            this.thisSession.uiPUT("user_cipher2", suite);
                            this.thisSession.add_log("SSL/TLS : Cipher used:" + suite, "ACCEPT");
                        }
                    }
                    this.thisSession.logLogin(good, "");
                    this.this_thread.setName(String.valueOf(this.thisSession.uiSG("user_name")) + ":(" + this.thisSession.uiSG("user_number") + ")-" + this.thisSession.uiSG("user_ip") + " (control)");
                    if (good) {
                        this.thisSession.stop_idle_timer();
                        this.thisSession.start_idle_timer();
                        Properties p = this.thisSession.do_event5("LOGIN", null);
                        if (p != null && p.containsKey("allowLogin") && p.getProperty("allowLogin", "true").equals("false")) {
                            good = false;
                            this.thisSession.logLogin(false, "Plugin rejected the login.");
                            this.thisSession.add_log_formatted("A plugin rejected the login. Login failed.", "USER");
                            this.thisSession.not_done = this.write_command("550", LOC.G("A plugin rejected the login. Login failed.") + "\r\n");
                            this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                            this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                        }
                    }
                } else if (this.thisSession.uiSG("the_command").equals("AUTH") && this.thisSession.uiSG("the_command_data").length() > 0) {
                    try {
                        this.thisSession.uiPUT("secureType", this.thisSession.uiSG("the_command_data").toUpperCase().trim());
                        if (this.thisSession.uiBG("explicit_ssl") && this.thisSession.uiSG("secureType").indexOf("SSL") >= 0) {
                            this.thisSession.uiPUT("secureType", "SSL");
                        } else if (this.thisSession.uiBG("explicit_tls") && this.thisSession.uiSG("secureType").indexOf("TLS") >= 0) {
                            this.thisSession.uiPUT("secureType", "TLS");
                        } else {
                            throw new Exception(LOC.G("Invalid Encryption type") + " : " + this.thisSession.uiSG("secureType"));
                        }
                        SSLSocket ss = null;
                        String keystore = SSLKeyManager.loadKeyStoreToMemory(ServerStatus.SG("cert_path"));
                        String certPass = ServerStatus.SG("globalKeystoreCertPass");
                        String keystorePass = ServerStatus.SG("globalKeystorePass");
                        if (!this.thisSession.server_item.getProperty("customKeystore", "").equals("")) {
                            keystore = SSLKeyManager.loadKeyStoreToMemory(this.thisSession.server_item.getProperty("customKeystore"));
                            certPass = this.thisSession.server_item.getProperty("customKeystoreCertPass", "");
                            keystorePass = this.thisSession.server_item.getProperty("customKeystorePass", "");
                        }
                        boolean needClientAuth = ServerStatus.BG("needClientAuth") || this.thisSession.server_item.getProperty("needClientAuth", "").equals("true") || this.thisSession.user != null && this.thisSession.user.getProperty("needClientAuth", "false").equals("true");
                        this.thisSession.not_done = this.write_command("234", LOC.G("Changing to secure mode..."));
                        this.ssl_context = ServerStatus.thisObj.common_code.getSSLContext(keystore, String.valueOf(keystore) + "_trust", keystorePass, certPass, this.thisSession.uiSG("secureType"), needClientAuth, true);
                        this.factory = this.ssl_context.getSocketFactory();
                        this.sockOriginal = this.sock;
                        this.sni_keystore_used.setLength(0);
                        ss = this.thisSession.server_item.getProperty("sni_enabled", "false").equals("true") ? (SSLSocket)TCPServer.doSni(this.sock, keystore, keystorePass, certPass, needClientAuth, this.sni_keystore_used) : (SSLSocket)this.factory.createSocket(this.sock, this.thisSession.uiSG("port_remote_ip"), this.thisSession.uiIG("port_remote_port"), false);
                        if (this.sni_keystore_used.length() > 0) {
                            Log.log("FTP_SERVER", 0, "Reusing SSL keystore:" + this.sni_keystore_used);
                        }
                        this.sock = ss;
                        Common.configureSSLTLSSocket(this.sock);
                        Common.setEnabledCiphers(ServerStatus.SG("disabled_ciphers"), (SSLSocket)this.sock, null);
                        ss.setNeedClientAuth(needClientAuth);
                        ss.setUseClientMode(this.thisSession.uiBG("sscn_mode"));
                        this.is = new BufferedReader(new InputStreamReader(this.sock.getInputStream(), this.thisSession.SG("char_encoding")));
                        this.os = this.sock.getOutputStream();
                        this.thisSession.uiPUT("secure", "true");
                        if (this.thisSession.server_item.getProperty("require_secure", "false").equals("true")) {
                            this.thisSession.uiPUT("dataSecure", "true");
                        }
                        this.thisSession.uiPUT("user_protocol", "FTPS");
                    }
                    catch (Exception e) {
                        this.thisSession.add_log("SSL/TLS : " + LOC.G("Negotiation Failed."), "ACCEPT");
                        this.thisSession.add_log("SSL/TLS : " + LOC.G("Have you setup a certificate in the CrushFTP advanced prefs under the SSL tab?"), "ACCEPT");
                        this.thisSession.add_log("SSL/TLS : " + e.toString(), "ACCEPT");
                        Log.log("FTP_SERVER", 1, e);
                        this.thisSession.not_done = this.write_command("550", LOC.G("Server not configured for encryption."));
                        this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                    }
                } else if (this.thisSession.uiSG("the_command").equals("CCC")) {
                    if (!this.thisSession.uiBG("secure")) {
                        this.thisSession.not_done = this.write_command("550", LOC.G("Channel not encrypted."));
                        this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                    } else {
                        this.thisSession.not_done = this.write_command("200", LOC.G("CCC command OK. Disabling encryption on control channel."));
                        this.thisSession.uiPUT("secure", "false");
                        if (this.sockOriginal != null) {
                            int originalTimeout = this.sock.getSoTimeout();
                            this.sock.setSoTimeout(1000);
                            if (System.getProperty("crushftp.allow_ccc_ssl_close", "true").equals("true")) {
                                Thread t = new Thread(new Runnable(){

                                    @Override
                                    public void run() {
                                        try {
                                            ServerSessionFTP.this.sock.setSoTimeout(5000);
                                            ServerSessionFTP.this.sock.close();
                                        }
                                        catch (Exception exception) {
                                            // empty catch block
                                        }
                                    }
                                });
                                t.start();
                                Thread.sleep(100L);
                                t.join(5000L);
                                try {
                                    t.interrupt();
                                }
                                catch (Exception certPass) {
                                    // empty catch block
                                }
                            }
                            this.sockOriginal.setSoTimeout(originalTimeout);
                            this.sock = this.sockOriginal;
                            this.is = new BufferedReader(new InputStreamReader(this.sockOriginal.getInputStream(), this.thisSession.SG("char_encoding")));
                            this.os = this.sockOriginal.getOutputStream();
                        }
                    }
                } else if (this.thisSession.uiSG("the_command").equals("PROT") && this.thisSession.uiSG("the_command_data").length() > 0) {
                    if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("P")) {
                        this.thisSession.uiPUT("dataSecure", "true");
                        this.thisSession.not_done = this.write_command("200", LOC.G("PROT command OK. Using $0 data connection", this.thisSession.uiBG("dataSecure") ? "secure" : "clear") + ".");
                    } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("C")) {
                        if (this.thisSession.uiBG("secure") && this.thisSession.server_item.getProperty("require_secure", "false").equals("true")) {
                            this.thisSession.not_done = this.write_command("550", LOC.G("Encryption is required."));
                            this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                        } else {
                            this.thisSession.uiPUT("dataSecure", "false");
                            this.thisSession.not_done = this.write_command("200", LOC.G("PROT command OK. Using $0 data connection", this.thisSession.uiBG("dataSecure") ? "secure" : "clear") + ".");
                        }
                    }
                } else if (this.thisSession.uiSG("the_command").equals("PBSZ")) {
                    int bLevel = 0;
                    if (!this.thisSession.uiSG("the_command_data").trim().equals("")) {
                        bLevel = Integer.parseInt(this.thisSession.uiSG("the_command_data").trim());
                    }
                    this.thisSession.not_done = this.write_command("200", LOC.G("PBSZ command OK.  Using buffer size set to $0", String.valueOf(bLevel)) + ".");
                } else if (this.thisSession.uiSG("the_command").equals("SYST")) {
                    this.thisSession.stop_idle_timer();
                    this.thisSession.not_done = this.thisSession.BG("dos_ftp_listing") ? this.write_command("215", "Windows_NT") : this.write_command("215", LOC.G("%SYST%"));
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("SSCN")) {
                    this.thisSession.stop_idle_timer();
                    if (this.thisSession.uiSG("the_command_data").toUpperCase().equals("ON")) {
                        this.thisSession.uiPUT("sscn_mode", "true");
                    } else if (this.thisSession.uiSG("the_command_data").toUpperCase().equals("OFF")) {
                        this.thisSession.uiPUT("sscn_mode", "false");
                    }
                    this.thisSession.not_done = this.write_command("200", "SSCN:" + this.thisSession.uiSG("the_command_data"));
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiSG("the_command").equals("FEAT")) {
                    this.thisSession.stop_idle_timer();
                    this.thisSession.not_done = this.write_command("211-" + LOC.G("Extensions supported:"));
                    if (this.thisSession.uiBG("explicit_tls")) {
                        this.thisSession.not_done = this.write_command_raw(" AUTH TLS");
                    }
                    if (this.thisSession.uiBG("explicit_ssl")) {
                        this.thisSession.not_done = this.write_command_raw(" AUTH SSL");
                    }
                    this.thisSession.not_done = this.write_command_raw(" SSCN");
                    this.thisSession.not_done = this.write_command_raw(" PBSZ");
                    this.thisSession.not_done = this.write_command_raw(" PROT");
                    this.thisSession.not_done = this.write_command_raw(" CCC");
                    this.thisSession.not_done = this.write_command_raw(" CLNT");
                    if (ServerStatus.BG("epsveprt")) {
                        this.thisSession.not_done = this.write_command_raw(" EPSV");
                        this.thisSession.not_done = this.write_command_raw(" EPRT");
                    }
                    this.thisSession.not_done = this.write_command_raw(" MDTM");
                    this.thisSession.not_done = this.write_command_raw(" MDTM YYYYMMDDHHMMSS[+-TZ];filename");
                    this.thisSession.not_done = this.write_command_raw(" MFMT");
                    this.thisSession.not_done = this.write_command_raw(" SIZE");
                    this.thisSession.not_done = this.write_command_raw(" REST STREAM");
                    this.thisSession.not_done = this.write_command_raw(" MODE Z");
                    this.thisSession.not_done = this.write_command_raw(" LIST -Q");
                    this.thisSession.not_done = this.write_command_raw(" SITE UTIME");
                    this.thisSession.not_done = this.write_command_raw(" SITE MD5");
                    this.thisSession.not_done = this.write_command_raw(" XMD5");
                    this.thisSession.not_done = this.write_command_raw(" SITE MD5s");
                    this.thisSession.not_done = this.write_command_raw(" SITE RANDOMACCESS");
                    if (ServerStatus.BG("allow_mlst")) {
                        this.thisSession.not_done = this.write_command_raw(" MLST " + this.thisSession.uiSG("mlst_format"));
                    }
                    this.thisSession.not_done = this.write_command_raw(" " + Common.replace_str(this.thisSession.SG("char_encoding"), "UTF-8", "UTF8"));
                    this.thisSession.not_done = this.write_command("211 END");
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("OPTS")) {
                    this.thisSession.stop_idle_timer();
                    if (this.thisSession.uiSG("the_command_data").toUpperCase().indexOf("LEVEL ") >= 0) {
                        this.thisSession.uiPUT("zlibLevel", String.valueOf(Integer.parseInt(this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").lastIndexOf(" ")).trim())));
                        this.thisSession.not_done = this.write_command("200 " + LOC.G("Level set to $0", this.thisSession.uiSG("zlibLevel")) + ".");
                    } else if (this.thisSession.uiSG("the_command_data").toUpperCase().indexOf("UTF8 ON") >= 0 || this.thisSession.uiSG("the_command_data").toUpperCase().indexOf("UTF-8 ON") >= 0) {
                        this.thisSession.user.put("char_encoding", "UTF8");
                        this.thisSession.not_done = this.write_command("200 UTF8 OPTS ON.");
                    } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("MLST")) {
                        String new_mlst = "";
                        String[] mlst_format = this.thisSession.uiSG("the_command_data").substring(5).trim().split(";");
                        int x = 0;
                        while (x < mlst_format.length) {
                            if (!mlst_format[x].trim().equals("") && mlst_format[x].trim().indexOf(" ") < 0 && "Type*;Size*;Modify*;Perm*;UNIX.owner*;UNIX.group*;".toUpperCase().indexOf(mlst_format[x].trim().toUpperCase()) >= 0) {
                                if (!(new_mlst = String.valueOf(new_mlst) + mlst_format[x]).endsWith("*")) {
                                    new_mlst = String.valueOf(new_mlst) + "*";
                                }
                                new_mlst = String.valueOf(new_mlst) + ";";
                            }
                            ++x;
                        }
                        this.thisSession.uiPUT("mlst_format", new_mlst);
                        this.thisSession.not_done = this.write_command("200 OPTS " + this.thisSession.uiSG("the_command_data"));
                    } else {
                        this.thisSession.not_done = this.write_command("502", LOC.G("Unknown OPS format."));
                    }
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("MODE") && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("Z")) {
                        this.thisSession.not_done = this.write_command("200", "MODE Z ok");
                        this.thisSession.uiPUT("modez", "true");
                    } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("S")) {
                        this.thisSession.not_done = this.write_command("200", "MODE S ok");
                        this.thisSession.uiPUT("modez", "false");
                    } else {
                        this.thisSession.not_done = this.write_command("502", LOC.G("%MODE%"));
                    }
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiSG("the_command").equals("CLNT") && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    this.thisSession.not_done = this.write_command("200", LOC.G("Noted."));
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && (this.thisSession.uiSG("the_command").equals("NOOP") || this.thisSession.uiSG("the_command").equals("ALLO"))) {
                    String the_dir = this.thisSession.uiSG("current_dir");
                    String parentPath = this.thisSession.uVFS.getRootVFS(the_dir, -1);
                    Properties dir_item = this.thisSession.uVFS.get_item(parentPath, -1);
                    if (dir_item.getProperty("protocol", "file").equalsIgnoreCase("FTP")) {
                        GenericClient c = this.thisSession.uVFS.getClient(dir_item);
                        try {
                            String response = c.doCommand("NOOP");
                            this.thisSession.not_done = this.write_command(response);
                        }
                        finally {
                            c = this.thisSession.uVFS.releaseClient(c);
                        }
                    } else {
                        this.thisSession.not_done = this.write_command("200", LOC.G("%NOOP%"));
                    }
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("SIZE") && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    this.thisSession.do_SIZE();
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && (this.thisSession.uiSG("the_command").equals("MDTM") || this.thisSession.uiSG("the_command").equals("MFMT"))) {
                    this.thisSession.stop_idle_timer();
                    this.thisSession.do_MDTM();
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("RNFR") && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    this.thisSession.do_RNFR(this.thisSession.fixupDir(null));
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("RNTO") && this.thisSession.uiSG("the_command_data").length() > 0 && this.thisSession.rnfr_file_path != null) {
                    this.thisSession.stop_idle_timer();
                    boolean rnto_overwrite = ServerStatus.BG("rnto_overwrite");
                    if (!this.thisSession.SG("rnto_overwrite").equals("")) {
                        rnto_overwrite = this.thisSession.BG("rnto_overwrite");
                    }
                    this.thisSession.do_RNTO(rnto_overwrite, this.thisSession.rnfr_file_path, this.thisSession.fixupDir(null));
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && (this.thisSession.uiSG("the_command").equals("PWD") || this.thisSession.uiSG("the_command").equals("XPWD"))) {
                    this.thisSession.stop_idle_timer();
                    String pwd = this.thisSession.get_PWD();
                    this.thisSession.not_done = this.write_command("257", LOC.G("\"$0\" PWD command successful.", ServerStatus.thisObj.strip_variables(pwd, this.thisSession)));
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("TYPE") && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("A")) {
                        this.thisSession.not_done = this.write_command("200", LOC.G("%TYPE-ascii%"));
                        this.thisSession.uiPUT("file_transfer_mode", "ASCII");
                    } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("I")) {
                        this.thisSession.not_done = this.write_command("200", LOC.G("%TYPE-binary%"));
                        this.thisSession.uiPUT("file_transfer_mode", "BINARY");
                    } else {
                        this.thisSession.not_done = this.write_command("504", LOC.G("Mode not supported."));
                    }
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("STRU") && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    this.thisSession.not_done = this.thisSession.uiSG("the_command_data").startsWith("F") ? this.write_command("200", LOC.G("Accepted")) : this.write_command("550", LOC.G("Not Supported."));
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("REST") && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    this.thisSession.uiPUT("start_resume_loc", String.valueOf(Long.parseLong(this.thisSession.uiSG("the_command_data"))));
                    this.thisSession.not_done = this.write_command("350", LOC.G("%REST%"));
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("DELE") && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    this.thisSession.do_DELE(false, null);
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && (this.thisSession.uiSG("the_command").equals("MKD") || this.thisSession.uiSG("the_command").equals("XMKD")) && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    this.thisSession.do_MKD(true, null);
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && (this.thisSession.uiSG("the_command").equals("RMD") || this.thisSession.uiSG("the_command").equals("XRMD")) && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    this.thisSession.do_RMD(this.thisSession.uiSG("current_dir"));
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").startsWith("ABOR")) {
                    this.thisSession.stop_idle_timer();
                    if (this.thisSession.uiBG("sending_file")) {
                        this.thisSession.kill_stor_files(this.thisSession.stor_files_pool_free);
                        this.thisSession.kill_stor_files(this.thisSession.stor_files_pool_used);
                    } else if (this.thisSession.uiBG("receiving_file")) {
                        this.thisSession.kill_retr_files(this.thisSession.retr_files_pool_free);
                        this.thisSession.kill_retr_files(this.thisSession.retr_files_pool_used);
                    } else if (this.thisSession.uiBG("listing_files")) {
                        this.list_files.thisThread.interrupt();
                    }
                    Thread.sleep(1000L);
                    this.thisSession.not_done = this.write_command("225", LOC.G("%ABOR%"));
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("RETR") && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    String the_dir = this.thisSession.uiSG("current_dir");
                    if (!this.thisSession.uiSG("the_command_data").equals("")) {
                        the_dir = this.thisSession.uiSG("the_command_data").startsWith("/") ? this.thisSession.uiSG("the_command_data") : String.valueOf(the_dir) + this.thisSession.uiSG("the_command_data");
                        if ((the_dir = com.crushftp.client.Common.dots(the_dir)).equals("/")) {
                            the_dir = this.thisSession.SG("root_dir");
                        }
                        if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSession.SG("root_dir").toUpperCase())) {
                            the_dir = String.valueOf(this.thisSession.SG("root_dir")) + the_dir.substring(1);
                        }
                    }
                    String other_the_dir = the_dir;
                    String encode_on_fly = "";
                    if (the_dir.toUpperCase().endsWith(".BIN") || the_dir.toUpperCase().endsWith(".ZIP")) {
                        other_the_dir = the_dir.substring(0, the_dir.length() - 4);
                    }
                    if (the_dir.endsWith("/")) {
                        if (this.thisSession.data_socks.size() > 0) {
                            ((Socket)this.thisSession.data_socks.remove(0)).close();
                        }
                        this.thisSession.not_done = this.write_command("550", LOC.G("%RETR-wrong%"));
                        this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                    } else {
                        RETR_handler retr_files = null;
                        String parentPath = this.thisSession.uVFS.getRootVFS(the_dir, -1);
                        Properties dir_item = this.thisSession.uVFS.get_item(parentPath, -1);
                        Properties item = this.thisSession.uVFS.get_fake_item(the_dir, "FILE");
                        boolean allowed = this.thisSession.check_access_privs(the_dir, this.thisSession.uiSG("the_command"), item);
                        if ((allowed || this.thisSession.check_access_privs(other_the_dir, this.thisSession.uiSG("the_command"))) && Common.filter_check("D", Common.last(the_dir), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter")) && Common.filter_check("F", Common.last(the_dir), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter"))) {
                            if (ServerStatus.BG("binary_mode")) {
                                this.thisSession.uiPUT("file_transfer_mode", "BINARY");
                            }
                            if (!allowed) {
                                encode_on_fly = "BIN";
                                item = this.thisSession.uVFS.get_item(other_the_dir);
                                if (item != null) {
                                    if (the_dir.toUpperCase().endsWith(".ZIP")) {
                                        encode_on_fly = "ZIP";
                                        item.put("url", String.valueOf(item.getProperty("url")) + ".zip");
                                        while (this.thisSession.retr_files_pool_free.size() == 0) {
                                            Thread.sleep(100L);
                                        }
                                        retr_files = (RETR_handler)this.thisSession.retr_files_pool_free.remove(0);
                                        this.thisSession.retr_files_pool_used.addElement(retr_files);
                                        Common.startMultiThreadZipper(this.thisSession.uVFS, retr_files, other_the_dir, 2000, !item.getProperty("url").toUpperCase().startsWith("FILE:/"), new Vector());
                                    }
                                    the_dir = other_the_dir;
                                }
                            }
                            boolean connected = false;
                            boolean pasv_conn = this.thisSession.uiBG("pasv_connect");
                            try {
                                this.do_port_connect();
                                connected = pasv_conn || this.thisSession.data_socks.size() > 0;
                            }
                            catch (Exception e) {
                                Log.log("FTP_SERVER", 1, e);
                            }
                            if (the_dir.indexOf(":filetree") >= 0) {
                                item = this.thisSession.uVFS.get_item(the_dir.substring(0, the_dir.indexOf(":filetree")));
                            }
                            if (!connected) {
                                if (this.thisSession.data_socks.size() > 0) {
                                    ((Socket)this.thisSession.data_socks.remove(0)).close();
                                }
                                this.thisSession.not_done = this.write_command("550", LOC.G("File not found, access denied, or port issue encountered."));
                                this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                                this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                                this.thisSession.start_idle_timer();
                            } else if (item != null && item.getProperty("type", "").equals("DIR") && !encode_on_fly.equals("ZIP") && the_dir.indexOf(":filetree") < 0 || item == null) {
                                if (this.thisSession.data_socks.size() > 0) {
                                    ((Socket)this.thisSession.data_socks.remove(0)).close();
                                }
                                this.thisSession.not_done = ServerStatus.BG("generic_ftp_responses") ? this.write_command("550", LOC.G("Failed to open file.")) : this.write_command("550", LOC.G("%RETR-bad%"));
                                this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                                this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                                this.thisSession.start_idle_timer();
                            } else {
                                boolean allowTransfer = true;
                                Properties p = new Properties();
                                p.put("the_dir", the_dir);
                                p.put("allowTransfer", "true");
                                p.put("transferType", this.thisSession.uiSG("the_command"));
                                this.thisSession.runPlugin("transfer_path", p);
                                the_dir = p.getProperty("the_dir", the_dir);
                                allowTransfer = p.getProperty("allowTransfer", "true").equals("true");
                                if (allowTransfer) {
                                    if (retr_files == null) {
                                        int x = 0;
                                        while (x < 500 && this.thisSession.retr_files_pool_free.size() == 0) {
                                            Thread.sleep(x);
                                            ++x;
                                        }
                                        retr_files = (RETR_handler)this.thisSession.retr_files_pool_free.remove(0);
                                        this.thisSession.retr_files_pool_used.addElement(retr_files);
                                    }
                                    retr_files.init_vars(the_dir, this.thisSession.uiLG("start_resume_loc"), -1L, this.thisSession, item, this.thisSession.uiBG("pasv_connect"), encode_on_fly, null, null);
                                    Worker.startWorker(retr_files, String.valueOf(this.thisSession.uiSG("user_name")) + ":(" + this.thisSession.uiSG("user_number") + ")-" + this.thisSession.uiSG("user_ip") + " (retr_handler)");
                                    this.thisSession.uiVG("failed_commands").removeAllElements();
                                    this.thisSession.uiPUT("start_resume_loc", "0");
                                    int loops = 0;
                                    while (loops++ < 10000 && (retr_files.active2.getProperty("streamOpenStatus", "").equals("STOPPED") || retr_files.active2.getProperty("streamOpenStatus", "").equals("PENDING"))) {
                                        Thread.sleep(1L);
                                    }
                                } else {
                                    if (this.thisSession.data_socks.size() > 0) {
                                        ((Socket)this.thisSession.data_socks.remove(0)).close();
                                    }
                                    this.thisSession.not_done = this.write_command("550", LOC.G("%RETR-bad% (plugin blocked)"));
                                    this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                                    this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                                    this.thisSession.start_idle_timer();
                                }
                            }
                        } else {
                            if (this.thisSession.data_socks.size() > 0) {
                                ((Socket)this.thisSession.data_socks.remove(0)).close();
                            }
                            this.thisSession.not_done = this.write_command("550", LOC.G("File not found, access denied."));
                            this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                            this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                            this.thisSession.start_idle_timer();
                        }
                    }
                } else if (this.thisSession.uiBG("user_logged_in") && (this.thisSession.uiSG("the_command").equals("STOR") || this.thisSession.uiSG("the_command").equals("APPE") || this.thisSession.uiSG("the_command").equals("STOU")) && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    this.thisSession.uiPUT("current_dir", this.thisSession.uiSG("current_dir").replace(':', '_'));
                    String the_dir = this.thisSession.uiSG("current_dir");
                    Log.log("FTP_SERVER", 3, "STOR the_dir1:" + the_dir);
                    if (!this.thisSession.uiSG("the_command_data").equals("")) {
                        the_dir = this.thisSession.uiSG("the_command_data").startsWith("/") ? this.thisSession.uiSG("the_command_data") : String.valueOf(the_dir) + this.thisSession.uiSG("the_command_data");
                        if ((the_dir = com.crushftp.client.Common.dots(the_dir)).equals("/")) {
                            the_dir = this.thisSession.SG("root_dir");
                        }
                        if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSession.SG("root_dir").toUpperCase())) {
                            the_dir = String.valueOf(this.thisSession.SG("root_dir")) + the_dir.substring(1);
                        }
                    }
                    Log.log("FTP_SERVER", 3, "STOR the_dir1:" + the_dir);
                    Properties item = this.thisSession.uVFS.get_item(the_dir);
                    Log.log("FTP_SERVER", 3, "item returned for STOR lookup of item:" + item);
                    if (item == null && (item = this.thisSession.uVFS.get_item_parent(the_dir)) != null) {
                        item.put("type", "FILE");
                    }
                    if (this.thisSession.check_access_privs(the_dir, this.thisSession.uiSG("the_command"), item) && Common.filter_check("U", Common.last(the_dir), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter")) && item.getProperty("type").equalsIgnoreCase("FILE") && Common.filter_check("F", Common.last(the_dir), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter"))) {
                        if (ServerStatus.BG("make_dir_uploads")) {
                            String tmp = this.thisSession.uiSG("dont_write");
                            this.thisSession.uiPUT("dont_write", "true");
                            this.thisSession.do_MKD(true, Common.all_but_last(the_dir));
                            this.thisSession.uiPUT("dont_write", tmp);
                        }
                        String realAction = this.thisSession.uiSG("the_command");
                        if (this.thisSession.uiSG("the_command").equals("APPE")) {
                            Properties item2 = this.thisSession.uVFS.get_item(the_dir);
                            try {
                                if (item2 != null) {
                                    this.thisSession.uiPUT("start_resume_loc", String.valueOf(Long.parseLong(item2.getProperty("size"))));
                                } else {
                                    this.thisSession.uiPUT("start_resume_loc", "0");
                                }
                            }
                            catch (Exception parentPath) {
                                // empty catch block
                            }
                        }
                        if (ServerStatus.BG("binary_mode_stor")) {
                            this.thisSession.uiPUT("file_transfer_mode", "BINARY");
                        }
                        long quota = this.thisSession.get_quota(the_dir);
                        this.do_port_connect();
                        if (quota < 0L && quota != -12345L) {
                            if (this.thisSession.data_socks.size() > 0) {
                                ((Socket)this.thisSession.data_socks.remove(0)).close();
                            }
                            this.thisSession.not_done = this.write_command("550-" + LOC.G("Your quota has been exceeded") + ".  " + LOC.G("Available") + ": " + quota + "k.");
                            this.thisSession.not_done = this.write_command("550", LOC.G("%STOR-quota exceeded%"));
                            this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                            this.thisSession.start_idle_timer();
                        } else {
                            boolean allowTransfer = true;
                            Properties p = new Properties();
                            p.put("the_dir", the_dir);
                            p.put("allowTransfer", "true");
                            p.put("transferType", this.thisSession.uiSG("the_command"));
                            p.put("pluginError", LOC.G("%STOR-bad% (plugin blocked)"));
                            this.thisSession.runPlugin("transfer_path", p);
                            String pluginMsg = p.getProperty("pluginError");
                            the_dir = p.getProperty("the_dir", the_dir);
                            allowTransfer = p.getProperty("allowTransfer", "true").equals("true");
                            if (allowTransfer) {
                                int x = 0;
                                while (x < 500 && this.thisSession.stor_files_pool_free.size() == 0) {
                                    Thread.sleep(x);
                                    ++x;
                                }
                                STOR_handler stor_files = (STOR_handler)this.thisSession.stor_files_pool_free.remove(0);
                                this.thisSession.stor_files_pool_used.addElement(stor_files);
                                stor_files.init_vars(the_dir, this.thisSession.uiLG("start_resume_loc"), this.thisSession, item, realAction, this.thisSession.uiSG("the_command").equals("STOU"), this.thisSession.uiBG("randomaccess"), null, null);
                                Worker.startWorker(stor_files, String.valueOf(this.thisSession.uiSG("user_name")) + ":(" + this.thisSession.uiSG("user_number") + ")-" + this.thisSession.uiSG("user_ip") + " (stor_handler)");
                                this.thisSession.uiPUT("start_resume_loc", "0");
                                this.thisSession.uiVG("failed_commands").removeAllElements();
                                int loops = 0;
                                while (loops++ < 10000 && (stor_files.active2.getProperty("streamOpenStatus", "").equals("STOPPED") || stor_files.active2.getProperty("streamOpenStatus", "").equals("PENDING"))) {
                                    Thread.sleep(1L);
                                }
                            } else {
                                if (this.thisSession.data_socks.size() > 0) {
                                    ((Socket)this.thisSession.data_socks.remove(0)).close();
                                }
                                this.thisSession.not_done = this.write_command("550", pluginMsg);
                                this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                                this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                                this.thisSession.start_idle_timer();
                            }
                        }
                    } else {
                        boolean file_filter = false;
                        if (item != null && item.getProperty("type").equalsIgnoreCase("FILE") && !Common.filter_check("F", Common.last(the_dir), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter"))) {
                            file_filter = true;
                        }
                        if (!Common.filter_check("U", Common.last(the_dir), String.valueOf(ServerStatus.SG("filename_filters_str")) + "\r\n" + this.thisSession.SG("file_filter")) || file_filter) {
                            this.thisSession.add_log_formatted("550 STOR error: Upload attempt was rejected because the block matching names! File name :" + Common.last(the_dir) + " Filters :" + ServerStatus.SG("filename_filters_str") + " " + this.thisSession.SG("file_filter"), "STOR");
                        }
                        if (this.thisSession.data_socks.size() > 0) {
                            ((Socket)this.thisSession.data_socks.remove(0)).close();
                        }
                        if (item != null && item.getProperty("type").equalsIgnoreCase("DIR")) {
                            this.thisSession.not_done = this.write_command("553", LOC.G("$0 is a directory.", this.thisSession.uiSG("the_command_data")));
                            Log.log("FTP_SERVER", 2, LOC.G("STOR Failure item:$0", item.toString()));
                        } else {
                            this.thisSession.not_done = this.write_command("550", LOC.G("%STOR-bad%"));
                        }
                        this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                        this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                        this.thisSession.start_idle_timer();
                    }
                } else if (this.thisSession.uiBG("user_logged_in") && (this.thisSession.uiSG("the_command").equals("LIST") || this.thisSession.uiSG("the_command").equals("NLST") || this.thisSession.uiSG("the_command").equals("MLSD") && ServerStatus.BG("allow_mlst"))) {
                    String the_dir;
                    this.thisSession.stop_idle_timer();
                    boolean names_only = false;
                    this.do_port_connect();
                    if (this.thisSession.uiSG("the_command_data").startsWith("-f")) {
                        names_only = true;
                    }
                    if (this.thisSession.uiSG("the_command_data").startsWith("-") && !this.thisSession.uiSG("the_command_data").startsWith("-Q") && this.thisSession.uiSG("the_command_data").indexOf(" ", this.thisSession.uiSG("the_command_data").indexOf("-")) > 0) {
                        this.thisSession.uiPUT("the_command_data", this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").indexOf(" ", this.thisSession.uiSG("the_command_data").indexOf("-")) + 1));
                    }
                    if ((the_dir = this.thisSession.uiSG("current_dir")).equals("/")) {
                        the_dir = this.thisSession.SG("root_dir");
                    }
                    if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSession.SG("root_dir").toUpperCase())) {
                        the_dir = String.valueOf(this.thisSession.SG("root_dir")) + the_dir.substring(1);
                    }
                    if (!this.thisSession.uiSG("the_command_data").equals("")) {
                        if (this.thisSession.uiSG("the_command_data").startsWith("/")) {
                            the_dir = this.thisSession.uiSG("the_command_data");
                            if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSession.SG("root_dir").toUpperCase())) {
                                the_dir = String.valueOf(this.thisSession.SG("root_dir")) + the_dir.substring(1);
                            }
                            Properties tester = this.thisSession.uVFS.get_fake_item(the_dir, "DIR");
                            if (the_dir.endsWith("*") && the_dir.startsWith("/") && ServerStatus.BG("disable_dir_filter")) {
                                the_dir = the_dir.substring(0, the_dir.length() - 1);
                                tester = this.thisSession.uVFS.get_fake_item(the_dir, "DIR");
                            }
                            if (tester == null) {
                                if (!the_dir.endsWith("/") && !ServerStatus.BG("disable_dir_filter")) {
                                    the_dir = Common.all_but_last(the_dir);
                                    this.thisSession.uiPUT("the_command_data", Common.last(this.thisSession.uiSG("the_command_data")));
                                } else if (the_dir.indexOf("*") < 0 && the_dir.indexOf("?") < 0) {
                                    this.thisSession.uiPUT("the_command_data", "");
                                    if (ServerStatus.BG("disable_dir_filter")) {
                                        the_dir = String.valueOf(the_dir) + "/";
                                    }
                                } else if (the_dir.endsWith("*") && the_dir.startsWith("/") && ServerStatus.BG("disable_dir_filter")) {
                                    this.thisSession.uiPUT("the_command_data", String.valueOf(this.thisSession.uiSG("the_command_data").substring(0, this.thisSession.uiSG("the_command_data").length() - 1)) + "/");
                                }
                            } else if (tester.getProperty("type", "").equals("DIR")) {
                                if (!the_dir.endsWith("/")) {
                                    the_dir = String.valueOf(the_dir) + "/";
                                    this.thisSession.uiPUT("the_command_data", "");
                                }
                            } else if (!the_dir.endsWith("/")) {
                                the_dir = Common.all_but_last(the_dir);
                                this.thisSession.uiPUT("the_command_data", Common.last(this.thisSession.uiSG("the_command_data")));
                            } else {
                                this.thisSession.uiPUT("the_command_data", "");
                            }
                        } else if (this.thisSession.uiSG("the_command_data").indexOf("/") >= 0 || ServerStatus.BG("disable_dir_filter")) {
                            Properties tester;
                            if ((the_dir = String.valueOf(the_dir) + this.thisSession.uiSG("the_command_data")).toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSession.SG("root_dir").toUpperCase())) {
                                the_dir = String.valueOf(this.thisSession.SG("root_dir")) + the_dir.substring(1);
                            }
                            if ((tester = this.thisSession.uVFS.get_fake_item(the_dir, "DIR")) == null) {
                                if (!the_dir.endsWith("/")) {
                                    the_dir = Common.all_but_last(the_dir);
                                    this.thisSession.uiPUT("the_command_data", Common.last(this.thisSession.uiSG("the_command_data")));
                                } else {
                                    this.thisSession.uiPUT("the_command_data", "");
                                }
                            } else if (tester.getProperty("type", "").equals("DIR")) {
                                if (!the_dir.endsWith("/")) {
                                    the_dir = String.valueOf(the_dir) + "/";
                                    this.thisSession.uiPUT("the_command_data", "");
                                }
                            } else if (!the_dir.endsWith("/")) {
                                the_dir = Common.all_but_last(the_dir);
                                this.thisSession.uiPUT("the_command_data", Common.last(this.thisSession.uiSG("the_command_data")));
                            } else {
                                this.thisSession.uiPUT("the_command_data", "");
                            }
                        }
                    }
                    if ((the_dir = com.crushftp.client.Common.dots(the_dir)).equals("/") || the_dir.equals("")) {
                        the_dir = this.thisSession.SG("root_dir");
                    }
                    if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSession.SG("root_dir").toUpperCase())) {
                        the_dir = String.valueOf(this.thisSession.SG("root_dir")) + the_dir.substring(1);
                    }
                    Properties dir_item = this.thisSession.uVFS.get_item(the_dir, -1);
                    boolean showListing = true;
                    if (!the_dir.endsWith("/")) {
                        the_dir = Common.all_but_last(the_dir);
                    }
                    if (this.list_files == null) {
                        this.list_files = new LIST_handler();
                    }
                    while (this.list_files.active) {
                        Thread.sleep(100L);
                    }
                    if (this.thisSession.uiSG("the_command").equals("NLST") && (!Common.last(this.thisSession.uiSG("the_command_data")).startsWith("-") || Common.last(this.thisSession.uiSG("the_command_data")).indexOf("l") <= 0)) {
                        names_only = true;
                    }
                    this.list_files.init_vars(the_dir, names_only, this.thisSession, Common.last(this.thisSession.uiSG("the_command_data")), showListing, this.thisSession.uiSG("the_command").equals("MLSD") && ServerStatus.BG("allow_mlst"));
                    this.thisSession.uiPUT("listing_files", "true");
                    Worker.startWorker(this.list_files, String.valueOf(this.thisSession.uiSG("user_name")) + ":(" + this.thisSession.uiSG("user_number") + ")-" + this.thisSession.uiSG("user_ip") + " (list_handler)");
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("HELP")) {
                    this.thisSession.not_done = this.write_command("214", String.valueOf(ServerStatus.thisObj.change_vars_to_values(LOC.G("%HELP-start%"), this.thisSession).trim()) + "\r\n" + LOC.G("%HELP-end%"));
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("STAT") && this.thisSession.uiSG("the_command_data").length() > 0) {
                    Properties item;
                    this.thisSession.stop_idle_timer();
                    String the_dir = this.thisSession.uiSG("current_dir");
                    if (!this.thisSession.uiSG("the_command_data").equals("")) {
                        the_dir = this.thisSession.uiSG("the_command_data").startsWith("/") ? this.thisSession.uiSG("the_command_data") : String.valueOf(the_dir) + this.thisSession.uiSG("the_command_data");
                        if ((the_dir = com.crushftp.client.Common.dots(the_dir)).equals("/")) {
                            the_dir = this.thisSession.SG("root_dir");
                        }
                        if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSession.SG("root_dir").toUpperCase())) {
                            the_dir = String.valueOf(this.thisSession.SG("root_dir")) + the_dir.substring(1);
                        }
                    }
                    boolean STAT_ok = false;
                    if (this.thisSession.check_access_privs(the_dir, this.thisSession.uiSG("the_command")) && (item = this.thisSession.uVFS.get_item(the_dir)) != null) {
                        STAT_ok = true;
                        Vector<Properties> items = new Vector<Properties>();
                        if (item.getProperty("type", "DIR").equals("DIR")) {
                            this.thisSession.uVFS.getListing(items, the_dir);
                        } else {
                            items.addElement(item);
                        }
                        StringBuffer item_str = new StringBuffer();
                        Vector<String> nameList = new Vector<String>();
                        int x = 0;
                        while (x < items.size()) {
                            Properties p = (Properties)items.elementAt(x);
                            if (LIST_handler.checkName(p, this.thisSession, false, false) && nameList.indexOf(p.getProperty("name")) < 0) {
                                LIST_handler.generateLineEntry(p, item_str, false, the_dir, false, this.thisSession, false);
                                nameList.addElement(p.getProperty("name"));
                            }
                            ++x;
                        }
                        this.thisSession.not_done = this.write_command("211-" + LOC.G("%STAT-start%"));
                        if (item_str.toString().trim().length() > 0) {
                            this.thisSession.not_done = this.write_command(item_str.toString().trim());
                        }
                        this.thisSession.not_done = this.write_command("211 " + LOC.G("%STAT-end%"));
                    }
                    if (!STAT_ok) {
                        this.thisSession.not_done = this.write_command("550", "%STAT-bad%");
                        this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                    }
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("MLST") && this.thisSession.uiSG("the_command_data").length() > 0 && ServerStatus.BG("allow_mlst")) {
                    Properties item;
                    if (ServerStatus.BG("mdtm_gmt")) {
                        this.thisSession.sdf_yyyyMMddHHmmss.setCalendar(Calendar.getInstance(new SimpleTimeZone(0, "GMT")));
                    }
                    this.thisSession.stop_idle_timer();
                    String the_dir = this.thisSession.uiSG("current_dir");
                    if (!this.thisSession.uiSG("the_command_data").equals("")) {
                        the_dir = this.thisSession.uiSG("the_command_data").startsWith("/") ? this.thisSession.uiSG("the_command_data") : String.valueOf(the_dir) + this.thisSession.uiSG("the_command_data");
                        if ((the_dir = com.crushftp.client.Common.dots(the_dir)).equals("/")) {
                            the_dir = this.thisSession.SG("root_dir");
                        }
                        if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSession.SG("root_dir").toUpperCase())) {
                            the_dir = String.valueOf(this.thisSession.SG("root_dir")) + the_dir.substring(1);
                        }
                    }
                    boolean STAT_ok = false;
                    if (this.thisSession.check_access_privs(the_dir, this.thisSession.uiSG("the_command")) && (item = this.thisSession.uVFS.get_item(the_dir)) != null) {
                        STAT_ok = true;
                        Vector<Properties> items = new Vector<Properties>();
                        items.addElement(item);
                        StringBuffer item_str = new StringBuffer();
                        int x = 0;
                        while (x < items.size()) {
                            Properties p = (Properties)items.elementAt(x);
                            if (LIST_handler.checkName(p, this.thisSession, false, false)) {
                                LIST_handler.generateLineEntry(p, item_str, false, the_dir, false, this.thisSession, true);
                            }
                            ++x;
                        }
                        this.thisSession.not_done = this.write_command("250-" + LOC.G("Listing") + " " + this.thisSession.uiSG("the_command_data"));
                        if (item_str.toString().trim().length() > 0) {
                            this.thisSession.not_done = this.write_command(item_str.toString().trim());
                        }
                        this.thisSession.not_done = this.write_command("250 " + LOC.G("End"));
                    }
                    if (!STAT_ok) {
                        this.thisSession.not_done = this.write_command("550", LOC.G("%STAT-bad%"));
                        this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                    }
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && (this.thisSession.uiSG("the_command").equals("CWD") || this.thisSession.uiSG("the_command").equals("XCWD"))) {
                    this.thisSession.stop_idle_timer();
                    this.thisSession.do_CWD();
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("CDUP")) {
                    this.thisSession.stop_idle_timer();
                    this.thisSession.uiPUT("the_command_data", "..");
                    this.thisSession.do_CWD();
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && (this.thisSession.uiSG("the_command").equals("PASV") || this.thisSession.uiSG("the_command").equals("EPSV") && ServerStatus.BG("epsveprt"))) {
                    block343: {
                        this.thisSession.stop_idle_timer();
                        try {
                            Properties pasv_stuff = this.do_pasv_command();
                            while (this.thisSession.pasv_socks.size() > 0) {
                                ((ServerSocket)this.thisSession.pasv_socks.remove(0)).close();
                            }
                            final ServerSocket pasv_sock2 = (ServerSocket)pasv_stuff.remove("server_socket");
                            this.thisSession.uiPUT("PASV_port", String.valueOf(Integer.parseInt(pasv_stuff.getProperty("port"))));
                            Worker.startWorker(new Runnable(){

                                @Override
                                public void run() {
                                    try {
                                        try {
                                            Thread.currentThread().setName(String.valueOf(ServerSessionFTP.this.this_thread.getName()) + " (PASV " + pasv_sock2.getLocalPort() + ")");
                                            pasv_sock2.setSoTimeout(20000);
                                            ServerSessionFTP.this.thisSession.pasv_socks.addElement(pasv_sock2);
                                            Socket tempSock = pasv_sock2.accept();
                                            tempSock.setTcpNoDelay(System.getProperty("crushftp.ftp.tcp_no_delay", "false").equals("true"));
                                            pasv_sock2.close();
                                            if (ServerSessionFTP.this.thisSession.uiBG("dataSecure") && ServerSessionFTP.this.thisSession.uiBG("sni_enabled") && ServerSessionFTP.this.sni_keystore_used.length() == 0) {
                                                String keystore = SSLKeyManager.loadKeyStoreToMemory(ServerStatus.SG("cert_path"));
                                                String certPass = ServerStatus.SG("globalKeystoreCertPass");
                                                String keystorePass = ServerStatus.SG("globalKeystorePass");
                                                if (!ServerSessionFTP.this.thisSession.server_item.getProperty("customKeystore", "").equals("")) {
                                                    keystore = SSLKeyManager.loadKeyStoreToMemory(ServerSessionFTP.this.thisSession.server_item.getProperty("customKeystore", ""));
                                                    certPass = ServerSessionFTP.this.thisSession.server_item.getProperty("customKeystoreCertPass", "");
                                                    keystorePass = ServerSessionFTP.this.thisSession.server_item.getProperty("customKeystorePass", "");
                                                }
                                                boolean needClientAuth = ServerStatus.BG("needClientAuth") || ServerSessionFTP.this.thisSession.server_item.getProperty("needClientAuth", "").equals("true") || ServerSessionFTP.this.thisSession.user != null && ServerSessionFTP.this.thisSession.user.getProperty("needClientAuth", "false").equals("true");
                                                tempSock = TCPServer.doSni(tempSock, keystore, keystorePass, certPass, needClientAuth, null);
                                            }
                                            if (!ServerSessionFTP.this.thisSession.uiBG("adminAllowed") && ServerSessionFTP.this.thisSession.uiSG("user_ip").equals("127.0.0.1")) {
                                                ServerSessionFTP.this.thisSession.uiPUT("user_ip", tempSock.getInetAddress().getHostAddress());
                                            }
                                            ServerSessionFTP.this.thisSession.kill_active_socks();
                                            ServerSessionFTP.this.thisSession.data_socks.addElement(tempSock);
                                            ServerSessionFTP.this.thisSession.old_data_socks.addElement(tempSock);
                                        }
                                        catch (SocketTimeoutException e) {
                                            if (ServerSessionFTP.this.thisSession.pasv_socks.contains(pasv_sock2)) {
                                                try {
                                                    ServerSessionFTP.this.write_command("550", e + "\r\n" + LOC.G("20 second timeout while waiting for PASV connection on port $0.", String.valueOf(pasv_sock2.getLocalPort())));
                                                    ServerSessionFTP.this.thisSession.doErrorEvent(new Exception(ServerSessionFTP.this.thisSession.uiSG("lastLog")));
                                                }
                                                catch (Exception exception) {
                                                    // empty catch block
                                                }
                                            }
                                            try {
                                                pasv_sock2.close();
                                            }
                                            catch (Exception exception) {
                                                // empty catch block
                                            }
                                            ServerSessionFTP.this.thisSession.pasv_socks.remove(pasv_sock2);
                                        }
                                        catch (IOException e) {
                                            try {
                                                pasv_sock2.close();
                                                ServerSessionFTP.this.thisSession.pasv_socks.remove(pasv_sock2);
                                            }
                                            catch (Exception exception) {
                                                // empty catch block
                                            }
                                            ServerSessionFTP.this.thisSession.pasv_socks.remove(pasv_sock2);
                                        }
                                    }
                                    finally {
                                        ServerSessionFTP.this.thisSession.pasv_socks.remove(pasv_sock2);
                                    }
                                }
                            });
                            this.thisSession.not_done = this.thisSession.uiSG("the_command").equals("EPSV") ? this.write_command("229", LOC.G("Entering Extended Passive Mode") + " (|||" + pasv_stuff.getProperty("port", "0") + "|)") : this.write_command("227", LOC.G("Entering Passive Mode") + " (" + pasv_stuff.getProperty("data_string") + ")");
                            this.thisSession.uiPUT("port_remote_ip", pasv_stuff.getProperty("ip", "0.0.0.0"));
                            this.thisSession.uiPUT("port_remote_port", String.valueOf(Integer.parseInt(pasv_stuff.getProperty("port", "0"))));
                            this.thisSession.uiPUT("pasv_connect", "true");
                        }
                        catch (Exception e) {
                            Log.log("FTP_SERVER", 1, e);
                            if (("" + e).indexOf("Interrupted") < 0) break block343;
                            throw e;
                        }
                    }
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && (this.thisSession.uiSG("the_command").equals("PORT") || this.thisSession.uiSG("the_command").equals("EPRT") && ServerStatus.BG("epsveprt")) && this.thisSession.uiSG("the_command_data").length() > 0) {
                    block344: {
                        this.thisSession.stop_idle_timer();
                        try {
                            if (this.thisSession.uiSG("the_command").equals("EPRT")) {
                                if (this.thisSession.uiSG("the_command_data").startsWith("|")) {
                                    this.thisSession.uiPUT("the_command_data", "1" + this.thisSession.uiSG("the_command_data"));
                                }
                                this.thisSession.uiPUT("the_command_data", this.thisSession.uiSG("the_command_data").replace('|', ','));
                                StringTokenizer st = new StringTokenizer(this.thisSession.uiSG("the_command_data"), ",");
                                String EIPV = st.nextToken().toString();
                                String EIPProtocol = st.nextToken().toString();
                                String EIP = st.nextToken().toString();
                                String EPortStr = st.nextToken().toString();
                                int EPort = Integer.parseInt(EPortStr.trim());
                                if (EIP.equals("")) {
                                    EIP = this.thisSession.uiSG("user_ip");
                                }
                                this.thisSession.uiPUT("the_command_data", String.valueOf(EIP.replace('.', ',')) + "," + EPort / 256 + "," + (EPort - EPort / 256 * 256));
                                this.thisSession.uiPUT("port_remote_ip", EIP);
                                this.thisSession.uiPUT("port_remote_port", String.valueOf(EPort));
                            } else {
                                this.thisSession.uiPUT("last_port_string", this.thisSession.uiSG("the_command_data"));
                                this.thisSession.uiPUT("pasv_connect", "false");
                                try {
                                    while (this.thisSession.pasv_socks.size() > 0) {
                                        ((ServerSocket)this.thisSession.pasv_socks.remove(0)).close();
                                    }
                                }
                                catch (Exception st) {
                                    // empty catch block
                                }
                                boolean parsing_ip = true;
                                int str_pos = 0;
                                int comma_count = 0;
                                while (parsing_ip) {
                                    if (this.thisSession.uiSG("the_command_data").charAt(str_pos++) == ',') {
                                        ++comma_count;
                                    }
                                    if (comma_count != 4) continue;
                                    parsing_ip = false;
                                }
                                this.thisSession.uiPUT("port_remote_ip", this.thisSession.uiSG("the_command_data").substring(0, str_pos - 1));
                                this.thisSession.uiPUT("port_remote_ip", this.thisSession.uiSG("port_remote_ip").replace(',', '.'));
                                if (!this.thisSession.uiBG("adminAllowed") && this.thisSession.uiSG("user_ip").equals("127.0.0.1")) {
                                    this.thisSession.uiPUT("user_ip", this.thisSession.uiSG("port_remote_ip"));
                                }
                                String remote_port_raw = this.thisSession.uiSG("the_command_data").substring(str_pos);
                                Integer port_part1 = new Integer(remote_port_raw.substring(0, remote_port_raw.indexOf(44)));
                                Integer port_part2 = new Integer(remote_port_raw.substring(remote_port_raw.indexOf(44) + 1, remote_port_raw.length()));
                                int port1 = port_part1;
                                int port2 = port_part2;
                                this.thisSession.uiPUT("port_remote_port", String.valueOf(port1 * 256 + port2));
                            }
                            if (ServerStatus.BG("deny_reserved_ports") && this.thisSession.uiIG("port_remote_port") < 1024) {
                                this.thisSession.uiPUT("port_remote_ip", "0.0.0.0");
                                this.thisSession.not_done = this.write_command("550", LOC.G("%PORT_reserved%"));
                                this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                                this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                                this.thisSession.uiPUT("port_remote_port", "0");
                            } else if (ServerStatus.BG("deny_fxp") && !this.thisSession.uiSG("port_remote_ip").equals(this.thisSession.uiSG("user_ip"))) {
                                this.thisSession.not_done = this.write_command("550", LOC.G("%PORT-fxp%"));
                                this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                                this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                                this.thisSession.uiPUT("port_remote_ip", "0.0.0.0");
                                this.thisSession.uiPUT("port_remote_port", "0");
                            } else if (this.thisSession.uiBG("secure") && ServerStatus.BG("deny_secure_active_mode")) {
                                this.thisSession.not_done = this.write_command("509", LOC.G("Active mode FTP won't work when the control channel is encrypted, use passive (PASV) mode."));
                                this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                                this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                                this.thisSession.uiPUT("port_remote_ip", "0.0.0.0");
                                this.thisSession.uiPUT("port_remote_port", "0");
                            } else {
                                this.thisSession.not_done = this.write_command("200", LOC.G("%PORT%"));
                            }
                        }
                        catch (Exception e) {
                            if (("" + e).indexOf("Interrupted") < 0) break block344;
                            throw e;
                        }
                    }
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && (this.thisSession.uiSG("the_command").equals("SITE") || this.thisSession.uiSG("the_command").equals("RCMD")) && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    this.do_SITE();
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").toUpperCase().indexOf("MD5") >= 0 && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    this.do_XMD5(this.thisSession.uiSG("the_command_data"));
                    this.thisSession.start_idle_timer();
                } else if (!this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("SITE") && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("PROXY_")) {
                        String key = this.thisSession.uiSG("the_command_data");
                        if (key.indexOf(" ") >= 0) {
                            key = key.substring(0, key.indexOf(" "));
                        }
                        String val = "";
                        if (this.thisSession.uiSG("the_command_data").indexOf(" ") >= 0) {
                            val = this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").indexOf(" ") + 1);
                        }
                        this.thisSession.uiPUT(key.toLowerCase(), val);
                        this.thisSession.not_done = this.write_command("214", "OK");
                    } else {
                        this.thisSession.not_done = this.write_command("502", LOC.G("%unknown_command%"));
                        this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                        this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                    }
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiBG("user_logged_in") && this.thisSession.uiSG("the_command").equals("ACCT") && this.thisSession.uiSG("the_command_data").length() > 0) {
                    this.thisSession.stop_idle_timer();
                    GenericClient c = this.thisSession.uVFS.getClient(this.thisSession.uVFS.get_item(this.thisSession.uiSG("current_dir")));
                    try {
                        String response = c.doCommand(String.valueOf(this.thisSession.uiSG("the_command")) + " " + this.thisSession.uiSG("the_command_data"));
                        if (response == null || response.trim().equals("")) {
                            this.thisSession.not_done = this.write_command("502", LOC.G("%unknown_command%"));
                            this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                            this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                        } else {
                            this.thisSession.not_done = this.write_command(response);
                        }
                    }
                    finally {
                        c = this.thisSession.uVFS.releaseClient(c);
                    }
                    this.thisSession.start_idle_timer();
                } else if (this.thisSession.uiSG("the_command").equals("QUIT")) {
                    this.thisSession.stop_idle_timer();
                    int loops = 0;
                    while (this.thisSession.retr_files_pool_free.size() == 0) {
                        Thread.sleep(100L);
                        if (loops++ > 50) break;
                    }
                    loops = 0;
                    while (this.thisSession.stor_files_pool_free.size() == 0) {
                        Thread.sleep(100L);
                        if (loops++ > 50) break;
                    }
                    loops = 0;
                    while (this.list_files != null && this.list_files.active) {
                        Thread.sleep(100L);
                        if (loops++ > 50) break;
                    }
                    this.thisSession.not_done = this.write_command("221", LOC.G("%QUIT%"));
                    this.thisSession.not_done = false;
                } else if (this.thisSession.uiBG("user_logged_in")) {
                    if (this.thisSession.uiSG("the_command").length() > 0) {
                        this.thisSession.not_done = this.write_command("502", LOC.G("%unknown_command%"));
                        if (!this.thisSession.uiSG("the_command").equalsIgnoreCase("EPSV") && !this.thisSession.uiSG("the_command").equalsIgnoreCase("EPRT")) {
                            this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                            this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                        }
                    }
                } else if (this.thisSession.uiSG("the_command").length() > 0) {
                    this.thisSession.not_done = this.write_command("550", LOC.G("%unknown_command%"));
                    this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                    this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                }
                this.thisSession.uiPUT("the_command", "");
                this.thisSession.uiPUT("the_command_data", "");
            }
        }
        catch (Exception e) {
            boolean needClientAuth;
            boolean bl = needClientAuth = ServerStatus.BG("needClientAuth") || this.thisSession.server_item.getProperty("needClientAuth", "").equals("true") || this.thisSession.user != null && this.thisSession.user.getProperty("needClientAuth", "false").equals("true");
            if (e.toString().indexOf("null cert chain") >= 0 && needClientAuth) {
                this.thisSession.add_log("[" + this.thisSession.server_item.getProperty("serverType", "ftp") + ":" + this.thisSession.uiSG("user_number") + ":" + this.thisSession.uiSG("user_name") + ":" + this.thisSession.uiSG("user_ip") + "] *" + LOC.G("Client certificate was rejected.") + "*", "QUIT");
            }
            Log.log("FTP_SERVER", 1, e);
            this.thisSession.uiPUT("dieing", "true");
        }
        this.thisSession.add_log("[" + this.thisSession.server_item.getProperty("serverType", "ftp") + ":" + this.thisSession.uiSG("user_number") + ":" + this.thisSession.uiSG("user_name") + ":" + this.thisSession.uiSG("user_ip") + "] *" + LOC.G("Disconnected") + ".*", "QUIT");
        this.thisSession.uiPUT("dieing", "true");
        this.thisSession.do_kill();
    }

    public int get_command() throws Exception {
        if (this.thisSession.uiBG("dont_read")) {
            return 0;
        }
        if (this.is == null) {
            return -1;
        }
        this.thisSession.uiPUT("the_command", "");
        String data = null;
        while (true) {
            try {
                data = this.is.readLine();
            }
            catch (SocketTimeoutException e) {
                boolean killOk = true;
                if (this.thisSession.retr_files_pool_used.size() > 0) {
                    killOk = false;
                }
                if (this.thisSession.stor_files_pool_used.size() > 0) {
                    killOk = false;
                }
                if (this.list_files == null || !this.list_files.active) continue;
                killOk = false;
                if (!killOk) continue;
                this.thisSession.uiPUT("termination_message", "TIMEOUT");
                this.thisSession.do_kill();
                throw e;
            }
            break;
        }
        if (data == null) {
            return -1;
        }
        Properties p = new Properties();
        p.put("the_command_data", data);
        this.thisSession.runPlugin("beforeCommand", p);
        data = p.getProperty("the_command_data", data);
        if (!data.toUpperCase().startsWith("PASS ")) {
            data = Common.url_decode(data);
        }
        this.thisSession.uiPUT("the_command", data.trim());
        this.thisSession.uiPUT("the_command", ServerStatus.thisObj.strip_variables(this.thisSession.uiSG("the_command"), this.thisSession));
        if (this.thisSession.uiSG("the_command").indexOf(" ") >= 0) {
            this.thisSession.uiPUT("the_command_data", this.thisSession.uiSG("the_command").substring(this.thisSession.uiSG("the_command").indexOf(" ") + 1));
            this.thisSession.uiPUT("the_command", this.thisSession.uiSG("the_command").substring(0, this.thisSession.uiSG("the_command").indexOf(" ")));
        } else {
            this.thisSession.uiPUT("the_command_data", "");
        }
        this.thisSession.uiPUT("the_command", this.thisSession.uiSG("the_command").toUpperCase());
        if (!this.thisSession.uiSG("the_command").equals("USER") && !this.thisSession.uiSG("the_command").equals("PASS") && ServerStatus.BG("fix_slashes")) {
            this.thisSession.uiPUT("the_command_data", this.thisSession.uiSG("the_command_data").replace('\\', '/'));
        }
        if (this.thisSession.uiSG("the_command").indexOf("ABOR") >= 0) {
            this.thisSession.uiPUT("the_command", "ABOR");
        }
        if (this.thisSession.uiSG("the_command").startsWith("PASS") && !this.thisSession.uiSG("user_name").toUpperCase().equals("ANONYMOUS") && !this.thisSession.uiSG("user_name").toUpperCase().equals("PUBLIC")) {
            this.thisSession.add_log("[" + this.thisSession.server_item.getProperty("serverType", "ftp") + ":" + this.thisSession.uiSG("user_number") + ":" + this.thisSession.uiSG("user_name") + ":" + this.thisSession.uiSG("user_ip") + "] " + this.thisSession.SG("READ") + " : *" + this.thisSession.uiSG("the_command") + "*", this.thisSession.uiSG("the_command"));
            this.thisSession.uiPUT("last_action", this.thisSession.uiSG("the_command"));
        } else if (this.thisSession.uiSG("the_command").startsWith("PASS") && this.thisSession.uiSG("user_name").toUpperCase().equals("ANONYMOUS")) {
            this.thisSession.add_log("[" + this.thisSession.server_item.getProperty("serverType", "ftp") + ":" + this.thisSession.uiSG("user_number") + ":" + this.thisSession.uiSG("user_name") + ":" + this.thisSession.uiSG("user_ip") + "] " + this.thisSession.SG("READ") + " : *" + this.thisSession.uiSG("the_command") + " " + this.thisSession.uiSG("the_command_data") + "*", "USER");
            this.thisSession.uiPUT("last_action", this.thisSession.uiSG("the_command"));
        } else {
            this.thisSession.add_log("[" + this.thisSession.server_item.getProperty("serverType", "ftp") + ":" + this.thisSession.uiSG("user_number") + ":" + this.thisSession.uiSG("user_name") + ":" + this.thisSession.uiSG("user_ip") + "] " + this.thisSession.SG("READ") + " : *" + this.thisSession.uiSG("the_command") + " " + this.thisSession.uiSG("the_command_data") + "*", this.thisSession.uiSG("the_command"));
            this.thisSession.uiPUT("last_action", String.valueOf(this.thisSession.uiSG("the_command")) + " " + this.thisSession.uiSG("the_command_data"));
        }
        this.thisSession.uiPUT("last_logged_command", this.thisSession.uiSG("the_command"));
        String full_the_command_data = this.thisSession.uiSG("the_command_data");
        if (!full_the_command_data.startsWith("/")) {
            full_the_command_data = String.valueOf(this.thisSession.uiSG("current_dir")) + full_the_command_data;
        }
        return 1;
    }

    public boolean write_command(String data) throws Exception {
        try {
            data = ServerStatus.change_user_safe_vars_to_values_static(data, this.thisSession.user, this.thisSession.user_info, this.thisSession);
            return this.write_command_raw(data);
        }
        catch (Exception e) {
            if (("" + e).indexOf("Interrupted") >= 0) {
                throw e;
            }
            this.thisSession.do_kill();
            return false;
        }
    }

    public boolean write_command_raw(String data) throws Exception {
        block4: {
            try {
                this.thisSession.add_log("[" + this.thisSession.server_item.getProperty("serverType", "ftp") + ":" + this.thisSession.uiSG("user_number") + ":" + this.thisSession.uiSG("user_name") + ":" + this.thisSession.uiSG("user_ip") + "] WROTE: *" + data + "*", this.thisSession.uiSG("last_logged_command"));
                data = String.valueOf(data) + "\r\n";
                if (!this.thisSession.uiBG("dont_write")) break block4;
                return true;
            }
            catch (Exception e) {
                if (("" + e).indexOf("Interrupted") >= 0) {
                    throw e;
                }
                this.thisSession.do_kill();
                return false;
            }
        }
        this.os.write(data.getBytes(this.thisSession.SG("char_encoding")));
        this.os.flush();
        return true;
    }

    public boolean write_command(String code, String data) throws Exception {
        block4: {
            try {
                data = ServerStatus.change_user_safe_vars_to_values_static(data, this.thisSession.user, this.thisSession.user_info, this.thisSession);
                Properties p = new Properties();
                p.put("command_code", code);
                p.put("command_data", data);
                this.thisSession.runPlugin("afterCommand", p);
                data = p.getProperty("command_data", data);
                code = p.getProperty("command_code", code);
                data = ServerStatus.thisObj.common_code.format_message(code, data).trim();
                this.thisSession.add_log("[" + this.thisSession.server_item.getProperty("serverType", "ftp") + ":" + this.thisSession.uiSG("user_number") + ":" + this.thisSession.uiSG("user_name") + ":" + this.thisSession.uiSG("user_ip") + "] " + this.thisSession.SG("WROTE") + ": *" + data + "*", this.thisSession.uiSG("last_logged_command"));
                this.thisSession.uiPUT("lastLog", data);
                if (!this.thisSession.uiBG("dont_write")) break block4;
                return true;
            }
            catch (Exception e) {
                if (("" + e).indexOf("Interrupted") >= 0) {
                    throw e;
                }
                Log.log("FTP_SERVER", 2, e);
                this.thisSession.do_kill();
                return false;
            }
        }
        data = String.valueOf(data) + "\r\n";
        this.os.write(data.getBytes(this.thisSession.SG("char_encoding")));
        this.os.flush();
        return true;
    }

    public void do_port_connect() throws Exception {
        if (this.thisSession.uiBG("pasv_connect")) {
            this.thisSession.uiPUT("pasv_connect", "false");
        } else {
            this.thisSession.start_idle_timer(60000);
            int loop = 0;
            while (loop++ < 3) {
                if (this.thisSession.uiBG("dataSecure")) {
                    SSLSocket ss = null;
                    try {
                        boolean needClientAuth;
                        String keystore = SSLKeyManager.loadKeyStoreToMemory(ServerStatus.SG("cert_path"));
                        String certPass = ServerStatus.SG("globalKeystoreCertPass");
                        String keystorePass = ServerStatus.SG("globalKeystorePass");
                        if (!this.thisSession.server_item.getProperty("customKeystore", "").equals("")) {
                            keystore = SSLKeyManager.loadKeyStoreToMemory(this.thisSession.server_item.getProperty("customKeystore", ""));
                            certPass = this.thisSession.server_item.getProperty("customKeystoreCertPass", "");
                            keystorePass = this.thisSession.server_item.getProperty("customKeystorePass", "");
                        }
                        boolean bl = needClientAuth = ServerStatus.BG("needClientAuth") || this.thisSession.server_item.getProperty("needClientAuth", "").equals("true") || this.thisSession.user != null && this.thisSession.user.getProperty("needClientAuth", "false").equals("true");
                        if (this.factory == null) {
                            this.ssl_context = ServerStatus.thisObj.common_code.getSSLContext(keystore, String.valueOf(keystore) + "_trust", keystorePass, certPass, this.thisSession.uiSG("secureType"), needClientAuth, true);
                            this.factory = this.ssl_context.getSocketFactory();
                        }
                        try {
                            Socket tempSock = new Socket();
                            tempSock.setReuseAddress(true);
                            try {
                                tempSock.bind(new InetSocketAddress(this.sock.getLocalAddress(), Integer.parseInt(this.thisSession.server_item.getProperty("source_port", String.valueOf(Integer.parseInt(this.thisSession.server_item.getProperty("bind_port", "21")) - 1)))));
                            }
                            catch (IOException e) {
                                tempSock.bind(new InetSocketAddress(this.sock.getLocalAddress(), 0));
                            }
                            tempSock.connect(new InetSocketAddress(this.thisSession.uiSG("port_remote_ip"), this.thisSession.uiIG("port_remote_port")));
                            ss = (SSLSocket)this.factory.createSocket(tempSock, this.thisSession.uiSG("port_remote_ip"), this.thisSession.uiIG("port_remote_port"), true);
                        }
                        catch (Exception e) {
                            Socket tempSock = new Socket(this.thisSession.uiSG("port_remote_ip"), this.thisSession.uiIG("port_remote_port"));
                            ss = (SSLSocket)this.factory.createSocket(tempSock, this.thisSession.uiSG("port_remote_ip"), this.thisSession.uiIG("port_remote_port"), true);
                        }
                        Common.configureSSLTLSSocket(ss);
                        ss.setNeedClientAuth(needClientAuth);
                        ss.setUseClientMode(this.thisSession.uiBG("sscn_mode"));
                        if (this.thisSession.uiBG("sscn_mode")) {
                            ss.startHandshake();
                        }
                        this.thisSession.kill_active_socks();
                        this.thisSession.data_socks.addElement(ss);
                        this.thisSession.old_data_socks.addElement(ss);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                try {
                    if (!Common.machine_is_mac()) {
                        Socket tempSock = new Socket();
                        tempSock.setReuseAddress(true);
                        try {
                            tempSock.bind(new InetSocketAddress(this.sock.getLocalAddress(), Integer.parseInt(this.thisSession.server_item.getProperty("source_port", "20"))));
                        }
                        catch (IOException e) {
                            tempSock.bind(new InetSocketAddress(this.sock.getLocalAddress(), 0));
                        }
                        tempSock.connect(new InetSocketAddress(this.thisSession.uiSG("port_remote_ip"), this.thisSession.uiIG("port_remote_port")));
                        this.thisSession.kill_active_socks();
                        this.thisSession.data_socks.addElement(tempSock);
                        this.thisSession.old_data_socks.addElement(tempSock);
                        break;
                    }
                    ServerSocket portlookup = new ServerSocket(0);
                    int locPort = portlookup.getLocalPort();
                    portlookup.close();
                    Socket tempSock = new Socket(this.thisSession.uiSG("port_remote_ip"), this.thisSession.uiIG("port_remote_port"), this.sock.getLocalAddress(), locPort);
                    this.thisSession.kill_active_socks();
                    this.thisSession.data_socks.addElement(tempSock);
                    this.thisSession.old_data_socks.addElement(tempSock);
                    break;
                }
                catch (IOException e) {
                    try {
                        Socket tempSock = new Socket(this.thisSession.uiSG("port_remote_ip"), this.thisSession.uiIG("port_remote_port"));
                        this.thisSession.kill_active_socks();
                        this.thisSession.data_socks.addElement(tempSock);
                        this.thisSession.old_data_socks.addElement(tempSock);
                        break;
                    }
                    catch (IOException ee) {
                        Log.log("FTP_SERVER", 1, ee);
                        Thread.sleep(1000L);
                    }
                }
            }
            this.thisSession.stop_idle_timer();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Properties do_pasv_command() throws Exception {
        ServerSocket create_it;
        Properties return_item;
        block72: {
            return_item = new Properties();
            create_it = null;
            try {
                int loop_count = 0;
                if (this.thisSession.server_item.getProperty("pasv_ports") == null) {
                    this.thisSession.server_item.put("pasv_ports", "1025-65535");
                }
                if (this.thisSession.server_item.getProperty("ftp_aware_router") == null) {
                    this.thisSession.server_item.put("ftp_aware_router", "true");
                }
                while (create_it == null) {
                    if (this.thisSession.server_item.getProperty("pasv_ports").indexOf("1025") >= 0 && (this.thisSession.server_item.getProperty("pasv_ports").indexOf("65536") >= 0 || this.thisSession.server_item.getProperty("pasv_ports").indexOf("65535") >= 0)) {
                        this.thisSession.uiPUT("sni_enabled", "false");
                        if (this.thisSession.server_item.getProperty("sni_enabled", "false").equals("true")) {
                            try {
                                new SNIReady().test();
                                this.thisSession.uiPUT("sni_enabled", "true");
                            }
                            catch (Throwable throwable) {
                                // empty catch block
                            }
                        }
                        if (this.thisSession.uiBG("dataSecure") && (!this.thisSession.uiBG("sni_enabled") || this.sni_keystore_used.length() > 0)) {
                            boolean needClientAuth;
                            String keystore = SSLKeyManager.loadKeyStoreToMemory(ServerStatus.SG("cert_path"));
                            String certPass = ServerStatus.SG("globalKeystoreCertPass");
                            String keystorePass = ServerStatus.SG("globalKeystorePass");
                            if (!this.thisSession.server_item.getProperty("customKeystore", "").equals("")) {
                                keystore = SSLKeyManager.loadKeyStoreToMemory(this.thisSession.server_item.getProperty("customKeystore"));
                                certPass = this.thisSession.server_item.getProperty("customKeystoreCertPass", "");
                                keystorePass = this.thisSession.server_item.getProperty("customKeystorePass", "");
                            }
                            boolean bl = needClientAuth = ServerStatus.BG("needClientAuth") || this.thisSession.server_item.getProperty("needClientAuth", "").equals("true") || this.thisSession.user != null && this.thisSession.user.getProperty("needClientAuth", "false").equals("true");
                            if (this.sni_keystore_used.length() > 0) {
                                keystore = this.sni_keystore_used.toString();
                                Log.log("FTP_SERVER", 0, "Reusing SSL keystore for PASV:" + keystore);
                            }
                            try {
                                create_it = ServerStatus.thisObj.common_code.getServerSocket(0, this.thisSession.uiSG("listen_ip"), keystore, keystorePass, certPass, ServerStatus.SG("disabled_ciphers"), needClientAuth, 10, true, true, this.ssl_context);
                            }
                            catch (Exception e) {
                                if (("" + e).indexOf("Interrupted") >= 0) {
                                    throw e;
                                }
                                Log.log("FTP_SERVER", 1, e);
                            }
                            if (create_it == null && !this.thisSession.server_item.getProperty("ip", "0.0.0.0").equals("lookup")) {
                                try {
                                    create_it = ServerStatus.thisObj.common_code.getServerSocket(0, this.thisSession.server_item.getProperty("ip", "0.0.0.0"), keystore, keystorePass, certPass, ServerStatus.SG("disabled_ciphers"), needClientAuth, 10, true, true, this.ssl_context);
                                }
                                catch (Exception e) {
                                    if (("" + e).indexOf("Interrupted") >= 0) {
                                        throw e;
                                    }
                                    Log.log("FTP_SERVER", 1, e);
                                }
                            }
                            if (create_it == null) {
                                try {
                                    create_it = ServerStatus.thisObj.common_code.getServerSocket(0, "0.0.0.0", keystore, keystorePass, certPass, ServerStatus.SG("disabled_ciphers"), needClientAuth, 10, true, true, this.ssl_context);
                                }
                                catch (Exception e) {
                                    if (("" + e).indexOf("Interrupted") >= 0) {
                                        throw e;
                                    }
                                    Log.log("FTP_SERVER", 1, e);
                                }
                            }
                        } else {
                            try {
                                create_it = new ServerSocket(0, 999, InetAddress.getByName(this.thisSession.uiSG("listen_ip")));
                            }
                            catch (Exception e) {
                                if (("" + e).indexOf("Interrupted") >= 0) {
                                    throw e;
                                }
                                Log.log("FTP_SERVER", 3, e);
                            }
                            if (create_it == null && !this.thisSession.server_item.getProperty("ip", "0.0.0.0").equals("lookup")) {
                                try {
                                    create_it = new ServerSocket(0, 999, InetAddress.getByName(this.thisSession.server_item.getProperty("ip", "0.0.0.0")));
                                }
                                catch (Exception e) {
                                    if (("" + e).indexOf("Interrupted") >= 0) {
                                        throw e;
                                    }
                                    Log.log("FTP_SERVER", 3, e);
                                }
                            }
                            if (create_it == null) {
                                try {
                                    create_it = new ServerSocket(0);
                                }
                                catch (Exception e) {
                                    if (("" + e).indexOf("Interrupted") >= 0) {
                                        throw e;
                                    }
                                    Log.log("FTP_SERVER", 1, e);
                                }
                            }
                        }
                    } else {
                        Object e = portLocker;
                        synchronized (e) {
                            block69: {
                                String pasv_ports = this.thisSession.server_item.getProperty("pasv_ports").trim();
                                int min_pasv_port = 1025;
                                int max_pasv_port = 65535;
                                try {
                                    block71: {
                                        if (pasv_ports.indexOf(",") < 0 && pasv_ports.indexOf("-") > 0) {
                                            min_pasv_port = Integer.parseInt(pasv_ports.substring(0, pasv_ports.indexOf("-")).trim());
                                            max_pasv_port = Integer.parseInt(pasv_ports.substring(pasv_ports.indexOf("-") + 1).trim());
                                        } else {
                                            min_pasv_port = Integer.parseInt(pasv_ports.substring(0, pasv_ports.indexOf(",")).trim());
                                            max_pasv_port = Integer.parseInt(pasv_ports.substring(pasv_ports.indexOf(",") + 1).trim());
                                        }
                                        this.current_pasv_port = Integer.parseInt(this.thisSession.server_item.getProperty("current_pasv_port", String.valueOf(min_pasv_port)));
                                        if (this.current_pasv_port < min_pasv_port || this.current_pasv_port > max_pasv_port) {
                                            this.current_pasv_port = min_pasv_port;
                                        }
                                        if (this.thisSession.uiBG("dataSecure")) {
                                            boolean needClientAuth;
                                            String keystorePass;
                                            String certPass;
                                            String keystore;
                                            block68: {
                                                keystore = SSLKeyManager.loadKeyStoreToMemory(ServerStatus.SG("cert_path"));
                                                certPass = ServerStatus.SG("globalKeystoreCertPass");
                                                keystorePass = ServerStatus.SG("globalKeystorePass");
                                                if (!this.thisSession.server_item.getProperty("customKeystore", "").equals("")) {
                                                    keystore = SSLKeyManager.loadKeyStoreToMemory(this.thisSession.server_item.getProperty("customKeystore", ""));
                                                    certPass = this.thisSession.server_item.getProperty("customKeystoreCertPass", "");
                                                    keystorePass = this.thisSession.server_item.getProperty("customKeystorePass", "");
                                                }
                                                boolean bl = needClientAuth = ServerStatus.BG("needClientAuth") || this.thisSession.server_item.getProperty("needClientAuth", "").equals("true") || this.thisSession.user != null && this.thisSession.user.getProperty("needClientAuth", "false").equals("true");
                                                if (this.sni_keystore_used.length() > 0) {
                                                    keystore = this.sni_keystore_used.toString();
                                                    Log.log("FTP_SERVER", 0, "Reusing SSL keystore for PASV:" + keystore);
                                                }
                                                if (ServerStatus.BG("pasv_bind_all")) {
                                                    create_it = ServerStatus.thisObj.common_code.getServerSocket(this.current_pasv_port, this.thisSession.server_item.getProperty("ip", "0.0.0.0"), keystore, keystorePass, certPass, ServerStatus.SG("disabled_ciphers"), needClientAuth, 10, true, true, this.ssl_context);
                                                }
                                                try {
                                                    create_it = ServerStatus.thisObj.common_code.getServerSocket(this.current_pasv_port, this.thisSession.uiSG("listen_ip"), keystore, keystorePass, certPass, ServerStatus.SG("disabled_ciphers"), needClientAuth, 10, true, true, this.ssl_context);
                                                }
                                                catch (Exception e2) {
                                                    if (("" + e2).indexOf("Interrupted") < 0) break block68;
                                                    throw e2;
                                                }
                                            }
                                            if (create_it != null || this.thisSession.server_item.getProperty("ip", "0.0.0.0").equals("lookup")) break block69;
                                            try {
                                                create_it = ServerStatus.thisObj.common_code.getServerSocket(this.current_pasv_port, this.thisSession.server_item.getProperty("ip", "0.0.0.0"), keystore, keystorePass, certPass, ServerStatus.SG("disabled_ciphers"), needClientAuth, 10, true, true, this.ssl_context);
                                                break block69;
                                            }
                                            catch (Exception e3) {
                                                if (("" + e3).indexOf("Interrupted") >= 0) {
                                                    throw e3;
                                                }
                                                break block69;
                                            }
                                        }
                                        if (ServerStatus.BG("pasv_bind_all")) {
                                            create_it = new ServerSocket(this.current_pasv_port);
                                        } else {
                                            block70: {
                                                try {
                                                    create_it = new ServerSocket(this.current_pasv_port, 999, InetAddress.getByName(this.thisSession.uiSG("listen_ip")));
                                                }
                                                catch (Exception e4) {
                                                    if (("" + e4).indexOf("Interrupted") < 0) break block70;
                                                    throw e4;
                                                }
                                            }
                                            if (create_it == null && !this.thisSession.server_item.getProperty("ip", "0.0.0.0").equals("lookup")) {
                                                try {
                                                    create_it = new ServerSocket(this.current_pasv_port, 999, InetAddress.getByName(this.thisSession.server_item.getProperty("ip", "0.0.0.0")));
                                                }
                                                catch (Exception e5) {
                                                    if (("" + e5).indexOf("Interrupted") < 0) break block71;
                                                    throw e5;
                                                }
                                            }
                                        }
                                    }
                                    if (create_it != null) break block69;
                                    try {
                                        create_it = new ServerSocket(this.current_pasv_port);
                                    }
                                    catch (Exception e6) {
                                        if (("" + e6).indexOf("Interrupted") >= 0) {
                                            throw e6;
                                        }
                                    }
                                }
                                finally {
                                    ++this.current_pasv_port;
                                    if (this.current_pasv_port > max_pasv_port) {
                                        this.current_pasv_port = min_pasv_port;
                                    }
                                    this.thisSession.server_item.put("current_pasv_port", String.valueOf(this.current_pasv_port));
                                    if (this.current_pasv_port == min_pasv_port && loop_count > 1) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (create_it == null) {
                        Thread.sleep(100L);
                        if (loop_count++ <= 1000) continue;
                        Thread.currentThread().interrupt();
                        continue;
                    }
                    break;
                }
            }
            catch (Exception e) {
                Log.log("FTP_SERVER", 1, e);
                if (("" + e).indexOf("Interrupted") < 0) break block72;
                throw e;
            }
        }
        return_item.put("server_socket", create_it);
        int the_port = create_it.getLocalPort();
        return_item.put("port", String.valueOf(the_port));
        String return_address = null;
        return_address = this.thisSession.uiSG("user_ip").startsWith("127.0") ? this.thisSession.uiSG("user_ip") : (this.thisSession.uiSG("listen_ip").indexOf(",") >= 0 ? this.thisSession.uiSG("listen_ip") : (this.thisSession.uiSG("listen_ip").trim().charAt(0) > '9' && !this.thisSession.uiSG("listen_ip").trim().equalsIgnoreCase("auto") ? InetAddress.getByName(this.thisSession.uiSG("listen_ip").trim()).getHostAddress() : (this.thisSession.uiSG("user_ip").split("\\.")[0].equals(this.sock.getLocalAddress().getHostAddress().split("\\.")[0]) && this.thisSession.uiSG("user_ip").split("\\.")[1].equals(this.sock.getLocalAddress().getHostAddress().split("\\.")[1]) ? this.sock.getLocalAddress().getHostAddress() : (ServerStatus.BG("allow_local_ip_pasv") && this.thisSession.uiSG("user_ip").split("\\.")[0].equals(com.crushftp.client.Common.getLocalIP().split("\\.")[0]) && this.thisSession.uiSG("user_ip").split("\\.")[1].equals(com.crushftp.client.Common.getLocalIP().split("\\.")[1]) ? com.crushftp.client.Common.getLocalIP() : (ServerStatus.BG("allow_local_ip_pasv_any") && Common.check_local_ip(this.thisSession.uiSG("user_ip")) ? this.sock.getLocalAddress().getHostAddress() : (this.thisSession.uiBG("secure") || this.thisSession.server_item.getProperty("ftp_aware_router").equals("false") ? this.thisSession.uiSG("listen_ip") : (create_it.getInetAddress().getHostAddress().equals("0.0.0.0") ? this.sock.getLocalAddress().getHostAddress() : this.sock.getLocalAddress().getHostAddress())))))));
        Log.log("FTP_SERVER", 3, "return_address:" + return_address);
        Log.log("FTP_SERVER", 3, "listen_ip:" + this.thisSession.uiSG("listen_ip"));
        Log.log("FTP_SERVER", 3, "create_it:" + create_it.getInetAddress().getHostAddress());
        if (return_address.indexOf(",") >= 0) {
            return_address = return_address.substring(0, return_address.indexOf(",")).trim();
            String ips = this.thisSession.uiSG("listen_ip");
            ips = ips.substring(ips.indexOf(",") + 1).trim();
            ips = String.valueOf(ips) + "," + return_address;
            this.thisSession.uiPUT("listen_ip", ips);
            this.thisSession.server_item.put("server_ip", ips);
        }
        return_item.put("ip", return_address);
        return_item.put("data_string", String.valueOf(return_address.replace('.', ',')) + "," + the_port / 256 + "," + (the_port - the_port / 256 * 256));
        return return_item;
    }

    public void do_XMD5(String the_command_data) throws Exception {
        this.thisSession.stop_idle_timer();
        boolean chunked = the_command_data.toUpperCase().startsWith("MD5S");
        String the_dir = this.thisSession.uiSG("current_dir");
        String the_permissions = the_command_data.substring(the_command_data.indexOf(" ") + 1);
        this.thisSession.uiPUT("the_command_data", the_permissions);
        if (!the_command_data.equals("")) {
            the_dir = the_command_data.startsWith("/") ? the_command_data : String.valueOf(the_dir) + the_command_data;
            if ((the_dir = com.crushftp.client.Common.dots(the_dir)).equals("/")) {
                the_dir = this.thisSession.SG("root_dir");
            }
            if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSession.SG("root_dir").toUpperCase())) {
                the_dir = String.valueOf(this.thisSession.SG("root_dir")) + the_dir.substring(1);
            }
        }
        if (this.thisSession.check_access_privs(the_dir, "RETR")) {
            Properties item = this.thisSession.uVFS.get_item(the_dir);
            GenericClient c = this.thisSession.uVFS.getClient(item);
            try {
                Vector md5s = new Vector();
                Common.getMD5(c.download(new VRL(item.getProperty("url")).getPath(), 0L, -1L, true), md5s, chunked, true, Long.parseLong(item.getProperty("size")), Long.parseLong(item.getProperty("size")));
                this.thisSession.not_done = this.write_command("214", md5s.elementAt(0).toString());
            }
            finally {
                c = this.thisSession.uVFS.releaseClient(c);
            }
        } else {
            this.thisSession.not_done = this.write_command("550", LOC.G("MD5 not allowed."));
            this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
            this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
        }
        this.thisSession.start_idle_timer();
    }

    public void do_SITE() throws Exception {
        String the_user;
        String the_dir;
        boolean doProxySite = false;
        String original_the_command_data = this.thisSession.uiSG("the_command_data");
        if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("PASS") && this.thisSession.SG("site").toUpperCase().indexOf("(SITE_PASS)") >= 0) {
            if (this.thisSession.uiSG("new_pass1").equals("")) {
                this.thisSession.uiPUT("new_pass1", this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").indexOf(" ") + 1, this.thisSession.uiSG("the_command_data").length()));
                this.thisSession.not_done = this.write_command("214", LOC.G("%SITE_PASS-first%"));
            } else {
                this.thisSession.uiPUT("new_pass2", this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").indexOf(" ") + 1, this.thisSession.uiSG("the_command_data").length()));
                this.thisSession.not_done = this.thisSession.uiSG("new_pass1").equals(this.thisSession.uiSG("new_pass2")) ? this.write_command("214", this.thisSession.do_ChangePass(this.thisSession.uiSG("user_name"), this.thisSession.uiSG("new_pass2"))) : this.write_command("214", LOC.G("%SITE_PASS-bad%"));
                this.thisSession.uiPUT("new_pass1", "");
                this.thisSession.uiPUT("new_pass2", "");
            }
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("ZONE")) {
            SimpleDateFormat timeZone = new SimpleDateFormat("Z", Locale.US);
            this.thisSession.not_done = this.write_command("210", "UTC" + timeZone.format(new Date()));
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("UTIME")) {
            SimpleDateFormat df;
            this.thisSession.stop_idle_timer();
            the_dir = this.thisSession.uiSG("current_dir");
            this.thisSession.uiPUT("the_command_data", this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").indexOf(" ") + 1));
            int loc = 0;
            int dEnd = 0;
            int dStart = 0;
            String fileName = "";
            try {
                try {
                    loc = this.thisSession.uiSG("the_command_data").lastIndexOf(" ");
                    dEnd = this.thisSession.uiSG("the_command_data").lastIndexOf(" ", loc - 1);
                    dStart = this.thisSession.uiSG("the_command_data").lastIndexOf(" ", dEnd - 1);
                    loc = this.thisSession.uiSG("the_command_data").lastIndexOf(" ", dStart - 1);
                    fileName = this.thisSession.uiSG("the_command_data").substring(0, loc).trim();
                    SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                    df2.parse(this.thisSession.uiSG("the_command_data").substring(dStart, dEnd).trim());
                }
                catch (Exception e) {
                    dEnd = this.thisSession.uiSG("the_command_data").indexOf(" ");
                    dStart = 0;
                    fileName = this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").indexOf(" ") + 1).trim();
                    df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                    df.parse(this.thisSession.uiSG("the_command_data").substring(dStart, dEnd).trim());
                }
            }
            catch (Exception e) {
                // empty catch block
            }
            if (!fileName.equals("")) {
                the_dir = fileName.startsWith("/") ? fileName : String.valueOf(the_dir) + fileName;
                if (the_dir.equals("/")) {
                    the_dir = this.thisSession.SG("root_dir");
                }
                if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSession.SG("root_dir").toUpperCase())) {
                    the_dir = String.valueOf(this.thisSession.SG("root_dir")) + the_dir.substring(1);
                }
            }
            if (this.thisSession.check_access_privs(the_dir, "STOR") && !fileName.equals("")) {
                Properties item = this.thisSession.uVFS.get_item(the_dir);
                if (item != null) {
                    try {
                        df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                        Date d = df.parse(this.thisSession.uiSG("the_command_data").substring(dStart, dEnd).trim());
                        GregorianCalendar cal = new GregorianCalendar();
                        cal.setTime(d);
                        cal.setTimeInMillis((long)((double)cal.getTimeInMillis() + this.thisSession.DG("timezone_offset") * 1000.0 * 60.0 * 60.0));
                        d = new Date(cal.getTime().getTime());
                        boolean disable_mdtm_modifications = ServerStatus.BG("disable_mdtm_modifications");
                        if (this.thisSession.user.containsKey("disable_mdtm_modifications")) {
                            disable_mdtm_modifications = this.thisSession.BG("disable_mdtm_modifications");
                        }
                        if (!disable_mdtm_modifications) {
                            GenericClient c = this.thisSession.uVFS.getClient(item);
                            try {
                                c.mdtm(new VRL(item.getProperty("url")).getPath(), d.getTime());
                            }
                            finally {
                                c = this.thisSession.uVFS.releaseClient(c);
                            }
                        }
                        this.thisSession.not_done = this.write_command("200", LOC.G("%UTIME-good%"));
                    }
                    catch (Exception e) {
                        this.thisSession.not_done = this.write_command("550", String.valueOf(LOC.G("%UTIME-error%-exception")) + e);
                        this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                    }
                } else {
                    this.thisSession.not_done = this.write_command("550", LOC.G("%UTIME-error%"));
                    this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                }
            } else if (fileName.equals("")) {
                this.thisSession.not_done = this.write_command("200", this.thisSession.SG("Command ignored (bad format!)"));
                this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
            } else {
                this.thisSession.not_done = this.write_command("550", LOC.G("%UTIME-bad%"));
                this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
            }
            this.thisSession.start_idle_timer();
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("CHMOD")) {
            this.thisSession.stop_idle_timer();
            the_dir = this.thisSession.uiSG("current_dir");
            String the_permissions = this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").indexOf(" ") + 1);
            this.thisSession.uiPUT("the_command_data", the_permissions.substring(the_permissions.indexOf(" ") + 1));
            the_permissions = the_permissions.substring(0, the_permissions.indexOf(" "));
            if (the_permissions.indexOf(";") >= 0) {
                the_permissions = "";
                this.thisSession.uiPUT("the_command_data", "");
            }
            if (!this.thisSession.uiSG("the_command_data").equals("")) {
                the_dir = this.thisSession.uiSG("the_command_data").startsWith("/") ? this.thisSession.uiSG("the_command_data") : String.valueOf(the_dir) + this.thisSession.uiSG("the_command_data");
                if ((the_dir = com.crushftp.client.Common.dots(the_dir)).equals("/")) {
                    the_dir = this.thisSession.SG("root_dir");
                }
                if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSession.SG("root_dir").toUpperCase())) {
                    the_dir = String.valueOf(this.thisSession.SG("root_dir")) + the_dir.substring(1);
                }
            }
            if (this.thisSession.check_access_privs(the_dir, "STOR") && this.thisSession.SG("site").toUpperCase().indexOf("(SITE_CHMOD)") >= 0) {
                Properties item = this.thisSession.uVFS.get_item(the_dir);
                if (item.getProperty("url").toUpperCase().startsWith("FILE:")) {
                    GenericClient c = this.thisSession.uVFS.getClient(item);
                    try {
                        c.setMod(new VRL(item.getProperty("url")).getPath(), the_permissions, "");
                    }
                    finally {
                        c = this.thisSession.uVFS.releaseClient(c);
                    }
                    this.thisSession.not_done = this.write_command("214", LOC.G("%CHMOD-good%"));
                } else {
                    doProxySite = true;
                }
            } else {
                this.thisSession.not_done = this.write_command("214", LOC.G("%CHMOD-bad%"));
                this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
            }
            this.thisSession.start_idle_timer();
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("CHOWN")) {
            this.thisSession.stop_idle_timer();
            the_dir = this.thisSession.uiSG("current_dir");
            String the_permissions = this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").indexOf(" ") + 1);
            this.thisSession.uiPUT("the_command_data", the_permissions.substring(the_permissions.lastIndexOf(" ") + 1));
            the_permissions = the_permissions.substring(0, the_permissions.lastIndexOf(" "));
            if (the_permissions.indexOf(";") >= 0) {
                the_permissions = "";
                this.thisSession.uiPUT("the_command_data", "");
            }
            if (!this.thisSession.uiSG("the_command_data").equals("")) {
                the_dir = this.thisSession.uiSG("the_command_data").startsWith("/") ? this.thisSession.uiSG("the_command_data") : String.valueOf(the_dir) + this.thisSession.uiSG("the_command_data");
                if ((the_dir = com.crushftp.client.Common.dots(the_dir)).equals("/")) {
                    the_dir = this.thisSession.SG("root_dir");
                }
                if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSession.SG("root_dir").toUpperCase())) {
                    the_dir = String.valueOf(this.thisSession.SG("root_dir")) + the_dir.substring(1);
                }
            }
            if (this.thisSession.check_access_privs(the_dir, "STOR") && this.thisSession.SG("site").toUpperCase().indexOf("(SITE_CHOWN)") >= 0) {
                Properties item = this.thisSession.uVFS.get_item(the_dir);
                if (item.getProperty("url").toUpperCase().startsWith("FILE:")) {
                    GenericClient c = this.thisSession.uVFS.getClient(item);
                    try {
                        c.setOwner(new VRL(item.getProperty("url")).getPath(), the_permissions, "");
                    }
                    finally {
                        c = this.thisSession.uVFS.releaseClient(c);
                    }
                    this.thisSession.not_done = this.write_command("214", LOC.G("%CHOWN-good%"));
                } else {
                    doProxySite = true;
                }
            } else {
                this.thisSession.not_done = this.write_command("214", LOC.G("%CHOWN-bad%"));
                this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
            }
            this.thisSession.start_idle_timer();
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("CHGRP")) {
            this.thisSession.stop_idle_timer();
            the_dir = this.thisSession.uiSG("current_dir");
            String the_permissions = this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").indexOf(" ") + 1);
            this.thisSession.uiPUT("the_command_data", the_permissions.substring(the_permissions.lastIndexOf(" ") + 1));
            the_permissions = the_permissions.substring(0, the_permissions.lastIndexOf(" "));
            if (the_permissions.indexOf(";") >= 0) {
                the_permissions = "";
                this.thisSession.uiPUT("the_command_data", "");
            }
            if (!this.thisSession.uiSG("the_command_data").equals("")) {
                the_dir = this.thisSession.uiSG("the_command_data").startsWith("/") ? this.thisSession.uiSG("the_command_data") : String.valueOf(the_dir) + this.thisSession.uiSG("the_command_data");
                if ((the_dir = com.crushftp.client.Common.dots(the_dir)).equals("/")) {
                    the_dir = this.thisSession.SG("root_dir");
                }
                if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSession.SG("root_dir").toUpperCase())) {
                    the_dir = String.valueOf(this.thisSession.SG("root_dir")) + the_dir.substring(1);
                }
            }
            if (this.thisSession.check_access_privs(the_dir, "STOR") && this.thisSession.SG("site").toUpperCase().indexOf("(SITE_CHGRP)") >= 0) {
                Properties item = this.thisSession.uVFS.get_item(the_dir);
                if (item.getProperty("url").toUpperCase().startsWith("FILE:")) {
                    GenericClient c = this.thisSession.uVFS.getClient(item);
                    try {
                        c.setGroup(new VRL(item.getProperty("url")).getPath(), the_permissions, "");
                    }
                    finally {
                        c = this.thisSession.uVFS.releaseClient(c);
                    }
                    this.thisSession.not_done = this.write_command("214", LOC.G("%CHGRP-good%"));
                } else {
                    doProxySite = true;
                }
            } else {
                this.thisSession.not_done = this.write_command("214", LOC.G("%CHGRP-bad%"));
                this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
            }
            this.thisSession.start_idle_timer();
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("RANDOMACCESS")) {
            this.thisSession.uiPUT("randomaccess", this.thisSession.uiSG("the_command_data").substring("RANDOMACCESS".length()).trim().toLowerCase());
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("MD5") || this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("XMD5")) {
            this.do_XMD5(this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").indexOf(" ") + 1));
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().trim().startsWith("ZIP") && (this.thisSession.SG("site").toUpperCase().indexOf("(SITE_ZIP)") >= 0 || this.thisSession.SG("site").toUpperCase().indexOf("(SITE_INVISIBLE)") >= 0)) {
            if (this.thisSession.uiSG("the_command_data").toUpperCase().indexOf("DIR") >= 0) {
                this.thisSession.uiPUT("list_zip_dir", String.valueOf(!this.thisSession.uiBG("list_zip_dir")));
            }
            if (this.thisSession.uiSG("the_command_data").toUpperCase().indexOf("FILE") >= 0) {
                this.thisSession.uiPUT("list_zip_file", String.valueOf(!this.thisSession.uiBG("list_zip_file")));
            }
            if (this.thisSession.uiSG("the_command_data").toUpperCase().indexOf("ONLY") >= 0) {
                this.thisSession.uiPUT("list_zip_only", String.valueOf(!this.thisSession.uiBG("list_zip_only")));
            }
            if (this.thisSession.uiSG("the_command_data").toUpperCase().indexOf("APP") >= 0) {
                this.thisSession.uiPUT("list_zip_app", String.valueOf(!this.thisSession.uiBG("list_zip_app")));
            }
            this.thisSession.not_done = this.write_command("214", "(" + LOC.G("Zip files") + ":" + this.thisSession.uiBG("list_zip_file") + ")  (" + LOC.G("Zip applications") + ":" + this.thisSession.uiBG("list_zip_app") + ")  (" + LOC.G("Zip directories") + ":" + this.thisSession.uiBG("list_zip_dir") + ")  (" + LOC.G("Only show zipped directories") + ":" + this.thisSession.uiBG("list_zip_only") + ")");
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().trim().startsWith("DOT") && this.thisSession.SG("site").toUpperCase().indexOf("(SITE_DOT)") >= 0) {
            this.thisSession.uiPUT("list_dot", String.valueOf(!this.thisSession.uiBG("list_dot")));
            this.thisSession.not_done = this.write_command("214", "(. " + LOC.G("files") + " : " + this.thisSession.uiBG("list_dot") + ")");
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("VERSION") && this.thisSession.SG("site").toUpperCase().indexOf("(SITE_VERSION)") >= 0) {
            this.thisSession.not_done = this.write_command("214", "%version_info%");
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("USERS") && this.thisSession.SG("site").toUpperCase().indexOf("(SITE_USERS)") >= 0) {
            String user_lising = "";
            int x = 0;
            while (x < ServerStatus.siVG("user_list").size()) {
                try {
                    user_lising = String.valueOf(user_lising) + "'" + ((Properties)ServerStatus.siVG("user_list").elementAt(x)).getProperty("id") + "' , ";
                }
                catch (Exception item) {
                    // empty catch block
                }
                ++x;
            }
            this.thisSession.not_done = this.write_command("214", "USER LIST=" + user_lising.substring(0, user_lising.length() - 3));
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("KICK") && !this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("KICKBAN") && this.thisSession.uiSG("the_command_data").length() > 4 && this.thisSession.SG("site").toUpperCase().indexOf("(SITE_KICK)") >= 0) {
            the_user = this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").indexOf(" ") + 1, this.thisSession.uiSG("the_command_data").length());
            this.thisSession.not_done = ServerStatus.thisObj.kick(the_user) ? this.write_command("214", "USER " + the_user + " " + LOC.G("KICKED") + ".") : this.write_command("214", "USER " + the_user + " " + LOC.G("KICK failed") + ".  " + LOC.G("User") + " '" + the_user + "' " + LOC.G("not found") + ".");
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("KICKBAN") && this.thisSession.uiSG("the_command_data").length() > 7 && this.thisSession.SG("site").toUpperCase().indexOf("(SITE_KICKBAN)") >= 0) {
            the_user = this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").indexOf(" ") + 1, this.thisSession.uiSG("the_command_data").length());
            this.thisSession.not_done = ServerStatus.thisObj.ban(the_user, "SITE BAN") && ServerStatus.thisObj.kick(the_user) ? this.write_command("214", "USER " + the_user + " " + LOC.G("KICKED and BANNED") + ".") : this.write_command("214", "USER " + the_user + " " + LOC.G("KICK and BAN failed") + ".  " + LOC.G("User") + " '" + the_user + "' " + LOC.G("not found") + ".");
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("QUIT") && this.thisSession.SG("site").toUpperCase().indexOf("(SITE_QUIT)") >= 0) {
            this.thisSession.not_done = this.write_command("214", LOC.G("%SITE_QUIT-good%"));
            ServerStatus.siPUT("waiting_quit_user_name", this.thisSession.user_info);
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("RESTART") && (this.thisSession.SG("site").toUpperCase().indexOf("(SITE_QUIT)") >= 0 || this.thisSession.SG("site").toUpperCase().indexOf("(CONNECT)") >= 0)) {
            this.thisSession.not_done = this.write_command("214", LOC.G("Server will restart when you logout."));
            ServerStatus.siPUT("waiting_restart_user_name", this.thisSession.user_info);
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("PLUGIN") && this.thisSession.SG("site").toUpperCase().indexOf("(SITE_PLUGIN)") >= 0) {
            Properties fileItem;
            the_dir = this.thisSession.uiSG("the_command_data").substring("plugin".length()).trim();
            String event_name = the_dir.substring(0, the_dir.indexOf(" "));
            if ((the_dir = the_dir.substring(event_name.length()).trim()).equals("")) {
                the_dir = "/";
            }
            if ((the_dir = com.crushftp.client.Common.dots(the_dir)).equals("/")) {
                the_dir = this.thisSession.SG("root_dir");
            }
            if (the_dir.toUpperCase().startsWith("/") && !the_dir.toUpperCase().startsWith(this.thisSession.SG("root_dir").toUpperCase())) {
                the_dir = String.valueOf(this.thisSession.SG("root_dir")) + the_dir.substring(1);
            }
            if ((fileItem = this.thisSession.uVFS.get_item(the_dir)) == null) {
                fileItem = new Properties();
                fileItem.put("url", "ftp://127.0.0.1:56789/");
                fileItem.put("the_file_size", "1");
                fileItem.put("the_file_name", Common.last(the_dir));
            }
            fileItem.put("the_file_path", the_dir);
            fileItem.put("event_name", event_name);
            this.thisSession.do_event5("SITE", fileItem);
            this.thisSession.not_done = this.write_command("214", fileItem.getProperty("execute_log", "No Result"));
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("LIST")) {
            try {
                String add_str = "";
                if (!add_str.equals("")) {
                    int item_count = Common.count_str(add_str, "\r\n");
                    int folder_count = Common.count_str(add_str, "/\r\n");
                    add_str = String.valueOf(folder_count) + " " + LOC.G("folders") + ", " + (item_count - folder_count) + " " + LOC.G("files") + "." + "\r\n" + add_str;
                }
                this.thisSession.not_done = this.write_command("214", String.valueOf(add_str.trim()) + "\r\n" + LOC.G("End of list") + ".");
            }
            catch (Exception e) {
                this.thisSession.not_done = this.write_command("214", LOC.G("Format is SITE LIST /dir/path/"));
            }
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("SEARCH") || this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("DUPE")) {
            Vector listing = new Vector();
            try {
                this.thisSession.uVFS.getListing(listing, this.thisSession.uiSG("current_dir"), 20, 30000, false);
            }
            catch (Exception item_count) {
                // empty catch block
            }
            String search = this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").indexOf(" ") + 1);
            StringBuffer item_str = new StringBuffer();
            try {
                int x = 0;
                while (x < listing.size()) {
                    Properties item = (Properties)listing.elementAt(x);
                    if (LIST_handler.checkName(item, this.thisSession, false, false) && com.crushftp.client.Common.do_search(search.toUpperCase(), item.getProperty("name", "").toUpperCase(), false, 0)) {
                        LIST_handler.generateLineEntry(item, item_str, false, "/", false, this.thisSession, false);
                    }
                    ++x;
                }
                this.thisSession.not_done = this.write_command("214", String.valueOf(item_str.toString()) + "\r\n" + LOC.G("End of list") + ".");
            }
            catch (Exception e) {
                this.thisSession.not_done = this.write_command("214", LOC.G("Format is SITE SEARCH filename"));
            }
        } else if (this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("HELP")) {
            if (this.thisSession.uiSG("the_command_data").toUpperCase().length() == 4) {
                this.thisSession.not_done = this.write_command("214", LOC.G("%SITE_HELP%"));
            } else if (this.thisSession.uiSG("the_command_data").toUpperCase().endsWith("PASS")) {
                this.thisSession.not_done = this.write_command("214", LOC.G("%SITE_PASS%"));
            } else if (this.thisSession.uiSG("the_command_data").toUpperCase().endsWith("VERSION")) {
                this.thisSession.not_done = this.write_command("214", LOC.G("%SITE_VERSION%"));
            } else if (this.thisSession.uiSG("the_command_data").toUpperCase().endsWith("USERS")) {
                this.thisSession.not_done = this.write_command("214", LOC.G("%SITE_USERS%"));
            } else if (this.thisSession.uiSG("the_command_data").toUpperCase().endsWith("KICKBAN")) {
                this.thisSession.not_done = this.write_command("214", LOC.G("%SITE_KICKBAN%"));
            } else if (this.thisSession.uiSG("the_command_data").toUpperCase().endsWith("KICK")) {
                this.thisSession.not_done = this.write_command("214", LOC.G("%SITE_KICK%"));
            } else if (this.thisSession.uiSG("the_command_data").toUpperCase().endsWith("QUIT")) {
                this.thisSession.not_done = this.write_command("214", LOC.G("%SITE_QUIT%"));
            } else if (this.thisSession.uiSG("the_command_data").toUpperCase().endsWith("QUIT")) {
                this.thisSession.not_done = this.write_command("550", LOC.G("Unknown HELP Command"));
            }
        } else if (this.thisSession.uiSG("the_command").equals("SITE") && this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("PROXY_") && !this.thisSession.uiSG("the_command_data").toUpperCase().startsWith("PROXY_CONFIRM")) {
            this.thisSession.stop_idle_timer();
            this.thisSession.uiPUT(this.thisSession.uiSG("the_command_data").substring(0, this.thisSession.uiSG("the_command_data").indexOf(" ")).toLowerCase(), this.thisSession.uiSG("the_command_data").substring(this.thisSession.uiSG("the_command_data").indexOf(" ") + 1));
            this.thisSession.not_done = this.write_command("214", LOC.G("OK"));
            this.thisSession.start_idle_timer();
        } else {
            doProxySite = true;
        }
        if (doProxySite) {
            try {
                GenericClient c = this.thisSession.uVFS.getClient(this.thisSession.uVFS.get_item_parent(this.thisSession.uiSG("current_dir")));
                try {
                    String response = c.doCommand(String.valueOf(this.thisSession.uiSG("the_command")) + " " + original_the_command_data);
                    if (response == null || response.equals("")) {
                        this.thisSession.not_done = this.write_command("502", LOC.G("%unknown_command%"));
                        this.thisSession.doErrorEvent(new Exception(this.thisSession.uiSG("lastLog")));
                        this.thisSession.uiVG("failed_commands").addElement("" + new Date().getTime());
                    } else {
                        this.thisSession.not_done = this.write_command(response);
                    }
                }
                finally {
                    c = this.thisSession.uVFS.releaseClient(c);
                }
            }
            catch (NullPointerException e) {
                this.thisSession.not_done = this.write_command("502", LOC.G("%unknown_command%"));
            }
        }
    }
}

