/*
 * Decompiled with CFR 0.152.
 */
package jline.console.completer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import jline.console.ConsoleReader;
import jline.console.CursorBuffer;
import jline.console.completer.CompletionHandler;
import jline.internal.Ansi;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CandidateListCompletionHandler
implements CompletionHandler {
    private boolean printSpaceAfterFullCompletion = true;
    private boolean stripAnsi;

    public boolean getPrintSpaceAfterFullCompletion() {
        return this.printSpaceAfterFullCompletion;
    }

    public void setPrintSpaceAfterFullCompletion(boolean printSpaceAfterFullCompletion) {
        this.printSpaceAfterFullCompletion = printSpaceAfterFullCompletion;
    }

    public boolean isStripAnsi() {
        return this.stripAnsi;
    }

    public void setStripAnsi(boolean stripAnsi) {
        this.stripAnsi = stripAnsi;
    }

    @Override
    public boolean complete(ConsoleReader reader, List<CharSequence> candidates, int pos) throws IOException {
        CursorBuffer buf = reader.getCursorBuffer();
        if (candidates.size() == 1) {
            String value = Ansi.stripAnsi(candidates.get(0).toString());
            if (buf.cursor == buf.buffer.length() && this.printSpaceAfterFullCompletion && !value.endsWith(" ")) {
                value = value + " ";
            }
            if (value.equals(buf.toString())) {
                return false;
            }
            CandidateListCompletionHandler.setBuffer(reader, value, pos);
            return true;
        }
        if (candidates.size() > 1) {
            String value = this.getUnambiguousCompletions(candidates);
            CandidateListCompletionHandler.setBuffer(reader, value, pos);
        }
        CandidateListCompletionHandler.printCandidates(reader, candidates);
        reader.drawLine();
        return true;
    }

    public static void setBuffer(ConsoleReader reader, CharSequence value, int offset) throws IOException {
        while (reader.getCursorBuffer().cursor > offset && reader.backspace()) {
        }
        reader.putString(value);
        reader.setCursorPosition(offset + value.length());
    }

    public static void printCandidates(ConsoleReader reader, Collection<CharSequence> candidates) throws IOException {
        HashSet<CharSequence> distinct = new HashSet<CharSequence>(candidates);
        if (distinct.size() > reader.getAutoprintThreshold()) {
            int c;
            reader.println();
            reader.print(Messages.DISPLAY_CANDIDATES.format(distinct.size()));
            reader.flush();
            String noOpt = Messages.DISPLAY_CANDIDATES_NO.format(new Object[0]);
            String yesOpt = Messages.DISPLAY_CANDIDATES_YES.format(new Object[0]);
            char[] allowed = new char[]{yesOpt.charAt(0), noOpt.charAt(0)};
            while ((c = reader.readCharacter(allowed)) != -1) {
                String tmp = new String(new char[]{(char)c});
                if (noOpt.startsWith(tmp)) {
                    reader.println();
                    return;
                }
                if (yesOpt.startsWith(tmp)) break;
                reader.beep();
            }
        }
        if (distinct.size() != candidates.size()) {
            ArrayList<CharSequence> copy = new ArrayList<CharSequence>();
            for (CharSequence next : candidates) {
                if (copy.contains(next)) continue;
                copy.add(next);
            }
            candidates = copy;
        }
        reader.println();
        reader.printColumns(candidates);
    }

    private String getUnambiguousCompletions(List<CharSequence> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return candidates.get(0).toString();
        }
        String first = null;
        String[] strings = new String[candidates.size() - 1];
        for (int i = 0; i < candidates.size(); ++i) {
            String str = candidates.get(i).toString();
            if (this.stripAnsi) {
                str = Ansi.stripAnsi(str);
            }
            if (first == null) {
                first = str;
                continue;
            }
            strings[i - 1] = str;
        }
        StringBuilder candidate = new StringBuilder();
        for (int i = 0; i < first.length() && CandidateListCompletionHandler.startsWith(first.substring(0, i + 1), strings); ++i) {
            candidate.append(first.charAt(i));
        }
        return candidate.toString();
    }

    private static boolean startsWith(String starts, String[] candidates) {
        for (String candidate : candidates) {
            if (candidate.toLowerCase().startsWith(starts.toLowerCase())) continue;
            return false;
        }
        return true;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    private static enum Messages {
        DISPLAY_CANDIDATES,
        DISPLAY_CANDIDATES_YES,
        DISPLAY_CANDIDATES_NO;

        private static final ResourceBundle bundle;

        public String format(Object ... args) {
            if (bundle == null) {
                return "";
            }
            return String.format(bundle.getString(this.name()), args);
        }

        static {
            bundle = ResourceBundle.getBundle(CandidateListCompletionHandler.class.getName(), Locale.getDefault());
        }
    }
}

