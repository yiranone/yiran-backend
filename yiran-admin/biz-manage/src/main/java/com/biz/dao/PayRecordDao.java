package com.biz.dao;

import com.biz.entity.PayRecord;
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
import java.util.Set;

@Repository
public interface PayRecordDao extends JpaRepository<PayRecord, Long>, JpaSpecificationExecutor<PayRecord> {

    @Query(value = "select distinct(pr.id) from PayRecord pr " +
            "where pr.createTime > :startTime and pr.createTime <= :endTime " +
            "and pr.sysState = 0 " +
            "and pr.accountType = 1 " +
            "and pr.memberId is not null")
    Set<Long> findMoneyReturnToProcess(@Param("startTime") LocalDateTime start,
                                       @Param("endTime")  LocalDateTime endTime);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update PayRecord t set t.sysState = 1,t.totalAmount=:totalAmount,t.executeTime=:executeTime " +
            "where t.id = :id and t.memberId = :memberId and t.sysState = 0")
    int updateStateDone(@Param("id") Long id , @Param("memberId") Long memberId,
                        @Param("totalAmount") BigDecimal totalAmount, @Param("executeTime") LocalDateTime executeTime);

    @Query(value = "SELECT count(*) FROM ( SELECT t.user_id FROM pay_record t WHERE t.type = ?1 GROUP BY t.user_id ) s", nativeQuery = true)
    BigDecimal countUser(String type);

    @Query(value = "SELECT count(*) FROM ( SELECT t.user_id FROM pay_record t, user u WHERE u.id = t.user_id and u.channel_code = ?1 and t.type = ?2 GROUP BY t.user_id ) s", nativeQuery = true)
    BigDecimal countUser(String channelCode, String type);

    PayRecord findByMemberIdAndInternalIdAndTypeValue(Long memberId, String internalId, int type);

    @Query(value = "SELECT IFNULL(SUM(pay_amount), 0) FROM pay_record WHERE sys_state = 1 AND user_id = ?1 AND type_value = ?2 AND currency = ?3", nativeQuery = true)
    BigDecimal sumPayAmount(Long memberId, Integer type, String currency);

    @Query(value = "SELECT IFNULL(SUM(txn_amount), 0) FROM pay_record WHERE sys_state = 1 AND user_id = ?1 AND type_value in ?2 AND currency = ?3", nativeQuery = true)
    BigDecimal sumPayAmount(Long memberId, List<Integer> types, String currency);

    @Query(value = "select IFNULL(SUM(txn_amount), 0) FROM pay_record WHERE sys_state = 1 AND user_id = ?1 AND type_value = ?2  AND currency = ?3" +
            " AND DATE_FORMAT(create_time, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 1 DAY)", nativeQuery = true)
    BigDecimal sumYesterdayPayAmount(Long memberId, Integer type, String currency);

    @Query(value = "select IFNULL(SUM(txn_amount), 0) FROM pay_record WHERE sys_state = 1 AND user_id = ?1 AND type_value in ?2  AND currency = ?3" +
            " AND DATE_FORMAT(create_time, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 1 DAY)", nativeQuery = true)
    BigDecimal sumYesterdayPayAmount(Long memberId, List<Integer> type, String currency);

    @Query(value = "select IFNULL(SUM(pay_amount), 0) as amount," +
            "IFNULL(SUM(IF(DATE_FORMAT(create_time, '%Y-%m-%d') = DATE_FORMAT(CURDATE(), '%Y-%m-%d'), pay_amount, 0)), 0) AS today," +
            "IFNULL(SUM(IF(DATE_FORMAT(create_time, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 1 DAY), pay_amount, 0)), 0) AS yesterday" +
            " FROM pay_record a, user u WHERE a.user_id = u.id and type_value = ?1 " +
            "  AND sys_state = 1 AND account_type = 1 AND currency = ?2 and u.user_type = 1", nativeQuery = true)
    Map<String, Object> countRealPayAmountByTypeValueAndCurrency(Integer type, String currency);

    @Query(value = "select IFNULL(SUM(pay_amount), 0) as amount," +
            "IFNULL(SUM(IF(DATE_FORMAT(create_time, '%Y-%m-%d') = DATE_FORMAT(CURDATE(), '%Y-%m-%d'), pay_amount, 0)), 0) AS today," +
            "IFNULL(SUM(IF(DATE_FORMAT(create_time, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 1 DAY), pay_amount, 0)), 0) AS yesterday" +
            " FROM pay_record a, user u WHERE a.user_id = u.id AND u.channel_code = ?1 AND type_value = ?2" +
            "  AND sys_state = 1 AND account_type = 1 AND currency = ?3 and u.user_type = 1", nativeQuery = true)
    Map<String, Object> countRealPayAmountByTypeValueAndCurrency(String channelCode, Integer type, String currency);

    @Query(value = "select IFNULL(SUM(txn_amount), 0) as amount," +
            "IFNULL(SUM(IF(DATE_FORMAT(trade_time, '%Y-%m-%d') = DATE_FORMAT(CURDATE(), '%Y-%m-%d'), pay_amount, 0)), 0) AS today," +
            "IFNULL(SUM(IF(DATE_FORMAT(trade_time, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 1 DAY), pay_amount, 0)), 0) AS yesterday" +
            " FROM pay_record a, user u WHERE a.user_id = u.id AND type_value in ?1 " +
            "  AND sys_state = 1 AND account_type = 1 AND currency = ?2 and u.user_type = 1", nativeQuery = true)
    Map<String, Object> countRealPayAmountByTypeValueAndCurrency(List<Integer> types, String currency);

    @Query(value = "select IFNULL(SUM(txn_amount), 0) as amount," +
            "IFNULL(SUM(IF(DATE_FORMAT(trade_time, '%Y-%m-%d') = DATE_FORMAT(CURDATE(), '%Y-%m-%d'), pay_amount, 0)), 0) AS today," +
            "IFNULL(SUM(IF(DATE_FORMAT(trade_time, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 1 DAY), pay_amount, 0)), 0) AS yesterday" +
            " FROM pay_record a, user u WHERE a.user_id = u.id AND u.channel_code = ?1 AND type_value in ?2" +
            "  AND sys_state = 1 AND account_type = 1 AND currency = ?3 and u.user_type = 1", nativeQuery = true)
    Map<String, Object> countRealPayAmountByTypeValueAndCurrency(String channelCode, List<Integer> types, String currency);

    @Query(value = "select IFNULL(SUM(txn_amount), 0) as amount," +
            "IFNULL(SUM(IF(DATE_FORMAT(create_time, '%Y-%m-%d') = DATE_FORMAT(CURDATE(), '%Y-%m-%d'), txn_amount, 0)), 0) AS today," +
            "IFNULL(SUM(IF(DATE_FORMAT(create_time, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 1 DAY), txn_amount, 0)), 0) AS yesterday" +
            " FROM pay_record a, user u WHERE a.user_id = u.id AND type_value in ?1 " +
            "  AND sys_state = 1 AND account_type = 1 and pay_method = ?2 AND currency = ?3 and u.user_type = 1", nativeQuery = true)
    Map<String, Object> countRealPayAmountByTypeValueAndPayMethodAndCurrency(List<Integer> types, String payMethod, String currency);

    @Query(value = "select IFNULL(SUM(txn_amount), 0) as amount," +
            "IFNULL(SUM(IF(DATE_FORMAT(create_time, '%Y-%m-%d') = DATE_FORMAT(CURDATE(), '%Y-%m-%d'), txn_amount, 0)), 0) AS today," +
            "IFNULL(SUM(IF(DATE_FORMAT(create_time, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 1 DAY), txn_amount, 0)), 0) AS yesterday" +
            " FROM pay_record a, user u WHERE a.user_id = u.id and u.channel_code = ?1 and type_value in ?2" +
            "  AND sys_state = 1 AND account_type = 1 and pay_method = ?3 AND currency = ?4 and u.user_type= 1", nativeQuery = true)
    Map<String, Object> countRealPayAmountByTypeValueAndPayMethodAndCurrency(String channelCode, List<Integer> types, String payMethod, String currency);

    //用户的当前余额
    @Query(value = "select coalesce(SUM(pr.txnAmount),0) FROM PayRecord pr" +
            " WHERE pr.sysState = 1 AND pr.memberId = :memberId AND pr.currency = :currency")
    BigDecimal sumTxnAmountByMemberIdAndCurrency(@Param("memberId") Long memberId, @Param("currency") String currency);
}
