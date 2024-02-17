package com.google.utils;

import org.testng.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataConverter {

    public DataConverter() {

    }

    public static String parseTextValue(String label, String regularExpression) {
        Matcher qtyMatcher = Pattern.compile(regularExpression).matcher(label);
        Assert.assertTrue(qtyMatcher.find(), "Unable to extract Due tasks value!");
        return qtyMatcher.group();
    }
}
