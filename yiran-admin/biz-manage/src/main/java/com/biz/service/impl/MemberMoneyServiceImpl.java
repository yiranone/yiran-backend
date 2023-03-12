package com.biz.service.impl;

import com.biz.dao.MemberMoneyDao;
import com.biz.entity.MemberMoney;
import com.biz.entity.QMember;
import com.biz.entity.QMemberMoney;
import com.biz.service.MemberMoneyService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class MemberMoneyServiceImpl extends CrudBaseServiceImpl<Long, MemberMoney> implements MemberMoneyService {
    @Autowired
    private MemberMoneyDao userMoneyDao;

    @PersistenceContext
    private EntityManager entityManager;

    private JPAQueryFactory queryFactory;

    @PostConstruct
    public void init() {
        queryFactory = new JPAQueryFactory(entityManager);
    }

    public QueryResults<Tuple> list(List<Predicate> predicateList, Pageable pageable) {
        QMember qUser = QMember.member;
        QMemberMoney qUserMoney = QMemberMoney.memberMoney;
        JPAQuery<Tuple> jpaQuery = queryFactory.select(qUserMoney, qUser.name, qUser.nickName, qUser.phone, qUser.channelId)
                .from(qUserMoney)
                .leftJoin(qUser).on(qUser.memberId.eq(qUserMoney.memberId))
                .where(predicateList.toArray(new Predicate[predicateList.size()]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        if (pageable.getSort() != null) {
            PathBuilder<MemberMoney> entityPath = new PathBuilder<>(MemberMoney.class, "memberMoney");
            for (Sort.Order order : pageable.getSort()) {
                PathBuilder<Object> path = entityPath.get(order.getProperty());
                jpaQuery.orderBy(new OrderSpecifier(com.querydsl.core.types.Order.valueOf(order.getDirection().name()), path));
            }
        } else {
            jpaQuery.orderBy(qUserMoney.id.desc());
        }
        return jpaQuery.fetchResults();
    }

    @Transactional
    @Override
    public void updateUserWithdraw(Long memberId, String currency, BigDecimal withdrawing, BigDecimal withdrawed) {
        if (memberId == null) {
            log.info("用户id为空，放弃更新取款金额");
            return;
        }
        if (StringUtils.isBlank(currency)) {
            log.info("currency为空，放弃更新取款金额");
            return;
        }
        MemberMoney um = userMoneyDao.findByMemberIdAndCurrency(memberId, currency);
        if (um == null) {
            log.error("用户余额数据为空，放弃更新取款金额");
            return;
        }
        BigDecimal dbWithdrawing = um.getWithdrawingAmount();
        BigDecimal dbWithdrawed = um.getWithdrawedAmount();
        if (withdrawing == null)
            withdrawing = BigDecimal.ZERO;
        if (withdrawed == null)
            withdrawed = BigDecimal.ZERO;
        if (!withdrawing.equals(dbWithdrawing) || !withdrawed.equals(dbWithdrawed)) {
            log.info("更新用户{}数据库取款中金额{}，已取款金额{}. 累计计算取款中金额{},累计计算已经取款金额{}",
                    memberId, dbWithdrawing, dbWithdrawed, withdrawing, withdrawed);
            QMemberMoney qUserMoney = QMemberMoney.memberMoney;
            queryFactory.update(qUserMoney).set(qUserMoney.withdrawingAmount, withdrawing)
                    .set(qUserMoney.withdrawedAmount, withdrawed)
                    .where(qUserMoney.memberId.eq(memberId).and(qUserMoney.currency.eq(currency))).execute();
        }
    }
}
