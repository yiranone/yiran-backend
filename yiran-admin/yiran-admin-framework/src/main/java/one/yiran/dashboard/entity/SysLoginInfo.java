package one.yiran.dashboard.entity;

import one.yiran.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.dashboard.common.annotation.Excel;
import one.yiran.db.common.annotation.Search;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_login_info")
@Data
@Entity
public class SysLoginInfo extends TimedBasedEntity {
    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Excel(name = "序号", cellType = Excel.ColumnType.NUMERIC)
    private Long infoId;

    @Column(updatable = false)
    private Long channelId;

    /**
     * 用户账号
     */
    @Excel(name = "用户账号")
    @Search
    @Column(length = 32)
    private String loginName;

    /**
     * 登录状态 0成功 1失败
     */
    @Excel(name = "登录状态", readConverterExp = "0=成功,1=失败")
    @Search
    @Column(length = 32)
    private String status;

    /**
     * 登录IP地址
     */
    @Excel(name = "登录地址")
    @Search
    @Column(length = 32)
    private String ipAddr;

    /**
     * 登录地点
     */
    @Excel(name = "登录地点")
    @Column(length = 64)
    private String loginLocation;

    /**
     * 浏览器类型
     */
    @Excel(name = "浏览器")
    @Column(length = 32)
    private String browser;

    /**
     * 操作系统
     */
    @Excel(name = "操作系统")
    @Column(length = 32)
    private String os;

    /**
     * 提示消息
     */
    @Excel(name = "提示消息")
    @Column
    private String msg;

    /**
     * 访问时间
     */
    @Excel(name = "访问时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Column
    private Date loginTime;

    /**
     * 登陆用户的id  memberId，userId
     */
    @Column
    private Long categoryId;

    @Excel(name = "User or Member")
    @Column(length = 32)
    private String category;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("infoId", getInfoId())
                .append("loginName", getLoginName())
                .append("ipAddr", getIpAddr())
                .append("loginLocation", getLoginLocation())
                .append("browser", getBrowser())
                .append("os", getOs())
                .append("status", getStatus())
                .append("msg", getMsg())
                .append("loginTime", getLoginTime())
                .toString();
    }
}
