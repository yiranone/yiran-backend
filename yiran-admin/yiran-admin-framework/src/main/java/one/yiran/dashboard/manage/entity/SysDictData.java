package one.yiran.dashboard.manage.entity;

import one.yiran.dashboard.common.annotation.Option;
import one.yiran.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.dashboard.common.annotation.Excel;
import one.yiran.db.common.annotation.Search;
import one.yiran.dashboard.common.constants.UserConstants;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_dict_data")
@Entity
@Data
public class SysDictData extends TimedBasedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Excel(name = "字典编码", cellType = Excel.ColumnType.NUMERIC)
    private Long dictCode;

    @Excel(name = "字典排序")
    @Column
    private Long dictSort;

    @Excel(name = "字典标签")
    @Search(op = Search.Op.REGEX)
    @NotBlank(message = "字典标签不能为空")
    @Size(min = 0, max = 100, message = "字典标签长度不能超过100个字符")
    @Column
    private String dictLabel;

    @Excel(name = "字典键值")
    @Search
    @NotBlank(message = "字典键值不能为空")
    @Size(min = 0, max = 100, message = "字典键值长度不能超过100个字符")
    @Column
    private String dictValue;

    @Excel(name = "字典类型")
    @Search
    @NotBlank(message = "字典类型不能为空")
    @Size(min = 0, max = 100, message = "字典类型长度不能超过100个字符")
    @Column
    private String dictType;

    @Excel(name = "字典样式")
    @Size(min = 0, max = 100, message = "样式属性长度不能超过100个字符")
    @Column
    private String cssClass;

    /**
     * 表格字典样式
     */
    @Column
    private String listClass;

    /**
     * 是否默认（Y是 N否）
     */
    @Excel(name = "是否默认", readConverterExp = "Y=是,N=否")
    @Column
    private String isDefault;

    @Option(value = {"0","1"}, message = "状态只能是0，1; 0=正常,1=停用")
    @NotBlank(message = "状态不能为空")
    @Search
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    @Column(length = 8,nullable = false)
    private String status;

    public boolean getDefault() {
        return UserConstants.YES.equals(this.isDefault) ? true : false;
    }
}
