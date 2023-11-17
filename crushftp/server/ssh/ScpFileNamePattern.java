/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.maverick.sshd.scp.FilenamePattern
 */
package crushftp.server.ssh;

import com.maverick.sshd.scp.FilenamePattern;
import crushftp.handlers.Log;

public class ScpFileNamePattern
extends FilenamePattern {
    public ScpFileNamePattern(String pattern) {
        super(pattern);
        Log.log("SSH_SERVER", 2, "SCP command : FNP pattern : " + pattern);
    }

    public boolean matches(String fname) {
        boolean mfname = super.matches(fname);
        Log.log("SSH_SERVER", 2, "SCP command : FNP : Match : " + fname + " result : " + mfname);
        return mfname;
    }

    public boolean setIgnoreCase(boolean flag) {
        Log.log("SSH_SERVER", 2, "SCP command : FNP : Set ignore case : " + flag);
        return super.setIgnoreCase(flag);
    }
}

