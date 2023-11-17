/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bouncycastle.jce.provider.BouncyCastleProvider
 *  org.jdom.Content
 *  org.jdom.Document
 *  org.jdom.Element
 *  org.jdom.Namespace
 *  org.jdom.input.SAXBuilder
 *  org.jdom.output.Format
 *  org.jdom.output.XMLOutputter
 *  org.jdom.transform.XSLTransformer
 */
package crushftp.handlers;

import com.crushftp.client.AS2Client;
import com.crushftp.client.AzureClient;
import com.crushftp.client.B2Client;
import com.crushftp.client.Base64;
import com.crushftp.client.BoxClient;
import com.crushftp.client.CitrixClient;
import com.crushftp.client.CustomClient;
import com.crushftp.client.DropBoxClient;
import com.crushftp.client.FTPClient;
import com.crushftp.client.FileClient;
import com.crushftp.client.File_B;
import com.crushftp.client.File_S;
import com.crushftp.client.File_U;
import com.crushftp.client.GDriveClient;
import com.crushftp.client.GStorageClient;
import com.crushftp.client.GenericClient;
import com.crushftp.client.GlacierClient;
import com.crushftp.client.HTTPClient;
import com.crushftp.client.HadoopClient;
import com.crushftp.client.Mailer;
import com.crushftp.client.MemoryClient;
import com.crushftp.client.OneDriveClient;
import com.crushftp.client.OutputStreamCloser;
import com.crushftp.client.RFileClient;
import com.crushftp.client.S3Client;
import com.crushftp.client.S3CrushClient;
import com.crushftp.client.SFTPClient;
import com.crushftp.client.SMB1Client;
import com.crushftp.client.SMB4jClient;
import com.crushftp.client.SMBjNQClient;
import com.crushftp.client.SharePointClient;
import com.crushftp.client.TrustManagerCustom;
import com.crushftp.client.VRL;
import com.crushftp.client.WebDAVClient;
import com.crushftp.client.Worker;
import com.crushftp.client.ZipClient;
import com.crushftp.crypt.BCrypt;
import com.crushftp.crypt.Crypt3;
import com.crushftp.crypt.MD5Crypt;
import com.crushftp.crypt.SHA512Crypt;
import com.crushftp.tunnel2.Tunnel2;
import com.didisoft.pgp.PGPLib;
import com.didisoft.pgp.inspect.PGPInspectLib;
import crushftp.db.SearchHandler;
import crushftp.db.SearchTools;
import crushftp.db.StatTools;
import crushftp.gui.LOC;
import crushftp.handlers.DesEncrypter;
import crushftp.handlers.Log;
import crushftp.handlers.PBKDF2WithHmacSHA256;
import crushftp.handlers.PreviewWorker;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.Sounds;
import crushftp.handlers.SyncTools;
import crushftp.handlers.UserTools;
import crushftp.server.QuickConnect;
import crushftp.server.RETR_handler;
import crushftp.server.STOR_handler;
import crushftp.server.ServerStatus;
import crushftp.server.VFS;
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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.XSLTransformer;

public class Common {
    public static Object writeLock = new Object();
    static String CRLF = "\r\n";
    static Properties pluginCache = new Properties();
    static Properties pluginCacheTime = new Properties();
    static String registration_name = "";
    static String registration_email = "";
    static String registration_code = "";
    public static boolean base64Decode = true;
    static PGPLib pgp = null;
    public static boolean addedBC = false;
    static Vector local_ips = null;
    public static Object onserversocklock = new Object();
    public static SecureRandom rn = new SecureRandom();
    public static boolean providerAdded;
    public static boolean fips140;
    static final String XML_master = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><userfile type=\"properties\">\t<password>Acf9C+U0B0UvQiwbMd9Km+uEHaQO/nLf</password>\t<root_dir>/</root_dir>\t<version>1.0</version>\t<max_logins>0</max_logins></userfile>";
    static final String XML_VFS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><VFS type=\"vector\">\t<VFS_subitem type=\"properties\">\t\t<url>FILE://</url>\t\t<type>dir</type>\t</VFS_subitem></VFS>";
    static final String XML_VFS_ITEM = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><VFS type=\"properties\">\t<item name=\"/\">(read)(view)(resume)</item></VFS>";
    int proFTPDLineCount = 0;
    static Properties recent_corrupt_users;
    public static Properties xmlCache;
    static long xmlLastCacheClean;
    public static String[] pads;
    static String isOSXApp;
    public static long lastPrefBackup;
    private static final byte[] iv;

    static {
        if (!addedBC) {
            addedBC = true;
            try {
                Security.setProperty("crypto.policy", "unlimited");
            }
            catch (Exception exception) {
                // empty catch block
            }
            Security.addProvider((Provider)new BouncyCastleProvider());
        }
        rn.nextBytes((String.valueOf(Runtime.getRuntime().maxMemory()) + Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()).getBytes());
        providerAdded = false;
        fips140 = false;
        recent_corrupt_users = new Properties();
        xmlCache = new Properties();
        xmlLastCacheClean = System.currentTimeMillis();
        pads = new String[]{"", " ", "  ", "   ", "    ", "     ", "      ", "       ", "        ", "         ", "          ", "           ", "            ", "             ", "              ", "               "};
        isOSXApp = "";
        lastPrefBackup = 0L;
        iv = new byte[]{-114, 18, 57, -100, 7, 114, 111, 90, -114, 18, 57, -100, 7, 114, 111, 90};
    }

    public static int V() {
        return 10;
    }

    public boolean register(String registration_name, String registration_email, String registration_code) {
        Common.registration_name = registration_name.toUpperCase().trim();
        Common.registration_email = registration_email.toUpperCase().trim();
        Common.registration_code = registration_code;
        DesEncrypter crypt = new DesEncrypter("crushftp:" + registration_name + registration_email, false);
        String s = crypt.decrypt(registration_code);
        if (s == null || !s.startsWith("(")) {
            crypt = new DesEncrypter("crushftp:" + registration_name + registration_email, true);
            s = crypt.decrypt(registration_code);
        }
        if (s != null && s.startsWith("(") && s.indexOf(")") > s.indexOf("(")) {
            int pos = s.indexOf("(DATE:");
            if (pos >= 0) {
                Date d;
                block11: {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    d = sdf.parse(s.substring(pos + "(DATE:".length(), s.indexOf(")", pos)));
                    if (new Date().getTime() <= d.getTime()) break block11;
                    Log.log("SERVER", 0, "Temporary license has expired.");
                    return false;
                }
                try {
                    Log.log("SERVER", 0, "Temporary license will expire on:" + d.toString());
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
            }
            return true;
        }
        if (s != null && s.startsWith("[") && s.indexOf("]") > s.indexOf("[")) {
            int pos = s.indexOf("[DATE:");
            if (pos >= 0) {
                Date d;
                block12: {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    d = sdf.parse(s.substring(pos + "[DATE:".length(), s.indexOf("]", pos)));
                    if (new Date().getTime() <= d.getTime()) break block12;
                    Log.log("SERVER", 0, "Temporary license has expired.");
                    return false;
                }
                try {
                    Log.log("SERVER", 0, "Temporary license will expire on:" + d.toString());
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
            }
            return true;
        }
        return false;
    }

    public String getRegistrationAccess(String key, String code) {
        DesEncrypter crypt = new DesEncrypter("crushftp:" + registration_name + registration_email, base64Decode);
        String s = crypt.decrypt(code);
        if (s == null) {
            crypt = new DesEncrypter("crushftp:" + registration_name + registration_email, false);
            s = crypt.decrypt(code);
        }
        s = this.findRegistrationValue(key, s, "(", ")") != null ? this.findRegistrationValue(key, s, "(", ")") : this.findRegistrationValue(key, s, "[", "]");
        String blackList = "";
        blackList = String.valueOf(blackList) + "uHs5Vvj3ujnBRfRX2UpnvA==\r\n";
        blackList = String.valueOf(blackList) + "gZZZVip+1PUm/FwxLE1+/ogwcLA9pzLD\r\n";
        blackList = String.valueOf(blackList) + "mnhVl3sUUqUFADJukeYLvA==\r\n";
        blackList = String.valueOf(blackList) + "4RJ6S3zfM5URyl8YQBdhpw==\r\n";
        blackList = String.valueOf(blackList) + "eNul0xzgonKJ6pGWrU7XmdKkG/J4Rvec\r\n";
        blackList = String.valueOf(blackList) + "4oRzZ/kng2mL8DOCmNOe0g==\r\n";
        blackList = String.valueOf(blackList) + "N+WrqZQ9IAcAUo4pY2kqQqmpwcdKX9MK\r\n";
        blackList = String.valueOf(blackList) + "UkayxZ/KNlO8N+BekoHkgXvIsl12DopG\r\n";
        blackList = String.valueOf(blackList) + "6hB28j0II/3FjVimQ2yMhzyFUYZ5xK55\r\n";
        if (registration_name.equalsIgnoreCase("KCN") || registration_email.equalsIgnoreCase("CREW") || registration_email.toUpperCase().indexOf("TEAMARN") >= 0 || blackList.indexOf(code) >= 0) {
            try {
                class Waiting
                implements Runnable {
                    Waiting() {
                    }

                    @Override
                    public void run() {
                        Log.log("SERVER", 0, "An unknown error type 23 has occured.  Attempting to recover...");
                        try {
                            Thread.sleep(1380000L);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        Log.log("SERVER", 0, "An unrecoverable error type 23 has occured.  No more internal memory buffer space.");
                        System.exit(0);
                    }
                }
                Worker.startWorker(new Waiting());
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        return s;
    }

    public String findRegistrationValue(String key, String s, String c1, String c2) {
        if (s.toUpperCase().indexOf(String.valueOf(c1) + key.toUpperCase() + "=") >= 0) {
            s = s.substring(s.toUpperCase().indexOf(String.valueOf(c1) + key.toUpperCase() + "="), s.indexOf(c2, s.toUpperCase().indexOf(String.valueOf(c1) + key.toUpperCase() + "=")));
            if ((s = s.substring(("(" + key.toUpperCase() + "=").length())).equals("100")) {
                s = "32768";
            }
            return s;
        }
        return null;
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
        return System.getProperties().getProperty("os.name", "").toUpperCase().equals("MAC OS X");
    }

    public static boolean machine_is_x_10_5_plus() {
        String[] version;
        return Common.machine_is_x() && Integer.parseInt((version = System.getProperties().getProperty("os.version", "").split("\\."))[0]) >= 10 && Integer.parseInt(version[1]) >= 5;
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

    public static boolean machine_is_linux() {
        if (System.getProperties().getProperty("os.name", "").toUpperCase().indexOf("LINUX") >= 0) {
            return true;
        }
        return System.getProperties().getProperty("os.name", "").toUpperCase().indexOf("HP-UX") >= 0;
    }

    public static boolean machine_is_unix() {
        return System.getProperties().getProperty("os.name", "").toUpperCase().indexOf("UNIX") >= 0;
    }

    public static boolean machine_is_windows() {
        return System.getProperties().getProperty("os.name", "").toUpperCase().indexOf("NDOWS") >= 0;
    }

    public static boolean machine_is_solaris() {
        try {
            return System.getProperties().getProperty("os.name").toUpperCase().indexOf("SUNOS") >= 0;
        }
        catch (Exception exception) {
            return false;
        }
    }

    public static boolean debug(int level, String s) {
        return Log.log("GENERAL", level, s);
    }

    public static boolean debug(int level, Throwable e) {
        return Log.log("GENERAL", level, e);
    }

    public static boolean debug(int level, Exception e) {
        return Log.log("GENERAL", level, e);
    }

    public static String format_string(String heading, String c) {
        String total_string = "";
        String original_string = String.valueOf(c.trim()) + CRLF;
        c = original_string.substring(0, original_string.indexOf("\r")).trim();
        while (c.length() > 0) {
            total_string = String.valueOf(total_string) + heading + c + CRLF;
            original_string = String.valueOf(original_string.substring(original_string.indexOf("\r"), original_string.length()).trim()) + CRLF;
            c = original_string.substring(0, original_string.indexOf("\r")).trim();
        }
        return total_string;
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

    public static void setFileText(String s, String file) throws IOException {
        RandomAccessFile f = new RandomAccessFile(new File_S(file), "rw");
        f.setLength(0L);
        f.write(s.getBytes("UTF8"));
        f.close();
    }

    public static String getFileText(String file) throws IOException {
        if (!new File_S(file).exists()) {
            return null;
        }
        RandomAccessFile f = new RandomAccessFile(new File_S(file), "r");
        byte[] b = new byte[(int)f.length()];
        f.readFully(b);
        f.close();
        return new String(b, "UTF8");
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
            String s2 = s.replace('+', '\u00fe');
            s2 = URLDecoder.decode(s2, "UTF8");
            s = s2.replace('\u00fe', '+');
        }
        catch (Exception e) {
            try {
                String s2 = "";
                int x = 0;
                while (x < s.length()) {
                    if (s.charAt(x) == '%' && x + 3 < s.length()) {
                        try {
                            s2 = String.valueOf(s2) + URLDecoder.decode(s.substring(x, x + 3), "UTF8");
                            x += 2;
                        }
                        catch (IllegalArgumentException ee) {
                            s2 = String.valueOf(s2) + s.charAt(x);
                        }
                    } else {
                        s2 = String.valueOf(s2) + s.charAt(x);
                    }
                    ++x;
                }
                s = s2;
            }
            catch (Exception ee) {
                Log.log("SERVER", 0, ee);
            }
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
            return_str = URLEncoder.encode(master_string, "utf-8");
            x = 0;
            while (x < OK_chars.length()) {
                s = URLEncoder.encode(String.valueOf(OK_chars.charAt(x)), "utf-8");
                return_str = Common.replace_str(return_str, s, String.valueOf(OK_chars.charAt(x)));
                ++x;
            }
            return_str = return_str.replaceAll("\\+", "%20");
            return return_str;
        }
        catch (Exception x) {
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
lbl26:
        // 1 sources

        return return_str;
    }

    public static int count_str(String master_string, String search_data) {
        int count = 0;
        int start_loc = 0;
        int end_loc = 0;
        try {
            start_loc = master_string.indexOf(search_data);
            while (start_loc >= 0 && !master_string.equals("") && !search_data.equals("")) {
                ++count;
                end_loc = start_loc + search_data.length();
                start_loc = master_string.indexOf(search_data, end_loc);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return count;
    }

    public String discover_ip() {
        int endLoc;
        int start_loc;
        String the_ip;
        String data;
        BufferedOutputStream gos;
        BufferedReader gis;
        Socket get_ip_socket = null;
        try {
            get_ip_socket = new Socket();
            get_ip_socket.setSoTimeout(5000);
            get_ip_socket.connect(new InetSocketAddress("checkip.dyndns.org", 80));
            gis = new BufferedReader(new InputStreamReader(get_ip_socket.getInputStream()));
            gos = new BufferedOutputStream(get_ip_socket.getOutputStream());
            gos.write(("GET / HTTP/1.0" + CRLF + CRLF).getBytes("UTF8"));
            gos.flush();
            data = "";
            while (data.toUpperCase().indexOf("ADDRESS") < 0) {
                data = gis.readLine();
                if (data != null) continue;
                throw new IOException("no response from server");
            }
            gis.close();
            gos.close();
            get_ip_socket.close();
            the_ip = String.valueOf(data) + CRLF;
            start_loc = the_ip.indexOf("Address:") + 8;
            endLoc = the_ip.indexOf("\n", start_loc);
            the_ip = the_ip.substring(start_loc, endLoc).trim();
            if (the_ip.indexOf("<") >= 0) {
                the_ip = the_ip.substring(0, the_ip.indexOf("<")).trim();
            }
            Log.log("SERVER", 2, "Auto IP lookup:" + the_ip);
            if (the_ip.indexOf("0.0.0.0") < 0) {
                String string = the_ip;
                return string;
            }
        }
        catch (IOException ee) {
            Log.log("SERVER", 1, ee);
        }
        finally {
            if (get_ip_socket != null) {
                try {
                    get_ip_socket.close();
                }
                catch (Exception exception) {}
            }
        }
        try {
            try {
                Thread.sleep(1000L);
            }
            catch (Exception ee) {
                // empty catch block
            }
            get_ip_socket = new Socket();
            get_ip_socket.setSoTimeout(5000);
            get_ip_socket.connect(new InetSocketAddress("www." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".com", 80));
            gis = new BufferedReader(new InputStreamReader(get_ip_socket.getInputStream()));
            gos = new BufferedOutputStream(get_ip_socket.getOutputStream());
            gos.write(("GET /ip.jsp HTTP/1.0" + CRLF + CRLF).getBytes("UTF8"));
            gos.flush();
            data = "";
            while (data.toUpperCase().indexOf("ADDRESS") < 0) {
                data = gis.readLine();
                if (data != null) continue;
                throw new IOException("no response from server");
            }
            gis.close();
            gos.close();
            get_ip_socket.close();
            the_ip = String.valueOf(data) + CRLF;
            start_loc = the_ip.indexOf("Address:") + 8;
            endLoc = the_ip.indexOf("\n", start_loc);
            the_ip = the_ip.substring(start_loc, endLoc).trim();
            if (the_ip.indexOf("<") >= 0) {
                the_ip = the_ip.substring(0, the_ip.indexOf("<")).trim();
            }
            Log.log("SERVER", 2, "Auto IP lookup:" + the_ip);
            String string = the_ip;
            return string;
        }
        catch (IOException ee) {
            Log.log("SERVER", 1, ee);
        }
        finally {
            if (get_ip_socket != null) {
                try {
                    get_ip_socket.close();
                }
                catch (Exception exception) {}
            }
        }
        return "0.0.0.0";
    }

    public String encode_pass(String raw, String method) {
        DesEncrypter crypt = new DesEncrypter(new String(com.crushftp.client.Common.encryption_password), base64Decode);
        String s = crypt.encrypt(raw, method, base64Decode, "");
        return s;
    }

    public String encode_pass(String raw, String method, String salt) {
        DesEncrypter crypt = new DesEncrypter(new String(com.crushftp.client.Common.encryption_password), base64Decode);
        String s = crypt.encrypt(raw, method, base64Decode, salt);
        return s;
    }

    public String crypt3(String raw, String hash) {
        if (hash.startsWith("CRYPT3:")) {
            return "CRYPT3:" + Crypt3.crypt(hash.substring(7, 9), raw);
        }
        return "";
    }

    public String bcrypt(String raw, String hash) {
        if (hash.startsWith("BCRYPT:")) {
            return "BCRYPT:" + BCrypt.hashpw(raw, hash.substring(7));
        }
        return "";
    }

    public String pbkdf2sha256(String plaintext_password, String existing_hashed) {
        if (existing_hashed.startsWith("PBKDF2SHA256:")) {
            return "PBKDF2SHA256:" + PBKDF2WithHmacSHA256.hashpw(plaintext_password, existing_hashed.substring("PBKDF2SHA256:".length()));
        }
        return "";
    }

    public String md5crypt(String raw, String hash) {
        if (hash.startsWith("MD5CRYPT:")) {
            return "MD5CRYPT:" + MD5Crypt.crypt(raw, hash.substring(12, 20));
        }
        return "";
    }

    public String sha512crypt(String raw, String hash, int rounds) {
        if (hash.startsWith("SHA512CRYPT:")) {
            return "SHA512CRYPT:" + SHA512Crypt.Sha512_crypt(raw, hash.substring("SHA512CRYPT:".length()), rounds);
        }
        return "";
    }

    public String decode_pass(String raw) {
        DesEncrypter crypt = new DesEncrypter(new String(com.crushftp.client.Common.encryption_password), base64Decode);
        String s = crypt.decrypt(raw);
        if (s == null) {
            crypt = new DesEncrypter(new String(com.crushftp.client.Common.encryption_password), false);
            s = crypt.decrypt(raw);
        }
        if (s == null) {
            s = Common.decode_pass3(raw);
        }
        return s;
    }

    public static String encode_pass3(String the_raw_password) {
        if (the_raw_password == null) {
            return "";
        }
        if (the_raw_password.equals("")) {
            return "";
        }
        String new_pass = "";
        int parse_loc = 0;
        while (parse_loc < the_raw_password.length()) {
            int the_char = the_raw_password.charAt(parse_loc);
            the_char += the_raw_password.length() + parse_loc + 13;
            while (the_char > 127) {
                the_char -= 106;
            }
            new_pass = String.valueOf(new_pass) + (char)the_char;
            ++parse_loc;
        }
        new_pass = Common.url_encode(new_pass);
        return new_pass;
    }

    public static String decode_pass3(String the_encoded_password) {
        if (the_encoded_password == null) {
            return "";
        }
        if (the_encoded_password.equals("")) {
            return "";
        }
        the_encoded_password = Common.replace_str(the_encoded_password, "%0D", "\r\n");
        the_encoded_password = Common.url_decode3(the_encoded_password);
        String new_pass = "";
        int parse_loc = 0;
        while (parse_loc < the_encoded_password.length()) {
            int the_char = the_encoded_password.charAt(parse_loc);
            the_char -= the_encoded_password.length() + parse_loc + 13;
            while (the_char < 22) {
                the_char += 106;
            }
            new_pass = String.valueOf(new_pass) + (char)the_char;
            ++parse_loc;
        }
        return new_pass;
    }

    public static boolean check_local_ip(String ip_check_str) throws Exception {
        if (local_ips == null) {
            local_ips = new Vector();
            Properties ip_item = new Properties();
            ip_item.put("type", "A");
            ip_item.put("start_ip", "192.168.0.0");
            ip_item.put("stop_ip", "192.168.255.255");
            local_ips.addElement(ip_item);
            ip_item = new Properties();
            ip_item.put("type", "A");
            ip_item.put("start_ip", "172.16.0.0");
            ip_item.put("stop_ip", "172.31.255.255");
            local_ips.addElement(ip_item);
            ip_item = new Properties();
            ip_item.put("type", "A");
            ip_item.put("start_ip", "10.0.0.0");
            ip_item.put("stop_ip", "10.255.255.255");
            local_ips.addElement(ip_item);
        }
        return Common.check_ip(local_ips, ip_check_str).equals("");
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static String check_ip(Vector allow_list, String ip_check_str) throws Exception {
        Properties last_item = null;
        boolean allow_all = allow_list == null || allow_list.size() == 0;
        try {
            int x = 0;
            while (x < allow_list.size()) {
                Properties ip_data;
                last_item = ip_data = (Properties)allow_list.elementAt(x);
                String reason = ip_data.getProperty("reason", "").trim();
                if (reason.equals("")) {
                    reason = "BANNED";
                }
                if ((String.valueOf(ip_data.getProperty("start_ip", "0.0.0.0")) + "1").charAt(0) < '0' || (String.valueOf(ip_data.getProperty("start_ip", "0.0.0.0")) + "1").charAt(0) > '9') {
                    ip_data = (Properties)ip_data.clone();
                    ip_data.put("start_ip", InetAddress.getByName(ip_data.getProperty("start_ip", "0.0.0.0")).getHostAddress());
                    ip_data.put("stop_ip", ip_data.getProperty("start_ip"));
                }
                if (ip_check_str.indexOf(".") >= 0) {
                    allow_all = ip_data.getProperty("type", "A").equals("A") && ip_data.getProperty("start_ip", "0.0.0.0").equals("0.0.0.0") && ip_data.getProperty("stop_ip", "255.255.255.255").equals("255.255.255.255") && allow_list.size() == 1;
                } else if (ip_check_str.indexOf(":") >= 0) {
                    boolean bl = allow_all = ip_data.getProperty("type", "A").equals("A") && Common.ipv6_num(ip_data.getProperty("start_ip", "::0")).equals(Common.ipv6_num("::0")) && Common.ipv6_num(ip_data.getProperty("stop_ip", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")).equals(Common.ipv6_num("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")) && allow_list.size() == 1;
                }
                if (ip_check_str.contains(".") && ip_data.getProperty("start_ip", "").indexOf(".") >= 0) {
                    Boolean is_ip_allowed = Common.is_ip_allowed(ip_check_str, ip_data);
                    if (is_ip_allowed != null) {
                        if (is_ip_allowed.booleanValue()) {
                            return "";
                        }
                        String string = reason;
                        return string;
                    }
                } else if (ip_check_str.contains(":")) {
                    String start_ip = ip_data.getProperty("start_ip");
                    String stop_ip = ip_data.getProperty("stop_ip");
                    if (start_ip.equals("0.0.0.0") && stop_ip.equals("255.255.255.255") && ip_data.getProperty("type").equals("A")) {
                        start_ip = "::0";
                        stop_ip = "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff";
                    }
                    if (start_ip.indexOf(":") >= 0 && Common.is_ipv6_in_range(ip_check_str, start_ip, stop_ip)) {
                        if (!ip_data.getProperty("type").equals("A")) return reason;
                        return "";
                    }
                }
                ++x;
            }
        }
        catch (Exception e) {
            if (("" + e).indexOf("Interrupted") >= 0) {
                throw e;
            }
            Log.log("SERVER", 2, "check_ip:" + ip_check_str + ":" + last_item);
            Log.log("SERVER", 2, e);
        }
        if (!allow_all) return "BLOCKED";
        return "";
    }

    public static boolean is_ipv6_in_range(String ipv6, String start_ipv6, String end_ipv6) {
        BigInteger ip6_num = Common.ipv6_num(ipv6);
        BigInteger start_ip_num = Common.ipv6_num(start_ipv6);
        BigInteger end_ip_num = Common.ipv6_num(end_ipv6);
        return start_ip_num.compareTo(ip6_num) <= 0 && end_ip_num.compareTo(ip6_num) >= 0;
    }

    public static BigInteger ipv6_num(String ipv6) {
        try {
            return new BigInteger(1, Inet6Address.getByName(ipv6).getAddress());
        }
        catch (UnknownHostException e) {
            Log.log("SERVER", 0, e);
            return new BigInteger("-1");
        }
    }

    public static Boolean is_ip_allowed(String ip_check_str, Properties ip_data) {
        long part3_end;
        long part2_end;
        long part1_end;
        long part3_start;
        long part2_start;
        long part1_start;
        long part4 = 0L;
        long part4_start = 0L;
        long part4_end = 0L;
        int part4_loc = 0;
        int part1_loc = ip_check_str.indexOf(".");
        int part2_loc = ip_check_str.indexOf(".", part1_loc + 1);
        int part3_loc = ip_check_str.indexOf(".", part2_loc + 1);
        long part1 = Long.parseLong(ip_check_str.substring(0, part1_loc));
        long part2 = Long.parseLong(ip_check_str.substring(part1_loc + 1, part2_loc));
        long part3 = Long.parseLong(ip_check_str.substring(part2_loc + 1, part3_loc));
        part4 = Long.parseLong(ip_check_str.substring(part3_loc + 1, ip_check_str.length()));
        String ip_str = ip_data.getProperty("start_ip");
        part1_loc = ip_str.indexOf(".");
        part2_loc = ip_str.indexOf(".", part1_loc + 1);
        part3_loc = ip_str.indexOf(".", part2_loc + 1);
        try {
            part1_start = Long.parseLong(ip_str.substring(0, part1_loc));
        }
        catch (Exception e) {
            part1_start = 0L;
        }
        try {
            part2_start = Long.parseLong(ip_str.substring(part1_loc + 1, part2_loc));
        }
        catch (Exception e) {
            part2_start = 0L;
        }
        try {
            part3_start = Long.parseLong(ip_str.substring(part2_loc + 1, part3_loc));
        }
        catch (Exception e) {
            part3_start = 0L;
        }
        try {
            part4_start = Long.parseLong(ip_str.substring(part3_loc + 1, ip_str.length()));
        }
        catch (Exception e) {
            part4_start = 0L;
        }
        ip_str = ip_data.getProperty("stop_ip");
        part1_loc = ip_str.indexOf(".");
        part2_loc = ip_str.indexOf(".", part1_loc + 1);
        part3_loc = ip_str.indexOf(".", part2_loc + 1);
        part4_loc = ip_str.length();
        try {
            part1_end = Long.parseLong(ip_str.substring(0, part1_loc));
        }
        catch (Exception e) {
            part1_end = 255L;
        }
        try {
            part2_end = Long.parseLong(ip_str.substring(part1_loc + 1, part2_loc));
        }
        catch (Exception e) {
            part2_end = 255L;
        }
        try {
            part3_end = Long.parseLong(ip_str.substring(part2_loc + 1, part3_loc));
        }
        catch (Exception e) {
            part3_end = 255L;
        }
        try {
            part4_end = Long.parseLong(ip_str.substring(part3_loc + 1, part4_loc));
        }
        catch (Exception e) {
            part4_end = 255L;
        }
        long the_ip = (part1 << 24) + (part2 << 16) + (part3 << 8) + part4;
        long the_ip_start = (part1_start << 24) + (part2_start << 16) + (part3_start << 8) + part4_start;
        long the_ip_end = (part1_end << 24) + (part2_end << 16) + (part3_end << 8) + part4_end;
        if (the_ip >= the_ip_start && the_ip <= the_ip_end) {
            return ip_data.getProperty("type", "A").equals("A") ? Boolean.TRUE : Boolean.FALSE;
        }
        return null;
    }

    public void remove_expired_bans(Vector ip_list) {
        Vector<Properties> temp = new Vector<Properties>();
        int x = ip_list.size() - 1;
        while (x >= 0) {
            long timer;
            Properties ip_data = (Properties)ip_list.elementAt(x);
            if (ip_data.getProperty("type").toUpperCase().equals("T") && (timer = Long.parseLong(ip_data.getProperty("timeout", "0"))) < new Date().getTime()) {
                ip_list.remove(ip_data);
                QuickConnect.ip_cache.clear();
            }
            if (ip_data.getProperty("type").toUpperCase().equals("D")) {
                temp.add(ip_data);
            }
            --x;
        }
        if (ServerStatus.IG("max_denied_ips") < temp.size()) {
            ip_list.removeAll(temp.subList(0, temp.size() - ServerStatus.IG("max_denied_ips")));
        }
    }

    public boolean check_date_expired_roll(String account_expire_field_str) {
        return this.check_date_expired(account_expire_field_str, System.currentTimeMillis());
    }

    public boolean check_date_expired(String expire_field_str, long time) {
        if (expire_field_str == null) {
            return false;
        }
        if (expire_field_str.equals("") || expire_field_str.equals("0") || expire_field_str.equals("account_expire")) {
            return false;
        }
        try {
            return this.get_expired_date_format(expire_field_str).parse(expire_field_str).getTime() < time;
        }
        catch (Exception e) {
            return true;
        }
    }

    public SimpleDateFormat get_expired_date_format(String expire_field_str) {
        SimpleDateFormat sdf = null;
        if (expire_field_str != null && expire_field_str.indexOf("/") >= 0 && expire_field_str.indexOf(":") >= 0) {
            if (expire_field_str.substring(expire_field_str.lastIndexOf("/") + 1, expire_field_str.indexOf(" ")).length() == 2 && expire_field_str.indexOf(":") == expire_field_str.lastIndexOf(":")) {
                sdf = new SimpleDateFormat("MM/dd/yy hh:mm aa", Locale.US);
            }
            if (expire_field_str.substring(expire_field_str.lastIndexOf("/") + 1, expire_field_str.indexOf(" ")).length() == 4 && expire_field_str.indexOf(":") == expire_field_str.lastIndexOf(":")) {
                sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm aa", Locale.US);
            }
            if (expire_field_str.substring(expire_field_str.lastIndexOf("/") + 1, expire_field_str.indexOf(" ")).length() == 4 && expire_field_str.indexOf(":") != expire_field_str.lastIndexOf(":")) {
                sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
            }
        } else {
            sdf = expire_field_str != null && expire_field_str.indexOf("/") >= 0 ? new SimpleDateFormat("MM/dd/yyyy", Locale.US) : new SimpleDateFormat("MMddyyyyHHmm", Locale.US);
        }
        return sdf;
    }

    public static boolean check_day_of_week(String allow_list, Date the_day) {
        String allow_date = "none";
        String today_date = new SimpleDateFormat("EEE", Locale.US).format(the_day).toUpperCase();
        if (today_date.equals("SUN")) {
            allow_date = "1";
        }
        if (today_date.equals("MON")) {
            allow_date = "2";
        }
        if (today_date.equals("TUE")) {
            allow_date = "3";
        }
        if (today_date.equals("WED")) {
            allow_date = "4";
        }
        if (today_date.equals("THU")) {
            allow_date = "5";
        }
        if (today_date.equals("FRI")) {
            allow_date = "6";
        }
        if (today_date.equals("SAT")) {
            allow_date = "7";
        }
        return allow_list.indexOf(allow_date) >= 0;
    }

    public static int check_protocol(String protocol, String allowed_protocols) {
        String[] aps = allowed_protocols.split(",");
        int x = 0;
        while (x < aps.length) {
            if (aps[x].split(":")[0].equalsIgnoreCase(protocol)) {
                String s = aps[x];
                s = s.split(":").length > 1 ? s.split(":")[1] : "0";
                if (s.equals("0")) {
                    s = "32768";
                }
                return Integer.parseInt(s);
            }
            ++x;
        }
        return -1;
    }

    public String format_message(String code_string, String the_message) {
        boolean use_old_method = false;
        if (code_string.endsWith("-")) {
            code_string = code_string.substring(0, code_string.length() - 1);
            use_old_method = true;
        }
        String original_message = the_message = the_message.trim();
        if (the_message.trim().lastIndexOf("\n") < 0) {
            return String.valueOf(code_string) + " " + the_message;
        }
        the_message = the_message.substring(0, the_message.trim().lastIndexOf("\n"));
        String return_string = "";
        if (the_message.length() > 0) {
            int temp_loc = 0;
            int temp_loc2 = 0;
            int skip_len = 1;
            while (temp_loc >= 0) {
                temp_loc = the_message.indexOf("\n");
                temp_loc2 = the_message.indexOf("\\n");
                if (temp_loc2 >= 0 && (temp_loc2 < temp_loc || temp_loc < 0)) {
                    temp_loc = temp_loc2;
                    skip_len = 2;
                } else {
                    skip_len = 1;
                }
                if (temp_loc < 0) continue;
                return_string = String.valueOf(return_string) + code_string + "-" + the_message.substring(0, temp_loc).trim() + CRLF;
                the_message = the_message.substring(temp_loc + skip_len, the_message.length()).trim();
            }
            return_string = String.valueOf(return_string) + code_string + "-" + the_message.substring(0, the_message.length()).trim() + CRLF;
        }
        return_string = use_old_method ? String.valueOf(return_string) + code_string + "-" + original_message.substring(original_message.trim().lastIndexOf("\n")).trim() + CRLF : String.valueOf(return_string) + code_string + " " + original_message.substring(original_message.trim().lastIndexOf("\n")).trim() + CRLF;
        return return_string;
    }

    public static boolean filter_check(String type, String the_dir, String filters) {
        boolean opposite;
        boolean opposite2;
        if (filters.equals("")) {
            return true;
        }
        if (type.equals("F") && the_dir.endsWith("/")) {
            return true;
        }
        if (type.equals("DIR") && !the_dir.endsWith("/")) {
            return true;
        }
        if (the_dir.endsWith("/")) {
            the_dir = the_dir.substring(0, the_dir.length() - 1);
        }
        if (the_dir.indexOf("/") >= 0) {
            the_dir = the_dir.substring(the_dir.lastIndexOf("/") + 1);
        }
        int parse_loc = filters.indexOf(":" + type + "C:");
        while (parse_loc >= 0) {
            String search_data = filters.substring(parse_loc + type.length() + 3, filters.indexOf(";", parse_loc));
            boolean bl = opposite2 = search_data.startsWith("!") && !search_data.trim().equals("!");
            if (opposite2) {
                search_data = search_data.substring(1);
            }
            if (the_dir.indexOf(search_data) >= 0 && !opposite2) {
                return false;
            }
            if (opposite2) {
                return the_dir.indexOf(search_data) >= 0;
            }
            parse_loc = filters.indexOf(":" + type + "C:", parse_loc + type.length() + 3);
        }
        parse_loc = filters.indexOf(":" + type + "S:");
        Properties opposite_cache = new Properties();
        while (parse_loc >= 0) {
            String search_data = filters.substring(parse_loc + type.length() + 3, filters.indexOf(";", parse_loc));
            boolean bl = opposite = search_data.startsWith("!") && !search_data.trim().equals("!");
            if (opposite) {
                if (!the_dir.startsWith(search_data = search_data.substring(1)) && !opposite_cache.getProperty("result", "false").equals("true")) {
                    opposite_cache.put("result", "false");
                } else {
                    opposite_cache.put("result", "true");
                }
            }
            if (the_dir.startsWith(search_data) && !opposite) {
                return false;
            }
            parse_loc = filters.indexOf(":" + type + "S:", parse_loc + type.length() + 3);
        }
        if (opposite_cache.containsKey("result") && opposite_cache.getProperty("result", "false").equals("false")) {
            return false;
        }
        opposite_cache.clear();
        parse_loc = filters.indexOf(":" + type + "E:");
        filters = filters.toUpperCase();
        the_dir = the_dir.toUpperCase();
        while (parse_loc >= 0) {
            String search_data = filters.substring(parse_loc + type.length() + 3, filters.indexOf(";", parse_loc));
            boolean bl = opposite = search_data.startsWith("!") && !search_data.trim().equals("!");
            if (opposite) {
                if (!the_dir.endsWith(search_data = search_data.substring(1)) && !opposite_cache.getProperty("result", "false").equals("true")) {
                    opposite_cache.put("result", "false");
                } else {
                    opposite_cache.put("result", "true");
                }
            }
            if (the_dir.endsWith(search_data) && !opposite) {
                return false;
            }
            parse_loc = filters.indexOf(":" + type + "E:", parse_loc + type.length() + 3);
        }
        if (opposite_cache.containsKey("result") && opposite_cache.getProperty("result", "false").equals("false")) {
            return false;
        }
        parse_loc = filters.indexOf(":" + type + "R:");
        opposite2 = false;
        while (parse_loc >= 0) {
            boolean simple;
            String search_data = filters.substring(parse_loc + type.length() + 3, filters.indexOf(";", parse_loc));
            boolean bl = opposite2 = search_data.startsWith("!") && !search_data.trim().equals("!");
            if (opposite2) {
                search_data = search_data.substring(1);
            }
            if (simple = search_data.startsWith("~")) {
                search_data = search_data.substring(1);
            }
            Pattern pattern = null;
            try {
                pattern = com.crushftp.client.Common.getPattern(search_data, true);
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
            if (simple && com.crushftp.client.Common.do_search(search_data, the_dir, false, 0)) {
                return opposite2;
            }
            if (pattern != null && pattern.matcher(the_dir).matches()) {
                return opposite2;
            }
            parse_loc = filters.indexOf(":" + type + "R:", parse_loc + type.length() + 3);
        }
        return !opposite2;
    }

    public String play_sound(String in_str) {
        while (in_str.indexOf("<SOUND>") >= 0) {
            String sound_file = in_str.substring(in_str.indexOf("<SOUND>") + "<SOUND>".length(), in_str.indexOf("</SOUND>"));
            new Sounds().loadSound(new File_S(sound_file));
            in_str = String.valueOf(in_str.substring(0, in_str.indexOf("<SOUND>"))) + in_str.substring(in_str.indexOf("</SOUND>") + "</SOUND>".length());
        }
        return in_str;
    }

    public static String space_encode(String in_str) {
        while (in_str.indexOf("<SPACE>") >= 0) {
            String data = in_str.substring(in_str.indexOf("<SPACE>") + "<SPACE>".length(), in_str.indexOf("</SPACE>"));
            data = Common.replace_str(data, " ", "%20");
            in_str = String.valueOf(in_str.substring(0, in_str.indexOf("<SPACE>"))) + data + in_str.substring(in_str.indexOf("</SPACE>") + "</SPACE>".length());
        }
        return in_str;
    }

    public static String url_encoder(String in_str) {
        while (in_str.indexOf("<URL>") >= 0) {
            String data = in_str.substring(in_str.indexOf("<URL>") + "<URL>".length(), in_str.indexOf("</URL>"));
            data = Common.url_encode(data);
            in_str = String.valueOf(in_str.substring(0, in_str.indexOf("<URL>"))) + data + in_str.substring(in_str.indexOf("</URL>") + "</URL>".length());
        }
        return in_str;
    }

    public static String reverse_ip(String in_str) {
        while (in_str.indexOf("<REVERSE_IP>") >= 0) {
            String data = in_str.substring(in_str.indexOf("<REVERSE_IP>") + "<REVERSE_IP>".length(), in_str.indexOf("</REVERSE_IP>"));
            try {
                data = InetAddress.getByName(data).getHostName();
            }
            catch (Exception exception) {
                // empty catch block
            }
            in_str = String.valueOf(in_str.substring(0, in_str.indexOf("<REVERSE_IP>"))) + data + in_str.substring(in_str.indexOf("</REVERSE_IP>") + "</REVERSE_IP>".length());
        }
        return in_str;
    }

    public static String cut(String item) {
        item = item.replace('\\', '/');
        item = item.substring(0, item.length() - 1);
        return item;
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

    public static void remove_osx_service() {
        if (Common.machine_is_x()) {
            try {
                new File("/System/Library/StartupItems/" + System.getProperty("appname", "CrushFTP") + "/Resources/English.lproj/Localizable.strings").delete();
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                new File("/System/Library/StartupItems/" + System.getProperty("appname", "CrushFTP") + "/StartupParameters.plist/").delete();
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                new File("/System/Library/StartupItems/" + System.getProperty("appname", "CrushFTP") + "/" + System.getProperty("appname", "CrushFTP")).delete();
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                new File("/System/Library/StartupItems/" + System.getProperty("appname", "CrushFTP") + "/Resources/English.lproj/").delete();
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                new File("/System/Library/StartupItems/" + System.getProperty("appname", "CrushFTP") + "/Resources/").delete();
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                new File("/System/Library/StartupItems/" + System.getProperty("appname", "CrushFTP") + "/").delete();
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                RandomAccessFile out = new RandomAccessFile(String.valueOf(System.getProperty("crushftp.prefs")) + System.getProperty("appname", "CrushFTP").toLowerCase() + "_exec_root.sh", "rw");
                out.setLength(0L);
                if (new File("/Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + ".plist").exists()) {
                    out.write(("launchctl stop com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "\n").getBytes("UTF8"));
                    out.write(("launchctl remove com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "\n").getBytes("UTF8"));
                }
                if (new File("/Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "Update.plist").exists()) {
                    out.write(("launchctl stop com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "Update\n").getBytes("UTF8"));
                    out.write(("launchctl remove com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "Update\n").getBytes("UTF8"));
                }
                out.close();
                File_S f = new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + System.getProperty("appname", "CrushFTP").toLowerCase() + "_exec_root.sh");
                Common.exec(new String[]{"chmod", "+x", f.getCanonicalPath()});
                Common.exec(new String[]{"osascript", "-e", "do shell script \"" + f.getCanonicalPath() + "\" with administrator privileges"});
                new File("/Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + ".plist").delete();
                new File("/Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "Update.plist").delete();
                f.delete();
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        }
    }

    public String install_osx_service() {
        if (Common.machine_is_x()) {
            try {
                String plist = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\r\n<plist version=\"1.0\">\r\n\t<dict>\r\n\t\t<key>Label</key>\r\n\t\t<string>com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "</string>\r\n" + "\t\t<key>ProgramArguments</key>\r\n" + "\t\t<array>\r\n" + "\t\t\t<string>" + new File_S(String.valueOf(System.getProperty("crushftp.home")) + System.getProperty("crushftp.executable")).getCanonicalPath() + "</string>\r\n\t\t\t<string>-d</string>\r\n" + "\t\t</array>\r\n" + "\t\t<key>RunAtLoad</key>\r\n" + "\t\t<true/>\r\n" + "\t</dict>\r\n" + "</plist>\r\n";
                RandomAccessFile plist_file = new RandomAccessFile(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + ".plist"), "rw");
                plist_file.setLength(0L);
                plist_file.write(plist.getBytes("UTF8"));
                plist_file.close();
                new File_S(String.valueOf(System.getProperty("crushftp.home")) + "OSX_scripts/").mkdirs();
                String plist2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\r\n<plist version=\"1.0\">\r\n\t<dict>\r\n\t\t<key>Label</key>\r\n\t\t<string>com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "Update</string>\r\n" + "\t\t<key>ProgramArguments</key>\r\n" + "\t\t<array>\r\n" + "\t\t\t<string>" + new File_S(System.getProperty("crushftp.home")).getCanonicalPath() + "/OSX_scripts/daemonUpdate.sh</string>\r\n" + "\t\t</array>\r\n" + "\t\t<key>RunAtLoad</key>\r\n" + "\t\t<false/>\r\n" + "\t</dict>\r\n" + "</plist>\r\n";
                plist_file = new RandomAccessFile(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "Update.plist"), "rw");
                plist_file.setLength(0L);
                plist_file.write(plist2.getBytes("UTF8"));
                plist_file.close();
                String path_to_crush = String.valueOf(new File_S(System.getProperty("crushftp.home")).getCanonicalPath()) + "/";
                String daemon = "#!/bin/sh\n";
                daemon = String.valueOf(daemon) + "echo " + System.getProperty("appname", "CrushFTP") + "Update starting...\n";
                daemon = String.valueOf(daemon) + "cd \"" + path_to_crush + "\"\n";
                daemon = String.valueOf(daemon) + "\"" + System.getProperty("java.home") + "/bin/java\" -cp plugins/lib/" + System.getProperty("appname", "CrushFTP") + "Restart.jar " + System.getProperty("appname", "CrushFTP") + "Restart\n";
                daemon = String.valueOf(daemon) + "echo " + System.getProperty("appname", "CrushFTP") + "Update stopped.\n";
                RandomAccessFile daemon_file = new RandomAccessFile(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "OSX_scripts/daemonUpdate.sh"), "rw");
                daemon_file.setLength(0L);
                daemon_file.write(daemon.getBytes("UTF8"));
                daemon_file.close();
                RandomAccessFile out = new RandomAccessFile(String.valueOf(System.getProperty("crushftp.prefs")) + System.getProperty("appname", "CrushFTP").toLowerCase() + "_exec_root.sh", "rw");
                out.setLength(0L);
                out.write(("mv \"" + new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + ".plist").getCanonicalPath() + "\" /Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + ".plist\n").getBytes("UTF8"));
                out.write(("mv \"" + new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "Update.plist").getCanonicalPath() + "\" /Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "Update.plist\n").getBytes("UTF8"));
                out.write(("chmod 755 \"" + new File_S(String.valueOf(path_to_crush) + "OSX_scripts/daemonUpdate.sh").getCanonicalPath() + "\"\n").getBytes("UTF8"));
                out.write(("chmod 704 /Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + ".plist\n").getBytes("UTF8"));
                out.write(("chown root /Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + ".plist\n").getBytes("UTF8"));
                out.write(("chgrp wheel /Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + ".plist\n").getBytes("UTF8"));
                out.write(("chmod 704 /Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "Update.plist\n").getBytes("UTF8"));
                out.write(("chown root /Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "Update.plist\n").getBytes("UTF8"));
                out.write(("chgrp wheel /Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "Update.plist\n").getBytes("UTF8"));
                out.write(("launchctl load -F -w /Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + ".plist\n").getBytes("UTF8"));
                out.write(("launchctl load -F -w /Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "Update.plist\n").getBytes("UTF8"));
                out.write(("launchctl start com." + System.getProperty("appname", "CrushFTP").toLowerCase() + "." + System.getProperty("appname", "CrushFTP") + "\n").getBytes("UTF8"));
                out.close();
                File_S f = new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + System.getProperty("appname", "CrushFTP").toLowerCase() + "_exec_root.sh");
                Common.exec(new String[]{"chmod", "+x", new File_S(String.valueOf(System.getProperty("crushftp.home")) + System.getProperty("crushftp.executable")).getCanonicalPath()});
                Common.exec(new String[]{"chmod", "+x", f.getCanonicalPath()});
                Common.exec(new String[]{"osascript", "-e", "do shell script \"" + f.getCanonicalPath() + "\" with administrator privileges"});
                f.delete();
                return "";
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                return e.toString();
            }
        }
        return "error: not OS X";
    }

    public static void checkForUpdate(Properties info) {
        try {
            String url = "https://www." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".com/version" + Common.V() + (info.getProperty("check_build", "false").equals("true") ? "_build" : "") + ".html";
            URLConnection urlc = new URL(url).openConnection();
            InputStream in = (InputStream)urlc.getContent();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            com.crushftp.client.Common.streamCopier(in, baos, false, true, true);
            String html = new String(baos.toByteArray(), "UTF8");
            String serverVersion = html.substring(0, html.indexOf("\r") > 0 ? html.indexOf("\r") : html.indexOf("\n")).trim();
            html = html.substring(html.indexOf("<")).trim();
            info.put("version", serverVersion);
            info.put("html", html);
        }
        catch (Exception e) {
            Log.log("SERVER", 1, e);
            Log.log("SERVER", 0, "Unable to check for update. " + e.toString());
        }
    }

    public static String decode64(String data) {
        byte[] stuff = null;
        try {
            stuff = Base64.decode(data);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            data = new String(stuff, "UTF8");
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            // empty catch block
        }
        return data;
    }

    public static String encode64(String data) {
        try {
            data = Base64.encodeBytes(data.getBytes("UTF8"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return data;
    }

    public ServerSocket getServerSocket(int serverPort, String listen_ip, String KEYSTORE, String keystorepass, String keypass, String disabled_ciphers, boolean needClientAuth, int backlog) throws Exception {
        return this.getServerSocket(serverPort, listen_ip, KEYSTORE, keystorepass, keypass, disabled_ciphers, needClientAuth, backlog, true, true, null);
    }

    public ServerSocket getServerSocket(int serverPort, String listen_ip, String KEYSTORE, String keystorepass, String keypass, String disabled_ciphers, boolean needClientAuth, int backlog, boolean allowBuiltIn, boolean bind_all, SSLContext ssl_context) throws Exception {
        SSLServerSocket serverSocket;
        SSLServerSocketFactory ssf;
        block10: {
            ssf = null;
            if (KEYSTORE.indexOf(";!!!keystore_used!!!;") > 0) {
                String[] data = KEYSTORE.split(";!!!keystore_used!!!;");
                KEYSTORE = data[0];
                ssf = this.getSSLContext(KEYSTORE, String.valueOf(KEYSTORE) + "_trust", keystorepass, keypass, keystorepass, keypass, "TLS", needClientAuth, true, allowBuiltIn, data[1]).getServerSocketFactory();
            } else {
                ssf = ssl_context != null ? ssl_context.getServerSocketFactory() : this.getSSLContext(KEYSTORE, String.valueOf(KEYSTORE) + "_trust", keystorepass, keypass, "TLS", needClientAuth, true, allowBuiltIn).getServerSocketFactory();
            }
            serverSocket = null;
            try {
                if (listen_ip != null) {
                    if (listen_ip.equalsIgnoreCase("lookup")) {
                        listen_ip = "0.0.0.0";
                    }
                    boolean all_numbers = true;
                    int x = 0;
                    while (x < listen_ip.length()) {
                        if (listen_ip.charAt(x) >= ':') {
                            all_numbers = false;
                        }
                        ++x;
                    }
                    if (!all_numbers || !bind_all) {
                        serverSocket = (SSLServerSocket)ssf.createServerSocket(serverPort, backlog, InetAddress.getByName(listen_ip));
                    }
                }
            }
            catch (SocketException e) {
                Log.log("SERVER", 2, e);
                if (!(e instanceof BindException)) break block10;
                throw e;
            }
        }
        if (serverSocket == null) {
            serverSocket = (SSLServerSocket)ssf.createServerSocket(serverPort, backlog);
        }
        Common.setEnabledCiphers(disabled_ciphers, null, serverSocket);
        serverSocket.setNeedClientAuth(needClientAuth);
        Common.configureSSLTLSSocket(serverSocket);
        return serverSocket;
    }

    public static void configureSSLTLSSocket(Object sock) {
        com.crushftp.client.Common.configureSSLTLSSocket(sock, ServerStatus.SG("tls_version"));
    }

    public static void setEnabledCiphers(String disabled_ciphers, SSLSocket sock, SSLServerSocket serverSock) {
        Common.setEnabledCiphers(disabled_ciphers, sock, serverSock, null);
    }

    public static String[] setEnabledCiphers(String disabled_ciphers, SSLSocket sock, SSLServerSocket serverSock, SSLSocketFactory factory) {
        String[] ciphers = null;
        if (disabled_ciphers.equals("")) {
            if (sock != null) {
                ciphers = sock.getSupportedCipherSuites();
            }
            if (serverSock != null) {
                ciphers = serverSock.getSupportedCipherSuites();
            }
            if (factory != null) {
                ciphers = factory.getSupportedCipherSuites();
            }
        } else {
            disabled_ciphers = disabled_ciphers.toUpperCase();
            Vector<String> enabled_ciphers = new Vector<String>();
            if (sock != null) {
                ciphers = sock.getSupportedCipherSuites();
            }
            if (serverSock != null) {
                ciphers = serverSock.getSupportedCipherSuites();
            }
            if (factory != null) {
                ciphers = factory.getSupportedCipherSuites();
            }
            int x = 0;
            while (x < ciphers.length) {
                if (disabled_ciphers.indexOf("(" + ciphers[x].toUpperCase() + ")") < 0 && ciphers[x].toUpperCase().indexOf("EXPORT") < 0) {
                    enabled_ciphers.addElement(ciphers[x]);
                }
                ++x;
            }
            try {
                SSLParameters sslp = null;
                if (sock != null) {
                    sslp = sock.getSSLParameters();
                }
                if (serverSock != null) {
                    Method getSSLParameters = SSLServerSocket.class.getDeclaredMethod("getSSLParameters", null);
                    sslp = (SSLParameters)getSSLParameters.invoke(serverSock, null);
                }
                Method setUseCipherSuitesOrder = SSLParameters.class.getDeclaredMethod("setUseCipherSuitesOrder", Boolean.TYPE);
                setUseCipherSuitesOrder.invoke(sslp, new Boolean(true));
                Vector<String> enabled_ciphers2 = new Vector<String>();
                int x2 = 1;
                while (x2 < 100) {
                    int pos = disabled_ciphers.indexOf(String.valueOf(x2) + ";");
                    if (pos >= 0) {
                        String cipher = disabled_ciphers.substring(pos, disabled_ciphers.indexOf(")", pos));
                        if (enabled_ciphers.indexOf(cipher = cipher.substring(cipher.indexOf(";") + 1).trim()) >= 0) {
                            enabled_ciphers2.addElement(cipher);
                        }
                    }
                    ++x2;
                }
                x2 = 0;
                while (x2 < enabled_ciphers.size()) {
                    if (enabled_ciphers2.indexOf(enabled_ciphers.elementAt(x2).toString()) < 0) {
                        enabled_ciphers2.addElement(enabled_ciphers.elementAt(x2).toString());
                    }
                    ++x2;
                }
                enabled_ciphers = enabled_ciphers2;
            }
            catch (Exception e) {
                Log.log("SERVER", 2, e);
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
        return ciphers;
    }

    public SSLContext getSSLContext(String KEYSTORE, String TRUSTSTORE, String keystorepass, String keypass, String secureType, boolean needClientAuth, boolean acceptAnyCert) throws Exception {
        return this.getSSLContext(KEYSTORE, TRUSTSTORE, keystorepass, keypass, keystorepass, keypass, secureType, needClientAuth, acceptAnyCert, true);
    }

    public SSLContext getSSLContext(String KEYSTORE, String TRUSTSTORE, String keystorepass, String keypass, String secureType, boolean needClientAuth, boolean acceptAnyCert, boolean allowBuiltIn) throws Exception {
        return this.getSSLContext(KEYSTORE, TRUSTSTORE, keystorepass, keypass, keystorepass, keypass, secureType, needClientAuth, acceptAnyCert, allowBuiltIn);
    }

    public SSLContext getSSLContext(String KEYSTORE, String TRUSTSTORE, String keystorepass, String keypass, String truststorepass, String trustpass, String secureType, boolean needClientAuth, boolean acceptAnyCert, boolean allowBuiltIn) throws Exception {
        return this.getSSLContext(KEYSTORE, TRUSTSTORE, keystorepass, keypass, truststorepass, trustpass, secureType, needClientAuth, acceptAnyCert, allowBuiltIn, null);
    }

    public SSLContext getSSLContext(String KEYSTORE, String TRUSTSTORE, String keystorepass, String keypass, String truststorepass, String trustpass, String secureType, boolean needClientAuth, boolean acceptAnyCert, boolean allowBuiltIn, final String alias) throws Exception {
        KeyStore truststore;
        KeyStore keystore;
        block73: {
            if (TRUSTSTORE == null) {
                TRUSTSTORE = KEYSTORE;
            }
            String className = System.getProperty("crushftp.sslprovider", "");
            try {
                if (!providerAdded && !className.equals("")) {
                    Log.log("SERVER", 0, "Adding SSL provider:" + className);
                    Provider provider = (Provider)ServerStatus.clasLoader.loadClass(className).newInstance();
                    Security.addProvider(provider);
                    providerAdded = true;
                }
            }
            catch (Exception e) {
                throw new Exception("Failed loading security provider " + className, e);
            }
            if (ServerStatus.BG("fips140") && !fips140) {
                try {
                    Class<?> c = ServerStatus.clasLoader.loadClass("com.sun.net.ssl.internal.ssl.Provider");
                    Constructor<?> cons = c.getConstructor(String.class);
                    cons.newInstance("SunPKCS11-NSS");
                }
                catch (Exception e) {
                    Security.getProvider("SunPKCS11-NSS");
                    Security.getProvider("SunPKCS11-NSS-FIPS");
                }
                fips140 = true;
            }
            keystore = null;
            truststore = null;
            String keystoreFormat = "JKS";
            if (KEYSTORE.toUpperCase().indexOf("PKCS12") >= 0 || KEYSTORE.toUpperCase().indexOf("PFX") >= 0 || KEYSTORE.toUpperCase().indexOf("P12") >= 0) {
                keystoreFormat = "pkcs12";
            }
            if (KEYSTORE.equalsIgnoreCase("PKCS11") && fips140) {
                keystoreFormat = "PKCS11";
                keystore = KeyStore.getInstance(keystoreFormat);
                keystore.load(null, this.decode_pass(keystorepass).toCharArray());
                truststore = KeyStore.getInstance(keystoreFormat);
                acceptAnyCert = false;
            }
            if (keystore == null) {
                GenericClient c;
                VRL vrl;
                InputStream in;
                Properties p;
                block70: {
                    keystore = KeyStore.getInstance(keystoreFormat);
                    if (KEYSTORE.equals("builtin")) {
                        keystore.load(this.getClass().getResource("/assets/builtin").openStream(), "crushftp".toCharArray());
                    } else if (com.crushftp.client.Common.System2.containsKey("crushftp.keystores." + KEYSTORE.toUpperCase().replace('\\', '/'))) {
                        p = (Properties)com.crushftp.client.Common.System2.get("crushftp.keystores." + KEYSTORE.toUpperCase().replace('\\', '/'));
                        keystore.load(new ByteArrayInputStream((byte[])p.get("bytes")), this.decode_pass(keystorepass).toCharArray());
                    } else if (ServerStatus.BG("v10_beta") && !KEYSTORE.toString().equals("") && (KEYSTORE.toUpperCase().startsWith("FILE://") || !new VRL(KEYSTORE).getProtocol().equalsIgnoreCase("FILE"))) {
                        in = null;
                        vrl = new VRL(KEYSTORE);
                        try {
                            try {
                                c = Common.getClient(Common.getBaseUrl(vrl.toString()), "SSL Key store load", new Vector());
                                if (vrl.getConfig() != null && vrl.getConfig().size() > 0) {
                                    c.setConfigObj(vrl.getConfig());
                                }
                                c.login(vrl.getUsername(), vrl.getPassword(), null);
                                in = c.download(vrl.getPath(), 0L, -1L, true, true);
                                keystore.load(in, this.decode_pass(keystorepass).toCharArray());
                            }
                            catch (Exception e) {
                                if (!allowBuiltIn) {
                                    throw e;
                                }
                                Log.log("SERVER", 0, "Couldn't load keystore " + vrl.getPath() + ", ignoring it.");
                                Log.log("SERVER", 0, e);
                                keystore.load(this.getClass().getResource("/assets/builtin").openStream(), "crushftp".toCharArray());
                                KEYSTORE = "builtin";
                                if (in != null) {
                                    in.close();
                                }
                                break block70;
                            }
                        }
                        catch (Throwable throwable) {
                            if (in != null) {
                                in.close();
                            }
                            throw throwable;
                        }
                        if (in != null) {
                            in.close();
                        }
                    } else {
                        in = null;
                        try {
                            try {
                                in = new FileInputStream(new File_S(KEYSTORE));
                                keystore.load(in, this.decode_pass(keystorepass).toCharArray());
                            }
                            catch (Exception e) {
                                if (!allowBuiltIn) {
                                    throw e;
                                }
                                Log.log("SERVER", 0, "Couldn't load keystore " + KEYSTORE + ", ignoring it.");
                                Log.log("SERVER", 0, e);
                                keystore.load(this.getClass().getResource("/assets/builtin").openStream(), "crushftp".toCharArray());
                                KEYSTORE = "builtin";
                                if (in != null) {
                                    in.close();
                                }
                                break block70;
                            }
                        }
                        catch (Throwable e) {
                            if (in != null) {
                                in.close();
                            }
                            throw e;
                        }
                        if (in != null) {
                            in.close();
                        }
                    }
                }
                if (truststore == null) {
                    truststore = KeyStore.getInstance(keystoreFormat);
                }
                if (KEYSTORE.equals("builtin")) {
                    truststore.load(this.getClass().getResource("/assets/builtin").openStream(), "crushftp".toCharArray());
                } else if (needClientAuth) {
                    if (com.crushftp.client.Common.System2.containsKey("crushftp.keystores." + TRUSTSTORE.toUpperCase().replace('\\', '/'))) {
                        p = (Properties)com.crushftp.client.Common.System2.get("crushftp.keystores." + TRUSTSTORE.toUpperCase().replace('\\', '/'));
                        truststore.load(new ByteArrayInputStream((byte[])p.get("bytes")), this.decode_pass(keystorepass).toCharArray());
                    } else if (ServerStatus.BG("v10_beta") && !KEYSTORE.toString().equals("") && (KEYSTORE.toUpperCase().startsWith("FILE://") || !new VRL(KEYSTORE).getProtocol().equalsIgnoreCase("FILE"))) {
                        in = null;
                        vrl = new VRL(KEYSTORE);
                        try {
                            try {
                                c = Common.getClient(Common.getBaseUrl(vrl.toString()), "SSL Key store load", new Vector());
                                if (vrl.getConfig() != null && vrl.getConfig().size() > 0) {
                                    c.setConfigObj(vrl.getConfig());
                                }
                                c.login(vrl.getUsername(), vrl.getPassword(), null);
                                in = c.download(vrl.getPath(), 0L, -1L, true, true);
                                truststore.load(in, this.decode_pass(keystorepass).toCharArray());
                            }
                            catch (Exception e) {
                                if (!allowBuiltIn) {
                                    throw e;
                                }
                                Log.log("SERVER", 0, "Couldn't load keystore " + vrl.getPath() + ", ignoring it.");
                                Log.log("SERVER", 0, e);
                                truststore.load(this.getClass().getResource("/assets/builtin").openStream(), "crushftp".toCharArray());
                                TRUSTSTORE = "builtin";
                                if (in != null) {
                                    in.close();
                                }
                                break block73;
                            }
                        }
                        catch (Throwable throwable) {
                            if (in != null) {
                                in.close();
                            }
                            throw throwable;
                        }
                        if (in != null) {
                            in.close();
                        }
                    } else {
                        in = null;
                        try {
                            if (!new File_S(TRUSTSTORE).exists()) {
                                Log.log("SERVER", 0, "Couldn't find truststore " + TRUSTSTORE + ", ignoring it.");
                                truststore.load(this.getClass().getResource("/assets/builtin").openStream(), "crushftp".toCharArray());
                                TRUSTSTORE = "builtin";
                            } else {
                                in = new FileInputStream(new File_S(TRUSTSTORE));
                                truststore.load(in, this.decode_pass(truststorepass).toCharArray());
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
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        if (KEYSTORE.equals("builtin")) {
            kmf.init(keystore, "crushftp".toCharArray());
            if (needClientAuth) {
                kmf.init(truststore, "crushftp".toCharArray());
            }
        } else {
            try {
                kmf.init(keystore, this.decode_pass(keypass).toCharArray());
            }
            catch (Exception e) {
                kmf.init(keystore, this.decode_pass(keystorepass).toCharArray());
            }
        }
        final X509KeyManager real_km = (X509KeyManager)kmf.getKeyManagers()[0];
        X509KeyManager km = new X509KeyManager(){

            @Override
            public String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2) {
                return real_km.chooseClientAlias(arg0, arg1, arg2);
            }

            @Override
            public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2) {
                return alias != null ? alias : real_km.chooseServerAlias(arg0, arg1, arg2);
            }

            @Override
            public X509Certificate[] getCertificateChain(String arg0) {
                return real_km.getCertificateChain(arg0);
            }

            @Override
            public String[] getClientAliases(String arg0, Principal[] arg1) {
                return real_km.getClientAliases(arg0, arg1);
            }

            @Override
            public PrivateKey getPrivateKey(String arg0) {
                return real_km.getPrivateKey(arg0);
            }

            @Override
            public String[] getServerAliases(String arg0, Principal[] arg1) {
                return real_km.getServerAliases(arg0, arg1);
            }
        };
        KeyManager[] kms = new KeyManager[]{km};
        TrustManager[] trustAllCerts = new TrustManager[]{new TrustManagerCustom(null, true, true)};
        SSLContext sslc = null;
        sslc = secureType.trim().equalsIgnoreCase("TLS") ? SSLContext.getInstance("TLS") : SSLContext.getInstance(secureType);
        if (needClientAuth) {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(truststore);
            if (ServerStatus.BG("trust_expired_client_cert")) {
                sslc.init(kms, new TrustManager[]{new TrustManagerCustom((X509TrustManager)tmf.getTrustManagers()[0], false, true)}, new SecureRandom());
            } else {
                sslc.init(kms, tmf.getTrustManagers(), new SecureRandom());
            }
        } else if (acceptAnyCert) {
            sslc.init(kms, trustAllCerts, new SecureRandom());
        } else {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keystore);
            if (fips140) {
                sslc.init(kms, tmf.getTrustManagers(), SecureRandom.getInstance("PKCS11"));
            } else {
                sslc.init(kms, tmf.getTrustManagers(), new SecureRandom());
            }
        }
        return sslc;
    }

    public KeyPair getPrivateKey(KeyStore keystore, String alias, char[] password) {
        try {
            Key key = keystore.getKey(alias, password);
            if (key instanceof PrivateKey) {
                Certificate cert = keystore.getCertificate(alias);
                PublicKey publicKey = cert.getPublicKey();
                return new KeyPair(publicKey, (PrivateKey)key);
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 1, e);
        }
        return null;
    }

    public static String makeBoundary() {
        return Common.makeBoundary(11);
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

    public static String makeBoundarySimple() {
        return Common.makeBoundarySimple(11);
    }

    public static String makeBoundarySimple(int len) {
        String chars = "2346789abcdefghjkmnpqrtuvwxyzABCDEFGHJKMNPQRTUVWXYZ";
        String rand = "";
        int i = 0;
        while (i < len) {
            rand = String.valueOf(rand) + chars.charAt(rn.nextInt(chars.length()));
            ++i;
        }
        return rand;
    }

    public static String makeBoundaryNumeric(int len) {
        String chars = "123467890";
        String rand = "";
        int i = 0;
        while (i < len) {
            rand = String.valueOf(rand) + chars.charAt(rn.nextInt(chars.length()));
            ++i;
        }
        return rand;
    }

    public void writeAdminUser(String curUser, String password, String serverGroup, boolean localhostOnly) {
        try {
            Properties user_prop = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_master.getBytes("UTF8"))).getRootElement());
            user_prop.put("site", "(CONNECT)(WEB_ADMIN)");
            user_prop.put("ignore_max_logins", "true");
            user_prop.put("max_logins_ip", "8");
            user_prop.put("max_idle_time", "0");
            if (!(password.startsWith("MD5:") || password.startsWith("SHA:") || password.startsWith("DES:") || password.startsWith("SHA3:") || password.startsWith("SHA256:") || password.startsWith("SHA512:") || password.startsWith("ARGOND:"))) {
                password = this.encode_pass(password, "DES", "");
            }
            if (password.startsWith("DES:")) {
                password = password.substring(password.indexOf(":") + 1).trim();
            }
            user_prop.put("password", password.trim());
            if (localhostOnly) {
                Vector ips = (Vector)user_prop.get("ip_restrictions");
                ips.removeAllElements();
                Properties p = new Properties();
                p.put("start_ip", "127.0.0.1");
                p.put("stop_ip", "127.0.0.1");
                p.put("type", "A");
                ips.addElement(p);
            }
            UserTools.writeUser(serverGroup, curUser, user_prop);
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
            System.out.println(new Date());
            e.printStackTrace();
        }
    }

    public void writeNewUser(String curUser, String password, String root_dir, String permissions, String templateUser, String notes, String email, String listen_ip_port) {
        String pathOut = String.valueOf(System.getProperties().getProperty("crushftp.users")) + listen_ip_port + "/";
        try {
            Properties user_prop = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_master.getBytes("UTF8"))).getRootElement());
            Vector user_vfs = (Vector)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS.getBytes("UTF8"))).getRootElement());
            Properties user_vfs_item = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS_ITEM.getBytes("UTF8"))).getRootElement());
            if (!templateUser.trim().equals("")) {
                try {
                    System.out.println("Loading template user " + templateUser + ".");
                    Properties p = UserTools.ut.getUser(listen_ip_port, templateUser, false);
                    if (p != null) {
                        Enumeration<Object> keys = p.keys();
                        while (keys.hasMoreElements()) {
                            String key = keys.nextElement().toString();
                            try {
                                user_prop.put(key, p.get(key));
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                    } else {
                        System.out.println("Template user " + templateUser + " not found.");
                    }
                }
                catch (Exception e) {
                    System.out.println(new Date());
                    e.printStackTrace();
                }
            }
            new File_S(String.valueOf(pathOut) + curUser).mkdirs();
            new File_S(String.valueOf(pathOut) + curUser + "/VFS/").mkdirs();
            String url = new File_S(root_dir).toURI().toURL().toExternalForm();
            if (!url.endsWith("/")) {
                url = String.valueOf(url) + "/";
            }
            ((Properties)user_vfs.elementAt(0)).put("url", url);
            if (!(password.startsWith("MD5:") || password.startsWith("SHA:") || password.startsWith("DES:") || password.startsWith("SHA3:") || password.startsWith("SHA256:") || password.startsWith("SHA512:") || password.startsWith("ARGOND:"))) {
                password = this.encode_pass(password, "DES", "");
            }
            if (password.startsWith("DES:")) {
                password = password.substring(password.indexOf(":") + 1).trim();
            }
            user_prop.put("password", password);
            if (notes != null) {
                user_prop.put("notes", notes);
            }
            if (email != null) {
                user_prop.put("email", email);
            }
            user_vfs_item.put("/" + new File_S(root_dir).getName().toUpperCase() + "/", permissions);
            new File_S(String.valueOf(pathOut) + curUser).mkdirs();
            Common.updateOSXInfo(String.valueOf(pathOut) + curUser);
            new File_S(String.valueOf(pathOut) + curUser + "/VFS/").mkdirs();
            Common.updateOSXInfo(String.valueOf(pathOut) + curUser + "/VFS/");
            Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/user.XML", (Object)user_prop, "userfile");
            Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS/" + new File_S(root_dir).getName(), (Object)user_vfs, "VFS");
            Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS.XML", (Object)user_vfs_item, "VFS");
        }
        catch (Exception e) {
            System.out.println(new Date());
            e.printStackTrace();
        }
    }

    public void ConvertOSXUsers(String user_path) throws Exception {
        this.ConvertFolderUsers("/Users/", user_path);
    }

    public void ConvertFolderUsers(String dir, String user_path) throws Exception {
        if (!dir.endsWith("/")) {
            dir = String.valueOf(dir) + "/";
        }
        String pathOut = String.valueOf(System.getProperties().getProperty("crushftp.users")) + user_path;
        String[] dirList = new File_U(dir).list();
        int x = 0;
        while (x < dirList.length) {
            String curUser = dirList[x];
            Properties user_prop = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_master.getBytes("UTF8"))).getRootElement());
            Vector user_vfs = (Vector)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS.getBytes("UTF8"))).getRootElement());
            Properties user_vfs_item = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS_ITEM.getBytes("UTF8"))).getRootElement());
            if (!curUser.equals("Shared") && new File_U(String.valueOf(dir) + curUser).isDirectory()) {
                ((Properties)user_vfs.elementAt(0)).put("url", new File_U(String.valueOf(dir) + curUser).toURI().toURL().toExternalForm());
                user_vfs_item.put("/" + curUser + "/", "(read)(write)(view)(delete)(resume)(rename)(makedir)(deletedir)");
                new File_S(String.valueOf(pathOut) + curUser).mkdirs();
                Common.updateOSXInfo(String.valueOf(pathOut) + curUser);
                new File_S(String.valueOf(pathOut) + curUser + "/VFS/").mkdirs();
                Common.updateOSXInfo(String.valueOf(pathOut) + curUser + "/VFS/");
                Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/user.XML", (Object)user_prop, "userfile");
                Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS/" + curUser, (Object)user_vfs, "VFS");
                Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS.XML", (Object)user_vfs_item, "VFS");
            }
            ++x;
        }
    }

    public void ConvertCrushFTP3Users(String dir, String user_path, String encryption_mode, String parentUser) throws Exception {
        if (!dir.endsWith("/")) {
            dir = String.valueOf(dir) + "/";
        }
        String pathOut = String.valueOf(System.getProperties().getProperty("crushftp.users")) + user_path;
        String[] dirList = new File_S(dir).list();
        int x = 0;
        while (x < dirList.length) {
            String item = dirList[x];
            String parentTemp = new File_S(String.valueOf(dir) + item).getParentFile().getName();
            if (parentTemp.toUpperCase().startsWith("USERS_")) {
                parentTemp = "";
            }
            if (new File_S(String.valueOf(dir) + item).isDirectory()) {
                this.ConvertCrushFTP3Users(String.valueOf(dir) + item, user_path, encryption_mode, parentTemp);
            }
            if (item.toUpperCase().equals("0.XML")) {
                String curUser = new File_S(dir).getName();
                String user_str = "";
                RandomAccessFile in = new RandomAccessFile(new File_S(String.valueOf(dir) + item), "r");
                byte[] b = new byte[(int)in.length()];
                in.readFully(b);
                in.close();
                user_str = new String(b, "UTF8");
                user_str = Common.replace_str(user_str, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>\r\n<userfile>", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><userfile type=\"properties\">");
                user_str = Common.replace_str(user_str, "<userfile_setting>", "");
                user_str = Common.replace_str(user_str, "</userfile_setting>", "");
                user_str = Common.replace_str(user_str, "\t\t<ip_restrictions>\r\n\t\t</ip_restrictions>\r\n", "");
                user_str = Common.replace_str(user_str, "VERSION_S:1", "(SITE_VERSION)");
                user_str = Common.replace_str(user_str, "USERS_S:1", "(SITE_USERS)");
                user_str = Common.replace_str(user_str, "KICK_S:1", "(SITE_KICK)");
                user_str = Common.replace_str(user_str, "KICKBAN_S:1", "(SITE_KICKBAN)");
                user_str = Common.replace_str(user_str, "PASS_S:1", "(SITE_PASS)");
                user_str = Common.replace_str(user_str, "ZIP_S:1", "(SITE_ZIP)");
                user_str = Common.replace_str(user_str, "HIDE_S:1", "");
                user_str = Common.replace_str(user_str, "QUIT_S:1", "(SITE_QUIT)");
                user_str = Common.replace_str(user_str, "IRC_OPIRC_SEARCHIRC_INVITE", "");
                user_str = Common.replace_str(user_str, "SEARCH", "");
                user_str = Common.replace_str(user_str, "*connect*", "(CONNECT)(WEB_ADMIN)");
                user_str = Common.replace_str(user_str, "*user_activity*", "(USER_ACTIVITY)");
                user_str = Common.replace_str(user_str, "*kick_user*", "(KICK_USER)(PASSIVE_KICK_USER)");
                user_str = Common.replace_str(user_str, "*passive_kick_user*", "");
                user_str = Common.replace_str(user_str, "*ban_user*", "(BAN_USER)");
                user_str = Common.replace_str(user_str, "*add_log*", "(ADD_LOG)");
                user_str = Common.replace_str(user_str, "*server_settings*", "(SERVER_SETTINGS)(SERVER_SETTINGS_WRITE)");
                user_str = Common.replace_str(user_str, "*get_user*", "(GET_USER_LIST_AND_INHERITANCE)(GET_USER)");
                user_str = Common.replace_str(user_str, "*get_user_list_and_inheritance*", "");
                user_str = Common.replace_str(user_str, "*write_user*", "(WRITE_USER)(WRITE_INHERITANCE)(GET_LOCAL_LISTING)(GET_REAL_LOCAL_LISTING)");
                user_str = Common.replace_str(user_str, "*delete_user_parts*", "(DELETE_USER)");
                user_str = Common.replace_str(user_str, "*change_name*", "(CHANGE_NAME)");
                user_str = Common.replace_str(user_str, "*modify_mirrors*", "");
                user_str = Common.replace_str(user_str, "*get_reports*", "(GET_REPORTS)");
                user_str = Common.replace_str(user_str, "*pause_user*", "(PAUSE_USER)");
                user_str = Common.replace_str(user_str, "*start_server*", "(START_SERVER)(STOP_SERVER)(STOP_SERVER_KICK)");
                user_str = Common.replace_str(user_str, "*stop_server*", "");
                user_str = Common.replace_str(user_str, "*stop_server_kick*", "");
                user_str = Common.replace_str(user_str, "*user_admin_connect*", "(USER_ADMIN_CONNECT)");
                user_str = Common.replace_str(user_str, "<dirs>", "<dirs_subitem type=\"properties\">");
                user_str = Common.replace_str(user_str, "</dirs>", "</dirs_subitem>");
                user_str = Common.replace_str(user_str, "<more_items></more_items>", "");
                user_str = Common.replace_str(user_str, "<email_event>", "<events_subitem type=\"properties\">");
                user_str = Common.replace_str(user_str, "</email_event>", "</events_subitem>");
                try {
                    user_str = String.valueOf(user_str.substring(0, user_str.indexOf("<events_subitem type=\"properties\">"))) + "<events type=\"vector\">" + user_str.substring(user_str.indexOf("<events_subitem type=\"properties\">"));
                    user_str = String.valueOf(user_str.substring(0, user_str.lastIndexOf("</events_subitem>") + "</events_subitem>".length())) + "</events>" + user_str.substring(user_str.lastIndexOf("</events_subitem>") + "</events_subitem>".length());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                user_str = Common.replace_str(user_str, "<ip_restrictions>", "<ip_restrictions_subitem type=\"properties\">");
                user_str = Common.replace_str(user_str, "</ip_restrictions>", "</ip_restrictions_subitem>");
                try {
                    user_str = String.valueOf(user_str.substring(0, user_str.indexOf("<ip_restrictions_subitem type=\"properties\">"))) + "<ip_restrictions type=\"vector\">" + user_str.substring(user_str.indexOf("<ip_restrictions_subitem type=\"properties\">"));
                    user_str = String.valueOf(user_str.substring(0, user_str.lastIndexOf("</ip_restrictions_subitem>") + "</ip_restrictions_subitem>".length())) + "</ip_restrictions>" + user_str.substring(user_str.lastIndexOf("</ip_restrictions_subitem>") + "</ip_restrictions_subitem>".length());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    user_str = String.valueOf(user_str.substring(0, user_str.indexOf("<dirs_subitem type=\"properties\">"))) + "<dirs type=\"vector\">" + user_str.substring(user_str.indexOf("<dirs_subitem type=\"properties\">"));
                    user_str = String.valueOf(user_str.substring(0, user_str.lastIndexOf("</dirs_subitem>") + "</dirs_subitem>".length())) + "</dirs>" + user_str.substring(user_str.lastIndexOf("</dirs_subitem>") + "</dirs_subitem>".length());
                    while (user_str.indexOf("<more_items>") >= 0) {
                        user_str = String.valueOf(user_str.substring(0, user_str.indexOf("<more_items>"))) + user_str.substring(user_str.indexOf("</more_items>") + "</more_items>".length());
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                String password = user_str.substring(user_str.indexOf("<password>") + "<password>".length(), user_str.indexOf("</password>"));
                user_str = String.valueOf(user_str.substring(0, user_str.indexOf("<password>"))) + user_str.substring(user_str.indexOf("</password>") + "</password>".length());
                user_str = Common.replace_str(user_str, "&", "%26");
                user_str = Common.replace_str(user_str, "%20", " ");
                user_str = Common.replace_str(user_str, "%%", "%");
                user_str = Common.replace_str(user_str, "%3C", "&lt;");
                user_str = Common.replace_str(user_str, "%3E", "&gt;");
                user_str = Common.replace_str(user_str, "%2F", "/");
                Properties user_prop = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(user_str.getBytes("UTF8"))).getRootElement());
                Properties user_vfs_item = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS_ITEM.getBytes("UTF8"))).getRootElement());
                user_prop.put("password", this.encode_pass(Common.decode_pass3(password), encryption_mode, ""));
                user_prop.put("root_dir", "/");
                try {
                    if (user_prop.get("events") == null) {
                        user_prop.put("events", new Vector());
                    }
                    Vector events = (Vector)user_prop.get("events");
                    int xx = 0;
                    while (xx < events.size()) {
                        Properties p = (Properties)events.elementAt(xx);
                        String s = p.getProperty("email_command_data", "");
                        p.remove("email_command_data");
                        if (s.equalsIgnoreCase("QUIT")) {
                            p.put("event_user_action_list", "(disconnect)");
                        }
                        if (s.equalsIgnoreCase("STOR")) {
                            p.put("event_user_action_list", "(upload)");
                        }
                        if (s.equalsIgnoreCase("RETR")) {
                            p.put("event_user_action_list", "(download)");
                        }
                        if (s.equalsIgnoreCase("PASS")) {
                            p.put("event_user_action_list", "(connect)");
                        }
                        p.put("smtp_user", p.getProperty("user_name", ""));
                        p.put("smtp_pass", p.getProperty("user_pass", ""));
                        p.remove("user_name");
                        p.remove("user_pass");
                        p.put("body", "<LINE>" + p.getProperty("body", "") + "</LINE>");
                        p.put("event_dir_data", p.getProperty("email_dir_data", ""));
                        p.remove("email_dir_data");
                        p.put("event_action_list", "(send_email)");
                        p.put("event_if_list", "");
                        p.put("event_always_cb", "true");
                        p.put("event_after_list", "");
                        p.put("event_after_cb", "false");
                        p.put("event_now_cb", "true");
                        p.put("event_if_cb", "false");
                        p.put("event_plugin_list", "");
                        ++xx;
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
                Enumeration<Object> keys = user_prop.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement().toString();
                    Object val = user_prop.get(key);
                    if (val instanceof String) {
                        if (!parentUser.equals("") && val.toString().startsWith("i*")) {
                            user_prop.put(key, String.valueOf(val.toString().substring(2)) + "@" + parentUser);
                        } else if (val.toString().startsWith("i*")) {
                            user_prop.put(key, val.toString().substring(2));
                        }
                    }
                    if (key.equals("inherit_email_event") && val.toString().equals("1") && !parentUser.equals("")) {
                        user_prop.put("inherit_events", "@" + parentUser);
                        continue;
                    }
                    if (key.equals("inherit_ip_restrictions") && val.toString().equals("1") && !parentUser.equals("")) {
                        user_prop.put("inherit_ip_restrictions", "@" + parentUser);
                        continue;
                    }
                    if (!key.equals("inherit_dirs") || !val.toString().equals("1") || parentUser.equals("")) continue;
                    user_prop.put("root_dir", String.valueOf(user_prop.getProperty("root_dir")) + "@" + parentUser);
                }
                String userSubdir = "";
                File_S parent = (File_S)new File_S(String.valueOf(dir) + item).getParentFile();
                while (!(parent = (File_S)parent.getParentFile()).getName().toUpperCase().startsWith("USERS_")) {
                    userSubdir = String.valueOf(parent.getName()) + "/" + userSubdir;
                }
                Vector dirs = (Vector)user_prop.get("dirs");
                user_prop.remove("dirs");
                if (dirs == null) {
                    dirs = new Vector();
                }
                new File_S(String.valueOf(pathOut) + userSubdir + curUser + "/VFS/").mkdirs();
                Common.updateOSXInfo(String.valueOf(pathOut) + userSubdir + curUser + "/VFS/");
                Properties dir_items = new Properties();
                int xx = 0;
                while (xx < dirs.size()) {
                    Properties p = (Properties)dirs.elementAt(xx);
                    if (p.getProperty("type").equals("RD")) {
                        Vector user_vfs = (Vector)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS.getBytes("UTF8"))).getRootElement());
                        String root_dir = Common.url_decode(p.getProperty("root_dir"));
                        root_dir = Common.replace_str(root_dir, "//", "/");
                        root_dir = Common.replace_str(root_dir, "//", "/");
                        String item_name = Common.url_decode(p.getProperty("name"));
                        String subdir = "";
                        if (item_name.equals("")) {
                            item_name = Common.last(Common.url_decode(p.getProperty("dir")));
                            item_name = item_name.substring(0, item_name.length() - 1);
                            subdir = Common.url_decode(p.getProperty("dir"));
                            String s = String.valueOf(subdir = subdir.substring(0, subdir.length() - (item_name.length() + 1))) + item_name;
                            if (!s.startsWith("/")) {
                                s = "/" + s;
                            }
                            if (!s.endsWith("/")) {
                                s = String.valueOf(s) + "/";
                            }
                            dir_items.put(s, "dir");
                        } else {
                            subdir = Common.url_decode(p.getProperty("dir"));
                            root_dir = String.valueOf(root_dir) + item_name;
                            root_dir = Common.replace_str(root_dir, "//", "/");
                            root_dir = Common.replace_str(root_dir, "//", "/");
                            ((Properties)user_vfs.elementAt(0)).put("type", "file");
                        }
                        String theUrl = String.valueOf(new File_S(root_dir).toURI().toURL().toExternalForm()) + (new File_S(root_dir).isDirectory() ? "/" : "");
                        theUrl = Common.replace_str(theUrl, "//", "/");
                        theUrl = Common.replace_str(theUrl, "//", "/");
                        theUrl = theUrl.startsWith("file:/") ? Common.replace_str(theUrl, "file:/", "FILE://") : Common.replace_str(theUrl, "FILE:/", "FILE://");
                        ((Properties)user_vfs.elementAt(0)).put("url", theUrl);
                        new File_S(String.valueOf(pathOut) + userSubdir + curUser + "/VFS/" + subdir).mkdirs();
                        Common.updateOSXInfo(String.valueOf(pathOut) + userSubdir + curUser + "/VFS/" + subdir);
                        Common.writeXMLObject(String.valueOf(pathOut) + userSubdir + curUser + "/VFS/" + subdir + item_name, (Object)user_vfs, "VFS");
                    }
                    ++xx;
                }
                this.ConvertCrushFTP3Users_R_Item(dirs, String.valueOf(pathOut) + userSubdir, curUser, user_vfs_item, dir_items);
                this.ConvertCrushFTP3Users_R_Item(dirs, String.valueOf(pathOut) + userSubdir, curUser, user_vfs_item, dir_items);
                Common.writeXMLObject(String.valueOf(pathOut) + userSubdir + curUser + "/VFS.XML", (Object)user_vfs_item, "VFS");
                Common.writeXMLObject(String.valueOf(pathOut) + userSubdir + curUser + "/user.XML", (Object)user_prop, "userfile");
            }
            ++x;
        }
    }

    public void ConvertCrushFTP3Users_R_Item(Vector dirs, String pathOut, String curUser, Properties user_vfs_item, Properties dir_items) {
        int xx = 0;
        while (xx < dirs.size()) {
            Properties p = (Properties)dirs.elementAt(xx);
            if (p.getProperty("type").equals("R")) {
                String s;
                String subdir = Common.url_decode(p.getProperty("dir"));
                String item_name1 = Common.url_decode(p.getProperty("name"));
                String item_name2 = Common.url_decode(p.getProperty("data"));
                if (!new File_S(String.valueOf(pathOut) + curUser + "/VFS/" + subdir + item_name2).exists()) {
                    new File_S(String.valueOf(pathOut) + curUser + "/VFS/" + subdir + item_name1).renameTo(new File_S(String.valueOf(pathOut) + curUser + "/VFS/" + subdir + item_name2));
                }
                String privs = p.getProperty("privs");
                String new_privs = "";
                if (privs.indexOf("r") >= 0) {
                    new_privs = String.valueOf(new_privs) + "(read)";
                }
                if (privs.indexOf("w") >= 0) {
                    new_privs = String.valueOf(new_privs) + "(write)";
                }
                if (privs.indexOf("v") >= 0) {
                    new_privs = String.valueOf(new_privs) + "(view)";
                }
                if (privs.indexOf("d") >= 0) {
                    new_privs = String.valueOf(new_privs) + "(delete)";
                }
                if (privs.indexOf("x") >= 0) {
                    new_privs = String.valueOf(new_privs) + "(resume)";
                }
                if (privs.indexOf("n") >= 0) {
                    new_privs = String.valueOf(new_privs) + "(rename)";
                }
                if (privs.indexOf("k") >= 0) {
                    new_privs = String.valueOf(new_privs) + "(makedir)";
                }
                if (privs.indexOf("m") >= 0) {
                    new_privs = String.valueOf(new_privs) + "(deletedir)";
                }
                if (privs.indexOf("h") >= 0) {
                    new_privs = String.valueOf(new_privs) + "(invisible)";
                }
                if (privs.indexOf("f") >= 0) {
                    new_privs = String.valueOf(new_privs) + "(ratio)";
                }
                if (privs.indexOf("e") >= 0) {
                    new_privs = String.valueOf(new_privs) + "(stealupload)";
                }
                if (privs.indexOf("b") >= 0) {
                    new_privs = String.valueOf(new_privs) + "(bypassqueue)";
                }
                String quota = "";
                if (privs.indexOf("q") >= 0) {
                    quota = "" + Long.parseLong(privs.substring(privs.indexOf("q") + 1)) / 1024L;
                }
                if (privs.indexOf("q") >= 0) {
                    new_privs = String.valueOf(new_privs) + "(quota" + quota + ")";
                }
                if (!(s = String.valueOf(subdir) + item_name1).startsWith("/")) {
                    s = "/" + s;
                }
                if (!s.endsWith("/")) {
                    s = String.valueOf(s) + "/";
                }
                user_vfs_item.put(String.valueOf((String.valueOf(subdir) + item_name2).toUpperCase()) + (dir_items.containsKey(s) ? "/" : ""), new_privs);
            }
            ++xx;
        }
    }

    public void convertTabDelimited(String pathToWebStartFile, String user_path) throws Exception {
        String pathOut = String.valueOf(System.getProperties().getProperty("crushftp.users")) + user_path;
        String serverGroup = user_path;
        if (serverGroup.endsWith("/")) {
            serverGroup = serverGroup.substring(0, serverGroup.length() - 1);
        }
        Properties groups = UserTools.getGroups(serverGroup);
        Properties inheritance = UserTools.getInheritance(serverGroup);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File_S(pathToWebStartFile))));
            String data = "";
            while ((data = br.readLine()) != null) {
                StringTokenizer get_em = new StringTokenizer(data, "\t");
                String curUser = get_em.nextToken().trim();
                String curPassword = get_em.nextToken();
                if (!(curPassword.startsWith("MD5:") || curPassword.startsWith("MD5CRYPT:") || curPassword.startsWith("PBKDF2SHA256:") || curPassword.startsWith("SHA512CRYPT:") || curPassword.startsWith("BCRYPT:") || curPassword.startsWith("CRYPT3:") || curPassword.startsWith("MD4:") || curPassword.startsWith("SHA512:") || curPassword.startsWith("SHA256:") || curPassword.startsWith("SHA3:") || curPassword.startsWith("ARGOND:"))) {
                    curPassword = this.encode_pass(curPassword, ServerStatus.SG("password_encryption"), "").trim();
                }
                String root_dir = get_em.nextToken().trim();
                if ((root_dir = Common.replace_str(root_dir, "ROOT:", "/")).split(":").length > 2) {
                    root_dir = root_dir.replace(':', '/');
                }
                String permissions = "(read)(write)(view)(delete)(resume)(rename)(makedir)(deletedir)";
                if (get_em.hasMoreElements()) {
                    permissions = get_em.nextToken().trim();
                }
                if (permissions.indexOf("(") < 0 && permissions.indexOf(")") < 0) {
                    permissions = "(read)(write)(view)(delete)(resume)(rename)(makedir)(deletedir)";
                }
                String email = "";
                String first_name = "";
                String last_name = "";
                String group = "";
                String notes = "";
                String salt = "";
                if (get_em.hasMoreElements()) {
                    email = get_em.nextToken().trim();
                }
                if (get_em.hasMoreElements()) {
                    first_name = get_em.nextToken().trim();
                }
                if (get_em.hasMoreElements()) {
                    last_name = get_em.nextToken().trim();
                }
                if (get_em.hasMoreElements()) {
                    group = get_em.nextToken().trim();
                }
                if (get_em.hasMoreElements()) {
                    notes = get_em.nextToken().trim();
                }
                if (get_em.hasMoreElements()) {
                    salt = get_em.nextToken().trim();
                }
                Properties user_prop = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_master.getBytes("UTF8"))).getRootElement());
                if (new File_S(String.valueOf(pathOut) + curUser + "/user.XML").exists()) {
                    user_prop = (Properties)Common.readXMLObject(String.valueOf(pathOut) + curUser + "/user.XML");
                }
                Vector user_vfs = (Vector)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS.getBytes("UTF8"))).getRootElement());
                Properties user_vfs_item = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS_ITEM.getBytes("UTF8"))).getRootElement());
                if (new File_S(String.valueOf(pathOut) + curUser + "/VFS.XML").exists()) {
                    user_vfs_item = (Properties)Common.readXMLObject(String.valueOf(pathOut) + curUser + "/VFS.XML");
                }
                String newUrl = new File_S(root_dir).toURI().toURL().toExternalForm();
                new File_S(root_dir).mkdirs();
                if (!newUrl.endsWith("/") && !newUrl.endsWith("\\")) {
                    newUrl = String.valueOf(newUrl) + "/";
                }
                if (newUrl.toLowerCase().startsWith("file:/") && !newUrl.toLowerCase().startsWith("file://")) {
                    newUrl = "FILE://" + newUrl.substring("file:/".length());
                }
                ((Properties)user_vfs.elementAt(0)).put("url", newUrl);
                user_prop.put("password", curPassword);
                if (!salt.equals("")) {
                    user_prop.put("salt", salt);
                }
                if (!email.equals("")) {
                    user_prop.put("email", email);
                }
                if (!first_name.equals("")) {
                    user_prop.put("first_name", first_name);
                }
                if (!last_name.equals("")) {
                    user_prop.put("last_name", last_name);
                }
                if (!notes.equals("")) {
                    user_prop.put("notes", notes);
                }
                if (!group.equals("")) {
                    Vector<String> v = (Vector<String>)groups.get(group);
                    if (v == null) {
                        v = new Vector<String>();
                    }
                    groups.put(group, v);
                    v.addElement(curUser.toUpperCase());
                    v = (Vector<String>)inheritance.get(curUser);
                    if (v == null) {
                        v = new Vector<String>();
                    }
                    v.addElement(group);
                    inheritance.put(curUser, v);
                }
                user_vfs_item.put("/" + new File_S(root_dir).getName().toUpperCase() + "/", permissions);
                new File_S(String.valueOf(pathOut) + curUser).mkdirs();
                Common.updateOSXInfo(String.valueOf(pathOut) + curUser);
                new File_S(String.valueOf(pathOut) + curUser + "/VFS/").mkdirs();
                Common.updateOSXInfo(String.valueOf(pathOut) + curUser + "/VFS/");
                Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/user.XML", (Object)user_prop, "userfile");
                Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS/" + new File_S(root_dir).getName(), (Object)user_vfs, "VFS");
                Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS.XML", (Object)user_vfs_item, "VFS");
            }
        }
        finally {
            br.close();
            UserTools.writeGroups(serverGroup, groups);
            UserTools.writeInheritance(serverGroup, inheritance);
        }
    }

    public static String importCSV(Properties request, String serverGroup) throws Exception {
        String results = "";
        if (serverGroup.endsWith("/")) {
            serverGroup = serverGroup.substring(0, serverGroup.length() - 1);
        }
        Properties defaults = UserTools.ut.getUser(serverGroup, "default", true);
        Properties groups = UserTools.getGroups(serverGroup);
        Properties inheritance = UserTools.getInheritance(serverGroup);
        Properties xref = new Properties();
        int x = 0;
        while (x < 50) {
            if (request.containsKey("col" + x)) {
                xref.put(String.valueOf(x), request.getProperty("col" + x));
            }
            ++x;
        }
        BufferedReader br = null;
        Vector current_user_group_listing = new Vector();
        UserTools.refreshUserList(serverGroup, current_user_group_listing);
        String last_line = "";
        try {
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(new File_S(request.getProperty("the_dir")))));
                String data = "";
                int loops = 0;
                while ((data = br.readLine()) != null) {
                    Properties tmp_user;
                    String group;
                    Vector<String> v;
                    String salt;
                    last_line = data;
                    if (++loops == 1 && request.getProperty("first_header", "false").equalsIgnoreCase("true")) continue;
                    String[] parts = data.split(request.getProperty("csv_separator"));
                    Properties ref = new Properties();
                    int loop = 0;
                    while (loop < parts.length) {
                        String s = parts[loop].trim();
                        if (!xref.getProperty(String.valueOf(loop)).equals("")) {
                            if (xref.getProperty(String.valueOf(loop)).equals("linked_vfs")) {
                                Vector<String> v2 = new Vector<String>();
                                int x2 = 0;
                                while (x2 < s.split(",").length) {
                                    if (!s.split(",")[x2].trim().equals("")) {
                                        v2.addElement(s.split(",")[x2].trim());
                                    }
                                    ++x2;
                                }
                                ref.put(xref.getProperty(String.valueOf(loop)), v2);
                            } else {
                                ref.put(xref.getProperty(String.valueOf(loop)), s.trim());
                            }
                        }
                        ++loop;
                    }
                    boolean has_pass = ref.containsKey("user_pass");
                    String curPassword = "" + ref.remove("user_pass");
                    if (request.getProperty("password_type", "").equalsIgnoreCase("md5saltedhash")) {
                        salt = "";
                        int chars = Integer.parseInt(request.getProperty("salted_x_char"));
                        if (chars < 0) {
                            salt = curPassword.substring(0, Math.abs(chars));
                            curPassword = curPassword.substring(salt.length());
                        } else {
                            salt = curPassword.substring(curPassword.length() - Math.abs(chars));
                            curPassword = curPassword.substring(0, curPassword.length() - salt.length());
                        }
                        curPassword = "MD5:" + curPassword.toLowerCase();
                        if (chars < 0) {
                            salt = "!" + salt;
                        }
                        ref.put("salt", salt);
                    } else if (request.getProperty("password_type", "").equalsIgnoreCase("sha512cryptsaltedhash")) {
                        salt = "";
                        int chars = Integer.parseInt(request.getProperty("salted_x_char"));
                        salt = chars < 0 ? curPassword.substring(0, Math.abs(chars)) : curPassword.substring(curPassword.length() - Math.abs(chars));
                        curPassword = "SHA512CRYPT:" + curPassword;
                        if (chars < 0) {
                            salt = "!" + salt;
                        }
                        ref.put("salt", salt);
                    } else {
                        String use_salt = "";
                        if (!ref.getProperty("salt", defaults.getProperty("salt", "")).equals("")) {
                            ref.put("salt", ref.getProperty("salt", defaults.getProperty("salt", "")));
                        }
                        if (ref.getProperty("salt", "").equalsIgnoreCase("random")) {
                            ref.put("salt", Common.makeBoundary(8));
                            use_salt = ref.getProperty("salt");
                            if (!request.getProperty("password_type", "").equals("plain")) {
                                use_salt = "";
                                ref.remove("salt");
                            }
                        }
                        if (request.getProperty("password_type", "").equalsIgnoreCase("md5hash")) {
                            curPassword = "MD5:" + curPassword.toLowerCase();
                        } else if (request.getProperty("password_type", "").equalsIgnoreCase("md5crypt")) {
                            curPassword = "MD5CRYPT:" + curPassword;
                        } else if (request.getProperty("password_type", "").equalsIgnoreCase("pbkdf2sha256")) {
                            curPassword = "PBKDF2SHA256:" + curPassword;
                        } else if (request.getProperty("password_type", "").equalsIgnoreCase("sha512crypt")) {
                            curPassword = "SHA512CRYPT:" + curPassword;
                        } else if (request.getProperty("password_type", "").equalsIgnoreCase("bcrypt")) {
                            curPassword = "BCRYPT:" + curPassword;
                        } else if (request.getProperty("password_type", "").equalsIgnoreCase("sha256")) {
                            curPassword = "SHA256:" + curPassword.toLowerCase();
                        } else if (request.getProperty("password_type", "").equalsIgnoreCase("sha512")) {
                            curPassword = "SHA512:" + curPassword.toLowerCase();
                        }
                        if (!(curPassword.startsWith("MD5:") || curPassword.startsWith("MD5CRYPT:") || curPassword.startsWith("PBKDF2SHA256:") || curPassword.startsWith("SHA512CRYPT:") || curPassword.startsWith("BCRYPT:") || curPassword.startsWith("CRYPT3:") || curPassword.startsWith("MD4:") || curPassword.startsWith("SHA512:") || curPassword.startsWith("SHA256:") || curPassword.startsWith("SHA3:") || curPassword.startsWith("ARGOND:"))) {
                            curPassword = ServerStatus.thisObj.common_code.encode_pass(curPassword, ServerStatus.SG("password_encryption"), use_salt).trim();
                        }
                    }
                    if (has_pass) {
                        ref.put("password", curPassword);
                    }
                    String root_dir = "" + ref.remove("home_folder");
                    Properties virtual = null;
                    if (!root_dir.equals("null")) {
                        VRL vrl;
                        Vector<Properties> vfs_permissions_object;
                        String permissions = "(read)(write)(view)(delete)(resume)(rename)(makedir)(deletedir)";
                        if (!ref.getProperty("permissions", "").equals("")) {
                            permissions = "" + ref.remove("permissions");
                        }
                        if (permissions.indexOf("(") < 0 && permissions.indexOf(")") < 0) {
                            permissions = "(read)(write)(view)(delete)(resume)(rename)(makedir)(deletedir)";
                        }
                        if ((vfs_permissions_object = (Vector<Properties>)(virtual = UserTools.ut.getVirtualVFS(serverGroup, ref.getProperty("user_name"))).get("vfs_permissions_object")) == null) {
                            vfs_permissions_object = new Vector<Properties>();
                            vfs_permissions_object.add(new Properties());
                        }
                        ((Properties)vfs_permissions_object.elementAt(0)).put("/" + new File_S(root_dir).getName().toUpperCase() + "/", permissions);
                        if (!((Properties)vfs_permissions_object.elementAt(0)).containsKey("/")) {
                            ((Properties)vfs_permissions_object.elementAt(0)).put("/", "(read)(view)(resume)");
                        }
                        String newUrl = null;
                        String item_name = null;
                        Properties vfs_prop = new Properties();
                        if (root_dir.toLowerCase().startsWith("s3:") || root_dir.toLowerCase().startsWith("s3crush:")) {
                            newUrl = root_dir;
                            vrl = new VRL(newUrl);
                            item_name = vrl.getName();
                            vfs_prop.put("secretKeyID", vrl.getUsername());
                            vfs_prop.put("secretKey", vrl.getPassword());
                            vfs_prop.put("type", "DIR");
                        } else if (root_dir.toLowerCase().startsWith("file:") || root_dir.toLowerCase().startsWith("ftp:") || root_dir.toLowerCase().startsWith("ftpes:") || root_dir.toLowerCase().startsWith("ftps:") || root_dir.toLowerCase().startsWith("sftp:") || root_dir.toLowerCase().startsWith("http:") || root_dir.toLowerCase().startsWith("https:") || root_dir.toLowerCase().startsWith("webdav:") || root_dir.toLowerCase().startsWith("webdavs:") || root_dir.toLowerCase().startsWith("smb:") || root_dir.toLowerCase().startsWith("smb3:") || root_dir.toLowerCase().startsWith("smb1:")) {
                            newUrl = root_dir;
                            vrl = new VRL(newUrl);
                            item_name = vrl.getName();
                            vfs_prop.put("type", "DIR");
                        } else {
                            newUrl = new File_S(root_dir).toURI().toURL().toExternalForm();
                            new File_S(root_dir).mkdirs();
                            if (!newUrl.endsWith("/") && !newUrl.endsWith("\\")) {
                                newUrl = String.valueOf(newUrl) + "/";
                            }
                            if (newUrl.toLowerCase().startsWith("file:/") && !newUrl.toLowerCase().startsWith("file://")) {
                                newUrl = "FILE://" + newUrl.substring("file:/".length());
                            }
                            item_name = new File_S(root_dir).getName();
                            vfs_prop.put("type", "FILE");
                        }
                        if (item_name.endsWith("/")) {
                            item_name = item_name.substring(0, item_name.length() - 1);
                        }
                        Vector<Properties> user_vfs = new Vector<Properties>();
                        user_vfs.addElement(vfs_prop);
                        vfs_prop.put("url", newUrl);
                        Properties pp = new Properties();
                        pp.put("virtualPath", "/" + item_name);
                        pp.put("name", item_name);
                        pp.put("type", "FILE");
                        pp.put("vItems", user_vfs);
                        virtual.put("/" + item_name, pp);
                        ref.put("root_dir", "/");
                    }
                    if (ref.getProperty("user_name") == null) {
                        throw new Exception("Username is required for import.");
                    }
                    Properties user_prop = UserTools.ut.getUser(serverGroup, ref.getProperty("user_name"), false);
                    boolean add_all = false;
                    if (user_prop == null) {
                        user_prop = (Properties)com.crushftp.client.Common.CLONE(ref);
                    } else {
                        add_all = true;
                    }
                    if (user_prop.containsKey("linked_vfs")) {
                        if (user_prop.get("linked_vfs") instanceof String) {
                            String[] s = user_prop.get("linked_vfs").toString().split(",");
                            v = new Vector<String>();
                            int x3 = 0;
                            while (x3 < s.length) {
                                if (!s[x3].trim().equals("")) {
                                    v.addElement(s[x3].trim());
                                }
                                ++x3;
                            }
                            user_prop.put("linked_vfs", v);
                        }
                        Vector v3 = (Vector)user_prop.get("linked_vfs");
                        Vector v2 = (Vector)ref.get("linked_vfs");
                        int x4 = 0;
                        while (v2 != null && x4 < v2.size()) {
                            if (v3.indexOf(v2.elementAt(x4)) < 0) {
                                v3.addElement(v2.elementAt(x4));
                            }
                            ++x4;
                        }
                        ref.put("linked_vfs", v3);
                    }
                    if (add_all) {
                        user_prop.putAll((Map<?, ?>)ref);
                    }
                    if ((group = "" + ref.remove("userGroup")).equalsIgnoreCase("null")) {
                        group = "";
                    }
                    if (group.equals("") && !request.getProperty("default_group", "").equals("all") && !request.getProperty("default_group", "").equals("notingroup")) {
                        group = request.getProperty("default_group");
                    }
                    if (!group.equals("")) {
                        v = (Vector<String>)groups.get(group);
                        if (v == null) {
                            v = new Vector<String>();
                        }
                        groups.put(group, v);
                        if (!v.contains(ref.getProperty("user_name"))) {
                            v.addElement(ref.getProperty("user_name"));
                        }
                        if (current_user_group_listing.indexOf(group) >= 0) {
                            v = (Vector<String>)inheritance.get(ref.getProperty("user_name"));
                            if (v == null) {
                                v = new Vector<String>();
                            }
                            v.addElement(group);
                            inheritance.put(ref.getProperty("user_name"), v);
                        }
                    }
                    if (!ref.getProperty("copy_user", "").equals("") && (tmp_user = UserTools.ut.getUser(serverGroup, ref.getProperty("copy_user", ""), false)) != null) {
                        tmp_user.putAll((Map<?, ?>)ref);
                        tmp_user = ref;
                    }
                    if (!ref.containsKey("created_time")) {
                        user_prop.put("created_time", String.valueOf(System.currentTimeMillis()));
                    }
                    if (!ref.containsKey("root_dir") && !user_prop.containsKey("root_dir")) {
                        user_prop.put("root_dir", "/");
                    }
                    UserTools.writeUser(serverGroup, ref.getProperty("user_name"), user_prop, true, true, request);
                    if (virtual == null) continue;
                    UserTools.writeVFS(serverGroup, ref.getProperty("user_name"), virtual, true, request);
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 0, "Last line:" + last_line);
                Log.log("SERVER", 0, e);
                throw new Exception(e + ":" + last_line);
            }
        }
        finally {
            br.close();
            UserTools.writeGroups(serverGroup, groups, true, request);
            UserTools.writeInheritance(serverGroup, inheritance, true, request);
        }
        return results;
    }

    public void convertWingFTP(String pathToXml, String user_path, String prefix) throws Exception {
        String pathOut = String.valueOf(System.getProperties().getProperty("crushftp.users")) + user_path;
        String serverGroup = user_path;
        if (serverGroup.endsWith("/")) {
            serverGroup = serverGroup.substring(0, serverGroup.length() - 1);
        }
        File_S[] files = (File_S[])new File_S(pathToXml).listFiles();
        Exception exception = null;
        int x = 0;
        while (x < files.length) {
            File_S f = files[x];
            if (f.getName().toUpperCase().endsWith(".XML")) {
                String curUser = "";
                try {
                    Document doc = Common.getSaxBuilder().build((File)f);
                    Element USER = doc.getRootElement().getChild("USER");
                    List props = USER.getChildren();
                    Properties user_prop = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_master.getBytes("UTF8"))).getRootElement());
                    Properties user_prop2 = new Properties();
                    Properties vfs_prop2 = new Properties();
                    int xx = 0;
                    while (xx < props.size()) {
                        Element prop = (Element)props.get(xx);
                        if (prop.getName().equalsIgnoreCase("folder")) {
                            List folders = prop.getChildren();
                            int xxx = 0;
                            while (xxx < folders.size()) {
                                Element vfse = (Element)folders.get(xxx);
                                vfs_prop2.put(vfse.getName().toLowerCase(), vfse.getText());
                                ++xxx;
                            }
                        } else {
                            user_prop2.put(prop.getName().toLowerCase(), prop.getText());
                        }
                        ++xx;
                    }
                    curUser = user_prop2.getProperty("username");
                    user_prop.put("user_name", user_prop2.getProperty("username"));
                    user_prop.put("password", String.valueOf(prefix) + user_prop2.getProperty("password"));
                    user_prop.put("max_logins", user_prop2.getProperty("maxconnection"));
                    user_prop.put("max_logins_ip", user_prop2.getProperty("connectionperip"));
                    if (!user_prop2.getProperty("enableaccount", "1").equals("1")) {
                        user_prop.put("max_logins", "-1");
                    }
                    if (!user_prop2.getProperty("enableexpire", "0").equals("1")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                        SimpleDateFormat sdf2 = new SimpleDateFormat("MMddyyyyHHmm", Locale.US);
                        user_prop.put("account_expire", sdf2.format(sdf.parse(user_prop2.getProperty("expiretime"))));
                        user_prop.put("account_expire_delete", "false");
                    }
                    user_prop.put("speed_limit_download", user_prop2.getProperty("maxdownloadspeedperuser"));
                    user_prop.put("speed_limit_upload", user_prop2.getProperty("maxuploadspeedperuser"));
                    user_prop.put("ssh_public_keys", user_prop2.getProperty("sshpublickeypath"));
                    user_prop.put("notes", String.valueOf(user_prop.getProperty("notes", "")) + " name:" + user_prop2.getProperty("notesname"));
                    user_prop.put("notes", String.valueOf(user_prop.getProperty("notes", "")) + " address:" + user_prop2.getProperty("notesaddress"));
                    user_prop.put("notes", String.valueOf(user_prop.getProperty("notes", "")) + " zip:" + user_prop2.getProperty("noteszipcode"));
                    user_prop.put("notes", String.valueOf(user_prop.getProperty("notes", "")) + " phone:" + user_prop2.getProperty("notesphone"));
                    user_prop.put("notes", String.valueOf(user_prop.getProperty("notes", "")) + " fax:" + user_prop2.getProperty("notesfax"));
                    user_prop.put("notes", String.valueOf(user_prop.getProperty("notes", "")) + " memo:" + user_prop2.getProperty("notesmemo"));
                    user_prop.put("email", user_prop2.getProperty("notesemail"));
                    Vector user_vfs = (Vector)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS.getBytes("UTF8"))).getRootElement());
                    Properties user_vfs_item = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS_ITEM.getBytes("UTF8"))).getRootElement());
                    File_S home_folder = new File_S(vfs_prop2.getProperty("path"));
                    String newUrl = home_folder.toURI().toURL().toExternalForm();
                    if (!newUrl.endsWith("/") && !newUrl.endsWith("\\")) {
                        newUrl = String.valueOf(newUrl) + "/";
                    }
                    ((Properties)user_vfs.elementAt(0)).put("url", newUrl);
                    String permissions = "";
                    if (vfs_prop2.getProperty("file_read", "").equals("1")) {
                        permissions = String.valueOf(permissions) + "(read)";
                    }
                    if (vfs_prop2.getProperty("file_write", "").equals("1")) {
                        permissions = String.valueOf(permissions) + "(write)";
                    }
                    if (vfs_prop2.getProperty("file_append", "").equals("1")) {
                        permissions = String.valueOf(permissions) + "(resume)";
                    }
                    if (vfs_prop2.getProperty("file_delete", "").equals("1")) {
                        permissions = String.valueOf(permissions) + "(delete)";
                    }
                    if (vfs_prop2.getProperty("file_rename", "").equals("1")) {
                        permissions = String.valueOf(permissions) + "(rename)";
                    }
                    if (vfs_prop2.getProperty("directory_list", "").equals("1")) {
                        permissions = String.valueOf(permissions) + "(view)";
                    }
                    if (vfs_prop2.getProperty("directory_rename", "").equals("1")) {
                        permissions = String.valueOf(permissions) + "(rename)";
                    }
                    if (vfs_prop2.getProperty("directory_make", "").equals("1")) {
                        permissions = String.valueOf(permissions) + "(makedirectory)";
                    }
                    if (vfs_prop2.getProperty("directory_delete", "").equals("1")) {
                        permissions = String.valueOf(permissions) + "(deletedirectory)";
                    }
                    user_vfs_item.put("/" + home_folder.getName().toUpperCase() + "/", permissions);
                    new File_S(String.valueOf(pathOut) + user_prop.getProperty("user_name")).mkdirs();
                    Common.updateOSXInfo(String.valueOf(pathOut) + user_prop.getProperty("user_name"));
                    new File_S(String.valueOf(pathOut) + user_prop.getProperty("user_name") + "/VFS/").mkdirs();
                    Common.updateOSXInfo(String.valueOf(pathOut) + user_prop.getProperty("user_name") + "/VFS/");
                    Common.writeXMLObject(String.valueOf(pathOut) + user_prop.getProperty("user_name") + "/user.XML", (Object)user_prop, "userfile");
                    Common.writeXMLObject(String.valueOf(pathOut) + user_prop.getProperty("user_name") + "/VFS/" + home_folder.getName(), (Object)user_vfs, "VFS");
                    Common.writeXMLObject(String.valueOf(pathOut) + user_prop.getProperty("user_name") + "/VFS.XML", (Object)user_vfs_item, "VFS");
                }
                catch (Exception e) {
                    if (exception == null) {
                        exception = e;
                    }
                    Log.log("SERVER", 0, "WingFTP:" + curUser);
                    Log.log("SERVER", 0, e);
                }
            }
            ++x;
        }
        if (exception != null) {
            throw exception;
        }
    }

    public void convertFilezilla(String pathToXml, String user_path) throws Exception {
        String pathOut = String.valueOf(System.getProperties().getProperty("crushftp.users")) + user_path;
        String serverGroup = user_path;
        if (serverGroup.endsWith("/")) {
            serverGroup = serverGroup.substring(0, serverGroup.length() - 1);
        }
        Properties groups = UserTools.getGroups(serverGroup);
        Properties inheritance = UserTools.getInheritance(serverGroup);
        try {
            Document doc = Common.getSaxBuilder().build((File)new File_S(pathToXml));
            Element rootElement = doc.getRootElement();
            List roots = rootElement.getChildren();
            int x = 0;
            while (x < roots.size()) {
                Element rootItem = (Element)roots.get(x);
                if (!rootItem.getName().equalsIgnoreCase("Settings")) {
                    String curUser;
                    int xx;
                    if (rootItem.getName().equalsIgnoreCase("Groups")) {
                        List groupList = rootItem.getChildren();
                        xx = 0;
                        while (xx < groupList.size()) {
                            Element groupItem = (Element)groupList.get(xx);
                            curUser = groupItem.getAttributeValue("Name");
                            Log.log("SERVER", 0, "Importing filezilla group:" + curUser);
                            Vector v = (Vector)groups.get(curUser);
                            if (v == null) {
                                v = new Vector();
                            }
                            groups.put(curUser, v);
                            new File_S(String.valueOf(pathOut) + curUser).mkdirs();
                            Common.updateOSXInfo(String.valueOf(pathOut) + curUser);
                            new File_S(String.valueOf(pathOut) + curUser + "/VFS/").mkdirs();
                            Common.updateOSXInfo(String.valueOf(pathOut) + curUser + "/VFS/");
                            Properties user_prop = this.convertFileZillaUser(groupItem, curUser, pathOut, groups, inheritance);
                            user_prop.put("max_logins", "-1");
                            user_prop.put("password", "");
                            Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/user.XML", (Object)user_prop, "userfile");
                            ++xx;
                        }
                    } else if (rootItem.getName().equalsIgnoreCase("Users")) {
                        List userList = rootItem.getChildren();
                        xx = 0;
                        while (xx < userList.size()) {
                            Element userItem = (Element)userList.get(xx);
                            curUser = userItem.getAttributeValue("Name");
                            Log.log("SERVER", 0, "Importing filezilla user:" + curUser);
                            new File_S(String.valueOf(pathOut) + curUser).mkdirs();
                            Common.updateOSXInfo(String.valueOf(pathOut) + curUser);
                            new File_S(String.valueOf(pathOut) + curUser + "/VFS/").mkdirs();
                            Common.updateOSXInfo(String.valueOf(pathOut) + curUser + "/VFS/");
                            Properties user_prop = this.convertFileZillaUser(userItem, curUser, pathOut, groups, inheritance);
                            Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/user.XML", (Object)user_prop, "userfile");
                            ++xx;
                        }
                    }
                }
                ++x;
            }
        }
        finally {
            UserTools.writeGroups(serverGroup, groups);
            UserTools.writeInheritance(serverGroup, inheritance);
        }
    }

    public Properties convertFileZillaUser(Element userElement, String curUser, String pathOut, Properties groups, Properties inheritance) throws Exception {
        Properties user_prop = new Properties();
        user_prop.put("version", "1.0");
        user_prop.put("userVersion", "6");
        user_prop.put("root_dir", "/");
        Properties user_vfs_item = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS_ITEM.getBytes("UTF8"))).getRootElement());
        List options = userElement.getChildren();
        int x = 0;
        while (x < options.size()) {
            Element option = (Element)options.get(x);
            String optionName = option.getAttributeValue("Name");
            if (optionName == null) {
                optionName = option.getName();
            }
            Log.log("SERVER", 0, "Importing filezilla user:" + curUser + " Options:" + optionName);
            if (optionName.equalsIgnoreCase("User Limit")) {
                user_prop.put("max_logins", option.getText());
            } else if (optionName.equalsIgnoreCase("IP Limit")) {
                user_prop.put("max_logins_ip", option.getText());
            } else if (optionName.equalsIgnoreCase("Enabled") && option.getText().equals("0")) {
                user_prop.put("max_logins", "-1");
            } else if (optionName.equalsIgnoreCase("Comments")) {
                user_prop.put("notes", option.getText());
            } else if (optionName.equalsIgnoreCase("ForceSsl")) {
                user_prop.put("require_encryption", String.valueOf(!option.getText().equals("0")));
            } else if (optionName.equalsIgnoreCase("Pass")) {
                user_prop.put("password", "MD5:" + option.getText().toLowerCase());
            } else if (optionName.equalsIgnoreCase("Group")) {
                if (!option.getText().equals("")) {
                    Vector v = (Vector)inheritance.get(curUser);
                    if (v == null) {
                        v = new Vector();
                    }
                    v.addElement(option.getText());
                    inheritance.put(curUser, v);
                    v = (Vector)groups.get(option.getText());
                    if (v != null) {
                        v.addElement(curUser);
                    }
                }
            } else if (optionName.equalsIgnoreCase("Permissions")) {
                List permissions = option.getChildren();
                int xx = 0;
                while (xx < permissions.size()) {
                    Vector user_vfs = (Vector)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS.getBytes("UTF8"))).getRootElement());
                    Element permission = (Element)permissions.get(xx);
                    String dir = permission.getAttributeValue("Dir").replace('\\', '/');
                    Log.log("SERVER", 0, "Importing filezilla user:" + curUser + " folder permission:" + dir);
                    if (dir.indexOf(":u") >= 0) {
                        dir = dir.substring(0, dir.indexOf(":u"));
                    }
                    if (!dir.endsWith("/")) {
                        dir = String.valueOf(dir) + "/";
                    }
                    ((Properties)user_vfs.elementAt(0)).put("url", "file://" + dir);
                    String privs = "";
                    List permissionOptions = permission.getChildren();
                    int xxx = 0;
                    while (xxx < permissionOptions.size()) {
                        Element perm = (Element)permissionOptions.get(xxx);
                        if (perm.getAttributeValue("Name") != null) {
                            if (perm.getAttributeValue("Name").equals("FileRead") && perm.getText().equals("1")) {
                                privs = String.valueOf(privs) + "(read)";
                            } else if (perm.getAttributeValue("Name").equals("FileWrite") && perm.getText().equals("1")) {
                                privs = String.valueOf(privs) + "(write)(rename)";
                            } else if (perm.getAttributeValue("Name").equals("FileDelete") && perm.getText().equals("1")) {
                                privs = String.valueOf(privs) + "(delete)";
                            } else if (perm.getAttributeValue("Name").equals("FileAppend") && perm.getText().equals("1")) {
                                privs = String.valueOf(privs) + "(resume)";
                            } else if (perm.getAttributeValue("Name").equals("DirCreate") && perm.getText().equals("1")) {
                                privs = String.valueOf(privs) + "(makedir)";
                            } else if (perm.getAttributeValue("Name").equals("DirDelete") && perm.getText().equals("1")) {
                                privs = String.valueOf(privs) + "(deletedir)";
                            } else if (perm.getAttributeValue("Name").equals("DirList") && perm.getText().equals("1")) {
                                privs = String.valueOf(privs) + "(view)";
                            }
                        }
                        ++xxx;
                    }
                    Log.log("SERVER", 0, "Importing filezilla user:" + curUser + " folder permission:" + dir + " privs:" + privs);
                    user_vfs_item.put("/" + new File_S(dir).getName().toUpperCase() + "/", privs);
                    Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS/" + new File_S(dir).getName(), (Object)user_vfs, "VFS");
                    ++xx;
                }
                Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS.XML", (Object)user_vfs_item, "VFS");
            }
            ++x;
        }
        return user_prop;
    }

    public void ConvertPasswdUsers(String path, String user_path, String prefix) throws Exception {
        String pathOut = String.valueOf(System.getProperties().getProperty("crushftp.users")) + user_path;
        BufferedReader in = new BufferedReader(new FileReader(new File_S(path)));
        String the_user = "";
        int line = 1;
        while ((the_user = in.readLine()) != null) {
            try {
                StringTokenizer get_em = new StringTokenizer(the_user, ":");
                int tokenCount = 0;
                while (get_em.hasMoreElements()) {
                    get_em.nextToken();
                    ++tokenCount;
                }
                get_em = new StringTokenizer(the_user, ":");
                String curUser = get_em.nextToken();
                String curPassword = String.valueOf(prefix) + get_em.nextToken().trim();
                get_em.nextToken();
                get_em.nextToken();
                if (tokenCount >= 10) {
                    get_em.nextToken();
                    get_em.nextToken();
                }
                String fullName = get_em.nextToken();
                String root_dir = get_em.nextToken();
                String root_dir2 = get_em.nextToken();
                if (!root_dir.startsWith("/") && root_dir2.startsWith("/") && root_dir2.indexOf("passwd") < 0 && root_dir2.indexOf("/bin") < 0) {
                    root_dir = root_dir2;
                }
                if (!root_dir.endsWith("/")) {
                    root_dir = String.valueOf(root_dir) + "/";
                }
                Properties user_prop = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_master.getBytes("UTF8"))).getRootElement());
                Vector user_vfs = (Vector)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS.getBytes("UTF8"))).getRootElement());
                Properties user_vfs_item = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS_ITEM.getBytes("UTF8"))).getRootElement());
                ((Properties)user_vfs.elementAt(0)).put("url", new File_S(root_dir).toURI().toURL().toExternalForm());
                user_prop.put("password", curPassword);
                user_prop.put("notes", "Name: " + fullName);
                user_prop.put("site", "(SITE_PASS)");
                user_vfs_item.put("/" + new File_S(root_dir).getName() + "/", "(read)(write)(view)(delete)(resume)(rename)(makedir)(deletedir)");
                new File_S(String.valueOf(pathOut) + curUser).mkdirs();
                Common.updateOSXInfo(String.valueOf(pathOut) + curUser);
                new File_S(String.valueOf(pathOut) + curUser + "/VFS/").mkdirs();
                Common.updateOSXInfo(String.valueOf(pathOut) + curUser + "/VFS/");
                Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/user.XML", (Object)user_prop, "userfile");
                Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS/" + new File_S(root_dir).getName(), (Object)user_vfs, "VFS");
                Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS.XML", (Object)user_vfs_item, "VFS");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, "IMPORT ERROR: (line " + line + ") " + the_user);
                Log.log("SERVER", 1, e);
            }
            ++line;
        }
    }

    public void ConvertProFTPDGroups(String serverGroup, String path, String user_path) {
        Properties inheritance = UserTools.getInheritance(serverGroup);
        this.proFTPDLineCount = 0;
        try {
            Vector<String> groups = new Vector<String>();
            BufferedReader in = new BufferedReader(new FileReader(new File_S(path)));
            String the_user = "";
            while ((the_user = in.readLine()) != null) {
                groups.addElement(the_user);
            }
            in.close();
            class Grouper
            implements Runnable {
                Vector groups = null;
                String user_path = null;
                private final /* synthetic */ String val$serverGroup;

                public Grouper(Vector groups, String user_path, String string) {
                    this.val$serverGroup = string;
                    this.groups = groups;
                    this.user_path = user_path;
                }

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void run() {
                    while (this.groups.size() >= 0) {
                        String the_user = null;
                        Vector vector = this.groups;
                        synchronized (vector) {
                            if (this.groups.size() > 0) {
                                the_user = this.groups.elementAt(0).toString();
                                this.groups.removeElementAt(0);
                            }
                        }
                        if (the_user == null) break;
                        try {
                            Log.log("SERVER", 0, "Working on entry: (line " + Common.this.proFTPDLineCount++ + ") " + the_user);
                            StringTokenizer get_em = new StringTokenizer(the_user, ":");
                            String curGroup = get_em.nextToken().trim();
                            get_em.nextToken();
                            String usernames = get_em.nextToken().trim();
                            try {
                                usernames = get_em.nextToken().trim();
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            StringTokenizer st_users = new StringTokenizer(usernames, ",");
                            while (st_users.hasMoreTokens()) {
                                String curUser = st_users.nextToken().trim();
                                Properties user = UserTools.ut.getUser(this.user_path, curUser, false);
                                if (user == null) continue;
                                Vector<String> linked_vfs = (Vector<String>)user.get("linked_vfs");
                                if (linked_vfs == null) {
                                    linked_vfs = new Vector<String>();
                                }
                                if (linked_vfs.indexOf(curGroup) < 0) {
                                    linked_vfs.addElement(curGroup);
                                }
                                user.put("linked_vfs", linked_vfs);
                                UserTools.changeUsername(this.val$serverGroup, curUser, curGroup, user.getProperty("password"));
                                try {
                                    UserTools.writeUser(this.val$serverGroup, curUser, user);
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 0, "GROUP IMPORT ERROR: (line " + Common.this.proFTPDLineCount + ") " + the_user);
                            Log.log("SERVER", 1, e);
                        }
                    }
                }
            }
            Worker.startWorker(new Grouper(groups, user_path, serverGroup));
            Worker.startWorker(new Grouper(groups, user_path, serverGroup));
            Worker.startWorker(new Grouper(groups, user_path, serverGroup));
            while (groups.size() > 0) {
                Thread.sleep(1000L);
            }
            Thread.sleep(10000L);
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
    }

    public void ConvertRumpusUsers(String pathToRumpustFile, String user_path) throws Exception {
        String pathOut = String.valueOf(System.getProperties().getProperty("crushftp.users")) + user_path;
        String user_info = "";
        RandomAccessFile user_is = new RandomAccessFile(new File_S(pathToRumpustFile), "r");
        int user_data_len = (int)user_is.length();
        byte[] temp_array = new byte[user_data_len];
        user_is.read(temp_array);
        user_is.close();
        user_info = new String(temp_array, 0, user_data_len, "UTF8");
        user_info = String.valueOf(user_info.trim()) + "\r";
        int loc = 0;
        while (loc >= 0) {
            if (user_info.indexOf("\r", loc) < 0) break;
            String the_user = String.valueOf(user_info.substring(loc, user_info.indexOf("\r", loc)).trim()) + "\t";
            if ((loc = user_info.indexOf("\r", loc)) > 0) {
                ++loc;
            }
            StringTokenizer get_em = new StringTokenizer(the_user, "\t");
            String curUser = get_em.nextToken();
            String curPassword = get_em.nextToken();
            if (curPassword.toUpperCase().startsWith("MCRYPT:")) {
                curPassword = "-AUTO-SET-ON-LOGIN-";
            }
            curPassword = curUser.toUpperCase().equals("ANONYMOUS") ? "" : this.encode_pass(curPassword, ServerStatus.SG("password_encryption"), "");
            String root_dir = get_em.nextToken();
            if (root_dir.indexOf(":") >= 0) {
                root_dir = root_dir.substring(root_dir.indexOf(":"));
                root_dir = "/Volumes" + root_dir;
                root_dir = root_dir.replace(':', '/');
            }
            String permissions = get_em.nextToken().toUpperCase();
            String p2 = "(resume)";
            if (permissions.charAt(1) == 'Y') {
                p2 = String.valueOf(p2) + "(read)";
            }
            if (permissions.charAt(2) == 'Y') {
                p2 = String.valueOf(p2) + "(write)(rename)";
            }
            if (permissions.charAt(3) == 'Y') {
                p2 = String.valueOf(p2) + "(delete)(rename)";
            }
            if (permissions.charAt(4) == 'Y') {
                p2 = String.valueOf(p2) + "(makedir)(rename)";
            }
            if (permissions.charAt(5) == 'Y') {
                p2 = String.valueOf(p2) + "(deletedir)";
            }
            if (permissions.charAt(7) == 'Y') {
                p2 = String.valueOf(p2) + "(view)";
            }
            permissions = p2;
            if (get_em.hasMoreTokens()) {
                get_em.nextToken();
            }
            if (get_em.hasMoreTokens()) {
                get_em.nextToken();
            }
            if (get_em.hasMoreTokens()) {
                get_em.nextToken();
            }
            String max_logins = "0";
            if (get_em.hasMoreTokens()) {
                max_logins = get_em.nextToken().substring(1);
            }
            String transfer_speed = "0";
            if (get_em.hasMoreTokens()) {
                transfer_speed = get_em.nextToken().substring(1);
            }
            if (get_em.hasMoreTokens()) {
                get_em.nextToken();
            }
            Properties user_prop = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_master.getBytes("UTF8"))).getRootElement());
            Vector user_vfs = (Vector)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS.getBytes("UTF8"))).getRootElement());
            Properties user_vfs_item = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS_ITEM.getBytes("UTF8"))).getRootElement());
            ((Properties)user_vfs.elementAt(0)).put("url", new File_S(root_dir).toURI().toURL().toExternalForm());
            user_prop.put("password", curPassword);
            user_prop.put("speed_limit_download", transfer_speed);
            user_prop.put("speed_limit_upload", transfer_speed);
            user_prop.put("max_logins_ip", max_logins);
            user_vfs_item.put("/" + new File_S(root_dir).getName() + "/", permissions);
            new File_S(String.valueOf(pathOut) + curUser).mkdirs();
            Common.updateOSXInfo(String.valueOf(pathOut) + curUser);
            new File_S(String.valueOf(pathOut) + curUser + "/VFS/").mkdirs();
            Common.updateOSXInfo(String.valueOf(pathOut) + curUser + "/VFS/");
            Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/user.XML", (Object)user_prop, "userfile");
            Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS/" + new File_S(root_dir).getName(), (Object)user_vfs, "VFS");
            Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS.XML", (Object)user_vfs_item, "VFS");
        }
    }

    public void ConvertServUUsers(String pathIn, String user_path) throws Exception {
        String pathOut = String.valueOf(System.getProperties().getProperty("crushftp.users")) + user_path;
        String curUser = "";
        String permissions = "";
        String homeDir = "";
        File_S curFile = new File_S(pathIn);
        BufferedReader in = new BufferedReader(new FileReader(new File_S(curFile)));
        boolean fisrtRun = true;
        Properties user_prop = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_master.getBytes("UTF8"))).getRootElement());
        Vector user_vfs = (Vector)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS.getBytes("UTF8"))).getRootElement());
        Properties user_vfs_item = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS_ITEM.getBytes("UTF8"))).getRootElement());
        while (in.ready()) {
            String curLine = in.readLine();
            if (curLine.startsWith("[USER=") && !fisrtRun) {
                if (!homeDir.trim().equals("")) {
                    ((Properties)user_vfs.elementAt(0)).put("url", new File_S(homeDir).toURI().toURL().toExternalForm());
                    user_vfs_item.put("/" + new File_S(homeDir).getName() + "/", permissions);
                }
                new File_S(String.valueOf(pathOut) + curUser).mkdirs();
                Common.updateOSXInfo(String.valueOf(pathOut) + curUser);
                new File_S(String.valueOf(pathOut) + curUser + "/VFS/").mkdirs();
                Common.updateOSXInfo(String.valueOf(pathOut) + curUser + "/VFS/");
                Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/user.XML", (Object)user_prop, "userfile");
                if (!homeDir.trim().equals("")) {
                    Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS/" + new File_S(homeDir).getName(), (Object)user_vfs, "VFS");
                }
                Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS.XML", (Object)user_vfs_item, "VFS");
                curUser = "";
                homeDir = "";
                permissions = "";
                user_prop = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_master.getBytes("UTF8"))).getRootElement());
                user_vfs = (Vector)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS.getBytes("UTF8"))).getRootElement());
                user_vfs_item = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS_ITEM.getBytes("UTF8"))).getRootElement());
            }
            if (curLine.startsWith("[USER=")) {
                fisrtRun = false;
                curUser = curLine.substring(6, curLine.indexOf("]"));
                if (curUser.indexOf("|") >= 0) {
                    curUser = curUser.substring(0, curUser.indexOf("|"));
                }
                if (curUser.endsWith("@1")) {
                    curUser = curUser.substring(0, curUser.length() - 2);
                }
            }
            if (curLine.startsWith("HomeDir=")) {
                String finalDirName = "";
                homeDir = curLine.substring(8).trim();
                finalDirName = homeDir.substring(homeDir.lastIndexOf("\\") + 1);
                if (finalDirName.trim().equals("")) {
                    finalDirName = "_" + Common.replace_str(homeDir, ":\\", "_").trim();
                } else {
                    homeDir = String.valueOf(homeDir) + "/";
                }
                homeDir = "/" + homeDir;
                homeDir = Common.replace_str(homeDir, ":\\", "^^");
                homeDir = Common.replace_str(homeDir, "\\", "/");
                homeDir = Common.replace_str(homeDir, "^^", ":\\/");
            }
            if (curLine.startsWith("Note")) {
                String note = curLine.substring(curLine.indexOf("=") + 1);
                if (note.startsWith("\"")) {
                    note = note.substring(1);
                }
                if (note.endsWith("\"")) {
                    note = note.substring(0, note.length() - 1);
                }
                user_prop.put("notes", String.valueOf(user_prop.getProperty("notes", "")) + note + "\r\n");
            }
            if (!curLine.startsWith("Access")) continue;
            String perms = curLine.substring(curLine.indexOf(",") + 1).toUpperCase();
            if (perms.indexOf("R") >= 0) {
                permissions = String.valueOf(permissions) + "(read)";
            }
            if (perms.indexOf("D") >= 0) {
                permissions = String.valueOf(permissions) + "(deletedir)";
            }
            if (perms.indexOf("W") >= 0) {
                permissions = String.valueOf(permissions) + "(write)(rename)";
            }
            if (perms.indexOf("M") >= 0) {
                permissions = String.valueOf(permissions) + "(delete)";
            }
            if (perms.indexOf("C") >= 0) {
                permissions = String.valueOf(permissions) + "(makedir)";
            }
            if (perms.indexOf("L") < 0) continue;
            permissions = String.valueOf(permissions) + "(view)(resume)";
        }
        in.close();
    }

    public void ConvertGene6Users(String pathIn, String user_path) throws Exception {
        String pathOut = String.valueOf(System.getProperties().getProperty("crushftp.users")) + user_path;
        File_S curFile = new File_S(pathIn);
        if (!curFile.isDirectory()) {
            curFile = (File_S)curFile.getParentFile();
        }
        Log.log("SERVER", 0, "Using user folder:" + curFile + " for Gene6 import");
        File_S[] items = (File_S[])curFile.listFiles();
        int loop = 0;
        while (loop < items.length) {
            if (!items[loop].isDirectory() && items[loop].getName().toUpperCase().endsWith(".INI")) {
                try (BufferedReader in = new BufferedReader(new FileReader(items[loop]));){
                    Properties user_prop = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_master.getBytes("UTF8"))).getRootElement());
                    Vector user_vfs = (Vector)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS.getBytes("UTF8"))).getRootElement());
                    Properties user_vfs_item = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS_ITEM.getBytes("UTF8"))).getRootElement());
                    String curUser = items[loop].getName().substring(0, items[loop].getName().lastIndexOf("."));
                    String permissions = "";
                    String homeDir = "";
                    while (in.ready()) {
                        String curLine = in.readLine();
                        if (curLine == null) break;
                        if (curLine.startsWith("AccessList")) {
                            curLine = curLine.substring(curLine.indexOf(",") + 1);
                            homeDir = curLine.substring(0, curLine.lastIndexOf(","));
                            String[] perms = curLine.substring((homeDir = homeDir.substring(0, homeDir.lastIndexOf(","))).length() + 1).toUpperCase().split(",");
                            if (perms[0].indexOf("R") >= 0) {
                                permissions = String.valueOf(permissions) + "(read)";
                            }
                            if (perms[0].indexOf("A") >= 0) {
                                permissions = String.valueOf(permissions) + "(resume)";
                            }
                            if (perms[1].indexOf("R") >= 0) {
                                permissions = String.valueOf(permissions) + "(deletedir)";
                            }
                            if (perms[0].indexOf("W") >= 0) {
                                permissions = String.valueOf(permissions) + "(write)(rename)";
                            }
                            if (perms[0].indexOf("D") >= 0) {
                                permissions = String.valueOf(permissions) + "(delete)";
                            }
                            if (perms[1].indexOf("M") >= 0) {
                                permissions = String.valueOf(permissions) + "(makedir)";
                            }
                            if (perms[1].indexOf("F") >= 0 || perms[1].indexOf("D") >= 0) {
                                permissions = String.valueOf(permissions) + "(view)";
                            }
                        }
                        if (curLine.startsWith("Notes")) {
                            String note = curLine.substring(curLine.indexOf("=") + 1);
                            if (note.startsWith("\"")) {
                                note = note.substring(1);
                            }
                            if (note.endsWith("\"")) {
                                note = note.substring(0, note.length() - 1);
                            }
                            user_prop.put("notes", String.valueOf(user_prop.getProperty("notes", "")) + note + "\r\n");
                        }
                        if (curLine.startsWith("Email")) {
                            String email = curLine.substring(curLine.indexOf("=") + 1);
                            if (email.startsWith("\"")) {
                                email = email.substring(1);
                            }
                            if (email.endsWith("\"")) {
                                email = email.substring(0, email.length() - 1);
                            }
                            user_prop.put("email", email);
                        }
                        if (!curLine.startsWith("Password=")) continue;
                        String pass = curLine.substring(curLine.indexOf("=") + 1);
                        if (pass.startsWith("\"")) {
                            pass = pass.substring(1);
                        }
                        if (pass.endsWith("\"")) {
                            pass = pass.substring(0, pass.length() - 1);
                        }
                        if (pass.length() > 10) {
                            user_prop.put("password", "MD5:" + pass.substring(4).toLowerCase());
                            continue;
                        }
                        user_prop.put("password", "");
                    }
                    if (!homeDir.trim().equals("")) {
                        ((Properties)user_vfs.elementAt(0)).put("url", new File_S(homeDir).toURI().toURL().toExternalForm());
                        user_vfs_item.put("/" + new File_S(homeDir).getName() + "/", permissions);
                    }
                    new File_S(String.valueOf(pathOut) + curUser).mkdirs();
                    Common.updateOSXInfo(String.valueOf(pathOut) + curUser);
                    new File_S(String.valueOf(pathOut) + curUser + "/VFS/").mkdirs();
                    Common.updateOSXInfo(String.valueOf(pathOut) + curUser + "/VFS/");
                    Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/user.XML", (Object)user_prop, "userfile");
                    if (!homeDir.trim().equals("")) {
                        Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS/" + new File_S(homeDir).getName(), (Object)user_vfs, "VFS");
                    }
                    Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS.XML", (Object)user_vfs_item, "VFS");
                    Log.log("SERVER", 0, "Wrote user:" + curUser);
                }
            }
            ++loop;
        }
    }

    public void ConvertBPFTPsers(String pathIn, String user_path) throws Exception {
        String pathOut = String.valueOf(System.getProperties().getProperty("crushftp.users")) + user_path;
        String permissions = "";
        String homeDir = "";
        File_S curFile = new File_S(pathIn);
        BufferedReader in = new BufferedReader(new FileReader(curFile));
        Properties user_prop = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_master.getBytes("UTF8"))).getRootElement());
        Vector user_vfs = (Vector)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS.getBytes("UTF8"))).getRootElement());
        Properties user_vfs_item = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS_ITEM.getBytes("UTF8"))).getRootElement());
        Properties user = new Properties();
        while (in.ready()) {
            String curLine = in.readLine();
            if (curLine.trim().equals("") && user.size() > 0) {
                if (!homeDir.trim().equals("")) {
                    ((Properties)user_vfs.elementAt(0)).put("url", new File_S(homeDir).toURI().toURL().toExternalForm());
                    user_vfs_item.put("/" + new File_S(homeDir).getName() + "/", permissions);
                }
                user_prop.putAll((Map<?, ?>)user);
                String curUser = user.getProperty("user_name");
                new File_S(String.valueOf(pathOut) + curUser).mkdirs();
                Common.updateOSXInfo(String.valueOf(pathOut) + curUser);
                new File_S(String.valueOf(pathOut) + curUser + "/VFS/").mkdirs();
                Common.updateOSXInfo(String.valueOf(pathOut) + curUser + "/VFS/");
                Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/user.XML", (Object)user_prop, "userfile");
                if (!homeDir.trim().equals("")) {
                    Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS/" + new File_S(homeDir).getName(), (Object)user_vfs, "VFS");
                }
                Common.writeXMLObject(String.valueOf(pathOut) + curUser + "/VFS.XML", (Object)user_vfs_item, "VFS");
                homeDir = "";
                permissions = "";
                user = new Properties();
                user_prop = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_master.getBytes("UTF8"))).getRootElement());
                user_vfs = (Vector)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS.getBytes("UTF8"))).getRootElement());
                user_vfs_item = (Properties)Common.getElements(Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(XML_VFS_ITEM.getBytes("UTF8"))).getRootElement());
            }
            if (curLine.indexOf("=") <= 0) continue;
            String key = curLine.split("=")[0];
            String val = "";
            if (curLine.split("=").length > 1) {
                val = curLine.split("=")[1];
            }
            if (key.equalsIgnoreCase("PASS")) {
                user.put("password", this.encode_pass(val, ServerStatus.SG("password_encryption"), ""));
            } else if (key.equalsIgnoreCase("LOGIN")) {
                user.put("user_name", val);
            } else if (key.equalsIgnoreCase("MaxUsers")) {
                user.put("max_logins", val);
            } else if (key.equalsIgnoreCase("MaxSpeedRcv")) {
                user.put("speed_limit_upload", val);
            } else if (key.equalsIgnoreCase("MaxSpeedSnd")) {
                user.put("speed_limit_download", val);
            } else if (key.equalsIgnoreCase("TimeOut")) {
                user.put("max_idle_time", String.valueOf(Integer.parseInt(val) / 60));
            } else if (key.equalsIgnoreCase("Dir0")) {
                homeDir = "/" + val.replace('\\', '/');
            } else if (key.equalsIgnoreCase("Attr0")) {
                String perms = curLine.substring(curLine.indexOf(",") + 1).toUpperCase();
                if (perms.indexOf("R") >= 0) {
                    permissions = String.valueOf(permissions) + "(read)";
                }
                if (perms.indexOf("W") >= 0) {
                    permissions = String.valueOf(permissions) + "(write)(rename)";
                }
                if (perms.indexOf("D") >= 0) {
                    permissions = String.valueOf(permissions) + "(delete)";
                }
                if (perms.indexOf("A") >= 0) {
                    permissions = String.valueOf(permissions) + "(resume)";
                }
                if (perms.indexOf("M") >= 0) {
                    permissions = String.valueOf(permissions) + "(makedir)";
                }
                if (perms.indexOf("L") >= 0) {
                    permissions = String.valueOf(permissions) + "(view)";
                }
                if (perms.indexOf("K") >= 0) {
                    permissions = String.valueOf(permissions) + "(deletedir)";
                }
            }
            user_prop.put("notes", String.valueOf(user_prop.getProperty("notes", "")) + curLine + "\r\n");
        }
        in.close();
    }

    public String migrateUsersVFS(String the_server, String from, String to) {
        from = from.replace('\\', '/');
        to = to.replace('\\', '/');
        if (from.toUpperCase().startsWith("FILE:/")) {
            from = from.substring("file:/".length());
        }
        if (to.toUpperCase().startsWith("FILE:/")) {
            to = to.substring("file:/".length());
        }
        if (!from.startsWith("/")) {
            from = "/" + from;
        }
        if (!to.startsWith("/")) {
            to = "/" + to;
        }
        String results = "";
        Vector v = new Vector();
        UserTools.refreshUserList(the_server, v);
        String lastItem = "";
        int x = 0;
        while (x < v.size()) {
            String username = v.elementAt(x).toString();
            String userpath = UserTools.get_real_path_to_user(the_server, username);
            Vector vv = new Vector();
            try {
                int fixedEntries = 0;
                Common.getAllFileListing(vv, String.valueOf(userpath) + "VFS", 99, false);
                int xx = 0;
                while (xx < vv.size()) {
                    File_S f = (File_S)vv.elementAt(xx);
                    lastItem = f.toString();
                    if (f.isFile()) {
                        int replaced = 0;
                        Vector proplist = (Vector)Common.readXMLObject(f.getCanonicalPath());
                        if (proplist != null) {
                            int xxx = 0;
                            while (xxx < proplist.size()) {
                                Properties p = (Properties)proplist.elementAt(xxx);
                                String url = p.getProperty("url", "");
                                if (url.toUpperCase().startsWith("FILE:/" + from.toUpperCase())) {
                                    url = "FILE:/" + to + url.substring(("FILE:/" + from).length());
                                    p.put("url", url);
                                    ++replaced;
                                    ++fixedEntries;
                                }
                                ++xxx;
                            }
                        }
                        if (replaced > 0) {
                            Common.writeXMLObject(f.getCanonicalPath(), (Object)proplist, "VFS");
                        }
                    }
                    ++xx;
                }
                if (fixedEntries > 0) {
                    results = String.valueOf(results) + LOC.G("Fixed $0 entries in user $1.", String.valueOf(fixedEntries), username) + "\r\n";
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                Log.log("SERVER", 0, lastItem);
            }
            ++x;
        }
        results = String.valueOf(results) + LOC.G("Finished!");
        return results;
    }

    public String setServerStatus(Properties server_item, String the_ip) {
        String statusMessage = "";
        if (server_item.getProperty("serverType", "FTP").toUpperCase().equals("FTPS")) {
            statusMessage = "(Implicit SSL)";
        } else if (server_item.getProperty("serverType", "FTP").toUpperCase().equals("FTP")) {
            statusMessage = "(" + (server_item.getProperty("explicit_ssl", "false").toUpperCase().equals("TRUE") ? " SSL" : "") + (server_item.getProperty("explicit_tls", "false").toUpperCase().equals("TRUE") ? " TLS" : "") + " )";
        } else if (server_item.getProperty("serverType", "FTP").toUpperCase().equals("SFTP")) {
            statusMessage = "( SSH )";
        } else if (server_item.getProperty("serverType", "FTP").toUpperCase().equals("HTTPS")) {
            statusMessage = server_item.getProperty("allow_webdav", "true").equalsIgnoreCase("true") ? "( Web, WebDAV SSL )" : "( Web, SSL )";
        } else if (server_item.getProperty("serverType", "FTP").toUpperCase().equals("HTTP")) {
            statusMessage = server_item.getProperty("allow_webdav", "true").equalsIgnoreCase("true") ? "( Web, WebDAV)" : "( Web )";
        }
        String port = ":" + server_item.getProperty("port", "21");
        if (server_item.getProperty("serverType", "FTP").toLowerCase().equals("ftp") && port.equals(":21")) {
            port = "";
        }
        if (server_item.getProperty("serverType", "FTP").toLowerCase().equals("http") && port.equals(":80")) {
            port = "";
        }
        if (server_item.getProperty("serverType", "FTP").toLowerCase().equals("https") && port.equals(":443")) {
            port = "";
        }
        if (server_item.getProperty("serverType", "FTP").toLowerCase().equals("sftp") && port.equals(":22")) {
            port = "";
        }
        statusMessage = String.valueOf(server_item.getProperty("server_item_name", " ")) + server_item.getProperty("serverType", "FTP").toLowerCase() + "://" + the_ip + port + "/ " + (statusMessage.equals("( )") ? "" : statusMessage);
        return statusMessage;
    }

    public void sortPlugins(Vector plugins) {
        if (plugins == null) {
            plugins = new Vector<Vector>();
        }
        Object[] pluginNames = new String[plugins.size()];
        int x = 0;
        while (x < plugins.size()) {
            Vector pluginPrefs = null;
            if (plugins.elementAt(x) instanceof Vector) {
                pluginPrefs = (Vector)plugins.elementAt(x);
            } else {
                pluginPrefs = new Vector();
                pluginPrefs.addElement(plugins.elementAt(x));
            }
            pluginNames[x] = ((Properties)pluginPrefs.elementAt(0)).getProperty("pluginName");
            ++x;
        }
        Arrays.sort(pluginNames);
        Vector<Object> pluginNamesVec = new Vector<Object>();
        int x2 = 0;
        while (x2 < pluginNames.length) {
            pluginNamesVec.addElement(pluginNames[x2]);
            ++x2;
        }
        Vector plugins2 = (Vector)plugins.clone();
        int x3 = 0;
        while (x3 < plugins2.size()) {
            Vector pluginPrefs = null;
            if (plugins2.elementAt(x3) instanceof Vector) {
                pluginPrefs = (Vector)plugins2.elementAt(x3);
            } else {
                pluginPrefs = new Vector();
                pluginPrefs.addElement(plugins2.elementAt(x3));
            }
            Properties pluginPref = (Properties)pluginPrefs.elementAt(0);
            String pluginName = pluginPref.getProperty("pluginName");
            plugins.setElementAt(pluginPrefs, pluginNamesVec.indexOf(pluginName));
            ++x3;
        }
    }

    public void loadPlugins(Properties server_settings, Properties server_info) {
        String[] list;
        Vector plugins = (Vector)server_settings.get("plugins");
        if (plugins == null) {
            plugins = new Vector();
        }
        this.sortPlugins(plugins);
        server_settings.put("plugins", plugins);
        Vector si_plugins = (Vector)server_info.get("plugins");
        if (si_plugins == null) {
            si_plugins = new Vector();
        }
        server_info.put("plugins", si_plugins);
        si_plugins.removeAllElements();
        if (new File_S(String.valueOf(System.getProperty("crushftp.plugins")) + "plugins/").exists() && (list = new File_S(String.valueOf(System.getProperty("crushftp.plugins")) + "plugins/").list()) != null) {
            int x = 0;
            while (x < list.length) {
                File_S test = new File_S(String.valueOf(System.getProperty("crushftp.plugins")) + "plugins/" + list[x]);
                try {
                    if (test.getName().toUpperCase().endsWith(".JAR")) {
                        Properties p;
                        String pluginName = test.getName().substring(0, test.getName().length() - 4);
                        boolean foundIt = false;
                        int xx = 0;
                        while (xx < plugins.size()) {
                            if (plugins.elementAt(xx) instanceof Vector) {
                                Vector v = (Vector)plugins.elementAt(xx);
                                Properties p2 = (Properties)v.elementAt(0);
                                if (p2.getProperty("pluginName").equals(pluginName)) {
                                    foundIt = true;
                                }
                            } else {
                                p = (Properties)plugins.elementAt(xx);
                                if (p.getProperty("pluginName").equals(pluginName)) {
                                    foundIt = true;
                                }
                            }
                            ++xx;
                        }
                        if (!foundIt) {
                            Vector<Properties> v = new Vector<Properties>();
                            p = new Properties();
                            p.put("pluginName", pluginName);
                            v.addElement(p);
                            plugins.addElement(v);
                        }
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
                ++x;
            }
        }
        int x = 0;
        while (x < plugins.size()) {
            try {
                Vector pluginPrefs = null;
                if (plugins.elementAt(x) instanceof Vector) {
                    pluginPrefs = (Vector)plugins.elementAt(x);
                } else {
                    pluginPrefs = new Vector();
                    pluginPrefs.addElement(plugins.elementAt(x));
                }
                Vector<Properties> siPluginPrefs = new Vector<Properties>();
                int xx = 0;
                while (xx < pluginPrefs.size()) {
                    Properties pluginPref = (Properties)pluginPrefs.elementAt(xx);
                    String pluginName = pluginPref.getProperty("pluginName");
                    Common.getPlugin(pluginName, new File_S(String.valueOf(System.getProperty("crushftp.plugins")) + "plugins/").toURI().toURL().toExternalForm(), pluginPref.getProperty("subItem", ""));
                    this.setPluginSettings(Common.getPlugin(pluginName, null, pluginPref.getProperty("subItem", "")), this.getPluginPrefs(pluginName, pluginPref));
                    Properties pp = (Properties)pluginPref.clone();
                    pp.put("plugin", Common.getPlugin(pluginName, null, pluginPref.getProperty("subItem", "")));
                    pp.put("pluginName", pluginName);
                    pp.put("subItem", pluginPref.getProperty("subItem", ""));
                    siPluginPrefs.addElement(pp);
                    ++xx;
                }
                if (siPluginPrefs != null) {
                    si_plugins.addElement(siPluginPrefs);
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
            ++x;
        }
    }

    public void loadPluginsSync(Properties server_settings, Properties server_info) {
        Vector plugins = (Vector)server_settings.get("plugins");
        if (plugins == null) {
            plugins = new Vector();
        }
        server_settings.put("plugins", plugins);
        Vector si_plugins = (Vector)server_info.get("plugins");
        if (si_plugins == null) {
            si_plugins = new Vector();
        }
        server_info.put("plugins", si_plugins);
        int x = 0;
        while (x < plugins.size()) {
            try {
                Vector pluginPrefs = null;
                if (plugins.elementAt(x) instanceof Vector) {
                    pluginPrefs = (Vector)plugins.elementAt(x);
                } else {
                    pluginPrefs = new Vector();
                    pluginPrefs.addElement(plugins.elementAt(x));
                }
                Vector siPluginPrefs = (Vector)si_plugins.elementAt(x);
                int xx = 0;
                while (xx < pluginPrefs.size()) {
                    Properties pluginPref = (Properties)pluginPrefs.elementAt(xx);
                    if (siPluginPrefs.size() <= xx) {
                        String pluginName = pluginPref.getProperty("pluginName");
                        Common.getPlugin(pluginName, new File_S(String.valueOf(System.getProperty("crushftp.plugins")) + "plugins/").toURI().toURL().toExternalForm(), pluginPref.getProperty("subItem", ""));
                        this.setPluginSettings(Common.getPlugin(pluginName, null, pluginPref.getProperty("subItem", "")), this.getPluginPrefs(pluginName, pluginPref));
                        Properties pp = (Properties)pluginPref.clone();
                        pp.put("plugin", Common.getPlugin(pluginName, null, pluginPref.getProperty("subItem", "")));
                        pp.put("pluginName", pluginName);
                        pp.put("subItem", pluginPref.getProperty("subItem", ""));
                        siPluginPrefs.addElement(pp);
                    } else {
                        Properties siPluginPref = (Properties)siPluginPrefs.elementAt(xx);
                        if (!(String.valueOf(pluginPref.getProperty("pluginName")) + "_" + pluginPref.getProperty("subItem", "")).equals(String.valueOf(siPluginPref.getProperty("pluginName")) + "_" + siPluginPref.getProperty("subItem", ""))) {
                            pluginCache.put(String.valueOf(pluginPref.getProperty("pluginName")) + "_" + pluginPref.getProperty("subItem", ""), pluginCache.remove(String.valueOf(siPluginPref.getProperty("pluginName")) + "_" + siPluginPref.getProperty("subItem", "")));
                        }
                        siPluginPref.put("subItem", pluginPref.getProperty("subItem", ""));
                        this.setPluginSettings(siPluginPref.get("plugin"), pluginPref);
                    }
                    ++xx;
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
            ++x;
        }
    }

    public void loadURLPlugins(Properties server_settings, Properties server_info, String url) {
        Vector plugins = (Vector)server_settings.get("plugins");
        if (plugins == null) {
            plugins = new Vector();
        }
        server_settings.put("plugins", plugins);
        Vector<Properties> si_plugins = (Vector<Properties>)server_info.get("plugins");
        if (si_plugins == null) {
            si_plugins = new Vector<Properties>();
        }
        server_info.put("plugins", si_plugins);
        si_plugins.removeAllElements();
        int x = 0;
        while (x < plugins.size()) {
            Vector pluginPrefs = null;
            if (plugins.elementAt(x) instanceof Vector) {
                pluginPrefs = (Vector)plugins.elementAt(x);
            } else {
                pluginPrefs = new Vector();
                pluginPrefs.addElement(plugins.elementAt(x));
            }
            try {
                int xx = 0;
                while (xx < pluginPrefs.size()) {
                    Properties pluginPref = (Properties)pluginPrefs.elementAt(xx);
                    String pluginName = pluginPref.getProperty("pluginName");
                    this.setPluginSettings(Common.getPlugin(pluginName, url, pluginPref.getProperty("subItem", "")), this.getPluginPrefs(pluginName, pluginPref));
                    Properties p = (Properties)pluginPref.clone();
                    p.put("plugin", Common.getPlugin(pluginName, url, pluginPref.getProperty("subItem", "")));
                    p.put("pluginName", pluginName);
                    si_plugins.addElement(p);
                    ++xx;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            ++x;
        }
    }

    public static Object getPlugin(String pluginName, String u, String subItem) throws Exception {
        Object o = pluginCache.get(String.valueOf(pluginName) + "_" + subItem);
        if (o != null) {
            return o;
        }
        if (u == null) {
            return null;
        }
        if (!u.endsWith("/")) {
            u = String.valueOf(u) + "/";
        }
        URL url = new URL(String.valueOf(u) + pluginName + ".jar");
        Class<?> c = null;
        c = System.getProperty("crushftp.jarproxy", "false").equals("true") ? Class.forName(String.valueOf(pluginName) + ".Start") : new URLClassLoader(new URL[]{url}, ServerStatus.clasLoader).loadClass(String.valueOf(pluginName) + ".Start");
        Constructor<?> cons = c.getConstructor(null);
        o = cons.newInstance(null);
        pluginCache.put(String.valueOf(pluginName) + "_" + subItem, o);
        return o;
    }

    public void setPluginSettings(Object o, Properties p) throws Exception {
        Method setSettings = o.getClass().getMethod("setSettings", new Properties().getClass());
        setSettings.invoke(o, p);
    }

    public Properties getPluginSettings(Object o) throws Exception {
        Method getSettings = o.getClass().getMethod("getSettings", null);
        Properties p = (Properties)getSettings.invoke(o, null);
        String pluginName = o.getClass().getName();
        p.put("pluginName", pluginName.substring(0, pluginName.indexOf(".")));
        return p;
    }

    public Properties getPluginPrefs(String pluginName, Properties pluginPrefs) throws Exception {
        String key;
        Properties defaultPrefs = this.getPluginDefaultPrefs(pluginName, pluginPrefs.getProperty("subItem", ""));
        defaultPrefs.put("subItem", "");
        if (pluginPrefs == null) {
            pluginPrefs = defaultPrefs;
        }
        Enumeration<Object> e = defaultPrefs.keys();
        while (e.hasMoreElements()) {
            key = e.nextElement().toString();
            if (pluginPrefs.containsKey(key)) continue;
            pluginPrefs.put(key, defaultPrefs.get(key));
        }
        if (!pluginName.equals("HomeDirectory")) {
            e = pluginPrefs.keys();
            while (e.hasMoreElements()) {
                key = e.nextElement().toString();
                if (defaultPrefs.containsKey(key)) continue;
                pluginPrefs.remove(key);
            }
        }
        return pluginPrefs;
    }

    public Properties getPluginDefaultPrefs(String pluginName, String subItem) throws Exception {
        Object o = Common.getPlugin(pluginName, null, subItem);
        Method getDefaults = o.getClass().getMethod("getDefaults", null);
        Properties p = (Properties)getDefaults.invoke(o, null);
        p.put("pluginName", pluginName);
        return p;
    }

    public static Properties runPlugin(String pluginName, Properties args, String subItem) throws Exception {
        Object o = Common.getPlugin(pluginName, null, subItem);
        if (o != null) {
            Method run = o.getClass().getMethod("run", new Properties().getClass());
            try {
                return (Properties)run.invoke(o, args);
            }
            catch (InvocationTargetException e) {
                Log.log("SERVER", 1, e.getCause());
                throw e;
            }
        }
        return null;
    }

    public static void runOtherPlugins(Properties info, boolean debug, Properties settings) {
        Vector plugins;
        if (settings.getProperty("subItem").indexOf("~") >= 0 && (plugins = (Vector)ServerStatus.server_settings.get("plugins")) != null) {
            int x = 0;
            while (x < plugins.size()) {
                Vector pluginPrefs = null;
                if (plugins.elementAt(x) instanceof Vector) {
                    pluginPrefs = (Vector)plugins.elementAt(x);
                } else {
                    pluginPrefs = new Vector();
                    pluginPrefs.addElement(plugins.elementAt(x));
                }
                int xx = 0;
                while (xx < pluginPrefs.size()) {
                    if (!(pluginPrefs.elementAt(xx) instanceof String)) {
                        Properties pluginPref = (Properties)pluginPrefs.elementAt(xx);
                        String subitem = "";
                        if (settings.getProperty("subItem").split("~").length > 1) {
                            subitem = settings.getProperty("subItem").split("~")[1];
                        }
                        if (pluginPref.getProperty("pluginName").equals(settings.getProperty("subItem").split("~")[0]) && pluginPref.getProperty("subItem").equals(subitem)) {
                            if (debug) {
                                Log.log("PLUGIN", 2, String.valueOf(pluginPref.getProperty("pluginName")) + " : " + pluginPref.getProperty("subItem", ""));
                            }
                            try {
                                info.put("ran_other_plugin", "true");
                                info.put("override_enabled", "true");
                                Common.runPlugin(pluginPref.getProperty("pluginName"), info, pluginPref.getProperty("subItem", ""));
                                info.put("override_enabled", "false");
                            }
                            catch (Exception e) {
                                Log.log("PLUGIN", 1, e);
                            }
                        }
                    }
                    ++xx;
                }
                ++x;
            }
        }
    }

    public Vector getPluginList() {
        Vector<String> v = new Vector<String>();
        if (new File_S(String.valueOf(System.getProperty("crushftp.plugins")) + "plugins/").exists()) {
            String[] list = new File_S(String.valueOf(System.getProperty("crushftp.plugins")) + "plugins/").list();
            int x = 0;
            while (x < list.length) {
                File_S test = new File_S(String.valueOf(System.getProperty("crushftp.plugins")) + "plugins/" + list[x]);
                if (test.getName().toUpperCase().endsWith(".JAR")) {
                    v.addElement(test.getName().substring(0, test.getName().indexOf(".")));
                }
                ++x;
            }
        }
        return v;
    }

    public static void OSXPermissionsGrant() throws Exception {
        RandomAccessFile out = new RandomAccessFile(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + System.getProperty("appname", "CrushFTP").toLowerCase() + "_suid_root.sh"), "rw");
        out.setLength(0L);
        out.write("#! /bin/bash\n".getBytes("UTF8"));
        out.write(("/bin/chmod u+s \"" + new File_S(String.valueOf(System.getProperty("crushftp.home")) + System.getProperty("crushftp.executable")).getCanonicalPath() + "\"\n").getBytes("UTF8"));
        out.write(("/usr/sbin/chown root \"" + new File_S(String.valueOf(System.getProperty("crushftp.home")) + System.getProperty("crushftp.executable")).getCanonicalPath() + "\"\n").getBytes("UTF8"));
        out.close();
        File_S f = new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + System.getProperty("appname", "CrushFTP").toLowerCase() + "_suid_root.sh");
        Common.exec(new String[]{"chmod", "+x", f.getCanonicalPath()});
        Common.exec(new String[]{"osascript", "-e", "do shell script \"" + f.getCanonicalPath() + "\" with administrator privileges"});
        f.delete();
    }

    public static Object readXMLObject(URL url) {
        Object result = null;
        try {
            Document doc = Common.getSaxBuilder().build(url);
            result = Common.getElements(doc.getRootElement());
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
        return result;
    }

    public static Object readXMLObject(InputStream in) throws Exception {
        in = com.crushftp.client.Common.sanitizeXML(in);
        Object result = null;
        try {
            Document doc = Common.getSaxBuilder().build(in);
            result = Common.getElements(doc.getRootElement());
        }
        finally {
            try {
                in.close();
            }
            catch (IOException e) {
                Log.log("SERVER", 1, e);
            }
        }
        return result;
    }

    public static SAXBuilder getSaxBuilder() {
        SAXBuilder sb = new SAXBuilder();
        sb.setExpandEntities(false);
        sb.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return sb;
    }

    public static Object readXMLObjectError(InputStream in) throws Exception {
        in = com.crushftp.client.Common.sanitizeXML(in);
        Object result = null;
        Document doc = Common.getSaxBuilder().build(in);
        result = Common.getElements(doc.getRootElement());
        in.close();
        return result;
    }

    public static Object readXMLObject_U(File file) {
        return Common.readXMLObject_restricted(new File_B(new File_U(file)));
    }

    public static Object readXMLObject(File file) {
        return Common.readXMLObject_restricted(new File_B(new File_S(file)));
    }

    public static Object readXMLObject_restricted(File_B file) {
        Object result = null;
        FileInputStream in = null;
        Exception ee = null;
        if (System.currentTimeMillis() - Long.parseLong(recent_corrupt_users.getProperty("" + file, "0")) < 30000L) {
            Log.log("SERVER", 0, "CORRUPT user.XML file:" + file);
            return null;
        }
        int x = 0;
        while (x < 3) {
            try {
                in = new FileInputStream(file);
                result = Common.readXMLObject(in);
                ee = null;
                recent_corrupt_users.remove("" + file);
                break;
            }
            catch (Exception e) {
                ee = e;
                recent_corrupt_users.put("" + file, String.valueOf(System.currentTimeMillis()));
            }
            finally {
                try {
                    if (in != null) {
                        ((InputStream)in).close();
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 2, e);
                }
            }
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
            ++x;
        }
        if (ee != null) {
            Log.log("SERVER", 0, "" + file);
            Log.log("SERVER", 0, ee);
        }
        return result;
    }

    public String readXMLDocumentAndConvert(URL url, String xslt) {
        Log.log("SERVER", 2, xslt);
        try {
            xslt = new File_S(String.valueOf(System.getProperty("crushftp.web")) + xslt).getAbsolutePath();
            Document doc = Common.getSaxBuilder().build(url);
            XMLOutputter xx = new XMLOutputter();
            Format formatter = Format.getPrettyFormat();
            formatter.setExpandEmptyElements(true);
            formatter.setIndent("\t");
            xx.setFormat(formatter);
            String s = "";
            try (InputStream in = null;){
                if (xslt != null && xslt.length() > 0) {
                    in = new FileInputStream(new File_S(xslt));
                    XSLTransformer transformer = new XSLTransformer(in);
                    doc = transformer.transform(doc);
                }
                s = xx.outputString(doc);
            }
            doc.removeContent();
            doc = null;
            return s;
        }
        catch (Exception e) {
            Log.log("SERVER", 1, e);
            return null;
        }
    }

    public static Object readXMLObject_U(String path) {
        return Common.readXMLObject(new File_B(new File_U(path)));
    }

    public static Object readXMLObject(String path) {
        return Common.readXMLObject(new File_B(new File_S(path)));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Object readXMLObject(File_B f) {
        Properties p;
        String path = null;
        try {
            path = f.getCanonicalPath();
        }
        catch (IOException e) {
            Log.log("SERVER", 0, e);
        }
        Properties e = xmlCache;
        synchronized (e) {
            if (System.currentTimeMillis() - xmlLastCacheClean > 30000L) {
                Enumeration<Object> keys = xmlCache.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement().toString();
                    Properties p2 = (Properties)xmlCache.get(key);
                    if (System.currentTimeMillis() - Long.parseLong(p2.getProperty("time")) <= 60000L) continue;
                    Log.log("SERVER", 3, "Time out: Remove XML from chache. Path: " + path);
                    xmlCache.remove(key);
                }
                xmlLastCacheClean = System.currentTimeMillis();
            }
            if (xmlCache.containsKey(path)) {
                p = (Properties)xmlCache.get(path);
                if (f.exists() && f.lastModified() == Long.parseLong(p.getProperty("modified"))) {
                    Log.log("SERVER", 3, "Found XML in cache. Path: " + path);
                    return com.crushftp.client.Common.CLONE(p.get("object"));
                }
                Log.log("SERVER", 3, "Modified XML. Remove XML from chache. Path: " + path);
                xmlCache.remove(path);
            }
        }
        try {
            if (f.exists()) {
                Log.log("SERVER", 3, "XML exists! Path: " + path);
                Object o = Common.readXMLObject_restricted(new File_B(path));
                if (o != null) {
                    Log.log("SERVER", 3, "Put XML in cache. Path: " + path);
                    p = new Properties();
                    p.put("time", String.valueOf(System.currentTimeMillis()));
                    p.put("modified", String.valueOf(f.lastModified()));
                    p.put("object", com.crushftp.client.Common.CLONE(o));
                    xmlCache.put(path, p);
                }
                return o;
            }
            Log.log("SERVER", 3, "XML does not exists! Path: " + path);
        }
        catch (Exception e2) {
            Log.log("SERVER", 0, "ERROR:" + path);
            Log.log("SERVER", 0, e2);
        }
        return null;
    }

    public static Object getElements(Element element) {
        Cloneable result = null;
        if (element.getAttributeValue("type", "string").equalsIgnoreCase("properties")) {
            result = new Properties();
            List items2 = element.getChildren();
            if (items2.size() == 0) {
                return result;
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
                return result;
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

    public static String getXMLString(Object obj, String root, String xslt) throws Exception {
        return Common.getXMLString(obj, root, xslt, true);
    }

    public static String getXMLString(Object obj, String root, String xslt, boolean pretty) throws Exception {
        if (obj instanceof Properties && !((Properties)obj).getProperty("export", "").equals("true")) {
            xslt = null;
        }
        if (xslt != null && xslt.length() > 0) {
            xslt = new File_S(String.valueOf(System.getProperty("crushftp.web")) + xslt).exists() ? new File_S(String.valueOf(System.getProperty("crushftp.web")) + xslt).getAbsolutePath() : null;
        }
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
        String s = "";
        try (InputStream in = null;){
            if (xslt != null && xslt.length() > 0) {
                in = new FileInputStream(new File_S(xslt));
                XSLTransformer transformer = new XSLTransformer(in);
                doc = transformer.transform(doc);
            }
            s = xx.outputString(doc);
        }
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

    public String transformXML(String xml, String xslt, Properties params) throws Exception {
        Document doc = Common.getSaxBuilder().build(new File_S(xml).toURI().toURL());
        Common.addParameters(doc, params);
        xslt = new File_S(xslt).getAbsolutePath();
        XMLOutputter xx = new XMLOutputter();
        Format formatter = Format.getPrettyFormat();
        formatter.setExpandEmptyElements(true);
        formatter.setIndent("\t");
        xx.setFormat(formatter);
        String s = "";
        if (xslt != null && xslt.length() > 0) {
            XSLTransformer transformer = new XSLTransformer(xslt);
            doc = transformer.transform(doc);
        }
        s = xx.outputString(doc);
        doc.removeContent();
        doc = null;
        return s;
    }

    public static void writeXMLObject(String path, Object obj, String root) throws Exception {
        Common.writeXMLObject(new File_B(new File_S(path)), obj, root);
    }

    public static void writeXMLObject_U(String path, Object obj, String root) throws Exception {
        Common.writeXMLObject(new File_B(new File_U(path)), obj, root);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void writeXMLObject(File_B f, Object obj, String root) throws Exception {
        String xml = Common.getXMLString(obj, root, null);
        String f_path = Common.safe_xss_filename(f.getCanonicalPath());
        Properties properties = xmlCache;
        synchronized (properties) {
            xmlCache.remove(f_path);
            Log.log("SERVER", 2, "Write XML. Remove XML from chache. Path: " + f_path);
        }
        RandomAccessFile eraser = new RandomAccessFile(f_path, "rw");
        eraser.setLength(0L);
        eraser.write(xml.getBytes("UTF8"));
        eraser.close();
        Common.updateOSXInfo(f, "");
    }

    public static String safe_xss_filename(String s) {
        s = s.replace('%', '_').replace('<', '_').replace('>', '_');
        return s;
    }

    public static void updateOSXInfo(String path) {
        Common.updateOSXInfo(path, "");
    }

    public static void updateOSXInfo(String path, String param) {
        Common.updateOSXInfo(new File_B(new File_S(path)), param);
    }

    public static void updateOSXInfo_U(String path, String param) {
        Common.updateOSXInfo(new File_B(new File_U(path)), param);
    }

    private static void updateOSXInfo(File_B f, String param) {
        if (Common.machine_is_windows()) {
            return;
        }
        try {
            Log.log("SERVER", 3, "Changing default owner/group/priv param=" + param + " owner=" + ServerStatus.SG("default_system_owner") + " group=" + ServerStatus.SG("default_system_group") + " path=" + f.getCanonicalPath());
            if (!(!f.exists() || Common.machine_is_windows() || ServerStatus.SG("default_system_owner").equals("") && ServerStatus.SG("default_system_group").equals(""))) {
                GenericClient c = Common.getClient(Common.getBaseUrl(f.toURI().toURL().toString()), "", new Vector());
                if (!ServerStatus.SG("default_system_owner").equals("")) {
                    c.setOwner(f.getCanonicalPath(), ServerStatus.SG("default_system_owner"), param);
                }
                if (!ServerStatus.SG("default_system_group").equals("")) {
                    c.setGroup(f.getCanonicalPath(), ServerStatus.SG("default_system_group"), param);
                }
                c.setMod(f.getCanonicalPath(), "775", param);
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 2, e);
        }
    }

    public static GenericClient getClient(String url, String logHeader, Vector logQueue) {
        try {
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
            if (ServerStatus.BG("v10_beta") && url.toUpperCase().startsWith("SMB1:")) {
                return new SMB1Client(url, logHeader, logQueue);
            }
            if (ServerStatus.BG("v10_beta") && (url.toUpperCase().startsWith("SMB:") || url.toUpperCase().startsWith("SMB2:"))) {
                return new SMB4jClient(url, logHeader, logQueue);
            }
            if (ServerStatus.BG("v10_beta") && url.toUpperCase().startsWith("SMB3:")) {
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
            if (url.toUpperCase().startsWith("AS2:") || url.toUpperCase().indexOf("VFS_AS2") >= 0) {
                return new AS2Client(url, logHeader, logQueue);
            }
            if (url.toUpperCase().startsWith("HADOOP:")) {
                return new HadoopClient(url, logHeader, logQueue);
            }
            if (url.toUpperCase().startsWith("AZURE:")) {
                return new AzureClient(url, logHeader, logQueue);
            }
            if (url.toUpperCase().startsWith("CITRIX:")) {
                return new CitrixClient(url, logHeader, logQueue);
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
            if (url.toUpperCase().startsWith("BOX:")) {
                return new BoxClient(url, logHeader, logQueue);
            }
            if (url.toUpperCase().startsWith("CUSTOM.")) {
                return new CustomClient(url, logHeader, logQueue);
            }
        }
        catch (Throwable e) {
            Log.log("SERVER", 0, e);
        }
        return null;
    }

    public static void addElements(Element element, Object obj) {
        block13: {
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
                    break block13;
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
                    break block13;
                }
                if (!(obj != null && obj instanceof BufferedReader || obj != null && obj instanceof BufferedWriter || obj != null && obj instanceof SessionCrush || obj == null)) {
                    String s = "" + obj;
                    try {
                        element.setText(s);
                    }
                    catch (Exception e) {
                        element.setText(Common.url_encode(s));
                    }
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
        }
    }

    public static void deepClone(Object dest, Object src) {
        block12: {
            block11: {
                if (src == null || !(src instanceof Properties)) break block11;
                Properties p = (Properties)src;
                Enumeration<Object> e = p.keys();
                while (e.hasMoreElements()) {
                    String key = e.nextElement().toString();
                    Object val = p.get(key);
                    if (val instanceof String) {
                        ((Properties)dest).put(key, val);
                        continue;
                    }
                    if (val instanceof Properties) {
                        Properties pp = new Properties();
                        ((Properties)dest).put(key, pp);
                        Common.deepClone(pp, val);
                        continue;
                    }
                    if (val instanceof Vector) {
                        Vector vv = new Vector();
                        ((Properties)dest).put(key, vv);
                        Common.deepClone(vv, val);
                        continue;
                    }
                    ((Properties)dest).put(key, val);
                }
                break block12;
            }
            if (src == null || !(src instanceof Vector)) break block12;
            Vector v = (Vector)src;
            int x = 0;
            while (x < v.size()) {
                Object val = v.elementAt(x);
                if (val instanceof String) {
                    ((Vector)dest).addElement(val);
                } else if (val instanceof Properties) {
                    Properties pp = new Properties();
                    ((Vector)dest).addElement(pp);
                    Common.deepClone(pp, val);
                } else if (val instanceof Vector) {
                    Vector vv = new Vector();
                    ((Vector)dest).addElement(vv);
                    Common.deepClone(vv, val);
                } else {
                    ((Vector)dest).addElement(val);
                }
                ++x;
            }
        }
    }

    public static String checkPasswordRequirements(String pass, String history, Properties password_rules) {
        if (pass.startsWith("MD5:") || pass.startsWith("MD5S2:") || pass.startsWith("MD4:") || pass.startsWith("SHA:") || pass.startsWith("SHA512:") || pass.startsWith("SHA256:") || pass.startsWith("SHA3:") || pass.startsWith("ARGOND:")) {
            return "";
        }
        String msg = "";
        if (pass.length() < Integer.parseInt(password_rules.getProperty("min_password_length"))) {
            msg = String.valueOf(msg) + "Password must have at least " + password_rules.getProperty("min_password_length") + " characters.\r\nPASS_LEN:" + password_rules.getProperty("min_password_length") + "\r\n";
        }
        String chars = "0123456789";
        int count = 0;
        int x = 0;
        while (x < chars.length()) {
            count += Common.count_str(pass, String.valueOf(chars.charAt(x)));
            ++x;
        }
        if (count < Integer.parseInt(password_rules.getProperty("min_password_numbers"))) {
            msg = String.valueOf(msg) + "Password must have at least " + password_rules.getProperty("min_password_numbers") + " number characters.\r\nPASS_NUM:" + password_rules.getProperty("min_password_numbers") + "\r\n";
        }
        chars = "abcdefghijklmnopqrstuvwxyz\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd";
        count = 0;
        x = 0;
        while (x < chars.length()) {
            count += Common.count_str(pass, String.valueOf(chars.charAt(x)));
            ++x;
        }
        if (count < Integer.parseInt(password_rules.getProperty("min_password_lowers"))) {
            msg = String.valueOf(msg) + "Password must have at least " + password_rules.getProperty("min_password_lowers") + " lower case characters.\r\nPASS_LOW:" + password_rules.getProperty("min_password_lowers") + "\r\n";
        }
        chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\ufffd\u0344";
        count = 0;
        x = 0;
        while (x < chars.length()) {
            count += Common.count_str(pass, String.valueOf(chars.charAt(x)));
            ++x;
        }
        if (count < Integer.parseInt(password_rules.getProperty("min_password_uppers"))) {
            msg = String.valueOf(msg) + "Password must have at least " + password_rules.getProperty("min_password_uppers") + " upper case characters.\r\nPASS_UP:" + password_rules.getProperty("min_password_uppers") + "\r\n";
        }
        chars = "!@#$%^&*()_+=-{}][|:;?<>,.~";
        count = 0;
        x = 0;
        while (x < chars.length()) {
            count += Common.count_str(pass, String.valueOf(chars.charAt(x)));
            ++x;
        }
        if (count < Integer.parseInt(password_rules.getProperty("min_password_specials"))) {
            msg = String.valueOf(msg) + "Password must have at least " + password_rules.getProperty("min_password_specials") + " special characters.\r\nPASS_SPEC:" + password_rules.getProperty("min_password_specials") + "\r\n";
        }
        chars = password_rules.getProperty("unsafe_password_chars");
        count = 0;
        x = 0;
        while (x < chars.length()) {
            count += Common.count_str(pass, String.valueOf(chars.charAt(x)));
            ++x;
        }
        if (count > 0) {
            msg = String.valueOf(msg) + "Password cannot contain URL unsafe chars: " + password_rules.getProperty("unsafe_password_chars") + "\r\nPASS_URL:" + password_rules.getProperty("unsafe_password_chars") + "\r\n";
        }
        try {
            String md5 = Common.getMD5(new ByteArrayInputStream(("crushftp" + pass).getBytes("UTF8")));
            if (history.toUpperCase().indexOf(md5.toUpperCase()) >= 0) {
                msg = String.valueOf(msg) + "Password cannot be one of your recent passwords.\r\nPASS_REC:0\r\n";
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return msg;
    }

    public static boolean checkPasswordBlacklisted(String pass) {
        GenericClient c;
        Vector<String> v2;
        block11: {
            String the_url = ServerStatus.SG("password_blacklist");
            String r1 = "{";
            String r2 = "}";
            v2 = new Vector<String>();
            try {
                if (the_url.indexOf("working_dir") >= 0) {
                    the_url = Common.replace_str(the_url, String.valueOf(r1) + "working_dir" + r2, String.valueOf(new File_S("./").getCanonicalPath().replace('\\', '/')) + "/");
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
            VRL vrl = new VRL(the_url);
            c = null;
            try {
                c = Common.getClient(Common.getBaseUrl(vrl.toString()), "black-password:", new Vector());
                c.login(vrl.getUsername(), vrl.getPassword(), "");
                Properties stat = c.stat(vrl.getPath());
                if (stat == null) break block11;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(c.download(vrl.getPath(), 0L, -1L, true)));){
                    String data = "";
                    while ((data = br.readLine()) != null) {
                        if (data.trim().startsWith("#")) continue;
                        v2.addElement(data.trim().toLowerCase());
                    }
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
        }
        try {
            c.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return v2.indexOf(pass.trim().toLowerCase()) >= 0;
    }

    public static String getPasswordHistory(String pass, String history, Properties password_rules) {
        String newHistory = "";
        try {
            String md5 = Common.getMD5(new ByteArrayInputStream(("crushftp" + pass).getBytes("UTF8")));
            if (history.indexOf(md5) < 0) {
                history = String.valueOf(md5) + "," + history;
            }
            int x = 0;
            while (x < Integer.parseInt(password_rules.getProperty("password_history_count")) && x < history.split(",").length) {
                newHistory = String.valueOf(newHistory) + history.split(",")[x] + ",";
                ++x;
            }
            if (newHistory.length() > 0) {
                newHistory = newHistory.substring(0, newHistory.length() - 1);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return newHistory;
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

    private static boolean isSymbolicLink_OFF(String link_name) {
        return Common.isSymbolicLink(new File_B(new File_S(link_name)));
    }

    public static boolean isSymbolicLink_U(String link_name) {
        return Common.isSymbolicLink(new File_B(new File_U(link_name)));
    }

    private static boolean isSymbolicLink(File_B f) {
        if (Common.machine_is_windows()) {
            return false;
        }
        try {
            if (!f.getAbsolutePath().equals(f.getCanonicalPath())) {
                return true;
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return false;
    }

    public static void recurseDelete_U(String real_path, boolean test_mode) {
        Common.recurseDelete(new File_B(new File_U(real_path)), test_mode);
    }

    public static void recurseDelete(String real_path, boolean test_mode) {
        Common.recurseDelete(new File_B(new File_S(real_path)), test_mode);
    }

    private static void recurseDelete(File_B f, boolean test_mode) {
        String real_path = f.getPath();
        try {
            if ((new File_B(real_path).getCanonicalPath().equals(new File_B(System.getProperty("crushftp.prefs")).getCanonicalPath()) || new File_B(real_path).getCanonicalPath().equals(new File_B(System.getProperty("crushftp.home")).getCanonicalPath()) || new File_B(real_path).getCanonicalPath().equals(new File_S("./").getCanonicalPath())) && new File_B(real_path).getCanonicalPath().indexOf(String.valueOf(System.getProperty("appname", "CrushFTP")) + "_temp") < 0) {
                Log.log("SERVER", 0, new Exception("Invalid delete attempted!"));
                return;
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
            return;
        }
        if (real_path.trim().equals("/")) {
            return;
        }
        if (real_path.trim().equals("~")) {
            return;
        }
        if (real_path.indexOf(":") >= 0 && real_path.length() < 4) {
            return;
        }
        f = new File_B(real_path);
        try {
            real_path = String.valueOf(f.getCanonicalPath()) + "/";
            f = new File_B(real_path);
        }
        catch (Exception e) {
            // empty catch block
        }
        if (f.isDirectory()) {
            String[] files = f.list();
            int x = 0;
            while (x < files.length) {
                File_B f2 = new File_B(String.valueOf(real_path) + files[x]);
                if (!Common.isSymbolicLink(f2)) {
                    if (f2.isDirectory()) {
                        Common.recurseDelete(new File_B(String.valueOf(real_path) + files[x] + "/"), test_mode);
                    }
                    if (test_mode) {
                        Log.log("SERVER", 0, "*****************" + LOC.G("DELETE") + ":" + f2);
                    } else {
                        f2.delete();
                    }
                }
                ++x;
            }
        }
        if (test_mode) {
            Log.log("SERVER", 0, "*****************" + LOC.G("DELETE") + ":" + f);
        } else {
            f.delete();
        }
    }

    public static void recurseDelete(VRL vrl1, boolean test_mode, GenericClient c1, int depth) throws Exception {
        if (vrl1.getProtocol().equalsIgnoreCase("file") && vrl1.getPath().trim().equals("/")) {
            return;
        }
        if (vrl1.getProtocol().equalsIgnoreCase("file") && vrl1.getPath().trim().equals("~")) {
            return;
        }
        if (vrl1.getProtocol().equalsIgnoreCase("file") && vrl1.getPath().indexOf(":") >= 0 && vrl1.getPath().length() < 4) {
            return;
        }
        boolean close1 = false;
        if (c1 == null) {
            c1 = Common.getClient(Common.getBaseUrl(vrl1.toString()), "", com.crushftp.client.Common.log);
            close1 = true;
        }
        if (c1.stat(vrl1.getPath()).getProperty("type").equalsIgnoreCase("DIR")) {
            Vector list = new Vector();
            c1.list(vrl1.getPath(), list);
            int x = 0;
            while (x < list.size()) {
                Properties p2 = (Properties)list.elementAt(x);
                if (p2.getProperty("type").equalsIgnoreCase("DIR")) {
                    Common.recurseDelete(new VRL(String.valueOf(vrl1.toString()) + p2.getProperty("name") + "/"), test_mode, c1, depth + 1);
                }
                if (test_mode) {
                    Log.log("SERVER", 0, "*****************" + LOC.G("DELETE") + ":" + vrl1.getProtocol() + "://" + vrl1.getUsername() + "@" + vrl1.getHost() + "/" + vrl1.getPath() + p2.getProperty("name"));
                } else {
                    c1.delete(String.valueOf(vrl1.getPath()) + p2.getProperty("name"));
                }
                ++x;
            }
        }
        if (test_mode) {
            Log.log("SERVER", 0, "*****************" + LOC.G("DELETE") + ":" + vrl1.getProtocol() + "://" + vrl1.getUsername() + "@" + vrl1.getHost() + "/" + vrl1.getPath());
        } else {
            c1.delete(vrl1.getPath());
        }
        if (depth == 0 && close1) {
            c1.logout();
            c1.close();
        }
    }

    public static long recurseSize_U(String real_path, long size, SessionCrush theSession) {
        return Common.recurseSize(new File_B(new File_U(real_path)), size, theSession);
    }

    private static long recurseSize(String real_path, long size, SessionCrush theSession) {
        return Common.recurseSize(new File_B(new File_S(real_path)), size, theSession);
    }

    private static long recurseSize(File_B f, long size, SessionCrush theSession) {
        String real_path = null;
        try {
            real_path = f.getCanonicalPath();
            if (theSession != null && !theSession.not_done && !theSession.uiSG("user_protocol").toUpperCase().startsWith("HTTP")) {
                throw new RuntimeException("CrushSession is dead...");
            }
            if (real_path.trim().equals("/")) {
                return size;
            }
            if (real_path.trim().equals("~")) {
                return size;
            }
            if (real_path.indexOf(":") >= 0 && real_path.length() < 4) {
                return size;
            }
            f = new File_B(real_path);
            real_path = String.valueOf(f.getCanonicalPath()) + "/";
            f = new File_B(real_path);
        }
        catch (IOException e) {
            System.out.println(new Date());
            e.printStackTrace();
        }
        if (f.isDirectory()) {
            String[] files = f.list();
            int x = 0;
            while (x < files.length) {
                File_B f2 = new File_B(String.valueOf(real_path) + files[x]);
                if (!Common.isSymbolicLink(f2)) {
                    size = f2.isDirectory() ? Common.recurseSize(new File_B(String.valueOf(real_path) + files[x] + "/"), size, theSession) : (size += f2.length());
                }
                ++x;
            }
        }
        return size += f.length();
    }

    public static long recurseSizeOfS3Crush(File_S f, long size, SessionCrush theSession) {
        String real_path = null;
        try {
            real_path = f.getCanonicalPath();
            if (theSession != null && !theSession.not_done) {
                throw new RuntimeException("Session is dead...");
            }
            if (real_path.trim().equals("/")) {
                return size;
            }
            if (real_path.trim().equals("~")) {
                return size;
            }
            if (real_path.indexOf(":") >= 0 && real_path.length() < 4) {
                return size;
            }
            f = new File_S(real_path);
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
                if (!Common.isSymbolicLink(new File_B(f2))) {
                    if (f2.isDirectory()) {
                        size = Common.recurseSizeOfS3Crush(f2, size, theSession);
                    } else {
                        try {
                            Properties s3Crush_file = (Properties)Common.readXMLObject(f2);
                            if (s3Crush_file.containsKey("size")) {
                                size += Long.parseLong(s3Crush_file.getProperty("size", "0"));
                            }
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 2, e);
                        }
                    }
                }
                ++x;
            }
        }
        return size += f.length();
    }

    public static void getAllFileListing(Vector list, String path, int depth, boolean includeFolders) throws Exception {
        File_S item = new File_S(path);
        if (item.isFile()) {
            list.addElement(item);
        } else {
            Common.appendListing(path, list, "", depth, includeFolders);
        }
    }

    public static void getAllFileListing_U(Vector list, String path, int depth, boolean includeFolders) throws Exception {
        File_U item = new File_U(path);
        if (item.isFile()) {
            list.addElement(item);
        } else {
            Common.appendListing_U(path, list, "", depth, includeFolders);
        }
    }

    public static void appendListing(String path, Vector list, String dir, int depth, boolean includeFolders) throws Exception {
        Common.appendListing(path, list, dir, depth, includeFolders, true);
    }

    public static void appendListing_U(String path, Vector list, String dir, int depth, boolean includeFolders) throws Exception {
        Common.appendListing(path, list, dir, depth, includeFolders, false);
    }

    private static void appendListing(String path, Vector list, String dir, int depth, boolean includeFolders, boolean is_server_File) throws Exception {
        String[] items;
        if (depth == 0) {
            return;
        }
        --depth;
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        if ((items = is_server_File ? new File_S(String.valueOf(path) + dir).list() : new File_U(String.valueOf(path) + dir).list()) == null) {
            return;
        }
        int x = 0;
        while (x < items.length) {
            File item = is_server_File ? new File_S(String.valueOf(path) + dir + items[x]) : new File_U(String.valueOf(path) + dir + items[x]);
            if (item.isFile() || includeFolders) {
                list.addElement(item);
            }
            if (item.isDirectory()) {
                Common.appendListing(path, list, String.valueOf(dir) + items[x] + "/", depth, includeFolders, is_server_File);
            }
            ++x;
        }
        if (items.length == 0) {
            if (is_server_File) {
                list.addElement(new File_S(String.valueOf(path) + dir));
            } else {
                list.addElement(new File_U(String.valueOf(path) + dir));
            }
        }
    }

    public static void updateObjectLogOnly(Object source, String path, StringBuffer log_summary) {
        Vector<String> log = new Vector<String>();
        log.addElement(String.valueOf(path) + " " + source);
        Common.runUpdateObjectAlert(log, "");
        while (log.size() > 0) {
            String s = "" + log.remove(0);
            Log.log("SERVER", 0, "SETTINGS:" + s);
            log_summary.append(s).append("<br/>\r\n");
        }
    }

    public static void updateObjectLog(Object source, Object dest, StringBuffer log_summary) {
        Common.updateObjectLog(source, dest, log_summary, false);
    }

    public static void updateObjectLog(Object source, Object dest, StringBuffer log_summary, boolean no_log) {
        Vector log = new Vector();
        Common.updateObjectLog(source, dest, "", log, true, log_summary);
        if (!no_log) {
            while (log.size() > 0) {
                Log.log("SERVER", 0, "SETTINGS:" + log.remove(0));
            }
        }
    }

    public static void updateObjectLog(Object source, Object dest, String path, boolean update, StringBuffer log_summary) {
        Vector log = new Vector();
        Common.updateObjectLog(source, dest, path, log, update, log_summary);
        Common.runUpdateObjectAlert(log, path);
        while (log.size() > 0) {
            Log.log("SERVER", 0, "SETTINGS:" + log.remove(0));
        }
    }

    public static void updateObjectLog(Object source, Object dest, String path, Vector log, boolean update, StringBuffer log_summary) {
        block22: {
            block21: {
                if (!(source instanceof Properties)) break block21;
                Enumeration<?> the_list = ((Properties)source).propertyNames();
                while (the_list.hasMoreElements()) {
                    String cur = the_list.nextElement().toString();
                    Object sourceO = ((Properties)source).get(cur);
                    Object destO = ((Properties)dest).get(cur);
                    if (destO == null || destO instanceof String) {
                        if (destO == null || destO != null && !destO.toString().equals(sourceO.toString())) {
                            String logged_item1 = "" + sourceO;
                            String logged_item2 = "" + destO;
                            if (cur.equals("password")) {
                                logged_item1 = "*********";
                            }
                            if (cur.equals("password")) {
                                logged_item2 = "*********";
                            }
                            if (cur.equals("url")) {
                                try {
                                    logged_item1 = new VRL(logged_item1).safe();
                                    logged_item2 = new VRL(logged_item2).safe();
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                            String s = String.valueOf(path) + ":" + cur + ", new=" + logged_item1 + " old=" + logged_item2;
                            if (!cur.endsWith("web_buttons")) {
                                log.addElement(s);
                                if (log_summary != null) {
                                    log_summary.append(s).append("<br/>\r\n");
                                }
                            }
                        }
                        if (!update) continue;
                        ((Properties)dest).put(cur, sourceO);
                        continue;
                    }
                    Common.updateObjectLog(sourceO, destO, String.valueOf(path) + "/" + cur, log, update, log_summary);
                }
                break block22;
            }
            if (!(source instanceof Vector)) break block22;
            while (((Vector)source).size() < ((Vector)dest).size()) {
                Object delO = ((Vector)dest).elementAt(((Vector)dest).size() - 1);
                String s = String.valueOf(path) + ":remove " + (((Vector)dest).size() - 1) + "=" + delO;
                log.addElement(s);
                if (log_summary != null) {
                    log_summary.append(s).append("<br/>\r\n");
                }
                if (!update) break;
                ((Vector)dest).removeElementAt(((Vector)dest).size() - 1);
            }
            int x = 0;
            while (x < ((Vector)source).size()) {
                Object destO;
                Object sourceO;
                if (x > ((Vector)dest).size() - 1) {
                    sourceO = ((Vector)source).elementAt(x);
                    String s = String.valueOf(path) + ":add " + x + "=" + sourceO;
                    log.addElement(s);
                    if (log_summary != null) {
                        log_summary.append(s).append("<br/>\r\n");
                    }
                    if (update) {
                        ((Vector)dest).addElement("");
                        ((Vector)dest).setElementAt(sourceO, x);
                    }
                }
                sourceO = ((Vector)source).elementAt(x);
                Object object = destO = x > ((Vector)dest).size() - 1 ? null : (Object)((Vector)dest).elementAt(x);
                if (destO == null || destO instanceof String) {
                    if (destO != null && !destO.toString().equals(sourceO.toString())) {
                        String s = String.valueOf(path) + ":" + x + "=" + sourceO;
                        log.addElement(s);
                        if (log_summary != null) {
                            log_summary.append(s).append("<br/>\r\n");
                        }
                    }
                    if (update) {
                        ((Vector)dest).setElementAt(sourceO, x);
                    }
                } else {
                    Common.updateObjectLog(sourceO, destO, String.valueOf(path) + "/" + x, log, update, log_summary);
                }
                ++x;
            }
        }
    }

    private static void runUpdateObjectAlert(Vector log, final String path) {
        if (ServerStatus.BG("v10_beta")) {
            try {
                String chgange_log = "";
                int x = 0;
                while (x < log.size()) {
                    chgange_log = String.valueOf(chgange_log) + log.get(x) + "\n";
                    ++x;
                }
                final String update_object_log = chgange_log;
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            Properties info = new Properties();
                            info.put("update_object_log", update_object_log);
                            info.put("update_object_path", path);
                            ServerStatus.thisObj.runAlerts("update_object", info, null, null);
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 1, e);
                        }
                    }
                }, "Run Update Object alert");
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
        }
    }

    public static boolean OSXApp() {
        if (isOSXApp.equals("")) {
            isOSXApp = String.valueOf(new File(System.getProperty("crushftp.executable")).exists() && System.getProperty("crushftp.executable", "").indexOf("/MacOS") >= 0);
        }
        return isOSXApp.equals("true");
    }

    public static void killSystemProperties() {
        System.getProperties().remove("crushftp.home");
        System.getProperties().remove("crushftp.users");
        System.getProperties().remove("crushftp.prefs");
        System.getProperties().remove("crushftp.log");
        System.getProperties().remove("crushftp.plugins");
        System.getProperties().remove("crushftp.web");
        System.getProperties().remove("crushftp.stats");
        System.getProperties().remove("crushftp.sync");
        System.getProperties().remove("crushftp.search");
        System.getProperties().remove("crushftp.backup");
    }

    public static void initSystemProperties(boolean osxAppOK) {
        System.setProperty("crushftp.executable", "../../MacOS/" + System.getProperty("appname", "CrushFTP") + ".command");
        if (!new File(System.getProperty("crushftp.executable")).exists()) {
            System.setProperty("crushftp.executable", "./" + System.getProperty("appname", "CrushFTP") + ".command");
        }
        System.setProperty("crushftp.osxprefix", "../../../../");
        String backupLocation = "/Library/Application Support/" + System.getProperty("appname", "CrushFTP") + "/";
        if (Common.OSXApp()) {
            new File(backupLocation).mkdirs();
            if (!new File(backupLocation).exists()) {
                backupLocation = String.valueOf(System.getProperty("user.home")) + backupLocation;
            }
        }
        if (System.getProperty("crushftp.home") == null) {
            System.setProperty("crushftp.home", "./");
        }
        System.setProperty("sshtools.home", System.getProperty("crushftp.home"));
        if (Common.OSXApp() && osxAppOK && new File(String.valueOf(System.getProperty("crushftp.home")) + System.getProperty("crushftp.osxprefix") + "plugins/").exists()) {
            new File(String.valueOf(System.getProperty("crushftp.home")) + System.getProperty("crushftp.osxprefix") + "plugins/").renameTo(new File(String.valueOf(System.getProperty("crushftp.home")) + "plugins/"));
        }
        if (System.getProperty("crushftp.users") == null) {
            if (Common.OSXApp() && osxAppOK) {
                System.setProperty("crushftp.users", String.valueOf(System.getProperty("crushftp.home")) + System.getProperty("crushftp.osxprefix") + "users/");
            } else {
                System.setProperty("crushftp.users", String.valueOf(System.getProperty("crushftp.home")) + "users/");
            }
        }
        if (System.getProperty("crushftp.prefs") == null) {
            if (Common.OSXApp() && osxAppOK) {
                System.setProperty("crushftp.prefs", String.valueOf(System.getProperty("crushftp.home")) + System.getProperty("crushftp.osxprefix"));
            } else {
                System.setProperty("crushftp.prefs", System.getProperty("crushftp.home"));
            }
        }
        if (System.getProperty("crushftp.log") == null) {
            if (Common.OSXApp() && osxAppOK) {
                System.setProperty("crushftp.log", String.valueOf(System.getProperty("crushftp.home")) + System.getProperty("crushftp.osxprefix") + System.getProperty("appname", "CrushFTP") + ".log");
            } else {
                System.setProperty("crushftp.log", String.valueOf(System.getProperty("crushftp.home")) + System.getProperty("appname", "CrushFTP") + ".log");
            }
        }
        if (System.getProperty("crushftp.plugins") == null) {
            if (Common.OSXApp() && osxAppOK) {
                System.setProperty("crushftp.plugins", System.getProperty("crushftp.home"));
            } else {
                System.setProperty("crushftp.plugins", System.getProperty("crushftp.home"));
            }
        }
        if (System.getProperty("crushftp.web") == null) {
            if (Common.OSXApp() && osxAppOK) {
                System.setProperty("crushftp.web", String.valueOf(System.getProperty("crushftp.home")) + System.getProperty("crushftp.osxprefix"));
            } else {
                System.setProperty("crushftp.web", System.getProperty("crushftp.home"));
            }
        }
        if (System.getProperty("crushftp.stats") == null) {
            if (Common.OSXApp() && osxAppOK) {
                System.setProperty("crushftp.stats", String.valueOf(System.getProperty("crushftp.home")) + System.getProperty("crushftp.osxprefix"));
            } else {
                System.setProperty("crushftp.stats", System.getProperty("crushftp.home"));
            }
        }
        if (System.getProperty("crushftp.sync") == null) {
            if (Common.OSXApp() && osxAppOK) {
                System.setProperty("crushftp.sync", String.valueOf(System.getProperty("crushftp.home")) + System.getProperty("crushftp.osxprefix"));
            } else {
                System.setProperty("crushftp.sync", System.getProperty("crushftp.home"));
            }
        }
        if (System.getProperty("crushftp.search") == null) {
            if (Common.OSXApp() && osxAppOK) {
                System.setProperty("crushftp.search", String.valueOf(System.getProperty("crushftp.home")) + System.getProperty("crushftp.osxprefix"));
            } else {
                System.setProperty("crushftp.search", System.getProperty("crushftp.home"));
            }
        }
        if (System.getProperty("crushftp.backup") == null) {
            if (Common.OSXApp() && osxAppOK) {
                System.setProperty("crushftp.backup", backupLocation);
            } else {
                System.setProperty("crushftp.backup", System.getProperty("crushftp.home"));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void write_server_settings(Properties prefs, String instance) throws Exception {
        Object object = writeLock;
        synchronized (object) {
            if (instance == null || instance.equals("")) {
                instance = "";
            } else if (!instance.startsWith("_")) {
                instance = "_" + instance;
            }
            new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/").mkdirs();
            if (new Date().getTime() - lastPrefBackup > 300000L) {
                new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/prefs" + instance + "199.XML").delete();
                int x = 198;
                while (x >= 0) {
                    try {
                        new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/prefs" + instance + x + ".XML").renameTo(new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/prefs" + instance + (x + 1) + ".XML"));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    --x;
                }
                lastPrefBackup = new Date().getTime();
            }
            new Common();
            Common.writeXMLObject(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs" + instance + ".saved.XML", (Object)prefs, "server_prefs");
            Common.copy(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs" + instance + ".XML", String.valueOf(System.getProperty("crushftp.backup")) + "backup/prefs" + instance + "0.XML", true);
            new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs" + instance + ".XML").delete();
            int loops = 0;
            while (!new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs" + instance + ".saved.XML").renameTo(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs" + instance + ".XML")) && loops++ < 100) {
                Thread.sleep(100L);
            }
            Common.updateOSXInfo(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs" + instance + ".XML");
            Common.updateOSXInfo(String.valueOf(System.getProperty("crushftp.backup")) + "backup/prefs" + instance + "0.XML");
        }
    }

    public static void startMultiThreadZipper(VFS uVFS, RETR_handler retr, String path, int msDelay, boolean singleThread, Vector activeThreads) throws Exception {
        if (singleThread) {
            try {
                uVFS.getListing(retr.zipFiles, path, 999, 10000, true, null, null, true);
            }
            catch (Exception exception) {
                // empty catch block
            }
            retr.zipping = true;
        } else {
            class MultithreadZip
            implements Runnable {
                String other_the_dir = null;
                RETR_handler retr = null;
                VFS uVFS = null;
                private final /* synthetic */ Vector val$activeThreads;

                public MultithreadZip(VFS uVFS, RETR_handler retr, String other_the_dir, Vector vector) {
                    this.val$activeThreads = vector;
                    this.other_the_dir = other_the_dir;
                    this.retr = retr;
                    this.uVFS = uVFS;
                }

                @Override
                public void run() {
                    try {
                        this.uVFS.getListing(this.retr.zipFiles, this.other_the_dir, 999, 500, true, null, null, true);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    this.val$activeThreads.remove(this);
                }
            }
            MultithreadZip mz = new MultithreadZip(uVFS, retr, path, activeThreads);
            activeThreads.addElement(mz);
            Worker.startWorker(mz);
            Thread.sleep(msDelay);
            retr.activeZipThreads = activeThreads;
            retr.zipping = true;
        }
    }

    public static void unzip(String path, GenericClient c, SessionCrush thisSession, String basePath) throws Exception {
        String path1 = Common.all_but_last(path);
        ZipInputStream zin = new ZipInputStream(new FileInputStream(new File_S(path)));
        try {
            ZipEntry entry;
            Vector<String> folders = new Vector<String>();
            while ((entry = zin.getNextEntry()) != null) {
                String path2 = entry.getName();
                path2 = path2.replace('\\', '/');
                path2 = path2.replace('\\', '/');
                if ((path2 = com.crushftp.client.Common.dots(path2)).startsWith("/")) {
                    path2 = path2.substring(1);
                }
                if (entry.isDirectory()) {
                    new File_S(String.valueOf(path1) + path2).mkdirs();
                    folders.addElement(String.valueOf(entry.getTime()) + ":" + path1 + path2);
                    thisSession.setFolderPrivs(c, thisSession.uVFS.get_item(String.valueOf(basePath) + path2));
                    continue;
                }
                new File_S(Common.all_but_last(String.valueOf(path1) + path2)).mkdirs();
                thisSession.setFolderPrivs(c, thisSession.uVFS.get_item(Common.all_but_last(String.valueOf(basePath) + path2)));
                byte[] b = new byte[32768];
                int bytes_read = 0;
                try (RandomAccessFile out = null;){
                    out = new RandomAccessFile(new File_S(String.valueOf(path1) + path2), "rw");
                    while (bytes_read >= 0) {
                        bytes_read = zin.read(b);
                        if (bytes_read <= 0) continue;
                        out.write(b, 0, bytes_read);
                    }
                }
                thisSession.setFolderPrivs(c, thisSession.uVFS.get_item(String.valueOf(basePath) + path2));
                boolean disable_mdtm_modifications = ServerStatus.BG("disable_mdtm_modifications");
                if (thisSession.user.containsKey("disable_mdtm_modifications")) {
                    disable_mdtm_modifications = thisSession.BG("disable_mdtm_modifications");
                }
                if (disable_mdtm_modifications) continue;
                new File_S(String.valueOf(path1) + path2).setLastModified(entry.getTime());
            }
            boolean disable_mdtm_modifications = ServerStatus.BG("disable_mdtm_modifications");
            if (thisSession.user.containsKey("disable_mdtm_modifications")) {
                disable_mdtm_modifications = thisSession.BG("disable_mdtm_modifications");
            }
            if (!disable_mdtm_modifications) {
                int x = folders.size() - 1;
                while (x >= 0) {
                    String s = folders.elementAt(x).toString();
                    new File_S(s.substring(s.indexOf(":") + 1)).setLastModified(Long.parseLong(s.substring(0, s.indexOf(":"))));
                    --x;
                }
            }
        }
        catch (Exception e) {
            zin.close();
            throw e;
        }
        zin.close();
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

    public void purgeOldBackups(int count) {
        try {
            String root = String.valueOf(System.getProperty("crushftp.backup")) + "backup/";
            String[] list = new File_S(root).list();
            if (list == null) {
                return;
            }
            if (list.length < count) {
                return;
            }
            int deletedCount = 0;
            int x = 0;
            while (x < list.length) {
                File_S f = new File_S(String.valueOf(root) + list[x]);
                String name = f.getName();
                if (name.startsWith("users-")) {
                    String date = name.substring(name.indexOf("-") + 1, name.indexOf("_"));
                    SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy", Locale.US);
                    if (new Date().getTime() - sdf.parse(date).getTime() > 604800000L) {
                        if (list.length - deletedCount < count) {
                            return;
                        }
                        ++deletedCount;
                        Common.recurseDelete(f.getAbsolutePath(), false);
                    }
                }
                ++x;
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 1, e);
        }
    }

    public static Properties removeNonStrings(Properties p) {
        Enumeration<Object> e = p.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement().toString();
            if (p.get(key) instanceof String) continue;
            p.remove(key);
        }
        return p;
    }

    public static Properties setupReportDates(Properties params, String show, String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
        if (!show.equals("")) {
            GregorianCalendar calendar;
            if (show.toUpperCase().endsWith("DAY") || show.toUpperCase().endsWith("DAYS")) {
                if (show.equalsIgnoreCase("LAST DAY")) {
                    show = "Last 1 day";
                }
                show = show.substring(show.indexOf(" ") + 1, show.lastIndexOf(" "));
                int days = Integer.parseInt(show);
                calendar = new GregorianCalendar();
                calendar.setTime(new Date());
                ((Calendar)calendar).add(5, -1 * days);
                params.put("startDate", sdf.format(calendar.getTime()));
            }
            if (show.toUpperCase().endsWith("HOUR") || show.toUpperCase().endsWith("HOURS")) {
                if (show.equalsIgnoreCase("LAST HOUR")) {
                    show = "Last 1 hour";
                }
                show = show.substring(show.indexOf(" ") + 1, show.lastIndexOf(" "));
                int hours = Integer.parseInt(show);
                calendar = new GregorianCalendar();
                calendar.setTime(new Date());
                ((Calendar)calendar).add(10, -1 * hours);
                params.put("startDate", sdf.format(calendar.getTime()));
            }
        } else {
            params.put("startDate", startDate);
        }
        if (endDate.trim().equals("")) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(new Date());
            ((Calendar)calendar).add(5, 1);
            endDate = sdf.format(calendar.getTime());
        }
        params.put("endDate", endDate);
        return params;
    }

    public static boolean haveWriteAccess() {
        File_S f = new File_S(String.valueOf(System.getProperty("crushftp.home")) + "/writeable.tmp");
        try {
            RandomAccessFile ra = new RandomAccessFile(f, "rw");
            ra.close();
        }
        catch (Exception ra) {
            // empty catch block
        }
        boolean ok = f.exists();
        f.delete();
        return ok;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Socket getSTORSocket(SessionCrush thisSession, STOR_handler stor_files, String upload_item, boolean httpUpload, String user_dir, boolean random_access, long start_resume_loc, Properties metaInfo, boolean binary) throws Exception {
        Socket local_s = null;
        while (stor_files.active2.getProperty("active", "").equals("true")) {
            Thread.sleep(1L);
        }
        Properties dir_item = null;
        dir_item = thisSession.uVFS.get_item(String.valueOf(user_dir) + upload_item);
        if (dir_item == null) {
            dir_item = thisSession.uVFS.get_item_parent(String.valueOf(user_dir) + upload_item);
        }
        Socket data_sock = null;
        Object object = onserversocklock;
        synchronized (object) {
            int x = 0;
            while (x < 10) {
                try (ServerSocket ss = new ServerSocket(0);){
                    ss.setSoTimeout(5000);
                    data_sock = new Socket("127.0.0.1", ss.getLocalPort());
                    local_s = ss.accept();
                    break;
                }
                ++x;
            }
        }
        if (binary) {
            thisSession.uiPUT("file_transfer_mode", "BINARY");
        } else {
            thisSession.uiPUT("file_transfer_mode", "ASCII");
        }
        stor_files.httpUpload = httpUpload;
        try {
            stor_files.c.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        String the_dir = String.valueOf(user_dir) + upload_item;
        Properties p = new Properties();
        p.put("the_dir", the_dir);
        thisSession.runPlugin("transfer_path", p);
        the_dir = p.getProperty("the_dir", the_dir);
        stor_files.init_vars(the_dir, start_resume_loc, thisSession, dir_item, "STOR", false, random_access, metaInfo, data_sock);
        Worker.startWorker(stor_files);
        return local_s;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Socket getRETRSocket(SessionCrush thisSession, RETR_handler retr_files, long start_resume_loc, String upload_item, boolean httpDownload, boolean binary) throws Exception {
        Socket local_s = null;
        String path = String.valueOf(thisSession.uiSG("current_dir")) + upload_item;
        if (path.indexOf(":filetree") >= 0 && ServerStatus.BG("allow_filetree")) {
            path = path.substring(0, path.indexOf(":filetree"));
        }
        Properties item = thisSession.uVFS.get_item(path);
        VRL otherFile = new VRL(item.getProperty("url"));
        Socket data_sock = null;
        Object object = onserversocklock;
        synchronized (object) {
            int x = 0;
            while (x < 10) {
                try (ServerSocket ss = new ServerSocket(0);){
                    ss.setSoTimeout(5000);
                    data_sock = new Socket("127.0.0.1", ss.getLocalPort());
                    local_s = ss.accept();
                    break;
                }
                ++x;
            }
        }
        if (binary) {
            thisSession.uiPUT("file_transfer_mode", "BINARY");
        } else {
            thisSession.uiPUT("file_transfer_mode", "ASCII");
        }
        try {
            retr_files.c.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        retr_files.data_os = data_sock.getOutputStream();
        retr_files.httpDownload = httpDownload;
        String the_dir = String.valueOf(thisSession.uiSG("current_dir")) + upload_item;
        Properties p = new Properties();
        p.put("the_dir", the_dir);
        thisSession.runPlugin("transfer_path", p);
        the_dir = p.getProperty("the_dir", the_dir);
        retr_files.init_vars(the_dir, start_resume_loc, -1L, thisSession, item, false, "", otherFile, data_sock);
        Worker.startWorker(retr_files);
        return local_s;
    }

    public static Properties getConnectedSockets() throws Exception {
        Properties sockProp = new Properties();
        Socket sock1 = null;
        Socket sock2 = null;
        int x = 0;
        while (x < 10) {
            try (ServerSocket ss = new ServerSocket(0);){
                ss.setSoTimeout(5000);
                sock1 = new Socket("127.0.0.1", ss.getLocalPort());
                sock2 = ss.accept();
                break;
            }
            ++x;
        }
        sockProp.put("sock1", sock1);
        sockProp.put("sock2", sock2);
        return sockProp;
    }

    public static String normalize2(String s) {
        if (ServerStatus.BG("normalize_utf8")) {
            return Normalizer.normalize(s, Normalizer.Form.NFC);
        }
        return s;
    }

    public static void doSign(String certPath, String keystorePass, String jarFile) throws Exception {
    }

    public static void copy(String src, String dst, boolean overwrite) throws Exception {
        Common.copy(new File_B(new File_S(src)), new File_B(new File_S(dst)), overwrite);
    }

    public static void copy_U(String src, String dst, boolean overwrite) throws Exception {
        Common.copy(new File_B(new File_U(src)), new File_B(new File_U(dst)), overwrite);
    }

    private static void copy(File_B src, File_B dst, boolean overwrite) throws Exception {
        if (src.isDirectory()) {
            dst.mkdirs();
            return;
        }
        if (dst.exists() && !overwrite) {
            return;
        }
        RandomAccessFile in = null;
        RandomAccessFile out = null;
        try {
            in = new RandomAccessFile(src, "r");
            out = new RandomAccessFile(dst, "rw");
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
        dst.setLastModified(src.lastModified());
    }

    public static void copy(VRL vrl1, VRL vrl2, GenericClient c1, GenericClient c2, boolean overwrite) throws Exception {
        Properties stat1 = c1.stat(vrl1.getPath());
        Log.log("SERVER", 2, "copy vrl1_path:" + vrl1.getPath());
        Log.log("SERVER", 2, "copy vrl:" + vrl1.safe() + " to " + vrl2.safe());
        Properties stat1_safe = (Properties)stat1.clone();
        if (stat1_safe.containsKey("url")) {
            stat1_safe.put("url", new VRL(stat1_safe.getProperty("url")).safe());
        }
        Log.log("SERVER", 2, "copy stat1:" + stat1_safe);
        if (stat1.getProperty("type").equalsIgnoreCase("DIR")) {
            c2.makedirs(vrl2.getPath());
            return;
        }
        Properties stat2 = c2.stat(vrl2.getPath());
        Properties stat2_safe = null;
        if (stat2 != null) {
            stat2_safe = (Properties)stat2.clone();
        }
        if (stat2_safe != null && stat2_safe.containsKey("url")) {
            stat2_safe.put("url", new VRL(stat2_safe.getProperty("url")).safe());
        }
        Log.log("SERVER", 2, "copy stat2:" + stat2_safe);
        if (stat2 != null && !overwrite) {
            return;
        }
        InputStream in = null;
        OutputStream out = null;
        try {
            in = c1.download(vrl1.getPath(), 0L, -1L, true);
            out = c2.upload(vrl2.getPath(), 0L, true, true);
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
        String uploaded_by = c1.getUploadedByMetadata(vrl1.getPath());
        if (uploaded_by != null && !uploaded_by.equals("")) {
            c2.setConfig("uploaded_by", uploaded_by);
        }
        c2.mdtm(vrl2.getPath(), Long.parseLong(stat1.getProperty("modified")));
    }

    public static void recurseCopyThreaded(String src, String dst, boolean overwrite, boolean move) throws Exception {
        Common.recurseCopyThreaded(new File_B(new File_S(src)), new File_B(new File_S(dst)), overwrite, move);
    }

    public static void recurseCopyThreaded_U(String src, String dst, boolean overwrite, boolean move) throws Exception {
        Common.recurseCopyThreaded(new File_B(new File_U(src)), new File_B(new File_U(dst)), overwrite, move);
    }

    private static void recurseCopyThreaded(File_B f_src, File_B f_dst, boolean overwrite, boolean move) throws Exception {
        class Copier
        implements Runnable {
            private final /* synthetic */ File_B val$f_src;
            private final /* synthetic */ File_B val$f_dst;
            private final /* synthetic */ boolean val$overwrite;
            private final /* synthetic */ boolean val$move;

            Copier(File_B file_B, File_B file_B2, boolean bl, boolean bl2) {
                this.val$f_src = file_B;
                this.val$f_dst = file_B2;
                this.val$overwrite = bl;
                this.val$move = bl2;
            }

            @Override
            public void run() {
                try {
                    Thread.currentThread().setName("recurseCopy:" + this.val$f_src + " -> " + this.val$f_dst);
                    Common.recurseCopy(this.val$f_src, this.val$f_dst, this.val$overwrite);
                    if (this.val$move) {
                        if (this.val$f_src.isDirectory()) {
                            Common.recurseDelete(this.val$f_src, false);
                        } else {
                            this.val$f_src.delete();
                        }
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
            }
        }
        Worker.startWorker(new Copier(f_src, f_dst, overwrite, move));
    }

    public static void recurseCopy(String src, String dst, boolean overwrite) throws Exception {
        Common.recurseCopy(new File_B(new File_S(src)), new File_B(new File_S(dst)), overwrite);
    }

    public static void recurseCopy_U(String src, String dst, boolean overwrite) throws Exception {
        Common.recurseCopy(new File_B(new File_U(src)), new File_B(new File_U(dst)), overwrite);
    }

    public static void recurseCopy(File_B f_src, File_B f_dst, boolean overwrite) throws Exception {
        String[] files;
        String src_real_path = f_src.getCanonicalPath();
        String dst_real_path = f_dst.getCanonicalPath();
        if (src_real_path.trim().equals("/")) {
            return;
        }
        if (src_real_path.trim().equals("~")) {
            return;
        }
        if (src_real_path.indexOf(":") >= 0 && src_real_path.length() < 4) {
            return;
        }
        if (dst_real_path.trim().equals("/")) {
            return;
        }
        if (dst_real_path.trim().equals("~")) {
            return;
        }
        if (dst_real_path.indexOf(":") >= 0 && dst_real_path.length() < 4) {
            return;
        }
        File_B f = new File_B(src_real_path);
        try {
            src_real_path = String.valueOf(f.getCanonicalPath()) + "/";
            f = new File_B(src_real_path);
        }
        catch (Exception exception) {
            // empty catch block
        }
        Common.copy(f, new File_B(dst_real_path), overwrite);
        if (f.isDirectory() && (files = f.list()) != null) {
            if (!dst_real_path.endsWith("/") && !dst_real_path.endsWith("\\")) {
                dst_real_path = String.valueOf(dst_real_path) + "/";
            }
            int x = 0;
            while (x < files.length) {
                File_B f2 = new File_B(String.valueOf(src_real_path) + files[x]);
                if (!Common.isSymbolicLink(f2)) {
                    Common.copy(f2.getCanonicalPath(), String.valueOf(dst_real_path) + files[x], overwrite);
                    if (f2.isDirectory()) {
                        Common.recurseCopy(new File_B(String.valueOf(src_real_path) + files[x] + "/"), new File_B(String.valueOf(dst_real_path) + files[x] + "/"), overwrite);
                    }
                }
                ++x;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void recurseCopy(VRL vrl1, VRL vrl2, GenericClient c1, GenericClient c2, int depth, boolean overwrite, StringBuffer status) throws Exception {
        if (vrl1.getProtocol().equalsIgnoreCase("file") && vrl1.getPath().trim().equals("/")) {
            return;
        }
        if (vrl1.getProtocol().equalsIgnoreCase("file") && vrl1.getPath().trim().equals("~")) {
            return;
        }
        if (vrl1.getProtocol().equalsIgnoreCase("file") && vrl1.getPath().indexOf(":") >= 0 && vrl1.getPath().length() < 4) {
            return;
        }
        if (vrl2.getProtocol().equalsIgnoreCase("file") && vrl2.getPath().trim().equals("/")) {
            return;
        }
        if (vrl2.getProtocol().equalsIgnoreCase("file") && vrl2.getPath().trim().equals("~")) {
            return;
        }
        if (vrl2.getProtocol().equalsIgnoreCase("file") && vrl2.getPath().indexOf(":") >= 0 && vrl2.getPath().length() < 4) {
            return;
        }
        boolean close1 = false;
        boolean close2 = false;
        if (c1 == null) {
            c1 = Common.getClient(Common.getBaseUrl(vrl1.toString()), "", com.crushftp.client.Common.log);
            c1.login(vrl1.getUsername(), vrl1.getPassword(), null);
            close1 = true;
        }
        if (c2 == null) {
            c2 = Common.getClient(Common.getBaseUrl(vrl2.toString()), "", com.crushftp.client.Common.log);
            c2.login(vrl2.getUsername(), vrl2.getPassword(), null);
            close2 = true;
        }
        Common.copy(vrl1, vrl2, c1, c2, overwrite);
        if (c1.stat(vrl1.getPath()).getProperty("type").equalsIgnoreCase("DIR")) {
            Vector list = new Vector();
            StringBuffer stringBuffer = status;
            synchronized (stringBuffer) {
                if (status.toString().equals("CANCELLED")) {
                    throw new Exception("CANCELLED");
                }
                status.setLength(0);
                status.append("Getting list:" + vrl1.getPath()).append("...");
            }
            c1.list(vrl1.getPath().endsWith("/") ? vrl1.getPath() : String.valueOf(vrl1.getPath()) + "/", list);
            int x = 0;
            while (x < list.size()) {
                Properties p1 = (Properties)list.elementAt(x);
                VRL vrl2_copy = new VRL(String.valueOf(Common.url_decode(vrl2.toString())) + p1.getProperty("name"));
                StringBuffer stringBuffer2 = status;
                synchronized (stringBuffer2) {
                    if (status.toString().equals("CANCELLED")) {
                        throw new Exception("CANCELLED");
                    }
                    status.setLength(0);
                    status.append("Copying:" + vrl2.getPath() + p1.getProperty("name")).append("...");
                }
                Common.copy(new VRL(Common.url_decode(p1.getProperty("url"))), vrl2_copy, c1, c2, overwrite);
                if (p1.getProperty("type").equalsIgnoreCase("DIR")) {
                    Common.recurseCopy(new VRL(Common.url_decode(p1.getProperty("url"))), new VRL(String.valueOf(Common.url_decode(vrl2.toString())) + p1.getProperty("name") + "/"), c1, c2, depth + 1, overwrite, status);
                }
                ++x;
            }
        }
        if (depth == 0) {
            if (close1) {
                c1.close();
                c1.logout();
            }
            if (close2) {
                c2.close();
                c2.logout();
            }
        }
    }

    public static boolean filterDir(String path, Vector filters) {
        if (filters == null || path.equals("/")) {
            return true;
        }
        boolean ok = true;
        int x = 0;
        while (x < filters.size()) {
            Properties p = (Properties)filters.elementAt(x);
            String method = p.getProperty("searchPath", "");
            if (method.equals("contains") && path.toUpperCase().indexOf(p.getProperty("path").toUpperCase()) < 0) {
                ok = false;
            }
            if (method.equals("starts with") && !path.toUpperCase().startsWith(p.getProperty("path").toUpperCase())) {
                ok = false;
            }
            if (method.equals("ends with") && !path.toUpperCase().endsWith(p.getProperty("path").toUpperCase())) {
                ok = false;
            }
            if (method.equals("equals") && !path.toUpperCase().equals(p.getProperty("path").toUpperCase())) {
                ok = false;
            }
            ++x;
        }
        return ok;
    }

    public static String exec(String[] c) throws Exception {
        com.crushftp.client.Common.check_exec();
        Process proc = Runtime.getRuntime().exec(c);
        BufferedReader br1 = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String result = "";
        String lastLine = "";
        while ((result = br1.readLine()) != null) {
            if (result.trim().equals("")) continue;
            lastLine = result;
        }
        br1.close();
        proc.waitFor();
        try {
            proc.destroy();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return lastLine;
    }

    public static String free_space(String in_str) {
        while (in_str.indexOf("<FREESPACE>") >= 0) {
            String data = in_str.substring(in_str.indexOf("<FREESPACE>") + "<FREESPACE>".length(), in_str.indexOf("</FREESPACE>"));
            data = com.crushftp.client.Common.format_bytes_short2(Common.get_free_disk_space(data));
            in_str = String.valueOf(in_str.substring(0, in_str.indexOf("<FREESPACE>"))) + data + in_str.substring(in_str.indexOf("</FREESPACE>") + "</FREESPACE>".length());
        }
        return in_str;
    }

    public static long get_free_disk_space(String disk) {
        disk = Common.check_valid_disk_path_chars(disk);
        String line = "";
        String totalData = "";
        try {
            if (Common.machine_is_windows()) {
                if (disk.length() == 1) {
                    disk = String.valueOf(disk) + ":";
                }
                if (!disk.endsWith("\\")) {
                    disk = String.valueOf(disk) + "\\";
                }
                com.crushftp.client.Common.check_exec();
                Process proc = Runtime.getRuntime().exec(new String[]{"cmd", "/C", "dir", disk});
                BufferedReader br1 = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String result = "";
                while ((result = br1.readLine()) != null) {
                    if (result.trim().equals("")) continue;
                    totalData = String.valueOf(totalData) + result + "\r\n";
                    if (result.toLowerCase().indexOf("bytes fr") < 0) continue;
                    line = result.toLowerCase().trim();
                }
                br1.close();
                proc.waitFor();
                try {
                    proc.destroy();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                line = line.toLowerCase();
                line = line.substring(line.lastIndexOf(")") + 1, line.lastIndexOf("bytes")).trim();
                String builder = "";
                int i = 0;
                while (i < line.length()) {
                    if (Character.isDigit(line.charAt(i))) {
                        builder = String.valueOf(builder) + line.charAt(i);
                    }
                    ++i;
                }
                line = builder;
                return Long.parseLong(line);
            }
            if (Common.machine_is_x()) {
                line = Common.exec(new String[]{"df", "-b", disk});
                StringTokenizer st = new StringTokenizer(line);
                st.nextElement();
                st.nextElement();
                st.nextElement();
                long size = Long.parseLong(st.nextElement().toString());
                return size * 512L;
            }
            if (Common.machine_is_solaris()) {
                line = Common.exec(new String[]{"df", "-k", disk});
                StringTokenizer st = new StringTokenizer(line);
                st.nextElement();
                st.nextElement();
                st.nextElement();
                return Long.parseLong(st.nextElement().toString()) * 1024L;
            }
            com.crushftp.client.Common.check_exec();
            Process proc = Runtime.getRuntime().exec(new String[]{"df", disk});
            BufferedReader proc_in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String data = "";
            while ((data = proc_in.readLine()) != null) {
                Log.log("SERVER", 2, data);
                if (data.trim().equals("")) continue;
                line = data;
                totalData = String.valueOf(totalData) + data + "\r\n";
            }
            proc_in.close();
            proc_in = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            data = "";
            while ((data = proc_in.readLine()) != null) {
                Log.log("SERVER", 2, data);
                if (data.trim().equals("")) continue;
                totalData = String.valueOf(totalData) + "ERROR:" + data + "\r\n";
            }
            proc_in.close();
            proc.waitFor();
            StringTokenizer st = new StringTokenizer(line);
            Vector<Object> tokens = new Vector<Object>();
            while (st.hasMoreElements()) {
                tokens.addElement(st.nextElement());
            }
            boolean isnumber = false;
            try {
                Long.parseLong(tokens.elementAt(0).toString().trim());
                isnumber = true;
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (isnumber) {
                tokens.insertElementAt("nothing", 0);
            }
            String device = tokens.remove(0).toString();
            String blocks = tokens.remove(0).toString();
            String used = tokens.remove(0).toString();
            String avail = tokens.remove(0).toString();
            if (avail.indexOf("%") >= 0) {
                avail = used;
            }
            if (avail.endsWith("K")) {
                avail = avail.substring(0, avail.length() - 1);
            }
            long free = Long.parseLong(avail);
            return free * 1024L;
        }
        catch (Exception e) {
            Log.log("SERVER", 1, "Format not understood:" + line);
            Log.log("SERVER", 1, "totalData:" + totalData);
            Log.log("SERVER", 1, e);
            return -1L;
        }
    }

    public static String check_valid_disk_path_chars(String s) {
        boolean ok = true;
        int x = 0;
        while (ok && x < s.length()) {
            if (!(s.charAt(x) >= '.' && s.charAt(x) <= ':' || s.charAt(x) >= '@' && s.charAt(x) <= 'z' || s.charAt(x) == '-' || s.charAt(x) == '(' || s.charAt(x) == ')')) {
                ok = false;
            }
            ++x;
        }
        if (ok) {
            return s;
        }
        return null;
    }

    public static String replaceFormVariables(Properties form_email, String s) {
        Enumeration<Object> keys = form_email.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            s = Common.replace_str(s, key.trim(), com.crushftp.client.Common.dots(form_email.getProperty(key)));
        }
        return s;
    }

    public static Properties buildFormEmail(Properties server_settings, Vector lastUploadStats) {
        Properties form_email = new Properties();
        if (lastUploadStats != null) {
            int xx = 0;
            while (xx < lastUploadStats.size()) {
                String web_upload_form = "";
                Properties uploadStat = (Properties)lastUploadStats.elementAt(xx);
                Properties metaInfo = (Properties)uploadStat.get("metaInfo");
                if (metaInfo != null) {
                    String id = metaInfo.getProperty("UploadFormId", "");
                    Properties customForm = null;
                    Vector customForms = (Vector)server_settings.get("CustomForms");
                    if (customForms != null) {
                        int x = 0;
                        while (x < customForms.size()) {
                            Properties p = (Properties)customForms.elementAt(x);
                            if (p.getProperty("id", "").equals(id)) {
                                customForm = p;
                                break;
                            }
                            ++x;
                        }
                        if (customForm != null) {
                            if (!customForm.containsKey("entries")) {
                                customForm.put("entries", new Vector());
                            }
                            Vector entries = (Vector)customForm.get("entries");
                            int x2 = 0;
                            while (x2 < entries.size()) {
                                Properties p = (Properties)entries.elementAt(x2);
                                if (!p.getProperty("type").equals("label")) {
                                    web_upload_form = String.valueOf(web_upload_form) + p.getProperty("name", "").trim() + ":" + metaInfo.getProperty(p.getProperty("name", "").trim()) + "\r\n\r\n";
                                    String val = metaInfo.getProperty(p.getProperty("name", "").trim());
                                    if (val != null) {
                                        form_email.put("%" + p.getProperty("name", "").trim() + "%", val);
                                    }
                                }
                                ++x2;
                            }
                        } else {
                            web_upload_form = String.valueOf(web_upload_form) + metaInfo;
                            Enumeration<Object> keys = metaInfo.keys();
                            while (keys.hasMoreElements()) {
                                String key = keys.nextElement().toString();
                                String val = metaInfo.getProperty(key);
                                form_email.put("%" + key.trim() + "%", val);
                            }
                        }
                    }
                }
                ++xx;
            }
        }
        return form_email;
    }

    public static void do_sort(Vector listing, String sort_type) {
        Common.do_sort(listing, sort_type, "name");
    }

    public static void do_sort(Vector listing, String sort_type, String key) {
        if (!sort_type.equals("type")) {
            Object[] names = new String[listing.size()];
            int x = 0;
            while (x < listing.size()) {
                names[x] = String.valueOf(((Properties)listing.elementAt(x)).getProperty(key, "")) + ";:;" + x;
                ++x;
            }
            Arrays.sort(names);
            Vector listing2 = (Vector)listing.clone();
            listing.removeAllElements();
            int x2 = 0;
            while (x2 < names.length) {
                listing.addElement(listing2.elementAt(Integer.parseInt(((String)names[x2]).split(";:;")[1])));
                ++x2;
            }
        } else {
            Properties item2;
            int xx;
            Properties item;
            int x = 0;
            while (x < listing.size()) {
                item = (Properties)listing.elementAt(x);
                xx = x;
                while (xx < listing.size()) {
                    item2 = (Properties)listing.elementAt(xx);
                    if (item2.getProperty("type").equals("DIR") && (item2.getProperty("name").toUpperCase().compareTo(item.getProperty("name").toUpperCase()) < 0 || item.getProperty("type").equals("FILE"))) {
                        listing.setElementAt(item2, x);
                        listing.setElementAt(item, xx);
                        item = item2;
                    }
                    ++xx;
                }
                ++x;
            }
            x = 0;
            while (x < listing.size()) {
                item = (Properties)listing.elementAt(x);
                if (item.getProperty("type").equals("FILE")) {
                    xx = x;
                    while (xx < listing.size()) {
                        item2 = (Properties)listing.elementAt(xx);
                        if (item2.getProperty("name").toUpperCase().compareTo(item.getProperty("name").toUpperCase()) < 0) {
                            listing.setElementAt(item2, x);
                            listing.setElementAt(item, xx);
                            item = item2;
                        }
                        ++xx;
                    }
                }
                ++x;
            }
        }
    }

    public static String chop(String dir) {
        if (dir.equals("/")) {
            return "";
        }
        return String.valueOf(dir.substring(0, dir.lastIndexOf("/", dir.length() - 2))) + "/";
    }

    public static String getMD5End(String s) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(s.getBytes("UTF8"));
            s = new BigInteger(1, md5.digest()).toString(16).toUpperCase();
            return s.substring(s.length() - 6, s.length());
        }
        catch (Exception e) {
            Log.log("SERVER", 1, e);
            return null;
        }
    }

    public static String getPartialIp(String ip) {
        String part = "";
        if (ip.indexOf(".") >= 1) {
            int x = 0;
            while (x < ServerStatus.IG("trusted_ip_parts")) {
                part = String.valueOf(part) + ip.split("\\.")[x] + ".";
                ++x;
            }
            if (part.endsWith(".")) {
                part = part.substring(0, part.length() - 1);
            }
        }
        return part;
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

    public static boolean deepNotEquals(Object dest, Object src) {
        boolean result;
        block10: {
            block9: {
                result = false;
                if (src == null || !(src instanceof Properties)) break block9;
                Properties p = (Properties)src;
                Enumeration<Object> e = p.keys();
                while (e.hasMoreElements()) {
                    String key = e.nextElement().toString();
                    Object val = p.get(key);
                    if (val instanceof Properties) {
                        Properties pp = (Properties)((Properties)dest).get(key);
                        result |= Common.deepNotEquals(pp, val);
                        continue;
                    }
                    if (val instanceof Vector) {
                        Vector vv = (Vector)((Properties)dest).get(key);
                        result |= Common.deepNotEquals(vv, val);
                        continue;
                    }
                    if (((Properties)dest).getProperty(key, "").equals(((Properties)src).getProperty(key, ""))) continue;
                    result = true;
                }
                break block10;
            }
            if (src == null || !(src instanceof Vector)) break block10;
            Vector v = (Vector)src;
            int x = 0;
            while (x < v.size()) {
                Object val = v.elementAt(x);
                if (val instanceof Properties) {
                    Properties pp = (Properties)((Vector)dest).elementAt(x);
                    result |= Common.deepNotEquals(pp, val);
                } else if (val instanceof Vector) {
                    Vector vv = (Vector)((Vector)dest).elementAt(x);
                    result |= Common.deepNotEquals(vv, val);
                } else if (!((Vector)dest).elementAt(x).toString().equals(((Vector)src).elementAt(x).toString())) {
                    result = true;
                }
                ++x;
            }
        }
        return result;
    }

    public static void diffObjects(Object o1, Object o2, Vector log, String path, boolean swapOldNew) {
        if (o2 != null && o2 instanceof Properties) {
            Properties p = (Properties)o2;
            Enumeration<Object> e = p.keys();
            while (e.hasMoreElements()) {
                String key = e.nextElement().toString();
                Object val = p.get(key);
                if (val instanceof Properties) {
                    Properties pp = null;
                    try {
                        pp = (Properties)((Properties)o1).get(key);
                    }
                    catch (ClassCastException ee) {
                        log.addElement("key:" + path + "/" + key + "    Unknown Class Difference");
                    }
                    Common.diffObjects(pp, val, log, String.valueOf(path) + "/" + key, swapOldNew);
                    continue;
                }
                if (val instanceof Vector) {
                    Vector vv = null;
                    try {
                        vv = (Vector)((Properties)o1).get(key);
                    }
                    catch (ClassCastException ee) {
                        log.addElement("key:" + path + "/" + key + "    Unknown Class Difference");
                    }
                    Common.diffObjects(vv, val, log, String.valueOf(path) + "/" + key, swapOldNew);
                    continue;
                }
                String val1 = "";
                String val2 = "";
                try {
                    if (o1 != null) {
                        val1 = ((Properties)o1).getProperty(key, "");
                    }
                }
                catch (ClassCastException ee) {
                    log.addElement("key:" + path + "/" + key + "    Unknown Class Difference");
                }
                try {
                    if (o2 != null) {
                        val2 = ((Properties)o2).getProperty(key, "");
                    }
                }
                catch (ClassCastException ee) {
                    log.addElement("key:" + path + "/" + key + "    Unknown Class Difference");
                }
                if (swapOldNew) {
                    String t = val2;
                    val2 = val1;
                    val1 = t;
                }
                if (val1.equals(val2)) continue;
                log.addElement("key:" + path + "/" + key + "    Previous:" + val2 + " New:" + val1);
            }
        } else if (o2 != null && o2 instanceof Vector) {
            Vector v = (Vector)o2;
            int x = 0;
            while (x < v.size()) {
                Object val = v.elementAt(x);
                if (val instanceof Properties) {
                    Properties pp = null;
                    try {
                        if (o1 != null && ((Vector)o1).size() > x) {
                            pp = (Properties)((Vector)o1).elementAt(x);
                        }
                    }
                    catch (ClassCastException ee) {
                        log.addElement("key:" + path + "/" + x + "    Unknown Class Difference");
                    }
                    Common.diffObjects(pp, val, log, String.valueOf(path) + "/" + x, swapOldNew);
                } else if (val instanceof Vector) {
                    Vector vv = null;
                    try {
                        if (o1 != null && ((Vector)o1).size() > x) {
                            vv = (Vector)((Vector)o1).elementAt(x);
                        }
                    }
                    catch (ClassCastException ee) {
                        log.addElement("key:" + path + "/" + x + "    Unknown Class Difference");
                    }
                    Common.diffObjects(vv, val, log, String.valueOf(path) + "/" + x, swapOldNew);
                } else {
                    String val1 = "";
                    String val2 = "";
                    try {
                        if (o1 != null && ((Vector)o1).size() > x) {
                            val1 = ((Vector)o1).elementAt(x).toString();
                        }
                    }
                    catch (ClassCastException ee) {
                        log.addElement("key:" + path + "/" + x + "    Unknown Class Difference");
                    }
                    try {
                        if (o2 != null && ((Vector)o2).size() > x) {
                            val2 = ((Vector)o2).elementAt(x).toString();
                        }
                    }
                    catch (ClassCastException ee) {
                        log.addElement("key:" + path + "/" + x + "    Unknown Class Difference");
                    }
                    if (swapOldNew) {
                        String t = val2;
                        val2 = val1;
                        val1 = t;
                    }
                    if (!val1.equals(val2)) {
                        log.addElement("key:" + path + "/" + x + "    Previous:" + val2 + " New:" + val1);
                    }
                }
                ++x;
            }
        } else if (o2 == null) {
            log.addElement("key:" + path + "/    New");
        }
    }

    public static String getMD5(InputStream in) throws Exception {
        MessageDigest m = MessageDigest.getInstance("MD5");
        byte[] b = new byte[0x100000];
        int bytesRead = 0;
        while (bytesRead >= 0) {
            bytesRead = in.read(b);
            if (bytesRead < 0) continue;
            m.update(b, 0, bytesRead);
        }
        in.close();
        String s = new BigInteger(1, m.digest()).toString(16).toLowerCase();
        while (s.length() < 32) {
            s = "0" + s;
        }
        return s;
    }

    public static void getMD5(InputStream in, Vector md5s, boolean chunked, boolean forward, long length, long localSize) throws Exception {
        BufferedInputStream bin = new BufferedInputStream(in);
        bin.mark(com.crushftp.client.Common.encryptedNote.length() * 2);
        byte[] b = new byte[com.crushftp.client.Common.encryptedNote.length()];
        int bytesRead = bin.read(b);
        String header = "";
        if (bytesRead > 0) {
            header = new String(b, 0, bytesRead, "UTF8");
        }
        if (header.equals(com.crushftp.client.Common.pgpChunkedheaderStr) && chunked) {
            bin.skip("0                                        ".length());
            byte[] b1 = new byte[1];
            ByteArrayOutputStream clearBytes = new ByteArrayOutputStream();
            while (true) {
                byte[] chunk;
                int bytes = -1;
                clearBytes.reset();
                do {
                    if ((bytes = bin.read(b1)) <= 0) continue;
                    clearBytes.write(b1);
                } while (bytes >= 0 && b1[0] != 13);
                if (bytes < 0) break;
                String[] segment = new String(clearBytes.toByteArray(), "UTF8").split(":");
                long pos = Integer.parseInt(segment[0].trim());
                int clearSize = Integer.parseInt(segment[1].trim());
                int chunkSize = Integer.parseInt(segment[2].trim());
                int paddingSize = Integer.parseInt(segment[3].trim());
                String md5Hash = segment[4].trim();
                bytes = 0;
                while (chunkSize > 0 && bytes >= 0) {
                    chunk = new byte[chunkSize];
                    bytes = bin.read(chunk);
                    if (bytes < 0) continue;
                    chunkSize -= bytes;
                }
                bytes = 0;
                while (paddingSize > 0 && bytes >= 0) {
                    chunk = new byte[paddingSize];
                    bytes = bin.read(chunk);
                    if (bytes < 0) continue;
                    paddingSize -= bytes;
                }
                md5s.addElement(String.valueOf(pos) + "-" + (pos + (long)clearSize) + ":" + md5Hash);
            }
            bin.close();
        } else {
            bin.reset();
            Tunnel2.getMd5s(bin, chunked, forward, length, md5s, new StringBuffer(), localSize);
            bin.close();
        }
        in.close();
    }

    public static String parseSyncPart(String privs, String part) {
        if (privs.indexOf("(syncName") >= 0) {
            String key = "(SYNC" + part.toUpperCase();
            if (privs.toUpperCase().indexOf(key) < 0) {
                return "";
            }
            int pos = privs.toUpperCase().indexOf(key) + key.length() + 1;
            return privs.substring(pos, privs.indexOf(")", pos));
        }
        return null;
    }

    public static void trackSync(String action, String path1, String path2, boolean isDir, long size, long modified, String root_dir, String privs, String clientid, String md5Str) throws Exception {
        if (privs.indexOf("(sync") >= 0 && Common.parseSyncPart(privs, "name") != null && !com.crushftp.client.Common.dmz_mode) {
            Log.log("SYNC", 2, "Track Sync:" + action + " path1=" + path1 + " path2=" + path2 + " clientid=" + clientid);
            String syncIDTemp = Common.parseSyncPart(privs, "name").toUpperCase();
            if (path2 == null) {
                path2 = path1;
            }
            String item_path1 = path1.substring(root_dir.length() - 1);
            String item_path2 = path2.substring(root_dir.length() - 1);
            if (action.equalsIgnoreCase("delete")) {
                SyncTools.addJournalEntry(syncIDTemp, item_path1, action, clientid, md5Str);
            } else if (action.equalsIgnoreCase("change")) {
                SyncTools.addJournalEntry(syncIDTemp, item_path1, action, clientid, md5Str);
            } else if (action.equalsIgnoreCase("rename")) {
                SyncTools.addJournalEntry(syncIDTemp, String.valueOf(item_path1) + ";" + item_path2, action, clientid, md5Str);
            }
        }
    }

    public static void publishPendingSyncs(Vector pendingSyncs) throws Exception {
        while (pendingSyncs.size() > 0) {
            Properties pendingSync = (Properties)pendingSyncs.remove(0);
            Common.trackSync(pendingSync.getProperty("action"), pendingSync.getProperty("path1"), pendingSync.getProperty("path2"), pendingSync.getProperty("isDir").equalsIgnoreCase("true"), Long.parseLong(pendingSync.getProperty("size")), Long.parseLong(pendingSync.getProperty("modified")), pendingSync.getProperty("root_dir"), pendingSync.getProperty("privs"), pendingSync.getProperty("clientid"), pendingSync.getProperty("md5Str"));
        }
    }

    public static void trackPendingSync(Vector pendingSyncs, String action, String path1, String path2, boolean isDir, long size, long modified, String root_dir, String privs, String clientid, String md5Str) throws Exception {
        if (privs.indexOf("(sync") >= 0 && !com.crushftp.client.Common.dmz_mode) {
            Properties pendingSync = new Properties();
            pendingSync.put("action", action);
            if (path1 != null) {
                pendingSync.put("path1", path1);
            }
            if (path2 != null) {
                pendingSync.put("path2", path2);
            }
            pendingSync.put("isDir", String.valueOf(isDir));
            pendingSync.put("size", String.valueOf(size));
            pendingSync.put("modified", String.valueOf(modified));
            pendingSync.put("root_dir", root_dir);
            pendingSync.put("privs", privs);
            if (clientid != null) {
                pendingSync.put("clientid", clientid);
            }
            if (md5Str != null) {
                pendingSync.put("md5Str", md5Str);
            }
            pendingSyncs.addElement(pendingSync);
        }
    }

    public static void trackSyncRevision(GenericClient c, VRL vrl, String path, String root_dir, String privs, boolean renameMove, Properties info) throws Exception {
        if (privs.indexOf("(sync") >= 0 && Common.parseSyncPart(privs, "revisionsPath") != null && !vrl.getName().equals(".DS_Store") && !com.crushftp.client.Common.dmz_mode) {
            String revPath;
            if (path.startsWith(root_dir)) {
                path = path.substring(root_dir.length() - 1);
            }
            if ((revPath = Common.parseSyncPart(privs, "revisionsPath")).equals("")) {
                return;
            }
            int revCount = Integer.parseInt(Common.parseSyncPart(privs, "revisions"));
            if (revCount == 0) {
                return;
            }
            if (new File_U(String.valueOf(revPath) + path + "/" + revCount).exists()) {
                Common.recurseDelete(new File_U(String.valueOf(revPath) + path + "/" + revCount).getCanonicalPath(), false);
            }
            int xx = revCount - 1;
            while (xx >= 0) {
                new File_U(String.valueOf(revPath) + path + "/" + xx).renameTo(new File_U(String.valueOf(revPath) + path + "/" + (xx + 1)));
                --xx;
            }
            new File_U(String.valueOf(revPath) + path + "/0/").mkdirs();
            if (!(c instanceof FileClient)) {
                renameMove = false;
            }
            if (!(renameMove && c.rename(vrl.getPath(), String.valueOf(revPath) + path + "/0/" + vrl.getName(), true) || c.stat(vrl.getPath()) == null)) {
                if (c instanceof S3CrushClient) {
                    String rawXML = ((S3CrushClient)c).getRawXmlPath(path);
                    Common.recurseCopy_U(rawXML, new File_U(String.valueOf(revPath) + path + "/0/" + vrl.getName()).getCanonicalPath(), false);
                    new File_U(rawXML).delete();
                } else {
                    Common.recurseCopy_U(vrl.getPath(), new File_U(String.valueOf(revPath) + path + "/0/" + vrl.getName()).getCanonicalPath(), false);
                }
            }
            if (info != null) {
                Common.writeXMLObject(String.valueOf(revPath) + path + "/0/" + "info.XML", (Object)info, "info");
            }
            return;
        }
    }

    public static Vector getSyncTableData(String syncIDTemp, long rid, String table, String clientid, String root_dir, VFS uVFS, String prior_md5s_item_path) throws IOException {
        return SyncTools.getSyncTableData(syncIDTemp.toUpperCase(), rid, table, clientid, root_dir, uVFS, prior_md5s_item_path);
    }

    public static void buildPrivateKeyFile(String path) throws Exception {
        SecretKey key = KeyGenerator.getInstance("AES").generateKey();
        FileOutputStream out = new FileOutputStream(new File_S(path) + System.getProperty("appname", "CrushFTP") + ".key");
        ObjectOutputStream s = new ObjectOutputStream(out);
        s.writeObject(key);
        s.flush();
        s.close();
    }

    public static OutputStream getEncryptedStream(OutputStream out, String keyLocation, long streamPosition, boolean ascii, GenericClient c, String path) throws Exception {
        return Common.getEncryptedStream(out, keyLocation, streamPosition, ascii, c, path, "");
    }

    public static OutputStream getEncryptedStream(OutputStream out, String keyLocation, long streamPosition, boolean ascii, GenericClient c, String path, String encryption_cypher) throws Exception {
        return Common.getEncryptedStream(out, keyLocation, streamPosition, ascii, c, path, encryption_cypher, !ascii, false, "", "");
    }

    public static OutputStream getEncryptedStream(final OutputStream out, String keyLocation, long streamPosition, final boolean ascii, GenericClient c, String path, final String encryption_cypher, boolean hint_decrypted_size, final boolean encryption_sign, String privateKeyLocation, final String pass) throws Exception {
        if (keyLocation.replace('\\', '/').endsWith("/")) {
            ObjectInputStream ois = null;
            if (!new File_S(keyLocation).exists()) {
                Common.buildPrivateKeyFile(keyLocation);
            }
            ois = new ObjectInputStream(new FileInputStream(new File_S(String.valueOf(keyLocation) + System.getProperty("appname", "CrushFTP") + ".key")));
            SecretKey key = (SecretKey)ois.readObject();
            ois.close();
            IvParameterSpec paramSpec = new IvParameterSpec(iv);
            Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ecipher.init(1, (Key)key, paramSpec);
            if (streamPosition == 0L) {
                out.write((String.valueOf(com.crushftp.client.Common.encryptedNote) + "0                                        ").getBytes("UTF8"));
            }
            return new BufferedOutputStream(new CipherOutputStream(out, ecipher));
        }
        if (streamPosition > 0L) {
            throw new Exception("Can't resume encrypted PGP files.");
        }
        if (pgp == null) {
            pgp = new PGPLib();
        }
        pgp.setUseExpiredKeys(true);
        ByteArrayOutputStream pub_key = new ByteArrayOutputStream();
        ByteArrayOutputStream priv_key = new ByteArrayOutputStream();
        boolean pbe = false;
        if (keyLocation.toLowerCase().startsWith("password:")) {
            pbe = true;
        } else {
            if (new VRL(keyLocation).getProtocol().equalsIgnoreCase("file")) {
                Log.log("SERVER", 1, "PGP key info: Path: " + keyLocation + " Info :" + com.crushftp.client.Common.getPgpKeyInfo(new VRL(keyLocation).getPath()));
            }
            pub_key = Common.loadPgpKey(keyLocation);
            if (encryption_sign) {
                priv_key = Common.loadPgpKey(privateKeyLocation);
            }
        }
        final String keyLocationF = keyLocation;
        final ByteArrayOutputStream baos_key = pub_key;
        final ByteArrayOutputStream prive_baos_key = priv_key;
        final boolean pbeF = pbe;
        final Properties socks = Common.getConnectedSockets();
        Socket sock1 = (Socket)socks.remove("sock1");
        final Properties status = new Properties();
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    Socket sock2 = (Socket)socks.remove("sock2");
                    ByteArrayInputStream bytesIn = baos_key == null ? null : new ByteArrayInputStream(baos_key.toByteArray());
                    ByteArrayInputStream priv_bytesIn = prive_baos_key == null ? null : new ByteArrayInputStream(prive_baos_key.toByteArray());
                    pgp.setCompression("UNCOMPRESSED");
                    String cypher = encryption_cypher;
                    if (cypher.equals("")) {
                        if (ServerStatus.SG("encryption_cypher") != null) {
                            if (!ServerStatus.SG("encryption_cypher").trim().equals("")) {
                                cypher = ServerStatus.SG("encryption_cypher").trim();
                            }
                        }
                    }
                    if (!cypher.trim().equals("") && !cypher.trim().equalsIgnoreCase("NULL")) {
                        if (!pgp.isOverrideKeyAlgorithmPreferences()) {
                            pgp.setOverrideKeyAlgorithmPreferences(true);
                        }
                        pgp.setCypher(cypher.trim());
                    }
                    if (ascii) {
                        pgp.setAsciiVersionHeader(String.valueOf(System.getProperty("appname", "CrushFTP").toUpperCase()) + "#                                        ");
                    }
                    if (pbeF) {
                        pgp.encryptStreamPBE(sock2.getInputStream(), "", keyLocationF.substring(keyLocationF.indexOf(":") + 1), out, ascii, ServerStatus.BG("pgp_integrity_protect"));
                    } else if (encryption_sign) {
                        pgp.signAndEncryptStream(sock2.getInputStream(), "", (InputStream)priv_bytesIn, ServerStatus.thisObj.common_code.decode_pass(pass), bytesIn, out, ascii, ServerStatus.BG("pgp_integrity_protect"));
                    } else {
                        pgp.encryptStream(sock2.getInputStream(), "", bytesIn, out, ascii, ServerStatus.BG("pgp_integrity_protect"));
                    }
                    if (bytesIn != null) {
                        bytesIn.close();
                    }
                    status.put("status", "SUCCESS");
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                    status.put("error", e);
                    status.put("status", "ERROR");
                }
            }
        }, String.valueOf(Thread.currentThread().getName()) + ":PGP Encrypt Streamer");
        return new OutputStreamCloser(sock1.getOutputStream(), status, c, path, hint_decrypted_size && !ascii, ascii, out);
    }

    public static ByteArrayOutputStream loadPgpKey(String keyLocation) throws IOException, Exception, InterruptedException {
        ByteArrayOutputStream baos_key = new ByteArrayOutputStream();
        if (ServerStatus.BG("v10_beta") && com.crushftp.client.Common.System2.containsKey("crushftp.keystores." + keyLocation.toUpperCase().replace('\\', '/')) && !keyLocation.equals("")) {
            Properties p = (Properties)com.crushftp.client.Common.System2.get("crushftp.keystores." + keyLocation.toUpperCase().replace('\\', '/'));
            baos_key.write((byte[])p.get("bytes"));
        } else {
            VRL key_vrl = new VRL(keyLocation);
            GenericClient c_key = com.crushftp.client.Common.getClient(Common.getBaseUrl(key_vrl.toString()), System.getProperty("appname", "CrushFTP"), new Vector());
            if (ServerStatus.BG("v10_beta") && key_vrl.getConfig() != null && key_vrl.getConfig().size() > 0) {
                c_key.setConfigObj(key_vrl.getConfig());
            }
            c_key.login(key_vrl.getUsername(), key_vrl.getPassword(), "");
            com.crushftp.client.Common.streamCopier(null, null, c_key.download(key_vrl.getPath(), 0L, -1L, true, true), baos_key, false, true, true);
            c_key.logout();
            if (ServerStatus.BG("v10_beta")) {
                Properties p2 = new Properties();
                p2.put("bytes", baos_key.toByteArray());
                if (ServerStatus.BG("v11_beta")) {
                    p2.put("name", "");
                    p2.put("type", "pgp");
                }
                com.crushftp.client.Common.System2.put("crushftp.keystores." + keyLocation.toUpperCase().replace('\\', '/'), p2);
            }
        }
        return baos_key;
    }

    public static InputStream getDecryptedStream(InputStream in, String oldKeyLocation, String keyLocation, final String pass) throws Exception {
        final BufferedInputStream bin = new BufferedInputStream(in);
        bin.mark(com.crushftp.client.Common.encryptedNote.length() * 2);
        byte[] b_header = new byte[com.crushftp.client.Common.encryptedNote.length()];
        int bytesRead = bin.read(b_header);
        byte[] b_stream_header = new byte[6];
        new ByteArrayInputStream(b_header).read(b_stream_header);
        String header = "";
        if (bytesRead > 0) {
            header = new String(b_header, 0, bytesRead, "UTF8");
        }
        if (!oldKeyLocation.equals("") && header.equals(com.crushftp.client.Common.encryptedNote)) {
            ObjectInputStream ois = null;
            ois = new ObjectInputStream(new FileInputStream(new File_S(String.valueOf(oldKeyLocation) + System.getProperty("appname", "CrushFTP") + ".key")));
            SecretKey key = (SecretKey)ois.readObject();
            ois.close();
            IvParameterSpec paramSpec = new IvParameterSpec(iv);
            Cipher dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            dcipher.init(2, (Key)key, paramSpec);
            bin.skip("0                                        ".length());
            return new BufferedInputStream(new CipherInputStream(bin, dcipher));
        }
        if (!keyLocation.equals("") && (header.toUpperCase().startsWith("-----BEGIN PGP MESSAGE-----") || new PGPInspectLib().isPGPData(b_stream_header))) {
            bin.reset();
            if (pgp == null) {
                pgp = new PGPLib();
            }
            pgp.setUseExpiredKeys(true);
            ByteArrayOutputStream baos_priv_key = new ByteArrayOutputStream();
            boolean pbe = false;
            if (keyLocation.toLowerCase().startsWith("password:")) {
                pbe = true;
            } else {
                if (new VRL(keyLocation).getProtocol().equalsIgnoreCase("file")) {
                    Log.log("SERVER", 1, "PGP key info: Path: " + keyLocation + " Info :" + com.crushftp.client.Common.getPgpKeyInfo(new VRL(keyLocation).getPath()));
                }
                baos_priv_key = Common.loadPgpKey(keyLocation);
            }
            final ByteArrayOutputStream baos_key = baos_priv_key;
            final String keyLocationF = keyLocation;
            final boolean pbeF = pbe;
            final Properties socks = Common.getConnectedSockets();
            Socket sock2 = (Socket)socks.remove("sock2");
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    OutputStream out4 = null;
                    try {
                        Socket sock1 = (Socket)socks.remove("sock1");
                        ByteArrayInputStream bytesIn = new ByteArrayInputStream(baos_key.toByteArray());
                        pgp.setCompression("UNCOMPRESSED");
                        out4 = sock1.getOutputStream();
                        if (pbeF) {
                            pgp.decryptStreamPBE(bin, keyLocationF.substring(keyLocationF.indexOf(":") + 1), out4);
                        } else {
                            pgp.decryptStream((InputStream)bin, bytesIn, ServerStatus.thisObj.common_code.decode_pass(pass), out4);
                        }
                        bytesIn.close();
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                    }
                    try {
                        if (out4 != null) {
                            out4.close();
                        }
                    }
                    catch (IOException e) {
                        Log.log("SERVER", 1, e);
                    }
                }
            }, String.valueOf(Thread.currentThread().getName()) + ":PGP Decrypt Streamer");
            return sock2.getInputStream();
        }
        bin.reset();
        return bin;
    }

    public static Properties urlDecodePost(Properties p) {
        Enumeration<Object> keys = p.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (!(p.get(key) instanceof String)) continue;
            p.put(key, Common.url_decode(p.get(key).toString().replace('+', ' ')));
        }
        return p;
    }

    public static void verifyOSXVolumeMounted(String url) {
        try {
            String vol_path;
            VRL vrl;
            if (Common.machine_is_x() && (vrl = new VRL(url)).getProtocol().equalsIgnoreCase("FILE") && (vol_path = new VRL(url).getPath()).startsWith("/Volumes/")) {
                vol_path = vol_path.substring(0, vol_path.indexOf("/", "/Volumes/".length() + 1));
                int x = 0;
                while (!new File_S(vol_path).exists() && x < 30) {
                    Thread.sleep(1000L);
                    ++x;
                }
                if (!new File_S(vol_path).exists()) {
                    throw new Exception("Volume not mounted:" + vol_path);
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String dots(String s) {
        return com.crushftp.client.Common.dots(s);
    }

    public static String getBaseUrl(String s) {
        String tmp_path;
        String s_original = s;
        if (s.indexOf(":") < 0) {
            s = "/";
        } else if (s.indexOf("@") > 0 && s.indexOf(":") != s.lastIndexOf(":") && !s.toLowerCase().startsWith("file:")) {
            if (s.toUpperCase().startsWith("GLACIER:/") && !s.endsWith("/")) {
                s = String.valueOf(s) + "/";
            }
            s = s.indexOf("@") != s.lastIndexOf("@") ? s.substring(0, s.indexOf("/", s.indexOf("@")) + 1) : (s.lastIndexOf("@") > s.lastIndexOf(":") ? s.substring(0, s.indexOf("/", s.lastIndexOf("@")) + 1) : (s.length() > 8 && s.indexOf(":", 8) != s.lastIndexOf(":") && !s.toUpperCase().startsWith("SHAREPOINT2:/") ? s.substring(0, s.indexOf("/", s.indexOf(":", 8)) + 1) : s.substring(0, s.indexOf("/", s.lastIndexOf(":")) + 1)));
        } else {
            s = s.toLowerCase().startsWith("file:/") && !s.toLowerCase().startsWith("file://") ? s.substring(0, s.indexOf("/", s.indexOf(":") + 1) + 1) : (s.toLowerCase().startsWith("file://") && !s.toLowerCase().startsWith("file:///") ? s.substring(0, s.indexOf("/", s.indexOf(":") + 2) + 1) : s.substring(0, s.indexOf("/", s.indexOf(":") + 3) + 1));
        }
        if ((s.toUpperCase().startsWith("S3:/") || s.toUpperCase().startsWith("GSTORAGE:/")) && (tmp_path = new VRL(s_original).getPath()).length() > 1) {
            tmp_path = tmp_path.substring(1, tmp_path.indexOf("/", 1) + 1);
            s = String.valueOf(s) + tmp_path;
        }
        if (System.getProperty("crushftp.v10_beta", "false").equals("true") && s.toUpperCase().indexOf("SMB3:") >= 0) {
            s = s_original;
        }
        if (s.toUpperCase().startsWith("AZURE:/")) {
            s = s_original;
        }
        return s;
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

    public static void streamCopier(Socket sock1, Socket sock2, InputStream in, OutputStream out, boolean async, boolean closeInput, boolean closeOutput) throws InterruptedException {
        com.crushftp.client.Common.streamCopier(sock1, sock2, in, out, async, closeInput, closeOutput);
    }

    public static String send_mail(String server_ip, String to_user, String cc_user, String bcc_user, String from_user, String subject, String body, String smtp_server, String smtp_user, String smtp_pass, boolean smtp_ssl, boolean html, File_B[] attachments) {
        return Common.send_mail(server_ip, to_user, cc_user, bcc_user, from_user, null, subject, body, smtp_server, smtp_user, smtp_pass, smtp_ssl, html, attachments);
    }

    public static String send_mail(String server_ip, String to_user, String cc_user, String bcc_user, String from_user, String reply_to_user, String subject, String body, String smtp_server, String smtp_user, String smtp_pass, boolean smtp_ssl, boolean html, File_B[] attachments) {
        try {
            return Mailer.send_mail(server_ip, to_user, cc_user, bcc_user, from_user, reply_to_user, subject, body, smtp_server, smtp_user, smtp_pass, smtp_ssl, html, attachments);
        }
        catch (Throwable e) {
            Log.log("SMTP", 1, e);
            return "ERROR:" + e.toString();
        }
    }

    public static String send_mail(String server_ip, String to_user, String cc_user, String bcc_user, String from_user, String subject, String body, String smtp_server, String smtp_user, String smtp_pass, boolean smtp_ssl, boolean html) {
        return Common.send_mail(server_ip, to_user, cc_user, bcc_user, from_user, null, subject, body, smtp_server, smtp_user, smtp_pass, smtp_ssl, html, null);
    }

    public static String send_mail(String server_ip, String to_user, String cc_user, String bcc_user, String from_user, String reply_to_user, String subject, String body, String smtp_server, String smtp_user, String smtp_pass, boolean smtp_ssl, boolean html) {
        return Common.send_mail(server_ip, to_user, cc_user, bcc_user, from_user, reply_to_user, subject, body, smtp_server, smtp_user, smtp_pass, smtp_ssl, html, null);
    }

    public static boolean compare_with_hack_username(String username, String hack_username) {
        username = username.trim();
        hack_username = hack_username.trim();
        if (username.equalsIgnoreCase("anonymous")) {
            return false;
        }
        if ((hack_username.indexOf(42) >= 0 || hack_username.indexOf(33) >= 0 || hack_username.indexOf(63) >= 0 || hack_username.indexOf(46) >= 0 || hack_username.indexOf(36) >= 0 || hack_username.indexOf(94) >= 0) && com.crushftp.client.Common.do_search(hack_username.toUpperCase(), username.toUpperCase(), false, 0)) {
            return true;
        }
        return username.equalsIgnoreCase(hack_username);
    }

    public static void send_change_pass_email(SessionCrush session) {
        Properties template = Common.get_email_template("Change Pass Email");
        if (template != null) {
            try {
                String username = "";
                if (session != null && session.user_info != null) {
                    username = session.uiSG("user_name");
                }
                final Properties email_info = new Properties();
                email_info.put("server", ServerStatus.SG("smtp_server"));
                email_info.put("user", ServerStatus.SG("smtp_user"));
                email_info.put("pass", ServerStatus.SG("smtp_pass"));
                email_info.put("ssl", ServerStatus.SG("smtp_ssl"));
                email_info.put("html", ServerStatus.SG("smtp_html"));
                email_info.put("from", ServerStatus.thisObj.change_vars_to_values(template.getProperty("emailFrom"), session));
                email_info.put("reply_to", ServerStatus.thisObj.change_vars_to_values(template.getProperty("emailReplyTo"), session));
                email_info.put("to", session.SG("email"));
                email_info.put("cc", ServerStatus.thisObj.change_vars_to_values(template.getProperty("emailCC"), session));
                email_info.put("bcc", ServerStatus.thisObj.change_vars_to_values(template.getProperty("emailBCC"), session));
                email_info.put("subject", ServerStatus.thisObj.change_vars_to_values(template.getProperty("emailSubject"), session));
                email_info.put("body", ServerStatus.thisObj.change_vars_to_values(template.getProperty("emailBody"), session));
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        ServerStatus.thisObj.sendEmail(email_info);
                    }
                }, "Change Pass Email Username:" + username);
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
        }
    }

    public static Properties get_email_template(String template_name) {
        template_name = Common.remove_html_special_chars(template_name);
        Vector email_templates = ServerStatus.VG("email_templates");
        if (email_templates != null) {
            int x = 0;
            while (x < email_templates.size()) {
                Properties email_template = (Properties)email_templates.elementAt(x);
                if (email_template.getProperty("name", "").replace('+', ' ').equals(template_name)) {
                    return email_template;
                }
                ++x;
            }
        }
        return null;
    }

    public static void send_otp_for_auth_sms(String otp_to, String otp_token) throws Exception {
        String otp_url = ServerStatus.SG("otp_url");
        otp_url = Common.replace_str(otp_url, "{otp_from}", Common.url_encode(ServerStatus.SG("otp_from")));
        otp_url = Common.replace_str(otp_url, "{otp_to}", Common.url_encode(otp_to));
        otp_url = Common.replace_str(otp_url, "{otp_token}", Common.url_encode(otp_token));
        otp_url = Common.replace_str(otp_url, "{otp_username}", Common.url_encode(ServerStatus.SG("otp_username")));
        otp_url = Common.replace_str(otp_url, "{otp_password}", Common.url_encode(ServerStatus.SG("otp_password")));
        otp_url = Common.replace_str(otp_url, "{otp_extra1}", Common.url_encode(ServerStatus.SG("otp_extra1")));
        otp_url = Common.replace_str(otp_url, "{otp_extra2}", Common.url_encode(ServerStatus.SG("otp_extra2")));
        otp_url = ServerStatus.change_vars_to_values_static(otp_url, null, null, null);
        VRL otp_vrl = new VRL(otp_url);
        HttpURLConnection urlc = (HttpURLConnection)new URL(otp_url).openConnection();
        urlc.setDoOutput(true);
        if (otp_vrl.getUsername() != null && otp_vrl.getUsername().trim().length() > 0) {
            String basicAuth = "Basic " + new String(Base64.encodeBytes((String.valueOf(otp_vrl.getUsername()) + ":" + otp_vrl.getPassword()).getBytes()));
            urlc.setRequestProperty("Authorization", basicAuth);
        }
        urlc.setRequestMethod(ServerStatus.SG("otp_url_verb"));
        String post = ServerStatus.SG("otp_post");
        post = Common.replace_str(post, "{otp_from}", Common.url_encode(ServerStatus.SG("otp_from")));
        post = Common.replace_str(post, "{otp_to}", Common.url_encode(otp_to));
        post = Common.replace_str(post, "{otp_token}", Common.url_encode(otp_token));
        post = Common.replace_str(post, "{otp_username}", Common.url_encode(ServerStatus.SG("otp_username")));
        post = Common.replace_str(post, "{otp_password}", Common.url_encode(ServerStatus.SG("otp_password")));
        post = Common.replace_str(post, "{otp_extra1}", Common.url_encode(ServerStatus.SG("otp_extra1")));
        post = Common.replace_str(post, "{otp_extra2}", Common.url_encode(ServerStatus.SG("otp_extra2")));
        post = ServerStatus.change_vars_to_values_static(post, null, null, null);
        if (ServerStatus.SG("otp_url_verb").equalsIgnoreCase("POST")) {
            OutputStream out = urlc.getOutputStream();
            out.write(post.getBytes("UTF8"));
            out.close();
        }
        if (urlc.getResponseCode() > 299) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Common.streamCopier(urlc.getInputStream(), baos, false, true, true);
            String result = new String(baos.toByteArray());
            urlc.disconnect();
            result = String.valueOf(urlc.getResponseCode()) + ":" + result;
            Log.log("LOGIN", 1, "CHALLENGE_OTP : Error :" + result);
            throw new Exception(result);
        }
        urlc.disconnect();
    }

    public static Comparator get_file_last_modified_Comparator() {
        class Counts
        implements Comparator {
            Counts() {
            }

            public int compare(Object p1, Object p2) {
                File_S f1 = (File_S)p1;
                File_S f2 = (File_S)p2;
                if (f1.lastModified() < f2.lastModified()) {
                    return -1;
                }
                if (f1.lastModified() > f2.lastModified()) {
                    return 1;
                }
                return 0;
            }
        }
        return new Counts();
    }

    public static Vector get_urls_from_VFS(Properties virtual) {
        Vector<String> urls = new Vector<String>();
        Enumeration<?> e = virtual.propertyNames();
        while (e.hasMoreElements()) {
            Vector vItems;
            Properties p;
            String key = (String)e.nextElement();
            if (key.equals("vfs_permissions_object") || !(p = (Properties)virtual.get(key)).containsKey("vItems") || (vItems = (Vector)p.get("vItems")) == null || vItems.isEmpty()) continue;
            Properties pp = (Properties)vItems.get(0);
            urls.add(VRL.fileFix(pp.getProperty("url")));
        }
        return urls;
    }

    public static File_B[] convert_files_to_files_both(File[] files) {
        File_B[] items = new File_B[files.length];
        int x = 0;
        while (x < files.length) {
            items[x] = new File_B(files[x]);
            ++x;
        }
        return items;
    }

    public static Properties get_vfs_posix_settings(String privs, boolean is_file) {
        Properties vfs_posix_settings = new Properties();
        String posix = "(posix:";
        if (!is_file) {
            posix = "(dir_posix:";
        }
        if (privs.indexOf(posix) > 0) {
            int end_index;
            String[] vfs_posix = null;
            int start_index = privs.indexOf(posix) + posix.length();
            vfs_posix = privs.substring(start_index, end_index = privs.indexOf(")", start_index)).split(":");
            if (vfs_posix != null && vfs_posix.length == 3) {
                vfs_posix_settings.put("vfs_privs", vfs_posix[0]);
                vfs_posix_settings.put("vfs_owner", vfs_posix[1]);
                vfs_posix_settings.put("vfs_group", vfs_posix[2]);
            }
        }
        return vfs_posix_settings;
    }

    public static void remove_keywords(Properties item) {
        if (item.getProperty("url", "").equals("")) {
            return;
        }
        String keywords_path = SearchHandler.getPreviewPath(item.getProperty("url"), "1", 1);
        if (!keywords_path.equals("")) {
            if (new File_U(String.valueOf(Common.all_but_last(Common.all_but_last(String.valueOf(ServerStatus.SG("previews_path")) + keywords_path.substring(1)))) + "index.txt").exists()) {
                new File_U(String.valueOf(Common.all_but_last(Common.all_but_last(String.valueOf(ServerStatus.SG("previews_path")) + keywords_path.substring(1)))) + "index.txt").delete();
            }
        }
    }

    public static Vector convertToVector(String[] array) {
        Vector<String> v = new Vector<String>();
        int x = 0;
        while (x < array.length) {
            v.add(array[x].trim());
            ++x;
        }
        return v;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void getAllSubFolders(VRL vrl1, GenericClient c1, int depth, StringBuffer status, Vector sub_folder_list) throws Exception {
        if (vrl1.getProtocol().equalsIgnoreCase("file") && vrl1.getPath().trim().equals("/")) {
            return;
        }
        if (vrl1.getProtocol().equalsIgnoreCase("file") && vrl1.getPath().trim().equals("~")) {
            return;
        }
        if (vrl1.getProtocol().equalsIgnoreCase("file") && vrl1.getPath().indexOf(":") >= 0 && vrl1.getPath().length() < 4) {
            return;
        }
        boolean close1 = false;
        if (c1 == null) {
            c1 = Common.getClient(Common.getBaseUrl(vrl1.toString()), "", com.crushftp.client.Common.log);
            c1.login(vrl1.getUsername(), vrl1.getPassword(), null);
            close1 = true;
        }
        if (c1.stat(vrl1.getPath()).getProperty("type").equalsIgnoreCase("DIR")) {
            StringBuffer stringBuffer = status;
            synchronized (stringBuffer) {
                if (status.toString().equals("CANCELLED")) {
                    throw new Exception("CANCELLED");
                }
                status.setLength(0);
                status.append("Getting list of:" + vrl1.getName()).append("...");
            }
            Vector list = new Vector();
            c1.list(vrl1.getPath().endsWith("/") ? vrl1.getPath() : String.valueOf(vrl1.getPath()) + "/", list);
            int x = 0;
            while (x < list.size()) {
                Properties p1 = (Properties)list.elementAt(x);
                StringBuffer stringBuffer2 = status;
                synchronized (stringBuffer2) {
                    if (status.toString().equals("CANCELLED")) {
                        throw new Exception("CANCELLED");
                    }
                    status.setLength(0);
                    status.append("Getting list:" + vrl1.getName()).append("...");
                }
                if (p1.getProperty("type").equalsIgnoreCase("DIR")) {
                    sub_folder_list.add(p1);
                    Common.getAllSubFolders(new VRL(Common.url_decode(p1.getProperty("url"))), c1, depth + 1, status, list);
                }
                ++x;
            }
        }
        if (depth == 0 && close1) {
            c1.close();
            c1.logout();
        }
    }

    public static String remove_html_special_chars(String data) {
        data = Common.replace_str(data, "&quot;", "\"");
        data = Common.replace_str(data, "&gt;", ">");
        data = Common.replace_str(data, "&lt;", "<");
        data = Common.replace_str(data, "&apos;", "'");
        data = Common.replace_str(data, "&amp;", "&");
        data = Common.replace_str(data, "&nbsp;", " ");
        return data;
    }

    public static boolean isValidTemplateUserOfDMZ() {
        boolean valid = true;
        if (com.crushftp.client.Common.dmz_mode && System.getProperty("crushftp.dmz.allow.invalid.template.user", "false").equals("false")) {
            Vector server_groups = ServerStatus.VG("server_groups");
            int x = 0;
            while (x < server_groups.size()) {
                String serverGroup = ((String)server_groups.get(x)).trim();
                if (!serverGroup.equals("ConnectionProfiles")) {
                    Properties template = UserTools.ut.getUser(serverGroup, "template", true);
                    if (template == null) {
                        try {
                            UserTools.addTemplateUserForDMZ(serverGroup, "template");
                            Log.log("SERVER", 0, "DMZ's template user is missing! User connection group: " + serverGroup + " Creating default template user for DMZ.");
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 0, "Error: Could not create template user! Error message : " + e);
                            valid = false;
                        }
                    } else {
                        VFS vfs = UserTools.ut.getVFS(serverGroup, "template");
                        if (vfs.homes.size() != 1) {
                            Log.log("SERVER", 0, "Error: Invalid template user. User connection group : " + serverGroup + "  It has more then one VFS define. Server port items will not start.");
                            valid = false;
                        } else {
                            Properties virtual = (Properties)vfs.homes.get(0);
                            if (virtual.size() != 3) {
                                Log.log("SERVER", 0, "Error: Invalid template user. User connection group : " + serverGroup + " It has more then one VFS define. Server port items will not start.");
                                valid = false;
                            } else if (!(virtual.containsKey("/Internal") || virtual.containsKey("/internal") || virtual.containsKey("/internal1") || virtual.containsKey("/Internal1"))) {
                                Log.log("SERVER", 0, "Error: Invalid template user. User connection group : " + serverGroup + " VFS: Missing Internal VFS item! Server port items will not start.");
                                valid = false;
                            }
                        }
                    }
                }
                ++x;
            }
        }
        return valid;
    }

    public static void sendResetPasswordTokenEmail(Properties user, String lang, String hostString, String token) throws Exception {
        String url = String.valueOf(Common.replace_str(Common.replace_str(ServerStatus.SG("reset_url"), "www.domain.com", hostString), "{host}", hostString)) + "?token=" + token;
        if (lang == null || lang.length() != 2 || lang.charAt(0) > 'z' || lang.charAt(0) < 'a' || lang.charAt(1) > 'z' || lang.charAt(1) < 'a') {
            lang = "en";
        }
        lang = lang.toLowerCase();
        String resetMsg = ServerStatus.SG("password_reset_message");
        File f = new File(String.valueOf(System.getProperty("crushftp.web")) + "localizations/password_reset_message_" + lang + ".html");
        if (f.exists() && f.length() < 0x100000L) {
            resetMsg = Common.getFileText(f.getPath());
        }
        resetMsg = Common.replace_str(resetMsg, "{url}", url);
        resetMsg = ServerStatus.change_vars_to_values_static(resetMsg, user, new Properties(), null);
        String resetSubject = ServerStatus.SG("password_reset_subject");
        f = new File(String.valueOf(System.getProperty("crushftp.web")) + "localizations/password_reset_subject_" + lang + ".html");
        if (f.exists() && f.length() < 0x100000L) {
            resetSubject = Common.getFileText(f.getPath());
        }
        com.crushftp.client.Common.send_mail(ServerStatus.SG("discovered_ip"), user.getProperty("email"), "", "", ServerStatus.SG("smtp_from"), resetSubject, resetMsg, ServerStatus.SG("smtp_server"), ServerStatus.SG("smtp_user"), ServerStatus.SG("smtp_pass"), ServerStatus.SG("smtp_ssl").equals("true"), true, null);
    }

    public void set_defaults(Properties default_settings) {
        default_settings.put("rid", String.valueOf(System.currentTimeMillis()));
        default_settings.put("listing_buffer_count", "500");
        default_settings.put("listing_multithreaded", "true");
        default_settings.put("registration_name", "crush");
        default_settings.put("registration_email", "ftp");
        default_settings.put("registration_code", "crushftp:(MAX=5)(V=5)");
        default_settings.put("ftp_welcome_message", "Welcome to " + System.getProperty("appname", "CrushFTP") + "!");
        default_settings.put("server_start_message", String.valueOf(System.getProperty("appname", "CrushFTP")) + " Server Ready!");
        default_settings.put("ssh_comments", "http://www." + System.getProperty("appname", "CrushFTP") + ".com/");
        default_settings.put("http_server_header", String.valueOf(System.getProperty("appname", "CrushFTP")) + " HTTP Server");
        default_settings.put("username_uppercase", "false");
        default_settings.put("allow_session_caching", "true");
        default_settings.put("allow_session_caching_on_exit", "true");
        default_settings.put("replicate_users", "true");
        default_settings.put("tls_version", "TLSv1.2,TLSv1.3");
        default_settings.put("relaxed_event_grouping", "false");
        default_settings.put("syslog_protocol", "udp");
        default_settings.put("syslog_host", "127.0.0.1");
        default_settings.put("syslog_port", "1514");
        default_settings.put("check_all_recursive_deletes", "false");
        default_settings.put("acl_cache_timeout", "60");
        default_settings.put("max_threads", "2000");
        default_settings.put("allow_local_ip_pasv", "true");
        default_settings.put("allow_local_ip_pasv_any", "false");
        default_settings.put("Access-Control-Allow-Origin", "");
        default_settings.put("http_session_timeout", "60");
        default_settings.put("log_debug_level", "0");
        default_settings.put("domain_cookie", "");
        default_settings.put("block_access", "");
        default_settings.put("count_dir_items", "false");
        default_settings.put("list_zip_app", "true");
        default_settings.put("zip_icon_preview_allowed", "false");
        default_settings.put("allow_auto_save", "true");
        default_settings.put("force_ipv4", "false");
        default_settings.put("allow_zipstream", "true");
        default_settings.put("allow_filetree", "true");
        default_settings.put("create_home_folder", "false");
        default_settings.put("allow_x_forwarded_host", "false");
        default_settings.put("recent_user_count", "100");
        default_settings.put("tunnel_ram_cache", "128");
        default_settings.put("s3_buffer", "5");
        default_settings.put("track_user_md4_hashes", "false");
        default_settings.put("jailproxy", "true");
        default_settings.put("learning_proxy", "false");
        default_settings.put("rfc_proxy", "false");
        default_settings.put("event_thread_timeout", "60");
        default_settings.put("exif_keywords", "false");
        default_settings.put("trusted_ip_parts", "4");
        default_settings.put("disable_stats", "false");
        default_settings.put("sync_history_days", "30");
        default_settings.put("fix_slashes", "true");
        default_settings.put("smtp_helo_ip", "");
        default_settings.put("zip64", "false");
        default_settings.put("zip64_always", "false");
        default_settings.put("mdtm_gmt", "false");
        default_settings.put("instant_chmod_chown_chgrp", "false");
        default_settings.put("7_token_proxy", "false");
        default_settings.put("command_flush_interval", "10");
        default_settings.put("sort_listings", "false");
        default_settings.put("case_sensitive_list_search", "false");
        default_settings.put("change_remote_password", "true");
        default_settings.put("expire_emailed_passwords", "false");
        default_settings.put("ignore_web_anonymous", "false");
        default_settings.put("ignore_web_anonymous_proxy", "false");
        default_settings.put("test_proxy_dir", "true");
        default_settings.put("lowercase_usernames", "false");
        default_settings.put("deny_secure_active_mode", "false");
        default_settings.put("event_empty_files", "true");
        default_settings.put("allow_nlst_empty", "true");
        default_settings.put("ssh_debug_logging", "");
        default_settings.put("ssh_close_all", "false");
        default_settings.put("socketpool_timeout", "20");
        default_settings.put("log_roll_time", "00:00");
        default_settings.put("zipCompressionLevel", "Best");
        default_settings.put("log_transfer_speeds", "true");
        default_settings.put("smtp_subject_utf8", "true");
        default_settings.put("log_location", System.getProperty("crushftp.log", "./" + System.getProperty("appname", "CrushFTP") + ".log"));
        default_settings.put("user_log_location", String.valueOf(Common.all_but_last(System.getProperty("crushftp.log", "./" + System.getProperty("appname", "CrushFTP") + ".log"))) + "logs/session_logs/");
        default_settings.put("logging_provider", "");
        default_settings.put("extended_logging", "false");
        default_settings.put("temp_accounts_path", "./TempAccounts/");
        if (Common.OSXApp()) {
            default_settings.put("temp_accounts_path", String.valueOf(System.getProperty("crushftp.home")) + "../../../../TempAccounts/");
        }
        default_settings.put("previews_path", System.getProperty("crushftp.home"));
        if (Common.OSXApp()) {
            default_settings.put("previews_path", String.valueOf(System.getProperty("crushftp.home")) + "../../../../");
        }
        default_settings.put("temp_accounts_length", "4");
        default_settings.put("char_encoding", "UTF-8");
        default_settings.put("deny_localhost_admin", "false");
        default_settings.put("line_separator_crlf", "false");
        default_settings.put("file_transfer_mode", "BINARY");
        default_settings.put("fileEncryption", "false");
        default_settings.put("fileDecryption", "false");
        default_settings.put("fileEncryptionKey", "");
        default_settings.put("filePublicEncryptionKey", "");
        default_settings.put("fileDecryptionKey", "");
        default_settings.put("fileDecryptionKeyPass", "");
        default_settings.put("password_reset_message_browser", "If your account was found, an email has been sent to you.  Follow the email's instructions.  The email's link will expire in 10 minutes.");
        default_settings.put("password_reset_message_browser_bad", "If your account was found, an email has been sent to you.  Follow the email's instructions.  The email's link will expire in 10 minutes.");
        default_settings.put("password_reset_subject", "Password Reset");
        default_settings.put("password_reset_message", "<html><body>Your password reset can be completed by clicking the following URL.<br><br><a href=\"{url}\">{url}</a> <br><br><b>This link will expire in 10 minutes.</b></body></html>");
        default_settings.put("proxyActivePorts", "1025-65535");
        default_settings.put("disable_dir_filter", "true");
        default_settings.put("event_reuse", "true");
        default_settings.put("event_asynch", "false");
        default_settings.put("event_batching", "true");
        default_settings.put("needClientAuth", "false");
        default_settings.put("client_cert_auth", "true");
        default_settings.put("epsveprt", "true");
        default_settings.put("allow_mlst", "true");
        default_settings.put("cert_path", "builtin");
        default_settings.put("globalKeystorePass", System.getProperty("appname", "CrushFTP").toLowerCase());
        default_settings.put("globalKeystoreCertPass", System.getProperty("appname", "CrushFTP").toLowerCase());
        default_settings.put("disabled_ciphers", "(TLS_RSA_WITH_AES_128_CBC_SHA)(TLS_RSA_WITH_AES_256_CBC_SHA)(TLS_DHE_RSA_WITH_AES_128_CBC_SHA)(TLS_DHE_RSA_WITH_AES_256_CBC_SHA)(SSL_RSA_WITH_3DES_EDE_CBC_SHA)(SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA)(SSL_RSA_WITH_DES_CBC_SHA)(SSL_DHE_RSA_WITH_DES_CBC_SHA)(SSL_RSA_EXPORT_WITH_RC4_40_MD5)(SSL_RSA_EXPORT_WITH_DES40_CBC_SHA)(SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA)(SSL_RSA_WITH_NULL_MD5)(SSL_RSA_WITH_NULL_SHA)(TLS_ECDH_ECDSA_WITH_NULL_SHA)(TLS_ECDH_RSA_WITH_NULL_SHA)(TLS_ECDHE_ECDSA_WITH_NULL_SHA)(TLS_ECDHE_RSA_WITH_NULL_SHA)(SSL_DH_anon_WITH_RC4_128_MD5)(TLS_DH_anon_WITH_AES_128_CBC_SHA)(TLS_DH_anon_WITH_AES_256_CBC_SHA)(SSL_DH_anon_WITH_3DES_EDE_CBC_SHA)(SSL_DH_anon_WITH_DES_CBC_SHA)(TLS_ECDH_anon_WITH_RC4_128_SHA)(TLS_ECDH_anon_WITH_AES_128_CBC_SHA)(TLS_ECDH_anon_WITH_AES_256_CBC_SHA)(TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA)(SSL_DH_anon_EXPORT_WITH_RC4_40_MD5)(SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA)(TLS_ECDH_anon_WITH_NULL_SHA)");
        default_settings.put("user_backup_count", "100");
        default_settings.put("proxyDownloadRepository", "./");
        default_settings.put("proxyUploadRepository", "./");
        default_settings.put("proxyKeepDownloads", "false");
        default_settings.put("proxyKeepUploads", "false");
        default_settings.put("proxy_socket_mode", "passive");
        default_settings.put("invalid_usernames_seconds", "60");
        default_settings.put("default_system_owner", "");
        default_settings.put("default_system_group", "");
        default_settings.put("default_logo", "logo.png");
        default_settings.put("default_title", String.valueOf(System.getProperty("appname", "CrushFTP")) + " WebInterface");
        default_settings.put("webFooterText", "");
        default_settings.put("web404Text", "The selected resource was not found.");
        default_settings.put("emailReminderSubjectText", LOC.G("Password Reminder"));
        default_settings.put("emailReminderBodyText", String.valueOf(LOC.G("Your password is : ")) + "%user_pass%" + "\r\n\r\n" + LOC.G("Requested from IP:") + "%user_ip%");
        Vector<Properties> email_templates = new Vector<Properties>();
        Properties email_template = new Properties();
        email_templates.addElement(email_template);
        email_template.put("emailBody", "Your username is:{user_name}<br>Your password is:{user_pass}<br>");
        email_template.put("emailSubject", "New Account Information");
        email_template.put("name", "New Account");
        default_settings.put("email_templates", email_templates);
        default_settings.put("reportSchedules", new Vector());
        default_settings.put("miniURLs", new Vector());
        default_settings.put("miniURLs_dmz", new Vector());
        default_settings.put("proxyRules", new Vector());
        default_settings.put("alerts", new Vector());
        default_settings.put("tunnels", new Vector());
        default_settings.put("tunnels_dmz", new Vector());
        default_settings.put("monitored_folders", new Vector());
        default_settings.put("sqlItems", new Properties());
        default_settings.put("customData", new Properties());
        default_settings.put("externalSqlUsers", "false");
        default_settings.put("sql_prefix", "");
        default_settings.put("xmlUsers", "true");
        default_settings.put("version", "1.0");
        default_settings.put("total_server_bytes_transfered", "0K");
        default_settings.put("total_server_bytes_sent", "0K");
        default_settings.put("total_server_bytes_sent_long", "0");
        default_settings.put("total_server_bytes_received", "0K");
        default_settings.put("total_server_bytes_received_long", "0");
        default_settings.put("failed_logins", "0");
        default_settings.put("successful_logins", "0");
        default_settings.put("uploaded_files", "0");
        default_settings.put("downloaded_files", "0");
        default_settings.put("roll_daily_logs", "true");
        default_settings.put("last_login_date_time", "<none>");
        default_settings.put("last_login_ip", "<none>");
        default_settings.put("last_login_user", "<none>");
        default_settings.put("discovered_ip", "0.0.0.0");
        default_settings.put("auto_ip_discovery", "true");
        default_settings.put("discover_ip_refresh", "60");
        default_settings.put("beep_connect", "false");
        default_settings.put("slow_directory_scanners", "true");
        default_settings.put("sftp_recurse_delete", "false");
        default_settings.put("disable_referer_cookie", "false");
        default_settings.put("disable_mdtm_modifications", "false");
        default_settings.put("delete_partial_uploads", "false");
        default_settings.put("password_encryption", "DES");
        default_settings.put("newversion", "true");
        default_settings.put("lsla2", "false");
        default_settings.put("posix", String.valueOf(!Common.machine_is_windows()));
        default_settings.put("allow_directory_caching", "false");
        default_settings.put("deny_fxp", "false");
        default_settings.put("deny_reserved_ports", "true");
        default_settings.put("allow_gzip", "true");
        default_settings.put("display_alt_logo", "false");
        default_settings.put("hide_email_password", "false");
        Vector preview_configs = new Vector();
        default_settings.put("preview_configs", preview_configs);
        Vector<String> server_groups = new Vector<String>();
        server_groups.addElement("MainUsers");
        default_settings.put("server_groups", server_groups);
        Vector<Properties> server_list = new Vector<Properties>();
        Properties server_item = new Properties();
        server_item.put("serverType", "FTP");
        server_item.put("ip", "lookup");
        server_item.put("port", "21");
        server_item.put("ftp_aware_router", "true");
        server_item.put("require_encryption", "false");
        server_item.put("explicit_ssl", "true");
        server_item.put("explicit_tls", "true");
        server_item.put("linkedServer", "MainUsers");
        server_list.addElement(server_item);
        server_item = new Properties();
        server_item.put("serverType", "HTTP");
        server_item.put("ip", "lookup");
        server_item.put("port", "8080");
        server_item.put("new_http", "true");
        server_item.put("require_encryption", "false");
        server_item.put("explicit_ssl", "true");
        server_item.put("explicit_tls", "true");
        server_item.put("linkedServer", "MainUsers");
        server_list.addElement(server_item);
        server_item = new Properties();
        server_item.put("serverType", "HTTP");
        server_item.put("ip", "lookup");
        server_item.put("port", "9090");
        server_item.put("new_http", "true");
        server_item.put("require_encryption", "false");
        server_item.put("explicit_ssl", "true");
        server_item.put("explicit_tls", "true");
        server_item.put("linkedServer", "MainUsers");
        server_list.addElement(server_item);
        server_item = new Properties();
        server_item.put("serverType", "HTTPS");
        server_item.put("ip", "lookup");
        server_item.put("port", "443");
        server_item.put("new_http", "true");
        server_item.put("require_encryption", "false");
        server_item.put("explicit_ssl", "true");
        server_item.put("explicit_tls", "true");
        server_item.put("linkedServer", "MainUsers");
        server_list.addElement(server_item);
        server_item = new Properties();
        server_item.put("serverType", "SFTP");
        server_item.put("ip", "lookup");
        server_item.put("port", "2222");
        server_item.put("ssh_cipher_list", "aes128-ctr,aes192-ctr,aes256-ctr,3des-ctr,3des-cbc,blowfish-cbc,arcfour,arcfour128,arcfour256,aes128-gcm@openssh.com,aes256-gcm@openssh.com");
        server_item.put("key_exchanges", "curve25519-sha256@libssh.org,diffie-hellman-group-exchange-sha256,diffie-hellman-group18-sha512,diffie-hellman-group17-sha512,diffie-hellman-group16-sha512,diffie-hellman-group15-sha512,diffie-hellman-group14-sha256,diffie-hellman-group14-sha1,ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521,diffie-hellman-group-exchange-sha1,rsa2048-sha256,rsa1024-sha1");
        server_item.put("ssh_mac_list", "hmac-sha256,hmac-sha2-256,hmac-sha256@ssh.com,hmac-sha2-256-etm@openssh.com,hmac-sha2-256-96,hmac-sha512,hmac-sha2-512,hmac-sha512@ssh.com,hmac-sha2-512-etm@openssh.com,hmac-sha2-512-96,hmac-sha1,hmac-sha1-etm@openssh.com,hmac-sha1-96,hmac-ripemd160,hmac-ripemd160@openssh.com,hmac-ripemd160-etm@openssh.com,hmac-md5,hmac-md5-etm@openssh.com,hmac-md5-96");
        server_item.put("ftp_welcome_message", "");
        server_item.put("ssh_rsa_key", String.valueOf(System.getProperty("crushftp.prefs")) + "ssh_host_rsa_key");
        server_item.put("ssh_dsa_key", String.valueOf(System.getProperty("crushftp.prefs")) + "ssh_host_dsa_key");
        server_item.put("ssh_ecsa_key", String.valueOf(System.getProperty("crushftp.prefs")) + "ssh_host_ecdsa_key");
        server_item.put("ssh_ed25519_key", String.valueOf(System.getProperty("crushftp.prefs")) + "ssh_host_ed25519_key");
        server_item.put("ssh_dsa_enabled", "false");
        server_item.put("ssh_rsa_enabled", "true");
        server_item.put("ssh_ecdsa_enabled", "false");
        server_item.put("ssh_ed25519_enabled", "false");
        server_item.put("ssh_transfer_threads", "2");
        server_item.put("ssh_accept_threads", "2");
        server_item.put("ssh_connect_threads", "2");
        server_item.put("ssh_require_password", "false");
        server_item.put("ssh_require_publickey", "false");
        server_item.put("ssh_text_encoding", "UTF8");
        server_item.put("ssh_session_timeout", "300");
        server_item.put("linkedServer", "MainUsers");
        server_list.addElement(server_item);
        default_settings.put("server_list", server_list);
        default_settings.put("plugins", new Vector());
        default_settings.put("CustomForms", new Vector());
        default_settings.put("stats_min", "10");
        default_settings.put("stats_transfer_days", "90");
        default_settings.put("stats_session_days", "90");
        default_settings.put("last_download_wait", "0");
        default_settings.put("last_upload_wait", "0");
        default_settings.put("server_download_queue_size", "0");
        default_settings.put("server_upload_queue_size", "0");
        default_settings.put("server_download_queue_size_max", "0");
        default_settings.put("server_upload_queue_size_max", "0");
        default_settings.put("user_log_buffer", "500");
        default_settings.put("filter_log_text", "");
        default_settings.put("recycle_path", "");
        default_settings.put("recycle", "false");
        default_settings.put("max_users", "5");
        default_settings.put("max_max_users", "5");
        default_settings.put("max_server_download_speed", "0");
        default_settings.put("max_server_upload_speed", "0");
        default_settings.put("bandwidth_immune_ips", "");
        default_settings.put("blank_passwords", "false");
        default_settings.put("smtp_server", "");
        default_settings.put("smtp_ssl", "false");
        default_settings.put("smtp_html", "true");
        default_settings.put("smtp_report_html", "true");
        default_settings.put("smtp_user", "");
        default_settings.put("smtp_pass", "");
        default_settings.put("smtp_from", "");
        default_settings.put("filename_filters_str", "");
        Properties ip_data = new Properties();
        ip_data.put("type", "A");
        ip_data.put("start_ip", "0.0.0.0");
        ip_data.put("stop_ip", "255.255.255.255");
        Vector<Properties> ip_vec = new Vector<Properties>();
        ip_vec.addElement(ip_data);
        default_settings.put("ip_restrictions", ip_vec);
        Properties login_page_data = new Properties();
        login_page_data.put("domain", "*");
        login_page_data.put("page", "login.html");
        Vector<Properties> login_page_vec = new Vector<Properties>();
        login_page_vec.addElement(login_page_data);
        default_settings.put("login_page_list", login_page_vec);
        default_settings.put("login_custom_script", "");
        default_settings.put("login_header", "");
        default_settings.put("login_footer", "");
        default_settings.put("day_of_week_allow", "1234567");
        default_settings.put("log_allow_str", "(GENERAL)(ERROR)(START)(STOP)(QUIT_SERVER)(RUN_SERVER)(KICK)(BAN)(DENIAL)(ACCEPT)(DISCONNECT)(USER)(PASS)(SYST)(NOOP)(SIZE)(MDTM)(RNFR)(RNTO)(PWD)(CWD)(TYPE)(REST)(DELE)(MKD)(RMD)(MACB)(ABOR)(RETR)(STOR)(APPE)(LIST)(NLST)(CDUP)(PASV)(PORT)(AUTH)(PBSZ)(PROT)(SITE)(QUIT)(GET)(PUT)(DELETE)(MOVE)(STAT)(HELP)(PAUSE_RESUME)(PROXY)(MLSD)(MLST)(EPSV)(EPRT)(OPTS)(POST)(WEBINTERFACE)(STOU)(DELETE)(MOVE)(PROPFIND)(MKCOL)(PUT)(LOCK)(MLSD)(MLST)");
        default_settings.put("write_to_log", "true");
        default_settings.put("binary_mode", "false");
        default_settings.put("binary_mode_stor", "false");
        default_settings.put("show_date_time", "true");
        default_settings.put("roll_log", "true");
        default_settings.put("roll_log_size", "10");
        default_settings.put("roll_log_count", "30");
        default_settings.put("hammer_attempts_http", "100");
        default_settings.put("hammer_banning_http", "3");
        default_settings.put("ban_timeout_http", "5");
        default_settings.put("hammer_attempts", "100");
        default_settings.put("hammer_banning", "10");
        default_settings.put("ban_timeout", "5");
        default_settings.put("chammer_attempts", "100");
        default_settings.put("chammer_banning", "30");
        default_settings.put("cban_timeout", "5");
        default_settings.put("phammer_attempts", "15");
        default_settings.put("phammer_banning", "30");
        default_settings.put("pban_timeout", "0");
        default_settings.put("hban_timeout", "0");
        default_settings.put("hack_usernames", "administrator,admin,root");
        default_settings.put("never_ban", "127.0.0.1");
        default_settings.put("miniURLHost", "http://www.domain.com:8080/");
        default_settings.put("user_default_folder_privs", "(read)(view)(resume)");
        default_settings.put("log_date_format", "MM/dd/yyyy HH:mm:ss.SSS");
        default_settings.put("localization", "English");
        default_settings.put("ssh_encoding", "UTF8");
        default_settings.put("webdav_timezone", "0");
        default_settings.put("random_password_length", "6");
        default_settings.put("min_password_length", "3");
        default_settings.put("min_password_numbers", "0");
        default_settings.put("min_password_lowers", "0");
        default_settings.put("min_password_uppers", "0");
        default_settings.put("min_password_specials", "0");
        default_settings.put("password_history_count", "0");
        default_settings.put("unsafe_password_chars", "@#%/:\\");
        default_settings.put("unsafe_filename_chars", "");
        default_settings.put("restart_script", "");
        default_settings.put("prefs_version", "6");
        default_settings.put("allow_ssh_0_byte_file", "true");
        default_settings.put("ssh_randomaccess", "false");
        default_settings.put("log_roll_date_format", "yyyyMMdd_HHmmss");
        default_settings.put("log_roll_rename_hours", "0");
        default_settings.put("bytes_label", "Bytes");
        default_settings.put("bytes_label_short", "B");
        default_settings.put("terrabytes_label", "Terra");
        default_settings.put("gigabytes_label", "Giga");
        default_settings.put("megabytes_label", "Mega");
        default_settings.put("kilobytes_label", "Kilo");
        default_settings.put("terrabytes_label_short", "T");
        default_settings.put("gigabytes_label_short", "G");
        default_settings.put("megabytes_label_short", "M");
        default_settings.put("kilobytes_label_short", "K");
        default_settings.put("resolve_inheritance", "true");
        default_settings.put("make_dir_uploads", "false");
        default_settings.put("temp_account_share_web_forms", "false");
        default_settings.put("temp_account_share_web_buttons", "true");
        default_settings.put("temp_account_share_web_customizations", "true");
        default_settings.put("temp_account_share_web_css", "true");
        default_settings.put("temp_account_share_web_javascript", "true");
        default_settings.put("temp_account_pgp_settings", "false");
        default_settings.put("stop_listing_on_login_failure", "true");
        default_settings.put("acl_mode", "2");
        default_settings.put("acl_lookup_tool", "plugins/lib/aclchk.exe");
        default_settings.put("ssh_header", String.valueOf(System.getProperty("appname", "CrushFTP")) + "SSHD");
        default_settings.put("logging_db_url", "jdbc:mysql://127.0.0.1:3306/" + System.getProperty("appname", "CrushFTP").toLowerCase() + "?autoReconnect=true");
        default_settings.put("logging_db_driver_file", "./mysql-connector-java-5.0.4-bin.jar");
        default_settings.put("logging_db_driver", "org.gjt.mm.mysql.Driver");
        default_settings.put("logging_db_user", System.getProperty("appname", "CrushFTP").toLowerCase());
        default_settings.put("logging_db_pass", "");
        default_settings.put("logging_db_insert", "insert into " + System.getProperty("appname", "CrushFTP").toUpperCase() + "_LOG (LOG_MILLIS,LOG_TAG,LOG_DATA,LOG_ROW_NUM) values(?,?,?,?)");
        default_settings.put("logging_db_query_count", "select max(LOG_ROW_NUM) from " + System.getProperty("appname", "CrushFTP").toUpperCase() + "_LOG");
        default_settings.put("logging_db_query", "select LOG_DATA,LOG_MILLIS,LOG_ROW_NUM from " + System.getProperty("appname", "CrushFTP").toUpperCase() + "_LOG where LOG_ROW_NUM >= ? and LOG_ROW_NUM <= ? order by LOG_ROW_NUM");
        default_settings.put("custom_delete_msg", "\"%user_the_command_data%\" delete successful.");
        default_settings.put("trust_expired_client_cert", "false");
        default_settings.put("email_reset_token", "false");
        default_settings.put("direct_link_access", "false");
        default_settings.put("rnto_overwrite", "false");
        default_settings.put("jobs_location", System.getProperty("crushftp.prefs"));
        default_settings.put("resume_idle_job_delay", "30");
        default_settings.put("password_salt_location", "");
        default_settings.put("replicate_session_host_port", "");
        default_settings.put("search_max_content_kb", "2");
        default_settings.put("find_list_previews", "true");
        default_settings.put("generic_ftp_responses", "false");
        default_settings.put("block_hack_username_immediately", "false");
        default_settings.put("recent_user_log_days", "7");
        default_settings.put("recent_job_log_days", "7");
        default_settings.put("recent_temp_job_log_days", "7");
        default_settings.put("recent_job_days", "7");
        default_settings.put("recent_temp_job_days", "7");
        default_settings.put("csrf", "true");
        default_settings.put("lsla_year", "false");
        default_settings.put("s3_threads_upload", "3");
        default_settings.put("s3_threads_download", "3");
        default_settings.put("csrf_flipped", "false");
        default_settings.put("syslog_encoding", "UTF8");
        default_settings.put("make_upload_parent_folders", "false");
        default_settings.put("search_keywords_also", "true");
        default_settings.put("file_client_not_found_error", "true");
        default_settings.put("max_event_threads", "100");
        default_settings.put("write_session_logs", "true");
        default_settings.put("http_buffer", "10");
        default_settings.put("memcache", "false");
        default_settings.put("normalize_utf8", "true");
        default_settings.put("track_last_logins", "true");
        default_settings.put("allow_session_caching_memory", "false");
        default_settings.put("ssl_renegotiation_blocked", "true");
        default_settings.put("reset_token_timeout", "10");
        default_settings.put("send_dot_dot_list_secure", "true");
        default_settings.put("send_dot_dot_list_sftp", "true");
        default_settings.put("exif_listings", "false");
        default_settings.put("max_job_summary_scan", "300");
        default_settings.put("s3_max_buffer_download", "100");
        default_settings.put("calculate_transfer_usage_listings", "false");
        default_settings.put("audit_job_logs", "false");
        default_settings.put("whitelist_web_commands", "batchComplete,logout,getXMLListing");
        default_settings.put("proxy_list_max", "0");
        default_settings.put("allow_save_pass_phone", "false");
        default_settings.put("hide_ftp_quota_log", "false");
        default_settings.put("multi_journal", "false");
        default_settings.put("hash_algorithm", "MD5");
        default_settings.put("ssh_rename_overwrite", "false");
        default_settings.put("plugin_log_call", "false");
        default_settings.put("single_job_scheduler_serverbeat", "true");
        default_settings.put("fips140", "false");
        default_settings.put("fips140_sftp_client", "false");
        default_settings.put("fips140_sftp_server", "false");
        default_settings.put("max_url_length", "99999");
        default_settings.put("ssh_runtime_exception", "false");
        default_settings.put("strip_windows_domain_webdav", "true");
        default_settings.put("single_report_scheduler_serverbeat", "true");
        default_settings.put("include_ftp_nlst_path", "false");
        default_settings.put("store_job_items", "true");
        default_settings.put("s3crush_replicated", "true");
        default_settings.put("replicate_jobs", "true");
        default_settings.put("auto_fix_stats_sessions", "true");
        default_settings.put("temp_accounts_account_expire_task", "");
        default_settings.put("separate_speeds_by_username_ip", "false");
        default_settings.put("update_proxy_type", "");
        default_settings.put("update_proxy_host", "");
        default_settings.put("update_proxy_port", "");
        default_settings.put("update_proxy_user", "");
        default_settings.put("update_proxy_pass", "");
        default_settings.put("pasv_bind_all", "false");
        default_settings.put("cookie_expire_hours", "0");
        default_settings.put("job_scheduler_enabled", "true");
        default_settings.put("minimum_speed_warn_seconds", "10");
        default_settings.put("minimum_speed_alert_seconds", "30");
        default_settings.put("file_encrypt_ascii", "false");
        default_settings.put("smtp_subject_encoded", "false");
        default_settings.put("replicated_server_ips", "*");
        default_settings.put("dmz_stat_caching", "false");
        default_settings.put("direct_link_to_webinterface", "true");
        default_settings.put("stor_pooling", "true");
        default_settings.put("tls_version_client", "SSLv2Hello,TLSv1.2,TLSv1.3");
        default_settings.put("tunnel_minimum_version", "3.4.0");
        default_settings.put("delete_threads", "40");
        default_settings.put("drop_folder_rename_new", "false");
        default_settings.put("serverbeat_relative_timing", "true");
        default_settings.put("windows_character_encoding_process", "windows-1252");
        default_settings.put("memory_log_interval", "600");
        default_settings.put("dump_threads_log_interval", "-1");
        default_settings.put("replicated_vfs_url", "");
        default_settings.put("replicated_vfs_root_url", "");
        default_settings.put("replicated_vfs_user", "");
        default_settings.put("replicated_vfs_pass", "");
        default_settings.put("replicated_vfs_ping_interval", "60");
        default_settings.put("replicated_auto_play_journal", "true");
        default_settings.put("startup_delay", "0");
        default_settings.put("s3_sha256", "false");
        default_settings.put("block_bad_ftp_socket_paths", "true");
        default_settings.put("as2_mic_alg", "optional, sha-256, sha1, md5");
        default_settings.put("temp_account_bad_timeout", "30");
        default_settings.put("s3_ignore_partial", "false");
        default_settings.put("expire_password_email_token_only", "false");
        default_settings.put("block_client_renegotiation", "true");
        default_settings.put("webdav_agent_learning", "true");
        default_settings.put("debug_socks_log", "false");
        default_settings.put("run_alerts_dmz", "false");
        default_settings.put("http_header1", "");
        default_settings.put("http_header2", "");
        default_settings.put("http_header3", "");
        default_settings.put("http_header4", "");
        default_settings.put("http_header5", "");
        default_settings.put("http_header6", "");
        default_settings.put("http_header7", "");
        default_settings.put("http_header8", "");
        default_settings.put("http_header9", "");
        default_settings.put("http_header10", "");
        default_settings.put("http_header11", "");
        default_settings.put("http_header12", "");
        default_settings.put("http_header13", "");
        default_settings.put("http_header14", "");
        default_settings.put("http_header15", "");
        default_settings.put("http_header16", "");
        default_settings.put("http_header17", "");
        default_settings.put("http_header18", "");
        default_settings.put("http_header19", "");
        default_settings.put("http_header20", "");
        default_settings.put("otp_url", "https://{otp_username}:{otp_password}@api.twilio.com/2010-04-01/Accounts/{otp_username}/Messages.json");
        default_settings.put("otp_username", "");
        default_settings.put("otp_password", "");
        default_settings.put("otp_extra1", "");
        default_settings.put("otp_extra2", "");
        default_settings.put("otp_url_verb", "POST");
        default_settings.put("otp_from", "");
        default_settings.put("otp_post", "To={otp_to}&From={otp_from}&Body={otp_token}");
        default_settings.put("otp_validated_logins", "false");
        default_settings.put("otp_token_timeout", "60000");
        default_settings.put("html5_chunk_timeout", "600");
        default_settings.put("crushauth_httponly", "true");
        default_settings.put("recursive_rename_event", "false");
        default_settings.put("recursive_delete_event", "false");
        default_settings.put("vfs_cache_interval", "60");
        default_settings.put("vfs_cache_enabled", "false");
        default_settings.put("expired_accounts_notify_now", "false");
        default_settings.put("expired_passwords_notify_now", "false");
        default_settings.put("stop_dmz_ports_internal_down", "true");
        default_settings.put("job_max_runtime_hours", "");
        default_settings.put("job_max_runtime_minutes", "");
        default_settings.put("task_max_runtime_hours", "");
        default_settings.put("task_max_runtime_minutes", "");
        default_settings.put("crushftp_smtp_sasl", "false");
        default_settings.put("allow_router_ban", "false");
        default_settings.put("report_prefix", "");
        default_settings.put("replicate_reports", "false");
        default_settings.put("serverbeat_dmz_master", "false");
        default_settings.put("synchronized_sftp", "true");
        default_settings.put("dmz_log_in_internal_server", "false");
        default_settings.put("track_jobs_for_reports", "false");
        default_settings.put("admin_ips", "127.0.0.1,*");
        default_settings.put("ignore_ssh_closefile_download_error", "true");
        default_settings.put("http_redirect_base", "DISABLED");
        default_settings.put("search_file_contents_also", "false");
        default_settings.put("multi_journal_timeout", "30000");
        default_settings.put("unsafe_filename_chars_rename", "");
        default_settings.put("webinterface_redirect_with_password", "true");
        default_settings.put("share_attached_file_size_limit", "10");
        default_settings.put("max_http_header_length", "4000");
        default_settings.put("pgp_check_downloads", "true");
        default_settings.put("sha3_keccak", "false");
        default_settings.put("ignore_failed_directory_listings", "false");
        default_settings.put("max_items_dir", "-1");
        default_settings.put("sftp_mkdir_exist_silent", "false");
        default_settings.put("log_ftp_client_listings", "true");
        default_settings.put("single_preview_serverbeat", "false");
        default_settings.put("max_denied_ips", "100");
        default_settings.put("as2_sha256", "sha-256");
        default_settings.put("expire_share_notify_days", "0");
        default_settings.put("share_expire_notify_task", "");
        default_settings.put("server_info_history_days", "30");
        default_settings.put("replicate_shares", "true");
        default_settings.put("always_validate_plugins_for_dmz_lookup", "true");
        default_settings.put("dmz_upload_ack_socket", "false");
        default_settings.put("send_report_link", "false");
        default_settings.put("ui_save_preferences", "");
        default_settings.put("replicate_preferences", "true");
        default_settings.put("max_connection_single_ip", "100");
        default_settings.put("max_password_resets_per_minute", "10");
        default_settings.put("validate_internal_share_username", "false");
        default_settings.put("ftp_cwd_validate", "true");
        default_settings.put("ssh_bypass_jce2", "false");
        default_settings.put("ssh_bouncycastle", "false");
        default_settings.put("job_statistics_enabled", "false");
        default_settings.put("glaciercrush_replicated", "true");
        default_settings.put("http_header_timeout", "30000");
        default_settings.put("show_vfs_root_folders_mdtm", "false");
        default_settings.put("encryption_pass_needed", "false");
        default_settings.put("encryption_pass_needed_test", "");
        default_settings.put("dmz_socket_pool_size", "50");
        default_settings.put("disconnect_ftp_on_socket_error", "false");
        default_settings.put("max_dmz_socket_idle_time", "10000");
        default_settings.put("report_memory_protection", "true");
        default_settings.put("strip_slashes", "false");
        default_settings.put("webinterface_show_password_rule", "true");
        default_settings.put("send_dmz_error_events_to_internal", "true");
        default_settings.put("scan_vfs_for_initial_listing", "true");
        default_settings.put("secondary_login_via_email", "false");
        default_settings.put("secondary_login_via_email_cache_interval", "5");
        default_settings.put("azure_upload_max_threads", "2");
        default_settings.put("max_html5_pending_upload_chunks", "11");
        default_settings.put("banned_ip_message", "Your IP is banned, no further requests will be processed from this IP");
        default_settings.put("encryption_cypher", "CAST5");
        default_settings.put("remove_keywords_on_delete", "true");
        default_settings.put("securedelete", "false");
        default_settings.put("lowercase_all_s3_paths", "false");
        default_settings.put("save_temp_bans", "false");
        default_settings.put("sql_users_reset_cache_on_changes", "true");
        default_settings.put("thread_dump_delayed_login", "false");
        default_settings.put("include_ftp_nlst_path_for_all_pattern", "false");
        default_settings.put("pgp_integrity_protect", "true");
        default_settings.put("http_same_site", "None");
        default_settings.put("sftp_chunk_buffer", "200");
        default_settings.put("dmz_memory_queue", "20");
        default_settings.put("pgp_http_downloads_variable_size", "false");
        default_settings.put("dmz_pong_timeout", "20");
        default_settings.put("reverse_dns_user_ip", "true");
        default_settings.put("dmz_chunk_temp_storage", "./dmz_tmp/");
        default_settings.put("http_chunk_temp_storage", "");
        default_settings.put("s3_one_delete_attempt", "false");
        default_settings.put("limited_admin_vfs_overwrite", "true");
        default_settings.put("smtp_xoauth2", "false");
        default_settings.put("smtp_client_id", "");
        default_settings.put("smtp_client_secret", "");
        default_settings.put("smtp_client_scope", "");
        default_settings.put("smtp_client_action", "");
        default_settings.put("smtp_client_params", "");
        default_settings.put("smtp_client_url", "");
        default_settings.put("twofactor_secret_auto_otp_enable", "false");
        default_settings.put("low_memory_trigger_value1", "40");
        default_settings.put("low_memory_trigger_value2", "30");
        default_settings.put("low_memory_trigger_value3", "20");
        default_settings.put("allow_memory_reload_of_users", "true");
        default_settings.put("invalid_usernames_seconds_attempts", "3");
        default_settings.put("allow_symlink_webinterface", "false");
        default_settings.put("ssh_disable_rsa_checks", "false");
        default_settings.put("test_vfs_return_error", "false");
        default_settings.put("webdav_agents", "");
        default_settings.put("dmzv3_two_sockets", "false");
        default_settings.put("azure_share_list_threads_count", "10");
        default_settings.put("login_autocomplete_off", "false");
        default_settings.put("merge_events", "false");
        default_settings.put("stat_auto_increment", "false");
        default_settings.put("sftp_round_seconds_up", "true");
        default_settings.put("allow_symlink_checking", "true");
        default_settings.put("s3_use_contianer_credentials_relative_uri", "false");
        default_settings.put("smtp_start_tls_allowed", "true");
        default_settings.put("geoip_access_key", "");
        default_settings.put("otp_numeric", "false");
        default_settings.put("validate_upload_physical_file_size", "false");
        default_settings.put("validate_upload_physical_file_size_error", "false");
        default_settings.put("hint_decrypted_size", "true");
        default_settings.put("encryption_sign", "false");
        default_settings.put("sftp_client_disableAutoFlush", "false");
        default_settings.put("login_hammer_attempts", "60");
        default_settings.put("login_hammer_interval", "30");
        default_settings.put("login_hammer_timeout", "1");
        default_settings.put("v10_beta", "true");
        default_settings.put("reverse_events", "false");
        default_settings.put("reverse_events_skip_sender", "false");
        default_settings.put("reset_url", "https://www.domain.com/WebInterface/jQuery/reset.html");
        default_settings.put("generatetoken_limited_admin_group_only", "false");
        default_settings.put("block_failed_filetransfer_events", "false");
        default_settings.put("http_cleaner_interval", "60");
        default_settings.put("webdav_timeout_secs", "10");
        default_settings.put("s3_buffer_download", "5");
        default_settings.put("encrypt_job_files_sensitive_data", "true");
        default_settings.put("pasv_simple_logic", "true");
        default_settings.put("max_resume_job_size_mb", "200");
        default_settings.put("thread_dump_port", "");
        default_settings.put("job_link_task_render", "true");
        default_settings.put("serverbeat_start_ports", "false");
        default_settings.put("as2_from_as_to_for_username", "false");
        default_settings.put("as2_prepend_as2_username", "true");
        default_settings.put("check_file_length_download_event", "true");
        if (Common.machine_is_windows()) {
            default_settings.put("serverbeat_command", "netsh");
        } else {
            default_settings.put("serverbeat_command", "/sbin/ifconfig {adapter}:{index} {vip} netmask {netmask} up");
        }
        default_settings.put("serverbeat_ifup_command", "/sbin/ifup {adapter}:{index}");
        default_settings.put("serverbeat_ifdown_command", "/sbin/ifdown {adapter}:{index}");
        default_settings.put("serverbeat_post_command", "");
        default_settings.put("serverbeat_command_disable", "/sbin/ifconfig {adapter}:{index} {ip} netmask {netmask} down");
        if (Common.machine_is_windows()) {
            default_settings.put("serverbeat_command_disable", "netsh");
        } else {
            default_settings.put("serverbeat_command_disable", "/sbin/ifconfig {adapter}:{index} {vip} netmask {netmask} down");
        }
        default_settings.put("vfs_lazy_load", "false");
        default_settings.put("quota_async", "false");
        default_settings.put("quota_async_cache_interval", "60");
        default_settings.put("quota_async_local_only", "false");
        default_settings.put("quota_async_threads", "3");
        default_settings.put("quota_async_user_threads", "3");
        default_settings.put("sftpclient_ls_dot", "false");
        default_settings.put("log_buffer_memory", "0");
        default_settings.put("s3_global_cache", "true");
        default_settings.put("dfs_default_enabled", "true");
        default_settings.put("blocked_ssh_clients", "");
        default_settings.put("jvm_timezone", "default");
        default_settings.put("replicated_vfs_root_url_offset", "0");
        default_settings.put("extra_system_properties", "");
        default_settings.put("block_symlinks", "false");
        default_settings.put("md5sum_native_exec", "false");
        default_settings.put("xml_user_read_retries", "5");
        default_settings.put("password_blacklist", "{working_dir}/password_blacklist.txt");
        default_settings.put("job_log_name", "{scheduleName}_{id}.log");
        default_settings.put("sftp_login_timeout_max", "10");
        default_settings.put("html5_max_pending_download_chunks", "40");
        default_settings.put("html5_max_pending_download_mb", "200");
        default_settings.put("temp_upload_ext_ignore_protocols", "s3,glacier,azure,sharepoint,sharepoint2,onedrive,dropbox,hadoop,box");
        default_settings.put("reveal_vfs_protocol_end_user", "false");
        default_settings.put("user_manager_admin_all_connection_groups", "false");
        default_settings.put("allow_default_user_updates", "true");
        default_settings.put("kill_prior_upload_session_if_file_in_use", "true");
        default_settings.put("user_reveal_version", "false");
        default_settings.put("user_reveal_hostname", "false");
        default_settings.put("active_jobs_shutdown_wait_secs", "5");
        default_settings.put("merged_vfs", "false");
        default_settings.put("filepart_silent_ignore", "false");
        default_settings.put("sftp_loadbalancer_header_presend", "false");
        default_settings.put("stats_ignore_unauthenticated_users", "false");
        default_settings.put("icap_scanning", "false");
        default_settings.put("icap_server_host_port", "");
        default_settings.put("icap_service", "avscan");
        default_settings.put("icap_max_bytes", "104857600");
        default_settings.put("ssh_client_key_exchanges", "");
        default_settings.put("ssh_client_mac_list", "");
        default_settings.put("ssh_client_cipher_list", "");
        default_settings.put("successful_login_hammering_neverban", "");
        default_settings.put("max_password_length", "500");
        default_settings.put("smb3_kerberos_kdc", "");
        default_settings.put("smb3_kerberos_realm", "");
        default_settings.put("fail_stor_0_byte_versus_data_received", "false");
        default_settings.put("job_cache_update_interval_minutes", "1");
        default_settings.put("job_summary_on_dashboard", "true");
        default_settings.put("s3_ec2_imdsv2", "false");
        default_settings.put("max_job_xml_size", "400");
        default_settings.put("job_start_threads", "50");
        default_settings.put("validate_upload_physical_file_size_ignore_protocols", "sharepoint,sharepoint2,onedrive");
        default_settings.put("sftp_client_listing_disableDirectoryCheck", "false");
        default_settings.put("quota_async_user_delay", "1");
        default_settings.put("quota_async_vfs_delay", "1");
        default_settings.put("proxy_protocol_ftp_pasv", "false");
        default_settings.put("ftp_pre_check_mkdir", "false");
        default_settings.put("v11_beta", "false");
        PreviewWorker.getDefaults(default_settings);
        StatTools.setDefaults(default_settings);
        SearchTools.setDefaults(default_settings);
        SyncTools.setDefaults(default_settings);
    }
}

