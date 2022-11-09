package com.biz.controller;

import com.biz.dao.CurrencyConfigDao;
import com.biz.dao.MemberDao;
import com.biz.dao.MemberMoneyDao;
import com.biz.entity.CurrencyConfig;
import com.biz.entity.Member;
import com.biz.entity.MemberMoney;
import com.biz.service.MemberAssetsService;
import com.biz.service.MemberService;
import com.biz.service.MoneyApplicationService;
import com.biz.vo.dto.WithdrawFeeDTO;
import lombok.extern.slf4j.Slf4j;
import one.yiran.common.exception.BusinessException;
import one.yiran.common.util.BigDecimalUtil;
import one.yiran.common.util.MoneyDisplayUtil;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.dashboard.common.annotation.RequireMemberLogin;
import one.yiran.dashboard.common.model.MemberSession;
import one.yiran.dashboard.common.util.UUID;
import one.yiran.dashboard.util.MemberCacheUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

@Controller
@AjaxWrapper
@RequestMapping(value = "/ext/member/money")
@Slf4j
public class MoneyApplicationController {

    @Autowired
    private MemberAssetsService memberAssetsService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberMoneyDao memberMoneyDao;

    @Autowired
    private CurrencyConfigDao currencyConfigDao;

    @Autowired
    private MoneyApplicationService moneyApplicationService;

    @PostMapping("/withdrawFee")
    @RequireMemberLogin
    public WithdrawFeeDTO withdrawFee(@ApiParam BigDecimal withdrawAmount,
                         @ApiParam(required = true) String currency,
                         HttpServletRequest request) {
        MemberSession session = MemberCacheUtil.getSessionInfo(request);
        Long memberId = session.getMemberId();
        Member member = memberService.selectByPId(memberId);
        MemberMoney money = memberMoneyDao.findByMemberIdAndCurrency(memberId, currency);
        if(money == null)
            throw BusinessException.build("币种不存在");
        CurrencyConfig config = currencyConfigDao.findByChannelIdAndCurrency(member.getChannelId(), currency);
        if (config == null) {
            throw BusinessException.build("通证未配置,请联系管理员");
        }
        if(withdrawAmount != null  && withdrawAmount.compareTo(BigDecimal.ZERO) <=0)
            throw BusinessException.build("提款金额要大于0");
//        if(withdrawAmount != null  && withdrawAmount.compareTo(new BigDecimal("0.000001")) <=0)
//            throw BusinessException.build("提款金额要大于0.000001");
        WithdrawFeeDTO withdrawFeeDTO = new WithdrawFeeDTO();
        if(withdrawAmount != null) {
            withdrawFeeDTO.setWithdrawAmount(withdrawAmount.toPlainString());
            BigDecimal fee;
            if (config.getFeeType() == 1) {//百分比
                BigDecimal rate = config.getFeeValue();
                fee = withdrawAmount.multiply(rate);
            } else if (config.getFeeType() == 2) {//单笔
                fee = config.getFeeValue();
            } else {
                throw BusinessException.build("暂不支持的手续费收取方式");
            }
            withdrawFeeDTO.setFeeAmount(fee.toPlainString());
            withdrawFeeDTO.setSettAmount(withdrawAmount.subtract(fee).toPlainString());
        }
        withdrawFeeDTO.setFeeType(config.getFeeType().toString());
        withdrawFeeDTO.setFeeValue(config.getFeeValue().toPlainString());
        withdrawFeeDTO.setCurrency(currency);
        return withdrawFeeDTO;
    }

    @PostMapping("/withdraw")
    @RequireMemberLogin
    public void withdraw(@ApiParam(required = true) BigDecimal withdrawAmount,
                         @ApiParam(required = true) String currency,
                           HttpServletRequest request) {
        MemberSession session = MemberCacheUtil.getSessionInfo(request);
        Long memberId = session.getMemberId();
        Member member = memberService.selectByPId(memberId);
//        if(withdrawAmount.compareTo(BigDecimal.ZERO) < 0) {
//            throw BusinessException.build("提现金额不能小于0");
//        }
        if(withdrawAmount != null  && withdrawAmount.compareTo(new BigDecimal("0.000001")) <=0)
            throw BusinessException.build("提款金额要大于0.000001");
        String txnId = UUID.fastUUID().toString(true);
        MemberMoney money = memberMoneyDao.findByMemberIdAndCurrency(memberId, currency);
        if(money == null)
            throw BusinessException.build("币种不存在");
        if(StringUtils.isBlank(money.getAddress()))
            throw BusinessException.build("提现地址没有设置");

        moneyApplicationService.makeEwalletApplication(member,txnId,withdrawAmount,currency,money.getAddress());
    }

    @PostMapping("/confirm")
    @RequireMemberLogin
    public void confirm(@ApiParam(required = true) Long id,
                         HttpServletRequest request) throws Exception {
        moneyApplicationService.approve(id);
    }


}
