package org.example.aispingboot.controller;

import cn.hutool.json.JSONUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.aispingboot.AiService.PsychologicalSupportService;
import org.example.aispingboot.AiService.StructOutPut;
import org.example.aispingboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.aispingboot.DTO.command.ConsultationStreamDTO;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.common.ResultCode;
import org.example.aispingboot.entity.ConsultationMessage;
import org.example.aispingboot.entity.ConsultationSession;
import org.example.aispingboot.mapper.ConsultationMessageMapper;
import org.example.aispingboot.mapper.ConsultationSessionMapper;
import org.example.aispingboot.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Fragment;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/psychological-chat")
public class PsychologicalChat {
    @Autowired
    private PsychologicalSupportService psychologicalSupportService;

    @Resource
    private ConsultationSessionMapper consultationSessionMapper;

    @Resource
    private ConsultationMessageMapper consultationMessageMapper;

    @PostMapping("/session/start")
    public Result<StructOutPut.StreamChatSession> startSession(@Valid @RequestBody ConsultationSessionCreateDTO createDTO) {
        // 获取当前用户
        String token = JwtTokenUtil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        StructOutPut.StreamChatSession session = psychologicalSupportService.startSession(userId, createDTO);
        return Result.ok(session);
    }

    // 获取当前用户ID的工具方法
    private Long getCurrentUserId() {
        String token = JwtTokenUtil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        return jwt.getClaim("userId").asLong();
    }

    // 查询当前用户的会话列表
    @GetMapping("/sessions")
    public Result<Object> getSessionList(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getCurrentUserId();
        Page<ConsultationSession> page = new Page<>(currentPage, size);
        LambdaQueryWrapper<ConsultationSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationSession::getUserId, userId)
                .orderByDesc(ConsultationSession::getStartedAt);
        Page<ConsultationSession> result = consultationSessionMapper.selectPage(page, wrapper);
        return Result.ok(result);
    }

    // 查询会话的消息详情
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ConsultationMessage>> getSessionDetail(@PathVariable Long sessionId) {
        Long userId = getCurrentUserId();
        // 验证会话属于当前用户
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return Result.error(ResultCode.ERROR.getCode(), "会话不存在", null);
        }
        LambdaQueryWrapper<ConsultationMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationMessage::getSessionId, sessionId)
                .orderByAsc(ConsultationMessage::getCreatedAt);
        List<ConsultationMessage> messages = consultationMessageMapper.selectList(wrapper);
        return Result.ok(messages);
    }

    // 删除会话
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId) {
        Long userId = getCurrentUserId();
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return Result.error(ResultCode.ERROR.getCode(), "会话不存在", null);
        }
        // 删除会话下的所有消息
        LambdaQueryWrapper<ConsultationMessage> msgWrapper = new LambdaQueryWrapper<>();
        msgWrapper.eq(ConsultationMessage::getSessionId, sessionId);
        consultationMessageMapper.delete(msgWrapper);
        // 删除会话
        consultationSessionMapper.deleteById(sessionId);
        return Result.ok();
    }

    // 获取会话情绪分析
    @GetMapping("/session/{sessionId}/emotion")
    public Result<Map<String, Object>> getSessionEmotion(@PathVariable String sessionId) {
        Long userId = getCurrentUserId();
        // 支持 "session_7" 格式，提取数字部分
        Long numericId;
        if (sessionId.startsWith("session_")) {
            numericId = Long.parseLong(sessionId.substring("session_".length()));
        } else {
            numericId = Long.parseLong(sessionId);
        }
        ConsultationSession session = consultationSessionMapper.selectById(numericId);
        if (session == null || !session.getUserId().equals(userId)) {
            return Result.error(ResultCode.ERROR.getCode(), "会话不存在", null);
        }
        // 返回会话的情绪分析结果
        Map<String, Object> emotionData = Map.of(
                "sessionId", sessionId,
                "emotionAnalysis", session.getLastEmotionAnalysis() != null ? session.getLastEmotionAnalysis() : "{}",
                "updatedAt", session.getLastEmotionUpdatedAt() != null ? session.getLastEmotionUpdatedAt().toString() : ""
        );
        return Result.ok(emotionData);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody ConsultationStreamDTO streamDTO) {
        // 获取当前用户
        String token = JwtTokenUtil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();

        if (userId == null) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data(JSONUtil.toJsonStr(Result.error(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg(), "用户未登录")))
                    .build());
        }

        // 开始流式对话
        return psychologicalSupportService.streamPsychologicalChat(streamDTO.getSessionId(), streamDTO.getUserMessage())
                .map(Fragment -> {
                    return ServerSentEvent.<String>builder()
                            .event("message")
                            .data(JSONUtil.toJsonStr(Result.ok(Map.of("content", Fragment, "type", "normal"))))
                            .build();
                })
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("{}")
                        .build()
                ))
                .delayElements(Duration.ofMillis(50)); // 添加延迟确保流式数据的体验
    }
}
