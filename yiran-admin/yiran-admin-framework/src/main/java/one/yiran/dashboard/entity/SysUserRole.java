package one.yiran.dashboard.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;


@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_user_role",indexes = {
        @Index(name = "idx_user_role",columnList = "userId,roleId",unique = true)
})
@Entity
public class SysUserRole implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 角色ID
     */
    @Column(nullable = false)
    private Long roleId;
}

