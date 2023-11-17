/*
 * Decompiled with CFR 0.152.
 */
package jline.console.completer;

import jline.console.completer.StringsCompleter;
import jline.internal.Preconditions;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class EnumCompleter
extends StringsCompleter {
    public EnumCompleter(Class<? extends Enum<?>> source) {
        this(source, true);
    }

    public EnumCompleter(Class<? extends Enum<?>> source, boolean toLowerCase) {
        Preconditions.checkNotNull(source);
        for (Enum<?> n : source.getEnumConstants()) {
            this.getStrings().add(toLowerCase ? n.name().toLowerCase() : n.name());
        }
    }
}

