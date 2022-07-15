package one.yiran.dashboard.web.util;

import one.yiran.dashboard.manage.entity.SysMenu;
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

}
