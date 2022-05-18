package one.yiran.dashboard.manage.entity;

import one.yiran.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import one.yiran.db.common.annotation.Search;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Table(name = "sys_menu",indexes = {
        @Index(name = "idx_menuName",columnList = "menuName",unique = true),
        @Index(name = "idx_parentId",columnList = "parentId")
})
@Entity
@Data
public class SysMenu extends TimedBasedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menuId;

    @Search(op = Search.Op.REGEX)
    @NotBlank(message = "菜单名称不能为空")
    @Size(min = 0, max = 50, message = "菜单名称长度不能超过50个字符")
    @Column(length = 50)
    private String menuName;

//    @Search
//    @Size(min = 0, max = 200, message = "请求地址不能超过200个字符")
//    @Column
//    private String url;

    @Search
    @Column
    private Long parentId;
    @Column(length = 50)
    private String parentName;

    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空")
    @Column
    private Integer orderNum;

    /**
     * 打开方式：menuItem页签 menuBlank新窗口
     */
    @Column
    private String target;

    @Column(length = 32)
    private String router;

    /**
     * 类型:M目录,C菜单,F按钮
     */
    @Search
    @NotBlank(message = "菜单类型不能为空")
    @Column(length = 1,nullable = false)
    private String menuType;

    /**
     * 菜单状态:0显示,1隐藏
     */
    @Search
    @Column(length = 1,nullable = false)
    private String visible;

    /**
     * 权限字符串
     */
    @Size(min = 0, max = 100, message = "权限标识长度不能超过100个字符")
    @Column(length = 32)
    private String perms;

    /**
     * 菜单图标
     */
    @Column(length = 32)
    private String icon;

    /**
     * 子菜单
     */
    @Transient
    private List<SysMenu> children = new ArrayList<SysMenu>();

}
