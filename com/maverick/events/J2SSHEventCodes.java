/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.events;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class J2SSHEventCodes {
    public static final String ATTRIBUTE_HOST_KEY = "HOST_KEY";
    public static final String ATTRIBUTE_HOST_PUBLIC_KEY = "HOST_PUBLIC_KEY";
    public static final String ATTRIBUTE_CLIENT = "CLIENT";
    public static final String ATTRIBUTE_SESSION = "SESSION";
    public static final String ATTRIBUTE_REMOTE_IDENT = "REMOTE_IDENT";
    public static final String ATTRIBUTE_LOCAL_IDENT = "LOCAL_IDENT";
    public static final String ATTRIBUTE_USING_KEY_EXCHANGE = "USING_KEY_EXCHANGE";
    public static final String ATTRIBUTE_USING_PUBLICKEY = "USING_PUBLICKEY";
    public static final String ATTRIBUTE_USING_CS_CIPHER = "USING_CS_CIPHER";
    public static final String ATTRIBUTE_USING_SC_CIPHER = "USING_SC_CIPHERC";
    public static final String ATTRIBUTE_USING_CS_MAC = "USING_CS_MAC";
    public static final String ATTRIBUTE_USING_SC_MAC = "USING_SC_MAC";
    public static final String ATTRIBUTE_USING_CS_COMPRESSION = "USING_CS_COMPRESSION";
    public static final String ATTRIBUTE_USING_SC_COMPRESSION = "USING_SC_COMPRESSION";
    public static final String ATTRIBUTE_REMOTE_KEY_EXCHANGES = "REMOTE_KEY_EXCHANGES";
    public static final String ATTRIBUTE_REMOTE_PUBLICKEYS = "REMOTE_PUBLICKEYS";
    public static final String ATTRIBUTE_REMOTE_CIPHERS_CS = "REMOTE_CIPHERS_CS";
    public static final String ATTRIBUTE_REMOTE_CIPHERS_SC = "REMOTE_CIPHERS_SC";
    public static final String ATTRIBUTE_REMOTE_CS_MACS = "REMOTE_CS_MACS";
    public static final String ATTRIBUTE_REMOTE_SC_MACS = "REMOTE_SC_MACS";
    public static final String ATTRIBUTE_REMOTE_CS_COMPRESSIONS = "REMOTE_CS_COMPRESSIONS";
    public static final String ATTRIBUTE_REMOTE_SC_COMPRESSIONS = "REMOTE_SC_COMPRESSIONS";
    public static final String ATTRIBUTE_LOCAL_KEY_EXCHANGES = "LOCAL_KEY_EXCHANGES";
    public static final String ATTRIBUTE_LOCAL_PUBLICKEYS = "LOCAL_PUBLICKEYS";
    public static final String ATTRIBUTE_LOCAL_CIPHERS_CS = "LOCAL_CIPHERS_CS";
    public static final String ATTRIBUTE_LOCAL_CIPHERS_SC = "LOCAL_CIPHERS_SC";
    public static final String ATTRIBUTE_LOCAL_CS_MACS = "LOCAL_CS_MACS";
    public static final String ATTRIBUTE_LOCAL_SC_MACS = "LOCAL_SC_MACS";
    public static final String ATTRIBUTE_LOCAL_CS_COMPRESSIONS = "LOCAL_CS_COMPRESSIONS";
    public static final String ATTRIBUTE_LOCAL_SC_COMPRESSIONS = "LOCAL_SC_COMPRESSIONS";
    public static final String ATTRIBUTE_AUTHENTICATION_METHODS = "AUTHENTICATION_METHODS";
    public static final String ATTRIBUTE_FORWARDING_TUNNEL_ENTRANCE = "FORWARDING_TUNNEL_ENTRANCE";
    public static final String ATTRIBUTE_FORWARDING_TUNNEL_EXIT = "FORWARDING_TUNNEL_EXIT";
    public static final String ATTRIBUTE_FILE_NAME = "FILE_NAME";
    public static final String ATTRIBUTE_FILE_NEW_NAME = "FILE_NEW_NAME";
    public static final String ATTRIBUTE_DIRECTORY_PATH = "DIRECTORY_PATH";
    public static final String ATTRIBUTE_COMMAND = "COMMAND";
    public static final String ATTRIBUTE_IP = "IP";
    public static final String ATTRIBUTE_NUMBER_OF_CONNECTIONS = "NUMBER_OF_CONNECTIONS";
    public static final String ATTRIBUTE_LOCAL_COMPONENT_LIST = "LOCAL_COMPONENT_LIST";
    public static final String ATTRIBUTE_REMOTE_COMPONENT_LIST = "REMOTE_COMPONENT_LIST";
    public static final int EVENT_HOSTKEY_RECEIVED = 0;
    public static final int EVENT_HOSTKEY_REJECTED = 1;
    public static final int EVENT_HOSTKEY_ACCEPTED = 2;
    public static final int EVENT_KEY_EXCHANGE_INIT = 3;
    public static final int EVENT_KEY_EXCHANGE_FAILURE = 4;
    public static final int EVENT_KEY_EXCHANGE_COMPLETE = 5;
    public static final int EVENT_AUTHENTICATION_METHODS_RECEIVED = 11;
    public static final int EVENT_USERAUTH_SUCCESS = 13;
    public static final int EVENT_USERAUTH_FAILURE = 14;
    public static final int EVENT_USERAUTH_FURTHER_AUTHENTICATION_REQUIRED = 15;
    public static final int EVENT_FORWARDING_LOCAL_STARTED = 16;
    public static final int EVENT_FORWARDING_REMOTE_STARTED = 17;
    public static final int EVENT_FORWARDING_LOCAL_STOPPED = 18;
    public static final int EVENT_FORWARDING_REMOTE_STOPPED = 19;
    public static final int EVENT_DISCONNECTED = 20;
    public static final int EVENT_RECEIVED_DISCONNECT = 21;
    public static final int EVENT_SHELL_SESSION_STARTED = 23;
    public static final int EVENT_SHELL_SESSION_FAILED_TO_START = 24;
    public static final int EVENT_SHELL_COMMAND = 30;
    public static final int EVENT_SUBSYSTEM_STARTED = 1001;
    public static final int EVENT_SFTP_SESSION_STARTED = 22;
    public static final int EVENT_SFTP_SESSION_STOPPED = 31;
    public static final int EVENT_SFTP_FILE_CLOSED = 25;
    public static final int EVENT_SFTP_FILE_OPENED = 26;
    public static final int EVENT_SFTP_FILE_RENAMED = 27;
    public static final int EVENT_SFTP_FILE_DELETED = 28;
    public static final int EVENT_SFTP_DIRECTORY_DELETED = 29;
    public static final int EVENT_FAILED_TO_NEGOTIATE_TRANSPORT_COMPONENT = 32;
    public static final int EVENT_CONNECTION_ATTEMPT = 100;
    public static final int EVENT_CONNECTED = 100;
    public static final int EVENT_REACHED_CONNECTION_LIMIT = 101;
    public static Map<Integer, String> messageCodes = new HashMap<Integer, String>();
    public static Map<String, String> messageAttributes = new HashMap<String, String>();

    static {
        Class<J2SSHEventCodes> mavevent = J2SSHEventCodes.class;
        Field[] fields = mavevent.getFields();
        for (int i = 0; i < fields.length; ++i) {
            int modifiers = fields[i].getModifiers();
            if (!Modifier.isFinal(modifiers) || !Modifier.isStatic(modifiers)) continue;
            try {
                String fieldName = fields[i].getName();
                if (fieldName.startsWith("EVENT_")) {
                    messageCodes.put((Integer)fields[i].get(null), fieldName.substring(6));
                    continue;
                }
                messageAttributes.put((String)fields[i].get(null), fieldName.substring(10));
                continue;
            }
            catch (IllegalArgumentException illegalArgumentException) {
                continue;
            }
            catch (IllegalAccessException illegalAccessException) {
                // empty catch block
            }
        }
    }
}

