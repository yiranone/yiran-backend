package one.yiran.dashboard.dao;

import one.yiran.dashboard.entity.SysUserRole;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleDao extends BaseDao<SysUserRole, Long> {

    @Modifying(clearAutomatically = true,flushAutomatically = true)
    @Query("delete from SysUserRole where userId = ?1")
    int deleteAllByUserId(Long userId);

    @Modifying(clearAutomatically = true,flushAutomatically = true)
    @Query("delete from SysUserRole where userId = ?1 and roleId = ?2")
    long deleteAllByUserIdAndRoleId(Long userId, Long roleId);

    int countByRoleId(Long roleId);

    List<SysUserRole> findAllByRoleIdAndUserIdIn(Long roleId, Long[] userIds);

    List<SysUserRole> findAllByRoleIdInAndUserId(List<Long> roleIds, Long userId);

    List<SysUserRole> findAllByUserId(Long userId);

}
