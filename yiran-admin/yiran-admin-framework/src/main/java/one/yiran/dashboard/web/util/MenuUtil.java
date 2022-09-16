package one.yiran.dashboard.web.util;

import one.yiran.dashboard.entity.SysMenu;
import one.yiran.dashboard.web.model.WebMenu;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MenuUtil {

    public static WebMenu toWebMenu(SysMenu sysMenu) {
        WebMenu webMenu = new WebMenu();
        webMenu.setName(sysMenu.getMenuName());
//        if(StringUtils.isNotBlank(sysMenu.getUrl()))
//            webMenu.setPath(sysMenu.getUrl());
        if(StringUtils.isNotBlank(sysMenu.getIcon()))
            webMenu.setIcon(sysMenu.getIcon());
        webMenu.setRouter(sysMenu.getRouter());
        webMenu.setMenuType(sysMenu.getMenuType());
        if(StringUtils.isNotBlank(sysMenu.getPerms()))
            webMenu.getAuthority().setPermission(sysMenu.getPerms());

        if (sysMenu.getChildren() != null && sysMenu.getChildren().size() > 0) {
            List<SysMenu> childs = sysMenu.getChildren();
            List<WebMenu> webMenuChilds = new ArrayList<>();
            webMenu.setChildren(webMenuChilds);
            for (SysMenu cm : childs) {
                webMenuChilds.add(toWebMenu(cm));
            }
        } else {
            webMenu.setChildren(null);
        }
        if(StringUtils.isNotBlank(sysMenu.getComponent())) {
            webMenu.setComponent(sysMenu.getComponent());
        }

        if(StringUtils.equals("会员列表",webMenu.getName())){
//            webMenu.setInvisible(true);
//            webMenu.setComponent("/pages/system/member");
        }

        return webMenu;
    }

    public static List<WebMenu> toWebMenu(List<SysMenu> sysMenu){
        List<WebMenu> webMenus = new ArrayList<>();
        for(SysMenu m: sysMenu){
            webMenus.add(toWebMenu(m));
        }
        return webMenus;
    }

    public static SysMenu toSysMenu(WebMenu webMenu) {
        SysMenu sysMenu = new SysMenu();
        return sysMenu;
    }
}
