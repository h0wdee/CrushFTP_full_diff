/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.Capture;
import com.visuality.nq.common.CaptureInfo;
import com.visuality.nq.common.IpAddressHelper;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.config.Config;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class CaptureInternal
extends Capture {
    public static final boolean DO_NOT_CAPTURE = false;
    private static final String CAPTUREFILE = "CAPTUREFILE";
    private static final String CAPTUREMAXRECORDSINFILE = "CAPTUREMAXRECORDSINFILE";
    private static final String CAPTUREMAXFILES = "CAPTUREMAXFILES";
    private static final String CAPTUREFILE_DEFAULT = "default.pcap";
    private static final int CAPTUREMAXRECORDSINFILE_DEFAULT = 1000;
    private static final int CAPTUREMAXFILES_DEFAULT = 50;
    private static final byte[] pcapBlockOrderMagic = new byte[]{-44, -61, -78, -95};
    private static final byte[] pcapBlockVersion = new byte[]{2, 0, 4, 0};
    private static final byte[] pcapBlockSnapLen = new byte[]{-1, -1, 0, 0};
    private static final int pcapFileHeaderSize = pcapBlockOrderMagic.length + pcapBlockVersion.length + pcapBlockSnapLen.length + 3 + 9;
    private static final int PCAPHEADERLENGTH = 16;
    private static final byte[] ETHERNETHEADER = new byte[]{1, 2, 3, 4, 5, 6, 17, 18, 19, 20, 21, 22};
    private static final byte[] ETHERNETIPV4 = new byte[]{8, 0};
    private static final byte[] ETHERNETIPV6 = new byte[]{-122, -35};
    private static final int ETHERNETHEADERLENGTH = 14;
    private static final int IPV4HEADERLENGTH = 20;
    private static final byte IPV4TYPE = 69;
    private static final byte[] IPV4FLAGS = new byte[]{64, 0, -128, 6, 0, 0};
    private static final int IPVIPV6HEADERLENGTH = 40;
    private static final byte IPV6TYPE = 96;
    private static final byte IPV6NEXTHEADERTCP = 6;
    private static final int TCPHEADERLENGTH = 32;
    private static final byte TCPHEADERLEN = -128;
    private static final byte TCPFLAGS = 16;
    private static final byte[] TCPWINDOWSIZE = new byte[]{1, 0};
    private static final int UDPHEADERLENGTH = 8;
    private static final byte[] IPV4FLAGS_UDP = new byte[]{64, 0, -1, 17, 0, 0};
    private static final byte IPV6NEXTHEADERUDP = 17;
    private static final int NETBIOSHEADERLENGTH = 4;
    private static final int CUTPACKETLEN = 10154;
    private static final byte CM_NB_SESSIONMESSAGE = 0;
    private static final byte CM_NB_SESSIONLENGTHEXTENSION = 1;
    private static int jvmId = 0;
    private static File logDir;
    private static short IPv4Id;
    private int sequenceNumberSend = 1;
    private int sequenceNumberReceive = 1;
    private boolean isIpv6 = false;
    private static InetAddress myLocalAddress;
    private Map captureMap = new HashMap();
    private static InetAddress ourIpv6Address;
    private static boolean fetchedLocalIpv6Address;
    private static boolean hasConfigValueBeenChecked;

    public CaptureInternal() {
        try {
            if (!hasConfigValueBeenChecked) {
                loggingEnabled = Config.jnq.getBool("ENABLECAPTUREPACKETS");
                hasConfigValueBeenChecked = true;
            }
        }
        catch (NqException e1) {
            loggingEnabled = false;
        }
        if (loggingEnabled && null == queue) {
            queue = new LinkedList();
            fileNumber = 0;
            CaptureInternal.startCaptureThread();
            doExit = false;
        }
        if (!fetchedLocalIpv6Address) {
            fetchedLocalIpv6Address = true;
            ourIpv6Address = IpAddressHelper.determineLocalIpv6Address();
        }
    }

    private void preparePcapHeader(CaptureInfo captureInfo, int length) {
        long packetTime = System.currentTimeMillis();
        int packetTimeSecs = (int)(packetTime / 1000L);
        int packetTimeMills = (int)(packetTime - (long)(packetTimeSecs * 1000));
        captureInfo.getPacket().writeInt4(packetTimeSecs);
        captureInfo.getPacket().writeInt4(packetTimeMills * 1000);
        captureInfo.getPacket().writeInt4(length);
        captureInfo.getPacket().writeInt4(length);
    }

    private void prepareIpHeader(CaptureInfo captureInfo, short length, CaptureHeader captureHeader, boolean ipv6) {
        if (!ipv6) {
            captureInfo.getPacket().writeByte((byte)69);
            captureInfo.getPacket().writeZeros(1);
            captureInfo.getPacket().writeInt2(CaptureInternal.cmHtob16(length));
            captureInfo.getPacket().writeInt2(CaptureInternal.cmHtob16(IPv4Id));
            IPv4Id = (short)(IPv4Id + 1);
            captureInfo.getPacket().writeBytes(IPV4FLAGS);
            captureInfo.getPacket().writeInt4(captureHeader.srcIP);
            captureInfo.getPacket().writeInt4(captureHeader.dstIP);
        } else {
            captureInfo.getPacket().writeByte((byte)96);
            captureInfo.getPacket().writeZeros(3);
            captureInfo.getPacket().writeInt2(CaptureInternal.cmHtob16(length));
            captureInfo.getPacket().writeByte((byte)6);
            captureInfo.getPacket().writeByte((byte)-128);
            captureInfo.getPacket().writeBytes(captureHeader.srcIPv6);
            captureInfo.getPacket().writeBytes(captureHeader.dstIPv6);
        }
    }

    private void prepareIpHeaderForUDP(CaptureInfo captureInfo, short length, CaptureHeader captureHeader, boolean ipv6) {
        if (!ipv6) {
            captureInfo.getPacket().writeByte((byte)69);
            captureInfo.getPacket().writeZeros(1);
            captureInfo.getPacket().writeInt2(CaptureInternal.cmHtob16(length));
            captureInfo.getPacket().writeInt2(0);
            captureInfo.getPacket().writeBytes(IPV4FLAGS_UDP);
            captureInfo.getPacket().writeInt4(captureHeader.srcIP);
            captureInfo.getPacket().writeInt4(captureHeader.dstIP);
        } else {
            captureInfo.getPacket().writeByte((byte)96);
            captureInfo.getPacket().writeZeros(3);
            captureInfo.getPacket().writeInt2(CaptureInternal.cmHtob16(length));
            captureInfo.getPacket().writeByte((byte)17);
            captureInfo.getPacket().writeByte((byte)-1);
            captureInfo.getPacket().writeBytes(captureHeader.srcIPv6);
            captureInfo.getPacket().writeBytes(captureHeader.dstIPv6);
        }
    }

    private void prepareUdpHeader(CaptureInfo captureInfo, int srcPort, int dstPort, int length) {
        captureInfo.getPacket().writeInt2(CaptureInternal.cmHtob16(srcPort));
        captureInfo.getPacket().writeInt2(CaptureInternal.cmHtob16(dstPort));
        captureInfo.getPacket().writeInt2(CaptureInternal.cmHtob16(length));
        captureInfo.getPacket().writeInt2(0);
    }

    private void prepareTcpHeader(CaptureInfo captureInfo, int sequenceNumber, CaptureHeader captureHeader) {
        captureInfo.getPacket().writeInt2(CaptureInternal.cmHtob16(captureHeader.srcPort));
        captureInfo.getPacket().writeInt2(CaptureInternal.cmHtob16(captureHeader.dstPort));
        captureInfo.getPacket().writeInt4(CaptureInternal.cmHtob32(sequenceNumber));
        captureInfo.getPacket().writeZeros(4);
        captureInfo.getPacket().writeByte((byte)-128);
        captureInfo.getPacket().writeByte((byte)16);
        captureInfo.getPacket().writeBytes(TCPWINDOWSIZE, TCPWINDOWSIZE.length);
        captureInfo.getPacket().writeInt2(0);
        captureInfo.getPacket().writeInt2(0);
        captureInfo.getPacket().writeZeros(12);
    }

    private void prepareNetBiosHeader(byte[] buffer, int length) {
        buffer[0] = 0;
        buffer[1] = (byte)(length >> 16 & 1);
        buffer[2] = (byte)(length >> 8 & 0xFF);
        buffer[3] = (byte)(length - (length >> 8 & 0xFF) * 256);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void capturePacketWriteStart(boolean isSMB, boolean receiving, Socket socket) {
        if (!loggingEnabled) {
            return;
        }
        if (socket == null) {
            TraceLog.get().error("Invalid socket");
            return;
        }
        CaptureInfo captureInfo = new CaptureInfo();
        Long threadID = Thread.currentThread().getId();
        Map map = this.captureMap;
        synchronized (map) {
            if (this.captureMap.containsKey(threadID)) {
                TraceLog.get().error("Capture started without ending the previous capture first.");
            }
        }
        CaptureHeader captureHeader = null;
        captureInfo.setSMB(isSMB);
        captureInfo.setReceiving(receiving);
        if (receiving) {
            captureHeader = new CaptureHeader(receiving, socket.getInetAddress(), socket.getPort(), socket.getLocalAddress(), socket.getLocalPort());
        } else {
            captureHeader = new CaptureHeader(receiving, socket.getLocalAddress(), socket.getLocalPort(), socket.getInetAddress(), socket.getPort());
            myLocalAddress = socket.getLocalAddress();
        }
        captureInfo.setCaptureHeader(captureHeader);
        this.isIpv6 = captureHeader.isIpv6();
        int initialOffset = 34;
        initialOffset += this.isIpv6 ? 40 : 20;
        if (isSMB) {
            initialOffset += 32;
        }
        captureInfo.getPacket().setOffset(initialOffset);
        Map map2 = this.captureMap;
        synchronized (map2) {
            this.captureMap.put(threadID, captureInfo);
        }
    }

    private void prepareEthernetHeader(CaptureInfo captureInfo, boolean isIpv6) {
        captureInfo.getPacket().writeBytes(ETHERNETHEADER, ETHERNETHEADER.length);
        if (isIpv6) {
            captureInfo.getPacket().writeBytes(ETHERNETIPV6, 2);
        } else {
            captureInfo.getPacket().writeBytes(ETHERNETIPV4, 2);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void capturePacketWritePacket(byte[] packetBuf, int length) {
        if (!loggingEnabled) {
            return;
        }
        CaptureInfo captureInfo = null;
        Map map = this.captureMap;
        synchronized (map) {
            captureInfo = (CaptureInfo)this.captureMap.get(Thread.currentThread().getId());
        }
        if (null == captureInfo) {
            TraceLog.get().message("No call to capturePacketWriteStart");
            return;
        }
        if (captureInfo.getDataLength() + length > 10154) {
            length = 10154 - captureInfo.getDataLength();
            TraceLog.get().message("new length =" + length);
        }
        captureInfo.getPacket().writeBytes(packetBuf, length);
        captureInfo.setDataLength(captureInfo.getDataLength() + length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void capturePacketWritePacket(byte[] packetData, int offset, int length) {
        if (!loggingEnabled) {
            return;
        }
        CaptureInfo captureInfo = null;
        Map map = this.captureMap;
        synchronized (map) {
            captureInfo = (CaptureInfo)this.captureMap.get(Thread.currentThread().getId());
        }
        if (null == captureInfo) {
            TraceLog.get().message("No call to capturePacketWriteStart");
            return;
        }
        byte[] packetBuf = new byte[length];
        System.arraycopy(packetData, offset, packetBuf, 0, length);
        if (captureInfo.getDataLength() + length > 10154) {
            length = 10154 - captureInfo.getDataLength();
        }
        captureInfo.getPacket().writeBytes(packetBuf, length);
        captureInfo.setDataLength(captureInfo.getDataLength() + length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void capturePacketWriteEnd() {
        if (!loggingEnabled) {
            return;
        }
        if (null == queue) {
            return;
        }
        CaptureInfo captureInfo = null;
        Map map = this.captureMap;
        synchronized (map) {
            captureInfo = (CaptureInfo)this.captureMap.get(Thread.currentThread().getId());
        }
        if (null == captureInfo) {
            return;
        }
        if (0 == captureInfo.getDataLength()) {
            return;
        }
        int packetLen = captureInfo.getPacket().getOffset();
        int totalPcapLength = captureInfo.getDataLength() + 14 + 4;
        totalPcapLength += this.isIpv6 ? 40 : 20;
        if (captureInfo.isSMB()) {
            totalPcapLength += 32;
        }
        short ipHeaderLength = (short)(captureInfo.getDataLength() + 4);
        ipHeaderLength = (short)(ipHeaderLength + (this.isIpv6 ? 40 : 20));
        if (captureInfo.isSMB()) {
            ipHeaderLength = (short)(ipHeaderLength + 32);
        }
        captureInfo.getPacket().setOffset(0);
        this.preparePcapHeader(captureInfo, totalPcapLength);
        this.prepareEthernetHeader(captureInfo, this.isIpv6);
        this.prepareIpHeader(captureInfo, ipHeaderLength, captureInfo.getCaptureHeader(), this.isIpv6);
        if (captureInfo.isSMB()) {
            int sequenceNumber;
            CaptureInternal captureInternal = this;
            synchronized (captureInternal) {
                if (captureInfo.isReceiving()) {
                    sequenceNumber = this.sequenceNumberReceive;
                    this.sequenceNumberReceive += 4 + captureInfo.getDataLength();
                } else {
                    sequenceNumber = this.sequenceNumberSend;
                    this.sequenceNumberSend += 4 + captureInfo.getDataLength();
                }
            }
            this.prepareTcpHeader(captureInfo, sequenceNumber, captureInfo.getCaptureHeader());
            byte[] netBiosHeader = new byte[4];
            this.prepareNetBiosHeader(netBiosHeader, captureInfo.getDataLength());
            captureInfo.getPacket().writeBytes(netBiosHeader);
        }
        captureInfo.getPacket().setOffset(packetLen);
        this.addToCaptureQueue(captureInfo.getPacket());
        Map map2 = this.captureMap;
        synchronized (map2) {
            this.captureMap.remove(Thread.currentThread().getId());
        }
    }

    public void capturePacketWriteUdp(boolean isInboundPacket, InetAddress localAddress, int localPort, InetAddress inetAddress, int inetPort, DatagramPacket packet) {
        if (!loggingEnabled) {
            return;
        }
        if (inetAddress.getHostAddress().equals("127.0.0.1")) {
            return;
        }
        if (null == localAddress || localAddress.getHostAddress().equals("0.0.0.0")) {
            try {
                localAddress = null != myLocalAddress ? myLocalAddress : InetAddress.getLocalHost();
            }
            catch (UnknownHostException e) {
                // empty catch block
            }
        }
        int length = packet.getLength();
        byte[] packetBuf = new byte[length];
        System.arraycopy(packet.getData(), packet.getOffset(), packetBuf, 0, length);
        CaptureInfo captureInfo = new CaptureInfo();
        CaptureHeader captureHeader = null;
        captureInfo.setSMB(false);
        captureInfo.setReceiving(isInboundPacket);
        captureHeader = isInboundPacket ? new CaptureHeader(isInboundPacket, inetAddress, inetPort, localAddress, localPort) : new CaptureHeader(isInboundPacket, localAddress, localPort, inetAddress, inetPort);
        captureInfo.setCaptureHeader(captureHeader);
        this.isIpv6 = captureHeader.isIpv6();
        int totalPcapLength = 14;
        totalPcapLength += this.isIpv6 ? 40 : 20;
        short ipHeaderLength = (short)length;
        ipHeaderLength = (short)(ipHeaderLength + (this.isIpv6 ? 40 : 20));
        ipHeaderLength = (short)(ipHeaderLength + 8);
        this.preparePcapHeader(captureInfo, totalPcapLength += 8 + length);
        this.prepareEthernetHeader(captureInfo, this.isIpv6);
        this.prepareIpHeaderForUDP(captureInfo, ipHeaderLength, captureInfo.getCaptureHeader(), this.isIpv6);
        this.prepareUdpHeader(captureInfo, localPort, inetPort, 8 + length);
        captureInfo.getPacket().writeBytes(packetBuf, length);
        captureInfo.setDataLength(captureInfo.getDataLength() + length);
        this.addToCaptureQueue(captureInfo.getPacket());
    }

    public static synchronized boolean loggingEnabled() {
        return loggingEnabled;
    }

    public static synchronized int getAndIncrementFileNumber() {
        return fileNumber++;
    }

    public static synchronized int getAndIncrementJvmId() {
        return jvmId++;
    }

    public static synchronized File getLogDir() {
        return logDir;
    }

    public static int convertInetAddressToInt(InetAddress addr) {
        int value = ByteBuffer.wrap(addr.getAddress()).getInt();
        int ipAddress = ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN) ? Integer.reverseBytes(value) : value;
        return ipAddress;
    }

    public static short cmHtob16(int v) {
        short low = (byte)(v / 256);
        short high = (byte)(v % 256);
        return (short)(low + high * 256);
    }

    public static int cmHtob32(int v) {
        byte[] data = new byte[4];
        data[3] = (byte)(v >> 24 & 0xFF);
        data[2] = (byte)(v >> 16 & 0xFF);
        data[1] = (byte)(v >> 8 & 0xFF);
        data[0] = (byte)(v % 256);
        return ByteBuffer.wrap(data).getInt();
    }

    public static byte[] convertIntToBytes(int v) {
        byte[] dest = new byte[]{(byte)(v >> 24 & 0xFF), (byte)(v >> 16 & 0xFF), (byte)(v >> 8 & 0xFF), (byte)(v % 256)};
        return dest;
    }

    public static byte[] convertShortToBytes(short v) {
        byte[] dest = new byte[]{(byte)(v >> 8 & 0xFF), (byte)(v % 256)};
        return dest;
    }

    public static byte[] int32toBytes(int v) {
        int hex = CaptureInternal.cmHtob32(v);
        byte[] b = new byte[]{(byte)((hex & 0xFF000000) >> 24), (byte)((hex & 0xFF0000) >> 16), (byte)((hex & 0xFF00) >> 8), (byte)(hex & 0xFF)};
        return b;
    }

    public static String toInetAddressString(int addr) {
        try {
            return InetAddress.getByAddress(CaptureInternal.int32toBytes(addr)).toString();
        }
        catch (UnknownHostException e) {
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addToCaptureQueue(BufferWriter buffer) {
        Queue queue = CaptureInternal.queue;
        synchronized (queue) {
            CaptureInternal.queue.add(new SpoolerConsumer(buffer));
            CaptureInternal.queue.notify();
        }
    }

    public Thread getCaptureSpoolerThread() {
        return captureSpoolerThread;
    }

    static {
        IPv4Id = 1;
        myLocalAddress = null;
        ourIpv6Address = null;
        fetchedLocalIpv6Address = false;
        hasConfigValueBeenChecked = false;
    }

    protected static class CaptureSpooler
    implements Runnable {
        private static int recordsInFile = 0;
        private static int captureFileSize;
        private static int maxNumberOfFiles;
        private static FileOutputStream fileOut;
        private static String captureFileName;
        private static String basicPath;
        private static String basicFileName;

        protected CaptureSpooler() {
        }

        public void init() {
            boolean startResult;
            try {
                captureFileSize = Config.jnq.getInt(CaptureInternal.CAPTUREMAXRECORDSINFILE);
            }
            catch (NqException e) {
                captureFileSize = 1000;
            }
            try {
                maxNumberOfFiles = Config.jnq.getInt(CaptureInternal.CAPTUREMAXFILES);
            }
            catch (NqException e) {
                maxNumberOfFiles = 50;
            }
            try {
                captureFileName = Config.jnq.getString(CaptureInternal.CAPTUREFILE);
            }
            catch (NqException e) {
                captureFileName = CaptureInternal.CAPTUREFILE_DEFAULT;
            }
            int pos = 0;
            int tmpPos = captureFileName.lastIndexOf("\\");
            if (tmpPos != -1) {
                pos = tmpPos + 1;
            } else {
                tmpPos = captureFileName.lastIndexOf("/");
                if (tmpPos != -1) {
                    pos = tmpPos + 1;
                }
            }
            basicFileName = captureFileName.substring(pos);
            basicPath = captureFileName.substring(0, captureFileName.length() - basicFileName.length());
            if (basicPath.length() != 0 && !basicPath.endsWith("/")) {
                basicPath = basicPath + "/";
            }
            if (!(startResult = CaptureSpooler.startFile())) {
                String msg = "Could not create packet capturing file " + captureFileName;
                Capture.loggingEnabled = false;
                TraceLog.get().error(msg);
            } else {
                CaptureSpooler.initPcapFile();
            }
        }

        private static void initPcapFile() {
            byte[] pcapFileHeader = new byte[pcapFileHeaderSize];
            BufferWriter bufferWriter = CaptureSpooler.preparePcapFile(pcapFileHeader);
            try {
                CaptureSpooler.writeToFile(bufferWriter, pcapFileHeaderSize);
            }
            catch (IOException ioe) {
                String msg = "Unable to write pcap header info to file " + captureFileName + ": ";
                TraceLog.get().error(msg, ioe);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void run() {
            SpoolerConsumer spoolerObj;
            boolean isFirstTime = true;
            while (!Capture.doExit) {
                spoolerObj = null;
                Queue queue = Capture.queue;
                synchronized (queue) {
                    if (!Capture.queue.isEmpty()) {
                        spoolerObj = (SpoolerConsumer)Capture.queue.poll();
                    } else {
                        try {
                            Capture.queue.wait(10000L);
                        }
                        catch (InterruptedException e) {
                            // empty catch block
                        }
                    }
                }
                if (null == spoolerObj) continue;
                if (isFirstTime) {
                    this.init();
                    isFirstTime = false;
                }
                CaptureSpooler.writeToFile(spoolerObj.buffer.getDest(), spoolerObj.buffer.getOffset());
            }
            while (!Capture.queue.isEmpty()) {
                spoolerObj = (SpoolerConsumer)Capture.queue.poll();
                if (null == spoolerObj) continue;
                CaptureSpooler.writeToFile(spoolerObj.buffer.getDest(), spoolerObj.buffer.getOffset());
            }
            CaptureSpooler.captureShutdown();
        }

        private static void writeToFile(byte[] data, int sz) {
            if (0 == sz) {
                return;
            }
            if (++recordsInFile > captureFileSize && fileOut != null) {
                boolean result;
                recordsInFile = 0;
                try {
                    fileOut.close();
                }
                catch (IOException e) {
                    TraceLog.get().error("Unable to close capture file: ", e);
                }
                if (++Capture.fileNumber + 1 > maxNumberOfFiles) {
                    String fileToDel = Capture.fileNumber == maxNumberOfFiles ? captureFileName : Capture.fileNumber - maxNumberOfFiles + "_" + basicFileName;
                    new File(logDir, fileToDel).delete();
                }
                if (!(result = CaptureSpooler.tryFile(Capture.fileNumber + "_" + basicFileName))) {
                    String msg = "Could not create packet capturing file " + captureFileName;
                    Capture.loggingEnabled = false;
                    TraceLog.get().error(msg);
                }
                CaptureSpooler.initPcapFile();
            }
            try {
                if (null != fileOut) {
                    fileOut.write(data, 0, sz);
                    fileOut.flush();
                }
            }
            catch (IOException ioe) {
                TraceLog.get().error("Unable to write to capture file: ", ioe);
            }
        }

        private static void writeToFile(BufferWriter bufferWriter, int sz) throws IOException {
            byte[] data = bufferWriter.getDest();
            CaptureSpooler.writeToFile(data, sz);
        }

        private static boolean startFile() {
            TraceLog.get().enter(700);
            if (Capture.loggingEnabled) {
                jvmId = 1;
                while (true) {
                    logDir = new File(CaptureSpooler.basicPath);
                    if (!logDir.exists()) {
                        basicPath = "";
                    }
                    logDir = new File(CaptureSpooler.basicPath + jvmId + ".pcap");
                    if (!logDir.exists()) break;
                    jvmId++;
                }
                logDir.mkdir();
                boolean result = CaptureSpooler.tryFile(basicFileName);
                TraceLog.get().message("Return from method tryFile() = " + result);
                TraceLog.get().exit(700);
                return result;
            }
            TraceLog.get().exit(700);
            return false;
        }

        private static boolean tryFile(String fileName) {
            TraceLog.get().enter(700);
            if (Capture.loggingEnabled) {
                try {
                    File f = new File(logDir, fileName);
                    if (f.exists()) {
                        f.delete();
                    }
                    fileOut = new FileOutputStream(f);
                    TraceLog.get().exit(700);
                    return true;
                }
                catch (FileNotFoundException e) {
                    TraceLog.get().error("Unable to start packet capturing into '" + fileName + "' - ", e);
                    TraceLog.get().exit(700);
                    return false;
                }
            }
            TraceLog.get().exit(700);
            return false;
        }

        public static void captureShutdown() {
            TraceLog.get().enter(700);
            if (null != fileOut) {
                try {
                    fileOut.close();
                }
                catch (IOException e) {
                    TraceLog.get().error("Unable to close the capture file: ", e);
                }
            }
            Capture.captureSpoolerThread = null;
            Capture.queue = null;
            TraceLog.get().exit(700);
        }

        private static BufferWriter preparePcapFile(byte[] buf) {
            BufferWriter bufferWriter = new BufferWriter(buf, 0, false);
            bufferWriter.writeBytes(pcapBlockOrderMagic);
            bufferWriter.writeBytes(pcapBlockVersion);
            bufferWriter.writeZeros(8);
            bufferWriter.writeBytes(pcapBlockSnapLen);
            bufferWriter.writeByte((byte)1);
            bufferWriter.writeZeros(3);
            return bufferWriter;
        }

        static {
            fileOut = null;
            basicPath = "";
            basicFileName = CaptureInternal.CAPTUREFILE_DEFAULT;
        }
    }

    private class SpoolerConsumer {
        BufferWriter buffer;

        SpoolerConsumer(BufferWriter buffer) {
            this.buffer = buffer;
        }
    }

    public class CaptureHeader {
        int srcIP;
        int dstIP;
        byte[] srcIPv6;
        byte[] dstIPv6;
        int srcPort;
        int dstPort;
        boolean receiving;
        boolean isIpv6 = false;

        public CaptureHeader(boolean receiving, InetAddress srcIP, int srcPort, InetAddress dstIP, int dstPort) {
            if (Inet6Address.class == srcIP.getClass() || Inet6Address.class == dstIP.getClass()) {
                this.isIpv6 = true;
            }
            this.receiving = receiving;
            this.srcPort = srcPort;
            this.dstPort = dstPort;
            if (!this.isIpv6) {
                this.srcIP = CaptureInternal.convertInetAddressToInt(srcIP);
                this.dstIP = CaptureInternal.convertInetAddressToInt(dstIP);
            } else {
                this.srcIPv6 = srcIP.getAddress();
                this.dstIPv6 = dstIP.getAddress();
                if (dstIP.toString().equals("0.0.0.0/0.0.0.0")) {
                    this.dstIPv6 = ourIpv6Address.getAddress();
                }
                if (srcIP.toString().equals("0.0.0.0/0.0.0.0")) {
                    this.srcIPv6 = ourIpv6Address.getAddress();
                }
            }
        }

        boolean isIpv6() {
            return this.isIpv6;
        }

        public String toString() {
            return "CaptureHeader [receiving=" + this.receiving + ", srcIP=" + CaptureInternal.toInetAddressString(this.srcIP) + ", srcPort=" + this.srcPort + ", dstIP=" + CaptureInternal.toInetAddressString(this.dstIP) + ", dstPort=" + this.dstPort + ", isIpv6=" + this.isIpv6 + "]";
        }
    }
}

