/*
 * Decompiled with CFR 0.152.
 */
import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import crushftp.gui.LOC;
import crushftp.handlers.Log;
import crushftp.server.AdminControls;
import crushftp.server.ServerStatus;
import crushftp.server.daemon.ServerBeat;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class CrushFTPLauncher {
    static {
        System.setProperty("Syslog.classloader.warning", "off");
    }

    public CrushFTPLauncher(Object o) {
        block81: {
            String defaultUserFolder;
            String[] args;
            block83: {
                block82: {
                    block80: {
                        Common.log = new Vector();
                        if (System.getProperties().containsKey("crushftp.log.append")) {
                            Common.log = (Vector)System.getProperties().remove("crushftp.log.append");
                        }
                        args = (String[])o;
                        crushftp.handlers.Common.initSystemProperties(true);
                        System.getProperties().put("crushftp.version", "6");
                        defaultUserFolder = "MainUsers";
                        if (!(args != null && args.length != 0 || System.getProperty("crushftp.cli_args", "").equalsIgnoreCase(""))) {
                            args = System.getProperty("crushftp.cli_args", "").split(" ");
                        }
                        if (args == null || args.length == 0) {
                            args = new String[]{"-g"};
                        }
                        if (!args[0].toUpperCase().startsWith("-DMZI")) break block80;
                        crushftp.handlers.Common common_code = new crushftp.handlers.Common();
                        if (crushftp.handlers.Common.machine_is_x()) {
                            common_code.install_osx_service();
                        } else if (crushftp.handlers.Common.machine_is_windows()) {
                            Common.install_windows_service(512, "CrushFTP", "plugins/lib/CrushFTPJarProxy.jar");
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            try {
                                Common.copyStreams(new FileInputStream(new File_S("service/CrushFTPService.ini")), baos, true, true);
                                String s = new String(baos.toByteArray(), "UTF8");
                                s = crushftp.handlers.Common.replace_str(s, "arg.1=-d", "arg.1=-dmz\r\narg.2=" + args[1]);
                                RandomAccessFile raf = new RandomAccessFile(new File_S("service/CrushFTPService.ini"), "rw");
                                raf.setLength(0L);
                                raf.write(s.getBytes("UTF8"));
                                raf.close();
                                Process proc = Runtime.getRuntime().exec("net stop \"CrushFTP Server\"");
                                BufferedReader proc_in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                                String data = "";
                                while ((data = proc_in.readLine()) != null) {
                                    Log.log("SERVER", 0, data);
                                }
                                proc_in.close();
                                proc = Runtime.getRuntime().exec("net start \"CrushFTP Server\"");
                                proc_in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                                data = "";
                                while ((data = proc_in.readLine()) != null) {
                                    Log.log("SERVER", 0, data);
                                }
                                proc_in.close();
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break block81;
                    }
                    if (!args[0].toUpperCase().startsWith("-TEST_THREADS")) break block82;
                    int count1 = 0;
                    int count2 = 0;
                    try {
                        int x = 0;
                        while (x < 10000) {
                            ++count1;
                            if (++count2 == 100) {
                                System.out.println(String.valueOf(count1) + " threads successful");
                                count2 = 0;
                            }
                            new Thread(new Runnable(){

                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(60000L);
                                    }
                                    catch (InterruptedException interruptedException) {
                                        // empty catch block
                                    }
                                }
                            }).start();
                            ++x;
                        }
                    }
                    catch (Throwable e) {
                        e.printStackTrace();
                    }
                    System.out.println(String.valueOf(count1) + " threads successful");
                    System.exit(0);
                    break block81;
                }
                if (!args[0].toUpperCase().startsWith("-TEST_OPENFILES")) break block83;
                int count1 = 0;
                int count2 = 0;
                Vector<FileInputStream> file_list = new Vector<FileInputStream>();
                try {
                    int x = 0;
                    while (x < 20000) {
                        ++count1;
                        if (++count2 == 100) {
                            System.out.println(String.valueOf(count1) + " files successful");
                            count2 = 0;
                        }
                        file_list.addElement(new FileInputStream(new File("./CrushFTP.jar")));
                        ++x;
                    }
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
                try {
                    while (file_list.size() > 0) {
                        ((FileInputStream)file_list.remove(0)).close();
                    }
                }
                catch (Exception e) {
                    // empty catch block
                }
                System.out.println(String.valueOf(count1) + " files successful");
                System.exit(0);
                break block81;
            }
            if (args[0].toUpperCase().startsWith("-VERIFYUSERSVFS")) {
                File[] users = new File("./users/" + args[1] + "/").listFiles();
                boolean fix = false;
                if (args.length > 2) {
                    fix = args[2].equals("fix");
                }
                int x = 0;
                while (x < users.length) {
                    try {
                        Properties vfs = (Properties)crushftp.handlers.Common.readXMLObject(String.valueOf(users[x].getPath()) + "/VFS.XML");
                        if (vfs != null) {
                            File[] vfs_roots = new File(String.valueOf(users[x].getPath()) + "/VFS/").listFiles();
                            Enumeration<Object> keys = vfs.keys();
                            boolean bad = false;
                            int count = 0;
                            String bad_entries = "";
                            while (keys.hasMoreElements()) {
                                String key = "" + keys.nextElement();
                                if (key.equals("/")) continue;
                                boolean found = false;
                                count = 0;
                                int xx = 0;
                                while (xx < vfs_roots.length) {
                                    String name = vfs_roots[xx].getName();
                                    if (!name.startsWith(".")) {
                                        ++count;
                                        if (key.startsWith("/" + name.toUpperCase() + "/")) {
                                            found = true;
                                        }
                                    }
                                    ++xx;
                                }
                                if (found) continue;
                                bad = true;
                                bad_entries = String.valueOf(bad_entries) + key + ",  ";
                                if (!fix) continue;
                                vfs.remove(key);
                            }
                            if (bad) {
                                System.out.println("BAD ENTRY IN USER:" + users[x] + ". Real VFS root entries:" + count + ", Bad Items:" + bad_entries + (fix ? "  FIXED!" : ""));
                                if (fix) {
                                    crushftp.handlers.Common.writeXMLObject(String.valueOf(users[x].getPath()) + "/VFS.XML", (Object)vfs, "VFS");
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        System.out.println(users[x]);
                        e.printStackTrace();
                    }
                    ++x;
                }
                System.exit(0);
            } else if (args[0].toUpperCase().startsWith("-DMZ")) {
                System.setProperty("java.awt.headless", "true");
                new CrushFTPDMZ(args);
            } else if (args[0].toUpperCase().startsWith("-V")) {
                System.out.println("CrushFTP " + ServerStatus.version_info_str + ServerStatus.sub_version_info_str);
                System.out.println("CrushTunnel 3.1.16");
            } else if (args[0].toUpperCase().startsWith("-SBD")) {
                System.setProperty("java.awt.headless", "true");
                if (args.length > 1) {
                    ServerBeat.main(new String[]{args[1]});
                } else {
                    ServerBeat.main(new String[]{"10"});
                }
            } else if (args[0].toUpperCase().startsWith("-D")) {
                System.setProperty("java.awt.headless", "true");
                if (args.length > 1) {
                    try {
                        Thread.sleep(1000 * Integer.parseInt(args[1].trim()));
                    }
                    catch (Exception users) {
                        // empty catch block
                    }
                }
                new CrushFTPD();
            } else if (args[0].toUpperCase().equals("-P")) {
                if (args[2].startsWith("RANDOM_")) {
                    args[2] = crushftp.handlers.Common.makeBoundary(Integer.parseInt(args[2].split("_")[1].trim()));
                    System.out.println("Using random password:" + args[2]);
                }
                if (args.length >= 4) {
                    String password_salt_location = args[3].trim();
                    if (new File_S(password_salt_location).exists()) {
                        if (new File_S(password_salt_location).length() <= 0x100000L) {
                            byte[] salt_bytes = new byte[]{};
                            try {
                                RandomAccessFile raf = new RandomAccessFile(new File_S(password_salt_location), "r");
                                salt_bytes = new byte[(int)raf.length()];
                                raf.read(salt_bytes);
                                raf.close();
                            }
                            catch (Exception e) {
                                System.out.println(e);
                            }
                            System.out.println(new crushftp.handlers.Common().encode_pass(args[2], args[1], "!!" + new String(salt_bytes)));
                        } else {
                            System.out.println("Error : The given salt file is too big (only 1 MB is allowed)!");
                        }
                    } else if (args[1].indexOf("DES") >= 0) {
                        String converted_pass = "";
                        try {
                            boolean encrypt = true;
                            if (System.currentTimeMillis() < new SimpleDateFormat("yyyyMMdd").parse("20200615").getTime() && args[1].equals("RDES")) {
                                encrypt = false;
                            }
                            converted_pass = Common.encryptDecrypt(args[2], encrypt);
                        }
                        catch (Exception e) {
                            System.out.println("Error : " + e);
                        }
                        System.out.println(converted_pass);
                    } else {
                        System.out.println(new crushftp.handlers.Common().encode_pass(args[2], args[1], args[3]));
                    }
                } else {
                    System.out.println(new crushftp.handlers.Common().encode_pass(args[2], args[1], ""));
                }
            } else if (args[0].toUpperCase().startsWith("-REG")) {
                Properties request = new Properties();
                request.put("registration_name", args[1]);
                request.put("registration_email", args[2]);
                request.put("registration_code", args[3]);
                new ServerStatus(true, null);
                System.out.println(AdminControls.registerCrushFTP(request, "(CONNECT)"));
                System.exit(0);
            } else if (args[0].toUpperCase().equals("-R")) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
                System.getProperties().put("crushftp.immunityToDieString", sdf.format(new Date()));
                if (crushftp.handlers.Common.machine_is_x()) {
                    crushftp.handlers.Common.remove_osx_service();
                } else if (crushftp.handlers.Common.machine_is_windows()) {
                    Common.remove_windows_service("CrushFTP", "plugins/lib/CrushFTPJarProxy.jar");
                }
            } else if (args[0].toUpperCase().equals("-A")) {
                String userDir = defaultUserFolder;
                new crushftp.handlers.Common().writeAdminUser(args[1], args[2], userDir, false);
                System.out.println(LOC.G("Admin user written to") + ":" + System.getProperties().getProperty("crushftp.users") + userDir + "/");
            } else if (args[0].toUpperCase().equals("-U")) {
                String permissions = "(read)(view)(resume)";
                String templateUser = "";
                String notes = null;
                String email = null;
                if (args.length >= 5) {
                    permissions = args[4].toUpperCase().indexOf("FULL") >= 0 ? "(read)(write)(view)(delete)(resume)(rename)(makedir)(deletedir)" : (args[4].toUpperCase().indexOf("READ_ONLY") >= 0 ? "(read)(view)(resume)" : args[4]);
                }
                if (args.length >= 6) {
                    templateUser = args[5];
                }
                if (args.length >= 7) {
                    notes = args[6];
                }
                if (args.length >= 8) {
                    email = args[7];
                }
                new crushftp.handlers.Common().writeNewUser(args[1], args[2], args[3], permissions, templateUser, notes, email, defaultUserFolder);
                System.out.println(LOC.G("User written to") + ":" + System.getProperties().getProperty("crushftp.users") + defaultUserFolder + "/");
            } else if (args[0].toUpperCase().equals("-?") || args[0].toUpperCase().equals("/?") || args[0].toUpperCase().equals("-HELP") || args[0].toUpperCase().equals("/HELP")) {
                System.out.println("   : (GUI LOADED) no parameters loads it normally with a GUI.");
                System.out.println("-d :      runs as a daemon without ever initializing any GUI objects at all.");
                System.out.println("                  Very fast, low memory, useful when being run as a service.");
                System.out.println("-h : (GUI LOADED) runs it normal with a GUI, but after loading it hides the main GUI");
                System.out.println("                  window so its not visible wasting CPU cycles.");
                System.out.println("-a :      takes 2 additional quoted parameters of [username] [password] and makes a directory");
                System.out.println("                  named [username] with a user.XML file in it that has FULL remote admin privs.");
                System.out.println("-u :      takes 7 additional quoted parameters of [username] [password] [start dir path in slash notation ex:\"/my hd/\"]");
                System.out.println("                  [permissions (either 'FULL' or 'READ_ONLY' or the actual items] [templateUser:(all settings from user are copied except VFS.  Use \"\" to skip)] [notes] [email]");
                System.out.println("                  and makes a directory named [username] with a user.XML file in it.");
                System.out.println("-reg :    takes 3 parameters in order: name, email, code");
                System.out.println("-r :      removes service/daemon (Windows and OS X only)");
                System.out.println("-p :      takes two parameters to encrypt a password. The first is the format (DES or SHA), the second is the password.");
                System.out.println("-sbd :    ServerBeat deadman's switch...if session.obj file stops having date updated for 10 seconds, release our VIP.");
                System.out.println("");
                System.out.println("-v : CrushFTP version info.");
                System.out.println("-? : this help screen.");
                System.out.println("/? : this help screen.");
                System.out.println("-help : this help screen.");
                System.out.println("/help : this help screen.");
            } else {
                new CrushFTPGUI();
            }
        }
    }
}

