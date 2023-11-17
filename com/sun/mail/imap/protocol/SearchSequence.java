/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.imap.protocol;

import com.sun.mail.iap.Argument;
import com.sun.mail.imap.ModifiedSinceTerm;
import com.sun.mail.imap.OlderTerm;
import com.sun.mail.imap.YoungerTerm;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.search.AddressTerm;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.DateTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.HeaderTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.RecipientTerm;
import javax.mail.search.SearchException;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import javax.mail.search.SizeTerm;
import javax.mail.search.StringTerm;
import javax.mail.search.SubjectTerm;

public class SearchSequence {
    private static String[] monthTable = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    protected Calendar cal = new GregorianCalendar();

    public Argument generateSequence(SearchTerm term, String charset) throws SearchException, IOException {
        if (term instanceof AndTerm) {
            return this.and((AndTerm)term, charset);
        }
        if (term instanceof OrTerm) {
            return this.or((OrTerm)term, charset);
        }
        if (term instanceof NotTerm) {
            return this.not((NotTerm)term, charset);
        }
        if (term instanceof HeaderTerm) {
            return this.header((HeaderTerm)term, charset);
        }
        if (term instanceof FlagTerm) {
            return this.flag((FlagTerm)term);
        }
        if (term instanceof FromTerm) {
            FromTerm fterm = (FromTerm)term;
            return this.from(fterm.getAddress().toString(), charset);
        }
        if (term instanceof FromStringTerm) {
            FromStringTerm fterm = (FromStringTerm)term;
            return this.from(fterm.getPattern(), charset);
        }
        if (term instanceof RecipientTerm) {
            RecipientTerm rterm = (RecipientTerm)term;
            return this.recipient(rterm.getRecipientType(), rterm.getAddress().toString(), charset);
        }
        if (term instanceof RecipientStringTerm) {
            RecipientStringTerm rterm = (RecipientStringTerm)term;
            return this.recipient(rterm.getRecipientType(), rterm.getPattern(), charset);
        }
        if (term instanceof SubjectTerm) {
            return this.subject((SubjectTerm)term, charset);
        }
        if (term instanceof BodyTerm) {
            return this.body((BodyTerm)term, charset);
        }
        if (term instanceof SizeTerm) {
            return this.size((SizeTerm)term);
        }
        if (term instanceof SentDateTerm) {
            return this.sentdate((SentDateTerm)term);
        }
        if (term instanceof ReceivedDateTerm) {
            return this.receiveddate((ReceivedDateTerm)term);
        }
        if (term instanceof OlderTerm) {
            return this.older((OlderTerm)term);
        }
        if (term instanceof YoungerTerm) {
            return this.younger((YoungerTerm)term);
        }
        if (term instanceof MessageIDTerm) {
            return this.messageid((MessageIDTerm)term, charset);
        }
        if (term instanceof ModifiedSinceTerm) {
            return this.modifiedSince((ModifiedSinceTerm)term);
        }
        throw new SearchException("Search too complex");
    }

    public static boolean isAscii(SearchTerm term) {
        if (term instanceof AndTerm) {
            return SearchSequence.isAscii(((AndTerm)term).getTerms());
        }
        if (term instanceof OrTerm) {
            return SearchSequence.isAscii(((OrTerm)term).getTerms());
        }
        if (term instanceof NotTerm) {
            return SearchSequence.isAscii(((NotTerm)term).getTerm());
        }
        if (term instanceof StringTerm) {
            return SearchSequence.isAscii(((StringTerm)term).getPattern());
        }
        if (term instanceof AddressTerm) {
            return SearchSequence.isAscii(((AddressTerm)term).getAddress().toString());
        }
        return true;
    }

    public static boolean isAscii(SearchTerm[] terms) {
        for (int i = 0; i < terms.length; ++i) {
            if (SearchSequence.isAscii(terms[i])) continue;
            return false;
        }
        return true;
    }

    public static boolean isAscii(String s) {
        int l = s.length();
        for (int i = 0; i < l; ++i) {
            if (s.charAt(i) <= '\u007f') continue;
            return false;
        }
        return true;
    }

    protected Argument and(AndTerm term, String charset) throws SearchException, IOException {
        SearchTerm[] terms = term.getTerms();
        Argument result = this.generateSequence(terms[0], charset);
        for (int i = 1; i < terms.length; ++i) {
            result.append(this.generateSequence(terms[i], charset));
        }
        return result;
    }

    protected Argument or(OrTerm term, String charset) throws SearchException, IOException {
        SearchTerm[] terms = term.getTerms();
        if (terms.length > 2) {
            SearchTerm t = terms[0];
            for (int i = 1; i < terms.length; ++i) {
                t = new OrTerm(t, terms[i]);
            }
            term = (OrTerm)t;
            terms = term.getTerms();
        }
        Argument result = new Argument();
        if (terms.length > 1) {
            result.writeAtom("OR");
        }
        if (terms[0] instanceof AndTerm || terms[0] instanceof FlagTerm) {
            result.writeArgument(this.generateSequence(terms[0], charset));
        } else {
            result.append(this.generateSequence(terms[0], charset));
        }
        if (terms.length > 1) {
            if (terms[1] instanceof AndTerm || terms[1] instanceof FlagTerm) {
                result.writeArgument(this.generateSequence(terms[1], charset));
            } else {
                result.append(this.generateSequence(terms[1], charset));
            }
        }
        return result;
    }

    protected Argument not(NotTerm term, String charset) throws SearchException, IOException {
        Argument result = new Argument();
        result.writeAtom("NOT");
        SearchTerm nterm = term.getTerm();
        if (nterm instanceof AndTerm || nterm instanceof FlagTerm) {
            result.writeArgument(this.generateSequence(nterm, charset));
        } else {
            result.append(this.generateSequence(nterm, charset));
        }
        return result;
    }

    protected Argument header(HeaderTerm term, String charset) throws SearchException, IOException {
        Argument result = new Argument();
        result.writeAtom("HEADER");
        result.writeString(term.getHeaderName());
        result.writeString(term.getPattern(), charset);
        return result;
    }

    protected Argument messageid(MessageIDTerm term, String charset) throws SearchException, IOException {
        Argument result = new Argument();
        result.writeAtom("HEADER");
        result.writeString("Message-ID");
        result.writeString(term.getPattern(), charset);
        return result;
    }

    protected Argument flag(FlagTerm term) throws SearchException {
        int i;
        boolean set = term.getTestSet();
        Argument result = new Argument();
        Flags flags = term.getFlags();
        Flags.Flag[] sf = flags.getSystemFlags();
        String[] uf = flags.getUserFlags();
        if (sf.length == 0 && uf.length == 0) {
            throw new SearchException("Invalid FlagTerm");
        }
        for (i = 0; i < sf.length; ++i) {
            if (sf[i] == Flags.Flag.DELETED) {
                result.writeAtom(set ? "DELETED" : "UNDELETED");
                continue;
            }
            if (sf[i] == Flags.Flag.ANSWERED) {
                result.writeAtom(set ? "ANSWERED" : "UNANSWERED");
                continue;
            }
            if (sf[i] == Flags.Flag.DRAFT) {
                result.writeAtom(set ? "DRAFT" : "UNDRAFT");
                continue;
            }
            if (sf[i] == Flags.Flag.FLAGGED) {
                result.writeAtom(set ? "FLAGGED" : "UNFLAGGED");
                continue;
            }
            if (sf[i] == Flags.Flag.RECENT) {
                result.writeAtom(set ? "RECENT" : "OLD");
                continue;
            }
            if (sf[i] != Flags.Flag.SEEN) continue;
            result.writeAtom(set ? "SEEN" : "UNSEEN");
        }
        for (i = 0; i < uf.length; ++i) {
            result.writeAtom(set ? "KEYWORD" : "UNKEYWORD");
            result.writeAtom(uf[i]);
        }
        return result;
    }

    protected Argument from(String address, String charset) throws SearchException, IOException {
        Argument result = new Argument();
        result.writeAtom("FROM");
        result.writeString(address, charset);
        return result;
    }

    protected Argument recipient(Message.RecipientType type, String address, String charset) throws SearchException, IOException {
        Argument result = new Argument();
        if (type == Message.RecipientType.TO) {
            result.writeAtom("TO");
        } else if (type == Message.RecipientType.CC) {
            result.writeAtom("CC");
        } else if (type == Message.RecipientType.BCC) {
            result.writeAtom("BCC");
        } else {
            throw new SearchException("Illegal Recipient type");
        }
        result.writeString(address, charset);
        return result;
    }

    protected Argument subject(SubjectTerm term, String charset) throws SearchException, IOException {
        Argument result = new Argument();
        result.writeAtom("SUBJECT");
        result.writeString(term.getPattern(), charset);
        return result;
    }

    protected Argument body(BodyTerm term, String charset) throws SearchException, IOException {
        Argument result = new Argument();
        result.writeAtom("BODY");
        result.writeString(term.getPattern(), charset);
        return result;
    }

    protected Argument size(SizeTerm term) throws SearchException {
        Argument result = new Argument();
        switch (term.getComparison()) {
            case 5: {
                result.writeAtom("LARGER");
                break;
            }
            case 2: {
                result.writeAtom("SMALLER");
                break;
            }
            default: {
                throw new SearchException("Cannot handle Comparison");
            }
        }
        result.writeNumber(term.getNumber());
        return result;
    }

    protected String toIMAPDate(Date date) {
        StringBuffer s = new StringBuffer();
        this.cal.setTime(date);
        s.append(this.cal.get(5)).append("-");
        s.append(monthTable[this.cal.get(2)]).append('-');
        s.append(this.cal.get(1));
        return s.toString();
    }

    protected Argument sentdate(DateTerm term) throws SearchException {
        Argument result = new Argument();
        String date = this.toIMAPDate(term.getDate());
        switch (term.getComparison()) {
            case 5: {
                result.writeAtom("NOT SENTON " + date + " SENTSINCE " + date);
                break;
            }
            case 3: {
                result.writeAtom("SENTON " + date);
                break;
            }
            case 2: {
                result.writeAtom("SENTBEFORE " + date);
                break;
            }
            case 6: {
                result.writeAtom("SENTSINCE " + date);
                break;
            }
            case 1: {
                result.writeAtom("OR SENTBEFORE " + date + " SENTON " + date);
                break;
            }
            case 4: {
                result.writeAtom("NOT SENTON " + date);
                break;
            }
            default: {
                throw new SearchException("Cannot handle Date Comparison");
            }
        }
        return result;
    }

    protected Argument receiveddate(DateTerm term) throws SearchException {
        Argument result = new Argument();
        String date = this.toIMAPDate(term.getDate());
        switch (term.getComparison()) {
            case 5: {
                result.writeAtom("NOT ON " + date + " SINCE " + date);
                break;
            }
            case 3: {
                result.writeAtom("ON " + date);
                break;
            }
            case 2: {
                result.writeAtom("BEFORE " + date);
                break;
            }
            case 6: {
                result.writeAtom("SINCE " + date);
                break;
            }
            case 1: {
                result.writeAtom("OR BEFORE " + date + " ON " + date);
                break;
            }
            case 4: {
                result.writeAtom("NOT ON " + date);
                break;
            }
            default: {
                throw new SearchException("Cannot handle Date Comparison");
            }
        }
        return result;
    }

    protected Argument older(OlderTerm term) throws SearchException {
        Argument result = new Argument();
        result.writeAtom("OLDER");
        result.writeNumber(term.getInterval());
        return result;
    }

    protected Argument younger(YoungerTerm term) throws SearchException {
        Argument result = new Argument();
        result.writeAtom("YOUNGER");
        result.writeNumber(term.getInterval());
        return result;
    }

    protected Argument modifiedSince(ModifiedSinceTerm term) throws SearchException {
        Argument result = new Argument();
        result.writeAtom("MODSEQ");
        result.writeNumber(term.getModSeq());
        return result;
    }
}

