/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.sshtools.net;

import com.maverick.events.Event;
import com.maverick.events.EventServiceImplementation;
import com.maverick.ssh.ChannelAdapter;
import com.maverick.ssh.Client;
import com.maverick.ssh.ForwardingRequestListener;
import com.maverick.ssh.SshChannel;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshTransport;
import com.maverick.ssh.SshTunnel;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.IOStreamConnector;
import com.sshtools.net.ForwardingClientListener;
import com.sshtools.net.SocketTransport;
import com.sshtools.net.SocketWrapper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForwardingClient
implements Client {
    static Logger log = LoggerFactory.getLogger(ForwardingClient.class);
    SshClient ssh;
    protected Map<String, Vector<ActiveTunnel>> incomingtunnels = new HashMap<String, Vector<ActiveTunnel>>();
    protected Map<String, String> remoteforwardings = new HashMap<String, String>();
    protected Map<String, Vector<ActiveTunnel>> outgoingtunnels = new HashMap<String, Vector<ActiveTunnel>>();
    protected Map<String, SocketListener> socketlisteners = new HashMap<String, SocketListener>();
    protected Vector<ForwardingClientListener> clientlisteners = new Vector();
    ForwardingListener forwardinglistener = new ForwardingListener();
    TunnelListener tunnellistener = new TunnelListener();
    public static final String X11_KEY = "X11";
    boolean isXForwarding = false;
    public final int LOWEST_RANDOM_PORT = 49152;
    public final int HIGHEST_RANDOM_PORT = 65535;

    public ForwardingClient(SshClient ssh) {
        this.ssh = ssh;
    }

    public void addListener(ForwardingClientListener listener) {
        if (listener != null) {
            this.clientlisteners.addElement(listener);
            for (SocketListener s : this.socketlisteners.values()) {
                if (!s.isListening()) continue;
                listener.forwardingStarted(1, this.generateKey(s.addressToBind, s.portToBind), s.hostToConnect, s.portToConnect);
            }
            if (log.isDebugEnabled()) {
                log.debug("enumerated socketlisteners");
            }
            for (String key : this.incomingtunnels.keySet()) {
                if (key.equals(X11_KEY) || this.ssh.getContext().getX11Display() != null && this.ssh.getContext().getX11Display().equals(key)) continue;
                String destination = this.remoteforwardings.get(key);
                String hostToConnect = destination.substring(0, destination.indexOf(58));
                int portToConnect = Integer.parseInt(destination.substring(destination.indexOf(58) + 1));
                listener.forwardingStarted(2, key, hostToConnect, portToConnect);
            }
            if (log.isDebugEnabled()) {
                log.debug("enumerated incomingtunnels");
            }
            String display = this.ssh.getContext().getX11Display();
            if (log.isDebugEnabled()) {
                log.debug("display is " + display);
            }
            if (display != null && this.isXForwarding) {
                int screen;
                String hostname = "localhost";
                int idx = display.indexOf(58);
                if (idx != -1) {
                    hostname = display.substring(0, idx);
                    screen = Integer.parseInt(display.substring(idx + 1));
                } else {
                    screen = Integer.parseInt(display);
                }
                listener.forwardingStarted(3, X11_KEY, hostname, screen);
            }
        }
    }

    public synchronized boolean hasRemoteForwarding(String addressBound, int portBound) {
        return this.remoteforwardings.containsKey(this.generateKey(addressBound, portBound));
    }

    public synchronized boolean hasLocalForwarding(String addressBound, int portBound) {
        return this.socketlisteners.containsKey(this.generateKey(addressBound, portBound));
    }

    public void removeListener(ForwardingClientListener listener) {
        this.clientlisteners.removeElement(listener);
    }

    public synchronized int startLocalForwarding(String addressToBind, int portToBind, String hostToConnect, int portToConnect) throws SshException {
        SocketListener listener = new SocketListener(addressToBind, portToBind, hostToConnect, portToConnect);
        listener.start();
        portToBind = listener.getLocalPort();
        String key = this.generateKey(addressToBind, portToBind);
        this.socketlisteners.put(key, listener);
        if (!this.outgoingtunnels.containsKey(key)) {
            this.outgoingtunnels.put(key, new Vector());
        }
        for (int i = 0; i < this.clientlisteners.size(); ++i) {
            this.clientlisteners.elementAt(i).forwardingStarted(1, key, hostToConnect, portToConnect);
        }
        EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.ssh, 16, true, this.ssh.getUuid()).addAttribute("CLIENT", this.ssh).addAttribute("FORWARDING_TUNNEL_ENTRANCE", key).addAttribute("FORWARDING_TUNNEL_EXIT", hostToConnect + ":" + portToConnect));
        return portToBind;
    }

    public String[] getRemoteForwardings() {
        String[] r = new String[this.remoteforwardings.size() - (this.remoteforwardings.containsKey(X11_KEY) ? 1 : 0)];
        int index = 0;
        for (String key : this.remoteforwardings.keySet()) {
            if (key.equals(X11_KEY)) continue;
            r[index++] = key;
        }
        return r;
    }

    public String[] getLocalForwardings() {
        String[] r = new String[this.socketlisteners.size()];
        int index = 0;
        for (String str : this.socketlisteners.keySet()) {
            r[index++] = str;
        }
        return r;
    }

    public ActiveTunnel[] getLocalForwardingTunnels(String key) throws IOException {
        if (this.outgoingtunnels.containsKey(key)) {
            Vector<ActiveTunnel> v = this.outgoingtunnels.get(key);
            Object[] t = new ActiveTunnel[v.size()];
            v.copyInto(t);
            return t;
        }
        return new ActiveTunnel[0];
    }

    public ActiveTunnel[] getLocalForwardingTunnels(String addressToBind, int portToBind) throws IOException {
        return this.getLocalForwardingTunnels(this.generateKey(addressToBind, portToBind));
    }

    public ActiveTunnel[] getRemoteForwardingTunnels() throws IOException {
        Vector<ActiveTunnel> v = new Vector<ActiveTunnel>();
        String[] remoteForwardings = this.getRemoteForwardings();
        for (int i = 0; i < remoteForwardings.length; ++i) {
            ActiveTunnel[] tmp = this.getRemoteForwardingTunnels(remoteForwardings[i]);
            for (int x = 0; x < tmp.length; ++x) {
                v.add(tmp[x]);
            }
        }
        return v.toArray(new ActiveTunnel[v.size()]);
    }

    public ActiveTunnel[] getLocalForwardingTunnels() throws IOException {
        Vector<ActiveTunnel> v = new Vector<ActiveTunnel>();
        for (String key : this.outgoingtunnels.keySet()) {
            ActiveTunnel[] tmp = this.getLocalForwardingTunnels(key);
            for (int x = 0; x < tmp.length; ++x) {
                v.add(tmp[x]);
            }
        }
        return v.toArray(new ActiveTunnel[v.size()]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ActiveTunnel[] getRemoteForwardingTunnels(String key) throws IOException {
        Map<String, Vector<ActiveTunnel>> map = this.incomingtunnels;
        synchronized (map) {
            if (this.incomingtunnels.containsKey(key)) {
                Vector<ActiveTunnel> v = this.incomingtunnels.get(key);
                Object[] t = new ActiveTunnel[v.size()];
                v.copyInto(t);
                return t;
            }
        }
        return new ActiveTunnel[0];
    }

    public boolean isXForwarding() {
        return this.isXForwarding;
    }

    public ActiveTunnel[] getRemoteForwardingTunnels(String addressToBind, int portToBind) throws IOException {
        return this.getRemoteForwardingTunnels(this.generateKey(addressToBind, portToBind));
    }

    public ActiveTunnel[] getX11ForwardingTunnels() throws IOException {
        if (this.incomingtunnels.containsKey(X11_KEY)) {
            Vector<ActiveTunnel> v = this.incomingtunnels.get(X11_KEY);
            Object[] t = new ActiveTunnel[v.size()];
            v.copyInto(t);
            return t;
        }
        return new ActiveTunnel[0];
    }

    public synchronized int requestRemoteForwarding(String addressToBind, int portToBind, String hostToConnect, int portToConnect) throws SshException {
        int boundPort = this.ssh.requestRemoteForwarding(addressToBind, portToBind, hostToConnect, portToConnect, this.forwardinglistener);
        if (boundPort > 0) {
            String key = this.generateKey(addressToBind, portToBind);
            if (!this.incomingtunnels.containsKey(key)) {
                this.incomingtunnels.put(key, new Vector());
            }
            this.remoteforwardings.put(key, hostToConnect + ":" + portToConnect);
            for (int i = 0; i < this.clientlisteners.size(); ++i) {
                this.clientlisteners.elementAt(i).forwardingStarted(2, key, hostToConnect, portToConnect);
            }
            return boundPort;
        }
        return boundPort;
    }

    public void allowX11Forwarding(String display, String magicCookie) throws SshException {
        if (this.remoteforwardings.containsKey(X11_KEY)) {
            throw new SshException("X11 forwarding is already in use!", 14);
        }
        if (!this.incomingtunnels.containsKey(X11_KEY)) {
            this.incomingtunnels.put(X11_KEY, new Vector());
        }
        this.ssh.getContext().setX11Display(display);
        this.ssh.getContext().setX11RequestListener(this.forwardinglistener);
        byte[] cookie = new byte[16];
        if (magicCookie.length() != 32) {
            throw new SshException("Invalid MIT-MAGIC_COOKIE-1 value " + magicCookie, 14);
        }
        for (int i = 0; i < 32; i += 2) {
            cookie[i / 2] = (byte)Integer.parseInt(magicCookie.substring(i, i + 2), 16);
        }
        this.ssh.getContext().setX11RealCookie(cookie);
        String hostname = "localhost";
        int screen = 0;
        int idx = display.indexOf(58);
        if (idx != -1) {
            hostname = display.substring(0, idx);
            display = display.substring(idx + 1);
        }
        if ((idx = display.indexOf(46)) > -1) {
            screen = Integer.parseInt(display.substring(idx + 1));
        }
        for (int i = 0; i < this.clientlisteners.size(); ++i) {
            this.clientlisteners.elementAt(i).forwardingStarted(3, X11_KEY, hostname, screen);
        }
        this.isXForwarding = true;
    }

    public void allowX11Forwarding(String display) throws SshException {
        String homeDir = "";
        try {
            homeDir = System.getProperty("user.home");
        }
        catch (SecurityException securityException) {
            // empty catch block
        }
        this.allowX11Forwarding(display, new File(homeDir, ".Xauthority"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized void allowX11Forwarding(String display, File f) throws SshException {
        if (this.remoteforwardings.containsKey(X11_KEY)) {
            throw new SshException("X11 forwarding is already in use!", 14);
        }
        if (!this.incomingtunnels.containsKey(X11_KEY)) {
            this.incomingtunnels.put(X11_KEY, new Vector());
        }
        this.ssh.getContext().setX11Display(display);
        this.ssh.getContext().setX11RequestListener(this.forwardinglistener);
        try {
            int idx;
            int screen;
            String hostname;
            if (f.exists()) {
                int read;
                hostname = "";
                screen = 0;
                idx = display.indexOf(58);
                if (idx != -1) {
                    hostname = display.substring(0, idx);
                    screen = Integer.parseInt(display.substring(idx + 1));
                }
                FileInputStream in = new FileInputStream(f);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while ((read = in.read()) != -1) {
                    out.write(read);
                }
                in.close();
                byte[] tmp = out.toByteArray();
                try (ByteArrayReader bar = new ByteArrayReader(tmp);){
                    while (bar.available() > 0) {
                        short family = bar.readShort();
                        short len = bar.readShort();
                        byte[] address = new byte[len];
                        bar.read(address);
                        len = bar.readShort();
                        byte[] number = new byte[len];
                        bar.read(number);
                        len = bar.readShort();
                        byte[] name = new byte[len];
                        bar.read(name);
                        len = bar.readShort();
                        byte[] data = new byte[len];
                        bar.read(data);
                        String n = new String(number);
                        int d = Integer.parseInt(n);
                        String protocol = new String(name);
                        if (!protocol.equals("MIT-MAGIC-COOKIE-1")) continue;
                        if (family == 0) {
                            String ip = (address[0] & 0xFF) + "." + (address[1] & 0xFF) + "." + (address[2] & 0xFF) + "." + (address[3] & 0xFF);
                            InetAddress addr = InetAddress.getByName(ip);
                            if (!addr.getHostAddress().equals(hostname) && !addr.getHostName().equals(hostname) || screen != d) continue;
                            this.ssh.getContext().setX11RealCookie(data);
                        } else {
                            String h;
                            if (family != 256 || !(h = new String(address)).equals(hostname) || screen != d) continue;
                            this.ssh.getContext().setX11RealCookie(data);
                        }
                        break;
                    }
                }
            }
            hostname = "localhost";
            screen = 0;
            idx = display.indexOf(58);
            if (idx != -1) {
                hostname = display.substring(0, idx);
                display = display.substring(idx + 1);
            }
            if ((idx = display.indexOf(46)) > -1) {
                screen = Integer.parseInt(display.substring(idx + 1));
            }
            for (int i = 0; i < this.clientlisteners.size(); ++i) {
                this.clientlisteners.elementAt(i).forwardingStarted(3, X11_KEY, hostname, screen);
            }
            this.isXForwarding = true;
        }
        catch (IOException ioe) {
            throw new SshException(ioe.getMessage(), 14);
        }
    }

    public void cancelRemoteForwarding(String bindAddress, int bindPort) throws SshException {
        this.cancelRemoteForwarding(bindAddress, bindPort, false);
    }

    public synchronized void cancelRemoteForwarding(String bindAddress, int bindPort, boolean killActiveTunnels) throws SshException {
        String key = this.generateKey(bindAddress, bindPort);
        boolean killedTunnels = false;
        if (killActiveTunnels) {
            try {
                ActiveTunnel[] tunnels = this.getRemoteForwardingTunnels(bindAddress, bindPort);
                if (tunnels != null) {
                    for (int i = 0; i < tunnels.length; ++i) {
                        if (tunnels[i] == null) continue;
                        tunnels[i].stop();
                        killedTunnels = true;
                    }
                }
            }
            catch (IOException tunnels) {
                // empty catch block
            }
            this.incomingtunnels.remove(key);
        }
        if (!this.remoteforwardings.containsKey(key)) {
            if (killActiveTunnels && killedTunnels) {
                return;
            }
            throw new SshException("Remote forwarding has not been started on " + key, 14);
        }
        if (this.ssh == null) {
            return;
        }
        this.ssh.cancelRemoteForwarding(bindAddress, bindPort);
        String destination = this.remoteforwardings.get(key);
        int idx = destination.indexOf(":");
        if (idx == -1) {
            throw new SshException("Invalid port reference in remote forwarding key!", 5);
        }
        String hostToConnect = destination.substring(0, idx);
        int portToConnect = Integer.parseInt(destination.substring(idx + 1));
        for (int i = 0; i < this.clientlisteners.size(); ++i) {
            if (this.clientlisteners.elementAt(i) == null) continue;
            this.clientlisteners.elementAt(i).forwardingStopped(2, key, hostToConnect, portToConnect);
        }
        this.remoteforwardings.remove(key);
    }

    public synchronized void cancelAllRemoteForwarding() throws SshException {
        this.cancelAllRemoteForwarding(false);
    }

    public synchronized void cancelAllRemoteForwarding(boolean killActiveTunnels) throws SshException {
        if (this.remoteforwardings == null) {
            return;
        }
        for (String host : this.remoteforwardings.keySet()) {
            if (host == null) {
                return;
            }
            try {
                int idx = host.indexOf(58);
                int port = -1;
                if (idx == -1) {
                    port = Integer.parseInt(host);
                    host = "";
                } else {
                    port = Integer.parseInt(host.substring(idx + 1));
                    host = host.substring(0, idx);
                }
                this.cancelRemoteForwarding(host, port, killActiveTunnels);
            }
            catch (NumberFormatException numberFormatException) {}
        }
    }

    public synchronized void stopAllLocalForwarding() throws SshException {
        this.stopAllLocalForwarding(false);
    }

    public synchronized void stopAllLocalForwarding(boolean killActiveTunnels) throws SshException {
        for (String str : new ArrayList<String>(this.socketlisteners.keySet())) {
            this.stopLocalForwarding(str, killActiveTunnels);
        }
    }

    public synchronized void stopLocalForwarding(String bindAddress, int bindPort) throws SshException {
        this.stopLocalForwarding(bindAddress, bindPort, false);
    }

    public synchronized void stopLocalForwarding(String bindAddress, int bindPort, boolean killActiveTunnels) throws SshException {
        String key = this.generateKey(bindAddress, bindPort);
        this.stopLocalForwarding(key, killActiveTunnels);
    }

    public synchronized void stopLocalForwarding(String key, boolean killActiveTunnels) throws SshException {
        int i;
        if (key == null) {
            return;
        }
        boolean killedTunnels = false;
        if (killActiveTunnels) {
            try {
                ActiveTunnel[] tunnels = this.getLocalForwardingTunnels(key);
                if (tunnels != null) {
                    for (i = 0; i < tunnels.length; ++i) {
                        if (tunnels[i] == null) continue;
                        tunnels[i].stop();
                        killedTunnels = true;
                    }
                }
            }
            catch (IOException tunnels) {
                // empty catch block
            }
            this.outgoingtunnels.remove(key);
        }
        if (!this.socketlisteners.containsKey(key)) {
            if (killActiveTunnels && killedTunnels) {
                return;
            }
            throw new SshException("Local forwarding has not been started for " + key, 14);
        }
        SocketListener listener = this.socketlisteners.get(key);
        listener.stop();
        this.socketlisteners.remove(key);
        for (i = 0; i < this.clientlisteners.size(); ++i) {
            if (this.clientlisteners.elementAt(i) == null) continue;
            this.clientlisteners.elementAt(i).forwardingStopped(1, key, listener.hostToConnect, listener.portToConnect);
        }
        EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.ssh, 18, true, this.ssh.getUuid()).addAttribute("CLIENT", this.ssh).addAttribute("FORWARDING_TUNNEL_ENTRANCE", key).addAttribute("FORWARDING_TUNNEL_EXIT", listener.hostToConnect + ":" + listener.portToConnect));
    }

    String generateKey(String host, int port) {
        return host.equals("") ? String.valueOf(port) : host + ":" + String.valueOf(port);
    }

    @Override
    public void exit() throws SshException {
        this.stopAllLocalForwarding();
        this.cancelAllRemoteForwarding();
    }

    protected class SocketListener
    implements Runnable {
        String addressToBind;
        int portToBind;
        String hostToConnect;
        int portToConnect;
        ServerSocket server;
        private Thread thread;
        private boolean listening;

        public SocketListener(String addressToBind, int portToBind, String hostToConnect, int portToConnect) {
            this.addressToBind = addressToBind;
            this.portToBind = portToBind;
            this.hostToConnect = hostToConnect;
            this.portToConnect = portToConnect;
        }

        public int getLocalPort() {
            return this.server == null ? -1 : this.server.getLocalPort();
        }

        public boolean isListening() {
            return this.listening;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            try {
                this.listening = true;
                while (this.listening && ForwardingClient.this.ssh.isConnected()) {
                    final Socket socket = this.server.accept();
                    if (!this.listening) break;
                    if (socket == null) {
                        break;
                    }
                    boolean accepted = true;
                    for (ForwardingClientListener l : ForwardingClient.this.clientlisteners) {
                        if (l.checkLocalSourceAddress(socket.getRemoteSocketAddress(), this.addressToBind, this.portToBind, this.hostToConnect, this.portToConnect)) continue;
                        accepted = false;
                    }
                    if (!accepted) {
                        if (log.isDebugEnabled()) {
                            log.debug("Forwarding listener declined to accept socket from " + socket.getRemoteSocketAddress());
                        }
                        try {
                            socket.close();
                        }
                        catch (Exception exception) {}
                        continue;
                    }
                    Thread t = new Thread(){

                        @Override
                        public void run() {
                            try {
                                ForwardingClient.this.ssh.openForwardingChannel(SocketListener.this.hostToConnect, SocketListener.this.portToConnect, SocketListener.this.addressToBind, SocketListener.this.portToBind, socket.getInetAddress().getHostAddress(), socket.getPort(), new SocketWrapper(socket), ForwardingClient.this.tunnellistener);
                                socket.setSoTimeout(30000);
                                socket.setTcpNoDelay(true);
                            }
                            catch (Exception ex) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Exception whilst opening channel", (Throwable)ex);
                                }
                                try {
                                    socket.close();
                                }
                                catch (IOException i) {
                                    for (int i2 = 0; i2 < ForwardingClient.this.clientlisteners.size(); ++i2) {
                                        ForwardingClient.this.clientlisteners.elementAt(i2).channelFailure(1, SocketListener.this.addressToBind + ":" + SocketListener.this.portToBind, SocketListener.this.hostToConnect, SocketListener.this.portToConnect, ForwardingClient.this.ssh.isConnected(), ex);
                                    }
                                }
                                finally {
                                    for (int i = 0; i < ForwardingClient.this.clientlisteners.size(); ++i) {
                                        ForwardingClient.this.clientlisteners.elementAt(i).channelFailure(1, SocketListener.this.addressToBind + ":" + SocketListener.this.portToBind, SocketListener.this.hostToConnect, SocketListener.this.portToConnect, ForwardingClient.this.ssh.isConnected(), ex);
                                    }
                                }
                            }
                        }
                    };
                    t.start();
                }
            }
            catch (IOException iOException) {
            }
            finally {
                this.stop();
                this.server = null;
                this.thread = null;
            }
        }

        public boolean isRunning() {
            return this.thread != null && this.thread.isAlive();
        }

        public String getHostToConnect() {
            return this.hostToConnect;
        }

        public int getPortToConnect() {
            return this.portToConnect;
        }

        public void start() throws SshException {
            try {
                this.server = new ServerSocket(this.portToBind, 1000, this.addressToBind.equals("") ? null : InetAddress.getByName(this.addressToBind));
                this.thread = new Thread(this);
                this.thread.setDaemon(true);
                this.thread.setName("SocketListener " + this.addressToBind + ":" + String.valueOf(this.portToBind));
                this.thread.start();
            }
            catch (IOException ioe) {
                throw new SshException("Failed to local forwarding server. ", 6, ioe);
            }
        }

        public void stop() {
            try {
                if (this.server != null) {
                    this.server.close();
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
            this.listening = false;
        }
    }

    public class ActiveTunnel {
        SshTunnel channel;
        IOStreamConnector tx;
        IOStreamConnector rx;
        IOStreamListener listener = new IOStreamListener();

        ActiveTunnel(SshTunnel channel) {
            this.channel = channel;
        }

        SshTunnel getChannel() {
            return this.channel;
        }

        void start() throws IOException {
            try {
                Map<String, Vector<ActiveTunnel>> owner;
                for (int i = 0; i < ForwardingClient.this.clientlisteners.size(); ++i) {
                    ForwardingClient.this.clientlisteners.elementAt(i).channelOpened(this.channel.isLocal() ? 1 : (this.channel.isX11() ? 3 : 2), this.channel.isX11() ? ForwardingClient.X11_KEY : ForwardingClient.this.generateKey(this.channel.getListeningAddress(), this.channel.getListeningPort()), this.channel);
                }
                this.rx = new IOStreamConnector();
                this.rx.addListener(this.listener);
                this.rx.connect(this.channel.getInputStream(), this.channel.getTransport().getOutputStream());
                this.tx = new IOStreamConnector();
                this.tx.addListener(this.listener);
                this.tx.connect(this.channel.getTransport().getInputStream(), this.channel.getOutputStream());
                String key = ForwardingClient.this.generateKey(this.channel.getListeningAddress(), this.channel.getListeningPort());
                Map<String, Vector<ActiveTunnel>> map = owner = this.channel.isLocal() ? ForwardingClient.this.outgoingtunnels : ForwardingClient.this.incomingtunnels;
                if (!owner.containsKey(key)) {
                    owner.put(key, new Vector());
                }
                Vector<ActiveTunnel> tunnels = owner.get(key);
                tunnels.addElement(this);
            }
            catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception whilst opening channel", (Throwable)ex);
                }
                try {
                    this.channel.close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                throw new IOException("The tunnel failed to start: " + ex.getMessage());
            }
        }

        public synchronized void stop() {
            if (!this.rx.isClosed()) {
                this.rx.close();
            }
            if (!this.tx.isClosed()) {
                this.tx.close();
            }
            String key = ForwardingClient.this.generateKey(this.channel.getListeningAddress(), this.channel.getListeningPort());
            Map<String, Vector<ActiveTunnel>> owner = this.channel.isLocal() ? ForwardingClient.this.outgoingtunnels : ForwardingClient.this.incomingtunnels;
            Vector<ActiveTunnel> tunnels = owner.get(key);
            if (tunnels != null && tunnels.contains(this)) {
                tunnels.removeElement(this);
                for (int i = 0; i < ForwardingClient.this.clientlisteners.size(); ++i) {
                    ForwardingClient.this.clientlisteners.elementAt(i).channelClosed(this.channel.isLocal() ? 1 : (this.channel.isX11() ? 3 : 2), this.channel.isX11() ? ForwardingClient.X11_KEY : key, this.channel);
                }
            }
        }

        class IOStreamListener
        implements IOStreamConnector.IOStreamConnectorListener {
            IOStreamListener() {
            }

            @Override
            public synchronized void connectorClosed(IOStreamConnector connector) {
                if (log.isDebugEnabled()) {
                    log.debug("Tunnel connector closed id=" + ActiveTunnel.this.channel.getChannelId() + " localEOF=" + ActiveTunnel.this.channel.isLocalEOF() + " remoteEOF=" + ActiveTunnel.this.channel.isRemoteEOF() + " closed=" + ActiveTunnel.this.channel.isClosed());
                }
                if (!ActiveTunnel.this.channel.isClosed()) {
                    try {
                        ActiveTunnel.this.channel.getTransport().close();
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                    try {
                        ActiveTunnel.this.channel.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                ActiveTunnel.this.stop();
            }

            @Override
            public void dataTransfered(byte[] buffer, int count) {
            }

            @Override
            public void connectorTimeout(IOStreamConnector connector) {
                if (log.isDebugEnabled()) {
                    log.debug("IO timeout detected in tunnel id=" + ActiveTunnel.this.channel.getChannelId() + " localEOF=" + ActiveTunnel.this.channel.isLocalEOF() + " remoteEOF=" + ActiveTunnel.this.channel.isRemoteEOF() + " closed=" + ActiveTunnel.this.channel.isClosed());
                }
                if (ActiveTunnel.this.channel.isLocalEOF() || ActiveTunnel.this.channel.isRemoteEOF()) {
                    try {
                        ActiveTunnel.this.channel.close();
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                }
            }
        }
    }

    class TunnelListener
    extends ChannelAdapter {
        TunnelListener() {
        }

        @Override
        public void channelOpened(SshChannel channel) {
            if (channel instanceof SshTunnel) {
                ActiveTunnel t = new ActiveTunnel((SshTunnel)channel);
                try {
                    t.start();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
        }
    }

    protected class ForwardingListener
    implements ForwardingRequestListener {
        protected ForwardingListener() {
        }

        @Override
        public SshTransport createConnection(String hostToConnect, int portToConnect) throws SshException {
            try {
                SocketTransport t = new SocketTransport(hostToConnect, portToConnect);
                t.setSoTimeout(30000);
                return t;
            }
            catch (IOException ex) {
                for (int i = 0; i < ForwardingClient.this.clientlisteners.size(); ++i) {
                    ForwardingClient.this.clientlisteners.elementAt(i).channelFailure(2, hostToConnect + ":" + portToConnect, hostToConnect, portToConnect, ForwardingClient.this.ssh.isConnected(), ex);
                }
                throw new SshException("Failed to connect", 10);
            }
        }

        @Override
        public void initializeTunnel(SshTunnel tunnel) {
            tunnel.addChannelEventListener(ForwardingClient.this.tunnellistener);
        }
    }
}

