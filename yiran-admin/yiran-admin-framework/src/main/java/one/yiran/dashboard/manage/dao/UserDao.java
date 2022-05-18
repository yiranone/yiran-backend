package one.yiran.dashboard.manage.dao;

import one.yiran.dashboard.manage.entity.SysUser;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDao extends BaseDao<SysUser, Long> {

    SysUser findByUserId(Long userId);

    List<SysUser> findAllByDeptId(Long deptId);

    List<SysUser> findAllByUserIdIn(Long[] userIds);

    SysUser findByLoginName(String loginName);

    List<SysUser> findByPhoneNumber(String phoneNumber);

    SysUser findByOpenId(String openId);

    List<SysUser> findByEmail(String email);
}
