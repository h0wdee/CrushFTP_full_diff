/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.kbx;

import com.didisoft.pgp.bc.kbx.KBXBlobType;
import com.didisoft.pgp.bc.kbx.KBXKey;
import com.didisoft.pgp.bc.kbx.KBXUserID;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class KBXDataBlob {
    int length;
    byte BlobTypeRaw;
    public KBXBlobType BlobType;
    byte VersionNumber;
    short BlobFlags;
    int Offset;
    int KeyBlocklength;
    short NumberOfKeys;
    short KeyInfoSize;
    KBXKey[] keys;
    short SerialNumberlength;
    byte[] SerialNumber;
    short NumberOfUserIDs;
    short UserIDSize;
    KBXUserID[] userIds;
    short NumberOfSignatures;
    short SizeOfSignature;
    int[] SignatureExpirations;
    byte Ownertrust;
    byte AllValidity;
    short Reserved1;
    int RecheckAfter;
    int LatestTimestamp;
    int BlobCreatedAt;
    int ReservedSpacelength;
    byte[] ReservedSpace;
    byte[] ArbitratrySpace;
    public byte[] Blob;
    byte[] ExtraSpace;
    byte[] Sha1;
    static final long IMAGELEN_LIMIT = 0x500000L;

    public static KBXDataBlob readFromStream(InputStream inputStream) throws IOException {
        int n;
        KBXDataBlob kBXDataBlob = new KBXDataBlob();
        int n2 = 0;
        kBXDataBlob.length = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
        n2 += 4;
        kBXDataBlob.BlobTypeRaw = (byte)inputStream.read();
        try {
            kBXDataBlob.BlobType = KBXBlobType.fromInt(kBXDataBlob.BlobTypeRaw);
        }
        catch (EOFException eOFException) {
            return null;
        }
        ++n2;
        if ((long)kBXDataBlob.length > 0x500000L) {
            return null;
        }
        kBXDataBlob.VersionNumber = (byte)inputStream.read();
        ++n2;
        kBXDataBlob.BlobFlags = (short)(inputStream.read() << 8 | inputStream.read());
        n2 += 2;
        kBXDataBlob.Offset = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
        n2 += 4;
        kBXDataBlob.KeyBlocklength = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
        n2 += 4;
        kBXDataBlob.NumberOfKeys = (short)(inputStream.read() << 8 | inputStream.read());
        n2 += 2;
        kBXDataBlob.KeyInfoSize = (short)(inputStream.read() << 8 | inputStream.read());
        n2 += 2;
        kBXDataBlob.keys = new KBXKey[kBXDataBlob.NumberOfKeys];
        for (n = 0; n < kBXDataBlob.NumberOfKeys; ++n) {
            kBXDataBlob.keys[n] = KBXKey.create(inputStream, kBXDataBlob.KeyInfoSize);
            n2 += kBXDataBlob.KeyInfoSize;
        }
        kBXDataBlob.SerialNumberlength = (short)(inputStream.read() << 8 | inputStream.read());
        n2 += 2;
        kBXDataBlob.SerialNumber = new byte[kBXDataBlob.SerialNumberlength];
        inputStream.read(kBXDataBlob.SerialNumber, 0, kBXDataBlob.SerialNumber.length);
        n2 += kBXDataBlob.SerialNumber.length;
        kBXDataBlob.NumberOfUserIDs = (short)(inputStream.read() << 8 | inputStream.read());
        n2 += 2;
        kBXDataBlob.UserIDSize = (short)(inputStream.read() << 8 | inputStream.read());
        n2 += 2;
        kBXDataBlob.userIds = new KBXUserID[kBXDataBlob.NumberOfUserIDs];
        for (n = 0; n < kBXDataBlob.NumberOfUserIDs; ++n) {
            kBXDataBlob.userIds[n] = new KBXUserID(inputStream);
            n2 += 12;
        }
        kBXDataBlob.NumberOfSignatures = (short)(inputStream.read() << 8 | inputStream.read());
        n2 += 2;
        kBXDataBlob.SizeOfSignature = (short)(inputStream.read() << 8 | inputStream.read());
        n2 += 2;
        kBXDataBlob.SignatureExpirations = new int[kBXDataBlob.NumberOfSignatures];
        for (n = 0; n < kBXDataBlob.NumberOfSignatures; ++n) {
            kBXDataBlob.SignatureExpirations[n] = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
            n2 += 4;
        }
        kBXDataBlob.Ownertrust = (byte)inputStream.read();
        ++n2;
        kBXDataBlob.AllValidity = (byte)inputStream.read();
        ++n2;
        kBXDataBlob.Reserved1 = (short)(inputStream.read() << 8 | inputStream.read());
        n2 += 2;
        kBXDataBlob.RecheckAfter = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
        n2 += 4;
        kBXDataBlob.LatestTimestamp = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
        n2 += 4;
        kBXDataBlob.BlobCreatedAt = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
        n2 += 4;
        kBXDataBlob.ReservedSpacelength = inputStream.read() << 24 | inputStream.read() << 16 | inputStream.read() << 8 | inputStream.read();
        n2 += 4;
        kBXDataBlob.ReservedSpace = new byte[kBXDataBlob.ReservedSpacelength];
        if (kBXDataBlob.ReservedSpacelength > 0) {
            inputStream.read(kBXDataBlob.ReservedSpace, 0, kBXDataBlob.ReservedSpace.length);
            n2 += kBXDataBlob.ReservedSpace.length;
        }
        kBXDataBlob.Blob = new byte[kBXDataBlob.KeyBlocklength];
        inputStream.read(kBXDataBlob.Blob, 0, kBXDataBlob.Blob.length);
        kBXDataBlob.Sha1 = new byte[20];
        kBXDataBlob.ExtraSpace = new byte[kBXDataBlob.length - (n2 += kBXDataBlob.Blob.length) - kBXDataBlob.Sha1.length];
        if (kBXDataBlob.ExtraSpace.length > 0) {
            inputStream.read(kBXDataBlob.ExtraSpace, 0, kBXDataBlob.ExtraSpace.length);
            n2 += kBXDataBlob.ExtraSpace.length;
        }
        inputStream.read(kBXDataBlob.Sha1, 0, kBXDataBlob.Sha1.length);
        return kBXDataBlob;
    }
}

