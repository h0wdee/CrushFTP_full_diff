/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.engio.mbassy.bus.SyncMessageBus
 *  net.engio.mbassy.bus.common.PubSubSupport
 *  net.engio.mbassy.bus.error.IPublicationErrorHandler
 *  net.engio.mbassy.bus.error.PublicationError
 */
package com.hierynomus.smbj.event;

import com.hierynomus.smbj.event.SMBEvent;
import net.engio.mbassy.bus.SyncMessageBus;
import net.engio.mbassy.bus.common.PubSubSupport;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMBEventBus {
    private static final Logger logger = LoggerFactory.getLogger(SMBEventBus.class);
    private PubSubSupport<SMBEvent> wrappedBus;

    public SMBEventBus() {
        this((PubSubSupport<SMBEvent>)new SyncMessageBus(new IPublicationErrorHandler(){

            public void handleError(PublicationError error) {
                if (error.getCause() != null) {
                    logger.error(error.toString(), error.getCause());
                } else {
                    logger.error(error.toString());
                }
            }
        }));
    }

    public SMBEventBus(PubSubSupport<SMBEvent> wrappedBus) {
        this.wrappedBus = wrappedBus;
    }

    public void subscribe(Object listener) {
        this.wrappedBus.subscribe(listener);
    }

    public boolean unsubscribe(Object listener) {
        return this.wrappedBus.unsubscribe(listener);
    }

    public void publish(SMBEvent message) {
        this.wrappedBus.publish((Object)message);
    }
}

