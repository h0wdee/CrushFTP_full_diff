/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msdfsc.messages;

import com.hierynomus.msdfsc.messages.DFSReferral;
import com.hierynomus.protocol.commons.Charsets;
import com.hierynomus.protocol.commons.EnumWithValue;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.smb.SMBBuffer;
import java.util.ArrayList;
import java.util.List;

public class DFSReferralV34
extends DFSReferral {
    private static final int SIZE = 34;

    DFSReferralV34() {
    }

    DFSReferralV34(int version, DFSReferral.ServerType serverType, int referralEntryFlags, int ttl, String dfsPath, String dfsAlternatePath, String path) {
        super(version, serverType, referralEntryFlags);
        this.ttl = ttl;
        this.dfsPath = dfsPath;
        this.dfsAlternatePath = dfsAlternatePath;
        this.path = path;
    }

    DFSReferralV34(int version, DFSReferral.ServerType serverType, int referralEntryFlags, int ttl, String specialName, List<String> expandedNames) {
        super(version, serverType, referralEntryFlags);
        this.ttl = ttl;
        this.specialName = specialName;
        this.expandedNames = expandedNames;
    }

    @Override
    protected void readReferral(SMBBuffer buffer, int referralStartPos) throws Buffer.BufferException {
        this.ttl = buffer.readUInt32AsInt();
        if (!EnumWithValue.EnumUtils.isSet(this.referralEntryFlags, DFSReferral.ReferralEntryFlags.NameListReferral)) {
            this.dfsPath = this.readOffsettedString(buffer, referralStartPos, buffer.readUInt16());
            this.dfsAlternatePath = this.readOffsettedString(buffer, referralStartPos, buffer.readUInt16());
            this.path = this.readOffsettedString(buffer, referralStartPos, buffer.readUInt16());
            buffer.skip(16);
        } else {
            this.specialName = this.readOffsettedString(buffer, referralStartPos, buffer.readUInt16());
            int nrNames = buffer.readUInt16();
            int firstExpandedNameOffset = buffer.readUInt16();
            this.expandedNames = new ArrayList(nrNames);
            int curPos = buffer.rpos();
            buffer.rpos(referralStartPos + firstExpandedNameOffset);
            for (int i = 0; i < nrNames; ++i) {
                this.expandedNames.add(buffer.readNullTerminatedString(Charsets.UTF_16));
            }
            buffer.rpos(curPos);
        }
    }

    @Override
    int writeReferral(SMBBuffer buffer, int entryStartPos, int bufferDataOffset) {
        int offset = bufferDataOffset;
        buffer.putUInt32(this.ttl);
        if (!EnumWithValue.EnumUtils.isSet(this.referralEntryFlags, DFSReferral.ReferralEntryFlags.NameListReferral)) {
            buffer.putUInt16(offset - entryStartPos);
            buffer.putUInt16((offset += (this.dfsPath.length() + 1) * 2) - entryStartPos);
            buffer.putUInt16((offset += (this.dfsAlternatePath.length() + 1) * 2) - entryStartPos);
            offset += (this.path.length() + 1) * 2;
            buffer.putReserved4();
            buffer.putReserved4();
            buffer.putReserved4();
            buffer.putReserved4();
            return offset;
        }
        buffer.putUInt16(offset - entryStartPos);
        buffer.putUInt16(this.expandedNames.size());
        buffer.putUInt16((offset += (this.specialName.length() + 1) * 2) - entryStartPos);
        for (String expandedName : this.expandedNames) {
            offset += (expandedName.length() + 1) * 2;
        }
        return offset;
    }

    @Override
    void writeOffsettedData(SMBBuffer buffer) {
        if (!EnumWithValue.EnumUtils.isSet(this.referralEntryFlags, DFSReferral.ReferralEntryFlags.NameListReferral)) {
            buffer.putNullTerminatedString(this.dfsPath, Charsets.UTF_16);
            buffer.putNullTerminatedString(this.dfsAlternatePath, Charsets.UTF_16);
            buffer.putNullTerminatedString(this.path, Charsets.UTF_16);
        } else {
            buffer.putNullTerminatedString(this.specialName, Charsets.UTF_16);
            for (String expandedName : this.expandedNames) {
                buffer.putNullTerminatedString(expandedName, Charsets.UTF_16);
            }
        }
    }

    @Override
    protected int determineSize() {
        return 34;
    }
}

