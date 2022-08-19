package one.yiran.dashboard.manage.service;

import one.yiran.dashboard.manage.entity.SysRole;
import one.yiran.dashboard.manage.entity.SysUserRole;
import one.yiran.db.common.service.CrudBaseService;
import one.yiran.common.exception.BusinessException;

import java.util.List;

public interface SysRoleService extends CrudBaseService<Long, SysRole> {

    List<SysRole> selectRolesByRoleIds(Long[] ids);
    /**
     * 根据用户ID查询角色，所有的 有check
     */
    List<SysRole> selectAllRolesByUserId(Long userId);

    /**
     * 获取当前登陆用户有权限看到的角色
     * @param currentUserId 当前登陆用户
     * @param userId 需要修改的用户，新增传null
     * @return
     */
    List<SysRole> selectAllVisibleRolesByUserId(Long currentUserId, Long userId);

    List<SysRole> selectRolesByUserId(Long userId);

    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    long deleteRoleById(Long roleId);

    /**
     * 批量删除角色用户信息
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     * @throws Exception 异常
     */
    long removeRoleInfo(Long[] ids) throws BusinessException;

    //保存role和role-perm
    int insertRole(SysRole sysRole);

    //更新role和role-perm
    int updateRole(SysRole sysRole);

    /**
     * 校验角色名称是否唯一
     */
    boolean checkRoleNameUnique(SysRole sysRole);

    /**
     * 校验角色权限是否唯一
     */
    boolean checkRoleKeyUnique(SysRole sysRole);

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    int countUserRoleByRoleId(Long roleId);

    /**
     * 查询角色都分配给哪些loginName了
     * @param roleId
     * @return
     */
    List<String> selectLoginNameUserRoleByRoleId(Long roleId);

    /**
     * 角色状态修改
     *
     * @param sysRole 角色信息
     * @return 结果
     */
    int changeStatus(SysRole sysRole);


    /**
     * 批量选择授权用户角色
     */
    int insertAuthUsers(Long roleId, Long[] userIds);
    /**
     * 取消授权用户角色
     */
    long deleteAuthUsers(Long roleId, Long[] userIds);
    int deleteAuthUsers(Long userId);


    void checkRoleAllowed(SysRole sysRole);

    SysRole findDetailById(Long roleId);

    List<String> findUserPermsByUserId(Long userId);
}
