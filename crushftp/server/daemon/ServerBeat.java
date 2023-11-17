/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server.daemon;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.Worker;
import crushftp.handlers.Log;
import crushftp.server.ServerStatus;
import crushftp.server.daemon.GenericServer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Properties;
import java.util.Vector;

public class ServerBeat
extends GenericServer {
    public static boolean current_master = true;
    Exception error = null;
    public String vip = "";
    public String member_ips = "";
    public String index1 = "1";
    public int port = 0;
    public String adapter = "";
    public String localIp = "";
    public String netmask = "255.255.255.0";
    boolean master = false;
    long born_on = 0L;
    long last_age_check = 0L;
    long born_on_min = 0L;
    String master_lan_ip = "";
    boolean need_remaster = false;
    static long start_millis = System.currentTimeMillis();
    long state_change_time = System.currentTimeMillis();
    boolean starting = true;

    public ServerBeat(Properties server_item) {
        super(server_item);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void updateStatus() {
        Object object = updateServerStatuses;
        synchronized (object) {
            this.updateStatusInit();
            if (this.socket_created) {
                this.server_item.put("display", "ServerBeat://" + this.vip + ":" + this.port + "/ is running. (" + this.member_ips + ") Master=" + this.master + " MasterIP=" + this.master_lan_ip);
            } else {
                this.server_item.put("display", "ServerBeat://" + this.vip + ":" + this.port + "/ is stopped.  (" + this.member_ips + ") Master=" + this.master + " MasterIP=" + this.master_lan_ip);
            }
        }
    }

    public static void main(String[] args) {
        boolean enabled = true;
        System.out.println("ServerBeat DeadMan's Switch: Validating session.obj timestamp to ensure its not more than 30 seconds old.");
        crushftp.handlers.Common.initSystemProperties(true);
        Common.log = new Vector();
        while (true) {
            try {
                while (true) {
                    if (System.currentTimeMillis() - new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "sessions.obj").lastModified() > (long)((args.length > 0 ? Integer.parseInt(args[0]) : 10) * 1000) && enabled) {
                        Properties server_prefs = (Properties)crushftp.handlers.Common.readXMLObject(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs.XML");
                        Vector server_list = (Vector)server_prefs.get("server_list");
                        int x = 0;
                        while (x < server_list.size()) {
                            Properties server_item = (Properties)server_list.elementAt(x);
                            if (server_item.getProperty("serverType", "").equalsIgnoreCase("SERVERBEAT")) {
                                ServerBeat.disableMaster(server_item.getProperty("vip"), server_item.getProperty("index1", "1"), true, server_item.getProperty("adapter"), server_item.getProperty("netmask", "255.255.255.0"), server_prefs.getProperty("serverbeat_command", "ifconfig"), server_prefs.getProperty("serverbeat_unplumb", "false").equals("true"), server_prefs.getProperty("serverbeat_ifdown_command", "ifdown"));
                            }
                            ++x;
                        }
                        enabled = false;
                    } else if (System.currentTimeMillis() - new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "sessions.obj").lastModified() < 5000L && !enabled) {
                        enabled = true;
                    }
                    Thread.sleep(1000L);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            break;
        }
    }

    @Override
    public void run() {
        this.starting = true;
        this.init();
        try {
            if (ServerStatus.siIG("enterprise_level") <= 0) {
                this.busyMessage = "ServerBeat only valid for Enterprise licenses.";
                throw new Exception(this.busyMessage);
            }
            this.getSocket();
            this.startBeat();
            this.member_ips = this.server_item.getProperty("vip2");
            this.last_age_check = System.currentTimeMillis();
            this.born_on = System.currentTimeMillis();
            this.born_on_min = 0L;
            this.init(this.server_item.getProperty("vip"), this.server_item.getProperty("index1", "1"), Integer.parseInt(this.server_item.getProperty("port")), this.server_item.getProperty("netmask", "255.255.255.0"), this.server_item.getProperty("adapter"));
            final ServerBeat sb = this;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    while (ServerBeat.this.die_now.length() == 0) {
                        new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "sessions.obj").setLastModified(System.currentTimeMillis());
                        try {
                            int priority = Integer.parseInt(ServerBeat.this.server_item.getProperty("priority", "1"));
                            ServerBeat.this.server_sock.setSoTimeout(1000);
                            Socket pong = ServerBeat.this.server_sock.accept();
                            pong.setSoTimeout(2000);
                            BufferedReader br = new BufferedReader(new InputStreamReader(pong.getInputStream()));
                            String msg = br.readLine();
                            Log.log("SERVER_BEAT", 3, "Serverbeat pong:" + msg + ":" + pong);
                            pong.getOutputStream().write((String.valueOf(ServerBeat.this.master) + ":" + ServerBeat.this.born_on + ":" + ServerBeat.this.listen_ip + ":" + ServerBeat.this.need_remaster + ":" + priority + "\r\n").getBytes());
                            br.close();
                            pong.close();
                            ServerBeat.this.master_receive_logic(msg, priority);
                        }
                        catch (SocketTimeoutException priority) {
                        }
                        catch (Exception e) {
                            Log.log("SERVER_BEAT", 2, e);
                        }
                        if (System.currentTimeMillis() - ServerBeat.this.last_age_check <= 5000L) continue;
                        ServerBeat.this.starting = false;
                        ServerBeat.this.need_remaster = false;
                        try {
                            if (ServerBeat.this.born_on == ServerBeat.this.born_on_min && !ServerBeat.this.master) {
                                Log.log("SERVER_BEAT", 0, "We are the oldest, but not master, starting master sequence now.");
                                ServerBeat.this.master = ServerBeat.disableMaster(ServerBeat.this.vip, ServerBeat.this.index1, ServerBeat.this.master, ServerBeat.this.adapter, ServerBeat.this.netmask, ServerStatus.SG("serverbeat_command"), ServerStatus.BG("serverbeat_unplumb"), ServerStatus.SG("serverbeat_ifdown_command"));
                                ServerBeat.this.changeStateAlert(true);
                                sb.becomeMaster(ServerBeat.this.vip, ServerBeat.this.index1);
                                ServerBeat.this.master_lan_ip = ServerBeat.this.listen_ip;
                                ServerBeat.this.updateStatus();
                            } else if (ServerBeat.this.born_on != ServerBeat.this.born_on_min && ServerBeat.this.master) {
                                Log.log("SERVER_BEAT", 0, "We are not the oldest, but we are master, releasing master status now.");
                                ServerBeat.this.born_on = System.currentTimeMillis() - start_millis;
                                ServerBeat.this.changeStateAlert(false);
                                ServerBeat.this.master = ServerBeat.disableMaster(ServerBeat.this.vip, ServerBeat.this.index1, ServerBeat.this.master, ServerBeat.this.adapter, ServerBeat.this.netmask, ServerStatus.SG("serverbeat_command"), ServerStatus.BG("serverbeat_unplumb"), ServerStatus.SG("serverbeat_ifdown_command"));
                                ServerBeat.this.need_remaster = true;
                                ServerBeat.this.updateStatus();
                            }
                        }
                        catch (Exception e) {
                            Log.log("SERVER_BEAT", 1, e);
                        }
                        ServerBeat.this.born_on_min = ServerBeat.this.born_on;
                        ServerBeat.this.last_age_check = System.currentTimeMillis();
                    }
                }
            }, "Serverbeat listener:" + this.server_sock.getLocalPort());
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    while (ServerBeat.this.die_now.length() == 0) {
                        int x = 0;
                        while (x < ServerBeat.this.member_ips.split(",").length) {
                            String member_ip = ServerBeat.this.member_ips.split(",")[x].trim();
                            try {
                                int priority = Integer.parseInt(ServerBeat.this.server_item.getProperty("priority", "1"));
                                Socket pinger = new Socket();
                                pinger.setSoTimeout(1000);
                                pinger.connect(new InetSocketAddress(member_ip, ServerBeat.this.port));
                                pinger.getOutputStream().write((String.valueOf(ServerBeat.this.master) + ":" + ServerBeat.this.born_on + ":" + ServerBeat.this.listen_ip + ":" + ServerBeat.this.need_remaster + ":" + priority + "\r\n").getBytes());
                                BufferedReader br = new BufferedReader(new InputStreamReader(pinger.getInputStream()));
                                String msg = br.readLine();
                                Log.log("SERVER_BEAT", 3, "Serverbeat ping:" + msg + ":" + pinger);
                                br.close();
                                pinger.close();
                                ServerBeat.this.master_sent_logic(msg, priority);
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            ++x;
                        }
                        try {
                            Thread.sleep(1000L);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
            }, "Serverbeat pinger:" + this.server_sock.getLocalPort());
            while (this.socket_created && this.die_now.length() == 0) {
                Thread.sleep(1000L);
                if (this.error != null) {
                    throw this.error;
                }
                this.updateStatus();
            }
        }
        catch (Exception e) {
            if (e.getMessage() == null || e.getMessage().indexOf("socket closed") < 0) {
                Log.log("SERVER_BEAT", 1, e);
            }
            Log.log("SERVER_BEAT", 3, e);
        }
        this.socket_created = false;
        this.kill();
        this.updateStatus();
        if (this.restart) {
            this.restart = false;
            this.die_now = new StringBuffer();
            new Thread(this).start();
        }
    }

    private void master_receive_logic(String msg, int priority) throws Exception {
        boolean master_opposite = msg.split(":")[0].equals("true");
        long born_on_opposite = Long.parseLong(msg.split(":")[1]);
        if (born_on_opposite < this.born_on_min || this.born_on_min == 0L) {
            this.born_on_min = born_on_opposite;
        }
        if (master_opposite) {
            this.master_lan_ip = msg.split(":")[2];
        }
        boolean need_remaster_opposite = msg.split(":")[3].equals("true");
        int priority_opposite = Integer.parseInt(msg.split(":")[4].trim());
        if (priority_opposite > priority && this.master && !this.starting) {
            Log.log("SERVER_BEAT", 0, "We are the master, but we are not the highest priority, making us young, and releasing master.");
            this.born_on = System.currentTimeMillis() - start_millis;
            this.changeStateAlert(false);
            this.master = ServerBeat.disableMaster(this.vip, this.index1, this.master, this.adapter, this.netmask, ServerStatus.SG("serverbeat_command"), ServerStatus.BG("serverbeat_unplumb"), ServerStatus.SG("serverbeat_ifdown_command"));
            this.updateStatus();
            this.need_remaster = true;
        } else if (need_remaster_opposite && this.master && !this.starting) {
            Log.log("SERVER_BEAT", 0, "Elected master.");
            this.changeStateAlert(true);
            this.becomeMaster(this.vip, this.index1);
            this.master_lan_ip = this.listen_ip;
            this.updateStatus();
        } else if (priority_opposite > priority && this.born_on == this.born_on_min && !this.starting) {
            Log.log("SERVER_BEAT", 0, "We are the oldest, but we are not the highest priority, making us young now.");
            this.born_on = System.currentTimeMillis() - start_millis;
            this.changeStateAlert(false);
            this.master = ServerBeat.disableMaster(this.vip, this.index1, this.master, this.adapter, this.netmask, ServerStatus.SG("serverbeat_command"), ServerStatus.BG("serverbeat_unplumb"), ServerStatus.SG("serverbeat_ifdown_command"));
            this.updateStatus();
            this.need_remaster = true;
        } else if (!this.master && priority > priority_opposite && !this.starting) {
            Log.log("SERVER_BEAT", 0, "We are not the oldest, and we are not master, but we have priority.  Becoming master.");
            this.changeStateAlert(true);
            this.becomeMaster(this.vip, this.index1);
            this.master_lan_ip = this.listen_ip;
            this.born_on = this.born_on_min;
            this.updateStatus();
        }
    }

    private void master_sent_logic(String msg, int priority) throws Exception {
        boolean master_opposite = msg.split(":")[0].equals("true");
        long born_on_opposite = Long.parseLong(msg.split(":")[1]);
        boolean need_remaster_opposite = msg.split(":")[3].equals("true");
        int priority_opposite = Integer.parseInt(msg.split(":")[4].trim());
        if (master_opposite && this.master && priority_opposite == priority && this.born_on != this.born_on_min && !this.starting) {
            Log.log("SERVER_BEAT", 0, "Opposite says its master, and so do we, and we have the same priority...new election being held now.");
            this.born_on = System.currentTimeMillis() - start_millis;
            this.changeStateAlert(false);
            this.master = ServerBeat.disableMaster(this.vip, this.index1, this.master, this.adapter, this.netmask, ServerStatus.SG("serverbeat_command"), ServerStatus.BG("serverbeat_unplumb"), ServerStatus.SG("serverbeat_ifdown_command"));
            this.updateStatus();
            this.need_remaster = true;
        } else if (need_remaster_opposite && this.master && !this.starting) {
            Log.log("SERVER_BEAT", 0, "Elected master, and already master.");
            this.changeStateAlert(true);
            this.becomeMaster(this.vip, this.index1);
            this.master_lan_ip = this.listen_ip;
            this.updateStatus();
        } else if (priority_opposite > priority && this.master && !this.starting) {
            Log.log("SERVER_BEAT", 0, "Opposite has higher priority but we are master...giving up master status now.");
            this.born_on = System.currentTimeMillis() - start_millis;
            this.changeStateAlert(false);
            this.master = ServerBeat.disableMaster(this.vip, this.index1, this.master, this.adapter, this.netmask, ServerStatus.SG("serverbeat_command"), ServerStatus.BG("serverbeat_unplumb"), ServerStatus.SG("serverbeat_ifdown_command"));
            this.updateStatus();
            this.need_remaster = true;
        }
    }

    public void init(String vip, String index1, int port, String netmask, String adapter) {
        this.vip = vip;
        this.index1 = index1;
        this.port = port;
        this.netmask = netmask;
        this.adapter = adapter;
        try {
            this.changeStateAlert(false);
            this.master = ServerBeat.disableMaster(vip, index1, this.master, adapter, netmask, ServerStatus.SG("serverbeat_command"), ServerStatus.BG("serverbeat_unplumb"), ServerStatus.SG("serverbeat_ifdown_command"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.localIp = Common.getLocalIP();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void startBeat() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){

            @Override
            public void run() {
                try {
                    Thread.sleep(1000L);
                    Log.log("SERVER_BEAT", 1, "Serverbeat:releasing " + ServerBeat.this.vip + "...");
                    ServerBeat.this.changeStateAlert(false);
                    ServerBeat.this.master = ServerBeat.disableMaster(ServerBeat.this.vip, ServerBeat.this.index1, ServerBeat.this.master, ServerBeat.this.adapter, ServerBeat.this.netmask, ServerStatus.SG("serverbeat_command"), ServerStatus.BG("serverbeat_unplumb"), ServerStatus.SG("serverbeat_ifdown_command"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                Log.log("SERVER_BEAT", 1, "Serverbeat:released " + ServerBeat.this.vip + ".");
            }
        }));
    }

    public void kill() {
        try {
            this.changeStateAlert(false);
            this.master = ServerBeat.disableMaster(this.vip, this.index1, this.master, this.adapter, this.netmask, ServerStatus.SG("serverbeat_command"), ServerStatus.BG("serverbeat_unplumb"), ServerStatus.SG("serverbeat_ifdown_command"));
        }
        catch (Exception e) {
            Log.log("SERVER_BEAT", 1, e);
        }
    }

    public void becomeMaster(String ip, String index) throws Exception {
        String ifconfig = ServerStatus.SG("serverbeat_command");
        if (ifconfig.equals("netsh") && !crushftp.handlers.Common.machine_is_windows()) {
            ifconfig = "ifconfig";
        }
        if (ifconfig.equals("ifconfig") && crushftp.handlers.Common.machine_is_windows()) {
            ifconfig = "netsh";
        }
        current_master = this.master = true;
        Thread.sleep(2000L);
        Log.log("SERVER_BEAT", 0, "ServerBeat: becoming Master..." + ip);
        if (crushftp.handlers.Common.machine_is_windows()) {
            ServerBeat.exec(new String[]{"cmd", "/C", String.valueOf(ifconfig) + " interface ip add address name=\"" + this.adapter + "\" addr=" + ip + " mask=" + this.netmask}, this.vip);
        } else if (crushftp.handlers.Common.machine_is_solaris() || crushftp.handlers.Common.machine_is_linux()) {
            ServerBeat.exec((String.valueOf(ServerStatus.SG("serverbeat_ifup_command")) + " " + this.adapter + ":" + index).split(" "), this.vip);
            if (ServerStatus.BG("serverbeat_plumb")) {
                ServerBeat.exec((String.valueOf(ifconfig) + " " + this.adapter + ":" + index + " plumb").split(" "), this.vip);
            }
            ServerBeat.exec((String.valueOf(ifconfig) + " " + this.adapter + ":" + index + " " + ip + " netmask " + this.netmask + " up").split(" "), this.vip);
        } else {
            ServerBeat.exec((String.valueOf(ifconfig) + " " + this.adapter + " alias " + ip + " netmask " + this.netmask).split(" "), this.vip);
        }
        current_master = this.master = true;
        String command = ServerStatus.SG("serverbeat_post_command");
        if (!command.equals("")) {
            command = crushftp.handlers.Common.replace_str(command, "{vip}", ip);
            command = crushftp.handlers.Common.replace_str(command, "{adapter}", this.adapter);
            ServerBeat.exec(command.split(" "), this.vip);
        }
    }

    public void changeStateAlert(boolean becoming) {
        if (this.master != becoming && System.currentTimeMillis() - this.state_change_time > 30000L) {
            try {
                Properties info = new Properties();
                info.put("alert_type", "Master change.  Current:" + this.master + " New:" + becoming);
                info.put("alert_sub_type", "Server IP:" + this.listen_ip);
                info.put("alert_timeout", "0");
                info.put("alert_max", "0");
                info.put("alert_msg", this.server_item.getProperty("display"));
                ServerStatus.thisObj.runAlerts("serverbeat_alert", info, null, null);
            }
            catch (Exception ee) {
                Log.log("BAN", 1, ee);
            }
        }
        this.state_change_time = System.currentTimeMillis();
    }

    public static boolean disableMaster(String vip, String index, boolean master, String adapter, String netmask, String ifconfig, boolean unplumb, String ifdown) throws Exception {
        if (ifconfig.equals("netsh") && !crushftp.handlers.Common.machine_is_windows()) {
            ifconfig = "ifconfig";
        }
        if (ifconfig.equals("ifconfig") && crushftp.handlers.Common.machine_is_windows()) {
            ifconfig = "netsh";
        }
        current_master = master = false;
        Log.log("SERVER_BEAT", 0, "ServerBeat: disabling Master..." + vip);
        if (crushftp.handlers.Common.machine_is_windows()) {
            ServerBeat.exec(new String[]{"cmd", "/C", String.valueOf(ifconfig) + " interface ip delete address name=\"" + adapter + "\" addr=" + vip}, vip);
        } else if (crushftp.handlers.Common.machine_is_solaris() || crushftp.handlers.Common.machine_is_linux()) {
            ServerBeat.exec((String.valueOf(ifconfig) + " " + adapter + ":" + index + " " + vip + " netmask " + netmask + " down").split(" "), vip);
            if (unplumb) {
                ServerBeat.exec((String.valueOf(ifconfig) + " " + adapter + ":" + index + " unplumb").split(" "), vip);
            }
            ServerBeat.exec((String.valueOf(ifdown) + " " + adapter + ":" + index).split(" "), vip);
        } else {
            ServerBeat.exec((String.valueOf(ifconfig) + " " + adapter + " -alias " + vip + " netmask " + netmask).split(" "), vip);
        }
        current_master = master = false;
        return master;
    }

    public static String exec(String[] c, String vip) throws Exception {
        if (vip.toUpperCase().indexOf("JOB") >= 0) {
            return "";
        }
        Common.check_exec();
        String s = "";
        int x = 0;
        while (x < c.length) {
            s = String.valueOf(s) + c[x] + " ";
            ++x;
        }
        Log.log("SERVER_BEAT", 0, "ServerBeat exec: " + s.trim());
        Process proc = Runtime.getRuntime().exec(c);
        BufferedReader br1 = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        String result = "";
        String lastLine = "";
        while ((result = br1.readLine()) != null) {
            Log.log("SERVER_BEAT", 0, "ServerBeat: " + result);
            lastLine = result;
        }
        proc.waitFor();
        try {
            proc.destroy();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return lastLine;
    }
}

