/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.ClientException;
import com.visuality.nq.client.Rap;
import com.visuality.nq.client.Smb1Transaction;
import com.visuality.nq.client.rpc.Dcerpc;
import com.visuality.nq.client.rpc.Samr;
import com.visuality.nq.client.rpc.Srvsvc;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.IpAddressHelper;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.Resolver;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import com.visuality.nq.config.Config;
import com.visuality.nq.resolve.DatagramMessage;
import com.visuality.nq.resolve.Matches;
import com.visuality.nq.resolve.NetbiosException;
import com.visuality.nq.resolve.NetbiosName;
import com.visuality.nq.resolve.WsdClient;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class Network {
    public static Iterator enumerateDomains() throws NqException {
        return Network.enumerateDomains(PasswordCredentials.getDefaultCredentials());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Iterator enumerateDomains(Credentials credentials) throws NqException {
        TraceLog.get().enter(200);
        if (null == credentials) {
            TraceLog.get().exit(200);
            throw new ClientException("credentials cannot be null", -103);
        }
        Credentials[] credsList = new Credentials[]{credentials, PasswordCredentials.getDefaultCredentials()};
        String dcName = null;
        Iterator res = null;
        NqException savedException = null;
        for (Credentials cred : credsList) {
            Resolver resolver;
            String domainName;
            block18: {
                domainName = cred.getDomain();
                if (null == domainName || domainName.length() == 0) continue;
                try {
                    Iterator it = Network.enumerateBackupList(domainName);
                    while (it.hasNext()) {
                        String serverName = (String)it.next();
                        res = Rap.enumerateDomains(domainName, serverName, cred);
                        if (res == null || !res.hasNext()) continue;
                        TraceLog.get().exit(200);
                        return res;
                    }
                }
                catch (NqException e) {
                    TraceLog.get().message("Error in Smb1Transaction.parseGetBackupListReesponse or Rap.enumerateDomains = ", e, 2000);
                    if (cred != credentials) break block18;
                    savedException = Network.saveException(e);
                }
            }
            if (null != (dcName = (resolver = new Resolver()).getDCNameByDomain(domainName))) {
                Samr samr;
                block20: {
                    block19: {
                        try {
                            res = Rap.enumerateDomains(domainName, dcName, credentials);
                        }
                        catch (NqException e) {
                            TraceLog.get().message("Error in Rap.enumerateDomains = ", e, 2000);
                            if (cred != credentials) break block19;
                            savedException = Network.saveException(e);
                        }
                    }
                    if (null != res && res.hasNext()) {
                        TraceLog.get().exit(200);
                        return res;
                    }
                    samr = null;
                    Dcerpc.Handle serverHandle = null;
                    try {
                        samr = new Samr(dcName, cred);
                        serverHandle = samr.openPolicy();
                        res = samr.enumerateDomainaInServer(serverHandle);
                        if (null == serverHandle) break block20;
                    }
                    catch (NqException e) {
                        block21: {
                            try {
                                TraceLog.get().message("Error in samr.enumerateDomainaInServer = ", e, 2000);
                                if (cred == credentials) {
                                    savedException = Network.saveException(e);
                                }
                                if (null == serverHandle) break block21;
                            }
                            catch (Throwable throwable) {
                                if (null != serverHandle) {
                                    samr.close(serverHandle);
                                }
                                if (null != samr) {
                                    samr.close();
                                }
                                throw throwable;
                            }
                            samr.close(serverHandle);
                        }
                        if (null == samr) continue;
                        samr.close();
                        continue;
                    }
                    samr.close(serverHandle);
                }
                if (null != samr) {
                    samr.close();
                }
            }
            if (res == null || !res.hasNext()) continue;
            TraceLog.get().exit(200);
            return res;
        }
        if (null != savedException) {
            TraceLog.get().exit(200);
            throw new NqException("Cannot enumerate domains (" + savedException.getErrCode() + "): " + savedException.getMessage(), savedException.getErrCode());
        }
        ArrayList emptyList = new ArrayList();
        TraceLog.get().exit("Returning empty list", 200);
        return emptyList.iterator();
    }

    public static Iterator enumerateServers(String domainName) throws NqException {
        return Network.enumerateServers(domainName, PasswordCredentials.getDefaultCredentials());
    }

    public static Iterator enumerateServers(String domainName, Credentials credentials) throws NqException {
        TraceLog.get().enter(200);
        HashSet<String> servers = new HashSet<String>();
        if (null == credentials) {
            TraceLog.get().exit(200);
            throw new ClientException("credentials cannot be null", -103);
        }
        Credentials[] creds = new Credentials[]{credentials, new PasswordCredentials()};
        if (null == domainName) {
            domainName = Config.getDomainName();
        }
        NqException savedException = null;
        Iterator res = null;
        Resolver resolver = new Resolver();
        int waitTime = Config.jnq.getInt("WSDWAITTIME");
        int nonComputers = 0;
        WsdClient wsdClient = new WsdClient(waitTime);
        Collection<Matches> probeMatches = wsdClient.probe();
        if (null != probeMatches && probeMatches.size() > 0) {
            for (Matches match : probeMatches) {
                if (!Network.isComputerType(match)) {
                    ++nonComputers;
                    continue;
                }
                TraceLog.get().message("WSD match returned ", match, 2000);
                if (null == match.getIp() || match.getIp().length() == 0) continue;
                try {
                    String host;
                    if (IpAddressHelper.isIpAddress(match.getIp())) {
                        InetAddress addr = InetAddress.getByName(match.getIp());
                        host = resolver.ipToHost(addr);
                        TraceLog.get().message("WSD resolved host=", host, 2000);
                        host = null == host ? match.getIp() : host;
                    } else {
                        host = match.getIp();
                    }
                    if (null == host) continue;
                    servers.add(host);
                }
                catch (UnknownHostException e) {}
            }
        }
        TraceLog.get().message("Number of non-computer hosts = " + nonComputers, 2000);
        if (!domainName.equalsIgnoreCase("workgroup")) {
            String dcName = resolver.getDCNameByDomain(domainName);
            for (Credentials cred : creds) {
                block20: {
                    try {
                        TraceLog.get().message("domainName=" + domainName + ", dcName=" + dcName, 2000);
                        res = Rap.enumerateServers(domainName, dcName, cred);
                        if (null != res && res.hasNext()) {
                            Network.combineIterators(servers, res);
                            TraceLog.get().exit(200);
                            return servers.iterator();
                        }
                    }
                    catch (NqException e) {
                        TraceLog.get().message(e, 10);
                        if (cred != credentials) break block20;
                        savedException = Network.saveException(e);
                    }
                }
                try {
                    Iterator it = Network.enumerateBackupList(domainName);
                    while (it.hasNext()) {
                        String serverName = (String)it.next();
                        TraceLog.get().message("domainName = " + domainName + ", serverName = " + serverName, 2000);
                        res = Rap.enumerateServers(domainName, serverName, cred);
                        if (res == null || !res.hasNext()) continue;
                        Network.combineIterators(servers, res);
                        TraceLog.get().exit(200);
                        return servers.iterator();
                    }
                }
                catch (NqException e) {
                    TraceLog.get().error("Exception thrown = ", e, 10, e.getErrCode());
                    if (cred == credentials && null == res) {
                        savedException = Network.saveException(e);
                        continue;
                    }
                    if (null == res) continue;
                    Network.combineIterators(servers, res);
                    TraceLog.get().exit(200);
                    return servers.iterator();
                }
            }
        }
        if (servers.size() > 0) {
            TraceLog.get().exit("Returning " + servers.size() + " server names.", 200);
            return servers.iterator();
        }
        if (null != savedException) {
            TraceLog.get().exit(200);
            throw new NqException("Cannot enumerate servers (" + savedException.getErrCode() + "): " + savedException.getMessage(), savedException.getErrCode());
        }
        ArrayList emptyList = new ArrayList();
        TraceLog.get().exit("Returning empty list", 200);
        return emptyList.iterator();
    }

    private static boolean isComputerType(Matches match) {
        for (Matches.WsType type : match.getTypes()) {
            if (type != Matches.WsType.Computer) continue;
            return true;
        }
        return false;
    }

    private static void combineIterators(Set<String> servers, Iterator<String> iter) {
        while (iter.hasNext()) {
            String item = iter.next();
            servers.add(item);
        }
    }

    public static Iterator enumerateBackupList(String domainName) throws NqException {
        TraceLog.get().enter("domainName = " + domainName, 200);
        if (null == domainName) {
            TraceLog.get().exit(200);
            throw new NetbiosException("domain name argument is null", -20);
        }
        DatagramMessage msg = new DatagramMessage();
        String netbiosDomainName = Utility.getNetbiosNameFromFQN(domainName);
        BufferWriter writer = msg.createHeader(new NetbiosName(netbiosDomainName, 29));
        writer = new BufferWriter(writer.getDest(), writer.getOffset(), false);
        Smb1Transaction.createGetBackupListRequest(writer, msg.getTranId());
        BufferReader reader = msg.sendReceive(writer.getOffset() + 2);
        Vector backupServers = Smb1Transaction.parseGetBackupListReesponse(reader);
        TraceLog.get().exit("Number of backup server records = " + backupServers.size(), 200);
        return backupServers.iterator();
    }

    public static Iterator enumerateShares(String serverName) throws NqException {
        return Network.enumerateShares(serverName, PasswordCredentials.getDefaultCredentials());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Iterator enumerateShares(String serverName, Credentials credentials) throws NqException {
        TraceLog.get().enter(200);
        if (null == credentials) {
            TraceLog.get().exit(200);
            throw new ClientException("credentials cannot be null", -103);
        }
        Credentials[] creds = new Credentials[]{credentials, new PasswordCredentials()};
        Iterator res = null;
        NqException savedException = null;
        for (Credentials cred : creds) {
            Srvsvc srvsvc = null;
            try {
                srvsvc = new Srvsvc(serverName, cred);
                res = srvsvc.shareEnum();
            }
            catch (NqException e) {
                TraceLog.get().message("Exception from calling srvsvc.shareEnum() = ", e, 2000);
                if (cred != credentials) continue;
                savedException = Network.saveException(e);
                continue;
            }
            finally {
                if (null != srvsvc) {
                    srvsvc.close();
                }
            }
            if (res == null || !res.hasNext()) continue;
            TraceLog.get().exit(200);
            return res;
        }
        if (null != savedException) {
            TraceLog.get().exit(200);
            throw new NqException("Cannot enumerate shares (" + savedException.getErrCode() + "): " + savedException.getMessage(), savedException.getErrCode());
        }
        ArrayList emptyList = new ArrayList();
        TraceLog.get().exit("Returning empty list", 200);
        return emptyList.iterator();
    }

    private static NqException saveException(NqException e) {
        NqException savedException = -105 == e.getErrCode() || -18 == e.getErrCode() ? new NqException(-18) : e;
        return savedException;
    }
}

