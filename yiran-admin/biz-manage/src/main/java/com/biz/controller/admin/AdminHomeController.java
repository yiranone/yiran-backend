package com.biz.controller.admin;

import one.yiran.dashboard.entity.QSysAnalysisItem;
import one.yiran.dashboard.entity.SysAnalysisItem;
import com.biz.entity.QMember;
import one.yiran.dashboard.service.SysAnalysisItemService;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiChannel;
import one.yiran.dashboard.vo.ChannelVO;
import one.yiran.db.common.util.PredicateBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@AjaxWrapper
@Controller
@RequestMapping("/biz/home")
public class AdminHomeController {

    @Autowired
    private SysAnalysisItemService analysisItemService;

    @RequestMapping("/member/count")
    public Map<String,Object> memberCount(@ApiChannel(required = false) ChannelVO channelVO, HttpServletRequest request) {
        Long channelId = null;
        if(channelVO != null)
            channelId = channelVO.getChannelId();

        QSysAnalysisItem qAnalysisItem = QSysAnalysisItem.sysAnalysisItem;
        Map<String,Object> result = new HashMap<>();

        LocalDate nowDate = LocalDate.now();
        LocalDate yesterdayDate = nowDate.plusDays(-1);
        LocalDate lastweekDate = yesterdayDate.plusDays(-7);

        List<SysAnalysisItem> lists = analysisItemService.selectList(PredicateBuilder.builder().addEqualIfNotBlank(qAnalysisItem.channelId, channelId)
                .addExpression(qAnalysisItem.type.in(SysAnalysisItem.TYPE_MEMBER))
                .addExpression(qAnalysisItem.belongDate.in(nowDate, yesterdayDate,lastweekDate)).toList());

        long totalRegisterCount = getMapValue(lists, nowDate, SysAnalysisItem.TYPE_MEMBER,"TotalRegisterCount");
        long todayRegisterCount = getMapValue(lists, nowDate, SysAnalysisItem.TYPE_MEMBER,"RegisterCount");
        long lastDayRegisterCount = getMapValue(lists, yesterdayDate, SysAnalysisItem.TYPE_MEMBER,"RegisterCount");
        long lastLastDayRegisterCount = getMapValue(lists, yesterdayDate.minusDays(-1), SysAnalysisItem.TYPE_MEMBER,"RegisterCount");
        long lastWeekRegisterCount = getMapValue(lists, lastweekDate, SysAnalysisItem.TYPE_MEMBER,"RegisterCount");
        BigDecimal lastDayPercent = lastLastDayRegisterCount == 0 ? (lastDayRegisterCount == 0 ? BigDecimal.ZERO : new BigDecimal("100")) :
                (new BigDecimal(lastDayRegisterCount).subtract(new BigDecimal(lastLastDayRegisterCount))).divide( new BigDecimal( lastLastDayRegisterCount), 2, RoundingMode.HALF_DOWN).multiply(new BigDecimal(100));
        BigDecimal lastWeekPercent = lastWeekRegisterCount == 0 ? (lastDayRegisterCount == 0 ? BigDecimal.ZERO : new BigDecimal("100")) :
                (new BigDecimal(lastDayRegisterCount).subtract(new BigDecimal(lastWeekRegisterCount))).divide( new BigDecimal( lastWeekRegisterCount), 2, RoundingMode.HALF_DOWN).multiply(new BigDecimal(100));

        result.put("totalRegisterCount",totalRegisterCount);
        result.put("todayRegisterCount",todayRegisterCount);
        result.put("lastDayRegisterCount",lastDayRegisterCount);
        result.put("lastLastDayRegisterCount",lastLastDayRegisterCount);
        result.put("lastDayPercent", lastDayPercent);
        result.put("lastWeekPercent",lastWeekPercent);


        //登陆用户
        LocalDate twoWeeksAgoDate = nowDate.plusDays(-14);
        lists = analysisItemService.selectList(PredicateBuilder.builder().addEqualIfNotBlank(qAnalysisItem.channelId, channelId)
                .addExpression(qAnalysisItem.type.in(SysAnalysisItem.TYPE_MEMBER))
                .addExpression(qAnalysisItem.keyName.in("LoginCount","RegisterCount","TwoWeeksLoginCount"))
                .addExpression(qAnalysisItem.belongDate.between(twoWeeksAgoDate, nowDate)).toList());
        long totalLoginCount = 0;
        List<Long> loginCounts = new ArrayList<>();
        List<String> loginCountDates = new ArrayList<>();
        for (LocalDate s = twoWeeksAgoDate;s.compareTo(nowDate) <=0; s = s.plusDays(1)) {
            long v = getMapValue(lists,s, SysAnalysisItem.TYPE_MEMBER,"LoginCount");
            totalLoginCount += v;
            loginCounts.add(v);
            loginCountDates.add(s.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        result.put("loginCountY",loginCounts);
        result.put("loginCountX",loginCountDates);
        result.put("averageLoginCount",totalLoginCount/loginCounts.size());

        long twoWeeksLoginCount = getMapValue(lists, nowDate, SysAnalysisItem.TYPE_MEMBER,"TwoWeeksLoginCount");
        result.put("twoWeeksLoginCount",twoWeeksLoginCount); //活跃用户


        long twoWeeksRegisterCount = 0;
        List<Long> registerCounts = new ArrayList<>();
        List<String> registerCountDates = new ArrayList<>();
        for (LocalDate s = twoWeeksAgoDate;s.compareTo(nowDate) <=0; s = s.plusDays(1)) {
            long v = getMapValue(lists,s, SysAnalysisItem.TYPE_MEMBER,"RegisterCount");
            twoWeeksRegisterCount += v;
            registerCounts.add(v);
            registerCountDates.add(s.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        result.put("registerCountY",registerCounts);
        result.put("registerCountX",registerCountDates);
        result.put("averageRegisterCount",twoWeeksRegisterCount/loginCounts.size());
        result.put("twoWeeksRegisterCount",twoWeeksRegisterCount); //

        return result;
    }

    private long getMapValue(List<SysAnalysisItem> lists, LocalDate belongDate, String type, String keyName){
        for(SysAnalysisItem ai:lists) {
            boolean belongDateEq = false;
            if (belongDate == null && ai.getBelongDate() == null ||
                    ai.getBelongDate() != null && belongDate!= null && ai.getBelongDate().isEqual(belongDate)) {
                belongDateEq = true;
            }
            if(belongDateEq && StringUtils.equals(type,ai.getType()) && StringUtils.equals(ai.getKeyName(),keyName)) {
                return Long.valueOf(ai.getValue());
            }
        }
        return 0;
    }
}
