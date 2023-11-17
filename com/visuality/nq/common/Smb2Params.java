/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import java.util.HashMap;
import java.util.Map;

public final class Smb2Params {
    public static final String SMB2_DIALECTSTRING = "SMB 2.002";
    public static final short SMB2_DIALECTREVISION = 514;
    public static final String SMB2_1_DIALECTSTRING = "SMB 2.100";
    public static final short SMB2_1_DIALECTREVISION = 528;
    public static final byte[] smb2ProtocolId = new byte[]{-2, 83, 77, 66};
    public static final int SMB2_FLAG_SERVER_TO_REDIR = 1;
    public static final int SMB2_FLAG_ASYNC_COMMAND = 2;
    public static final int SMB2_FLAG_RELATED_OPERATIONS = 4;
    public static final int SMB2_FLAG_SIGNED = 8;
    public static final int SMB2_FLAG_DFS_OPERATIONS = 0x10000000;
    public static final int SMB2_FLAG_REPLAY_OPERATIONS = 0x20000000;
    public static final int SMB2_PID_RESERVED = 65279;
    public static final int SECURITY_SIGNATURE_SIZE = 16;
    public static final int SECURITY_SIGNATURE_OFFSET = 48;
    public static final byte PREAUTH_INTEGRITY_SALT_SIZE = 32;
    public static final int PREAUTH_INTEGRITY_CAPABILITIES = 1;
    public static final int ENCRYPTION_CAPABILITIES = 2;
    public static final int PREAUTH_INTEGRITY_CONTEXT_LEN_BYTES = 38;
    public static final int ENCRYPTION_CCONTEXT_LEN_BYTES = 6;
    public static final byte PLOCK_LEVEL_NONE = 0;
    public static final byte PLOCK_LEVEL_II = 1;
    public static final byte PLOCK_LEVEL_EXCLUSIVE = 8;
    public static final byte PLOCK_LEVEL_BATCH = 9;
    public static final byte PLOCK_LEVEL_LEASE = -1;
    public static final short SMB2_ENCRYPTION_AES128_CCM = 1;
    public static final short SMB2_ENCRYPTION_AES128_GCM = 2;
    public static final int SMB2_ENCRYPTION_HDR_NONCE_SIZE = 16;
    public static final int SMB2_AES128_CCM_NONCE_SIZE = 11;
    public static final int SMB2_AES128_GCM_NONCE_SIZE = 12;
    public static final int SMB2_USE_NEGOTIATED_CIPHER = 1;
    public static final int SMB2_HEADERSIZE = 64;
    public static final int TRANSFORMHEADER_SIZE = 52;
    public static final int HEADERANDSTRUCT_SIZE = 66;
    public static final int SEQNUMBEROFFSET = 28;
    public static final short SMB2_NEGOTIATE_SIGNINGENABLED = 1;
    public static final short SMB2_NEGOTIATE_SIGNINGREQUIRED = 2;
    public static final int SMB2_CAPABILITY_DFS = 1;
    public static final int SMB2_CAPABILITY_LEASING = 2;
    public static final int SMB2_CAPABILITY_LARGE_MTU = 4;
    public static final int SMB2_CAPABILITY_MULTI_CHANNEL = 8;
    public static final int SMB2_CAPABILITY_PERSISTENT_HANDLES = 16;
    public static final int SMB2_CAPABILITY_DIRECTORY_LEASING = 32;
    public static final int SMB2_CAPABILITY_ENCRYPTION = 64;
    public static final int SMB2_0_IOCTL_IS_FSCTL = 1;
    public static final int SMB2_INFO_FILE = 1;
    public static final int SMB2_INFO_FILESYSTEM = 2;
    public static final int SMB2_INFO_SECURITY = 3;
    public static final int SMB2_INFO_QUOTA = 4;
    public static final int MAXINFORESPONSE_SIZE = 4096;
    public static final byte SMB2_FILEINFO_FILE_BOTHDIRECTORY = 3;
    public static final byte SMB2_FILEINFO_ALLINFORMATION = 18;
    public static final byte SMB2_FILEINFO_BASIC = 4;
    public static final byte SMB2_FILEINFO_STANDARD = 5;
    public static final byte SMB2_FILEINFO_INTERNAL = 6;
    public static final byte SMB2_FILEINFO_RENAME = 10;
    public static final byte SMB2_FILEINFO_DISPOSITION = 13;
    public static final byte SMB2_FILEINFO_ALLOCATION = 19;
    public static final byte SMB2_FILEINFO_EOF = 20;
    public static final byte SMB2_FILEINFO_FILEID_BOTHDIRECTORY = 37;
    public static final short SMB2_CMD_NEGOTIATE = 0;
    public static final short SMB2_CMD_SESSIONSETUP = 1;
    public static final short SMB2_CMD_LOGOFF = 2;
    public static final short SMB2_CMD_TREECONNECT = 3;
    public static final short SMB2_CMD_TREEDISCONNECT = 4;
    public static final short SMB2_CMD_CREATE = 5;
    public static final short SMB2_CMD_CLOSE = 6;
    public static final short SMB2_CMD_FLUSH = 7;
    public static final short SMB2_CMD_READ = 8;
    public static final short SMB2_CMD_WRITE = 9;
    public static final short SMB2_CMD_LOCK = 10;
    public static final short SMB2_CMD_IOCTL = 11;
    public static final short SMB2_CMD_CANCEL = 12;
    public static final short SMB2_CMD_ECHO = 13;
    public static final short SMB2_CMD_QUERYDIRECTORY = 14;
    public static final short SMB2_CMD_CHANGENOTIFY = 15;
    public static final short SMB2_CMD_QUERYINFO = 16;
    public static final short SMB2_CMD_SETINFO = 17;
    public static final short SMB2_CMD_OPLOCKBREAK = 18;
    public static final short SMB2_CMD_NOCOMMAND = -1;
    public static final int SMB2_CREATEDISPOSITION_SUPERSEDE = 0;
    public static final int SMB2_CREATEDISPOSITION_OPEN = 1;
    public static final int SMB2_CREATEDISPOSITION_CREATE = 2;
    public static final int SMB2_CREATEDISPOSITION_OPEN_IF = 3;
    public static final int SMB2_CREATEDISPOSITION_OVERWRITE = 4;
    public static final int SMB2_CREATEDISPOSITION_OVERWRITE_IF = 5;
    public static final int SMB2_CREATEOPTIONS_NONE = 0;
    public static final int SMB2_CREATEOPTIONS_DIRECTORY_FILE = 1;
    public static final int SMB2_CREATEOPTIONS_WRITE_THROUGH = 2;
    public static final int SMB2_CREATEOPTIONS_SEQUENTIAL_ONLY = 4;
    public static final int SMB2_CREATEOPTIONS_NO_INTERMEDIATE_BUFFERING = 8;
    public static final int SMB2_CREATEOPTIONS_SYNCHRONOUS_OPERATIONS = 32;
    public static final int SMB2_CREATEOPTIONS_NON_DIRECTORY_FILE = 64;
    public static final int SMB2_CREATEOPTIONS_NO_EA_KNOWLEDGE = 512;
    public static final int SMB2_CREATEOPTIONS_DELETE_ON_CLOSE = 4096;
    public static final int SMB2_CREATEOPTIONS_OPEN_FOR_BACKUP_INTENT = 16384;
    public static final int SMB2_CREATEOPTIONS_RANDOM_ACCESS = 2048;
    public static final int SMB2_CREATEOPTIONS_NO_COMPRESSION = 32768;
    public static final int SMB2_CREATEOPTIONS_OPEN_REPARSE_POINT = 0x200000;
    public static final int SMB2_CREATEOPTIONS_OPEN_NO_RECALL = 0x400000;
    public static final int SMB2_CREATEOPTIONS_MASK = -16777216;
    public static final int SMB2_CREATEOPTIONS_INVALIDMASK = 216071680;
    public static final byte SMB2_FSINFO_VOLUME = 1;
    public static final byte SMB2_FSINFO_SIZE = 3;
    public static final int SMB2_SECINFO_0 = 0;
    public static final int SMB2_SIF_OWNER = 1;
    public static final int SMB2_SIF_GROUP = 2;
    public static final int SMB2_SIF_DACL = 4;
    public static final int SMB2_SIF_SACL = 8;
    public static final short SMB2_QDF_RESTARTSCANS = 1;
    public static final short SMB2_QDF_RETURNSINGLEENTRY = 2;
    public static final short SMB2_QDF_INDEXSPECIFIED = 4;
    public static final short SMB2_QDF_REOPEN = 16;
    public static final short SMB2_ATTRIBUTE_NORMAL = 128;
    public static final short SMB2_SHARE_TYPE_DISK = 1;
    public static final short SMB2_SHARE_TYPE_PIPE = 2;
    public static final short SMB2_SHARE_TYPE_PRINT = 3;
    public static final int SMB2_SHAREACCESS_READ = 1;
    public static final int SMB2_SHAREACCESS_WRITE = 2;
    public static final int SMB2_SHAREACCESS_DELETE = 4;
    public static final int SMB2_SHARE_SRV_TYPE_DISK = 0;
    public static final int SMB2_SHARE_SRV_TYPE_PRINT = 1;
    public static final int SMB2_SHARE_SRV_TYPE_DEVICE = 2;
    public static final int SMB2_SHARE_SRV_TYPE_IPC = 3;
    public static final int SMB2_SHARE_SRV_TYPE_CLUSTER = 0x2000000;
    public static final int SMB2_SHARE_SRV_TYPE_CLUSTER_SCALE = 0x4000000;
    public static final int SMB2_SHARE_SRV_TYPE_CLUSTER_DFS = 0x8000000;
    public static final int SMB2_SHARE_SRV_TYPE_HIDDEN = Integer.MIN_VALUE;
    public static final int SMB2_SHARE_FLAG_MANUAL_CACHING = 0;
    public static final int SMB2_SHARE_FLAG_AUTO_CACHING = 16;
    public static final int SMB2_SHARE_FLAG_VDO_CACHING = 32;
    public static final int SMB2_SHARE_FLAG_NO_CACHING = 48;
    public static final int SMB2_SHARE_FLAG_DFS = 1;
    public static final int SMB2_SHARE_FLAG_DFS_ROOT = 2;
    public static final int SMB2_SHARE_FLAG_RESTRICT_EXCLUSIVE_OPENS = 256;
    public static final int SMB2_SHARE_FLAG_FORCE_SHARED_DELETE = 512;
    public static final int SMB2_SHARE_FLAG_ALLOW_NAMESPACE_CACHING = 1024;
    public static final int SMB2_SHARE_FLAG_ACCESS_BASED_DIRECTORY_ENUM = 2048;
    public static final int SMB2_SHARE_FLAG_ENCRYPT_DATA = 32768;
    public static final int SMB2_SHARE_CAPS_DFS = 8;
    public static final int SMB2_SHARE_CAP_SCALEOUT = 32;
    public static final int SMB2DHANDLE_FLAG_NOTPERSISTENT = 0;
    public static final int SMB2DHANDLE_FLAG_PERSISTENT = 2;
    public static final int SMB2_IMPERSONATION_ANONYMOUS = 0;
    public static final int SMB2_IMPERSONATION_IDENTIFICATION = 1;
    public static final int SMB2_IMPERSONATION_IMPERSONATION = 2;
    public static final int SMB2_IMPERSONATION_DELEGATE = 3;
    public static final int SMB2_CHANNEL_INFO_LEN = 16;
    public static final short SMB2_CLIENT_MAX_CREDITS_TO_REQUEST = 128;
    public static final short SMB2SESSIONFLAG_IS_GUEST = 1;
    public static final short SMB2SESSIONFLAG_IS_ANON = 2;
    public static final short SMB2SESSIONFLAG_ENCRYPT_DATA = 4;
    public static final byte SMB2_OPLOCK_LEVEL_NONE = 0;
    public static final byte SMB2_OPLOCK_LEVEL_II = 1;
    public static final byte SMB2_OPLOCK_LEVEL_EXCLUSIVE = 8;
    public static final byte SMB2_OPLOCK_LEVEL_BATCH = 9;
    public static final byte SMB2_OPLOCK_LEVEL_LEASE = -1;
    public static final int SMB2_READSTRUCTSIZE = 14;
    public static final int SMB2_CHANGENOTIFYSTRUCTSIZE = 6;
    private static short[][] smb2RequestResponseStructureSize = new short[][]{{36, 65}, {25, 9}, {4, 4}, {9, 16}, {4, 4}, {57, 89}, {24, 60}, {24, 4}, {49, 17}, {49, 17}, {0, 0}, {57, 49}, {4, 4}, {4, 4}, {33, 9}, {32, 1}, {41, 9}, {33, 2}, {24, 0}};
    public static short smb2Req = 0;
    public static short smb2Res = 1;

    private Smb2Params() {
    }

    public static short getSmb2StructureSize(short command, int ReqOrRes) {
        short result = smb2RequestResponseStructureSize[command][ReqOrRes];
        return result;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum OplockName {
        LEVEL_NONE("LEVEL_NONE"),
        LEVEL_II("LEVEL_II"),
        LEVEL_EXCLUSIVE("LEVEL_EXCLUSIVE"),
        LEVEL_BATCH("LEVEL_BATCH"),
        LEVEL_LEASE("LEVEL_LEASE");

        private final String text;
        static Map oplockNameMap;

        private OplockName(String text) {
            this.text = text;
        }

        public static OplockName getEnum(byte level) {
            return (OplockName)((Object)oplockNameMap.get(level));
        }

        public String toString() {
            return this.text;
        }

        static {
            oplockNameMap = new HashMap();
            oplockNameMap.put((byte)0, LEVEL_NONE);
            oplockNameMap.put((byte)1, LEVEL_II);
            oplockNameMap.put((byte)8, LEVEL_EXCLUSIVE);
            oplockNameMap.put((byte)9, LEVEL_BATCH);
            oplockNameMap.put((byte)-1, LEVEL_LEASE);
        }
    }
}

