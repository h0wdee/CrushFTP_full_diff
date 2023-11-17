/*
 * Decompiled with CFR 0.152.
 */
package javax.mail;

import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.MessagingException;

public interface UIDFolder {
    public static final long LASTUID = -1L;

    public long getUIDValidity() throws MessagingException;

    public Message getMessageByUID(long var1) throws MessagingException;

    public Message[] getMessagesByUID(long var1, long var3) throws MessagingException;

    public Message[] getMessagesByUID(long[] var1) throws MessagingException;

    public long getUID(Message var1) throws MessagingException;

    public static class FetchProfileItem
    extends FetchProfile.Item {
        public static final FetchProfileItem UID = new FetchProfileItem("UID");

        protected FetchProfileItem(String name) {
            super(name);
        }
    }
}

