package com.biz.service;

import com.biz.constants.AccountTypeEnum;
import com.biz.constants.MoneyTypeEnum;
import com.biz.constants.PayMethodEnum;
import com.biz.dao.CurrencyConfigDao;
import com.biz.dao.MemberDao;
import com.biz.dao.MemberMoneyDao;
import com.biz.dao.MoneyApplicationDao;
import com.biz.dao.PayRecordDao;
import com.biz.entity.MemberMoney;
import com.biz.entity.PayRecord;
import com.biz.job.PayRecordServiceHelper;
import lombok.extern.slf4j.Slf4j;
import one.yiran.common.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class PayRecordService<UserMoneyDao> {
    @Autowired
    private PayRecordDao payRecordDao;
    @Autowired
    private MemberMoneyDao userMoneyDao;
    @Autowired
    private MemberDao userDao;
    @Autowired
    private MoneyApplicationDao moneyApplicationDao;
    @Autowired
    private CurrencyConfigDao currencyConfigDao;


    /**
     * 增加用户通证
     *
     * @param userId
     * @param payRecordId
     */
    @Transactional
    public void updateUserMoney(Long userId, Long payRecordId) {
        PayRecord payRecord = payRecordDao.findById(payRecordId).orElse(null);

        BigDecimal txnAmount = payRecord.getTxnAmount();
        if (txnAmount == null) {
            log.error("payrecore {} txnAmount = null", payRecordId);
            return;
        }
        if (payRecord.getAccountType().intValue() != AccountTypeEnum.MONEY.getCode()) {
            throw new RuntimeException("账户类型错误,不是通证" + payRecordId);
        }
        if (StringUtils.isBlank(payRecord.getCurrency())) {
            throw new RuntimeException("通证不能为空,错误" + payRecordId);
        }

        int maxTry = 1;
        while (maxTry <= 10) {
            MemberMoney userMoney = userMoneyDao.selectMemberForUpdate(userId, payRecord.getCurrency());
            txnAmount = payRecordDao.findById(payRecordId).orElse(null).getTxnAmount();
            LocalDateTime now = LocalDateTime.now();
            BigDecimal afterAmount = userMoney.getAvailableAmount().add(txnAmount);

            log.info("更新用户{}余额之前,增加{},可用{}账户余额{}->{} executeTime:{}", userId, txnAmount, userMoney.getCurrency(),
                    userMoney.getAvailableAmount(), afterAmount, now);

            int ct = payRecordDao.updateStateDone(payRecordId, userId, afterAmount, now);
            if (ct == 1) {
                log.info("更新用户{}余额,增加{},可用{}账户余额变化{}->{}", userId, txnAmount, userMoney.getCurrency(),
                        userMoney.getAvailableAmount(), afterAmount);
                userMoney.setAvailableAmount(afterAmount);
                userMoneyDao.saveAndFlush(userMoney);
                break;
            } else {
                log.info("更新用户{}余额,增加{} 获取锁失败 tryTime:{}", userId, txnAmount, maxTry);
            }
            maxTry++;
        }
    }


    //增加金额
    @Transactional
    public PayRecord makeMoneyForReceiverAsync(String txnId, Long userId, String currency, BigDecimal returnAmount,
                                               PayMethodEnum payMethodEnum, MoneyTypeEnum moneyTypeEnum, String comment) {
        PayRecord record = new PayRecord();
        record.setPayMethod(payMethodEnum.name());
        record.setSysState(0);
        record.setCreateTime(LocalDateTime.now());
        record.setTradeTime(LocalDateTime.now());
        record.setInternalId(txnId);
        record.setTotalAmount(returnAmount);
        record.setAccountType(AccountTypeEnum.MONEY.getCode());
        record.setType(moneyTypeEnum.getDescription());
        record.setTypeValue(moneyTypeEnum.getType());
        record.setComment(comment);
        record.setCurrency(currency);
        payRecordDao.saveAndFlush(record);
        PayRecordServiceHelper.triggerAsync(record.getId());
        return record;
    }

    //减少金额
    @Transactional
    public PayRecord makeMoneyForPayerAsync(String txnId, Long userId, String currency, BigDecimal returnAmount,
                                            PayMethodEnum payMethodEnum, MoneyTypeEnum type, String comment) {
        if(returnAmount.compareTo(BigDecimal.ZERO) > 0) {
            throw BusinessException.build("金额应该小于0");
        }
        PayRecord record = new PayRecord();
        record.setPayMethod(payMethodEnum.name());
        record.setSysState(0);
        record.setCreateTime(LocalDateTime.now());
        record.setTradeTime(LocalDateTime.now());
        record.setInternalId(txnId);
        record.setTxnAmount(returnAmount);
        record.setAccountType(AccountTypeEnum.MONEY.getCode());
        record.setType(type.getDescription());
        record.setTypeValue(type.getType());
        record.setComment(comment);
        record.setCurrency(currency);
        payRecordDao.saveAndFlush(record);
        PayRecordServiceHelper.triggerAsync(record.getId());
        return record;
    }

    //减少金额
    @Transactional
    public PayRecord makeMoneyForPayer(String txnId, Long userId, String currency, BigDecimal returnAmount, BigDecimal totalAmount,
                                       MoneyTypeEnum type, String comment) {
        return this.makeMoneyForPayer(txnId, userId, currency, returnAmount, totalAmount, PayMethodEnum.MONEY, type, comment);
    }

    @Transactional
    public PayRecord makeMoneyForPayer(String txnId, Long userId, String currency, BigDecimal returnAmount, BigDecimal totalAmount,
                                       PayMethodEnum payMethod, MoneyTypeEnum type, String comment) {
        if(returnAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.build("金额应该大于0");
        }
        PayRecord record = new PayRecord();
        record.setSysState(1);
        record.setCreateTime(LocalDateTime.now());
        record.setTradeTime(LocalDateTime.now());
        record.setInternalId(txnId);
        record.setTxnAmount(returnAmount);
        record.setTotalAmount(totalAmount);
        record.setPayMethod(payMethod.name());
        record.setAccountType(AccountTypeEnum.MONEY.getCode());
        //record.setPayState(PayStatusEnum.SUCCESS.name());
        record.setType(type.getDescription());
        record.setTypeValue(type.getType());
        record.setComment(comment);
        record.setCurrency(currency);
        payRecordDao.saveAndFlush(record);
        return record;
    }

    public BigDecimal sumTxnAmountByUserIdAndCurrency(Long userId, String currency) {
        return payRecordDao.sumTxnAmountByMemberIdAndCurrency(userId, currency);
    }
}
