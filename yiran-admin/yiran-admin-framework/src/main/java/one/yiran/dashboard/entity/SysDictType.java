package one.yiran.dashboard.entity;

import one.yiran.dashboard.common.annotation.Option;
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

    @Option(value = {"1","2"},message = "状态只能是1，2。 1=正常,2=停用")
    @NotBlank(message = "状态不能为空")
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    @Column(length = 8,nullable = false)
    private String status;
}
