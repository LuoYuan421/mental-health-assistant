package org.example.aispingboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("questionnaire_record")
public class QuestionnaireRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long questionnaireId;
    private String answers;
    private Integer totalScore;
    private String level;
    private String suggestion;
    private LocalDateTime createdAt;
}
