package org.example.aispingboot.AiService;

import org.example.aispingboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.aispingboot.entity.ConsultationSession;
import org.example.aispingboot.service.ConsultationMessageService;
import org.example.aispingboot.service.ConsultationSessionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import cn.hutool.json.JSONUtil;
import org.example.aispingboot.mapper.ConsultationSessionMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class PsychologicalSupportService {
    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private ConsultationSessionService consultationSessionService;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;

    public StructOutPut.StreamChatSession startSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        // 创建数据库会话记录
        ConsultationSession consultationSession = consultationSessionService.createSession(userId, createDTO);
        // 注意：用户消息由 streamPsychologicalChat 统一保存，避免重复存储

        // 创建会话信息
        String sessionId = "session_" + consultationSession.getId();
        return new StructOutPut.StreamChatSession(
                sessionId,
                userId,
                createDTO.getInitialMessage(),
                System.currentTimeMillis(),
                System.currentTimeMillis() + 86400000L, // 24小时
                1,
                "ACTIVE"
        );
    }

    public Flux<String> streamPsychologicalChat(String sessionId, String userMessage) {
        // 创建响应流
        return Flux.create(sink -> {
            // sink.next("数据1") // 发布数据
            // sink.complete(); // 完成流
            // sink.error(exception); // 发布错误
            Long dbSessionId = extractSessionId(sessionId);
            if (dbSessionId == null) {
                sink.error(new RuntimeException("会话ID格式错误"));
                return;
            }
            // 保存用户消息到数据库（消息只在此处保存，避免重复）
            consultationMessageService.saveUserMessage(dbSessionId, userMessage, null);

            // 进行流式对话
            // 生成对话记忆管理
            String conversationId = "conversation_" + sessionId;
            // 构建系统提示词
            List<Message> userMessages = new ArrayList<>();
            userMessages.add(new UserMessage(userMessage));
            chatMemory.add(conversationId, userMessages);
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(PromptManage.PSYCHOLOGICAL_SUPPORT_SYSTEM_PROMPT)
            ));

            //用于存储AI完成的响应
            StringBuilder fullResponse = new StringBuilder();

            // 使用chatClient发送消息到Open AI
            chatClient.prompt(prompt)
                    .user(userMessage)
                    .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .stream()
                    .content()
                    .doOnNext(Fragment -> {
                        fullResponse.append(Fragment);
                        sink.next(Fragment);
                    })
                    .doOnComplete(() -> {
                        String completeRes = fullResponse.toString();
                        // 将AI返回的内容保存到数据库
                        consultationMessageService.saveAimessage(dbSessionId, completeRes, "openai");
                        // 添加AI回复到chatMemory
                        List<Message> aiMessages = new ArrayList<>();
                        aiMessages.add(new AssistantMessage(completeRes));
                        chatMemory.add(conversationId, aiMessages);

                        // 先关闭SSE流，再异步做情绪分析（不阻塞前端）
                        sink.complete();

                        CompletableFuture.runAsync(() -> analyzeAndSaveEmotion(dbSessionId, userMessage, completeRes));
                    })
                    .doOnError(error -> {
                        sink.error(error);
                    })
                    .subscribe(); // 订阅并启动流
        });
    }

    // 获取参数中的sessionId
    public Long extractSessionId(String sessionId) {
        if (sessionId != null && sessionId.startsWith("session_")) {
            String idStr = sessionId.substring("session_".length());
            return Long.parseLong(idStr);
        }
        return null;
    }

    // 分析对话情绪并保存（同步，doOnComplete中调用）
    public void analyzeAndSaveEmotion(Long sessionId, String userMessage, String aiResponse) {
        try {
            System.out.println("=== 情绪分析开始，sessionId=" + sessionId + " ===");
            String emotionPrompt = String.format(
                    "请分析以下对话的情绪状态，返回JSON格式（不要markdown包裹）：\n" +
                    "用户说：\"%s\"\n" +
                    "AI回复：\"%s\"\n" +
                    "请返回：{\"primaryEmotion\":\"主要情绪(如：焦虑/开心/悲伤/平静/愤怒)\"," +
                    "\"emotionScore\":0-100的数字," +
                    "\"isNegative\":true或false," +
                    "\"riskLevel\":0-3的风险等级," +
                    "\"suggestion\":\"给用户的一句温暖建议\"," +
                    "\"riskDescription\":\"如果风险>1则给出风险提示，否则为空\"," +
                    "\"improvementSuggestions\":[\"一个具体行动建议\"]}",
                    userMessage.length() > 200 ? userMessage.substring(0, 200) : userMessage,
                    aiResponse.length() > 200 ? aiResponse.substring(0, 200) : aiResponse
            );

            String result = chatClient.prompt()
                    .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, "emotion_analysis_" + sessionId))
                    .user(emotionPrompt).call().content();
            System.out.println("=== AI情绪分析原始返回: " + result + " ===");
            // 清理可能的markdown包裹
            result = result.trim();
            if (result.startsWith("```")) {
                result = result.replaceAll("^```(json)?\\n?", "").replaceAll("\\n?```$", "");
            }

            // 验证JSON有效性
            JSONUtil.parseObj(result);
            System.out.println("=== 情绪分析JSON有效，准备保存 ===");

            // 保存到consultation_session
            ConsultationSession session = consultationSessionMapper.selectById(sessionId);
            if (session != null) {
                session.setLastEmotionAnalysis(result);
                session.setLastEmotionUpdatedAt(java.time.LocalDateTime.now());
                consultationSessionMapper.updateById(session);
                System.out.println("=== 情绪分析已保存到数据库 ===");
            } else {
                System.out.println("=== 未找到会话 sessionId=" + sessionId + " ===");
            }
        } catch (Exception e) {
            // 情绪分析失败不影响主流程
            System.out.println("=== 情绪分析失败: " + e.getMessage() + " ===");
            e.printStackTrace();
        }
    }
}
