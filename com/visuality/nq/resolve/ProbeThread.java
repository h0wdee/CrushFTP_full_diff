/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.resolve.Matches;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ProbeThread
implements Runnable {
    public static String SOAP_VERSION = "SOAP 1.2 Protocol";
    public static String CONTENT_TYPE = "application/soap+xml";
    public static int BYTE_SIZE = 4096;
    public static String[] validTypes = new String[]{"Computer"};
    private DatagramSocket server;
    private int timeout;
    private Collection<Matches> matches;
    private String uuid;

    public ProbeThread(DatagramSocket server, Collection<Matches> matches, int timeout, String uuid) {
        this.server = server;
        this.matches = matches;
        this.timeout = timeout;
        this.uuid = uuid;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    @Override
    public void run() {
        TraceLog.get().enter("server = " + this.server.getLocalAddress() + ", timeout = " + this.timeout, 2000);
        Thread.currentThread().setName("ProbeThread");
        String dataReceived = null;
        try {
            DatagramPacket packet = new DatagramPacket(new byte[BYTE_SIZE], BYTE_SIZE);
            this.server.setSoTimeout(this.timeout);
            long timerStarted = System.currentTimeMillis();
            while (System.currentTimeMillis() - timerStarted < (long)this.timeout) {
                this.server.receive(packet);
                dataReceived = new String(packet.getData(), 0, packet.getLength()).trim();
                TraceLog.get().message("ProbeThread -> ", dataReceived, 2000);
                Collection<Matches> collection = ProbeThread.parseProbeMatch(dataReceived, this.uuid);
                Collection<Matches> collection2 = this.matches;
                synchronized (collection2) {
                    this.matches.addAll(collection);
                }
            }
            this.server.close();
        }
        catch (SocketTimeoutException ignored) {
            this.server.close();
        }
        catch (Exception e) {
            NqException ce = new NqException("Error processing Probe Match record: " + e.getMessage(), -20);
            ce.initCause(e);
            try {
                TraceLog.get().exit("Raw data = " + dataReceived + "; ", e, 700);
                throw ce;
            }
            catch (NqException e1) {
                this.server.close();
            }
            {
                catch (Throwable throwable) {
                    this.server.close();
                    throw throwable;
                }
            }
        }
        TraceLog.get().exit(700);
    }

    private static Collection<Matches> parseProbeMatch(String data, String uuid) throws Exception {
        int colonPtr;
        String contentUuid;
        Node relatesToNode;
        TraceLog.get().enter("data = " + data, 700);
        HashSet<Matches> probeMatches = new HashSet<Matches>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(data.getBytes("UTF-8"));
        Document doc = builder.parse(input);
        XPath xPath = XPathFactory.newInstance().newXPath();
        String search = "/Envelope";
        NodeList nodeList = (NodeList)xPath.compile(search).evaluate(doc, XPathConstants.NODESET);
        Node topnode = nodeList.item(0);
        Iterator<Node> i$ = ProbeThread.getMatchedNode(topnode, ".*:RelatesTo").iterator();
        if (i$.hasNext() && (relatesToNode = i$.next()).getTextContent().length() > 0 && null != (contentUuid = relatesToNode.getTextContent()) && 0 <= (colonPtr = contentUuid.lastIndexOf(58)) && !uuid.equals(contentUuid = contentUuid.substring(colonPtr + 1))) {
            TraceLog.get().exit("ProbeMatch record returned with different UUID = " + contentUuid, 2000);
            return probeMatches;
        }
        ArrayList<Matches.WsType> deviceTypes = new ArrayList<Matches.WsType>();
        for (Node node : ProbeThread.getMatchedNode(topnode, ".*:Types")) {
            if (node.getTextContent().length() <= 0) continue;
            List<String> types = Arrays.asList(node.getTextContent().split(" "));
            for (String type : types) {
                int ptr = type.indexOf(58);
                String typ = -1 == ptr ? type : type.substring(ptr + 1);
                if (!ProbeThread.inTypesArray(typ) || 0 == typ.length()) continue;
                Matches.WsType wsType = null;
                try {
                    wsType = Matches.WsType.valueOf(typ);
                }
                catch (IllegalArgumentException e) {
                    wsType = Matches.WsType.Other;
                }
                deviceTypes.add(wsType);
            }
        }
        if (deviceTypes.size() == 0) {
            return probeMatches;
        }
        for (Node node : ProbeThread.getMatchedNode(topnode, ".*:XAddrs")) {
            if (node.getTextContent().length() <= 0) continue;
            List<String> addrs = Arrays.asList(node.getTextContent().split(" "));
            Matches.WsType[] deviceArray = deviceTypes.toArray(new Matches.WsType[deviceTypes.size()]);
            for (String s : addrs) {
                Matches pm;
                String s2;
                int ptr;
                int ptr0;
                if (s.startsWith("http://[") || s.startsWith("https://[")) {
                    ptr0 = s.indexOf(91);
                    ptr = s.indexOf(93);
                    s2 = s.substring(ptr0 + 1, ptr);
                    pm = new Matches(s2, true, deviceArray);
                    probeMatches.add(pm);
                    continue;
                }
                if (s.startsWith("http://") || s.startsWith("https://")) {
                    ptr0 = s.indexOf("//");
                    ptr = s.indexOf(58);
                    ptr = s.indexOf(58, ptr + 1);
                    s2 = s.substring(ptr0 + 2, ptr);
                    pm = new Matches(s2, false, deviceArray);
                    probeMatches.add(pm);
                    continue;
                }
                TraceLog.get().message("Unknown data found: ", s, 700);
            }
        }
        TraceLog.get().exit("probeMatches = ", probeMatches, 700);
        return probeMatches;
    }

    private static boolean inTypesArray(String testType) {
        for (String type : validTypes) {
            if (!testType.equals(type)) continue;
            return true;
        }
        return false;
    }

    private static Collection<Node> getMatchedNode(Node body, String regexp) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        if (body.getNodeName().matches(regexp)) {
            nodes.add(body);
        }
        if (body.getChildNodes().getLength() == 0) {
            return nodes;
        }
        NodeList childNodes = body.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node node = childNodes.item(i);
            nodes.addAll(ProbeThread.getMatchedNode(node, regexp));
        }
        return nodes;
    }
}

