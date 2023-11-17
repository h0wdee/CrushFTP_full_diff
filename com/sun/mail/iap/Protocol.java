/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.iap;

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.ByteArray;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.LiteralException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseHandler;
import com.sun.mail.iap.ResponseInputStream;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.SocketFetcher;
import com.sun.mail.util.TraceInputStream;
import com.sun.mail.util.TraceOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import javax.net.ssl.SSLSocket;

public class Protocol {
    protected String host;
    private Socket socket;
    protected boolean quote;
    protected MailLogger logger;
    protected MailLogger traceLogger;
    protected Properties props;
    protected String prefix;
    private TraceInputStream traceInput;
    private volatile ResponseInputStream input;
    private TraceOutputStream traceOutput;
    private volatile DataOutputStream output;
    private int tagCounter = 0;
    private String localHostName;
    private final List<ResponseHandler> handlers = new CopyOnWriteArrayList<ResponseHandler>();
    private volatile long timestamp;
    private static final byte[] CRLF = new byte[]{13, 10};

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Protocol(String host, int port, Properties props, String prefix, boolean isSSL, MailLogger logger) throws IOException, ProtocolException {
        boolean connected = false;
        try {
            this.host = host;
            this.props = props;
            this.prefix = prefix;
            this.logger = logger;
            this.traceLogger = logger.getSubLogger("protocol", null);
            this.socket = SocketFetcher.getSocket(host, port, props, prefix, isSSL);
            this.quote = PropUtil.getBooleanProperty(props, "mail.debug.quote", false);
            this.initStreams();
            this.processGreeting(this.readResponse());
            this.timestamp = System.currentTimeMillis();
            connected = true;
            Object var9_8 = null;
            if (!connected) {
                this.disconnect();
            }
        }
        catch (Throwable throwable) {
            Object var9_9 = null;
            if (!connected) {
                this.disconnect();
            }
            throw throwable;
        }
    }

    private void initStreams() throws IOException {
        this.traceInput = new TraceInputStream(this.socket.getInputStream(), this.traceLogger);
        this.traceInput.setQuote(this.quote);
        this.input = new ResponseInputStream(this.traceInput);
        this.traceOutput = new TraceOutputStream(this.socket.getOutputStream(), this.traceLogger);
        this.traceOutput.setQuote(this.quote);
        this.output = new DataOutputStream(new BufferedOutputStream(this.traceOutput));
    }

    public Protocol(InputStream in, PrintStream out, Properties props, boolean debug) throws IOException {
        this.host = "localhost";
        this.props = props;
        this.quote = false;
        this.logger = new MailLogger(this.getClass(), "DEBUG", debug, System.out);
        this.traceLogger = this.logger.getSubLogger("protocol", null);
        this.traceInput = new TraceInputStream(in, this.traceLogger);
        this.traceInput.setQuote(this.quote);
        this.input = new ResponseInputStream(this.traceInput);
        this.traceOutput = new TraceOutputStream((OutputStream)out, this.traceLogger);
        this.traceOutput.setQuote(this.quote);
        this.output = new DataOutputStream(new BufferedOutputStream(this.traceOutput));
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void addResponseHandler(ResponseHandler h) {
        this.handlers.add(h);
    }

    public void removeResponseHandler(ResponseHandler h) {
        this.handlers.remove(h);
    }

    public void notifyResponseHandlers(Response[] responses) {
        if (this.handlers.isEmpty()) {
            return;
        }
        for (Response r : responses) {
            if (r == null) continue;
            for (ResponseHandler rh : this.handlers) {
                if (rh == null) continue;
                rh.handleResponse(r);
            }
        }
    }

    protected void processGreeting(Response r) throws ProtocolException {
        if (r.isBYE()) {
            throw new ConnectionException(this, r);
        }
    }

    protected ResponseInputStream getInputStream() {
        return this.input;
    }

    protected OutputStream getOutputStream() {
        return this.output;
    }

    protected synchronized boolean supportsNonSyncLiterals() {
        return false;
    }

    public Response readResponse() throws IOException, ProtocolException {
        return new Response(this);
    }

    public boolean hasResponse() {
        try {
            return this.input.available() > 0;
        }
        catch (IOException iOException) {
            return false;
        }
    }

    protected ByteArray getResponseBuffer() {
        return null;
    }

    public String writeCommand(String command, Argument args) throws IOException, ProtocolException {
        String tag = "A" + Integer.toString(this.tagCounter++, 10);
        this.output.writeBytes(tag + " " + command);
        if (args != null) {
            this.output.write(32);
            args.write(this);
        }
        this.output.write(CRLF);
        this.output.flush();
        return tag;
    }

    public synchronized Response[] command(String command, Argument args) {
        this.commandStart(command);
        ArrayList<Response> v = new ArrayList<Response>();
        boolean done = false;
        String tag = null;
        Response r = null;
        try {
            tag = this.writeCommand(command, args);
        }
        catch (LiteralException lex) {
            v.add(lex.getResponse());
            done = true;
        }
        catch (Exception ex) {
            v.add(Response.byeResponse(ex));
            done = true;
        }
        Response byeResp = null;
        while (!done) {
            try {
                r = this.readResponse();
            }
            catch (IOException ioex) {
                if (byeResp != null) break;
                r = Response.byeResponse(ioex);
            }
            catch (ProtocolException pex) {
                this.logger.log(Level.FINE, "ignoring bad response", pex);
                continue;
            }
            if (r.isBYE()) {
                byeResp = r;
                continue;
            }
            v.add(r);
            if (!r.isTagged() || !r.getTag().equals(tag)) continue;
            done = true;
        }
        if (byeResp != null) {
            v.add(byeResp);
        }
        Response[] responses = new Response[v.size()];
        v.toArray(responses);
        this.timestamp = System.currentTimeMillis();
        this.commandEnd();
        return responses;
    }

    public void handleResult(Response response) throws ProtocolException {
        if (response.isOK()) {
            return;
        }
        if (response.isNO()) {
            throw new CommandFailedException(response);
        }
        if (response.isBAD()) {
            throw new BadCommandException(response);
        }
        if (response.isBYE()) {
            this.disconnect();
            throw new ConnectionException(this, response);
        }
    }

    public void simpleCommand(String cmd, Argument args) throws ProtocolException {
        Response[] r = this.command(cmd, args);
        this.notifyResponseHandlers(r);
        this.handleResult(r[r.length - 1]);
    }

    public synchronized void startTLS(String cmd) throws IOException, ProtocolException {
        if (this.socket instanceof SSLSocket) {
            return;
        }
        this.simpleCommand(cmd, null);
        this.socket = SocketFetcher.startTLS(this.socket, this.host, this.props, this.prefix);
        this.initStreams();
    }

    public synchronized void startCompression(String cmd) throws IOException, ProtocolException {
        Class<DeflaterOutputStream> dc = DeflaterOutputStream.class;
        Constructor cons = null;
        try {
            cons = dc.getConstructor(OutputStream.class, Deflater.class, Boolean.TYPE);
        }
        catch (NoSuchMethodException ex) {
            this.logger.fine("Ignoring COMPRESS; missing JDK 1.7 DeflaterOutputStream constructor");
            return;
        }
        this.simpleCommand(cmd, null);
        Inflater inf = new Inflater(true);
        this.traceInput = new TraceInputStream((InputStream)new InflaterInputStream(this.socket.getInputStream(), inf), this.traceLogger);
        this.traceInput.setQuote(this.quote);
        this.input = new ResponseInputStream(this.traceInput);
        int level = PropUtil.getIntProperty(this.props, this.prefix + ".compress.level", -1);
        int strategy = PropUtil.getIntProperty(this.props, this.prefix + ".compress.strategy", 0);
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.log(Level.FINE, "Creating Deflater with compression level {0} and strategy {1}", new Object[]{level, strategy});
        }
        Deflater def = new Deflater(-1, true);
        try {
            def.setLevel(level);
        }
        catch (IllegalArgumentException ex) {
            this.logger.log(Level.FINE, "Ignoring bad compression level", ex);
        }
        try {
            def.setStrategy(strategy);
        }
        catch (IllegalArgumentException ex) {
            this.logger.log(Level.FINE, "Ignoring bad compression strategy", ex);
        }
        try {
            this.traceOutput = new TraceOutputStream((OutputStream)cons.newInstance(this.socket.getOutputStream(), def, true), this.traceLogger);
        }
        catch (Exception ex) {
            throw new ProtocolException("can't create deflater", ex);
        }
        this.traceOutput.setQuote(this.quote);
        this.output = new DataOutputStream(new BufferedOutputStream(this.traceOutput));
    }

    public boolean isSSL() {
        return this.socket instanceof SSLSocket;
    }

    public InetAddress getInetAddress() {
        return this.socket.getInetAddress();
    }

    public SocketChannel getChannel() {
        return this.socket.getChannel();
    }

    protected synchronized void disconnect() {
        if (this.socket != null) {
            try {
                this.socket.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            this.socket = null;
        }
    }

    protected synchronized String getLocalHost() {
        InetAddress localHost2;
        if (this.localHostName == null || this.localHostName.length() <= 0) {
            this.localHostName = this.props.getProperty(this.prefix + ".localhost");
        }
        if (this.localHostName == null || this.localHostName.length() <= 0) {
            this.localHostName = this.props.getProperty(this.prefix + ".localaddress");
        }
        try {
            if (this.localHostName == null || this.localHostName.length() <= 0) {
                localHost2 = InetAddress.getLocalHost();
                this.localHostName = localHost2.getCanonicalHostName();
                if (this.localHostName == null) {
                    this.localHostName = "[" + localHost2.getHostAddress() + "]";
                }
            }
        }
        catch (UnknownHostException localHost2) {
            // empty catch block
        }
        if ((this.localHostName == null || this.localHostName.length() <= 0) && this.socket != null && this.socket.isBound()) {
            localHost2 = this.socket.getLocalAddress();
            this.localHostName = localHost2.getCanonicalHostName();
            if (this.localHostName == null) {
                this.localHostName = "[" + localHost2.getHostAddress() + "]";
            }
        }
        return this.localHostName;
    }

    protected boolean isTracing() {
        return this.traceLogger.isLoggable(Level.FINEST);
    }

    protected void suspendTracing() {
        if (this.traceLogger.isLoggable(Level.FINEST)) {
            this.traceInput.setTrace(false);
            this.traceOutput.setTrace(false);
        }
    }

    protected void resumeTracing() {
        if (this.traceLogger.isLoggable(Level.FINEST)) {
            this.traceInput.setTrace(true);
            this.traceOutput.setTrace(true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void finalize() throws Throwable {
        try {
            this.disconnect();
            Object var2_1 = null;
        }
        catch (Throwable throwable) {
            Object var2_2 = null;
            super.finalize();
            throw throwable;
        }
        super.finalize();
    }

    private void commandStart(String command) {
    }

    private void commandEnd() {
    }
}

