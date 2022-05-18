package one.yiran.dashboard.manage.dao;

import one.yiran.dashboard.manage.entity.SysChannel;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;


@Repository
public interface ChannelDao extends BaseDao<SysChannel, Long> {

    SysChannel findByChannelId(Long channelId);

    SysChannel findByChannelCode(String channelKey);

}
