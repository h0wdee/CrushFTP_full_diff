/*
 * Decompiled with CFR 0.152.
 */
package org.apache.tools.tar;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import org.apache.tools.tar.TarConstants;
import org.apache.tools.tar.TarUtils;

public class TarEntry
implements TarConstants {
    private StringBuffer name;
    private int mode;
    private int userId;
    private int groupId;
    private long size;
    private long modTime;
    private byte linkFlag;
    private StringBuffer linkName;
    private StringBuffer magic = new StringBuffer("ustar  ");
    private StringBuffer userName;
    private StringBuffer groupName;
    private int devMajor;
    private int devMinor;
    private File file;
    public static final int MAX_NAMELEN = 31;
    public static final int DEFAULT_DIR_MODE = 16877;
    public static final int DEFAULT_FILE_MODE = 33188;
    public static final int MILLIS_PER_SECOND = 1000;

    private TarEntry() {
        this.name = new StringBuffer();
        this.linkName = new StringBuffer();
        String string = "";
        if (string.length() > 31) {
            string = string.substring(0, 31);
        }
        this.userId = 0;
        this.groupId = 0;
        this.userName = new StringBuffer(string);
        this.groupName = new StringBuffer("");
        this.file = null;
    }

    public TarEntry(String string) {
        this(string, false);
    }

    public TarEntry(String string, boolean bl) {
        this();
        string = TarEntry.normalizeFileName(string, bl);
        boolean bl2 = string.endsWith("/");
        this.devMajor = 0;
        this.devMinor = 0;
        this.name = new StringBuffer(string);
        this.mode = bl2 ? 16877 : 33188;
        this.linkFlag = (byte)(bl2 ? 53 : 48);
        this.userId = 0;
        this.groupId = 0;
        this.size = 0L;
        this.modTime = new Date().getTime() / 1000L;
        this.linkName = new StringBuffer("");
        this.userName = new StringBuffer("");
        this.groupName = new StringBuffer("");
        this.devMajor = 0;
        this.devMinor = 0;
    }

    public TarEntry(String string, String string2) {
        this();
        string = TarEntry.normalizeFileName(this.file.getAbsolutePath(), false);
        boolean bl = string.endsWith("/");
        this.devMajor = 0;
        this.devMinor = 0;
        this.name = new StringBuffer(string);
        this.mode = bl ? 16877 : 33188;
        this.linkFlag = (byte)(bl ? 53 : 48);
        this.userId = 0;
        this.groupId = 0;
        this.size = 0L;
        this.modTime = new Date().getTime() / 1000L;
        this.linkName = new StringBuffer("");
        this.userName = new StringBuffer("");
        this.groupName = new StringBuffer("");
        this.devMajor = 0;
        this.devMinor = 0;
    }

    public TarEntry(String string, byte by) {
        this(string);
        this.linkFlag = by;
        if (by == 76) {
            this.magic = new StringBuffer("ustar  ");
        }
    }

    public TarEntry(File file) {
        this(file, "");
    }

    public TarEntry(File file, String string) {
        this();
        this.file = file;
        String string2 = file.getAbsolutePath().replace(File.separatorChar, '/');
        string2 = string2.substring(string2.indexOf(string));
        if ("".equals(string)) {
            string2 = file.getName();
        }
        String string3 = TarEntry.normalizeFileName(string2, false);
        this.linkName = new StringBuffer("");
        this.name = new StringBuffer(string3);
        if (file.isDirectory()) {
            this.mode = 16877;
            this.linkFlag = (byte)53;
            int n = this.name.length();
            if (n == 0 || this.name.charAt(n - 1) != '/') {
                this.name.append("/");
            }
            this.size = 0L;
        } else {
            this.mode = 33188;
            this.linkFlag = (byte)48;
            this.size = file.length();
        }
        this.modTime = file.lastModified() / 1000L;
        this.devMajor = 0;
        this.devMinor = 0;
    }

    public TarEntry(byte[] byArray) {
        this();
        this.parseTarHeader(byArray);
    }

    public boolean equals(TarEntry tarEntry) {
        return this.getName().equals(tarEntry.getName());
    }

    public boolean equals(Object object) {
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        return this.equals((TarEntry)object);
    }

    public int hashCode() {
        return this.getName().hashCode();
    }

    public boolean isDescendent(TarEntry tarEntry) {
        return tarEntry.getName().startsWith(this.getName());
    }

    public String getName() {
        return this.name.toString();
    }

    public void setName(String string) {
        this.name = new StringBuffer(TarEntry.normalizeFileName(string, false));
    }

    public void setMode(int n) {
        this.mode = n;
    }

    public String getLinkName() {
        return this.linkName.toString();
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int n) {
        this.userId = n;
    }

    public int getGroupId() {
        return this.groupId;
    }

    public void setGroupId(int n) {
        this.groupId = n;
    }

    public String getUserName() {
        return this.userName.toString();
    }

    public void setUserName(String string) {
        this.userName = new StringBuffer(string);
    }

    public String getGroupName() {
        return this.groupName.toString();
    }

    public void setGroupName(String string) {
        this.groupName = new StringBuffer(string);
    }

    public void setIds(int n, int n2) {
        this.setUserId(n);
        this.setGroupId(n2);
    }

    public void setNames(String string, String string2) {
        this.setUserName(string);
        this.setGroupName(string2);
    }

    public void setModTime(long l) {
        this.modTime = l / 1000L;
    }

    public void setModTime(Date date) {
        this.modTime = date.getTime() / 1000L;
    }

    public Date getModTime() {
        return new Date(this.modTime * 1000L);
    }

    public File getFile() {
        return this.file;
    }

    public int getMode() {
        return this.mode;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long l) {
        this.size = l;
    }

    public boolean isGNULongNameEntry() {
        return this.linkFlag == 76 && this.name.toString().equals("././@LongLink");
    }

    public boolean isDirectory() {
        if (this.file != null) {
            return this.file.isDirectory();
        }
        if (this.linkFlag == 53) {
            return true;
        }
        return this.getName().endsWith("/");
    }

    public TarEntry[] getDirectoryEntries() {
        if (this.file == null || !this.file.isDirectory()) {
            return new TarEntry[0];
        }
        String[] stringArray = this.file.list();
        TarEntry[] tarEntryArray = new TarEntry[stringArray.length];
        for (int i = 0; i < stringArray.length; ++i) {
            tarEntryArray[i] = new TarEntry(new File(this.file, stringArray[i]));
        }
        return tarEntryArray;
    }

    public void writeEntryHeader(byte[] byArray) {
        int n = 0;
        n = TarUtils.getNameBytes(this.name, byArray, n, 100);
        n = TarUtils.getOctalBytesUnix(438L, byArray, n, 8);
        n = TarUtils.getOctalBytesUnix(this.userId, byArray, n, 8);
        n = TarUtils.getOctalBytesUnix(this.groupId, byArray, n, 8);
        n = TarUtils.getLongOctalBytesUnix(this.size, byArray, n, 12);
        int n2 = n = TarUtils.getLongOctalBytesUnix(this.modTime, byArray, n, 12);
        for (int i = 0; i < 8; ++i) {
            byArray[n++] = 32;
        }
        byArray[n++] = this.linkFlag;
        n = TarUtils.getNameBytes(this.linkName, byArray, n, 100);
        n = TarUtils.getNameBytes(this.magic, byArray, n, this.magic.length() + 1);
        n = TarUtils.getNameBytes(this.userName, byArray, n, 32);
        n = TarUtils.getNameBytes(this.groupName, byArray, n, 32);
        n = TarUtils.getNameBytes(new StringBuffer(""), byArray, n, 8);
        n = TarUtils.getNameBytes(new StringBuffer(""), byArray, n, 8);
        while (n < byArray.length) {
            byArray[n++] = 0;
        }
        long l = TarUtils.computeCheckSum(byArray);
        TarUtils.getCheckSumOctalBytesUnix(l, byArray, n2, 8);
    }

    public void parseTarHeader(byte[] byArray) {
        int n = 0;
        this.name = TarUtils.parseName(byArray, n, 100);
        this.mode = (int)TarUtils.parseOctal(byArray, n += 100, 8);
        this.userId = (int)TarUtils.parseOctal(byArray, n += 8, 8);
        this.groupId = (int)TarUtils.parseOctal(byArray, n += 8, 8);
        this.size = TarUtils.parseOctal(byArray, n += 8, 12);
        this.modTime = TarUtils.parseOctal(byArray, n += 12, 12);
        n += 12;
        n += 8;
        this.linkFlag = byArray[n++];
        this.linkName = TarUtils.parseName(byArray, n, 100);
        this.magic = TarUtils.parseName(byArray, n += 100, 8);
        this.userName = TarUtils.parseName(byArray, n += 8, 32);
        this.groupName = TarUtils.parseName(byArray, n += 32, 32);
        this.devMajor = (int)TarUtils.parseOctal(byArray, n += 32, 8);
        this.devMinor = (int)TarUtils.parseOctal(byArray, n += 8, 8);
    }

    private static String normalizeFileName(String string, boolean bl) {
        String string2 = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (string2 != null) {
            int n;
            if (string2.startsWith("windows")) {
                if (string.length() > 2) {
                    n = string.charAt(0);
                    char c = string.charAt(1);
                    if (c == ':' && (n >= 97 && n <= 122 || n >= 65 && n <= 90)) {
                        string = string.substring(2);
                    }
                }
            } else if (string2.indexOf("netware") > -1 && (n = string.indexOf(58)) != -1) {
                string = string.substring(n + 1);
            }
        }
        string = string.replace(File.separatorChar, '/');
        while (!bl && string.startsWith("/")) {
            string = string.substring(1);
        }
        return string;
    }
}

