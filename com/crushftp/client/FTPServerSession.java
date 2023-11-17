/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.HTTPClient;
import com.crushftp.client.Worker;
import com.crushftp.client.ZipTransfer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class FTPServerSession {
    Properties t = null;
    SimpleDateFormat sdf_yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss");
    SimpleDateFormat MMMddHHmm = new SimpleDateFormat("MMM dd HH:mm");
    SimpleDateFormat ddHHmmyyyy = new SimpleDateFormat("MMM dd  yyyy");
    SimpleDateFormat yyyySDF = new SimpleDateFormat("yyyy");

    public static void main(String[] args) {
        Properties t = new Properties();
        int x = 0;
        while (x < args.length) {
            String[] s = args[x].split(";");
            int xx = 0;
            while (xx < s.length) {
                String key = s[xx].split("=")[0].trim();
                String val = "";
                try {
                    val = s[xx].split("=")[1].trim();
                }
                catch (Exception exception) {}
                while (key.startsWith("-")) {
                    key = key.substring(1);
                }
                if (val.startsWith("\"") && val.endsWith("\"")) {
                    val = val.substring(1, val.length() - 1);
                }
                t.put(key.toUpperCase(), val);
                t.put(key, val);
                ++xx;
            }
            ++x;
        }
        if (!t.containsKey("URL") && t.containsKey("PROTOCOL")) {
            t.put("URL", String.valueOf(t.getProperty("PROTOCOL")) + "://" + t.getProperty("HOST") + ":" + t.getProperty("PORT") + t.getProperty("PATH", "/"));
        }
        if (t.getProperty("trustall", "true").equals("true")) {
            Common.trustEverything();
        }
        FTPServerSession f = new FTPServerSession();
        f.go(t);
    }

    public void go(Properties t) {
        this.t = t;
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(Integer.parseInt(t.getProperty("port", "55555")));
            while (true) {
                Socket sock = ss.accept();
                Worker.startWorker(new FTPSession(sock));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public class FTPSession
    implements Runnable {
        Socket sock = null;
        BufferedReader in = null;
        OutputStream out = null;
        Properties ui = new Properties();
        HTTPClient http;

        public FTPSession(Socket sock) {
            this.http = new HTTPClient(FTPServerSession.this.t.getProperty("URL", "http://127.0.0.1:8080/"), "", null);
            this.sock = sock;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("Tunnel FTPSession:" + this.sock);
            try {
                this.in = new BufferedReader(new InputStreamReader(this.sock.getInputStream(), "UTF8"));
                this.out = this.sock.getOutputStream();
                this.ui.put("dir", "/");
                this.ui.put("rest", "0");
                this.write("220 Welcome.");
                while (this.in != null) {
                    String command = this.in.readLine();
                    System.out.println(command);
                    String data = "";
                    if (command.indexOf(" ") >= 0) {
                        data = command.substring(command.indexOf(" ") + 1);
                        command = command.substring(0, command.indexOf(" "));
                    }
                    if (this.processCommand(command = command.toUpperCase(), data)) continue;
                }
                this.sock.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.finishUploads();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean processCommand(String command, String data) throws Exception {
            if (this.sock.isClosed()) {
                return false;
            }
            if (command.equals("QUIT")) {
                this.write("221 Goodbye.");
                return false;
            }
            if (command.equals("USER")) {
                this.ui.put("user", data);
                this.write("331 Username OK.  Need password.");
            } else if (command.equals("PASS")) {
                this.ui.put("pass", data);
                if (this.http.login(this.G("user"), this.G("pass"), "").toUpperCase().indexOf("SUCCESS") >= 0) {
                    this.write("230 Password OK.  Connected.");
                } else {
                    this.write("550 Login failed.");
                }
            } else if (command.equals("SYST")) {
                this.write("215 UNIX Type: L8");
            } else if (command.equals("FEAT")) {
                this.write("211-Extensions supported:");
                this.write(" EPSV");
                this.write(" EPRT");
                this.write(" SIZE");
                this.write(" MDTM");
                this.write(" REST STREAM");
                this.write("211 END");
            } else if (command.equals("NOOP")) {
                this.write("200 Command OK. (NOOP)");
            } else if (command.equals("PWD")) {
                this.write("257 \"" + this.G("dir") + "\" PWD command successful.");
            } else if (command.equals("CWD")) {
                Properties stat;
                if (!data.endsWith("/")) {
                    data = String.valueOf(data) + "/";
                }
                if ((stat = this.http.stat(data = this.dir(data))) != null && stat.getProperty("type").equals("DIR")) {
                    this.ui.put("dir", data);
                    this.write("250 \"" + this.G("dir") + "\" CWD command successful.");
                } else {
                    this.write("550 No such directory.");
                }
            } else if (command.equals("TYPE")) {
                this.write("200 Command OK : " + (data.toUpperCase().startsWith("A") ? "ASCII" : "BINARY") + " type selected.");
            } else if (command.equals("PASV") || command.equals("EPSV")) {
                this.ui.remove("port_ip");
                this.ui.remove("port_port");
                if (this.ui.containsKey("pasv")) {
                    ((ServerSocket)this.ui.remove("pasv")).close();
                }
                ServerSocket pasv = new ServerSocket(0);
                int the_port = pasv.getLocalPort();
                this.ui.put("pasv", pasv);
                if (command.equals("EPSV")) {
                    this.write("229 Entering Extended Passive Mode (|||" + the_port + "|)");
                } else {
                    this.write("227 Entering Passive Mode (127,0,0,1," + the_port / 256 + "," + (the_port - the_port / 256 * 256) + ")");
                }
            } else if (command.equals("PORT")) {
                if (this.ui.containsKey("pasv")) {
                    ((ServerSocket)this.ui.remove("pasv")).close();
                }
                String port_ip = data.substring(0, data.lastIndexOf(","));
                port_ip = port_ip.substring(0, port_ip.lastIndexOf(",")).replace(',', '.');
                String port_port = data.substring(port_ip.length() + 1);
                port_port = String.valueOf(Integer.parseInt(port_port.split(",")[0]) * 256 + Integer.parseInt(port_port.split(",")[1]));
                this.ui.put("port_ip", port_ip);
                this.ui.put("port_port", port_port);
                this.write("200 PORT command successful. " + port_ip + ":" + port_port);
            } else if (command.equals("EPRT")) {
                if (this.ui.containsKey("pasv")) {
                    ((ServerSocket)this.ui.remove("pasv")).close();
                }
                String port_ip = data.split("|")[2];
                String port_port = data.split("|")[3];
                this.ui.put("port_ip", port_ip);
                this.ui.put("port_port", port_port);
                this.write("200 PORT command successful. " + port_ip + ":" + port_port);
            } else if (command.equals("SIZE")) {
                Properties stat = this.http.stat(this.dir(data));
                if (stat != null && stat.getProperty("type").equals("FILE")) {
                    this.write("213 " + stat.getProperty("size"));
                } else if (stat != null) {
                    this.write("550 " + data + ": not a plain file.");
                } else {
                    this.write("550 No such file.");
                }
            } else if (command.equals("REST")) {
                long pos = Long.parseLong(data);
                this.ui.put("rest", String.valueOf(pos));
                this.write("350 Restarting at " + pos + ". Send STORE or RETRIEVE to initiate transfer.");
            } else if (command.equals("DELE")) {
                Properties stat = this.http.stat(this.dir(data));
                if (stat != null && stat.getProperty("type").equals("FILE")) {
                    this.http.delete(this.dir(data));
                    this.write("250 \"" + data + "\" delete successful.");
                } else if (stat != null) {
                    this.write("550 " + data + ": not a plain file.");
                } else {
                    this.write("550 No such file.");
                }
            } else if (command.equals("RMD")) {
                Properties stat = this.http.stat(this.dir(data));
                if (stat != null && stat.getProperty("type").equals("DIR")) {
                    this.http.delete(this.dir(data));
                    stat = this.http.stat(this.dir(data));
                    if (stat == null) {
                        this.write("250 \"" + data + "\" deleted.");
                    } else {
                        this.write("550 Directory not empty, or directory is locked.");
                    }
                } else {
                    this.write("550 No such file.");
                }
            } else if (command.equals("MKD")) {
                Properties stat = this.http.stat(this.dir(data));
                if (stat != null) {
                    this.write("521 \"" + data + "\" already exists.");
                } else {
                    this.http.makedir(this.dir(data));
                    this.write("257 \"" + data + "\" directory created.");
                }
            } else if (command.equals("MDTM")) {
                String dateNumber = "";
                if (!data.equals("")) {
                    if (data.lastIndexOf(" ") >= 0) {
                        dateNumber = data.substring(data.lastIndexOf(" ")).trim();
                    }
                    try {
                        Long.parseLong(dateNumber);
                        if (dateNumber.length() > 5) {
                            dateNumber = String.valueOf(Long.parseLong(dateNumber.trim()));
                            data = data.substring(0, data.length() - dateNumber.length()).trim();
                        } else {
                            dateNumber = "";
                        }
                    }
                    catch (Exception e) {
                        if (data.indexOf(" ") >= 0) {
                            dateNumber = data.substring(0, data.indexOf(" ")).trim();
                        }
                        try {
                            Long.parseLong(dateNumber);
                            if (dateNumber.length() > 5) {
                                dateNumber = String.valueOf(Long.parseLong(dateNumber.trim()));
                                data = data.substring(dateNumber.length() + 1);
                            } else {
                                dateNumber = "";
                            }
                        }
                        catch (Exception ee) {
                            dateNumber = "";
                        }
                    }
                }
                boolean ok = false;
                if (dateNumber.trim().length() > 0 && this.http.mdtm(this.dir(data), FTPServerSession.this.sdf_yyyyMMddHHmmss.parse(dateNumber).getTime())) {
                    ok = true;
                }
                if (dateNumber.trim().length() == 0) {
                    ok = true;
                }
                if (ok) {
                    Properties stat = this.http.stat(this.dir(data));
                    if (stat != null) {
                        this.write("213 " + FTPServerSession.this.sdf_yyyyMMddHHmmss.format(new Date(Long.parseLong(stat.getProperty("modified")))));
                    } else {
                        this.write("550 No such file.");
                    }
                } else {
                    this.write("550 Unable to set last modified date and time.");
                }
            } else if (command.equals("RNFR")) {
                Properties stat = this.http.stat(this.dir(data));
                if (stat != null) {
                    this.ui.put("rnfr", this.dir(data));
                    this.write("350 File exists, ready for new name.");
                } else {
                    this.write("550 No such file.");
                }
            } else if (command.equals("RNTO")) {
                Properties stat = this.http.stat(this.dir(data));
                if (stat == null) {
                    if (this.http.rename(this.G("rnfr"), this.dir(data), false)) {
                        this.write("250 File renamed OK.");
                    } else {
                        this.write("550 Rename failed. (File locked or bad path?)");
                    }
                } else {
                    this.write("550 File already exists.");
                }
            } else if (command.trim().endsWith("ABOR")) {
                if (this.ui.containsKey("data_sock")) {
                    ((Socket)this.ui.remove("data_sock")).close();
                }
                this.write("225 ABOR command successful.");
            } else if (command.equals("LIST")) {
                if (!data.startsWith("/")) {
                    data = "";
                }
                StringBuffer item_str = new StringBuffer();
                this.finishUploads();
                Vector v = new Vector();
                this.http.list(this.dir(data), v);
                int x = 0;
                while (x < v.size()) {
                    Properties item = (Properties)v.elementAt(x);
                    item_str.append(item.getProperty("privs"));
                    item_str.append(String.valueOf(Common.lpad(item.getProperty("count"), 4)) + " ");
                    item_str.append(String.valueOf(Common.rpad(item.getProperty("owner"), 8)) + " ");
                    item_str.append(String.valueOf(Common.rpad(item.getProperty("group"), 8)) + " ");
                    item_str.append(String.valueOf(Common.lpad(String.valueOf(item.getProperty("size")), 13)) + " ");
                    Date d = new Date(Long.parseLong(item.getProperty("modified")));
                    if (FTPServerSession.this.yyyySDF.format(d).equals(FTPServerSession.this.yyyySDF.format(new Date()))) {
                        item_str.append(String.valueOf(FTPServerSession.this.MMMddHHmm.format(d)) + " ");
                    } else {
                        item_str.append(String.valueOf(FTPServerSession.this.ddHHmmyyyy.format(d)) + " ");
                    }
                    item_str.append(String.valueOf(item.getProperty("name")) + "\r\n");
                    ++x;
                }
                String dot = "drwxrwxrwx" + Common.lpad("1", 4) + " " + Common.rpad("user", 8) + " " + Common.rpad("group", 8) + " " + Common.lpad("0", 13) + " " + FTPServerSession.this.MMMddHHmm.format(new Date()) + " ";
                item_str.insert(0, String.valueOf(dot) + "..\r\n");
                item_str.insert(0, String.valueOf(dot) + ".\r\n");
                this.write("150 Opening data connection for file list.");
                Socket data_sock = this.getDataSock();
                System.out.println(item_str.toString().trim());
                data_sock.getOutputStream().write(item_str.toString().getBytes("UTF8"));
                data_sock.close();
                this.ui.remove("data_sock");
                this.write("226 Directory transfer complete.");
            } else if (command.equals("STOR") || command.equals("APPE")) {
                boolean ok = true;
                if (!this.ui.containsKey("zipUpload")) {
                    ok = false;
                    Properties stat = this.http.stat(Common.all_but_last(this.dir(data)));
                    if (stat == null) {
                        this.write("550 No such directory.");
                    } else {
                        ok = true;
                    }
                }
                if (ok) {
                    final String data2 = data;
                    final String command2 = command;
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            Thread.currentThread().setName("Tunnel FTPSession STOR/APPE:" + FTPSession.this.sock + " data2:" + data2);
                            Socket data_sock = null;
                            ZipTransfer zip = null;
                            try {
                                FTPSession.this.write("150 Opening BINARY mode data connection.  Ready to write file \"" + FTPSession.this.dir(data2) + "\"");
                                data_sock = FTPSession.this.getDataSock();
                                if (!FTPSession.this.ui.containsKey("zipUpload")) {
                                    zip = FTPSession.this.http.getZipTransfer("/", null, false);
                                    FTPSession.this.ui.put("zipUpload", zip);
                                    zip.openUpload();
                                }
                                if (command2.equals("APPE")) {
                                    Properties stat = FTPSession.this.http.stat(FTPSession.this.dir(data2));
                                    FTPSession.this.ui.put("rest", stat.getProperty("size"));
                                }
                                zip = (ZipTransfer)FTPSession.this.ui.get("zipUpload");
                                String addOn = ":R=" + Common.makeBoundary(3);
                                if (!FTPSession.this.G("rest").equals("0")) {
                                    addOn = String.valueOf(addOn) + ";REST=" + FTPSession.this.G("rest");
                                }
                                zip.addUploadFile(String.valueOf(FTPSession.this.dir(data2)) + addOn, 0L);
                                FTPSession.this.ui.put("rest", "0");
                                BufferedInputStream din = new BufferedInputStream(data_sock.getInputStream());
                                int bytesRead = 0;
                                byte[] b = new byte[65535];
                                long bytesTransferred = 0L;
                                while (bytesRead >= 0) {
                                    bytesRead = din.read(b);
                                    if (bytesRead < 0) continue;
                                    zip.write(b, 0, bytesRead);
                                    bytesTransferred += (long)bytesRead;
                                }
                                din.close();
                                data_sock.close();
                                FTPSession.this.write("226 Transfer complete. (\"" + FTPSession.this.dir(data2) + "\" " + bytesTransferred + ") STOR");
                            }
                            catch (Exception e) {
                                FTPSession.this.ui.remove("data_sock");
                                try {
                                    if (data_sock != null && !data_sock.isClosed()) {
                                        e.printStackTrace();
                                        data_sock.close();
                                        FTPSession.this.write("550 Error during transfer:" + e.getMessage());
                                    } else {
                                        FTPSession.this.write("550 Transfer aborted.");
                                    }
                                    if (zip != null) {
                                        zip.urlc.disconnect();
                                    }
                                    FTPSession.this.ui.remove("zipUpload");
                                }
                                catch (Exception ee) {
                                    Common.log("FTP_CLIENT", 3, ee);
                                }
                            }
                        }
                    });
                }
            } else if (command.equals("RETR")) {
                Properties stat = this.http.stat(this.dir(data));
                if (stat == null) {
                    this.write("550 No such file.");
                } else {
                    final String data2 = data;
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            Thread.currentThread().setName("Tunnel FTPSession RETR:" + FTPSession.this.sock + " data2:" + data2);
                            Socket data_sock = null;
                            ZipTransfer zip = null;
                            try {
                                zip = FTPSession.this.http.getZipTransfer("/", null, false);
                                String filters = "";
                                if (!FTPSession.this.G("rest").equals("0")) {
                                    filters = String.valueOf(filters) + FTPSession.this.dir(data2) + ":" + FTPSession.this.G("rest");
                                }
                                FTPSession.this.ui.put("rest", "0");
                                Vector<String> files = new Vector<String>();
                                files.addElement(FTPSession.this.dir(data2));
                                zip.openDownload(files, filters);
                                zip.getDownloadFile();
                                FTPSession.this.write("150 Opening BINARY mode data connection.  Ready to read file \"" + FTPSession.this.dir(data2) + "\"");
                                data_sock = FTPSession.this.getDataSock();
                                BufferedOutputStream dout = new BufferedOutputStream(data_sock.getOutputStream());
                                int bytesRead = 0;
                                byte[] b = new byte[65535];
                                long bytesTransferred = 0L;
                                while (bytesRead >= 0) {
                                    bytesRead = zip.read(b);
                                    if (bytesRead < 0) continue;
                                    dout.write(b, 0, bytesRead);
                                    bytesTransferred += (long)bytesRead;
                                }
                                zip.closeDownload();
                                dout.close();
                                data_sock.close();
                                FTPSession.this.ui.remove("data_sock");
                                FTPSession.this.write("226 Transfer complete. (\"" + FTPSession.this.dir(data2) + "\" " + bytesTransferred + ") RETR");
                            }
                            catch (Exception e) {
                                FTPSession.this.ui.remove("data_sock");
                                try {
                                    if (data_sock != null && !data_sock.isClosed()) {
                                        e.printStackTrace();
                                        data_sock.close();
                                        FTPSession.this.write("550 Error during transfer:" + e.getMessage());
                                    } else {
                                        FTPSession.this.write("550 Transfer aborted.");
                                    }
                                    if (zip != null) {
                                        zip.urlc.disconnect();
                                    }
                                }
                                catch (Exception ee) {
                                    Common.log("FTP_CLIENT", 3, ee);
                                }
                            }
                        }
                    });
                }
            } else {
                this.write("550 Unknown Command.");
            }
            return true;
        }

        public String dir(String data) {
            if (!data.startsWith("/")) {
                data = String.valueOf(this.G("dir")) + data;
            }
            data = Common.dots(data);
            return data;
        }

        public void finishUploads() throws Exception {
            ZipTransfer zip = (ZipTransfer)this.ui.get("zipUpload");
            if (zip != null) {
                zip.closeUpload();
                this.ui.remove("zipUpload");
            }
        }

        public Socket getDataSock() throws Exception {
            Socket data_sock = null;
            if (this.ui.containsKey("pasv")) {
                ServerSocket pasv = (ServerSocket)this.ui.remove("pasv");
                data_sock = pasv.accept();
                pasv.close();
            } else {
                data_sock = new Socket(this.ui.remove("port_ip").toString(), Integer.parseInt(this.ui.remove("port_port").toString()));
            }
            this.ui.put("data_sock", data_sock);
            return data_sock;
        }

        public String G(String key) {
            return this.ui.getProperty(key, "");
        }

        public void write(String s) throws Exception {
            System.out.println(s);
            this.out.write((String.valueOf(s) + "\r\n").getBytes("UTF8"));
        }
    }
}

