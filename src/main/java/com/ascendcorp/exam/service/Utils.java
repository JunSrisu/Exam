package com.ascendcorp.exam.service;

import java.util.ResourceBundle;

public class Utils {
    /**
     * Get messages resource from properties.
     *
     * @param key - the messages key msg.abc
     * @return String value of messages key.
     */
    public static String getMessagesProperties(String key) {
        ResourceBundle messageBundle = ResourceBundle.getBundle("messages");
        return messageBundle.getString(key);
    }


}
