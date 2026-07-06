package org.example.aispingboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("questionnaire")
public class Questionnaire {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String description;
    private String questions;
    private Integer questionCount;
    private Integer totalScore;
    private String scoringRules;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
