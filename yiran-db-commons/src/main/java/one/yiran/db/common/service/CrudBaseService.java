package one.yiran.db.common.service;

import com.querydsl.core.types.Predicate;
import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.common.exception.BusinessException;

import java.util.List;

public interface CrudBaseService<K,T> {

     PageModel<T> selectPage(PageRequest request, T target);
     PageModel<T> selectPage(PageRequest request, T target, Predicate predicate);
     PageModel<T> selectPage(PageRequest request, T target, List<Predicate> pres);

     List<T> selectList(PageRequest request, T target, Predicate predicate);
     List<T> selectList(PageRequest request, T target, List<Predicate> pres);
     List<T> selectList(PageRequest request, T target);
     List<T> selectList(T target);
     List<T> selectList(Predicate predicate);
     List<T> selectList(List<Predicate> pres);

     long count(PageRequest pageRequest, T searchUser, Predicate predicate);
     long count(PageRequest pageRequest, T searchUser, List<Predicate> pres);
     long count(PageRequest request, T target);
     long count(Predicate predicate);
     long count(List<Predicate> pres);
     long count();

     T selectByPId(K pId);

     List<T> selectAll();

     /**
      * 逻辑删除
      * @param pId
      * @return
      * @throws BusinessException
      */
     long deleteByPId(K pId) throws BusinessException;
     long deleteByPIds(K[] pIds) throws BusinessException;
     long deleteAll();


     T insert(T target);

     T update(T target);

     /**
      * 物理删除
      * @param pId
      * @return
      */
     long remove(K pId);
     long remove(List<K> pIds);
     long removeAll();

     long removeByPIds(K[] pIds) throws BusinessException;

}
