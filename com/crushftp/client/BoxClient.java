/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bouncycastle.asn1.pkcs.PrivateKeyInfo
 *  org.bouncycastle.openssl.PEMParser
 *  org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
 *  org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder
 *  org.bouncycastle.operator.InputDecryptorProvider
 *  org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo
 */
package com.crushftp.client;

import com.crushftp.client.Base64;
import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.GenericClient;
import com.crushftp.client.S3CrushClient;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class BoxClient
extends GenericClient {
    static Properties resourceIdCache = new Properties();
    String enterpriseID = "";
    String bearer = "";
    String clientID = "";
    String clientSecret = "";
    String publicKeyId = "";
    String userId = "";
    PrivateKey key = null;
    String upload_root = "./box/";

    public BoxClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"username", "password", "token_start", "token_expire", "box_store_jwt_json", "box_jwt_config_content", "box_enterprise_id", "box_client_id", "box_public_key_id", "box_private_key", "box_private_pass_phrase", "box_meta_md5_and_upload_by", "uploaded_by", "uploaded_md5"};
        System.setProperty("crushtunnel.debug", "2");
        if (!url.endsWith("/")) {
            url = String.valueOf(url) + "/";
        }
        this.url = url;
        this.upload_root = System.getProperty("crushftp.box_upload_root", "./box/");
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        username = VRL.vrlDecode(username);
        password = VRL.vrlDecode(password);
        this.config.put("username", username.trim());
        this.config.put("password", password);
        try {
            this.load_connection_info(this.config.getProperty("login_config", ""));
        }
        catch (Exception e) {
            throw new Exception("ERROR : Bad credentials : Could not load athentication related configs! Error :" + e);
        }
        Properties p = new Properties();
        try {
            p = this.renew_access_token();
        }
        catch (Exception e) {
            throw new Exception("ERROR : Bad credentials : Could not renew Access Token! Error :" + e);
        }
        if (p.containsKey("expires_in")) {
            this.config.put("token_start", String.valueOf(System.currentTimeMillis()));
            String expire_in = p.getProperty("expires_in");
            if (expire_in.endsWith(",")) {
                expire_in = expire_in.substring(0, expire_in.length() - 1);
            }
            this.config.put("token_expire", expire_in);
        }
        if (!p.containsKey("access_token")) {
            throw new IOException("ERROR : Bad credentials : Authentication failure: Missing token!");
        }
        this.bearer = p.getProperty("access_token");
        if (!username.equals("")) {
            String result;
            URLConnection urlc = URLConnection.openConnection(new VRL("https://api.box.com/2.0/users/"), new Properties());
            urlc.setDoOutput(false);
            urlc.setRequestMethod("GET");
            urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
            urlc.setRequestProperty("Content-Type", "application/json");
            int code = urlc.getResponseCode();
            if (code < 200 || code > 299) {
                String result2 = URLConnection.consumeResponse(urlc.getInputStream());
                if (urlc.getHeaderField("WWW-AUTHENTICATE") != null && urlc.getHeaderField("WWW-AUTHENTICATE").contains("error")) {
                    result2 = String.valueOf(result2) + " " + urlc.getHeaderField("WWW-AUTHENTICATE");
                }
                urlc.disconnect();
                this.log(String.valueOf(result2) + "\r\n");
                throw new Exception("ERROR : Bad credentials : Could not find username! Error: " + result2);
            }
            String json = Common.consumeResponse(urlc.getInputStream());
            Object obj = ((JSONObject)JSONValue.parse(json)).get("entries");
            if (obj instanceof JSONArray) {
                JSONArray ja = (JSONArray)obj;
                int xxx = 0;
                while (xxx < ja.size()) {
                    Object obj2 = ja.get(xxx);
                    if (obj2 instanceof JSONObject) {
                        Properties item = new Properties();
                        JSONObject jo = (JSONObject)obj2;
                        boolean folder = false;
                        if (jo.get("type").equals("folder")) {
                            folder = true;
                        }
                        Object[] a = jo.entrySet().toArray();
                        int i = 0;
                        while (i < a.length) {
                            String key2 = a[i].toString().split("=")[0];
                            item.put(key2.trim(), ("" + jo.get(key2)).trim());
                            ++i;
                        }
                        this.log("Box login : User name to check : " + item.getProperty("login", "").trim());
                        if (item.getProperty("login", "").trim().equals(username.trim())) {
                            this.log("Box login : User id  : " + item.getProperty("id", "").trim());
                            this.userId = item.getProperty("id", "");
                            break;
                        }
                    }
                    ++xxx;
                }
            }
            if (this.userId.equals("") && username.matches("[0-9]+")) {
                this.log("Box login : User id : " + username);
                URLConnection urlc2 = URLConnection.openConnection(new VRL("https://api.box.com/2.0/users/" + username), this.config);
                urlc2.setDoOutput(false);
                urlc2.setRequestMethod("GET");
                urlc2.setRequestProperty("Authorization", "Bearer " + this.getBearer());
                urlc2.setRequestProperty("Content-Type", "application/json");
                int code2 = urlc2.getResponseCode();
                if (code2 < 200 || code2 > 299) {
                    result = "";
                    if (code2 == 404) {
                        result = String.valueOf(result) + "Error : Wrong user id! ";
                    }
                    result = String.valueOf(result) + URLConnection.consumeResponse(urlc2.getInputStream());
                    if (urlc2.getHeaderField("WWW-AUTHENTICATE") != null && urlc2.getHeaderField("WWW-AUTHENTICATE").contains("error")) {
                        result = String.valueOf(result) + " " + urlc2.getHeaderField("WWW-AUTHENTICATE");
                    }
                    urlc2.disconnect();
                    this.log(String.valueOf(result) + "\r\n");
                    throw new Exception("ERROR : Bad credentials : Invalid username! Error: " + result);
                }
                this.userId = username;
            }
            if (this.userId.equals("")) {
                throw new Exception("ERROR : Bad credentials : Invalid username! Error: Could not find user! Username : " + username);
            }
            URLConnection urlc_list = URLConnection.openConnection(new VRL("https://api.box.com/2.0/folders/0/items?limit=1"), this.config);
            urlc_list.setDoOutput(false);
            urlc_list.setRequestMethod("GET");
            urlc_list.setRequestProperty("as-user", this.userId);
            urlc_list.setRequestProperty("authorization", "Bearer " + this.getBearer());
            urlc_list.setRequestProperty("Content-Type", "application/json");
            int code3 = urlc_list.getResponseCode();
            if (code3 < 200 || code3 > 299) {
                result = "ERROR : Bad credentials : Invalid Custom Application config! Error : " + urlc_list.getResponseMessage() + " " + URLConnection.consumeResponse(urlc_list.getInputStream());
                if (urlc_list.getHeaderField("WWW-AUTHENTICATE") != null && urlc_list.getHeaderField("WWW-AUTHENTICATE").contains("error")) {
                    result = String.valueOf(result) + " " + urlc_list.getHeaderField("WWW-AUTHENTICATE");
                }
                if (code3 == 403) {
                    result = String.valueOf(result) + " Check your Custom Appliaction's Configuration. At App Access Level select the App + Enterprise Access and at Advanced Feautures check the Make API calls using the as-user header. Any changes on the Custom application requires Reauthorize App.";
                }
                urlc_list.disconnect();
                this.log(String.valueOf(result) + "\r\n");
                throw new Exception(result);
            }
        }
        this.config.put("logged_out", "false");
        return "Success";
    }

    private void load_connection_info(String config_url) throws Exception {
        String privateKey = "";
        String passphrase = "";
        String content = "";
        if (!config_url.equals("")) {
            VRL vrl_json = new VRL(config_url);
            GenericClient c = Common.getClient(Common.getBaseUrl(vrl_json.toString()), "CrushTask7", new Vector());
            if (c instanceof S3CrushClient) {
                c.login(vrl_json.getUsername(), vrl_json.getPassword(), Common.all_but_last(vrl_json.getPath()));
            } else {
                c.login(vrl_json.getUsername(), vrl_json.getPassword(), "");
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Common.streamCopier(null, null, c.download(vrl_json.getPath(), 0L, -1L, false), baos, false, true, true);
            content = new String(baos.toByteArray(), "UTF8");
            JSONObject jo = (JSONObject)JSONValue.parse(content);
            this.enterpriseID = (String)jo.get("enterpriseID");
            JSONObject boxAppSettings = (JSONObject)jo.get("boxAppSettings");
            JSONObject appAuth = (JSONObject)boxAppSettings.get("appAuth");
            this.clientID = (String)boxAppSettings.get("clientID");
            this.clientSecret = (String)boxAppSettings.get("clientSecret");
            this.publicKeyId = (String)appAuth.get("publicKeyID");
            privateKey = (String)appAuth.get("privateKey");
            passphrase = (String)appAuth.get("passphrase");
        } else if (this.config.getProperty("box_store_jwt_json", "false").equals("true") && !this.config.getProperty("box_jwt_config_content", "").equals("")) {
            content = Common.encryptDecrypt(this.config.getProperty("box_jwt_config_content", ""), false);
            JSONObject jo = (JSONObject)JSONValue.parse(content);
            this.enterpriseID = (String)jo.get("enterpriseID");
            JSONObject boxAppSettings = (JSONObject)jo.get("boxAppSettings");
            JSONObject appAuth = (JSONObject)boxAppSettings.get("appAuth");
            this.clientID = (String)boxAppSettings.get("clientID");
            this.clientSecret = (String)boxAppSettings.get("clientSecret");
            this.publicKeyId = (String)appAuth.get("publicKeyID");
            privateKey = (String)appAuth.get("privateKey");
            passphrase = (String)appAuth.get("passphrase");
        } else {
            this.enterpriseID = this.config.getProperty("box_enterprise_id", "");
            this.clientID = this.config.getProperty("box_client_id", "");
            this.clientSecret = this.config.getProperty("password", "laA8fq0RGOREbomf9jjZtwGk7r6MSUZW");
            this.publicKeyId = this.config.getProperty("box_public_key_id", "");
            privateKey = Common.encryptDecrypt(this.config.getProperty("box_private_key", ""), false);
            privateKey = Common.replace_str(privateKey, "\\n", "\n");
            passphrase = Common.encryptDecrypt(this.config.getProperty("box_private_pass_phrase", ""), false);
        }
        PEMParser pp = new PEMParser((Reader)new BufferedReader(new StringReader(privateKey)));
        Object o = pp.readObject();
        JceOpenSSLPKCS8DecryptorProviderBuilder decryptBuilder = new JceOpenSSLPKCS8DecryptorProviderBuilder().setProvider("BC");
        InputDecryptorProvider decryptProvider = decryptBuilder.build(passphrase.toCharArray());
        PrivateKeyInfo keyInfo = ((PKCS8EncryptedPrivateKeyInfo)o).decryptPrivateKeyInfo(decryptProvider);
        this.key = new JcaPEMKeyConverter().getPrivateKey(keyInfo);
    }

    private String generateAssertion() throws Exception {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(this.clientID);
        claims.setAudience("https://api.box.com/oauth2/token");
        claims.setSubject(this.enterpriseID);
        claims.setClaim("box_sub_type", "enterprise");
        claims.setGeneratedJwtId(64);
        claims.setExpirationTimeMinutesInTheFuture(0.75f);
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(this.key);
        jws.setAlgorithmHeaderValue("RS512");
        jws.setHeader("typ", "JWT");
        jws.setHeader("kid", this.publicKeyId);
        String assertion = jws.getCompactSerialization();
        return assertion;
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        String resourceId = "0";
        if (!path.equals("/")) {
            resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("username", "")) + path, "");
        }
        if (resourceId.equals("") && !path.equals("/")) {
            this.loadResourceIds(Common.all_but_last(path));
            resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("username", "")) + path, "");
        }
        String marker = "";
        int x = 0;
        while (x < 1000) {
            if (this.config.getProperty("logged_out", "false").equals("true")) {
                throw new Exception("Error: Cancel dir listing. The client is already closed.");
            }
            String fields = "name,id,type,modified_at,item_status,size,metadata.global.properties";
            if (this.logHeader.equals("PREVIEW")) {
                fields = String.valueOf(fields) + ",representations";
            }
            URLConnection urlc = URLConnection.openConnection(new VRL("https://api.box.com/2.0/folders/" + resourceId + "/items?fields=" + fields + "&limit=1000&usemarker=true&marker=" + marker), this.config);
            urlc.setDoOutput(false);
            urlc.setRequestMethod("GET");
            if (!this.userId.equals("")) {
                urlc.setRequestProperty("as-user", this.userId);
            }
            urlc.setRequestProperty("authorization", "Bearer " + this.getBearer());
            String json = Common.consumeResponse(urlc.getInputStream());
            this.parseListResult(path, list, json);
            Object obj = ((JSONObject)JSONValue.parse(json)).get("next_marker");
            if (obj == null) break;
            marker = (String)obj;
            ++x;
        }
        return list;
    }

    private void parseListResult(String path, Vector list, String json) throws Exception {
        Object obj;
        SimpleDateFormat sdf_rfc1123_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        SimpleDateFormat sdf_meta = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sdf_meta.setTimeZone(TimeZone.getTimeZone("GMT"));
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        if ((obj = ((JSONObject)JSONValue.parse(json)).get("entries")) instanceof JSONArray) {
            JSONArray ja = (JSONArray)obj;
            int x = 0;
            while (x < ja.size()) {
                Object obj2 = ja.get(x);
                if (obj2 instanceof JSONObject) {
                    Properties item = new Properties();
                    JSONObject jo = (JSONObject)obj2;
                    boolean folder = false;
                    if (jo.get("type").equals("folder")) {
                        folder = true;
                    }
                    if (jo.containsKey("representations")) {
                        try {
                            Vector<Properties> representations = new Vector<Properties>();
                            JSONObject jor = (JSONObject)jo.remove("representations");
                            if (jor.get("entries") != null) {
                                JSONArray jar = (JSONArray)jor.get("entries");
                                int xx = 0;
                                while (xx < jar.size()) {
                                    JSONObject jorep = (JSONObject)jar.get(xx);
                                    if (jorep.get("representation").equals("jpg")) {
                                        Properties p = new Properties();
                                        p.put("type", jorep.get("representation"));
                                        if (jorep.get("properties") != null) {
                                            JSONObject jorep_prop = (JSONObject)jorep.get("properties");
                                            p.put("paged", jorep_prop.get("paged"));
                                            p.put("thumb", jorep_prop.get("thumb"));
                                            p.put("dimensions", jorep_prop.get("dimensions"));
                                        }
                                        JSONObject jorep_info = (JSONObject)jorep.get("info");
                                        p.put("url", jorep_info.get("url"));
                                        representations.add(p);
                                    }
                                    ++xx;
                                }
                            }
                            if (representations.size() > 0) {
                                item.put("thumbnails", representations);
                            }
                        }
                        catch (Exception e) {
                            this.log("PREVIEW", 1, e);
                        }
                    }
                    Object[] a = jo.entrySet().toArray();
                    int i = 0;
                    while (i < a.length) {
                        String key2 = a[i].toString().split("=")[0];
                        if (jo.get(key2) != null) {
                            item.put(key2.trim(), ("" + jo.get(key2)).trim());
                        }
                        ++i;
                    }
                    if (item.getProperty("item_status", "").equals("active")) {
                        Date d;
                        block25: {
                            d = null;
                            if (item.containsKey("metadata")) {
                                try {
                                    JSONObject metadata = (JSONObject)((JSONObject)((JSONObject)JSONValue.parse(item.getProperty("metadata", ""))).get("global")).get("properties");
                                    if (metadata.containsKey("modified_date_time")) {
                                        d = sdf_meta.parse((String)metadata.get("modified_date_time"));
                                    }
                                    if (metadata.containsKey("uploaded_by") && metadata.get("uploaded_by") != null) {
                                        item.put("uploaded_by", metadata.get("uploaded_by"));
                                    }
                                    break block25;
                                }
                                catch (Exception e) {
                                    this.log("Parse metadata:" + e);
                                    if (d == null) {
                                        d = sdf_rfc1123_2.parse(item.getProperty("modified_at"));
                                    }
                                    break block25;
                                }
                            }
                            d = sdf_rfc1123_2.parse(item.getProperty("modified_at"));
                        }
                        String line = String.valueOf(folder ? "d" : "-") + "rwxrwxrwx   1    owner   group   " + item.getProperty("size") + "   " + yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + item.getProperty("name");
                        Properties stat = BoxClient.parseStat(line);
                        stat.put("resource_id", item.getProperty("id"));
                        if (item.containsKey("uploaded_by")) {
                            stat.put("uploaded_by", item.getProperty("uploaded_by", ""));
                        }
                        if (item.containsKey("thumbnails")) {
                            stat.put("thumbnails", item.get("thumbnails"));
                        }
                        stat.put("url", "box://" + VRL.vrlEncode(this.config.getProperty("username", "")) + ":" + this.config.getProperty("password", "") + "@api.box.com" + path + stat.getProperty("name"));
                        resourceIdCache.put(String.valueOf(this.config.getProperty("username", "")) + path + stat.getProperty("name"), item.getProperty("id"));
                        if (stat.getProperty("type", "").equalsIgnoreCase("DIR")) {
                            resourceIdCache.put(String.valueOf(this.config.getProperty("username", "")) + path + stat.getProperty("name") + "/", item.getProperty("id"));
                        }
                        list.addElement(stat);
                    }
                }
                ++x;
            }
        }
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        String parent_id = "";
        if (!Common.all_but_last(path).equals("/")) {
            parent_id = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("username", "")) + Common.all_but_last(path), "");
            if (parent_id.equals("")) {
                Properties p = this.stat(Common.all_but_last(path));
                if (p != null) {
                    parent_id = p.getProperty("resource_id", "");
                }
                if (parent_id.equals("")) {
                    this.log("Upload: Parent path not found:" + path + "\r\n");
                    throw new IOException("Upload: Parent path not found! Path:" + path);
                }
            }
        } else {
            parent_id = "0";
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://api.box.com/2.0/files/content"), this.config);
        urlc.setDoOutput(true);
        urlc.setRequestMethod("OPTIONS");
        if (!this.userId.equals("")) {
            urlc.setRequestProperty("as-user", this.userId);
        }
        urlc.setRequestProperty("Content-Type", "application/json");
        urlc.setRequestProperty("authorization", "Bearer " + this.getBearer());
        JSONObject file_info = new JSONObject();
        file_info.put("name", Common.last(path));
        JSONObject parent = new JSONObject();
        parent.put("id", parent_id);
        file_info.put("parent", parent);
        OutputStream cout = urlc.getOutputStream();
        cout.write(file_info.toString().getBytes("UTF8"));
        cout.close();
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        if (code < 200 || code > 299) {
            this.log(String.valueOf(result) + "\r\n");
            throw new Exception("Error: " + result);
        }
        String folder_id = parent_id;
        class OutputWrapper
        extends OutputStream {
            OutputStream mout = null;
            URLConnection murlc = null;
            MessageDigest digest = null;
            RandomAccessFile f = null;
            String boundary = "";
            String session_id = "";
            String file_id = "";
            boolean closed = false;
            long size = 0L;
            long part_size = 0L;
            long offset = 0L;
            boolean large_file = false;
            JSONArray parts = new JSONArray();
            private final /* synthetic */ String val$path;
            private final /* synthetic */ String val$folder_id;

            public OutputWrapper(String string, String string2) {
                this.val$path = string;
                this.val$folder_id = string2;
                this.boundary = Common.makeBoundary(34);
                try {
                    this.digest = MessageDigest.getInstance("SHA1");
                }
                catch (Exception e) {
                    BoxClient.this.log(e);
                }
                new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + Common.all_but_last(string)).mkdirs();
                if (new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + string).exists()) {
                    new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + string).delete();
                }
                try {
                    this.f = new RandomAccessFile(new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + string), "rw");
                }
                catch (Exception e) {
                    BoxClient.this.log(e);
                }
            }

            @Override
            public void write(int i) throws IOException {
                this.write(new byte[]{(byte)i}, 0, 1);
            }

            @Override
            public void write(byte[] b) throws IOException {
                this.write(b, 0, b.length);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                if (this.size > 0x1900000L) {
                    if (!this.large_file) {
                        try {
                            this.finishMultipartUpload();
                        }
                        catch (Exception e) {
                            BoxClient.this.log(e);
                            if (new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).exists()) {
                                new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).delete();
                            }
                            throw new IOException(e.getMessage());
                        }
                        this.large_file = true;
                    }
                    this.f.write(b, off, len);
                    this.digest.update(b, off, len);
                    this.size += (long)len;
                } else {
                    if (this.mout == null) {
                        try {
                            this.initMultipartUpload();
                        }
                        catch (Exception e) {
                            BoxClient.this.log(e);
                            if (new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).exists()) {
                                new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).delete();
                            }
                            throw new IOException(e.getMessage());
                        }
                    }
                    this.mout.write(b, off, len);
                    this.f.write(b, off, len);
                    this.digest.update(b, off, len);
                    this.size += (long)len;
                }
            }

            @Override
            public void close() throws IOException {
                if (this.closed) {
                    return;
                }
                if (!this.large_file) {
                    try {
                        this.finishMultipartUpload();
                    }
                    catch (Exception e) {
                        BoxClient.this.log(e);
                        if (new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).exists()) {
                            new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).delete();
                        }
                        throw new IOException(e.getMessage());
                    }
                    if (new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).exists()) {
                        new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).delete();
                    }
                } else {
                    try {
                        this.createUploadSession();
                    }
                    catch (Exception e) {
                        BoxClient.this.log(e);
                        if (new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).exists()) {
                            new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).delete();
                        }
                        try {
                            BoxClient.this.delete(this.val$path);
                        }
                        catch (Exception ed) {
                            BoxClient.this.log(ed);
                        }
                        throw new IOException(e.getMessage());
                    }
                    RandomAccessFile in = new RandomAccessFile(new File_S(new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path)), "r");
                    byte[] b = new byte[(int)this.part_size];
                    int length = 0;
                    int loop = 1;
                    while (length >= 0 && loop < 10000) {
                        length = in.read(b);
                        if (length >= 0) {
                            try {
                                this.uploadChunk(b, length);
                            }
                            catch (Exception e) {
                                try {
                                    this.deleteUploadSession();
                                }
                                catch (Exception ed) {
                                    BoxClient.this.log(ed);
                                }
                                if (new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).exists()) {
                                    new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).delete();
                                }
                                try {
                                    BoxClient.this.delete(this.val$path);
                                }
                                catch (Exception ed) {
                                    BoxClient.this.log(ed);
                                }
                                throw new IOException(e.getMessage());
                            }
                        }
                        this.offset += (long)length;
                        ++loop;
                    }
                    try {
                        this.commitUploadSession();
                    }
                    catch (Exception e) {
                        BoxClient.this.log(e);
                        try {
                            this.deleteUploadSession();
                        }
                        catch (Exception ed) {
                            BoxClient.this.log(ed);
                        }
                        if (new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).exists()) {
                            new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).delete();
                        }
                        try {
                            BoxClient.this.delete(this.val$path);
                        }
                        catch (Exception ed) {
                            BoxClient.this.log(ed);
                        }
                        throw new IOException(e.getMessage());
                    }
                    if (new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).exists()) {
                        new File_S(String.valueOf(BoxClient.this.upload_root) + BoxClient.this.config.getProperty("username") + this.val$path).delete();
                    }
                }
                this.closed = true;
            }

            private void initMultipartUpload() throws Exception {
                this.murlc = URLConnection.openConnection(new VRL("https://upload.box.com/api/2.0/files/content"), BoxClient.this.config);
                this.murlc.setDoOutput(true);
                this.murlc.setRequestMethod("POST");
                if (!BoxClient.this.userId.equals("")) {
                    this.murlc.setRequestProperty("as-user", BoxClient.this.userId);
                }
                this.murlc.setRequestProperty("authorization", "Bearer " + BoxClient.this.getBearer());
                String numbers = this.boundary;
                this.murlc.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + this.boundary);
                this.boundary = "--" + this.boundary;
                this.murlc.setDoOutput(true);
                this.murlc.setChunkedStreamingMode(0L);
                this.mout = this.murlc.getOutputStream();
                String text = "";
                text = String.valueOf(text) + this.boundary;
                text = String.valueOf(text) + "\r\nContent-Disposition: form-data; name=\"attributes\"\r\n\r\n";
                this.mout.write(text.getBytes("UTF8"));
                JSONObject fileMetaInfo = new JSONObject();
                String parent_id = this.val$folder_id;
                JSONObject parent = new JSONObject();
                parent.put("id", parent_id);
                fileMetaInfo.put("name", Common.last(this.val$path));
                fileMetaInfo.put("parent", parent);
                text = fileMetaInfo.toString();
                this.mout.write(text.getBytes("UTF8"));
                text = "\r\n" + this.boundary;
                text = String.valueOf(text) + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"" + Common.last(this.val$path) + "\"\r\n";
                text = String.valueOf(text) + "content-type: application/octet-stream\r\n\r\n";
                this.mout.write(text.getBytes("UTF8"));
            }

            private void finishMultipartUpload() throws Exception {
                String text = "";
                text = "\r\n";
                text = String.valueOf(text) + this.boundary;
                this.mout.write(text.getBytes("UTF8"));
                this.mout.write("--".getBytes("UTF8"));
                this.mout.close();
                int code = this.murlc.getResponseCode();
                String result = URLConnection.consumeResponse(this.murlc.getInputStream());
                this.murlc.disconnect();
                if (code < 200 || code > 299) {
                    BoxClient.this.log(String.valueOf(result) + "\r\n");
                    throw new Exception("Error: " + result);
                }
                Vector new_items = new Vector();
                BoxClient.this.parseListResult(Common.all_but_last(this.val$path), new_items, result);
                this.file_id = ((Properties)new_items.get(0)).getProperty("resource_id", "");
            }

            private void createUploadSession() throws Exception {
                URLConnection urlc = URLConnection.openConnection(new VRL("https://upload.box.com/api/2.0/files/" + this.file_id + "/upload_sessions"), BoxClient.this.config);
                urlc.setDoOutput(true);
                urlc.setRequestMethod("POST");
                if (!BoxClient.this.userId.equals("")) {
                    urlc.setRequestProperty("as-user", BoxClient.this.userId);
                }
                urlc.setRequestProperty("authorization", "Bearer " + BoxClient.this.getBearer());
                urlc.setRequestProperty("Content-Type", "application/json");
                JSONObject session_info = new JSONObject();
                session_info.put("file_size", new Long(this.size));
                OutputStream commitout = urlc.getOutputStream();
                commitout.write(session_info.toString().getBytes("UTF8"));
                commitout.close();
                int code = urlc.getResponseCode();
                String session_result = URLConnection.consumeResponse(urlc.getInputStream());
                urlc.disconnect();
                if (code < 200 || code > 299) {
                    BoxClient.this.log(String.valueOf(session_result) + "\r\n");
                    throw new Exception("Error: " + session_result);
                }
                this.session_id = (String)((JSONObject)JSONValue.parse(session_result)).get("id");
                this.part_size = (Long)((JSONObject)JSONValue.parse(session_result)).get("part_size");
                if (this.session_id.equals("")) {
                    this.deleteUploadSession();
                    throw new Exception("Error: Missing session id!");
                }
            }

            private void deleteUploadSession() throws Exception {
                URLConnection urlc = URLConnection.openConnection(new VRL("https://upload.box.com/api/2.0/files/upload_sessions/" + this.session_id), BoxClient.this.config);
                urlc.setDoOutput(false);
                urlc.setRequestMethod("DELETE");
                if (!BoxClient.this.userId.equals("")) {
                    urlc.setRequestProperty("as-user", BoxClient.this.userId);
                }
                urlc.setRequestProperty("Content-Type", "application/json");
                urlc.setRequestProperty("authorization", "Bearer " + BoxClient.this.getBearer());
                int code = urlc.getResponseCode();
                String result = URLConnection.consumeResponse(urlc.getInputStream());
                urlc.disconnect();
                if (code < 200 || code > 299) {
                    BoxClient.this.log(String.valueOf(result) + "\r\n");
                    throw new Exception("Error: " + result);
                }
            }

            private void uploadChunk(byte[] b, int length) throws Exception {
                URLConnection urlc = URLConnection.openConnection(new VRL("https://upload.box.com/api/2.0/files/upload_sessions/" + this.session_id), BoxClient.this.config);
                urlc.setDoOutput(true);
                urlc.setRequestMethod("PUT");
                if (!BoxClient.this.userId.equals("")) {
                    urlc.setRequestProperty("as-user", BoxClient.this.userId);
                }
                urlc.setRequestProperty("authorization", "Bearer " + BoxClient.this.getBearer());
                urlc.setRequestProperty("Content-Type", "application/octet-stream");
                MessageDigest md = MessageDigest.getInstance("SHA1");
                md.update(b, 0, length);
                byte[] digestBytes = md.digest();
                String digestStr = Base64.encodeBytes(digestBytes);
                urlc.setRequestProperty("Digest", "sha=" + digestStr);
                urlc.setRequestProperty("Content-Range", "bytes " + this.offset + "-" + (this.offset + (long)length - 1L) + "/" + this.size);
                urlc.setChunkedStreamingMode(0L);
                urlc.getOutputStream().write(b, 0, length);
                urlc.getOutputStream().close();
                int code = urlc.getResponseCode();
                String result = Common.consumeResponse(urlc.getInputStream());
                if (code < 200 || code > 299) {
                    BoxClient.this.log(String.valueOf(result) + "\r\n");
                    throw new Exception("Error: " + result);
                }
                JSONObject part = (JSONObject)((JSONObject)JSONValue.parse(result)).get("part");
                this.parts.add(part);
            }

            private void commitUploadSession() throws Exception {
                URLConnection urlc = URLConnection.openConnection(new VRL("https://upload.box.com/api/2.0/files/upload_sessions/" + this.session_id + "/commit"), BoxClient.this.config);
                urlc.setDoOutput(true);
                urlc.setRequestMethod("POST");
                if (!BoxClient.this.userId.equals("")) {
                    urlc.setRequestProperty("as-user", BoxClient.this.userId);
                }
                urlc.setRequestProperty("authorization", "Bearer " + BoxClient.this.getBearer());
                byte[] digestBytes = this.digest.digest();
                String digestStr = Base64.encodeBytes(digestBytes);
                urlc.setRequestProperty("Digest", "sha=" + digestStr);
                urlc.setRequestProperty("Content-Type", "application/json");
                JSONObject commit_info = new JSONObject();
                commit_info.put("parts", this.parts);
                urlc.getOutputStream().write(commit_info.toString().getBytes("UTF8"));
                urlc.getOutputStream().close();
                int code = urlc.getResponseCode();
                String session_result = URLConnection.consumeResponse(urlc.getInputStream());
                urlc.disconnect();
                if (code < 200 || code > 299) {
                    BoxClient.this.log(String.valueOf(session_result) + "\r\n");
                    throw new Exception("Error: " + session_result);
                }
            }
        }
        this.out = new OutputWrapper(path, folder_id);
        return this.out;
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        int code;
        String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("username", "")) + path, "");
        if (resourceId.equals("")) {
            Properties p = this.stat(path);
            if (p != null) {
                resourceId = p.getProperty("resource_id", "");
            }
            if (resourceId.equals("")) {
                this.log("Download path not found:" + path + "\r\n");
                throw new IOException("Download: Path not found! Path:" + path);
            }
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://api.box.com/2.0/files/" + resourceId + "/content/"), this.config);
        urlc.setDoOutput(false);
        urlc.setRequestMethod("GET");
        if (!this.userId.equals("")) {
            urlc.setRequestProperty("as-user", this.userId);
        }
        urlc.setRequestProperty("authorization", "Bearer " + this.getBearer());
        if (startPos > 0L || endPos >= 0L) {
            urlc.setRequestProperty("Range", "bytes=" + startPos + "-" + (endPos >= 0L ? String.valueOf(endPos) : ""));
        }
        if ((code = urlc.getResponseCode()) < 200 || code > 303) {
            String result = URLConnection.consumeResponse(urlc.getInputStream());
            urlc.disconnect();
            this.log(String.valueOf(result) + "\r\n");
            throw new Exception(result);
        }
        URLConnection urlc2 = URLConnection.openConnection(new VRL(urlc.getHeaderField("LOCATION")), this.config);
        urlc2.setDoOutput(false);
        urlc2.setRequestMethod("GET");
        urlc2.setRequestProperty("Content-Type", null);
        urlc2.setRequestProperty("Accept", null);
        urlc2.setRequestProperty("Pragma", null);
        urlc2.setRequestProperty("Cache", null);
        urlc2.setRequestProperty("Cache-Control", null);
        urlc2.setUseCaches(false);
        urlc.disconnect();
        int code2 = urlc2.getResponseCode();
        if (code2 < 200 || code > 303) {
            String result = URLConnection.consumeResponse(urlc2.getInputStream());
            urlc2.disconnect();
            this.log(String.valueOf(result) + "\r\n");
            throw new Exception(result);
        }
        this.in = urlc2.getInputStream();
        return this.in;
    }

    @Override
    public boolean delete(String path) throws Exception {
        String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("username", "")) + path, "");
        if (resourceId.equals("")) {
            Properties p = this.stat(path);
            if (p != null) {
                resourceId = p.getProperty("resource_id", "");
            }
            if (resourceId.equals("")) {
                this.log("Delete path not found:" + path + "\r\n");
                throw new IOException("Delete: Path not found! Path:" + path);
            }
        }
        String url = "https://api.box.com/2.0/files/" + resourceId;
        String folderPropertyId = String.valueOf(this.config.getProperty("username", "")) + path + "/";
        if (path.endsWith("/")) {
            folderPropertyId.substring(0, folderPropertyId.length() - 1);
        }
        if (resourceIdCache.getProperty(folderPropertyId) != null) {
            url = "https://api.box.com/2.0/folders/" + resourceId + "?recursive=true";
        }
        URLConnection urlc = URLConnection.openConnection(new VRL(url), this.config);
        urlc.setDoOutput(false);
        urlc.setRequestMethod("DELETE");
        if (!this.userId.equals("")) {
            urlc.setRequestProperty("as-user", this.userId);
        }
        urlc.setRequestProperty("Content-Type", "application/json");
        urlc.setRequestProperty("authorization", "Bearer " + this.getBearer());
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        if (code < 200 || code > 299) {
            this.log(String.valueOf(result) + "\r\n");
            return false;
        }
        resourceIdCache.remove(String.valueOf(this.config.getProperty("username", "")) + path);
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        boolean is_folder = false;
        String resourceId = "";
        Properties p = this.stat(rnfr);
        if (p == null) {
            this.log("Rename from path not found:" + rnfr + "\r\n");
            throw new IOException("Rename: From path not found! Path:" + rnfr);
        }
        if (p.getProperty("type", "").equalsIgnoreCase("DIR")) {
            is_folder = true;
        }
        resourceId = p.getProperty("resource_id", "");
        String url = "https://api.box.com/2.0/files/" + resourceId;
        if (is_folder) {
            url = "https://api.box.com/2.0/folders/" + resourceId;
        }
        URLConnection urlc = URLConnection.openConnection(new VRL(url), this.config);
        urlc.setDoOutput(true);
        urlc.setRequestMethod("PUT");
        if (!this.userId.equals("")) {
            urlc.setRequestProperty("as-user", this.userId);
        }
        urlc.setRequestProperty("Content-Type", "application/json");
        urlc.setRequestProperty("authorization", "Bearer " + this.getBearer());
        JSONObject fileMetaInfo = new JSONObject();
        fileMetaInfo.put("name", Common.last(rnto));
        OutputStream out = urlc.getOutputStream();
        out.write(fileMetaInfo.toString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        if (code < 200 || code > 299) {
            this.log(String.valueOf(result) + "\r\n");
            return false;
        }
        String string = resourceId;
        synchronized (string) {
            try {
                resourceIdCache.put(String.valueOf(this.config.getProperty("username", "")) + rnto, resourceIdCache.remove(String.valueOf(this.config.getProperty("username", "")) + rnfr));
                if (is_folder) {
                    resourceIdCache.put(String.valueOf(this.config.getProperty("username", "")) + rnto + "/", resourceIdCache.remove(String.valueOf(this.config.getProperty("username", "")) + rnfr + "/"));
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return true;
    }

    @Override
    public boolean makedir(String path) throws Exception {
        URLConnection urlc = URLConnection.openConnection(new VRL("https://api.box.com/2.0/folders/"), this.config);
        urlc.setDoOutput(true);
        urlc.setRequestMethod("POST");
        if (!this.userId.equals("")) {
            urlc.setRequestProperty("as-user", this.userId);
        }
        urlc.setRequestProperty("Content-Type", "application/json");
        urlc.setRequestProperty("authorization", "Bearer " + this.getBearer());
        JSONObject fileMetaInfo = new JSONObject();
        String[] folders = path.split("/");
        fileMetaInfo.put("name", folders[folders.length - 1]);
        String temp_path = path;
        if (path.endsWith("/")) {
            temp_path = path.substring(0, path.length() - 1);
        }
        String parent_id = "0";
        if (Common.all_but_last(temp_path).length() > 1) {
            String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("username", "")) + Common.all_but_last(temp_path));
            if (resourceId == null) {
                Properties p = this.stat(Common.all_but_last(temp_path));
                parent_id = p.getProperty("resource_id", "");
            } else {
                parent_id = resourceId;
            }
        }
        JSONObject parentID = new JSONObject();
        parentID.put("id", parent_id);
        fileMetaInfo.put("parent", parentID);
        OutputStream out = urlc.getOutputStream();
        out.write(fileMetaInfo.toString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        if (code < 200 || code > 299) {
            this.log(String.valueOf(result) + "\r\n");
            return false;
        }
        JSONObject json_result = (JSONObject)((JSONObject)JSONValue.parse(result)).get("owned_by");
        resourceIdCache.put(String.valueOf(this.config.getProperty("username", "")) + path, json_result.get("id"));
        return true;
    }

    @Override
    public boolean makedirs(String path) throws Exception {
        boolean ok = true;
        String[] parts = path.split("/");
        String path2 = "";
        int x = 0;
        while (x < parts.length && ok) {
            String resourceId;
            path2 = String.valueOf(path2) + parts[x] + "/";
            if (x >= 1 && (resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("username", "")) + path2)) == null) {
                ok = this.makedir(path2);
            }
            ++x;
        }
        return ok;
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        return this.update_meta_data(path, modified);
    }

    public void doStandardDocsAlterations(URLConnection urlc, String contentType) throws Exception {
        urlc.setRequestProperty("Content-Type", contentType);
        urlc.setRequestProperty("Accept", null);
        urlc.setRequestProperty("Pragma", null);
        urlc.setRequestProperty("Cache", null);
        urlc.setRequestProperty("Cache-Control", null);
        urlc.setRequestProperty("Authorization", "Bearer " + this.getBearer());
        urlc.setUseCaches(false);
    }

    @Override
    public Properties stat(String path) throws Exception {
        if (path.endsWith(":filetree")) {
            path = path.substring(0, path.indexOf(":filetree") - 1);
        }
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }
        Vector v = new Vector();
        this.list(Common.all_but_last(path), v);
        int x = 0;
        while (x < v.size()) {
            Properties p = (Properties)v.elementAt(x);
            if (p.getProperty("name").equals(Common.last(path))) {
                return p;
            }
            ++x;
        }
        return null;
    }

    private void loadResourceIds(String path) {
        this.log("List : Searching for resource id. The full path : " + path);
        String[] parts = null;
        parts = path.equals("/") ? new String[]{""} : path.split("/");
        String path2 = "";
        int x = 0;
        while (x < parts.length) {
            path2 = String.valueOf(path2) + parts[x] + "/";
            String resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("username", "")) + path2);
            if (path2.equals("/")) {
                resourceId = "root";
            }
            try {
                this.list(path2, new Vector());
                resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("username", "")) + path2);
                this.log("List : Searching for resource id. The path : " + path2 + " Resource id :" + resourceId);
            }
            catch (Exception e) {
                this.log("Load Resource Ids : " + e);
            }
            ++x;
        }
    }

    private Properties renew_access_token() throws Exception {
        String assertion = this.generateAssertion();
        String full_form = "";
        full_form = String.valueOf(full_form) + "client_id=" + this.clientID;
        full_form = String.valueOf(full_form) + "&client_secret=" + this.clientSecret;
        full_form = String.valueOf(full_form) + "&assertion=" + assertion;
        full_form = String.valueOf(full_form) + "&grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer";
        byte[] b = full_form.getBytes("UTF8");
        URLConnection urlc = URLConnection.openConnection(new VRL("https://api.box.com/oauth2/token"), this.config);
        urlc.setDoOutput(true);
        urlc.setRequestMethod("POST");
        OutputStream out = urlc.getOutputStream();
        out.write(full_form.getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String response = Common.consumeResponse(urlc.getInputStream());
        if (code < 200 || code > 299) {
            throw new IOException(response);
        }
        String access_token = (String)((JSONObject)JSONValue.parse(response)).get("access_token");
        Properties p = new Properties();
        p.put("access_token", access_token);
        String expire_in = ((JSONObject)JSONValue.parse(response)).get("expires_in").toString();
        if (expire_in.endsWith(",")) {
            expire_in = expire_in.substring(0, expire_in.length() - 1);
        }
        p.put("expires_in", expire_in);
        p.put("time", String.valueOf(System.currentTimeMillis()));
        return p;
    }

    private String getBearer() throws Exception {
        Properties p;
        if (this.config.containsKey("token_start") && this.config.containsKey("token_expire") && System.currentTimeMillis() - Long.parseLong(this.config.getProperty("token_start")) > (Long.parseLong(this.config.getProperty("token_expire")) - 3000L) * 1000L && (p = this.renew_access_token()).containsKey("access_token")) {
            this.bearer = p.getProperty("access_token");
            this.config.put("token_start", String.valueOf(System.currentTimeMillis()));
        }
        return this.bearer;
    }

    @Override
    public String getUploadedByMetadata(String path) {
        String user_name = "";
        if (this.config.getProperty("box_meta_md5_and_upload_by", "false").equals("false")) {
            return user_name;
        }
        try {
            String resourceId = "0";
            if (!path.equals("/")) {
                resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("username", "")) + path, "");
            }
            if (resourceId.equals("") && !path.equals("/")) {
                this.loadResourceIds(Common.all_but_last(path));
                resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("username", "")) + path, "");
            }
            URLConnection urlc = URLConnection.openConnection(new VRL("https://api.box.com/2.0/files/" + resourceId + "/metadata"), this.config);
            urlc.setDoOutput(false);
            urlc.setRequestMethod("GET");
            if (!this.userId.equals("")) {
                urlc.setRequestProperty("as-user", this.userId);
            }
            urlc.setRequestProperty("authorization", "Bearer " + this.getBearer());
            String result = Common.consumeResponse(urlc.getInputStream());
            Object obj = ((JSONObject)JSONValue.parse(result)).get("entries");
            if (obj instanceof JSONArray) {
                JSONArray ja = (JSONArray)obj;
                int xxx = 0;
                while (xxx < ja.size()) {
                    JSONObject jo;
                    Object obj2 = ja.get(xxx);
                    if (obj2 instanceof JSONObject && (jo = (JSONObject)obj2).containsKey("$scope") && ((String)jo.get("$scope")).equals("global") && jo.containsKey("$template") && ((String)jo.get("$template")).equals("properties") && jo.containsKey("uploaded_by")) {
                        user_name = (String)jo.get("upload_by");
                    }
                    ++xxx;
                }
            }
        }
        catch (Exception e) {
            this.log(e);
        }
        return user_name;
    }

    @Override
    public void set_MD5_and_upload_id(String path) throws Exception {
        if (this.config.getProperty("box_meta_md5_and_upload_by", "false").equals("false") || this.config.getProperty("uploaded_by", "").equals("") || this.config.getProperty("uploaded_md5", "").equals("")) {
            return;
        }
        this.update_meta_data(path, 0L);
    }

    private boolean update_meta_data(String path, long modified) throws Exception {
        String resourceId = "0";
        if (!path.equals("/")) {
            resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("username", "")) + path, "");
        }
        if (resourceId.equals("") && !path.equals("/")) {
            this.loadResourceIds(Common.all_but_last(path));
            resourceId = resourceIdCache.getProperty(String.valueOf(this.config.getProperty("username", "")) + path, "");
        }
        URLConnection urlc = URLConnection.openConnection(new VRL("https://api.box.com/2.0/files/" + resourceId + "/metadata/global/properties"), this.config);
        urlc.setDoOutput(true);
        urlc.setRequestMethod("POST");
        if (!this.userId.equals("")) {
            urlc.setRequestProperty("as-user", this.userId);
        }
        urlc.setRequestProperty("Content-Type", "application/json");
        urlc.setRequestProperty("authorization", "Bearer " + this.getBearer());
        JSONObject meta_info = new JSONObject();
        meta_info.put("md5", this.config.getProperty("uploaded_md5", ""));
        meta_info.put("uploaded_by", this.config.getProperty("uploaded_by", ""));
        if (modified > 0L) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            meta_info.put("modified_date_time", sdf.format(new Date(modified)));
        }
        OutputStream out = urlc.getOutputStream();
        out.write(meta_info.toString().getBytes("UTF8"));
        out.close();
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        if (code < 200 || code > 299) {
            this.log(String.valueOf(result) + "\r\n");
            return false;
        }
        return true;
    }

    @Override
    public boolean hasThumbnails(Properties item) {
        return item.containsKey("thumbnails");
    }

    /*
     * Exception decompiling
     */
    @Override
    public boolean downloadThumbnail(Properties info) throws Exception {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [0[TRYBLOCK], 1[TRYBLOCK]], but top level block is 8[WHILELOOP]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    @Override
    public void logout() throws Exception {
        this.enterpriseID = "";
        this.bearer = "";
        this.clientID = "";
        this.clientSecret = "";
        this.publicKeyId = "";
        this.key = null;
        this.close();
    }
}

