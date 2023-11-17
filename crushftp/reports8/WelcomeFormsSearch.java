/*
 * Decompiled with CFR 0.152.
 */
package crushftp.reports8;

import com.crushftp.tunnel2.DVector;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.reports8.ReportTools;
import crushftp.server.ServerStatus;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class WelcomeFormsSearch {
    Properties server_info = null;
    Properties server_settings = null;

    public void init(Properties server_settings, Properties server_info) {
        this.server_settings = server_settings;
        this.server_info = server_info;
    }

    public void generate(Properties stats, Properties params, StringBuffer sb, Properties status) {
        try {
            Common.setupReportDates(params, params.getProperty("show", ""), params.getProperty("startDate"), params.getProperty("endDate"));
            SimpleDateFormat mmddyyyy = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
            Vector usernames = (Vector)params.get("usernames");
            String sql = "select s.USER_NAME as \"username\", s.RID as \"rid\", s.START_TIME as \"date\" \r FROM SESSIONS s\r where s.START_TIME >= ? and s.END_TIME <= ?\r /*START_USERNAMES*/and USER_NAME in (%usernames%)/*END_USERNAMES*/\r";
            String tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/WelcomeFormsSearch.sql");
            if (tmp != null) {
                sql = tmp;
            } else {
                Common.setFileText(sql, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/WelcomeFormsSearch.sql");
            }
            sql = ReportTools.fixSqlUsernames(sql, usernames);
            try (DVector summary = ServerStatus.thisObj.statTools.executeSqlQuery(sql, new Object[]{mmddyyyy.parse(params.getProperty("startDate", "1/1/2000 00:00:00")), mmddyyyy.parse(params.getProperty("endDate", "1/1/2100 00:00:00"))}, false, params);){
                String sql_meta = "SELECT ITEM_KEY as \"item_key\", ITEM_VALUE as \"item_value\" \r FROM META_INFO \r WHERE SESSION_RID = ? and TRANSFER_RID = 0 \r";
                tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/WelcomeFormsSearch_meta.sql");
                if (tmp != null) {
                    sql_meta = tmp;
                } else {
                    Common.setFileText(sql_meta, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/WelcomeFormsSearch_meta.sql");
                }
                String sql_transfers = "SELECT DIRECTION as \"direction\", cast(SPEED as bigint) as \"speed\", PATH as \"path\", FILE_NAME as \"name\", URL as \"url\", cast(TRANSFER_SIZE as bigint) as \"size\"\r FROM TRANSFERS t\r where t.SESSION_RID = ? \r";
                tmp = Common.getFileText(String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/WelcomeFormsSearch_transfers.sql");
                if (tmp != null) {
                    sql_transfers = tmp;
                } else {
                    Common.setFileText(sql_transfers, String.valueOf(System.getProperty("crushftp.web")) + "WebInterface/Reports/WelcomeFormsSearch_transfers.sql");
                }
                Vector<Properties> metas = new Vector<Properties>();
                int x = 0;
                while (x < summary.size()) {
                    Properties p = (Properties)summary.elementAt(x);
                    DVector transferMetas = ServerStatus.thisObj.statTools.executeSqlQuery(sql_meta, new Object[]{p.getProperty("rid")}, false, params);
                    Properties metaInfo = new Properties();
                    int xxx = 0;
                    while (xxx < transferMetas.size()) {
                        Properties ppp = (Properties)transferMetas.elementAt(xxx);
                        metaInfo.put(ppp.getProperty("item_key"), ppp.getProperty("item_value"));
                        ++xxx;
                    }
                    p.put("metaInfo", metaInfo);
                    if (transferMetas.size() > 0) {
                        Vector transfers = ServerStatus.thisObj.statTools.executeSqlQuery_mem(sql_transfers, new Object[]{p.getProperty("rid")}, false, params);
                        if (transfers != null) {
                            p.put("transfers", transfers);
                        }
                        metas.addElement(p);
                    }
                    transferMetas.close();
                    ++x;
                }
                Properties results = new Properties();
                results.put("metas", metas);
                Common common_code = new Common();
                results.put("export", params.getProperty("export", ""));
                results.put("params", Common.removeNonStrings(params).toString());
                results.put("paramsObj", Common.removeNonStrings(params));
                if (metas.size() > 0) {
                    status.put("report_empty", "false");
                }
                sb.append(Common.getXMLString(results, "results", "WebInterface/Reports/WelcomeFormsSearch.xsl"));
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

