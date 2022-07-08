package one.yiran.dashboard.web.controller.admin;

import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.common.domain.PageModel;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.manage.entity.SysDictData;
import one.yiran.dashboard.manage.security.UserInfoContextHelper;
import one.yiran.dashboard.manage.security.config.PermissionConstants;
import one.yiran.dashboard.manage.service.SysDictDataService;
import one.yiran.dashboard.common.util.ExcelUtil;
import one.yiran.dashboard.common.annotation.RequirePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@AjaxWrapper
@Controller
@RequestMapping("/system/dict/data")
public class DictDataController {

    @Autowired
    private SysDictDataService sysDictDataService;

    @PostMapping("/list")
    @RequirePermission(PermissionConstants.Dict.VIEW)
    public PageModel list(@RequestBody SysDictData sysDictData, HttpServletRequest request) {
        sysDictData.setIsDelete(false);
        PageModel<SysDictData> list = sysDictDataService.selectPage(PageRequestUtil.fromRequest(request), sysDictData);
        return list;
    }

    @Log(title = "字典数据", businessType = BusinessType.EXPORT)
    @RequirePermission(PermissionConstants.Dict.EXPORT)
    @PostMapping("/export")
    public String export(@RequestBody SysDictData sysDictData, HttpServletRequest request) {
        List<SysDictData> list = sysDictDataService.selectList(PageRequestUtil.fromRequestIgnorePageSize(request), sysDictData);
        ExcelUtil<SysDictData> util = new ExcelUtil<SysDictData>(SysDictData.class);
        return util.exportExcel(list, "字典数据");
    }

    @Log(title = "字典数据", businessType = BusinessType.ADD)
    @RequirePermission(PermissionConstants.Dict.ADD)
    @PostMapping("/add")
    public SysDictData addSave(@Validated @RequestBody SysDictData dict) {
        dict.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
        dict.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        return sysDictDataService.insert(dict);
    }

    @Log(title = "字典数据", businessType = BusinessType.EDIT)
    @RequirePermission(PermissionConstants.Dict.EDIT)
    @PostMapping("/edit")
    public SysDictData editSave(@Validated @RequestBody SysDictData dict) {
        dict.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
        return sysDictDataService.update(dict);
    }

    @Log(title = "字典数据", businessType = BusinessType.DELETE)
    @RequirePermission(PermissionConstants.Dict.REMOVE)
    @PostMapping("/remove")
    public long remove(@RequestBody Long[] ids) {
        return sysDictDataService.deleteByPIds(ids);
    }
}