package com.biz.job;

import one.yiran.dashboard.dao.AnalysisItemDao;
import one.yiran.dashboard.entity.QSysAnalysisItem;
import one.yiran.dashboard.entity.SysAnalysisItem;
import com.biz.entity.QMember;
import one.yiran.dashboard.service.SysAnalysisItemService;
import com.biz.service.MemberService;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import one.yiran.common.util.DateUtil;
import one.yiran.dashboard.common.constants.SystemConstants;
import one.yiran.dashboard.entity.QSysLoginInfo;
import one.yiran.dashboard.entity.SysChannel;
import one.yiran.dashboard.service.SysChannelService;
import one.yiran.dashboard.service.SysLoginInfoService;
import one.yiran.db.common.util.PredicateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * 统计最新注册用户，活跃用户
 */
@ConditionalOnProperty(name = "dashboard.job.enable", havingValue = "true")
@Component
@Slf4j
public class AnalysisTask implements ApplicationRunner {

    @Autowired
    private MemberService memberService;

    @Autowired
    private SysChannelService sysChannelService;

    @Autowired
    private SysAnalysisItemService analysisItemService;

    @Autowired
    private SysLoginInfoService sysLoginInfoService;

    @Autowired
    private AnalysisItemDao analysisItemDao;

    @Autowired
    protected JPAQueryFactory queryFactory;

    @Scheduled(cron = "10 */10 * * * ?")
    public void excuteTask() {
        log.info("汇总用户注册活跃数量 start");
        long start = System.currentTimeMillis();
        refresh();
        long gap = System.currentTimeMillis() - start;
        log.info("汇总用户注册活跃数量 end 耗时:{}ms",gap);
    }

    private void refresh() {

        QMember qM = QMember.member;
        QSysLoginInfo qSysLoginInfo = QSysLoginInfo.sysLoginInfo;
        LocalDate nowDate = LocalDate.now();
        Date todayStartDate = DateUtil.toDate(nowDate);

        //汇总所有的 注册
        long totalRegisterCountAll = memberService.count(PredicateBuilder.builder().toList());
        long todayRegisterCountAll = memberService.count(PredicateBuilder.builder().addExpression(qM.registerTime.goe(todayStartDate)).toList());
        long lastDayRegisterCountAll = memberService.count(PredicateBuilder.builder().addExpression(qM.registerTime.between(DateUtil.addDays(todayStartDate,-1), todayStartDate)).toList());
        saveMemberAnalysis(null, nowDate, SysAnalysisItem.TYPE_MEMBER,"TotalRegisterCount", totalRegisterCountAll + "");
        saveMemberAnalysis(null, nowDate, SysAnalysisItem.TYPE_MEMBER, "RegisterCount", todayRegisterCountAll + "");
        saveMemberAnalysis(null, nowDate.minusDays(1), SysAnalysisItem.TYPE_MEMBER, "RegisterCount", lastDayRegisterCountAll + "");
        //汇总所有的 登陆

        long todayLoginCountAll = queryFactory.selectFrom(qM).innerJoin(qSysLoginInfo)
                .on(qSysLoginInfo.categoryId.eq(qM.memberId)
                        .and(qSysLoginInfo.category.eq("Member"))
                        .and(qSysLoginInfo.createTime.goe(todayStartDate))
                        .and(qSysLoginInfo.status.in(SystemConstants.LOGIN_SUCCESS,SystemConstants.LOGIN_FAIL)))
                .distinct().fetchCount();

        long lastDayLoginCountAll = queryFactory.selectFrom(qM).innerJoin(qSysLoginInfo)
                .on(qSysLoginInfo.categoryId.eq(qM.memberId)
                        .and(qSysLoginInfo.category.eq("Member"))
                        .and(qSysLoginInfo.createTime.between(DateUtil.addDays(todayStartDate,-1), todayStartDate))
                        .and(qSysLoginInfo.status.in(SystemConstants.LOGIN_SUCCESS,SystemConstants.LOGIN_FAIL)))
                .distinct().fetchCount();

        long inTwoWeeksLoginCountAll = queryFactory.selectFrom(qM).innerJoin(qSysLoginInfo)
                .on(qSysLoginInfo.categoryId.eq(qM.memberId)
                        .and(qSysLoginInfo.category.eq("Member"))
                        .and(qSysLoginInfo.createTime.goe(DateUtil.addDays(todayStartDate,-14)))
                        .and(qSysLoginInfo.status.in(SystemConstants.LOGIN_SUCCESS,SystemConstants.LOGIN_FAIL)))
                .distinct().fetchCount();

        saveMemberAnalysis(null, nowDate, SysAnalysisItem.TYPE_MEMBER, "LoginCount", todayLoginCountAll + "");
        saveMemberAnalysis(null, nowDate.minusDays(1), SysAnalysisItem.TYPE_MEMBER, "LoginCount", lastDayLoginCountAll + "");
        saveMemberAnalysis(null, nowDate, SysAnalysisItem.TYPE_MEMBER, "TwoWeeksLoginCount", inTwoWeeksLoginCountAll + "");


        //按照渠道汇总
        List<SysChannel> partnerChannelList = sysChannelService.selectAll();
        partnerChannelList.stream().forEach(partner -> {
            Long channelId = partner.getChannelId();
            long totalRegisterCount = memberService.count(PredicateBuilder.builder().addEqualIfNotBlank(qM.channelId,channelId).toList());
            long todayRegisterCount = memberService.count(PredicateBuilder.builder().addEqualIfNotBlank(qM.channelId,channelId).addExpression(qM.registerTime.goe(todayStartDate)).toList());
            long lastDayRegisterCount = memberService.count(PredicateBuilder.builder().addEqualIfNotBlank(qM.channelId,channelId).addExpression(qM.registerTime.between(DateUtil.addDays(todayStartDate,-1), todayStartDate)).toList());
            long lastLastDayRegisterCount = memberService.count(PredicateBuilder.builder().addEqualIfNotBlank(qM.channelId,channelId).addExpression(qM.registerTime.between(DateUtil.addDays(todayStartDate,-2), DateUtil.addDays(todayStartDate,-1))).toList());
            long lastWeekRegisterCount = memberService.count(PredicateBuilder.builder().addEqualIfNotBlank(qM.channelId,channelId).addExpression(qM.registerTime.between(DateUtil.addDays(todayStartDate,-9), DateUtil.addDays(todayStartDate,-8))).toList());

            saveMemberAnalysis(channelId, nowDate, SysAnalysisItem.TYPE_MEMBER,"TotalRegisterCount", totalRegisterCount + "");
            saveMemberAnalysis(channelId, nowDate, SysAnalysisItem.TYPE_MEMBER, "RegisterCount", todayRegisterCount + "");
            saveMemberAnalysis(channelId, nowDate.minusDays(1), SysAnalysisItem.TYPE_MEMBER, "RegisterCount", lastDayRegisterCount + "");
            saveMemberAnalysis(channelId, nowDate.minusDays(2), SysAnalysisItem.TYPE_MEMBER, "RegisterCount", lastLastDayRegisterCount + "");

            long todayLoginCount = queryFactory.selectFrom(qM).innerJoin(qSysLoginInfo)
                    .on(qSysLoginInfo.categoryId.eq(qM.memberId).and(qSysLoginInfo.category.eq("Member"))
                            .and(qSysLoginInfo.createTime.goe(todayStartDate))
                            .and(qSysLoginInfo.status.in(SystemConstants.LOGIN_SUCCESS,SystemConstants.LOGIN_FAIL)))
                    .where(qM.channelId.eq(channelId))
                    .distinct().fetchCount();
            long lastDayLoginCount = queryFactory.selectFrom(qM).innerJoin(qSysLoginInfo)
                    .on(qSysLoginInfo.categoryId.eq(qM.memberId).and(qSysLoginInfo.category.eq("Member"))
                            .and(qSysLoginInfo.createTime.between(DateUtil.addDays(todayStartDate,-1), todayStartDate))
                            .and(qSysLoginInfo.status.in(SystemConstants.LOGIN_SUCCESS,SystemConstants.LOGIN_FAIL)))
                    .where(qM.channelId.eq(channelId))
                    .distinct().fetchCount();

            long inTwoWeeksLoginCount = queryFactory.selectFrom(qM).innerJoin(qSysLoginInfo)
                    .on(qSysLoginInfo.categoryId.eq(qM.memberId)
                            .and(qSysLoginInfo.category.eq("Member"))
                            .and(qSysLoginInfo.createTime.goe(DateUtil.addDays(todayStartDate,-14)))
                            .and(qSysLoginInfo.status.in(SystemConstants.LOGIN_SUCCESS,SystemConstants.LOGIN_FAIL)))
                    .where(qM.channelId.eq(channelId))
                    .distinct().fetchCount();

            saveMemberAnalysis(channelId, nowDate, SysAnalysisItem.TYPE_MEMBER, "LoginCount", todayLoginCount + "");
            saveMemberAnalysis(channelId, nowDate.minusDays(1), SysAnalysisItem.TYPE_MEMBER, "LoginCount", lastDayLoginCount + "");
            saveMemberAnalysis(channelId, nowDate, SysAnalysisItem.TYPE_MEMBER, "TwoWeeksLoginCount", inTwoWeeksLoginCount + "");
            // end of channelId loop
        });
    }
    private void saveMemberAnalysis(Long channelId, LocalDate date, String type, String key , String value){
        analysisItemService.insertOrUpdate(channelId,date,type,key,value);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        refresh();
    }
}
