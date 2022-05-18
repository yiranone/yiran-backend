package one.yiran.dashboard.manage.entity;

import one.yiran.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.dashboard.common.annotation.Excel;
import one.yiran.db.common.annotation.Search;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_dict_type",indexes = {
        @Index(name = "idx_dictName",columnList = "dictName")
})
@Entity
@Data
public class SysDictType extends TimedBasedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Excel(name = "字典主键", cellType = Excel.ColumnType.NUMERIC)
    @Search
    private Long dictId;

    @Excel(name = "字典名称")
    @Search(op = Search.Op.REGEX)
    @NotBlank(message = "字典名称不能为空")
    @Size(min = 0, max = 100, message = "字典类型名称长度不能超过100个字符")
    @Column
    private String dictName;

    @Excel(name = "字典类型")
    @Search
    @NotBlank(message = "字典类型不能为空")
    @Size(min = 0, max = 100, message = "字典类型类型长度不能超过100个字符")
    @Column
    private String dictType;

    /**
     * 状态（0正常 1停用）
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    @Column
    private String status;
}
