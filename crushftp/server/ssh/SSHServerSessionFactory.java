/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.maverick.sshd.Connection
 *  com.maverick.sshd.platform.FileSystem
 *  com.maverick.sshd.platform.FileSystemFactory
 *  com.maverick.sshd.platform.PermissionDeniedException
 */
package crushftp.server.ssh;

import com.maverick.sshd.Connection;
import com.maverick.sshd.platform.FileSystem;
import com.maverick.sshd.platform.FileSystemFactory;
import com.maverick.sshd.platform.PermissionDeniedException;
import crushftp.handlers.Log;
import crushftp.server.ServerSessionSSH;
import java.io.IOException;
import java.util.Properties;

public class SSHServerSessionFactory
implements FileSystemFactory {
    public static Properties scp_fs = new Properties();

    public FileSystem createInstance(Connection conn, String protocolInUse) throws PermissionDeniedException, IOException {
        ServerSessionSSH fs = new ServerSessionSSH();
        fs.init(conn, protocolInUse);
        if (protocolInUse.toUpperCase().equals("SCP")) {
            Log.log("SSH_SERVER", 2, "SCP command : Store SSH session : Session id : " + conn.getSessionId() + " Session SSH : " + fs);
            scp_fs.put(conn.getSessionId(), fs);
        }
        return fs;
    }

    public FileSystem getFileSystem(String sessionId) {
        if (scp_fs.containsKey(sessionId)) {
            FileSystem fs = (FileSystem)scp_fs.get(sessionId);
            Log.log("SSH_SERVER", 2, "SCP command : Get SSH Sesssion : Session id : " + sessionId + " Session SSH : " + fs);
            return fs;
        }
        return null;
    }
}

