/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.imap;

import java.util.ArrayList;

public class Rights
implements Cloneable {
    private boolean[] rights = new boolean[128];

    public Rights() {
    }

    public Rights(Rights rights) {
        System.arraycopy(rights.rights, 0, this.rights, 0, this.rights.length);
    }

    public Rights(String rights) {
        for (int i = 0; i < rights.length(); ++i) {
            this.add(Right.getInstance(rights.charAt(i)));
        }
    }

    public Rights(Right right) {
        this.rights[right.right] = true;
    }

    public void add(Right right) {
        this.rights[right.right] = true;
    }

    public void add(Rights rights) {
        for (int i = 0; i < rights.rights.length; ++i) {
            if (!rights.rights[i]) continue;
            this.rights[i] = true;
        }
    }

    public void remove(Right right) {
        this.rights[right.right] = false;
    }

    public void remove(Rights rights) {
        for (int i = 0; i < rights.rights.length; ++i) {
            if (!rights.rights[i]) continue;
            this.rights[i] = false;
        }
    }

    public boolean contains(Right right) {
        return this.rights[right.right];
    }

    public boolean contains(Rights rights) {
        for (int i = 0; i < rights.rights.length; ++i) {
            if (!rights.rights[i] || this.rights[i]) continue;
            return false;
        }
        return true;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Rights)) {
            return false;
        }
        Rights rights = (Rights)obj;
        for (int i = 0; i < rights.rights.length; ++i) {
            if (rights.rights[i] == this.rights[i]) continue;
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < this.rights.length; ++i) {
            if (!this.rights[i]) continue;
            ++hash;
        }
        return hash;
    }

    public Right[] getRights() {
        ArrayList<Right> v = new ArrayList<Right>();
        for (int i = 0; i < this.rights.length; ++i) {
            if (!this.rights[i]) continue;
            v.add(Right.getInstance((char)i));
        }
        return v.toArray(new Right[v.size()]);
    }

    public Object clone() {
        Rights r = null;
        try {
            r = (Rights)super.clone();
            r.rights = new boolean[128];
            System.arraycopy(this.rights, 0, r.rights, 0, this.rights.length);
        }
        catch (CloneNotSupportedException cloneNotSupportedException) {
            // empty catch block
        }
        return r;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < this.rights.length; ++i) {
            if (!this.rights[i]) continue;
            sb.append((char)i);
        }
        return sb.toString();
    }

    public static final class Right {
        private static Right[] cache = new Right[128];
        public static final Right LOOKUP = Right.getInstance('l');
        public static final Right READ = Right.getInstance('r');
        public static final Right KEEP_SEEN = Right.getInstance('s');
        public static final Right WRITE = Right.getInstance('w');
        public static final Right INSERT = Right.getInstance('i');
        public static final Right POST = Right.getInstance('p');
        public static final Right CREATE = Right.getInstance('c');
        public static final Right DELETE = Right.getInstance('d');
        public static final Right ADMINISTER = Right.getInstance('a');
        char right;

        private Right(char right) {
            if (right >= '\u0080') {
                throw new IllegalArgumentException("Right must be ASCII");
            }
            this.right = right;
        }

        public static synchronized Right getInstance(char right) {
            if (right >= '\u0080') {
                throw new IllegalArgumentException("Right must be ASCII");
            }
            if (cache[right] == null) {
                Right.cache[right] = new Right(right);
            }
            return cache[right];
        }

        public String toString() {
            return String.valueOf(this.right);
        }
    }
}

