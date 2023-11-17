/*
 * Decompiled with CFR 0.152.
 */
package org.fusesource.hawtjni.runtime;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Library {
    static final String SLASH = System.getProperty("file.separator");
    private final String name;
    private final String version;
    private final ClassLoader classLoader;
    private boolean loaded;

    public Library(String name) {
        this(name, null, null);
    }

    public Library(String name, Class<?> clazz) {
        this(name, Library.version(clazz), clazz.getClassLoader());
    }

    public Library(String name, String version) {
        this(name, version, null);
    }

    public Library(String name, String version, ClassLoader classLoader) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        this.name = name;
        this.version = version;
        this.classLoader = classLoader;
    }

    private static String version(Class<?> clazz) {
        try {
            return clazz.getPackage().getImplementationVersion();
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    public static String getOperatingSystem() {
        String name = System.getProperty("os.name").toLowerCase().trim();
        if (name.startsWith("linux")) {
            return "linux";
        }
        if (name.startsWith("mac os x")) {
            return "osx";
        }
        if (name.startsWith("win")) {
            return "windows";
        }
        return name.replaceAll("\\W+", "_");
    }

    public static String getPlatform() {
        return Library.getOperatingSystem() + Library.getBitModel();
    }

    public static int getBitModel() {
        String prop = System.getProperty("sun.arch.data.model");
        if (prop == null) {
            prop = System.getProperty("com.ibm.vm.bitmode");
        }
        if (prop != null) {
            return Integer.parseInt(prop);
        }
        return -1;
    }

    public synchronized void load() {
        if (this.loaded) {
            return;
        }
        this.doLoad();
        this.loaded = true;
    }

    private void doLoad() {
        String version = System.getProperty("library." + this.name + ".version");
        if (version == null) {
            version = this.version;
        }
        ArrayList<String> errors = new ArrayList<String>();
        String customPath = System.getProperty("library." + this.name + ".path");
        if (customPath != null) {
            if (version != null && this.load(errors, this.file(customPath, this.map(this.name + "-" + version)))) {
                return;
            }
            if (this.load(errors, this.file(customPath, this.map(this.name)))) {
                return;
            }
        }
        if (version != null && this.load(errors, this.name + Library.getBitModel() + "-" + version)) {
            return;
        }
        if (version != null && this.load(errors, this.name + "-" + version)) {
            return;
        }
        if (this.load(errors, this.name)) {
            return;
        }
        if (this.classLoader != null) {
            if (this.exractAndLoad(errors, version, customPath, this.getPlatformSpecifcResourcePath())) {
                return;
            }
            if (this.exractAndLoad(errors, version, customPath, this.getOperatingSystemSpecifcResourcePath())) {
                return;
            }
            if (this.exractAndLoad(errors, version, customPath, this.getResorucePath())) {
                return;
            }
        }
        throw new UnsatisfiedLinkError("Could not load library. Reasons: " + errors.toString());
    }

    public final String getOperatingSystemSpecifcResourcePath() {
        return this.getPlatformSpecifcResourcePath(Library.getOperatingSystem());
    }

    public final String getPlatformSpecifcResourcePath() {
        return this.getPlatformSpecifcResourcePath(Library.getPlatform());
    }

    public final String getPlatformSpecifcResourcePath(String platform) {
        return "META-INF/native/" + platform + "/" + this.map(this.name);
    }

    public final String getResorucePath() {
        return "META-INF/native/" + this.map(this.name);
    }

    public final String getLibraryFileName() {
        return this.map(this.name);
    }

    private boolean exractAndLoad(ArrayList<String> errors, String version, String customPath, String resourcePath) {
        URL resource = this.classLoader.getResource(resourcePath);
        if (resource != null) {
            File target;
            String libName = this.name + "-" + Library.getBitModel();
            if (version != null) {
                libName = libName + "-" + version;
            }
            String[] libNameParts = this.map(libName).split("\\.");
            String prefix = libNameParts[0] + "-";
            String suffix = "." + libNameParts[1];
            if (customPath != null && (target = this.extract(errors, resource, prefix, suffix, this.file(customPath))) != null && this.load(errors, target)) {
                return true;
            }
            customPath = System.getProperty("java.io.tmpdir");
            target = this.extract(errors, resource, prefix, suffix, this.file(customPath));
            if (target != null && this.load(errors, target)) {
                return true;
            }
        }
        return false;
    }

    private File file(String ... paths) {
        File rc = null;
        for (String path : paths) {
            rc = rc == null ? new File(path) : new File(rc, path);
        }
        return rc;
    }

    private String map(String libName) {
        String ext;
        if ((libName = System.mapLibraryName(libName)).endsWith(ext = ".dylib")) {
            libName = libName.substring(0, libName.length() - ext.length()) + ".jnilib";
        }
        return libName;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private File extract(ArrayList<String> errors, URL source, String prefix, String suffix, File directory) {
        File file;
        File target = null;
        FileOutputStream os = null;
        InputStream is = null;
        try {
            target = File.createTempFile(prefix, suffix, directory);
            is = source.openStream();
            if (is != null) {
                int read;
                byte[] buffer = new byte[4096];
                os = new FileOutputStream(target);
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                this.chmod("755", target);
            }
            target.deleteOnExit();
            file = target;
        }
        catch (Throwable throwable) {
            try {
                Library.close(os);
                Library.close(is);
                throw throwable;
            }
            catch (Throwable e) {
                if (target != null) {
                    target.delete();
                }
                errors.add(e.getMessage());
                return null;
            }
        }
        Library.close(os);
        Library.close(is);
        return file;
    }

    private static void close(Closeable file) {
        if (file != null) {
            try {
                file.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private void chmod(String permision, File path) {
        if (Library.getPlatform().startsWith("windows")) {
            return;
        }
        try {
            Runtime.getRuntime().exec(new String[]{"chmod", permision, path.getCanonicalPath()}).waitFor();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private boolean load(ArrayList<String> errors, File lib) {
        try {
            System.load(lib.getPath());
            return true;
        }
        catch (UnsatisfiedLinkError e) {
            errors.add(e.getMessage());
            return false;
        }
    }

    private boolean load(ArrayList<String> errors, String lib) {
        try {
            System.loadLibrary(lib);
            return true;
        }
        catch (UnsatisfiedLinkError e) {
            errors.add(e.getMessage());
            return false;
        }
    }
}

