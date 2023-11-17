/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.jose4j.http.Response;
import org.jose4j.http.SimpleGet;
import org.jose4j.http.SimpleResponse;
import org.jose4j.lang.UncheckedJoseException;

public class Get
implements SimpleGet {
    private static final long MAX_RETRY_WAIT = 8000L;
    private int connectTimeout = 20000;
    private int readTimeout = 20000;
    private int retries = 3;
    private long initialRetryWaitTime = 180L;
    private boolean progressiveRetryWait = true;
    private SSLSocketFactory sslSocketFactory;
    private HostnameVerifier hostnameVerifier;
    private int responseBodySizeLimit = 524288;
    private Proxy proxy;

    @Override
    public SimpleResponse get(String location) throws IOException {
        int attempts = 0;
        URL url = new URL(location);
        while (true) {
            try {
                URLConnection urlConnection = this.proxy == null ? url.openConnection() : url.openConnection(this.proxy);
                urlConnection.setConnectTimeout(this.connectTimeout);
                urlConnection.setReadTimeout(this.readTimeout);
                this.setUpTls(urlConnection);
                HttpURLConnection httpUrlConnection = (HttpURLConnection)urlConnection;
                int code = httpUrlConnection.getResponseCode();
                String msg = httpUrlConnection.getResponseMessage();
                if (code != 200) {
                    throw new IOException("Non 200 status code (" + code + " " + msg + ") returned from " + url);
                }
                String charset = this.getCharset(urlConnection);
                String body = this.getBody(urlConnection, charset);
                Map<String, List<String>> headers = httpUrlConnection.getHeaderFields();
                Response simpleResponse = new Response(code, msg, headers, body);
                return simpleResponse;
            }
            catch (FileNotFoundException | SSLHandshakeException | SSLPeerUnverifiedException | ResponseBodyTooLargeException e) {
                throw e;
            }
            catch (IOException e) {
                if (++attempts > this.retries) {
                    throw e;
                }
                long retryWaitTime = this.getRetryWaitTime(attempts);
                try {
                    Thread.sleep(retryWaitTime);
                }
                catch (InterruptedException interruptedException) {
                }
                continue;
            }
            break;
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private String getBody(URLConnection urlConnection, String charset) throws IOException {
        StringWriter writer = new StringWriter();
        Throwable throwable = null;
        Object var5_6 = null;
        try {
            InputStream is = urlConnection.getInputStream();
            try {
                try (InputStreamReader isr = new InputStreamReader(is, charset);){
                    int n;
                    int charactersRead = 0;
                    char[] buffer = new char[1024];
                    while (-1 != (n = isr.read(buffer))) {
                        writer.write(buffer, 0, n);
                        if (this.responseBodySizeLimit <= 0 || (charactersRead += n) <= this.responseBodySizeLimit) continue;
                        throw new ResponseBodyTooLargeException("More than " + this.responseBodySizeLimit + " characters have been read from the response body.");
                    }
                }
                if (is == null) return writer.toString();
            }
            catch (Throwable throwable2) {
                if (throwable == null) {
                    throwable = throwable2;
                } else if (throwable != throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                if (is == null) throw throwable;
                is.close();
                throw throwable;
            }
            is.close();
            return writer.toString();
        }
        catch (Throwable throwable3) {
            if (throwable == null) {
                throwable = throwable3;
                throw throwable;
            } else {
                if (throwable == throwable3) throw throwable;
                throwable.addSuppressed(throwable3);
            }
            throw throwable;
        }
    }

    private void setUpTls(URLConnection urlConnection) {
        if (urlConnection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsUrlConnection = (HttpsURLConnection)urlConnection;
            if (this.sslSocketFactory != null) {
                httpsUrlConnection.setSSLSocketFactory(this.sslSocketFactory);
            }
            if (this.hostnameVerifier != null) {
                httpsUrlConnection.setHostnameVerifier(this.hostnameVerifier);
            }
        }
    }

    private String getCharset(URLConnection urlConnection) {
        String contentType = urlConnection.getHeaderField("Content-Type");
        String charset = "UTF-8";
        try {
            if (contentType != null) {
                String[] stringArray = contentType.replace(" ", "").split(";");
                int n = stringArray.length;
                int n2 = 0;
                while (n2 < n) {
                    String part = stringArray[n2];
                    String prefix = "charset=";
                    if (part.startsWith(prefix)) {
                        charset = part.substring(prefix.length());
                        break;
                    }
                    ++n2;
                }
                Charset.forName(charset);
            }
        }
        catch (Exception e) {
            charset = "UTF-8";
        }
        return charset;
    }

    private long getRetryWaitTime(int attempt) {
        if (this.progressiveRetryWait) {
            double pow = Math.pow(2.0, attempt - 1);
            long wait = (long)(pow * (double)this.initialRetryWaitTime);
            return Math.min(wait, 8000L);
        }
        return this.initialRetryWaitTime;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public void setTrustedCertificates(X509Certificate ... certificates) {
        this.setTrustedCertificates(Arrays.asList(certificates));
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void setProgressiveRetryWait(boolean progressiveRetryWait) {
        this.progressiveRetryWait = progressiveRetryWait;
    }

    public void setInitialRetryWaitTime(long initialRetryWaitTime) {
        this.initialRetryWaitTime = initialRetryWaitTime;
    }

    public void setResponseBodySizeLimit(int responseBodySizeLimit) {
        this.responseBodySizeLimit = responseBodySizeLimit;
    }

    public void setTrustedCertificates(Collection<X509Certificate> certificates) {
        try {
            TrustManagerFactory trustMgrFactory = TrustManagerFactory.getInstance("PKIX");
            KeyStore keyStore = KeyStore.getInstance("jks");
            keyStore.load(null, null);
            int i = 0;
            for (X509Certificate certificate : certificates) {
                keyStore.setCertificateEntry("alias" + i++, certificate);
            }
            trustMgrFactory.init(keyStore);
            TrustManager[] customTrustManagers = trustMgrFactory.getTrustManagers();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, customTrustManagers, null);
            this.sslSocketFactory = sslContext.getSocketFactory();
        }
        catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new UncheckedJoseException("Unable to initialize socket factory with custom trusted  certificates.", e);
        }
    }

    public void setHttpProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    private static class ResponseBodyTooLargeException
    extends IOException {
        public ResponseBodyTooLargeException(String message) {
            super(message);
        }
    }
}

