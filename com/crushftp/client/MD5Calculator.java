/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class MD5Calculator {
    MessageDigest md5 = null;
    Process proc = null;
    OutputStream md5_out = null;
    boolean enabled = true;
    Exception last_error = null;

    public MD5Calculator(boolean native_proc, String instance_type, boolean enabled) throws Exception {
        this.enabled = enabled;
        if (enabled && (!native_proc || Common.machine_is_windows())) {
            this.md5 = MessageDigest.getInstance(instance_type);
        }
    }

    public void reset() {
        if (this.enabled && this.md5 != null) {
            this.md5.reset();
        } else if (this.enabled) {
            try {
                this.proc = Common.machine_is_x() ? Runtime.getRuntime().exec("md5") : Runtime.getRuntime().exec("md5sum");
                this.md5_out = new BufferedOutputStream(this.proc.getOutputStream());
            }
            catch (IOException e) {
                this.last_error = e;
                Common.log("SERVER", 0, e);
            }
        }
    }

    public void update(byte[] b) {
        this.update(b, 0, b.length);
    }

    public void update(byte[] b, int pos, int len) {
        if (this.enabled && this.md5 != null) {
            this.md5.update(b, pos, len);
        } else if (this.enabled) {
            if (this.md5_out == null) {
                this.reset();
            }
            try {
                this.md5_out.write(b, pos, len);
            }
            catch (Exception e) {
                this.last_error = e;
                Common.log("SERVER", 0, e);
            }
        }
    }

    public String getHash() throws Exception {
        String md5_str = "DISABLED";
        if (this.enabled) {
            if (this.md5 != null) {
                md5_str = new BigInteger(1, this.md5.digest()).toString(16).toLowerCase();
            } else {
                if (this.md5_out == null) {
                    this.reset();
                }
                InputStream in = this.proc.getInputStream();
                this.md5_out.flush();
                this.md5_out.close();
                md5_str = Common.consumeResponse(in).trim().toLowerCase();
                if (md5_str.indexOf(" ") > 0) {
                    md5_str = md5_str.substring(0, md5_str.indexOf(" "));
                }
                this.proc.waitFor();
                this.proc.destroy();
                this.md5_out = null;
                this.proc = null;
            }
            while (md5_str.length() < 32) {
                md5_str = "0" + md5_str;
            }
        }
        if (this.last_error != null) {
            md5_str = "" + this.last_error;
        }
        return md5_str;
    }

    public void close() {
        try {
            if (this.md5_out != null) {
                this.md5_out.close();
            }
            if (this.proc != null) {
                this.proc.destroy();
            }
            this.md5_out = null;
            this.proc = null;
        }
        catch (IOException e) {
            Common.log("SERVER", 0, e);
        }
    }
}

