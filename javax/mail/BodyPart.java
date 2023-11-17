/*
 * Decompiled with CFR 0.152.
 */
package javax.mail;

import javax.mail.Multipart;
import javax.mail.Part;

public abstract class BodyPart
implements Part {
    protected Multipart parent;

    public Multipart getParent() {
        return this.parent;
    }

    void setParent(Multipart parent) {
        this.parent = parent;
    }
}

