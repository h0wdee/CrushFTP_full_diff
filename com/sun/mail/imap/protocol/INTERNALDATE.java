/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.Item;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.mail.internet.MailDateFormat;

public class INTERNALDATE
implements Item {
    static final char[] name = new char[]{'I', 'N', 'T', 'E', 'R', 'N', 'A', 'L', 'D', 'A', 'T', 'E'};
    public int msgno;
    protected Date date;
    private static final MailDateFormat mailDateFormat = new MailDateFormat();
    private static SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss ", Locale.US);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public INTERNALDATE(FetchResponse r) throws ParsingException {
        this.msgno = r.getNumber();
        r.skipSpaces();
        String s = r.readString();
        if (s == null) {
            throw new ParsingException("INTERNALDATE is NIL");
        }
        try {
            MailDateFormat mailDateFormat = INTERNALDATE.mailDateFormat;
            synchronized (mailDateFormat) {
                this.date = INTERNALDATE.mailDateFormat.parse(s);
            }
        }
        catch (ParseException pex) {
            throw new ParsingException("INTERNALDATE parse error");
        }
    }

    public Date getDate() {
        return this.date;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String format(Date d) {
        StringBuffer sb = new StringBuffer();
        SimpleDateFormat simpleDateFormat = df;
        synchronized (simpleDateFormat) {
            df.format(d, sb, new FieldPosition(0));
        }
        TimeZone tz = TimeZone.getDefault();
        int offset = tz.getOffset(d.getTime());
        int rawOffsetInMins = offset / 60 / 1000;
        if (rawOffsetInMins < 0) {
            sb.append('-');
            rawOffsetInMins = -rawOffsetInMins;
        } else {
            sb.append('+');
        }
        int offsetInHrs = rawOffsetInMins / 60;
        int offsetInMins = rawOffsetInMins % 60;
        sb.append(Character.forDigit(offsetInHrs / 10, 10));
        sb.append(Character.forDigit(offsetInHrs % 10, 10));
        sb.append(Character.forDigit(offsetInMins / 10, 10));
        sb.append(Character.forDigit(offsetInMins % 10, 10));
        return sb.toString();
    }
}

