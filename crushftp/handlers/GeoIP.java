/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.Common;
import crushftp.handlers.Log;
import crushftp.server.ServerStatus;
import java.util.Properties;

public class GeoIP {
    boolean enabled = false;
    public String server_lat = "0";
    public String server_lon = "0";

    public void init(String server_ip) {
        try {
            if (ServerStatus.SG("geoip_access_key").equals("")) {
                Log.log("SERVER", 0, "Skipping GEOIP lookups GEOIP accesskey not configured in Preferences, Misc.");
            } else {
                String data = Common.geo_ip_lookup(server_ip, 10000);
                this.server_lat = data.split(",")[3];
                this.server_lon = data.split(",")[4];
                this.enabled = true;
            }
        }
        catch (Throwable e) {
            System.out.println(e);
        }
    }

    public Properties getLoc(String ip) throws Exception {
        Properties loc = new Properties();
        loc.put("lat", "0");
        loc.put("lon", "0");
        loc.put("ip", ip);
        loc.put("t", String.valueOf(System.currentTimeMillis()));
        if (this.enabled && !ip.equals("127.0.0.1")) {
            String data = Common.geo_ip_lookup(ip, 10000);
            loc.put("lat", data.split(",")[3]);
            loc.put("lon", data.split(",")[4]);
        }
        return loc;
    }
}

