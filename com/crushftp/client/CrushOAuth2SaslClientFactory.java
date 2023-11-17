/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.CrushOAuth2SaslClient;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslClientFactory;

public class CrushOAuth2SaslClientFactory
implements SaslClientFactory {
    public SaslClient createSaslClient(String[] mechanisms, String authorizationId, String protocol, String serverName, Map props, CallbackHandler callbackHandler) {
        boolean matchedMechanism = false;
        int i = 0;
        while (i < mechanisms.length) {
            if ("XOAUTH2".equalsIgnoreCase(mechanisms[i])) {
                matchedMechanism = true;
                break;
            }
            ++i;
        }
        if (!matchedMechanism) {
            return null;
        }
        return new CrushOAuth2SaslClient((String)props.get("mail.sasl.mechanisms.oauth2.oauthToken"), callbackHandler);
    }

    public String[] getMechanismNames(Map props) {
        return new String[]{"XOAUTH2"};
    }
}

