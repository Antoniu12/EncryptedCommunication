package com.chat_client_B.B.EncryptionMethods.DiffieHellman;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class DiffieHellman {

    private static BigInteger p;
    private static BigInteger g;
    private static BigInteger privateKey;
    private static BigInteger publicKey;
    private static BigInteger sharedSecret;
    private static SecretKey aesKey;

    private static final SecureRandom random = new SecureRandom();

    public static void generateParams() {
        p = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E08" +
                "8A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD" +
                "3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7" +
                "EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5" +
                "AE9F24117C4B1FE649286651ECE65381FFFFFFFFFFFFFFFF", 16);
        g = BigInteger.valueOf(2);
        generateKeys();
    }

    public static void init(BigInteger receivedP, BigInteger receivedG) {
        p = receivedP;
        g = receivedG;
        generateKeys();
    }

    private static void generateKeys() {
        privateKey = new BigInteger(256, random);
        publicKey = g.modPow(privateKey, p);
    }

    public static BigInteger getP() {
        return p;
    }

    public static BigInteger getG() {
        return g;
    }

    public static BigInteger getPublicKey() {
        return publicKey;
    }

    public static SecretKey computeSharedKey(BigInteger peerPublic) throws Exception {
        sharedSecret = peerPublic.modPow(privateKey, p);
        aesKey = deriveAESKey(sharedSecret);
        return aesKey;
    }

    private static SecretKey deriveAESKey(BigInteger shared) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] fullKey = sha256.digest(shared.toByteArray());
        return new SecretKeySpec(fullKey, 0, 16, "AES");
    }

    public static String encrypt(String plaintext) throws Exception {
        if (aesKey == null) throw new IllegalStateException("Shared key not established.");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String base64Ciphertext) throws Exception {
        if (aesKey == null) throw new IllegalStateException("Shared key not established.");

        byte[] combined = Base64.getDecoder().decode(base64Ciphertext);
        byte[] iv = new byte[16];
        byte[] ciphertext = new byte[combined.length - 16];

        System.arraycopy(combined, 0, iv, 0, 16);
        System.arraycopy(combined, 16, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));

        return new String(cipher.doFinal(ciphertext));
    }

    public static boolean isKeyEstablished() {
        return aesKey != null;
    }
}
