package one.yiran.dashboard.service;

import one.yiran.common.domain.Ztree;
import one.yiran.dashboard.entity.SysDept;
import one.yiran.db.common.service.CrudBaseService;

import java.util.List;

public interface SysDeptService extends CrudBaseService<Long, SysDept> {

    List<Ztree> deptTreeData();

    List<SysDept> selectAllDept(SysDept dept);
}
