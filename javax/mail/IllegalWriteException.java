/*
 * Decompiled with CFR 0.152.
 */
package javax.mail;

import javax.mail.MessagingException;

public class IllegalWriteException
extends MessagingException {
    private static final long serialVersionUID = 3974370223328268013L;

    public IllegalWriteException() {
    }

    public IllegalWriteException(String s) {
        super(s);
    }

    public IllegalWriteException(String s, Exception e) {
        super(s, e);
    }
}

