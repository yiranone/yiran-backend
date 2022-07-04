package one.yiran.dashboard.web.controller.admin;

import one.yiran.common.domain.PageRequest;
import one.yiran.dashboard.common.annotation.*;
import one.yiran.dashboard.manage.dao.RolePermDao;
import one.yiran.dashboard.manage.entity.SysRolePerm;
import one.yiran.dashboard.manage.entity.SysRole;
import one.yiran.dashboard.manage.entity.SysUserRole;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.common.domain.PageModel;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.manage.entity.SysUser;
import one.yiran.dashboard.manage.security.UserInfoContextHelper;
import one.yiran.dashboard.manage.service.SysRoleService;
import one.yiran.dashboard.manage.service.SysUserService;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.manage.security.config.PermissionConstants;
import one.yiran.dashboard.common.util.ExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/system/role")
@AjaxWrapper
public class RoleAdminController {

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RolePermDao rolePermDao;

    @RequirePermission(PermissionConstants.Role.VIEW)
    @PostMapping("/list")
    public PageModel<SysRole> list(@ApiObject(createIfNull = true) SysRole sysRole, HttpServletRequest request) {
        sysRole.setIsDelete(false);
        PageRequest pageRequest = PageRequestUtil.fromRequest(request);
//        if(!UserInfoContextHelper.getLoginUser().isHasAllDeptPerm())
//            pageRequest.setDepts(UserInfoContextHelper.getLoginUser().getScopeData(PermissionConstants.Role.VIEW));
        PageModel<SysRole> list = sysRoleService.selectPage(pageRequest, sysRole);
        list.getRows().stream().forEach(t -> {
            List<SysRolePerm> rolePermList = rolePermDao.findAllByRoleId(t.getRoleId());
            List<Long> permIds = rolePermList.stream().map(p -> p.getPermId()).collect(Collectors.toList());
            t.setPermIds(permIds);
        });
        return list;
    }

    @Log(title = "角色管理", businessType = BusinessType.ADD)
    @RequirePermission(PermissionConstants.Role.ADD)
    @PostMapping("/add")
    public int addSave(@ApiObject(validate = true) SysRole sysRole) {
        if (!sysRoleService.checkRoleNameUnique(sysRole)) {
            throw BusinessException.build("新增角色'" + sysRole.getRoleName() + "'失败，角色名称已存在");
        } else if (!sysRoleService.checkRoleKeyUnique(sysRole)) {
            throw BusinessException.build("新增角色'" + sysRole.getRoleName() + "'失败，角色权限已存在");
        }
        sysRole.setRoleId(null);
        if (StringUtils.isBlank(sysRole.getStatus())) {
            sysRole.setStatus("0");
        }
        sysRole.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
        sysRole.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
        return sysRoleService.insertRole(sysRole);
    }

    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @RequirePermission(PermissionConstants.Role.EDIT)
    @PostMapping("/edit")
    public int editSave(@ApiObject(validate = true) SysRole sysRole) {
        if (!sysRoleService.checkRoleNameUnique(sysRole)) {
            throw BusinessException.build("修改角色'" + sysRole.getRoleName() + "'失败，角色名称已存在");
        } else if (!sysRoleService.checkRoleKeyUnique(sysRole)) {
            throw BusinessException.build("修改角色'" + sysRole.getRoleName() + "'失败，角色权限已存在");
        }

        SysRole dbSysRole = sysRoleService.selectByPId(sysRole.getRoleId());
        if (StringUtils.isNotBlank(sysRole.getStatus())) {
            dbSysRole.setStatus(sysRole.getStatus());
        }
        dbSysRole.setRoleName(sysRole.getRoleName());
        dbSysRole.setRoleKey(sysRole.getRoleKey());
        dbSysRole.setRoleSort(sysRole.getRoleSort());
        dbSysRole.setRemark(sysRole.getRemark());
        dbSysRole.setPermIds(sysRole.getPermIds());
        dbSysRole.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
        return sysRoleService.updateRole(dbSysRole);
    }

    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @RequirePermission(PermissionConstants.Role.REMOVE)
    @PostMapping("/remove")
    public long remove(@ApiParam(required = true) Long[] roleIds) {
        return sysRoleService.removeRoleInfo(roleIds);
    }

    @RequirePermission(PermissionConstants.Role.VIEW)
    @PostMapping("/detail")
    public SysRole detail(@ApiParam(required = true) Long roleId) throws BusinessException {
        return sysRoleService.findDetailById(roleId);
    }

    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @RequirePermission(PermissionConstants.Role.EDIT)
    @PostMapping("/changeStatus")
    public int changeStatus(@ApiObject SysRole sysRole) {
        sysRoleService.checkRoleAllowed(sysRole);
        return sysRoleService.changeStatus(sysRole);
    }

    /**
     * 取消授权
     */
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PostMapping("/authUser/cancel")
    public long cancelAuthUser(@ApiObject SysUserRole sysUserRole) {
        return sysRoleService.deleteAuthUser(sysUserRole);
    }

    /**
     * 批量取消授权
     */
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PostMapping("/authUser/cancelAll")
    @AjaxWrapper
    public long cancelAuthUserAll(@ApiParam(required = true) Long roleId,@ApiParam(required = true)  String userIds) {
        return sysRoleService.deleteAuthUsers(roleId, userIds);
    }

    /**
     * 查询已分配用户角色列表
     */
    @RequirePermission(PermissionConstants.Role.VIEW)
    @PostMapping("/authUser/allocatedList")
    @AjaxWrapper
    public PageModel<SysUser> allocatedList(@ApiParam(required = true) Long roleId,
                                            @ApiParam(required = true) SysUser user,
                                            HttpServletRequest request) {
        return sysUserService.selectAllocatedList(PageRequestUtil.fromRequest(request), roleId, user, null);
    }

    /**
     * 查询未分配用户角色列表
     */
    @RequirePermission(PermissionConstants.Role.VIEW)
    @PostMapping("/authUser/unallocatedList")
    @AjaxWrapper
    public PageModel<SysUser> unallocatedList(@ApiParam(required = true) Long roleId,
                                              @ApiParam(required = true) SysUser user,
                                              HttpServletRequest request) {
        return sysUserService.selectUnallocatedList(PageRequestUtil.fromRequest(request), roleId, user, null);
    }

    /**
     * 批量选择用户授权
     */
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PostMapping("/authUser/selectAll")
    @AjaxWrapper
    public int selectAuthUserAll(@ApiParam(required = true) Long roleId,@ApiParam(required = true)  String userIds) {
        return sysRoleService.insertAuthUsers(roleId, userIds);
    }

    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    @RequirePermission(PermissionConstants.Role.EXPORT)
    @PostMapping("/export")
    @AjaxWrapper
    public String export(@ApiObject SysRole sysRole, HttpServletRequest request) {
        List<SysRole> list = sysRoleService.selectList(PageRequestUtil.fromRequestIgnorePageSize(request), sysRole);
        ExcelUtil<SysRole> util = new ExcelUtil<SysRole>(SysRole.class);
        return util.exportExcel(list, "角色数据");
    }

}
