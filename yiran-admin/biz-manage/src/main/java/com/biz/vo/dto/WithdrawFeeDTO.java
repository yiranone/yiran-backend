package com.biz.vo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WithdrawFeeDTO {
    private Long memberId;
    private String withdrawAmount;
    private String feeType;
    private String feeValue;
    private String feeAmount;
    private String settAmount;
    private String currency;
}
