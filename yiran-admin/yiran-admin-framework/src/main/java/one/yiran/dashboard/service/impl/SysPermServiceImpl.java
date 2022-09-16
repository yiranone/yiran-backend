package one.yiran.dashboard.service.impl;

import com.querydsl.jpa.impl.JPAQuery;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.dao.PermDao;
import one.yiran.dashboard.dao.RoleDao;
import one.yiran.dashboard.dao.RolePermDao;
import one.yiran.dashboard.dao.UserPostDao;
import one.yiran.dashboard.entity.*;
import one.yiran.dashboard.security.UserInfoContextHelper;
import one.yiran.dashboard.service.SysPermService;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SysPermServiceImpl extends CrudBaseServiceImpl<Long, SysPerm> implements SysPermService {

    @Autowired
    private PermDao permDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private RolePermDao rolePermDao;

    @Autowired
    private UserPostDao userPostDao;

    @Transactional
    @Override
    public void grantPermsToRole(Long roleId, List<Long> permIds) {
        List<SysRolePerm> toSave = new ArrayList<>();
        for (Long permId : permIds) {
            long cou = rolePermDao.countByRoleIdAndPermId(roleId, permId);
            if (cou > 0) {

            } else {
                SysRolePerm sysRolePerm = new SysRolePerm();
                sysRolePerm.setPermId(permId);
                sysRolePerm.setRoleId(roleId);
                toSave.add(sysRolePerm);
            }
        }
        if (toSave.size() > 0)
            rolePermDao.saveAll(toSave);
    }

    @Transactional
    @Override
    public void revokePermsFromRole(Long roleId, List<Long> permIds) {
        Assert.notNull(roleId, "");
        Assert.notNull(permIds, "");
        rolePermDao.deleteAllByRoleIdAndPermIdIn(roleId, permIds);
    }

    @Override
    public List<SysPerm> findPermsByRoleId(Long roleId) {
        Assert.notNull(roleId, "");
        QSysPerm qSysPerm = QSysPerm.sysPerm;
        QSysRolePerm qSysRolePerm = QSysRolePerm.sysRolePerm;
        JPAQuery<SysPerm> jpa = queryFactory.selectFrom(qSysPerm).innerJoin(qSysRolePerm)
                .on(qSysPerm.permId.eq(qSysRolePerm.permId).and(qSysRolePerm.roleId.eq(roleId)));
        return jpa.fetch();
    }

    @Override
    public List<SysPerm> findPermsByUserId(Long userId) {
        Assert.notNull(userId, "");
        QSysPerm qSysPerm = QSysPerm.sysPerm;
        QSysRolePerm qSysRolePerm = QSysRolePerm.sysRolePerm;
        QSysUserRole qSysUserRole = QSysUserRole.sysUserRole;
        JPAQuery<SysPerm> jpa = queryFactory.selectFrom(qSysPerm)
                .innerJoin(qSysRolePerm)
                .on(qSysPerm.permId.eq(qSysRolePerm.permId))
                .innerJoin(qSysUserRole)
                .on(qSysRolePerm.roleId.eq(qSysUserRole.roleId)
                        .and(qSysUserRole.userId.eq(userId)));
        return jpa.fetch();
    }

    @Override
    public boolean checkPermNameUnique(SysPerm sysPerm) {
        Assert.notNull(sysPerm, "sysMenu 不能为空");
        Assert.notNull(sysPerm.getPermName(), "permName 不能为空");

        SysPerm m = permDao.findByPermName(sysPerm.getPermName());
        if (sysPerm.getPermId() == null && m != null) {//新增
            return false;
        } else if (sysPerm.getPermId() != null && m != null
                && !sysPerm.getPermId().equals(m.getPermId())) {//修改
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public int insertPerm(SysPerm sysPerm) {
        checkPermValid(sysPerm);
        if (sysPerm.getPermId() != null) {
            throw BusinessException.build("新建权限 permId不能有值:" + sysPerm.getPermId());
        }
        sysPerm.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
        sysPerm.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
        sysPerm.setStatus("0");
        super.insert(sysPerm);
        return 1;
    }

    @Override
    @Transactional
    public int updatePerm(SysPerm sysPerm) {
        Assert.notNull(sysPerm, "传入参数不能为空");
        Assert.notNull(sysPerm.getPermId(), "permId不能未空");
        SysPerm dbSysPerm = permDao.findByPermId(sysPerm.getPermId());
        if (dbSysPerm != null) {
            sysPerm.setPermId(dbSysPerm.getPermId());
        } else {
            throw BusinessException.build("权限未找到 permId=" + sysPerm.getPermId());
        }
        checkPermValid(sysPerm);
        sysPerm.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
        sysPerm.setUpdateTime(new Date());
        permDao.save(sysPerm);
        return 1;
    }

    @Override
    @Transactional
    public long removePerm(List<Long> permIds) {
        permIds.stream().forEach(permId -> {
            int usedCount = rolePermDao.countByPermId(permId);
            if (usedCount > 0) {
                List<SysRolePerm> mrs = rolePermDao.findAllByPermId(permId);
                List<String> noticeRoleNames = new ArrayList<>();
                mrs.forEach(e -> {
                    Long roleId = e.getRoleId();
                    SysRole sr = roleDao.findByRoleId(roleId);
                    noticeRoleNames.add(sr.getRoleName());
                });
                throw BusinessException.build("权限已分配给角色" + noticeRoleNames + ",不允许删除");
            }
        });
        return super.remove(permIds);
    }

    private void checkPermValid(SysPerm m) throws BusinessException {
        if (!checkPermNameUnique(m))
            throw BusinessException.build("权限名称重复");
        if (StringUtils.isBlank(m.getPermGroup())) {
            throw BusinessException.build("权限Key不能为空");
        }
        if (StringUtils.isBlank(m.getPermOperation())) {
            throw BusinessException.build("权限Key不能为空");
        }
        if (StringUtils.isBlank(m.getPermSort())) {
            throw BusinessException.build("权限排序不能为空");
        }
    }

}
