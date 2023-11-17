/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh2;

import com.maverick.ssh.SshException;
import com.maverick.ssh2.AuthenticationClient;
import com.maverick.ssh2.AuthenticationProtocol;
import com.maverick.ssh2.AuthenticationResult;
import com.maverick.ssh2.KBIPrompt;
import com.maverick.ssh2.KBIRequestHandler;
import com.maverick.util.ByteArrayReader;
import com.maverick.util.ByteArrayWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KBIAuthentication
implements AuthenticationClient {
    static Logger log = LoggerFactory.getLogger(KBIAuthentication.class);
    String username;
    KBIRequestHandler handler;
    static final int SSH_MSG_USERAUTH_INFO_REQUEST = 60;
    static final int SSH_MSG_USERAUTH_INFO_RESPONSE = 61;

    public KBIAuthentication() {
    }

    public KBIAuthentication(KBIRequestHandler handler) {
        this.handler = handler;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getMethod() {
        return "keyboard-interactive";
    }

    public void setKBIRequestHandler(KBIRequestHandler handler) {
        this.handler = handler;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    @Override
    public void authenticate(AuthenticationProtocol authentication, String servicename) throws SshException, AuthenticationResult {
        baw = new ByteArrayWriter();
        try {
            try {
                if (this.handler == null) {
                    throw new SshException("A request handler must be set!", 4);
                }
                baw.writeString("");
                baw.writeString("");
                authentication.sendRequest(this.username, servicename, "keyboard-interactive", baw.toByteArray());
                while (true) lbl-1000:
                // 3 sources

                {
                    msg = authentication.readMessage();
                    bar = new ByteArrayReader(msg);
                    try {
                        if (bar.read() != 60) {
                            authentication.transport.disconnect(2, "Unexpected authentication message received!");
                            throw new SshException("Unexpected authentication message received!", 3);
                        }
                        name = bar.readString();
                        instruction = bar.readString();
                        langtag = bar.readString();
                        if (KBIAuthentication.log.isDebugEnabled()) {
                            authentication.transport.debug(KBIAuthentication.log, "Processing Keyboard Interactive authentication name={} langtag={} instructions={}", new Object[]{name, langtag, instruction});
                        }
                        num = (int)bar.readInt();
                        prompts = new KBIPrompt[num];
                        for (i = 0; i < num; ++i) {
                            prompt = bar.readString();
                            echo = bar.read() == 1;
                            prompts[i] = new KBIPrompt(prompt, echo);
                            if (!KBIAuthentication.log.isDebugEnabled()) continue;
                            authentication.transport.debug(KBIAuthentication.log, "Received prompt {} [{}]", new Object[]{prompt, String.valueOf(echo)});
                        }
                        if (!this.handler.showPrompts(name, instruction, prompts)) {
                            throw new AuthenticationResult(4);
                        }
                        baw.reset();
                        baw.write(61);
                        baw.writeInt(prompts.length);
                        for (i = 0; i < prompts.length; ++i) {
                            if (KBIAuthentication.log.isDebugEnabled()) {
                                authentication.transport.debug(KBIAuthentication.log, "Answering prompt {}", new Object[]{prompts[i].getPrompt()});
                            }
                            baw.writeString(prompts[i].getResponse());
                        }
                        authentication.transport.sendMessage(baw.toByteArray(), true);
                    }
                    finally {
                        bar.close();
                        continue;
                    }
                    break;
                }
            }
            catch (IOException ex) {
                throw new SshException(ex, 5);
            }
            ** GOTO lbl-1000
        }
        catch (Throwable var15_16) {
            try {
                baw.close();
            }
            catch (IOException var16_17) {
                // empty catch block
            }
            throw var15_16;
        }
    }

    public String getMethodName() {
        return "keyboard-interactive";
    }
}

