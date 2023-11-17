/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.net;

import com.didisoft.pgp.KeyPairInformation;
import com.didisoft.pgp.KeyStore;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public final class HKPClient {
    protected String serverName;
    protected int port;
    protected boolean useHttps = false;
    private String userAgent = "Mozilla/5.0";
    static final String LINEFEED = "\r\n";
    private boolean partialMatch = true;

    public HKPClient(String string) {
        this(string, 80);
    }

    public HKPClient(String string, int n) {
        this.serverName = string;
        this.port = n;
    }

    public HKPClient(String string, int n, boolean bl) {
        this.serverName = string;
        this.port = n;
        this.useHttps = bl;
    }

    private byte[] findKeyById(String string) throws IOException {
        byte[] byArray = new byte[]{};
        boolean bl = false;
        DataInputStream dataInputStream = null;
        try {
            dataInputStream = this.partialMatch ? new DataInputStream(new URL(this.useHttps ? "https" : "http", this.serverName, this.port, "/pks/lookup?op=get&search=" + URLEncoder.encode(string)).openStream()) : new DataInputStream(new URL(this.useHttps ? "https" : "http", this.serverName, this.port, "/pks/lookup?op=get&exact=on&search=" + URLEncoder.encode(string)).openStream());
        }
        catch (FileNotFoundException fileNotFoundException) {
            return byArray;
        }
        String string2 = null;
        StringBuilder stringBuilder = new StringBuilder();
        do {
            if ((string2 = dataInputStream.readLine()) != null) continue;
            return null;
        } while (!string2.equals("-----BEGIN PGP PUBLIC KEY BLOCK-----"));
        stringBuilder.append(string2).append(LINEFEED);
        while ((string2 = dataInputStream.readLine()) != null) {
            if (string2.equals("-----END PGP PUBLIC KEY BLOCK-----")) {
                stringBuilder.append(string2);
                bl = true;
                break;
            }
            stringBuilder.append(string2).append(LINEFEED);
        }
        if (bl) {
            byArray = stringBuilder.toString().getBytes("ASCII");
        }
        return byArray;
    }

    private byte[] findKeysById(String string) throws IOException {
        byte[] byArray = new byte[]{};
        boolean bl = false;
        DataInputStream dataInputStream = null;
        try {
            dataInputStream = this.partialMatch ? new DataInputStream(new URL(this.useHttps ? "https" : "http", this.serverName, this.port, "/pks/lookup?op=get&search=" + URLEncoder.encode(string)).openStream()) : new DataInputStream(new URL(this.useHttps ? "https" : "http", this.serverName, this.port, "/pks/lookup?op=get&exact=on&search=" + URLEncoder.encode(string)).openStream());
        }
        catch (FileNotFoundException fileNotFoundException) {
            return byArray;
        }
        String string2 = null;
        StringBuilder stringBuilder = new StringBuilder();
        do {
            if ((string2 = dataInputStream.readLine()) != null) continue;
            return null;
        } while (!string2.equals("-----BEGIN PGP PUBLIC KEY BLOCK-----"));
        stringBuilder.append(string2).append(LINEFEED);
        while ((string2 = dataInputStream.readLine()) != null) {
            stringBuilder.append(string2).append(LINEFEED);
        }
        if (stringBuilder.indexOf("-----END PGP PUBLIC KEY BLOCK-----") != -1) {
            int n = stringBuilder.lastIndexOf("-----END PGP PUBLIC KEY BLOCK-----");
            byte[] byArray2 = stringBuilder.toString().getBytes("ASCII");
            byArray = new byte[n];
            System.arraycopy(byArray2, 0, byArray, 0, n);
        } else {
            byArray = stringBuilder.toString().getBytes("ASCII");
        }
        return byArray;
    }

    public byte[] getKeyByKeyIdHex(String string) throws IOException {
        return this.findKeyById("0x" + string);
    }

    public byte[] getKeyByUserId(String string) throws IOException {
        return this.findKeyById(string);
    }

    public byte[] getKeysByUserId(String string) throws IOException {
        return this.findKeyById(string);
    }

    public byte[] getKeyByKeyId(long l) throws IOException {
        String string = Long.toHexString(l).toUpperCase();
        return this.getKeyByKeyIdHex(string.substring(string.length() - 8));
    }

    public boolean submitKey(byte[] byArray) throws Exception {
        String string;
        URL uRL = new URL(this.useHttps ? "https" : "http", this.serverName, this.port, "/pks/add");
        HttpURLConnection httpURLConnection = (HttpURLConnection)uRL.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("User-Agent", this.getUserAgent());
        httpURLConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        KeyStore keyStore = new KeyStore();
        KeyPairInformation[] keyPairInformationArray = keyStore.importKeyRing(new ByteArrayInputStream(byArray));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        keyStore.exportPublicKey((OutputStream)byteArrayOutputStream, keyPairInformationArray[0].getKeyID(), true);
        String string2 = "keytext=" + URLEncoder.encode(new String(byteArrayOutputStream.toByteArray(), "ASCII"));
        httpURLConnection.setDoOutput(true);
        DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
        dataOutputStream.writeBytes(string2);
        dataOutputStream.flush();
        dataOutputStream.close();
        int n = httpURLConnection.getResponseCode();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuffer stringBuffer = new StringBuffer();
        while ((string = bufferedReader.readLine()) != null) {
            stringBuffer.append(string);
        }
        bufferedReader.close();
        return n == 200;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setUserAgent(String string) {
        this.userAgent = string;
    }

    public boolean isPartialMatchUserIds() {
        return this.partialMatch;
    }

    public void setPartialMatchUserIds(boolean bl) {
        this.partialMatch = bl;
    }
}

