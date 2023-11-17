/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh2;

import com.maverick.events.Event;
import com.maverick.events.EventServiceImplementation;
import com.maverick.ssh.AdaptiveConfiguration;
import com.maverick.ssh.ChannelAdapter;
import com.maverick.ssh.PseudoTerminalModes;
import com.maverick.ssh.SshChannel;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshClientListener;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshSession;
import com.maverick.ssh.components.Utils;
import com.maverick.ssh.message.SshChannelMessage;
import com.maverick.ssh.message.SshMessage;
import com.maverick.ssh2.Ssh2Channel;
import com.maverick.ssh2.Ssh2Client;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import java.io.IOException;
import java.io.InputStream;

public class Ssh2Session
extends Ssh2Channel
implements SshSession {
    static final int SSH_EXTENDED_DATA_STDERR = 1;
    Ssh2Channel.ChannelInputStream stderr;
    boolean flowControlEnabled = false;
    int exitcode = Integer.MIN_VALUE;
    String exitsignalinfo = "";
    Ssh2Client client;
    String term = null;
    boolean logCommands;
    boolean verbose;

    public Ssh2Session(int windowsize, int packetsize, Ssh2Client client) {
        super("session", windowsize, packetsize);
        this.client = client;
        this.logCommands = AdaptiveConfiguration.getBoolean("logCommands", false, this.getClient().getHost(), this.getClient().getIdent());
        this.verbose = AdaptiveConfiguration.getBoolean("verbose", false, this.getClient().getHost(), this.getClient().getIdent());
        this.stderr = this.createExtendedDataStream();
    }

    @Override
    public SshClient getClient() {
        return this.client;
    }

    @Override
    public String getTerm() {
        return this.term;
    }

    @Override
    protected void processExtendedData(int typecode, int length, SshChannelMessage msg) throws SshException {
        super.processExtendedData(typecode, length, msg);
        if (typecode == 1) {
            this.stderr.addMessage(length, msg);
        }
    }

    @Override
    public InputStream getStderrInputStream() {
        return this.stderr;
    }

    @Override
    public boolean requestPseudoTerminal(String term, int cols, int rows, int width, int height) throws SshException {
        return this.requestPseudoTerminal(term, cols, rows, width, height, new byte[]{0});
    }

    @Override
    public boolean requestPseudoTerminal(String term, int cols, int rows, int width, int height, PseudoTerminalModes terminalModes) throws SshException {
        return this.requestPseudoTerminal(term, cols, rows, width, height, terminalModes.toByteArray());
    }

    @Override
    public boolean requestPseudoTerminal(String term, int cols, int rows, int width, int height, byte[] modes) throws SshException {
        ByteArrayWriter request = new ByteArrayWriter();
        try {
            if (log.isDebugEnabled()) {
                this.connection.getTransport().debug(log, "Requesting pseudo-terminal term={} cols={} rows={} modes={}", term, cols, rows, Utils.bytesToHex(modes));
            }
            request.writeString(term);
            request.writeInt(cols);
            request.writeInt(rows);
            request.writeInt(width);
            request.writeInt(height);
            request.writeBinaryString(modes);
            boolean success = this.sendRequest("pty-req", true, request.toByteArray());
            if (success) {
                this.term = term;
            }
            boolean bl = success;
            return bl;
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
        finally {
            try {
                request.close();
            }
            catch (IOException iOException) {}
        }
    }

    @Override
    public boolean startShell() throws SshException {
        if (this.verbose || this.logCommands) {
            this.addChannelEventListener(new CommandLogger());
        }
        for (SshClientListener listener : this.client.listeners) {
            try {
                listener.startingShell(this.client, this);
            }
            catch (Throwable throwable) {}
        }
        boolean success = this.sendRequest("shell", true, null);
        if (success) {
            for (SshClientListener listener : this.client.listeners) {
                try {
                    listener.startedShell(this.client, this);
                }
                catch (Throwable throwable) {}
            }
            EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.getClient(), 23, true, this.client.getUuid()).addAttribute("CLIENT", this.getClient()).addAttribute("SESSION", this));
            this.addChannelEventListener(new ChannelAdapter(){

                @Override
                public void channelClosed(SshChannel channel) {
                    for (SshClientListener listener : Ssh2Session.this.client.listeners) {
                        try {
                            listener.shellClosed(Ssh2Session.this.client, Ssh2Session.this);
                        }
                        catch (Throwable throwable) {}
                    }
                }
            });
        } else {
            EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.getClient(), 24, false, this.client.getUuid()).addAttribute("CLIENT", this.getClient()).addAttribute("SESSION", this));
        }
        return success;
    }

    @Override
    public boolean executeCommand(String cmd) throws SshException {
        return this.executeCommand(cmd, "UTF-8");
    }

    @Override
    public boolean executeCommand(final String cmd, String charset) throws SshException {
        if (this.verbose || this.logCommands) {
            this.addChannelEventListener(new CommandLogger());
        }
        ByteArrayWriter request = new ByteArrayWriter();
        try {
            boolean success;
            for (SshClientListener listener : this.client.listeners) {
                try {
                    listener.executingCommand(this.client, this, cmd);
                }
                catch (Throwable throwable) {}
            }
            request.writeString(cmd, charset);
            if (this.logCommands || this.verbose || log.isDebugEnabled()) {
                this.connection.getTransport().info(log, "Executing: " + cmd, new Object[0]);
            }
            if (success = this.sendRequest("exec", true, request.toByteArray())) {
                for (SshClientListener listener : this.client.listeners) {
                    try {
                        listener.executedCommand(this.client, this, cmd);
                    }
                    catch (Throwable throwable) {}
                }
                EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.getClient(), 30, true, this.client.getUuid()).addAttribute("COMMAND", cmd).addAttribute("CLIENT", this.getClient()).addAttribute("SESSION", this));
                this.addChannelEventListener(new ChannelAdapter(){

                    @Override
                    public void channelClosed(SshChannel channel) {
                        for (SshClientListener listener : Ssh2Session.this.client.listeners) {
                            try {
                                listener.commandExecuted(Ssh2Session.this.client, Ssh2Session.this, cmd, Ssh2Session.this.exitCode());
                            }
                            catch (Throwable throwable) {}
                        }
                    }
                });
            } else {
                EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.getClient(), 30, false, this.client.getUuid()).addAttribute("COMMAND", cmd).addAttribute("CLIENT", this.getClient()).addAttribute("SESSION", this));
            }
            boolean bl = success;
            return bl;
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
        finally {
            try {
                request.close();
            }
            catch (IOException iOException) {}
        }
    }

    public boolean startSubsystem(final String subsystem) throws SshException {
        ByteArrayWriter request = new ByteArrayWriter();
        try {
            for (SshClientListener listener : this.client.listeners) {
                try {
                    listener.startingSubsystem(this.client, this, subsystem);
                }
                catch (Throwable throwable) {}
            }
            request.writeString(subsystem);
            boolean success = this.sendRequest("subsystem", true, request.toByteArray());
            if (success) {
                for (SshClientListener listener : this.client.listeners) {
                    try {
                        listener.startedSubsystem(this.client, this, subsystem);
                    }
                    catch (Throwable throwable) {}
                }
                EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.getClient(), 1001, true, this.client.getUuid()).addAttribute("COMMAND", subsystem).addAttribute("CLIENT", this.getClient()).addAttribute("SESSION", this));
                this.addChannelEventListener(new ChannelAdapter(){

                    @Override
                    public void channelClosed(SshChannel channel) {
                        for (SshClientListener listener : Ssh2Session.this.client.listeners) {
                            try {
                                listener.subsystemClosed(Ssh2Session.this.client, Ssh2Session.this, subsystem);
                            }
                            catch (Throwable throwable) {}
                        }
                    }
                });
            } else {
                EventServiceImplementation.getInstance().fireEvent(new Event((Object)this.getClient(), 1001, false, this.client.getUuid()).addAttribute("COMMAND", subsystem).addAttribute("CLIENT", this.getClient()).addAttribute("SESSION", this));
            }
            boolean bl = success;
            return bl;
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
        finally {
            try {
                request.close();
            }
            catch (IOException iOException) {}
        }
    }

    boolean requestX11Forwarding(boolean singleconnection, String protocol, String cookie, int screen) throws SshException {
        ByteArrayWriter request = new ByteArrayWriter();
        try {
            request.writeBoolean(singleconnection);
            request.writeString(protocol);
            request.writeString(cookie);
            request.writeInt(screen);
            boolean bl = this.sendRequest("x11-req", true, request.toByteArray());
            return bl;
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
        finally {
            try {
                request.close();
            }
            catch (IOException iOException) {}
        }
    }

    public boolean setEnvironmentVariable(String name, String value) throws SshException {
        ByteArrayWriter request = new ByteArrayWriter();
        try {
            request.writeString(name);
            request.writeString(value);
            boolean bl = this.sendRequest("env", true, request.toByteArray());
            return bl;
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
        finally {
            try {
                request.close();
            }
            catch (IOException iOException) {}
        }
    }

    @Override
    public void changeTerminalDimensions(int cols, int rows, int width, int height) throws SshException {
        ByteArrayWriter request = new ByteArrayWriter();
        try {
            request.writeInt(cols);
            request.writeInt(rows);
            request.writeInt(width);
            request.writeInt(height);
            this.sendRequest("window-change", false, request.toByteArray());
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
        finally {
            try {
                request.close();
            }
            catch (IOException iOException) {}
        }
    }

    public boolean isFlowControlEnabled() {
        return this.flowControlEnabled;
    }

    public void signal(String signal) throws SshException {
        ByteArrayWriter request = new ByteArrayWriter();
        try {
            request.writeString(signal);
            this.sendRequest("signal", false, request.toByteArray());
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
        finally {
            try {
                request.close();
            }
            catch (IOException iOException) {}
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void channelRequest(String requesttype, boolean wantreply, byte[] requestdata) throws SshException {
        try {
            if (requesttype.equals("exit-status") && requestdata != null) {
                this.exitcode = (int)ByteArrayReader.readInt(requestdata, 0);
                if (log.isDebugEnabled()) {
                    this.connection.getTransport().debug(log, "Remote process exited with status code " + this.exitcode, new Object[0]);
                }
            }
            if (requesttype.equals("exit-signal") && requestdata != null) {
                try (ByteArrayReader bar = new ByteArrayReader(requestdata, 0, requestdata.length);){
                    this.exitsignalinfo = "Signal=" + bar.readString() + " CoreDump=" + String.valueOf(bar.read() != 0) + " Message=" + bar.readString();
                }
            }
            if (requesttype.equals("xon-xoff")) {
                this.flowControlEnabled = requestdata != null && requestdata[0] != 0;
            }
            super.channelRequest(requesttype, wantreply, requestdata);
        }
        catch (IOException ex) {
            throw new SshException(ex, 5);
        }
    }

    @Override
    public int exitCode() {
        return this.exitcode;
    }

    @Override
    protected void checkCloseStatus(boolean remoteClosed) {
        block12: {
            if (this.state != 3) {
                if (log.isDebugEnabled()) {
                    this.connection.getTransport().debug(log, "Local state of channel is not closed id=" + this.channelid + " rid=" + this.remoteid, new Object[0]);
                }
                this.close();
            }
            if (!remoteClosed) {
                if (log.isDebugEnabled()) {
                    this.connection.getTransport().debug(log, "Checking remote channel is not already closed id=" + this.channelid + " rid=" + this.remoteid, new Object[0]);
                }
                boolean bl = remoteClosed = this.ms.hasMessage(this.CHANNEL_CLOSE_MESSAGES) != null;
            }
            if (!remoteClosed) {
                try {
                    if (log.isDebugEnabled()) {
                        this.connection.getTransport().debug(log, "Waiting for remote channel close id=" + this.channelid + " rid=" + this.remoteid, new Object[0]);
                    }
                    if (!AdaptiveConfiguration.getBoolean("blockForRemoteClose", true, this.getClient().getHost(), this.getClient().getIdent())) break block12;
                    int timeout = AdaptiveConfiguration.getInt("remoteCloseTimeoutMs", 5000, this.getClient().getHost(), this.getClient().getIdent());
                    SshMessage message = null;
                    if (log.isDebugEnabled()) {
                        this.connection.getTransport().debug(log, "Blocking for channel close id=" + this.channelid + " rid=" + this.remoteid + " timeout=" + timeout, new Object[0]);
                    }
                    if ((message = this.ms.nextMessage(this.CHANNEL_CLOSE_MESSAGES, timeout)) != null) {
                        remoteClosed = true;
                        if (log.isDebugEnabled()) {
                            this.connection.getTransport().debug(log, "Remote channel has been closed id=" + this.channelid + " rid=" + this.remoteid, new Object[0]);
                        }
                        this.free();
                    }
                }
                catch (Exception e) {
                    this.connection.getTransport().error(log, "An error was generated whilst we were waiting for the remote channel to close", e);
                }
            } else {
                this.free();
            }
        }
    }

    public boolean hasExitSignal() {
        return !this.exitsignalinfo.equals("");
    }

    public String getExitSignalInfo() {
        return this.exitsignalinfo;
    }

    class CommandLogger
    extends ChannelAdapter {
        CommandLogger() {
        }

        @Override
        public void dataReceived(SshChannel channel, byte[] buf, int off, int len) {
            String str;
            if ((Ssh2Session.this.verbose || Ssh2Session.this.logCommands || Ssh2Channel.log.isDebugEnabled()) && (str = new String(buf, off, len).trim()).length() > 0) {
                Ssh2Session.this.connection.getTransport().info(Ssh2Channel.log, "Session IN: " + str, new Object[0]);
            }
        }

        @Override
        public void dataSent(SshChannel channel, byte[] buf, int off, int len) {
            String str;
            if ((Ssh2Session.this.verbose || Ssh2Session.this.logCommands || Ssh2Channel.log.isDebugEnabled()) && (str = new String(buf, off, len).trim()).length() > 0) {
                Ssh2Session.this.connection.getTransport().info(Ssh2Channel.log, "Session OUT: " + str, new Object[0]);
            }
        }
    }
}

