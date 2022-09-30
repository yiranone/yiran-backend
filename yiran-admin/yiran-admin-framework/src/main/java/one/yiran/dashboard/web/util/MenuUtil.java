package one.yiran.dashboard.web.util;

import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.common.constants.SystemConstants;
import one.yiran.dashboard.entity.SysMenu;
import one.yiran.dashboard.web.model.WebMenu;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
        if(StringUtils.isNotBlank(sysMenu.getIsFrame()) && StringUtils.equals(sysMenu.getIsFrame(), SystemConstants.MENU_IS_FRAME)) {
            webMenu.setIsFrame(true);
            webMenu.setLink(sysMenu.getRouter());
//            webMenu.setRouter(null);
        }
        if(StringUtils.isNotBlank(sysMenu.getIsCache()) || StringUtils.equals(sysMenu.getIsCache(),SystemConstants.MENU_NOT_CACHE)) {
            webMenu.setIsCache(false);
        } else {
            webMenu.setIsCache(true);
        }
        try {
            if (StringUtils.isNotBlank(sysMenu.getQuery())) {
                String[] kvs = sysMenu.getQuery().split("&");
                Map<String,String> querys  = new LinkedHashMap<>();
                for(String kv : kvs){
                   if(StringUtils.isNotBlank(kv)) {
                       String[] d = kv.split("=");
                       if(d.length > 1) {
                           querys.put(d[0],d[1]);
                       }
                   }
                }
                webMenu.setQuery(querys);
            } else {
                webMenu.setQuery(null);
            }
        } catch (Exception e) {
            log.error("解析菜单参数异常",e);
        }
//        if(StringUtils.isNotBlank(sysMenu.getPerms()))
//            webMenu.getAuthority().setPermission(sysMenu.getPerms());

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
