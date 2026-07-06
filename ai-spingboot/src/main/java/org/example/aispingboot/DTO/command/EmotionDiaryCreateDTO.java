package org.example.aispingboot.DTO.command;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmotionDiaryCreateDTO {
    private LocalDate diaryDate;

    @NotNull(message = "情绪评分不能为空")
    @Min(value = 1, message = "情绪评分最小为1")
    @Max(value = 10, message = "情绪评分最大为10")
    private Integer moodScore;

    @NotBlank(message = "请选择主要情绪")
    private String dominantEmotion;

    private String emotionTriggers;

    private String diaryContent;

    @Min(value = 1, message = "睡眠质量最小为1")
    @Max(value = 5, message = "睡眠质量最大为5")
    private Integer sleepQuality;

    @Min(value = 1, message = "压力水平最小为1")
    @Max(value = 5, message = "压力水平最大为5")
    private Integer stressLevel;
}
