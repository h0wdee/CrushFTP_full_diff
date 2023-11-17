/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import java.util.ArrayList;
import java.util.List;

public class Arrays {
    private static int med3(int[] x, int a, int b, int c) {
        return x[a] < x[b] ? (x[b] < x[c] ? b : (x[a] < x[c] ? c : a)) : (x[b] > x[c] ? b : (x[a] > x[c] ? c : a));
    }

    private static void swap(int[] x, int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    public static void sort(int[] a) {
        Arrays.sort1(a, 0, a.length);
    }

    private static void sort1(int[] x, int off, int len) {
        int c;
        int a;
        if (len < 7) {
            for (int i = off; i < len + off; ++i) {
                for (int j = i; j > off && x[j - 1] > x[j]; --j) {
                    Arrays.swap(x, j, j - 1);
                }
            }
            return;
        }
        int m = off + (len >> 1);
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {
                int s = len / 8;
                l = Arrays.med3(x, l, l + s, l + 2 * s);
                m = Arrays.med3(x, m - s, m, m + s);
                n = Arrays.med3(x, n - 2 * s, n - s, n);
            }
            m = Arrays.med3(x, l, m, n);
        }
        int v = x[m];
        int b = a = off;
        int d = c = off + len - 1;
        while (true) {
            if (b <= c && x[b] <= v) {
                if (x[b] == v) {
                    Arrays.swap(x, a++, b);
                }
                ++b;
                continue;
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v) {
                    Arrays.swap(x, c, d--);
                }
                --c;
            }
            if (b > c) break;
            Arrays.swap(x, b++, c--);
        }
    }

    public static boolean areEqual(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; ++i) {
            if (a[i] == b[i]) continue;
            return false;
        }
        return true;
    }

    public static <T> List<T> asList(T ... elements) {
        ArrayList<T> tmp = new ArrayList<T>();
        for (T element : elements) {
            tmp.add(element);
        }
        return tmp;
    }

    public static <T> T[] add(T obj, T ... array) {
        Object[] res = new Object[array.length + 1];
        System.arraycopy(array, 0, res, 0, array.length);
        res[array.length] = obj;
        return res;
    }

    public static boolean areEqual(char[] a, char[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; ++i) {
            if (a[i] == b[i]) continue;
            return false;
        }
        return true;
    }

    public static byte[] copy(byte[] array, int len) {
        return Arrays.copy(array, 0, len);
    }

    public static byte[] copy(byte[] array, int offset, int len) {
        byte[] tmp = new byte[len];
        System.arraycopy(array, offset, tmp, 0, len);
        return tmp;
    }

    public static byte[] cat(byte[] a, byte[] b) {
        byte[] tmp = new byte[a.length + b.length];
        System.arraycopy(a, 0, tmp, 0, a.length);
        System.arraycopy(b, 0, tmp, a.length, b.length);
        return tmp;
    }

    public static boolean compare(byte[] arr, byte[] arr2) {
        if (arr.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr.length; ++i) {
            if (arr[i] == arr2[i]) continue;
            return false;
        }
        return true;
    }
}

