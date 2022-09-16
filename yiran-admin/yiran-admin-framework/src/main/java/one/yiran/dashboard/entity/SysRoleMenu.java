package one.yiran.dashboard.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_role_menu",indexes = {
        @Index(name = "idx_role_menu",columnList = "roleId,menuId",unique = true)
})
@Entity
@Data
public class SysRoleMenu implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 角色ID
     */
    @Column
    private Long roleId;

    /**
     * 菜单ID
     */
    @Column
    private Long menuId;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("roleId", getRoleId())
                .append("menuId", getMenuId())
                .toString();
    }
}
