/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.engio.mbassy.listener.Handler
 */
package com.hierynomus.smbj.session;

import com.hierynomus.mserref.NtStatus;
import com.hierynomus.mssmb2.SMB2Header;
import com.hierynomus.mssmb2.SMB2MessageCommandCode;
import com.hierynomus.mssmb2.SMB2Packet;
import com.hierynomus.mssmb2.SMB2ShareCapabilities;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.mssmb2.messages.SMB2CreateRequest;
import com.hierynomus.mssmb2.messages.SMB2Logoff;
import com.hierynomus.mssmb2.messages.SMB2SessionSetup;
import com.hierynomus.mssmb2.messages.SMB2TreeConnectRequest;
import com.hierynomus.mssmb2.messages.SMB2TreeConnectResponse;
import com.hierynomus.protocol.commons.concurrent.Futures;
import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.security.SecurityProvider;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.event.SMBEventBus;
import com.hierynomus.smbj.event.SessionLoggedOff;
import com.hierynomus.smbj.event.TreeDisconnected;
import com.hierynomus.smbj.paths.PathResolveException;
import com.hierynomus.smbj.paths.PathResolver;
import com.hierynomus.smbj.session.PacketSignatory;
import com.hierynomus.smbj.session.SMB2GuestSigningRequiredException;
import com.hierynomus.smbj.session.TreeConnectTable;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.PipeShare;
import com.hierynomus.smbj.share.PrinterShare;
import com.hierynomus.smbj.share.Share;
import com.hierynomus.smbj.share.TreeConnect;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import net.engio.mbassy.listener.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Session
implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Session.class);
    private long sessionId;
    private PacketSignatory packetSignatory;
    private boolean signingRequired;
    private boolean encryptData;
    private Connection connection;
    private SMBEventBus bus;
    private final PathResolver pathResolver;
    private TreeConnectTable treeConnectTable = new TreeConnectTable();
    private List<Session> nestedSessions = new ArrayList<Session>();
    private AuthenticationContext userCredentials;
    private boolean guest;
    private boolean anonymous;

    public Session(Connection connection, AuthenticationContext userCredentials, SMBEventBus bus, PathResolver pathResolver, SecurityProvider securityProvider) {
        this.connection = connection;
        this.userCredentials = userCredentials;
        this.bus = bus;
        this.pathResolver = pathResolver;
        this.packetSignatory = new PacketSignatory(connection.getNegotiatedProtocol().getDialect(), securityProvider);
        if (bus != null) {
            bus.subscribe(this);
        }
    }

    public void init(SMB2SessionSetup setup) {
        this.guest = setup.getSessionFlags().contains(SMB2SessionSetup.SMB2SessionFlags.SMB2_SESSION_FLAG_IS_GUEST);
        this.anonymous = setup.getSessionFlags().contains(SMB2SessionSetup.SMB2SessionFlags.SMB2_SESSION_FLAG_IS_NULL);
        this.validateAndSetSigning(setup);
        if (this.guest || this.anonymous) {
            this.packetSignatory.init(null);
        }
    }

    private void validateAndSetSigning(SMB2SessionSetup setup) {
        boolean requireMessageSigning = this.connection.getConfig().isSigningRequired();
        boolean connectionSigningRequired = this.connection.getConnectionInfo().isServerRequiresSigning();
        this.signingRequired = requireMessageSigning || connectionSigningRequired;
        if (this.anonymous) {
            this.signingRequired = false;
        }
        if (this.guest && this.signingRequired) {
            throw new SMB2GuestSigningRequiredException();
        }
        if (this.guest && !requireMessageSigning) {
            this.signingRequired = false;
        }
        if (this.connection.getNegotiatedProtocol().getDialect().isSmb3x() && setup.getSessionFlags().contains(SMB2SessionSetup.SMB2SessionFlags.SMB2_SESSION_FLAG_ENCRYPT_DATA)) {
            this.encryptData = true;
            this.signingRequired = false;
        }
    }

    public long getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public Share connectShare(String shareName) {
        if (shareName.contains("\\")) {
            throw new IllegalArgumentException(String.format("Share name (%s) cannot contain '\\' characters.", shareName));
        }
        Share connectedShare = this.treeConnectTable.getTreeConnect(shareName);
        if (connectedShare != null) {
            logger.debug("Returning cached Share {} for {}", (Object)connectedShare, (Object)shareName);
            return connectedShare;
        }
        return this.connectTree(shareName);
    }

    private Share connectTree(String shareName) {
        String remoteHostname = this.connection.getRemoteHostname();
        SmbPath smbPath = new SmbPath(remoteHostname, shareName);
        logger.info("Connecting to {} on session {}", (Object)smbPath, (Object)this.sessionId);
        try {
            Share share;
            SMB2TreeConnectRequest smb2TreeConnectRequest = new SMB2TreeConnectRequest(this.connection.getNegotiatedProtocol().getDialect(), smbPath, this.sessionId);
            ((SMB2Header)smb2TreeConnectRequest.getHeader()).setCreditRequest(256);
            Future send = this.send(smb2TreeConnectRequest);
            SMB2TreeConnectResponse response = (SMB2TreeConnectResponse)Futures.get(send, this.connection.getConfig().getTransactTimeout(), TimeUnit.MILLISECONDS, TransportException.Wrapper);
            try {
                SmbPath resolvedSharePath = this.pathResolver.resolve(this, response, smbPath);
                Session session = this;
                if (!resolvedSharePath.isOnSameHost(smbPath)) {
                    logger.info("Re-routing the connection to host {}", (Object)resolvedSharePath.getHostname());
                    session = this.buildNestedSession(resolvedSharePath);
                }
                if (!resolvedSharePath.isOnSameShare(smbPath)) {
                    return session.connectShare(resolvedSharePath.getShareName());
                }
            }
            catch (PathResolveException resolvedSharePath) {
                // empty catch block
            }
            if (NtStatus.isError(((SMB2Header)response.getHeader()).getStatusCode())) {
                logger.debug(((SMB2Header)response.getHeader()).toString());
                throw new SMBApiException((SMB2Header)response.getHeader(), "Could not connect to " + smbPath);
            }
            if (response.getCapabilities().contains(SMB2ShareCapabilities.SMB2_SHARE_CAP_ASYMMETRIC)) {
                throw new SMBRuntimeException("ASYMMETRIC capability unsupported");
            }
            long treeId = ((SMB2Header)response.getHeader()).getTreeId();
            TreeConnect treeConnect = new TreeConnect(treeId, smbPath, this, response.getCapabilities(), this.connection, this.bus, response.getMaximalAccess());
            if (response.isDiskShare()) {
                share = new DiskShare(smbPath, treeConnect, this.pathResolver);
            } else if (response.isNamedPipe()) {
                share = new PipeShare(smbPath, treeConnect);
            } else if (response.isPrinterShare()) {
                share = new PrinterShare(smbPath, treeConnect);
            } else {
                throw new SMBRuntimeException("Unknown ShareType returned in the TREE_CONNECT Response");
            }
            this.treeConnectTable.register(share);
            return share;
        }
        catch (TransportException e) {
            throw new SMBRuntimeException(e);
        }
    }

    public Session buildNestedSession(SmbPath resolvedSharePath) {
        try {
            Connection connection = this.getConnection().getClient().connect(resolvedSharePath.getHostname());
            Session session = connection.authenticate(this.getAuthenticationContext());
            this.nestedSessions.add(session);
            return session;
        }
        catch (IOException e) {
            throw new SMBApiException(NtStatus.STATUS_OTHER.getValue(), SMB2MessageCommandCode.SMB2_NEGOTIATE, "Could not connect to DFS root " + resolvedSharePath, e);
        }
    }

    @Handler
    private void disconnectTree(TreeDisconnected disconnectEvent) {
        if (disconnectEvent.getSessionId() == this.sessionId) {
            logger.debug("Notified of TreeDisconnected <<{}>>", (Object)disconnectEvent.getTreeId());
            this.treeConnectTable.closed(disconnectEvent.getTreeId());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void logoff() throws TransportException {
        try {
            logger.info("Logging off session {} from host {}", (Object)this.sessionId, (Object)this.connection.getRemoteHostname());
            for (Share share : this.treeConnectTable.getOpenTreeConnects()) {
                try {
                    share.close();
                }
                catch (IOException e) {
                    logger.error("Caught exception while closing TreeConnect with id: {}", (Object)share.getTreeConnect().getTreeId(), (Object)e);
                }
            }
            for (Session nestedSession : this.nestedSessions) {
                logger.info("Logging off nested session {} for session {}", (Object)nestedSession.getSessionId(), (Object)this.sessionId);
                try {
                    nestedSession.logoff();
                }
                catch (TransportException te) {
                    logger.error("Caught exception while logging off nested session {}", (Object)nestedSession.getSessionId());
                }
            }
            SMB2Logoff logoff = new SMB2Logoff(this.connection.getNegotiatedProtocol().getDialect(), this.sessionId);
            SMB2Logoff response = (SMB2Logoff)Futures.get(this.send(logoff), this.connection.getConfig().getTransactTimeout(), TimeUnit.MILLISECONDS, TransportException.Wrapper);
            if (!NtStatus.isSuccess(((SMB2Header)response.getHeader()).getStatusCode())) {
                throw new SMBApiException((SMB2Header)response.getHeader(), "Could not logoff session <<" + this.sessionId + ">>");
            }
        }
        finally {
            this.bus.publish(new SessionLoggedOff(this.sessionId));
        }
    }

    public boolean isSigningRequired() {
        return this.signingRequired;
    }

    public boolean isGuest() {
        return this.guest;
    }

    public boolean isAnonymous() {
        return this.anonymous;
    }

    public void setSigningKey(byte[] signingKeyBytes) {
        this.packetSignatory.init(signingKeyBytes);
    }

    @Override
    public void close() throws IOException {
        this.logoff();
    }

    public Connection getConnection() {
        return this.connection;
    }

    public <T extends SMB2Packet> Future<T> send(SMB2Packet packet) throws TransportException {
        if (this.signingRequired && !this.packetSignatory.isInitialized()) {
            throw new TransportException("Message signing is required, but no signing key is negotiated");
        }
        return this.connection.send(this.packetSignatory.sign(packet));
    }

    public <T extends SMB2Packet> T processSendResponse(SMB2CreateRequest packet) throws TransportException {
        Future<T> responseFuture = this.send(packet);
        return (T)((SMB2Packet)Futures.get(responseFuture, SMBRuntimeException.Wrapper));
    }

    public AuthenticationContext getAuthenticationContext() {
        return this.userCredentials;
    }

    public PacketSignatory getPacketSignatory() {
        return this.packetSignatory;
    }
}

