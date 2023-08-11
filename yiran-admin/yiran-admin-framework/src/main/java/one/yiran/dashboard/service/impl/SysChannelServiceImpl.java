package one.yiran.dashboard.service.impl;

import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.util.SpringUtil;
import one.yiran.dashboard.dao.ChannelDao;
import one.yiran.dashboard.entity.QSysChannel;
import one.yiran.dashboard.entity.SysChannel;
import one.yiran.dashboard.service.SysChannelHandleService;
import one.yiran.dashboard.service.SysChannelService;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class SysChannelServiceImpl extends CrudBaseServiceImpl<Long, SysChannel> implements SysChannelService {

    private final ChannelDao channelDao;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

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

    @Override
    public SysChannel selectByChannelCode(String channelCode) {
        Assert.hasLength(channelCode,"channelCode不能为空");
        return selectOne(QSysChannel.sysChannel.channelCode.eq(channelCode));
    }

    @Override
    public SysChannel selectByDomainName(String domainName) {
        Assert.hasLength(domainName,"domainName不能为空");
        return selectOne(QSysChannel.sysChannel.domainName.eq(domainName));
    }

    @Override
    public SysChannel selectByChannelIdWithCheck(Long channelId) {
        Assert.notNull(channelId,"channelId不能为空");
        SysChannel channel = selectOne(QSysChannel.sysChannel.channelId.eq(channelId));
        if(channel ==null) {
            throw BusinessException.build("渠道不存在:channelId=" + channelId);
        }
        return channel;
    }

    @Override
    @Transactional
    public SysChannel create(SysChannel channel) {
        SysChannel db = this.insert(channel);
        SysChannelHandleService service = SpringUtil.getBean(SysChannelHandleService.class);
        service.initChannelData(db.getChannelId());
        return db;
    }
}
