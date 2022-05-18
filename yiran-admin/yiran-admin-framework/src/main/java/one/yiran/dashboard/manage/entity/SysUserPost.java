package one.yiran.dashboard.manage.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_user_post",indexes = {
        @Index(name = "idx_user_post",columnList = "userId,postId",unique = true)
})
@Entity
public class SysUserPost implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 用户ID
     */
    @Column
    private Long userId;

    /**
     * 岗位ID
     */
    @Column
    private Long postId;
}
