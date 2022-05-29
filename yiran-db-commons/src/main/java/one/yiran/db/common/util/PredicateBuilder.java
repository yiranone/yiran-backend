package one.yiran.db.common.util;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import one.yiran.db.common.annotation.Search;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PredicateBuilder {

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder extends ArrayList<Predicate> {
         public Builder addEqualIfNotBlank(SimpleExpression path, Object value){
            if (value != null) {
                add(path.eq(value));
            }
            return this;
         }
        public Builder addLikeIfNotBlank(StringPath path, String value){
            if (value != null) {
                add(path.like("%" + value + "%"));
            }
            return this;
        }
        public Builder addExpression(BooleanExpression path){
             add(path);
            return this;
        }

        public Builder addEntityByAnnotation(Object target, EntityPath path) {
            List<Predicate> predicates = this;
            if (target != null) {
                List<Field> fields = FieldUtils.getAllFieldsList(target.getClass());
                for (Field f : fields) {
                    //处理实体对象有Search注解的字段
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
                                        predicates.add(PredicateUtil.buildDeletePredicate(path));
                                    } else if(!((Boolean) value).booleanValue()){
                                        //PredicateUtil.addNotDeletePredicate(query);
                                        predicates.add(PredicateUtil.buildNotDeletePredicate(path));
                                    }
                                }
                            } else {
//                            if (value instanceof String)
                                predicates.add(PredicateUtil.buildPredicate(Ops.EQ,path,key,fieldType.cast(value)));
//                            else if (value instanceof Long)
//                                predicates.add(PredicateUtil.buildPredicate(Ops.EQ,key,Long.valueOf(value.toString())));
//                            else
//                                predicates.add(PredicateUtil.buildPredicate(Ops.EQ,key,value.toString().trim()));
                            }
                        } else if (op.equals(Search.Op.REGEX)) {
                            predicates.add(PredicateUtil.buildPredicate(Ops.LIKE,path,key,fieldType.cast(value)));
                        } else if (op.equals(Search.Op.IN)) {
                            if(value instanceof Number[]) {
                                Number[] numValue = (Number[]) value;
                                if(numValue.length > 0)
                                    predicates.add(PredicateUtil.buildPredicate(Ops.IN,path, key,numValue));
                            }else if(value instanceof String[]) {
                                String[] strValue = (String[]) value;
                                if(strValue.length > 0)
                                    predicates.add(PredicateUtil.buildPredicate(Ops.IN,path, key,strValue));
                            }
                        } else if (op.equals(Search.Op.GT)) {
                            predicates.add(PredicateUtil.buildPredicate(Ops.GT,path,key,fieldType.cast(value)));
                        } else if (op.equals(Search.Op.GTE)) {
                            if(value instanceof Number) {
                                Number numValue = (Number) value;
                                predicates.add(PredicateUtil.buildPredicate(Ops.GOE,path, key,numValue));
                            }
                        } else if (op.equals(Search.Op.LT)) {
                            if(value instanceof Number) {
                                Number numValue = (Number) value;
                                predicates.add(PredicateUtil.buildPredicate(Ops.LT,path, key,numValue));
                            }
                        } else if (op.equals(Search.Op.GTE)) {
                            if(value instanceof Number) {
                                Number numValue = (Number) value;
                                predicates.add(PredicateUtil.buildPredicate(Ops.LOE,path, key,numValue));
                            }
                        }
                    }
                }
            }
            return this;
        }

         public List<Predicate> toList() {
            return this;
        }
        public  Predicate[] toArray() {
            return toArray(new Predicate[]{});
        }
    }
}
