package com.biz.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class FileCoinUtil {

    public static String formatCoinDecimal(BigDecimal v){
        if(v == null)
            return "";
        DecimalFormat decimalFormat = new DecimalFormat("##########.##########");
        return decimalFormat.format(v.setScale(8, RoundingMode.HALF_DOWN));
    }

    public static String formatCoinDecimal6(BigDecimal v){
        if(v == null)
            return "";
        DecimalFormat decimalFormat = new DecimalFormat("##########.##########");
        return decimalFormat.format(v.setScale(6, RoundingMode.HALF_DOWN));
    }

    public static String formatCoinDecimal2(BigDecimal v){
        if(v == null)
            return "";
        DecimalFormat decimalFormat = new DecimalFormat("##########.##########");
        return decimalFormat.format(v.setScale(2, RoundingMode.HALF_DOWN));
    }

    public static String formatCoinDecimalWithSign(BigDecimal v){
        if(v!= null && v.compareTo(BigDecimal.ZERO) > 0) {
            String rt = FileCoinUtil.formatCoinDecimal(v);
            return "+" + rt;
        } else {
            return FileCoinUtil.formatCoinDecimal(v);
        }
    }

    public static String formatCNYDecimal(BigDecimal v){
        if(v == null)
            return "";
        DecimalFormat decimalFormat = new DecimalFormat("##########.##");
        return decimalFormat.format(v.setScale(2, RoundingMode.HALF_DOWN));
    }
}
