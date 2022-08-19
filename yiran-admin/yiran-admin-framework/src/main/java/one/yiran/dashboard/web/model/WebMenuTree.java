package one.yiran.dashboard.web.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WebMenuTree {
    private String name;
    private String perms;
    private Long menuId;
    private List<WebMenuTree> children = new ArrayList<>();
}
