package com.bid.bidmanage.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.db.common.annotation.Search;
import one.yiran.db.common.domain.TimedBasedEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_src_sys_address", indexes = {
        @Index(name = "idx_srcSys", columnList = "srcSys", unique = false)
})
@Entity
@Data
public class SrcSysAddress extends TimedBasedEntity {

    @Id
    private Long id;

    @Search
    @NotBlank(message = "系统来源不能为空")
    @Column(length = 64)
    private String srcSys;

    @Search
    @NotBlank(message = "链")
    @Column(length = 64)
    private String chain;

    @Search
    @NotBlank(message = "币种不能为空")
    @Column
    private String currency;

    @NotBlank(message = "币种地址不能为空")
    @Column(length = 128)
    private String address;

    @Transient
    @Column(length = 64)
    private BigDecimal withdrawRate;

    @Transient
    private Boolean isPayPwd;

}
