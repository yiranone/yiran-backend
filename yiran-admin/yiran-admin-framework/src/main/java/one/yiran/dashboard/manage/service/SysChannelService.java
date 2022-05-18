package one.yiran.dashboard.manage.service;

import one.yiran.dashboard.manage.entity.SysChannel;
import one.yiran.db.common.service.CrudBaseService;

public interface SysChannelService extends CrudBaseService<Long, SysChannel> {

    boolean checkChannelKeyUnique(SysChannel sysChannel);
}
