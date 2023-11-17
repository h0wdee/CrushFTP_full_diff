/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.crypto.digests.SHA256Digest
 */
package com.didisoft.pgp.net;

import com.didisoft.pgp.KeyPairInformation;
import com.didisoft.pgp.KeyStore;
import com.didisoft.pgp.bc.BaseLib;
import com.didisoft.pgp.bc.IOUtil;
import com.didisoft.pgp.events.ICustomKeyListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import lw.bouncycastle.crypto.digests.SHA256Digest;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class LDAPClient {
    public static final int DEFAULT_LDAP_PORT = 389;
    private static String LDAP_PROVIDER = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String URL_CONTEXT_PREFIX = "com.sun.jndi.url";
    private static String REFERRALS_IGNORE = "ignore";
    private static final String SEARCH_SECURITY_LEVEL = "none";
    private String host;
    private String username = "";
    private byte[] xKey;
    private SealedObject password;
    private ICustomKeyListener passKeyListener = null;
    private int port = 389;
    private boolean partialMatch = true;
    private KeyStore helperKS = new KeyStore();

    public LDAPClient(String string) {
        this(string, 389);
    }

    public LDAPClient(String string, int n) {
        this.host = string;
        this.port = n;
    }

    public LDAPClient(String string, int n, String string2, String string3) {
        this(string, n);
        this.username = string2;
        SecureRandom secureRandom = IOUtil.getSecureRandom();
        this.xKey = new byte[16];
        secureRandom.nextBytes(this.xKey);
        this.setPassword(string3);
    }

    public LDAPClient(String string, int n, String string2, String string3, ICustomKeyListener iCustomKeyListener) {
        this(string, n);
        this.username = string2;
        SecureRandom secureRandom = IOUtil.getSecureRandom();
        this.passKeyListener = iCustomKeyListener;
        this.setPassword(string3);
    }

    private String getPassword() {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(this.passKeyListener == null ? this.xKey : this.paddAesKey(this.passKeyListener.getKey(this)), "AES");
            return new String((char[])this.password.getObject(secretKeySpec));
        }
        catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    private void setPassword(String string) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec secretKeySpec = new SecretKeySpec(this.passKeyListener == null ? this.xKey : this.paddAesKey(this.passKeyListener.getKey(this)), "AES");
            cipher.init(1, secretKeySpec);
            this.password = new SealedObject((Serializable)string.toCharArray(), cipher);
        }
        catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    public boolean isPartialMatchUserIds() {
        return this.partialMatch;
    }

    public void setPartialMatchUserIds(boolean bl) {
        this.partialMatch = bl;
    }

    private String getLdapServerUrl() {
        return "ldap://" + this.host + ":" + this.port;
    }

    private DirContext connectLDAP() throws NamingException {
        Properties properties = new Properties();
        properties.setProperty("java.naming.factory.initial", LDAP_PROVIDER);
        properties.setProperty("java.naming.batchsize", "0");
        properties.setProperty("java.naming.provider.url", this.getLdapServerUrl());
        properties.setProperty("java.naming.factory.url.pkgs", URL_CONTEXT_PREFIX);
        properties.setProperty("java.naming.referral", REFERRALS_IGNORE);
        properties.setProperty("java.naming.security.authentication", SEARCH_SECURITY_LEVEL);
        InitialDirContext initialDirContext = new InitialDirContext(properties);
        return initialDirContext;
    }

    private String searchDnObject(DirContext dirContext, String string, String string2) throws NamingException {
        String string3 = "objectClass=*";
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(0);
        searchControls.setCountLimit(0L);
        String[] stringArray = new String[]{string2};
        searchControls.setReturningAttributes(stringArray);
        String string4 = "(&(" + string3 + ")(" + stringArray[0] + "=*))";
        NamingEnumeration<SearchResult> namingEnumeration = dirContext.search(string, string4, searchControls);
        while (namingEnumeration.hasMoreElements()) {
            SearchResult searchResult = namingEnumeration.next();
            NamingEnumeration<?> namingEnumeration2 = searchResult.getAttributes().getAll().next().getAll();
            if (!namingEnumeration2.hasMore()) continue;
            Object obj = namingEnumeration2.next();
            return obj.toString();
        }
        return null;
    }

    private String searchKey(DirContext dirContext, String string, String string2, String string3) throws Exception {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(2);
        searchControls.setCountLimit(0L);
        String[] stringArray = new String[]{string3};
        searchControls.setReturningAttributes(stringArray);
        String string4 = "(&(" + string2 + ")(" + stringArray[0] + "=*))";
        NamingEnumeration<SearchResult> namingEnumeration = dirContext.search(string, string4, searchControls);
        while (namingEnumeration.hasMoreElements()) {
            SearchResult searchResult = namingEnumeration.next();
            NamingEnumeration<?> namingEnumeration2 = searchResult.getAttributes().getAll().next().getAll();
            if (!namingEnumeration2.hasMore()) continue;
            Object obj = namingEnumeration2.next();
            return obj.toString();
        }
        return null;
    }

    private List<String> searchMultipleKeys(DirContext dirContext, String string, String string2, String string3) throws Exception {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(2);
        searchControls.setCountLimit(0L);
        String[] stringArray = new String[]{string3};
        searchControls.setReturningAttributes(stringArray);
        String string4 = "(&(" + string2 + ")(" + stringArray[0] + "=*))";
        NamingEnumeration<SearchResult> namingEnumeration = dirContext.search(string, string4, searchControls);
        LinkedList<String> linkedList = new LinkedList<String>();
        while (namingEnumeration.hasMoreElements()) {
            SearchResult searchResult = namingEnumeration.next();
            NamingEnumeration<?> namingEnumeration2 = searchResult.getAttributes().getAll().next().getAll();
            if (!namingEnumeration2.hasMore()) continue;
            Object obj = namingEnumeration2.next();
            linkedList.add(obj.toString());
        }
        return linkedList;
    }

    private String getRootDN(DirContext dirContext) throws IOException {
        try {
            Attributes attributes = dirContext.getAttributes("", new String[]{"namingContexts"});
            Attribute attribute = attributes.get("namingContexts");
            NamingEnumeration<?> namingEnumeration = attribute.getAll();
            String string = "";
            if (namingEnumeration.hasMore()) {
                String string2 = (String)namingEnumeration.next();
                return string2;
            }
        }
        catch (Exception exception) {
            if (exception instanceof IOException) {
                throw (IOException)exception;
            }
            throw new IOException("Error getting results from LDAP directory " + exception);
        }
        return null;
    }

    private String getKeysDn(DirContext dirContext) throws Exception {
        String string = this.getRootDN(dirContext);
        String string2 = "";
        try {
            string2 = this.searchDnObject(dirContext, "cn=pgpServerInfo," + string, "pgpBaseKeySpaceDN");
        }
        catch (NamingException namingException) {
            try {
                string2 = this.searchDnObject(dirContext, "cn=pgpServerInfo", "pgpBaseKeySpaceDN");
            }
            catch (NamingException namingException2) {
                string2 = "";
            }
        }
        if (string2 == "") {
            Attributes attributes = dirContext.getAttributes("", new String[]{"namingContexts"});
            Attribute attribute = attributes.get("namingContexts");
            NamingEnumeration<?> namingEnumeration = attribute.getAll();
            while (namingEnumeration.hasMore()) {
                String string3 = (String)namingEnumeration.next();
                try {
                    string2 = this.searchDnObject(dirContext, "cn=pgpServerInfo" + string3, "pgpBaseKeySpaceDN");
                }
                catch (NamingException namingException) {}
            }
        }
        return string2;
    }

    public byte[] getKeyByKeyIdHex(String string) throws IOException {
        if (!BaseLib.isHexId(string)) {
            throw new IllegalArgumentException("Parameter keyIdHex is not a hexadecimal Key Id : " + string);
        }
        byte[] byArray = new byte[]{};
        DirContext dirContext = null;
        try {
            dirContext = this.connectLDAP();
            String string2 = this.getKeysDn(dirContext);
            String[] stringArray = new String[]{"pgpKey"};
            for (int i = 0; i < stringArray.length; ++i) {
                String string3 = this.searchKey(dirContext, string2, "pgpKeyID=" + string, stringArray[i]);
                if (string3 == null) continue;
                byArray = string3.getBytes("ASCII");
            }
        }
        catch (Exception exception) {
            if (exception instanceof IOException) {
                throw (IOException)exception;
            }
            throw new IOException("Error getting results from LDAP directory " + exception);
        }
        finally {
            try {
                if (null != dirContext) {
                    dirContext.close();
                }
            }
            catch (Exception exception) {}
        }
        return byArray;
    }

    public KeyPairInformation getSingleKeyByKeyIdHex(String string) throws IOException {
        if (!BaseLib.isHexId(string)) {
            throw new IllegalArgumentException("Parameter keyIdHex is not a hexadecimal Key Id : " + string);
        }
        byte[] byArray = new byte[]{};
        DirContext dirContext = null;
        try {
            dirContext = this.connectLDAP();
            String string2 = this.getKeysDn(dirContext);
            String string3 = "pgpKey";
            List<String> list = this.searchMultipleKeys(dirContext, string2, "pgpKeyID=" + string, string3);
            if (list.size() > 0) {
                KeyPairInformation keyPairInformation = new KeyPairInformation(list.get(0).getBytes());
                return keyPairInformation;
            }
        }
        catch (Exception exception) {
            if (exception instanceof IOException) {
                throw (IOException)exception;
            }
            throw new IOException("Error getting results from LDAP directory " + exception);
        }
        finally {
            try {
                if (null != dirContext) {
                    dirContext.close();
                }
            }
            catch (Exception exception) {}
        }
        return null;
    }

    public byte[] getKeyByUserId(String string) throws IOException {
        byte[] byArray = new byte[]{};
        DirContext dirContext = null;
        try {
            dirContext = this.connectLDAP();
            String string2 = this.getKeysDn(dirContext);
            String[] stringArray = new String[]{"pgpKey"};
            for (int i = 0; i < stringArray.length; ++i) {
                String string3 = this.partialMatch ? this.searchKey(dirContext, string2, "pgpUserID=*" + string + "*", stringArray[i]) : this.searchKey(dirContext, string2, "pgpUserID=" + string, stringArray[i]);
                if (string3 == null) continue;
                byArray = string3.getBytes("ASCII");
            }
        }
        catch (Exception exception) {
            if (exception instanceof IOException) {
                throw (IOException)exception;
            }
            throw new IOException("Error getting results from LDAP directory " + exception);
        }
        finally {
            try {
                if (null != dirContext) {
                    dirContext.close();
                }
            }
            catch (Exception exception) {}
        }
        return byArray;
    }

    public KeyPairInformation getSingleKeyByUserId(String string) throws IOException {
        DirContext dirContext = null;
        try {
            List<String> list;
            dirContext = this.connectLDAP();
            String string2 = this.getKeysDn(dirContext);
            String string3 = "pgpKey";
            if (this.partialMatch) {
                if (string == null || string.trim().length() == 0) {
                    string = "*";
                } else if (!string.contains("*")) {
                    string = "*" + string + "*";
                }
            }
            if ((list = this.searchMultipleKeys(dirContext, string2, "pgpUserID=" + string, string3)).size() > 0) {
                KeyPairInformation keyPairInformation = new KeyPairInformation(list.get(0).getBytes());
                return keyPairInformation;
            }
            KeyPairInformation keyPairInformation = null;
            return keyPairInformation;
        }
        catch (Exception exception) {
            if (exception instanceof IOException) {
                throw (IOException)exception;
            }
            throw new IOException("Error getting results from LDAP directory " + exception);
        }
        finally {
            try {
                if (null != dirContext) {
                    dirContext.close();
                }
            }
            catch (Exception exception) {}
        }
    }

    public KeyPairInformation[] getMultipleKeysByUserId(String string) throws IOException {
        DirContext dirContext = null;
        try {
            dirContext = this.connectLDAP();
            String string2 = this.getKeysDn(dirContext);
            List<String> list = null;
            String[] stringArray = new String[]{"pgpKey"};
            for (int i = 0; i < stringArray.length; ++i) {
                if (this.partialMatch) {
                    if (string == null || string.trim().length() == 0) {
                        string = "*";
                    } else if (!string.contains("*")) {
                        string = "*" + string + "*";
                    }
                }
                list = this.searchMultipleKeys(dirContext, string2, "pgpUserID=" + string, stringArray[i]);
            }
            LinkedList<KeyPairInformation> linkedList = new LinkedList<KeyPairInformation>();
            for (int i = 0; i < list.size(); ++i) {
                linkedList.add(new KeyPairInformation(((String)list.get(i)).getBytes()));
            }
            KeyPairInformation[] keyPairInformationArray = linkedList.toArray(new KeyPairInformation[0]);
            return keyPairInformationArray;
        }
        catch (Exception exception) {
            if (exception instanceof IOException) {
                throw (IOException)exception;
            }
            throw new IOException("Error getting results from LDAP directory " + exception);
        }
        finally {
            try {
                if (null != dirContext) {
                    dirContext.close();
                }
            }
            catch (Exception exception) {}
        }
    }

    public byte[] getKeyByKeyId(long l) throws IOException {
        String string = Long.toHexString(l).toUpperCase();
        return this.getKeyByKeyIdHex(string.substring(string.length() - 8));
    }

    public boolean submitKey(byte[] byArray) throws IOException {
        DirContext dirContext = null;
        try {
            KeyPairInformation.SubKey[] subKeyArray;
            int n;
            dirContext = this.connectLDAP();
            String string = this.getKeysDn(dirContext);
            this.helperKS.purge();
            KeyPairInformation[] keyPairInformationArray = this.helperKS.importPublicKey(new ByteArrayInputStream(byArray));
            KeyPairInformation keyPairInformation = keyPairInformationArray[0];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            this.helperKS.exportPublicKey((OutputStream)byteArrayOutputStream, keyPairInformation.getKeyID(), true);
            String string2 = Long.toHexString(keyPairInformation.getKeyID()).toUpperCase();
            for (n = 0; n < 16 - string2.length(); ++n) {
                string2 = "0" + string2;
            }
            n = 0;
            String[] stringArray = new String[]{"pgpKeyID"};
            for (int i = 0; i < stringArray.length; ++i) {
                subKeyArray = this.searchKey(dirContext, string, "pgpCertID=" + string2, stringArray[i]);
                n = subKeyArray != null ? 1 : 0;
            }
            String string3 = "pgpCertID=" + string2 + "," + string;
            subKeyArray = keyPairInformation.getPublicSubKeys();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
            String string4 = simpleDateFormat.format(keyPairInformation.getCreationTime());
            if (n != 0) {
                ModificationItem[] modificationItemArray = new ModificationItem[]{new ModificationItem(2, new BasicAttribute("pgpDisabled", false)), new ModificationItem(2, new BasicAttribute("pgpKeyID", null)), new ModificationItem(2, new BasicAttribute("pgpKeyType", null)), new ModificationItem(2, new BasicAttribute("pgpUserID", null)), new ModificationItem(2, new BasicAttribute("pgpKeyCreateTime", null)), new ModificationItem(2, new BasicAttribute("pgpSignerID", null)), new ModificationItem(2, new BasicAttribute("pgpRevoked", null)), new ModificationItem(2, new BasicAttribute("pgpSubKeyID", null)), new ModificationItem(2, new BasicAttribute("pgpKeySize", null)), new ModificationItem(2, new BasicAttribute("pgpKeyExpireTime", null)), new ModificationItem(2, new BasicAttribute("pgpCertID", null)), new ModificationItem(2, new BasicAttribute("pgpCertID", string2)), new ModificationItem(2, new BasicAttribute("pgpKeyID", keyPairInformation.getKeyIDHex())), new ModificationItem(2, new BasicAttribute("pgpKeyType", keyPairInformation.getAlgorithm())), new ModificationItem(2, new BasicAttribute("pgpUserID", keyPairInformation.getUserID())), new ModificationItem(2, new BasicAttribute("pgpKeyCreateTime", string4)), new ModificationItem(2, new BasicAttribute("pgpSignerID", string2)), new ModificationItem(2, new BasicAttribute("pgpRevoked", keyPairInformation.isRevoked() ? "1" : "0")), subKeyArray.length > 0 ? new ModificationItem(2, new BasicAttribute("pgpSubKeyID", Long.toHexString(subKeyArray[0].getKeyID()).toUpperCase())) : new ModificationItem(2, new BasicAttribute("pgpSubKeyID", new Long(0L))), new ModificationItem(2, new BasicAttribute("pgpKeySize", LDAPClient.padLeft(keyPairInformation.getKeySize(), 5))), new ModificationItem(2, new BasicAttribute("pgpDisabled", "0")), new ModificationItem(2, new BasicAttribute("objectClass", "pgpKeyInfo")), new ModificationItem(2, new BasicAttribute("pgpKey", byteArrayOutputStream.toByteArray()))};
                dirContext.modifyAttributes(string3, modificationItemArray);
            } else {
                BasicAttributes basicAttributes = new BasicAttributes(true);
                basicAttributes.put("pgpCertID", string2);
                basicAttributes.put("pgpKeyID", keyPairInformation.getKeyIDHex());
                basicAttributes.put("pgpKeyType", keyPairInformation.getAlgorithm());
                basicAttributes.put("pgpUserID", keyPairInformation.getUserID());
                basicAttributes.put("pgpKeyCreateTime", string4);
                basicAttributes.put("pgpSignerID", string2);
                basicAttributes.put("pgpRevoked", keyPairInformation.isRevoked() ? "1" : "0");
                if (subKeyArray.length > 0) {
                    basicAttributes.put("pgpSubKeyID", Long.toHexString(subKeyArray[0].getKeyID()).toUpperCase());
                }
                basicAttributes.put("pgpDisabled", "0");
                basicAttributes.put("pgpCertID", string2);
                basicAttributes.put("pgpKeyID", keyPairInformation.getKeyIDHex());
                basicAttributes.put("pgpKeyType", keyPairInformation.getAlgorithm());
                basicAttributes.put("pgpUserID", keyPairInformation.getUserID() + '\u0000');
                basicAttributes.put("pgpKeyCreateTime", string4);
                basicAttributes.put("pgpSignerID", string2);
                basicAttributes.put("pgpRevoked", keyPairInformation.isRevoked() ? "1" : "0");
                if (subKeyArray.length > 0) {
                    basicAttributes.put("pgpSubKeyID", Long.toHexString(subKeyArray[0].getKeyID()).toUpperCase());
                }
                basicAttributes.put("pgpKeySize", LDAPClient.padLeft(keyPairInformation.getKeySize(), 5));
                basicAttributes.put("pgpDisabled", "0");
                basicAttributes.put("objectClass", "pgpKeyInfo");
                basicAttributes.put("pgpKey", byteArrayOutputStream.toByteArray());
                dirContext.createSubcontext(string3, (Attributes)basicAttributes);
            }
        }
        catch (Exception exception) {
            throw new IOException("Error uploading OpenPGP key to LDAP directory: " + exception);
        }
        finally {
            try {
                if (null != dirContext) {
                    dirContext.close();
                }
            }
            catch (Exception exception) {}
        }
        return true;
    }

    private static String padLeft(int n, int n2) {
        return String.format("%1$" + n2 + "d", n);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private byte[] paddAesKey(byte[] byArray) {
        byte[] byArray2 = new byte[]{};
        try {
            SHA256Digest sHA256Digest = new SHA256Digest();
            sHA256Digest.update(byArray, 0, byArray.length);
            byArray2 = sHA256Digest.getEncodedState();
            byte[] byArray3 = Arrays.copyOf(byArray2, 16);
            return byArray3;
        }
        finally {
            Arrays.fill(byArray2, (byte)0);
        }
    }
}

