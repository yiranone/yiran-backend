package com.bid.bidmanage.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.dashboard.common.annotation.Excel;
import one.yiran.db.common.annotation.Search;
import one.yiran.db.common.domain.TimedBasedEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_user_src_sys",indexes = {
        @Index(name = "idx_srcSys", columnList = "srcSys", unique = false)
})
@Entity
@Data
public class UserSrcSys extends TimedBasedEntity {

    /**
     * 参数主键
     */
    @Excel(name = "参数主键", cellType = Excel.ColumnType.NUMERIC)
    @Search
    @Id
    private Long userId;

    /** 参数键名 */
    @Excel(name = "参数键名")
    @Search
    @NotBlank(message = "系统来源长度不能为空")
    @Size(min = 0, max = 100, message = "系统来源长度不能超过100个字符")
    @Column
    private String srcSys;

}
