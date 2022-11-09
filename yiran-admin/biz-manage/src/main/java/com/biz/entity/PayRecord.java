package com.biz.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "PAY_RECORD", uniqueConstraints =
        {@UniqueConstraint(columnNames = {"memberId", "internalId"})})
public class PayRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long channelId;

    @Column(nullable = false)
    private Long memberId;
    //用户交易金额 txnAmount=payAmount 或者 txnAmount=-payAmount
    @Column(precision = 19, scale = 8,nullable = false)
    private BigDecimal txnAmount;

    @Column(precision = 19, scale = 8)
    private BigDecimal totalAmount;

    @Column(length = 32)
    private String internalId;
    @Column(length = 32)
    private String externalId;
    //明细类型，1通证 2积分两种 AccountTypeEnum
    @NotNull
    @Column(name = "ACCOUNT_TYPE", length = 20,nullable = false)
    private Integer accountType;
    @Column(name = "PAY_METHOD", length = 20,nullable = false)
    private String payMethod;
    @Column(name = "TYPE", length = 20)
    private String type;
    @Column(name = "TYPE_VALUE")
    private Integer typeValue;
    //SUCCESS.name()
    @Column(name = "SYS_STATE")
    private Integer sysState; //  0: 需要API JOB处理; 1: 已处理
    @Column(name = "COMMENT", length = 200)
    private String comment;

    @Column(name="CREATE_TIME")
    private LocalDateTime createTime;
    @Column(name="CREATE_BY",length = 32)
    private String createdBy;

    @Column(name = "trade_time")
    private LocalDateTime tradeTime;
    @Column(name = "currency",length = 10)
    private String currency;
    @Column(name = "execute_time")
    private LocalDateTime executeTime; //异步计算时间

    public void setCreateTime(LocalDateTime createTime){
        this.createTime = createTime;
        this.executeTime = createTime;
    }
}
