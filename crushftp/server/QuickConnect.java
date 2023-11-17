/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server;

import com.crushftp.client.Common;
import crushftp.server.ServerStatus;
import crushftp.server.daemon.GenericServer;
import java.net.Socket;
import java.util.Properties;
import java.util.Vector;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class QuickConnect
implements Runnable {
    public int listen_port = 21;
    Socket sock;
    GenericServer server = null;
    String listen_ip = "127.0.0.1";
    String listen_ip_port = "lookup_21";
    Properties server_item = null;
    public StringBuffer sni_keystore_used = null;
    public static transient Object syncUserNumbers = new Object();
    public static Properties ip_cache = new Properties();
    public static Properties connected_ips = new Properties();
    SSLSocketFactory factory = null;
    SSLContext ssl_context = null;
    String user_real_ip = null;

    public QuickConnect(GenericServer server, int listen_port, Socket sock, String listen_ip, String listen_ip_port, Properties server_item, SSLContext ssl_context, SSLSocketFactory factory, String user_real_ip) {
        this.listen_port = listen_port;
        this.sock = sock;
        this.server = server;
        this.listen_ip = listen_ip;
        this.listen_ip_port = listen_ip_port;
        this.server_item = server_item;
        this.ssl_context = ssl_context;
        this.factory = factory;
        this.user_real_ip = user_real_ip;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void remove_ip_count(String ip) {
        Properties properties = connected_ips;
        synchronized (properties) {
            int i = Integer.parseInt(connected_ips.getProperty(ip, "0")) - 1;
            if (i == 0) {
                connected_ips.remove(ip);
            } else {
                connected_ips.put(ip, String.valueOf(i));
            }
        }
        ServerStatus.thisObj.server_info.put("connected_unique_ips", String.valueOf(connected_ips.size()));
    }

    /*
     * Exception decompiling
     */
    @Override
    public void run() {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 4 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    public static String validate_ip(String ip, Properties server_item) throws Exception {
        String reason = crushftp.handlers.Common.check_ip((Vector)ServerStatus.thisObj.server_info.get("ip_restrictions_temp"), ip);
        reason = String.valueOf(reason) + crushftp.handlers.Common.check_ip((Vector)ServerStatus.server_settings.get("ip_restrictions"), ip);
        boolean notHammer = false;
        notHammer = server_item.getProperty("serverType", "").toUpperCase().indexOf("HTTP") >= 0 ? ServerStatus.thisObj.check_hammer_ip_http(ip) : ServerStatus.thisObj.check_hammer_ip(ip);
        Vector server_ips = (Vector)server_item.get("ip_restrictions");
        if (reason.equals("") && server_ips != null) {
            reason = String.valueOf(reason) + crushftp.handlers.Common.check_ip(server_ips, ip);
        }
        if (reason.equals("") && notHammer) {
            String addon;
            String string = addon = server_item.getProperty("serverType", "").toUpperCase().indexOf("HTTP") >= 0 ? "_http" : "";
            if (ServerStatus.IG("hammer_banning" + addon) > 0) {
                ServerStatus.siPUT("hammer_history" + addon, String.valueOf(ServerStatus.siSG("hammer_history" + addon)) + ip + "\r\n");
            }
            return "";
        }
        String[] never_ban = ServerStatus.SG("never_ban").split(",");
        int x = 0;
        while (x < never_ban.length) {
            if (!never_ban[x].trim().equals("") && Common.do_search(never_ban[x].trim(), ip, false, 0)) {
                return "";
            }
            ++x;
        }
        return reason;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int getUserLoginNum() {
        Object object = syncUserNumbers;
        synchronized (object) {
            int i = ServerStatus.siIG("user_login_num");
            if (i >= 0x7FFFFFF8) {
                i = 0;
            }
            ServerStatus.siPUT("user_login_num", String.valueOf(i + 1));
            return i + 1;
        }
    }
}

