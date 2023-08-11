package one.yiran.dashboard.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.common.exception.BusinessException;
import one.yiran.common.util.DateUtil;
import one.yiran.dashboard.dao.UserOnlineDao;
import one.yiran.dashboard.entity.QSysUserOnline;
import one.yiran.dashboard.entity.SysUserOnline;
import one.yiran.dashboard.service.SysUserOnlineService;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
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

    @Transactional
    @Override
    public void saveOnline(SysUserOnline online) {
        if (StringUtils.isBlank(online.getSessionId())) {
            throw BusinessException.build("session id is null");
        }
        userOnlineDao.save(online);
    }

    @Transactional
    @Override
    public void forceLogout(String sessionId) {
        Assert.notNull(sessionId,"");
        QSysUserOnline online = QSysUserOnline.sysUserOnline;
        queryFactory.update(online).set(online.status, SysUserOnline.OnlineStatus.off_line).
                where(online.sessionId.eq(sessionId)).execute();
    }

    //用户访问的时候 刷新下lastAccessTime
    @Transactional
    @Override
    public void refreshUserLastAccessTime(String sessionId,Date accessTime) {
        Assert.notNull(sessionId,"");
        Assert.notNull(accessTime,"");
        QSysUserOnline online = QSysUserOnline.sysUserOnline;
        queryFactory.update(online).set(online.lastAccessTime,accessTime).
                set(online.status, SysUserOnline.OnlineStatus.on_line).
        where(online.sessionId.eq(sessionId)).execute();
    }

    @Override
    public List<SysUserOnline> selectOnlineByLastAccessTime(Date lastAccessTime) {
        Assert.notNull(lastAccessTime,"");
        return (List<SysUserOnline>) userOnlineDao.findAll(QSysUserOnline.sysUserOnline.lastAccessTime.lt(lastAccessTime));
    }

    @Override
    public PageModel<SysUserOnline> selectPage(PageRequest fromRequest, SysUserOnline sysUserOnline) {
        BooleanExpression pre = QSysUserOnline.sysUserOnline.loginName.isNotEmpty();
        return super.selectPage(fromRequest,sysUserOnline,pre);
    }

    // 根据lastAccessTime 判断用户是不是离线了
    @Transactional
    @Override
    public void updateExpireUserOffline(Long sessionTimeout) {
        Assert.notNull(sessionTimeout,"");
        LocalDateTime dateTime = LocalDateTime.now().minusMinutes(sessionTimeout);
        Date x = DateUtil.toDate(dateTime);
        QSysUserOnline online = QSysUserOnline.sysUserOnline;
        long aff = queryFactory.update(online).set(online.status, SysUserOnline.OnlineStatus.off_line).
                where(online.lastAccessTime.loe(x).and(online.status.eq(SysUserOnline.OnlineStatus.on_line))).execute();
        log.info("后台用户登陆状态变化，转换{}人为离线状态",aff);
        dateTime = LocalDateTime.now().minusMinutes(sessionTimeout).minusDays(1);
        x = DateUtil.toDate(dateTime);
        aff = queryFactory.delete(online).where(online.lastAccessTime.loe(x)).execute();
        log.info("后台用户登陆状态变化，删除离线人数{}人",aff);

    }

}
