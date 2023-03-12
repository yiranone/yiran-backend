package one.yiran.dashboard.util;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.QTuple;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import one.yiran.common.domain.PageModel;
import one.yiran.common.domain.PageRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


import java.util.List;

import static org.springframework.data.domain.PageRequest.of;

public class PageableConvertUtil {

    public static Pageable toDataPageable(PageRequest request) {
        Sort sort = Sort.unsorted();
        if (!StringUtils.isEmpty(request.getOrderByColumn()) && !StringUtils.isEmpty(request.getOrderDirection())) {
            sort = Sort.by("asc".equalsIgnoreCase(request.getOrderDirection()) ? Sort.Direction.ASC: Sort.Direction.DESC,
                    request.getOrderByColumn());
        }

        return of(
                Long.valueOf(request.getPageNum()-1).intValue(),
                Long.valueOf(request.getPageSize()).intValue(), sort);
    }

    public static <T> PageModel<T> toPageModel(PageRequest request, JPAQuery<T> jpa) {
        if (request.getPageNum() == null || request.getPageNum() < 1)
            request.setPageNum(1L);
        if (request.getPageSize() == null || request.getPageSize() <= 0)
            request.setPageSize(10L);
        if (request.getPageSize() != null && request.getPageSize() > Long.MAX_VALUE)
            request.setPageSize(Long.MAX_VALUE);
        jpa.offset((request.getPageNum() - 1) * request.getPageSize()).limit(request.getPageSize());
        if (StringUtils.isNotBlank(request.getOrderByColumn())) {
            Expression expression = jpa.getMetadata().getProjection();
            if (expression instanceof QTuple) {
                expression = ((QTuple) expression).getArgs().get(0);
            }
            PathBuilder<T> entityPath = new PathBuilder(expression.getClass(), expression.toString());
            PathBuilder<Object> path = entityPath.get(request.getOrderByColumn());
            if(StringUtils.equalsIgnoreCase(request.getOrderDirection(),"DESC")) {
                jpa.orderBy(new OrderSpecifier(Order.DESC, path));
            } else {
                jpa.orderBy(new OrderSpecifier(Order.ASC, path));
            }
        }
        long count = jpa.fetchCount();
        List<T> datas = jpa.fetch();
        return PageModel.instance(count,datas);
    }
}
