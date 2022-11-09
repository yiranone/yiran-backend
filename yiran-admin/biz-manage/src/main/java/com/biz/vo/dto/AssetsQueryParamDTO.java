package com.biz.vo.dto;

import lombok.Data;
import one.yiran.common.domain.PageRequest;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class AssetsQueryParamDTO extends PageRequest {
    private String channelCode;
    //common
    private Long memberId;
    // user
    private Long inviteMemberId;

    private Integer vipLevel;
    private String agentPassword;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isAgent;
    private Boolean isVip;

    private Date beginDate;
    private Date endDate;

    // 通证类型
//    @JSONField(name="moneyTypes", deserializeUsing = CommaSplitList.class)
    private List<Integer> moneyTypes; // 0:普通算力,1:充值,2:寄售算力,10:返现

    private String currency;
}
