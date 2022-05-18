package one.yiran.db.common.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import one.yiran.db.common.annotation.CreateTimeAdvise;
import one.yiran.db.common.annotation.Search;
import one.yiran.db.common.annotation.UpdateTimeAdvise;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Date;

@Data
@MappedSuperclass
public class TimedBasedEntity implements Serializable {

    @Column(nullable = false,updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @CreateTimeAdvise
    private Date createTime;

    @Column
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @UpdateTimeAdvise
    private Date updateTime;

    @Column(length = 32,nullable = false,updatable = false)
    private String createBy;

    @Column(length = 32)
    private String updateBy;

    @Column
    @Search
    @JSONField(deserialize = false)
    private Boolean isDelete;

    /**
     * 备注
     */
    @Column
    private String remark;
}
