package one.yiran.db.common.util;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanOperation;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import one.yiran.common.exception.BusinessException;
import one.yiran.common.util.TimeUtil;
import one.yiran.common.domain.PageRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

@Slf4j
public class PageRequestUtil {

    public static PageRequest fromRequestIgnorePageSize(HttpServletRequest request) {
        return doFromRequest(request,true);
    }

    public static PageRequest fromRequest(HttpServletRequest request) {
        return doFromRequest(request,false);
    }

    private static PageRequest doFromRequest(HttpServletRequest request, boolean ignorePageSize) {
        PageRequest pageRequest = new PageRequest();
        if(ignorePageSize) {
            pageRequest.setPageSize(null);
            pageRequest.setPageNum(null);
        } else {
            Long pageSize = ServletRequestUtil.getValueFromRequest(request,"pageSize",Long.class);
            if (pageSize != null) {
                pageRequest.pageSize(pageSize);
            }
            Long pageNum = ServletRequestUtil.getValueFromRequest(request,"pageNum",Long.class);
            if (pageNum != null) {
                pageRequest.pageNum(pageNum);
            }
        }
        String orderByColumn = ServletRequestUtil.getValueFromRequest(request,"orderByColumn",String.class);
        if (StringUtils.isNotBlank(orderByColumn)) {
            pageRequest.orderBy(orderByColumn);
        }
        String orderDirection = ServletRequestUtil.getValueFromRequest(request,"orderDirection",String.class);
        if (StringUtils.isNotBlank(orderDirection)) {
            pageRequest.orderDirection(orderDirection);
        }

        String status = ServletRequestUtil.getValueFromRequest(request,"status",String.class);
        if (StringUtils.isNotBlank(status)) {
            pageRequest.status(status);
        }

        String beginTime = ServletRequestUtil.getValueFromRequest(request,"beginCreateTime",String.class);
        if (StringUtils.isNotBlank(beginTime)) {
            pageRequest.beginDate(TimeUtil.parseDate_yyyyMMdd_hl(beginTime));
        }

        String endTime = ServletRequestUtil.getValueFromRequest(request,"endCreateTime",String.class);
        if (StringUtils.isNotBlank(endTime)) {
            pageRequest.endDate(TimeUtil.parseDate_yyyyMMdd_hl(endTime));
        }
        return pageRequest;
    }

    public static void injectQuery(PageRequest request, JPAQuery query) {
          injectQuery(request,query,null);
    }

    private static Path detectType(String name, Path<Type> defaultPath, EntityPath... paths){
        Path<Type> s = QClassUtil.searchFieldOrderPath(name,paths);
        if(s != null)
            return s;
        return defaultPath;
    }
    public static void injectQuery(PageRequest request, JPAQuery query, EntityPath<?>... alternativePaths) {

        boolean succ = request.alreadyInjectToQuery.compareAndSet(false, true);
        if (!succ)
            return;

        String simpleName = query.getType().getName();
        EntityPath<Type> defaultPath = null;
        if(alternativePaths == null) {
            defaultPath = QClassUtil.getPathByEntityName(simpleName);
        }

        if(StringUtils.isNotBlank(request.getStatus())){
            Path<Type> p = detectType("status",defaultPath,alternativePaths);
            Path<String> statusPath = Expressions.path(String.class, p, "status");
            if(statusPath != null)  {
                BooleanOperation bpre = Expressions.predicate(Ops.EQ, statusPath, Expressions.constant(request.getStatus()));
                query.where(bpre);
            }
        }
        if (request.getPageNum() != null && request.getPageNum() < 1)
            request.setPageNum(1L);
        if (request.getPageSize() != null && request.getPageSize() <= 0)
            request.setPageSize(10L);
        if (request.getPageSize() != null && request.getPageSize() > Long.MAX_VALUE)
            request.setPageSize(Long.MAX_VALUE);
        if(request.getPageNum() != null && request.getPageSize() != null)
            query.offset((request.getPageNum() -1 ) * request.getPageSize());
        if(request.getPageSize() != null)
            query.limit(request.getPageSize());

        if (StringUtils.isNotBlank(request.getOrderByColumn())) {
            Path orderPath = QClassUtil.getFieldPathByEntityName(simpleName, request.getOrderByColumn());
            orderPath = detectType(request.getOrderByColumn(),orderPath,alternativePaths);
            if(orderPath == null){
                throw BusinessException.build("参数请求异常，排序字段"+request.getOrderByColumn()+"不支持");
            }
            if(StringUtils.isNotBlank(request.getOrderDirection())){
                if (request.getOrderDirection().equalsIgnoreCase("DESC")) {
                    query.orderBy(new OrderSpecifier(Order.DESC,orderPath));
                } else {
                    query.orderBy(new OrderSpecifier(Order.ASC,orderPath));
                }
            } else {
                query.orderBy(new OrderSpecifier(Order.ASC,orderPath));
                //Expressions.stringPath(p,request.getOrderByColumn())
            }
         }
    }
}
