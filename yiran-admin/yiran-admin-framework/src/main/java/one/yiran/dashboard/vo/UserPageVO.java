package one.yiran.dashboard.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import one.yiran.common.util.DateUtil;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.util.SpringUtil;
import one.yiran.dashboard.entity.SysDept;
import one.yiran.dashboard.entity.SysUser;
import one.yiran.dashboard.security.service.PasswordService;

import java.util.List;

@Data
@NoArgsConstructor
public class UserPageVO {
    private Long userId;
    private String loginName;
    private String userName;
    private String phoneNumber;
    private String email;
    private String sex;
    private String avatar;
    private String status;
    private Boolean isAdmin;

    private String token;
    private Long tokenExpires; //token过期时间 毫秒

    private String loginDate;
    private String createTime;
    private String updateTime;
    private String createBy;
    private String updateBy;

    //详细页面返回
    private List<Long> roleIds;
    private List<String> roleNames;

    private Long deptId;
    private String deptName;
    private Long channelId;
    private String channelName;
    private Boolean isLock;

    public static UserPageVO from(SysUser sysUser) {
        UserPageVO vo = new UserPageVO();
        vo.setUserId(sysUser.getUserId());
        vo.setLoginName(sysUser.getLoginName());
        vo.setUserName(sysUser.getUserName());
        vo.setPhoneNumber(sysUser.getPhoneNumber());
        vo.setDeptId(sysUser.getDeptId());
        vo.setChannelId(sysUser.getChannelId());
        vo.setEmail(sysUser.getEmail());
        vo.setSex(sysUser.getSex());
        vo.setAvatar(sysUser.getAvatar());
        vo.setStatus(sysUser.getStatus());
        vo.setIsAdmin(sysUser.isAdmin());
        vo.setLoginDate(DateUtil.dateTime(sysUser.getLoginDate()));
        vo.setCreateTime(DateUtil.dateTime(sysUser.getCreateTime()));
        vo.setUpdateTime(DateUtil.dateTime(sysUser.getUpdateTime()));
        vo.setCreateBy(sysUser.getCreateBy());
        vo.setUpdateBy(sysUser.getUpdateBy());

        PasswordService ps = SpringUtil.getBean(PasswordService.class);
        if (!ps.timeExpire(sysUser.getPasswordErrorTime()) && sysUser.getPasswordErrorCount() != null && sysUser.getPasswordErrorCount().longValue() >= Global.getPasswordLimitCount() ){
            vo.setIsLock(true);
        } else {
            vo.setIsLock(false);
        }
        return vo;
    }

    public static UserPageVO from(SysUser sysUser, SysDept  sysDept) {
        UserPageVO vo = from(sysUser);
        if(sysDept != null) {
            vo.setDeptName(sysDept.getDeptName());
        }
        return vo;
    }
}
