/*
 * Decompiled with CFR 0.152.
 */
package jline;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import jline.AnsiWindowsTerminal;
import jline.OSvTerminal;
import jline.Terminal;
import jline.UnixTerminal;
import jline.UnsupportedTerminal;
import jline.internal.Configuration;
import jline.internal.Log;
import jline.internal.Preconditions;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class TerminalFactory {
    public static final String JLINE_TERMINAL = "jline.terminal";
    public static final String AUTO = "auto";
    public static final String UNIX = "unix";
    public static final String OSV = "osv";
    public static final String WIN = "win";
    public static final String WINDOWS = "windows";
    public static final String FREEBSD = "freebsd";
    public static final String NONE = "none";
    public static final String OFF = "off";
    public static final String FALSE = "false";
    private static Terminal term = null;
    private static final Map<Flavor, Class<? extends Terminal>> FLAVORS = new HashMap<Flavor, Class<? extends Terminal>>();

    public static synchronized Terminal create() {
        return TerminalFactory.create(null);
    }

    public static synchronized Terminal create(String ttyDevice) {
        Terminal t;
        block18: {
            String type;
            if (Log.TRACE) {
                Log.trace(new Throwable("CREATE MARKER"));
            }
            if ((type = Configuration.getString(JLINE_TERMINAL)) == null) {
                type = AUTO;
                if ("dumb".equals(System.getenv("TERM"))) {
                    String emacs = System.getenv("EMACS");
                    String insideEmacs = System.getenv("INSIDE_EMACS");
                    if (emacs == null || insideEmacs == null) {
                        type = NONE;
                    }
                }
            }
            Log.debug("Creating terminal; type=", type);
            try {
                String tmp = type.toLowerCase();
                if (tmp.equals(UNIX)) {
                    t = TerminalFactory.getFlavor(Flavor.UNIX);
                    break block18;
                }
                if (tmp.equals(OSV)) {
                    t = TerminalFactory.getFlavor(Flavor.OSV);
                    break block18;
                }
                if (tmp.equals(WIN) || tmp.equals(WINDOWS)) {
                    t = TerminalFactory.getFlavor(Flavor.WINDOWS);
                    break block18;
                }
                if (tmp.equals(NONE) || tmp.equals(OFF) || tmp.equals(FALSE)) {
                    t = new UnsupportedTerminal();
                    break block18;
                }
                if (tmp.equals(AUTO)) {
                    String os = Configuration.getOsName();
                    Flavor flavor = Flavor.UNIX;
                    if (os.contains(WINDOWS)) {
                        flavor = Flavor.WINDOWS;
                    } else if (System.getenv("OSV_CPUS") != null) {
                        flavor = Flavor.OSV;
                    }
                    t = TerminalFactory.getFlavor(flavor, ttyDevice);
                    break block18;
                }
                try {
                    t = (Terminal)Thread.currentThread().getContextClassLoader().loadClass(type).newInstance();
                }
                catch (Exception e) {
                    throw new IllegalArgumentException(MessageFormat.format("Invalid terminal type: {0}", type), e);
                }
            }
            catch (Exception e) {
                Log.error("Failed to construct terminal; falling back to unsupported", e);
                t = new UnsupportedTerminal();
            }
        }
        Log.debug("Created Terminal: ", t);
        try {
            t.init();
        }
        catch (Throwable e) {
            Log.error("Terminal initialization failed; falling back to unsupported", e);
            return new UnsupportedTerminal();
        }
        return t;
    }

    public static synchronized void reset() {
        term = null;
    }

    public static synchronized void resetIf(Terminal t) {
        if (t == term) {
            TerminalFactory.reset();
        }
    }

    public static synchronized void configure(String type) {
        Preconditions.checkNotNull(type);
        System.setProperty(JLINE_TERMINAL, type);
    }

    public static synchronized void configure(Type type) {
        Preconditions.checkNotNull(type);
        TerminalFactory.configure(type.name().toLowerCase());
    }

    public static synchronized Terminal get(String ttyDevice) {
        if (term == null) {
            term = TerminalFactory.create(ttyDevice);
        }
        return term;
    }

    public static synchronized Terminal get() {
        return TerminalFactory.get(null);
    }

    public static Terminal getFlavor(Flavor flavor) throws Exception {
        return TerminalFactory.getFlavor(flavor, null);
    }

    public static Terminal getFlavor(Flavor flavor, String ttyDevice) throws Exception {
        Class<? extends Terminal> type = FLAVORS.get((Object)flavor);
        Terminal result = null;
        if (type != null) {
            Constructor<? extends Terminal> ttyDeviceConstructor;
            result = ttyDevice != null ? ((ttyDeviceConstructor = type.getConstructor(String.class)) != null ? ttyDeviceConstructor.newInstance(ttyDevice) : type.newInstance()) : type.newInstance();
        } else {
            throw new InternalError();
        }
        return result;
    }

    public static void registerFlavor(Flavor flavor, Class<? extends Terminal> type) {
        FLAVORS.put(flavor, type);
    }

    static {
        TerminalFactory.registerFlavor(Flavor.WINDOWS, AnsiWindowsTerminal.class);
        TerminalFactory.registerFlavor(Flavor.UNIX, UnixTerminal.class);
        TerminalFactory.registerFlavor(Flavor.OSV, OSvTerminal.class);
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum Flavor {
        WINDOWS,
        UNIX,
        OSV;

    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum Type {
        AUTO,
        WINDOWS,
        UNIX,
        OSV,
        NONE;

    }
}

