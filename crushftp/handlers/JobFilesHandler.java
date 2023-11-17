/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import crushftp.handlers.Log;
import crushftp.server.AdminControls;
import crushftp.server.ServerStatus;
import java.util.Properties;
import java.util.Vector;

public class JobFilesHandler {
    public static Properties job_writing_lock = new Properties();
    public static Object log_writing_lock_retriever = new Object();

    public static Object readXMLObject(File_S file) {
        Object o = Common.readXMLObject(file);
        return JobFilesHandler.readXMLObject(o, false);
    }

    public static Object readXMLObject(String path) {
        Object o = Common.readXMLObject(path);
        return JobFilesHandler.readXMLObject(o, false);
    }

    public static Object readXMLObject(Object o, boolean encypt) {
        if (encypt) {
            if (ServerStatus.BG("encrypt_job_files_sensitive_data") && o instanceof Properties) {
                JobFilesHandler.encryptDecryptData((Properties)o, encypt);
            }
            return o;
        }
        if (o instanceof Properties) {
            JobFilesHandler.encryptDecryptData((Properties)o, encypt);
        }
        return o;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean writeXMLObject(String path, Object obj, String root) {
        String uid = Common.makeBoundary(6);
        try {
            Object lock = null;
            Object object = log_writing_lock_retriever;
            synchronized (object) {
                lock = job_writing_lock.get(path);
                if (lock == null) {
                    lock = new Object();
                    job_writing_lock.put(path, lock);
                }
            }
            object = lock;
            synchronized (object) {
                if (ServerStatus.BG("encrypt_job_files_sensitive_data")) {
                    JobFilesHandler.encryptDecryptData((Properties)obj, true);
                }
                Common.writeXMLObject(String.valueOf(path) + ".tmp_" + uid + "_new", obj, root);
                new File_S(String.valueOf(path) + ".tmp_" + uid + "_old").delete();
                new File_S(path).renameTo(new File_S(String.valueOf(path) + ".tmp_" + uid + "_old"));
                new File_S(String.valueOf(path) + ".tmp_" + uid + "_new").renameTo(new File_S(path));
                new File_S(String.valueOf(path) + ".tmp_" + uid + "_old").delete();
                try {
                    JobFilesHandler.addToCache(path, obj);
                }
                catch (Exception ee) {
                    Log.log("SERVER", 0, ee);
                }
            }
            return true;
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
            ServerStatus.expired_log_cleanup();
            try {
                Common.writeXMLObject(String.valueOf(path) + ".tmp_" + uid + "_new", obj, root);
                new File_S(String.valueOf(path) + ".tmp_" + uid + "_old").delete();
                new File_S(path).renameTo(new File_S(String.valueOf(path) + ".tmp_" + uid + "_old"));
                new File_S(String.valueOf(path) + ".tmp_" + uid + "_new").renameTo(new File_S(path));
                new File_S(String.valueOf(path) + ".tmp_" + uid + "_old").delete();
                try {
                    JobFilesHandler.addToCache(path, obj);
                }
                catch (Exception ee) {
                    Log.log("SERVER", 0, ee);
                }
                return true;
            }
            catch (Exception ee) {
                Log.log("SERVER", 0, ee);
                return false;
            }
        }
    }

    public static Properties addToCache(String path, Object obj) {
        String job_name = new File_S(path).getName();
        if (!(job_name.equalsIgnoreCase("job.XML") || job_name.equalsIgnoreCase("inprogress.XML") || job_name.equalsIgnoreCase("inprogress") || job_name.endsWith("_new.XML") || !job_name.toUpperCase().endsWith(".XML"))) {
            if (new File_S(path).length() > 0x100000L * ServerStatus.LG("max_job_xml_size")) {
                return null;
            }
            if ((job_name = job_name.substring(0, job_name.lastIndexOf(".XML"))).indexOf("_") > 0) {
                String job_id = job_name.split("_")[0];
                Properties summaryJob = JobFilesHandler.makeSummaryJob(new File_S(path), obj, job_id);
                AdminControls.jobs_summary_cache.put(new File_S(path).getPath(), summaryJob);
                return summaryJob;
            }
        }
        return null;
    }

    public static Properties makeSummaryJob(File_S job_file, Object obj, String job_id) {
        Properties tracker = null;
        tracker = obj != null ? (Properties)obj : (Properties)JobFilesHandler.readXMLObject(job_file.getPath());
        Properties settings = (Properties)tracker.get("settings");
        Properties summaryJob = new Properties();
        summaryJob.put("name", "");
        summaryJob.put("start", tracker.getProperty("start", ""));
        summaryJob.put("end", tracker.getProperty("end", "0"));
        summaryJob.put("id", job_id);
        summaryJob.put("log_file", tracker.getProperty("log_file", ""));
        summaryJob.put("stop", tracker.getProperty("stop", ""));
        summaryJob.put("status", tracker.getProperty("status", ""));
        if (settings == null) {
            settings = new Properties();
        }
        Properties set2 = new Properties();
        set2.put("pluginName", settings.getProperty("pluginName", ""));
        set2.put("subItem", settings.getProperty("subItem", ""));
        set2.put("scheduleName", settings.getProperty("scheduleName", ""));
        set2.put("name", settings.getProperty("name", ""));
        set2.put("id", settings.getProperty("id", ""));
        set2.put("allowed_usernames", settings.getProperty("allowed_usernames", ""));
        summaryJob.put("settings", set2);
        summaryJob.put("modified", String.valueOf(job_file.lastModified()));
        return summaryJob;
    }

    private static void encryptDecryptData(Properties data, boolean encrypt) {
        Properties settings = null;
        settings = data.get("settings") != null && data.get("settings") instanceof Properties ? (Properties)data.get("settings") : data;
        if (settings != null && settings.get("tasks") != null && settings.get("tasks") instanceof Vector) {
            Vector tasks = (Vector)settings.get("tasks");
            Vector<Properties> new_tasks = new Vector<Properties>();
            int x = 0;
            while (x < tasks.size()) {
                Properties task = (Properties)tasks.get(x);
                try {
                    JobFilesHandler.encryptDecryptRemoteLocations(task, encrypt);
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
                new_tasks.add(task);
                ++x;
            }
            settings.put("tasks", new_tasks);
        }
        if (data.get("active_items") != null && data.get("active_items") instanceof Vector) {
            Vector active_items = (Vector)data.get("active_items");
            int x = 0;
            while (x < active_items.size()) {
                if (active_items.get(x) != null && active_items.get(x) instanceof Properties) {
                    Properties active_item = (Properties)active_items.get(x);
                    if (active_item.get("newItems") != null && active_item.get("newItems") instanceof Vector) {
                        Vector newItems = (Vector)active_item.get("newItems");
                        int xx = 0;
                        while (xx < newItems.size()) {
                            if (newItems.get(xx) != null && newItems.get(xx) instanceof Properties) {
                                JobFilesHandler.encryptDecryptRemoteLocations((Properties)newItems.get(xx), encrypt);
                            }
                            ++xx;
                        }
                    }
                    if (active_item.get("items") != null && active_item.get("items") instanceof Vector) {
                        Vector items = (Vector)active_item.get("items");
                        int xx = 0;
                        while (xx < items.size()) {
                            if (items.get(xx) != null && items.get(xx) instanceof Properties) {
                                JobFilesHandler.encryptDecryptRemoteLocations((Properties)items.get(xx), encrypt);
                            }
                            ++xx;
                        }
                    }
                }
                ++x;
            }
        }
    }

    public static Object encryptDecryptRemoteLocations(Properties p, boolean encrypt) {
        try {
            String[] keys = "cms_key_storage,filePath,destPath,findUrl,destUrl,key_path,private_key_path,mail_path,url,kafka_custom_config_data,jms_config_data,jms_message_store_url".split(",");
            int xx = 0;
            while (xx < keys.length) {
                String key = keys[xx];
                if (p.containsKey(key)) {
                    if (encrypt) {
                        if (!(p.getProperty(key).toUpperCase().startsWith("FILE://") && p.getProperty(key).toUpperCase().startsWith("{") && p.getProperty(key).toUpperCase().startsWith("ENCRYPT:"))) {
                            p.put(key, "ENCRYPT:" + Common.encryptDecrypt(p.getProperty(key), encrypt));
                        }
                    } else if (p.getProperty(key).toUpperCase().startsWith("ENCRYPT:") && !encrypt) {
                        String value = Common.encryptDecrypt(p.getProperty(key).substring("ENCRYPT:".length()), encrypt);
                        p.put(key, value);
                    }
                }
                ++xx;
            }
        }
        catch (Exception e) {
            Common.log("SERVER", 1, e);
        }
        return p;
    }
}

