package one.yiran.dashboard.web.controller;

import one.yiran.common.domain.PageModel;
import one.yiran.dashboard.common.annotation.*;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.entity.SysRole;
import one.yiran.dashboard.entity.SysUser;
import one.yiran.dashboard.security.UserInfoContextHelper;
import one.yiran.dashboard.security.config.PermissionConstants;
import one.yiran.dashboard.service.SysMenuService;
import one.yiran.dashboard.service.SysPermService;
import one.yiran.dashboard.service.SysRoleService;
import one.yiran.dashboard.service.SysUserService;
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
     * 当前登陆用户的权限
     * @return
     */
    @RequireUserLogin
    @PostMapping("/current/perms")
    public List<String> myPermission() {
        Long userId = UserInfoContextHelper.getCurrentUserId();
        return sysRoleService.findUserPermsByUserId(userId);
    }

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
     * 批量取消授权，用户-角色
     */
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PostMapping("/roleUser/cancel")
    public long cancelRoleUsers(@ApiParam(required = true) Long roleId,
                                  @ApiParam(required = true) Long[] userIds) {
        return sysRoleService.deleteAuthUsers(roleId, userIds);
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
     * 查询已分配用户角色列表
     */
    @RequirePermission(PermissionConstants.Role.VIEW)
    @PostMapping("/roleUser/allocatedList")
    public PageModel<UserPageVO> allocatedList(@ApiParam(required = true) Long roleId,
                                            @ApiObject SysUser search,
                                            HttpServletRequest request) {
        return sysUserService.selectAllocatedList(PageRequestUtil.fromRequest(request), roleId, search, null);
    }

    /**
     * 查询未分配用户角色列表
     */
    @RequirePermission(PermissionConstants.Role.VIEW)
    @PostMapping("/roleUser/unallocatedList")
    public PageModel<UserPageVO> unallocatedList(@ApiParam(required = true) Long roleId,
                                                 @ApiObject SysUser search,
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