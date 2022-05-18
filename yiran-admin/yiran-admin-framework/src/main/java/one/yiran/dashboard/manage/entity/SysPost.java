package one.yiran.dashboard.manage.entity;

import one.yiran.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.dashboard.common.annotation.Excel;
import one.yiran.db.common.annotation.Search;
import org.springframework.data.annotation.Transient;
import javax.persistence.*;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_post")
@Entity
@Data
public class SysPost extends TimedBasedEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 岗位序号
     */
    @Excel(name = "岗位序号", cellType = Excel.ColumnType.NUMERIC)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    /**
     * 岗位编码
     */
    @Search
    @Excel(name = "岗位编码")
    @Column
    private String postCode;

    /**
     * 岗位名称
     */
    @Search(op = Search.Op.REGEX)
    @Excel(name = "岗位名称")
    @Column
    private String postName;

    /**
     * 岗位排序
     */
    @Excel(name = "岗位排序")
    @Column
    private String postSort;

    /**
     * 状态（0正常 1停用）
     */
    @Search
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    @Column
    private String status;

    /**
     * 用户是否存在此岗位标识 默认不存在
     */
    @Transient
    private boolean flag = false;

}
