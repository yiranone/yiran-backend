package com.biz.service;//package com.bid.bidmanage.service;

import com.biz.entity.Member;
import one.yiran.db.common.service.CrudBaseService;

public interface MemberService extends CrudBaseService<Long, Member> {

    Member selectByPhone(Long channelId, String phone);
}
