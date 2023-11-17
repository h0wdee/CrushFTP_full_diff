/*
 * Decompiled with CFR 0.152.
 */
package crushftp.db;

import com.crushftp.client.Common;
import com.crushftp.client.FileClient;
import com.crushftp.client.File_S;
import com.crushftp.client.File_U;
import com.crushftp.client.GenericClient;
import com.crushftp.client.GenericClientMulti;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import crushftp.handlers.Log;
import crushftp.handlers.PreviewWorker;
import crushftp.handlers.SessionCrush;
import crushftp.server.LIST_handler;
import crushftp.server.ServerStatus;
import crushftp.server.VFS;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class SearchHandler
implements Runnable {
    Properties status = new Properties();
    Vector listing = null;
    SessionCrush thisSession = null;
    String the_dir = null;
    Properties added_hash = new Properties();
    int depth = 20;
    public static Properties keywords_cache = new Properties();

    public SearchHandler(SessionCrush thisSession, Vector listing, String the_dir, int depth) {
        this.thisSession = thisSession;
        this.listing = listing;
        this.the_dir = the_dir;
        this.depth = depth;
        this.status.put("done", "false");
    }

    public void recurseAddCache(String cpath, int depth_level) {
        block7: {
            Vector dirCache;
            block6: {
                if (depth_level-- == 0) {
                    return;
                }
                dirCache = (Vector)FileClient.dirCachePerm.get(String.valueOf(cpath) + "/");
                if (Log.log("SEARCH", 2, "")) {
                    Log.log("SEARCH", 2, "Search " + cpath + " size=" + (dirCache == null ? "null" : String.valueOf(dirCache.size())));
                }
                if (dirCache != null) break block6;
                Properties dir_item = (Properties)FileClient.dirCachePerm.get(cpath);
                if (dir_item == null) break block7;
                if (Log.log("SEARCH", 2, "")) {
                    Log.log("SEARCH", 2, "Search " + cpath + " size=" + dir_item.size());
                }
                dir_item = (Properties)dir_item.clone();
                dir_item.put("db", "true");
                if (this.added_hash.containsKey(dir_item.getProperty("url"))) break block7;
                this.listing.addElement(dir_item);
                SearchHandler.getKeywords(dir_item.getProperty("url"));
                this.added_hash.put(dir_item.getProperty("url"), "");
                break block7;
            }
            Properties dir_item = (Properties)FileClient.dirCachePerm.get(cpath);
            if (dir_item != null) {
                dir_item = (Properties)dir_item.clone();
                dir_item.put("db", "true");
                if (!this.added_hash.containsKey(dir_item.getProperty("url"))) {
                    this.listing.addElement(dir_item);
                    SearchHandler.getKeywords(dir_item.getProperty("url"));
                    this.added_hash.put(dir_item.getProperty("url"), "");
                }
            }
            int x = 0;
            while (x < dirCache.size()) {
                this.recurseAddCache(dirCache.elementAt(x).toString(), depth_level);
                ++x;
            }
        }
    }

    @Override
    public void run() {
        try {
            if (FileClient.memCache) {
                Properties lookupItem = this.thisSession.uVFS.get_item(this.the_dir);
                VRL vrl = new VRL(lookupItem.getProperty("url"));
                Vector root_items = new Vector();
                if (vrl.getProtocol().equalsIgnoreCase("virtual")) {
                    this.thisSession.uVFS.getListing(root_items, this.the_dir);
                } else {
                    GenericClient c = this.thisSession.uVFS.getClient(lookupItem);
                    c.list(new VRL(lookupItem.getProperty("url")).getPath(), root_items);
                    c.close();
                    this.thisSession.uVFS.releaseClient(c);
                }
                int x = 0;
                while (x < root_items.size()) {
                    lookupItem = (Properties)root_items.elementAt(x);
                    vrl = new VRL(lookupItem.getProperty("url"));
                    if (vrl.getProtocol().equalsIgnoreCase("virtual") && new VRL((lookupItem = this.thisSession.uVFS.get_item(vrl.getPath())).getProperty("url")).getProtocol().equalsIgnoreCase("file")) {
                        GenericClient c = this.thisSession.uVFS.getClient(lookupItem);
                        Vector root_items2 = new Vector();
                        if (c instanceof GenericClientMulti) {
                            GenericClientMulti gcm = (GenericClientMulti)c;
                            int xx = 0;
                            while (xx < gcm.clients.size()) {
                                GenericClient c2 = (GenericClient)gcm.clients.elementAt(xx);
                                Vector v = new Vector();
                                c2.list(new VRL("" + c2.getConfig("url")).getPath(), v);
                                root_items2.addAll(v);
                                ++xx;
                            }
                        } else {
                            c.list(new VRL(lookupItem.getProperty("url")).getPath(), root_items2);
                        }
                        c.close();
                        this.thisSession.uVFS.releaseClient(c);
                        root_items.addAll(root_items2);
                    }
                    if (vrl.getProtocol().equalsIgnoreCase("file")) {
                        SearchHandler.getKeywords(vrl.toString());
                        String cpath = new File_U(vrl.getPath()).getCanonicalPath().replace('\\', '/');
                        this.recurseAddCache(cpath, this.depth);
                    }
                    ++x;
                }
                Log.log("SEARCH", 0, "Listing results size for search:" + this.listing.size());
            } else if (ServerStatus.SG("search_index_usernames").equals("")) {
                this.thisSession.uVFS.getListing(this.listing, this.the_dir, this.depth, 1000, true);
            } else {
                Properties lookupItem = this.thisSession.uVFS.get_item(this.thisSession.uiSG("current_dir"));
                try {
                    Vector v = ServerStatus.thisObj.searchTools.executeSqlQuery(ServerStatus.SG("search_db_query"), new Object[]{String.valueOf(new VRL(lookupItem.getProperty("url")).getPath()) + "%"}, false, false);
                    int x = 0;
                    while (x < v.size()) {
                        Properties pp = (Properties)v.elementAt(x);
                        if (pp.containsKey("ITEM_MODIFIED")) {
                            pp.put("root_dir", pp.remove("ITEM_PATH"));
                            pp.put("url", new VRL(pp.getProperty("root_dir", "")).toString());
                            String item_name = crushftp.handlers.Common.last(pp.getProperty("root_dir"));
                            if (item_name.endsWith("/")) {
                                item_name = item_name.substring(0, item_name.length() - 1);
                            }
                            pp.put("name", item_name);
                            pp.put("type", pp.remove("ITEM_TYPE"));
                            pp.put("size", pp.remove("ITEM_SIZE"));
                            pp.put("modified", pp.remove("ITEM_MODIFIED"));
                            pp.put("keywords", pp.remove("ITEM_KEYWORDS"));
                            pp.put("db", "true");
                            this.listing.addElement(pp);
                        }
                        ++x;
                    }
                    Log.log("SEARCH", 0, "Listing results size for search:" + this.listing.size());
                }
                catch (Throwable t) {
                    Log.log("SEARCH", 0, t);
                }
            }
        }
        catch (Exception e) {
            Log.log("SEARCH", 0, e);
        }
        this.added_hash.clear();
        this.status.put("done", "true");
    }

    public static void buildEntry(final Properties pp, final VFS uVFS, String action, Properties dest_item) {
        if (!FileClient.memCache && ServerStatus.SG("search_index_usernames").equals("")) {
            return;
        }
        if (FileClient.memCache && ServerStatus.SG("search_index_usernames").equals("") && !new VRL(pp.getProperty("url")).getProtocol().equalsIgnoreCase("file")) {
            return;
        }
        try {
            if (action.equals("rename")) {
                Vector v = ServerStatus.thisObj.searchTools.executeSqlQuery(ServerStatus.SG("search_db_query"), new Object[]{String.valueOf(new VRL(pp.getProperty("url")).getPath()) + "%"}, false, false);
                int x = 0;
                while (x < v.size()) {
                    Properties ppp = (Properties)v.elementAt(x);
                    ServerStatus.thisObj.searchTools.executeSql(ServerStatus.SG("search_db_delete"), new Object[]{new VRL(ppp.getProperty("url")).getPath()});
                    Object[] values = new Object[]{String.valueOf(new VRL(dest_item.getProperty("url")).getPath()) + new VRL(ppp.getProperty("url")).getPath().substring(new VRL(pp.getProperty("url")).getPath().length()), ppp.getProperty("ITEM_TYPE", "DIR"), ppp.getProperty("ITEM_SIZE", "0"), ppp.getProperty("ITEM_MODIFIED", "0"), ppp.getProperty("ITEM_KEYWORDS", "")};
                    ServerStatus.thisObj.searchTools.executeSql(ServerStatus.SG("search_db_insert"), values);
                    ++x;
                }
                return;
            }
        }
        catch (Throwable t) {
            Log.log("SEARCH", 0, t);
        }
        try {
            if (!FileClient.memCache && action.equals("delete")) {
                ServerStatus.thisObj.searchTools.executeSql(ServerStatus.SG("search_db_delete"), new Object[]{String.valueOf(new VRL(pp.getProperty("url")).getPath()) + "%"});
            }
        }
        catch (Throwable t) {
            Log.log("SEARCH", 0, t);
        }
        if (action.equals("delete")) {
            return;
        }
        final Vector<Properties> listing = new Vector<Properties>();
        final StringBuffer status = new StringBuffer();
        if (pp.getProperty("type", "").equalsIgnoreCase("DIR")) {
            try {
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            uVFS.getListing(listing, pp.getProperty("root_dir"), 20, 50000, true);
                        }
                        catch (Exception e) {
                            listing.addElement(pp);
                        }
                        status.append("done");
                    }
                }, String.valueOf(Thread.currentThread().getName()) + ":lister:");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
                status.append("done");
            }
        } else {
            listing.addElement(pp);
            status.append("done");
        }
        boolean tika = new File_S(String.valueOf(System.getProperty("crushftp.search")) + "tika-app.jar").exists();
        while (listing.size() > 0 || status.length() == 0) {
            if (listing.size() > 0) {
                Properties ppp = (Properties)listing.remove(0);
                ServerStatus.thisObj.server_info.put("memcache_objects", String.valueOf(FileClient.dirCachePerm.size()));
                if (FileClient.memCache) {
                    SearchHandler.getKeywords(ppp.getProperty("url"));
                    continue;
                }
                try {
                    if (uVFS.thisSession != null && !LIST_handler.checkName(ppp, uVFS.thisSession, false, false)) {
                        continue;
                    }
                }
                catch (Throwable e) {
                    Log.log("SEARCH", 1, e);
                }
                try {
                    String contents = "";
                    VRL vrl = new VRL(ppp.getProperty("url"));
                    Thread.currentThread().setName("SEARCH:Scanning " + vrl.safe() + "...");
                    if (tika && ppp.getProperty("type", "FILE").equalsIgnoreCase("FILE")) {
                        Vector v = ServerStatus.thisObj.searchTools.executeSqlQuery(ServerStatus.SG("search_db_query"), new Object[]{new VRL(ppp.getProperty("url")).getPath()}, false, false);
                        if (v.size() == 1 && ((Properties)v.elementAt(0)).getProperty("ITEM_MODIFIED", "0").equals(ppp.getProperty("modified", "-1"))) continue;
                        long start = System.currentTimeMillis();
                        contents = new String(SearchHandler.getContents(vrl, ServerStatus.IG("search_max_content_kb")), "UTF8");
                        Log.log("SEARCH", 0, "Generated " + contents.length() + " bytes of text from Tika results in " + (System.currentTimeMillis() - start) + "ms:" + vrl.safe());
                    }
                    Object[] values = new Object[4];
                    values[0] = ppp.getProperty("size");
                    values[1] = ppp.getProperty("modified", "0");
                    String keywords = SearchHandler.getKeywords(ppp.getProperty("url"));
                    if (keywords.length() > 1600) {
                        keywords = keywords.substring(0, 1600);
                    }
                    values[2] = String.valueOf(keywords) + "\r\n" + contents;
                    values[3] = new VRL(ppp.getProperty("url")).getPath();
                    if (ServerStatus.thisObj.searchTools.executeSql(ServerStatus.SG("search_db_update"), values) > 0) continue;
                    values = new Object[]{new VRL(ppp.getProperty("url")).getPath(), ppp.getProperty("type"), ppp.getProperty("size"), ppp.getProperty("modified", "0"), String.valueOf(keywords) + "\r\n" + contents};
                    Log.log("SEARCH", 0, "No existing item found, doing insert instead:" + vrl.safe());
                    ServerStatus.thisObj.searchTools.executeSql(ServerStatus.SG("search_db_insert"), values);
                    Log.log("SEARCH", 0, "Insert completed:" + vrl.safe());
                }
                catch (Throwable t) {
                    Log.log("SEARCH", 0, t);
                }
                continue;
            }
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
    }

    public static byte[] getContents(VRL vrl, final int max_kb) throws Exception {
        Vector log = new Vector();
        GenericClient c = crushftp.handlers.Common.getClient(crushftp.handlers.Common.getBaseUrl(vrl.toString()), "SEARCH", log);
        c.login(vrl.getUsername(), vrl.getPassword(), null);
        final File_U f = new File_U(String.valueOf(ServerStatus.SG("previews_path")) + "Preview/tmp/" + crushftp.handlers.Common.makeBoundary(3) + "_" + vrl.getName());
        new File_U(String.valueOf(ServerStatus.SG("previews_path")) + "Preview/tmp/").mkdirs();
        Common.streamCopier(null, null, c.download(vrl.getPath(), 0L, -1L, true), new FileOutputStream(f), false, true, true);
        Log.log("SEARCH", 0, "Copying file down to temp folder:" + vrl.getPath() + "-->" + f + ":Done");
        final Properties status = new Properties();
        try {
            Common.check_exec();
            final Process proc = Runtime.getRuntime().exec((String.valueOf(System.getProperty("java.home")) + File_S.separator + "bin" + File_S.separator + "java" + (crushftp.handlers.Common.machine_is_windows() ? ".exe" : "") + ";-jar;tika-app.jar;-t;" + f.getCanonicalPath()).split(";"), null, (File)new File_S(System.getProperty("crushftp.search")));
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            Thread t = new Thread(new Runnable(){

                @Override
                public void run() {
                    try {
                        long start = System.currentTimeMillis();
                        Thread.currentThread().setName("SEARCH:TIKA:" + f + " " + (System.currentTimeMillis() - start) + "ms");
                        InputStream in = proc.getInputStream();
                        InputStream in2 = proc.getErrorStream();
                        byte[] b = new byte[1024];
                        int bytes_read = 0;
                        while (bytes_read >= 0) {
                            Thread.currentThread().setName("SEARCH:TIKA:" + f + " " + (System.currentTimeMillis() - start) + "ms");
                            bytes_read = in.read(b);
                            if (baos.size() > max_kb * 1024) break;
                            if (bytes_read < 0) continue;
                            baos.write(b, 0, bytes_read);
                        }
                        if (in2.available() > 0) {
                            Thread.currentThread().setName("SEARCH:TIKA:" + f + " " + (System.currentTimeMillis() - start) + "ms");
                            bytes_read = 0;
                            while (bytes_read > 0) {
                                bytes_read = in2.read(b);
                                if (bytes_read < 0) continue;
                                baos2.write(b, 0, bytes_read);
                            }
                            String error = new String(baos2.toByteArray()).trim();
                            if (error.length() > 0) {
                                Log.log("SEARCH", 0, "TIKA_ERROR:" + error);
                            }
                        }
                        in2.close();
                        in.close();
                        status.put("status", "");
                    }
                    catch (IOException e) {
                        Log.log("SEARCH", 0, e);
                        status.put("status", "" + e);
                    }
                }
            });
            t.start();
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 300000L) {
                if (status.containsKey("status")) break;
            }
            if (!status.containsKey("status")) {
                Log.log("SEARCH", 0, "Timeout of 5 minutes waiting for tika:" + f);
                try {
                    t.interrupt();
                }
                catch (Exception exception) {}
            } else if (!status.getProperty("status", "").equals("")) {
                Log.log("SEARCH", 0, "Error " + status.getProperty("status") + " for tika:" + f);
            }
            try {
                proc.destroy();
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (crushftp.handlers.Common.machine_is_windows()) {
                String s = new String(baos.toByteArray(), ServerStatus.SG("windows_character_encoding_process"));
                baos.reset();
                baos.write(s.getBytes("UTF8"));
            }
            byte[] byArray = baos.toByteArray();
            return byArray;
        }
        finally {
            f.delete();
            c.logout();
        }
    }

    public boolean isActive() {
        return this.status.getProperty("done", "false").equalsIgnoreCase("false");
    }

    public static Properties findItem(Properties pp, VFS uVFS, Vector items, String root_dir) throws Exception {
        String pp_canonical = null;
        pp_canonical = FileClient.memCache ? new File_U(new VRL(pp.getProperty("url")).getPath()).getCanonicalPath().replace('\\', '/') : (new File_U(pp.getProperty("root_dir")).exists() ? new File_U(pp.getProperty("root_dir")).getCanonicalPath().replace('\\', '/') : pp.getProperty("root_dir").replace('\\', '/'));
        int x = 0;
        while (x < uVFS.homes.size()) {
            Properties virtual = (Properties)uVFS.homes.elementAt(x);
            Enumeration<Object> keys = virtual.keys();
            while (keys.hasMoreElements()) {
                Properties home;
                String key = keys.nextElement().toString();
                if (key.equals("vfs_permissions_object") || !(home = (Properties)virtual.get(key)).containsKey("vItems")) continue;
                Vector vItems = (Vector)home.get("vItems");
                int xx = 0;
                while (xx < vItems.size()) {
                    Properties vitem = (Properties)vItems.elementAt(xx);
                    VRL vrl = new VRL(vitem.getProperty("url"));
                    String home_canonical = new File_U(vrl.getPath()).getCanonicalPath().replace('\\', '/');
                    if (pp_canonical.startsWith(home_canonical)) {
                        Properties ppp = uVFS.get_item(String.valueOf(key) + pp_canonical.substring(home_canonical.length()));
                        return ppp;
                    }
                    ++xx;
                }
            }
            ++x;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getKeywords(String url) {
        if (!ServerStatus.BG("search_keywords_also")) {
            return "";
        }
        String the_dir = SearchHandler.getPreviewPath(url, "1", 1);
        if (the_dir == null) {
            return "";
        }
        String index = String.valueOf(ServerStatus.SG("previews_path")) + the_dir.substring(1);
        StringBuffer resultData = new StringBuffer();
        if (FileClient.memCache && keywords_cache.containsKey(index)) {
            return keywords_cache.getProperty(index);
        }
        if (new File_U(String.valueOf(crushftp.handlers.Common.all_but_last(index)) + "../index.txt").exists()) {
            try {
                RandomAccessFile out = new RandomAccessFile(new File_U(String.valueOf(crushftp.handlers.Common.all_but_last(index)) + "../index.txt"), "r");
                byte[] b = new byte[(int)out.length()];
                out.readFully(b);
                out.close();
                resultData.append(new String(b, "UTF8"));
            }
            catch (Exception e) {
                Log.log("PREVIEW", 1, e);
            }
        }
        if (new File_U(String.valueOf(crushftp.handlers.Common.all_but_last(index)) + "../info.xml").exists()) {
            Properties xml = (Properties)crushftp.handlers.Common.readXMLObject_U(String.valueOf(crushftp.handlers.Common.all_but_last(index)) + "../info.xml");
            Enumeration<Object> keys = xml.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                resultData.append(" ").append(xml.getProperty(key));
            }
        }
        if (FileClient.memCache && ServerStatus.BG("search_keywords_also")) {
            Properties properties = keywords_cache;
            synchronized (properties) {
                String s = resultData.toString();
                long total_size = ServerStatus.siLG("keywords_cache_size") + (long)s.length() + (long)index.length() + 10L;
                keywords_cache.put(index, s);
                ServerStatus.siPUT("keywords_cache_size", String.valueOf(total_size));
            }
        }
        return resultData.toString();
    }

    public static String getPreviewPath(String url, String size, int frame) {
        if (frame < 1) {
            frame = 1;
        }
        String path = null;
        if (url != null) {
            VRL otherFile = new VRL(url);
            if (otherFile.getFile() == null) {
                return "/";
            }
            try {
                path = otherFile.getCanonicalPath();
            }
            catch (Exception e) {
                Log.log("PREVIEW", 1, e);
            }
            if (path.equalsIgnoreCase("/")) {
                return null;
            }
            try {
                path = String.valueOf(PreviewWorker.getDestPath2(url)) + otherFile.getName() + "/";
            }
            catch (Exception e) {
                path = "./";
            }
            while (!new File_U(String.valueOf(path) + "p" + frame).exists() && frame > 1) {
                --frame;
            }
            path = (size = Common.dots(size)).indexOf(".") >= 0 ? String.valueOf(path) + "p" + frame + "/" + size : String.valueOf(path) + "p" + frame + "/" + size + ".jpg";
            path = ServerStatus.SG("previews_path").length() - 1 < 0 ? null : path.substring(ServerStatus.SG("previews_path").length() - 1);
        }
        return path;
    }
}

