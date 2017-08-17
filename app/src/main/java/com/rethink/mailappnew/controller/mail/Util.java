package com.rethink.mailappnew.controller.mail;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;

/**
 * Just to covert to base64
 * Created by Shibin.co
 */

public class Util {

    public static String base64UrlDecode(String input) {
        if (input != null && !input.isEmpty()) {
            String result;
            Base64 decoder = new Base64(true);
            byte[] decodedBytes = decoder.decode(input);
            result = new String(decodedBytes);
            return result;
        }
        return null;
    }
}









