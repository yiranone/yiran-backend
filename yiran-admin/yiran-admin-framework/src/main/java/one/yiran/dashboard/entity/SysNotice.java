package one.yiran.dashboard.entity;

import one.yiran.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.db.common.annotation.Search;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_notice")
@Entity
@Data
public class SysNotice extends TimedBasedEntity {

    /**
     * 公告ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    /**
     * 公告标题
     */
    @Search(op = Search.Op.REGEX)
    @NotBlank(message = "公告标题不能为空")
    @Size(min = 5, max = 50, message = "公告标题需要在5到50个字符")
    private String noticeTitle;
    /**
     * 公告类型（1通知 2公告）
     */
    @Search
    @NotNull(message = "公告类型不能为空")
    private String noticeType;
    /**
     * 公告内容
     */
    @NotNull(message = "公告内容不能为空")
    @Size(min = 5, max = 2000, message = "公告标题需要在5到50000个字符")
    private String noticeContent;
    /**
     * 公告状态（0正常 1关闭）
     */
    private String status;
}
