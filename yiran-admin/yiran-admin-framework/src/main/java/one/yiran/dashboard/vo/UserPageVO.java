package one.yiran.dashboard.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import one.yiran.common.util.DateUtil;
import one.yiran.dashboard.entity.SysDept;
import one.yiran.dashboard.entity.SysUser;

import java.util.List;

@Data
@NoArgsConstructor
public class UserPageVO {
    private Long userId;
    private String loginName;
    private String userName;
    private String phoneNumber;
    private String status;
    private String createTime;
    private String updateTime;
    private String createBy;
    private String updateBy;

    //详细页面返回
    private List<Long> roleIds;

    private Long deptId;
    private String deptName;
    private Long channelId;

    public static UserPageVO from(SysUser sysUser) {
        UserPageVO vo = new UserPageVO();
        vo.setUserId(sysUser.getUserId());
        vo.setLoginName(sysUser.getLoginName());
        vo.setUserName(sysUser.getUserName());
        vo.setPhoneNumber(sysUser.getPhoneNumber());
        vo.setDeptId(sysUser.getDeptId());
        vo.setChannelId(sysUser.getChannelId());
        vo.setStatus(sysUser.getStatus());
        vo.setCreateTime(DateUtil.dateTime(sysUser.getCreateTime()));
        vo.setUpdateTime(DateUtil.dateTime(sysUser.getUpdateTime()));
        vo.setCreateBy(sysUser.getCreateBy());
        vo.setUpdateBy(sysUser.getUpdateBy());
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
