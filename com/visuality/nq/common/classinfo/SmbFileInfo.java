/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common.classinfo;

import com.visuality.nq.client.File;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.Utility;
import java.net.URI;

public class SmbFileInfo {
    public static final String ERR_NOT_SUPPORT = "This JNQ version does not support SmbFile";
    private static final String SMB_FILE = "com.visuality.nq.client.SmbFile";
    private static final Class[] constructorArgs = new Class[]{URI.class};
    private static final String TO_FILE_METHOD = "toFile";
    private static final Class[] toFileArgs = new Class[]{File.Params.class};
    private static final String CLOSE_METHOD = "close";
    private static final Class[] closeArgs = new Class[0];
    private static Utility.ClassInfo smbFileClassInfo = new Utility.ClassInfo("com.visuality.nq.client.SmbFile");
    private static Utility.ConstructorInfo constructorInfo = new Utility.ConstructorInfo(smbFileClassInfo, constructorArgs);
    private static Utility.MethodInfo toFileMethodInfo = new Utility.MethodInfo(smbFileClassInfo, "toFile", toFileArgs);
    private static Utility.MethodInfo closeMethodInfo = new Utility.MethodInfo(smbFileClassInfo, "close", closeArgs);

    public static boolean isSmbFileClassSupported() {
        return smbFileClassInfo.isClassSupported();
    }

    public static Object getNewSmbFileInstance(URI uri) throws NqException {
        return constructorInfo.newInstance(uri);
    }

    public static File smbFileToFile(Object smbFile, File.Params params) throws NqException {
        return (File)toFileMethodInfo.invokeMethod(smbFile, params);
    }

    public static void closeSmbFile(Object smbFile) throws NqException {
        closeMethodInfo.invokeMethod(smbFile, new Object[0]);
    }
}

