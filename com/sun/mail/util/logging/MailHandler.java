/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.DataHandler
 *  javax.activation.DataSource
 *  javax.activation.FileTypeMap
 */
package com.sun.mail.util.logging;

import com.sun.mail.util.logging.LogManagerProperties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessageContext;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Service;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class MailHandler
extends Handler {
    private static final Filter[] EMPTY_FILTERS = new Filter[0];
    private static final Formatter[] EMPTY_FORMATTERS = new Formatter[0];
    private static final int MIN_HEADER_SIZE = 1024;
    private static final int offValue = Level.OFF.intValue();
    private static final PrivilegedAction<Object> MAILHANDLER_LOADER = new GetAndSetContext(MailHandler.class);
    private static final ThreadLocal<Integer> MUTEX = new ThreadLocal();
    private static final Integer MUTEX_PUBLISH = -2;
    private static final Integer MUTEX_REPORT = -4;
    private static final Integer MUTEX_LINKAGE = -8;
    private volatile boolean sealed;
    private boolean isWriting;
    private Properties mailProps;
    private Authenticator auth;
    private Session session;
    private int[] matched;
    private LogRecord[] data;
    private int size;
    private int capacity;
    private Comparator<? super LogRecord> comparator;
    private Formatter subjectFormatter;
    private Level pushLevel;
    private Filter pushFilter;
    private volatile Filter filter;
    private volatile Level logLevel = Level.ALL;
    private volatile Filter[] attachmentFilters;
    private String encoding;
    private Formatter formatter;
    private Formatter[] attachmentFormatters;
    private Formatter[] attachmentNames;
    private FileTypeMap contentTypes;
    private volatile ErrorManager errorManager = this.defaultErrorManager();

    public MailHandler() {
        this.init(null);
        this.sealed = true;
        this.checkAccess();
    }

    public MailHandler(int capacity) {
        this.init(null);
        this.sealed = true;
        this.setCapacity0(capacity);
    }

    public MailHandler(Properties props) {
        if (props == null) {
            throw new NullPointerException();
        }
        this.init(props);
        this.sealed = true;
        this.setMailProperties0(props);
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        int levelValue = this.getLevel().intValue();
        if (record.getLevel().intValue() < levelValue || levelValue == offValue) {
            return false;
        }
        Filter body = this.getFilter();
        if (body == null || body.isLoggable(record)) {
            this.setMatchedPart(-1);
            return true;
        }
        return this.isAttachmentLoggable(record);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void publish(LogRecord record) {
        if (this.tryMutex()) {
            try {
                block6: {
                    try {
                        if (!this.isLoggable(record)) break block6;
                        record.getSourceMethodName();
                        this.publish0(record);
                    }
                    catch (LinkageError JDK8152515) {
                        this.reportLinkageError(JDK8152515, 1);
                        Object var4_3 = null;
                        this.releaseMutex();
                    }
                }
                Object var4_2 = null;
                this.releaseMutex();
            }
            catch (Throwable throwable) {
                Object var4_4 = null;
                this.releaseMutex();
                throw throwable;
            }
        } else {
            this.reportUnPublishedError(record);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void publish0(LogRecord record) {
        Message msg;
        boolean priority;
        MailHandler mailHandler = this;
        synchronized (mailHandler) {
            if (this.size == this.data.length && this.size < this.capacity) {
                this.grow();
            }
            if (this.size < this.data.length) {
                this.matched[this.size] = this.getMatchedPart();
                this.data[this.size] = record;
                ++this.size;
                priority = this.isPushable(record);
                msg = priority || this.size >= this.capacity ? this.writeLogRecords(1) : null;
            } else {
                priority = false;
                msg = null;
            }
        }
        if (msg != null) {
            this.send(msg, priority, 1);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void reportUnPublishedError(LogRecord record) {
        Integer idx = MUTEX.get();
        if (idx == null || idx > MUTEX_REPORT) {
            MUTEX.set(MUTEX_REPORT);
            try {
                String msg;
                if (record != null) {
                    Formatter f = MailHandler.createSimpleFormatter();
                    msg = "Log record " + record.getSequenceNumber() + " was not published. " + this.head(f) + this.format(f, record) + this.tail(f, "");
                } else {
                    msg = null;
                }
                IllegalStateException e = new IllegalStateException("Recursive publish detected by thread " + Thread.currentThread());
                this.reportError(msg, (Exception)e, 1);
                Object var6_5 = null;
                if (idx != null) {
                    MUTEX.set(idx);
                } else {
                    MUTEX.remove();
                }
            }
            catch (Throwable throwable) {
                Object var6_6 = null;
                if (idx != null) {
                    MUTEX.set(idx);
                } else {
                    MUTEX.remove();
                }
                throw throwable;
            }
        }
    }

    private boolean tryMutex() {
        if (MUTEX.get() == null) {
            MUTEX.set(MUTEX_PUBLISH);
            return true;
        }
        return false;
    }

    private void releaseMutex() {
        MUTEX.remove();
    }

    private int getMatchedPart() {
        Integer idx = MUTEX.get();
        if (idx == null || idx >= this.readOnlyAttachmentFilters().length) {
            idx = MUTEX_PUBLISH;
        }
        return idx;
    }

    private void setMatchedPart(int index) {
        if (MUTEX_PUBLISH.equals(MUTEX.get())) {
            MUTEX.set(index);
        }
    }

    private void clearMatches(int index) {
        assert (Thread.holdsLock(this));
        for (int r = 0; r < this.size; ++r) {
            if (this.matched[r] < index) continue;
            this.matched[r] = MUTEX_PUBLISH;
        }
    }

    public void postConstruct() {
    }

    public void preDestroy() {
        this.push(false, 3);
    }

    public void push() {
        this.push(true, 2);
    }

    @Override
    public void flush() {
        this.push(false, 2);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() {
        try {
            this.checkAccess();
            Message msg = null;
            MailHandler mailHandler = this;
            synchronized (mailHandler) {
                block11: {
                    try {
                        msg = this.writeLogRecords(3);
                        Object var4_4 = null;
                        this.logLevel = Level.OFF;
                        if (this.capacity <= 0) break block11;
                    }
                    catch (Throwable throwable) {
                        Object var4_5 = null;
                        this.logLevel = Level.OFF;
                        if (this.capacity > 0) {
                            this.capacity = -this.capacity;
                        }
                        if (this.size == 0 && this.data.length != 1) {
                            this.data = new LogRecord[1];
                            this.matched = new int[this.data.length];
                        }
                        throw throwable;
                    }
                    this.capacity = -this.capacity;
                }
                if (this.size == 0 && this.data.length != 1) {
                    this.data = new LogRecord[1];
                    this.matched = new int[this.data.length];
                }
            }
            if (msg != null) {
                this.send(msg, false, 3);
            }
        }
        catch (LinkageError JDK8152515) {
            this.reportLinkageError(JDK8152515, 3);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setLevel(Level newLevel) {
        if (newLevel == null) {
            throw new NullPointerException();
        }
        this.checkAccess();
        MailHandler mailHandler = this;
        synchronized (mailHandler) {
            if (this.capacity > 0) {
                this.logLevel = newLevel;
            }
        }
    }

    @Override
    public Level getLevel() {
        return this.logLevel;
    }

    @Override
    public ErrorManager getErrorManager() {
        this.checkAccess();
        return this.errorManager;
    }

    @Override
    public void setErrorManager(ErrorManager em) {
        this.checkAccess();
        this.setErrorManager0(em);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setErrorManager0(ErrorManager em) {
        if (em == null) {
            throw new NullPointerException();
        }
        try {
            MailHandler mailHandler = this;
            synchronized (mailHandler) {
                this.errorManager = em;
                super.setErrorManager(em);
            }
        }
        catch (RuntimeException runtimeException) {
        }
        catch (LinkageError linkageError) {
            // empty catch block
        }
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setFilter(Filter newFilter) {
        this.checkAccess();
        MailHandler mailHandler = this;
        synchronized (mailHandler) {
            if (newFilter != this.filter) {
                this.clearMatches(-1);
            }
            this.filter = newFilter;
        }
    }

    @Override
    public synchronized String getEncoding() {
        return this.encoding;
    }

    @Override
    public void setEncoding(String encoding) throws UnsupportedEncodingException {
        this.checkAccess();
        this.setEncoding0(encoding);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setEncoding0(String e) throws UnsupportedEncodingException {
        if (e != null) {
            try {
                if (!Charset.isSupported(e)) {
                    throw new UnsupportedEncodingException(e);
                }
            }
            catch (IllegalCharsetNameException icne) {
                throw new UnsupportedEncodingException(e);
            }
        }
        MailHandler mailHandler = this;
        synchronized (mailHandler) {
            this.encoding = e;
        }
    }

    @Override
    public synchronized Formatter getFormatter() {
        return this.formatter;
    }

    @Override
    public synchronized void setFormatter(Formatter newFormatter) throws SecurityException {
        this.checkAccess();
        if (newFormatter == null) {
            throw new NullPointerException();
        }
        this.formatter = newFormatter;
    }

    public final synchronized Level getPushLevel() {
        return this.pushLevel;
    }

    public final synchronized void setPushLevel(Level level) {
        this.checkAccess();
        if (level == null) {
            throw new NullPointerException();
        }
        if (this.isWriting) {
            throw new IllegalStateException();
        }
        this.pushLevel = level;
    }

    public final synchronized Filter getPushFilter() {
        return this.pushFilter;
    }

    public final synchronized void setPushFilter(Filter filter) {
        this.checkAccess();
        if (this.isWriting) {
            throw new IllegalStateException();
        }
        this.pushFilter = filter;
    }

    public final synchronized Comparator<? super LogRecord> getComparator() {
        return this.comparator;
    }

    public final synchronized void setComparator(Comparator<? super LogRecord> c) {
        this.checkAccess();
        if (this.isWriting) {
            throw new IllegalStateException();
        }
        this.comparator = c;
    }

    public final synchronized int getCapacity() {
        assert (this.capacity != Integer.MIN_VALUE && this.capacity != 0) : this.capacity;
        return Math.abs(this.capacity);
    }

    public final synchronized Authenticator getAuthenticator() {
        this.checkAccess();
        return this.auth;
    }

    public final void setAuthenticator(Authenticator auth) {
        this.setAuthenticator0(auth);
    }

    public final void setAuthenticator(char ... password) {
        if (password == null) {
            this.setAuthenticator0(null);
        } else {
            this.setAuthenticator0(DefaultAuthenticator.of(new String(password)));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setAuthenticator0(Authenticator auth) {
        Session settings;
        this.checkAccess();
        MailHandler mailHandler = this;
        synchronized (mailHandler) {
            if (this.isWriting) {
                throw new IllegalStateException();
            }
            this.auth = auth;
            settings = this.updateSession();
        }
        this.verifySettings(settings);
    }

    public final void setMailProperties(Properties props) {
        this.setMailProperties0(props);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setMailProperties0(Properties props) {
        Session settings;
        this.checkAccess();
        props = (Properties)props.clone();
        MailHandler mailHandler = this;
        synchronized (mailHandler) {
            if (this.isWriting) {
                throw new IllegalStateException();
            }
            this.mailProps = props;
            settings = this.updateSession();
        }
        this.verifySettings(settings);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final Properties getMailProperties() {
        Properties props;
        this.checkAccess();
        MailHandler mailHandler = this;
        synchronized (mailHandler) {
            props = this.mailProps;
        }
        return (Properties)props.clone();
    }

    public final Filter[] getAttachmentFilters() {
        return (Filter[])this.readOnlyAttachmentFilters().clone();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void setAttachmentFilters(Filter ... filters) {
        this.checkAccess();
        filters = filters.length == 0 ? MailHandler.emptyFilterArray() : (Filter[])MailHandler.copyOf(filters, filters.length, Filter[].class);
        MailHandler mailHandler = this;
        synchronized (mailHandler) {
            if (this.attachmentFormatters.length != filters.length) {
                throw MailHandler.attachmentMismatch(this.attachmentFormatters.length, filters.length);
            }
            if (this.isWriting) {
                throw new IllegalStateException();
            }
            if (this.size != 0) {
                for (int i = 0; i < filters.length; ++i) {
                    if (filters[i] == this.attachmentFilters[i]) continue;
                    this.clearMatches(i);
                    break;
                }
            }
            this.attachmentFilters = filters;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final Formatter[] getAttachmentFormatters() {
        Formatter[] formatters;
        MailHandler mailHandler = this;
        synchronized (mailHandler) {
            formatters = this.attachmentFormatters;
        }
        return (Formatter[])formatters.clone();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void setAttachmentFormatters(Formatter ... formatters) {
        this.checkAccess();
        if (formatters.length == 0) {
            formatters = MailHandler.emptyFormatterArray();
        } else {
            formatters = (Formatter[])MailHandler.copyOf(formatters, formatters.length, Formatter[].class);
            for (int i = 0; i < formatters.length; ++i) {
                if (formatters[i] != null) continue;
                throw new NullPointerException(MailHandler.atIndexMsg(i));
            }
        }
        MailHandler mailHandler = this;
        synchronized (mailHandler) {
            if (this.isWriting) {
                throw new IllegalStateException();
            }
            this.attachmentFormatters = formatters;
            this.alignAttachmentFilters();
            this.alignAttachmentNames();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final Formatter[] getAttachmentNames() {
        Formatter[] formatters;
        MailHandler mailHandler = this;
        synchronized (mailHandler) {
            formatters = this.attachmentNames;
        }
        return (Formatter[])formatters.clone();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void setAttachmentNames(String ... names) {
        this.checkAccess();
        Formatter[] formatters = names.length == 0 ? MailHandler.emptyFormatterArray() : new Formatter[names.length];
        for (int i = 0; i < names.length; ++i) {
            String name = names[i];
            if (name != null) {
                if (name.length() <= 0) {
                    throw new IllegalArgumentException(MailHandler.atIndexMsg(i));
                }
            } else {
                throw new NullPointerException(MailHandler.atIndexMsg(i));
            }
            formatters[i] = TailNameFormatter.of(name);
        }
        MailHandler mailHandler = this;
        synchronized (mailHandler) {
            if (this.attachmentFormatters.length != names.length) {
                throw MailHandler.attachmentMismatch(this.attachmentFormatters.length, names.length);
            }
            if (this.isWriting) {
                throw new IllegalStateException();
            }
            this.attachmentNames = formatters;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void setAttachmentNames(Formatter ... formatters) {
        this.checkAccess();
        formatters = formatters.length == 0 ? MailHandler.emptyFormatterArray() : (Formatter[])MailHandler.copyOf(formatters, formatters.length, Formatter[].class);
        for (int i = 0; i < formatters.length; ++i) {
            if (formatters[i] != null) continue;
            throw new NullPointerException(MailHandler.atIndexMsg(i));
        }
        MailHandler mailHandler = this;
        synchronized (mailHandler) {
            if (this.attachmentFormatters.length != formatters.length) {
                throw MailHandler.attachmentMismatch(this.attachmentFormatters.length, formatters.length);
            }
            if (this.isWriting) {
                throw new IllegalStateException();
            }
            this.attachmentNames = formatters;
        }
    }

    public final synchronized Formatter getSubject() {
        return this.subjectFormatter;
    }

    public final void setSubject(String subject) {
        if (subject == null) {
            this.checkAccess();
            throw new NullPointerException();
        }
        this.setSubject(TailNameFormatter.of(subject));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void setSubject(Formatter format) {
        this.checkAccess();
        if (format == null) {
            throw new NullPointerException();
        }
        MailHandler mailHandler = this;
        synchronized (mailHandler) {
            if (this.isWriting) {
                throw new IllegalStateException();
            }
            this.subjectFormatter = format;
        }
    }

    @Override
    protected void reportError(String msg, Exception ex, int code) {
        try {
            if (msg != null) {
                this.errorManager.error(Level.SEVERE.getName().concat(": ").concat(msg), ex, code);
            } else {
                this.errorManager.error(null, ex, code);
            }
        }
        catch (RuntimeException GLASSFISH_21258) {
            this.reportLinkageError(GLASSFISH_21258, code);
        }
        catch (LinkageError GLASSFISH_21258) {
            this.reportLinkageError(GLASSFISH_21258, code);
        }
    }

    private void checkAccess() {
        if (this.sealed) {
            LogManagerProperties.checkLogManagerAccess();
        }
    }

    final String contentTypeOf(String head) {
        if (!MailHandler.isEmpty(head)) {
            int MAX_CHARS = 25;
            if (head.length() > 25) {
                head = head.substring(0, 25);
            }
            try {
                String charset = this.getEncodingName();
                ByteArrayInputStream in = new ByteArrayInputStream(head.getBytes(charset));
                assert (in.markSupported()) : in.getClass().getName();
                return URLConnection.guessContentTypeFromStream(in);
            }
            catch (IOException IOE) {
                this.reportError(IOE.getMessage(), (Exception)IOE, 5);
            }
        }
        return null;
    }

    final String contentTypeOf(Formatter f) {
        if (f != null) {
            for (Class<?> k = f.getClass(); k != Formatter.class; k = k.getSuperclass()) {
                String name = k.getName().toLowerCase(Locale.ENGLISH);
                int idx = name.indexOf(36) + 1;
                while ((idx = name.indexOf("ml", idx)) > -1) {
                    if (idx > 0) {
                        if (name.charAt(idx - 1) == 'x') {
                            return "application/xml";
                        }
                        if (idx > 1 && name.charAt(idx - 2) == 'h' && name.charAt(idx - 1) == 't') {
                            return "text/html";
                        }
                    }
                    idx += 2;
                }
            }
        }
        return "text/plain";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    final boolean isMissingContent(Message msg, Throwable t) {
        Object ccl = this.getAndSetContextClassLoader(MAILHANDLER_LOADER);
        try {
            try {
                msg.writeTo(new ByteArrayOutputStream(1024));
            }
            catch (RuntimeException RE) {
                throw RE;
            }
            catch (Exception noContent) {
                String txt = noContent.getMessage();
                if (!MailHandler.isEmpty(txt)) {
                    while (t != null) {
                        if (noContent.getClass() == t.getClass() && txt.equals(t.getMessage())) {
                            boolean bl = true;
                            Object var8_5 = null;
                            this.getAndSetContextClassLoader(ccl);
                            return bl;
                        }
                        t = t.getCause();
                    }
                }
                Object var8_6 = null;
                this.getAndSetContextClassLoader(ccl);
                return false;
            }
            Object var8_4 = null;
            this.getAndSetContextClassLoader(ccl);
            return false;
        }
        catch (Throwable throwable) {
            Object var8_7 = null;
            this.getAndSetContextClassLoader(ccl);
            throw throwable;
        }
    }

    private void reportError(Message msg, Exception ex, int code) {
        try {
            try {
                this.errorManager.error(this.toRawString(msg), ex, code);
            }
            catch (RuntimeException re) {
                this.reportError(this.toMsgString(re), ex, code);
            }
            catch (Exception e) {
                this.reportError(this.toMsgString(e), ex, code);
            }
        }
        catch (LinkageError GLASSFISH_21258) {
            this.reportLinkageError(GLASSFISH_21258, code);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void reportLinkageError(Throwable le, int code) {
        block15: {
            if (le == null) {
                throw new NullPointerException(String.valueOf(code));
            }
            Integer idx = MUTEX.get();
            if (idx == null || idx > MUTEX_LINKAGE) {
                MUTEX.set(MUTEX_LINKAGE);
                try {
                    try {
                        Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), le);
                    }
                    catch (RuntimeException runtimeException) {
                        Object var6_5 = null;
                        if (idx != null) {
                            MUTEX.set(idx);
                        } else {
                            MUTEX.remove();
                        }
                        break block15;
                    }
                    catch (LinkageError linkageError) {
                        Object var6_6 = null;
                        if (idx != null) {
                            MUTEX.set(idx);
                        } else {
                            MUTEX.remove();
                        }
                    }
                    Object var6_4 = null;
                    if (idx != null) {
                        MUTEX.set(idx);
                    } else {
                        MUTEX.remove();
                    }
                }
                catch (Throwable throwable) {
                    Object var6_7 = null;
                    if (idx != null) {
                        MUTEX.set(idx);
                    } else {
                        MUTEX.remove();
                    }
                    throw throwable;
                }
            }
        }
    }

    private String getContentType(String name) {
        assert (Thread.holdsLock(this));
        String type = this.contentTypes.getContentType(name);
        if ("application/octet-stream".equalsIgnoreCase(type)) {
            return null;
        }
        return type;
    }

    private String getEncodingName() {
        String charset = this.getEncoding();
        if (charset == null) {
            charset = MimeUtility.getDefaultJavaCharset();
        }
        return charset;
    }

    private void setContent(MimeBodyPart part, CharSequence buf, String type) throws MessagingException {
        String charset = this.getEncodingName();
        if (type != null && !"text/plain".equalsIgnoreCase(type)) {
            type = this.contentWithEncoding(type, charset);
            try {
                ByteArrayDataSource source = new ByteArrayDataSource(buf.toString(), type);
                part.setDataHandler(new DataHandler((DataSource)source));
            }
            catch (IOException IOE) {
                this.reportError(IOE.getMessage(), (Exception)IOE, 5);
                part.setText(buf.toString(), charset);
            }
        } else {
            part.setText(buf.toString(), MimeUtility.mimeCharset(charset));
        }
    }

    private String contentWithEncoding(String type, String encoding) {
        assert (encoding != null);
        try {
            ContentType ct = new ContentType(type);
            ct.setParameter("charset", MimeUtility.mimeCharset(encoding));
            encoding = ct.toString();
            if (!MailHandler.isEmpty(encoding)) {
                type = encoding;
            }
        }
        catch (MessagingException ME) {
            this.reportError(type, (Exception)ME, 5);
        }
        return type;
    }

    private synchronized void setCapacity0(int newCapacity) {
        this.checkAccess();
        if (newCapacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero.");
        }
        if (this.isWriting) {
            throw new IllegalStateException();
        }
        this.capacity = this.capacity < 0 ? -newCapacity : newCapacity;
    }

    private Filter[] readOnlyAttachmentFilters() {
        return this.attachmentFilters;
    }

    private static Formatter[] emptyFormatterArray() {
        return EMPTY_FORMATTERS;
    }

    private static Filter[] emptyFilterArray() {
        return EMPTY_FILTERS;
    }

    private boolean alignAttachmentNames() {
        assert (Thread.holdsLock(this));
        boolean fixed = false;
        int current = this.attachmentNames.length;
        int expect = this.attachmentFormatters.length;
        if (current != expect) {
            this.attachmentNames = (Formatter[])MailHandler.copyOf(this.attachmentNames, expect, Formatter[].class);
            boolean bl = fixed = current != 0;
        }
        if (expect == 0) {
            this.attachmentNames = MailHandler.emptyFormatterArray();
            assert (this.attachmentNames.length == 0);
        } else {
            for (int i = 0; i < expect; ++i) {
                if (this.attachmentNames[i] != null) continue;
                this.attachmentNames[i] = TailNameFormatter.of(this.toString(this.attachmentFormatters[i]));
            }
        }
        return fixed;
    }

    private boolean alignAttachmentFilters() {
        assert (Thread.holdsLock(this));
        boolean fixed = false;
        int current = this.attachmentFilters.length;
        int expect = this.attachmentFormatters.length;
        if (current != expect) {
            this.attachmentFilters = (Filter[])MailHandler.copyOf(this.attachmentFilters, expect, Filter[].class);
            this.clearMatches(current);
            fixed = current != 0;
            Filter body = this.filter;
            if (body != null) {
                for (int i = current; i < expect; ++i) {
                    this.attachmentFilters[i] = body;
                }
            }
        }
        if (expect == 0) {
            this.attachmentFilters = MailHandler.emptyFilterArray();
            assert (this.attachmentFilters.length == 0);
        }
        return fixed;
    }

    private static int[] copyOf(int[] a, int len) {
        int[] copy = new int[len];
        System.arraycopy(a, 0, copy, 0, Math.min(len, a.length));
        return copy;
    }

    private static <T, U> T[] copyOf(U[] a, int len, Class<? extends T[]> type) {
        Object[] copy = (Object[])Array.newInstance(type.getComponentType(), len);
        System.arraycopy(a, 0, copy, 0, Math.min(len, a.length));
        return copy;
    }

    private void reset() {
        assert (Thread.holdsLock(this));
        if (this.size < this.data.length) {
            Arrays.fill(this.data, 0, this.size, null);
        } else {
            Arrays.fill(this.data, null);
        }
        this.size = 0;
    }

    private void grow() {
        assert (Thread.holdsLock(this));
        int len = this.data.length;
        int newCapacity = len + (len >> 1) + 1;
        if (newCapacity > this.capacity || newCapacity < len) {
            newCapacity = this.capacity;
        }
        assert (len != this.capacity) : len;
        this.data = (LogRecord[])MailHandler.copyOf(this.data, newCapacity, LogRecord[].class);
        this.matched = MailHandler.copyOf(this.matched, newCapacity);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private synchronized void init(Properties props) {
        assert (this.errorManager != null);
        String p = this.getClass().getName();
        this.mailProps = new Properties();
        Object ccl = this.getAndSetContextClassLoader(MAILHANDLER_LOADER);
        try {
            this.contentTypes = FileTypeMap.getDefaultFileTypeMap();
            Object var5_4 = null;
            this.getAndSetContextClassLoader(ccl);
        }
        catch (Throwable throwable) {
            Object var5_5 = null;
            this.getAndSetContextClassLoader(ccl);
            throw throwable;
        }
        this.initErrorManager(p);
        this.initLevel(p);
        this.initFilter(p);
        this.initCapacity(p);
        this.initAuthenticator(p);
        this.initEncoding(p);
        this.initFormatter(p);
        this.initComparator(p);
        this.initPushLevel(p);
        this.initPushFilter(p);
        this.initSubject(p);
        this.initAttachmentFormaters(p);
        this.initAttachmentFilters(p);
        this.initAttachmentNames(p);
        if (props == null && LogManagerProperties.fromLogManager(p.concat(".verify")) != null) {
            this.verifySettings(this.initSession());
        }
        this.intern();
    }

    private void intern() {
        assert (Thread.holdsLock(this));
        try {
            Object result;
            Object canidate;
            HashMap<Object, Object> seen = new HashMap<Object, Object>();
            try {
                this.intern(seen, this.errorManager);
            }
            catch (SecurityException se) {
                this.reportError(se.getMessage(), (Exception)se, 4);
            }
            try {
                canidate = this.filter;
                result = this.intern(seen, canidate);
                if (result != canidate && result instanceof Filter) {
                    this.filter = (Filter)result;
                }
                if ((result = this.intern(seen, canidate = this.formatter)) != canidate && result instanceof Formatter) {
                    this.formatter = (Formatter)result;
                }
            }
            catch (SecurityException se) {
                this.reportError(se.getMessage(), (Exception)se, 4);
            }
            canidate = this.subjectFormatter;
            result = this.intern(seen, canidate);
            if (result != canidate && result instanceof Formatter) {
                this.subjectFormatter = (Formatter)result;
            }
            if ((result = this.intern(seen, canidate = this.pushFilter)) != canidate && result instanceof Filter) {
                this.pushFilter = (Filter)result;
            }
            for (int i = 0; i < this.attachmentFormatters.length; ++i) {
                canidate = this.attachmentFormatters[i];
                result = this.intern(seen, canidate);
                if (result != canidate && result instanceof Formatter) {
                    this.attachmentFormatters[i] = (Formatter)result;
                }
                if ((result = this.intern(seen, canidate = this.attachmentFilters[i])) != canidate && result instanceof Filter) {
                    this.attachmentFilters[i] = (Filter)result;
                }
                if ((result = this.intern(seen, canidate = this.attachmentNames[i])) == canidate || !(result instanceof Formatter)) continue;
                this.attachmentNames[i] = (Formatter)result;
            }
        }
        catch (Exception skip) {
            this.reportError(skip.getMessage(), skip, 4);
        }
        catch (LinkageError skip) {
            this.reportError(skip.getMessage(), (Exception)new InvocationTargetException(skip), 4);
        }
    }

    private Object intern(Map<Object, Object> m, Object o) throws Exception {
        Object use;
        if (o == null) {
            return null;
        }
        Object key = o.getClass().getName().equals(TailNameFormatter.class.getName()) ? o : o.getClass().getConstructor(new Class[0]).newInstance(new Object[0]);
        if (key.getClass() == o.getClass()) {
            Object found = m.get(key);
            if (found == null) {
                boolean right = key.equals(o);
                boolean left = o.equals(key);
                if (right && left) {
                    found = m.put(o, o);
                    if (found != null) {
                        this.reportNonDiscriminating(key, found);
                        found = m.remove(key);
                        if (found != o) {
                            this.reportNonDiscriminating(key, found);
                            m.clear();
                        }
                    }
                } else if (right != left) {
                    this.reportNonSymmetric(o, key);
                }
                use = o;
            } else if (o.getClass() == found.getClass()) {
                use = found;
            } else {
                this.reportNonDiscriminating(o, found);
                use = o;
            }
        } else {
            use = o;
        }
        return use;
    }

    private static Formatter createSimpleFormatter() {
        return (Formatter)Formatter.class.cast(new SimpleFormatter());
    }

    private static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    private static boolean hasValue(String name) {
        return !MailHandler.isEmpty(name) && !"null".equalsIgnoreCase(name);
    }

    private void initAttachmentFilters(String p) {
        assert (Thread.holdsLock(this));
        assert (this.attachmentFormatters != null);
        String list = LogManagerProperties.fromLogManager(p.concat(".attachment.filters"));
        if (!MailHandler.isEmpty(list)) {
            String[] names = list.split(",");
            Filter[] a = new Filter[names.length];
            for (int i = 0; i < a.length; ++i) {
                names[i] = names[i].trim();
                if ("null".equalsIgnoreCase(names[i])) continue;
                try {
                    a[i] = LogManagerProperties.newFilter(names[i]);
                    continue;
                }
                catch (SecurityException SE) {
                    throw SE;
                }
                catch (Exception E) {
                    this.reportError(E.getMessage(), E, 4);
                }
            }
            this.attachmentFilters = a;
            if (this.alignAttachmentFilters()) {
                this.reportError("Attachment filters.", (Exception)MailHandler.attachmentMismatch("Length mismatch."), 4);
            }
        } else {
            this.attachmentFilters = MailHandler.emptyFilterArray();
            this.alignAttachmentFilters();
        }
    }

    private void initAttachmentFormaters(String p) {
        assert (Thread.holdsLock(this));
        String list = LogManagerProperties.fromLogManager(p.concat(".attachment.formatters"));
        if (!MailHandler.isEmpty(list)) {
            String[] names = list.split(",");
            Formatter[] a = names.length == 0 ? MailHandler.emptyFormatterArray() : new Formatter[names.length];
            for (int i = 0; i < a.length; ++i) {
                names[i] = names[i].trim();
                if (!"null".equalsIgnoreCase(names[i])) {
                    try {
                        a[i] = LogManagerProperties.newFormatter(names[i]);
                        if (!(a[i] instanceof TailNameFormatter)) continue;
                        ClassNotFoundException CNFE = new ClassNotFoundException(a[i].toString());
                        this.reportError("Attachment formatter.", (Exception)CNFE, 4);
                        a[i] = MailHandler.createSimpleFormatter();
                        continue;
                    }
                    catch (SecurityException SE) {
                        throw SE;
                    }
                    catch (Exception E) {
                        this.reportError(E.getMessage(), E, 4);
                        a[i] = MailHandler.createSimpleFormatter();
                        continue;
                    }
                }
                NullPointerException NPE = new NullPointerException(MailHandler.atIndexMsg(i));
                this.reportError("Attachment formatter.", (Exception)NPE, 4);
                a[i] = MailHandler.createSimpleFormatter();
            }
            this.attachmentFormatters = a;
        } else {
            this.attachmentFormatters = MailHandler.emptyFormatterArray();
        }
    }

    private void initAttachmentNames(String p) {
        assert (Thread.holdsLock(this));
        assert (this.attachmentFormatters != null);
        String list = LogManagerProperties.fromLogManager(p.concat(".attachment.names"));
        if (!MailHandler.isEmpty(list)) {
            String[] names = list.split(",");
            Formatter[] a = new Formatter[names.length];
            for (int i = 0; i < a.length; ++i) {
                names[i] = names[i].trim();
                if (!"null".equalsIgnoreCase(names[i])) {
                    try {
                        try {
                            a[i] = LogManagerProperties.newFormatter(names[i]);
                        }
                        catch (ClassNotFoundException literal) {
                            a[i] = TailNameFormatter.of(names[i]);
                        }
                        catch (ClassCastException literal) {
                            a[i] = TailNameFormatter.of(names[i]);
                        }
                        continue;
                    }
                    catch (SecurityException SE) {
                        throw SE;
                    }
                    catch (Exception E) {
                        this.reportError(E.getMessage(), E, 4);
                        continue;
                    }
                }
                NullPointerException NPE = new NullPointerException(MailHandler.atIndexMsg(i));
                this.reportError("Attachment names.", (Exception)NPE, 4);
            }
            this.attachmentNames = a;
            if (this.alignAttachmentNames()) {
                this.reportError("Attachment names.", (Exception)MailHandler.attachmentMismatch("Length mismatch."), 4);
            }
        } else {
            this.attachmentNames = MailHandler.emptyFormatterArray();
            this.alignAttachmentNames();
        }
    }

    private void initAuthenticator(String p) {
        assert (Thread.holdsLock(this));
        String name = LogManagerProperties.fromLogManager(p.concat(".authenticator"));
        if (name != null && !"null".equalsIgnoreCase(name)) {
            if (name.length() != 0) {
                try {
                    this.auth = LogManagerProperties.newObjectFrom(name, Authenticator.class);
                }
                catch (SecurityException SE) {
                    throw SE;
                }
                catch (ClassNotFoundException literalAuth) {
                    this.auth = DefaultAuthenticator.of(name);
                }
                catch (ClassCastException literalAuth) {
                    this.auth = DefaultAuthenticator.of(name);
                }
                catch (Exception E) {
                    this.reportError(E.getMessage(), E, 4);
                }
            } else {
                this.auth = DefaultAuthenticator.of(name);
            }
        }
    }

    private void initLevel(String p) {
        assert (Thread.holdsLock(this));
        try {
            String val = LogManagerProperties.fromLogManager(p.concat(".level"));
            this.logLevel = val != null ? Level.parse(val) : Level.WARNING;
        }
        catch (SecurityException SE) {
            throw SE;
        }
        catch (RuntimeException RE) {
            this.reportError(RE.getMessage(), (Exception)RE, 4);
            this.logLevel = Level.WARNING;
        }
    }

    private void initFilter(String p) {
        assert (Thread.holdsLock(this));
        try {
            String name = LogManagerProperties.fromLogManager(p.concat(".filter"));
            if (MailHandler.hasValue(name)) {
                this.filter = LogManagerProperties.newFilter(name);
            }
        }
        catch (SecurityException SE) {
            throw SE;
        }
        catch (Exception E) {
            this.reportError(E.getMessage(), E, 4);
        }
    }

    private void initCapacity(String p) {
        assert (Thread.holdsLock(this));
        int DEFAULT_CAPACITY = 1000;
        try {
            String value = LogManagerProperties.fromLogManager(p.concat(".capacity"));
            if (value != null) {
                this.setCapacity0(Integer.parseInt(value));
            } else {
                this.setCapacity0(1000);
            }
        }
        catch (SecurityException SE) {
            throw SE;
        }
        catch (RuntimeException RE) {
            this.reportError(RE.getMessage(), (Exception)RE, 4);
        }
        if (this.capacity <= 0) {
            this.capacity = 1000;
        }
        this.data = new LogRecord[1];
        this.matched = new int[this.data.length];
    }

    private void initEncoding(String p) {
        assert (Thread.holdsLock(this));
        try {
            String e = LogManagerProperties.fromLogManager(p.concat(".encoding"));
            if (e != null) {
                this.setEncoding0(e);
            }
        }
        catch (SecurityException SE) {
            throw SE;
        }
        catch (UnsupportedEncodingException UEE) {
            this.reportError(UEE.getMessage(), (Exception)UEE, 4);
        }
        catch (RuntimeException RE) {
            this.reportError(RE.getMessage(), (Exception)RE, 4);
        }
    }

    private ErrorManager defaultErrorManager() {
        ErrorManager em;
        try {
            em = super.getErrorManager();
        }
        catch (RuntimeException ignore) {
            em = null;
        }
        catch (LinkageError ignore) {
            em = null;
        }
        if (em == null) {
            em = new ErrorManager();
        }
        return em;
    }

    private void initErrorManager(String p) {
        assert (Thread.holdsLock(this));
        try {
            String name = LogManagerProperties.fromLogManager(p.concat(".errorManager"));
            if (name != null) {
                this.setErrorManager0(LogManagerProperties.newErrorManager(name));
            }
        }
        catch (SecurityException SE) {
            throw SE;
        }
        catch (Exception E) {
            this.reportError(E.getMessage(), E, 4);
        }
    }

    private void initFormatter(String p) {
        assert (Thread.holdsLock(this));
        try {
            String name = LogManagerProperties.fromLogManager(p.concat(".formatter"));
            if (MailHandler.hasValue(name)) {
                Formatter f = LogManagerProperties.newFormatter(name);
                assert (f != null);
                this.formatter = !(f instanceof TailNameFormatter) ? f : MailHandler.createSimpleFormatter();
            } else {
                this.formatter = MailHandler.createSimpleFormatter();
            }
        }
        catch (SecurityException SE) {
            throw SE;
        }
        catch (Exception E) {
            this.reportError(E.getMessage(), E, 4);
            this.formatter = MailHandler.createSimpleFormatter();
        }
    }

    private void initComparator(String p) {
        assert (Thread.holdsLock(this));
        try {
            String name = LogManagerProperties.fromLogManager(p.concat(".comparator"));
            String reverse = LogManagerProperties.fromLogManager(p.concat(".comparator.reverse"));
            if (MailHandler.hasValue(name)) {
                this.comparator = LogManagerProperties.newComparator(name);
                if (Boolean.parseBoolean(reverse)) {
                    assert (this.comparator != null) : "null";
                    this.comparator = LogManagerProperties.reverseOrder(this.comparator);
                }
            } else if (!MailHandler.isEmpty(reverse)) {
                throw new IllegalArgumentException("No comparator to reverse.");
            }
        }
        catch (SecurityException SE) {
            throw SE;
        }
        catch (Exception E) {
            this.reportError(E.getMessage(), E, 4);
        }
    }

    private void initPushLevel(String p) {
        assert (Thread.holdsLock(this));
        try {
            String val = LogManagerProperties.fromLogManager(p.concat(".pushLevel"));
            if (val != null) {
                this.pushLevel = Level.parse(val);
            }
        }
        catch (RuntimeException RE) {
            this.reportError(RE.getMessage(), (Exception)RE, 4);
        }
        if (this.pushLevel == null) {
            this.pushLevel = Level.OFF;
        }
    }

    private void initPushFilter(String p) {
        assert (Thread.holdsLock(this));
        try {
            String name = LogManagerProperties.fromLogManager(p.concat(".pushFilter"));
            if (MailHandler.hasValue(name)) {
                this.pushFilter = LogManagerProperties.newFilter(name);
            }
        }
        catch (SecurityException SE) {
            throw SE;
        }
        catch (Exception E) {
            this.reportError(E.getMessage(), E, 4);
        }
    }

    private void initSubject(String p) {
        assert (Thread.holdsLock(this));
        String name = LogManagerProperties.fromLogManager(p.concat(".subject"));
        if (MailHandler.hasValue(name)) {
            try {
                this.subjectFormatter = LogManagerProperties.newFormatter(name);
            }
            catch (SecurityException SE) {
                throw SE;
            }
            catch (ClassNotFoundException literalSubject) {
                this.subjectFormatter = TailNameFormatter.of(name);
            }
            catch (ClassCastException literalSubject) {
                this.subjectFormatter = TailNameFormatter.of(name);
            }
            catch (Exception E) {
                this.subjectFormatter = TailNameFormatter.of(name);
                this.reportError(E.getMessage(), E, 4);
            }
        } else if (name != null) {
            this.subjectFormatter = TailNameFormatter.of(name);
        }
        if (this.subjectFormatter == null) {
            this.subjectFormatter = TailNameFormatter.of("");
        }
    }

    private boolean isAttachmentLoggable(LogRecord record) {
        Filter[] filters = this.readOnlyAttachmentFilters();
        for (int i = 0; i < filters.length; ++i) {
            Filter f = filters[i];
            if (f != null && !f.isLoggable(record)) continue;
            this.setMatchedPart(i);
            return true;
        }
        return false;
    }

    private boolean isPushable(LogRecord record) {
        assert (Thread.holdsLock(this));
        int value = this.getPushLevel().intValue();
        if (value == offValue || record.getLevel().intValue() < value) {
            return false;
        }
        Filter push = this.getPushFilter();
        if (push == null) {
            return true;
        }
        int match = this.getMatchedPart();
        if (match == -1 && this.getFilter() == push || match >= 0 && this.attachmentFilters[match] == push) {
            return true;
        }
        return push.isLoggable(record);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void push(boolean priority, int code) {
        if (this.tryMutex()) {
            try {
                block6: {
                    try {
                        Message msg = this.writeLogRecords(code);
                        if (msg == null) break block6;
                        this.send(msg, priority, code);
                    }
                    catch (LinkageError JDK8152515) {
                        this.reportLinkageError(JDK8152515, code);
                        Object var5_6 = null;
                        this.releaseMutex();
                    }
                }
                Object var5_5 = null;
                this.releaseMutex();
            }
            catch (Throwable throwable) {
                Object var5_7 = null;
                this.releaseMutex();
                throw throwable;
            }
        } else {
            this.reportUnPublishedError(null);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void send(Message msg, boolean priority, int code) {
        try {
            this.envelopeFor(msg, priority);
            Object ccl = this.getAndSetContextClassLoader(MAILHANDLER_LOADER);
            try {
                Transport.send(msg);
                Object var6_7 = null;
                this.getAndSetContextClassLoader(ccl);
            }
            catch (Throwable throwable) {
                Object var6_8 = null;
                this.getAndSetContextClassLoader(ccl);
                throw throwable;
            }
            {
            }
        }
        catch (RuntimeException re) {
            this.reportError(msg, (Exception)re, code);
        }
        catch (Exception e) {
            this.reportError(msg, e, code);
        }
    }

    private void sort() {
        assert (Thread.holdsLock(this));
        if (this.comparator != null) {
            try {
                if (this.size != 1) {
                    Arrays.sort(this.data, 0, this.size, this.comparator);
                } else if (this.comparator.compare(this.data[0], this.data[0]) != 0) {
                    throw new IllegalArgumentException(this.comparator.getClass().getName());
                }
            }
            catch (RuntimeException RE) {
                this.reportError(RE.getMessage(), (Exception)RE, 5);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private Message writeLogRecords(int code) {
        try {
            MailHandler mailHandler = this;
            synchronized (mailHandler) {
                if (this.size <= 0) return null;
                if (this.isWriting) return null;
                this.isWriting = true;
                try {
                    Message message = this.writeLogRecords0();
                    Object var5_6 = null;
                    this.isWriting = false;
                    if (this.size <= 0) return message;
                    this.reset();
                    return message;
                }
                catch (Throwable throwable) {
                    Object var5_7 = null;
                    this.isWriting = false;
                    if (this.size <= 0) throw throwable;
                    this.reset();
                    throw throwable;
                }
            }
        }
        catch (RuntimeException re) {
            this.reportError(re.getMessage(), (Exception)re, code);
            return null;
        }
        catch (Exception e) {
            this.reportError(e.getMessage(), e, code);
        }
        return null;
    }

    private Message writeLogRecords0() throws Exception {
        assert (Thread.holdsLock(this));
        this.sort();
        if (this.session == null) {
            this.initSession();
        }
        MimeMessage msg = new MimeMessage(this.session);
        msg.setDescription(this.descriptionFrom(this.comparator, this.pushLevel, this.pushFilter));
        MimeBodyPart[] parts = new MimeBodyPart[this.attachmentFormatters.length];
        StringBuilder[] buffers = new StringBuilder[parts.length];
        String contentType = null;
        StringBuilder buf = null;
        this.appendSubject(msg, this.head(this.subjectFormatter));
        MimeBodyPart body = this.createBodyPart();
        Formatter bodyFormat = this.getFormatter();
        Filter bodyFilter = this.getFilter();
        Locale lastLocale = null;
        for (int ix = 0; ix < this.size; ++ix) {
            boolean formatted = false;
            int match = this.matched[ix];
            LogRecord r = this.data[ix];
            this.data[ix] = null;
            Locale locale = this.localeFor(r);
            this.appendSubject(msg, this.format(this.subjectFormatter, r));
            Filter lmf = null;
            if (bodyFilter == null || match == -1 || parts.length == 0 || match < -1 && bodyFilter.isLoggable(r)) {
                lmf = bodyFilter;
                if (buf == null) {
                    buf = new StringBuilder();
                    String head = this.head(bodyFormat);
                    buf.append(head);
                    contentType = this.contentTypeOf(head);
                }
                formatted = true;
                buf.append(this.format(bodyFormat, r));
                if (locale != null && !locale.equals(lastLocale)) {
                    this.appendContentLang(body, locale);
                }
            }
            for (int i = 0; i < parts.length; ++i) {
                Filter af = this.attachmentFilters[i];
                if (af != null && lmf != af && match != i && (match >= i || !af.isLoggable(r))) continue;
                if (lmf == null && af != null) {
                    lmf = af;
                }
                if (parts[i] == null) {
                    parts[i] = this.createBodyPart(i);
                    buffers[i] = new StringBuilder();
                    buffers[i].append(this.head(this.attachmentFormatters[i]));
                    this.appendFileName(parts[i], this.head(this.attachmentNames[i]));
                }
                formatted = true;
                this.appendFileName(parts[i], this.format(this.attachmentNames[i], r));
                buffers[i].append(this.format(this.attachmentFormatters[i], r));
                if (locale == null || locale.equals(lastLocale)) continue;
                this.appendContentLang(parts[i], locale);
            }
            if (formatted) {
                if (locale != null && !locale.equals(lastLocale)) {
                    this.appendContentLang(msg, locale);
                }
            } else {
                this.reportFilterError(r);
            }
            lastLocale = locale;
        }
        this.size = 0;
        for (int i = parts.length - 1; i >= 0; --i) {
            if (parts[i] == null) continue;
            this.appendFileName(parts[i], this.tail(this.attachmentNames[i], "err"));
            buffers[i].append(this.tail(this.attachmentFormatters[i], ""));
            if (buffers[i].length() > 0) {
                String name = parts[i].getFileName();
                if (MailHandler.isEmpty(name)) {
                    name = this.toString(this.attachmentFormatters[i]);
                    parts[i].setFileName(name);
                }
                this.setContent(parts[i], buffers[i], this.getContentType(name));
            } else {
                this.setIncompleteCopy(msg);
                parts[i] = null;
            }
            buffers[i] = null;
        }
        if (buf != null) {
            buf.append(this.tail(bodyFormat, ""));
        } else {
            buf = new StringBuilder(0);
        }
        this.appendSubject(msg, this.tail(this.subjectFormatter, ""));
        MimeMultipart multipart = new MimeMultipart();
        String altType = this.getContentType(bodyFormat.getClass().getName());
        this.setContent(body, buf, altType == null ? contentType : altType);
        multipart.addBodyPart(body);
        for (int i = 0; i < parts.length; ++i) {
            if (parts[i] == null) continue;
            multipart.addBodyPart(parts[i]);
        }
        msg.setContent(multipart);
        return msg;
    }

    private void verifySettings(Session session) {
        try {
            if (session != null) {
                Properties props = session.getProperties();
                Object check = props.put("verify", "");
                if (check instanceof String) {
                    String value = (String)check;
                    if (MailHandler.hasValue(value)) {
                        this.verifySettings0(session, value);
                    }
                } else if (check != null) {
                    this.verifySettings0(session, check.getClass().toString());
                }
            }
        }
        catch (LinkageError JDK8152515) {
            this.reportLinkageError(JDK8152515, 4);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    private void verifySettings0(Session session, String verify) {
        String[] atn;
        Object UEE;
        String msg;
        assert (verify != null) : null;
        if (!("local".equals(verify) || "remote".equals(verify) || "limited".equals(verify) || "resolve".equals(verify))) {
            this.reportError("Verify must be 'limited', local', 'resolve' or 'remote'.", (Exception)new IllegalArgumentException(verify), 4);
            return;
        }
        MimeMessage abort = new MimeMessage(session);
        if (!"limited".equals(verify)) {
            msg = "Local address is " + InternetAddress.getLocalAddress(session) + '.';
            try {
                Charset.forName(this.getEncodingName());
            }
            catch (RuntimeException RE) {
                UEE = new UnsupportedEncodingException(RE.toString());
                ((Throwable)UEE).initCause(RE);
                this.reportError(msg, (Exception)UEE, 5);
            }
        } else {
            msg = "Skipping local address check.";
        }
        UEE = this;
        synchronized (UEE) {
            this.appendSubject(abort, this.head(this.subjectFormatter));
            this.appendSubject(abort, this.tail(this.subjectFormatter, ""));
            atn = new String[this.attachmentNames.length];
            for (int i = 0; i < atn.length; ++i) {
                atn[i] = this.head(this.attachmentNames[i]);
                atn[i] = atn[i].length() == 0 ? this.tail(this.attachmentNames[i], "") : atn[i].concat(this.tail(this.attachmentNames[i], ""));
            }
        }
        this.setIncompleteCopy(abort);
        this.envelopeFor(abort, true);
        try {
            abort.saveChanges();
        }
        catch (MessagingException ME) {
            this.reportError(msg, (Exception)ME, 5);
        }
        try {
            MessagingException ME;
            MailHandler mailHandler;
            String local;
            Object ccl;
            Transport t;
            Address[] all;
            block68: {
                block67: {
                    MessagingException closed;
                    block65: {
                        all = abort.getAllRecipients();
                        if (all == null) {
                            all = new InternetAddress[]{};
                        }
                        try {
                            Address[] any;
                            Address[] addressArray = any = all.length != 0 ? all : abort.getFrom();
                            if (any == null || any.length == 0) {
                                MessagingException me = new MessagingException("No recipient or from address.");
                                this.reportError(msg, (Exception)me, 4);
                                throw me;
                            }
                            t = session.getTransport(any[0]);
                            session.getProperty("mail.transport.protocol");
                        }
                        catch (MessagingException protocol) {
                            ccl = this.getAndSetContextClassLoader(MAILHANDLER_LOADER);
                            try {
                                t = session.getTransport();
                                Object var12_21 = null;
                                this.getAndSetContextClassLoader(ccl);
                            }
                            catch (Throwable throwable) {
                                Object var12_22 = null;
                                this.getAndSetContextClassLoader(ccl);
                                throw throwable;
                            }
                            {
                                catch (MessagingException fail) {
                                    throw MailHandler.attach(protocol, fail);
                                }
                            }
                        }
                        local = null;
                        if (!"remote".equals(verify)) break block67;
                        closed = null;
                        t.connect();
                        try {
                            try {
                                local = this.getLocalHost(t);
                                t.sendMessage(abort, all);
                                mailHandler = null;
                            }
                            catch (Throwable throwable) {
                                Object var14_33 = null;
                                try {
                                    t.close();
                                }
                                catch (MessagingException ME2) {
                                    closed = ME2;
                                }
                                throw throwable;
                            }
                            try {
                                t.close();
                            }
                            catch (MessagingException ME2) {
                                closed = ME2;
                            }
                            this.reportUnexpectedSend(abort, verify, null);
                        }
                        catch (SendFailedException sfe) {
                            Address[] recip = sfe.getInvalidAddresses();
                            if (recip != null && recip.length != 0) {
                                this.setErrorContent(abort, verify, sfe);
                                this.reportError(abort, (Exception)sfe, 4);
                            }
                            if ((recip = sfe.getValidSentAddresses()) != null && recip.length != 0) {
                                this.reportUnexpectedSend(abort, verify, sfe);
                            }
                        }
                        catch (MessagingException ME3) {
                            if (this.isMissingContent(abort, ME3)) break block65;
                            this.setErrorContent(abort, verify, ME3);
                            this.reportError(abort, (Exception)ME3, 4);
                        }
                    }
                    if (closed != null) {
                        this.setErrorContent(abort, verify, closed);
                        this.reportError(abort, (Exception)closed, 3);
                    }
                    break block68;
                }
                String protocol = t.getURLName().getProtocol();
                String mailHost = session.getProperty("mail." + protocol + ".host");
                if (MailHandler.isEmpty(mailHost)) {
                    mailHost = session.getProperty("mail.host");
                } else {
                    session.getProperty("mail.host");
                }
                session.getProperty("mail." + protocol + ".port");
                session.getProperty("mail." + protocol + ".user");
                session.getProperty("mail.user");
                session.getProperty("mail." + protocol + ".localport");
                local = session.getProperty("mail." + protocol + ".localhost");
                if (MailHandler.isEmpty(local)) {
                    local = session.getProperty("mail." + protocol + ".localaddress");
                } else {
                    session.getProperty("mail." + protocol + ".localaddress");
                }
                if ("resolve".equals(verify)) {
                    try {
                        String transportHost = t.getURLName().getHost();
                        if (!MailHandler.isEmpty(transportHost)) {
                            MailHandler.verifyHost(transportHost);
                            if (!transportHost.equalsIgnoreCase(mailHost)) {
                                MailHandler.verifyHost(mailHost);
                            }
                        } else {
                            MailHandler.verifyHost(mailHost);
                        }
                    }
                    catch (IOException IOE) {
                        ME = new MessagingException(msg, IOE);
                        this.setErrorContent(abort, verify, ME);
                        this.reportError(abort, (Exception)ME, 4);
                    }
                    catch (RuntimeException RE) {
                        ME = new MessagingException(msg, RE);
                        this.setErrorContent(abort, verify, RE);
                        this.reportError(abort, (Exception)ME, 4);
                    }
                }
            }
            if (!"limited".equals(verify)) {
                MessagingException ME4;
                try {
                    if (!"remote".equals(verify)) {
                        local = this.getLocalHost(t);
                    }
                    MailHandler.verifyHost(local);
                }
                catch (IOException IOE) {
                    ME4 = new MessagingException(msg, IOE);
                    this.setErrorContent(abort, verify, ME4);
                    this.reportError(abort, (Exception)ME4, 4);
                }
                catch (RuntimeException RE) {
                    ME4 = new MessagingException(msg, RE);
                    this.setErrorContent(abort, verify, ME4);
                    this.reportError(abort, (Exception)ME4, 4);
                }
                try {
                    ccl = this.getAndSetContextClassLoader(MAILHANDLER_LOADER);
                    try {
                        MimeBodyPart body;
                        String bodyContentType;
                        MimeMultipart multipart = new MimeMultipart();
                        MimeBodyPart[] ambp = new MimeBodyPart[atn.length];
                        mailHandler = this;
                        synchronized (mailHandler) {
                            bodyContentType = this.contentTypeOf(this.getFormatter());
                            body = this.createBodyPart();
                            for (int i = 0; i < atn.length; ++i) {
                                ambp[i] = this.createBodyPart(i);
                                ambp[i].setFileName(atn[i]);
                                atn[i] = this.getContentType(atn[i]);
                            }
                        }
                        body.setDescription(verify);
                        this.setContent(body, "", bodyContentType);
                        multipart.addBodyPart(body);
                        for (int i = 0; i < ambp.length; ++i) {
                            ambp[i].setDescription(verify);
                            this.setContent(ambp[i], "", atn[i]);
                        }
                        abort.setContent(multipart);
                        abort.saveChanges();
                        abort.writeTo(new ByteArrayOutputStream(1024));
                        Object var18_41 = null;
                        this.getAndSetContextClassLoader(ccl);
                    }
                    catch (Throwable throwable) {
                        Object var18_42 = null;
                        this.getAndSetContextClassLoader(ccl);
                        throw throwable;
                    }
                    {
                    }
                }
                catch (IOException IOE) {
                    ME4 = new MessagingException(msg, IOE);
                    this.setErrorContent(abort, verify, ME4);
                    this.reportError(abort, (Exception)ME4, 5);
                }
            }
            if (all.length == 0) {
                throw new MessagingException("No recipient addresses.");
            }
            MailHandler.verifyAddresses(all);
            Address[] from = abort.getFrom();
            Address sender = abort.getSender();
            if (sender instanceof InternetAddress) {
                ((InternetAddress)sender).validate();
            }
            if (abort.getHeader("From", ",") != null && from.length != 0) {
                MailHandler.verifyAddresses(from);
                for (int i = 0; i < from.length; ++i) {
                    if (!from[i].equals(sender)) continue;
                    ME = new MessagingException("Sender address '" + sender + "' equals from address.");
                    throw new MessagingException(msg, ME);
                }
            } else if (sender == null) {
                MessagingException ME5 = new MessagingException("No from or sender address.");
                throw new MessagingException(msg, ME5);
            }
            MailHandler.verifyAddresses(abort.getReplyTo());
        }
        catch (RuntimeException RE) {
            this.setErrorContent(abort, verify, RE);
            this.reportError(abort, (Exception)RE, 4);
        }
        catch (Exception ME) {
            this.setErrorContent(abort, verify, ME);
            this.reportError(abort, ME, 4);
        }
    }

    private static InetAddress verifyHost(String host) throws IOException {
        InetAddress a = MailHandler.isEmpty(host) ? InetAddress.getLocalHost() : InetAddress.getByName(host);
        if (a.getCanonicalHostName().length() == 0) {
            throw new UnknownHostException();
        }
        return a;
    }

    private static void verifyAddresses(Address[] all) throws AddressException {
        if (all != null) {
            for (int i = 0; i < all.length; ++i) {
                Address a = all[i];
                if (!(a instanceof InternetAddress)) continue;
                ((InternetAddress)a).validate();
            }
        }
    }

    private void reportUnexpectedSend(MimeMessage msg, String verify, Exception cause) {
        MessagingException write = new MessagingException("An empty message was sent.", cause);
        this.setErrorContent(msg, verify, write);
        this.reportError(msg, (Exception)write, 4);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setErrorContent(MimeMessage msg, String verify, Throwable t) {
        try {
            String subjectType;
            String msgDesc;
            MimeBodyPart body;
            MailHandler mailHandler = this;
            synchronized (mailHandler) {
                body = this.createBodyPart();
                msgDesc = this.descriptionFrom(this.comparator, this.pushLevel, this.pushFilter);
                subjectType = this.getClassId(this.subjectFormatter);
            }
            body.setDescription("Formatted using " + (t == null ? Throwable.class.getName() : t.getClass().getName()) + ", filtered with " + verify + ", and named by " + subjectType + '.');
            this.setContent(body, this.toMsgString(t), "text/plain");
            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(body);
            msg.setContent(multipart);
            msg.setDescription(msgDesc);
            this.setAcceptLang(msg);
            msg.saveChanges();
        }
        catch (MessagingException ME) {
            this.reportError("Unable to create body.", (Exception)ME, 4);
        }
        catch (RuntimeException RE) {
            this.reportError("Unable to create body.", (Exception)RE, 4);
        }
    }

    private Session updateSession() {
        Session settings;
        assert (Thread.holdsLock(this));
        if (this.mailProps.getProperty("verify") != null) {
            settings = this.initSession();
            assert (settings == this.session) : this.session;
        } else {
            this.session = null;
            settings = null;
        }
        return settings;
    }

    private Session initSession() {
        assert (Thread.holdsLock(this));
        String p = this.getClass().getName();
        LogManagerProperties proxy = new LogManagerProperties(this.mailProps, p);
        this.session = Session.getInstance(proxy, this.auth);
        return this.session;
    }

    private void envelopeFor(Message msg, boolean priority) {
        this.setAcceptLang(msg);
        this.setFrom(msg);
        if (!this.setRecipient(msg, "mail.to", Message.RecipientType.TO)) {
            this.setDefaultRecipient(msg, Message.RecipientType.TO);
        }
        this.setRecipient(msg, "mail.cc", Message.RecipientType.CC);
        this.setRecipient(msg, "mail.bcc", Message.RecipientType.BCC);
        this.setReplyTo(msg);
        this.setSender(msg);
        this.setMailer(msg);
        this.setAutoSubmitted(msg);
        if (priority) {
            this.setPriority(msg);
        }
        try {
            msg.setSentDate(new Date());
        }
        catch (MessagingException ME) {
            this.reportError(ME.getMessage(), (Exception)ME, 5);
        }
    }

    private MimeBodyPart createBodyPart() throws MessagingException {
        assert (Thread.holdsLock(this));
        MimeBodyPart part = new MimeBodyPart();
        part.setDisposition("inline");
        part.setDescription(this.descriptionFrom(this.getFormatter(), this.getFilter(), this.subjectFormatter));
        this.setAcceptLang(part);
        return part;
    }

    private MimeBodyPart createBodyPart(int index) throws MessagingException {
        assert (Thread.holdsLock(this));
        MimeBodyPart part = new MimeBodyPart();
        part.setDisposition("attachment");
        part.setDescription(this.descriptionFrom(this.attachmentFormatters[index], this.attachmentFilters[index], this.attachmentNames[index]));
        this.setAcceptLang(part);
        return part;
    }

    private String descriptionFrom(Comparator<?> c, Level l, Filter f) {
        return "Sorted using " + (c == null ? "no comparator" : c.getClass().getName()) + ", pushed when " + l.getName() + ", and " + (f == null ? "no push filter" : f.getClass().getName()) + '.';
    }

    private String descriptionFrom(Formatter f, Filter filter, Formatter name) {
        return "Formatted using " + this.getClassId(f) + ", filtered with " + (filter == null ? "no filter" : filter.getClass().getName()) + ", and named by " + this.getClassId(name) + '.';
    }

    private String getClassId(Formatter f) {
        if (f instanceof TailNameFormatter) {
            return String.class.getName();
        }
        return f.getClass().getName();
    }

    private String toString(Formatter f) {
        String name = f.toString();
        if (!MailHandler.isEmpty(name)) {
            return name;
        }
        return this.getClassId(f);
    }

    private void appendFileName(Part part, String chunk) {
        if (chunk != null) {
            if (chunk.length() > 0) {
                this.appendFileName0(part, chunk);
            }
        } else {
            this.reportNullError(5);
        }
    }

    private void appendFileName0(Part part, String chunk) {
        try {
            chunk = chunk.replaceAll("[\\x00-\\x1F\\x7F]+", "");
            String old = part.getFileName();
            part.setFileName(old != null ? old.concat(chunk) : chunk);
        }
        catch (MessagingException ME) {
            this.reportError(ME.getMessage(), (Exception)ME, 5);
        }
    }

    private void appendSubject(Message msg, String chunk) {
        if (chunk != null) {
            if (chunk.length() > 0) {
                this.appendSubject0(msg, chunk);
            }
        } else {
            this.reportNullError(5);
        }
    }

    private void appendSubject0(Message msg, String chunk) {
        try {
            chunk = chunk.replaceAll("[\\x00-\\x1F\\x7F]+", "");
            String charset = this.getEncodingName();
            String old = msg.getSubject();
            assert (msg instanceof MimeMessage) : msg;
            ((MimeMessage)msg).setSubject(old != null ? old.concat(chunk) : chunk, MimeUtility.mimeCharset(charset));
        }
        catch (MessagingException ME) {
            this.reportError(ME.getMessage(), (Exception)ME, 5);
        }
    }

    private Locale localeFor(LogRecord r) {
        Locale l;
        ResourceBundle rb = r.getResourceBundle();
        if (rb != null) {
            l = rb.getLocale();
            if (l == null || MailHandler.isEmpty(l.getLanguage())) {
                l = Locale.getDefault();
            }
        } else {
            l = null;
        }
        return l;
    }

    private void appendContentLang(MimePart p, Locale l) {
        try {
            String lang = LogManagerProperties.toLanguageTag(l);
            if (lang.length() != 0) {
                String header = p.getHeader("Content-Language", null);
                if (MailHandler.isEmpty(header)) {
                    p.setHeader("Content-Language", lang);
                } else if (!header.equalsIgnoreCase(lang)) {
                    lang = ",".concat(lang);
                    int idx = 0;
                    while ((idx = header.indexOf(lang, idx)) > -1 && (idx += lang.length()) != header.length() && header.charAt(idx) != ',') {
                    }
                    if (idx < 0) {
                        int len = header.lastIndexOf("\r\n\t");
                        len = len < 0 ? 20 + header.length() : header.length() - len + 8;
                        header = len + lang.length() > 76 ? header.concat("\r\n\t".concat(lang)) : header.concat(lang);
                        p.setHeader("Content-Language", header);
                    }
                }
            }
        }
        catch (MessagingException ME) {
            this.reportError(ME.getMessage(), (Exception)ME, 5);
        }
    }

    private void setAcceptLang(Part p) {
        try {
            String lang = LogManagerProperties.toLanguageTag(Locale.getDefault());
            if (lang.length() != 0) {
                p.setHeader("Accept-Language", lang);
            }
        }
        catch (MessagingException ME) {
            this.reportError(ME.getMessage(), (Exception)ME, 5);
        }
    }

    private void reportFilterError(LogRecord record) {
        assert (Thread.holdsLock(this));
        Formatter f = MailHandler.createSimpleFormatter();
        String msg = "Log record " + record.getSequenceNumber() + " was filtered from all message parts.  " + this.head(f) + this.format(f, record) + this.tail(f, "");
        String txt = this.getFilter() + ", " + Arrays.asList(this.readOnlyAttachmentFilters());
        this.reportError(msg, (Exception)new IllegalArgumentException(txt), 5);
    }

    private void reportNonSymmetric(Object o, Object found) {
        this.reportError("Non symmetric equals implementation.", (Exception)new IllegalArgumentException(o.getClass().getName() + " is not equal to " + found.getClass().getName()), 4);
    }

    private void reportNonDiscriminating(Object o, Object found) {
        this.reportError("Non discriminating equals implementation.", (Exception)new IllegalArgumentException(o.getClass().getName() + " should not be equal to " + found.getClass().getName()), 4);
    }

    private void reportNullError(int code) {
        this.reportError("null", (Exception)new NullPointerException(), code);
    }

    private String head(Formatter f) {
        try {
            return f.getHead(this);
        }
        catch (RuntimeException RE) {
            this.reportError(RE.getMessage(), (Exception)RE, 5);
            return "";
        }
    }

    private String format(Formatter f, LogRecord r) {
        try {
            return f.format(r);
        }
        catch (RuntimeException RE) {
            this.reportError(RE.getMessage(), (Exception)RE, 5);
            return "";
        }
    }

    private String tail(Formatter f, String def) {
        try {
            return f.getTail(this);
        }
        catch (RuntimeException RE) {
            this.reportError(RE.getMessage(), (Exception)RE, 5);
            return def;
        }
    }

    private void setMailer(Message msg) {
        try {
            String value;
            Class<MailHandler> mail = MailHandler.class;
            Class<?> k = this.getClass();
            if (k == mail) {
                value = mail.getName();
            } else {
                try {
                    value = MimeUtility.encodeText(k.getName());
                }
                catch (UnsupportedEncodingException E) {
                    this.reportError(E.getMessage(), (Exception)E, 5);
                    value = k.getName().replaceAll("[^\\x00-\\x7F]", "\u001a");
                }
                value = MimeUtility.fold(10, mail.getName() + " using the " + value + " extension.");
            }
            msg.setHeader("X-Mailer", value);
        }
        catch (MessagingException ME) {
            this.reportError(ME.getMessage(), (Exception)ME, 5);
        }
    }

    private void setPriority(Message msg) {
        try {
            msg.setHeader("Importance", "High");
            msg.setHeader("Priority", "urgent");
            msg.setHeader("X-Priority", "2");
        }
        catch (MessagingException ME) {
            this.reportError(ME.getMessage(), (Exception)ME, 5);
        }
    }

    private void setIncompleteCopy(Message msg) {
        try {
            msg.setHeader("Incomplete-Copy", "");
        }
        catch (MessagingException ME) {
            this.reportError(ME.getMessage(), (Exception)ME, 5);
        }
    }

    private void setAutoSubmitted(Message msg) {
        if (this.allowRestrictedHeaders()) {
            try {
                msg.setHeader("auto-submitted", "auto-generated");
            }
            catch (MessagingException ME) {
                this.reportError(ME.getMessage(), (Exception)ME, 5);
            }
        }
    }

    private void setFrom(Message msg) {
        block5: {
            String from = this.getSession(msg).getProperty("mail.from");
            if (from != null) {
                try {
                    Address[] address = InternetAddress.parse(from, false);
                    if (address.length <= 0) break block5;
                    if (address.length == 1) {
                        msg.setFrom(address[0]);
                        break block5;
                    }
                    msg.addFrom(address);
                }
                catch (MessagingException ME) {
                    this.reportError(ME.getMessage(), (Exception)ME, 5);
                    this.setDefaultFrom(msg);
                }
            } else {
                this.setDefaultFrom(msg);
            }
        }
    }

    private void setDefaultFrom(Message msg) {
        try {
            msg.setFrom();
        }
        catch (MessagingException ME) {
            this.reportError(ME.getMessage(), (Exception)ME, 5);
        }
    }

    private void setDefaultRecipient(Message msg, Message.RecipientType type) {
        block5: {
            try {
                InternetAddress a = InternetAddress.getLocalAddress(this.getSession(msg));
                if (a != null) {
                    msg.setRecipient(type, a);
                    break block5;
                }
                MimeMessage m = new MimeMessage(this.getSession(msg));
                m.setFrom();
                Address[] from = m.getFrom();
                if (from.length > 0) {
                    msg.setRecipients(type, from);
                    break block5;
                }
                throw new MessagingException("No local address.");
            }
            catch (MessagingException ME) {
                this.reportError("Unable to compute a default recipient.", (Exception)ME, 5);
            }
            catch (RuntimeException RE) {
                this.reportError("Unable to compute a default recipient.", (Exception)RE, 5);
            }
        }
    }

    private void setReplyTo(Message msg) {
        String reply = this.getSession(msg).getProperty("mail.reply.to");
        if (!MailHandler.isEmpty(reply)) {
            try {
                Address[] address = InternetAddress.parse(reply, false);
                if (address.length > 0) {
                    msg.setReplyTo(address);
                }
            }
            catch (MessagingException ME) {
                this.reportError(ME.getMessage(), (Exception)ME, 5);
            }
        }
    }

    private void setSender(Message msg) {
        assert (msg instanceof MimeMessage) : msg;
        String sender = this.getSession(msg).getProperty("mail.sender");
        if (!MailHandler.isEmpty(sender)) {
            try {
                Address[] address = InternetAddress.parse(sender, false);
                if (address.length > 0) {
                    ((MimeMessage)msg).setSender(address[0]);
                    if (address.length > 1) {
                        this.reportError("Ignoring other senders.", (Exception)this.tooManyAddresses(address, 1), 5);
                    }
                }
            }
            catch (MessagingException ME) {
                this.reportError(ME.getMessage(), (Exception)ME, 5);
            }
        }
    }

    private AddressException tooManyAddresses(Address[] address, int offset) {
        List<Address> l = Arrays.asList(address).subList(offset, address.length);
        return new AddressException(l.toString());
    }

    private boolean setRecipient(Message msg, String key, Message.RecipientType type) {
        boolean containsKey;
        String value = this.getSession(msg).getProperty(key);
        boolean bl = containsKey = value != null;
        if (!MailHandler.isEmpty(value)) {
            try {
                Address[] address = InternetAddress.parse(value, false);
                if (address.length > 0) {
                    msg.setRecipients(type, address);
                }
            }
            catch (MessagingException ME) {
                this.reportError(ME.getMessage(), (Exception)ME, 5);
            }
        }
        return containsKey;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String toRawString(Message msg) throws MessagingException, IOException {
        if (msg != null) {
            String string;
            Object ccl = this.getAndSetContextClassLoader(MAILHANDLER_LOADER);
            try {
                int nbytes = Math.max(msg.getSize() + 1024, 1024);
                ByteArrayOutputStream out = new ByteArrayOutputStream(nbytes);
                msg.writeTo(out);
                string = out.toString("US-ASCII");
                Object var7_6 = null;
                this.getAndSetContextClassLoader(ccl);
            }
            catch (Throwable throwable) {
                Object var7_7 = null;
                this.getAndSetContextClassLoader(ccl);
                throw throwable;
            }
            return string;
        }
        return null;
    }

    private String toMsgString(Throwable t) {
        if (t == null) {
            return "null";
        }
        String charset = this.getEncodingName();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter((OutputStream)out, charset));
            pw.println(t.getMessage());
            t.printStackTrace(pw);
            pw.flush();
            pw.close();
            return out.toString(charset);
        }
        catch (RuntimeException unexpected) {
            return t.toString() + ' ' + unexpected.toString();
        }
        catch (Exception badMimeCharset) {
            return t.toString() + ' ' + badMimeCharset.toString();
        }
    }

    private Object getAndSetContextClassLoader(Object ccl) {
        if (ccl != GetAndSetContext.NOT_MODIFIED) {
            try {
                PrivilegedAction pa = ccl instanceof PrivilegedAction ? (PrivilegedAction)ccl : new GetAndSetContext(ccl);
                return AccessController.doPrivileged(pa);
            }
            catch (SecurityException securityException) {
                // empty catch block
            }
        }
        return GetAndSetContext.NOT_MODIFIED;
    }

    private static RuntimeException attachmentMismatch(String msg) {
        return new IndexOutOfBoundsException(msg);
    }

    private static RuntimeException attachmentMismatch(int expected, int found) {
        return MailHandler.attachmentMismatch("Attachments mismatched, expected " + expected + " but given " + found + '.');
    }

    private static MessagingException attach(MessagingException required, Exception optional) {
        MessagingException head;
        if (optional != null && !required.setNextException(optional) && optional instanceof MessagingException && (head = (MessagingException)optional).setNextException(required)) {
            return head;
        }
        return required;
    }

    private String getLocalHost(Service s) {
        try {
            return LogManagerProperties.getLocalHost(s);
        }
        catch (SecurityException securityException) {
        }
        catch (NoSuchMethodException noSuchMethodException) {
        }
        catch (LinkageError linkageError) {
        }
        catch (Exception ex) {
            this.reportError(s.toString(), ex, 4);
        }
        return null;
    }

    private Session getSession(Message msg) {
        if (msg == null) {
            throw new NullPointerException();
        }
        return new MessageContext(msg).getSession();
    }

    private boolean allowRestrictedHeaders() {
        return LogManagerProperties.hasLogManager();
    }

    private static String atIndexMsg(int i) {
        return "At index: " + i + '.';
    }

    private static final class TailNameFormatter
    extends Formatter {
        private final String name;

        static Formatter of(String name) {
            return new TailNameFormatter(name);
        }

        private TailNameFormatter(String name) {
            assert (name != null);
            this.name = name;
        }

        public final String format(LogRecord record) {
            return "";
        }

        public final String getTail(Handler h) {
            return this.name;
        }

        public final boolean equals(Object o) {
            if (o instanceof TailNameFormatter) {
                return this.name.equals(((TailNameFormatter)o).name);
            }
            return false;
        }

        public final int hashCode() {
            return this.getClass().hashCode() + this.name.hashCode();
        }

        public final String toString() {
            return this.name;
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    private static final class GetAndSetContext
    implements PrivilegedAction<Object> {
        public static final Object NOT_MODIFIED = GetAndSetContext.class;
        private final Object source;

        GetAndSetContext(Object source) {
            this.source = source;
        }

        @Override
        public final Object run() {
            ClassLoader loader;
            Thread current = Thread.currentThread();
            ClassLoader ccl = current.getContextClassLoader();
            if (this.source == null) {
                loader = null;
            } else if (this.source instanceof ClassLoader) {
                loader = (ClassLoader)this.source;
            } else if (this.source instanceof Class) {
                loader = ((Class)this.source).getClassLoader();
            } else if (this.source instanceof Thread) {
                loader = ((Thread)this.source).getContextClassLoader();
            } else {
                assert (!(this.source instanceof Class)) : this.source;
                loader = this.source.getClass().getClassLoader();
            }
            if (ccl != loader) {
                current.setContextClassLoader(loader);
                return ccl;
            }
            return NOT_MODIFIED;
        }
    }

    private static final class DefaultAuthenticator
    extends Authenticator {
        private final String pass;

        static Authenticator of(String pass) {
            return new DefaultAuthenticator(pass);
        }

        private DefaultAuthenticator(String pass) {
            assert (pass != null);
            this.pass = pass;
        }

        protected final PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(this.getDefaultUserName(), this.pass);
        }
    }
}

