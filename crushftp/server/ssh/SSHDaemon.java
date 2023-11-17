/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.maverick.events.Event
 *  com.maverick.events.EventListener
 *  com.maverick.nio.Daemon
 *  com.maverick.nio.DaemonContext
 *  com.maverick.nio.ProtocolContext
 *  com.maverick.ssh.SshException
 *  com.maverick.ssh.components.jce.AES128Cbc
 *  com.maverick.ssh.components.jce.AES128Ctr
 *  com.maverick.ssh.components.jce.AES128Gcm
 *  com.maverick.ssh.components.jce.AES192Cbc
 *  com.maverick.ssh.components.jce.AES192Ctr
 *  com.maverick.ssh.components.jce.AES256Cbc
 *  com.maverick.ssh.components.jce.AES256Ctr
 *  com.maverick.ssh.components.jce.AES256Gcm
 *  com.maverick.ssh.components.jce.ArcFour
 *  com.maverick.ssh.components.jce.ArcFour128
 *  com.maverick.ssh.components.jce.ArcFour256
 *  com.maverick.ssh.components.jce.BlowfishCbc
 *  com.maverick.ssh.components.jce.HmacRipeMd160
 *  com.maverick.ssh.components.jce.HmacRipeMd160ETM
 *  com.maverick.ssh.components.jce.JCEProvider
 *  com.maverick.ssh.components.jce.TripleDesCbc
 *  com.maverick.ssh.components.jce.TripleDesCtr
 *  com.maverick.sshd.AuthenticationMechanismFactory
 *  com.maverick.sshd.Authenticator
 *  com.maverick.sshd.Connection
 *  com.maverick.sshd.ForwardingPolicy
 *  com.maverick.sshd.KeyboardInteractiveAuthenticationProvider
 *  com.maverick.sshd.PasswordAuthenticationProvider
 *  com.maverick.sshd.PasswordKeyboardInteractiveProvider
 *  com.maverick.sshd.SshContext
 *  com.maverick.sshd.events.EventServiceImplementation
 *  com.maverick.sshd.platform.FileSystemFactory
 *  com.maverick.sshd.platform.KeyboardInteractiveProvider
 */
package crushftp.server.ssh;

import com.crushftp.client.File_S;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import com.maverick.events.Event;
import com.maverick.events.EventListener;
import com.maverick.logging.Log;
import com.maverick.nio.Daemon;
import com.maverick.nio.DaemonContext;
import com.maverick.nio.ProtocolContext;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.jce.AES128Cbc;
import com.maverick.ssh.components.jce.AES128Ctr;
import com.maverick.ssh.components.jce.AES128Gcm;
import com.maverick.ssh.components.jce.AES192Cbc;
import com.maverick.ssh.components.jce.AES192Ctr;
import com.maverick.ssh.components.jce.AES256Cbc;
import com.maverick.ssh.components.jce.AES256Ctr;
import com.maverick.ssh.components.jce.AES256Gcm;
import com.maverick.ssh.components.jce.ArcFour;
import com.maverick.ssh.components.jce.ArcFour128;
import com.maverick.ssh.components.jce.ArcFour256;
import com.maverick.ssh.components.jce.BlowfishCbc;
import com.maverick.ssh.components.jce.HmacRipeMd160;
import com.maverick.ssh.components.jce.HmacRipeMd160ETM;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh.components.jce.TripleDesCbc;
import com.maverick.ssh.components.jce.TripleDesCtr;
import com.maverick.sshd.AuthenticationMechanismFactory;
import com.maverick.sshd.Authenticator;
import com.maverick.sshd.Connection;
import com.maverick.sshd.ForwardingPolicy;
import com.maverick.sshd.KeyboardInteractiveAuthenticationProvider;
import com.maverick.sshd.PasswordAuthenticationProvider;
import com.maverick.sshd.PasswordKeyboardInteractiveProvider;
import com.maverick.sshd.SshContext;
import com.maverick.sshd.events.EventServiceImplementation;
import com.maverick.sshd.platform.FileSystemFactory;
import com.maverick.sshd.platform.KeyboardInteractiveProvider;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.server.ServerStatus;
import crushftp.server.ssh.LimitedAuthProvider;
import crushftp.server.ssh.PasswordAuthenticationProviderImpl;
import crushftp.server.ssh.PublicKeyVerifier;
import crushftp.server.ssh.SSHForwardingPolicy;
import crushftp.server.ssh.SSHServerSessionFactory;
import crushftp.server.ssh.SSH_ScpCommand;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class SSHDaemon
extends Daemon {
    static transient Object maverickConfiguredLock = new Object();
    public int localSSHPort = 0;
    public static boolean sshLoaded = false;
    public static transient Object lock = new Object();
    Properties server_item = null;
    SshContext sshContext = null;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SSHDaemon(Properties server_item) {
        this.server_item = server_item;
        System.setProperty("ssh.maxWindowSpace", String.valueOf(1024 * Integer.parseInt(server_item.getProperty("window_space", "4096"))));
        System.setProperty("maverick.disableProtocolViolation", "true");
        System.setProperty("maverick.disableRSARestrictions", String.valueOf(ServerStatus.BG("ssh_disable_rsa_checks")));
        try {
            Object object = lock;
            synchronized (object) {
                ServerSocket ss = new ServerSocket(0, 100, InetAddress.getByName("127.0.0.1"));
                this.localSSHPort = ss.getLocalPort();
                ss.close();
                server_item.put("ssh_local_port", String.valueOf(this.localSSHPort));
                if (!sshLoaded) {
                    sshLoaded = true;
                    EventServiceImplementation.getInstance().addListener(new EventListener(){

                        public void processEvent(Event evt) {
                            try {
                                if (evt == null) {
                                    return;
                                }
                                Throwable t = null;
                                String s = "";
                                if (evt.getAttribute("LOG_MESSAGE") != null) {
                                    s = evt.getAttribute("LOG_MESSAGE").toString();
                                }
                                if (evt.getAttribute("IP") != null) {
                                    s = String.valueOf(s) + ":" + evt.getAttribute("IP").toString();
                                }
                                if (evt.getAttribute("THROWABLE") != null) {
                                    t = (Throwable)evt.getAttribute("THROWABLE");
                                }
                                if (t != null) {
                                    String tt = "" + t;
                                    if (tt.indexOf("DELE-error") >= 0) {
                                        Log.log("SSH_SERVER", 0, tt);
                                    } else if (tt.indexOf("RMD-bad") >= 0) {
                                        Log.log("SSH_SERVER", 0, tt);
                                    } else {
                                        Log.log("SSH_SERVER", 2, t);
                                    }
                                }
                                if (s.equals("")) {
                                    return;
                                }
                                if (s.toUpperCase().indexOf("Failed to create".toUpperCase()) >= 0) {
                                    s = String.valueOf(s) + "#######Do you have the Java Strong cryptography policy files installed?";
                                }
                                s = String.valueOf(ServerStatus.thisObj.logDateFormat.format(new Date())) + "|" + s;
                                ServerStatus.thisObj.append_log(s, "ACCEPT");
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
        catch (Exception e) {
            Log.log("SSH_SERVER", 0, e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void configure(DaemonContext context) throws IOException {
        Object object = maverickConfiguredLock;
        synchronized (object) {
            com.maverick.logging.Log.getDefaultContext().shutdown();
            if (this.server_item.getProperty("ssh_debug_log", "false").equals("true")) {
                com.maverick.logging.Log.getDefaultContext().enableConsole(Log.Level.DEBUG);
            } else {
                com.maverick.logging.Log.getDefaultContext().enableConsole(Log.Level.NONE);
            }
            Log.log("SSH_SERVER", 0, "Configuring SSHD (" + Daemon.getVersion() + ")");
            try {
                JCEProvider.enableBouncyCastle((boolean)ServerStatus.BG("ssh_bouncycastle"));
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            this.sshContext = new SshContext((Daemon)this);
            if (ServerStatus.BG("fips140_sftp_server")) {
                try {
                    this.sshContext.enableFIPSMode();
                }
                catch (SshException e) {
                    throw new IOException(e);
                }
            }
            if (!this.server_item.getProperty("max_dh_size", "").trim().equals("") && !this.server_item.getProperty("max_dh_size", "").trim().equals("0")) {
                this.sshContext.setMaxDHGroupExchangeKeySize(Integer.parseInt(this.server_item.getProperty("max_dh_size", "1024")));
            }
            if (!this.server_item.getProperty("min_dh_size", "").trim().equals("") && !this.server_item.getProperty("min_dh_size", "").trim().equals("0")) {
                this.sshContext.setMinDHGroupExchangeKeySize(Integer.parseInt(this.server_item.getProperty("min_dh_size", "1024")));
            }
            if (this.sshContext.getMinDHGroupExchangeKeySize() > this.sshContext.getMaxDHGroupExchangeKeySize()) {
                this.sshContext.setMinDHGroupExchangeKeySize(this.sshContext.getMaxDHGroupExchangeKeySize());
            }
            this.sshContext.setForwardingPolicy((ForwardingPolicy)new SSHForwardingPolicy());
            this.sshContext.setLocale(Locale.US);
            int max_packet_size = Integer.parseInt(this.server_item.getProperty("max_packet_length", "70000"));
            if (max_packet_size < 32000) {
                max_packet_size = 70000;
            }
            this.server_item.put("max_packet_length", String.valueOf(max_packet_size));
            this.sshContext.setMaximumPacketLength(max_packet_size);
            this.sshContext.setMaxAuthentications(30);
            int max_channels = Integer.parseInt(this.server_item.getProperty("max_channels", "5"));
            if (max_channels < 1) {
                max_channels = 1;
            }
            this.sshContext.setChannelLimit(max_channels);
            try {
                String welcome_msg;
                if (!ServerStatus.BG("fips140_sftp_server")) {
                    String[] ciphers = this.server_item.getProperty("ssh_cipher_list", "aes128-ctr,aes192-ctr,aes256-ctr,3des-ctr,3des-cbc,blowfish-cbc,arcfour,arcfour128,arcfour256,aes128-gcm@openssh.com,aes256-gcm@openssh.com").split(",");
                    this.sshContext.supportedCiphersCS().clear();
                    this.sshContext.supportedCiphersSC().clear();
                    int x = 0;
                    while (x < ciphers.length) {
                        String c = ciphers[x].trim();
                        if (c.equalsIgnoreCase("blowfish-cbc")) {
                            this.sshContext.supportedCiphersCS().add("blowfish-cbc", BlowfishCbc.class);
                        } else if (c.equalsIgnoreCase("3des-cbc")) {
                            this.sshContext.supportedCiphersCS().add("3des-cbc", TripleDesCbc.class);
                        } else if (c.equalsIgnoreCase("aes128-ctr")) {
                            this.sshContext.supportedCiphersCS().add("aes128-ctr", AES128Ctr.class);
                        } else if (c.equalsIgnoreCase("aes128-cbc")) {
                            this.sshContext.supportedCiphersCS().add("aes128-cbc", AES128Cbc.class);
                        } else if (c.equalsIgnoreCase("aes192-ctr")) {
                            this.sshContext.supportedCiphersCS().add("aes192-ctr", AES192Ctr.class);
                        } else if (c.equalsIgnoreCase("aes192-cbc")) {
                            this.sshContext.supportedCiphersCS().add("aes192-cbc", AES192Cbc.class);
                        } else if (c.equalsIgnoreCase("aes256-ctr")) {
                            this.sshContext.supportedCiphersCS().add("aes256-ctr", AES256Ctr.class);
                        } else if (c.equalsIgnoreCase("aes256-cbc")) {
                            this.sshContext.supportedCiphersCS().add("aes256-cbc", AES256Cbc.class);
                        } else if (c.equalsIgnoreCase("arcfour")) {
                            this.sshContext.supportedCiphersCS().add("arcfour", ArcFour.class);
                        } else if (c.equalsIgnoreCase("arcfour128")) {
                            this.sshContext.supportedCiphersCS().add("arcfour128", ArcFour128.class);
                        } else if (c.equalsIgnoreCase("arcfour256")) {
                            this.sshContext.supportedCiphersCS().add("arcfour256", ArcFour256.class);
                        } else if (c.equalsIgnoreCase("3des-ctr")) {
                            this.sshContext.supportedCiphersCS().add("3des-ctr", TripleDesCtr.class);
                        } else if (c.equalsIgnoreCase("aes128-gcm@openssh.com")) {
                            this.sshContext.supportedCiphersCS().add("aes128-gcm@openssh.com", AES128Gcm.class);
                        } else if (c.equalsIgnoreCase("aes256-gcm@openssh.com")) {
                            this.sshContext.supportedCiphersCS().add("aes256-gcm@openssh.com", AES256Gcm.class);
                        }
                        if (c.equalsIgnoreCase("blowfish-cbc")) {
                            this.sshContext.supportedCiphersSC().add("blowfish-cbc", BlowfishCbc.class);
                        } else if (c.equalsIgnoreCase("3des-cbc")) {
                            this.sshContext.supportedCiphersSC().add("3des-cbc", TripleDesCbc.class);
                        } else if (c.equalsIgnoreCase("aes128-ctr")) {
                            this.sshContext.supportedCiphersSC().add("aes128-ctr", AES128Ctr.class);
                        } else if (c.equalsIgnoreCase("aes128-cbc")) {
                            this.sshContext.supportedCiphersSC().add("aes128-cbc", AES128Cbc.class);
                        } else if (c.equalsIgnoreCase("aes192-ctr")) {
                            this.sshContext.supportedCiphersSC().add("aes192-ctr", AES192Ctr.class);
                        } else if (c.equalsIgnoreCase("aes192-cbc")) {
                            this.sshContext.supportedCiphersSC().add("aes192-cbc", AES192Cbc.class);
                        } else if (c.equalsIgnoreCase("aes256-ctr")) {
                            this.sshContext.supportedCiphersSC().add("aes256-ctr", AES256Ctr.class);
                        } else if (c.equalsIgnoreCase("aes256-cbc")) {
                            this.sshContext.supportedCiphersSC().add("aes256-cbc", AES256Cbc.class);
                        } else if (c.equalsIgnoreCase("arcfour")) {
                            this.sshContext.supportedCiphersSC().add("arcfour", ArcFour.class);
                        } else if (c.equalsIgnoreCase("arcfour128")) {
                            this.sshContext.supportedCiphersSC().add("arcfour128", ArcFour128.class);
                        } else if (c.equalsIgnoreCase("arcfour256")) {
                            this.sshContext.supportedCiphersSC().add("arcfour256", ArcFour256.class);
                        } else if (c.equalsIgnoreCase("3des-ctr")) {
                            this.sshContext.supportedCiphersSC().add("3des-ctr", TripleDesCtr.class);
                        } else if (c.equalsIgnoreCase("aes128-gcm@openssh.com")) {
                            this.sshContext.supportedCiphersSC().add("aes128-gcm@openssh.com", AES128Gcm.class);
                        } else if (c.equalsIgnoreCase("aes256-gcm@openssh.com")) {
                            this.sshContext.supportedCiphersSC().add("aes256-gcm@openssh.com", AES256Gcm.class);
                        }
                        ++x;
                    }
                    String kex = this.server_item.getProperty("key_exchanges", "curve25519-sha256@libssh.org,diffie-hellman-group-exchange-sha256,diffie-hellman-group18-sha512,diffie-hellman-group17-sha512,diffie-hellman-group16-sha512,diffie-hellman-group15-sha512,diffie-hellman-group14-sha256,diffie-hellman-group14-sha1,ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521,diffie-hellman-group-exchange-sha1");
                    String[] supported_key = this.sshContext.supportedKeyExchanges().toArray();
                    int x2 = 0;
                    while (x2 < supported_key.length) {
                        if (kex.indexOf(supported_key[x2].toLowerCase()) < 0) {
                            this.sshContext.supportedKeyExchanges().remove(supported_key[x2]);
                        }
                        ++x2;
                    }
                    try {
                        this.sshContext.setPreferredCipherCS("aes128-ctr");
                    }
                    catch (Exception e) {
                        Log.log("SSH_SERVER", 2, e);
                    }
                    try {
                        this.sshContext.setPreferredCipherSC("aes128-ctr");
                    }
                    catch (Exception e) {
                        Log.log("SSH_SERVER", 2, e);
                    }
                    try {
                        this.sshContext.setPreferredCipherCS("aes256-ctr");
                    }
                    catch (Exception e) {
                        Log.log("SSH_SERVER", 3, e);
                    }
                    try {
                        this.sshContext.setPreferredCipherSC("aes256-ctr");
                    }
                    catch (Exception e) {
                        Log.log("SSH_SERVER", 3, e);
                    }
                }
                this.sshContext.setMaximumNumberofAsyncSFTPRequests(Integer.parseInt(this.server_item.getProperty("max_async_req", "200")));
                try {
                    this.sshContext.setPreferredMacCS("hmac-md5");
                    this.sshContext.setPreferredMacSC("hmac-md5");
                }
                catch (Exception ciphers) {
                    // empty catch block
                }
                String macs_list = this.server_item.getProperty("ssh_mac_list", "hmac-sha256,hmac-sha2-256,hmac-sha256@ssh.com,hmac-sha2-256-etm@openssh.com,hmac-sha2-256-96,hmac-sha512,hmac-sha2-512,hmac-sha512@ssh.com,hmac-sha2-512-etm@openssh.com,hmac-sha2-512-96,hmac-sha1,hmac-sha1-etm@openssh.com,hmac-sha1-96,hmac-ripemd160,hmac-ripemd160@openssh.com,hmac-ripemd160-etm@openssh.com,hmac-md5,hmac-md5-etm@openssh.com,hmac-md5-96").toLowerCase();
                macs_list = "," + macs_list + ",";
                macs_list = macs_list.replace(' ', ',');
                String[] macs = this.sshContext.supportedMacsCS().toArray();
                int x = 0;
                while (x < macs.length) {
                    String c = macs[x].trim().toLowerCase();
                    if (macs_list.indexOf("," + c + ",") < 0) {
                        this.sshContext.supportedMacsCS().remove(c);
                        this.sshContext.supportedMacsSC().remove(c);
                    }
                    ++x;
                }
                if (macs_list.toLowerCase().indexOf("hmac-ripemd160") >= 0 || macs_list.toLowerCase().indexOf("hmac-ripemd160@openssh.com") >= 0) {
                    this.sshContext.supportedMacsSC().add("hmac-ripemd160", HmacRipeMd160.class);
                    this.sshContext.supportedMacsCS().add("hmac-ripemd160", HmacRipeMd160.class);
                }
                if (macs_list.toLowerCase().indexOf("ripemd160-etm@openssh.com") >= 0) {
                    this.sshContext.supportedMacsSC().add("hmac-ripemd160-etm@openssh.com", HmacRipeMd160ETM.class);
                    this.sshContext.supportedMacsCS().add("hmac-ripemd160-etm@openssh.com", HmacRipeMd160ETM.class);
                }
                if (!(welcome_msg = this.server_item.getProperty("ftp_welcome_message", "").trim()).equals("")) {
                    welcome_msg = String.valueOf(welcome_msg) + "\r\n";
                }
                this.sshContext.setBannerMessage(welcome_msg);
                String rsa_key = String.valueOf(System.getProperty("crushftp.prefs")) + "ssh_host_rsa_key";
                if (this.server_item.get("ssh_rsa_key") != null) {
                    rsa_key = this.server_item.getProperty("ssh_rsa_key", "");
                }
                if (this.server_item.getProperty("ssh_rsa_enabled", "true").equals("true")) {
                    if (com.crushftp.client.Common.System2.containsKey("crushftp.keystores." + rsa_key.toString().toUpperCase().replace('\\', '/')) && !rsa_key.toString().equals("")) {
                        Properties p = (Properties)com.crushftp.client.Common.System2.get("crushftp.keystores." + rsa_key.toString().toUpperCase().replace('\\', '/'));
                        try {
                            this.sshContext.loadHostKey((InputStream)new ByteArrayInputStream((byte[])p.get("bytes")), "RSA", 1024);
                        }
                        catch (Exception e) {
                            try {
                                this.sshContext.loadHostKey((InputStream)new ByteArrayInputStream((byte[])p.get("bytes")), "RSA", 2048);
                            }
                            catch (Exception e1) {
                                this.sshContext.loadHostKey((InputStream)new ByteArrayInputStream((byte[])p.get("bytes")), "RSA", 4096);
                            }
                        }
                    } else if (ServerStatus.BG("v10_beta") && !rsa_key.toString().equals("") && (rsa_key.toUpperCase().startsWith("FILE://") || !new VRL(rsa_key).getProtocol().equalsIgnoreCase("FILE"))) {
                        try {
                            VRL vrl = new VRL(rsa_key);
                            GenericClient c = Common.getClient(Common.getBaseUrl(vrl.toString()), "SSHDaemmon", new Vector());
                            if (vrl.getConfig() != null && vrl.getConfig().size() > 0) {
                                c.setConfigObj(vrl.getConfig());
                            }
                            c.login(vrl.getUsername(), vrl.getPassword(), null);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            com.crushftp.client.Common.streamCopier(null, null, c.download(vrl.getPath(), 0L, -1L, true), baos, false, true, true);
                            try {
                                this.sshContext.loadHostKey((InputStream)new ByteArrayInputStream(baos.toByteArray()), "RSA", 1024);
                            }
                            catch (Exception e) {
                                try {
                                    this.sshContext.loadHostKey((InputStream)new ByteArrayInputStream(baos.toByteArray()), "RSA", 2048);
                                }
                                catch (Exception e1) {
                                    this.sshContext.loadHostKey((InputStream)new ByteArrayInputStream(baos.toByteArray()), "RSA", 4096);
                                }
                            }
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 0, e);
                        }
                    } else if (!rsa_key.toString().equals("")) {
                        try {
                            this.sshContext.loadOrGenerateHostKey((File)new File_S(rsa_key.toString()), "ssh-rsa", 4096);
                        }
                        catch (Exception e) {
                            try {
                                this.sshContext.loadOrGenerateHostKey((File)new File_S(rsa_key.toString()), "ssh-rsa", 2048);
                            }
                            catch (Exception e1) {
                                this.sshContext.loadOrGenerateHostKey((File)new File_S(rsa_key.toString()), "ssh-rsa", 1024);
                            }
                        }
                    }
                }
                String dsa_key = String.valueOf(System.getProperty("crushftp.prefs")) + "ssh_host_dsa_key";
                if (this.server_item.get("ssh_dsa_key") != null) {
                    dsa_key = this.server_item.getProperty("ssh_dsa_key", "");
                }
                if (this.server_item.getProperty("ssh_dsa_enabled", "true").equals("true")) {
                    if (com.crushftp.client.Common.System2.containsKey("crushftp.keystores." + dsa_key.toString().toUpperCase().replace('\\', '/'))) {
                        Properties p = (Properties)com.crushftp.client.Common.System2.get("crushftp.keystores." + dsa_key.toString().toUpperCase().replace('\\', '/'));
                        try {
                            this.sshContext.loadHostKey((InputStream)new ByteArrayInputStream((byte[])p.get("bytes")), "DSA", 1024);
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 0, e);
                            try {
                                this.sshContext.loadHostKey((InputStream)new ByteArrayInputStream((byte[])p.get("bytes")), "DSA", 2048);
                            }
                            catch (Exception e1) {
                                Log.log("SERVER", 0, e1);
                                this.sshContext.loadHostKey((InputStream)new ByteArrayInputStream((byte[])p.get("bytes")), "DSA", 4096);
                            }
                        }
                    } else if (ServerStatus.BG("v10_beta") && !dsa_key.toString().equals("") && (dsa_key.toUpperCase().startsWith("FILE://") || !new VRL(dsa_key).getProtocol().equalsIgnoreCase("FILE"))) {
                        try {
                            VRL vrl = new VRL(dsa_key);
                            GenericClient c = Common.getClient(Common.getBaseUrl(vrl.toString()), "SSHDaemmon", new Vector());
                            if (vrl.getConfig() != null && vrl.getConfig().size() > 0) {
                                c.setConfigObj(vrl.getConfig());
                            }
                            c.login(vrl.getUsername(), vrl.getPassword(), null);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            com.crushftp.client.Common.streamCopier(null, null, c.download(vrl.getPath(), 0L, -1L, true), baos, false, true, true);
                            try {
                                this.sshContext.loadHostKey((InputStream)new ByteArrayInputStream(baos.toByteArray()), "DSA", 1024);
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 0, e);
                                try {
                                    this.sshContext.loadHostKey((InputStream)new ByteArrayInputStream(baos.toByteArray()), "DSA", 2048);
                                }
                                catch (Exception e1) {
                                    Log.log("SERVER", 0, e1);
                                    this.sshContext.loadHostKey((InputStream)new ByteArrayInputStream(baos.toByteArray()), "DSA", 4096);
                                }
                            }
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 0, e);
                        }
                    } else if (dsa_key instanceof String && !dsa_key.toString().equals("")) {
                        try {
                            this.sshContext.loadOrGenerateHostKey((File)new File_S(dsa_key.toString()), "ssh-dss", 4096);
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 0, e);
                            try {
                                this.sshContext.loadOrGenerateHostKey((File)new File_S(dsa_key.toString()), "ssh-dss", 2048);
                            }
                            catch (Exception e1) {
                                Log.log("SERVER", 0, e1);
                                this.sshContext.loadOrGenerateHostKey((File)new File_S(dsa_key.toString()), "ssh-dss", 1024);
                            }
                        }
                    }
                }
                this.server_item.put("ssh_rsa_key", rsa_key);
                this.server_item.put("ssh_dsa_key", dsa_key);
                LimitedAuthProvider authFactory = new LimitedAuthProvider();
                authFactory.addProvider((Authenticator)new PasswordAuthenticationProviderImpl());
                authFactory.addProvider((Authenticator)new PublicKeyVerifier());
                if (this.server_item.getProperty("ssh_require_password", "false").equals("false")) {
                    authFactory.addProvider((Authenticator)new KeyboardInteractiveAuthenticationProvider(){

                        public KeyboardInteractiveProvider createInstance(Connection con) {
                            return new PasswordKeyboardInteractiveProvider(new PasswordAuthenticationProvider[]{new PasswordAuthenticationProviderImpl()}, con);
                        }
                    });
                }
                this.sshContext.setAuthenicationMechanismFactory((AuthenticationMechanismFactory)authFactory);
                this.sshContext.setFileSystemProvider((FileSystemFactory)new SSHServerSessionFactory());
                this.sshContext.addCommand("scp", SSH_ScpCommand.class);
            }
            catch (Throwable e) {
                e.printStackTrace();
                Log.log("SSH_SERVER", 0, e);
            }
            this.sshContext.setRequiredAuthenticationMethods(0);
            if (this.server_item.getProperty("ssh_require_password", "false").equals("true")) {
                this.sshContext.addRequiredAuthentication("password");
            }
            if (this.server_item.getProperty("ssh_require_publickey", "false").equals("true")) {
                this.sshContext.addRequiredAuthentication("publickey");
            }
            this.sshContext.setChannelLimit(1000);
            context.addListeningInterface("127.0.0.1", this.localSSHPort, (ProtocolContext)this.sshContext);
            this.sshContext.setRemoteForwardingCancelKillsTunnels(true);
            context.setPermanentTransferThreads(Integer.parseInt(this.server_item.getProperty("ssh_transfer_threads", "10")));
            context.setPermanentAcceptThreads(Integer.parseInt(this.server_item.getProperty("ssh_accept_threads", "10")));
            context.setPermanentConnectThreads(Integer.parseInt(this.server_item.getProperty("ssh_connect_threads", "10")));
            this.sshContext.setSoftwareVersionComments(ServerStatus.SG("ssh_header"));
            this.sshContext.setSocketOptionKeepAlive(true);
            this.sshContext.setSocketOptionTcpNoDelay(true);
            this.sshContext.setSocketOptionReuseAddress(true);
            this.sshContext.setSFTPCharsetEncoding(this.server_item.getProperty("ssh_text_encoding", "UTF8"));
            this.sshContext.setAllowDeniedKEX(true);
            this.sshContext.setSessionTimeout(Integer.parseInt(this.server_item.getProperty("ssh_session_timeout", "300")));
            if (!this.server_item.getProperty("ssh_keep_alive_interval", "").equals("")) {
                this.sshContext.setKeepAliveInterval(Integer.parseInt(this.server_item.getProperty("ssh_keep_alive_interval", "30")));
            }
            if (!this.server_item.getProperty("ssh_auth_timeout", "").equals("")) {
                this.sshContext.setIdleAuthenticationTimeoutSeconds(Integer.parseInt(this.server_item.getProperty("ssh_auth_timeout", "30")));
            }
            Log.log("SSH_SERVER", 0, "SSHD Configuration complete.");
        }
    }

    public void stop() {
        this.shutdownAsync(false, 1000L);
    }

    public static void setupDaemon(Properties server_item) {
        if (!server_item.containsKey("ssh_rsa_enabled")) {
            server_item.put("ssh_rsa_enabled", "false");
            server_item.put("ssh_dsa_enabled", "false");
            server_item.put("ssh_rsa_key", "ssh_host_rsa_key");
            server_item.put("ssh_dsa_key", "ssh_host_dsa_key");
            server_item.put("ssh_cipher_list", "aes128-ctr,3des-cbc,blowfish-cbc,arcfour128,arcfour");
            server_item.put("ssh_debug_log", "false");
            server_item.put("ssh_text_encoding", "UTF8");
            server_item.put("ssh_session_timeout", "300");
            server_item.put("ssh_async", "false");
            server_item.put("ssh_require_password", "false");
            server_item.put("ssh_require_publickey", "false");
            try {
                String home = System.getProperty("crushftp.home");
                String filename = String.valueOf(new File_S(home).getCanonicalPath()) + "/conf/server_host_key";
                if (new File_S(filename).exists()) {
                    RandomAccessFile ra = new RandomAccessFile(new File_S(filename), "r");
                    byte[] b = new byte[(int)ra.length()];
                    ra.readFully(b);
                    ra.close();
                    String key = new String(b);
                    if (key.indexOf("bit dsa") >= 0) {
                        Common.copy(filename, String.valueOf(new File_S(home).getCanonicalPath()) + "/ssh_host_dsa_key", false);
                        server_item.put("ssh_dsa_enabled", "true");
                    } else {
                        Common.copy(filename, String.valueOf(new File_S(home).getCanonicalPath()) + "/ssh_host_rsa_key", false);
                        server_item.put("ssh_rsa_enabled", "true");
                    }
                } else {
                    server_item.put("ssh_dsa_enabled", "true");
                    server_item.put("ssh_rsa_enabled", "true");
                }
            }
            catch (Exception ee) {
                Log.log("SSH_SERVER", 0, ee);
            }
        }
    }
}

