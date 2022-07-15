package one.yiran.dashboard.manage.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.dashboard.common.annotation.Excel;
import one.yiran.dashboard.common.annotation.Option;
import one.yiran.db.common.annotation.Search;
import one.yiran.db.common.domain.TimedBasedEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_dept")
@Entity
@Data
public class SysDept extends TimedBasedEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 部门 序号
     */
    @Excel(name = "岗位序号", cellType = Excel.ColumnType.NUMERIC)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deptId;

    private Long parentId;

    @Search
    @Excel(name = "部门编码")
    @Column(length = 32)
    private String deptCode;

    @Search(op = Search.Op.REGEX)
    @Excel(name = "部门名称")
    @Column(length = 32)
    private String deptName;

    @Column
    private Integer deptSort;

    @Search(op = Search.Op.REGEX)
    @Excel(name = "联系电话")
    @Column(length = 15)
    private String phone;

    @Option(value = {"0","1"}, message = "状态只能是0，1; 0=正常,1=停用")
    @NotBlank(message = "状态不能为空")
    @Search
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    @Column(length = 8,nullable = false)
    private String status;

}
