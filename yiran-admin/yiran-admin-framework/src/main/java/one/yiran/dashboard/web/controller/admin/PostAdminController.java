package one.yiran.dashboard.web.controller.admin;

import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.annotation.RequirePermission;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.common.util.ExcelUtil;
import one.yiran.dashboard.entity.SysPost;
import one.yiran.dashboard.security.config.PermissionConstants;
import one.yiran.dashboard.service.SysPostService;
import one.yiran.db.common.util.PageRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@AjaxWrapper
@Controller
@RequestMapping("/system/post")
public class PostAdminController {

    @Autowired
    private SysPostService sysPostService;

    @RequirePermission(PermissionConstants.Post.VIEW)
    @PostMapping("/list")
    public PageModel list(SysPost sysPost, HttpServletRequest request) {
        sysPost.setIsDelete(false);
        PageRequest pageRequest = PageRequestUtil.fromRequest(request);
        PageModel<SysPost> list = sysPostService.selectPage(pageRequest, sysPost);
        return list;
    }

    @Log(title = "岗位管理", businessType = BusinessType.EXPORT)
    @RequirePermission("system:post:export")
    @PostMapping("/export")
    public String export(SysPost sysPost, HttpServletRequest request) {
        PageRequest pageRequest = PageRequestUtil.fromRequestIgnorePageSize(request);
        PageModel<SysPost> list = sysPostService.selectPage(pageRequest, sysPost);
        ExcelUtil<SysPost> util = new ExcelUtil<SysPost>(SysPost.class);
        return util.exportExcel(list.getRows(), "岗位数据");
    }

    @RequirePermission(PermissionConstants.Post.REMOVE)
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(@RequestBody Long[] ids) throws BusinessException {
        return sysPostService.removePostInfo(ids);
    }

    @RequirePermission(PermissionConstants.Post.VIEW)
    @Log(title = "岗位管理", businessType = BusinessType.ADD)
    @PostMapping("/add")
    public SysPost addSave(@Validated SysPost sysPost) {
        if (!sysPostService.checkPostNameUnique(sysPost)) {
            throw BusinessException.build("新增岗位'" + sysPost.getPostName() + "'失败，岗位名称已存在");
        } else if (!sysPostService.checkPostCodeUnique(sysPost)) {
            throw BusinessException.build("新增岗位'" + sysPost.getPostName() + "'失败，岗位编码已存在");
        }
        return sysPostService.insert(sysPost);
    }

    @RequirePermission(PermissionConstants.Post.EDIT)
    @Log(title = "岗位管理", businessType = BusinessType.EDIT)
    @PostMapping("/edit")
    public SysPost editSave(@Validated SysPost sysPost) {
        if (!sysPostService.checkPostNameUnique(sysPost)) {
            throw BusinessException.build("修改岗位'" + sysPost.getPostName() + "'失败，岗位名称已存在");
        } else if (!sysPostService.checkPostCodeUnique(sysPost)) {
            throw BusinessException.build("修改岗位'" + sysPost.getPostName() + "'失败，岗位编码已存在");
        }
        return sysPostService.update(sysPost);
    }


    @PostMapping("/checkPostNameUnique")
    public boolean checkPostNameUnique(SysPost sysPost) {
        return sysPostService.checkPostNameUnique(sysPost);
    }

    @PostMapping("/checkPostCodeUnique")
    public boolean checkPostCodeUnique(SysPost sysPost) {
        return sysPostService.checkPostCodeUnique(sysPost);
    }
}
