package one.yiran.dashboard.manage.service;

import one.yiran.db.common.service.CrudBaseService;
import one.yiran.dashboard.manage.entity.SysDictData;

import java.util.List;

public interface SysDictDataService extends CrudBaseService<Long,SysDictData> {

    List<SysDictData> selectDictDataByType(String dictType);

    List<SysDictData> selectNormalDictDataByType(String dictType);

    String selectDictLabel(String dictType, String dictValue);

}
