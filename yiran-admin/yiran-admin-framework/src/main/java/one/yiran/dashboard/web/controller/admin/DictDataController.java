package one.yiran.dashboard.web.controller.admin;

import one.yiran.dashboard.common.annotation.*;
import one.yiran.dashboard.entity.SysDictType;
import one.yiran.dashboard.service.SysDictTypeService;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.entity.SysDictData;
import one.yiran.dashboard.security.SessionContextHelper;
import one.yiran.dashboard.security.config.PermissionConstants;
import one.yiran.dashboard.service.SysDictDataService;
import one.yiran.dashboard.common.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@AjaxWrapper
@Controller
@RequestMapping("/system/dict/data")
public class DictDataController {

    @Autowired
    private SysDictDataService sysDictDataService;

    @Autowired
    private SysDictTypeService sysDictTypeService;

    @PostMapping("/detail")
    @RequirePermission(PermissionConstants.Dict.VIEW)
    public SysDictData detail(@ApiParam(required = true) Long dictCode) {
        return sysDictDataService.selectByPId(dictCode);
    }

    @PostMapping("/list")
    @RequirePermission(PermissionConstants.Dict.VIEW)
    public List<SysDictData> list(@ApiParam Long dictId, @ApiObject(createIfNull = true) SysDictData sysDictData, HttpServletRequest request) {
        sysDictData.setIsDelete(false);
        if(dictId != null) {
            SysDictType type = sysDictTypeService.selectByPId(dictId);
            if(type != null) {
                sysDictData.setDictType(type.getDictType());
            }
        }
        List<SysDictData> list = sysDictDataService.selectList(sysDictData);
        return list;
    }

    @Log(title = "字典数据", businessType = BusinessType.EXPORT)
    @RequirePermission(PermissionConstants.Dict.EXPORT)
    @PostMapping("/export")
    public void export(@ApiObject(createIfNull = true) SysDictData sysDictData, HttpServletRequest request, HttpServletResponse response) {
        List<SysDictData> list = sysDictDataService.selectList(PageRequestUtil.fromRequestIgnorePageSize(request), sysDictData);
        ExcelUtil<SysDictData> util = new ExcelUtil<>(SysDictData.class);
        util.exportExcel(response, list, "字典数据");
    }

    @Log(title = "字典数据", businessType = BusinessType.ADD)
    @RequirePermission(PermissionConstants.Dict.ADD)
    @PostMapping("/add")
    public SysDictData add(@ApiObject(validate = true) SysDictData dict) {
        dict.setCreateBy(SessionContextHelper.getCurrentLoginName());
        dict.setUpdateBy(SessionContextHelper.getCurrentLoginName());
        return sysDictDataService.insert(dict);
    }

    @Log(title = "字典数据", businessType = BusinessType.EDIT)
    @RequirePermission(PermissionConstants.Dict.EDIT)
    @PostMapping("/edit")
    public SysDictData edit(@ApiObject(validate = true) SysDictData dict) {
        dict.setUpdateBy(SessionContextHelper.getCurrentLoginName());
        return sysDictDataService.update(dict);
    }

    @Log(title = "字典数据", businessType = BusinessType.DELETE)
    @RequirePermission(PermissionConstants.Dict.DELETE)
    @PostMapping("/delete")
    public long remove(@ApiParam Long[] dictCodes) {
        return sysDictDataService.deleteByPIds(dictCodes);
    }
}