/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.sftp;

import com.maverick.sftp.ACL;
import com.maverick.sftp.SftpStatusException;
import com.maverick.sftp.SftpSubsystemChannel;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import com.maverick.util.UnsignedInteger32;
import com.maverick.util.UnsignedInteger64;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SftpFileAttributes {
    static final long SSH_FILEXFER_ATTR_SIZE = 1L;
    static final long SSH_FILEXFER_ATTR_PERMISSIONS = 4L;
    static final long SSH_FILEXFER_ATTR_ACCESSTIME = 8L;
    static final long SSH_FILEXFER_ATTR_CREATETIME = 16L;
    static final long SSH_FILEXFER_ATTR_MODIFYTIME = 32L;
    static final long SSH_FILEXFER_ATTR_ACL = 64L;
    static final long SSH_FILEXFER_ATTR_OWNERGROUP = 128L;
    static final long SSH_FILEXFER_ATTR_SUBSECOND_TIMES = 256L;
    static final long SSH_FILEXFER_ATTR_BITS = 512L;
    static final long SSH_FILEXFER_ATTR_ALLOCATION_SIZE = 1024L;
    static final long SSH_FILEXFER_ATTR_TEXT_HINT = 2048L;
    static final long SSH_FILEXFER_ATTR_MIME_TYPE = 4096L;
    static final long SSH_FILEXFER_ATTR_LINK_COUNT = 8192L;
    static final long SSH_FILEXFER_ATTR_UNTRANSLATED = 16384L;
    static final long SSH_FILEXFER_ATTR_CTIME = 32768L;
    static final long SSH_FILEXFER_ATTR_EXTENDED = Integer.MIN_VALUE;
    static final long SSH_FILEXFER_ATTR_UIDGID = 2L;
    public static final int SSH_FILEXFER_TYPE_REGULAR = 1;
    public static final int SSH_FILEXFER_TYPE_DIRECTORY = 2;
    public static final int SSH_FILEXFER_TYPE_SYMLINK = 3;
    public static final int SSH_FILEXFER_TYPE_SPECIAL = 4;
    public static final int SSH_FILEXFER_TYPE_UNKNOWN = 5;
    public static final int SSH_FILEXFER_TYPE_SOCKET = 6;
    public static final int SSH_FILEXFER_TYPE_CHAR_DEVICE = 7;
    public static final int SSH_FILEXFER_TYPE_BLOCK_DEVICE = 8;
    public static final int SSH_FILEXFER_TYPE_FIFO = 9;
    public static final int SSH_FILEXFER_ATTR_FLAGS_READONLY = 1;
    public static final int SSH_FILEXFER_ATTR_FLAGS_SYSTEM = 2;
    public static final int SSH_FILEXFER_ATTR_FLAGS_HIDDEN = 4;
    public static final int SSH_FILEXFER_ATTR_FLAGS_CASE_INSENSITIVE = 8;
    public static final int SSH_FILEXFER_ATTR_FLAGS_ARCHIVE = 16;
    public static final int SSH_FILEXFER_ATTR_FLAGS_ENCRYPTED = 32;
    public static final int SSH_FILEXFER_ATTR_FLAGS_COMPRESSED = 64;
    public static final int SSH_FILEXFER_ATTR_FLAGS_SPARSE = 128;
    public static final int SSH_FILEXFER_ATTR_FLAGS_APPEND_ONLY = 256;
    public static final int SSH_FILEXFER_ATTR_FLAGS_IMMUTABLE = 512;
    public static final int SSH_FILEXFER_ATTR_FLAGS_SYNC = 1024;
    public static final int SSH_FILEXFER_ATTR_FLAGS_TRANSLATION_ERR = 2048;
    public static final int SFX_ACL_CONTROL_INCLUDED = 1;
    public static final int SFX_ACL_CONTROL_PRESENT = 2;
    public static final int SFX_ACL_CONTROL_INHERITED = 4;
    public static final int SFX_ACL_AUDIT_ALARM_INCLUDED = 16;
    public static final int SFX_ACL_AUDIT_ALARM_INHERITED = 32;
    public static final int SSH_FILEXFER_ATTR_KNOWN_TEXT = 0;
    public static final int SSH_FILEXFER_ATTR_GUESSED_TEXT = 1;
    public static final int SSH_FILEXFER_ATTR_KNOWN_BINARY = 2;
    public static final int SSH_FILEXFER_ATTR_GUESSED_BINARY = 0;
    public static final int S_IFMT = 61440;
    public static final int S_IFSOCK = 49152;
    public static final int S_IFLNK = 40960;
    public static final int S_IFREG = 32768;
    public static final int S_IFBLK = 24576;
    public static final int S_IFDIR = 16384;
    public static final int S_IFCHR = 8192;
    public static final int S_IFIFO = 4096;
    public static final int S_ISUID = 2048;
    public static final int S_ISGID = 1024;
    public static final int S_IRUSR = 256;
    public static final int S_IWUSR = 128;
    public static final int S_IXUSR = 64;
    public static final int S_IRGRP = 32;
    public static final int S_IWGRP = 16;
    public static final int S_IXGRP = 8;
    public static final int S_IROTH = 4;
    public static final int S_IWOTH = 2;
    public static final int S_IXOTH = 1;
    int version = 5;
    long flags = 0L;
    int type;
    UnsignedInteger64 size = null;
    UnsignedInteger64 allocationSize = null;
    String uid = null;
    String gid = null;
    UnsignedInteger32 permissions = null;
    UnsignedInteger64 atime = null;
    UnsignedInteger32 atime_nano = null;
    UnsignedInteger64 createtime = null;
    UnsignedInteger32 createtime_nano = null;
    UnsignedInteger64 mtime = null;
    UnsignedInteger32 mtime_nano = null;
    UnsignedInteger64 ctime = null;
    UnsignedInteger32 ctime_nano = null;
    UnsignedInteger32 attributeBits;
    UnsignedInteger32 attributeBitsValid;
    byte textHint;
    String mimeType;
    UnsignedInteger32 linkCount;
    String untralsatedName;
    UnsignedInteger32 aclFlags = null;
    private Vector<ACL> acls = new Vector();
    private Map<String, byte[]> extendedAttributes = new HashMap<String, byte[]>();
    String username;
    String group;
    char[] types = new char[]{'p', 'c', 'd', 'b', '-', 'l', 's'};
    String charsetEncoding;
    Long supportedAttributeMask;
    Long supportedAttributeBits;

    public SftpFileAttributes(SftpSubsystemChannel sftp, int type) {
        this.version = sftp.getVersion();
        this.supportedAttributeBits = sftp.supportedAttributeBits;
        this.supportedAttributeMask = sftp.supportedAttributeMask;
        this.charsetEncoding = sftp.getCharsetEncoding();
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public SftpFileAttributes(SftpSubsystemChannel sftp, ByteArrayReader bar) throws IOException {
        this(bar, sftp.getVersion(), sftp.supportedAttributeBits, sftp.supportedAttributeMask, sftp.getCharsetEncoding());
    }

    public SftpFileAttributes(ByteArrayReader bar, int version, Long supportedAttributeBits, Long supportedAttributeMask, String charsetEncoding) throws IOException {
        byte[] raw;
        this.version = version;
        this.supportedAttributeBits = supportedAttributeBits;
        this.supportedAttributeMask = supportedAttributeMask;
        this.charsetEncoding = charsetEncoding;
        if (bar.available() >= 4) {
            this.flags = bar.readInt();
        }
        if (version > 3 && bar.available() > 0) {
            this.type = bar.read();
        }
        if (this.isFlagSet(1L) && bar.available() >= 8) {
            raw = new byte[8];
            bar.read(raw);
            this.size = new UnsignedInteger64(raw);
        }
        if (this.isFlagSet(1024L) && bar.available() >= 8) {
            raw = new byte[8];
            bar.read(raw);
            this.allocationSize = new UnsignedInteger64(raw);
        }
        if (version <= 3 && this.isFlagSet(2L) && bar.available() >= 8) {
            this.uid = String.valueOf(bar.readInt());
            this.gid = String.valueOf(bar.readInt());
        } else if (version > 3 && this.isFlagSet(128L) && bar.available() > 0) {
            this.uid = bar.readString(charsetEncoding);
            this.gid = bar.readString(charsetEncoding);
        }
        if (this.isFlagSet(4L) && bar.available() >= 4) {
            int ifmt;
            this.permissions = new UnsignedInteger32(bar.readInt());
            if (version <= 3 && (ifmt = (int)this.permissions.longValue() & 0xF000) > 0) {
                this.type = ifmt == 32768 ? 1 : (ifmt == 40960 ? 3 : (ifmt == 8192 ? 7 : (ifmt == 24576 ? 8 : (ifmt == 16384 ? 2 : (ifmt == 4096 ? 9 : (ifmt == 49152 ? 6 : (ifmt == 61440 ? 4 : 5)))))));
            }
        }
        if (this.type == 0) {
            this.type = 5;
        }
        if (version <= 3 && this.isFlagSet(8L) && bar.available() >= 8) {
            this.atime = new UnsignedInteger64(bar.readInt());
            this.mtime = new UnsignedInteger64(bar.readInt());
        } else if (version > 3 && bar.available() > 0 && this.isFlagSet(8L) && bar.available() >= 8) {
            this.atime = bar.readUINT64();
            if (this.isFlagSet(256L) && bar.available() >= 4) {
                this.atime_nano = bar.readUINT32();
            }
        }
        if (version > 3 && bar.available() > 0 && this.isFlagSet(16L) && bar.available() >= 8) {
            this.createtime = bar.readUINT64();
            if (this.isFlagSet(256L) && bar.available() >= 4) {
                this.createtime_nano = bar.readUINT32();
            }
        }
        if (version > 3 && bar.available() > 0 && this.isFlagSet(32L) && bar.available() >= 8) {
            this.mtime = bar.readUINT64();
            if (this.isFlagSet(256L) && bar.available() >= 4) {
                this.mtime_nano = bar.readUINT32();
            }
        }
        if (version >= 6 && bar.available() > 0 && this.isFlagSet(32768L) && bar.available() >= 8) {
            this.ctime = bar.readUINT64();
            if (this.isFlagSet(256L) && bar.available() >= 4) {
                this.ctime_nano = bar.readUINT32();
            }
        }
        if (version > 3 && this.isFlagSet(64L) && bar.available() >= 4) {
            int length;
            if (version >= 6 && bar.available() >= 4) {
                this.aclFlags = bar.readUINT32();
            }
            if ((length = (int)bar.readInt()) > 0 && bar.available() >= length) {
                int count = (int)bar.readInt();
                for (int i = 0; i < count; ++i) {
                    this.acls.addElement(new ACL((int)bar.readInt(), (int)bar.readInt(), (int)bar.readInt(), bar.readString()));
                }
            }
        }
        if (version >= 5 && this.isFlagSet(512L) && bar.available() >= 4) {
            this.attributeBits = bar.readUINT32();
        }
        if (version >= 6) {
            if (this.isFlagSet(512L) && bar.available() >= 4) {
                this.attributeBitsValid = bar.readUINT32();
            }
            if (this.isFlagSet(2048L) && bar.available() >= 1) {
                this.textHint = (byte)bar.read();
            }
            if (this.isFlagSet(4096L) && bar.available() >= 4) {
                this.mimeType = bar.readString();
            }
            if (this.isFlagSet(8192L) && bar.available() >= 4) {
                this.linkCount = bar.readUINT32();
            }
            if (this.isFlagSet(16384L) && bar.available() >= 4) {
                this.untralsatedName = bar.readString();
            }
        }
        if (version >= 3 && this.isFlagSet(Integer.MIN_VALUE) && bar.available() >= 4) {
            int count = (int)bar.readInt();
            for (int i = 0; i < count; ++i) {
                this.extendedAttributes.put(bar.readString(), bar.readBinaryString());
            }
        }
    }

    public String getUID() {
        if (this.username != null) {
            return this.username;
        }
        if (this.uid != null) {
            return this.uid;
        }
        return "";
    }

    public void setUID(String uid) {
        this.flags = this.version > 3 ? (this.flags |= 0x80L) : (this.flags |= 2L);
        this.uid = uid;
    }

    public void setGID(String gid) {
        this.flags = this.version > 3 ? (this.flags |= 0x80L) : (this.flags |= 2L);
        this.gid = gid;
    }

    public String getGID() {
        if (this.group != null) {
            return this.group;
        }
        if (this.gid != null) {
            return this.gid;
        }
        return "";
    }

    public boolean hasUID() {
        return this.uid != null;
    }

    public boolean hasGID() {
        return this.gid != null;
    }

    public void setSize(UnsignedInteger64 size) {
        this.size = size;
        this.flags = size != null ? (this.flags |= 1L) : (this.flags ^= 1L);
    }

    public UnsignedInteger64 getSize() {
        if (this.size != null) {
            return this.size;
        }
        return new UnsignedInteger64("0");
    }

    public boolean hasSize() {
        return this.size != null;
    }

    public void setPermissions(UnsignedInteger32 permissions) {
        this.permissions = permissions;
        this.flags = permissions != null ? (this.flags |= 4L) : (this.flags ^= 4L);
    }

    public void setPermissionsFromMaskString(String mask) {
        if (mask.length() != 4) {
            throw new IllegalArgumentException("Mask length must be 4");
        }
        try {
            this.setPermissions(new UnsignedInteger32(String.valueOf(Integer.parseInt(mask, 8))));
        }
        catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Mask must be 4 digit octal number.");
        }
    }

    public void setPermissionsFromUmaskString(String umask) {
        if (umask.length() != 4) {
            throw new IllegalArgumentException("umask length must be 4");
        }
        try {
            this.setPermissions(new UnsignedInteger32(String.valueOf(Integer.parseInt(umask, 8) ^ 0x1FF)));
        }
        catch (NumberFormatException ex) {
            throw new IllegalArgumentException("umask must be 4 digit octal number");
        }
    }

    public void setPermissions(String newPermissions) {
        int len;
        int cp = 0;
        if (this.permissions != null) {
            cp |= (this.permissions.longValue() & 0xF000L) == 61440L ? 61440 : 0;
            cp |= (this.permissions.longValue() & 0xC000L) == 49152L ? 49152 : 0;
            cp |= (this.permissions.longValue() & 0xA000L) == 40960L ? 40960 : 0;
            cp |= (this.permissions.longValue() & 0x8000L) == 32768L ? 32768 : 0;
            cp |= (this.permissions.longValue() & 0x6000L) == 24576L ? 24576 : 0;
            cp |= (this.permissions.longValue() & 0x4000L) == 16384L ? 16384 : 0;
            cp |= (this.permissions.longValue() & 0x2000L) == 8192L ? 8192 : 0;
            cp |= (this.permissions.longValue() & 0x1000L) == 4096L ? 4096 : 0;
            cp |= (this.permissions.longValue() & 0x800L) == 2048L ? 2048 : 0;
            cp |= (this.permissions.longValue() & 0x400L) == 1024L ? 1024 : 0;
        }
        if ((len = newPermissions.length()) >= 1) {
            cp |= newPermissions.charAt(0) == 'r' ? 256 : 0;
        }
        if (len >= 2) {
            cp |= newPermissions.charAt(1) == 'w' ? 128 : 0;
        }
        if (len >= 3) {
            cp |= newPermissions.charAt(2) == 'x' ? 64 : 0;
        }
        if (len >= 4) {
            cp |= newPermissions.charAt(3) == 'r' ? 32 : 0;
        }
        if (len >= 5) {
            cp |= newPermissions.charAt(4) == 'w' ? 16 : 0;
        }
        if (len >= 6) {
            cp |= newPermissions.charAt(5) == 'x' ? 8 : 0;
        }
        if (len >= 7) {
            cp |= newPermissions.charAt(6) == 'r' ? 4 : 0;
        }
        if (len >= 8) {
            cp |= newPermissions.charAt(7) == 'w' ? 2 : 0;
        }
        if (len >= 9) {
            cp |= newPermissions.charAt(8) == 'x' ? 1 : 0;
        }
        this.setPermissions(new UnsignedInteger32(cp));
    }

    public UnsignedInteger32 getPermissions() {
        if (this.permissions != null) {
            return this.permissions;
        }
        return new UnsignedInteger32(0L);
    }

    public void setTimes(UnsignedInteger64 atime, UnsignedInteger64 mtime) {
        this.atime = atime;
        this.mtime = mtime;
        this.flags = atime != null ? (this.flags |= 8L) : (this.flags ^= 8L);
    }

    public UnsignedInteger64 getAccessedTime() {
        return this.atime;
    }

    public UnsignedInteger64 getModifiedTime() {
        if (this.mtime != null) {
            return this.mtime;
        }
        return new UnsignedInteger64(0L);
    }

    public Date getModifiedDateTime() {
        long time = 0L;
        if (this.mtime != null) {
            time = this.mtime.longValue() * 1000L;
        }
        if (this.mtime_nano != null) {
            time += this.mtime_nano.longValue() / 1000000L;
        }
        return new Date(time);
    }

    public Date getCreationDateTime() {
        long time = 0L;
        if (this.createtime != null) {
            time = this.createtime.longValue() * 1000L;
        }
        if (this.createtime_nano != null) {
            time += this.createtime_nano.longValue() / 1000000L;
        }
        return new Date(time);
    }

    public Date getAccessedDateTime() {
        long time = 0L;
        if (this.atime != null) {
            time = this.atime.longValue() * 1000L;
        }
        if (this.atime_nano != null) {
            time += this.atime_nano.longValue() / 1000000L;
        }
        return new Date(time);
    }

    public UnsignedInteger64 getCreationTime() {
        if (this.createtime != null) {
            return this.createtime;
        }
        return new UnsignedInteger64(0L);
    }

    public boolean isFlagSet(long flag) {
        if (this.version >= 5 && this.supportedAttributeMask != null) {
            boolean set;
            boolean bl = set = (this.flags & (flag & 0xFFFFFFFFL)) == (flag & 0xFFFFFFFFL);
            if (set) {
                boolean bl2 = (this.supportedAttributeMask & (flag & 0xFFFFFFFFL)) == (flag & 0xFFFFFFFFL);
            }
        }
        return (this.flags & (flag & 0xFFFFFFFFL)) == (flag & 0xFFFFFFFFL);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public byte[] toByteArray() throws IOException {
        try (ByteArrayWriter baw = new ByteArrayWriter();){
            Object object;
            baw.writeInt(this.flags);
            if (this.version > 3) {
                baw.write(this.type);
            }
            if (this.isFlagSet(1L)) {
                baw.write(this.size.toByteArray());
            }
            if (this.version <= 3 && this.isFlagSet(2L)) {
                if (this.uid != null) {
                    try {
                        baw.writeInt(Long.parseLong(this.uid));
                    }
                    catch (NumberFormatException ex) {
                        baw.writeInt(0);
                    }
                } else {
                    baw.writeInt(0);
                }
                if (this.gid != null) {
                    try {
                        baw.writeInt(Long.parseLong(this.gid));
                    }
                    catch (NumberFormatException ex) {
                        baw.writeInt(0);
                    }
                } else {
                    baw.writeInt(0);
                }
            } else if (this.version > 3 && this.isFlagSet(128L)) {
                if (this.uid != null) {
                    baw.writeString(this.uid, this.charsetEncoding);
                } else {
                    baw.writeString("");
                }
                if (this.gid != null) {
                    baw.writeString(this.gid, this.charsetEncoding);
                } else {
                    baw.writeString("");
                }
            }
            if (this.isFlagSet(4L)) {
                baw.writeInt(this.permissions.longValue());
            }
            if (this.version <= 3 && this.isFlagSet(8L)) {
                baw.writeInt(this.atime.longValue());
                baw.writeInt(this.mtime.longValue());
            } else if (this.version > 3) {
                if (this.isFlagSet(8L)) {
                    baw.writeUINT64(this.atime);
                    if (this.isFlagSet(256L)) {
                        baw.writeUINT32(this.atime_nano);
                    }
                }
                if (this.isFlagSet(16L)) {
                    baw.writeUINT64(this.createtime);
                    if (this.isFlagSet(256L)) {
                        baw.writeUINT32(this.createtime_nano);
                    }
                }
                if (this.isFlagSet(32L)) {
                    baw.writeUINT64(this.mtime);
                    if (this.isFlagSet(256L)) {
                        baw.writeUINT32(this.mtime_nano);
                    }
                }
            }
            if (this.isFlagSet(64L)) {
                try (ByteArrayWriter tmp = new ByteArrayWriter();){
                    Enumeration<ACL> e = this.acls.elements();
                    tmp.writeInt(this.acls.size());
                    while (e.hasMoreElements()) {
                        ACL acl = e.nextElement();
                        tmp.writeInt(acl.getType());
                        tmp.writeInt(acl.getFlags());
                        tmp.writeInt(acl.getMask());
                        tmp.writeString(acl.getWho());
                    }
                    baw.writeBinaryString(tmp.toByteArray());
                }
            }
            if (this.version >= 5 && this.isFlagSet(512L)) {
                if (this.attributeBits == null) {
                    baw.writeInt(0);
                } else if (this.supportedAttributeBits == null) {
                    baw.writeInt(this.attributeBits.longValue());
                } else {
                    baw.writeInt(this.attributeBits.longValue() & this.supportedAttributeBits);
                }
            }
            if (this.isFlagSet(Integer.MIN_VALUE)) {
                baw.writeInt(this.extendedAttributes.size());
                object = this.extendedAttributes.keySet().iterator();
                while (object.hasNext()) {
                    String key = (String)object.next();
                    baw.writeString(key);
                    baw.writeBinaryString(this.extendedAttributes.get(key));
                }
            }
            object = baw.toByteArray();
            return object;
        }
    }

    private int octal(int v, int r) {
        return (((v >>>= r) & 4) != 0 ? 4 : 0) + ((v & 2) != 0 ? 2 : 0) + ((v & 1) != 0 ? 1 : 0);
    }

    private String rwxString(int v, int r) {
        String rwx = (((v >>>= r) & 4) != 0 ? "r" : "-") + ((v & 2) != 0 ? "w" : "-");
        rwx = r == 6 && (this.permissions.longValue() & 0x800L) == 2048L || r == 3 && (this.permissions.longValue() & 0x400L) == 1024L ? rwx + ((v & 1) != 0 ? "s" : "S") : rwx + ((v & 1) != 0 ? "x" : "-");
        return rwx;
    }

    public String getPermissionsString() {
        if (this.permissions != null) {
            boolean has_ifmt;
            StringBuffer str = new StringBuffer();
            boolean bl = has_ifmt = ((int)this.permissions.longValue() & 0xF000) > 0;
            if (has_ifmt) {
                str.append(this.types[(int)(this.permissions.longValue() & 0xF000L) >>> 13]);
            } else {
                str.append('-');
            }
            str.append(this.rwxString((int)this.permissions.longValue(), 6));
            str.append(this.rwxString((int)this.permissions.longValue(), 3));
            str.append(this.rwxString((int)this.permissions.longValue(), 0));
            return str.toString();
        }
        return "";
    }

    public String getMaskString() {
        StringBuffer buf = new StringBuffer();
        if (this.permissions != null) {
            int i = (int)this.permissions.longValue();
            buf.append('0');
            buf.append(this.octal(i, 6));
            buf.append(this.octal(i, 3));
            buf.append(this.octal(i, 0));
        } else {
            buf.append("----");
        }
        return buf.toString();
    }

    public boolean isDirectory() {
        if (this.version > 3) {
            return this.type == 2;
        }
        return this.permissions != null && (this.permissions.longValue() & 0x4000L) == 16384L;
    }

    public boolean isFile() {
        if (this.version > 3) {
            return this.type == 1;
        }
        return this.permissions != null && (this.permissions.longValue() & 0x8000L) == 32768L;
    }

    public boolean isLink() {
        if (this.version > 3) {
            return this.type == 3;
        }
        return this.permissions != null && (this.permissions.longValue() & 0xA000L) == 40960L;
    }

    public boolean isFifo() {
        if (this.version > 4) {
            return this.type == 9;
        }
        return this.permissions != null && (this.permissions.longValue() & 0x1000L) == 4096L;
    }

    public boolean isBlock() {
        if (this.version > 4) {
            return this.type == 8;
        }
        return this.permissions != null && (this.permissions.longValue() & 0x6000L) == 24576L;
    }

    public boolean isCharacter() {
        if (this.version > 4) {
            return this.type == 7;
        }
        return this.permissions != null && (this.permissions.longValue() & 0x2000L) == 8192L;
    }

    public boolean isSocket() {
        if (this.version > 4) {
            return this.type == 6;
        }
        return this.permissions != null && (this.permissions.longValue() & 0xC000L) == 49152L;
    }

    void setUsername(String username) {
        this.username = username;
    }

    void setGroup(String group) {
        this.group = group;
    }

    public boolean hasAttributeBits() {
        return this.attributeBits != null;
    }

    private void setAttributeBit(long attributeBit, boolean value) throws SftpStatusException {
        if (!this.hasAttributeBits()) {
            throw new SftpStatusException(9999);
        }
        this.flags |= 0x200L;
        if (value) {
            this.attributeBits = new UnsignedInteger32(this.attributeBits.longValue() | attributeBit);
        } else if ((this.attributeBits.longValue() & attributeBit) == attributeBit) {
            this.attributeBits = new UnsignedInteger32(this.attributeBits.longValue() ^ attributeBit);
        }
    }

    public boolean isAttributeBitSet(long attributeBit) throws SftpStatusException {
        if (!this.hasAttributeBits()) {
            throw new SftpStatusException(9999);
        }
        return (this.attributeBits.longValue() & (attributeBit & 0xFFFFFFFFL)) == (attributeBit & 0xFFFFFFFFL);
    }

    public boolean isReadOnly() throws SftpStatusException {
        return this.isAttributeBitSet(1L);
    }

    public void setReadOnly(boolean value) throws SftpStatusException {
        this.setAttributeBit(1L, value);
    }

    public boolean isSystem() throws SftpStatusException {
        return this.isAttributeBitSet(2L);
    }

    public void setSystem(boolean value) throws SftpStatusException {
        this.setAttributeBit(2L, value);
    }

    public boolean isHidden() throws SftpStatusException {
        return this.isAttributeBitSet(4L);
    }

    public void setHidden(boolean value) throws SftpStatusException {
        this.setAttributeBit(4L, value);
    }

    public boolean isCaseInsensitive() throws SftpStatusException {
        return this.isAttributeBitSet(8L);
    }

    public void setCaseSensitive(boolean value) throws SftpStatusException {
        this.setAttributeBit(8L, value);
    }

    public boolean isArchive() throws SftpStatusException {
        return this.isAttributeBitSet(16L);
    }

    public void setArchive(boolean value) throws SftpStatusException {
        this.setAttributeBit(16L, value);
    }

    public boolean isEncrypted() throws SftpStatusException {
        return this.isAttributeBitSet(32L);
    }

    public void setEncrypted(boolean value) throws SftpStatusException {
        this.setAttributeBit(32L, value);
    }

    public boolean isCompressed() throws SftpStatusException {
        return this.isAttributeBitSet(64L);
    }

    public void setCompressed(boolean value) throws SftpStatusException {
        this.setAttributeBit(64L, value);
    }

    public boolean isSparse() throws SftpStatusException {
        return this.isAttributeBitSet(128L);
    }

    public void setSparse(boolean value) throws SftpStatusException {
        this.setAttributeBit(128L, value);
    }

    public boolean isAppendOnly() throws SftpStatusException {
        return this.isAttributeBitSet(256L);
    }

    public void setAppendOnly(boolean value) throws SftpStatusException {
        this.setAttributeBit(256L, value);
    }

    public boolean isImmutable() throws SftpStatusException {
        return this.isAttributeBitSet(512L);
    }

    public void setImmutable(boolean value) throws SftpStatusException {
        this.setAttributeBit(512L, value);
    }

    public boolean isSync() throws SftpStatusException {
        return this.isAttributeBitSet(1024L);
    }

    public void setSync(boolean value) throws SftpStatusException {
        this.setAttributeBit(1024L, value);
    }

    public void setExtendedAttributes(Map<String, byte[]> attributes) {
        this.flags |= Integer.MIN_VALUE;
        this.extendedAttributes = attributes;
    }

    public void setExtendedAttribute(String attrName, byte[] attrValue) {
        this.flags |= Integer.MIN_VALUE;
        if (this.extendedAttributes == null) {
            this.extendedAttributes = new HashMap<String, byte[]>();
        }
        this.extendedAttributes.put(attrName, attrValue);
    }

    public void removeExtendedAttribute(String attrName) {
        if (this.extendedAttributes != null && this.extendedAttributes.containsKey(attrName)) {
            this.extendedAttributes.remove(attrName);
        }
    }

    public Map<String, byte[]> getExtendedAttributes() {
        return this.extendedAttributes;
    }

    public boolean hasExtendedAttribute(String attrName) {
        return this.extendedAttributes.containsKey(attrName);
    }

    public byte[] getExtendedAttribute(String attrName) {
        if (this.extendedAttributes != null) {
            return this.extendedAttributes.get(attrName);
        }
        return null;
    }
}

