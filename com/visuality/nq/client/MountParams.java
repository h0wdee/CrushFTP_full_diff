/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.auth.Authentications;

public class MountParams {
    protected static final int NOT_SET = -1;
    private static final int[] VALID_DIALECTS = new int[]{256, 514, 528, 768, 770, 785};
    public int minDialect = -1;
    public int maxDialect = -1;
    public int maxSecurityLevel = Authentications.AM_MAXSECURITYLEVEL;
    public int minSecurityLevel = Authentications.AM_MINSECURITYLEVEL;
    public boolean isReadAccessSufficient = false;
    public boolean enableAccessDeniedUpdater = false;
    public int port = -1;

    protected boolean valid() {
        if (!this.isDialectValid(this.minDialect)) {
            return false;
        }
        if (!this.isDialectValid(this.maxDialect)) {
            return false;
        }
        if (256 == this.maxDialect) {
            return false;
        }
        if (-1 != this.maxDialect && this.minDialect > this.maxDialect) {
            return false;
        }
        if (0 > this.maxSecurityLevel || 4 < this.maxSecurityLevel) {
            return false;
        }
        if (0 > this.minSecurityLevel || 4 < this.minSecurityLevel) {
            return false;
        }
        return this.minSecurityLevel <= this.maxSecurityLevel;
    }

    private boolean isDialectValid(int dialect) {
        if (-1 != dialect) {
            boolean dialectFound = false;
            for (int i = 0; i < VALID_DIALECTS.length; ++i) {
                if (VALID_DIALECTS[i] != dialect) continue;
                dialectFound = true;
                break;
            }
            if (!dialectFound) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return "MountParams [minDialect=" + this.minDialect + ", maxDialect=" + this.maxDialect + ", maxSecurityLevel=" + this.maxSecurityLevel + ", minSecurityLevel=" + this.minSecurityLevel + ", isReadAccessSufficient=" + this.isReadAccessSufficient + ", enableAccessDeniedUpdater=" + this.enableAccessDeniedUpdater + "]";
    }
}

