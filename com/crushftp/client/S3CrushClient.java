/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdom.Element
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.S3Client;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import javax.crypto.spec.SecretKeySpec;
import org.jdom.Element;

public class S3CrushClient
extends S3Client {
    String bucketName0 = null;
    String s3_root = "./s3/";
    boolean uploading = false;
    Vector replicating = null;

    public S3CrushClient(String url, String header, Vector log) {
        super(url, header, log);
        this.fields = new String[]{"username", "password", "s3_bucket_in_path", "ignore_login_errors", "random_id", "segmented", "s3_sha256", "server_side_encrypt", "server_side_encrypt_kms", "s3_buffer", "s3_threads_upload", "delete_403"};
        System.setProperty("crushtunnel.debug", System.getProperty("crushftp.debug", "2"));
        if (!url.endsWith("/")) {
            url = String.valueOf(url) + "/";
        }
        this.url = url;
        this.s3_root = System.getProperty("crushftp.s3_root", "./s3/");
        this.replicating = (Vector)System.getProperties().get("crushftp.s3_replicated");
    }

    public String getRawXmlPath(String path0) throws Exception {
        path0 = this.getPath(path0);
        return String.valueOf(this.s3_root) + this.bucketName0 + path0;
    }

    private String getPath(String path0) throws Exception {
        if (this.bucketName0 == null) {
            this.bucketName0 = path0.substring(1, path0.indexOf("/", 1)).toLowerCase();
            new File_S(String.valueOf(this.s3_root) + this.bucketName0).mkdirs();
        }
        if (this.secretKey == null) {
            this.login(new VRL(this.url).getUsername(), new VRL(this.url).getPassword(), "");
        }
        return Common.dots(path0.substring(path0.indexOf("/", 1)));
    }

    public static void writeFs(String s3_root, String bucketName0, Vector replicating, String path0, Properties p) throws Exception {
        String path = Common.dots(path0);
        new File_S(String.valueOf(s3_root) + bucketName0 + Common.all_but_last(path)).mkdirs();
        Common.writeXMLObject(String.valueOf(s3_root) + bucketName0 + path, p, "s3");
        if (replicating != null) {
            Properties p2 = new Properties();
            p2.put("bucketName0", bucketName0);
            p2.put("path", path0.replace(" ", "+"));
            p2.put("data", Common.CLONE(p));
            replicating.addElement(p2);
        }
    }

    public void resetBucket() {
        this.bucketName0 = null;
    }

    @Override
    public String login(String username, String password, String clientid) throws Exception {
        String md5hash;
        if (clientid == null) {
            clientid = "";
        }
        this.config.put("username", username.trim());
        this.config.put("password", password.trim());
        this.secretKey = new SecretKeySpec(password.trim().getBytes("UTF8"), "HmacSHA1");
        if (!clientid.equals("") && !clientid.endsWith("/")) {
            clientid = String.valueOf(clientid) + "/";
        }
        if (!clientid.equals("") && clientid.startsWith("/")) {
            clientid = clientid.substring(1);
        }
        if (!clientid.equals("")) {
            clientid = clientid.substring(0, clientid.indexOf("/") + 1);
        }
        if (!valid_credentials_cache.containsKey(md5hash = Common.getMD5(new ByteArrayInputStream((String.valueOf(username) + password + clientid).getBytes()))) || System.getProperty("crushftp.s3.always_auth", "false").equals("true")) {
            URLConnection urlc = this.doAction("GET", new VRL(String.valueOf(this.url) + clientid).getPath(), null, false, true, this.config.getProperty("s3_bucket_in_path", "false").equals("true"));
            String result = "";
            if (this.config.getProperty("ignore_login_errors", "false").equals("false")) {
                int code = urlc.getResponseCode();
                result = URLConnection.consumeResponse(urlc.getInputStream());
                urlc.disconnect();
                if (code < 200 || code > 299) {
                    this.log(String.valueOf(result) + "\r\n");
                    throw new IOException(result);
                }
            }
            valid_credentials_cache.put(md5hash, result);
        }
        this.config.remove("ignore_login_errors");
        return valid_credentials_cache.getProperty(md5hash);
    }

    @Override
    public void close() throws Exception {
        if (this.in != null) {
            this.in.close();
            this.in = null;
        }
        if (this.out != null) {
            int x = 0;
            while (x < 60 && this.uploading) {
                Thread.sleep(1000L);
                ++x;
            }
            this.out.close();
            this.out = null;
        }
    }

    @Override
    public Vector list(String path0, Vector list) throws Exception {
        path0 = this.getPath(path0);
        if (!new File_S(String.valueOf(this.s3_root) + this.bucketName0 + path0).exists()) {
            throw new Exception("No such folder: \"" + this.s3_root + this.bucketName0 + path0 + "\"");
        }
        File_S[] f = (File_S[])new File_S(String.valueOf(this.s3_root) + this.bucketName0 + path0).listFiles();
        int x = 0;
        while (f != null && x < f.length) {
            if (!f[x].getName().equals(".DS_Store")) {
                Date d = new Date(f[x].lastModified());
                Properties p = null;
                p = f[x].isFile() ? (Properties)Common.readXMLObject(f[x]) : new Properties();
                String line = String.valueOf(f[x].isDirectory() ? "d" : "-") + "rwxrwxrwx   1    owner   group   " + p.getProperty("size", "0") + "   " + this.yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + f[x].getName();
                Properties stat = S3CrushClient.parseStat(line);
                stat.put("url", String.valueOf(this.url) + this.bucketName0 + path0 + stat.getProperty("name") + (f[x].isDirectory() ? "/" : ""));
                list.addElement(stat);
            }
            ++x;
        }
        return list;
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        this.in = this.download4(path, startPos, endPos, binary, this.bucketName0);
        return this.in;
    }

    @Override
    protected OutputStream upload3(String path0, long startPos0, boolean truncate, boolean binary) throws Exception {
        String original_path = path0;
        String path_f = path0 = this.getPath(path0);
        StringBuffer uid = new StringBuffer(String.valueOf(path0.substring(1)) + (this.config.getProperty("random_id", "true").equals("true") ? "/" + Common.makeBoundary(10) : ""));
        Properties stat_existing = new Properties();
        Vector segments = new Vector();
        Vector tempResumeParts = null;
        StringBuffer partNumber = new StringBuffer("1");
        StringBuffer partNumberPos = new StringBuffer("1");
        String tempUploadId = null;
        boolean needCopyResume = false;
        ByteArrayOutputStream temp_buf = new ByteArrayOutputStream();
        Properties p_upload = new Properties();
        if (startPos0 > 0L) {
            stat_existing = this.stat(original_path);
            if (stat_existing.containsKey("segments")) {
                uid.setLength(0);
                uid.append(stat_existing.getProperty("uid"));
                segments.addAll((Vector)stat_existing.get("segments"));
                partNumber.setLength(0);
                partNumber.append(Integer.parseInt(segments.elementAt(segments.size() - 1).toString().split(":")[0]) + 1);
                p_upload.put("segments", segments);
            } else if (Long.parseLong(stat_existing.getProperty("size")) < 0x500000L) {
                Common.streamCopier(this.download3(original_path, -1L, -1L, true), temp_buf, false, true, false);
                this.deleteAws(stat_existing.getProperty("uid"));
                startPos0 = 0L;
            } else {
                if (this.config.getProperty("random_id", "true").equals("false")) {
                    String rnfr = stat_existing.getProperty("uid");
                    String rnto = String.valueOf(stat_existing.getProperty("uid")) + "_" + Common.makeBoundary(4) + ".original";
                    StringBuffer bucketNameSB = new StringBuffer();
                    URLConnection urlc = this.doAction("PUT", "/" + this.bucketName0 + "/" + rnto, bucketNameSB, true, true, this.config.getProperty("s3_bucket_in_path", "false").equals("true"));
                    urlc.setRequestProperty("x-amz-copy-source", "/" + this.bucketName0 + "/" + rnfr);
                    urlc.setRequestProperty("x-amz-metadata-directive", "COPY");
                    Properties mimes = Common.mimes;
                    String ext = "NULL";
                    if (rnto.toString().lastIndexOf(".") >= 0) {
                        ext = rnto.toString().substring(rnto.toString().lastIndexOf(".")).toUpperCase();
                    }
                    if (mimes.getProperty(ext, "").equals("")) {
                        ext = "NULL";
                    }
                    Common.updateMimes();
                    this.doStandardAmazonAlterations(urlc, Common.mimes.getProperty(ext), bucketNameSB.toString());
                    int code = urlc.getResponseCode();
                    String result = URLConnection.consumeResponse(urlc.getInputStream());
                    urlc.disconnect();
                    this.get_cache_item("list_cache").clear();
                    this.get_cache_item("cache_resume").clear();
                    if (code < 200 || code > 299) {
                        this.log(String.valueOf(result) + "\r\n");
                        throw new Exception(result);
                    }
                    this.deleteAws(rnfr);
                    stat_existing.put("uid", rnto);
                }
                needCopyResume = true;
            }
        } else {
            this.delete(original_path);
        }
        long startPos = startPos0;
        p_upload.put("size", String.valueOf(startPos));
        p_upload.put("uid", uid.toString().replace(" ", "+"));
        S3CrushClient.writeFs(this.s3_root, this.bucketName0, this.replicating, path0, p_upload);
        if (this.config.getProperty("segmented", "false").equals("false")) {
            String request_parameter = "?uploads";
            boolean s3_sha256 = System.getProperty("crushftp.s3_sha256", "false").equals("true");
            if (this.config.containsKey("s3_sha256")) {
                s3_sha256 = this.config.getProperty("s3_sha256", "false").equals("true");
            }
            if (s3_sha256 || !this.config.getProperty("server_side_encrypt_kms", "").equals("")) {
                request_parameter = String.valueOf(request_parameter) + "=";
            }
            URLConnection urlc = URLConnection.openConnection(new VRL("https://" + (this.bucketName0.equals("") || this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? "" : String.valueOf(this.bucketName0) + ".") + this.region_host + "/" + (this.config.getProperty("s3_bucket_in_path", "false").equals("true") ? String.valueOf(this.bucketName0) + "/" : "") + this.handle_path_special_chars(uid.toString(), false) + request_parameter), this.config);
            urlc.setRemoveDoubleEncoding(true);
            urlc.setRequestMethod("POST");
            if (!this.config.getProperty("server_side_encrypt_kms", "").equals("")) {
                urlc.setRequestProperty("x-amz-server-side-encryption", "aws:kms");
                urlc.setRequestProperty("x-amz-server-side-encryption-aws-kms-key-id", this.config.getProperty("server_side_encrypt_kms", ""));
            } else if (this.config.getProperty("server_side_encrypt", "false").equals("true")) {
                urlc.setRequestProperty("x-amz-server-side-encryption", "AES256");
            }
            urlc.setDoOutput(false);
            Properties mimes = Common.mimes;
            String ext = "NULL";
            if (path0.toString().lastIndexOf(".") >= 0) {
                ext = path0.toString().substring(path0.toString().lastIndexOf(".")).toUpperCase();
            }
            if (mimes.getProperty(ext, "").equals("")) {
                ext = "NULL";
            }
            Common.updateMimes();
            this.doStandardAmazonAlterations(urlc, Common.mimes.getProperty(ext), this.bucketName0);
            int code = urlc.getResponseCode();
            String result = URLConnection.consumeResponse(urlc.getInputStream());
            urlc.disconnect();
            if (code < 200 || code > 299) {
                this.log(String.valueOf(result) + "\r\n");
                throw new IOException(result);
            }
            Element root = Common.getSaxBuilder().build((InputStream)new ByteArrayInputStream(result.getBytes("UTF8"))).getRootElement();
            tempUploadId = S3CrushClient.getKeyText(root, "UploadId");
        } else {
            p_upload.put("segments", segments);
        }
        tempResumeParts = new Vector();
        Vector resumePartsDone = new Vector();
        Vector resumeParts = tempResumeParts;
        String uploadId = tempUploadId;
        long maxBuf = Long.parseLong(this.config.getProperty("s3_buffer", "5"));
        if (maxBuf < 5L) {
            maxBuf = 5L;
        }
        long maxBufferSize = maxBuf * 1024L * 1024L;
        if (needCopyResume) {
            this.doCopyResume(tempResumeParts, resumePartsDone, partNumber, partNumberPos, tempUploadId, "/" + this.bucketName0 + "/" + stat_existing.getProperty("uid"), "/" + this.bucketName0 + "/" + uid, startPos);
            this.deleteAws(stat_existing.getProperty("uid"));
        }
        this.uploading = true;
        class OutputWrapper
        extends OutputStream {
            boolean closed = false;
            long start = System.currentTimeMillis();
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            long size;
            long last_pos;
            boolean wrote;
            private final /* synthetic */ Properties val$p_upload;
            private final /* synthetic */ long val$maxBufferSize;
            private final /* synthetic */ StringBuffer val$partNumber;
            private final /* synthetic */ StringBuffer val$partNumberPos;
            private final /* synthetic */ Vector val$resumeParts;
            private final /* synthetic */ Vector val$resumePartsDone;
            private final /* synthetic */ Vector val$segments;
            private final /* synthetic */ StringBuffer val$uid;
            private final /* synthetic */ String val$uploadId;
            private final /* synthetic */ String val$path_f;

            OutputWrapper(long l, Properties properties, long l2, StringBuffer stringBuffer, StringBuffer stringBuffer2, Vector vector, Vector vector2, Vector vector3, StringBuffer stringBuffer3, String string, String string2) {
                this.val$p_upload = properties;
                this.val$maxBufferSize = l2;
                this.val$partNumber = stringBuffer;
                this.val$partNumberPos = stringBuffer2;
                this.val$resumeParts = vector;
                this.val$resumePartsDone = vector2;
                this.val$segments = vector3;
                this.val$uid = stringBuffer3;
                this.val$uploadId = string;
                this.val$path_f = string2;
                this.size = l;
                this.last_pos = l;
                this.wrote = false;
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
                if (this.closed) {
                    throw new IOException("Stream already closed.");
                }
                if (len > 0) {
                    this.wrote = true;
                }
                this.buf.write(b, off, len);
                this.size += (long)len;
                this.val$p_upload.put("size", String.valueOf(this.size));
                if ((long)this.buf.size() > this.val$maxBufferSize) {
                    int i = Integer.parseInt(this.val$partNumber.toString());
                    this.val$partNumber.setLength(0);
                    this.val$partNumber.append(i + 1);
                    int ii = Integer.parseInt(this.val$partNumberPos.toString());
                    this.val$partNumberPos.setLength(0);
                    this.val$partNumberPos.append(ii + 1);
                    final int partNumber2 = i;
                    final int partNumberPos2 = ii;
                    final ByteArrayOutputStream buf2 = this.buf;
                    this.val$resumeParts.addElement(new Properties());
                    while (this.val$resumeParts.size() - this.val$resumePartsDone.size() >= Integer.parseInt(S3CrushClient.this.config.getProperty("s3_threads_upload", "3")) + 1) {
                        try {
                            Thread.sleep(100L);
                        }
                        catch (InterruptedException interruptedException) {
                            // empty catch block
                        }
                    }
                    if (S3CrushClient.this.config.getProperty("segmented", "false").equals("true")) {
                        this.val$p_upload.put("size", String.valueOf(this.size));
                        this.val$segments.addElement(String.valueOf(i) + ":" + this.last_pos + "-" + this.size);
                        this.last_pos = this.size;
                    }
                    Worker.startWorker(new Runnable(){

                        /*
                         * WARNING - Removed try catching itself - possible behaviour change.
                         */
                        @Override
                        public void run() {
                            block5: {
                                Thread.currentThread().setName("Buf Flusher:" + partNumber2);
                                try {
                                    S3CrushClient.this.flushNow(val$resumePartsDone, buf2, val$resumeParts, val$uploadId, "/" + ((OutputWrapper)this).S3CrushClient.this.bucketName0 + "/" + val$uid, partNumber2, partNumberPos2, false);
                                    if (!((OutputWrapper)this).S3CrushClient.this.config.getProperty("segmented", "false").equals("true")) break block5;
                                    Vector vector = val$segments;
                                    synchronized (vector) {
                                        S3CrushClient.writeFs(((OutputWrapper)this).S3CrushClient.this.s3_root, ((OutputWrapper)this).S3CrushClient.this.bucketName0, ((OutputWrapper)this).S3CrushClient.this.replicating, val$path_f, val$p_upload);
                                    }
                                }
                                catch (Exception e) {
                                    S3CrushClient.this.log("S3_CLIENT", 0, e);
                                }
                            }
                        }
                    });
                    this.buf = new ByteArrayOutputStream();
                    this.start = System.currentTimeMillis();
                }
            }

            @Override
            public void close() throws IOException {
                if (this.closed) {
                    return;
                }
                int loops = 0;
                final int i = Integer.parseInt(this.val$partNumber.toString());
                final int ii = Integer.parseInt(this.val$partNumberPos.toString());
                if (this.buf.size() > 0 || !this.wrote) {
                    this.val$resumeParts.addElement(new Properties());
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            Thread.currentThread().setName("Buf Flusher:" + i);
                            try {
                                S3CrushClient.this.flushNow(val$resumePartsDone, buf, val$resumeParts, val$uploadId, "/" + ((OutputWrapper)this).S3CrushClient.this.bucketName0 + "/" + val$uid, i, ii, !wrote);
                                val$segments.addElement(String.valueOf(i) + ":" + last_pos + "-" + size);
                            }
                            catch (Exception e) {
                                S3CrushClient.this.log("S3_CLIENT", 0, e);
                            }
                        }
                    });
                }
                S3CrushClient.this.log("S3_CLIENT", 0, "S3 thread count at close:" + (this.val$resumeParts.size() - this.val$resumePartsDone.size()));
                while (this.val$resumePartsDone.size() < this.val$resumeParts.size() && loops++ < 6000) {
                    int x22 = 0;
                    while (x22 < this.val$resumeParts.size()) {
                        Properties chunk_part = (Properties)this.val$resumeParts.elementAt(x22);
                        if (!chunk_part.containsKey("etag") && System.currentTimeMillis() - Long.parseLong(chunk_part.getProperty("start", String.valueOf(System.currentTimeMillis()))) > Long.parseLong(S3CrushClient.this.config.getProperty("s3_buffer", "5")) * 1000L * 2L) {
                            chunk_part.put("start", String.valueOf(System.currentTimeMillis()));
                            chunk_part.put("time", String.valueOf(System.currentTimeMillis() - Long.parseLong(chunk_part.getProperty("start", String.valueOf(System.currentTimeMillis())))));
                            S3CrushClient.this.log("S3_CLIENT", 0, "Segment timed out, resending:" + chunk_part);
                            URLConnection urlc = (URLConnection)chunk_part.remove("urlc");
                            if (urlc != null) {
                                urlc.disconnect();
                            }
                        }
                        ++x22;
                    }
                    try {
                        Thread.sleep(100L);
                    }
                    catch (InterruptedException x22) {
                        // empty catch block
                    }
                }
                this.closed = true;
                S3CrushClient.this.finishUpload(this.val$resumeParts, "/" + S3CrushClient.this.bucketName0 + "/" + this.val$uid, this.val$uploadId);
                this.val$p_upload.put("size", String.valueOf(this.size));
                try {
                    S3CrushClient.writeFs(S3CrushClient.this.s3_root, S3CrushClient.this.bucketName0, S3CrushClient.this.replicating, this.val$path_f, this.val$p_upload);
                }
                catch (Exception e) {
                    S3CrushClient.this.log("S3_CLIENT", 0, e);
                }
                S3CrushClient.this.uploading = false;
            }
        }
        this.out = new OutputWrapper(startPos, p_upload, maxBufferSize, partNumber, partNumberPos, resumeParts, resumePartsDone, segments, uid, uploadId, path_f);
        if (temp_buf.size() > 0) {
            this.out.write(temp_buf.toByteArray());
        }
        return this.out;
    }

    public boolean setSize(String path0, long size) throws Exception {
        Properties stat0 = this.stat(path0);
        if (stat0 == null) {
            return false;
        }
        if (stat0.getProperty("type", "").equalsIgnoreCase("DIR")) {
            return false;
        }
        path0 = this.getPath(path0);
        Properties p_upload = new Properties();
        p_upload.put("uid", stat0.getProperty("uid"));
        p_upload.put("size", String.valueOf(size));
        try {
            S3CrushClient.writeFs(this.s3_root, this.bucketName0, this.replicating, path0, p_upload);
            return true;
        }
        catch (Exception e) {
            this.log("S3_CLIENT", 0, e);
            return false;
        }
    }

    public boolean delete2(String path0) throws Exception {
        Properties stat0 = this.stat(path0);
        if (stat0 == null) {
            return false;
        }
        path0 = this.getPath(path0);
        Vector<File_S> items = new Vector<File_S>();
        if (new File_S(String.valueOf(this.s3_root) + this.bucketName0 + path0).isDirectory()) {
            Vector items2 = new Vector();
            Common.appendListing_S(String.valueOf(this.s3_root) + this.bucketName0 + path0, items2, "", 3, false);
            if (items2.size() > 1) {
                return false;
            }
            items = items2;
        } else {
            items.addElement(new File_S(String.valueOf(this.s3_root) + this.bucketName0 + path0));
        }
        boolean deleted = true;
        int x = items.size() - 1;
        while (x >= 0) {
            File_S f = (File_S)items.elementAt(x);
            if (!f.getName().equals(".DS_Store") && !f.isDirectory() && !f.delete() && f.exists()) {
                deleted = false;
            }
            --x;
        }
        if (deleted) {
            Common.recurseDelete(String.valueOf(this.s3_root) + this.bucketName0 + path0, false);
        }
        return deleted;
    }

    @Override
    public boolean delete(String path0) throws Exception {
        Properties stat0 = this.stat(path0);
        if (stat0 == null) {
            return false;
        }
        path0 = this.getPath(path0);
        Vector<File_S> items = new Vector<File_S>();
        if (new File_S(String.valueOf(this.s3_root) + this.bucketName0 + path0).isDirectory()) {
            Vector items2 = new Vector();
            Common.appendListing_S(String.valueOf(this.s3_root) + this.bucketName0 + path0, items2, "", 3, false);
            if (items2.size() > 1) {
                return false;
            }
            items = items2;
        } else {
            items.addElement(new File_S(String.valueOf(this.s3_root) + this.bucketName0 + path0));
        }
        int x = items.size() - 1;
        while (x >= 0) {
            File_S f = (File_S)items.elementAt(x);
            if (!f.getName().equals(".DS_Store") && !f.isDirectory()) {
                Properties p = (Properties)Common.readXMLObject(f);
                if (p.containsKey("segments")) {
                    final Vector<String> threads = new Vector<String>();
                    final Vector errors = new Vector();
                    Vector segments = (Vector)p.get("segments");
                    int xx = 0;
                    while (xx < segments.size()) {
                        final String delete_path = String.valueOf(p.getProperty("uid")) + "_" + segments.elementAt(xx).toString().split(":")[0];
                        threads.addElement(delete_path);
                        while (threads.size() > 20) {
                            Thread.sleep(100L);
                        }
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                boolean ok2 = true;
                                try {
                                    int loop = 0;
                                    while (loop < 10) {
                                        ok2 = true;
                                        if (!S3CrushClient.this.deleteAws(delete_path)) {
                                            ok2 = false;
                                            ++loop;
                                            continue;
                                        }
                                        break;
                                    }
                                }
                                catch (Exception e) {
                                    S3CrushClient.this.log("S3_CLIENT", 1, e);
                                    ok2 = false;
                                }
                                if (!ok2) {
                                    errors.addElement(delete_path);
                                }
                                threads.remove(delete_path);
                            }
                        });
                        ++xx;
                    }
                    int loops = 0;
                    while (loops < 6000 && threads.size() > 0) {
                        Thread.sleep(100L);
                        ++loops;
                    }
                    if (errors.size() > 0) {
                        this.log("Failed to delete:" + errors);
                        return false;
                    }
                    f.delete();
                } else {
                    boolean ok2 = true;
                    try {
                        int loop = 0;
                        while (loop < 10) {
                            ok2 = true;
                            if (this.deleteAws(p.getProperty("uid"))) {
                                f.delete();
                                break;
                            }
                            ok2 = false;
                            ++loop;
                        }
                    }
                    catch (Exception e) {
                        this.log("S3_CLIENT", 1, e);
                        ok2 = false;
                    }
                    if (!ok2) {
                        throw new Exception("Can't delete:" + p.getProperty("uid"));
                    }
                }
            }
            --x;
        }
        Common.recurseDelete(String.valueOf(this.s3_root) + this.bucketName0 + path0, false);
        return true;
    }

    private boolean deleteAws(String uid) throws Exception {
        URLConnection urlc = this.doAction("DELETE", "/" + this.bucketName0 + "/" + uid.replace("+", " "), null, false, true, this.config.getProperty("s3_bucket_in_path", "false").equals("true"));
        int code = urlc.getResponseCode();
        String result = URLConnection.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        if (code == 403 && this.config.getProperty("delete_403", "false").equalsIgnoreCase("true")) {
            return true;
        }
        if ((code < 200 || code > 299) && code != 404) {
            this.log(String.valueOf(code) + ":" + result + "\r\n");
            return false;
        }
        return true;
    }

    @Override
    public boolean makedir(String path0) throws Exception {
        if (!(path0 = this.getPath(path0)).endsWith("/")) {
            path0 = String.valueOf(path0) + "/";
        }
        if (path0.equals("/")) {
            return true;
        }
        return new File_S(String.valueOf(this.s3_root) + this.bucketName0 + path0).mkdirs();
    }

    @Override
    public boolean makedirs(String path0) throws Exception {
        return this.makedir(path0);
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        rnfr = this.getPath(rnfr);
        rnto = this.getPath(rnto);
        return new File_S(String.valueOf(this.s3_root) + this.bucketName0 + rnfr).renameTo(new File_S(String.valueOf(this.s3_root) + this.bucketName0 + rnto));
    }

    @Override
    public Properties stat(String path0) throws Exception {
        if (path0.endsWith(":filetree")) {
            path0 = path0.substring(0, path0.indexOf(":filetree") - 1);
        }
        path0 = this.getPath(path0);
        File_S f = new File_S(String.valueOf(this.s3_root) + this.bucketName0 + Common.dots(path0));
        if (!f.exists()) {
            return null;
        }
        if (f.isDirectory()) {
            Date d = new Date(f.lastModified());
            String line = "drwxrwxrwx   1    owner   group   0   " + this.yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + f.getName();
            Properties stat = S3CrushClient.parseStat(line);
            stat.put("url", String.valueOf(this.url) + this.bucketName0 + path0 + (stat.getProperty("type").equalsIgnoreCase("DIR") && !path0.endsWith("/") ? "/" : ""));
            stat.put("modified", String.valueOf(d.getTime()));
            return stat;
        }
        Properties p = (Properties)Common.readXMLObject(f);
        Date d = new Date(f.lastModified());
        String line = "-rwxrwxrwx   1    owner   group   " + p.getProperty("size", "0") + "   " + this.yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + f.getName();
        Properties stat = S3CrushClient.parseStat(line);
        stat.put("url", String.valueOf(this.url) + this.bucketName0 + path0);
        if (p.containsKey("segments")) {
            stat.put("segments", p.get("segments"));
        }
        stat.put("uid", p.getProperty("uid").replace("+", " "));
        stat.put("modified", String.valueOf(d.getTime()));
        return stat;
    }

    @Override
    public boolean mdtm(String path0, long modified) throws Exception {
        path0 = this.getPath(path0);
        return new File_S(String.valueOf(this.s3_root) + this.bucketName0 + path0).setLastModified(modified);
    }
}

