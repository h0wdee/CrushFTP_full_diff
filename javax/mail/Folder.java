/*
 * Decompiled with CFR 0.152.
 */
package javax.mail;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Vector;
import java.util.concurrent.Executor;
import javax.mail.EventQueue;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.FolderEvent;
import javax.mail.event.FolderListener;
import javax.mail.event.MailEvent;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.search.SearchTerm;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class Folder {
    protected Store store;
    protected int mode = -1;
    private final EventQueue q;
    public static final int HOLDS_MESSAGES = 1;
    public static final int HOLDS_FOLDERS = 2;
    public static final int READ_ONLY = 1;
    public static final int READ_WRITE = 2;
    private volatile Vector<ConnectionListener> connectionListeners = null;
    private volatile Vector<FolderListener> folderListeners = null;
    private volatile Vector<MessageCountListener> messageCountListeners = null;
    private volatile Vector<MessageChangedListener> messageChangedListeners = null;

    protected Folder(Store store) {
        this.store = store;
        Session session = store.getSession();
        String scope = session.getProperties().getProperty("mail.event.scope", "folder");
        Executor executor = (Executor)session.getProperties().get("mail.event.executor");
        this.q = scope.equalsIgnoreCase("application") ? EventQueue.getApplicationEventQueue(executor) : (scope.equalsIgnoreCase("session") ? session.getEventQueue() : (scope.equalsIgnoreCase("store") ? store.getEventQueue() : new EventQueue(executor)));
    }

    public abstract String getName();

    public abstract String getFullName();

    public URLName getURLName() throws MessagingException {
        URLName storeURL = this.getStore().getURLName();
        String fullname = this.getFullName();
        StringBuffer encodedName = new StringBuffer();
        if (fullname != null) {
            encodedName.append(fullname);
        }
        return new URLName(storeURL.getProtocol(), storeURL.getHost(), storeURL.getPort(), encodedName.toString(), storeURL.getUsername(), null);
    }

    public Store getStore() {
        return this.store;
    }

    public abstract Folder getParent() throws MessagingException;

    public abstract boolean exists() throws MessagingException;

    public abstract Folder[] list(String var1) throws MessagingException;

    public Folder[] listSubscribed(String pattern) throws MessagingException {
        return this.list(pattern);
    }

    public Folder[] list() throws MessagingException {
        return this.list("%");
    }

    public Folder[] listSubscribed() throws MessagingException {
        return this.listSubscribed("%");
    }

    public abstract char getSeparator() throws MessagingException;

    public abstract int getType() throws MessagingException;

    public abstract boolean create(int var1) throws MessagingException;

    public boolean isSubscribed() {
        return true;
    }

    public void setSubscribed(boolean subscribe) throws MessagingException {
        throw new MethodNotSupportedException();
    }

    public abstract boolean hasNewMessages() throws MessagingException;

    public abstract Folder getFolder(String var1) throws MessagingException;

    public abstract boolean delete(boolean var1) throws MessagingException;

    public abstract boolean renameTo(Folder var1) throws MessagingException;

    public abstract void open(int var1) throws MessagingException;

    public abstract void close(boolean var1) throws MessagingException;

    public abstract boolean isOpen();

    public synchronized int getMode() {
        if (!this.isOpen()) {
            throw new IllegalStateException("Folder not open");
        }
        return this.mode;
    }

    public abstract Flags getPermanentFlags();

    public abstract int getMessageCount() throws MessagingException;

    public synchronized int getNewMessageCount() throws MessagingException {
        if (!this.isOpen()) {
            return -1;
        }
        int newmsgs = 0;
        int total = this.getMessageCount();
        for (int i = 1; i <= total; ++i) {
            try {
                if (!this.getMessage(i).isSet(Flags.Flag.RECENT)) continue;
                ++newmsgs;
                continue;
            }
            catch (MessageRemovedException me) {
                // empty catch block
            }
        }
        return newmsgs;
    }

    public synchronized int getUnreadMessageCount() throws MessagingException {
        if (!this.isOpen()) {
            return -1;
        }
        int unread = 0;
        int total = this.getMessageCount();
        for (int i = 1; i <= total; ++i) {
            try {
                if (this.getMessage(i).isSet(Flags.Flag.SEEN)) continue;
                ++unread;
                continue;
            }
            catch (MessageRemovedException me) {
                // empty catch block
            }
        }
        return unread;
    }

    public synchronized int getDeletedMessageCount() throws MessagingException {
        if (!this.isOpen()) {
            return -1;
        }
        int deleted = 0;
        int total = this.getMessageCount();
        for (int i = 1; i <= total; ++i) {
            try {
                if (!this.getMessage(i).isSet(Flags.Flag.DELETED)) continue;
                ++deleted;
                continue;
            }
            catch (MessageRemovedException me) {
                // empty catch block
            }
        }
        return deleted;
    }

    public abstract Message getMessage(int var1) throws MessagingException;

    public synchronized Message[] getMessages(int start, int end) throws MessagingException {
        Message[] msgs = new Message[end - start + 1];
        for (int i = start; i <= end; ++i) {
            msgs[i - start] = this.getMessage(i);
        }
        return msgs;
    }

    public synchronized Message[] getMessages(int[] msgnums) throws MessagingException {
        int len = msgnums.length;
        Message[] msgs = new Message[len];
        for (int i = 0; i < len; ++i) {
            msgs[i] = this.getMessage(msgnums[i]);
        }
        return msgs;
    }

    public synchronized Message[] getMessages() throws MessagingException {
        if (!this.isOpen()) {
            throw new IllegalStateException("Folder not open");
        }
        int total = this.getMessageCount();
        Message[] msgs = new Message[total];
        for (int i = 1; i <= total; ++i) {
            msgs[i - 1] = this.getMessage(i);
        }
        return msgs;
    }

    public abstract void appendMessages(Message[] var1) throws MessagingException;

    public void fetch(Message[] msgs, FetchProfile fp) throws MessagingException {
    }

    public synchronized void setFlags(Message[] msgs, Flags flag, boolean value) throws MessagingException {
        for (int i = 0; i < msgs.length; ++i) {
            try {
                msgs[i].setFlags(flag, value);
                continue;
            }
            catch (MessageRemovedException messageRemovedException) {
                // empty catch block
            }
        }
    }

    public synchronized void setFlags(int start, int end, Flags flag, boolean value) throws MessagingException {
        for (int i = start; i <= end; ++i) {
            try {
                Message msg = this.getMessage(i);
                msg.setFlags(flag, value);
                continue;
            }
            catch (MessageRemovedException messageRemovedException) {
                // empty catch block
            }
        }
    }

    public synchronized void setFlags(int[] msgnums, Flags flag, boolean value) throws MessagingException {
        for (int i = 0; i < msgnums.length; ++i) {
            try {
                Message msg = this.getMessage(msgnums[i]);
                msg.setFlags(flag, value);
                continue;
            }
            catch (MessageRemovedException messageRemovedException) {
                // empty catch block
            }
        }
    }

    public void copyMessages(Message[] msgs, Folder folder) throws MessagingException {
        if (!folder.exists()) {
            throw new FolderNotFoundException(folder.getFullName() + " does not exist", folder);
        }
        folder.appendMessages(msgs);
    }

    public abstract Message[] expunge() throws MessagingException;

    public Message[] search(SearchTerm term) throws MessagingException {
        return this.search(term, this.getMessages());
    }

    public Message[] search(SearchTerm term, Message[] msgs) throws MessagingException {
        ArrayList<Message> matchedMsgs = new ArrayList<Message>();
        for (Message msg : msgs) {
            try {
                if (!msg.match(term)) continue;
                matchedMsgs.add(msg);
            }
            catch (MessageRemovedException messageRemovedException) {
                // empty catch block
            }
        }
        return matchedMsgs.toArray(new Message[matchedMsgs.size()]);
    }

    public synchronized void addConnectionListener(ConnectionListener l) {
        if (this.connectionListeners == null) {
            this.connectionListeners = new Vector();
        }
        this.connectionListeners.addElement(l);
    }

    public synchronized void removeConnectionListener(ConnectionListener l) {
        if (this.connectionListeners != null) {
            this.connectionListeners.removeElement(l);
        }
    }

    protected void notifyConnectionListeners(int type) {
        if (this.connectionListeners != null) {
            ConnectionEvent e = new ConnectionEvent(this, type);
            this.queueEvent(e, this.connectionListeners);
        }
        if (type == 3) {
            this.q.terminateQueue();
        }
    }

    public synchronized void addFolderListener(FolderListener l) {
        if (this.folderListeners == null) {
            this.folderListeners = new Vector();
        }
        this.folderListeners.addElement(l);
    }

    public synchronized void removeFolderListener(FolderListener l) {
        if (this.folderListeners != null) {
            this.folderListeners.removeElement(l);
        }
    }

    protected void notifyFolderListeners(int type) {
        if (this.folderListeners != null) {
            FolderEvent e = new FolderEvent(this, this, type);
            this.queueEvent(e, this.folderListeners);
        }
        this.store.notifyFolderListeners(type, this);
    }

    protected void notifyFolderRenamedListeners(Folder folder) {
        if (this.folderListeners != null) {
            FolderEvent e = new FolderEvent(this, this, folder, 3);
            this.queueEvent(e, this.folderListeners);
        }
        this.store.notifyFolderRenamedListeners(this, folder);
    }

    public synchronized void addMessageCountListener(MessageCountListener l) {
        if (this.messageCountListeners == null) {
            this.messageCountListeners = new Vector();
        }
        this.messageCountListeners.addElement(l);
    }

    public synchronized void removeMessageCountListener(MessageCountListener l) {
        if (this.messageCountListeners != null) {
            this.messageCountListeners.removeElement(l);
        }
    }

    protected void notifyMessageAddedListeners(Message[] msgs) {
        if (this.messageCountListeners == null) {
            return;
        }
        MessageCountEvent e = new MessageCountEvent(this, 1, false, msgs);
        this.queueEvent(e, this.messageCountListeners);
    }

    protected void notifyMessageRemovedListeners(boolean removed, Message[] msgs) {
        if (this.messageCountListeners == null) {
            return;
        }
        MessageCountEvent e = new MessageCountEvent(this, 2, removed, msgs);
        this.queueEvent(e, this.messageCountListeners);
    }

    public synchronized void addMessageChangedListener(MessageChangedListener l) {
        if (this.messageChangedListeners == null) {
            this.messageChangedListeners = new Vector();
        }
        this.messageChangedListeners.addElement(l);
    }

    public synchronized void removeMessageChangedListener(MessageChangedListener l) {
        if (this.messageChangedListeners != null) {
            this.messageChangedListeners.removeElement(l);
        }
    }

    protected void notifyMessageChangedListeners(int type, Message msg) {
        if (this.messageChangedListeners == null) {
            return;
        }
        MessageChangedEvent e = new MessageChangedEvent(this, type, msg);
        this.queueEvent(e, this.messageChangedListeners);
    }

    private void queueEvent(MailEvent event, Vector<? extends EventListener> vector) {
        Vector v = (Vector)vector.clone();
        this.q.enqueue(event, v);
    }

    protected void finalize() throws Throwable {
        try {
            this.q.terminateQueue();
        }
        finally {
            super.finalize();
        }
    }

    public String toString() {
        String s = this.getFullName();
        if (s != null) {
            return s;
        }
        return super.toString();
    }
}

