/*
 * Decompiled with CFR 0.152.
 */
package crushftp.user;

import com.crushftp.client.File_S;
import com.crushftp.client.VRL;
import crushftp.handlers.CIProperties;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.handlers.UserTools;
import crushftp.server.ServerStatus;
import crushftp.user.UserProvider;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class XMLUsers
extends UserProvider {
    public static transient Object user_lock = new Object();
    public static Properties user_case_lookup = new Properties();
    public static transient Object groupLoc = new Object();
    public static transient Object inheritanceLoc = new Object();
    public static Properties user_write_lock = new Properties();
    public static transient Object username_lookup_lock = new Object();
    public static transient Properties inheritance_groups_cache = new Properties();

    private static String getUserPath(String serverGroup, String username) {
        if (serverGroup.endsWith("_restored_backup")) {
            return String.valueOf(System.getProperty("crushftp.backup")) + "backup/" + username + "/";
        }
        String path = String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/" + username + "/";
        String path2 = user_case_lookup.getProperty(path.toUpperCase());
        if (!new File_S(path).exists() && (Common.machine_is_unix() || Common.machine_is_linux())) {
            if (path2 != null && new File_S(path2).exists()) {
                path = path2;
            } else {
                path2 = XMLUsers.findUser(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/", username);
                if (path2 != null) {
                    user_case_lookup.put(path.toUpperCase(), path2);
                    path = path2;
                }
            }
        }
        return path;
    }

    public static String findUser(String path, String username) {
        if (username.equals("")) {
            return null;
        }
        File_S[] list = (File_S[])new File_S(path).listFiles();
        int x = 0;
        while (list != null && x < list.length) {
            if (list[x].isDirectory() && (System.getProperty("crushftp.case_sensitive_user", "false").equals("true") ? list[x].getName().equals(username) : list[x].getName().equalsIgnoreCase(username))) {
                return String.valueOf(list[x].getPath()) + "/";
            }
            ++x;
        }
        if (System.getProperty("crushftp.recurse_user_search", "false").equals("false")) {
            return null;
        }
        x = 0;
        while (list != null && x < list.length) {
            if (list[x].isDirectory()) {
                if (list[x].getName().equalsIgnoreCase(username)) {
                    return String.valueOf(list[x].getPath()) + "/";
                }
                if (!list[x].getName().equalsIgnoreCase("VFS")) {
                    Log.log("USER_OBJ", 2, "Searching for user " + username + ", searching inside of user " + list[x].getName() + " located here:" + list[x].getPath() + "/");
                    String result = XMLUsers.findUser(String.valueOf(list[x].getPath()) + "/", username);
                    if (result != null) {
                        return result;
                    }
                }
            }
            ++x;
        }
        return null;
    }

    /*
     * Unable to fully structure code
     */
    public static CIProperties buildVFSXML(String vfsHome) {
        block17: {
            block16: {
                if (!vfsHome.endsWith("/")) {
                    vfsHome = String.valueOf(vfsHome) + "/";
                }
                virtual = new CIProperties();
                permissions0 = (Properties)Common.readXMLObject(String.valueOf(vfsHome) + "VFS.XML");
                if (permissions0 == null) {
                    permissions0 = new Properties();
                    permissions0.put("/", "(read)(view)(resume)");
                }
                permissions = new Vector<Properties>();
                keys = permissions0.keys();
                permisions0_new = new Properties();
                while (keys.hasMoreElements()) {
                    key = keys.nextElement().toString();
                    o = permissions0.remove(key);
                    permisions0_new.put(key.toUpperCase(), o);
                }
                permissions.addElement(permisions0_new);
                virtual.put("vfs_permissions_object", permissions);
                list = new Vector<File_S>();
                rootPath = "";
                try {
                    rootPath = String.valueOf(new File_S(String.valueOf(vfsHome) + "VFS/").getCanonicalPath().replace('\\', '/')) + "/";
                    list.addElement(new File_S(rootPath));
                    Common.getAllFileListing(list, String.valueOf(vfsHome) + "VFS/", 20, true);
                }
                catch (Exception e) {
                    Log.log("USER_OBJ", 0, e);
                }
                if (new File_S(String.valueOf(vfsHome) + "VFS/").exists()) break block16;
                p = new Properties();
                p.put("virtualPath", "/");
                p.put("name", "VFS");
                p.put("type", "DIR");
                p.put("vItems", new Vector<E>());
                virtual.put("/", p);
                break block17;
            }
            x = 0;
            while (x < list.size()) {
                block18: {
                    block19: {
                        f = (File_S)list.elementAt(x);
                        if (f.getName().equals(".DS_Store")) break block18;
                        p = new Properties();
                        p.put("name", f.getName());
                        if (!f.isDirectory()) break block19;
                        p.put("type", "DIR");
                        p.put("modified", String.valueOf(f.lastModified()));
                        ** GOTO lbl-1000
                    }
                    p.put("type", "FILE");
                    v = UserTools.vItemLoad(f.getPath());
                    if (v == null) {
                        v = new Vector<E>();
                    }
                    xx = 0;
                    while (xx < v.size()) {
                        pp = (Properties)v.elementAt(xx);
                        pp.put("modified", String.valueOf(f.lastModified()));
                        if (pp.getProperty("url", "").toLowerCase().startsWith("file:/") && !pp.getProperty("url", "").toLowerCase().startsWith("file://")) {
                            if (!pp.getProperty("url", "").toLowerCase().endsWith("/")) {
                                pp.put("url", String.valueOf(pp.getProperty("url")) + "/");
                            }
                            pp.put("url", "file://" + pp.getProperty("url").substring("file:/".length()));
                        }
                        ++xx;
                    }
                    p.put("vItems", v);
                    if (v.size() == 0) {
                        Log.log("SERVER", 0, "Ignoring invalid item with no vItem:" + f);
                    } else lbl-1000:
                    // 2 sources

                    {
                        try {
                            itemPath = f.getCanonicalPath().replace('\\', '/');
                            p.put("virtualPath", itemPath.substring(rootPath.length() - 1));
                            p.put("modified", String.valueOf(f.lastModified()));
                            if (x == 0) {
                                virtual.put("/", p);
                            } else {
                                virtual.put(itemPath.substring(rootPath.length() - 1), p);
                            }
                        }
                        catch (Exception e) {
                            Log.log("USER_OBJ", 0, e);
                        }
                    }
                }
                ++x;
            }
        }
        return virtual;
    }

    @Override
    public Properties buildVFS(String serverGroup, String username) {
        return XMLUsers.buildVFSXML(XMLUsers.getUserPath(serverGroup, username));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Properties loadGroups(String serverGroup) {
        Properties groups2 = null;
        String groupsPath = String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/groups.XML";
        File_S groupsF = new File_S(groupsPath);
        if (groupsF.exists()) {
            Properties properties = inheritance_groups_cache;
            synchronized (properties) {
                if (inheritance_groups_cache.getProperty(String.valueOf(groupsPath) + "_time", "0").equals(String.valueOf(groupsF.lastModified()))) {
                    groups2 = (Properties)inheritance_groups_cache.get(groupsPath);
                } else {
                    try {
                        groups2 = (Properties)Common.readXMLObject(groupsPath);
                        inheritance_groups_cache.put(groupsPath, groups2);
                        inheritance_groups_cache.put(String.valueOf(groupsPath) + "_time", String.valueOf(groupsF.lastModified()));
                    }
                    catch (Exception e) {
                        Log.log("USER_OBJ", 2, e);
                    }
                }
            }
            if (groups2 == null) {
                try {
                    Thread.sleep(1000L);
                    groups2 = (Properties)Common.readXMLObject(groupsPath);
                }
                catch (Exception e) {
                    Log.log("USER_OBJ", 2, e);
                }
            }
        }
        if (groups2 == null) {
            groups2 = new Properties();
        }
        return groups2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void writeGroups(String serverGroup, Properties groups) {
        Object object = groupLoc;
        synchronized (object) {
            new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/").mkdirs();
            new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/groups99.XML").delete();
            int x = 98;
            while (x >= 0) {
                try {
                    new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/groups" + x + ".XML").renameTo(new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/groups" + (x + 1) + ".XML"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                --x;
            }
            try {
                Common.writeXMLObject(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/groups.XML.new", (Object)groups, "groups");
                new File_S(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/groups.XML").renameTo(new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/groups0.XML"));
                new File_S(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/groups.XML").delete();
                int loops = 0;
                Common.xmlCache.remove(new File_S(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/groups.XML").getCanonicalPath());
                while (!new File_S(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/groups.XML.new").renameTo(new File_S(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/groups.XML")) && loops++ < 100) {
                    Thread.sleep(100L);
                }
                Common.xmlCache.remove(new File_S(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/groups.XML").getCanonicalPath());
            }
            catch (Exception ee) {
                Log.log("USER_OBJ", 1, ee);
            }
        }
    }

    @Override
    public void writeVFS(String serverGroup, String username, Properties virtual) {
        try {
            Vector permissions = (Vector)virtual.get("vfs_permissions_object");
            Properties permissions0 = new Properties();
            permissions0.put("/", "(read)(view)(resume)");
            if (permissions != null && permissions.size() > 0) {
                permissions0 = (Properties)permissions.elementAt(0);
            }
            Common.writeXMLObject(String.valueOf(XMLUsers.getUserPath(serverGroup, username)) + "VFS.XML", (Object)permissions0, "VFS");
        }
        catch (Exception ee) {
            Log.log("USER_OBJ", 0, ee);
        }
        try {
            Common.recurseDelete(String.valueOf(XMLUsers.getUserPath(serverGroup, username)) + "VFS/", false);
            new File_S(String.valueOf(XMLUsers.getUserPath(serverGroup, username)) + "VFS/").mkdir();
            Enumeration<Object> keys = virtual.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                if (key.equals("vfs_permissions_object")) continue;
                Properties p = (Properties)virtual.get(key);
                String virtualPath = p.getProperty("virtualPath");
                if (p.getProperty("type").equalsIgnoreCase("DIR")) {
                    new File_S(String.valueOf(XMLUsers.getUserPath(serverGroup, username)) + "VFS" + virtualPath).mkdirs();
                    continue;
                }
                new File_S(String.valueOf(XMLUsers.getUserPath(serverGroup, username)) + "VFS" + Common.all_but_last(virtualPath)).mkdirs();
                Vector v = (Vector)p.get("vItems");
                int xx = 0;
                while (xx < v.size()) {
                    Properties pp = (Properties)v.elementAt(xx);
                    if (pp.getProperty("encrypted", "false").equals("true")) {
                        if (!pp.getProperty("encrypted_class", "").trim().equals("")) {
                            try {
                                Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(pp.getProperty("encrypted_class").trim());
                                Constructor<?> cons = c.getConstructor(new Properties().getClass(), new String().getClass());
                                cons.newInstance(pp, "encrypt");
                            }
                            catch (Exception ee) {
                                Log.log("USER_OBJ", 1, ee);
                            }
                        } else {
                            pp.put("url", ServerStatus.thisObj.common_code.encode_pass(pp.getProperty("url"), "DES", ""));
                        }
                    }
                    ++xx;
                }
                new Common();
                Common.writeXMLObject(String.valueOf(XMLUsers.getUserPath(serverGroup, username)) + "VFS" + virtualPath, (Object)v, "VFS");
            }
            Common.updateOSXInfo(XMLUsers.getUserPath(serverGroup, username));
        }
        catch (Exception ee) {
            Log.log("USER_OBJ", 0, ee);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Properties loadInheritance(String serverGroup) {
        String inheritancePath;
        File_S inheritanceF;
        Properties inheritance2 = null;
        if (serverGroup.endsWith("_restored_backup")) {
            serverGroup = serverGroup.substring(0, serverGroup.indexOf("_restored_backup"));
        }
        if ((inheritanceF = new File_S(inheritancePath = String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/inheritance.XML")).exists()) {
            Properties properties = inheritance_groups_cache;
            synchronized (properties) {
                if (inheritance_groups_cache.getProperty(String.valueOf(inheritancePath) + "_time", "0").equals(String.valueOf(inheritanceF.lastModified()))) {
                    inheritance2 = (Properties)inheritance_groups_cache.get(inheritancePath);
                } else {
                    try {
                        inheritance2 = (Properties)Common.readXMLObject(inheritancePath);
                        inheritance_groups_cache.put(inheritancePath, inheritance2);
                        inheritance_groups_cache.put(String.valueOf(inheritancePath) + "_time", String.valueOf(inheritanceF.lastModified()));
                    }
                    catch (Exception e) {
                        Log.log("USER_OBJ", 2, e);
                    }
                }
            }
            if (inheritance2 == null) {
                try {
                    Thread.sleep(1000L);
                    inheritance2 = (Properties)Common.readXMLObject(inheritancePath);
                }
                catch (Exception e) {
                    Log.log("USER_OBJ", 2, e);
                }
            }
        }
        if (inheritance2 == null) {
            inheritance2 = new Properties();
        }
        return inheritance2;
    }

    @Override
    public Properties loadUser(String serverGroup, String username, Properties inheritance, boolean flattenUser) {
        Properties user = this.read_user_no_cache(serverGroup, username);
        String default_server_group = serverGroup;
        if (serverGroup.endsWith("_restored_backup")) {
            default_server_group = serverGroup.substring(0, serverGroup.indexOf("_restored_backup"));
        }
        Properties newUser = this.read_user_no_cache(default_server_group, "default");
        if (user == null) {
            return null;
        }
        Properties originalUser = (Properties)user.clone();
        boolean needWrite = this.fixExpireAccount(user, originalUser, newUser);
        if (needWrite |= this.fixExpirePassword(user, originalUser, newUser)) {
            this.writeUser(serverGroup, username, originalUser, false);
        }
        if (!flattenUser) {
            return user;
        }
        Enumeration<Object> keys = inheritance.keys();
        Vector ichain = null;
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (!key.equalsIgnoreCase(username)) continue;
            ichain = (Vector)inheritance.get(key);
            break;
        }
        if (ichain != null) {
            int x = 0;
            while (x < ichain.size()) {
                Properties p = this.read_user_no_cache(serverGroup, ichain.elementAt(x).toString());
                UserTools.mergeWebCustomizations(newUser, p);
                UserTools.mergeLinkedVFS(newUser, p);
                UserTools.mergeGroupAdminNames(newUser, p);
                UserTools.mergeEvents(newUser, p);
                try {
                    needWrite = this.fixExpireAccount(user, originalUser, p);
                    if (needWrite |= this.fixExpirePassword(user, originalUser, p)) {
                        this.writeUser(serverGroup, username, originalUser, true);
                    }
                }
                catch (Exception e) {
                    Common.debug(1, e);
                }
                if (p != null) {
                    newUser.putAll((Map<?, ?>)p);
                }
                ++x;
            }
        }
        UserTools.mergeWebCustomizations(newUser, user);
        UserTools.mergeEvents(newUser, user);
        newUser.putAll((Map<?, ?>)user);
        return newUser;
    }

    public boolean fixExpirePassword(Properties user, Properties originalUser, Properties p) {
        if (user != null && user.getProperty("expire_password", "").equals("") && !p.getProperty("expire_password", "").equals("") && !p.getProperty("expire_password", "").equals("false")) {
            int days = Integer.parseInt(p.getProperty("expire_password_days"));
            originalUser.put("expire_password_days", String.valueOf(days));
            if (days < 0) {
                originalUser.put("expire_password_days", String.valueOf(days *= -1));
            }
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(new Date());
            gc.add(5, days);
            p.put("expire_password_when", new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US).format(gc.getTime()));
            originalUser.put("expire_password_when", new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US).format(gc.getTime()));
            originalUser.put("expire_password", p.getProperty("expire_password"));
            return true;
        }
        return false;
    }

    public boolean fixExpireAccount(Properties user, Properties originalUser, Properties p) {
        if (user != null && user.getProperty("account_expire") == null && !p.getProperty("account_expire", "").equals("") && !p.getProperty("account_expire_rolling_days", "0").equals("") && Math.abs(Integer.parseInt(p.getProperty("account_expire_rolling_days", "0"))) > 0) {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(new Date());
            int days = Integer.parseInt(p.getProperty("account_expire_rolling_days"));
            originalUser.put("account_expire_rolling_days", String.valueOf(days));
            if (days < 0) {
                originalUser.put("account_expire_rolling_days", String.valueOf(days *= -1));
            }
            gc.add(5, days);
            originalUser.put("account_expire_task", p.getProperty("account_expire_task", ""));
            originalUser.put("account_expire_delete", p.getProperty("account_expire_delete", "false"));
            originalUser.put("account_expire", new SimpleDateFormat("MM/dd/yy hh:mm aa", Locale.US).format(gc.getTime()));
            originalUser.put("account_expire", new SimpleDateFormat("MM/dd/yy hh:mm aa", Locale.US).format(gc.getTime()));
            return true;
        }
        return false;
    }

    @Override
    public void updateUser(String serverGroup, String username1, String username2, String password) {
        if (username2 != null) {
            File_S rnfr = new File_S(XMLUsers.getUserPath(serverGroup, username1));
            File_S rnto = new File_S(XMLUsers.getUserPath(serverGroup, username2));
            rnfr.renameTo(rnto);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void writeUser(String serverGroup, String username, Properties user, boolean backup) {
        Object lock = null;
        Object object = user_write_lock;
        synchronized (object) {
            if (!user_write_lock.containsKey(username.toUpperCase())) {
                user_write_lock.put(username.toUpperCase(), new Object());
            }
            lock = user_write_lock.get(username.toUpperCase());
        }
        object = lock;
        synchronized (object) {
            boolean is_newUser = false;
            String user_dir = XMLUsers.getUserPath(serverGroup, username);
            if (user_dir == null || !new File_S(user_dir).exists()) {
                is_newUser = true;
            }
            if (!is_newUser) {
                try {
                    if (backup) {
                        this.writeBackupUser(serverGroup, username, user_dir);
                        this.writeExtraVFSBackup(username);
                    }
                }
                catch (Exception ee) {
                    Log.log("USER_OBJ", 0, ee);
                }
            }
            try {
                new File_S(user_dir).mkdirs();
                if (!new File_S(user_dir).getCanonicalFile().getName().equals(username)) {
                    String tmp = Common.makeBoundary(3);
                    if (!user_dir.endsWith("/")) {
                        user_dir = String.valueOf(user_dir) + "/";
                    }
                    new File_S(user_dir).renameTo(new File_S(String.valueOf(user_dir.substring(0, user_dir.length() - 1)) + tmp));
                    new File_S(String.valueOf(user_dir.substring(0, user_dir.length() - 1)) + tmp).renameTo(new File_S(user_dir));
                }
                Common.updateOSXInfo(user_dir);
                user.put("version", "1.0");
                Common c = null;
                c = ServerStatus.thisObj != null ? ServerStatus.thisObj.common_code : new Common();
                new File_S(String.valueOf(user_dir) + "user_old.XML").delete();
                String uid = Common.makeBoundary(6);
                Common.writeXMLObject(String.valueOf(user_dir) + "user_" + uid + ".XML", (Object)user, "userfile");
                Common.xmlCache.clear();
                Object object2 = user_lock;
                synchronized (object2) {
                    if (new File_S(String.valueOf(user_dir) + "user_" + uid + ".XML").length() > 0L) {
                        Common.copy(String.valueOf(user_dir) + "user_" + uid + ".XML", String.valueOf(user_dir) + "user.XML", true);
                    }
                    new File_S(String.valueOf(user_dir) + "user_" + uid + ".XML").delete();
                }
                Common.updateOSXInfo(user_dir);
            }
            catch (Exception ee) {
                Log.log("USER_OBJ", 0, ee);
                throw new RuntimeException(ee);
            }
            if (is_newUser) {
                user_dir = XMLUsers.getUserPath(serverGroup, username);
                try {
                    if (user_dir != null && new File_S(user_dir).exists() && backup) {
                        this.writeBackupUser(serverGroup, username, user_dir);
                        this.writeExtraVFSBackup(username);
                    }
                }
                catch (Exception ee) {
                    Log.log("USER_OBJ", 0, ee);
                }
            }
        }
    }

    private void writeExtraVFSBackup(String username) throws Exception {
        File_S[] folders = (File_S[])new File_S(String.valueOf(System.getProperty("crushftp.users")) + "extra_vfs/").listFiles();
        int x = 0;
        while (x < folders.length) {
            File_S f = folders[x];
            if (f.getName().startsWith(String.valueOf(username) + "~")) {
                this.writeBackupUser("extra_vfs", f.getName(), XMLUsers.getUserPath("extra_vfs", f.getName()));
            }
            ++x;
        }
    }

    private void writeBackupUser(String serverGroup, String username, String path) throws Exception {
        UserTools.purgeOldBackups(ServerStatus.IG("user_backup_count"));
        SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy_HHmmss", Locale.US);
        new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/").mkdirs();
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/" + username + "-" + sdf.format(new Date()) + ".zip")));
        Vector zipFiles = new Vector();
        Common.getAllFileListing(zipFiles, path, 10, false);
        int x = zipFiles.size() - 1;
        while (x >= 0) {
            if (Common.last(zipFiles.elementAt(x).toString()).equals(".DS_Store")) {
                zipFiles.removeElementAt(x);
            } else if (((File_S)zipFiles.elementAt(x)).length() > 0x100000L) {
                zipFiles.removeElementAt(x);
            }
            --x;
        }
        int offset = new File_S(path).getCanonicalPath().length();
        int xx = 0;
        while (xx < zipFiles.size()) {
            File_S item = (File_S)zipFiles.elementAt(xx);
            if (item.isDirectory()) {
                zout.putNextEntry(new ZipEntry(String.valueOf(item.getCanonicalPath().substring(offset)) + "/"));
            } else if (item.isFile()) {
                zout.putNextEntry(new ZipEntry(item.getCanonicalPath().substring(offset)));
                RandomAccessFile in = new RandomAccessFile(item, "r");
                byte[] b = new byte[(int)in.length()];
                in.readFully(b);
                in.close();
                zout.write(b);
            }
            zout.closeEntry();
            ++xx;
        }
        zout.finish();
        zout.close();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void writeInheritance(String serverGroup, Properties inheritance) {
        Object object = inheritanceLoc;
        synchronized (object) {
            new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/").mkdirs();
            new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/inheritance99.XML").delete();
            int x = 98;
            while (x >= 0) {
                try {
                    new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/inheritance" + x + ".XML").renameTo(new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/inheritance" + (x + 1) + ".XML"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                --x;
            }
            try {
                Common.writeXMLObject(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/inheritance.XML.new", (Object)inheritance, "inheritance");
                new File_S(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/inheritance.XML").renameTo(new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/inheritance0.XML"));
                new File_S(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/inheritance.XML").delete();
                int loops = 0;
                Common.xmlCache.remove(new File_S(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/inheritance.XML").getCanonicalPath());
                while (!new File_S(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/inheritance.XML.new").renameTo(new File_S(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/inheritance.XML")) && loops++ < 100) {
                    Thread.sleep(100L);
                }
                Common.xmlCache.remove(new File_S(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/inheritance.XML").getCanonicalPath());
            }
            catch (Exception ee) {
                Log.log("USER_OBJ", 1, ee);
            }
        }
    }

    @Override
    public void deleteUser(String serverGroup, String username) {
        String user_dir = XMLUsers.getUserPath(serverGroup, username);
        try {
            this.writeBackupUser(serverGroup, username, user_dir);
            this.writeExtraVFSBackup(username);
        }
        catch (Exception ee) {
            Log.log("USER_OBJ", 0, ee);
        }
        Common.recurseDelete(XMLUsers.getUserPath(serverGroup, username), false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Properties read_user_no_cache(String serverGroup, String username) {
        Properties p;
        String real_path;
        block18: {
            username = username.replaceAll("/", "");
            username = username.replaceAll("\\\\", "");
            real_path = "";
            try {
                real_path = XMLUsers.getUserPath(serverGroup, username);
                Common common = null;
                if (ServerStatus.thisObj != null) {
                    common = ServerStatus.thisObj.common_code;
                }
                if (common == null) {
                    common = new Common();
                }
                p = null;
                int x = 0;
                while (p == null && x < 5 && new File_S(real_path).exists()) {
                    block17: {
                        try {
                            Log.log("USER_OBJ", 2, "Validating user path object:" + real_path + "user.XML");
                            Log.log("USER_OBJ", 2, "Validating user path XML exists:" + new File_S(String.valueOf(real_path) + "user.XML").exists());
                            Object object = user_lock;
                            synchronized (object) {
                                p = (Properties)Common.readXMLObject(String.valueOf(real_path) + "user.XML");
                            }
                            Log.log("USER_OBJ", 2, "Validating user loading object:" + (p != null ? String.valueOf(p.size()) : "no real_path " + real_path + " found!"));
                            if (!(p != null || username.equals("template") || username.equals("anonymous") || username.equals("") || username.endsWith(".SHARED") || username.equals("default"))) {
                                Thread.sleep(200L);
                            }
                        }
                        catch (Exception e) {
                            if (username.equals("template") || username.equals("anonymous") || username.equals("") || username.endsWith(".SHARED") || username.equals("default")) {
                                throw e;
                            }
                            if (x < 4) break block17;
                            throw e;
                        }
                    }
                    ++x;
                }
                Log.log("USER_OBJ", 3, "Finding path to username:" + username + ":" + real_path);
                username = new File_S(real_path).getCanonicalFile().getName();
                Log.log("USER_OBJ", 3, "Got path to username:" + username + ":" + real_path);
                if (p != null) break block18;
                return null;
            }
            catch (Exception e) {
                if (username.equals("default")) {
                    p = (Properties)Common.readXMLObject(new Common().getClass().getResource("/assets/default_user.xml"));
                    try {
                        this.writeUser(serverGroup, "default", p, false);
                    }
                    catch (Exception ee) {
                        Log.log("USER_OBJ", 0, ee);
                    }
                    return p;
                }
                if (!(username.equals("template") || username.equals("anonymous") || username.equals("") || username.endsWith(".SHARED"))) {
                    Log.log("USER_OBJ", 1, "Username not found:" + username + ":" + real_path);
                    Log.log("USER_OBJ", 2, e);
                }
                return null;
            }
        }
        p.put("username", username);
        p.put("user_name", username);
        p.put("real_path_to_user", real_path);
        return p;
    }

    @Override
    public Vector loadUserList(String serverGroup) {
        Vector listing = new Vector();
        File_S dir = new File_S(String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/");
        this.loadUserListRecurse(listing, dir.getPath());
        Collections.sort(listing, new Comparator(){

            public int compare(Object a, Object b) {
                return a.toString().toUpperCase().compareTo(b.toString().toUpperCase());
            }
        });
        return listing;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Vector findUserEmail(String serverGroup, String email) {
        Object object = username_lookup_lock;
        synchronized (object) {
            Vector listing = this.loadUserList(serverGroup);
            Vector<Properties> matchingUsernames = new Vector<Properties>();
            int x = 0;
            while (x < listing.size()) {
                block18: {
                    if (!listing.elementAt(x).equals("TempAccount") && !((String)listing.elementAt(x)).startsWith("TempAccount_")) {
                        try {
                            Thread.sleep(1L);
                            Thread.sleep(x / 100);
                        }
                        catch (InterruptedException interruptedException) {
                            // empty catch block
                        }
                        String path = String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/" + listing.elementAt(x) + "/user.XML";
                        if (new File_S(path).exists()) {
                            try {
                                if (listing.elementAt(x).toString().equalsIgnoreCase(email)) {
                                    Properties user = null;
                                    Object object2 = user_lock;
                                    synchronized (object2) {
                                        user = (Properties)Common.readXMLObject(path);
                                    }
                                    user.put("username", listing.elementAt(x));
                                    user.put("user_name", listing.elementAt(x));
                                    matchingUsernames.addElement(user);
                                    break block18;
                                }
                                if (email.indexOf("@") < 0) break block18;
                                RandomAccessFile in = null;
                                byte[] b = null;
                                Object object3 = user_lock;
                                synchronized (object3) {
                                    in = new RandomAccessFile(new File_S(path), "r");
                                    b = new byte[(int)in.length()];
                                    in.read(b);
                                    in.close();
                                }
                                String s = new String(b, "UTF8").toUpperCase();
                                if (s.indexOf(email.toUpperCase()) > 0) {
                                    Properties user = (Properties)Common.readXMLObject(new ByteArrayInputStream(b));
                                    user.put("username", listing.elementAt(x));
                                    user.put("user_name", listing.elementAt(x));
                                    matchingUsernames.addElement(user);
                                }
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 2, e);
                            }
                        }
                    }
                }
                ++x;
            }
            return matchingUsernames;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void cacheEmailUsernames() {
        Vector<String> bad_emails = new Vector<String>();
        Properties user_email_cache_tmp = new Properties();
        Vector sgs = ServerStatus.VG("server_groups");
        int xx = 0;
        while (xx < sgs.size()) {
            String serverGroup = sgs.elementAt(xx).toString();
            Vector listing = null;
            Object object = username_lookup_lock;
            synchronized (object) {
                listing = this.loadUserList(serverGroup);
            }
            int x = 0;
            while (x < listing.size()) {
                if (!(listing.elementAt(x).equals("TempAccount") || ((String)listing.elementAt(x)).startsWith("TempAccount_") || listing.elementAt(x).equals("template") || ((String)listing.elementAt(x)).startsWith("default"))) {
                    try {
                        Thread.sleep(1L);
                        Thread.sleep(x / 100);
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                    String path = String.valueOf(System.getProperty("crushftp.users")) + serverGroup + "/" + listing.elementAt(x) + "/user.XML";
                    if (new File_S(path).exists()) {
                        try {
                            RandomAccessFile in = null;
                            byte[] b = null;
                            Object object2 = user_lock;
                            synchronized (object2) {
                                in = new RandomAccessFile(new File_S(path), "r");
                                b = new byte[(int)in.length()];
                                in.read(b);
                                in.close();
                            }
                            Properties user = (Properties)Common.readXMLObject(new ByteArrayInputStream(b));
                            if (user != null && !user.getProperty("email", "").equals("")) {
                                if (user_email_cache_tmp.containsKey(String.valueOf(serverGroup) + ":" + user.getProperty("email").toUpperCase())) {
                                    bad_emails.addElement(String.valueOf(serverGroup) + ":" + user.getProperty("email").toUpperCase());
                                }
                                user_email_cache_tmp.put(String.valueOf(serverGroup) + ":" + user.getProperty("email").toUpperCase(), listing.elementAt(x));
                            }
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 2, "Corrupt username:" + path);
                            Log.log("SERVER", 2, e);
                        }
                    }
                }
                ++x;
            }
            ++xx;
        }
        int x = 0;
        while (x < bad_emails.size()) {
            user_email_cache_tmp.remove(bad_emails.elementAt(x));
            ++x;
        }
        UserTools.user_email_cache = user_email_cache_tmp;
        Log.log("SERVER", 0, "Cached " + UserTools.user_email_cache.size() + " emails to XF table.");
    }

    private void loadUserListRecurse(Vector listing, String path) {
        File_S dir = new File_S(path);
        File_S[] item_list = (File_S[])dir.listFiles();
        if (item_list != null) {
            int x = 0;
            while (x < item_list.length) {
                if (item_list[x].isDirectory() && !item_list[x].getName().equalsIgnoreCase("VFS")) {
                    listing.addElement(item_list[x].getName());
                }
                ++x;
            }
        }
    }

    @Override
    public void addFolder(String serverGroup, String username, String path, String name) {
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.equals("/")) {
            path = "";
        }
        new File_S(String.valueOf(XMLUsers.getUserPath(serverGroup, username)) + "VFS/" + path + name).mkdirs();
        Common.updateOSXInfo(String.valueOf(XMLUsers.getUserPath(serverGroup, username)) + "VFS");
    }

    @Override
    public void addItem(String serverGroup, String username, String path, String name, String url, String type, Properties moreItems, boolean encrypted, String encrypted_class) throws Exception {
        if (type.equalsIgnoreCase("DIR") && !url.endsWith("/")) {
            url = String.valueOf(url) + "/";
        }
        if (!path.endsWith("/")) {
            path = String.valueOf(path) + "/";
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.equals("/")) {
            path = "";
        }
        VRL vv = new VRL(url);
        Vector<Properties> v = new Vector<Properties>();
        Properties p = new Properties();
        v.addElement(p);
        p.put("type", type.toUpperCase());
        p.put("url", vv.toString());
        p.putAll((Map<?, ?>)moreItems);
        if (encrypted) {
            p.put("encrypted", "true");
            p.put("encrypted_class", encrypted_class);
            if (!p.getProperty("encrypted_class", "").trim().equals("")) {
                try {
                    Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(p.getProperty("encrypted_class").trim());
                    Constructor<?> cons = c.getConstructor(new Properties().getClass(), new String().getClass());
                    cons.newInstance(p, "encrypt");
                }
                catch (Exception ee) {
                    Log.log("USER_OBJ", 1, ee);
                }
            } else {
                p.put("url", ServerStatus.thisObj.common_code.encode_pass(p.getProperty("url"), "DES", ""));
            }
        }
        Common.writeXMLObject(String.valueOf(XMLUsers.getUserPath(serverGroup, username)) + "VFS/" + path + name, v, "VFS");
    }
}

