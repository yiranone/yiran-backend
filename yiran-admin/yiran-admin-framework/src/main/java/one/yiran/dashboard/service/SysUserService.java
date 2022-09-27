package one.yiran.dashboard.service;


import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.dashboard.entity.SysUser;
import one.yiran.dashboard.vo.UserPageVO;

import java.util.Date;
import java.util.List;

public interface SysUserService {

    SysUser login(String username, String password);

    SysUser findUser(Long id);
    SysUser findUserCheckExist(Long userId);

    SysUser findUserByLoginName(String username);

    SysUser findUserByEmail(String email);

    SysUser findUserByPhoneNumber(String phoneNumber);

    SysUser recordLoginIp(Long userId, String loginIp);

    SysUser recordLoginFail(Long userId,long passwordErrorCount);

    SysUser resetLoginFail(Long userId);

    SysUser updateMyInfos(Long userId,String userName,String email,String phoneNumber,String sex);

    SysUser updateMyAvatar(Long userId,String avatar);

    SysUser saveUser(SysUser user);

    SysUser saveUserAndPerms(SysUser user);

    void saveUserRoles(Long userId,List<Long> roleIds);

    List<SysUser> findUsersByUserIds(Long[] id);

    PageModel<SysUser> getPage(PageRequest pageRequest, SysUser searchUser);

    /**
     * 查询用户列表，返回包含角色信息
     * @param pageRequest
     * @param searchUser
     * @return
     */
    PageModel<UserPageVO> getPageDetail(PageRequest pageRequest, SysUser searchUser, String deptName, Long deptId, Long roleId);

    long getListSize(SysUser searchUser, Date bTime, Date eTime);

    boolean isPhoneNumberExist(String phoneNumber,Long userId);

    boolean isEmailExist(String email,Long userId);

    boolean isLoginNameExist(String loginName,Long userId);

    PageModel<UserPageVO> selectUnallocatedList(PageRequest request, Long roleId, SysUser searchUser, List<Long> deptIds);

    PageModel<UserPageVO> selectAllocatedList(PageRequest request, Long roleId, SysUser searchUser, List<Long> deptIds);

    long deleteUserByIds(Long[] userIds);

    void deleteAllUserInfoByUserId(Long userId);

    SysUser resetUserPwd(Long userId, String newPassword, String salt);
    SysUser resetUserAssetPwd(Long userId, String assertEncodePassword, String salt);

    String selectUserRoleGroup(Long userId);

    String selectUserPostGroup(Long userId);

    String importUser(List<SysUser> userList, boolean updateSupport, String operName);

    void checkAdminModifyAllowed(String loginName, String actionName);

    SysUser registerUser(SysUser user);
}
