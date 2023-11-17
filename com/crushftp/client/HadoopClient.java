/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.http.HttpEntity
 *  org.apache.http.auth.AuthScope
 *  org.apache.http.auth.Credentials
 *  org.apache.http.client.ClientProtocolException
 *  org.apache.http.client.CredentialsProvider
 *  org.apache.http.client.methods.CloseableHttpResponse
 *  org.apache.http.client.methods.HttpDelete
 *  org.apache.http.client.methods.HttpGet
 *  org.apache.http.client.methods.HttpPut
 *  org.apache.http.client.methods.HttpUriRequest
 *  org.apache.http.config.Lookup
 *  org.apache.http.config.Registry
 *  org.apache.http.config.RegistryBuilder
 *  org.apache.http.conn.socket.LayeredConnectionSocketFactory
 *  org.apache.http.conn.ssl.NoopHostnameVerifier
 *  org.apache.http.conn.ssl.SSLConnectionSocketFactory
 *  org.apache.http.entity.ContentType
 *  org.apache.http.entity.InputStreamEntity
 *  org.apache.http.impl.auth.SPNegoSchemeFactory
 *  org.apache.http.impl.client.BasicCredentialsProvider
 *  org.apache.http.impl.client.CloseableHttpClient
 *  org.apache.http.impl.client.HttpClientBuilder
 *  org.apache.http.impl.client.HttpClients
 *  org.apache.http.ssl.SSLContextBuilder
 *  org.apache.http.ssl.SSLContexts
 *  org.apache.http.ssl.TrustStrategy
 *  org.apache.log4j.BasicConfigurator
 *  org.apache.log4j.Level
 *  org.apache.log4j.Logger
 *  org.json.simple.JSONArray
 *  org.json.simple.JSONObject
 *  org.json.simple.JSONValue
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class HadoopClient
extends GenericClient {
    static Properties resourceIdCache = new Properties();
    private String action = "";
    private String local_file = "";
    static Properties non_active_url_cache = new Properties();
    LoginContext loginContext = null;

    public static void main(String[] args) throws Exception {
        VRL vrl = new VRL(args[0]);
        HadoopClient instance = new HadoopClient(args[0], "HADOOP:", null);
        instance.parseOptions(args);
        instance.login(vrl.getUsername(), vrl.getPassword(), "");
        instance.query();
    }

    public HadoopClient(String url, String logHeader, Vector logQueue) {
        super(logHeader, logQueue);
        this.fields = new String[]{"username", "password", "login_config", "kerberos_config", "acceptAnyCert", "user_realm", "auth_security"};
        this.url = url;
        if (System.getProperty("crushftp.debug", "0").equals("2")) {
            if (Logger.getLogger((String)"org.apache.http").getLevel() != Level.ALL) {
                BasicConfigurator.configure();
                Logger.getLogger((String)"org.apache.http").setLevel(Level.ALL);
            }
        } else if (Logger.getLogger((String)"org.apache.http") != null && Logger.getLogger((String)"org.apache.http").getLevel() != Level.OFF) {
            BasicConfigurator.resetConfiguration();
            Logger.getLogger((String)"org.apache.http").setLevel(Level.OFF);
        }
    }

    public void query() {
        try {
            final VRL vrl = new VRL(this.url);
            this.loginContext = new LoginContext("KrbLogin", new KerberosCallBackHandler(this.config.getProperty("username"), this.config.getProperty("password")));
            this.loginContext.login();
            PrivilegedAction sendAction = new PrivilegedAction(){

                public Object run() {
                    long start = System.currentTimeMillis();
                    try {
                        if (HadoopClient.this.action.equals("list")) {
                            Vector v = new Vector();
                            HadoopClient.this.list(vrl.getPath(), v);
                            System.out.println(v);
                        } else if (HadoopClient.this.action.equals("download")) {
                            FileOutputStream fout = new FileOutputStream(HadoopClient.this.local_file, false);
                            InputStream tin = HadoopClient.this.download(vrl.getPath(), 0L, -1L, true);
                            Common.streamCopier(tin, fout, false, true, true);
                        } else if (HadoopClient.this.action.equals("upload")) {
                            FileInputStream fin = new FileInputStream(HadoopClient.this.local_file);
                            OutputStream tout = HadoopClient.this.upload(vrl.getPath(), 0L, true, true);
                            Common.streamCopier(fin, tout, false, true, true);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("Transfer time:" + (System.currentTimeMillis() - start) + "ms");
                    System.exit(0);
                    return new Boolean(true);
                }
            };
            Subject.doAs(this.loginContext.getSubject(), sendAction);
        }
        catch (Exception le) {
            le.printStackTrace();
        }
    }

    private void parseOptions(String[] args) {
        int i = 1;
        while (i < args.length - 1) {
            if (args[i].equals("-l")) {
                this.config.put("login_config", args[++i]);
            }
            if (args[i].equals("-k")) {
                this.config.put("kerberos_config", args[++i]);
            }
            if (args[i].equals("-action")) {
                this.action = args[++i];
                System.out.println("action:" + this.action);
            }
            if (args[i].equals("-file")) {
                this.local_file = args[++i];
                System.out.println("local_file:" + this.local_file);
            }
            ++i;
        }
    }

    private CloseableHttpClient createHttpClient() {
        String[] enabled_ciphers = null;
        if (System.getProperties().containsKey("crushftp.enabled_ciphers")) {
            enabled_ciphers = System.getProperty("crushftp.enabled_ciphers").split(",");
        }
        HttpClientBuilder hcb = HttpClients.custom().setDefaultAuthSchemeRegistry((Lookup)this.buildSPNEGOAuthSchemeRegistry()).setDefaultCredentialsProvider(this.setupCredentialsProvider());
        try {
            SSLContext sslc = null;
            sslc = this.config.getProperty("acceptAnyCert", "true").equalsIgnoreCase("true") ? new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy(){

                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build() : SSLContexts.createDefault();
            hcb.setSSLSocketFactory((LayeredConnectionSocketFactory)new SSLConnectionSocketFactory(sslc, System.getProperty("crushftp.tls_version_client", "SSLv2Hello,TLSv1,TLSv1.1,TLSv1.2").split(","), enabled_ciphers, (HostnameVerifier)new NoopHostnameVerifier()));
        }
        catch (Exception e) {
            Common.log("SERVER", 0, e);
        }
        return hcb.build();
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        if (Logger.getLogger((String)"org.apache.http") != null) {
            if (System.getProperty("crushftp.debug", "0").equals("2")) {
                if (Logger.getLogger((String)"org.apache.http").getLevel() != Level.ALL) {
                    BasicConfigurator.configure();
                    Logger.getLogger((String)"org.apache.http").setLevel(Level.ALL);
                }
            } else if (Logger.getLogger((String)"org.apache.http").getLevel() != Level.OFF) {
                BasicConfigurator.resetConfiguration();
                Logger.getLogger((String)"org.apache.http").setLevel(Level.OFF);
            }
        }
        password = VRL.vrlDecode(password);
        this.config.put("username", username);
        this.config.put("password", password);
        String k_user_name = this.config.getProperty("username");
        if (!this.config.getProperty("user_realm", "").equals("")) {
            k_user_name = String.valueOf(k_user_name) + "@" + this.config.getProperty("user_realm", "");
        }
        this.setJavaSecurityProperties(true, this.config.getProperty("login_config"), this.config.getProperty("kerberos_config"));
        this.loginContext = new LoginContext("KrbLogin", new KerberosCallBackHandler(k_user_name, this.config.getProperty("password")));
        this.loginContext.login();
        return "Success";
    }

    @Override
    public void logout() throws Exception {
        this.close();
    }

    @Override
    public Vector list(final String path, Vector list) throws Exception {
        if (list == null) {
            list = new Vector();
        }
        final Vector list2 = list;
        boolean inc = false;
        Exception last_e = null;
        final Properties status = new Properties();
        int x = 0;
        while (x < 5) {
            if (VRL.getActiveUrl(this.url, inc) == null) break;
            if (this.isNonActive()) {
                last_e = new Exception("Hadoop: The url is not active: " + new VRL(VRL.getActiveUrl(this.url, false)).safe());
            } else {
                PrivilegedAction sendAction = new PrivilegedAction(){

                    public Object run() {
                        try {
                            status.put("obj", HadoopClient.this.list4(path, list2));
                            status.put("status", "DONE");
                        }
                        catch (Exception e) {
                            status.put("error", e);
                            status.put("status", "ERROR");
                        }
                        return new Boolean(true);
                    }
                };
                Subject.doAs(this.loginContext.getSubject(), sendAction);
                int i = 1;
                while (!status.containsKey("status")) {
                    Thread.sleep(i++ < 100 ? i : 100);
                }
                status.remove("status");
                if (status.containsKey("error")) {
                    last_e = (Exception)status.remove("error");
                    inc = true;
                    non_active_url_cache.put(VRL.getActiveUrl(this.url, false), String.valueOf(System.currentTimeMillis()));
                    Thread.sleep(1000L);
                } else {
                    last_e = null;
                    break;
                }
            }
            ++x;
        }
        if (last_e != null) {
            throw last_e;
        }
        return (Vector)status.remove("obj");
    }

    private Vector list4(String path, Vector list) throws Exception {
        CloseableHttpClient httpclient = this.createHttpClient();
        String url_path = this.format_path_for_url(path);
        String url_str = String.valueOf(url_path) + "?op=LISTSTATUS" + this.getHadoopAuth();
        this.log("List:" + new VRL(url_str).safe());
        CloseableHttpResponse response = httpclient.execute((HttpUriRequest)new HttpGet(new URL(url_str).toURI()));
        if (response.getStatusLine().getStatusCode() == 403) {
            throw new Exception("Hadoop: List - " + response.getStatusLine());
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new ClientProtocolException("Hadoop: List - Response contains no content:" + response);
        }
        String json = Common.consumeResponse(entity.getContent());
        if (response.getStatusLine().getStatusCode() >= 400) {
            this.log("Hadoop :Status Code :" + response.getStatusLine().getStatusCode() + json + "\r\n");
            httpclient.close();
            throw new Exception("Hadoop: List - " + response.getStatusLine());
        }
        httpclient.close();
        try {
            Object obj = ((JSONObject)((JSONObject)JSONValue.parse((String)json)).get((Object)"FileStatuses")).get((Object)"FileStatus");
            if (obj instanceof JSONArray) {
                JSONArray ja = (JSONArray)obj;
                int x = 0;
                while (x < ja.size()) {
                    Object obj2 = ja.get(x);
                    if (obj2 instanceof JSONObject) {
                        Properties stat = this.parseFileStatus(path, obj2);
                        list.addElement(stat);
                    }
                    ++x;
                }
            }
        }
        catch (Exception e) {
            throw new Exception("Hadoop: List - " + e.getMessage());
        }
        return list;
    }

    private Properties parseFileStatus(String path, Object obj) throws Exception {
        SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        Properties item = new Properties();
        JSONObject jo = (JSONObject)obj;
        if (jo != null) {
            Common.log("HADOOP", 2, String.valueOf(jo.toJSONString()) + "\r\n");
        }
        boolean folder = false;
        if (jo.get((Object)"type").equals("DIRECTORY")) {
            folder = true;
        }
        Object[] a = jo.entrySet().toArray();
        int i = 0;
        while (i < a.length) {
            String key2 = a[i].toString().split("=")[0];
            item.put(key2.trim(), ("" + jo.get((Object)key2)).trim());
            ++i;
        }
        Date d = new Date(Long.parseLong(item.getProperty("modificationTime", "0")));
        String line = String.valueOf(folder ? "d" : "-") + "rwxrwxrwx   1    owner   group   " + item.getProperty("length") + "   " + yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + item.getProperty("pathSuffix");
        Common.log("HADOOP", 2, String.valueOf(line) + "\r\n");
        Properties stat = HadoopClient.parseStat(line, false);
        String fileId = item.getProperty("fileId");
        if (fileId == null) {
            fileId = String.valueOf(this.url) + path.substring(1) + stat.getProperty("name");
        }
        stat.put("resource_id", fileId);
        stat.put("url", String.valueOf(this.url) + path.substring(1) + stat.getProperty("name"));
        resourceIdCache.put(String.valueOf(path) + stat.getProperty("name"), fileId);
        if (stat.getProperty("type", "").equalsIgnoreCase("DIR")) {
            resourceIdCache.put(String.valueOf(path) + stat.getProperty("name") + "/", fileId);
        }
        return stat;
    }

    private Properties getFileStatus(String path) throws Exception {
        CloseableHttpClient httpclient = this.createHttpClient();
        CloseableHttpResponse response = null;
        boolean inc = false;
        Exception last_e = null;
        int x = 0;
        while (x < 5) {
            if (VRL.getActiveUrl(this.url, inc) == null) break;
            if (this.isNonActive()) {
                last_e = new Exception("Hadoop: The url is not active: " + new VRL(VRL.getActiveUrl(this.url, false)).safe());
            } else {
                String url_path = this.format_path_for_url(path);
                String url_str = String.valueOf(url_path) + "?op=GETFILESTATUS" + this.getHadoopAuth();
                try {
                    this.log("Stat:" + new VRL(url_str).safe());
                    response = httpclient.execute((HttpUriRequest)new HttpGet(new URL(url_str).toURI()));
                    last_e = null;
                    if (response.getStatusLine().getStatusCode() < 400 || response.getStatusLine().getStatusCode() == 404) break;
                    this.log("URL: " + new VRL(this.url).safe() + " STAT: Response message: " + response.getStatusLine());
                    inc = true;
                }
                catch (Exception e) {
                    last_e = e;
                    non_active_url_cache.put(VRL.getActiveUrl(this.url, false), String.valueOf(System.currentTimeMillis()));
                    inc = true;
                    Thread.sleep(300L);
                }
            }
            ++x;
        }
        if (last_e != null) {
            throw last_e;
        }
        if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() >= 400) {
            return null;
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new ClientProtocolException("Hadoop: Response contains no content:" + response);
        }
        String json = Common.consumeResponse(entity.getContent());
        Properties stat = this.parseFileStatus(path, ((JSONObject)JSONValue.parse((String)json)).get((Object)"FileStatus"));
        if (stat.getProperty("name").equals("/")) {
            stat.put("name", Common.last(path));
        }
        httpclient.close();
        return stat;
    }

    @Override
    protected OutputStream upload3(final String path, final long startPos, final boolean truncate, final boolean binary) throws Exception {
        final Properties status = new Properties();
        PrivilegedAction sendAction = new PrivilegedAction(){

            public Object run() {
                try {
                    status.put("obj", HadoopClient.this.upload4(path, startPos, truncate, binary));
                    status.put("status", "DONE");
                }
                catch (Exception e) {
                    status.put("error", e);
                    status.put("status", "ERROR");
                }
                return new Boolean(true);
            }
        };
        Subject.doAs(this.loginContext.getSubject(), sendAction);
        int i = 1;
        while (!status.containsKey("status")) {
            Thread.sleep(i++ < 100 ? i : 100);
        }
        if (status.containsKey("error")) {
            throw (Exception)status.remove("error");
        }
        this.out = (OutputStream)status.remove("obj");
        return this.out;
    }

    private OutputStream upload4(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        CloseableHttpClient httpclient = this.createHttpClient();
        CloseableHttpResponse response = null;
        boolean inc = false;
        Exception last_e = null;
        int x = 0;
        while (x < 5) {
            if (VRL.getActiveUrl(this.url, inc) == null) break;
            if (this.isNonActive()) {
                last_e = new Exception("Hadoop: The url is not active: " + new VRL(VRL.getActiveUrl(this.url, false)).safe());
            } else {
                String url_str = String.valueOf(this.format_path_for_url(path)) + "?op=CREATE&permission=777&overwrite=true" + this.getHadoopAuth();
                try {
                    this.log("Upload:" + new VRL(url_str).safe());
                    response = httpclient.execute((HttpUriRequest)new HttpPut(new URL(url_str).toURI()));
                    last_e = null;
                    break;
                }
                catch (Exception e) {
                    last_e = e;
                    inc = true;
                    non_active_url_cache.put(VRL.getActiveUrl(this.url, false), String.valueOf(System.currentTimeMillis()));
                    Thread.sleep(1000L);
                }
            }
            ++x;
        }
        if (last_e != null) {
            throw last_e;
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new ClientProtocolException("Hadoop: Response contains no content");
        }
        String json = Common.consumeResponse(entity.getContent());
        if (!response.containsHeader("Location")) {
            throw new IOException("Redirect url missing. Upload response : " + json);
        }
        String redirect_url = response.getFirstHeader("Location").getValue();
        if (response.getStatusLine().getStatusCode() >= 400) {
            this.log("Hadoop :Status Code :" + response.getStatusLine().getStatusCode() + json + "\r\n");
            httpclient.close();
            return null;
        }
        httpclient.close();
        final CloseableHttpClient httpclient_data = this.createHttpClient();
        this.log("Upload3:" + new VRL(redirect_url).safe());
        final HttpPut httpPutData = new HttpPut(new URL(redirect_url).toURI());
        Properties socks = Common.getConnectedSocks(false);
        Socket sock1 = (Socket)socks.remove("sock1");
        Socket sock2 = (Socket)socks.remove("sock2");
        InputStreamEntity reqEntity = new InputStreamEntity(sock2.getInputStream(), -1L, ContentType.APPLICATION_OCTET_STREAM);
        reqEntity.setChunked(true);
        httpPutData.setEntity((HttpEntity)reqEntity);
        final Properties status = new Properties();
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                block2: {
                    try {
                        httpclient_data.execute((HttpUriRequest)httpPutData);
                        status.put("status", "DONE");
                    }
                    catch (Exception e) {
                        HadoopClient.this.log(e);
                        status.put("e", e);
                        status.put("status", "ERROR");
                        if (e.getCause() == null) break block2;
                        HadoopClient.this.log("Caused by: " + e.getCause());
                    }
                }
            }
        }, Thread.currentThread() + ":HADOOP_PUT:" + reqEntity);
        class OutputWrapper
        extends OutputStream {
            OutputStream out3 = null;
            boolean closed = false;
            private final /* synthetic */ Properties val$status;

            public OutputWrapper(OutputStream out3, Properties properties) {
                this.val$status = properties;
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
                this.out3.close();
                this.closed = true;
                int i = 1;
                long start = System.currentTimeMillis();
                while (!this.val$status.containsKey("status")) {
                    try {
                        Thread.sleep(i++);
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                    if (i > 100) {
                        i = 100;
                    }
                    if (System.currentTimeMillis() - start <= 60000L) continue;
                    throw new IOException("Timeout closing hadoop file...");
                }
                if (this.val$status.getProperty("status").equals("ERROR")) {
                    throw new IOException((Exception)this.val$status.get("e"));
                }
            }
        }
        return new OutputWrapper(sock1.getOutputStream(), status);
    }

    @Override
    public boolean upload_0_byte(String path) throws Exception {
        return true;
    }

    @Override
    protected InputStream download3(final String path, final long startPos, final long endPos, final boolean binary) throws Exception {
        final Properties status = new Properties();
        PrivilegedAction sendAction = new PrivilegedAction(){

            public Object run() {
                try {
                    status.put("obj", HadoopClient.this.download4(path, startPos, endPos, binary));
                    status.put("status", "DONE");
                }
                catch (Exception e) {
                    status.put("error", e);
                    status.put("status", "ERROR");
                }
                return new Boolean(true);
            }
        };
        Subject.doAs(this.loginContext.getSubject(), sendAction);
        int i = 1;
        while (!status.containsKey("status")) {
            Thread.sleep(i++ < 100 ? i : 100);
        }
        if (status.containsKey("error")) {
            throw (Exception)status.remove("error");
        }
        this.in = (InputStream)status.remove("obj");
        return this.in;
    }

    private InputStream download4(String path, long startPos, long endPos, boolean binary) throws Exception {
        CloseableHttpClient httpclient = this.createHttpClient();
        CloseableHttpResponse response = null;
        boolean inc = false;
        Exception last_e = null;
        int x = 0;
        while (x < 5) {
            if (VRL.getActiveUrl(this.url, inc) == null) break;
            if (this.isNonActive()) {
                last_e = new Exception("Hadoop: The url is not active: " + new VRL(VRL.getActiveUrl(this.url, false)).safe());
            } else {
                String url_path = this.format_path_for_url(path);
                String url_str = String.valueOf(url_path) + "?op=OPEN&buffersize=32768" + this.getHadoopAuth();
                try {
                    this.log("Download:" + new VRL(url_str).safe());
                    response = httpclient.execute((HttpUriRequest)new HttpGet(new URL(url_str).toURI()));
                    last_e = null;
                    break;
                }
                catch (Exception e) {
                    last_e = e;
                    inc = true;
                    non_active_url_cache.put(VRL.getActiveUrl(this.url, false), String.valueOf(System.currentTimeMillis()));
                    Thread.sleep(1000L);
                }
            }
            ++x;
        }
        if (last_e != null) {
            throw last_e;
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new ClientProtocolException("Hadoop: Response contains no content");
        }
        if (response.getStatusLine().getStatusCode() >= 400) {
            String json = Common.consumeResponse(entity.getContent());
            this.log("Hadoop :SatusCode: " + response.getStatusLine().getStatusCode() + json + "\r\n");
            httpclient.close();
            return null;
        }
        return entity.getContent();
    }

    @Override
    public boolean makedir(final String path) throws Exception {
        final Properties status = new Properties();
        PrivilegedAction sendAction = new PrivilegedAction(){

            public Object run() {
                try {
                    status.put("obj", String.valueOf(HadoopClient.this.makedir4(path)));
                    status.put("status", "DONE");
                }
                catch (Exception e) {
                    status.put("error", e);
                    status.put("status", "ERROR");
                }
                return new Boolean(true);
            }
        };
        Subject.doAs(this.loginContext.getSubject(), sendAction);
        int i = 1;
        while (!status.containsKey("status")) {
            Thread.sleep(i++ < 100 ? i : 100);
        }
        if (status.containsKey("error")) {
            throw (Exception)status.remove("error");
        }
        return status.getProperty("obj").equals("true");
    }

    private boolean makedir4(String path) throws Exception {
        CloseableHttpClient httpclient = this.createHttpClient();
        CloseableHttpResponse response = null;
        boolean inc = false;
        Exception last_e = null;
        int x = 0;
        while (x < 5) {
            if (VRL.getActiveUrl(this.url, inc) == null) break;
            if (this.isNonActive()) {
                last_e = new Exception("Hadoop: The url is not active: " + new VRL(VRL.getActiveUrl(this.url, false)).safe());
            } else {
                String url_path = this.format_path_for_url(path);
                String url_str = String.valueOf(url_path) + "?op=MKDIRS&permission=777" + this.getHadoopAuth();
                try {
                    this.log("MakeDir:" + new VRL(url_str).safe());
                    response = httpclient.execute((HttpUriRequest)new HttpPut(new URL(url_str).toURI()));
                    last_e = null;
                    break;
                }
                catch (Exception e) {
                    last_e = e;
                    inc = true;
                    non_active_url_cache.put(VRL.getActiveUrl(this.url, false), String.valueOf(System.currentTimeMillis()));
                    Thread.sleep(1000L);
                }
            }
            ++x;
        }
        if (last_e != null) {
            throw last_e;
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new ClientProtocolException("Hadoop: Response contains no content");
        }
        String json = Common.consumeResponse(entity.getContent());
        httpclient.close();
        Boolean result = (Boolean)((JSONObject)JSONValue.parse((String)json)).get((Object)"boolean");
        return result;
    }

    @Override
    public boolean makedirs(final String path) throws Exception {
        final Properties status = new Properties();
        PrivilegedAction sendAction = new PrivilegedAction(){

            public Object run() {
                try {
                    status.put("obj", String.valueOf(HadoopClient.this.makedirs4(path)));
                    status.put("status", "DONE");
                }
                catch (Exception e) {
                    status.put("error", e);
                    status.put("status", "ERROR");
                }
                return new Boolean(true);
            }
        };
        Subject.doAs(this.loginContext.getSubject(), sendAction);
        int i = 1;
        while (!status.containsKey("status")) {
            Thread.sleep(i++ < 100 ? i : 100);
        }
        if (status.containsKey("error")) {
            throw (Exception)status.remove("error");
        }
        return status.getProperty("obj").equals("true");
    }

    private boolean makedirs4(String path) throws Exception {
        boolean ok = true;
        String[] parts = path.split("/");
        String path2 = "";
        int x = 0;
        while (x < parts.length && ok) {
            path2 = String.valueOf(path2) + parts[x] + "/";
            if (x >= 1 && this.stat(path2) == null) {
                ok = this.makedir(path2);
            }
            ++x;
        }
        return ok;
    }

    @Override
    public boolean delete(final String path) throws Exception {
        final Properties status = new Properties();
        PrivilegedAction sendAction = new PrivilegedAction(){

            public Object run() {
                try {
                    status.put("obj", String.valueOf(HadoopClient.this.delete4(path)));
                    status.put("status", "DONE");
                }
                catch (Exception e) {
                    status.put("error", e);
                    status.put("status", "ERROR");
                }
                return new Boolean(true);
            }
        };
        Subject.doAs(this.loginContext.getSubject(), sendAction);
        int i = 1;
        while (!status.containsKey("status")) {
            Thread.sleep(i++ < 100 ? i : 100);
        }
        if (status.containsKey("error")) {
            throw (Exception)status.remove("error");
        }
        return status.getProperty("obj").equals("true");
    }

    private boolean delete4(String path) throws Exception {
        CloseableHttpClient httpclient = this.createHttpClient();
        CloseableHttpResponse response = null;
        boolean inc = false;
        Exception last_e = null;
        int x = 0;
        while (x < 5) {
            if (VRL.getActiveUrl(this.url, inc) == null) break;
            if (this.isNonActive()) {
                last_e = new Exception("Hadoop: The url is not active: " + new VRL(VRL.getActiveUrl(this.url, false)).safe());
            } else {
                String url_path = this.format_path_for_url(path);
                String url_str = String.valueOf(url_path) + "?op=DELETE&recursive=true" + this.getHadoopAuth();
                try {
                    this.log("Delete:" + new VRL(url_str).safe());
                    response = httpclient.execute((HttpUriRequest)new HttpDelete(new URL(url_str).toURI()));
                    last_e = null;
                    break;
                }
                catch (Exception e) {
                    last_e = e;
                    inc = true;
                    non_active_url_cache.put(VRL.getActiveUrl(this.url, false), String.valueOf(System.currentTimeMillis()));
                    Thread.sleep(1000L);
                }
            }
            ++x;
        }
        if (last_e != null) {
            throw last_e;
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new ClientProtocolException("Hadoop: Response contains no content");
        }
        String json = Common.consumeResponse(entity.getContent());
        httpclient.close();
        Boolean result = (Boolean)((JSONObject)JSONValue.parse((String)json)).get((Object)"boolean");
        if (result.booleanValue()) {
            if (resourceIdCache.containsKey(path)) {
                resourceIdCache.remove(path);
            }
            if (resourceIdCache.containsKey(String.valueOf(path) + "/")) {
                resourceIdCache.remove(String.valueOf(path) + "/");
            }
        }
        return result;
    }

    @Override
    public boolean rename(final String rnfr, final String rnto, boolean overwrite) throws Exception {
        final Properties status = new Properties();
        PrivilegedAction sendAction = new PrivilegedAction(){

            public Object run() {
                try {
                    status.put("obj", String.valueOf(HadoopClient.this.rename4(rnfr, rnto)));
                    status.put("status", "DONE");
                }
                catch (Exception e) {
                    status.put("error", e);
                    status.put("status", "ERROR");
                }
                return new Boolean(true);
            }
        };
        Subject.doAs(this.loginContext.getSubject(), sendAction);
        int i = 1;
        while (!status.containsKey("status")) {
            Thread.sleep(i++ < 100 ? i : 100);
        }
        if (status.containsKey("error")) {
            throw (Exception)status.remove("error");
        }
        return status.getProperty("obj").equals("true");
    }

    private boolean rename4(String rnfr, String rnto) throws Exception {
        CloseableHttpClient httpclient = this.createHttpClient();
        CloseableHttpResponse response = null;
        boolean inc = false;
        Exception last_e = null;
        int x = 0;
        while (x < 5) {
            if (VRL.getActiveUrl(this.url, inc) == null) break;
            if (this.isNonActive()) {
                last_e = new Exception("Hadoop: The url is not active: " + new VRL(VRL.getActiveUrl(this.url, false)).safe());
            } else {
                String url_path = this.format_path_for_url(rnfr);
                String url_str = String.valueOf(url_path) + "?op=RENAME" + this.getHadoopAuth() + "&destination=" + Common.url_encode(rnto);
                try {
                    this.log("Rename:" + new VRL(url_str).safe());
                    response = httpclient.execute((HttpUriRequest)new HttpPut(new URL(url_str).toURI()));
                    last_e = null;
                    break;
                }
                catch (Exception e) {
                    last_e = e;
                    inc = true;
                    non_active_url_cache.put(VRL.getActiveUrl(this.url, false), String.valueOf(System.currentTimeMillis()));
                    Thread.sleep(1000L);
                }
            }
            ++x;
        }
        if (last_e != null) {
            throw last_e;
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new ClientProtocolException("Hadoop: Response contains no content");
        }
        String json = Common.consumeResponse(entity.getContent());
        Boolean result = (Boolean)((JSONObject)JSONValue.parse((String)json)).get((Object)"boolean");
        if (result == null) {
            throw new Exception("Missing result! Result : " + json);
        }
        if (result.booleanValue()) {
            if (resourceIdCache.containsKey(rnfr)) {
                resourceIdCache.remove(rnfr);
            }
            if (resourceIdCache.containsKey(String.valueOf(rnfr) + "/")) {
                resourceIdCache.remove(String.valueOf(rnfr) + "/");
            }
        }
        return result;
    }

    @Override
    public boolean mdtm(final String path, final long modified) throws Exception {
        final Properties status = new Properties();
        PrivilegedAction sendAction = new PrivilegedAction(){

            public Object run() {
                try {
                    status.put("obj", String.valueOf(HadoopClient.this.mdtm4(path, modified)));
                    status.put("status", "DONE");
                }
                catch (Exception e) {
                    status.put("error", e);
                    status.put("status", "ERROR");
                }
                return new Boolean(true);
            }
        };
        Subject.doAs(this.loginContext.getSubject(), sendAction);
        int i = 1;
        while (!status.containsKey("status")) {
            Thread.sleep(i++ < 100 ? i : 100);
        }
        if (status.containsKey("error")) {
            throw (Exception)status.remove("error");
        }
        return status.getProperty("obj").equals("true");
    }

    private boolean mdtm4(String path, long modified) throws Exception {
        CloseableHttpClient httpclient = this.createHttpClient();
        CloseableHttpResponse response = null;
        boolean inc = false;
        Exception last_e = null;
        int x = 0;
        while (x < 5) {
            if (VRL.getActiveUrl(this.url, inc) == null) break;
            if (this.isNonActive()) {
                last_e = new Exception("Hadoop: The url is not active: " + new VRL(VRL.getActiveUrl(this.url, false)).safe());
            } else {
                String url_path = this.format_path_for_url(path);
                String url_str = String.valueOf(url_path) + "?op=SETTIMES" + this.getHadoopAuth() + "&modificationtime=" + modified + "&accesstime=" + modified;
                try {
                    this.log("MDTM:" + new VRL(url_str).safe());
                    response = httpclient.execute((HttpUriRequest)new HttpPut(new URL(url_str).toURI()));
                    last_e = null;
                    break;
                }
                catch (Exception e) {
                    last_e = e;
                    non_active_url_cache.put(VRL.getActiveUrl(this.url, false), String.valueOf(System.currentTimeMillis()));
                    inc = true;
                    Thread.sleep(1000L);
                }
            }
            ++x;
        }
        if (last_e != null) {
            throw last_e;
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new ClientProtocolException("Hadoop: Response contains no content");
        }
        String json = Common.consumeResponse(entity.getContent());
        if (response.getStatusLine().getStatusCode() >= 400) {
            this.log("Hadoop :Status Code :" + response.getStatusLine().getStatusCode() + json + "\r\n");
            httpclient.close();
            return false;
        }
        httpclient.close();
        return true;
    }

    @Override
    public Properties stat(final String path) throws Exception {
        final Properties status = new Properties();
        PrivilegedAction sendAction = new PrivilegedAction(){

            public Object run() {
                try {
                    Properties obj = HadoopClient.this.stat4(path);
                    if (obj != null) {
                        status.put("obj", obj);
                    }
                    status.put("status", "DONE");
                }
                catch (Exception e) {
                    status.put("error", e);
                    status.put("status", "ERROR");
                }
                return new Boolean(true);
            }
        };
        Subject.doAs(this.loginContext.getSubject(), sendAction);
        int x = 1;
        while (!status.containsKey("status")) {
            Thread.sleep(x++);
            if (x <= 100) continue;
            x = 100;
        }
        if (status.containsKey("error")) {
            throw (Exception)status.remove("error");
        }
        return (Properties)status.get("obj");
    }

    public Properties stat4(String path) throws Exception {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        Properties p = null;
        p = this.getFileStatus(path);
        return p;
    }

    private String format_path_for_url(String path) {
        if (path.endsWith("/") && !path.equals("/")) {
            path = path.substring(0, path.length() - 1);
        }
        VRL vrl = new VRL(VRL.getActiveUrl(this.url, false));
        String[] path_names = path.split("/");
        String encoded_path = "";
        if (path_names.length > 0) {
            int x = 0;
            while (x < path_names.length) {
                if (!path_names[x].equals("")) {
                    encoded_path = String.valueOf(encoded_path) + "/" + Common.url_encode(path_names[x]);
                }
                ++x;
            }
        } else {
            encoded_path = path;
        }
        String url_tmp = "https://" + vrl.getHost() + ":" + vrl.getPort() + "/webhdfs/v1" + encoded_path;
        return url_tmp;
    }

    boolean isNonActive() {
        if (non_active_url_cache.containsKey(VRL.getActiveUrl(this.url, false))) {
            long start = Long.parseLong(non_active_url_cache.getProperty(VRL.getActiveUrl(this.url, false), "10000"));
            if (System.currentTimeMillis() - start >= 10000L) {
                non_active_url_cache.remove(VRL.getActiveUrl(this.url, false));
            } else {
                return true;
            }
        }
        return false;
    }

    private void setJavaSecurityProperties(boolean debug, String loginConf, String krb5Conf) {
        System.setProperty("java.security.auth.login.config", loginConf);
        System.setProperty("java.security.krb5.conf", krb5Conf);
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        if (debug) {
            System.setProperty("sun.security.krb5.debug", "true");
        }
    }

    private Registry buildSPNEGOAuthSchemeRegistry() {
        return RegistryBuilder.create().register("Negotiate", (Object)new SPNegoSchemeFactory(true)).build();
    }

    private String getHadoopAuth() {
        if (this.config.getProperty("auth_security", "false").equals("false")) {
            return "&user.name=" + this.config.getProperty("username");
        }
        return "";
    }

    private CredentialsProvider setupCredentialsProvider() {
        Credentials use_jaas_creds = new Credentials(){

            public String getPassword() {
                return null;
            }

            public Principal getUserPrincipal() {
                return null;
            }
        };
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, -1, AuthScope.ANY_REALM), use_jaas_creds);
        return credsProvider;
    }

    private class KerberosCallBackHandler
    implements CallbackHandler {
        private final String user;
        private final String password;

        public KerberosCallBackHandler(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            int x = 0;
            while (x < callbacks.length) {
                Callback callback = callbacks[x];
                if (callback instanceof NameCallback) {
                    NameCallback nc = (NameCallback)callback;
                    nc.setName(this.user);
                } else if (callback instanceof PasswordCallback) {
                    PasswordCallback pc = (PasswordCallback)callback;
                    pc.setPassword(this.password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(callback, "Unknown Callback");
                }
                ++x;
            }
        }
    }
}

