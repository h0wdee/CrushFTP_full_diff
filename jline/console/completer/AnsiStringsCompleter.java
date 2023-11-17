/*
 * Decompiled with CFR 0.152.
 */
package jline.console.completer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import jline.console.completer.Completer;
import jline.internal.Ansi;
import jline.internal.Preconditions;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class AnsiStringsCompleter
implements Completer {
    private final SortedMap<String, String> strings = new TreeMap<String, String>();

    public AnsiStringsCompleter() {
    }

    public AnsiStringsCompleter(Collection<String> strings) {
        Preconditions.checkNotNull(strings);
        for (String str : strings) {
            this.strings.put(Ansi.stripAnsi(str), str);
        }
    }

    public AnsiStringsCompleter(String ... strings) {
        this(Arrays.asList(strings));
    }

    public Collection<String> getStrings() {
        return this.strings.values();
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        Preconditions.checkNotNull(candidates);
        if (buffer == null) {
            candidates.addAll(this.strings.values());
        } else {
            buffer = Ansi.stripAnsi(buffer);
            for (Map.Entry<String, String> match : this.strings.tailMap(buffer).entrySet()) {
                if (!match.getKey().startsWith(buffer)) break;
                candidates.add(match.getValue());
            }
        }
        return candidates.isEmpty() ? -1 : 0;
    }
}

