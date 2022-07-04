package one.yiran.dashboard.manage.entity.pk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysRolePK implements Serializable {
    private Long userId;
    private Long roleId;
}
