/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.sftp;

public class SftpStatusException
extends Exception {
    private static final long serialVersionUID = 3611692615088253193L;
    public static final int SSH_FX_OK = 0;
    public static final int SSH_FX_EOF = 1;
    public static final int SSH_FX_NO_SUCH_FILE = 2;
    public static final int SSH_FX_PERMISSION_DENIED = 3;
    public static final int SSH_FX_FAILURE = 4;
    public static final int SSH_FX_BAD_MESSAGE = 5;
    public static final int SSH_FX_NO_CONNECTION = 6;
    public static final int SSH_FX_CONNECTION_LOST = 7;
    public static final int SSH_FX_OP_UNSUPPORTED = 8;
    public static final int SSH_FX_INVALID_HANDLE = 9;
    public static final int SSH_FX_NO_SUCH_PATH = 10;
    public static final int SSH_FX_FILE_ALREADY_EXISTS = 11;
    public static final int SSH_FX_WRITE_PROTECT = 12;
    public static final int SSH_FX_NO_MEDIA = 13;
    public static final int SSH_FX_NO_SPACE_ON_FILESYSTEM = 14;
    public static final int SSH_FX_QUOTA_EXCEEDED = 15;
    public static final int SSH_FX_UNKNOWN_PRINCIPAL = 16;
    public static final int SSH_FX_LOCK_CONFLICT = 17;
    public static final int SSH_FX_DIR_NOT_EMPTY = 18;
    public static final int SSH_FX_NOT_A_DIRECTORY = 19;
    public static final int SSH_FX_INVALID_FILENAME = 20;
    public static final int SSH_FX_LINK_LOOP = 21;
    public static final int SSH_FX_CANNOT_DELETE = 22;
    public static final int SSH_FX_INVALID_PARAMETER = 23;
    public static final int SSH_FX_FILE_IS_A_DIRECTORY = 24;
    public static final int SSH_FX_BYTE_RANGE_LOCK_CONFLICT = 25;
    public static final int SSH_FX_BYTE_RANGE_LOCK_REFUSED = 26;
    public static final int SSH_FX_DELETE_PENDING = 27;
    public static final int SSH_FX_FILE_CORRUPT = 28;
    public static final int SSH_FX_OWNER_INVALID = 29;
    public static final int SSH_FX_GROUP_INVALID = 30;
    public static final int SSH_FX_NO_MATCHING_BYTE_RANGE_LOCK = 31;
    public static final int INVALID_HANDLE = 100;
    public static final int INVALID_RESUME_STATE = 101;
    public static final int INVALID_TEXT_MODE = 102;
    public static final int ATTRIBUTE_BITS_NOT_AVAILABLE = 9999;
    public static final int BAD_API_USAGE = Integer.MAX_VALUE;
    int status;

    public SftpStatusException(int status, String msg) {
        super(SftpStatusException.getStatusText(status) + (SftpStatusException.getStatusText(status).equalsIgnoreCase(msg) ? "" : ": " + msg));
        this.status = status;
    }

    public SftpStatusException(int status) {
        this(status, SftpStatusException.getStatusText(status));
    }

    public int getStatus() {
        return this.status;
    }

    public static String getStatusText(int status) {
        switch (status) {
            case 0: {
                return "OK";
            }
            case 1: {
                return "EOF";
            }
            case 2: {
                return "No such file";
            }
            case 3: {
                return "Permission denied";
            }
            case 4: {
                return "Server responded with an unknown failure";
            }
            case 5: {
                return "Server responded to a bad message";
            }
            case 6: {
                return "No connection available";
            }
            case 7: {
                return "Connection lost";
            }
            case 8: {
                return "The operation is unsupported";
            }
            case 9: 
            case 100: {
                return "Invalid file handle";
            }
            case 10: {
                return "No such path";
            }
            case 11: {
                return "File already exists";
            }
            case 12: {
                return "Write protect error";
            }
            case 13: {
                return "No media at location";
            }
            case 14: {
                return "No space on filesystem";
            }
            case 15: {
                return "Quota exceeded";
            }
            case 16: {
                return "Unknown principal";
            }
            case 17: {
                return "Lock conflict";
            }
            case 18: {
                return "Dir not empty";
            }
            case 19: {
                return "Not a directory";
            }
            case 20: {
                return "Invalid filename";
            }
            case 21: {
                return "Link loop";
            }
            case 22: {
                return "Cannot delete";
            }
            case 23: {
                return "Invalid parameter";
            }
            case 24: {
                return "File is a directory";
            }
            case 25: {
                return "Byte range lock conflict";
            }
            case 26: {
                return "Byte range lock refused";
            }
            case 27: {
                return "Delete pending";
            }
            case 28: {
                return "File corrupt";
            }
            case 29: {
                return "Owner invalid";
            }
            case 30: {
                return "Group invalid";
            }
            case 31: {
                return "No matching byte range lock";
            }
            case 101: {
                return "Invalid resume state";
            }
            case 9999: {
                return "Attribute bits field is not available. Did you check hasAttributeBits()?";
            }
            case 0x7FFFFFFF: {
                return "Bad API usage";
            }
        }
        return "Unknown status type " + String.valueOf(status);
    }
}

