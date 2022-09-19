package one.yiran.dashboard.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.dashboard.common.annotation.Excel;
import one.yiran.dashboard.common.annotation.Option;
import one.yiran.db.common.annotation.Search;
import one.yiran.db.common.domain.TimedBasedEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_perm")
@Entity
@Data
public class SysPerm extends TimedBasedEntity {

    /**
     * 权限ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Excel(name = "权限序号", cellType = Excel.ColumnType.NUMERIC)
    private Long permId;


    /**
     * 权限key
     */
    @Excel(name = "权限key")
    @Search
    @NotBlank(message = "权限字符不能为空")
    @Size(min = 0, max = 100, message = "权限key字符长度不能超过100个字符")
    @Column
    private String permGroup;

    /**
     * 权限key
     */
    @Excel(name = "权限key")
    @Search
    @Size(min = 0, max = 100, message = "权限key字符长度不能超过100个字符")
    @Column
    private String permOperation;

    /**
     * 权限名称
     */
    @Excel(name = "权限名称")
    @Search(op = Search.Op.REGEX)
    @NotBlank(message = "权限名称不能为空")
    @Size(min = 0, max = 30, message = "权限名称长度不能超过30个字符")
    @Column
    private String permName;

    /**
     * 权限排序
     */
    @Excel(name = "权限排序")
    @NotBlank(message = "显示顺序不能为空")
    @Column
    private String permSort;

    @Option(value = {"1","2"},message = "状态只能是1，2。 1=正常,2=停用")
    @NotBlank(message = "状态不能为空")
    @Excel(name = "权限状态", readConverterExp = "0=正常,1=停用")
    @Search
    @Column(length = 8,nullable = false)
    private String status;

    @Transient
    private boolean flag = false;
}
