/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.didisoft.pgp.CypherAlgorithm$Enum
 *  com.didisoft.pgp.HashAlgorithm$Enum
 *  com.didisoft.pgp.PGPKeyPair
 *  org.bouncycastle.crypto.digests.KeccakDigest
 *  org.bouncycastle.jcajce.provider.digest.SHA3$DigestSHA3
 *  org.bouncycastle.util.encoders.Hex
 *  org.jdom.Content
 *  org.jdom.Document
 *  org.jdom.Element
 *  org.jdom.Namespace
 *  org.jdom.input.SAXBuilder
 *  org.jdom.output.Format
 *  org.jdom.output.XMLOutputter
 */
package com.crushftp.client;

import com.crushftp.client.AzureClient;
import com.crushftp.client.B2Client;
import com.crushftp.client.BCProxy;
import com.crushftp.client.Base64;
import com.crushftp.client.BoxClient;
import com.crushftp.client.CitrixClient;
import com.crushftp.client.CustomClient;
import com.crushftp.client.DMZSocket;
import com.crushftp.client.DropBoxClient;
import com.crushftp.client.FTPClient;
import com.crushftp.client.FileClient;
import com.crushftp.client.File_B;
import com.crushftp.client.File_S;
import com.crushftp.client.File_U;
import com.crushftp.client.GDriveClient;
import com.crushftp.client.GStorageClient;
import com.crushftp.client.GenericClient;
import com.crushftp.client.GenericClientMulti;
import com.crushftp.client.GlacierClient;
import com.crushftp.client.HTTPBufferedClient;
import com.crushftp.client.HTTPClient;
import com.crushftp.client.HadoopClient;
import com.crushftp.client.MD4;
import com.crushftp.client.Mailer;
import com.crushftp.client.MemoryClient;
import com.crushftp.client.OneDriveClient;
import com.crushftp.client.RFileClient;
import com.crushftp.client.S3Client;
import com.crushftp.client.S3CrushClient;
import com.crushftp.client.SFTPClient;
import com.crushftp.client.SMB1Client;
import com.crushftp.client.SMB4jClient;
import com.crushftp.client.SMBjNQClient;
import com.crushftp.client.SharePointClient;
import com.crushftp.client.TrustManagerCustom;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Variables;
import com.crushftp.client.WebDAVClient;
import com.crushftp.client.Worker;
import com.crushftp.client.ZipClient;
import com.crushftp.tunnel2.Tunnel2;
import com.didisoft.pgp.CypherAlgorithm;
import com.didisoft.pgp.HashAlgorithm;
import com.didisoft.pgp.PGPKeyPair;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ServerSocketFactory;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.script.ScriptEngineManager;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import org.apache.commons.codec.binary.Base32;
import org.bouncycastle.crypto.digests.KeccakDigest;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Common {
    static boolean providerAdded = false;
    public static final String pgpChunkedheaderStr = String.valueOf(System.getProperty("appname", "CrushFTP").toUpperCase()) + "_PGPChunkedStream:dBa3Em7W4N:";
    public static final String encryptedNote = String.valueOf(System.getProperty("appname", "CrushFTP").toUpperCase()) + "_ENCRYPTED_kHBeMxiWj7Sb4PdqJ8";
    public static final String encryptedDefaultSize = "0                                        ";
    public static Properties socketPool = new Properties();
    public static int socketTimeout = 20000;
    public static Vector log = null;
    public static Properties System2 = new Properties();
    static SecureRandom rn;
    static Properties bad_tls_protocols;
    public static long mimesModified;
    public static Properties mimes;
    public static boolean dmz_mode;
    public static char[] encryption_password;
    public static Properties oauth_access_tokens;
    static Properties ip_lookup_cache;
    private static Properties salt_hash;
    static boolean added_bc;
    static long UID_GLOBAL;
    public static Properties refresh_tokens;
    static TrustManager[] trustAllCerts;
    protected static final String[] pads;
    static long lastIpLookup;
    static String lastLocalIP;

    static {
        System2.put("running_tasks", new Vector());
        rn = new SecureRandom();
        bad_tls_protocols = new Properties();
        mimesModified = 0L;
        mimes = new Properties();
        dmz_mode = false;
        encryption_password = "crushftp".toCharArray();
        oauth_access_tokens = new Properties();
        ip_lookup_cache = new Properties();
        salt_hash = new Properties();
        added_bc = false;
        UID_GLOBAL = System.currentTimeMillis();
        refresh_tokens = new Properties();
        rn.nextBytes((String.valueOf(Runtime.getRuntime().maxMemory()) + Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()).getBytes());
        trustAllCerts = new TrustManager[]{new X509TrustManager(){

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};
        pads = new String[]{"", " ", "  ", "   ", "    ", "     ", "      ", "       ", "        ", "         ", "          ", "           ", "            ", "             ", "              ", "               "};
        lastIpLookup = 0L;
        lastLocalIP = "127.0.0.1";
    }

    public static synchronized long uidg() {
        while (System.currentTimeMillis() == UID_GLOBAL) {
            try {
                Thread.sleep(1L);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        UID_GLOBAL = System.currentTimeMillis();
        return UID_GLOBAL;
    }

    public static int V() {
        return Integer.parseInt(System.getProperty("crushftp.version", "9"));
    }

    public static boolean machine_is_mac() {
        try {
            return System.getProperties().getProperty("os.name").toUpperCase().equals("MAC OS");
        }
        catch (Exception exception) {
            return false;
        }
    }

    public static boolean machine_is_x() {
        try {
            return System.getProperties().getProperty("os.name").toUpperCase().equals("MAC OS X");
        }
        catch (Exception exception) {
            return false;
        }
    }

    public static boolean machine_is_linux() {
        block3: {
            try {
                if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("LINUX") < 0) break block3;
                return true;
            }
            catch (Exception exception) {
                return false;
            }
        }
        return System.getProperties().getProperty("os.name", "").toUpperCase().indexOf("HP-UX") >= 0;
    }

    public static boolean machine_is_unix() {
        try {
            return System.getProperties().getProperty("os.name").toUpperCase().indexOf("UNIX") >= 0;
        }
        catch (Exception exception) {
            return false;
        }
    }

    public static boolean machine_is_windows() {
        try {
            return System.getProperties().getProperty("os.name").toUpperCase().indexOf("NDOWS") >= 0;
        }
        catch (Exception exception) {
            return false;
        }
    }

    public static boolean machine_is_windows_xp() {
        try {
            return Common.machine_is_windows() && System.getProperties().getProperty("os.name").toUpperCase().indexOf("XP") >= 0;
        }
        catch (Exception exception) {
            return false;
        }
    }

    public static boolean machine_is_x_10_6_plus() {
        if (Common.machine_is_x()) {
            String[] version = System.getProperties().getProperty("os.version", "").split("\\.");
            if (Integer.parseInt(version[0]) >= 10 && Integer.parseInt(version[1]) >= 6) {
                return true;
            }
            if (Integer.parseInt(version[0]) > 10) {
                return true;
            }
        }
        return false;
    }

    public static boolean showUrl(URL url) {
        boolean result = false;
        if (!result) {
            result = Common.openURL(url.toExternalForm());
        }
        return result;
    }

    public static boolean openURL(String url) {
        try {
            if (Common.machine_is_x()) {
                Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
                openURL.invoke(null, url);
            } else if (Common.machine_is_windows()) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else {
                String[] browsers = new String[]{"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String browser = null;
                int count = 0;
                while (count < browsers.length && browser == null) {
                    if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                    if (browser == null) {
                        throw new Exception("Could not find web browser");
                    }
                    Runtime.getRuntime().exec(new String[]{browser, url});
                    ++count;
                }
            }
            return true;
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error attempting to launch web browser:\n" + e.getLocalizedMessage());
            return false;
        }
    }

    public static boolean isSymbolicLink(String link_name) {
        if (Common.machine_is_windows()) {
            return false;
        }
        try {
            File f = new File(link_name);
            if (!f.getAbsolutePath().equals(f.getCanonicalPath())) {
                return true;
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return false;
    }

    public static boolean log(String tag, int level, String s) {
        int at;
        int colslashslash;
        String inner;
        if (s.indexOf("url=") >= 0) {
            String url = s.substring(s.indexOf("url=") + 4).trim();
            if (url.indexOf(",") >= 0) {
                if (url.indexOf("@") >= 0) {
                    int loc = url.indexOf(",", url.indexOf("@"));
                    if (loc < 0) {
                        loc = url.indexOf(", ");
                    }
                    url = url.substring(0, loc).trim();
                } else {
                    url = url.substring(0, url.indexOf(",")).trim();
                }
            }
            try {
                VRL vrl = new VRL(url);
                String url2 = String.valueOf(vrl.getProtocol()) + "://" + vrl.getUsername() + ":********" + "@" + vrl.getHost() + ":" + vrl.getPort() + vrl.getPath();
                s = Common.replace_str(s, url, url2);
            }
            catch (Exception vrl) {}
        } else if (s.indexOf("://") >= 0 && s.indexOf("@", s.indexOf("://")) >= 0 && (inner = s.substring(colslashslash = s.indexOf("://"), at = s.indexOf("@", colslashslash))).indexOf(":") >= 0) {
            s = Common.replace_str(s, inner, String.valueOf(inner.substring(0, inner.indexOf(":") + 1)) + "************");
        }
        if (s.indexOf("password=") >= 0) {
            String url = s.substring(s.indexOf("password=") + 9).trim();
            if (url.indexOf(",") >= 0) {
                if (url.indexOf("@") >= 0) {
                    int loc = url.indexOf(",", url.indexOf("@"));
                    if (loc < 0) {
                        loc = url.indexOf(", ");
                    }
                    url = url.substring(0, loc).trim();
                } else {
                    url = url.substring(0, url.indexOf(",")).trim();
                }
            }
            s = Common.replace_str(s, url, "***********");
        }
        if (log != null) {
            if (Integer.parseInt(System.getProperty("crushftp.debug", "1")) >= level) {
                if (s.trim().length() > 0) {
                    Properties p = new Properties();
                    p.put("tag", tag);
                    p.put("level", String.valueOf(level));
                    p.put("data", s.trim());
                    log.addElement(p);
                }
                return true;
            }
        } else if (Integer.parseInt(System.getProperty("crushtunnel.debug", "0")) >= level) {
            if (s.trim().length() > 0) {
                System.out.println(s.trim());
            }
            return true;
        }
        return false;
    }

    public static boolean log(String tag, int level, Throwable e) {
        if (Integer.parseInt(System.getProperty("crushtunnel.debug", "0")) >= level || Integer.parseInt(System.getProperty("crushftp.debug", "1")) >= level) {
            Common.log(tag, level, Thread.currentThread().getName());
            Common.log(tag, level, e.toString());
            StackTraceElement[] ste = e.getStackTrace();
            int x = 0;
            while (x < ste.length) {
                Common.log(tag, level, String.valueOf(ste[x].getClassName()) + "." + ste[x].getMethodName() + ":" + ste[x].getLineNumber());
                ++x;
            }
            if (level >= 2) {
                System.out.println(new Date());
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public static boolean log(String tag, int level, Exception e) {
        return Common.log(tag, level, (Throwable)e);
    }

    public static String replace_str(String master_string, String search_data, String replace_data) {
        if (search_data.equals(replace_data) || search_data.equals("")) {
            return master_string;
        }
        int start_loc = 0;
        int end_loc = 0;
        try {
            start_loc = master_string.indexOf(search_data);
            while (start_loc >= 0) {
                end_loc = start_loc + search_data.length();
                master_string = String.valueOf(master_string.substring(0, start_loc)) + replace_data + master_string.substring(end_loc);
                start_loc = master_string.indexOf(search_data, start_loc + replace_data.length());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return master_string;
    }

    public static String normalize2(String s) {
        if (System.getProperty("java.version").startsWith("1.4") || Common.machine_is_x() && !Common.machine_is_x_10_6_plus()) {
            return s;
        }
        try {
            return Normalizer.normalize(s, Normalizer.Form.NFD);
        }
        catch (NoClassDefFoundError e) {
            return s;
        }
    }

    public static String url_decode3(String master_string) {
        master_string = Common.replace_str(master_string, "%%", "\u00fe");
        int start_loc = 0;
        try {
            start_loc = master_string.indexOf("%");
            while (start_loc >= 0) {
                int val2;
                String tester = master_string.substring(start_loc + 1, start_loc + 3);
                int val = tester.charAt(0) - 48;
                if (val > 9) {
                    val -= 7;
                }
                if ((val2 = tester.charAt(1) - 48) > 9) {
                    val2 -= 7;
                }
                val = val * 16 + val2;
                master_string = Common.replace_str(master_string, "%" + tester, "" + (char)val);
                start_loc = master_string.indexOf("%", start_loc + 1);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        master_string = Common.replace_str(master_string, "\u00fe", "%");
        return master_string;
    }

    public static String url_decode(String s) {
        try {
            if (s.indexOf("% ") < 0 && !s.endsWith("%")) {
                String s2 = s.replace('+', '\u00fe');
                s2 = URLDecoder.decode(s2, "UTF8");
                s = s2.replace('\u00fe', '+');
            }
        }
        catch (Exception e) {
            Common.log("SERVER", 2, e);
        }
        int x = 0;
        while (s != null && x < 32) {
            if (x < 9 || x > 13) {
                s = s.replace((char)x, '_');
            }
            ++x;
        }
        return s;
    }

    public static String url_encode_all(String master_string) {
        String return_str = "";
        int x = 0;
        while (x < master_string.length()) {
            String temp = Long.toHexString(master_string.charAt(x));
            if ((temp = temp.toUpperCase()).length() == 1) {
                temp = "0" + temp;
            }
            return_str = String.valueOf(return_str) + "%" + temp;
            ++x;
        }
        return return_str;
    }

    public static String url_encode(String master_string) {
        return Common.url_encode(master_string, "");
    }

    /*
     * Unable to fully structure code
     */
    public static String url_encode(String master_string, String OK_chars) {
        return_str = "";
        try {
            master_string = Common.replace_str(master_string, "+", "_-_THIS_-_IS_-_A_-_PLUS_-_");
            return_str = URLEncoder.encode(master_string, "utf-8");
            x = 0;
            while (x < OK_chars.length()) {
                s = URLEncoder.encode(String.valueOf(OK_chars.charAt(x)), "utf-8");
                return_str = Common.replace_str(return_str, s, String.valueOf(OK_chars.charAt(x)));
                ++x;
            }
            return_str = Common.replace_str(return_str, "+", "%20");
            return_str = Common.replace_str(return_str, "_-_THIS_-_IS_-_A_-_PLUS_-_", "+");
            return return_str;
        }
        catch (Exception e) {
            Common.log("SERVER", 2, e);
            return_str = "";
            x = 0;
            ** while (x < master_string.length())
        }
lbl-1000:
        // 1 sources

        {
            temp = Long.toHexString(master_string.charAt(x));
            val = master_string.charAt(x);
            if (val >= 48L && val <= 57L || val >= 65L && val <= 90L || val >= 97L && val <= 122L || val == 46L || val == 95L || OK_chars.indexOf(String.valueOf(master_string.charAt(x))) >= 0) {
                return_str = String.valueOf(return_str) + master_string.charAt(x);
            } else {
                if ((temp = temp.toUpperCase()).length() == 1) {
                    temp = "0" + temp;
                }
                return_str = String.valueOf(return_str) + "%" + temp;
            }
            ++x;
            continue;
        }
lbl30:
        // 1 sources

        return return_str;
    }

    public ServerSocket getServerSocket(ServerSocketFactory ssf, int serverPort, String listen_ip, String disabled_ciphers, boolean needClientAuth) throws Exception {
        SSLServerSocket serverSocket = null;
        try {
            if (listen_ip != null) {
                serverSocket = (SSLServerSocket)ssf.createServerSocket(serverPort, 1000, InetAddress.getByName(listen_ip));
            }
        }
        catch (SocketException e) {
            Common.log("SERVER", 2, e);
        }
        if (serverSocket == null) {
            serverSocket = (SSLServerSocket)ssf.createServerSocket(serverPort, 1000);
        }
        Common.setEnabledCiphers(disabled_ciphers, null, serverSocket);
        serverSocket.setNeedClientAuth(needClientAuth);
        return serverSocket;
    }

    public SSLContext getSSLContext(String KEYSTORE, String TRUSTSTORE, String keystorepass, String keypass, String secureType, boolean needClientAuth, boolean acceptAnyCert) throws Exception {
        if (KEYSTORE.equals("PKCS11")) {
            return Common.getFips();
        }
        if (KEYSTORE.endsWith("cacerts") && needClientAuth) {
            needClientAuth = false;
        }
        if (TRUSTSTORE == null) {
            TRUSTSTORE = KEYSTORE;
        }
        String className = System.getProperty("crushftp.sslprovider", "");
        try {
            if (!providerAdded && !className.equals("")) {
                Common.log("SERVER", 0, "Adding SSL provider:" + className);
                Provider provider = (Provider)Thread.currentThread().getContextClassLoader().loadClass(className).newInstance();
                Security.addProvider(provider);
                providerAdded = true;
            }
        }
        catch (Exception e) {
            throw new Exception("Failed loading security provider " + className, e);
        }
        KeyStore keystore = null;
        KeyStore truststore = null;
        String keystoreFormat = "JKS";
        if (KEYSTORE.toUpperCase().endsWith("PKCS12") || KEYSTORE.toUpperCase().endsWith("P12") || KEYSTORE.toUpperCase().endsWith("PFX")) {
            keystoreFormat = "pkcs12";
        }
        if (keystore == null) {
            InputStream in;
            Properties p;
            keystore = KeyStore.getInstance(keystoreFormat);
            if (KEYSTORE.equals("builtin")) {
                keystore.load(this.getClass().getResource("/assets/builtin").openStream(), "crushftp".toCharArray());
            } else if (System2.containsKey("crushftp.keystores." + KEYSTORE.toUpperCase().replace('\\', '/'))) {
                p = (Properties)System2.get("crushftp.keystores." + KEYSTORE.toUpperCase().replace('\\', '/'));
                keystore.load(new ByteArrayInputStream((byte[])p.get("bytes")), p.getProperty("keystorepass", keystorepass).toCharArray());
            } else {
                in = null;
                try {
                    if (!new File_S(KEYSTORE).exists()) {
                        Common.log("SERVER", 0, "Couldn't find keystore " + KEYSTORE + ", ignoring it.");
                        keystore.load(this.getClass().getResource("/assets/builtin").openStream(), "crushftp".toCharArray());
                        KEYSTORE = "builtin";
                    } else {
                        in = new FileInputStream(new File_S(KEYSTORE));
                        keystore.load(in, keystorepass.toCharArray());
                    }
                }
                finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }
            truststore = KeyStore.getInstance(keystoreFormat);
            if (KEYSTORE.equals("builtin")) {
                truststore.load(this.getClass().getResource("/assets/builtin").openStream(), "crushftp".toCharArray());
            } else if (needClientAuth) {
                if (System2.containsKey("crushftp.keystores." + TRUSTSTORE.toUpperCase().replace('\\', '/'))) {
                    p = (Properties)System2.get("crushftp.keystores." + TRUSTSTORE.toUpperCase().replace('\\', '/'));
                    truststore.load(new ByteArrayInputStream((byte[])p.get("bytes")), p.getProperty("keystorepass", keystorepass).toCharArray());
                } else {
                    in = null;
                    try {
                        if (!new File_S(TRUSTSTORE).exists()) {
                            Common.log("SERVER", 0, "Couldn't find truststore " + TRUSTSTORE + ", ignoring it.");
                            truststore.load(this.getClass().getResource("/assets/builtin").openStream(), "crushftp".toCharArray());
                            TRUSTSTORE = "builtin";
                        } else {
                            in = new FileInputStream(new File_S(TRUSTSTORE));
                            truststore.load(in, keystorepass.toCharArray());
                        }
                    }
                    finally {
                        if (in != null) {
                            in.close();
                        }
                    }
                }
            }
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        if (KEYSTORE.equals("builtin")) {
            kmf.init(keystore, "crushftp".toCharArray());
            if (needClientAuth) {
                kmf.init(truststore, "crushftp".toCharArray());
            }
        } else {
            try {
                kmf.init(keystore, keypass.toCharArray());
            }
            catch (Exception e) {
                kmf.init(keystore, keystorepass.toCharArray());
            }
        }
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};
        SSLContext sslc = SSLContext.getInstance(secureType);
        if (needClientAuth) {
            if (acceptAnyCert) {
                sslc.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());
            } else {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(truststore);
                sslc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
            }
        } else if (acceptAnyCert) {
            sslc.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());
        } else {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keystore);
            sslc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        }
        return sslc;
    }

    private static SSLContext getFips() throws Exception {
        Class<?> c = Thread.currentThread().getContextClassLoader().loadClass("com.sun.net.ssl.internal.ssl.Provider");
        Constructor<?> cons = c.getConstructor(String.class);
        cons.newInstance("SunPKCS11-NSS");
        String keystoreFormat = "PKCS11";
        KeyStore keystore = KeyStore.getInstance(keystoreFormat);
        keystore.load(null, "".toCharArray());
        KeyStore truststore = KeyStore.getInstance(keystoreFormat);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keystore);
        SSLContext sslc = SSLContext.getInstance("TLS");
        kmf.init(null, "".toCharArray());
        sslc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), SecureRandom.getInstance("PKCS11"));
        return sslc;
    }

    public static void setEnabledCiphers(String disabled_ciphers, SSLSocket sock, SSLServerSocket serverSock) {
        if (!disabled_ciphers.equals("")) {
            Vector<String> enabled_ciphers = new Vector<String>();
            String[] ciphers = null;
            if (sock != null) {
                ciphers = sock.getSupportedCipherSuites();
            }
            if (serverSock != null) {
                ciphers = serverSock.getSupportedCipherSuites();
            }
            int x = 0;
            while (x < ciphers.length) {
                if (disabled_ciphers.indexOf("(" + ciphers[x] + ")") < 0) {
                    enabled_ciphers.addElement(ciphers[x]);
                }
                ++x;
            }
            ciphers = new String[enabled_ciphers.size()];
            x = 0;
            while (x < enabled_ciphers.size()) {
                ciphers[x] = enabled_ciphers.elementAt(x).toString();
                ++x;
            }
            if (sock != null) {
                sock.setEnabledCipherSuites(ciphers);
            }
            if (serverSock != null) {
                serverSock.setEnabledCipherSuites(ciphers);
            }
        }
    }

    public static String winPath(String path) {
        return path.replace('\\', '/');
    }

    public static String exec(String[] c) throws Exception {
        Process proc = Runtime.getRuntime().exec(c);
        try (BufferedReader br1 = null;){
            br1 = new BufferedReader(new InputStreamReader(proc.getInputStream(), "UTF8"));
            String data = "";
            String result1 = "";
            while ((data = br1.readLine()) != null) {
                result1 = String.valueOf(result1) + data + "\r\n";
            }
            br1.close();
            proc.waitFor();
            String string = result1;
            return string;
        }
    }

    public static String[] getCommandAction(String type, Properties p) {
        String[] s = null;
        if (type.equals("open_afp")) {
            s = new String[]{"open", "afp://127.0.0.1:" + p.getProperty("localPort") + "/"};
        } else if (type.equals("open_smb")) {
            s = new String[]{"open", "smb://0.0.0.0:" + p.getProperty("localPort") + "/"};
        } else if (type.equals("open_vnc")) {
            s = new String[]{"open", "vnc://127.0.0.1:" + p.getProperty("localPort") + "/"};
        } else {
            type = Common.replace_str(type, "{localPort}", p.getProperty("localPort"));
            type = Common.replace_str(type, "%localPort%", p.getProperty("localPort"));
            s = type.split(";");
        }
        return s;
    }

    public static String makeBoundary(int len) {
        String chars = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String rand = "";
        int i = 0;
        while (i < len) {
            rand = String.valueOf(rand) + chars.charAt(rn.nextInt(chars.length()));
            ++i;
        }
        return rand;
    }

    public static int getRandomInt() {
        return rn.nextInt();
    }

    public static int getRandomInt(int max) {
        return rn.nextInt(max);
    }

    public static String dots(String s) {
        String t;
        boolean uncFix = s.indexOf(":////") > 0;
        s = s.replace('\\', '/');
        String s2 = "";
        while (s.indexOf("%") >= 0 && !s.equals(s2)) {
            s2 = s;
            s = Common.url_decode(s);
            s = s.replace('\\', '/');
        }
        if (s.startsWith("../")) {
            s = s.substring(2);
        }
        if (s.endsWith("..")) {
            s = String.valueOf(s) + "/";
        }
        while (s.indexOf("/./") >= 0) {
            t = s.substring(0, s.indexOf("/./"));
            s = t = String.valueOf(t) + s.substring(s.indexOf("/./") + 2);
        }
        while (s.indexOf("/../") >= 0) {
            t = s.substring(0, s.indexOf("/../"));
            t = Common.all_but_last(t);
            s = t = String.valueOf(t) + s.substring(s.indexOf("/../") + 4);
        }
        if (s.endsWith("/.")) {
            s = s.substring(0, s.length() - 1);
        }
        if (s.startsWith("../")) {
            s = s.substring(2);
        }
        if (s.toLowerCase().indexOf("s3:/") < 0 && s.toLowerCase().indexOf("s3crush:/") < 0) {
            if (!uncFix) {
                s = Common.replace_str(s, "://", ":\\~~\\~~");
            }
            boolean unc = s.startsWith("////");
            while (s.indexOf("//") >= 0) {
                s = Common.replace_str(s, "//", "/");
            }
            if (unc) {
                s = "///" + s;
            }
            if (!uncFix) {
                s = Common.replace_str(s, ":\\~~\\~~", "://");
            }
        }
        if (uncFix) {
            s = String.valueOf(s.substring(0, s.indexOf(":") + 1)) + "///" + s.substring(s.indexOf(":") + 1);
        }
        if (s.indexOf("!!!") >= 0 && s.indexOf("~", s.indexOf("!!!")) > 0) {
            if (s.contains("/")) {
                String[] data = s.split("/");
                int x = 0;
                while (x < data.length) {
                    String part = data[x];
                    String temp = part;
                    if (temp.indexOf("!!!") >= 0 && temp.indexOf("~", temp.indexOf("!!!")) > 0) {
                        temp = Common.replace_str(temp, "!!!", "");
                        temp = Common.replace_str(temp, "~", "");
                        s = Common.replace_str(s, part, temp);
                    }
                    ++x;
                }
            } else if (s.indexOf("!!!") >= 0 && s.indexOf("~", s.indexOf("!!!")) > 0) {
                s = Common.replace_str(s, "!!!", "");
                s = Common.replace_str(s, "~", "");
            }
        }
        return s;
    }

    public static String all_but_last(String item) {
        String master = item = item.replace('\\', '/');
        if ((item = item.substring(0, item.lastIndexOf("/", item.length() - 2) + 1)).equals("")) {
            item = master.substring(0, master.lastIndexOf("\\", master.length() - 2) + 1);
        }
        return item;
    }

    public static String last(String item) {
        item = item.replace('\\', '/');
        item = item.substring(item.lastIndexOf("/", item.length() - 2) + 1);
        return item;
    }

    public static String first(String item) {
        item = item.replace('\\', '/');
        item = item.substring(0, item.indexOf("/", 1));
        return item;
    }

    public static Socket getSSLSocket(String host, int port, boolean acceptAnyCert) throws Exception {
        SSLContext sc = Common.getSSLContext();
        if (acceptAnyCert) {
            sc.init(null, trustAllCerts, new SecureRandom());
        }
        Socket sock = new Socket(host, port);
        sock = (SSLSocket)sc.getSocketFactory().createSocket(sock, host, port, true);
        Common.configureSSLTLSSocket(sock, System.getProperty("crushftp.tls_version_client", "SSLv2Hello,TLSv1,TLSv1.1,TLSv1.2"));
        return sock;
    }

    public static ServerSocket getSSLServerSocket(int serverPort, String listen_ip, boolean acceptAnyCert) throws Exception {
        SSLContext sslc = SSLContext.getInstance("TLS");
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(Common.class.getResource("/assets/builtin").openStream(), "crushftp".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keystore, "crushftp".toCharArray());
        if (acceptAnyCert) {
            sslc.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());
        } else {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keystore);
            sslc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        }
        SSLServerSocket serverSocket = null;
        serverSocket = listen_ip == null ? (SSLServerSocket)sslc.getServerSocketFactory().createServerSocket(serverPort, 1000, InetAddress.getByName("0.0.0.0")) : (SSLServerSocket)sslc.getServerSocketFactory().createServerSocket(serverPort, 1000, InetAddress.getByName(listen_ip));
        return serverSocket;
    }

    public static ServerSocket getSSLServerSocket(int serverPort, String listen_ip, boolean acceptAnyCert, String keystore_path, String keystore_pass, String keystore_key_pass) throws Exception {
        SSLContext sslc = SSLContext.getInstance("TLS");
        KeyStore keystore = KeyStore.getInstance("JKS");
        InputStream keystore_in = null;
        keystore_in = keystore_path == null || keystore_path.equals("") || keystore_path.equalsIgnoreCase("builtin") ? Common.class.getResource("/assets/builtin").openStream() : new FileInputStream(keystore_path);
        keystore.load(keystore_in, keystore_pass.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keystore, keystore_key_pass.toCharArray());
        if (acceptAnyCert) {
            sslc.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());
        } else {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keystore);
            sslc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        }
        SSLServerSocket serverSocket = null;
        serverSocket = listen_ip == null ? (SSLServerSocket)sslc.getServerSocketFactory().createServerSocket(serverPort, 1000, InetAddress.getByName("0.0.0.0")) : (SSLServerSocket)sslc.getServerSocketFactory().createServerSocket(serverPort, 1000, InetAddress.getByName(listen_ip));
        return serverSocket;
    }

    public static void trustEverything() {
        try {
            SSLContext sc = Common.getSSLContext();
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier hv = new HostnameVerifier(){

                @Override
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
        }
        catch (Exception e) {
            Common.log("SERVER", 1, e);
        }
    }

    public static SSLContext getSSLContext() {
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLSv1.2");
        }
        catch (NoSuchAlgorithmException e1) {
            try {
                sc = SSLContext.getInstance("TLSv1.1");
            }
            catch (NoSuchAlgorithmException e) {
                try {
                    sc = SSLContext.getInstance("TLSv1");
                }
                catch (NoSuchAlgorithmException e2) {
                    try {
                        sc = SSLContext.getInstance("SSL");
                    }
                    catch (NoSuchAlgorithmException e3) {
                        Common.log("SERVER", 0, e3);
                    }
                }
            }
        }
        return sc;
    }

    public static void streamCopier(InputStream in, OutputStream out) throws InterruptedException {
        Common.streamCopier(null, null, in, out, true, true, true);
    }

    public static void streamCopier(InputStream in, OutputStream out, boolean async) throws InterruptedException {
        Common.streamCopier(null, null, in, out, async, true, true);
    }

    public static void streamCopier(InputStream in, OutputStream out, boolean async, boolean closeInput, boolean closeOutput) throws InterruptedException {
        Common.streamCopier(null, null, in, out, async, closeInput, closeOutput);
    }

    public static void streamCopier(final Socket sock1, final Socket sock2, final InputStream in, final OutputStream out, boolean async, final boolean closeInput, final boolean closeOutput) throws InterruptedException {
        Runnable r = new Runnable(){

            @Override
            public void run() {
                block40: {
                    InputStream inp = in;
                    OutputStream outp = out;
                    try {
                        try {
                            byte[] b = new byte[65535];
                            int bytesRead = 0;
                            while (bytesRead >= 0) {
                                bytesRead = inp.read(b);
                                if (bytesRead < 0) continue;
                                outp.write(b, 0, bytesRead);
                            }
                        }
                        catch (Exception e) {
                            if (e.getMessage() == null || !e.getMessage().equalsIgnoreCase("Socket closed") && !e.getMessage().equalsIgnoreCase("Connection reset")) {
                                Common.log("SERVER", 2, e);
                            }
                            if (closeInput) {
                                try {
                                    inp.close();
                                }
                                catch (Exception e2) {
                                    Common.log("SERVER", 1, e2);
                                }
                            }
                            if (closeOutput) {
                                try {
                                    outp.close();
                                }
                                catch (Exception e3) {
                                    Common.log("SERVER", 1, e3);
                                }
                            }
                            if (!closeInput || !closeOutput) break block40;
                            try {
                                if (sock1 != null) {
                                    sock1.close();
                                }
                            }
                            catch (Exception e3) {
                                // empty catch block
                            }
                            try {
                                if (sock2 != null) {
                                    sock2.close();
                                }
                            }
                            catch (Exception e3) {}
                        }
                    }
                    finally {
                        if (closeInput) {
                            try {
                                inp.close();
                            }
                            catch (Exception e) {
                                Common.log("SERVER", 1, e);
                            }
                        }
                        if (closeOutput) {
                            try {
                                outp.close();
                            }
                            catch (Exception e) {
                                Common.log("SERVER", 1, e);
                            }
                        }
                        if (closeInput && closeOutput) {
                            try {
                                if (sock1 != null) {
                                    sock1.close();
                                }
                            }
                            catch (Exception exception) {}
                            try {
                                if (sock2 != null) {
                                    sock2.close();
                                }
                            }
                            catch (Exception exception) {}
                        }
                    }
                }
            }
        };
        try {
            if (async) {
                Worker.startWorker(r);
            } else {
                r.run();
            }
        }
        catch (Exception e) {
            Common.log("SERVER", 0, e);
        }
    }

    public static void copyStreams(InputStream in, Object out, boolean closeInput, boolean closeOutput) throws IOException {
        RandomAccessFile raf = null;
        BufferedOutputStream outStream = null;
        try {
            if (out instanceof RandomAccessFile) {
                raf = (RandomAccessFile)out;
            } else {
                outStream = new BufferedOutputStream((OutputStream)out);
            }
            BufferedInputStream inStream = new BufferedInputStream(in);
            byte[] b = new byte[32768];
            int bytesRead = 0;
            while (bytesRead >= 0) {
                bytesRead = inStream.read(b);
                if (bytesRead <= 0) continue;
                if (raf != null) {
                    raf.write(b, 0, bytesRead);
                    continue;
                }
                outStream.write(b, 0, bytesRead);
            }
            if (raf == null) {
                outStream.flush();
            }
        }
        finally {
            if (closeInput) {
                in.close();
            }
            if (closeOutput) {
                if (raf != null) {
                    raf.close();
                } else {
                    outStream.close();
                }
            }
        }
    }

    public static String login(String url, String username, String password) throws Exception {
        return Common.login(url, username, password, null);
    }

    public static String login(String url, String username, String password, String clientid) throws Exception {
        HttpURLConnection urlc = (HttpURLConnection)new URL(url).openConnection();
        urlc.setRequestMethod("POST");
        urlc.setUseCaches(false);
        urlc.setDoOutput(true);
        urlc.getOutputStream().write(("command=login&username=" + username + "&password=" + password + (clientid != null ? "&clientid=" + clientid : "")).getBytes("UTF8"));
        urlc.getResponseCode();
        String currentAuth = "";
        String ca = null;
        int x = 0;
        while (x < 100 && ca == null) {
            try {
                String header = urlc.getHeaderField(x);
                if (header != null) {
                    if (header.indexOf("currentAuth") >= 0) {
                        currentAuth = header;
                    }
                    if (header.indexOf("CrushAuth") >= 0) {
                        ca = header;
                    }
                }
            }
            catch (Exception header) {
                // empty catch block
            }
            ++x;
        }
        if (ca != null) {
            ca = ca.substring(ca.indexOf("CrushAuth=") + "CrushAuth=".length(), ca.indexOf(";", ca.indexOf("CrushAuth="))).trim();
        } else if (currentAuth != null && currentAuth.indexOf("currentAuth=") >= 0) {
            String c2f = currentAuth.substring(currentAuth.indexOf("currentAuth=") + "currentAuth=".length(), currentAuth.indexOf(";", currentAuth.indexOf("currentAuth="))).trim();
            urlc = (HttpURLConnection)new URL(url).openConnection();
            urlc.setRequestMethod("POST");
            urlc.setUseCaches(false);
            urlc.setDoOutput(true);
            urlc.getOutputStream().write(("command=getCrushAuth&c2f=" + c2f).getBytes("UTF8"));
            urlc.getResponseCode();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Common.streamCopier(urlc.getInputStream(), baos, false, true, true);
            ca = new String(baos.toByteArray());
            ca = ca.substring(ca.indexOf("<auth>") + "<auth>".length(), ca.indexOf("</auth>")).trim();
            ca = ca.substring("CrushAuth=".length());
        }
        urlc.disconnect();
        return ca;
    }

    public static String consumeResponse(InputStream in) throws Exception {
        byte[] b = new byte[32768];
        int bytesRead = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (bytesRead >= 0) {
            bytesRead = in.read(b);
            if (bytesRead <= 0) continue;
            baos.write(b, 0, bytesRead);
        }
        in.close();
        String s = new String(baos.toByteArray(), "UTF8");
        Common.log("HTTP_CLIENT", 2, s);
        return s;
    }

    public static Properties getConnectedSocks(boolean ssl) throws Exception {
        final Properties tmpSocks = new Properties();
        Properties socks = new Properties();
        int local_port = 0;
        Socket sock1 = null;
        while (true) {
            long start = System.currentTimeMillis();
            final ServerSocket ss = ssl ? Common.getSSLServerSocket(0, "127.0.0.1", true) : new ServerSocket(0);
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        tmpSocks.put(String.valueOf(ss.getLocalPort()), ss.accept());
                    }
                    catch (Exception e) {
                        Common.log("SERVER", 1, e);
                    }
                }
            });
            local_port = ss.getLocalPort();
            sock1 = ssl ? Common.getSSLSocket("127.0.0.1", local_port, true) : new Socket("127.0.0.1", local_port);
            while (!tmpSocks.containsKey(String.valueOf(local_port)) && System.currentTimeMillis() - start < 10000L) {
                Thread.sleep(10L);
            }
            ss.close();
            if (System.currentTimeMillis() - start < 10000L) break;
            sock1.close();
        }
        Socket sock2 = (Socket)tmpSocks.get(String.valueOf(local_port));
        socks.put("sock1", sock1);
        socks.put("sock2", sock2);
        return socks;
    }

    public static String percent(int i1, int i2) {
        return String.valueOf(i1 * 100 / i2) + "%";
    }

    public static String format_bytes_short(long bytes) {
        String return_str = "";
        try {
            long tb = 0x10000000000L;
            return_str = bytes > tb ? String.valueOf((float)((int)((float)bytes / (float)tb * 100.0f)) / 100.0f) + " TB" : (bytes > 0x40000000L ? String.valueOf((float)((int)((float)bytes / 1.07374182E9f * 100.0f)) / 100.0f) + " GB" : (bytes > 0x100000L ? String.valueOf((float)((int)((float)bytes / 1048576.0f * 100.0f)) / 100.0f) + " MB" : (bytes > 1024L ? String.valueOf((float)((int)((float)bytes / 1024.0f * 100.0f)) / 100.0f) + " KB" : String.valueOf(bytes) + " B")));
        }
        catch (Exception e) {
            Common.log("SERVER", 1, e);
        }
        return return_str;
    }

    public static String lpad(String s, int len) {
        if (len - s.length() > 0) {
            s = String.valueOf(pads[len - s.length()]) + s;
        }
        return s;
    }

    public static String rpad(String s, int len) {
        if (len - s.length() > 0) {
            s = String.valueOf(s) + pads[len - s.length()];
        }
        return s;
    }

    public static void writeEnd(BufferedOutputStream dos, String boundary) throws Exception {
        dos.write((String.valueOf(boundary) + "--\r\n").getBytes("UTF8"));
        dos.flush();
        dos.close();
    }

    public static void writeEntry(String key, String val, BufferedOutputStream dos, String boundary) throws Exception {
        dos.write((String.valueOf(boundary) + "\r\n").getBytes("UTF8"));
        dos.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n").getBytes("UTF8"));
        dos.write("\r\n".getBytes("UTF8"));
        dos.write(val.getBytes("UTF8"));
        dos.write("\r\n".getBytes("UTF8"));
    }

    public static GenericClient getClient(String url, String logHeader, Vector logQueue) {
        return Common.getClientSingle(url, logHeader, logQueue);
    }

    public static GenericClient getClientReplication(String real_url, String logHeader, Vector logQueue, String taskName) {
        if (taskName != null && taskName.toUpperCase().indexOf("NOREPLICAT") >= 0) {
            return Common.getClientSingle(real_url, logHeader, logQueue);
        }
        return Common.getClientReplication(real_url, logHeader, logQueue);
    }

    public static GenericClient getClientReplication(String real_url, String logHeader, Vector logQueue) {
        GenericClient c = Common.getClientSingle(Common.getBaseUrl(real_url), logHeader, logQueue);
        if (c == null) {
            if (logQueue != null) {
                logQueue.addElement("URL not understood:" + real_url);
            }
            Common.log("SERVER", 1, "URL not understood:" + real_url);
        }
        if (!System.getProperty("crushftp.replicated_vfs_root_url", "").equals("")) {
            Vector<GenericClient> clients = new Vector<GenericClient>();
            Vector<Properties> vItems = new Vector<Properties>();
            Properties originalvItem = new Properties();
            if (!real_url.endsWith("/")) {
                real_url = Common.all_but_last(real_url);
            }
            originalvItem.put("url", real_url);
            VRL root_vrl = new VRL(System.getProperty("crushftp.replicated_vfs_root_url"));
            String[] vrls = System.getProperty("crushftp.replicated_vfs_url").split(",");
            System.getProperties().put("crushftp.replicated_vfs", "true");
            clients.addElement(c);
            vItems.addElement(originalvItem);
            if (real_url.toUpperCase().startsWith(System.getProperty("crushftp.replicated_vfs_root_url", "").toUpperCase())) {
                int x = 0;
                while (x < vrls.length) {
                    if (!vrls[x].trim().equals("")) {
                        VRL vrl = new VRL(vrls[x].trim());
                        String relative_path = new VRL(originalvItem.getProperty("url")).toString().substring(root_vrl.toString().length());
                        Properties vItem = new Properties();
                        VRL vrl2 = null;
                        try {
                            vrl2 = new VRL(String.valueOf(vrl.getProtocol()) + "://" + VRL.vrlEncode(System.getProperty("crushftp.replicated_vfs_user")) + ":" + VRL.vrlEncode(Common.encryptDecrypt(System.getProperty("crushftp.replicated_vfs_pass"), false)) + "@" + vrl.getHost() + ":" + vrl.getPort() + vrl.getPath() + relative_path);
                            vItem.put("url", String.valueOf(vrl.getProtocol()) + "://" + VRL.vrlEncode(System.getProperty("crushftp.replicated_vfs_user")) + ":" + VRL.vrlEncode(Common.encryptDecrypt(System.getProperty("crushftp.replicated_vfs_pass"), false)) + "@" + vrl.getHost() + ":" + vrl.getPort() + vrl.getPath() + relative_path);
                        }
                        catch (Exception e) {
                            Common.log("SERVER", 1, e);
                        }
                        GenericClient c2 = Common.getClientSingle(Common.getBaseUrl(vItem.getProperty("url")), logHeader, logQueue);
                        c2.setConfig("replicated_login_user", vrl2.getUsername());
                        c2.setConfig("replicated_login_pass", vrl2.getPassword());
                        clients.addElement(c2);
                        vItems.addElement(vItem);
                    }
                    ++x;
                }
            }
            if (clients.size() > 1) {
                c = new GenericClientMulti(logHeader, log, originalvItem, vItems, clients, System.getProperty("crushftp.replicated_auto_play_journal").equals("true"));
            }
        }
        return c;
    }

    public static GenericClient getClientSingle(String url, String logHeader, Vector logQueue) {
        if (url.toUpperCase().startsWith("FTP:") || url.toUpperCase().startsWith("FTPS:") || url.toUpperCase().startsWith("FTPES:")) {
            return new FTPClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("FILE:")) {
            return new FileClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("RFILE:")) {
            return new RFileClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("MEMORY:")) {
            return new MemoryClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("ZIP:")) {
            return new ZipClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("SFTP:")) {
            return new SFTPClient(url, logHeader, logQueue);
        }
        if ((url.toUpperCase().startsWith("HTTP:") || url.toUpperCase().startsWith("HTTPS:")) && logHeader != null && logHeader.indexOf("CACHED") >= 0) {
            return new HTTPBufferedClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("HTTP:") || url.toUpperCase().startsWith("HTTPS:")) {
            return new HTTPClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("WEBDAV:") || url.toUpperCase().startsWith("WEBDAVS:")) {
            return new WebDAVClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("S3:")) {
            return new S3Client(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("S3CRUSH:")) {
            return new S3CrushClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("GDRIVE:")) {
            return new GDriveClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("GSTORAGE:")) {
            return new GStorageClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("CITRIX:")) {
            return new CitrixClient(url, logHeader, logQueue);
        }
        if (System.getProperty("crushftp.v10_beta", "false").equals("true") && url.toUpperCase().startsWith("SMB1:")) {
            return new SMB1Client(url, logHeader, logQueue);
        }
        if (System.getProperty("crushftp.v10_beta", "false").equals("true") && (url.toUpperCase().startsWith("SMB:") || url.toUpperCase().startsWith("SMB2:"))) {
            return new SMB4jClient(url, logHeader, logQueue);
        }
        if (System.getProperty("crushftp.v10_beta", "false").equals("true") && url.toUpperCase().startsWith("SMB3:")) {
            return new SMBjNQClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("SMB:")) {
            return new SMB1Client(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("SMB1:")) {
            return new SMB1Client(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("SMB2:")) {
            return new SMB4jClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("SMB3:")) {
            return new SMB4jClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("HADOOP:")) {
            return new HadoopClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("AZURE:")) {
            return new AzureClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("GLACIER:")) {
            return new GlacierClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("DROPBOX:")) {
            return new DropBoxClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("B2:")) {
            return new B2Client(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("ONEDRIVE:")) {
            return new OneDriveClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("SHAREPOINT:")) {
            return new OneDriveClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("SHAREPOINT2:")) {
            return new SharePointClient(url, logHeader, logQueue);
        }
        if (System.getProperty("crushftp.v10_beta", "false").equals("true") && url.toUpperCase().startsWith("BOX:")) {
            return new BoxClient(url, logHeader, logQueue);
        }
        if (url.toUpperCase().startsWith("CUSTOM.")) {
            return new CustomClient(url, logHeader, logQueue);
        }
        return null;
    }

    public static Socket getSockVRL(VRL u) throws Exception {
        int port = u.getPort();
        if (u.getProtocol().equalsIgnoreCase("HTTP") && port < 0) {
            port = 80;
        }
        if (u.getProtocol().equalsIgnoreCase("HTTPS") && port < 0) {
            port = 443;
        }
        if (u.getProtocol().equalsIgnoreCase("FTP") && port < 0) {
            port = 21;
        }
        Socket sock = new Socket(u.getHost(), port);
        if (u.getProtocol().equalsIgnoreCase("HTTPS")) {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};
            SSLContext sc = Common.getSSLContext();
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            sock = (SSLSocket)sc.getSocketFactory().createSocket(sock, u.getHost(), port, true);
            Common.configureSSLTLSSocket(sock, System.getProperty("crushftp.tls_version_client", "SSLv2Hello,TLSv1,TLSv1.1,TLSv1.2"));
            ((SSLSocket)sock).setUseClientMode(true);
            ((SSLSocket)sock).startHandshake();
        }
        return sock;
    }

    public static void configureSSLTLSSocket(Object sock, String list) {
        if (list == null || list.equals("*")) {
            return;
        }
        if (sock instanceof SSLServerSocket) {
            ((SSLServerSocket)sock).setEnabledProtocols(new String[]{"TLSv1"});
        } else if (sock instanceof SSLSocket) {
            ((SSLSocket)sock).setEnabledProtocols(new String[]{"TLSv1"});
        }
        if (list != null && !list.equals("")) {
            Vector<String> tls = new Vector<String>();
            int x = 0;
            while (x < list.split(",").length) {
                String s = list.split(",")[x];
                if (!s.trim().equals("")) {
                    if ((s = s.replace('t', 'T').replace('l', 'L').replace('s', 'S').replace('V', 'v')).toUpperCase().indexOf("SSLv2Hello".toUpperCase()) >= 0) {
                        s = Common.replace_str(s.toUpperCase(), "SSLV2HELLO", "SSLv2Hello");
                    }
                    if (!s.startsWith("TLSv") && !s.startsWith("SSLv")) {
                        s = "TLSv" + s;
                    }
                    if (!bad_tls_protocols.containsKey(s)) {
                        try {
                            if (!s.equals("SSLv2Hello")) {
                                if (sock instanceof SSLServerSocket) {
                                    ((SSLServerSocket)sock).setEnabledProtocols(new String[]{s});
                                } else if (sock instanceof SSLSocket) {
                                    ((SSLSocket)sock).setEnabledProtocols(new String[]{s});
                                }
                            }
                            tls.addElement(s);
                        }
                        catch (IllegalArgumentException e) {
                            bad_tls_protocols.put(s, "false");
                            Common.log("SERVER", 0, String.valueOf(s) + " not supported:" + e);
                        }
                    }
                }
                ++x;
            }
            String[] tls_str = new String[tls.size()];
            int x2 = 0;
            while (x2 < tls.size()) {
                tls_str[x2] = tls.elementAt(x2).toString();
                ++x2;
            }
            if (sock instanceof SSLServerSocket) {
                ((SSLServerSocket)sock).setEnabledProtocols(tls_str);
            } else if (sock instanceof SSLSocket) {
                ((SSLSocket)sock).setEnabledProtocols(tls_str);
                ((SSLSocket)sock).addHandshakeCompletedListener(new HandshakeCompletedListener(){

                    @Override
                    public void handshakeCompleted(HandshakeCompletedEvent event) {
                        if (System.getProperty("crushftp.ssl_renegotiation_blocked", "true").equals("true")) {
                            event.getSocket().setEnabledCipherSuites(new String[0]);
                        }
                    }
                });
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Socket grabDataSock(Properties requestSock) throws Exception {
        Vector data_sock_available = (Vector)System2.get("crushftp.dmz.data_sock_available");
        Object data_sock_available_lock = System2.get("crushftp.dmz.data_sock_available_lock");
        if (data_sock_available == null) {
            data_sock_available = (Vector)System2.get("crushftp.dmz.queue.sock");
            data_sock_available.addElement(requestSock);
            long start = System.currentTimeMillis();
            int wait = 10;
            while (System.currentTimeMillis() - start < 30000L) {
                try {
                    if (requestSock.containsKey("socket")) break;
                    Thread.sleep(wait);
                    if (wait >= 100) continue;
                    wait += 10;
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
            }
            data_sock_available.remove(requestSock);
            if (requestSock.get("socket") == null) {
                throw new IOException("failure: Waited 30 seconds for DMZ socket, giving up (v8).");
            }
            return (Socket)requestSock.remove("socket");
        }
        if (System2.get("crushftp.dmz.socket_lookup") != null) {
            Properties sockets = Common.getConnectedSocks(false);
            Socket queue_sock = (Socket)sockets.remove("sock1");
            Socket client_sock = (Socket)sockets.remove("sock2");
            Properties socket_lookup = (Properties)System2.get("crushftp.dmz.socket_lookup");
            Vector qwrite_queue = (Vector)System2.get("crushftp.dmz.qwrite_queue");
            Properties sock_info = new Properties();
            sock_info.put("socket", queue_sock);
            sock_info.put("out", queue_sock.getOutputStream());
            socket_lookup.put("" + queue_sock, sock_info);
            Properties p = new Properties();
            p.put("command", "create");
            p.put("sock", "" + queue_sock);
            p.put("port", requestSock.getProperty("port"));
            qwrite_queue.addElement(p);
            Common.startQueueSocket(queue_sock, "" + queue_sock);
            return client_sock;
        }
        Socket use_this_sock = null;
        Object object = data_sock_available_lock;
        synchronized (object) {
            int x = data_sock_available.size() - 1;
            if (x >= 0) {
                Properties p = (Properties)data_sock_available.elementAt(x);
                long time = Long.parseLong(p.getProperty("time"));
                Socket sock = (Socket)p.get("sock");
                Common.sockLog(sock, "data sock being used:" + (System.currentTimeMillis() - time) + "ms old.  data_sock_available size=" + data_sock_available.size());
                data_sock_available.remove(p);
                use_this_sock = sock;
            }
        }
        if (use_this_sock != null) {
            use_this_sock.setSoTimeout(0);
            use_this_sock.getOutputStream().write((String.valueOf(requestSock.getProperty("port")) + "                                                                                                           ").substring(0, 100).getBytes());
            use_this_sock.getOutputStream().flush();
            Common.sockLog(use_this_sock, "data sock loop used..alerting waiter.  data_sock_available size=" + data_sock_available.size());
        }
        return use_this_sock;
    }

    public static void startQueueSocket(final Socket sock, final String sock_name) {
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Vector qwrite_queue = (Vector)System2.get("crushftp.dmz.qwrite_queue");
                    int bytes_read = 0;
                    try {
                        byte[] b1 = new byte[0x100000];
                        InputStream in = sock.getInputStream();
                        while (bytes_read >= 0) {
                            bytes_read = in.read(b1);
                            if (bytes_read >= 0) {
                                byte[] b2 = new byte[bytes_read];
                                System.arraycopy(b1, 0, b2, 0, bytes_read);
                                Properties p = new Properties();
                                p.put("command", "data");
                                p.put("sock", sock_name);
                                p.put("b", b2);
                                p.put("len", String.valueOf(bytes_read));
                                while (qwrite_queue.size() > 5000) {
                                    Thread.sleep(100L);
                                }
                                qwrite_queue.addElement(p);
                                continue;
                            }
                            Properties p = new Properties();
                            p.put("command", "close");
                            p.put("sock", sock_name);
                            qwrite_queue.addElement(p);
                        }
                    }
                    catch (IOException e) {
                        Common.log("SERVER", 2, e);
                    }
                    catch (InterruptedException e) {
                        // empty catch block
                    }
                    try {
                        sock.close();
                    }
                    catch (IOException e) {
                        System.out.println(new Date());
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (IOException e) {
            Common.log("SERVER", 1, e);
        }
    }

    public static Socket getSocket(String protocol, VRL u, String use_dmz, String sticky_token) throws IOException {
        return Common.getSocket(protocol, u, use_dmz, sticky_token, 0);
    }

    public static Socket getSocket(String protocol, VRL u, String use_dmz, String sticky_token, int timeout) throws IOException {
        if (use_dmz.toLowerCase().startsWith("variable:")) {
            use_dmz = use_dmz.substring("variable:".length());
        }
        if ((System2.containsKey("crushftp.dmz.queue.sock") || System2.containsKey("crushftp.dmz.data_sock_available")) && (use_dmz.equals("") || use_dmz.equalsIgnoreCase("false") || use_dmz.equalsIgnoreCase("null") || use_dmz.equalsIgnoreCase("no") || use_dmz.startsWith("socks://") || use_dmz.startsWith("http://") || use_dmz.equalsIgnoreCase("internal://"))) {
            Socket sock2;
            Vector socket_queue = (Vector)System2.get("crushftp.dmz.queue.sock");
            if (socket_queue == null) {
                socket_queue = (Vector)System2.get("crushftp.dmz.queue");
            }
            Common.log("DMZ", 2, "GET:SOCKET:Requesting socket connection from internal server out of the pool using port:" + u.getPort());
            Properties mySock = new Properties();
            mySock.put("type", "GET:SOCKET");
            mySock.put("port", String.valueOf(u.getPort()));
            if (use_dmz.equalsIgnoreCase("internal://")) {
                mySock.put("port", String.valueOf(u.getHost()) + ":" + u.getPort());
            }
            mySock.put("data", new Properties());
            mySock.put("id", String.valueOf(Common.makeBoundary(10)) + new Date().getTime());
            mySock.put("sticky_token", sticky_token);
            long start_wait = System.currentTimeMillis();
            mySock.put("created", String.valueOf(start_wait));
            mySock.put("need_response", "true");
            long start = System.currentTimeMillis();
            int wait = 10;
            Vector data_sock_available = (Vector)System2.get("crushftp.dmz.data_sock_available");
            while (System.currentTimeMillis() - start < 30000L) {
                try {
                    sock2 = Common.grabDataSock(mySock);
                    if (sock2 != null) {
                        mySock.put("socket", sock2);
                        break;
                    }
                    Thread.sleep(wait);
                    if (wait < 100) {
                        wait += 10;
                    }
                }
                catch (Exception sock2) {
                    // empty catch block
                }
                if (data_sock_available == null || System.currentTimeMillis() - start <= 5000L || System.currentTimeMillis() - start >= 5500L) continue;
                Common.log("SERVER", 2, "DMZ is bored waiting for sockets from the internal server...data_sock_available size=" + data_sock_available.size());
            }
            if (mySock.get("socket") == null) {
                throw new IOException("failure: Waited 30 seconds for DMZ socket, giving up.");
            }
            sock2 = (Socket)mySock.remove("socket");
            Common.sockLog(sock2, "Waited for DMZ socket:" + (System.currentTimeMillis() - start_wait) + "ms");
            return sock2;
        }
        SocketException lastError = null;
        int x = 0;
        while (x < 3) {
            try {
                Socket sock;
                block44: {
                    sock = null;
                    try {
                        if (!(use_dmz.equals("") || use_dmz.equals("(current_server)") || use_dmz.equalsIgnoreCase("false") || use_dmz.equalsIgnoreCase("no") || use_dmz.startsWith("socks://") || use_dmz.startsWith("http://") || use_dmz.startsWith("internal://"))) {
                            sock = new DMZSocket(u, use_dmz);
                        }
                    }
                    catch (IOException e) {
                        if (("" + e).toUpperCase().indexOf("DMZ") >= 0) break block44;
                        Common.log("SERVER", 1, e);
                    }
                }
                if (sock == null) {
                    if (use_dmz.startsWith("socks://")) {
                        sock = new Socket(new Proxy(Proxy.Type.SOCKS, InetSocketAddress.createUnresolved(new VRL(use_dmz).getHost(), new VRL(use_dmz).getPort())));
                        sock.connect(InetSocketAddress.createUnresolved(u.getHost(), u.getPort()));
                    } else if (System.getProperty("java.net.useSystemProxies", "false").equals("true") || use_dmz.startsWith("http://")) {
                        Proxy p0 = null;
                        if (!use_dmz.startsWith("http://")) {
                            List<Proxy> proxies = ProxySelector.getDefault().select(URI.create(String.valueOf(u.getProtocol()) + "://" + u.getHost() + ":" + u.getPort()));
                            p0 = proxies.get(0);
                        }
                        if (p0 != null && p0.type().toString().equalsIgnoreCase("DIRECT")) {
                            sock = new Socket(u.getHost(), u.getPort());
                        } else if (p0 != null && p0.type().toString().equalsIgnoreCase("SOCKS")) {
                            sock = new Socket(p0);
                            sock.connect(InetSocketAddress.createUnresolved(u.getHost(), u.getPort()));
                        } else if (p0 != null && p0.type().toString().equalsIgnoreCase("HTTP") || use_dmz.startsWith("http://")) {
                            VRL vrl = null;
                            if (p0 != null) {
                                String url_temp = p0.toString().substring(p0.toString().indexOf("@") + 1).trim();
                                if (url_temp.indexOf("/") >= 0) {
                                    url_temp = url_temp.substring(url_temp.indexOf("/") + 1).trim();
                                }
                                System.out.println("Connecting through proxy server:" + url_temp);
                                if (url_temp.toUpperCase().startsWith("HTTP://")) {
                                    vrl = new VRL(String.valueOf(url_temp) + "/");
                                }
                                vrl = url_temp.startsWith("/") ? new VRL("http:/" + url_temp + "/") : new VRL("http://" + url_temp + "/");
                            } else {
                                vrl = new VRL(use_dmz);
                            }
                            System.out.println("Connecting through proxy server:" + vrl + " to location:" + u.getHost() + ":" + u.getPort());
                            sock = new Socket(vrl.getHost(), vrl.getPort());
                            String header = "CONNECT " + u.getHost() + ":" + u.getPort() + " HTTP/1.1\r\n";
                            header = String.valueOf(header) + "Host: " + u.getHost() + ":" + u.getPort() + "\r\n";
                            header = String.valueOf(header) + "Proxy-Connection: Keep-Alive\r\n";
                            header = String.valueOf(header) + "\r\n";
                            sock.getOutputStream().write(header.getBytes("UTF8"));
                            sock.getOutputStream().flush();
                            byte[] b1 = new byte[1];
                            StringBuffer sb = new StringBuffer();
                            int bytesRead = 0;
                            InputStream in = sock.getInputStream();
                            while (bytesRead >= 0) {
                                bytesRead = in.read(b1);
                                if (bytesRead > 0) {
                                    sb.append(new String(b1));
                                }
                                if (sb.toString().endsWith("\r\n\r\n")) break;
                            }
                            if (!(sb.toString().trim().startsWith("200") || sb.toString().trim().startsWith("HTTP/1.1 200") || sb.toString().trim().startsWith("HTTP/1.0 200"))) {
                                sock.close();
                                throw new IOException(sb.toString());
                            }
                        }
                        Common.sockLog(sock, "Using socket from pool:" + use_dmz);
                    } else {
                        String host = u.getHost();
                        if (host.indexOf("~") >= 0) {
                            sock = new Socket(host.split("~")[1], u.getPort(), InetAddress.getByName(host.split("~")[0]), 0);
                        } else if (timeout == 0) {
                            sock = new Socket(host, u.getPort());
                        } else {
                            sock = new Socket();
                            sock.connect(new InetSocketAddress(host, u.getPort()), timeout);
                        }
                    }
                }
                sock.setTcpNoDelay(true);
                return sock;
            }
            catch (SocketException e) {
                lastError = e;
                if (("" + lastError).toUpperCase().indexOf("REFUSED") >= 0) {
                    try {
                        Thread.sleep(100L);
                    }
                    catch (Exception exception) {}
                } else {
                    try {
                        Thread.sleep(10000L);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                ++x;
            }
        }
        throw new IOException(lastError);
    }

    public static void releaseSocket(Socket sock, VRL u, String sticky_token) {
        try {
            Common.sockLog(sock, "Disconnect " + u.getHost() + " / socktimeout=" + socketTimeout);
            sock.close();
        }
        catch (IOException e) {
            Common.log("SERVER", 1, e);
        }
    }

    public static long getDecryptedSize(String the_file_path) throws Exception {
        File_S f = new File_S(the_file_path);
        if (f.isDirectory()) {
            return -1L;
        }
        RandomAccessFile in = new RandomAccessFile(f, "r");
        int headerSize = encryptedNote.length() + encryptedDefaultSize.length();
        long size = in.length();
        byte[] b = new byte[headerSize];
        int bytesRead = in.read(b);
        String head = "";
        String tail = "";
        if (bytesRead >= 0) {
            head = new String(b, 0, bytesRead, "UTF8");
        }
        if (size > 30L) {
            in.seek(in.length() - 30L);
            bytesRead = in.read(b);
            if (bytesRead >= 0) {
                tail = new String(b, 0, bytesRead, "UTF8");
            }
        }
        in.close();
        if (tail.indexOf(":::" + System.getProperty("appname", "CrushFTP").toUpperCase()) >= 0) {
            try {
                if (tail.split("#").length > 0) {
                    tail = tail.substring(tail.indexOf(":::" + System.getProperty("appname", "CrushFTP").toUpperCase()));
                    tail = tail.split("#")[1].trim();
                    return Long.parseLong(tail.trim());
                }
            }
            catch (NumberFormatException e) {
                Common.log("SERVER", 1, e);
            }
        }
        if (size < (long)headerSize) {
            return -1L;
        }
        if (head.toUpperCase().startsWith("-----BEGIN PGP MESSAGE-----")) {
            head = head.substring("-----BEGIN PGP MESSAGE-----".length()).trim();
            try {
                if (head.split("#").length > 0) {
                    head = head.split("#")[1].trim();
                    return Long.parseLong(head.trim());
                }
            }
            catch (NumberFormatException e) {
                Common.log("SERVER", 1, e);
            }
            return -1L;
        }
        if (!head.startsWith(encryptedNote) && !head.startsWith(pgpChunkedheaderStr)) {
            return -1L;
        }
        head = head.substring(encryptedNote.length());
        return Long.parseLong(head.trim());
    }

    public static long getFileSize(String the_file_path) {
        try {
            long size = Common.getDecryptedSize(the_file_path);
            if (size >= 0L) {
                return size;
            }
        }
        catch (Exception e) {
            Common.log("SERVER", 1, e);
        }
        return new File_S(the_file_path).length();
    }

    public static long pgpOffsetAdjuster(long l, Properties controller) {
        if (controller.getProperty("pgpEncrypt", "false").equals("true") || controller.getProperty("pgpDecrypt", "false").equals("true")) {
            long chunkAndPadding = 0x100800L;
            long mbs = l / 0x100000L;
            l = mbs * chunkAndPadding;
            l += (long)(encryptedNote.length() + encryptedDefaultSize.length());
        }
        return l;
    }

    public static void generateKeyPair(String privateKeyPath, int keySize, int days, String password, String commonName, String[] cyphers) throws IOException {
        String[] hashingAlgorithms = new String[]{"SHA1", "SHA256", "SHA384", "SHA512", "MD5"};
        String[] compressions = new String[]{"ZIP", "ZLIB", "UNCOMPRESSED"};
        try {
            PGPKeyPair key = PGPKeyPair.generateKeyPair((int)keySize, (String)commonName, (String)"RSA", (String)password, (String[])compressions, (String[])hashingAlgorithms, (String[])cyphers, (long)days);
            key.exportPrivateKey(privateKeyPath, true);
            key.exportPublicKey(String.valueOf(privateKeyPath.substring(0, privateKeyPath.lastIndexOf("."))) + ".pub", true);
        }
        catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static void generateKeyPair(String privateKeyPath, int keySize, int days, String password, String commonName) throws IOException {
        String[] cyphers = new String[]{"CAST5", "AES_128", "AES_192", "AES_256", "TWOFISH"};
        Common.generateKeyPair(privateKeyPath, keySize, days, password, commonName, cyphers);
    }

    public static long getFreeRam() {
        return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory();
    }

    public static long getChunkSize(InputStream original_is) throws IOException {
        String data;
        byte[] chunkBytes = new byte[20];
        byte[] b = new byte[1];
        int bytesRead = 0;
        int loc = 0;
        while (bytesRead >= 0) {
            bytesRead = original_is.read(b);
            if (bytesRead <= 0) {
                return -1L;
            }
            chunkBytes[loc++] = b[0];
            if (loc <= 80 && (loc <= 1 || chunkBytes[loc - 2] != 13 || chunkBytes[loc - 1] != 10)) continue;
        }
        if ((data = new String(chunkBytes, 0, loc, "UTF8").trim()).equals("")) {
            return 0L;
        }
        return Long.parseLong(data.trim(), 16);
    }

    public static String getBaseUrl(String s) {
        return Common.getBaseUrl(s, true);
    }

    public static String getBaseUrl(String s, boolean s3_root_path) {
        String tmp_path;
        String s_original = s;
        if (s.indexOf(":") < 0) {
            s = "/";
        } else {
            if (s.toUpperCase().startsWith("GLACIER:/") && !s.endsWith("/")) {
                s = String.valueOf(s) + "/";
            }
            s = s.indexOf("@") > 0 && s.indexOf(":") != s.lastIndexOf(":") && !s.toLowerCase().startsWith("file:") && !s.toLowerCase().startsWith("azure:") ? (s.lastIndexOf("@") > s.lastIndexOf(":") && s.indexOf("@") == s.lastIndexOf("@") ? (System.getProperty("crushftp.v10_beta", "false").equals("true") && s.toUpperCase().startsWith("SMB3://") ? s.substring(0, s.indexOf("/", s.indexOf("/", s.lastIndexOf("@")) + 1) + 1) : s.substring(0, s.indexOf("/", s.lastIndexOf("@")) + 1)) : (System.getProperty("crushftp.v10_beta", "false").equals("true") && s.toUpperCase().startsWith("SMB3://") ? s.substring(0, s.indexOf("/", s.indexOf("/", s.indexOf(":", s.indexOf(":", 8)) + 1) + 1) + 1) : s.substring(0, s.indexOf("/", s.indexOf(":", s.indexOf(":", 8)) + 1) + 1))) : (s.toLowerCase().startsWith("file:/") && !s.toLowerCase().startsWith("file://") ? s.substring(0, s.indexOf("/", s.indexOf(":") + 1) + 1) : (s.toLowerCase().startsWith("file://") && !s.toLowerCase().startsWith("file:///") ? s.substring(0, s.indexOf("/", s.indexOf(":") + 2) + 1) : (s.toLowerCase().startsWith("azure:") ? s_original : s.substring(0, s.indexOf("/", s.indexOf(":") + 3) + 1))));
        }
        if ((s.toUpperCase().startsWith("S3:/") && s3_root_path || s.toUpperCase().startsWith("GSTORAGE:/")) && (tmp_path = new VRL(s_original).getPath()).length() > 1) {
            tmp_path = tmp_path.substring(1, tmp_path.indexOf("/", 1) + 1);
            s = String.valueOf(s) + tmp_path;
        }
        if (System.getProperty("crushftp.v10_beta", "false").equals("true") && s.toUpperCase().indexOf("SMB3:") >= 0) {
            s = s_original;
        }
        return s;
    }

    public static InputStream sanitizeXML(InputStream in) throws Exception {
        if (in instanceof ByteArrayInputStream) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Common.streamCopier(in, baos, false, true, true);
            String xml_str = new String(baos.toByteArray());
            if (xml_str.indexOf("<!") >= 0) {
                throw new Exception("XML syntax not allowed.");
            }
            in = new ByteArrayInputStream(baos.toByteArray());
        }
        return in;
    }

    public static Object readXMLObject(InputStream in) {
        Object result = null;
        try {
            in = Common.sanitizeXML(in);
            Document doc = Common.getSaxBuilder().build(in);
            result = Common.getElements(doc.getRootElement());
            in.close();
        }
        catch (Exception e) {
            Common.log("", 0, e);
        }
        return result;
    }

    public static Object readXMLObject(File_S file) {
        Object result = null;
        FileInputStream in = null;
        try {
            try {
                in = new FileInputStream(file);
                result = Common.readXMLObject(in);
            }
            catch (Exception e) {
                Common.log("", 0, e);
                try {
                    if (in != null) {
                        ((InputStream)in).close();
                    }
                }
                catch (Exception exception) {}
            }
        }
        finally {
            try {
                if (in != null) {
                    ((InputStream)in).close();
                }
            }
            catch (Exception exception) {}
        }
        return result;
    }

    public static Object readXMLObject(String path) {
        try {
            if (new File_S(path).exists()) {
                return Common.readXMLObject(new File_S(path));
            }
        }
        catch (Exception e) {
            Common.log("", 0, e);
        }
        return null;
    }

    public static Object getElements(Element element) {
        Cloneable result = null;
        if (element.getAttributeValue("type", "string").equalsIgnoreCase("properties")) {
            result = new Properties();
            List items2 = element.getChildren();
            if (items2.size() == 0) {
                return null;
            }
            int x = 0;
            while (x < items2.size()) {
                Element element2 = (Element)items2.get(x);
                Object o = Common.getElements(element2);
                String keyName = element2.getName();
                keyName = element2.getAttributeValue("name", keyName);
                if (o != null) {
                    ((Properties)result).put(keyName, o);
                }
                ++x;
            }
        } else if (element.getAttributeValue("type", "string").equalsIgnoreCase("vector")) {
            result = new Vector();
            List items2 = element.getChildren();
            if (items2.size() == 0) {
                return null;
            }
            int x = 0;
            while (x < items2.size()) {
                Element element2 = (Element)items2.get(x);
                Object o = Common.getElements(element2);
                if (o != null) {
                    ((Vector)result).addElement(o);
                }
                ++x;
            }
        } else if (element.getAttributeValue("type", "string").equalsIgnoreCase("string")) {
            return element.getText();
        }
        return result;
    }

    public static String getXMLString(Object obj, String root) throws IOException {
        return Common.getXMLString(obj, root, true);
    }

    public static String getXMLString(Object obj, String root, boolean pretty) throws IOException {
        Element element = new Element(root);
        Common.addElements(element, obj);
        Document doc = new Document(element);
        XMLOutputter xx = new XMLOutputter();
        Format formatter = null;
        formatter = Format.getPrettyFormat();
        formatter.setExpandEmptyElements(true);
        if (pretty) {
            formatter.setIndent("\t");
        } else {
            formatter.setIndent(" ");
        }
        xx.setFormat(formatter);
        String s = xx.outputString(doc);
        doc.removeContent();
        doc = null;
        element.removeContent();
        element.detach();
        element = null;
        return s;
    }

    public static void addParameters(Document document, Properties params) throws Exception {
        Element root = document.getRootElement();
        Iterator<Object> iterator = params.keySet().iterator();
        while (iterator != null && iterator.hasNext()) {
            String name = (String)iterator.next();
            String value = (String)params.get(name);
            Element element = new Element("variable");
            element.setNamespace(Namespace.getNamespace((String)"xsl"));
            element.setAttribute("name", name);
            element.addContent(value);
            root.addContent(1, (Content)element);
        }
    }

    public static void writeXMLObject(String path, Object obj, String root) throws IOException {
        byte[] b = Common.getXMLString(obj, root).getBytes("UTF8");
        RandomAccessFile raf = new RandomAccessFile(new File_S(path), "rw");
        raf.setLength(b.length);
        raf.seek(0L);
        raf.write(b);
        raf.close();
    }

    public static void addElements(Element element, Object obj) {
        block17: {
            try {
                if (obj != null && obj instanceof Properties) {
                    Properties p = (Properties)obj;
                    element.setAttribute("type", "properties");
                    Enumeration<Object> e = p.keys();
                    while (e.hasMoreElements()) {
                        String key = e.nextElement().toString();
                        Object val = p.get(key);
                        Element element2 = null;
                        try {
                            element2 = new Element(key);
                        }
                        catch (Exception ee) {
                            element2 = new Element("item");
                            element2.setAttribute("name", key);
                        }
                        element.addContent((Content)element2);
                        Common.addElements(element2, val);
                    }
                    break block17;
                }
                if (obj != null && obj instanceof Vector) {
                    Vector v = (Vector)obj;
                    element.setAttribute("type", "vector");
                    int x = 0;
                    while (x < v.size()) {
                        String keyName = element.getName();
                        keyName = element.getAttributeValue("name", keyName);
                        Element element2 = null;
                        try {
                            element2 = new Element(String.valueOf(element.getName()) + "_subitem");
                        }
                        catch (Exception ee) {
                            element2 = new Element("item_subitem");
                            element2.setAttribute("name", keyName);
                        }
                        element.addContent((Content)element2);
                        Common.addElements(element2, v.elementAt(x));
                        ++x;
                    }
                    break block17;
                }
                if (obj != null && obj instanceof VRL) {
                    String s = "" + obj;
                    try {
                        element.setText(s);
                    }
                    catch (Exception e) {
                        element.setText(URLEncoder.encode(s, "utf-8"));
                    }
                } else if (!(obj != null && obj instanceof BufferedReader || obj != null && obj instanceof BufferedWriter || obj == null)) {
                    String s = (String)obj;
                    try {
                        element.setText(s);
                    }
                    catch (Exception e) {
                        element.setText(URLEncoder.encode(s, "utf-8"));
                    }
                }
            }
            catch (Exception e) {
                Common.log("", 1, e);
            }
        }
    }

    public static void copy(String src, String dst, boolean overwrite) throws Exception {
        if (new File_S(src).isDirectory()) {
            new File_S(dst).mkdirs();
            return;
        }
        if (new File_S(dst).exists() && !overwrite) {
            return;
        }
        RandomAccessFile in = null;
        RandomAccessFile out = null;
        try {
            in = new RandomAccessFile(new File_S(src), "r");
            out = new RandomAccessFile(new File_S(dst), "rw");
            out.setLength(0L);
            byte[] b = new byte[32768];
            int bytesRead = 0;
            while (bytesRead >= 0) {
                bytesRead = in.read(b);
                if (bytesRead < 0) continue;
                out.write(b, 0, bytesRead);
            }
            in.close();
            out.close();
        }
        finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
        new File_S(dst).setLastModified(new File_S(src).lastModified());
    }

    public static boolean zip(String root_dir, Vector zipFiles, String outputPath) throws Exception {
        root_dir = String.valueOf(new File_S(root_dir).getCanonicalPath().replace('\\', '/')) + "/";
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(new File_S(outputPath)));
        zout.setLevel(9);
        int xx = 0;
        while (xx < zipFiles.size()) {
            String itemName;
            Properties item = (Properties)zipFiles.elementAt(xx);
            File_S file = new File_S(new VRL(item.getProperty("url")).getPath());
            if (file.isDirectory()) {
                itemName = (String.valueOf(file.getCanonicalPath().substring(root_dir.length())) + "/").replace('\\', '/');
                zout.putNextEntry(new ZipEntry(itemName));
            } else if (file.isFile()) {
                itemName = file.getCanonicalPath().substring(root_dir.length()).replace('\\', '/');
                if (itemName.indexOf(":") >= 0) {
                    itemName = itemName.substring(itemName.indexOf(":") + 1).trim();
                }
                zout.putNextEntry(new ZipEntry(itemName));
                RandomAccessFile in = new RandomAccessFile(file, "r");
                byte[] b = new byte[65535];
                int bytesRead = 0;
                while (bytesRead >= 0) {
                    bytesRead = in.read(b);
                    if (bytesRead <= 0) continue;
                    zout.write(b, 0, bytesRead);
                }
                in.close();
            }
            zout.closeEntry();
            ++xx;
        }
        zout.finish();
        zout.flush();
        zout.close();
        return true;
    }

    public static String encryptDecrypt(String s, boolean encrypt) throws Exception {
        return Common.encryptDecrypt(s, encrypt, new String(encryption_password));
    }

    public static String encryptDecrypt(String s, boolean encrypt, String key) throws Exception {
        if (s == null || s.equals("")) {
            return "";
        }
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(key.getBytes());
        DESKeySpec desKeySpec = new DESKeySpec(Base64.encodeBytes(md.digest()).getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        Cipher ecipher = Cipher.getInstance("DES");
        Cipher dcipher = Cipher.getInstance("DES");
        ecipher.init(1, secretKey);
        dcipher.init(2, secretKey);
        if (encrypt) {
            return Base64.encodeBytes(ecipher.doFinal(s.getBytes("UTF8")));
        }
        return new String(dcipher.doFinal(Base64.decode(s.replace(' ', '+'))), "UTF8");
    }

    public static String getHash(String key, String method, String salt) throws Exception {
        return String.valueOf(method.toUpperCase()) + ":" + Common.getHash(key, true, method, "", salt, false);
    }

    public static String getHash(String key, boolean base64, String method, String salt_data, String user_salt, boolean sha3_keccak_mode) throws Exception {
        String hash;
        byte[] b;
        Object md;
        byte[] salt_bytes = new byte[]{};
        if (salt_data != null && !salt_data.equals("") && new File_S(salt_data).exists() && (salt_bytes = (byte[])salt_hash.get(String.valueOf(new File_S(salt_data).lastModified()))) == null) {
            RandomAccessFile raf = new RandomAccessFile(new File_S(salt_data), "r");
            salt_bytes = new byte[(int)raf.length()];
            raf.read(salt_bytes);
            raf.close();
            salt_hash.put(String.valueOf(new File_S(salt_data).lastModified()), salt_bytes);
        }
        if ((method = method.toUpperCase()).equals("MD4")) {
            md = new MD4();
            byte[] b2 = key.getBytes("UnicodeLittleUnmarked");
            ((MD4)md).update(b2, 0, b2.length);
            if (base64) {
                return Base64.encodeBytes(((MD4)md).digest());
            }
            return new String(((MD4)md).digest());
        }
        md = null;
        SHA3.DigestSHA3 sha3 = null;
        KeccakDigest sha3_keccak = null;
        if (method.startsWith("SHA3")) {
            md = MessageDigest.getInstance("MD5");
            if (sha3_keccak_mode || method.equalsIgnoreCase("SHA3_KECCAK")) {
                sha3_keccak = new KeccakDigest(224);
            } else {
                sha3 = new SHA3.DigestSHA3(224);
            }
        } else {
            md = MessageDigest.getInstance(method);
        }
        if (user_salt.trim().length() > 0) {
            key = user_salt.startsWith("!!") ? String.valueOf(key) + user_salt.substring(2) : (user_salt.startsWith("!") ? String.valueOf(user_salt.trim().substring(1)) + key : String.valueOf(key) + user_salt.trim());
            b = key.getBytes(System.getProperty("crushftp.hash.encoding", "UTF8"));
            int cut_amount = Integer.parseInt(System.getProperty("crushftp.hash.cut", "0"));
            byte[] b2 = b;
            if (cut_amount > 0) {
                b2 = Arrays.copyOfRange(b, cut_amount - 1, b.length + 1);
            }
            if (sha3 != null) {
                sha3.update(b2);
            } else if (sha3_keccak != null) {
                sha3_keccak.update(b2, 0, b2.length);
            } else {
                ((MessageDigest)md).update(b2);
            }
        } else {
            b = key.getBytes(System.getProperty("crushftp.hash.encoding", "UTF8"));
            if (sha3 != null) {
                sha3.update(b);
            } else if (sha3_keccak != null) {
                sha3_keccak.update(b, 0, b.length);
            } else {
                ((MessageDigest)md).update(b);
            }
        }
        if (salt_bytes.length > 0) {
            if (sha3 != null) {
                sha3.update(salt_bytes);
            } else if (sha3_keccak != null) {
                sha3_keccak.update(salt_bytes, 0, salt_bytes.length);
            } else {
                ((MessageDigest)md).update(salt_bytes);
            }
        }
        if (method.equals("MD5")) {
            hash = new BigInteger(1, ((MessageDigest)md).digest()).toString(16).toLowerCase();
            while (hash.length() < 32) {
                hash = "0" + hash;
            }
            return hash;
        }
        if (method.equals("SHA512")) {
            hash = new BigInteger(1, ((MessageDigest)md).digest()).toString(16).toLowerCase();
            while (hash.length() < 128) {
                hash = "0" + hash;
            }
            return hash;
        }
        if (method.equals("SHA256")) {
            hash = new BigInteger(1, ((MessageDigest)md).digest()).toString(16).toLowerCase();
            while (hash.length() < 64) {
                hash = "0" + hash;
            }
            return hash;
        }
        if (method.equals("SHA3") && sha3 != null) {
            return Hex.toHexString((byte[])sha3.digest()).toLowerCase();
        }
        if (method.startsWith("SHA3") && sha3_keccak != null) {
            b = new byte[28];
            sha3_keccak.doFinal(b, 0);
            return Hex.toHexString((byte[])b).toLowerCase();
        }
        if (base64) {
            return Base64.encodeBytes(((MessageDigest)md).digest());
        }
        return new String(((MessageDigest)md).digest(), "UTF8");
    }

    public static void recurseDelete(String real_path, boolean test_mode) {
        if (real_path.trim().equals("/")) {
            return;
        }
        if (real_path.trim().equals("~")) {
            return;
        }
        if (real_path.indexOf(":") >= 0 && real_path.length() < 4) {
            return;
        }
        File_S f = new File_S(real_path);
        try {
            real_path = String.valueOf(f.getCanonicalPath()) + "/";
            f = new File_S(real_path);
        }
        catch (Exception e) {
            Common.log("", 1, e);
        }
        if (f.isDirectory()) {
            String[] files = f.list();
            int x = 0;
            while (x < files.length) {
                File_S f2 = new File_S(String.valueOf(real_path) + files[x]);
                if (!Common.isSymbolicLink(f2.getAbsolutePath())) {
                    if (f2.isDirectory()) {
                        Common.recurseDelete(String.valueOf(real_path) + files[x] + "/", test_mode);
                    }
                    if (test_mode) {
                        Common.log("", 0, "*****************DELETE:" + f2);
                    } else {
                        f2.delete();
                    }
                } else {
                    f2.delete();
                }
                ++x;
            }
        }
        if (test_mode) {
            Common.log("", 0, "*****************DELETE:" + f);
        } else {
            f.delete();
        }
    }

    public static boolean recurseDelete_U(String real_path, boolean test_mode) {
        if (real_path.trim().equals("/")) {
            return false;
        }
        if (real_path.trim().equals("~")) {
            return false;
        }
        if (real_path.indexOf(":") >= 0 && real_path.length() < 4) {
            return false;
        }
        File_U f = new File_U(real_path);
        try {
            real_path = String.valueOf(f.getCanonicalPath()) + "/";
            f = new File_U(real_path);
        }
        catch (Exception e) {
            Common.log("", 1, e);
        }
        if (f.isDirectory()) {
            String[] files = f.list();
            int x = 0;
            while (x < files.length) {
                File_U f2 = new File_U(String.valueOf(real_path) + files[x]);
                if (!Common.isSymbolicLink(f2.getAbsolutePath())) {
                    if (f2.isDirectory()) {
                        Common.recurseDelete_U(String.valueOf(real_path) + files[x] + "/", test_mode);
                    }
                    if (test_mode) {
                        Common.log("", 0, "*****************DELETE:" + f2);
                    } else {
                        f2.delete();
                    }
                } else {
                    f2.delete();
                }
                ++x;
            }
        }
        if (!test_mode) {
            return f.delete();
        }
        Common.log("", 0, "*****************DELETE:" + f);
        return true;
    }

    public static long recurseSize(String real_path, long size) {
        if (real_path.trim().equals("/")) {
            return size;
        }
        if (real_path.trim().equals("~")) {
            return size;
        }
        if (real_path.indexOf(":") >= 0 && real_path.length() < 4) {
            return size;
        }
        File_S f = new File_S(real_path);
        try {
            real_path = String.valueOf(f.getCanonicalPath()) + "/";
            f = new File_S(real_path);
        }
        catch (IOException e) {
            System.out.println(new Date());
            e.printStackTrace();
        }
        if (f.isDirectory()) {
            String[] files = f.list();
            int x = 0;
            while (x < files.length) {
                File_S f2 = new File_S(String.valueOf(real_path) + files[x]);
                if (!Common.isSymbolicLink(f2.getAbsolutePath())) {
                    size = f2.isDirectory() ? Common.recurseSize(String.valueOf(real_path) + files[x] + "/", size) : (size += f2.length());
                }
                ++x;
            }
        }
        return size += f.length();
    }

    public static String getMD5(InputStream in) throws Exception {
        MessageDigest m = MessageDigest.getInstance("MD5");
        try {
            in = new BufferedInputStream(in, 0x100000);
            byte[] b = new byte[0x100000];
            int bytesRead = 0;
            while (bytesRead >= 0) {
                bytesRead = in.read(b);
                if (bytesRead < 0) continue;
                m.update(b, 0, bytesRead);
            }
        }
        finally {
            in.close();
        }
        String s = new BigInteger(1, m.digest()).toString(16).toLowerCase();
        while (s.length() < 32) {
            s = "0" + s;
        }
        return s;
    }

    public static void getAllFileListing_S(Vector list, String path, int depth, boolean includeFolders) throws Exception {
        File_S item = new File_S(path);
        if (item.isFile()) {
            list.addElement(item);
        } else {
            Common.appendListing_S(path, list, "", depth, includeFolders);
        }
    }

    public static void appendListing_S(String path, Vector list, String dir, int depth, boolean includeFolders) throws Exception {
        String[] items;
        if (depth == 0) {
            return;
        }
        --depth;
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        if ((items = new File_S(String.valueOf(path) + dir).list()) == null) {
            return;
        }
        int x = 0;
        while (x < items.length) {
            File_S item = new File_S(String.valueOf(path) + dir + items[x]);
            if (item.isFile() || includeFolders) {
                if (item.lastModified() < 172800000L) {
                    item.setLastModified(new SimpleDateFormat("MM/dd/yy").parse("04/10/1998").getTime());
                    item = new File_S(String.valueOf(path) + dir + items[x]);
                }
                list.addElement(item);
            }
            if (item.isDirectory()) {
                Common.appendListing_S(path, list, String.valueOf(dir) + items[x] + "/", depth, includeFolders);
            }
            ++x;
        }
        if (items.length == 0) {
            list.addElement(new File_S(String.valueOf(path) + dir));
        }
    }

    public static String getPasswordPrompt(String label) {
        final JPasswordField jpf = new JPasswordField(30);
        JPanel messagePanel = new JPanel();
        messagePanel.add(new JLabel(label));
        messagePanel.add(jpf);
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    int x = 0;
                    while (x < 5) {
                        try {
                            Thread.sleep(100L);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        jpf.requestFocus();
                        ++x;
                    }
                }
            });
        }
        catch (IOException iOException) {
            // empty catch block
        }
        if (JOptionPane.showConfirmDialog(null, messagePanel, "Password", 2) == 0) {
            return new String(jpf.getPassword());
        }
        return null;
    }

    public static void doMD5Comparisons(final VRL localVrl, final VRL remoteVrl, String direction, Properties statusInfo, final String path3, final Vector chunksF1, final Vector chunksF2, final StringBuffer crushAuth, final StringBuffer status1, final StringBuffer status2, Vector byteRanges) throws InterruptedException {
        Thread keepTunnelActiveThread = null;
        final long localSize = new File_S(localVrl.getPath()).length();
        try {
            statusInfo.put(String.valueOf(direction) + "Status", String.valueOf(direction) + ": Getting MD5s for " + path3);
            try {
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        Thread.currentThread().setName("GetRemoteMD5s:" + path3);
                        try {
                            try {
                                Tunnel2.getRemoteMd5s(remoteVrl.toString(), path3, chunksF1, true, crushAuth, status1, localSize);
                            }
                            catch (Exception e) {
                                System.out.println(new Date());
                                e.printStackTrace();
                                status1.append("done");
                                if (chunksF1.size() == 0) {
                                    status2.append("skip");
                                }
                            }
                        }
                        finally {
                            status1.append("done");
                            if (chunksF1.size() == 0) {
                                status2.append("skip");
                            }
                        }
                    }
                });
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        Thread.currentThread().setName("GetLocalMD5s:" + localVrl);
                        try {
                            try {
                                Tunnel2.getLocalMd5s(new File_S(localVrl.getPath()), true, status2, chunksF2);
                            }
                            catch (Exception e) {
                                System.out.println(new Date());
                                e.printStackTrace();
                                status2.append("done");
                                if (chunksF2.size() == 0) {
                                    status1.append("skip");
                                }
                            }
                        }
                        finally {
                            status2.append("done");
                            if (chunksF2.size() == 0) {
                                status1.append("skip");
                            }
                        }
                    }
                });
            }
            catch (IOException iOException) {}
            while (status1.length() == 0 || status2.length() == 0) {
                if (status2.length() > 0 && chunksF1.size() > chunksF2.size()) {
                    status1.setLength(0);
                    status1.append("done");
                } else if (status1.length() > 0 && chunksF2.size() > chunksF1.size()) {
                    status2.setLength(0);
                    status2.append("done");
                }
                Thread.sleep(100L);
            }
            if (chunksF1.size() > 0) {
                byteRanges.removeAllElements();
                byteRanges.addAll(Tunnel2.compareMd5s(chunksF1, chunksF2, false));
            }
        }
        finally {
            status1.append("done");
            status2.append("done");
            if (keepTunnelActiveThread != null) {
                keepTunnelActiveThread.interrupt();
            }
        }
    }

    public static boolean do_searches(String filters, String data, boolean single, int iteration) {
        String[] multiple_filter = filters.split(",");
        int x = 0;
        while (x < multiple_filter.length) {
            String filter = multiple_filter[x];
            if (!filter.equals("") && Common.do_search(filter, data, single, iteration)) {
                return true;
            }
            ++x;
        }
        return false;
    }

    public static boolean do_search(String filter, String data, boolean single, int iteration) {
        return Common.do_search(filter, data, single, iteration, false);
    }

    public static boolean do_search(String filter, String data, boolean single, int iteration, boolean caseSensitive) {
        boolean opposite = filter.startsWith("!");
        if (opposite) {
            filter = filter.substring(1);
        }
        if (!filter.startsWith("REGEX:") && filter.indexOf("*") < 0 && filter.indexOf("?") < 0) {
            return opposite ? !filter.equals(data) : filter.equals(data);
        }
        return opposite ? !Common.doFilter(Common.getPattern(filter, caseSensitive), data) : Common.doFilter(Common.getPattern(filter, caseSensitive), data);
    }

    public static boolean doFilter(Pattern pattern, String data) {
        if (pattern == null) {
            return false;
        }
        boolean result = pattern.matcher(data).matches();
        if (!result) {
            result = pattern.matcher(data).find();
        }
        return result;
    }

    public static Pattern getPattern(String patternStr, boolean caseSensitive) {
        if (patternStr.startsWith("REGEX:")) {
            patternStr = patternStr.substring("REGEX:".length());
        } else {
            patternStr = Common.replace_str(patternStr, "^", "\\^");
            patternStr = Common.replace_str(patternStr, "$", "\\$");
            if (!(patternStr = "^" + patternStr).endsWith("*") && !patternStr.endsWith("$")) {
                patternStr = String.valueOf(patternStr) + "$";
            }
            patternStr = Common.replace_str(patternStr, ".", "\\.").replace('?', '.');
            patternStr = Common.replace_str(patternStr, "*", ".*");
            patternStr = Common.replace_str(patternStr, "+", "\\+");
            patternStr = Common.replace_str(patternStr, "-", "\\-");
            patternStr = Common.replace_str(patternStr, "=", "\\=");
            patternStr = Common.replace_str(patternStr, "<", "\\<");
            patternStr = Common.replace_str(patternStr, ">", "\\>");
            patternStr = Common.replace_str(patternStr, "(", "\\(");
            patternStr = Common.replace_str(patternStr, ")", "\\)");
            patternStr = Common.replace_str(patternStr, "[", "\\[");
            patternStr = Common.replace_str(patternStr, "]", "\\]");
        }
        if (caseSensitive) {
            try {
                return Pattern.compile(patternStr);
            }
            catch (Exception e) {
                Common.log("SERVER", 1, e);
            }
        } else {
            try {
                return Pattern.compile(patternStr, 2);
            }
            catch (Exception e) {
                Common.log("SERVER", 1, e);
            }
        }
        return null;
    }

    public static String safe_filename_characters(String s) {
        String safe = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String s2 = "";
        int x = 0;
        while (x < s.length()) {
            s2 = safe.indexOf(String.valueOf(s.charAt(x))) >= 0 ? String.valueOf(s2) + s.charAt(x) : String.valueOf(s2) + "_";
            ++x;
        }
        return s2;
    }

    public static String getCanonicalPath(String url) throws IOException {
        VRL vrl = new VRL(url);
        if (vrl.getProtocol().equalsIgnoreCase("file")) {
            return new File_S(vrl.getPath()).getCanonicalPath();
        }
        String tmp_user = vrl.getUsername();
        tmp_user = tmp_user.replace('/', '_');
        tmp_user = tmp_user.replace('\\', '_');
        tmp_user = tmp_user.replace('<', '_');
        tmp_user = tmp_user.replace('>', '_');
        tmp_user = tmp_user.replace('+', '_');
        tmp_user = tmp_user.replace('#', '_');
        tmp_user = tmp_user.replace('%', '_');
        tmp_user = tmp_user.replace('^', '_');
        tmp_user = tmp_user.replace(':', '_');
        tmp_user = tmp_user.replace(';', '_');
        return "/" + vrl.getProtocol().toLowerCase() + "/" + vrl.getHost().toLowerCase() + vrl.getPath();
    }

    public static SSLSocket getSSLSocket(String trust_path, String pass1, String pass2, boolean acceptAnyCert, Socket sock, String host, int port) throws IOException {
        SSLSocket ss = null;
        if (trust_path != null && trust_path.equals("PKCS11")) {
            try {
                ss = (SSLSocket)Common.getFips().getSocketFactory().createSocket(sock, host, port, true);
            }
            catch (Exception e) {
                Common.log("SERVER", 2, e);
                throw new IOException(e);
            }
            return ss;
        }
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new TrustManagerCustom(null, true, true)};
            SSLContext sslc = SSLContext.getInstance("TLS");
            KeyManager[] key_managers = null;
            if (trust_path != null && !trust_path.equals("")) {
                KeyStore trust_pfx = KeyStore.getInstance("PKCS12");
                KeyStore trust_jks = KeyStore.getInstance("JKS");
                KeyStore trust = null;
                String pass = "";
                try {
                    pass = Common.encryptDecrypt(pass1, false);
                    trust_pfx.load(new FileInputStream(new File_S(trust_path)), pass.toCharArray());
                    trust = trust_pfx;
                }
                catch (Exception e1) {
                    try {
                        pass = Common.encryptDecrypt(pass2, false);
                        trust_pfx.load(new FileInputStream(new File_S(trust_path)), pass.toCharArray());
                        trust = trust_pfx;
                    }
                    catch (Exception e2) {
                        try {
                            pass = Common.encryptDecrypt(pass1, false);
                            trust_jks.load(new FileInputStream(new File_S(trust_path)), pass.toCharArray());
                            trust = trust_jks;
                        }
                        catch (Exception e3) {
                            try {
                                pass = Common.encryptDecrypt(pass2, false);
                                trust_jks.load(new FileInputStream(new File_S(trust_path)), pass.toCharArray());
                                trust = trust_jks;
                            }
                            catch (Exception e4) {
                                System.out.println(new Date());
                                e4.printStackTrace();
                            }
                        }
                    }
                }
                if (trust != null) {
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    kmf.init(trust, pass.toCharArray());
                    key_managers = kmf.getKeyManagers();
                }
            }
            if (!acceptAnyCert) {
                trustAllCerts = null;
            }
            sslc.init(key_managers, trustAllCerts, new SecureRandom());
            ss = (SSLSocket)sslc.getSocketFactory().createSocket(sock, host, port, true);
            Common.configureSSLTLSSocket(ss, System.getProperty("crushftp.default_tls", "TLSv1,TLSv1.1,TLSv1.2"));
            return ss;
        }
        catch (Exception e) {
            Common.log("SERVER", 2, e);
            throw new IOException(e);
        }
    }

    public static String textFunctions_old(String the_line, String r1, String r2) throws Exception {
        int loc2;
        String inner;
        String params;
        String inner2;
        while (the_line.indexOf(String.valueOf(r1) + "encrypt_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "encrypt_end" + r2) >= 0) {
            inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "encrypt_start" + r2) + (String.valueOf(r1) + "encrypt_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "encrypt_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "encrypt_start" + r2))) + Common.encryptDecrypt(inner2, true) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "encrypt_end" + r2) + (String.valueOf(r1) + "encrypt_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "encode_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "encode_end" + r2) >= 0) {
            inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "encode_start" + r2) + (String.valueOf(r1) + "encode_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "encode_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "encode_start" + r2))) + Common.url_encode(inner2) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "encode_end" + r2) + (String.valueOf(r1) + "encode_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "encode_all_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "encode_all_end" + r2) >= 0) {
            inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "encode_all_start" + r2) + (String.valueOf(r1) + "encode_all_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "encode_all_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "encode_all_start" + r2))) + Common.url_encode_all(inner2) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "encode_all_end" + r2) + (String.valueOf(r1) + "encode_all_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "decode_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "decode_end" + r2) >= 0) {
            inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "decode_start" + r2) + (String.valueOf(r1) + "decode_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "decode_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "decode_start" + r2))) + Common.url_decode(inner2) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "decode_end" + r2) + (String.valueOf(r1) + "decode_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "upper_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "upper_end" + r2) >= 0) {
            inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "upper_start" + r2) + (String.valueOf(r1) + "upper_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "upper_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "upper_start" + r2))) + inner2.toUpperCase() + the_line.substring(the_line.indexOf(String.valueOf(r1) + "upper_end" + r2) + (String.valueOf(r1) + "upper_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "lower_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "lower_end" + r2) >= 0) {
            inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "lower_start" + r2) + (String.valueOf(r1) + "lower_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "lower_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "lower_start" + r2))) + inner2.toLowerCase() + the_line.substring(the_line.indexOf(String.valueOf(r1) + "lower_end" + r2) + (String.valueOf(r1) + "lower_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "md5_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "md5_end" + r2) >= 0) {
            inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "md5_start" + r2) + (String.valueOf(r1) + "md5_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "md5_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "md5_start" + r2))) + "MD5:" + Common.getMD5(new ByteArrayInputStream(inner2.getBytes("UTF8"))) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "md5_end" + r2) + (String.valueOf(r1) + "md5_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "chop_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "chop_end" + r2) >= 0) {
            inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "chop_start" + r2) + (String.valueOf(r1) + "chop_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "chop_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "chop_start" + r2))) + Common.all_but_last(inner2) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "chop_end" + r2) + (String.valueOf(r1) + "chop_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "htmlclean_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "htmlclean_end" + r2) >= 0) {
            inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "htmlclean_start" + r2) + (String.valueOf(r1) + "htmlclean_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "htmlclean_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "htmlclean_start" + r2))) + Common.html_clean(inner2) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "htmlclean_end" + r2) + (String.valueOf(r1) + "htmlclean_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "htmlclean2_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "htmlclean2_end" + r2) >= 0) {
            inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "htmlclean2_start" + r2) + (String.valueOf(r1) + "htmlclean2_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "htmlclean2_end" + r2));
            while (inner2.indexOf("<") >= 0) {
                if (inner2.indexOf("<") < 0 || inner2.indexOf(">") < 0) break;
                inner2 = String.valueOf(inner2.substring(0, inner2.indexOf("<"))) + inner2.substring(inner2.indexOf(">", inner2.indexOf("<")) + 1);
            }
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "htmlclean2_start" + r2))) + inner2 + the_line.substring(the_line.indexOf(String.valueOf(r1) + "htmlclean2_end" + r2) + (String.valueOf(r1) + "htmlclean2_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "last_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "last_end" + r2) >= 0) {
            inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "last_start" + r2) + (String.valueOf(r1) + "last_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "last_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "last_start" + r2))) + Common.last(inner2) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "last_end" + r2) + (String.valueOf(r1) + "last_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "trim_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "trim_end" + r2) >= 0) {
            inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "trim_start" + r2) + (String.valueOf(r1) + "trim_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "trim_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "trim_start" + r2))) + inner2.trim() + the_line.substring(the_line.indexOf(String.valueOf(r1) + "trim_end" + r2) + (String.valueOf(r1) + "trim_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "sql_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "sql_end" + r2) >= 0) {
            inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "sql_start" + r2) + (String.valueOf(r1) + "sql_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "sql_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "sql_start" + r2))) + inner2.replace('\'', '_').replace('\"', '_').replace('%', '_').replace(';', ' ') + the_line.substring(the_line.indexOf(String.valueOf(r1) + "sql_end" + r2) + (String.valueOf(r1) + "sql_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "indexof_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "indexof_end" + r2) >= 0) {
            params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "indexof_start") + (String.valueOf(r1) + "indexof_start").length());
            params = params.substring(0, params.indexOf("}"));
            String search = params.split(":")[1];
            search = Common.replace_str(search, "~..~", ":");
            int loc = Integer.parseInt(params.split(":")[2]);
            inner = the_line.substring(the_line.indexOf(String.valueOf(r1) + "indexof_start" + params + r2) + (String.valueOf(r1) + "indexof_start" + params + r2).length(), the_line.indexOf(String.valueOf(r1) + "indexof_end" + r2));
            the_line = loc < 0 ? String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "indexof_start" + params + r2))) + inner.lastIndexOf(search, loc * -1) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "indexof_end" + r2) + (String.valueOf(r1) + "indexof_end" + r2).length()) : String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "indexof_start" + params + r2))) + inner.indexOf(search, loc) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "indexof_end" + r2) + (String.valueOf(r1) + "indexof_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "substring_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "substring_end" + r2) >= 0) {
            params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "substring_start") + (String.valueOf(r1) + "substring_start").length());
            params = params.substring(0, params.indexOf("}"));
            int loc1 = Integer.parseInt(params.split(":")[1]);
            loc2 = Integer.parseInt(params.split(":")[2]);
            inner = the_line.substring(the_line.indexOf(String.valueOf(r1) + "substring_start" + params + r2) + (String.valueOf(r1) + "substring_start" + params + r2).length(), the_line.indexOf(String.valueOf(r1) + "substring_end" + r2));
            if (loc2 < 0) {
                loc2 = inner.length();
            }
            if (loc2 > inner.length()) {
                loc2 = inner.length();
            }
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "substring_start" + params + r2))) + inner.substring(loc1, loc2) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "substring_end" + r2) + (String.valueOf(r1) + "substring_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "split_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "split_end" + r2) >= 0) {
            params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "split_start") + (String.valueOf(r1) + "split_start").length());
            params = params.substring(0, params.indexOf("}"));
            String loc1 = params.split(":")[1];
            loc1 = Common.replace_str(loc1, "~..~", ":");
            loc2 = Integer.parseInt(params.split(":")[2]);
            inner = the_line.substring(the_line.indexOf(String.valueOf(r1) + "split_start" + params + r2) + (String.valueOf(r1) + "split_start" + params + r2).length(), the_line.indexOf(String.valueOf(r1) + "split_end" + r2));
            if (loc2 < 0) {
                loc2 = inner.split(loc1).length + loc2;
            }
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "split_start" + params + r2))) + inner.split(loc1)[loc2] + the_line.substring(the_line.indexOf(String.valueOf(r1) + "split_end" + r2) + (String.valueOf(r1) + "split_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "replace_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "replace_end" + r2) >= 0) {
            params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "replace_start") + (String.valueOf(r1) + "replace_start").length());
            params = params.substring(0, params.indexOf("}"));
            String loc1 = params.split(":")[1];
            String loc22 = "";
            if (params.split(":").length > 2) {
                loc22 = params.split(":")[2];
            }
            inner = the_line.substring(the_line.indexOf(String.valueOf(r1) + "replace_start" + params + r2) + (String.valueOf(r1) + "replace_start" + params + r2).length(), the_line.lastIndexOf(String.valueOf(r1) + "replace_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "replace_start" + params + r2))) + Common.replace_str(inner, loc1, loc22) + the_line.substring(the_line.lastIndexOf(String.valueOf(r1) + "replace_end" + r2) + (String.valueOf(r1) + "replace_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "increment_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "increment_end" + r2) >= 0) {
            inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "increment_start" + r2) + (String.valueOf(r1) + "increment_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "increment_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "increment_start" + r2))) + (Integer.parseInt(inner2.trim()) + 1) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "increment_end" + r2) + (String.valueOf(r1) + "increment_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "decrement_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "decrement_end" + r2) >= 0) {
            inner2 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "decrement_start" + r2) + (String.valueOf(r1) + "decrement_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "decrement_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "decrement_start" + r2))) + (Integer.parseInt(inner2.trim()) - 1) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "decrement_end" + r2) + (String.valueOf(r1) + "decrement_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "add_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "add_end" + r2) >= 0) {
            params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "add_start") + (String.valueOf(r1) + "add_start").length());
            params = params.substring(0, params.indexOf("}"));
            long add = Long.parseLong(params.split(":")[1]);
            inner = the_line.substring(the_line.indexOf(String.valueOf(r1) + "add_start" + params + r2) + (String.valueOf(r1) + "add_start" + params + r2).length(), the_line.indexOf(String.valueOf(r1) + "add_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "add_start" + params + r2))) + (Long.parseLong(inner.trim()) + add) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "add_end" + r2) + (String.valueOf(r1) + "add_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "parse_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "parse_end" + r2) >= 0) {
            params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "parse_start") + (String.valueOf(r1) + "parse_start").length());
            params = params.substring(0, params.indexOf("}"));
            String parse = params.substring(params.indexOf(":") + 1).trim();
            SimpleDateFormat sdf = new SimpleDateFormat(parse);
            inner = the_line.substring(the_line.indexOf(String.valueOf(r1) + "parse_start" + params + r2) + (String.valueOf(r1) + "parse_start" + params + r2).length(), the_line.indexOf(String.valueOf(r1) + "parse_end" + r2));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "parse_start" + params + r2))) + sdf.parse(inner.trim()).getTime() + the_line.substring(the_line.indexOf(String.valueOf(r1) + "parse_end" + r2) + (String.valueOf(r1) + "parse_end" + r2).length());
        }
        while (the_line.indexOf(String.valueOf(r1) + "math_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "math_end" + r2) >= 0) {
            params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "math_start") + (String.valueOf(r1) + "math_start").length());
            params = params.substring(0, params.indexOf("}"));
            String result_type = "";
            if (params.indexOf(":") >= 0) {
                result_type = params.substring(params.indexOf(":") + 1).trim();
            }
            String inner3 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "math_start" + params + r2) + (String.valueOf(r1) + "math_start" + params + r2).length(), the_line.indexOf(String.valueOf(r1) + "math_end" + r2));
            String r = "";
            r = result_type.equalsIgnoreCase("d") ? String.valueOf(Variables.eval_math(inner3.trim())) : (result_type.equalsIgnoreCase("f") ? String.valueOf((float)Variables.eval_math(inner3.trim())) : String.valueOf((int)Variables.eval_math(inner3.trim())));
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "math_start" + params + r2))) + r + the_line.substring(the_line.indexOf(String.valueOf(r1) + "math_end" + r2) + (String.valueOf(r1) + "math_end" + r2).length());
        }
        return the_line;
    }

    public static String textFunctions(String the_line, String r1, String r2) throws Exception {
        if (the_line.startsWith(String.valueOf(r1) + "ignore_functions" + r2)) {
            return the_line;
        }
        if (System.getProperty("crushftp.old.text", "false").equals("true")) {
            return Common.textFunctions_old(the_line, r1, r2);
        }
        boolean found = true;
        while (found) {
            int loc2;
            String inner;
            String params;
            String inner2;
            int end_pos;
            String end_str;
            String inner3;
            found = false;
            if (the_line.indexOf(String.valueOf(r1) + "encrypt_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "encrypt_end" + r2) >= 0) {
                inner3 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "encrypt_start" + r2) + (String.valueOf(r1) + "encrypt_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "encrypt_end" + r2));
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "encrypt_start" + r2))) + Common.encryptDecrypt(inner3, true) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "encrypt_end" + r2) + (String.valueOf(r1) + "encrypt_end" + r2).length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "decrypt_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "decrypt_end" + r2) >= 0) {
                inner3 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "decrypt_start" + r2) + (String.valueOf(r1) + "decrypt_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "decrypt_end" + r2));
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "decrypt_start" + r2))) + Common.encryptDecrypt(inner3, false) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "decrypt_end" + r2) + (String.valueOf(r1) + "decrypt_end" + r2).length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "base64_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "base64_end" + r2) >= 0) {
                inner3 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "base64_start" + r2) + (String.valueOf(r1) + "base64_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "base64_end" + r2));
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "base64_start" + r2))) + Base64.encodeBytes(inner3.getBytes("UTF8")) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "base64_end" + r2) + (String.valueOf(r1) + "base64_end" + r2).length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "encode_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "encode_end" + r2) >= 0) {
                inner3 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "encode_start" + r2) + (String.valueOf(r1) + "encode_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "encode_end" + r2));
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "encode_start" + r2))) + Common.url_encode(inner3) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "encode_end" + r2) + (String.valueOf(r1) + "encode_end" + r2).length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "decode_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "decode_end" + r2) >= 0) {
                inner3 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "decode_start" + r2) + (String.valueOf(r1) + "decode_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "decode_end" + r2));
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "decode_start" + r2))) + Common.url_decode(inner3) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "decode_end" + r2) + (String.valueOf(r1) + "decode_end" + r2).length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "upper_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "upper_end" + r2) >= 0) {
                inner3 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "upper_start" + r2) + (String.valueOf(r1) + "upper_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "upper_end" + r2));
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "upper_start" + r2))) + inner3.toUpperCase() + the_line.substring(the_line.indexOf(String.valueOf(r1) + "upper_end" + r2) + (String.valueOf(r1) + "upper_end" + r2).length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "lower_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "lower_end" + r2) >= 0) {
                inner3 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "lower_start" + r2) + (String.valueOf(r1) + "lower_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "lower_end" + r2));
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "lower_start" + r2))) + inner3.toLowerCase() + the_line.substring(the_line.indexOf(String.valueOf(r1) + "lower_end" + r2) + (String.valueOf(r1) + "lower_end" + r2).length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "md5_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "md5_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "md5_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "md5_start", end_str, the_line);
                inner2 = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "md5_start" + r2) + (String.valueOf(r1) + "md5_start" + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "md5_start" + r2))) + "MD5:" + Common.getMD5(new ByteArrayInputStream(inner2.getBytes("UTF8"))) + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "chop_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "chop_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "chop_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "chop_start", end_str, the_line);
                inner2 = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "chop_start" + r2) + (String.valueOf(r1) + "chop_start" + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "chop_start" + r2))) + Common.all_but_last(inner2) + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "1char_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "1char_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "1char_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "1char_start", end_str, the_line);
                inner2 = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "1char_start" + r2) + (String.valueOf(r1) + "1char_start" + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "1char_start" + r2))) + inner2.substring(0, inner2.length() - 1) + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "htmlclean_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "htmlclean_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "htmlclean_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "htmlclean_start", end_str, the_line);
                inner2 = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "htmlclean_start" + r2) + (String.valueOf(r1) + "htmlclean_start" + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "htmlclean_start" + r2))) + inner2.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "") + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "htmlclean2_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "htmlclean2_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "htmlclean2_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "htmlclean2_start", end_str, the_line);
                inner2 = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "htmlclean2_start" + r2) + (String.valueOf(r1) + "htmlclean2_start" + r2).length(), end_pos), r1, r2);
                while (inner2.indexOf("<") >= 0) {
                    if (inner2.indexOf("<") < 0 || inner2.indexOf(">") < 0) break;
                    inner2 = String.valueOf(inner2.substring(0, inner2.indexOf("<"))) + inner2.substring(inner2.indexOf(">", inner2.indexOf("<")) + 1);
                }
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "htmlclean2_start" + r2))) + inner2 + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "last_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "last_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "last_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "last_start", end_str, the_line);
                inner2 = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "last_start" + r2) + (String.valueOf(r1) + "last_start" + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "last_start" + r2))) + Common.last(inner2) + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "trim_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "trim_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "trim_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "trim_start", end_str, the_line);
                inner2 = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "trim_start" + r2) + (String.valueOf(r1) + "trim_start" + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "trim_start" + r2))) + inner2.trim() + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "sql_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "sql_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "sql_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "sql_start", end_str, the_line);
                inner2 = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "sql_start" + r2) + (String.valueOf(r1) + "sql_start" + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "sql_start" + r2))) + inner2.replace('\'', '_').replace('\"', '_').replace('%', '_').replace(';', ' ') + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "indexof_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "indexof_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "indexof_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "indexof_start", end_str, the_line);
                params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "indexof_start") + (String.valueOf(r1) + "indexof_start").length());
                params = params.substring(0, params.indexOf("}"));
                String search = params.split(":")[1];
                search = Common.replace_str(search, "~..~", ":");
                int loc = Integer.parseInt(params.split(":")[2]);
                inner = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "indexof_start" + params + r2) + (String.valueOf(r1) + "indexof_start" + params + r2).length(), end_pos), r1, r2);
                the_line = loc < 0 ? String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "indexof_start" + params + r2))) + inner.lastIndexOf(search, loc * -1 == 1 ? inner.length() - 1 : loc * -1) + the_line.substring(end_pos + end_str.length()) : String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "indexof_start" + params + r2))) + inner.indexOf(search, loc) + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "substring_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "substring_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "substring_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "substring_start", end_str, the_line);
                params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "substring_start") + (String.valueOf(r1) + "substring_start").length());
                params = params.substring(0, params.indexOf("}"));
                int loc1 = Integer.parseInt(params.split(":")[1]);
                loc2 = Integer.parseInt(params.split(":")[2]);
                inner = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "substring_start" + params + r2) + (String.valueOf(r1) + "substring_start" + params + r2).length(), end_pos), r1, r2);
                if (loc1 < 0) {
                    loc1 = inner.length() + loc1;
                    if (loc2 > 0) {
                        loc2 = loc1 + loc2;
                    }
                }
                if (loc2 < 0) {
                    loc2 = inner.length();
                }
                if (loc2 > inner.length()) {
                    loc2 = inner.length();
                }
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "substring_start" + params + r2))) + inner.substring(loc1, loc2) + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "split_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "split_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "split_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "split_start", end_str, the_line);
                params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "split_start") + (String.valueOf(r1) + "split_start").length());
                params = params.substring(0, params.indexOf("}"));
                String loc1 = params.split(":")[1];
                loc1 = Common.replace_str(loc1, "~..~", ":");
                loc1 = Common.replace_str(loc1, "~.|~", "{");
                if ((loc1 = Common.replace_str(loc1, "~|.~", "}")).equals(".")) {
                    loc1 = "\\.";
                }
                if (loc1.equals("|")) {
                    loc1 = "\\|";
                }
                loc2 = Integer.parseInt(params.split(":")[2]);
                inner = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "split_start" + params + r2) + (String.valueOf(r1) + "split_start" + params + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "split_start" + params + r2))) + inner.split(loc1)[loc2] + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "replace_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "replace_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "replace_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "replace_start", end_str, the_line);
                params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "replace_start") + (String.valueOf(r1) + "replace_start").length());
                params = params.substring(0, params.indexOf("}"));
                String loc1 = params.split(":")[1];
                loc1 = Common.replace_str(loc1, "~..~", ":");
                loc1 = Common.replace_str(loc1, "~.|~", "{");
                loc1 = Common.replace_str(loc1, "~|.~", "}");
                String loc22 = "";
                if (params.split(":").length > 2) {
                    loc22 = params.split(":")[2];
                }
                loc22 = Common.replace_str(loc22, "~..~", ":");
                loc22 = Common.replace_str(loc22, "~.|~", "{");
                loc22 = Common.replace_str(loc22, "~|.~", "}");
                inner = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "replace_start" + params + r2) + (String.valueOf(r1) + "replace_start" + params + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "replace_start" + params + r2))) + Common.replace_str(inner, loc1, loc22) + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "increment_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "increment_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "increment_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "increment_start", end_str, the_line);
                inner2 = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "increment_start" + r2) + (String.valueOf(r1) + "increment_start" + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "increment_start" + r2))) + (Integer.parseInt(inner2.trim()) + 1) + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "decrement_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "decrement_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "decrement_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "decrement_start", end_str, the_line);
                inner2 = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "decrement_start" + r2) + (String.valueOf(r1) + "decrement_start" + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "decrement_start" + r2))) + (Integer.parseInt(inner2.trim()) - 1) + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "add_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "add_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "add_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "add_start", end_str, the_line);
                params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "add_start") + (String.valueOf(r1) + "add_start").length());
                params = params.substring(0, params.indexOf("}"));
                long add = Long.parseLong(params.split(":")[1]);
                inner = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "add_start" + params + r2) + (String.valueOf(r1) + "add_start" + params + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "add_start" + params + r2))) + (Long.parseLong(inner.trim()) + add) + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "parse_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "parse_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "parse_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "parse_start", end_str, the_line);
                params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "parse_start") + (String.valueOf(r1) + "parse_start").length());
                params = params.substring(0, params.indexOf("}"));
                String parse = params.substring(params.indexOf(":") + 1).trim();
                SimpleDateFormat sdf = new SimpleDateFormat((parse = Common.replace_str(parse, "~..~", ":")).charAt(0) == '=' ? parse.substring(1) : parse);
                sdf.setLenient(parse.charAt(0) != '=');
                inner = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "parse_start" + params + r2) + (String.valueOf(r1) + "parse_start" + params + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "parse_start" + params + r2))) + sdf.parse(inner.trim()).getTime() + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "rparse_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "rparse_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "rparse_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "rparse_start", end_str, the_line);
                params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "rparse_start") + (String.valueOf(r1) + "rparse_start").length());
                params = params.substring(0, params.indexOf("}"));
                String parse = params.substring(params.indexOf(":") + 1).trim();
                parse = Common.replace_str(parse, "~..~", ":");
                SimpleDateFormat sdf = new SimpleDateFormat(parse);
                inner = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "rparse_start" + params + r2) + (String.valueOf(r1) + "rparse_start" + params + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "rparse_start" + params + r2))) + sdf.format(new Date(Long.parseLong(inner.trim()))) + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "url_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "url_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "url_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "url_start", end_str, the_line);
                params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "url_start") + (String.valueOf(r1) + "url_start").length());
                params = params.substring(0, params.indexOf("}"));
                String part = params.substring(params.indexOf(":") + 1).trim();
                String inner4 = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "url_start" + params + r2) + (String.valueOf(r1) + "url_start" + params + r2).length(), end_pos), r1, r2);
                String part_item = "";
                if (part.equalsIgnoreCase("user")) {
                    part_item = new VRL(inner4).getUsername();
                } else if (part.equalsIgnoreCase("pass")) {
                    part_item = new VRL(inner4).getPassword();
                } else if (part.equalsIgnoreCase("path")) {
                    part_item = new VRL(inner4).getPath();
                } else if (part.equalsIgnoreCase("port")) {
                    part_item = String.valueOf(new VRL(inner4).getPort());
                } else if (part.equalsIgnoreCase("host")) {
                    part_item = new VRL(inner4).getHost();
                } else if (part.equalsIgnoreCase("protocol")) {
                    part_item = new VRL(inner4).getProtocol();
                } else if (part.equalsIgnoreCase("file")) {
                    part_item = new VRL(inner4).getFile();
                } else if (part.equalsIgnoreCase("query")) {
                    part_item = new VRL(inner4).getQuery();
                }
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "url_start" + params + r2))) + part_item + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "length_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "length_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "length_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "length_start", end_str, the_line);
                inner2 = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "length_start" + r2) + (String.valueOf(r1) + "length_start" + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "length_start" + r2))) + inner2.length() + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "geoip_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "geoip_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "geoip_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "geoip_start", end_str, the_line);
                inner2 = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "geoip_start" + r2) + (String.valueOf(r1) + "geoip_start" + r2).length(), end_pos), r1, r2);
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "geoip_start" + r2))) + Common.geo_ip_lookup(inner2, 0) + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "math_start") >= 0 && the_line.indexOf(String.valueOf(r1) + "math_end" + r2) >= 0) {
                end_str = String.valueOf(r1) + "math_end" + r2;
                end_pos = Common.findEnd(String.valueOf(r1) + "math_start", end_str, the_line);
                params = the_line.substring(the_line.indexOf(String.valueOf(r1) + "math_start") + (String.valueOf(r1) + "math_start").length());
                params = params.substring(0, params.indexOf("}"));
                String result_type = "";
                if (params.indexOf(":") >= 0) {
                    result_type = params.substring(params.indexOf(":") + 1).trim();
                }
                String inner5 = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "math_start" + params + r2) + (String.valueOf(r1) + "math_start" + params + r2).length(), end_pos), r1, r2);
                String r = "";
                DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                df.setMaximumFractionDigits(340);
                r = result_type.equalsIgnoreCase("d") ? String.valueOf(df.format(Variables.eval_math(inner5.trim()))) : (result_type.equalsIgnoreCase("i") ? String.valueOf((int)Variables.eval_math(inner5.trim())) : (result_type.equalsIgnoreCase("f") ? String.valueOf((float)Variables.eval_math(inner5.trim())) : String.valueOf(df.format(Variables.eval_math(inner5.trim())))));
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "math_start" + params + r2))) + r + the_line.substring(end_pos + end_str.length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "base64url_start" + r2) >= 0 && the_line.indexOf(String.valueOf(r1) + "base64url_end" + r2) >= 0) {
                inner3 = the_line.substring(the_line.indexOf(String.valueOf(r1) + "base64url_start" + r2) + (String.valueOf(r1) + "base64url_start" + r2).length(), the_line.indexOf(String.valueOf(r1) + "base64url_end" + r2));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                VRL vrl = new VRL(inner3);
                GenericClient c = Common.getClient(vrl.toString(), "", new Vector());
                c.login(vrl.getUsername(), vrl.getPassword(), "");
                Common.streamCopier(c.download(vrl.getPath(), 0L, -1L, true), baos, false, true, true);
                c.logout();
                the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "base64url_start" + r2))) + Base64.encodeBytes(baos.toByteArray()) + the_line.substring(the_line.indexOf(String.valueOf(r1) + "base64url_end" + r2) + (String.valueOf(r1) + "base64url_end" + r2).length());
                found = true;
            }
            if (the_line.indexOf(String.valueOf(r1) + "log_start" + r2) < 0 || the_line.indexOf(String.valueOf(r1) + "log_end" + r2) < 0) continue;
            end_str = String.valueOf(r1) + "log_end" + r2;
            int end_pos2 = Common.findEnd(String.valueOf(r1) + "log_start", end_str, the_line);
            inner2 = Common.textFunctions(the_line.substring(the_line.indexOf(String.valueOf(r1) + "log_start" + r2) + (String.valueOf(r1) + "log_start" + r2).length(), end_pos2), r1, r2);
            the_line = String.valueOf(the_line.substring(0, the_line.indexOf(String.valueOf(r1) + "log_start" + r2))) + inner2 + the_line.substring(end_pos2 + end_str.length());
            Common.log("SERVER", 0, inner2);
            found = true;
        }
        return the_line;
    }

    public static String geo_ip_lookup(String ip, int max) throws Exception {
        if (!System.getProperty("crushftp.v10_beta", "false").equals("true") || System.getProperty("crushftp.geoip_access_key", "").equals("")) {
            return "";
        }
        String result_tmp = ip_lookup_cache.getProperty(ip);
        if (result_tmp != null) {
            return result_tmp;
        }
        final StringBuffer geo_ip_info = new StringBuffer();
        final String ip_f = ip;
        final StringBuffer status = new StringBuffer();
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    String access_key = Common.encryptDecrypt(System.getProperty("crushftp.geoip_access_key", ""), false);
                    URLConnection urlc = URLConnection.openConnection(new VRL("http://api.ipstack.com/" + ip_f + "?access_key=" + access_key), new Properties());
                    urlc.setDoOutput(true);
                    urlc.setRequestMethod("GET");
                    urlc.setRequestProperty("Content-Type", "application/json");
                    String result = Common.consumeResponse(urlc.getInputStream());
                    JSONObject obj = (JSONObject)JSONValue.parse(result);
                    geo_ip_info.append(obj.get("country_name")).append(",");
                    geo_ip_info.append(obj.get("region_name")).append(",");
                    geo_ip_info.append(obj.get("city")).append(",");
                    geo_ip_info.append(obj.get("latitude")).append(",");
                    geo_ip_info.append(obj.get("longitude"));
                }
                catch (Exception e) {
                    Common.log("SERVER", 1, e);
                }
                status.append("done");
            }
        }, "GEO_IP_LOOKUP:" + ip);
        int x = 0;
        while (x < 100 && status.length() == 0) {
            Thread.sleep(100L);
            ++x;
        }
        result_tmp = geo_ip_info.toString();
        if (max > 0 && ip_lookup_cache.size() > max) {
            ip_lookup_cache.clear();
        }
        ip_lookup_cache.put(ip, result_tmp);
        return result_tmp;
    }

    public static int findEnd(String start, String end, String the_line) {
        int depth = 0;
        int x = 0;
        while (x < the_line.length()) {
            if (the_line.startsWith(start, x)) {
                ++depth;
            } else {
                if (the_line.startsWith(end, x) && depth == 1) {
                    return x;
                }
                if (the_line.startsWith(end, x)) {
                    --depth;
                }
            }
            ++x;
        }
        return -1;
    }

    public static String getLocalIP() {
        if (System.currentTimeMillis() - lastIpLookup < 60000L) {
            return lastLocalIP;
        }
        lastIpLookup = System.currentTimeMillis();
        String ip = "127.0.0.1";
        try {
            try {
                lastLocalIP = ip = InetAddress.getLocalHost().getHostAddress();
            }
            catch (Exception e) {
                Tunnel2.msg(e);
            }
            if (!ip.startsWith("127.0")) {
                return ip;
            }
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface i = en.nextElement();
                Enumeration<InetAddress> en2 = i.getInetAddresses();
                while (en2.hasMoreElements()) {
                    InetAddress addr = en2.nextElement();
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address && !addr.getHostAddress().startsWith("127.0")) {
                        lastLocalIP = ip = addr.getHostAddress();
                        return ip;
                    }
                    if (addr.isLoopbackAddress() || !(addr instanceof Inet4Address)) continue;
                    ip = addr.getHostAddress();
                }
            }
        }
        catch (Exception e) {
            Tunnel2.msg(e);
        }
        lastLocalIP = ip;
        return ip;
    }

    public static Object CLONE(Object o) {
        Object o2 = null;
        try {
            byte[] b = Common.CLONE1(o);
            o2 = Common.CLONE2(b);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return o2;
    }

    public static String getContentType(String s) {
        if ((s = s.toUpperCase()).endsWith(".ZIP")) {
            return "application/zip";
        }
        if (s.endsWith(".JPG")) {
            return "image/jpeg";
        }
        if (s.endsWith(".ICO")) {
            return "image/x-icon";
        }
        if (s.endsWith(".JPEG")) {
            return "image/jpeg";
        }
        if (s.endsWith(".GIF")) {
            return "image/gif";
        }
        if (s.endsWith(".PNG")) {
            return "image/png";
        }
        if (s.endsWith(".BMP")) {
            return "image/bmp";
        }
        if (s.endsWith(".HTML")) {
            return "text/html";
        }
        if (s.endsWith(".HTML")) {
            return "text/html";
        }
        if (s.endsWith(".CSS")) {
            return "text/css";
        }
        if (s.endsWith(".JS")) {
            return "text/javascript";
        }
        if (s.endsWith(".XML")) {
            return "text/xml";
        }
        if (s.endsWith(".TXT")) {
            return "text/plain";
        }
        if (s.endsWith(".PDF")) {
            return "applciation/pdf";
        }
        if (s.endsWith(".SWF")) {
            return "applciation/x-shockwave-flash";
        }
        if (s.endsWith(".WOFF")) {
            return "application/font-woff";
        }
        if (s.endsWith(".WOFF2")) {
            return "application/font-woff";
        }
        return "applciation/binary";
    }

    public static byte[] CLONE1(Object o) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream tempOut = new ObjectOutputStream(baos);
            tempOut.reset();
            tempOut.writeObject(o);
            tempOut.flush();
            tempOut.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return baos.toByteArray();
    }

    public static Object CLONE2(byte[] b) {
        Object o = null;
        try {
            ObjectInputStream tempIn = new ObjectInputStream(new ByteArrayInputStream(b));
            o = tempIn.readObject();
            tempIn.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return o;
    }

    public static void activateFront() {
        if (Common.machine_is_x()) {
            try {
                new ScriptEngineManager().getEngineByName("AppleScript").eval("tell me to activate");
            }
            catch (Exception e) {
                System.out.println(new Date());
                e.printStackTrace();
            }
        }
    }

    public static String format_time(long seconds) {
        String minsStr;
        String hoursStr;
        int secs = (int)seconds;
        int hours = 0;
        int mins = 0;
        if (secs > 60) {
            hoursStr = String.valueOf((double)secs / 60.0 / 60.0);
            hours = Integer.parseInt(hoursStr.substring(0, hoursStr.indexOf(".")));
            minsStr = String.valueOf((double)(secs -= hours * 60 * 60) / 60.0);
            mins = Integer.parseInt(minsStr.substring(0, minsStr.indexOf(".")));
            secs -= mins * 60;
        }
        if (hours < 0) {
            hours = 0;
        }
        if (mins < 0) {
            mins = 0;
        }
        if (secs < 0) {
            secs = 0;
        }
        hoursStr = String.valueOf(hours);
        minsStr = String.valueOf(mins);
        String secsStr = String.valueOf(secs);
        if (hours < 10) {
            hoursStr = "0" + hours;
        }
        if (mins < 10) {
            minsStr = "0" + mins;
        }
        if (secs < 10) {
            secsStr = "0" + secs;
        }
        return String.valueOf(hoursStr) + ":" + minsStr + ":" + secsStr;
    }

    public static String format_time_pretty(long seconds) {
        String minsStr;
        String hoursStr;
        int secs = (int)seconds;
        int hours = 0;
        int mins = 0;
        if (secs > 60) {
            hoursStr = String.valueOf((double)secs / 60.0 / 60.0);
            hours = Integer.parseInt(hoursStr.substring(0, hoursStr.indexOf(".")));
            minsStr = String.valueOf((double)(secs -= hours * 60 * 60) / 60.0);
            mins = Integer.parseInt(minsStr.substring(0, minsStr.indexOf(".")));
            secs -= mins * 60;
        }
        if (hours < 0) {
            hours = 0;
        }
        if (mins < 0) {
            mins = 0;
        }
        if (secs < 0) {
            secs = 0;
        }
        hoursStr = String.valueOf(hours);
        minsStr = String.valueOf(mins);
        String secsStr = String.valueOf(secs);
        if (hours < 10) {
            hoursStr = "0" + hours;
        }
        if (mins < 10) {
            minsStr = "0" + mins;
        }
        if (secs < 10) {
            secsStr = "0" + secs;
        }
        String s = "";
        if (!hoursStr.equals("00")) {
            s = String.valueOf(s) + hoursStr + " hr, ";
        }
        if (!minsStr.equals("00")) {
            s = String.valueOf(s) + minsStr + " min, ";
        }
        s = String.valueOf(s) + secsStr + " sec.";
        return s;
    }

    public static String format_bytes_short2(String bytes) {
        try {
            return Common.format_bytes_short2(Long.parseLong(bytes));
        }
        catch (Exception exception) {
            return bytes;
        }
    }

    public static String format_bytes_short2(long bytes) {
        boolean neg;
        boolean bl = neg = bytes < 0L;
        if (bytes < 0L) {
            bytes = Math.abs(bytes);
        }
        String return_str = "";
        try {
            long tb = 0x10000000000L;
            return_str = bytes > tb ? String.valueOf((float)((int)((float)bytes / (float)tb * 100.0f)) / 100.0f) + " " + System.getProperty("crushftp.terrabytes_label_short", "T") + System.getProperty("bytes_label_short", "B") : (bytes > 0x40000000L ? String.valueOf((float)((int)((float)bytes / 1.07374182E9f * 100.0f)) / 100.0f) + " " + System.getProperty("crushftp.gigabytes_label_short", "G") + System.getProperty("bytes_label_short", "B") : (bytes > 0x100000L ? String.valueOf((float)((int)((float)bytes / 1048576.0f * 100.0f)) / 100.0f) + " " + System.getProperty("crushftp.megabytes_label_short", "M") + System.getProperty("bytes_label_short", "B") : (bytes > 1024L ? String.valueOf((float)((int)((float)bytes / 1024.0f * 100.0f)) / 100.0f) + " " + System.getProperty("crushftp.kilobytes_label_short", "K") + System.getProperty("bytes_label_short", "B") : String.valueOf(bytes) + " " + System.getProperty("bytes_label_short", "B"))));
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (neg) {
            return_str = "-" + return_str;
        }
        return return_str;
    }

    public static String format_bytes2(String byte_amount) {
        String return_str = "";
        try {
            long bytes = Long.parseLong(byte_amount);
            long tb = 0x10000000000L;
            return_str = bytes > tb ? String.valueOf((float)((int)((float)bytes / (float)tb * 100.0f)) / 100.0f) + " " + System.getProperty("crushftp.terrabytes_label", "Terra") + System.getProperty("bytes_label", "Bytes") : (bytes > 0x40000000L ? String.valueOf((float)((int)((float)bytes / 1.07374182E9f * 100.0f)) / 100.0f) + " " + System.getProperty("crushftp.gigabytes_label", "Giga") + System.getProperty("bytes_label", "Bytes") : (bytes > 0x100000L ? String.valueOf((float)((int)((float)bytes / 1048576.0f * 100.0f)) / 100.0f) + " " + System.getProperty("crushftp.megabytes_label", "Mega") + System.getProperty("bytes_label", "Bytes") : (bytes > 1024L ? String.valueOf((float)((int)((float)bytes / 1024.0f * 100.0f)) / 100.0f) + " " + System.getProperty("crushftp.kilobytes_label", "Kilo") + System.getProperty("bytes_label", "Bytes") : String.valueOf(bytes) + " " + System.getProperty("bytes_label", "Bytes").toLowerCase())));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return return_str;
    }

    public static void updateMimes() throws Exception {
        long mimesModifiedNew = new File_S(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/mime_types.txt").lastModified();
        if (mimesModified != mimesModifiedNew) {
            mimesModified = mimesModifiedNew;
            mimes = new Properties();
            try {
                BufferedReader mimeIn = new BufferedReader(new FileReader(new File_S(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/mime_types.txt")));
                String s = mimeIn.readLine();
                while (s != null) {
                    if (!s.startsWith("#")) {
                        try {
                            mimes.put(s.substring(0, s.indexOf(" ")).trim().toUpperCase(), s.substring(s.indexOf(" ") + 1).trim());
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    s = mimeIn.readLine();
                }
                mimeIn.close();
            }
            catch (Exception e) {
                System.out.println(new Date());
                e.printStackTrace();
            }
        }
    }

    public static void check_exec() {
        if (!System.getProperty("crushftp.security.exec", "true").equals("true")) {
            throw new RuntimeException("Executing external processes not allowed.");
        }
    }

    public static String dumpStack(String info) {
        String cpu_usage;
        String extra_info = cpu_usage = Common.getCpuUsage();
        try {
            extra_info = "Server_CPU:" + cpu_usage.split(":")[0] + ",OS_CPU:" + cpu_usage.split(":")[1] + ",Open_Files:" + cpu_usage.split(":")[2] + ",MAX_Files:" + cpu_usage.split(":")[3];
        }
        catch (Exception exception) {
            // empty catch block
        }
        String result = new Date() + "\r\nJava:" + System.getProperty("java.version") + " from:" + System.getProperty("java.home") + " " + System.getProperty("sun.arch.data.model") + " bit  OS:" + System.getProperties().getProperty("os.name") + "\r\n";
        result = String.valueOf(result) + "Server Memory Stats: Max=" + Common.format_bytes_short(Runtime.getRuntime().maxMemory()) + ", Free=" + Common.format_bytes_short(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) + "\r\n" + extra_info + "\r\n";
        result = String.valueOf(result) + info + "\r\n";
        try {
            result = String.valueOf(result) + "Working dir:" + new File("./").getCanonicalPath() + "\r\n";
        }
        catch (Exception e) {
            result = String.valueOf(result) + "Working dir:" + e + "\r\n";
        }
        try {
            result = String.valueOf(result) + "statsDB folder size:" + Common.format_bytes_short(Common.recurseSize("./statsDB", 0L)) + "\r\n";
        }
        catch (Exception e) {
            result = String.valueOf(result) + "statsDB folder size:" + e + "\r\n";
        }
        try {
            result = String.valueOf(result) + "sessions.obj size:" + Common.format_bytes_short(Common.recurseSize("./sessions.obj", 0L)) + "\r\n";
        }
        catch (Exception e) {
            result = String.valueOf(result) + "sessions.obj size:" + e + "\r\n";
        }
        result = String.valueOf(result) + "System variables:";
        Enumeration<Object> keys = System.getProperties().keys();
        while (keys.hasMoreElements()) {
            String key = "" + keys.nextElement();
            String val = String.valueOf(System.getProperty(key));
            if (!key.startsWith("crushftp.")) continue;
            result = String.valueOf(result) + " " + key + "=" + val;
        }
        result = String.valueOf(result) + "\r\n";
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threads1 = threadSet.toArray(new Thread[threadSet.size()]);
        Vector<Thread> threads_output = new Vector<Thread>();
        Object[] tmp = new String[threads1.length];
        Properties cpu_times = new Properties();
        ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
        Properties highest_usage = new Properties();
        int x22 = 0;
        while (x22 < threads1.length) {
            cpu_times.put(String.valueOf(threads1[x22].getId()), String.valueOf(tmxb.getThreadCpuTime(threads1[x22].getId())));
            ++x22;
        }
        try {
            Thread.sleep(1000L);
        }
        catch (InterruptedException x22) {
            // empty catch block
        }
        int x = 0;
        while (x < threads1.length) {
            long current = Long.parseLong(cpu_times.getProperty(String.valueOf(threads1[x].getId()), "0"));
            long usage = (tmxb.getThreadCpuTime(threads1[x].getId()) - current) / 1000L;
            cpu_times.put(String.valueOf(threads1[x].getId()), String.valueOf(usage));
            tmp[x] = String.valueOf(threads1[x].getName()) + ":" + usage + "!~!" + x;
            if (highest_usage.size() < 5) {
                highest_usage.put(String.valueOf(usage), threads1[x]);
            } else {
                keys = highest_usage.keys();
                while (keys.hasMoreElements()) {
                    String key = "" + keys.nextElement();
                    long other_usage = Long.parseLong(key);
                    if (usage <= other_usage) continue;
                    highest_usage.remove(key);
                    highest_usage.put(String.valueOf(usage), threads1[x]);
                }
            }
            ++x;
        }
        Arrays.sort(tmp);
        x = 0;
        while (x < tmp.length) {
            threads_output.addElement(threads1[Integer.parseInt(((String)tmp[x]).split("!~!")[1])]);
            ++x;
        }
        result = String.valueOf(result) + "####################### TOP 5 HIGHEST CPU USAGE THREADS(" + threads_output.size() + ") START #######################\r\n";
        long[] tmp_usage = new long[highest_usage.size()];
        int pos = 0;
        keys = highest_usage.keys();
        while (keys.hasMoreElements()) {
            String key = "" + keys.nextElement();
            tmp_usage[pos++] = Long.parseLong(key);
        }
        Arrays.sort(tmp_usage);
        int x3 = tmp_usage.length - 1;
        while (x3 >= 0) {
            result = String.valueOf(result) + Common.build_thread_info((Thread)highest_usage.get(String.valueOf(tmp_usage[x3])), cpu_times);
            --x3;
        }
        result = String.valueOf(result) + "####################### TOP 5 HIGHEST CPU USAGE THREADS(" + threads_output.size() + ") END #######################\r\n";
        result = String.valueOf(result) + "\r\n#######################Start Thread Dump (" + threads_output.size() + ")#######################\r\n";
        x3 = 0;
        while (x3 < threads_output.size()) {
            result = String.valueOf(result) + Common.build_thread_info((Thread)threads_output.elementAt(x3), cpu_times);
            ++x3;
        }
        result = String.valueOf(result) + "#######################End Thread Dump (" + threads_output.size() + ")#######################\r\n";
        System.out.println(result);
        return result;
    }

    static String build_thread_info(Thread t, Properties cpu_times) {
        String result = "";
        String data = "---------------------------------------------------------------------------------------------------";
        result = String.valueOf(result) + data + "\r\n";
        data = String.valueOf(cpu_times.getProperty(String.valueOf(t.getId()), "0")) + "ms:" + t.getName();
        result = String.valueOf(result) + data + "\r\n";
        StackTraceElement[] ste = t.getStackTrace();
        int xx = 0;
        while (xx < ste.length) {
            data = "\t\t" + ste[xx].getClassName() + "." + ste[xx].getMethodName() + "(" + ste[xx].getFileName() + ":" + ste[xx].getLineNumber() + ")";
            result = String.valueOf(result) + data + "\r\n";
            ++xx;
        }
        return result;
    }

    public static void sockLog(Socket sock, String msg) {
        if (!System.getProperty("crushftp.debug_socks_log", "false").equals("true")) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss.SSS");
        try {
            Common.log("DMZ", 0, String.valueOf(sdf.format(new Date())) + "|DEBUG_SOCKETS|" + sock + "|" + msg);
        }
        catch (Exception e) {
            Common.log("SERVER", 1, e);
        }
    }

    public static String send_mail(String server_ip, String to_user, String cc_user, String bcc_user, String from_user, String subject, String body, String smtp_server, String smtp_user, String smtp_pass, boolean smtp_ssl, boolean html, File_B[] attachments) {
        return Common.send_mail(server_ip, to_user, cc_user, bcc_user, from_user, null, subject, body, smtp_server, smtp_user, smtp_pass, smtp_ssl, html, attachments, new Vector());
    }

    public static String send_mail(String server_ip, String to_user, String cc_user, String bcc_user, String from_user, String reply_to_user, String subject, String body, String smtp_server, String smtp_user, String smtp_pass, boolean smtp_ssl, boolean html, File_B[] attachments) {
        return Common.send_mail(server_ip, to_user, cc_user, bcc_user, from_user, reply_to_user, subject, body, smtp_server, smtp_user, smtp_pass, smtp_ssl, html, attachments, new Vector());
    }

    public static String send_mail(String server_ip, String to_user, String cc_user, String bcc_user, String from_user, String reply_to_user, String subject, String body, String smtp_server, String smtp_user, String smtp_pass, boolean smtp_ssl, boolean html, File_B[] attachments, Vector fileMimeTypes) {
        return Common.send_mail(server_ip, to_user, cc_user, bcc_user, from_user, reply_to_user, subject, body, smtp_server, smtp_user, smtp_pass, smtp_ssl, html, attachments, fileMimeTypes, new Vector());
    }

    public static String send_mail(String server_ip, String to_user, String cc_user, String bcc_user, String from_user, String reply_to_user, String subject, String body, String smtp_server, String smtp_user, String smtp_pass, boolean smtp_ssl, boolean html, File_B[] attachments, Vector fileMimeTypes, Vector remoteFiles) {
        try {
            return Mailer.send_mail(server_ip, to_user, cc_user, bcc_user, from_user, reply_to_user, subject, body, smtp_server, smtp_user, smtp_pass, smtp_ssl, html, attachments, fileMimeTypes, remoteFiles);
        }
        catch (Throwable e) {
            Common.log("SMTP", 1, e);
            return "ERROR:" + e.toString();
        }
    }

    public static String send_mail(String server_ip, String to_user, String cc_user, String bcc_user, String from_user, String subject, String body, String smtp_server, String smtp_user, String smtp_pass, boolean smtp_ssl, boolean html) {
        return Common.send_mail(server_ip, to_user, cc_user, bcc_user, from_user, null, subject, body, smtp_server, smtp_user, smtp_pass, smtp_ssl, html, null, new Vector());
    }

    public static String send_mail(String server_ip, String to_user, String cc_user, String bcc_user, String from_user, String reply_to_user, String subject, String body, String smtp_server, String smtp_user, String smtp_pass, boolean smtp_ssl, boolean html) {
        return Common.send_mail(server_ip, to_user, cc_user, bcc_user, from_user, reply_to_user, subject, body, smtp_server, smtp_user, smtp_pass, smtp_ssl, html, null, new Vector());
    }

    public static SAXBuilder getSaxBuilder() {
        SAXBuilder sb = new SAXBuilder();
        sb.setExpandEntities(false);
        sb.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return sb;
    }

    public static String bytesToHex(byte[] b) {
        char[] hex = "0123456789ABCDEF".toCharArray();
        String s = "";
        int x = 0;
        while (x < b.length) {
            s = String.valueOf(s) + hex[(b[x] & 0xFF) >>> 4] + hex[b[x] & 0xFF & 0xF];
            ++x;
        }
        return s;
    }

    public static byte[] hexToBytes(String s) {
        byte[] b = new byte[s.length() / 2];
        int x = 0;
        while (x < s.length()) {
            b[x / 2] = (byte)((Character.digit(s.charAt(x), 16) << 4) + Character.digit(s.charAt(x + 1), 16));
            x += 2;
        }
        return b;
    }

    public static String getCpuUsage() {
        try {
            OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
            Method getProcessCpuLoad = bean.getClass().getMethod("getProcessCpuLoad", new Class[0]);
            getProcessCpuLoad.setAccessible(true);
            String s = String.valueOf((int)((double)Float.parseFloat(getProcessCpuLoad.invoke(bean, new Class[0]).toString()) * 100.0));
            Method getSystemCpuLoad = bean.getClass().getMethod("getSystemCpuLoad", new Class[0]);
            getSystemCpuLoad.setAccessible(true);
            s = String.valueOf(s) + ":" + (int)((double)Float.parseFloat(getSystemCpuLoad.invoke(bean, new Class[0]).toString()) * 100.0);
            try {
                Method getOpenFileDescriptorCount = bean.getClass().getMethod("getOpenFileDescriptorCount", new Class[0]);
                getOpenFileDescriptorCount.setAccessible(true);
                s = String.valueOf(s) + ":" + (int)Float.parseFloat(getOpenFileDescriptorCount.invoke(bean, new Class[0]).toString());
                Method getMaxFileDescriptorCount = bean.getClass().getMethod("getMaxFileDescriptorCount", new Class[0]);
                getMaxFileDescriptorCount.setAccessible(true);
                s = String.valueOf(s) + ":" + (int)Float.parseFloat(getMaxFileDescriptorCount.invoke(bean, new Class[0]).toString());
            }
            catch (Exception exception) {
                // empty catch block
            }
            return s;
        }
        catch (Throwable throwable) {
            return "";
        }
    }

    public static void writeFileFromJar_plain(String src, String dst, boolean preservePath) {
        block25: {
            InputStream in = null;
            RandomAccessFile out = null;
            try {
                try {
                    if (!new File_S(src).exists()) {
                        in = new Common().getClass().getResourceAsStream(src);
                        if (preservePath) {
                            new File_S(Common.all_but_last(String.valueOf(dst) + src)).mkdirs();
                        } else if (src.indexOf("/") >= 0) {
                            src = Common.last(src);
                        }
                        out = new RandomAccessFile(new File_S(String.valueOf(dst) + src), "rw");
                        int bytes = 0;
                        byte[] b = new byte[32768];
                        while (bytes >= 0) {
                            bytes = in.read(b);
                            if (bytes <= 0) continue;
                            out.write(b, 0, bytes);
                        }
                    }
                }
                catch (Exception e) {
                    Common.log("", 1, e);
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (Exception e2) {
                            Common.log("", 1, e2);
                        }
                    }
                    if (out == null) break block25;
                    try {
                        out.close();
                    }
                    catch (Exception e3) {
                        Common.log("", 1, e3);
                    }
                }
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (Exception e) {
                        Common.log("", 1, e);
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (Exception e) {
                        Common.log("", 1, e);
                    }
                }
            }
        }
    }

    public static void writeFileFromJar_assets(String src, String dst, boolean preservePath) {
        try {
            if (!new File_S(src).exists()) {
                InputStream in = new Common().getClass().getResourceAsStream("/assets/crushftp/" + src);
                if (preservePath) {
                    new File_S(Common.all_but_last(String.valueOf(dst) + src)).mkdirs();
                } else if (src.indexOf("/") >= 0) {
                    src = Common.last(src);
                }
                RandomAccessFile out = new RandomAccessFile(new File_S(String.valueOf(dst) + src), "rw");
                int bytes = 0;
                byte[] b = new byte[32768];
                while (bytes >= 0) {
                    bytes = in.read(b);
                    if (bytes <= 0) continue;
                    out.write(b, 0, bytes);
                }
                in.close();
                out.close();
            }
        }
        catch (Exception e) {
            System.out.println(new Date());
            e.printStackTrace();
        }
    }

    public static void write_service_conf(String filename, int ram_megabytes, String main_class, String service_name, String main_jar) {
        if (main_class.equals("CrushClientWinService")) {
            main_class = "com.crushftp.client.Client";
        }
        if (main_class.equals("CrushTunnelWinService")) {
            main_class = "com.crushftp.client.Client";
        }
        try {
            new File_S("./service").mkdir();
            String service_ini = "service.class=" + main_class + "\r\n";
            service_ini = String.valueOf(service_ini) + "service.id=" + service_name + "\r\n";
            service_ini = String.valueOf(service_ini) + "service.name=" + service_name + "\r\n";
            service_ini = String.valueOf(service_ini) + "service.description=" + service_name + "\r\n";
            service_ini = service_name.toUpperCase().indexOf("RESTART") >= 0 ? String.valueOf(service_ini) + "service.startup=demand\r\n" : String.valueOf(service_ini) + "service.startup=auto\r\n";
            service_ini = String.valueOf(service_ini) + "classpath.1=" + main_jar + "\r\n";
            service_ini = String.valueOf(service_ini) + "working.directory=../\r\n";
            service_ini = String.valueOf(service_ini) + "log=wrapper.log\r\n";
            service_ini = String.valueOf(service_ini) + "log.roll.size=10\r\n";
            service_ini = String.valueOf(service_ini) + "vm.heapsize.preferred=" + ram_megabytes + "\r\n";
            service_ini = String.valueOf(service_ini) + "vm.sysfirst=false\r\n";
            service_ini = String.valueOf(service_ini) + "vm.location=.\\java\\bin\\server\\jvm.dll|%JAVA_HOME%\\bin\\server\\jvm.dll|%JAVA_PATH%\\bin\\server\\jvm.dll\r\n";
            if (!(System.getProperty("java.version").startsWith("1.4") || System.getProperty("java.version").startsWith("1.5") || System.getProperty("java.version").startsWith("1.6") || System.getProperty("java.version").startsWith("1.7") || System.getProperty("java.version").startsWith("1.8"))) {
                service_ini = String.valueOf(service_ini) + "vmarg.1=--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED\r\n";
            }
            service_ini = String.valueOf(service_ini) + "arg.1=-d\r\n";
            RandomAccessFile wrapper = new RandomAccessFile(new File_S(filename), "rw");
            wrapper.setLength(0L);
            wrapper.seek(0L);
            wrapper.write(service_ini.getBytes("UTF8"));
            wrapper.close();
        }
        catch (Exception e) {
            Common.log("SERVER", 0, e);
        }
    }

    public static void update_service_memory(int MB, String service_name) {
        if (Common.machine_is_windows()) {
            try {
                String filename = "./service/" + service_name + "Service.ini";
                RandomAccessFile wrapper = new RandomAccessFile(new File_S(filename), "rw");
                byte[] b = new byte[(int)wrapper.length()];
                wrapper.readFully(b);
                String wrapperStr = new String(b, "UTF8");
                int pos = wrapperStr.indexOf("vm.heapsize.preferred=");
                wrapperStr = String.valueOf(wrapperStr.substring(0, pos + "vm.heapsize.preferred=".length())) + MB + wrapperStr.substring(wrapperStr.indexOf("\r\n", pos));
                wrapper.setLength(0L);
                wrapper.seek(0L);
                wrapper.write(wrapperStr.getBytes("UTF8"));
                wrapper.close();
            }
            catch (Exception e) {
                Common.log("SERVER", 0, e);
            }
        } else if (Common.machine_is_linux() || Common.machine_is_x()) {
            try {
                String filename = "./crushftp_init.sh";
                if (Common.machine_is_x()) {
                    filename = "./CrushFTP.command";
                }
                RandomAccessFile wrapper = new RandomAccessFile(new File_S(filename), "rw");
                byte[] b = new byte[(int)wrapper.length()];
                wrapper.readFully(b);
                String wrapperStr = new String(b, "UTF8");
                int pos = wrapperStr.indexOf("-Xmx");
                while (pos > 0) {
                    wrapperStr = String.valueOf(wrapperStr.substring(0, pos + "-Xmx".length())) + MB + "m" + wrapperStr.substring(wrapperStr.indexOf(" ", pos));
                    pos = wrapperStr.indexOf("-Xmx", pos + 1);
                }
                wrapper.setLength(0L);
                wrapper.seek(0L);
                wrapper.write(wrapperStr.getBytes("UTF8"));
                wrapper.close();
            }
            catch (Exception e) {
                Common.log("SERVER", 0, e);
            }
        }
    }

    public static void set_encryption_password(String pass) {
        encryption_password = pass.toCharArray();
    }

    public static boolean test_elevate() throws Exception {
        try {
            Common.check_exec();
            Process proc = Runtime.getRuntime().exec(new String[]{new File_S("./service/elevate.exe").getCanonicalPath(), "-c", "-w", "net", "user"}, null, (File)new File_S("./service/"));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Common.streamCopier(null, null, proc.getInputStream(), baos, false, true, true);
            proc.destroy();
            String data = new String(baos.toByteArray());
            return data.toUpperCase().indexOf("NOT A VALID") < 0 && data.toUpperCase().indexOf("ACCESS DENIED") < 0;
        }
        catch (Throwable t) {
            Common.log("SERVER", 0, t);
            return false;
        }
    }

    public static boolean install_windows_service(int ram_megabytes, String service_name, String main_jar) {
        return Common.install_windows_service(ram_megabytes, service_name, main_jar, true);
    }

    public static boolean install_windows_service(int ram_megabytes, String service_name, String main_jar, boolean start) {
        boolean ok = true;
        if (Common.machine_is_windows()) {
            try {
                String data;
                BufferedReader proc_in;
                Process proc;
                Common.write_wrapper_files(ram_megabytes, Integer.parseInt(System.getProperty("sun.arch.data.model", "64")), service_name, main_jar);
                String bat = "";
                bat = String.valueOf(bat) + service_name + "Service.exe --WinRun4J:RegisterService\r\n";
                if (service_name.equals(System.getProperty("appname", "CrushFTP"))) {
                    bat = String.valueOf(bat) + System.getProperty("appname", "CrushFTP") + "Restart.exe --WinRun4J:RegisterService\r\n";
                }
                if (start) {
                    bat = String.valueOf(bat) + "net start \"" + service_name + " Server\"\r\n";
                }
                RandomAccessFile out = new RandomAccessFile(new File_S("./service/service.bat"), "rw");
                out.setLength(0L);
                out.write(bat.getBytes());
                out.close();
                if (Common.test_elevate()) {
                    Common.check_exec();
                    proc = Runtime.getRuntime().exec(new String[]{new File_S("./service/elevate.exe").getCanonicalPath(), "-c", "-w", "service.bat"}, null, (File)new File_S("./service/"));
                    proc_in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    data = "";
                    while ((data = proc_in.readLine()) != null) {
                        Common.log("SERVER", 0, data);
                        if (data.toUpperCase().indexOf("NOT A VALID") < 0 && data.toUpperCase().indexOf("ACCESS DENIED") < 0) continue;
                        ok = false;
                    }
                    proc_in.close();
                    proc.waitFor();
                } else {
                    ok = false;
                }
                if (!ok) {
                    ok = true;
                    Common.check_exec();
                    proc = Runtime.getRuntime().exec(new String[]{"cmd", "/C", "service.bat"}, null, (File)new File_S("./service/"));
                    proc_in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    data = "";
                    while ((data = proc_in.readLine()) != null) {
                        Common.log("SERVER", 0, data);
                        if (data.toUpperCase().indexOf("ACCESS DENIED") < 0) continue;
                        ok = false;
                    }
                    proc_in.close();
                    proc.waitFor();
                }
            }
            catch (Exception e) {
                Common.log("SERVER", 0, e);
            }
            new File_S("./service/elevate.exe").delete();
            new File_S("./service/service.bat").delete();
        }
        return ok;
    }

    public static String install_windows_service_username(String domainuser, String domainpass, String service_name) {
        boolean ok = false;
        String result = "";
        if (Common.machine_is_windows()) {
            try {
                String data;
                BufferedReader proc_in;
                Process proc;
                String bat = "setlocal\r\n";
                bat = String.valueOf(bat) + "sc.exe config \"" + service_name + " Server\" obj= \"" + domainuser + "\" password= \"" + domainpass + "\"\r\n";
                RandomAccessFile out = new RandomAccessFile(new File_S("./service/service.bat"), "rw");
                out.setLength(0L);
                out.write(bat.getBytes());
                out.close();
                if (Common.test_elevate()) {
                    Common.check_exec();
                    proc = Runtime.getRuntime().exec(new String[]{new File_S("./service/elevate.exe").getCanonicalPath(), "-c", "-w", "service.bat"}, null, (File)new File_S("./service/"));
                    proc_in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    data = "";
                    while ((data = proc_in.readLine()) != null) {
                        Common.log("SERVER", 0, data);
                        if (data.toUpperCase().indexOf("INVALID") >= 0 || data.toUpperCase().indexOf("ACCESS DENIED") >= 0) {
                            ok = false;
                        }
                        if (data.toUpperCase().indexOf("SUCCESS") >= 0) {
                            ok = true;
                        }
                        result = String.valueOf(result) + data + "\r\n";
                    }
                    proc_in.close();
                    proc.waitFor();
                } else {
                    ok = false;
                }
                if (!ok) {
                    Common.check_exec();
                    proc = Runtime.getRuntime().exec(new String[]{"cmd", "/C", "service.bat"}, null, (File)new File_S("./service/"));
                    proc_in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    data = "";
                    while ((data = proc_in.readLine()) != null) {
                        Common.log("SERVER", 0, data);
                        if (data.toUpperCase().indexOf("ACCESS DENIED") >= 0) {
                            ok = false;
                        }
                        if (data.toUpperCase().indexOf("INVALID") >= 0) {
                            ok = false;
                        }
                        if (data.toUpperCase().indexOf("SUCCESS") >= 0) {
                            ok = true;
                        }
                        result = String.valueOf(result) + data + "\r\n";
                    }
                    proc_in.close();
                    proc.waitFor();
                }
            }
            catch (Exception e) {
                Common.log("SERVER", 0, e);
            }
            new File_S("./service/service.bat").delete();
            new File_S("./service/elevate.exe").delete();
        }
        if (ok) {
            return "";
        }
        return result;
    }

    public static void write_wrapper_files(int ram_megabytes, int bit, String service_name, String main_jar) throws Exception {
        Common.writeFileFromJar_assets("service/" + System.getProperty("appname", "CrushFTP") + "Service" + bit + ".exe", "./", true);
        if (service_name.equals(System.getProperty("appname", "CrushFTP"))) {
            Common.copy(new File_B("service/" + System.getProperty("appname", "CrushFTP") + "Service" + bit + ".exe").getPath(), new File_B("service/" + System.getProperty("appname", "CrushFTP") + "Restart.exe").getPath(), true);
        }
        new File_S("service/" + System.getProperty("appname", "CrushFTP") + "Service" + bit + ".exe").renameTo(new File_S("service/" + service_name + "Service.exe"));
        Common.copyStreams(new ByteArrayInputStream(Common.getElevate()), new FileOutputStream("./service/elevate.exe", false), true, true);
        Common.write_service_conf("./service/" + service_name + "Service.ini", ram_megabytes, String.valueOf(service_name) + "WinService", String.valueOf(service_name) + " Server", main_jar);
        if (service_name.equals(System.getProperty("appname", "CrushFTP"))) {
            Common.write_service_conf("./service/" + System.getProperty("appname", "CrushFTP") + "Restart.ini", ram_megabytes, String.valueOf(System.getProperty("appname", "CrushFTP")) + "WinServiceRestart", String.valueOf(System.getProperty("appname", "CrushFTP")) + "Restart", main_jar);
        }
    }

    public static boolean remove_windows_service(String service_name, String main_jar) {
        boolean ok = true;
        if (Common.machine_is_windows()) {
            try {
                String data;
                BufferedReader proc_in;
                Process proc;
                Common.write_wrapper_files(512, Integer.parseInt(System.getProperty("sun.arch.data.model", "64")), service_name, main_jar);
                String bat = "net stop \"" + service_name + " Server\"\r\n";
                bat = String.valueOf(bat) + service_name + "Service.exe --WinRun4J:UnregisterService\r\n";
                if (service_name.equals(System.getProperty("appname", "CrushFTP"))) {
                    bat = String.valueOf(bat) + System.getProperty("appname", "CrushFTP") + "Restart.exe --WinRun4J:UnregisterService\r\n";
                }
                RandomAccessFile out = new RandomAccessFile(new File_S("./service/service.bat"), "rw");
                out.setLength(0L);
                out.write(bat.getBytes());
                out.close();
                if (Common.test_elevate()) {
                    Common.check_exec();
                    proc = Runtime.getRuntime().exec(new String[]{new File_S("./service/elevate.exe").getCanonicalPath(), "-c", "-w", "service.bat"}, null, (File)new File_S("./service/"));
                    proc_in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    data = "";
                    while ((data = proc_in.readLine()) != null) {
                        Common.log("SERVER", 0, data);
                        if (data.toUpperCase().indexOf("NOT A VALID") < 0 && data.toUpperCase().indexOf("ACCESS DENIED") < 0) continue;
                        ok = false;
                    }
                    proc_in.close();
                    proc.waitFor();
                } else {
                    ok = false;
                }
                if (!ok) {
                    ok = true;
                    Common.check_exec();
                    proc = Runtime.getRuntime().exec(new String[]{"cmd", "/C", "service.bat"}, null, (File)new File_S("./service/"));
                    proc_in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    data = "";
                    while ((data = proc_in.readLine()) != null) {
                        Common.log("SERVER", 0, data);
                        if (data.toUpperCase().indexOf("ACCESS DENIED") < 0) continue;
                        ok = false;
                    }
                    proc_in.close();
                    proc.waitFor();
                }
                if (ok) {
                    Common.recurseDelete("./service/", false);
                }
            }
            catch (Exception e) {
                Common.log("SERVER", 1, e);
            }
            new File_S("./service/elevate.exe").delete();
            new File_S("./service/service.bat").delete();
        }
        return ok;
    }

    public static void stopDaemon(boolean silent, String daemon_name) {
        block14: {
            String results = "";
            try {
                Process proc = null;
                if (Common.machine_is_windows()) {
                    Common.copyStreams(new ByteArrayInputStream(Common.getElevate()), new FileOutputStream("./service/elevate.exe", false), true, true);
                    Common.check_exec();
                    proc = Common.test_elevate() ? Runtime.getRuntime().exec(new String[]{new File_S("./service/elevate.exe").getCanonicalPath(), "-c", "-w", "net", "stop", String.valueOf(daemon_name) + " Server"}, null, (File)new File_S("./service/")) : Runtime.getRuntime().exec(new String[]{"net", "stop", String.valueOf(daemon_name) + " Server"}, null, (File)new File_S("./service/"));
                } else if (Common.machine_is_x()) {
                    RandomAccessFile out = new RandomAccessFile(new File_S(String.valueOf(daemon_name) + "_exec_root.sh"), "rw");
                    out.setLength(0L);
                    out.write(("launchctl stop com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + daemon_name + "\n").getBytes("UTF8"));
                    out.close();
                    Common.exec(new String[]{"chmod", "+x", new File_S(String.valueOf(daemon_name) + "_exec_root.sh").getCanonicalPath()});
                    Common.check_exec();
                    proc = Runtime.getRuntime().exec(new String[]{"osascript", "-e", "do shell script \"" + new File_S(String.valueOf(daemon_name) + "_exec_root.sh").getCanonicalPath() + "\" with administrator privileges"});
                }
                BufferedReader proc_in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String data = "";
                boolean ok = true;
                while ((data = proc_in.readLine()) != null) {
                    results = String.valueOf(results) + data + "\r\n";
                    if (data.toUpperCase().indexOf("ACCESS DENIED") >= 0 || data.toUpperCase().indexOf("NOT A VALID") >= 0) {
                        ok = false;
                    }
                    Common.log("SERVER", 0, data);
                }
                proc_in.close();
                proc_in = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                data = "";
                while ((data = proc_in.readLine()) != null) {
                    results = String.valueOf(results) + data + "\r\n";
                    if (data.toUpperCase().indexOf("ACCESS DENIED") >= 0 || data.toUpperCase().indexOf("NOT A VALID") >= 0) {
                        ok = false;
                    }
                    Common.log("SERVER", 0, data);
                }
                if (ok && !silent) {
                    JOptionPane.showMessageDialog(null, "Stopped\r\n\r\n" + results);
                } else if (!silent) {
                    JOptionPane.showMessageDialog(null, "Failure:\r\n\r\n" + results);
                }
            }
            catch (Exception e) {
                System.out.println(new Date());
                e.printStackTrace();
                if (silent) break block14;
                JOptionPane.showMessageDialog(null, String.valueOf(e.toString()) + "\r\n\r\n" + results);
            }
        }
        if (new File_S(String.valueOf(daemon_name) + "_exec_root.sh").exists()) {
            new File_S(String.valueOf(daemon_name) + "_exec_root.sh").delete();
        }
        if (new File_S("service/elevate.exe").exists()) {
            new File_S("service/elevate.exe").delete();
        }
        new File_S("./service/elevate.exe").delete();
    }

    public static void startDaemon(boolean silent, String daemon_name) {
        block14: {
            String results = "";
            try {
                Process proc = null;
                Common.check_exec();
                if (Common.machine_is_windows()) {
                    Common.copyStreams(new ByteArrayInputStream(Common.getElevate()), new FileOutputStream("./service/elevate.exe", false), true, true);
                    proc = Common.test_elevate() ? Runtime.getRuntime().exec(new String[]{new File_S("./service/elevate.exe").getCanonicalPath(), "-c", "-w", "net", "start", String.valueOf(daemon_name) + " Server"}, null, (File)new File_S("./service/")) : Runtime.getRuntime().exec(new String[]{"net", "stop", String.valueOf(daemon_name) + " Server"}, null, (File)new File_S("./service/"));
                } else if (Common.machine_is_x()) {
                    RandomAccessFile out = new RandomAccessFile(new File_S(String.valueOf(daemon_name) + "_exec_root.sh"), "rw");
                    out.setLength(0L);
                    out.write(("launchctl start com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + daemon_name + "\n").getBytes("UTF8"));
                    out.close();
                    Common.exec(new String[]{"chmod", "+x", new File_S(String.valueOf(daemon_name) + "_exec_root.sh").getCanonicalPath()});
                    proc = Runtime.getRuntime().exec(new String[]{"osascript", "-e", "do shell script \"" + new File_S(String.valueOf(daemon_name) + "_exec_root.sh").getCanonicalPath() + "\" with administrator privileges"});
                }
                BufferedReader proc_in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String data = "";
                boolean ok = true;
                while ((data = proc_in.readLine()) != null) {
                    results = String.valueOf(results) + data + "\r\n";
                    if (data.toUpperCase().indexOf("ACCESS DENIED") >= 0 || data.toUpperCase().indexOf("NOT A VALID") >= 0) {
                        ok = false;
                    }
                    Common.log("SERVER", 0, data);
                }
                proc_in.close();
                proc_in = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                data = "";
                while ((data = proc_in.readLine()) != null) {
                    results = String.valueOf(results) + data + "\r\n";
                    if (data.toUpperCase().indexOf("ACCESS DENIED") >= 0 || data.toUpperCase().indexOf("NOT A VALID") >= 0) {
                        ok = false;
                    }
                    Common.log("SERVER", 0, data);
                }
                if (ok && !silent) {
                    JOptionPane.showMessageDialog(null, "Started\r\n\r\n" + results);
                } else if (!silent) {
                    JOptionPane.showMessageDialog(null, "Failure:\r\n\r\n" + results);
                }
            }
            catch (Exception e) {
                System.out.println(new Date());
                e.printStackTrace();
                if (silent) break block14;
                JOptionPane.showMessageDialog(null, String.valueOf(e.toString()) + "\r\n\r\n" + results);
            }
        }
        if (new File_S(String.valueOf(daemon_name) + "_exec_root.sh").exists()) {
            new File_S(String.valueOf(daemon_name) + "_exec_root.sh").delete();
        }
        if (new File_S("service/elevate.exe").exists()) {
            new File_S("service/elevate.exe").delete();
        }
        new File_S("./service/elevate.exe").delete();
    }

    public static void loadPersistentVariables() {
        if (new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "persistent_job_variables.XML").exists()) {
            Properties persistent_variables = (Properties)Common.readXMLObject(String.valueOf(System.getProperty("crushftp.prefs")) + "persistent_job_variables.XML");
            System2.put("persistent_variables", persistent_variables);
            persistent_variables.put("time", String.valueOf(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "persistent_job_variables.XML").lastModified()));
        }
    }

    public static String encodeBase32(byte[] b) {
        return new Base32().encodeToString(b);
    }

    public static int totp_calculateCode(byte[] key, long time) throws Exception {
        byte[] data = new byte[8];
        long time2 = time;
        int i = 8;
        while (i-- > 0) {
            data[i] = (byte)time2;
            time2 >>>= 8;
        }
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, mac.getAlgorithm()));
        byte[] hash = mac.doFinal(data);
        int loc = hash[hash.length - 1] & 0xF;
        long shortened = 0L;
        int i2 = 0;
        while (i2 <= 3) {
            shortened <<= 8;
            shortened |= (long)(hash[loc + i2] & 0xFF);
            ++i2;
        }
        return (int)(shortened & Integer.MAX_VALUE) % (int)Math.pow(10.0, 6.0);
    }

    public static boolean totp_checkCode(String secret, long code, long l) throws Exception {
        byte[] decodedKey = new Base32().decode(secret.toUpperCase());
        int window = 10;
        int i = -((window - 1) / 2);
        while (i <= window / 2) {
            if ((long)Common.totp_calculateCode(decodedKey, l / TimeUnit.SECONDS.toMillis(30L) + (long)i) == code) {
                return true;
            }
            ++i;
        }
        return false;
    }

    public static boolean isNumeric(String s) {
        try {
            Long.parseLong(s.trim());
            return true;
        }
        catch (Exception exception) {
            return false;
        }
    }

    public static byte[] getElevate() {
        String b64 = "";
        b64 = String.valueOf(b64) + "H4sIAAAAAAAA/+1XbWxbVxk+dtwPpx9uWTNaBNqt101lqhMnTkbS2CyV69DQZPGa1mm7Re2NfW58k+t73fuRD0akIrcIy4pWkBAS4sdAjTQ+JPojPwoClokxt4hN7AdjQoUVqT9apYhojdZUCr0859zrJKYt7aRJI";
        b64 = String.valueOf(b64) + "MSxzj3v13ne9z3n+J739hw/R2oIIT502ybkInFaB3l4u4S++YmfbyYz/rd3XvR0v73zcFY2hLyuDeliTkiLqqqZwiAVdEsVZFXY39sn5LQMrd+0qXaXizFWN/KjiLnmTqXPv3z0TjOnfXdinD9+p4mPL7rjCT4ekt";
        b64 = String.valueOf(b64) + "NZZl+JJZkgpNvjJe2PGz0V2VUS8GzwrKsltWDWOrLvbsNjC7rgZslor6P2YCXWViaHnUW58APC1ucMN2S2y+PywNu1TyMG4uCeXvMIi/eoDU72/xt1vUnHTYyn/W5AtU7cqxtCOlmvG3qauLFBwBPdXG3X8Wjb/v/";
        b64 = String.valueOf(b64) + "2P9TGtxPydfRvoZ9Hn0F/B/0K+ofoZAch23c4tq3uOIhxGD21YwVHwC9EniJpTj1FDFLPj+IRooLOEwqNTCR0SjKwoETHT0N37BLLvEBexPwwaSVHyYCrJRyRtX4iAsMEJwDNsTc5Wg5yFVoTowaqnXs5RSzwo5Ar";
        b64 = String.valueOf(b64) + "4FRomb2GZxAYJuQ6lzWQMRdZQKQa0HLgVcQadP1m+XwBeAb3NMQzZvk6UYTICNnj6ilGlrnMsTLgHx5NGh4zPBLL9fnx/GY4joonQ81z+yxHNDkGy8viq1zxnAGeznfG5FgTkA2Cd9aV8ojHud5aXtVK60YOFvg0x";
        b64 = String.valueOf(b64) + "2a7IXKvbCdYnAbfFfU+68ms2E6n+TyD+/o4qzNy3x26fzz3ngxn/T7ZiNIPODO9wK2sm0H2uv8HAzZDQNvLPTDcUR4b5Sd/t7u3X3V39vNcGgLCgEtZoCr0GKeqfTvXXRDyp51LruossMicyOvd3SUkzuf3uf9Sgu";
        b64 = String.valueOf(b64) + "wtvl7sH3ekdPOFlF23cUsHKc1HZtvnzQPJ5FTP0gvJVDJLHiOkmFiy69ZDfXb2+80YzFrbWsq+BmpubSr7DYyF2Y6kXRdl1JsbobTrfEz7ODeHMnsSo2tx7Niwx64joH4Hz1+7mcHV3JeKfNRftJaK1vWitVC0bha";
        b64 = String.valueOf(b64) + "t+Yht1+1iMb3f/pfiwGLgTACXtFSqkWICGfNL7W+ZT7ffNT8jxYLE8t7+YzwOo7kPuHYTZ5ht+1vW3zAOe09IG8pSrIEEzp4jDCbuhS5wpgBmqs8nFfYKY19ps5asJ0qXCpcFqXAnbfnLiSUP9HPHwY5Ya4qTS3O9";
        b64 = String.valueOf(b64) + "bdaCta1iozKbBW7zbJt1c0VhMcVNrvhcm3Xd2rpRkGJjhEmvc+m6cmKeEaU/3Thk23abNR84+2cIGPwaQJl+BBM481uIpLaI5W+bXAqc/QW44UBkFqpjU5Pnb/24nDgfBUg58VrHGjZc2LatA8MsQ7YOFgdmIperF";
        b64 = String.valueOf(b64) + "mx3+zvmLr5ggdvvgl5bSMx452qkYiQen/uDVGgXxmo5w3zOFwculgbmb/iA9jp79E+d/d4/bDuZ9cGLXbcFewPMQHvZ2OBoiokZltjMwmMdLJHA2VcQR2S2NLlYuGx/KP309rtd30yVFlPFyWt2ncCnFxPY2A9gVl";
        b64 = String.valueOf(b64) + "Oe2k92J3EOP8sVgTOXCTt584XZjaXFX10D5PR0YXbzdNK2FqXpYuKqA1HquTrl21fquQaj95nRVLffOz01vr70xhs3vKVbyODWNLblrzW3zkc+mqsp/HoBy1uO+9i/2lxfrmHv4rm15Rp2p0VmC5NLHqkcrxVI4Nu";
        b64 = String.valueOf(b64) + "zU77fsP+KVIyvx1pMJc7jBO/ZwnMrLVrr+pxTjkO4s826Yu7ARpsbhm3bumLXbYeCE9tApOaO3fjOkm2XWlrBZfMI036v7Nv1UoW49KkOMpx+/QC0/fZ72VbGjjDq1a2gVEbtZpTFqAuwGh4DVXgzOnzX/Sc1b1+5";
        b64 = String.valueOf(b64) + "p59hdzbqxJOrZF+GbA9k2VUyE7Lm1QXuJ1wz/IwkxmUzqWtpahjkJW+3lhaVTp1SskA6NT0nmj1QiEO0n5BTni9Rs1s0zISuazohxxifUEdlXVNzVDVToi6LgwpMm0lc0Qx6QFQzCiXnavpF2QRan6wOKbR3cJimT";
        b64 = String.valueOf(b64) + "XLU8bVPUTRs4D6GFbd0HTj7ZR0Gmj4Bn1u5XMvlANUtq8A+mDj0fKI70lSfURSU2Z6+LFWUxDhNWyZNjPeTvgOJ7or6olcay+uyakqEdHpOyNogIQdqKqKcMZrWTQfnv6t5K59Gp9k2tmKs1nsIe3skHyDP3kfu99";
        b64 = String.valueOf(b64) + "0jXJazl+2rcJb3rmi2PEmIvIrPe5vxTOF2OoFnghwC1YWb9XnwXXh2gmbtl76/33W+4Dycr4zPuTg+/Dz/EsXvPWxGH+5e3a0MOjGyG7kLHKuCCPkJtwnjZIVJGx8H+adhFB9PHvfmzPObdwLRiOAox67UffW4wxW";
        b64 = String.valueOf(b64) + "gWqBYXTEEnUqexNyKr/286kjzGPJVdVa86l4PocZhUTq15T5oBazHSv3gzGM6kyOzWnaC44TJ+lX+Uss1WsVPI2KLoId5Zx+BG2HfyyMacmsnZXm+upxldfWyUk+Mk2cwvxv0EJ/J8sgjFgcvS0w3t2qZQH6IfpBX";
        b64 = String.valueOf(b64) + "4QLPla2ZgEwV/IRVlgbnKK+zdfinvJpxvpM9yI/V9PfuJCG7+IfxYWhFXpEpq9aNNb/vgi/63HhOEUapbsiaGgs21oeDAlXTWgavkFjwyOHOUGtQMEy8FURFU2ksOEGN4HNf3FQbFQ2D5gaVCQEAqhELWrq610hna";
        b64 = String.valueOf(b64) + "U40Qjk5rWuGJpmhtJbbKxq5+tHGoIB3iyxRw0yt9sagTN0yzC5V0h4Rq4nN8kcNvIt02ZxgjD+q01MWwGkmqcujskKHqMEVqzTO2wuuu+koVbjSrzAyFhSNLnVUG6F60BFb8r40e1XHgpKoGNSRNjieGh7gKtqwKq";
        b64 = String.valueOf(b64) + "Jow3JWjEPsedGUB2UF6oenWWXOFo/ji/m8IqdFloETiWHl85qOQHr7hK5MLPgybQo3Nja3fCHU2NL8bKg5km4JiS0SDYXDrRlKaSQTkcKTQaHhAdMjLY2R1sE2MdSSacN0aTATaqVNmZDY1NwcbmpqQSkkVqZHG6r";
        b64 = String.valueOf(b64) + "DiTZUBc0llTMC5p534n+m/ROkCMvDABQAAA==";
        try {
            return Base64.decode(b64);
        }
        catch (Exception e) {
            System.out.println(new Date());
            e.printStackTrace();
            return null;
        }
    }

    public static Properties google_get_refresh_token(String oauth_access_code, String server_url, String google_client_id, String google_client_secret) throws Exception {
        try {
            String full_form = "code=" + URLEncoder.encode(oauth_access_code, "UTF-8");
            full_form = String.valueOf(full_form) + "&client_id=" + google_client_id;
            full_form = String.valueOf(full_form) + "&client_secret=" + google_client_secret;
            full_form = String.valueOf(full_form) + "&redirect_uri=" + server_url;
            full_form = String.valueOf(full_form) + "&grant_type=authorization_code";
            byte[] b = full_form.getBytes("UTF8");
            URLConnection urlc = URLConnection.openConnection(new VRL("https://oauth2.googleapis.com/token"), new Properties());
            urlc.setDoOutput(true);
            urlc.setRequestMethod("POST");
            OutputStream out = urlc.getOutputStream();
            out.write(full_form.getBytes("UTF8"));
            out.close();
            String result = Common.consumeResponse(urlc.getInputStream());
            int code = urlc.getResponseCode();
            if (code < 200 || code > 299) {
                throw new Exception("Error :" + result);
            }
            JSONObject obj = (JSONObject)JSONValue.parse(result);
            Properties p = new Properties();
            if (obj.containsKey("refresh_token") && obj.containsKey("access_token")) {
                String refresh_token = (String)obj.get("refresh_token");
                String access_token = (String)obj.get("access_token");
                p.put("refresh_token", refresh_token);
                p.put("access_token", access_token);
                return p;
            }
            p.put("error", result);
            return p;
        }
        catch (Exception e) {
            Common.log("SERVER", 1, e);
            throw e;
        }
    }

    public static Properties get_smtp_oauth_refresh_token(String oauth_url, String oauth_access_code, String server_url, String oauth_client_id, String oauth_client_secret, String oauth_client_scope) {
        try {
            String full_form = "&client_id=" + oauth_client_id;
            full_form = String.valueOf(full_form) + "&code=" + oauth_access_code;
            full_form = String.valueOf(full_form) + "&scope=" + oauth_client_scope.replaceAll(" ", "%20");
            full_form = String.valueOf(full_form) + "&redirect_uri=" + server_url;
            full_form = String.valueOf(full_form) + "&grant_type=authorization_code";
            full_form = String.valueOf(full_form) + "&client_secret=" + oauth_client_secret;
            byte[] b = full_form.getBytes("UTF8");
            URLConnection urlc = URLConnection.openConnection(new VRL(String.valueOf(oauth_url) + "token"), new Properties());
            urlc.setDoOutput(true);
            urlc.setRequestMethod("POST");
            OutputStream out = urlc.getOutputStream();
            out.write(full_form.getBytes("UTF8"));
            out.close();
            int code = urlc.getResponseCode();
            String result = Common.consumeResponse(urlc.getInputStream());
            if (code < 200 || code > 299) {
                throw new IOException(result);
            }
            String refresh_token = (String)((JSONObject)JSONValue.parse(result)).get("refresh_token");
            String access_token = (String)((JSONObject)JSONValue.parse(result)).get("access_token");
            String id_token = "";
            if (((JSONObject)JSONValue.parse(result)).get("id_token") != null) {
                id_token = (String)((JSONObject)JSONValue.parse(result)).get("id_token");
            }
            Properties p = new Properties();
            p.put("refresh_token", refresh_token);
            p.put("access_token", access_token);
            if (!id_token.equals("")) {
                if (id_token.split("\\.").length == 3) {
                    String payload = id_token.split("\\.")[1];
                    byte[] decoded_bytes = java.util.Base64.getDecoder().decode(payload);
                    String data = new String(decoded_bytes, "UTF-8");
                    if (!data.endsWith("}")) {
                        data = String.valueOf(data) + "}";
                    }
                    JSONObject obj_a = (JSONObject)JSONValue.parse(data);
                    Object[] ac = obj_a.entrySet().toArray();
                    Properties pp = new Properties();
                    int i = 0;
                    while (i < ac.length) {
                        String key = ac[i].toString().split("=")[0];
                        if (key.equals("emails")) {
                            JSONArray ja = (JSONArray)obj_a.get(key);
                            String email = (String)ja.get(0);
                            pp.put("email", email);
                        } else {
                            pp.put(key.trim(), ("" + obj_a.get(key)).trim());
                        }
                        ++i;
                    }
                    pp.put("id_token", id_token);
                    p.put("id_token", pp);
                    if (!access_token.equals("")) {
                        String access_payload = access_token.split("\\.")[1];
                        byte[] bytes = java.util.Base64.getDecoder().decode(access_payload);
                        String dtat2 = new String(decoded_bytes, "UTF-8");
                        if (!dtat2.endsWith("}")) {
                            dtat2 = String.valueOf(dtat2) + "}";
                        }
                        JSONObject obj = (JSONObject)JSONValue.parse(dtat2);
                        Object[] array = obj.entrySet().toArray();
                        Properties info = new Properties();
                        int i2 = 0;
                        while (i2 < array.length) {
                            String key = array[i2].toString().split("=")[0];
                            if (key.equals("emails")) {
                                JSONArray ja = (JSONArray)obj.get(key);
                                String email = (String)ja.get(0);
                                info.put("email", email);
                            } else {
                                info.put(key.trim(), ("" + obj.get(key)).trim());
                            }
                            ++i2;
                        }
                        p.put("access_token_info", info);
                    }
                } else {
                    p.put("id_token", id_token);
                }
            }
            return p;
        }
        catch (Exception e) {
            Common.log("SERVER", 1, "OAuth2 token url :" + oauth_url + " Error : " + e);
            return null;
        }
    }

    public static Properties oauth_renew_tokens(String refresh_token, String client_id, String client_secret, String oauth_url) throws Exception {
        return Common.oauth_renew_tokens(refresh_token, client_id, client_secret, oauth_url, true);
    }

    public static Properties oauth_renew_tokens(String refresh_token, String client_id, String client_secret, String oauth_url, boolean cache) throws Exception {
        if (cache && oauth_access_tokens.containsKey(String.valueOf(client_id) + "~" + client_secret + "~" + refresh_token)) {
            Properties acces_token = (Properties)oauth_access_tokens.get(String.valueOf(client_id) + "~" + client_secret + "~" + refresh_token);
            if (System.currentTimeMillis() - Long.parseLong(acces_token.getProperty("time")) < (Long.parseLong(acces_token.getProperty("expires_in")) - 300L) * 1000L) {
                return acces_token;
            }
        }
        String last_refresh_token = refresh_token;
        if (refresh_tokens != null && refresh_tokens.containsKey(refresh_token)) {
            last_refresh_token = refresh_tokens.getProperty(refresh_token, refresh_token);
            Common.log("SERVER", 2, "OAuth renew token : Found previous refresh token.");
        }
        String full_form = "";
        if (client_id.contains("~google_jwt~")) {
            String assertion = Common.get_gogole_jwt_assertation(refresh_token, client_id.split("~")[0], client_id.split("~")[2]);
            full_form = String.valueOf(full_form) + "&assertion=" + assertion;
            full_form = String.valueOf(full_form) + "&grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer";
        } else {
            full_form = "client_id=" + client_id;
            full_form = String.valueOf(full_form) + "&client_secret=" + client_secret;
            full_form = String.valueOf(full_form) + "&refresh_token=" + last_refresh_token;
            full_form = String.valueOf(full_form) + "&grant_type=refresh_token";
        }
        URLConnection urlc = URLConnection.openConnection(new VRL(oauth_url), new Properties());
        urlc.setDoOutput(true);
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        OutputStream out = urlc.getOutputStream();
        out.write(full_form.getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            throw new IOException(result);
        }
        Common.log("SERVER", 2, "OAuth response keys: " + ((JSONObject)JSONValue.parse(result)).keySet());
        String access_token = "";
        if (((JSONObject)JSONValue.parse(result)).get("access_token") != null) {
            access_token = "" + ((JSONObject)JSONValue.parse(result)).get("access_token");
        }
        String expire_in = "";
        if (((JSONObject)JSONValue.parse(result)).get("expires_in") != null) {
            expire_in = "" + ((JSONObject)JSONValue.parse(result)).get("expires_in");
        }
        String refresh = "";
        if (((JSONObject)JSONValue.parse(result)).get("refresh_token") != null) {
            refresh = (String)((JSONObject)JSONValue.parse(result)).get("refresh_token");
            Common.log("SERVER", 2, "OAuth renew token : Got refresh token.");
            if (refresh_tokens != null) {
                refresh_tokens.put(refresh_token, refresh);
            }
        }
        Properties p = new Properties();
        if (expire_in.endsWith(",")) {
            expire_in = expire_in.substring(0, expire_in.length() - 1);
        }
        p.put("access_token", access_token);
        if (!refresh.equals("")) {
            p.put("refresh_token", refresh);
        }
        if (!expire_in.equals("")) {
            if (expire_in.endsWith(",")) {
                expire_in = expire_in.substring(0, expire_in.length() - 1);
            }
            p.put("expires_in", expire_in);
            p.put("time", String.valueOf(System.currentTimeMillis()));
        }
        if (cache && !access_token.equals("")) {
            oauth_access_tokens.put(String.valueOf(client_id) + "~" + client_secret + "~" + refresh_token, p);
        }
        return p;
    }

    public static Properties ms_client_credential_grant_token(String clinet_id, String client_secret, String tenant, String scope) throws Exception {
        Properties p = new Properties();
        try {
            String full_form = "client_id=" + clinet_id;
            full_form = String.valueOf(full_form) + "&scope=" + scope;
            full_form = String.valueOf(full_form) + "&client_secret=" + client_secret;
            full_form = String.valueOf(full_form) + "&grant_type=client_credentials";
            byte[] b = full_form.getBytes("UTF8");
            URLConnection urlc = URLConnection.openConnection(new VRL("https://login.microsoftonline.com/" + tenant + "/oauth2/v2.0/token"), new Properties());
            urlc.setDoOutput(true);
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStream out = urlc.getOutputStream();
            out.write(full_form.getBytes("UTF8"));
            out.close();
            String result = Common.consumeResponse(urlc.getInputStream());
            int code = urlc.getResponseCode();
            if (code < 200 || code > 299) {
                if (result.equals("")) {
                    result = "Error : " + urlc.getResponseMessage();
                }
                throw new Exception(result);
            }
            String access_token = (String)((JSONObject)JSONValue.parse(result)).get("access_token");
            String expire_in = "" + ((JSONObject)JSONValue.parse(result)).get("expires_in");
            p.put("access_token", access_token);
            if (expire_in != null && !expire_in.equals("")) {
                if (expire_in.endsWith(",")) {
                    expire_in = expire_in.substring(0, expire_in.length() - 1);
                }
                p.put("expires_in", expire_in);
                p.put("token_start", String.valueOf(System.currentTimeMillis()));
            }
        }
        catch (Exception e) {
            Common.log("SERVER", 1, e);
            throw e;
        }
        return p;
    }

    public static Properties parse_json_reply(String result) {
        BufferedReader br = new BufferedReader(new StringReader(result));
        String line = "";
        Properties p = new Properties();
        try {
            while ((line = br.readLine()) != null) {
                String val;
                if (line.indexOf(":") < 0) continue;
                String key = line.split(":")[0].trim();
                if (key.indexOf("\"") >= 0) {
                    key = key.substring(1, key.lastIndexOf("\""));
                }
                if ((val = line.split(":")[1].trim()).indexOf("\"") >= 0) {
                    val = val.substring(1, val.lastIndexOf("\""));
                }
                p.put(key, val);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return p;
    }

    public static String xss_strip(String keyword) {
        if ((keyword = Common.url_decode(keyword)).toUpperCase().indexOf("<") >= 0 && keyword.toUpperCase().indexOf(">") >= 0) {
            keyword = "INVALID";
        }
        if (keyword.toUpperCase().indexOf("ONKEY") >= 0 && keyword.toUpperCase().indexOf("(") >= 0) {
            keyword = "INVALID";
        }
        if (keyword.toUpperCase().indexOf("ONMOUSE") >= 0 && keyword.toUpperCase().indexOf("(") >= 0) {
            keyword = "INVALID";
        }
        if (keyword.toUpperCase().indexOf("ONCLICK") >= 0 && keyword.toUpperCase().indexOf("(") >= 0) {
            keyword = "INVALID";
        }
        if (keyword.toUpperCase().indexOf("ONDBCLICK") >= 0 && keyword.toUpperCase().indexOf("(") >= 0) {
            keyword = "INVALID";
        }
        if (keyword.toUpperCase().indexOf("ONCONTEXT") >= 0 && keyword.toUpperCase().indexOf("(") >= 0) {
            keyword = "INVALID";
        }
        if (keyword.toUpperCase().indexOf("ONFOCUS") >= 0 && keyword.toUpperCase().indexOf("(") >= 0) {
            keyword = "INVALID";
        }
        if (keyword.toUpperCase().indexOf("ONCHANGE") >= 0 && keyword.toUpperCase().indexOf("(") >= 0) {
            keyword = "INVALID";
        }
        if (keyword.toUpperCase().indexOf("ONLOAD") >= 0 && keyword.toUpperCase().indexOf("(") >= 0) {
            keyword = "INVALID";
        }
        if (keyword.toUpperCase().indexOf("EVAL") >= 0 && keyword.toUpperCase().indexOf("(") >= 0) {
            keyword = "INVALID";
        }
        if (keyword.toUpperCase().indexOf("FUNCTION") >= 0 && keyword.toUpperCase().indexOf("(") >= 0) {
            keyword = "INVALID";
        }
        if (keyword.toUpperCase().indexOf("<SCRIPT") >= 0) {
            keyword = "INVALID";
        }
        return keyword;
    }

    public static String getPgpKeyInfo(String key_path) {
        String info = "";
        try {
            if (new File_S(key_path).exists()) {
                PGPKeyPair key = new PGPKeyPair(key_path);
                info = String.valueOf(info) + "user_id=" + key.getUserID() + "; ";
                info = String.valueOf(info) + "creation_time=" + key.getCreationTime() + "; ";
                info = String.valueOf(info) + "is_expired=" + key.isExpired() + "; ";
                info = String.valueOf(info) + "expiration_date=" + key.getExpirationDate() + "; ";
                info = String.valueOf(info) + "expiration_time=" + key.getExpirationTime() + "; ";
                info = String.valueOf(info) + "algorithm=" + key.getAlgorithm() + "; ";
                info = String.valueOf(info) + "algorithm_type=" + key.getAlgorithmType().toString() + "; ";
                String preferredCiphers = "";
                int xx = 0;
                while (xx < key.getPreferredCiphers().length) {
                    int cipher = key.getPreferredCiphers()[xx];
                    preferredCiphers = cipher < CypherAlgorithm.Enum.values().length ? String.valueOf(preferredCiphers) + CypherAlgorithm.Enum.values()[cipher].toString() + " " : String.valueOf(preferredCiphers) + cipher + " ";
                    ++xx;
                }
                info = String.valueOf(info) + "prefferred_cipherse=" + preferredCiphers + "; ";
                String preferredHash = "";
                int xx2 = 0;
                while (xx2 < key.getPreferredHashes().length) {
                    int hash = key.getPreferredHashes()[xx2];
                    preferredHash = hash < HashAlgorithm.Enum.values().length ? String.valueOf(preferredHash) + HashAlgorithm.Enum.values()[hash].toString() + " " : String.valueOf(preferredHash) + hash + " ";
                    ++xx2;
                }
                info = String.valueOf(info) + "prefferred_hash=" + preferredHash + "; ";
                info = String.valueOf(info) + "ascii_version_header=" + key.getAsciiVersionHeader() + "; ";
                info = String.valueOf(info) + "valid_days=" + key.getValidDays() + "; ";
                info = String.valueOf(info) + "key_size=" + key.getKeySize() + "; ";
                info = String.valueOf(info) + "key_id_long_hex=" + key.getKeyIDLongHex() + "; ";
                info = String.valueOf(info) + "is_valid_forever=" + key.isValidForever() + "; ";
            }
        }
        catch (Exception e) {
            Common.log("SERVER", 1, e);
        }
        return info;
    }

    public static Properties get_google_user_info_fom_id_token(String id_token) throws Exception {
        Properties p = new Properties();
        URLConnection urlc = URLConnection.openConnection(new VRL("https://oauth2.googleapis.com/tokeninfo?id_token=" + id_token), new Properties());
        urlc.setRequestMethod("GET");
        urlc.setDoOutput(true);
        urlc.setUseCaches(false);
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        JSONObject obj = (JSONObject)JSONValue.parse(result);
        Common.log("SERVER", 2, "Google Id token keys: " + obj.keySet());
        Object[] a = obj.entrySet().toArray();
        int i = 0;
        while (i < a.length) {
            String key2 = a[i].toString().split("=")[0];
            p.put(key2.trim(), ("" + obj.get(key2)).trim());
            ++i;
        }
        return p;
    }

    public static Properties validate_ms_id_token(String tenant_name, String user_flow_name, String client_id, String id_token) {
        Properties p = new Properties();
        try {
            String signature = id_token.split("\\.")[1];
            byte[] decoded_bytes = java.util.Base64.getDecoder().decode(signature);
            String data = new String(decoded_bytes, "UTF-8");
            if (!data.endsWith("}")) {
                data = String.valueOf(data) + "}";
            }
            JSONObject obj_a = (JSONObject)JSONValue.parse(data);
            Common.log("SERVER", 2, "Microsoft Id token keys: " + obj_a.keySet());
            Object[] ac = obj_a.entrySet().toArray();
            Properties pa = new Properties();
            int i = 0;
            while (i < ac.length) {
                String key = ac[i].toString().split("=")[0];
                if (key.equals("emails")) {
                    JSONArray ja = (JSONArray)obj_a.get(key);
                    String email = (String)ja.get(0);
                    pa.put("email", email);
                } else {
                    pa.put(key.trim(), ("" + obj_a.get(key)).trim());
                }
                ++i;
            }
            if (!pa.getProperty("aud", "").equals(client_id) || !pa.getProperty("tfp", "").equals(user_flow_name)) {
                throw new Exception("Error: Invalid id token! Client id or userflow name does not match!");
            }
            HttpsJwks httpsJkws = new HttpsJwks("https://" + tenant_name + ".b2clogin.com/" + tenant_name + ".onmicrosoft.com/" + user_flow_name + "/discovery/v2.0/keys");
            HttpsJwksVerificationKeyResolver httpsJwksKeyResolver = new HttpsJwksVerificationKeyResolver(httpsJkws);
            JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireExpirationTime().setAllowedClockSkewInSeconds(3600).setRequireSubject().setExpectedIssuer(pa.getProperty("iss")).setExpectedAudience(client_id).setVerificationKeyResolver(httpsJwksKeyResolver).build();
            JwtClaims jwtClaims = jwtConsumer.processToClaims(id_token);
            p.put("info", pa);
        }
        catch (Exception e) {
            Common.log("SERVER", 2, e);
            p.put("error", "" + e);
        }
        return p;
    }

    public static Properties get_ms_user_info(String access_token) throws Exception {
        URLConnection urlc = URLConnection.openConnection(new VRL("https://graph.microsoft.com/v1.0/me"), new Properties());
        urlc.setDoOutput(false);
        urlc.setRequestMethod("GET");
        urlc.setRequestProperty("Authorization", "Bearer " + access_token);
        urlc.setRequestProperty("Accept", "application/json");
        urlc.setRequestProperty("Content-Type", "application/json");
        urlc.setRemoveDoubleEncoding(true);
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            throw new Exception(result);
        }
        JSONObject obj = (JSONObject)JSONValue.parse(result);
        Object[] a = obj.entrySet().toArray();
        Common.log("SERVER", 2, "Microsoft User Info keys: " + obj.keySet());
        Properties p = new Properties();
        int i = 0;
        while (i < a.length) {
            String key = a[i].toString().split("=")[0];
            p.put(key.trim(), ("" + obj.get(key)).trim());
            ++i;
        }
        if (access_token.split("\\.").length >= 1) {
            String signature = access_token.split("\\.")[1];
            byte[] decoded_bytes = Base64.decode(signature);
            String data = new String(decoded_bytes, "UTF-8");
            if (!data.endsWith("}")) {
                data = String.valueOf(data) + "}";
            }
            JSONObject obj_a = (JSONObject)JSONValue.parse(data);
            Common.log("SERVER", 2, "Microsoft User Info Access token keys: " + obj_a.keySet());
            Object[] ac = obj_a.entrySet().toArray();
            Properties pa = new Properties();
            int i2 = 0;
            while (i2 < ac.length) {
                String key = ac[i2].toString().split("=")[0];
                p.put("access_token_" + key.trim(), ("" + obj_a.get(key)).trim());
                ++i2;
            }
        }
        return p;
    }

    public static String get_gogole_jwt_assertation(String content, String user, String scope) throws Exception {
        String privateKey = "";
        String passphrase = "";
        String clientId = "";
        String clientEmail = "";
        String privateKeyID = "";
        PrivateKey key = null;
        JSONObject jo = (JSONObject)JSONValue.parse(content);
        clientId = (String)jo.get("client_id");
        clientEmail = (String)jo.get("client_email");
        privateKeyID = (String)jo.get("private_key_id");
        privateKey = (String)jo.get("private_key");
        if (!added_bc) {
            Provider provider = (Provider)BCProxy.instance().loader.loadClass("org.bouncycastle.jce.provider.BouncyCastleProvider").getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            Security.addProvider(provider);
            added_bc = true;
        }
        BufferedReader reader = new BufferedReader(new StringReader(privateKey));
        String line = null;
        String encoded_key = "";
        while ((line = reader.readLine()) != null) {
            if (line.indexOf("PRIVATE KEY") > 0) continue;
            encoded_key = String.valueOf(encoded_key) + line;
        }
        byte[] bytes = Base64.decode(encoded_key.getBytes("UTF8"));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        key = kf.generatePrivate(keySpec);
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(clientEmail);
        claims.setAudience("https://oauth2.googleapis.com/token");
        if (!user.equals("")) {
            claims.setSubject(user);
        } else {
            claims.setSubject(clientEmail);
        }
        claims.setClaim("scope", scope);
        claims.setIssuedAtToNow();
        claims.setGeneratedJwtId(64);
        claims.setExpirationTimeMinutesInTheFuture(0.75f);
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(key);
        jws.setAlgorithmHeaderValue("RS256");
        jws.setHeader("typ", "JWT");
        jws.setHeader("kid", privateKeyID);
        String assertion = jws.getCompactSerialization();
        return assertion;
    }

    public static Properties get_cognito_user_info(String cognito_domain_prefix, String access_token) throws Exception {
        URLConnection urlc = URLConnection.openConnection(new VRL("https://" + cognito_domain_prefix + ".amazoncognito.com/oauth2/userInfo"), new Properties());
        urlc.setRequestMethod("GET");
        urlc.setRequestProperty("Authorization", "Bearer " + access_token);
        urlc.setRequestProperty("Accept", "application/json");
        int code = urlc.getResponseCode();
        String result = Common.consumeResponse(urlc.getInputStream());
        JSONObject jo = (JSONObject)JSONValue.parse(result);
        Object[] a = jo.entrySet().toArray();
        Common.log("SERVER", 2, "Cognito User Info keys: " + jo.keySet());
        Properties info = new Properties();
        int i = 0;
        while (i < a.length) {
            String key2 = a[i].toString().split("=")[0];
            info.put(key2.trim(), ("" + jo.get(key2)).trim());
            ++i;
        }
        return info;
    }

    public static String[] html_clean_usernames(String[] usernames) {
        int x = 0;
        while (x < usernames.length) {
            usernames[x] = Common.html_clean(usernames[x]);
            ++x;
        }
        return usernames;
    }

    public static String html_clean(String s) {
        return s.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
    }

    public static void initRefreshTokens(Properties p) {
        refresh_tokens = p;
    }

    public static void searchObjectLog(Object src, Vector log) throws Exception {
        block9: {
            block8: {
                if (src == null || !(src instanceof Properties)) break block8;
                log.addElement("(Properties)");
                Properties p = (Properties)src;
                Enumeration<Object> e = p.keys();
                while (e.hasMoreElements()) {
                    String key = e.nextElement().toString();
                    log.addElement(key);
                    Object val = p.get(key);
                    if (val instanceof String) continue;
                    if (val instanceof Properties || val instanceof Vector) {
                        Common.searchObjectLog(val, log);
                        continue;
                    }
                    if (val instanceof Object) {
                        log.addElement("can't clone generic object from properties:" + val);
                        throw new Exception("can't clone generic object from properties");
                    }
                    log.addElement("can't clone unknown object from properties:" + val);
                    throw new Exception("can't clone unknown object from properties");
                }
                break block9;
            }
            if (src == null || !(src instanceof Vector)) break block9;
            log.addElement("(Vector)");
            Vector v = (Vector)src;
            int x = 0;
            while (x < v.size()) {
                Object val = v.elementAt(x);
                if (!(val instanceof String)) {
                    if (val instanceof Properties || val instanceof Vector) {
                        Common.searchObjectLog(val, log);
                    } else {
                        if (val instanceof Object) {
                            log.addElement("can't clone generic object from vector:" + val);
                            throw new Exception("can't clone generic object from vector");
                        }
                        log.addElement("can't clone unknown object from vector:" + val);
                        throw new Exception("can't clone unknown object from vector");
                    }
                }
                ++x;
            }
        }
    }

    public static void putAllSafe(Properties p1, Properties p2) {
        if (p2 == null) {
            return;
        }
        if (p1 == null) {
            return;
        }
        Enumeration<Object> keys = p2.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (p1.containsKey(key)) continue;
            p1.put(key, p2.get(key));
        }
    }
}

