package one.yiran.dashboard.manage.dao;

import one.yiran.dashboard.manage.entity.SysLoginInfo;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginInfoDao extends BaseDao<SysLoginInfo, Long> {

    SysLoginInfo findByInfoId(Long infoId);

    int deleteByInfoIdIn(List<Long> infoIds);
}
