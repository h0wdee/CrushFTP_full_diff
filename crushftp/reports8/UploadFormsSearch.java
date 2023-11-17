/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import com.crushftp.client.VRL;
import com.crushftp.tunnel2.DVector;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.reports8.ReportTools;
import crushftp.server.ServerStatus;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class UploadFormsSearch {
    Properties server_info = null;
    Properties server_settings = null;

    public void init(Properties server_settings, Properties server_info) {
        this.server_settings = server_settings;
        this.server_info = server_info;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void generate(Properties stats, Properties params, StringBuffer sb, Properties status) {
        try {
            String tmp;
            String sql;
            Properties metaSearch;
            Vector usernames;
            SimpleDateFormat mmddyyyy;
            block28: {
                Common.setupReportDates(params, params.getProperty("show", ""), params.getProperty("startDate"), params.getProperty("endDate"));
                mmddyyyy = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
                usernames = (Vector)params.get("usernames");
                metaSearch = (Properties)params.clone();
                Enumeration<Object> keys = metaSearch.keys();
                while (true) {
                    if (!keys.hasMoreElements()) {
                        sql = "select s.RID as \"rid\" \r FROM TRANSFERS t, SESSIONS s\r where s.RID = t.SESSION_RID\r and s.START_TIME >= ? and s.END_TIME <= ?\r /*START_USERNAMES*/and USER_NAME in (%usernames%)/*END_USERNAMES*/\r";
                        tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UploadFormsSearch.sql");
                        if (tmp == null) break;
                        sql = tmp;
                        break block28;
                    }
                    String key = keys.nextElement().toString();
                    if (key.toUpperCase().startsWith("META_")) continue;
                    metaSearch.remove(key);
                }
                Common.setFileText(sql, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UploadFormsSearch.sql");
            }
            sql = ReportTools.fixSqlUsernames(sql, usernames);
            try (DVector summary = ServerStatus.thisObj.statTools.executeSqlQuery(sql, new Object[]{mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);){
                String sql_transfers = "SELECT t.RID as \"rid\", SESSION_RID as \"session_rid\", cast(SPEED as bigint) as \"speed\", PATH as \"path\", FILE_NAME as \"name\", URL as \"url\", cast(TRANSFER_SIZE as bigint) as \"size\"\r FROM TRANSFERS t, SESSIONS s\r where s.RID = t.SESSION_RID\r and DIRECTION = ?\r and t.START_TIME >= ? and t.START_TIME <= ?\r order by t.START_TIME\r";
                tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UploadFormsSearch_transfers.sql");
                if (tmp != null) {
                    sql_transfers = tmp;
                } else {
                    Common.setFileText(sql_transfers, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UploadFormsSearch_transfers.sql");
                }
                Vector<Properties> uploads = new Vector<Properties>();
                try (DVector allUploads = ServerStatus.thisObj.statTools.executeSqlQuery(sql_transfers, new Object[]{"UPLOAD", mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);){
                    String sql_meta = "SELECT ITEM_KEY as \"item_key\", ITEM_VALUE as \"item_value\" \r FROM META_INFO \r WHERE TRANSFER_RID = ? \r";
                    tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UploadFormsSearch_meta.sql");
                    if (tmp != null) {
                        sql_meta = tmp;
                    } else {
                        Common.setFileText(sql_meta, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/UploadFormsSearch_meta.sql");
                    }
                    int x = 0;
                    block12: while (x < summary.size()) {
                        Properties sessions = (Properties)summary.elementAt(x);
                        int xx = 0;
                        while (true) {
                            if (xx >= allUploads.size()) {
                                ++x;
                                continue block12;
                            }
                            Properties transfer = (Properties)allUploads.elementAt(xx);
                            if (transfer.getProperty("session_rid", "").equals(sessions.getProperty("rid", ""))) {
                                Properties metaInfo = new Properties();
                                try (DVector transferMetas = ServerStatus.thisObj.statTools.executeSqlQuery(sql_meta, new Object[]{transfer.getProperty("rid")}, false, params);){
                                    int xxx = 0;
                                    while (transferMetas != null && xxx < transferMetas.size()) {
                                        Properties ppp = (Properties)transferMetas.elementAt(xxx);
                                        metaInfo.put(ppp.getProperty("item_key"), ppp.getProperty("item_value"));
                                        ++xxx;
                                    }
                                }
                                Enumeration<Object> e_metas = metaSearch.keys();
                                boolean ok = true;
                                while (true) {
                                    if (!e_metas.hasMoreElements()) {
                                        transfer.put("metaInfo", metaInfo);
                                        if (!ok) break;
                                        uploads.addElement(VRL.safe(transfer));
                                        break;
                                    }
                                    String meta_key = e_metas.nextElement().toString();
                                    String meta_val = metaSearch.getProperty(meta_key, "");
                                    if (meta_key.endsWith("___type") || meta_val.trim().equals("")) continue;
                                    String type = metaSearch.getProperty(String.valueOf(meta_key) + "___type", "");
                                    if (type.equals("text") || type.equals("textarea")) {
                                        if (metaInfo.getProperty(meta_key.substring("meta_".length()), "").toUpperCase().indexOf(meta_val.toUpperCase()) >= 0) continue;
                                        ok = false;
                                        continue;
                                    }
                                    if (metaInfo.getProperty(meta_key.substring("meta_".length()), "").toUpperCase().equals(meta_val.toUpperCase())) continue;
                                    ok = false;
                                }
                            }
                            ++xx;
                        }
                    }
                }
                Properties results = new Properties();
                results.put("uploads", uploads);
                results.put("showPaths", params.getProperty("showPaths"));
                results.put("showURLs", params.getProperty("showURLs"));
                results.put("showFormInfo", params.getProperty("showFormInfo"));
                int maxUp = Integer.parseInt(params.getProperty("uploadCount"));
                while (true) {
                    if (uploads.size() <= maxUp) {
                        Common common_code = new Common();
                        results.put("export", params.getProperty("export", ""));
                        results.put("params", Common.removeNonStrings(params).toString());
                        results.put("paramsObj", Common.removeNonStrings(params));
                        if (uploads.size() > 0) {
                            status.put("report_empty", "false");
                        }
                        sb.append(Common.getXMLString(results, "results", "WebInterface/Reports/UploadFormsSearch.xsl"));
                        return;
                    }
                    uploads.removeElementAt(uploads.size() - 1);
                }
            }
        }
        catch (Exception e) {
            Log.log("REPORT", 1, e);
        }
    }

    public Vector doSort(Properties item, counts c) {
        Enumeration<Object> e = item.keys();
        Vector<Object> v = new Vector<Object>();
        while (e.hasMoreElements()) {
            String key = e.nextElement().toString();
            v.addElement((Properties)item.get(key));
        }
        Object[] objs = v.toArray();
        Arrays.sort(objs, c);
        v.removeAllElements();
        int x = 0;
        while (x < objs.length) {
            v.addElement(objs[x]);
            ++x;
        }
        return v;
    }

    public void updateItems(Properties p, Properties details, String username, SimpleDateFormat sdf, long start, long end) {
        long date = Long.parseLong(p.getProperty("date"));
        if (date < start || date > end) {
            return;
        }
        if (details.get(p.getProperty("url")) == null) {
            Properties pp = new Properties();
            pp.putAll((Map<?, ?>)p);
            pp.remove("speed");
            pp.remove("ip");
            pp.remove("date");
            pp.put("dates", new Vector());
            pp.put("speeds", new Vector());
            pp.put("users", new Vector());
            pp.put("ips", new Vector());
            details.put(p.getProperty("url"), pp);
        }
        Properties itemDetails = (Properties)details.get(p.getProperty("url"));
        int i = Integer.parseInt(itemDetails.getProperty("count", "0"));
        itemDetails.put("count", String.valueOf(++i));
        long speed = Long.parseLong(p.getProperty("speed"));
        long sumSpeed = Long.parseLong(itemDetails.getProperty("sumSpeed", "0")) + speed;
        itemDetails.put("sumSpeed", String.valueOf(sumSpeed));
        if (((Vector)itemDetails.get("speeds")).size() == 0) {
            itemDetails.put("averageSpeed", "0");
        } else {
            itemDetails.put("averageSpeed", String.valueOf(sumSpeed / (long)((Vector)itemDetails.get("speeds")).size()));
        }
        ((Vector)itemDetails.get("speeds")).addElement(p.getProperty("speed"));
        if (((Vector)itemDetails.get("ips")).indexOf(p.getProperty("ip")) < 0) {
            ((Vector)itemDetails.get("ips")).addElement(p.getProperty("ip"));
        }
        if (((Vector)itemDetails.get("users")).indexOf(username) < 0) {
            ((Vector)itemDetails.get("users")).addElement(username);
        }
        String dateStr = sdf.format(new Date(date));
        if (((Vector)itemDetails.get("dates")).indexOf(dateStr) < 0) {
            ((Vector)itemDetails.get("dates")).addElement(dateStr);
        }
    }

    class counts
    implements Comparator {
        Properties allItems = null;
        String sort = null;

        counts() {
        }

        public void setObj(Properties allItems, String sort) {
            this.allItems = allItems;
            this.sort = sort;
        }

        public int compare(Object p1, Object p2) {
            String val1 = ((Properties)p1).getProperty(this.sort, "0");
            String val2 = ((Properties)p2).getProperty(this.sort, "0");
            try {
                if (Integer.parseInt(val1) > Integer.parseInt(val2)) {
                    return -1;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                if (Integer.parseInt(val1) < Integer.parseInt(val2)) {
                    return 1;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            return val1.compareTo(val2) * -1;
        }
    }
}

