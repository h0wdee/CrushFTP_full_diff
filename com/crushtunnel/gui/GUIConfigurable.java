/*
 * Decompiled with CFR 0.152.
 */
package com.crushtunnel.gui;

import com.crushftp.client.Worker;
import com.crushftp.tunnel2.Tunnel2;
import com.crushtunnel.gui.GUI;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class GUIConfigurable
extends JPanel {
    GUI gui = null;
    Properties p = null;
    private static final long serialVersionUID = 1L;
    Vector tunnels = new Vector();
    Thread tunnelThread = null;
    Tunnel2 tunnel = null;
    JTextField urlText = new JTextField();
    JTextField localPort = new JTextField();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    JTextField destIp = new JTextField();
    JLabel jLabel3 = new JLabel();
    JTextField destPort = new JTextField();
    JLabel jLabel4 = new JLabel();
    JLabel jLabel5 = new JLabel();
    JTextField userText = new JTextField();
    JPasswordField passText = new JPasswordField();
    JCheckBox reverse = new JCheckBox();
    JButton toggleTunnelButton = new JButton();
    JLabel descriptionText = new JLabel();
    JPanel connectionInfoPanel = new JPanel();
    JLabel urlLabel = new JLabel();
    int preferredHeight = 0;
    JTextField bindIp = new JTextField();
    private final JLabel label = new JLabel();
    private final JLabel lblIn = new JLabel();
    JTextField channelsInMax = new JTextField();
    private final JLabel lblOut = new JLabel();
    JTextField channelsOutMax = new JTextField();
    JLabel statusLabel = new JLabel();
    JLabel tunnelInfoLabel = new JLabel("");
    private final JLabel infoLabel = new JLabel("");

    public GUIConfigurable(Properties p, GUI gui) {
        this.p = p;
        this.gui = gui;
        try {
            this.jbInit();
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    GUIConfigurable.this.descriptionLoop();
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.urlText.setToolTipText("URL of the " + System.getProperty("appname", "CrushFTP") + " server.");
        this.urlText.setText("http://127.0.0.1:8080/");
        this.urlText.setBounds(new Rectangle(196, 6, 406, 22));
        this.jLabel1.setText(String.valueOf(System.getProperty("appname", "CrushFTP")) + " WebInterface URL:");
        this.jLabel1.setBounds(new Rectangle(5, 9, 199, 16));
        this.jLabel2.setBounds(new Rectangle(5, 42, 187, 16));
        this.jLabel2.setText("Local server IP and port:");
        this.destIp.setBounds(new Rectangle(196, 8, 198, 22));
        this.destIp.setToolTipText("This is where the tunnel ultimately connects to.");
        this.destIp.setText("192.168.0.10");
        this.jLabel3.setBounds(new Rectangle(5, 11, 226, 16));
        this.jLabel3.setText("Destination host and port:");
        this.destPort.setText("5900");
        this.destPort.setToolTipText("This is where the tunnel ultimately connects to.");
        this.destPort.setBounds(new Rectangle(405, 8, 78, 22));
        this.jLabel4.setToolTipText("");
        this.jLabel4.setText(":");
        this.jLabel4.setBounds(new Rectangle(396, 9, 18, 16));
        this.jLabel5.setBounds(new Rectangle(4, 36, 199, 16));
        this.jLabel5.setText(String.valueOf(System.getProperty("appname", "CrushFTP")) + " user and pass:");
        this.userText.setText("test2");
        this.userText.setToolTipText("This is where the tunnel ultimately connects to.");
        this.userText.setBounds(new Rectangle(195, 33, 198, 22));
        this.passText.setBounds(new Rectangle(404, 33, 198, 22));
        this.descriptionText.setHorizontalAlignment(0);
        this.descriptionText.setBounds(new Rectangle(6, 116, 563, 47));
        this.connectionInfoPanel.setVisible(false);
        this.connectionInfoPanel.setBorder(BorderFactory.createEtchedBorder());
        this.connectionInfoPanel.setBounds(new Rectangle(8, 177, 615, 63));
        this.connectionInfoPanel.setLayout(null);
        this.setSize(new Dimension(600, 180));
        this.setBorder(BorderFactory.createEtchedBorder());
        this.setPreferredSize(new Dimension(600, 180));
        this.urlLabel.setHorizontalAlignment(0);
        this.urlLabel.setBounds(new Rectangle(1, 14, 213, 19));
        this.setLayout(null);
        this.connectionInfoPanel.add((Component)this.userText, null);
        this.connectionInfoPanel.add((Component)this.passText, null);
        this.connectionInfoPanel.add((Component)this.jLabel5, null);
        this.connectionInfoPanel.add((Component)this.jLabel1, null);
        this.connectionInfoPanel.add((Component)this.urlText, null);
        this.add((Component)this.jLabel3, null);
        this.add((Component)this.destIp, null);
        this.add((Component)this.jLabel4, null);
        this.add((Component)this.destPort, null);
        this.add((Component)this.jLabel2, null);
        this.label.setToolTipText("");
        this.label.setText(":");
        this.label.setBounds(new Rectangle(396, 9, 18, 16));
        this.label.setBounds(396, 43, 18, 16);
        this.add(this.label);
        this.bindIp.setToolTipText("This is the local bind ip.");
        this.bindIp.setText("192.168.0.10");
        this.bindIp.setBounds(new Rectangle(196, 8, 198, 22));
        this.bindIp.setBounds(196, 42, 198, 22);
        this.add(this.bindIp);
        this.localPort.setBounds(new Rectangle(406, 37, 78, 22));
        this.localPort.setToolTipText("This is the port you connect to on your machine to be connected through the tunnel.");
        this.localPort.setText("5905");
        this.add((Component)this.localPort, null);
        this.reverse.setText("Reverse?");
        this.reverse.setBounds(new Rectangle(483, 42, 100, 22));
        this.add((Component)this.reverse, null);
        this.lblIn.setText("In:");
        this.lblIn.setBounds(new Rectangle(5, 11, 226, 16));
        this.lblIn.setBounds(5, 74, 44, 16);
        this.add(this.lblIn);
        this.channelsInMax.setToolTipText("Max channels in.");
        this.channelsInMax.setText("1");
        this.channelsInMax.setBounds(new Rectangle(196, 8, 198, 22));
        this.channelsInMax.setBounds(30, 71, 55, 22);
        this.add(this.channelsInMax);
        this.lblOut.setText("Out:");
        this.lblOut.setBounds(new Rectangle(5, 11, 226, 16));
        this.lblOut.setBounds(94, 74, 44, 16);
        this.add(this.lblOut);
        this.channelsOutMax.setToolTipText("Max channels out.");
        this.channelsOutMax.setText("1");
        this.channelsOutMax.setBounds(new Rectangle(196, 8, 198, 22));
        this.channelsOutMax.setBounds(124, 71, 55, 22);
        this.add(this.channelsOutMax);
        this.toggleTunnelButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                GUIConfigurable.this.gui.startStopTunnel(GUIConfigurable.this.p);
            }
        });
        this.tunnelInfoLabel.setBounds(196, 74, 134, 16);
        this.add(this.tunnelInfoLabel);
        this.infoLabel.setHorizontalAlignment(0);
        this.infoLabel.setBounds(11, 99, 569, 16);
        this.add(this.infoLabel);
        this.toggleTunnelButton.setText("Connect");
        this.toggleTunnelButton.setBounds(new Rectangle(333, 71, 150, 30));
        this.add((Component)this.toggleTunnelButton, null);
        this.statusLabel.setText(this.p.getProperty("localPort", "0"));
        this.statusLabel.setBounds(493, 68, 65, 30);
        this.statusLabel.setIcon(new ImageIcon(this.getClass().getResource("/assets/red.gif")));
        this.statusLabel.setFont(new Font("Arial", 0, 10));
        this.add((Component)this.statusLabel, null);
        this.add((Component)this.descriptionText, null);
        this.add((Component)this.connectionInfoPanel, null);
        this.add((Component)this.urlLabel, null);
        Properties guiItem = new Properties();
        guiItem.put("toggleTunnelButton", this.toggleTunnelButton);
        guiItem.put("tunnelItemPanel", this);
        guiItem.put("tunnelInfoLabel", this.tunnelInfoLabel);
        guiItem.put("infoLabel", this.infoLabel);
        guiItem.put("statusLabel", this.statusLabel);
        this.p.put("gui", guiItem);
    }

    public void descriptionLoop() {
        while (true) {
            String s = "";
            s = this.reverse.isSelected() ? "<html><body><center>Once started, connections on the " + System.getProperty("appname", "CrushFTP") + " server port " + this.localPort.getText() + " will go through the tunnel to this machine, and be connected to " + this.destIp.getText() + ":" + this.destPort.getText() + " in your network.</center></body></html>" : "<html><body><center>Once started, connection on this machine's port " + this.localPort.getText() + " will go through the tunnel and be connected to " + this.destIp.getText() + ":" + this.destPort.getText() + " in the " + System.getProperty("appname", "CrushFTP") + " server's network.</center></body></html>";
            this.descriptionText.setText(s);
            try {
                Thread.sleep(1000L);
            }
            catch (Exception exception) {
            }
        }
    }
}

