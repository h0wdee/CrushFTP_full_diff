/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.imap;

import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseHandler;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.CopyUID;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.IdleManager;
import com.sun.mail.imap.MessageCache;
import com.sun.mail.imap.MessageLiteral;
import com.sun.mail.imap.MessageVanishedEvent;
import com.sun.mail.imap.ResyncData;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.SortTerm;
import com.sun.mail.imap.Utility;
import com.sun.mail.imap.protocol.FLAGS;
import com.sun.mail.imap.protocol.FetchItem;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.ListInfo;
import com.sun.mail.imap.protocol.MODSEQ;
import com.sun.mail.imap.protocol.MailboxInfo;
import com.sun.mail.imap.protocol.MessageSet;
import com.sun.mail.imap.protocol.Status;
import com.sun.mail.imap.protocol.UID;
import com.sun.mail.imap.protocol.UIDSet;
import com.sun.mail.util.MailLogger;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Quota;
import javax.mail.ReadOnlyFolderException;
import javax.mail.StoreClosedException;
import javax.mail.UIDFolder;
import javax.mail.event.MailEvent;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchException;
import javax.mail.search.SearchTerm;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class IMAPFolder
extends Folder
implements UIDFolder,
ResponseHandler {
    protected volatile String fullName;
    protected String name;
    protected int type;
    protected char separator;
    protected Flags availableFlags;
    protected Flags permanentFlags;
    protected volatile boolean exists;
    protected boolean isNamespace = false;
    protected volatile String[] attributes;
    protected volatile IMAPProtocol protocol;
    protected MessageCache messageCache;
    protected final Object messageCacheLock = new Object();
    protected Hashtable<Long, IMAPMessage> uidTable;
    protected static final char UNKNOWN_SEPARATOR = '\uffff';
    private volatile boolean opened = false;
    private boolean reallyClosed = true;
    private static final int RUNNING = 0;
    private static final int IDLE = 1;
    private static final int ABORTING = 2;
    private int idleState = 0;
    private IdleManager idleManager;
    private volatile int total = -1;
    private volatile int recent = -1;
    private int realTotal = -1;
    private long uidvalidity = -1L;
    private long uidnext = -1L;
    private volatile long highestmodseq = -1L;
    private boolean doExpungeNotification = true;
    private Status cachedStatus = null;
    private long cachedStatusTime = 0L;
    private boolean hasMessageCountListener = false;
    protected MailLogger logger;
    private MailLogger connectionPoolLogger;

    protected IMAPFolder(String fullName, char separator, IMAPStore store, Boolean isNamespace) {
        super(store);
        int i;
        if (fullName == null) {
            throw new NullPointerException("Folder name is null");
        }
        this.fullName = fullName;
        this.separator = separator;
        this.logger = new MailLogger(this.getClass(), "DEBUG IMAP", store.getSession());
        this.connectionPoolLogger = store.getConnectionPoolLogger();
        this.isNamespace = false;
        if (separator != '\uffff' && separator != '\u0000' && (i = this.fullName.indexOf(separator)) > 0 && i == this.fullName.length() - 1) {
            this.fullName = this.fullName.substring(0, i);
            this.isNamespace = true;
        }
        if (isNamespace != null) {
            this.isNamespace = isNamespace;
        }
    }

    protected IMAPFolder(ListInfo li, IMAPStore store) {
        this(li.name, li.separator, store, null);
        if (li.hasInferiors) {
            this.type |= 2;
        }
        if (li.canOpen) {
            this.type |= 1;
        }
        this.exists = true;
        this.attributes = li.attrs;
    }

    protected void checkExists() throws MessagingException {
        if (!this.exists && !this.exists()) {
            throw new FolderNotFoundException(this, this.fullName + " not found");
        }
    }

    protected void checkClosed() {
        if (this.opened) {
            throw new IllegalStateException("This operation is not allowed on an open folder");
        }
    }

    protected void checkOpened() throws FolderClosedException {
        assert (Thread.holdsLock(this));
        if (!this.opened) {
            if (this.reallyClosed) {
                throw new IllegalStateException("This operation is not allowed on a closed folder");
            }
            throw new FolderClosedException(this, "Lost folder connection to server");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void checkRange(int msgno) throws MessagingException {
        if (msgno < 1) {
            throw new IndexOutOfBoundsException("message number < 1");
        }
        if (msgno <= this.total) {
            return;
        }
        Object object = this.messageCacheLock;
        synchronized (object) {
            try {
                this.keepConnectionAlive(false);
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (msgno > this.total) {
            throw new IndexOutOfBoundsException(msgno + " > " + this.total);
        }
    }

    private void checkFlags(Flags flags) throws MessagingException {
        assert (Thread.holdsLock(this));
        if (this.mode != 2) {
            throw new IllegalStateException("Cannot change flags on READ_ONLY folder: " + this.fullName);
        }
    }

    @Override
    public synchronized String getName() {
        if (this.name == null) {
            try {
                this.name = this.fullName.substring(this.fullName.lastIndexOf(this.getSeparator()) + 1);
            }
            catch (MessagingException messagingException) {
                // empty catch block
            }
        }
        return this.name;
    }

    @Override
    public String getFullName() {
        return this.fullName;
    }

    @Override
    public synchronized Folder getParent() throws MessagingException {
        char c = this.getSeparator();
        int index = this.fullName.lastIndexOf(c);
        if (index != -1) {
            return ((IMAPStore)this.store).newIMAPFolder(this.fullName.substring(0, index), c);
        }
        return new DefaultFolder((IMAPStore)this.store);
    }

    @Override
    public synchronized boolean exists() throws MessagingException {
        ListInfo[] li = null;
        final String lname = this.isNamespace && this.separator != '\u0000' ? this.fullName + this.separator : this.fullName;
        li = (ListInfo[])this.doCommand(new ProtocolCommand(){

            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.list("", lname);
            }
        });
        if (li != null) {
            int i = this.findName(li, lname);
            this.fullName = li[i].name;
            this.separator = li[i].separator;
            int len = this.fullName.length();
            if (this.separator != '\u0000' && len > 0 && this.fullName.charAt(len - 1) == this.separator) {
                this.fullName = this.fullName.substring(0, len - 1);
            }
            this.type = 0;
            if (li[i].hasInferiors) {
                this.type |= 2;
            }
            if (li[i].canOpen) {
                this.type |= 1;
            }
            this.exists = true;
            this.attributes = li[i].attrs;
        } else {
            this.exists = this.opened;
            this.attributes = null;
        }
        return this.exists;
    }

    private int findName(ListInfo[] li, String lname) {
        int i;
        for (i = 0; i < li.length && !li[i].name.equals(lname); ++i) {
        }
        if (i >= li.length) {
            i = 0;
        }
        return i;
    }

    @Override
    public Folder[] list(String pattern) throws MessagingException {
        return this.doList(pattern, false);
    }

    @Override
    public Folder[] listSubscribed(String pattern) throws MessagingException {
        return this.doList(pattern, true);
    }

    private synchronized Folder[] doList(final String pattern, final boolean subscribed) throws MessagingException {
        this.checkExists();
        if (this.attributes != null && !this.isDirectory()) {
            return new Folder[0];
        }
        final char c = this.getSeparator();
        ListInfo[] li = (ListInfo[])this.doCommandIgnoreFailure(new ProtocolCommand(){

            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                if (subscribed) {
                    return p.lsub("", IMAPFolder.this.fullName + c + pattern);
                }
                return p.list("", IMAPFolder.this.fullName + c + pattern);
            }
        });
        if (li == null) {
            return new Folder[0];
        }
        int start = 0;
        if (li.length > 0 && li[0].name.equals(this.fullName + c)) {
            start = 1;
        }
        Folder[] folders = new IMAPFolder[li.length - start];
        IMAPStore st = (IMAPStore)this.store;
        for (int i = start; i < li.length; ++i) {
            folders[i - start] = st.newIMAPFolder(li[i]);
        }
        return folders;
    }

    @Override
    public synchronized char getSeparator() throws MessagingException {
        if (this.separator == '\uffff') {
            ListInfo[] li = null;
            li = (ListInfo[])this.doCommand(new ProtocolCommand(){

                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    if (p.isREV1()) {
                        return p.list(IMAPFolder.this.fullName, "");
                    }
                    return p.list("", IMAPFolder.this.fullName);
                }
            });
            this.separator = li != null ? li[0].separator : (char)47;
        }
        return this.separator;
    }

    @Override
    public synchronized int getType() throws MessagingException {
        if (this.opened) {
            if (this.attributes == null) {
                this.exists();
            }
        } else {
            this.checkExists();
        }
        return this.type;
    }

    @Override
    public synchronized boolean isSubscribed() {
        ListInfo[] li = null;
        final String lname = this.isNamespace && this.separator != '\u0000' ? this.fullName + this.separator : this.fullName;
        try {
            li = (ListInfo[])this.doProtocolCommand(new ProtocolCommand(){

                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    return p.lsub("", lname);
                }
            });
        }
        catch (ProtocolException protocolException) {
            // empty catch block
        }
        if (li != null) {
            int i = this.findName(li, lname);
            return li[i].canOpen;
        }
        return false;
    }

    @Override
    public synchronized void setSubscribed(final boolean subscribe) throws MessagingException {
        this.doCommandIgnoreFailure(new ProtocolCommand(){

            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                if (subscribe) {
                    p.subscribe(IMAPFolder.this.fullName);
                } else {
                    p.unsubscribe(IMAPFolder.this.fullName);
                }
                return null;
            }
        });
    }

    @Override
    public synchronized boolean create(final int type) throws MessagingException {
        char sep;
        Object ret;
        char c = '\u0000';
        if ((type & 1) == 0) {
            c = this.getSeparator();
        }
        if ((ret = this.doCommandIgnoreFailure(new ProtocolCommand(sep = c){
            final /* synthetic */ char val$sep;
            {
                this.val$sep = c;
            }

            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                if ((type & 1) == 0) {
                    p.create(IMAPFolder.this.fullName + this.val$sep);
                } else {
                    ListInfo[] li;
                    p.create(IMAPFolder.this.fullName);
                    if ((type & 2) != 0 && (li = p.list("", IMAPFolder.this.fullName)) != null && !li[0].hasInferiors) {
                        p.delete(IMAPFolder.this.fullName);
                        throw new ProtocolException("Unsupported type");
                    }
                }
                return Boolean.TRUE;
            }
        })) == null) {
            return false;
        }
        boolean retb = this.exists();
        if (retb) {
            this.notifyFolderListeners(1);
        }
        return retb;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized boolean hasNewMessages() throws MessagingException {
        Object object = this.messageCacheLock;
        synchronized (object) {
            if (this.opened) {
                try {
                    this.keepConnectionAlive(true);
                }
                catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                }
                catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
                return this.recent > 0;
            }
        }
        ListInfo[] li = null;
        final String lname = this.isNamespace && this.separator != '\u0000' ? this.fullName + this.separator : this.fullName;
        li = (ListInfo[])this.doCommandIgnoreFailure(new ProtocolCommand(){

            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.list("", lname);
            }
        });
        if (li == null) {
            throw new FolderNotFoundException(this, this.fullName + " not found");
        }
        int i = this.findName(li, lname);
        if (li[i].changeState == 1) {
            return true;
        }
        if (li[i].changeState == 2) {
            return false;
        }
        try {
            Status status = this.getStatus();
            return status.recent > 0;
        }
        catch (BadCommandException bex) {
            return false;
        }
        catch (ConnectionException cex) {
            throw new StoreClosedException(this.store, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    @Override
    public synchronized Folder getFolder(String name) throws MessagingException {
        if (this.attributes != null && !this.isDirectory()) {
            throw new MessagingException("Cannot contain subfolders");
        }
        char c = this.getSeparator();
        return ((IMAPStore)this.store).newIMAPFolder(this.fullName + c + name, c);
    }

    @Override
    public synchronized boolean delete(boolean recurse) throws MessagingException {
        Object ret;
        this.checkClosed();
        if (recurse) {
            Folder[] f = this.list();
            for (int i = 0; i < f.length; ++i) {
                f[i].delete(recurse);
            }
        }
        if ((ret = this.doCommandIgnoreFailure(new ProtocolCommand(){

            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                p.delete(IMAPFolder.this.fullName);
                return Boolean.TRUE;
            }
        })) == null) {
            return false;
        }
        this.exists = false;
        this.attributes = null;
        this.notifyFolderListeners(2);
        return true;
    }

    @Override
    public synchronized boolean renameTo(final Folder f) throws MessagingException {
        this.checkClosed();
        this.checkExists();
        if (f.getStore() != this.store) {
            throw new MessagingException("Can't rename across Stores");
        }
        Object ret = this.doCommandIgnoreFailure(new ProtocolCommand(){

            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                p.rename(IMAPFolder.this.fullName, f.getFullName());
                return Boolean.TRUE;
            }
        });
        if (ret == null) {
            return false;
        }
        this.exists = false;
        this.attributes = null;
        this.notifyFolderRenamedListeners(f);
        return true;
    }

    @Override
    public synchronized void open(int mode) throws MessagingException {
        this.open(mode, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public synchronized List<MailEvent> open(int mode, ResyncData rd) throws MessagingException {
        this.checkClosed();
        MailboxInfo mi = null;
        this.protocol = ((IMAPStore)this.store).getProtocol(this);
        ArrayList<MailEvent> openEvents = null;
        Object object = this.messageCacheLock;
        synchronized (object) {
            this.protocol.addResponseHandler(this);
            try {
                if (rd != null) {
                    if (rd == ResyncData.CONDSTORE) {
                        if (!this.protocol.isEnabled("CONDSTORE") && !this.protocol.isEnabled("QRESYNC")) {
                            if (this.protocol.hasCapability("CONDSTORE")) {
                                this.protocol.enable("CONDSTORE");
                            } else {
                                this.protocol.enable("QRESYNC");
                            }
                        }
                    } else if (!this.protocol.isEnabled("QRESYNC")) {
                        this.protocol.enable("QRESYNC");
                    }
                }
                mi = mode == 1 ? this.protocol.examine(this.fullName, rd) : this.protocol.select(this.fullName, rd);
            }
            catch (CommandFailedException cex) {
                try {
                    this.checkExists();
                    if ((this.type & 1) != 0) throw new MessagingException(cex.getMessage(), cex);
                    throw new MessagingException("folder cannot contain messages");
                }
                catch (Throwable throwable) {
                    Object var8_14 = null;
                    this.exists = false;
                    this.attributes = null;
                    this.type = 0;
                    this.releaseProtocol(true);
                    throw throwable;
                }
            }
            catch (ProtocolException pex) {
                try {
                    try {
                        this.protocol.logout();
                    }
                    catch (ProtocolException protocolException) {
                        Object var10_17 = null;
                        this.releaseProtocol(false);
                        throw new MessagingException(pex.getMessage(), pex);
                    }
                    Object var10_16 = null;
                    this.releaseProtocol(false);
                    throw new MessagingException(pex.getMessage(), pex);
                }
                catch (Throwable throwable) {
                    Object var10_18 = null;
                    this.releaseProtocol(false);
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
            if (!(mi.mode == mode || mode == 2 && mi.mode == 1 && ((IMAPStore)this.store).allowReadOnlySelect())) {
                try {
                    try {
                        this.protocol.close();
                        this.releaseProtocol(true);
                    }
                    catch (ProtocolException pex) {
                        block33: {
                            try {
                                try {
                                    this.protocol.logout();
                                }
                                catch (ProtocolException protocolException) {
                                    Object var12_26 = null;
                                    this.releaseProtocol(false);
                                    break block33;
                                }
                                Object var12_25 = null;
                                this.releaseProtocol(false);
                            }
                            catch (Throwable throwable) {
                                Object var12_27 = null;
                                this.releaseProtocol(false);
                                throw throwable;
                            }
                        }
                        Object var14_23 = null;
                        throw new ReadOnlyFolderException(this, "Cannot open in desired mode");
                    }
                    Object var14_22 = null;
                    throw new ReadOnlyFolderException(this, "Cannot open in desired mode");
                }
                catch (Throwable throwable) {
                    Object var14_24 = null;
                    throw new ReadOnlyFolderException(this, "Cannot open in desired mode");
                }
            }
            this.opened = true;
            this.reallyClosed = false;
            this.mode = mi.mode;
            this.availableFlags = mi.availableFlags;
            this.permanentFlags = mi.permanentFlags;
            this.total = this.realTotal = mi.total;
            this.recent = mi.recent;
            this.uidvalidity = mi.uidvalidity;
            this.uidnext = mi.uidnext;
            this.highestmodseq = mi.highestmodseq;
            this.messageCache = new MessageCache(this, (IMAPStore)this.store, this.total);
            if (mi.responses != null) {
                openEvents = new ArrayList<MailEvent>();
                for (IMAPResponse ir : mi.responses) {
                    if (ir.keyEquals("VANISHED")) {
                        String uids;
                        UIDSet[] uidset;
                        long[] luid;
                        String[] s = ir.readAtomStringList();
                        if (s == null || s.length != 1 || !s[0].equalsIgnoreCase("EARLIER") || (luid = UIDSet.toArray(uidset = UIDSet.parseUIDSets(uids = ir.readAtom()), this.uidnext)) == null || luid.length <= 0) continue;
                        openEvents.add(new MessageVanishedEvent(this, luid));
                        continue;
                    }
                    if (!ir.keyEquals("FETCH")) continue;
                    assert (ir instanceof FetchResponse) : "!ir instanceof FetchResponse";
                    Message msg = this.processFetchResponse((FetchResponse)ir);
                    if (msg == null) continue;
                    openEvents.add(new MessageChangedEvent(this, 1, msg));
                }
            }
        }
        this.exists = true;
        this.attributes = null;
        this.type = 1;
        this.notifyConnectionListeners(1);
        return openEvents;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized void fetch(Message[] msgs, FetchProfile fp) throws MessagingException {
        FetchItem[] fitems;
        boolean isRev1;
        Object object = this.messageCacheLock;
        synchronized (object) {
            this.checkOpened();
            isRev1 = this.protocol.isREV1();
            fitems = this.protocol.getFetchItems();
        }
        StringBuffer command = new StringBuffer();
        boolean first = true;
        boolean allHeaders = false;
        if (fp.contains(FetchProfile.Item.ENVELOPE)) {
            command.append(this.getEnvelopeCommand());
            first = false;
        }
        if (fp.contains(FetchProfile.Item.FLAGS)) {
            command.append(first ? "FLAGS" : " FLAGS");
            first = false;
        }
        if (fp.contains(FetchProfile.Item.CONTENT_INFO)) {
            command.append(first ? "BODYSTRUCTURE" : " BODYSTRUCTURE");
            first = false;
        }
        if (fp.contains(UIDFolder.FetchProfileItem.UID)) {
            command.append(first ? "UID" : " UID");
            first = false;
        }
        if (fp.contains(FetchProfileItem.HEADERS)) {
            allHeaders = true;
            if (isRev1) {
                command.append(first ? "BODY.PEEK[HEADER]" : " BODY.PEEK[HEADER]");
            } else {
                command.append(first ? "RFC822.HEADER" : " RFC822.HEADER");
            }
            first = false;
        }
        if (fp.contains(FetchProfileItem.MESSAGE)) {
            allHeaders = true;
            if (isRev1) {
                command.append(first ? "BODY.PEEK[]" : " BODY.PEEK[]");
            } else {
                command.append(first ? "RFC822" : " RFC822");
            }
            first = false;
        }
        if (fp.contains(FetchProfile.Item.SIZE) || fp.contains(FetchProfileItem.SIZE)) {
            command.append(first ? "RFC822.SIZE" : " RFC822.SIZE");
            first = false;
        }
        if (fp.contains(FetchProfileItem.INTERNALDATE)) {
            command.append(first ? "INTERNALDATE" : " INTERNALDATE");
            first = false;
        }
        String[] hdrs = null;
        if (!allHeaders && (hdrs = fp.getHeaderNames()).length > 0) {
            if (!first) {
                command.append(" ");
            }
            command.append(this.createHeaderCommand(hdrs, isRev1));
        }
        for (int i = 0; i < fitems.length; ++i) {
            if (!fp.contains(fitems[i].getFetchProfileItem())) continue;
            if (command.length() != 0) {
                command.append(" ");
            }
            command.append(fitems[i].getName());
        }
        IMAPMessage.FetchProfileCondition condition = new IMAPMessage.FetchProfileCondition(fp, fitems);
        Object object2 = this.messageCacheLock;
        synchronized (object2) {
            this.checkOpened();
            MessageSet[] msgsets = Utility.toMessageSetSorted(msgs, condition);
            if (msgsets == null) {
                return;
            }
            Response[] r = null;
            ArrayList<Response> v = new ArrayList<Response>();
            try {
                r = this.getProtocol().fetch(msgsets, command.toString());
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            }
            catch (CommandFailedException cex) {
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
            if (r == null) {
                return;
            }
            for (int i = 0; i < r.length; ++i) {
                if (r[i] == null) continue;
                if (!(r[i] instanceof FetchResponse)) {
                    v.add(r[i]);
                    continue;
                }
                FetchResponse f = (FetchResponse)r[i];
                IMAPMessage msg = this.getMessageBySeqNumber(f.getNumber());
                int count = f.getItemCount();
                boolean unsolicitedFlags = false;
                for (int j = 0; j < count; ++j) {
                    Item item = f.getItem(j);
                    if (item instanceof Flags && (!fp.contains(FetchProfile.Item.FLAGS) || msg == null)) {
                        unsolicitedFlags = true;
                        continue;
                    }
                    if (msg == null) continue;
                    msg.handleFetchItem(item, hdrs, allHeaders);
                }
                if (msg != null) {
                    msg.handleExtensionFetchItems(f.getExtensionItems());
                }
                if (!unsolicitedFlags) continue;
                v.add(f);
            }
            if (!v.isEmpty()) {
                Response[] responses = new Response[v.size()];
                v.toArray(responses);
                this.handleResponses(responses);
            }
        }
    }

    protected String getEnvelopeCommand() {
        return "ENVELOPE INTERNALDATE RFC822.SIZE";
    }

    protected IMAPMessage newIMAPMessage(int msgnum) {
        return new IMAPMessage(this, msgnum);
    }

    private String createHeaderCommand(String[] hdrs, boolean isRev1) {
        StringBuffer sb = isRev1 ? new StringBuffer("BODY.PEEK[HEADER.FIELDS (") : new StringBuffer("RFC822.HEADER.LINES (");
        for (int i = 0; i < hdrs.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(hdrs[i]);
        }
        if (isRev1) {
            sb.append(")]");
        } else {
            sb.append(")");
        }
        return sb.toString();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized void setFlags(Message[] msgs, Flags flag, boolean value) throws MessagingException {
        this.checkOpened();
        this.checkFlags(flag);
        if (msgs.length == 0) {
            return;
        }
        Object object = this.messageCacheLock;
        synchronized (object) {
            try {
                IMAPProtocol p = this.getProtocol();
                MessageSet[] ms = Utility.toMessageSetSorted(msgs, null);
                if (ms == null) {
                    throw new MessageRemovedException("Messages have been removed");
                }
                p.storeFlags(ms, flag, value);
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
    }

    @Override
    public synchronized void setFlags(int start, int end, Flags flag, boolean value) throws MessagingException {
        this.checkOpened();
        Message[] msgs = new Message[end - start + 1];
        int i = 0;
        for (int n = start; n <= end; ++n) {
            msgs[i++] = this.getMessage(n);
        }
        this.setFlags(msgs, flag, value);
    }

    @Override
    public synchronized void setFlags(int[] msgnums, Flags flag, boolean value) throws MessagingException {
        this.checkOpened();
        Message[] msgs = new Message[msgnums.length];
        for (int i = 0; i < msgnums.length; ++i) {
            msgs[i] = this.getMessage(msgnums[i]);
        }
        this.setFlags(msgs, flag, value);
    }

    @Override
    public synchronized void close(boolean expunge) throws MessagingException {
        this.close(expunge, false);
    }

    public synchronized void forceClose() throws MessagingException {
        this.close(false, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void close(boolean expunge, boolean force) throws MessagingException {
        assert (Thread.holdsLock(this));
        Object object = this.messageCacheLock;
        synchronized (object) {
            if (!this.opened && this.reallyClosed) {
                throw new IllegalStateException("This operation is not allowed on a closed folder");
            }
            this.reallyClosed = true;
            if (!this.opened) {
                return;
            }
            boolean reuseProtocol = true;
            try {
                block24: {
                    try {
                        this.waitIfIdle();
                        if (force) {
                            this.logger.log(Level.FINE, "forcing folder {0} to close", this.fullName);
                            if (this.protocol != null) {
                                this.protocol.disconnect();
                            }
                            break block24;
                        }
                        if (((IMAPStore)this.store).isConnectionPoolFull()) {
                            this.logger.fine("pool is full, not adding an Authenticated connection");
                            if (expunge && this.protocol != null) {
                                this.protocol.close();
                            }
                            if (this.protocol != null) {
                                this.protocol.logout();
                            }
                            break block24;
                        }
                        if (!expunge && this.mode == 2) {
                            try {
                                if (this.protocol != null && this.protocol.hasCapability("UNSELECT")) {
                                    this.protocol.unselect();
                                    break block24;
                                }
                                if (this.protocol == null) break block24;
                                boolean selected = true;
                                try {
                                    this.protocol.examine(this.fullName);
                                }
                                catch (CommandFailedException ex) {
                                    selected = false;
                                }
                                if (selected && this.protocol != null) {
                                    this.protocol.close();
                                }
                                break block24;
                            }
                            catch (ProtocolException pex2) {
                                reuseProtocol = false;
                            }
                            break block24;
                        }
                        if (this.protocol == null) break block24;
                        this.protocol.close();
                    }
                    catch (ProtocolException pex) {
                        throw new MessagingException(pex.getMessage(), pex);
                    }
                }
                Object var8_9 = null;
                if (this.opened) {
                    this.cleanup(reuseProtocol);
                }
            }
            catch (Throwable throwable) {
                Object var8_10 = null;
                if (this.opened) {
                    this.cleanup(reuseProtocol);
                }
                throw throwable;
            }
        }
    }

    private void cleanup(boolean returnToPool) {
        assert (Thread.holdsLock(this.messageCacheLock));
        this.releaseProtocol(returnToPool);
        this.messageCache = null;
        this.uidTable = null;
        this.exists = false;
        this.attributes = null;
        this.opened = false;
        this.idleState = 0;
        this.messageCacheLock.notifyAll();
        this.notifyConnectionListeners(3);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized boolean isOpen() {
        Object object = this.messageCacheLock;
        synchronized (object) {
            if (this.opened) {
                try {
                    this.keepConnectionAlive(false);
                }
                catch (ProtocolException protocolException) {
                    // empty catch block
                }
            }
        }
        return this.opened;
    }

    @Override
    public synchronized Flags getPermanentFlags() {
        if (this.permanentFlags == null) {
            return null;
        }
        return (Flags)this.permanentFlags.clone();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized int getMessageCount() throws MessagingException {
        Object object = this.messageCacheLock;
        synchronized (object) {
            if (this.opened) {
                try {
                    this.keepConnectionAlive(true);
                    return this.total;
                }
                catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                }
                catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
        }
        this.checkExists();
        try {
            Status status = this.getStatus();
            return status.total;
        }
        catch (BadCommandException bex) {
            IMAPProtocol p = null;
            try {
                p = this.getStoreProtocol();
                MailboxInfo minfo = p.examine(this.fullName);
                p.close();
                int n = minfo.total;
                Object var6_12 = null;
                this.releaseStoreProtocol(p);
                return n;
            }
            catch (ProtocolException pex) {
                try {
                    throw new MessagingException(pex.getMessage(), pex);
                }
                catch (Throwable throwable) {
                    Object var6_13 = null;
                    this.releaseStoreProtocol(p);
                    throw throwable;
                }
            }
        }
        catch (ConnectionException cex) {
            throw new StoreClosedException(this.store, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized int getNewMessageCount() throws MessagingException {
        Object object = this.messageCacheLock;
        synchronized (object) {
            if (this.opened) {
                try {
                    this.keepConnectionAlive(true);
                    return this.recent;
                }
                catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                }
                catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
        }
        this.checkExists();
        try {
            Status status = this.getStatus();
            return status.recent;
        }
        catch (BadCommandException bex) {
            IMAPProtocol p = null;
            try {
                p = this.getStoreProtocol();
                MailboxInfo minfo = p.examine(this.fullName);
                p.close();
                int n = minfo.recent;
                Object var6_12 = null;
                this.releaseStoreProtocol(p);
                return n;
            }
            catch (ProtocolException pex) {
                try {
                    throw new MessagingException(pex.getMessage(), pex);
                }
                catch (Throwable throwable) {
                    Object var6_13 = null;
                    this.releaseStoreProtocol(p);
                    throw throwable;
                }
            }
        }
        catch (ConnectionException cex) {
            throw new StoreClosedException(this.store, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized int getUnreadMessageCount() throws MessagingException {
        if (!this.opened) {
            this.checkExists();
            try {
                Status status = this.getStatus();
                return status.unseen;
            }
            catch (BadCommandException bex) {
                return -1;
            }
            catch (ConnectionException cex) {
                throw new StoreClosedException(this.store, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        Flags f = new Flags();
        f.add(Flags.Flag.SEEN);
        try {
            Object object = this.messageCacheLock;
            synchronized (object) {
                int[] matches = this.getProtocol().search(new FlagTerm(f, false));
                return matches.length;
            }
        }
        catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized int getDeletedMessageCount() throws MessagingException {
        if (!this.opened) {
            this.checkExists();
            return -1;
        }
        Flags f = new Flags();
        f.add(Flags.Flag.DELETED);
        try {
            Object object = this.messageCacheLock;
            synchronized (object) {
                int[] matches = this.getProtocol().search(new FlagTerm(f, true));
                return matches.length;
            }
        }
        catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Status getStatus() throws ProtocolException {
        int statusCacheTimeout = ((IMAPStore)this.store).getStatusCacheTimeout();
        if (statusCacheTimeout > 0 && this.cachedStatus != null && System.currentTimeMillis() - this.cachedStatusTime < (long)statusCacheTimeout) {
            return this.cachedStatus;
        }
        IMAPProtocol p = null;
        try {
            p = this.getStoreProtocol();
            Status s = p.status(this.fullName, null);
            if (statusCacheTimeout > 0) {
                this.cachedStatus = s;
                this.cachedStatusTime = System.currentTimeMillis();
            }
            Status status = s;
            Object var6_5 = null;
            this.releaseStoreProtocol(p);
            return status;
        }
        catch (Throwable throwable) {
            Object var6_6 = null;
            this.releaseStoreProtocol(p);
            throw throwable;
        }
    }

    @Override
    public synchronized Message getMessage(int msgnum) throws MessagingException {
        this.checkOpened();
        this.checkRange(msgnum);
        return this.messageCache.getMessage(msgnum);
    }

    @Override
    public synchronized Message[] getMessages() throws MessagingException {
        this.checkOpened();
        int total = this.getMessageCount();
        Message[] msgs = new Message[total];
        for (int i = 1; i <= total; ++i) {
            msgs[i - 1] = this.messageCache.getMessage(i);
        }
        return msgs;
    }

    @Override
    public synchronized void appendMessages(Message[] msgs) throws MessagingException {
        this.checkExists();
        int maxsize = ((IMAPStore)this.store).getAppendBufferSize();
        for (int i = 0; i < msgs.length; ++i) {
            MessageLiteral mos;
            Message m = msgs[i];
            Date d = m.getReceivedDate();
            if (d == null) {
                d = m.getSentDate();
            }
            final Date dd = d;
            final Flags f = m.getFlags();
            try {
                mos = new MessageLiteral(m, m.getSize() > maxsize ? 0 : maxsize);
            }
            catch (IOException ex) {
                throw new MessagingException("IOException while appending messages", ex);
            }
            catch (MessageRemovedException mrex) {
                continue;
            }
            this.doCommand(new ProtocolCommand(){

                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    p.append(IMAPFolder.this.fullName, f, dd, mos);
                    return null;
                }
            });
        }
    }

    public synchronized AppendUID[] appendUIDMessages(Message[] msgs) throws MessagingException {
        this.checkExists();
        int maxsize = ((IMAPStore)this.store).getAppendBufferSize();
        AppendUID[] uids = new AppendUID[msgs.length];
        for (int i = 0; i < msgs.length; ++i) {
            AppendUID auid;
            MessageLiteral mos;
            Message m = msgs[i];
            try {
                mos = new MessageLiteral(m, m.getSize() > maxsize ? 0 : maxsize);
            }
            catch (IOException ex) {
                throw new MessagingException("IOException while appending messages", ex);
            }
            catch (MessageRemovedException mrex) {
                continue;
            }
            Date d = m.getReceivedDate();
            if (d == null) {
                d = m.getSentDate();
            }
            final Date dd = d;
            final Flags f = m.getFlags();
            uids[i] = auid = (AppendUID)this.doCommand(new ProtocolCommand(){

                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    return p.appenduid(IMAPFolder.this.fullName, f, dd, mos);
                }
            });
        }
        return uids;
    }

    public synchronized Message[] addMessages(Message[] msgs) throws MessagingException {
        this.checkOpened();
        Message[] rmsgs = new MimeMessage[msgs.length];
        AppendUID[] uids = this.appendUIDMessages(msgs);
        for (int i = 0; i < uids.length; ++i) {
            AppendUID auid = uids[i];
            if (auid == null || auid.uidvalidity != this.uidvalidity) continue;
            try {
                rmsgs[i] = this.getMessageByUID(auid.uid);
                continue;
            }
            catch (MessagingException messagingException) {
                // empty catch block
            }
        }
        return rmsgs;
    }

    @Override
    public synchronized void copyMessages(Message[] msgs, Folder folder) throws MessagingException {
        this.copymoveMessages(msgs, folder, false);
    }

    public synchronized AppendUID[] copyUIDMessages(Message[] msgs, Folder folder) throws MessagingException {
        return this.copymoveUIDMessages(msgs, folder, false);
    }

    public synchronized void moveMessages(Message[] msgs, Folder folder) throws MessagingException {
        this.copymoveMessages(msgs, folder, true);
    }

    public synchronized AppendUID[] moveUIDMessages(Message[] msgs, Folder folder) throws MessagingException {
        return this.copymoveUIDMessages(msgs, folder, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private synchronized void copymoveMessages(Message[] msgs, Folder folder, boolean move) throws MessagingException {
        this.checkOpened();
        if (msgs.length == 0) {
            return;
        }
        if (folder.getStore() == this.store) {
            Object object = this.messageCacheLock;
            synchronized (object) {
                try {
                    IMAPProtocol p = this.getProtocol();
                    MessageSet[] ms = Utility.toMessageSet(msgs, null);
                    if (ms == null) {
                        throw new MessageRemovedException("Messages have been removed");
                    }
                    if (move) {
                        p.move(ms, folder.getFullName());
                    } else {
                        p.copy(ms, folder.getFullName());
                    }
                }
                catch (CommandFailedException cfx) {
                    if (cfx.getMessage().indexOf("TRYCREATE") != -1) {
                        throw new FolderNotFoundException(folder, folder.getFullName() + " does not exist");
                    }
                    throw new MessagingException(cfx.getMessage(), cfx);
                }
                catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                }
                catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
        }
        if (move) {
            throw new MessagingException("Move between stores not supported");
        }
        super.copyMessages(msgs, folder);
    }

    private synchronized AppendUID[] copymoveUIDMessages(Message[] msgs, Folder folder, boolean move) throws MessagingException {
        this.checkOpened();
        if (msgs.length == 0) {
            return null;
        }
        if (folder.getStore() != this.store) {
            throw new MessagingException(move ? "can't moveUIDMessages to a different store" : "can't copyUIDMessages to a different store");
        }
        FetchProfile fp = new FetchProfile();
        fp.add(UIDFolder.FetchProfileItem.UID);
        this.fetch(msgs, fp);
        Object object = this.messageCacheLock;
        synchronized (object) {
            try {
                IMAPProtocol p = this.getProtocol();
                MessageSet[] ms = Utility.toMessageSet(msgs, null);
                if (ms == null) {
                    throw new MessageRemovedException("Messages have been removed");
                }
                CopyUID cuid = move ? p.moveuid(ms, folder.getFullName()) : p.copyuid(ms, folder.getFullName());
                long[] srcuids = UIDSet.toArray(cuid.src);
                long[] dstuids = UIDSet.toArray(cuid.dst);
                Message[] srcmsgs = this.getMessagesByUID(srcuids);
                AppendUID[] result = new AppendUID[msgs.length];
                block7: for (int i = 0; i < msgs.length; ++i) {
                    int j = i;
                    do {
                        if (msgs[i] == srcmsgs[j]) {
                            result[i] = new AppendUID(cuid.uidvalidity, dstuids[j]);
                            continue block7;
                        }
                        if (++j < srcmsgs.length) continue;
                        j = 0;
                    } while (j != i);
                }
                return result;
            }
            catch (CommandFailedException cfx) {
                if (cfx.getMessage().indexOf("TRYCREATE") != -1) {
                    throw new FolderNotFoundException(folder, folder.getFullName() + " does not exist");
                }
                throw new MessagingException(cfx.getMessage(), cfx);
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
    }

    @Override
    public synchronized Message[] expunge() throws MessagingException {
        return this.expunge(null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized Message[] expunge(Message[] msgs) throws MessagingException {
        Message[] rmsgs;
        this.checkOpened();
        if (msgs != null) {
            FetchProfile fp = new FetchProfile();
            fp.add(UIDFolder.FetchProfileItem.UID);
            this.fetch(msgs, fp);
        }
        Object object = this.messageCacheLock;
        synchronized (object) {
            this.doExpungeNotification = false;
            try {
                block15: {
                    try {
                        IMAPProtocol p = this.getProtocol();
                        if (msgs != null) {
                            p.uidexpunge(Utility.toUIDSet(msgs));
                            break block15;
                        }
                        p.expunge();
                    }
                    catch (CommandFailedException cfx) {
                        if (this.mode != 2) {
                            throw new IllegalStateException("Cannot expunge READ_ONLY folder: " + this.fullName);
                        }
                        throw new MessagingException(cfx.getMessage(), cfx);
                    }
                    catch (ConnectionException cex) {
                        throw new FolderClosedException(this, cex.getMessage());
                    }
                    catch (ProtocolException pex) {
                        throw new MessagingException(pex.getMessage(), pex);
                    }
                }
                Object var6_9 = null;
                this.doExpungeNotification = true;
            }
            catch (Throwable throwable) {
                Object var6_10 = null;
                this.doExpungeNotification = true;
                throw throwable;
            }
            rmsgs = msgs != null ? this.messageCache.removeExpungedMessages(msgs) : this.messageCache.removeExpungedMessages();
            if (this.uidTable != null) {
                for (int i = 0; i < rmsgs.length; ++i) {
                    IMAPMessage m = rmsgs[i];
                    long uid = m.getUID();
                    if (uid == -1L) continue;
                    this.uidTable.remove(uid);
                }
            }
            this.total = this.messageCache.size();
        }
        if (rmsgs.length > 0) {
            this.notifyMessageRemovedListeners(true, rmsgs);
        }
        return rmsgs;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized Message[] search(SearchTerm term) throws MessagingException {
        this.checkOpened();
        try {
            Message[] matchMsgs = null;
            Object object = this.messageCacheLock;
            synchronized (object) {
                int[] matches = this.getProtocol().search(term);
                if (matches != null) {
                    matchMsgs = this.getMessagesBySeqNumbers(matches);
                }
            }
            return matchMsgs;
        }
        catch (CommandFailedException cfx) {
            return super.search(term);
        }
        catch (SearchException sex) {
            if (((IMAPStore)this.store).throwSearchException()) {
                throw sex;
            }
            return super.search(term);
        }
        catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized Message[] search(SearchTerm term, Message[] msgs) throws MessagingException {
        this.checkOpened();
        if (msgs.length == 0) {
            return msgs;
        }
        try {
            Message[] matchMsgs = null;
            Object object = this.messageCacheLock;
            synchronized (object) {
                IMAPProtocol p = this.getProtocol();
                MessageSet[] ms = Utility.toMessageSetSorted(msgs, null);
                if (ms == null) {
                    throw new MessageRemovedException("Messages have been removed");
                }
                int[] matches = p.search(ms, term);
                if (matches != null) {
                    matchMsgs = this.getMessagesBySeqNumbers(matches);
                }
            }
            return matchMsgs;
        }
        catch (CommandFailedException cfx) {
            return super.search(term, msgs);
        }
        catch (SearchException sex) {
            return super.search(term, msgs);
        }
        catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    public synchronized Message[] getSortedMessages(SortTerm[] term) throws MessagingException {
        return this.getSortedMessages(term, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized Message[] getSortedMessages(SortTerm[] term, SearchTerm sterm) throws MessagingException {
        this.checkOpened();
        try {
            Message[] matchMsgs = null;
            Object object = this.messageCacheLock;
            synchronized (object) {
                int[] matches = this.getProtocol().sort(term, sterm);
                if (matches != null) {
                    matchMsgs = this.getMessagesBySeqNumbers(matches);
                }
            }
            return matchMsgs;
        }
        catch (CommandFailedException cfx) {
            throw new MessagingException(cfx.getMessage(), cfx);
        }
        catch (SearchException sex) {
            throw new MessagingException(sex.getMessage(), sex);
        }
        catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    @Override
    public synchronized void addMessageCountListener(MessageCountListener l) {
        super.addMessageCountListener(l);
        this.hasMessageCountListener = true;
    }

    @Override
    public synchronized long getUIDValidity() throws MessagingException {
        Status status;
        block7: {
            if (this.opened) {
                return this.uidvalidity;
            }
            IMAPProtocol p = null;
            status = null;
            try {
                try {
                    p = this.getStoreProtocol();
                    String[] item = new String[]{"UIDVALIDITY"};
                    status = p.status(this.fullName, item);
                }
                catch (BadCommandException bex) {
                    throw new MessagingException("Cannot obtain UIDValidity", bex);
                }
                catch (ConnectionException cex) {
                    this.throwClosedException(cex);
                    Object var5_8 = null;
                    this.releaseStoreProtocol(p);
                    break block7;
                }
                catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
                Object var5_7 = null;
                this.releaseStoreProtocol(p);
            }
            catch (Throwable throwable) {
                Object var5_9 = null;
                this.releaseStoreProtocol(p);
                throw throwable;
            }
        }
        return status.uidvalidity;
    }

    public synchronized long getUIDNext() throws MessagingException {
        Status status;
        block7: {
            if (this.opened) {
                return this.uidnext;
            }
            IMAPProtocol p = null;
            status = null;
            try {
                try {
                    p = this.getStoreProtocol();
                    String[] item = new String[]{"UIDNEXT"};
                    status = p.status(this.fullName, item);
                }
                catch (BadCommandException bex) {
                    throw new MessagingException("Cannot obtain UIDNext", bex);
                }
                catch (ConnectionException cex) {
                    this.throwClosedException(cex);
                    Object var5_8 = null;
                    this.releaseStoreProtocol(p);
                    break block7;
                }
                catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
                Object var5_7 = null;
                this.releaseStoreProtocol(p);
            }
            catch (Throwable throwable) {
                Object var5_9 = null;
                this.releaseStoreProtocol(p);
                throw throwable;
            }
        }
        return status.uidnext;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized Message getMessageByUID(long uid) throws MessagingException {
        this.checkOpened();
        IMAPMessage m = null;
        try {
            Object object = this.messageCacheLock;
            synchronized (object) {
                Long l = uid;
                if (this.uidTable != null) {
                    m = this.uidTable.get(l);
                    if (m != null) {
                        return m;
                    }
                } else {
                    this.uidTable = new Hashtable();
                }
                this.getProtocol().fetchSequenceNumber(uid);
                if (this.uidTable != null && (m = this.uidTable.get(l)) != null) {
                    return m;
                }
            }
        }
        catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return m;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized Message[] getMessagesByUID(long start, long end) throws MessagingException {
        Message[] msgs;
        this.checkOpened();
        try {
            Object object = this.messageCacheLock;
            synchronized (object) {
                if (this.uidTable == null) {
                    this.uidTable = new Hashtable();
                }
                long[] ua = this.getProtocol().fetchSequenceNumbers(start, end);
                ArrayList<Message> ma = new ArrayList<Message>();
                for (int i = 0; i < ua.length; ++i) {
                    Message m = this.uidTable.get(ua[i]);
                    if (m == null) continue;
                    ma.add(m);
                }
                msgs = ma.toArray(new Message[ma.size()]);
            }
        }
        catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return msgs;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized Message[] getMessagesByUID(long[] uids) throws MessagingException {
        this.checkOpened();
        try {
            Object object = this.messageCacheLock;
            synchronized (object) {
                long[] unavailUids = uids;
                if (this.uidTable != null) {
                    ArrayList<Long> v = new ArrayList<Long>();
                    for (long uid : uids) {
                        if (this.uidTable.containsKey(uid)) continue;
                        v.add(uid);
                    }
                    int vsize = v.size();
                    unavailUids = new long[vsize];
                    for (int i = 0; i < vsize; ++i) {
                        unavailUids[i] = (Long)v.get(i);
                    }
                } else {
                    this.uidTable = new Hashtable();
                }
                if (unavailUids.length > 0) {
                    this.getProtocol().fetchSequenceNumbers(unavailUids);
                }
                Message[] msgs = new Message[uids.length];
                for (int i = 0; i < uids.length; ++i) {
                    msgs[i] = this.uidTable.get(uids[i]);
                }
                return msgs;
            }
        }
        catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized long getUID(Message message) throws MessagingException {
        if (message.getFolder() != this) {
            throw new NoSuchElementException("Message does not belong to this folder");
        }
        this.checkOpened();
        if (!(message instanceof IMAPMessage)) {
            throw new MessagingException("message is not an IMAPMessage");
        }
        IMAPMessage m = (IMAPMessage)message;
        long uid = m.getUID();
        if (uid != -1L) {
            return uid;
        }
        Object object = this.messageCacheLock;
        synchronized (object) {
            try {
                IMAPProtocol p = this.getProtocol();
                m.checkExpunged();
                UID u = p.fetchUID(m.getSequenceNumber());
                if (u != null) {
                    uid = u.uid;
                    m.setUID(uid);
                    if (this.uidTable == null) {
                        this.uidTable = new Hashtable();
                    }
                    this.uidTable.put(uid, m);
                }
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        return uid;
    }

    private Message[] createMessagesForUIDs(long[] uids) {
        Message[] msgs = new IMAPMessage[uids.length];
        for (int i = 0; i < uids.length; ++i) {
            IMAPMessage m = null;
            if (this.uidTable != null) {
                m = this.uidTable.get(uids[i]);
            }
            if (m == null) {
                m = this.newIMAPMessage(-1);
                m.setUID(uids[i]);
                m.setExpunged(true);
            }
            msgs[i++] = m;
        }
        return msgs;
    }

    public synchronized long getHighestModSeq() throws MessagingException {
        Status status;
        block8: {
            if (this.opened) {
                return this.highestmodseq;
            }
            IMAPProtocol p = null;
            status = null;
            try {
                try {
                    p = this.getStoreProtocol();
                    if (!p.hasCapability("CONDSTORE")) {
                        throw new BadCommandException("CONDSTORE not supported");
                    }
                    String[] item = new String[]{"HIGHESTMODSEQ"};
                    status = p.status(this.fullName, item);
                }
                catch (BadCommandException bex) {
                    throw new MessagingException("Cannot obtain HIGHESTMODSEQ", bex);
                }
                catch (ConnectionException cex) {
                    this.throwClosedException(cex);
                    Object var5_8 = null;
                    this.releaseStoreProtocol(p);
                    break block8;
                }
                catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
                Object var5_7 = null;
                this.releaseStoreProtocol(p);
            }
            catch (Throwable throwable) {
                Object var5_9 = null;
                this.releaseStoreProtocol(p);
                throw throwable;
            }
        }
        return status.highestmodseq;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized Message[] getMessagesByUIDChangedSince(long start, long end, long modseq) throws MessagingException {
        this.checkOpened();
        try {
            Object object = this.messageCacheLock;
            synchronized (object) {
                IMAPProtocol p = this.getProtocol();
                if (!p.hasCapability("CONDSTORE")) {
                    throw new BadCommandException("CONDSTORE not supported");
                }
                int[] nums = p.uidfetchChangedSince(start, end, modseq);
                return this.getMessagesBySeqNumbers(nums);
            }
        }
        catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    public Quota[] getQuota() throws MessagingException {
        return (Quota[])this.doOptionalCommand("QUOTA not supported", new ProtocolCommand(){

            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.getQuotaRoot(IMAPFolder.this.fullName);
            }
        });
    }

    public void setQuota(final Quota quota) throws MessagingException {
        this.doOptionalCommand("QUOTA not supported", new ProtocolCommand(){

            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                p.setQuota(quota);
                return null;
            }
        });
    }

    public ACL[] getACL() throws MessagingException {
        return (ACL[])this.doOptionalCommand("ACL not supported", new ProtocolCommand(){

            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.getACL(IMAPFolder.this.fullName);
            }
        });
    }

    public void addACL(ACL acl) throws MessagingException {
        this.setACL(acl, '\u0000');
    }

    public void removeACL(final String name) throws MessagingException {
        this.doOptionalCommand("ACL not supported", new ProtocolCommand(){

            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                p.deleteACL(IMAPFolder.this.fullName, name);
                return null;
            }
        });
    }

    public void addRights(ACL acl) throws MessagingException {
        this.setACL(acl, '+');
    }

    public void removeRights(ACL acl) throws MessagingException {
        this.setACL(acl, '-');
    }

    public Rights[] listRights(final String name) throws MessagingException {
        return (Rights[])this.doOptionalCommand("ACL not supported", new ProtocolCommand(){

            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.listRights(IMAPFolder.this.fullName, name);
            }
        });
    }

    public Rights myRights() throws MessagingException {
        return (Rights)this.doOptionalCommand("ACL not supported", new ProtocolCommand(){

            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.myRights(IMAPFolder.this.fullName);
            }
        });
    }

    private void setACL(final ACL acl, final char mod) throws MessagingException {
        this.doOptionalCommand("ACL not supported", new ProtocolCommand(){

            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                p.setACL(IMAPFolder.this.fullName, mod, acl);
                return null;
            }
        });
    }

    public synchronized String[] getAttributes() throws MessagingException {
        this.checkExists();
        if (this.attributes == null) {
            this.exists();
        }
        return this.attributes == null ? new String[]{} : (String[])this.attributes.clone();
    }

    public void idle() throws MessagingException {
        this.idle(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void idle(boolean once) throws MessagingException {
        IMAPFolder iMAPFolder = this;
        synchronized (iMAPFolder) {
            if (this.protocol != null && this.protocol.getChannel() != null) {
                throw new MessagingException("idle method not supported with SocketChannels");
            }
        }
        if (!this.startIdle(null)) {
            return;
        }
        while (this.handleIdle(once)) {
        }
        int minidle = ((IMAPStore)this.store).getMinIdleTime();
        if (minidle > 0) {
            try {
                Thread.sleep(minidle);
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean startIdle(final IdleManager im) throws MessagingException {
        assert (!Thread.holdsLock(this));
        IMAPFolder iMAPFolder = this;
        synchronized (iMAPFolder) {
            this.checkOpened();
            if (im != null && this.idleManager != null && im != this.idleManager) {
                throw new MessagingException("Folder already being watched by another IdleManager");
            }
            Boolean started = (Boolean)this.doOptionalCommand("IDLE not supported", new ProtocolCommand(){

                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    if (IMAPFolder.this.idleState == 1 && im != null && im == IMAPFolder.this.idleManager) {
                        return Boolean.TRUE;
                    }
                    if (IMAPFolder.this.idleState == 0) {
                        p.idleStart();
                        IMAPFolder.this.logger.finest("startIdle: set to IDLE");
                        IMAPFolder.this.idleState = 1;
                        IMAPFolder.this.idleManager = im;
                        return Boolean.TRUE;
                    }
                    try {
                        IMAPFolder.this.messageCacheLock.wait();
                    }
                    catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    return Boolean.FALSE;
                }
            });
            this.logger.log(Level.FINEST, "startIdle: return {0}", started);
            return started;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    boolean handleIdle(boolean once) throws MessagingException {
        Response r = null;
        do {
            r = this.protocol.readIdleResponse();
            try {
                Object object = this.messageCacheLock;
                synchronized (object) {
                    Object var7_11;
                    Exception ex;
                    if (r.isBYE() && r.isSynthetic() && this.idleState == 1 && (ex = r.getException()) instanceof InterruptedIOException && ((InterruptedIOException)ex).bytesTransferred == 0) {
                        if (ex instanceof SocketTimeoutException) {
                            this.logger.finest("handleIdle: ignoring socket timeout");
                            r = null;
                        } else {
                            this.logger.finest("handleIdle: interrupting IDLE");
                            IdleManager im = this.idleManager;
                            if (im != null) {
                                this.logger.finest("handleIdle: request IdleManager to abort");
                                im.requestAbort(this);
                            } else {
                                this.logger.finest("handleIdle: abort IDLE");
                                this.protocol.idleAbort();
                                this.idleState = 2;
                            }
                        }
                        continue;
                    }
                    boolean done = true;
                    try {
                        if (this.protocol == null || !this.protocol.processIdleResponse(r)) {
                            boolean bl = false;
                            var7_11 = null;
                            if (done) {
                                this.logger.finest("handleIdle: set to RUNNING");
                                this.idleState = 0;
                                this.idleManager = null;
                                this.messageCacheLock.notifyAll();
                            }
                            return bl;
                        }
                        done = false;
                    }
                    catch (Throwable throwable) {
                        var7_11 = null;
                        if (done) {
                            this.logger.finest("handleIdle: set to RUNNING");
                            this.idleState = 0;
                            this.idleManager = null;
                            this.messageCacheLock.notifyAll();
                        }
                        throw throwable;
                    }
                    var7_11 = null;
                    if (done) {
                        this.logger.finest("handleIdle: set to RUNNING");
                        this.idleState = 0;
                        this.idleManager = null;
                        this.messageCacheLock.notifyAll();
                    }
                    if (once && this.idleState == 1) {
                        try {
                            this.protocol.idleAbort();
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        this.idleState = 2;
                    }
                }
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        } while (r == null || this.protocol.hasResponse());
        return true;
    }

    void waitIfIdle() throws ProtocolException {
        assert (Thread.holdsLock(this.messageCacheLock));
        while (this.idleState != 0) {
            if (this.idleState == 1) {
                IdleManager im = this.idleManager;
                if (im != null) {
                    this.logger.finest("waitIfIdle: request IdleManager to abort");
                    im.requestAbort(this);
                } else {
                    this.logger.finest("waitIfIdle: abort IDLE");
                    this.protocol.idleAbort();
                    this.idleState = 2;
                }
            } else {
                this.logger.log(Level.FINEST, "waitIfIdle: idleState {0}", this.idleState);
            }
            try {
                if (this.logger.isLoggable(Level.FINEST)) {
                    this.logger.finest("waitIfIdle: wait to be not idle: " + Thread.currentThread());
                }
                this.messageCacheLock.wait();
                if (!this.logger.isLoggable(Level.FINEST)) continue;
                this.logger.finest("waitIfIdle: wait done, idleState " + this.idleState + ": " + Thread.currentThread());
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new ProtocolException("Interrupted waitIfIdle", ex);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void idleAbort() {
        Object object = this.messageCacheLock;
        synchronized (object) {
            if (this.idleState == 1 && this.protocol != null) {
                this.protocol.idleAbort();
                this.idleState = 2;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void idleAbortWait() {
        Object object = this.messageCacheLock;
        synchronized (object) {
            if (this.idleState == 1 && this.protocol != null) {
                this.protocol.idleAbort();
                this.idleState = 2;
                try {
                    while (this.handleIdle(false)) {
                    }
                }
                catch (Exception ex) {
                    this.logger.log(Level.FINEST, "Exception in idleAbortWait", ex);
                }
                this.logger.finest("IDLE aborted");
            }
        }
    }

    SocketChannel getChannel() {
        return this.protocol != null ? this.protocol.getChannel() : null;
    }

    public Map<String, String> id(final Map<String, String> clientParams) throws MessagingException {
        this.checkOpened();
        return (Map)this.doOptionalCommand("ID not supported", new ProtocolCommand(){

            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.id(clientParams);
            }
        });
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public synchronized long getStatusItem(String item) throws MessagingException {
        if (this.opened) {
            return -1L;
        }
        this.checkExists();
        IMAPProtocol p = null;
        Status status = null;
        try {
            try {
                p = this.getStoreProtocol();
                String[] items = new String[]{item};
                status = p.status(this.fullName, items);
                long l = status.getItem(item);
                Object var8_10 = null;
                this.releaseStoreProtocol(p);
                return l;
            }
            catch (BadCommandException bex) {
                long l = -1L;
                Object var8_11 = null;
                this.releaseStoreProtocol(p);
                return l;
            }
            catch (ConnectionException cex) {
                throw new StoreClosedException(this.store, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        catch (Throwable throwable) {
            Object var8_12 = null;
            this.releaseStoreProtocol(p);
            throw throwable;
        }
    }

    @Override
    public void handleResponse(Response r) {
        assert (Thread.holdsLock(this.messageCacheLock));
        if (r.isOK() || r.isNO() || r.isBAD() || r.isBYE()) {
            ((IMAPStore)this.store).handleResponseCode(r);
        }
        if (r.isBYE()) {
            if (this.opened) {
                this.cleanup(false);
            }
            return;
        }
        if (r.isOK()) {
            String s;
            r.skipSpaces();
            if (r.readByte() == 91 && (s = r.readAtom()).equalsIgnoreCase("HIGHESTMODSEQ")) {
                this.highestmodseq = r.readLong();
            }
            r.reset();
            return;
        }
        if (!r.isUnTagged()) {
            return;
        }
        if (!(r instanceof IMAPResponse)) {
            this.logger.fine("UNEXPECTED RESPONSE : " + r.toString());
            return;
        }
        IMAPResponse ir = (IMAPResponse)r;
        if (ir.keyEquals("EXISTS")) {
            int exists = ir.getNumber();
            if (exists <= this.realTotal) {
                return;
            }
            int count = exists - this.realTotal;
            Message[] msgs = new Message[count];
            this.messageCache.addMessages(count, this.realTotal + 1);
            int oldtotal = this.total;
            this.realTotal += count;
            this.total += count;
            if (this.hasMessageCountListener) {
                for (int i = 0; i < count; ++i) {
                    msgs[i] = this.messageCache.getMessage(++oldtotal);
                }
                this.notifyMessageAddedListeners(msgs);
            }
        } else if (ir.keyEquals("EXPUNGE")) {
            int seqnum = ir.getNumber();
            Message[] msgs = null;
            if (this.doExpungeNotification && this.hasMessageCountListener && (msgs = new Message[]{this.getMessageBySeqNumber(seqnum)})[0] == null) {
                msgs = null;
            }
            this.messageCache.expungeMessage(seqnum);
            --this.realTotal;
            if (msgs != null) {
                this.notifyMessageRemovedListeners(false, msgs);
            }
        } else if (ir.keyEquals("VANISHED")) {
            String[] s = ir.readAtomStringList();
            if (s == null) {
                Message[] msgs;
                String uids = ir.readAtom();
                UIDSet[] uidset = UIDSet.parseUIDSets(uids);
                this.realTotal = (int)((long)this.realTotal - UIDSet.size(uidset));
                long[] luid = UIDSet.toArray(uidset);
                for (Message m : msgs = this.createMessagesForUIDs(luid)) {
                    if (m.getMessageNumber() <= 0) continue;
                    this.messageCache.expungeMessage(m.getMessageNumber());
                }
                if (this.doExpungeNotification && this.hasMessageCountListener) {
                    this.notifyMessageRemovedListeners(true, msgs);
                }
            }
        } else if (ir.keyEquals("FETCH")) {
            assert (ir instanceof FetchResponse) : "!ir instanceof FetchResponse";
            Message msg = this.processFetchResponse((FetchResponse)ir);
            if (msg != null) {
                this.notifyMessageChangedListeners(1, msg);
            }
        } else if (ir.keyEquals("RECENT")) {
            this.recent = ir.getNumber();
        }
    }

    private Message processFetchResponse(FetchResponse fr) {
        IMAPMessage msg = this.getMessageBySeqNumber(fr.getNumber());
        if (msg != null) {
            FLAGS flags;
            MODSEQ modseq;
            boolean notify = false;
            UID uid = fr.getItem(UID.class);
            if (uid != null && msg.getUID() != uid.uid) {
                msg.setUID(uid.uid);
                if (this.uidTable == null) {
                    this.uidTable = new Hashtable();
                }
                this.uidTable.put(uid.uid, msg);
                notify = true;
            }
            if ((modseq = fr.getItem(MODSEQ.class)) != null && msg._getModSeq() != modseq.modseq) {
                msg.setModSeq(modseq.modseq);
                notify = true;
            }
            if ((flags = fr.getItem(FLAGS.class)) != null) {
                msg._setFlags(flags);
                notify = true;
            }
            msg.handleExtensionFetchItems(fr.getExtensionItems());
            if (!notify) {
                msg = null;
            }
        }
        return msg;
    }

    void handleResponses(Response[] r) {
        for (int i = 0; i < r.length; ++i) {
            if (r[i] == null) continue;
            this.handleResponse(r[i]);
        }
    }

    protected synchronized IMAPProtocol getStoreProtocol() throws ProtocolException {
        this.connectionPoolLogger.fine("getStoreProtocol() borrowing a connection");
        return ((IMAPStore)this.store).getFolderStoreProtocol();
    }

    protected synchronized void throwClosedException(ConnectionException cex) throws FolderClosedException, StoreClosedException {
        if (this.protocol != null && cex.getProtocol() == this.protocol || this.protocol == null && !this.reallyClosed) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        throw new StoreClosedException(this.store, cex.getMessage());
    }

    protected IMAPProtocol getProtocol() throws ProtocolException {
        assert (Thread.holdsLock(this.messageCacheLock));
        this.waitIfIdle();
        if (this.protocol == null) {
            throw new ConnectionException("Connection closed");
        }
        return this.protocol;
    }

    public Object doCommand(ProtocolCommand cmd) throws MessagingException {
        try {
            return this.doProtocolCommand(cmd);
        }
        catch (ConnectionException cex) {
            this.throwClosedException(cex);
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return null;
    }

    public Object doOptionalCommand(String err, ProtocolCommand cmd) throws MessagingException {
        try {
            return this.doProtocolCommand(cmd);
        }
        catch (BadCommandException bex) {
            throw new MessagingException(err, bex);
        }
        catch (ConnectionException cex) {
            this.throwClosedException(cex);
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return null;
    }

    public Object doCommandIgnoreFailure(ProtocolCommand cmd) throws MessagingException {
        try {
            return this.doProtocolCommand(cmd);
        }
        catch (CommandFailedException cfx) {
            return null;
        }
        catch (ConnectionException cex) {
            this.throwClosedException(cex);
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected synchronized Object doProtocolCommand(ProtocolCommand cmd) throws ProtocolException {
        if (this.protocol != null) {
            Object object = this.messageCacheLock;
            synchronized (object) {
                return cmd.doCommand(this.getProtocol());
            }
        }
        IMAPProtocol p = null;
        try {
            p = this.getStoreProtocol();
            Object object = cmd.doCommand(p);
            Object var5_6 = null;
            this.releaseStoreProtocol(p);
            return object;
        }
        catch (Throwable throwable) {
            Object var5_7 = null;
            this.releaseStoreProtocol(p);
            throw throwable;
        }
    }

    protected synchronized void releaseStoreProtocol(IMAPProtocol p) {
        if (p != this.protocol) {
            ((IMAPStore)this.store).releaseFolderStoreProtocol(p);
        } else {
            this.logger.fine("releasing our protocol as store protocol?");
        }
    }

    protected void releaseProtocol(boolean returnToPool) {
        if (this.protocol != null) {
            this.protocol.removeResponseHandler(this);
            if (returnToPool) {
                ((IMAPStore)this.store).releaseProtocol(this, this.protocol);
            } else {
                this.protocol.disconnect();
                ((IMAPStore)this.store).releaseProtocol(this, null);
            }
            this.protocol = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void keepConnectionAlive(boolean keepStoreAlive) throws ProtocolException {
        assert (Thread.holdsLock(this.messageCacheLock));
        if (this.protocol == null) {
            return;
        }
        if (System.currentTimeMillis() - this.protocol.getTimestamp() > 1000L) {
            this.waitIfIdle();
            if (this.protocol != null) {
                this.protocol.noop();
            }
        }
        if (keepStoreAlive && ((IMAPStore)this.store).hasSeparateStoreConnection()) {
            IMAPProtocol p = null;
            try {
                p = ((IMAPStore)this.store).getFolderStoreProtocol();
                if (System.currentTimeMillis() - p.getTimestamp() > 1000L) {
                    p.noop();
                }
                Object var4_3 = null;
                ((IMAPStore)this.store).releaseFolderStoreProtocol(p);
            }
            catch (Throwable throwable) {
                Object var4_4 = null;
                ((IMAPStore)this.store).releaseFolderStoreProtocol(p);
                throw throwable;
            }
        }
    }

    protected IMAPMessage getMessageBySeqNumber(int seqnum) {
        if (seqnum > this.messageCache.size()) {
            if (this.logger.isLoggable(Level.FINE)) {
                this.logger.fine("ignoring message number " + seqnum + " outside range " + this.messageCache.size());
            }
            return null;
        }
        return this.messageCache.getMessageBySeqnum(seqnum);
    }

    protected IMAPMessage[] getMessagesBySeqNumbers(int[] seqnums) {
        IMAPMessage[] msgs = new IMAPMessage[seqnums.length];
        int nulls = 0;
        for (int i = 0; i < seqnums.length; ++i) {
            msgs[i] = this.getMessageBySeqNumber(seqnums[i]);
            if (msgs[i] != null) continue;
            ++nulls;
        }
        if (nulls > 0) {
            IMAPMessage[] nmsgs = new IMAPMessage[seqnums.length - nulls];
            int j = 0;
            for (int i = 0; i < msgs.length; ++i) {
                if (msgs[i] == null) continue;
                nmsgs[j++] = msgs[i];
            }
            msgs = nmsgs;
        }
        return msgs;
    }

    private boolean isDirectory() {
        return (this.type & 2) != 0;
    }

    public static interface ProtocolCommand {
        public Object doCommand(IMAPProtocol var1) throws ProtocolException;
    }

    public static class FetchProfileItem
    extends FetchProfile.Item {
        public static final FetchProfileItem HEADERS = new FetchProfileItem("HEADERS");
        @Deprecated
        public static final FetchProfileItem SIZE = new FetchProfileItem("SIZE");
        public static final FetchProfileItem MESSAGE = new FetchProfileItem("MESSAGE");
        public static final FetchProfileItem INTERNALDATE = new FetchProfileItem("INTERNALDATE");

        protected FetchProfileItem(String name) {
            super(name);
        }
    }
}

