package one.yiran.db.common.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseDao<T, ID> extends JpaRepository<T, ID>, QuerydslPredicateExecutor<T> {

}
