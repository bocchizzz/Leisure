package com.campus.market.product.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.product.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "文件接口", description = "文件上传")
public class FileController {
    
    private final FileService fileService;
    
    @PostMapping("/upload")
    @Operation(summary = "上传图片")
    public ApiResponse<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = fileService.uploadFile(file);
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return ApiResponse.success("上传成功", result);
    }
}
