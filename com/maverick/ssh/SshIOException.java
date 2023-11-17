/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.ssh;

import com.maverick.ssh.SshException;
import java.io.IOException;

public class SshIOException
extends IOException {
    private static final long serialVersionUID = 6171680689279356698L;
    SshException realEx;

    public SshIOException(SshException realEx) {
        super(realEx.getMessage(), realEx);
        this.realEx = realEx;
    }

    public SshException getRealException() {
        return this.realEx;
    }
}

