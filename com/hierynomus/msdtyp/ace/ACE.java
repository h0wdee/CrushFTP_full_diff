/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msdtyp.ace;

import com.hierynomus.msdtyp.SID;
import com.hierynomus.msdtyp.ace.AceHeader;
import com.hierynomus.msdtyp.ace.AceType1;
import com.hierynomus.msdtyp.ace.AceType2;
import com.hierynomus.msdtyp.ace.AceType3;
import com.hierynomus.msdtyp.ace.AceType4;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.smb.SMBBuffer;

public abstract class ACE {
    private static final int HEADER_STRUCTURE_SIZE = 4;
    AceHeader aceHeader = new AceHeader();

    ACE(AceHeader header) {
        this.aceHeader = header;
    }

    private ACE() {
    }

    public final void write(SMBBuffer buffer) {
        int startPos = buffer.wpos();
        buffer.wpos(startPos + 4);
        this.writeBody(buffer);
        int endPos = buffer.wpos();
        buffer.wpos(startPos);
        this.aceHeader.writeTo(buffer, endPos - startPos);
        buffer.wpos(endPos);
    }

    abstract void writeBody(SMBBuffer var1);

    public static ACE read(SMBBuffer buffer) throws Buffer.BufferException {
        ACE ace;
        int startPos = buffer.rpos();
        AceHeader header = AceHeader.readFrom(buffer);
        switch (header.getAceType()) {
            case ACCESS_ALLOWED_ACE_TYPE: {
                ace = AceType1.read(header, buffer);
                break;
            }
            case ACCESS_ALLOWED_CALLBACK_ACE_TYPE: {
                ace = AceType3.read(header, buffer, startPos);
                break;
            }
            case ACCESS_ALLOWED_CALLBACK_OBJECT_ACE_TYPE: {
                ace = AceType4.read(header, buffer, startPos);
                break;
            }
            case ACCESS_ALLOWED_OBJECT_ACE_TYPE: {
                ace = AceType2.read(header, buffer, startPos);
                break;
            }
            case ACCESS_DENIED_ACE_TYPE: {
                ace = AceType1.read(header, buffer);
                break;
            }
            case ACCESS_DENIED_CALLBACK_ACE_TYPE: {
                ace = AceType3.read(header, buffer, startPos);
                break;
            }
            case ACCESS_DENIED_CALLBACK_OBJECT_ACE_TYPE: {
                ace = AceType4.read(header, buffer, startPos);
                break;
            }
            case ACCESS_DENIED_OBJECT_ACE_TYPE: {
                ace = AceType2.read(header, buffer, startPos);
                break;
            }
            case SYSTEM_AUDIT_ACE_TYPE: {
                ace = AceType1.read(header, buffer);
                break;
            }
            case SYSTEM_AUDIT_CALLBACK_ACE_TYPE: {
                ace = AceType3.read(header, buffer, startPos);
                break;
            }
            case SYSTEM_AUDIT_CALLBACK_OBJECT_ACE_TYPE: {
                ace = AceType4.read(header, buffer, startPos);
                break;
            }
            case SYSTEM_AUDIT_OBJECT_ACE_TYPE: {
                ace = AceType4.read(header, buffer, startPos);
                break;
            }
            case SYSTEM_MANDATORY_LABEL_ACE_TYPE: {
                ace = AceType1.read(header, buffer);
                break;
            }
            case SYSTEM_RESOURCE_ATTRIBUTE_ACE_TYPE: {
                ace = AceType3.read(header, buffer, startPos);
                break;
            }
            case SYSTEM_SCOPED_POLICY_ID_ACE_TYPE: {
                ace = AceType1.read(header, buffer);
                break;
            }
            default: {
                throw new IllegalStateException("Unknown ACE type: " + header.getAceType());
            }
        }
        return ace;
    }

    public AceHeader getAceHeader() {
        return this.aceHeader;
    }

    public abstract SID getSid();

    public abstract long getAccessMask();
}

