/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.share;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2FileId;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.ProgressListener;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.io.ByteChunkProvider;
import com.hierynomus.smbj.io.InputStreamByteChunkProvider;
import com.hierynomus.smbj.share.SMB2Writer;
import com.hierynomus.smbj.share.Share;
import com.hierynomus.smbj.share.TreeConnect;
import java.io.InputStream;
import java.util.EnumSet;

public class PrinterShare
extends Share {
    public PrinterShare(SmbPath smbPath, TreeConnect treeConnect) {
        super(smbPath, treeConnect);
    }

    public void print(InputStream inputStream) {
        this.print(inputStream, null);
    }

    public void print(InputStream inputStream, ProgressListener progressListener) {
        this.print(new InputStreamByteChunkProvider(inputStream), progressListener);
    }

    public void print(ByteChunkProvider provider) {
        this.print(provider, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void print(ByteChunkProvider provider, ProgressListener progressListener) {
        SMB2FileId fileId = this.openFileId(this.smbPath, null, EnumSet.of(AccessMask.FILE_WRITE_DATA), EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL), EnumSet.of(SMB2ShareAccess.FILE_SHARE_WRITE), SMB2CreateDisposition.FILE_CREATE, EnumSet.of(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE, SMB2CreateOptions.FILE_WRITE_THROUGH));
        try {
            new SMB2Writer(this, fileId, this.getSmbPath().toString()).write(provider, progressListener);
        }
        finally {
            this.closeFileId(fileId);
        }
    }
}

