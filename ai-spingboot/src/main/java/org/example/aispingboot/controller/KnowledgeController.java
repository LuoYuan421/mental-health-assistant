package org.example.aispingboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.entity.KnowledgeArticle;
import org.example.aispingboot.entity.KnowledgeCategory;
import org.example.aispingboot.mapper.KnowledgeArticleMapper;
import org.example.aispingboot.mapper.KnowledgeCategoryMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    @Resource
    private KnowledgeArticleMapper articleMapper;

    @Resource
    private KnowledgeCategoryMapper categoryMapper;

    // 分类树
    @GetMapping("/category/tree")
    public Result<List<KnowledgeCategory>> categoryTree() {
        LambdaQueryWrapper<KnowledgeCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeCategory::getStatus, 1)
                .orderByAsc(KnowledgeCategory::getSortOrder);
        List<KnowledgeCategory> categories = categoryMapper.selectList(wrapper);
        return Result.ok(categories);
    }

    // 文章分页列表
    @GetMapping("/article/page")
    public Result<Object> articlePage(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection) {

        Page<KnowledgeArticle> page = new Page<>(currentPage, size);
        LambdaQueryWrapper<KnowledgeArticle> wrapper = new LambdaQueryWrapper<>();

        // 条件筛选
        if (StringUtils.hasText(title)) {
            wrapper.like(KnowledgeArticle::getTitle, title);
        }
        if (categoryId != null) {
            wrapper.eq(KnowledgeArticle::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(KnowledgeArticle::getStatus, status);
        }

        // 排序
        if ("readCount".equals(sortField) && "desc".equals(sortDirection)) {
            wrapper.orderByDesc(KnowledgeArticle::getReadCount);
        } else if ("publishedAt".equals(sortField) && "desc".equals(sortDirection)) {
            wrapper.orderByDesc(KnowledgeArticle::getPublishedAt);
        } else {
            wrapper.orderByDesc(KnowledgeArticle::getUpdatedAt);
        }

        Page<KnowledgeArticle> result = articleMapper.selectPage(page, wrapper);

        // 填充分类名称
        List<Long> categoryIds = result.getRecords().stream()
                .map(KnowledgeArticle::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        if (!categoryIds.isEmpty()) {
            Map<Long, String> categoryNameMap = categoryMapper.selectBatchIds(categoryIds)
                    .stream()
                    .collect(Collectors.toMap(KnowledgeCategory::getId, KnowledgeCategory::getCategoryName));
            result.getRecords().forEach(a -> a.setAuthorName(
                    categoryNameMap.getOrDefault(a.getCategoryId(), "") + ""));
        }

        return Result.ok(result);
    }

    // 文章详情
    @GetMapping("/article/{id}")
    public Result<KnowledgeArticle> getArticleDetail(@PathVariable Long id) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article != null) {
            // 增加阅读量
            article.setReadCount(article.getReadCount() != null ? article.getReadCount() + 1 : 1);
            articleMapper.updateById(article);

            // 填充分类名称
            KnowledgeCategory category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                // 前端期望 categoryName 字段
                article.setAuthorName(article.getAuthorName());
            }
            // 分割标签
            if (StringUtils.hasText(article.getTags())) {
                article.setTags(article.getTags());
            }
        }
        return Result.ok(article);
    }

    // 创建文章
    @PostMapping("/article")
    public Result<KnowledgeArticle> createArticle(@RequestBody KnowledgeArticle article) {
        article.setReadCount(0);
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        if (article.getStatus() == null) {
            article.setStatus(0); // 默认草稿
        }
        articleMapper.insert(article);
        return Result.ok(article);
    }

    // 更新文章
    @PutMapping("/article/{id}")
    public Result<KnowledgeArticle> updateArticle(@PathVariable Long id, @RequestBody KnowledgeArticle article) {
        article.setId(id);
        article.setUpdatedAt(LocalDateTime.now());
        articleMapper.updateById(article);
        return Result.ok(article);
    }

    // 修改文章状态
    @PutMapping("/article/{id}/status")
    public Result<Void> changeArticleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article != null) {
            article.setStatus(body.get("status"));
            article.setUpdatedAt(LocalDateTime.now());
            if (body.get("status") == 1) {
                article.setPublishedAt(LocalDateTime.now());
            }
            articleMapper.updateById(article);
        }
        return Result.ok();
    }

    // 删除文章
    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleMapper.deleteById(id);
        return Result.ok();
    }
}
