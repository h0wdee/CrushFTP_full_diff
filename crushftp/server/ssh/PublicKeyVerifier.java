/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.maverick.ssh.SshException
 *  com.maverick.ssh.components.SshPublicKey
 *  com.maverick.sshd.Connection
 *  com.maverick.sshd.auth.AbstractPublicKeyAuthenticationProvider
 *  com.maverick.sshd.platform.PermissionDeniedException
 *  com.sshtools.publickey.SshPublicKeyFileFactory
 */
package crushftp.server.ssh;

import com.crushftp.client.Common;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.sshd.Connection;
import com.maverick.sshd.auth.AbstractPublicKeyAuthenticationProvider;
import com.maverick.sshd.platform.PermissionDeniedException;
import com.sshtools.publickey.SshPublicKeyFileFactory;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.UserTools;
import crushftp.server.ServerStatus;
import crushftp.server.ssh.SSHCrushAuthentication8;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

public class PublicKeyVerifier
extends AbstractPublicKeyAuthenticationProvider {
    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean isAuthorizedKey(SshPublicKey key, Connection conn) {
        try {
            SessionCrush thisSession = SSHCrushAuthentication8.getSession(conn.getSessionId());
            String username = conn.getUsername();
            thisSession.add_log_formatted("Verifying username and public key " + username + ".", "ACCEPT");
            Properties user = null;
            try {
                user = UserTools.ut.getUser(thisSession.server_item.getProperty("linkedServer"), username, true);
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (user == null) {
                thisSession.add_log_formatted(String.valueOf(username) + " not found, checking plugins.", "ACCEPT");
                Properties u = new Properties();
                Properties pp = new Properties();
                pp.put("user", u);
                pp.put("username", thisSession.uiSG("user_name"));
                pp.put("password", "");
                pp.put("anyPass", "true");
                if (thisSession.uiSG("user_name").equalsIgnoreCase("default")) {
                    return false;
                }
                if (!pp.getProperty("action", "").equalsIgnoreCase("success")) {
                    pp.put("publickey_lookup", "true");
                    pp.put("ssh_fingerprint", key.getFingerprint());
                    pp = thisSession.runPlugin("login", pp);
                    user = u;
                    if (!pp.getProperty("templateUser", "").equals("")) {
                        Vector extraLinkedVfs = (Vector)pp.get("linked_vfs");
                        Vector<String> ichain = new Vector<String>();
                        ichain.addElement("default");
                        int x = 0;
                        while (x < pp.getProperty("templateUser", "").split(";").length) {
                            ichain.addElement(pp.getProperty("templateUser", "").split(";")[x].trim());
                            ++x;
                        }
                        if (extraLinkedVfs != null) {
                            ichain.addAll(extraLinkedVfs);
                        }
                        x = 0;
                        while (x < ichain.size()) {
                            Properties tempUser = UserTools.ut.getUser(((Properties)pp.get("user_info")).getProperty("listen_ip_port"), ichain.elementAt(x).toString(), ServerStatus.BG("resolve_inheritance"));
                            if (tempUser != null && !tempUser.getProperty("ssh_public_keys", "").equals("")) {
                                u.put("ssh_public_keys", (String.valueOf(u.getProperty("ssh_public_keys", "")) + "\r\n" + tempUser.getProperty("ssh_public_keys", "")).trim());
                            }
                            ++x;
                        }
                    }
                    if (u.getProperty("ssh_public_keys", "").equals("")) {
                        thisSession.add_log_formatted(String.valueOf(username) + " didn't have any public keys references, checking default.", "ACCEPT");
                        Properties tmp_user = UserTools.ut.getUser(thisSession.server_item.getProperty("linkedServer"), "default", true);
                        u.put("ssh_public_keys", tmp_user.getProperty("ssh_public_keys", ""));
                    }
                }
            }
            if (user == null) {
                thisSession.add_log_formatted(String.valueOf(username) + " not found.", "ACCEPT");
                if (!System.getProperty("crushftp.ssh_auth_alerts", "false").equals("true")) return false;
                try {
                    Properties info = new Properties();
                    info.put("alert_type", "bad_login");
                    info.put("alert_sub_type", "username");
                    info.put("alert_timeout", "0");
                    info.put("alert_max", "0");
                    info.put("alert_msg", String.valueOf(username) + " not found, public key auth ignored.");
                    ServerStatus.thisObj.runAlerts("security_alert", info, thisSession.user_info, thisSession);
                    return false;
                }
                catch (Exception ee) {
                    Log.log("BAN", 1, ee);
                }
                return false;
            }
            Vector keysVec = UserTools.buildPublicKeys(username, user, thisSession.server_item.getProperty("linkedServer"));
            Log.log("SSH_SERVER", 2, "public_keys found:" + keysVec.toString());
            int x = 0;
            while (x < keysVec.size()) {
                String data = keysVec.elementAt(x).toString();
                if (data != null) {
                    data = data.trim();
                    String ssh_key_info = "";
                    if (data.contains("!!!ssh_key_info!!!")) {
                        ssh_key_info = data.substring(data.indexOf("!!!ssh_key_info!!!") + "!!!sshs_key_info!!!".length(), data.length());
                        data = data.substring(0, data.indexOf("!!!ssh_key_info!!!"));
                    }
                    try (InputStream in = null;){
                        StringTokenizer st = new StringTokenizer(data);
                        int parts = 0;
                        while (st.hasMoreElements()) {
                            ++parts;
                            st.nextElement();
                        }
                        if (parts <= 2) {
                            data = String.valueOf(data.trim()) + " nouser@domain.com";
                        }
                        if (SshPublicKeyFileFactory.parse((InputStream)(in = new ByteArrayInputStream(data.getBytes()))).toPublicKey().getFingerprint().equals(key.getFingerprint())) {
                            thisSession.add_log_formatted("Accepted public key for " + username + ":" + data, "ACCEPT");
                            thisSession.uiPUT("publickey_auth_ok", "true");
                            thisSession.uiPUT("publickey_auth_info", ssh_key_info);
                            boolean bl = this.logonUser(conn, username, key);
                            return bl;
                        }
                    }
                }
                ++x;
            }
            if (keysVec.size() <= 0) return false;
            thisSession.add_log_formatted(String.valueOf(username) + ":" + keysVec.size() + " public keys checked, none were valid for the login attempt.", "ACCEPT");
            if (!System.getProperty("crushftp.ssh_auth_alerts", "false").equals("true")) return false;
            try {
                Properties info = new Properties();
                info.put("alert_type", "bad_login");
                info.put("alert_sub_type", "username");
                info.put("alert_timeout", "0");
                info.put("alert_max", "0");
                info.put("alert_msg", String.valueOf(username) + " failed public key auth, no matching fingerprints (" + keysVec.size() + " keys checked.");
                ServerStatus.thisObj.runAlerts("security_alert", info, thisSession.user_info, thisSession);
                return false;
            }
            catch (Exception ee) {
                Log.log("BAN", 1, ee);
                return false;
            }
        }
        catch (Exception e) {
            Log.log("SSH_SERVER", 1, e);
        }
        return false;
    }

    public void add(SshPublicKey arg0, String arg1, Connection arg2) throws IOException, PermissionDeniedException, SshException {
    }

    public Iterator getKeys(Connection arg0) throws PermissionDeniedException, IOException {
        return null;
    }

    public void remove(SshPublicKey arg0, Connection arg1) throws IOException, PermissionDeniedException, SshException {
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean logonUser(Connection conn, String username, SshPublicKey key) {
        try {
            boolean anyPass;
            Properties user;
            SessionCrush thisSession;
            block30: {
                String pass;
                block29: {
                    if (ServerStatus.BG("username_uppercase")) {
                        username = username.toUpperCase();
                    }
                    if (ServerStatus.BG("lowercase_usernames")) {
                        username = username.toLowerCase();
                    }
                    thisSession = SSHCrushAuthentication8.getSession(conn.getSessionId());
                    thisSession.runPlugin("beforeLogin", null);
                    user = null;
                    try {
                        user = UserTools.ut.getUser(thisSession.uiSG("listen_ip_port"), thisSession.uiSG("user_name"), true);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (user == null) {
                        thisSession.add_log_formatted(String.valueOf(username) + " not found, checking plugins.", "ACCEPT");
                        Properties u = new Properties();
                        Properties pp = new Properties();
                        pp.put("user", u);
                        pp.put("username", thisSession.uiSG("user_name"));
                        pp.put("password", "");
                        pp.put("anyPass", "true");
                        if (thisSession.uiSG("user_name").equalsIgnoreCase("default")) {
                            return false;
                        }
                        SessionCrush.checkTempAccounts(pp, thisSession.server_item.getProperty("linkedServer", ""));
                        if (!pp.getProperty("action", "").equalsIgnoreCase("success")) {
                            if (!(pp = thisSession.runPlugin("login", pp)).getProperty("templateUser", "").equals("")) {
                                Vector extraLinkedVfs = (Vector)pp.get("linked_vfs");
                                Vector<String> ichain = new Vector<String>();
                                ichain.addElement("default");
                                int x = 0;
                                while (x < pp.getProperty("templateUser", "").split(";").length) {
                                    ichain.addElement(pp.getProperty("templateUser", "").split(";")[x].trim());
                                    ++x;
                                }
                                if (extraLinkedVfs != null) {
                                    ichain.addAll(extraLinkedVfs);
                                }
                                x = 0;
                                while (x < ichain.size()) {
                                    Properties tempUser = UserTools.ut.getUser(((Properties)pp.get("user_info")).getProperty("listen_ip_port"), ichain.elementAt(x).toString(), ServerStatus.BG("resolve_inheritance"));
                                    if (tempUser != null && !tempUser.getProperty("publickey_password", "").equals("")) {
                                        u.put("publickey_password", tempUser.getProperty("publickey_password", ""));
                                    }
                                    ++x;
                                }
                            }
                            user = u;
                        }
                    }
                    if (user == null) {
                        thisSession.add_log_formatted(String.valueOf(username) + " not found.", "ACCEPT");
                        return false;
                    }
                    pass = user.getProperty("password");
                    anyPass = true;
                    thisSession.uiPUT("current_password", "");
                    if (!pass.startsWith("SHA:") && !pass.startsWith("SHA512:") && !pass.startsWith("SHA256:") && !pass.startsWith("SHA3:") && !pass.startsWith("MD5:") && !pass.startsWith("CRYPT3:") && !pass.startsWith("BCRYPT:") && !pass.startsWith("MD5CRYPT:") && !pass.startsWith("PBKDF2SHA256:") && !pass.startsWith("SHA512CRYPT:") && !pass.startsWith("ARGOND:")) break block29;
                    if (thisSession.uiBG("publickey_auth_ok")) {
                        anyPass = true;
                    }
                    if (!Common.System2.getProperty("crushftp.proxy.anyPassToken", "").equals("")) {
                        anyPass = false;
                        Log.log("SSH_SERVER", 2, "Logging in via proxy magic token 1.");
                        if (!user.getProperty("ssh_public_keys", "").trim().equals("")) {
                            thisSession.uiPUT("current_password", Common.System2.getProperty("crushftp.proxy.anyPassToken", ""));
                        }
                    }
                    break block30;
                }
                if (!Common.System2.getProperty("crushftp.proxy.anyPassToken", "").equals("")) {
                    anyPass = false;
                    Log.log("SSH_SERVER", 2, "Logging in via proxy magic token 2.");
                    if (!user.getProperty("ssh_public_keys", "").trim().equals("")) {
                        thisSession.uiPUT("current_password", Common.System2.getProperty("crushftp.proxy.anyPassToken", ""));
                    }
                } else {
                    thisSession.uiPUT("current_password", ServerStatus.thisObj.common_code.decode_pass(pass));
                }
            }
            thisSession.add_log_formatted("USER " + thisSession.uiSG("user_name"), "USER");
            thisSession.add_log_formatted("PASS PublicKeyAuthentication", "PASS");
            boolean publickey_password = user.getProperty("publickey_password", "false").equalsIgnoreCase("true");
            if (Common.dmz_mode) {
                Vector queue = (Vector)Common.System2.get("crushftp.dmz.queue");
                Properties action = new Properties();
                action.put("type", "GET:USER");
                action.put("id", crushftp.handlers.Common.makeBoundary());
                String internal_username = username;
                if (thisSession.server_item.getProperty("linkedServer", "").equals("@AutoDomain") && internal_username.contains("@")) {
                    internal_username = internal_username.substring(0, username.lastIndexOf("@"));
                }
                action.put("username", internal_username);
                action.put("need_response", "true");
                String preferred_port = UserTools.ut.getPreferredPort(thisSession.uiSG("listen_ip_port"), thisSession.uiSG("user_name"));
                if (!preferred_port.equals("")) {
                    action.put("preferred_port", preferred_port);
                }
                queue.addElement(action);
                action = UserTools.waitResponse(action, 60);
                if (action != null && action.get("user") != null) {
                    publickey_password = ((Properties)action.get("user")).getProperty("publickey_password", "false").equalsIgnoreCase("true");
                }
            }
            if (!publickey_password) {
                thisSession.login_user_pass(anyPass);
            }
            if (thisSession.uiBG("user_logged_in")) {
                thisSession.uiPUT("publickey_auth_ok", "true");
                if (publickey_password) return true;
                Properties p = thisSession.do_event5("LOGIN", null);
                if (p == null) return true;
                if (!p.containsKey("allowLogin")) return true;
                if (!p.getProperty("allowLogin", "true").equals("false")) return true;
                thisSession.logLogin(false, "Plugin rejected the login.");
                thisSession.add_log_formatted("A plugin rejected the login. Login failed.", "USER");
                return false;
            }
            try {
                Properties info = new Properties();
                info.put("alert_type", "bad_login");
                info.put("alert_sub_type", "username");
                info.put("alert_timeout", "0");
                info.put("alert_max", "0");
                info.put("alert_msg", String.valueOf(username) + " failed public key auth.");
                ServerStatus.thisObj.runAlerts("security_alert", info, thisSession.user_info, thisSession);
                return false;
            }
            catch (Exception ee) {
                Log.log("BAN", 1, ee);
            }
            return false;
        }
        catch (Exception e) {
            Log.log("SSH_SERVER", 1, e);
            return false;
        }
    }
}

