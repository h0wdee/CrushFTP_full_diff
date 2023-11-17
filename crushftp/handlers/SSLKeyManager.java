/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bouncycastle.asn1.ASN1Encodable
 *  org.bouncycastle.asn1.ASN1ObjectIdentifier
 *  org.bouncycastle.asn1.ASN1Sequence
 *  org.bouncycastle.asn1.DEROctetString
 *  org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
 *  org.bouncycastle.asn1.x500.X500Name
 *  org.bouncycastle.asn1.x509.AlgorithmIdentifier
 *  org.bouncycastle.asn1.x509.BasicConstraints
 *  org.bouncycastle.asn1.x509.Extension
 *  org.bouncycastle.asn1.x509.ExtensionsGenerator
 *  org.bouncycastle.asn1.x509.GeneralName
 *  org.bouncycastle.asn1.x509.GeneralNames
 *  org.bouncycastle.asn1.x509.KeyUsage
 *  org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
 *  org.bouncycastle.cert.X509v3CertificateBuilder
 *  org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
 *  org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
 *  org.bouncycastle.crypto.util.PrivateKeyFactory
 *  org.bouncycastle.jce.provider.BouncyCastleProvider
 *  org.bouncycastle.openssl.PEMEncryptedKeyPair
 *  org.bouncycastle.openssl.PEMKeyPair
 *  org.bouncycastle.openssl.PEMParser
 *  org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
 *  org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder
 *  org.bouncycastle.operator.ContentSigner
 *  org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder
 *  org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder
 *  org.bouncycastle.operator.bc.BcRSAContentSignerBuilder
 *  org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
 *  org.bouncycastle.pkcs.PKCS10CertificationRequest
 *  org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
 */
package crushftp.handlers;

import com.crushftp.client.Base64;
import com.crushftp.client.File_S;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.server.ServerStatus;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

public class SSLKeyManager {
    public static void main(String[] args) {
        try {
            Security.addProvider((Provider)new BouncyCastleProvider());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String loadKeyStoreToMemory(String url) {
        if (url.equals("builtin")) {
            return url;
        }
        try {
            VRL vrl = new VRL(url);
            GenericClient c = Common.getClient(Common.getBaseUrl(vrl.toString()), "SSL Key store load", new Vector());
            if (ServerStatus.BG("v10_beta") && vrl.getConfig() != null && vrl.getConfig().size() > 0) {
                c.setConfigObj(vrl.getConfig());
            }
            c.login(vrl.getUsername(), vrl.getPassword(), null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            com.crushftp.client.Common.streamCopier(null, null, c.download(vrl.getPath(), 0L, -1L, true), baos, false, true, true);
            Properties p2 = new Properties();
            p2.put("bytes", baos.toByteArray());
            com.crushftp.client.Common.System2.put("crushftp.keystores." + url.toUpperCase().replace('\\', '/'), p2);
        }
        catch (Exception e) {
            Log.log("SERVER", 1, e);
        }
        return url;
    }

    public static String importReply(String keystore_path, String keystore_pass, String key_pass, String import_path, String trusted_paths) throws Exception {
        String result = "";
        Vector<X509Certificate> trusted_certs_used = new Vector<X509Certificate>();
        Vector<Object> trusted_certificates = new Vector<Object>();
        X509Certificate import_cert = null;
        String private_alias = null;
        String keystore_format = keystore_path.toUpperCase().trim().indexOf("PFX") >= 0 || keystore_path.toUpperCase().trim().indexOf("PKCS12") >= 0 ? "PKCS12" : "JKS";
        KeyStore jks = KeyStore.getInstance(keystore_format);
        jks.load(new FileInputStream(new File_S(keystore_path)), keystore_pass.toCharArray());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        FileInputStream cert_in = new FileInputStream(new File_S(import_path));
        Collection<? extends Certificate> c = cf.generateCertificates(cert_in);
        cert_in.close();
        import_cert = (X509Certificate)c.toArray()[0];
        int x = 1;
        while (x < c.size()) {
            trusted_certificates.addElement(c.toArray()[x]);
            ++x;
        }
        trusted_certs_used.addElement(import_cert);
        String[] trusted_items = trusted_paths.split(";");
        int x2 = 0;
        while (x2 < trusted_items.length) {
            if (!trusted_items[x2].trim().equals("")) {
                try {
                    cert_in = new FileInputStream(new File_S(trusted_items[x2].trim()));
                    c = cf.generateCertificates(cert_in);
                    cert_in.close();
                    int xx = 0;
                    while (xx < c.size()) {
                        trusted_certificates.addElement(c.toArray()[xx]);
                        ++xx;
                    }
                }
                catch (IOException e) {
                    result = String.valueOf(result) + "ERROR:" + trusted_items[x2].trim() + ":" + e + "\r\n";
                }
            }
            ++x2;
        }
        Properties trusted_user_certs = new Properties();
        Enumeration<String> keys = jks.aliases();
        while (keys.hasMoreElements()) {
            String alias = keys.nextElement().toString();
            if (jks.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
                private_alias = alias;
                continue;
            }
            if (!jks.entryInstanceOf(alias, KeyStore.TrustedCertificateEntry.class)) continue;
            trusted_user_certs.put(alias, jks.getCertificate(alias));
            trusted_certificates.addElement(jks.getCertificate(alias));
        }
        KeyStore cacerts = KeyStore.getInstance("JKS");
        cacerts.load(new FileInputStream(String.valueOf(System.getProperty("java.home")) + "/lib/security/cacerts"), "changeit".toCharArray());
        keys = cacerts.aliases();
        while (keys.hasMoreElements()) {
            String alias = keys.nextElement().toString();
            if (!cacerts.entryInstanceOf(alias, KeyStore.TrustedCertificateEntry.class)) continue;
            trusted_certificates.addElement(cacerts.getCertificate(alias));
        }
        X509Certificate private_cert = (X509Certificate)jks.getCertificate(private_alias);
        if (new String(Base64.encodeBytes(private_cert.getPublicKey().getEncoded())).equals(new String(Base64.encodeBytes(import_cert.getPublicKey().getEncoded())))) {
            int x3;
            int i = 0;
            block7: while (i < trusted_certs_used.size()) {
                X509Certificate cert = (X509Certificate)trusted_certs_used.elementAt(i);
                ++i;
                x3 = 0;
                while (x3 < trusted_certificates.size()) {
                    X509Certificate cert2 = (X509Certificate)trusted_certificates.elementAt(x3);
                    if (SSLKeyManager.findCN(cert2.getSubjectX500Principal().getName()).equalsIgnoreCase(SSLKeyManager.findCN(cert.getIssuerX500Principal().getName()))) {
                        if (SSLKeyManager.findCN(cert2.getSubjectX500Principal().getName()).equalsIgnoreCase(SSLKeyManager.findCN(cert.getSubjectX500Principal().getName()))) continue block7;
                        trusted_certs_used.addElement(cert2);
                        continue block7;
                    }
                    ++x3;
                }
            }
            Certificate[] certs2 = new X509Certificate[trusted_certs_used.size()];
            x3 = 0;
            while (x3 < trusted_certs_used.size()) {
                certs2[x3] = (X509Certificate)trusted_certs_used.elementAt(x3);
                ++x3;
            }
            jks.setKeyEntry(private_alias, jks.getKey(private_alias, key_pass.toCharArray()), key_pass.toCharArray(), certs2);
            Enumeration<Object> keys2 = trusted_user_certs.keys();
            while (keys2.hasMoreElements()) {
                String key = keys2.nextElement().toString();
                jks.setCertificateEntry(key, (Certificate)trusted_user_certs.get(key));
            }
            jks.store(new FileOutputStream(new File_S(keystore_path)), key_pass.toCharArray());
            return "SUCCESS:" + result;
        }
        return "ERROR:The private key (" + SSLKeyManager.findCN(private_cert.getSubjectX500Principal().getName()) + ", SERIALNUMBER=" + private_cert.getSerialNumber().toString(16) + ") does not match import reply key (" + SSLKeyManager.findCN(import_cert.getSubjectX500Principal().getName()) + ", SERIALNUMBER=" + import_cert.getSerialNumber().toString(16) + ").";
    }

    public static String findCN(String name) {
        if ((name = name.toUpperCase()).indexOf("CN=") < 0) {
            return name;
        }
        if ((name = name.substring(name.indexOf("CN="))).indexOf(",") >= 0) {
            name = name.substring(0, name.indexOf(","));
        }
        return name.trim();
    }

    public static String buildNew(String key_alg, int key_size, String sig_alg, int days, String cn, String ou, String o, String l, String st, String c, String e, String keystore_path, String keystore_pass, String key_pass, String sans) throws Exception {
        if (c.equalsIgnoreCase("UK")) {
            c = "GB";
        }
        if (new File_S(keystore_path).exists()) {
            throw new IOException("Cannot overwrite an existing keystore file.");
        }
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(key_alg);
        keyGen.initialize(key_size, SecureRandom.getInstance("SHA1PRNG"));
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey priv = pair.getPrivate();
        PublicKey pub = pair.getPublic();
        X509Certificate new_cert = SSLKeyManager.generateCert(cn, ou, o, l, st, c, e, days, pub, priv, sig_alg, sans);
        KeyStore jks = KeyStore.getInstance("JKS");
        jks.load(null, keystore_pass.toCharArray());
        Certificate[] certs = new Certificate[]{new_cert};
        jks.setKeyEntry(cn, priv, key_pass.toCharArray(), certs);
        jks.store(new FileOutputStream(new File_S(keystore_path)), keystore_pass.toCharArray());
        return "SUCCESS:Created";
    }

    public static String makeCSR(String keystore_path, String keystore_pass, String key_pass) throws Exception {
        String private_alias = null;
        String keystore_format = keystore_path.toUpperCase().trim().indexOf("PFX") >= 0 || keystore_path.toUpperCase().trim().indexOf("PKCS12") >= 0 ? "PKCS12" : "JKS";
        KeyStore jks = KeyStore.getInstance(keystore_format);
        jks.load(new FileInputStream(new File_S(keystore_path)), keystore_pass.toCharArray());
        Enumeration<String> keys = jks.aliases();
        while (keys.hasMoreElements()) {
            String alias = keys.nextElement().toString();
            if (!jks.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) continue;
            private_alias = alias;
        }
        X509Certificate private_cert = (X509Certificate)((KeyStore.PrivateKeyEntry)jks.getEntry(private_alias, new KeyStore.PasswordProtection(key_pass.toCharArray()))).getCertificate();
        AlgorithmIdentifier signatureAlgorithm = new DefaultSignatureAlgorithmIdentifierFinder().find(private_cert.getSigAlgName());
        AlgorithmIdentifier digestAlgorithm = new DefaultDigestAlgorithmIdentifierFinder().find("SHA-256");
        ContentSigner signer = new BcRSAContentSignerBuilder(signatureAlgorithm, digestAlgorithm).build(PrivateKeyFactory.createKey((byte[])jks.getKey(private_alias, key_pass.toCharArray()).getEncoded()));
        JcaPKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(new X500Name(private_cert.getSubjectDN().getName()), private_cert.getPublicKey());
        ExtensionsGenerator extensionsGenerator = new ExtensionsGenerator();
        extensionsGenerator.addExtension(Extension.basicConstraints, true, (ASN1Encodable)new BasicConstraints(true));
        extensionsGenerator.addExtension(Extension.keyUsage, true, (ASN1Encodable)new KeyUsage(6));
        GeneralName[] sans_array = null;
        Collection<List<?>> col = private_cert.getSubjectAlternativeNames();
        if (col != null && col.toArray() != null && col.toArray().length > 0) {
            Object[] a = col.toArray();
            sans_array = new GeneralName[a.length];
            int x = 0;
            while (x < a.length) {
                String host = ("" + ((List)a[x]).get(1)).trim();
                boolean ip_only = true;
                int xx = 0;
                while (xx < host.length() && ip_only) {
                    if (host.charAt(xx) >= ':') {
                        ip_only = false;
                    }
                    ++xx;
                }
                sans_array[x] = ip_only ? new GeneralName(7, host) : new GeneralName(2, host);
                ++x;
            }
        }
        if (sans_array != null) {
            extensionsGenerator.addExtension(Extension.subjectAlternativeName, false, (ASN1Encodable)new GeneralNames(sans_array));
        }
        csrBuilder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, (ASN1Encodable)extensionsGenerator.generate());
        PKCS10CertificationRequest csr = csrBuilder.build(signer);
        String csr64 = "-----BEGIN CERTIFICATE REQUEST-----\r\n";
        String csr_encoded = new String(Base64.encodeBytes(csr.getEncoded()));
        int loc = 0;
        while (loc < csr_encoded.length()) {
            csr64 = csr_encoded.length() - loc > 64 ? String.valueOf(csr64) + csr_encoded.substring(loc, loc + 64) + "\r\n" : String.valueOf(csr64) + csr_encoded.substring(loc) + "\r\n";
            loc += 64;
        }
        csr64 = String.valueOf(csr64) + "-----END CERTIFICATE REQUEST-----\r\n";
        return csr64;
    }

    public static X509Certificate generateCert(String sCommonName, String sOrganisationUnit, String sOrganisation, String sLocality, String sState, String sCountryCode, String sEmailAddress, int iValidity, PublicKey publicKey, PrivateKey privateKey, String sig_alg, String sans) throws Exception {
        String cn = "";
        cn = String.valueOf(cn) + "  OU=\"" + sOrganisationUnit + "\"";
        cn = String.valueOf(cn) + ", O=\"" + sOrganisation + "\"";
        cn = String.valueOf(cn) + ", L=\"" + sLocality + "\"";
        cn = String.valueOf(cn) + ", ST=" + sState;
        cn = String.valueOf(cn) + ", C=" + sCountryCode;
        cn = String.valueOf(cn) + ", EMAILADDRESS=" + sEmailAddress;
        cn = String.valueOf(cn) + ", CN=" + sCommonName;
        X509v3CertificateBuilder certbuild = new X509v3CertificateBuilder(new X500Name(cn), new BigInteger(Long.toString(System.currentTimeMillis() / 1000L)), new Date(), new Date(System.currentTimeMillis() + (long)iValidity * 24L * 60L * 60L * 1000L), new X500Name(cn), new SubjectPublicKeyInfo(ASN1Sequence.getInstance((Object)publicKey.getEncoded())));
        GeneralName[] sans_array = null;
        if (sans != null && !sans.equals("")) {
            sans_array = new GeneralName[sans.split(",").length];
            int x = 0;
            while (x < sans.split(",").length) {
                String host = sans.split(",")[x].trim();
                boolean ip_only = true;
                int xx = 0;
                while (xx < host.length() && ip_only) {
                    if (host.charAt(xx) >= ':') {
                        ip_only = false;
                    }
                    ++xx;
                }
                sans_array[x] = ip_only ? new GeneralName(7, host) : new GeneralName(2, host);
                ++x;
            }
        }
        if (sans_array != null) {
            certbuild.addExtension(Extension.subjectAlternativeName, false, (ASN1Encodable)new GeneralNames(sans_array));
        }
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certbuild.build(new JcaContentSignerBuilder(sig_alg).setProvider("BC").build(privateKey)));
    }

    public static Vector list(String keystore_path, String keystore_pass) throws Exception {
        Vector<Properties> v = new Vector<Properties>();
        String keystore_format = keystore_path.toUpperCase().trim().indexOf("PFX") >= 0 || keystore_path.toUpperCase().trim().indexOf("PKCS12") >= 0 ? "PKCS12" : "JKS";
        KeyStore jks = KeyStore.getInstance(keystore_format);
        if (com.crushftp.client.Common.System2.containsKey("crushftp.keystores." + keystore_path.toUpperCase().replace('\\', '/'))) {
            Properties p = (Properties)com.crushftp.client.Common.System2.get("crushftp.keystores." + keystore_path.toUpperCase().replace('\\', '/'));
            jks.load(new ByteArrayInputStream((byte[])p.get("bytes")), keystore_pass.toCharArray());
        } else if (!new File_S(keystore_path).exists()) {
            jks.load(null, keystore_pass.toCharArray());
            FileOutputStream out = new FileOutputStream(new File_S(keystore_path));
            jks.store(out, keystore_pass.toCharArray());
            out.close();
        } else {
            jks.load(new FileInputStream(new File_S(keystore_path)), keystore_pass.toCharArray());
        }
        Enumeration<String> keys = jks.aliases();
        while (keys.hasMoreElements()) {
            String alias = keys.nextElement().toString();
            Certificate entry = jks.getCertificate(alias);
            Properties p = new Properties();
            p.put("alias", alias);
            p.put("private", "false");
            p.put("public", "false");
            if (jks.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
                p.put("private", "true");
            }
            if (jks.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
                p.put("public", "true");
            }
            p.put("type", entry.getType());
            p.put("format", entry.getPublicKey().getFormat());
            p.put("algorithm", entry.getPublicKey().getAlgorithm());
            if (entry instanceof X509Certificate) {
                X509Certificate x509 = (X509Certificate)entry;
                p.put("sigAlg", x509.getSigAlgName());
                p.put("issuerDN", x509.getIssuerDN().toString());
                p.put("expires", String.valueOf(x509.getNotAfter().getTime()));
                p.put("subjectDN", x509.getSubjectDN().toString());
                p.put("version", String.valueOf(x509.getVersion()));
                p.put("serial", x509.getSerialNumber().toString());
                Collection<List<?>> col = x509.getSubjectAlternativeNames();
                if (col != null) {
                    Object[] o = col.toArray();
                    int x = 0;
                    while (x < o.length) {
                        p.put("subjectDN" + (x + 1), o[x].toString());
                        ++x;
                    }
                }
            }
            v.addElement(p);
        }
        return v;
    }

    public static boolean delete(String keystore_path, String keystore_pass, String alias) throws Exception {
        String keystore_format = keystore_path.toUpperCase().trim().indexOf("PFX") >= 0 || keystore_path.toUpperCase().trim().indexOf("PKCS12") >= 0 ? "PKCS12" : "JKS";
        KeyStore jks = KeyStore.getInstance(keystore_format);
        jks.load(new FileInputStream(new File_S(keystore_path)), keystore_pass.toCharArray());
        jks.deleteEntry(alias);
        FileOutputStream out = new FileOutputStream(new File_S(keystore_path));
        jks.store(out, keystore_pass.toCharArray());
        out.close();
        return true;
    }

    public static boolean rename(String keystore_path, String keystore_pass, String alias1, String alias2) throws Exception {
        String keystore_format = keystore_path.toUpperCase().trim().indexOf("PFX") >= 0 || keystore_path.toUpperCase().trim().indexOf("PKCS12") >= 0 ? "PKCS12" : "JKS";
        KeyStore jks = KeyStore.getInstance(keystore_format);
        jks.load(new FileInputStream(new File_S(keystore_path)), keystore_pass.toCharArray());
        KeyStore.Entry entry = jks.getEntry(alias1, null);
        jks.setEntry(alias2, entry, null);
        jks.deleteEntry(alias1);
        FileOutputStream out = new FileOutputStream(new File_S(keystore_path));
        jks.store(out, keystore_pass.toCharArray());
        out.close();
        return true;
    }

    public static String export(String keystore_path, String keystore_pass, String alias) throws Exception {
        String keystore_format = keystore_path.toUpperCase().trim().indexOf("PFX") >= 0 || keystore_path.toUpperCase().trim().indexOf("PKCS12") >= 0 ? "PKCS12" : "JKS";
        KeyStore jks = KeyStore.getInstance(keystore_format);
        jks.load(new FileInputStream(new File_S(keystore_path)), keystore_pass.toCharArray());
        Key key = jks.getKey(alias, keystore_pass.toCharArray());
        if (key == null) {
            throw new Exception("No such alias in keystore.");
        }
        String s = "";
        if (key instanceof PrivateKey) {
            s = String.valueOf(s) + "-----BEGIN PRIVATE KEY-----\r\n" + Base64.encodeBytes(key.getEncoded()) + "\r\n-----END PRIVATE KEY-----";
        }
        if (jks.getCertificate(alias) != null) {
            s = String.valueOf(s) + "\r\n-----BEGIN PUBLIC KEY-----\r\n" + Base64.encodeBytes(jks.getCertificate(alias).getPublicKey().getEncoded()) + "\r\n-----END PUBLIC KEY-----";
        }
        return s.trim();
    }

    public static void addPrivate(String keystore_path, String keystore_pass, String alias, String key_path, String key_pass) throws Exception {
        String keystore_format = keystore_path.toUpperCase().trim().indexOf("PFX") >= 0 || keystore_path.toUpperCase().trim().indexOf("PKCS12") >= 0 ? "PKCS12" : "JKS";
        KeyStore jks = KeyStore.getInstance(keystore_format);
        jks.load(new FileInputStream(new File_S(keystore_path)), keystore_pass.toCharArray());
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        com.crushftp.client.Common.streamCopier(new FileInputStream(new File_S(key_path)), baos1, false);
        String keydata = new String(baos1.toByteArray());
        String other_keys = "";
        if (keydata.indexOf("-----BEGIN RSA PRIVATE KEY-----") < 0) {
            baos1 = new ByteArrayOutputStream();
            com.crushftp.client.Common.streamCopier(new FileInputStream(new File_S(String.valueOf(key_path) + ".pem")), baos1, false);
            other_keys = new String(baos1.toByteArray());
        } else {
            other_keys = String.valueOf(keydata.substring(0, keydata.indexOf("-----BEGIN RSA PRIVATE KEY-----"))) + keydata.substring(keydata.indexOf("-----END RSA PRIVATE KEY-----") + "-----END RSA PRIVATE KEY-----".length()).trim();
        }
        PEMParser pp = new PEMParser((Reader)new BufferedReader(new StringReader(keydata)));
        Object o = pp.readObject();
        if (o instanceof PEMEncryptedKeyPair) {
            o = ((PEMEncryptedKeyPair)o).decryptKeyPair(new JcePEMDecryptorProviderBuilder().build(key_pass.toCharArray()));
        }
        PEMKeyPair pemKeyPair = (PEMKeyPair)o;
        KeyPair kp = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
        pp.close();
        PrivateKey priv = kp.getPrivate();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> c = cf.generateCertificates(new ByteArrayInputStream(other_keys.getBytes()));
        Certificate[] certs = new Certificate[1];
        if (c.size() == 1) {
            Certificate cert;
            certs[0] = cert = cf.generateCertificate(new ByteArrayInputStream(other_keys.getBytes()));
        } else {
            certs = new Certificate[c.toArray().length];
            int x = 0;
            while (x < certs.length) {
                certs[x] = (Certificate)c.toArray()[x];
                ++x;
            }
        }
        jks.setKeyEntry(alias, priv, key_pass.toCharArray(), certs);
        int x = 0;
        while (x < certs.length) {
            String s = "";
            try {
                s = ((X509Certificate)certs[x]).getSubjectDN().getName();
                s = s.substring(s.toUpperCase().indexOf("CN=") + 3);
                s = s.substring(0, s.lastIndexOf(",", s.indexOf("="))).trim();
                if (s.startsWith("\"")) {
                    s = s.substring(1, s.length() - 1);
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 1, e);
                s = "unknown_cn_" + x;
            }
            jks.setCertificateEntry(s, certs[x]);
            ++x;
        }
        jks.store(new FileOutputStream(new File_S(keystore_path)), keystore_pass.toCharArray());
    }

    public static boolean downloadCertificates(String host, int port) {
        boolean ok = false;
        try {
            Vector last_chain;
            block11: {
                KeyStore cacerts = KeyStore.getInstance("JKS");
                cacerts.load(new FileInputStream(String.valueOf(System.getProperty("java.home")) + "/lib/security/cacerts"), "changeit".toCharArray());
                last_chain = new Vector();
                final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(cacerts);
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, new TrustManager[]{new X509TrustManager(){

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        int x = 0;
                        while (x < chain.length) {
                            last_chain.addElement(chain[x]);
                            ++x;
                        }
                        ((X509TrustManager)tmf.getTrustManagers()[0]).checkServerTrusted(chain, authType);
                    }
                }}, null);
                Log.log("SERVER", 0, "Connecting to:" + host + ":" + port);
                SSLSocket socket = (SSLSocket)context.getSocketFactory().createSocket(host, port);
                try {
                    try {
                        socket.setSoTimeout(10000);
                        socket.startHandshake();
                        ok = true;
                    }
                    catch (SSLException e) {
                        Log.log("SERVER", 1, e);
                        socket.close();
                        break block11;
                    }
                }
                catch (Throwable throwable) {
                    socket.close();
                    throw throwable;
                }
                socket.close();
            }
            if (!ok) {
                if (last_chain.size() > 0) {
                    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                    FileInputStream in = new FileInputStream("localcacerts");
                    ks.load(in, "changeit".toCharArray());
                    ((InputStream)in).close();
                    int x = 0;
                    while (x < last_chain.size()) {
                        X509Certificate cert = (X509Certificate)last_chain.elementAt(x);
                        Log.log("SERVER", 0, String.valueOf(x) + ":Common Name / Subject: " + cert.getSubjectDN());
                        Log.log("SERVER", 0, String.valueOf(x) + ":Issuer: " + cert.getIssuerDN());
                        ks.setCertificateEntry(String.valueOf(host) + "-" + x, cert);
                        ++x;
                    }
                    FileOutputStream out = new FileOutputStream("localcacerts");
                    ks.store(out, "changeit".toCharArray());
                    ((OutputStream)out).close();
                } else {
                    Log.log("SERVER", 0, "Cert chain couldn't be downloaded...?");
                }
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
        return ok;
    }

    public static boolean addPublic(String keystore_path, String keystore_pass, String alias, String key_path) throws Exception {
        String keystore_format = keystore_path.toUpperCase().trim().indexOf("PFX") >= 0 || keystore_path.toUpperCase().trim().indexOf("PKCS12") >= 0 ? "PKCS12" : "JKS";
        KeyStore jks = KeyStore.getInstance(keystore_format);
        jks.load(new FileInputStream(new File_S(keystore_path)), keystore_pass.toCharArray());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        if (new File_S(key_path).length() > 0x100000L) {
            throw new Exception("File too big.");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        com.crushftp.client.Common.copyStreams(new FileInputStream(new File_S(key_path)), baos, true, true);
        Certificate[] certs = new Certificate[1];
        Collection<? extends Certificate> c = cf.generateCertificates(new ByteArrayInputStream(baos.toByteArray()));
        if (c.size() == 1) {
            Certificate cert;
            certs[0] = cert = cf.generateCertificate(new ByteArrayInputStream(baos.toByteArray()));
        } else {
            certs = new Certificate[c.toArray().length];
            int x = 0;
            while (x < certs.length) {
                certs[x] = (Certificate)c.toArray()[x];
                ++x;
            }
        }
        int x = 0;
        while (x < certs.length) {
            String s = alias;
            if (certs.length > 1) {
                s = String.valueOf(s) + x;
            }
            jks.setCertificateEntry(s, certs[x]);
            ++x;
        }
        jks.store(new FileOutputStream(new File_S(keystore_path)), keystore_pass.toCharArray());
        return true;
    }

    public static X509Certificate createTlsAlpn01Certificate(KeyPair keypair, String domain, byte[] acmeValidation) throws Exception {
        if (acmeValidation == null || acmeValidation.length != 32) {
            throw new IllegalArgumentException("Bad acmeValidation parameter");
        }
        X500Name issuer = new X500Name("CN=acme.invalid");
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date();
        long ltime = notBefore.getTime() + 604800000L;
        Date notAfter = new Date(ltime);
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuer, serial, notBefore, notAfter, issuer, keypair.getPublic());
        GeneralName[] gns = new GeneralName[]{new GeneralName(2, domain)};
        certBuilder.addExtension(Extension.subjectAlternativeName, false, (ASN1Encodable)new GeneralNames(gns));
        certBuilder.addExtension(new ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.31").intern(), true, (ASN1Encodable)new DEROctetString(acmeValidation));
        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA256withRSA");
        byte[] cert = certBuilder.build(signerBuilder.build(keypair.getPrivate())).getEncoded();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(cert));
    }

    public static void addReply(KeyStore jks, X509Certificate ca_reply, String private_alias, String key_pass, X509Certificate caIntermediateCertificate) throws Exception {
        Vector<X509Certificate> trusted_certs_used = new Vector<X509Certificate>();
        trusted_certs_used.addElement(ca_reply);
        if (caIntermediateCertificate != null) {
            trusted_certs_used.addElement(caIntermediateCertificate);
        }
        Certificate[] certs2 = new X509Certificate[trusted_certs_used.size()];
        int x = 0;
        while (x < trusted_certs_used.size()) {
            certs2[x] = (X509Certificate)trusted_certs_used.elementAt(x);
            ++x;
        }
        jks.setKeyEntry(private_alias, jks.getKey(private_alias, key_pass.toCharArray()), key_pass.toCharArray(), certs2);
    }
}

