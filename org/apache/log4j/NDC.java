/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.ThreadContext
 */
package org.apache.log4j;

import java.util.Stack;
import org.apache.logging.log4j.ThreadContext;

public final class NDC {
    private NDC() {
    }

    public static void clear() {
        ThreadContext.clearStack();
    }

    public static Stack cloneStack() {
        Stack<String> stack = new Stack<String>();
        for (String element : ThreadContext.cloneStack().asList()) {
            stack.push(element);
        }
        return stack;
    }

    public static void inherit(Stack<String> stack) {
        ThreadContext.setStack(stack);
    }

    public static String get() {
        return ThreadContext.peek();
    }

    public static int getDepth() {
        return ThreadContext.getDepth();
    }

    public static String pop() {
        return ThreadContext.pop();
    }

    public static String peek() {
        return ThreadContext.peek();
    }

    public static void push(String message) {
        ThreadContext.push((String)message);
    }

    public static void remove() {
        ThreadContext.removeStack();
    }

    public static void setMaxDepth(int maxDepth) {
        ThreadContext.trim((int)maxDepth);
    }
}

