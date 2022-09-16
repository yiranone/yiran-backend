package one.yiran.dashboard.web.util;

import one.yiran.dashboard.entity.SysMenu;
import one.yiran.dashboard.web.model.WebMenuTree;
import one.yiran.dashboard.web.model.WebPerm;

import java.util.ArrayList;
import java.util.List;

public class PermUtil {

    public static WebPerm toWebPerm(SysMenu sysMenu) {
        WebPerm webPerm = new WebPerm();
        webPerm.setName(sysMenu.getMenuName());
        webPerm.setPermId(sysMenu.getMenuId());
        webPerm.setPerms(sysMenu.getPerms());

        if (sysMenu.getChildren() != null && sysMenu.getChildren().size() > 0) {
            List<SysMenu> childs = sysMenu.getChildren();
            List<WebPerm> webMenuChilds = new ArrayList<>();
            webPerm.setChildren(webMenuChilds);
            for (SysMenu cm : childs) {
                webMenuChilds.add(toWebPerm(cm));
            }
        } else {
            webPerm.setChildren(null);
        }
        return webPerm;
    }

    public static List<WebPerm> toWebPerm(List<SysMenu> sysMenu){
        List<WebPerm> webMenus = new ArrayList<>();
        for(SysMenu m: sysMenu){
            webMenus.add(toWebPerm(m));
        }
        return webMenus;
    }

    public static WebMenuTree toWebMenuTree(SysMenu sysMenu) {
        WebMenuTree webPerm = new WebMenuTree();
        webPerm.setName(sysMenu.getMenuName());
        webPerm.setMenuId(sysMenu.getMenuId());
        webPerm.setPerms(sysMenu.getPerms());

        if (sysMenu.getChildren() != null && sysMenu.getChildren().size() > 0) {
            List<SysMenu> childs = sysMenu.getChildren();
            List<WebMenuTree> webMenuChilds = new ArrayList<>();
            webPerm.setChildren(webMenuChilds);
            for (SysMenu cm : childs) {
                webMenuChilds.add(toWebMenuTree(cm));
            }
        } else {
            webPerm.setChildren(null);
        }
        return webPerm;
    }

    public static List<WebMenuTree> toWebMenuTree(List<SysMenu> sysMenu){
        List<WebMenuTree> webMenus = new ArrayList<>();
        for(SysMenu m: sysMenu){
            webMenus.add(toWebMenuTree(m));
        }
        return webMenus;
    }

}
