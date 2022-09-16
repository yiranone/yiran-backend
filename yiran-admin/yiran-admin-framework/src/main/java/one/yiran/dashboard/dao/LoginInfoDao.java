package one.yiran.dashboard.dao;

import one.yiran.dashboard.entity.SysLoginInfo;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginInfoDao extends BaseDao<SysLoginInfo, Long> {

    SysLoginInfo findByInfoId(Long infoId);

    int deleteByInfoIdIn(List<Long> infoIds);
}
