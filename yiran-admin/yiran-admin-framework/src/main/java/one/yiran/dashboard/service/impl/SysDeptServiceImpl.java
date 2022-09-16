package one.yiran.dashboard.service.impl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.dsl.BooleanExpression;
import one.yiran.common.domain.Ztree;
import one.yiran.dashboard.dao.DeptDao;
import one.yiran.dashboard.entity.QSysDept;
import one.yiran.dashboard.entity.SysDept;
import one.yiran.dashboard.service.SysDeptService;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SysDeptServiceImpl extends CrudBaseServiceImpl<Long, SysDept> implements SysDeptService {

    @Autowired
    private DeptDao deptDao;

    /**
     * 得到子节点列表
     */
    private static List<Ztree> getDirectChildList(List<Ztree> list, Ztree t) {
        List<Ztree> tlist = new ArrayList<>();
        Iterator<Ztree> it = list.iterator();
        while (it.hasNext()) {
            Ztree n = it.next();
            if (n.getPId() != null && n.getPId().longValue() == t.getId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    public static List<Ztree> toTree(List<SysDept> list, long parentId) {
        List<Ztree> trees = list.stream().map(n->{
            Ztree ztree = new Ztree();
            ztree.setId(n.getDeptId());
            ztree.setPId(n.getParentId());
            ztree.setName(n.getDeptName());
            return ztree;
        }).collect(Collectors.toList());
        List<Ztree> returnList = new ArrayList<>();
        for (Iterator<Ztree> iterator = trees.iterator(); iterator.hasNext(); ) {
            Ztree t = iterator.next();
            if (t.getPId() != null && t.getPId() == parentId) {
                recursionTreeFn(trees, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    private static void recursionTreeFn(List<Ztree> list, Ztree t) {
        // 得到子节点列表
        List<Ztree> childList = getDirectChildList(list, t);
        t.setChildren(childList);
        for (Ztree tChild : childList) {
            recursionTreeFn(list, tChild);
        }
    }


    @Override
    public List<Ztree> deptTreeData() {
        List<SysDept> sysDeptList = selectList(QSysDept.sysDept.isDelete.eq(Boolean.FALSE).or(QSysDept.sysDept.isDelete.isNull()),QSysDept.sysDept.orderNum, Order.ASC);

        sortDepts(sysDeptList);
        List<Ztree> ztrees = toTree(sysDeptList,0);
        return ztrees;
    }

    @Override
    public List<SysDept> selectAllDept(SysDept dept) {
        BooleanExpression predicate = QSysDept.sysDept.isDelete.eq(Boolean.FALSE).or(QSysDept.sysDept.isDelete.isNull());
        if(StringUtils.isNotBlank(dept.getDeptName())) {
            predicate = predicate.and(
                    QSysDept.sysDept.deptName.like("%" + dept.getDeptName().trim() + "%" )
                    .or(QSysDept.sysDept.deptCode.like("%" + dept.getDeptName().trim() + "%" )));
        }
        List<SysDept> sysDeptList = selectList(predicate,QSysDept.sysDept.orderNum, Order.ASC);
        return sysDeptList;
    }

    private void sortDepts(List<SysDept> sysDepts) {
        if (sysDepts == null || sysDepts.size() == 0)
            return;
        Collections.sort(sysDepts, (o1, o2) -> {
            if (o1.getParentId() == null) {
                return -1;
            } else if (o2.getParentId() == null) {
                return 1;
            }
            if (o1.getParentId() == o2.getParentId()) {
                return o1.getOrderNum() - o2.getOrderNum();
            } else if (o1.getParentId() > o2.getParentId()) {
                return 1;
            } else {
                return -1;
            }
        });
    }
}
