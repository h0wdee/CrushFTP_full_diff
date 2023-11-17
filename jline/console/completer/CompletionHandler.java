/*
 * Decompiled with CFR 0.152.
 */
package jline.console.completer;

import java.io.IOException;
import java.util.List;
import jline.console.ConsoleReader;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface CompletionHandler {
    public boolean complete(ConsoleReader var1, List<CharSequence> var2, int var3) throws IOException;
}

