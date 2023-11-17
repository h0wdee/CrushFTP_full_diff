/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.imap;

import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.ResyncData;
import com.sun.mail.imap.protocol.MessageSet;
import com.sun.mail.imap.protocol.UIDSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import javax.mail.Message;

public final class Utility {
    private Utility() {
    }

    public static MessageSet[] toMessageSet(Message[] msgs, Condition cond) {
        ArrayList<MessageSet> v = new ArrayList<MessageSet>(1);
        for (int i = 0; i < msgs.length; ++i) {
            IMAPMessage msg = (IMAPMessage)msgs[i];
            if (msg.isExpunged()) continue;
            int current = msg.getSequenceNumber();
            if (cond != null && !cond.test(msg)) continue;
            MessageSet set = new MessageSet();
            set.start = current;
            ++i;
            while (i < msgs.length) {
                msg = (IMAPMessage)msgs[i];
                if (!msg.isExpunged()) {
                    int next = msg.getSequenceNumber();
                    if (cond == null || cond.test(msg)) {
                        if (next == current + 1) {
                            current = next;
                        } else {
                            --i;
                            break;
                        }
                    }
                }
                ++i;
            }
            set.end = current;
            v.add(set);
        }
        if (v.isEmpty()) {
            return null;
        }
        return v.toArray(new MessageSet[v.size()]);
    }

    public static MessageSet[] toMessageSetSorted(Message[] msgs, Condition cond) {
        msgs = (Message[])msgs.clone();
        Arrays.sort(msgs, new Comparator<Message>(){

            @Override
            public int compare(Message msg1, Message msg2) {
                return msg1.getMessageNumber() - msg2.getMessageNumber();
            }
        });
        return Utility.toMessageSet(msgs, cond);
    }

    public static UIDSet[] toUIDSet(Message[] msgs) {
        ArrayList<UIDSet> v = new ArrayList<UIDSet>(1);
        for (int i = 0; i < msgs.length; ++i) {
            IMAPMessage msg = (IMAPMessage)msgs[i];
            if (msg.isExpunged()) continue;
            long current = msg.getUID();
            UIDSet set = new UIDSet();
            set.start = current;
            ++i;
            while (i < msgs.length) {
                msg = (IMAPMessage)msgs[i];
                if (!msg.isExpunged()) {
                    long next = msg.getUID();
                    if (next == current + 1L) {
                        current = next;
                    } else {
                        --i;
                        break;
                    }
                }
                ++i;
            }
            set.end = current;
            v.add(set);
        }
        if (v.isEmpty()) {
            return null;
        }
        return v.toArray(new UIDSet[v.size()]);
    }

    public static UIDSet[] getResyncUIDSet(ResyncData rd) {
        return rd.getUIDSet();
    }

    public static interface Condition {
        public boolean test(IMAPMessage var1);
    }
}

