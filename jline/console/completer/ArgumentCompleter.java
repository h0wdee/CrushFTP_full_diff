/*
 * Decompiled with CFR 0.152.
 */
package jline.console.completer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import jline.console.completer.Completer;
import jline.internal.Log;
import jline.internal.Preconditions;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ArgumentCompleter
implements Completer {
    private final ArgumentDelimiter delimiter;
    private final List<Completer> completers = new ArrayList<Completer>();
    private boolean strict = true;

    public ArgumentCompleter(ArgumentDelimiter delimiter, Collection<Completer> completers) {
        this.delimiter = Preconditions.checkNotNull(delimiter);
        Preconditions.checkNotNull(completers);
        this.completers.addAll(completers);
    }

    public ArgumentCompleter(ArgumentDelimiter delimiter, Completer ... completers) {
        this(delimiter, Arrays.asList(completers));
    }

    public ArgumentCompleter(Completer ... completers) {
        this((ArgumentDelimiter)new WhitespaceArgumentDelimiter(), completers);
    }

    public ArgumentCompleter(List<Completer> completers) {
        this((ArgumentDelimiter)new WhitespaceArgumentDelimiter(), completers);
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public boolean isStrict() {
        return this.strict;
    }

    public ArgumentDelimiter getDelimiter() {
        return this.delimiter;
    }

    public List<Completer> getCompleters() {
        return this.completers;
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        Preconditions.checkNotNull(candidates);
        ArgumentDelimiter delim = this.getDelimiter();
        ArgumentList list = delim.delimit(buffer, cursor);
        int argpos = list.getArgumentPosition();
        int argIndex = list.getCursorArgumentIndex();
        if (argIndex < 0) {
            return -1;
        }
        List<Completer> completers = this.getCompleters();
        Completer completer = argIndex >= completers.size() ? completers.get(completers.size() - 1) : completers.get(argIndex);
        for (int i = 0; this.isStrict() && i < argIndex; ++i) {
            Completer sub = completers.get(i >= completers.size() ? completers.size() - 1 : i);
            String[] args = list.getArguments();
            String arg = args == null || i >= args.length ? "" : args[i];
            LinkedList<CharSequence> subCandidates = new LinkedList<CharSequence>();
            if (sub.complete(arg, arg.length(), subCandidates) == -1) {
                return -1;
            }
            if (subCandidates.contains(arg)) continue;
            return -1;
        }
        int ret = completer.complete(list.getCursorArgument(), argpos, candidates);
        if (ret == -1) {
            return -1;
        }
        int pos = ret + list.getBufferPosition() - argpos;
        if (cursor != buffer.length() && delim.isDelimiter(buffer, cursor)) {
            for (int i = 0; i < candidates.size(); ++i) {
                CharSequence val = candidates.get(i);
                while (val.length() > 0 && delim.isDelimiter(val, val.length() - 1)) {
                    val = val.subSequence(0, val.length() - 1);
                }
                candidates.set(i, val);
            }
        }
        Log.trace("Completing ", buffer, " (pos=", cursor, ") with: ", candidates, ": offset=", pos);
        return pos;
    }

    public static class ArgumentList {
        private String[] arguments;
        private int cursorArgumentIndex;
        private int argumentPosition;
        private int bufferPosition;

        public ArgumentList(String[] arguments, int cursorArgumentIndex, int argumentPosition, int bufferPosition) {
            this.arguments = Preconditions.checkNotNull(arguments);
            this.cursorArgumentIndex = cursorArgumentIndex;
            this.argumentPosition = argumentPosition;
            this.bufferPosition = bufferPosition;
        }

        public void setCursorArgumentIndex(int i) {
            this.cursorArgumentIndex = i;
        }

        public int getCursorArgumentIndex() {
            return this.cursorArgumentIndex;
        }

        public String getCursorArgument() {
            if (this.cursorArgumentIndex < 0 || this.cursorArgumentIndex >= this.arguments.length) {
                return null;
            }
            return this.arguments[this.cursorArgumentIndex];
        }

        public void setArgumentPosition(int pos) {
            this.argumentPosition = pos;
        }

        public int getArgumentPosition() {
            return this.argumentPosition;
        }

        public void setArguments(String[] arguments) {
            this.arguments = arguments;
        }

        public String[] getArguments() {
            return this.arguments;
        }

        public void setBufferPosition(int pos) {
            this.bufferPosition = pos;
        }

        public int getBufferPosition() {
            return this.bufferPosition;
        }
    }

    public static class WhitespaceArgumentDelimiter
    extends AbstractArgumentDelimiter {
        public boolean isDelimiterChar(CharSequence buffer, int pos) {
            return Character.isWhitespace(buffer.charAt(pos));
        }
    }

    public static abstract class AbstractArgumentDelimiter
    implements ArgumentDelimiter {
        private char[] quoteChars = new char[]{'\'', '\"'};
        private char[] escapeChars = new char[]{'\\'};

        public void setQuoteChars(char[] chars) {
            this.quoteChars = chars;
        }

        public char[] getQuoteChars() {
            return this.quoteChars;
        }

        public void setEscapeChars(char[] chars) {
            this.escapeChars = chars;
        }

        public char[] getEscapeChars() {
            return this.escapeChars;
        }

        public ArgumentList delimit(CharSequence buffer, int cursor) {
            LinkedList<String> args = new LinkedList<String>();
            StringBuilder arg = new StringBuilder();
            int argpos = -1;
            int bindex = -1;
            int quoteStart = -1;
            for (int i = 0; buffer != null && i < buffer.length(); ++i) {
                if (i == cursor) {
                    bindex = args.size();
                    argpos = arg.length();
                }
                if (quoteStart < 0 && this.isQuoteChar(buffer, i)) {
                    quoteStart = i;
                    continue;
                }
                if (quoteStart >= 0) {
                    if (buffer.charAt(quoteStart) == buffer.charAt(i) && !this.isEscaped(buffer, i)) {
                        args.add(arg.toString());
                        arg.setLength(0);
                        quoteStart = -1;
                        continue;
                    }
                    if (this.isEscapeChar(buffer, i)) continue;
                    arg.append(buffer.charAt(i));
                    continue;
                }
                if (this.isDelimiter(buffer, i)) {
                    if (arg.length() <= 0) continue;
                    args.add(arg.toString());
                    arg.setLength(0);
                    continue;
                }
                if (this.isEscapeChar(buffer, i)) continue;
                arg.append(buffer.charAt(i));
            }
            if (cursor == buffer.length()) {
                bindex = args.size();
                argpos = arg.length();
            }
            if (arg.length() > 0) {
                args.add(arg.toString());
            }
            return new ArgumentList(args.toArray(new String[args.size()]), bindex, argpos, cursor);
        }

        public boolean isDelimiter(CharSequence buffer, int pos) {
            return !this.isQuoted(buffer, pos) && !this.isEscaped(buffer, pos) && this.isDelimiterChar(buffer, pos);
        }

        public boolean isQuoted(CharSequence buffer, int pos) {
            return false;
        }

        public boolean isQuoteChar(CharSequence buffer, int pos) {
            if (pos < 0) {
                return false;
            }
            for (int i = 0; this.quoteChars != null && i < this.quoteChars.length; ++i) {
                if (buffer.charAt(pos) != this.quoteChars[i]) continue;
                return !this.isEscaped(buffer, pos);
            }
            return false;
        }

        public boolean isEscapeChar(CharSequence buffer, int pos) {
            if (pos < 0) {
                return false;
            }
            for (int i = 0; this.escapeChars != null && i < this.escapeChars.length; ++i) {
                if (buffer.charAt(pos) != this.escapeChars[i]) continue;
                return !this.isEscaped(buffer, pos);
            }
            return false;
        }

        public boolean isEscaped(CharSequence buffer, int pos) {
            if (pos <= 0) {
                return false;
            }
            return this.isEscapeChar(buffer, pos - 1);
        }

        public abstract boolean isDelimiterChar(CharSequence var1, int var2);
    }

    public static interface ArgumentDelimiter {
        public ArgumentList delimit(CharSequence var1, int var2);

        public boolean isDelimiter(CharSequence var1, int var2);
    }
}

