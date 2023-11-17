/*
 * Decompiled with CFR 0.152.
 */
package javax.mail;

import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.MailLogger;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.EventQueue;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Provider;
import javax.mail.Service;
import javax.mail.Store;
import javax.mail.StreamLoader;
import javax.mail.Transport;
import javax.mail.URLName;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class Session {
    private final Properties props;
    private final Authenticator authenticator;
    private final Hashtable<URLName, PasswordAuthentication> authTable = new Hashtable();
    private boolean debug = false;
    private PrintStream out;
    private MailLogger logger;
    private final Vector<Provider> providers = new Vector();
    private final Hashtable<String, Provider> providersByProtocol = new Hashtable();
    private final Hashtable<String, Provider> providersByClassName = new Hashtable();
    private final Properties addressMap = new Properties();
    private final EventQueue q;
    private static Session defaultSession = null;

    private Session(Properties props, Authenticator authenticator) {
        this.props = props;
        this.authenticator = authenticator;
        if (Boolean.valueOf(props.getProperty("mail.debug")).booleanValue()) {
            this.debug = true;
        }
        this.initLogger();
        this.logger.log(Level.CONFIG, "JavaMail version {0}", "1.5.6");
        Class<?> cl = authenticator != null ? authenticator.getClass() : this.getClass();
        this.loadProviders(cl);
        this.loadAddressMap(cl);
        this.q = new EventQueue((Executor)props.get("mail.event.executor"));
    }

    private final synchronized void initLogger() {
        this.logger = new MailLogger(this.getClass(), "DEBUG", this.debug, this.getDebugOut());
    }

    public static Session getInstance(Properties props, Authenticator authenticator) {
        return new Session(props, authenticator);
    }

    public static Session getInstance(Properties props) {
        return new Session(props, null);
    }

    public static synchronized Session getDefaultInstance(Properties props, Authenticator authenticator) {
        if (defaultSession == null) {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkSetFactory();
            }
            defaultSession = new Session(props, authenticator);
        } else if (Session.defaultSession.authenticator != authenticator && (Session.defaultSession.authenticator == null || authenticator == null || Session.defaultSession.authenticator.getClass().getClassLoader() != authenticator.getClass().getClassLoader())) {
            throw new SecurityException("Access to default session denied");
        }
        return defaultSession;
    }

    public static Session getDefaultInstance(Properties props) {
        return Session.getDefaultInstance(props, null);
    }

    public synchronized void setDebug(boolean debug) {
        this.debug = debug;
        this.initLogger();
        this.logger.log(Level.CONFIG, "setDebug: JavaMail version {0}", "1.5.6");
    }

    public synchronized boolean getDebug() {
        return this.debug;
    }

    public synchronized void setDebugOut(PrintStream out) {
        this.out = out;
        this.initLogger();
    }

    public synchronized PrintStream getDebugOut() {
        if (this.out == null) {
            return System.out;
        }
        return this.out;
    }

    public synchronized Provider[] getProviders() {
        Object[] _providers = new Provider[this.providers.size()];
        this.providers.copyInto(_providers);
        return _providers;
    }

    public synchronized Provider getProvider(String protocol) throws NoSuchProviderException {
        if (protocol == null || protocol.length() <= 0) {
            throw new NoSuchProviderException("Invalid protocol: null");
        }
        Provider _provider = null;
        String _className = this.props.getProperty("mail." + protocol + ".class");
        if (_className != null) {
            if (this.logger.isLoggable(Level.FINE)) {
                this.logger.fine("mail." + protocol + ".class property exists and points to " + _className);
            }
            _provider = this.providersByClassName.get(_className);
        }
        if (_provider != null) {
            return _provider;
        }
        _provider = this.providersByProtocol.get(protocol);
        if (_provider == null) {
            throw new NoSuchProviderException("No provider for " + protocol);
        }
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine("getProvider() returning " + _provider.toString());
        }
        return _provider;
    }

    public synchronized void setProvider(Provider provider) throws NoSuchProviderException {
        if (provider == null) {
            throw new NoSuchProviderException("Can't set null provider");
        }
        this.providersByProtocol.put(provider.getProtocol(), provider);
        this.props.put("mail." + provider.getProtocol() + ".class", provider.getClassName());
    }

    public Store getStore() throws NoSuchProviderException {
        return this.getStore(this.getProperty("mail.store.protocol"));
    }

    public Store getStore(String protocol) throws NoSuchProviderException {
        return this.getStore(new URLName(protocol, null, -1, null, null, null));
    }

    public Store getStore(URLName url) throws NoSuchProviderException {
        String protocol = url.getProtocol();
        Provider p = this.getProvider(protocol);
        return this.getStore(p, url);
    }

    public Store getStore(Provider provider) throws NoSuchProviderException {
        return this.getStore(provider, null);
    }

    private Store getStore(Provider provider, URLName url) throws NoSuchProviderException {
        if (provider == null || provider.getType() != Provider.Type.STORE) {
            throw new NoSuchProviderException("invalid provider");
        }
        return this.getService(provider, url, Store.class);
    }

    public Folder getFolder(URLName url) throws MessagingException {
        Store store = this.getStore(url);
        store.connect();
        return store.getFolder(url);
    }

    public Transport getTransport() throws NoSuchProviderException {
        String prot = this.getProperty("mail.transport.protocol");
        if (prot != null) {
            return this.getTransport(prot);
        }
        prot = (String)this.addressMap.get("rfc822");
        if (prot != null) {
            return this.getTransport(prot);
        }
        return this.getTransport("smtp");
    }

    public Transport getTransport(String protocol) throws NoSuchProviderException {
        return this.getTransport(new URLName(protocol, null, -1, null, null, null));
    }

    public Transport getTransport(URLName url) throws NoSuchProviderException {
        String protocol = url.getProtocol();
        Provider p = this.getProvider(protocol);
        return this.getTransport(p, url);
    }

    public Transport getTransport(Provider provider) throws NoSuchProviderException {
        return this.getTransport(provider, null);
    }

    public Transport getTransport(Address address) throws NoSuchProviderException {
        String transportProtocol = this.getProperty("mail.transport.protocol." + address.getType());
        if (transportProtocol != null) {
            return this.getTransport(transportProtocol);
        }
        transportProtocol = (String)this.addressMap.get(address.getType());
        if (transportProtocol != null) {
            return this.getTransport(transportProtocol);
        }
        throw new NoSuchProviderException("No provider for Address type: " + address.getType());
    }

    private Transport getTransport(Provider provider, URLName url) throws NoSuchProviderException {
        if (provider == null || provider.getType() != Provider.Type.TRANSPORT) {
            throw new NoSuchProviderException("invalid provider");
        }
        return this.getService(provider, url, Transport.class);
    }

    private <T extends Service> T getService(Provider provider, URLName url, Class<T> type) throws NoSuchProviderException {
        if (provider == null) {
            throw new NoSuchProviderException("null");
        }
        if (url == null) {
            url = new URLName(provider.getProtocol(), null, -1, null, null, null);
        }
        Object service = null;
        ClassLoader cl = this.authenticator != null ? this.authenticator.getClass().getClassLoader() : this.getClass().getClassLoader();
        Class<?> serviceClass = null;
        try {
            ClassLoader ccl = Session.getContextClassLoader();
            if (ccl != null) {
                try {
                    serviceClass = Class.forName(provider.getClassName(), false, ccl);
                }
                catch (ClassNotFoundException classNotFoundException) {
                    // empty catch block
                }
            }
            if (serviceClass == null || !type.isAssignableFrom(serviceClass)) {
                serviceClass = Class.forName(provider.getClassName(), false, cl);
            }
            if (!type.isAssignableFrom(serviceClass)) {
                throw new ClassCastException(type.getName() + " " + serviceClass.getName());
            }
        }
        catch (Exception ex1) {
            try {
                serviceClass = Class.forName(provider.getClassName());
                if (!type.isAssignableFrom(serviceClass)) {
                    throw new ClassCastException(type.getName() + " " + serviceClass.getName());
                }
            }
            catch (Exception ex) {
                this.logger.log(Level.FINE, "Exception loading provider", ex);
                throw new NoSuchProviderException(provider.getProtocol());
            }
        }
        try {
            Class[] c = new Class[]{Session.class, URLName.class};
            Constructor<?> cons = serviceClass.getConstructor(c);
            Object[] o = new Object[]{this, url};
            service = cons.newInstance(o);
        }
        catch (Exception ex) {
            this.logger.log(Level.FINE, "Exception loading provider", ex);
            throw new NoSuchProviderException(provider.getProtocol());
        }
        return (T)((Service)type.cast(service));
    }

    public void setPasswordAuthentication(URLName url, PasswordAuthentication pw) {
        if (pw == null) {
            this.authTable.remove(url);
        } else {
            this.authTable.put(url, pw);
        }
    }

    public PasswordAuthentication getPasswordAuthentication(URLName url) {
        return this.authTable.get(url);
    }

    public PasswordAuthentication requestPasswordAuthentication(InetAddress addr, int port, String protocol, String prompt, String defaultUserName) {
        if (this.authenticator != null) {
            return this.authenticator.requestPasswordAuthentication(addr, port, protocol, prompt, defaultUserName);
        }
        return null;
    }

    public Properties getProperties() {
        return this.props;
    }

    public String getProperty(String name) {
        return this.props.getProperty(name);
    }

    private void loadProviders(Class<?> cl) {
        StreamLoader loader = new StreamLoader(){

            public void load(InputStream is) throws IOException {
                Session.this.loadProvidersFromStream(is);
            }
        };
        try {
            String res = System.getProperty("java.home") + File.separator + "lib" + File.separator + "javamail.providers";
            this.loadFile(res, loader);
        }
        catch (SecurityException sex) {
            this.logger.log(Level.CONFIG, "can't get java.home", sex);
        }
        this.loadAllResources("META-INF/javamail.providers", cl, loader);
        this.loadResource("/META-INF/javamail.default.providers", cl, loader);
        if (this.providers.size() == 0) {
            this.logger.config("failed to load any providers, using defaults");
            this.addProvider(new Provider(Provider.Type.STORE, "imap", "com.sun.mail.imap.IMAPStore", "Oracle", "1.5.6"));
            this.addProvider(new Provider(Provider.Type.STORE, "imaps", "com.sun.mail.imap.IMAPSSLStore", "Oracle", "1.5.6"));
            this.addProvider(new Provider(Provider.Type.STORE, "pop3", "com.sun.mail.pop3.POP3Store", "Oracle", "1.5.6"));
            this.addProvider(new Provider(Provider.Type.STORE, "pop3s", "com.sun.mail.pop3.POP3SSLStore", "Oracle", "1.5.6"));
            this.addProvider(new Provider(Provider.Type.TRANSPORT, "smtp", "com.sun.mail.smtp.SMTPTransport", "Oracle", "1.5.6"));
            this.addProvider(new Provider(Provider.Type.TRANSPORT, "smtps", "com.sun.mail.smtp.SMTPSSLTransport", "Oracle", "1.5.6"));
        }
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("Tables of loaded providers");
            this.logger.config("Providers Listed By Class Name: " + this.providersByClassName.toString());
            this.logger.config("Providers Listed By Protocol: " + this.providersByProtocol.toString());
        }
    }

    private void loadProvidersFromStream(InputStream is) throws IOException {
        if (is != null) {
            String currLine;
            LineInputStream lis = new LineInputStream(is);
            while ((currLine = lis.readLine()) != null) {
                if (currLine.startsWith("#")) continue;
                Provider.Type type = null;
                String protocol = null;
                String className = null;
                String vendor = null;
                String version = null;
                StringTokenizer tuples = new StringTokenizer(currLine, ";");
                while (tuples.hasMoreTokens()) {
                    String currTuple = tuples.nextToken().trim();
                    int sep = currTuple.indexOf("=");
                    if (currTuple.startsWith("protocol=")) {
                        protocol = currTuple.substring(sep + 1);
                        continue;
                    }
                    if (currTuple.startsWith("type=")) {
                        String strType = currTuple.substring(sep + 1);
                        if (strType.equalsIgnoreCase("store")) {
                            type = Provider.Type.STORE;
                            continue;
                        }
                        if (!strType.equalsIgnoreCase("transport")) continue;
                        type = Provider.Type.TRANSPORT;
                        continue;
                    }
                    if (currTuple.startsWith("class=")) {
                        className = currTuple.substring(sep + 1);
                        continue;
                    }
                    if (currTuple.startsWith("vendor=")) {
                        vendor = currTuple.substring(sep + 1);
                        continue;
                    }
                    if (!currTuple.startsWith("version=")) continue;
                    version = currTuple.substring(sep + 1);
                }
                if (type == null || protocol == null || className == null || protocol.length() <= 0 || className.length() <= 0) {
                    this.logger.log(Level.CONFIG, "Bad provider entry: {0}", currLine);
                    continue;
                }
                Provider provider = new Provider(type, protocol, className, vendor, version);
                this.addProvider(provider);
            }
        }
    }

    public synchronized void addProvider(Provider provider) {
        this.providers.addElement(provider);
        this.providersByClassName.put(provider.getClassName(), provider);
        if (!this.providersByProtocol.containsKey(provider.getProtocol())) {
            this.providersByProtocol.put(provider.getProtocol(), provider);
        }
    }

    private void loadAddressMap(Class<?> cl) {
        StreamLoader loader = new StreamLoader(){

            public void load(InputStream is) throws IOException {
                Session.this.addressMap.load(is);
            }
        };
        this.loadResource("/META-INF/javamail.default.address.map", cl, loader);
        this.loadAllResources("META-INF/javamail.address.map", cl, loader);
        try {
            String res = System.getProperty("java.home") + File.separator + "lib" + File.separator + "javamail.address.map";
            this.loadFile(res, loader);
        }
        catch (SecurityException sex) {
            this.logger.log(Level.CONFIG, "can't get java.home", sex);
        }
        if (this.addressMap.isEmpty()) {
            this.logger.config("failed to load address map, using defaults");
            this.addressMap.put("rfc822", "smtp");
        }
    }

    public synchronized void setProtocolForAddress(String addresstype, String protocol) {
        if (protocol == null) {
            this.addressMap.remove(addresstype);
        } else {
            this.addressMap.put(addresstype, protocol);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void loadFile(String name, StreamLoader loader) {
        InputStream clis = null;
        try {
            clis = new BufferedInputStream(new FileInputStream(name));
            loader.load(clis);
            this.logger.log(Level.CONFIG, "successfully loaded file: {0}", name);
        }
        catch (FileNotFoundException fileNotFoundException) {
        }
        catch (IOException e) {
            if (this.logger.isLoggable(Level.CONFIG)) {
                this.logger.log(Level.CONFIG, "not loading file: " + name, e);
            }
        }
        catch (SecurityException sex) {
            if (this.logger.isLoggable(Level.CONFIG)) {
                this.logger.log(Level.CONFIG, "not loading file: " + name, sex);
            }
        }
        finally {
            try {
                if (clis != null) {
                    clis.close();
                }
            }
            catch (IOException e) {}
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void loadResource(String name, Class<?> cl, StreamLoader loader) {
        InputStream clis = null;
        try {
            clis = Session.getResourceAsStream(cl, name);
            if (clis != null) {
                loader.load(clis);
                this.logger.log(Level.CONFIG, "successfully loaded resource: {0}", name);
            }
        }
        catch (IOException e) {
            this.logger.log(Level.CONFIG, "Exception loading resource", e);
        }
        catch (SecurityException sex) {
            this.logger.log(Level.CONFIG, "Exception loading resource", sex);
        }
        finally {
            try {
                if (clis != null) {
                    clis.close();
                }
            }
            catch (IOException e) {}
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void loadAllResources(String name, Class<?> cl, StreamLoader loader) {
        boolean anyLoaded;
        block26: {
            anyLoaded = false;
            try {
                URL[] urls;
                ClassLoader cld = null;
                cld = Session.getContextClassLoader();
                if (cld == null) {
                    cld = cl.getClassLoader();
                }
                if ((urls = cld != null ? Session.getResources(cld, name) : Session.getSystemResources(name)) == null) break block26;
                for (int i = 0; i < urls.length; ++i) {
                    URL url = urls[i];
                    InputStream clis = null;
                    this.logger.log(Level.CONFIG, "URL {0}", url);
                    try {
                        clis = Session.openStream(url);
                        if (clis != null) {
                            loader.load(clis);
                            anyLoaded = true;
                            this.logger.log(Level.CONFIG, "successfully loaded resource: {0}", url);
                            continue;
                        }
                        this.logger.log(Level.CONFIG, "not loading resource: {0}", url);
                        continue;
                    }
                    catch (FileNotFoundException fileNotFoundException) {
                        continue;
                    }
                    catch (IOException ioex) {
                        this.logger.log(Level.CONFIG, "Exception loading resource", ioex);
                        continue;
                    }
                    catch (SecurityException sex) {
                        this.logger.log(Level.CONFIG, "Exception loading resource", sex);
                        continue;
                    }
                    finally {
                        try {
                            if (clis != null) {
                                clis.close();
                            }
                        }
                        catch (IOException ioex) {}
                    }
                }
            }
            catch (Exception ex) {
                this.logger.log(Level.CONFIG, "Exception loading resource", ex);
            }
        }
        if (!anyLoaded) {
            this.loadResource("/" + name, cl, loader);
        }
    }

    static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>(){

            @Override
            public ClassLoader run() {
                ClassLoader cl = null;
                try {
                    cl = Thread.currentThread().getContextClassLoader();
                }
                catch (SecurityException securityException) {
                    // empty catch block
                }
                return cl;
            }
        });
    }

    private static InputStream getResourceAsStream(final Class<?> c, final String name) throws IOException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>(){

                @Override
                public InputStream run() throws IOException {
                    try {
                        return c.getResourceAsStream(name);
                    }
                    catch (RuntimeException e) {
                        IOException ioex = new IOException("ClassLoader.getResourceAsStream failed");
                        ioex.initCause(e);
                        throw ioex;
                    }
                }
            });
        }
        catch (PrivilegedActionException e) {
            throw (IOException)e.getException();
        }
    }

    private static URL[] getResources(final ClassLoader cl, final String name) {
        return AccessController.doPrivileged(new PrivilegedAction<URL[]>(){

            @Override
            public URL[] run() {
                URL[] ret = null;
                try {
                    ArrayList<URL> v = Collections.list(cl.getResources(name));
                    if (!v.isEmpty()) {
                        ret = new URL[v.size()];
                        v.toArray(ret);
                    }
                }
                catch (IOException iOException) {
                }
                catch (SecurityException securityException) {
                    // empty catch block
                }
                return ret;
            }
        });
    }

    private static URL[] getSystemResources(final String name) {
        return AccessController.doPrivileged(new PrivilegedAction<URL[]>(){

            @Override
            public URL[] run() {
                URL[] ret = null;
                try {
                    ArrayList<URL> v = Collections.list(ClassLoader.getSystemResources(name));
                    if (!v.isEmpty()) {
                        ret = new URL[v.size()];
                        v.toArray(ret);
                    }
                }
                catch (IOException iOException) {
                }
                catch (SecurityException securityException) {
                    // empty catch block
                }
                return ret;
            }
        });
    }

    private static InputStream openStream(final URL url) throws IOException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>(){

                @Override
                public InputStream run() throws IOException {
                    return url.openStream();
                }
            });
        }
        catch (PrivilegedActionException e) {
            throw (IOException)e.getException();
        }
    }

    EventQueue getEventQueue() {
        return this.q;
    }
}

