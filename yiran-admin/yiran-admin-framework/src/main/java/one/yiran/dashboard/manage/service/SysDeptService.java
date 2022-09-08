package one.yiran.dashboard.manage.service;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import one.yiran.common.domain.Ztree;
import one.yiran.dashboard.manage.entity.SysDept;
import one.yiran.db.common.service.CrudBaseService;

import java.util.List;

public interface SysDeptService extends CrudBaseService<Long, SysDept> {

    List<Ztree> deptTreeData();

    List<SysDept> selectAllDept(SysDept dept);
}
