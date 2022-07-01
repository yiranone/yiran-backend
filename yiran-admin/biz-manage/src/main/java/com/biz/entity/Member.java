package com.biz.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.dashboard.common.annotation.Excel;
import one.yiran.db.common.annotation.Search;
import one.yiran.db.common.domain.TimedBasedEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = false)
@Table(name = "ext_member",indexes = {
        @Index(name = "idx_pho_chid", columnList = "phone,channelId", unique = true),
        @Index(name = "idx_phone", columnList = "phone", unique = false)
})
@Entity
@Data
public class Member extends TimedBasedEntity {

    /**
     * 参数主键
     */
    @Excel(name = "参数主键", cellType = Excel.ColumnType.NUMERIC)
    @Search
    @Id
    private Long userId;

    @Search
    @NotBlank(message = "手机号不能为空")
    @Column(length = 13,nullable = false)
    private String phone;

    /** 参数键名 */
    @Excel(name = "参数键名")
    @Search
    @NotBlank(message = "系统来源长度不能为空")
    @Size(min = 0, max = 100, message = "系统来源长度不能超过100个字符")
    @Column(length = 32,nullable = false)
    private Long channelId;

}
