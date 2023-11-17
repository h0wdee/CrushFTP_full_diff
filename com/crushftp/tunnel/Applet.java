/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel;

import com.crushftp.client.Common;
import com.crushftp.tunnel.Downloader;
import com.crushftp.tunnel.Uploader;
import com.crushftp.tunnel2.Tunnel2;
import java.applet.AppletContext;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Applet
extends java.applet.Applet
implements DropTargetListener {
    public static Object uploadLock = new Object();
    public static Object downloadLock = new Object();
    public static Applet thisObj = null;
    private static final long serialVersionUID = 1L;
    public static AppletContext appletContext = null;
    Properties tunnelController = new Properties();
    public Properties uploaders = new Properties();
    public Properties downloaders = new Properties();
    StringBuffer CrushAuth = new StringBuffer();
    long availableRam = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory();
    boolean ramConfigured = false;
    Vector commands = new Vector();
    Properties resultsAsync = new Properties();
    Vector droppedFiles = new Vector();

    @Override
    public void init() {
        System.setProperty("java.net.useSystemProxies", "true");
        System.getProperties().put("sun.net.http.retryPost", "false");
        thisObj = this;
        this.updateRam();
        System.out.println();
        System.out.println("C 3.1.16 Initialized. " + Common.format_bytes_short(this.availableRam));
        this.setBackground(this.parseColorStr(this.getParameter("background-color")));
        appletContext = this.getAppletContext();
        new Thread(new Runnable(){

            @Override
            public void run() {
                Thread.currentThread().setName("Applet Javascript Command Watcher");
                Applet.this.commandWatcher();
            }
        }).start();
        new DropTarget(this, this);
    }

    public void updateRam() {
        this.availableRam = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory();
        if (this.availableRam / 1024L / 1024L < 32L) {
            this.availableRam = 0x2000000L;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    public String test() {
        return "OK";
    }

    public void commandWatcher() {
        while (true) {
            try {
                Properties p = null;
                if (this.commands.size() > 0) {
                    p = (Properties)this.commands.remove(0);
                }
                if (p != null) {
                    if (!p.getProperty("COMMAND").equalsIgnoreCase("DND") && !p.getProperty("COMMAND").equalsIgnoreCase("ACTION")) {
                        System.out.println(p);
                    }
                    if (p.getProperty("COMMAND").equalsIgnoreCase("AUTH")) {
                        this.doAuth(p);
                    } else if (p.getProperty("COMMAND").equalsIgnoreCase("LOGIN")) {
                        this.doLogin(p);
                    } else if (p.getProperty("COMMAND").equalsIgnoreCase("BROWSE")) {
                        this.doBrowse(p);
                    } else if (p.getProperty("COMMAND").equalsIgnoreCase("LIST")) {
                        this.doList(p);
                    } else if (p.getProperty("COMMAND").equalsIgnoreCase("RESOLVE")) {
                        this.doResolve(p);
                    } else if (p.getProperty("COMMAND").equalsIgnoreCase("UPLOAD")) {
                        this.doUpload(p);
                    } else if (p.getProperty("COMMAND").equalsIgnoreCase("DOWNLOAD")) {
                        this.doDownload(p);
                    } else if (p.getProperty("COMMAND").equalsIgnoreCase("ACTION")) {
                        this.doAction(p);
                    } else if (p.getProperty("COMMAND").equalsIgnoreCase("DND")) {
                        this.doDnd(p);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(100L);
            }
            catch (Exception exception) {
            }
        }
    }

    public String doCommandSync(String req) {
        try {
            String[] parts = req.split(":::");
            Properties p = new Properties();
            int x = 0;
            while (x < parts.length) {
                p.put(parts[x].split("=")[0], parts[x].substring(parts[x].indexOf("=") + 1));
                ++x;
            }
            this.commands.addElement(p);
            while (!p.containsKey("result")) {
                Thread.sleep(100L);
            }
            return p.getProperty("result_data", "");
        }
        catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String doCommandASync(final String req) {
        final String id = Common.makeBoundary(5);
        new Thread(new Runnable(){

            @Override
            public void run() {
                try {
                    String myId = id;
                    String[] parts = req.split(":::");
                    Properties p = new Properties();
                    int x = 0;
                    while (x < parts.length) {
                        p.put(parts[x].split("=")[0], parts[x].substring(parts[x].indexOf("=") + 1));
                        ++x;
                    }
                    Applet.this.commands.addElement(p);
                    while (!p.containsKey("result")) {
                        Thread.sleep(100L);
                    }
                    Applet.this.resultsAsync.put(myId, p.getProperty("result_data", ""));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return id;
    }

    public String getASyncResult(String id) {
        return this.resultsAsync.getProperty(id);
    }

    public void doAuth(Properties p) {
        this.CrushAuth.setLength(0);
        this.CrushAuth.append(p.getProperty("CRUSHAUTH", ""));
        p.put("result", "ok");
    }

    public void doLogin(Properties p) {
        StringBuffer auth = new StringBuffer();
        boolean result = Uploader.login(p, false, auth);
        p.put("CRUSHAUTH", auth.toString());
        if (result) {
            this.doAuth(p);
            p.put("result", auth.toString());
        } else {
            p.put("result", "login failed");
        }
    }

    public void doBrowse(final Properties p) {
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                try {
                    Container chooser;
                    String path = "";
                    if (Applet.this.machine_is_x()) {
                        try {
                            path = String.valueOf(new File("~/Desktop/").getCanonicalPath()) + "/";
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    int returnVal = -1;
                    File[] selected = new File[1];
                    boolean badJava = System.getProperty("java.version").startsWith("1.7");
                    if (!badJava && Common.machine_is_x() && p.getProperty("DIRECTORIES_ONLY", "").equals("true")) {
                        chooser = null;
                        chooser = new FileDialog(null, p.getProperty("TITLE", "Choose Where to Download To..."));
                        ((FileDialog)chooser).setDirectory(path);
                        System.setProperty("apple.awt.fileDialogForDirectories", "true");
                        ((Dialog)chooser).setVisible(true);
                        System.setProperty("apple.awt.fileDialogForDirectories", "false");
                        if (((FileDialog)chooser).getDirectory() != null && ((FileDialog)chooser).getFile() != null) {
                            returnVal = 0;
                            selected[0] = new File(String.valueOf(((FileDialog)chooser).getDirectory()) + ((FileDialog)chooser).getFile());
                        }
                    } else {
                        if (Common.machine_is_windows()) {
                            try {
                                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        chooser = new JFileChooser(path);
                        if (p.getProperty("DIRECTORIES_ONLY", "").equals("true")) {
                            ((JFileChooser)chooser).setFileSelectionMode(1);
                            ((JFileChooser)chooser).setMultiSelectionEnabled(false);
                            ((JFileChooser)chooser).setDialogTitle(p.getProperty("TITLE", "Choose Where to Download To..."));
                        } else {
                            ((JFileChooser)chooser).setFileSelectionMode(2);
                            ((JFileChooser)chooser).setMultiSelectionEnabled(true);
                            ((JFileChooser)chooser).setDialogTitle(p.getProperty("TITLE", "Choose Items to Upload..."));
                        }
                        returnVal = ((JFileChooser)chooser).showOpenDialog(thisObj);
                        selected = ((JFileChooser)chooser).getSelectedFiles();
                        if (p.getProperty("DIRECTORIES_ONLY", "").equals("true")) {
                            selected = new File[]{((JFileChooser)chooser).getSelectedFile()};
                        }
                    }
                    if (returnVal == 0) {
                        String result_data = "";
                        int x = 0;
                        while (x < selected.length) {
                            result_data = String.valueOf(result_data) + "path=" + selected[x].getCanonicalPath() + ":::name=" + selected[x].getName() + ":::size=" + selected[x].length() + ":::type=" + (selected[x].isDirectory() ? "dir" : "file") + ";;;";
                            ++x;
                        }
                        if (result_data.length() > 0) {
                            result_data = result_data.substring(0, result_data.length() - 3);
                        }
                        p.put("result_data", result_data);
                        p.put("result", "ok");
                    } else {
                        p.put("result", "cancelled");
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    p.put("result", "cancelled");
                }
            }
        });
    }

    public void doList(Properties p) {
        try {
            File f = new File(Common.replace_str(p.getProperty("PATH"), "~", p.getProperty("PATH").startsWith("~") ? System.getProperty("user.home") : "~"));
            f = new File(f.getCanonicalPath());
            System.out.println("Getting list for:" + p.getProperty("PATH") + " which translates to:" + f);
            File[] files = f.listFiles();
            String result_data = "";
            if (files != null) {
                System.out.println("Found " + files.length + " items.");
            } else {
                System.out.println("No files found.");
            }
            int x = 0;
            while (files != null && x < files.length) {
                String s = "path=" + files[x].getCanonicalPath() + ":::name=" + files[x].getName() + ":::size=" + files[x].length() + ":::type=" + (files[x].isDirectory() ? "dir" : "file") + ":::modified=" + files[x].lastModified() + ";;;";
                System.out.println(s);
                result_data = String.valueOf(result_data) + s;
                ++x;
            }
            if (result_data.length() > 0) {
                result_data = result_data.substring(0, result_data.length() - 3);
            }
            p.put("result_data", result_data);
            p.put("result", "ok");
        }
        catch (Exception e) {
            e.printStackTrace();
            p.put("result", "cancelled");
        }
    }

    public void doResolve(Properties p) {
        try {
            System.out.println("Resolving " + p.getProperty("PATH"));
            File f = new File(Common.replace_str(p.getProperty("PATH"), "~", p.getProperty("PATH").startsWith("~") ? System.getProperty("user.home") : "~"));
            f = new File(f.getCanonicalPath());
            System.out.println("And got:" + f);
            String result_data = "path=" + f.getCanonicalPath() + ":::name=" + f.getName() + ":::size=" + f.length() + ":::type=" + (f.isDirectory() ? "dir" : "file") + ":::modified=" + f.lastModified() + ";;;";
            if (result_data.length() > 0) {
                result_data = result_data.substring(0, result_data.length() - 3);
            }
            p.put("result_data", result_data);
            p.put("result", "ok");
        }
        catch (Exception e) {
            e.printStackTrace();
            p.put("result", "cancelled");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void doUpload(Properties p) {
        Uploader uploader;
        if (this.uploaders.get(p.getProperty("UNIQUE_KEY", "0")) == null) {
            this.uploaders.put(p.getProperty("UNIQUE_KEY", "0"), new Uploader(new Properties(), this.CrushAuth));
        }
        Uploader uploader2 = uploader = (Uploader)this.uploaders.get(p.getProperty("UNIQUE_KEY", "0"));
        synchronized (uploader2) {
            uploader.controller.clear();
            uploader.controller.put("PROTOCOL", "http");
            uploader.controller.put("PORT", "80");
            uploader.controller.put("HOST", "127.0.0.1");
            uploader.controller.put("PATH", "/");
            uploader.controller.put("TYPE", "upload");
            uploader.controller.put("inuse", "true");
            uploader.controller.putAll((Map<?, ?>)p);
        }
        new Thread(new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                if (!Applet.this.ramConfigured) {
                    Applet.this.ramConfigured = true;
                    Applet.this.updateRam();
                    Tunnel2.setMaxRam((int)(Applet.this.availableRam / 0x100000L) - 16);
                }
                Uploader uploader2 = uploader;
                synchronized (uploader2) {
                    uploader.go();
                }
            }
        }).start();
        p.put("result", "ok");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void doDownload(Properties p) {
        Downloader downloader;
        if (this.downloaders.get(p.getProperty("UNIQUE_KEY", "0")) == null) {
            this.downloaders.put(p.getProperty("UNIQUE_KEY", "0"), new Downloader(new Properties(), new Properties(), "/", this.CrushAuth));
        }
        Downloader downloader2 = downloader = (Downloader)this.downloaders.get(p.getProperty("UNIQUE_KEY", "0"));
        synchronized (downloader2) {
            downloader.controller.clear();
            downloader.controller.put("PROTOCOL", "http");
            downloader.controller.put("PORT", "80");
            downloader.controller.put("HOST", "127.0.0.1");
            downloader.controller.put("PATH", "/");
            downloader.controller.put("TYPE", "download");
            downloader.controller.putAll((Map<?, ?>)p);
        }
        new Thread(new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                if (!Applet.this.ramConfigured) {
                    Applet.this.ramConfigured = true;
                    Applet.this.updateRam();
                    Tunnel2.setMaxRam((int)(Applet.this.availableRam / 0x100000L) - 16);
                }
                Downloader downloader2 = downloader;
                synchronized (downloader2) {
                    downloader.go();
                }
            }
        }).start();
        p.put("result", "ok");
    }

    public void doAction(Properties p) {
        if (p.getProperty("TYPE", "UPLOAD").equals("UPLOAD")) {
            Uploader uploader = (Uploader)this.uploaders.get(p.getProperty("UNIQUE_KEY", "0"));
            if (p.getProperty("ACTION", "").equals("PAUSE")) {
                uploader.pause();
            }
            if (p.getProperty("ACTION", "").equals("RESUME")) {
                uploader.resume();
            }
            if (p.getProperty("ACTION", "").equals("CANCEL")) {
                uploader.cancel();
            }
            String result_data = "status=" + uploader.getStatus() + ":::totalBytes=" + uploader.getTotalBytes() + ":::transferedBytes=" + uploader.getTransferedBytes() + ":::totalItems=" + uploader.getTotalItems() + ":::transferedItems=" + uploader.getTransferedItems();
            p.put("result_data", result_data);
        } else {
            Downloader downloader = (Downloader)this.downloaders.get(p.getProperty("UNIQUE_KEY", "0"));
            if (p.getProperty("ACTION", "").equals("PAUSE")) {
                downloader.pause();
            }
            if (p.getProperty("ACTION", "").equals("RESUME")) {
                downloader.resume();
            }
            if (p.getProperty("ACTION", "").equals("CANCEL")) {
                downloader.cancel();
            }
            String result_data = "status=" + downloader.getStatus() + ":::totalBytes=" + downloader.getTotalBytes() + ":::transferedBytes=" + downloader.getTransferedBytes() + ":::totalItems=" + downloader.getTotalItems() + ":::transferedItems=" + downloader.getTransferedItems();
            p.put("result_data", result_data);
        }
        p.put("result", "ok");
    }

    @Override
    public void stop() {
    }

    @Override
    public void dragEnter(DropTargetDragEvent event) {
        event.acceptDrag(1);
    }

    @Override
    public void dragExit(DropTargetEvent event) {
    }

    @Override
    public void dragOver(DropTargetDragEvent event) {
        event.acceptDrag(1);
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent event) {
    }

    @Override
    public void drop(DropTargetDropEvent event) {
        event.acceptDrop(1);
        this.processFlavors(event.getTransferable());
    }

    public void processFlavors(Transferable data) {
        DataFlavor[] flavors = data.getTransferDataFlavors();
        int i = 0;
        while (i < flavors.length) {
            block10: {
                try {
                    Object stuff = data.getTransferData(flavors[i]);
                    if (!(stuff instanceof List)) break block10;
                    List list = (List)stuff;
                    int j = 0;
                    while (j < list.size()) {
                        try {
                            Object item = list.get(j);
                            if (item instanceof File) {
                                File file = (File)item;
                                long size = 0L;
                                if (file.isFile()) {
                                    size = file.length();
                                } else if (file.isDirectory()) {
                                    size = Common.recurseSize(String.valueOf(file.getCanonicalPath()) + "/", 0L);
                                }
                                this.droppedFiles.addElement("path=" + file.getCanonicalPath().replace('\\', '/') + ":::name=" + file.getName() + ":::size=" + size + ":::type=" + (file.isDirectory() ? "DIR" : "FILE"));
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        ++j;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ++i;
        }
    }

    public void doDnd(Properties p) {
        p.put("result_data", "");
        if (this.droppedFiles.size() > 0) {
            p.put("result_data", this.droppedFiles.remove(0).toString());
        }
        p.put("result", "ok");
    }

    private Color parseColorStr(String s) {
        if (s.length() == 7 && s.charAt(0) == '#') {
            try {
                int r = Integer.parseInt(s.substring(1, 3), 16);
                int g = Integer.parseInt(s.substring(3, 5), 16);
                int b = Integer.parseInt(s.substring(5, 7), 16);
                return new Color(r, g, b);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return Color.white;
    }

    public boolean machine_is_x() {
        return System.getProperties().getProperty("os.name", "").toUpperCase().equals("MAC OS X");
    }
}

