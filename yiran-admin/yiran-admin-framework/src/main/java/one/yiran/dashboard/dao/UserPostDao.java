package one.yiran.dashboard.dao;

import one.yiran.dashboard.entity.SysUserPost;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPostDao extends BaseDao<SysUserPost, Long> {

    long deleteAllByUserId(Long userId);

    List<SysUserPost> findAllByPostIdInAndUserId(List<Long> postIds, Long userId);
}
