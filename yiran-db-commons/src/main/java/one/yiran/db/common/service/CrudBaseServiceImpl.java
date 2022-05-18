package one.yiran.db.common.service;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import one.yiran.db.common.annotation.CreateTimeAdvise;
import one.yiran.db.common.annotation.Search;
import one.yiran.db.common.annotation.UpdateTimeAdvise;
import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import one.yiran.common.exception.BusinessException;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.db.common.util.PredicateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class CrudBaseServiceImpl<K,T> implements CrudBaseService<K,T> {

    protected Type kClass;
    protected Type tClass;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected JPAQueryFactory queryFactory;

    public CrudBaseServiceImpl() {
        ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
        kClass = pt.getActualTypeArguments()[0];
        tClass = pt.getActualTypeArguments()[1];
    }

    @Override
    public PageModel<T> selectPage(PageRequest request, T target) {
        return selectPage(request, target, new ArrayList<>());
    }

    @Override
    public PageModel<T> selectPage(PageRequest request, T target, Predicate predicate) {
        List<T> results = selectList(request,target,predicate);
        long count = doCount(predicate);
        return PageModel.instance(count, results);
    }

    @Override
    public PageModel<T> selectPage(PageRequest request, T target, List<Predicate> pres) {
        List<T> results = selectList(request,target,pres);
        long count = count(request,target,pres);
        return PageModel.instance(count, results);
    }

    @Override
    public List<T> selectList(PageRequest request, T target, Predicate predicate) {
        return selectList(request,target,new ArrayList<Predicate>(){{add(predicate);}});
    }

    @Override
    public List<T> selectList(PageRequest request, T target, List<Predicate> predicates) {
        log.info("query list sql Entity:{},{}", tClass.getTypeName(),predicates);

        JPAQuery q = queryFactory.selectFrom(entityPath());
        if(predicates != null) {
            predicates.forEach(e->{
                q.where(e);
            });
        }

        if(request != null)
            PageRequestUtil.injectQuery(request,q);
        injectObject(target,q);

        return q.fetch();
    }

    @Override
    public List<T> selectList(PageRequest request, T target) {
        return selectList(request, target, new ArrayList<>());
    }

    @Override
    public List<T> selectList(T target) {
        return selectList(null,target);
    }

    @Override
    public List<T> selectList(Predicate predicate) {
        return selectList(new ArrayList<Predicate>(){{add(predicate);}});
    }

    @Override
    public List<T> selectList(List<Predicate> pres) {
        return selectList(null,null,pres);
    }

    private EntityPath entityPath(){
        String name  =  ((Class) tClass).getSimpleName();
        String newName = name.substring(0,1).toLowerCase() + name.substring(1);
        EntityPath<T> path = new EntityPathBase<T>((Class) tClass, newName);
        return path;
    }

    private void injectObject(T target, JPAQuery query) {
        List<Predicate> pres = exactFromObject(target);
        if(pres!=null && pres.size()>0){
            pres.forEach(e->{
                query.where(e);
            });
        }
    }

    @Override
    public long count(PageRequest request, T target) {
        return count(request,target,new ArrayList<>());
    }

    @Override
    public long count(Predicate predicate) {
        return doCount(predicate);
    }

    @Override
    public long count(List<Predicate> predicates) {
        return doCount(predicates);
    }

    @Override
    public long count() {
        return doCount(new ArrayList<>());
    }
    @Override
    public long count(PageRequest request, T target,Predicate predicate) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(predicate);
        return count(request,target,predicates);
    }

    @Override
    public long count(PageRequest request, T target, List<Predicate> predicates) {
        JPAQuery q = queryFactory.selectFrom(entityPath());

        if(request != null)
            PageRequestUtil.injectQuery(request,q);

        predicates.addAll(exactFromObject(target));
        return doCount(predicates);
    }

    private long doCount(Predicate predicate){
        return doCount(new ArrayList<Predicate>(){{
            add(predicate);
        }});
    }

    private long doCount(List<Predicate> predicates){
        log.info("query count sql Entity:{},{}",tClass.getTypeName(),predicates);
        JPAQuery query = queryFactory.selectFrom(entityPath());
        if (predicates != null) {
            predicates.forEach(e->{
                query.where(e);
            });
        }
        return query.fetchCount();
    }

    @Override
    public T selectByPId(K pId) {
        Assert.notNull(pId,"");
        String fieldName = doGetPrimaryId();

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery query = builder.createQuery((Class)tClass);
        Root root = query.from((Class)tClass);

        javax.persistence.criteria.Predicate pre1 = builder.equal(root.get(fieldName), pId);

        query.where(pre1);

        List<T> rs = entityManager.createQuery(query.select(root)).getResultList();
        if(rs == null || rs.size() == 0){
            return null;
        } else if(rs.size() > 1) {
            throw BusinessException.build("查询到多个结果");
        }
        return rs.get(0);

//        Specification specification = new Specification<T>() {
//            @Override
//            public javax.persistence.criteria.Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
//                List<javax.persistence.criteria.Predicate> predicateList = new ArrayList<>();
//                predicateList.add(cb.equal(root.get(fieldName).as(String.class), pId));
//                javax.persistence.criteria.Predicate[] pre = new javax.persistence.criteria.Predicate[predicateList.size()];
//                pre = predicateList.toArray(pre);
//                return query.where(pre).getRestriction();
//            }
//        };
    }

    @Override
    public List<T> selectAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery query = builder.createQuery((Class)tClass);
        Root root = query.from((Class)tClass);
        return entityManager.createQuery(query.select(root)).getResultList();
    }

    @Transactional
    @Override
    public long deleteByPId(K pId) throws BusinessException {
        T ent = entityManager.find((Class<T>) tClass, 1);
        entityManager.remove(ent);
        return 1;
    }

    @Transactional
    @Override
    public long deleteByPIds(K[] ids) throws BusinessException {
        Assert.notNull(ids,"");
        String fieldName = doGetPrimaryId();
        Class filedType = doGetPrimaryType();

        String sb = "";
        for(int i = 0; i <ids.length; i ++) {
            sb = sb + ":p" + i;
            if(i != ids.length-1) {
                sb = sb + ",";
            }
        }
        Query query = entityManager.createQuery(
                "update " + ((Class) tClass).getName() + " set isDelete = true WHERE  " + fieldName + " in (" + sb + ")");

        if (Long.class.isAssignableFrom(filedType)) {
            for(int i = 0; i <ids.length; i ++) {
                query.setParameter("p"+i, Long.valueOf(ids[i].toString()));
            }
        } else {
            for(int i = 0; i <ids.length; i ++) {
                query.setParameter("p"+i, ids[i]);
            }
        }

        int deletedCount = query.executeUpdate();
        return deletedCount;
    }

    @Transactional
    @Override
    public long deleteAll() {
        Query query = entityManager.createQuery(
                "update " + ((Class) tClass).getName() + " set isDelete = true");
        int deletedCount = query.executeUpdate();
        return deletedCount;
    }

    @Transactional
    @Override
    public T insert(T target) {
        String primaryId = doGetPrimaryId();
        processionAnnotations(target);
//        Field f = ReflectionUtils.findField((Class<T>) tClass, primaryId);
//        Object fv ;
//        try {
//            fv = FieldUtils.readField(f,target,true);
//        } catch (IllegalAccessException e) {
//            log.error("",e);
//            throw BusinessException.build("读取主键异常");
//        }

        entityManager.persist(target);
        return target;
    }

    @Transactional
    @Override
    public T update(T target) {
        processionAnnotations(target);
        return entityManager.merge(target);
    }

    /**
     * 物理删除
     * @param pId
     * @return
     */
    @Transactional
    @Override
    public long remove(K pId) {
        Assert.notNull(pId,"");
        String fieldName = doGetPrimaryId();

        Query query = entityManager.createQuery(
                "DELETE FROM  " + ((Class) tClass).getName() + " WHERE  " + fieldName +" = (:p)");
        int deletedCount = query.setParameter("p", pId).executeUpdate();
        return deletedCount;
    }

    /**
     * 物理删除
     * @param pIds
     * @return
     */
    @Transactional
    @Override
    public long remove(List<K> pIds) {
        Assert.notNull(pIds,"");
        String fieldName = doGetPrimaryId();

        Query query = entityManager.createQuery(
                "DELETE FROM  " + ((Class) tClass).getName() + " WHERE  " + fieldName +" in (:p)");
        int deletedCount = query.setParameter("p", pIds).executeUpdate();

        return deletedCount;
    }

    @Transactional
    @Override
    public long removeByPIds(K[] ids) throws BusinessException {
        Assert.notNull(ids,"");
        String fieldName = doGetPrimaryId();
        Class filedType = doGetPrimaryType();

        String sb = "";
        for(int i = 0; i <ids.length; i ++) {
            sb = sb + ":p" + i;
            if(i != ids.length-1) {
                sb = sb + ",";
            }
        }
        Query query = entityManager.createQuery(
                "DELETE FROM  " + ((Class) tClass).getName() + " WHERE  " + fieldName + " in (" + sb + ")");

        if (Long.class.isAssignableFrom(filedType)) {
            for(int i = 0; i <ids.length; i ++) {
                query.setParameter("p"+i, Long.valueOf(ids[i].toString()));
            }
        } else {
            for(int i = 0; i <ids.length; i ++) {
                query.setParameter("p"+i, ids[i]);
            }
        }
        int deletedCount = query.executeUpdate();
        return deletedCount;
    }

    @Transactional
    @Override
    public long removeAll() {
        Query query = entityManager.createQuery(
                "DELETE FROM  " + ((Class) tClass).getName());
        int deletedCount = query.executeUpdate();
        return deletedCount;
    }

    private String doGetPrimaryId(){
        Field[] fields = FieldUtils.getFieldsWithAnnotation((Class) tClass, Id.class);
        if(fields ==null || fields.length == 0)
            throw BusinessException.build("PrimaryId主键没有设置");

        String fieldName = fields[0].getName();
        return  fieldName;
    }

    private Class doGetPrimaryType(){
        Field[] fields = FieldUtils.getFieldsWithAnnotation((Class) tClass, Id.class);
        if(fields ==null || fields.length == 0)
            throw BusinessException.build("PrimaryId主键没有设置");

        Class zl = fields[0].getType();
        return  zl;
    }

    private void processionAnnotations(final Object source) {
        if(source == null)
            return;
        ReflectionUtils.doWithFields(source.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                ReflectionUtils.makeAccessible(field);
//                boolean isNew = false;
//                if (field.isAnnotationPresent(AutoId.class)) {
//                    if (!field.getType().equals(Long.class)) {
//                        throw BusinessException.build("AutoId field must be Long type");
//                    }
//                    if (field.get(source) == null || ((Long) field.get(source)).longValue() == 0) {
//                        field.set(source, mongoSequenceService.getNextId(source.getClass().getSimpleName() + "|" + field.getName()));
//                        isNew = true;
//                    }
//                }

                if (field.isAnnotationPresent(CreateTimeAdvise.class)) {
                    if (field.get(source) == null) {
                        field.set(source, new Date());
                    }
                }
                if (field.isAnnotationPresent(UpdateTimeAdvise.class)) {
                    field.set(source, new Date());
                }
            }
        });
    }

    private List<Predicate> exactFromObject(T target) {
        List<Predicate> predicates = new ArrayList<>();
        if (target != null) {
            List<Field> fields = FieldUtils.getAllFieldsList(target.getClass());
            for (Field f : fields) {
                Search search = f.getAnnotation(Search.class);
                if (search == null)
                    continue;
                String key = StringUtils.isNotEmpty(search.columnName()) ? search.columnName() : f.getName();
                Search.Op op = search.op();
                Object value = null;
                Class fieldType = null;
                try {
                    f.setAccessible(true);
                    fieldType = f.getType();
                    value = FieldUtils.readField(f, target);
                } catch (IllegalAccessException e) {
                    log.error("", e);
                }
                if (value instanceof String && StringUtils.isBlank(((String) value))) {
                    continue;
                }
                if (value != null) {
                    if (op.equals(Search.Op.IS)) {
                        if(key.equals("isDelete")) {
                            if (value instanceof Boolean) {
                                if(((Boolean) value).booleanValue()) {
                                    //PredicateUtil.addDeletePredicate(query);
                                    predicates.add(PredicateUtil.buildDeletePredicate());
                                } else if(!((Boolean) value).booleanValue()){
                                    //PredicateUtil.addNotDeletePredicate(query);
                                    predicates.add(PredicateUtil.buildNotDeletePredicate());
                                }
                            }
                        } else {
//                            if (value instanceof String)
                                predicates.add(PredicateUtil.buildPredicate(Ops.EQ,key,fieldType.cast(value)));
//                            else if (value instanceof Long)
//                                predicates.add(PredicateUtil.buildPredicate(Ops.EQ,key,Long.valueOf(value.toString())));
//                            else
//                                predicates.add(PredicateUtil.buildPredicate(Ops.EQ,key,value.toString().trim()));
                        }
                    } else if (op.equals(Search.Op.REGEX)) {
                        predicates.add(PredicateUtil.buildPredicate(Ops.LIKE,key,fieldType.cast(value)));
                    } else if (op.equals(Search.Op.IN)) {
                        if(value instanceof Number[]) {
                            Number[] numValue = (Number[]) value;
                            if(numValue.length > 0)
                                predicates.add(PredicateUtil.buildPredicate(Ops.IN, key,numValue));
                        }else if(value instanceof String[]) {
                            String[] strValue = (String[]) value;
                            if(strValue.length > 0)
                                predicates.add(PredicateUtil.buildPredicate(Ops.IN, key,strValue));
                        }
                    } else if (op.equals(Search.Op.GT)) {
                        predicates.add(PredicateUtil.buildPredicate(Ops.GT,key,fieldType.cast(value)));
                    } else if (op.equals(Search.Op.GTE)) {
                        if(value instanceof Number) {
                            Number numValue = (Number) value;
                            predicates.add(PredicateUtil.buildPredicate(Ops.GOE, key,numValue));
                        }
                    } else if (op.equals(Search.Op.LT)) {
                        if(value instanceof Number) {
                            Number numValue = (Number) value;
                            predicates.add(PredicateUtil.buildPredicate(Ops.LT, key,numValue));
                        }
                    } else if (op.equals(Search.Op.GTE)) {
                        if(value instanceof Number) {
                            Number numValue = (Number) value;
                            predicates.add(PredicateUtil.buildPredicate(Ops.LOE, key,numValue));
                        }
                    }
                }
            }
        }
        return predicates;
    }
}
