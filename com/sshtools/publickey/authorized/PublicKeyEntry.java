/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.publickey.authorized;

import com.maverick.ssh.components.SshPublicKey;
import com.maverick.util.Entry;
import com.sshtools.publickey.SshPublicKeyFileFactory;
import com.sshtools.publickey.authorized.AuthorizedKeyOptions;
import com.sshtools.publickey.authorized.CommandOption;
import com.sshtools.publickey.authorized.EnvironmentOption;
import com.sshtools.publickey.authorized.FromOption;
import com.sshtools.publickey.authorized.Option;
import com.sshtools.publickey.authorized.Patterns;
import com.sshtools.publickey.authorized.PermitOpenOption;
import com.sshtools.publickey.authorized.PrincipalsOption;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class PublicKeyEntry
extends Entry<SshPublicKey> {
    String comment;
    LinkedList<Option<?>> orderedOptions = new LinkedList();

    PublicKeyEntry(SshPublicKey value, LinkedList<Option<?>> orderedOptions, String comment) {
        super(value);
        this.orderedOptions = orderedOptions;
        this.comment = comment;
    }

    void setOption(Option<?> o) {
        Option<?> current;
        if (!(o instanceof EnvironmentOption) && (current = this.getOption(o.getName())) != null) {
            this.orderedOptions.remove(current);
        }
        this.orderedOptions.addLast(o);
    }

    void removeOption(Option<?> o) {
        if (o instanceof EnvironmentOption) {
            throw new IllegalArgumentException("Incorrect use. Use removeEnvironmentVariable method");
        }
        Option<?> current = this.getOption(o.getName());
        if (current != null) {
            this.orderedOptions.remove(current);
        }
    }

    boolean hasOption(Option<?> option) {
        for (Option option2 : this.orderedOptions) {
            if (!option2.getName().equals(option.getName())) continue;
            return true;
        }
        return false;
    }

    Option<?> getOption(String name) {
        for (Option option : this.orderedOptions) {
            if (!option.getName().equals(name)) continue;
            return option;
        }
        return null;
    }

    public void addEnvironmentVariable(String name, String value) {
        this.setOption(new EnvironmentOption(name, value));
    }

    public void removeEnvironmentVariable(String name) {
        Option o;
        EnvironmentOption e = null;
        Iterator iterator = this.orderedOptions.iterator();
        while (!(!iterator.hasNext() || (o = (Option)iterator.next()) instanceof EnvironmentOption && (e = (EnvironmentOption)o).getEnvironmentName().equals(name))) {
        }
        if (e != null) {
            this.orderedOptions.remove(e);
        }
    }

    Map<String, String> getEnvironmentOptions() {
        HashMap<String, String> env = new HashMap<String, String>();
        for (Option option : this.orderedOptions) {
            if (!(option instanceof EnvironmentOption)) continue;
            EnvironmentOption e = (EnvironmentOption)option;
            env.put(e.getEnvironmentName(), e.getEnvironmentValue());
        }
        return Collections.unmodifiableMap(env);
    }

    @Override
    public String getFormattedEntry() throws IOException {
        StringBuffer buf = new StringBuffer();
        for (Option option : this.orderedOptions) {
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append(option.getFormattedOption());
        }
        if (buf.length() > 0) {
            buf.append(" ");
        }
        buf.append(new String(SshPublicKeyFileFactory.create((SshPublicKey)this.value, this.comment, 0).getFormattedKey(), "UTF-8"));
        return buf.toString();
    }

    protected boolean supportsRestrictedOption(Option<?> option) {
        boolean restrict = this.hasOption(AuthorizedKeyOptions.RESRICT);
        if (restrict) {
            return this.hasOption(option);
        }
        return !this.hasOption(AuthorizedKeyOptions.getNoOption(option));
    }

    public boolean supportsPty() {
        return this.supportsRestrictedOption(AuthorizedKeyOptions.PTY);
    }

    public boolean supportsPortForwarding() {
        return this.supportsRestrictedOption(AuthorizedKeyOptions.PORT_FORWARDING);
    }

    public boolean supportsAgentForwarding() {
        return this.supportsRestrictedOption(AuthorizedKeyOptions.AGENT_FORWARDING);
    }

    public boolean supportsUserRc() {
        return this.supportsRestrictedOption(AuthorizedKeyOptions.USER_RC);
    }

    public boolean supportsX11Forwarding() {
        return this.supportsRestrictedOption(AuthorizedKeyOptions.X11_FORWARDING);
    }

    public boolean isCertAuthority() {
        return this.hasOption(AuthorizedKeyOptions.CERT_AUTHORITY);
    }

    public boolean requiresCommandExecution() {
        return this.hasOption(CommandOption.class);
    }

    boolean hasOption(Class<? extends Option<?>> clz) {
        for (Option option : this.orderedOptions) {
            if (!option.getClass().isAssignableFrom(clz)) continue;
            return true;
        }
        return false;
    }

    Option<?> getOption(Class<? extends Option<?>> clz) {
        for (Option option : this.orderedOptions) {
            if (!option.getClass().isAssignableFrom(clz)) continue;
            return option;
        }
        return null;
    }

    public String getCommand() {
        if (this.hasOption(CommandOption.class)) {
            return (String)this.getOption(CommandOption.class).getValue();
        }
        return null;
    }

    public void setCommand(String command) {
        this.setOption(new CommandOption(command));
    }

    public void addConnectFrom(String remoteAddress) {
        if (!this.hasOption(FromOption.class)) {
            this.setOption(new FromOption(remoteAddress));
        } else {
            FromOption o = (FromOption)this.getOption(FromOption.class);
            ((Collection)o.getValue()).add(remoteAddress);
        }
    }

    public void removeConnectFrom(String remoteAddress) {
        if (this.hasOption(FromOption.class)) {
            FromOption o = (FromOption)this.getOption(FromOption.class);
            ((Collection)o.getValue()).remove(remoteAddress);
        }
    }

    public boolean canConnectFrom(String remoteAddress) {
        if (this.hasOption(FromOption.class)) {
            return Patterns.matchesWithCIDR((Collection)this.getOption(FromOption.class).getValue(), remoteAddress);
        }
        return true;
    }

    public void addForwardTo(String forwardTo) {
        if (!this.hasOption(PermitOpenOption.class)) {
            this.setOption(new PermitOpenOption(forwardTo));
        } else {
            PermitOpenOption o = (PermitOpenOption)this.getOption(PermitOpenOption.class);
            ((Collection)o.getValue()).add(forwardTo);
        }
    }

    public void removeForwardTo(String forwardTo) {
        if (this.hasOption(PermitOpenOption.class)) {
            PermitOpenOption o = (PermitOpenOption)this.getOption(PermitOpenOption.class);
            ((Collection)o.getValue()).remove(forwardTo);
        }
    }

    public boolean canForwardTo(String hostname, int port) {
        if (!this.supportsPortForwarding()) {
            return false;
        }
        if (this.hasOption(PermitOpenOption.class)) {
            for (String rule : (Collection)this.getOption(PermitOpenOption.class).getValue()) {
                int idx = rule.indexOf(58);
                if (idx == -1) {
                    throw new IllegalArgumentException("Invalid permitopen rule " + rule);
                }
                String permitHostname = rule.substring(0, idx);
                String permitPort = rule.substring(idx + 1);
                if (!(permitPort.equals("*") ? hostname.equalsIgnoreCase(permitHostname) : hostname.equalsIgnoreCase(permitHostname) && port == Integer.parseInt(permitPort))) continue;
                return true;
            }
            return false;
        }
        return true;
    }

    public Collection<String> getPrincipals() {
        if (!this.hasOption(PrincipalsOption.class)) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection((Collection)this.getOption(PrincipalsOption.class).getValue());
    }

    public void addPrincipal(String principal) {
        if (!this.hasOption(PrincipalsOption.class)) {
            this.setOption(new PrincipalsOption(principal));
        } else {
            PrincipalsOption o = (PrincipalsOption)this.getOption(PrincipalsOption.class);
            ((Collection)o.getValue()).add(principal);
        }
    }

    public void removePrincipal(String principal) {
        if (this.hasOption(PrincipalsOption.class)) {
            PrincipalsOption o = (PrincipalsOption)this.getOption(PrincipalsOption.class);
            ((Collection)o.getValue()).remove(principal);
        }
    }
}

