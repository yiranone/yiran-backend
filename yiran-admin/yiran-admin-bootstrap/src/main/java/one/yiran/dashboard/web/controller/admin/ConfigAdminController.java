package one.yiran.dashboard.web.controller.admin;

import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.manage.entity.SysConfig;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.common.domain.PageModel;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.manage.security.config.PermissionConstants;
import one.yiran.dashboard.manage.service.SysConfigService;
import one.yiran.dashboard.common.util.ExcelUtil;
import one.yiran.dashboard.common.annotation.RequirePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/system/config")
public class ConfigAdminController {
    private String prefix = "system/config";

    @Autowired
    private SysConfigService sysConfigService;

    @RequirePermission(PermissionConstants.Config.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel list(SysConfig sysConfig, HttpServletRequest request) {
        sysConfig.setIsDelete(false);
        return sysConfigService.selectPage(PageRequestUtil.fromRequest(request), sysConfig);
    }

    @Log(title = "参数管理", businessType = BusinessType.EXPORT)
    @RequirePermission(PermissionConstants.Config.EXPORT)
    @PostMapping("/export")
    @AjaxWrapper
    public String export(SysConfig sysConfig, HttpServletRequest request) {
        PageModel<SysConfig> list = sysConfigService.selectPage(PageRequestUtil.fromRequestIgnorePageSize(request), sysConfig);
        ExcelUtil<SysConfig> util = new ExcelUtil<SysConfig>(SysConfig.class);
        return util.exportExcel(list.getRows(), "参数数据");
    }

    /**
     * 新增参数配置
     */
    @RequirePermission(PermissionConstants.Config.ADD)
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 新增保存参数配置
     */
    @RequirePermission(PermissionConstants.Config.ADD)
    @Log(title = "参数管理", businessType = BusinessType.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public SysConfig addSave(SysConfig sysConfig) {
        if (!sysConfigService.checkConfigKeyUnique(sysConfig)) {
            throw BusinessException.build("新增参数'" + sysConfig.getConfigName() + "'失败，参数键名已存在");
        }
        return sysConfigService.insert(sysConfig);
    }

    /**
     * 修改参数配置
     */
    @RequirePermission(PermissionConstants.Config.EDIT)
    @GetMapping("/edit/{configId}")
    public String edit(@PathVariable("configId") Long configId, ModelMap mmap) {
        mmap.put("config", sysConfigService.selectByPId(configId));
        return prefix + "/edit";
    }

    @RequirePermission(PermissionConstants.Config.EDIT)
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @AjaxWrapper
    public SysConfig editSave(SysConfig sysConfig) {
        if (!sysConfigService.checkConfigKeyUnique(sysConfig)) {
            throw BusinessException.build("修改参数'" + sysConfig.getConfigName() + "'失败，参数键名已存在");
        }
        return sysConfigService.update(sysConfig);
    }


    @RequirePermission(PermissionConstants.Config.REMOVE)
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(@RequestBody Long[] ids) {
        return sysConfigService.deleteByPIds(ids);
    }


    @PostMapping("/checkConfigKeyUnique")
    @ResponseBody
    public boolean checkConfigKeyUnique(SysConfig sysConfig) {
        return sysConfigService.checkConfigKeyUnique(sysConfig);
    }
}