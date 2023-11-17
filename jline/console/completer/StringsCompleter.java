/*
 * Decompiled with CFR 0.152.
 */
package jline.console.completer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import jline.console.completer.Completer;
import jline.internal.Preconditions;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class StringsCompleter
implements Completer {
    private final SortedSet<String> strings = new TreeSet<String>();

    public StringsCompleter() {
    }

    public StringsCompleter(Collection<String> strings) {
        Preconditions.checkNotNull(strings);
        this.getStrings().addAll(strings);
    }

    public StringsCompleter(String ... strings) {
        this(Arrays.asList(strings));
    }

    public Collection<String> getStrings() {
        return this.strings;
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        Preconditions.checkNotNull(candidates);
        if (buffer == null) {
            candidates.addAll(this.strings);
        } else {
            String match;
            Iterator iterator = this.strings.tailSet(buffer).iterator();
            while (iterator.hasNext() && (match = (String)iterator.next()).startsWith(buffer)) {
                candidates.add(match);
            }
        }
        return candidates.isEmpty() ? -1 : 0;
    }
}

