package org.example.aispingboot.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.common.ResultCode;
import org.example.aispingboot.entity.Questionnaire;
import org.example.aispingboot.entity.QuestionnaireRecord;
import org.example.aispingboot.mapper.QuestionnaireMapper;
import org.example.aispingboot.mapper.QuestionnaireRecordMapper;
import org.example.aispingboot.util.JwtTokenUtil;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/questionnaire")
public class QuestionnaireController {

    @Resource
    private QuestionnaireMapper questionnaireMapper;

    @Resource
    private QuestionnaireRecordMapper questionnaireRecordMapper;

    // 获取当前用户ID的工具方法（与PsychologicalChat一致）
    private Long getCurrentUserId() {
        String token = JwtTokenUtil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        return jwt.getClaim("userId").asLong();
    }

    // 获取所有启用的问卷列表
    @GetMapping("/list")
    public Result<List<Questionnaire>> list() {
        LambdaQueryWrapper<Questionnaire> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Questionnaire::getStatus, 1)
                .orderByAsc(Questionnaire::getId);
        List<Questionnaire> list = questionnaireMapper.selectList(wrapper);
        return Result.ok(list);
    }

    // 获取问卷详情
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            return Result.error(ResultCode.ERROR.getCode(), "问卷不存在", null);
        }
        // 解析JSON格式的题目列表
        JSONObject result = new JSONObject();
        result.set("id", questionnaire.getId());
        result.set("name", questionnaire.getName());
        result.set("code", questionnaire.getCode());
        result.set("description", questionnaire.getDescription());
        result.set("questionCount", questionnaire.getQuestionCount());
        result.set("totalScore", questionnaire.getTotalScore());
        result.set("scoringRules", JSONUtil.parse(questionnaire.getScoringRules()));
        // 将字符串解析为JSON数组返回
        result.set("questions", JSONUtil.parseArray(questionnaire.getQuestions()));
        return Result.ok(result);
    }

    // 提交问卷答案
    @SuppressWarnings("unchecked")
    @PostMapping("/submit")
    public Result<Map<String, Object>> submit(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();

        // 参数校验
        Integer questionnaireId = (Integer) body.get("questionnaireId");
        if (questionnaireId == null) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "缺少questionnaireId", null);
        }
        Object answersObj = body.get("answers");
        if (!(answersObj instanceof List)) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "answers必须是数组", null);
        }
        List<Integer> answers = (List<Integer>) answersObj;

        // 查询问卷
        Questionnaire questionnaire = questionnaireMapper.selectById(questionnaireId.longValue());
        if (questionnaire == null) {
            return Result.error(ResultCode.ERROR.getCode(), "问卷不存在", null);
        }

        // 计算总分（将所有答案相加）
        int totalScore = answers.stream().mapToInt(Integer::intValue).sum();

        // 根据问卷编码判断评估等级和建议
        String code = questionnaire.getCode();
        String level;
        String suggestion;

        if ("PHQ-9".equals(code)) {
            if (totalScore <= 4) {
                level = "正常";
                suggestion = "情绪状态良好，继续保持健康的生活方式和积极的心态。";
            } else if (totalScore <= 9) {
                level = "轻度";
                suggestion = "建议关注情绪变化，适当增加运动，保持规律作息。如果持续感到不适，可以考虑寻求心理咨询。";
            } else if (totalScore <= 14) {
                level = "中度";
                suggestion = "建议寻求专业心理咨询，与信任的人倾诉你的感受。必要时请前往医院心理科就诊。";
            } else if (totalScore <= 19) {
                level = "中重度";
                suggestion = "强烈建议尽快寻求专业心理帮助，联系心理咨询师或前往医院心理科进行评估。请不要独自承受。";
            } else {
                level = "重度";
                suggestion = "强烈建议尽快寻求专业心理帮助，请立即联系心理咨询师或前往医院心理科就诊。如有自伤想法，请拨打心理援助热线：400-161-9995。";
            }
        } else if ("GAD-7".equals(code)) {
            if (totalScore <= 4) {
                level = "正常";
                suggestion = "焦虑水平正常，继续保持良好的心态和健康的生活习惯。";
            } else if (totalScore <= 9) {
                level = "轻度";
                suggestion = "建议学习一些放松技巧，如深呼吸、冥想等，适当运动有助于缓解轻度焦虑。";
            } else if (totalScore <= 14) {
                level = "中度";
                suggestion = "建议寻求专业心理咨询，学习焦虑管理技巧。如症状持续影响日常生活，请前往医院心理科就诊。";
            } else {
                level = "重度";
                suggestion = "强烈建议尽快寻求专业心理帮助，焦虑症状较为严重，请及时前往医院心理科就诊或拨打心理援助热线：400-161-9995。";
            }
        } else {
            // 其他量表通用处理
            if (totalScore <= questionnaire.getTotalScore() * 0.25) {
                level = "正常";
                suggestion = "评估结果正常，请继续保持健康的生活方式。";
            } else if (totalScore <= questionnaire.getTotalScore() * 0.5) {
                level = "轻度";
                suggestion = "建议关注自身状态，适当调整生活方式。";
            } else if (totalScore <= questionnaire.getTotalScore() * 0.75) {
                level = "中度";
                suggestion = "建议寻求专业心理咨询。";
            } else {
                level = "重度";
                suggestion = "强烈建议尽快寻求专业心理帮助。";
            }
        }

        // 保存测评记录
        QuestionnaireRecord record = new QuestionnaireRecord();
        record.setUserId(userId);
        record.setQuestionnaireId(questionnaireId.longValue());
        record.setAnswers(JSONUtil.toJsonStr(answers));
        record.setTotalScore(totalScore);
        record.setLevel(level);
        record.setSuggestion(suggestion);
        record.setCreatedAt(LocalDateTime.now());
        questionnaireRecordMapper.insert(record);

        // 返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordId", record.getId());
        result.put("totalScore", totalScore);
        result.put("level", level);
        result.put("suggestion", suggestion);
        return Result.ok(result);
    }

    // 获取当前用户的测评记录列表
    @GetMapping("/records")
    public Result<List<QuestionnaireRecord>> getRecords() {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<QuestionnaireRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionnaireRecord::getUserId, userId)
                .orderByDesc(QuestionnaireRecord::getCreatedAt);
        List<QuestionnaireRecord> records = questionnaireRecordMapper.selectList(wrapper);
        return Result.ok(records);
    }

    // 获取指定测评记录详情
    @GetMapping("/records/{id}")
    public Result<Map<String, Object>> getRecordDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        QuestionnaireRecord record = questionnaireRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            return Result.error(ResultCode.ERROR.getCode(), "记录不存在", null);
        }

        // 查询关联的问卷信息
        Questionnaire questionnaire = questionnaireMapper.selectById(record.getQuestionnaireId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("record", record);
        if (questionnaire != null) {
            result.put("questionnaireName", questionnaire.getName());
            result.put("questionnaireCode", questionnaire.getCode());
            // 解析题目，方便前端展示
            result.put("questions", JSONUtil.parseArray(questionnaire.getQuestions()));
        }
        // 解析答案数组
        result.put("answersList", JSONUtil.parseArray(record.getAnswers()));
        return Result.ok(result);
    }
}
