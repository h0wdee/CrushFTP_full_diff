/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.maverick.logging.Log
 *  com.maverick.logging.Log$Level
 *  com.maverick.logging.RootLoggerContext
 *  com.maverick.nio.Daemon
 *  com.maverick.nio.DaemonContext
 *  com.maverick.nio.ProtocolContext
 *  com.maverick.sshd.AuthenticationMechanismFactory
 *  com.maverick.sshd.Authenticator
 *  com.maverick.sshd.Connection
 *  com.maverick.sshd.ForwardingPolicy
 *  com.maverick.sshd.KeyboardInteractiveAuthenticationProvider
 *  com.maverick.sshd.PasswordAuthenticationProvider
 *  com.maverick.sshd.PasswordKeyboardInteractiveProvider
 *  com.maverick.sshd.RequiredAuthenticationStrategy
 *  com.maverick.sshd.SshContext
 *  com.maverick.sshd.platform.DisplayAwareKeyboardInteractiveProvider
 *  com.maverick.sshd.platform.FileSystemFactory
 */
package crushftp.server.ssh;

import com.crushftp.client.File_S;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import com.maverick.logging.Log;
import com.maverick.logging.RootLoggerContext;
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
import com.maverick.ssh.components.jce.ChaCha20Poly1305;
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
import com.maverick.sshd.RequiredAuthenticationStrategy;
import com.maverick.sshd.SshContext;
import com.maverick.sshd.platform.DisplayAwareKeyboardInteractiveProvider;
import com.maverick.sshd.platform.FileSystemFactory;
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
                    com.maverick.logging.Log.setDefaultContext((RootLoggerContext)new RootLoggerContext(){

                        public void raw(Log.Level level, String msg) {
                        }

                        public void newline() {
                        }

                        public void log(Log.Level level, String msg, Throwable e, Object ... args) {
                            if (msg != null && msg.indexOf("SSH_MSG_IGNORE") >= 0) {
                                return;
                            }
                            if (msg != null && msg.indexOf("There are now ") >= 0) {
                                return;
                            }
                            if (e != null) {
                                Log.log("SSH_SERVER", 2, e);
                            }
                            String log_tag = "SSH_SERVER";
                            if (msg != null && msg.indexOf(" - ") >= 0) {
                                log_tag = "SSH_CLIENT";
                            }
                            if (level == Log.Level.TRACE) {
                                Log.log(log_tag, 3, msg);
                            } else if (level == Log.Level.DEBUG) {
                                Log.log(log_tag, 2, msg);
                            } else if (level == Log.Level.WARN) {
                                Log.log(log_tag, 1, msg);
                            } else if (level == Log.Level.INFO || level == Log.Level.NONE) {
                                Log.log(log_tag, 0, msg);
                            }
                        }

                        public boolean isLogging(Log.Level level) {
                            return true;
                        }

                        public void close() {
                        }

                        public void shutdown() {
                        }

                        public String getProperty(String key, String defaultValue) {
                            return "";
                        }

                        public void enableConsole(Log.Level level) {
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
                JCEProvider.enableBouncyCastle(ServerStatus.BG("ssh_bouncycastle"));
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
            this.sshContext.setSHA1SignaturesSupported(this.server_item.getProperty("rsa_sha1_signatures", "true").equals("true"));
            try {
                String welcome_msg;
                if (!ServerStatus.BG("fips140_sftp_server")) {
                    String[] ciphers = this.server_item.getProperty("ssh_cipher_list", "aes128-ctr,aes192-ctr,aes256-ctr,3des-ctr,3des-cbc,blowfish-cbc,arcfour,arcfour128,arcfour256,aes128-gcm@openssh.com,aes256-gcm@openssh.com,chacha20-poly1305@openssh.com").split(",");
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
                        } else if (c.equalsIgnoreCase("chacha20poly1305@openssh.com")) {
                            this.sshContext.supportedCiphersCS().add("chacha20poly1305@openssh.com", ChaCha20Poly1305.class);
                        } else if (c.equalsIgnoreCase("chacha20-poly1305@openssh.com")) {
                            this.sshContext.supportedCiphersCS().add("chacha20-poly1305@openssh.com", ChaCha20Poly1305.class);
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
                        } else if (c.equalsIgnoreCase("chacha20poly1305@openssh.com")) {
                            this.sshContext.supportedCiphersSC().add("chacha20poly1305@openssh.com", ChaCha20Poly1305.class);
                        } else if (c.equalsIgnoreCase("chacha20-poly1305@openssh.com")) {
                            this.sshContext.supportedCiphersSC().add("chacha20-poly1305@openssh.com", ChaCha20Poly1305.class);
                        }
                        ++x;
                    }
                    String kex = this.server_item.getProperty("key_exchanges", "curve25519-sha2@libssh.org,curve25519-sha256@libssh.org,diffie-hellman-group-exchange-sha256,diffie-hellman-group18-sha512,diffie-hellman-group17-sha512,diffie-hellman-group16-sha512,diffie-hellman-group15-sha512,diffie-hellman-group14-sha256,diffie-hellman-group14-sha1,ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521,diffie-hellman-group-exchange-sha1");
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
                if (!ServerStatus.BG("fips140_sftp_server")) {
                    if (macs_list.toLowerCase().indexOf("hmac-ripemd160") >= 0 || macs_list.toLowerCase().indexOf("hmac-ripemd160@openssh.com") >= 0) {
                        this.sshContext.supportedMacsSC().add("hmac-ripemd160", HmacRipeMd160.class);
                        this.sshContext.supportedMacsCS().add("hmac-ripemd160", HmacRipeMd160.class);
                    }
                    if (macs_list.toLowerCase().indexOf("ripemd160-etm@openssh.com") >= 0) {
                        this.sshContext.supportedMacsSC().add("hmac-ripemd160-etm@openssh.com", HmacRipeMd160ETM.class);
                        this.sshContext.supportedMacsCS().add("hmac-ripemd160-etm@openssh.com", HmacRipeMd160ETM.class);
                    }
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
                            this.loadKey(new ByteArrayInputStream((byte[])p.get("bytes")), "ssh-rsa", "4096,2048,1024");
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 0, e);
                        }
                    } else if (!rsa_key.toString().equals("")) {
                        String rsa_key_url = rsa_key;
                        if (new VRL(rsa_key_url).getProtocol().equalsIgnoreCase("FILE")) {
                            try {
                                this.loadOrGenerateKey(new VRL(rsa_key_url).getCanonicalPath().replace('\\', '/'), "ssh-rsa", "4096,2048,1024");
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 0, e);
                            }
                        } else {
                            try {
                                VRL vrl = new VRL(rsa_key_url);
                                GenericClient c = Common.getClient(Common.getBaseUrl(vrl.toString()), "SSHDaemmon", new Vector());
                                if (vrl.getConfig() != null && vrl.getConfig().size() > 0) {
                                    c.setConfigObj(vrl.getConfig());
                                }
                                c.login(vrl.getUsername(), vrl.getPassword(), null);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                com.crushftp.client.Common.streamCopier(null, null, c.download(vrl.getPath(), 0L, -1L, true, true), baos, false, true, true);
                                try {
                                    this.loadKey(new ByteArrayInputStream(baos.toByteArray()), "ssh-rsa", "4096,2048,1024");
                                }
                                catch (Exception e) {
                                    Log.log("SERVER", 0, e);
                                }
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 0, e);
                            }
                        }
                    }
                }
                String dsa_key = String.valueOf(System.getProperty("crushftp.prefs")) + "ssh_host_dsa_key";
                if (this.server_item.get("ssh_dsa_key") != null) {
                    dsa_key = this.server_item.getProperty("ssh_dsa_key", "");
                }
                if (this.server_item.getProperty("ssh_dsa_enabled", "false").equals("true")) {
                    if (com.crushftp.client.Common.System2.containsKey("crushftp.keystores." + dsa_key.toString().toUpperCase().replace('\\', '/'))) {
                        Properties p = (Properties)com.crushftp.client.Common.System2.get("crushftp.keystores." + dsa_key.toString().toUpperCase().replace('\\', '/'));
                        try {
                            this.loadKey(new ByteArrayInputStream((byte[])p.get("bytes")), "ssh-dss", "4096,2048,1024");
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 0, e);
                        }
                    } else if (!dsa_key.toString().equals("")) {
                        String dsa_key_url = dsa_key;
                        if (new VRL(dsa_key_url).getProtocol().equalsIgnoreCase("FILE")) {
                            try {
                                this.loadOrGenerateKey(new VRL(dsa_key_url).getCanonicalPath().replace('\\', '/'), "ssh-dss", "4096,2048,1024");
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 0, e);
                            }
                        } else {
                            try {
                                VRL vrl = new VRL(dsa_key_url);
                                GenericClient c = Common.getClient(Common.getBaseUrl(vrl.toString()), "SSHDaemmon", new Vector());
                                if (vrl.getConfig() != null && vrl.getConfig().size() > 0) {
                                    c.setConfigObj(vrl.getConfig());
                                }
                                c.login(vrl.getUsername(), vrl.getPassword(), null);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                com.crushftp.client.Common.streamCopier(null, null, c.download(vrl.getPath(), 0L, -1L, true, true), baos, false, true, true);
                                this.loadKey(new ByteArrayInputStream(baos.toByteArray()), "ssh-dss", "4096,2048,1024");
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 0, e);
                            }
                        }
                    }
                }
                String ed25519_key = String.valueOf(System.getProperty("crushftp.prefs")) + "ssh_host_ed25519_key";
                if (this.server_item.get("ssh_ed25519_key") != null) {
                    ed25519_key = this.server_item.getProperty("ssh_ed25519_key", "");
                }
                this.server_item.put("ssh_ed25519_enabled", this.server_item.getProperty("ssh_ed25519_enabled", "false"));
                if (this.server_item.getProperty("ssh_ed25519_enabled", "false").equals("true")) {
                    if (com.crushftp.client.Common.System2.containsKey("crushftp.keystores." + ed25519_key.toString().toUpperCase().replace('\\', '/'))) {
                        Properties p = (Properties)com.crushftp.client.Common.System2.get("crushftp.keystores." + ed25519_key.toString().toUpperCase().replace('\\', '/'));
                        try {
                            this.loadKey(new ByteArrayInputStream((byte[])p.get("bytes")), "ed25519", "65535");
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 0, e);
                        }
                    } else if (!ed25519_key.toString().equals("")) {
                        String ed25519_key_url = ed25519_key;
                        if (new VRL(ed25519_key_url).getProtocol().equalsIgnoreCase("FILE")) {
                            try {
                                this.loadOrGenerateKey(new VRL(ed25519_key_url).getCanonicalPath().replace('\\', '/'), "ed25519", "65535");
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 0, e);
                            }
                        } else {
                            try {
                                VRL vrl = new VRL(ed25519_key_url);
                                GenericClient c = Common.getClient(Common.getBaseUrl(vrl.toString()), "SSHDaemmon", new Vector());
                                if (vrl.getConfig() != null && vrl.getConfig().size() > 0) {
                                    c.setConfigObj(vrl.getConfig());
                                }
                                c.login(vrl.getUsername(), vrl.getPassword(), null);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                com.crushftp.client.Common.streamCopier(null, null, c.download(vrl.getPath(), 0L, -1L, true, true), baos, false, true, true);
                                this.loadKey(new ByteArrayInputStream(baos.toByteArray()), "ed25519", "65535");
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 0, e);
                            }
                        }
                    }
                }
                String ecdsa_key = String.valueOf(System.getProperty("crushftp.prefs")) + "ssh_host_ecdsa_key";
                if (this.server_item.get("ssh_ecdsa_key") != null) {
                    ecdsa_key = this.server_item.getProperty("ssh_ecdsa_key", "");
                }
                this.server_item.put("ssh_ecdsa_enabled", this.server_item.getProperty("ssh_ecdsa_enabled", "false"));
                if (this.server_item.getProperty("ssh_ecdsa_enabled", "false").equals("true")) {
                    if (com.crushftp.client.Common.System2.containsKey("crushftp.keystores." + ecdsa_key.toString().toUpperCase().replace('\\', '/'))) {
                        Properties p = (Properties)com.crushftp.client.Common.System2.get("crushftp.keystores." + ecdsa_key.toString().toUpperCase().replace('\\', '/'));
                        try {
                            this.loadKey(new ByteArrayInputStream((byte[])p.get("bytes")), "ecdsa", "521,384,256");
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 0, e);
                        }
                    } else if (!ecdsa_key.toString().equals("")) {
                        String ecdsa_key_url = ecdsa_key;
                        if (new VRL(ecdsa_key_url).getProtocol().equalsIgnoreCase("FILE")) {
                            try {
                                this.loadOrGenerateKey(new VRL(ecdsa_key_url).getCanonicalPath().replace('\\', '/'), "ecdsa", "521,384,256");
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 0, e);
                            }
                        } else {
                            try {
                                VRL vrl = new VRL(ecdsa_key_url);
                                GenericClient c = Common.getClient(Common.getBaseUrl(vrl.toString()), "SSHDaemmon", new Vector());
                                if (vrl.getConfig() != null && vrl.getConfig().size() > 0) {
                                    c.setConfigObj(vrl.getConfig());
                                }
                                c.login(vrl.getUsername(), vrl.getPassword(), null);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                com.crushftp.client.Common.streamCopier(null, null, c.download(vrl.getPath(), 0L, -1L, true, true), baos, false, true, true);
                                this.loadKey(new ByteArrayInputStream(baos.toByteArray()), "ecdsa", "521,384,256");
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 0, e);
                            }
                        }
                    }
                }
                this.server_item.put("ssh_rsa_key", rsa_key);
                this.server_item.put("ssh_dsa_key", dsa_key);
                this.server_item.put("ssh_ecdsa_key", ecdsa_key);
                this.server_item.put("ssh_ed25519_key", ed25519_key);
                LimitedAuthProvider authFactory = new LimitedAuthProvider();
                authFactory.addProvider((Authenticator)new PublicKeyVerifier());
                authFactory.addProvider((Authenticator)new PasswordAuthenticationProviderImpl());
                if (this.server_item.getProperty("ssh_require_password", "false").equals("false")) {
                    authFactory.addProvider((Authenticator)new KeyboardInteractiveAuthenticationProvider(){

                        public DisplayAwareKeyboardInteractiveProvider createInstance(Connection con) {
                            class PasswordKeyboardInteractiveProviderCrush
                            extends PasswordKeyboardInteractiveProvider {
                                public PasswordKeyboardInteractiveProviderCrush(PasswordAuthenticationProvider[] providers, Connection con) {
                                    super(providers, con);
                                }

                                public String getInstructions(String username) {
                                    return "";
                                }

                                public String getDisplayName() {
                                    return "";
                                }

                                public String getName() {
                                    return "password";
                                }
                            }
                            return new PasswordKeyboardInteractiveProviderCrush(new PasswordAuthenticationProvider[]{new PasswordAuthenticationProviderImpl()}, con);
                        }
                    });
                }
                this.sshContext.setRequiredAuthenticationStrategy(RequiredAuthenticationStrategy.ONCE_PER_AUTHENTICATION_ATTEMPT);
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
            this.sshContext.setDisableSFTPDirChecks(ServerStatus.BG("sftp_mkdir_exist_silent"));
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

    public void loadKey(InputStream in, String type, String bits) throws Exception {
        Exception last_e = null;
        int x = 0;
        while (x < bits.split(",").length) {
            try {
                this.sshContext.loadHostKey(in, type, Integer.parseInt(bits.split(",")[x]));
                last_e = null;
                break;
            }
            catch (Exception e) {
                last_e = e;
                ++x;
            }
        }
        if (last_e != null) {
            throw last_e;
        }
    }

    public void loadOrGenerateKey(String path, String type, String bits) throws Exception {
        Exception last_e = null;
        int x = 0;
        while (x < bits.split(",").length) {
            try {
                this.sshContext.loadOrGenerateHostKey((File)new File_S(path), type, Integer.parseInt(bits.split(",")[x]));
                last_e = null;
                break;
            }
            catch (Exception e) {
                last_e = e;
                ++x;
            }
        }
        if (last_e != null) {
            throw last_e;
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

