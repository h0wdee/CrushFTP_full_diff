/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.IncompatibleAlgorithm;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SshException
extends Exception {
    private static final long serialVersionUID = 9007933160589824147L;
    public static final int UNEXPECTED_TERMINATION = 1;
    public static final int REMOTE_HOST_DISCONNECTED = 2;
    public static final int PROTOCOL_VIOLATION = 3;
    public static final int BAD_API_USAGE = 4;
    public static final int INTERNAL_ERROR = 5;
    public static final int CHANNEL_FAILURE = 6;
    public static final int UNSUPPORTED_ALGORITHM = 7;
    public static final int CANCELLED_CONNECTION = 8;
    public static final int KEY_EXCHANGE_FAILED = 9;
    public static final int CONNECT_FAILED = 10;
    public static final int LICENSE_ERROR = 11;
    public static final int CONNECTION_CLOSED = 12;
    public static final int AGENT_ERROR = 13;
    public static final int FORWARDING_ERROR = 14;
    public static final int PSEUDO_TTY_ERROR = 15;
    public static final int SHELL_ERROR = 15;
    public static final int SESSION_STREAM_ERROR = 15;
    public static final int JCE_ERROR = 16;
    public static final int POSSIBLE_CORRUPT_FILE = 17;
    public static final int SCP_TRANSFER_CANCELLED = 18;
    public static final int SOCKET_TIMEOUT = 19;
    public static final int PROMPT_TIMEOUT = 20;
    public static final int MESSAGE_TIMEOUT = 21;
    public static final int CIPHER_ERROR = 57345;
    public static final int MAC_ERROR = 57346;
    public static final int KEY_EXCHANGE_ERROR = 57347;
    public static final int COMPRESSION_ERROR = 57348;
    public static final int HOST_KEY_ERROR = 57349;
    public static final int FAILED_NEGOTIATION = 57350;
    public static final int UNSUPPORTED_OPERATION = 57351;
    public static final int UNEXPECTED_EOF_IN_KEYEXCHANGE = 57352;
    public static final int PROBALITY_KEX_TOO_LARGE = 57353;
    public static final int KEY_EXCHANGE_RETRY = 57354;
    int reason;
    String component;
    Throwable cause;
    List<IncompatibleAlgorithm> incompatible = null;

    public SshException(int reason, String component) {
        this(String.format("%s generated an error", component), reason, null, component);
    }

    public SshException(String msg, int reason) {
        this(msg, reason, null, null);
    }

    public SshException(String msg, int reason, List<IncompatibleAlgorithm> incompatible) {
        this(msg, reason, null, null);
        this.incompatible = incompatible;
    }

    public SshException(String msg, int reason, String component) {
        this(msg, reason, null, component);
    }

    public SshException(int reason, Throwable cause) {
        this(null, reason, cause, null);
    }

    public SshException(Throwable cause, int reason) {
        this(null, reason, cause, null);
    }

    public SshException(String msg, Throwable cause) {
        this(msg, 5, cause, null);
    }

    public SshException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public SshException(String msg, int reason, Throwable cause) {
        this(msg, reason, cause, null);
    }

    public SshException(String msg, int reason, Throwable cause, String component) {
        super(msg == null ? (cause == null ? "Unknown cause" : cause.getClass().getName()) : msg);
        this.cause = cause;
        this.reason = reason;
        this.component = component;
    }

    public int getReason() {
        return this.reason;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }

    public String getComponent() {
        return this.component;
    }

    public Collection<IncompatibleAlgorithm> getIncompatibleAlgorithms() {
        if (this.incompatible == null) {
            return Collections.emptyList();
        }
        return this.incompatible;
    }
}

