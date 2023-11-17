/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.FileClient;
import com.crushftp.client.File_S;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class RFileClient
extends GenericClient {
    Socket sock = null;
    ObjectOutputStream oos = null;
    ObjectInputStream ois = null;
    Process proc = null;
    VRL vrl = null;
    static long last_activity = System.currentTimeMillis();
    static Object log_lock = new Object();
    static String server_port = "";

    public static void main(String[] args) {
        server_port = args[0];
        try {
            Properties p;
            RFileClient.write_log(new Date() + ":LAUNCHED:server_port:" + server_port);
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    while (true) {
                        try {
                            while (true) {
                                if (System.currentTimeMillis() - last_activity > 60000L) {
                                    last_activity = System.currentTimeMillis();
                                    RFileClient.write_log(Common.dumpStack("ServerPort:" + server_port + " Date:" + new Date()));
                                    last_activity = System.currentTimeMillis();
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
            });
            Socket sock = new Socket("127.0.0.1", Integer.parseInt(server_port));
            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
            String url = ois.readObject().toString();
            System.out.println(new VRL(url).getUsername());
            String header = ois.readObject().toString();
            Vector log = new Vector();
            FileClient fc = new FileClient(url, header, log);
            while ((p = (Properties)ois.readObject()) != null) {
                last_activity = System.currentTimeMillis();
                try {
                    Socket sock_tmp;
                    p.put("response", "");
                    if (p.getProperty("command").equals("list")) {
                        RFileClient.write_log(new Date() + ":Command:" + p.getProperty("command") + " " + p.getProperty("path"));
                        p.put("response", fc.list(p.getProperty("path"), (Vector)p.get("list")));
                    } else if (p.getProperty("command").equals("stat")) {
                        RFileClient.write_log(new Date() + ":Command:" + p.getProperty("command") + " " + p.getProperty("path"));
                        Properties o = fc.stat(p.getProperty("path"));
                        p.put("response", o == null ? "null" : o);
                    } else if (p.getProperty("command").equals("mdtm")) {
                        RFileClient.write_log(new Date() + ":Command:" + p.getProperty("command") + " " + p.getProperty("path") + " " + p.getProperty("modified"));
                        p.put("response", String.valueOf(fc.mdtm(p.getProperty("path"), Long.parseLong(p.getProperty("modified")))));
                    } else if (p.getProperty("command").equals("rename")) {
                        RFileClient.write_log(new Date() + ":Command:" + p.getProperty("command") + " RNFR:" + p.getProperty("rnfr0") + " RNTO:" + p.getProperty("rnto0"));
                        p.put("response", String.valueOf(fc.rename(p.getProperty("rnfr0"), p.getProperty("rnto0"), false)));
                    } else if (p.getProperty("command").equals("delete")) {
                        RFileClient.write_log(new Date() + ":Command:" + p.getProperty("command") + " " + p.getProperty("path"));
                        p.put("response", String.valueOf(fc.delete(p.getProperty("path"))));
                    } else if (p.getProperty("command").equals("makedir")) {
                        RFileClient.write_log(new Date() + ":Command:" + p.getProperty("command") + " " + p.getProperty("path"));
                        p.put("response", String.valueOf(fc.makedir(p.getProperty("path"))));
                    } else if (p.getProperty("command").equals("makedirs")) {
                        RFileClient.write_log(new Date() + ":Command:" + p.getProperty("command") + " " + p.getProperty("path"));
                        p.put("response", String.valueOf(fc.makedirs(p.getProperty("path"))));
                    } else if (p.getProperty("command").equals("setMod")) {
                        RFileClient.write_log(new Date() + ":Command:" + p.getProperty("command") + " " + p.getProperty("path") + " " + p.getProperty("val") + " " + p.getProperty("param"));
                        fc.setMod(p.getProperty("path"), p.getProperty("val"), p.getProperty("param"));
                    } else if (p.getProperty("command").equals("setOwner")) {
                        RFileClient.write_log(new Date() + ":Command:" + p.getProperty("command") + " " + p.getProperty("path") + " " + p.getProperty("val") + " " + p.getProperty("param"));
                        fc.setOwner(p.getProperty("path"), p.getProperty("val"), p.getProperty("param"));
                    } else if (p.getProperty("command").equals("setGroup")) {
                        RFileClient.write_log(new Date() + ":Command:" + p.getProperty("command") + " " + p.getProperty("path") + " " + p.getProperty("val") + " " + p.getProperty("param"));
                        fc.setGroup(p.getProperty("path"), p.getProperty("val"), p.getProperty("param"));
                    } else if (p.getProperty("command").equals("doCommand")) {
                        RFileClient.write_log(new Date() + ":Command:" + p.getProperty("command") + " " + p.getProperty("command2"));
                        p.put("response", fc.doCommand(p.getProperty("command2")));
                    } else if (p.getProperty("command").equals("download3")) {
                        RFileClient.write_log(new Date() + ":Command:" + p.getProperty("command") + " " + p.getProperty("path"));
                        InputStream in_tmp = fc.download3(p.getProperty("path"), Long.parseLong(p.getProperty("startPos")), Long.parseLong(p.getProperty("endPos")), p.getProperty("binary").equals("true"));
                        sock_tmp = new Socket("127.0.0.1", Integer.parseInt(p.getProperty("port")));
                        Common.streamCopier(in_tmp, sock_tmp.getOutputStream(), true, true, true);
                        p.put("response", "");
                    } else if (p.getProperty("command").equals("upload3")) {
                        RFileClient.write_log(new Date() + ":Command:" + p.getProperty("command") + " " + p.getProperty("path"));
                        OutputStream out_tmp = fc.upload3(p.getProperty("path"), Long.parseLong(p.getProperty("startPos")), p.getProperty("truncate").equals("true"), p.getProperty("binary").equals("true"));
                        sock_tmp = new Socket("127.0.0.1", Integer.parseInt(p.getProperty("port")));
                        Common.streamCopier(sock_tmp.getInputStream(), out_tmp, true, true, true);
                        p.put("response", "");
                    } else if (p.getProperty("command").equals("logout")) {
                        RFileClient.write_log(new Date() + ":Command:" + p.getProperty("command"));
                        fc.logout();
                    } else if (p.getProperty("command").equals("freeCache")) {
                        RFileClient.write_log(new Date() + ":Command:" + p.getProperty("command"));
                        fc.freeCache();
                    }
                }
                catch (Exception e) {
                    RFileClient.write_log(new Date() + ":ERROR:" + e);
                    p.put("error", e);
                    e.printStackTrace();
                }
                p.put("log", log);
                oos.reset();
                oos.writeObject(p);
                log.removeAllElements();
            }
            sock.close();
        }
        catch (EOFException sock) {
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(5000L);
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void write_log(String s) throws Exception {
        if (new File("./RFILE/").exists()) {
            Object object = log_lock;
            synchronized (object) {
                RandomAccessFile raf;
                new File("./RFILE/").mkdir();
                String log_file = "./RFILE/" + server_port + ".txt";
                if (new File(log_file).exists() && System.currentTimeMillis() - new File(log_file).lastModified() > 70000L) {
                    new File(log_file).delete();
                }
                if ((raf = new RandomAccessFile(log_file, "rw")).length() > 0x500000L) {
                    raf.setLength(0L);
                }
                try {
                    raf.seek(raf.length());
                    raf.write(s.getBytes());
                }
                finally {
                    raf.close();
                }
            }
        }
    }

    public RFileClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"*"};
        this.url = url;
        this.vrl = new VRL(url);
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(0, 10, InetAddress.getByName("127.0.0.1"));
            ss.setSoTimeout(30000);
            File f = new File(String.valueOf(System.getProperty("crushftp.home"))).getCanonicalFile();
            String java_binary = "java";
            if (new File_S(String.valueOf(System.getProperty("crushftp.home")) + "Java/bin/java.exe").exists()) {
                java_binary = new File_S(String.valueOf(System.getProperty("crushftp.home")) + "Java/bin/java.exe").getCanonicalPath();
            }
            System.out.println(new Date() + "|Using java:" + java_binary + " for local port:" + ss.getLocalPort());
            this.log("Using java:" + java_binary + " for local port:" + ss.getLocalPort());
            String[] args = new String[]{"psexec.exe", "-accepteula", "-i", "-x", "-e", "-d", "-u", this.vrl.getUsername(), "-p", this.vrl.getPassword(), "-w", f.getCanonicalPath(), java_binary, "-Xmx32M", "-cp", "WebInterface/CrushTunnel.jar", "com.crushftp.client.RFileClient", String.valueOf(ss.getLocalPort())};
            String args_str = "";
            int x = 0;
            while (x < args.length) {
                args_str = x != 9 ? String.valueOf(args_str) + args[x] + " " : String.valueOf(args_str) + "***password*** ";
                ++x;
            }
            this.log("Launching RFile with args:" + args_str.trim());
            this.proc = Runtime.getRuntime().exec(args, null, f);
            this.log_and_print_stream(this.proc.getErrorStream());
            this.log_and_print_stream(this.proc.getInputStream());
            try {
                this.sock = ss.accept();
            }
            catch (SocketTimeoutException e) {
                this.log_and_print_stream(this.proc.getErrorStream());
                this.log_and_print_stream(this.proc.getInputStream());
                throw e;
            }
            this.log("Got RFile socket connection:" + ss.getLocalPort() + " for user:" + this.vrl.getUsername());
            this.oos = new ObjectOutputStream(this.sock.getOutputStream());
            this.oos.writeObject(url);
            this.oos.writeObject(header);
            this.oos.flush();
            this.ois = new ObjectInputStream(this.sock.getInputStream());
        }
        catch (Exception e) {
            try {
                ss.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            System.out.println(new Date() + "|" + e);
            e.printStackTrace();
        }
        try {
            ss.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void log_and_print_stream(InputStream read_in) throws IOException {
        int bytes_read = 0;
        byte[] b = new byte[32768];
        while (bytes_read >= 0 && read_in.available() > 0) {
            bytes_read = this.proc.getInputStream().read(b);
            if (bytes_read <= 0) continue;
            this.log("RFile:" + new String(b, 0, bytes_read));
            System.out.println(new String(b, 0, bytes_read));
        }
    }

    @Override
    public void logout() throws Exception {
        Properties p = new Properties();
        try {
            p.put("command", "logout");
            this.oos.writeObject(p);
            this.log("Issued command:" + p);
            p = this.getResult();
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (this.oos != null) {
            this.oos.close();
        }
        if (this.ois != null) {
            this.ois.close();
        }
        if (this.sock != null) {
            this.sock.close();
        }
    }

    public void freeCache() {
        Properties p = new Properties();
        try {
            p.put("command", "freeCache");
            this.oos.writeObject(p);
            this.log("Issued command:" + p);
            p = this.getResult();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Override
    public Properties stat(String path) throws Exception {
        Properties p = new Properties();
        p.put("command", "stat");
        p.put("path", path);
        this.oos.writeObject(p);
        this.log("Issued command:" + p);
        p = this.getResult();
        Object o = p.get("response");
        if (o instanceof String && o.toString().equals("null")) {
            o = null;
        }
        return (Properties)o;
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        Properties p = new Properties();
        p.put("command", "list");
        p.put("path", path);
        p.put("list", list);
        this.oos.writeObject(p);
        this.log("Issued command:" + p);
        p = this.getResult();
        Vector v = (Vector)p.get("response");
        if (list != null && list.size() == 0) {
            list.addAll(v);
        } else {
            list = v;
        }
        int x = list.size() - 1;
        while (x >= 0) {
            Properties dir_item = (Properties)list.elementAt(x);
            if (dir_item.getProperty("modified", "0").equals("0")) {
                list.remove(x);
            }
            --x;
        }
        return list;
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        Properties p = new Properties();
        p.put("command", "download3");
        p.put("path", path);
        p.put("startPos", String.valueOf(startPos));
        p.put("endPos", String.valueOf(endPos));
        p.put("binary", String.valueOf(binary));
        ServerSocket ss = new ServerSocket(0);
        ss.setSoTimeout(10000);
        p.put("port", String.valueOf(ss.getLocalPort()));
        Socket sock_tmp = null;
        try {
            try {
                this.oos.writeObject(p);
                this.log("Issued command:" + p);
                sock_tmp = ss.accept();
                ss.close();
                p = this.getResult();
            }
            catch (Exception e) {
                if (sock_tmp != null) {
                    sock_tmp.close();
                }
                throw e;
            }
        }
        finally {
            ss.close();
        }
        return sock_tmp.getInputStream();
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        Properties p = new Properties();
        p.put("command", "mdtm");
        p.put("path", path);
        p.put("modified", String.valueOf(modified));
        this.oos.writeObject(p);
        this.log("Issued command:" + p);
        p = this.getResult();
        return p.get("response").toString().equals("true");
    }

    @Override
    public boolean rename(String rnfr0, String rnto0, boolean overwrite) throws Exception {
        Properties p = new Properties();
        p.put("command", "rename");
        p.put("rnfr0", rnfr0);
        p.put("rnto0", rnto0);
        this.oos.writeObject(p);
        this.log("Issued command:" + p);
        p = this.getResult();
        return p.get("response").toString().equals("true");
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        Properties p = new Properties();
        p.put("command", "upload3");
        p.put("path", path);
        p.put("startPos", String.valueOf(startPos));
        p.put("truncate", String.valueOf(truncate));
        p.put("binary", String.valueOf(binary));
        ServerSocket ss = new ServerSocket(0);
        ss.setSoTimeout(10000);
        p.put("port", String.valueOf(ss.getLocalPort()));
        Socket sock_tmp = null;
        try {
            try {
                this.oos.writeObject(p);
                this.log("Issued command:" + p);
                sock_tmp = ss.accept();
                ss.close();
                p = this.getResult();
            }
            catch (Exception e) {
                if (sock_tmp != null) {
                    sock_tmp.close();
                }
                throw e;
            }
        }
        finally {
            ss.close();
        }
        return sock_tmp.getOutputStream();
    }

    @Override
    public boolean delete(String path) {
        Properties p = new Properties();
        try {
            p.put("command", "delete");
            p.put("path", path);
            this.oos.writeObject(p);
            this.log("Issued command:" + p);
            p = this.getResult();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return p.get("response").toString().equals("true");
    }

    @Override
    public boolean makedir(String path0) {
        Properties p = new Properties();
        try {
            p.put("command", "makedir");
            p.put("path", path0);
            this.oos.writeObject(p);
            this.log("Issued command:" + p);
            p = this.getResult();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return p.get("response").toString().equals("true");
    }

    @Override
    public boolean makedirs(String path) {
        Properties p = new Properties();
        try {
            p.put("command", "makedirs");
            p.put("path", path);
            this.oos.writeObject(p);
            this.log("Issued command:" + p);
            p = this.getResult();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return p.get("response").toString().equals("true");
    }

    @Override
    public void setMod(String path, String val, String param) {
        Properties p = new Properties();
        try {
            p.put("command", "setMod");
            p.put("path", path);
            p.put("val", val);
            p.put("param", param);
            this.oos.writeObject(p);
            this.log("Issued command:" + p);
            p = this.getResult();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Override
    public void setOwner(String path, String val, String param) {
        Properties p = new Properties();
        try {
            p.put("command", "setOwner");
            p.put("path", path);
            p.put("val", val);
            p.put("param", param);
            this.oos.writeObject(p);
            this.log("Issued command:" + p);
            p = this.getResult();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Override
    public void setGroup(String path, String val, String param) {
        Properties p = new Properties();
        try {
            p.put("command", "setGroup");
            p.put("path", path);
            p.put("val", val);
            p.put("param", param);
            this.oos.writeObject(p);
            this.log("Issued command:" + p);
            p = this.getResult();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void doOSCommand(String app, String param, String val, String path) {
    }

    @Override
    public String doCommand(String command) throws Exception {
        Properties p = new Properties();
        p.put("command", "doCommand");
        p.put("command2", command);
        this.oos.writeObject(p);
        this.log("Issued command:" + p);
        p = this.getResult();
        return p.getProperty("response", "");
    }

    private Properties getResult() throws Exception {
        Properties p = (Properties)this.ois.readObject();
        Vector log = (Vector)p.remove("log");
        int x = 0;
        while (x < log.size()) {
            this.log(log.remove(0).toString());
            ++x;
        }
        if (p.get("error") != null) {
            throw new Exception((Exception)p.get("error"));
        }
        return p;
    }
}

