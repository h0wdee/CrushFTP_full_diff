/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.tomp2p.natpmp.Gateway
 *  net.tomp2p.natpmp.MapRequestMessage
 *  net.tomp2p.natpmp.Message
 *  net.tomp2p.natpmp.NatPmpDevice
 *  net.tomp2p.upnp.InternetGatewayDevice
 */
package crushftp.handlers;

import com.crushftp.client.Common;
import crushftp.handlers.Log;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;
import net.tomp2p.natpmp.Gateway;
import net.tomp2p.natpmp.MapRequestMessage;
import net.tomp2p.natpmp.Message;
import net.tomp2p.natpmp.NatPmpDevice;
import net.tomp2p.upnp.InternetGatewayDevice;

public class PortMapper
implements Runnable {
    NatPmpDevice pmp = null;
    Properties gateways = new Properties();
    boolean added = false;
    boolean skip_pmp = false;
    boolean skip_upnp = false;

    @Override
    public void run() {
        this.clearAll();
    }

    public synchronized boolean mapPort(String ip, int port, int secs) throws Exception {
        if (!this.added) {
            Runtime.getRuntime().addShutdownHook(new Thread(this));
            this.added = true;
        }
        if (ip == null || ip.equals("lookup") || ip.equals("auto") || ip.equals("0.0.0.0")) {
            ip = Common.getLocalIP();
        }
        boolean ok = false;
        if (!this.skip_pmp) {
            if (this.pmp == null) {
                this.pmp = new NatPmpDevice(Gateway.getIP());
            }
            MapRequestMessage tcp = new MapRequestMessage(true, port, port, secs, null);
            this.pmp.enqueueMessage((Message)tcp);
            int x = 0;
            while (x < 25 && !ok) {
                Thread.sleep(100L);
                if (("" + tcp.getResultCode()).equalsIgnoreCase("Success")) {
                    ok = true;
                }
                ++x;
            }
            if (ok) {
                Log.log("SERVER", 0, "Mapped external port using PMP:" + port);
            } else {
                this.skip_pmp = true;
            }
        }
        if (!this.skip_upnp) {
            Object[] IGDs = new Object[]{};
            Collection col = InternetGatewayDevice.getDevices((int)1500);
            if (col != null) {
                IGDs = col.toArray();
            }
            int x = 0;
            while (x < IGDs.length) {
                InternetGatewayDevice igd = (InternetGatewayDevice)IGDs[x];
                try {
                    if (igd.addPortMapping("CrushFTP", "TCP", ip, port, "TCP", port, secs)) {
                        this.gateways.put(String.valueOf(port), igd);
                        ok = true;
                        Log.log("SERVER", 0, "Mapped external port using UPNP:" + port);
                    } else {
                        this.skip_upnp = true;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.log("SERVER", 1, e);
                }
                ++x;
            }
        }
        return ok;
    }

    public synchronized void clearAll() {
        if (this.pmp != null) {
            this.pmp.shutdown();
        }
        this.pmp = null;
        Enumeration<Object> keys = this.gateways.keys();
        while (keys.hasMoreElements()) {
            String port = keys.nextElement().toString();
            InternetGatewayDevice igd = (InternetGatewayDevice)this.gateways.remove(port);
            try {
                igd.deletePortMapping(null, Integer.parseInt(port), "TCP");
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}

