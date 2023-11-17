/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.BCProxy;
import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.SFTPHostKeyVerifier;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import com.maverick.events.Event;
import com.maverick.events.EventListener;
import com.maverick.events.J2SSHEventCodes;
import com.maverick.sftp.SftpFile;
import com.maverick.sftp.SftpFileAttributes;
import com.maverick.sftp.SftpStatusException;
import com.maverick.ssh.LicenseManager;
import com.maverick.ssh.PasswordAuthentication;
import com.maverick.ssh.PublicKeyAuthentication;
import com.maverick.ssh.SecurityLevel;
import com.maverick.ssh.SshConnector;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshKeyPair;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh2.Ssh2Client;
import com.maverick.ssh2.Ssh2Context;
import com.maverick.util.UnsignedInteger64;
import com.sshtools.net.SocketWrapper;
import com.sshtools.publickey.SshPrivateKeyFile;
import com.sshtools.publickey.SshPrivateKeyFileFactory;
import com.sshtools.sftp.SftpClient;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.Socket;
import java.security.Provider;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

public class SFTPClient
extends GenericClient {
    private SftpClient sftp;
    private Ssh2Client session;
    Ssh2Context ssh2Context;
    String uniqueId;
    Vector recent_mkdirs;
    public static Object ssh_bug_lock;
    static boolean added_bc;
    Socket ssh_socket;
    public Vector created_stack;
    static Properties created_clients;

    static {
        LicenseManager.addLicense("----BEGIN 3SP LICENSE----\r\nProduct : Maverick Legacy Client\r\nLicensee: Ben Spink\r\nComments: Standard Support\r\nType    : Standard Support (Runtime License)\r\nCreated : 23-Jul-2023\r\n\r\n378720803B9DC65BA600F3CF9CCEF4C80FCAB0ADF46C8024\r\n0557D5369C1819468AFFBF224CC9FAFEAC09AE47E4069341\r\n4E4C3BB0E787F87F849493402C5FB1544ABB21CB5E5A4AC8\r\n65B21E01BAEF67EAA71C03B9CE3E82632E8DB164407F1D5E\r\n481E68EE4444A90F5B17DE69FCA8D3B500EC24133F1C494B\r\n07DB2F3A7E6BA6168C07A649BCF42A8AEBB3CB014488CAB8\r\n----END 3SP LICENSE----\r\n");
        ssh_bug_lock = new Object();
        added_bc = false;
        created_clients = new Properties();
    }

    public SFTPClient(String url, String header, Vector log) {
        block5: {
            super(header, log);
            this.sftp = null;
            this.session = null;
            this.ssh2Context = null;
            this.uniqueId = Common.makeBoundary(10);
            this.recent_mkdirs = new Vector();
            this.ssh_socket = null;
            this.created_stack = new Vector();
            this.fields = new String[]{"sftp_compress", "username", "password", "clientid", "knownHostFile", "verifyHost", "addNewHost", "custom_*", "ssh_private_key", "privateKeyFilePath", "timeout", "ssh_two_factor", "twoFactorAuthentication", "dot_default_dir", "default_dir", "sftp_7_token", "sftp_11_token", "simple", "*script", "prepend_home_path"};
            this.url = url;
            if (!added_bc && System.getProperty("crushftp.ssh_bouncycastle", "true").equals("true")) {
                added_bc = true;
                try {
                    Provider provider = (Provider)BCProxy.instance().loader.loadClass("org.bouncycastle.jce.provider.BouncyCastleProvider").getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                    if (!Common.added_bc) {
                        Security.addProvider(provider);
                        Common.added_bc = true;
                    }
                    JCEProvider.enableBouncyCastle(System.getProperty("crushftp.ssh_bouncycastle", "true").equals("true"), JCEProvider.BC_FLAVOR.BC, provider);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    if (log == null) break block5;
                    log.addElement("" + e);
                }
            }
        }
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        this.created_stack.addElement("" + new Date());
        this.created_stack.addElement(Thread.currentThread().getName());
        int xx = 0;
        while (xx < ste.length) {
            String s = String.valueOf(ste[xx].getClassName()) + "." + ste[xx].getMethodName() + "(" + ste[xx].getFileName() + ":" + ste[xx].getLineNumber() + "),";
            this.created_stack.addElement(s);
            ++xx;
        }
        created_clients.put(this.uniqueId, this.created_stack);
    }

    private void setThreadName() {
        String cur_name = Thread.currentThread().getName();
        if (cur_name.indexOf("|") >= 0) {
            cur_name = cur_name.substring(cur_name.indexOf("|") + 1);
        }
        if (!Thread.currentThread().getName().startsWith(this.uniqueId)) {
            Thread.currentThread().setName(String.valueOf(this.uniqueId) + "|" + cur_name);
        }
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        this.setThreadName();
        this.config.put("username", username);
        this.config.put("password", password);
        if (clientid != null) {
            this.config.put("clientid", clientid);
        }
        SshConnector.addEventListener(this.uniqueId, new SftpLogger());
        this.reconnect();
        return "";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void reconnect() throws Exception {
        try {
            if (this.sftp != null && !this.sftp.isClosed()) {
                return;
            }
            if (this.sftp != null) {
                this.log("SSH_CLIENT", 0, "Reconnecting disconnected SFTP connection...");
            } else {
                this.log("SSH_CLIENT", 2, "Connecting new SFTP connection...(" + SshConnector.getVersion() + ")");
            }
            SshConnector con = null;
            Object object = ssh_bug_lock;
            synchronized (object) {
                con = SshConnector.createInstance(SecurityLevel.WEAK, false);
                con.setSoftwareVersionComments(String.valueOf(System.getProperty("appname", "CrushFTP")) + "_java");
                this.ssh2Context = (Ssh2Context)con.getContext(2);
                if (System.getProperty("crushftp.fips140_sftp_client", "false").equals("true")) {
                    this.ssh2Context.enableFIPSMode();
                }
            }
            if (this.config.getProperty("sftp_compress", "false").equals("true")) {
                this.ssh2Context.enableCompression();
            }
            SFTPHostKeyVerifier sftphv = new SFTPHostKeyVerifier(this.config.getProperty("knownHostFile"), this.config.getProperty("verifyHost", "false").equalsIgnoreCase("true"), this.config.getProperty("addNewHost", "false").equalsIgnoreCase("true"));
            this.ssh2Context.setHostKeyVerification(sftphv);
            this.ssh2Context.setPreferredPublicKey("ssh-dss");
            this.ssh2Context.setCipherPreferredPositionCS("aes128-ctr", 0);
            this.ssh2Context.setCipherPreferredPositionSC("aes128-ctr", 0);
            this.ssh2Context.setSocketTimeout(600000);
            this.ssh2Context.setIdleConnectionTimeoutSeconds(600);
            this.ssh2Context.setPreferredPublicKey("ssh-rsa");
            this.ssh2Context.setPublicKeyPreferredPosition("ssh-rsa", 0);
            this.ssh2Context.setPreferredKeyExchange("diffie-hellman-group14-sha1");
            this.ssh2Context.setKeyExchangePreferredPosition("diffie-hellman-group14-sha1", 0);
            if (this.config.containsKey("custom_setKeyReExchangeDisabled")) {
                this.ssh2Context.setKeyReExchangeDisabled(this.config.getProperty("custom_setKeyReExchangeDisabled", "").equals("true"));
            }
            if (this.config.containsKey("custom_setIdleConnectionTimeoutSeconds")) {
                this.ssh2Context.setIdleConnectionTimeoutSeconds(Integer.parseInt(this.config.getProperty("custom_setIdleConnectionTimeoutSeconds", "600")));
                this.ssh2Context.setSocketTimeout(this.ssh2Context.getIdleConnectionTimeoutSeconds() * 1000);
                this.ssh2Context.setTreatIdleConnectionAsError(false);
            }
            if (this.config.getProperty("custom_enableCompression", "false").equals("true")) {
                this.ssh2Context.enableCompression();
            }
            if (this.config.getProperty("custom_enableFIPSMode", "false").equals("true")) {
                this.ssh2Context.enableFIPSMode();
            }
            if (this.config.containsKey("custom_setDHGroupExchangeBackwardsCompatible")) {
                this.ssh2Context.setDHGroupExchangeBackwardsCompatible(this.config.getProperty("custom_setDHGroupExchangeBackwardsCompatible", "").equals("true"));
            }
            if (!System.getProperties().getProperty("crushftp.ssh_client_key_exchanges", "").equals("")) {
                String kex = System.getProperties().getProperty("crushftp.ssh_client_key_exchanges", "curve25519-sha2@libssh.org,curve25519-sha256@libssh.org,diffie-hellman-group-exchange-sha256,diffie-hellman-group18-sha512,diffie-hellman-group17-sha512,diffie-hellman-group16-sha512,diffie-hellman-group15-sha512,diffie-hellman-group14-sha256,diffie-hellman-group14-sha1,ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521,diffie-hellman-group-exchange-sha1").toLowerCase();
                String[] supported_key = this.ssh2Context.supportedKeyExchanges().toArray();
                int x = 0;
                while (x < supported_key.length) {
                    if (kex.indexOf(supported_key[x].toLowerCase()) < 0) {
                        this.ssh2Context.supportedKeyExchanges().remove(supported_key[x]);
                    }
                    ++x;
                }
            }
            if (!System.getProperties().getProperty("crushftp.ssh_client_cipher_list", "").equals("")) {
                String ciphers = System.getProperties().getProperty("crushftp.ssh_client_cipher_list", "aes128-ctr,aes192-ctr,aes256-ctr,3des-ctr,3des-cbc,blowfish-cbc,arcfour,arcfour128,arcfour256,aes128-gcm@openssh.com,aes256-gcm@openssh.com,chacha20-poly1305@openssh.com");
                String[] supported_ciphersCS = this.ssh2Context.supportedCiphersCS().toArray();
                String[] supported_ciphersSC = this.ssh2Context.supportedCiphersSC().toArray();
                int x = 0;
                while (x < supported_ciphersCS.length) {
                    if (ciphers.indexOf(supported_ciphersCS[x].toLowerCase()) < 0) {
                        this.ssh2Context.supportedCiphersCS().remove(supported_ciphersCS[x]);
                    }
                    ++x;
                }
                x = 0;
                while (x < supported_ciphersSC.length) {
                    if (ciphers.indexOf(supported_ciphersSC[x].toLowerCase()) < 0) {
                        this.ssh2Context.supportedCiphersSC().remove(supported_ciphersSC[x]);
                    }
                    ++x;
                }
            }
            if (!System.getProperties().getProperty("crushftp.ssh_client_mac_list", "").equals("")) {
                String macs_list = System.getProperties().getProperty("crushftp.ssh_client_mac_list", "hmac-sha256,hmac-sha2-256,hmac-sha256@ssh.com,hmac-sha2-256-etm@openssh.com,hmac-sha2-256-96,hmac-sha512,hmac-sha2-512,hmac-sha512@ssh.com,hmac-sha2-512-etm@openssh.com,hmac-sha2-512-96,hmac-sha1,hmac-sha1-etm@openssh.com,hmac-sha1-96,hmac-ripemd160,hmac-ripemd160@openssh.com,hmac-ripemd160-etm@openssh.com,hmac-md5,hmac-md5-etm@openssh.com,hmac-md5-96").toLowerCase();
                macs_list = "," + macs_list + ",";
                macs_list = macs_list.replace(' ', ',');
                String[] macs = this.ssh2Context.supportedMacsCS().toArray();
                int x = 0;
                while (x < macs.length) {
                    String c = macs[x].trim().toLowerCase();
                    if (macs_list.indexOf("," + c + ",") < 0) {
                        this.ssh2Context.supportedMacsCS().remove(c);
                        this.ssh2Context.supportedMacsSC().remove(c);
                    }
                    ++x;
                }
            }
            if (this.config.containsKey("custom_preferredCipher")) {
                this.ssh2Context.setPreferredCipherCS(this.config.getProperty("custom_preferredCipher", ""));
                this.ssh2Context.setPreferredCipherSC(this.config.getProperty("custom_preferredCipher", ""));
                this.ssh2Context.setCipherPreferredPositionCS(this.config.getProperty("custom_preferredCipher", ""), 0);
                this.ssh2Context.setCipherPreferredPositionSC(this.config.getProperty("custom_preferredCipher", ""), 0);
            }
            if (this.config.containsKey("custom_setDHGroupExchangeKeySize")) {
                this.ssh2Context.setDHGroupExchangeKeySize(Integer.parseInt(this.config.getProperty("custom_setDHGroupExchangeKeySize", "")));
            }
            if (this.config.containsKey("custom_preferredKex")) {
                this.ssh2Context.setPreferredKeyExchange(this.config.getProperty("custom_preferredKex", ""));
                this.ssh2Context.setKeyExchangePreferredPosition(this.config.getProperty("custom_preferredKex", ""), 0);
            }
            if (this.config.containsKey("custom_setUseRSAKey") && this.config.getProperty("custom_setUseRSAKey", "true").equals("false")) {
                this.ssh2Context.setPreferredPublicKey("ssh-dss");
                this.ssh2Context.setPublicKeyPreferredPosition("ssh-dss", 0);
            }
            if (this.config.containsKey("custom_preferredMac")) {
                this.ssh2Context.setPreferredMacCS(this.config.getProperty("custom_preferredMac", ""));
                this.ssh2Context.setPreferredMacSC(this.config.getProperty("custom_preferredMac", ""));
            }
            if (this.config.containsKey("custom_setMaximumPacketLength")) {
                this.ssh2Context.setMaximumPacketLength(Integer.parseInt(this.config.getProperty("custom_setMaximumPacketLength", "35000")));
            }
            if (this.config.containsKey("custom_setSessionMaxPacketSize")) {
                this.ssh2Context.setSessionMaxPacketSize(Integer.parseInt(this.config.getProperty("custom_setSessionMaxPacketSize", "35000")));
                this.ssh2Context.setSftpMaxPacketSize(Integer.parseInt(this.config.getProperty("custom_setSessionMaxPacketSize", "35000")));
            }
            if (this.config.containsKey("custom_setSessionMaxWindowSpace")) {
                this.ssh2Context.setSessionMaxWindowSpace(Integer.parseInt(this.config.getProperty("custom_setSessionMaxWindowSpace", "3000000")));
                this.ssh2Context.setSftpMaxWindowSpace(Integer.parseInt(this.config.getProperty("custom_setSessionMaxWindowSpace", "3000000")));
            }
            if (this.config.containsKey("custom_enableETM")) {
                if (this.config.getProperty("custom_enableETM", "false").equals("true")) {
                    this.ssh2Context.enableETM();
                } else {
                    this.ssh2Context.disableETM();
                }
            }
            if (this.config.containsKey("custom_enableNonStandardAlgorithms")) {
                if (this.config.getProperty("custom_enableNonStandardAlgorithms", "false").equals("true")) {
                    this.ssh2Context.enableNonStandardAlgorithms();
                } else {
                    this.ssh2Context.disableNonStandardAlgorithms();
                }
            }
            VRL u = new VRL(this.url);
            sftphv.setHost(u.getHost());
            this.log("SSH_CLIENT", 0, "Connecting to:" + u.getHost() + ":" + u.getPort() + " with socket timeout:" + this.ssh2Context.getSocketTimeout() + "ms");
            this.ssh_socket = Common.getSocket("SFTP", u, this.config.getProperty("use_dmz", "false"), "", Integer.parseInt(this.config.getProperty("timeout", "20000")));
            this.ssh_socket.setSoTimeout(600000);
            if (Integer.parseInt(this.config.getProperty("timeout", "0")) > 0) {
                this.ssh2Context.setSocketTimeout(Integer.parseInt(this.config.getProperty("timeout", "0")));
                this.ssh2Context.setIdleConnectionTimeoutSeconds(Integer.parseInt(this.config.getProperty("timeout", "0")) / 1000);
                this.ssh_socket.setSoTimeout(Integer.parseInt(this.config.getProperty("timeout", "0")));
            }
            this.session = (Ssh2Client)con.connect(new SocketWrapper(this.ssh_socket), this.config.getProperty("username"));
            if (!this.config.getProperty("ssh_private_key", this.config.getProperty("privateKeyFilePath", "")).equals("") && !this.config.getProperty("ssh_private_key", this.config.getProperty("privateKeyFilePath", "")).equalsIgnoreCase("NONE")) {
                String private_key_path = Common.replace_str(this.config.getProperty("ssh_private_key", this.config.getProperty("privateKeyFilePath", "")), "{username}", this.config.getProperty("username"));
                private_key_path = Common.replace_str(private_key_path, "{user_name}", this.config.getProperty("username"));
                this.log("SSH_CLIENT", 2, this.log("Using SSH KEY:" + new VRL(private_key_path).safe()));
                PublicKeyAuthentication auth = new PublicKeyAuthentication();
                SshPrivateKeyFile pkfile = null;
                if (System.getProperty("crushftp.v10_beta", "false").equals("true") && Common.System2.containsKey("crushftp.keystores." + private_key_path.toString().toUpperCase().replace('\\', '/'))) {
                    Properties p = (Properties)Common.System2.get("crushftp.keystores." + private_key_path.toString().toUpperCase().replace('\\', '/'));
                    pkfile = SshPrivateKeyFileFactory.parse(new ByteArrayInputStream((byte[])p.get("bytes")));
                } else if (System.getProperty("crushftp.v10_beta", "false").equals("true") && !private_key_path.toString().equals("") && (private_key_path.toUpperCase().startsWith("FILE://") || !new VRL(private_key_path).getProtocol().equalsIgnoreCase("FILE"))) {
                    VRL vrl = new VRL(private_key_path);
                    GenericClient c = Common.getClient(Common.getBaseUrl(vrl.toString()), "SFTPClient", new Vector());
                    if (vrl.getConfig() != null && vrl.getConfig().size() > 0) {
                        c.setConfigObj(vrl.getConfig());
                    }
                    c.login(vrl.getUsername(), vrl.getPassword(), null);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Common.streamCopier(null, null, c.download(vrl.getPath(), 0L, -1L, true), baos, false, true, true);
                    pkfile = SshPrivateKeyFileFactory.parse(new ByteArrayInputStream(baos.toByteArray()));
                    Properties p2 = new Properties();
                    p2.put("bytes", baos.toByteArray());
                    if (System.getProperty("crushftp.v10_beta", "false").equals("true")) {
                        p2.put("name", "");
                        p2.put("type", "ssh");
                    }
                    Common.System2.put("crushftp.keystores." + private_key_path.toUpperCase().replace('\\', '/'), p2);
                } else {
                    pkfile = SshPrivateKeyFileFactory.parse(new FileInputStream(new VRL(private_key_path).getPath()));
                }
                SshKeyPair pair = null;
                if (pkfile.isPassphraseProtected() || !this.config.getProperty("ssh_private_key_pass", "").equals("")) {
                    String ssh_private_key_pass = this.config.getProperty("ssh_private_key_pass", this.config.getProperty("privateKeyFilePass", ""));
                    try {
                        pair = pkfile.toKeyPair(ssh_private_key_pass);
                    }
                    catch (Exception e) {
                        try {
                            ssh_private_key_pass = Common.encryptDecrypt(ssh_private_key_pass, false);
                        }
                        catch (Exception ee) {
                            this.log("SSH_CLIENT", 2, e);
                        }
                        try {
                            pair = pkfile.toKeyPair(ssh_private_key_pass);
                        }
                        catch (Exception eee) {
                            if (("" + eee).toUpperCase().indexOf("AES256") >= 0) {
                                this.log("SSH_CLIENT", 2, this.log("WARNING: Max encryption strength is 128bit."));
                                this.log("Strong cryptography extensions are not installed.  Some SSH clients may fail to connect as they expect AES256 to be available.");
                                this.log("The files must be downloaded manually and installed in your Java lib/security folder.");
                                this.log("Find from Google: https://www.google.com/search?q=java+jce+policy");
                                this.log("Java6 result:http://www.oracle.com/technetwork/java/javase/downloads/jce-6-download-429243.html");
                                this.log("Java7 result:http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html");
                                this.log("OS X install location: /System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home/lib/security/");
                                this.log("Windows install location: C:\\Program Files\\Java\\jre6\\lib\\security\\");
                            }
                            throw eee;
                        }
                    }
                } else {
                    pair = pkfile.toKeyPair(null);
                }
                auth.setPrivateKey(pair.getPrivateKey());
                auth.setPublicKey(pair.getPublicKey());
                auth.setUsername(this.config.getProperty("username"));
                this.session.authenticate(auth);
            }
            if (this.config.getProperty("ssh_private_key", this.config.getProperty("privateKeyFilePath", "")).equals("") || this.config.getProperty("ssh_private_key", this.config.getProperty("privateKeyFilePath", "")).equalsIgnoreCase("NONE") || this.config.getProperty("ssh_two_factor", this.config.getProperty("twoFactorAuthentication", "")).equals("true")) {
                PasswordAuthentication auth = new PasswordAuthentication();
                auth.setPassword(this.config.getProperty("password"));
                auth.setUsername(this.config.getProperty("username"));
                if (!this.session.isAuthenticated()) {
                    this.session.authenticate(auth);
                }
            }
            if (!this.session.isAuthenticated()) {
                throw new Exception("SFTP login failed.");
            }
            if (this.config.getProperty("custom_dot_default_dir", "false").equals("true") || this.config.getProperty("dot_default_dir", "false").equals("true")) {
                System.getProperties().put("maverick.globalscapeDefaultDirWorkaround", "true");
            }
            this.sftp = new SftpClient(this.session);
            try {
                Method setDirectoryAttributeCheck = this.sftp.getClass().getDeclaredMethod("setDirectoryAttributeCheck", Boolean.TYPE);
                setDirectoryAttributeCheck.invoke(this.sftp, this.config.getProperty("custom_checkDirectoryAttributeBeforeList", "false").equals("true"));
            }
            catch (NoSuchMethodException setDirectoryAttributeCheck) {
                // empty catch block
            }
            if (this.session.getRemoteIdentification().indexOf("GXSSSHD") >= 0) {
                this.config.put("sftp_7_token", "true");
            }
            if (this.session.getRemoteIdentification().indexOf("AzureSSH") >= 0) {
                this.config.put("sftp_11_token", "true");
            }
            if (this.session.getRemoteIdentification().indexOf("WeOnlyDo") >= 0) {
                this.config.put("sftp_11_token", "true");
            }
            this.log("SSH_CLIENT", 0, "Remote server vendor:" + this.session.getRemoteIdentification());
            if (this.config.containsKey("custom_charEncoding")) {
                this.sftp.getSubsystemChannel().setCharsetEncoding(this.config.getProperty("custom_charEncoding"));
            } else {
                this.sftp.getSubsystemChannel().setCharsetEncoding("UTF-8");
            }
            if (!System.getProperty("crushftp.client.sftp_max_async", "").equals("")) {
                this.sftp.setMaxAsyncRequests(Integer.parseInt(System.getProperty("crushftp.client.sftp_max_async", "")));
            }
            this.executeScript(this.config.getProperty("after_login_script", ""), "");
            this.config.put("default_dir", "/");
            String pwdStr = this.sftp.pwd();
            this.config.put("default_pwd", pwdStr);
            if (pwdStr.startsWith("/")) {
                this.config.put("default_dir", String.valueOf(pwdStr) + (pwdStr.endsWith("/") ? "" : "/"));
            }
            if (this.config.containsKey("custom_setSftpBufferSize")) {
                this.sftp.setBufferSize(Integer.parseInt(this.config.getProperty("custom_setSftpBufferSize", "32768")));
            }
            if (this.config.containsKey("custom_setSftpBlockSize")) {
                this.sftp.setBlockSize(Integer.parseInt(this.config.getProperty("custom_setSftpBlockSize", "32768")));
            }
            if (this.config.containsKey("custom_setMaxAsyncRequests")) {
                this.sftp.setMaxAsyncRequests(Integer.parseInt(this.config.getProperty("custom_setMaxAsyncRequests", "100")));
            }
        }
        catch (SshException e) {
            this.log("SSH_CLIENT", 2, this.log(e));
            if (e.getCause() != null) {
                this.log("SSH_CLIENT", 2, this.log(e.getCause()));
            }
            this.log("SSH_CLIENT", 2, this.log("Msg1:" + e.getMessage()));
            this.log("SSH_CLIENT", 2, this.log("Msg2:" + e.getLocalizedMessage()));
            this.log("SSH_CLIENT", 2, this.log("Reason:" + e.getReason()));
            this.log("SSH_CLIENT", 2, this.log("SSH Client Version:" + SshConnector.getVersion()));
            this.logout();
            throw e;
        }
        catch (Exception e) {
            this.log("SSH_CLIENT", 2, this.log(e));
            if (e.getCause() != null) {
                this.log("SSH_CLIENT", 2, this.log(e.getCause()));
            }
            this.log("SSH_CLIENT", 2, this.log("Msg1:" + e.getMessage()));
            this.log("SSH_CLIENT", 2, this.log("Msg2:" + e.getLocalizedMessage()));
            this.log("SSH_CLIENT", 2, this.log("SSH Client Version:" + SshConnector.getVersion()));
            this.logout();
            throw e;
        }
    }

    @Override
    public Properties stat(String path) throws Exception {
        Properties dir_item;
        block15: {
            this.reconnect();
            this.config.put("simple", "true");
            this.setThreadName();
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            if (path.endsWith(";1*")) {
                path = path.substring(0, path.length() - 3);
            }
            dir_item = null;
            try {
                String uid;
                String path2 = path;
                if (!path2.startsWith("/")) {
                    path2 = "/" + path2;
                }
                SftpFileAttributes attrs = this.sftp.stat(this.convertPath(path2));
                dir_item = new Properties();
                if (this.url.endsWith("/") && path.startsWith("/")) {
                    dir_item.put("url", String.valueOf(this.url) + path.substring(1));
                } else {
                    dir_item.put("url", String.valueOf(this.url) + path);
                }
                dir_item.put("protocol", "sftp");
                dir_item.put("dir", Common.all_but_last(path));
                dir_item.put("name", Common.last(path));
                dir_item.put("root_dir", Common.all_but_last(path));
                dir_item.put("type", attrs.isDirectory() ? "DIR" : "FILE");
                String perms = attrs.getPermissionsString();
                if (perms.trim().equals("")) {
                    perms = String.valueOf(attrs.isDirectory() ? "d" : "-") + "rwxrwxrwx";
                }
                while (perms.length() < 10) {
                    perms = String.valueOf(perms) + "-";
                }
                dir_item.put("permissions", perms);
                dir_item.put("num_items", "1");
                String gid = attrs.getGID();
                if (gid.trim().equals("")) {
                    gid = "0";
                }
                if ((uid = attrs.getUID()).trim().equals("")) {
                    uid = "0";
                }
                dir_item.put("owner", String.valueOf(uid));
                dir_item.put("group", String.valueOf(gid));
                dir_item.put("size", "" + attrs.getSize());
                Date d = attrs.getModifiedDateTime();
                dir_item.put("modified", String.valueOf(d.getTime()));
                dir_item.put("month", this.mmm.format(d));
                dir_item.put("day", this.dd.format(d));
                dir_item.put("time_or_year", this.yyyy.format(d));
                if (dir_item.getProperty("type").equalsIgnoreCase("DIR")) {
                    dir_item.put("size", "1");
                }
                dir_item.put("simple", String.valueOf(this.config.getProperty("simple", "").equals("true")));
                if (!this.config.getProperty("sftp_7_token", "false").equals("true")) break block15;
                Vector v = new Vector();
                this.list(Common.all_but_last(path), v);
                int x = 0;
                while (x < v.size()) {
                    Properties p = (Properties)v.elementAt(x);
                    if (p.getProperty("name").equals(dir_item.getProperty("name"))) {
                        dir_item.put("size", p.getProperty("size"));
                        break;
                    }
                    ++x;
                }
            }
            catch (SftpStatusException e) {
                if (System.getProperty("crushftp.isTestCall" + Thread.currentThread().getId(), "false").equals("true")) {
                    throw e;
                }
                return null;
            }
        }
        return dir_item;
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        Properties dir_item;
        SftpFile[] v;
        VRL vrl;
        block66: {
            this.reconnect();
            this.executeScript(this.config.getProperty("before_dir_script", ""), path.trim());
            this.setThreadName();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            vrl = new VRL(this.url);
            this.log("SSH_CLIENT", 2, "ls " + vrl.safe() + path);
            String path2 = Common.all_but_last(String.valueOf(vrl.getPath()) + path);
            if ((String.valueOf(vrl.getPath()) + path).endsWith("/")) {
                path2 = String.valueOf(vrl.getPath()) + path;
            }
            this.sftp.cd(this.convertPath(path2));
            v = new SftpFile[]{};
            try {
                v = System.getProperty("crushftp.sftpclient_ls_dot", "true").equalsIgnoreCase("true") ? this.sftp.ls(".") : this.sftp.ls("");
            }
            catch (SftpStatusException ee) {
                try {
                    if (("" + ee).indexOf("No such file") >= 0) {
                        this.log("SSH_CLIENT", 2, this.log("" + ee));
                        this.log("SSH_CLIENT", 2, "Trying blank ls param...");
                        v = this.sftp.ls();
                    }
                }
                catch (SftpStatusException e) {
                    if (("" + e).indexOf("Failed to open") >= 0) {
                        this.log("SSH_CLIENT", 2, this.log("" + e));
                        break block66;
                    }
                    throw e;
                }
            }
        }
        this.log("SSH_CLIENT", 2, "DIR Count:" + v.length);
        boolean bad_mainframe_listing1 = false;
        int x = 0;
        while (x < v.length) {
            block67: {
                dir_item = new Properties();
                String data = v[x].getLongname();
                if (x == 0 && !bad_mainframe_listing1 && data != null && data.indexOf("Referred") >= 0 && data.indexOf("Tracks") >= 0 && data.indexOf("Dsorg") >= 0) {
                    this.log("SSH_CLIENT", 2, "bad_mainframe_listing1 detected:" + data);
                    bad_mainframe_listing1 = true;
                } else {
                    if (bad_mainframe_listing1) {
                        data = null;
                    }
                    if (data == null || data.startsWith("/") || this.config.getProperty("sftp_11_token", "false").equals("true")) {
                        Date d = v[x].getAttributes().getModifiedDateTime();
                        data = SftpClient.formatLongname(v[x]);
                        this.log("SSH_CLIENT", 2, "RAW long name (" + v[x].isDirectory() + "):" + data);
                        if (data.indexOf("                        ") >= 0 || data.trim().startsWith("-") && v[x].isDirectory() || this.config.getProperty("sftp_11_token", "false").equals("true")) {
                            String uid;
                            String perms = v[x].getAttributes().getPermissionsString();
                            if (perms.trim().equals("")) {
                                perms = "rwxrwxrwx";
                            }
                            while (perms.length() < 10) {
                                perms = String.valueOf(perms) + "-";
                            }
                            String designator = "";
                            if (v[x].isDirectory()) {
                                if (!perms.trim().startsWith("d")) {
                                    designator = "d";
                                }
                            } else if (!perms.trim().startsWith("-")) {
                                designator = "-";
                            }
                            data = String.valueOf(designator) + perms + " ";
                            data = String.valueOf(data) + "1 ";
                            String gid = v[x].getAttributes().getGID();
                            if (gid.trim().equals("")) {
                                gid = "0";
                            }
                            if ((uid = v[x].getAttributes().getUID()).trim().equals("")) {
                                uid = "0";
                            }
                            data = String.valueOf(data) + gid + " ";
                            data = String.valueOf(data) + uid + " ";
                            data = String.valueOf(data) + v[x].getAttributes().getSize() + " ";
                            data = String.valueOf(data) + this.mmm.format(d) + " ";
                            data = String.valueOf(data) + this.dd.format(d) + " ";
                            data = String.valueOf(data) + this.yyyy.format(d) + " ";
                            data = String.valueOf(data) + Common.last(v[x].getFilename());
                        }
                    }
                    dir_item.put("modified", String.valueOf(v[x].getAttributes().getModifiedDateTime().getTime()));
                    if (data != null) {
                        data = data.replaceAll(" domain users ", " domain_users ");
                        this.log("SSH_CLIENT", 2, data);
                        try {
                            if ((String.valueOf(vrl.getPath()) + path).endsWith("/")) {
                                dir_item.put("root_dir", String.valueOf(vrl.getPath()) + path);
                            } else {
                                dir_item.put("root_dir", Common.all_but_last(String.valueOf(vrl.getPath()) + path));
                            }
                            dir_item.put("protocol", "sftp");
                            if (!data.toUpperCase().startsWith("TOTAL ")) {
                                StringTokenizer get_em = new StringTokenizer(data, " ");
                                boolean normalMode = true;
                                if (data.toUpperCase().startsWith("D")) {
                                    dir_item.put("type", "DIR");
                                } else if (data.toUpperCase().startsWith("L")) {
                                    try {
                                        String path3 = String.valueOf(vrl.getPath()) + path + v[x].getFilename();
                                        SftpFile sf = this.sftp.openFile(this.convertPath(path3));
                                        dir_item.put("type", sf.isFile() ? "FILE" : "DIR");
                                        sf.close();
                                    }
                                    catch (Exception e) {
                                        this.log("SSH_CLIENT", 2, e);
                                        dir_item.put("type", "DIR");
                                    }
                                } else if (data.toUpperCase().startsWith("-")) {
                                    dir_item.put("type", "FILE");
                                } else {
                                    normalMode = false;
                                    dir_item.put("type", v[x].isDirectory() ? "DIR" : "FILE");
                                    dir_item.put("permissions", String.valueOf(v[x].isDirectory() ? "d" : "-") + "rwxrwxrwx");
                                    dir_item.put("num_items", "1");
                                    dir_item.put("owner", "user");
                                    dir_item.put("group", "group");
                                    dir_item.put("size", get_em.nextToken().trim());
                                    dir_item.put("month", get_em.nextToken().trim());
                                    String day = get_em.nextToken().trim();
                                    day = day.substring(0, day.length() - 1);
                                    dir_item.put("day", day);
                                    dir_item.put("year", get_em.nextToken().trim());
                                    dir_item.put("time_or_year", get_em.nextToken().trim());
                                }
                                if (normalMode) {
                                    String perms = get_em.nextToken().trim();
                                    while (perms.length() < 10) {
                                        perms = String.valueOf(perms) + "-";
                                    }
                                    dir_item.put("permissions", perms);
                                    dir_item.put("num_items", get_em.nextToken().trim());
                                    String user_part = "owner";
                                    String group_part = "group";
                                    if (data.indexOf("                   ") < 0) {
                                        user_part = get_em.nextToken().trim();
                                        group_part = get_em.nextToken().trim();
                                        dir_item.put("owner", user_part.replace('\\', '_'));
                                        dir_item.put("group", group_part.replace('\\', '_'));
                                    } else {
                                        dir_item.put("owner", "owner");
                                        dir_item.put("group", "group");
                                    }
                                    boolean skip_size = false;
                                    if (data.indexOf("       root      ") >= 0) {
                                        try {
                                            Long.parseLong(dir_item.getProperty("group"));
                                            try {
                                                Long.parseLong(dir_item.getProperty("owner"));
                                            }
                                            catch (Exception e) {
                                                skip_size = true;
                                                dir_item.put("size", dir_item.getProperty("group"));
                                                dir_item.put("group", dir_item.getProperty("owner"));
                                            }
                                        }
                                        catch (Exception e) {
                                            // empty catch block
                                        }
                                    }
                                    String size_part = dir_item.getProperty("size", "0");
                                    if (!skip_size) {
                                        size_part = get_em.nextToken();
                                        while (!Common.isNumeric(size_part)) {
                                            size_part = get_em.nextToken().trim();
                                        }
                                    }
                                    if (!skip_size) {
                                        dir_item.put("size", size_part.trim());
                                    }
                                    dir_item.put("month", get_em.nextToken().trim());
                                    dir_item.put("day", get_em.nextToken().trim());
                                    dir_item.put("time_or_year", get_em.nextToken().trim());
                                }
                                String name_data = get_em.nextToken();
                                String searchName = String.valueOf(dir_item.getProperty("time_or_year")) + " " + name_data;
                                if ((name_data = data.substring(data.indexOf(name_data, data.indexOf(searchName) + dir_item.getProperty("time_or_year").length() + 1))).endsWith("*")) {
                                    name_data = name_data.substring(0, name_data.length() - 1);
                                }
                                name_data = name_data.replace("\u0000", "");
                                dir_item.put("name", name_data);
                                if (data.toUpperCase().startsWith("L") && name_data.indexOf(" ->") >= 0) {
                                    dir_item.put("name", name_data.substring(0, name_data.indexOf(" ->")));
                                    dir_item.put("permissions", "drwxrwxrwx");
                                }
                                if (!normalMode && name_data.endsWith("/")) {
                                    name_data = name_data.substring(0, name_data.length() - 1);
                                    dir_item.put("type", "DIR");
                                    dir_item.put("permissions", "drwxrwxrwx");
                                    dir_item.put("name", name_data);
                                } else if (normalMode && dir_item.getProperty("type", "").equalsIgnoreCase("DIR") && name_data.endsWith("/")) {
                                    name_data = name_data.substring(0, name_data.length() - 1);
                                    dir_item.put("name", name_data);
                                }
                                if (this.url.endsWith("/") && path.startsWith("/")) {
                                    dir_item.put("url", String.valueOf(this.url) + path.substring(1) + dir_item.getProperty("name"));
                                } else {
                                    dir_item.put("url", String.valueOf(this.url) + path + dir_item.getProperty("name"));
                                }
                                dir_item.put("dir", path);
                                if (!(dir_item.getProperty("name").equals(".") || dir_item.getProperty("name").equals("..") || dir_item.getProperty("name").equals("./") || dir_item.getProperty("name").equals("../"))) {
                                    list.addElement(dir_item);
                                }
                            }
                            if (dir_item.getProperty("type").equalsIgnoreCase("DIR")) {
                                dir_item.put("size", "1");
                            }
                        }
                        catch (Exception eee) {
                            if (("" + eee).indexOf("Interrupted") < 0) break block67;
                            throw eee;
                        }
                    }
                }
            }
            ++x;
        }
        x = 0;
        while (x < list.size()) {
            dir_item = (Properties)list.elementAt(x);
            if (dir_item != null) {
                SimpleDateFormat mmddyyyy = new SimpleDateFormat("MMM dd yyyy HH:mm", Locale.US);
                SimpleDateFormat yyyy = new SimpleDateFormat("yyyy");
                Date modified = new Date();
                String time_or_year = dir_item.getProperty("time_or_year", "");
                String year = yyyy.format(new Date(Long.parseLong(dir_item.getProperty("modified", String.valueOf(System.currentTimeMillis())))));
                String time = "00:00";
                if (time_or_year.indexOf(":") < 0) {
                    year = time_or_year;
                } else {
                    time = time_or_year;
                }
                if (dir_item.containsKey("year")) {
                    year = dir_item.getProperty("year");
                }
                try {
                    modified = mmddyyyy.parse(String.valueOf(dir_item.getProperty("month", "")) + " " + dir_item.getProperty("day", "") + " " + year + " " + time);
                }
                catch (Exception e) {
                    this.log("SSH_CLIENT", 1, e);
                }
                if (modified.getTime() > System.currentTimeMillis() + 172800000L) {
                    GregorianCalendar calendar = new GregorianCalendar();
                    calendar.setTime(modified);
                    ((Calendar)calendar).add(1, -1);
                    modified = calendar.getTime();
                }
                if (!dir_item.containsKey("modified")) {
                    dir_item.put("modified", String.valueOf(modified.getTime()));
                }
            }
            ++x;
        }
        this.executeScript(this.config.getProperty("after_dir_script", ""), path.trim());
        return list;
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        this.reconnect();
        this.executeScript(this.config.getProperty("before_download_script", ""), path.trim());
        this.setThreadName();
        this.log("SSH_CLIENT", 2, "get1 " + path);
        if (startPos < 0L) {
            startPos = 0L;
        }
        if (path.indexOf("$") >= 0 && path.indexOf(";") >= 0) {
            path = path.substring(0, path.lastIndexOf(";"));
        }
        if (this.config.getProperty("before_download_script", "").toUpperCase().startsWith("SIMPLE")) {
            path = path.substring(path.lastIndexOf("/") + 1);
        }
        if (this.config.containsKey("custom_setIdleConnectionTimeoutSeconds")) {
            int secs = Integer.parseInt(this.config.getProperty("custom_setIdleConnectionTimeoutSeconds", "600"));
            this.ssh2Context.setIdleConnectionTimeoutSeconds(secs);
            this.ssh2Context.setSocketTimeout(secs * 1000);
            this.ssh2Context.setTreatIdleConnectionAsError(false);
            this.log("SSH_CLIENT", 1, "Configured SFTP connection idle with " + secs + " seconds.");
        }
        if (this.config.getProperty("before_download_script", "").toUpperCase().indexOf("PREPEND_SLASH") >= 0) {
            path = "/" + path;
        }
        this.log("SSH_CLIENT", 2, "get2 " + path);
        this.in = this.sftp.getInputStream(this.convertPath(path), startPos);
        if (endPos > 0L) {
            this.in = this.getLimitedInputStream(this.in, startPos, endPos);
        }
        return this.in;
    }

    @Override
    protected OutputStream upload3(String path2, final long startPos, boolean truncate, boolean binary) throws Exception {
        this.reconnect();
        this.executeScript(this.config.getProperty("before_upload_script", ""), path2.trim());
        this.setThreadName();
        this.log("SSH_CLIENT", 2, "put " + path2);
        if (path2.endsWith("/")) {
            path2 = path2.substring(0, path2.length() - 1);
        }
        final String path = path2;
        try {
            if (truncate && startPos <= 0L) {
                this.sftp.rm(this.convertPath(path));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        Properties sockets = Common.getConnectedSocks(false);
        final Socket sock1 = (Socket)sockets.remove("sock1");
        Socket sock2 = (Socket)sockets.remove("sock2");
        final Properties upload_status = new Properties();
        upload_status.put("status", "");
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    SFTPClient.this.sftp.put(sock1.getInputStream(), SFTPClient.this.convertPath(path), startPos < 0L ? 0L : startPos);
                    upload_status.put("status", "complete");
                }
                catch (Exception e) {
                    SFTPClient.this.log("SSH_CLIENT", 0, SFTPClient.this.log("" + e));
                    upload_status.put("status", "ERROR:" + e);
                }
            }
        });
        class OutputWrapper
        extends OutputStream {
            OutputStream out3 = null;
            boolean closed = false;
            private final /* synthetic */ Properties val$upload_status;
            private final /* synthetic */ String val$path;

            public OutputWrapper(OutputStream out3, Properties properties, String string) {
                this.val$upload_status = properties;
                this.val$path = string;
                this.out3 = out3;
            }

            @Override
            public void write(int i) throws IOException {
                this.out3.write(i);
            }

            @Override
            public void write(byte[] b) throws IOException {
                this.write(b, 0, b.length);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                this.out3.write(b, off, len);
            }

            @Override
            public void close() throws IOException {
                if (this.closed) {
                    return;
                }
                if (this.out3 != null) {
                    this.out3.close();
                }
                this.closed = true;
                int loops = 0;
                while (loops++ < 600 && this.val$upload_status.getProperty("status", "").equalsIgnoreCase("")) {
                    try {
                        Thread.sleep(100L);
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                }
                if (!this.val$upload_status.getProperty("status", "").equalsIgnoreCase("complete")) {
                    throw new IOException(this.val$upload_status.getProperty("status", ""));
                }
                try {
                    SFTPClient.this.executeScript(SFTPClient.this.config.getProperty("after_upload_script", ""), this.val$path.trim());
                }
                catch (Exception e) {
                    SFTPClient.this.log("SSH_CLIENT", 2, SFTPClient.this.log(e));
                }
            }
        }
        return new OutputWrapper(sock2.getOutputStream(), upload_status, path);
    }

    @Override
    public boolean rename(String path1, String path2, boolean overwrite) throws Exception {
        this.reconnect();
        this.recent_mkdirs.removeAllElements();
        this.setThreadName();
        this.log("SSH_CLIENT", 2, "rename " + path1 + "   to   " + path2);
        try {
            this.sftp.rename(this.convertPath(path1), this.convertPath(path2));
        }
        catch (Exception e) {
            this.log("SSH_CLIENT", 1, "Rename failed:" + path1 + " -->" + path2);
            this.log("SSH_CLIENT", 1, e);
            return false;
        }
        return true;
    }

    @Override
    public boolean delete(String path) throws Exception {
        this.reconnect();
        this.recent_mkdirs.removeAllElements();
        this.setThreadName();
        try {
            this.sftp.rm(this.convertPath(path));
        }
        catch (SftpStatusException e) {
            if (("" + e).toUpperCase().indexOf("NO SUCH FILE") >= 0 || ("" + e).toUpperCase().indexOf("NOT A VALID FILE PATH") >= 0) {
                return true;
            }
            this.log("SSH_CLIENT", 1, e);
            return false;
        }
        return true;
    }

    @Override
    public void logout() throws Exception {
        try {
            Thread t2;
            this.executeScript(this.config.getProperty("before_logout_script", ""), "");
            this.setThreadName();
            try {
                if (this.ssh2Context != null) {
                    this.ssh2Context.setSocketTimeout(2000);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                if (this.sftp != null) {
                    t2 = new Thread(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                SFTPClient.this.sftp.quit();
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                    });
                    t2.start();
                    t2.join(3000L);
                    t2.interrupt();
                }
                this.sftp = null;
            }
            catch (Exception t2) {
                // empty catch block
            }
            try {
                if (this.session != null) {
                    t2 = new Thread(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                SFTPClient.this.session.disconnect();
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                    });
                    t2.start();
                    t2.join(3000L);
                    t2.interrupt();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                this.close();
            }
            catch (Exception exception) {}
        }
        finally {
            try {
                if (this.ssh_socket != null) {
                    this.ssh_socket.close();
                }
            }
            catch (Exception exception) {}
            this.ssh_socket = null;
            SshConnector.removeEventListener(this.uniqueId);
            created_clients.remove(this.uniqueId);
        }
    }

    @Override
    public boolean makedir(String path) throws Exception {
        this.reconnect();
        this.setThreadName();
        try {
            this.log("SSH_CLIENT", 2, "SFTPClient:MAKEDIR:" + path);
            this.sftp.mkdir(this.convertPath(path));
        }
        catch (Exception e) {
            this.log("SSH_CLIENT", 2, e);
            if (("" + e).indexOf("exists") >= 0) {
                return true;
            }
            throw e;
        }
        return true;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public boolean makedirs(String path) throws Exception {
        block9: {
            this.reconnect();
            ok = false;
            try {
                ok = this.makedir(path);
                break block9;
            }
            catch (Exception e) {
                this.log("SSH_CLIENT", 1, "MKDIR recursive failed:" + path + " so we will try recursive. (" + e + ")");
                if (("" + e).indexOf("already exists") >= 0) {
                    ok = true;
                }
                if (ok) break block9;
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
                            this.log("SSH_CLIENT", 1, "MKDIR individual:" + path2 + " failed, moving to next. (" + ee + ")");
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
        this.reconnect();
        this.setThreadName();
        try {
            SftpFileAttributes attrs = this.sftp.stat(this.convertPath(path));
            attrs.setTimes(new UnsignedInteger64(modified / 1000L), new UnsignedInteger64(modified / 1000L));
            this.sftp.getSubsystemChannel().setAttributes(this.convertPath(path), attrs);
        }
        catch (Exception e) {
            this.log("SSH_CLIENT", 1, e);
            return false;
        }
        return true;
    }

    public void executeScript(String script, String verb_data) throws Exception {
        String command;
        if (script == null || script.trim().equals("")) {
            return;
        }
        BufferedReader br = new BufferedReader(new StringReader(script));
        while ((command = br.readLine()) != null) {
            this.log("SSH_CLIENT", 2, this.log("SCRIPT:" + command));
            if (command.startsWith("ls ") || command.startsWith("dir ") || command.startsWith("list ")) {
                this.log("SSH_CLIENT", 2, this.log("Trying openDirectory:" + command.substring(command.indexOf(" ") + 1)));
                SftpFile sf = this.sftp.getSubsystemChannel().openDirectory(command.substring(command.indexOf(" ") + 1), true);
                this.sftp.getSubsystemChannel().closeFile(sf);
                continue;
            }
            if (command.startsWith("rm ") || command.startsWith("del ")) {
                this.sftp.rm(command.substring(command.indexOf(" ") + 1));
                continue;
            }
            if (command.startsWith("mv ") || command.startsWith("rename ")) {
                this.sftp.rename(command.substring(3).split(":")[0], command.substring(3).split(":")[1]);
                continue;
            }
            if (command.startsWith("cd ") || command.startsWith("cwd ")) {
                this.sftp.cd(command.substring(command.indexOf(" ") + 1));
                continue;
            }
            if (command.startsWith("chgrp ")) {
                this.sftp.chgrp(command.substring(6).split(":")[0], verb_data);
                continue;
            }
            if (command.startsWith("chown ")) {
                this.sftp.chown(command.substring(6).split(":")[0], verb_data);
                continue;
            }
            if (command.startsWith("chmod ")) {
                this.sftp.chmod(Integer.parseInt(command.substring(6).split(":")[0], 8), verb_data);
                continue;
            }
            if (command.startsWith("mkd ")) {
                this.sftp.mkdir(command.substring(command.indexOf(" ") + 1));
                continue;
            }
            if (!command.startsWith("abort")) continue;
            this.ssh_socket.close();
        }
    }

    public String convertPath(String path) {
        return path.startsWith("/~/") ? String.valueOf(this.config.getProperty("default_pwd", "/")) + path.substring(2) : path;
    }

    @Override
    public String doCommand(String command) throws Exception {
        if (command.startsWith("abort")) {
            this.ssh_socket.close();
        }
        return "";
    }

    class SftpLogger
    implements EventListener {
        final int EVENT_LOG = 110;
        final int EVENT_DEBUG_LOG = 111;
        final int EVENT_EXCEPTION_LOG = 112;
        final String ATTRIBUTE_LOG_MESSAGE = "LOG_MESSAGE";
        final String ATTRIBUTE_THROWABLE = "THROWABLE";
        boolean ignoreLogEvents = false;

        public void setProduct(String product) {
        }

        public String getProduct() {
            return "";
        }

        public SftpLogger() {
        }

        public SftpLogger(boolean ignoreLogEvents) {
            this.setIgnoreLogEvents(ignoreLogEvents);
        }

        public void setIgnoreLogEvents(boolean ignore) {
            this.ignoreLogEvents = ignore;
        }

        @Override
        public void processEvent(Event evt) {
            if (!(evt.getId() != 110 && evt.getId() != 111 && evt.getId() != 112 || this.ignoreLogEvents)) {
                SFTPClient.this.log("SSH_CLIENT", 0, String.valueOf(evt.getAllAttributes()));
                if (evt.getId() == 110) {
                    SFTPClient.this.log("SSH_CLIENT", 0, "" + evt.getAttribute("LOG_MESSAGE"));
                } else if (evt.getId() == 111) {
                    SFTPClient.this.log("SSH_CLIENT", 2, "" + evt.getAttribute("LOG_MESSAGE"));
                } else if (evt.getId() == 112) {
                    SFTPClient.this.log("SSH_CLIENT", 1, SFTPClient.this.log("" + evt.getAttribute("LOG_MESSAGE")));
                    SFTPClient.this.log("SSH_CLIENT", 1, SFTPClient.this.log((Throwable)evt.getAttribute("THROWABLE")));
                }
            } else {
                SFTPClient.this.log("SSH_CLIENT", 0, String.valueOf(J2SSHEventCodes.messageCodes.get(new Integer(evt.getId()))) + evt.getAllAttributes());
            }
        }
    }
}

