package one.yiran.dashboard.web.controller.admin;

import lombok.extern.slf4j.Slf4j;
import one.yiran.common.domain.PageRequest;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.*;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.entity.SysDept;
import one.yiran.dashboard.security.config.PermissionConstants;
import one.yiran.dashboard.service.SysDeptService;
import one.yiran.db.common.util.PageRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/system/dept")
@AjaxWrapper
public class DeptAdminController {

    @Autowired
    private SysDeptService sysDeptService;

    @RequirePermission(PermissionConstants.Dept.VIEW)
    @PostMapping("/list")
    public List<SysDept> list(@ApiObject(createIfNull = true) SysDept sysDept, HttpServletRequest request) {
        PageRequest pq = PageRequestUtil.fromRequest(request);
        List<SysDept> depts = sysDeptService.selectAllDept( sysDept);
        return depts;
    }

    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @RequirePermission(PermissionConstants.Dept.REMOVE)
    @RequestMapping("/remove")
    public long remove(@ApiParam(required = true) Long[] deptIds) {
        return sysDeptService.deleteByPIds(deptIds);
    }

    @Log(title = "部门管理", businessType = BusinessType.ADD)
    @RequirePermission(PermissionConstants.Dept.ADD)
    @PostMapping("/add")
    public SysDept addSave(@ApiObject(validate = true) SysDept sysDept) {
        if(sysDept.getDeptId() != null)
            throw BusinessException.build("部门ID不能有值");
        return sysDeptService.insert(sysDept);
    }

    @Log(title = "菜单管理", businessType = BusinessType.EDIT)
    @RequirePermission(PermissionConstants.Dept.EDIT)
    @PostMapping("/edit")
    public SysDept editSave(@ApiObject(validate = true) SysDept sysDept) {
        if(sysDept.getDeptId() == null)
            throw BusinessException.build("部门ID不能为空");
        return sysDeptService.update(sysDept);
    }

//    @GetMapping("/deptTreeData")
//    public List<Ztree> menuTreeData() {
//        List<Ztree> ztrees = sysDeptService.menuTreeData();
//        return ztrees;
//    }

}
