/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.protocol.commons;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public interface EnumWithValue<E extends Enum<E>> {
    public long getValue();

    public static class EnumUtils {
        public static <E extends Enum<E>> long toLong(Collection<E> set) {
            long l = 0L;
            for (Enum e : set) {
                if (e instanceof EnumWithValue) {
                    l |= ((EnumWithValue)((Object)e)).getValue();
                    continue;
                }
                throw new IllegalArgumentException("Can only be used with EnumWithValue enums.");
            }
            return l;
        }

        public static <E extends Enum<E>> EnumSet<E> toEnumSet(long l, Class<E> clazz) {
            if (!EnumWithValue.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Can only be used with EnumWithValue enums.");
            }
            EnumSet<Enum> es = EnumSet.noneOf(clazz);
            for (Enum anEnum : (Enum[])clazz.getEnumConstants()) {
                if (!EnumUtils.isSet(l, (EnumWithValue)((Object)anEnum))) continue;
                es.add(anEnum);
            }
            return es;
        }

        public static <E extends EnumWithValue<?>> boolean isSet(long bytes, E value) {
            return (bytes & value.getValue()) > 0L;
        }

        public static <E extends EnumWithValue<?>> E valueOf(long l, Class<E> enumClass, E defaultValue) {
            EnumWithValue[] enumConstants;
            for (EnumWithValue enumConstant : enumConstants = (EnumWithValue[])enumClass.getEnumConstants()) {
                if (enumConstant.getValue() != l) continue;
                return (E)enumConstant;
            }
            return defaultValue;
        }

        public static <E extends Enum<E>> Set<E> ensureNotNull(Set<E> set, Class<E> clazz) {
            if (set == null) {
                return EnumSet.noneOf(clazz);
            }
            return set;
        }

        public static <E extends Enum<E>> E ensureNotNull(E value, E defaultValue) {
            return value != null ? value : defaultValue;
        }
    }
}

