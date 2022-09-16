package one.yiran.dashboard.dao;

import one.yiran.dashboard.entity.SysPost;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostDao extends BaseDao<SysPost, Long> {


    SysPost findByPostName(String postName);

    SysPost findByPostCode(String postCode);

    SysPost findByPostId(Long postId);

    List<SysPost> findAllByPostIdIn(List<Long> longs);
}
