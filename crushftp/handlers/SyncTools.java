/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.Base64;
import com.crushftp.client.File_S;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.server.ServerStatus;
import crushftp.server.VFS;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class SyncTools
extends Thread {
    public static String minSyncVersion = "03.12.01";
    public static SyncTools dbt = null;
    static URLClassLoader cl = null;
    static Class drvCls = null;
    static Driver driver = null;
    Properties settings = new Properties();
    public boolean derby = false;
    protected static boolean started = false;
    static long lastU = 0L;
    public static Properties userSyncAgents = new Properties();
    SimpleDateFormat modified_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    public static final Vector connPool = new Vector();
    public static final Properties statementCache = new Properties();
    public static Properties cachedSyncList = new Properties();
    public static final OutputStream DEV_NULL = new OutputStream(){

        @Override
        public void write(int b) {
        }
    };

    public SyncTools(Properties settings) {
        this.settings = settings;
        this.init();
    }

    public static synchronized long u() {
        while (lastU == System.currentTimeMillis()) {
            try {
                Thread.sleep(1L);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        lastU = System.currentTimeMillis();
        return lastU;
    }

    public static void setDefaults(Properties p) {
        p.put("syncs_debug", "true");
        p.put("syncs_db_driver_file", "");
        p.put("syncs_db_driver", "org.apache.derby.jdbc.EmbeddedDriver");
        p.put("syncs_db_url", "jdbc:derby:" + System.getProperty("crushftp.sync") + "syncsDB;create=true");
        p.put("syncs_db_user", "app");
        p.put("syncs_db_pass", "");
        p.put("syncs_insert_journal", "INSERT INTO FILE_JOURNAL (RID, SYNC_ID, ITEM_PATH, EVENT_TYPE, EVENT_TIME, CLIENTID, PRIOR_MD5) VALUES (?,?,?,?,?,?,?)");
        p.put("syncs_delete_journal", "DELETE from FILE_JOURNAL where RID = ?");
        p.put("syncs_delete_journal_expired", "DELETE from FILE_JOURNAL where EVENT_TIME < ?");
        p.put("syncs_get_journal", "select * from FILE_JOURNAL where SYNC_ID = ? and RID > ? and (CLIENTID <> ? or CLIENTID is null) order by RID");
        p.put("syncs_get_prior_md5s", "select PRIOR_MD5 from FILE_JOURNAL where SYNC_ID = ? and ITEM_PATH = ? order by RID");
    }

    public synchronized void init() {
        if (started) {
            return;
        }
        System.getProperties().put("derby.stream.error.field", "crushftp.handlers.SyncTools.DEV_NULL");
        boolean bl = this.derby = this.settings.getProperty("syncs_db_driver").toUpperCase().indexOf("DERBY") >= 0;
        if (this.settings.getProperty("syncs_db_url").equals("jdbc:derby:syncsDB;create=true") && Common.machine_is_x()) {
            this.settings.put("syncs_db_url", "jdbc:derby:" + System.getProperty("crushftp.sync") + "syncsDB;create=true");
        }
        if (this.settings.getProperty("syncs_get_journal").indexOf("FILE_ITEMS") >= 0) {
            Properties p = new Properties();
            SyncTools.setDefaults(p);
            this.settings.put("syncs_get_journal", p.getProperty("syncs_get_journal"));
        }
        if (this.derby) {
            String script = "";
            script = String.valueOf(script) + "CREATE TABLE FILE_JOURNAL(RID DOUBLE NOT NULL PRIMARY KEY,SYNC_ID VARCHAR(255) NOT NULL,ITEM_PATH VARCHAR(2000) NOT NULL, EVENT_TYPE VARCHAR(20) NOT NULL,EVENT_TIME TIMESTAMP NOT NULL,CLIENTID VARCHAR(20) NOT NULL, PRIOR_MD5 VARCHAR(50) NOT NULL)\n";
            if (this.derby && !new File_S(String.valueOf(System.getProperty("crushftp.sync", "./")) + "syncsDB/").exists()) {
                started = true;
                BufferedReader br = new BufferedReader(new StringReader(script));
                String data = "";
                try {
                    while ((data = br.readLine()) != null) {
                        this.executeSql(data, new Object[0]);
                    }
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
        }
        Runtime.getRuntime().addShutdownHook(this);
        dbt = this;
        started = true;
    }

    @Override
    public void run() {
        try {
            this.stopDB();
        }
        catch (Exception e) {
            this.msg(e);
        }
    }

    public void stopDB() throws Exception {
        if (!started) {
            return;
        }
        if (this.derby) {
            try {
                DriverManager.getConnection("jdbc:derby:;shutdown=true").close();
            }
            catch (Throwable e) {
                Log.log("SYNC", 3, e);
            }
        }
        started = false;
    }

    public void executeSql(String sql, Object[] values) {
        this.msg("Connecting to db, executing sql:" + sql);
        Connection conn = null;
        boolean loop = true;
        PreparedStatement ps = null;
        while (loop) {
            loop = false;
            try {
                conn = this.getConnection();
                ps = statementCache.containsKey(sql) ? (PreparedStatement)statementCache.remove(sql) : conn.prepareStatement(sql);
                int x = 0;
                while (x < values.length) {
                    if (values[x] instanceof String) {
                        ps.setString(x + 1, (String)values[x]);
                    } else if (values[x] instanceof Date) {
                        ps.setTimestamp(x + 1, new Timestamp(((Date)values[x]).getTime()));
                    } else if (values[x] instanceof Long) {
                        ps.setLong(x + 1, (Long)values[x]);
                    } else if (values[x] == null) {
                        ps.setString(x + 1, null);
                    }
                    ++x;
                }
                ps.executeUpdate();
                if (statementCache.containsKey(sql)) {
                    ps.close();
                    continue;
                }
                statementCache.put(sql, ps);
            }
            catch (Throwable e) {
                this.msg(e);
                if (e.toString().toUpperCase().indexOf("CONNECTION") >= 0 && e.toString().toUpperCase().indexOf("READ-ONLY") < 0) {
                    loop = true;
                }
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
            }
        }
    }

    public Vector executeSqlQuery(String sql, Object[] values, boolean includeColumns) {
        return this.executeSqlQuery(sql, values, (Boolean)includeColumns);
    }

    public Vector executeSqlQuery(String sql, Object[] values, Boolean includeColumnsB) {
        this.msg("Connecting to db, executing sql:" + sql);
        Vector<Cloneable> results = new Vector<Cloneable>();
        Connection conn = null;
        Statement ps = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            this.msg("Connecting to db, got conenction:" + conn);
            ps = statementCache.containsKey(sql) ? (PreparedStatement)statementCache.remove(sql) : conn.prepareStatement(sql);
            this.msg("Adding values to prepared statement:" + values.length);
            int x = 0;
            while (x < values.length) {
                if (values[x] instanceof String) {
                    ps.setString(x + 1, (String)values[x]);
                } else if (values[x] instanceof Long) {
                    ps.setLong(x + 1, (Long)values[x]);
                } else if (values[x] instanceof Date) {
                    ps.setTimestamp(x + 1, new Timestamp(((Date)values[x]).getTime()));
                }
                ++x;
            }
            this.msg("Executing sync query...");
            rs = ps.executeQuery();
            Vector<String> cols = new Vector<String>();
            this.msg("Looping through sync query results...");
            while (rs.next()) {
                Properties p = new Properties();
                int x2 = 0;
                while (x2 < ps.getMetaData().getColumnCount()) {
                    String key = ps.getMetaData().getColumnLabel(x2 + 1);
                    if (ps.getMetaData().getColumnTypeName(x2 + 1).equalsIgnoreCase("TIMESTAMP")) {
                        try {
                            p.put(key, this.modified_sdf.format(new Date(rs.getTimestamp(x2 + 1).getTime())));
                        }
                        catch (Exception e) {
                            Common.debug(1, e);
                        }
                    } else if (ps.getMetaData().getColumnTypeName(x2 + 1).equalsIgnoreCase("DOUBLE")) {
                        try {
                            p.put(key, String.valueOf(rs.getLong(x2 + 1)));
                        }
                        catch (Exception e) {
                            this.msg(e);
                        }
                    } else {
                        try {
                            String val = rs.getString(x2 + 1);
                            if (val != null) {
                                p.put(key, val);
                            }
                        }
                        catch (Exception e) {
                            this.msg(e);
                        }
                    }
                    if (results.size() == 0) {
                        cols.addElement(key);
                    }
                    ++x2;
                }
                if (results.size() == 0 && includeColumnsB.booleanValue()) {
                    results.addElement(cols);
                }
                results.addElement(p);
            }
            this.msg("Done looping through sync query results..." + results.size());
        }
        catch (Throwable e) {
            this.msg(e);
        }
        if (rs != null) {
            try {
                rs.close();
            }
            catch (Exception e) {
                Common.debug(1, e);
            }
        }
        if (statementCache.containsKey(sql)) {
            try {
                ps.close();
            }
            catch (Exception e) {
                Common.debug(1, e);
            }
        } else if (ps != null) {
            statementCache.put(sql, ps);
        }
        return results;
    }

    public String get(String key) {
        return this.settings.getProperty(key, "");
    }

    public void msg(String s) {
        if (this.settings.getProperty("syncs_debug").equals("true")) {
            Common.debug(0, "SQL:" + s);
        }
    }

    public void msg(Throwable e) {
        Common.debug(0, e);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Connection getConnection() throws Exception {
        if (!started) {
            this.init();
        }
        Vector vector = connPool;
        synchronized (vector) {
            Connection c;
            if (connPool.size() >= 5 && !(c = (Connection)connPool.remove(0)).isClosed()) {
                connPool.addElement(c);
                return c;
            }
        }
        Connection conn = null;
        String syncs_db_url = this.get("syncs_db_url");
        try {
            String pass = this.get("syncs_db_pass");
            pass = ServerStatus.thisObj.common_code.decode_pass(pass);
            if (!this.get("syncs_db_driver_file").equals("") && System.getProperty("crushftp.security.classloader", "false").equals("true")) {
                String[] db_drv_files = this.get("syncs_db_driver_file").split(";");
                final URL[] urls = new URL[db_drv_files.length];
                int x22 = 0;
                while (x22 < db_drv_files.length) {
                    urls[x22] = new File_S(db_drv_files[x22]).toURI().toURL();
                    ++x22;
                }
                Properties x22 = this.settings;
                synchronized (x22) {
                    if (cl == null) {
                        AccessController.doPrivileged(new PrivilegedAction(){

                            public Object run() {
                                try {
                                    cl = new URLClassLoader(urls);
                                    drvCls = Class.forName(SyncTools.this.get("syncs_db_driver"), true, cl);
                                    driver = (Driver)drvCls.newInstance();
                                }
                                catch (Exception e) {
                                    SyncTools.this.msg(e);
                                }
                                return null;
                            }
                        });
                    }
                }
                Properties props = new Properties();
                props.setProperty("user", this.get("syncs_db_user"));
                props.setProperty("password", pass);
                conn = driver.connect(syncs_db_url, props);
            } else {
                Class.forName(this.get("syncs_db_driver"));
                conn = DriverManager.getConnection(syncs_db_url, this.get("syncs_db_user"), pass);
            }
            conn.setAutoCommit(true);
        }
        catch (Exception e) {
            this.msg(e);
        }
        connPool.addElement(conn);
        return conn;
    }

    public static Vector getSyncTableData(String syncIDTemp, long rid, String table, String clientid, String root_dir, VFS uVFS) {
        return SyncTools.getSyncTableData(syncIDTemp, rid, table, clientid, root_dir, uVFS, null);
    }

    /*
     * Unable to fully structure code
     */
    public static Vector getSyncTableData(String syncIDTemp, long rid, String table, String clientid, final String root_dir, final VFS uVFS, String prior_md5s_item_path) {
        block34: {
            block33: {
                url2 = "";
                try {
                    url2 = Common.url_decode(uVFS.get_item(root_dir).getProperty("url"));
                }
                catch (Exception e) {
                    Log.log("SERVER", 0, e);
                }
                url = url2;
                if (table.equalsIgnoreCase("journal") && prior_md5s_item_path != null && !prior_md5s_item_path.trim().equals("")) {
                    Log.log("SERVER", 2, "PRIOR_MD5s:" + prior_md5s_item_path);
                    list = SyncTools.dbt.executeSqlQuery(SyncTools.dbt.get("syncs_get_prior_md5s"), new Object[]{syncIDTemp, prior_md5s_item_path}, false);
                    while (list.size() > 100) {
                        list.remove(0);
                    }
                    return list;
                }
                if (table.equalsIgnoreCase("journal")) {
                    list = SyncTools.dbt.executeSqlQuery(SyncTools.dbt.get("syncs_get_journal"), new Object[]{syncIDTemp, rid, clientid}, false);
                    x = 0;
                    while (x < list.size()) {
                        p = (Properties)list.elementAt(x);
                        Log.log("SERVER", 2, "URL:" + url);
                        Log.log("SERVER", 2, "path:" + p.getProperty("ITEM_PATH").substring(1));
                        Log.log("SERVER", 2, "new path:" + new VRL(String.valueOf(url) + p.getProperty("ITEM_PATH").substring(1)).getPath());
                        dir_item = null;
                        try {
                            item_path = p.getProperty("ITEM_PATH");
                            if (item_path.startsWith("//")) {
                                item_path = item_path.substring(1);
                            }
                            p.put("ITEM_PATH", item_path);
                            dir_item = uVFS.get_item(String.valueOf(uVFS.thisSession.uiSG("root_dir").substring(1)) + item_path);
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 0, e);
                        }
                        if (dir_item == null) {
                            dir_item = new Properties();
                            dir_item.put("type", p.getProperty("ITEM_PATH").endsWith("/") != false ? "DIR" : "FILE");
                        }
                        p.put("ITEM_MODIFIED", dir_item.getProperty("modified", "0"));
                        p.put("ITEM_SIZE", dir_item.getProperty("size", "0"));
                        p.put("ITEM_TYPE", dir_item.getProperty("type", "DIR").equalsIgnoreCase("DIR") != false ? "D" : "F");
                        ++x;
                    }
                    return list;
                }
                if (!table.equalsIgnoreCase("file")) break block33;
                list2 = null;
                startThread = false;
                if (SyncTools.cachedSyncList.containsKey(url)) {
                    list2 = (Vector)SyncTools.cachedSyncList.get(url);
                } else {
                    list2 = new Vector();
                    SyncTools.cachedSyncList.put(url, list2);
                    startThread = true;
                }
                list = list2;
                try {
                    original_url = url;
                    if (startThread) {
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                try {
                                    uVFS.getListing(list, root_dir, 99, 50000, true);
                                    list.addElement("DONE");
                                }
                                catch (Exception e) {
                                    Log.log("SYNC", 1, e);
                                    list.addElement("ERROR:" + e.toString());
                                }
                            }
                        }, "Sync listing:" + url);
                    }
                    list3 = new Vector<Properties>();
                    do {
                        loops = 0;
                        while (list.size() == 0 && loops++ < 600) {
                            Thread.sleep(100L);
                        }
                        if (loops >= 29) {
                            SyncTools.cachedSyncList.remove(url);
                            throw new Exception("Timeout waiting for list data...");
                        }
                        if (list.size() == 1 && list.elementAt(0) instanceof String) {
                            msg = list.elementAt(0).toString();
                            if (list3.size() != 0) break;
                            SyncTools.cachedSyncList.remove(url);
                            if (msg.equals("DONE")) break;
                            throw new Exception(msg);
                        }
                        dir_item = (Properties)list.remove(0);
                        p = new Properties();
                        item_path = Common.url_decode(dir_item.getProperty("url")).substring(original_url.length() - 1);
                        if (!item_path.startsWith("/")) {
                            item_path = "/" + item_path;
                        }
                        if (item_path.startsWith("//")) {
                            item_path = item_path.substring(1);
                        }
                        p.put("ITEM_PATH", item_path);
                        p.put("ITEM_MODIFIED", dir_item.getProperty("modified"));
                        p.put("ITEM_SIZE", dir_item.getProperty("size"));
                        p.put("ITEM_TYPE", dir_item.getProperty("type").equalsIgnoreCase("DIR") != false ? "D" : "F");
                        list3.addElement(p);
                    } while (list3.size() <= 2000);
                    return list3;
                }
                catch (Exception e) {
                    Log.log("SYNC", 0, e);
                }
                break block34;
            }
            if (!table.startsWith("file_")) break block34;
            pos = Integer.parseInt(table.substring("file_".length()));
            sync_info = null;
            startThread = false;
            if (SyncTools.cachedSyncList.containsKey(String.valueOf(clientid) + url + uVFS.username)) {
                sync_info = (Properties)SyncTools.cachedSyncList.get(String.valueOf(clientid) + url + uVFS.username);
            } else {
                sync_info = new Properties();
                sync_info.put("temp_list", new Vector<E>());
                sync_info.put("current_list", new Vector<E>());
                SyncTools.cachedSyncList.put(String.valueOf(clientid) + url + uVFS.username, sync_info);
                startThread = true;
            }
            last_pos = Integer.parseInt(sync_info.getProperty("last_pos", "0"));
            current_list = (Vector)sync_info.get("current_list");
            sync_info.put("last_pos", String.valueOf(pos));
            x = last_pos;
            while (x < pos) {
                current_list.setElementAt("", x);
                ++x;
            }
            list = (Vector)sync_info.get("temp_list");
            try {
                original_url = url;
                if (startThread) {
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                uVFS.getListing(list, root_dir, 99, 10000, true);
                                list.addElement("DONE");
                            }
                            catch (Exception e) {
                                Log.log("SYNC", 1, e);
                                list.addElement("ERROR:" + e.toString());
                            }
                        }
                    }, "Sync listing:" + url);
                }
                list3 = new Vector<Properties>();
                while (true) {
                    loops = 0;
                    while (list.size() == 0 && loops++ < 1200) {
                        Thread.sleep(100L);
                    }
                    if (loops >= 1190 && list.size() == 0) {
                        SyncTools.cachedSyncList.remove(url);
                        throw new Exception("Timeout waiting for list data...");
                    }
                    if (list.size() != 1 || !(list.elementAt(0) instanceof String)) ** GOTO lbl159
                    msg = list.elementAt(0).toString();
                    if (list3.size() != 0) break;
                    SyncTools.cachedSyncList.remove(String.valueOf(clientid) + url + uVFS.username);
                    if (msg.equals("DONE")) break;
                    throw new Exception(msg);
lbl-1000:
                    // 1 sources

                    {
                        dir_item = (Properties)list.remove(0);
                        current_list.addElement(dir_item);
lbl159:
                        // 2 sources

                        ** while (pos >= current_list.size() && list.size() > 0)
                    }
lbl160:
                    // 1 sources

                    if (pos >= current_list.size()) continue;
                    if (!(item_path = Common.url_decode((dir_item = (Properties)current_list.elementAt(pos++)).getProperty("url")).substring(original_url.length() - 1)).startsWith("/")) {
                        item_path = "/" + item_path;
                    }
                    if (item_path.startsWith("//")) {
                        item_path = item_path.substring(1);
                    }
                    p = new Properties();
                    p.put("ITEM_PATH", item_path);
                    p.put("ITEM_MODIFIED", dir_item.getProperty("modified"));
                    p.put("ITEM_SIZE", dir_item.getProperty("size"));
                    p.put("ITEM_TYPE", dir_item.getProperty("type").equalsIgnoreCase("DIR") != false ? "D" : "F");
                    list3.addElement(p);
                    if (list3.size() >= 1000) break;
                }
                return list3;
            }
            catch (Exception e) {
                Log.log("SYNC", 0, e);
                SyncTools.cachedSyncList.remove(String.valueOf(clientid) + url + uVFS.username);
            }
        }
        return null;
    }

    public static void addJournalEntry(String syncIDTemp, String path, String change, String clientid, String prior_md5) throws Exception {
        Log.log("SYNC", 2, "Event Type:" + change + ":" + path);
        dbt.executeSql(dbt.get("syncs_insert_journal"), new Object[]{SyncTools.u(), syncIDTemp, path, change, new Date(), clientid, String.valueOf(prior_md5)});
    }

    public static void deleteJournalItem(String rid) {
        dbt.executeSql(dbt.get("syncs_delete_journal"), new Object[]{Long.parseLong(rid)});
    }

    public static void purgeExpired(long time) {
        dbt.executeSql(dbt.get("syncs_delete_journal_expired"), new Object[]{new Date(time)});
    }

    public static Properties getAllAgents() {
        Properties all = new Properties();
        Enumeration<Object> keys = userSyncAgents.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            all.putAll((Map<?, ?>)((Properties)userSyncAgents.get(key)));
        }
        return all;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Object getSyncPrefs(Properties request) throws Exception {
        Properties properties = userSyncAgents;
        synchronized (properties) {
            Properties agent;
            Properties agents = (Properties)userSyncAgents.get(request.getProperty("user_name").toUpperCase());
            if (agents == null) {
                agents = new Properties();
                userSyncAgents.put(request.getProperty("user_name").toUpperCase(), agents);
            }
            if (!agents.containsKey(request.getProperty("clientid"))) {
                agent = new Properties();
                agent.put("queue", new Vector());
                agents.put(request.getProperty("clientid"), agent);
            }
            if (request.getProperty("site").indexOf("(CONNECT)") >= 0) {
                agents = SyncTools.getAllAgents();
            }
            agent = (Properties)agents.get(request.getProperty("clientid"));
            agent.putAll((Map<?, ?>)request);
            agent.remove("command");
            agent.put("ping", String.valueOf(System.currentTimeMillis()));
            agent.put("ip", request.getProperty("user_ip"));
            Vector v = new Vector();
            v.addAll((Vector)agent.get("queue"));
            ((Vector)agent.get("queue")).removeAllElements();
            return v;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void sendSyncResult(Properties request) throws Exception {
        Properties properties = userSyncAgents;
        synchronized (properties) {
            Properties agents = (Properties)userSyncAgents.get(request.getProperty("user_name").toUpperCase());
            if (agents == null) {
                agents = new Properties();
                userSyncAgents.put(request.getProperty("user_name").toUpperCase(), agents);
            }
            if (request.getProperty("site").indexOf("(CONNECT)") >= 0) {
                agents = SyncTools.getAllAgents();
            }
            Properties agent = (Properties)agents.get(request.getProperty("clientid"));
            agent.put("ping", String.valueOf(System.currentTimeMillis()));
            Object o = Common.readXMLObject(new ByteArrayInputStream(Base64.decode(request.getProperty("result"))));
            agent.put(request.getProperty("resultid", "0"), o);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Object getSyncAgents(Properties request) throws Exception {
        Vector<Properties> v = new Vector<Properties>();
        Properties properties = userSyncAgents;
        synchronized (properties) {
            Properties agents = (Properties)userSyncAgents.get(request.getProperty("user_name").toUpperCase());
            if (request.getProperty("site").indexOf("(CONNECT)") >= 0) {
                agents = SyncTools.getAllAgents();
            }
            if (agents != null) {
                Enumeration<Object> keys = agents.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement().toString();
                    Properties agent = (Properties)agents.get(key);
                    if (System.currentTimeMillis() - Long.parseLong(agent.getProperty("ping")) < 45000L) {
                        v.addElement(agent);
                    }
                    if (System.currentTimeMillis() - Long.parseLong(agent.getProperty("ping")) <= 30000L) continue;
                    Vector queue = (Vector)agent.get("queue");
                    Properties command = new Properties();
                    command.put("COMMAND", "NOOP");
                    command.put("RESULTID", Common.makeBoundary());
                    queue.addElement(command);
                }
            }
        }
        return v;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Object sendSyncCommand(Properties request) throws Exception {
        Object o = new Properties();
        Properties agent = null;
        Vector queue = null;
        Properties command = new Properties();
        Properties properties = userSyncAgents;
        synchronized (properties) {
            Properties agents = (Properties)userSyncAgents.get(request.getProperty("user_name").toUpperCase());
            if (request.getProperty("site").indexOf("(CONNECT)") >= 0) {
                agents = SyncTools.getAllAgents();
            }
            agent = (Properties)agents.get(request.getProperty("agentid"));
            queue = (Vector)agent.get("queue");
            Enumeration<Object> keys = request.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                if (!key.startsWith("sync_")) continue;
                Object val = request.getProperty(key);
                if (key.endsWith("_obj")) {
                    val = Common.readXMLObject(new ByteArrayInputStream(request.getProperty(key).getBytes("UTF8")));
                }
                command.put(key.substring("sync_".length()).toUpperCase(), val);
            }
            command.put("RESULTID", Common.makeBoundary());
            queue.addElement(command);
        }
        int loops = 0;
        while (queue.contains(command) && loops++ < 100) {
            Thread.sleep(100L);
        }
        loops = 0;
        while (!agent.containsKey(command.getProperty("RESULTID")) && loops++ < 100) {
            Thread.sleep(100L);
        }
        if (agent.containsKey(command.getProperty("RESULTID"))) {
            o = agent.remove(command.getProperty("RESULTID"));
        }
        return o;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Object getSyncXMLList(Properties request) throws Exception {
        Properties agent = null;
        Vector queue = null;
        Properties command = new Properties();
        Properties properties = userSyncAgents;
        synchronized (properties) {
            Properties agents = (Properties)userSyncAgents.get(request.getProperty("user_name").toUpperCase());
            if (request.getProperty("site").indexOf("(CONNECT)") >= 0) {
                agents = SyncTools.getAllAgents();
            }
            agent = (Properties)agents.get(request.getProperty("get_from_agentid"));
            queue = (Vector)agent.get("queue");
            command.put("COMMAND", "list_folder");
            command.put("PATH", request.getProperty("path"));
            command.put("PASSWORD", request.getProperty("admin_password"));
            command.put("RESULTID", Common.makeBoundary());
            queue.addElement(command);
        }
        int loops = 0;
        while (queue.contains(command) && loops++ < 100) {
            Thread.sleep(100L);
        }
        loops = 0;
        while (!agent.containsKey(command.getProperty("RESULTID")) && loops++ < 100) {
            Thread.sleep(100L);
        }
        Vector items = new Vector();
        if (agent.containsKey(command.getProperty("RESULTID"))) {
            items = (Vector)agent.remove(command.getProperty("RESULTID"));
        }
        return items;
    }
}

