/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.resolve;

import com.visuality.nq.client.Client;
import com.visuality.nq.common.NqException;
import com.visuality.nq.config.Config;
import com.visuality.nq.resolve.Matches;
import com.visuality.nq.resolve.WsdProbe;
import java.util.Collection;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class WsdClient {
    public static int WS_TIMEOUT = 5000;
    private int timeout = WS_TIMEOUT;

    public WsdClient(int timeToWait) {
        this.timeout = timeToWait;
    }

    public WsdClient() {
    }

    public Collection<Matches> probe() {
        WsdProbe wsdProbe = new WsdProbe();
        if (0 == this.timeout) {
            return wsdProbe.matches;
        }
        Collection<Matches> probeMatches = wsdProbe.probe(this.timeout);
        return probeMatches;
    }

    public Collection<Matches> resolve() {
        throw new UnsupportedOperationException("WsdClient.resolve() method is not yet implemented.");
    }

    public static void main(String[] args) throws NqException {
        if (1 == args.length && args[0].equals("-d")) {
            Config.jnq.set("LOGFILE", "jnq.log");
            Config.jnq.set("LOGTHRESHOLD", 2000);
            Config.jnq.set("LOGMAXRECORDSINFILE", 1000000);
            Config.jnq.set("LOGTOFILE", true);
        }
        WsdClient wsdClient = new WsdClient(4000);
        Collection<Matches> probeMatches = wsdClient.probe();
        System.out.println("Results of probe...");
        for (Matches pm : probeMatches) {
            System.out.println(pm);
        }
        Client.stop();
    }
}

