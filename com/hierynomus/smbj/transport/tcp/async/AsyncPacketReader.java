/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.transport.tcp.async;

import com.hierynomus.protocol.PacketData;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.protocol.transport.PacketFactory;
import com.hierynomus.protocol.transport.PacketReceiver;
import com.hierynomus.smbj.transport.PacketReader;
import com.hierynomus.smbj.transport.tcp.async.PacketBufferReader;
import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncPacketReader<D extends PacketData<?>> {
    private static final Logger logger = LoggerFactory.getLogger(PacketReader.class);
    private final PacketFactory<D> packetFactory;
    private PacketReceiver<D> handler;
    private final AsynchronousSocketChannel channel;
    private String remoteHost;
    private int soTimeout = 0;
    private AtomicBoolean stopped = new AtomicBoolean(false);

    public AsyncPacketReader(AsynchronousSocketChannel channel, PacketFactory<D> packetFactory, PacketReceiver<D> handler) {
        this.channel = channel;
        this.packetFactory = packetFactory;
        this.handler = handler;
    }

    public void start(String remoteHost, int soTimeout) {
        this.remoteHost = remoteHost;
        this.soTimeout = soTimeout;
        this.initiateNextRead(new PacketBufferReader());
    }

    public void stop() {
        this.stopped.set(true);
    }

    private void initiateNextRead(PacketBufferReader bufferReader) {
        if (this.stopped.get()) {
            logger.trace("Stopped, not initiating another read operation.");
            return;
        }
        logger.trace("Initiating next read");
        this.channel.read(bufferReader.getBuffer(), this.soTimeout, TimeUnit.MILLISECONDS, bufferReader, new CompletionHandler<Integer, PacketBufferReader>(){

            @Override
            public void completed(Integer bytesRead, PacketBufferReader reader) {
                logger.trace("Received {} bytes", (Object)bytesRead);
                if (bytesRead < 0) {
                    this.handleClosedReader();
                    return;
                }
                try {
                    this.processPackets(reader);
                    AsyncPacketReader.this.initiateNextRead(reader);
                }
                catch (RuntimeException e) {
                    AsyncPacketReader.this.handleAsyncFailure(e);
                }
            }

            @Override
            public void failed(Throwable exc, PacketBufferReader attachment) {
                AsyncPacketReader.this.handleAsyncFailure(exc);
            }

            private void processPackets(PacketBufferReader reader) {
                byte[] packetBytes = reader.readNext();
                while (packetBytes != null) {
                    AsyncPacketReader.this.readAndHandlePacket(packetBytes);
                    packetBytes = reader.readNext();
                }
            }

            private void handleClosedReader() {
                if (!AsyncPacketReader.this.stopped.get()) {
                    AsyncPacketReader.this.handleAsyncFailure(new EOFException("Connection closed by server"));
                }
            }
        });
    }

    private void readAndHandlePacket(byte[] packetBytes) {
        try {
            D packet = this.packetFactory.read(packetBytes);
            logger.trace("Received packet << {} >>", (Object)packet);
            this.handler.handle(packet);
        }
        catch (Buffer.BufferException | IOException e) {
            this.handleAsyncFailure(e);
        }
    }

    private void handleAsyncFailure(Throwable exc) {
        if (this.isChannelClosedByOtherParty(exc)) {
            logger.trace("Channel to {} closed by other party, closing it locally.", (Object)this.remoteHost);
        } else {
            String excClass = exc.getClass().getSimpleName();
            logger.trace("{} on channel to {}, closing channel: {}", excClass, this.remoteHost, exc.getMessage());
        }
        this.closeChannelQuietly();
    }

    private boolean isChannelClosedByOtherParty(Throwable exc) {
        return exc instanceof AsynchronousCloseException;
    }

    private void closeChannelQuietly() {
        try {
            this.channel.close();
        }
        catch (IOException e) {
            String eClass = e.getClass().getSimpleName();
            logger.debug("{} while closing channel to {} on failure: {}", eClass, this.remoteHost, e.getMessage());
        }
    }
}

