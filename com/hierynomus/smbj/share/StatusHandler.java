/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.share;

import com.hierynomus.mserref.NtStatus;

public interface StatusHandler {
    public static final StatusHandler SUCCESS = new StatusHandler(){

        @Override
        public boolean isSuccess(long statusCode) {
            return statusCode == NtStatus.STATUS_SUCCESS.getValue();
        }
    };

    public boolean isSuccess(long var1);
}

