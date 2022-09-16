package one.yiran.dashboard.web.controller.admin;

import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.annotation.RequirePermission;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.entity.SysPerm;
import one.yiran.dashboard.security.config.PermissionConstants;
import one.yiran.dashboard.service.SysPermService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/system/perm")
@AjaxWrapper
public class PermAdminController {

    @Autowired
    private SysPermService sysPermService;

    @RequirePermission(PermissionConstants.Perm.VIEW)
    @PostMapping("/list")
    public PageModel list(PageRequest pageRequest, @RequestBody SysPerm sysPerm) {
        sysPerm.setIsDelete(false);
        PageModel<SysPerm> list = sysPermService.selectPage(pageRequest, sysPerm);
        return list;
    }

    @Log(title = "权限管理", businessType = BusinessType.ADD)
    @RequirePermission(PermissionConstants.Perm.ADD)
    @PostMapping("/add")
    public int addSave(@Validated @RequestBody SysPerm sysPerm) {
        if (!sysPermService.checkPermNameUnique(sysPerm)) {
            throw BusinessException.build("新增权限'" + sysPerm.getPermName() + "'失败，权限名称已存在");
        }
        return sysPermService.insertPerm(sysPerm);
    }

    @Log(title = "权限管理", businessType = BusinessType.DELETE)
    @RequirePermission(PermissionConstants.Perm.REMOVE)
    @RequestMapping("/remove")
    public long remove(@RequestBody Map<String, List<Long>> params) {
        List<Long> permIds = params.get("ids");
        return sysPermService.removePerm(permIds);
    }

    @Log(title = "权限管理", businessType = BusinessType.EDIT)
    @RequirePermission(PermissionConstants.Perm.EDIT)
    @PostMapping("/edit")
    public int editSave(@Validated @RequestBody SysPerm sysPerm) {
        if (!sysPermService.checkPermNameUnique(sysPerm)) {
            throw BusinessException.build("修改权限'" + sysPerm.getPermName() + "'失败，权限名称已存在");
        }
        return sysPermService.updatePerm(sysPerm);
    }

    @RequirePermission(PermissionConstants.Perm.VIEW)
    @RequestMapping("/detail")
    public SysPerm detail(@RequestBody SysPerm sysPerm) {
        if (sysPerm.getPermId() == null) {
            throw BusinessException.build("permId不能为空");
        }
        return sysPermService.selectByPId(sysPerm.getPermId());
    }

}
