package one.yiran.dashboard.manage.service.impl;

import one.yiran.dashboard.manage.dao.ChannelDao;
import one.yiran.dashboard.manage.entity.SysChannel;
import one.yiran.dashboard.manage.service.SysChannelService;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysChannelServiceImpl extends CrudBaseServiceImpl<Long, SysChannel> implements SysChannelService {

    private final ChannelDao channelDao;

    public SysChannelServiceImpl(ChannelDao channelDao) {
        this.channelDao = channelDao;
    }

    @Override
    public boolean checkChannelKeyUnique(SysChannel sysChannel) {
        Long channelId = sysChannel.getChannelId() == null ? -1L : sysChannel.getChannelId();
        SysChannel info = channelDao.findByChannelCode(sysChannel.getChannelCode());
        if (info != null && info.getChannelId().longValue() != channelId.longValue()) {
            return false;
        }
        return true;
    }
}
