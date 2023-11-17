/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel;

import com.crushftp.client.Common;
import com.crushftp.tunnel2.Tunnel2;
import com.crushftp.tunnel3.StreamController;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Properties;

public class AutoChannelProxy {
    public static Object oneActiveTunnel = new Object();
    public static boolean v3 = false;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    public static Object enableAppletTunnel(final Properties controller, final boolean onlyOne, final StringBuffer CrushAuth) throws Exception {
        block4: {
            controller.remove("stopTunnel");
            u = new URL(controller.getProperty("URL"));
            tunnel = AutoChannelProxy.getTunnel(u, CrushAuth);
            if (tunnel == null || tunnel.size() <= 0) break block4;
            var5_5 = AutoChannelProxy.oneActiveTunnel;
            synchronized (var5_5) {
                controller.put("tunnelInitialized", "false");
                System.out.println("Starting tunnel:" + tunnel);
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        Thread.currentThread().setName("Applet Tunnel Thread:" + tunnel);
                        try {
                            if (tunnel.getProperty("tunnel_version", "").equalsIgnoreCase("Tunnel3")) {
                                v3 = true;
                            }
                            tunnel.put("url", controller.getProperty("URL"));
                            controller.put("URL", "http://127.0.0.1:" + tunnel.getProperty("localPort") + "/");
                            String url = (String.valueOf(tunnel.getProperty("url")) + "#").substring(0, (String.valueOf(tunnel.getProperty("url")) + "#").indexOf("#"));
                            if (v3) {
                                StreamController t = new StreamController(url, tunnel.getProperty("username", ""), tunnel.getProperty("password", ""), null);
                                controller.put("tunnelObj", t);
                                t.setTunnel(tunnel);
                                t.setAuth(CrushAuth.toString());
                                t.startThreads();
                                controller.put("tunnelInitialized", "true");
                                Properties statusInfo = (Properties)controller.get("statusInfo");
                                while (!controller.containsKey("stopTunnel")) {
                                    Thread.sleep(100L);
                                    statusInfo.put("tunnelInfo", " (Out=" + t.outgoing.size() + ", In=" + t.incoming.size() + ")");
                                }
                                t.startStopTunnel(false);
                            } else {
                                Tunnel2 t = new Tunnel2(url, tunnel.getProperty("username", ""), tunnel.getProperty("password", ""), onlyOne);
                                controller.put("tunnelObj", t);
                                t.setTunnel(tunnel);
                                t.setAuth(CrushAuth.toString());
                                t.startThreads();
                                controller.put("tunnelInitialized", "true");
                                Properties statusInfo = (Properties)controller.get("statusInfo");
                                while (!controller.containsKey("stopTunnel")) {
                                    Thread.sleep(100L);
                                    statusInfo.put("tunnelInfo", " (Out=" + t.getSends() + ", In=" + t.getGets() + ")");
                                }
                                t.setActive(false);
                                t.waitForShutdown();
                                while (!t.isShutdown()) {
                                    Thread.sleep(100L);
                                }
                            }
                        }
                        catch (Exception e) {
                            System.out.println("Unable to load tunnels.");
                            e.printStackTrace();
                            controller.put("tunnelInitialized", "true");
                        }
                        controller.remove("stopTunnel");
                    }
                }).start();
                // MONITOREXIT @DISABLED, blocks:[0, 1] lbl13 : MonitorExitStatement: MONITOREXIT : var5_5
                if (true) ** GOTO lbl19
            }
            do {
                Thread.sleep(100L);
lbl19:
                // 2 sources

            } while (!controller.getProperty("tunnelInitialized", "false").equals("true"));
        }
        return controller.remove("tunnelObj");
    }

    public static Properties getTunnel(URL u, StringBuffer CrushAuth) throws IOException {
        HttpURLConnection urlc = (HttpURLConnection)u.openConnection();
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Cookie", "CrushAuth=" + CrushAuth.toString() + ";");
        urlc.setUseCaches(false);
        urlc.setDoOutput(true);
        urlc.getOutputStream().write(("c2f=" + CrushAuth.toString().substring(CrushAuth.toString().length() - 4) + "&command=getTunnels").getBytes("UTF8"));
        urlc.getResponseCode();
        InputStream in = urlc.getInputStream();
        String data = "";
        int bytesRead = 0;
        byte[] b = new byte[32768];
        while (bytesRead >= 0) {
            bytesRead = in.read(b);
            if (bytesRead <= 0) continue;
            data = String.valueOf(data) + new String(b, 0, bytesRead, "UTF8");
        }
        in.close();
        urlc.disconnect();
        Properties tunnel = null;
        if (data.indexOf("<response>") > 0) {
            data = data.substring(data.indexOf("<response>") + "<response>".length(), data.indexOf("</response"));
            String[] tunnelsStr = Common.url_decode(data.replace('~', '%')).split(";;;");
            int x = 0;
            while (x < tunnelsStr.length) {
                tunnel = new Properties();
                try {
                    tunnel.load(new ByteArrayInputStream(tunnelsStr[x].getBytes("UTF8")));
                    if (tunnel.getProperty("localPort", "0").equals(System.getProperty("crushtunnel.magicport", "55555"))) {
                        ServerSocket ss = new ServerSocket(0);
                        tunnel.put("localPort", String.valueOf(ss.getLocalPort()));
                        ss.close();
                        break;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                ++x;
            }
        }
        return tunnel;
    }
}

