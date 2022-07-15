package one.yiran.dashboard.common.util;

import one.yiran.dashboard.common.model.UserSession;
import one.yiran.dashboard.manage.entity.SysUser;

public class MemberConvertUtil {
    public static UserSession convert(SysUser sysUser) {
        UserSession u = new UserSession();
        if (sysUser != null) {
            u.setUserId(sysUser.getUserId());
            u.setLoginName(sysUser.getLoginName());
            u.setLoginIp(sysUser.getLoginIp());
            u.setUserName(sysUser.getUserName());
            u.setEmail(sysUser.getEmail());
            u.setPhoneNumber(sysUser.getPhoneNumber());
            u.setSex(sysUser.getSex());
            u.setAvatar(sysUser.getAvatar());
            u.setDeptId(sysUser.getDeptId());
            u.setCreateTime(sysUser.getCreateTime());
            u.setUpdateTime(sysUser.getUpdateTime());
            //u.setDeptName(sysUser.getDeptName());
            u.setStatus(sysUser.getStatus());
            u.setCreateBy(sysUser.getCreateBy());
            u.setUpdateBy(sysUser.getUpdateBy());
            u.setAdmin(sysUser.isAdmin());
        }
        return u;
    }
}
