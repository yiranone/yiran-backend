package one.yiran.dashboard.manage.service.impl;

import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.common.expection.user.UserNotFoundException;
import one.yiran.dashboard.manage.dao.*;
import one.yiran.dashboard.manage.entity.*;
import one.yiran.dashboard.manage.service.SysUserService;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.manage.service.SysRoleService;
import one.yiran.common.util.Convert;
import one.yiran.db.common.util.PredicateBuilder;
import one.yiran.db.common.util.PredicateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysRoleServiceImpl extends CrudBaseServiceImpl<Long, SysRole> implements SysRoleService {

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private RolePermDao rolePermDao;

    @Autowired
    private RoleMenuDao roleMenuDao;

    @Autowired
    private PermDao permDao;

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysRoleService sysRoleService;

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
            List<String> loginNames = this.selectLoginNameUserRoleByRoleId(roleId);
            if(loginNames != null && loginNames.size() >0) {
                throw BusinessException.build(String.format("角色[%s]已经分配给%s，不能删除",sysRole.getRoleName(),loginNames));
            }
//            if (countUserRoleByRoleId(roleId) > 0) {
//                throw BusinessException.build(String.format("角色[%s]已分配,不能删除", sysRole.getRoleName()));
//            }
            rolePermDao.deleteAllByRoleId(roleId);
        }
        return super.removeByPIds(ids);
    }

    @Override
    @Transactional
    public int insertRole(SysRole sysRole) {
        super.insert(sysRole);
        return insertRolePerm(sysRole.getRoleId(), sysRole.getMenuIds());
    }

    @Override
    @Transactional
    public int updateRole(SysRole sysRole) {
        Assert.notNull(sysRole.getRoleId(), "roleId cant be null");
        roleDao.save(sysRole);
        long deletedRolePermSize = roleMenuDao.deleteAllByRoleId(sysRole.getRoleId());
        entityManager.flush(); //保证上面的delete先执行
        log.info("清理原有角色数量{} 新增数量{}",deletedRolePermSize,sysRole.getPermIds().size());
        return insertRolePerm(sysRole.getRoleId(), sysRole.getMenuIds());
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

    @Override
    public List<String> selectLoginNameUserRoleByRoleId(Long roleId) {
        QSysUser qSysUser = QSysUser.sysUser;
        QSysUserRole qSysUserRole = QSysUserRole.sysUserRole;
        JPAQuery<String> jpa = queryFactory.select(qSysUser.loginName).from(qSysUser)
                .innerJoin(qSysUserRole)
                .on(qSysUser.userId.eq(qSysUserRole.userId))
                .on(qSysUserRole.roleId.eq(roleId));
        return jpa.fetch();
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
    public int insertAuthUsers(Long roleId, Long[] userIds) {
        Assert.notNull(roleId, "roleId cant be null");
        Assert.notNull(userIds, "userIds cant be null");
        if(roleDao.findByRoleId(roleId) == null ) {
            throw BusinessException.build("角色不存在roleId:"+ roleId);
        }
        // 新增用户与角色管理
        List<SysUserRole> list = new ArrayList<>();
        for (Long userId : userIds) {
            if(userDao.findByUserId(userId) == null ) {
                throw UserNotFoundException.build("用户不存在userId:"+ userId);
            }
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        return userRoleDao.saveAll(list).size();
    }

    /**
     * 批量取消授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    @Override
    public long deleteAuthUsers(Long roleId, Long[] userIds) {
        Assert.notNull(roleId, "roleId cant be null");
        Assert.notNull(userIds, "userIds cant be null");
        if(roleDao.findByRoleId(roleId) == null ) {
            throw BusinessException.build("角色不存在roleId:"+ roleId);
        }
        if(roleId.equals(1L)) {
            Arrays.stream(userIds).forEach(e -> {
                sysUserService.checkAdminModifyAllowed(new SysUser(e), "取消授权");
            });
        }
        List<SysUserRole> sysUserRoles = userRoleDao.findAllByRoleIdAndUserIdIn(roleId, userIds);
        userRoleDao.deleteAll(sysUserRoles);
        return sysUserRoles.size();
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

        List<SysRolePerm> rolePermList = rolePermDao.findAllByRoleId(roleId);
        List<Long> permIds = rolePermList.stream().map(SysRolePerm::getPermId).collect(Collectors.toList());
        role.setPermIds(permIds);

        List<SysRoleMenu> roleMenuList = roleMenuDao.findAllByRoleId(roleId);
        List<Long> menuIds = roleMenuList.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        role.setMenuIds(menuIds);
        return role;
    }

    /** 查询用户已经有的菜单权限 */
    @Override
    public List<String> findUserPermsByUserId(Long userId) {
        Assert.notNull(userId,"用户ID不能为空");
        QSysMenu qSysMenu = QSysMenu.sysMenu;
        QSysRoleMenu qSysRoleMenu = QSysRoleMenu.sysRoleMenu;
        QSysUserRole qSysUserRole = QSysUserRole.sysUserRole;
        QSysRole qSysRole = QSysRole.sysRole;
        JPAQuery<String> jpa = queryFactory.select(qSysMenu.perms).from(qSysMenu)
                .innerJoin(qSysRoleMenu)
                .on(qSysMenu.menuId.eq(qSysRoleMenu.menuId))
                .innerJoin(qSysUserRole)
                .on(qSysRoleMenu.roleId.eq(qSysUserRole.roleId)
                        .and(qSysUserRole.userId.eq(userId)))
                .innerJoin(qSysRole).on(qSysUserRole.roleId.eq(qSysRole.roleId))
                .where(PredicateUtil.buildNotDeletePredicate(qSysRole));
        return jpa.fetch();
    }

    private int insertRolePerm(Long roleId, List<Long> menuIds) {
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            entityManager.persist(rm);
        }
        return menuIds.size();
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
