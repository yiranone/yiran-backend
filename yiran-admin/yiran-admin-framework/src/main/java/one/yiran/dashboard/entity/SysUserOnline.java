package one.yiran.dashboard.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.db.common.annotation.Search;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_user_online")
@Entity
@Data
public class SysUserOnline implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户会话id
     */
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 64)
    private String sessionId;

    /**
     * 部门名称
     */
    @Search
    @Column(length = 64)
    private String deptName;

    @Search
    @Column(length = 64)
    private String channelName;
    /**
     * 登录名称
     */
    @Search(op = Search.Op.REGEX)
    @Column(length = 64)
    private String loginName;

    /**
     * 登录IP地址
     */
    @Search(op = Search.Op.REGEX)
    @Column(length = 64)
    private String ipAddr;

    /**
     * 登录地址
     */
    @Search(op = Search.Op.REGEX)
    @Column(length = 128)
    private String loginLocation;

    /**
     * 浏览器类型
     */
    @Column(length = 64)
    private String browser;

    /**
     * 操作系统
     */
    @Column(length = 64)
    private String os;

    /**
     * session创建时间
     */
    @Column
    private Date startTimestamp;

    /**
     * session最后访问时间
     */
    @Column
    private Date lastAccessTime;

    /**
     * 超时时间，单位为毫秒
     */
    @Column
    private Long expireTime;

    /**
     * 在线状态
     */
    @Column
    private OnlineStatus status = OnlineStatus.on_line;


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("sessionId", getSessionId())
                .append("loginName", getLoginName())
                .append("deptName", getDeptName())
                .append("ipAddr", getIpAddr())
                .append("loginLocation", getLoginLocation())
                .append("browser", getBrowser())
                .append("os", getOs())
                .append("status", getStatus())
                .append("startTimestamp", getStartTimestamp())
                .append("lastAccessTime", getLastAccessTime())
                .append("expireTime", getExpireTime())
                .toString();
    }

    public enum OnlineStatus {
        /**
         * 用户状态
         */
        on_line("在线"), off_line("离线");
        private final String info;

        OnlineStatus(String info) {
            this.info = info;
        }

        public String getInfo() {
            return info;
        }
    }
}
