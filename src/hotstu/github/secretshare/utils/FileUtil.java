package hotstu.github.secretshare.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;


public class FileUtil {
    

    /**
     * 从文件名或文件路径中提取文件后缀
     * 
     * @param filename
     * @return 文件后缀如zip
     */
    public static String getsuffix(String fileName) {
        String[] filenameArray = fileName.split("\\.");
        return filenameArray[filenameArray.length - 1].toLowerCase(Locale.US);
    }

    /**
     * 从文件路径中提取文件名
     * 
     * @param filePath
     *            /foldername/filename.zip
     * @return 如filename.zip
     */
    public static String getfileName(String filePath) {
        String[] filenameArray = filePath.split("/");
        return filenameArray[filenameArray.length - 1];
    }
    

    /**
     * 根据文件后缀判断是否为图片文件
     * 
     * @param fileName
     * @return
     */
    public static boolean isImage(String fileName) {
        String suffix = getsuffix(fileName);
        return ("jpg".equals(suffix) || "png".equals(suffix) || "bmp"
                .equals(suffix));
    }

    public static boolean isZipfile(String fileName) {
        String suffix = getsuffix(fileName);
        return ("zip".equals(suffix) || "rar".equals(suffix));
    }
    
    public static String md5(byte[] content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
    
   
}
