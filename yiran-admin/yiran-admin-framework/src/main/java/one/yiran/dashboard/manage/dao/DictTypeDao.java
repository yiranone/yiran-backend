package one.yiran.dashboard.manage.dao;

import one.yiran.dashboard.manage.entity.SysDictType;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictTypeDao extends BaseDao<SysDictType, Long> {

    SysDictType findDictTypeByDictId(Long dictId);

    SysDictType findDictTypeByDictType(String dictType);

    List<SysDictType> findAllDictTypeByDictType(String dictType);

    int deleteDictTypeByDictId(Long ditcId);
}
