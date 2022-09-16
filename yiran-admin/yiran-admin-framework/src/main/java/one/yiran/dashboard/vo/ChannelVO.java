package one.yiran.dashboard.vo;

import lombok.Data;
import one.yiran.dashboard.entity.SysChannel;

import java.time.LocalDate;

@Data
public class ChannelVO {

    private Long channelId;
    private String channelName;
    private String channelCode;
    private LocalDate expireDate;
    private String channelType;
    private String status;

    public static final ChannelVO from(SysChannel db) {
        ChannelVO vo = new ChannelVO();
        vo.setChannelId(db.getChannelId());
        vo.setChannelName(db.getChannelName());
        vo.setChannelCode(db.getChannelCode());
        vo.setChannelType(db.getChannelType());
        vo.setExpireDate(db.getExpireDate());
        return vo;
    }
}
