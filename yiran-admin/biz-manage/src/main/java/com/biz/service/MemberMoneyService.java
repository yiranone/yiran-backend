package com.biz.service;

import com.biz.entity.MemberMoney;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import one.yiran.db.common.service.CrudBaseService;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface MemberMoneyService extends CrudBaseService<Long, MemberMoney> {

    QueryResults<Tuple> list(List<Predicate> predicates, Pageable pageable);

    void updateUserWithdraw(Long userId, String currency, BigDecimal withdrawing, BigDecimal withdrawed );
}
