package com.biz.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import one.yiran.common.exception.BusinessException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "Member_MONEY",
        uniqueConstraints = {
        @UniqueConstraint(name = "idx_mid_cur", columnNames = {"memberId", "currency"}),
        @UniqueConstraint(name = "idx_add_cur", columnNames = {"address", "currency"})
    })
public class MemberMoney {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long memberId;

    @NotNull
    @Column(length = 12)
    protected String currency;

    @NotNull
    @Column(name = "AVA_AMOUNT", precision = 19, scale = 8)
    private BigDecimal availableAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "UNAVA_AMOUNT", precision = 19, scale = 8)
    private BigDecimal unavailableAmount = BigDecimal.ZERO;

    @NotNull
    @Column(precision = 19, scale = 8)
    private BigDecimal withdrawingAmount = BigDecimal.ZERO;//提现中

    @NotNull
    @Column(precision = 19, scale = 8)
    private BigDecimal withdrawedAmount = BigDecimal.ZERO;//已提现

    @NotNull
    @Column(name = "LOCK_AMOUNT", precision = 19, scale = 8)
    private BigDecimal lockAmount = BigDecimal.ZERO;//锁仓金额

    @NotNull
    @Column(name = "INVITE_LOCK_AMOUNT", precision = 19, scale = 8)
    private BigDecimal inviteLockAmount = BigDecimal.ZERO;//推荐锁仓金额

    @Column(length = 64)
    private String address;//钱包地址

    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;

    @Column(name = "UPDATE_BY",length = 32)
    private String updatedBy;

    @Column(name = "CREATE_TIME")
    protected LocalDateTime createTime;

    @Column(name = "CREATE_BY",length = 32)
    protected String createdBy;


    public void setAvailableAmount(BigDecimal availableAmount) {
        if (availableAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.build("用户余额异常:" + availableAmount);
        }
        this.availableAmount = availableAmount;
    }
}
