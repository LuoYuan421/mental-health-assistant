package org.example.aispingboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.entity.*;
import org.example.aispingboot.mapper.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/data-analytics")
public class DataAnalyticsController {
    @Resource
    private UserMapper userMapper;

    @Resource
    private ConsultationSessionMapper consultationSessionMapper;

    @Resource
    private ConsultationMessageMapper consultationMessageMapper;

    @Resource
    private EmotionDiaryMapper emotionDiaryMapper;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new LinkedHashMap<>();

        // 总用户数
        long totalUsers = userMapper.selectCount(null);

        // 总咨询会话数
        long totalSessions = consultationSessionMapper.selectCount(null);

        // 总消息数
        long totalMessages = consultationMessageMapper.selectCount(null);

        // 情绪日记数
        long totalDiaries = emotionDiaryMapper.selectCount(null);

        data.put("totalUsers", totalUsers);
        data.put("totalSessions", totalSessions);
        data.put("totalMessages", totalMessages);
        data.put("totalDiaries", totalDiaries);

        // 最近7天情绪趋势
        List<Map<String, Object>> emotionTrend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LambdaQueryWrapper<EmotionDiary> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(EmotionDiary::getDiaryDate, date)
                    .le(EmotionDiary::getDiaryDate, date);
            List<EmotionDiary> diaries = emotionDiaryMapper.selectList(wrapper);
            double avgScore = diaries.stream()
                    .filter(d -> d.getMoodScore() != null)
                    .mapToInt(EmotionDiary::getMoodScore)
                    .average()
                    .orElse(0);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", date.format(formatter));
            point.put("avgScore", Math.round(avgScore * 10.0) / 10.0);
            point.put("count", diaries.size());
            emotionTrend.add(point);
        }
        data.put("emotionTrend", emotionTrend);

        // 最近7天咨询趋势
        List<Map<String, Object>> sessionTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LambdaQueryWrapper<ConsultationSession> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(ConsultationSession::getStartedAt, date.atStartOfDay())
                    .le(ConsultationSession::getStartedAt, date.atTime(LocalTime.MAX));
            long count = consultationSessionMapper.selectCount(wrapper);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", date.format(formatter));
            point.put("count", count);
            sessionTrend.add(point);
        }
        data.put("sessionTrend", sessionTrend);

        return Result.ok(data);
    }
}
