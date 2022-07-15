package one.yiran.dashboard.manage.entity;

import one.yiran.dashboard.common.annotation.Option;
import one.yiran.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.dashboard.common.annotation.Excel;
import one.yiran.db.common.annotation.Search;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_role")
@Entity
@Data
public class SysRole extends TimedBasedEntity {

    /**
     * 角色ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Excel(name = "角色序号", cellType = Excel.ColumnType.NUMERIC)
    private Long roleId;

    /**
     * 角色名称
     */
    @Excel(name = "角色名称")
    @Search(op = Search.Op.REGEX)
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 0, max = 30, message = "角色名称长度不能超过30个字符")
    @Column
    private String roleName;

    /**
     * 角色权限
     */
    @Excel(name = "角色权限")
    @Search
    @NotBlank(message = "权限字符不能为空")
    @Size(min = 0, max = 100, message = "权限字符长度不能超过100个字符")
    @Column
    private String roleKey;

    /**
     * 角色排序
     */
    @Excel(name = "角色排序")
    @NotBlank(message = "显示顺序不能为空")
    @Column
    private Integer roleSort;

    /**
     * 角色状态（0正常 1停用）
     */
    @Option(value = {"0","1"}, message = "状态只能是0，1; 0=正常,1=停用")
    @NotBlank(message = "状态不能为空")
    @Excel(name = "角色状态", readConverterExp = "0=正常,1=停用")
    @Search
    @Column(length = 8,nullable = false)
    private String status;

    /**
     * 用户是否存在此角色标识 默认不存在
     */
    @Transient
    private boolean flag = false;

    /**
     * 数据权限
     */
    @Transient
    private List<Long> permIds;

    @Transient
    private List<SysPerm> permList;

    public boolean isAdmin()
    {
        return isAdmin(this.roleId);
    }

    public static boolean isAdmin(Long roleId)
    {
        return roleId != null && 1L == roleId;
    }
}
