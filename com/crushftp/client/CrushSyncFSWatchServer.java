/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Base64;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

public class CrushSyncFSWatchServer {
    static Process proc = null;
    static int ssport = 15151;
    static String fs_path = System.getProperty("java.io.tmpdir");

    public static void main(String[] args) {
        int x = 0;
        while (args != null && x < args.length) {
            if (args[x].toLowerCase().startsWith("port=")) {
                ssport = Integer.parseInt(args[x].split("=")[1]);
            } else if (args[x].toLowerCase().startsWith("i") || args[x].toLowerCase().startsWith("-i")) {
                CrushSyncFSWatchServer.install_osx_service();
            } else if (args[x].toLowerCase().startsWith("r") || args[x].toLowerCase().startsWith("-r")) {
                CrushSyncFSWatchServer.remove_osx_service();
            }
            ++x;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){

            @Override
            public void run() {
                try {
                    proc.destroy();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                new File(String.valueOf(fs_path) + "/fswatch").delete();
            }
        }));
        final Vector listeners = new Vector();
        new Thread(new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                Thread.currentThread().setName("fswatch");
                try {
                    RandomAccessFile out = new RandomAccessFile(String.valueOf(fs_path) + "/fswatch", "rw");
                    GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode("H4sICIOu31IAA2Zzd2F0Y2gA7VpfaFtVGD83bVzXtU2Gw1Wdko1Vq0Jb5h661WKivfOOrV2XRi1l7K5pbpdAmoSbm9mKhfbh4q5ZJOCEgcP1RSaiUFFRiuv+PJgiBYs+OGSKIGoCTuqL6KiN33fOSXNzm5Y9+eL94J7v+53vfL/7nT+39+TcfnXn9uoWQoQaQqYchBA3XLNOQnTiISj3wyXBJcv9vufEIdF/jKwTYX3VOkGecQfyBMTBQJV4tyWAY8iNOLkmNA9NGdfKzax8S9sY3++OMq4189aSKTOU5aSWCiY35JtvYnwdFlySOs53TwWfHFaiCUWtwrfM40+ZsGPT/EbisaRmwpV8/S7GV2fCNWRjAb6kpkZipzfgG+R8z5qwWRyVEOJTsZcisZAciY3Gq/C9xfkkE95MZFkJy6Pq8JhSPb8852s24c36i+uuU0CeHl/AZ3K4LevOonG91ZAytyzHonJyYiwYj8oJTd2Qz23C5rxwfdSZsCyfjmsVuJLP7a6cV3cVvvqK+OjwZvk1cz6vCZv5sL/bTLj83B893HdE7DlcmgOPZdw8TEUdZR7z87EH/q508HbYlzpP+VlC6QQcBj3D65EG53YZcAPold2EJHhu/aRSHPyqJxvLLMTfW6W+iaeO92hPJdX2aCTYHpqIhrj/QZ7HpZ2XLl/WDqzOXX/9nb4/Aif2lrq+tZ5ej5PKP12t0F+MWzIPJr9PJ+fEnAXhg1ocqbV7wzUwkdSUsbZn2iCPSJDFPcL5l/j4PcbxTQ/zP2HCzcQWW2yxxZb/qzwvGT9L079NwUtG+qxUKaUb98LbJieuIDIm/5KM1LKUPiAZX+R/cqDf2QObAckQ81Km2wfmPDacw42zlBEXpExvXjJ6l3LiTXwH5cRb+C7KiYtFkJy4gAqCf5wl+VeAzhAXuzHedQ7fXtK8kGyeIoxqKT25KBk3oGEvNGStMp3U1/0t1IDjkIOi75BIP1/8p5hvAxPi8k2gr+LmwtCzWF/L6/8UaMSHAHP65+ChvdTPgQV86DX0swCkudL+Vcp8cg0q0pML+e/RnTOuYFxOf59Hp6nVRdlcmSy9gY4h0kgGPZKwIBn6DK2gTSUnjoxxw3iTojT1TXf+CmPp0h/AW5baYQbGdYPSFM7jyLHGjAFYZ2kmF3kmsyRNm3ZlMJez+qs0I9zSTF/BNrixcL19jTleo+W7gkvH30yFb2hPsJWTDdtFmlsG7+D6VOh++qPNs3uDrh2dNfc6JGct917AwWMl7a/w5Vq/WeO66b+RyaU/yea2VaBM2M90B2/FO+xcoasH18QRmuRUmQhG+8LaaGNF4fZqsVg4iUUbFMWWABAXW16g5SAth2h5gpYnaXmKlkG6LT2aeWg7GL6BYosAOot7T1L4BajCjWjdQgs3v4Wv0cIFXMD1Hd6H1jxaXrQ+RqsPrffQGkJrBiyBS3tIOdM+mlTOKDEtSUS//5j/YAk21pM1z0DA5w+IPWZfS+hgC2rMdxd/gs0aBxa3l/sJu642MR/auKVvgEh3eRPOjB0l9LKfCONuwd2wpS672Q93/jurnusdXD/MdSvX+7h+iutDrrs7D7DFFltsscUWW2yxxRZbbLHFFlv+O9m+p3mQaq+Mn65D8ZSWIMdVd9aLH8PYx+xgJBZSVDKVL3IRskRthoiRaDypEAAeAMp4REO7FexIfESLIugAEE8oMbQ7wU4oqhpXEXkRqZGYNopIAqQqwyG0+8FOKlowhR4iyMRJHPJYGG6gjKQ0RQ5DO0hn99hwJEZa8DSkhizt5P0pGXd+aMAjMDz2qDiOoKdSAvtih477KC7/38AuVr8W86gFt1nwfgvusmCfBR+24OMW/KIFyxaMBzx4Coedw2/A+L0Vj9G2EvYdlH929Druoq2HyFXGVabjaloNfJrZBPOpZZNams7SRLIpLE3eugX0L+PfRsFsIgAA")));
                    int bytes = 0;
                    byte[] b = new byte[10000];
                    while (bytes >= 0) {
                        bytes = in.read(b);
                        if (bytes <= 0) continue;
                        out.write(b, 0, bytes);
                    }
                    in.close();
                    out.close();
                    new File(String.valueOf(fs_path) + "/fswatch").deleteOnExit();
                    Runtime.getRuntime().exec(("chmod;+x;" + fs_path + "/fswatch").split(";"));
                    Runtime.getRuntime().exec(("chmod;777;" + fs_path + "/fswatch").split(";"));
                    proc = Runtime.getRuntime().exec((String.valueOf(fs_path) + "/fswatch").split(";"));
                    BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    String data = br.readLine();
                    System.out.println(data);
                    if (!data.startsWith("STARTED:")) {
                        System.exit(1);
                    }
                    while ((data = br.readLine()) != null) {
                        Vector vector = listeners;
                        synchronized (vector) {
                            int x = 0;
                            while (x < listeners.size()) {
                                Vector v = (Vector)listeners.elementAt(x);
                                if (v.size() < 1000) {
                                    v.addElement(data);
                                }
                                ++x;
                            }
                        }
                    }
                    br.close();
                    proc.waitFor();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    proc.destroy();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                System.exit(1);
            }
        }).start();
        ServerSocket ss = null;
        while (true) {
            try {
                while (true) {
                    if (ss == null) {
                        ss = new ServerSocket(ssport);
                    }
                    final Socket sock = ss.accept();
                    new Thread(new Runnable(){

                        /*
                         * WARNING - Removed try catching itself - possible behaviour change.
                         * Unable to fully structure code
                         */
                        @Override
                        public void run() {
                            block27: {
                                Thread.currentThread().setName("" + sock);
                                v = new Vector<E>();
                                try {
                                    try {
                                        out = sock.getOutputStream();
                                        in = sock.getInputStream();
                                        sock.setSoTimeout(500);
                                        var4_5 = listeners;
                                        synchronized (var4_5) {
                                            listeners.addElement(v);
                                            // MONITOREXIT @DISABLED, blocks:[0, 1, 2, 12] lbl12 : MonitorExitStatement: MONITOREXIT : var4_5
                                            if (true) ** GOTO lbl26
                                        }
                                        do {
                                            if (v.size() > 0) {
                                                out.write((String.valueOf(v.remove(0).toString()) + "\r\n").getBytes("UTF8"));
                                                continue;
                                            }
                                            try {
                                                in.read();
                                            }
                                            catch (SocketTimeoutException var4_6) {
                                                // empty catch block
                                            }
lbl26:
                                            // 4 sources

                                        } while (sock.isConnected());
                                    }
                                    catch (Exception var2_3) {
                                        var6_7 = listeners;
                                        synchronized (var6_7) {
                                            listeners.remove(v);
                                        }
                                        try {
                                            sock.close();
                                        }
                                        catch (Exception var6_8) {}
                                        break block27;
                                    }
                                }
                                catch (Throwable var5_13) {
                                    var6_9 = listeners;
                                    synchronized (var6_9) {
                                        listeners.remove(v);
                                    }
                                    try {
                                        sock.close();
                                    }
                                    catch (Exception var6_10) {
                                        // empty catch block
                                    }
                                    throw var5_13;
                                }
                                var6_11 = listeners;
                                synchronized (var6_11) {
                                    listeners.remove(v);
                                }
                                try {
                                    sock.close();
                                }
                                catch (Exception var6_12) {
                                    // empty catch block
                                }
                            }
                        }
                    }).start();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            break;
        }
    }

    public static void install_osx_service() {
        try {
            String plist2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\r\n<plist version=\"1.0\">\r\n\t<dict>\r\n\t\t<key>Label</key>\r\n\t\t<string>com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".fswatch</string>\r\n" + "\t\t<key>ProgramArguments</key>\r\n" + "\t\t<array>\r\n" + "\t\t\t<string>" + System.getProperty("java.home") + "/bin/java</string><string>-cp</string><string>" + new File("./CrushTunnel.jar").getCanonicalPath() + "</string><string>com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".client.CrushSyncFSWatchServer</string><string>port=" + ssport + "</string>\r\n" + "\t\t</array>\r\n" + "\t\t<key>RunAtLoad</key>\r\n" + "\t\t<true/>\r\n" + "\t</dict>\r\n" + "</plist>\r\n";
            RandomAccessFile plist_file = new RandomAccessFile("./com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".fswatch.plist", "rw");
            plist_file.setLength(0L);
            plist_file.write(plist2.getBytes("UTF8"));
            plist_file.close();
            RandomAccessFile out = new RandomAccessFile("fswatch_exec_root.sh", "rw");
            out.setLength(0L);
            out.write(("mv ./com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".fswatch.plist /Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".fswatch.plist\n").getBytes("UTF8"));
            out.write(("chmod 700 /Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".fswatch.plist\n").getBytes("UTF8"));
            out.write(("chown root /Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".fswatch.plist\n").getBytes("UTF8"));
            out.write(("chgrp wheel /Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".fswatch.plist\n").getBytes("UTF8"));
            out.write(("launchctl load -F -w /Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".fswatch.plist\n").getBytes("UTF8"));
            out.write(("launchctl start com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".fswatch\n").getBytes("UTF8"));
            out.close();
            File f = new File("fswatch_exec_root.sh");
            Process proc2 = Runtime.getRuntime().exec(new String[]{"chmod", "+x", f.getCanonicalPath()});
            proc2.waitFor();
            proc2.destroy();
            proc2 = Runtime.getRuntime().exec(new String[]{"osascript", "-e", "do shell script \"" + f.getCanonicalPath() + "\" with administrator privileges"});
            proc2.waitFor();
            proc2.destroy();
            f.delete();
            System.out.println("Installed com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".fswatch");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void remove_osx_service() {
        try {
            RandomAccessFile out = new RandomAccessFile("fswatch_exec_root.sh", "rw");
            out.setLength(0L);
            if (new File("/Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".fswatch.plist").exists()) {
                out.write(("launchctl stop com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".fswatch\n").getBytes("UTF8"));
                out.write(("launchctl remove com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".fswatch\n").getBytes("UTF8"));
            }
            out.close();
            File f = new File("fswatch_exec_root.sh");
            Process proc2 = Runtime.getRuntime().exec(new String[]{"chmod", "+x", f.getCanonicalPath()});
            proc2.waitFor();
            proc2.destroy();
            proc2 = Runtime.getRuntime().exec(new String[]{"osascript", "-e", "do shell script \"" + f.getCanonicalPath() + "\" with administrator privileges"});
            proc2.waitFor();
            proc2.destroy();
            new File("/Library/LaunchDaemons/com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".fswatch.plist").delete();
            f.delete();
            System.out.println("Removed com." + System.getProperty("appname", "CrushFTP").toLowerCase() + ".fswatch");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}

