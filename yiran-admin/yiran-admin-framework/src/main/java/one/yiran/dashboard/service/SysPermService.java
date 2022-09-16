package one.yiran.dashboard.service;

import one.yiran.dashboard.entity.SysPerm;
import one.yiran.db.common.service.CrudBaseService;

import java.util.List;

public interface SysPermService extends CrudBaseService<Long, SysPerm> {

     void grantPermsToRole(Long roleId, List<Long> permIds );
     void revokePermsFromRole(Long roleId, List<Long> permIds);

     List<SysPerm> findPermsByRoleId(Long roleId);
     List<SysPerm> findPermsByUserId(Long userId);

     boolean checkPermNameUnique(SysPerm sysPerm);

     int insertPerm(SysPerm sysPerm);

     int updatePerm(SysPerm sysPerm);

     long removePerm(List<Long> permIds);
}
