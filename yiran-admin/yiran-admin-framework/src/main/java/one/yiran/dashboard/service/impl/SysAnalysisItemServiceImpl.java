package one.yiran.dashboard.service.impl;

import one.yiran.dashboard.entity.QSysAnalysisItem;
import one.yiran.dashboard.entity.SysAnalysisItem;
import one.yiran.dashboard.service.SysAnalysisItemService;
import lombok.extern.slf4j.Slf4j;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import one.yiran.db.common.util.PredicateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;

@Slf4j
@Service
public class SysAnalysisItemServiceImpl extends CrudBaseServiceImpl<Long, SysAnalysisItem> implements SysAnalysisItemService {

    @Transactional
    @Override
    public SysAnalysisItem insertOrUpdate(Long channelId, LocalDate date, String type, String key , String value){
        QSysAnalysisItem qAnalysisItem = QSysAnalysisItem.sysAnalysisItem;
        SysAnalysisItem dbItem = selectOne(PredicateBuilder.builder()
                .addEqual(qAnalysisItem.channelId,channelId)
                .addEqualIfNotBlank(qAnalysisItem.belongDate, date)
                .addEqualIfNotBlank(qAnalysisItem.type, type)
                .addExpression(qAnalysisItem.keyName.eq(key)).toPredicate());
        if (dbItem == null) {
            SysAnalysisItem item = new SysAnalysisItem();
            item.setChannelId(channelId);
            item.setBelongDate(date);
            item.setType(type);
            item.setKeyName(key);
            item.setValue(value);
            item.setCreateTime(new Date());
            item.setUpdateTime(new Date());
            return insert(item);
        } else {
            dbItem.setValue(value);
            dbItem.setUpdateTime(new Date());
            return update(dbItem);
        }
    }

}
