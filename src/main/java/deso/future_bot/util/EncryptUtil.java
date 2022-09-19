package deso.future_bot.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class EncryptUtil {
    private Cipher cipher;
    PrivateKey privateKey;
    PublicKey pubKey;

    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair pair = keyGen.generateKeyPair();
        System.out.println(Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
        System.out.println(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
    }

    public EncryptUtil(@Value("${key.encrypt.public}") String encryptPublicKey, @Value("${key.encrypt.private}") String encryptPrivateKey) {
        try {
            this.cipher = Cipher.getInstance("RSA");
            byte[] publicBytes = Base64.getDecoder().decode(encryptPublicKey);
            byte[] privateBytes = Base64.getDecoder().decode(encryptPrivateKey);
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicBytes);
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.pubKey = keyFactory.generatePublic(publicSpec);
            this.privateKey = keyFactory.generatePrivate(privateSpec);
        } catch (Exception var8) {
            var8.printStackTrace();
        }

    }

    public String encrypt(String content) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] contentBytes = content.getBytes();
        this.cipher.init(1, this.pubKey);
        byte[] cipherContent = this.cipher.doFinal(contentBytes);
        return Base64.getEncoder().encodeToString(cipherContent);
    }

    public String decrypt(String cipherContent) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] cipherContentBytes = Base64.getDecoder().decode(cipherContent.getBytes());
        this.cipher.init(2, this.privateKey);
        byte[] decryptedContent = this.cipher.doFinal(cipherContentBytes);
        return new String(decryptedContent);
    }
}
