/*
 * Decompiled with CFR 0.152.
 */
package javax.mail;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;

public class MessageContext {
    private Part part;

    public MessageContext(Part part) {
        this.part = part;
    }

    public Part getPart() {
        return this.part;
    }

    public Message getMessage() {
        try {
            return MessageContext.getMessage(this.part);
        }
        catch (MessagingException ex) {
            return null;
        }
    }

    private static Message getMessage(Part p) throws MessagingException {
        while (p != null) {
            if (p instanceof Message) {
                return (Message)p;
            }
            BodyPart bp = (BodyPart)p;
            Multipart mp = bp.getParent();
            if (mp == null) {
                return null;
            }
            p = mp.getParent();
        }
        return null;
    }

    public Session getSession() {
        Message msg = this.getMessage();
        return msg != null ? msg.getSession() : null;
    }
}

