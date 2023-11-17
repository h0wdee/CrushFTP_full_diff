/*
 * Decompiled with CFR 0.152.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.StringReader;
import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;

public class CrushFTPWinServiceRestart
extends AbstractService {
    @Override
    public int serviceMain(String[] args) throws ServiceException {
        CrushFTPWinServiceRestart.run();
        System.exit(0);
        return 0;
    }

    public static void run() {
        block18: {
            String home = "./";
            try {
                if (System.getProperties().getProperty("os.name", "").toUpperCase().indexOf("NDOWS") >= 0) {
                    RandomAccessFile test2;
                    String fileList = "";
                    System.out.println("Loading list of files to update...");
                    try {
                        RandomAccessFile in = new RandomAccessFile(new File("update_list.txt"), "r");
                        byte[] b = new byte[(int)in.length()];
                        in.readFully(b);
                        in.close();
                        fileList = new String(b);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Thread.sleep(100L);
                    }
                    System.out.println("-----------------------------------------------------------------------");
                    System.out.println(fileList);
                    System.out.println("-----------------------------------------------------------------------");
                    int loops = 0;
                    while (loops++ < 5) {
                        System.out.println("Stopping server...");
                        Runtime.getRuntime().exec(new String[]{"net", "stop", "CrushFTP Server"});
                        try {
                            test2 = new RandomAccessFile(new File("CrushFTP.jar"), "rw");
                            test2.close();
                            break;
                        }
                        catch (Exception test2) {
                            Thread.sleep(2000L);
                        }
                    }
                    System.out.println("Server stopped.");
                    Thread.sleep(1000L);
                    if (fileList.equals("")) {
                        Process proc = Runtime.getRuntime().exec("cmd.exe /C update.bat".split(" "), null, new File(home));
                        proc.waitFor();
                    } else {
                        BufferedReader br = new BufferedReader(new StringReader(fileList));
                        String data = "";
                        while ((data = br.readLine()) != null) {
                            String f1 = data.substring(data.indexOf("UpdateTemp") + "UpdateTemp/".length()).trim();
                            new File("./" + f1).delete();
                            new File("./UpdateTemp/" + f1).renameTo(new File("./" + f1));
                        }
                        new File("update.bat").delete();
                    }
                    System.out.println("Finished updating files.");
                    loops = 0;
                    while (loops++ < 5) {
                        try {
                            test2 = new RandomAccessFile(new File("CrushFTP.jar"), "rw");
                            test2.close();
                            System.out.println("Starting server...");
                            Runtime.getRuntime().exec(new String[]{"net", "start", "CrushFTP Server"});
                        }
                        catch (Exception e) {
                            break;
                        }
                        Thread.sleep(2000L);
                    }
                    System.out.println("Server started.");
                    Runtime.getRuntime().exec("net stop CrushFTPRestart");
                    break block18;
                }
                if (System.getProperties().getProperty("os.name", "").toUpperCase().equals("MAC OS X")) {
                    System.out.println("Trying to restart the daemon...");
                    Process proc = Runtime.getRuntime().exec("launchctl stop com.crushftp.CrushFTP".split(" "), null, new File(home));
                    proc.waitFor();
                    System.out.println("Daemon stopped.");
                    Thread.sleep(1000L);
                    proc = Runtime.getRuntime().exec("launchctl start com.crushftp.CrushFTP".split(" "), null, new File(home));
                    proc.waitFor();
                    System.out.println("Finished restarting daemon.");
                } else {
                    System.out.println("Trying to restart the daemon...");
                    home = String.valueOf(new File(home).getCanonicalPath()) + "/";
                    Process proc = Runtime.getRuntime().exec(String.valueOf(home) + "crushftp_init.sh stop");
                    proc.waitFor();
                    System.out.println("Daemon stopped.");
                    Thread.sleep(1000L);
                    proc = Runtime.getRuntime().exec(String.valueOf(home) + "crushftp_init.sh start");
                    proc.waitFor();
                    System.out.println("Finished restarting daemon.");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(100L);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        System.out.println("Exiting");
        System.exit(0);
    }
}

