package org.example.aispingboot.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aispingboot.DTO.command.EmotionDiaryCreateDTO;
import org.example.aispingboot.entity.EmotionDiary;
import org.example.aispingboot.mapper.EmotionDiaryMapper;
import org.example.aispingboot.util.JwtTokenUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmotionDiaryService {
    @Autowired
    private EmotionDiaryMapper emotionDiaryMapper;

    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;

    // 获取当前用户ID
    private Long getCurrentUserId() {
        String token = JwtTokenUtil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        return jwt.getClaim("userId").asLong();
    }

    public EmotionDiary createDiary(EmotionDiaryCreateDTO dto) {
        Long userId = getCurrentUserId();
        EmotionDiary diary = EmotionDiary.builder()
                .userId(userId)
                .diaryDate(dto.getDiaryDate())
                .moodScore(dto.getMoodScore())
                .dominantEmotion(dto.getDominantEmotion())
                .emotionTriggers(dto.getEmotionTriggers())
                .diaryContent(dto.getDiaryContent())
                .sleepQuality(dto.getSleepQuality())
                .stressLevel(dto.getStressLevel())
                .build();
        emotionDiaryMapper.insert(diary);
        return diary;
    }

    // 用户日记列表（分页）
    public Page<EmotionDiary> getUserDiaryPage(Integer currentPage, Integer size) {
        Long userId = getCurrentUserId();
        Page<EmotionDiary> page = new Page<>(currentPage, size);
        LambdaQueryWrapper<EmotionDiary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmotionDiary::getUserId, userId)
                .orderByDesc(EmotionDiary::getDiaryDate)
                .orderByDesc(EmotionDiary::getCreatedAt);
        return emotionDiaryMapper.selectPage(page, wrapper);
    }

    // AI情绪分析
    public Map<String, Object> analyzeDiary(String diaryContent, String dominantEmotion, Integer moodScore) {
        String prompt = String.format(
                "请根据以下日记内容进行简短的情绪分析（2-3句话），给出温暖的回应和一个具体的小建议。\n" +
                "用户情绪: %s，情绪评分: %d/10\n" +
                "日记内容: %s\n" +
                "请用JSON格式返回，包含：analysis(分析), suggestion(建议), positive(正面点)",
                dominantEmotion, moodScore,
                diaryContent != null && !diaryContent.isEmpty() ? diaryContent : "（无具体内容）"
        );

        try {
            String response = chatClient.prompt().user(prompt).call().content();
            // 解析JSON响应
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("analysis", response);
            result.put("dominantEmotion", dominantEmotion);
            result.put("moodScore", moodScore);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("analysis", "记录情绪是一种很好的自我关爱方式，继续保持！");
            result.put("suggestion", "试着在今天做一件让自己开心的小事");
            result.put("dominantEmotion", dominantEmotion);
            result.put("moodScore", moodScore);
            return result;
        }
    }

    // 情绪趋势数据（最近N天）
    public Map<String, Object> getEmotionTrend(int days) {
        Long userId = getCurrentUserId();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        LambdaQueryWrapper<EmotionDiary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmotionDiary::getUserId, userId)
                .ge(EmotionDiary::getDiaryDate, startDate)
                .le(EmotionDiary::getDiaryDate, endDate)
                .orderByAsc(EmotionDiary::getDiaryDate);
        List<EmotionDiary> diaries = emotionDiaryMapper.selectList(wrapper);

        // 按日期分组
        Map<LocalDate, List<EmotionDiary>> grouped = diaries.stream()
                .collect(Collectors.groupingBy(EmotionDiary::getDiaryDate));

        List<Map<String, Object>> trendData = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", date.toString());
            List<EmotionDiary> dayDiaries = grouped.getOrDefault(date, List.of());
            if (!dayDiaries.isEmpty()) {
                double avgScore = dayDiaries.stream()
                        .mapToInt(EmotionDiary::getMoodScore)
                        .average().orElse(0);
                point.put("moodScore", Math.round(avgScore * 10.0) / 10.0);
                point.put("count", dayDiaries.size());
                // 主要情绪取众数
                String topEmotion = dayDiaries.stream()
                        .collect(Collectors.groupingBy(EmotionDiary::getDominantEmotion, Collectors.counting()))
                        .entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey).orElse("无");
                point.put("dominantEmotion", topEmotion);
            } else {
                point.put("moodScore", null);
                point.put("count", 0);
                point.put("dominantEmotion", null);
            }
            trendData.add(point);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("trend", trendData);
        result.put("totalDays", days);
        result.put("recordedDays", (int) grouped.keySet().stream().filter(d -> !grouped.get(d).isEmpty()).count());
        return result;
    }
}
