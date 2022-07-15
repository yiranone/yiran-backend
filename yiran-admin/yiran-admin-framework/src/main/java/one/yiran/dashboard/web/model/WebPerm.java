package one.yiran.dashboard.web.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WebPerm {
    private String name;
    private String perms;
    private Long permId;
    private List<WebPerm> children = new ArrayList<>();
}
