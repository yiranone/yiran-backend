package com.biz.service.impl;


import com.biz.dao.MemberDao;
import com.biz.entity.Member;
import com.biz.entity.QMember;
import com.biz.service.MemberService;
import com.biz.vo.MemberVO;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.manage.entity.*;
import one.yiran.dashboard.vo.ChannelVO;
import one.yiran.dashboard.vo.UserPageVO;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.db.common.util.PredicateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MemberServiceImpl extends CrudBaseServiceImpl<Long, Member> implements MemberService {

    @Autowired
    private MemberDao memberDao;

    @Override
    public Member selectByPhone(Long channelId, String phone) {
        Assert.notNull(channelId,"channelId不能为空");
        Assert.hasLength(phone,"phone不能为空");
        return super.selectOne(QMember.member.channelId.eq(channelId)
                .and(QMember.member.phone.eq(phone)));
    }

    @Override
    public PageModel<MemberVO> selectPageDetail(PageRequest pageRequest, MemberVO m,
                                                Long channelId) {

        QMember qMember = QMember.member;
        QSysChannel qSysChannel = QSysChannel.sysChannel;

        Predicate[] pres = PredicateBuilder.builder()
                .addLikeIfNotBlank(qMember.name, m.getName())
                .addEqualIfNotBlank(qSysChannel.channelId, channelId)
                .addEntityByAnnotation(m,qMember)
                .toArray();

        JPAQuery<Tuple> q = queryFactory.select(qMember,qSysChannel).from(qMember).leftJoin(qSysChannel)
                .on(qSysChannel.channelId.eq(qMember.channelId)).where(pres);

        PageRequestUtil.injectQuery(pageRequest,q,qMember,qSysChannel);

        List<Tuple> ts = q.fetch();
        List<MemberVO> memberVOS = new ArrayList<>();
        for (Tuple r : ts) {
            Member member = r.get(qMember);
            SysChannel sysChannel = r.get(qSysChannel);
            memberVOS.add(MemberVO.from(member,sysChannel));
        }
        long count = q.fetchCount();
        return PageModel.instance(count,memberVOS);
    }

    @Transactional
    @Override
    public Member recordLoginFail(Long memberId, long passwordErrorCount) throws BusinessException {
        Assert.notNull(memberId, "会员不能为空");
        Member db = selectByPId(memberId);
        if(db != null) {
            db.setPasswordErrorCount(passwordErrorCount);
            db.setPasswordErrorTime(new Date());
            db = memberDao.save(db);
        }
        return db;
    }
    @Transactional
    @Override
    public Member resetLoginFail(Long memberId) throws BusinessException {
        Assert.notNull(memberId, "会员不能为空");
        Member db = selectByPId(memberId);
        if(db != null && db.getPasswordErrorTime() != null) {
            db.setPasswordErrorCount(0L);
            db.setPasswordErrorTime(null);
            db = memberDao.save(db);
        }
        return db;
    }
}
