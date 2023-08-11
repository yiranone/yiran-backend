package one.yiran.dashboard.service;

import one.yiran.dashboard.entity.SysAnalysisItem;
import one.yiran.db.common.service.CrudBaseService;

import java.time.LocalDate;

public interface SysAnalysisItemService extends CrudBaseService<Long, SysAnalysisItem> {

    SysAnalysisItem insertOrUpdate(Long channelId, LocalDate date, String type, String key , String value);
    SysAnalysisItem insertOrUpdate(Long channelId, LocalDate date, String type, String subType, String key , String value);
}
