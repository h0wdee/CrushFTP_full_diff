/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.client.AsyncConsumer;
import com.visuality.nq.client.Client;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.ServerCleanup;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.CaptureInternal;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SyncObject;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.config.Config;
import com.visuality.nq.resolve.NetbiosException;
import com.visuality.nq.resolve.NetbiosName;
import com.visuality.nq.resolve.SessionService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class Transport {
    static final int TYPE_NETBIOS = 1;
    static final int TYPE_IPV4 = 2;
    static final int TYPE_IPV6 = 3;
    static final int DEFAULT_TCP_PORT = 445;
    static final int NETBIOS_PORT = 139;
    private ConnectionBrokenCallback connectionBrokenCallback;
    private boolean isNetbios = false;
    private Server server = null;
    private boolean connected = false;
    private boolean doReceive = false;
    private boolean isHardDisconnect = false;
    private byte[] recvHeaderdata = new byte[4];
    private boolean forceNetbios = false;
    private Spooler spooler = new Spooler();
    private Thread spoolThread = new Thread((Runnable)this.spooler, "SpoolerThread");
    private Queue queue = new LinkedList();
    private ReceiveThreadBody receiveThreadBody = new ReceiveThreadBody();
    private Thread receiveThread = new Thread((Runnable)this.receiveThreadBody, "ReceiveThread");
    private Socket socket = null;
    private InputStream receiveStream;
    private OutputStream sendStream;
    private boolean receiving = false;
    private boolean isConnectionTimedout = false;
    private CaptureInternal capture = null;
    protected Object connectionGuard = new Object();
    private int receivingRemain = 0;
    private boolean doDisconnect = false;
    private boolean waitingDisconect = false;

    protected void initTransport() {
        TraceLog.get().enter(700);
        TraceLog.get().message("Creating new transport thread", 700);
        this.receiveThreadBody = new ReceiveThreadBody();
        this.receiveThread = new Thread((Runnable)this.receiveThreadBody, "ReceiveThread");
        this.spooler = new Spooler();
        this.spoolThread = new Thread((Runnable)this.spooler, "SpoolerThread");
        TraceLog.get().exit(700);
    }

    boolean isConnectionTimedout() {
        return this.isConnectionTimedout;
    }

    public String getTransportIp() {
        if (null == this.socket || null == this.socket.getInetAddress()) {
            return null;
        }
        return this.socket.getInetAddress().getHostAddress();
    }

    public int getTransportPort() {
        return this.socket.getLocalPort();
    }

    public boolean isReceiving() {
        return this.receiving;
    }

    public void setReceiving(boolean receiving) {
        this.receiving = receiving;
    }

    public int getReceivingRemain() {
        return this.receivingRemain;
    }

    public void setReceivingRemaiing(int remain) {
        this.receivingRemain = remain;
    }

    public Transport() {
    }

    public Transport(Server server) {
        this.server = server;
        this.capture = new CaptureInternal();
        ServerCleanup.initializeCleanupThread();
    }

    void start() {
        TraceLog.get().enter(700);
        this.receiveThread.start();
        this.spoolThread.start();
        TraceLog.get().exit(700);
    }

    public CaptureInternal getCapture() {
        return this.capture;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean connect(InetAddress[] ips, String serverName, ResponseCallback responseCallback, ConnectionBrokenCallback connectionBrokenCallback, boolean isTemporary, byte[] captureHdr, int port) {
        boolean isNetBIOSTransportDisabled;
        TraceLog.get().enter(700);
        TraceLog.get().message("port = " + port + "; ips length = " + (null == ips ? "null" : Integer.valueOf(ips.length)), 2000);
        int chosenTCPPort = -1 == port ? 445 : port;
        try {
            isNetBIOSTransportDisabled = Config.jnq.getBool("DISABLENETBIOSTRANSPORT");
        }
        catch (NqException e) {
            TraceLog.get().error("Failed to get the value of DISABLENETBIOSTRANSPORT, default will be used. Exception = ", e);
            isNetBIOSTransportDisabled = false;
        }
        long transportTimeout = 5000L;
        try {
            transportTimeout = Config.jnq.getInt("TRANSPORTTIMEOUT");
        }
        catch (NqException e) {
            TraceLog.get().error("Failed to get the value of TRANSPORTTIMEOUT, default will be used. Exception = ", e);
        }
        TransportType[] transportTypes1 = new TransportType[]{new TransportType(2, chosenTCPPort, false), new TransportType(3, chosenTCPPort, false), new TransportType(1, 139, true)};
        TransportType[] transportTypes2 = new TransportType[]{new TransportType(1, 139, true), new TransportType(2, chosenTCPPort, false), new TransportType(3, chosenTCPPort, false)};
        TransportType[] transportTypesNoNetBIOS = new TransportType[]{new TransportType(2, chosenTCPPort, false), new TransportType(3, chosenTCPPort, false)};
        TransportType[] transportTypes = isNetBIOSTransportDisabled ? transportTypesNoNetBIOS : (this.forceNetbios ? transportTypes2 : transportTypes1);
        this.isConnectionTimedout = false;
        this.connected = false;
        this.doDisconnect = false;
        this.waitingDisconect = false;
        this.connectionBrokenCallback = connectionBrokenCallback;
        NetbiosName nbName = null;
        try {
            nbName = new NetbiosName(serverName, 32);
        }
        catch (NetbiosException e) {
            TraceLog.get().error("NetbiosException = ", e);
            TraceLog.get().message("Notify receiveThread, hashCode = " + this.receiveThread.hashCode() + "; Transport hashCode = " + this.hashCode(), 2000);
            this.receiveThreadBody.syncObj.syncNotify();
            TraceLog.get().exit(700);
            return false;
        }
        if (null == ips || null == ips[0]) {
            TraceLog.get().error("ip list is empty, cannot connect");
            TraceLog.get().message("Notify receiveThread, hashCode = " + this.receiveThread.hashCode() + "; Transport hashCode = " + this.hashCode(), 2000);
            this.receiveThreadBody.syncObj.syncNotify();
            TraceLog.get().exit(700);
            return false;
        }
        block18: for (int transportIdx = 0; transportIdx < transportTypes.length; ++transportIdx) {
            for (int ipIdx = 0; ipIdx < ips.length; ++ipIdx) {
                int addrType;
                int n = addrType = Inet4Address.class.isInstance(ips[ipIdx]) ? 2 : 3;
                if (addrType == 2 && transportTypes[transportIdx].type == 3 || addrType == 3 && transportTypes[transportIdx].type != 3) continue;
                int maxTimeToWait = 0;
                if (!this.forceNetbios && 139 == transportTypes[transportIdx].port) {
                    maxTimeToWait = 250;
                } else if (this.forceNetbios && 139 != transportTypes[transportIdx].port) {
                    maxTimeToWait = 500;
                }
                if (0 != maxTimeToWait) {
                    Object object = this.connectionGuard;
                    synchronized (object) {
                        if (!this.connected) {
                            try {
                                this.connectionGuard.wait(maxTimeToWait);
                            }
                            catch (InterruptedException e) {
                                // empty catch block
                            }
                        }
                        if (this.connected) {
                            continue block18;
                        }
                    }
                }
                try {
                    TraceLog.get().message("Initializing ConnectionThread for " + ips[ipIdx], 700);
                    new Thread((Runnable)new ConnectionThread(transportTypes[transportIdx], ips[ipIdx], nbName), "ConnectionThread").start();
                    continue;
                }
                catch (Exception ex) {
                    TraceLog.get().error("Unable to create thread: ", ex, 10, 0);
                }
            }
        }
        try {
            Object transportIdx = this.connectionGuard;
            synchronized (transportIdx) {
                if (!this.connected) {
                    this.connectionGuard.wait(transportTimeout);
                }
                this.doReceive = true;
                TraceLog.get().message("Notify receiveThread, hashCode = " + this.receiveThread.hashCode() + "; Transport hashCode = " + this.hashCode(), 2000);
                this.receiveThreadBody.syncObj.syncNotify();
                TraceLog.get().exit(700);
                return this.connected;
            }
        }
        catch (InterruptedException e) {
            TraceLog.get().message("Notify receiveThread, hashCode = " + this.receiveThread.hashCode() + "; Transport hashCode = " + this.hashCode(), 2000);
            this.receiveThreadBody.syncObj.syncNotify();
            TraceLog.get().exit(700);
            return false;
        }
    }

    public boolean getIsNetbios() {
        return this.isNetbios;
    }

    public Server getServer() {
        return this.server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public boolean isTimeoutExpired(long idleTime) {
        if (0L == this.server.getTtl()) {
            return false;
        }
        return this.server.getTtl() + idleTime < System.currentTimeMillis();
    }

    public boolean isConnected() {
        return this.connected && this.isSocketConnected();
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    protected void finalize() throws Throwable {
        if (null != this.server && 0 < this.server.getLocks()) {
            this.server.disconnect();
        }
        this.server = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean disconnect() throws NetbiosException {
        TraceLog.get().enter(700);
        boolean result = false;
        this.doReceive = false;
        this.spooler.setSpoolerThreadExit(true);
        this.receiveThreadBody.setReceiveThreadExit(true);
        Queue queue = this.queue;
        synchronized (queue) {
            this.queue.notify();
        }
        this.isHardDisconnect = false;
        this.receiveThread.interrupt();
        if (this.connected) {
            this.connected = false;
            try {
                this.socket.close();
            }
            catch (IOException e) {
                TraceLog.get().error("Invalid socket: ", e, 10, 0);
                TraceLog.get().exit(700);
                throw new NetbiosException("Invalid socket: " + e.getMessage(), -504);
            }
            if (null != this.server && null != this.server.smb) {
                this.server.smb.signalAllMatch(this);
            }
            result = true;
        } else if (null != this.server && null != this.server.smb) {
            this.server.smb.signalAllMatch(this);
            result = false;
        }
        TraceLog.get().exit(700);
        return result;
    }

    private boolean isSocketAlive() {
        try {
            return !this.socket.isClosed() && !this.socket.isInputShutdown() && !this.socket.isOutputShutdown();
        }
        catch (Exception e) {
            return false;
        }
    }

    private boolean isSocketConnected() {
        return this.socket.isConnected() && this.isSocketAlive();
    }

    public void send(byte[] buffer, int offset, int packetLen, int dataLen) throws NqException, NetbiosException {
        this.send(buffer, offset, packetLen, dataLen, true);
    }

    public void send(byte[] buffer, int offset, int packetLen, int dataLen, boolean toCapture) throws NqException {
        if (!this.isSocketAlive()) {
            this.disconnect();
            throw new NqException("Socket is dead", -16);
        }
        if (toCapture) {
            this.capture.capturePacketWritePacket(buffer, 4, dataLen);
        }
        dataLen = SessionService.makeHeader(buffer, packetLen, dataLen);
        SessionService.sendFromBuffer(this.sendStream, buffer, dataLen);
        this.server.updateTtl();
    }

    public void sendTail(byte[] data, int offset, int dataLen) throws NetbiosException {
        try {
            this.sendStream.write(data, offset, dataLen);
        }
        catch (IOException e) {
            TraceLog.get().error("Socket error: ", e, 10, 0);
            throw new NetbiosException("Invalid socket: " + e.getMessage(), -504);
        }
        byte[] captureData = new byte[dataLen];
        System.arraycopy(data, offset, captureData, 0, dataLen);
        this.capture.capturePacketWritePacket(captureData, dataLen);
    }

    public Buffer receiveAll() throws NqException {
        int packetLen = this.receivePacketLength();
        Buffer buffer = null;
        buffer = Buffer.getNewBuffer(packetLen);
        buffer.dataLen = packetLen;
        buffer.remaining = packetLen;
        this.receiveBytes(buffer, 0, buffer.dataLen);
        this.receiving = false;
        return buffer;
    }

    public synchronized int receivePacketLength() throws NqException {
        int len;
        this.receiving = true;
        try {
            len = SessionService.recvHeader(this.receiveStream, this.recvHeaderdata);
        }
        catch (NqException e) {
            if (-505 != e.getErrCode() && this.connected) {
                TraceLog.get().error("Cannot receive header: ", e, e.getErrCode());
                TraceLog.get().caught(e);
                throw e;
            }
            return 0;
        }
        this.receivingRemain = len;
        return len;
    }

    public synchronized int receiveBytes(Buffer buffer, int offset, int count) throws NetbiosException {
        return this.receiveBytes(buffer, offset, count, true);
    }

    public synchronized int receiveBytes(Buffer buffer, int offset, int count, boolean toCapture) throws NetbiosException {
        SessionService.recvIntoBuffer(this.receiveStream, buffer.data, offset, count);
        this.receivingRemain -= count;
        buffer.remaining -= count;
        if (toCapture) {
            this.capture.capturePacketWritePacket(buffer.data, offset, count);
            if (0 == this.receivingRemain) {
                this.capture.capturePacketWriteEnd();
            }
        }
        return count;
    }

    public synchronized void receiveEnd(Buffer buffer) throws NetbiosException {
        SessionService.recvSkip(this.receiveStream, this.receivingRemain);
        this.receivingRemain = 0;
        if (null != buffer) {
            buffer.remaining = 0;
        }
        this.receiving = false;
    }

    public synchronized void discardReceive() {
        this.receiving = false;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void setForceNetbios(boolean forceNetbios) {
        this.forceNetbios = forceNetbios;
    }

    public void hardDisconnect() throws IOException {
        this.hardDisconnect(true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void hardDisconnect(boolean isDisconnect) throws IOException {
        TraceLog.get().enter("isDisconnect=" + isDisconnect, 700);
        Server server = this.server;
        try {
            Server.lock(server);
            if (isDisconnect) {
                try {
                    server.disconnect(null, false, true);
                }
                catch (NqException e) {
                    TraceLog.get().error("Error disconnecting from server: ", e, 2000, e.getErrCode());
                }
            }
            this.connected = false;
            this.doReceive = false;
            this.isHardDisconnect = true;
            this.socket.close();
        }
        finally {
            Server.releaseLock(server);
        }
        TraceLog.get().exit(700);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addToSpoolerQueue(AsyncConsumer consumer, Throwable status, int len, Object context) {
        Queue queue = this.queue;
        synchronized (queue) {
            this.queue.add(new SpoolerConsumer(consumer, status, len, context));
            this.queue.notify();
        }
    }

    public String toString() {
        return "Transport [connectionBrokenCallback=" + this.connectionBrokenCallback + ", isNetbios=" + this.isNetbios + ", server=" + this.server + ", connected=" + this.connected + ", getTtl=" + this.server.getTtl() + ", doReceive=" + this.doReceive + ", recvHeaderdata=" + Arrays.toString(this.recvHeaderdata) + ", forceNetbios=" + this.forceNetbios + ", socket=" + this.socket + ", doDisconnect=" + this.doDisconnect + ", waitingDisconect=" + this.waitingDisconect + "]";
    }

    private class Spooler
    implements Runnable {
        private boolean spoolerThreadExit = false;
        SpoolerConsumer spoolerObj = null;

        private Spooler() {
        }

        protected void setSpoolerThreadExit(boolean spoolerThreadExit) {
            this.spoolerThreadExit = spoolerThreadExit;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void run() {
            TraceLog.get().enter(700);
            while (!this.spoolerThreadExit) {
                this.spoolerObj = null;
                Queue queue = Transport.this.queue;
                synchronized (queue) {
                    if (!Transport.this.queue.isEmpty()) {
                        this.spoolerObj = (SpoolerConsumer)Transport.this.queue.poll();
                    } else {
                        try {
                            Transport.this.queue.wait(10000L);
                        }
                        catch (InterruptedException interruptedException) {
                            // empty catch block
                        }
                    }
                }
                if (null != this.spoolerObj && !this.spoolerThreadExit) {
                    try {
                        this.spoolerObj.consumer.complete(this.spoolerObj.status, this.spoolerObj.len, this.spoolerObj.context);
                    }
                    catch (NqException nqException) {}
                    continue;
                }
                if (null == this.spoolerObj || !this.spoolerThreadExit) continue;
                TraceLog.get().message("Exiting Spooler with extra data = " + this.spoolerObj.context, 2000);
            }
            TraceLog.get().exit(700);
        }
    }

    private class SpoolerConsumer {
        AsyncConsumer consumer;
        Throwable status;
        int len;
        Object context;

        SpoolerConsumer(AsyncConsumer consumer, Throwable status, int len, Object context) {
            this.consumer = consumer;
            this.status = status;
            this.len = len;
            this.context = context;
        }
    }

    public class Recv {
        boolean receiving_ = false;
        int recevingRemain_ = 0;

        public boolean isReceiving_() {
            return this.receiving_;
        }

        public void setReceiving_(boolean receiving_) {
            this.receiving_ = receiving_;
        }

        public int getRecevingRemain_() {
            return this.recevingRemain_;
        }

        public void setRecevingRemain_(int recevingRemain_) {
            this.recevingRemain_ = recevingRemain_;
        }
    }

    private class ReceiveThreadBody
    implements Runnable {
        private boolean receiveThreadExit = false;
        private int waitingCounter = 0;
        private static final int MAX_TIMES_FOR_WAITING = 15;
        public final SyncObject syncObj = new SyncObject();

        private ReceiveThreadBody() {
        }

        protected void setReceiveThreadExit(boolean receiveThreadExit) {
            this.receiveThreadExit = receiveThreadExit;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void run() {
            TraceLog.get().enter(700);
            TraceLog.get().message("receiveThread hashCode = " + this.hashCode(), 2000);
            this.receiveThreadExit = false;
            this.waitingCounter = 0;
            while (!this.receiveThreadExit) {
                if (!Transport.this.doReceive) {
                    try {
                        Thread thread = Transport.this.receiveThread;
                        synchronized (thread) {
                            boolean isNotifySent;
                            if (Transport.this.isHardDisconnect) {
                                ((Transport)Transport.this).server.smb.signalAllMatch(Transport.this);
                            }
                            if ((isNotifySent = this.syncObj.syncWait(1000L)) && !Transport.this.doReceive) {
                                this.receiveThreadExit = true;
                                TraceLog.get().message("Transport is not connected", 700);
                            } else if (!isNotifySent) {
                                ++this.waitingCounter;
                                if (15 <= this.waitingCounter) {
                                    this.receiveThreadExit = true;
                                    TraceLog.get().message("Waiting too many times", 700);
                                }
                            }
                            continue;
                        }
                    }
                    catch (InterruptedException e) {
                        continue;
                    }
                }
                this.waitingCounter = 0;
                try {
                    Transport e = Transport.this;
                    synchronized (e) {
                        block30: {
                            if (!Transport.this.isSocketAlive() || Transport.this.doDisconnect) {
                                if (null != Transport.this.connectionBrokenCallback) {
                                    Transport.this.connectionBrokenCallback.connectionBroken(Transport.this);
                                }
                                try {
                                    if (Transport.this.connected) {
                                        Transport.this.socket.close();
                                        Transport.this.connected = false;
                                    }
                                }
                                catch (Exception e2) {
                                    this.receiveThreadExit = true;
                                    TraceLog.get().exit("Caught exception = ", e2, 10);
                                    return;
                                }
                                if (Transport.this.waitingDisconect) {
                                    Transport.this.notifyAll();
                                }
                                this.receiveThreadExit = true;
                                TraceLog.get().exit("Terminating receiveThread for " + this.hashCode(), 700);
                                return;
                            }
                            try {
                                Transport.this.socket.setSoTimeout((int)Client.getSmbTimeout());
                                int len = Transport.this.receivePacketLength();
                                if (0 == len) {
                                    Transport.this.receiving = false;
                                    continue;
                                }
                            }
                            catch (Exception ex) {
                                NetbiosException nbEx;
                                TraceLog.get().error("Exception caught while reading packet length; ex = ", ex, 700, 0);
                                if (ex instanceof NqException) {
                                    NqException nqEx = (NqException)ex;
                                    if (-505 == nqEx.getErrCode()) {
                                        continue;
                                    }
                                } else if (ex instanceof NetbiosException && -507 == (nbEx = (NetbiosException)ex).getErrCode()) {
                                    continue;
                                }
                                if (!Transport.this.connected) break block30;
                                TraceLog.get().error("Socket error: ", ex, 10, 0);
                                Transport.this.doDisconnect = true;
                                if (((Transport)Transport.this).server.isReconnecting || !Transport.this.server.isFindable()) break block30;
                                Transport.this.connected = false;
                                ((Transport)Transport.this).server.smb.signalAllMatch(Transport.this);
                                ((Transport)Transport.this).server.connectionBroke = true;
                            }
                        }
                        TraceLog.get().message("Thread is yielding=" + this.hashCode(), 2000);
                        Thread.yield();
                        TraceLog.get().message("Wakeup from yield", 2000);
                        ((Transport)Transport.this).server.smb.response(Transport.this);
                    }
                }
                catch (Exception ex) {
                    TraceLog.get().error("Exception = ", ex, 10);
                }
            }
            TraceLog.get().exit(700);
        }
    }

    private class ConnectionThread
    implements Runnable {
        TransportType transportType;
        InetAddress ip;
        NetbiosName name;
        final int CONNECTION_TIMEOUT;

        public ConnectionThread(TransportType transportType, InetAddress ip, NetbiosName name) {
            this.transportType = transportType;
            this.ip = ip;
            this.name = name;
            this.CONNECTION_TIMEOUT = (Integer)Config.jnq.getNE("CONNECTION_TIMEOUT");
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void run() {
            TraceLog.get().enter("ip = " + this.ip + "; port = " + this.transportType.port, 700);
            Socket socket = null;
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(this.ip, this.transportType.port), this.CONNECTION_TIMEOUT);
                socket.setSoTimeout((int)Client.getSmbTimeout());
                if (this.transportType.type == 1) {
                    SessionService.startSession(socket, this.ip, this.name);
                    Transport.this.isNetbios = true;
                }
                socket.setTcpNoDelay(true);
                socket.setPerformancePreferences(0, 10, 8);
                Object object = Transport.this.connectionGuard;
                synchronized (object) {
                    if (Transport.this.connected) {
                        if (this.transportType.type == 1) {
                            Transport.this.isNetbios = false;
                        }
                        socket.close();
                        TraceLog.get().exit(700);
                        return;
                    }
                    Transport.this.receiveStream = socket.getInputStream();
                    Transport.this.sendStream = socket.getOutputStream();
                    Transport.this.receiving = true;
                    Transport.this.socket = socket;
                    Transport.this.connectionGuard.notify();
                    Transport.this.connected = true;
                    ((Transport)Transport.this).server.connectionBroke = false;
                }
            }
            catch (SocketTimeoutException ste) {
                TraceLog.get().error("Timeout trying to connect to " + this.ip, 10);
                Transport.this.isConnectionTimedout = true;
                TraceLog.get().caught(ste, 10);
                TraceLog.get().exit(700);
                return;
            }
            catch (IOException e) {
                TraceLog.get().error("Invalid socket - not an error");
                TraceLog.get().exit(700);
                return;
            }
            catch (Exception e) {
                if (this.transportType.type == 1) {
                    Transport.this.isNetbios = false;
                }
                if (null != socket) {
                    try {
                        socket.close();
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                }
                TraceLog.get().error("Exception = ", e);
            }
            TraceLog.get().exit(700);
        }
    }

    private class TransportType {
        protected int type;
        protected int port;

        protected TransportType(int type, int port, boolean netbios) {
            this.type = type;
            this.port = port;
        }
    }

    public static interface ResponseCallback {
        public void response(Transport var1) throws NqException;
    }

    public static interface ConnectionBrokenCallback {
        public void connectionBroken(Transport var1);
    }
}

