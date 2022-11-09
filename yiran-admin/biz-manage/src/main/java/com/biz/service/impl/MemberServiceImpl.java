package com.biz.service.impl;


import com.biz.dao.CurrencyConfigDao;
import com.biz.dao.MemberDao;
import com.biz.dao.MemberMoneyDao;
import com.biz.entity.CurrencyConfig;
import com.biz.entity.Member;
import com.biz.entity.MemberMoney;
import com.biz.entity.QMember;
import com.biz.service.MemberService;
import com.biz.util.ShareCodeUtil;
import com.biz.vo.MemberVO;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.constants.SystemConstants;
import one.yiran.dashboard.entity.*;
import one.yiran.dashboard.security.service.PasswordService;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.db.common.util.PredicateBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class MemberServiceImpl extends CrudBaseServiceImpl<Long, Member> implements MemberService {

    @Autowired
    private MemberDao memberDao;
    @Autowired
    private PasswordService passwordService;
    @Autowired
    private MemberMoneyDao memberMoneyDao;
    @Autowired
    private CurrencyConfigDao currencyConfigDao;

    @Override
    public Member selectByPhone(Long channelId, String phone) {
        Assert.notNull(channelId,"channelId不能为空");
        Assert.hasLength(phone,"phone不能为空");
        return super.selectOne(QMember.member.channelId.eq(channelId)
                .and(QMember.member.phone.eq(phone)));
    }

    @Override
    public Member selectByEmail(Long channelId, String email) {
        Assert.notNull(channelId,"channelId不能为空");
        Assert.hasLength(email,"email不能为空");
        return super.selectOne(QMember.member.channelId.eq(channelId)
                .and(QMember.member.email.eq(email)));
    }

    @Override
    public Member selectByLoginName(Long channelId, String loginName) {
        Assert.notNull(channelId,"channelId不能为空");
        Assert.hasLength(loginName,"loginName不能为空");
        return super.selectOne(QMember.member.channelId.eq(channelId)
                .and(QMember.member.loginName.eq(loginName)));
    }

    @Override
    public PageModel<MemberVO> selectPageDetail(PageRequest pageRequest, MemberVO search,
                                                Long channelId) {

        QMember qMember = QMember.member;
        QSysChannel qSysChannel = QSysChannel.sysChannel;

        Predicate[] pres = PredicateBuilder.builder()
                //.addLikeIfNotBlank(qMember.name, search.getName())
                .addEqualIfNotBlank(qSysChannel.channelId, channelId)
                .addEntityByAnnotation(search,qMember)
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

    @Transactional
    @Override
    public void tryInitMemberMoney(Long memberId) {
        Member member = memberDao.findById(memberId).orElse(null);
        List<CurrencyConfig> currencyList = currencyConfigDao.findAllByChannelIdOrderBySortNoAsc(member.getChannelId());
        currencyList.stream().forEach(d -> {
            MemberMoney money = memberMoneyDao.findByMemberIdAndCurrency(memberId, d.getCurrency());
            if (money == null) {
                money = new MemberMoney();
                money.setMemberId(memberId);
                money.setCurrency(d.getCurrency());
                money.setAvailableAmount(BigDecimal.ZERO);
                money.setUnavailableAmount(BigDecimal.ZERO);
                money.setWithdrawingAmount(BigDecimal.ZERO);
                money.setWithdrawedAmount(BigDecimal.ZERO);
                money.setCreateTime(LocalDateTime.now());
                log.info("创建会员账户{} {}", memberId, d.getCurrency());
                memberMoneyDao.save(money);
            }
        });
    }

    @Override
    public void expectNew(Member member) {
        if (!StringUtils.isEmpty(member.getLoginName()) &&
                selectByLoginName(member.getChannelId(), member.getLoginName()) != null) {
            throw BusinessException.build("用户名已经存在:" + member.getLoginName());
        }
        if (!StringUtils.isEmpty(member.getEmail()) &&
                selectByEmail(member.getChannelId(), member.getEmail()) != null) {
            throw BusinessException.build("邮箱已经被使用:" + member.getEmail());
        }
        if (!StringUtils.isEmpty(member.getPhone()) &&
                selectByPhone(member.getChannelId(), member.getPhone()) != null) {
            throw BusinessException.build("该号码已被使用:" + member.getPhone());
        }
    }

    @Transactional
    @Override
    public Member registerMember(Long channelId, Member member) {
        if (selectByPhone(channelId, member.getPhone()) != null) {
            throw BusinessException.build("该号码已被使用:" + member.getPhone());
        }
        if (member.getInviteMemberId() != null) {
            Member invitor = selectByPId(member.getInviteMemberId());
            if (invitor == null) {
                throw BusinessException.build("邀请人不存在");
            }
            if (!channelId.equals(invitor.getChannelId())) {
                log.info("邀请人和注册人不属于一个组织");
                throw BusinessException.build("商户邀请人不存在");
            }
            member.setInviteMemberNickName(invitor.getNickName());
        }

        if(StringUtils.isBlank(member.getAvatar())){
            //member.setAvatar(PlatformConfigUtil.get默认头像(user.getChannelCode()));
        }

        member.setStatus(SystemConstants.STATUS_ENABLE);
        member.setLoginDate(new Date());
        member.setCreateTime(new Date());
        member.setCreateBy("sys");
        if (StringUtils.isBlank(member.getPassword())) {
            throw BusinessException.build("密码不能为空");
        } else {
            member.setSalt(Global.getSalt());
            member.setPassword(passwordService.encryptPassword(member.getPassword(), Global.getSalt()));
            member.setPasswordUpdateTime(new Date());
        }
        member = memberDao.saveAndFlush(member);
        member.setMemberCode(ShareCodeUtil.generateCode(member.getMemberId()));

        List<CurrencyConfig> currencyList = currencyConfigDao.findAllByChannelIdOrderBySortNoAsc(member.getChannelId());
        Long memberId = member.getMemberId();
        currencyList.stream().forEach(d -> {
            MemberMoney money = new MemberMoney();
            money.setMemberId(memberId);
            money.setCurrency(d.getCurrency());
            money.setAvailableAmount(BigDecimal.ZERO);
            money.setUnavailableAmount(BigDecimal.ZERO);
            money.setWithdrawingAmount(BigDecimal.ZERO);
            money.setWithdrawedAmount(BigDecimal.ZERO);
            money.setCreateTime(LocalDateTime.now());
            money.setCreatedBy("sys_register");
            memberMoneyDao.save(money);
        });
        return member;
    }

    @Override
    public Member resetPassword(Long memberId, String password) {
        Member member = selectByPId(memberId);
        Assert.notNull(member,"会员不存在");
        if (StringUtils.isBlank(password)) {
            throw BusinessException.build("密码不能为空");
        }

        member.setSalt(Global.getSalt());
        member.setPassword(passwordService.encryptPassword(password, Global.getSalt()));
        member.setPasswordUpdateTime(new Date());

        member = memberDao.saveAndFlush(member);
        return member;
    }

}
