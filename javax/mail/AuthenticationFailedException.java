/*
 * Decompiled with CFR 0.152.
 */
package javax.mail;

import javax.mail.MessagingException;

public class AuthenticationFailedException
extends MessagingException {
    private static final long serialVersionUID = 492080754054436511L;

    public AuthenticationFailedException() {
    }

    public AuthenticationFailedException(String message) {
        super(message);
    }

    public AuthenticationFailedException(String message, Exception e) {
        super(message, e);
    }
}

