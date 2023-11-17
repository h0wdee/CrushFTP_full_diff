/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.utils;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.io.InputStreamByteChunkProvider;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumSet;

public class SmbFiles {
    public static int copy(java.io.File source, DiskShare share, String destPath, boolean overwrite) throws IOException {
        int r = 0;
        if (source != null && source.exists() && source.canRead() && source.isFile()) {
            Throwable throwable;
            FileInputStream is;
            block11: {
                is = new FileInputStream(source);
                throwable = null;
                try {
                    if (destPath == null || is == null) break block11;
                    try (File f = share.openFile(destPath, EnumSet.of(AccessMask.GENERIC_WRITE), EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL), EnumSet.of(SMB2ShareAccess.FILE_SHARE_WRITE), overwrite ? SMB2CreateDisposition.FILE_OVERWRITE_IF : SMB2CreateDisposition.FILE_CREATE, EnumSet.noneOf(SMB2CreateOptions.class));){
                        r = f.write(new InputStreamByteChunkProvider(is));
                    }
                }
                catch (Throwable throwable2) {
                    try {
                        throwable = throwable2;
                        throw throwable2;
                    }
                    catch (Throwable throwable3) {
                        SmbFiles.$closeResource(throwable, is);
                        throw throwable3;
                    }
                }
            }
            SmbFiles.$closeResource(throwable, is);
        }
        return r;
    }
}

