package one.yiran.dashboard.service.impl;

import com.google.common.collect.Lists;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleTemplate;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import one.yiran.dashboard.common.constants.UserConstants;
import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.dashboard.common.expection.user.UserDeleteException;
import one.yiran.dashboard.dao.RoleDao;
import one.yiran.dashboard.dao.UserDao;
import one.yiran.dashboard.dao.UserPostDao;
import one.yiran.dashboard.dao.UserRoleDao;
import one.yiran.dashboard.entity.*;
import one.yiran.dashboard.vo.UserPageVO;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.expection.user.UserNotFoundException;
import one.yiran.dashboard.service.SysConfigService;
import one.yiran.dashboard.service.SysPostService;
import one.yiran.dashboard.service.SysRoleService;
import one.yiran.dashboard.service.SysUserService;
import one.yiran.dashboard.common.util.MD5Util;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.db.common.util.PredicateBuilder;
import one.yiran.db.common.util.PredicateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SysUserServiceImpl extends CrudBaseServiceImpl<Long,SysUser> implements SysUserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysPostService sysPostService;

    @Autowired
    private UserPostDao userPostDao;

    @Autowired
    private SysConfigService sysConfigService;

    @Override
    public SysUser login(String username, String password) {
        Assert.notNull(username, "用户名不能为空");
        Assert.notNull(password, "密码不能为空");
        SysUser user = userDao.findByLoginName(username);
        if (!StringUtils.equals(password, user.getPassword())) {
            throw new RuntimeException("密码不正确");
        }
        if (user != null)
            user.setPassword(null);
        return user;
    }

    @Override
    public SysUser findUser(Long userId) {
        SysUser u = userDao.findByUserId(userId);
        return u;
    }

    @Override
    public SysUser findUserCheckExist(Long userId) {
        SysUser u = findUser(userId);
        if (u == null) {
            throw new UserNotFoundException();
        }
        if (u.getIsDelete() != null && u.getIsDelete().booleanValue()) {
            throw new UserDeleteException(u.getLoginName());
        }
        return u;
    }

    @Override
    public SysUser findUserByLoginName(String loginName) {
        Assert.notNull(loginName, "用户名不能为空");
        SysUser u = userDao.findByLoginName(loginName);
        return u;
    }

    @Override
    public SysUser findUserByEmail(String email) {
        Assert.notNull(email, "邮箱不能为空");
        List<SysUser> us = userDao.findByEmail(email);
        for (SysUser u : us) {
            if (u.getEmailLoginEnable() != null && u.getEmailLoginEnable().booleanValue())
                return u;
        }
        return null;
    }

    @Override
    public SysUser findUserByPhoneNumber(String phoneNumber) {
        Assert.notNull(phoneNumber, "手机号不能为空");
        List<SysUser> us = userDao.findByPhoneNumber(phoneNumber);
        for (SysUser u : us) {
            if (u.getPhoneNumberLoginEnable() != null && u.getPhoneNumberLoginEnable().booleanValue())
                return u;
        }
        return null;
    }

    @Transactional
    @Override
    public SysUser saveUserAndPerms(SysUser user) throws BusinessException {
        //checkAdminModifyAllowed(user,"操作");
        SysUser db = findUser(user.getUserId());
        if(db != null && db.isAdmin()) {
            List<SysUserRole> userRoleList = userRoleDao.findAllByUserId(db.getUserId());
            List<Long> roleIds = userRoleList.stream().map(t -> t.getRoleId()).collect(Collectors.toList());
            if(user.getRoleIds() == null || !user.getRoleIds().containsAll(roleIds)){
                throw BusinessException.build("超级用户的角色不能修改");
            }
            if(user.getDeptId() == null || user.getDeptId().longValue() != db.getDeptId().longValue()) {
                throw BusinessException.build("超级用户的部门不能修改");
            }
            if(user.getStatus() == null || !user.getStatus().equals(db.getStatus())) {
                throw BusinessException.build("超级用户的状态不能修改");
            }
        }
        saveUser(user);
        doUserPerms(user);
        return user;
    }

    @Transactional
    @Override
    public void saveUserRoles(Long userId, List<Long> roleIds) {
        // 删除用户与角色关联
        userRoleDao.deleteAllByUserId(userId);
        userRoleDao.flush();
        // 新增用户与角色关联
        insertUserRole(userId,roleIds);
    }

    @Transactional
    @Override
    public SysUser recordLoginIp(Long userId, String loginIp) throws BusinessException {
        Assert.notNull(userId, "用户不能为空");
        SysUser dbuser = userDao.findByUserId(userId);
        if(dbuser != null) {
            dbuser.setLoginIp(loginIp);
            dbuser.setLoginDate(new Date());
            dbuser = userDao.save(dbuser);
        }
        return dbuser;
    }

    @Transactional
    @Override
    public SysUser recordLoginFail(Long userId, long passwordErrorCount) throws BusinessException {
        Assert.notNull(userId, "用户不能为空");
        SysUser dbuser = userDao.findByUserId(userId);
        if(dbuser != null) {
            dbuser.setPasswordErrorCount(passwordErrorCount);
            dbuser.setPasswordErrorTime(new Date());
            dbuser = userDao.save(dbuser);
        }
        return dbuser;
    }

    @Transactional
    @Override
    public SysUser resetLoginFail(Long userId) throws BusinessException {
        Assert.notNull(userId, "用户不能为空");
        SysUser dbuser = userDao.findByUserId(userId);
        if(dbuser != null && dbuser.getPasswordErrorTime() != null) {
            log.info("解锁用户{},重置用户登陆错误次数为0",dbuser.getLoginName());
            dbuser.setPasswordErrorCount(0L);
            dbuser.setPasswordErrorTime(null);
            dbuser = userDao.save(dbuser);
        }
        return dbuser;
    }

    @Transactional
    @Override
    public SysUser updateMyInfos(Long userId,String userName,String email,String phoneNumber,String sex) throws BusinessException {
        Assert.notNull(userId, "用户不能为空");
        SysUser dbuser = userDao.findByUserId(userId);
        if(dbuser != null) {
            dbuser.setUserName(userName);
            dbuser.setEmail(email);
            dbuser.setPhoneNumber(phoneNumber);
            dbuser.setSex(sex);
            dbuser = userDao.save(dbuser);
        }
        return dbuser;
    }

    @Transactional
    @Override
    public SysUser updateMyAvatar(Long userId,String avatar) throws BusinessException {
        Assert.notNull(userId, "用户不能为空");
        SysUser dbuser = userDao.findByUserId(userId);
        if(dbuser != null) {
            dbuser.setAvatar(avatar);
            dbuser = userDao.save(dbuser);
        }
        return dbuser;
    }

    @Transactional
    @Override
    public SysUser saveUser(SysUser user) throws BusinessException {
        Assert.notNull(user, "用户不能为空");
        try {
            boolean isNew = true;
            SysUser dbuser;
            if (user.getUserId() != null) {
                dbuser = userDao.findByUserId(user.getUserId());
                if(dbuser != null) {
                    isNew = false;
                    user.setUserId(dbuser.getUserId());
                }
            }
            if (isLoginNameExist(user.getLoginName(),user.getUserId())) {
                throw BusinessException.build(String.format("用户名字[%s]重复", user.getLoginName()));
            }

            if(isNew) {
                return super.insert(user);
            } else {
                return super.update(user);
            }
        } catch (Exception e) {
            log.error("", e);
            if (e instanceof BusinessException)
                throw e;
            throw BusinessException.build("用户保存失败" + e.getMessage(), e);
        }
    }

    @Override
    public List<SysUser> findUsersByUserIds(Long[] ids) {
        Assert.notNull(ids,"");
        return userDao.findAllByUserIdIn(ids);
    }

    @Override
    public PageModel<SysUser> getPage(PageRequest pageRequest, SysUser searchUser) {

        List<Predicate> pres = new ArrayList<>();
        if(StringUtils.isNotBlank(searchUser.getLoginName())){
            pres.add(QSysUser.sysUser.loginName.eq(searchUser.getLoginName()));
        }
        if(StringUtils.isNotBlank(searchUser.getPhoneNumber())){
            pres.add(QSysUser.sysUser.phoneNumber.eq(searchUser.getPhoneNumber()));
        }

        return super.selectPage(pageRequest,searchUser, pres);
    }

    @Override
    public PageModel<UserPageVO> getPageDetail(PageRequest pageRequest, SysUser searchUser, String deptName) {

        QSysUser qUser = QSysUser.sysUser;
        QSysDept qDept = QSysDept.sysDept;
        QSysRole qRole = QSysRole.sysRole;
        QSysUserRole qUserRole = QSysUserRole.sysUserRole;
        SimpleTemplate<String> userRoleNamesTemplate = Expressions.simpleTemplate(String.class, "group_concat({0})", qRole.roleName);
        StringPath userRoleNamesPath = Expressions.stringPath("userRoleNames");
        Predicate[] pres = PredicateBuilder.builder()
                .addExpression(PredicateUtil.buildNotDeletePredicate(qUser))
                .addLikeIfNotBlank(qDept.deptName, deptName)
                .addEqualIfNotBlank(QSysUser.sysUser.loginName, searchUser.getLoginName())
//                .addEqualIfNotBlank(QSysUser.sysUser.phoneNumber, searchUser.getPhoneNumber())
                .addEntityByAnnotation(searchUser,QSysUser.sysUser)
                .toArray();

        JPAQuery<Tuple> q = queryFactory.select(qUser,qDept,userRoleNamesTemplate.as(userRoleNamesPath))
                .from(qUser)
                .leftJoin(qDept).on(qUser.deptId.eq(qDept.deptId))
                .leftJoin(qUserRole).on(qUser.userId.eq(qUserRole.userId))
                .leftJoin(qRole).on(qRole.roleId.eq(qUserRole.roleId))
                .groupBy(qUser.userId)
                .where(pres);

        PageRequestUtil.injectQuery(pageRequest,q,qUser,qDept);

        List<Tuple> ts = q.fetch();
        List<UserPageVO> userPageVOs = new ArrayList<>();
        for (Tuple r : ts) {
            SysUser su = r.get(qUser);
            SysDept sd = r.get(qDept);
            UserPageVO up =  UserPageVO.from(su,sd);
            String userRoleNames = r.get(userRoleNamesPath);
            if(userRoleNames != null)
                up.setRoleNames(Lists.newArrayList(userRoleNames.split(",")));
            userPageVOs.add(up);
        }
        long count = q.fetchCount();
        return PageModel.instance(count,userPageVOs);
    }

    @Override
    public long getListSize(SysUser searchUser, Date bTime, Date eTime) {
        QSysUser qSysUser = QSysUser.sysUser;
        BooleanExpression p = qSysUser.createTime.between(bTime,eTime);
        return super.count(null,searchUser,p);
    }

    @Override
    public boolean isPhoneNumberExist(String phoneNumber,Long userId) {
        Assert.notNull(phoneNumber, "phoneNumber 不能为空");
        userId = userId == null ? 0L : userId;
        SysUser sysUser = findUserByPhoneNumber(phoneNumber);
        if (sysUser != null && !sysUser.getUserId().equals(userId)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isEmailExist(String email,Long userId) {
        Assert.notNull(email, "email 不能为空");
        userId = userId == null ? 0L : userId;
        SysUser sysUser = findUserByEmail(email);
        if (sysUser != null && !sysUser.getUserId().equals(userId)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isLoginNameExist(String loginName,Long userId) {
        Assert.hasLength(loginName, "loginName 不能为空");
        userId = userId == null ? 0L : userId;
        SysUser sysUser = findUserByLoginName(loginName);
        if (sysUser != null && !sysUser.getUserId().equals(userId)) {
            return true;
        }
        return false;
    }

    /**
     * @param roleId
     * @param deptIds 部门用户隔离
     * @return
     */
    @Override
    public PageModel<UserPageVO> selectAllocatedList(PageRequest request, Long roleId, SysUser user, List<Long> deptIds) {
        return doGetUserAllocatedList(request, roleId, user, deptIds, true);
    }

    @Override
    public PageModel<UserPageVO> selectUnallocatedList(PageRequest request, Long roleId, SysUser user, List<Long> deptIds) {
        return doGetUserAllocatedList(request, roleId, user, deptIds, false);
    }

    @Transactional
    @Override
    public long deleteUserByIds(Long[] userIds) throws BusinessException {
        for (Long userId : userIds) {
            deleteAllUserInfoByUserId(userId);
        }
        return userIds.length;
    }

    @Transactional
    @Override
    public void deleteAllUserInfoByUserId(Long userId) {
        SysUser u = userDao.findByUserId(userId);
        if(u == null ){
            throw new UserNotFoundException(userId);
        }
        checkAdminModifyAllowed(u.getLoginName(),"删除");
        //删除用户角色
        int delRoleCount = sysRoleService.deleteAuthUsers(userId);
        //删除用户岗位
        long delPostCount = sysPostService.deleteAuthUsers(userId);
        //逻辑删除用户信息
        super.deleteByPId(userId);
        log.info("逻辑删除用户Id{},删除用户角色数量{}, 删除用户岗位数量{}", userId, delRoleCount, delPostCount);
    }

    /**
     * 查询用户所属角色组
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(Long userId) {
        List<SysRole> list = sysRoleService.selectAllRolesByUserId(userId);
        StringBuffer idsStr = new StringBuffer();
        for (SysRole sysRole : list) {
            if(sysRole.isFlag())
                idsStr.append(sysRole.getRoleName()).append(",");
        }
        if (StringUtils.isNotEmpty(idsStr.toString())) {
            return idsStr.substring(0, idsStr.length() - 1);
        }
        return idsStr.toString();
    }

    /**
     * 查询用户所属岗位组
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public String selectUserPostGroup(Long userId) {
        List<SysPost> list = sysPostService.selectPostsByUserId(userId);
        StringBuffer idsStr = new StringBuffer();
        for (SysPost sysPost : list) {
            if(sysPost.isFlag())
                idsStr.append(sysPost.getPostName()).append(",");
        }
        if (StringUtils.isNotEmpty(idsStr.toString())) {
            return idsStr.substring(0, idsStr.length() - 1);
        }
        return idsStr.toString();
    }

    @Transactional
    @Override
    public SysUser resetUserPwd(Long userId, String newPassword, String salt) {
        Assert.notNull(salt,"");
        SysUser dbUser = findUser(userId);
        if (dbUser == null)
            throw new UserNotFoundException(userId+"");
        if (StringUtils.isBlank(newPassword)) {
            throw BusinessException.build("password不能为空");
        }
        dbUser.setSalt(salt);
        dbUser.setPassword(newPassword);
        dbUser.setPasswordUpdateTime(new Date());
        userDao.save(dbUser);
        return dbUser;
    }

    @Transactional
    @Override
    public SysUser resetUserAssetPwd(Long userId, String assertEncodePassword, String salt) {
        Assert.notNull(salt,"");
        SysUser dbUser = findUser(userId);
        if (dbUser == null)
            throw new UserNotFoundException(userId+"");
        if (StringUtils.isBlank(assertEncodePassword)) {
            throw BusinessException.build("password不能为空");
        }
        dbUser.setAssertSalt(salt);
        dbUser.setAssertPassword(assertEncodePassword);
        dbUser.setAssertPasswordUpdateTime(new Date());
        userDao.save(dbUser);
        return dbUser;
    }

    @Transactional
    @Override
    public String importUser(List<SysUser> userList, boolean isUpdateSupport, String operName) {
        if (userList == null || userList.size() == 0) {
            throw BusinessException.build("导入用户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        String password = sysConfigService.selectConfigByKey("sys.user.initPassword");
        for (SysUser user : userList) {
            try {
                // 验证是否存在这个用户
                SysUser u = userDao.findByLoginName(user.getLoginName());
                checkAdminModifyAllowed(u.getLoginName(),"导入");
                if (u == null) {
                    //user.setUserId(mongoSequenceService.getNextId(SysUser.SEQUENCE_KEY));
                    user.setSalt(Global.getSalt());
                    user.setPassword(MD5Util.encode(Global.getSalt() + password));
                    user.setCreateBy(operName);
                    user.setUpdateBy(operName);
                    user.setIsDelete(false);
                    user = saveUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getLoginName() + " 导入成功");
                } else if (isUpdateSupport) {
                    user.setUpdateBy(operName);
                    user = saveUser(user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、账号 " + user.getLoginName() + " 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、账号 " + user.getLoginName() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、账号 " + user.getLoginName() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw BusinessException.build(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    /**
     * 给用户授予角色
     */
    private void insertUserRole(Long userId, List<Long> roleIds) {
        if (roleIds != null && roleIds.size() > 0) {
            // 新增用户与角色管理
            for (Long roleId : roleIds) {
                if(sysRoleService.selectByPId(roleId) == null){
                    log.info("角色异常，角色[{}]不存在，忽略",roleId);
                    continue;
                    //throw BusinessException.build("角色异常，角色["+roleId+"]不存在");
                }
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                entityManager.persist(ur);
            }
        }
    }

    /**
     * 新增用户岗位信息
     *
     * @param user 用户对象
     */
    private void insertUserPost(SysUser user) {
        List<Long> posts = user.getPostIds();
        if (posts != null && posts.size() > 0) {
            // 新增用户与岗位管理
            for (Long postId : user.getPostIds()) {
                if(sysPostService.selectByPId(postId) == null){
                    throw BusinessException.build("岗位异常，岗位["+postId+"]不存在");
                }
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                entityManager.persist(up);
            }
        }
    }

    private void doUserPerms(SysUser user) {
        Long userId = user.getUserId();
        //如果传递的roleIds为空，表示不修改角色
        if (user.getRoleIds() != null && user.getRoleIds().size() > 0) {
            // 删除用户与角色关联
            userRoleDao.deleteAllByUserId(userId);
            // 新增用户与角色管理
            insertUserRole(user.getUserId(), user.getRoleIds());
        }
        // 删除用户与岗位关联
        userPostDao.deleteAllByUserId(userId);
        // 新增用户与岗位管理
        insertUserPost(user);
    }

    private PageModel<UserPageVO> doGetUserAllocatedList(PageRequest request,
                                                      Long roleId, SysUser user, List<Long> deptIds, boolean inRoles) {

        QSysUser qSysUser = QSysUser.sysUser;
        QSysUserRole qSysUserRole = QSysUserRole.sysUserRole;
        JPAQuery<SysUser> jpa = queryFactory.selectFrom(qSysUser);
//                .innerJoin(qSysDept)
//                .on(qSysUser.deptId.eq(qSysDept.deptId));

        if (inRoles) {
            jpa.where(qSysUser.userId.in(
                    JPAExpressions.select(qSysUserRole.userId).from(qSysUserRole).where(qSysUserRole.roleId.eq(roleId))
            ));
        } else {
            jpa.where(qSysUser.userId.notIn(
                    JPAExpressions.select(qSysUserRole.userId).from(qSysUserRole).where(qSysUserRole.roleId.eq(roleId))
            ));
        }
        if (user != null) {
            if (StringUtils.isNotBlank(user.getLoginName())) {
                jpa.where(qSysUser.loginName.like("%" + user.getLoginName().trim() + "%"));
            }
            if (StringUtils.isNotBlank(user.getUserName())) {
                jpa.where(qSysUser.userName.like("%" + user.getUserName().trim() + "%"));
            }
            if (StringUtils.isNotBlank(user.getPhoneNumber())) {
                jpa.where(qSysUser.phoneNumber.eq(user.getPhoneNumber().trim()));
            }
        }
        jpa.where(qSysUser.isDelete.ne(Boolean.TRUE).or(qSysUser.isDelete.isNull()));
        long count = jpa.fetchCount();

        PageRequestUtil.injectQuery(request,jpa,qSysUser);

//        jpa.offset((request.getPageNum() - 1) * request.getPageSize()).limit(request.getPageSize());
        List<SysUser> sysUserList = jpa.fetch();

        List<UserPageVO> userPageVOs = new ArrayList<>();
        for (SysUser u : sysUserList) {
            userPageVOs.add(UserPageVO.from(u));
        }
        return PageModel.instance(count,userPageVOs);
    }

    @Override
    public void checkAdminModifyAllowed(String loginName, String actionName) {
        if (!Global.isDebugMode() && Global.isAdmin(loginName)) {
            throw BusinessException.build("不允许" + StringUtils.defaultIfBlank(actionName,"操作") + "超级管理员用户" + StringUtils.defaultIfBlank(loginName,""));
        }
    }

    @Override
    public SysUser registerUser(SysUser user) {
        user.setUserType(UserConstants.REGISTER_USER_TYPE);
        return userDao.save(user);
    }
}
