package hotstu.github.secretshare.utils.cipher;


/**
 * it seams Java only reads keys in PKCS#8 format 
 * the one with beginning of -----BEGIN PRIVATE KEY-----,-----BEGIN PUBLIC KEY-----
 * use openssl to convert PKCS#1 to PKCS#8.
 * @author foo
 *
 */
public class RSAKeyGen {
    private static String publicKey = 
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDb7trySThInjEVMa+DhkMa7qVP\n"+
            "9791aXQAbCGzfkS86J74X/lcphaAOv3gr+bpwIfdO9mJlna9wd4nvU++pMluLkI6\n"+
            "xLXR/E7Pz7scv4tY2d9HLCFM/h1r0ts4JglphAbvIuqywSdom/WlWlk1NR/vgq/8\n"+
            "60ybMR6tYZROTokYHwIDAQAB";
  
    /** 
     * 取得公钥  in base64
     *  
     * @param keyMap 
     * @return 
     * @throws Exception 
     */  
    public static String getPublicKey()  {
        return publicKey;
    }
          
   

}
