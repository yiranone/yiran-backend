package one.yiran.dashboard.common.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSession {

    private Long memberId;

    private String phone;
    private String name;
    private String nickName;
    private String avatar;
    private String token;
    private Boolean isLocked;
    private Long tokenExpires; //token过期时间 毫秒

    private Long channelId;
    private String channelCode;
    private String channelName;
}
