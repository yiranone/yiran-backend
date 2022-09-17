package com.biz.controller;

import com.biz.entity.Member;
import com.biz.service.MemberService;
import com.biz.service.util.MemberPasswordService;
import com.biz.vo.MemberVO;
import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.*;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.expection.user.UserNotFoundException;
import one.yiran.dashboard.entity.SysChannel;
import one.yiran.dashboard.security.SessionContextHelper;
import one.yiran.dashboard.security.config.PermissionConstants;
import one.yiran.dashboard.service.SysChannelService;
import one.yiran.dashboard.common.util.WrapUtil;
import one.yiran.db.common.util.PageRequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;


@AjaxWrapper
@Controller
@RequestMapping("/ext/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private SysChannelService channelService;

    @Autowired
    private MemberPasswordService passwordService;

    @RequirePermission(PermissionConstants.Member.VIEW)
    @RequestMapping("/list")
    public PageModel<MemberVO> list(@ApiObject(createIfNull = true) MemberVO memberVO,
                                    @ApiParam String deptName, HttpServletRequest request) {
        PageRequest pageRequest = PageRequestUtil.fromRequest(request);
        Long channelId = SessionContextHelper.getChannelId();
        memberVO.setIsDelete(false);
        PageModel pe = memberService.selectPageDetail(pageRequest, memberVO, channelId);
        return pe;
    }

    @Log(title = "会员管理", businessType = BusinessType.ADD)
    @RequirePermission(PermissionConstants.User.ADD)
    @PostMapping("/add")
    public MemberVO addMember(@ApiObject(validate = true) MemberVO member,
                              @ApiParam String password) {
        Long channelId = SessionContextHelper.getChannelIdWithCheck();
        Member db = new Member();
        if (StringUtils.isBlank(password)) {
            throw BusinessException.build("密码不能为空");
        } else {
            db.setSalt(Global.getSalt());
            db.setPassword(passwordService.encryptPassword(password, Global.getSalt()));
            db.setPasswordUpdateTime(new Date());
        }

        db.setStatus(member.getStatus());
        db.setIsDelete(false);
        db.setPhone(member.getPhone());
        db.setName(member.getName());

        db.setCreateBy(SessionContextHelper.getCurrentLoginName());
        db.setUpdateBy(SessionContextHelper.getCurrentLoginName());

        SysChannel channel = channelService.selectByPId(channelId);
        db.setChannelId(channelId);
        if(memberService.selectByPhone(db.getChannelId(),db.getPhone()) != null ){
            throw BusinessException.build("手机号已经存在");
        }
        db = memberService.insert(db);

        return MemberVO.from(db,channel);
    }

    @Log(title = "会员管理", businessType = BusinessType.EDIT)
    @RequirePermission(PermissionConstants.User.EDIT)
    @PostMapping("edit")
    public MemberVO editUser(@ApiObject(validate = true) MemberVO member,
                             @ApiParam String password) {
        if (member == null) {
            throw BusinessException.build("member不能为空");
        }
        if (member.getMemberId() == null) {
            throw BusinessException.build("会员ID不能为空");
        }
        //修改
        Member db = memberService.selectByPId(member.getMemberId());
        if (db == null) {
            throw BusinessException.build("会员不存在");
        }
        db.setStatus(member.getStatus());
        db.setStatus(member.getStatus());
        db.setPhone(member.getPhone());
        db.setName(member.getName());

        db.setUpdateBy(SessionContextHelper.getCurrentLoginName());

        Member check = memberService.selectByPhone(db.getChannelId(),db.getPhone());
        if(check != null && !check.getMemberId().equals(db.getMemberId()))
            throw BusinessException.build("手机号已经存在");

        db = memberService.update(db);
        SysChannel channel = channelService.selectByPId(db.getChannelId());

        return MemberVO.from(db,channel);
    }

    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @RequirePermission(PermissionConstants.User.REMOVE)
    @PostMapping("/remove")
    public Map<String, Object> remove(@ApiParam(required = true) Long[] ids) {
        return WrapUtil.wrap("deleteCount",memberService.deleteByPIds(ids));
    }

    @RequirePermission(PermissionConstants.Member.VIEW)
    @PostMapping("/detail")
    public MemberVO detail(@ApiParam(required = true) Long memberId) {
        Member db =  memberService.selectByPId(memberId);
        if (db == null)
            throw new UserNotFoundException();

        SysChannel channel = channelService.selectByPId(db.getChannelId());
        return MemberVO.from(db,channel);
    }

    @Log(title = "会员管理", businessType = BusinessType.EDIT)
    @RequirePermission(PermissionConstants.User.EDIT)
    @PostMapping("resetPassword")
    public MemberVO resetPassword(@ApiParam(required = true) Long memberId,
                             @ApiParam(required = true) String password) {
        Member db = memberService.selectByPId(memberId);
        if (db == null) {
            throw BusinessException.build("会员不存在");
        }
        db.setSalt(Global.getSalt());
        db.setPassword(passwordService.encryptPassword(password, Global.getSalt()));
        db.setPasswordUpdateTime(new Date());

        db.setUpdateBy(SessionContextHelper.getCurrentLoginName());
        db = memberService.update(db);
        SysChannel channel = channelService.selectByPId(db.getChannelId());

        return MemberVO.from(db,channel);
    }

}
