package org.example.aispingboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.aispingboot.DTO.command.EmotionDiaryCreateDTO;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.entity.EmotionDiary;
import org.example.aispingboot.mapper.EmotionDiaryMapper;
import org.example.aispingboot.service.EmotionDiaryService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/emotion-diary")
public class EmotionDiaryController {
    @Resource
    private EmotionDiaryService emotionDiaryService;

    @Resource
    private EmotionDiaryMapper emotionDiaryMapper;

    // 提交情绪日记
    @PostMapping
    public Result<Map<String, Object>> create(@Valid @RequestBody EmotionDiaryCreateDTO dto) {
        EmotionDiary diary = emotionDiaryService.createDiary(dto);
        // 自动触发AI分析
        Map<String, Object> aiAnalysis = emotionDiaryService.analyzeDiary(
                dto.getDiaryContent(), dto.getDominantEmotion(), dto.getMoodScore());
        Map<String, Object> result = Map.of(
                "diary", diary,
                "aiAnalysis", aiAnalysis
        );
        return Result.ok(result);
    }

    // 用户日记历史列表
    @GetMapping("/list")
    public Result<Object> getUserDiaryList(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<EmotionDiary> page = emotionDiaryService.getUserDiaryPage(currentPage, size);
        return Result.ok(page);
    }

    // 情绪趋势数据
    @GetMapping("/trend")
    public Result<Map<String, Object>> getEmotionTrend(
            @RequestParam(defaultValue = "7") int days) {
        Map<String, Object> trend = emotionDiaryService.getEmotionTrend(days);
        return Result.ok(trend);
    }

    // AI情绪分析（单独调用）
    @PostMapping("/analyze")
    public Result<Map<String, Object>> analyzeDiary(@RequestBody Map<String, Object> body) {
        String content = (String) body.getOrDefault("diaryContent", "");
        String emotion = (String) body.getOrDefault("dominantEmotion", "中性");
        Integer score = (Integer) body.getOrDefault("moodScore", 5);
        Map<String, Object> analysis = emotionDiaryService.analyzeDiary(content, emotion, score);
        return Result.ok(analysis);
    }

    // 后台管理-分页查询情绪日记
    @GetMapping("/admin/page")
    public Result<Object> adminPage(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<EmotionDiary> page = new Page<>(currentPage, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EmotionDiary> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.orderByDesc(EmotionDiary::getCreatedAt);
        Page<EmotionDiary> result = emotionDiaryMapper.selectPage(page, wrapper);
        return Result.ok(result);
    }

    // 后台管理-删除情绪日记
    @DeleteMapping("/admin/{id}")
    public Result<Void> adminDelete(@PathVariable Long id) {
        emotionDiaryMapper.deleteById(id);
        return Result.ok();
    }
}
