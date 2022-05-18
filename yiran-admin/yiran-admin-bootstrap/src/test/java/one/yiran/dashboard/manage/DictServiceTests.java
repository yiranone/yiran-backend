//package one.yiran.dashboard.manage;
//
//import lombok.extern.slf4j.Slf4j;
//import SysDictData;
//import SysDictType;
//import SysDictTypeService;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.List;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@Slf4j
//public class DictServiceTests {
//
//	@Autowired
//	private SysDictTypeService dictTypeService;
//
//
//	@Autowired
//	private SysDictDataService  dictDataService;
//
//	@Test
//	public void contextLoads() {
//	}
//
//	@Test
//	public void testAggregate(){
//
//		SysDictType dictType = new SysDictType();
//		dictType.setDictId(1L);
//		dictType.setDictName("用户性别");
//		dictType.setDictType("sys_user_sex");
//		dictType.setStatus("0");
//		dictType.setCreateBy("sys");
//
//		dictTypeService.insertDictType(dictType);
//	}
//
//
//	@Test
//	public void insertDictData(){
//
//		SysDictData data = new SysDictData();
//		data.setDictCode(1L);
//		data.setDictSort(1L);
//		data.setDictLabel("男");
//		data.setDictValue("0");
//		data.setDictType("sys_user_sex");
//		data.setListClass("");
//		data.setIsDefault("Y");
//		data.setStatus("0");
//		dictDataService.insertDictData(data);
//	}
//
//
//	@Test
//	public void sys_show_hide(){
//
//		SysDictType dictType = new SysDictType();
//		dictType.setDictId(2L);
//		dictType.setDictName("菜单状态");
//		dictType.setDictType("sys_show_hide");
//		dictType.setStatus("0");
//		dictType.setCreateBy("sys");
//
//		dictTypeService.insertDictType(dictType);
//	}
//
//
//	@Test
//	public void sys_show_hide1(){
//
//		SysDictData data = new SysDictData();
//		data.setDictCode(4L);
//		data.setDictSort(1L);
//		data.setDictLabel("显示");
//		data.setDictValue("0");
//		data.setDictType("sys_show_hide");
//		data.setListClass("primary");
//		data.setIsDefault("Y");
//		data.setStatus("0");
//		dictDataService.insertDictData(data);
//	}
//
//	@Test
//	public void sys_show_hide2(){
//
//		SysDictData data = new SysDictData();
//		data.setDictCode(5L);
//		data.setDictSort(2L);
//		data.setDictLabel("隐藏");
//		data.setDictValue("0");
//		data.setDictType("sys_show_hide");
//		data.setListClass("danger");
//		data.setIsDefault("N");
//		data.setStatus("0");
//		dictDataService.insertDictData(data);
//	}
//
//
//	@Test
//	public void sys_normal_disable(){
//
//		SysDictType dictType = new SysDictType();
//		dictType.setDictId(3L);
//		dictType.setDictName("系统开关");
//		dictType.setDictType("sys_normal_disable");
//		dictType.setStatus("0");
//		dictType.setCreateBy("sys");
//
//		dictTypeService.insertDictType(dictType);
//	}
//
//
//	@Test
//	public void sys_normal_disable1(){
//
//		SysDictData data = new SysDictData();
//		data.setDictCode(6L);
//		data.setDictSort(1L);
//		data.setDictLabel("正常");
//		data.setDictValue("0");
//		data.setDictType("sys_normal_disable");
//		data.setListClass("");
//		data.setIsDefault("Y");
//		data.setStatus("0");
//		dictDataService.insertDictData(data);
//	}
//
//	@Test
//	public void sys_normal_disable2(){
//
//		SysDictData data = new SysDictData();
//		data.setDictCode(7L);
//		data.setDictSort(2L);
//		data.setDictLabel("停用");
//		data.setDictValue("1");
//		data.setDictType("sys_normal_disable");
//		data.setListClass("");
//		data.setIsDefault("N");
//		data.setStatus("0");
//		dictDataService.insertDictData(data);
//	}
//
//}
