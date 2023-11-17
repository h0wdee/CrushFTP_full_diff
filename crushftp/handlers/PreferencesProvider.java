/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.File_S;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.server.ServerStatus;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class PreferencesProvider {
    public long getPrefsTime(String instance) {
        if (com.crushftp.client.Common.dmz_mode) {
            return 0L;
        }
        if (instance == null || instance.equals("")) {
            instance = "";
        } else if (!instance.startsWith("_")) {
            instance = "_" + instance;
        }
        return new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs" + instance + ".XML").lastModified();
    }

    public Properties loadPrefs(String instance) {
        if (com.crushftp.client.Common.dmz_mode) {
            return ServerStatus.server_settings;
        }
        if (instance == null || instance.equals("")) {
            instance = "";
        } else if (!instance.startsWith("_")) {
            instance = "_" + instance;
        }
        Properties prefs_temp = (Properties)Common.readXMLObject(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs" + instance + ".XML");
        boolean updated = this.updateSettingsWithSeparateFiles(instance, prefs_temp);
        Vector pref_server_items = (Vector)prefs_temp.get("server_list");
        int x = 0;
        while (x < pref_server_items.size()) {
            Properties the_server = (Properties)pref_server_items.elementAt(x);
            updated |= this.updateSettingsWithSeparateFiles("server_list" + instance, the_server);
            if (!instance.equals("")) {
                updated |= this.updateSettingsWithSeparateFiles(String.valueOf(instance) + "_server_list", the_server);
            }
            ++x;
        }
        if (updated) {
            this.savePrefs(prefs_temp, instance);
        }
        return prefs_temp;
    }

    public boolean updateSettingsWithSeparateFiles(String prefix, Properties p) {
        if (System.getProperty("crushftp.split_prefs", "true").equals("false")) {
            return false;
        }
        boolean updated = false;
        Enumeration<Object> keys = p.keys();
        while (keys.hasMoreElements()) {
            String val_path;
            String key = keys.nextElement().toString();
            if (p.get(key) instanceof String) {
                val_path = null;
                if (new File(String.valueOf(System.getProperty("crushftp.prefs")) + "split_prefs/" + prefix + "_" + Common.dots(key) + ".TXT").exists()) {
                    val_path = String.valueOf(System.getProperty("crushftp.prefs")) + "split_prefs/" + prefix + "_" + Common.dots(key) + ".TXT";
                }
                if (new File(String.valueOf(System.getProperty("crushftp.prefs")) + "split_prefs/" + prefix + "_" + Common.dots(key) + ".txt").exists()) {
                    val_path = String.valueOf(System.getProperty("crushftp.prefs")) + "split_prefs/" + prefix + "_" + Common.dots(key) + ".txt";
                }
                if (val_path == null) continue;
                updated = true;
                try {
                    String val = Common.getFileText(val_path).trim();
                    p.put(key, val);
                    Log.log("SERVER", 0, "SPLIT_PREFS:Using " + key + " value from " + val_path + " with value " + val);
                }
                catch (IOException e) {
                    Log.log("SERVER", 0, e);
                }
                continue;
            }
            val_path = null;
            if (new File(String.valueOf(System.getProperty("crushftp.prefs")) + "split_prefs/" + prefix + "_" + Common.dots(key) + ".XML").exists()) {
                val_path = String.valueOf(System.getProperty("crushftp.prefs")) + "split_prefs/" + prefix + "_" + Common.dots(key) + ".XML";
            }
            if (new File(String.valueOf(System.getProperty("crushftp.prefs")) + "split_prefs/" + prefix + "_" + Common.dots(key) + ".xml").exists()) {
                val_path = String.valueOf(System.getProperty("crushftp.prefs")) + "split_prefs/" + prefix + "_" + Common.dots(key) + ".xml";
            }
            if (val_path == null) continue;
            updated = true;
            try {
                Object val_temp = Common.readXMLObject(val_path);
                if (val_temp != null) {
                    p.put(key, val_temp);
                }
                Log.log("SERVER", 0, "SPLIT_PREFS:Using " + key + " value from " + val_path + " with XML value.");
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        }
        return updated;
    }

    public Properties getBackupPrefs(String instance) {
        if (com.crushftp.client.Common.dmz_mode) {
            return (Properties)ServerStatus.thisObj.default_settings.clone();
        }
        if (instance == null || instance.equals("")) {
            instance = "";
        } else if (!instance.startsWith("_")) {
            instance = "_" + instance;
        }
        Properties newPrefs = null;
        int index = 0;
        index = 0;
        while (index < 100) {
            if (new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/prefs" + index + ".XML").exists() && new File_S(String.valueOf(System.getProperty("crushftp.backup")) + "backup/prefs" + index + ".XML").length() != 0L) {
                try {
                    Log.log("SERVER", 0, "Trying prefs.xml backup file:prefs" + index + ".XML");
                    newPrefs = (Properties)Common.readXMLObject(String.valueOf(System.getProperty("crushftp.backup")) + "backup/prefs" + index + ".XML");
                    if (newPrefs != null) break;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            ++index;
        }
        if (index >= 98) {
            Log.log("SERVER", 0, "prefs.XML backup files were missing or corrupt.  Using defaults instead...");
            newPrefs = (Properties)ServerStatus.thisObj.default_settings.clone();
        }
        return newPrefs;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void savePrefs(Properties server_settings, String instance) {
        if (com.crushftp.client.Common.dmz_mode) {
            return;
        }
        if (instance == null || instance.equals("")) {
            instance = "";
        } else if (!instance.startsWith("_")) {
            instance = "_" + instance;
        }
        Properties properties = Common.xmlCache;
        synchronized (properties) {
            Properties properties2 = server_settings;
            synchronized (properties2) {
                try {
                    Vector<Properties> add_vec = new Vector<Properties>();
                    add_vec.addElement(server_settings);
                    try {
                        if (!new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs" + instance + ".XML").exists()) {
                            RandomAccessFile makeIt = new RandomAccessFile(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs" + instance + ".XML"), "rw");
                            makeIt.close();
                            ServerStatus.thisObj.server_info.put("currentFileDate" + instance, String.valueOf(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs" + instance + ".XML").lastModified()));
                        }
                        if (new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs" + instance + ".XML").lastModified() == ServerStatus.siLG("currentFileDate" + instance)) {
                            Common.write_server_settings(server_settings, instance);
                            ServerStatus.thisObj.server_info.put("currentFileDate" + instance, String.valueOf(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs" + instance + ".XML").lastModified()));
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 0, "Prefs.XML failed to be written1...");
                        Log.log("SERVER", 0, e);
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 0, "Prefs.XML failed to be written2...");
                    Log.log("SERVER", 0, e);
                }
            }
        }
    }

    public boolean check_code() {
        String name = ServerStatus.SG("registration_name");
        String email = ServerStatus.SG("registration_email");
        String code = ServerStatus.SG("registration_code");
        boolean ok = ServerStatus.thisObj.common_code.register(name, email, code);
        if (ok) {
            String v = ServerStatus.thisObj.common_code.getRegistrationAccess("V", ServerStatus.SG("registration_code"));
            if (v != null && (v.equals("4") || v.equals("5") || v.equals("6") || v.equals("7") || v.equals("8") || v.equals("9"))) {
                String msg = String.valueOf(System.getProperty("appname", "CrushFTP")) + " " + ServerStatus.version_info_str + " will not work with a " + System.getProperty("appname", "CrushFTP") + " " + v + " license.";
                Log.log("SERVER", 0, msg);
                ServerStatus.put_in("max_max_users", "5");
                ServerStatus.put_in("max_users", "5");
                return false;
            }
            ServerStatus.put_in("max_max_users", ServerStatus.thisObj.common_code.getRegistrationAccess("MAX", code));
            String e_level = ServerStatus.thisObj.common_code.getRegistrationAccess("E", ServerStatus.SG("registration_code"));
            if (e_level == null) {
                e_level = "0";
            }
            ServerStatus.thisObj.server_info.put("enterprise_level", e_level);
            ServerStatus.thisObj.server_info.put("registration_name", Common.url_decode(name));
            ServerStatus.thisObj.server_info.put("registration_email", email);
            return true;
        }
        ServerStatus.put_in("max_max_users", "5");
        ServerStatus.put_in("max_users", "5");
        return false;
    }
}

