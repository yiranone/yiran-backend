package one.yiran.dashboard.service;

import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.dashboard.entity.SysUserOnline;
import one.yiran.db.common.service.CrudBaseService;

import java.util.Date;
import java.util.List;

public interface SysUserOnlineService extends CrudBaseService<String, SysUserOnline> {

    void batchDeleteOnline(List<String> sessionIds);

    void saveOnline(SysUserOnline online);

    void forceLogout(String sessionId);

    void refreshUserLastAccessTime(String sessionId,Date accessTime);

    List<SysUserOnline> selectOnlineByLastAccessTime(Date lastAccessTime);

    PageModel<SysUserOnline> selectPage(PageRequest fromRequest, SysUserOnline sysUserOnline);

    void updateExpireUserOffline(Long sessionTimeout);
}
