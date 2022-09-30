package one.yiran.dashboard.web.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class WebMenu {
    private String router;
    private String name;
    private String menuType;
//    private Authority authority = new Authority();
    private String redirect;
    private String icon;
    private String component;
//    private Boolean renderMenu;
    private Boolean invisible;
    private Boolean isCache;
    private Boolean isFrame;
    private String link;
    private Map<String,String> query = new LinkedHashMap<>();

//    private String component; //() => import('@/pages/exception/404'), TabsView BlankView PageView
//    private WebMenuMeta meta = new WebMenuMeta();
    private List<WebMenu> children = new ArrayList<>();

//    @Data
//    public static class Authority {
//        private String permission;
//    }

//    @Data
//    public static class WebMenuMeta {
//        private String icon;
//        private Boolean invisible;
//        private String page;
//        private String link;
//    }
}
