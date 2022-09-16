package one.yiran.dashboard.entity;

import one.yiran.dashboard.common.annotation.Option;
import one.yiran.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.db.common.annotation.CreateTimeAdvise;
import one.yiran.dashboard.common.annotation.Excel;
import one.yiran.db.common.annotation.Search;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_operate_log")
@Entity
@Data
public class SysOperateLog extends TimedBasedEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 日志主键
     */
    @Excel(name = "操作序号", cellType = Excel.ColumnType.NUMERIC)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long operateId;

    /**
     * 操作模块
     */
    @Excel(name = "操作模块")
    @Search
    @Column(length = 32)
    private String title;

    /**
     * 业务类型
     */
    @Excel(name = "业务类型", readConverterExp = "0=其它,1=新增,2=修改,3=删除,4=授权,5=导出,6=导入,7=强退,8=生成代码,9=清空数据")
    @NotNull
    @Search
    @Column
    private Integer businessType;

    /**
     * 业务类型数组
     */
    @Search(columnName ="businessType", op = Search.Op.IN)
    @Column
    private Integer businessTypes;

    /**
     * 请求方法
     */
    @Excel(name = "请求方法")
    @Column
    private String method;

    /** 请求方式 */
    @Excel(name = "请求方式")
    @Column
    private String requestMethod;

    @Excel(name = "操作人类别", readConverterExp = "0=其它,1=后台用户,2=手机端用户")
    @Search
    @Column
    private Integer operatorType;

    @Excel(name = "操作人员")
    @Search
    @Column
    private String operateName;

    @Excel(name = "部门名称")
    @Search
    @Column
    private String deptName;

    /**
     * 请求url
     */
    @Excel(name = "请求地址")
    @Column(length = 256)
    private String operateUrl;

    @Excel(name = "操作地址")
    @Column(length = 32)
    private String operateIp;

    /**
     * 操作地点
     */
    @Excel(name = "操作地点")
    @Column(length = 64)
    private String operateLocation;

    /**
     * 请求参数
     */
    @Excel(name = "请求参数")
    @Size(max = 2048)
    @Column(length = 2048)
    private String operateParam;

    /** 返回参数 */
    @Excel(name = "返回参数")
    @Size(max = 2048)
    @Column(length = 2048)
    private String jsonResult;

    /**
     * 状态0正常 1异常
     */
    @Search
    @Excel(name = "状态", readConverterExp = "0=正常,1=异常")
    @Column
    private String status;

    /**
     * 错误消息
     */
    @Excel(name = "错误消息")
    @Column
    private String errorMsg;

    /**
     * 操作时间
     */
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @CreateTimeAdvise
    @Column
    private Date operateTime;

    @Option(value = {"ADMIN","MEMBER"}, message = "sessionType只能是ADMIN，MEMBER")
    @Column(length = 8)
    private String sessionType;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("operateId", this.getOperateId())
                .append("title", getTitle())
                .append("businessType", getBusinessType())
                .append("businessTypes", getBusinessTypes())
                .append("method", getMethod())
                .append("operatorType", getOperatorType())
                .append("operateName", this.getOperateName())
                .append("deptName", getDeptName())
                .append("operateUrl", this.getOperateUrl())
                .append("operateIp", this.getOperateIp())
                .append("operateLocation", this.getOperateLocation())
                .append("operateParam", this.getOperateParam())
                .append("status", getStatus())
                .append("errorMsg", getErrorMsg())
                .append("operateTime", this.getOperateTime())
                .append("sessionType", getSessionType())
                .toString();
    }
}
