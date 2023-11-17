/*
 * Decompiled with CFR 0.152.
 */
package com.crushtunnel.gui;

import com.crushftp.client.Common;
import com.crushftp.client.Worker;
import com.crushftp.tunnel2.Tunnel2;
import com.crushftp.tunnel3.StreamController;
import com.crushtunnel.gui.GUIConfigurable;
import com.crushtunnel.gui.GUINormal;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class GUI
extends JPanel {
    private static final long serialVersionUID = 1L;
    public static GUI thisObj = null;
    Vector tunnels = new Vector();
    Thread tunnelThread = null;
    Tunnel2 tunnel = null;
    private JTabbedPane mainTabs = new JTabbedPane();
    JPanel tunnelsPanel = new JPanel();
    private JScrollPane tunnelsScrollPane = new JScrollPane();
    private BorderLayout borderLayout1 = new BorderLayout();
    int preferredHeight = 0;
    String url = null;
    boolean v3 = false;
    public static int default_local_port = 55555;

    public GUI() {
        thisObj = this;
        try {
            String data;
            this.jbInit();
            if (System.getProperty("crushtunnel.remote.protocol", "").equals("") && (data = JOptionPane.showInputDialog("Enter the protocol (HTTP/HTTPS):", (Object)"HTTPS")) != null) {
                System.getProperties().put("crushtunnel.remote.protocol", data.trim());
                data = null;
                data = JOptionPane.showInputDialog("Enter the " + System.getProperty("appname", "CrushFTP") + " server host/ip:", (Object)"www.domain.com");
                if (data != null) {
                    System.getProperties().put("crushtunnel.remote.ip", data.trim());
                    data = null;
                    data = JOptionPane.showInputDialog("Enter the " + System.getProperty("appname", "CrushFTP") + " server port:", (Object)"443");
                    if (data != null) {
                        System.getProperties().put("crushtunnel.remote.port", data.trim());
                    }
                }
            }
            if (!System.getProperty("crushtunnel.remote.protocol", "").equals("")) {
                this.url = String.valueOf(System.getProperty("crushtunnel.remote.protocol", "")) + "://" + System.getProperty("crushtunnel.remote.ip", "") + ":" + System.getProperty("crushtunnel.remote.port", "") + "/";
                System.getProperties().put("crushtunnel.remote.url", this.url);
                StringBuffer auth = new StringBuffer();
                if (!System.getProperty("crushtunnel.remote.crushauth", "").equals("")) {
                    auth.append(System.getProperty("crushtunnel.remote.crushauth", ""));
                }
                Common.trustEverything();
                URL u = new URL(this.url);
                HttpURLConnection urlc = null;
                InputStream in = null;
                int bytesRead = 0;
                byte[] b = new byte[32768];
                String result = "";
                if (result.indexOf("<response>success</response>") < 0) {
                    String user = System.getProperty("crushtunnel.remote.user");
                    if (user == null) {
                        user = GUI.getUser();
                    }
                    System.getProperties().put("crushtunnel.remote.user", user);
                    String pass = System.getProperty("crushtunnel.remote.pass");
                    if (pass == null) {
                        pass = Common.getPasswordPrompt("Enter Password:");
                    }
                    if (user == null || pass == null) {
                        System.exit(0);
                    }
                    System.getProperties().put("crushtunnel.remote.pass", pass);
                    auth.setLength(0);
                    String msg = this.doLogin(auth);
                    if (!msg.equals("")) {
                        JOptionPane.showMessageDialog(null, msg, "Login Failed", 0);
                        System.exit(0);
                    }
                }
                urlc = (HttpURLConnection)u.openConnection();
                urlc.setRequestMethod("POST");
                urlc.setRequestProperty("Cookie", "CrushAuth=" + auth + ";");
                urlc.setUseCaches(false);
                urlc.setDoOutput(true);
                urlc.getOutputStream().write(("c2f=" + auth.toString().substring(auth.toString().length() - 4)).getBytes("UTF8"));
                urlc.getOutputStream().write("&command=getTunnels".getBytes("UTF8"));
                urlc.getResponseCode();
                in = urlc.getInputStream();
                String data2 = "";
                bytesRead = 0;
                while (bytesRead >= 0) {
                    bytesRead = in.read(b);
                    if (bytesRead <= 0) continue;
                    data2 = String.valueOf(data2) + new String(b, 0, bytesRead, "UTF8");
                }
                in.close();
                urlc.disconnect();
                if (data2.indexOf("<response>") > 0) {
                    data2 = data2.substring(data2.indexOf("<response>") + "<response>".length(), data2.indexOf("</response"));
                    String[] tunnelsStr = Common.url_decode(data2.replace('~', '%')).split(";;;");
                    int x = 0;
                    while (x < tunnelsStr.length) {
                        Properties p = new Properties();
                        try {
                            p.load(new ByteArrayInputStream(tunnelsStr[x].getBytes("UTF8")));
                            if (p.getProperty("tunnelType", "").equals("HTTP")) {
                                if (p.getProperty("configurable", "false").equals("true")) {
                                    this.addConfigurableTunnelItem(p);
                                } else {
                                    this.addTunnelItem(p);
                                }
                                if (p.getProperty("tunnel_version", "").equalsIgnoreCase("Tunnel3")) {
                                    this.v3 = true;
                                }
                                this.tunnels.addElement(p);
                                if (p.getProperty("run", "false").equals("true")) {
                                    this.startStopTunnel(p);
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            Common.log("TUNNEL", 1, e);
                        }
                        ++x;
                    }
                }
                this.tunnelsPanel.setPreferredSize(new Dimension(620, this.preferredHeight));
            }
            this.setVisible(true);
            if (this.v3) {
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        Thread.currentThread().setName("Tunnel Status Updater");
                        while (true) {
                            try {
                                int xx = 0;
                                while (xx < GUI.this.tunnels.size()) {
                                    Properties p = (Properties)GUI.this.tunnels.elementAt(xx);
                                    Properties gui = (Properties)p.get("gui");
                                    if (gui != null) {
                                        JLabel l = (JLabel)gui.get("infoLabel");
                                        StreamController sc = (StreamController)p.get("tunnel");
                                        if (sc == null) {
                                            l.setText("Not Connected");
                                        } else {
                                            l.setText("<html><body>" + GUI.getInfoLines(sc) + "</body></html>");
                                        }
                                    }
                                    ++xx;
                                }
                            }
                            catch (Exception e) {
                                Common.log("TUNNEL", 0, e);
                            }
                            try {
                                Thread.sleep(1000L);
                            }
                            catch (Exception exception) {
                                continue;
                            }
                            break;
                        }
                    }
                });
            } else {
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        Thread.currentThread().setName("Tunnel Status Updater");
                        while (true) {
                            try {
                                int x = 0;
                                while (x < GUI.this.tunnels.size()) {
                                    Properties p = (Properties)GUI.this.tunnels.elementAt(x);
                                    Properties gui = (Properties)p.get("gui");
                                    Tunnel2 t = (Tunnel2)p.get("tunnel");
                                    if (gui != null) {
                                        JLabel l = (JLabel)gui.get("infoLabel");
                                        if (t == null) {
                                            l.setText("Not Connected");
                                        } else {
                                            Vector<String> averageIn = (Vector<String>)gui.get("averageIn");
                                            if (averageIn == null) {
                                                averageIn = new Vector<String>();
                                            }
                                            gui.put("averageIn", averageIn);
                                            Vector<String> averageOut = (Vector<String>)gui.get("averageOut");
                                            if (averageOut == null) {
                                                averageOut = new Vector<String>();
                                            }
                                            gui.put("averageOut", averageOut);
                                            long nowBytesIn = t.getBytesIn();
                                            long nowBytesOut = t.getBytesOut();
                                            averageIn.addElement(String.valueOf(nowBytesIn));
                                            averageOut.addElement(String.valueOf(nowBytesOut));
                                            while (averageIn.size() > 3) {
                                                averageIn.remove(0);
                                            }
                                            while (averageOut.size() > 3) {
                                                averageOut.remove(0);
                                            }
                                            l.setText("(Tunnel is Active: Out=" + t.getSends() + ":" + Common.format_bytes_short(GUI.this.calcAverage(averageOut)) + "/sec, In=" + t.getGets() + ":" + Common.format_bytes_short(GUI.this.calcAverage(averageIn)) + "/sec)");
                                        }
                                    }
                                    ++x;
                                }
                            }
                            catch (Exception e) {
                                Common.log("TUNNEL", 0, e);
                            }
                            try {
                                Thread.sleep(1000L);
                            }
                            catch (Exception exception) {
                                continue;
                            }
                            break;
                        }
                    }
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to login to " + this.url + ".\r\n" + e.toString(), "Login Failed", 0);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getInfoLines(StreamController sc) {
        float baseSpeedOut = sc.st.getSpeedAverage(sc.st.speedHistory, "speedOut", "countOut", 3) * (float)sc.outgoing.size();
        float baseSpeedIn = sc.st.getSpeedAverage(sc.st.speedHistory, "speedIn", "countIn", 3) * (float)sc.incoming.size();
        String lines = "Tunnel2 is Active: Out=" + sc.outgoing.size() + ":" + Common.format_bytes_short((long)baseSpeedOut) + "/sec, In=" + sc.incoming.size() + ":" + Common.format_bytes_short((long)baseSpeedIn) + "/sec";
        Properties properties = sc.st.speedHistoryIp;
        synchronized (properties) {
            Enumeration<Object> keys = sc.st.speedHistoryIp.keys();
            while (keys.hasMoreElements()) {
                String bind_ip = keys.nextElement().toString();
                Vector speedHistory = (Vector)sc.st.speedHistoryIp.get(bind_ip);
                Properties last_item = (Properties)speedHistory.elementAt(speedHistory.size() - 1);
                baseSpeedOut = sc.st.getSpeedAverage(speedHistory, "speedOut", "countOut", 3) * (float)Integer.parseInt(last_item.getProperty("countOut", "0"));
                baseSpeedIn = sc.st.getSpeedAverage(speedHistory, "speedIn", "countIn", 3) * (float)Integer.parseInt(last_item.getProperty("countIn", "0"));
                lines = String.valueOf(lines) + "<br>" + bind_ip + ":Out=" + last_item.getProperty("countOut", "0") + ":" + Common.format_bytes_short((long)baseSpeedOut) + "/sec, In=" + last_item.getProperty("countIn", "0") + ":" + Common.format_bytes_short((long)baseSpeedIn) + "/sec)";
            }
        }
        return lines;
    }

    public long calcAverage(Vector v) {
        if (v.size() == 0) {
            return 0L;
        }
        long total = Long.parseLong("" + v.elementAt(v.size() - 1)) - Long.parseLong("" + v.elementAt(0));
        return total /= (long)v.size();
    }

    public String doLogin(StringBuffer sb) throws Exception {
        String ca = Common.login(this.url, System.getProperty("crushtunnel.remote.user"), System.getProperty("crushtunnel.remote.pass"), "CrushTunnel");
        if (ca == null) {
            return "Failed to login to " + this.url + ".";
        }
        sb.append(ca);
        return "";
    }

    public static String getUser() {
        return JOptionPane.showInputDialog("Enter your username:");
    }

    private void jbInit() throws Exception {
        this.setLayout(this.borderLayout1);
        this.tunnelsPanel.setPreferredSize(new Dimension(620, 10));
        this.tunnelsScrollPane.setMaximumSize(new Dimension(700, Short.MAX_VALUE));
        this.tunnelsScrollPane.getViewport().add((Component)this.tunnelsPanel, null);
        this.mainTabs.add((Component)this.tunnelsScrollPane, "Tunnels");
        this.add((Component)this.mainTabs, "Center");
    }

    public void addTunnelItem(Properties p) {
        GUINormal tunnelItemPanel = new GUINormal(p, this);
        this.tunnelsPanel.add((Component)tunnelItemPanel, null);
        this.preferredHeight += tunnelItemPanel.getPreferredSize().height + 10;
    }

    public void addConfigurableTunnelItem(Properties p) {
        GUIConfigurable tunnelItemPanel = new GUIConfigurable(p, this);
        tunnelItemPanel.setVisible(true);
        Properties guiItem = (Properties)p.get("gui");
        guiItem.put("tunnelItemPanel", tunnelItemPanel);
        tunnelItemPanel.urlText.setText(this.url);
        tunnelItemPanel.destIp.setText(p.getProperty("destIp"));
        tunnelItemPanel.bindIp.setText(p.getProperty("bindIp"));
        tunnelItemPanel.destPort.setText(p.getProperty("destPort"));
        tunnelItemPanel.localPort.setText(p.getProperty("localPort"));
        tunnelItemPanel.reverse.setSelected(p.getProperty("reverse").equals("true"));
        tunnelItemPanel.channelsInMax.setText(p.getProperty("channelsInMax"));
        tunnelItemPanel.channelsOutMax.setText(p.getProperty("channelsOutMax"));
        this.tunnelsPanel.add((Component)tunnelItemPanel, null);
        this.preferredHeight += tunnelItemPanel.getPreferredSize().height + 10;
    }

    public void startStopTunnel(Properties p) {
        if (p.get("tunnel") == null) {
            p.put("url", this.url);
            p.put("status", new Properties());
            Common.log("TUNNEL", 1, "Clicked connect button.");
            if (!p.getProperty("runBeforeConnect", "").equals("")) {
                try {
                    Common.exec(Common.getCommandAction(p.getProperty("runBeforeConnect"), p));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            try {
                StringBuffer auth;
                if (this.v3) {
                    StreamController sc = new StreamController(this.url, System.getProperty("crushtunnel.remote.user"), System.getProperty("crushtunnel.remote.pass"), null);
                    if (p.getProperty("configurable", "false").equals("true")) {
                        Properties guiItem = (Properties)p.get("gui");
                        GUIConfigurable tunnelItemPanel = (GUIConfigurable)guiItem.get("tunnelItemPanel");
                        p.put("bindIp", tunnelItemPanel.bindIp.getText());
                        p.put("localPort", tunnelItemPanel.localPort.getText());
                        p.put("destIp", tunnelItemPanel.destIp.getText());
                        p.put("destPort", tunnelItemPanel.destPort.getText());
                        p.put("channelsOutMax", tunnelItemPanel.channelsOutMax.getText());
                        p.put("channelsInMax", tunnelItemPanel.channelsInMax.getText());
                        p.put("reverse", String.valueOf(tunnelItemPanel.reverse.isSelected()));
                    }
                    sc.setTunnel(p);
                    auth = new StringBuffer();
                    this.doLogin(auth);
                    sc.setAuth(auth.toString());
                    Common.log("TUNNEL", 0, "Using auth:" + auth + " for tunnel:" + p);
                    p.put("tunnel", sc);
                    sc.startThreads();
                } else {
                    Tunnel2 t = new Tunnel2(this.url, System.getProperty("crushtunnel.remote.user"), System.getProperty("crushtunnel.remote.pass"), false);
                    t.setTunnel(p);
                    auth = new StringBuffer();
                    this.doLogin(auth);
                    t.setAuth(auth.toString());
                    Common.log("TUNNEL", 0, "Using auth:" + auth + " for tunnel:" + p);
                    p.put("tunnel", t);
                    t.startThreads();
                }
                default_local_port = Integer.parseInt(p.getProperty("localPort", "55555"));
            }
            catch (Exception e) {
                Common.log("TUNNEL", 0, e);
            }
            Properties guiItem = (Properties)p.get("gui");
            JLabel statusLabel = (JLabel)guiItem.get("statusLabel");
            try {
                statusLabel.setIcon(new ImageIcon(this.getClass().getResource("/assets/green.gif")));
            }
            catch (Exception tunnelItemPanel) {
                // empty catch block
            }
            JButton toggleTunnelButton = (JButton)guiItem.get("toggleTunnelButton");
            toggleTunnelButton.setText(p.getProperty("buttonDisconnect", ""));
            if (!p.getProperty("runAfterConnect", "").equals("")) {
                try {
                    Common.exec(Common.getCommandAction(p.getProperty("runAfterConnect"), p));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (System.getProperty("crushtunnel.launchbrowser", "").equalsIgnoreCase("true") || p.getProperty("name", "").toUpperCase().endsWith("_LAUNCH")) {
                try {
                    Desktop.getDesktop().browse(new URI("http://127.0.0.1:" + default_local_port + "/"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (!p.getProperty("runBeforeDisconnect", "").equals("")) {
                try {
                    Common.exec(Common.getCommandAction(p.getProperty("runBeforeDisconnect"), p));
                }
                catch (Exception guiItem) {
                    // empty catch block
                }
            }
            Properties guiItem = (Properties)p.get("gui");
            if (this.v3) {
                StreamController sc = (StreamController)p.get("tunnel");
                try {
                    sc.startStopTunnel(false);
                    p.remove("tunnel");
                    JLabel statusLabel = (JLabel)guiItem.get("statusLabel");
                    statusLabel.setIcon(new ImageIcon(this.getClass().getResource("/assets/red.gif")));
                }
                catch (Exception statusLabel) {}
            } else {
                Tunnel2 t = (Tunnel2)p.get("tunnel");
                t.stopThisTunnel();
                p.remove("tunnel");
                JLabel statusLabel = (JLabel)guiItem.get("statusLabel");
                try {
                    statusLabel.setIcon(new ImageIcon(this.getClass().getResource("/assets/red.gif")));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            JButton toggleTunnelButton = (JButton)guiItem.get("toggleTunnelButton");
            toggleTunnelButton.setText(p.getProperty("buttonConnect", ""));
            if (!p.getProperty("runAfterDisconnect", "").equals("")) {
                try {
                    Common.exec(Common.getCommandAction(p.getProperty("runAfterDisconnect"), p));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }
}

