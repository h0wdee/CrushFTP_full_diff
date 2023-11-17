/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.connection;

import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.connection.Request;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class OutstandingRequests {
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<Long, Request> lookup = new HashMap<Long, Request>();
    private Map<UUID, Request> cancelLookup = new HashMap<UUID, Request>();

    OutstandingRequests() {
    }

    boolean isOutstanding(Long messageId) {
        this.lock.readLock().lock();
        try {
            boolean bl = this.lookup.containsKey(messageId);
            return bl;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    Request getRequestByMessageId(Long messageId) {
        this.lock.readLock().lock();
        try {
            Request request = this.lookup.get(messageId);
            return request;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    Request getRequestByCancelId(UUID cancelId) {
        this.lock.readLock().lock();
        try {
            Request request = this.cancelLookup.get(cancelId);
            return request;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    Request receivedResponseFor(Long messageId) {
        this.lock.writeLock().lock();
        try {
            Request r = this.lookup.remove(messageId);
            if (r == null) {
                throw new SMBRuntimeException("Unable to find outstanding request for messageId " + messageId);
            }
            this.cancelLookup.remove(r.getCancelId());
            Request request = r;
            return request;
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    void registerOutstanding(Request request) {
        this.lock.writeLock().lock();
        try {
            this.lookup.put(request.getMessageId(), request);
            this.cancelLookup.put(request.getCancelId(), request);
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void handleError(Throwable t) {
        this.lock.writeLock().lock();
        try {
            for (Long id : new HashSet<Long>(this.lookup.keySet())) {
                Request removed = this.lookup.remove(id);
                this.cancelLookup.remove(removed.getCancelId());
                removed.getPromise().deliverError(t);
            }
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }
}

