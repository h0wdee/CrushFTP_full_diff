/*
 * Decompiled with CFR 0.152.
 */
package crushftp.db;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import com.crushftp.tunnel2.DVector;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.server.ServerStatus;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class StatTools {
    static URLClassLoader cl = null;
    static Class drvCls = null;
    static Driver driver = null;
    public Properties settings = new Properties();
    public boolean mssql = false;
    public boolean mysql = false;
    public boolean derby = false;
    public static boolean started = false;
    public static int port = 3309;
    static Vector freeConnections = new Vector();
    static Vector usedConnections = new Vector();
    public static Object used_lock = new Object();
    public boolean temp_disabled = false;
    public int failed_login_stat_count = 0;
    public Object failed_login_stat_lock = new Object();
    public static final OutputStream DEV_NULL = new OutputStream(){

        @Override
        public void write(int b) {
        }
    };
    static long lastU = System.currentTimeMillis();

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
        p.put("stats_debug", "true");
        p.put("stats_db_driver_file", "");
        p.put("stats_db_driver", "org.apache.derby.jdbc.EmbeddedDriver");
        p.put("stats_db_url", "jdbc:derby:" + System.getProperty("crushftp.stats") + "statsDB;create=true");
        p.put("stats_db_user", "app");
        p.put("stats_db_pass", "");
        p.put("stats_get_max_sessions_rid", "select max(RID) from SESSIONS");
        p.put("stats_get_max_transfers_rid", "select max(RID) from TRANSFERS");
        p.put("stats_get_max_meta_info_rid", "select max(RID) from META_INFO");
        p.put("stats_get_transfers_sessions", "select * from TRANSFERS where SESSION_RID in (%sessions%)");
        p.put("stats_get_session_rid_sessions", "select RID from SESSIONS where START_TIME > ? and USER_NAME = ?");
        p.put("stats_update_transfers_ignore_size", "update TRANSFERS set IGNORE_SIZE = 'Y' where SESSION_RID in (%sessions%) and START_TIME > ?");
        p.put("stats_insert_sessions", "INSERT INTO SESSIONS (RID, SESSION, SERVER_GROUP, USER_NAME, START_TIME, END_TIME, SUCCESS_LOGIN, IP) VALUES (?,?,?,?,?,?,?,?)");
        p.put("stats_insert_transfers", "INSERT INTO TRANSFERS (RID, SESSION_RID, START_TIME, DIRECTION, PATH, FILE_NAME, URL, SPEED, TRANSFER_SIZE, IGNORE_SIZE) VALUES (?,?,?,?,?,?,?,?,?,'N')");
        p.put("stats_insert_meta_info", "INSERT INTO META_INFO (RID, SESSION_RID, TRANSFER_RID, ITEM_KEY, ITEM_VALUE) VALUES (?,?,?,?,?)");
        p.put("stats_update_sessions", "UPDATE SESSIONS set END_TIME = ? where RID = ?");
        p.put("stats_get_transfers_time", "SELECT RID FROM TRANSFERS WHERE START_TIME < ?");
        p.put("stats_get_sessions_time", "SELECT RID FROM SESSIONS WHERE START_TIME < ? and SERVER_GROUP <> 'CHANGE_PASS'");
        p.put("stats_delete_meta_transfers", "DELETE FROM META_INFO WHERE TRANSFER_RID IN (%transfers%)");
        p.put("stats_delete_transfers_time", "DELETE FROM TRANSFERS WHERE START_TIME < ?");
        p.put("stats_delete_sessions_time", "DELETE FROM SESSIONS WHERE START_TIME < ? and SERVER_GROUP <> 'CHANGE_PASS'");
        p.put("stats_get_transfers_download", "select count(*) from TRANSFERS where DIRECTION = 'DOWNLOAD' and SESSION_RID in (select RID from SESSIONS where USER_NAME = ?)");
        p.put("stats_get_meta_info", "select * from META_INFO where LOCATE(UPPER(ITEM_VALUE), UPPER(?)) > 0");
        p.put("stats_get_transfers_period", "select sum(TRANSFER_SIZE) from TRANSFERS t, SESSIONS s where DIRECTION = ? and (IGNORE_SIZE = 'N' or IGNORE_SIZE is null) and t.SESSION_RID = s.RID and USER_NAME = ? and t.START_TIME >= ?");
        p.put("stats_get_transfers_count_period", "select count(TRANSFER_SIZE) from TRANSFERS t, SESSIONS s where DIRECTION = ? and (IGNORE_SIZE = 'N' or IGNORE_SIZE is null) and t.SESSION_RID = s.RID and USER_NAME = ? and t.START_TIME >= ?");
    }

    public synchronized void init() {
        System.getProperties().put("derby.stream.error.field", "crushftp.db.StatTools.DEV_NULL");
        this.settings = ServerStatus.server_settings;
        this.mssql = this.settings.getProperty("stats_db_url").toUpperCase().indexOf("SQLSERVER") >= 0;
        this.mysql = this.settings.getProperty("stats_db_driver").toUpperCase().indexOf("MYSQL") >= 0 || this.settings.getProperty("stats_db_driver").toUpperCase().indexOf("MARIA") >= 0;
        boolean bl = this.derby = this.settings.getProperty("stats_db_driver").toUpperCase().indexOf("DERBY") >= 0;
        if (ServerStatus.BG("disable_stats") || Common.dmz_mode) {
            Log.log("STATISTICS", 0, "Statistics database is disabled.");
            return;
        }
        String script2 = "CREATE TABLE META_INFO(RID DOUBLE NOT NULL PRIMARY KEY,SESSION_RID DOUBLE NOT NULL,TRANSFER_RID DOUBLE NOT NULL,ITEM_KEY VARCHAR(100) DEFAULT NULL,ITEM_VALUE VARCHAR(2000) DEFAULT NULL)\n";
        script2 = String.valueOf(script2) + "CREATE TABLE SESSIONS(RID DOUBLE NOT NULL PRIMARY KEY,SESSION VARCHAR(200) DEFAULT NULL,SERVER_GROUP VARCHAR(50) DEFAULT NULL,USER_NAME VARCHAR(100) DEFAULT NULL,START_TIME TIMESTAMP DEFAULT NULL,END_TIME TIMESTAMP DEFAULT NULL,SUCCESS_LOGIN VARCHAR(10) DEFAULT NULL,IP VARCHAR(50) DEFAULT NULL)\n";
        script2 = String.valueOf(script2) + "CREATE TABLE TRANSFERS(RID DOUBLE NOT NULL PRIMARY KEY,SESSION_RID DOUBLE NOT NULL,START_TIME TIMESTAMP DEFAULT NULL,DIRECTION VARCHAR(8) DEFAULT NULL,PATH VARCHAR(255) DEFAULT NULL,FILE_NAME VARCHAR(2000) DEFAULT NULL,URL VARCHAR(2000) DEFAULT NULL,SPEED INTEGER DEFAULT NULL,TRANSFER_SIZE DOUBLE DEFAULT NULL,IGNORE_SIZE VARCHAR(1) DEFAULT NULL)\n";
        if (!started) {
            new File_S(String.valueOf(System.getProperty("crushftp.stats")) + "stats/").renameTo(new File_S(String.valueOf(System.getProperty("crushftp.stats")) + "stats_crush5/"));
        }
        started = true;
        if (this.derby && !new File_S(String.valueOf(System.getProperty("crushftp.stats")) + "statsDB/").exists()) {
            try {
                Log.log("STATISTICS", 0, "Creating statsDB...");
                try {
                    this.createDerbyDB(script2);
                }
                catch (Exception e) {
                    Log.log("STATISTICS", 0, e);
                }
                crushftp.handlers.Common.recurseDelete(String.valueOf(System.getProperty("crushftp.stats")) + "stats/", false);
                this.settings.put("stats_db_driver", "org.apache.derby.jdbc.EmbeddedDriver");
                this.settings.put("stats_db_url", "jdbc:derby:" + System.getProperty("crushftp.stats") + "statsDB;create=true");
                this.settings.put("stats_db_user", "app");
                this.settings.put("stats_db_pass", "");
                this.derby = true;
                started = true;
                ServerStatus.thisObj.save_server_settings(true);
                Log.log("STATISTICS", 0, "Creation complete.");
            }
            catch (Throwable e) {
                Log.log("STATISTICS", 0, e);
            }
            if (this.settings.getProperty("stats_get_session_rid_sessions").indexOf("%now%") >= 0) {
                this.settings.put("stats_get_session_rid_sessions", "select RID from SESSIONS where START_TIME > ? and USER_NAME = ?");
            }
            if (this.settings.getProperty("stats_update_transfers_ignore_size").indexOf("%now%") >= 0) {
                this.settings.put("stats_update_transfers_ignore_size", "update TRANSFERS set IGNORE_SIZE = 'Y' where SESSION_RID in (%sessions%) and START_TIME > ?");
            }
        }
        try {
            this.executeSqlQuery("select count(*) from SESSIONS", new Object[0], false, null);
        }
        catch (Exception e) {
            Log.log("STATISTICS", 1, e);
            try {
                this.createDerbyDB(script2);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        try {
            this.executeSqlQuery("select count(*) from TRANSFERS", new Object[0], false, null);
        }
        catch (Exception e) {
            Log.log("STATISTICS", 1, e);
            try {
                this.createDerbyDB(script2);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        if (this.settings.getProperty("stats_get_sessions_time").equalsIgnoreCase("SELECT RID FROM SESSIONS WHERE START_TIME < ?")) {
            Properties p2 = new Properties();
            StatTools.setDefaults(p2);
            this.settings.put("stats_get_sessions_time", p2.getProperty("stats_get_sessions_time"));
            this.settings.put("stats_delete_sessions_time", p2.getProperty("stats_delete_sessions_time"));
        }
    }

    public void createDerbyDB(String script2) throws Throwable {
        block16: {
            Connection conn1 = null;
            Connection conn2 = null;
            try {
                conn1 = this.getConnection();
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
                conn2 = DriverManager.getConnection("jdbc:derby:" + System.getProperty("crushftp.stats") + "statsDB;create=true", "app", "");
                conn2.setAutoCommit(true);
                if (script2 == null) break block16;
                BufferedReader br = new BufferedReader(new StringReader(script2));
                String data = "";
                try {
                    while ((data = br.readLine()) != null) {
                        PreparedStatement ps = conn2.prepareStatement(data);
                        ps.execute();
                        ps.close();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            finally {
                try {
                    if (conn1 != null) {
                        conn1.close();
                    }
                }
                catch (Exception exception) {}
                try {
                    if (conn2 != null) {
                        conn2.close();
                    }
                }
                catch (Exception exception) {}
            }
        }
    }

    public synchronized void stopDB() {
        if (!started) {
            return;
        }
        try {
            if (this.derby) {
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
            }
        }
        catch (Throwable e) {
            Log.log("STATISTICS", 3, e);
        }
        started = false;
    }

    public void findMetas(String sql, String[] values, Vector v) {
        if ((ServerStatus.BG("disable_stats") || Common.dmz_mode || this.temp_disabled) && this.derby) {
            return;
        }
        this.msg("Connecting to db, executing sql:" + sql);
        Connection conn = null;
        try {
            try {
                conn = this.getConnection();
                PreparedStatement ps_metas = conn.prepareStatement(sql);
                String sessions = ",";
                Properties metas = new Properties();
                try {
                    int x = 0;
                    while (x < values.length) {
                        ps_metas.setString(x + 1, values[x]);
                        ++x;
                    }
                    ResultSet rs = ps_metas.executeQuery();
                    while (rs.next()) {
                        Properties meta = (Properties)metas.get(rs.getString("SESSION_RID"));
                        if (meta == null) {
                            meta = new Properties();
                        }
                        meta.put(rs.getString("ITEM_KEY"), rs.getString("ITEM_VALUE"));
                        metas.put(rs.getString("SESSION_RID"), meta);
                        if (sessions.indexOf("," + rs.getString("SESSION_RID") + ",") >= 0) continue;
                        sessions = String.valueOf(sessions) + rs.getString("SESSION_RID") + ",";
                    }
                    rs.close();
                }
                finally {
                    ps_metas.close();
                }
                if (sessions.length() > 1) {
                    sessions = sessions.substring(1, sessions.length() - 1);
                }
                sql = crushftp.handlers.Common.replace_str(this.get("stats_get_transfers_sessions"), "%sessions%", sessions);
                try (PreparedStatement ps_sessions = conn.prepareStatement(sql);){
                    ResultSet rs = ps_sessions.executeQuery();
                    while (rs.next()) {
                        Properties p = new Properties();
                        p.put("url", rs.getString("URL"));
                        p.put("metaInfo", metas.get(rs.getString("SESSION_RID")));
                        v.addElement(p);
                    }
                    rs.close();
                }
            }
            catch (Throwable e) {
                try {
                    conn.close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                this.msg(e);
                this.releaseConnection(conn);
            }
        }
        finally {
            this.releaseConnection(conn);
        }
    }

    public String executeSql(String sql, Object[] values) {
        if (ServerStatus.BG("disable_stats") || Common.dmz_mode || this.temp_disabled) {
            return "";
        }
        String rid = "";
        this.msg("Connecting to db, executing sql:" + sql);
        if (this.mysql) {
            sql = crushftp.handlers.Common.replace_str(sql, "as bigint", "as unsigned");
        }
        Connection conn = null;
        try {
            try {
                conn = this.getConnection();
                PreparedStatement ps = null;
                ps = ServerStatus.BG("stat_auto_increment") && sql.startsWith("INSERT INTO") ? conn.prepareStatement(sql, 1) : conn.prepareStatement(sql);
                try {
                    ResultSet rs;
                    int x = 0;
                    while (x < values.length) {
                        if (values[x] instanceof String) {
                            ps.setString(x + 1, (String)values[x]);
                        } else if (values[x] instanceof Date) {
                            ps.setTimestamp(x + 1, new Timestamp(((Date)values[x]).getTime()));
                        } else if (values[x] == null) {
                            ps.setString(x + 1, null);
                        }
                        ++x;
                    }
                    ps.executeUpdate();
                    if (ServerStatus.BG("stat_auto_increment") && sql.startsWith("INSERT INTO") && (rs = ps.getGeneratedKeys()).next()) {
                        rid = String.valueOf(rs.getLong(1));
                    }
                }
                finally {
                    ps.close();
                }
            }
            catch (Throwable e) {
                if (("" + e).indexOf("'SESSIONS' does not exist") >= 0) {
                    crushftp.handlers.Common.recurseDelete(String.valueOf(System.getProperty("crushftp.stats")) + "statsDB/", false);
                    this.init();
                }
                try {
                    conn.close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                this.msg(e);
                this.releaseConnection(conn);
            }
        }
        finally {
            this.releaseConnection(conn);
        }
        return rid;
    }

    public DVector executeSqlQuery(String sql, Object[] values, boolean includeColumns, Properties params, int max) {
        return (DVector)this.executeSqlQuery(sql, values, new Boolean(includeColumns), params, true, max);
    }

    public DVector executeSqlQuery(String sql, Object[] values, boolean includeColumns, Properties params) {
        return (DVector)this.executeSqlQuery(sql, values, new Boolean(includeColumns), params, true, -1);
    }

    public Vector executeSqlQuery_mem(String sql, Object[] values, boolean includeColumns, Properties params) {
        return (Vector)this.executeSqlQuery(sql, values, new Boolean(includeColumns), params, false, -1);
    }

    public Object executeSqlQuery(String sql, Object[] values, Boolean includeColumnsB, Properties params, boolean disk_based, int max) {
        Vector<Cloneable> results2;
        DVector results1;
        block48: {
            if ((ServerStatus.BG("disable_stats") || Common.dmz_mode || this.temp_disabled) && this.derby) {
                return new DVector();
            }
            if (this.mysql) {
                sql = crushftp.handlers.Common.replace_str(sql, "as bigint", "as unsigned");
            }
            if (this.derby) {
                while (sql.indexOf("CASEWHEN") >= 0) {
                    int loc1 = sql.indexOf("CASEWHEN");
                    int loc2 = sql.indexOf(",", loc1);
                    int loc3 = sql.indexOf(",", loc2 + 1);
                    int loc4 = sql.indexOf(")", loc3 + 1);
                    sql = String.valueOf(sql.substring(0, loc1)) + "CASE WHEN " + sql.substring(loc1 + "CASEWHEN".length(), loc2) + ") THEN " + sql.substring(loc2 + 1, loc3) + " ELSE " + sql.substring(loc3 + 1, loc4) + " END " + sql.substring(loc4 + 1);
                }
            }
            this.msg("Connecting to db, executing sql:" + sql);
            results1 = null;
            results2 = null;
            if (disk_based) {
                results1 = new DVector();
            } else {
                results2 = new Vector<Cloneable>();
            }
            Connection conn = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            try {
                try {
                    conn = this.getConnection();
                    boolean fixLimit = false;
                    if (this.derby && sql.indexOf("limit ?") >= 0) {
                        sql = sql.replaceAll("limit \\?", "").trim();
                        fixLimit = true;
                    }
                    try (PreparedStatement ps = conn.prepareStatement(sql);){
                        int x = 0;
                        while (x < values.length) {
                            if (this.derby && fixLimit && x == values.length - 1) {
                                ps.setMaxRows((Integer)values[x]);
                                break;
                            }
                            if (values[x] instanceof String) {
                                ps.setString(x + 1, (String)values[x]);
                            } else if (values[x] instanceof Integer) {
                                ps.setInt(x + 1, (Integer)values[x]);
                            } else if (values[x] instanceof Date) {
                                ps.setTimestamp(x + 1, new Timestamp(((Date)values[x]).getTime()));
                            }
                            ++x;
                        }
                        ResultSet rs = ps.executeQuery();
                        Vector<String> cols = new Vector<String>();
                        int added = 0;
                        while (rs.next()) {
                            Properties p = new Properties();
                            int x2 = 0;
                            while (x2 < rs.getMetaData().getColumnCount()) {
                                String key = rs.getMetaData().getColumnLabel(x2 + 1);
                                if (rs.getMetaData().getColumnTypeName(x2 + 1).equalsIgnoreCase("TIMESTAMP")) {
                                    try {
                                        p.put(key, sdf.format(new Date(rs.getTimestamp(x2 + 1).getTime())));
                                    }
                                    catch (Exception exception) {}
                                } else if (rs.getMetaData().getColumnTypeName(x2 + 1).equalsIgnoreCase("DOUBLE")) {
                                    try {
                                        p.put(key, String.valueOf(rs.getLong(x2 + 1)));
                                    }
                                    catch (Exception exception) {}
                                } else {
                                    try {
                                        p.put(key, rs.getString(x2 + 1));
                                    }
                                    catch (Exception exception) {
                                        // empty catch block
                                    }
                                }
                                if (disk_based && results1.size() == 0 || !disk_based && results2.size() == 0) {
                                    cols.addElement(key);
                                }
                                ++x2;
                            }
                            if ((disk_based && results1.size() == 0 || !disk_based && results2.size() == 0) && includeColumnsB.booleanValue()) {
                                if (disk_based) {
                                    results1.addElement(cols);
                                } else {
                                    results2.addElement(cols);
                                }
                            }
                            if (disk_based) {
                                results1.addElement(p);
                            } else {
                                results2.addElement(p);
                            }
                            if (added++ == 10000) {
                                added = 0;
                                System.gc();
                                if (Common.getFreeRam() < 0x10000000L) {
                                    if (ServerStatus.BG("report_memory_protection")) {
                                        throw new Exception("Memory is too low to run report of this size.  Aborting:" + Common.format_bytes_short2(Common.getFreeRam()));
                                    }
                                }
                            }
                            if (max > 0) {
                                if (results1 != null && results1.size() >= max) break;
                                if (results2 != null && results2.size() >= max) {
                                    break;
                                }
                            }
                            if (max <= 0) continue;
                            Thread.sleep(1L);
                        }
                    }
                }
                catch (Throwable e) {
                    try {
                        conn.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    params.put("error", "" + e);
                    this.msg(e);
                    this.releaseConnection(conn);
                    break block48;
                }
            }
            catch (Throwable throwable) {
                this.releaseConnection(conn);
                throw throwable;
            }
            this.releaseConnection(conn);
        }
        if (disk_based) {
            return results1;
        }
        return results2;
    }

    public String getValue(String sql, Object[] values) {
        this.msg("Connecting to db, executing sql:" + sql);
        String val = null;
        Connection conn = null;
        try {
            try {
                conn = this.getConnection();
                try (PreparedStatement ps = conn.prepareStatement(sql);){
                    int x = 0;
                    while (x < values.length) {
                        if (values[x] instanceof String) {
                            ps.setString(x + 1, (String)values[x]);
                        } else if (values[x] instanceof Date) {
                            ps.setTimestamp(x + 1, new Timestamp(((Date)values[x]).getTime()));
                        }
                        ++x;
                    }
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        try {
                            val = String.valueOf(rs.getLong(1));
                        }
                        catch (Exception e) {
                            val = rs.getString(1);
                        }
                    }
                    rs.close();
                }
            }
            catch (Throwable e) {
                try {
                    conn.close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                this.msg(e);
                this.releaseConnection(conn);
            }
        }
        finally {
            this.releaseConnection(conn);
        }
        return val;
    }

    public void insertMetaInfo(String session_rid2, Properties metaInfo, String transfer_rid2) {
        if (ServerStatus.BG("disable_stats") || Common.dmz_mode || this.temp_disabled || metaInfo == null) {
            return;
        }
        String sql = this.get("stats_insert_meta_info");
        Enumeration<Object> keys = metaInfo.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            String val = metaInfo.getProperty(key, "");
            if (ServerStatus.BG("stat_auto_increment")) {
                this.executeSql(sql, new String[]{session_rid2, transfer_rid2, key, val});
                continue;
            }
            this.executeSql(sql, new String[]{String.valueOf(StatTools.u()), session_rid2, transfer_rid2, key, val});
        }
    }

    public void setIgnore(String user_name, String transfer_type, String duration) {
        if (ServerStatus.BG("disable_stats") || Common.dmz_mode || this.temp_disabled) {
            return;
        }
        this.msg("Connecting to db:setIgnore");
        Connection conn = null;
        try {
            try {
                conn = this.getConnection();
                PreparedStatement ps = conn.prepareStatement(this.get("stats_get_session_rid_sessions"));
                String sessions = ",";
                GregorianCalendar gc = new GregorianCalendar();
                try {
                    gc.setTimeInMillis(System.currentTimeMillis());
                    gc.add(5, Integer.parseInt(duration) * -1);
                    ps.setTimestamp(1, new Timestamp(gc.getTime().getTime()));
                    ps.setString(2, user_name);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        if (sessions.indexOf("," + rs.getString("RID") + ",") >= 0) continue;
                        sessions = String.valueOf(sessions) + rs.getString("RID") + ",";
                    }
                    rs.close();
                }
                finally {
                    ps.close();
                }
                if (sessions.length() > 1) {
                    sessions = sessions.substring(1, sessions.length() - 1);
                    PreparedStatement ps_sessions = conn.prepareStatement(crushftp.handlers.Common.replace_str(this.get("stats_update_transfers_ignore_size"), "%sessions%", sessions));
                    ps_sessions.setTimestamp(1, new Timestamp(gc.getTime().getTime()));
                    ps_sessions.executeUpdate();
                    ps_sessions.close();
                }
            }
            catch (Throwable e) {
                try {
                    conn.close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                this.msg(e);
                this.releaseConnection(conn);
            }
        }
        finally {
            this.releaseConnection(conn);
        }
    }

    public String get(String key) {
        return this.settings.getProperty(key, "");
    }

    public void msg(String s) {
        if (this.settings.getProperty("stats_debug", "true").equals("true")) {
            Log.log("STATISTICS", 2, "SQL:" + s);
        }
    }

    public void msg(Throwable e) {
        if (this.settings.getProperty("stats_debug", "true").equals("true")) {
            Log.log("STATISTICS", 1, e);
        }
    }

    private void releaseConnection(Connection conn) {
        try {
            usedConnections.remove(conn);
            if (conn != null && !conn.isClosed()) {
                freeConnections.addElement(conn);
            }
        }
        catch (SQLException sQLException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Connection getConnection() throws Throwable {
        if (!started) {
            this.init();
        }
        Connection conn = null;
        try {
            if (!this.get("stats_db_driver_file").equals("") && System.getProperty("crushftp.security.classloader", "false").equals("true")) {
                String[] db_drv_files = this.get("stats_db_driver_file").split(";");
                URL[] urls = new URL[db_drv_files.length];
                int x = 0;
                while (x < db_drv_files.length) {
                    urls[x] = new File_S(db_drv_files[x]).toURI().toURL();
                    ++x;
                }
                if (cl == null) {
                    cl = new URLClassLoader(urls);
                    drvCls = Class.forName(this.get("stats_db_driver"), true, cl);
                    driver = (Driver)drvCls.newInstance();
                }
                Object object = used_lock;
                synchronized (object) {
                    while (usedConnections.size() > Integer.parseInt(System.getProperty("crushftp.stats.sql.maxpool", "1000"))) {
                        Thread.sleep(100L);
                    }
                    Vector vector = freeConnections;
                    synchronized (vector) {
                        if (freeConnections.size() > 0) {
                            conn = (Connection)freeConnections.remove(0);
                            usedConnections.addElement(conn);
                            return conn;
                        }
                    }
                    Properties props = new Properties();
                    props.setProperty("user", this.get("stats_db_user"));
                    props.setProperty("password", ServerStatus.thisObj.common_code.decode_pass(this.get("stats_db_pass")));
                    conn = driver.connect(this.get("stats_db_url"), props);
                    usedConnections.addElement(conn);
                }
            }
            Object db_drv_files = used_lock;
            synchronized (db_drv_files) {
                while (usedConnections.size() > Integer.parseInt(System.getProperty("crushftp.stats.sql.maxpool", "1000"))) {
                    Thread.sleep(100L);
                }
                Vector vector = freeConnections;
                synchronized (vector) {
                    if (freeConnections.size() > 0) {
                        conn = (Connection)freeConnections.remove(0);
                        usedConnections.addElement(conn);
                        return conn;
                    }
                }
                Class.forName(this.get("stats_db_driver"));
                conn = DriverManager.getConnection(this.get("stats_db_url"), this.get("stats_db_user"), ServerStatus.thisObj.common_code.decode_pass(this.get("stats_db_pass")));
                usedConnections.addElement(conn);
            }
            conn.setAutoCommit(true);
        }
        catch (Exception e) {
            if (e.toString().indexOf("hsqldb") >= 0) {
                this.settings.put("stats_db_driver_file", "");
                this.settings.put("stats_db_driver", "org.apache.derby.jdbc.EmbeddedDriver");
                this.settings.put("stats_db_url", "jdbc:derby:" + System.getProperty("crushftp.stats") + "statsDB;create=true");
                this.settings.put("stats_db_user", "app");
                started = false;
                this.init();
                Class.forName(this.get("stats_db_driver"));
                conn = DriverManager.getConnection(this.get("stats_db_url"), this.get("stats_db_user"), this.get("stats_db_pass"));
                conn.setAutoCommit(true);
            }
            this.msg(e);
        }
        return conn;
    }

    public int getUserDownloadCount(String username) {
        if (ServerStatus.BG("disable_stats") || Common.dmz_mode) {
            return 0;
        }
        String sql = ServerStatus.SG("stats_get_transfers_download");
        if (sql.indexOf("transfers") >= 0 && sql.indexOf("sessions") >= 0 || sql.indexOf("where direction") >= 0 || sql.indexOf("and session_rid") >= 0 || sql.indexOf("(select rid") >= 0 || sql.indexOf("(where user_name") >= 0) {
            sql = crushftp.handlers.Common.replace_str(sql, " from transfers ", " from TRANSFERS ");
            sql = crushftp.handlers.Common.replace_str(sql, " from sessions ", " from SESSIONS ");
            sql = crushftp.handlers.Common.replace_str(sql, " where direction ", " where DIRECTION ");
            sql = crushftp.handlers.Common.replace_str(sql, " and session_rid ", " and SESSION_RID ");
            sql = crushftp.handlers.Common.replace_str(sql, " (select rid ", " (select RID ");
            sql = crushftp.handlers.Common.replace_str(sql, " where user_name ", " where USER_NAME ");
            ServerStatus.server_settings.put("stats_get_transfers_download", sql);
        }
        return Integer.parseInt(this.getValue(sql, new String[]{username}));
    }

    public Vector getMatchingMetas(String meta1_value, Properties server_item) {
        Vector matchingUploads = new Vector();
        this.findMetas(ServerStatus.SG("stats_get_meta_info"), new String[]{meta1_value}, matchingUploads);
        return matchingUploads;
    }

    public long getTransferAmountToday(String user_ip, String user_name, Properties userStat, String transfer_type, SessionCrush thisSession) {
        String totalStr;
        if (ServerStatus.BG("disable_stats") || Common.dmz_mode) {
            return 0L;
        }
        long total = 0L;
        long daySeconds = 86400000L;
        transfer_type = transfer_type.toUpperCase().substring(0, transfer_type.length() - 1);
        String user_or_ip = user_ip;
        if (ServerStatus.SG("stats_get_transfers_period").toUpperCase().indexOf("USER_NAME") >= 0) {
            user_or_ip = user_name;
        }
        if ((totalStr = this.getValue(ServerStatus.SG("stats_get_transfers_period"), new Object[]{transfer_type, user_or_ip, new Date(System.currentTimeMillis() - daySeconds)})) == null) {
            totalStr = "0";
        }
        total = Long.parseLong(totalStr);
        return total;
    }

    public long getTransferCountToday(String user_ip, String user_name, Properties userStat, String transfer_type, SessionCrush thisSession) {
        String totalStr;
        if (ServerStatus.BG("disable_stats") || Common.dmz_mode) {
            return 0L;
        }
        long total = 0L;
        long daySeconds = 86400000L;
        transfer_type = transfer_type.toUpperCase().substring(0, transfer_type.length() - 1);
        String user_or_ip = user_ip;
        if (ServerStatus.SG("stats_get_transfers_count_period").toUpperCase().indexOf("USER_NAME") >= 0) {
            user_or_ip = user_name;
        }
        if ((totalStr = this.getValue(ServerStatus.SG("stats_get_transfers_count_period"), new Object[]{transfer_type, user_or_ip, new Date(System.currentTimeMillis() - daySeconds)})) == null) {
            totalStr = "0";
        }
        total = Long.parseLong(totalStr);
        return total;
    }

    public long getTransferAmountThisMonth(String user_ip, String user_name, Properties userStat, String transfer_type, SessionCrush thisSession) {
        String totalStr;
        if (ServerStatus.BG("disable_stats") || Common.dmz_mode) {
            return 0L;
        }
        long total = 0L;
        long monthSeconds = 86400000L;
        monthSeconds *= 30L;
        transfer_type = transfer_type.toUpperCase().substring(0, transfer_type.length() - 1);
        String user_or_ip = user_ip;
        if (ServerStatus.SG("stats_get_transfers_period").toUpperCase().indexOf("USER_NAME") >= 0) {
            user_or_ip = user_name;
        }
        if ((totalStr = this.getValue(ServerStatus.SG("stats_get_transfers_period"), new Object[]{transfer_type, user_or_ip, new Date(System.currentTimeMillis() - monthSeconds)})) == null) {
            totalStr = "0";
        }
        total = Long.parseLong(totalStr);
        return total;
    }

    public long getTransferCountThisMonth(String user_ip, String user_name, Properties userStat, String transfer_type, SessionCrush thisSession) {
        String totalStr;
        if (ServerStatus.BG("disable_stats") || Common.dmz_mode) {
            return 0L;
        }
        long total = 0L;
        long monthSeconds = 86400000L;
        monthSeconds *= 30L;
        transfer_type = transfer_type.toUpperCase().substring(0, transfer_type.length() - 1);
        String user_or_ip = user_ip;
        if (ServerStatus.SG("stats_get_transfers_count_period").toUpperCase().indexOf("USER_NAME") >= 0) {
            user_or_ip = user_name;
        }
        if ((totalStr = this.getValue(ServerStatus.SG("stats_get_transfers_count_period"), new Object[]{transfer_type, user_or_ip, new Date(System.currentTimeMillis() - monthSeconds)})) == null) {
            totalStr = "0";
        }
        total = Long.parseLong(totalStr);
        return total;
    }

    public void clearMaxTransferAmounts(Properties pp) {
        String user_name = pp.getProperty("user_name");
        long duration = Long.parseLong(pp.getProperty("duration"));
        String transfer_type = pp.getProperty("transfer_type");
        transfer_type = transfer_type.toUpperCase().substring(0, transfer_type.length() - 1);
        this.setIgnore(user_name, transfer_type, String.valueOf(duration));
    }

    public void add_items_stat(SessionCrush theSession, Vector items, String action, String command) {
        int x = 0;
        while (x < items.size()) {
            Properties item = (Properties)items.get(x);
            VRL vrl = new VRL(item.getProperty("url"));
            String path = String.valueOf(item.getProperty("root_dir", "")) + item.getProperty("name", "");
            if (path.equals("")) {
                path = vrl.getPath();
            }
            item.put("the_command", command);
            item.put("the_command_data", path);
            item.put("url", "" + vrl);
            item.put("the_file_path", path);
            item.put("the_file_name", vrl.getName());
            item.put("the_file_size", String.valueOf(item.getProperty("size")));
            item.put("the_file_speed", "0");
            item.put("the_file_start", String.valueOf(new Date().getTime()));
            item.put("the_file_end", String.valueOf(new Date().getTime()));
            item.put("the_file_error", "");
            item.put("the_file_type", item.getProperty("type"));
            item.put("the_file_status", "SUCCESS");
            this.add_item_stat(theSession, item, action);
            ++x;
        }
    }

    public Properties add_item_stat(SessionCrush theSession, Properties item, String action) {
        return this.add_item_stat(theSession.uiSG("user_ip"), theSession.uiSG("sessionID"), theSession.user_info.getProperty("SESSION_RID"), item, action);
    }

    public Properties add_item_stat(String user_ip, String sessionID, String SESSION_RID, Properties item, String action) {
        Properties data_item = new Properties();
        data_item.put("date", "" + new Date().getTime());
        data_item.put("path", item.getProperty("the_file_path", ""));
        data_item.put("name", item.getProperty("the_file_name", ""));
        data_item.put("size", item.getProperty("the_file_size", ""));
        data_item.put("speed", item.getProperty("the_file_speed", ""));
        data_item.put("url", new VRL(item.getProperty("url", "")).safe());
        data_item.put("ip", user_ip);
        data_item.put("sessionID", sessionID == null ? "" : sessionID);
        long transfer_rid = StatTools.u();
        data_item.put("TRANSFER_RID", String.valueOf(transfer_rid));
        item.put("transfer_rid", String.valueOf(transfer_rid));
        if (item.getProperty("the_file_path", "").indexOf("/WebInterface/") < 0 && item.getProperty("the_file_path", "").indexOf(".DS_Store") < 0) {
            if (ServerStatus.BG("stat_auto_increment")) {
                String transfer_rid_value = this.executeSql(ServerStatus.SG("stats_insert_transfers"), new Object[]{SESSION_RID, new Date(), action, item.getProperty("the_file_path", ""), item.getProperty("the_file_name", ""), new VRL(item.getProperty("url", "")).safe(), item.getProperty("the_file_speed", ""), item.getProperty("the_file_size", "")});
                item.put("transfer_rid", transfer_rid_value);
                data_item.put("TRANSFER_RID", transfer_rid_value);
            } else {
                this.executeSql(ServerStatus.SG("stats_insert_transfers"), new Object[]{String.valueOf(transfer_rid), SESSION_RID, new Date(), action, item.getProperty("the_file_path", ""), item.getProperty("the_file_name", ""), new VRL(item.getProperty("url", "")).safe(), item.getProperty("the_file_speed", ""), item.getProperty("the_file_size", "")});
            }
        }
        return data_item;
    }

    public Properties add_login_stat(Properties server_item, String user_name, String user_ip, boolean success, SessionCrush theSession) throws Exception {
        return this.add_login_stat(String.valueOf(server_item.getProperty("ip")) + "_" + server_item.getProperty("port"), user_name, user_ip, success, theSession.user_info);
    }

    public Properties add_login_stat(String linkedServer, String user_name, String user_ip, boolean success, Properties user_info) throws Exception {
        long rid = StatTools.u();
        user_info.put("SESSION_RID", String.valueOf(rid));
        String sessionID = String.valueOf(user_info.getProperty("user_protocol")) + ":" + user_info.getProperty("listen_ip_port") + ":" + user_info.getProperty("connection_info", "");
        if (sessionID.length() > 100) {
            sessionID = sessionID.substring(0, 100);
        }
        user_info.put("sessionID", String.valueOf(sessionID));
        Properties p = this.add_login_stat(linkedServer, user_name, user_ip, success, sessionID, rid);
        if (ServerStatus.BG("stat_auto_increment") && p.containsKey("rid")) {
            user_info.put("SESSION_RID", p.getProperty("rid", ""));
        }
        return p;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Properties add_login_stat(String linkedServer, String user_name, String user_ip, boolean success, String session_id, long rid) throws Exception {
        Properties p = new Properties();
        if (user_name.equals("")) {
            return p;
        }
        if (ServerStatus.BG("stats_ignore_unauthenticated_users") && (user_name.equals("anonymous") || !success)) {
            return p;
        }
        int x = 0;
        while (!(x >= 10 || this.temp_disabled || ServerStatus.BG("disable_stats") || Common.dmz_mode)) {
            try {
                if (ServerStatus.BG("stat_auto_increment")) {
                    String rid_value = this.executeSql(ServerStatus.SG("stats_insert_sessions"), new Object[]{session_id, linkedServer, user_name, new Date(), new Date(1000000000000L), String.valueOf(success), user_ip});
                    p.put("rid", rid_value);
                } else {
                    this.executeSql(ServerStatus.SG("stats_insert_sessions"), new Object[]{String.valueOf(rid), session_id, linkedServer, user_name, new Date(), new Date(new Date().getTime() + 86400000L), String.valueOf(success), user_ip});
                }
                this.failed_login_stat_count = 0;
                break;
            }
            catch (Exception e) {
                ++this.failed_login_stat_count;
                Thread.sleep(500L);
                if (x > 5) {
                    Log.log("STATISTICS", 0, e);
                }
                Object object = this.failed_login_stat_lock;
                synchronized (object) {
                    if (this.failed_login_stat_count > 6) {
                        this.temp_disabled = true;
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                while (StatTools.this.temp_disabled) {
                                    try {
                                        Thread.sleep(10000L);
                                        if (!StatTools.this.temp_disabled) continue;
                                        Connection conn = StatTools.this.getConnection();
                                        try {
                                            PreparedStatement ps = conn.prepareStatement("select count(*) from SESSIONS");
                                            ps.executeQuery().close();
                                            ps.close();
                                            StatTools.this.releaseConnection(conn);
                                            StatTools.this.temp_disabled = false;
                                            break;
                                        }
                                        catch (Exception e) {
                                            conn.close();
                                        }
                                    }
                                    catch (Throwable e) {
                                        Log.log("SERVER", 2, e);
                                    }
                                }
                            }
                        }, "Stat temp disabled re-enabler");
                    }
                }
                ++x;
            }
        }
        return p;
    }
}

