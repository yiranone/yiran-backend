package com.biz.service;

import com.biz.constants.AccountTypeEnum;
import com.biz.entity.Member;
import com.biz.vo.dto.AssetsDetailDTO;
import com.biz.vo.dto.AssetsQueryParamDTO;
import com.biz.vo.dto.ChargeWithdrawQueryParamDTO;
import com.biz.vo.dto.MemberMoneyDTO;
import com.biz.vo.dto.ChargeWithdrawDetailDTO;
import one.yiran.common.domain.PageModel;
import one.yiran.db.common.service.CrudBaseService;

import java.util.List;

public interface MemberAssetsService extends CrudBaseService<Long, Member> {

    List<MemberMoneyDTO> findMoneyListByMemberId(Long memberId);

    PageModel<AssetsDetailDTO> findAppLists(Long userId, AssetsQueryParamDTO request, AccountTypeEnum accountType);

    void setAddress(Long memberId, String trim);

    PageModel<ChargeWithdrawDetailDTO> findAppChargeWithdrawLists(Long memberId, ChargeWithdrawQueryParamDTO pageRequest);
}
