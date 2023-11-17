/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal;

import com.didisoft.pgp.bc.elgamal.GenericElGamalParameterSet;

public class DefaultElGamalParameterSet
extends GenericElGamalParameterSet {
    private static final int[] primeLengths = new int[]{512, 768};
    private static final String[][] precomputed = new String[][]{{"48be6b5f8d2a96c39a7bb1047dae6d0796cd3c9b3cc875758e1ad86da82af35e56059756fdce765d2ef38e0670397bb5243e8f101c6c7f13b2d70217d7550649801", "7ddd0ba5d8861f8425f26cd65790852fe68a664461603574ec32288d8dc5680e069e18c9a9d0d8395d0e0c2fa623124b7024c5f5c077f30782af7016298decf300c"}, {"76a59d6204e58995115b833dae2f4baefd1a8f3ae914d7c4e2ca4227c90e07c45e8532d20f0dcbfbb3a31a00baace24ae5afb940c4603cf8841e6a9018913761442aa2dd7c9b48dc4e89bdaefc9169c7167c9db41c733531b6610ed696a8e38291a5", "667503f758ace0e04a29b7b660452e56cbb564cb22828a68fdfe8af1cfd90242d444ee3b236a6d1e47a1def82f5082598891c2ce031e4c1ac6883349c29459032cb57754c6eca99474de8371a04d9dba6ccffc7fa55bc3b04b75c0f6ad742836f9d1"}};

    public DefaultElGamalParameterSet() {
        super(primeLengths, precomputed);
    }
}

