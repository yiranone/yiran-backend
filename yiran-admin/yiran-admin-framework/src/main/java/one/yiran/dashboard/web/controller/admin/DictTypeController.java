package one.yiran.dashboard.web.controller.admin;

import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.common.domain.Ztree;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.*;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.common.util.ExcelUtil;
import one.yiran.dashboard.entity.SysDictType;
import one.yiran.dashboard.security.SessionContextHelper;
import one.yiran.dashboard.security.config.PermissionConstants;
import one.yiran.dashboard.service.SysDictTypeService;
import one.yiran.db.common.util.PageRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@AjaxWrapper
@Controller
@RequestMapping("/system/dict/type")
public class DictTypeController {

    @Autowired
    private SysDictTypeService sysDictTypeService;

    @PostMapping("/detail")
    @RequirePermission(PermissionConstants.Dict.VIEW)
    public SysDictType detail(@ApiParam(required = true) Long dictId) {
        return sysDictTypeService.selectByPId(dictId);
    }

    @RequirePermission(PermissionConstants.Dict.VIEW)
    @PostMapping("/list")
    public PageModel list(@ApiObject(createIfNull = true) SysDictType sysDictType, @ApiParam String dictId, PageRequest pageRequest) {
        sysDictType.setIsDelete(false);
        PageModel<SysDictType> list = sysDictTypeService.selectPage(pageRequest, sysDictType);
        return list;
    }

    @Log(title = "字典类型", businessType = BusinessType.EXPORT)
    @RequirePermission(PermissionConstants.Dict.EXPORT)
    @RequestMapping("/export")
    public void export(@ApiObject(createIfNull = true) SysDictType sysDictType, HttpServletRequest request, HttpServletResponse response) throws IOException {
        sysDictType.setIsDelete(false);
        List<SysDictType> list = sysDictTypeService.selectList(PageRequestUtil.fromRequestIgnorePageSize(request), sysDictType);
        ExcelUtil<SysDictType> util = new ExcelUtil<>(SysDictType.class);
        util.exportExcel(response, list, "字典类型");
    }

    @Log(title = "字典类型", businessType = BusinessType.ADD)
    @RequirePermission(PermissionConstants.Dict.ADD)
    @PostMapping("/add")
    public SysDictType addSave(@ApiObject SysDictType bean) {
        SysDictType dict = new SysDictType();
        dict.setDictType(bean.getDictType());
        dict.setDictName(bean.getDictName());
        dict.setStatus(bean.getStatus());
        dict.setRemark(bean.getRemark());
        dict.setCreateBy(SessionContextHelper.getCurrentLoginName());
        dict.setUpdateBy(SessionContextHelper.getCurrentLoginName());

        if (!sysDictTypeService.checkDictTypeUnique(dict)) {
            throw BusinessException.build("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }

        return sysDictTypeService.insertDictType(dict);
    }

    @Log(title = "字典类型", businessType = BusinessType.EDIT)
    @RequirePermission(PermissionConstants.Dict.EDIT)
    @PostMapping("/edit")
    public SysDictType editSave(@ApiObject SysDictType bean) {
        if (bean.getDictId() == null) {
            throw BusinessException.build("修改字典失败，dictId不能为空");
        }
        SysDictType dict = new SysDictType();
        dict.setDictId(bean.getDictId());
        dict.setDictType(bean.getDictType());
        dict.setDictName(bean.getDictName());
        dict.setStatus(bean.getStatus());
        dict.setRemark(bean.getRemark());
        dict.setUpdateBy(SessionContextHelper.getCurrentLoginName());

        if (!sysDictTypeService.checkDictTypeUnique(dict)) {
            throw BusinessException.build("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        return sysDictTypeService.updateDictType(dict);
    }

//    @Log(title = "字典类型", businessType = BusinessType.DELETE)
//    @RequirePermission(PermissionConstants.Dict.REMOVE)
//    @PostMapping("/remove")
//    public long remove(@ApiParam Long[] dictIds) throws BusinessException {
//        return sysDictTypeService.removeByPIds(dictIds);
//    }

    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @RequirePermission(PermissionConstants.Dict.REMOVE)
    @PostMapping("/delete")
    public long delete(@ApiParam Long[] dictIds) throws BusinessException {
        return sysDictTypeService.deleteByPIds(dictIds);
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
