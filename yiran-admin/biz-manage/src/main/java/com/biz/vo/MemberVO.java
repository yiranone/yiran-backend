package com.biz.vo;

import com.biz.entity.Member;
import lombok.Data;
import one.yiran.common.util.DateUtil;
import one.yiran.dashboard.manage.entity.SysChannel;
import one.yiran.db.common.annotation.Search;
import org.springframework.beans.BeanUtils;

@Data
public class MemberVO {
    @Search
    private Long memberId;

    @Search
    private String phone;
    @Search
    private String name;
    private String avatar;
    private String token;
    private String status;
    private Boolean isLocked;
    private String createTime;
    private String updateTime;
    private String createBy;
    private String updateBy;

    private Long tokenExpires; //token过期时间 毫秒

    @Search
    private Long channelId;
    private String channelCode;
    private String channelName;

    public static MemberVO from(Member member, SysChannel sysChannel) {
        MemberVO memberVO = new MemberVO();
        BeanUtils.copyProperties(member,memberVO);
        memberVO.setCreateTime(DateUtil.dateTime(member.getCreateTime()));
        memberVO.setUpdateTime(DateUtil.dateTime(member.getUpdateTime()));
        memberVO.setCreateBy(member.getCreateBy());
        memberVO.setUpdateBy(member.getUpdateBy());
        memberVO.setChannelCode(sysChannel.getChannelCode());
        memberVO.setChannelName(sysChannel.getChannelName());
        return memberVO;
    }

}
