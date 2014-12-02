package hotstu.github.secretshare.utils.cipher;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/** 
 * RSA安全编码组件 
 *  
 * @author  
 * @version 1.0 
 * @since 1.0 
 */  
public abstract class RSACoder {  
    public static final String KEY_ALGORITHM = "RSA";  
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";  

   
    /** 
     * 用私钥对信息生成数字签名 
     *  
     * @param data 
     *            加密数据 
     * @param privateKey 
     *            私钥 
     *  
     * @return 
     * @throws Exception 
     */  
    public static String sign(byte[] data, String privateKey) throws Exception {  
        // 解密由base64编码的私钥  
        byte[] keyBytes = Base64.decode(privateKey,Base64.DEFAULT);  

        // 构造PKCS8EncodedKeySpec对象  
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);  

        // KEY_ALGORITHM 指定的加密算法  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  

        // 取私钥匙对象  
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);  

        // 用私钥对信息生成数字签名  
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);  
        signature.initSign(priKey);  
        signature.update(data);  

        return Base64.encodeToString(signature.sign(), Base64.URL_SAFE|Base64.NO_WRAP);  
    }  

    /** 
     * 校验数字签名 
     *  
     * @param data 
     *            加密数据 
     * @param publicKey 
     *            公钥 
     * @param sign 
     *            数字签名 
     *  
     * @return 校验成功返回true 失败返回false 
     * @throws Exception 
     *  
     */  
    public static boolean verify(byte[] data, String publicKey, String sign)  
            throws Exception {  

        // 解密由base64编码的公钥  
        byte[] keyBytes = Base64.decode(publicKey, Base64.DEFAULT);  

        // 构造X509EncodedKeySpec对象  
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);  

        // KEY_ALGORITHM 指定的加密算法  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  

        // 取公钥匙对象  
        PublicKey pubKey = keyFactory.generatePublic(keySpec);  

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);  
        signature.initVerify(pubKey);  
        signature.update(data);  

        // 验证签名是否正常  
        return signature.verify(Base64.decode(sign, Base64.URL_SAFE|Base64.NO_WRAP));  
    }  

    /** 
     * 解密<br> 
     * 用私钥解密 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static byte[] decryptByPrivateKey(byte[] data, String key)  
            throws Exception {  
        // 对密钥解密  
        byte[] keyBytes = Base64.decode(key, Base64.DEFAULT);  

        // 取得私钥  
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);  

        // 对数据解密  
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
        cipher.init(Cipher.DECRYPT_MODE, privateKey);  

        return cipher.doFinal(data);  
    }  

    /** 
     * 解密<br> 
     * 用公钥解密 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static byte[] decryptByPublicKey(byte[] data, String key)  
            throws Exception {  
        // 对密钥解密  
        byte[] keyBytes = Base64.decode(key, Base64.DEFAULT);  

        // 取得公钥  
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key publicKey = keyFactory.generatePublic(x509KeySpec);  

        // 对数据解密  
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
        cipher.init(Cipher.DECRYPT_MODE, publicKey);  

        return cipher.doFinal(data);  
    }  

    /** 
     * 加密<br> 
     * 用公钥加密 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static byte[] encryptByPublicKey(byte[] data, String key)  
            throws Exception {  
        // 对公钥解密  
        byte[] keyBytes = Base64.decode(key, Base64.DEFAULT);  

        // 取得公钥  
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key publicKey = keyFactory.generatePublic(x509KeySpec);  
        System.out.println(publicKey);

        // 对数据加密  
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);  

        return cipher.doFinal(data);  
    }  

    /** 
     * 加密<br> 
     * 用私钥加密 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static byte[] encryptByPrivateKey(byte[] data, String key)  
            throws Exception {  
        // 对密钥解密  
        byte[] keyBytes = Base64.decode(key, Base64.DEFAULT);  

        // 取得私钥  
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);  

        // 对数据加密  
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);  

        return cipher.doFinal(data);  
    }  

    
     
}  