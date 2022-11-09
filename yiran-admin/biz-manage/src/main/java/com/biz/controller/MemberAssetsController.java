package com.biz.controller;

import com.biz.constants.AccountTypeEnum;
import com.biz.service.MemberAssetsService;
import com.biz.vo.dto.AssetsDetailDTO;
import com.biz.vo.dto.AssetsQueryParamDTO;
import com.biz.vo.dto.ChargeWithdrawQueryParamDTO;
import com.biz.vo.dto.MemberMoneyDTO;
import com.biz.vo.dto.ChargeWithdrawDetailDTO;
import lombok.extern.slf4j.Slf4j;
import one.yiran.common.domain.PageModel;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiChannel;
import one.yiran.dashboard.common.annotation.ApiMember;
import one.yiran.dashboard.common.annotation.ApiObject;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.dashboard.common.annotation.RequireMemberLogin;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.model.MemberSession;
import one.yiran.dashboard.util.MemberCacheUtil;
import one.yiran.dashboard.vo.ChannelVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@AjaxWrapper
@RequestMapping(value = "/ext/member/assets")
@Slf4j
public class MemberAssetsController {

    @Autowired
    private MemberAssetsService memberAssetsService;

    @PostMapping("/setAddress")
    @RequireMemberLogin
    public void setAddress(@ApiParam(required = true) String address,
                           HttpServletRequest request) {
        MemberSession session = MemberCacheUtil.getSessionInfo(request);
        Long memberId = session.getMemberId();
        memberAssetsService.setAddress(memberId,address.trim());
    }

    @RequestMapping("/getRechargeInfo")
    public Map<String,String> getRechargeAddress(@ApiChannel ChannelVO channelVO, HttpServletRequest request){
        Map<String,String> map = new HashMap<>();
        map.put("rechargeAddress", Global.getConfig("dashboard.pay.rechargeAddress"));
        return map;
    }

    @PostMapping("/money/list")
    @RequireMemberLogin
    public List<MemberMoneyDTO> moneyList(HttpServletRequest request) {
        MemberSession session = MemberCacheUtil.getSessionInfo(request);
        Long memberId = session.getMemberId();
        return memberAssetsService.findMoneyListByMemberId(memberId);
    }

    @PostMapping("/money/detail")
    @RequireMemberLogin
    public PageModel<AssetsDetailDTO> moneyReturnDetail(@ApiObject(createIfNull = true) AssetsQueryParamDTO pageRequest, HttpServletRequest request) {
        MemberSession session = MemberCacheUtil.getSessionInfo(request);
        Long memberId = session.getMemberId();
        return memberAssetsService.findAppLists(memberId, pageRequest, AccountTypeEnum.MONEY);
    }

    @PostMapping("/score/detail")
    @RequireMemberLogin
    public PageModel<AssetsDetailDTO> scoreReturnDetail(@ApiObject(createIfNull = true) AssetsQueryParamDTO pageRequest, HttpServletRequest request) {
        MemberSession session = MemberCacheUtil.getSessionInfo(request);
        Long memberId = session.getMemberId();
        return memberAssetsService.findAppLists(memberId, pageRequest, AccountTypeEnum.SCORE);
    }

//    @PostMapping("/money/chargeList")
//    @RequireMemberLogin
//    public PageModel<ChargeWithdrawDetailDTO> moneyChargeList(@ApiObject(createIfNull = true) AssetsQueryParamDTO pageRequest, HttpServletRequest request) {
//        MemberSession session = MemberCacheUtil.getSessionInfo(request);
//        Long memberId = session.getMemberId();
//        return memberAssetsService.findAppChargeWithdrawLists(memberId, pageRequest);
//    }
//
//    @PostMapping("/money/withdrawList")
//    @RequireMemberLogin
//    public PageModel<ChargeWithdrawDetailDTO> withdrawList(@ApiObject(createIfNull = true) AssetsQueryParamDTO pageRequest, HttpServletRequest request) {
//        MemberSession session = MemberCacheUtil.getSessionInfo(request);
//        Long memberId = session.getMemberId();
//        return memberAssetsService.findAppChargeWithdrawLists(memberId, pageRequest);
//    }

    @PostMapping("/money/chargeWithdrawList")
    @RequireMemberLogin
    public PageModel<ChargeWithdrawDetailDTO> chargeWithdrawList(
            @ApiObject(createIfNull = true) ChargeWithdrawQueryParamDTO pageRequest, HttpServletRequest request) {
        MemberSession session = MemberCacheUtil.getSessionInfo(request);
        Long memberId = session.getMemberId();

        return memberAssetsService.findAppChargeWithdrawLists(memberId, pageRequest);
    }

}
