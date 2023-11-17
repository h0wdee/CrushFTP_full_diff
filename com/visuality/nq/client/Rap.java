/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.Mount;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.Smb100;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Rap {
    private static final int NETSERVERENUM2 = 104;
    private static final int NETSERVERENUM3 = 215;
    private static final int NetServerEnum2Req = 22;
    private static final int NetServerEnum3Req = 23;
    private static final int ERROR_MORE_DATA = 234;
    private static final boolean DEBUG_MODE = false;
    private static final int BUFFER_SIZE = 100;
    private static final String DEBUG_DOMAIN = "WORKGROUP";

    public static Iterator enumerateServers(String domainName, String serverName, Credentials credentials) throws NqException {
        return Rap.enumerateServersOrDomains(domainName, serverName, credentials, true);
    }

    public static Iterator enumerateDomains(String domainName, String serverName, Credentials credentials) throws NqException {
        return Rap.enumerateServersOrDomains(domainName, serverName, credentials, false);
    }

    private static Iterator enumerateServersOrDomains(String domainName, String serverName, Credentials credentials, boolean bringServers) throws NqException {
        TraceLog.get().enter(200);
        TraceLog.get().message("domainName=" + domainName + "; serverName=" + serverName + "; bringServers=" + bringServers, 1000);
        if (null == serverName || serverName.length() == 0) {
            TraceLog.get().error("Input serverName is null or empty.");
            throw new ClientException("Input serverName is null or empty.", -103);
        }
        boolean[] security = new boolean[]{true, false};
        short[] opcode = new short[]{104, 215};
        Credentials[] credsList = new Credentials[]{new PasswordCredentials(), credentials};
        LinkedHashSet servers = new LinkedHashSet();
        byte[] lastEntry = new byte[16];
        boolean doExit = false;
        boolean switchOpcode = false;
        short total = 0;
        short received = 0;
        int status = 0;
        int len = 0;
        ResponseValues responseValues = new ResponseValues();
        if (null == domainName || domainName.length() == 0) {
            len = 0;
            domainName = null;
        } else {
            len = domainName.length() + 1;
        }
        for (boolean securityValue : security) {
            Mount ipc = null;
            for (Credentials creds : credsList) {
                try {
                    ipc = new Mount(serverName, "IPC$", creds, securityValue, new Smb100(), true);
                }
                catch (NqException e) {
                    TraceLog.get().error("IPC$ mount error = ", e, 2000, e.getErrCode());
                    continue;
                }
                Server server = ipc.getServer();
                TraceLog.get().message("IPC Server = ", server, 2000);
                for (int k = 0; k < opcode.length; ++k) {
                    boolean doContinue = true;
                    do {
                        Buffer inData = Rap.packRequest(opcode[k], domainName, len, server, bringServers, lastEntry);
                        Buffer outParams = new Buffer(8);
                        Buffer outData = new Buffer(Rap.getMaxBufferSize(server));
                        try {
                            server.smb.doRapTransaction(ipc.share, inData, outParams, outData);
                            status = Rap.processResponse(opcode[k], servers, responseValues, outParams, outData, lastEntry);
                            TraceLog.get().message("Status after call to processResponse()=" + status, 2000);
                        }
                        catch (NqException e) {
                            TraceLog.get().caught(e);
                            if (null != ipc) {
                                ipc.close();
                            }
                            TraceLog.get().exit(200);
                            throw new NqException("RAP request failed: " + e, e.getErrCode());
                        }
                        switch (opcode[k]) {
                            case 104: {
                                if (responseValues.totalEntries == responseValues.numEntries) {
                                    doContinue = false;
                                    doExit = true;
                                    break;
                                }
                                if (0 == responseValues.numEntries) {
                                    doContinue = false;
                                    doExit = true;
                                    break;
                                }
                                doContinue = false;
                                switchOpcode = true;
                                lastEntry[0] = 0;
                                total = 0;
                                received = 0;
                                break;
                            }
                            case 215: {
                                if (status != 234 && status != 0) {
                                    TraceLog.get().message("Can't receive response=" + status, 1000);
                                    doContinue = false;
                                    doExit = true;
                                    break;
                                }
                                if (0 == responseValues.numEntries) {
                                    doContinue = false;
                                    doExit = true;
                                    break;
                                }
                                if (switchOpcode) {
                                    total = responseValues.totalEntries;
                                    received = responseValues.numEntries;
                                    switchOpcode = false;
                                } else {
                                    received = (short)(received + responseValues.numEntries - 1);
                                }
                                if (responseValues.totalEntries != responseValues.numEntries || status == 234) {
                                    doContinue = true;
                                    break;
                                }
                                if (received != total) break;
                                doContinue = false;
                                doExit = true;
                            }
                        }
                    } while (doContinue);
                    if (doExit) break;
                }
                if (null != ipc) {
                    ipc.close();
                    ipc = null;
                }
                TraceLog.get().exit(200);
                return servers.iterator();
            }
        }
        TraceLog.get().exit(200);
        return null;
    }

    private static Buffer packRequest(short opcode, String domainName, int len, Server server, boolean bringServers, byte[] lastEntry) {
        Buffer inData = null;
        switch (opcode) {
            case 104: {
                inData = new Buffer(22 + len);
                BufferWriter writer = new BufferWriter(inData.data, 0, false);
                writer.writeInt2(opcode);
                String patternIn = null == domainName ? "WrLehDO" : "WrLehDz";
                writer.writeBytes(patternIn.getBytes(), patternIn.length());
                writer.writeByte((byte)0);
                String patternOut = "B16";
                writer.writeBytes(patternOut.getBytes(), patternOut.length());
                writer.writeByte((byte)0);
                writer.writeInt2(0);
                writer.writeInt2(Rap.getMaxBufferSize(server));
                writer.writeInt4(bringServers ? -1 : Integer.MIN_VALUE);
                String nbtName = Utility.getNetbiosNameFromFQN(domainName);
                if (null == nbtName) break;
                writer.writeBytes(nbtName.getBytes(), nbtName.length());
                writer.writeByte((byte)0);
                break;
            }
            case 215: {
                inData = new Buffer(39 + len);
                BufferWriter writer = new BufferWriter(inData.data, 0, false);
                writer.writeInt2(opcode);
                String patternIn = "WrLehDzz";
                writer.writeBytes(patternIn.getBytes(), patternIn.length());
                writer.writeByte((byte)0);
                String patternOut = "B16";
                writer.writeBytes(patternOut.getBytes(), patternOut.length());
                writer.writeByte((byte)0);
                writer.writeInt2(0);
                writer.writeInt2(Rap.getMaxBufferSize(server));
                writer.writeInt4(bringServers ? -1 : Integer.MIN_VALUE);
                String nbtName = Utility.getNetbiosNameFromFQN(domainName);
                if (null != nbtName) {
                    writer.writeBytes(nbtName.getBytes(), nbtName.length());
                    writer.writeByte((byte)0);
                }
                if (0 == lastEntry[0]) break;
                writer.writeBytes(lastEntry);
            }
        }
        return inData;
    }

    private static int getMaxBufferSize(Server server) {
        int maxBufferSz = server.maxTrans;
        if (maxBufferSz > 65280) {
            maxBufferSz = 65280;
        }
        return maxBufferSz;
    }

    private static int processResponse(short opcode, Set servers, ResponseValues responseValues, Buffer outParams, Buffer outData, byte[] lastEntry) throws NqException {
        int i;
        BufferReader reader = new BufferReader(outParams.data, 0, false);
        short status = reader.readInt2();
        reader.readInt2();
        responseValues.numEntries = reader.readInt2();
        responseValues.totalEntries = reader.readInt2();
        TraceLog.get().message("totalEntries=" + responseValues.totalEntries + "; numEntries=" + responseValues.numEntries, 1000);
        short entryCount = responseValues.numEntries;
        reader = new BufferReader(outData.data, 0, false);
        String lastItem = null;
        block0: while (true) {
            short s = entryCount;
            entryCount = (short)(s - 1);
            if (s <= 0) break;
            byte[] nameBytes = new byte[16];
            reader.readBytes(nameBytes, 16);
            i = 15;
            while (true) {
                if (i < 0) continue block0;
                if (nameBytes[i] != 0) {
                    String entry = new String(nameBytes, 0, i + 1);
                    SmbException smbExc = Rap.checkForServerLoop(responseValues.totalEntries, responseValues.numEntries, servers, entry);
                    if (smbExc.getErrCode() != 0) {
                        throw smbExc;
                    }
                    servers.add(entry);
                    lastItem = entry;
                    continue block0;
                }
                --i;
            }
            break;
        }
        if (null != lastItem) {
            byte[] lastItemBytes = lastItem.getBytes();
            for (i = 0; i < 16; ++i) {
                lastEntry[i] = i < lastItem.length() ? lastItemBytes[i] : (byte)0;
            }
            int len = Math.min(lastItem.length(), 16);
            if (len < 16) {
                lastEntry[len] = 0;
            }
        }
        return status;
    }

    private static SmbException checkForServerLoop(int totalEntries, int numEntries, Set servers, String entry) {
        if (numEntries > 1) {
            return new SmbException(0);
        }
        if (numEntries < totalEntries || servers.contains(entry)) {
            return new SmbException("Cannot retrieve all entries (" + numEntries + " out of " + totalEntries + ")", -1073741789);
        }
        return new SmbException(0);
    }

    static class ResponseValues {
        short numEntries = 0;
        short totalEntries = 0;

        ResponseValues() {
        }

        public String toString() {
            return "ResponseValues [numEntries=" + this.numEntries + ", totalEntries=" + this.totalEntries + "]";
        }
    }
}

