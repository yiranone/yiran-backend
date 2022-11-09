package com.biz.service;

import com.biz.entity.Member;
import com.biz.entity.MoneyApplication;
//import com.biz.vo.NotifyRequest;
import one.yiran.db.common.service.CrudBaseService;

import java.math.BigDecimal;
import java.util.List;

public interface MoneyApplicationService extends CrudBaseService<Long, MoneyApplication> {
    void saveOrUpdate(MoneyApplication application);

    MoneyApplication findMoneyApplicationById(Long id);

    void approve(Long id) throws Exception;

    void approve(String gPass, String pass, Long appId, String remark, Long operatorId, String operator) throws Exception;
    void manualPass(Long appId, String remark, Long operatorId, String operator);
    void reject(Long appId, String rejectReason, Long operatorId, String operator);

    MoneyApplication makeEwalletApplication(Member member, String txnId, BigDecimal withdrawAmount, String currency, String account);

    List<Long> findWithdrawNeedApprove();

//    void recharge(Long memberId, String currency, NotifyRequest request);
}
