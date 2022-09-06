package one.yiran.dashboard.manage.service.impl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.manage.dao.UserOnlineDao;
import one.yiran.dashboard.manage.entity.QSysUserOnline;
import one.yiran.dashboard.manage.entity.SysUserOnline;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.manage.service.SysUserOnlineService;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class SysUserOnlineServiceImpl extends CrudBaseServiceImpl<String, SysUserOnline> implements SysUserOnlineService {

    @Autowired
    private UserOnlineDao userOnlineDao;

    @Override
    public void batchDeleteOnline(List<String> sessions) {
        for (String sessionId : sessions) {
            SysUserOnline sysUserOnline = userOnlineDao.findBySessionId(sessionId);
            if (sysUserOnline != null) {
                userOnlineDao.delete(sysUserOnline);
            }
        }
    }

    //@Transactional
    @Override
    public void saveOnline(SysUserOnline online) {
        if (StringUtils.isBlank(online.getSessionId())) {
            throw BusinessException.build("session id is null");
        }
        userOnlineDao.save(online);
    }

    //@Transactional
    @Override
    public void forceLogout(String sessionId) {
        userOnlineDao.deleteById(sessionId);
    }

    @Transactional
    @Override
    public void refreshUserLastAccessTime(String sessionId,Date accessTime) {
        Assert.notNull(sessionId,"");
        Assert.notNull(accessTime,"");
        QSysUserOnline online = QSysUserOnline.sysUserOnline;
        queryFactory.update(online).set(online.lastAccessTime,accessTime).
        where(online.sessionId.eq(sessionId)).execute();
    }

    @Override
    public List<SysUserOnline> selectOnlineByLastAccessTime(Date lastAccessTime) {
        Assert.notNull(lastAccessTime,"");
        return (List<SysUserOnline>) userOnlineDao.findAll(QSysUserOnline.sysUserOnline.lastAccessTime.lt(lastAccessTime));
    }

    @Override
    public SysUserOnline selectByPId(String sessionId) {
        return userOnlineDao.findBySessionId(sessionId);
    }

    @Override
    public PageModel<SysUserOnline> selectPage(PageRequest fromRequest, SysUserOnline sysUserOnline) {
        BooleanExpression pre = QSysUserOnline.sysUserOnline.loginName.isNotEmpty();
        return super.selectPage(fromRequest,sysUserOnline,pre);
    }

    @Override
    public void deleteById(String sessionId) {
        userOnlineDao.deleteById(sessionId);
    }

    @Override
    public List<SysUserOnline> findAll() {
        return userOnlineDao.findAll();
    }

    public List<SysUserOnline> selectList(Predicate predicate, PageRequest request, SysUserOnline target) {
        return super.selectList(request,target,predicate);
    }
}
