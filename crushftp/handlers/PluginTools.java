/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.File_U;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.server.AdminControls;
import java.util.Properties;
import java.util.Vector;

public class PluginTools {
    public static void makeFolder(String path, Properties con_settings, Properties clientCache) {
        if (new VRL(path).getProtocol().toUpperCase().equals("FILE")) {
            Log.log("SERVER", 2, "Making folder at local URL:" + new VRL(path).safe());
            Properties request = new Properties();
            request.put("name", new File_U(new VRL(path).getPath()).getName());
            request.put("path", Common.all_but_last(new VRL(path).getPath()));
            AdminControls.newFolder(request, "(CONNECT)", true);
        } else {
            VRL folder_vrl = new VRL(path);
            Log.log("SERVER", 2, "Making folder at URL:" + folder_vrl.safe() + " path2=" + path);
            GenericClient c_folder = null;
            if (!clientCache.containsKey(Common.getBaseUrl(path))) {
                c_folder = com.crushftp.client.Common.getClient(Common.getBaseUrl(folder_vrl.toString()), "CrushFTP", new Vector());
                c_folder.setConfigObj((Properties)com.crushftp.client.Common.CLONE(con_settings));
                try {
                    c_folder.login(folder_vrl.getUsername(), folder_vrl.getPassword(), "");
                }
                catch (Exception e) {
                    Log.log("SERVER", 1, e);
                }
                clientCache.put(Common.getBaseUrl(path), c_folder);
            } else {
                c_folder = (GenericClient)clientCache.get(Common.getBaseUrl(path));
            }
            try {
                c_folder.makedirs(folder_vrl.getPath());
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
            }
        }
    }

    public static boolean home_exists(String path, Properties con_settings, Properties clientCache) {
        if (new VRL(path).getProtocol().toUpperCase().equals("FILE")) {
            return new File_U(new VRL(path).getPath()).exists();
        }
        VRL folder_vrl = new VRL(path);
        Log.log("SERVER", 2, "Check folder at URL:" + folder_vrl.safe() + " path2=" + path);
        GenericClient c_folder = null;
        if (!clientCache.containsKey(Common.getBaseUrl(path))) {
            c_folder = com.crushftp.client.Common.getClient(Common.getBaseUrl(folder_vrl.toString()), "CrushFTP", new Vector());
            c_folder.setConfigObj((Properties)com.crushftp.client.Common.CLONE(con_settings));
            try {
                c_folder.login(folder_vrl.getUsername(), folder_vrl.getPassword(), "");
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
                return false;
            }
            clientCache.put(Common.getBaseUrl(path), c_folder);
        } else {
            c_folder = (GenericClient)clientCache.get(Common.getBaseUrl(path));
        }
        try {
            Properties p = c_folder.stat(folder_vrl.getPath());
            return p != null;
        }
        catch (Exception e) {
            Log.log("SERVER", 1, e);
            return false;
        }
    }
}

