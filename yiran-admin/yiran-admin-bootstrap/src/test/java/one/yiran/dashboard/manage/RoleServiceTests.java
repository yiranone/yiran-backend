//package one.yiran.dashboard.manage;
//
//import lombok.extern.slf4j.Slf4j;
//import one.yiran.dashboard.entity.SysDept;
//import SysPost;
//import SysRole;
//import one.yiran.dashboard.service.SysDeptService;
//import SysPostService;
//import SysRoleService;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//import java.util.Set;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@Slf4j
//public class RoleServiceTests {
//
//
//    @Autowired
//    private SysDeptService deptService;
//
//    @Autowired
//    private SysRoleService roleService;
//
//    @Autowired
//    private SysPostService postService;
//
//
//    @Test
//    public void contextLoads() {
//    }
//
//    @Test
//    public void testAggregate() {
//
//        SysDept dept = new SysDept();
//        dept.setDeptId(1L);
//        dept.setParentId(0L);
//        dept.setAncestors(Arrays.asList(new Long[]{}));
//        dept.setChildren(Arrays.asList(new Long[]{101L, 102L, 10101L, 10102L}));
//        dept.setDeptName("总部");
//        dept.setOrderNum("0");
//        dept.setLeader("老大");
//        dept.setEmail("tt@tt.tt");
//        dept.setStatus("0");
//        dept.setDelFlag("0");
//        dept.setCreateBy("admin");
//        dept.setCreateTime(new Date());
//        dept.setUpdateBy("admin");
//        dept.setUpdateTime(new Date());
//        deptService.insertDept(dept);
//    }
//
//    @Test
//    public void testAggregate2() {
//
//        SysDept dept = new SysDept();
//        dept.setDeptId(101L);
//        dept.setParentId(1L);
//        dept.setAncestors(Arrays.asList(new Long[]{1L}));
//        dept.setChildren(Arrays.asList(new Long[]{10101L, 10102L}));
//        dept.setDeptName("深圳总公司");
//        dept.setOrderNum("0");
//        dept.setLeader("老大");
//        dept.setEmail("tt@tt.tt");
//        dept.setStatus("0");
//        dept.setDelFlag("0");
//        dept.setCreateBy("admin");
//        dept.setCreateTime(new Date());
//        dept.setUpdateBy("admin");
//        dept.setUpdateTime(new Date());
//        deptService.insertDept(dept);
//    }
//
//    @Test
//    public void testAggregate3() {
//
//        SysDept dept = new SysDept();
//        dept.setDeptId(102L);
//        dept.setParentId(1L);
//        dept.setAncestors(Arrays.asList(new Long[]{1L}));
//        dept.setChildren(Arrays.asList(new Long[]{}));
//        dept.setDeptName("上海总公司");
//        dept.setOrderNum("0");
//        dept.setLeader("老大");
//        dept.setEmail("tt@tt.tt");
//        dept.setStatus("0");
//        dept.setDelFlag("0");
//        dept.setCreateBy("admin");
//        dept.setCreateTime(new Date());
//        dept.setUpdateBy("admin");
//        dept.setUpdateTime(new Date());
//        deptService.insertDept(dept);
//    }
//
//    @Test
//    public void testAggregate4() {
//
//        SysDept dept = new SysDept();
//        dept.setDeptId(10101L);
//        dept.setParentId(101L);
//        dept.setAncestors(Arrays.asList(new Long[]{101L, 1L}));
//        dept.setDeptName("研发");
//        dept.setOrderNum("0");
//        dept.setLeader("老大");
//        dept.setEmail("tt@tt.tt");
//        dept.setStatus("0");
//        dept.setDelFlag("0");
//        dept.setCreateBy("admin");
//        dept.setCreateTime(new Date());
//        dept.setUpdateBy("admin");
//        dept.setUpdateTime(new Date());
//        deptService.insertDept(dept);
//    }
//
//    @Test
//    public void testAggregate5() {
//
//        SysDept dept = new SysDept();
//        dept.setDeptId(10102L);
//        dept.setParentId(101L);
//        dept.setAncestors(Arrays.asList(new Long[]{101L, 1L}));
//        dept.setChildren(Arrays.asList(new Long[]{}));
//        dept.setDeptName("销售");
//        dept.setOrderNum("0");
//        dept.setLeader("老大");
//        dept.setEmail("tt@tt.tt");
//        dept.setStatus("0");
//        dept.setDelFlag("0");
//        dept.setCreateBy("admin");
//        dept.setCreateTime(new Date());
//        dept.setUpdateBy("admin");
//        dept.setUpdateTime(new Date());
//        deptService.insertDept(dept);
//    }
//
//    @Test
//    public void insertRole1() {
//
//        SysRole role = new SysRole();
//        role.setRoleId(1L);
//        role.setRoleName("管理员");
//        role.setRoleKey("admin");
//        role.setRoleSort("1");
//        role.setDataScope("1");
//        role.setStatus("0");
//        role.setDelFlag("0");
//
//
//        role.setCreateBy("admin");
//        role.setCreateTime(new Date());
//        role.setUpdateBy("admin");
//        role.setUpdateTime(new Date());
//        roleService.insertRole(role);
//    }
//
//    @Test
//    public void insertRole2() {
//
//        SysRole role = new SysRole();
//        role.setRoleId(2L);
//        role.setRoleName("普通人员");
//        role.setRoleKey("common");
//        role.setRoleSort("2");
//        role.setDataScope("3");
//        role.setStatus("0");
//        role.setDelFlag("0");
//
//
//        role.setCreateBy("admin");
//        role.setCreateTime(new Date());
//        role.setUpdateBy("admin");
//        role.setUpdateTime(new Date());
//        roleService.insertRole(role);
//    }
//
//    @Test
//    public void insertPost1() {
//
//        SysPost post = new SysPost();
//        post.setPostId(1L);
//        post.setPostCode("ceo");
//        post.setPostName("董事长");
//        post.setPostSort("1");
//        post.setStatus("0");
//
//
//        post.setCreateBy("admin");
//        post.setCreateTime(new Date());
//        post.setUpdateBy("admin");
//        post.setUpdateTime(new Date());
//        postService.insert(post);
//    }
//
//    @Test
//    public void insertPost2() {
//
//        SysPost post = new SysPost();
//        post.setPostId(2L);
//        post.setPostName("项目经理");
//        post.setPostCode("se");
//        post.setPostSort("2");
//        post.setStatus("0");
//
//
//        post.setCreateBy("admin");
//        post.setCreateTime(new Date());
//        post.setUpdateBy("admin");
//        post.setUpdateTime(new Date());
//        postService.insert(post);
//    }
//
//    @Test
//    public void insertPost4() {
//
//        SysPost post = new SysPost();
//        post.setPostId(4L);
//        post.setPostCode("user");
//        post.setPostName("普通员工");
//        post.setPostSort("4");
//        post.setStatus("0");
//
//
//        post.setCreateBy("admin");
//        post.setCreateTime(new Date());
//        post.setUpdateBy("admin");
//        post.setUpdateTime(new Date());
//        postService.insert(post);
//    }
//
//
//    @Test
//    public void selectByUser() {
//        Set<String> rs = roleService.selectRoleKeys(1002L);
//        log.info("========={}", rs);
//
//        List<SysRole> sysRoles = roleService.selectAllRolesByUserId(1002L);
//        log.info("========000={}", sysRoles);
//
//    }
//
//
//}
