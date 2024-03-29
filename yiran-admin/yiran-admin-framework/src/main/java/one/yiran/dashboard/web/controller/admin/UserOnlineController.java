package one.yiran.dashboard.web.controller.admin;

import lombok.extern.slf4j.Slf4j;
import one.yiran.common.domain.PageModel;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiObject;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.annotation.RequirePermission;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.common.model.UserSession;
import one.yiran.dashboard.util.UserCacheUtil;
import one.yiran.dashboard.entity.SysUserOnline;
import one.yiran.dashboard.security.SessionContextHelper;
import one.yiran.dashboard.security.config.PermissionConstants;
import one.yiran.dashboard.service.SysUserOnlineService;
import one.yiran.db.common.util.PageRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@AjaxWrapper
@Controller
@RequestMapping("/system/online")
public class UserOnlineController {

    @Autowired
    private SysUserOnlineService sysUserOnlineService;

    @RequirePermission(PermissionConstants.UserOnline.VIEW)
    @PostMapping("/list")
    public PageModel<SysUserOnline> list(HttpServletRequest request, @ApiObject SysUserOnline sysUserOnline) {
        return sysUserOnlineService.selectPage(PageRequestUtil.fromRequest(request), sysUserOnline);
    }

    @RequirePermission(PermissionConstants.UserOnline.FORCE_LOGOUT)
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @PostMapping("/batchForceLogout")
    public boolean batchForceLogout(@ApiParam String[] ids) {
        for (String sessionId : ids) {
            forceLogout(sessionId);
        }
        return true;
    }

    @RequirePermission(PermissionConstants.UserOnline.FORCE_LOGOUT)
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @PostMapping("/forceLogout")
    public boolean forceLogout(@ApiParam String sessionId) {
        if (sessionId.equals(SessionContextHelper.getLoginUser().getToken())) {
            //throw BusinessException.build("当前登陆用户无法强退");
        }

        SysUserOnline sysUserOnline = sysUserOnlineService.selectByPId(sessionId);
        if(sysUserOnline != null){
            sysUserOnline.setStatus(SysUserOnline.OnlineStatus.off_line);
            sysUserOnlineService.saveOnline(sysUserOnline);
            log.info("手动退出用户{} sessionId={}",sysUserOnline.getLoginName(), sysUserOnline.getSessionId());
        }

        UserSession session = UserCacheUtil.getSessionInfo(sessionId);
        UserCacheUtil.removeSessionInfo(sessionId);

        return true;
    }
}
