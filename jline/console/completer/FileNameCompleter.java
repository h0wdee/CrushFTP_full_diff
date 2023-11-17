/*
 * Decompiled with CFR 0.152.
 */
package jline.console.completer;

import java.io.File;
import java.util.List;
import jline.console.completer.Completer;
import jline.internal.Configuration;
import jline.internal.Preconditions;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class FileNameCompleter
implements Completer {
    private static final boolean OS_IS_WINDOWS;

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        Preconditions.checkNotNull(candidates);
        if (buffer == null) {
            buffer = "";
        }
        if (OS_IS_WINDOWS) {
            buffer = buffer.replace('/', '\\');
        }
        String translated = buffer;
        File homeDir = this.getUserHome();
        if (translated.startsWith("~" + this.separator())) {
            translated = homeDir.getPath() + translated.substring(1);
        } else if (translated.startsWith("~")) {
            translated = homeDir.getParentFile().getAbsolutePath();
        } else if (!new File(translated).isAbsolute()) {
            String cwd = this.getUserDir().getAbsolutePath();
            translated = cwd + this.separator() + translated;
        }
        File file = new File(translated);
        File dir = translated.endsWith(this.separator()) ? file : file.getParentFile();
        File[] entries = dir == null ? new File[]{} : dir.listFiles();
        return this.matchFiles(buffer, translated, entries, candidates);
    }

    protected String separator() {
        return File.separator;
    }

    protected File getUserHome() {
        return Configuration.getUserHome();
    }

    protected File getUserDir() {
        return new File(".");
    }

    protected int matchFiles(String buffer, String translated, File[] files, List<CharSequence> candidates) {
        if (files == null) {
            return -1;
        }
        int matches = 0;
        for (File file : files) {
            if (!file.getAbsolutePath().startsWith(translated)) continue;
            ++matches;
        }
        for (File file : files) {
            if (!file.getAbsolutePath().startsWith(translated)) continue;
            String name = file.getName() + (matches == 1 && file.isDirectory() ? this.separator() : " ");
            candidates.add(this.render(file, name).toString());
        }
        int index = buffer.lastIndexOf(this.separator());
        return index + this.separator().length();
    }

    protected CharSequence render(File file, CharSequence name) {
        return name;
    }

    static {
        String os = Configuration.getOsName();
        OS_IS_WINDOWS = os.contains("windows");
    }
}

