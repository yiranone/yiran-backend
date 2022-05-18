package one.yiran.dashboard.manage.service;

import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.dashboard.manage.entity.SysUserOnline;

import java.util.Date;
import java.util.List;

public interface SysUserOnlineService {

    public void batchDeleteOnline(List<String> sessionIds);

    public void saveOnline(SysUserOnline online);

    public void forceLogout(String sessionId);

    public List<SysUserOnline> selectOnlineByLastAccessTime(Date lastAccessTime);

    SysUserOnline selectByPId(String sessionId);

    PageModel<SysUserOnline> selectPage(PageRequest fromRequest, SysUserOnline sysUserOnline);

    void deleteById(String sessionId);

    List<SysUserOnline> findAll();
}
