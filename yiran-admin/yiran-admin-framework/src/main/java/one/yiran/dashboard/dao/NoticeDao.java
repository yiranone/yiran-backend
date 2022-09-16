package one.yiran.dashboard.dao;

import one.yiran.dashboard.entity.SysNotice;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeDao extends BaseDao<SysNotice, Long> {


    SysNotice findByNoticeId(Long configId);

    int deleteAllByNoticeIdIn(List<Long> delIds);
}
