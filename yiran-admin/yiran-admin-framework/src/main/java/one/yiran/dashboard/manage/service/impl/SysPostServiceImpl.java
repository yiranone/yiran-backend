package one.yiran.dashboard.manage.service.impl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import one.yiran.dashboard.manage.dao.UserPostDao;
import one.yiran.dashboard.manage.entity.*;
import one.yiran.dashboard.manage.entity.*;
import one.yiran.dashboard.manage.service.SysUserService;
import one.yiran.dashboard.manage.dao.PostDao;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.manage.service.SysPostService;
import one.yiran.dashboard.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class SysPostServiceImpl extends CrudBaseServiceImpl<Long, SysPost> implements SysPostService{

    @Autowired
    private PostDao postDao;

    @Autowired
    private UserPostDao userPostDao;

    @Autowired
    private SysUserService userService;
    /**
     * 根据用户ID查询岗位
     *
     * @param userId 用户ID
     * @return 岗位列表
     */
    @Override
    public List<SysPost> selectPostsByUserId(Long userId) {
        List<SysPost> userSysPosts = doSelectPostsByUserId(userId);
        List<SysPost> sysPosts = postDao.findAll();
        for (SysPost sysPost : sysPosts) {
            for (SysPost userRole : userSysPosts) {
                if (sysPost.getPostId().longValue() == userRole.getPostId().longValue()) {
                    sysPost.setFlag(true);
                    break;
                }
            }
        }
        return sysPosts;
    }

    /**
     * 批量删除岗位信息
     *
     * @param ids 需要删除的数据ID
     * @throws Exception
     */
    @Transactional
    @Override
    public long deleteByPIds(Long[] ids) throws BusinessException {
        //校验下
        Long[] postIds = ids;
        for (Long postId : postIds) {
            SysPost sysPost = selectByPId(postId);
            if (countUserPostByPostId(postId) > 0) {
                List<SysUser> sysUsers = doSelectUserByPostId(postId,30);
                List<String> userNames = sysUsers.stream().map(e->e.getLoginName()).collect(Collectors.toList());
                throw BusinessException.build(StringUtil.format("岗位[{}]已分配给{},不能删除", sysPost.getPostName(),userNames));
            }
        }

        return super.deleteByPIds(ids);
    }

    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param postId 岗位ID
     * @return 结果
     */
    @Override
    public long countUserPostByPostId(Long postId) {
        return doCountUserPostByPostId(postId);
    }

    @Override
    public List<SysUser> selectUserByPostId(Long postId, long limit) {
        return doSelectUserByPostId(postId,limit);
    }

    /**
     * 校验岗位名称是否唯一
     *
     * @param sysPost 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostNameUnique(SysPost sysPost) {
        Long postId = sysPost.getPostId() == null ? -1L : sysPost.getPostId();
        SysPost info = postDao.findByPostName(sysPost.getPostName());
        if (info != null && info.getPostId().longValue() != postId.longValue()) {
            return false;
        }
        return true;
    }

    /**
     * 校验岗位编码是否唯一
     *
     * @param sysPost 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostCodeUnique(SysPost sysPost) {
        Long postId = sysPost.getPostId() == null ? -1L : sysPost.getPostId();
        SysPost info = postDao.findByPostCode(sysPost.getPostCode());
        if (info != null && info.getPostId().longValue() != postId.longValue()) {
            return false;
        }
        return true;
    }

    @Override
    public long deleteAuthUsers(Long userId) {
        Assert.notNull(userId,"");
        return userPostDao.deleteAllByUserId(userId);
    }

    @Transactional
    @Override
    public long removePostInfo(Long[] ids) {
        Long[] postIds = ids;
        for (Long postId : postIds) {
            SysPost sysPost = selectByPId(postId);
            List<SysUser> userPosts = doSelectUserByPostId(postId, 10);
            if (userPosts!= null && userPosts.size() > 0) {
                List<String> loginNames = userPosts.stream().map(e->e.getLoginName()).collect(Collectors.toList());
                throw BusinessException.build("[" + sysPost.getPostName() + "]已分配给用户"+loginNames+"不能删除");
            }
        }
        return removeByPIds(ids);
    }

    private List<SysPost> doSelectPostsByUserId(Long userId) {
        QSysPost qSysPost = QSysPost.sysPost;
        QSysUserPost qSysUserPost = QSysUserPost.sysUserPost;
        JPAQuery<SysPost> jpa = queryFactory.selectFrom(qSysPost).innerJoin(qSysUserPost)
                .on(qSysPost.postId.eq(qSysUserPost.postId).and(qSysUserPost.userId.eq(userId)))
                .where(qSysPost.isDelete.ne(Boolean.TRUE));
        jpa.orderBy(new OrderSpecifier(Order.ASC,qSysPost.postSort));
        return jpa.fetch();
    }

    private long doCountUserPostByPostId(Long postId) {
        return userPostDao.count(QSysUserPost.sysUserPost.postId.eq(postId));
    }

    private List<SysUser> doSelectUserByPostId(Long postId,long limit) {
        Assert.notNull(postId,"");
        QSysUser qSysUser = QSysUser.sysUser;
        QSysUserPost qSysUserPost = QSysUserPost.sysUserPost;
        JPAQuery<SysUser> jpa = queryFactory.selectFrom(qSysUser).innerJoin(qSysUserPost)
                .on(qSysUser.userId.eq(qSysUserPost.userId).and(qSysUserPost.postId.eq(postId)));
        jpa.limit(limit);
        return jpa.fetch();
    }

    private long doCountUserPostByUserId(Long userId) {
        return userPostDao.count(QSysUserPost.sysUserPost.userId.eq(userId));
    }

}
