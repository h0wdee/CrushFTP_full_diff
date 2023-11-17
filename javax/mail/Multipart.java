/*
 * Decompiled with CFR 0.152.
 */
package javax.mail;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.MultipartDataSource;
import javax.mail.Part;

public abstract class Multipart {
    protected Vector parts = new Vector();
    protected String contentType = "multipart/mixed";
    protected Part parent;

    protected Multipart() {
    }

    protected synchronized void setMultipartDataSource(MultipartDataSource mp) throws MessagingException {
        this.contentType = mp.getContentType();
        int count = mp.getCount();
        for (int i = 0; i < count; ++i) {
            this.addBodyPart(mp.getBodyPart(i));
        }
    }

    public synchronized String getContentType() {
        return this.contentType;
    }

    public synchronized int getCount() throws MessagingException {
        if (this.parts == null) {
            return 0;
        }
        return this.parts.size();
    }

    public synchronized BodyPart getBodyPart(int index) throws MessagingException {
        if (this.parts == null) {
            throw new IndexOutOfBoundsException("No such BodyPart");
        }
        return (BodyPart)this.parts.elementAt(index);
    }

    public synchronized boolean removeBodyPart(BodyPart part) throws MessagingException {
        if (this.parts == null) {
            throw new MessagingException("No such body part");
        }
        boolean ret = this.parts.removeElement(part);
        part.setParent(null);
        return ret;
    }

    public synchronized void removeBodyPart(int index) throws MessagingException {
        if (this.parts == null) {
            throw new IndexOutOfBoundsException("No such BodyPart");
        }
        BodyPart part = (BodyPart)this.parts.elementAt(index);
        this.parts.removeElementAt(index);
        part.setParent(null);
    }

    public synchronized void addBodyPart(BodyPart part) throws MessagingException {
        if (this.parts == null) {
            this.parts = new Vector();
        }
        this.parts.addElement(part);
        part.setParent(this);
    }

    public synchronized void addBodyPart(BodyPart part, int index) throws MessagingException {
        if (this.parts == null) {
            this.parts = new Vector();
        }
        this.parts.insertElementAt(part, index);
        part.setParent(this);
    }

    public abstract void writeTo(OutputStream var1) throws IOException, MessagingException;

    public synchronized Part getParent() {
        return this.parent;
    }

    public synchronized void setParent(Part parent) {
        this.parent = parent;
    }
}

