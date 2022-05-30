package one.yiran.dashboard.manage.service.impl;

import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.manage.entity.*;
import one.yiran.dashboard.manage.dao.PermDao;
import one.yiran.dashboard.manage.service.SysUserService;
import one.yiran.dashboard.manage.dao.RoleDao;
import one.yiran.dashboard.manage.dao.RolePermDao;
import one.yiran.dashboard.manage.dao.UserRoleDao;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.manage.service.SysRoleService;
import one.yiran.common.util.Convert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;

@Slf4j
@Service
public class SysRoleServiceImpl extends CrudBaseServiceImpl<Long, SysRole> implements SysRoleService {

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private RolePermDao rolePermDao;

    @Autowired
    private PermDao permDao;

    @Autowired
    private SysUserService sysUserService;

    @Override
    public List<SysRole> selectAllRolesByUserId(Long userId) {
        List<SysRole> userSysRoles = findAllByUserId(userId);
        List<SysRole> sysRoles = selectAll();
        for (SysRole sysRole : sysRoles) {
            for (SysRole userSysRole : userSysRoles) {
                if (sysRole.getRoleId().longValue() == userSysRole.getRoleId().longValue()) {
                    sysRole.setFlag(true);
                    break;
                }
            }
        }
        //返回的是系统所有的角色,用户有权限的节点会打勾
        return sysRoles;
    }

    @Override
    public List<SysRole> selectAllVisibleRolesByUserId(Long currentUserId, Long userId) {
        List<SysRole> sysRoles = selectAll();
        if(userId != null) {
            List<SysRole> userSysRoles = findAllByUserId(userId);
            for (SysRole sysRole : sysRoles) {
                for (SysRole userSysRole : userSysRoles) {
                    if (sysRole.getRoleId().longValue() == userSysRole.getRoleId().longValue()) {
                        sysRole.setFlag(true);
                        break;
                    }
                }
            }
        }
        //返回的是系统所有的角色,用户有权限的节点会打勾
        return sysRoles;
    }

    @Override
    public List<SysRole> selectRolesByRoleIds(Long[] ids) {
        Assert.notNull(ids,"");
        return roleDao.findAllByRoleIdIn(ids);
    }

    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        return findAllByUserId(userId);
    }

    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    @Transactional
    public long deleteRoleById(Long roleId) {
        return removeRoleInfo(new Long[]{roleId} );
    }

    /**
     * 批量删除角色信息
     *
     * @param ids 需要删除的数据ID
     * @throws Exception
     */
    @Override
    @Transactional
    public long removeRoleInfo(Long[] ids) throws BusinessException {
        Long[] roleIds = ids;
        for (Long roleId : roleIds) {
            SysRole sysRole = selectByPId(roleId);
            checkRoleAllowed(sysRole);
            if (countUserRoleByRoleId(roleId) > 0) {
                throw BusinessException.build(String.format("%1$s已分配,不能删除", sysRole.getRoleName()));
            }
            rolePermDao.deleteAllByRoleId(roleId);
        }
        return super.removeByPIds(ids);
    }

    @Override
    @Transactional
    public int insertRole(SysRole sysRole) {
        super.insert(sysRole);
        return insertRolePerm(sysRole.getRoleId(), sysRole.getPermIds());
    }

    @Override
    @Transactional
    public int updateRole(SysRole sysRole) {
        Assert.notNull(sysRole.getRoleId(), "roleId cant be null");
        roleDao.save(sysRole);
        long deletedRolePermSize = rolePermDao.deleteAllByRoleId(sysRole.getRoleId());
        log.info("清理原有角色数量{} 新增数量{}",deletedRolePermSize,sysRole.getPermIds().size());
        rolePermDao.flush();
        return insertRolePerm(sysRole.getRoleId(), sysRole.getPermIds());
    }

    @Override
    public boolean checkRoleNameUnique(SysRole sysRole) {
        Long roleId = sysRole.getRoleId() == null ? -1L : sysRole.getRoleId();
        SysRole info = roleDao.findByRoleName(sysRole.getRoleName());
        if (info != null && info.getRoleId().longValue() != roleId.longValue()) {
            return false;
        }
        return true;

    }

    @Override
    public boolean checkRoleKeyUnique(SysRole sysRole) {
        Long roleId = sysRole.getRoleId() == null ? -1L : sysRole.getRoleId();
        SysRole info = roleDao.findByRoleKey(sysRole.getRoleKey());
        if (info != null && info.getRoleId().longValue() != roleId.longValue()) {
            return false;
        }
        return true;
    }

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    public int countUserRoleByRoleId(Long roleId) {
        return userRoleDao.countByRoleId(roleId);
    }

    /**
     * 角色状态修改
     *
     * @param sysRole 角色信息
     * @return 结果
     */
    @Override
    public int changeStatus(SysRole sysRole) {
        SysRole dbSysRole = roleDao.findByRoleId(sysRole.getRoleId());
        dbSysRole.setStatus(sysRole.getStatus());
        roleDao.save(dbSysRole);
        return 1;
    }

    /**
     * 取消授权用户角色
     *
     * @param sysUserRole 用户和角色关联信息
     * @return 结果
     */
    @Override
    public long deleteAuthUser(SysUserRole sysUserRole) {
        Assert.notNull(sysUserRole.getUserId(), "userId cant be null");
        Assert.notNull(sysUserRole.getRoleId(), "roleId cant be null");
        if(sysUserRole.getRoleId().equals(1L)) {
            sysUserService.checkAdminModifyAllowed(new SysUser(sysUserRole.getUserId()), "取消授权");
        }
        return userRoleDao.deleteAllByUserIdAndRoleId(sysUserRole.getUserId(), sysUserRole.getRoleId());
    }

    /**
     * 批量取消授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    @Override
    public long deleteAuthUsers(Long roleId, String userIds) {
        Assert.notNull(roleId, "roleId cant be null");
        if(roleId.equals(1L)) {
            List<Long> uids = Convert.toLongList(userIds);
            uids.forEach(e -> {
                sysUserService.checkAdminModifyAllowed(new SysUser(e), "取消授权");
            });
        }
        List<SysUserRole> sysUserRoles = userRoleDao.findAllByRoleIdAndUserIdIn(roleId, Convert.toLongArray(userIds));
        userRoleDao.deleteAll(sysUserRoles);
        return sysUserRoles.size();
    }

    @Override
    public long deleteAuthUsers(List<Long> roleIds, Long userId) {
        sysUserService.checkAdminModifyAllowed(new SysUser(userId),"取消授权");
        List<SysUserRole> sysUserRoles = userRoleDao.findAllByRoleIdInAndUserId(roleIds, userId);
        userRoleDao.deleteAll(sysUserRoles);
        return sysUserRoles.size();
    }

    @Override
    public int deleteAuthUsers(Long userId) {
        sysUserService.checkAdminModifyAllowed(new SysUser(userId),"取消授权");
        return userRoleDao.deleteAllByUserId(userId);
    }

    /**
     * 批量选择授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    @Override
    public int insertAuthUsers(Long roleId, String userIds) {
        Long[] users = Convert.toLongArray(userIds);
        // 新增用户与角色管理
        List<SysUserRole> list = new ArrayList<SysUserRole>();
        for (Long userId : users) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        return userRoleDao.saveAll(list).size();
    }

    @Override
    public void checkRoleAllowed(SysRole sysRole) {
        if (sysRole.getRoleId() !=null  && sysRole.isAdmin()) {
            throw BusinessException.build("不允许操作超级管理员角色");
        }
    }

    @Override
    public SysRole findDetailById(Long roleId) {
        SysRole role = super.selectByPId(roleId);
        List<SysPerm> permList = permDao.findAllByOrderByPermSortAscPermIdAsc();
        List<SysRolePerm> rolePermList = rolePermDao.findAllByRoleId(roleId);
        permList.stream().forEach(p -> {
            long count = rolePermList.stream().filter(rp -> p.getPermId().equals(rp.getPermId())).count();
            if (count > 0) {
                p.setFlag(true);
            }
        });
        role.setPermList(permList);
        return role;
    }

    private int insertRolePerm(Long roleId, List<Long> permIds) {
        int rows = 1;
        List<SysRolePerm> list = new ArrayList<>();
        for (Long permId : permIds) {
            SysRolePerm rm = new SysRolePerm();
            rm.setRoleId(roleId);
            rm.setPermId(permId);
            list.add(rm);
        }
        if (list.size() > 0) {
            List<SysRolePerm> rm = rolePermDao.saveAll(list);
            rows = rm.size();
        }
        return rows;
    }

    /**
     * 查找用户拥有的角色,返回所有
     *
     * @param userId
     * @return
     */
    private List<SysRole> findAllByUserId(Long userId) {
        Assert.notNull(userId, "");

        QSysRole qSysRole = QSysRole.sysRole;
        QSysUserRole qSysUserRole = QSysUserRole.sysUserRole;
        JPAQuery<SysRole> jpa = queryFactory.selectFrom(qSysRole)
                .innerJoin(qSysUserRole)
                .on(qSysRole.roleId.eq(qSysUserRole.roleId))
                .on(qSysUserRole.userId.eq(userId))
                .where(qSysRole.isDelete.ne(Boolean.TRUE));
        return jpa.fetch();
    }
}
