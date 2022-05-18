package one.yiran.dashboard.manage.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.dashboard.common.annotation.Excel;
import one.yiran.db.common.annotation.Search;
import one.yiran.db.common.domain.TimedBasedEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
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


    @Excel(name = "渠道有效期")
    @Column(nullable = false)
    private LocalDate expireDate;


    /**
     * 系统内置（Y是 N否）
     */
    @Excel(name = "系统内置", readConverterExp = "Y=是,N=否")
    @Search
    @Size(min = 0, max = 1, message = "类型不能超过1个字符")
    @Column
    private String channelType;

    /**
     * （0正常 1关闭）
     */
    @Search
    private String status;
}
