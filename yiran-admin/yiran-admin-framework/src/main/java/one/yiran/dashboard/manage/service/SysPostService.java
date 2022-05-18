package one.yiran.dashboard.manage.service;

import one.yiran.db.common.service.CrudBaseService;
import one.yiran.dashboard.manage.entity.SysPost;
import one.yiran.dashboard.manage.entity.SysUser;

import java.util.List;

public interface SysPostService extends CrudBaseService<Long,SysPost> {


    /**
     * 根据用户ID查询岗位
     *
     * @param userId 用户ID
     * @return 岗位列表
     */
    public List<SysPost> selectPostsByUserId(Long userId);


    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param postId 岗位ID
     * @return 结果
     */
    public long countUserPostByPostId(Long postId);


    /**
     * 查询这个岗位下面有哪些用户
     * @param postId
     * @return
     */
    List<SysUser> selectUserByPostId(Long postId, long limit);

    public boolean checkPostNameUnique(SysPost sysPost);

    public boolean checkPostCodeUnique(SysPost sysPost);

    long deleteAuthUsers(Long userId);

    long removePostInfo(Long[] ids);
}
