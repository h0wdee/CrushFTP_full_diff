/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.maverick.sshd.scp.FilenamePattern
 *  com.maverick.sshd.scp.ScpCommand
 */
package crushftp.server.ssh;

import com.maverick.sshd.scp.FilenamePattern;
import com.maverick.sshd.scp.ScpCommand;
import crushftp.handlers.Log;
import crushftp.server.ServerSessionSSH;
import crushftp.server.ssh.SSHServerSessionFactory;
import crushftp.server.ssh.ScpFileNamePattern;

public class SSH_ScpCommand
extends ScpCommand {
    protected FilenamePattern createFilenamePattern(String pattern) {
        ScpFileNamePattern fp = new ScpFileNamePattern(pattern);
        if (this.session != null && this.session.getContext() != null && this.session.getContext().getFileSystemProvider() != null) {
            try {
                ServerSessionSSH fs = (ServerSessionSSH)((SSHServerSessionFactory)this.session.getContext().getFileSystemProvider()).getFileSystem(this.session.getSessionIdentifier());
                fs.setFileNamePattern(fp);
                Log.log("SSH_SERVER", 2, "SCP command : Session id : " + this.session.getSessionIdentifier() + " Session SSH : " + fs);
            }
            catch (Exception e) {
                Log.log("SSH_SERVER", 2, e);
            }
        }
        return fp;
    }
}

