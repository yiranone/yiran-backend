package com.biz.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现申请/充值
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "MONEY_APPLICATION",
        indexes = {@Index(name = "IDX_uniqueHash",columnList = "uniqueHash",unique = true)})
public class MoneyApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long channelId;

    @Column(name = "TXN_ID",length = 32)
    private String txnId;

    @Column(name = "TXN_HASH",length = 256)
    private String txnHash;

    @Column(nullable = false)
    private Long memberId;

    @Column(length = 32)
    private String memberName;

    @Column(name = "USER_TYPE")
    private Integer userType; //用户类型 0-虚拟 1-真实

    /**
     * 提现金额
     */
    @Column(name = "AMOUNT", precision = 19, scale = 8)
    private BigDecimal amount;

    /**
     * 手续费
     */
    @Column(name = "FEE", precision = 19, scale = 8)
    private BigDecimal fee;
    /**
     * 到账金额
     */
    @Column(name = "SETT_FEE", precision = 19, scale = 8)
    private BigDecimal settFee;

    /**
     * 用户手机号
     */
    @Column(name = "PHONE",length = 15)
    private String phone;
    /**
     * 账户
     */
    @Column(name = "ACCOUNT", length = 50)
    private String account;
    /**
     * 提现账户类型
     */
    @Column(name = "ACCOUNT_TYPE", length = 30)
    private String accountType;

    @Column(length = 12,nullable = false)
    private String currency;

    /**
     * 账户人的名字
     */
    @Column(name = "ACCOUNT_NAME", length = 30)
    private String accountName;
    /**
     * 银行名称
     */
    @Column(name = "BANK_NAME", length = 30)
    private String bankName;
    /**
     * 状态 0申请 1批准 2拒绝 3失败 4提现/充值处理中 5.提现/充值成功
     */
    @NotNull
    @Column(name = "STATE")
    private Integer state;
    @Column(name = "comment", length = 200)
    private String comment;
    @Version
    private Integer version;
    @Column(name = "ORDER_SN", length = 50)
    private String orderNo;

    @Column(name = "OPERATOR_ID")
    private Long operatorId;

    @Column(name = "OPERATOR",length = 32)
    private String operator;
    @Column(name = "APPROVE_TIME")
    private LocalDateTime approveTime;
    @Column(name = "FINISH_TIME")
    private LocalDateTime finishTime;

    @Column(name = "CREATE_TIME")
    private LocalDateTime createTime;
    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;

    @Column(length = 65)
    private String fromAddress;

    @Column(length = 65)
    private String toAddress;

    @Column(length = 32)
    private String uniqueHash; //全局唯一

}
