/*
 * Decompiled with CFR 0.152.
 */
package org.boris.winrun4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import org.boris.winrun4j.Log;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Launcher {
    private Map<String, Map<String, String>> bundle = new LinkedHashMap<String, Map<String, String>>();
    private int classpathIndex;
    private int vmargIndex;
    private int argIndex;
    private int fileAssIndex;
    private int serviceDepIndex;
    private File launcherFile;
    private File launcher;
    private File ini;

    public Launcher(File launcherFile) {
        this.launcherFile = launcherFile;
    }

    public File getLauncher() {
        return this.launcher;
    }

    public Launcher createAt(File exeFile) throws IOException {
        this.launcher = exeFile;
        Launcher.copy(this.launcherFile, this.launcher);
        this.ini = new File(this.launcher.getParent(), Launcher.getNameSansExtension(this.launcher) + ".ini");
        if (this.ini.exists()) {
            this.ini.delete();
        }
        Launcher.copy(new StringReader(this.toString()), new FileWriter(this.ini), true);
        return this;
    }

    public Launcher create() throws IOException {
        this.launcher = File.createTempFile("winrun4j.launcher.", ".exe");
        this.launcher.deleteOnExit();
        Launcher.copy(this.launcherFile, this.launcher);
        this.ini = new File(this.launcher.getParent(), Launcher.getNameSansExtension(this.launcher) + ".ini");
        this.ini.deleteOnExit();
        Launcher.copy(new StringReader(this.toString()), new FileWriter(this.ini), true);
        return this;
    }

    public Process launch(String ... args) throws Exception {
        if (this.launcher == null) {
            this.create();
        }
        String[] cmd = new String[args == null ? 1 : args.length + 1];
        cmd[0] = this.launcher.getAbsolutePath();
        if (args != null) {
            System.arraycopy(args, 0, cmd, 1, args.length);
        }
        return Runtime.getRuntime().exec(cmd);
    }

    public Launcher main(Class clazz) {
        return this.main(clazz.getName());
    }

    public Launcher main(String main) {
        this.set(null, "main.class", main);
        return this;
    }

    public Launcher classpath(File f) {
        return this.classpath(f.getAbsolutePath());
    }

    public Launcher classpath(String entry) {
        ++this.classpathIndex;
        this.set(null, "classpath." + this.classpathIndex, entry);
        return this;
    }

    public Launcher workingDir(File dir) {
        return this.workingDir(dir.getAbsolutePath());
    }

    public Launcher workingDir(String dir) {
        this.set(null, "working.directory", dir);
        return this;
    }

    public Launcher arg(String value) {
        this.set(null, "arg." + ++this.argIndex, value);
        return this;
    }

    public Launcher vmarg(String value) {
        this.set(null, "vmarg." + ++this.vmargIndex, value);
        return this;
    }

    public Launcher vmVersion(String min, String max, String exact) {
        this.set(null, "vm.version.min", min);
        this.set(null, "vm.version.max", max);
        this.set(null, "vm.version", exact);
        return this;
    }

    public Launcher vmLocation(File location) {
        return this.vmLocation(location.getAbsolutePath());
    }

    public Launcher vmLocation(String location) {
        this.set(null, "vm.location", location);
        return this;
    }

    public Launcher heap(String max, String min, String preferred) {
        this.set(null, "vm.heapsize.max.percent", max);
        this.set(null, "vm.heapsize.min.percent", min);
        this.set(null, "vm.heapsize.preferred", preferred);
        return this;
    }

    public Launcher log(Log.Level level) {
        this.set(null, "log.level", level.getText());
        return this;
    }

    public Launcher log(String file, Log.Level level, boolean overwrite, boolean andConsole) {
        this.set(null, "log", file);
        this.set(null, "log.level", level.getText());
        this.set(null, "log.overwrite", Boolean.toString(overwrite));
        this.set(null, "log.file.and.console", Boolean.toString(andConsole));
        return this;
    }

    public Launcher logRoll(double rollSize, String prefix, String suffix) {
        this.set(null, "log.roll.size", Double.toString(rollSize));
        this.set(null, "log.roll.prefix", prefix);
        this.set(null, "log.roll.suffix", suffix);
        return this;
    }

    public Launcher splash(String image, boolean autohide) {
        this.set(null, "splash.image", image);
        this.set(null, "splash.autohide", autohide);
        return this;
    }

    public Launcher dde(boolean enabled, Class clazz) {
        this.set(null, "dde.enabled", enabled);
        if (clazz != null) {
            this.set(null, "dde.class", clazz.getName());
        }
        return this;
    }

    public Launcher ddeServer(String server, String topic, String windowClass) {
        this.set(null, "dde.server.name", server);
        this.set(null, "dde.topic", topic);
        this.set(null, "dde.window.class", windowClass);
        return this;
    }

    public Launcher fileAss(String ext, String name, String desc) {
        int index = ++this.fileAssIndex;
        this.set("FileAssociations", "file." + index + ".extension", ext);
        this.set("FileAssociations", "file." + index + ".name", name);
        this.set("FileAssociations", "file." + index + ".description", desc);
        return this;
    }

    public Launcher service(Class clazz, String name, String description) {
        return this.service(clazz, clazz.getSimpleName(), name, description);
    }

    public Launcher service(Class clazz, String id, String name, String description) {
        this.set(null, "service.class", clazz.getName());
        this.set(null, "service.id", id);
        this.set(null, "service.name", name);
        this.set(null, "service.description", description);
        return this;
    }

    public Launcher startup(String mode) {
        this.set(null, "service.startup", mode);
        return this;
    }

    public Launcher depends(String otherService) {
        int index = ++this.serviceDepIndex;
        this.set(null, "service.dependency." + index, otherService);
        return this;
    }

    public Launcher debug(int port, boolean server, boolean suspend) {
        this.vmarg("-Xdebug");
        this.vmarg("-Xnoagent");
        this.vmarg("-Xrunjdwp:transport=dt_socket,address=" + port + ",server=" + (server ? "y" : "n") + ",suspend=" + (suspend ? "y" : "n"));
        return this;
    }

    public Launcher heapMax(double percent) {
        this.set(null, "vm.heapsize.max.percent", Double.toString(percent));
        return this;
    }

    public Launcher heapMin(double percent) {
        this.set(null, "vm.heapsize.min.percent", Double.toString(percent));
        return this;
    }

    public Launcher heapPreferred(double mb) {
        this.set(null, "vm.heapsize.preferred", Double.toString(mb));
        return this;
    }

    public Launcher errorMessages(String notFound, String loadFailed) {
        this.set("ErrorMessages", "java.not.found", notFound);
        this.set("ErrorMessages", "java.failed", loadFailed);
        return this;
    }

    public Launcher showErrorPopup(boolean show) {
        this.set("ErrorMessages", "show.popup", show);
        return this;
    }

    private void set(String section, String name, Object value) {
        if (value == null) {
            return;
        }
        Map<String, String> p = this.bundle.get(section);
        if (p == null) {
            p = new LinkedHashMap<String, String>();
            this.bundle.put(section, p);
        }
        p.put(name, String.valueOf(value));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.toString(null, this.bundle.get(null), sb);
        for (String key : this.bundle.keySet()) {
            if (key == null) continue;
            this.toString(key, this.bundle.get(key), sb);
        }
        return sb.toString();
    }

    private void toString(String sectionName, Map<String, String> section, StringBuilder sb) {
        if (sectionName != null) {
            sb.append("[");
            sb.append(sectionName);
            sb.append("]\r\n");
        }
        for (String key : section.keySet()) {
            sb.append(key);
            sb.append("=");
            sb.append(section.get(key));
            sb.append("\r\n");
        }
        sb.append("\r\n");
    }

    public Launcher singleInstance(String si) {
        this.set(null, "single.instance", si);
        return this;
    }

    public static void copy(File source, File target) throws IOException {
        Launcher.copy(new FileInputStream(source), new FileOutputStream(target), true);
    }

    public static void copy(Reader r, Writer w, boolean close) throws IOException {
        char[] buf = new char[4096];
        int len = 0;
        while ((len = r.read(buf)) > 0) {
            w.write(buf, 0, len);
        }
        if (close) {
            r.close();
            w.close();
        }
    }

    public static void copy(InputStream r, OutputStream w, boolean close) throws IOException {
        byte[] buf = new byte[4096];
        int len = 0;
        while ((len = r.read(buf)) > 0) {
            w.write(buf, 0, len);
        }
        if (close) {
            r.close();
            w.close();
        }
    }

    public static String getNameSansExtension(File f) {
        if (f == null) {
            return null;
        }
        String n = f.getName();
        int idx = n.lastIndexOf(46);
        if (idx == -1) {
            return n;
        }
        return n.substring(0, idx);
    }
}

