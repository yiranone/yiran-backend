package one.yiran.dashboard.web.controller;

import one.yiran.common.domain.PageModel;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.annotation.RequirePermission;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.manage.entity.SysRole;
import one.yiran.dashboard.manage.entity.SysUser;
import one.yiran.dashboard.manage.security.UserInfoContextHelper;
import one.yiran.dashboard.manage.security.config.PermissionConstants;
import one.yiran.dashboard.manage.service.SysMenuService;
import one.yiran.dashboard.manage.service.SysPermService;
import one.yiran.dashboard.manage.service.SysRoleService;
import one.yiran.dashboard.manage.service.SysUserService;
import one.yiran.dashboard.vo.UserPageVO;
import one.yiran.dashboard.web.util.ChannelCheckUtils;
import one.yiran.db.common.util.PageRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AjaxWrapper
@Controller
@RequestMapping("/auth")
public class UserAuthController {

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private SysPermService sysPermService;

    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysUserService sysUserService;

    /**
     * 批量选择用户授权一个角色，只新增
     */
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PostMapping("/roleUser/insert")
    public int insertRoleUsers(@ApiParam(required = true) Long roleId,
                                 @ApiParam(required = true) Long[] userIds) {
        return sysRoleService.insertAuthUsers(roleId, userIds);
    }
    /**
     * 给用户授权角色，存量的角色会被清理，客户端一次性传所有角色
     */
    @RequirePermission("system:user:role")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PostMapping("/userRole/update")
    public void insertAuthRole(@ApiParam(required = true) Long userId,
                               @ApiParam(required = true) Long[] roleIds) {
        if (roleIds != null && !Arrays.asList(roleIds).contains(1L))
            sysUserService.checkAdminModifyAllowed(new SysUser(userId), "取消授权");
        SysUser user = sysUserService.findUserCheckExist(userId);
        ChannelCheckUtils.checkHasPermission(user.getChannelId());
        List<Long> toSaveRoleIds = filterRoles(userId, Arrays.asList(roleIds));

        sysUserService.saveUserRoles(userId, toSaveRoleIds);
    }

    /**
     * 取消授权，用户-角色
     */
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PostMapping("/userRole/cancel")
    public long cancelAuthUser(@ApiParam(required = true) Long userId,
                               @ApiParam(required = true) Long roleId) {
        return sysRoleService.deleteAuthUser(userId,roleId);
    }

    /**
     * 批量取消授权，用户-角色
     */
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PostMapping("/userRole/cancelAll")
    public long cancelAuthUserAll(@ApiParam(required = true) Long roleId,
                                  @ApiParam(required = true)  String userIds) {
        return sysRoleService.deleteAuthUsers(roleId, userIds);
    }

    /**
     * 查询已分配用户角色列表
     */
    @RequirePermission(PermissionConstants.Role.VIEW)
    @PostMapping("/userRole/allocatedList")
    public PageModel<UserPageVO> allocatedList(@ApiParam(required = true) Long roleId,
                                            @ApiParam SysUser search,
                                            HttpServletRequest request) {
        return sysUserService.selectAllocatedList(PageRequestUtil.fromRequest(request), roleId, search, null);
    }

    /**
     * 查询未分配用户角色列表
     */
    @RequirePermission(PermissionConstants.Role.VIEW)
    @PostMapping("/userRole/unallocatedList")
    public PageModel<UserPageVO> unallocatedList(@ApiParam(required = true) Long roleId,
                                                 @ApiParam SysUser search,
                                                 HttpServletRequest request) {
        return sysUserService.selectUnallocatedList(PageRequestUtil.fromRequest(request), roleId, search, null);
    }

    /**
     * 有 PermissionConstants.User.ROLE 权限才能给其他人
     *
     * @param userId
     * @param roleIds
     * @return
     */
    private List<Long> filterRoles(Long userId, List<Long> roleIds) {
        //这里不能让用户保存的时候选择太多权限
        //这个用户以前就有的权限
        List<Long> dbRoles = sysRoleService.selectRolesByUserId(userId).stream().map(e -> e.getRoleId()).collect(Collectors.toList());
        //给客户端操作的角色
        List<SysRole> toPageRoles = sysRoleService.selectAllVisibleRolesByUserId(UserInfoContextHelper.getCurrentUserId(), userId);

        //除去给客户端展示的，剩下保持不变
        dbRoles.removeAll(toPageRoles.stream().map(e -> e.getRoleId()).collect(Collectors.toList()));
        //给客户端的检查有没有勾选
        if (roleIds != null && roleIds.size() > 0) {
            List<SysRole> requestRoles = sysRoleService.selectRolesByRoleIds(roleIds.toArray(new Long[]{}));
            List<Long> pageRoleIds = toPageRoles.stream().map(e -> e.getRoleId()).collect(Collectors.toList());
            //判断用户有没有添加新的角色
            requestRoles.forEach(e -> {
                //防止越权
                if (pageRoleIds.contains(e.getRoleId())) {
                    dbRoles.add(e.getRoleId());
                }
            });
        }
        return dbRoles;
    }

}