package com.biz.vo;

import com.biz.entity.Member;
import lombok.Data;
import one.yiran.dashboard.manage.entity.SysChannel;
import org.springframework.beans.BeanUtils;

@Data
public class MemberVO {
    private Long memberId;

    private String phone;
    private String name;
    private String avatar;
    private String token;
    private Boolean isLocked;
    private Long tokenExpires; //token过期时间 毫秒

    private Long channelId;
    private String channelCode;
    private String channelName;

    public static MemberVO from(Member member, SysChannel sysChannel) {
        MemberVO memberVO = new MemberVO();
        BeanUtils.copyProperties(member,memberVO);
        memberVO.setChannelCode(sysChannel.getChannelCode());
        memberVO.setChannelName(sysChannel.getChannelName());
        return memberVO;
    }

}
