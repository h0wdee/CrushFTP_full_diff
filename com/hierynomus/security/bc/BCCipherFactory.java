/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bouncycastle.crypto.BlockCipher
 *  org.bouncycastle.crypto.BufferedBlockCipher
 *  org.bouncycastle.crypto.CipherParameters
 *  org.bouncycastle.crypto.InvalidCipherTextException
 *  org.bouncycastle.crypto.StreamCipher
 *  org.bouncycastle.crypto.engines.DESEngine
 *  org.bouncycastle.crypto.engines.RC4Engine
 *  org.bouncycastle.crypto.params.DESedeParameters
 *  org.bouncycastle.crypto.params.KeyParameter
 */
package com.hierynomus.security.bc;

import com.hierynomus.protocol.commons.Factory;
import com.hierynomus.security.Cipher;
import com.hierynomus.security.SecurityException;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.params.DESedeParameters;
import org.bouncycastle.crypto.params.KeyParameter;

public class BCCipherFactory {
    private static final Map<String, Factory<Cipher>> lookup = new HashMap<String, Factory<Cipher>>();

    public static Cipher create(String name) {
        Factory<Cipher> cipherFactory = lookup.get(name);
        if (cipherFactory == null) {
            throw new IllegalArgumentException("Unknown Cipher " + name);
        }
        return cipherFactory.create();
    }

    static {
        lookup.put("DES/ECB/NoPadding", new Factory<Cipher>(){

            @Override
            public Cipher create() {
                return new BCBlockCipher(new BufferedBlockCipher((BlockCipher)new DESEngine())){

                    @Override
                    protected CipherParameters createParams(byte[] key) {
                        return new DESedeParameters(key);
                    }
                };
            }
        });
        lookup.put("RC4", new Factory<Cipher>(){

            @Override
            public Cipher create() {
                return new BCStreamCipher((StreamCipher)new RC4Engine()){

                    @Override
                    protected CipherParameters createParams(byte[] key) {
                        return new KeyParameter(key);
                    }
                };
            }
        });
    }

    private static abstract class BCStreamCipher
    implements Cipher {
        private StreamCipher streamCipher;

        BCStreamCipher(StreamCipher streamCipher) {
            this.streamCipher = streamCipher;
        }

        @Override
        public void init(Cipher.CryptMode cryptMode, byte[] bytes) {
            this.streamCipher.init(cryptMode == Cipher.CryptMode.ENCRYPT, this.createParams(bytes));
        }

        protected abstract CipherParameters createParams(byte[] var1);

        @Override
        public int update(byte[] in, int inOff, int bytes, byte[] out, int outOff) {
            return this.streamCipher.processBytes(in, inOff, bytes, out, outOff);
        }

        @Override
        public int doFinal(byte[] out, int outOff) {
            this.streamCipher.reset();
            return 0;
        }

        @Override
        public void reset() {
            this.streamCipher.reset();
        }
    }

    private static abstract class BCBlockCipher
    implements Cipher {
        private BufferedBlockCipher wrappedCipher;

        BCBlockCipher(BufferedBlockCipher bufferedBlockCipher) {
            this.wrappedCipher = bufferedBlockCipher;
        }

        @Override
        public void init(Cipher.CryptMode cryptMode, byte[] bytes) {
            this.wrappedCipher.init(cryptMode == Cipher.CryptMode.ENCRYPT, this.createParams(bytes));
        }

        @Override
        public int update(byte[] in, int inOff, int bytes, byte[] out, int outOff) {
            return this.wrappedCipher.processBytes(in, inOff, bytes, out, outOff);
        }

        @Override
        public int doFinal(byte[] out, int outOff) throws SecurityException {
            try {
                return this.wrappedCipher.doFinal(out, outOff);
            }
            catch (InvalidCipherTextException e) {
                throw new SecurityException((Exception)((Object)e));
            }
        }

        @Override
        public void reset() {
            this.wrappedCipher.reset();
        }

        protected abstract CipherParameters createParams(byte[] var1);
    }
}

