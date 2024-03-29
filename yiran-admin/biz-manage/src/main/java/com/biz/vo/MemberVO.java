package com.biz.vo;

import com.biz.entity.Member;
import lombok.Data;
import one.yiran.common.util.DateUtil;
import one.yiran.dashboard.entity.SysChannel;
import one.yiran.db.common.annotation.Search;
import org.springframework.beans.BeanUtils;

@Data
public class MemberVO {
    @Search
    private Long memberId;
    @Search
    private Long inviteMemberId;

    @Search
    private String phone;
    @Search
    private String name;
    private String avatar;
    private String token;
    private String status;
    private Boolean isLocked;
    private String createTime;
    private String loginDate;
    private String loginIp;
    private String updateTime;
    private String registerTime;
    private String createBy;
    private String updateBy;
    private Boolean isDelete;
    private String remark;

    private Long tokenExpires; //token过期时间 毫秒

    @Search
    private Long channelId;
    private String channelCode;
    private String channelName;

    public static MemberVO from(Member member, SysChannel sysChannel) {
        MemberVO memberVO = new MemberVO();
        BeanUtils.copyProperties(member, memberVO);
        memberVO.setCreateTime(DateUtil.dateTime(member.getCreateTime()));
        memberVO.setUpdateTime(DateUtil.dateTime(member.getUpdateTime()));
        memberVO.setRegisterTime(DateUtil.dateTime(member.getRegisterTime()));
        memberVO.setCreateBy(member.getCreateBy());
        memberVO.setUpdateBy(member.getUpdateBy());
        memberVO.setChannelCode(sysChannel.getChannelCode());
        memberVO.setChannelName(sysChannel.getChannelName());
        memberVO.setLoginDate(DateUtil.dateTime(member.getLoginDate()));
        memberVO.setLoginIp(member.getLoginIp());
        return memberVO;
    }

}
