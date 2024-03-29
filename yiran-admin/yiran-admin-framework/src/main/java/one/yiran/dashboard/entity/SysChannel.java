package one.yiran.dashboard.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.dashboard.common.annotation.Excel;
import one.yiran.dashboard.common.annotation.Option;
import one.yiran.db.common.annotation.Search;
import one.yiran.db.common.domain.TimedBasedEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_channel",indexes = {
        @Index(name = "idx_cha_code",columnList = "channelCode",unique = true)
})
@Entity
@Data
public class SysChannel extends TimedBasedEntity {

    /**
     * 渠道ID
     */
    @Search
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long channelId;

    /**
     * 渠道名称
     */
    @Excel(name = "渠道名称")
    @Search
    @NotBlank(message = "渠道名称不能为空")
    @Column(nullable = false,length = 32)
    private String channelName;

    /**
     * 渠道代码
     */
    @Excel(name = "渠道代码")
    @Search
    @NotBlank(message = "渠道代码不能为空")
    @Column(nullable = false,length = 32)
    private String channelCode;

    /**
     * 渠道域名
     */
    @Excel(name = "渠道域名")
    @Search
    @Column(nullable = true,length = 32)
    private String domainName;

    /**
     * 渠道logot图片
     */
    @Excel(name = "渠道logo")
    @Column(nullable = true,length = 256)
    private String logo;

    //显示在浏览器上面的favicon.ico
    @Excel(name = "渠道icon")
    @Column(nullable = true,length = 256)
    private String icon;

    @Excel(name = "后台显示名称")
    @Column(nullable = true,length = 32)
    private String displayName;


    @Excel(name = "渠道有效期")
    @Column(nullable = false)
    private LocalDate expireDate;

    @Excel(name = "排序")
    @NotNull
    @Column(nullable = false)
    private Integer channelSort;

    /**
     * 系统内置（Y是 N否）
     */
    @Excel(name = "系统内置", readConverterExp = "Y=是,N=否")
    @Search
    @Size(min = 0, max = 1, message = "类型不能超过1个字符")
    @Column
    private String channelType;

    @Option(value = {"1","2"},message = "状态只能是1，2。 1=正常,2=停用")
    @NotBlank(message = "状态不能为空")
    @Search
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    @Column(length = 8,nullable = false)
    private String status;
}
