/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.transport.tcp.async;

import com.hierynomus.protocol.Packet;
import com.hierynomus.protocol.PacketData;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.protocol.transport.PacketHandlers;
import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.protocol.transport.TransportLayer;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.transport.tcp.async.AsyncPacketReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncDirectTcpTransport<D extends PacketData<?>, P extends Packet<?>>
implements TransportLayer<P> {
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DIRECT_HEADER_SIZE = 4;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PacketHandlers<D, P> handlers;
    private final AsynchronousSocketChannel socketChannel;
    private final AsyncPacketReader<D> packetReader;
    private final AtomicBoolean connected;
    private int soTimeout = 0;
    private final Queue<ByteBuffer> writeQueue;
    private AtomicBoolean writingNow;

    public AsyncDirectTcpTransport(int soTimeout, PacketHandlers<D, P> handlers, AsynchronousChannelGroup group) throws IOException {
        this.soTimeout = soTimeout;
        this.handlers = handlers;
        this.socketChannel = AsynchronousSocketChannel.open(group);
        this.packetReader = new AsyncPacketReader<D>(this.socketChannel, handlers.getPacketFactory(), handlers.getReceiver());
        this.writeQueue = new LinkedBlockingQueue<ByteBuffer>();
        this.connected = new AtomicBoolean(false);
        this.writingNow = new AtomicBoolean(false);
    }

    @Override
    public void write(P packet) throws TransportException {
        ByteBuffer bufferToSend = this.prepareBufferToSend(packet);
        this.logger.trace("Sending packet << {} >>", (Object)packet);
        this.writeOrEnqueue(bufferToSend);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void writeOrEnqueue(ByteBuffer buffer) {
        AsyncDirectTcpTransport asyncDirectTcpTransport = this;
        synchronized (asyncDirectTcpTransport) {
            this.writeQueue.add(buffer);
            if (!this.writingNow.getAndSet(true)) {
                this.startAsyncWrite();
            }
        }
    }

    @Override
    public void connect(InetSocketAddress remoteAddress) throws IOException {
        String remoteHostname = remoteAddress.getHostString();
        try {
            Future<Void> connectFuture = this.socketChannel.connect(remoteAddress);
            connectFuture.get(5000L, TimeUnit.MILLISECONDS);
            this.connected.set(true);
        }
        catch (ExecutionException | TimeoutException e) {
            throw TransportException.Wrapper.wrap(e);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw TransportException.Wrapper.wrap(e);
        }
        this.packetReader.start(remoteHostname, this.soTimeout);
    }

    @Override
    public void disconnect() throws IOException {
        this.connected.set(false);
        this.socketChannel.close();
    }

    @Override
    public boolean isConnected() {
        return this.connected.get();
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    private void startAsyncWrite() {
        if (!this.isConnected()) {
            throw new IllegalStateException("Transport is not connected");
        }
        ByteBuffer toSend = this.writeQueue.peek();
        this.socketChannel.write(toSend, this.soTimeout, TimeUnit.MILLISECONDS, null, new CompletionHandler<Integer, Object>(){

            @Override
            public void completed(Integer result, Object attachment) {
                AsyncDirectTcpTransport.this.logger.trace("Written {} bytes to async transport", (Object)result);
                this.startNextWriteIfWaiting();
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                try {
                    if (exc instanceof ClosedChannelException) {
                        AsyncDirectTcpTransport.this.connected.set(false);
                    } else {
                        this.startNextWriteIfWaiting();
                    }
                }
                finally {
                    AsyncDirectTcpTransport.this.handlers.getReceiver().handleError(exc);
                }
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            private void startNextWriteIfWaiting() {
                AsyncDirectTcpTransport asyncDirectTcpTransport = AsyncDirectTcpTransport.this;
                synchronized (asyncDirectTcpTransport) {
                    ByteBuffer head = (ByteBuffer)AsyncDirectTcpTransport.this.writeQueue.peek();
                    if (head != null && head.hasRemaining()) {
                        AsyncDirectTcpTransport.this.startAsyncWrite();
                    } else if (head != null) {
                        AsyncDirectTcpTransport.this.writeQueue.remove();
                        this.startNextWriteIfWaiting();
                    } else {
                        AsyncDirectTcpTransport.this.writingNow.set(false);
                    }
                }
            }
        });
    }

    private ByteBuffer prepareBufferToSend(P packet) {
        Object packetData = this.handlers.getSerializer().write(packet);
        int dataSize = ((Buffer)packetData).available();
        ByteBuffer toSend = ByteBuffer.allocate(dataSize + 4);
        toSend.order(ByteOrder.BIG_ENDIAN);
        toSend.putInt(((Buffer)packetData).available());
        toSend.put(((Buffer)packetData).array(), ((Buffer)packetData).rpos(), ((Buffer)packetData).available());
        toSend.flip();
        try {
            ((Buffer)packetData).skip(dataSize);
        }
        catch (Buffer.BufferException e) {
            throw SMBRuntimeException.Wrapper.wrap(e);
        }
        return toSend;
    }
}

