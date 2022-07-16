package one.yiran.dashboard.manage.entity;

import com.alibaba.fastjson.annotation.JSONField;
import one.yiran.dashboard.common.annotation.Option;
import one.yiran.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.dashboard.common.annotation.Excel;
import one.yiran.db.common.annotation.Search;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_user",indexes = {
        @Index(name = "idx_userName",columnList = "userName"),
        @Index(name = "idx_phoneNumber",columnList = "phoneNumber")
})
@Entity
@Data
public class SysUser extends TimedBasedEntity {

    @Search
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Excel(name = "用户序号", prompt = "用户编号", cellType = Excel.ColumnType.NUMERIC)
    @Column
    private Long userId;

    @Excel(name = "登录名称")
    @Search(op = Search.Op.REGEX)
    @NotBlank(message = "登录账号不能为空")
    @Size(min = 0, max = 30, message = "登录账号长度不能大于30个字符")
    @Column
    private String loginName;

    @Excel(name = "用户名称")
    @Search(op = Search.Op.REGEX)
    @Size(min = 0, max = 30, message = "用户昵称长度不能超过30个字符")
    @Column
    private String userName;

    @Column
    private String openId;

    /** 用户类型 */
    @Column
    private String userType;

    @Excel(name = "手机号码")
    @Search
    @Size(min = 0, max = 11, message = "手机号码长度不能超过11个字符")
    @Column
    private String phoneNumber;

    @Column
    private Boolean phoneNumberLoginEnable;

    @Excel(name = "用户邮箱")
    @Search
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    @Column
    private String email;

    @Column
    private Boolean emailLoginEnable;

    /**
     * 用户性别
     */
    @Excel(name = "用户性别", readConverterExp = "0=男,1=女,2=未知")
    @Search
    @Column(length = 4)
    private String sex;

    @Column(length = 255)
    private String avatar;

    @JSONField(serialize = false)
    @Column(length = 32)
    private String password;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Column
    private Date passwordUpdateTime;
    /**
     * 盐加密
     */
    @Column(nullable = false,length = 32)
    private String salt;

    @JSONField(serialize = false)
    @Column(length = 32)
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
     * 帐号状态（0正常 1停用）
     */
    @Option(value = {"0","1"}, message = "状态只能是0，1; 0=正常,1=停用")
    @Excel(name = "帐号状态", readConverterExp = "0=正常,1=停用")
    @Search
    @Column(length = 8,nullable = false)
    private String status;

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

    /**
     * 部门ID
     */
    @Excel(name = "部门编号", type = Excel.Type.ALL)
    //@Search
    @Column
    private Long deptId;

    /**
     * 部门父ID
     */
    @Transient
    private Long parentId;

    @Column
    private Long passwordErrorCount;
    @Column
    private Date passwordErrorTime;

    @NotNull(message = "渠道号不能为空")
    @Column(nullable = false,updatable = false)
    private Long channelId;
    /**
     * 角色集合
     */
    @Transient
    private List<SysRole> sysRoles;

    /**
     * 角色组
     */
    @Transient
    private List<Long> roleIds;

    /**
     * 岗位组
     */
    @Transient
    private List<Long> postIds;

    public SysUser(){
    }


    public SysUser(Long userId){
        this.userId = userId;
    }

    public boolean isAdmin() {
        return isAdmin(this.userId);
    }

    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }
}
