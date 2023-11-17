/*
 * Decompiled with CFR 0.152.
 */
package jline.console.internal;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.history.FileHistory;
import jline.console.history.PersistentHistory;
import jline.console.internal.ConsoleReaderInputStream;
import jline.internal.Configuration;

public class ConsoleRunner {
    public static final String property = "jline.history";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void main(String[] args) throws Exception {
        ArrayList<String> argList = new ArrayList<String>(Arrays.asList(args));
        if (argList.size() == 0) {
            ConsoleRunner.usage();
            return;
        }
        String historyFileName = System.getProperty(property, null);
        String mainClass = (String)argList.remove(0);
        ConsoleReader reader = new ConsoleReader();
        if (historyFileName != null) {
            reader.setHistory(new FileHistory(new File(Configuration.getUserHome(), String.format(".jline-%s.%s.history", mainClass, historyFileName))));
        } else {
            reader.setHistory(new FileHistory(new File(Configuration.getUserHome(), String.format(".jline-%s.history", mainClass))));
        }
        String completors = System.getProperty(ConsoleRunner.class.getName() + ".completers", "");
        ArrayList<Completer> completorList = new ArrayList<Completer>();
        StringTokenizer tok = new StringTokenizer(completors, ",");
        while (tok.hasMoreTokens()) {
            Object obj = Class.forName(tok.nextToken()).newInstance();
            completorList.add((Completer)obj);
        }
        if (completorList.size() > 0) {
            reader.addCompleter(new ArgumentCompleter(completorList));
        }
        ConsoleReaderInputStream.setIn(reader);
        try {
            Class<?> type = Class.forName(mainClass);
            Method method = type.getMethod("main", String[].class);
            String[] mainArgs = argList.toArray(new String[argList.size()]);
            method.invoke(null, new Object[]{mainArgs});
        }
        finally {
            ConsoleReaderInputStream.restoreIn();
            if (reader.getHistory() instanceof PersistentHistory) {
                ((PersistentHistory)reader.getHistory()).flush();
            }
        }
    }

    private static void usage() {
        System.out.println("Usage: \n   java [-Djline.history='name'] " + ConsoleRunner.class.getName() + " <target class name> [args]" + "\n\nThe -Djline.history option will avoid history" + "\nmangling when running ConsoleRunner on the same application." + "\n\nargs will be passed directly to the target class name.");
    }
}

