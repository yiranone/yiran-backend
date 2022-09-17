package one.yiran.dashboard.web.controller;

import com.querydsl.core.types.Order;
import one.yiran.common.domain.Ztree;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.dashboard.common.expection.user.UserNotLoginException;
import one.yiran.dashboard.common.model.UserSession;
import one.yiran.dashboard.entity.*;
import one.yiran.dashboard.security.SessionContextHelper;
import one.yiran.dashboard.service.*;
import one.yiran.dashboard.web.model.WebMenuTree;
import one.yiran.dashboard.web.util.PermUtil;
import one.yiran.db.common.util.PredicateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private SysMenuService sysMenuService;

    @RequestMapping("/perm/tree")
    public List<WebMenuTree> perms() {
        UserSession user = SessionContextHelper.getLoginUser();
        if(user == null)
            throw new UserNotLoginException();
        List<SysMenu> menusList;
//        if (user.isAdmin()) {
            menusList = sysMenuService.selectVisibleTreeMenus(false);
//        } else {
//            menusList = sysMenuService.selectVisibleTreeMenusByUser(user.getUserId(),false);
//        }
        return PermUtil.toWebMenuTree(menusList);
    }

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

    @PostMapping("/dict/all")
    public Map dictAll() {
        List<SysDictData> dictDatas = sysDictDataService.selectList(PredicateUtil.buildNotDeletePredicate(QSysDictData.sysDictData));
        Map<String, List<HashMap<String, String>>> rts = dictDatas.stream().collect(
                Collectors.groupingBy(SysDictData::getDictType)).entrySet().stream().collect(
                Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> entry.getValue().stream().map(x -> new HashMap<String, String>() {{
                            put("label", x.getDictLabel());
                            put("value", x.getDictValue());
                            put("cssClass", x.getCssClass());
                            put("isDefault", x.getIsDefault());
                        }}).collect(Collectors.toList()))
        );
        return rts;
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