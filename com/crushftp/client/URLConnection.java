/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.HttpURLConnection;
import com.crushftp.client.UnChunkInputStream;
import com.crushftp.client.VRL;
import com.crushftp.tunnel2.DProperties;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;
import java.util.zip.ZipInputStream;
import javax.net.ssl.SSLSocket;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

public class URLConnection {
    String method = "GET";
    int responseCode = -1;
    String message = null;
    Socket sock = null;
    VRL u = null;
    Properties config = null;
    boolean connected = false;
    Properties requestProps = new Properties();
    boolean doOutput = false;
    boolean outputDone = true;
    boolean gotHeaders = false;
    Properties headers = new Properties();
    Properties cookies = new Properties();
    OutputStream outputProxy = null;
    long maxRead = -1L;
    long content_length = -1L;
    boolean expect100 = false;
    boolean sendChunked = false;
    boolean send_compress = false;
    boolean receive_compress = false;
    boolean receiveChunked = false;
    boolean autoClose = false;
    boolean allowPool = true;
    boolean headersFinished = false;
    boolean remove_double_encoding = false;
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    OutputStream bufferedOut = null;
    public Date date = new Date();
    public SimpleDateFormat sdf_rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    public static Thread socketCleaner = null;
    String bind_ip = null;
    static final String skip_encode_chars = "/.#@&?!\\=+~";
    public static Vector cipher_suites = null;
    public static String last_cipher = null;
    public static String preferred_cipher = null;
    public Properties encode_path_special_chars = new Properties();
    int receive_buffer_size = -1;

    protected URLConnection(VRL u, Properties config) {
        this.sdf_rfc1123.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.u = u;
        this.config = config;
        this.requestProps.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        this.requestProps.put("Cache-Control", "no-cache");
        this.requestProps.put("Pragma", "no-cache");
        this.requestProps.put("User-Agent", "CrushClient" + config.getProperty("protocol", "DAV") + "/" + config.getProperty("version", "6.0") + " (Generic OS) Java");
        this.requestProps.put("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
        this.requestProps.put("Date", this.sdf_rfc1123.format(this.date));
        Enumeration<Object> keys = config.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (!key.startsWith("proxy_")) continue;
            String val = config.getProperty(key);
            this.requestProps.put("X-" + key.toUpperCase(), val);
        }
        if (socketCleaner == null) {
            this.startSocketCleaner();
        }
    }

    public static URLConnection openConnection(VRL u, Properties config) {
        return new HttpURLConnection(u, config);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void disconnect() throws IOException {
        if (this.connected) {
            if (this.autoClose || !this.allowPool || this.sendChunked || this.receiveChunked) {
                if (this.outputProxy != null) {
                    OutputStream outputStream = this.outputProxy;
                    synchronized (outputStream) {
                        Common.sockLog(this.sock, "Disconnect URLConnection (outputProxy)");
                        this.sock.close();
                    }
                } else {
                    Common.sockLog(this.sock, "Disconnect URLConnection");
                    this.sock.close();
                }
            } else {
                Common.releaseSocket(this.sock, this.u, this.config.getProperty("crushAuth", ""));
            }
            this.connected = false;
        }
    }

    public void setAllowPool(boolean allowPool) {
        this.allowPool = allowPool;
    }

    public void setUseChunkedStreaming(boolean chunked) {
        this.sendChunked = chunked;
    }

    public void setExpect100(boolean expect100) {
        this.expect100 = expect100;
    }

    public void setSendCompression(boolean send_compress) {
        this.send_compress = send_compress;
    }

    public void setReceiveCompression(boolean receive_compress) {
        this.receive_compress = receive_compress;
    }

    public void setChunkedStreamingMode(long size) {
        this.sendChunked = true;
    }

    public boolean isChunkedSend() {
        return this.sendChunked;
    }

    public boolean isExpect100() {
        return this.expect100;
    }

    public boolean isChunkedReceive() {
        return this.receiveChunked;
    }

    public void setBindIp(String bind_ip) {
        this.bind_ip = bind_ip;
    }

    public String getBindIp() {
        return this.bind_ip == null ? "0.0.0.0" : this.bind_ip;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void startSocketCleaner() {
        Properties properties = Common.socketPool;
        synchronized (properties) {
            if (socketCleaner == null) {
                socketCleaner = new Thread(new Runnable(){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    @Override
                    public void run() {
                        Thread.currentThread().setName("URLConnection socket cleanup thread.");
                        while (true) {
                            int sleep_time;
                            if ((sleep_time = (Common.socketTimeout = Integer.parseInt(System.getProperty("crushftp.socketpooltimeout", String.valueOf(Common.socketTimeout))))) == 0) {
                                sleep_time = 1000;
                            }
                            String threadName = "URLConnection socket cleanup thread:";
                            try {
                                Properties properties = Common.socketPool;
                                synchronized (properties) {
                                    Enumeration<Object> keys = Common.socketPool.keys();
                                    while (keys.hasMoreElements()) {
                                        String key = keys.nextElement().toString();
                                        Vector sockets = (Vector)Common.socketPool.get(key);
                                        if (sockets.size() == 0) {
                                            Common.socketPool.remove(key);
                                            continue;
                                        }
                                        threadName = threadName.length() > 5000 ? String.valueOf(threadName) + "." : String.valueOf(threadName) + key + "=" + sockets.size() + ",";
                                        int x = sockets.size() - 1;
                                        while (x >= 0) {
                                            Properties info = (Properties)sockets.elementAt(x);
                                            if (System.currentTimeMillis() - Long.parseLong(info.getProperty("time")) > (long)sleep_time) {
                                                Socket sock = (Socket)info.remove("sock");
                                                sockets.remove(x);
                                                Common.log("HTTP_CLIENT", 2, sock + ":Closing expired socket.");
                                                sock.close();
                                            }
                                            --x;
                                        }
                                    }
                                }
                            }
                            catch (Exception e) {
                                threadName = String.valueOf(threadName) + e;
                            }
                            Thread.currentThread().setName(threadName);
                            try {
                                Thread.sleep(sleep_time);
                            }
                            catch (Exception exception) {
                                continue;
                            }
                            break;
                        }
                    }
                });
                socketCleaner.start();
            }
        }
    }

    public void setReceiveBuffer(int i) {
        this.receive_buffer_size = i;
    }

    public void enableHighLatencyBuffer() {
        this.receive_buffer_size = 0x280000;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void connect() throws IOException {
        if (this.connected) {
            return;
        }
        int port = this.u.getPort();
        if (!this.requestProps.containsKey("Host")) {
            if (this.u.getPort() != 80 && this.u.getPort() != 443) {
                this.requestProps.put("Host", String.valueOf(this.u.getHost()) + ":" + this.u.getPort());
            } else {
                this.requestProps.put("Host", this.u.getHost());
            }
        }
        Vector socks = null;
        boolean lastTry = false;
        Properties properties = Common.socketPool;
        synchronized (properties) {
            socks = (Vector)Common.socketPool.get(String.valueOf(this.u.getProtocol()) + ":" + this.u.getHost() + ":" + this.u.getPort());
        }
        if (socks == null) {
            socks = new Vector();
        }
        while (true) {
            this.sock = Common.getSocket(this.config.getProperty("protocol", "DAV"), this.u, this.config.getProperty("use_dmz", "false"), this.config.getProperty("crushAuth", ""), Integer.parseInt(this.config.getProperty("timeout", "20000")));
            if (this.receive_buffer_size > 0) {
                this.sock.setReceiveBufferSize(this.receive_buffer_size);
                this.sock.setPerformancePreferences(0, 1, 2);
            }
            try {
                if ((this.u.getProtocol().equalsIgnoreCase("HTTPS") && !(this.sock instanceof SSLSocket) || this.u.getProtocol().equalsIgnoreCase("WEBDAVS") && !(this.sock instanceof SSLSocket)) && (!this.u.getHost().equals("127.0.0.1") || System.getProperty("crushftp.dmz.ssl", "true").equals("true"))) {
                    SSLSocket ss = Common.getSSLSocket(this.config.getProperty("trustore_path", this.config.getProperty("keystore_path", "")), this.config.getProperty("keystore_pass", ""), this.config.getProperty("truststore_pass", ""), this.config.getProperty("acceptAnyCert", "true").equalsIgnoreCase("true"), this.sock, this.u.getHost(), port);
                    if (preferred_cipher != null) {
                        ss.setEnabledCipherSuites(new String[]{preferred_cipher});
                    } else {
                        Common.setEnabledCiphers(this.config.getProperty("disabled_ciphers", ""), ss, null);
                    }
                    ss.setUseClientMode(true);
                    ss.setSoTimeout(Integer.parseInt(this.config.getProperty("timeout", "0")));
                    ss.startHandshake();
                    this.sock = ss;
                    if (cipher_suites == null) {
                        cipher_suites = new Vector();
                        String[] suites = ss.getSupportedCipherSuites();
                        int x = 0;
                        while (x < suites.length) {
                            cipher_suites.addElement(suites[x]);
                            ++x;
                        }
                    }
                    if ((last_cipher = ss.getSession().getCipherSuite()) != null && Thread.currentThread().getName().indexOf(last_cipher) < 0) {
                        Thread.currentThread().setName(String.valueOf(Thread.currentThread().getName()) + ":" + last_cipher);
                    }
                }
                this.connected = true;
                this.bufferedOut = new BufferedOutputStream(this.sock.getOutputStream());
                String tmp_path = Common.url_encode(this.u.getPath(), skip_encode_chars);
                if (this.remove_double_encoding) {
                    tmp_path = URLConnection.remove_double_encoding_of_special_chars(tmp_path);
                }
                this.bufferedOut.write((String.valueOf(this.method.toUpperCase().trim()) + " " + tmp_path + (this.receive_compress ? ".zip" : "") + " HTTP/1.1" + "\r\n").getBytes("UTF8"));
                this.log("HTTP_CLIENT", 2, String.valueOf(this.method.toUpperCase().trim()) + " " + tmp_path + " HTTP/1.1");
                this.bufferedOut.flush();
                Enumeration<Object> keys = this.requestProps.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement().toString();
                    String val = this.requestProps.getProperty(key);
                    this.bufferedOut.write((String.valueOf(key.trim()) + ": " + val + "\r\n").getBytes("UTF8"));
                    this.log("HTTP_CLIENT", 2, String.valueOf(key.trim()) + ": " + val);
                }
                this.bufferedOut.flush();
                if (this.content_length >= 0L || this.sendChunked) {
                    this.closeHeaders();
                }
                if (!this.doOutput) {
                    this.buildResponseHeaders();
                }
                this.sock.setSoTimeout(Integer.parseInt(this.config.getProperty("timeout", "0")));
            }
            catch (IOException e) {
                this.sock.close();
                this.connected = false;
                if (("" + e).indexOf("Expect-100") >= 0) {
                    throw e;
                }
                if (lastTry) {
                    throw e;
                }
                if (socks.size() != 0) continue;
                lastTry = true;
                continue;
            }
            break;
        }
        this.sock.setSoTimeout(Integer.parseInt(this.config.getProperty("timeout", "0")));
    }

    public void setUseCaches(boolean b) {
        if (b) {
            this.requestProps.remove("Cache-Control");
            this.requestProps.remove("Pragma");
        } else {
            this.requestProps.put("Cache-Control", "no-cache");
            this.requestProps.put("Pragma", "no-cache");
        }
    }

    public void setDoOutput(boolean doOutput) {
        this.doOutput = doOutput;
        this.outputDone = false;
    }

    public VRL getURL() {
        return this.u;
    }

    public void setLength(long content_length) {
        this.content_length = content_length;
    }

    private void closeHeaders() throws IOException {
        if (this.headersFinished) {
            return;
        }
        if (this.send_compress) {
            this.bufferedOut.write("Content-Encoding: gzip\r\n".getBytes("UTF8"));
            this.log("HTTP_CLIENT", 2, "Content-Encoding: gzip");
        }
        if (this.sendChunked) {
            this.bufferedOut.write("Transfer-Encoding: chunked\r\n".getBytes("UTF8"));
            this.log("HTTP_CLIENT", 2, "Transfer-Encoding: chunked");
        } else if (this.content_length >= 0L) {
            this.bufferedOut.write(("Content-Length: " + this.content_length + "\r\n").getBytes("UTF8"));
            this.log("HTTP_CLIENT", 2, "Content-Length: " + this.content_length);
        } else if (this.content_length < 0L) {
            this.bufferedOut.write("Connection: close\r\n".getBytes("UTF8"));
            this.autoClose = true;
            this.log("HTTP_CLIENT", 2, "Connection: close");
        }
        if (this.expect100) {
            this.bufferedOut.write("Expect: 100-continue\r\n".getBytes("UTF8"));
            this.log("HTTP_CLIENT", 2, "Expect: 100-continue");
        }
        this.bufferedOut.write("\r\n".getBytes("UTF8"));
        this.bufferedOut.flush();
        if (this.expect100) {
            this.sock.setSoTimeout(5000);
            try {
                this.readResponseHeaders();
            }
            catch (Exception e) {
                this.gotHeaders = false;
                e.printStackTrace();
            }
            if (this.responseCode != -1 && this.responseCode != 100) {
                throw new IOException("Expect-100 failed:" + this.responseCode + ":" + this.message);
            }
        }
        this.headersFinished = true;
    }

    private void buildResponseHeaders() throws IOException {
        this.connect();
        this.closeHeaders();
        if (this.gotHeaders || this.doOutput && !this.outputDone) {
            return;
        }
        this.readResponseHeaders();
    }

    protected void readResponseHeaders() throws IOException {
        this.responseCode = -1;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead = 0;
        byte[] b = new byte[1];
        String headerStr = "";
        int line = 0;
        long start = System.currentTimeMillis();
        try {
            while (bytesRead >= 0) {
                bytesRead = this.sock.getInputStream().read(b);
                if (bytesRead >= 0) {
                    baos.write(b);
                    headerStr = String.valueOf(headerStr) + new String(b, "UTF8");
                    if (!headerStr.equals("\r\n") && headerStr.endsWith("\r\n")) {
                        if (line == 0) {
                            headerStr = new String(baos.toByteArray(), "UTF8");
                            this.responseCode = Integer.parseInt(headerStr.split(" ")[1].trim());
                            this.message = headerStr.substring(headerStr.indexOf(" " + this.responseCode + " "));
                        } else if (headerStr.indexOf(":") > 0) {
                            String key = headerStr.substring(0, headerStr.indexOf(":")).trim();
                            String val = headerStr.substring(headerStr.indexOf(":") + 1).trim();
                            if (!key.equalsIgnoreCase("Set-Cookie") || this.headers.getProperty("SET-COOKIE", "").indexOf("CrushAuth") < 0) {
                                this.headers.put(key.toUpperCase(), val);
                            }
                            if (key.equalsIgnoreCase("Set-Cookie")) {
                                String name = val.substring(0, val.indexOf("="));
                                String cookie_val = val.substring(val.indexOf("=") + 1, val.indexOf(";", val.indexOf("=")));
                                this.cookies.put(name.toUpperCase(), cookie_val);
                            }
                        }
                        headerStr = "";
                        ++line;
                    }
                }
                if (!headerStr.equals("\r\n")) continue;
                break;
            }
        }
        finally {
            headerStr = new String(baos.toByteArray(), "UTF8");
            this.log("HTTP_CLIENT", 2, "Waited " + (System.currentTimeMillis() - start) + " ms for header response.");
            this.log("HTTP_CLIENT", 2, headerStr);
        }
        if (this.headers.containsKey("CONTENT-LENGTH")) {
            this.maxRead = Long.parseLong(this.headers.getProperty("CONTENT-LENGTH"));
        }
        if (this.headers.containsKey("TRANSFER-ENCODING")) {
            boolean bl = this.receiveChunked = this.headers.getProperty("TRANSFER-ENCODING").toUpperCase().indexOf("CHUNKED") >= 0;
        }
        if (this.expect100 && this.responseCode == 100) {
            this.responseCode = -1;
        } else {
            this.gotHeaders = true;
        }
    }

    public String getHeaderField(String key) {
        return this.headers.getProperty(key.toUpperCase());
    }

    public String getCookie(String key) {
        return this.cookies.getProperty(key.toUpperCase());
    }

    public void setRequestMethod(String method) {
        this.method = method.toUpperCase();
    }

    public String getRequestMethod() {
        return this.method;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContentType() {
        return this.requestProps.getProperty("Content-Type", "");
    }

    public void setRequestProperty(String key, String val) {
        if (val == null) {
            this.requestProps.remove(key);
        } else {
            this.requestProps.put(key, val);
        }
    }

    public int getResponseCode() throws IOException {
        block3: {
            try {
                if (this.outputProxy != null) {
                    this.outputProxy.close();
                }
            }
            catch (IOException e) {
                if (this.responseCode != -1) break block3;
                this.setDoOutput(false);
            }
        }
        this.buildResponseHeaders();
        return this.responseCode;
    }

    public Properties getRequestProps() {
        return this.requestProps;
    }

    public String getResponseMessage() throws IOException {
        block3: {
            try {
                if (this.outputProxy != null) {
                    this.outputProxy.close();
                }
            }
            catch (IOException e) {
                if (this.responseCode != -1) break block3;
                this.setDoOutput(false);
            }
        }
        this.buildResponseHeaders();
        return this.message;
    }

    public InputStream getInputStream() throws IOException, SocketTimeoutException {
        this.connect();
        this.outputDone = true;
        this.buildResponseHeaders();
        InputStream in = this.sock.getInputStream();
        if (this.receiveChunked) {
            in = new UnChunkInputStream(in);
        }
        if (this.receive_compress) {
            in = new ZipInputStream(in);
            ((ZipInputStream)in).getNextEntry();
            return in;
        }
        class InputStreamProxy
        extends BufferedInputStream {
            InputStream in;
            long totalBytesRead;

            public InputStreamProxy(InputStream in) {
                super(in);
                this.in = null;
                this.totalBytesRead = 0L;
                this.in = in;
            }

            @Override
            public int read() throws IOException {
                byte[] b1 = new byte[1];
                int bytesRead = this.read(b1, 0, 1);
                if (bytesRead < 0) {
                    return -1;
                }
                return b1[0] & 0xFF;
            }

            @Override
            public int read(byte[] b) throws IOException {
                return this.read(b, 0, b.length);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (URLConnection.this.maxRead >= 0L && this.totalBytesRead == URLConnection.this.maxRead) {
                    return -1;
                }
                if (URLConnection.this.maxRead >= 0L && this.totalBytesRead + (long)len > URLConnection.this.maxRead) {
                    len = (int)(URLConnection.this.maxRead - this.totalBytesRead);
                }
                if (len < 0) {
                    return -1;
                }
                int bytesRead = this.in.read(b, off, len);
                if (bytesRead >= 0) {
                    this.totalBytesRead += (long)bytesRead;
                }
                return bytesRead;
            }

            @Override
            public long skip(long n) throws IOException {
                return this.in.skip(n);
            }

            @Override
            public int available() throws IOException {
                return this.in.available();
            }

            @Override
            public synchronized void mark(int readlimit) {
                this.in.mark(readlimit);
            }

            @Override
            public synchronized void reset() throws IOException {
                this.in.reset();
            }

            @Override
            public boolean markSupported() {
                return this.in.markSupported();
            }

            @Override
            public void close() throws IOException {
            }
        }
        return new InputStreamProxy(in);
    }

    public OutputStream getOutputStream() throws IOException {
        this.connect();
        class OutputStreamProxy
        extends OutputStream {
            OutputStream out = null;
            boolean multipart;

            public OutputStreamProxy(OutputStream out) throws IOException {
                this.multipart = URLConnection.this.getContentType().indexOf("multipart") >= 0;
                this.out = out;
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
            public void flush() throws IOException {
                this.out.flush();
            }

            @Override
            public void write(byte[] b, int start, int len) throws IOException {
                if (URLConnection.this.sendChunked) {
                    this.out.write((String.valueOf(Long.toHexString(len)) + "\r\n").getBytes("UTF8"));
                }
                if (!URLConnection.this.sendChunked && URLConnection.this.content_length < 0L && URLConnection.this.buffer != null && !this.multipart) {
                    URLConnection.this.buffer.write(b, start, len);
                    if (URLConnection.this.buffer.size() > 0x100000 * Integer.parseInt(System.getProperty("crushftp.http_buffer", "10"))) {
                        URLConnection.this.closeHeaders();
                        this.out.write(URLConnection.this.buffer.toByteArray());
                        URLConnection.this.buffer = null;
                    }
                } else {
                    URLConnection.this.closeHeaders();
                    this.out.write(b, start, len);
                }
                if (URLConnection.this.sendChunked) {
                    this.out.write("\r\n".getBytes("UTF8"));
                }
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void close() throws IOException {
                OutputStreamProxy outputStreamProxy = this;
                synchronized (outputStreamProxy) {
                    if (URLConnection.this.sendChunked) {
                        this.out.write("0\r\n\r\n".getBytes("UTF8"));
                    }
                    if (URLConnection.this.buffer != null && URLConnection.this.buffer.size() > 0) {
                        URLConnection.this.content_length = URLConnection.this.buffer.size();
                        URLConnection.this.closeHeaders();
                        this.out.write(URLConnection.this.buffer.toByteArray());
                    }
                    if (URLConnection.this.buffer != null) {
                        URLConnection.this.buffer.reset();
                    }
                    this.out.flush();
                    URLConnection.this.outputDone = true;
                }
                if (!URLConnection.this.autoClose) {
                    URLConnection.this.buildResponseHeaders();
                }
            }
        }
        this.outputProxy = new OutputStreamProxy(this.bufferedOut);
        if (this.send_compress) {
            this.outputProxy = new GzipCompressorOutputStream(this.outputProxy);
        }
        return this.outputProxy;
    }

    public void setReadTimeout(int read_timeout) throws IOException {
        this.config.put("timeout", String.valueOf(read_timeout));
        if (this.sock != null) {
            this.sock.setSoTimeout(read_timeout);
        }
    }

    public void setDoInput(boolean ignored) {
    }

    public boolean getRemoveDoubleEncoding() {
        return this.remove_double_encoding;
    }

    public void setRemoveDoubleEncoding(boolean remove_double_encoding) {
        this.remove_double_encoding = remove_double_encoding;
    }

    public String getConfig(String key) {
        if (this.config != null) {
            return this.config.getProperty(key, "");
        }
        return "";
    }

    public Properties getConfig() {
        return this.config;
    }

    public void putConfig(String key, String value) {
        if (this.config != null) {
            this.config.put(key, value);
        }
    }

    public static String consumeResponse(InputStream in) throws IOException {
        return URLConnection.consumeResponse(in, true);
    }

    public static String consumeResponse(InputStream in, boolean close) throws IOException {
        byte[] b = DProperties.getArray();
        int bytesRead = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (bytesRead >= 0) {
            bytesRead = in.read(b);
            if (bytesRead <= 0) continue;
            baos.write(b, 0, bytesRead);
        }
        b = DProperties.releaseArray(b);
        if (close) {
            in.close();
        }
        String s = new String(baos.toByteArray(), "UTF8");
        Common.log("HTTP_CLIENT", 2, s);
        return s;
    }

    public static String remove_double_encoding_of_special_chars(String tempPath) {
        if (tempPath.contains("%2520")) {
            tempPath = tempPath.replace("%2520", "%20");
        }
        if (tempPath.contains("%252B")) {
            tempPath = tempPath.replace("%252B", "%2B");
        }
        if (tempPath.contains("%2526")) {
            tempPath = tempPath.replace("%2526", "%26");
        }
        if (tempPath.contains("%2524")) {
            tempPath = tempPath.replace("%2524", "%24");
        }
        if (tempPath.contains("%2540")) {
            tempPath = tempPath.replace("%2540", "%40");
        }
        if (tempPath.contains("%253D")) {
            tempPath = tempPath.replace("%253D", "%3D");
        }
        if (tempPath.contains("%253A")) {
            tempPath = tempPath.replace("%253A", "%3A");
        }
        if (tempPath.contains("%252C")) {
            tempPath = tempPath.replace("%252C", "%2C");
        }
        if (tempPath.contains("%253F")) {
            tempPath = tempPath.replace("%253F", "%3F");
        }
        if (tempPath.contains("%252F")) {
            tempPath = tempPath.replace("%252F", "%2F");
        }
        if (tempPath.contains("%255C")) {
            tempPath = tempPath.replace("%255C", "%5C");
        }
        if (tempPath.contains("%2523")) {
            tempPath = tempPath.replace("%2523", "%23");
        }
        if (tempPath.contains("%257E")) {
            tempPath = tempPath.replace("%257E", "%7E");
        }
        if (tempPath.contains("%2521")) {
            tempPath = tempPath.replace("%2521", "%21");
        }
        if (tempPath.contains("%2525")) {
            tempPath = tempPath.replace("%2525", "%25");
        }
        if (tempPath.contains("%2528")) {
            tempPath = tempPath.replace("%2528", "%28");
        }
        if (tempPath.contains("%2529")) {
            tempPath = tempPath.replace("%2529", "%29");
        }
        if (tempPath.contains("%2527")) {
            tempPath = tempPath.replace("%2527", "%27");
        }
        return tempPath;
    }

    private void log(String tag, int level, String log) {
        Common.log(tag, level, log);
        if (this.config.get("http_log") != null && this.config.get("http_log") instanceof Vector && !this.config.getProperty("http_log_header", "").equals("")) {
            Vector logQueue = (Vector)this.config.get("http_log");
            SimpleDateFormat logDateFormat = new SimpleDateFormat(System.getProperty("crushftp.log_date_format", "MM/dd/yyyy hh:mm:ss aa"), Locale.US);
            String time = String.valueOf(logDateFormat.format(new Date())) + "|";
            logQueue.addElement("~" + time + this.config.getProperty("http_log_header", "") + log);
        }
    }
}

