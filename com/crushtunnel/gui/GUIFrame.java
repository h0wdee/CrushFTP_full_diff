/*
 * Decompiled with CFR 0.152.
 */
package com.crushtunnel.gui;

import com.crushftp.client.Common;
import com.crushtunnel.gui.GUI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.net.URI;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class GUIFrame
extends JFrame {
    private static final long serialVersionUID = 1L;
    public static MenuItem itemStatus = new MenuItem();
    public TrayIcon trayIcon = null;
    public static GUIFrame thisObj = null;

    public GUIFrame() {
        GUI gui = new GUI();
        thisObj = this;
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add((Component)gui, "Center");
        this.setDefaultCloseOperation(1);
        this.setSize(new Dimension(1050, 576));
        if (Common.machine_is_x() || Common.machine_is_windows()) {
            this.setVisible(false);
        } else {
            this.setVisible(true);
        }
        this.setTitle("Tunnels");
        this.setupSysTray();
    }

    public static void main(String[] args) {
        if (args != null && args.length > 0 && args[0].startsWith("appname")) {
            String[] args2 = Common.url_decode(args[0]).split(":::");
            int x = 0;
            while (x < args2.length) {
                String key = args2[x].substring(0, args2[x].indexOf("="));
                String val = args2[x].substring(args2[x].indexOf("=") + 1);
                System.getProperties().put("crushtunnel." + key, val);
                ++x;
            }
        }
        new GUIFrame();
    }

    public void setupSysTray() {
        PopupMenu popup = new PopupMenu();
        try {
            BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("/com/crushtunnel/gui/icon.png"));
            BufferedImage resizedImage = new BufferedImage(20, 20, image.getType() == 0 ? 2 : image.getType());
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(image, 0, 0, 20, 20, null);
            g.dispose();
            this.trayIcon = new TrayIcon(resizedImage, System.getProperty("crushtunnel.appname", "Tunnel"));
            this.trayIcon.setImageAutoSize(true);
            SystemTray tray = SystemTray.getSystemTray();
            this.trayIcon.setPopupMenu(popup);
            this.trayIcon.addMouseListener(new MouseListener(){

                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }
            });
            tray.add(this.trayIcon);
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "" + e);
        }
        try {
            if (Common.machine_is_windows()) {
                this.addMenuItem(popup, GUIFrame.l("Exit"), "exit");
            } else {
                this.addMenuItem(popup, GUIFrame.l("Quit"), "exit");
            }
            this.addMenuItem(popup, GUIFrame.l("About..."), "about");
            this.addMenuItem(popup, GUIFrame.l("Launch browser..."), "launch");
            this.addMenuItem(popup, GUIFrame.l("Tunnels"), "tunnels");
            itemStatus = this.addMenuItem(popup, GUIFrame.l("Status"), "status");
            itemStatus.setEnabled(false);
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "" + e);
        }
    }

    public void showMessage(String s) {
        if (this.trayIcon != null) {
            this.trayIcon.displayMessage(System.getProperty("crushtunnel.appname", "Tunnel"), s, TrayIcon.MessageType.INFO);
        }
        Common.activateFront();
    }

    public void menuItemSelected(String action) {
        if (action.equals("exit")) {
            System.exit(0);
        } else if (action.equals("about")) {
            JOptionPane.showMessageDialog(null, String.valueOf(System.getProperty("crushtunnel.appname", "Tunnel")) + " Version : " + "3.1.16");
        } else if (action.equals("tunnels")) {
            this.setVisible(false);
            Common.activateFront();
            this.setVisible(true);
        } else if (action.equals("launch")) {
            try {
                Desktop.getDesktop().browse(new URI("http://127.0.0.1:" + GUI.default_local_port + "/"));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.setVisible(true);
        }
    }

    public MenuItem addMenuItem(PopupMenu popup, String label, final String action) {
        MenuItem mi = new MenuItem(label);
        popup.insert(mi, 0);
        mi.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                GUIFrame.this.menuItemSelected(action);
            }
        });
        return mi;
    }

    public static String l(String key) {
        String s = System.getProperties().getProperty("crushtunnel.localization." + key, key);
        s = Common.replace_str(s, "%appname%", System.getProperty("crushtunnel.appname", "Tunnel"));
        return s;
    }
}

