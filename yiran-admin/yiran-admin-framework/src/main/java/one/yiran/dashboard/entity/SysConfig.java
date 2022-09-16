package one.yiran.dashboard.entity;

import one.yiran.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.dashboard.common.annotation.Excel;
import one.yiran.db.common.annotation.Search;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_config")
@Entity
@Data
public class SysConfig extends TimedBasedEntity {

    /**
     * 参数主键
     */
    @Excel(name = "参数主键", cellType = Excel.ColumnType.NUMERIC)
    @Search
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long configId;

    /**
     * 参数名称
     */
    @Excel(name = "参数名称")
    @Search
    @NotBlank(message = "参数名称不能为空")
    @Size(min = 0, max = 100, message = "参数名称不能超过100个字符")
    @Column
    private String configName;

    /** 参数键名 */
    @Excel(name = "参数键名")
    @Search(op = Search.Op.REGEX)
    @NotBlank(message = "参数键名长度不能为空")
    @Size(min = 0, max = 100, message = "参数键名长度不能超过100个字符")
    @Column
    private String configKey;

    /**
     * 参数键值
     */
    @Excel(name = "参数键值")
    @Search
    @NotBlank(message = "参数键值不能为空")
    @Size(min = 0, max = 500, message = "参数键值长度不能超过500个字符")
    @Column
    private String configValue;

    /**
     * 系统内置（Y是 N否）
     */
    @Excel(name = "系统内置", readConverterExp = "Y=是,N=否")
    @Search
    @Size(min = 0, max = 1, message = "类型不能超过1个字符")
    @Column
    private String configType;
}
