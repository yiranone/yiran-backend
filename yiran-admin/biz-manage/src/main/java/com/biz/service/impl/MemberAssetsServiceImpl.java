package com.biz.service.impl;


import com.biz.constants.AccountTypeEnum;
import com.biz.constants.MoneyTypeEnum;
import com.biz.dao.CurrencyConfigDao;
import com.biz.dao.MemberDao;
import com.biz.dao.MemberMoneyDao;
import com.biz.entity.CurrencyConfig;
import com.biz.entity.Member;
import com.biz.entity.MemberMoney;
import com.biz.entity.MoneyApplication;
import com.biz.entity.PayRecord;
import com.biz.entity.QMoneyApplication;
import com.biz.entity.QPayRecord;
import com.biz.service.MemberAssetsService;
import com.biz.util.FileCoinUtil;
import com.biz.vo.dto.AssetsDetailDTO;
import com.biz.vo.dto.AssetsQueryParamDTO;
import com.biz.vo.dto.ChargeWithdrawQueryParamDTO;
import com.biz.vo.dto.MemberMoneyDTO;
import com.biz.vo.dto.ChargeWithdrawDetailDTO;
import com.google.common.collect.Lists;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import one.yiran.common.domain.PageModel;
import one.yiran.common.exception.BusinessException;
import one.yiran.common.util.DateUtil;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberAssetsServiceImpl extends CrudBaseServiceImpl<Long, Member> implements MemberAssetsService {

    @Autowired
    private MemberDao memberDao;
    @Autowired
    private MemberMoneyDao memberMoneyDao;
    @Autowired
    private CurrencyConfigDao currencyConfigDao;

    @Override
    public List<MemberMoneyDTO> findMoneyListByMemberId(Long memberId) {
        Member member = memberDao.findById(memberId).orElseThrow(() -> BusinessException.build("用户不存在"));
        List<CurrencyConfig> currencyConfigList = currencyConfigDao.findAllByChannelIdOrderBySortNoAsc(member.getChannelId());
        currencyConfigList = currencyConfigList.stream()
                .filter(t -> t.getIsShow().intValue() == 1)
                .filter(t -> t.getIsDelete().intValue() == 0)
                .collect(Collectors.toList());
        List<MemberMoneyDTO> dtoList = currencyConfigList.stream().map(t -> {
            MemberMoney memberMoney = memberMoneyDao.findByMemberIdAndCurrency(memberId, t.getCurrency());
            return MemberMoneyDTO.from(t, memberMoney);
        }).collect(Collectors.toList());
        return dtoList;
    }

    @Override
    public PageModel<AssetsDetailDTO> findAppLists(Long memberId, AssetsQueryParamDTO request, AccountTypeEnum accountType) {
        QueryResults<PayRecord> results = this.getMemberPayRecordPage(memberId, request, accountType, null);
        DateTimeFormatter longSdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<AssetsDetailDTO> collects = results.getResults().stream().map(t -> {
                    Long id = t.getId();
                    LocalDateTime time = t.getExecuteTime() == null ? t.getCreateTime() : t.getExecuteTime();
                    String longTimeDesc = time.format(longSdf);
                    return AssetsDetailDTO.builder().id(id)
                            .typeDesc(t.getType())
                            .dealDesc(FileCoinUtil.formatCoinDecimalWithSign(t.getTxnAmount()))
                            .totalDesc("余额: " + FileCoinUtil.formatCoinDecimal(t.getTotalAmount()))
                            .longTimeDesc(longTimeDesc)
                            .txnAmount(FileCoinUtil.formatCoinDecimal(t.getTxnAmount()))
                            .build();
                }
        ).collect(Collectors.toList());
        return PageModel.instance(results.getTotal(), collects);
    }

    private QueryResults<PayRecord> getMemberPayRecordPage(Long memberId, AssetsQueryParamDTO request, AccountTypeEnum accountType, MoneyTypeEnum moneyType) {
        QPayRecord qPay = QPayRecord.payRecord;
        List<Predicate> predicates = Lists.newArrayList();
        predicates.add(qPay.accountType.eq(accountType.getCode()));
        predicates.add(qPay.sysState.eq(1));
        predicates.add(qPay.memberId.eq(memberId));

        LocalDateTime start = DateUtil.toLocalDateTime(request.getBeginDate());
        if (start != null) {
            predicates.add(qPay.createTime.before(start).not());
        }

        LocalDateTime end = DateUtil.toLocalDateTime(request.getEndDate());
        if (end != null) {
            predicates.add(qPay.createTime.after(end).not());
        }
        if (moneyType != null) {
            predicates.add(qPay.typeValue.eq(moneyType.getType()));
        }

        if (StringUtils.isNotBlank(request.getCurrency())) {
            predicates.add(qPay.currency.eq(request.getCurrency()));
        }

        List<OrderSpecifier> orders = new ArrayList<>();
        if (StringUtils.equals(request.getOrderByColumn(), "id")) {
            OrderSpecifier order = new OrderSpecifier(StringUtils.equals(request.getOrderDirection(), "asc") ? Order.ASC : Order.DESC, qPay.id);
            orders.add(order);
        } else if (StringUtils.equals(request.getOrderByColumn(), "createTime")) {
            OrderSpecifier order = new OrderSpecifier(StringUtils.equals(request.getOrderDirection(), "asc") ? Order.ASC : Order.DESC, qPay.createTime);
            orders.add(order);
        } else {
            OrderSpecifier order = new OrderSpecifier(Order.DESC, qPay.executeTime);
            orders.add(order);
            OrderSpecifier order2 = new OrderSpecifier(Order.DESC, qPay.id);
            orders.add(order2);
        }
        JPAQuery<PayRecord> jpaQuery = queryFactory.selectFrom(qPay)
                .where(predicates.toArray(new Predicate[predicates.size()]))
                .offset((request.getPageNum() - 1) * request.getPageSize())
                .limit(request.getPageSize());
        if (orders != null && orders.size() > 0) {
            jpaQuery = jpaQuery.orderBy(orders.toArray(new OrderSpecifier[]{}));
        }
        return jpaQuery.fetchResults();
    }

    @Transactional
    @Override
    public void setAddress(Long memberId, String address) {
        Assert.notNull(address,"地址不能为空");
        Member member = memberDao.findById(memberId).orElseThrow(() -> BusinessException.build("用户不存在"));
        List<MemberMoney> moneys = memberMoneyDao.findAllByMemberId(memberId);
        moneys.forEach(
                e -> e.setAddress(address)
        );
        memberMoneyDao.saveAll(moneys);
        memberMoneyDao.flush();
    }

    @Override
    public PageModel<ChargeWithdrawDetailDTO> findAppChargeWithdrawLists(Long memberId, ChargeWithdrawQueryParamDTO pageRequest) {
        QueryResults<MoneyApplication> results = getMemberMoneyApplicationPage(memberId, pageRequest,
                pageRequest.getOnlyCharge(), pageRequest.getOnlyWithdraw(), pageRequest.getStatus());
        List<ChargeWithdrawDetailDTO> collects = results.getResults().stream().map(
                t -> ChargeWithdrawDetailDTO.from(t)
        ).collect(Collectors.toList());
        return PageModel.instance(results.getTotal(), collects);
    }

    private QueryResults<MoneyApplication> getMemberMoneyApplicationPage(Long memberId,
                                    ChargeWithdrawQueryParamDTO request,
                                     Boolean onlyCharge,Boolean onlyWithdraw,
                                     String status) {
        QMoneyApplication qPay = QMoneyApplication.moneyApplication;
        List<Predicate> predicates = Lists.newArrayList();
        predicates.add(qPay.memberId.eq(memberId));
//        predicates.add(qPay.state.eq(1));
        if(onlyCharge != null && onlyCharge.booleanValue()) {
            predicates.add(qPay.amount.gt(0));
        }
        if(onlyWithdraw != null && onlyWithdraw.booleanValue()) {
            predicates.add(qPay.amount.lt(0));
        }
        //状态 0申请 1批准 2拒绝 3失败 4提现/充值处理中 5.提现/充值成功
        if(StringUtils.isNotBlank(status) && StringUtils.equals(status,"UNCONFIRMED")) {
            predicates.add(qPay.state.in(0,1,4));
        } else if(StringUtils.isNotBlank(status) && StringUtils.equals(status,"CONFIRMED")) {
            predicates.add(qPay.state.in(5));
        } else if(StringUtils.isNotBlank(status) && StringUtils.equals(status,"FAIL")) {
            predicates.add(qPay.state.in(2,3));
        }

        LocalDateTime start = DateUtil.toLocalDateTime(request.getStartTime());
        if (start != null) {
            predicates.add(qPay.createTime.before(start).not());
        }

        LocalDateTime end = DateUtil.toLocalDateTime(request.getEndTime());
        if (end != null) {
            predicates.add(qPay.createTime.after(end).not());
        }

        List<OrderSpecifier> orders = new ArrayList<>();
        if (StringUtils.equals(request.getOrderByColumn(), "id")) {
            OrderSpecifier order = new OrderSpecifier(StringUtils.equals(request.getOrderDirection(), "asc") ? Order.ASC : Order.DESC, qPay.id);
            orders.add(order);
        } else if (StringUtils.equals(request.getOrderByColumn(), "createTime")) {
            OrderSpecifier order = new OrderSpecifier(StringUtils.equals(request.getOrderDirection(), "asc") ? Order.ASC : Order.DESC, qPay.createTime);
            orders.add(order);
        } else {
            OrderSpecifier order = new OrderSpecifier(Order.DESC, qPay.finishTime);
            orders.add(order);
            OrderSpecifier order2 = new OrderSpecifier(Order.DESC, qPay.id);
            orders.add(order2);
        }
        JPAQuery<MoneyApplication> jpaQuery = queryFactory.selectFrom(qPay)
                .where(predicates.toArray(new Predicate[predicates.size()]))
                .offset((request.getPageNum() - 1) * request.getPageSize())
                .limit(request.getPageSize());
        if (orders != null && orders.size() > 0) {
            jpaQuery = jpaQuery.orderBy(orders.toArray(new OrderSpecifier[]{}));
        }
        return jpaQuery.fetchResults();
    }

}
