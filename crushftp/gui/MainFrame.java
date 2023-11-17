/*
 * Decompiled with CFR 0.152.
 */
package crushftp.gui;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import crushftp.gui.LOC;
import crushftp.handlers.Log;
import crushftp.server.ServerStatus;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class MainFrame
extends JFrame {
    private static final long serialVersionUID = 1L;
    static ServerStatus st = null;
    public JFrame thisObj = null;
    private JPanel contentPane;
    JButton btnAuthenticateCrushftpFor = new JButton("Authenticate for OS X");
    JButton btnStartTemporaryServer = new JButton("Start Temporary Server");
    JButton btnInstallDaemon = new JButton("Install Daemon / Service");
    JButton btnRemoveDaemon = new JButton("Remove Daemon / Service");
    JLabel auth_label = new JLabel("<html><body>Before we can accept connections on ports below 1024 (such as 21,80,443) as a temp server, you must authenticate this on OS X.  This will give it the permissions needed to listen on priviledged ports.</body></html>");
    JLabel lblthisWillInstall = new JLabel("<html><body>This will install the daemon on OS X, or Windows service.  This allows " + System.getProperty("appname", "CrushFTP") + " to be running without the need for a user to be logged in on the OS.</body></html>");
    JLabel lblthisWillRemove = new JLabel("<html><body>This will remove the daemon on OS X, or Windows service.  Only use this when you want to stop and uninstall the server.</body></html>");
    private final JButton btnCreateNewAdmin = new JButton("Create New Admin User");
    private final JLabel lblthisWillBuild = new JLabel("<html><body>This will build a new administration user so that you can configure the server from your web browser.  Do not use the username 'admin', or 'administrator', or 'root'.  Suggested usernames would be your OS username, 'remoteadmin' or 'crushadmin', etc.</body></html>");
    JButton btnStartDaemon = new JButton("Start Daemon / Service");
    JButton btnStopDaemon = new JButton("Stop Daemon / Service");

    public MainFrame() {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", System.getProperty("appname", "CrushFTP"));
        crushftp.handlers.Common.initSystemProperties(true);
        this.thisObj = this;
        String msg = "";
        if (!crushftp.handlers.Common.haveWriteAccess()) {
            msg = String.valueOf(msg) + LOC.G("It appears you are running this from a locked disk.") + "\r\n" + LOC.G("Please copy the application folder to a location where it has full access to its own folder.  (Or run as an administrator.)") + "\r\n\r\n";
        }
        if (msg.length() > 0) {
            JOptionPane.showMessageDialog(this, msg, LOC.G("Alert"), 0);
        }
        MenuBar mbar = new MenuBar();
        if (!crushftp.handlers.Common.machine_is_x()) {
            Menu menu = new Menu("File");
            MenuItem item = new MenuItem("Quit");
            item.setShortcut(new MenuShortcut(81, false));
            item.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    MainFrame.this.quit();
                }
            });
            menu.add(item);
            mbar.add(menu);
        }
        this.setMenuBar(mbar);
        this.setResizable(false);
        this.setTitle(String.valueOf(System.getProperty("appname", "CrushFTP")) + " " + ServerStatus.version_info_str + ServerStatus.sub_version_info_str);
        this.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                MainFrame.this.quit();
            }
        });
        this.setBounds(100, 100, 645, 584);
        this.contentPane = new JPanel();
        this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.setContentPane(this.contentPane);
        this.contentPane.setLayout(null);
        this.btnAuthenticateCrushftpFor.setToolTipText("Grants " + System.getProperty("appname", "CrushFTP") + " permissions so that it can install a daemon, and use reserved ports.");
        this.btnAuthenticateCrushftpFor.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    Common.check_exec();
                    Runtime.getRuntime().exec(new String[]{"osascript", "-e", "do shell script \"" + new File_S(String.valueOf(System.getProperty("crushftp.home")) + System.getProperty("crushftp.executable")).getCanonicalPath() + "\" with administrator privileges"});
                }
                catch (Exception ee) {
                    Log.log("SERVER", 0, ee);
                    JOptionPane.showMessageDialog(null, ee.getMessage());
                }
                MainFrame.this.thisObj.setVisible(true);
                MainFrame.this.quit();
            }
        });
        this.btnAuthenticateCrushftpFor.setBounds(182, 146, 282, 29);
        this.contentPane.add(this.btnAuthenticateCrushftpFor);
        this.auth_label.setBounds(5, 175, 635, 63);
        this.contentPane.add(this.auth_label);
        this.btnInstallDaemon.setToolTipText("Install the daemon/service allowing the server to start when the machine boots without user interaction.");
        this.btnInstallDaemon.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (ServerStatus.SG("max_max_users").equals("5")) {
                    JOptionPane.showMessageDialog(MainFrame.this.thisObj, "Sorry, this feature is only available to registered users.", "Alert", 0);
                } else {
                    if (System.getProperty("java.version").startsWith("1.4")) {
                        JOptionPane.showMessageDialog(MainFrame.this.thisObj, "Sorry, Java 1.4 is not support for installing a service.\r\n\r\nPlease update to java version 1.6 or higher.", "Alert", 0);
                        return;
                    }
                    Log.log("SERVER", 0, "Preparing to install service...");
                    ServerStatus.thisObj.stop_all_servers();
                    ServerStatus.thisObj.statTools.stopDB();
                    if (crushftp.handlers.Common.machine_is_windows()) {
                        Log.log("SERVER", 0, "Unzipping required service files for Windows.");
                        String ram_megabytes = (String)JOptionPane.showInputDialog(MainFrame.this.thisObj, "Max RAM for server in MegaBytes:", "Max RAM for Server", 0, null, null, "512");
                        if (ram_megabytes != null) {
                            if (!Common.install_windows_service(Integer.parseInt(ram_megabytes.trim()), System.getProperty("appname", "CrushFTP"), "plugins/lib/" + System.getProperty("appname", "CrushFTP") + "JarProxy.jar")) {
                                JOptionPane.showMessageDialog(MainFrame.this.thisObj, "Access Denied: You must right click and run " + System.getProperty("appname", "CrushFTP") + " as an Administrator.", "Service Install Failed", 0);
                                MainFrame.this.quit();
                                return;
                            }
                            int response = JOptionPane.showConfirmDialog(null, "Do you want to change the 'run as user' for the service?", "Change Run As User for service?", 0, 3);
                            if (response == 0) {
                                boolean ok = false;
                                while (!ok) {
                                    String domainuser = (String)JOptionPane.showInputDialog(MainFrame.this.thisObj, "Windows Domain Username:", "Windows Domain Username", 0, null, null, ".\\smithbob");
                                    if (domainuser != null) {
                                        String domainpass = Common.getPasswordPrompt("Password:");
                                        String result = Common.install_windows_service_username(domainuser, domainpass, System.getProperty("appname", "CrushFTP"));
                                        if (result.equals("")) {
                                            MainFrame.this.stopDaemon(true);
                                            MainFrame.this.startDaemon(true);
                                            JOptionPane.showMessageDialog(null, "Service account configured.");
                                            ok = true;
                                            continue;
                                        }
                                        JOptionPane.showMessageDialog(null, "Error:\r\n" + result);
                                        ok = false;
                                        continue;
                                    }
                                    JOptionPane.showMessageDialog(null, "Service account not configured.");
                                    break;
                                }
                            }
                        }
                    } else if (crushftp.handlers.Common.machine_is_x()) {
                        ServerStatus.thisObj.common_code.install_osx_service();
                    }
                    Log.log("SERVER", 0, "Finished.  Log file has messages if there were any errors. (Administrator user is required.");
                    JOptionPane.showMessageDialog(null, "Service Installed");
                    MainFrame.this.quit();
                }
            }
        });
        this.lblthisWillInstall.setBounds(5, 305, 635, 63);
        this.contentPane.add(this.lblthisWillInstall);
        this.btnInstallDaemon.setBounds(27, 278, 282, 29);
        this.contentPane.add(this.btnInstallDaemon);
        this.btnStartTemporaryServer.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                MainFrame.this.startTempServer();
            }
        });
        this.btnStartTemporaryServer.setBounds(337, 278, 282, 29);
        this.contentPane.add(this.btnStartTemporaryServer);
        this.btnRemoveDaemon.setToolTipText("Remove the daemon/service which allowed the server to start when the machine booted without user interaction.");
        this.btnRemoveDaemon.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (crushftp.handlers.Common.machine_is_windows()) {
                    if (!Common.remove_windows_service(System.getProperty("appname", "CrushFTP"), "plugins/lib/" + System.getProperty("appname", "CrushFTP") + "JarProxy.jar")) {
                        JOptionPane.showMessageDialog(MainFrame.this.thisObj, "Access Denied: You must right click and run " + System.getProperty("appname", "CrushFTP") + " as an Administrator.", "Service Remove Failed", 0);
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.this.thisObj, "Service removed.", "Alert", 1);
                    }
                } else if (crushftp.handlers.Common.machine_is_x()) {
                    crushftp.handlers.Common.remove_osx_service();
                    JOptionPane.showMessageDialog(null, "Daemon removed.");
                }
            }
        });
        this.lblthisWillRemove.setBounds(5, 441, 635, 63);
        this.contentPane.add(this.lblthisWillRemove);
        this.btnRemoveDaemon.setBounds(182, 414, 282, 29);
        this.contentPane.add(this.btnRemoveDaemon);
        this.btnCreateNewAdmin.setToolTipText("Create an admin user that can do administration from the web browser.");
        this.btnCreateNewAdmin.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                String username;
                Vector pref_server_items = (Vector)ServerStatus.server_settings.get("server_list");
                int x = 0;
                while (x < pref_server_items.size()) {
                    Properties p = (Properties)pref_server_items.elementAt(x);
                    if (!p.getProperty("linkedServer", "").equals("") && ServerStatus.VG("server_groups").indexOf(p.getProperty("linkedServer", "")) < 0) {
                        ServerStatus.VG("server_groups").addElement(p.getProperty("linkedServer", ""));
                    }
                    ++x;
                }
                Object[] possibleValues = ServerStatus.VG("server_groups").toArray();
                String serverGroup = null;
                serverGroup = possibleValues.length == 1 ? (String)possibleValues[0] : (String)JOptionPane.showInputDialog(MainFrame.this.thisObj, "Pick a user connection group:", "Pick A User Connection Group", 1, null, possibleValues, null);
                if (serverGroup != null && (username = (String)JOptionPane.showInputDialog(MainFrame.this.thisObj, "Username:", "Username", 0, null, null, "crushadmin")) != null) {
                    String password = Common.getPasswordPrompt("Password:");
                    ServerStatus.thisObj.common_code.writeAdminUser(username, password, serverGroup, false);
                    JOptionPane.showMessageDialog(null, "Admin User Created.");
                }
            }
        });
        this.lblthisWillBuild.setBounds(5, 33, 635, 63);
        this.contentPane.add(this.lblthisWillBuild);
        this.btnCreateNewAdmin.setBounds(182, 6, 282, 29);
        this.contentPane.add(this.btnCreateNewAdmin);
        this.btnStopDaemon.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                MainFrame.this.stopDaemon(false);
            }
        });
        this.btnStopDaemon.setToolTipText("If installed, stop the daemon/service.");
        this.btnStopDaemon.setBounds(25, 502, 282, 29);
        this.contentPane.add(this.btnStopDaemon);
        this.btnStartDaemon.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                MainFrame.this.startDaemon(false);
            }
        });
        this.btnStartDaemon.setToolTipText("If installed, start the daemon/service.");
        this.btnStartDaemon.setBounds(337, 502, 282, 29);
        this.contentPane.add(this.btnStartDaemon);
        this.checkAuthed();
        this.setVisible(true);
        new Thread(new Runnable(){

            @Override
            public void run() {
                if (Common.log == null) {
                    Common.log = new Vector();
                }
                st = new ServerStatus(false, null);
            }
        }).start();
        if (!ServerStatus.killUpdateFiles()) {
            JOptionPane.showMessageDialog(this, LOC.G("Update not complete!  Please close " + System.getProperty("appname", "CrushFTP") + " and run 'update.bat' first!"));
        }
        if (System.getProperties().getProperty("crushftp.autostart", "false").equals("true")) {
            this.startTempServer();
        }
    }

    public void stopDaemon(boolean silent) {
        Common.stopDaemon(silent, System.getProperty("appname", "CrushFTP"));
    }

    public void startDaemon(boolean silent) {
        Common.startDaemon(silent, System.getProperty("appname", "CrushFTP"));
    }

    public void checkAuthed() {
        if (crushftp.handlers.Common.machine_is_x_10_6_plus()) {
            this.btnAuthenticateCrushftpFor.setVisible(true);
            this.auth_label.setVisible(true);
            return;
        }
        if (crushftp.handlers.Common.OSXApp()) {
            this.btnAuthenticateCrushftpFor.setEnabled(true);
        }
        int openedPorts = 0;
        int x = 900;
        while (x < 1000) {
            try {
                ServerSocket s = new ServerSocket(x);
                s.close();
                ++openedPorts;
                break;
            }
            catch (BindException e) {
                if (e.toString().indexOf("denied") >= 0) {
                    break;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            ++x;
        }
        if (openedPorts > 0) {
            this.auth_label.setVisible(false);
            this.btnAuthenticateCrushftpFor.setVisible(false);
            this.btnAuthenticateCrushftpFor.setEnabled(false);
        }
        if (!crushftp.handlers.Common.OSXApp()) {
            this.btnAuthenticateCrushftpFor.setEnabled(false);
        }
        if (crushftp.handlers.Common.machine_is_x()) {
            this.btnInstallDaemon.setEnabled(crushftp.handlers.Common.OSXApp());
            this.btnRemoveDaemon.setEnabled(crushftp.handlers.Common.OSXApp());
            this.btnStartDaemon.setEnabled(crushftp.handlers.Common.OSXApp());
            this.btnStopDaemon.setEnabled(crushftp.handlers.Common.OSXApp());
            if (!crushftp.handlers.Common.OSXApp()) {
                this.btnAuthenticateCrushftpFor.setToolTipText("You must be running as an application for these buttons to be enabled.");
                this.btnInstallDaemon.setToolTipText(this.btnAuthenticateCrushftpFor.getToolTipText());
                this.btnRemoveDaemon.setToolTipText(this.btnAuthenticateCrushftpFor.getToolTipText());
            }
        }
    }

    public void quit() {
        new Thread(new Runnable(){

            @Override
            public void run() {
                ServerStatus.thisObj.quit_server(true);
                System.exit(0);
            }
        }).start();
        try {
            Thread.sleep(30000L);
            System.exit(1);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void startTempServer() {
        try {
            while (st == null) {
                Thread.sleep(100L);
            }
            st.init_setup(true);
            this.btnStartTemporaryServer.setEnabled(false);
            this.btnStartTemporaryServer.setToolTipText("Server is running, closing this window will quit the server.");
            String validURLs = "";
            Vector servers = ServerStatus.VG("server_list");
            int x = 0;
            while (x < servers.size()) {
                Properties server = (Properties)servers.elementAt(x);
                if (!server.getProperty("port").startsWith("555")) {
                    String ip = Common.getLocalIP();
                    if (server.getProperty("serverType", "").equals("HTTP")) {
                        if (server.getProperty("ip").equals("lookup")) {
                            validURLs = String.valueOf(validURLs) + "http://127.0.0.1" + (server.getProperty("port").equals("80") ? "" : ":" + server.getProperty("port")) + "/\r\n";
                            validURLs = String.valueOf(validURLs) + "http://" + ip + (server.getProperty("port").equals("80") ? "" : ":" + server.getProperty("port")) + "/\r\n";
                        } else {
                            validURLs = String.valueOf(validURLs) + "http://" + server.getProperty("ip") + (server.getProperty("port").equals("80") ? "" : ":" + server.getProperty("port")) + "/\r\n";
                        }
                    } else if (server.getProperty("serverType", "").equals("HTTPS")) {
                        if (server.getProperty("ip").equals("lookup")) {
                            validURLs = String.valueOf(validURLs) + "https://127.0.0.1" + (server.getProperty("port").equals("443") ? "" : ":" + server.getProperty("port")) + "/\r\n";
                            validURLs = String.valueOf(validURLs) + "https://" + ip + (server.getProperty("port").equals("443") ? "" : ":" + server.getProperty("port")) + "/\r\n";
                        } else {
                            validURLs = String.valueOf(validURLs) + "https://" + server.getProperty("ip") + (server.getProperty("port").equals("443") ? "" : ":" + server.getProperty("port")) + "/\r\n";
                        }
                    }
                }
                ++x;
            }
            if (validURLs.equals("")) {
                Properties server_item = new Properties();
                server_item.put("linkedServer", ServerStatus.VG("server_groups").elementAt(0));
                server_item.put("serverType", "HTTP");
                server_item.put("ip", "lookup");
                server_item.put("port", "9090");
                server_item.put("require_encryption", "false");
                server_item.put("https_redirect", "false");
                server_item.put("explicit_ssl", "false");
                server_item.put("explicit_tls", "false");
                server_item.put("explicit_tls", "false");
                server_item.put("require_secure", "false");
                server_item.put("http", "true");
                server_item.put("server_ip", "auto");
                server_item.put("pasv_ports", "1025-65535");
                server_item.put("ftp_aware_router", "true");
                servers.addElement(server_item);
                st.save_server_settings(true);
                this.startTempServer();
                return;
            }
            this.btnStartTemporaryServer.setToolTipText("<html><body>Server is running, closing this window will quit the server.<br/><pre>" + validURLs + "</pre></body></html>");
            if (System.getProperties().getProperty("crushftp.autostart", "false").equals("false")) {
                JOptionPane.showMessageDialog(null, "Servers Started, use a web browser now and go to one of these URLs:\r\n" + validURLs);
            }
            if (ServerStatus.thisObj != null) {
                ServerStatus.thisObj.checkCrushExpiration();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error" + e.getMessage());
        }
    }
}

