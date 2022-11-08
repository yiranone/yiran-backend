package com.biz.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.dashboard.common.annotation.Excel;
import one.yiran.dashboard.common.annotation.Option;
import one.yiran.db.common.annotation.Search;
import one.yiran.db.common.domain.TimedBasedEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Table(name = "ext_member",indexes = {
        @Index(name = "idx_pho_chid", columnList = "phone,channelId", unique = true),
        @Index(name = "idx_phone", columnList = "phone", unique = false)
})
@Entity
@Data
public class Member extends TimedBasedEntity {

    @Excel(name = "会员ID", cellType = Excel.ColumnType.NUMERIC)
    @Search
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long memberId;

    @Column(length = 6)
    private String memberCode;

    @Search
    //@NotBlank(message = "登陆名不能为空")
    @Column(length = 18)
    private String loginName;

    @Search
    //@NotBlank(message = "邮箱不能为空")
    @Column(length = 18)
    private String email;

    @Column(length = 4)
    private String countryCode;

    @Search
    @NotBlank(message = "手机号不能为空")
    @Column(length = 13,nullable = false)
    private String phone;

    @Option(value = {"1","2"},message = "状态只能是1，2。 1=正常,2=停用")
    @Excel(name = "帐号状态", readConverterExp = "1=正常,2=停用")
    @Search
    @Column(length = 8,nullable = false)
    private String status;

    @Column(length = 32)
    private String name;

    @Column(length = 32)
    private String nickName;


    @Column(length = 256)
    private String avatar;

    @JSONField(serialize = false)
    @Column
    private String password;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column
    private Date passwordUpdateTime;

    @Column
    private Long passwordErrorCount;
    @Column
    private Date passwordErrorTime;

    @JSONField(serialize = false)
    @Column
    private String assertPassword;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column
    private Date assertPasswordUpdateTime;
    /**
     * 盐加密
     */
    @Column(nullable = true,length = 32)
    private String assertSalt;

    /**
     * 盐加密
     */
    @Column(nullable = false,length = 32)
    private String salt;

    /**
     * 最后登陆IP
     */
    @Excel(name = "最后登陆IP", type = Excel.Type.EXPORT)
    @Column(length = 64)
    private String loginIp;

    /**
     * 最后登陆时间
     */
    @Excel(name = "最后登陆时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Excel.Type.EXPORT)
    @Column
    private Date loginDate;

    @Search
    @NotNull(message = "渠道不能为空")
    @Column(nullable = false)
    private Long channelId;

    @Column
    private Long inviteMemberId;

    @Column(length = 32)
    private String inviteMemberNickName; //上级用户昵称

    @Column(length = 32)
    private String registerChannel; //注册来源
}
