package com.biz.vo.dto;

import com.biz.entity.CurrencyConfig;
import com.biz.entity.MemberMoney;
import com.biz.util.FileCoinUtil;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MemberMoneyDTO {
    private String currency;
    private String unit;//合约
    private String icon;
    private String amount;//可用余额
    private String availableAmount;//可提现金额
    private String lockAmount;//锁仓余额
    private String inviteLockAmount;//锁仓余额
    private String rmbAmount;//可用余额
    private String withdrawingAmount;//提现中
    private String withdrawedAmount;//已提现
    private Integer isCharge;//是否充值
    private Integer isWithdraw;//是否提现
    private Integer isTransfer;//是否转账
    private String address;

    public static MemberMoneyDTO from(CurrencyConfig t, MemberMoney userMoney) {
        BigDecimal amount = BigDecimal.ZERO;
        if (t.getExchangeRate() == null) {
            t.setExchangeRate(BigDecimal.ONE);
        }
        amount = amount.add(userMoney.getLockAmount()).add(userMoney.getInviteLockAmount())
                .add(userMoney.getWithdrawedAmount()).add(userMoney.getWithdrawingAmount())
                .add(userMoney.getAvailableAmount());
        MemberMoneyDTO dto = MemberMoneyDTO.builder()
                .currency(t.getCurrency())
                .unit(t.getUnit())
                .icon(t.getIcon())
                .amount(FileCoinUtil.formatCoinDecimal2(amount))
                .availableAmount(FileCoinUtil.formatCoinDecimal(userMoney.getAvailableAmount()))
                .lockAmount(FileCoinUtil.formatCoinDecimal(userMoney.getLockAmount()))
                .rmbAmount(FileCoinUtil.formatCNYDecimal(amount.multiply(t.getExchangeRate())))
                .withdrawingAmount(FileCoinUtil.formatCoinDecimal(userMoney.getWithdrawingAmount()))
                .withdrawedAmount(FileCoinUtil.formatCoinDecimal(userMoney.getWithdrawedAmount()))
                .isCharge(t.getIsCharge())
                .isWithdraw(t.getIsWithdraw())
                .isTransfer(t.getIsTransfer())
                .address(userMoney.getAddress())
                .build();
        return dto;
    }
}
