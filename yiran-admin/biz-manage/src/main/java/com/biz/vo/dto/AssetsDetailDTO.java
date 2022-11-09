package com.biz.vo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetsDetailDTO {
    private Long id;
    private Long userId;

    private String txnAmount;
    private String typeDesc;
    private String dealDesc;
    private String longTimeDesc;
    private String totalDesc;
}
