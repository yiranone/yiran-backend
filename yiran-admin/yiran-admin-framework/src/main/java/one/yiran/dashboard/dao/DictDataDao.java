package one.yiran.dashboard.dao;

import one.yiran.dashboard.entity.SysDictData;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictDataDao extends BaseDao<SysDictData, Long> {

    int deleteAllByDictCode(Long dictCode);

    SysDictData findByDictCode(Long dictCode);

    SysDictData findByDictTypeAndDictValue(String dictType, String dictValue);

    int deleteAllByDictCodeIn(List<Long> delIds);
}
