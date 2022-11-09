package com.biz.dao;

import com.biz.entity.MemberMoney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface MemberMoneyDao extends JpaRepository<MemberMoney, Long> {

    List<MemberMoney> findAllByMemberId(Long memberId);
    MemberMoney findByMemberIdAndCurrency(Long memberId, String currency);

    MemberMoney findByAddressAndCurrency(String address, String currency);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select t from MemberMoney t where t.memberId =?1 and t.currency = ?2")
    MemberMoney selectMemberForUpdate(Long memberId, String currency);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update MemberMoney t set t.unavailableAmount = t.unavailableAmount - :amount, " +
            "t.withdrawingAmount = t.withdrawingAmount - :amount, t.withdrawedAmount = t.withdrawedAmount + :amount, t.updateTime = :date " +
            "where t.memberId = :memberId and t.unavailableAmount >= :amount and t.currency = :currency")
    int decreaseUnavailableAmountAndCurrency(@Param("memberId") Long memberId, @Param("amount") BigDecimal amount, @Param("date") LocalDateTime date, @Param("currency") String currency);

    @Query(value = "SELECT SUM(IFNULL(t.ava_amount, 0) + IFNULL(t.unava_amount, 0)) as amount FROM user_money t, user u WHERE t.user_id = u.id AND u.user_type = 1 AND t.currency = ?1", nativeQuery = true)
    Map<String, Object> countUserMoneyAmount(String currency);

    @Query(value = "SELECT SUM(IFNULL(t.ava_amount, 0) + IFNULL(t.unava_amount, 0)) as amount FROM user_money t, user u WHERE t.user_id = u.id AND u.user_type = 1 AND u.channel_code = ?1 AND t.currency = ?2", nativeQuery = true)
    Map<String, Object> countUserMoneyAmount(String channelCode, String currency);

    @Modifying(clearAutomatically = true)
    @Query(value = "update MemberMoney t set t.address = ?2 where t.memberId = ?1")
    void updateAddressById(Long id, String address);

    @Query(value = "select t from MemberMoney t where t.currency = ?1 and t.availableAmount > 0")
    List<MemberMoney> findAllNeedWithdrawByCurrency(String currency);
}
