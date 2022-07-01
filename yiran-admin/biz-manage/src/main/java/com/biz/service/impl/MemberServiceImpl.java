package com.biz.service.impl;


import com.biz.entity.Member;
import com.biz.entity.QMember;
import com.biz.service.MemberService;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class MemberServiceImpl extends CrudBaseServiceImpl<Long, Member> implements MemberService {

    @Override
    public Member selectByPhone(Long channelId, String phone) {
        Assert.notNull(channelId,"channelId不能为空");
        Assert.hasLength(phone,"phone不能为空");
        return super.selectOne(QMember.member.channelId.eq(channelId)
                .and(QMember.member.phone.eq(phone)));
    }
}
