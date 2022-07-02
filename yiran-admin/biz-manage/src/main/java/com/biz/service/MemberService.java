package com.biz.service;

import com.biz.entity.Member;
import com.biz.vo.MemberVO;
import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.vo.ChannelVO;
import one.yiran.db.common.service.CrudBaseService;

public interface MemberService extends CrudBaseService<Long, Member> {

    Member selectByPhone(Long channelId, String phone);

    PageModel<MemberVO> selectPageDetail(PageRequest pageRequest, MemberVO m, Long channelId);

    Member recordLoginFail(Long memberId, long passwordErrorCount) throws BusinessException;
    Member resetLoginFail(Long memberId) throws BusinessException;
}
