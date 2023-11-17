/*
 * Decompiled with CFR 0.152.
 */
import com.crushftp.client.File_S;
import crushftp.server.ServerStatus;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

public class CrushFTP {
    public static void main(String[] args) {
        URL[] urls;
        block10: {
            Thread.currentThread().setName("CrushFTP Jar Proxy");
            Vector<URL> all = new Vector<URL>();
            urls = new URL[]{};
            File_S[] files = (File_S[])new File_S("plugins/lib/").listFiles();
            try {
                if (!new File_S("plugins/lib/").exists()) {
                    System.out.println("plugins/lib folder not found, CrushFTP may not be able to start...");
                    break block10;
                }
                all.addElement(new File_S("CrushFTP.jar").toURI().toURL());
                int x = 0;
                while (x < files.length) {
                    if ((!files[x].isFile() || !files[x].getName().equalsIgnoreCase("CRUSHFTPRESTART.JAR")) && files[x].isFile() && files[x].getName().toUpperCase().endsWith(".JAR")) {
                        all.addElement(files[x].toURI().toURL());
                    }
                    ++x;
                }
                urls = new URL[all.size()];
                x = 0;
                while (x < all.size()) {
                    try {
                        urls[x] = (URL)all.elementAt(x);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    ++x;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            URLClassLoader loader = URLClassLoader.newInstance(urls);
            ServerStatus.clasLoader = loader;
            Class<?> c = Class.forName("CrushFTPLauncher", true, ServerStatus.clasLoader);
            Constructor<?> cons = c.getConstructor(Object.class);
            cons.newInstance(new Object[]{args});
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

