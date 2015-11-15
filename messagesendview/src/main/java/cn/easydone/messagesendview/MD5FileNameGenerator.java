package cn.easydone.messagesendview;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Android Studio
 * User: Ailurus(ailurus@foxmail.com)
 * Date: 2015-11-15
 * Time: 09:42
 */
public class MD5FileNameGenerator {

    public MD5FileNameGenerator() {
    }

    public String generate(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}
