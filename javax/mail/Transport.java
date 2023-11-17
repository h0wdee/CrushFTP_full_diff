/*
 * Decompiled with CFR 0.152.
 */
package javax.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Service;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;

public abstract class Transport
extends Service {
    private volatile Vector<TransportListener> transportListeners = null;

    public Transport(Session session, URLName urlname) {
        super(session, urlname);
    }

    public static void send(Message msg) throws MessagingException {
        msg.saveChanges();
        Transport.send0(msg, msg.getAllRecipients(), null, null);
    }

    public static void send(Message msg, Address[] addresses) throws MessagingException {
        msg.saveChanges();
        Transport.send0(msg, addresses, null, null);
    }

    public static void send(Message msg, String user, String password) throws MessagingException {
        msg.saveChanges();
        Transport.send0(msg, msg.getAllRecipients(), user, password);
    }

    public static void send(Message msg, Address[] addresses, String user, String password) throws MessagingException {
        msg.saveChanges();
        Transport.send0(msg, addresses, user, password);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void send0(Message msg, Address[] addresses, String user, String password) throws MessagingException {
        Session s;
        if (addresses == null || addresses.length == 0) {
            throw new SendFailedException("No recipient addresses");
        }
        HashMap protocols = new HashMap();
        ArrayList<Address> invalid = new ArrayList<Address>();
        ArrayList<Address> validSent = new ArrayList<Address>();
        ArrayList<Address> validUnsent = new ArrayList<Address>();
        for (int i = 0; i < addresses.length; ++i) {
            if (protocols.containsKey(addresses[i].getType())) {
                List v = (List)protocols.get(addresses[i].getType());
                v.add(addresses[i]);
                continue;
            }
            ArrayList<Address> w = new ArrayList<Address>();
            w.add(addresses[i]);
            protocols.put(addresses[i].getType(), w);
        }
        int dsize = protocols.size();
        if (dsize == 0) {
            throw new SendFailedException("No recipient addresses");
        }
        Session session = s = msg.session != null ? msg.session : Session.getDefaultInstance(System.getProperties(), null);
        if (dsize == 1) {
            Transport transport = s.getTransport(addresses[0]);
            try {
                if (user != null) {
                    transport.connect(user, password);
                } else {
                    transport.connect();
                }
                transport.sendMessage(msg, addresses);
            }
            finally {
                transport.close();
            }
            return;
        }
        MessagingException chainedEx = null;
        boolean sendFailed = false;
        for (List v : protocols.values()) {
            Address[] protaddresses = new Address[v.size()];
            v.toArray(protaddresses);
            Transport transport = s.getTransport(protaddresses[0]);
            if (transport == null) {
                for (int j = 0; j < protaddresses.length; ++j) {
                    invalid.add(protaddresses[j]);
                }
                continue;
            }
            try {
                transport.connect();
                transport.sendMessage(msg, protaddresses);
            }
            catch (SendFailedException sex) {
                Address[] c;
                sendFailed = true;
                if (chainedEx == null) {
                    chainedEx = sex;
                } else {
                    chainedEx.setNextException(sex);
                }
                Address[] a = sex.getInvalidAddresses();
                if (a != null) {
                    for (int j = 0; j < a.length; ++j) {
                        invalid.add(a[j]);
                    }
                }
                if ((a = sex.getValidSentAddresses()) != null) {
                    for (int k = 0; k < a.length; ++k) {
                        validSent.add(a[k]);
                    }
                }
                if ((c = sex.getValidUnsentAddresses()) == null) continue;
                for (int l = 0; l < c.length; ++l) {
                    validUnsent.add(c[l]);
                }
            }
            catch (MessagingException mex) {
                sendFailed = true;
                if (chainedEx == null) {
                    chainedEx = mex;
                    continue;
                }
                chainedEx.setNextException(mex);
            }
            finally {
                transport.close();
            }
        }
        if (sendFailed || invalid.size() != 0 || validUnsent.size() != 0) {
            Address[] a = null;
            Address[] b = null;
            Address[] c = null;
            if (validSent.size() > 0) {
                a = new Address[validSent.size()];
                validSent.toArray(a);
            }
            if (validUnsent.size() > 0) {
                b = new Address[validUnsent.size()];
                validUnsent.toArray(b);
            }
            if (invalid.size() > 0) {
                c = new Address[invalid.size()];
                invalid.toArray(c);
            }
            throw new SendFailedException("Sending failed", chainedEx, a, b, c);
        }
    }

    public abstract void sendMessage(Message var1, Address[] var2) throws MessagingException;

    public synchronized void addTransportListener(TransportListener l) {
        if (this.transportListeners == null) {
            this.transportListeners = new Vector();
        }
        this.transportListeners.addElement(l);
    }

    public synchronized void removeTransportListener(TransportListener l) {
        if (this.transportListeners != null) {
            this.transportListeners.removeElement(l);
        }
    }

    protected void notifyTransportListeners(int type, Address[] validSent, Address[] validUnsent, Address[] invalid, Message msg) {
        if (this.transportListeners == null) {
            return;
        }
        TransportEvent e = new TransportEvent(this, type, validSent, validUnsent, invalid, msg);
        this.queueEvent(e, this.transportListeners);
    }
}

