package one.yiran.dashboard.manage.service.impl;

import one.yiran.dashboard.manage.dao.DeptDao;
import one.yiran.dashboard.manage.entity.SysDept;
import one.yiran.dashboard.manage.service.SysDeptService;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class SysDeptServiceImpl extends CrudBaseServiceImpl<Long, SysDept> implements SysDeptService {

    @Autowired
    private DeptDao deptDao;


}
