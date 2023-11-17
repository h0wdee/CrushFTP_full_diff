/*
 * Decompiled with CFR 0.152.
 */
package crushftp.user;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.VRL;
import crushftp.handlers.CIProperties;
import crushftp.handlers.Log;
import crushftp.handlers.UserTools;
import crushftp.server.ServerSessionAJAX;
import crushftp.server.ServerStatus;
import crushftp.server.VFS;
import crushftp.user.UserProvider;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class SQLUsers
extends UserProvider {
    static ClassLoader cl = null;
    static Class drvCls = null;
    static Driver driver = null;
    public Properties settings = new Properties();
    public String lastDriver = "";
    public static boolean loadEvents4 = true;
    public static boolean for_menu_buttons = false;
    static Vector freeConnections = new Vector();
    static Vector usedConnections = new Vector();
    public static transient Object used_lock = new Object();
    Properties cache = new Properties();
    Properties userCache = new Properties();
    Properties inheritanceCache = new Properties();
    Vector cacheRevolver = new Vector();

    public SQLUsers() {
        SQLUsers.setDefaults(this.settings);
    }

    public static void setDefaults(Properties settings2) {
        settings2.put("db_debug", "true");
        settings2.put("db_mysql_groups_compatibility", "true");
        settings2.put("db_read_only", "false");
        settings2.put("db_driver_file", "./mysql-connector-java-5.0.4-bin.jar");
        settings2.put("db_driver", "org.gjt.mm.mysql.Driver");
        settings2.put("db_url", "jdbc:mysql://127.0.0.1:3306/crushftp?autoReconnect=true");
        settings2.put("db_user", System.getProperty("appname", "CrushFTP").toLowerCase());
        settings2.put("db_pass", "");
        settings2.put("db_user_query", "SELECT * FROM USERS WHERE SERVER_GROUP=? and UPPER(USERNAME)=UPPER(?)");
        settings2.put("db_user_insert", "INSERT INTO USERS (USERNAME,PASSWORD,SERVER_GROUP) VALUES(?,?,?)");
        settings2.put("db_user_update", "UPDATE USERS SET USERNAME=?, PASSWORD=? WHERE UPPER(USERNAME)=UPPER(?) AND SERVER_GROUP=?");
        settings2.put("db_user_delete", "DELETE FROM USERS WHERE UPPER(USERNAME)=UPPER(?) AND SERVER_GROUP=?");
        settings2.put("db_users_query", "SELECT * FROM USERS WHERE SERVER_GROUP=? order by USERNAME");
        settings2.put("db_user_inheritance_query", "SELECT * FROM INHERITANCE WHERE SERVER_GROUP=?");
        settings2.put("db_user_inheritance_delete", "DELETE FROM INHERITANCE WHERE USERID=?");
        settings2.put("db_user_inheritance_insert", "INSERT INTO INHERITANCE (USERID,INHERIT_USERNAME,SORT_ORDER) VALUES(?,?,?)");
        settings2.put("db_user_inheritance_id_query", "SELECT * FROM INHERITANCE WHERE USERID=?");
        settings2.put("db_user_properties_query", "SELECT * FROM USER_PROPERTIES WHERE USERID=?");
        settings2.put("db_user_properties_delete", "DELETE FROM USER_PROPERTIES WHERE USERID=?");
        settings2.put("db_user_properties_insert", "INSERT INTO USER_PROPERTIES (USERID,PROP_NAME,PROP_VALUE) VALUES(?,?,?)");
        settings2.put("db_user_user_properties_id_query", "SELECT * FROM USER_PROPERTIES WHERE USERID=?");
        settings2.put("db_user_events_delete", "DELETE FROM EVENTS5 WHERE USERID=?");
        settings2.put("db_user_events_insert", "INSERT INTO EVENTS5 (USERID,EVENT_NAME,PROP_NAME,PROP_VALUE) VALUES(?,?,?,?)");
        settings2.put("db_user_events_id_query", "SELECT * FROM EVENTS5 WHERE USERID=?");
        settings2.put("db_user_domain_root_list_delete", "DELETE FROM DOMAIN_ROOT_LIST WHERE USERID=?");
        settings2.put("db_user_domain_root_list_insert", "INSERT INTO DOMAIN_ROOT_LIST (USERID,DOMAIN,PATH,SORT_ORDER) VALUES(?,?,?,?)");
        settings2.put("db_user_domain_root_list_id_query", "SELECT * FROM DOMAIN_ROOT_LIST WHERE USERID=?");
        settings2.put("db_user_ip_restrictions_delete", "DELETE FROM IP_RESTRICTIONS WHERE USERID=?");
        settings2.put("db_user_ip_restrictions_insert", "INSERT INTO IP_RESTRICTIONS (USERID,START_IP,TYPE,STOP_IP,SORT_ORDER) VALUES(?,?,?,?,?)");
        settings2.put("db_user_ip_restrictions_id_query", "SELECT * FROM IP_RESTRICTIONS WHERE USERID=?");
        settings2.put("db_user_web_buttons_delete", "DELETE FROM WEB_BUTTONS WHERE USERID=?");
        settings2.put("db_user_web_buttons_insert", "INSERT INTO WEB_BUTTONS (USERID,SQL_FIELD_KEY,SQL_FIELD_VALUE,FOR_MENU,FOR_CONTEXT_MENU,SORT_ORDER) VALUES(?,?,?,?,?,?)");
        settings2.put("db_user_web_buttons_id_query", "SELECT * FROM WEB_BUTTONS WHERE USERID=?");
        settings2.put("db_user_web_customizations_delete", "DELETE FROM WEB_CUSTOMIZATIONS WHERE USERID=?");
        settings2.put("db_user_web_customizations_insert", "INSERT INTO WEB_CUSTOMIZATIONS (USERID,SQL_FIELD_KEY,SQL_FIELD_VALUE,SORT_ORDER) VALUES(?,?,?,?)");
        settings2.put("db_user_web_customizations_id_query", "SELECT * FROM WEB_CUSTOMIZATIONS WHERE USERID=?");
        settings2.put("db_user_vfs_delete", "DELETE FROM VFS WHERE USERID=?");
        settings2.put("db_user_vfs_properties_delete", "DELETE FROM VFS_PROPERTIES WHERE USERID=?");
        settings2.put("db_user_vfs_vdir_insert", "INSERT INTO VFS (USERID,URL,TYPE,PATH,SORT_ORDER) VALUES(?,?,?,?,?)");
        settings2.put("db_user_vfs_insert", "INSERT INTO VFS (USERID,URL,TYPE,PATH,SORT_ORDER) VALUES(?,?,?,?,?)");
        settings2.put("db_user_vfs_properties_insert", "INSERT INTO VFS_PROPERTIES (USERID,PATH,PROP_NAME,PROP_VALUE) VALUES(?,?,?,?)");
        settings2.put("db_user_vfs_id_query", "SELECT * FROM VFS WHERE USERID=?");
        settings2.put("db_user_vfs_properties_id_query", "SELECT * FROM VFS_PROPERTIES WHERE USERID=?");
        settings2.put("db_user_vfs_permissions_delete", "DELETE FROM VFS_PERMISSIONS WHERE USERID=?");
        settings2.put("db_user_vfs_permissions_insert", "INSERT INTO VFS_PERMISSIONS (USERID,PATH,PRIVS) VALUES(?,?,?)");
        settings2.put("db_user_vfs_permissions_query", "SELECT * FROM VFS_PERMISSIONS WHERE USERID=?");
        settings2.put("db_user_vfs_permissions_id_query", "SELECT * FROM VFS_PERMISSIONS WHERE USERID=?");
        settings2.put("db_groups_query", "SELECT * FROM GROUPS G LEFT JOIN USERS ON G.USERID = USERS.USERID WHERE G.SERVER_GROUP=?");
        settings2.put("db_groups_delete", "DELETE FROM GROUPS WHERE SERVER_GROUP=?");
        settings2.put("db_groups_user_delete", "DELETE FROM GROUPS WHERE USERID=?");
        settings2.put("db_groups_insert", "INSERT INTO GROUPS (GROUPNAME, USERID, SERVER_GROUP) VALUES(?,?,?)");
        settings2.put("db_modified_query", "SELECT * FROM MODIFIED_TIMES WHERE SERVER_GROUP=? AND PROP_NAME=?");
        settings2.put("db_modified_insert", "INSERT INTO MODIFIED_TIMES (SERVER_GROUP,PROP_NAME,PROP_VALUE) VALUES(?,?,?)");
        settings2.put("db_modified_delete", "DELETE FROM MODIFIED_TIMES WHERE SERVER_GROUP=? AND PROP_NAME=?");
        settings2.put("db_user_email_query", "SELECT USERNAME FROM USERS LEFT JOIN USER_PROPERTIES ON USERS.USERID = USER_PROPERTIES.USERID WHERE SERVER_GROUP = ? AND PROP_NAME = 'email' AND PROP_VALUE = ?");
    }

    public void setSettings(Properties p) {
        SQLUsers.fixSql(p);
        this.settings.putAll((Map<?, ?>)p);
        if (this.settings.getProperty("db_mysql_groups_compatibility", "false").equalsIgnoreCase("true") && this.settings.getProperty("db_driver", "").toLowerCase().indexOf("sqlserver") >= 0) {
            this.settings.put("db_mysql_groups_compatibility", "false");
        }
    }

    public static void fixSql(Properties settings) {
        Enumeration<Object> keys = settings.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            String val = settings.getProperty(key);
            if (val.startsWith("DELETE USERS ")) {
                val = "DELETE FROM USERS " + val.substring("DELETE USERS ".length());
            }
            if (val.equals("SELECT * FROM USERS WHERE SERVER_GROUP=?")) {
                val = "SELECT * FROM USERS WHERE SERVER_GROUP=? order by USERNAME";
            }
            if (val.equals("SELECT * FROM USERS WHERE SERVER_GROUP=? and USERNAME=?")) {
                val = "SELECT * FROM USERS WHERE SERVER_GROUP=? and UPPER(USERNAME)=UPPER(?)";
            }
            if (val.equals("SELECT * FROM USERS WHERE SERVER_GROUP=? and USERNAME=UPPER(?)")) {
                val = "SELECT * FROM USERS WHERE SERVER_GROUP=? and UPPER(USERNAME)=UPPER(?)";
            }
            if (val.equals("DELETE FROM USERS WHERE USERNAME=? AND SERVER_GROUP=?")) {
                val = "DELETE FROM USERS WHERE UPPER(USERNAME)=UPPER(?) AND SERVER_GROUP=?";
            }
            if (val.equals("UPDATE USERS SET USERNAME=?, PASSWORD=? WHERE USERNAME=? AND SERVER_GROUP=?")) {
                val = "UPDATE USERS SET USERNAME=?, PASSWORD=? WHERE UPPER(USERNAME)=UPPER(?) AND SERVER_GROUP=?";
            }
            if (val.equals("INSERT INTO WEB_BUTTONS (USERID,SQL_FIELD_KEY,SQL_FIELD_VALUE,SORT_ORDER) VALUES(?,?,?,?)")) {
                val = "INSERT INTO WEB_BUTTONS (USERID,SQL_FIELD_KEY,SQL_FIELD_VALUE,FOR_MENU,FOR_CONTEXT_MENU,SORT_ORDER) VALUES(?,?,?,?,?,?)";
            }
            settings.put(key, val);
        }
    }

    @Override
    public Properties buildVFS(String serverGroup, String username) {
        CIProperties virtual = new CIProperties();
        Properties permissions0 = new Properties();
        this.msg("Connecting to db.");
        Connection conn = null;
        try {
            conn = this.getConnection();
            Properties user = this.findUser(conn, serverGroup, username);
            if (user == null) {
                Properties p = new Properties();
                p.put("virtualPath", "/");
                p.put("name", "VFS");
                p.put("type", "DIR");
                p.put("vItems", new Vector());
                virtual.put("/", p);
                permissions0 = new Properties();
                permissions0.put("/", "(read)(view)(resume)");
                Vector<Properties> permissions = new Vector<Properties>();
                permissions.addElement(permissions0);
                virtual.put("vfs_permissions_object", permissions);
                CIProperties cIProperties = virtual;
                return cIProperties;
            }
            try {
                String id = user.getProperty("id");
                Statement statement = conn.createStatement();
                Vector<Properties> permissions = new Vector<Properties>();
                Properties vfs = new Properties();
                Properties vfs_properties = new Properties();
                Vector vfs_properties_list = null;
                try {
                    this.loadTable(id, "VFS_PERMISSIONS", permissions0, statement, true, null, null, "PATH", "PRIVS");
                    permissions.addElement(permissions0);
                    virtual.put("vfs_permissions_object", permissions);
                    this.loadTable(id, "VFS", vfs, statement, false, "SORT_ORDER", null, "", "");
                    try {
                        this.loadTable(id, "VFS_PROPERTIES", vfs_properties, statement, false, null, null, "", "");
                    }
                    catch (Throwable e) {
                        this.msg(e);
                    }
                    vfs_properties_list = (Vector)vfs_properties.get("VFS_PROPERTIES");
                    if (vfs_properties_list == null) {
                        vfs_properties_list = new Vector();
                    }
                }
                finally {
                    statement.close();
                }
                Vector v = (Vector)vfs.get("VFS");
                if (v != null) {
                    int x = 0;
                    while (x < v.size()) {
                        Properties p = (Properties)v.elementAt(x);
                        String path = p.getProperty("path");
                        int xx = 0;
                        while (xx < vfs_properties_list.size()) {
                            Properties pp = (Properties)vfs_properties_list.elementAt(xx);
                            if (pp.getProperty("path").equals(p.getProperty("path"))) {
                                p.put(pp.getProperty("prop_name"), pp.getProperty("prop_value"));
                            }
                            ++xx;
                        }
                        if (!path.startsWith("/")) {
                            path = "/" + path;
                        }
                        p.remove("path");
                        if (p.containsKey("url")) {
                            String url = crushftp.handlers.Common.url_decode(p.getProperty("url"));
                            if (p.getProperty("encrypted", "false").equals("true") && !url.contains("://")) {
                                if (!p.getProperty("encrypted_class", "").trim().equals("")) {
                                    try {
                                        Class<?> c = ServerStatus.clasLoader.loadClass(p.getProperty("encrypted_class").trim());
                                        Constructor<?> cons = c.getConstructor(new Properties().getClass(), new String().getClass());
                                        cons.newInstance(p, "decrypt");
                                    }
                                    catch (Exception e) {
                                        Log.log("USER_OBJ", 1, e);
                                        p.put("url", url);
                                    }
                                } else {
                                    try {
                                        p.put("url", new crushftp.handlers.Common().decode_pass(url));
                                    }
                                    catch (Exception e) {
                                        Log.log("USER_OBJ", 1, e);
                                        p.put("url", url);
                                    }
                                }
                            } else {
                                p.put("url", url);
                            }
                        }
                        Properties pp = new Properties();
                        pp.put("name", crushftp.handlers.Common.last(path));
                        if (!pp.getProperty("name").equals("") && !pp.getProperty("name").equals("/")) {
                            if (p.getProperty("type").equalsIgnoreCase("vdir")) {
                                pp.put("type", "DIR");
                            } else {
                                pp.put("type", "FILE");
                                Vector<Properties> vv = new Vector<Properties>();
                                vv.addElement(p);
                                pp.put("vItems", vv);
                            }
                            pp.put("virtualPath", path);
                            virtual.put(path, pp);
                        }
                        ++x;
                    }
                }
                if (!virtual.containsKey("/")) {
                    Properties p = new Properties();
                    p.put("virtualPath", "");
                    p.put("name", "VFS");
                    p.put("type", "DIR");
                    virtual.put("/", p);
                }
            }
            catch (Throwable e) {
                this.msg(e);
                virtual = null;
                try {
                    conn.close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        finally {
            this.releaseConnection(conn);
        }
        return virtual;
    }

    private Properties findUser(Connection conn, String serverGroup, String username) throws Exception {
        username = crushftp.handlers.Common.safe_xss_filename(username);
        Properties user = null;
        ResultSet rs = null;
        try (Statement ps = null;){
            ps = conn.prepareStatement(this.settings.getProperty("db_user_query"));
            this.msg("Connected.");
            ps.setString(1, serverGroup);
            ps.setString(2, username);
            rs = ps.executeQuery();
            this.msg("Querying DB for user:" + this.settings.getProperty("db_user_query"));
            if (rs.next()) {
                this.msg("Found user.");
                user = new Properties();
                user.put("id", rs.getString("USERID"));
                user.put("username", rs.getString("USERNAME"));
                user.put("password", rs.getString("PASSWORD") == null ? "" : rs.getString("PASSWORD"));
            }
            rs.close();
            this.msg("Done loading user info.");
        }
        return user;
    }

    @Override
    public Properties loadGroups(String serverGroup) {
        Properties groups;
        block17: {
            this.msg("Loading groups from DB");
            groups = new Properties();
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            Statement s = null;
            try {
                try {
                    this.msg("Connecting to db.");
                    conn = this.getConnection();
                    s = conn.createStatement();
                    try {
                        String query = this.settings.getProperty("db_groups_query");
                        if (query.equals("SELECT * FROM GROUPS G, USERS U WHERE G.USERID = U.USERID AND G.SERVER_GROUP=?") || query.equals("SELECT * FROM `GROUPS` G, USERS U WHERE G.USERID = U.USERID AND G.SERVER_GROUP=?")) {
                            query = crushftp.handlers.Common.replace_str(query, ", USERS U WHERE G.USERID = U.USERID AND G.SERVER_GROUP=?", " LEFT JOIN USERS ON G.USERID = USERS.USERID WHERE G.SERVER_GROUP=?");
                        }
                        if (this.settings.getProperty("db_mysql_groups_compatibility", "false").equalsIgnoreCase("true") && this.settings.getProperty("db_driver", "").toLowerCase().indexOf("sqlserver") < 0) {
                            query = crushftp.handlers.Common.replace_str(this.settings.getProperty("db_groups_query"), " GROUPS ", " `GROUPS` ");
                        }
                        ps = conn.prepareStatement(query);
                        try {
                            this.msg("Connected.  Searching for serverGroup:" + serverGroup);
                            this.msg("Executing sql:" + query);
                            ps.setString(1, serverGroup);
                            rs = ps.executeQuery();
                            while (rs.next()) {
                                Vector<String> v = (Vector<String>)groups.get(rs.getString("GROUPNAME"));
                                if (v == null) {
                                    v = new Vector<String>();
                                }
                                groups.put(rs.getString("GROUPNAME"), v);
                                if (rs.getString("USERNAME") == null) continue;
                                v.addElement(rs.getString("USERNAME"));
                            }
                            rs.close();
                        }
                        finally {
                            ps.close();
                        }
                    }
                    finally {
                        s.close();
                    }
                }
                catch (Throwable e) {
                    this.msg(e);
                    groups = null;
                    try {
                        conn.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    this.releaseConnection(conn);
                    break block17;
                }
            }
            catch (Throwable throwable) {
                this.releaseConnection(conn);
                throw throwable;
            }
            this.releaseConnection(conn);
        }
        return groups;
    }

    @Override
    public void writeGroups(String serverGroup, Properties groups) {
        block24: {
            if (this.settings.getProperty("db_read_only", "false").equals("true")) {
                return;
            }
            this.msg("Writing groups to DB");
            Connection conn = null;
            PreparedStatement ps = null;
            PreparedStatement insertGroup = null;
            try {
                try {
                    this.msg("Connecting to db.");
                    conn = this.getConnection();
                    ResultSet rs = null;
                    String query = this.settings.getProperty("db_groups_delete");
                    if (this.settings.getProperty("db_mysql_groups_compatibility", "false").equalsIgnoreCase("true")) {
                        query = crushftp.handlers.Common.replace_str(this.settings.getProperty("db_groups_delete"), " GROUPS ", " `GROUPS` ");
                    }
                    ps = conn.prepareStatement(query);
                    try {
                        ps.setString(1, serverGroup);
                        this.msg("Connected.");
                        ps.executeUpdate();
                    }
                    finally {
                        ps.close();
                    }
                    ps = conn.prepareStatement(this.settings.getProperty("db_users_query"));
                    try {
                        this.msg("Connected.");
                        ps.setString(1, serverGroup);
                        rs = ps.executeQuery();
                        String id = "-1";
                        this.msg("Querying DB for user:" + this.settings.getProperty("db_users_query"));
                        String query_groups_insert = this.settings.getProperty("db_groups_insert");
                        if (this.settings.getProperty("db_mysql_groups_compatibility", "false").equalsIgnoreCase("true")) {
                            query_groups_insert = crushftp.handlers.Common.replace_str(this.settings.getProperty("db_groups_insert"), " GROUPS ", " `GROUPS` ");
                        }
                        insertGroup = conn.prepareStatement(query_groups_insert);
                        try {
                            String key;
                            Enumeration<Object> keys;
                            while (rs.next()) {
                                String username = rs.getString("USERNAME");
                                id = rs.getString("USERID");
                                keys = groups.keys();
                                while (keys.hasMoreElements()) {
                                    key = keys.nextElement().toString();
                                    Vector v = (Vector)groups.get(key);
                                    if (v.indexOf(username) < 0) continue;
                                    this.msg("Adding user (" + id + ") to group:" + key);
                                    insertGroup.setString(1, key);
                                    insertGroup.setString(2, id);
                                    insertGroup.setString(3, serverGroup);
                                    insertGroup.executeUpdate();
                                }
                            }
                            Properties added_groups = this.loadGroups(serverGroup);
                            keys = groups.keys();
                            while (keys.hasMoreElements()) {
                                key = keys.nextElement().toString();
                                if (added_groups.containsKey(key)) continue;
                                this.msg("Adding group:" + key);
                                insertGroup.setString(1, key);
                                insertGroup.setString(2, null);
                                insertGroup.setString(3, serverGroup);
                                insertGroup.executeUpdate();
                            }
                        }
                        finally {
                            insertGroup.close();
                        }
                        rs.close();
                    }
                    finally {
                        ps.close();
                    }
                }
                catch (Throwable e) {
                    this.msg(e);
                    try {
                        conn.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    this.releaseConnection(conn);
                    break block24;
                }
            }
            catch (Throwable throwable) {
                this.releaseConnection(conn);
                throw throwable;
            }
            this.releaseConnection(conn);
        }
    }

    @Override
    public void writeVFS(String serverGroup, String username, Properties virtual) {
        this.writeVFS(serverGroup, username, virtual, true);
    }

    private void writeVFS(String serverGroup, String username, Properties virtual, boolean deleteFirst) {
        Properties user;
        PreparedStatement ps2;
        PreparedStatement ps;
        Connection conn;
        block54: {
            if (this.settings.getProperty("db_read_only", "false").equals("true")) {
                return;
            }
            conn = null;
            ps = null;
            ps2 = null;
            this.msg("Connecting to db.");
            conn = this.getConnection();
            user = this.findUser(conn, serverGroup, username);
            if (user != null) break block54;
            this.releaseConnection(conn);
            return;
        }
        try {
            try {
                String id = user.getProperty("id");
                Vector permissions = (Vector)virtual.get("vfs_permissions_object");
                if (permissions != null) {
                    Properties permissions0 = new Properties();
                    permissions0.put("/", "(read)(view)(resume)");
                    if (permissions != null && permissions.size() > 0) {
                        permissions0 = (Properties)permissions.elementAt(0);
                    }
                    if (deleteFirst) {
                        this.msg("Deleting user db_user_vfs_permissions_delete:" + id);
                        ps = conn.prepareStatement(this.settings.getProperty("db_user_vfs_permissions_delete"));
                        try {
                            ps.setString(1, id);
                            ps.executeUpdate();
                        }
                        finally {
                            ps.close();
                        }
                    }
                    this.msg("Writing user db_user_vfs_permissions_insert:" + id);
                    ps = conn.prepareStatement(this.settings.getProperty("db_user_vfs_permissions_insert"));
                    try {
                        Enumeration<Object> keys = permissions0.keys();
                        while (keys.hasMoreElements()) {
                            String key = keys.nextElement().toString();
                            ps.setString(1, id);
                            ps.setString(2, key.toUpperCase());
                            ps.setString(3, permissions0.getProperty(key));
                            ps.execute();
                        }
                    }
                    finally {
                        ps.close();
                    }
                }
                if (deleteFirst) {
                    this.msg("Deleting user db_user_vfs_delete:" + id);
                    ps = conn.prepareStatement(this.settings.getProperty("db_user_vfs_delete"));
                    try {
                        ps.setString(1, id);
                        ps.executeUpdate();
                    }
                    finally {
                        ps.close();
                    }
                    this.msg("Deleting user db_user_vfs_properties_delete:" + id);
                    try {
                        ps = conn.prepareStatement(this.settings.getProperty("db_user_vfs_properties_delete"));
                        try {
                            ps.setString(1, id);
                            ps.executeUpdate();
                        }
                        finally {
                            ps.close();
                        }
                    }
                    catch (Throwable e) {
                        this.msg(e);
                    }
                }
                Enumeration<Object> keys = virtual.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement().toString();
                    if (key.equals("vfs_permissions_object")) continue;
                    Properties pp = (Properties)virtual.get(key);
                    String virtualPath = pp.getProperty("virtualPath");
                    if (pp.getProperty("type").equalsIgnoreCase("DIR")) {
                        this.msg("Writing VFS (dir) item " + virtualPath + ":" + this.settings.getProperty("db_user_vfs_vdir_insert"));
                        ps2 = conn.prepareStatement(this.settings.getProperty("db_user_vfs_vdir_insert"));
                        try {
                            ps2.setString(1, id);
                            ps2.setString(2, "");
                            ps2.setString(3, "vdir");
                            ps2.setString(4, virtualPath);
                            ps2.setString(5, "0");
                            ps2.execute();
                            continue;
                        }
                        finally {
                            ps2.close();
                        }
                    }
                    this.msg("Writing VFS (file) item " + virtualPath + ":" + this.settings.getProperty("db_user_vfs_insert"));
                    Vector v = (Vector)pp.get("vItems");
                    int x = 0;
                    while (x < v.size()) {
                        Properties p = (Properties)v.elementAt(x);
                        p.put("path", virtualPath);
                        if (p.getProperty("encrypted", "false").equals("true")) {
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
                        String sql_insert = this.settings.getProperty("db_user_vfs_insert");
                        ps2 = conn.prepareStatement(sql_insert);
                        try {
                            String[] fields = sql_insert.substring(sql_insert.indexOf("(") + 1, sql_insert.indexOf(")")).split(",");
                            int xx = 0;
                            while (xx < fields.length) {
                                if (fields[xx].trim().equalsIgnoreCase("USERID")) {
                                    ps2.setString(xx + 1, id);
                                } else if (fields[xx].trim().equalsIgnoreCase("SORT_ORDER")) {
                                    ps2.setString(xx + 1, String.valueOf(x));
                                } else {
                                    String key2 = fields[xx].trim();
                                    if (key2.toUpperCase().startsWith("ORACLE_")) {
                                        key2 = key2.substring("ORACLE_".length());
                                    }
                                    if (key2.toUpperCase().startsWith("SQL_FIELD_")) {
                                        key2 = key2.substring("SQL_FIELD_".length());
                                    }
                                    ps2.setString(xx + 1, p.getProperty(key2.toLowerCase()));
                                }
                                ++xx;
                            }
                            ps2.execute();
                        }
                        finally {
                            ps2.close();
                        }
                        sql_insert = this.settings.getProperty("db_user_vfs_properties_insert");
                        ps2 = conn.prepareStatement(sql_insert);
                        try {
                            Enumeration<Object> keys2 = p.keys();
                            while (keys2.hasMoreElements()) {
                                String key2 = keys2.nextElement().toString();
                                if (key2.equalsIgnoreCase("userid") || key2.equalsIgnoreCase("url") || key2.equalsIgnoreCase("path") || key2.equalsIgnoreCase("type") || key2.equalsIgnoreCase("sort_order")) continue;
                                String val2 = p.get(key2).toString();
                                ps2.setString(1, id);
                                ps2.setString(2, virtualPath);
                                ps2.setString(3, key2);
                                ps2.setString(4, val2);
                                try {
                                    ps2.execute();
                                }
                                catch (Throwable e) {
                                    this.msg(e);
                                }
                            }
                        }
                        finally {
                            ps2.close();
                        }
                        ++x;
                    }
                }
            }
            catch (Throwable e) {
                this.msg(e);
                try {
                    conn.close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                this.releaseConnection(conn);
            }
        }
        catch (Throwable throwable) {
            this.releaseConnection(conn);
            throw throwable;
        }
        this.releaseConnection(conn);
    }

    @Override
    public Properties loadInheritance(String serverGroup) {
        Properties inheritanceProp;
        block25: {
            Connection conn;
            block22: {
                this.msg("Loading inheritance from DB");
                inheritanceProp = new Properties();
                conn = null;
                PreparedStatement ps = null;
                Statement statement = null;
                Statement s = null;
                try {
                    try {
                        String m1 = this.cache.getProperty(String.valueOf(serverGroup) + "_INHERITANCE");
                        String m2 = this.getModified(serverGroup, "INHERITANCE");
                        String cacheId = String.valueOf(serverGroup) + "_INHERITANCE";
                        if (m1 != null && m2 != null && m1.equals(m2) && this.inheritanceCache.containsKey(cacheId)) {
                            inheritanceProp = (Properties)this.inheritanceCache.get(cacheId);
                            inheritanceProp = (Properties)Common.CLONE(inheritanceProp);
                            this.msg("Loaded inheritance from cache.");
                            break block22;
                        }
                        if (m1 != null && m2 != null && !m1.equals(m2)) {
                            this.cache.put(String.valueOf(serverGroup) + "_INHERITANCE", this.getModified(serverGroup, "INHERITANCE"));
                        }
                        if (m1 == null && m2 != null) {
                            this.cache.put(String.valueOf(serverGroup) + "_INHERITANCE", m2);
                        }
                        if (m1 == null && m2 == null) {
                            this.resetCache(serverGroup, "INHERITANCE");
                            this.cache.put(String.valueOf(serverGroup) + "_INHERITANCE", this.getModified(serverGroup, "INHERITANCE"));
                        }
                        this.msg("Connecting to db.");
                        conn = this.getConnection();
                        ResultSet rs = null;
                        s = conn.createStatement();
                        try {
                            ps = conn.prepareStatement(this.settings.getProperty("db_users_query"));
                            try {
                                this.msg("Connected.");
                                ps.setString(1, serverGroup);
                                rs = ps.executeQuery();
                                String id = "-1";
                                this.msg("Querying DB for user:" + this.settings.getProperty("db_user_query"));
                                statement = conn.createStatement();
                                try {
                                    while (rs.next()) {
                                        String username = rs.getString("USERNAME");
                                        id = rs.getString("USERID");
                                        this.msg("Loading user inheritance...");
                                        Properties p = new Properties();
                                        this.loadTable(id, "INHERITANCE", p, statement, false, "SORT_ORDER", null, "", "");
                                        Vector v = (Vector)p.remove("INHERITANCE");
                                        Vector<String> inheritance = new Vector<String>();
                                        if (v != null) {
                                            int x = 0;
                                            while (x < v.size()) {
                                                inheritance.addElement(((Properties)v.elementAt(x)).getProperty("inherit_username"));
                                                ++x;
                                            }
                                        }
                                        inheritanceProp.put(username, inheritance);
                                    }
                                }
                                finally {
                                    statement.close();
                                }
                                rs.close();
                            }
                            finally {
                                ps.close();
                            }
                        }
                        finally {
                            s.close();
                        }
                        statement.close();
                        this.cacheInheritanceItem(cacheId, inheritanceProp);
                    }
                    catch (Throwable e) {
                        this.msg(e);
                        try {
                            conn.close();
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        this.releaseConnection(conn);
                        break block25;
                    }
                }
                catch (Throwable throwable) {
                    this.releaseConnection(conn);
                    throw throwable;
                }
            }
            this.releaseConnection(conn);
        }
        return inheritanceProp;
    }

    @Override
    public Properties loadUser(String serverGroup, String username, Properties inheritance, boolean flattenUser, boolean allow_update) {
        Vector ichain;
        Properties user;
        boolean found_user;
        block42: {
            username = crushftp.handlers.Common.safe_xss_filename(username);
            this.msg("Loading user from DB:" + username);
            found_user = false;
            Connection conn = null;
            user = new Properties();
            ichain = new Vector();
            ichain.addElement("default");
            if (username.trim().length() > 0) {
                block40: {
                    PreparedStatement ps = null;
                    Statement statement = null;
                    try {
                        try {
                            String cacheId;
                            block41: {
                                String m1 = this.cache.getProperty(String.valueOf(serverGroup) + "_USERS");
                                String m2 = this.getModified(serverGroup, "USERS");
                                Log.log("USER_OBJ", 3, "CACHE: " + serverGroup + " :USERS:local cached date=" + m1);
                                cacheId = String.valueOf(serverGroup) + "_" + username.toUpperCase() + "_" + flattenUser;
                                if (m1 != null && m2 != null && m1.equals(m2) && this.userCache.containsKey(cacheId)) {
                                    user = (Properties)this.userCache.get(cacheId);
                                    user = (Properties)Common.CLONE(user);
                                    this.msg("Loaded user from cache.");
                                    if (user.get("linked_vfs") != null && user.get("linked_vfs") instanceof String) {
                                        Vector<String> v = new Vector<String>();
                                        String[] lvs = user.getProperty("linked_vfs", "").split(",");
                                        int x = 0;
                                        while (x < lvs.length) {
                                            if (!lvs[x].trim().equals("")) {
                                                v.addElement(lvs[x].trim());
                                            }
                                            ++x;
                                        }
                                        user.put("linked_vfs", v);
                                        if (v.size() == 0) {
                                            user.remove("linked_vfs");
                                        }
                                    }
                                    found_user = true;
                                    break block40;
                                }
                                if (m1 != null && m2 != null && !m1.equals(m2)) {
                                    this.msg("RESETTING SQL CACHE:" + serverGroup + ":USERS:local cached date=" + m1 + " versus DB date=" + m2);
                                    this.userCache.clear();
                                    this.cache.put(String.valueOf(serverGroup) + "_USERS", this.getModified(serverGroup, "USERS"));
                                }
                                if (m1 == null && m2 != null) {
                                    this.cache.put(String.valueOf(serverGroup) + "_USERS", m2);
                                }
                                if (m1 == null && m2 == null) {
                                    this.resetCache(serverGroup, "USERS");
                                    this.cache.put(String.valueOf(serverGroup) + "_USERS", this.getModified(serverGroup, "USERS"));
                                }
                                this.msg("Connecting to db.");
                                conn = this.getConnection();
                                ResultSet rs = null;
                                ps = conn.prepareStatement(this.settings.getProperty("db_user_query"));
                                String id = "-1";
                                try {
                                    this.msg("Connected.");
                                    ps.setString(1, serverGroup);
                                    ps.setString(2, username);
                                    rs = ps.executeQuery();
                                    this.msg("Querying DB for user:" + this.settings.getProperty("db_user_query"));
                                    if (rs.next()) {
                                        this.msg("Found user.");
                                        found_user = true;
                                        id = rs.getString("USERID");
                                        user.put("username", rs.getString("USERNAME"));
                                        user.put("password", rs.getString("PASSWORD") == null ? "" : rs.getString("PASSWORD"));
                                    }
                                    rs.close();
                                    this.msg("Done loading user info.");
                                    statement = conn.createStatement();
                                    try {
                                        if (id.equals("-1")) {
                                            user = null;
                                            break block41;
                                        }
                                        Enumeration<Object> keys = inheritance.keys();
                                        while (keys.hasMoreElements()) {
                                            String key = keys.nextElement().toString();
                                            if (!key.equalsIgnoreCase(username)) continue;
                                            ichain = (Vector)inheritance.get(key);
                                            break;
                                        }
                                        if (ichain == null || ichain.size() == 0) {
                                            ichain = new Vector();
                                            ichain.addElement("default");
                                        }
                                        if (ichain.size() == 1 && !ichain.elementAt(0).toString().equals("default")) {
                                            ichain.insertElementAt("default", 0);
                                        }
                                        Properties tempUser = (Properties)user.clone();
                                        int x = 0;
                                        while (x < ichain.size()) {
                                            String iusername = ichain.elementAt(x).toString();
                                            this.msg("Loading inherited user settings...:" + iusername);
                                            ps.setString(1, serverGroup);
                                            ps.setString(2, iusername);
                                            rs = ps.executeQuery();
                                            this.msg("Querying DB for inherited userid:" + this.settings.getProperty("db_user_query"));
                                            if (rs.next()) {
                                                this.msg("Got inherited userid...:" + rs.getString("USERID"));
                                                Properties pp = new Properties();
                                                this.loadUserItems(rs.getString("USERID"), pp, statement);
                                                pp.remove("password");
                                                Properties originalUser = (Properties)user.clone();
                                                this.loadUserItems(id, originalUser, statement);
                                                UserTools.mergeWebCustomizations(user, pp);
                                                UserTools.mergeLinkedVFS(user, pp);
                                                if (!user.containsKey("admin_group_name")) {
                                                    user.put("admin_group_name", "");
                                                }
                                                UserTools.mergeGroupAdminNames(user, pp);
                                                UserTools.mergeEvents(user, pp);
                                                try {
                                                    GregorianCalendar gc;
                                                    boolean needWrite = false;
                                                    if (originalUser.getProperty("account_expire") == null && !pp.getProperty("account_expire", "").equals("") && !pp.getProperty("account_expire_rolling_days", "0").equals("") && Integer.parseInt(pp.getProperty("account_expire_rolling_days", "0")) > 0) {
                                                        gc = new GregorianCalendar();
                                                        gc.setTime(new Date());
                                                        gc.add(5, Integer.parseInt(pp.getProperty("account_expire_rolling_days")));
                                                        pp.put("account_expire", new SimpleDateFormat("MM/dd/yy hh:mm aa", Locale.US).format(gc.getTime()));
                                                        originalUser.put("account_expire", pp.getProperty("account_expire"));
                                                        originalUser.put("account_expire_rolling_days", pp.getProperty("account_expire_rolling_days"));
                                                        needWrite = true;
                                                    }
                                                    if (originalUser.getProperty("expire_password", "").equals("") && !pp.getProperty("expire_password", "").equals("") && !pp.getProperty("expire_password", "").equals("false")) {
                                                        gc = new GregorianCalendar();
                                                        gc.setTime(new Date());
                                                        gc.add(5, Integer.parseInt(pp.getProperty("expire_password_days")));
                                                        pp.put("expire_password_when", new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US).format(gc.getTime()));
                                                        originalUser.put("expire_password_when", pp.getProperty("expire_password_when"));
                                                        originalUser.put("expire_password_days", pp.getProperty("expire_password_days"));
                                                        originalUser.put("expire_password", pp.getProperty("expire_password"));
                                                        needWrite = true;
                                                    }
                                                    if (needWrite && allow_update) {
                                                        this.writeUser(serverGroup, username, originalUser, false);
                                                    }
                                                }
                                                catch (Exception e) {
                                                    crushftp.handlers.Common.debug(1, e);
                                                }
                                                if (flattenUser) {
                                                    tempUser.putAll((Map<?, ?>)pp);
                                                }
                                            }
                                            rs.close();
                                            ++x;
                                        }
                                        if (flattenUser) {
                                            user.putAll((Map<?, ?>)tempUser);
                                        }
                                        user.put("username", username);
                                        user.put("user_name", username);
                                        Properties pp = new Properties();
                                        this.loadUserItems(id, pp, statement);
                                        pp.remove("password");
                                        user.putAll((Map<?, ?>)pp);
                                    }
                                    finally {
                                        statement.close();
                                    }
                                }
                                finally {
                                    ps.close();
                                }
                            }
                            if (found_user) {
                                this.cacheUserItem(cacheId, user);
                            }
                        }
                        catch (Throwable e) {
                            this.msg(e);
                            try {
                                conn.close();
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            this.releaseConnection(conn);
                            break block42;
                        }
                    }
                    catch (Throwable throwable) {
                        this.releaseConnection(conn);
                        throw throwable;
                    }
                }
                this.releaseConnection(conn);
            }
        }
        if (found_user) {
            this.msg("Loaded user.");
        } else {
            this.msg("User not found.");
        }
        if (user != null) {
            user.put("ichain", ichain);
        }
        return user;
    }

    @Override
    public void updateUser(String serverGroup, String username1, String username2, String password) {
        if (this.settings.getProperty("db_read_only", "false").equals("true")) {
            return;
        }
        if (username2 != null) {
            username1 = crushftp.handlers.Common.safe_xss_filename(username1);
            username2 = crushftp.handlers.Common.safe_xss_filename(username2);
            this.msg("Updating user in DB:" + username2);
            Connection conn = null;
            if (username1.trim().length() > 0 && username2.trim().length() > 0) {
                PreparedStatement ps = null;
                try {
                    try {
                        this.msg("Connecting to db.");
                        conn = this.getConnection();
                        ps = conn.prepareStatement(this.settings.getProperty("db_user_update"));
                        try {
                            this.msg("Connected.");
                            ps.setString(1, username2);
                            ps.setString(2, password);
                            ps.setString(3, username1);
                            ps.setString(4, serverGroup);
                            ps.executeUpdate();
                        }
                        finally {
                            ps.close();
                        }
                    }
                    catch (Throwable e) {
                        this.msg(e);
                        try {
                            conn.close();
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        throw new RuntimeException(e);
                    }
                }
                catch (Throwable throwable) {
                    this.releaseConnection(conn);
                    throw throwable;
                }
                this.releaseConnection(conn);
                if (!username1.equals(username2)) {
                    this.resetCache(serverGroup, "USERS");
                } else {
                    Properties user;
                    if (ServerStatus.BG("sql_users_reset_cache_on_changes")) {
                        this.resetCache(serverGroup, "USERS");
                    }
                    if ((user = (Properties)this.userCache.get(String.valueOf(serverGroup) + "_" + username1.toUpperCase() + "_false")) != null && password != null) {
                        user.put("password", password);
                    }
                    if ((user = (Properties)this.userCache.get(String.valueOf(serverGroup) + "_" + username1.toUpperCase() + "_true")) != null && password != null) {
                        user.put("password", password);
                    }
                }
            }
        }
    }

    @Override
    public void writeUser(String serverGroup, String username, Properties user, boolean backup) {
        this.writeUser(serverGroup, username, user, backup, false);
    }

    @Override
    public void writeUser(String serverGroup, String username, Properties user, boolean backup, boolean clear_only_user_related_xml_from_cache) {
        if (this.settings.getProperty("db_read_only", "false").equals("true")) {
            return;
        }
        username = crushftp.handlers.Common.safe_xss_filename(username);
        this.msg("Writing user to DB:" + username);
        user = (Properties)user.clone();
        Vector lvsv = (Vector)user.get("linked_vfs");
        if (lvsv == null) {
            lvsv = new Vector();
        }
        String lvs = "";
        int x = 0;
        while (x < lvsv.size()) {
            lvs = String.valueOf(lvs) + lvsv.elementAt(x).toString().trim() + ",";
            ++x;
        }
        if (lvs.length() > 0) {
            lvs = lvs.substring(0, lvs.length() - 1);
        }
        user.put("linked_vfs", lvs);
        if (lvsv.size() == 0) {
            user.remove("linked_vfs");
        }
        Connection conn = null;
        if (username.trim().length() > 0) {
            PreparedStatement ps = null;
            PreparedStatement ps2 = null;
            PreparedStatement event_delete = null;
            PreparedStatement event_insert = null;
            PreparedStatement ps_insert_user_properties = null;
            try {
                try {
                    String id;
                    block49: {
                        this.msg("Connecting to db.");
                        conn = this.getConnection();
                        ResultSet rs = null;
                        ps = conn.prepareStatement(this.settings.getProperty("db_user_query"));
                        id = "-1";
                        try {
                            this.msg("Connected.");
                            ps.setString(1, serverGroup);
                            ps.setString(2, username);
                            rs = ps.executeQuery();
                            this.msg("Querying DB for user:" + this.settings.getProperty("db_user_query"));
                            if (rs.next()) {
                                id = rs.getString("USERID");
                                this.msg("Found user:" + id);
                                rs.close();
                                this.updateUser(serverGroup, username, username, user.getProperty("password"));
                                break block49;
                            }
                            rs.close();
                            ps2 = conn.prepareStatement(this.settings.getProperty("db_user_insert"));
                            try {
                                String pass = user.getProperty("password", "");
                                if (pass.equals("")) {
                                    pass = crushftp.handlers.Common.makeBoundary();
                                }
                                ps2.setString(1, username);
                                ps2.setString(2, pass);
                                ps2.setString(3, serverGroup);
                                ps2.execute();
                            }
                            finally {
                                ps2.close();
                            }
                            rs = ps.executeQuery();
                            rs.next();
                            id = rs.getString("USERID");
                            this.msg("Created new user:" + id);
                            rs.close();
                        }
                        finally {
                            ps.close();
                        }
                    }
                    this.deleteUserProperties(id, conn, true);
                    ps_insert_user_properties = conn.prepareStatement(this.settings.getProperty("db_user_properties_insert"));
                    try {
                        Enumeration<Object> keys = user.keys();
                        while (keys.hasMoreElements()) {
                            Vector v;
                            String key = keys.nextElement().toString();
                            Object val = user.get(key);
                            if (key.equalsIgnoreCase("EVENTS")) {
                                this.msg("Writing table:" + key);
                                v = (Vector)user.get(key);
                                if (this.settings.getProperty("db_user_" + key.toLowerCase() + "_delete").toUpperCase().indexOf("EVENTS5") >= 0) {
                                    event_delete = conn.prepareStatement(this.settings.getProperty("db_user_" + key.toLowerCase() + "_delete"));
                                    try {
                                        event_delete.setString(1, id);
                                        event_delete.executeUpdate();
                                    }
                                    finally {
                                        event_delete.close();
                                    }
                                    event_insert = conn.prepareStatement(this.settings.getProperty("db_user_" + key.toLowerCase() + "_insert"));
                                    try {
                                        boolean noname_used = false;
                                        int x2 = 0;
                                        while (x2 < v.size()) {
                                            Properties event = (Properties)v.elementAt(x2);
                                            if (!event.getProperty("event_name", "").startsWith("noname_")) {
                                                Enumeration<Object> event_keys = event.keys();
                                                while (event_keys.hasMoreElements()) {
                                                    String event_key = event_keys.nextElement().toString();
                                                    String event_val = "";
                                                    if (event.get(event_key) instanceof Vector || event.get(event_key) instanceof Properties) {
                                                        crushftp.handlers.Common cfr_ignored_0 = ServerStatus.thisObj.common_code;
                                                        event_val = crushftp.handlers.Common.getXMLString(event.get(event_key), event_key, null);
                                                    } else {
                                                        event_val = event.getProperty(event_key, "");
                                                    }
                                                    String event_name = event.getProperty("name", "");
                                                    if (event_name.equals("")) {
                                                        if (noname_used) break;
                                                        event_name = "noname_" + crushftp.handlers.Common.makeBoundary();
                                                        event.put("name", event_name);
                                                        noname_used = true;
                                                    }
                                                    event_insert.setString(1, id);
                                                    event_insert.setString(2, event_name);
                                                    event_insert.setString(3, event_key);
                                                    event_insert.setString(4, event_val);
                                                    event_insert.executeUpdate();
                                                }
                                            }
                                            ++x2;
                                        }
                                        continue;
                                    }
                                    finally {
                                        event_insert.close();
                                    }
                                }
                                this.doTableDeleteInsert(id, v, conn, this.settings.getProperty("db_user_" + key.toLowerCase() + "_delete"), this.settings.getProperty("db_user_" + key.toLowerCase() + "_insert"));
                                continue;
                            }
                            if (key.equalsIgnoreCase("IP_RESTRICTIONS")) {
                                this.msg("Writing table:" + key);
                                v = (Vector)user.get(key);
                                this.doTableDeleteInsert(id, v, conn, this.settings.getProperty("db_user_" + key.toLowerCase() + "_delete"), this.settings.getProperty("db_user_" + key.toLowerCase() + "_insert"));
                                continue;
                            }
                            if (key.equalsIgnoreCase("WEB_BUTTONS")) {
                                this.msg("Writing table:" + key);
                                v = (Vector)user.get(key);
                                ServerSessionAJAX.fixButtons(v);
                                if (for_menu_buttons || username.equalsIgnoreCase("default")) {
                                    try {
                                        this.doTableDeleteInsert(id, v, conn, this.settings.getProperty("db_user_" + key.toLowerCase() + "_delete"), this.settings.getProperty("db_user_" + key.toLowerCase() + "_insert"));
                                    }
                                    catch (SQLException e) {
                                        Log.log("USER_OBJ", 0, "SQL table WEB_BUTTONS is missing required columns.  Please update the schema.  Buttons not saved.");
                                    }
                                    continue;
                                }
                                Log.log("USER_OBJ", 1, "SQL table WEB_BUTTONS is missing required columns.  Please update the schema.");
                                continue;
                            }
                            if (key.equalsIgnoreCase("WEB_CUSTOMIZATIONS")) {
                                this.msg("Writing table:" + key);
                                v = (Vector)user.get(key);
                                this.doTableDeleteInsert(id, v, conn, this.settings.getProperty("db_user_" + key.toLowerCase() + "_delete"), this.settings.getProperty("db_user_" + key.toLowerCase() + "_insert"));
                                continue;
                            }
                            if (key.equalsIgnoreCase("DOMAIN_ROOT_LIST")) {
                                this.msg("Writing table:" + key);
                                v = (Vector)user.get(key);
                                this.doTableDeleteInsert(id, v, conn, this.settings.getProperty("db_user_" + key.toLowerCase() + "_delete"), this.settings.getProperty("db_user_" + key.toLowerCase() + "_insert"));
                                continue;
                            }
                            this.msg("Writing property:" + key + "=" + val);
                            ps_insert_user_properties.setString(1, id);
                            ps_insert_user_properties.setString(2, key);
                            ps_insert_user_properties.setString(3, val.toString());
                            if (key.equals("password")) continue;
                            ps_insert_user_properties.execute();
                        }
                    }
                    finally {
                        ps_insert_user_properties.close();
                    }
                    this.msg("Finished writing user.");
                }
                catch (Throwable e) {
                    this.msg(e);
                    try {
                        conn.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    throw new RuntimeException(e);
                }
            }
            catch (Throwable throwable) {
                this.releaseConnection(conn);
                throw throwable;
            }
            this.releaseConnection(conn);
        }
        if (ServerStatus.BG("sql_users_reset_cache_on_changes")) {
            this.resetCache(serverGroup, "USERS");
        } else {
            Properties user2 = (Properties)this.userCache.get(String.valueOf(serverGroup) + "_" + username.toUpperCase() + "_false");
            if (user2 != null) {
                user2.putAll((Map<?, ?>)user);
            }
            if ((user2 = (Properties)this.userCache.get(String.valueOf(serverGroup) + "_" + username.toUpperCase() + "_true")) != null) {
                user2.putAll((Map<?, ?>)user);
            }
        }
    }

    @Override
    public void writeInheritance(String serverGroup, Properties inheritance) {
        block21: {
            if (this.settings.getProperty("db_read_only", "false").equals("true")) {
                return;
            }
            this.msg("Writing inheritance to DB");
            Connection conn = null;
            PreparedStatement ps = null;
            PreparedStatement deleteInheritance = null;
            PreparedStatement insertInheritance = null;
            try {
                try {
                    this.msg("Connecting to db.");
                    conn = this.getConnection();
                    ResultSet rs = null;
                    ps = conn.prepareStatement(this.settings.getProperty("db_users_query"));
                    try {
                        this.msg("Connected.");
                        ps.setString(1, serverGroup);
                        rs = ps.executeQuery();
                        String id = "-1";
                        this.msg("Querying DB for users:" + this.settings.getProperty("db_users_query"));
                        deleteInheritance = conn.prepareStatement(this.settings.getProperty("db_user_inheritance_delete"));
                        try {
                            insertInheritance = conn.prepareStatement(this.settings.getProperty("db_user_inheritance_insert"));
                            try {
                                while (rs.next()) {
                                    String username = rs.getString("USERNAME");
                                    id = rs.getString("USERID");
                                    this.msg("Deleting old user inheritance for userid:" + id);
                                    deleteInheritance.setString(1, id);
                                    deleteInheritance.execute();
                                    Vector v = (Vector)inheritance.get(username);
                                    if (v == null) continue;
                                    Vector<String> addedUsers = new Vector<String>();
                                    int x = 0;
                                    while (x < v.size()) {
                                        this.msg("Inserting user inheritance for userid:" + id + "  " + x);
                                        insertInheritance.setString(1, id);
                                        insertInheritance.setString(2, v.elementAt(x).toString());
                                        insertInheritance.setString(3, String.valueOf(x));
                                        if (addedUsers.indexOf(v.elementAt(x).toString()) < 0) {
                                            insertInheritance.execute();
                                        }
                                        addedUsers.addElement(v.elementAt(x).toString());
                                        ++x;
                                    }
                                }
                            }
                            finally {
                                insertInheritance.close();
                            }
                        }
                        finally {
                            deleteInheritance.close();
                        }
                        rs.close();
                    }
                    finally {
                        ps.close();
                    }
                }
                catch (Throwable e) {
                    this.msg(e);
                    try {
                        conn.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    this.releaseConnection(conn);
                    break block21;
                }
            }
            catch (Throwable throwable) {
                this.releaseConnection(conn);
                throw throwable;
            }
            this.releaseConnection(conn);
        }
        this.resetCache(serverGroup, "INHERITANCE");
    }

    @Override
    public void deleteUser(String serverGroup, String username) {
        block29: {
            if (this.settings.getProperty("db_read_only", "false").equals("true")) {
                return;
            }
            username = crushftp.handlers.Common.safe_xss_filename(username);
            this.msg("Deleting user in DB:" + username);
            Connection conn = null;
            if (username.trim().length() > 0) {
                block25: {
                    PreparedStatement ps = null;
                    boolean found_user = false;
                    String id = "-1";
                    try {
                        try {
                            this.msg("Connecting to db.");
                            conn = this.getConnection();
                            ResultSet rs = null;
                            ps = conn.prepareStatement(this.settings.getProperty("db_user_query"));
                            try {
                                this.msg("Connected.");
                                ps.setString(1, serverGroup);
                                ps.setString(2, username);
                                rs = ps.executeQuery();
                                this.msg("Querying DB for user:" + this.settings.getProperty("db_user_query"));
                                if (rs.next()) {
                                    this.msg("Found user.");
                                    id = rs.getString("USERID");
                                    found_user = true;
                                }
                                rs.close();
                                this.msg("Done loading user info.");
                            }
                            finally {
                                ps.close();
                            }
                            if (!found_user) break block25;
                            this.msg("Deleting user db_user_inheritance_delete:" + id);
                            ps = conn.prepareStatement(this.settings.getProperty("db_user_inheritance_delete"));
                            try {
                                ps.setString(1, id);
                                ps.executeUpdate();
                            }
                            finally {
                                ps.close();
                            }
                            this.deleteUserProperties(id, conn, false);
                            this.msg("Deleting user db_user_vfs_delete:" + id);
                            ps = conn.prepareStatement(this.settings.getProperty("db_user_vfs_delete"));
                            try {
                                ps.setString(1, id);
                                ps.executeUpdate();
                            }
                            finally {
                                ps.close();
                            }
                            this.msg("Deleting user db_user_vfs_permissions_delete:" + id);
                            ps = conn.prepareStatement(this.settings.getProperty("db_user_vfs_permissions_delete"));
                            try {
                                ps.setString(1, id);
                                ps.executeUpdate();
                            }
                            finally {
                                ps.close();
                            }
                            this.msg("Deleting user db_user_delete:" + username + ":" + serverGroup);
                            ps = conn.prepareStatement(this.settings.getProperty("db_user_delete"));
                            try {
                                ps.setString(1, username);
                                ps.setString(2, serverGroup);
                                ps.executeUpdate();
                            }
                            finally {
                                ps.close();
                            }
                        }
                        catch (Throwable e) {
                            this.msg(e);
                            try {
                                conn.close();
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            this.releaseConnection(conn);
                            break block29;
                        }
                    }
                    catch (Throwable throwable) {
                        this.releaseConnection(conn);
                        throw throwable;
                    }
                }
                this.releaseConnection(conn);
            }
        }
        this.resetCache(serverGroup, "USERS");
    }

    @Override
    public Vector loadUserList(String serverGroup) {
        Vector<String> v;
        block11: {
            this.msg("Loading user list from DB...");
            v = new Vector<String>();
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                try {
                    this.msg("Connecting to db.");
                    conn = this.getConnection();
                    ResultSet rs = null;
                    ps = conn.prepareStatement(this.settings.getProperty("db_users_query"));
                    try {
                        this.msg("Connected.");
                        ps.setString(1, serverGroup);
                        rs = ps.executeQuery();
                        this.msg("Querying DB for user:" + this.settings.getProperty("db_user_query"));
                        while (rs.next()) {
                            this.msg("Found user.");
                            v.addElement(crushftp.handlers.Common.safe_xss_filename(rs.getString("USERNAME")));
                        }
                        rs.close();
                    }
                    finally {
                        ps.close();
                    }
                }
                catch (Throwable e) {
                    try {
                        conn.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    Log.log("USER_OBJ", 0, e);
                    this.releaseConnection(conn);
                    break block11;
                }
            }
            catch (Throwable throwable) {
                this.releaseConnection(conn);
                throw throwable;
            }
            this.releaseConnection(conn);
        }
        return v;
    }

    @Override
    public Vector findUserEmail(String serverGroup, String email) {
        Vector<Properties> v;
        block12: {
            this.msg("Querying email...");
            v = new Vector<Properties>();
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                try {
                    this.msg("Connecting to db.");
                    conn = this.getConnection();
                    ResultSet rs = null;
                    ps = conn.prepareStatement(this.settings.getProperty("db_user_email_query"));
                    try {
                        this.msg("Connected.");
                        ps.setString(1, serverGroup);
                        ps.setString(2, email);
                        rs = ps.executeQuery();
                        this.msg("Querying DB for user:" + this.settings.getProperty("db_user_email_query"));
                        Properties user_hash = new Properties();
                        while (rs.next()) {
                            this.msg("Found email/user.");
                            if (rs.getString("USERNAME").equals("TempAccount") || rs.getString("USERNAME").startsWith("TempAccount_")) continue;
                            Properties user = this.loadUser(serverGroup, rs.getString("USERNAME"), new Properties(), false, false);
                            if (!user_hash.containsKey(rs.getString("USERNAME"))) {
                                v.addElement(user);
                            }
                            user_hash.put(rs.getString("USERNAME"), "");
                        }
                        rs.close();
                    }
                    finally {
                        ps.close();
                    }
                }
                catch (Throwable e) {
                    try {
                        conn.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    Log.log("USER_OBJ", 0, e);
                    this.releaseConnection(conn);
                    break block12;
                }
            }
            catch (Throwable throwable) {
                this.releaseConnection(conn);
                throw throwable;
            }
            this.releaseConnection(conn);
        }
        return v;
    }

    @Override
    public void addFolder(String serverGroup, String username, String path, String name) {
        Properties virtual = new Properties();
        Properties p = new Properties();
        p.put("virtualPath", String.valueOf(path) + name);
        p.put("name", name);
        p.put("type", "DIR");
        virtual.put(String.valueOf(path) + name, p);
        this.writeVFS(serverGroup, username, virtual, false);
    }

    @Override
    public void addItem(String serverGroup, String username, String path, String name, String url, String type, Properties moreItems, boolean encrypted, String encrypted_class) throws Exception {
        if (this.settings.getProperty("db_read_only", "false").equals("true")) {
            return;
        }
        if (type.toUpperCase().equals("DIR") && !url.endsWith("/")) {
            url = String.valueOf(url) + "/";
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
        }
        Properties virtual = new Properties();
        Properties pp = new Properties();
        pp.put("type", "FILE");
        pp.put("virtualPath", String.valueOf(path) + name);
        pp.put("name", name);
        pp.put("vItems", v);
        virtual.put(String.valueOf(path) + name, pp);
        this.writeVFS(serverGroup, username, virtual, false);
    }

    private void deleteUserProperties(String id, Connection conn, boolean ignore_groups) throws Exception {
        block41: {
            if (this.settings.getProperty("db_read_only", "false").equals("true")) {
                return;
            }
            this.msg("Deleting user db_user_properties_delete:" + id);
            try (Statement ps = null;){
                ps = conn.prepareStatement(this.settings.getProperty("db_user_properties_delete"));
                try {
                    ps.setString(1, id);
                    ps.executeUpdate();
                }
                finally {
                    ps.close();
                }
                this.msg("Deleting user db_user_events_delete:" + id);
                ps = conn.prepareStatement(this.settings.getProperty("db_user_events_delete"));
                try {
                    ps.setString(1, id);
                    try {
                        ps.executeUpdate();
                    }
                    catch (Exception exception) {}
                }
                finally {
                    ps.close();
                }
                ps = conn.prepareStatement(crushftp.handlers.Common.replace_str(this.settings.getProperty("db_user_events_delete"), "DELETE FROM EVENTS WHERE USERID", "DELETE FROM EVENTS5 WHERE USERID"));
                try {
                    ps.setString(1, id);
                    try {
                        ps.executeUpdate();
                    }
                    catch (Exception exception) {}
                }
                finally {
                    ps.close();
                }
                this.msg("Deleting user db_user_domain_root_list_delete:" + id);
                ps = conn.prepareStatement(this.settings.getProperty("db_user_domain_root_list_delete"));
                try {
                    ps.setString(1, id);
                    ps.executeUpdate();
                }
                finally {
                    ps.close();
                }
                this.msg("Deleting user db_user_ip_restrictions_delete:" + id);
                ps = conn.prepareStatement(this.settings.getProperty("db_user_ip_restrictions_delete"));
                try {
                    ps.setString(1, id);
                    ps.executeUpdate();
                }
                finally {
                    ps.close();
                }
                this.msg("Deleting user db_user_web_buttons_delete:" + id);
                ps = conn.prepareStatement(this.settings.getProperty("db_user_web_buttons_delete"));
                try {
                    ps.setString(1, id);
                    ps.executeUpdate();
                }
                finally {
                    ps.close();
                }
                this.msg("Deleting user db_user_web_customizations_delete:" + id);
                ps = conn.prepareStatement(this.settings.getProperty("db_user_web_customizations_delete"));
                try {
                    ps.setString(1, id);
                    ps.executeUpdate();
                }
                finally {
                    ps.close();
                }
                if (ignore_groups) break block41;
                this.msg("Deleting user db_groups_user_delete:" + id);
                String query = this.settings.getProperty("db_groups_user_delete");
                if (this.settings.getProperty("db_mysql_groups_compatibility", "false").equalsIgnoreCase("true")) {
                    query = crushftp.handlers.Common.replace_str(this.settings.getProperty("db_groups_user_delete"), " GROUPS ", " `GROUPS` ");
                }
                ps = conn.prepareStatement(query);
                try {
                    ps.setString(1, id);
                    ps.executeUpdate();
                }
                finally {
                    ps.close();
                }
            }
        }
    }

    private void doTableDeleteInsert(String id, Vector v, Connection conn, String sql_delete, String sql_insert) throws Exception {
        if (this.settings.getProperty("db_read_only", "false").equals("true")) {
            return;
        }
        PreparedStatement ps2 = null;
        ps2 = conn.prepareStatement(sql_delete);
        try {
            ps2.setString(1, id);
            ps2.execute();
        }
        finally {
            ps2.close();
        }
        if (v == null) {
            return;
        }
        ps2 = conn.prepareStatement(sql_insert);
        try {
            int x = 0;
            while (x < v.size()) {
                Properties p = (Properties)v.elementAt(x);
                String[] fields = sql_insert.substring(sql_insert.indexOf("(") + 1, sql_insert.indexOf(")")).split(",");
                int xx = 0;
                while (xx < fields.length) {
                    if (fields[xx].trim().equalsIgnoreCase("USERID")) {
                        ps2.setString(xx + 1, id);
                    } else if (fields[xx].trim().equalsIgnoreCase("SORT_ORDER")) {
                        ps2.setString(xx + 1, String.valueOf(x));
                    } else {
                        String key = fields[xx].trim();
                        if (key.toUpperCase().startsWith("ORACLE_")) {
                            key = key.substring("ORACLE_".length());
                        }
                        if (key.toUpperCase().startsWith("SQL_FIELD_")) {
                            key = key.substring("SQL_FIELD_".length());
                        }
                        ps2.setString(xx + 1, p.getProperty(key.toLowerCase()));
                    }
                    ++xx;
                }
                ps2.execute();
                ++x;
            }
        }
        finally {
            ps2.close();
        }
    }

    private void loadUserItems(String id, Properties user, Statement s) {
        Properties button;
        Vector buttons;
        this.msg("Loading user properties...");
        String formerPass = user.getProperty("password", "");
        this.loadTable(id, "USER_PROPERTIES", user, s, true, null, null, "PROP_NAME", "PROP_VALUE");
        user.put("password", formerPass);
        Vector<String> v = new Vector<String>();
        String[] lvs = user.getProperty("linked_vfs", "").split(",");
        int x = 0;
        while (x < lvs.length) {
            if (!lvs[x].trim().equals("")) {
                v.addElement(lvs[x].trim());
            }
            ++x;
        }
        user.put("linked_vfs", v);
        if (v.size() == 0) {
            user.remove("linked_vfs");
        }
        this.msg("Loading events...");
        this.loadTable(id, "EVENTS5", user, s, true, "event_name", "event_name", "prop_name", "prop_value");
        if (user.get("EVENTS5") == null && loadEvents4) {
            this.loadTable(id, "EVENTS", user, s, false, null, null, "", "");
        } else if (user.containsKey("EVENTS5")) {
            user.put("events", user.remove("EVENTS5"));
        }
        this.msg("Loading ip_restrictions...");
        this.loadTable(id, "IP_RESTRICTIONS", user, s, false, null, null, "", "");
        this.msg("Loading web_buttons...");
        this.loadTable(id, "WEB_BUTTONS", user, s, false, "SORT_ORDER", null, "", "");
        if (user.containsKey("web_buttons") && (buttons = (Vector)user.get("web_buttons")).size() > 0 && (button = (Properties)buttons.elementAt(0)).containsKey("for_menu")) {
            for_menu_buttons = true;
        }
        if (user.getProperty("user_name", "").equalsIgnoreCase("default") && !user.containsKey("web_buttons")) {
            Properties defaultUser = (Properties)crushftp.handlers.Common.readXMLObject(VFS.class.getResource("/assets/default_user.xml"));
            user.put("web_buttons", defaultUser.get("web_buttons"));
        }
        this.msg("Loading web_customizations...");
        this.loadTable(id, "WEB_CUSTOMIZATIONS", user, s, false, null, null, "", "");
        this.msg("Loading domain_root_list...");
        this.loadTable(id, "DOMAIN_ROOT_LIST", user, s, false, "SORT_ORDER", null, "", "");
    }

    private void loadTable(String userid, String table, Properties user, Statement s, boolean propertyMode, String orderby, String groupby, String prop_name, String prop_value) {
        Vector<Properties> v = new Vector<Properties>();
        try {
            String tableKey = table.toLowerCase();
            if (tableKey.equalsIgnoreCase("events5")) {
                tableKey = "events";
            }
            String _sql = this.get("db_user_" + tableKey + "_id_query");
            _sql = crushftp.handlers.Common.replace_str(_sql, "?", "'" + userid.replace('\'', '_') + "'");
            if (orderby != null && _sql.toUpperCase().startsWith("SELECT")) {
                _sql = String.valueOf(_sql) + " ORDER BY " + orderby;
            }
            this.msg("Querying " + table + ":" + _sql);
            ResultSet rs = s.executeQuery(_sql);
            Properties lastProp = new Properties();
            String lastGroupByVal = "";
            while (rs.next()) {
                if (propertyMode) {
                    String key = rs.getString(prop_name);
                    String val = rs.getString(prop_value);
                    if (val == null) {
                        val = "";
                    }
                    if (groupby == null) {
                        if (key != null) {
                            user.put(key, val);
                        }
                    } else {
                        String groupByVal;
                        String altKeyName = groupby;
                        String string = groupByVal = rs.getString(groupby) == null ? "" : rs.getString(groupby);
                        if (altKeyName.toUpperCase().startsWith("ORACLE_")) {
                            altKeyName = altKeyName.substring("ORACLE_".length());
                        }
                        if (altKeyName.toUpperCase().startsWith("SQL_FIELD_")) {
                            altKeyName = altKeyName.substring("SQL_FIELD_".length());
                        }
                        if (lastGroupByVal.equals("")) {
                            lastGroupByVal = groupByVal;
                        }
                        if (!lastGroupByVal.equals(groupByVal)) {
                            v.addElement(lastProp);
                            lastProp = new Properties();
                        }
                        if (key != null) {
                            lastProp.put(key, val);
                            if (val.indexOf(" type=\"vector\">") >= 0 || val.indexOf(" type=\"properties\">") >= 0) {
                                Object o = crushftp.handlers.Common.readXMLObject(new ByteArrayInputStream(val.getBytes("UTF8")));
                                lastProp.put(key, o == null ? new Vector() : o);
                            }
                            lastProp.put(altKeyName, groupByVal);
                        }
                        lastGroupByVal = groupByVal;
                    }
                    this.msg("Got:" + key + "=" + val);
                    continue;
                }
                Properties saver = new Properties();
                try {
                    int columnCount = rs.getMetaData().getColumnCount();
                    int x = 1;
                    while (x <= columnCount) {
                        String val;
                        String key = rs.getMetaData().getColumnName(x);
                        if (key.toUpperCase().startsWith("ORACLE_")) {
                            key = key.substring("ORACLE_".length());
                        }
                        if (key.toUpperCase().startsWith("SQL_FIELD_")) {
                            key = key.substring("SQL_FIELD_".length());
                        }
                        if ((val = rs.getString(x)) == null) {
                            val = "";
                        }
                        saver.put(key.toLowerCase(), val);
                        ++x;
                    }
                }
                catch (Throwable ee) {
                    this.msg(ee);
                }
                v.addElement(saver);
                this.msg("Got:" + saver);
            }
            rs.close();
            this.msg("Finished with table load.");
            this.msg(table);
            if (groupby != null && !lastGroupByVal.equals("")) {
                v.addElement(lastProp);
            }
            if (!propertyMode || propertyMode && groupby != null) {
                if (table.equalsIgnoreCase("WEB_BUTTONS") || table.equalsIgnoreCase("IP_RESTRICTIONS") || table.equalsIgnoreCase("DOMAIN_ROOT_LIST") || table.equalsIgnoreCase("EVENTS") || table.equalsIgnoreCase("WEB_CUSTOMIZATIONS")) {
                    table = table.toLowerCase();
                }
                if (v.size() > 0) {
                    user.put(table, v);
                }
            }
        }
        catch (Throwable e) {
            if (table.equalsIgnoreCase("events")) {
                loadEvents4 = false;
            }
            this.msg(e);
        }
    }

    private String get(String key) {
        return this.settings.getProperty(key, "");
    }

    private void msg(String s) {
        if (this.settings.getProperty("db_debug").equals("true")) {
            Log.log("USER_OBJ", 0, "SQL:" + s);
        }
    }

    private void msg(Throwable e) {
        if (this.settings.getProperty("db_debug").equals("true")) {
            Log.log("USER_OBJ", 0, e);
        }
    }

    private void releaseConnection(Connection conn) {
        try {
            if (conn != null) {
                usedConnections.remove(conn);
            }
            if (conn != null && !conn.isClosed()) {
                this.msg("Add sql connection to free connections. Free connection size was: " + freeConnections.size() + " Used Connection size is: " + usedConnections.size() + ":" + conn);
                freeConnections.addElement(conn);
            } else if (conn != null) {
                throw new SQLException("Connection was closed or null.");
            }
        }
        catch (SQLException e) {
            this.msg(e);
            this.msg("Connection was closed/dead, so discarding it. Free connection size was: " + freeConnections.size() + " Used Connection size is: " + usedConnections.size() + ":" + e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Connection getConnection() throws Throwable {
        try {
            String[] db_drv_files = this.get("db_driver_file").split(";");
            URL[] urls = new URL[db_drv_files.length];
            int x = 0;
            while (x < db_drv_files.length) {
                urls[x] = new File_S(db_drv_files[x]).toURI().toURL();
                ++x;
            }
            if (cl == null || !this.lastDriver.equals(this.get("db_driver_file"))) {
                cl = !System.getProperty("crushftp.security.classloader", "false").equals("true") ? Thread.currentThread().getContextClassLoader() : new URLClassLoader(urls);
                drvCls = Class.forName(this.get("db_driver"), true, cl);
                driver = (Driver)drvCls.newInstance();
            }
            this.lastDriver = this.get("db_driver_file");
            Object object = used_lock;
            synchronized (object) {
                while (usedConnections.size() > Integer.parseInt(System.getProperty("crushftp.user.sql.maxpool", "1000"))) {
                    Thread.sleep(100L);
                }
                Connection conn = null;
                Vector vector = freeConnections;
                synchronized (vector) {
                    if (freeConnections.size() > 0) {
                        conn = (Connection)freeConnections.remove(0);
                        usedConnections.addElement(conn);
                        this.msg("Reuse sql connection. Free connection size : " + freeConnections.size() + " Used Connection size : " + usedConnections.size() + ":" + conn);
                    }
                }
                int fail_count = 0;
                if (conn != null) {
                    block40: {
                        PreparedStatement ps;
                        block38: {
                            ps = null;
                            try {
                                try {
                                    ps = conn.prepareStatement("SELECT 1");
                                    ps.executeQuery().close();
                                }
                                catch (Exception e) {
                                    ++fail_count;
                                    try {
                                        ps.close();
                                    }
                                    catch (Exception exception) {}
                                    break block38;
                                }
                            }
                            catch (Throwable throwable) {
                                try {
                                    ps.close();
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                                throw throwable;
                            }
                            try {
                                ps.close();
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                        try {
                            try {
                                ps = conn.prepareStatement("SELECT 1 FROM DUAL");
                                ps.executeQuery().close();
                            }
                            catch (Exception e) {
                                ++fail_count;
                                try {
                                    ps.close();
                                }
                                catch (Exception exception) {}
                                break block40;
                            }
                        }
                        catch (Throwable throwable) {
                            try {
                                ps.close();
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            throw throwable;
                        }
                        try {
                            ps.close();
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    if (fail_count == 2) {
                        try {
                            conn.close();
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        this.releaseConnection(conn);
                        conn = null;
                    }
                }
                if (conn != null) {
                    return conn;
                }
                Properties props = new Properties();
                props.setProperty("user", this.get("db_user"));
                props.setProperty("password", ServerStatus.thisObj.common_code.decode_pass(crushftp.handlers.Common.url_decode(this.get("db_pass"))));
                conn = driver.connect(this.get("db_url"), props);
                usedConnections.addElement(conn);
                this.msg("Create new sql connection. Free connection size : " + freeConnections.size() + " Used Connection size : " + usedConnections.size() + ":" + conn);
                return conn;
            }
        }
        catch (Throwable e) {
            Log.log("SERVER", 0, "SQL users connection problem: " + e);
            Log.log("SERVER", 0, e);
            throw e;
        }
    }

    private void cacheUserItem(String cacheId, Object obj) {
        this.userCache.put(cacheId, Common.CLONE(obj));
        this.cacheRevolver.addElement(cacheId);
        while (this.cacheRevolver.size() > 500) {
            String removeId = this.cacheRevolver.remove(0).toString();
            this.userCache.remove(removeId);
        }
    }

    private void cacheInheritanceItem(String cacheId, Object obj) {
        this.inheritanceCache.put(cacheId, Common.CLONE(obj));
    }

    private void resetCache(String serverGroup, String prop_name) {
        block15: {
            this.userCache.clear();
            this.msg("RESETTING SQL CACHE:" + serverGroup + ":" + prop_name);
            if (Log.log("USER_OBJ", 2, "")) {
                this.msg(new Exception("RESETTING SQL CACHE:" + serverGroup + ":" + prop_name));
            }
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                try {
                    this.msg("Connecting to db.");
                    conn = this.getConnection();
                    ps = conn.prepareStatement(this.settings.getProperty("db_modified_delete"));
                    try {
                        this.msg("Connected.");
                        ps.setString(1, serverGroup);
                        ps.setString(2, prop_name);
                        ps.executeUpdate();
                    }
                    finally {
                        ps.close();
                    }
                    ps = conn.prepareStatement(this.settings.getProperty("db_modified_insert"));
                    try {
                        ps.setString(1, serverGroup);
                        ps.setString(2, prop_name);
                        ps.setString(3, new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US).format(new Date()));
                        ps.executeUpdate();
                    }
                    finally {
                        ps.close();
                    }
                }
                catch (Throwable e) {
                    this.msg(e);
                    try {
                        conn.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    this.releaseConnection(conn);
                    break block15;
                }
            }
            catch (Throwable throwable) {
                this.releaseConnection(conn);
                throw throwable;
            }
            this.releaseConnection(conn);
        }
    }

    private String getModified(String serverGroup, String prop_name) {
        String val;
        block11: {
            val = null;
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                try {
                    this.msg("Connecting to db.");
                    conn = this.getConnection();
                    ps = conn.prepareStatement(this.settings.getProperty("db_modified_query"));
                    try {
                        this.msg("Connected.");
                        ps.setString(1, serverGroup);
                        ps.setString(2, prop_name);
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            val = rs.getString("PROP_VALUE");
                        }
                        rs.close();
                    }
                    finally {
                        ps.close();
                    }
                }
                catch (Throwable e) {
                    this.msg(e);
                    try {
                        conn.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    this.releaseConnection(conn);
                    break block11;
                }
            }
            catch (Throwable throwable) {
                this.releaseConnection(conn);
                throw throwable;
            }
            this.releaseConnection(conn);
        }
        return val;
    }
}

