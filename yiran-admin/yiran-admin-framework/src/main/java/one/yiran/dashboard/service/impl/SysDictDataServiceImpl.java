package one.yiran.dashboard.service.impl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.Predicate;
import one.yiran.dashboard.entity.QSysDictData;
import one.yiran.dashboard.dao.DictDataDao;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.entity.SysDictData;
import one.yiran.dashboard.service.SysDictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;


@Service
@Slf4j
public class SysDictDataServiceImpl extends CrudBaseServiceImpl<Long,SysDictData> implements SysDictDataService {

    @Autowired
    private DictDataDao dictDataDao;

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        Assert.notNull(dictType,"dictType不能为空");
        return selectList(QSysDictData.sysDictData.dictType.eq(dictType),QSysDictData.sysDictData.orderNum, Order.ASC);
    }

    @Override
    public List<SysDictData> selectNormalDictDataByType(String dictType) {
        QSysDictData qSysDictData = QSysDictData.sysDictData;
        Predicate predicate = qSysDictData.dictType.eq(dictType).and(qSysDictData.status.eq("0"));
        return (List<SysDictData>) dictDataDao.findAll(predicate);
    }

    @Override
    public String selectDictLabel(String dictType, String dictValue) {
        SysDictData sysDictData = dictDataDao.findByDictTypeAndDictValue(dictType, dictValue);
        if (sysDictData != null) {
            return sysDictData.getDictLabel();
        }
        return "";
    }

}
