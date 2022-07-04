package one.yiran.dashboard.manage.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.dashboard.manage.entity.pk.SysRolePK;

import javax.persistence.*;
import java.io.Serializable;


@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_user_role",indexes = {
        @Index(name = "idx_user_role",columnList = "userId,roleId",unique = true)
})
@Entity
@IdClass(SysRolePK.class)
public class SysUserRole implements Serializable {

    /**
     * 用户ID
     */
    @Id
    @Column(nullable = false)
    private Long userId;

    /**
     * 角色ID
     */
    @Id
    @Column(nullable = false)
    private Long roleId;
}

