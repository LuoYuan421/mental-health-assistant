package org.example.aispingboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_article")
@Builder
public class KnowledgeArticle {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String summary;

    private String content;

    @TableField("category_id")
    private Long categoryId;

    @TableField("author_name")
    private String authorName;

    @TableField("cover_image")
    private String coverImage;

    @TableField("read_count")
    private Integer readCount;

    // 状态 0:草稿 1:已发布 2:已下线
    private Integer status;

    // 标签，逗号分隔
    private String tags;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
