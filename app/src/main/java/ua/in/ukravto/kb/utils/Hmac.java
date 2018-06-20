package ua.in.ukravto.kb.utils;

import android.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Hmac {

    public static String sha256_hmac(String str, String secret) throws Exception{

        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");

        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKey);

        return Base64.encodeToString(sha256_HMAC.doFinal(str.getBytes()), Base64.DEFAULT);
    }

}
