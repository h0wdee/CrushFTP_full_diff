/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;

class LicenseVerification {
    static LicenseVerification instance;
    String licensee;
    String license;
    String comments;
    String description;
    long expires;
    String product;
    public static final int EXPIRED = 1;
    public static final int INVALID = 2;
    public static final int OK = 4;
    public static final int NOT_LICENSED = 8;
    public static final int EXPIRED_SUBSCRIPTION = 16;
    static final int LICENSE_VERIFICATION_MASK = 31;
    static final int LICENSE_TYPE_MASK = 65504;
    int status = 8;
    int type;
    BigInteger modulus = new BigInteger(new byte[]{0, -114, 50, 88, -53, 37, -110, -74, -67, 34, 105, 25, 91, 43, -35, 116, -13, 86, 51, -124, 59, 6, -117, 107, 89, 115, 77, -127, -117, 8, -54, -35, -70, -32, -99, -84, -51, 63, 56, 77, -18, -7, 82, -66, -62, 68, -49, 29, 15, 106, 39, 107, 68, 66, 22, -101, 99, 44, -20, -77, 75, -103, -12, 101, -100, -127, 36, 43, 99, -34, -33, -60, -104, -64, 58, 120, -72, 46, 94, -34, 23, -76, 73, 41, -121, 3, 105, -49, -4, -104, 56, 53, 48, 87, -7, -78, -102, -85, -93, -87, 15, 112, 19, -75, 61, -31, -51, -71, -10, -127, 106, -20, 50, -103, 95, -126, -2, -12, 43, 85, 109, -7, -70, -75, -25, 122, 108, -22, -111});
    BigInteger exponent = new BigInteger(new byte[]{1, 0, 1});

    LicenseVerification() {
        this.loadLicenses();
    }

    void loadLicenses() {
        this.treResource(this.getClass().getClassLoader().getResource("maverick-license.txt"));
        this.treResource(this.getClass().getClassLoader().getResource("META-INF/maverick-license.txt"));
        try {
            this.tryLicenseFile(new File(System.getProperty("maverick.license.directory", System.getProperty("user.dir")) + File.separator + System.getProperty("maverick.license.filename", ".maverick-license.txt")));
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.tryLicenseFile(new File(System.getProperty("maverick.license.directory", System.getProperty("user.dir")) + File.separator + System.getProperty("maverick.license.filename", "maverick-license.txt")));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void treResource(URL resource) {
        if (resource != null) {
            try (InputStream in = resource.openStream();){
                this.loadLicense(in);
            }
            catch (Exception ioe) {
                System.err.println("WARNING: Failed to read Maverick license resource " + resource + ". " + ioe.getMessage());
            }
        }
    }

    private void tryLicenseFile(File file) {
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file);){
                this.loadLicense(in);
            }
            catch (Exception e) {
                System.err.println("WARNING: Failed to read Maverick license file " + file + ". " + e.getMessage());
            }
        }
    }

    void loadLicense(InputStream in) throws IOException {
        String fullText = this.readToString(in);
        while (!fullText.startsWith("\"----BEGIN 3SP LICENSE")) {
            fullText = fullText.substring(1);
        }
        while (!fullText.endsWith("----END 3SP LICENSE----\\r\\n")) {
            fullText = fullText.substring(0, fullText.length() - 1);
        }
        StringBuffer buf = new StringBuffer();
        boolean inQuote = false;
        boolean escape = false;
        for (int i = 0; i < fullText.length(); ++i) {
            int ch = fullText.charAt(i);
            if (ch == 34 && !inQuote && !escape) {
                inQuote = true;
                continue;
            }
            if (ch == 34 && inQuote && !escape) {
                inQuote = false;
                continue;
            }
            if (!inQuote) continue;
            if (!escape && ch == 92) {
                escape = true;
                continue;
            }
            if (!inQuote) continue;
            if (escape) {
                if (ch == 114) {
                    ch = 13;
                } else if (ch == 110) {
                    ch = 10;
                }
                escape = false;
            }
            buf.append((char)ch);
        }
        this.license = buf.toString();
    }

    static LicenseVerification getInstance() {
        return instance == null ? (instance = new LicenseVerification()) : instance;
    }

    final synchronized void setLicense(String license) {
        this.license = license;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final synchronized void addLicense(InputStream in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            this.license = this.readToString(in);
        }
        catch (IOException ex) {
            this.license = "";
        }
        finally {
            try {
                in.close();
            }
            catch (IOException iOException) {}
            try {
                out.close();
            }
            catch (IOException iOException) {}
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final String readToString(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int read;
            while ((read = in.read()) > -1) {
                out.write(read);
            }
            String string = new String(out.toByteArray(), "UTF-8");
            return string;
        }
        finally {
            try {
                in.close();
            }
            catch (IOException iOException) {}
            try {
                out.close();
            }
            catch (IOException iOException) {}
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    final synchronized int verifyLicense() {
        String[] attemptEncodings = new String[]{"UTF-8", "ISO-8859-1"};
        int e = 0;
        while (true) {
            if (e >= attemptEncodings.length) {
                this.status = 2;
                return 2;
            }
            try {
                long releaseDate = 1697127759215L;
                long year = 31708800000L;
                long unregistered = 1209600000L;
                if (this.license == null || this.license.equals("")) {
                    this.status = 8;
                    return 8;
                }
                LicenseRSAKey mykey = new LicenseRSAKey(this.modulus, this.exponent);
                SimpleReader reader = new SimpleReader(new ByteArrayInputStream(this.license.getBytes(attemptEncodings[e])));
                String line = reader.readLine();
                if (!line.equals("----BEGIN 3SP LICENSE----") && !line.equals("----BEGIN SSHTOOLS LICENSE----")) {
                    this.status = 2;
                    return 2;
                }
                String licensee = "";
                String comments = "";
                String validfrom = null;
                String expires = null;
                String product = "";
                StringBuffer keydataBuffer = new StringBuffer("");
                while ((line = reader.readLine()) != null && !line.equals("----END 3SP LICENSE----") && !line.equals("----END SSHTOOLS LICENSE----")) {
                    int idx = line.indexOf(58);
                    if (idx > -1) {
                        String header = line.substring(0, idx).trim();
                        String value = line.substring(idx + 1).trim();
                        if (header.equals("Licensee")) {
                            licensee = value;
                            continue;
                        }
                        if (header.equals("Comments")) {
                            comments = value;
                            continue;
                        }
                        if (header.equals("Created")) {
                            validfrom = value;
                            continue;
                        }
                        if (header.equals("Type")) {
                            this.description = value;
                            continue;
                        }
                        if (header.equals("Product")) {
                            product = value;
                            continue;
                        }
                        if (header.equals("Expires")) {
                            expires = value;
                            continue;
                        }
                        keydataBuffer.append(header);
                        for (int i = header.length(); i < 8; ++i) {
                            keydataBuffer.append(' ');
                        }
                        keydataBuffer.append(": " + value);
                        continue;
                    }
                    keydataBuffer.append(line);
                }
                String keydata = keydataBuffer.toString();
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                int pos = 0;
                while (pos < keydata.length()) {
                    if (keydata.charAt(pos) == '\r' || keydata.charAt(pos) == '\n') {
                        ++pos;
                        continue;
                    }
                    buf.write(Integer.parseInt(keydata.substring(pos, pos + 2), 16));
                    pos += 2;
                }
                DataInputStream din = new DataInputStream(new ByteArrayInputStream(buf.toByteArray()));
                byte[] tmp = new byte[16];
                din.readFully(tmp);
                byte[] xor = new byte[]{55, -121, 33, 9, 68, 73, 11, -37, -39, -1, 12, 48, 99, 49, 11, 55};
                for (int i = 0; i < 16; ++i) {
                    int n = i;
                    tmp[n] = (byte)(tmp[n] ^ xor[i]);
                }
                DataInputStream din2 = new DataInputStream(new ByteArrayInputStream(tmp));
                long startdate = din2.readLong();
                long enddate = din2.readLong();
                byte[] signature = new byte[din.available()];
                din.readFully(signature);
                for (int i = 0; i < 23; ++i) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try (MyDataOutputStream dout = new MyDataOutputStream(out);){
                        dout.writeString(product);
                        dout.writeString("3SP Ltd");
                        dout.writeString(comments);
                        dout.writeString(licensee);
                        int type = 256 << i;
                        dout.writeInt(type);
                        dout.writeString(this.description);
                        dout.writeLong(startdate);
                        dout.writeLong(enddate);
                        if (enddate <= System.currentTimeMillis()) {
                            this.comments = comments;
                            this.licensee = licensee;
                            this.product = product;
                            this.type = type;
                            this.expires = enddate;
                            int n = this.status = 1 | type;
                            return n;
                        }
                        if (!mykey.verifySignature(signature, out.toByteArray())) continue;
                        if (releaseDate > startdate + year) {
                            int n = this.status = 0x10 | type;
                            return n;
                        }
                        this.comments = comments;
                        this.licensee = licensee;
                        this.product = product;
                        this.type = type;
                        this.expires = enddate;
                        int n = this.status = 4 | type;
                        return n;
                    }
                }
            }
            catch (Throwable ex) {
                // empty catch block
            }
            ++e;
        }
    }

    synchronized String getDescription() {
        return this.description;
    }

    synchronized String getComments() {
        return this.comments;
    }

    synchronized String getLicensee() {
        return this.licensee;
    }

    synchronized int getStatus() {
        return this.status;
    }

    synchronized String getProduct() {
        return this.product;
    }

    synchronized long getExpiryDate() {
        return this.expires;
    }

    synchronized int getType() {
        return this.type;
    }

    class SHA1Digest
    extends GeneralDigest {
        private final int DIGEST_LENGTH = 20;
        private int H1;
        private int H2;
        private int H3;
        private int H4;
        private int H5;
        private int[] X;
        private int xOff;
        private final int Y1 = 1518500249;
        private final int Y2 = 1859775393;
        private final int Y3 = -1894007588;
        private final int Y4 = -899497514;

        public SHA1Digest() {
            this.DIGEST_LENGTH = 20;
            this.X = new int[80];
            this.Y1 = 1518500249;
            this.Y2 = 1859775393;
            this.Y3 = -1894007588;
            this.Y4 = -899497514;
            this.reset();
        }

        public SHA1Digest(SHA1Digest t) {
            super(t);
            this.DIGEST_LENGTH = 20;
            this.X = new int[80];
            this.Y1 = 1518500249;
            this.Y2 = 1859775393;
            this.Y3 = -1894007588;
            this.Y4 = -899497514;
            this.H1 = t.H1;
            this.H2 = t.H2;
            this.H3 = t.H3;
            this.H4 = t.H4;
            this.H5 = t.H5;
            System.arraycopy(t.X, 0, this.X, 0, t.X.length);
            this.xOff = t.xOff;
        }

        @Override
        public String getAlgorithmName() {
            return "SHA-1";
        }

        @Override
        public int getDigestSize() {
            return 20;
        }

        @Override
        protected void processWord(byte[] in, int inOff) {
            this.X[this.xOff++] = (in[inOff] & 0xFF) << 24 | (in[inOff + 1] & 0xFF) << 16 | (in[inOff + 2] & 0xFF) << 8 | in[inOff + 3] & 0xFF;
            if (this.xOff == 16) {
                this.processBlock();
            }
        }

        private void unpackWord(int word, byte[] out, int outOff) {
            out[outOff] = (byte)(word >>> 24);
            out[outOff + 1] = (byte)(word >>> 16);
            out[outOff + 2] = (byte)(word >>> 8);
            out[outOff + 3] = (byte)word;
        }

        @Override
        protected void processLength(long bitLength) {
            if (this.xOff > 14) {
                this.processBlock();
            }
            this.X[14] = (int)(bitLength >>> 32);
            this.X[15] = (int)(bitLength & 0xFFFFFFFFFFFFFFFFL);
        }

        @Override
        public int doFinal(byte[] out, int outOff) {
            this.finish();
            this.unpackWord(this.H1, out, outOff);
            this.unpackWord(this.H2, out, outOff + 4);
            this.unpackWord(this.H3, out, outOff + 8);
            this.unpackWord(this.H4, out, outOff + 12);
            this.unpackWord(this.H5, out, outOff + 16);
            this.reset();
            return 20;
        }

        @Override
        public void reset() {
            super.reset();
            this.H1 = 1732584193;
            this.H2 = -271733879;
            this.H3 = -1732584194;
            this.H4 = 271733878;
            this.H5 = -1009589776;
            this.xOff = 0;
            for (int i = 0; i != this.X.length; ++i) {
                this.X[i] = 0;
            }
        }

        private int f(int u, int v, int w) {
            return u & v | ~u & w;
        }

        private int h(int u, int v, int w) {
            return u ^ v ^ w;
        }

        private int g(int u, int v, int w) {
            return u & v | u & w | v & w;
        }

        private int rotateLeft(int x, int n) {
            return x << n | x >>> 32 - n;
        }

        @Override
        protected void processBlock() {
            int t;
            int j;
            for (int i = 16; i <= 79; ++i) {
                this.X[i] = this.rotateLeft(this.X[i - 3] ^ this.X[i - 8] ^ this.X[i - 14] ^ this.X[i - 16], 1);
            }
            int A = this.H1;
            int B = this.H2;
            int C = this.H3;
            int D = this.H4;
            int E = this.H5;
            for (j = 0; j <= 19; ++j) {
                t = this.rotateLeft(A, 5) + this.f(B, C, D) + E + this.X[j] + 1518500249;
                E = D;
                D = C;
                C = this.rotateLeft(B, 30);
                B = A;
                A = t;
            }
            for (j = 20; j <= 39; ++j) {
                t = this.rotateLeft(A, 5) + this.h(B, C, D) + E + this.X[j] + 1859775393;
                E = D;
                D = C;
                C = this.rotateLeft(B, 30);
                B = A;
                A = t;
            }
            for (j = 40; j <= 59; ++j) {
                t = this.rotateLeft(A, 5) + this.g(B, C, D) + E + this.X[j] + -1894007588;
                E = D;
                D = C;
                C = this.rotateLeft(B, 30);
                B = A;
                A = t;
            }
            for (j = 60; j <= 79; ++j) {
                t = this.rotateLeft(A, 5) + this.h(B, C, D) + E + this.X[j] + -899497514;
                E = D;
                D = C;
                C = this.rotateLeft(B, 30);
                B = A;
                A = t;
            }
            this.H1 += A;
            this.H2 += B;
            this.H3 += C;
            this.H4 += D;
            this.H5 += E;
            this.xOff = 0;
            for (int i = 0; i != this.X.length; ++i) {
                this.X[i] = 0;
            }
        }
    }

    abstract class GeneralDigest {
        private byte[] xBuf;
        private int xBufOff;
        private long byteCount;

        protected GeneralDigest() {
            this.xBuf = new byte[4];
            this.xBufOff = 0;
        }

        protected GeneralDigest(GeneralDigest t) {
            this.xBuf = new byte[t.xBuf.length];
            System.arraycopy(t.xBuf, 0, this.xBuf, 0, t.xBuf.length);
            this.xBufOff = t.xBufOff;
            this.byteCount = t.byteCount;
        }

        public void update(byte in) {
            this.xBuf[this.xBufOff++] = in;
            if (this.xBufOff == this.xBuf.length) {
                this.processWord(this.xBuf, 0);
                this.xBufOff = 0;
            }
            ++this.byteCount;
        }

        public void update(byte[] in, int inOff, int len) {
            while (this.xBufOff != 0 && len > 0) {
                this.update(in[inOff]);
                ++inOff;
                --len;
            }
            while (len > this.xBuf.length) {
                this.processWord(in, inOff);
                inOff += this.xBuf.length;
                len -= this.xBuf.length;
                this.byteCount += (long)this.xBuf.length;
            }
            while (len > 0) {
                this.update(in[inOff]);
                ++inOff;
                --len;
            }
        }

        public void finish() {
            long bitLength = this.byteCount << 3;
            this.update((byte)-128);
            while (this.xBufOff != 0) {
                this.update((byte)0);
            }
            this.processLength(bitLength);
            this.processBlock();
        }

        public void reset() {
            this.byteCount = 0L;
            this.xBufOff = 0;
            for (int i = 0; i < this.xBuf.length; ++i) {
                this.xBuf[i] = 0;
            }
        }

        protected abstract void processWord(byte[] var1, int var2);

        protected abstract void processLength(long var1);

        protected abstract void processBlock();

        public abstract int getDigestSize();

        public abstract int doFinal(byte[] var1, int var2);

        public abstract String getAlgorithmName();
    }

    class RsaPublicKey {
        BigInteger publicExponent;
        BigInteger modulus;
        final byte[] ASN_SHA1 = new byte[]{48, 33, 48, 9, 6, 5, 43, 14, 3, 2, 26, 5, 0, 4, 20};

        public RsaPublicKey(BigInteger modulus, BigInteger publicExponent) {
            this.modulus = modulus;
            this.publicExponent = publicExponent;
        }

        public BigInteger doPublic(BigInteger input, BigInteger modulus, BigInteger publicExponent) {
            return input.modPow(publicExponent, modulus);
        }

        public BigInteger removePKCS1(BigInteger input, int type) throws IOException {
            int i;
            byte[] strip = input.toByteArray();
            if (strip[0] != type) {
                throw new IOException("PKCS1 padding type " + type + " is not valid");
            }
            for (i = 1; i < strip.length && strip[i] != 0; ++i) {
                if (type != 1 || strip[i] == -1) continue;
                throw new IOException("Corrupt data found in expected PKSC1 padding");
            }
            if (i == strip.length) {
                throw new IOException("Corrupt data found in expected PKSC1 padding");
            }
            byte[] val = new byte[strip.length - i];
            System.arraycopy(strip, i, val, 0, val.length);
            return new BigInteger(1, val);
        }

        public boolean verifySignature(byte[] signature, byte[] msg) {
            try {
                BigInteger signatureInt = new BigInteger(1, signature);
                signatureInt = this.doPublic(signatureInt, this.modulus, this.publicExponent);
                signatureInt = this.removePKCS1(signatureInt, 1);
                signature = signatureInt.toByteArray();
                SHA1Digest h = new SHA1Digest();
                h.update(msg, 0, msg.length);
                byte[] data = new byte[h.getDigestSize()];
                h.doFinal(data, 0);
                if (data.length != signature.length - this.ASN_SHA1.length) {
                    return false;
                }
                byte[] cmp = this.ASN_SHA1;
                int i = 0;
                int j = 0;
                while (i < signature.length) {
                    if (i == this.ASN_SHA1.length) {
                        cmp = data;
                        j = 0;
                    }
                    if (signature[i] != cmp[j]) {
                        return false;
                    }
                    ++i;
                    ++j;
                }
                return true;
            }
            catch (IOException ex) {
                return false;
            }
        }
    }

    static class SimpleReader {
        InputStream in;

        SimpleReader(InputStream in) {
            this.in = in;
        }

        String readLine() throws IOException {
            int ch;
            StringBuffer buf = new StringBuffer();
            while ((ch = this.in.read()) > -1 && ch != 10) {
                buf.append((char)ch);
            }
            if (ch == -1 && buf.length() == 0) {
                return null;
            }
            return new String(buf.toString().getBytes("UTF8"), "UTF8").trim();
        }
    }

    static class MyDataOutputStream
    extends DataOutputStream {
        MyDataOutputStream(OutputStream out) {
            super(out);
        }

        void writeString(String str) throws IOException {
            byte[] tmp = str.getBytes("UTF8");
            this.writeInt(tmp.length);
            this.write(tmp);
        }
    }

    class LicenseRSAKey
    extends RsaPublicKey {
        LicenseRSAKey(BigInteger m, BigInteger e) {
            super(m, e);
        }

        public void init(byte[] b, int o, int l) {
        }

        public byte[] getEncoded() {
            return null;
        }

        public String getAlgorithm() {
            return "rsa";
        }
    }
}

