/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.File_S;
import com.crushftp.client.File_U;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.SharedSessionReplicated;
import crushftp.handlers.UserTools;
import crushftp.server.ServerStatus;
import crushftp.server.VFS;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class QuotaWorker
implements Runnable {
    boolean refresh = false;
    static boolean running = false;
    static boolean first_run = true;
    public static Properties memoryQuota = new Properties();
    public static Properties memoryQuota_time = new Properties();
    static Properties async_scan_locations = new Properties();
    static Properties async_scan_users = new Properties();
    int users_with_quota = 0;
    int items_tracked_total = 0;
    long total_xml_user_loading_time = 0L;
    long total_xml_vfs_loading_time = 0L;
    static Object quota_start_lock = new Object();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        boolean success_scan;
        Properties items_found_total;
        long start;
        block16: {
            Object object = quota_start_lock;
            synchronized (object) {
                if (running) {
                    return;
                }
                running = true;
            }
            Properties pp = new Properties();
            pp.put("need_response", "true");
            SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.QuotaWorker.getStatus", "info", pp);
            long start2 = System.currentTimeMillis();
            while (pp.getProperty("response_num", "0").equals("0") && System.currentTimeMillis() - start2 < 5000L) {
                try {
                    Thread.sleep(100L);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            Properties val = (Properties)pp.get("val");
            if (val != null && val.getProperty("quota_status", "").toUpperCase().indexOf("RUNNING") >= 0) {
                ServerStatus.thisObj.server_info.put("last_quota_check", String.valueOf(System.currentTimeMillis()));
                running = false;
                Log.log("QUOTA", 0, "QuotaWorker:scan running on opposite server, skipping scan on this server, waiting for shared results.");
                return;
            }
            Log.log("QUOTA", 0, "QuotaWorker:scan started.");
            Thread.currentThread().setName("QuotaWorker:");
            start = System.currentTimeMillis();
            items_found_total = new Properties();
            items_found_total.put("items_found_total", "0");
            String scan_time = String.valueOf(System.currentTimeMillis());
            this.total_xml_user_loading_time = 0L;
            this.total_xml_vfs_loading_time = 0L;
            success_scan = true;
            try {
                try {
                    Vector sgs = (Vector)ServerStatus.server_settings.get("server_groups");
                    int x = 0;
                    while (x < sgs.size()) {
                        String serverGroup = sgs.elementAt(x).toString();
                        Thread.currentThread().setName("QuotaWorker:" + serverGroup + ":");
                        Vector user_list = new Vector();
                        long start_xml_loading = System.currentTimeMillis();
                        UserTools.refreshUserList(serverGroup, user_list);
                        this.total_xml_user_loading_time += System.currentTimeMillis() - start_xml_loading;
                        int xx = 0;
                        while (xx < user_list.size()) {
                            ServerStatus.thisObj.server_info.put("last_quota_check", String.valueOf(System.currentTimeMillis()));
                            start_xml_loading = System.currentTimeMillis();
                            String username = com.crushftp.client.Common.dots(user_list.elementAt(xx).toString());
                            Thread.currentThread().setName("QuotaWorker:async_scan_locations.size=" + async_scan_locations.size() + " async_scan_users.size=" + async_scan_users.size() + ":" + serverGroup + ":(" + xx + "/" + user_list.size() + "):" + username + ":MASTER");
                            Properties user = UserTools.ut.getUser(serverGroup, username, true);
                            this.total_xml_user_loading_time += System.currentTimeMillis() - start_xml_loading;
                            Thread.sleep(ServerStatus.LG("quota_async_user_delay"));
                            this.startUserScanThread(serverGroup, username, xx, user_list, user, items_found_total, scan_time);
                            ++xx;
                        }
                        ++x;
                    }
                    int loops = 0;
                    while (async_scan_locations.size() > 0 && loops++ < 3600) {
                        Thread.sleep(1000L);
                        ServerStatus.thisObj.server_info.put("quota_async_locations_size", String.valueOf(async_scan_locations.size()));
                        Thread.currentThread().setName("QuotaWorker:async_scan_locations.size=" + async_scan_locations.size() + " async_scan_users.size=" + async_scan_users.size() + ":MASTER");
                    }
                }
                catch (Exception e) {
                    Log.log("QUOTA", 1, e);
                    success_scan = true;
                    running = false;
                    first_run = false;
                    break block16;
                }
            }
            catch (Throwable throwable) {
                running = false;
                first_run = false;
                throw throwable;
            }
            running = false;
            first_run = false;
        }
        String quota_timing_info = "QuotaWorker:scan complete.  Success=" + success_scan + "  Seconds=" + (System.currentTimeMillis() - start) / 1000L + " User XML Loading=" + this.total_xml_user_loading_time / 1000L + "secs VFS XML Loading=" + this.total_xml_vfs_loading_time / 1000L + "secs, memoryQuota.size=" + memoryQuota.size();
        QuotaWorker.sendSharedQuota(quota_timing_info);
        Log.log("QUOTA", 0, quota_timing_info);
        ServerStatus.thisObj.server_info.put("quota_async_scan_time_secs", String.valueOf((System.currentTimeMillis() - start) / 1000L));
        ServerStatus.thisObj.server_info.put("quota_async_scan_users", String.valueOf(this.users_with_quota));
        ServerStatus.thisObj.server_info.put("quota_async_scan_tracked", String.valueOf(this.items_tracked_total));
        ServerStatus.thisObj.server_info.put("quota_async_scan_items_total", items_found_total.getProperty("items_found_total", "0"));
        ServerStatus.thisObj.server_info.put("quota_async_locations_size", String.valueOf(async_scan_locations.size()));
    }

    public static void sendSharedQuota(String quota_timing_info) {
        Properties pp = new Properties();
        pp.put("need_response", "true");
        pp.put("memoryQuota", memoryQuota);
        pp.put("quota_timing_info", quota_timing_info);
        SharedSessionReplicated.send(Common.makeBoundary(), "crushftp.QuotaWorker.receiveSharedQuota", "info", pp);
    }

    public static void receiveSharedQuota(Properties val) {
        ServerStatus.thisObj.server_info.put("last_quota_check", String.valueOf(System.currentTimeMillis()));
        memoryQuota.putAll((Map<?, ?>)((Properties)val.get("memoryQuota")));
        Log.log("QUOTA", 0, "QuotaWorker:CLUSTERED_SERVER_QUOTA_RESULT:" + val.getProperty("quota_timing_info", "") + ": localMemoryQuota.size=" + memoryQuota.size());
    }

    public static String getStatus() {
        if (running) {
            return "RUNNING";
        }
        return "IDLE";
    }

    /*
     * Enabled aggressive block sorting
     */
    public void startUserScanThread(final String serverGroup, final String username, final int xx, final Vector user_list, final Properties user, final Properties items_found_total, final String scan_time) throws Exception {
        int loops = 0;
        while (async_scan_users.size() > ServerStatus.IG("quota_async_user_threads") && loops++ < 6000) {
            Thread.sleep(100L);
        }
        Worker.startWorker(new Runnable(){

            /*
             * Unable to fully structure code
             */
            @Override
            public void run() {
                block37: {
                    block36: {
                        running_async_threads = new Properties();
                        start_xml_loading = System.currentTimeMillis();
                        uVFS = UserTools.ut.getVFS(serverGroup, username);
                        QuotaWorker.async_scan_users.put(username, "");
                        try {
                            try {
                                virtual = UserTools.ut.getVirtualVFS(serverGroup, username);
                                QuotaWorker.this.total_xml_vfs_loading_time += System.currentTimeMillis() - start_xml_loading;
                                Thread.sleep(ServerStatus.LG("quota_async_vfs_delay"));
                                permissions = (Properties)((Vector)virtual.get("vfs_permissions_object")).elementAt(0);
                                keys = permissions.propertyNames();
                                while (keys.hasMoreElements()) {
                                    key = (String)keys.nextElement();
                                    Thread.currentThread().setName("QuotaWorker:" + serverGroup + ":(" + xx + "/" + user_list.size() + "):" + username + ":" + key);
                                    val = permissions.getProperty(key);
                                    if ((val.indexOf("(quota") < 0 || val.indexOf("(real_quota)") < 0) && (user == null || user.getProperty("quota_mb", "").equals(""))) continue;
                                    ++QuotaWorker.this.users_with_quota;
                                    keys2 = virtual.propertyNames();
                                    while (keys2.hasMoreElements()) {
                                        key2 = (String)keys2.nextElement();
                                        if (key2.equals("vfs_permissions_object") || !key.toUpperCase().startsWith(key2.toUpperCase())) continue;
                                        total_size = new Properties();
                                        total_size.put("total_size", "0");
                                        start_item = uVFS.get_item(key2, 0);
                                        if (ServerStatus.BG("quota_async_local_only") && !new VRL(start_item.getProperty("url")).getProtocol().equalsIgnoreCase("FILE")) continue;
                                        real_path = start_item.getProperty("root_dir");
                                        if (real_path.equals("/")) {
                                            real_path = key2;
                                        }
                                        v = new Vector<E>();
                                        found = true;
                                        infinite_loop = 0L;
                                        start_scan_key = System.currentTimeMillis();
                                        Log.log("QUOTA", 0, "QuotaWorker:Scanning VFS for key:" + serverGroup + ":" + username + ":" + key);
                                        try {
                                            block16: while (found && infinite_loop < 10000L) {
                                                v.removeAllElements();
                                                uVFS.getListing(v, real_path, 1, 99999, false, null, null, true);
                                                found = false;
                                                xxx = 0;
                                                while (xxx < v.size()) {
                                                    p = (Properties)v.elementAt(xxx);
                                                    if (key.toUpperCase().startsWith(String.valueOf(p.getProperty("root_dir").toUpperCase()) + p.getProperty("name").toUpperCase()) && !key.equalsIgnoreCase(String.valueOf(p.getProperty("root_dir")) + p.getProperty("name") + "/")) {
                                                        start_item = p;
                                                        real_path = String.valueOf(start_item.getProperty("root_dir")) + start_item.getProperty("name") + "/";
                                                        found = true;
                                                        if (start_item.getProperty("type").equalsIgnoreCase("FILE") && !start_item.getProperty("url").toUpperCase().startsWith("VIRTUAL:")) {
                                                            found = false;
                                                        }
                                                        ++infinite_loop;
                                                        continue block16;
                                                    }
                                                    if (key.equalsIgnoreCase(String.valueOf(p.getProperty("root_dir")) + p.getProperty("name") + "/")) {
                                                        start_item = p;
                                                        real_path = String.valueOf(start_item.getProperty("root_dir")) + start_item.getProperty("name") + "/";
                                                        found = false;
                                                        ++infinite_loop;
                                                        continue block16;
                                                    }
                                                    ++xxx;
                                                }
                                            }
                                        }
                                        catch (Exception e) {
                                            Log.log("QUOTA", 0, e);
                                            Log.log("QUOTA", 0, "QuotaWorker:Stopping scan for VFS key:" + serverGroup + ":" + username + ":" + key);
                                            continue;
                                        }
                                        if (!key.equalsIgnoreCase(String.valueOf(start_item.getProperty("root_dir")) + start_item.getProperty("name") + "/")) continue;
                                        root_item = start_item;
                                        status = new StringBuffer();
                                        vrl_key = VRL.fileFix(root_item.getProperty("url").toUpperCase());
                                        loops = 0;
                                        ServerStatus.thisObj.server_info.put("quota_async_locations_size", String.valueOf(QuotaWorker.async_scan_locations.size()));
                                        if (true) ** GOTO lbl82
                                        do {
                                            Thread.sleep(100L);
lbl82:
                                            // 2 sources

                                        } while (QuotaWorker.async_scan_locations.size() >= ServerStatus.IG("quota_async_threads") && loops++ < 6000);
                                        Thread.currentThread().setName("QuotaWorker:" + serverGroup + ":(" + xx + "/" + user_list.size() + "):" + username + ":" + key + ":" + QuotaWorker.async_scan_locations.size());
                                        if (!QuotaWorker.async_scan_locations.containsKey(vrl_key)) {
                                            scan_path1 = "";
                                            scan_vrl_key = vrl_key;
                                            if (!user.getProperty("parent_quota_dir", "").equals("") && new VRL(root_item.getProperty("url")).getProtocol().equalsIgnoreCase("FILE")) {
                                                scan_path1 = Common.dots(String.valueOf(new VRL(root_item.getProperty("url")).getPath()) + user.getProperty("parent_quota_dir", ""));
                                                scan_vrl_key = Common.dots(String.valueOf(scan_vrl_key) + user.getProperty("parent_quota_dir", ""));
                                            } else {
                                                scan_path1 = String.valueOf(root_item.getProperty("root_dir")) + root_item.getProperty("name") + "/";
                                            }
                                            scan_path2 = scan_path1;
                                            scan_vrl_key_f = scan_vrl_key;
                                            if (!QuotaWorker.memoryQuota_time.getProperty(scan_vrl_key, "0").equals(scan_time)) {
                                                QuotaWorker.memoryQuota_time.put(scan_vrl_key, scan_time);
                                                QuotaWorker.async_scan_locations.put(vrl_key, "");
                                                try {
                                                    if (ServerStatus.siIG("server_cpu") > 90) {
                                                        Thread.sleep(10000L);
                                                    } else if (ServerStatus.siIG("server_cpu") > 80) {
                                                        Thread.sleep(5000L);
                                                    } else if (ServerStatus.siIG("server_cpu") > 50) {
                                                        Thread.sleep(2000L);
                                                    }
                                                }
                                                catch (Exception e) {
                                                    Log.log("QUOTA", 2, "Server CPU : " + ServerStatus.siSG("server_cpu") + " Error : " + e);
                                                    Thread.sleep(10000L);
                                                }
                                                r = new Runnable(){

                                                    @Override
                                                    public void run() {
                                                        try {
                                                            try {
                                                                final Vector listing = new Vector();
                                                                Worker.startWorker(new Runnable(){

                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            if (!user.getProperty("parent_quota_dir", "").equals("") && new VRL(root_item.getProperty("url")).getProtocol().equalsIgnoreCase("FILE")) {
                                                                                Common.appendListing_U(scan_path2, listing, "", 99, false);
                                                                            } else {
                                                                                uVFS.getListing(listing, scan_path2, 99, 9999, false, null, null, true);
                                                                            }
                                                                        }
                                                                        catch (Exception ee) {
                                                                            Log.log("QUOTA", 1, ee);
                                                                            status.append("error:" + ee);
                                                                        }
                                                                        status.append("done");
                                                                    }
                                                                }, String.valueOf(Thread.currentThread().getName()) + ":" + new VRL(vrl_key).safe() + ":lister");
                                                                while (status.length() == 0 || listing.size() > 0) {
                                                                    try {
                                                                        if (ServerStatus.siIG("server_cpu") > 90) {
                                                                            Thread.sleep(10000L);
                                                                        } else if (ServerStatus.siIG("server_cpu") > 80) {
                                                                            Thread.sleep(5000L);
                                                                        } else if (ServerStatus.siIG("server_cpu") > 50) {
                                                                            Thread.sleep(2000L);
                                                                        }
                                                                    }
                                                                    catch (Exception e) {
                                                                        Log.log("QUOTA", 2, "Server CPU : " + ServerStatus.siSG("server_cpu") + " Error : " + e);
                                                                        Thread.sleep(10000L);
                                                                    }
                                                                    if (listing.size() <= 0) continue;
                                                                    if (listing.elementAt(0) instanceof File_U) {
                                                                        File_U f = (File_U)listing.remove(0);
                                                                        total_size.put("total_size", "" + (Long.parseLong(total_size.getProperty("total_size", "0")) + f.length()));
                                                                        items_found_total.put("items_found_total", "" + (Long.parseLong(items_found_total.getProperty("items_found_total", "0")) + 1L));
                                                                        continue;
                                                                    }
                                                                    Properties p = (Properties)listing.remove(0);
                                                                    if (!p.getProperty("type", "").equalsIgnoreCase("FILE")) continue;
                                                                    total_size.put("total_size", "" + (Long.parseLong(total_size.getProperty("total_size", "0")) + Long.parseLong(p.getProperty("size", "0"))));
                                                                    items_found_total.put("items_found_total", "" + (Long.parseLong(items_found_total.getProperty("items_found_total", "0")) + 1L));
                                                                }
                                                                memoryQuota.put(VRL.fileFix(scan_vrl_key_f).toUpperCase(), total_size.getProperty("total_size"));
                                                                Log.log("QUOTA", 0, "QuotaWorker:NEW_RECORD:" + serverGroup + ":" + username + ":" + user.getProperty("parent_quota_dir", "") + ":record key=" + VRL.fileFix(scan_vrl_key_f).toUpperCase() + ", URL=" + new VRL(vrl_key).safe() + ":SIZE=" + com.crushftp.client.Common.format_bytes_short(Long.parseLong(total_size.getProperty("total_size"))) + " (" + total_size.getProperty("total_size") + ") Secs:" + (System.currentTimeMillis() - start_scan_key) + "ms");
                                                            }
                                                            catch (Exception e) {
                                                                Log.log("QUOTA", 1, e);
                                                                async_scan_locations.remove(vrl_key);
                                                                running_async_threads.remove(vrl_key);
                                                            }
                                                        }
                                                        finally {
                                                            async_scan_locations.remove(vrl_key);
                                                            running_async_threads.remove(vrl_key);
                                                        }
                                                    }
                                                };
                                                running_async_threads.put(vrl_key, "");
                                                Worker.startWorker(r, String.valueOf(Thread.currentThread().getName()) + ":" + new VRL(vrl_key).safe() + ":worker");
                                            } else {
                                                Log.log("QUOTA", 0, "QuotaWorker:SKIP_RECORD:" + serverGroup + ":" + username + ":" + user.getProperty("parent_quota_dir", "") + ":record key=" + VRL.fileFix(scan_vrl_key_f).toUpperCase() + ", URL=" + new VRL(vrl_key).safe());
                                            }
                                            ++QuotaWorker.this.items_tracked_total;
                                        }
                                        Thread.currentThread().setName("QuotaWorker:" + serverGroup + ":(" + xx + "/" + user_list.size() + "):" + username + ":" + key + ":" + QuotaWorker.async_scan_locations.size());
                                    }
                                }
                                break block36;
                            }
                            catch (Exception e) {
                                Log.log("QUOTA", 2, "Scan failure...Error : " + e);
                                Log.log("QUOTA", 2, e);
                                QuotaWorker.async_scan_users.remove(username);
                                ** while (running_async_threads.size() > 0)
                            }
                        }
                        catch (Throwable var30_34) {
                            QuotaWorker.async_scan_users.remove(username);
                            ** while (running_async_threads.size() > 0)
                        }
lbl-1000:
                        // 1 sources

                        {
                            try {
                                Thread.sleep(500L);
                            }
                            catch (InterruptedException var31_31) {
                                // empty catch block
                            }
                            continue;
                        }
lbl142:
                        // 1 sources

                        uVFS.disconnect();
                        break block37;
lbl-1000:
                        // 1 sources

                        {
                            try {
                                Thread.sleep(500L);
                            }
                            catch (InterruptedException var31_32) {
                                // empty catch block
                            }
                            continue;
                        }
lbl154:
                        // 1 sources

                        uVFS.disconnect();
                        throw var30_34;
                    }
                    QuotaWorker.async_scan_users.remove(username);
                    while (running_async_threads.size() > 0) {
                        try {
                            Thread.sleep(500L);
                        }
                        catch (InterruptedException var31_33) {
                            // empty catch block
                        }
                    }
                    uVFS.disconnect();
                }
            }
        }, String.valueOf(Thread.currentThread().getName()) + ":" + username + ":quota_scan_worker");
    }

    public static long get_quota_used(String the_dir, VFS uVFS, String parentQuotaDir, SessionCrush thisSession) throws Exception {
        if (parentQuotaDir == null || parentQuotaDir.equals("parent_quota_dir")) {
            parentQuotaDir = "";
        }
        if (!ServerStatus.BG("quota_async")) {
            return QuotaWorker.get_quota_used_now(the_dir, uVFS, parentQuotaDir, thisSession);
        }
        if (first_run) {
            return 0L;
        }
        Properties item = uVFS.get_item_parent(the_dir);
        String val = null;
        String url_chopping = Common.dots(String.valueOf(item.getProperty("url")) + parentQuotaDir);
        int x = 0;
        while (x < 30 && val == null) {
            val = memoryQuota.getProperty(VRL.fileFix(url_chopping).toUpperCase());
            if (new VRL(url_chopping).getPath().equals("/")) break;
            if (val == null) {
                url_chopping = Common.all_but_last(url_chopping);
            }
            ++x;
        }
        if (val == null) {
            val = "0";
        }
        Log.log("QUOTA", 2, "Getting quota for directory:(" + parentQuotaDir + "):" + the_dir + ":" + VRL.fileFix(Common.dots(String.valueOf(item.getProperty("url")) + parentQuotaDir)).toUpperCase() + ":size=" + com.crushftp.client.Common.format_bytes_short(Long.parseLong(val)));
        if (val != null && !val.equals("")) {
            return Long.parseLong(val);
        }
        return 0L;
    }

    public static long get_quota_used_now(String the_dir, VFS uVFS, String parentQuotaDir, SessionCrush thisSession) throws Exception {
        block27: {
            Properties item;
            block26: {
                item = uVFS.get_item(uVFS.getPrivPath(the_dir));
                if (!ServerStatus.BG("quota_async_local_only") || new VRL(item.getProperty("url")).getProtocol().equalsIgnoreCase("FILE")) break block26;
                return -12345L;
            }
            try {
                if (item.getProperty("privs", "").indexOf("(quota") < 0) break block27;
                if (item.getProperty("privs", "").indexOf("(real_quota)") >= 0) {
                    Properties p;
                    Log.log("QUOTA", 3, "get_quota_used: checking for cache or disk quota");
                    String real_path = "";
                    if (parentQuotaDir.startsWith("FILE:") || parentQuotaDir.startsWith("file:")) {
                        real_path = new VRL(parentQuotaDir).getPath();
                    } else {
                        String parentAddon = parentQuotaDir;
                        if (parentAddon.equals("parent_quota_dir")) {
                            parentAddon = "";
                        }
                        real_path = !new VRL(item.getProperty("url")).getCanonicalPath().endsWith("/") ? String.valueOf(new VRL(item.getProperty("url")).getCanonicalPath()) + "/" + parentAddon : String.valueOf(new VRL(item.getProperty("url")).getCanonicalPath()) + parentAddon;
                    }
                    long size = -12345L;
                    if (VFS.quotaCache.containsKey(real_path.toUpperCase())) {
                        p = (Properties)VFS.quotaCache.get(real_path.toUpperCase());
                        if (Long.parseLong(p.getProperty("time")) < new Date().getTime() - 300000L) {
                            VFS.quotaCache.remove(real_path.toUpperCase());
                        } else {
                            size = Long.parseLong(p.getProperty("size"));
                        }
                    }
                    Log.log("QUOTA", 3, "get_quota_used: checking " + the_dir + " for cache or disk quota:" + size);
                    if (size == -12345L) {
                        while (VFS.activeQuotaChecks.size() > Integer.parseInt(System.getProperty("crushftp.quotathreads", "5"))) {
                            Thread.sleep(100L);
                        }
                        while (VFS.activeQuotaChecks.indexOf(real_path) >= 0) {
                            Thread.sleep(100L);
                        }
                        if (VFS.quotaCache.containsKey(real_path.toUpperCase())) {
                            p = (Properties)VFS.quotaCache.get(real_path.toUpperCase());
                            if (Long.parseLong(p.getProperty("time")) < new Date().getTime() - 300000L) {
                                VFS.quotaCache.remove(real_path.toUpperCase());
                            } else {
                                size = Long.parseLong(p.getProperty("size"));
                            }
                        }
                        Log.log("QUOTA", 3, "get_quota_used: checking " + the_dir + " for cache:" + size);
                        if (size == -12345L) {
                            try {
                                VFS.activeQuotaChecks.addElement(real_path);
                                Properties qp = new Properties();
                                qp.put("realPath", real_path);
                                if (thisSession != null) {
                                    thisSession.runPlugin("getUsedQuota", qp);
                                }
                                if (!qp.getProperty("usedQuota", "").equals("")) {
                                    size = Long.parseLong(qp.getProperty("usedQuota", "0"));
                                }
                                Log.log("QUOTA", 3, "get_quota_used: checking " + the_dir + " for plugin returned size:" + size);
                                if (size == -12345L) {
                                    VRL vrl = new VRL(item.getProperty("url"));
                                    if (vrl.getProtocol().equalsIgnoreCase("FILE")) {
                                        size = Common.recurseSize_U(real_path, 0L, thisSession);
                                    } else if (vrl.getProtocol().equalsIgnoreCase("S3CRUSH")) {
                                        size = Common.recurseSizeOfS3Crush(new File_S(String.valueOf(System.getProperty("crushftp.s3_root", "./s3/")) + vrl.getPath()), 0L, thisSession);
                                    }
                                    Log.log("QUOTA", 3, "get_quota_used: checking " + the_dir + " with protocol:" + vrl.getProtocol() + " for final size:" + size);
                                }
                                Log.log("QUOTA", 3, "get_quota_used: caching and returning " + the_dir + " for final size:" + size);
                                Properties p2 = new Properties();
                                p2.put("time", String.valueOf(new Date().getTime()));
                                p2.put("size", String.valueOf(size));
                                VFS.quotaCache.put(real_path.toUpperCase(), p2);
                            }
                            finally {
                                VFS.activeQuotaChecks.remove(real_path);
                            }
                        }
                    }
                    return size;
                }
                return -12345L;
            }
            catch (Exception e) {
                Log.log("SERVER", 3, e);
                if (("" + e).indexOf("Interrupted") < 0) break block27;
                throw e;
            }
        }
        return -12345L;
    }

    public static void add_quota_to_all_vfs_entries(long quota, VFS uVFS) {
        int x = 0;
        while (x < uVFS.homes.size()) {
            Properties p = (Properties)uVFS.homes.elementAt(x);
            Vector vfs_permissions_object = (Vector)p.get("vfs_permissions_object");
            int xx = 0;
            while (xx < vfs_permissions_object.size()) {
                Properties permissions = (Properties)vfs_permissions_object.elementAt(xx);
                Enumeration<Object> keys = permissions.keys();
                while (keys.hasMoreElements()) {
                    String key = "" + keys.nextElement();
                    String val = permissions.getProperty(key);
                    if (val.indexOf("(quota") >= 0) continue;
                    val = String.valueOf(val) + "(quota" + quota * 1024L * 1024L + ")(real_quota)";
                    permissions.put(key, val);
                }
                ++xx;
            }
            ++x;
        }
    }
}

