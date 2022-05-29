package one.yiran.db.common.util;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanOperation;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PredicateUtil {

    public static void addDeletePredicate(JPAQuery query){
        query.where(buildDeletePredicate());
    }

    public static BooleanExpression buildDeletePredicate(){
        Path<Boolean> statusPath = Expressions.booleanPath("isDelete");
        BooleanOperation bpre1 = Expressions.predicate(Ops.EQ, statusPath, Expressions.constant(Boolean.FALSE));
        BooleanOperation bpre2 = Expressions.predicate(Ops.IS_NOT_NULL, statusPath);
        BooleanExpression bpre = bpre1.or(bpre2);
        return bpre;
     }

    public static BooleanExpression buildDeletePredicate(EntityPath path){
        Path<Boolean> statusPath = Expressions.booleanPath(path,"isDelete");
        BooleanOperation bpre1 = Expressions.predicate(Ops.EQ, statusPath, Expressions.constant(Boolean.FALSE));
        BooleanOperation bpre2 = Expressions.predicate(Ops.IS_NOT_NULL, statusPath);
        BooleanExpression bpre = bpre1.or(bpre2);
        return bpre;
    }

    public static BooleanExpression buildNotDeletePredicate(EntityPath path){
        Path<Boolean> statusPath = Expressions.booleanPath(path, "isDelete");
        BooleanOperation bpre1 = Expressions.predicate(Ops.EQ, statusPath, Expressions.constant(Boolean.FALSE));
        BooleanOperation bpre2 = Expressions.predicate(Ops.IS_NULL, statusPath);
        BooleanExpression bpre = bpre1.or(bpre2);
        return bpre;
    }

    public static void addPredicate(String name, String value, JPAQuery query){
        addPredicate(Ops.EQ,name,value,query);
    }

    public static void addPredicate(String name, Long value, JPAQuery query){
        addPredicate(Ops.EQ,name,value,query);
    }

    public static void addPredicate(String name, Boolean value, JPAQuery query){
        addPredicate(Ops.EQ,name,value,query);
    }

    public static void addPredicate(Ops ops, String name, Object value, JPAQuery query){
            Path statusPath = Expressions.stringPath(name);
            BooleanOperation bpre = Expressions.predicate(ops, statusPath, Expressions.constant(value));
            query.where(bpre);
    }

    public static BooleanOperation buildPredicate(Ops ops, EntityPath path, String name, Object value){
        Path statusPath = Expressions.stringPath(path, name);
        BooleanOperation bpre = Expressions.predicate(ops, statusPath, Expressions.constant(value));
        return bpre;
    }

    public static BooleanOperation buildPredicate(Ops ops, String name, Object value){
        return buildPredicate(ops,null,name,value);
    }

    public static BooleanExpression buildEqualPredicate(String key, Object value){
        return buildPredicate(Ops.EQ,key,value);
    }

}
