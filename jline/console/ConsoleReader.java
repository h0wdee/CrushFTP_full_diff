/*
 * Decompiled with CFR 0.152.
 */
package jline.console;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;
import java.util.Stack;
import jline.DefaultTerminal2;
import jline.Terminal;
import jline.Terminal2;
import jline.TerminalFactory;
import jline.UnixTerminal;
import jline.console.ConsoleKeys;
import jline.console.CursorBuffer;
import jline.console.KeyMap;
import jline.console.KillRing;
import jline.console.Operation;
import jline.console.UserInterruptException;
import jline.console.WCWidth;
import jline.console.completer.CandidateListCompletionHandler;
import jline.console.completer.Completer;
import jline.console.completer.CompletionHandler;
import jline.console.history.History;
import jline.console.history.MemoryHistory;
import jline.internal.Ansi;
import jline.internal.Configuration;
import jline.internal.Curses;
import jline.internal.InputStreamReader;
import jline.internal.Log;
import jline.internal.NonBlockingInputStream;
import jline.internal.Nullable;
import jline.internal.Preconditions;
import jline.internal.Urls;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ConsoleReader
implements Closeable {
    public static final String JLINE_NOBELL = "jline.nobell";
    public static final String JLINE_ESC_TIMEOUT = "jline.esc.timeout";
    public static final String JLINE_INPUTRC = "jline.inputrc";
    public static final String INPUT_RC = ".inputrc";
    public static final String DEFAULT_INPUT_RC = "/etc/inputrc";
    public static final String JLINE_EXPAND_EVENTS = "jline.expandevents";
    public static final char BACKSPACE = '\b';
    public static final char RESET_LINE = '\r';
    public static final char KEYBOARD_BELL = '\u0007';
    public static final char NULL_MASK = '\u0000';
    public static final int TAB_WIDTH = 8;
    private static final ResourceBundle resources = ResourceBundle.getBundle(CandidateListCompletionHandler.class.getName());
    private static final int ESCAPE = 27;
    private static final int READ_EXPIRED = -2;
    private final Terminal2 terminal;
    private final Writer out;
    private final CursorBuffer buf = new CursorBuffer();
    private boolean cursorOk;
    private String prompt;
    private int promptLen;
    private boolean expandEvents = Configuration.getBoolean("jline.expandevents", true);
    private boolean bellEnabled = !Configuration.getBoolean("jline.nobell", true);
    private boolean handleUserInterrupt = false;
    private boolean handleLitteralNext = true;
    private Character mask;
    private Character echoCharacter;
    private CursorBuffer originalBuffer = null;
    private StringBuffer searchTerm = null;
    private String previousSearchTerm = "";
    private int searchIndex = -1;
    private int parenBlinkTimeout = 500;
    private final StringBuilder opBuffer = new StringBuilder();
    private final Stack<Character> pushBackChar = new Stack();
    private NonBlockingInputStream in;
    private long escapeTimeout;
    private Reader reader;
    private char charSearchChar = '\u0000';
    private char charSearchLastInvokeChar = '\u0000';
    private char charSearchFirstInvokeChar = '\u0000';
    private String yankBuffer = "";
    private KillRing killRing = new KillRing();
    private String encoding;
    private boolean quotedInsert;
    private boolean recording;
    private String macro = "";
    private String appName;
    private URL inputrcUrl;
    private ConsoleKeys consoleKeys;
    private String commentBegin = null;
    private boolean skipLF = false;
    private boolean copyPasteDetection = false;
    private State state = State.NORMAL;
    public static final String JLINE_COMPLETION_THRESHOLD = "jline.completion.threshold";
    private final List<Completer> completers = new LinkedList<Completer>();
    private CompletionHandler completionHandler = new CandidateListCompletionHandler();
    private int autoprintThreshold = Configuration.getInteger("jline.completion.threshold", 100);
    private boolean paginationEnabled;
    private History history = new MemoryHistory();
    private boolean historyEnabled = true;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private Thread maskThread;

    public ConsoleReader() throws IOException {
        this(null, new FileInputStream(FileDescriptor.in), System.out, null);
    }

    public ConsoleReader(InputStream in, OutputStream out) throws IOException {
        this(null, in, out, null);
    }

    public ConsoleReader(InputStream in, OutputStream out, Terminal term) throws IOException {
        this(null, in, out, term);
    }

    public ConsoleReader(@Nullable String appName, InputStream in, OutputStream out, @Nullable Terminal term) throws IOException {
        this(appName, in, out, term, null);
    }

    public ConsoleReader(@Nullable String appName, InputStream in, OutputStream out, @Nullable Terminal term, @Nullable String encoding) throws IOException {
        this.appName = appName != null ? appName : "JLine";
        this.encoding = encoding != null ? encoding : Configuration.getEncoding();
        Terminal terminal = term != null ? term : TerminalFactory.get();
        this.terminal = terminal instanceof Terminal2 ? (Terminal2)terminal : new DefaultTerminal2(terminal);
        String outEncoding = terminal.getOutputEncoding() != null ? terminal.getOutputEncoding() : this.encoding;
        this.out = new OutputStreamWriter(terminal.wrapOutIfNeeded(out), outEncoding);
        this.setInput(in);
        this.inputrcUrl = ConsoleReader.getInputRc();
        this.consoleKeys = new ConsoleKeys(this.appName, this.inputrcUrl);
        if (terminal instanceof UnixTerminal && "/dev/tty".equals(((UnixTerminal)terminal).getSettings().getTtyDevice()) && Configuration.getBoolean("jline.sigcont", false)) {
            this.setupSigCont();
        }
    }

    private void setupSigCont() {
        try {
            Class<?> signalClass = Class.forName("sun.misc.Signal");
            Class<?> signalHandlerClass = Class.forName("sun.misc.SignalHandler");
            Object signalHandler = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{signalHandlerClass}, new InvocationHandler(){

                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    ConsoleReader.this.terminal.init();
                    try {
                        ConsoleReader.this.drawLine();
                        ConsoleReader.this.flush();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
            signalClass.getMethod("handle", signalClass, signalHandlerClass).invoke(null, signalClass.getConstructor(String.class).newInstance("CONT"), signalHandler);
        }
        catch (ClassNotFoundException classNotFoundException) {
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static URL getInputRc() throws IOException {
        String path = Configuration.getString(JLINE_INPUTRC);
        if (path == null) {
            File f = new File(Configuration.getUserHome(), INPUT_RC);
            if (!f.exists()) {
                f = new File(DEFAULT_INPUT_RC);
            }
            return f.toURI().toURL();
        }
        return Urls.create(path);
    }

    public KeyMap getKeys() {
        return this.consoleKeys.getKeys();
    }

    void setInput(InputStream in) throws IOException {
        boolean nonBlockingEnabled;
        this.escapeTimeout = Configuration.getLong(JLINE_ESC_TIMEOUT, 100L);
        boolean bl = nonBlockingEnabled = this.escapeTimeout > 0L && this.terminal.isSupported() && in != null;
        if (this.in != null) {
            this.in.shutdown();
        }
        InputStream wrapped = this.terminal.wrapInIfNeeded(in);
        this.in = new NonBlockingInputStream(wrapped, nonBlockingEnabled);
        this.reader = new InputStreamReader((InputStream)this.in, this.encoding);
    }

    @Override
    public void close() {
        if (this.in != null) {
            this.in.shutdown();
        }
    }

    @Deprecated
    public void shutdown() {
        this.close();
    }

    protected void finalize() throws Throwable {
        try {
            this.close();
        }
        finally {
            super.finalize();
        }
    }

    public InputStream getInput() {
        return this.in;
    }

    public Writer getOutput() {
        return this.out;
    }

    public Terminal getTerminal() {
        return this.terminal;
    }

    public CursorBuffer getCursorBuffer() {
        return this.buf;
    }

    public void setExpandEvents(boolean expand) {
        this.expandEvents = expand;
    }

    public boolean getExpandEvents() {
        return this.expandEvents;
    }

    public void setCopyPasteDetection(boolean onoff) {
        this.copyPasteDetection = onoff;
    }

    public boolean isCopyPasteDetectionEnabled() {
        return this.copyPasteDetection;
    }

    public void setBellEnabled(boolean enabled) {
        this.bellEnabled = enabled;
    }

    public boolean getBellEnabled() {
        return this.bellEnabled;
    }

    public void setHandleUserInterrupt(boolean enabled) {
        this.handleUserInterrupt = enabled;
    }

    public boolean getHandleUserInterrupt() {
        return this.handleUserInterrupt;
    }

    public void setHandleLitteralNext(boolean handleLitteralNext) {
        this.handleLitteralNext = handleLitteralNext;
    }

    public boolean getHandleLitteralNext() {
        return this.handleLitteralNext;
    }

    public void setCommentBegin(String commentBegin) {
        this.commentBegin = commentBegin;
    }

    public String getCommentBegin() {
        String str = this.commentBegin;
        if (str == null && (str = this.consoleKeys.getVariable("comment-begin")) == null) {
            str = "#";
        }
        return str;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
        this.promptLen = prompt == null ? 0 : this.wcwidth(Ansi.stripAnsi(ConsoleReader.lastLine(prompt)), 0);
    }

    public String getPrompt() {
        return this.prompt;
    }

    public void setEchoCharacter(Character c) {
        this.echoCharacter = c;
    }

    public Character getEchoCharacter() {
        return this.echoCharacter;
    }

    protected final boolean resetLine() throws IOException {
        char c;
        if (this.buf.cursor == 0) {
            return false;
        }
        StringBuilder killed = new StringBuilder();
        while (this.buf.cursor > 0 && (c = this.buf.current()) != '\u0000') {
            killed.append(c);
            this.backspace();
        }
        String copy = killed.reverse().toString();
        this.killRing.addBackwards(copy);
        return true;
    }

    int wcwidth(CharSequence str, int pos) {
        return this.wcwidth(str, 0, str.length(), pos);
    }

    int wcwidth(CharSequence str, int start, int end, int pos) {
        int cur = pos;
        int i = start;
        while (i < end) {
            int ucs;
            int c1;
            if (!Character.isHighSurrogate((char)(c1 = str.charAt(i++))) || i >= end) {
                ucs = c1;
            } else {
                char c2 = str.charAt(i);
                if (Character.isLowSurrogate(c2)) {
                    ++i;
                    ucs = Character.toCodePoint((char)c1, c2);
                } else {
                    ucs = c1;
                }
            }
            cur += this.wcwidth(ucs, cur);
        }
        return cur - pos;
    }

    int wcwidth(int ucs, int pos) {
        if (ucs == 9) {
            return this.nextTabStop(pos);
        }
        if (ucs < 32) {
            return 2;
        }
        int w = WCWidth.wcwidth(ucs);
        return w > 0 ? w : 0;
    }

    int nextTabStop(int pos) {
        int tabWidth = 8;
        int mod = (pos + tabWidth - 1) % tabWidth;
        int npos = pos + tabWidth - mod;
        int width = this.getTerminal().getWidth();
        return npos < width ? npos - pos : width - pos;
    }

    int getCursorPosition() {
        return this.promptLen + this.wcwidth(this.buf.buffer, 0, this.buf.cursor, this.promptLen);
    }

    private static String lastLine(String str) {
        if (str == null) {
            return "";
        }
        int last = str.lastIndexOf("\n");
        if (last >= 0) {
            return str.substring(last + 1, str.length());
        }
        return str;
    }

    public boolean setCursorPosition(int position) throws IOException {
        if (position == this.buf.cursor) {
            return true;
        }
        return this.moveCursor(position - this.buf.cursor) != 0;
    }

    private void setBuffer(String buffer) throws IOException {
        if (buffer.equals(this.buf.buffer.toString())) {
            return;
        }
        int sameIndex = 0;
        int l1 = buffer.length();
        int l2 = this.buf.buffer.length();
        for (int i = 0; i < l1 && i < l2 && buffer.charAt(i) == this.buf.buffer.charAt(i); ++i) {
            ++sameIndex;
        }
        int diff = this.buf.cursor - sameIndex;
        if (diff < 0) {
            this.moveToEnd();
            diff = this.buf.buffer.length() - sameIndex;
        }
        this.backspace(diff);
        this.killLine();
        this.buf.buffer.setLength(sameIndex);
        this.putString(buffer.substring(sameIndex));
    }

    private void setBuffer(CharSequence buffer) throws IOException {
        this.setBuffer(String.valueOf(buffer));
    }

    private void setBufferKeepPos(String buffer) throws IOException {
        int pos = this.buf.cursor;
        this.setBuffer(buffer);
        this.setCursorPosition(pos);
    }

    private void setBufferKeepPos(CharSequence buffer) throws IOException {
        this.setBufferKeepPos(String.valueOf(buffer));
    }

    public void drawLine() throws IOException {
        String prompt = this.getPrompt();
        if (prompt != null) {
            this.rawPrint(prompt);
        }
        this.fmtPrint(this.buf.buffer, 0, this.buf.cursor, this.promptLen);
        this.drawBuffer();
    }

    public void redrawLine() throws IOException {
        this.tputs("carriage_return", new Object[0]);
        this.drawLine();
    }

    final String finishBuffer() throws IOException {
        String str;
        String historyLine = str = this.buf.buffer.toString();
        if (this.expandEvents) {
            try {
                str = this.expandEvents(str);
                historyLine = str.replace("!", "\\!");
                historyLine = historyLine.replaceAll("^\\^", "\\\\^");
            }
            catch (IllegalArgumentException e) {
                Log.error("Could not expand event", e);
                this.beep();
                this.buf.clear();
                str = "";
            }
        }
        if (str.length() > 0) {
            if (this.mask == null && this.isHistoryEnabled()) {
                this.history.add(historyLine);
            } else {
                this.mask = null;
            }
        }
        this.history.moveToEnd();
        this.buf.buffer.setLength(0);
        this.buf.cursor = 0;
        return str;
    }

    protected String expandEvents(String str) throws IOException {
        StringBuilder sb = new StringBuilder();
        block16: for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            switch (c) {
                case '\\': {
                    char nextChar;
                    if (i + 1 < str.length() && ((nextChar = str.charAt(i + 1)) == '!' || nextChar == '^' && i == 0)) {
                        c = nextChar;
                        ++i;
                    }
                    sb.append(c);
                    continue block16;
                }
                case '!': {
                    if (i + 1 < str.length()) {
                        c = str.charAt(++i);
                        boolean neg = false;
                        String rep = null;
                        switch (c) {
                            case '!': {
                                if (this.history.size() == 0) {
                                    throw new IllegalArgumentException("!!: event not found");
                                }
                                rep = this.history.get(this.history.index() - 1).toString();
                                break;
                            }
                            case '#': {
                                sb.append(sb.toString());
                                break;
                            }
                            case '?': {
                                int i1 = str.indexOf(63, i + 1);
                                if (i1 < 0) {
                                    i1 = str.length();
                                }
                                String sc = str.substring(i + 1, i1);
                                i = i1;
                                int idx = this.searchBackwards(sc);
                                if (idx < 0) {
                                    throw new IllegalArgumentException("!?" + sc + ": event not found");
                                }
                                rep = this.history.get(idx).toString();
                                break;
                            }
                            case '$': {
                                if (this.history.size() == 0) {
                                    throw new IllegalArgumentException("!$: event not found");
                                }
                                String previous = this.history.get(this.history.index() - 1).toString().trim();
                                int lastSpace = previous.lastIndexOf(32);
                                if (lastSpace != -1) {
                                    rep = previous.substring(lastSpace + 1);
                                    break;
                                }
                                rep = previous;
                                break;
                            }
                            case '\t': 
                            case ' ': {
                                sb.append('!');
                                sb.append(c);
                                break;
                            }
                            case '-': {
                                neg = true;
                            }
                            case '0': 
                            case '1': 
                            case '2': 
                            case '3': 
                            case '4': 
                            case '5': 
                            case '6': 
                            case '7': 
                            case '8': 
                            case '9': {
                                int i1 = ++i;
                                while (i < str.length() && (c = str.charAt(i)) >= '0' && c <= '9') {
                                    ++i;
                                }
                                int idx = 0;
                                try {
                                    idx = Integer.parseInt(str.substring(i1, i));
                                }
                                catch (NumberFormatException e) {
                                    throw new IllegalArgumentException((neg ? "!-" : "!") + str.substring(i1, i) + ": event not found");
                                }
                                if (neg) {
                                    if (idx > 0 && idx <= this.history.size()) {
                                        rep = this.history.get(this.history.index() - idx).toString();
                                        break;
                                    }
                                    throw new IllegalArgumentException((neg ? "!-" : "!") + str.substring(i1, i) + ": event not found");
                                }
                                if (idx > this.history.index() - this.history.size() && idx <= this.history.index()) {
                                    rep = this.history.get(idx - 1).toString();
                                    break;
                                }
                                throw new IllegalArgumentException((neg ? "!-" : "!") + str.substring(i1, i) + ": event not found");
                            }
                            default: {
                                String ss = str.substring(i);
                                i = str.length();
                                int idx = this.searchBackwards(ss, this.history.index(), true);
                                if (idx < 0) {
                                    throw new IllegalArgumentException("!" + ss + ": event not found");
                                }
                                rep = this.history.get(idx).toString();
                            }
                        }
                        if (rep == null) continue block16;
                        sb.append(rep);
                        continue block16;
                    }
                    sb.append(c);
                    continue block16;
                }
                case '^': {
                    if (i == 0) {
                        int i1 = str.indexOf(94, i + 1);
                        int i2 = str.indexOf(94, i1 + 1);
                        if (i2 < 0) {
                            i2 = str.length();
                        }
                        if (i1 > 0 && i2 > 0) {
                            String s1 = str.substring(i + 1, i1);
                            String s2 = str.substring(i1 + 1, i2);
                            String s = this.history.get(this.history.index() - 1).toString().replace(s1, s2);
                            sb.append(s);
                            i = i2 + 1;
                            continue block16;
                        }
                    }
                    sb.append(c);
                    continue block16;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        String result = sb.toString();
        if (!str.equals(result)) {
            this.fmtPrint(result, this.getCursorPosition());
            this.println();
            this.flush();
        }
        return result;
    }

    public void putString(CharSequence str) throws IOException {
        int pos = this.getCursorPosition();
        this.buf.write(str);
        if (this.mask == null) {
            this.fmtPrint(str, pos);
        } else if (this.mask.charValue() != '\u0000') {
            this.rawPrint(this.mask.charValue(), str.length());
        }
        this.drawBuffer();
    }

    private void drawBuffer(int clear) throws IOException {
        int nbChars = this.buf.length() - this.buf.cursor;
        if (this.buf.cursor != this.buf.length() || clear != 0) {
            if (this.mask != null) {
                if (this.mask.charValue() != '\u0000') {
                    this.rawPrint(this.mask.charValue(), nbChars);
                } else {
                    nbChars = 0;
                }
            } else {
                this.fmtPrint(this.buf.buffer, this.buf.cursor, this.buf.length());
            }
        }
        int cursorPos = this.promptLen + this.wcwidth(this.buf.buffer, 0, this.buf.length(), this.promptLen);
        if (this.terminal.hasWeirdWrap() && !this.cursorOk) {
            int width = this.terminal.getWidth();
            if (cursorPos > 0 && cursorPos % width == 0) {
                this.rawPrint(32);
                this.tputs("carriage_return", new Object[0]);
            }
            this.cursorOk = true;
        }
        this.clearAhead(clear, cursorPos);
        this.back(nbChars);
    }

    private void drawBuffer() throws IOException {
        this.drawBuffer(0);
    }

    private void clearAhead(int num, int pos) throws IOException {
        if (num == 0) {
            return;
        }
        int width = this.terminal.getWidth();
        if (this.terminal.getStringCapability("clr_eol") != null) {
            int cur = pos;
            int c0 = cur % width;
            int nb = Math.min(num, width - c0);
            this.tputs("clr_eol", new Object[0]);
            num -= nb;
            while (num > 0) {
                int prev = cur;
                cur = cur - cur % width + width;
                this.moveCursorFromTo(prev, cur);
                nb = Math.min(num, width);
                this.tputs("clr_eol", new Object[0]);
                num -= nb;
            }
            this.moveCursorFromTo(cur, pos);
        } else if (!this.terminal.getBooleanCapability("auto_right_margin")) {
            int cur = pos;
            int c0 = cur % width;
            int nb = Math.min(num, width - c0);
            this.rawPrint(' ', nb);
            num -= nb;
            cur += nb;
            while (num > 0) {
                this.moveCursorFromTo(cur++, cur);
                nb = Math.min(num, width);
                this.rawPrint(' ', nb);
                num -= nb;
                cur += nb;
            }
            this.moveCursorFromTo(cur, pos);
        } else {
            this.rawPrint(' ', num);
            this.moveCursorFromTo(pos + num, pos);
        }
    }

    protected void back(int num) throws IOException {
        if (num == 0) {
            return;
        }
        int i0 = this.promptLen + this.wcwidth(this.buf.buffer, 0, this.buf.cursor, this.promptLen);
        int i1 = i0 + (this.mask != null ? num : this.wcwidth(this.buf.buffer, this.buf.cursor, this.buf.cursor + num, i0));
        this.moveCursorFromTo(i1, i0);
    }

    public void flush() throws IOException {
        this.out.flush();
    }

    private int backspaceAll() throws IOException {
        return this.backspace(Integer.MAX_VALUE);
    }

    private int backspace(int num) throws IOException {
        if (this.buf.cursor == 0) {
            return 0;
        }
        int count = -this.moveCursor(-num);
        int clear = this.wcwidth(this.buf.buffer, this.buf.cursor, this.buf.cursor + count, this.getCursorPosition());
        this.buf.buffer.delete(this.buf.cursor, this.buf.cursor + count);
        this.drawBuffer(clear);
        return count;
    }

    public boolean backspace() throws IOException {
        return this.backspace(1) == 1;
    }

    protected boolean moveToEnd() throws IOException {
        if (this.buf.cursor == this.buf.length()) {
            return true;
        }
        return this.moveCursor(this.buf.length() - this.buf.cursor) > 0;
    }

    private boolean deleteCurrentCharacter() throws IOException {
        if (this.buf.length() == 0 || this.buf.cursor == this.buf.length()) {
            return false;
        }
        this.buf.buffer.deleteCharAt(this.buf.cursor);
        this.drawBuffer(1);
        return true;
    }

    private Operation viDeleteChangeYankToRemap(Operation op) {
        switch (op) {
            case VI_EOF_MAYBE: 
            case ABORT: 
            case BACKWARD_CHAR: 
            case FORWARD_CHAR: 
            case END_OF_LINE: 
            case VI_MATCH: 
            case VI_BEGINNING_OF_LINE_OR_ARG_DIGIT: 
            case VI_ARG_DIGIT: 
            case VI_PREV_WORD: 
            case VI_END_WORD: 
            case VI_CHAR_SEARCH: 
            case VI_NEXT_WORD: 
            case VI_FIRST_PRINT: 
            case VI_GOTO_MARK: 
            case VI_COLUMN: 
            case VI_DELETE_TO: 
            case VI_YANK_TO: 
            case VI_CHANGE_TO: {
                return op;
            }
        }
        return Operation.VI_MOVEMENT_MODE;
    }

    private boolean viRubout(int count) throws IOException {
        boolean ok = true;
        for (int i = 0; ok && i < count; ++i) {
            ok = this.backspace();
        }
        return ok;
    }

    private boolean viDelete(int count) throws IOException {
        boolean ok = true;
        for (int i = 0; ok && i < count; ++i) {
            ok = this.deleteCurrentCharacter();
        }
        return ok;
    }

    private boolean viChangeCase(int count) throws IOException {
        boolean ok = true;
        for (int i = 0; ok && i < count; ++i) {
            boolean bl = ok = this.buf.cursor < this.buf.buffer.length();
            if (!ok) continue;
            char ch = this.buf.buffer.charAt(this.buf.cursor);
            if (Character.isUpperCase(ch)) {
                ch = Character.toLowerCase(ch);
            } else if (Character.isLowerCase(ch)) {
                ch = Character.toUpperCase(ch);
            }
            this.buf.buffer.setCharAt(this.buf.cursor, ch);
            this.drawBuffer(1);
            this.moveCursor(1);
        }
        return ok;
    }

    private boolean viChangeChar(int count, int c) throws IOException {
        if (c < 0 || c == 27 || c == 3) {
            return true;
        }
        boolean ok = true;
        for (int i = 0; ok && i < count; ++i) {
            boolean bl = ok = this.buf.cursor < this.buf.buffer.length();
            if (!ok) continue;
            this.buf.buffer.setCharAt(this.buf.cursor, (char)c);
            this.drawBuffer(1);
            if (i >= count - 1) continue;
            this.moveCursor(1);
        }
        return ok;
    }

    private boolean viPreviousWord(int count) throws IOException {
        boolean ok = true;
        if (this.buf.cursor == 0) {
            return false;
        }
        int pos = this.buf.cursor - 1;
        for (int i = 0; pos > 0 && i < count; ++i) {
            while (pos > 0 && ConsoleReader.isWhitespace(this.buf.buffer.charAt(pos))) {
                --pos;
            }
            while (pos > 0 && !ConsoleReader.isDelimiter(this.buf.buffer.charAt(pos - 1))) {
                --pos;
            }
            if (pos <= 0 || i >= count - 1) continue;
            --pos;
        }
        this.setCursorPosition(pos);
        return ok;
    }

    private boolean viDeleteTo(int startPos, int endPos, boolean isChange) throws IOException {
        if (startPos == endPos) {
            return true;
        }
        if (endPos < startPos) {
            int tmp = endPos;
            endPos = startPos;
            startPos = tmp;
        }
        this.setCursorPosition(startPos);
        this.buf.cursor = startPos;
        this.buf.buffer.delete(startPos, endPos);
        this.drawBuffer(endPos - startPos);
        if (!isChange && startPos > 0 && startPos == this.buf.length()) {
            this.moveCursor(-1);
        }
        return true;
    }

    private boolean viYankTo(int startPos, int endPos) throws IOException {
        int cursorPos = startPos;
        if (endPos < startPos) {
            int tmp = endPos;
            endPos = startPos;
            startPos = tmp;
        }
        if (startPos == endPos) {
            this.yankBuffer = "";
            return true;
        }
        this.yankBuffer = this.buf.buffer.substring(startPos, endPos);
        this.setCursorPosition(cursorPos);
        return true;
    }

    private boolean viPut(int count) throws IOException {
        if (this.yankBuffer.length() == 0) {
            return true;
        }
        if (this.buf.cursor < this.buf.buffer.length()) {
            this.moveCursor(1);
        }
        for (int i = 0; i < count; ++i) {
            this.putString(this.yankBuffer);
        }
        this.moveCursor(-1);
        return true;
    }

    private boolean viCharSearch(int count, int invokeChar, int ch) throws IOException {
        if (ch < 0 || invokeChar < 0) {
            return false;
        }
        char searchChar = (char)ch;
        if (invokeChar == 59 || invokeChar == 44) {
            if (this.charSearchChar == '\u0000') {
                return false;
            }
            if (this.charSearchLastInvokeChar == ';' || this.charSearchLastInvokeChar == ',') {
                if (this.charSearchLastInvokeChar != invokeChar) {
                    this.charSearchFirstInvokeChar = ConsoleReader.switchCase(this.charSearchFirstInvokeChar);
                }
            } else if (invokeChar == 44) {
                this.charSearchFirstInvokeChar = ConsoleReader.switchCase(this.charSearchFirstInvokeChar);
            }
            searchChar = this.charSearchChar;
        } else {
            this.charSearchChar = searchChar;
            this.charSearchFirstInvokeChar = (char)invokeChar;
        }
        this.charSearchLastInvokeChar = (char)invokeChar;
        boolean isForward = Character.isLowerCase(this.charSearchFirstInvokeChar);
        boolean stopBefore = Character.toLowerCase(this.charSearchFirstInvokeChar) == 't';
        boolean ok = false;
        if (isForward) {
            block0: while (count-- > 0) {
                for (int pos = this.buf.cursor + 1; pos < this.buf.buffer.length(); ++pos) {
                    if (this.buf.buffer.charAt(pos) != searchChar) continue;
                    this.setCursorPosition(pos);
                    ok = true;
                    continue block0;
                }
            }
            if (ok) {
                if (stopBefore) {
                    this.moveCursor(-1);
                }
                if (this.isInViMoveOperationState()) {
                    this.moveCursor(1);
                }
            }
        } else {
            block2: while (count-- > 0) {
                for (int pos = this.buf.cursor - 1; pos >= 0; --pos) {
                    if (this.buf.buffer.charAt(pos) != searchChar) continue;
                    this.setCursorPosition(pos);
                    ok = true;
                    continue block2;
                }
            }
            if (ok && stopBefore) {
                this.moveCursor(1);
            }
        }
        return ok;
    }

    private static char switchCase(char ch) {
        if (Character.isUpperCase(ch)) {
            return Character.toLowerCase(ch);
        }
        return Character.toUpperCase(ch);
    }

    private final boolean isInViMoveOperationState() {
        return this.state == State.VI_CHANGE_TO || this.state == State.VI_DELETE_TO || this.state == State.VI_YANK_TO;
    }

    private boolean viNextWord(int count) throws IOException {
        int pos = this.buf.cursor;
        int end = this.buf.buffer.length();
        for (int i = 0; pos < end && i < count; ++i) {
            while (pos < end && !ConsoleReader.isDelimiter(this.buf.buffer.charAt(pos))) {
                ++pos;
            }
            if (i >= count - 1 && this.state == State.VI_CHANGE_TO) continue;
            while (pos < end && ConsoleReader.isDelimiter(this.buf.buffer.charAt(pos))) {
                ++pos;
            }
        }
        this.setCursorPosition(pos);
        return true;
    }

    private boolean viEndWord(int count) throws IOException {
        int pos = this.buf.cursor;
        int end = this.buf.buffer.length();
        for (int i = 0; pos < end && i < count; ++i) {
            if (pos < end - 1 && !ConsoleReader.isDelimiter(this.buf.buffer.charAt(pos)) && ConsoleReader.isDelimiter(this.buf.buffer.charAt(pos + 1))) {
                ++pos;
            }
            while (pos < end && ConsoleReader.isDelimiter(this.buf.buffer.charAt(pos))) {
                ++pos;
            }
            while (pos < end - 1 && !ConsoleReader.isDelimiter(this.buf.buffer.charAt(pos + 1))) {
                ++pos;
            }
        }
        this.setCursorPosition(pos);
        return true;
    }

    private boolean previousWord() throws IOException {
        while (ConsoleReader.isDelimiter(this.buf.current()) && this.moveCursor(-1) != 0) {
        }
        while (!ConsoleReader.isDelimiter(this.buf.current()) && this.moveCursor(-1) != 0) {
        }
        return true;
    }

    private boolean nextWord() throws IOException {
        while (ConsoleReader.isDelimiter(this.buf.nextChar()) && this.moveCursor(1) != 0) {
        }
        while (!ConsoleReader.isDelimiter(this.buf.nextChar()) && this.moveCursor(1) != 0) {
        }
        return true;
    }

    private boolean unixWordRubout(int count) throws IOException {
        boolean success = true;
        StringBuilder killed = new StringBuilder();
        while (count > 0) {
            char c;
            if (this.buf.cursor == 0) {
                success = false;
                break;
            }
            while (ConsoleReader.isWhitespace(this.buf.current()) && (c = this.buf.current()) != '\u0000') {
                killed.append(c);
                this.backspace();
            }
            while (!ConsoleReader.isWhitespace(this.buf.current()) && (c = this.buf.current()) != '\u0000') {
                killed.append(c);
                this.backspace();
            }
            --count;
        }
        String copy = killed.reverse().toString();
        this.killRing.addBackwards(copy);
        return success;
    }

    private String insertComment(boolean isViMode) throws IOException {
        String comment = this.getCommentBegin();
        this.setCursorPosition(0);
        this.putString(comment);
        if (isViMode) {
            this.consoleKeys.setKeyMap("vi-insert");
        }
        return this.accept();
    }

    private int viSearch(char searchChar) throws IOException {
        int i;
        int start;
        boolean isForward = searchChar == '/';
        CursorBuffer origBuffer = this.buf.copy();
        this.setCursorPosition(0);
        this.killLine();
        this.putString(Character.toString(searchChar));
        this.flush();
        boolean isAborted = false;
        boolean isComplete = false;
        int ch = -1;
        while (!isAborted && !isComplete && (ch = this.readCharacter()) != -1) {
            switch (ch) {
                case 27: {
                    isAborted = true;
                    break;
                }
                case 8: 
                case 127: {
                    this.backspace();
                    if (this.buf.cursor != 0) break;
                    isAborted = true;
                    break;
                }
                case 10: 
                case 13: {
                    isComplete = true;
                    break;
                }
                default: {
                    this.putString(Character.toString((char)ch));
                }
            }
            this.flush();
        }
        if (ch == -1 || isAborted) {
            this.setCursorPosition(0);
            this.killLine();
            this.putString(origBuffer.buffer);
            this.setCursorPosition(origBuffer.cursor);
            return -1;
        }
        String searchTerm = this.buf.buffer.substring(1);
        int idx = -1;
        int end = this.history.index();
        int n = start = end <= this.history.size() ? 0 : end - this.history.size();
        if (isForward) {
            for (i = start; i < end; ++i) {
                if (!this.history.get(i).toString().contains(searchTerm)) continue;
                idx = i;
                break;
            }
        } else {
            for (i = end - 1; i >= start; --i) {
                if (!this.history.get(i).toString().contains(searchTerm)) continue;
                idx = i;
                break;
            }
        }
        if (idx == -1) {
            this.setCursorPosition(0);
            this.killLine();
            this.putString(origBuffer.buffer);
            this.setCursorPosition(0);
            return -1;
        }
        this.setCursorPosition(0);
        this.killLine();
        this.putString(this.history.get(idx));
        this.setCursorPosition(0);
        this.flush();
        isComplete = false;
        while (!isComplete && (ch = this.readCharacter()) != -1) {
            boolean forward = isForward;
            switch (ch) {
                case 80: 
                case 112: {
                    forward = !isForward;
                }
                case 78: 
                case 110: {
                    int i2;
                    boolean isMatch = false;
                    if (forward) {
                        for (i2 = idx + 1; !isMatch && i2 < end; ++i2) {
                            if (!this.history.get(i2).toString().contains(searchTerm)) continue;
                            idx = i2;
                            isMatch = true;
                        }
                    } else {
                        for (i2 = idx - 1; !isMatch && i2 >= start; --i2) {
                            if (!this.history.get(i2).toString().contains(searchTerm)) continue;
                            idx = i2;
                            isMatch = true;
                        }
                    }
                    if (!isMatch) break;
                    this.setCursorPosition(0);
                    this.killLine();
                    this.putString(this.history.get(idx));
                    this.setCursorPosition(0);
                    break;
                }
                default: {
                    isComplete = true;
                }
            }
            this.flush();
        }
        return ch;
    }

    public void setParenBlinkTimeout(int timeout) {
        this.parenBlinkTimeout = timeout;
    }

    private void insertClose(String s) throws IOException {
        this.putString(s);
        int closePosition = this.buf.cursor;
        this.moveCursor(-1);
        this.viMatch();
        if (this.in.isNonBlockingEnabled()) {
            this.in.peek(this.parenBlinkTimeout);
        }
        this.setCursorPosition(closePosition);
        this.flush();
    }

    private boolean viMatch() throws IOException {
        int pos = this.buf.cursor;
        if (pos == this.buf.length()) {
            return false;
        }
        int type = ConsoleReader.getBracketType(this.buf.buffer.charAt(pos));
        int move = type < 0 ? -1 : 1;
        int count = 1;
        if (type == 0) {
            return false;
        }
        while (count > 0) {
            if ((pos += move) < 0 || pos >= this.buf.buffer.length()) {
                return false;
            }
            int curType = ConsoleReader.getBracketType(this.buf.buffer.charAt(pos));
            if (curType == type) {
                ++count;
                continue;
            }
            if (curType != -type) continue;
            --count;
        }
        if (move > 0 && this.isInViMoveOperationState()) {
            ++pos;
        }
        this.setCursorPosition(pos);
        this.flush();
        return true;
    }

    private static int getBracketType(char ch) {
        switch (ch) {
            case '[': {
                return 1;
            }
            case ']': {
                return -1;
            }
            case '{': {
                return 2;
            }
            case '}': {
                return -2;
            }
            case '(': {
                return 3;
            }
            case ')': {
                return -3;
            }
        }
        return 0;
    }

    private boolean deletePreviousWord() throws IOException {
        char c;
        StringBuilder killed = new StringBuilder();
        while (ConsoleReader.isDelimiter(c = this.buf.current()) && c != '\u0000') {
            killed.append(c);
            this.backspace();
        }
        while (!ConsoleReader.isDelimiter(c = this.buf.current()) && c != '\u0000') {
            killed.append(c);
            this.backspace();
        }
        String copy = killed.reverse().toString();
        this.killRing.addBackwards(copy);
        return true;
    }

    private boolean deleteNextWord() throws IOException {
        char c;
        StringBuilder killed = new StringBuilder();
        while (ConsoleReader.isDelimiter(c = this.buf.nextChar()) && c != '\u0000') {
            killed.append(c);
            this.delete();
        }
        while (!ConsoleReader.isDelimiter(c = this.buf.nextChar()) && c != '\u0000') {
            killed.append(c);
            this.delete();
        }
        String copy = killed.toString();
        this.killRing.add(copy);
        return true;
    }

    private boolean capitalizeWord() throws IOException {
        char c;
        boolean first = true;
        int i = 1;
        while (this.buf.cursor + i - 1 < this.buf.length() && !ConsoleReader.isDelimiter(c = this.buf.buffer.charAt(this.buf.cursor + i - 1))) {
            this.buf.buffer.setCharAt(this.buf.cursor + i - 1, first ? Character.toUpperCase(c) : Character.toLowerCase(c));
            first = false;
            ++i;
        }
        this.drawBuffer();
        this.moveCursor(i - 1);
        return true;
    }

    private boolean upCaseWord() throws IOException {
        char c;
        int i = 1;
        while (this.buf.cursor + i - 1 < this.buf.length() && !ConsoleReader.isDelimiter(c = this.buf.buffer.charAt(this.buf.cursor + i - 1))) {
            this.buf.buffer.setCharAt(this.buf.cursor + i - 1, Character.toUpperCase(c));
            ++i;
        }
        this.drawBuffer();
        this.moveCursor(i - 1);
        return true;
    }

    private boolean downCaseWord() throws IOException {
        char c;
        int i = 1;
        while (this.buf.cursor + i - 1 < this.buf.length() && !ConsoleReader.isDelimiter(c = this.buf.buffer.charAt(this.buf.cursor + i - 1))) {
            this.buf.buffer.setCharAt(this.buf.cursor + i - 1, Character.toLowerCase(c));
            ++i;
        }
        this.drawBuffer();
        this.moveCursor(i - 1);
        return true;
    }

    private boolean transposeChars(int count) throws IOException {
        while (count > 0) {
            if (this.buf.cursor == 0 || this.buf.cursor == this.buf.buffer.length()) {
                return false;
            }
            int first = this.buf.cursor - 1;
            int second = this.buf.cursor;
            char tmp = this.buf.buffer.charAt(first);
            this.buf.buffer.setCharAt(first, this.buf.buffer.charAt(second));
            this.buf.buffer.setCharAt(second, tmp);
            this.moveInternal(-1);
            this.drawBuffer();
            this.moveInternal(2);
            --count;
        }
        return true;
    }

    public boolean isKeyMap(String name) {
        KeyMap map = this.consoleKeys.getKeys();
        KeyMap mapByName = this.consoleKeys.getKeyMaps().get(name);
        if (mapByName == null) {
            return false;
        }
        return map == mapByName;
    }

    public String accept() throws IOException {
        this.moveToEnd();
        this.println();
        this.flush();
        return this.finishBuffer();
    }

    private void abort() throws IOException {
        this.beep();
        this.buf.clear();
        this.println();
        this.redrawLine();
    }

    public int moveCursor(int num) throws IOException {
        int where = num;
        if (this.buf.cursor == 0 && where <= 0) {
            return 0;
        }
        if (this.buf.cursor == this.buf.buffer.length() && where >= 0) {
            return 0;
        }
        if (this.buf.cursor + where < 0) {
            where = -this.buf.cursor;
        } else if (this.buf.cursor + where > this.buf.buffer.length()) {
            where = this.buf.buffer.length() - this.buf.cursor;
        }
        this.moveInternal(where);
        return where;
    }

    private void moveInternal(int where) throws IOException {
        int i0;
        int i1;
        this.buf.cursor += where;
        if (this.mask == null) {
            if (where < 0) {
                i1 = this.promptLen + this.wcwidth(this.buf.buffer, 0, this.buf.cursor, this.promptLen);
                i0 = i1 + this.wcwidth(this.buf.buffer, this.buf.cursor, this.buf.cursor - where, i1);
            } else {
                i0 = this.promptLen + this.wcwidth(this.buf.buffer, 0, this.buf.cursor - where, this.promptLen);
                i1 = i0 + this.wcwidth(this.buf.buffer, this.buf.cursor - where, this.buf.cursor, i0);
            }
        } else if (this.mask.charValue() != '\u0000') {
            i1 = this.promptLen + this.buf.cursor;
            i0 = i1 - where;
        } else {
            return;
        }
        this.moveCursorFromTo(i0, i1);
    }

    private void moveCursorFromTo(int i0, int i1) throws IOException {
        int i;
        if (i0 == i1) {
            return;
        }
        int width = this.getTerminal().getWidth();
        int l0 = i0 / width;
        int c0 = i0 % width;
        int l1 = i1 / width;
        int c1 = i1 % width;
        if (l0 == l1 + 1) {
            if (!this.tputs("cursor_up", new Object[0])) {
                this.tputs("parm_up_cursor", 1);
            }
        } else if (l0 > l1) {
            if (!this.tputs("parm_up_cursor", l0 - l1)) {
                for (i = l1; i < l0; ++i) {
                    this.tputs("cursor_up", new Object[0]);
                }
            }
        } else if (l0 < l1) {
            this.tputs("carriage_return", new Object[0]);
            this.rawPrint('\n', l1 - l0);
            c0 = 0;
        }
        if (c0 == c1 - 1) {
            this.tputs("cursor_right", new Object[0]);
        } else if (c0 == c1 + 1) {
            this.tputs("cursor_left", new Object[0]);
        } else if (c0 < c1) {
            if (!this.tputs("parm_right_cursor", c1 - c0)) {
                for (i = c0; i < c1; ++i) {
                    this.tputs("cursor_right", new Object[0]);
                }
            }
        } else if (c0 > c1 && !this.tputs("parm_left_cursor", c0 - c1)) {
            for (i = c1; i < c0; ++i) {
                this.tputs("cursor_left", new Object[0]);
            }
        }
        this.cursorOk = true;
    }

    public int readCharacter() throws IOException {
        return this.readCharacter(false);
    }

    public int readCharacter(boolean checkForAltKeyCombo) throws IOException {
        int c = this.reader.read();
        if (c >= 0) {
            Log.trace("Keystroke: ", c);
            if (this.terminal.isSupported()) {
                this.clearEcho(c);
            }
            if (c == 27 && checkForAltKeyCombo && this.in.peek(this.escapeTimeout) >= 32) {
                int next = this.reader.read();
                return next += 1000;
            }
        }
        return c;
    }

    private int clearEcho(int c) throws IOException {
        if (!this.terminal.isEchoEnabled()) {
            return 0;
        }
        int pos = this.getCursorPosition();
        int num = this.wcwidth(c, pos);
        this.moveCursorFromTo(pos + num, pos);
        this.drawBuffer(num);
        return num;
    }

    public int readCharacter(char ... allowed) throws IOException {
        return this.readCharacter(false, allowed);
    }

    public int readCharacter(boolean checkForAltKeyCombo, char ... allowed) throws IOException {
        char c;
        Arrays.sort(allowed);
        while (Arrays.binarySearch(allowed, c = (char)this.readCharacter(checkForAltKeyCombo)) < 0) {
        }
        return c;
    }

    public Object readBinding(KeyMap keys) throws IOException {
        Object o;
        this.opBuffer.setLength(0);
        do {
            int c;
            int n = c = this.pushBackChar.isEmpty() ? this.readCharacter() : (int)this.pushBackChar.pop().charValue();
            if (c == -1) {
                return null;
            }
            this.opBuffer.appendCodePoint(c);
            if (this.recording) {
                this.macro = this.macro + new String(Character.toChars(c));
            }
            if (this.quotedInsert) {
                o = Operation.SELF_INSERT;
                this.quotedInsert = false;
            } else {
                o = keys.getBound(this.opBuffer);
            }
            if (!this.recording && !(o instanceof KeyMap)) {
                if (o != Operation.YANK_POP && o != Operation.YANK) {
                    this.killRing.resetLastYank();
                }
                if (o != Operation.KILL_LINE && o != Operation.KILL_WHOLE_LINE && o != Operation.BACKWARD_KILL_WORD && o != Operation.KILL_WORD && o != Operation.UNIX_LINE_DISCARD && o != Operation.UNIX_WORD_RUBOUT) {
                    this.killRing.resetLastKill();
                }
            }
            if (o == Operation.DO_LOWERCASE_VERSION) {
                this.opBuffer.setLength(this.opBuffer.length() - 1);
                this.opBuffer.append(Character.toLowerCase((char)c));
                o = keys.getBound(this.opBuffer);
            }
            if (o instanceof KeyMap) {
                if (c != 27 || !this.pushBackChar.isEmpty() || !this.in.isNonBlockingEnabled() || this.in.peek(this.escapeTimeout) != -2 || (o = ((KeyMap)o).getAnotherKey()) == null || o instanceof KeyMap) continue;
                this.opBuffer.setLength(0);
            }
            while (o == null && this.opBuffer.length() > 0) {
                c = this.opBuffer.charAt(this.opBuffer.length() - 1);
                this.opBuffer.setLength(this.opBuffer.length() - 1);
                Object o2 = keys.getBound(this.opBuffer);
                if (!(o2 instanceof KeyMap) || (o = ((KeyMap)o2).getAnotherKey()) == null) continue;
                this.pushBackChar.push(Character.valueOf((char)c));
            }
        } while (o == null || o instanceof KeyMap);
        return o;
    }

    public String getLastBinding() {
        return this.opBuffer.toString();
    }

    public String readLine() throws IOException {
        return this.readLine((String)null);
    }

    public String readLine(Character mask) throws IOException {
        return this.readLine(null, mask);
    }

    public String readLine(String prompt) throws IOException {
        return this.readLine(prompt, null);
    }

    public String readLine(String prompt, Character mask) throws IOException {
        return this.readLine(prompt, mask, null);
    }

    public boolean setKeyMap(String name) {
        return this.consoleKeys.setKeyMap(name);
    }

    public String getKeyMap() {
        return this.consoleKeys.getKeys().getName();
    }

    public String readLine(String prompt, Character mask, String buffer) throws IOException {
        int repeatCount = 0;
        Character c = this.mask = mask != null ? mask : this.echoCharacter;
        if (prompt != null) {
            this.setPrompt(prompt);
        } else {
            prompt = this.getPrompt();
        }
        try {
            if (buffer != null) {
                this.buf.write(buffer);
            }
            if (!this.terminal.isSupported()) {
                this.beforeReadLine(prompt, mask);
            }
            if (buffer != null && buffer.length() > 0 || prompt != null && prompt.length() > 0) {
                this.drawLine();
                this.out.flush();
            }
            if (!this.terminal.isSupported()) {
                String string = this.readLineSimple();
                return string;
            }
            if (this.handleUserInterrupt) {
                this.terminal.disableInterruptCharacter();
            }
            if (this.handleLitteralNext && this.terminal instanceof UnixTerminal) {
                ((UnixTerminal)this.terminal).disableLitteralNextCharacter();
            }
            String originalPrompt = this.prompt;
            this.state = State.NORMAL;
            boolean success = true;
            this.pushBackChar.clear();
            while (true) {
                Object o;
                if ((o = this.readBinding(this.getKeys())) == null) {
                    String string = null;
                    return string;
                }
                int c2 = 0;
                if (this.opBuffer.length() > 0) {
                    c2 = this.opBuffer.codePointBefore(this.opBuffer.length());
                }
                Log.trace("Binding: ", o);
                if (o instanceof String) {
                    String macro = (String)o;
                    for (int i = 0; i < macro.length(); ++i) {
                        this.pushBackChar.push(Character.valueOf(macro.charAt(macro.length() - 1 - i)));
                    }
                    this.opBuffer.setLength(0);
                    continue;
                }
                if (o instanceof ActionListener) {
                    ((ActionListener)o).actionPerformed(null);
                    this.opBuffer.setLength(0);
                    continue;
                }
                CursorBuffer oldBuf = new CursorBuffer();
                oldBuf.buffer.append((CharSequence)this.buf.buffer);
                oldBuf.cursor = this.buf.cursor;
                if (this.state == State.SEARCH || this.state == State.FORWARD_SEARCH) {
                    int cursorDest = -1;
                    switch ((Operation)((Object)o)) {
                        case ABORT: {
                            this.state = State.NORMAL;
                            this.buf.clear();
                            this.buf.write(this.originalBuffer.buffer);
                            this.buf.cursor = this.originalBuffer.cursor;
                            break;
                        }
                        case REVERSE_SEARCH_HISTORY: {
                            this.state = State.SEARCH;
                            if (this.searchTerm.length() == 0) {
                                this.searchTerm.append(this.previousSearchTerm);
                            }
                            if (this.searchIndex <= 0) break;
                            this.searchIndex = this.searchBackwards(this.searchTerm.toString(), this.searchIndex);
                            break;
                        }
                        case FORWARD_SEARCH_HISTORY: {
                            this.state = State.FORWARD_SEARCH;
                            if (this.searchTerm.length() == 0) {
                                this.searchTerm.append(this.previousSearchTerm);
                            }
                            if (this.searchIndex <= -1 || this.searchIndex >= this.history.size() - 1) break;
                            this.searchIndex = this.searchForwards(this.searchTerm.toString(), this.searchIndex);
                            break;
                        }
                        case BACKWARD_DELETE_CHAR: {
                            if (this.searchTerm.length() <= 0) break;
                            this.searchTerm.deleteCharAt(this.searchTerm.length() - 1);
                            if (this.state == State.SEARCH) {
                                this.searchIndex = this.searchBackwards(this.searchTerm.toString());
                                break;
                            }
                            this.searchIndex = this.searchForwards(this.searchTerm.toString());
                            break;
                        }
                        case SELF_INSERT: {
                            this.searchTerm.appendCodePoint(c2);
                            if (this.state == State.SEARCH) {
                                this.searchIndex = this.searchBackwards(this.searchTerm.toString());
                                break;
                            }
                            this.searchIndex = this.searchForwards(this.searchTerm.toString());
                            break;
                        }
                        default: {
                            if (this.searchIndex != -1) {
                                this.history.moveTo(this.searchIndex);
                                cursorDest = this.history.current().toString().indexOf(this.searchTerm.toString());
                            }
                            if (o != Operation.ACCEPT_LINE) {
                                o = null;
                            }
                            this.state = State.NORMAL;
                        }
                    }
                    if (this.state == State.SEARCH || this.state == State.FORWARD_SEARCH) {
                        if (this.searchTerm.length() == 0) {
                            if (this.state == State.SEARCH) {
                                this.printSearchStatus("", "");
                            } else {
                                this.printForwardSearchStatus("", "");
                            }
                            this.searchIndex = -1;
                        } else if (this.searchIndex == -1) {
                            this.beep();
                            this.printSearchStatus(this.searchTerm.toString(), "");
                        } else if (this.state == State.SEARCH) {
                            this.printSearchStatus(this.searchTerm.toString(), this.history.get(this.searchIndex).toString());
                        } else {
                            this.printForwardSearchStatus(this.searchTerm.toString(), this.history.get(this.searchIndex).toString());
                        }
                    } else {
                        this.restoreLine(originalPrompt, cursorDest);
                    }
                }
                if (this.state != State.SEARCH && this.state != State.FORWARD_SEARCH) {
                    boolean isArgDigit = false;
                    int count = repeatCount == 0 ? 1 : repeatCount;
                    success = true;
                    if (o instanceof Operation) {
                        Operation op = (Operation)((Object)o);
                        int cursorStart = this.buf.cursor;
                        State origState = this.state;
                        if (this.state == State.VI_CHANGE_TO || this.state == State.VI_YANK_TO || this.state == State.VI_DELETE_TO) {
                            op = this.viDeleteChangeYankToRemap(op);
                        }
                        switch (op) {
                            case COMPLETE: {
                                boolean isTabLiteral = false;
                                if (this.copyPasteDetection && c2 == 9 && (!this.pushBackChar.isEmpty() || this.in.isNonBlockingEnabled() && this.in.peek(this.escapeTimeout) != -2)) {
                                    isTabLiteral = true;
                                }
                                if (!isTabLiteral) {
                                    success = this.complete();
                                    break;
                                }
                                this.putString(this.opBuffer);
                                break;
                            }
                            case POSSIBLE_COMPLETIONS: {
                                this.printCompletionCandidates();
                                break;
                            }
                            case BEGINNING_OF_LINE: {
                                success = this.setCursorPosition(0);
                                break;
                            }
                            case YANK: {
                                success = this.yank();
                                break;
                            }
                            case YANK_POP: {
                                success = this.yankPop();
                                break;
                            }
                            case KILL_LINE: {
                                success = this.killLine();
                                break;
                            }
                            case KILL_WHOLE_LINE: {
                                success = this.setCursorPosition(0) && this.killLine();
                                break;
                            }
                            case CLEAR_SCREEN: {
                                success = this.clearScreen();
                                this.redrawLine();
                                break;
                            }
                            case OVERWRITE_MODE: {
                                this.buf.setOverTyping(!this.buf.isOverTyping());
                                break;
                            }
                            case SELF_INSERT: {
                                this.putString(this.opBuffer);
                                break;
                            }
                            case ACCEPT_LINE: {
                                String string = this.accept();
                                return string;
                            }
                            case ABORT: {
                                if (this.searchTerm != null) break;
                                this.abort();
                                break;
                            }
                            case INTERRUPT: {
                                if (!this.handleUserInterrupt) break;
                                this.println();
                                this.flush();
                                String partialLine = this.buf.buffer.toString();
                                this.buf.clear();
                                this.history.moveToEnd();
                                throw new UserInterruptException(partialLine);
                            }
                            case VI_MOVE_ACCEPT_LINE: {
                                this.consoleKeys.setKeyMap("vi-insert");
                                String partialLine = this.accept();
                                return partialLine;
                            }
                            case BACKWARD_WORD: {
                                success = this.previousWord();
                                break;
                            }
                            case FORWARD_WORD: {
                                success = this.nextWord();
                                break;
                            }
                            case PREVIOUS_HISTORY: {
                                success = this.moveHistory(false);
                                break;
                            }
                            case VI_PREVIOUS_HISTORY: {
                                success = this.moveHistory(false, count) && this.setCursorPosition(0);
                                break;
                            }
                            case NEXT_HISTORY: {
                                success = this.moveHistory(true);
                                break;
                            }
                            case VI_NEXT_HISTORY: {
                                success = this.moveHistory(true, count) && this.setCursorPosition(0);
                                break;
                            }
                            case BACKWARD_DELETE_CHAR: {
                                success = this.backspace();
                                break;
                            }
                            case EXIT_OR_DELETE_CHAR: {
                                if (this.buf.buffer.length() == 0) {
                                    String partialLine = null;
                                    return partialLine;
                                }
                                success = this.deleteCurrentCharacter();
                                break;
                            }
                            case DELETE_CHAR: {
                                success = this.deleteCurrentCharacter();
                                break;
                            }
                            case BACKWARD_CHAR: {
                                success = this.moveCursor(-count) != 0;
                                break;
                            }
                            case FORWARD_CHAR: {
                                success = this.moveCursor(count) != 0;
                                break;
                            }
                            case UNIX_LINE_DISCARD: {
                                success = this.resetLine();
                                break;
                            }
                            case UNIX_WORD_RUBOUT: {
                                success = this.unixWordRubout(count);
                                break;
                            }
                            case BACKWARD_KILL_WORD: {
                                success = this.deletePreviousWord();
                                break;
                            }
                            case KILL_WORD: {
                                success = this.deleteNextWord();
                                break;
                            }
                            case BEGINNING_OF_HISTORY: {
                                success = this.history.moveToFirst();
                                if (!success) break;
                                this.setBuffer(this.history.current());
                                break;
                            }
                            case END_OF_HISTORY: {
                                success = this.history.moveToLast();
                                if (!success) break;
                                this.setBuffer(this.history.current());
                                break;
                            }
                            case HISTORY_SEARCH_BACKWARD: {
                                this.searchTerm = new StringBuffer(this.buf.upToCursor());
                                this.searchIndex = this.searchBackwards(this.searchTerm.toString(), this.history.index(), true);
                                if (this.searchIndex == -1) {
                                    this.beep();
                                    break;
                                }
                                success = this.history.moveTo(this.searchIndex);
                                if (!success) break;
                                this.setBufferKeepPos(this.history.current());
                                break;
                            }
                            case HISTORY_SEARCH_FORWARD: {
                                this.searchTerm = new StringBuffer(this.buf.upToCursor());
                                int index = this.history.index() + 1;
                                if (index == this.history.size()) {
                                    this.history.moveToEnd();
                                    this.setBufferKeepPos(this.searchTerm.toString());
                                    break;
                                }
                                if (index >= this.history.size()) break;
                                this.searchIndex = this.searchForwards(this.searchTerm.toString(), index, true);
                                if (this.searchIndex == -1) {
                                    this.beep();
                                    break;
                                }
                                success = this.history.moveTo(this.searchIndex);
                                if (!success) break;
                                this.setBufferKeepPos(this.history.current());
                                break;
                            }
                            case REVERSE_SEARCH_HISTORY: {
                                this.originalBuffer = new CursorBuffer();
                                this.originalBuffer.write(this.buf.buffer);
                                this.originalBuffer.cursor = this.buf.cursor;
                                if (this.searchTerm != null) {
                                    this.previousSearchTerm = this.searchTerm.toString();
                                }
                                this.searchTerm = new StringBuffer(this.buf.buffer);
                                this.state = State.SEARCH;
                                if (this.searchTerm.length() > 0) {
                                    this.searchIndex = this.searchBackwards(this.searchTerm.toString());
                                    if (this.searchIndex == -1) {
                                        this.beep();
                                    }
                                    this.printSearchStatus(this.searchTerm.toString(), this.searchIndex > -1 ? this.history.get(this.searchIndex).toString() : "");
                                    break;
                                }
                                this.searchIndex = -1;
                                this.printSearchStatus("", "");
                                break;
                            }
                            case FORWARD_SEARCH_HISTORY: {
                                this.originalBuffer = new CursorBuffer();
                                this.originalBuffer.write(this.buf.buffer);
                                this.originalBuffer.cursor = this.buf.cursor;
                                if (this.searchTerm != null) {
                                    this.previousSearchTerm = this.searchTerm.toString();
                                }
                                this.searchTerm = new StringBuffer(this.buf.buffer);
                                this.state = State.FORWARD_SEARCH;
                                if (this.searchTerm.length() > 0) {
                                    this.searchIndex = this.searchForwards(this.searchTerm.toString());
                                    if (this.searchIndex == -1) {
                                        this.beep();
                                    }
                                    this.printForwardSearchStatus(this.searchTerm.toString(), this.searchIndex > -1 ? this.history.get(this.searchIndex).toString() : "");
                                    break;
                                }
                                this.searchIndex = -1;
                                this.printForwardSearchStatus("", "");
                                break;
                            }
                            case CAPITALIZE_WORD: {
                                success = this.capitalizeWord();
                                break;
                            }
                            case UPCASE_WORD: {
                                success = this.upCaseWord();
                                break;
                            }
                            case DOWNCASE_WORD: {
                                success = this.downCaseWord();
                                break;
                            }
                            case END_OF_LINE: {
                                success = this.moveToEnd();
                                break;
                            }
                            case TAB_INSERT: {
                                this.putString("\t");
                                break;
                            }
                            case RE_READ_INIT_FILE: {
                                this.consoleKeys.loadKeys(this.appName, this.inputrcUrl);
                                break;
                            }
                            case START_KBD_MACRO: {
                                this.recording = true;
                                break;
                            }
                            case END_KBD_MACRO: {
                                this.recording = false;
                                this.macro = this.macro.substring(0, this.macro.length() - this.opBuffer.length());
                                break;
                            }
                            case CALL_LAST_KBD_MACRO: {
                                for (int i = 0; i < this.macro.length(); ++i) {
                                    this.pushBackChar.push(Character.valueOf(this.macro.charAt(this.macro.length() - 1 - i)));
                                }
                                this.opBuffer.setLength(0);
                                break;
                            }
                            case VI_EDITING_MODE: {
                                this.consoleKeys.setKeyMap("vi-insert");
                                break;
                            }
                            case VI_MOVEMENT_MODE: {
                                if (this.state == State.NORMAL) {
                                    this.moveCursor(-1);
                                }
                                this.consoleKeys.setKeyMap("vi-move");
                                break;
                            }
                            case VI_INSERTION_MODE: {
                                this.consoleKeys.setKeyMap("vi-insert");
                                break;
                            }
                            case VI_APPEND_MODE: {
                                this.moveCursor(1);
                                this.consoleKeys.setKeyMap("vi-insert");
                                break;
                            }
                            case VI_APPEND_EOL: {
                                success = this.moveToEnd();
                                this.consoleKeys.setKeyMap("vi-insert");
                                break;
                            }
                            case VI_EOF_MAYBE: {
                                if (this.buf.buffer.length() == 0) {
                                    String i = null;
                                    return i;
                                }
                                String i = this.accept();
                                return i;
                            }
                            case TRANSPOSE_CHARS: {
                                success = this.transposeChars(count);
                                break;
                            }
                            case INSERT_COMMENT: {
                                String i = this.insertComment(false);
                                return i;
                            }
                            case INSERT_CLOSE_CURLY: {
                                this.insertClose("}");
                                break;
                            }
                            case INSERT_CLOSE_PAREN: {
                                this.insertClose(")");
                                break;
                            }
                            case INSERT_CLOSE_SQUARE: {
                                this.insertClose("]");
                                break;
                            }
                            case VI_INSERT_COMMENT: {
                                String i = this.insertComment(true);
                                return i;
                            }
                            case VI_MATCH: {
                                success = this.viMatch();
                                break;
                            }
                            case VI_SEARCH: {
                                int lastChar = this.viSearch(this.opBuffer.charAt(0));
                                if (lastChar == -1) break;
                                this.pushBackChar.push(Character.valueOf((char)lastChar));
                                break;
                            }
                            case VI_ARG_DIGIT: {
                                repeatCount = repeatCount * 10 + this.opBuffer.charAt(0) - 48;
                                isArgDigit = true;
                                break;
                            }
                            case VI_BEGINNING_OF_LINE_OR_ARG_DIGIT: {
                                if (repeatCount > 0) {
                                    repeatCount = repeatCount * 10 + this.opBuffer.charAt(0) - 48;
                                    isArgDigit = true;
                                    break;
                                }
                                success = this.setCursorPosition(0);
                                break;
                            }
                            case VI_FIRST_PRINT: {
                                success = this.setCursorPosition(0) && this.viNextWord(1);
                                break;
                            }
                            case VI_PREV_WORD: {
                                success = this.viPreviousWord(count);
                                break;
                            }
                            case VI_NEXT_WORD: {
                                success = this.viNextWord(count);
                                break;
                            }
                            case VI_END_WORD: {
                                success = this.viEndWord(count);
                                break;
                            }
                            case VI_INSERT_BEG: {
                                success = this.setCursorPosition(0);
                                this.consoleKeys.setKeyMap("vi-insert");
                                break;
                            }
                            case VI_RUBOUT: {
                                success = this.viRubout(count);
                                break;
                            }
                            case VI_DELETE: {
                                success = this.viDelete(count);
                                break;
                            }
                            case VI_DELETE_TO: {
                                if (this.state == State.VI_DELETE_TO) {
                                    success = this.setCursorPosition(0) && this.killLine();
                                    this.state = origState = State.NORMAL;
                                    break;
                                }
                                this.state = State.VI_DELETE_TO;
                                break;
                            }
                            case VI_YANK_TO: {
                                if (this.state == State.VI_YANK_TO) {
                                    this.yankBuffer = this.buf.buffer.toString();
                                    this.state = origState = State.NORMAL;
                                    break;
                                }
                                this.state = State.VI_YANK_TO;
                                break;
                            }
                            case VI_CHANGE_TO: {
                                if (this.state == State.VI_CHANGE_TO) {
                                    success = this.setCursorPosition(0) && this.killLine();
                                    this.state = origState = State.NORMAL;
                                    this.consoleKeys.setKeyMap("vi-insert");
                                    break;
                                }
                                this.state = State.VI_CHANGE_TO;
                                break;
                            }
                            case VI_KILL_WHOLE_LINE: {
                                success = this.setCursorPosition(0) && this.killLine();
                                this.consoleKeys.setKeyMap("vi-insert");
                                break;
                            }
                            case VI_PUT: {
                                success = this.viPut(count);
                                break;
                            }
                            case VI_CHAR_SEARCH: {
                                int searchChar = c2 != 59 && c2 != 44 ? (this.pushBackChar.isEmpty() ? this.readCharacter() : (int)this.pushBackChar.pop().charValue()) : 0;
                                success = this.viCharSearch(count, c2, searchChar);
                                break;
                            }
                            case VI_CHANGE_CASE: {
                                success = this.viChangeCase(count);
                                break;
                            }
                            case VI_CHANGE_CHAR: {
                                success = this.viChangeChar(count, this.pushBackChar.isEmpty() ? this.readCharacter() : (int)this.pushBackChar.pop().charValue());
                                break;
                            }
                            case VI_DELETE_TO_EOL: {
                                success = this.viDeleteTo(this.buf.cursor, this.buf.buffer.length(), false);
                                break;
                            }
                            case VI_CHANGE_TO_EOL: {
                                success = this.viDeleteTo(this.buf.cursor, this.buf.buffer.length(), true);
                                this.consoleKeys.setKeyMap("vi-insert");
                                break;
                            }
                            case EMACS_EDITING_MODE: {
                                this.consoleKeys.setKeyMap("emacs");
                                break;
                            }
                            case QUIT: {
                                this.getCursorBuffer().clear();
                                String string = this.accept();
                                return string;
                            }
                            case QUOTED_INSERT: {
                                this.quotedInsert = true;
                                break;
                            }
                            case PASTE_FROM_CLIPBOARD: {
                                this.paste();
                                break;
                            }
                        }
                        if (origState != State.NORMAL) {
                            if (origState == State.VI_DELETE_TO) {
                                success = this.viDeleteTo(cursorStart, this.buf.cursor, false);
                            } else if (origState == State.VI_CHANGE_TO) {
                                success = this.viDeleteTo(cursorStart, this.buf.cursor, true);
                                this.consoleKeys.setKeyMap("vi-insert");
                            } else if (origState == State.VI_YANK_TO) {
                                success = this.viYankTo(cursorStart, this.buf.cursor);
                            }
                            this.state = State.NORMAL;
                        }
                        if (this.state == State.NORMAL && !isArgDigit) {
                            repeatCount = 0;
                        }
                        if (this.state != State.SEARCH && this.state != State.FORWARD_SEARCH) {
                            this.originalBuffer = null;
                            this.previousSearchTerm = "";
                            this.searchTerm = null;
                            this.searchIndex = -1;
                        }
                    }
                }
                if (!success) {
                    this.beep();
                }
                this.opBuffer.setLength(0);
                this.flush();
            }
        }
        finally {
            if (!this.terminal.isSupported()) {
                this.afterReadLine();
            }
            if (this.handleUserInterrupt) {
                this.terminal.enableInterruptCharacter();
            }
        }
    }

    private String readLineSimple() throws IOException {
        int i;
        StringBuilder buff = new StringBuilder();
        if (this.skipLF) {
            this.skipLF = false;
            i = this.readCharacter();
            if (i == -1 || i == 13) {
                return buff.toString();
            }
            if (i != 10) {
                buff.append((char)i);
            }
        }
        while ((i = this.readCharacter()) != -1 || buff.length() != 0) {
            if (i == -1 || i == 10) {
                return buff.toString();
            }
            if (i == 13) {
                this.skipLF = true;
                return buff.toString();
            }
            buff.append((char)i);
        }
        return null;
    }

    public boolean addCompleter(Completer completer) {
        return this.completers.add(completer);
    }

    public boolean removeCompleter(Completer completer) {
        return this.completers.remove(completer);
    }

    public Collection<Completer> getCompleters() {
        return Collections.unmodifiableList(this.completers);
    }

    public void setCompletionHandler(CompletionHandler handler) {
        this.completionHandler = Preconditions.checkNotNull(handler);
    }

    public CompletionHandler getCompletionHandler() {
        return this.completionHandler;
    }

    protected boolean complete() throws IOException {
        Completer comp;
        if (this.completers.size() == 0) {
            return false;
        }
        LinkedList<CharSequence> candidates = new LinkedList<CharSequence>();
        String bufstr = this.buf.buffer.toString();
        int cursor = this.buf.cursor;
        int position = -1;
        Iterator<Completer> iterator = this.completers.iterator();
        while (iterator.hasNext() && (position = (comp = iterator.next()).complete(bufstr, cursor, candidates)) == -1) {
        }
        return candidates.size() != 0 && this.getCompletionHandler().complete(this, candidates, position);
    }

    protected void printCompletionCandidates() throws IOException {
        if (this.completers.size() == 0) {
            return;
        }
        LinkedList<CharSequence> candidates = new LinkedList<CharSequence>();
        String bufstr = this.buf.buffer.toString();
        int cursor = this.buf.cursor;
        for (Completer comp : this.completers) {
            if (comp.complete(bufstr, cursor, candidates) != -1) break;
        }
        CandidateListCompletionHandler.printCandidates(this, candidates);
        this.drawLine();
    }

    public void setAutoprintThreshold(int threshold) {
        this.autoprintThreshold = threshold;
    }

    public int getAutoprintThreshold() {
        return this.autoprintThreshold;
    }

    public void setPaginationEnabled(boolean enabled) {
        this.paginationEnabled = enabled;
    }

    public boolean isPaginationEnabled() {
        return this.paginationEnabled;
    }

    public void setHistory(History history) {
        this.history = history;
    }

    public History getHistory() {
        return this.history;
    }

    public void setHistoryEnabled(boolean enabled) {
        this.historyEnabled = enabled;
    }

    public boolean isHistoryEnabled() {
        return this.historyEnabled;
    }

    private boolean moveHistory(boolean next, int count) throws IOException {
        boolean ok = true;
        for (int i = 0; i < count && (ok = this.moveHistory(next)); ++i) {
        }
        return ok;
    }

    private boolean moveHistory(boolean next) throws IOException {
        if (next && !this.history.next()) {
            return false;
        }
        if (!next && !this.history.previous()) {
            return false;
        }
        this.setBuffer(this.history.current());
        return true;
    }

    private int fmtPrint(CharSequence buff, int cursorPos) throws IOException {
        return this.fmtPrint(buff, 0, buff.length(), cursorPos);
    }

    private int fmtPrint(CharSequence buff, int start, int end) throws IOException {
        return this.fmtPrint(buff, start, end, this.getCursorPosition());
    }

    private int fmtPrint(CharSequence buff, int start, int end, int cursorPos) throws IOException {
        Preconditions.checkNotNull(buff);
        for (int i = start; i < end; ++i) {
            char c = buff.charAt(i);
            if (c == '\t') {
                int nb = this.nextTabStop(cursorPos);
                cursorPos += nb;
                while (nb-- > 0) {
                    this.out.write(32);
                }
                continue;
            }
            if (c < ' ') {
                this.out.write(94);
                this.out.write((char)(c + 64));
                cursorPos += 2;
                continue;
            }
            int w = WCWidth.wcwidth(c);
            if (w <= 0) continue;
            this.out.write(c);
            cursorPos += w;
        }
        this.cursorOk = false;
        return cursorPos;
    }

    public void print(CharSequence s) throws IOException {
        this.rawPrint(s.toString());
    }

    public void println(CharSequence s) throws IOException {
        this.print(s);
        this.println();
    }

    public void println() throws IOException {
        this.rawPrint(LINE_SEPARATOR);
    }

    final void rawPrint(int c) throws IOException {
        this.out.write(c);
        this.cursorOk = false;
    }

    final void rawPrint(String str) throws IOException {
        this.out.write(str);
        this.cursorOk = false;
    }

    private void rawPrint(char c, int num) throws IOException {
        for (int i = 0; i < num; ++i) {
            this.rawPrint(c);
        }
    }

    private void rawPrintln(String s) throws IOException {
        this.rawPrint(s);
        this.println();
    }

    public boolean delete() throws IOException {
        if (this.buf.cursor == this.buf.buffer.length()) {
            return false;
        }
        this.buf.buffer.delete(this.buf.cursor, this.buf.cursor + 1);
        this.drawBuffer(1);
        return true;
    }

    public boolean killLine() throws IOException {
        int cp = this.buf.cursor;
        int len = this.buf.buffer.length();
        if (cp >= len) {
            return false;
        }
        int num = len - cp;
        int pos = this.getCursorPosition();
        int width = this.wcwidth(this.buf.buffer, cp, len, pos);
        this.clearAhead(width, pos);
        char[] killed = new char[num];
        this.buf.buffer.getChars(cp, cp + num, killed, 0);
        this.buf.buffer.delete(cp, cp + num);
        String copy = new String(killed);
        this.killRing.add(copy);
        return true;
    }

    public boolean yank() throws IOException {
        String yanked = this.killRing.yank();
        if (yanked == null) {
            return false;
        }
        this.putString(yanked);
        return true;
    }

    public boolean yankPop() throws IOException {
        if (!this.killRing.lastYank()) {
            return false;
        }
        String current = this.killRing.yank();
        if (current == null) {
            return false;
        }
        this.backspace(current.length());
        String yanked = this.killRing.yankPop();
        if (yanked == null) {
            return false;
        }
        this.putString(yanked);
        return true;
    }

    public boolean clearScreen() throws IOException {
        if (!this.tputs("clear_screen", new Object[0])) {
            this.println();
        }
        return true;
    }

    public void beep() throws IOException {
        if (this.bellEnabled && this.tputs("bell", new Object[0])) {
            this.flush();
        }
    }

    public boolean paste() throws IOException {
        Clipboard clipboard;
        try {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        catch (Exception e) {
            return false;
        }
        if (clipboard == null) {
            return false;
        }
        Transferable transferable = clipboard.getContents(null);
        if (transferable == null) {
            return false;
        }
        try {
            String value;
            Object content = transferable.getTransferData(DataFlavor.plainTextFlavor);
            if (content == null) {
                try {
                    content = new DataFlavor().getReaderForText(transferable);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (content == null) {
                return false;
            }
            if (content instanceof Reader) {
                String line;
                value = "";
                BufferedReader read = new BufferedReader((Reader)content);
                while ((line = read.readLine()) != null) {
                    if (value.length() > 0) {
                        value = value + "\n";
                    }
                    value = value + line;
                }
            } else {
                value = content.toString();
            }
            if (value == null) {
                return true;
            }
            this.putString(value);
            return true;
        }
        catch (UnsupportedFlavorException e) {
            Log.error("Paste failed: ", e);
            return false;
        }
    }

    public void addTriggeredAction(char c, ActionListener listener) {
        this.getKeys().bind(Character.toString(c), listener);
    }

    public void printColumns(Collection<? extends CharSequence> items) throws IOException {
        if (items == null || items.isEmpty()) {
            return;
        }
        int width = this.getTerminal().getWidth();
        int height = this.getTerminal().getHeight();
        int maxWidth = 0;
        for (CharSequence charSequence : items) {
            int len = this.wcwidth(Ansi.stripAnsi(charSequence.toString()), 0);
            maxWidth = Math.max(maxWidth, len);
        }
        Log.debug("Max width: ", maxWidth += 3);
        int showLines = this.isPaginationEnabled() ? height - 1 : Integer.MAX_VALUE;
        StringBuilder stringBuilder = new StringBuilder();
        int realLength = 0;
        for (CharSequence charSequence : items) {
            if (realLength + maxWidth > width) {
                this.rawPrintln(stringBuilder.toString());
                stringBuilder.setLength(0);
                realLength = 0;
                if (--showLines == 0) {
                    this.print(resources.getString("DISPLAY_MORE"));
                    this.flush();
                    int c = this.readCharacter();
                    if (c == 13 || c == 10) {
                        showLines = 1;
                    } else if (c != 113) {
                        showLines = height - 1;
                    }
                    this.tputs("carriage_return", new Object[0]);
                    if (c == 113) break;
                }
            }
            stringBuilder.append(charSequence.toString());
            int strippedItemLength = this.wcwidth(Ansi.stripAnsi(charSequence.toString()), 0);
            for (int i = 0; i < maxWidth - strippedItemLength; ++i) {
                stringBuilder.append(' ');
            }
            realLength += maxWidth;
        }
        if (stringBuilder.length() > 0) {
            this.rawPrintln(stringBuilder.toString());
        }
    }

    private void beforeReadLine(String prompt, Character mask) {
        if (mask != null && this.maskThread == null) {
            final String fullPrompt = "\r" + prompt + "                 " + "                 " + "                 " + "\r" + prompt;
            this.maskThread = new Thread(){

                public void run() {
                    while (!2.interrupted()) {
                        try {
                            Writer out = ConsoleReader.this.getOutput();
                            out.write(fullPrompt);
                            out.flush();
                            2.sleep(3L);
                        }
                        catch (IOException e) {
                            return;
                        }
                        catch (InterruptedException e) {
                            return;
                        }
                    }
                }
            };
            this.maskThread.setPriority(10);
            this.maskThread.setDaemon(true);
            this.maskThread.start();
        }
    }

    private void afterReadLine() {
        if (this.maskThread != null && this.maskThread.isAlive()) {
            this.maskThread.interrupt();
        }
        this.maskThread = null;
    }

    public void resetPromptLine(String prompt, String buffer, int cursorDest) throws IOException {
        this.moveToEnd();
        this.buf.buffer.append(this.prompt);
        int promptLength = 0;
        if (this.prompt != null) {
            promptLength = this.prompt.length();
        }
        this.buf.cursor += promptLength;
        this.setPrompt("");
        this.backspaceAll();
        this.setPrompt(prompt);
        this.redrawLine();
        this.setBuffer(buffer);
        if (cursorDest < 0) {
            cursorDest = buffer.length();
        }
        this.setCursorPosition(cursorDest);
        this.flush();
    }

    public void printSearchStatus(String searchTerm, String match) throws IOException {
        this.printSearchStatus(searchTerm, match, "(reverse-i-search)`");
    }

    public void printForwardSearchStatus(String searchTerm, String match) throws IOException {
        this.printSearchStatus(searchTerm, match, "(i-search)`");
    }

    private void printSearchStatus(String searchTerm, String match, String searchLabel) throws IOException {
        String prompt = searchLabel + searchTerm + "': ";
        int cursorDest = match.indexOf(searchTerm);
        this.resetPromptLine(prompt, match, cursorDest);
    }

    public void restoreLine(String originalPrompt, int cursorDest) throws IOException {
        String prompt = ConsoleReader.lastLine(originalPrompt);
        String buffer = this.buf.buffer.toString();
        this.resetPromptLine(prompt, buffer, cursorDest);
    }

    public int searchBackwards(String searchTerm, int startIndex) {
        return this.searchBackwards(searchTerm, startIndex, false);
    }

    public int searchBackwards(String searchTerm) {
        return this.searchBackwards(searchTerm, this.history.index());
    }

    public int searchBackwards(String searchTerm, int startIndex, boolean startsWith) {
        ListIterator<History.Entry> it = this.history.entries(startIndex);
        while (it.hasPrevious()) {
            History.Entry e = it.previous();
            if (!(startsWith ? e.value().toString().startsWith(searchTerm) : e.value().toString().contains(searchTerm))) continue;
            return e.index();
        }
        return -1;
    }

    public int searchForwards(String searchTerm, int startIndex) {
        return this.searchForwards(searchTerm, startIndex, false);
    }

    public int searchForwards(String searchTerm) {
        return this.searchForwards(searchTerm, this.history.index());
    }

    public int searchForwards(String searchTerm, int startIndex, boolean startsWith) {
        if (startIndex >= this.history.size()) {
            startIndex = this.history.size() - 1;
        }
        ListIterator<History.Entry> it = this.history.entries(startIndex);
        if (this.searchIndex != -1 && it.hasNext()) {
            it.next();
        }
        while (it.hasNext()) {
            History.Entry e = it.next();
            if (!(startsWith ? e.value().toString().startsWith(searchTerm) : e.value().toString().contains(searchTerm))) continue;
            return e.index();
        }
        return -1;
    }

    private static boolean isDelimiter(char c) {
        return !Character.isLetterOrDigit(c);
    }

    private static boolean isWhitespace(char c) {
        return Character.isWhitespace(c);
    }

    private boolean tputs(String cap, Object ... params) throws IOException {
        String str = this.terminal.getStringCapability(cap);
        if (str == null) {
            return false;
        }
        Curses.tputs(this.out, str, params);
        return true;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    private static enum State {
        NORMAL,
        SEARCH,
        FORWARD_SEARCH,
        VI_YANK_TO,
        VI_DELETE_TO,
        VI_CHANGE_TO;

    }
}

