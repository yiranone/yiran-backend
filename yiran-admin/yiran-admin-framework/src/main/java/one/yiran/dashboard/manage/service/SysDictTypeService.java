package one.yiran.dashboard.manage.service;

import one.yiran.common.domain.PageRequest;
import one.yiran.dashboard.manage.entity.SysDictType;
import one.yiran.db.common.service.CrudBaseService;
import one.yiran.common.domain.Ztree;

import java.util.List;


public interface SysDictTypeService extends CrudBaseService<Long, SysDictType> {

    /**
     * 根据字典类型查询信息
     *
     * @param dictType 字典类型
     * @return 字典类型
     */
    public SysDictType selectDictTypeByType(String dictType);

    public SysDictType insertDictType(SysDictType sysDictType);

    public SysDictType updateDictType(SysDictType sysDictType);

    public boolean checkDictTypeUnique(SysDictType sysDictType);

    /**
     * 查询字典类型树
     *
     * @param dictType 字典类型
     * @return 所有字典类型
     */
    public List<Ztree> selectDictTree(PageRequest request, SysDictType dictType);
}
