/*
 * Decompiled with CFR 0.152.
 */
package jline.internal;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

public class Curses {
    private static Object[] sv = new Object[26];
    private static Object[] dv = new Object[26];
    private static final int IFTE_NONE = 0;
    private static final int IFTE_IF = 1;
    private static final int IFTE_THEN = 2;
    private static final int IFTE_ELSE = 3;

    public static void tputs(Writer out, String str, Object ... params) throws IOException {
        int index = 0;
        int length = str.length();
        int ifte = 0;
        boolean exec = true;
        Stack<Object> stack = new Stack<Object>();
        block45: while (index < length) {
            char ch = str.charAt(index++);
            switch (ch) {
                case '\\': {
                    ch = str.charAt(index++);
                    if (ch >= '0' && ch <= '9') {
                        throw new UnsupportedOperationException();
                    }
                    switch (ch) {
                        case 'E': 
                        case 'e': {
                            if (!exec) continue block45;
                            out.write(27);
                            continue block45;
                        }
                        case 'n': {
                            out.write(10);
                            continue block45;
                        }
                        case 'r': {
                            if (!exec) continue block45;
                            out.write(13);
                            continue block45;
                        }
                        case 't': {
                            if (!exec) continue block45;
                            out.write(9);
                            continue block45;
                        }
                        case 'b': {
                            if (!exec) continue block45;
                            out.write(8);
                            continue block45;
                        }
                        case 'f': {
                            if (!exec) continue block45;
                            out.write(12);
                            continue block45;
                        }
                        case 's': {
                            if (!exec) continue block45;
                            out.write(32);
                            continue block45;
                        }
                        case ':': 
                        case '\\': 
                        case '^': {
                            if (!exec) continue block45;
                            out.write(ch);
                            continue block45;
                        }
                    }
                    throw new IllegalArgumentException();
                }
                case '^': {
                    ch = str.charAt(index++);
                    if (!exec) continue block45;
                    out.write(ch - 64);
                    continue block45;
                }
                case '%': {
                    ch = str.charAt(index++);
                    switch (ch) {
                        case '%': {
                            if (!exec) continue block45;
                            out.write(37);
                            continue block45;
                        }
                        case 'p': {
                            ch = str.charAt(index++);
                            if (!exec) continue block45;
                            stack.push(params[ch - 49]);
                            continue block45;
                        }
                        case 'P': {
                            ch = str.charAt(index++);
                            if (ch >= 'a' && ch <= 'z') {
                                if (!exec) continue block45;
                                Curses.dv[ch - 97] = stack.pop();
                                continue block45;
                            }
                            if (ch >= 'A' && ch <= 'Z') {
                                if (!exec) continue block45;
                                Curses.sv[ch - 65] = stack.pop();
                                continue block45;
                            }
                            throw new IllegalArgumentException();
                        }
                        case 'g': {
                            ch = str.charAt(index++);
                            if (ch >= 'a' && ch <= 'z') {
                                if (!exec) continue block45;
                                stack.push(dv[ch - 97]);
                                continue block45;
                            }
                            if (ch >= 'A' && ch <= 'Z') {
                                if (!exec) continue block45;
                                stack.push(sv[ch - 65]);
                                continue block45;
                            }
                            throw new IllegalArgumentException();
                        }
                        case '\'': {
                            ch = str.charAt(index++);
                            if (exec) {
                                stack.push(Integer.valueOf(ch));
                            }
                            if ((ch = str.charAt(index++)) == '\'') continue block45;
                            throw new IllegalArgumentException();
                        }
                        case '{': {
                            int start = index;
                            while (str.charAt(index++) != '}') {
                            }
                            if (!exec) continue block45;
                            int v = Integer.valueOf(str.substring(start, index - 1));
                            stack.push(v);
                            continue block45;
                        }
                        case 'l': {
                            if (!exec) continue block45;
                            stack.push(stack.pop().toString().length());
                            continue block45;
                        }
                        case '+': {
                            if (!exec) continue block45;
                            int v2 = Curses.toInteger(stack.pop());
                            int v1 = Curses.toInteger(stack.pop());
                            stack.push(v1 + v2);
                            continue block45;
                        }
                        case '-': {
                            if (!exec) continue block45;
                            int v2 = Curses.toInteger(stack.pop());
                            int v1 = Curses.toInteger(stack.pop());
                            stack.push(v1 - v2);
                            continue block45;
                        }
                        case '*': {
                            if (!exec) continue block45;
                            int v2 = Curses.toInteger(stack.pop());
                            int v1 = Curses.toInteger(stack.pop());
                            stack.push(v1 * v2);
                            continue block45;
                        }
                        case '/': {
                            if (!exec) continue block45;
                            int v2 = Curses.toInteger(stack.pop());
                            int v1 = Curses.toInteger(stack.pop());
                            stack.push(v1 / v2);
                            continue block45;
                        }
                        case 'm': {
                            if (!exec) continue block45;
                            int v2 = Curses.toInteger(stack.pop());
                            int v1 = Curses.toInteger(stack.pop());
                            stack.push(v1 % v2);
                            continue block45;
                        }
                        case '&': {
                            if (!exec) continue block45;
                            int v2 = Curses.toInteger(stack.pop());
                            int v1 = Curses.toInteger(stack.pop());
                            stack.push(v1 & v2);
                            continue block45;
                        }
                        case '|': {
                            if (!exec) continue block45;
                            int v2 = Curses.toInteger(stack.pop());
                            int v1 = Curses.toInteger(stack.pop());
                            stack.push(v1 | v2);
                            continue block45;
                        }
                        case '^': {
                            if (!exec) continue block45;
                            int v2 = Curses.toInteger(stack.pop());
                            int v1 = Curses.toInteger(stack.pop());
                            stack.push(v1 ^ v2);
                            continue block45;
                        }
                        case '=': {
                            if (!exec) continue block45;
                            int v2 = Curses.toInteger(stack.pop());
                            int v1 = Curses.toInteger(stack.pop());
                            stack.push(v1 == v2);
                            continue block45;
                        }
                        case '>': {
                            if (!exec) continue block45;
                            int v2 = Curses.toInteger(stack.pop());
                            int v1 = Curses.toInteger(stack.pop());
                            stack.push(v1 > v2);
                            continue block45;
                        }
                        case '<': {
                            if (!exec) continue block45;
                            int v2 = Curses.toInteger(stack.pop());
                            int v1 = Curses.toInteger(stack.pop());
                            stack.push(v1 < v2);
                            continue block45;
                        }
                        case 'A': {
                            if (!exec) continue block45;
                            int v2 = Curses.toInteger(stack.pop());
                            int v1 = Curses.toInteger(stack.pop());
                            stack.push(v1 != 0 && v2 != 0);
                            continue block45;
                        }
                        case '!': {
                            if (!exec) continue block45;
                            int v1 = Curses.toInteger(stack.pop());
                            stack.push(v1 == 0);
                            continue block45;
                        }
                        case '~': {
                            if (!exec) continue block45;
                            int v1 = Curses.toInteger(stack.pop());
                            stack.push(~v1);
                            continue block45;
                        }
                        case 'O': {
                            if (!exec) continue block45;
                            int v2 = Curses.toInteger(stack.pop());
                            int v1 = Curses.toInteger(stack.pop());
                            stack.push(v1 != 0 || v2 != 0);
                            continue block45;
                        }
                        case '?': {
                            if (ifte != 0) {
                                throw new IllegalArgumentException();
                            }
                            ifte = 1;
                            continue block45;
                        }
                        case 't': {
                            if (ifte != 1 && ifte != 3) {
                                throw new IllegalArgumentException();
                            }
                            ifte = 2;
                            exec = Curses.toInteger(stack.pop()) != 0;
                            continue block45;
                        }
                        case 'e': {
                            if (ifte != 2) {
                                throw new IllegalArgumentException();
                            }
                            ifte = 3;
                            exec = !exec;
                            continue block45;
                        }
                        case ';': {
                            if (ifte == 0 || ifte == 1) {
                                throw new IllegalArgumentException();
                            }
                            ifte = 0;
                            exec = true;
                            continue block45;
                        }
                        case 'i': {
                            if (params.length >= 1) {
                                params[0] = Curses.toInteger(params[0]) + 1;
                            }
                            if (params.length < 2) continue block45;
                            params[1] = Curses.toInteger(params[1]) + 1;
                            continue block45;
                        }
                        case 'd': {
                            out.write(Integer.toString(Curses.toInteger(stack.pop())));
                            continue block45;
                        }
                    }
                    throw new UnsupportedOperationException();
                }
            }
            if (!exec) continue;
            out.write(ch);
        }
    }

    private static int toInteger(Object pop) {
        if (pop instanceof Number) {
            return ((Number)pop).intValue();
        }
        if (pop instanceof Boolean) {
            return (Boolean)pop != false ? 1 : 0;
        }
        return Integer.valueOf(pop.toString());
    }
}

