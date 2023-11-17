/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.engio.mbassy.listener.Handler
 */
package com.hierynomus.smbj.connection;

import com.hierynomus.asn1.types.primitive.ASN1ObjectIdentifier;
import com.hierynomus.mserref.NtStatus;
import com.hierynomus.mssmb.SMB1NotSupportedException;
import com.hierynomus.mssmb.SMB1PacketFactory;
import com.hierynomus.mssmb.messages.SMB1ComNegotiateRequest;
import com.hierynomus.mssmb2.SMB2Dialect;
import com.hierynomus.mssmb2.SMB2GlobalCapability;
import com.hierynomus.mssmb2.SMB2Header;
import com.hierynomus.mssmb2.SMB2MessageCommandCode;
import com.hierynomus.mssmb2.SMB2MessageConverter;
import com.hierynomus.mssmb2.SMB2MessageFlag;
import com.hierynomus.mssmb2.SMB2Packet;
import com.hierynomus.mssmb2.SMB2PacketData;
import com.hierynomus.mssmb2.SMB2PacketFactory;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.mssmb2.messages.SMB2CancelRequest;
import com.hierynomus.mssmb2.messages.SMB2NegotiateRequest;
import com.hierynomus.mssmb2.messages.SMB2NegotiateResponse;
import com.hierynomus.mssmb2.messages.SMB2SessionSetup;
import com.hierynomus.protocol.commons.Factory;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.protocol.commons.concurrent.AFuture;
import com.hierynomus.protocol.commons.concurrent.CancellableFuture;
import com.hierynomus.protocol.commons.concurrent.Futures;
import com.hierynomus.protocol.transport.PacketFactory;
import com.hierynomus.protocol.transport.PacketHandlers;
import com.hierynomus.protocol.transport.PacketReceiver;
import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.protocol.transport.TransportLayer;
import com.hierynomus.smb.SMBPacket;
import com.hierynomus.smb.SMBPacketData;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticateResponse;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.auth.Authenticator;
import com.hierynomus.smbj.common.Pooled;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.connection.ConnectionInfo;
import com.hierynomus.smbj.connection.NegotiatedProtocol;
import com.hierynomus.smbj.connection.OutstandingRequests;
import com.hierynomus.smbj.connection.Request;
import com.hierynomus.smbj.connection.SMBPacketSerializer;
import com.hierynomus.smbj.connection.SequenceWindow;
import com.hierynomus.smbj.connection.SessionTable;
import com.hierynomus.smbj.event.ConnectionClosed;
import com.hierynomus.smbj.event.SMBEventBus;
import com.hierynomus.smbj.event.SessionLoggedOff;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.spnego.NegTokenInit;
import com.hierynomus.spnego.NegTokenInit2;
import com.hierynomus.spnego.SpnegoException;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.engio.mbassy.listener.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connection
extends Pooled<Connection>
implements Closeable,
PacketReceiver<SMBPacketData<?>> {
    private static final Logger logger = LoggerFactory.getLogger(Connection.class);
    private static final DelegatingSMBMessageConverter converter = new DelegatingSMBMessageConverter(new SMB2PacketFactory(), new SMB1PacketFactory());
    private ConnectionInfo connectionInfo;
    private SessionTable sessionTable = new SessionTable();
    private SessionTable preauthSessionTable = new SessionTable();
    private OutstandingRequests outstandingRequests = new OutstandingRequests();
    private SequenceWindow sequenceWindow;
    private SMB2MessageConverter smb2Converter = new SMB2MessageConverter();
    private String remoteName;
    private SMBClient client;
    private SmbConfig config;
    private TransportLayer<SMBPacket<?, ?>> transport;
    private final SMBEventBus bus;
    private final ReentrantLock lock = new ReentrantLock();
    private int remotePort;

    public SMBClient getClient() {
        return this.client;
    }

    public Connection(SmbConfig config, SMBClient client, SMBEventBus bus) {
        this.config = config;
        this.client = client;
        this.transport = config.getTransportLayerFactory().createTransportLayer(new PacketHandlers(new SMBPacketSerializer(), this, converter), config);
        this.bus = bus;
        bus.subscribe(this);
    }

    public Connection(Connection connection) {
        this.client = connection.client;
        this.config = connection.config;
        this.transport = connection.transport;
        this.bus = connection.bus;
        this.bus.subscribe(this);
    }

    public void connect(String hostname, int port) throws IOException {
        if (this.isConnected()) {
            throw new IllegalStateException(String.format("This connection is already connected to %s", this.getRemoteHostname()));
        }
        this.remoteName = hostname;
        this.remotePort = port;
        this.transport.connect(new InetSocketAddress(hostname, port));
        this.sequenceWindow = new SequenceWindow();
        this.connectionInfo = new ConnectionInfo(this.config.getClientGuid(), hostname);
        this.negotiateDialect();
        logger.info("Successfully connected to: {}", (Object)this.getRemoteHostname());
    }

    @Override
    public void close() throws IOException {
        this.close(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close(boolean force) throws IOException {
        if (!force && !this.release()) {
            return;
        }
        try {
            if (!force) {
                for (Session session : this.sessionTable.activeSessions()) {
                    try {
                        session.close();
                    }
                    catch (IOException e) {
                        logger.warn("Exception while closing session {}", (Object)session.getSessionId(), (Object)e);
                    }
                }
            }
        }
        finally {
            this.transport.disconnect();
            logger.info("Closed connection to {}", (Object)this.getRemoteHostname());
            this.bus.publish(new ConnectionClosed(this.remoteName, this.remotePort));
        }
    }

    public SmbConfig getConfig() {
        return this.config;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Session authenticate(AuthenticationContext authContext) {
        Session session;
        block10: {
            Authenticator authenticator = this.getAuthenticator(authContext);
            authenticator.init(this.config);
            Session session2 = this.getSession(authContext);
            byte[] securityContext = this.processAuthenticationToken(authenticator, authContext, this.connectionInfo.getGssNegotiateToken(), session2);
            SMB2SessionSetup receive = this.initiateSessionSetup(securityContext, 0L);
            long preauthSessionId = ((SMB2Header)receive.getHeader()).getSessionId();
            if (preauthSessionId != 0L) {
                this.preauthSessionTable.registerSession(preauthSessionId, session2);
            }
            try {
                while (((SMB2Header)receive.getHeader()).getStatusCode() == NtStatus.STATUS_MORE_PROCESSING_REQUIRED.getValue()) {
                    logger.debug("More processing required for authentication of {} using {}", (Object)authContext.getUsername(), (Object)authenticator);
                    securityContext = this.processAuthenticationToken(authenticator, authContext, receive.getSecurityBuffer(), session2);
                    receive = this.initiateSessionSetup(securityContext, preauthSessionId);
                }
                if (((SMB2Header)receive.getHeader()).getStatusCode() != NtStatus.STATUS_SUCCESS.getValue()) {
                    throw new SMBApiException((SMB2Header)receive.getHeader(), String.format("Authentication failed for '%s' using %s", authContext.getUsername(), authenticator));
                }
                session2.setSessionId(((SMB2Header)receive.getHeader()).getSessionId());
                if (receive.getSecurityBuffer() != null) {
                    this.processAuthenticationToken(authenticator, authContext, receive.getSecurityBuffer(), session2);
                }
                session2.init(receive);
                logger.info("Successfully authenticated {} on {}, session is {}", authContext.getUsername(), this.remoteName, session2.getSessionId());
                this.sessionTable.registerSession(session2.getSessionId(), session2);
                session = session2;
                if (preauthSessionId == 0L) break block10;
                this.preauthSessionTable.sessionClosed(preauthSessionId);
            }
            catch (Throwable throwable) {
                try {
                    if (preauthSessionId != 0L) {
                        this.preauthSessionTable.sessionClosed(preauthSessionId);
                    }
                    throw throwable;
                }
                catch (SpnegoException | IOException e) {
                    throw new SMBRuntimeException(e);
                }
            }
        }
        return session;
    }

    private Session getSession(AuthenticationContext authContext) {
        return new Session(this, authContext, this.bus, this.client.getPathResolver(), this.config.getSecurityProvider());
    }

    private byte[] processAuthenticationToken(Authenticator authenticator, AuthenticationContext authContext, byte[] inputToken, Session session) throws IOException {
        AuthenticateResponse resp = authenticator.authenticate(authContext, inputToken, session);
        if (resp == null) {
            return null;
        }
        this.connectionInfo.setWindowsVersion(resp.getWindowsVersion());
        this.connectionInfo.setNetBiosName(resp.getNetBiosName());
        byte[] securityContext = resp.getNegToken();
        if (resp.getSigningKey() != null) {
            session.setSigningKey(resp.getSigningKey());
        }
        return securityContext;
    }

    private SMB2SessionSetup initiateSessionSetup(byte[] securityContext, long sessionId) throws TransportException {
        SMB2SessionSetup req = new SMB2SessionSetup(this.connectionInfo.getNegotiatedProtocol().getDialect(), EnumSet.of(SMB2SessionSetup.SMB2SecurityMode.SMB2_NEGOTIATE_SIGNING_ENABLED), this.connectionInfo.getClientCapabilities());
        req.setSecurityBuffer(securityContext);
        ((SMB2Header)req.getHeader()).setSessionId(sessionId);
        return (SMB2SessionSetup)this.sendAndReceive(req);
    }

    private Authenticator getAuthenticator(AuthenticationContext context) throws SpnegoException {
        ArrayList<Factory.Named<Authenticator>> supportedAuthenticators = new ArrayList<Factory.Named<Authenticator>>(this.config.getSupportedAuthenticators());
        List<Object> mechTypes = new ArrayList();
        if (this.connectionInfo.getGssNegotiateToken().length > 0) {
            NegTokenInit negTokenInit = new NegTokenInit2().read(this.connectionInfo.getGssNegotiateToken());
            mechTypes = negTokenInit.getSupportedMechTypes();
        }
        for (Factory.Named<Authenticator> factory : new ArrayList<Factory.Named<Authenticator>>(supportedAuthenticators)) {
            Authenticator authenticator;
            if (!mechTypes.isEmpty() && !mechTypes.contains(new ASN1ObjectIdentifier(factory.getName())) || !(authenticator = (Authenticator)factory.create()).supports(context)) continue;
            return authenticator;
        }
        throw new SMBRuntimeException("Could not find a configured authenticator for mechtypes: " + mechTypes + " and authentication context: " + context);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public <T extends SMB2Packet> Future<T> send(SMB2Packet packet) throws TransportException {
        this.lock.lock();
        AFuture f = null;
        try {
            if (!(packet.getPacket() instanceof SMB2CancelRequest)) {
                int availableCredits = this.sequenceWindow.available();
                int grantCredits = this.calculateGrantedCredits(packet, availableCredits);
                if (availableCredits == 0) {
                    logger.warn("There are no credits left to send {}, will block until there are more credits available.", (Object)((SMB2Header)packet.getHeader()).getMessage());
                }
                long[] messageIds = this.sequenceWindow.get(grantCredits);
                ((SMB2Header)packet.getHeader()).setMessageId(messageIds[0]);
                logger.debug("Granted {} (out of {}) credits to {}", grantCredits, availableCredits, packet);
                ((SMB2Header)packet.getHeader()).setCreditRequest(Math.max(512 - availableCredits - grantCredits, grantCredits));
                Request request = new Request(packet.getPacket(), messageIds[0], UUID.randomUUID());
                this.outstandingRequests.registerOutstanding(request);
                f = request.getFuture(new CancelRequest(request, ((SMB2Header)packet.getHeader()).getSessionId()));
            }
            this.transport.write(packet);
            AFuture aFuture = f;
            return aFuture;
        }
        finally {
            this.lock.unlock();
        }
    }

    private <T extends SMB2Packet> T sendAndReceive(SMB2Packet packet) throws TransportException {
        return (T)((SMB2Packet)Futures.get(this.send(packet), this.getConfig().getTransactTimeout(), TimeUnit.MILLISECONDS, TransportException.Wrapper));
    }

    private int calculateGrantedCredits(SMB2Packet packet, int availableCredits) {
        int grantCredits;
        int maxPayloadSize = packet.getMaxPayloadSize();
        int creditsNeeded = this.creditsNeeded(maxPayloadSize);
        if (creditsNeeded > 1 && !this.connectionInfo.supports(SMB2GlobalCapability.SMB2_GLOBAL_CAP_LARGE_MTU)) {
            logger.trace("Connection to {} does not support multi-credit requests.", (Object)this.getRemoteHostname());
            grantCredits = 1;
        } else {
            grantCredits = creditsNeeded < availableCredits ? creditsNeeded : (creditsNeeded > 1 && availableCredits > 1 ? availableCredits - 1 : 1);
        }
        packet.setCreditsAssigned(grantCredits);
        return grantCredits;
    }

    private void negotiateDialect() throws TransportException {
        logger.debug("Negotiating dialects {} with server {}", (Object)this.config.getSupportedDialects(), (Object)this.getRemoteHostname());
        SMB2Packet resp = this.config.isUseMultiProtocolNegotiate() ? this.multiProtocolNegotiate() : this.smb2OnlyNegotiate();
        if (!(resp instanceof SMB2NegotiateResponse)) {
            throw new IllegalStateException("Expected a SMB2 NEGOTIATE Response, but got: " + resp);
        }
        SMB2NegotiateResponse negotiateResponse = (SMB2NegotiateResponse)resp;
        if (!NtStatus.isSuccess(((SMB2Header)negotiateResponse.getHeader()).getStatusCode())) {
            throw new SMBApiException((SMB2Header)negotiateResponse.getHeader(), "Failure during dialect negotiation");
        }
        this.connectionInfo.negotiated(negotiateResponse);
        logger.debug("Negotiated the following connection settings: {}", (Object)this.connectionInfo);
    }

    private SMB2Packet smb2OnlyNegotiate() throws TransportException {
        SMB2NegotiateRequest negotiatePacket = new SMB2NegotiateRequest(this.config.getSupportedDialects(), this.connectionInfo.getClientGuid(), this.config.isSigningRequired());
        return this.sendAndReceive(negotiatePacket);
    }

    private SMB2Packet multiProtocolNegotiate() throws TransportException {
        SMB1ComNegotiateRequest negotiatePacket = new SMB1ComNegotiateRequest(this.config.getSupportedDialects());
        long l = this.sequenceWindow.get();
        if (l != 0L) {
            throw new IllegalStateException("The SMBv1 SMB_COM_NEGOTIATE packet needs to be the first packet sent.");
        }
        Request request = new Request(negotiatePacket, l, UUID.randomUUID());
        this.outstandingRequests.registerOutstanding(request);
        this.transport.write(negotiatePacket);
        AFuture future = request.getFuture(null);
        SMB2Packet packet = (SMB2Packet)Futures.get(future, this.getConfig().getTransactTimeout(), TimeUnit.MILLISECONDS, TransportException.Wrapper);
        if (!(packet instanceof SMB2NegotiateResponse)) {
            throw new IllegalStateException("Expected a SMB2 NEGOTIATE Response to our SMB_COM_NEGOTIATE, but got: " + packet);
        }
        SMB2NegotiateResponse negotiateResponse = (SMB2NegotiateResponse)packet;
        if (negotiateResponse.getDialect() == SMB2Dialect.SMB_2XX) {
            return this.smb2OnlyNegotiate();
        }
        return negotiateResponse;
    }

    private int creditsNeeded(int payloadSize) {
        return Math.abs((payloadSize - 1) / 65536) + 1;
    }

    public NegotiatedProtocol getNegotiatedProtocol() {
        return this.connectionInfo.getNegotiatedProtocol();
    }

    @Override
    public void handle(SMBPacketData uncheckedPacket) throws TransportException {
        if (!(uncheckedPacket instanceof SMB2PacketData)) {
            throw new SMB1NotSupportedException();
        }
        SMB2PacketData packetData = (SMB2PacketData)uncheckedPacket;
        long messageId = packetData.getSequenceNumber();
        if (!this.outstandingRequests.isOutstanding(messageId)) {
            throw new TransportException("Received response with unknown sequence number <<" + messageId + ">>");
        }
        this.sequenceWindow.creditsGranted(((SMB2Header)packetData.getHeader()).getCreditResponse());
        logger.debug("Server granted us {} credits for {}, now available: {} credits", ((SMB2Header)packetData.getHeader()).getCreditResponse(), packetData, this.sequenceWindow.available());
        Request request = this.outstandingRequests.getRequestByMessageId(messageId);
        logger.trace("Send/Recv of packet {} took << {} ms >>", (Object)packetData, (Object)(System.currentTimeMillis() - request.getTimestamp().getTime()));
        if (packetData.isIntermediateAsyncResponse()) {
            logger.debug("Received ASYNC packet {} with AsyncId << {} >>", (Object)packetData, (Object)((SMB2Header)packetData.getHeader()).getAsyncId());
            request.setAsyncId(((SMB2Header)packetData.getHeader()).getAsyncId());
            return;
        }
        SMB2Packet packet = null;
        try {
            packet = this.smb2Converter.readPacket(request.getPacket(), packetData);
        }
        catch (Buffer.BufferException e) {
            throw new TransportException("Unable to deserialize SMB2 Packet Data.", e);
        }
        long sessionId = ((SMB2Header)packetData.getHeader()).getSessionId();
        if (sessionId != 0L && ((SMB2Header)packetData.getHeader()).getMessage() != SMB2MessageCommandCode.SMB2_SESSION_SETUP) {
            Session session = this.sessionTable.find(sessionId);
            if (session == null && (session = this.preauthSessionTable.find(sessionId)) == null) {
                logger.warn("Illegal request, no session matching the sessionId: {}", (Object)sessionId);
                return;
            }
            this.verifyPacketSignature(packet, session);
        }
        this.outstandingRequests.receivedResponseFor(messageId).getPromise().deliver(packet);
    }

    private void verifyPacketSignature(SMB2Packet packet, Session session) throws TransportException {
        if (((SMB2Header)packet.getHeader()).isFlagSet(SMB2MessageFlag.SMB2_FLAGS_SIGNED)) {
            if (!session.getPacketSignatory().verify(packet)) {
                logger.warn("Invalid packet signature for packet {}", (Object)packet);
                if (session.isSigningRequired()) {
                    throw new TransportException("Packet signature for packet " + packet + " was not correct");
                }
            }
        } else if (session.isSigningRequired()) {
            logger.warn("Illegal request, session requires message signing, but packet {} is not signed.", (Object)packet);
            throw new TransportException("Session requires signing, but packet " + packet + " was not signed");
        }
    }

    @Override
    public void handleError(Throwable t) {
        this.outstandingRequests.handleError(t);
        try {
            this.close();
        }
        catch (Exception e) {
            String exceptionClass = e.getClass().getSimpleName();
            logger.debug("{} while closing connection on error, ignoring: {}", (Object)exceptionClass, (Object)e.getMessage());
        }
    }

    public String getRemoteHostname() {
        return this.remoteName;
    }

    public boolean isConnected() {
        return this.transport.isConnected();
    }

    public ConnectionInfo getConnectionInfo() {
        return this.connectionInfo;
    }

    @Handler
    private void sessionLogoff(SessionLoggedOff loggedOff) {
        this.sessionTable.sessionClosed(loggedOff.getSessionId());
        logger.debug("Session << {} >> logged off", (Object)loggedOff.getSessionId());
    }

    private class CancelRequest
    implements CancellableFuture.CancelCallback {
        private Request request;
        private long sessionId;

        public CancelRequest(Request request, long sessionId) {
            this.request = request;
            this.sessionId = sessionId;
        }

        @Override
        public void cancel() {
            SMB2CancelRequest cancel = new SMB2CancelRequest(Connection.this.connectionInfo.getNegotiatedProtocol().getDialect(), this.request.getMessageId(), this.request.getAsyncId());
            try {
                Connection.this.sessionTable.find(this.sessionId).send(cancel);
            }
            catch (TransportException e) {
                logger.error("Failed to send {}", (Object)cancel);
            }
        }
    }

    private static class DelegatingSMBMessageConverter
    implements PacketFactory<SMBPacketData<?>> {
        private PacketFactory<?>[] packetFactories;

        public DelegatingSMBMessageConverter(PacketFactory<?> ... packetFactories) {
            this.packetFactories = packetFactories;
        }

        @Override
        public SMBPacketData<?> read(byte[] data) throws Buffer.BufferException, IOException {
            for (PacketFactory<?> packetFactory : this.packetFactories) {
                if (!packetFactory.canHandle(data)) continue;
                return (SMBPacketData)packetFactory.read(data);
            }
            throw new IOException("Unknown packet format received.");
        }

        @Override
        public boolean canHandle(byte[] data) {
            for (PacketFactory<?> packetFactory : this.packetFactories) {
                if (!packetFactory.canHandle(data)) continue;
                return true;
            }
            return false;
        }
    }
}

