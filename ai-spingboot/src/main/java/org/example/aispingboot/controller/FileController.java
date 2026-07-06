package org.example.aispingboot.controller;

import org.example.aispingboot.common.Result;
import org.example.aispingboot.common.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${server.port:1236}")
    private String serverPort;

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "businessType", defaultValue = "ARTICLE") String businessType,
            @RequestParam(value = "businessId", defaultValue = "0") String businessId,
            @RequestParam(value = "businessField", defaultValue = "cover") String businessField) {

        if (file.isEmpty()) {
            return Result.error(ResultCode.FILE_UPLOAD_FAILED.getCode(), "文件不能为空", null);
        }

        try {
            // 确保上传目录存在
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            File dest = new File(UPLOAD_DIR + filename);
            file.transferTo(dest);

            // 返回文件访问路径
            String fileUrl = "/uploads/" + filename;
            return Result.ok(Map.of("url", fileUrl, "filename", filename));

        } catch (IOException e) {
            return Result.error(ResultCode.FILE_UPLOAD_FAILED.getCode(), "文件上传失败: " + e.getMessage(), null);
        }
    }
}
