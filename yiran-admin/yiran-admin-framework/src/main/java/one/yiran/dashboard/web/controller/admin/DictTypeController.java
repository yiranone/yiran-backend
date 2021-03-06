package one.yiran.dashboard.web.controller.admin;

import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.common.domain.PageRequest;
import one.yiran.dashboard.manage.entity.SysDictType;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.common.domain.PageModel;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.common.domain.Ztree;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.manage.security.UserInfoContextHelper;
import one.yiran.dashboard.manage.security.config.PermissionConstants;
import one.yiran.dashboard.manage.service.SysDictTypeService;
import one.yiran.dashboard.common.util.ExcelUtil;
import one.yiran.dashboard.common.annotation.RequirePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/system/dict")
@AjaxWrapper
public class DictTypeController {

    @Autowired
    private SysDictTypeService sysDictTypeService;

    @RequirePermission(PermissionConstants.Dict.VIEW)
    @PostMapping("/list")
    public PageModel list(@RequestBody SysDictType sysDictType, @ApiParam String dictId, PageRequest pageRequest) {
        sysDictType.setIsDelete(false);
        PageModel<SysDictType> list = sysDictTypeService.selectPage(pageRequest, sysDictType);
        return list;
    }

    @Log(title = "字典类型", businessType = BusinessType.EXPORT)
    @RequirePermission(PermissionConstants.Dict.EXPORT)
    @PostMapping("/export")
    public Object export(@RequestBody SysDictType sysDictType, HttpServletRequest request) {
        List<SysDictType> list = sysDictTypeService.selectList(PageRequestUtil.fromRequestIgnorePageSize(request), sysDictType);
        ExcelUtil<SysDictType> util = new ExcelUtil<SysDictType>(SysDictType.class);
        String name = util.exportExcel(list, "字典类型");
        Map map = new HashMap<>();
        map.put("name", name);
        return name;
    }

    @Log(title = "字典类型", businessType = BusinessType.ADD)
    @RequirePermission(PermissionConstants.Dict.ADD)
    @PostMapping("/add")
    public SysDictType addSave(@Validated @RequestBody SysDictType bean) {
        SysDictType dict = new SysDictType();
        dict.setDictType(bean.getDictType());
        dict.setDictName(bean.getDictName());
        dict.setStatus(bean.getStatus());
        dict.setRemark(bean.getRemark());
        dict.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
        dict.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        if (!sysDictTypeService.checkDictTypeUnique(dict)) {
            throw BusinessException.build("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }

        return sysDictTypeService.insertDictType(dict);
    }

    @Log(title = "字典类型", businessType = BusinessType.EDIT)
    @RequirePermission(PermissionConstants.Dict.EDIT)
    @PostMapping("/edit")
    public SysDictType editSave(@Validated @RequestBody SysDictType bean) {
        if (bean.getDictId() == null) {
            throw BusinessException.build("修改字典失败，dictId不能为空");
        }
        SysDictType dict = new SysDictType();
        dict.setDictId(bean.getDictId());
        dict.setDictType(bean.getDictType());
        dict.setDictName(bean.getDictName());
        dict.setStatus(bean.getStatus());
        dict.setRemark(bean.getRemark());
        dict.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        if (!sysDictTypeService.checkDictTypeUnique(dict)) {
            throw BusinessException.build("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        return sysDictTypeService.updateDictType(dict);
    }

    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @RequirePermission(PermissionConstants.Dict.REMOVE)
    @PostMapping("/remove")
    public long remove(@RequestBody Long[] ids) throws BusinessException {
        return sysDictTypeService.removeByPIds(ids);
    }

    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @RequirePermission(PermissionConstants.Dict.REMOVE)
    @PostMapping("/delete")
    public long delete(@RequestBody Long[] ids) throws BusinessException {
        return sysDictTypeService.deleteByPIds(ids);
    }
//    public long remove(@RequestBody JSONObject jsons) throws BusinessException {
//        return sysDictTypeService.deleteByPIds(jsons.getJSONArray("ids").toArray(new String[]{}));
//    }

    @RequirePermission(PermissionConstants.Dict.VIEW)
    @GetMapping("/detail/{dictId}")
    public SysDictType detail(@PathVariable("dictId") Long dictId) {
        return sysDictTypeService.selectByPId(dictId);
    }

    @RequirePermission(PermissionConstants.Dict.VIEW)
    @PostMapping("/checkDictTypeUnique")
    public boolean checkDictTypeUnique(String dictType, Long dictId) {
        SysDictType dict = new SysDictType();
        dict.setDictId(dictId);
        dict.setDictType(dictType);
        return sysDictTypeService.checkDictTypeUnique(dict);
    }

    /**
     * 加载字典列表树
     */
    @RequirePermission(PermissionConstants.Dict.VIEW)
    @GetMapping("/treeData")
    public List<Ztree> treeData(HttpServletRequest request) {
        List<Ztree> ztrees = sysDictTypeService.selectDictTree(PageRequestUtil.fromRequestIgnorePageSize(request), new SysDictType());
        return ztrees;
    }
}
