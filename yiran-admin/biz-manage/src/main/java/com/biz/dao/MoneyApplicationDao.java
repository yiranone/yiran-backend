package com.biz.dao;

import com.biz.entity.MoneyApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface MoneyApplicationDao extends JpaRepository<MoneyApplication, Long>, JpaSpecificationExecutor<MoneyApplication> {

    List<MoneyApplication> findAllByState(Integer state);

    MoneyApplication findOneByUniqueHash(String uniqueHash);

    @Modifying(clearAutomatically = true,flushAutomatically = true)
    @Query(value = "update MoneyApplication t set t.state = 4, t.updateTime = :date " +
            "where t.id = :id and (t.state = 0 or t.state = 3)")
    int tryUpdateApprove(@Param("id") Long id, @Param("date") LocalDateTime date);

    @Modifying(flushAutomatically = true)
    @Query(value = "update MoneyApplication t set t.state = 5, t.updateTime = :date, t.approveTime = :date " +
            "where t.id = :id and (t.state = 0 or t.state = 3 or t.state = 4)")
    int tryUpdateSuccess(@Param("id") Long id, @Param("date") LocalDateTime date);

    @Modifying(clearAutomatically = true,flushAutomatically = true)
    @Query(value = "update MoneyApplication t set t.state = 2, t.updateTime = :date " +
            "where t.id = :id and (t.state = 0 or t.state = 3)")
    int tryUpdateReject(@Param("id") Long id, @Param("date") LocalDateTime date);

    @Modifying(clearAutomatically = true,flushAutomatically = true)
    @Query(value = "update MoneyApplication t set t.state = 3, t.updateTime = :date " +
            "where t.id = :id and (t.state = 0 or t.state = 4)")
    int tryUpdateFail(@Param("id") Long id, @Param("date") LocalDateTime date);

    //状态 0申请 1批准 2拒绝 3失败 4提现/充值处理中 5.提现/充值成功
    @Query(value = "select IFNULL(SUM(amount),0) from money_application where state in (0, 1, 3, 4) and member_id = ?1 and account_type = ?2 and amount < 0", nativeQuery = true)
    BigDecimal sumWithdrawingAmount(Long userId, String accountType);

    @Query(value = "select IFNULL(SUM(amount),0) from money_application where state in (5) and member_id = ?1 and account_type = ?2 and amount < 0", nativeQuery = true)
    BigDecimal sumWithdrawedAmount(Long userId, String accountType);

    MoneyApplication findByMemberIdAndTxnId(Long userId, String txnId);
    MoneyApplication findByMemberIdAndUniqueHash(Long userId, String uniqueHash);


    @Query(value = "select SUM(fee) as amount," +
            "SUM(IF(DATE_FORMAT(finish_time, '%Y-%m-%d') = DATE_FORMAT(CURDATE(), '%Y-%m-%d'), fee, 0)) AS today," +
            "SUM(IF(DATE_FORMAT(finish_time, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 1 DAY), fee, 0)) AS yesterday" +
            " FROM money_application a WHERE state = 5 and account_type = ?1 AND EXISTS(SELECT id FROM user WHERE id = a.user_id AND user_type = 1)", nativeQuery = true)
    Map<String, Object> countWithdrawFeeAmountByCurrency(String currency);

    @Query(value = "select SUM(fee) as amount," +
            "SUM(IF(DATE_FORMAT(finish_time, '%Y-%m-%d') = DATE_FORMAT(CURDATE(), '%Y-%m-%d'), fee, 0)) AS today," +
            "SUM(IF(DATE_FORMAT(finish_time, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 1 DAY), fee, 0)) AS yesterday" +
            " FROM money_application a, user u WHERE a.user_id = u.id AND u.channel_id = ?1 AND state = 5" +
            "  AND account_type = ?2 AND u.user_type = 1", nativeQuery = true)
    Map<String, Object> countWithdrawFeeAmountByCurrency(Long channelId, String currency);
}
