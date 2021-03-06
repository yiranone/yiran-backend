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
                boxes.add(new PredicateBox(path, Ops.GOE, value, false));
            return this;
        }

        public Builder addLittlerOrEqualIfNotBlank(Path path, Object value) {
            if (value != null)
                boxes.add(new PredicateBox(path, Ops.LOE, value, false));
            return this;
        }

        public Builder addEqualIfNotBlank(Path path, Object value) {
            if (value != null)
                boxes.add(new PredicateBox(path, Ops.EQ, value, false));
            return this;
        }

        public Builder addLikeIfNotBlank(StringPath path, String value) {
            if (StringUtils.isNotBlank(value))
                boxes.add(new PredicateBox(path, Ops.LIKE, "%" + value + "%", false));
            return this;
        }

        public Builder addExpression(BooleanExpression e) {
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
                                boxes.add(new PredicateBox(pathValue, Ops.EQ, value, false));
                            }
                        } else if (op.equals(Search.Op.REGEX)) {
                            boxes.add(new PredicateBox(pathValue, Ops.LIKE, value, false));
                        } else if (op.equals(Search.Op.IN)) {
                            boxes.add(new PredicateBox(pathValue, Ops.IN, value, false));
                        } else if (op.equals(Search.Op.GT)) {
                            boxes.add(new PredicateBox(pathValue, Ops.GT, value, false));
                        } else if (op.equals(Search.Op.GTE)) {
                            boxes.add(new PredicateBox(pathValue, Ops.GOE, value, false));
                        } else if (op.equals(Search.Op.LT)) {
                            boxes.add(new PredicateBox(pathValue, Ops.LOE, value, false));
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

        private List<Predicate> convert() {
            List<Predicate> list = new ArrayList<>();
            for (PredicateBox b : boxes) {
                Path path = b.getPath();
                Ops ops = b.getOps();
                Object value = b.getValue();
                boolean allowNull = b.isAllowNull();
                if (value != null) {
                    if (ops == Ops.GOE) {
                        if (path instanceof DateExpression && value instanceof LocalDate) {
                            list.add(((DateExpression) path).goe((LocalDate) value));
                        } else if (path instanceof NumberExpression && value instanceof Number) {
                            list.add(((NumberExpression) path).goe((Number) value));
                        } else {
                            log.error("Predicate构建异常 path={},ops={} value{}", path, ops, value);
                            throw new RuntimeException("Predicate构建异常");
                        }
                    } else if (ops == Ops.LOE) {
                        if (path instanceof DateExpression && value instanceof LocalDate) {
                            list.add(((DateExpression) path).loe((LocalDate) value));
                        } else if (path instanceof NumberExpression && value instanceof Number) {
                            list.add(((NumberExpression) path).loe((Number) value));
                        } else {
                            log.error("Predicate构建异常 path={},ops={} value{}", path, ops, value);
                            throw new RuntimeException("Predicate构建异常");
                        }
                    } else if (ops == Ops.GT) {
                        if (path instanceof DateExpression && value instanceof LocalDate) {
                            list.add(((DateExpression) path).gt((LocalDate) value));
                        } else if (path instanceof NumberExpression && value instanceof Number) {
                            list.add(((NumberExpression) path).gt((Number) value));
                        } else {
                            log.error("Predicate构建异常 path={},ops={} value{}", path, ops, value);
                            throw new RuntimeException("Predicate构建异常");
                        }
                    } else if (ops == Ops.LT) {
                        if (path instanceof DateExpression && value instanceof LocalDate) {
                            list.add(((DateExpression) path).lt((LocalDate) value));
                        } else if (path instanceof NumberExpression && value instanceof Number) {
                            list.add(((NumberExpression) path).lt((Number) value));
                        } else {
                            log.error("Predicate构建异常 path={},ops={} value{}", path, ops, value);
                            throw new RuntimeException("Predicate构建异常");
                        }
                    } else if (ops == Ops.EQ) {
                        if (path instanceof SimpleExpression && value instanceof String) {
                            list.add(((SimpleExpression) path).eq((String) value));
                        } else if (path instanceof NumberExpression && value instanceof Number) {
                            list.add(((NumberExpression) path).eq((Number) value));
                        } else if (path instanceof BooleanExpression && value instanceof Boolean) {
                            list.add(((BooleanExpression) path).eq((Boolean) value));
                        } else {
                            log.error("Predicate构建异常 path={},ops={} value{}", path, ops, value);
                            throw new RuntimeException("Predicate构建异常");
                        }
                    } else if (ops == Ops.IN) {
                        if (value instanceof Number[]) {
                            Number[] numValue = (Number[]) value;
                            if (numValue.length > 0)
                                list.add(((SimpleExpression) path).in(numValue));
                        } else if (value instanceof String[]) {
                            String[] strValue = (String[]) value;
                            if (strValue.length > 0)
                                list.add(((SimpleExpression) path).in(strValue));
                        }
                    } else if (ops == Ops.LIKE) {
                        if (path instanceof StringExpression && value instanceof String) {
                            if (StringUtils.isNotBlank((String) value))
                                list.add(((StringExpression) path).like((String) value));
                        }
                    } else {
                        log.error("ops还不支持 path={},ops={} value{}", path, ops, value);
                        throw new RuntimeException("ops还不支持");
                    }
                }
            }
            return list;
        }


    }
}
