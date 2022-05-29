package one.yiran.dashboard.web.controller.admin;

import com.alibaba.fastjson.JSONObject;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.dashboard.common.annotation.RequireUserLogin;
import one.yiran.dashboard.common.expection.user.UserHasNotPermissionException;
import one.yiran.dashboard.common.expection.user.UserNotFoundException;
import one.yiran.dashboard.common.util.UserCacheUtil;
import one.yiran.dashboard.manage.dao.RoleDao;
import one.yiran.dashboard.manage.dao.UserDao;
import one.yiran.dashboard.manage.dao.UserRoleDao;
import one.yiran.dashboard.manage.entity.SysRole;
import one.yiran.dashboard.manage.entity.SysUserRole;
import one.yiran.dashboard.manage.service.SysConfigService;
import one.yiran.dashboard.manage.service.SysPostService;
import one.yiran.dashboard.manage.service.SysRoleService;
import one.yiran.dashboard.manage.service.SysUserService;
import one.yiran.dashboard.vo.UserPageVO;
import one.yiran.dashboard.web.service.ThirdPartyService;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.dashboard.common.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.manage.entity.SysUser;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.manage.security.config.PermissionConstants;
import one.yiran.dashboard.manage.security.service.PasswordService;
import one.yiran.dashboard.common.util.ExcelUtil;
import one.yiran.dashboard.manage.security.UserInfoContextHelper;
import one.yiran.dashboard.common.util.UserConvertUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import one.yiran.dashboard.common.annotation.RequirePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/system/user")
@AjaxWrapper
public class UserAdminController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysPostService sysPostService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private ThirdPartyService thirdPartyService;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @RequirePermission(PermissionConstants.User.VIEW)
    @RequestMapping("/list")
    public PageModel<UserPageVO> list(@RequestBody SysUser sysUser, @ApiParam String deptName, HttpServletRequest request) {
        PageRequest pageRequest = PageRequestUtil.fromRequest(request);
        PageModel pe = sysUserService.getPageDetail(pageRequest, sysUser, deptName );
        return pe;
    }

    @Log(title = "用户管理", businessType = BusinessType.ADD)
    @RequirePermission(PermissionConstants.User.ADD)
    @PostMapping("/add")
    public UserInfo addUser(@Valid @RequestBody SysUser user) {
//        UserInfoContextHelper.getLoginUser().checkScopePermission(PermissionConstants.User.ADD,user.getDeptId());
        checkUserFields(user);
        SysUser dbUser = new SysUser();
        if (StringUtils.isBlank(user.getPassword())) {
            throw BusinessException.build("密码不能为空");
        } else {
            dbUser.setSalt(Global.getSalt());
            dbUser.setPassword(passwordService.encryptPassword(user.getPassword(), Global.getSalt()));
            dbUser.setPasswordUpdateTime(new Date());
        }

        dbUser.setStatus(user.getStatus());
        dbUser.setIsDelete(false);
        dbUser.setPhoneNumber(user.getPhoneNumber());
        dbUser.setEmail(user.getEmail());
        dbUser.setUserName(user.getUserName());
        dbUser.setLoginName(user.getLoginName());
        dbUser.setSex(user.getSex());
        dbUser.setPostIds(user.getPostIds());
//        if (user.getDeptId() != null) {
//            SysDept sysDept = sysDeptService.selectDeptByDeptId(user.getDeptId());
//            dbUser.setDeptId(user.getDeptId());
//            dbUser.setDeptName(sysDept.getDeptName());
//        }
        dbUser.setRoleIds(user.getRoleIds());
        dbUser.setRemark(user.getRemark());

        dbUser.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
        dbUser.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        return UserConvertUtil.convert(sysUserService.saveUserAndPerms(dbUser));
    }

    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @RequirePermission(PermissionConstants.User.EDIT)
    @PostMapping("edit")
    public UserInfo editUser(@Valid @RequestBody SysUser user) {
//        UserInfoContextHelper.checkScopePermission(PermissionConstants.User.EDIT,user.getDeptId());
        if (user == null) {
            throw BusinessException.build("user不能为空");
        }
        if (user.getUserId() == null) {
            throw BusinessException.build("用户ID不能为空");
        }
        checkUserFields(user);
        //修改
        SysUser dbUser = sysUserService.findUser(user.getUserId());
        if (dbUser == null) {
            throw BusinessException.build("用户不存在");
        }
        dbUser.setStatus(user.getStatus());
        dbUser.setAvatar(user.getAvatar());
        dbUser.setPhoneNumber(user.getPhoneNumber());
        dbUser.setEmail(user.getEmail());
        dbUser.setUserName(user.getUserName());
        dbUser.setSex(user.getSex());
        dbUser.setRoleIds(user.getRoleIds());
        dbUser.setPostIds(user.getPostIds());
//        if (user.getDeptId() != null) {
//            dbUser.setDeptId(user.getDeptId());
//            SysDept sysDept = sysDeptService.selectDeptByDeptId(user.getDeptId());
//            dbUser.setDeptName(sysDept.getDeptName());
//        }

//        if(UserInfoContextHelper.getLoginUser().hashScopePermission(PermissionConstants.User.ROLE,user.getDeptId())) {
//            List<Long> toSaveRoleIds = filterRoles(user.getUserId(), user.getRoleIds() == null ? new ArrayList<>() : Arrays.asList(user.getRoleIds()));
//            dbUser.setRoleIds(toSaveRoleIds.toArray(new Long[]{}));
//        }

        dbUser.setRemark(user.getRemark());
        dbUser.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());

        sysUserService.checkUserAllowed(user, "修改");
        return UserConvertUtil.convert(sysUserService.saveUserAndPerms(dbUser));
    }

    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @RequirePermission(PermissionConstants.User.REMOVE)
    @PostMapping("/remove")
    public long remove(@ApiParam(required = true) Long[] userIds) {
        return sysUserService.deleteUserByIds(userIds);
    }

    @RequirePermission(PermissionConstants.User.VIEW)
    @PostMapping("/detail")
    public UserPageVO detail(@ApiParam(required = true) Long userId) {
        SysUser user =  sysUserService.findUser(userId);
        if (user == null)
            throw new UserNotFoundException();
        List<SysUserRole> userRoleList = userRoleDao.findAllByUserId(user.getUserId());
        List<Long> roleIds = userRoleList.stream().map(t -> t.getRoleId()).collect(Collectors.toList());
        user.setRoleIds(roleIds);
        UserPageVO vo = UserPageVO.from(user);
        vo.setRoleIds(roleIds);
        return vo;
    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @RequirePermission(PermissionConstants.User.EXPORT)
    @PostMapping("/export")
    public String export(HttpServletRequest request, SysUser user) {
        user.setIsDelete(false);
        PageModel<SysUser> list = sysUserService.getPage(PageRequestUtil.fromRequestIgnorePageSize(request), user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        return util.exportExcel(list.getRows(), "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @RequirePermission(PermissionConstants.User.IMPORT)
    @PostMapping("/importData")
    public String importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<SysUser> util = new ExcelUtil<>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String operName = UserInfoContextHelper.getCurrentLoginName();
        String message = sysUserService.importUser(userList, updateSupport, operName);
        return message;
    }

    @RequirePermission(PermissionConstants.User.VIEW)
    @GetMapping("/importTemplate")
    public String importTemplate() {
        ExcelUtil<SysUser> util = new ExcelUtil<>(SysUser.class);
        return util.importTemplateExcel("用户数据");
    }


    @RequirePermission(PermissionConstants.User.RESET_PWD)
    @Log(title = "重置密码", businessType = BusinessType.UPDATE)
    @PostMapping("/resetPwd")
    public void resetPwd(@ApiParam(required = true) Long userId,
                         @ApiParam(required = true) String password) {
        SysUser dbUser = sysUserService.findUser(userId);
        if (dbUser == null)
            throw new UserNotFoundException();
        UserInfoContextHelper.checkScopePermission(PermissionConstants.User.RESET_PWD, dbUser.getDeptId());
        //设置新密码
        dbUser.setSalt(Global.getSalt());
        dbUser.setPassword(passwordService.encryptPassword(password, dbUser.getSalt()));
        //解锁账户
        sysUserService.resetLoginFail(dbUser.getUserId());

        dbUser.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
        sysUserService.checkUserAllowed(dbUser, "重置");
        sysUserService.resetUserPwd(dbUser.getUserId(), dbUser.getPassword(), dbUser.getSalt());
    }

    /**
     * 用户授权角色
     */
    @RequirePermission("system:user:role")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PostMapping("/authRole/insertAuthRole")
    public void insertAuthRole(Long userId, Long[] roleIds) {
        if (roleIds != null && !Arrays.asList(roleIds).contains(1L))
            sysUserService.checkUserAllowed(new SysUser(userId), "取消授权");
        SysUser user = sysUserService.findUser(userId);
        boolean hasUserDept = UserInfoContextHelper.getLoginUser().hashScopePermission(PermissionConstants.User.ROLE, user.getDeptId());
        if (!hasUserDept)
            throw UserHasNotPermissionException.buildWithPermission(PermissionConstants.User.ROLE);

        List<Long> toSaveRoleIds = filterRoles(userId, Arrays.asList(roleIds));

        sysUserService.saveUserRoles(userId, toSaveRoleIds);
    }

    /**
     * 用户状态修改
     */
    @RequirePermission(PermissionConstants.User.EDIT)
    @Log(title = "状态修改", businessType = BusinessType.UPDATE)
    @PostMapping("/changeStatus")
    public void changeStatus(SysUser user) {
        sysUserService.checkUserAllowed(user, "修改状态");
        SysUser u = sysUserService.findUser(user.getUserId());
        UserInfoContextHelper.checkScopePermission(PermissionConstants.User.EDIT, u.getDeptId());
        u.setStatus(user.getStatus());
        sysUserService.saveUser(u);
    }

    @RequirePermission(PermissionConstants.User.UNLOCK)
    @Log(title = "账户解锁", businessType = BusinessType.OTHER)
    @PostMapping("/unlock")
    public void unlock(Long userId) {
        sysUserService.resetLoginFail(userId);
    }

    @RequireUserLogin
    @PostMapping("/sendSms")
    public Map sendSms(HttpServletRequest request) {
        UserInfo user = UserCacheUtil.getSessionInfo(request);
        if (StringUtils.isBlank(user.getPhoneNumber())) {
            throw BusinessException.build("未设置手机号");
        }
        String randomNumber = RandomStringUtils.randomNumeric(6);
        log.info("用户{}发送短信验证码:{}",user.getPhoneNumber(),randomNumber);
        thirdPartyService.sendSms(user.getPhoneNumber(), new JSONObject() {{
            put("code", randomNumber);
        }}, false);

        String randomToken = RandomStringUtils.randomNumeric(32) + "_" + user.getPhoneNumber();
        UserCacheUtil.setSmsInfo(randomToken, randomNumber);
        return new HashMap() {{
            put("token", randomToken);
        }};
    }

    /**
     * 校验用户名是否重复，
     */
    @PostMapping("/isLoginNameExist")
    public boolean isLoginNameExist(SysUser user) {
        return sysUserService.isLoginNameExist(user);
    }

    /**
     * 校验手机号码是否重复
     */
    @PostMapping("/isPhoneNumberExist")
    public boolean isPhoneNumberExist(SysUser user) {
        return sysUserService.isPhoneNumberExist(user);
    }

    /**
     * 校验email邮箱是否重复
     */
    @PostMapping("/isEmailExist")
    public boolean isEmailExist(SysUser user) {
        return sysUserService.isEmailExist(user);
    }

    private void checkUserFields(SysUser user){
        if (StringUtils.isBlank(user.getLoginName())) {
            throw BusinessException.build("登陆名称不能为空");
        }
        if (sysUserService.isLoginNameExist(user)) {
            throw BusinessException.build("用户名字:"+user.getLoginName()+"已经存在");
        }
        if (sysUserService.isPhoneNumberExist(user)) {
            throw BusinessException.build("手机号:"+user.getPhoneNumber()+"已经存在");
        }
    }

    /**
     * 有 PermissionConstants.User.ROLE 权限才能给其他人
     *
     * @param userId
     * @param roleIds
     * @return
     */
    private List<Long> filterRoles(Long userId, List<Long> roleIds) {
        //这里不能让用户保存的时候选择太多权限
        //这个用户以前就有的权限
        List<Long> dbRoles = sysRoleService.selectRolesByUserId(userId).stream().map(e -> e.getRoleId()).collect(Collectors.toList());
        //给客户端操作的角色
        List<SysRole> toPageRoles = sysRoleService.selectAllVisibleRolesByUserId(UserInfoContextHelper.getCurrentUserId(), userId);

        //除去给客户端展示的，剩下保持不变
        dbRoles.removeAll(toPageRoles.stream().map(e -> e.getRoleId()).collect(Collectors.toList()));
        //给客户端的检查有没有勾选
        if (roleIds != null && roleIds.size() > 0) {
            List<SysRole> requestRoles = sysRoleService.selectRolesByRoleIds(roleIds.toArray(new Long[]{}));
            List<Long> pageRoleIds = toPageRoles.stream().map(e -> e.getRoleId()).collect(Collectors.toList());
            //判断用户有没有添加新的角色
            requestRoles.forEach(e -> {
                //防止越权
                if (pageRoleIds.contains(e.getRoleId())) {
                    dbRoles.add(e.getRoleId());
                }
            });
        }
        return dbRoles;
    }
}
