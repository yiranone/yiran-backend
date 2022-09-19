package one.yiran.dashboard.web.controller.admin;

import com.querydsl.core.types.Predicate;
import one.yiran.common.domain.PageModel;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.*;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.common.util.ExcelUtil;
import one.yiran.dashboard.common.util.WrapUtil;
import one.yiran.dashboard.entity.QSysConfig;
import one.yiran.dashboard.entity.SysConfig;
import one.yiran.dashboard.security.config.PermissionConstants;
import one.yiran.dashboard.service.SysConfigService;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.db.common.util.PredicateBuilder;
import one.yiran.db.common.util.PredicateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@AjaxWrapper
@RequestMapping("/system/config")
public class ConfigAdminController {

    @Autowired
    private SysConfigService sysConfigService;

    @PostMapping("/detail")
    @RequirePermission(PermissionConstants.Config.VIEW)
    public SysConfig detail(@ApiParam(required = true) Long configId) {
        return sysConfigService.selectByPId(configId);
    }


    @RequirePermission(PermissionConstants.Config.VIEW)
    @PostMapping("/list")
    public PageModel list(@ApiObject(createIfNull = true) SysConfig sysConfig,
                          @ApiParam(format = "yyyy-MM-dd") Date createBeginTime,
                          @ApiParam(format = "yyyy-MM-dd") Date createEndTime,
                          HttpServletRequest request) {
        QSysConfig config = QSysConfig.sysConfig;
        List<Predicate> predicates = PredicateBuilder.builder()
                .addGreaterOrEqualIfNotBlank(config.createTime, createBeginTime)
                .addLittlerOrEqualIfNotBlank(config.createTime, createEndTime)
                .addExpression(PredicateUtil.buildNotDeletePredicate(config)).toList();
        return sysConfigService.selectPage(PageRequestUtil.fromRequest(request), sysConfig, predicates);
    }

    @Log(title = "参数管理", businessType = BusinessType.EXPORT)
    @RequirePermission(PermissionConstants.Config.EXPORT)
    @PostMapping("/export")
    public void export(@ApiObject(createIfNull = true) SysConfig sysConfig, HttpServletRequest request, HttpServletResponse response) {
        PageModel<SysConfig> list = sysConfigService.selectPage(PageRequestUtil.fromRequestIgnorePageSize(request), sysConfig);
        ExcelUtil<SysConfig> util = new ExcelUtil<>(SysConfig.class);
        util.exportExcel(response, list.getRows(), "参数数据");
    }

    /**
     * 新增保存参数配置
     */
    @RequirePermission(PermissionConstants.Config.ADD)
    @Log(title = "参数管理", businessType = BusinessType.ADD)
    @PostMapping("/add")
    public SysConfig addSave(@ApiObject(validate = true) SysConfig sysConfig) {
        if (!sysConfigService.checkConfigKeyUnique(sysConfig.getConfigKey(),sysConfig.getConfigId())) {
            throw BusinessException.build("新增参数'" + sysConfig.getConfigName() + "'失败，参数键名已存在");
        }
        return sysConfigService.insert(sysConfig);
    }

    @RequirePermission(PermissionConstants.Config.EDIT)
    @Log(title = "参数管理", businessType = BusinessType.EDIT)
    @PostMapping("/edit")
    public SysConfig editSave(@ApiObject(validate = true)SysConfig sysConfig) {
        if (!sysConfigService.checkConfigKeyUnique(sysConfig.getConfigKey(),sysConfig.getConfigId())) {
            throw BusinessException.build("修改参数'" + sysConfig.getConfigName() + "'失败，参数键名已存在");
        }
        return sysConfigService.update(sysConfig);
    }


    @RequirePermission(PermissionConstants.Config.DELETE)
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public long remove(@ApiParam(required = true) Long[] configIds) {
        return sysConfigService.deleteByPIds(configIds);
    }


    @PostMapping("/checkConfigKeyUnique")
    public Map checkConfigKeyUnique(@ApiParam(required = true) String configKey,
                                                    @ApiParam Long configId) {
        return WrapUtil.wrapWithExist(sysConfigService.checkConfigKeyUnique(configKey,configId));
    }
}