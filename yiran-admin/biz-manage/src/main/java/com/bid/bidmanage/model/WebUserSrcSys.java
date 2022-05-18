package com.bid.bidmanage.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class WebUserSrcSys {

    private Long userId;
    private String loginName;
    private String phoneNumber;
    private String srcSys;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String createBy;

    private String updateBy;

}
