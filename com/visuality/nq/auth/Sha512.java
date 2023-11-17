/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.auth;

import com.visuality.nq.common.Blob;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;

public class Sha512 {
    public static final int SHA512_DIGEST_SIZE = 64;
    public static final int SHA512_PREAUTH_INTEG_HASH_LENGTH = 64;
    private static final byte[] pad = new byte[]{-128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final long[] sha512_k = new long[]{4794697086780616226L, 8158064640168781261L, -5349999486874862801L, -1606136188198331460L, 4131703408338449720L, 6480981068601479193L, -7908458776815382629L, -6116909921290321640L, -2880145864133508542L, 1334009975649890238L, 2608012711638119052L, 6128411473006802146L, 8268148722764581231L, -9160688886553864527L, -7215885187991268811L, -4495734319001033068L, -1973867731355612462L, -1171420211273849373L, 1135362057144423861L, 2597628984639134821L, 3308224258029322869L, 5365058923640841347L, 6679025012923562964L, 8573033837759648693L, -7476448914759557205L, -6327057829258317296L, -5763719355590565569L, -4658551843659510044L, -4116276920077217854L, -3051310485924567259L, 489312712824947311L, 1452737877330783856L, 2861767655752347644L, 3322285676063803686L, 5560940570517711597L, 5996557281743188959L, 7280758554555802590L, 8532644243296465576L, -9096487096722542874L, -7894198246740708037L, -6719396339535248540L, -6333637450476146687L, -4446306890439682159L, -4076793802049405392L, -3345356375505022440L, -2983346525034927856L, -860691631967231958L, 1182934255886127544L, 1847814050463011016L, 2177327727835720531L, 2830643537854262169L, 3796741975233480872L, 4115178125766777443L, 5681478168544905931L, 6601373596472566643L, 7507060721942968483L, 8399075790359081724L, 8693463985226723168L, -8878714635349349518L, -8302665154208450068L, -8016688836872298968L, -6606660893046293015L, -4685533653050689259L, -4147400797238176981L, -3880063495543823972L, -3348786107499101689L, -1523767162380948706L, -757361751448694408L, 500013540394364858L, 748580250866718886L, 1242879168328830382L, 1977374033974150939L, 2944078676154940804L, 3659926193048069267L, 4368137639120453308L, 4836135668995329356L, 5532061633213252278L, 6448918945643986474L, 6902733635092675308L, 7801388544844847127L};
    public static final int SHA_512 = 1;

    private static void sha512Init(Sha512Ctx context) {
        context.h[0] = 7640891576956012808L;
        context.h[1] = -4942790177534073029L;
        context.h[2] = 4354685564936845355L;
        context.h[3] = -6534734903238641935L;
        context.h[4] = 5840696475078001361L;
        context.h[5] = -7276294671716946913L;
        context.h[6] = 2270897969802886507L;
        context.h[7] = 6620516959819538809L;
        context.totalLen = 0L;
        context.len = 0;
    }

    private static void sha512Update(Sha512Ctx context, byte[] data, int length) throws NqException {
        int dataOffset = 0;
        while (length > 0) {
            int count;
            int len = length < 128 - context.len ? length : 128 - context.len;
            int max = count + len / 8;
            int leftBits = context.len % 8;
            BufferReader reader = new BufferReader(data, dataOffset, false);
            for (count = context.len / 8; count <= max; ++count) {
                if (count == context.len / 8 && 0 != leftBits) {
                    int n = count;
                    context.w[n] = context.w[n] | reader.readLong() << leftBits * 8;
                    continue;
                }
                context.w[count] = reader.readLong();
            }
            context.len += len;
            context.totalLen += (long)len;
            dataOffset += len;
            length -= len;
            if (context.len != 128) continue;
            Sha512.sha512ProcessBlock(context);
            context.len = 0;
        }
    }

    static void sha512Final(Sha512Ctx context, byte[] digest) throws NqException {
        int i;
        long totalLen = context.totalLen * 8L;
        int paddingSize = context.len < 112 ? 112 - context.len : 240 - context.len;
        Sha512.sha512Update(context, pad, paddingSize);
        context.w[14] = 0L;
        context.w[15] = (totalLen & 0xFFL) << 56 | (totalLen & 0xFF00L) << 40 | (totalLen & 0xFF0000L) << 24 | (totalLen & 0xFF000000L) << 8 | (totalLen & 0xFF00000000L) >>> 8 | (totalLen & 0xFF0000000000L) >>> 24 | (totalLen & 0xFF000000000000L) >>> 40 | (totalLen & 0xFF00000000000000L) >>> 56;
        Sha512.sha512ProcessBlock(context);
        for (i = 0; i < 8; ++i) {
            context.h[i] = (context.h[i] & 0xFFL) << 56 | (context.h[i] & 0xFF00L) << 40 | (context.h[i] & 0xFF0000L) << 24 | (context.h[i] & 0xFF000000L) << 8 | (context.h[i] & 0xFF00000000L) >>> 8 | (context.h[i] & 0xFF0000000000L) >>> 24 | (context.h[i] & 0xFF000000000000L) >>> 40 | (context.h[i] & 0xFF00000000000000L) >>> 56;
        }
        if (digest != null) {
            BufferWriter writer = new BufferWriter(digest, 0, false);
            for (i = 0; i < context.h.length; ++i) {
                writer.writeLong(context.h[i]);
            }
        } else {
            TraceLog.get().error("digest is null");
        }
    }

    private static void sha512ProcessBlock(Sha512Ctx context) {
        int t;
        long[] w = context.w;
        long a = context.h[0];
        long b = context.h[1];
        long c = context.h[2];
        long d = context.h[3];
        long e = context.h[4];
        long f = context.h[5];
        long g = context.h[6];
        long h = context.h[7];
        for (t = 0; t < 16; ++t) {
            w[t] = (w[t] & 0xFFL) << 56 | (w[t] & 0xFF00L) << 40 | (w[t] & 0xFF0000L) << 24 | (w[t] & 0xFF000000L) << 8 | (w[t] & 0xFF00000000L) >>> 8 | (w[t] & 0xFF0000000000L) >>> 24 | (w[t] & 0xFF000000000000L) >>> 40 | (w[t] & 0xFF00000000000000L) >>> 56;
        }
        for (t = 16; t < 80; ++t) {
            w[t] = ((w[t - 2] >>> 19 | w[t - 2] << 45) ^ (w[t - 2] >>> 61 | w[t - 2] << 3) ^ w[t - 2] >>> 6) + w[t - 7] + ((w[t - 15] >>> 1 | w[t - 15] << 63) ^ (w[t - 15] >>> 8 | w[t - 15] << 56) ^ w[t - 15] >>> 7) + w[t - 16];
        }
        for (t = 0; t < 80; ++t) {
            long temp1 = h + ((e >>> 14 | e << 50) ^ (e >>> 18 | e << 46) ^ (e >>> 41 | e << 23)) + (g ^ e & (f ^ g)) + sha512_k[t] + w[t];
            long temp2 = ((a >>> 28 | a << 36) ^ (a >>> 34 | a << 30) ^ (a >>> 39 | a << 25)) + (a & b | a & c | b & c);
            h = g;
            g = f;
            f = e;
            e = temp1 + d;
            d = c;
            c = b;
            b = a;
            a = temp1 + temp2;
        }
        context.h[0] = context.h[0] + a;
        context.h[1] = context.h[1] + b;
        context.h[2] = context.h[2] + c;
        context.h[3] = context.h[3] + d;
        context.h[4] = context.h[4] + e;
        context.h[5] = context.h[5] + f;
        context.h[6] = context.h[6] + g;
        context.h[7] = context.h[7] + h;
    }

    public static void sha512Internal(Blob key, Blob key1, Blob[] dataFragments, int numFragments, byte[] buffer, int bufferSize, Object sha512CtxBuf) throws NqException {
        Sha512Ctx ctx = null != sha512CtxBuf ? (Sha512Ctx)sha512CtxBuf : new Sha512().new Sha512Ctx();
        Sha512.sha512Init(ctx);
        for (int i = 0; i < numFragments; ++i) {
            if (null == dataFragments[i].data || dataFragments[i].len <= 0) continue;
            Sha512.sha512Update(ctx, dataFragments[i].data, dataFragments[i].len);
        }
        Sha512.sha512Final(ctx, buffer);
    }

    public static void sha512_compute(byte[] buffer, int bufferSize, byte[] digestResult) throws NqException {
        Sha512Ctx context = new Sha512().new Sha512Ctx();
        Sha512.sha512Init(context);
        Sha512.sha512Update(context, buffer, bufferSize);
        Sha512.sha512Final(context, digestResult);
        if (digestResult != null) {
            BufferWriter writer = new BufferWriter(digestResult, 0, false);
            for (int i = 0; i < context.h.length; ++i) {
                writer.writeLong(context.h[i]);
            }
        } else {
            TraceLog.get().error("digest is null");
        }
    }

    class SHA_U64 {
        int high;
        int low;

        SHA_U64() {
        }
    }

    class Sha512Ctx {
        long[] h = new long[8];
        long[] w = new long[80];
        int len;
        long totalLen;

        Sha512Ctx() {
        }
    }
}

