/*
 * Decompiled with CFR 0.152.
 */
package jline.console.completer;

import java.util.List;
import jline.console.completer.Completer;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class NullCompleter
implements Completer {
    public static final NullCompleter INSTANCE = new NullCompleter();

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        return -1;
    }
}

