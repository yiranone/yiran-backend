package com.biz.util;

import org.apache.commons.lang3.RandomStringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TxnUtils {
    public static String generateTxnId() {
        String times = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String randString = RandomStringUtils.randomNumeric(6);
        return times + randString;
    }
}
