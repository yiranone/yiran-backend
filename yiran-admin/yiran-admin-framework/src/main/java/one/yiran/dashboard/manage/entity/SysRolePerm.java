package one.yiran.dashboard.manage.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_role_perm",indexes = {
        @Index(name = "idx_role_perm",columnList = "roleId,permId",unique = true)
})
@Entity
@Data
public class SysRolePerm implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 角色ID
     */
    @Column
    private Long roleId;

    /**
     * 权限ID
     */
    @Column
    private Long permId;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("roleId", getRoleId())
                .append("permId", getPermId())
                .toString();
    }
}
