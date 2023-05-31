package com.biz.dao;

import com.biz.entity.Member;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;

@Repository
public interface MemberDao extends BaseDao<Member, Long> {

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT t FROM Member t WHERE t.memberId = ?1")
    Member lockById(Long id);

    List<Member> findAllByChannelId(Long channelId);

}
