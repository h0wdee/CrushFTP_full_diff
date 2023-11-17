/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.Item;
import javax.mail.Flags;

public class FLAGS
extends Flags
implements Item {
    static final char[] name = new char[]{'F', 'L', 'A', 'G', 'S'};
    public int msgno;
    private static final long serialVersionUID = 439049847053756670L;

    public FLAGS(IMAPResponse r) throws ParsingException {
        this.msgno = r.getNumber();
        r.skipSpaces();
        String[] flags = r.readSimpleList();
        if (flags != null) {
            block8: for (int i = 0; i < flags.length; ++i) {
                String s = flags[i];
                if (s.length() >= 2 && s.charAt(0) == '\\') {
                    switch (Character.toUpperCase(s.charAt(1))) {
                        case 'S': {
                            this.add(Flags.Flag.SEEN);
                            break;
                        }
                        case 'R': {
                            this.add(Flags.Flag.RECENT);
                            break;
                        }
                        case 'D': {
                            if (s.length() >= 3) {
                                char c = s.charAt(2);
                                if (c == 'e' || c == 'E') {
                                    this.add(Flags.Flag.DELETED);
                                    break;
                                }
                                if (c != 'r' && c != 'R') continue block8;
                                this.add(Flags.Flag.DRAFT);
                                break;
                            }
                            this.add(s);
                            break;
                        }
                        case 'A': {
                            this.add(Flags.Flag.ANSWERED);
                            break;
                        }
                        case 'F': {
                            this.add(Flags.Flag.FLAGGED);
                            break;
                        }
                        case '*': {
                            this.add(Flags.Flag.USER);
                            break;
                        }
                        default: {
                            this.add(s);
                            break;
                        }
                    }
                    continue;
                }
                this.add(s);
            }
        }
    }
}

