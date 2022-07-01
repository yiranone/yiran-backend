package com.biz.dao;

import com.biz.entity.Member;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberDao extends BaseDao<Member, Long> {
}
