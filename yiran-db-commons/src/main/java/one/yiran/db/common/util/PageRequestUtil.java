package one.yiran.db.common.util;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanOperation;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import one.yiran.common.util.TimeUtil;
import one.yiran.common.domain.PageRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;

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

        boolean succ = request.alreadyInjectToQuery.compareAndSet(false, true);
        if (!succ)
            return;

        Path<Type> pathEntity = Expressions.path(query.getType(), query.getType().getSimpleName());

        if(StringUtils.isNotBlank(request.getStatus())){
//            Path<String> statusPath = Expressions.path(String.class, pathEntity, "status");
            Path<String> statusPath = Expressions.stringPath("status");
            BooleanOperation bpre = Expressions.predicate(Ops.EQ, statusPath, Expressions.constant(request.getStatus()));
            query.where(bpre);
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
            if(StringUtils.isNotBlank(request.getOrderDirection())){
                if (request.getOrderDirection().equals("DESC")) {
                    query.orderBy(new OrderSpecifier(Order.DESC,Expressions.stringPath(request.getOrderByColumn())));
                } else {
                    query.orderBy(new OrderSpecifier(Order.ASC,Expressions.stringPath(request.getOrderByColumn())));
                }
            } else {
                query.orderBy(new OrderSpecifier(Order.ASC,Expressions.stringPath(request.getOrderByColumn())));
            }
         }
    }


    public static void injectQuery(PageRequest request, Predicate predicate) {

        boolean succ  = request.alreadyInjectToQuery.compareAndSet(false,true);
        if(!succ)
            return;



        //if (StringUtils.isNotBlank(status)) {
        //    query.addCriteria(Criteria.where("status").is(status));
        //}

//        if (request.getBeginCreateTime() != null && request.getEndCreateTime() != null) {
//            //query.addCriteria(Criteria.where("createTime").gte(request.getBeginCreateTime()).lte(request.getEndCreateTime() ));
//            CriteriaUtil.addCriteria(query,Criteria.where("createTime").gte(request.getBeginCreateTime()).lte(request.getEndCreateTime() ));
//        } else if (request.getBeginCreateTime() != null) {
//            //query.addCriteria(Criteria.where("createTime").gte(request.getBeginCreateTime()));
//            CriteriaUtil.addCriteria(query,Criteria.where("createTime").gte(request.getBeginCreateTime()));
//        } else if (request.getEndCreateTime()  != null) {
//            //query.addCriteria(Criteria.where("createTime").lte(request.getEndCreateTime() ));
//            CriteriaUtil.addCriteria(query,Criteria.where("createTime").lte(request.getEndCreateTime() ));
//        }
//
//        if (StringUtils.isNotBlank(request.getOrderByColumn())) {
//            query.with(
//                    new Sort(
//                            Sort.Direction.fromOptionalString(request.getOrderDirection()).orElse(Sort.Direction.ASC), request.getOrderByColumn()));
//        }
//        if (request.getPageNum() < 1)
//            request.setPageNum(1);
//        if (request.getPageSize() <= 0)
//            request.setPageSize(10);
//        if (request.getPageSize() > Integer.MAX_VALUE)
//            request.setPageSize(Integer.MAX_VALUE);
//        Pageable pageableRequest = org.springframework.data.domain.PageRequest.of((int) request.getPageNum() - 1, (int) request.getPageSize());
//        query.with(pageableRequest);
//
//        if(request.getDepts() != null && request.getDepts().size() > 0) {
//            try {
//                Field cField = FieldUtils.getField(query.getClass(), "criteria",true);
//                cField.setAccessible(true);
//                Object v = cField.get(query);
//                Map<String, CriteriaDefinition> mq = (Map<String, CriteriaDefinition> )v;
//
//                Criteria reqDeptCri = null;
//                if (mq != null) {
//                    Criteria[] criterias = mq.values().toArray(new Criteria[]{});
//                    for(Criteria c : criterias){
//                        if(StringUtils.equals(c.getKey(),"deptId")) {
//                            reqDeptCri = c;
//                            break;
//                        }
//                    }
//                }
//
//                if(reqDeptCri != null) {
//                    //reqDeptCri.andOperator()
//                    //Long[] xx = new Long[]{34L,35L};
//                    //reqDeptCri.getCriteriaObject();
//                    //reqDeptCri.in(xx);
//                    reqDeptCri.andOperator(Criteria.where("deptId").in(request.getDepts()));
//                    log.info("inject query:{}",query);
//                } else {
//                    Criteria addCri = Criteria.where("deptId").in(request.getDepts());
//                    query.addCriteria(addCri);
//                }
//
//
//                //log.info("{}",mq);
//
//            } catch (Exception e) {
//                log.error("",e);
//            }
//        }

    }

//    public static void injectAggregation(PageRequest request, List<AggregationOperation> aggregationOperations) {
//        boolean succ  = request.alreadyInjectToQuery.compareAndSet(false,true);
//        //if(!succ)
//        //    return;
//
//        if (StringUtils.isNotBlank(request.getStatus())) {
//            aggregationOperations.add(Aggregation.match(Criteria.where("status").is(request.getStatus())));
//        }
//        Date beginDate = request.getBeginCreateTime();
//        Date endDate = request.getEndCreateTime();
//        if (beginDate != null && endDate != null) {
//            aggregationOperations.add(Aggregation.match(Criteria.where("createTime").gte(beginDate).lte(endDate)));
//        } else if (beginDate != null) {
//            aggregationOperations.add(Aggregation.match(Criteria.where("createTime").gte(beginDate)));
//        } else if (endDate != null) {
//            aggregationOperations.add(Aggregation.match(Criteria.where("createTime").lte(endDate)));
//        }
//        injectAggregationOnlyPage(request, aggregationOperations);
//    }
//
//    public static void injectAggregationOnlyPage(PageRequest request, List<AggregationOperation> aggregationOperations) {
//        boolean succ  = request.alreadyInjectToQuery.compareAndSet(false,true);
//        //if(!succ)
//        //    return;
//
//        if (StringUtils.isNotBlank(request.getOrderByColumn())) {
//            aggregationOperations.add(Aggregation.sort(
//                    new Sort(Sort.Direction.fromOptionalString(request.getOrderDirection()).orElse(Sort.Direction.ASC), request.getOrderByColumn())));
//        }
//        long pageNum = request.getPageNum();
//        long pageSize = request.getPageSize();
//        if (request.getPageNum() < 1)
//            pageNum = 1;
//        if (pageSize > Integer.MAX_VALUE)
//            pageSize = Integer.MAX_VALUE;
//        Pageable pageableRequest = org.springframework.data.domain.PageRequest.of((int) pageNum - 1, (int) pageSize);
//        aggregationOperations.add(Aggregation.skip((pageNum - 1) * pageSize));
//        aggregationOperations.add(Aggregation.limit(pageSize));
//
//        if(request.getDepts() != null && request.getDepts().size() > 0) {
//            aggregationOperations.add(Aggregation.match(Criteria.where("deptId").in(request.getDepts())));
//        }
//    }
}
