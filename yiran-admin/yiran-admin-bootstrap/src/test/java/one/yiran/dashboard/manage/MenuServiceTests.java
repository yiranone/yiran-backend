//package one.yiran.dashboard.manage;
//
//import lombok.extern.slf4j.Slf4j;
//import SysMenu;
//import SysUser;
//import SysMenuService;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.Date;
//import java.util.Set;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@Slf4j
//public class MenuServiceTests {
//
//
//    @Autowired
//    private SysMenuService menuService;
//
//
//    @Test
//    public void contextLoads() {
//    }
//
//    @Test
//    public void findMenus() {
//
//        menuService.selectMenusByUser(new SysUser());
//    }
//
//    @Test
//    public void insertMenu() {
//        SysMenu m = new SysMenu();
//        m.setMenuId(1L);
//        m.setMenuName("系统管理");
//        m.setParentId(0L);
//        m.setOrderNum("1");
//        m.setUrl("#");
//        m.setMenuType("M");
//        m.setVisible("0");
//        m.setPerms("");
//        m.setIcon("fa fa-gear");
//        m.setCreateBy("admin");
//        m.setCreateTime(new Date());
//        m.setUpdateBy("admin");
//        m.setUpdateTime(new Date());
//        menuService.insertMenu(m);
//    }
//
//    @Test
//    public void insertMenu2() {
//        SysMenu m = new SysMenu();
//        m.setMenuId(2L);
//        m.setMenuName("系统监控");
//        m.setParentId(0L);
//        m.setOrderNum("2");
//        m.setUrl("#");
//        m.setMenuType("M");
//        m.setVisible("0");
//        m.setPerms("");
//        m.setIcon("fa fa-video-camera");
//        m.setCreateBy("admin");
//        m.setCreateTime(new Date());
//        m.setUpdateBy("admin");
//        m.setUpdateTime(new Date());
//        menuService.insertMenu(m);
//    }
//
//    @Test
//    public void insertMenu3() {
//        SysMenu m = new SysMenu();
//        m.setMenuId(3L);
//        m.setMenuName("系统工具");
//        m.setParentId(0L);
//        m.setOrderNum("3");
//        m.setUrl("#");
//        m.setMenuType("M");
//        m.setVisible("0");
//        m.setPerms("");
//        m.setIcon("fa fa-bars");
//        m.setCreateBy("admin");
//        m.setCreateTime(new Date());
//        m.setUpdateBy("admin");
//        m.setUpdateTime(new Date());
//        menuService.insertMenu(m);
//    }
//
//
//    @Test
//    public void insertMenu100() {
//        SysMenu m = new SysMenu();
//        m.setMenuId(100L);
//        m.setMenuName("用户管理");
//        m.setParentId(1L);
//        m.setOrderNum("1");
//        m.setUrl("/system/user");
//        m.setMenuType("C");
//        m.setVisible("0");
//        m.setPerms("system:user:view");
//        m.setIcon("#");
//        m.setCreateBy("admin");
//        m.setCreateTime(new Date());
//        m.setUpdateBy("admin");
//        m.setUpdateTime(new Date());
//        menuService.insertMenu(m);
//    }
//
//    @Test
//    public void insertMenu101() {
//        SysMenu m = new SysMenu();
//        m.setMenuId(101L);
//        m.setMenuName("角色管理");
//        m.setParentId(1L);
//        m.setOrderNum("1");
//        m.setUrl("/system/role");
//        m.setMenuType("C");
//        m.setVisible("0");
//        m.setPerms("system:role:view");
//        m.setIcon("#");
//        m.setCreateBy("admin");
//        m.setCreateTime(new Date());
//        m.setUpdateBy("admin");
//        m.setUpdateTime(new Date());
//        menuService.insertMenu(m);
//    }
//
//    @Test
//    public void insertMenu102() {
//        SysMenu m = new SysMenu();
//        m.setMenuId(102L);
//        m.setMenuName("菜单管理");
//        m.setParentId(1L);
//        m.setOrderNum("3");
//        m.setUrl("/system/menu");
//        m.setMenuType("C");
//        m.setVisible("0");
//        m.setPerms("system:menu:view");
//        m.setIcon("#");
//        m.setCreateBy("admin");
//        m.setCreateTime(new Date());
//        m.setUpdateBy("admin");
//        m.setUpdateTime(new Date());
//        menuService.insertMenu(m);
//    }
//
//    @Test
//    public void insertMenu103() {
//        SysMenu m = new SysMenu();
//        m.setMenuId(103L);
//        m.setMenuName("部门管理");
//        m.setParentId(1L);
//        m.setOrderNum("4");
//        m.setUrl("/system/dept");
//        m.setMenuType("C");
//        m.setVisible("0");
//        m.setPerms("system:dept:view");
//        m.setIcon("#");
//        m.setCreateBy("admin");
//        m.setCreateTime(new Date());
//        m.setUpdateBy("admin");
//        m.setUpdateTime(new Date());
//        menuService.insertMenu(m);
//    }
//
//    @Test
//    public void insertMenu104() {
//        SysMenu m = new SysMenu();
//        m.setMenuId(104L);
//        m.setMenuName("岗位管理");
//        m.setParentId(1L);
//        m.setOrderNum("5");
//        m.setUrl("/system/post");
//        m.setMenuType("C");
//        m.setVisible("0");
//        m.setPerms("system:post:view");
//        m.setIcon("#");
//        m.setCreateBy("admin");
//        m.setCreateTime(new Date());
//        m.setUpdateBy("admin");
//        m.setUpdateTime(new Date());
//        menuService.insertMenu(m);
//    }
//
//    @Test
//    public void insertMenu105() {
//        SysMenu m = new SysMenu();
//        m.setMenuId(105L);
//        m.setMenuName("字典管理");
//        m.setParentId(1L);
//        m.setOrderNum("6");
//        m.setUrl("/system/dict");
//        m.setMenuType("C");
//        m.setVisible("0");
//        m.setPerms("system:dict:view");
//        m.setIcon("#");
//        m.setCreateBy("admin");
//        m.setCreateTime(new Date());
//        m.setUpdateBy("admin");
//        m.setUpdateTime(new Date());
//        menuService.insertMenu(m);
//    }
//
//    @Test
//    public void insertMenu106() {
//        SysMenu m = new SysMenu();
//        m.setMenuId(106L);
//        m.setMenuName("参数设置");
//        m.setParentId(1L);
//        m.setOrderNum("7");
//        m.setUrl("/system/config");
//        m.setMenuType("C");
//        m.setVisible("0");
//        m.setPerms("system:config:view");
//        m.setIcon("#");
//        m.setCreateBy("admin");
//        m.setCreateTime(new Date());
//        m.setUpdateBy("admin");
//        m.setUpdateTime(new Date());
//        menuService.insertMenu(m);
//    }
//
//    @Test
//    public void insertMenu107() {
//        SysMenu m = new SysMenu();
//        m.setMenuId(107L);
//        m.setMenuName("通知公告");
//        m.setParentId(1L);
//        m.setOrderNum("8");
//        m.setUrl("/system/notice");
//        m.setMenuType("C");
//        m.setVisible("0");
//        m.setPerms("system:notice:view");
//        m.setIcon("#");
//        m.setCreateBy("admin");
//        m.setCreateTime(new Date());
//        m.setUpdateBy("admin");
//        m.setUpdateTime(new Date());
//        menuService.insertMenu(m);
//    }
//
//    @Test
//    public void insertMenu108() {
//        SysMenu m = new SysMenu();
//        m.setMenuId(108L);
//        m.setMenuName("在线用户");
//        m.setParentId(1L);
//        m.setOrderNum("9");
//        m.setUrl("/monitor/online");
//        m.setMenuType("C");
//        m.setVisible("0");
//        m.setPerms("monitor:online:view");
//        m.setIcon("#");
//        m.setCreateBy("admin");
//        m.setCreateTime(new Date());
//        m.setUpdateBy("admin");
//        m.setUpdateTime(new Date());
//        menuService.insertMenu(m);
//    }
//
//
//    @Test
//    public void selectUserMenu() {
//
//        Set<String> menus = menuService.selectPermsByUserId(1002L);
//        log.info("=======::{}", menus);
//    }
//
//
//}
