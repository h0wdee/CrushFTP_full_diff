/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.Base64;
import com.crushftp.client.File_S;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import crushftp.gui.LOC;
import crushftp.handlers.AlertTools;
import crushftp.handlers.Common;
import crushftp.handlers.DesEncrypter;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.SharedSessionReplicated;
import crushftp.server.ServerStatus;
import crushftp.server.VFS;
import crushftp.user.SQLUsers;
import crushftp.user.UserProvider;
import crushftp.user.XMLUsers;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class UserTools {
    public static Vector anyPassTokens = new Vector();
    public static Properties anyPassTokensTime = new Properties();
    public static String db_class = "XML";
    public static UserTools ut = new UserTools();
    public static transient Object userExpirePasswordLock = new Object();
    private static UserProvider up = new XMLUsers();
    public static SimpleDateFormat expire_vfs = new SimpleDateFormat("MM/dd/yyyy hh:mm aa", Locale.US);
    public static transient Object user_lock = new Object();
    public static Properties users_lock = new Properties();
    public static Properties user_email_cache = new Properties();
    public static transient Properties pending_put_in = new Properties();

    static {
        String token = Common.makeBoundary(20);
        anyPassTokens.addElement(token);
        anyPassTokensTime.put(token, String.valueOf(System.currentTimeMillis()));
    }

    public UserTools() {
        UserTools.initUserProvider();
    }

    public void forceMemoryReload(String username) {
        if (!ServerStatus.BG("allow_memory_reload_of_users")) {
            return;
        }
        int x = ServerStatus.siVG("user_list").size() - 1;
        while (x >= 0) {
            try {
                SessionCrush thisSession = (SessionCrush)((Properties)ServerStatus.siVG("user_list").elementAt(x)).get("session");
                Properties user = ut.getUser(thisSession.uiSG("listen_ip_port"), thisSession.uiSG("user_name"), true);
                VFS uVFS = ut.getVFS(thisSession.uiSG("listen_ip_port"), thisSession.uiSG("user_name"));
                if (thisSession.user != null && user != null) {
                    String root_dir = thisSession.user.getProperty("root_dir");
                    thisSession.user.putAll((Map<?, ?>)user);
                    thisSession.user.put("root_dir", root_dir);
                    if (thisSession.uVFS != null && thisSession.uVFS.username.equalsIgnoreCase(thisSession.uiSG("user_name"))) {
                        thisSession.uVFS.homes = uVFS.homes;
                        thisSession.uVFS.permissions = uVFS.permissions;
                        UserTools.setupVFSLinking(thisSession.uiSG("listen_ip_port"), thisSession.uiSG("user_name"), thisSession.uVFS, thisSession.user);
                    }
                    uVFS.disconnect();
                    uVFS.free();
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
            --x;
        }
    }

    public void forceMemoryReloadUser(String username) {
        if (!ServerStatus.BG("allow_memory_reload_of_users")) {
            return;
        }
        int x = ServerStatus.siVG("user_list").size() - 1;
        while (x >= 0) {
            try {
                SessionCrush thisSession = (SessionCrush)((Properties)ServerStatus.siVG("user_list").elementAt(x)).get("session");
                if (thisSession.uiSG("user_name").equalsIgnoreCase(username)) {
                    Properties user = ut.getUser(thisSession.uiSG("listen_ip_port"), thisSession.uiSG("user_name"), true);
                    VFS uVFS = ut.getVFS(thisSession.uiSG("listen_ip_port"), thisSession.uiSG("user_name"));
                    if (thisSession.user != null && user != null) {
                        String root_dir = thisSession.user.getProperty("root_dir");
                        thisSession.user.putAll((Map<?, ?>)user);
                        thisSession.user.put("root_dir", root_dir);
                        if (thisSession.uVFS != null && thisSession.uVFS.username.equalsIgnoreCase(thisSession.uiSG("user_name"))) {
                            thisSession.uVFS.homes = uVFS.homes;
                            thisSession.uVFS.permissions = uVFS.permissions;
                            UserTools.setupVFSLinking(thisSession.uiSG("listen_ip_port"), thisSession.uiSG("user_name"), thisSession.uVFS, thisSession.user);
                        }
                        uVFS.disconnect();
                        uVFS.free();
                    }
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
            --x;
        }
    }

    public static void setupVFSLinking(String serverGroup, String username, VFS uVFS, Properties user) {
        VFS tempVFS;
        Vector linked_vfs;
        int x;
        Properties permissions;
        if (uVFS == null || uVFS.homes == null) {
            return;
        }
        Properties virtual = (Properties)uVFS.homes.elementAt(0);
        if (!virtual.containsKey("/")) {
            Properties pp = new Properties();
            pp.put("virtualPath", "/");
            pp.put("name", "VFS");
            pp.put("type", "DIR");
            pp.put("vItems", new Vector());
            virtual.put("/", pp);
        }
        if (!(permissions = (Properties)((Vector)virtual.get("vfs_permissions_object")).elementAt(0)).containsKey("/")) {
            permissions.put("/", "(read)(view)(resume)");
        }
        UserTools.getExtraVFS(serverGroup, username, uVFS, user);
        Log.log("LOGIN", 2, "Getting linked VFSs: " + user.get("linked_vfs"));
        if (user.get("linked_vfs") instanceof String) {
            String[] s = user.get("linked_vfs").toString().split(",");
            Vector<String> v = new Vector<String>();
            x = 0;
            while (x < s.length) {
                if (!s[x].trim().equals("")) {
                    v.addElement(s[x].trim());
                }
                ++x;
            }
            user.put("linked_vfs", v);
        }
        if ((linked_vfs = (Vector)user.get("linked_vfs")) == null) {
            linked_vfs = new Vector();
        }
        Vector<String> alreadyAdded = new Vector<String>();
        alreadyAdded.addElement(uVFS.user_info.getProperty("vfs_username", ""));
        x = 0;
        while (x < linked_vfs.size()) {
            if (!linked_vfs.elementAt(x).toString().trim().equals("")) {
                try {
                    if (alreadyAdded.indexOf(linked_vfs.elementAt(x).toString()) < 0) {
                        tempVFS = ut.getVFS(serverGroup, linked_vfs.elementAt(x).toString());
                        uVFS.addLinkedVFS(tempVFS);
                        alreadyAdded.addElement(linked_vfs.elementAt(x).toString());
                        Properties tempUser = ut.getUser(serverGroup, linked_vfs.elementAt(x).toString(), false);
                        UserTools.getExtraVFS(serverGroup, linked_vfs.elementAt(x).toString(), uVFS, tempUser);
                    }
                }
                catch (Exception e) {
                    Log.log("LOGIN", 1, e);
                }
            }
            ++x;
        }
        if (!ServerStatus.thisObj.server_info.getProperty("enterprise_level", "0").equals("0") && user.getProperty("allow_user_shares", "").equals("true")) {
            try {
                Properties tempUser = ut.getUser(serverGroup, String.valueOf(username) + ".SHARED", false);
                if (tempUser != null && !tempUser.getProperty("username", "").equalsIgnoreCase("TEMPLATE") && (tempVFS = ut.getVFS(serverGroup, String.valueOf(username) + ".SHARED")) != null) {
                    uVFS.addLinkedVFS(tempVFS);
                }
            }
            catch (Exception e) {
                Log.log("LOGIN", 1, e);
            }
        }
    }

    public static void getExtraVFS(String serverGroup, String username, VFS uVFS, Properties user) {
        Log.log("LOGIN", 2, "Getting extra VFSs: " + (user == null ? "null" : user.get("extra_vfs")));
        String inheritedUsername = ut.getEndUsernameVFS(serverGroup, username);
        Vector allUsers = new Vector();
        UserTools.refreshUserList("extra_vfs", allUsers);
        Vector<Object> extra_vfs = new Vector<Object>();
        int x = 0;
        while (x < allUsers.size()) {
            if (allUsers.elementAt(x).toString().toLowerCase().startsWith(String.valueOf(inheritedUsername.toLowerCase()) + "~")) {
                extra_vfs.addElement(allUsers.elementAt(x).toString().substring((String.valueOf(inheritedUsername.toLowerCase()) + "~").length()));
            }
            ++x;
        }
        Object[] evi = extra_vfs.toArray();
        Arrays.sort(evi);
        extra_vfs.removeAllElements();
        int x2 = 0;
        while (x2 < evi.length) {
            extra_vfs.addElement(evi[x2]);
            ++x2;
        }
        if (extra_vfs.size() > 0) {
            user.put("extra_vfs", extra_vfs);
        }
        if (uVFS != null) {
            x2 = 0;
            while (x2 < extra_vfs.size()) {
                if (!extra_vfs.elementAt(x2).toString().trim().equals("")) {
                    try {
                        VFS tempVFS = ut.getVFS("extra_vfs", String.valueOf(inheritedUsername) + "~" + extra_vfs.elementAt(x2).toString());
                        uVFS.addLinkedVFS(tempVFS);
                    }
                    catch (Exception e) {
                        Log.log("LOGIN", 1, e);
                    }
                }
                ++x2;
            }
        }
    }

    public VFS getVFS(String serverGroup, String username) {
        String inheritedUsername = this.getEndUsernameVFS(serverGroup, username);
        Properties virtual = up.buildVFS(serverGroup, inheritedUsername);
        Enumeration<Object> keys = virtual.keys();
        boolean needWrite = false;
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (key.equals("vfs_permissions_object")) continue;
            Properties vp = (Properties)virtual.get(key);
            Vector v = (Vector)vp.get("vItems");
            int x = 0;
            while (v != null && x < v.size()) {
                block18: {
                    Properties p = (Properties)v.elementAt(x);
                    if (!p.getProperty("expires_on", "").trim().equals("")) {
                        try {
                            VRL vrl;
                            GenericClient c;
                            long expire = expire_vfs.parse(p.getProperty("expires_on", "0")).getTime();
                            if (expire >= System.currentTimeMillis() || expire <= 0L) break block18;
                            virtual.remove(key);
                            Properties permission = (Properties)((Vector)virtual.get("vfs_permissions_object")).elementAt(0);
                            permission.remove(key.toUpperCase());
                            needWrite = true;
                            if (!p.getProperty("delete_expired_item", "false").equalsIgnoreCase("true") || (c = com.crushftp.client.Common.getClient(Common.getBaseUrl((vrl = new VRL(p.getProperty("url", ""))).toString()), System.getProperty("appname", "CrushFTP"), new Vector())) == null) break block18;
                            try {
                                try {
                                    boolean result;
                                    c.login(vrl.getUsername(), vrl.getPassword(), "");
                                    if (c.stat(vrl.getPath()) != null && !(result = c.delete(vrl.getPath())) && vrl.getProtocol().equalsIgnoreCase("FILE") && p.getProperty("type", "").equalsIgnoreCase("DIR") && (c.getConfig("file_recurse_delete") == null || !c.getConfig("file_recurse_delete").equals("true"))) {
                                        c.setConfig("file_recurse_delete", "true");
                                        boolean is_deleted = c.delete(vrl.getPath());
                                        c.setConfig("file_recurse_delete", "false");
                                        if (!is_deleted) {
                                            Log.log("SERVER", 1, "Expired VFS : Could not delete folder : " + vrl.safe());
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    Log.log("SERVER", 1, e);
                                    try {
                                        c.logout();
                                    }
                                    catch (Exception e2) {
                                        Log.log("SERVER", 1, e2);
                                    }
                                    break block18;
                                }
                            }
                            catch (Throwable throwable) {
                                try {
                                    c.logout();
                                }
                                catch (Exception e) {
                                    Log.log("SERVER", 1, e);
                                }
                                throw throwable;
                            }
                            try {
                                c.logout();
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 1, e);
                            }
                        }
                        catch (ParseException e) {
                            Log.log("SERVER", 1, e);
                        }
                    }
                }
                ++x;
            }
        }
        if (needWrite) {
            UserTools.writeVFS(serverGroup, username, virtual);
        }
        VFS tempVFS = VFS.getVFS(virtual);
        tempVFS.user_info.put("vfs_username", inheritedUsername);
        return tempVFS;
    }

    public Properties getVirtualVFS(String serverGroup, String username) {
        return up.buildVFS(serverGroup, username);
    }

    private String getEndUsernameVFS(String serverGroup, String username) {
        String vfs_user = "";
        Properties inheritance = up.loadInheritance(serverGroup);
        Enumeration<Object> keys = inheritance.keys();
        Vector ichain = null;
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (!key.equalsIgnoreCase(username)) continue;
            ichain = (Vector)inheritance.get(key);
            break;
        }
        if (ichain != null) {
            Properties user = (Properties)com.crushftp.client.Common.CLONE(up.loadUser(serverGroup, username, inheritance, false, false));
            vfs_user = username;
            if (user != null && !user.containsKey("root_dir")) {
                try {
                    int x = ichain.size() - 1;
                    while (x >= 0) {
                        Properties p = up.loadUser(serverGroup, ichain.elementAt(x).toString(), inheritance, false, false);
                        if (p.containsKey("root_dir") && !user.containsKey("root_dir")) {
                            vfs_user = ichain.elementAt(x).toString();
                            break;
                        }
                        --x;
                    }
                }
                catch (Exception e) {
                    Log.log("USER_OBJ", 1, e);
                }
            }
        }
        if (vfs_user.equals("")) {
            vfs_user = username;
        }
        return vfs_user;
    }

    public Properties getUser(boolean allow_update, String serverGroup, String username, boolean flattenUser, boolean getVfsNotUserVar) {
        return this.getUser(allow_update, serverGroup, username, flattenUser);
    }

    public Properties getUser(String serverGroup, String username, boolean flattenUser, boolean getVfsNotUserVar) {
        return this.getUser(false, serverGroup, username, flattenUser);
    }

    public Properties getUser(String serverGroup, String username, boolean flattenUser) {
        return this.getUser(false, serverGroup, username, flattenUser);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Properties getUser(boolean allow_update, String serverGroup, String username, boolean flattenUser) {
        if (username.equals("")) {
            return null;
        }
        UserTools.initUserProvider();
        Object object = UserTools.get_user_lock(serverGroup, username);
        synchronized (object) {
            Vector events;
            if (serverGroup.equals("@AutoDomain") && username.indexOf("@") > 0) {
                String newLinkedServer = username.split("@")[username.split("@").length - 1];
                String newLinkedServer2 = com.crushftp.client.Common.dots(newLinkedServer);
                if (newLinkedServer.equals(newLinkedServer2 = newLinkedServer2.replace('/', '-').replace('\\', '-').replace('%', '-').replace(':', '-').replace(';', '-'))) {
                    username = username.substring(0, username.lastIndexOf("@"));
                    serverGroup = newLinkedServer;
                }
            }
            Properties inheritance2 = UserTools.getInheritance(serverGroup);
            Properties defaults = null;
            Object object2 = user_lock;
            synchronized (object2) {
                String default_server_group = serverGroup;
                if (serverGroup.endsWith("_restored_backup")) {
                    default_server_group = serverGroup.substring(0, serverGroup.indexOf("_restored_backup"));
                }
                defaults = up.loadUser(default_server_group, "default", inheritance2, true, false);
                Log.log("USER_OBJ", 2, "Validating default object:" + (defaults != null ? String.valueOf(defaults.size()) : "no defaults user.XML found!"));
                if (defaults == null) {
                    Properties p = (Properties)Common.readXMLObject(VFS.class.getResource("/assets/default_user.xml"));
                    if (defaults == null) {
                        defaults = p;
                    }
                    p.putAll((Map<?, ?>)defaults);
                    p.put("defaultsVersion", String.valueOf(ServerStatus.version_info_str) + ServerStatus.sub_version_info_str);
                    try {
                        p.put("password", "MD5:" + Common.getMD5(new ByteArrayInputStream(Common.makeBoundary().getBytes())));
                        UserTools.writeUser(serverGroup, "default", p);
                    }
                    catch (Exception e) {
                        Common.debug(2, e);
                    }
                    defaults = p;
                }
            }
            Properties theUser = up.loadUser(serverGroup, username, inheritance2, flattenUser, allow_update);
            if (theUser == null && !com.crushftp.client.Common.dmz_mode) {
                if (ServerStatus.BG("xmlUsers")) {
                    String user_path = String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/" + username + "/user.XML";
                    boolean exists = new File(user_path).exists();
                    long size = new File(user_path).length();
                    if (!username.equals("anonymous") && !username.equals("template")) {
                        Log.log("SERVER", 0, String.valueOf(user_path) + " not found or invalid. exists=" + exists + " size=" + size);
                    }
                }
            }
            if (theUser == null && !username.equals("template") && (theUser = this.getUser(serverGroup, "template", true)) != null) {
                Log.log("USER_OBJ", 1, " Found template user for : " + serverGroup + "/" + username);
            }
            if (theUser != null && (events = (Vector)theUser.get("events")) != null) {
                int x = 0;
                while (x < events.size()) {
                    Properties p = (Properties)events.elementAt(x);
                    if (!p.getProperty("linkUser", "").equals("") && flattenUser) {
                        Properties pp = this.getLinkedEvent(serverGroup, p);
                        p.putAll((Map<?, ?>)pp);
                    }
                    if (p.containsKey("tasks") && p.get("tasks") instanceof Vector) {
                        Vector v = (Vector)p.get("tasks");
                        int xx = 0;
                        while (xx < v.size()) {
                            Properties pp = (Properties)v.elementAt(xx);
                            Enumeration<Object> keys = pp.keys();
                            while (keys.hasMoreElements()) {
                                String key = keys.nextElement().toString();
                                if (pp.get(key).toString().contains("<LINE>")) {
                                    pp.put(key, Common.replace_str(pp.get(key).toString(), "<LINE>", "{line_start}"));
                                }
                                if (pp.get(key).toString().contains("&lt;LINE&gt;")) {
                                    pp.put(key, Common.replace_str(pp.get(key).toString(), "&lt;LINE&gt;", "{line_start}"));
                                }
                                if (pp.get(key).toString().contains("&amp;lt;LINE&gt;")) {
                                    pp.put(key, Common.replace_str(pp.get(key).toString(), "&amp;lt;LINE&gt;", "{line_start}"));
                                }
                                if (pp.get(key).toString().contains("</LINE>")) {
                                    pp.put(key, Common.replace_str(pp.get(key).toString(), "</LINE>", "{line_end}"));
                                }
                                if (pp.get(key).toString().contains("&lt;/LINE&gt;")) {
                                    pp.put(key, Common.replace_str(pp.get(key).toString(), "&lt;/LINE&gt;", "{line_end}"));
                                }
                                if (!pp.get(key).toString().contains("&amp;lt;/LINE&gt;")) continue;
                                pp.put(key, Common.replace_str(pp.get(key).toString(), "&amp;lt;/LINE&gt;", "{line_end}"));
                            }
                            ++xx;
                        }
                    }
                    ++x;
                }
            }
            if (theUser != null && !theUser.containsKey("username") && theUser.containsKey("user_name")) {
                theUser.put("username", theUser.getProperty("user_name"));
            }
            return theUser;
        }
    }

    public Properties getLinkedEvent(String serverGroup, Properties event) {
        Properties saver = event;
        try {
            String linkUser = event.getProperty("linkUser", "");
            String linkEvent = event.getProperty("linkEvent", "");
            if (!linkUser.equals("")) {
                Properties the_user = this.getUser(serverGroup, linkUser, false);
                Vector copy_event_list = (Vector)the_user.get("events");
                int x = 0;
                while (x < copy_event_list.size()) {
                    Properties p = (Properties)copy_event_list.elementAt(x);
                    if (p.getProperty("name").equals(linkEvent)) {
                        saver = p;
                        break;
                    }
                    ++x;
                }
                saver.put("linkUser", linkUser);
                saver.put("linkEvent", linkEvent);
            }
            if (saver == null) {
                saver = event;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return saver;
    }

    public String getEndUserProperty(String serverGroup, String username, String key, String defaultValue) {
        try {
            Properties the_user = null;
            try {
                the_user = this.getUser(serverGroup, username, false);
            }
            catch (Exception e) {
                Log.log("USER_OBJ", 3, e);
            }
            return the_user.getProperty(key, defaultValue);
        }
        catch (Exception e) {
            Log.log("USER_OBJ", 1, e);
            return defaultValue;
        }
    }

    public synchronized void put_in_user(String serverGroup, String username, String key, String val, boolean backup, boolean replicate) {
        this.put_in_user(serverGroup, username, key, val, backup, replicate, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void put_in_user(String serverGroup, String username, String key, String val, boolean backup, boolean replicate, boolean clear_only_user_related_xml_from_cache) {
        Properties properties = pending_put_in;
        synchronized (properties) {
            Properties config = (Properties)pending_put_in.get(String.valueOf(serverGroup) + ":" + username);
            if (config == null) {
                config = new Properties();
                config.put("u", new Properties());
                config.put("backup", String.valueOf(backup));
                config.put("replicate", String.valueOf(replicate));
                config.put("clear_only_user_related_xml_from_cache", String.valueOf(clear_only_user_related_xml_from_cache));
                config.put("serverGroup", String.valueOf(serverGroup));
                config.put("username", String.valueOf(username));
            }
            Properties u = (Properties)config.get("u");
            u.put(key, val);
            pending_put_in.put(String.valueOf(serverGroup) + ":" + username, config);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void put_in_user_flush() {
        Properties pending_put_in2 = null;
        Properties properties = pending_put_in;
        synchronized (properties) {
            pending_put_in2 = (Properties)pending_put_in.clone();
            pending_put_in.clear();
        }
        Enumeration<Object> keys = pending_put_in2.keys();
        while (keys.hasMoreElements()) {
            Properties config = (Properties)pending_put_in2.get("" + keys.nextElement());
            Properties u = (Properties)config.get("u");
            boolean backup = config.getProperty("backup", "").equals("true");
            boolean replicate = config.getProperty("replicate", "").equals("true");
            boolean clear_only_user_related_xml_from_cache = config.getProperty("clear_only_user_related_xml_from_cache", "").equals("true");
            String serverGroup = config.getProperty("serverGroup");
            String username = config.getProperty("username");
            Object object = UserTools.get_user_lock(serverGroup, username);
            synchronized (object) {
                try {
                    Properties the_user = this.getUser(serverGroup, username, false);
                    if (the_user.getProperty("username", "").equalsIgnoreCase("template") && !username.equalsIgnoreCase("template")) {
                        return;
                    }
                    the_user.putAll((Map<?, ?>)u);
                    if (clear_only_user_related_xml_from_cache) {
                        UserTools.writeUser(serverGroup, username, the_user, replicate, backup, null, clear_only_user_related_xml_from_cache);
                    } else {
                        UserTools.writeUser(serverGroup, username, the_user, replicate, backup);
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (clear_only_user_related_xml_from_cache) continue;
            object = Common.xmlCache;
            synchronized (object) {
                Common.xmlCache.clear();
            }
        }
    }

    public void force_put_in_user_flush() {
        ServerStatus.siPUT("last_put_in_user_flush", String.valueOf(System.currentTimeMillis() + 60000L));
        this.put_in_user_flush();
    }

    public static synchronized void purgeOldBackups(int maxCount) {
        try {
            Log.log("USER_OBJ", 0, "UserBackupPurge:Looking for old user folders to delete from here:" + System.getProperty("crushftp.backup") + "backup/");
            new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/").mkdirs();
            File_S[] folders = (File_S[])new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/").listFiles();
            Log.log("USER_OBJ", 0, "UserBackupPurge:Folder has " + folders.length + " items.");
            if (folders.length >= maxCount) {
                Vector<File_S> validItems = new Vector<File_S>();
                class Counts
                implements Comparator {
                    Counts() {
                    }

                    public int compare(Object p1, Object p2) {
                        File_S f1 = (File_S)p1;
                        File_S f2 = (File_S)p2;
                        if (f1.lastModified() < f2.lastModified()) {
                            return -1;
                        }
                        if (f1.lastModified() > f2.lastModified()) {
                            return 1;
                        }
                        return 0;
                    }
                }
                Arrays.sort(folders, new Counts());
                int x = 0;
                while (x < folders.length) {
                    File_S f = folders[x];
                    if (f.isDirectory() && f.getName().toUpperCase().indexOf("USERS-") >= 0) {
                        validItems.addElement(f);
                    } else if (f.isFile() && f.getName().endsWith(".zip")) {
                        validItems.addElement(f);
                    }
                    ++x;
                }
                Log.log("USER_OBJ", 0, "UserBackupPurge:Folder has " + validItems.size() + " user items, max is " + maxCount + ".");
                while (validItems.size() > 0 && validItems.size() >= maxCount) {
                    File_S f = (File_S)validItems.elementAt(0);
                    Log.log("USER_OBJ", 0, "UserBackupPurge:Purging old backup users from:" + f.getAbsolutePath());
                    Common.recurseDelete(f.getAbsolutePath(), false);
                    validItems.removeElementAt(0);
                }
            }
        }
        catch (Exception e) {
            Log.log("USER_OBJ", 0, e);
        }
    }

    public static void expireUserVFSTask(Properties user, String serverGroup, String username) throws Exception {
        if (!user.getProperty("account_expire_task", "").equals("")) {
            VFS tempVFS = ut.getVFS(serverGroup, username);
            Vector<Properties> items = new Vector<Properties>();
            Vector<Properties> folderItems = new Vector<Properties>();
            Vector foundItems = new Vector();
            tempVFS.appendListing("/", foundItems, "", 99, 10000, true, null, null, null, null);
            tempVFS.disconnect();
            tempVFS.free();
            while (foundItems.size() > 0) {
                Properties item = (Properties)foundItems.remove(0);
                VRL vrl = new VRL(item.getProperty("url"));
                if (vrl.getProtocol().equalsIgnoreCase("virtual")) continue;
                item.put("the_file_name", item.getProperty("name"));
                item.put("the_file_path", String.valueOf(item.getProperty("root_dir")) + item.getProperty("name"));
                item.put("the_file_size", item.getProperty("size", "0"));
                if (item.getProperty("type", "").equalsIgnoreCase("DIR")) {
                    folderItems.addElement(item);
                    continue;
                }
                items.addElement(item);
            }
            while (folderItems.size() > 0) {
                items.insertElementAt((Properties)folderItems.remove(0), 0);
            }
            Log.log("USER_OBJ", 0, "Executing CrushTask " + user.getProperty("account_expire_task", "") + " with VFS items:" + items.size());
            Properties event = new Properties();
            event.put("event_plugin_list", user.getProperty("account_expire_task", ""));
            event.put("name", "ExpiringUser:" + username + ":" + user.getProperty("account_expire", ""));
            Properties info = new Properties();
            info.put("user", user);
            info.put("user_info", user);
            ServerStatus.thisObj.events6.doEventPlugin(info, event, null, items);
        }
    }

    public static Properties getGroups(String serverGroup) {
        return up.loadGroups(serverGroup);
    }

    public static void writeGroups(String serverGroup, Properties groups) {
        UserTools.writeGroups(serverGroup, groups, true);
    }

    public static void writeGroups(String serverGroup, Properties groups, boolean replicate) {
        UserTools.writeGroups(serverGroup, groups, replicate, null);
    }

    public static void writeGroups(String serverGroup, Properties groups, boolean replicate, Properties request) {
        up.writeGroups(serverGroup, groups);
        Properties p = new Properties();
        p.put("serverGroup", serverGroup);
        p.put("groups", groups);
        if (request != null) {
            p.put("request", request);
        }
        if (replicate) {
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.handlers.writeGroups", "info", p);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void refreshUserList(String serverGroup, Vector current_user_group_listing) {
        current_user_group_listing.removeAllElements();
        Vector listing = up.loadUserList(serverGroup);
        current_user_group_listing.addAll(listing);
        Properties upper = new Properties();
        int x2 = 0;
        while (x2 < listing.size()) {
            String username = listing.elementAt(x2).toString();
            upper.put(username.toUpperCase(), username);
            ++x2;
        }
        if (!upper.getProperty("DEFAULT", "DEFAULT").equals("default")) {
            Object x2 = user_lock;
            synchronized (x2) {
                try {
                    up.writeUser(serverGroup, "default", ut.getUser(serverGroup, "default", false), false);
                    current_user_group_listing.addElement("default");
                    upper.put("DEFAULT", "default");
                }
                catch (Exception e) {
                    Log.log("USER_OBJ", 0, e);
                }
            }
        }
        boolean fixedOne = false;
        Properties inheritance = up.loadInheritance(serverGroup);
        if (inheritance != null) {
            Enumeration<Object> keys = inheritance.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                Vector ichain = (Vector)inheritance.get(key);
                int xx = 0;
                while (xx < ichain.size()) {
                    String tempUser = ichain.elementAt(xx).toString();
                    String tempUser2 = upper.getProperty(tempUser.toUpperCase(), "");
                    if (!tempUser2.equals("") && !tempUser2.equals(tempUser)) {
                        ichain.setElementAt(tempUser2, xx);
                        fixedOne = true;
                    }
                    ++xx;
                }
            }
            if (fixedOne) {
                UserTools.writeInheritance(serverGroup, inheritance);
            }
        }
        fixedOne = false;
        Properties groups = up.loadGroups(serverGroup);
        if (groups != null) {
            Enumeration<Object> keys = groups.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                Vector group = (Vector)groups.get(key);
                int xx = 0;
                while (xx < group.size()) {
                    String tempUser = group.elementAt(xx).toString();
                    String tempUser2 = upper.getProperty(tempUser.toUpperCase(), "");
                    if (!tempUser2.equals("") && !tempUser2.equals(tempUser)) {
                        group.setElementAt(tempUser2, xx);
                        fixedOne = true;
                    }
                    ++xx;
                }
            }
        }
        if (fixedOne) {
            UserTools.writeGroups(serverGroup, groups);
        }
    }

    public static Vector findUserEmail(String serverGroup, String email) {
        return up.findUserEmail(serverGroup, email);
    }

    public static Properties generateEmptyVirtual() {
        Properties virtual = new Properties();
        Vector<Properties> v = new Vector<Properties>();
        Properties p = new Properties();
        p.put("/", "(read)(view)(resume)");
        v.addElement(p);
        virtual.put("vfs_permissions_object", v);
        return virtual;
    }

    public static Properties getInheritance(String serverGroup) {
        return up.loadInheritance(serverGroup);
    }

    public static void changeUsername(String serverGroup, String username1, String username2, String password) {
        if (!ServerStatus.BG("allow_default_user_updates") && username1 != null && username1.equals("default")) {
            Log.log("USER_OBJ", 0, "Update not allwoed on user " + username1 + " The allow default user updates :  " + ServerStatus.BG("allow_default_user_updates"));
            return;
        }
        up.updateUser(serverGroup, username1, username2, password);
    }

    public static void writeUser(String serverGroup, String username, Properties user) {
        UserTools.writeUser(serverGroup, username, user, true, true);
    }

    public static void writeUser(String serverGroup, String username, Properties user, boolean replicate, boolean backup) {
        UserTools.writeUser(serverGroup, username, user, replicate, backup, null);
    }

    public static void writeUser(String serverGroup, String username, Properties user, boolean replicate, boolean backup, Properties request) {
        UserTools.writeUser(serverGroup, username, user, replicate, backup, null, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void writeUser(String serverGroup, String username, Properties user, boolean replicate, boolean backup, Properties request, boolean clear_only_user_related_xml_from_cache) {
        if (!ServerStatus.BG("allow_default_user_updates") && username != null && username.equals("default")) {
            Log.log("USER_OBJ", 0, "Write action not allwoed on user: " + username + " The allow default user updates:  " + ServerStatus.BG("allow_default_user_updates"));
            return;
        }
        Object object = UserTools.get_user_lock(serverGroup, username);
        synchronized (object) {
            user.remove("extra_vfs");
            if (clear_only_user_related_xml_from_cache) {
                up.writeUser(serverGroup, username, user, backup, clear_only_user_related_xml_from_cache);
            } else {
                up.writeUser(serverGroup, username, user, backup);
            }
        }
        Properties p = new Properties();
        p.put("serverGroup", serverGroup);
        p.put("username", username);
        p.put("user", user);
        p.put("backup", String.valueOf(backup));
        if (request != null) {
            p.put("request", request);
        }
        if (replicate) {
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.handlers.writeUser", "info", p);
        }
    }

    public static void stripKeys(Properties tempUser, String theUsername, Vector users, Properties inheritance, Properties groups, Properties defaults) {
        Enumeration<Object> keys = tempUser.keys();
        while (keys.hasMoreElements()) {
            String parentUsername;
            String key = keys.nextElement().toString();
            if (!(tempUser.get(key) instanceof String)) continue;
            String val = tempUser.get(key).toString();
            if (!(val.indexOf("@") < 0 || key.equalsIgnoreCase("email") || key.startsWith("x_") || (parentUsername = val.substring(val.lastIndexOf("@") + 1)).length() >= 40 || parentUsername.trim().equals("") || parentUsername.equals(theUsername))) {
                val = val.substring(0, val.lastIndexOf("@"));
                if (users.indexOf(parentUsername) >= 0) {
                    Vector v;
                    Vector<String> inheritanceUser = (Vector<String>)inheritance.get(theUsername);
                    if (inheritanceUser == null) {
                        inheritanceUser = new Vector<String>();
                    }
                    inheritance.put(theUsername, inheritanceUser);
                    if (inheritanceUser.indexOf(parentUsername) < 0) {
                        inheritanceUser.addElement(parentUsername);
                    }
                    if (!groups.containsKey(parentUsername)) {
                        groups.put(parentUsername, new Vector());
                    }
                    if ((v = (Vector)groups.get(parentUsername)).indexOf(theUsername) < 0) {
                        v.addElement(theUsername);
                    }
                }
                tempUser.remove(key);
            }
            if (defaults.get(key) == null || !defaults.get(key).toString().equals(val)) continue;
            tempUser.remove(key);
        }
    }

    public static void writeInheritance(String serverGroup, Properties inheritance) {
        UserTools.writeInheritance(serverGroup, inheritance, true);
    }

    public static void writeInheritance(String serverGroup, Properties inheritance, boolean replicate) {
        UserTools.writeInheritance(serverGroup, inheritance, replicate, null);
    }

    public static void writeInheritance(String serverGroup, Properties inheritance, boolean replicate, Properties request) {
        up.writeInheritance(serverGroup, inheritance);
        Properties p = new Properties();
        p.put("serverGroup", serverGroup);
        p.put("inheritance", inheritance);
        if (request != null) {
            p.put("request", request);
        }
        if (replicate) {
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.handlers.writeInheritance", "info", p);
        }
    }

    public static void deleteUser(String serverGroup, String username) {
        UserTools.deleteUser(serverGroup, username, true);
    }

    public static void deleteUser(String serverGroup, String username, boolean replicate) {
        if (!ServerStatus.BG("allow_default_user_updates") && username != null && username.equals("default")) {
            Log.log("USER_OBJ", 0, "Delete not allwoed on user " + username + " The allow default user updates :  " + ServerStatus.BG("allow_default_user_updates"));
            return;
        }
        up.deleteUser(serverGroup, username);
        Properties p = new Properties();
        p.put("serverGroup", serverGroup);
        p.put("username", username);
        if (replicate) {
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.handlers.deleteUser", "info", p);
        }
        UserTools.sync_vfs_cache(serverGroup, username, new Properties());
        UserTools.removeUserFromGroups(serverGroup, username);
        UserTools.removeUserFromInheritance(serverGroup, username);
    }

    public static String convertUsers(boolean allUsers, Vector users, String serverGroup, String username) {
        Properties inheritance = UserTools.getInheritance(serverGroup);
        Properties groups = UserTools.getGroups(serverGroup);
        Properties defaults = up.loadUser(serverGroup, "default", inheritance, true, false);
        if (allUsers) {
            int x = 0;
            while (x < users.size()) {
                username = users.elementAt(x).toString();
                if (!username.equalsIgnoreCase("default")) {
                    Log.log("USER_OBJ", 0, "Converting user..." + username);
                    try {
                        Vector v;
                        File_S f = new File_S(UserTools.get_real_path_to_user(serverGroup, username));
                        if (!groups.containsKey(f.getParentFile().getName())) {
                            groups.put(f.getParentFile().getName(), new Vector());
                        }
                        if ((v = (Vector)groups.get(f.getParentFile().getName())).indexOf(username) < 0) {
                            v.addElement(username);
                        }
                        f.renameTo(new File_S(String.valueOf(System.getProperty("crushftp.users")) + "/" + serverGroup + "/" + username));
                    }
                    catch (Exception e) {
                        Log.log("USER_OBJ", 0, e);
                    }
                    Properties tempUser = ut.getUser(serverGroup, username, false);
                    if (tempUser != null) {
                        boolean inheritVFS = tempUser.getProperty("root_dir", "").indexOf("@") >= 0;
                        UserTools.stripKeys(tempUser, username, users, inheritance, groups, defaults);
                        UserTools.writeInheritance(serverGroup, inheritance);
                        String the_password = ServerStatus.thisObj.common_code.decode_pass(tempUser.getProperty("password", ""));
                        the_password = ServerStatus.thisObj.common_code.encode_pass(the_password, ServerStatus.SG("password_encryption"), "");
                        tempUser.put("password", the_password);
                        if (!inheritVFS) {
                            tempUser.put("root_dir", "/");
                        }
                        tempUser.put("userVersion", "6");
                        UserTools.writeUser(serverGroup, username, tempUser);
                        Log.log("USER_OBJ", 0, "Converted user:" + username);
                    }
                }
                ++x;
            }
            UserTools.writeGroups(serverGroup, groups);
            return "Users have been updated.\r\nYou will only see GUI items that this user is overriding from the default username.\r\nClick the 'Show All' button to make other changes.\r\n";
        }
        if (username.equalsIgnoreCase("default")) {
            return "default user cannot be converted";
        }
        Properties tempUser = ut.getUser(serverGroup, username, false);
        boolean inheritVFS = tempUser.getProperty("root_dir", "").indexOf("@") >= 0;
        UserTools.stripKeys(tempUser, username, users, inheritance, groups, defaults);
        UserTools.writeInheritance(serverGroup, inheritance);
        if (!inheritVFS) {
            tempUser.put("root_dir", "/");
        }
        tempUser.put("userVersion", "6");
        String the_password = ServerStatus.thisObj.common_code.decode_pass(tempUser.getProperty("password", ""));
        the_password = ServerStatus.thisObj.common_code.encode_pass(the_password, ServerStatus.SG("password_encryption"), "");
        tempUser.put("password", the_password);
        UserTools.writeUser(serverGroup, username, tempUser);
        UserTools.writeGroups(serverGroup, groups);
        return "User has been updated.";
    }

    public static void initUserProvider() {
        if (ServerStatus.server_settings.getProperty("externalSqlUsers", "").equals("true") && ServerStatus.server_settings.getProperty("xmlUsers", "").equals("true")) {
            ServerStatus.server_settings.put("externalSqlUsers", "false");
        }
        if (ServerStatus.server_settings.getProperty("externalSqlUsers", "").equals("true")) {
            db_class = "SQL";
        }
        if ((db_class = ServerStatus.server_settings.getProperty("xmlUsers", "true").equals("true") ? "XML" : (ServerStatus.server_settings.getProperty("xmlUsers", "").equals("false") ? "SQL" : ServerStatus.server_settings.getProperty("xmlUsers", "").trim())).equalsIgnoreCase("XML") || db_class.equals("")) {
            if (up == null || !(up instanceof XMLUsers)) {
                up = new XMLUsers();
            }
        } else if (db_class.equals("SQL")) {
            if (up == null || !(up instanceof SQLUsers)) {
                up = new SQLUsers();
                Properties sqlItems = (Properties)ServerStatus.server_settings.get("sqlItems");
                sqlItems.remove("debug");
                sqlItems.remove("read_only");
                ((SQLUsers)up).setSettings(sqlItems);
                ServerStatus.thisObj.save_server_settings(true);
            }
        } else {
            try {
                Class<?> c = ServerStatus.clasLoader.loadClass(db_class);
                Constructor<?> cons = c.getConstructor(new Properties().getClass(), new String().getClass());
                up = (UserProvider)cons.newInstance(null);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, "Can't load class:" + db_class);
                Log.log("SERVER", 0, e);
            }
        }
    }

    public static String convertUsersSQLXML(String fromMode, String toMode, String serverGroup) {
        String msg = "";
        if (serverGroup == null || serverGroup.equals("CANCELLED")) {
            return "Cancelled";
        }
        UserTools.initUserProvider();
        UserProvider up1 = new XMLUsers();
        UserProvider up2 = new XMLUsers();
        if (fromMode.equals("SQL")) {
            up1 = new SQLUsers();
            ((SQLUsers)up1).setSettings((Properties)ServerStatus.server_settings.get("sqlItems"));
        }
        if (toMode.equals("SQL")) {
            up2 = new SQLUsers();
            ((SQLUsers)up2).setSettings((Properties)ServerStatus.server_settings.get("sqlItems"));
        }
        Vector user_list = up1.loadUserList(serverGroup);
        Properties inheritance = up1.loadInheritance(serverGroup);
        Properties defaults = up1.loadUser(serverGroup, "default", inheritance, true, false);
        msg = String.valueOf(msg) + "Converted " + inheritance.size() + " Inheritance rules.\r\n";
        int x = 0;
        while (x < user_list.size()) {
            Properties user;
            String username = user_list.elementAt(x).toString();
            if ((ServerStatus.BG("allow_default_user_updates") || !username.equals("default") || up2.loadUser(serverGroup, username, up2.loadInheritance(serverGroup), false, false) == null) && (user = up1.loadUser(serverGroup, username, inheritance, false, false)) != null) {
                VFS uVFS = VFS.getVFS(up1.buildVFS(serverGroup, username));
                if (!username.equals("default")) {
                    UserTools.stripUser(user, defaults);
                }
                user.put("userVersion", "6");
                if (user.containsKey("first_name")) {
                    user.put("email", user.getProperty("email", ""));
                }
                up2.writeUser(serverGroup, username, user, true);
                up2.writeVFS(serverGroup, username, (Properties)uVFS.homes.elementAt(0));
            }
            ++x;
        }
        msg = String.valueOf(msg) + "Converted " + user_list.size() + " users.\r\n";
        up2.writeInheritance(serverGroup, inheritance);
        up2.writeGroups(serverGroup, up1.loadGroups(serverGroup));
        return msg;
    }

    public static void stripUser(Properties user, Properties defaults) {
        Enumeration<Object> keys = user.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (!(user.get(key) instanceof String) || key.equals("root_dir") || !defaults.getProperty(key, "").equals(user.getProperty(key))) continue;
            user.remove(key);
        }
    }

    public static Properties getAllowedUsers(String admin_username, String serverGroup, Vector list) {
        Properties info = new Properties();
        Vector<String> allowed_users = new Vector<String>();
        boolean defaultUserEditable = false;
        Properties groups = UserTools.getGroups(serverGroup);
        Vector<String> groupUsers = (Vector<String>)groups.get(admin_username);
        if (groupUsers == null) {
            groupUsers = new Vector<String>();
        }
        int x = 0;
        while (x < groupUsers.size()) {
            groupUsers.setElementAt(groupUsers.elementAt(x).toString(), x);
            ++x;
        }
        Properties inheritance = UserTools.getInheritance(serverGroup);
        Properties inheritance2 = new Properties();
        int x2 = 0;
        while (x2 < list.size()) {
            String username = list.elementAt(x2).toString();
            if (groupUsers.indexOf(username) >= 0) {
                allowed_users.addElement(username);
                if (username.equals("default")) {
                    defaultUserEditable = true;
                }
                if (inheritance.containsKey(username)) {
                    inheritance2.put(username, inheritance.get(username));
                }
            }
            ++x2;
        }
        if (!defaultUserEditable) {
            allowed_users.addElement("default");
        }
        info.put("default_edittable", String.valueOf(defaultUserEditable));
        info.put("list", allowed_users);
        info.put("inheritance", inheritance2);
        return info;
    }

    public static void mergeWebCustomizations(Properties newUser, Properties user) {
        if (newUser != null && newUser.containsKey("web_customizations") && user != null && user.containsKey("web_customizations")) {
            Vector newUser_v = (Vector)newUser.get("web_customizations");
            Vector tempUser_v = (Vector)user.get("web_customizations");
            int xx = 0;
            while (xx < tempUser_v.size()) {
                boolean found = false;
                Properties tempUser_p = (Properties)tempUser_v.elementAt(xx);
                int xxx = 0;
                while (xxx < newUser_v.size()) {
                    Properties newUser_p = (Properties)newUser_v.elementAt(xxx);
                    if (newUser_p.getProperty("key").equals(tempUser_p.getProperty("key"))) {
                        found = true;
                        newUser_v.setElementAt(tempUser_p, xxx);
                        break;
                    }
                    ++xxx;
                }
                if (!found) {
                    newUser_v.addElement(tempUser_p);
                }
                ++xx;
            }
            user.remove("web_customizations");
        }
    }

    public static void mergeLinkedVFS(Properties newUser, Properties user) {
        if (newUser != null && newUser.containsKey("linked_vfs") && user != null && user.containsKey("linked_vfs")) {
            Vector newUser_v = (Vector)newUser.get("linked_vfs");
            Vector tempUser_v = (Vector)user.get("linked_vfs");
            int xx = 0;
            while (xx < tempUser_v.size()) {
                if (newUser_v.indexOf(tempUser_v.elementAt(xx)) < 0) {
                    newUser_v.addElement(tempUser_v.elementAt(xx));
                }
                ++xx;
            }
            user.remove("linked_vfs");
        }
    }

    public static void mergeGroupAdminNames(Properties newUser, Properties user) {
        if (newUser != null && newUser.containsKey("admin_group_name") && user != null && user.containsKey("admin_group_name")) {
            String[] user_admin_group_names = user.getProperty("admin_group_name", "").split(",");
            String[] new_admin_group_names = newUser.getProperty("admin_group_name", "").split(",");
            String admin_goup_names = "";
            int x = 0;
            while (x < user_admin_group_names.length) {
                if (!user_admin_group_names[x].equals("")) {
                    boolean already_has = false;
                    int xx = 0;
                    while (xx < new_admin_group_names.length) {
                        if (user_admin_group_names[x].equals(new_admin_group_names[xx])) {
                            already_has = true;
                        }
                        ++xx;
                    }
                    if (!already_has) {
                        admin_goup_names = String.valueOf(admin_goup_names) + (admin_goup_names.equals("") ? "" : ",") + user_admin_group_names[x];
                    }
                }
                ++x;
            }
            if (!admin_goup_names.equals("")) {
                newUser.put("admin_group_name", String.valueOf(newUser.getProperty("admin_group_name", "")) + (newUser.getProperty("admin_group_name", "").equals("") ? "" : ",") + admin_goup_names);
            }
            user.remove("admin_group_name");
        }
    }

    public static void mergeEvents(Properties newUser, Properties user) {
        if (ServerStatus.BG("merge_events") && newUser != null && user != null) {
            if (!newUser.containsKey("events") || newUser.get("events") == null) {
                newUser.put("events", new Vector());
            }
            if (!user.containsKey("events") || user.get("events") == null) {
                user.put("events", new Vector());
            }
            Vector user_events = (Vector)user.get("events");
            Vector newUser_events = (Vector)newUser.get("events");
            int x = 0;
            while (x < user_events.size()) {
                Properties p = (Properties)user_events.elementAt(x);
                String event_name = p.getProperty("name", "");
                boolean exits = false;
                int xx = 0;
                while (xx < newUser_events.size()) {
                    Properties pp = (Properties)newUser_events.elementAt(xx);
                    if (pp.getProperty("name", "").equals(event_name)) {
                        exits = true;
                        break;
                    }
                    ++xx;
                }
                if (!exits) {
                    newUser_events.add(p);
                }
                ++x;
            }
            user.remove("events");
        }
    }

    public Properties verify_user(ServerStatus server_status_frame, String the_user, String the_password, String listen_ip_port, int user_number, String user_ip, int user_port, Properties server_item, Properties loginReason) {
        return this.verify_user(server_status_frame, the_user, the_password, listen_ip_port, null, user_number, user_ip, user_port, server_item, loginReason, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public Properties verify_user(ServerStatus server_status_frame, String the_user, String the_password, String serverGroup, SessionCrush thisSession, int user_number, String user_ip, int user_port, Properties server_item, Properties loginReason, boolean anyPass) {
        String the_password2;
        if (the_user.indexOf("\\") >= 0) {
            the_user = the_user.substring(the_user.indexOf("\\") + 1);
        }
        if ((the_password2 = the_password).startsWith("SHA:")) return null;
        if (the_password2.startsWith("SHA512:")) return null;
        if (the_password2.startsWith("SHA256:")) return null;
        if (the_password2.startsWith("SHA3:")) return null;
        if (the_password2.startsWith("MD5:")) return null;
        if (the_password2.startsWith("MD5S2:")) return null;
        if (the_password2.startsWith("CRYPT3:")) return null;
        if (the_password2.startsWith("BCRYPT:")) return null;
        if (the_password2.startsWith("MD5CRYPT:")) return null;
        if (the_password2.startsWith("PBKDF2SHA256:")) return null;
        if (the_password2.startsWith("SHA512CRYPT:")) return null;
        if (the_password2.startsWith("ARGOND:")) {
            return null;
        }
        the_password2 = Common.url_decode(the_password);
        if (the_password2.startsWith("SHA:")) return null;
        if (the_password2.startsWith("SHA512:")) return null;
        if (the_password2.startsWith("SHA256:")) return null;
        if (the_password2.startsWith("SHA3:")) return null;
        if (the_password2.startsWith("MD5:")) return null;
        if (the_password2.startsWith("MD5S2:")) return null;
        if (the_password2.startsWith("CRYPT3:")) return null;
        if (the_password2.startsWith("BCRYPT:")) return null;
        if (the_password2.startsWith("MD5CRYPT:")) return null;
        if (the_password2.startsWith("PBKDF2SHA256:")) return null;
        if (the_password2.startsWith("SHA512CRYPT:")) return null;
        if (the_password2.startsWith("ARGOND:")) {
            return null;
        }
        Properties user = null;
        Log.log("USER_OBJ", 2, "Validating user " + the_user + " with password " + (the_password != null && !the_password.equals("")) + " ");
        if (!ServerStatus.BG("blank_passwords") && the_password.trim().equals("") && !anyPass && !the_user.equalsIgnoreCase("ANONYMOUS")) {
            return null;
        }
        try {
            user = ut.getUser(true, serverGroup, the_user, true);
        }
        catch (Exception e) {
            Log.log("USER_OBJ", 2, e);
        }
        Log.log("USER_OBJ", 1, "Validating user " + the_user + " with local user file:" + (user != null ? String.valueOf(user.size()) : "no user.XML found!"));
        if (user != null) {
            String salt;
            loginReason.put("reason", "valid user");
            if (ServerStatus.BG("secondary_login_via_email") && the_user.indexOf("@") >= 0 && user.getProperty("username").indexOf("@") < 0) {
                the_user = user.getProperty("username");
            }
            if (anyPass && user.getProperty("username").equalsIgnoreCase(the_user)) {
                return user;
            }
            if (the_password.startsWith("NTLM:")) {
                try {
                    if (this.validateMd4(the_user, the_password, user.getProperty("password"))) {
                        return user;
                    }
                }
                catch (Exception e) {
                    Log.log("USER_OBJ", 1, e);
                }
                the_password = Common.makeBoundary();
            }
            if ((salt = user.getProperty("salt", "")).equals("random")) {
                salt = "";
            }
            if (user.getProperty("username").equalsIgnoreCase(the_user) && UserTools.check_pass_variants(user.getProperty("password"), the_password, salt)) {
                return user;
            }
            if (user.getProperty("username").equalsIgnoreCase(the_user) && (user.getProperty("auto_set_pass", "false").equals("true") || ServerStatus.thisObj.common_code.decode_pass(user.getProperty("password")).equals("-AUTO-SET-ON-LOGIN-") || ServerStatus.thisObj.common_code.encode_pass("-AUTO-SET-ON-LOGIN-", "SHA", salt).equals(user.getProperty("password")) || ServerStatus.thisObj.common_code.encode_pass("-AUTO-SET-ON-LOGIN-", "SHA512", salt).equals(user.getProperty("password")) || ServerStatus.thisObj.common_code.encode_pass("-AUTO-SET-ON-LOGIN-", "SHA256", salt).equals(user.getProperty("password")) || ServerStatus.thisObj.common_code.encode_pass("-AUTO-SET-ON-LOGIN-", "SHA3", salt).equals(user.getProperty("password")) || ServerStatus.thisObj.common_code.encode_pass("-AUTO-SET-ON-LOGIN-", "MD5", salt).equals(user.getProperty("password")) || ServerStatus.thisObj.common_code.encode_pass("-AUTO-SET-ON-LOGIN-", ServerStatus.SG("password_encryption"), salt).equals(user.getProperty("password")) || ServerStatus.thisObj.common_code.encode_pass("-AUTO-SET-ON-LOGIN-", ServerStatus.SG("password_encryption"), salt).equals(user.getProperty("password")))) {
                Log.log("SERVER", 0, String.valueOf(the_user) + " logging in to change expired password...");
                Properties password_rules = SessionCrush.build_password_rules(user);
                if (ServerStatus.thisObj.common_code.decode_pass(user.getProperty("password")).equals(the_password) || ServerStatus.thisObj.common_code.encode_pass(the_password, "SHA", salt).equals(user.getProperty("password")) || ServerStatus.thisObj.common_code.encode_pass(the_password, "SHA512", salt).equals(user.getProperty("password")) || ServerStatus.thisObj.common_code.encode_pass(the_password, "SHA256", salt).equals(user.getProperty("password")) || ServerStatus.thisObj.common_code.encode_pass(the_password, "SHA3", salt).equals(user.getProperty("password")) || ServerStatus.thisObj.common_code.encode_pass(the_password, "MD5", salt).equals(user.getProperty("password")) || ServerStatus.thisObj.common_code.encode_pass(the_password, ServerStatus.SG("password_encryption"), salt).equals(user.getProperty("password"))) {
                    Log.log("SERVER", 0, String.valueOf(the_user) + " logging in with expired password...");
                } else {
                    String pass_requiremetns = Common.checkPasswordRequirements(the_password, user.getProperty("password_history", ""), password_rules);
                    if (!pass_requiremetns.equals("")) {
                        loginReason.put("reason", "Auto set password : Invalid password! Error : " + pass_requiremetns);
                        Log.log("SERVER", 0, String.valueOf(the_user) + " Auto set password : Invalid password! Error :" + pass_requiremetns);
                        if (thisSession == null) return null;
                        thisSession.uiPUT("lastProxyError", "Auto set password : Invalid password! Error : " + pass_requiremetns);
                        thisSession.uiPUT("lastLog", "<response><message>Auto set password : Invalid password! Error : " + pass_requiremetns + "</message><response>");
                        return null;
                    }
                    Log.log("SERVER", 0, String.valueOf(the_user) + " password is being changed...");
                    Object object = userExpirePasswordLock;
                    synchronized (object) {
                        try {
                            this.put_in_user(serverGroup, the_user, "password", ServerStatus.thisObj.common_code.encode_pass(the_password, ServerStatus.SG("password_encryption"), salt), true, true);
                            this.put_in_user(serverGroup, the_user, "password_history", Common.getPasswordHistory(the_password, user.getProperty("password_history", ""), password_rules), true, true);
                            if (!user.getProperty("expire_password_days").equals("")) {
                                GregorianCalendar gc = new GregorianCalendar();
                                gc.setTime(new Date());
                                ((Calendar)gc).add(5, Integer.parseInt(user.getProperty("expire_password_days")));
                                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
                                String s = sdf.format(gc.getTime());
                                Log.log("SERVER", 0, String.valueOf(the_user) + " logging in with new password, changing it..." + user.getProperty("expire_password_when") + " to " + s);
                                this.put_in_user(serverGroup, the_user, "expire_password_when", s, true, true);
                            }
                            this.put_in_user(serverGroup, the_user, "auto_set_pass", "false", true, true);
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 1, e);
                            loginReason.put("reason", "Auto set password : Could not set password!");
                            return null;
                        }
                    }
                    loginReason.put("changePassword", "true");
                }
                try {
                    return ut.getUser(serverGroup, the_user, true);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return user;
            }
            if (user.getProperty("username").equalsIgnoreCase(the_user) && the_user.equalsIgnoreCase("ANONYMOUS")) {
                return user;
            }
            if (user.getProperty("username").equalsIgnoreCase(the_user)) {
                Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
                if (ServerStatus.thisObj.common_code.decode_pass(user.getProperty("password")).equals(Common.url_decode(the_password))) return user;
                Common cfr_ignored_1 = ServerStatus.thisObj.common_code;
                if (ServerStatus.thisObj.common_code.encode_pass(Common.url_decode(the_password), "SHA", salt).equals(user.getProperty("password"))) return user;
                Common cfr_ignored_2 = ServerStatus.thisObj.common_code;
                if (ServerStatus.thisObj.common_code.encode_pass(Common.url_decode(the_password), "SHA512", salt).equals(user.getProperty("password"))) return user;
                Common cfr_ignored_3 = ServerStatus.thisObj.common_code;
                if (ServerStatus.thisObj.common_code.encode_pass(Common.url_decode(the_password), "SHA256", salt).equals(user.getProperty("password"))) return user;
                Common cfr_ignored_4 = ServerStatus.thisObj.common_code;
                if (ServerStatus.thisObj.common_code.encode_pass(Common.url_decode(the_password), "SHA3", salt).equals(user.getProperty("password"))) return user;
                Common cfr_ignored_5 = ServerStatus.thisObj.common_code;
                if (ServerStatus.thisObj.common_code.encode_pass(Common.url_decode(the_password), "MD5", salt).equals(user.getProperty("password"))) return user;
                Common cfr_ignored_6 = ServerStatus.thisObj.common_code;
                if (ServerStatus.thisObj.common_code.encode_pass(Common.url_decode(the_password), ServerStatus.SG("password_encryption"), salt).equals(user.getProperty("password"))) return user;
                if (user.getProperty("password").startsWith("MD5S2:") && ServerStatus.thisObj.common_code.encode_pass(String.valueOf(user.getProperty("password").substring(6, 8)) + the_password, "MD5", salt).substring(4).equalsIgnoreCase(user.getProperty("password").substring(8))) {
                    return user;
                }
            }
            if (user.getProperty("username").equalsIgnoreCase("TEMPLATE")) {
                return user;
            }
            if (user.getProperty("username").equalsIgnoreCase("ANONYMOUS") && loginReason.getProperty("no_log_invalid_password", "false").equals("false")) {
                Log.log("SERVER", 0, String.valueOf(the_user) + " password invalid.");
            }
        }
        if (com.crushftp.client.Common.dmz_mode) return null;
        if (user == null) return null;
        if (user.getProperty("failure_count", "0").equals("0")) return null;
        if (user.getProperty("failure_count", "0").equals("")) return null;
        if (user.getProperty("failure_count_max", "0").equals("0")) return null;
        if (user.getProperty("failure_count_max", "0").equals("")) return null;
        Properties info = new Properties();
        info.put("count", String.valueOf(user.getProperty("failure_count", "0")));
        info.put("attempts", String.valueOf(user.getProperty("failure_count_max", "0")));
        info.put("user_name", thisSession.uiSG("user_name"));
        this.doLoginFailureAlert(thisSession, the_user, serverGroup, user, info, "repeated_login_failure");
        return null;
    }

    public static boolean check_pass_variants(String stored_pass, String check_pass, String salt) {
        boolean ok = false;
        if (!ok && ServerStatus.thisObj.common_code.decode_pass(stored_pass).equals(check_pass)) {
            ok = true;
        }
        if (!ok && stored_pass.startsWith("SHA:") && ServerStatus.thisObj.common_code.encode_pass(check_pass, "SHA", salt).equals(stored_pass)) {
            ok = true;
        }
        if (!ok && stored_pass.startsWith("SHA512:") && ServerStatus.thisObj.common_code.encode_pass(check_pass, "SHA512", salt).equals(stored_pass)) {
            ok = true;
        }
        if (!ok && stored_pass.startsWith("SHA256:") && ServerStatus.thisObj.common_code.encode_pass(check_pass, "SHA256", salt).equals(stored_pass)) {
            ok = true;
        }
        if (!ok && stored_pass.startsWith("SHA3:") && ServerStatus.thisObj.common_code.encode_pass(check_pass, "SHA3", salt).equals(stored_pass)) {
            ok = true;
        }
        if (!ok && stored_pass.startsWith("MD5:") && ServerStatus.thisObj.common_code.encode_pass(check_pass, "MD5", salt).equals(stored_pass)) {
            ok = true;
        }
        if (!ok && stored_pass.startsWith("MD5S2:") && ServerStatus.thisObj.common_code.encode_pass(String.valueOf(stored_pass.substring(6, 8)) + check_pass, "MD5", salt).substring(4).equalsIgnoreCase(stored_pass.substring(8))) {
            ok = true;
        }
        if (!ok && stored_pass.startsWith("MD4:") && ServerStatus.thisObj.common_code.encode_pass(check_pass, "MD4", salt).equals(stored_pass)) {
            ok = true;
        }
        if (!ok && !stored_pass.equals("") && ServerStatus.thisObj.common_code.crypt3(check_pass, stored_pass).equals(stored_pass)) {
            ok = true;
        }
        if (!ok && stored_pass.startsWith("BCRYPT:") && ServerStatus.thisObj.common_code.bcrypt(check_pass, stored_pass).equals(stored_pass)) {
            ok = true;
        }
        if (!ok && stored_pass.startsWith("MD5CRYPT:") && ServerStatus.thisObj.common_code.md5crypt(check_pass, stored_pass).equals(stored_pass)) {
            ok = true;
        }
        if (!ok && stored_pass.startsWith("PBKDF2SHA256:") && ServerStatus.thisObj.common_code.pbkdf2sha256(check_pass, stored_pass).equals(stored_pass)) {
            ok = true;
        }
        if (!ok && stored_pass.startsWith("SHA512CRYPT:") && ServerStatus.thisObj.common_code.sha512crypt(check_pass, stored_pass, 0).equals(stored_pass)) {
            ok = true;
        }
        if (!ok && stored_pass.startsWith("SHA512CRYPT:") && ServerStatus.thisObj.common_code.sha512crypt(check_pass, stored_pass, 5000).equals(stored_pass)) {
            ok = true;
        }
        if (!ok && ServerStatus.thisObj.common_code.encode_pass(check_pass, ServerStatus.SG("password_encryption"), salt).equals(stored_pass)) {
            ok = true;
        }
        return ok;
    }

    public void doLoginFailureAlert(SessionCrush thisSession, String the_user, String serverGroup, Properties user, Properties info, String alert_ype) {
        Properties alert_user_info = null;
        boolean is_hack_username = false;
        if (thisSession != null && thisSession.user_info != null) {
            alert_user_info = (Properties)thisSession.user_info.clone();
            if (thisSession.checkHackUsernames(the_user)) {
                is_hack_username = true;
            }
        }
        if (user == null && (user = ut.getUser(serverGroup, the_user, true)) == null) {
            Log.log("SERVER", 0, "Skipping alert trigger due to invalid user profile attempted:" + the_user);
        }
        if (!is_hack_username && user != null) {
            String msg = "ALERT:Repeated login failure : User :" + serverGroup + "/" + the_user;
            Log.log("SERVER", 0, String.valueOf(msg) + " Max failures:" + user.getProperty("failure_count_max", "0") + " Current count:" + user.getProperty("failure_count", "0"));
            if (!info.containsKey("alert_msg")) {
                info.put("alert_msg", msg);
            }
            if (!info.containsKey("alert_msg2")) {
                info.put("alert_msg2", "login failures and username is now disabled");
            }
            user.put("username", the_user);
            user.put("user_name", the_user);
            info.put("username", the_user);
            info.put("user_name", the_user);
            if (alert_user_info != null) {
                alert_user_info.put("username", the_user);
                alert_user_info.put("user_name", the_user);
            }
            AlertTools.runAlerts(alert_ype, info, alert_user_info, user, null, null, com.crushftp.client.Common.dmz_mode);
        } else {
            Log.log("SERVER", 0, "Skipping alert trigger due to hack username for repeated login failure alert:" + the_user);
        }
    }

    public void check_login_count_max(Properties user, String serverGroup, String the_user, String user_ip, String user_port) {
        block7: {
            if (user != null && !user.getProperty("failure_count_max", "0").equals("0") && !user.getProperty("failure_count_max", "0").equals("")) {
                Log.log("USER_OBJ", 1, "Login failed, check for disabling the account.");
                int failure_count = 0;
                if (!user.getProperty("failure_count", "0").equals("")) {
                    failure_count = Integer.parseInt(user.getProperty("failure_count", "0"));
                }
                ++failure_count;
                int max = Integer.parseInt(user.getProperty("failure_count_max", "0"));
                if (max < 0) {
                    this.put_in_user(serverGroup, the_user, "failure_count_max", String.valueOf(max *= -1), true, true);
                }
                this.put_in_user(serverGroup, the_user, "failure_count", String.valueOf(failure_count), true, true);
                try {
                    throw new RuntimeException(String.valueOf(serverGroup) + "/" + the_user + " failure count incremented:" + failure_count);
                }
                catch (Exception e) {
                    Log.log("USER_OBJ", 1, e);
                    if (failure_count < max) break block7;
                    this.put_in_user(serverGroup, the_user, "max_logins", "-1", true, true);
                    if (user.getProperty("disabled_account_task", "").equals("")) break block7;
                    final Properties user_f = user;
                    Properties user_info = new Properties();
                    user_info.put("user_ip", user_ip);
                    user_info.put("user_port", String.valueOf(user_port));
                    final Properties user_info_f = user_info;
                    final int failure_count_f = failure_count;
                    final String the_user_f = the_user;
                    try {
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                Properties event = new Properties();
                                event.put("event_plugin_list", user_f.getProperty("disabled_account_task", ""));
                                event.put("name", "DisabledUser:" + the_user_f + ":" + failure_count_f);
                                Vector<Properties> items = new Vector<Properties>();
                                items.addElement(user_f);
                                user_f.put("url", "virtual://user/" + the_user_f);
                                Properties info = new Properties();
                                info.put("user", user_f);
                                info.put("user_info", user_info_f);
                                ServerStatus.thisObj.events6.doEventPlugin(info, event, null, items);
                            }
                        });
                    }
                    catch (Exception e2) {
                        Log.log("SERVER", 0, e2);
                    }
                }
            }
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean validateMd4(String the_user, String the_password, String user_hashed_pass) throws Exception {
        String[] parts = the_password.split(":");
        String alg = parts[1];
        String domain = new String(Base64.decode(parts[2]), "UTF8");
        byte[] challenge = Base64.decode(parts[3]);
        byte[] encpass1 = Base64.decode(parts[4]);
        if (!user_hashed_pass.toUpperCase().startsWith("MD4:")) {
            if ((user_hashed_pass = ServerStatus.thisObj.common_code.decode_pass(user_hashed_pass)).equals("") || user_hashed_pass.startsWith("MD5:") || user_hashed_pass.startsWith("BCRYPT:") || user_hashed_pass.startsWith("SHA:") || user_hashed_pass.startsWith("SHA512:") || user_hashed_pass.startsWith("SHA256:") || user_hashed_pass.startsWith("SHA3:") || user_hashed_pass.startsWith("3CRYPT:") || user_hashed_pass.startsWith("MD5CRYPT:") || user_hashed_pass.startsWith("PBKDF2SHA256:") || user_hashed_pass.startsWith("MD5S2:") || user_hashed_pass.startsWith("SHA512CRYPT:") || user_hashed_pass.startsWith("ARGOND:")) {
                String md4_user;
                Properties md4_hashes = (Properties)ServerStatus.thisObj.server_info.get("md4_hashes");
                if (md4_hashes == null) {
                    md4_hashes = new Properties();
                }
                if (md4_hashes.getProperty(md4_user = ServerStatus.thisObj.common_code.encode_pass(the_user, "MD4", "").substring("MD4:".length()), "").equals("")) return false;
                user_hashed_pass = "MD4:" + md4_hashes.getProperty(md4_user, "");
            } else {
                user_hashed_pass = ServerStatus.thisObj.common_code.encode_pass(user_hashed_pass, "MD4", "");
            }
        }
        String tp = user_hashed_pass.substring("MD4:".length());
        byte[] md4pass = Base64.decode(tp);
        byte[] encpass2 = null;
        if (alg.equals("1") && encpass1.length == 64) {
            byte[] b = encpass1;
            encpass1 = new byte[encpass1.length];
            Mac hmacMD5 = Mac.getInstance("HMACMD5");
            SecretKeySpec key = new SecretKeySpec(md4pass, 0, md4pass.length, "MD5");
            hmacMD5.init(key);
            byte[] h2 = hmacMD5.doFinal((String.valueOf(the_user.toUpperCase()) + domain).getBytes("UnicodeLittleUnmarked"));
            byte[] b2 = new byte[b.length - 8];
            System.arraycopy(challenge, 0, b2, 0, 8);
            System.arraycopy(b, 16, b2, 8, b.length - 16);
            hmacMD5 = Mac.getInstance("HMACMD5");
            hmacMD5.init(new SecretKeySpec(h2, 0, h2.length, "MD5"));
            encpass2 = hmacMD5.doFinal(b2);
            encpass1 = new byte[16];
            System.arraycopy(b, 0, encpass1, 0, encpass1.length);
        } else if (alg.equals("1")) {
            byte[] p21 = new byte[21];
            System.arraycopy(md4pass, 0, p21, 0, md4pass.length);
            encpass2 = DesEncrypter.blockEncrypt(p21, challenge);
        } else if (alg.equals("2")) {
            Mac hmacMD5 = Mac.getInstance("HMACMD5");
            SecretKeySpec key = new SecretKeySpec(md4pass, 0, md4pass.length, "MD5");
            hmacMD5.init(key);
            encpass2 = hmacMD5.doFinal((String.valueOf(the_user.toUpperCase()) + domain).getBytes("UnicodeLittleUnmarked"));
        }
        boolean ok = encpass1.length == encpass2.length;
        int x = 0;
        while (x < encpass2.length && ok) {
            if (encpass1[x] != encpass2[x]) {
                ok = false;
            }
            ++x;
        }
        return ok;
    }

    public static String get_real_path_to_user(String serverGroup, String username) {
        return XMLUsers.findUser(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/", username);
    }

    public static void addFolder(String serverGroup, String username, String path, String name) {
        UserTools.addFolder(serverGroup, username, path, name, true);
    }

    public static void addFolder(String serverGroup, String username, String path, String name, boolean replicate) {
        up.addFolder(serverGroup, username, path, name);
        Properties p = new Properties();
        p.put("serverGroup", serverGroup);
        p.put("username", username);
        p.put("path", path);
        p.put("name", name);
        if (replicate) {
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.handlers.addFolder", "info", p);
        }
    }

    public static void addItem(String serverGroup, String username, String path, String name, String url, String type, Properties moreItems, boolean encrypted, String encrypted_class) throws Exception {
        UserTools.addItem(serverGroup, username, path, name, url, type, moreItems, encrypted, encrypted_class, true);
    }

    public static void addItem(String serverGroup, String username, String path, String name, String url, String type, Properties moreItems, boolean encrypted, String encrypted_class, boolean replicate) throws Exception {
        up.addItem(serverGroup, username, path, name, url, type, moreItems, encrypted, encrypted_class);
        Properties p = new Properties();
        p.put("serverGroup", serverGroup);
        p.put("username", username);
        p.put("path", path);
        p.put("name", name);
        p.put("url", url);
        p.put("type", type);
        p.put("moreItems", moreItems);
        p.put("encrypted", String.valueOf(encrypted));
        p.put("encrypted_class", encrypted_class);
        if (replicate) {
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.handlers.addItem", "info", p);
        }
        UserTools.sync_vfs_cache(serverGroup, username, ut.getVirtualVFS(serverGroup, username));
    }

    public static String addPriv(String serverGroup, String username, String path, String priv, int homeIndex, VFS tempVFS) {
        try {
            Properties item = tempVFS.get_item(path);
            if (item.getProperty("type", "DIR").equalsIgnoreCase("DIR") && !path.endsWith("/")) {
                path = String.valueOf(path) + "/";
            }
            if (item.getProperty("is_virtual", "").equals("true") && priv.indexOf("(write)") >= 0 && item.getProperty("VFS_real_path", "").equals("")) {
                return LOC.G("Cannot allow write access to virtual folder.");
            }
            if (item.getProperty("is_virtual", "").equals("true") && priv.indexOf("(makedir)") >= 0 && item.getProperty("VFS_real_path", "").equals("")) {
                return LOC.G("Cannot allow make directory access to a virtual folder.");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (priv.indexOf("(inherited)") >= 0) {
            priv = Common.replace_str(priv, "(inherited)", "");
        }
        ((Properties)tempVFS.permissions.elementAt(homeIndex)).put(UserTools.getUpper(path), priv);
        if (priv.trim().equals("")) {
            ((Properties)tempVFS.permissions.elementAt(homeIndex)).remove(UserTools.getUpper(path));
        }
        UserTools.writeVFS(serverGroup, username, tempVFS);
        return "";
    }

    public static void updatePrivpath(String serverGroup, String username, String oldPath, String newPath, Properties item, String privs, VFS tempVFS) {
        String priv = "";
        try {
            if (item == null) {
                item = tempVFS.get_item(oldPath);
            }
            if (item.getProperty("type", "DIR").equalsIgnoreCase("DIR") && !newPath.endsWith("/")) {
                newPath = String.valueOf(newPath) + "/";
            }
            priv = item.getProperty("privs", "");
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (privs != null) {
            priv = privs;
        }
        if (priv.indexOf("(inherited)") < 0) {
            priv = Common.replace_str(priv, "(inherited)", "");
            ((Properties)tempVFS.permissions.elementAt(0)).remove(UserTools.getUpper(oldPath));
            ((Properties)tempVFS.permissions.elementAt(0)).remove(String.valueOf(UserTools.getUpper(oldPath)) + "/");
            ((Properties)tempVFS.permissions.elementAt(0)).put(UserTools.getUpper(newPath), priv);
            ((Properties)tempVFS.permissions.elementAt(0)).put(String.valueOf(UserTools.getUpper(newPath)) + "/", priv);
            UserTools.writeVFS(serverGroup, username, tempVFS);
        }
    }

    public static void writeVFS(String serverGroup, String username, VFS uVFS) {
        UserTools.writeVFS(serverGroup, username, (Properties)uVFS.homes.elementAt(0));
    }

    public static void writeVFS(String serverGroup, String username, Properties virtual) {
        UserTools.writeVFS(serverGroup, username, virtual, true);
    }

    public static void writeVFS(String serverGroup, String username, Properties virtual, boolean replicate) {
        UserTools.writeVFS(serverGroup, username, virtual, replicate, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void writeVFS(String serverGroup, String username, Properties virtual, boolean replicate, Properties request) {
        Object object = UserTools.get_user_lock(serverGroup, username);
        synchronized (object) {
            up.writeVFS(serverGroup, username, (Properties)com.crushftp.client.Common.CLONE(virtual));
            Properties p = new Properties();
            p.put("serverGroup", serverGroup);
            p.put("username", username);
            p.put("virtual", virtual);
            if (request != null) {
                p.put("request", request);
            }
            if (replicate) {
                SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.handlers.writeVFS", "info", p);
            }
            UserTools.sync_vfs_cache(serverGroup, username, virtual);
        }
    }

    public static String getUpper(String path) {
        String path2 = path;
        if (System.getProperty("crushftp.priv_upper", "true").equals("true")) {
            path2 = path2.toUpperCase();
        }
        return path2;
    }

    public static void loadPermissions(VFS tempVFS) {
    }

    public static Vector vItemLoad(String path) {
        Vector v = (Vector)Common.readXMLObject(path);
        if (v != null) {
            int x = 0;
            while (x < v.size()) {
                Properties p = (Properties)v.elementAt(x);
                if (p.getProperty("encrypted", "false").equals("true")) {
                    if (!p.getProperty("encrypted_class", "").trim().equals("")) {
                        try {
                            Class<?> c = ServerStatus.clasLoader.loadClass(p.getProperty("encrypted_class").trim());
                            Constructor<?> cons = c.getConstructor(new Properties().getClass(), new String().getClass());
                            cons.newInstance(p, "decrypt");
                        }
                        catch (Exception e) {
                            Log.log("USER_OBJ", 1, e);
                        }
                    } else {
                        p.put("url", new Common().decode_pass(p.getProperty("url")));
                    }
                }
                ++x;
            }
        }
        return v;
    }

    public Vector get_virtual_list_fake(VFS tempVFS, String path, String serverGroup, String parentUser) {
        Vector<String> listing = new Vector<String>();
        try {
            try {
                tempVFS.getListing(listing, path);
                if (parentUser != null) {
                    boolean ok = false;
                    int x = 0;
                    while (x < listing.size()) {
                        Properties p = (Properties)listing.elementAt(x);
                        ok = UserTools.parentPathOK(serverGroup, parentUser, (p = tempVFS.get_item(String.valueOf(p.getProperty("root_dir")) + p.getProperty("name"))).getProperty("url"));
                        if (ok) break;
                        ++x;
                    }
                    if (!ok) {
                        listing = new Vector();
                    }
                }
            }
            catch (Exception e) {
                listing.addElement("" + e);
                Log.log("USER_OBJ", 1, e);
                if (tempVFS != null) {
                    tempVFS.disconnect();
                }
            }
        }
        finally {
            if (tempVFS != null) {
                tempVFS.disconnect();
            }
        }
        return listing;
    }

    public static boolean testLimitedAdminAccess(Object o, String parentUser, String serverGroup) throws Exception {
        boolean ok;
        block14: {
            ok = true;
            if (o != null) break block14;
            return true;
        }
        try {
            Vector<Object> v = null;
            if (o instanceof Properties) {
                v = new Vector<Object>();
                v.addElement(o);
            } else {
                v = (Vector<Object>)o;
            }
            int x = 0;
            while (x < v.size()) {
                Properties item = (Properties)v.elementAt(x);
                if (item.containsKey("url") && !UserTools.parentPathOK(serverGroup, parentUser, item.getProperty("url"))) {
                    ok = false;
                }
                if (item.containsKey("events")) {
                    Vector vv = (Vector)item.get("events");
                    int xx = 0;
                    while (xx < vv.size()) {
                        Properties event = (Properties)vv.elementAt(xx);
                        if (!event.getProperty("event_plugin_list", "").toUpperCase().startsWith("JOB:") && event.getProperty("event_action_list", "").indexOf("plugin") >= 0) {
                            try {
                                if (!event.getProperty("pluginName", "").equals("CrushTask")) {
                                    throw new Exception("Only CrushTask plugin allowed as an event.");
                                }
                                Properties request = new Properties();
                                request.put("calling_linkedServer", serverGroup);
                                request.put("calling_user", parentUser);
                                UserTools.testLimitedTasks(event, request);
                            }
                            catch (Exception e) {
                                Log.log("USER_OBJ", 0, e);
                                throw e;
                            }
                        }
                        ++xx;
                    }
                }
                Properties parent_user = ut.getUser(serverGroup, parentUser, true);
                if (!item.getProperty("admin_group_name", "").equals("")) {
                    throw new Exception("Cannot change user can administer group name.");
                }
                ok &= UserTools.checkSite(item, parent_user, "(CONNECT", true);
                ok &= UserTools.checkSite(item, parent_user, "(USER_ADMIN", true);
                ok &= UserTools.checkSite(item, parent_user, "(SITE_QUIT", true);
                ok &= UserTools.checkSite(item, parent_user, "(SITE_KICK", true);
                ok &= UserTools.checkSite(item, parent_user, "(SITE_KICKBAN", true);
                ok &= UserTools.checkSite(item, parent_user, "(SITE_USERS", true);
                ok &= UserTools.checkSite(item, parent_user, "(SITE_PLUGIN", true);
                ok &= UserTools.checkSite(item, parent_user, "(USER_EDIT)", false);
                ok &= UserTools.checkSite(item, parent_user, "(USER_VIEW)", false);
                ok &= UserTools.checkSite(item, parent_user, "(JOB_EDIT)", false);
                ok &= UserTools.checkSite(item, parent_user, "(JOB_VIEW)", false);
                ok &= UserTools.checkSite(item, parent_user, "(JOB_RUN)", false);
                ok &= UserTools.checkSite(item, parent_user, "(JOB_MONITOR)", false);
                ok &= UserTools.checkSite(item, parent_user, "(JOB_LIST_HISTORY)", false);
                ok &= UserTools.checkSite(item, parent_user, "(JOB_", false);
                ok &= UserTools.checkSite(item, parent_user, "(SERVER_VIEW)", false);
                ok &= UserTools.checkSite(item, parent_user, "(SERVER_EDIT", false);
                ok &= UserTools.checkSite(item, parent_user, "(LOG_ACCESS)", false);
                ok &= UserTools.checkSite(item, parent_user, "(LOG_", false);
                ok &= UserTools.checkSite(item, parent_user, "(PREF_EDIT)", false);
                ok &= UserTools.checkSite(item, parent_user, "(PREF_VIEW)", false);
                ok &= UserTools.checkSite(item, parent_user, "(PREF_", false);
                ok &= UserTools.checkSite(item, parent_user, "(REPORT_VIEW)", false);
                ok &= UserTools.checkSite(item, parent_user, "(REPORT_EDIT)", false);
                ok &= UserTools.checkSite(item, parent_user, "(REPORT_", false);
                ok &= UserTools.checkSite(item, parent_user, "(SHARE_EDIT)", false);
                ok &= UserTools.checkSite(item, parent_user, "(SHARE_VIEW)", false);
                ok &= UserTools.checkSite(item, parent_user, "(SHARE_", false);
                ok &= UserTools.checkSite(item, parent_user, "(UPDATE_RUN)", true);
                ++x;
            }
        }
        catch (Exception e) {
            Log.log("USER_OBJ", 0, e);
            throw e;
        }
        return ok;
    }

    public static boolean checkSite(Properties user, Properties parent, String s, boolean never) throws Exception {
        if (never && user.getProperty("site", "").toUpperCase().indexOf(s.toUpperCase()) >= 0) {
            throw new Exception(s);
        }
        if (user.getProperty("site", "").toUpperCase().indexOf(s.toUpperCase()) >= 0 && parent.getProperty("site", "").toUpperCase().indexOf(s.toUpperCase()) < 0) {
            throw new Exception(s);
        }
        if (user.getProperty("site", "").toUpperCase().indexOf(s.toUpperCase()) >= 0 && parent.getProperty("site", "").toUpperCase().indexOf(s.toUpperCase()) >= 0) {
            return true;
        }
        if (user.getProperty("site", "").toUpperCase().indexOf(s.toUpperCase()) < 0) {
            return true;
        }
        return true;
    }

    public static void testLimitedTasks(Properties job, Properties request) throws Exception {
        Vector tasks = (Vector)job.get("tasks");
        if (tasks != null) {
            int x = tasks.size() - 1;
            while (x >= 0) {
                Properties task = (Properties)tasks.elementAt(x);
                if (task.getProperty("type").equalsIgnoreCase("Execute")) {
                    tasks.remove(x);
                } else if (task.getProperty("type").equalsIgnoreCase("Java")) {
                    tasks.remove(x);
                } else if (task.getProperty("type").equalsIgnoreCase("Tunnel")) {
                    tasks.remove(x);
                } else if (task.getProperty("type").equalsIgnoreCase("Link")) {
                    tasks.remove(x);
                } else if (task.getProperty("type").equalsIgnoreCase("UsersList")) {
                    tasks.remove(x);
                } else if (task.getProperty("type").equalsIgnoreCase("Custom")) {
                    tasks.remove(x);
                } else {
                    try {
                        UserTools.testLimitedUrl(request.getProperty("calling_linkedServer"), request.getProperty("calling_user"), task, "findUrl");
                        UserTools.testLimitedUrl(request.getProperty("calling_linkedServer"), request.getProperty("calling_user"), task, "copyUniqueName");
                        UserTools.testLimitedUrl(request.getProperty("calling_linkedServer"), request.getProperty("calling_user"), task, "temp_name");
                        UserTools.testLimitedUrl(request.getProperty("calling_linkedServer"), request.getProperty("calling_user"), task, "destPath");
                        UserTools.testLimitedUrl(request.getProperty("calling_linkedServer"), request.getProperty("calling_user"), task, "rename_after_copy");
                        UserTools.testLimitedUrl(request.getProperty("calling_linkedServer"), request.getProperty("calling_user"), task, "filePath");
                        UserTools.testLimitedUrl(request.getProperty("calling_linkedServer"), request.getProperty("calling_user"), task, "cache_folder");
                        UserTools.testLimitedUrl(request.getProperty("calling_linkedServer"), request.getProperty("calling_user"), task, "destUrl");
                        UserTools.testLimitedUrl(request.getProperty("calling_linkedServer"), request.getProperty("calling_user"), task, "mail_path");
                        UserTools.testLimitedUrl(request.getProperty("calling_linkedServer"), request.getProperty("calling_user"), task, "newName");
                        UserTools.testLimitedUrl(request.getProperty("calling_linkedServer"), request.getProperty("calling_user"), task, "varValue");
                    }
                    catch (Exception e) {
                        throw new Exception(String.valueOf(e.getMessage()) + "\r\n\r\nTask item failed due to limited admin violation.");
                    }
                }
                --x;
            }
        }
    }

    public static void testLimitedUrl(String linkedServer, String username, Properties task, String key) throws Exception {
        String s = task.getProperty(key, "").trim();
        if (s.equals("")) {
            return;
        }
        if (s.indexOf("decode_start") >= 0 || s.indexOf("chop_start") >= 0 || s.indexOf("htmlclean") >= 0 || s.indexOf("last_start") >= 0 || s.indexOf("substring_start") >= 0 || s.indexOf("split_start") >= 0 || s.indexOf("replace_start") >= 0 || s.indexOf("url_start") >= 0 || s.indexOf("group_start") >= 0 || s.indexOf("..") >= 0) {
            throw new Exception(String.valueOf(task.getProperty("connectionID")) + ":" + task.getProperty("type") + ":" + task.getProperty("name") + ":" + key + ":" + s);
        }
        VRL vrl = new VRL(s);
        if (vrl.getProtocol().equalsIgnoreCase("file") && !UserTools.parentPathOK(linkedServer, username, vrl.toString())) {
            throw new Exception(String.valueOf(task.getProperty("connectionID")) + ":" + task.getProperty("type") + ":" + task.getProperty("name") + ":" + key + ":" + vrl.toString());
        }
    }

    public static boolean parentPathOK(String serverGroup, String parentUser, String url) {
        if (parentUser == null) {
            return true;
        }
        if (!url.toUpperCase().startsWith("FILE:/")) {
            return true;
        }
        Vector listing2 = new Vector();
        VFS tempVFS = null;
        try {
            tempVFS = VFS.getVFS(up.buildVFS(serverGroup, parentUser));
            tempVFS.getListing(listing2, "/");
            int xx = 0;
            while (xx < listing2.size()) {
                Properties pp = (Properties)listing2.elementAt(xx);
                pp = tempVFS.get_item(String.valueOf(pp.getProperty("root_dir")) + pp.getProperty("name"));
                String url1 = Common.replace_str(url.toUpperCase(), "FILE:///", "FILE:/");
                String url2 = Common.replace_str(pp.getProperty("url").toUpperCase(), "FILE:///", "FILE:/");
                url1 = Common.url_decode(Common.replace_str(url1, "FILE://", "FILE:/"));
                url2 = Common.url_decode(Common.replace_str(url2, "FILE://", "FILE:/"));
                Log.log("USER_OBJ", 2, "Comparing urls...url1=" + url1 + "   url2=" + url2);
                if (url1.startsWith(url2)) {
                    return true;
                }
                try {
                    ++xx;
                    continue;
                }
                catch (Exception e) {
                    Log.log("USER_OBJ", 1, e);
                }
                break;
            }
        }
        finally {
            if (tempVFS != null) {
                tempVFS.disconnect();
            }
        }
        return false;
    }

    public static Properties waitResponse(Properties p, int timeout) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < (long)(timeout * 1000)) {
            if (p.getProperty("status", "").equals("done")) {
                return p;
            }
            try {
                Thread.sleep(10L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        Properties p2 = (Properties)p.clone();
        if (p2.containsKey("password")) {
            p2.put("password", "*******");
        }
        if (p2.containsKey("reset_token")) {
            p2.put("reset_token", "*******");
        }
        Log.log("DMZ", 0, "Timeout waiting for response (" + timeout + "):" + p2);
        return null;
    }

    /*
     * Unable to fully structure code
     */
    public static Vector buildPublicKeys(String username, Properties user, String serverGroup) throws IOException {
        block63: {
            keyStr = user.getProperty("ssh_public_keys", "");
            if (keyStr.indexOf("://") < 0) {
                Log.log("SSH_SERVER", 2, keyStr);
            } else {
                Log.log("SSH_SERVER", 2, "Found URL public key references...");
            }
            if ((keyStr.trim().equalsIgnoreCase("DMZ") || keyStr.trim().startsWith("DMZ:")) && com.crushftp.client.Common.dmz_mode) {
                queue = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.queue");
                action = new Properties();
                action.put("type", "GET:USER_SSH_KEYS");
                action.put("id", Common.makeBoundary());
                action.put("username", username);
                action.put("need_response", "true");
                if (keyStr.trim().startsWith("DMZ:")) {
                    action.put("linkedServer", keyStr.substring(keyStr.indexOf(":") + 1).trim());
                } else {
                    try {
                        serverGroup2 = serverGroup;
                        username2 = username;
                        if (serverGroup2.equals("@AutoDomain") && username2.indexOf("@") > 0) {
                            newLinkedServer = username2.split("@")[username2.split("@").length - 1];
                            newLinkedServer2 = com.crushftp.client.Common.dots(newLinkedServer);
                            if (newLinkedServer.equals(newLinkedServer2 = newLinkedServer2.replace('/', '-').replace('\\', '-').replace('%', '-').replace(':', '-').replace(';', '-'))) {
                                username2 = username2.substring(0, username2.lastIndexOf("@"));
                                serverGroup2 = newLinkedServer;
                            }
                        }
                        action.put("username", username2);
                        template_vfs = UserTools.ut.getVirtualVFS(serverGroup2, "template");
                        p0 = (Properties)template_vfs.get("/internal");
                        if (p0 == null) {
                            p0 = (Properties)template_vfs.get("/Internal");
                        }
                        if (p0 == null) {
                            p0 = (Properties)template_vfs.get("/INTERNAL");
                        }
                        if (p0 == null) {
                            p0 = (Properties)template_vfs.get("/internal1");
                        }
                        if (p0 == null) {
                            p0 = (Properties)template_vfs.get("/INTERNAL1");
                        }
                        if (p0 == null) {
                            p0 = (Properties)template_vfs.get("/Internal1");
                        }
                        if (p0 != null) {
                            v = (Vector)p0.get("vItems");
                            p1 = (Properties)v.elementAt(0);
                            action.put("preferred_port", String.valueOf(new VRL(p1.getProperty("url")).getPort()));
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 2, e);
                        action.put("linkedServer", "MainUsers");
                    }
                }
                queue.addElement(action);
                action = UserTools.waitResponse(action, 60);
                Log.log("SSH_SERVER", 2, "GET:USER_SSH_KEYS:Got response.." + action);
                if (action != null && action.containsKey("public_keys")) {
                    return (Vector)action.get("public_keys");
                }
                return new Vector<E>();
            }
            br = new BufferedReader(new StringReader(keyStr));
            s = "";
            keysVec = new Vector<String>();
            simpleUsername = username;
            if (simpleUsername.indexOf("\\") >= 0) {
                simpleUsername = simpleUsername.substring(simpleUsername.indexOf("\\") + 1);
            }
            if (simpleUsername.indexOf("/") >= 0) {
                simpleUsername = simpleUsername.substring(simpleUsername.indexOf("/") + 1);
            }
            if (simpleUsername.startsWith("$ASCII$")) {
                simpleUsername = simpleUsername.substring("$ASCII$".length());
            }
            Log.log("SSH_SERVER", 2, "publicKey_username:" + simpleUsername);
            if (keyStr.toUpperCase().indexOf("SSH2 PUBLIC KEY") < 0 && keyStr.indexOf(";;;") < 0) ** GOTO lbl156
            keys = keyStr.split(";;;");
            x = 0;
            while (x < keys.length) {
                if (!keys[x].trim().equals("")) {
                    keysVec.addElement(keys[x].trim());
                }
                ++x;
            }
            break block63;
lbl-1000:
            // 1 sources

            {
                s = s.trim();
                Log.log("SSH_SERVER", 2, new VRL(s).safe());
                if (s.endsWith("/") || s.endsWith("\\")) {
                    Log.log("SSH_SERVER", 2, "Updating username variable in public key if found...");
                    s = Common.replace_str(s, "{username}", simpleUsername);
                    s = Common.replace_str(s, "{user_name}", simpleUsername);
                    try {
                        s = com.crushftp.client.Common.textFunctions(s, "{", "}");
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                    }
                    if (s.toLowerCase().startsWith("file:/")) {
                        s = new VRL(s).getCanonicalPath();
                    }
                    if (new File_S(s).exists()) {
                        files = (File_S[])new File_S(s).listFiles();
                        if (files == null) continue;
                        x = 0;
                        while (x < files.length) {
                            Log.log("SSH_SERVER", 2, files[x].toString());
                            if (files[x].length() < 512000L && !files[x].getName().toUpperCase().startsWith(".DS_") && (files[x].getName().toUpperCase().startsWith(String.valueOf(simpleUsername.toUpperCase()) + "_") || files[x].getName().toUpperCase().equals(simpleUsername.toUpperCase()) || files[x].getName().toUpperCase().equals(String.valueOf(simpleUsername.toUpperCase()) + ".PUB"))) {
                                keysVec.addElement(files[x].getPath());
                            }
                            ++x;
                        }
                        continue;
                    }
                    key_vrl = new VRL(s);
                    if (key_vrl.getProtocol().equalsIgnoreCase("FILE")) continue;
                    c_key = com.crushftp.client.Common.getClient(Common.getBaseUrl(key_vrl.toString()), System.getProperty("appname", "CrushFTP"), new Vector<E>());
                    try {
                        try {
                            c_key.login(key_vrl.getUsername(), key_vrl.getPassword(), "");
                            v = new Vector<E>();
                            c_key.list(key_vrl.getPath(), v);
                            while (v.size() > 0) {
                                p = (Properties)v.remove(0);
                                if (Long.parseLong(p.getProperty("size", "0")) >= 512000L || p.getProperty("name", "").startsWith(".") || !p.getProperty("name", "").toUpperCase().startsWith(String.valueOf(simpleUsername.toUpperCase()) + "_") && !p.getProperty("name", "").toUpperCase().equals(simpleUsername.toUpperCase()) && !p.getProperty("name", "").toUpperCase().equals(String.valueOf(simpleUsername.toUpperCase()) + ".PUB")) continue;
                                keysVec.addElement(p.getProperty("url"));
                            }
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 1, e);
                            try {
                                c_key.logout();
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 1, e);
                            }
                            continue;
                        }
                    }
                    catch (Throwable var12_36) {
                        try {
                            c_key.logout();
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 1, e);
                        }
                        throw var12_36;
                    }
                    try {
                        c_key.logout();
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                    }
                    continue;
                }
                keysVec.addElement(s);
lbl156:
                // 9 sources

                ** while ((s = br.readLine()) != null)
            }
        }
        x = 0;
        while (x < keysVec.size()) {
            block62: {
                data = ServerStatus.change_vars_to_values_static(keysVec.elementAt(x).toString(), user, user, null);
                data = Common.replace_str(data, "{username}", simpleUsername);
                if ((data = Common.replace_str(data, "{user_name}", simpleUsername)).toLowerCase().startsWith("file:/")) {
                    data = new VRL(data).getCanonicalPath();
                }
                try {
                    data = com.crushftp.client.Common.textFunctions(data, "{", "}");
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
                if (new File_S(data).exists() && new File_S(data).isFile()) {
                    in = null;
                    try {
                        in = new RandomAccessFile(new File_S(data), "r");
                        b = new byte[(int)in.length()];
                        in.readFully(b);
                        key_data = new String(b, "UTF8");
                        key_data = String.valueOf(key_data) + "!!!ssh_key_info!!!" + data;
                        keysVec.setElementAt(key_data, x);
                    }
                    finally {
                        in.close();
                    }
                }
                if (data.indexOf("BEGIN SSH2") < 0 && !(key_vrl = new VRL(data)).getProtocol().equalsIgnoreCase("FILE")) {
                    baos_key = new ByteArrayOutputStream();
                    c_key = com.crushftp.client.Common.getClient(Common.getBaseUrl(key_vrl.toString()), System.getProperty("appname", "CrushFTP"), new Vector<E>());
                    try {
                        try {
                            c_key.login(key_vrl.getUsername(), key_vrl.getPassword(), "");
                            Common.streamCopier(c_key.download(key_vrl.getPath(), 0L, -1L, true), baos_key, false, true, true);
                            key_data = new String(baos_key.toByteArray(), "UTF8");
                            key_data = String.valueOf(key_data) + "!!!ssh_key_info!!!" + data;
                            keysVec.setElementAt(key_data, x);
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 1, e);
                            try {
                                c_key.logout();
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 1, e);
                            }
                            break block62;
                        }
                    }
                    catch (Throwable var14_42) {
                        try {
                            c_key.logout();
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 1, e);
                        }
                        throw var14_42;
                    }
                    try {
                        c_key.logout();
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 1, e);
                    }
                }
            }
            ++x;
        }
        keys = com.crushftp.client.Common.System2.keys();
        while (keys.hasMoreElements()) {
            propKey = keys.nextElement().toString();
            if (!propKey.startsWith("j2ssh.publickeys.") || !propKey.startsWith("j2ssh.publickeys." + username.toUpperCase() + "_") && !propKey.equals("j2ssh.publickeys." + username.toUpperCase())) continue;
            p = (Properties)com.crushftp.client.Common.System2.get(propKey);
            b = (byte[])p.get("bytes");
            key_data = new String(b);
            key_data = String.valueOf(key_data) + "!!!ssh_key_info!!!" + propKey;
            keysVec.addElement(key_data);
        }
        return keysVec;
    }

    public static void addAnyPassToken(String token) {
        if (anyPassTokens.indexOf(token) < 0) {
            anyPassTokensTime.put(token, String.valueOf(System.currentTimeMillis()));
            anyPassTokens.insertElementAt(token, 0);
        }
    }

    public static boolean checkPassword(String pass) {
        if (pass == null) {
            return false;
        }
        int x = anyPassTokens.size() - 1;
        while (x >= 0) {
            String token = anyPassTokens.elementAt(x).toString();
            if (x > 0 && System.currentTimeMillis() - Long.parseLong(anyPassTokensTime.getProperty(token, "0")) > 28800000L) {
                anyPassTokens.remove(x);
                anyPassTokensTime.remove(token);
            }
            if (pass.equals(token)) {
                return true;
            }
            --x;
        }
        return false;
    }

    public static boolean isValidUsername(String username) {
        Vector server_groups = ServerStatus.VG("server_groups");
        int x = 0;
        while (x < server_groups.size()) {
            String serverGroup = server_groups.elementAt(x).toString();
            Vector user_list = new Vector();
            UserTools.refreshUserList(serverGroup, user_list);
            int xx = 0;
            while (xx < user_list.size()) {
                if (user_list.elementAt(xx).toString().equalsIgnoreCase(username)) {
                    return true;
                }
                ++xx;
            }
            ++x;
        }
        return false;
    }

    public static void removeUserFromGroups(String serverGroup, String username) {
        Properties groups = UserTools.getGroups(serverGroup);
        Enumeration<Object> keys = groups.keys();
        while (keys.hasMoreElements()) {
            String propKey = keys.nextElement().toString();
            Vector g = (Vector)groups.get(propKey);
            if (!g.contains(username)) continue;
            g.remove(username);
        }
        UserTools.writeGroups(serverGroup, groups);
    }

    public static void removeUserFromInheritance(String serverGroup, String username) {
        Properties inheritance = UserTools.getInheritance(serverGroup);
        inheritance.remove(username);
        Enumeration<Object> keys = inheritance.keys();
        while (keys.hasMoreElements()) {
            String propKey = keys.nextElement().toString();
            Vector inherit = (Vector)inheritance.get(propKey);
            if (!inherit.contains(username)) continue;
            inherit.remove(username);
        }
        UserTools.writeInheritance(serverGroup, inheritance);
    }

    public static void cacheEmailUsernames() {
        up.cacheEmailUsernames();
    }

    private static void sync_vfs_cache(String serverGroup, String username, Properties virtual) {
        Vector urls = Common.get_urls_from_VFS(virtual);
        Properties vfs_url_cache = (Properties)ServerStatus.thisObj.server_info.get("vfs_url_cache");
        if (vfs_url_cache != null) {
            int x = 0;
            while (x < urls.size()) {
                if (vfs_url_cache.containsKey(String.valueOf(serverGroup) + ":" + urls.get(x))) {
                    Vector users = (Vector)vfs_url_cache.get(String.valueOf(serverGroup) + ":" + urls.get(x));
                    if (!users.contains(username)) {
                        users.add(username);
                    }
                } else {
                    Vector<String> vfs_users = new Vector<String>();
                    vfs_users.add(username);
                    vfs_url_cache.put(String.valueOf(serverGroup) + ":" + urls.get(x), vfs_users);
                }
                ++x;
            }
            Enumeration<?> e_url_cache = vfs_url_cache.propertyNames();
            while (e_url_cache.hasMoreElements()) {
                String key = (String)e_url_cache.nextElement();
                String url_cache = key.substring(key.indexOf(":") + 1, key.length());
                if (urls.contains(url_cache)) continue;
                Vector users = (Vector)vfs_url_cache.get(key);
                if (users.contains(username)) {
                    users.remove(username);
                }
                if (!users.isEmpty()) continue;
                vfs_url_cache.remove(key);
            }
        }
    }

    public String getPreferredPort(String serverGroup, String username) {
        Properties template_vfs;
        Properties p0;
        if (serverGroup.equals("@AutoDomain") && username.indexOf("@") > 0) {
            String newLinkedServer = username.split("@")[username.split("@").length - 1];
            String newLinkedServer2 = com.crushftp.client.Common.dots(newLinkedServer);
            if (newLinkedServer.equals(newLinkedServer2 = newLinkedServer2.replace('/', '-').replace('\\', '-').replace('%', '-').replace(':', '-').replace(';', '-'))) {
                username = username.substring(0, username.lastIndexOf("@"));
                serverGroup = newLinkedServer;
            }
        }
        if ((p0 = (Properties)(template_vfs = ut.getVirtualVFS(serverGroup, "template")).get("/internal")) == null) {
            p0 = (Properties)template_vfs.get("/Internal");
        }
        if (p0 == null) {
            p0 = (Properties)template_vfs.get("/Internal1");
        }
        if (p0 == null) {
            p0 = (Properties)template_vfs.get("/INTERNAL");
        }
        if (p0 == null) {
            p0 = (Properties)template_vfs.get("/core");
        }
        if (p0 == null) {
            p0 = (Properties)template_vfs.get("/CORE");
        }
        if (p0 != null) {
            Vector v = (Vector)p0.get("vItems");
            Properties p1 = (Properties)v.elementAt(0);
            return String.valueOf(new VRL(p1.getProperty("url")).getPort());
        }
        return "";
    }

    private static Object get_user_lock(String serverGroup, String username) {
        Object user_lock = null;
        if (!users_lock.containsKey(String.valueOf(serverGroup) + "~" + username)) {
            user_lock = new Object();
            users_lock.put(String.valueOf(serverGroup) + "~" + username, user_lock);
        } else {
            user_lock = users_lock.get(String.valueOf(serverGroup) + "~" + username);
        }
        return user_lock;
    }

    public static String getSubscribeReverseNotificationEvents(String serverGroup, String username, Properties settings) throws Exception {
        if (com.crushftp.client.Common.dmz_mode) {
            return "";
        }
        Properties user = ut.getUser(serverGroup, username, true);
        if (user == null) {
            throw new Exception("User does not exists! This feature only available for real users (not plugin based users)!");
        }
        if (settings.getProperty("path", "").equals("") || settings.getProperty("path", "").equals("") || !settings.getProperty("path", "").endsWith("/")) {
            throw new Exception("Error : The given path is wrong!");
        }
        String privs = "";
        Vector events = (Vector)user.get("events");
        if (events != null) {
            String event_name = "subscribe" + Common.replace_str(settings.getProperty("path", ""), "/", "_");
            int x = 0;
            while (x < events.size()) {
                Properties event = (Properties)events.elementAt(x);
                if (event.getProperty("name", "").equals(event_name)) {
                    privs = event.getProperty("event_user_action_list", "").trim();
                    privs = Common.replace_str(privs, "r_", "");
                    return privs;
                }
                ++x;
            }
        }
        return "";
    }

    public static String saveSubscribeReverseNotificationEvents(String serverGroup, String username, Properties settings) throws Exception {
        if (com.crushftp.client.Common.dmz_mode) {
            return "";
        }
        Properties user_tmp = ut.getUser(serverGroup, username, true);
        if (user_tmp == null) {
            throw new Exception("User does not exists! This feature only available for real users (not plugin based users)!");
        }
        String permissions = settings.getProperty("privs", "").trim();
        if (!(permissions.equals("") || permissions.startsWith("(") && permissions.endsWith(")"))) {
            throw new Exception("Error : Wrong event permissions format!");
        }
        if (settings.getProperty("path", "").equals("") || settings.getProperty("path", "").equals("") || !settings.getProperty("path", "").endsWith("/")) {
            throw new Exception("Error : The given path is wrong!");
        }
        if (!user_tmp.getProperty("subscribe_reverse_notification_event", "").equals("true")) {
            throw new Exception("Error : Subscribe not supported!");
        }
        boolean save = false;
        Vector<Properties> events = (Vector<Properties>)com.crushftp.client.Common.CLONE(user_tmp.get("events"));
        if (events == null) {
            events = new Vector<Properties>();
            user_tmp.put("events", events);
            save = true;
        }
        permissions = Common.replace_str(permissions, "(", "(r_");
        String event_name = "subscribe" + Common.replace_str(settings.getProperty("path", ""), "/", "_");
        boolean event_exists = false;
        int x = 0;
        while (x < events.size()) {
            Properties event = (Properties)events.elementAt(x);
            if (event.getProperty("name", "").equals(event_name)) {
                event_exists = true;
                if (permissions.equals("")) {
                    events.remove(event);
                    save = true;
                    break;
                }
                String event_if_list = Common.replace_str(permissions, "r_", "");
                event_if_list = Common.replace_str(event_if_list, "makedir", "make");
                event_if_list = Common.replace_str(event_if_list, ")", "_dir)");
                if (event.getProperty("event_user_action_list", "").trim().length() != permissions.length() || event.getProperty("event_if_list", "").trim().length() != event_if_list.length()) {
                    event.put("event_user_action_list", permissions);
                    event.put("event_if_list", event_if_list);
                    if (event.getProperty("event_always_cb", "").equals("true")) {
                        event.put("event_always_cb", "false");
                    }
                    save = true;
                    break;
                }
                String[] event_privs = permissions.split("\\(r_");
                int xx = 0;
                while (xx < event_privs.length) {
                    String priv = event_privs[xx].trim();
                    if (!priv.equals("")) {
                        String if_list_priv = Common.replace_str(priv, "makedir", "make");
                        if_list_priv = Common.replace_str(priv, ")", "_dir)");
                        if (event.getProperty("event_user_action_list", "").trim().indexOf("(r_" + priv) < 0 || event.getProperty("event_if_list", "").trim().indexOf(if_list_priv) < 0) {
                            event.put("event_user_action_list", permissions);
                            event.put("event_if_list", event_if_list);
                            if (event.getProperty("event_always_cb", "").equals("true")) {
                                event.put("event_always_cb", "false");
                            }
                            save = true;
                            break;
                        }
                    }
                    ++xx;
                }
            }
            ++x;
        }
        if (!event_exists) {
            if (permissions.equals("")) {
                throw new Exception("Error : Select at least one user action!");
            }
            Properties event = null;
            int x2 = 0;
            while (x2 < events.size()) {
                Properties temp_event = (Properties)events.elementAt(x2);
                if (temp_event.getProperty("event_action_list", "").equals("(subscribe_reverse_notification_event_template)")) {
                    event = (Properties)com.crushftp.client.Common.CLONE(temp_event);
                }
                ++x2;
            }
            if (event == null) {
                throw new Exception("Could not found subscribe reverse notification event template !");
            }
            event.put("id", Common.makeBoundary(10));
            event.put("name", "subscribe" + Common.replace_str(settings.getProperty("path", ""), "/", "_"));
            event.put("event_action_list", "(run_plugin)");
            event.put("event_user_action_list", permissions);
            String event_if_list = Common.replace_str(permissions, "r_", "");
            event_if_list = Common.replace_str(event_if_list, "makedir", "make");
            event_if_list = Common.replace_str(event_if_list, ")", "_dir)");
            event.put("event_if_list", event_if_list);
            event.put("event_dir_data", settings.getProperty("path", ""));
            event.put("event_always_cb", "false");
            events.add(event);
            save = true;
        }
        if (save) {
            Properties user = ut.getUser(serverGroup, username, false);
            user.put("events", events);
            UserTools.writeUser(serverGroup, user_tmp.getProperty("user_name", user.getProperty("username", "")), user);
        }
        return "";
    }

    public static void addTemplateUserForDMZ(String serverGroup, String username) throws Exception {
        Properties p = new Properties();
        long current_date = System.currentTimeMillis();
        p.put("created_time", String.valueOf(current_date));
        p.put("updated_time", String.valueOf(current_date));
        p.put("root_dir", "/");
        p.put("max_logins", "0");
        p.put("version", "1.0");
        p.put("userVersion", "6");
        p.put("username", "template");
        p.put("ssh_public_keys", "DMZ");
        p.put("password", "");
        UserTools.writeUser(serverGroup, username, p);
        VFS vfs = ut.getVFS(serverGroup, username);
        UserTools.addPriv(serverGroup, username, "/INTERNAL/", "(read)(write)(view)(delete)(deletedir)(makedir)(rename)(resume)(share)", 0, vfs);
        Properties settings = new Properties();
        settings.put("use_dmz", "false");
        settings.put("multi", "false");
        settings.put("multi_segmented_download", "false");
        settings.put("haDownload", "false");
        settings.put("haUpload", "false");
        settings.put("read_timeout", "20000");
        settings.put("write_timeout", "20000");
        settings.put("timeout", "20000");
        UserTools.addItem(serverGroup, username, "/", "Internal", "HTTP://{username}:{password}@127.0.0.1:8080/", "DIR", new Properties(), false, "");
        ut.forceMemoryReload("template");
    }

    public static void disable_OTP(Properties user) {
        user.put("otp_auth", "false");
        user.put("twofactor_secret", "");
        Vector<Properties> v = (Vector<Properties>)user.get("web_customizations");
        if (v == null) {
            v = new Vector<Properties>();
            user.put("web_customizations", v);
        }
        boolean found = false;
        int x = 0;
        while (x < v.size()) {
            Properties settings = (Properties)v.get(x);
            if (settings.getProperty("key").equalsIgnoreCase("twofactor_force_google_enrollment")) {
                settings.put("value", "false");
                found = true;
            }
            ++x;
        }
        if (!found) {
            Properties settings = new Properties();
            settings.put("key", "twofactor_force_google_enrollment");
            settings.put("value", "false");
            v.add(settings);
        }
    }

    public VFS get_full_VFS(String serverGroup, String username, Properties user) {
        VFS vfs = this.getVFS(serverGroup, username);
        Vector linked_vfs = (Vector)user.get("linked_vfs");
        if (linked_vfs == null) {
            linked_vfs = new Vector();
        }
        int xx = 0;
        while (xx < linked_vfs.size()) {
            if (!linked_vfs.elementAt(xx).toString().trim().equals("")) {
                try {
                    VFS tempVFS = ut.getVFS(serverGroup, linked_vfs.elementAt(xx).toString());
                    vfs.addLinkedVFS(tempVFS);
                }
                catch (Exception e) {
                    Log.log("REPORT", 1, e);
                }
            }
            ++xx;
        }
        return vfs;
    }
}

