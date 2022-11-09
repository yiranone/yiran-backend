package com.biz.vo.dto;

import lombok.Data;
import one.yiran.common.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class ChargeWithdrawQueryParamDTO extends PageRequest {
    private String channelCode;
    //common
    private Long memberId;

    private Date startTime;
    private Date endTime;
    private Boolean onlyCharge;
    private Boolean onlyWithdraw;
    private String status;
    private String currency;
}
