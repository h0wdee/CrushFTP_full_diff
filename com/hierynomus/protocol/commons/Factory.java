/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.protocol.commons;

import java.util.LinkedList;
import java.util.List;

public interface Factory<T> {
    public T create();

    public static interface Named<T>
    extends Factory<T> {
        public String getName();

        public static class Util {
            public static <T> T create(List<Named<T>> factories, String name) {
                Named<T> factory = Util.get(factories, name);
                if (factory != null) {
                    return factory.create();
                }
                return null;
            }

            public static <T> Named<T> get(List<Named<T>> factories, String name) {
                if (factories != null) {
                    for (Named<T> f : factories) {
                        if (!f.getName().equals(name)) continue;
                        return f;
                    }
                }
                return null;
            }

            public static <T> List<String> getNames(List<Named<T>> factories) {
                LinkedList<String> list = new LinkedList<String>();
                for (Named<T> f : factories) {
                    list.add(f.getName());
                }
                return list;
            }
        }
    }
}

