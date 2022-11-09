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
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "currency_config",
        indexes = {@Index(name = "idx_cc_curr", columnList = "channel_id,currency", unique = true)})
public class CurrencyConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "channel_id")
    private Long channelId;
    @Column(name = "icon")
    private String icon;
    @Column(name = "currency", length = 10)
    private String currency;
    @Column(name = "unit", length = 20)
    private String unit;//合约
    @Column(name = "min", length = 10)
    private String min;
    @Column(name = "max", length = 10)
    private String max;
    @Column(name = "fee_type", precision = 2, scale = 0,nullable = false)
    private Integer feeType;//提现手续费 1-百分比 2-单笔
    @Column(name = "fee_value", precision = 12, scale = 4,nullable = false)
    private BigDecimal feeValue;//提现手续费
    @Column(name = "audit_desc")
    private String auditDesc;
    @Column(name = "exchange_rate")
    private BigDecimal exchangeRate;//兑换比率：换算RMB
    @Column(name = "IS_SHOW", columnDefinition = "int(11) DEFAULT 1")
    private Integer isShow;//是否展示
    @Column(name = "IS_CHARGE", columnDefinition = "int(11) DEFAULT 1")
    private Integer isCharge;//是否充值
    @Column(name = "IS_WITHDRAW", columnDefinition = "int(11) DEFAULT 1")
    private Integer isWithdraw;//是否提现
    @Column(name = "IS_TRANSFER", columnDefinition = "int(11) DEFAULT 1")
    private Integer isTransfer;//是否转账
    @Column(name = "IS_DIG", columnDefinition = "int(11) DEFAULT 0")
    private Integer isDig;//是否云存储
    @Column(name = "SORT_NO")
    private Integer sortNo;
    @Column(name = "STATUS_URL")
    private String statusUrl;//全网状态
    @Column(name = "IS_DELETE", columnDefinition = "int(11) DEFAULT 0")
    private Integer isDelete;

    @Column(name = "transfer_rate", precision = 12, scale = 4)
    private BigDecimal transferRate;//转账手续费
}
