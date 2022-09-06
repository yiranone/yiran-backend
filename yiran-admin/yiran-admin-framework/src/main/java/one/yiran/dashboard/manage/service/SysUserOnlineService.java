package one.yiran.dashboard.manage.service;

import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.dashboard.manage.entity.SysUserOnline;

import java.util.Date;
import java.util.List;

public interface SysUserOnlineService {

    void batchDeleteOnline(List<String> sessionIds);

    void saveOnline(SysUserOnline online);

    void forceLogout(String sessionId);

    void refreshUserLastAccessTime(String sessionId,Date accessTime);

    List<SysUserOnline> selectOnlineByLastAccessTime(Date lastAccessTime);

    SysUserOnline selectByPId(String sessionId);

    PageModel<SysUserOnline> selectPage(PageRequest fromRequest, SysUserOnline sysUserOnline);

    void deleteById(String sessionId);

    List<SysUserOnline> findAll();
}
