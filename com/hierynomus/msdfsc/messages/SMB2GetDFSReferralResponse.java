/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msdfsc.messages;

import com.hierynomus.msdfsc.messages.DFSReferral;
import com.hierynomus.protocol.commons.EnumWithValue;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.smb.SMBBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class SMB2GetDFSReferralResponse {
    private String originalPath;
    private int pathConsumed;
    private EnumSet<ReferralHeaderFlags> referralHeaderFlags;
    private List<DFSReferral> referralEntries = new ArrayList<DFSReferral>();

    public SMB2GetDFSReferralResponse(String originalPath) {
        this.originalPath = originalPath;
    }

    SMB2GetDFSReferralResponse(String originalPath, int pathConsumed, EnumSet<ReferralHeaderFlags> referralHeaderFlags, List<DFSReferral> referralEntries) {
        this.originalPath = originalPath;
        this.pathConsumed = pathConsumed;
        this.referralHeaderFlags = referralHeaderFlags;
        this.referralEntries = referralEntries;
    }

    public Set<ReferralHeaderFlags> getReferralHeaderFlags() {
        return this.referralHeaderFlags;
    }

    public void read(SMBBuffer buffer) throws Buffer.BufferException {
        this.pathConsumed = buffer.readUInt16();
        int numberOfReferrals = buffer.readUInt16();
        this.referralHeaderFlags = EnumWithValue.EnumUtils.toEnumSet(buffer.readUInt32AsInt(), ReferralHeaderFlags.class);
        for (int i = 0; i < numberOfReferrals; ++i) {
            DFSReferral ref = DFSReferral.factory(buffer);
            if (ref.getDfsPath() == null) {
                ref.setDfsPath(this.originalPath);
            }
            this.referralEntries.add(ref);
        }
    }

    public void writeTo(SMBBuffer buffer) {
        buffer.putUInt16(this.pathConsumed);
        buffer.putUInt16(this.referralEntries.size());
        buffer.putUInt32(EnumWithValue.EnumUtils.toLong(this.referralHeaderFlags));
        int entriesEndIndex = buffer.wpos();
        for (DFSReferral referralEntry : this.referralEntries) {
            entriesEndIndex += referralEntry.determineSize();
        }
        int entryDataOffset = 0;
        for (DFSReferral referralEntry : this.referralEntries) {
            entryDataOffset = referralEntry.writeTo(buffer, entriesEndIndex + entryDataOffset);
        }
        for (DFSReferral referralEntry : this.referralEntries) {
            referralEntry.writeOffsettedData(buffer);
        }
    }

    public List<DFSReferral> getReferralEntries() {
        return this.referralEntries;
    }

    public int getPathConsumed() {
        return this.pathConsumed;
    }

    public int getVersionNumber() {
        if (!this.referralEntries.isEmpty()) {
            return this.referralEntries.get(0).getVersionNumber();
        }
        return 0;
    }

    public static enum ReferralHeaderFlags implements EnumWithValue<ReferralHeaderFlags>
    {
        ReferralServers(1L),
        StorageServers(2L),
        TargetFailback(4L);

        private long value;

        private ReferralHeaderFlags(long value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return this.value;
        }
    }
}

