/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal;

import com.didisoft.pgp.bc.IOUtil;
import java.math.BigInteger;
import java.util.Properties;

public class FastElGamal {
    private int byte_strength = 31;
    private int byte_strength_key = this.byte_strength + 1;
    private int bit_strength = this.byte_strength_key * 8;
    static final int PRIME_CERTAINTY = 128;
    private PublicKey public_key;
    private PrivateKey private_key;

    public FastElGamal(int n) {
        this.setSize(n);
    }

    public FastElGamal() {
    }

    public void setSize(int n) {
        this.byte_strength = n - 1;
        this.byte_strength_key = this.byte_strength + 1;
        this.bit_strength = this.byte_strength_key * 8;
    }

    public void generateKeys() {
        int n = (int)System.currentTimeMillis();
        BigInteger bigInteger = new BigInteger(this.bit_strength, 128, IOUtil.getSecureRandom());
        BigInteger bigInteger2 = FastElGamal.random(n, this.bit_strength);
        BigInteger bigInteger3 = bigInteger;
        if (bigInteger2.compareTo(bigInteger) >= 0) {
            bigInteger = bigInteger2;
            bigInteger2 = bigInteger3;
        }
        BigInteger bigInteger4 = BigInteger.ONE;
        BigInteger bigInteger5 = BigInteger.ONE;
        this.public_key = new PublicKey(bigInteger5, bigInteger, bigInteger2);
        this.private_key = new PrivateKey(bigInteger4, bigInteger);
    }

    private BigInteger getPrime(int n, int n2, int n3) {
        BigInteger bigInteger = new BigInteger("2");
        BigInteger bigInteger2 = FastElGamal.random(n, n2);
        while (!bigInteger2.isProbablePrime(n3)) {
            bigInteger2 = bigInteger2.add(bigInteger);
        }
        return bigInteger2;
    }

    private static BigInteger random(int n, int n2) {
        int n3;
        if (n2 % 8 != 0) {
            n2 += 8 - n2 % 8;
        }
        byte[] byArray = new byte[n2 / 8];
        BigInteger bigInteger = new BigInteger("2147483647");
        BigInteger bigInteger2 = new BigInteger("1073741823");
        if (n <= 0) {
            n = (int)(Math.random() * 2.147483647E9);
        }
        while (n <= 65536) {
            n *= 2;
        }
        for (n3 = 0; n3 < n2 / 8; ++n3) {
            byArray[n3] = 0;
        }
        for (n3 = 0; n3 < n2; ++n3) {
            BigInteger bigInteger3 = new BigInteger("" + (int)(Math.random() * 2.147483647E9)).add(new BigInteger("" + n)).mod(bigInteger);
            if (bigInteger3.compareTo(bigInteger2) >= 0 && n3 != 0 && n3 != n2 - 1) continue;
            int n4 = n3 / 8;
            byArray[n4] = (byte)(byArray[n4] | 1 << 7 - n3 % 8);
        }
        return new BigInteger(1, byArray);
    }

    public PublicKey getPublicKey() {
        return this.public_key;
    }

    public PrivateKey getPrivateKey() {
        return this.private_key;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.public_key = publicKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        if (privateKey == null) {
            System.out.println("Private Key is null");
        } else {
            System.out.println("Private Key is not null");
        }
        this.private_key = privateKey;
    }

    public byte[] encrypt(byte[] byArray) {
        int n = byArray.length;
        int n2 = 0;
        BigInteger bigInteger = this.public_key.getG();
        BigInteger bigInteger2 = this.public_key.getY();
        BigInteger bigInteger3 = this.public_key.getP();
        BigInteger bigInteger4 = new BigInteger("1");
        BigInteger bigInteger5 = bigInteger3.subtract(bigInteger4);
        if (n % this.byte_strength != 0) {
            n2 = n % this.byte_strength;
            n += this.byte_strength - n2;
        }
        byte[] byArray2 = new byte[n / this.byte_strength * this.byte_strength_key * 2];
        byte[] byArray3 = new byte[this.byte_strength];
        for (int i = 0; i < n / this.byte_strength; ++i) {
            BigInteger bigInteger6;
            while ((bigInteger6 = FastElGamal.random((int)System.currentTimeMillis(), this.bit_strength)).compareTo(bigInteger3) >= 0 || !bigInteger6.gcd(bigInteger5).equals(bigInteger4)) {
            }
            for (int j = 0; j < this.byte_strength; ++j) {
                byArray3[j] = i * this.byte_strength + j < byArray.length ? byArray[i * this.byte_strength + j] : (byte)(Math.random() * 255.0);
            }
            BigInteger bigInteger7 = new BigInteger(1, byArray3);
            BigInteger bigInteger8 = bigInteger.modPow(bigInteger6, bigInteger3);
            BigInteger bigInteger9 = bigInteger2.modPow(bigInteger6, bigInteger3).multiply(bigInteger7).mod(bigInteger3);
            byte[] byArray4 = bigInteger8.toByteArray();
            byte[] byArray5 = bigInteger9.toByteArray();
            this.byteArrayCopy(byArray2, byArray4, byArray4.length - this.byte_strength_key, i * 2 * this.byte_strength_key, this.byte_strength_key);
            this.byteArrayCopy(byArray2, byArray5, byArray5.length - this.byte_strength_key, i * 2 * this.byte_strength_key + this.byte_strength_key, this.byte_strength_key);
        }
        return byArray2;
    }

    public byte[] sign(byte[] byArray) {
        int n = byArray.length;
        int n2 = 0;
        BigInteger bigInteger = this.public_key.getG();
        BigInteger bigInteger2 = this.public_key.getY();
        BigInteger bigInteger3 = this.private_key.getX();
        BigInteger bigInteger4 = this.private_key.getP();
        BigInteger bigInteger5 = new BigInteger("1");
        BigInteger bigInteger6 = bigInteger4.subtract(bigInteger5);
        if (n % this.byte_strength != 0) {
            n2 = n % this.byte_strength;
            n += this.byte_strength - n2;
        }
        byte[] byArray2 = new byte[n / this.byte_strength * this.byte_strength_key * 2];
        byte[] byArray3 = new byte[this.byte_strength_key + 1];
        for (int i = 0; i < n / this.byte_strength; ++i) {
            BigInteger bigInteger7;
            while ((bigInteger7 = FastElGamal.random((int)System.currentTimeMillis(), this.bit_strength)).compareTo(bigInteger4) >= 0 || !bigInteger7.gcd(bigInteger6).equals(bigInteger5)) {
            }
            this.byteArrayCopy(byArray3, byArray, i * this.byte_strength, 1, this.byte_strength_key);
            BigInteger bigInteger8 = new BigInteger(1, byArray3);
            BigInteger bigInteger9 = bigInteger.modPow(bigInteger7, bigInteger4);
            BigInteger bigInteger10 = bigInteger7.modInverse(bigInteger6).multiply(bigInteger8.subtract(bigInteger3.multiply(bigInteger9)).mod(bigInteger6)).mod(bigInteger6);
            byte[] byArray4 = bigInteger9.toByteArray();
            byte[] byArray5 = bigInteger10.toByteArray();
            this.byteArrayCopy(byArray2, byArray4, byArray4.length - this.byte_strength_key, i * 2 * this.byte_strength_key, this.byte_strength_key);
            this.byteArrayCopy(byArray2, byArray5, byArray5.length - this.byte_strength_key, i * 2 * this.byte_strength_key + this.byte_strength_key, this.byte_strength_key);
        }
        return byArray2;
    }

    private void byteArrayCopy(byte[] byArray, byte[] byArray2, int n, int n2, int n3) {
        int n4;
        if (n < 0) {
            n = 0;
        }
        int n5 = 0;
        int n6 = byArray2.length - n;
        if (n6 < n3) {
            n5 = n3 - n6;
            for (n4 = 0; n4 < n5; ++n4) {
                byArray[n4 + n2] = 0;
            }
        }
        for (n4 = n5; n4 < n3; ++n4) {
            byArray[n4 + n2] = byArray2[n4 + n - n5];
        }
    }

    public byte[] decrypt(byte[] byArray, int n) {
        if (this.private_key == null) {
            System.out.println("Private key i snull");
        }
        byte[] byArray2 = new byte[n];
        byte[] byArray3 = new byte[this.byte_strength_key + 1];
        byte[] byArray4 = new byte[this.byte_strength];
        BigInteger bigInteger = this.private_key.getX();
        BigInteger bigInteger2 = this.private_key.getP();
        int n2 = this.byte_strength;
        for (int i = 0; i < byArray.length / this.byte_strength_key / 2; ++i) {
            this.byteArrayCopy(byArray3, byArray, i * this.byte_strength_key * 2, 1, this.byte_strength_key);
            BigInteger bigInteger3 = new BigInteger(1, byArray3);
            this.byteArrayCopy(byArray3, byArray, i * this.byte_strength_key * 2 + this.byte_strength_key, 1, this.byte_strength_key);
            BigInteger bigInteger4 = new BigInteger(1, byArray3);
            BigInteger bigInteger5 = bigInteger4.multiply(bigInteger3.modInverse(bigInteger2).modPow(bigInteger, bigInteger2)).mod(bigInteger2);
            if (i * this.byte_strength + n2 > n) {
                n2 = n - i * this.byte_strength;
            }
            if (i * this.byte_strength > n) break;
            byte[] byArray5 = bigInteger5.toByteArray();
            this.byteArrayCopy(byArray4, byArray5, byArray5.length - this.byte_strength, 0, this.byte_strength);
            this.byteArrayCopy(byArray2, byArray4, 0, i * this.byte_strength, n2);
        }
        return byArray2;
    }

    public boolean verify(byte[] byArray, byte[] byArray2) {
        byte[] byArray3 = new byte[this.byte_strength_key + 1];
        byte[] byArray4 = new byte[this.byte_strength];
        BigInteger bigInteger = this.public_key.getG();
        BigInteger bigInteger2 = this.public_key.getY();
        BigInteger bigInteger3 = this.public_key.getP();
        int n = this.byte_strength;
        boolean bl = byArray.length / this.byte_strength_key / 2 > 0;
        for (int i = 0; i < byArray.length / this.byte_strength_key / 2 && bl; ++i) {
            this.byteArrayCopy(byArray3, byArray2, i * this.byte_strength, 1, this.byte_strength_key);
            BigInteger bigInteger4 = new BigInteger(1, byArray3);
            this.byteArrayCopy(byArray3, byArray, i * this.byte_strength_key * 2, 1, this.byte_strength_key);
            BigInteger bigInteger5 = new BigInteger(1, byArray3);
            this.byteArrayCopy(byArray3, byArray, i * this.byte_strength_key * 2 + this.byte_strength_key, 1, this.byte_strength_key);
            BigInteger bigInteger6 = new BigInteger(1, byArray3);
            bl = bigInteger2.modPow(bigInteger5, bigInteger3).multiply(bigInteger5.modPow(bigInteger6, bigInteger3)).mod(bigInteger3).equals(bigInteger.modPow(bigInteger4, bigInteger3));
        }
        return bl;
    }

    public static class PrivateKey {
        private BigInteger x;
        private BigInteger p;

        public PrivateKey(BigInteger bigInteger, BigInteger bigInteger2) {
            this.x = bigInteger;
            this.p = bigInteger2;
        }

        public PrivateKey(Properties properties) {
            String string = (String)properties.get("x");
            String string2 = (String)properties.get("p");
            if (string == null) {
                string = (String)properties.get("X");
            }
            if (string2 == null) {
                string2 = (String)properties.get("P");
            }
            this.x = new BigInteger(string);
            this.p = new BigInteger(string2);
        }

        public BigInteger getX() {
            return this.x;
        }

        public BigInteger getP() {
            return this.p;
        }

        public Properties getProperties() {
            Properties properties = new Properties();
            properties.setProperty("x", this.x.toString());
            properties.setProperty("p", this.p.toString());
            return properties;
        }
    }

    public static class PublicKey {
        private BigInteger y;
        private BigInteger p;
        private BigInteger g;

        public PublicKey(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3) {
            this.y = bigInteger;
            this.p = bigInteger2;
            this.g = bigInteger3;
        }

        public PublicKey(Properties properties) {
            String string = (String)properties.get("p");
            String string2 = (String)properties.get("y");
            String string3 = (String)properties.get("g");
            if (string == null) {
                string = (String)properties.get("P");
            }
            if (string2 == null) {
                string2 = (String)properties.get("Y");
            }
            if (string3 == null) {
                string3 = (String)properties.get("G");
            }
            this.p = new BigInteger(string);
            this.y = new BigInteger(string2);
            this.g = new BigInteger(string3);
        }

        public BigInteger getY() {
            return this.y;
        }

        public BigInteger getP() {
            return this.p;
        }

        public BigInteger getG() {
            return this.g;
        }

        public Properties getProperties() {
            Properties properties = new Properties();
            properties.setProperty("y", this.y.toString());
            properties.setProperty("p", this.p.toString());
            properties.setProperty("g", this.g.toString());
            return properties;
        }
    }
}

