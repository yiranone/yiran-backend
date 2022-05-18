package one.yiran.dashboard.manage.dao;

import one.yiran.dashboard.manage.entity.SysDictData;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictDataDao extends BaseDao<SysDictData, Long> {

    List<SysDictData> findAllByDictTypeOrderByDictSortAsc(String dictType);

    int deleteAllByDictCode(Long dictCode);

    SysDictData findByDictCode(Long dictCode);

    SysDictData findByDictTypeAndDictValue(String dictType, String dictValue);

    int deleteAllByDictCodeIn(List<Long> delIds);
}
