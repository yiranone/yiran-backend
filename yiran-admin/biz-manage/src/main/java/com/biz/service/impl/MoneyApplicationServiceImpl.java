package com.biz.service.impl;

import com.biz.constants.AccountTypeEnum;
import com.biz.constants.MoneyApplicationStatusEnum;
import com.biz.constants.MoneyTypeEnum;
import com.biz.constants.PayMethodEnum;
import com.biz.dao.CurrencyConfigDao;
import com.biz.dao.MemberDao;
import com.biz.dao.MoneyApplicationDao;
import com.biz.dao.MemberMoneyDao;
import com.biz.entity.CurrencyConfig;
import com.biz.entity.Member;
import com.biz.entity.MoneyApplication;
import com.biz.entity.PayRecord;
import com.biz.entity.MemberMoney;
import com.biz.entity.QMoneyApplication;
import com.biz.service.MoneyApplicationService;
//import com.biz.vo.NotifyRequest;
//import com.biz.walletapi.PoolApi;
import lombok.extern.slf4j.Slf4j;
import one.yiran.common.exception.BusinessException;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MoneyApplicationServiceImpl extends CrudBaseServiceImpl<Long, MoneyApplication> implements MoneyApplicationService {
    @Autowired
    private MoneyApplicationDao moneyApplicationDao;
    @Autowired
    private CurrencyConfigDao currencyConfigDao;

    @Autowired
    private MemberMoneyDao memberMoneyDao;

    @Autowired
    private MemberDao memberDao;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void saveOrUpdate(MoneyApplication application) {
        moneyApplicationDao.save(application);
    }

    /**
     * 提现审核同意，发起支付平台提现
     */
    @Override
    @Transactional
    public void approve(String gPass, String pass, Long appId, String remark, Long operatorId, String operator) throws Exception {
        MoneyApplication application = moneyApplicationDao.findById(appId).orElse(null);
        if (application == null)
            throw BusinessException.build("未找到提现申请");

        if (application.getState() != null && application.getState() != MoneyApplicationStatusEnum.APPLY.getType()
                && application.getState() != MoneyApplicationStatusEnum.ERROR.getType())
            throw BusinessException.build("提现申请" + appId + "已处理,请勿重复操作");

        int upd = moneyApplicationDao.tryUpdateApprove(appId, LocalDateTime.now());
        if (upd == 1) {
            em.refresh(application);
//            String transactionHash = PoolApi.applyWithdraw(application);//钱包申请提现
            application.setTxnHash(null);
            application.setOperator(operator);
            application.setOperatorId(operatorId);
            application.setApproveTime(LocalDateTime.now());
            em.merge(application);
            em.flush();
        } else {
            throw BusinessException.build("审核异常");
        }
    }

    /**
     * 手动审核转账确认，不发起支付平台提现
     */
    @Override
    @Transactional
    public void manualPass(Long appId, String remark, Long operatorId, String operator) {
        MoneyApplication application = moneyApplicationDao.findById(appId).orElse(null);
        if (application == null)
            BusinessException.build("未找到提现申请");

        if (StringUtils.isBlank(remark))
            BusinessException.build("手动提现审核，请填写备注");

        if (application.getState() != null && application.getState() != MoneyApplicationStatusEnum.APPLY.getType()
                && application.getState() != MoneyApplicationStatusEnum.ERROR.getType())
            throw BusinessException.build("提现申请" + appId + "已处理,请勿重复操作");

        int upd = moneyApplicationDao.tryUpdateSuccess(appId, LocalDateTime.now());
        if (upd == 1) {
            application.setComment("手动审核:" + remark);
            application.setOperator(operator);
            application.setOperatorId(operatorId);
            application.setApproveTime(LocalDateTime.now());
            application.setState(MoneyApplicationStatusEnum.FINISH.getType());
            application.setFinishTime(LocalDateTime.now());
            em.merge(application);
            em.flush();

            MemberMoney userMoney = memberMoneyDao.selectMemberForUpdate(application.getMemberId(), application.getAccountType());
            BigDecimal amount = application.getAmount();

            //减少冻结通证
            int affectCount = memberMoneyDao.decreaseUnavailableAmountAndCurrency(application.getMemberId(), amount, LocalDateTime.now(), application.getAccountType());
            if (affectCount != 1)
                throw BusinessException.build("交易提现异常，冻结通证不足");

            //记录record
            PayRecord moneyWithdraw = new PayRecord();
            moneyWithdraw.setChannelId(application.getChannelId());
            moneyWithdraw.setMemberId(application.getMemberId());
            moneyWithdraw.setAccountType(AccountTypeEnum.MONEY.getCode());
            moneyWithdraw.setType(MoneyTypeEnum.WITHDRAW_MONEY.getDescription());
            moneyWithdraw.setTypeValue(MoneyTypeEnum.WITHDRAW_MONEY.getType());
            moneyWithdraw.setCreateTime(LocalDateTime.now());
            moneyWithdraw.setTradeTime(LocalDateTime.now());
            moneyWithdraw.setPayMethod(PayMethodEnum.MONEY.name());
            moneyWithdraw.setCurrency(application.getAccountType());
            moneyWithdraw.setTxnAmount(BigDecimal.ZERO.subtract(amount));
            moneyWithdraw.setTotalAmount(userMoney.getAvailableAmount());
            moneyWithdraw.setSysState(1);
            moneyWithdraw.setTradeTime(LocalDateTime.now());
            em.persist(moneyWithdraw);
            em.flush();

        } else {
            throw BusinessException.build("审核异常");
        }
    }

    /**
     * 审核不通过
     */
    @Override
    @Transactional
    public void reject(Long appId, String rejectReason, Long operatorId, String operator) {
        MoneyApplication application = moneyApplicationDao.findById(appId).orElse(null);
        if (application == null)
            BusinessException.build("未找到提现申请");

        if (application.getState() == MoneyApplicationStatusEnum.REJECT.getType())
            throw BusinessException.build("提现申请" + appId + "已拒绝,请勿重复操作");

        if (application.getState() != null && application.getState() != MoneyApplicationStatusEnum.APPLY.getType()
                && application.getState() != MoneyApplicationStatusEnum.ERROR.getType())
            throw BusinessException.build("提现申请" + appId + "已处理,请勿重复操作");

        int upd = moneyApplicationDao.tryUpdateReject(appId, LocalDateTime.now());
        if (upd == 1) {
            application.setState(MoneyApplicationStatusEnum.REJECT.getType());
            application.setComment(rejectReason);
            application.setOperator(operator);
            application.setOperatorId(operatorId);
            application.setApproveTime(LocalDateTime.now()); //拒绝时间
            em.merge(application);
            em.flush();
            //钱返回去
            Long memberId = application.getMemberId();
            MemberMoney userMoney = memberMoneyDao.selectMemberForUpdate(memberId, application.getAccountType());
            if (userMoney == null) {
                throw BusinessException.build("用户通证不存在,请联系管理员");
            }
            BigDecimal amount = application.getAmount();
            userMoney.setUnavailableAmount(userMoney.getUnavailableAmount().subtract(amount));
            userMoney.setAvailableAmount(userMoney.getAvailableAmount().add(amount));
            userMoney.setWithdrawingAmount(userMoney.getWithdrawingAmount().subtract(amount));
            memberMoneyDao.saveAndFlush(userMoney);
        }
    }

    @Override
    public MoneyApplication findMoneyApplicationById(Long id) {
        MoneyApplication moneyApplication = moneyApplicationDao.findById(id).orElse(null);
        return moneyApplication;
    }

    @Transactional
    @Override
    public void approve(Long appId) throws Exception {
        MoneyApplication application = moneyApplicationDao.findById(appId).orElse(null);
        if (application == null)
            throw BusinessException.build("未找到提现申请");

        if( application.getAmount().compareTo(BigDecimal.ZERO) >= 0) {
            throw BusinessException.build("提现金额异常");
        }

        if (application.getState() != null && application.getState() != MoneyApplicationStatusEnum.APPLY.getType()
                && application.getState() != MoneyApplicationStatusEnum.ERROR.getType())
            throw BusinessException.build("提现申请" + appId + "已处理,请勿重复操作");

        int upd = moneyApplicationDao.tryUpdateSuccess(appId, LocalDateTime.now());
        if (upd == 1) {
            application = moneyApplicationDao.findById(appId).orElse(null);
            em.refresh(application);

//            String transactionHash = PoolApi.applyWithdraw(application);//钱包申请提现
            String transactionHash = null;
            application.setTxnHash(transactionHash);
            application.setComment("提现已经发送成功");
            application.setOperator("sys");
            application.setOperatorId(0L);
            em.merge(application);
            em.flush();

            MemberMoney userMoney = memberMoneyDao.selectMemberForUpdate(application.getMemberId(), application.getAccountType());
            //减少冻结通证
            int affectCount = memberMoneyDao.decreaseUnavailableAmountAndCurrency(application.getMemberId(), application.getAmount(), LocalDateTime.now(), application.getAccountType());
            if (affectCount != 1)
                throw BusinessException.build("交易提现异常，冻结通证不足");
            //记录record
            PayRecord moneyWithdraw = new PayRecord();
            moneyWithdraw.setChannelId(application.getChannelId());
            moneyWithdraw.setMemberId(application.getMemberId());
            moneyWithdraw.setAccountType(AccountTypeEnum.MONEY.getCode());
            moneyWithdraw.setType(MoneyTypeEnum.WITHDRAW_MONEY.getDescription());
            moneyWithdraw.setTypeValue(MoneyTypeEnum.WITHDRAW_MONEY.getType());
            moneyWithdraw.setCreateTime(LocalDateTime.now());
            moneyWithdraw.setTradeTime(LocalDateTime.now());
            moneyWithdraw.setPayMethod(PayMethodEnum.MONEY.name());
            moneyWithdraw.setCurrency(application.getAccountType());
            moneyWithdraw.setTxnAmount(application.getAmount());
            moneyWithdraw.setTotalAmount(userMoney.getAvailableAmount());
            moneyWithdraw.setSysState(1);
            moneyWithdraw.setTradeTime(LocalDateTime.now());
            moneyWithdraw.setCreatedBy("sys");
            moneyWithdraw.setComment("提现自动审核");
            em.persist(moneyWithdraw);
            em.flush();
        } else {
            throw BusinessException.build("提现自动审核审核异常");
        }
    }

    @Override
    @Transactional
    public MoneyApplication makeEwalletApplication(Member member, String txnId, BigDecimal chargeAmount, String currency, String account) {
        Assert.notNull(currency, "提现账户类型不能为空");
        Assert.isTrue(chargeAmount.compareTo(BigDecimal.ZERO) > 0 , "提现金额异常");

        CurrencyConfig config = currencyConfigDao.findByChannelIdAndCurrency(member.getChannelId(), currency);
        if (config == null) {
            throw BusinessException.build("通证未配置,请联系管理员");
        }
        MemberMoney memberMoney = memberMoneyDao.selectMemberForUpdate(member.getMemberId(), currency);
        if (memberMoney == null) {
            throw BusinessException.build("用户通证账户不存在,请联系管理员");
        }
        if (memberMoney.getAvailableAmount().compareTo(chargeAmount) < 0) {
            throw BusinessException.build("可用余额不足");
        }
        if (!account.equals(memberMoney.getAddress())) {
            throw BusinessException.build("仅支持绑定的提现地址提现");
        }


        MoneyApplication application = new MoneyApplication();
        application.setChannelId(member.getChannelId());
        application.setState(0);
        application.setTxnId(txnId);
        application.setToAddress(account);
        application.setAccountType(currency);
        application.setCurrency(currency);
        application.setBankName("");
        application.setAccount(account);
        application.setAccountName("链账户");
        application.setAmount(BigDecimal.ZERO.subtract(chargeAmount));//提现币金额
        application.setMemberId(member.getMemberId());
        application.setMemberName(member.getName());
        application.setPhone(member.getPhone());
        application.setCreateTime(LocalDateTime.now());

        BigDecimal fee;
        if (config.getFeeType() == 1) {//百分比
            BigDecimal rate = config.getFeeValue();
            fee = application.getAmount().multiply(rate);
        } else if (config.getFeeType() == 2) {//单笔
            fee = config.getFeeValue();
        } else {
            throw BusinessException.build("暂不支持的手续费收取方式");
        }
        BigDecimal settFee = application.getAmount().subtract(fee);
        application.setFee(fee);
        application.setSettFee(settFee);
        em.persist(application);
        em.flush();


        memberMoney.setUnavailableAmount(memberMoney.getUnavailableAmount().add(chargeAmount));
        memberMoney.setAvailableAmount(memberMoney.getAvailableAmount().subtract(chargeAmount));
        memberMoney.setWithdrawingAmount(memberMoney.getWithdrawingAmount().add(chargeAmount));
        memberMoneyDao.saveAndFlush(memberMoney);
        return application;
    }

    @Override
    public List<Long> findWithdrawNeedApprove() {
        QMoneyApplication qMoneyApplication = QMoneyApplication.moneyApplication;
        return selectList(qMoneyApplication.state.eq(0).and(qMoneyApplication.amount.lt(0)))
                .stream().map(MoneyApplication::getId).collect(Collectors.toList());
    }

//    @Transactional
//    @Override
//    public void recharge(Long memberId, String currency, NotifyRequest notifyRequest) {
//        Member member = memberDao.getOne(memberId);
//        String amount = notifyRequest.getValue();
//        if(new BigDecimal(amount).compareTo(BigDecimal.ZERO) <= 0) {
//            log.info("充值金额小于0，不处理");
//            return;
//        }
//        String from = notifyRequest.getFrom();
//        String to = notifyRequest.getTo();
//        String chain = notifyRequest.getChain();
//        String hash = notifyRequest.getTransactionId();
//        String uniqueHash = MD5Util.hash(to + "_" + hash);
//        String status = notifyRequest.getResult();
//        if(StringUtils.equalsIgnoreCase(status, NotifyRequest.RESULT_UNCONFIRMED)) {
//            //显示充值中
//            MoneyApplication application = moneyApplicationDao.findOneByUniqueHash(uniqueHash);
//            if(application != null) {
//                if(application.getAmount().compareTo(BigDecimal.ZERO) < 0) {
//                    //提现的
//                }
//                log.info("已经处理过了，忽略");
//                return;
//            }
//            application = new MoneyApplication();
//            application.setChannelId(member.getChannelId());
//            application.setFromAddress(from);
//            application.setToAddress(to);
//            application.setCurrency(currency);
//            application.setState(4); //充值处理中
//            application.setTxnId(notifyRequest.getNotifyId() + "");
//            application.setTxnHash(notifyRequest.getTransactionId());
//            application.setUniqueHash(uniqueHash);
//            application.setAmount(new BigDecimal(amount));//充值金额
//            application.setBankName(chain);
//            application.setAccount(from); //充值地址
//            application.setAccountType(currency);
//            application.setAccountName(member.getName());
//            application.setMemberId(member.getMemberId());
//            application.setMemberName(member.getName());
//            application.setPhone(member.getPhone());
//            application.setCreateTime(LocalDateTime.now());
//            application.setOperator("sys");
//            moneyApplicationDao.saveAndFlush(application);
//        } else if(StringUtils.equalsIgnoreCase(status, NotifyRequest.RESULT_CONFIRMED)){
//            //充值成功
//            MoneyApplication application = moneyApplicationDao.findByMemberIdAndUniqueHash(memberId,uniqueHash);
//            if(application == null) {
//                //新增一个确认
//            } else {
//                if (application.getAmount().compareTo(BigDecimal.ZERO) < 0) {
//                    //提现的
//                    log.info("收到提现确认成功 {}",application.getTxnHash());
//                    int upd = moneyApplicationDao.tryUpdateSuccess(application.getId(), LocalDateTime.now());
//                    if (upd == 1) {
//                        em.refresh(application);
//                        application.setComment("提现确认成功:");
//                        application.setOperator("sys");
//                        application.setFinishTime(LocalDateTime.now());
//                        em.merge(application);
//                    }
//                } else {
//                    //充值确认成功
//                    int upd = moneyApplicationDao.tryUpdateSuccess(application.getId(), LocalDateTime.now());
//                    if (upd == 1) {
//                        em.refresh(application);
//                        application.setComment("充值确认成功:");
//                        application.setOperator("sys");
//                        application.setFinishTime(LocalDateTime.now());
//                        em.merge(application);
//
//                        BigDecimal appAmount = application.getAmount();
//
//                        //记录record
//                        PayRecord moneyRecharge = new PayRecord();
//                        moneyRecharge.setChannelId(member.getChannelId());
//                        moneyRecharge.setMemberId(memberId);
//                        moneyRecharge.setAccountType(AccountTypeEnum.MONEY.getCode());
//                        moneyRecharge.setType(MoneyTypeEnum.DEPOSIT_MONEY.getDescription());
//                        moneyRecharge.setTypeValue(MoneyTypeEnum.DEPOSIT_MONEY.getType());
//                        moneyRecharge.setPayMethod(PayMethodEnum.MONEY.name());
//                        moneyRecharge.setCurrency(application.getAccountType());
//                        moneyRecharge.setTxnAmount(appAmount);
//                        moneyRecharge.setSysState(0);
//                        moneyRecharge.setComment("自动充值");
//                        moneyRecharge.setCreatedBy("sys");
//                        moneyRecharge.setCreateTime(LocalDateTime.now());
//                        moneyRecharge.setTradeTime(LocalDateTime.now());
//                        em.persist(moneyRecharge);
//                        em.flush();
//                        PayRecordServiceHelper.triggerAsync(moneyRecharge.getId());
//                    } else {
//                        log.info("已经充值成功了，放弃通知");
//                        return;
//                    }
//                }
//            }
//
//        }
//    }

}
