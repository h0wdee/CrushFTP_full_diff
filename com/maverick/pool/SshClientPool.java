/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.pool;

import com.maverick.pool.SshClientFactory;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshConnector;
import com.maverick.ssh.SshException;
import com.maverick.ssh2.Ssh2Client;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshClientPool {
    static Logger log = LoggerFactory.getLogger(SshClientPool.class);
    Map<String, LinkedList<SshClient>> clients = new HashMap<String, LinkedList<SshClient>>();
    ExecutorService executor;
    SshClientFactory factory;
    boolean running = false;
    long processingInterval;
    long clientSocketTimeout = 1000L;

    public SshClientPool(ExecutorService executor, SshClientFactory factory) {
        this(executor, factory, 30000L);
    }

    public SshClientPool(ExecutorService executor, SshClientFactory factory, long processingInterval) {
        if (processingInterval < 1000L) {
            throw new IllegalArgumentException("Processing interval must be >= 1000ms");
        }
        this.executor = executor;
        this.factory = factory;
        this.processingInterval = processingInterval;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized void checkin(SshClient client) {
        String key = client.getTransport().getHost() + ":" + client.getTransport().getPort() + ":" + client.getUsername();
        if (!this.clients.containsKey(key)) {
            this.clients.put(key, new LinkedList());
        }
        LinkedList<SshClient> list = this.clients.get(key);
        Map<String, LinkedList<SshClient>> map = this.clients;
        synchronized (map) {
            list.addLast(client);
        }
        if (!this.running) {
            this.running = true;
            this.executor.execute(new SshClientPoolTask());
        }
    }

    public synchronized SshClient checkout(String host, int port, String username) throws SshException, IOException {
        return this.checkout(host, port, username, this.factory);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized SshClient checkout(String host, int port, String username, SshClientFactory factory) throws SshException, IOException {
        String key = host + ":" + port + ":" + username;
        if (this.clients.containsKey(key)) {
            LinkedList<SshClient> list;
            LinkedList<SshClient> linkedList = list = this.clients.get(key);
            synchronized (linkedList) {
                if (!list.isEmpty()) {
                    return list.removeFirst();
                }
            }
        }
        SshConnector con = SshConnector.createInstance(this.executor);
        factory.configureConnector(con);
        SshClient client = con.connect(factory.createTransport(host, port), username);
        factory.authenticateClient(client);
        if (!client.isAuthenticated()) {
            throw new SshException("Client should be authenticated after call to SshClientFactory.authenticateClient", 4);
        }
        return client;
    }

    public void setClientSocketTimeout(long clientSocketTimeout) {
        this.clientSocketTimeout = clientSocketTimeout;
    }

    protected boolean processIdle(SshClient client) {
        if (!client.isConnected()) {
            return false;
        }
        if (client instanceof Ssh2Client) {
            Ssh2Client ssh = (Ssh2Client)client;
            try {
                ssh.processMessages(this.clientSocketTimeout);
            }
            catch (SshException e) {
                return false;
            }
        }
        return true;
    }

    public synchronized int getClientCount() {
        int count = 0;
        for (LinkedList<SshClient> list : this.clients.values()) {
            count += list.size();
        }
        return count;
    }

    public synchronized int getClientCount(String host, int port, String username) {
        String key = host + ":" + port + ":" + username;
        if (this.clients.containsKey(key)) {
            return this.clients.get(key).size();
        }
        return 0;
    }

    class SshClientPoolTask
    implements Runnable {
        SshClientPoolTask() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            if (log.isInfoEnabled()) {
                log.info("Started client pool idle processor");
            }
            while (SshClientPool.this.getClientCount() > 0) {
                long started = System.currentTimeMillis();
                HashSet<String> keys = new HashSet<String>(SshClientPool.this.clients.keySet());
                for (String key : keys) {
                    SshClient client;
                    LinkedList<SshClient> list = SshClientPool.this.clients.get(key);
                    if (list.isEmpty()) continue;
                    LinkedList<SshClient> linkedList = list;
                    synchronized (linkedList) {
                        client = list.removeFirst();
                    }
                    if (!SshClientPool.this.processIdle(client)) continue;
                    linkedList = list;
                    synchronized (linkedList) {
                        list.addLast(client);
                    }
                }
                long delay = SshClientPool.this.processingInterval - (System.currentTimeMillis() - started);
                if (delay <= 0L) continue;
                try {
                    Thread.sleep(delay);
                }
                catch (InterruptedException interruptedException) {}
            }
            if (log.isInfoEnabled()) {
                log.info("There are no more clients left in the pool so idle processor is shutting down");
            }
            SshClientPoolTask sshClientPoolTask = this;
            synchronized (sshClientPoolTask) {
                SshClientPool.this.running = false;
            }
        }
    }
}

