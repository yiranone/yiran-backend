package com.biz.vo.dto;

import com.biz.entity.MoneyApplication;
import com.biz.util.FileCoinUtil;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
public class ChargeWithdrawDetailDTO {
    private String currency;
    private String statusDesc;
    private String createTimeDesc;
    private String finishTimeDesc;
    private String amount;
    private String txnHash;
    private String fromAddress;

    public static ChargeWithdrawDetailDTO from(MoneyApplication application){
        DateTimeFormatter longSdf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime createTime = application.getCreateTime();
        LocalDateTime finishTime = application.getFinishTime();
        return ChargeWithdrawDetailDTO.builder()
                .currency(application.getCurrency())
                .amount(FileCoinUtil.formatCoinDecimalWithSign(application.getAmount()))
                .createTimeDesc(createTime == null ? "" : createTime.format(longSdf))
                .finishTimeDesc(finishTime == null ? "" : finishTime.format(longSdf))
                .statusDesc(resultDesc(application))
                .txnHash(application.getTxnHash())
                .fromAddress(application.getFromAddress())
                .build();
    }

    public static String resultDesc(MoneyApplication application){
        String pre = "";
        if(application.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            pre = "Withdraw ";
        } else if(application.getAmount().compareTo(BigDecimal.ZERO) > 0){
            pre = "Charge ";
        }
        if(application.getState().intValue() == 5){
            pre += "Success";
        }else if(application.getState().intValue() == 4){
            pre += "Processing";
        } else if(application.getState().intValue() == 3){
            pre += "Fail";
        } else if(application.getState().intValue() == 2){
            pre += "Reject";
        } else if(application.getState().intValue() == 1){
            //pre += "已批准处理中";
            pre += "Processing";
        } else if(application.getState().intValue() == 0){
            pre += "Applying";
        } else {

        }
        return pre;
    }
}
