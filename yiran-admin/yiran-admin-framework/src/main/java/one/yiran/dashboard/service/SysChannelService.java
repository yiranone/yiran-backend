package one.yiran.dashboard.service;

import one.yiran.dashboard.entity.SysChannel;
import one.yiran.db.common.service.CrudBaseService;

public interface SysChannelService extends CrudBaseService<Long, SysChannel> {

    boolean checkChannelKeyUnique(SysChannel sysChannel);

    SysChannel selectByChannelCode(String channelCode);
    SysChannel selectByDomainName(String domainName);
    SysChannel selectByChannelIdWithCheck(Long channelId);

    SysChannel create(SysChannel channel);
}
