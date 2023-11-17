/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.maverick.sshd.AuthenticationMechanism
 *  com.maverick.sshd.AuthenticationMechanismFactory
 *  com.maverick.sshd.AuthenticationProtocol
 *  com.maverick.sshd.Authenticator
 *  com.maverick.sshd.Connection
 *  com.maverick.sshd.KeyboardInteractiveAuthentication
 *  com.maverick.sshd.KeyboardInteractiveAuthenticationProvider
 *  com.maverick.sshd.PasswordAuthentication
 *  com.maverick.sshd.PasswordAuthenticationProvider
 *  com.maverick.sshd.PasswordKeyboardInteractiveProvider
 *  com.maverick.sshd.PublicKeyAuthentication
 *  com.maverick.sshd.PublicKeyAuthenticationProvider
 *  com.maverick.sshd.TransportProtocol
 *  com.maverick.sshd.UnsupportedChannelException
 *  com.maverick.sshd.platform.KeyboardInteractiveProvider
 */
package crushftp.server.ssh;

import com.maverick.sshd.AuthenticationMechanism;
import com.maverick.sshd.AuthenticationMechanismFactory;
import com.maverick.sshd.AuthenticationProtocol;
import com.maverick.sshd.Authenticator;
import com.maverick.sshd.Connection;
import com.maverick.sshd.KeyboardInteractiveAuthentication;
import com.maverick.sshd.KeyboardInteractiveAuthenticationProvider;
import com.maverick.sshd.PasswordAuthentication;
import com.maverick.sshd.PasswordAuthenticationProvider;
import com.maverick.sshd.PasswordKeyboardInteractiveProvider;
import com.maverick.sshd.PublicKeyAuthentication;
import com.maverick.sshd.PublicKeyAuthenticationProvider;
import com.maverick.sshd.TransportProtocol;
import com.maverick.sshd.UnsupportedChannelException;
import com.maverick.sshd.platform.KeyboardInteractiveProvider;
import crushftp.server.ssh.PublicKeyVerifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class LimitedAuthProvider
implements AuthenticationMechanismFactory {
    protected Set supportedMechanisms = new HashSet();
    protected List passwordProviders = new ArrayList();
    protected List publickeyProviders = new ArrayList();
    protected List keyboardInteractiveProviders = new ArrayList();

    public void addPasswordAuthenticationProvider(PasswordAuthenticationProvider provider) {
        this.passwordProviders.add(provider);
        this.supportedMechanisms.add("password");
    }

    public void addPublicKeyAuthenticationProvider(PublicKeyAuthenticationProvider provider) {
        this.publickeyProviders.add(provider);
        this.supportedMechanisms.add("publickey");
    }

    public void addKeyboardInteractiveProvider(KeyboardInteractiveAuthenticationProvider provider) {
        this.keyboardInteractiveProviders.add(provider);
        this.supportedMechanisms.add("keyboard-interactive");
    }

    public void removePasswordAuthenticationProvider(PasswordAuthenticationProvider provider) {
        this.passwordProviders.remove(provider);
        if (this.passwordProviders.size() == 0) {
            this.supportedMechanisms.remove("password");
        }
    }

    public void removePublicKeyAuthenticationProvider(PublicKeyAuthenticationProvider provider) {
        this.publickeyProviders.remove(provider);
        if (this.publickeyProviders.size() == 0) {
            this.supportedMechanisms.remove("publickey");
        }
    }

    public void removeKeyboardInteractiveProvider(KeyboardInteractiveProvider provider) {
        this.keyboardInteractiveProviders.remove(provider);
    }

    public void addProvider(Authenticator provider) {
        if (provider instanceof PasswordAuthenticationProvider) {
            this.addPasswordAuthenticationProvider((PasswordAuthenticationProvider)provider);
        } else if (provider instanceof PublicKeyAuthenticationProvider) {
            this.addPublicKeyAuthenticationProvider((PublicKeyAuthenticationProvider)provider);
        } else if (provider instanceof KeyboardInteractiveAuthenticationProvider) {
            this.addKeyboardInteractiveProvider((KeyboardInteractiveAuthenticationProvider)provider);
        } else {
            throw new IllegalArgumentException(String.valueOf(provider.getClass().getName()) + " is not a supported AuthenticationProvider");
        }
    }

    public AuthenticationMechanism createInstance(String name, TransportProtocol transport, AuthenticationProtocol authentication, Connection con) throws UnsupportedChannelException {
        if (name.equals("password")) {
            return new PasswordAuthentication(transport, authentication, con, this.getPasswordAuthenticationProviders(con));
        }
        if (name.equals("publickey")) {
            return new PublicKeyAuthentication(transport, authentication, con, this.getPublicKeyAuthenticationProviders(con));
        }
        if (name.equals("keyboard-interactive")) {
            return new KeyboardInteractiveAuthentication(transport, authentication, con, this.getKeyboardInteractiveProviders(con));
        }
        throw new UnsupportedChannelException();
    }

    public KeyboardInteractiveAuthenticationProvider[] getKeyboardInteractiveProviders(Connection con) {
        if (this.keyboardInteractiveProviders.size() == 0) {
            return new KeyboardInteractiveAuthenticationProvider[]{new KeyboardInteractiveAuthenticationProvider(){

                public KeyboardInteractiveProvider createInstance(Connection con) {
                    return new PasswordKeyboardInteractiveProvider(LimitedAuthProvider.this.passwordProviders.toArray(new PasswordAuthenticationProvider[0]), con);
                }
            }};
        }
        return this.keyboardInteractiveProviders.toArray(new KeyboardInteractiveAuthenticationProvider[0]);
    }

    public String[] getRequiredMechanisms(Connection con) {
        Properties user = PublicKeyVerifier.findUserForSSH(con.getUsername(), con);
        if (user != null && user.getProperty("publickey_password", "false").equalsIgnoreCase("true")) {
            return new String[]{"publickey", "password"};
        }
        if (user != null && user.getProperty("publickey_keyboardinteractive", "false").equalsIgnoreCase("true")) {
            return new String[]{"publickey", "keyboard-interactive"};
        }
        return con.getContext().getRequiredAuthentications();
    }

    public String[] getSupportedMechanisms() {
        return this.supportedMechanisms.toArray(new String[0]);
    }

    public PublicKeyAuthenticationProvider[] getPublicKeyAuthenticationProviders(Connection con) {
        return this.publickeyProviders.toArray(new PublicKeyAuthenticationProvider[0]);
    }

    public PasswordAuthenticationProvider[] getPasswordAuthenticationProviders(Connection con) {
        return this.passwordProviders.toArray(new PasswordAuthenticationProvider[0]);
    }

    public Authenticator[] getProviders(String name, Connection con) {
        if (name.equals("password")) {
            return this.getPasswordAuthenticationProviders(con);
        }
        if (name.equals("publickey")) {
            return this.getPublicKeyAuthenticationProviders(con);
        }
        if (name.equals("keyboard-interactive")) {
            return this.getKeyboardInteractiveProviders(con);
        }
        throw new IllegalArgumentException("Unknown provider type");
    }
}

