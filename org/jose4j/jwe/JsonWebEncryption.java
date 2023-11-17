/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwe;

import java.security.Key;
import java.util.Map;
import org.jose4j.base64url.Base64Url;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmFactory;
import org.jose4j.jwa.AlgorithmFactoryFactory;
import org.jose4j.jwe.ContentEncryptionAlgorithm;
import org.jose4j.jwe.ContentEncryptionKeyDescriptor;
import org.jose4j.jwe.ContentEncryptionKeys;
import org.jose4j.jwe.ContentEncryptionParts;
import org.jose4j.jwe.KeyManagementAlgorithm;
import org.jose4j.jwx.CompactSerializer;
import org.jose4j.jwx.Headers;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.lang.InvalidAlgorithmException;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.StringUtil;
import org.jose4j.zip.CompressionAlgorithm;

public class JsonWebEncryption
extends JsonWebStructure {
    public static final short COMPACT_SERIALIZATION_PARTS = 5;
    private Base64Url base64url = new Base64Url();
    private String plaintextCharEncoding = "UTF-8";
    private byte[] plaintext;
    byte[] encryptedKey;
    byte[] iv;
    byte[] ciphertext;
    byte[] contentEncryptionKey;
    private AlgorithmConstraints contentEncryptionAlgorithmConstraints = AlgorithmConstraints.NO_CONSTRAINTS;

    public void setPlainTextCharEncoding(String plaintextCharEncoding) {
        this.plaintextCharEncoding = plaintextCharEncoding;
    }

    public void setPlaintext(byte[] plaintext) {
        this.plaintext = plaintext;
    }

    public void setPlaintext(String plaintext) {
        this.plaintext = StringUtil.getBytesUnchecked(plaintext, this.plaintextCharEncoding);
    }

    public String getPlaintextString() throws JoseException {
        return StringUtil.newString(this.getPlaintextBytes(), this.plaintextCharEncoding);
    }

    public byte[] getPlaintextBytes() throws JoseException {
        if (this.plaintext == null) {
            this.decrypt();
        }
        return this.plaintext;
    }

    @Override
    public String getPayload() throws JoseException {
        return this.getPlaintextString();
    }

    @Override
    public void setPayload(String payload) {
        this.setPlaintext(payload);
    }

    public void setEncryptionMethodHeaderParameter(String enc) {
        this.setHeader("enc", enc);
    }

    public String getEncryptionMethodHeaderParameter() {
        return this.getHeader("enc");
    }

    public void setCompressionAlgorithmHeaderParameter(String zip) {
        this.setHeader("zip", zip);
    }

    public String getCompressionAlgorithmHeaderParameter() {
        return this.getHeader("zip");
    }

    public void enableDefaultCompression() {
        this.setCompressionAlgorithmHeaderParameter("DEF");
    }

    public void setContentEncryptionAlgorithmConstraints(AlgorithmConstraints contentEncryptionAlgorithmConstraints) {
        this.contentEncryptionAlgorithmConstraints = contentEncryptionAlgorithmConstraints;
    }

    public ContentEncryptionAlgorithm getContentEncryptionAlgorithm() throws InvalidAlgorithmException {
        String encValue = this.getEncryptionMethodHeaderParameter();
        if (encValue == null) {
            throw new InvalidAlgorithmException("Content encryption header (enc) not set.");
        }
        this.contentEncryptionAlgorithmConstraints.checkConstraint(encValue);
        AlgorithmFactoryFactory factoryFactory = AlgorithmFactoryFactory.getInstance();
        AlgorithmFactory<ContentEncryptionAlgorithm> factory = factoryFactory.getJweContentEncryptionAlgorithmFactory();
        return factory.getAlgorithm(encValue);
    }

    public KeyManagementAlgorithm getKeyManagementModeAlgorithm() throws InvalidAlgorithmException {
        return this.getKeyManagementModeAlgorithm(true);
    }

    KeyManagementAlgorithm getKeyManagementModeAlgorithm(boolean checkConstraints) throws InvalidAlgorithmException {
        String algo = this.getAlgorithmHeaderValue();
        if (algo == null) {
            throw new InvalidAlgorithmException("Encryption key management algorithm header (alg) not set.");
        }
        if (checkConstraints) {
            this.getAlgorithmConstraints().checkConstraint(algo);
        }
        AlgorithmFactoryFactory factoryFactory = AlgorithmFactoryFactory.getInstance();
        AlgorithmFactory<KeyManagementAlgorithm> factory = factoryFactory.getJweKeyManagementAlgorithmFactory();
        return factory.getAlgorithm(algo);
    }

    @Override
    public KeyManagementAlgorithm getAlgorithmNoConstraintCheck() throws InvalidAlgorithmException {
        return this.getKeyManagementModeAlgorithm(false);
    }

    @Override
    public KeyManagementAlgorithm getAlgorithm() throws InvalidAlgorithmException {
        return this.getKeyManagementModeAlgorithm();
    }

    @Override
    protected void setCompactSerializationParts(String[] parts) throws JoseException {
        if (parts.length != 5) {
            throw new JoseException("A JWE Compact Serialization must have exactly 5 parts separated by period ('.') characters");
        }
        this.setEncodedHeader(parts[0]);
        this.encryptedKey = this.base64url.base64UrlDecode(parts[1]);
        this.setEncodedIv(parts[2]);
        String encodedCiphertext = parts[3];
        this.checkNotEmptyPart(encodedCiphertext, "Encoded JWE Ciphertext");
        this.ciphertext = this.base64url.base64UrlDecode(encodedCiphertext);
        String encodedAuthenticationTag = parts[4];
        this.checkNotEmptyPart(encodedAuthenticationTag, "Encoded JWE Authentication Tag");
        byte[] tag = this.base64url.base64UrlDecode(encodedAuthenticationTag);
        this.setIntegrity(tag);
    }

    @Override
    public void setFlattenedJsonSerialization(String json) throws JoseException {
        Map<String, Object> parsedJson = JsonUtil.parseJson(json);
        if (parsedJson.containsKey("unprotected") || parsedJson.containsKey("header")) {
            throw new JoseException("Only protected headers are currently supported.");
        }
        if (!parsedJson.containsKey("protected")) {
            throw new JoseException("No protected header in JSON serialization");
        }
        if (!parsedJson.containsKey("ciphertext")) {
            throw new JoseException("JWE contains no ciphertext");
        }
        if (parsedJson.containsKey("aad")) {
            throw new JoseException("AAD not currently supported");
        }
        String encodedHeader = (String)parsedJson.get("protected");
        this.setEncodedHeader(encodedHeader);
        if (parsedJson.containsKey("encrypted_key")) {
            this.encryptedKey = this.base64url.base64UrlDecode((String)parsedJson.get("encrypted_key"));
        }
        if (parsedJson.containsKey("iv")) {
            this.setEncodedIv((String)parsedJson.get("iv"));
        }
        String encodedCiphertext = (String)parsedJson.get("ciphertext");
        this.checkNotEmptyPart(encodedCiphertext, "Encoded JWE Ciphertext");
        this.ciphertext = this.base64url.base64UrlDecode(encodedCiphertext);
        if (parsedJson.containsKey("tag")) {
            String encodedAuthenticationTag = (String)parsedJson.get("tag");
            this.checkNotEmptyPart(encodedAuthenticationTag, "Encoded JWE Authentication Tag");
            byte[] tag = this.base64url.base64UrlDecode(encodedAuthenticationTag);
            this.setIntegrity(tag);
        }
    }

    private void decrypt() throws JoseException {
        KeyManagementAlgorithm keyManagementModeAlg = this.getKeyManagementModeAlgorithm();
        ContentEncryptionAlgorithm contentEncryptionAlg = this.getContentEncryptionAlgorithm();
        ContentEncryptionKeyDescriptor contentEncryptionKeyDesc = contentEncryptionAlg.getContentEncryptionKeyDescriptor();
        if (this.isDoKeyValidation()) {
            keyManagementModeAlg.validateDecryptionKey(this.getKey(), contentEncryptionAlg);
        }
        this.checkCrit();
        Key cek = keyManagementModeAlg.manageForDecrypt(this.getKey(), this.getEncryptedKey(), contentEncryptionKeyDesc, this.getHeaders(), this.getProviderCtx());
        ContentEncryptionParts contentEncryptionParts = new ContentEncryptionParts(this.iv, this.ciphertext, this.getIntegrity());
        byte[] aad = this.getEncodedHeaderAsciiBytesForAdditionalAuthenticatedData();
        byte[] decrypted = contentEncryptionAlg.decrypt(contentEncryptionParts, aad, cek.getEncoded(), this.getHeaders(), this.getProviderCtx());
        decrypted = this.decompress(this.getHeaders(), decrypted);
        this.setPlaintext(decrypted);
    }

    public byte[] getEncryptedKey() {
        return this.encryptedKey;
    }

    byte[] getEncodedHeaderAsciiBytesForAdditionalAuthenticatedData() {
        String encodedHeader = this.getEncodedHeader();
        return StringUtil.getBytesAscii(encodedHeader);
    }

    byte[] decompress(Headers headers, byte[] data) throws JoseException {
        String zipHeaderValue = headers.getStringHeaderValue("zip");
        if (zipHeaderValue != null) {
            AlgorithmFactoryFactory factoryFactory = AlgorithmFactoryFactory.getInstance();
            AlgorithmFactory<CompressionAlgorithm> zipAlgFactory = factoryFactory.getCompressionAlgorithmFactory();
            CompressionAlgorithm compressionAlgorithm = zipAlgFactory.getAlgorithm(zipHeaderValue);
            data = compressionAlgorithm.decompress(data);
        }
        return data;
    }

    byte[] compress(Headers headers, byte[] data) throws InvalidAlgorithmException {
        String zipHeaderValue = headers.getStringHeaderValue("zip");
        if (zipHeaderValue != null) {
            AlgorithmFactoryFactory factoryFactory = AlgorithmFactoryFactory.getInstance();
            AlgorithmFactory<CompressionAlgorithm> zipAlgFactory = factoryFactory.getCompressionAlgorithmFactory();
            CompressionAlgorithm compressionAlgorithm = zipAlgFactory.getAlgorithm(zipHeaderValue);
            data = compressionAlgorithm.compress(data);
        }
        return data;
    }

    @Override
    public String getCompactSerialization() throws JoseException {
        KeyManagementAlgorithm keyManagementModeAlg = this.getKeyManagementModeAlgorithm();
        ContentEncryptionAlgorithm contentEncryptionAlg = this.getContentEncryptionAlgorithm();
        ContentEncryptionKeyDescriptor contentEncryptionKeyDesc = contentEncryptionAlg.getContentEncryptionKeyDescriptor();
        Key managementKey = this.getKey();
        if (this.isDoKeyValidation()) {
            keyManagementModeAlg.validateEncryptionKey(this.getKey(), contentEncryptionAlg);
        }
        ContentEncryptionKeys contentEncryptionKeys = keyManagementModeAlg.manageForEncrypt(managementKey, contentEncryptionKeyDesc, this.getHeaders(), this.contentEncryptionKey, this.getProviderCtx());
        this.setContentEncryptionKey(contentEncryptionKeys.getContentEncryptionKey());
        this.encryptedKey = contentEncryptionKeys.getEncryptedKey();
        byte[] aad = this.getEncodedHeaderAsciiBytesForAdditionalAuthenticatedData();
        byte[] contentEncryptionKey = contentEncryptionKeys.getContentEncryptionKey();
        byte[] plaintextBytes = this.plaintext;
        if (plaintextBytes == null) {
            throw new NullPointerException("The plaintext payload for the JWE has not been set.");
        }
        plaintextBytes = this.compress(this.getHeaders(), plaintextBytes);
        ContentEncryptionParts contentEncryptionParts = contentEncryptionAlg.encrypt(plaintextBytes, aad, contentEncryptionKey, this.getHeaders(), this.getIv(), this.getProviderCtx());
        this.setIv(contentEncryptionParts.getIv());
        this.ciphertext = contentEncryptionParts.getCiphertext();
        String encodedIv = this.base64url.base64UrlEncode(contentEncryptionParts.getIv());
        String encodedCiphertext = this.base64url.base64UrlEncode(contentEncryptionParts.getCiphertext());
        String encodedTag = this.base64url.base64UrlEncode(contentEncryptionParts.getAuthenticationTag());
        byte[] encryptedKey = contentEncryptionKeys.getEncryptedKey();
        String encodedEncryptedKey = this.base64url.base64UrlEncode(encryptedKey);
        return CompactSerializer.serialize(this.getEncodedHeader(), encodedEncryptedKey, encodedIv, encodedCiphertext, encodedTag);
    }

    @Override
    public String getFlattenedJsonSerialization() throws JoseException {
        KeyManagementAlgorithm keyManagementModeAlg = this.getKeyManagementModeAlgorithm();
        ContentEncryptionAlgorithm contentEncryptionAlg = this.getContentEncryptionAlgorithm();
        ContentEncryptionKeyDescriptor contentEncryptionKeyDesc = contentEncryptionAlg.getContentEncryptionKeyDescriptor();
        Key managementKey = this.getKey();
        if (this.isDoKeyValidation()) {
            keyManagementModeAlg.validateEncryptionKey(this.getKey(), contentEncryptionAlg);
        }
        ContentEncryptionKeys contentEncryptionKeys = keyManagementModeAlg.manageForEncrypt(managementKey, contentEncryptionKeyDesc, this.getHeaders(), this.contentEncryptionKey, this.getProviderCtx());
        this.setContentEncryptionKey(contentEncryptionKeys.getContentEncryptionKey());
        this.encryptedKey = contentEncryptionKeys.getEncryptedKey();
        byte[] aad = this.getEncodedHeaderAsciiBytesForAdditionalAuthenticatedData();
        byte[] contentEncryptionKey = contentEncryptionKeys.getContentEncryptionKey();
        byte[] plaintextBytes = this.plaintext;
        if (plaintextBytes == null) {
            throw new NullPointerException("The plaintext payload for the JWE has not been set.");
        }
        plaintextBytes = this.compress(this.getHeaders(), plaintextBytes);
        ContentEncryptionParts contentEncryptionParts = contentEncryptionAlg.encrypt(plaintextBytes, aad, contentEncryptionKey, this.getHeaders(), this.getIv(), this.getProviderCtx());
        this.setIv(contentEncryptionParts.getIv());
        this.ciphertext = contentEncryptionParts.getCiphertext();
        String encodedIv = this.base64url.base64UrlEncode(contentEncryptionParts.getIv());
        String encodedCiphertext = this.base64url.base64UrlEncode(contentEncryptionParts.getCiphertext());
        String encodedTag = this.base64url.base64UrlEncode(contentEncryptionParts.getAuthenticationTag());
        byte[] encryptedKey = contentEncryptionKeys.getEncryptedKey();
        String encodedEncryptedKey = this.base64url.base64UrlEncode(encryptedKey);
        Map json = JsonUtil.CONTAINER_FACTORY.createObjectContainer();
        json.put("protected", this.getEncodedHeader());
        json.put("encrypted_key", encodedEncryptedKey);
        json.put("iv", encodedIv);
        json.put("ciphertext", encodedCiphertext);
        json.put("tag", encodedTag);
        return JsonUtil.toJson(json);
    }

    public byte[] getContentEncryptionKey() {
        return this.contentEncryptionKey;
    }

    public void setContentEncryptionKey(byte[] contentEncryptionKey) {
        this.contentEncryptionKey = contentEncryptionKey;
    }

    public void setEncodedContentEncryptionKey(String encodedContentEncryptionKey) {
        this.setContentEncryptionKey(Base64Url.decode(encodedContentEncryptionKey));
    }

    public byte[] getIv() {
        return this.iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public void setEncodedIv(String encodedIv) {
        this.setIv(this.base64url.base64UrlDecode(encodedIv));
    }
}

