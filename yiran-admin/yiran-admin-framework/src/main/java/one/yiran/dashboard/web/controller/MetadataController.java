package one.yiran.dashboard.web.controller;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.dsl.BooleanExpression;
import one.yiran.common.domain.Ztree;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.dashboard.manage.entity.*;
import one.yiran.dashboard.manage.service.SysChannelService;
import one.yiran.dashboard.manage.service.SysDeptService;
import one.yiran.dashboard.manage.service.SysDictDataService;
import one.yiran.dashboard.manage.service.SysPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AjaxWrapper
@Controller
@RequestMapping("/metadata")
public class MetadataController {

    @Autowired
    private SysChannelService channelService;
    @Autowired
    private SysPostService postService;
    @Autowired
    private SysDeptService sysDeptService;
    @Autowired
    private SysDictDataService sysDictDataService;

    @PostMapping("/dict/list")
    public List dictList(@ApiParam String dictType) {
        List<SysDictData> dictDatas = sysDictDataService.selectDictDataByType(dictType);
        List list = new ArrayList();
        for (SysDictData d : dictDatas) {
            Map<String,Object> map = new HashMap<>();
            map.put("label",d.getDictLabel());
            map.put("value",d.getDictValue());
            list.add(map);
        }
        return list;
    }

    @PostMapping("/dept/all")
    public List deptAll() {
        List<SysDept> depts = sysDeptService.selectList(QSysDept.sysDept.isDelete.eq(Boolean.FALSE).or(QSysDept.sysDept.isDelete.isNull()),QSysDept.sysDept.orderNum, Order.ASC);
        List list = new ArrayList();
        for (SysDept d : depts) {
            Map<String,Object> map = new HashMap<>();
            map.put("deptName",d.getDeptName());
            map.put("deptId",d.getDeptId());
            map.put("parentId",d.getParentId());
            list.add(map);
        }
        return list;
    }

    @PostMapping("/dept/tree")
    public List deptTree() {
        List<Ztree> depts = sysDeptService.deptTreeData();
        return depts;
    }

    @PostMapping("/channel/all")
    public List channelAll() {
        QSysChannel qObj = QSysChannel.sysChannel;
        List<SysChannel> channels = channelService.selectList(qObj.isDelete.eq(Boolean.FALSE).or(qObj.isDelete.isNull()), qObj.channelSort, Order.ASC);
        List list = new ArrayList();
        for (SysChannel d : channels) {
            Map<String,Object> map = new HashMap<>();
            map.put("channelName",d.getChannelName());
            map.put("channelId",d.getChannelId());
            list.add(map);
        }
        return list;
    }

    @PostMapping("/post/all")
    public List postAll() {
        List<SysPost> posts = postService.selectList(QSysPost.sysPost.isDelete.eq(Boolean.FALSE).or(QSysPost.sysPost.isDelete.isNull()),QSysPost.sysPost.postSort, Order.ASC);
        List list = new ArrayList();
        for (SysPost p : posts) {
            Map<String,Object> map = new HashMap<>();
            map.put("postName",p.getPostName());
            map.put("postId",p.getPostId());
            list.add(map);
        }
        return list;
    }

}