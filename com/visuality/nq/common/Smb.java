/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

public final class Smb {
    public static final short DIALECT_100 = 256;
    public static final short DIALECT_202 = 514;
    public static final short DIALECT_ANY = 767;
    public static final short DIALECT_210 = 528;
    public static final short DIALECT_300 = 768;
    public static final short DIALECT_302 = 770;
    public static final short DIALECT_311 = 785;
    public static final short DIALECT_ILLEGALSMBREVISION = -1;
    public static final short CAP_NONE = 0;
    public static final short CAP_MESSAGESIGNING = 1;
    public static final short CAP_DFS = 2;
    public static final short CAP_INFOPASSTHRU = 4;
    public static final short CAP_LARGEMTU = 8;
    public static final int SMB_SIGNINGKEY_LENGTH = 16;
    public static final int SMB_SESSIONKEY_LENGTH = 16;
    public static final int SMB_SERVERGUID_LENGTH = 16;
    public static final int SMB_DESIREDACCESS_NOCHANGE = 0;
    public static final int SMB_DESIREDACCESS_READDATA = 1;
    public static final int SMB_DESIREDACCESS_WRITEDATA = 2;
    public static final int SMB_DESIREDACCESS_APPENDDATA = 4;
    public static final int SMB_DESIREDACCESS_READEA = 8;
    public static final int SMB_DESIREDACCESS_WRITEEA = 16;
    public static final int SMB_DESIREDACCESS_EXECUTE = 32;
    public static final int SMB_DESIREDACCESS_DELETECHILD = 64;
    public static final int SMB_DESIREDACCESS_READATTRIBUTES = 128;
    public static final int SMB_DESIREDACCESS_WRITEATTRIBUTES = 256;
    public static final int SMB_DESIREDACCESS_GENREAD = Integer.MIN_VALUE;
    public static final int SMB_DESIREDACCESS_GENWRITE = 0x40000000;
    public static final int SMB_DESIREDACCESS_GENEXECUTE = 0x20000000;
    public static final int SMB_DESIREDACCESS_GENALL = 0x10000000;
    public static final int SMB_DESIREDACCESS_GENMAXIMUMALLOWED = 0x2000000;
    public static final int SMB_DESIREDACCESS_GENSYSTEMSECURITY = 0x1000000;
    public static final int SMB_DESIREDACCESS_SYNCHRONISE = 0x100000;
    public static final int SMB_DESIREDACCESS_WRITEOWNER = 524288;
    public static final int SMB_DESIREDACCESS_WRITEDAC = 262144;
    public static final int SMB_DESIREDACCESS_READCONTROL = 131072;
    public static final int SMB_DESIREDACCESS_DELETE = 65536;
    public static final int SMB_DESIREDACCESS_PRINTERUSE = 8;
    public static final int SMB_DESIREDACCESS_PRINTERADMIN = 4;
    public static final int SMB_DESIREDACCESS_JOBASSIGNPROCESS = 1;
    public static final int SMB_DESIREDACCESS_JOBSETATTRIBUTES = 2;
    public static final int SMB_DESIREDACCESS_JOBQUERY = 4;
    public static final int SMB_DESIREDACCESS_JOBTERMINATE = 8;
    public static final int SMB_DESIREDACCESS_JOBSETSECURITY = 16;
    public static final int SMB_DESIREDACCESS_GENMASK = -268435456;
    public static final int SMB_SHAREACCESS_NONE = 0;
    public static final int SMB_SHAREACCESS_READ = 1;
    public static final int SMB_SHAREACCESS_WRITE = 2;
    public static final int SMB_SHAREACCESS_DELETE = 4;
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
    public static final int SMB2_CHANNEL_INFO_LEN = 16;
    public static final int PENDING_TIMEOUT_EXTENTION = 2;
    public static final int NQ_FAIL = -1;
    public static final int IOCTL_GET_REFERRALS = 393620;
    public static final int IOCTL_GET_REFERRALS_EX = 393648;
    public static final int IOCTL_GET_OBJECTID = 590016;
    public static final int IOCTL_PIPE_PEEK = 1130508;
    public static final int IOCTL_PIPE_WAIT = 0x110018;
    public static final int IOCTL_PIPE_TRANCEIVE = 1163287;
    public static final int IOCTL_QUERY_NETWORK_INTERFACEINFO = 1311228;
    public static final int IOCTL_SRV_RESUME_KEY = 1310840;
    public static final int IOCTL_SRV_COPYCHUNK = 1327346;
    public static final int IOCTL_SRV_COPYCHUNK_WRITE = 1343730;
    public static final int IOCTL_SRV_ENUM_SNEPSHOTS = 1327204;
    public static final int IOCTL_SRV_READ_HASH = 0x1441BB;
    public static final int IOCTL_REQUEST_RESILIENCY = 1311188;
    public static final int IOCTL_SET_REPARSE_POINT = 589988;
    public static final int IOCTL_FILE_LEVEL_TRIM = 623112;
    public static final int IOCTL_VALIDATE_NEGOTIATE = 1311236;
    public static final int IOCTL_OFFLOAD_READ = 606820;
    public static final int IOCTL_OFFLOAD_WRITE = 623208;
    public static final int IOCTL_COMPRESSION = 639040;
    public static final int IOCTL_SET_INTEGRITY_INFORMATION = 639616;
    public static final int IOCTL_GET_INTEGRITY_INFORMATION = 590460;
    public static final int IOCTL_DUPLICATE_EXTENTSTOFILE = 623428;
    public static final int IOCTL_SET_ZERO_DATA = 622792;
    public static final int STATUS_DISCONNECT = -536870911;
    public static final int STATUS_CUSTOM_ERROR_RESPONSE = -536870910;
    public static final int STATUS_INTERNAL_BUFFER_TOO_SMALL = -536870908;
    public static final int STATUS_DONOTRELEASERESPONSE = -536870907;

    private Smb() {
    }
}
