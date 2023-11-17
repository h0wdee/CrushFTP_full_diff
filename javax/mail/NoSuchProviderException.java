/*
 * Decompiled with CFR 0.152.
 */
package javax.mail;

import javax.mail.MessagingException;

public class NoSuchProviderException
extends MessagingException {
    private static final long serialVersionUID = 8058319293154708827L;

    public NoSuchProviderException() {
    }

    public NoSuchProviderException(String message) {
        super(message);
    }

    public NoSuchProviderException(String message, Exception e) {
        super(message, e);
    }
}

