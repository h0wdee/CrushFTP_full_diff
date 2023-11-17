/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.IpAddressHelper;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.resolve.NameMessage;
import com.visuality.nq.resolve.NetbiosException;
import com.visuality.nq.resolve.NetbiosName;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public abstract class SessionService {
    private static final int FLAG_SESSIONMESSAGE = 0;
    private static final int FLAG_SSESSIONREQUEST = 129;
    private static final int FLAG_SPOSITIVESESSIONRESPONSE = 130;
    private static final int FLAG_SNEGATIVESESSIONRESPONSE = 131;
    private static final int FLAG_SSESSIONRETARGETRESPONSE = 132;
    private static final int FLAG_SSESSIONKEEPALIVE = 133;

    public static int makeHeader(byte[] buf, int packetLen, int dataCount) throws NetbiosException {
        BufferWriter writer = new BufferWriter(buf, 0, true);
        if (packetLen <= 0) {
            throw new NetbiosException("Invalid data length");
        }
        writer.writeInt4(packetLen);
        return dataCount + 4;
    }

    public static void sendFromBuffer(OutputStream out, byte[] buffer, int dataLen) throws NetbiosException {
        try {
            out.write(buffer, 0, dataLen);
        }
        catch (IOException e) {
            throw new NetbiosException("Invalid socket : " + e.getMessage(), -504);
        }
    }

    public static int recvHeader(InputStream in, byte[] recvHeaderdata) throws NqException {
        TraceLog.get().enter(250);
        int result = 0;
        int recvLen = 0;
        int recvOffset = 0;
        try {
            for (int bytesToRead = recvHeaderdata.length; bytesToRead > 0; bytesToRead -= recvLen) {
                recvLen = in.read(recvHeaderdata, recvOffset, bytesToRead);
                if (-1 == recvLen) {
                    TraceLog.get().message("stream is at end of file : " + recvHeaderdata.length + " , " + recvLen, 10);
                    TraceLog.get().exit(250);
                    throw new NetbiosException("stream is at end of file", -507);
                }
                recvOffset += recvLen;
                if (0 == recvLen) {
                    TraceLog.get().message("Invalid NBT header - len : " + recvHeaderdata.length + " , " + recvLen, 10);
                    TraceLog.get().exit(250);
                    throw new NetbiosException("Invalid NBT header", -502);
                }
                if (133 != (0x85 & recvHeaderdata[0])) {
                    continue;
                }
                break;
            }
        }
        catch (SocketTimeoutException e1) {
            TraceLog.get().exit("socket timeout, didn't get any bytes to read.", 2000);
            throw new NetbiosException("socket timeout : " + e1.getMessage(), -505);
        }
        catch (IOException e) {
            TraceLog.get().exit("Invalid NBT socket : ", e, 250);
            throw new NetbiosException("Invalid NBT socket : " + e.getMessage(), -504);
        }
        switch (recvHeaderdata[0]) {
            case 0: {
                BufferReader reader = new BufferReader(recvHeaderdata, 0, true);
                result = reader.readInt4();
                break;
            }
            case -123: {
                result = 0;
                break;
            }
            default: {
                TraceLog.get().message("read smb header ERROR Unexpected NBT flag: " + recvHeaderdata[0], 2000);
                TraceLog.get().exit(250);
                throw new NetbiosException("Unexpected NBT flag: " + recvHeaderdata[0], -506);
            }
        }
        TraceLog.get().exit(250);
        return result;
    }

    public static int recvIntoBuffer(InputStream in, byte[] buffer, int offset, int dataLen) throws NetbiosException {
        int bytesToRead = dataLen;
        while (bytesToRead > 0) {
            try {
                int res = in.read(buffer, offset, bytesToRead);
                if (-1 == res) {
                    TraceLog.get().message("stream is at end of file : " + buffer.length + " , " + res, 5);
                    throw new NetbiosException("stream is at end of file", -502);
                }
                bytesToRead -= res;
                offset += res;
            }
            catch (SocketTimeoutException e1) {
                throw new NetbiosException("socket timeout", -505);
            }
            catch (IOException e) {
                TraceLog.get().error("IOException = ", e);
                throw new NetbiosException("Invalid socket", -504);
            }
        }
        return dataLen;
    }

    public static byte[] recvSkip(InputStream in, int remaining) throws NetbiosException {
        byte[] dataRead = new byte[remaining];
        int bytesReadOffset = 0;
        while (remaining > 0) {
            try {
                remaining -= in.read(dataRead, bytesReadOffset, remaining);
                bytesReadOffset += remaining;
                if (-1 != remaining) continue;
                TraceLog.get().message("stream is at end of file : " + dataRead.length + " , " + remaining, 5);
                throw new NetbiosException("stream is at end of file", -502);
            }
            catch (IOException e) {
                TraceLog.get().error("IOException = ", e);
                throw new NetbiosException("Invalid socket", -504);
            }
        }
        return dataRead;
    }

    public static void startSession(Socket sock, InetAddress ip, NetbiosName name) throws NqException {
        byte[] data = new byte[72];
        BufferWriter writer = new BufferWriter(data, 0, true);
        try {
            writer.writeByte((byte)-127);
            writer.writeByte((byte)0);
            writer.writeInt2(68);
            if (null == name || IpAddressHelper.isIpAddress(name.toString())) {
                NameMessage msg = new NameMessage();
                msg.queryByIp(ip);
                NetbiosName dstName = msg.getName();
                dstName.setRole(32);
                dstName.encodeName(data, writer.getOffset());
            } else {
                name.encodeName(data, writer.getOffset());
            }
            writer.skip(34);
            InetAddress addr = IpAddressHelper.getLocalHostIp();
            NetbiosName hostName = new NetbiosName(addr.getHostName(), 0);
            hostName.encodeName(data, writer.getOffset());
            sock.getOutputStream().write(data);
            sock.getInputStream().read(data, 0, 4);
        }
        catch (IOException e) {
            throw new NetbiosException(-504);
        }
        if (data[0] != -126) {
            if (data[0] == -125) {
                throw new NetbiosException("NetBIOS Session refused");
            }
            throw new NetbiosException(-21);
        }
    }
}

