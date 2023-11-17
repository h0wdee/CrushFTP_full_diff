/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.mssmb2.messages;

import com.hierynomus.msdtyp.SecurityInformation;
import com.hierynomus.msfscc.FileInformationClass;
import com.hierynomus.msfscc.FileSystemInformationClass;
import com.hierynomus.mssmb2.SMB2Dialect;
import com.hierynomus.mssmb2.SMB2FileId;
import com.hierynomus.mssmb2.SMB2MessageCommandCode;
import com.hierynomus.mssmb2.SMB2Packet;
import com.hierynomus.protocol.commons.EnumWithValue;
import com.hierynomus.smb.SMBBuffer;
import java.util.Set;

public class SMB2QueryInfoRequest
extends SMB2Packet {
    private static final long MAX_OUTPUT_BUFFER_LENGTH = 65536L;
    private final SMB2FileId fileId;
    private final SMB2QueryInfoType infoType;
    private final FileInformationClass fileInformationClass;
    private final FileSystemInformationClass fileSystemInformationClass;
    private final byte[] inputBuffer;
    private final Set<SecurityInformation> securityInformation;

    public SMB2QueryInfoRequest(SMB2Dialect smbDialect, long sessionId, long treeId, SMB2FileId fileId, SMB2QueryInfoType infoType, FileInformationClass fileInformationClass, FileSystemInformationClass fileSystemInformationClass, byte[] inputBuffer, Set<SecurityInformation> securityInformation) {
        super(41, smbDialect, SMB2MessageCommandCode.SMB2_QUERY_INFO, sessionId, treeId);
        this.infoType = infoType;
        this.fileInformationClass = fileInformationClass;
        this.fileSystemInformationClass = fileSystemInformationClass;
        this.inputBuffer = inputBuffer;
        this.securityInformation = securityInformation;
        this.fileId = fileId;
    }

    @Override
    protected void writeTo(SMBBuffer buffer) {
        buffer.putUInt16(this.structureSize);
        buffer.putByte((byte)this.infoType.getValue());
        int BUFFER_OFFSET = 104;
        int offset = 0;
        switch (this.infoType) {
            case SMB2_0_INFO_FILE: {
                buffer.putByte((byte)this.fileInformationClass.getValue());
                buffer.putUInt32(65536L);
                if (this.fileInformationClass == FileInformationClass.FileFullEaInformation) {
                    buffer.putUInt16(offset);
                    buffer.putReserved2();
                    buffer.putUInt32(this.inputBuffer.length);
                    offset = BUFFER_OFFSET;
                } else {
                    buffer.putUInt16(0);
                    buffer.putReserved2();
                    buffer.putUInt32(0L);
                }
                buffer.putUInt32(0L);
                buffer.putUInt32(0L);
                this.fileId.write(buffer);
                break;
            }
            case SMB2_0_INFO_FILESYSTEM: {
                buffer.putByte((byte)this.fileSystemInformationClass.getValue());
                buffer.putUInt32(65536L);
                buffer.putUInt16(0);
                buffer.putReserved2();
                buffer.putUInt32(0L);
                buffer.putUInt32(0L);
                buffer.putUInt32(0L);
                this.fileId.write(buffer);
                break;
            }
            case SMB2_0_INFO_SECURITY: {
                buffer.putByte((byte)0);
                buffer.putUInt32(65536L);
                buffer.putUInt16(0);
                buffer.putReserved2();
                buffer.putUInt32(0L);
                buffer.putUInt32(EnumWithValue.EnumUtils.toLong(this.securityInformation));
                buffer.putUInt32(0L);
                this.fileId.write(buffer);
                break;
            }
            case SMB2_0_INFO_QUOTA: {
                buffer.putByte((byte)0);
                buffer.putUInt32(65536L);
                buffer.putUInt16(offset);
                buffer.putReserved2();
                buffer.putUInt32(this.inputBuffer.length);
                buffer.putUInt32(0L);
                buffer.putUInt32(0L);
                this.fileId.write(buffer);
                offset = BUFFER_OFFSET;
                break;
            }
            default: {
                throw new IllegalStateException("Unknown SMB2QueryInfoType: " + this.infoType);
            }
        }
        if (offset > 0) {
            buffer.putRawBytes(this.inputBuffer);
        }
    }

    public static enum SMB2QueryInfoType implements EnumWithValue<SMB2QueryInfoType>
    {
        SMB2_0_INFO_FILE(1L),
        SMB2_0_INFO_FILESYSTEM(2L),
        SMB2_0_INFO_SECURITY(3L),
        SMB2_0_INFO_QUOTA(4L);

        private long value;

        private SMB2QueryInfoType(long value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return this.value;
        }
    }
}

