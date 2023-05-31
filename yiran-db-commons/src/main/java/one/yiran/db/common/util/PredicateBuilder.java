package one.yiran.db.common.util;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.yiran.db.common.annotation.Search;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class PredicateBuilder {

    public static Builder builder() {
        return new Builder();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class PredicateBox {
        private Path path;
        private Ops ops;
        private Object value;
        private boolean allowNull;  //value = null 的时候是否增加
    }

    public static class Builder {

        private List<PredicateBox> boxes = new ArrayList<>();
        private List<Predicate> predicates = new ArrayList<>();

        public Builder addGreaterOrEqualIfNotBlank(Path path, Object value) {
            if (value != null)
                doAddOPredict(path, value, Ops.GOE);
            return this;
        }

        public Builder addLittlerOrEqualIfNotBlank(Path path, Object value) {
            if (value != null)
                doAddOPredict(path, value, Ops.LOE);
            return this;
        }

        public Builder addEqualIfNotBlank(Path path, Object value) {
            if(value instanceof String ) {
                if(StringUtils.isNotBlank((String)value))
                    doAddOPredict(path, value, Ops.EQ);
            } else if (value != null) {
                doAddOPredict(path, value, Ops.EQ);
            }
            return this;
        }

        public Builder addEqual(Path path, Object value) {
            if (value == null) {
                doAddOPredict(path, null, Ops.EQ, true);
            } else {
                addEqualIfNotBlank(path,value);
            }
            return this;
        }

        private void doAddOPredict(Path path, Object value, Ops eq) {
            boxes.add(new PredicateBox(path, eq, value, false));
        }

        private void doAddOPredict(Path path, Object value, Ops eq, boolean allowNull) {
            boxes.add(new PredicateBox(path, eq, value, allowNull));
        }

        public Builder addLikeIfNotBlank(StringPath path, String value) {
            if (StringUtils.isNotBlank(value))
                doAddOPredict(path, "%" + value + "%", Ops.LIKE);
            return this;
        }

        public Builder addExpression(BooleanExpression e) {
            predicates.add(e);
            return this;
        }

        public Builder addEqualOrNullExpression(Path path, Object value){
            BooleanOperation bpre1 = Expressions.predicate(Ops.EQ, path, Expressions.constant(value));
            BooleanOperation bpre2 = Expressions.predicate(Ops.IS_NULL, path);
            BooleanExpression e = bpre1.or(bpre2);
            predicates.add(e);
            return this;
        }

        public Builder addEntityByAnnotation(Object target, EntityPath path) {
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
                        Field pathField = FieldUtils.getDeclaredField(path.getClass(), f.getName(), true);
                        if(pathField == null) {
                            log.info("path:{} 不存在 field:{}",path.getClass(),f.getName());
                            continue;
                        }
                        Path pathValue = null;
                        try {
                            pathValue = (Path) FieldUtils.readField(pathField, path);
                        } catch (IllegalAccessException e) {
                            log.error("系统错误", e);
                            continue;
                        }

                        if (op.equals(Search.Op.IS)) {
                            if (key.equals("isDelete")) {
                                if (value instanceof Boolean) {
                                    if (((Boolean) value).booleanValue()) {
                                        predicates.add(PredicateUtil.buildDeletePredicate(path));
                                    } else if (!((Boolean) value).booleanValue()) {
                                        //PredicateUtil.addNotDeletePredicate(query);
                                        predicates.add(PredicateUtil.buildNotDeletePredicate(path));
                                    }
                                }
                            } else {
                                doAddOPredict(pathValue, value, Ops.EQ);
                            }
                        } else if (op.equals(Search.Op.REGEX)) {
                            doAddOPredict(pathValue, "%" + value + "%", Ops.LIKE);
                        } else if (op.equals(Search.Op.IN)) {
                            doAddOPredict(pathValue, value, Ops.IN);
                        } else if (op.equals(Search.Op.GT)) {
                            doAddOPredict(pathValue, value, Ops.GT);
                        } else if (op.equals(Search.Op.GTE)) {
                            doAddOPredict(pathValue, value, Ops.GOE);
                        } else if (op.equals(Search.Op.LT)) {
                            doAddOPredict(pathValue, value, Ops.LOE);
                        }
                    }
                }
            }
            return this;
        }

        public List<Predicate> toList() {
            List<Predicate> cvs = convert();
            cvs.addAll(predicates);
            return cvs;
        }

        public Predicate[] toArray() {
            return toList().toArray(new Predicate[]{});
        }

        public Predicate toPredicate() {
            List<Predicate> cvs = toList();
            if (cvs == null || cvs.size() == 0) {
                return null;
            }
            BooleanExpression booleanExpression = null;
            for (Predicate p : cvs) {
                if (booleanExpression == null) {
                    booleanExpression = (BooleanOperation)p;
                } else {
                    booleanExpression = booleanExpression.and(p);
                }
            }
            return booleanExpression;
        }

        private List<Predicate> convert() {
            List<Predicate> list = new ArrayList<>();
            for (PredicateBox b : boxes) {
                Path path = b.getPath();
                Ops ops = b.getOps();
                Object value = b.getValue();
                boolean allowNull = b.isAllowNull();
                if (value != null) {
                    if (ops == Ops.GOE) {
                        if (path instanceof ComparableExpression) {
                            if (value instanceof Comparable) {
                                list.add(((ComparableExpression) path).goe((Comparable) value));
                            } else {
                                log.error("Predicate Comparable 构建异常 path={},ops={} value{}", path, ops, value);
                                throw new RuntimeException("Predicate构建异常");
                            }
                        } else {
                            log.error("类型不支持了，Predicate ComparableExpression 构建异常 path={},ops={} value{}", path, ops, value);
                            throw new RuntimeException("Predicate构建异常");
                        }
                    } else if (ops == Ops.LOE) {
                        if (path instanceof ComparableExpression) {
                            if (value instanceof Comparable) {
                                list.add(((ComparableExpression) path).loe((Comparable) value));
                            } else {
                                log.error("Predicate Comparable 构建异常 path={},ops={} value{}", path, ops, value);
                                throw new RuntimeException("Predicate构建异常");
                            }
                        } else {
                            log.error("Predicate ComparableExpression 构建异常 path={},ops={} value{}", path, ops, value);
                            throw new RuntimeException("Predicate构建异常");
                        }
                    } else if (ops == Ops.GT) {
                        if (path instanceof ComparableExpression) {
                            if (value instanceof Comparable) {
                                list.add(((ComparableExpression) path).gt((Comparable) value));
                            } else {
                                log.error("Predicate Comparable gt 构建异常 path={},ops={} value{}", path, ops, value);
                                throw new RuntimeException("Predicate构建异常");
                            }
                        } else {
                            log.error("类型不支持了，Predicate ComparableExpression gt构建异常 path={},ops={} value{}", path, ops, value);
                            throw new RuntimeException("Predicate构建异常");
                        }
                    } else if (ops == Ops.LT) {
                        if (path instanceof ComparableExpression) {
                            if (value instanceof Comparable) {
                                list.add(((ComparableExpression) path).lt((Comparable) value));
                            } else {
                                log.error("Predicate Comparable lt 构建异常 path={},ops={} value{}", path, ops, value);
                                throw new RuntimeException("Predicate构建异常");
                            }
                        } else {
                            log.error("类型不支持了，Predicate ComparableExpression lt构建异常 path={},ops={} value{}", path, ops, value);
                            throw new RuntimeException("Predicate构建异常");
                        }
                    } else if (ops == Ops.EQ) {
                        if (path instanceof SimpleExpression) {
                            list.add(((SimpleExpression) path).eq(value));
                        } else {
                            log.error("Predicate构建异常 path={},ops={} value{}", path, ops, value);
                            throw new RuntimeException("Predicate构建异常");
                        }
                    } else if (ops == Ops.IN) {
                        if (path instanceof SimpleExpression && (value instanceof Number[] || value instanceof String[])) {
                            list.add(((SimpleExpression) path).in(value));
                        } else {
                            log.error("Predicate in 构建异常 path={},ops={} value{}", path, ops, value);
                            throw new RuntimeException("Predicate构建异常");
                        }
                    } else if (ops == Ops.LIKE) {
                        if (path instanceof StringExpression && value instanceof String) {
                            if (StringUtils.isNotBlank((String) value))
                                list.add(((StringExpression) path).like((String) value));
                        } else {
                            log.error("Predicate like 构建异常 path={},ops={} value{}", path, ops, value);
                            throw new RuntimeException("Predicate构建异常");
                        }
                    } else {
                        log.error("ops还不支持 path={},ops={} value{}", path, ops, value);
                        throw new RuntimeException("ops还不支持");
                    }
                } else {
                    //value == null
                    if (allowNull) {
                        list.add(((SimpleExpression) path).isNull());
                    } else {
                        log.error("ops还不支持 path={},ops={} value is null,but not allow", path, ops);
                        throw new RuntimeException("value is null,but not allow");
                    }
                }
            }
            return list;
        }


    }
}
