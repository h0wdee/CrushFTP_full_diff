/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal;

import com.didisoft.pgp.bc.elgamal.BaseElGamalParams;
import com.didisoft.pgp.bc.elgamal.BaseElGamalPrivateKey;
import com.didisoft.pgp.bc.elgamal.BaseElGamalPublicKey;
import com.didisoft.pgp.bc.elgamal.CryptixException;
import com.didisoft.pgp.bc.elgamal.DefaultElGamalParameterSet;
import com.didisoft.pgp.bc.elgamal.GenericElGamalParameterSet;
import com.didisoft.pgp.bc.elgamal.Prime;
import com.didisoft.pgp.bc.elgamal.interfaces.ElGamalKeyPairGenerator;
import com.didisoft.pgp.bc.elgamal.interfaces.ElGamalParams;
import com.didisoft.pgp.bc.elgamal.util.Debug;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

public class BaseElGamalKeyPairGenerator
extends KeyPairGenerator
implements ElGamalKeyPairGenerator {
    private static final boolean DEBUG = true;
    private static final int debuglevel = Debug.getLevel("ElGamal", "BaseElGamalKeyPairGenerator");
    private static final PrintWriter err = Debug.getOutput();
    private boolean withLucasLehmerTest = true;
    private static final int CONFIDENCE = 80;
    private static final boolean USE_PRECOMPUTED = true;
    private static final boolean USE_SMALL_G = false;
    private static final int PRIME_TYPE = 0;
    private static final int MIN_PRIME_LEN = 256;
    private static final BigInteger ZERO = BigInteger.valueOf(0L);
    private static final BigInteger ONE = BigInteger.valueOf(1L);
    private static GenericElGamalParameterSet defaultParamSet = new DefaultElGamalParameterSet();
    protected BigInteger p;
    protected BigInteger g;
    protected SecureRandom source;
    private static BigInteger[] efficientBases;

    private static void debug(String string) {
    }

    private static void progress(String string) {
    }

    public BaseElGamalKeyPairGenerator() {
        super("ElGamal");
    }

    public void initialize(ElGamalParams elGamalParams, SecureRandom secureRandom) throws InvalidParameterException {
        this.initialize(elGamalParams.getP(), elGamalParams.getG(), secureRandom);
    }

    public void initialize(BigInteger bigInteger, BigInteger bigInteger2, SecureRandom secureRandom) throws InvalidParameterException {
        if (bigInteger == null) {
            throw new NullPointerException("prime == null");
        }
        if (bigInteger2 == null) {
            throw new NullPointerException("base == null");
        }
        if (secureRandom == null) {
            throw new NullPointerException("random == null");
        }
        if (bigInteger2.compareTo(bigInteger) >= 0) {
            throw new InvalidParameterException("base >= prime");
        }
        this.p = bigInteger;
        this.g = bigInteger2;
        this.source = secureRandom;
    }

    public void initialize(int n, SecureRandom secureRandom) {
        ElGamalParams elGamalParams = null;
        if (defaultParamSet != null) {
            elGamalParams = defaultParamSet.getParameters(n);
        }
        if (elGamalParams == null) {
            elGamalParams = this.generateParams(n, secureRandom);
        }
        this.p = elGamalParams.getP();
        this.g = elGamalParams.getG();
        this.source = secureRandom;
    }

    public void initialize(int n, boolean bl, SecureRandom secureRandom) throws InvalidParameterException {
        ElGamalParams elGamalParams;
        if (n < 256) {
            throw new InvalidParameterException("ElGamal: prime length " + n + " is too short (< " + 256 + ")");
        }
        if (bl || defaultParamSet == null) {
            elGamalParams = this.generateParams(n, secureRandom);
        } else {
            elGamalParams = defaultParamSet.getParameters(n);
            if (elGamalParams == null) {
                throw new InvalidParameterException("ElGamal: no pre-computed parameters for prime length " + n);
            }
        }
        this.p = elGamalParams.getP();
        this.g = elGamalParams.getG();
        this.source = secureRandom;
    }

    public KeyPair generateKeyPair() {
        if (this.p == null) {
            throw new CryptixException("ElGamal: key pair generator not initialized");
        }
        int n = this.p.bitLength() - 1;
        BigInteger bigInteger = new BigInteger(n, this.source).setBit(n);
        BaseElGamalPrivateKey baseElGamalPrivateKey = new BaseElGamalPrivateKey(this.p, this.g, bigInteger);
        BaseElGamalPublicKey baseElGamalPublicKey = new BaseElGamalPublicKey(this.p, this.g, baseElGamalPrivateKey.getY());
        return new KeyPair(baseElGamalPublicKey, baseElGamalPrivateKey);
    }

    public ElGamalParams generateParams(int n, SecureRandom secureRandom) throws InvalidParameterException {
        if (n < 256) {
            throw new InvalidParameterException("ElGamal: prime length " + n + " is too short (< " + 256 + ")");
        }
        if (this.withLucasLehmerTest) {
            Object[] objectArray = Prime.getElGamal(n, 80, secureRandom, 0);
            BigInteger bigInteger = (BigInteger)objectArray[0];
            BigInteger[] bigIntegerArray = (BigInteger[])objectArray[1];
            BigInteger bigInteger2 = BaseElGamalKeyPairGenerator.findG(bigInteger, bigIntegerArray, secureRandom);
            return new BaseElGamalParams(bigInteger, bigInteger2);
        }
        Object[] objectArray = Prime.getElGamal(n, 80, secureRandom, 0, this.withLucasLehmerTest);
        BigInteger bigInteger = (BigInteger)objectArray[0];
        BigInteger[] bigIntegerArray = (BigInteger[])objectArray[1];
        BigInteger bigInteger3 = BaseElGamalKeyPairGenerator.findG(bigInteger, bigIntegerArray, secureRandom);
        return new BaseElGamalParams(bigInteger, bigInteger3);
    }

    public static BigInteger findG(BigInteger bigInteger, BigInteger[] bigIntegerArray, SecureRandom secureRandom) {
        BigInteger bigInteger2;
        int n;
        BigInteger bigInteger3 = bigInteger.subtract(ONE);
        BigInteger[] bigIntegerArray2 = new BigInteger[bigIntegerArray.length];
        for (n = 0; n < bigIntegerArray.length; ++n) {
            bigIntegerArray2[n] = bigInteger3.divide(bigIntegerArray[n]);
        }
        if (debuglevel >= 5) {
            BaseElGamalKeyPairGenerator.progress("g =");
        }
        n = bigInteger.bitLength() - 1;
        do {
            if (debuglevel < 5) continue;
            BaseElGamalKeyPairGenerator.progress(" ?");
        } while (!Prime.isGeneratorModP(bigInteger2 = new BigInteger(n, secureRandom).setBit(n), bigInteger, bigIntegerArray2));
        if (debuglevel >= 4) {
            err.println(" OK");
        }
        return bigInteger2;
    }

    public boolean isWithLucasLehmerTest() {
        return this.withLucasLehmerTest;
    }

    public void setWithLucasLehmerTest(boolean bl) {
        this.withLucasLehmerTest = bl;
    }
}

