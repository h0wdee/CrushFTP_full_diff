/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.asn1.ASN1ObjectIdentifier
 *  lw.bouncycastle.asn1.ASN1OctetString
 *  lw.bouncycastle.asn1.ASN1Primitive
 *  lw.bouncycastle.asn1.DEROctetString
 *  lw.bouncycastle.asn1.cryptlib.CryptlibObjectIdentifiers
 *  lw.bouncycastle.asn1.gnu.GNUObjectIdentifiers
 *  lw.bouncycastle.asn1.nist.NISTNamedCurves
 *  lw.bouncycastle.asn1.teletrust.TeleTrusTNamedCurves
 *  lw.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers
 *  lw.bouncycastle.asn1.x509.SubjectPublicKeyInfo
 *  lw.bouncycastle.asn1.x9.X9ECParameters
 *  lw.bouncycastle.asn1.x9.X9ECPoint
 *  lw.bouncycastle.bcpg.ArmoredInputStream
 *  lw.bouncycastle.bcpg.ArmoredOutputStream
 *  lw.bouncycastle.bcpg.BCPGKey
 *  lw.bouncycastle.bcpg.ECDHPublicBCPGKey
 *  lw.bouncycastle.bcpg.ECDSAPublicBCPGKey
 *  lw.bouncycastle.bcpg.ExperimentalPacket
 *  lw.bouncycastle.bcpg.PublicKeyPacket
 *  lw.bouncycastle.bcpg.PublicSubkeyPacket
 *  lw.bouncycastle.bcpg.SignatureSubpacket
 *  lw.bouncycastle.bcpg.TrustPacket
 *  lw.bouncycastle.bcpg.sig.NotationData
 *  lw.bouncycastle.crypto.AsymmetricCipherKeyPair
 *  lw.bouncycastle.crypto.Digest
 *  lw.bouncycastle.crypto.KeyGenerationParameters
 *  lw.bouncycastle.crypto.digests.SHA256Digest
 *  lw.bouncycastle.crypto.generators.DSAKeyPairGenerator
 *  lw.bouncycastle.crypto.generators.DSAParametersGenerator
 *  lw.bouncycastle.crypto.generators.ECKeyPairGenerator
 *  lw.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
 *  lw.bouncycastle.crypto.generators.ElGamalKeyPairGenerator
 *  lw.bouncycastle.crypto.generators.RSAKeyPairGenerator
 *  lw.bouncycastle.crypto.generators.X25519KeyPairGenerator
 *  lw.bouncycastle.crypto.params.AsymmetricKeyParameter
 *  lw.bouncycastle.crypto.params.DSAKeyGenerationParameters
 *  lw.bouncycastle.crypto.params.DSAParameterGenerationParameters
 *  lw.bouncycastle.crypto.params.ECDomainParameters
 *  lw.bouncycastle.crypto.params.ECKeyGenerationParameters
 *  lw.bouncycastle.crypto.params.ECNamedDomainParameters
 *  lw.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
 *  lw.bouncycastle.crypto.params.ElGamalKeyGenerationParameters
 *  lw.bouncycastle.crypto.params.ElGamalParameters
 *  lw.bouncycastle.crypto.params.RSAKeyGenerationParameters
 *  lw.bouncycastle.crypto.params.X25519KeyGenerationParameters
 *  lw.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory
 *  lw.bouncycastle.openpgp.PGPDataValidationException
 *  lw.bouncycastle.openpgp.PGPEncryptedDataGenerator
 *  lw.bouncycastle.openpgp.PGPEncryptedDataList
 *  lw.bouncycastle.openpgp.PGPException
 *  lw.bouncycastle.openpgp.PGPKdfParameters
 *  lw.bouncycastle.openpgp.PGPKeyPair
 *  lw.bouncycastle.openpgp.PGPKeyRingGenerator
 *  lw.bouncycastle.openpgp.PGPLiteralData
 *  lw.bouncycastle.openpgp.PGPLiteralDataGenerator
 *  lw.bouncycastle.openpgp.PGPOnePassSignatureList
 *  lw.bouncycastle.openpgp.PGPPBEEncryptedData
 *  lw.bouncycastle.openpgp.PGPPrivateKey
 *  lw.bouncycastle.openpgp.PGPPublicKey
 *  lw.bouncycastle.openpgp.PGPPublicKeyRing
 *  lw.bouncycastle.openpgp.PGPPublicKeyRingCollection
 *  lw.bouncycastle.openpgp.PGPSecretKey
 *  lw.bouncycastle.openpgp.PGPSecretKeyRing
 *  lw.bouncycastle.openpgp.PGPSecretKeyRingCollection
 *  lw.bouncycastle.openpgp.PGPSignature
 *  lw.bouncycastle.openpgp.PGPSignatureGenerator
 *  lw.bouncycastle.openpgp.PGPSignatureList
 *  lw.bouncycastle.openpgp.PGPSignatureSubpacketGenerator
 *  lw.bouncycastle.openpgp.PGPSignatureSubpacketVector
 *  lw.bouncycastle.openpgp.PGPUtil
 *  lw.bouncycastle.openpgp.operator.KeyFingerPrintCalculator
 *  lw.bouncycastle.openpgp.operator.PBESecretKeyDecryptor
 *  lw.bouncycastle.openpgp.operator.PBESecretKeyEncryptor
 *  lw.bouncycastle.openpgp.operator.PGPContentSignerBuilder
 *  lw.bouncycastle.openpgp.operator.PGPDigestCalculator
 *  lw.bouncycastle.openpgp.operator.PGPKeyEncryptionMethodGenerator
 *  lw.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator
 *  lw.bouncycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder
 *  lw.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder
 *  lw.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider
 *  lw.bouncycastle.openpgp.operator.bc.BcPGPKeyConverter
 *  lw.bouncycastle.openpgp.operator.bc.BcPGPKeyPair
 *  lw.bouncycastle.util.Arrays
 *  lw.bouncycastle.util.encoders.DecoderException
 */
package com.didisoft.pgp;

import com.didisoft.pgp.CompressionAlgorithm;
import com.didisoft.pgp.CypherAlgorithm;
import com.didisoft.pgp.EcCurve;
import com.didisoft.pgp.HashAlgorithm;
import com.didisoft.pgp.KeyAlgorithm;
import com.didisoft.pgp.KeyPairInformation;
import com.didisoft.pgp.PGPException;
import com.didisoft.pgp.bc.BaseLib;
import com.didisoft.pgp.bc.BoolValue;
import com.didisoft.pgp.bc.DirectByteArrayOutputStream;
import com.didisoft.pgp.bc.IOUtil;
import com.didisoft.pgp.bc.PGPObjectFactory2;
import com.didisoft.pgp.bc.PGPSignatureSubpacketGeneratorExtended;
import com.didisoft.pgp.bc.ReflectionUtils;
import com.didisoft.pgp.bc.UnknownKeyPacketsException;
import com.didisoft.pgp.bc.elgamal.BaseElGamalKeyPairGenerator;
import com.didisoft.pgp.bc.elgamal.FastElGamal;
import com.didisoft.pgp.bc.kbx.KBXDataBlob;
import com.didisoft.pgp.bc.kbx.KBXFirstBlob;
import com.didisoft.pgp.events.ICustomKeyListener;
import com.didisoft.pgp.events.IKeyStoreSaveListener;
import com.didisoft.pgp.events.IKeyStoreSearchListener;
import com.didisoft.pgp.exceptions.NoPrivateKeyFoundException;
import com.didisoft.pgp.exceptions.NoPublicKeyFoundException;
import com.didisoft.pgp.exceptions.NonPGPDataException;
import com.didisoft.pgp.exceptions.WrongPasswordException;
import com.didisoft.pgp.storage.FileKeyStorage;
import com.didisoft.pgp.storage.IKeyStoreStorage;
import com.didisoft.pgp.storage.InMemoryKeyStorage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;
import lw.bouncycastle.asn1.ASN1ObjectIdentifier;
import lw.bouncycastle.asn1.ASN1OctetString;
import lw.bouncycastle.asn1.ASN1Primitive;
import lw.bouncycastle.asn1.DEROctetString;
import lw.bouncycastle.asn1.cryptlib.CryptlibObjectIdentifiers;
import lw.bouncycastle.asn1.gnu.GNUObjectIdentifiers;
import lw.bouncycastle.asn1.nist.NISTNamedCurves;
import lw.bouncycastle.asn1.teletrust.TeleTrusTNamedCurves;
import lw.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import lw.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import lw.bouncycastle.asn1.x9.X9ECParameters;
import lw.bouncycastle.asn1.x9.X9ECPoint;
import lw.bouncycastle.bcpg.ArmoredInputStream;
import lw.bouncycastle.bcpg.ArmoredOutputStream;
import lw.bouncycastle.bcpg.BCPGKey;
import lw.bouncycastle.bcpg.ECDHPublicBCPGKey;
import lw.bouncycastle.bcpg.ECDSAPublicBCPGKey;
import lw.bouncycastle.bcpg.ExperimentalPacket;
import lw.bouncycastle.bcpg.PublicKeyPacket;
import lw.bouncycastle.bcpg.PublicSubkeyPacket;
import lw.bouncycastle.bcpg.SignatureSubpacket;
import lw.bouncycastle.bcpg.TrustPacket;
import lw.bouncycastle.bcpg.sig.NotationData;
import lw.bouncycastle.crypto.AsymmetricCipherKeyPair;
import lw.bouncycastle.crypto.Digest;
import lw.bouncycastle.crypto.KeyGenerationParameters;
import lw.bouncycastle.crypto.digests.SHA256Digest;
import lw.bouncycastle.crypto.generators.DSAKeyPairGenerator;
import lw.bouncycastle.crypto.generators.DSAParametersGenerator;
import lw.bouncycastle.crypto.generators.ECKeyPairGenerator;
import lw.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import lw.bouncycastle.crypto.generators.ElGamalKeyPairGenerator;
import lw.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import lw.bouncycastle.crypto.generators.X25519KeyPairGenerator;
import lw.bouncycastle.crypto.params.AsymmetricKeyParameter;
import lw.bouncycastle.crypto.params.DSAKeyGenerationParameters;
import lw.bouncycastle.crypto.params.DSAParameterGenerationParameters;
import lw.bouncycastle.crypto.params.ECDomainParameters;
import lw.bouncycastle.crypto.params.ECKeyGenerationParameters;
import lw.bouncycastle.crypto.params.ECNamedDomainParameters;
import lw.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import lw.bouncycastle.crypto.params.ElGamalKeyGenerationParameters;
import lw.bouncycastle.crypto.params.ElGamalParameters;
import lw.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import lw.bouncycastle.crypto.params.X25519KeyGenerationParameters;
import lw.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import lw.bouncycastle.openpgp.PGPDataValidationException;
import lw.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import lw.bouncycastle.openpgp.PGPEncryptedDataList;
import lw.bouncycastle.openpgp.PGPKdfParameters;
import lw.bouncycastle.openpgp.PGPKeyPair;
import lw.bouncycastle.openpgp.PGPKeyRingGenerator;
import lw.bouncycastle.openpgp.PGPLiteralData;
import lw.bouncycastle.openpgp.PGPLiteralDataGenerator;
import lw.bouncycastle.openpgp.PGPOnePassSignatureList;
import lw.bouncycastle.openpgp.PGPPBEEncryptedData;
import lw.bouncycastle.openpgp.PGPPrivateKey;
import lw.bouncycastle.openpgp.PGPPublicKey;
import lw.bouncycastle.openpgp.PGPPublicKeyRing;
import lw.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import lw.bouncycastle.openpgp.PGPSecretKey;
import lw.bouncycastle.openpgp.PGPSecretKeyRing;
import lw.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import lw.bouncycastle.openpgp.PGPSignature;
import lw.bouncycastle.openpgp.PGPSignatureGenerator;
import lw.bouncycastle.openpgp.PGPSignatureList;
import lw.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import lw.bouncycastle.openpgp.PGPSignatureSubpacketVector;
import lw.bouncycastle.openpgp.PGPUtil;
import lw.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import lw.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import lw.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import lw.bouncycastle.openpgp.operator.PGPContentSignerBuilder;
import lw.bouncycastle.openpgp.operator.PGPDigestCalculator;
import lw.bouncycastle.openpgp.operator.PGPKeyEncryptionMethodGenerator;
import lw.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import lw.bouncycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder;
import lw.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import lw.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import lw.bouncycastle.openpgp.operator.bc.BcPGPKeyConverter;
import lw.bouncycastle.openpgp.operator.bc.BcPGPKeyPair;
import lw.bouncycastle.util.Arrays;
import lw.bouncycastle.util.encoders.DecoderException;

public class KeyStore
extends BaseLib
implements Serializable {
    private static final long serialVersionUID = -47989515304466957L;
    protected static final int DEFAULT_BUFFER_SIZE = 0x100000;
    static final String BOUNCY_CASTLE_PROVIDER = "BC";
    private String keystoreFileName;
    private DirectByteArrayOutputStream storeStream = new DirectByteArrayOutputStream(102400);
    private IKeyStoreStorage storage = null;
    private byte[] xKey;
    private SealedObject password;
    private boolean autoSave = true;
    private boolean backupOnSave = true;
    PGPPublicKeyRingCollection pubCollection;
    PGPSecretKeyRingCollection secCollection;
    private Date pubModifiedDate;
    private Date secModifiedDate;
    private KeyCertificationType defaultCertificationType = KeyCertificationType.PositiveCertification;
    private boolean caseSensitiveMatch = true;
    private boolean partialMatch = true;
    private static final Logger log = Logger.getLogger(KeyStore.class.getName());
    public static final String ELGAMAL = "ELGAMAL";
    public static final String DSA = "DSA";
    public static final String RSA = "RSA";
    public static final String EC = "EC";
    private boolean usePrecomputedPrimes = true;
    private boolean skipLucasLehmerPrimeTest = false;
    private boolean fastElGamalGeneration = true;
    private String kekCypher = "CAST5";
    private static Pattern regexHex = Pattern.compile("^(0x)?[A-Fa-f0-9]{6,8}$");
    private static Pattern longRegexHex = Pattern.compile("^(0x)?[A-Fa-f0-9]{14,16}$");
    private static String p1024 = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE65381FFFFFFFFFFFFFFFF";
    private static String g1024 = "2";
    private static String p1536 = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF";
    private static String g1536 = "2";
    private static String p2048 = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AACAA68FFFFFFFFFFFFFFFF";
    private static String g2048 = "2";
    private static String p3072 = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6BF12FFA06D98A0864D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E208E24FA074E5AB3143DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF";
    private static String g3072 = "2";
    private static String p4096 = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6BF12FFA06D98A0864D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E208E24FA074E5AB3143DB5BFCE0FD108E4B82D120A92108011A723C12A787E6D788719A10BDBA5B2699C327186AF4E23C1A946834B6150BDA2583E9CA2AD44CE8DBBBC2DB04DE8EF92E8EFC141FBECAA6287C59474E6BC05D99B2964FA090C3A2233BA186515BE7ED1F612970CEE2D7AFB81BDD762170481CD0069127D5B05AA993B4EA988D8FDDC186FFB7DC90A6C08F4DF435C934063199FFFFFFFFFFFFFFFF";
    private static String g4096 = "2";
    private static String p6144 = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6BF12FFA06D98A0864D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E208E24FA074E5AB3143DB5BFCE0FD108E4B82D120A92108011A723C12A787E6D788719A10BDBA5B2699C327186AF4E23C1A946834B6150BDA2583E9CA2AD44CE8DBBBC2DB04DE8EF92E8EFC141FBECAA6287C59474E6BC05D99B2964FA090C3A2233BA186515BE7ED1F612970CEE2D7AFB81BDD762170481CD0069127D5B05AA993B4EA988D8FDDC186FFB7DC90A6C08F4DF435C93402849236C3FAB4D27C7026C1D4DCB2602646DEC9751E763DBA37BDF8FF9406AD9E530EE5DB382F413001AEB06A53ED9027D831179727B0865A8918DA3EDBEBCF9B14ED44CE6CBACED4BB1BDB7F1447E6CC254B332051512BD7AF426FB8F401378CD2BF5983CA01C64B92ECF032EA15D1721D03F482D7CE6E74FEF6D55E702F46980C82B5A84031900B1C9E59E7C97FBEC7E8F323A97A7E36CC88BE0F1D45B7FF585AC54BD407B22B4154AACC8F6D7EBF48E1D814CC5ED20F8037E0A79715EEF29BE32806A1D58BB7C5DA76F550AA3D8A1FBFF0EB19CCB1A313D55CDA56C9EC2EF29632387FE8D76E3C0468043E8F663F4860EE12BF2D5B0B7474D6E694F91E6DCC4024FFFFFFFFFFFFFFFF";
    private static String g6144 = "2";
    private static String p8192 = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6BF12FFA06D98A0864D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E208E24FA074E5AB3143DB5BFCE0FD108E4B82D120A92108011A723C12A787E6D788719A10BDBA5B2699C327186AF4E23C1A946834B6150BDA2583E9CA2AD44CE8DBBBC2DB04DE8EF92E8EFC141FBECAA6287C59474E6BC05D99B2964FA090C3A2233BA186515BE7ED1F612970CEE2D7AFB81BDD762170481CD0069127D5B05AA993B4EA988D8FDDC186FFB7DC90A6C08F4DF435C93402849236C3FAB4D27C7026C1D4DCB2602646DEC9751E763DBA37BDF8FF9406AD9E530EE5DB382F413001AEB06A53ED9027D831179727B0865A8918DA3EDBEBCF9B14ED44CE6CBACED4BB1BDB7F1447E6CC254B332051512BD7AF426FB8F401378CD2BF5983CA01C64B92ECF032EA15D1721D03F482D7CE6E74FEF6D55E702F46980C82B5A84031900B1C9E59E7C97FBEC7E8F323A97A7E36CC88BE0F1D45B7FF585AC54BD407B22B4154AACC8F6D7EBF48E1D814CC5ED20F8037E0A79715EEF29BE32806A1D58BB7C5DA76F550AA3D8A1FBFF0EB19CCB1A313D55CDA56C9EC2EF29632387FE8D76E3C0468043E8F663F4860EE12BF2D5B0B7474D6E694F91E6DBE115974A3926F12FEE5E438777CB6A932DF8CD8BEC4D073B931BA3BC832B68D9DD300741FA7BF8AFC47ED2576F6936BA424663AAB639C5AE4F5683423B4742BF1C978238F16CBE39D652DE3FDB8BEFC848AD922222E04A4037C0713EB57A81A23F0C73473FC646CEA306B4BCBC8862F8385DDFA9D4B7FA2C087E879683303ED5BDD3A062B3CF5B3A278A66D2A13F83F44F82DDF310EE074AB6A364597E899A0255DC164F31CC50846851DF9AB48195DED7EA1B1D510BD7EE74D73FAF36BC31ECFA268359046F4EB879F924009438B481C6CD7889A002ED5EE382BC9190DA6FC026E479558E4475677E9AA9E3050E2765694DFC81F56E880B96E7160C980DD98EDD3DFFFFFFFFFFFFFFFFF";
    private static String g8192 = "2";
    private HashMap userIds = new HashMap();
    private HashMap keyHexIds = new HashMap();
    private HashMap keys = new HashMap();
    private String asciiVersionHeader = null;
    private int maxTrustDepthCheck = 5;
    private int marginalsNeeded = 3;
    private LinkedList saveListeners = new LinkedList();
    private LinkedList searchListeners = new LinkedList();
    private ICustomKeyListener passKeyListener = null;

    public KeyStore() {
        try {
            this.storage = new InMemoryKeyStorage();
            this.asciiVersionHeader = version;
            this.pubCollection = new PGPPublicKeyRingCollection((Collection)Collections.EMPTY_LIST);
            this.secCollection = new PGPSecretKeyRingCollection((Collection)Collections.EMPTY_LIST);
            this.pubModifiedDate = this.secModifiedDate = new Date();
            SecureRandom secureRandom = IOUtil.getSecureRandom();
            this.xKey = new byte[16];
            secureRandom.nextBytes(this.xKey);
        }
        catch (Exception exception) {
            StackTraceElement[] stackTraceElementArray = exception.getStackTrace();
            for (int i = 0; i < stackTraceElementArray.length; ++i) {
                this.Debug(stackTraceElementArray.toString());
            }
        }
    }

    public KeyStore(String string, String string2) throws IOException, PGPException {
        this(new FileKeyStorage(string), string2);
        this.Debug("Opening KeyStore from {0}", string);
        this.keystoreFileName = string;
    }

    public KeyStore(IKeyStoreStorage iKeyStoreStorage) throws IOException, PGPException {
        this(iKeyStoreStorage, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public KeyStore(IKeyStoreStorage iKeyStoreStorage, String string) throws IOException, PGPException {
        this();
        this.storage = iKeyStoreStorage;
        this.setPassword(string);
        InputStream inputStream = null;
        try {
            inputStream = this.storage.getInputStream();
            if (inputStream != null) {
                this.loadFromStreamInternal(inputStream, this.getPassword());
                this.onLoadKeys();
            }
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public KeyStore(IKeyStoreStorage iKeyStoreStorage, String string, ICustomKeyListener iCustomKeyListener) throws IOException, PGPException {
        this();
        this.storage = iKeyStoreStorage;
        this.passKeyListener = iCustomKeyListener;
        this.setPassword(string);
        InputStream inputStream = null;
        try {
            inputStream = this.storage.getInputStream();
            if (inputStream != null) {
                this.loadFromStreamInternal(inputStream, this.getPassword());
                this.onLoadKeys();
            }
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    public KeyStore(String string, String string2, ICustomKeyListener iCustomKeyListener) throws IOException, PGPException {
        this(string, string2);
        this.passKeyListener = iCustomKeyListener;
        this.setPassword(string2);
    }

    public static KeyStore openFile(String string, String string2) throws PGPException, IOException {
        KeyStore keyStore = new KeyStore(string, string2);
        return keyStore;
    }

    public static KeyStore openFile(String string, String string2, ICustomKeyListener iCustomKeyListener) throws PGPException, IOException {
        KeyStore keyStore = new KeyStore(string, string2, iCustomKeyListener);
        return keyStore;
    }

    public static KeyStore getInstance() {
        KeyStore keyStore = new KeyStore();
        return keyStore;
    }

    public static KeyStore openInMemory() {
        KeyStore keyStore = new KeyStore();
        return keyStore;
    }

    public void addSaveListener(IKeyStoreSaveListener iKeyStoreSaveListener) {
        this.saveListeners.add(iKeyStoreSaveListener);
    }

    public boolean removeSaveListener(IKeyStoreSaveListener iKeyStoreSaveListener) {
        return this.saveListeners.remove(iKeyStoreSaveListener);
    }

    public void addSearchListener(IKeyStoreSearchListener iKeyStoreSearchListener) {
        this.searchListeners.add(iKeyStoreSearchListener);
    }

    public boolean removeSearchListener(IKeyStoreSearchListener iKeyStoreSearchListener) {
        return this.searchListeners.remove(iKeyStoreSearchListener);
    }

    void triggetPrivateKeyNotFound(long l) {
        if (this.containsPrivateKey(l)) {
            return;
        }
        for (int i = 0; i < this.searchListeners.size(); ++i) {
            ((IKeyStoreSearchListener)this.searchListeners.get(i)).onKeyNotFound(this, false, l, KeyPairInformation.keyId2Hex(l), "");
        }
    }

    public void purge() throws PGPException {
        this.keyHexIds.clear();
        this.keys.clear();
        try {
            this.pubCollection = new PGPPublicKeyRingCollection((Collection)Collections.EMPTY_LIST);
            this.secCollection = new PGPSecretKeyRingCollection((Collection)Collections.EMPTY_LIST);
        }
        catch (Exception exception) {
            throw new PGPException("unable to initialise: " + exception, exception);
        }
        this.userIds.clear();
        this.save(false);
    }

    public void loadFromStream(InputStream inputStream, String string) throws IOException, PGPException {
        if (!inputStream.markSupported()) {
            inputStream = new BufferedInputStream(inputStream);
        }
        inputStream.mark(0x100000);
        InputStream inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
        if (inputStream2 instanceof ArmoredInputStream) {
            if (!((ArmoredInputStream)inputStream2).isEndOfStream()) {
                inputStream.reset();
                this.importKeyRing(inputStream, string);
            }
        } else {
            PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream);
            Object object = pGPObjectFactory2.nextObject();
            if (object instanceof PGPPublicKeyRing) {
                inputStream.reset();
                this.importKeyRing(inputStream, string);
            } else if (object instanceof PGPSecretKeyRing) {
                inputStream.reset();
                this.importKeyRing(inputStream, string);
            } else if (object instanceof PGPLiteralData) {
                inputStream.reset();
                this.loadFromStreamInternal(inputStream, string);
            } else if (object instanceof PGPEncryptedDataList) {
                inputStream.reset();
                this.loadFromStreamInternal(inputStream, string);
            } else {
                throw new NonPGPDataException("The provided key storage does not contain valid key data.");
            }
        }
        this.onLoadKeys();
    }

    private void loadFromStreamInternal(InputStream inputStream, String string) throws IOException, PGPException {
        block11: {
            try {
                InputStream inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
                PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream2);
                Object object = pGPObjectFactory2.nextObject();
                if (object instanceof PGPLiteralData) {
                    PGPLiteralData pGPLiteralData = (PGPLiteralData)object;
                    if (pGPLiteralData != null) {
                        byte[] byArray = this.readCollection(pGPLiteralData);
                        this.pubCollection = new PGPPublicKeyRingCollection(byArray, staticBCFactory.CreateKeyFingerPrintCalculator());
                        pGPLiteralData = (PGPLiteralData)pGPObjectFactory2.nextObject();
                        if (pGPLiteralData != null) {
                            byte[] byArray2 = this.readCollection(pGPLiteralData);
                            this.secCollection = new PGPSecretKeyRingCollection(byArray2, staticBCFactory.CreateKeyFingerPrintCalculator());
                        }
                    }
                    break block11;
                }
                if (!(object instanceof PGPEncryptedDataList)) break block11;
                PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
                PGPPBEEncryptedData pGPPBEEncryptedData = (PGPPBEEncryptedData)pGPEncryptedDataList.get(0);
                InputStream inputStream3 = null;
                try {
                    inputStream3 = pGPPBEEncryptedData.getDataStream(staticBCFactory.CreatePBEDataDecryptorFactory(string));
                }
                catch (PGPDataValidationException pGPDataValidationException) {
                    throw new WrongPasswordException("The specified password is wrong.", pGPDataValidationException.getUnderlyingException());
                }
                pGPObjectFactory2 = new PGPObjectFactory2(inputStream3);
                PGPLiteralData pGPLiteralData = (PGPLiteralData)pGPObjectFactory2.nextObject();
                byte[] byArray = this.readCollection(pGPLiteralData);
                this.pubModifiedDate = pGPLiteralData.getModificationTime();
                pGPLiteralData = (PGPLiteralData)pGPObjectFactory2.nextObject();
                byte[] byArray3 = this.readCollection(pGPLiteralData);
                this.secModifiedDate = pGPLiteralData.getModificationTime();
                if (!pGPPBEEncryptedData.isIntegrityProtected()) {
                    throw new PGPDataValidationException("no integrity protection found.");
                }
                if (!pGPPBEEncryptedData.verify()) {
                    throw new PGPDataValidationException("store failed integrity check.");
                }
                this.pubCollection = new PGPPublicKeyRingCollection(byArray, staticBCFactory.CreateKeyFingerPrintCalculator());
                this.secCollection = new PGPSecretKeyRingCollection(byArray3, staticBCFactory.CreateKeyFingerPrintCalculator());
            }
            catch (DecoderException decoderException) {
                throw new PGPException(decoderException.getMessage(), (Exception)((Object)decoderException));
            }
            catch (IOException iOException) {
                throw new PGPException(iOException.getMessage(), iOException);
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
        }
    }

    public void loadFromStream(InputStream inputStream) throws IOException, PGPException {
        this.loadFromStream(inputStream, "");
    }

    public void saveToStream(OutputStream outputStream) throws IOException {
        this.writeCollection(outputStream, "pubring.pkr", new Date(), this.pubCollection.getEncoded());
        this.writeCollection(outputStream, "secring.skr", new Date(), this.secCollection.getEncoded());
    }

    public void saveToStream(OutputStream outputStream, String string) throws IOException, PGPException {
        this.store(outputStream, string);
    }

    public boolean isPartialMatchUserIds() {
        return this.partialMatch;
    }

    public void setPartialMatchUserIds(boolean bl) {
        this.partialMatch = bl;
    }

    public KeyCertificationType getDefaultKeyCertificationType() {
        return this.defaultCertificationType;
    }

    public void setDefaultKeyCertificationType(KeyCertificationType keyCertificationType) {
        this.defaultCertificationType = keyCertificationType;
    }

    public boolean getUsePrecomputedPrimes() {
        return this.usePrecomputedPrimes;
    }

    public void setUsePrecomputedPrimes(boolean bl) {
        this.usePrecomputedPrimes = bl;
    }

    public boolean isInMemory() {
        return this.storage instanceof InMemoryKeyStorage;
    }

    public String getAsciiVersionHeader() {
        return this.asciiVersionHeader;
    }

    public void setAsciiVersionHeader(String string) {
        this.asciiVersionHeader = string;
    }

    private void setAsciiVersionHeader(OutputStream outputStream) {
        if (outputStream instanceof ArmoredOutputStream) {
            ((ArmoredOutputStream)outputStream).setHeader("Version", this.asciiVersionHeader);
        }
    }

    public PGPSecretKeyRingCollection getRawSecretKeys() {
        return this.secCollection;
    }

    public PGPPublicKeyRingCollection getRawPublicKeys() {
        return this.pubCollection;
    }

    public static boolean isPasswordProtected(String string) throws IOException {
        return KeyStore.isPasswordProtected(new FileKeyStorage(string));
    }

    public static boolean isPasswordProtected(IKeyStoreStorage iKeyStoreStorage) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = iKeyStoreStorage.getInputStream();
            boolean bl = KeyStore.isPasswordProtected(inputStream);
            return bl;
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    public static boolean isPasswordProtected(InputStream inputStream) throws IOException {
        InputStream inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream);
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream2);
        Object object = pGPObjectFactory2.nextObject();
        return object instanceof PGPEncryptedDataList;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean checkPassword(IKeyStoreStorage iKeyStoreStorage, String string) throws IOException {
        if (string == null) {
            string = "";
        }
        InputStream inputStream = null;
        try {
            inputStream = iKeyStoreStorage.getInputStream();
            boolean bl = KeyStore.checkPassword(inputStream, string);
            return bl;
        }
        finally {
            IOUtil.closeStream(inputStream);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean checkPassword(String string, String string2) throws IOException {
        boolean bl;
        if (string2 == null) {
            string2 = "";
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string);
            bl = KeyStore.checkPassword(fileInputStream, string2);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        return bl;
    }

    public static boolean checkPassword(InputStream inputStream, String string) throws IOException {
        InputStream inputStream2;
        PGPObjectFactory2 pGPObjectFactory2;
        Object object;
        if (string == null) {
            string = "";
        }
        if ((object = (pGPObjectFactory2 = new PGPObjectFactory2(inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream))).nextObject()) instanceof PGPLiteralData) {
            return false;
        }
        PGPEncryptedDataList pGPEncryptedDataList = (PGPEncryptedDataList)object;
        PGPPBEEncryptedData pGPPBEEncryptedData = (PGPPBEEncryptedData)pGPEncryptedDataList.get(0);
        InputStream inputStream3 = null;
        try {
            inputStream3 = pGPPBEEncryptedData.getDataStream(staticBCFactory.CreatePBEDataDecryptorFactory(string));
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            if (pGPException instanceof PGPDataValidationException) {
                return false;
            }
            return false;
        }
        return true;
    }

    public String[] getUserIds() {
        return this.userIds.keySet().toArray(new String[this.userIds.keySet().size()]);
    }

    public String[] getKeyHexIds() {
        return this.keyHexIds.keySet().toArray(new String[this.keyHexIds.keySet().size()]);
    }

    public long getKeyIdForUserId(String string) {
        if (null != this.userIds.get(string)) {
            List list = (List)this.userIds.get(string);
            return (Long)list.get(0);
        }
        long l = this.getKeyIdForKeyIdHex(string);
        if (l > 0L) {
            return l;
        }
        Collection collection = this.getPublicKeyRingCollection(string);
        if (collection.size() > 0) {
            return ((PGPPublicKeyRing)collection.iterator().next()).getPublicKey().getKeyID();
        }
        return -1L;
    }

    public long getKeyIdForKeyIdHex(String string) {
        String string2 = this.normalizeHexId(string);
        if (longRegexHex.matcher(string2).matches()) {
            return Long.parseLong(string2, 16);
        }
        if (null != this.keyHexIds.get(string2)) {
            List list = (List)this.keyHexIds.get(string2);
            return (Long)list.get(0);
        }
        return -1L;
    }

    public void addCertification(long l, long l2, String string, String string2) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(l);
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        PGPSecretKey pGPSecretKey = null;
        try {
            pGPSecretKey = this.secCollection.getSecretKey(l2);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        PGPSignatureGenerator pGPSignatureGenerator = null;
        try {
            pGPSignatureGenerator = staticBCFactory.CreatePGPSignatureGenerator(pGPPublicKey.getAlgorithm(), 2);
            staticBCFactory.initSign(pGPSignatureGenerator, 16, KeyStore.extractPrivateKey(pGPSecretKey, string));
            PGPSignature pGPSignature = pGPSignatureGenerator.generateCertification(string2, pGPPublicKey);
            pGPPublicKey = PGPPublicKey.addCertification((PGPPublicKey)pGPPublicKey, (String)string2, (PGPSignature)pGPSignature);
        }
        catch (Exception exception) {
            throw new PGPException("exception creating signature: " + exception, exception);
        }
        pGPPublicKeyRing = PGPPublicKeyRing.removePublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey);
        pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey);
        this.replacePublicKeyRing(pGPPublicKeyRing);
    }

    public void signPublicKey(long l, long l2, String string) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(l);
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        PGPSecretKey pGPSecretKey = null;
        try {
            pGPSecretKey = this.secCollection.getSecretKey(l2);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        if (pGPSecretKey == null) {
            throw new NoPrivateKeyFoundException("No key found with Key Id: " + l2);
        }
        String string2 = "";
        Iterator iterator = pGPPublicKey.getRawUserIDs();
        if (iterator.hasNext()) {
            string2 = BaseLib.toUserID((byte[])iterator.next());
        }
        this.internalSignKey(pGPPublicKeyRing, pGPPublicKey, string2, pGPSecretKey, string);
    }

    public void signPublicKey(String string, String string2, String string3) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(string);
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        PGPSecretKey pGPSecretKey = null;
        pGPSecretKey = this.findSecretKeyRing(string2).getSecretKey();
        this.internalSignKey(pGPPublicKeyRing, pGPPublicKey, string, pGPSecretKey, string3);
    }

    public void signPublicKeyAsTrustedIntroducer(long l, long l2, String string) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(l);
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        PGPSecretKey pGPSecretKey = null;
        try {
            pGPSecretKey = this.secCollection.getSecretKey(l2);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        if (pGPSecretKey == null) {
            throw new NoPrivateKeyFoundException("No key found with Key Id: " + l2);
        }
        String string2 = "";
        Iterator iterator = pGPPublicKey.getRawUserIDs();
        if (iterator.hasNext()) {
            string2 = BaseLib.toUserID((byte[])iterator.next());
        }
        this.internalSignKeyAsTrustedIntroducer(pGPPublicKeyRing, pGPPublicKey, string2, pGPSecretKey, string);
    }

    public void signPublicKeyAsTrustedIntroducer(String string, String string2, String string3) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(string);
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        PGPSecretKey pGPSecretKey = this.findSecretKeyRing(string2).getSecretKey();
        this.internalSignKeyAsTrustedIntroducer(pGPPublicKeyRing, pGPPublicKey, string, pGPSecretKey, string3);
    }

    public void setTrust(long l, byte by) throws PGPException, NoPublicKeyFoundException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(l);
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        TrustPacket trustPacket = new TrustPacket((int)by);
        ReflectionUtils.setPrivateFieldvalue(pGPPublicKey, "trustPk", trustPacket);
        pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey);
        this.replacePublicKeyRing(pGPPublicKeyRing);
    }

    public void setTrust(String string, byte by) throws PGPException, NoPublicKeyFoundException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(string);
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        TrustPacket trustPacket = new TrustPacket((int)by);
        ReflectionUtils.setPrivateFieldvalue(pGPPublicKey, "trustPk", trustPacket);
        pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey);
        this.replacePublicKeyRing(pGPPublicKeyRing);
    }

    public boolean isTrusted(String string) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(string);
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        return this.isTrusted(pGPPublicKey.getKeyID(), 0);
    }

    public boolean isTrusted(long l) throws PGPException {
        return this.isTrusted(l, 0);
    }

    private boolean isTrusted(long l, int n) throws PGPException {
        PGPSignature pGPSignature;
        if (n >= this.maxTrustDepthCheck) {
            return false;
        }
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(l);
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        if (this.isTrustedInKeyStore(l, n)) {
            return true;
        }
        int n2 = 0;
        Iterator iterator = pGPPublicKeyRing.getPublicKey().getSignaturesOfType(16);
        while (iterator.hasNext()) {
            pGPSignature = (PGPSignature)iterator.next();
            if (pGPSignature.getKeyID() == pGPPublicKey.getKeyID()) continue;
            if (this.isTrustedIntroducer(pGPSignature.getKeyID())) {
                return true;
            }
            if (this.isMarginalIntroducer(pGPSignature.getKeyID())) {
                ++n2;
            }
            if (n2 < this.getMarginalsNeeded()) continue;
            return true;
        }
        iterator = pGPPublicKeyRing.getPublicKey().getSignaturesOfType(18);
        while (iterator.hasNext()) {
            pGPSignature = (PGPSignature)iterator.next();
            if (pGPSignature.getKeyID() == pGPPublicKey.getKeyID()) continue;
            if (this.isTrustedIntroducer(pGPSignature.getKeyID())) {
                return true;
            }
            if (this.isMarginalIntroducer(pGPSignature.getKeyID())) {
                ++n2;
            }
            if (n2 < this.getMarginalsNeeded()) continue;
            return true;
        }
        iterator = pGPPublicKeyRing.getPublicKey().getSignaturesOfType(19);
        while (iterator.hasNext()) {
            pGPSignature = (PGPSignature)iterator.next();
            if (pGPSignature.getKeyID() == pGPPublicKey.getKeyID()) continue;
            if (this.isTrustedIntroducer(pGPSignature.getKeyID())) {
                return true;
            }
            if (this.isMarginalIntroducer(pGPSignature.getKeyID())) {
                ++n2;
            }
            if (n2 < this.getMarginalsNeeded()) continue;
            return true;
        }
        return false;
    }

    private boolean isMarginalIntroducer(long l) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(l);
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        KeyPairInformation keyPairInformation = (KeyPairInformation)this.keys.get(new Long(pGPPublicKey.getKeyID()));
        byte by = keyPairInformation.getTrust();
        if (by >= 60) {
            return true;
        }
        if (this.maxTrustDepthCheck > 1) {
            Iterator iterator = pGPPublicKeyRing.getPublicKey().getSignaturesOfType(16);
            while (iterator.hasNext()) {
                PGPSignature pGPSignature = (PGPSignature)iterator.next();
                if (pGPSignature.getKeyID() == pGPPublicKey.getKeyID() || !this.isTrustedIntroducer(pGPSignature.getKeyID())) continue;
                return true;
            }
        }
        return false;
    }

    public boolean deleteKeyPair(String string) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing;
        Object object;
        boolean bl;
        long l;
        Collection collection;
        block7: {
            Collection collection2 = this.getPublicKeyRingCollection(string);
            collection = this.getSecretKeyRingCollection(string);
            l = -1L;
            bl = false;
            object = collection2.iterator();
            if (object.hasNext()) {
                pGPPublicKeyRing = (PGPPublicKeyRing)object.next();
                try {
                    this.pubCollection = PGPPublicKeyRingCollection.removePublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
                    this.onRemovePublicRing(pGPPublicKeyRing);
                    this.keys.remove(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()));
                    this.Debug("Deleted public key Id {0}", KeyPairInformation.keyId2Hex(pGPPublicKeyRing.getPublicKey().getKeyID()));
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    break block7;
                }
                l = pGPPublicKeyRing.getPublicKey().getKeyID();
                bl = true;
            }
        }
        if (bl) {
            object = this.getSecretKeyRing(l);
            if (object != null) {
                this.secCollection = PGPSecretKeyRingCollection.removeSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)object);
                this.onRemoveSecretRing((PGPSecretKeyRing)object);
                this.Debug("Deleted private key Id {0}", KeyPairInformation.keyId2Hex(l));
            }
        } else {
            object = collection.iterator();
            if (object.hasNext()) {
                pGPPublicKeyRing = (PGPSecretKeyRing)object.next();
                this.secCollection = PGPSecretKeyRingCollection.removeSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPPublicKeyRing);
                this.onRemoveSecretRing((PGPSecretKeyRing)pGPPublicKeyRing);
                this.Debug("Deleted private key Id {0}", KeyPairInformation.keyId2Hex(pGPPublicKeyRing.getPublicKey().getKeyID()));
            }
        }
        this.save(false);
        return bl;
    }

    public boolean deletePrivateKey(String string) throws PGPException {
        Collection collection = this.getSecretKeyRingCollection(string);
        boolean bl = false;
        Iterator iterator = collection.iterator();
        if (iterator.hasNext()) {
            PGPSecretKeyRing pGPSecretKeyRing = (PGPSecretKeyRing)iterator.next();
            this.secCollection = PGPSecretKeyRingCollection.removeSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPSecretKeyRing);
            this.onRemoveSecretRing(pGPSecretKeyRing);
            this.Debug("Deleted private key Id {0}", KeyPairInformation.keyId2Hex(pGPSecretKeyRing.getPublicKey().getKeyID()));
            bl = true;
        }
        this.save(false);
        return bl;
    }

    public boolean deletePrivateKey(long l) throws PGPException {
        PGPSecretKeyRing pGPSecretKeyRing = this.getSecretKeyRing(l);
        boolean bl = false;
        if (pGPSecretKeyRing != null) {
            this.secCollection = PGPSecretKeyRingCollection.removeSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPSecretKeyRing);
            this.onRemoveSecretRing(pGPSecretKeyRing);
            this.Debug("Deleted private key Id {0}", KeyPairInformation.keyId2Hex(pGPSecretKeyRing.getPublicKey().getKeyID()));
            bl = true;
        }
        this.save(false);
        return bl;
    }

    public boolean deletePublicKey(String string) throws PGPException {
        boolean bl;
        block3: {
            Collection collection = this.getPublicKeyRingCollection(string);
            bl = false;
            Iterator iterator = collection.iterator();
            if (iterator.hasNext()) {
                PGPPublicKeyRing pGPPublicKeyRing = (PGPPublicKeyRing)iterator.next();
                try {
                    this.pubCollection = PGPPublicKeyRingCollection.removePublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
                    this.onRemovePublicRing(pGPPublicKeyRing);
                    this.keys.remove(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()));
                    this.Debug("Deleted public key Id {0}", KeyPairInformation.keyId2Hex(pGPPublicKeyRing.getPublicKey().getKeyID()));
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    break block3;
                }
                bl = true;
            }
        }
        this.save(false);
        return bl;
    }

    public boolean deletePublicKey(long l) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.getPublicKeyRing(l);
        boolean bl = false;
        if (pGPPublicKeyRing != null) {
            try {
                this.pubCollection = PGPPublicKeyRingCollection.removePublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
                this.onRemovePublicRing(pGPPublicKeyRing);
                this.keys.remove(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()));
                this.Debug("Deleted public key Id {0}", KeyPairInformation.keyId2Hex(pGPPublicKeyRing.getPublicKey().getKeyID()));
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
            bl = true;
        }
        this.save(false);
        return bl;
    }

    public void deleteKeyPair(long l) throws PGPException {
        PGPSecretKeyRing pGPSecretKeyRing = this.getSecretKeyRing(l);
        PGPPublicKeyRing pGPPublicKeyRing = this.getPublicKeyRing(l);
        try {
            this.pubCollection = PGPPublicKeyRingCollection.removePublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
            this.onRemovePublicRing(pGPPublicKeyRing);
            this.keys.remove(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()));
            this.Debug("Deleted public key Id {0}", KeyPairInformation.keyId2Hex(pGPPublicKeyRing.getPublicKey().getKeyID()));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        if (pGPSecretKeyRing != null) {
            this.secCollection = PGPSecretKeyRingCollection.removeSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPSecretKeyRing);
            this.onRemoveSecretRing(pGPSecretKeyRing);
            this.Debug("Deleted private key Id {0}", KeyPairInformation.keyId2Hex(pGPSecretKeyRing.getPublicKey().getKeyID()));
        }
        this.save(false);
    }

    public boolean changePrivateKeyPassword(String string, String string2, String string3) throws WrongPasswordException, PGPException {
        Collection collection = this.getSecretKeyRingCollection(string);
        Iterator iterator = collection.iterator();
        if (iterator.hasNext()) {
            PGPSecretKeyRing pGPSecretKeyRing = (PGPSecretKeyRing)iterator.next();
            int n = pGPSecretKeyRing.getSecretKey().getKeyEncryptionAlgorithm();
            try {
                pGPSecretKeyRing = PGPSecretKeyRing.copyWithNewPassword((PGPSecretKeyRing)pGPSecretKeyRing, (PBESecretKeyDecryptor)staticBCFactory.CreatePBESecretKeyDecryptor(string2), (PBESecretKeyEncryptor)staticBCFactory.CreatePBESecretKeyEncryptor(string3, n));
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                if (pGPException.getMessage().startsWith("checksum mismatch at")) {
                    throw new WrongPasswordException(pGPException.getMessage(), pGPException.getUnderlyingException());
                }
                throw IOUtil.newPGPException(pGPException);
            }
            this.replaceSecretKeyRing(pGPSecretKeyRing);
            return true;
        }
        return false;
    }

    public void changePrivateKeyPassword(long l, String string, String string2) throws NoPrivateKeyFoundException, WrongPasswordException, PGPException {
        PGPSecretKeyRing pGPSecretKeyRing = this.findSecretKeyRing(l);
        int n = pGPSecretKeyRing.getSecretKey().getKeyEncryptionAlgorithm();
        try {
            pGPSecretKeyRing = PGPSecretKeyRing.copyWithNewPassword((PGPSecretKeyRing)pGPSecretKeyRing, (PBESecretKeyDecryptor)staticBCFactory.CreatePBESecretKeyDecryptor(string), (PBESecretKeyEncryptor)staticBCFactory.CreatePBESecretKeyEncryptor(string2, n));
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            if (pGPException.getMessage().startsWith("checksum mismatch at")) {
                throw new WrongPasswordException(pGPException.getMessage(), pGPException.getUnderlyingException());
            }
            throw IOUtil.newPGPException(pGPException);
        }
        this.replaceSecretKeyRing(pGPSecretKeyRing);
    }

    public void addUserId(long l, String string, String string2) throws NoPublicKeyFoundException, NoPrivateKeyFoundException, WrongPasswordException, PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.getPublicKeyRing(l);
        if (pGPPublicKeyRing == null) {
            throw new NoPublicKeyFoundException("No public key exists with key Id :" + String.valueOf(l));
        }
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        PGPSignatureGenerator pGPSignatureGenerator = staticBCFactory.CreatePGPSignatureGenerator(pGPPublicKey.getAlgorithm(), 2);
        PGPSecretKeyRing pGPSecretKeyRing = this.getSecretKeyRing(pGPPublicKey.getKeyID());
        if (pGPSecretKeyRing == null) {
            throw new NoPrivateKeyFoundException("No secret key found. You must have the secret key with key Id :" + String.valueOf(l));
        }
        try {
            staticBCFactory.initSign(pGPSignatureGenerator, 19, BaseLib.extractPrivateKey(pGPSecretKeyRing.getSecretKey(), string));
            PGPSignature pGPSignature = pGPSignatureGenerator.generateCertification(string2, pGPPublicKey);
            PGPPublicKey pGPPublicKey2 = PGPPublicKey.addCertification((PGPPublicKey)pGPPublicKey, (String)string2, (PGPSignature)pGPSignature);
            pGPPublicKeyRing = PGPPublicKeyRing.removePublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey);
            pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey2);
        }
        catch (Exception exception) {
            throw new PGPException("creating signature for userId : " + string2, exception);
        }
        this.replacePublicKeyRing(pGPPublicKeyRing);
    }

    public boolean deleteUserId(long l, String string) throws NoPublicKeyFoundException, PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.getPublicKeyRing(l);
        if (pGPPublicKeyRing == null) {
            throw new NoPublicKeyFoundException("No public key exists with key Id :" + String.valueOf(l));
        }
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        PGPPublicKey pGPPublicKey2 = PGPPublicKey.removeCertification((PGPPublicKey)pGPPublicKey, (String)string);
        if (pGPPublicKey2 != null) {
            pGPPublicKeyRing = PGPPublicKeyRing.removePublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey);
            pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey2);
            this.replacePublicKeyRing(pGPPublicKeyRing);
            return true;
        }
        return false;
    }

    public KeyPairInformation clearKeyExpirationTime(String string, String string2) throws NoPublicKeyFoundException, NoPrivateKeyFoundException, PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(string);
        return this.changeKeyExpirationTime(pGPPublicKeyRing, pGPPublicKeyRing.getPublicKey().getKeyID(), string2, 0);
    }

    public KeyPairInformation clearKeyExpirationTime(long l, String string) throws NoPublicKeyFoundException, NoPrivateKeyFoundException, PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(l);
        return this.changeKeyExpirationTime(pGPPublicKeyRing, l, string, 0);
    }

    public KeyPairInformation setKeyExpirationTime(String string, String string2, int n) throws NoPublicKeyFoundException, NoPrivateKeyFoundException, PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(string);
        return this.changeKeyExpirationTime(pGPPublicKeyRing, pGPPublicKeyRing.getPublicKey().getKeyID(), string2, n);
    }

    public KeyPairInformation setKeyExpirationTime(long l, String string, int n) throws NoPublicKeyFoundException, NoPrivateKeyFoundException, PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(l);
        return this.changeKeyExpirationTime(pGPPublicKeyRing, l, string, n);
    }

    private PGPSignatureSubpacketGeneratorExtended copySignature(PGPSignatureSubpacketVector pGPSignatureSubpacketVector, boolean bl) {
        PGPSignatureSubpacketGeneratorExtended pGPSignatureSubpacketGeneratorExtended = new PGPSignatureSubpacketGeneratorExtended();
        if (pGPSignatureSubpacketVector.getFeatures() != null) {
            pGPSignatureSubpacketGeneratorExtended.setFeature(pGPSignatureSubpacketVector.getFeatures().isCritical(), pGPSignatureSubpacketVector.getFeatures().getData()[0]);
        }
        if (pGPSignatureSubpacketVector.getIssuerKeyID() != 0L) {
            pGPSignatureSubpacketGeneratorExtended.setIssuerKeyID(true, pGPSignatureSubpacketVector.getIssuerKeyID());
        }
        if (pGPSignatureSubpacketVector.getKeyFlags() > 0) {
            pGPSignatureSubpacketGeneratorExtended.setKeyFlags(false, pGPSignatureSubpacketVector.getKeyFlags());
        }
        if (pGPSignatureSubpacketVector.getNotationDataOccurences().length > 0) {
            for (int i = 0; i < pGPSignatureSubpacketVector.getNotationDataOccurences().length; ++i) {
                NotationData notationData = pGPSignatureSubpacketVector.getNotationDataOccurences()[i];
                pGPSignatureSubpacketGeneratorExtended.setNotationData(notationData.isCritical(), notationData.isHumanReadable(), notationData.getNotationName(), notationData.getNotationValue());
            }
        }
        if (pGPSignatureSubpacketVector.getPreferredCompressionAlgorithms() != null) {
            pGPSignatureSubpacketGeneratorExtended.setPreferredCompressionAlgorithms(false, pGPSignatureSubpacketVector.getPreferredCompressionAlgorithms());
        }
        if (pGPSignatureSubpacketVector.getPreferredHashAlgorithms() != null) {
            pGPSignatureSubpacketGeneratorExtended.setPreferredHashAlgorithms(false, pGPSignatureSubpacketVector.getPreferredHashAlgorithms());
        }
        if (pGPSignatureSubpacketVector.getPreferredSymmetricAlgorithms() != null) {
            pGPSignatureSubpacketGeneratorExtended.setPreferredSymmetricAlgorithms(false, pGPSignatureSubpacketVector.getPreferredSymmetricAlgorithms());
        }
        if (pGPSignatureSubpacketVector.getSignatureCreationTime() != null) {
            pGPSignatureSubpacketGeneratorExtended.setSignatureCreationTime(false, pGPSignatureSubpacketVector.getSignatureCreationTime());
        }
        if (pGPSignatureSubpacketVector.getSignatureExpirationTime() > 0L) {
            pGPSignatureSubpacketGeneratorExtended.setSignatureExpirationTime(false, pGPSignatureSubpacketVector.getSignatureExpirationTime());
        }
        if (pGPSignatureSubpacketVector.getSignerUserID() != null) {
            pGPSignatureSubpacketGeneratorExtended.setSignerUserID(false, pGPSignatureSubpacketVector.getSignerUserID());
        }
        if (pGPSignatureSubpacketVector.isPrimaryUserID()) {
            pGPSignatureSubpacketGeneratorExtended.setPrimaryUserID(false, true);
        }
        if (bl && pGPSignatureSubpacketVector.getKeyExpirationTime() > 0L) {
            pGPSignatureSubpacketGeneratorExtended.setKeyExpirationTime(false, pGPSignatureSubpacketVector.getKeyExpirationTime());
        }
        return pGPSignatureSubpacketGeneratorExtended;
    }

    private KeyPairInformation changeKeyExpirationTime(PGPPublicKeyRing pGPPublicKeyRing, long l, String string, int n) throws NoPublicKeyFoundException, NoPrivateKeyFoundException, PGPException {
        PGPPublicKey pGPPublicKey = null;
        pGPPublicKey = pGPPublicKeyRing.getPublicKey(l);
        PGPSecretKeyRing pGPSecretKeyRing = this.getSecretKeyRing(pGPPublicKey.getKeyID());
        if (pGPSecretKeyRing == null) {
            throw new NoPrivateKeyFoundException("No secret key found. You must have the secret key with key Id :" + String.valueOf(l));
        }
        Iterator iterator = pGPPublicKey.getSignatures();
        while (iterator.hasNext()) {
            PGPSignatureSubpacketVector pGPSignatureSubpacketVector;
            PGPSignature pGPSignature = (PGPSignature)iterator.next();
            if (!pGPSignature.hasSubpackets() || (pGPSignatureSubpacketVector = pGPSignature.getHashedSubPackets()) == null || pGPSignatureSubpacketVector.getSubpacket(27) == null) continue;
            PGPSignatureSubpacketGeneratorExtended pGPSignatureSubpacketGeneratorExtended = this.copySignature(pGPSignatureSubpacketVector, false);
            if (n > 0) {
                pGPSignatureSubpacketGeneratorExtended.setKeyExpirationTime(false, n * 60 * 60 * 24);
            }
            PGPSignatureSubpacketVector pGPSignatureSubpacketVector2 = pGPSignatureSubpacketGeneratorExtended.generate();
            PGPSignatureGenerator pGPSignatureGenerator = staticBCFactory.CreatePGPSignatureGenerator(pGPSignature.getKeyAlgorithm(), pGPSignature.getHashAlgorithm());
            try {
                PGPPublicKey pGPPublicKey2 = PGPPublicKey.removeCertification((PGPPublicKey)pGPPublicKey, (PGPSignature)pGPSignature);
                staticBCFactory.initSign(pGPSignatureGenerator, pGPSignature.getSignatureType(), BaseLib.extractPrivateKey(pGPSecretKeyRing.getSecretKey(), string));
                pGPSignatureGenerator.setHashedSubpackets(pGPSignatureSubpacketVector2);
                pGPSignatureGenerator.setUnhashedSubpackets(pGPSignature.getUnhashedSubPackets());
                PGPSignature pGPSignature2 = null;
                if (pGPSignatureSubpacketVector.isPrimaryUserID()) {
                    String string2 = null;
                    if (pGPPublicKey.getRawUserIDs().hasNext()) {
                        string2 = BaseLib.toUserID((byte[])pGPPublicKey.getRawUserIDs().next());
                    }
                    pGPSignature2 = pGPSignatureGenerator.generateCertification(string2, pGPPublicKey2);
                    pGPPublicKey2 = PGPPublicKey.addCertification((PGPPublicKey)pGPPublicKey2, (String)string2, (PGPSignature)pGPSignature2);
                } else {
                    pGPSignature2 = pGPSignatureGenerator.generateCertification(pGPPublicKeyRing.getPublicKey(), pGPPublicKey2);
                    pGPPublicKey2 = PGPPublicKey.addCertification((PGPPublicKey)pGPPublicKey2, (PGPSignature)pGPSignature2);
                }
                pGPPublicKeyRing = PGPPublicKeyRing.removePublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey);
                pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey2);
            }
            catch (Exception exception) {
                throw new PGPException("Error changing key expiration time for Key ID : " + pGPPublicKey.getKeyID(), exception);
            }
            this.replacePublicKeyRing(pGPPublicKeyRing);
            return (KeyPairInformation)this.keys.get(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()));
        }
        return null;
    }

    public KeyPairInformation setKeyCertificationType(long l, String string, KeyCertificationType keyCertificationType) throws NoPublicKeyFoundException, NoPrivateKeyFoundException, PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(l);
        return this.changeKeyCertificationType(pGPPublicKeyRing, string, keyCertificationType);
    }

    public KeyPairInformation setKeyCertificationType(String string, String string2, KeyCertificationType keyCertificationType) throws NoPublicKeyFoundException, NoPrivateKeyFoundException, PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(string);
        return this.changeKeyCertificationType(pGPPublicKeyRing, string2, keyCertificationType);
    }

    private KeyPairInformation changeKeyCertificationType(PGPPublicKeyRing pGPPublicKeyRing, String string, KeyCertificationType keyCertificationType) throws NoPrivateKeyFoundException, PGPException {
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        this.replacePublicKeyRing(pGPPublicKeyRing);
        PGPSecretKeyRing pGPSecretKeyRing = this.getSecretKeyRing(pGPPublicKey.getKeyID());
        if (pGPSecretKeyRing == null) {
            throw new NoPrivateKeyFoundException("No secret key found. You must have the secret key with key Id :" + String.valueOf(pGPPublicKey.getKeyID()));
        }
        Iterator iterator = pGPPublicKey.getRawUserIDs();
        while (iterator.hasNext()) {
            String string2 = BaseLib.toUserID((byte[])iterator.next());
            Iterator iterator2 = pGPPublicKey.getSignaturesForID(string2);
            while (iterator2.hasNext()) {
                PGPSignature pGPSignature = (PGPSignature)iterator2.next();
                if (!pGPSignature.hasSubpackets()) continue;
                PGPSignatureSubpacketVector pGPSignatureSubpacketVector = pGPSignature.getHashedSubPackets();
                PGPSignatureGenerator pGPSignatureGenerator = staticBCFactory.CreatePGPSignatureGenerator(pGPSignature.getKeyAlgorithm(), pGPSignature.getHashAlgorithm());
                try {
                    PGPPublicKey pGPPublicKey2 = PGPPublicKey.removeCertification((PGPPublicKey)pGPPublicKey, (PGPSignature)pGPSignature);
                    staticBCFactory.initSign(pGPSignatureGenerator, keyCertificationType.getValue(), BaseLib.extractPrivateKey(pGPSecretKeyRing.getSecretKey(), string));
                    pGPSignatureGenerator.setHashedSubpackets(pGPSignatureSubpacketVector);
                    pGPSignatureGenerator.setUnhashedSubpackets(pGPSignature.getUnhashedSubPackets());
                    PGPSignature pGPSignature2 = null;
                    pGPSignature2 = pGPSignatureGenerator.generateCertification(string2, pGPPublicKey);
                    pGPPublicKey2 = PGPPublicKey.addCertification((PGPPublicKey)pGPPublicKey2, (String)string2, (PGPSignature)pGPSignature2);
                    pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey2);
                }
                catch (Exception exception) {
                    throw new PGPException("Error changing key certification for Key ID : " + pGPPublicKey.getKeyID(), exception);
                }
            }
        }
        this.replacePublicKeyRing(pGPPublicKeyRing);
        return (KeyPairInformation)this.keys.get(new Long(pGPPublicKey.getKeyID()));
    }

    public boolean changeUserId(long l, String string, String string2, String string3) throws NoPublicKeyFoundException, NoPrivateKeyFoundException, WrongPasswordException, PGPException {
        Object object;
        PGPSignature pGPSignature;
        PGPPublicKeyRing pGPPublicKeyRing = this.getPublicKeyRing(l);
        if (pGPPublicKeyRing == null) {
            throw new NoPublicKeyFoundException("No public key exists with key Id :" + String.valueOf(l));
        }
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        PGPSignatureSubpacketVector pGPSignatureSubpacketVector = null;
        PGPSignatureSubpacketVector pGPSignatureSubpacketVector2 = null;
        Iterator iterator = pGPPublicKey.getSignaturesForID(string2);
        while (iterator.hasNext()) {
            pGPSignature = (PGPSignature)iterator.next();
            if (!pGPSignature.hasSubpackets()) continue;
            pGPSignatureSubpacketVector = pGPSignature.getHashedSubPackets();
            pGPSignatureSubpacketVector2 = pGPSignature.getUnhashedSubPackets();
            if (pGPSignatureSubpacketVector == null || pGPSignatureSubpacketVector.getSubpacket(27) == null) continue;
            object = this.copySignature(pGPSignatureSubpacketVector, true);
            pGPSignatureSubpacketVector = ((PGPSignatureSubpacketGeneratorExtended)((Object)object)).generate();
        }
        if ((pGPPublicKey = PGPPublicKey.removeCertification((PGPPublicKey)pGPPublicKey, (String)string2)) != null) {
            try {
                pGPSignature = staticBCFactory.CreatePGPSignatureGenerator(pGPPublicKey.getAlgorithm(), 2);
                if (pGPSignatureSubpacketVector != null) {
                    pGPSignature.setHashedSubpackets(pGPSignatureSubpacketVector);
                }
                if (pGPSignatureSubpacketVector2 != null) {
                    pGPSignature.setUnhashedSubpackets(pGPSignatureSubpacketVector2);
                }
            }
            catch (Exception exception) {
                throw new PGPException("creating signature generator: " + exception, exception);
            }
            object = this.getSecretKeyRing(pGPPublicKey.getKeyID());
            if (object == null) {
                throw new NoPrivateKeyFoundException("No secret key found. You must have the secret key with key Id :" + String.valueOf(l));
            }
            try {
                staticBCFactory.initSign((PGPSignatureGenerator)pGPSignature, 19, BaseLib.extractPrivateKey(object.getSecretKey(), string));
                PGPSignature pGPSignature2 = pGPSignature.generateCertification(string3, pGPPublicKey);
                PGPPublicKey pGPPublicKey2 = PGPPublicKey.addCertification((PGPPublicKey)pGPPublicKey, (String)string3, (PGPSignature)pGPSignature2);
                pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey2);
            }
            catch (Exception exception) {
                throw new PGPException("creating signature for userId : " + string3, exception);
            }
            this.replacePublicKeyRing(pGPPublicKeyRing);
            return true;
        }
        return false;
    }

    public void changePrimaryUserId(long l, String string, String string2) throws NoPublicKeyFoundException, NoPrivateKeyFoundException, WrongPasswordException, PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.getPublicKeyRing(l);
        if (pGPPublicKeyRing == null) {
            throw new NoPublicKeyFoundException("No public key exists with key Id :" + String.valueOf(l));
        }
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        Iterator iterator = pGPPublicKey.getRawUserIDs();
        String string3 = null;
        if (iterator.hasNext()) {
            string3 = BaseLib.toUserID((byte[])iterator.next());
        }
        if (string3 != null) {
            this.changeUserId(l, string, string3, string2);
        } else {
            this.addUserId(l, string, string2);
        }
    }

    public KeyPairInformation generateEccKeyPair(String string, String string2, String string3) throws PGPException {
        return this.generateEccKeyPair(string, string2, string3, new String[]{"ZIP", "ZLIB", "BZIP2", "UNCOMPRESSED"}, new String[]{"SHA512", "SHA384", "SHA256"}, new String[]{"AES_256", "AES_192", "AES_128"});
    }

    private EcCurve.Enum ecCurveFromString(String string) {
        EcCurve.Enum enum_ = EcCurve.Enum.NIST_P_256;
        if ("P256".equalsIgnoreCase(string)) {
            enum_ = EcCurve.Enum.NIST_P_256;
        } else if ("P384".equalsIgnoreCase(string)) {
            enum_ = EcCurve.Enum.NIST_P_384;
        } else if ("P521".equalsIgnoreCase(string)) {
            enum_ = EcCurve.Enum.NIST_P_521;
        } else if ("Brainpool256".equalsIgnoreCase(string)) {
            enum_ = EcCurve.Enum.Brainpool256;
        } else if ("Brainpool384".equalsIgnoreCase(string)) {
            enum_ = EcCurve.Enum.Brainpool384;
        } else if ("Brainpool512".equalsIgnoreCase(string)) {
            enum_ = EcCurve.Enum.Brainpool512;
        } else if ("EdDsa".equalsIgnoreCase(string)) {
            enum_ = EcCurve.Enum.EdDsa;
        } else if ("Curve25519".equalsIgnoreCase(string)) {
            enum_ = EcCurve.Enum.Curve25519;
        } else {
            throw new IllegalArgumentException("The supplied EC Curve parameter is invalid : " + string);
        }
        return enum_;
    }

    public KeyPairInformation generateEccKeyPair(String string, String string2, String string3, long l) throws PGPException {
        EcCurve.Enum enum_ = this.ecCurveFromString(string);
        return this.generateEccKeyPair(enum_, enum_, string2, string3, new CompressionAlgorithm.Enum[]{CompressionAlgorithm.Enum.ZIP, CompressionAlgorithm.Enum.ZLIB, CompressionAlgorithm.Enum.BZIP2, CompressionAlgorithm.Enum.UNCOMPRESSED}, new HashAlgorithm.Enum[]{HashAlgorithm.Enum.SHA512, HashAlgorithm.Enum.SHA384, HashAlgorithm.Enum.SHA256}, new CypherAlgorithm.Enum[]{CypherAlgorithm.Enum.AES_256, CypherAlgorithm.Enum.AES_192, CypherAlgorithm.Enum.AES_128}, l);
    }

    public KeyPairInformation generateEccKeyPair(String string, String string2, String string3, String[] stringArray, String[] stringArray2, String[] stringArray3) throws PGPException {
        EcCurve.Enum enum_ = this.ecCurveFromString(string);
        CypherAlgorithm.Enum[] enumArray = new CypherAlgorithm.Enum[stringArray3.length];
        for (int i = 0; i < enumArray.length; ++i) {
            enumArray[i] = CypherAlgorithm.Enum.fromString(stringArray3[i]);
        }
        CompressionAlgorithm.Enum[] enumArray2 = new CompressionAlgorithm.Enum[stringArray.length];
        for (int i = 0; i < enumArray2.length; ++i) {
            enumArray2[i] = CompressionAlgorithm.Enum.fromString(stringArray[i]);
        }
        HashAlgorithm.Enum[] enumArray3 = new HashAlgorithm.Enum[stringArray2.length];
        for (int i = 0; i < enumArray3.length; ++i) {
            enumArray3[i] = HashAlgorithm.Enum.fromString(stringArray2[i]);
        }
        return this.generateEccKeyPair(enum_, enum_, string2, string3, enumArray2, enumArray3, enumArray, 0L);
    }

    public KeyPairInformation generateEccKeyPair(String string, String string2, String string3, String[] stringArray, String[] stringArray2, String[] stringArray3, long l) throws PGPException {
        int n = 256;
        if ("P256".equalsIgnoreCase(string)) {
            n = 256;
        } else if ("P384".equalsIgnoreCase(string)) {
            n = 384;
        } else if ("P521".equalsIgnoreCase(string)) {
            n = 521;
        } else {
            throw new IllegalArgumentException("The supplied EC Curve parameter is invalid");
        }
        return this.generateKeyPair(n, string2, EC, string3, stringArray, stringArray2, stringArray3, l);
    }

    public KeyPairInformation generateRsaKeyPair(int n, String string, String string2) throws PGPException {
        return this.generateKeyPair(n, string, RSA, string2, new String[]{"ZIP", "UNCOMPRESSED", "ZLIB", "BZIP2"}, new String[]{"SHA256", "SHA384", "SHA512", "SHA1", "MD5", "SHA256"}, new String[]{"CAST5", "TRIPLE_DES", "AES_128", "AES_192", "AES_256", "TWOFISH"});
    }

    public KeyPairInformation generateRsaKeyPair(int n, String string, String string2, int n2) throws PGPException {
        return this.generateKeyPair(n, string, RSA, string2, new String[]{"ZIP", "UNCOMPRESSED", "ZLIB", "BZIP2"}, new String[]{"SHA256", "SHA384", "SHA512", "SHA1", "MD5", "SHA256"}, new String[]{"CAST5", "TRIPLE_DES", "AES_128", "AES_192", "AES_256", "TWOFISH"}, (long)n2);
    }

    public KeyPairInformation generateElGamalKeyPair(int n, String string, String string2) throws PGPException {
        return this.generateKeyPair(n, string, ELGAMAL, string2, new String[]{"ZIP", "UNCOMPRESSED", "ZLIB", "BZIP2"}, new String[]{"SHA256", "SHA384", "SHA512", "SHA1", "MD5", "SHA256"}, new String[]{"CAST5", "TRIPLE_DES", "AES_128", "AES_192", "AES_256", "TWOFISH"});
    }

    public KeyPairInformation generateElGamalKeyPair(int n, int n2, String string, String string2) throws PGPException {
        return this.generateKeyPair(n, n2, string, ELGAMAL, string2, new String[]{"ZIP", "UNCOMPRESSED", "ZLIB", "BZIP2"}, new String[]{"SHA256", "SHA384", "SHA512", "SHA1", "MD5", "SHA256"}, new String[]{"CAST5", "TRIPLE_DES", "AES_128", "AES_192", "AES_256", "TWOFISH"}, 0L);
    }

    public KeyPairInformation generateElGamalKeyPair(int n, String string, String string2, int n2) throws PGPException {
        return this.generateKeyPair(1024, n, string, ELGAMAL, string2, new String[]{"ZIP", "UNCOMPRESSED", "ZLIB", "BZIP2"}, new String[]{"SHA256", "SHA384", "SHA512", "SHA1", "MD5", "SHA256"}, new String[]{"CAST5", "TRIPLE_DES", "AES_128", "AES_192", "AES_256", "TWOFISH"}, (long)n2);
    }

    public KeyPairInformation generateElGamalKeyPair(int n, int n2, String string, String string2, int n3) throws PGPException {
        return this.generateKeyPair(n, n2, string, ELGAMAL, string2, new String[]{"ZIP", "UNCOMPRESSED", "ZLIB", "BZIP2"}, new String[]{"SHA256", "SHA384", "SHA512", "SHA1", "MD5", "SHA256"}, new String[]{"CAST5", "TRIPLE_DES", "AES_128", "AES_192", "AES_256", "TWOFISH"}, (long)n3);
    }

    public KeyPairInformation generateKeyPair(int n, String string, String string2) throws PGPException {
        return this.generateKeyPair(n, string, RSA, string2, new String[]{"ZIP", "UNCOMPRESSED"}, new String[]{"SHA1", "MD5", "SHA256"}, new String[]{"CAST5", "TRIPLE_DES", "AES_128", "AES_192", "AES_256"});
    }

    public KeyPairInformation generateKeyPair(int n, String string, String string2, String string3, String[] stringArray, String[] stringArray2, String[] stringArray3) throws PGPException {
        return this.generateKeyPair(n, string, string2, string3, stringArray, stringArray2, stringArray3, 0L);
    }

    public KeyPairInformation generateKeyPair(int n, String string, String string2, String string3, String[] stringArray, String[] stringArray2, String[] stringArray3, long l) throws PGPException {
        String string4 = KeyStore.implode(",", stringArray);
        String string5 = KeyStore.implode(",", stringArray3);
        String string6 = KeyStore.implode(",", stringArray2);
        return this.generateKeyPair(n, string, string2, string3, string4, string6, string5, l);
    }

    public KeyPairInformation generateKeyPair(int n, int n2, String string, String string2, String string3, String[] stringArray, String[] stringArray2, String[] stringArray3, long l) throws PGPException {
        String string4 = KeyStore.implode(",", stringArray);
        String string5 = KeyStore.implode(",", stringArray3);
        String string6 = KeyStore.implode(",", stringArray2);
        return this.generateKeyPair(n, n2, string, string2, string3, string4, string6, string5, l);
    }

    public KeyPairInformation generateKeyPair(int n, String string, String string2, String string3, String string4, String string5, String string6) throws PGPException {
        return this.generateKeyPair(n, string, string2, string3, string4, string5, string6, 0L);
    }

    public KeyPairInformation generateKeyPair(int n, String string, String string2, String string3, String string4, String string5, String string6, long l) throws PGPException {
        this.Debug("Generating {0} OpenPGP key pair.", string2);
        this.Debug("Primary User Id is {0}", string);
        this.Debug("Key size is {0} bits", String.valueOf(n));
        this.Debug("Preferred cipher algorithms are {0}", string6);
        this.Debug("Preferred hash algorithms are {0}", string5);
        this.Debug("Preferred compression algorithms are {0}", string4);
        int n2 = n;
        PGPKeyRingGenerator pGPKeyRingGenerator = KeyStore.createKeyPairGenerator(n2, n, string, string2, string3, string4, string5, string6, l, this.usePrecomputedPrimes, !this.skipLucasLehmerPrimeTest, this.fastElGamalGeneration, this.defaultCertificationType.getValue());
        PGPSecretKeyRing pGPSecretKeyRing = pGPKeyRingGenerator.generateSecretKeyRing();
        PGPPublicKeyRing pGPPublicKeyRing = pGPKeyRingGenerator.generatePublicKeyRing();
        this.secCollection = PGPSecretKeyRingCollection.addSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPSecretKeyRing);
        this.pubCollection = PGPPublicKeyRingCollection.addPublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
        this.onAddPublicRing(pGPPublicKeyRing);
        KeyPairInformation keyPairInformation = (KeyPairInformation)this.keys.get(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()));
        keyPairInformation.setPublicKeyRing(pGPPublicKeyRing);
        keyPairInformation.setPrivateKeyRing(pGPSecretKeyRing);
        this.keys.put(new Long(pGPSecretKeyRing.getPublicKey().getKeyID()), keyPairInformation);
        this.save(false);
        return keyPairInformation;
    }

    public KeyPairInformation generateEccKeyPair(EcCurve.Enum enum_, String string, String string2, CompressionAlgorithm.Enum[] enumArray, HashAlgorithm.Enum[] enumArray2, CypherAlgorithm.Enum[] enumArray3, long l) throws PGPException {
        return this.generateEccKeyPair(enum_, enum_, string, string2, enumArray, enumArray2, enumArray3, l);
    }

    public KeyPairInformation generateEccKeyPair(EcCurve.Enum enum_, EcCurve.Enum enum_2, String string, String string2, CompressionAlgorithm.Enum[] enumArray, HashAlgorithm.Enum[] enumArray2, CypherAlgorithm.Enum[] enumArray3, long l) throws PGPException {
        this.Debug("Primary User Id is {0}", string);
        this.Debug("EC Curve is {0} bits", enum_.toString());
        PGPKeyRingGenerator pGPKeyRingGenerator = KeyStore.createKeyPairGenerator(enum_, enum_2, string, string2, enumArray, enumArray2, enumArray3, l, this.defaultCertificationType.value);
        PGPSecretKeyRing pGPSecretKeyRing = pGPKeyRingGenerator.generateSecretKeyRing();
        PGPPublicKeyRing pGPPublicKeyRing = pGPKeyRingGenerator.generatePublicKeyRing();
        this.secCollection = PGPSecretKeyRingCollection.addSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPSecretKeyRing);
        this.pubCollection = PGPPublicKeyRingCollection.addPublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
        this.onAddPublicRing(pGPPublicKeyRing);
        KeyPairInformation keyPairInformation = (KeyPairInformation)this.keys.get(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()));
        keyPairInformation.setPublicKeyRing(pGPPublicKeyRing);
        keyPairInformation.setPrivateKeyRing(pGPSecretKeyRing);
        this.keys.put(new Long(pGPSecretKeyRing.getPublicKey().getKeyID()), keyPairInformation);
        this.save(false);
        return keyPairInformation;
    }

    public KeyPairInformation generateKeyPair(int n, int n2, String string, String string2, String string3, String string4, String string5, String string6, long l) throws PGPException {
        this.Debug("Generating {0} OpenPGP key pair.", string2);
        this.Debug("Primary User Id is {0}", string);
        this.Debug("Key size is {0} bits", String.valueOf(n));
        this.Debug("Preferred cipher algorithms are {0}", string6);
        this.Debug("Preferred hash algorithms are {0}", string5);
        this.Debug("Preferred compression algorithms are {0}", string4);
        PGPKeyRingGenerator pGPKeyRingGenerator = KeyStore.createKeyPairGenerator(n, n2, string, string2, string3, string4, string5, string6, l, this.usePrecomputedPrimes, !this.skipLucasLehmerPrimeTest, this.fastElGamalGeneration, this.defaultCertificationType.getValue());
        PGPSecretKeyRing pGPSecretKeyRing = pGPKeyRingGenerator.generateSecretKeyRing();
        PGPPublicKeyRing pGPPublicKeyRing = pGPKeyRingGenerator.generatePublicKeyRing();
        this.secCollection = PGPSecretKeyRingCollection.addSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPSecretKeyRing);
        this.pubCollection = PGPPublicKeyRingCollection.addPublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
        this.onAddPublicRing(pGPPublicKeyRing);
        KeyPairInformation keyPairInformation = (KeyPairInformation)this.keys.get(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()));
        keyPairInformation.setPublicKeyRing(pGPPublicKeyRing);
        keyPairInformation.setPrivateKeyRing(pGPSecretKeyRing);
        this.keys.put(new Long(pGPSecretKeyRing.getPublicKey().getKeyID()), keyPairInformation);
        this.save(false);
        return keyPairInformation;
    }

    static PGPKeyRingGenerator createKeyPairGenerator(int n, int n2, String string, String string2, String string3, String string4, String string5, String string6, long l, boolean bl, boolean bl2, boolean bl3) throws PGPException {
        return KeyStore.createKeyPairGenerator(n, n2, string, string2, string3, string4, string5, string6, l, bl, bl2, bl3, KeyCertificationType.PositiveCertification.getValue());
    }

    static PGPKeyRingGenerator createKeyPairGenerator(int n, int n2, String string, String string2, String string3, String string4, String string5, String string6, long l, boolean bl, boolean bl2, boolean bl3, int n3) throws PGPException {
        List<Integer> list = KeyStore.listOfPrefferedCyphers(string6);
        if (list.contains(new Integer(-1))) {
            throw new InvalidParameterException("Wrong value for parameter 'cipherTypes': " + string6 + ". Must be one of: TRIPLE_DES, CAST5, BLOWFISH, AES_128, AES_192, AES_256, TWOFISH, IDEA, DES, SAFER");
        }
        int[] nArray = new int[list.size()];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[i] = list.get(i);
        }
        String[] stringArray = string5.split(",");
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        for (int i = 0; i < stringArray.length; ++i) {
            String string7 = stringArray[i].trim();
            int n4 = KeyStore.parseHashAlgorithm(string7);
            if (n4 < 0) {
                throw new InvalidParameterException("Wrong value for parameter 'hashingAlgorithmTypes': " + string7 + ". Must be one of: SHA256, SHA384, SHA512, SHA224, SHA1, MD5, RIPEMD160, MD2");
            }
            arrayList.add(new Integer(n4));
        }
        int[] nArray2 = new int[arrayList.size()];
        for (int i = 0; i < nArray2.length; ++i) {
            nArray2[i] = (Integer)arrayList.get(i);
        }
        List<Integer> list2 = KeyStore.listOfPrefferedCompressions(string4);
        if (list2.contains(new Integer(-1))) {
            throw new InvalidParameterException("Wrong value for parameter 'compressionTypes': " + string4 + ". Must be one of: ZLIB, ZIP, UNCOMPRESSED, BZIP2");
        }
        int[] nArray3 = new int[list2.size()];
        for (int i = 0; i < nArray3.length; ++i) {
            nArray3[i] = list2.get(i);
        }
        if (EC.equalsIgnoreCase(string2)) {
            ECKeyPairGenerator eCKeyPairGenerator = new ECKeyPairGenerator();
            X9ECParameters x9ECParameters = NISTNamedCurves.getByName((String)"P-256");
            String string8 = "P-256";
            if (n > 256 && n < 521) {
                x9ECParameters = NISTNamedCurves.getByName((String)"P-384");
                string8 = "P-384";
            } else if (n >= 521) {
                x9ECParameters = NISTNamedCurves.getByName((String)"P-521");
                string8 = "P-521";
            }
            try {
                SecureRandom secureRandom = IOUtil.getSecureRandom();
                ECNamedDomainParameters eCNamedDomainParameters = new ECNamedDomainParameters(NISTNamedCurves.getOID((String)string8), x9ECParameters.getCurve(), x9ECParameters.getG(), x9ECParameters.getN());
                eCKeyPairGenerator.init((KeyGenerationParameters)new ECKeyGenerationParameters((ECDomainParameters)eCNamedDomainParameters, secureRandom));
                AsymmetricCipherKeyPair asymmetricCipherKeyPair = eCKeyPairGenerator.generateKeyPair();
                AsymmetricCipherKeyPair asymmetricCipherKeyPair2 = eCKeyPairGenerator.generateKeyPair();
                BcPGPKeyPair bcPGPKeyPair = new BcPGPKeyPair(19, asymmetricCipherKeyPair, new Date());
                BcPGPKeyPair bcPGPKeyPair2 = new BcPGPKeyPair(18, asymmetricCipherKeyPair2, new Date());
                PGPSignatureSubpacketGenerator pGPSignatureSubpacketGenerator = new PGPSignatureSubpacketGenerator();
                if (l > 0L) {
                    pGPSignatureSubpacketGenerator.setKeyExpirationTime(false, l * 24L * 60L * 60L);
                }
                pGPSignatureSubpacketGenerator.setKeyFlags(false, 3);
                pGPSignatureSubpacketGenerator.setPreferredSymmetricAlgorithms(false, nArray);
                pGPSignatureSubpacketGenerator.setPreferredHashAlgorithms(false, nArray2);
                pGPSignatureSubpacketGenerator.setPreferredCompressionAlgorithms(false, nArray3);
                pGPSignatureSubpacketGenerator.setPrimaryUserID(false, true);
                PGPDigestCalculator pGPDigestCalculator = new BcPGPDigestCalculatorProvider().get(2);
                PGPKeyRingGenerator pGPKeyRingGenerator = new PGPKeyRingGenerator(n3, (PGPKeyPair)bcPGPKeyPair, string, pGPDigestCalculator, pGPSignatureSubpacketGenerator.generate(), null, (PGPContentSignerBuilder)new BcPGPContentSignerBuilder(bcPGPKeyPair.getPublicKey().getAlgorithm(), 10), new BcPBESecretKeyEncryptorBuilder(9, pGPDigestCalculator).build(string3.toCharArray()));
                if (bcPGPKeyPair2 != null) {
                    KeyStore.addEncryptionSubkey(pGPKeyRingGenerator, (PGPKeyPair)bcPGPKeyPair2, nArray, nArray2, nArray3, l);
                }
                return pGPKeyRingGenerator;
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
        }
        if (ELGAMAL.equalsIgnoreCase(string2) || DSA.equalsIgnoreCase(string2)) {
            Object object;
            Object object2;
            BigInteger bigInteger;
            DSAParameterGenerationParameters dSAParameterGenerationParameters;
            DSAParametersGenerator dSAParametersGenerator = new DSAParametersGenerator();
            int n5 = 50;
            if (n < 2048) {
                dSAParametersGenerator.init(n, n5, IOUtil.getSecureRandom());
            } else {
                n = 2048;
                dSAParametersGenerator = new DSAParametersGenerator((Digest)new SHA256Digest());
                int n6 = 256;
                dSAParameterGenerationParameters = new DSAParameterGenerationParameters(n, n6, n5, IOUtil.getSecureRandom());
                dSAParametersGenerator.init(dSAParameterGenerationParameters);
            }
            DSAKeyPairGenerator dSAKeyPairGenerator = new DSAKeyPairGenerator();
            dSAKeyPairGenerator.init((KeyGenerationParameters)new DSAKeyGenerationParameters(IOUtil.getSecureRandom(), dSAParametersGenerator.generateParameters()));
            dSAParameterGenerationParameters = new ElGamalKeyPairGenerator();
            if (n2 == 8192 && bl) {
                bigInteger = new BigInteger(g8192);
                object2 = new BigInteger(p8192, 16);
                object = new ElGamalParameters((BigInteger)object2, bigInteger);
                dSAParameterGenerationParameters.init((KeyGenerationParameters)new ElGamalKeyGenerationParameters(IOUtil.getSecureRandom(), (ElGamalParameters)object));
            } else if (n2 == 6144 && bl) {
                bigInteger = new BigInteger(g6144);
                object2 = new BigInteger(p6144, 16);
                object = new ElGamalParameters((BigInteger)object2, bigInteger);
                dSAParameterGenerationParameters.init((KeyGenerationParameters)new ElGamalKeyGenerationParameters(IOUtil.getSecureRandom(), (ElGamalParameters)object));
            } else if (n2 == 4096 && bl) {
                bigInteger = new BigInteger(g4096);
                object2 = new BigInteger(p4096, 16);
                object = new ElGamalParameters((BigInteger)object2, bigInteger);
                dSAParameterGenerationParameters.init((KeyGenerationParameters)new ElGamalKeyGenerationParameters(IOUtil.getSecureRandom(), (ElGamalParameters)object));
            } else if (n2 == 3072 && bl) {
                bigInteger = new BigInteger(g3072);
                object2 = new BigInteger(p3072, 16);
                object = new ElGamalParameters((BigInteger)object2, bigInteger);
                dSAParameterGenerationParameters.init((KeyGenerationParameters)new ElGamalKeyGenerationParameters(IOUtil.getSecureRandom(), (ElGamalParameters)object));
            } else if (n2 == 2048 && bl) {
                bigInteger = new BigInteger(g2048, 16);
                object2 = new BigInteger(p2048, 16);
                object = new ElGamalParameters((BigInteger)object2, bigInteger);
                dSAParameterGenerationParameters.init((KeyGenerationParameters)new ElGamalKeyGenerationParameters(IOUtil.getSecureRandom(), (ElGamalParameters)object));
            } else if (n2 == 1536 && bl) {
                bigInteger = new BigInteger(g1536, 16);
                object2 = new BigInteger(p1536, 16);
                object = new ElGamalParameters((BigInteger)object2, bigInteger);
                dSAParameterGenerationParameters.init((KeyGenerationParameters)new ElGamalKeyGenerationParameters(IOUtil.getSecureRandom(), (ElGamalParameters)object));
            } else if (n2 == 1024 && bl) {
                bigInteger = new BigInteger(g1024, 16);
                object2 = new BigInteger(p1024, 16);
                object = new ElGamalParameters((BigInteger)object2, bigInteger);
                dSAParameterGenerationParameters.init((KeyGenerationParameters)new ElGamalKeyGenerationParameters(IOUtil.getSecureRandom(), (ElGamalParameters)object));
            } else {
                bigInteger = null;
                if (bl3) {
                    object2 = new FastElGamal(n2 / 8);
                    ((FastElGamal)object2).generateKeys();
                    bigInteger = new ElGamalParameters(((FastElGamal)object2).getPrivateKey().getP(), ((FastElGamal)object2).getPublicKey().getG());
                } else {
                    object2 = new BaseElGamalKeyPairGenerator();
                    object2.setWithLucasLehmerTest(bl2);
                    object = object2.generateParams(n2, IOUtil.getSecureRandom());
                    bigInteger = new ElGamalParameters(object.getP(), object.getG());
                }
                dSAParameterGenerationParameters.init((KeyGenerationParameters)new ElGamalKeyGenerationParameters(IOUtil.getSecureRandom(), (ElGamalParameters)bigInteger));
            }
            bigInteger = dSAParameterGenerationParameters.generateKeyPair();
            try {
                object2 = new BcPGPKeyPair(17, dSAKeyPairGenerator.generateKeyPair(), new Date());
                object = new BcPGPKeyPair(16, (AsymmetricCipherKeyPair)bigInteger, new Date());
                PGPSignatureSubpacketGenerator pGPSignatureSubpacketGenerator = new PGPSignatureSubpacketGenerator();
                if (l > 0L) {
                    pGPSignatureSubpacketGenerator.setKeyExpirationTime(false, l * 24L * 60L * 60L);
                }
                pGPSignatureSubpacketGenerator.setKeyFlags(false, 3);
                pGPSignatureSubpacketGenerator.setPreferredSymmetricAlgorithms(false, nArray);
                pGPSignatureSubpacketGenerator.setPreferredHashAlgorithms(false, nArray2);
                pGPSignatureSubpacketGenerator.setPreferredCompressionAlgorithms(false, nArray3);
                pGPSignatureSubpacketGenerator.setPrimaryUserID(false, true);
                PGPDigestCalculator pGPDigestCalculator = new BcPGPDigestCalculatorProvider().get(2);
                BcPGPContentSignerBuilder bcPGPContentSignerBuilder = new BcPGPContentSignerBuilder(object2.getPublicKey().getAlgorithm(), 2);
                if (n >= 2048) {
                    bcPGPContentSignerBuilder = new BcPGPContentSignerBuilder(object2.getPublicKey().getAlgorithm(), 8);
                }
                PGPKeyRingGenerator pGPKeyRingGenerator = new PGPKeyRingGenerator(n3, (PGPKeyPair)object2, string, pGPDigestCalculator, pGPSignatureSubpacketGenerator.generate(), null, (PGPContentSignerBuilder)bcPGPContentSignerBuilder, new BcPBESecretKeyEncryptorBuilder(9, pGPDigestCalculator).build(string3.toCharArray()));
                KeyStore.addEncryptionSubkey(pGPKeyRingGenerator, (PGPKeyPair)object, nArray, nArray2, nArray3, l);
                return pGPKeyRingGenerator;
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
        }
        if (RSA.equalsIgnoreCase(string2)) {
            RSAKeyPairGenerator rSAKeyPairGenerator = null;
            rSAKeyPairGenerator = new RSAKeyPairGenerator();
            SecureRandom secureRandom = IOUtil.getSecureRandom();
            rSAKeyPairGenerator.init((KeyGenerationParameters)new RSAKeyGenerationParameters(BigInteger.valueOf(65537L), secureRandom, n, 80));
            AsymmetricCipherKeyPair asymmetricCipherKeyPair = rSAKeyPairGenerator.generateKeyPair();
            rSAKeyPairGenerator.init((KeyGenerationParameters)new RSAKeyGenerationParameters(BigInteger.valueOf(65537L), secureRandom, n2, 80));
            AsymmetricCipherKeyPair asymmetricCipherKeyPair3 = rSAKeyPairGenerator.generateKeyPair();
            try {
                BcPGPKeyPair bcPGPKeyPair = new BcPGPKeyPair(KeyStore.algorithmID(RSA), asymmetricCipherKeyPair, new Date());
                BcPGPKeyPair bcPGPKeyPair3 = new BcPGPKeyPair(KeyStore.algorithmID(RSA), asymmetricCipherKeyPair3, new Date());
                PGPSignatureSubpacketGenerator pGPSignatureSubpacketGenerator = new PGPSignatureSubpacketGenerator();
                if (l > 0L) {
                    pGPSignatureSubpacketGenerator.setKeyExpirationTime(false, l * 24L * 60L * 60L);
                }
                pGPSignatureSubpacketGenerator.setKeyFlags(false, 3);
                pGPSignatureSubpacketGenerator.setPreferredSymmetricAlgorithms(false, nArray);
                pGPSignatureSubpacketGenerator.setPreferredHashAlgorithms(false, nArray2);
                pGPSignatureSubpacketGenerator.setPreferredCompressionAlgorithms(false, nArray3);
                pGPSignatureSubpacketGenerator.setPrimaryUserID(false, true);
                PGPDigestCalculator pGPDigestCalculator = new BcPGPDigestCalculatorProvider().get(2);
                PGPKeyRingGenerator pGPKeyRingGenerator = new PGPKeyRingGenerator(n3, (PGPKeyPair)bcPGPKeyPair, string, pGPDigestCalculator, pGPSignatureSubpacketGenerator.generate(), null, (PGPContentSignerBuilder)new BcPGPContentSignerBuilder(bcPGPKeyPair.getPublicKey().getAlgorithm(), 2), new BcPBESecretKeyEncryptorBuilder(9, pGPDigestCalculator).build(string3.toCharArray()));
                if (bcPGPKeyPair3 != null) {
                    KeyStore.addEncryptionSubkey(pGPKeyRingGenerator, (PGPKeyPair)bcPGPKeyPair3, nArray, nArray2, nArray3, l);
                }
                return pGPKeyRingGenerator;
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
        }
        KeyStore.generateKeyPairException("keyAlgorighm", string2, "RSA, ELGAMAL, ECC");
        return null;
    }

    private static X9ECParameters calculateCurveX9(EcCurve.Enum enum_) {
        if (EcCurve.Enum.NIST_P_256.equals((Object)enum_)) {
            return NISTNamedCurves.getByName((String)"P-256");
        }
        if (EcCurve.Enum.NIST_P_384.equals((Object)enum_)) {
            return NISTNamedCurves.getByName((String)"P-384");
        }
        if (EcCurve.Enum.NIST_P_521.equals((Object)enum_)) {
            return NISTNamedCurves.getByName((String)"P-521");
        }
        if (EcCurve.Enum.Brainpool256.equals((Object)enum_)) {
            return TeleTrusTNamedCurves.getByOID((ASN1ObjectIdentifier)TeleTrusTObjectIdentifiers.brainpoolP256r1);
        }
        if (EcCurve.Enum.Brainpool384.equals((Object)enum_)) {
            return TeleTrusTNamedCurves.getByOID((ASN1ObjectIdentifier)TeleTrusTObjectIdentifiers.brainpoolP384r1);
        }
        if (EcCurve.Enum.Brainpool512.equals((Object)enum_)) {
            return TeleTrusTNamedCurves.getByOID((ASN1ObjectIdentifier)TeleTrusTObjectIdentifiers.brainpoolP512r1);
        }
        return NISTNamedCurves.getByName((String)"P-256");
    }

    private static ASN1ObjectIdentifier calculateCurveASN1(EcCurve.Enum enum_) {
        if (EcCurve.Enum.NIST_P_256.equals((Object)enum_)) {
            return NISTNamedCurves.getOID((String)"P-256");
        }
        if (EcCurve.Enum.NIST_P_384.equals((Object)enum_)) {
            return NISTNamedCurves.getOID((String)"P-384");
        }
        if (EcCurve.Enum.NIST_P_521.equals((Object)enum_)) {
            return NISTNamedCurves.getOID((String)"P-521");
        }
        if (EcCurve.Enum.Brainpool256.equals((Object)enum_)) {
            return TeleTrusTObjectIdentifiers.brainpoolP256r1;
        }
        if (EcCurve.Enum.Brainpool384.equals((Object)enum_)) {
            return TeleTrusTObjectIdentifiers.brainpoolP384r1;
        }
        if (EcCurve.Enum.Brainpool512.equals((Object)enum_)) {
            return TeleTrusTObjectIdentifiers.brainpoolP512r1;
        }
        return NISTNamedCurves.getOID((String)"P-256");
    }

    static EcCurve.Enum calculateCurve(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        if (NISTNamedCurves.getOID((String)"P-256").equals((ASN1Primitive)aSN1ObjectIdentifier)) {
            return EcCurve.Enum.NIST_P_256;
        }
        if (NISTNamedCurves.getOID((String)"P-384").equals((ASN1Primitive)aSN1ObjectIdentifier)) {
            return EcCurve.Enum.NIST_P_384;
        }
        if (NISTNamedCurves.getOID((String)"P-521").equals((ASN1Primitive)aSN1ObjectIdentifier)) {
            return EcCurve.Enum.NIST_P_521;
        }
        if (TeleTrusTObjectIdentifiers.brainpoolP256r1.equals((ASN1Primitive)aSN1ObjectIdentifier)) {
            return EcCurve.Enum.Brainpool256;
        }
        if (TeleTrusTObjectIdentifiers.brainpoolP384r1.equals((ASN1Primitive)aSN1ObjectIdentifier)) {
            return EcCurve.Enum.Brainpool384;
        }
        if (TeleTrusTObjectIdentifiers.brainpoolP512r1.equals((ASN1Primitive)aSN1ObjectIdentifier)) {
            return EcCurve.Enum.Brainpool512;
        }
        if (GNUObjectIdentifiers.Ed25519.equals((ASN1Primitive)aSN1ObjectIdentifier)) {
            return EcCurve.Enum.EdDsa;
        }
        if (CryptlibObjectIdentifiers.curvey25519.equals((ASN1Primitive)aSN1ObjectIdentifier)) {
            return EcCurve.Enum.Curve25519;
        }
        return EcCurve.Enum.None;
    }

    static PGPKeyRingGenerator createKeyPairGenerator(EcCurve.Enum enum_, EcCurve.Enum enum_2, String string, String string2, CompressionAlgorithm.Enum[] enumArray, HashAlgorithm.Enum[] enumArray2, CypherAlgorithm.Enum[] enumArray3, long l, int n) throws PGPException {
        int[] nArray = new int[enumArray3.length];
        for (int i = 0; i < enumArray3.length; ++i) {
            nArray[i] = enumArray3[i].intValue();
        }
        int[] nArray2 = new int[enumArray2.length];
        for (int i = 0; i < enumArray2.length; ++i) {
            nArray2[i] = enumArray2[i].intValue();
        }
        int[] nArray3 = new int[enumArray.length];
        for (int i = 0; i < nArray3.length; ++i) {
            nArray3[i] = enumArray[i].intValue();
        }
        ECKeyPairGenerator eCKeyPairGenerator = new ECKeyPairGenerator();
        ASN1ObjectIdentifier aSN1ObjectIdentifier = KeyStore.calculateCurveASN1(enum_);
        X9ECParameters x9ECParameters = KeyStore.calculateCurveX9(enum_);
        ASN1ObjectIdentifier aSN1ObjectIdentifier2 = KeyStore.calculateCurveASN1(enum_2);
        X9ECParameters x9ECParameters2 = KeyStore.calculateCurveX9(enum_2);
        try {
            AsymmetricCipherKeyPair asymmetricCipherKeyPair;
            Ed25519KeyPairGenerator ed25519KeyPairGenerator;
            SecureRandom secureRandom = IOUtil.getSecureRandom();
            BcPGPKeyPair bcPGPKeyPair = null;
            BcPGPKeyPair bcPGPKeyPair2 = null;
            if (enum_.equals("EdDsa") || enum_.equals("Curve25519")) {
                ed25519KeyPairGenerator = new Ed25519KeyPairGenerator();
                ed25519KeyPairGenerator.init((KeyGenerationParameters)new Ed25519KeyGenerationParameters(null));
                bcPGPKeyPair = new BcPGPKeyPair(22, ed25519KeyPairGenerator.generateKeyPair(), new Date());
            } else {
                ed25519KeyPairGenerator = new ECNamedDomainParameters(aSN1ObjectIdentifier, x9ECParameters.getCurve(), x9ECParameters.getG(), x9ECParameters.getN());
                eCKeyPairGenerator.init((KeyGenerationParameters)new ECKeyGenerationParameters((ECDomainParameters)ed25519KeyPairGenerator, secureRandom));
                asymmetricCipherKeyPair = eCKeyPairGenerator.generateKeyPair();
                bcPGPKeyPair = KeyStore.newBcPGKeyPair(19, asymmetricCipherKeyPair, aSN1ObjectIdentifier, x9ECParameters);
            }
            if (enum_2.equals("EdDsa") || enum_2.equals("Curve25519")) {
                ed25519KeyPairGenerator = new X25519KeyPairGenerator();
                ed25519KeyPairGenerator.init((KeyGenerationParameters)new X25519KeyGenerationParameters(null));
                bcPGPKeyPair2 = new BcPGPKeyPair(18, ed25519KeyPairGenerator.generateKeyPair(), new Date());
            } else {
                ed25519KeyPairGenerator = new ECNamedDomainParameters(aSN1ObjectIdentifier2, x9ECParameters2.getCurve(), x9ECParameters2.getG(), x9ECParameters2.getN());
                eCKeyPairGenerator.init((KeyGenerationParameters)new ECKeyGenerationParameters((ECDomainParameters)ed25519KeyPairGenerator, secureRandom));
                asymmetricCipherKeyPair = eCKeyPairGenerator.generateKeyPair();
                bcPGPKeyPair2 = KeyStore.newBcPGKeyPair(18, asymmetricCipherKeyPair, aSN1ObjectIdentifier2, x9ECParameters2);
            }
            ed25519KeyPairGenerator = new PGPSignatureSubpacketGenerator();
            if (l > 0L) {
                ed25519KeyPairGenerator.setKeyExpirationTime(false, l * 24L * 60L * 60L);
            }
            ed25519KeyPairGenerator.setKeyFlags(false, 3);
            ed25519KeyPairGenerator.setPreferredSymmetricAlgorithms(false, nArray);
            ed25519KeyPairGenerator.setPreferredHashAlgorithms(false, nArray2);
            ed25519KeyPairGenerator.setPreferredCompressionAlgorithms(false, nArray3);
            ed25519KeyPairGenerator.setPrimaryUserID(false, true);
            asymmetricCipherKeyPair = new BcPGPDigestCalculatorProvider().get(2);
            PGPKeyRingGenerator pGPKeyRingGenerator = new PGPKeyRingGenerator(n, (PGPKeyPair)bcPGPKeyPair, string, (PGPDigestCalculator)asymmetricCipherKeyPair, ed25519KeyPairGenerator.generate(), null, (PGPContentSignerBuilder)new BcPGPContentSignerBuilder(bcPGPKeyPair.getPublicKey().getAlgorithm(), 10), new BcPBESecretKeyEncryptorBuilder(9, (PGPDigestCalculator)asymmetricCipherKeyPair).build(string2.toCharArray()));
            if (bcPGPKeyPair2 != null) {
                KeyStore.addEncryptionSubkey(pGPKeyRingGenerator, (PGPKeyPair)bcPGPKeyPair2, nArray, nArray2, nArray3, l);
            }
            return pGPKeyRingGenerator;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    static PGPKeyPair newBcPGKeyPair(int n, AsymmetricCipherKeyPair asymmetricCipherKeyPair, ASN1ObjectIdentifier aSN1ObjectIdentifier, X9ECParameters x9ECParameters) throws PGPException {
        ECDSAPublicBCPGKey eCDSAPublicBCPGKey;
        PGPKdfParameters pGPKdfParameters;
        SubjectPublicKeyInfo subjectPublicKeyInfo;
        try {
            subjectPublicKeyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo((AsymmetricKeyParameter)asymmetricCipherKeyPair.getPublic());
        }
        catch (IOException iOException) {
            throw new PGPException("Unable to encode key: " + iOException.getMessage(), iOException);
        }
        DEROctetString dEROctetString = new DEROctetString(subjectPublicKeyInfo.getPublicKeyData().getBytes());
        X9ECPoint x9ECPoint = new X9ECPoint(x9ECParameters.getCurve(), (ASN1OctetString)dEROctetString);
        if (n == 18) {
            pGPKdfParameters = new PGPKdfParameters(8, 7);
            eCDSAPublicBCPGKey = new ECDHPublicBCPGKey(aSN1ObjectIdentifier, x9ECPoint.getPoint(), pGPKdfParameters.getHashAlgorithm(), pGPKdfParameters.getSymmetricWrapAlgorithm());
        } else if (n == 19) {
            eCDSAPublicBCPGKey = new ECDSAPublicBCPGKey(aSN1ObjectIdentifier, x9ECPoint.getPoint());
        } else {
            throw new PGPException("unknown EC algorithm");
        }
        try {
            pGPKdfParameters = new PGPPublicKey(new PublicKeyPacket(n, new Date(), (BCPGKey)eCDSAPublicBCPGKey), (KeyFingerPrintCalculator)new BcKeyFingerprintCalculator());
            PGPPrivateKey pGPPrivateKey = new BcPGPKeyConverter().getPGPPrivateKey((PGPPublicKey)pGPKdfParameters, asymmetricCipherKeyPair.getPrivate());
            PGPKeyPair pGPKeyPair = new PGPKeyPair((PGPPublicKey)pGPKdfParameters, pGPPrivateKey);
            return pGPKeyPair;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw new PGPException(pGPException.getMessage(), (Exception)((Object)pGPException));
        }
    }

    static void generateKeyPairException(String string, String string2, String string3) throws PGPException {
        throw new PGPException("Wrong value for parameter " + string + ": " + string2 + ". Must be one of " + string3);
    }

    public void exportKeyRing(String string, String string2) throws NoPublicKeyFoundException, IOException {
        this.exportKeyRing(string, string2, true);
    }

    public void exportPubring(String string) throws IOException {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(string);
            this.pubCollection.encode((OutputStream)fileOutputStream);
        }
        finally {
            if (fileOutputStream != null) {
                ((OutputStream)fileOutputStream).close();
            }
        }
    }

    public void exportSecring(String string) throws IOException {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(string);
            this.secCollection.encode((OutputStream)fileOutputStream);
        }
        finally {
            if (fileOutputStream != null) {
                ((OutputStream)fileOutputStream).close();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void exportKeyRing(String string, String string2, boolean bl) throws NoPublicKeyFoundException, IOException {
        boolean bl2 = false;
        FileOutputStream fileOutputStream = null;
        OutputStream outputStream = null;
        try {
            fileOutputStream = new FileOutputStream(string);
            this.exportKeyRing((OutputStream)fileOutputStream, string2, bl);
        }
        catch (Exception exception) {
            try {
                bl2 = true;
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(fileOutputStream);
                IOUtil.closeStream(outputStream);
                if (bl2) {
                    new File(string).delete();
                }
                throw throwable;
            }
            IOUtil.closeStream(fileOutputStream);
            IOUtil.closeStream(outputStream);
            if (bl2) {
                new File(string).delete();
            }
        }
        IOUtil.closeStream(fileOutputStream);
        IOUtil.closeStream(outputStream);
        if (bl2) {
            new File(string).delete();
        }
    }

    public void exportKeyRing(OutputStream outputStream, String string, boolean bl) throws NoPublicKeyFoundException, IOException {
        OutputStream outputStream2 = null;
        try {
            PGPSecretKeyRing pGPSecretKeyRing;
            if (bl) {
                outputStream2 = outputStream;
                outputStream = new ArmoredOutputStream(outputStream2);
                this.setAsciiVersionHeader(outputStream);
            }
            try {
                pGPSecretKeyRing = this.findSecretKeyRing(string);
                pGPSecretKeyRing.encode(outputStream);
                if (bl) {
                    IOUtil.closeStream(outputStream);
                }
            }
            catch (NoPrivateKeyFoundException noPrivateKeyFoundException) {
                // empty catch block
            }
            if (bl) {
                outputStream = new ArmoredOutputStream(outputStream2);
                this.setAsciiVersionHeader(outputStream);
            }
            pGPSecretKeyRing = this.findPublicKeyRing(string);
            pGPSecretKeyRing.encode(outputStream);
        }
        catch (IOException iOException) {
            throw iOException;
        }
        finally {
            if (bl) {
                IOUtil.closeStream(outputStream);
            }
        }
    }

    public void exportKeyRing(OutputStream outputStream, long l, boolean bl) throws NoPublicKeyFoundException, IOException {
        OutputStream outputStream2 = null;
        try {
            PGPSecretKeyRing pGPSecretKeyRing;
            if (bl) {
                outputStream2 = outputStream;
                outputStream = new ArmoredOutputStream(outputStream2);
                this.setAsciiVersionHeader(outputStream);
            }
            try {
                pGPSecretKeyRing = this.findSecretKeyRing(l);
                pGPSecretKeyRing.encode(outputStream);
                if (bl) {
                    IOUtil.closeStream(outputStream);
                }
            }
            catch (NoPrivateKeyFoundException noPrivateKeyFoundException) {
                // empty catch block
            }
            if (bl) {
                outputStream = new ArmoredOutputStream(outputStream2);
                this.setAsciiVersionHeader(outputStream);
            }
            pGPSecretKeyRing = this.findPublicKeyRing(l);
            pGPSecretKeyRing.encode(outputStream);
        }
        catch (IOException iOException) {
            throw iOException;
        }
        finally {
            if (bl) {
                IOUtil.closeStream(outputStream);
            }
        }
    }

    public void exportKeyRing(String string, long l, boolean bl) throws NoPublicKeyFoundException, IOException {
        FileOutputStream fileOutputStream = null;
        FileOutputStream fileOutputStream2 = null;
        try {
            PGPSecretKeyRing pGPSecretKeyRing;
            fileOutputStream = new FileOutputStream(string);
            if (bl) {
                fileOutputStream2 = fileOutputStream;
                fileOutputStream = new ArmoredOutputStream((OutputStream)fileOutputStream2);
                this.setAsciiVersionHeader(fileOutputStream);
            }
            try {
                pGPSecretKeyRing = this.findSecretKeyRing(l);
                pGPSecretKeyRing.encode((OutputStream)fileOutputStream);
                if (bl) {
                    IOUtil.closeStream(fileOutputStream);
                }
            }
            catch (NoPrivateKeyFoundException noPrivateKeyFoundException) {
                // empty catch block
            }
            if (bl) {
                fileOutputStream = new ArmoredOutputStream((OutputStream)fileOutputStream2);
                this.setAsciiVersionHeader(fileOutputStream);
            }
            pGPSecretKeyRing = this.findPublicKeyRing(l);
            pGPSecretKeyRing.encode((OutputStream)fileOutputStream);
        }
        catch (IOException iOException) {
            try {
                throw iOException;
            }
            catch (Throwable throwable) {
                IOUtil.closeStream(fileOutputStream);
                IOUtil.closeStream(fileOutputStream2);
                throw throwable;
            }
        }
        IOUtil.closeStream(fileOutputStream);
        IOUtil.closeStream(fileOutputStream2);
    }

    public void exportPublicKey(String string, String string2, boolean bl) throws NoPublicKeyFoundException, IOException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(string2);
        IOUtil.exportPublicKeyRing(pGPPublicKeyRing, string, bl, this.asciiVersionHeader);
    }

    public void exportPublicKey(OutputStream outputStream, String string, boolean bl) throws NoPublicKeyFoundException, IOException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(string);
        IOUtil.exportPublicKeyRing(pGPPublicKeyRing, outputStream, bl, this.asciiVersionHeader);
    }

    public void exportPublicKey(OutputStream outputStream, long l, boolean bl) throws NoPublicKeyFoundException, IOException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(l);
        IOUtil.exportPublicKeyRing(pGPPublicKeyRing, outputStream, bl, this.asciiVersionHeader);
    }

    public void exportPublicKey(String string, long l, boolean bl) throws NoPublicKeyFoundException, IOException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(l);
        IOUtil.exportPublicKeyRing(pGPPublicKeyRing, string, bl, this.asciiVersionHeader);
    }

    public void exportPrivateKey(String string, String string2, boolean bl) throws NoPrivateKeyFoundException, IOException {
        PGPSecretKeyRing pGPSecretKeyRing = this.findSecretKeyRing(string2);
        IOUtil.exportPrivateKey(pGPSecretKeyRing, string, bl, this.asciiVersionHeader);
    }

    public void exportPrivateKey(OutputStream outputStream, String string, boolean bl) throws NoPrivateKeyFoundException, IOException {
        PGPSecretKeyRing pGPSecretKeyRing = this.findSecretKeyRing(string);
        IOUtil.exportPrivateKey(pGPSecretKeyRing, outputStream, bl, this.asciiVersionHeader);
    }

    public void exportPrivateKey(OutputStream outputStream, long l, boolean bl) throws NoPrivateKeyFoundException, IOException {
        PGPSecretKeyRing pGPSecretKeyRing = this.findSecretKeyRing(l);
        IOUtil.exportPrivateKey(pGPSecretKeyRing, outputStream, bl, this.asciiVersionHeader);
    }

    public void exportPrivateKey(String string, long l, boolean bl) throws NoPrivateKeyFoundException, IOException {
        PGPSecretKeyRing pGPSecretKeyRing = this.findSecretKeyRing(l);
        IOUtil.exportPrivateKey(pGPSecretKeyRing, string, bl, this.asciiVersionHeader);
    }

    public KeyPairInformation importPublicKey(KeyPairInformation keyPairInformation) {
        try {
            this.onAddPublicRing(keyPairInformation.getRawPublicKeyRing());
            if (this.pubCollection.contains(keyPairInformation.getKeyID())) {
                this.pubCollection = PGPPublicKeyRingCollection.removePublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)keyPairInformation.getRawPublicKeyRing());
            }
            this.pubCollection = PGPPublicKeyRingCollection.addPublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)keyPairInformation.getRawPublicKeyRing());
            KeyPairInformation keyPairInformation2 = new KeyPairInformation();
            keyPairInformation2.setPublicKeyRing(keyPairInformation.getRawPublicKeyRing());
            this.keys.put(new Long(keyPairInformation.getKeyID()), keyPairInformation2);
            return keyPairInformation2;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            return null;
        }
    }

    public KeyPairInformation[] importPublicKey(String string) throws IOException, PGPException, NoPublicKeyFoundException {
        List list = this.loadKeyFile(string);
        return this.importPublicKey(list);
    }

    public KeyPairInformation[] importPublicKey(InputStream inputStream) throws IOException, PGPException, NoPublicKeyFoundException {
        List list = KeyStore.loadKeyStream(inputStream);
        return this.importPublicKey(list);
    }

    private KeyPairInformation[] importPublicKey(List list) throws IOException, PGPException, NoPublicKeyFoundException {
        LinkedList<KeyPairInformation> linkedList = new LinkedList<KeyPairInformation>();
        boolean bl = false;
        for (int i = 0; i < list.size(); ++i) {
            Object e = list.get(i);
            if (!(e instanceof PGPPublicKeyRing)) continue;
            bl = true;
            PGPPublicKeyRing pGPPublicKeyRing = (PGPPublicKeyRing)e;
            try {
                if (this.pubCollection.contains(pGPPublicKeyRing.getPublicKey().getKeyID())) {
                    this.pubCollection = PGPPublicKeyRingCollection.removePublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
                }
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                // empty catch block
            }
            this.pubCollection = PGPPublicKeyRingCollection.addPublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
            this.onAddPublicRing(pGPPublicKeyRing);
            KeyPairInformation keyPairInformation = new KeyPairInformation(this.asciiVersionHeader);
            keyPairInformation.setPublicKeyRing(pGPPublicKeyRing);
            linkedList.add(keyPairInformation);
            this.Debug("Imported public key {0}", keyPairInformation.getKeyIDHex());
        }
        if (!bl) {
            throw new NoPublicKeyFoundException("No public key was found in the supplied source.");
        }
        this.save(false);
        return linkedList.toArray(new KeyPairInformation[linkedList.size()]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public KeyPairInformation[] importGnuPgKbx(String string) throws IOException, PGPException {
        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\me\\AppData\\Roaming\\gnupg\\pubring.kbx");
        try {
            KeyPairInformation[] keyPairInformationArray = this.importGnuPgKbx(fileInputStream);
            return keyPairInformationArray;
        }
        finally {
            IOUtil.closeStream(fileInputStream);
        }
    }

    public KeyPairInformation[] importGnuPgKbx(InputStream inputStream) throws IOException, PGPException {
        KeyStore keyStore = new KeyStore();
        InputStream inputStream2 = inputStream;
        KBXFirstBlob kBXFirstBlob = new KBXFirstBlob(inputStream2);
        KBXDataBlob kBXDataBlob = null;
        while ((kBXDataBlob = KBXDataBlob.readFromStream(inputStream2)) != null) {
            keyStore.importKeyRing(kBXDataBlob.Blob);
        }
        this.importKeyStore(keyStore);
        return keyStore.getKeys();
    }

    public KeyPairInformation[] importKeyRing(String string) throws IOException, PGPException {
        return this.importKeyRing(string, null);
    }

    public KeyPairInformation[] importKeyStore(KeyStore keyStore) throws IOException, PGPException {
        int n;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(keyStore.pubCollection.getEncoded());
        ByteArrayInputStream byteArrayInputStream2 = new ByteArrayInputStream(keyStore.secCollection.getEncoded());
        KeyPairInformation[] keyPairInformationArray = this.importKeyRing(byteArrayInputStream);
        KeyPairInformation[] keyPairInformationArray2 = this.importKeyRing(byteArrayInputStream2);
        ArrayList<KeyPairInformation> arrayList = new ArrayList<KeyPairInformation>(keyPairInformationArray.length + keyPairInformationArray2.length);
        boolean[] blArray = new boolean[keyPairInformationArray2.length];
        java.util.Arrays.fill(blArray, false);
        for (n = 0; n < keyPairInformationArray.length; ++n) {
            KeyPairInformation keyPairInformation = keyPairInformationArray[n];
            for (int i = 0; i < keyPairInformationArray2.length; ++i) {
                KeyPairInformation keyPairInformation2 = keyPairInformationArray2[i];
                if (keyPairInformation.getKeyID() != keyPairInformation2.getKeyID()) continue;
                blArray[i] = true;
                keyPairInformation.setPrivateKeyRing(keyPairInformation2.getRawPrivateKeyRing());
                break;
            }
            arrayList.add(keyPairInformation);
        }
        for (n = 0; n < blArray.length; ++n) {
            if (blArray[n]) continue;
            arrayList.add(keyPairInformationArray2[n]);
        }
        return arrayList.toArray(new KeyPairInformation[arrayList.size()]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public KeyPairInformation[] importKeyRing(byte[] byArray) throws IOException, PGPException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byArray);
        try {
            KeyPairInformation[] keyPairInformationArray = this.importKeyRing(byteArrayInputStream, null);
            return keyPairInformationArray;
        }
        finally {
            IOUtil.closeStream(byteArrayInputStream);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public KeyPairInformation[] importKeyRing(String string, String string2) throws IOException, PGPException {
        KeyPairInformation[] keyPairInformationArray;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string);
            keyPairInformationArray = this.importKeyRing(fileInputStream, string2);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        return keyPairInformationArray;
    }

    public KeyPairInformation importKeyRing(KeyPairInformation keyPairInformation) {
        try {
            this.onAddPublicRing(keyPairInformation.getRawPublicKeyRing());
            if (this.pubCollection.contains(keyPairInformation.getKeyID())) {
                this.pubCollection = PGPPublicKeyRingCollection.removePublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)keyPairInformation.getRawPublicKeyRing());
            }
            this.pubCollection = PGPPublicKeyRingCollection.addPublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)keyPairInformation.getRawPublicKeyRing());
            if (this.secCollection.contains(keyPairInformation.getKeyID())) {
                this.secCollection = PGPSecretKeyRingCollection.removeSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)keyPairInformation.getRawPrivateKeyRing());
            }
            this.secCollection = PGPSecretKeyRingCollection.addSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)keyPairInformation.getRawPrivateKeyRing());
            this.keys.put(new Long(keyPairInformation.getKeyID()), keyPairInformation);
            this.Debug("Imported key {0}", keyPairInformation.getKeyIDHex());
            return keyPairInformation;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            return null;
        }
    }

    public KeyPairInformation[] importKeyRing(InputStream inputStream) throws IOException, PGPException {
        return this.importKeyRing(inputStream, null);
    }

    public KeyPairInformation[] importKeyRing(InputStream inputStream, String string) throws IOException, PGPException {
        this.Debug("Importing OpePGP key ring.");
        InputStream inputStream2 = inputStream;
        if (!(inputStream instanceof ArmoredInputStream)) {
            inputStream2 = BaseLib.cleanGnuPGBackupKeys(inputStream);
            inputStream2 = PGPUtil.getDecoderStream((InputStream)inputStream2);
        }
        HashMap<String, KeyPairInformation> hashMap = new HashMap<String, KeyPairInformation>();
        BoolValue boolValue = new BoolValue(false);
        if (inputStream2 instanceof ArmoredInputStream) {
            ArmoredInputStream armoredInputStream = (ArmoredInputStream)inputStream2;
            while (!armoredInputStream.isEndOfStream()) {
                List list = this.parseKeyStream((InputStream)armoredInputStream, string, boolValue);
                for (int i = 0; i < list.size(); ++i) {
                    KeyPairInformation keyPairInformation = (KeyPairInformation)list.get(i);
                    hashMap.put(keyPairInformation.getKeyIDHex(), keyPairInformation);
                    this.Debug("Imported key {0}", keyPairInformation.getKeyIDHex());
                }
                if (!boolValue.isValue()) continue;
                break;
            }
        } else {
            List list = this.parseKeyStream(inputStream2, string, boolValue);
            for (int i = 0; i < list.size(); ++i) {
                KeyPairInformation keyPairInformation = (KeyPairInformation)list.get(i);
                hashMap.put(keyPairInformation.getKeyIDHex(), keyPairInformation);
                this.Debug("Imported key {0}", keyPairInformation.getKeyIDHex());
            }
        }
        this.save(false);
        return hashMap.values().toArray(new KeyPairInformation[hashMap.size()]);
    }

    public KeyPairInformation[] importPrivateKey(String string) throws IOException, PGPException, NoPrivateKeyFoundException {
        return this.importPrivateKey(string, null);
    }

    public KeyPairInformation importKey(InputStream inputStream) throws IOException, PGPException {
        List list = KeyStore.loadKeyStream(inputStream);
        return this.importKey(list);
    }

    public KeyPairInformation importKey(String string) throws IOException, PGPException {
        List list = this.loadKeyFile(string);
        return this.importKey(list);
    }

    public KeyPairInformation[] importPrivateKey(String string, String string2) throws IOException, PGPException, NoPrivateKeyFoundException {
        List list = this.loadKeyFile(string);
        return this.importPrivateKey(list, string2);
    }

    public KeyPairInformation[] importPrivateKey(InputStream inputStream) throws IOException, PGPException, NoPrivateKeyFoundException {
        List list = KeyStore.loadKeyStream(inputStream);
        return this.importPrivateKey(list, null);
    }

    public KeyPairInformation[] importPrivateKey(InputStream inputStream, String string) throws IOException, PGPException, NoPrivateKeyFoundException {
        List list = KeyStore.loadKeyStream(inputStream);
        return this.importPrivateKey(list, string);
    }

    private KeyPairInformation[] importPrivateKey(List list, String string) throws IOException, PGPException, NoPrivateKeyFoundException {
        LinkedList<KeyPairInformation> linkedList = new LinkedList<KeyPairInformation>();
        boolean bl = false;
        for (int i = 0; i < list.size(); ++i) {
            Object e = list.get(i);
            if (!(e instanceof PGPSecretKeyRing)) continue;
            bl = true;
            PGPSecretKeyRing pGPSecretKeyRing = (PGPSecretKeyRing)e;
            if (string == null || this.isKeyPasswordConfirmed(pGPSecretKeyRing, string)) {
                Object object;
                try {
                    if (this.secCollection.contains(pGPSecretKeyRing.getPublicKey().getKeyID())) {
                        this.secCollection = PGPSecretKeyRingCollection.removeSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPSecretKeyRing);
                    }
                }
                catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                    // empty catch block
                }
                this.secCollection = PGPSecretKeyRingCollection.addSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPSecretKeyRing);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(0x100000);
                Iterator iterator = pGPSecretKeyRing.getSecretKeys();
                while (iterator.hasNext()) {
                    object = ((PGPSecretKey)iterator.next()).getPublicKey();
                    if (object == null) continue;
                    byteArrayOutputStream.write(object.getEncoded());
                }
                iterator = staticBCFactory.CreatePGPPublicKeyRing(byteArrayOutputStream.toByteArray());
                try {
                    if (!this.pubCollection.contains(pGPSecretKeyRing.getPublicKey().getKeyID())) {
                        this.pubCollection = PGPPublicKeyRingCollection.addPublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)iterator);
                        this.onAddPublicRing((PGPPublicKeyRing)iterator);
                    }
                }
                catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                    // empty catch block
                }
                object = new Long(pGPSecretKeyRing.getPublicKey().getKeyID());
                KeyPairInformation keyPairInformation = (KeyPairInformation)this.keys.get(object);
                if (keyPairInformation == null) {
                    keyPairInformation = new KeyPairInformation(this.asciiVersionHeader);
                    keyPairInformation.setPublicKeyRing((PGPPublicKeyRing)iterator);
                    this.keys.put(object, keyPairInformation);
                }
                keyPairInformation.setPrivateKeyRing(pGPSecretKeyRing);
                this.Debug("Imported private key {0}", keyPairInformation.getKeyIDHex());
                linkedList.add(keyPairInformation);
                continue;
            }
            throw new WrongPasswordException("Secret key password is incorrect: " + string);
        }
        if (!bl) {
            throw new NoPrivateKeyFoundException("No private key was found in the supplied source");
        }
        this.save(false);
        return linkedList.toArray(new KeyPairInformation[linkedList.size()]);
    }

    private KeyPairInformation importKey(List list) throws IOException, PGPException {
        KeyPairInformation keyPairInformation = null;
        for (int i = 0; i < list.size(); ++i) {
            Object e = list.get(i);
            if (e instanceof PGPSecretKeyRing) {
                Object object;
                PGPSecretKeyRing pGPSecretKeyRing = (PGPSecretKeyRing)e;
                try {
                    if (this.secCollection.contains(pGPSecretKeyRing.getPublicKey().getKeyID())) {
                        this.secCollection = PGPSecretKeyRingCollection.removeSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPSecretKeyRing);
                    }
                }
                catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                    // empty catch block
                }
                this.secCollection = PGPSecretKeyRingCollection.addSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPSecretKeyRing);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(0x100000);
                Iterator iterator = pGPSecretKeyRing.getSecretKeys();
                while (iterator.hasNext()) {
                    object = ((PGPSecretKey)iterator.next()).getPublicKey();
                    if (object == null) continue;
                    byteArrayOutputStream.write(object.getEncoded());
                }
                iterator = staticBCFactory.CreatePGPPublicKeyRing(byteArrayOutputStream.toByteArray());
                try {
                    if (!this.pubCollection.contains(pGPSecretKeyRing.getPublicKey().getKeyID())) {
                        this.pubCollection = PGPPublicKeyRingCollection.addPublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)iterator);
                        this.onAddPublicRing((PGPPublicKeyRing)iterator);
                    }
                }
                catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                    // empty catch block
                }
                object = new Long(pGPSecretKeyRing.getPublicKey().getKeyID());
                keyPairInformation = (KeyPairInformation)this.keys.get(object);
                if (keyPairInformation == null) {
                    keyPairInformation = new KeyPairInformation(this.asciiVersionHeader);
                    keyPairInformation.setPublicKeyRing((PGPPublicKeyRing)iterator);
                    this.keys.put(object, keyPairInformation);
                }
                keyPairInformation.setPrivateKeyRing(pGPSecretKeyRing);
                this.Debug("Imported private key {0}", keyPairInformation.getKeyIDHex());
                break;
            }
            if (!(e instanceof PGPPublicKeyRing)) continue;
            PGPPublicKeyRing pGPPublicKeyRing = (PGPPublicKeyRing)e;
            try {
                if (this.pubCollection.contains(pGPPublicKeyRing.getPublicKey().getKeyID())) {
                    this.pubCollection = PGPPublicKeyRingCollection.removePublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
                }
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                // empty catch block
            }
            this.pubCollection = PGPPublicKeyRingCollection.addPublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
            keyPairInformation = this.onAddPublicRing(pGPPublicKeyRing);
            this.Debug("Imported public key {0}", keyPairInformation.getKeyIDHex());
            break;
        }
        this.save(false);
        return keyPairInformation;
    }

    public String getKeystoreFileName() {
        return this.keystoreFileName;
    }

    public String getKeystorePassword() {
        return this.getPassword();
    }

    public void setKeystorePassword(String string) {
        this.setPassword(string);
    }

    public KeyPairInformation[] listKeys() {
        KeyPairInformation[] keyPairInformationArray = this.getKeys();
        System.out.print(KeyStore.padRight("Type", 8));
        System.out.print(KeyStore.padRight("Bits", 10));
        System.out.print(KeyStore.padRight("Key ID", 9));
        System.out.print(KeyStore.padRight("Date", 11));
        System.out.println("User ID");
        for (int i = 0; i < keyPairInformationArray.length; ++i) {
            KeyPairInformation keyPairInformation = keyPairInformationArray[i];
            System.out.print(KeyStore.padRight(keyPairInformation.getAlgorithm(), 8));
            System.out.print(KeyStore.padRight(String.valueOf(keyPairInformation.getKeySize()), 10));
            System.out.print(KeyStore.padRight(keyPairInformation.getKeyIDHex(), 9));
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(keyPairInformation.getCreationTime().getYear()).append('/').append(keyPairInformation.getCreationTime().getMonth()).append('/').append(keyPairInformation.getCreationTime().getDate());
            System.out.print(KeyStore.padRight(stringBuffer.toString(), 11));
            for (int j = 0; j < keyPairInformation.getUserIDs().length; ++j) {
                System.out.print(keyPairInformation.getUserIDs()[j]);
            }
            System.out.println();
        }
        return keyPairInformationArray;
    }

    public KeyPairInformation getKey(String string) {
        KeyPairInformation keyPairInformation = (KeyPairInformation)this.keys.get(new Long(this.getKeyIdForUserId(string)));
        if (keyPairInformation == null) {
            return (KeyPairInformation)this.keys.get(new Long(this.getKeyIdForKeyIdHex(string)));
        }
        return keyPairInformation;
    }

    public KeyPairInformation getKey(long l) {
        if (this.keys.get(new Long(l)) != null) {
            return (KeyPairInformation)this.keys.get(new Long(l));
        }
        try {
            PGPPublicKeyRing pGPPublicKeyRing = this.pubCollection.getPublicKeyRing(l);
            if (pGPPublicKeyRing != null) {
                return (KeyPairInformation)this.keys.get(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()));
            }
            return null;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            return null;
        }
    }

    public KeyPairInformation[] getKeys() {
        return this.keys.values().toArray(new KeyPairInformation[this.keys.size()]);
    }

    public KeyPairInformation[] getKeys(String string) {
        LinkedList linkedList = new LinkedList();
        Collection collection = this.getPublicKeyRingCollection(string);
        for (PGPPublicKeyRing pGPPublicKeyRing : collection) {
            linkedList.add(this.keys.get(new Long(pGPPublicKeyRing.getPublicKey().getKeyID())));
        }
        return linkedList.toArray(new KeyPairInformation[linkedList.size()]);
    }

    private KeyPairInformation onAddPublicRing(PGPPublicKeyRing pGPPublicKeyRing) {
        KeyPairInformation keyPairInformation = (KeyPairInformation)this.keys.get(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()));
        if (keyPairInformation == null) {
            keyPairInformation = new KeyPairInformation(this.asciiVersionHeader);
            this.keys.put(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()), keyPairInformation);
        }
        keyPairInformation.setPublicKeyRing(pGPPublicKeyRing);
        Iterator iterator = pGPPublicKeyRing.getPublicKeys();
        while (iterator.hasNext()) {
            Object object;
            PGPPublicKey pGPPublicKey = (PGPPublicKey)iterator.next();
            String string = KeyPairInformation.keyId2Hex(pGPPublicKey.getKeyID());
            if (this.keyHexIds.get(string) == null) {
                object = new LinkedList<Long>();
                object.add(new Long(pGPPublicKey.getKeyID()));
                this.keyHexIds.put(string, object);
            } else {
                object = (List)this.keyHexIds.get(string);
                object.add(new Long(pGPPublicKey.getKeyID()));
            }
            object = pGPPublicKey.getRawUserIDs();
            while (object.hasNext()) {
                List<Long> list;
                String string2 = BaseLib.toUserID((byte[])object.next());
                if (this.userIds.get(string2) == null) {
                    list = new LinkedList<Long>();
                    list.add(new Long(pGPPublicKey.getKeyID()));
                    this.userIds.put(string2, list);
                    continue;
                }
                list = (List)this.userIds.get(string2);
                list.add(new Long(pGPPublicKey.getKeyID()));
            }
        }
        return keyPairInformation;
    }

    private void onRemovePublicKey(PGPPublicKey pGPPublicKey) {
        Object object;
        Object object2 = pGPPublicKey.getRawUserIDs();
        while (object2.hasNext()) {
            object = BaseLib.toUserID((byte[])object2.next());
            List list = (List)this.userIds.get(object);
            if (list == null) continue;
            for (int i = 0; i < list.size(); ++i) {
                if (((Long)list.get(i)).longValue() == pGPPublicKey.getKeyID()) {
                    list.remove(i);
                }
                if (list.size() != 0) continue;
                this.userIds.remove(object);
            }
        }
        object2 = KeyPairInformation.keyId2Hex(pGPPublicKey.getKeyID());
        object = (List)this.keyHexIds.get(object2);
        if (object != null) {
            for (int i = 0; i < object.size(); ++i) {
                if (((Long)object.get(i)).longValue() == pGPPublicKey.getKeyID()) {
                    object.remove(i);
                }
                if (object.size() != 0) continue;
                this.keyHexIds.remove(object2);
            }
        }
    }

    private void onRemovePublicRing(PGPPublicKeyRing pGPPublicKeyRing) {
        this.keys.remove(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()));
        Iterator iterator = pGPPublicKeyRing.getPublicKeys();
        while (iterator.hasNext()) {
            PGPPublicKey pGPPublicKey = (PGPPublicKey)iterator.next();
            this.onRemovePublicKey(pGPPublicKey);
        }
    }

    private void onRemoveSecretRing(PGPSecretKeyRing pGPSecretKeyRing) {
        Object object;
        boolean bl = false;
        Object object2 = pGPSecretKeyRing.getSecretKeys();
        while (object2.hasNext()) {
            object = (PGPSecretKey)object2.next();
            try {
                if (this.pubCollection.contains(object.getKeyID())) continue;
                bl = true;
                this.onRemovePublicKey(object.getPublicKey());
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {}
        }
        object2 = new Long(pGPSecretKeyRing.getPublicKey().getKeyID());
        if (bl) {
            this.keys.remove(object2);
        } else {
            object = (KeyPairInformation)this.keys.get(object2);
            ((KeyPairInformation)object).setPrivateKeyRing(null);
        }
    }

    protected void onLoadKeys() throws PGPException {
        KeyPairInformation keyPairInformation;
        PGPPublicKeyRing pGPPublicKeyRing;
        this.keyHexIds.clear();
        this.keys.clear();
        this.userIds.clear();
        HashSet<Long> hashSet = new HashSet<Long>();
        Iterator iterator = this.pubCollection.getKeyRings();
        while (iterator.hasNext()) {
            pGPPublicKeyRing = (PGPPublicKeyRing)iterator.next();
            keyPairInformation = new KeyPairInformation(this.asciiVersionHeader);
            keyPairInformation.setPublicKeyRing(pGPPublicKeyRing);
            this.keys.put(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()), keyPairInformation);
            this.onAddPublicRing(keyPairInformation.getRawPublicKeyRing());
        }
        iterator = this.secCollection.getKeyRings();
        while (iterator.hasNext()) {
            pGPPublicKeyRing = (PGPSecretKeyRing)iterator.next();
            keyPairInformation = null;
            if (!this.keys.containsKey(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()))) {
                PGPSecretKey pGPSecretKey;
                keyPairInformation = new KeyPairInformation(this.asciiVersionHeader);
                ArrayList<PGPPublicKey> arrayList = new ArrayList<PGPPublicKey>();
                Object object = pGPPublicKeyRing.getSecretKeys();
                while (object.hasNext()) {
                    pGPSecretKey = (PGPSecretKey)object.next();
                    if (pGPSecretKey.getPublicKey() == null) continue;
                    arrayList.add(pGPSecretKey.getPublicKey());
                }
                object = new ByteArrayOutputStream();
                for (int i = 0; i != arrayList.size(); ++i) {
                    PGPPublicKey pGPPublicKey = (PGPPublicKey)arrayList.get(i);
                    hashSet.add(new Long(pGPPublicKey.getKeyID()));
                    try {
                        pGPPublicKey.encode((OutputStream)object);
                        continue;
                    }
                    catch (IOException iOException) {
                        throw new PGPException(iOException.getMessage(), iOException);
                    }
                }
                pGPSecretKey = null;
                try {
                    pGPSecretKey = staticBCFactory.CreatePGPPublicKeyRing(((ByteArrayOutputStream)object).toByteArray());
                }
                catch (IOException iOException) {
                    throw new PGPException(iOException.getMessage(), iOException);
                }
                keyPairInformation.setPublicKeyRing((PGPPublicKeyRing)pGPSecretKey);
                this.onAddPublicRing(keyPairInformation.getRawPublicKeyRing());
            } else {
                keyPairInformation = (KeyPairInformation)this.keys.get(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()));
            }
            keyPairInformation.setPrivateKeyRing((PGPSecretKeyRing)pGPPublicKeyRing);
            this.keys.put(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()), keyPairInformation);
        }
    }

    private boolean hasSignatureFrom(PGPPublicKey pGPPublicKey, long l, int n) {
        Iterator iterator = pGPPublicKey.getSignaturesOfType(n);
        while (iterator.hasNext()) {
            PGPSignature pGPSignature = (PGPSignature)iterator.next();
            if (pGPSignature.getKeyID() != l) continue;
            return true;
        }
        return false;
    }

    private PGPPublicKey deleteSignatureFrom(PGPPublicKey pGPPublicKey, long l, int n) {
        Iterator iterator = pGPPublicKey.getSignaturesOfType(n);
        while (iterator.hasNext()) {
            PGPSignature pGPSignature = (PGPSignature)iterator.next();
            if (pGPSignature.getKeyID() != l) continue;
            pGPPublicKey = PGPPublicKey.removeCertification((PGPPublicKey)pGPPublicKey, (PGPSignature)pGPSignature);
        }
        return pGPPublicKey;
    }

    private void internalSignKey(PGPPublicKeyRing pGPPublicKeyRing, PGPPublicKey pGPPublicKey, String string, PGPSecretKey pGPSecretKey, String string2) throws PGPException {
        if (this.hasSignatureFrom(pGPPublicKey, pGPSecretKey.getKeyID(), 16)) {
            return;
        }
        if (!this.isKeyPasswordConfirmed(pGPSecretKey, string2)) {
            throw new WrongPasswordException("Secret key password is incorrect: " + string2);
        }
        PGPSignatureGenerator pGPSignatureGenerator = null;
        try {
            pGPSignatureGenerator = staticBCFactory.CreatePGPSignatureGenerator(pGPSecretKey.getPublicKey().getAlgorithm(), 2);
            staticBCFactory.initSign(pGPSignatureGenerator, 16, BaseLib.extractPrivateKey(pGPSecretKey, string2.toCharArray()));
            PGPSignature pGPSignature = pGPSignatureGenerator.generateCertification(string, pGPPublicKey);
            pGPPublicKey = PGPPublicKey.addCertification((PGPPublicKey)pGPPublicKey, (String)string, (PGPSignature)pGPSignature);
        }
        catch (Exception exception) {
            throw new PGPException("exception creating signature: " + exception, exception);
        }
        pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey);
        this.replacePublicKeyRing(pGPPublicKeyRing);
    }

    private void internalSignKeyAsTrustedIntroducer(PGPPublicKeyRing pGPPublicKeyRing, PGPPublicKey pGPPublicKey, String string, PGPSecretKey pGPSecretKey, String string2) throws PGPException {
        if (this.isTrustedIntroducerFor(pGPPublicKey.getKeyID(), pGPSecretKey.getKeyID())) {
            return;
        }
        if (!this.isKeyPasswordConfirmed(pGPSecretKey, string2)) {
            throw new WrongPasswordException("Secret key password is incorrect: " + string2);
        }
        PGPSignatureGenerator pGPSignatureGenerator = null;
        try {
            pGPSignatureGenerator = staticBCFactory.CreatePGPSignatureGenerator(pGPSecretKey.getPublicKey().getAlgorithm(), 2);
            staticBCFactory.initSign(pGPSignatureGenerator, 16, BaseLib.extractPrivateKey(pGPSecretKey, string2));
            PGPSignatureSubpacketGenerator pGPSignatureSubpacketGenerator = new PGPSignatureSubpacketGenerator();
            pGPSignatureSubpacketGenerator.setSignatureCreationTime(false, new Date());
            pGPSignatureSubpacketGenerator.setTrust(false, 1, 120);
            pGPSignatureGenerator.setHashedSubpackets(pGPSignatureSubpacketGenerator.generate());
            PGPSignatureSubpacketGeneratorExtended pGPSignatureSubpacketGeneratorExtended = new PGPSignatureSubpacketGeneratorExtended();
            pGPSignatureSubpacketGeneratorExtended.setIssuerKeyID(false, pGPSecretKey.getKeyID());
            pGPSignatureGenerator.setUnhashedSubpackets(pGPSignatureSubpacketGeneratorExtended.generate());
            PGPSignature pGPSignature = pGPSignatureGenerator.generateCertification(string, pGPPublicKey);
            pGPPublicKey = this.deleteSignatureFrom(pGPPublicKey, pGPSecretKey.getKeyID(), 16);
            pGPPublicKey = PGPPublicKey.addCertification((PGPPublicKey)pGPPublicKey, (String)string, (PGPSignature)pGPSignature);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKey);
        this.replacePublicKeyRing(pGPPublicKeyRing);
    }

    public boolean containsPublicKey(String string) {
        Collection collection = this.getPublicKeyRingCollection(string);
        return collection.size() > 0;
    }

    public boolean containsPrivateKey(String string) {
        Collection collection = this.getSecretKeyRingCollection(string);
        return collection.size() > 0;
    }

    public boolean containsKey(String string) {
        Collection collection = this.getPublicKeyRingCollection(string);
        if (collection.size() > 0) {
            return true;
        }
        Collection collection2 = this.getSecretKeyRingCollection(string);
        if (collection2.size() > 0) {
            return true;
        }
        if (regexHex.matcher(string).matches()) {
            String string2 = this.normalizeHexId(string);
            return this.keyHexIds.containsKey(string2);
        }
        if (longRegexHex.matcher(string).matches()) {
            String string3 = this.normalizeHexId(string);
            long l = Long.parseLong(string3, 16);
            try {
                if (this.pubCollection.contains(l) || this.secCollection.contains(l)) {
                    return true;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return false;
    }

    public boolean containsKey(long l) {
        PGPPublicKeyRing pGPPublicKeyRing = this.getPublicKeyRing(l);
        if (pGPPublicKeyRing != null) {
            return true;
        }
        try {
            PGPSecretKeyRing pGPSecretKeyRing = this.getSecretKeyRing(l);
            return pGPSecretKeyRing != null;
        }
        catch (NoPrivateKeyFoundException noPrivateKeyFoundException) {
            return false;
        }
    }

    public boolean containsPrivateKey(long l) {
        try {
            PGPSecretKeyRing pGPSecretKeyRing = this.getSecretKeyRing(l);
            return pGPSecretKeyRing != null;
        }
        catch (NoPrivateKeyFoundException noPrivateKeyFoundException) {
            return false;
        }
    }

    public boolean containsPublicKey(long l) {
        boolean bl = false;
        try {
            bl = this.pubCollection.getPublicKeyRing(l) != null;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            return false;
        }
        return bl;
    }

    public void setAutoSave(boolean bl) {
        this.autoSave = bl;
    }

    public boolean isAutoSave() {
        return this.autoSave;
    }

    public boolean isBackupOnSave() {
        return this.backupOnSave;
    }

    public void setBackupOnSave(boolean bl) {
        this.backupOnSave = bl;
    }

    public void save() throws PGPException {
        this.save(true);
    }

    public void save(boolean bl) throws PGPException {
        if (!bl && !this.autoSave) {
            return;
        }
        for (int i = 0; i < this.saveListeners.size(); ++i) {
            ((IKeyStoreSaveListener)this.saveListeners.get(i)).onSave(this);
        }
        try {
            this.storeStream.reset();
            if (this.getPassword() != null && !"".equals(this.getPassword())) {
                this.saveToStream(this.storeStream, this.getPassword());
            } else {
                this.saveToStream(this.storeStream);
            }
            this.storage.store(new ByteArrayInputStream(this.storeStream.getArray()), this.storeStream.getArray().length);
        }
        catch (IOException iOException) {
            throw new PGPException("exception saving key store", iOException);
        }
        finally {
            IOUtil.closeStream(this.storeStream);
        }
    }

    public void store(OutputStream outputStream, String string) throws PGPException, IOException {
        PGPEncryptedDataGenerator pGPEncryptedDataGenerator = staticBCFactory.CreatePGPEncryptedDataGenerator(9, true, IOUtil.getSecureRandom());
        try {
            pGPEncryptedDataGenerator.addMethod((PGPKeyEncryptionMethodGenerator)staticBCFactory.CreatePBEKeyEncryptionMethodGenerator(string));
            OutputStream outputStream2 = pGPEncryptedDataGenerator.open(outputStream, new byte[1024]);
            this.writeCollection(outputStream2, "pubring.gpg", this.pubModifiedDate, this.pubCollection.getEncoded());
            this.writeCollection(outputStream2, "secring.gpg", this.secModifiedDate, this.secCollection.getEncoded());
            outputStream2.close();
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    private byte[] readCollection(PGPLiteralData pGPLiteralData) throws IOException {
        int n;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream = pGPLiteralData.getInputStream();
        while ((n = inputStream.read()) >= 0) {
            byteArrayOutputStream.write(n);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private void copy(File file, File file2) throws IOException {
        int n;
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file2));
        while ((n = ((InputStream)bufferedInputStream).read()) >= 0) {
            ((OutputStream)bufferedOutputStream).write(n);
        }
        ((InputStream)bufferedInputStream).close();
        ((OutputStream)bufferedOutputStream).close();
    }

    private void writeCollection(OutputStream outputStream, String string, Date date, byte[] byArray) throws IOException {
        PGPLiteralDataGenerator pGPLiteralDataGenerator = new PGPLiteralDataGenerator();
        OutputStream outputStream2 = pGPLiteralDataGenerator.open(outputStream, 'b', string, (long)byArray.length, date);
        outputStream2.write(byArray);
        pGPLiteralDataGenerator.close();
        outputStream2.close();
    }

    PGPPublicKeyRing findPublicKeyRing(String string) throws NoPublicKeyFoundException {
        Collection collection = this.getPublicKeyRingCollection(string);
        if (collection.size() == 0 && this.searchListeners.size() > 0) {
            String string2 = "";
            String string3 = "";
            if (regexHex.matcher(string).matches() || longRegexHex.matcher(string).matches()) {
                string3 = string;
            } else {
                string2 = string;
            }
            for (int i = 0; i < this.searchListeners.size(); ++i) {
                ((IKeyStoreSearchListener)this.searchListeners.get(i)).onKeyNotFound(this, true, 0L, string3, string2);
            }
            collection = this.getPublicKeyRingCollection(string);
        }
        if (collection.size() > 0) {
            return (PGPPublicKeyRing)collection.iterator().next();
        }
        throw new NoPublicKeyFoundException("No key found for userID: " + string);
    }

    PGPPublicKeyRing findPublicKeyRing(long l) throws NoPublicKeyFoundException {
        PGPPublicKeyRing pGPPublicKeyRing = this.getPublicKeyRing(l);
        if (pGPPublicKeyRing == null && this.searchListeners.size() > 0) {
            for (int i = 0; i < this.searchListeners.size(); ++i) {
                ((IKeyStoreSearchListener)this.searchListeners.get(i)).onKeyNotFound(this, true, l, KeyPairInformation.keyId2Hex(l), "");
            }
            pGPPublicKeyRing = this.getPublicKeyRing(l);
        }
        if (pGPPublicKeyRing == null) {
            throw new NoPublicKeyFoundException("no key found matching keyID: " + l);
        }
        return pGPPublicKeyRing;
    }

    private Collection getPublicKeyRingCollection(String string) {
        try {
            PGPPublicKeyRing pGPPublicKeyRing;
            ArrayList<Object> arrayList = new ArrayList<Object>();
            HashSet<Long> hashSet = new HashSet<Long>();
            Iterator iterator = this.pubCollection.getKeyRings(string, this.partialMatch, !this.caseSensitiveMatch);
            while (iterator.hasNext()) {
                pGPPublicKeyRing = (PGPPublicKeyRing)iterator.next();
                hashSet.add(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()));
                arrayList.add(pGPPublicKeyRing);
            }
            iterator = this.secCollection.getKeyRings(string, this.partialMatch, !this.caseSensitiveMatch);
            while (iterator.hasNext()) {
                pGPPublicKeyRing = (PGPSecretKeyRing)iterator.next();
                if (hashSet.contains(new Long(pGPPublicKeyRing.getSecretKey().getKeyID()))) continue;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Iterator iterator2 = pGPPublicKeyRing.getSecretKeys();
                while (iterator2.hasNext()) {
                    PGPSecretKey pGPSecretKey = (PGPSecretKey)iterator2.next();
                    byteArrayOutputStream.write(pGPSecretKey.getPublicKey().getEncoded());
                }
                arrayList.add(staticBCFactory.CreatePGPPublicKeyRing(byteArrayOutputStream.toByteArray()));
            }
            if (arrayList.size() == 0 && (regexHex.matcher(string).matches() || longRegexHex.matcher(string).matches()) && (iterator = this.getPublicKeyRing(this.getKeyIdForKeyIdHex(string))) != null) {
                arrayList.add(iterator);
            }
            return arrayList;
        }
        catch (IOException iOException) {
            throw new IllegalStateException("unexpected exception on extraction: " + iOException);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw new IllegalStateException("unexpected exception: " + (Object)((Object)pGPException));
        }
    }

    private PGPPublicKeyRing getPublicKeyRing(long l) {
        try {
            if (this.pubCollection.contains(l)) {
                return this.pubCollection.getPublicKeyRing(l);
            }
            if (this.secCollection.contains(l)) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Iterator iterator = this.secCollection.getSecretKeyRing(l).getSecretKeys();
                while (iterator.hasNext()) {
                    PGPSecretKey pGPSecretKey = (PGPSecretKey)iterator.next();
                    byteArrayOutputStream.write(pGPSecretKey.getPublicKey().getEncoded());
                }
                return staticBCFactory.CreatePGPPublicKeyRing(byteArrayOutputStream.toByteArray());
            }
            return null;
        }
        catch (IOException iOException) {
            throw new IllegalStateException("unexpected exception on extraction: " + iOException);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw new IllegalStateException("unexpected exception: " + (Object)((Object)pGPException));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected List loadKeyFile(String string) throws FileNotFoundException, IOException, PGPException {
        List list;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(string);
            list = KeyStore.loadKeyStream(fileInputStream);
        }
        catch (Throwable throwable) {
            IOUtil.closeStream(fileInputStream);
            throw throwable;
        }
        IOUtil.closeStream(fileInputStream);
        return list;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected List parseKeyStream(InputStream inputStream, String string, BoolValue boolValue) throws PGPException, IOException {
        LinkedList<Object> linkedList = new LinkedList<Object>();
        PGPObjectFactory2 pGPObjectFactory2 = new PGPObjectFactory2(inputStream);
        pGPObjectFactory2.setLoadingKey(true);
        try {
            Object object = pGPObjectFactory2.nextObject();
            while (object != null) {
                Object object2;
                Object object3;
                PGPPublicKeyRing pGPPublicKeyRing;
                if (object instanceof PGPPublicKeyRing) {
                    pGPPublicKeyRing = (PGPPublicKeyRing)object;
                    if (this.pubCollection.contains(pGPPublicKeyRing.getPublicKey().getKeyID())) {
                        this.pubCollection = PGPPublicKeyRingCollection.removePublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
                    }
                    this.pubCollection = PGPPublicKeyRingCollection.addPublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
                    this.onAddPublicRing(pGPPublicKeyRing);
                    object3 = new Long(pGPPublicKeyRing.getPublicKey().getKeyID());
                    object2 = (KeyPairInformation)this.keys.get(object3);
                    if (object2 == null) {
                        object2 = new KeyPairInformation(this.asciiVersionHeader);
                        ((KeyPairInformation)object2).setPublicKeyRing(pGPPublicKeyRing);
                        this.keys.put(object3, object2);
                    }
                    linkedList.add(object2);
                } else if (object instanceof PGPSecretKeyRing) {
                    Object object4;
                    pGPPublicKeyRing = (PGPSecretKeyRing)object;
                    if (string != null && !this.isKeyPasswordConfirmed((PGPSecretKeyRing)pGPPublicKeyRing, string)) throw new WrongPasswordException("secret key password is incorrect");
                    if (this.secCollection.contains(pGPPublicKeyRing.getPublicKey().getKeyID())) {
                        this.secCollection = PGPSecretKeyRingCollection.removeSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPPublicKeyRing);
                    }
                    this.secCollection = PGPSecretKeyRingCollection.addSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPPublicKeyRing);
                    object3 = new ByteArrayOutputStream(10240);
                    object2 = pGPPublicKeyRing.getSecretKeys();
                    while (object2.hasNext()) {
                        object4 = ((PGPSecretKey)object2.next()).getPublicKey();
                        if (object4 == null) continue;
                        ((OutputStream)object3).write(object4.getEncoded());
                    }
                    object2 = staticBCFactory.CreatePGPPublicKeyRing(((ByteArrayOutputStream)object3).toByteArray());
                    if (!this.pubCollection.contains(pGPPublicKeyRing.getPublicKey().getKeyID())) {
                        this.pubCollection = PGPPublicKeyRingCollection.addPublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)object2);
                        this.onAddPublicRing((PGPPublicKeyRing)object2);
                    }
                    if ((object4 = (KeyPairInformation)this.keys.get(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()))) == null) {
                        object4 = new KeyPairInformation(this.asciiVersionHeader);
                        ((KeyPairInformation)object4).setPublicKeyRing((PGPPublicKeyRing)object2);
                        this.keys.put(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()), object4);
                    }
                    ((KeyPairInformation)object4).setPrivateKeyRing((PGPSecretKeyRing)pGPPublicKeyRing);
                    linkedList.add(object4);
                } else if (!(object instanceof ExperimentalPacket || object instanceof PGPOnePassSignatureList || object instanceof PGPSignatureList || object instanceof PGPEncryptedDataList)) {
                    throw new PGPException("Unexpected object found in stream: " + object.getClass().getName());
                }
                object = pGPObjectFactory2.nextObject();
            }
            return linkedList;
        }
        catch (UnknownKeyPacketsException unknownKeyPacketsException) {
            boolValue.setValue(true);
            return linkedList;
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    private boolean isTrustedInKeyStore(long l, int n) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(l);
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        KeyPairInformation keyPairInformation = (KeyPairInformation)this.keys.get(new Long(pGPPublicKey.getKeyID()));
        byte by = keyPairInformation.getTrust();
        if (by >= 120) {
            return true;
        }
        Iterator iterator = pGPPublicKeyRing.getPublicKey().getSignaturesOfType(16);
        while (iterator.hasNext()) {
            PGPSignature pGPSignature = (PGPSignature)iterator.next();
            if (pGPSignature.getKeyID() == pGPPublicKey.getKeyID() || !this.isTrusted(pGPSignature.getKeyID(), n + 1)) continue;
            return true;
        }
        return false;
    }

    private boolean isTrustedIntroducerFor(long l, long l2) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(l);
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        Iterator iterator = pGPPublicKeyRing.getPublicKey().getSignaturesOfType(16);
        while (iterator.hasNext()) {
            SignatureSubpacket signatureSubpacket;
            PGPSignatureSubpacketVector pGPSignatureSubpacketVector;
            PGPSignature pGPSignature = (PGPSignature)iterator.next();
            if (pGPSignature.getKeyID() == pGPPublicKey.getKeyID() || !pGPSignature.hasSubpackets() || (pGPSignatureSubpacketVector = pGPSignature.getHashedSubPackets()) == null || (signatureSubpacket = pGPSignatureSubpacketVector.getSubpacket(5)) == null || pGPSignature.getKeyID() != l2) continue;
            return true;
        }
        return false;
    }

    private boolean isTrustedIntroducer(long l) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(l);
        PGPPublicKey pGPPublicKey = pGPPublicKeyRing.getPublicKey();
        Iterator iterator = pGPPublicKeyRing.getPublicKey().getSignaturesOfType(16);
        while (iterator.hasNext()) {
            PGPSignatureSubpacketVector pGPSignatureSubpacketVector;
            SignatureSubpacket signatureSubpacket;
            PGPSignature pGPSignature = (PGPSignature)iterator.next();
            if (pGPSignature.getKeyID() == pGPPublicKey.getKeyID() || !pGPSignature.hasSubpackets() || (signatureSubpacket = (pGPSignatureSubpacketVector = pGPSignature.getHashedSubPackets()).getSubpacket(5)) == null) continue;
            try {
                if (!this.secCollection.contains(pGPSignature.getKeyID())) continue;
                return true;
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                return false;
            }
        }
        return false;
    }

    protected boolean isKeyPasswordConfirmed(PGPSecretKeyRing pGPSecretKeyRing, String string) {
        int n = 0;
        Iterator iterator = pGPSecretKeyRing.getSecretKeys();
        while (iterator.hasNext()) {
            PGPSecretKey pGPSecretKey = (PGPSecretKey)iterator.next();
            try {
                pGPSecretKey.extractPrivateKey(staticBCFactory.CreatePBESecretKeyDecryptor(string));
                ++n;
            }
            catch (Exception exception) {}
        }
        return n != 0;
    }

    protected boolean isKeyPasswordConfirmed(PGPSecretKey pGPSecretKey, String string) {
        int n = 0;
        try {
            pGPSecretKey.extractPrivateKey(staticBCFactory.CreatePBESecretKeyDecryptor(string));
            ++n;
        }
        catch (Exception exception) {
            // empty catch block
        }
        return n != 0;
    }

    protected PGPSecretKeyRing findSecretKeyRing(String string) throws NoPrivateKeyFoundException {
        Collection collection = this.getSecretKeyRingCollection(string);
        if (collection.size() == 0 && this.searchListeners.size() > 0) {
            String string2 = "";
            String string3 = "";
            if (regexHex.matcher(string).matches() || longRegexHex.matcher(string).matches()) {
                string3 = string;
            } else {
                string2 = string;
            }
            for (int i = 0; i < this.searchListeners.size(); ++i) {
                ((IKeyStoreSearchListener)this.searchListeners.get(i)).onKeyNotFound(this, false, 0L, string3, string2);
            }
            collection = this.getSecretKeyRingCollection(string);
        }
        if (collection.size() > 0) {
            return (PGPSecretKeyRing)collection.iterator().next();
        }
        throw new NoPrivateKeyFoundException("No key found for userID: " + string);
    }

    protected PGPSecretKeyRing findSecretKeyRing(long l) throws NoPrivateKeyFoundException {
        PGPSecretKeyRing pGPSecretKeyRing = this.getSecretKeyRing(l);
        if (pGPSecretKeyRing == null && this.searchListeners.size() > 0) {
            for (int i = 0; i < this.searchListeners.size(); ++i) {
                ((IKeyStoreSearchListener)this.searchListeners.get(i)).onKeyNotFound(this, false, l, KeyPairInformation.keyId2Hex(l), "");
            }
            pGPSecretKeyRing = this.getSecretKeyRing(l);
        }
        if (pGPSecretKeyRing == null) {
            throw new NoPrivateKeyFoundException("No key found matching keyID: " + l);
        }
        return pGPSecretKeyRing;
    }

    protected Collection getSecretKeyRingCollection(String string) {
        ArrayList<Object> arrayList = new ArrayList<Object>();
        try {
            Iterator iterator = this.secCollection.getKeyRings(string, this.partialMatch, !this.caseSensitiveMatch);
            while (iterator.hasNext()) {
                PGPSecretKeyRing pGPSecretKeyRing = (PGPSecretKeyRing)iterator.next();
                arrayList.add(pGPSecretKeyRing);
            }
            if (arrayList.size() == 0 && (regexHex.matcher(string).matches() || longRegexHex.matcher(string).matches()) && (iterator = this.getSecretKeyRing(this.getKeyIdForKeyIdHex(string))) != null) {
                arrayList.add(iterator);
            }
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            // empty catch block
        }
        return arrayList;
    }

    private PGPSecretKeyRing getSecretKeyRing(long l) throws NoPrivateKeyFoundException {
        try {
            return this.secCollection.getSecretKeyRing(l);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw new NoPrivateKeyFoundException(pGPException.getMessage(), (Exception)((Object)pGPException));
        }
    }

    public boolean deleteSubKey(String string) throws PGPException {
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(string);
        if (regexHex.matcher(string).matches()) {
            String string2 = this.normalizeHexId(string);
            Iterator iterator = pGPPublicKeyRing.getPublicKeys();
            while (iterator.hasNext()) {
                PGPPublicKey pGPPublicKey = (PGPPublicKey)iterator.next();
                if (!KeyStore.keyId2Hex(pGPPublicKey.getKeyID()).equalsIgnoreCase(string2)) continue;
                this.deleteSubKey(pGPPublicKey.getKeyID());
                return true;
            }
        } else if (longRegexHex.matcher(string).matches()) {
            long l = Long.parseLong(this.normalizeHexId(string), 16);
            this.deleteSubKey(l);
            return true;
        }
        return false;
    }

    public void deleteSubKey(long l) throws PGPException {
        PGPSecretKeyRing pGPSecretKeyRing;
        PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(l);
        try {
            pGPSecretKeyRing = this.findSecretKeyRing(l);
            PGPSecretKeyRing pGPSecretKeyRing2 = PGPSecretKeyRing.removeSecretKey((PGPSecretKeyRing)pGPSecretKeyRing, (PGPSecretKey)pGPSecretKeyRing.getSecretKey(l));
            this.replaceSecretKeyRing(pGPSecretKeyRing2);
        }
        catch (NoPrivateKeyFoundException noPrivateKeyFoundException) {
            // empty catch block
        }
        pGPSecretKeyRing = PGPPublicKeyRing.removePublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKeyRing.getPublicKey(l));
        this.replacePublicKeyRing((PGPPublicKeyRing)pGPSecretKeyRing);
        this.save(false);
    }

    public long addSubKey(String string, String string2, boolean bl, String string3, int n) throws PGPException {
        PGPSecretKeyRing pGPSecretKeyRing = this.findSecretKeyRing(string);
        return this.addSubKey(pGPSecretKeyRing.getPublicKey().getKeyID(), string2, bl, string3, n);
    }

    public long addSubKey(long l, String string, boolean bl, EcCurve.Enum enum_) throws PGPException {
        PGPSecretKeyRing pGPSecretKeyRing = this.findSecretKeyRing(l);
        PGPSecretKey pGPSecretKey = pGPSecretKeyRing.getSecretKey();
        ECKeyPairGenerator eCKeyPairGenerator = new ECKeyPairGenerator();
        ASN1ObjectIdentifier aSN1ObjectIdentifier = KeyStore.calculateCurveASN1(enum_);
        X9ECParameters x9ECParameters = KeyStore.calculateCurveX9(enum_);
        ECNamedDomainParameters eCNamedDomainParameters = new ECNamedDomainParameters(aSN1ObjectIdentifier, x9ECParameters.getCurve(), x9ECParameters.getG(), x9ECParameters.getN());
        SecureRandom secureRandom = IOUtil.getSecureRandom();
        eCKeyPairGenerator.init((KeyGenerationParameters)new ECKeyGenerationParameters((ECDomainParameters)eCNamedDomainParameters, secureRandom));
        AsymmetricCipherKeyPair asymmetricCipherKeyPair = eCKeyPairGenerator.generateKeyPair();
        Date date = new Date();
        BcPGPKeyPair bcPGPKeyPair = null;
        try {
            bcPGPKeyPair = bl ? new BcPGPKeyPair(19, asymmetricCipherKeyPair, date) : new BcPGPKeyPair(18, asymmetricCipherKeyPair, date);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        return this.internalAddSubKey(pGPSecretKeyRing, pGPSecretKey, string, (PGPKeyPair)bcPGPKeyPair, bl);
    }

    public long addSubKey(long l, String string, boolean bl, String string2, int n) throws PGPException {
        PGPSecretKeyRing pGPSecretKeyRing = this.findSecretKeyRing(l);
        PGPSecretKey pGPSecretKey = pGPSecretKeyRing.getSecretKey();
        Date date = new Date();
        SecureRandom secureRandom = IOUtil.getSecureRandom();
        BcPGPKeyPair bcPGPKeyPair = null;
        if (RSA.equalsIgnoreCase(string2)) {
            RSAKeyPairGenerator rSAKeyPairGenerator = null;
            rSAKeyPairGenerator = new RSAKeyPairGenerator();
            rSAKeyPairGenerator.init((KeyGenerationParameters)new RSAKeyGenerationParameters(BigInteger.valueOf(65537L), secureRandom, n, 25));
            AsymmetricCipherKeyPair asymmetricCipherKeyPair = rSAKeyPairGenerator.generateKeyPair();
            try {
                bcPGPKeyPair = new BcPGPKeyPair(1, asymmetricCipherKeyPair, date);
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
        }
        if (ELGAMAL.equalsIgnoreCase(string2)) {
            if (bl) {
                FastElGamal fastElGamal = new FastElGamal(n / 8);
                fastElGamal.generateKeys();
                ElGamalParameters elGamalParameters = new ElGamalParameters(fastElGamal.getPrivateKey().getP(), fastElGamal.getPublicKey().getG());
                ElGamalKeyPairGenerator elGamalKeyPairGenerator = new ElGamalKeyPairGenerator();
                elGamalKeyPairGenerator.init((KeyGenerationParameters)new ElGamalKeyGenerationParameters(secureRandom, elGamalParameters));
                AsymmetricCipherKeyPair asymmetricCipherKeyPair = elGamalKeyPairGenerator.generateKeyPair();
                try {
                    bcPGPKeyPair = new BcPGPKeyPair(16, asymmetricCipherKeyPair, date);
                }
                catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                    throw IOUtil.newPGPException(pGPException);
                }
            } else {
                DSAParametersGenerator dSAParametersGenerator = new DSAParametersGenerator();
                dSAParametersGenerator.init(1024, 50, IOUtil.getSecureRandom());
                DSAKeyPairGenerator dSAKeyPairGenerator = new DSAKeyPairGenerator();
                dSAKeyPairGenerator.init((KeyGenerationParameters)new DSAKeyGenerationParameters(IOUtil.getSecureRandom(), dSAParametersGenerator.generateParameters()));
                try {
                    bcPGPKeyPair = new BcPGPKeyPair(17, dSAKeyPairGenerator.generateKeyPair(), date);
                }
                catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                    throw IOUtil.newPGPException(pGPException);
                }
            }
        } else {
            ECKeyPairGenerator eCKeyPairGenerator = new ECKeyPairGenerator();
            X9ECParameters x9ECParameters = NISTNamedCurves.getByName((String)"P-256");
            String string3 = "P-256";
            if (n > 256 && n < 521) {
                x9ECParameters = NISTNamedCurves.getByName((String)"P-384");
                string3 = "P-384";
            } else if (n >= 521) {
                x9ECParameters = NISTNamedCurves.getByName((String)"P-521");
                string3 = "P-521";
            }
            ECNamedDomainParameters eCNamedDomainParameters = new ECNamedDomainParameters(NISTNamedCurves.getOID((String)string3), x9ECParameters.getCurve(), x9ECParameters.getG(), x9ECParameters.getN());
            eCKeyPairGenerator.init((KeyGenerationParameters)new ECKeyGenerationParameters((ECDomainParameters)eCNamedDomainParameters, secureRandom));
            AsymmetricCipherKeyPair asymmetricCipherKeyPair = eCKeyPairGenerator.generateKeyPair();
            try {
                bcPGPKeyPair = bl ? new BcPGPKeyPair(19, asymmetricCipherKeyPair, date) : new BcPGPKeyPair(18, asymmetricCipherKeyPair, date);
            }
            catch (lw.bouncycastle.openpgp.PGPException pGPException) {
                throw IOUtil.newPGPException(pGPException);
            }
        }
        return this.internalAddSubKey(pGPSecretKeyRing, pGPSecretKey, string, (PGPKeyPair)bcPGPKeyPair, bl);
    }

    private long internalAddSubKey(PGPSecretKeyRing pGPSecretKeyRing, PGPSecretKey pGPSecretKey, String string, PGPKeyPair pGPKeyPair, boolean bl) throws PGPException {
        PGPSignatureGenerator pGPSignatureGenerator = new PGPSignatureGenerator(staticBCFactory.CreatePGPContentSignerBuilder(pGPSecretKey.getPublicKey().getAlgorithm(), 2));
        if (pGPSecretKey.getPublicKey().getAlgorithm() == 18 || pGPSecretKey.getPublicKey().getAlgorithm() == 19) {
            pGPSignatureGenerator = new PGPSignatureGenerator(staticBCFactory.CreatePGPContentSignerBuilder(pGPSecretKey.getPublicKey().getAlgorithm(), 10));
        }
        PGPSecretKey pGPSecretKey2 = null;
        try {
            pGPSignatureGenerator.init(24, BaseLib.extractPrivateKey(pGPSecretKey, string));
            PGPSignatureSubpacketGenerator pGPSignatureSubpacketGenerator = new PGPSignatureSubpacketGenerator();
            if (bl) {
                pGPSignatureSubpacketGenerator.setKeyFlags(false, 12);
            } else {
                pGPSignatureSubpacketGenerator.setKeyFlags(false, 2);
            }
            pGPSignatureGenerator.setHashedSubpackets(pGPSignatureSubpacketGenerator.generate());
            pGPSignatureGenerator.setUnhashedSubpackets(null);
            LinkedList<PGPSignature> linkedList = new LinkedList<PGPSignature>();
            linkedList.add(pGPSignatureGenerator.generateCertification(pGPSecretKey.getPublicKey(), pGPKeyPair.getPublicKey()));
            int n = 9;
            boolean bl2 = true;
            PGPPublicKey pGPPublicKey = (PGPPublicKey)ReflectionUtils.callPrivateConstrtuctor(PGPPublicKey.class, new Object[]{pGPKeyPair.getPublicKey(), null, linkedList}, new Class[]{PGPPublicKey.class, TrustPacket.class, List.class});
            ReflectionUtils.setPrivateFieldvalue(pGPPublicKey, "publicPk", new PublicSubkeyPacket(pGPPublicKey.getPublicKeyPacket().getAlgorithm(), pGPPublicKey.getPublicKeyPacket().getTime(), pGPPublicKey.getPublicKeyPacket().getKey()));
            PGPDigestCalculator pGPDigestCalculator = new BcPGPDigestCalculatorProvider().get(2);
            pGPSecretKey2 = new PGPSecretKey(pGPKeyPair.getPrivateKey(), pGPPublicKey, pGPDigestCalculator, false, staticBCFactory.CreatePBESecretKeyEncryptor(string, n));
            pGPSecretKeyRing = PGPSecretKeyRing.insertSecretKey((PGPSecretKeyRing)pGPSecretKeyRing, (PGPSecretKey)pGPSecretKey2);
            PGPPublicKeyRing pGPPublicKeyRing = this.findPublicKeyRing(pGPSecretKeyRing.getPublicKey().getKeyID());
            pGPPublicKeyRing = PGPPublicKeyRing.insertPublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPSecretKey2.getPublicKey());
            this.replacePublicKeyRing(pGPPublicKeyRing);
            this.replaceSecretKeyRing(pGPSecretKeyRing);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
        this.save(false);
        return pGPSecretKey2.getKeyID();
    }

    private void addSigningSubkey(PGPKeyRingGenerator pGPKeyRingGenerator, PGPKeyPair pGPKeyPair, PGPKeyPair pGPKeyPair2) throws PGPException {
        PGPSignatureSubpacketGenerator pGPSignatureSubpacketGenerator = new PGPSignatureSubpacketGenerator();
        pGPSignatureSubpacketGenerator.setKeyFlags(false, 2);
        PGPSignatureGenerator pGPSignatureGenerator = null;
        try {
            pGPSignatureGenerator = staticBCFactory.CreatePGPSignatureGenerator(pGPKeyPair.getPublicKey().getAlgorithm(), 2);
            staticBCFactory.initSign(pGPSignatureGenerator, 25, pGPKeyPair.getPrivateKey());
            pGPSignatureSubpacketGenerator.setEmbeddedSignature(false, pGPSignatureGenerator.generateCertification(pGPKeyPair2.getPublicKey(), pGPKeyPair.getPublicKey()));
            pGPKeyRingGenerator.addSubKey(pGPKeyPair, pGPSignatureSubpacketGenerator.generate(), null);
        }
        catch (IOException iOException) {
            throw new PGPException(iOException.getMessage(), iOException);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    private static void addEncryptionSubkey(PGPKeyRingGenerator pGPKeyRingGenerator, PGPKeyPair pGPKeyPair, int[] nArray, int[] nArray2, int[] nArray3, long l) throws PGPException {
        PGPSignatureSubpacketGenerator pGPSignatureSubpacketGenerator = new PGPSignatureSubpacketGenerator();
        pGPSignatureSubpacketGenerator.setKeyFlags(false, 12);
        if (l > 0L) {
            pGPSignatureSubpacketGenerator.setKeyExpirationTime(false, l * 24L * 60L * 60L);
        }
        pGPSignatureSubpacketGenerator.setPreferredSymmetricAlgorithms(false, nArray);
        pGPSignatureSubpacketGenerator.setPreferredHashAlgorithms(false, nArray2);
        pGPSignatureSubpacketGenerator.setPreferredCompressionAlgorithms(false, nArray3);
        try {
            pGPKeyRingGenerator.addSubKey(pGPKeyPair, pGPSignatureSubpacketGenerator.generate(), null);
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    private static int algorithmID(String string) {
        if (string.equalsIgnoreCase(RSA)) {
            return 1;
        }
        if (string.equalsIgnoreCase(DSA)) {
            return 17;
        }
        if (string.equalsIgnoreCase(ELGAMAL)) {
            return 16;
        }
        if (string.equalsIgnoreCase(EC)) {
            return 18;
        }
        throw new IllegalArgumentException("unknown key algorithm: " + string);
    }

    private void deleteKey(long l) throws PGPException {
        try {
            PGPPublicKeyRing pGPPublicKeyRing;
            if (this.pubCollection.contains(l)) {
                this.pubModifiedDate = this.getSecondDate();
                pGPPublicKeyRing = this.pubCollection.getPublicKeyRing(l);
                this.pubCollection = PGPPublicKeyRingCollection.removePublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
                if (!pGPPublicKeyRing.getPublicKey(l).isMasterKey()) {
                    pGPPublicKeyRing = PGPPublicKeyRing.removePublicKey((PGPPublicKeyRing)pGPPublicKeyRing, (PGPPublicKey)pGPPublicKeyRing.getPublicKey(l));
                    this.pubCollection = PGPPublicKeyRingCollection.addPublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
                }
            }
            if (this.secCollection.contains(l)) {
                this.secModifiedDate = this.getSecondDate();
                pGPPublicKeyRing = this.secCollection.getSecretKeyRing(l);
                this.secCollection = PGPSecretKeyRingCollection.removeSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPPublicKeyRing);
                if (!pGPPublicKeyRing.getSecretKey(l).isMasterKey()) {
                    pGPPublicKeyRing = PGPSecretKeyRing.removeSecretKey((PGPSecretKeyRing)pGPPublicKeyRing, (PGPSecretKey)pGPPublicKeyRing.getSecretKey(l));
                    this.secCollection = PGPSecretKeyRingCollection.addSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPPublicKeyRing);
                }
            }
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw IOUtil.newPGPException(pGPException);
        }
    }

    private Date getSecondDate() {
        Date date = new Date();
        date.setTime(date.getTime() / 1000L * 1000L);
        return date;
    }

    protected void replacePublicKeyRing(PGPPublicKeyRing pGPPublicKeyRing) throws PGPException {
        try {
            boolean bl = false;
            long l = pGPPublicKeyRing.getPublicKey().getKeyID();
            KeyPairInformation keyPairInformation = new KeyPairInformation();
            if (this.pubCollection.contains(l)) {
                bl = true;
                keyPairInformation.setPublicKeyRing(pGPPublicKeyRing);
                this.keys.put(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()), keyPairInformation);
                this.pubCollection = PGPPublicKeyRingCollection.removePublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
                this.pubCollection = PGPPublicKeyRingCollection.addPublicKeyRing((PGPPublicKeyRingCollection)this.pubCollection, (PGPPublicKeyRing)pGPPublicKeyRing);
            }
            if (this.secCollection.contains(l)) {
                bl = true;
                PGPSecretKeyRing pGPSecretKeyRing = this.secCollection.getSecretKeyRing(l);
                pGPSecretKeyRing = PGPSecretKeyRing.replacePublicKeys((PGPSecretKeyRing)pGPSecretKeyRing, (PGPPublicKeyRing)pGPPublicKeyRing);
                keyPairInformation.setPublicKeyRing(pGPPublicKeyRing);
                keyPairInformation.setPrivateKeyRing(pGPSecretKeyRing);
                this.keys.put(new Long(pGPPublicKeyRing.getPublicKey().getKeyID()), keyPairInformation);
                this.secCollection = PGPSecretKeyRingCollection.removeSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPSecretKeyRing);
                this.secCollection = PGPSecretKeyRingCollection.addSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPSecretKeyRing);
            }
            if (!bl) {
                throw new IllegalStateException("unknown key ring in replace");
            }
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw new PGPException(pGPException.getMessage(), pGPException.getUnderlyingException());
        }
        this.save(false);
    }

    protected void replaceSecretKeyRing(PGPSecretKeyRing pGPSecretKeyRing) throws PGPException {
        try {
            boolean bl = false;
            long l = pGPSecretKeyRing.getPublicKey().getKeyID();
            KeyPairInformation keyPairInformation = new KeyPairInformation();
            if (this.pubCollection.contains(l)) {
                bl = true;
                PGPPublicKeyRing pGPPublicKeyRing = this.pubCollection.getPublicKeyRing(l);
                keyPairInformation.setPublicKeyRing(pGPPublicKeyRing);
            }
            if (this.secCollection.contains(l)) {
                bl = true;
                keyPairInformation.setPrivateKeyRing(pGPSecretKeyRing);
                this.keys.put(new Long(pGPSecretKeyRing.getPublicKey().getKeyID()), keyPairInformation);
                this.secCollection = PGPSecretKeyRingCollection.removeSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPSecretKeyRing);
                this.secCollection = PGPSecretKeyRingCollection.addSecretKeyRing((PGPSecretKeyRingCollection)this.secCollection, (PGPSecretKeyRing)pGPSecretKeyRing);
            }
            if (!bl) {
                throw new IllegalStateException("unknown key ring in replace");
            }
        }
        catch (lw.bouncycastle.openpgp.PGPException pGPException) {
            throw new PGPException(pGPException.getMessage(), pGPException.getUnderlyingException());
        }
        this.save(false);
    }

    protected int getEncAlgorithm(String string) {
        if (string == null) {
            return 9;
        }
        if (string.equalsIgnoreCase("AES_256")) {
            return 9;
        }
        if (string.equalsIgnoreCase("AES_192")) {
            return 8;
        }
        if (string.equalsIgnoreCase("AES_128")) {
            return 7;
        }
        if (string.equalsIgnoreCase("TRIPLE_DES")) {
            return 2;
        }
        if (string.equalsIgnoreCase("TWOFISH")) {
            return 10;
        }
        if (string.equalsIgnoreCase("NULL")) {
            return 0;
        }
        throw new IllegalArgumentException("unknown symmetric encryption algorithm: " + string);
    }

    protected static String getKeyAlgorithm(int n) {
        switch (n) {
            case 1: {
                return RSA;
            }
            case 2: {
                return RSA;
            }
            case 3: {
                return RSA;
            }
            case 16: {
                return "DH/DSS";
            }
            case 17: {
                return "DH/DSS";
            }
            case 18: {
                return "ECDH";
            }
            case 19: {
                return "ECDSA";
            }
            case 20: {
                return "DH/DSS";
            }
            case 21: {
                return "DH/DSS";
            }
        }
        return "Unknown";
    }

    static KeyAlgorithm.Enum getKeyAlgorithmType(int n) {
        switch (n) {
            case 1: {
                return KeyAlgorithm.Enum.RSA;
            }
            case 2: {
                return KeyAlgorithm.Enum.RSA;
            }
            case 3: {
                return KeyAlgorithm.Enum.RSA;
            }
            case 16: {
                return KeyAlgorithm.Enum.ELGAMAL;
            }
            case 17: {
                return KeyAlgorithm.Enum.ELGAMAL;
            }
            case 18: {
                return KeyAlgorithm.Enum.EC;
            }
            case 19: {
                return KeyAlgorithm.Enum.EC;
            }
            case 20: {
                return KeyAlgorithm.Enum.ELGAMAL;
            }
            case 21: {
                return KeyAlgorithm.Enum.ELGAMAL;
            }
        }
        return KeyAlgorithm.Enum.Unknown;
    }

    static String compressionToString(int n) {
        switch (n) {
            case 1: {
                return "Zip";
            }
            case 3: {
                return "BZip2";
            }
            case 2: {
                return "ZLib";
            }
            case 0: {
                return "No compression";
            }
        }
        return "Unknown";
    }

    static String hashToString(int n) {
        switch (n) {
            case 4: {
                return "Double SHA";
            }
            case 7: {
                return "Haval 5";
            }
            case 5: {
                return "MD 2";
            }
            case 1: {
                return "MD 5";
            }
            case 3: {
                return "RipeMD 160";
            }
            case 2: {
                return "SHA1";
            }
            case 11: {
                return "SHA2 - 224";
            }
            case 8: {
                return "SHA2 - 256";
            }
            case 9: {
                return "SHA2 - 384";
            }
            case 10: {
                return "SHA2 - 512";
            }
            case 6: {
                return "Tiger 192";
            }
        }
        return "Unknown";
    }

    static String cypherToString(int n) {
        switch (n) {
            case 7: {
                return "AES 128";
            }
            case 8: {
                return "AES 192";
            }
            case 9: {
                return "AES 256";
            }
            case 4: {
                return "Blowfish";
            }
            case 3: {
                return "Cast 5";
            }
            case 6: {
                return "DES";
            }
            case 1: {
                return "IDEA";
            }
            case 5: {
                return "Safer";
            }
            case 2: {
                return "3 DES";
            }
            case 10: {
                return "Twofish";
            }
        }
        return "Unknown";
    }

    private static String padRight(String string, int n) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(string);
        if (string.length() < n) {
            for (int i = 0; i < n - string.length(); ++i) {
                stringBuffer.append(' ');
            }
        }
        return stringBuffer.toString();
    }

    private void Debug(String string) {
        if (log.isLoggable(Level.FINE)) {
            log.fine(string);
        }
    }

    private void Debug(String string, String string2) {
        if (log.isLoggable(Level.FINE)) {
            log.fine(MessageFormat.format(string, string2));
        }
    }

    private String normalizeHexId(String string) {
        String string2 = string.startsWith("0x") ? string.substring(2) : string;
        return string2.toUpperCase();
    }

    public boolean isSkipLucasLehmerPrimeTest() {
        return this.skipLucasLehmerPrimeTest;
    }

    public void setSkipLucasLehmerPrimeTest(boolean bl) {
        this.skipLucasLehmerPrimeTest = bl;
    }

    public boolean isFastElGamalGeneration() {
        return this.fastElGamalGeneration;
    }

    public void setFastElGamalGeneration(boolean bl) {
        this.fastElGamalGeneration = bl;
    }

    public int getMaxTrustDepth() {
        return this.maxTrustDepthCheck;
    }

    public void setMaxTrustDepth(int n) {
        this.maxTrustDepthCheck = n;
    }

    public int getMarginalsNeeded() {
        return this.marginalsNeeded;
    }

    public void setMarginalsNeeded(int n) {
        this.marginalsNeeded = n;
    }

    public boolean isCaseSensitiveMatchUserIds() {
        return this.caseSensitiveMatch;
    }

    public void setCaseSensitiveMatchUserIds(boolean bl) {
        this.caseSensitiveMatch = bl;
    }

    public static String keyId2Hex(long l) {
        return KeyPairInformation.keyId2Hex(l);
    }

    public static String keyIdToHex(long l) {
        return KeyPairInformation.keyIdToHex(l);
    }

    public static String keyIdToLongHex(long l) {
        return KeyPairInformation.keyIdToLongHex(l);
    }

    static String implode(String string, String[] stringArray) {
        String string2 = "";
        for (int i = 0; i < stringArray.length; ++i) {
            string2 = string2 + (i == stringArray.length - 1 ? stringArray[i] : stringArray[i] + string);
        }
        return string2;
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
            byte[] byArray3 = Arrays.copyOf((byte[])byArray2, (int)16);
            return byArray3;
        }
        finally {
            java.util.Arrays.fill(byArray2, (byte)0);
        }
    }

    public String getPassword() {
        if (this.password == null) {
            return null;
        }
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(this.passKeyListener == null ? this.xKey : this.paddAesKey(this.passKeyListener.getKey(this)), "AES");
            return (String)this.password.getObject(secretKeySpec);
        }
        catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    public void setPassword(String string) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(this.passKeyListener == null ? this.xKey : this.paddAesKey(this.passKeyListener.getKey(this)), "AES");
            cipher.init(1, secretKeySpec);
            this.password = new SealedObject((Serializable)((Object)string), cipher);
        }
        catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum KeyCertificationType {
        GenericCertification(16),
        PersonalCertification(17),
        CasualCertification(18),
        PositiveCertification(19);

        private int value;

        private KeyCertificationType(int n2) {
            this.value = n2;
        }

        public int getValue() {
            return this.value;
        }
    }
}

