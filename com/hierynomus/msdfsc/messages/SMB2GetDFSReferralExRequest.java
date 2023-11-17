/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msdfsc.messages;

import com.hierynomus.smb.SMBBuffer;

public class SMB2GetDFSReferralExRequest {
    private int maxReferralLevel = 0;
    private int requestFlags;
    private String requestFileName;
    private String siteName;

    public SMB2GetDFSReferralExRequest(String path) {
        this.requestFlags = 0;
        this.requestFileName = path;
        this.siteName = null;
    }

    public SMB2GetDFSReferralExRequest(String path, String site) {
        this.requestFlags = RequestFlags.FLAGS_SITENAMEPRESENT.getValue();
        this.requestFileName = path;
        this.siteName = site;
    }

    public void writeTo(SMBBuffer buffer) {
        buffer.putUInt16(this.maxReferralLevel);
        buffer.putUInt16(this.requestFlags);
        if ((this.requestFlags & RequestFlags.FLAGS_SITENAMEPRESENT.getValue()) != 0) {
            buffer.putUInt32((long)(this.requestFileName.length() + 2 + this.siteName.length()) + 2L);
        } else {
            buffer.putUInt32((long)this.requestFileName.length() + 2L);
        }
        buffer.putStringLengthUInt16(this.requestFileName);
        buffer.putString(this.requestFileName);
        if ((this.requestFlags & RequestFlags.FLAGS_SITENAMEPRESENT.getValue()) != 0) {
            buffer.putStringLengthUInt16(this.requestFileName);
            buffer.putString(this.requestFileName);
        }
    }

    static enum RequestFlags {
        FLAGS_SITENAMEPRESENT(1);

        private int value;

        private RequestFlags(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}

