/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.sshtools.publickey.authorized;

import com.maverick.ssh.SshException;
import com.maverick.ssh.components.ComponentManager;
import com.maverick.ssh.components.SshPublicKey;
import com.maverick.ssh.components.SshRsaPublicKey;
import com.maverick.util.Base64;
import com.maverick.util.BlankLineEntry;
import com.maverick.util.CommentEntry;
import com.maverick.util.Entry;
import com.sshtools.publickey.SshPublicKeyFileFactory;
import com.sshtools.publickey.authorized.CommandOption;
import com.sshtools.publickey.authorized.EnvironmentOption;
import com.sshtools.publickey.authorized.FromOption;
import com.sshtools.publickey.authorized.NoArgOption;
import com.sshtools.publickey.authorized.Option;
import com.sshtools.publickey.authorized.PermitOpenOption;
import com.sshtools.publickey.authorized.PrincipalsOption;
import com.sshtools.publickey.authorized.PublicKeyEntry;
import com.sshtools.publickey.authorized.TunnelOption;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizedKeyFile {
    static Logger log = LoggerFactory.getLogger(AuthorizedKeyFile.class);
    LinkedList<Entry<?>> allEntries = new LinkedList();
    LinkedList<PublicKeyEntry> keyEntries = new LinkedList();
    Set<String> supportedOptions = new HashSet<String>(Arrays.asList("agent-forwarding", "cert-authority", "command", "environment", "from", "no-agent-forwarding", "no-port-forwarding", "no-pty", "no-user-rc", "no-X11-forwarding", "permitopen", "port-forwarding", "principals", "pty", "restrict", "tunnel", "user-rc", "X11-forwarding"));

    public AuthorizedKeyFile() {
    }

    public AuthorizedKeyFile(String authorized_keys) throws IOException {
        this.load(new ByteArrayInputStream(authorized_keys.getBytes("UTF-8")));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void load(InputStream in) throws IOException {
        try {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("")) {
                    this.addBlankLine();
                    continue;
                }
                if (line.trim().startsWith("#")) {
                    this.addCommentLine(line);
                    continue;
                }
                String[] tokens = this.parseLine(line, ' ', false);
                if (tokens.length < 2) {
                    this.addErrorEntry(line);
                    continue;
                }
                if (this.isNumeric(tokens[0]) && tokens.length >= 3) {
                    try {
                        this.addSSH1KeyEntry("", tokens[0], tokens[1], tokens[2], tokens.length > 3 ? tokens[3] : "");
                    }
                    catch (SshException e) {
                        this.addErrorEntry(line);
                    }
                    continue;
                }
                if (this.isBase64(tokens[1]) && tokens.length >= 2) {
                    try {
                        this.addSSH2KeyEntry("", tokens[0], tokens[1], tokens.length > 2 ? tokens[2] : "");
                    }
                    catch (SshException e) {
                        this.addErrorEntry(line);
                    }
                    continue;
                }
                if (this.isNumeric(tokens[1]) && tokens.length >= 4) {
                    try {
                        this.addSSH1KeyEntry(tokens[0], tokens[1], tokens[2], tokens[3], tokens.length > 4 ? tokens[4] : "");
                    }
                    catch (SshException e) {
                        this.addErrorEntry(line);
                    }
                    continue;
                }
                if (tokens.length <= 2 || !this.isBase64(tokens[2])) continue;
                try {
                    this.addSSH2KeyEntry(tokens[0], tokens[1], tokens[2], tokens.length > 3 ? tokens[3] : "");
                }
                catch (SshException e) {
                    this.addErrorEntry(line);
                }
            }
        }
        finally {
            try {
                in.close();
            }
            catch (Exception exception) {}
        }
    }

    public boolean isAuthorizedKey(SshPublicKey key) {
        for (PublicKeyEntry k : this.keyEntries) {
            if (!((SshPublicKey)k.getValue()).equals(key)) continue;
            return true;
        }
        return false;
    }

    public PublicKeyEntry getKeyEntry(SshPublicKey key) {
        for (PublicKeyEntry k : this.keyEntries) {
            if (!((SshPublicKey)k.getValue()).equals(key)) continue;
            return k;
        }
        return null;
    }

    public Collection<PublicKeyEntry> getKeys() {
        return Collections.unmodifiableCollection(this.keyEntries);
    }

    public void removeKeys(SshPublicKey ... keys) {
        for (SshPublicKey key : keys) {
            try {
                PublicKeyEntry entry = this.getKeyEntry(key);
                this.removeKey(entry);
            }
            catch (NoSuchElementException noSuchElementException) {
                // empty catch block
            }
        }
    }

    public void removeKey(PublicKeyEntry entry) {
        this.keyEntries.remove(entry);
        this.allEntries.remove(entry);
    }

    public void addKey(SshPublicKey key, String comment) {
        PublicKeyEntry entry = new PublicKeyEntry(key, new LinkedList(), comment);
        this.allEntries.addLast(entry);
        this.keyEntries.addLast(entry);
    }

    public void addKey(SshPublicKey key, String comment, Option<?> ... options) {
        if (this.getKeyEntry(key) != null) {
            throw new IllegalArgumentException("Public key is already present in authorized_keys file");
        }
        PublicKeyEntry entry = new PublicKeyEntry(key, new LinkedList(Arrays.asList(options)), comment);
        this.allEntries.addLast(entry);
        this.keyEntries.addLast(entry);
    }

    public void setOption(PublicKeyEntry entry, Option<?> option) {
        entry.setOption(option);
    }

    public void setOption(SshPublicKey key, Option<?> option) {
        this.getKeyEntry(key).setOption(option);
    }

    boolean isBase64(String line) {
        return line.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$");
    }

    boolean isNumeric(String line) {
        try {
            Integer.parseInt(line);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    void addErrorEntry(String line) {
        log.error("Failed to parse authorized_keys line: " + line);
        this.allEntries.add(new ErrorEntry(line));
    }

    void addCommentLine(String line) {
        this.allEntries.add(new CommentEntry(line));
    }

    void addBlankLine() {
        this.allEntries.add(new BlankLineEntry());
    }

    public String getFormattedFile() throws IOException {
        StringBuffer buf = new StringBuffer();
        for (Entry entry : this.allEntries) {
            if (buf.length() > 0) {
                buf.append("\r\n");
            }
            buf.append(entry.getFormattedEntry());
        }
        return buf.toString();
    }

    void addSSH1KeyEntry(String options, String bitLength, String e, String n, String comment) throws SshException {
        BigInteger publicExponent = new BigInteger(e);
        BigInteger modulus = new BigInteger(n);
        SshRsaPublicKey key = ComponentManager.getInstance().createRsaPublicKey(modulus, publicExponent, 1);
        LinkedList<Option<?>> parsedOptions = this.parseOptions(options);
        PublicKeyEntry entry = new PublicKeyEntry(key, parsedOptions, comment);
        this.keyEntries.add(entry);
        this.allEntries.add(entry);
    }

    void addSSH2KeyEntry(String options, String algorithm, String encodedKey, String comment) throws SshException, IOException {
        SshPublicKey key = SshPublicKeyFileFactory.decodeSSH2PublicKey(Base64.decode(encodedKey));
        LinkedList<Option<?>> parsedOptions = this.parseOptions(options);
        PublicKeyEntry entry = new PublicKeyEntry(key, parsedOptions, comment);
        this.keyEntries.add(entry);
        this.allEntries.add(entry);
    }

    static String splitName(String option) {
        int idx = option.indexOf(61);
        if (idx == -1) {
            throw new IllegalArgumentException("Option with invalid format! " + option);
        }
        return option.substring(0, idx);
    }

    static String splitValue(String option) {
        int idx = option.indexOf(61);
        if (idx == -1) {
            throw new IllegalArgumentException("Option with invalid format! " + option);
        }
        return option.substring(idx + 1);
    }

    LinkedList<Option<?>> parseOptions(String options) {
        String[] parsedOptions;
        if (options.trim().equals("")) {
            return new LinkedList();
        }
        LinkedList builtOptions = new LinkedList();
        for (String option : parsedOptions = this.parseLine(options, ',', true)) {
            if (option.equalsIgnoreCase("agent-forwarding")) {
                builtOptions.add(new NoArgOption(option));
                continue;
            }
            if (option.equalsIgnoreCase("cert-authority")) {
                builtOptions.add(new NoArgOption(option));
                continue;
            }
            if (option.startsWith("command=")) {
                builtOptions.add(new CommandOption(AuthorizedKeyFile.splitValue(option)));
                continue;
            }
            if (option.startsWith("environment=")) {
                builtOptions.add(new EnvironmentOption(AuthorizedKeyFile.splitValue(option)));
                continue;
            }
            if (option.startsWith("from=")) {
                builtOptions.add(new FromOption(AuthorizedKeyFile.splitValue(option)));
                continue;
            }
            if (option.equalsIgnoreCase("no-agent-forwarding")) {
                builtOptions.add(new NoArgOption(option));
                continue;
            }
            if (option.equalsIgnoreCase("no-port-forwarding")) {
                builtOptions.add(new NoArgOption(option));
                continue;
            }
            if (option.equalsIgnoreCase("no-pty")) {
                builtOptions.add(new NoArgOption(option));
                continue;
            }
            if (option.equalsIgnoreCase("no-user-rc")) {
                builtOptions.add(new NoArgOption(option));
                continue;
            }
            if (option.equalsIgnoreCase("no-X11-forwarding")) {
                builtOptions.add(new NoArgOption(option));
                continue;
            }
            if (option.startsWith("permitopen=")) {
                builtOptions.add(new PermitOpenOption(AuthorizedKeyFile.splitValue(option)));
                continue;
            }
            if (option.equalsIgnoreCase("port-forwarding")) {
                builtOptions.add(new NoArgOption(option));
                continue;
            }
            if (option.startsWith("principals=")) {
                builtOptions.add(new PrincipalsOption(AuthorizedKeyFile.splitValue(option)));
                continue;
            }
            if (option.equalsIgnoreCase("pty")) {
                builtOptions.add(new NoArgOption(option));
                continue;
            }
            if (option.equalsIgnoreCase("restrict")) {
                builtOptions.add(new NoArgOption(option));
                continue;
            }
            if (option.startsWith("tunnel")) {
                builtOptions.add(new TunnelOption(AuthorizedKeyFile.splitValue(option)));
                continue;
            }
            if (option.equalsIgnoreCase("user-rc")) {
                builtOptions.add(new NoArgOption(option));
                continue;
            }
            if (option.equalsIgnoreCase("X11-forwarding")) {
                builtOptions.add(new NoArgOption(option));
                continue;
            }
            throw new IllegalArgumentException(option + " not recognised");
        }
        return builtOptions;
    }

    String[] parseLine(String line, char delim, boolean stripQuotes) {
        int i = 0;
        StringBuffer buf = new StringBuffer();
        boolean quoted = false;
        boolean escaped = false;
        ArrayList<String> tokens = new ArrayList<String>();
        while (i < line.length()) {
            char ch = line.charAt(i);
            if (!quoted && ch == delim) {
                tokens.add(buf.toString());
                buf.setLength(0);
            } else {
                if (ch == '\\') {
                    escaped = true;
                    buf.append(ch);
                    ++i;
                    continue;
                }
                if (ch == '\"' && !escaped) {
                    boolean bl = quoted = !quoted;
                    if (!stripQuotes) {
                        buf.append(ch);
                    }
                } else {
                    buf.append(ch);
                }
            }
            ++i;
            escaped = false;
        }
        if (buf.length() > 0) {
            tokens.add(buf.toString());
        }
        return tokens.toArray(new String[0]);
    }

    public static void main(String[] args) {
        try {
            AuthorizedKeyFile auth = new AuthorizedKeyFile("restrict,agent-forwarding,cert-authority,command=\"ls\",environment=\"VALUE=value\",from=\"127.0.0.1,192.168.0.0/24\",no-agent-forwarding,no-port-forwarding,no-pty,no-user-rc,no-X11-forwarding,permitopen=\"localhost:80,localhost:443\",port-forwarding,principals=\"lee,root\",pty,tunnel=\"3\",user-rc,X11-forwarding ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDRqJb3pwl7vkQAMUxYpSHPWnZGJJ5bBP0GA3fK/JWIdXplSclIleukhJC/gP4HQTVPAQ+lMl7L9dy9mScRHcRYZzpY8Cm46mji7HaYPgDrjHYnla6A6cOqdJuw8IYk3vVjmo49OZLJE7p2GwdLg0poFFwhUZa5wJQxQwy8PetehgN3oUYOB7NP6wHB4jdfY6GrMWzDeP52OX3QOZZKZfoKuVeVATmYCvn7LFYb5ysEFBve2Jr7bXcN5AFDpAerM/4ybRWcpWGt7IG7bOMLlxI2j9zEkTSwFQ5ShakyaZNA1v+qZXZJ3y54OwqETUSjFmDpA2RBGWJ3wYbrN2sk5YJt lee@kit");
            PublicKeyEntry e = auth.getKeys().iterator().next();
            auth = new AuthorizedKeyFile("from=\"!192.168.0.4?,192.168.0.0/24\",permitopen=\"localhost:22\" ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDRqJb3pwl7vkQAMUxYpSHPWnZGJJ5bBP0GA3fK/JWIdXplSclIleukhJC/gP4HQTVPAQ+lMl7L9dy9mScRHcRYZzpY8Cm46mji7HaYPgDrjHYnla6A6cOqdJuw8IYk3vVjmo49OZLJE7p2GwdLg0poFFwhUZa5wJQxQwy8PetehgN3oUYOB7NP6wHB4jdfY6GrMWzDeP52OX3QOZZKZfoKuVeVATmYCvn7LFYb5ysEFBve2Jr7bXcN5AFDpAerM/4ybRWcpWGt7IG7bOMLlxI2j9zEkTSwFQ5ShakyaZNA1v+qZXZJ3y54OwqETUSjFmDpA2RBGWJ3wYbrN2sk5YJt lee@kit");
            e = auth.getKeys().iterator().next();
            e.addEnvironmentVariable("FOO", "BAR");
            System.out.println("Agent Forwarding : " + e.supportsAgentForwarding());
            System.out.println("Port Forwarding  : " + e.supportsPortForwarding());
            System.out.println("Pty              : " + e.supportsPty());
            System.out.println("User RC          : " + e.supportsUserRc());
            System.out.println("X11 Forwarding   : " + e.supportsX11Forwarding());
            System.out.println("Fixed Command    : " + (e.requiresCommandExecution() ? e.getCommand() : "<No Fixed Command>"));
            System.out.println("Can Connect      : " + e.canConnectFrom("127.0.0.1"));
            System.out.println("Can Connect      : " + e.canConnectFrom("192.168.0.45"));
            System.out.println("Can Connect      : " + e.canConnectFrom("localhost"));
            System.out.println("Can Connect      : " + e.canConnectFrom("example.com"));
            System.out.println("Can Connect      : " + e.canConnectFrom("foo.example.com"));
            System.out.println("Can Forward To   : " + e.canForwardTo("localhost", 22));
            System.out.println("Can Forward To   : " + e.canForwardTo("localhost", 443));
            System.out.println("Environment      : " + e.getEnvironmentOptions().toString());
            System.out.println("Cert Authority   : " + e.isCertAuthority());
            System.out.println("Principals       : " + e.getPrincipals().toString());
            System.out.println();
            System.out.println(auth.getFormattedFile());
            System.out.println();
            e.addConnectFrom("10.0.0.0/16");
            e.removeConnectFrom("192.168.0.0/24");
            e.removeEnvironmentVariable("FOO");
            e.addPrincipal("lee");
            e.addForwardTo("localhost:4000");
            System.out.println("Agent Forwarding : " + e.supportsAgentForwarding());
            System.out.println("Port Forwarding  : " + e.supportsPortForwarding());
            System.out.println("Pty              : " + e.supportsPty());
            System.out.println("User RC          : " + e.supportsUserRc());
            System.out.println("X11 Forwarding   : " + e.supportsX11Forwarding());
            System.out.println("Fixed Command    : " + (e.requiresCommandExecution() ? e.getCommand() : "<No Fixed Command>"));
            System.out.println("Can Connect      : " + e.canConnectFrom("127.0.0.1"));
            System.out.println("Can Connect      : " + e.canConnectFrom("192.168.0.45"));
            System.out.println("Can Connect      : " + e.canConnectFrom("localhost"));
            System.out.println("Can Connect      : " + e.canConnectFrom("example.com"));
            System.out.println("Can Connect      : " + e.canConnectFrom("foo.example.com"));
            System.out.println("Can Forward To   : " + e.canForwardTo("localhost", 22));
            System.out.println("Can Forward To   : " + e.canForwardTo("localhost", 443));
            System.out.println("Environment      : " + e.getEnvironmentOptions().toString());
            System.out.println("Cert Authority   : " + e.isCertAuthority());
            System.out.println("Principals       : " + e.getPrincipals().toString());
            System.out.println();
            System.out.println(auth.getFormattedFile());
            System.out.println();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ErrorEntry
    extends Entry<String> {
        ErrorEntry(String value) {
            super(value);
        }

        @Override
        public String getFormattedEntry() {
            return (String)this.value;
        }
    }
}

