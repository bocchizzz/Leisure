package com.campus.market.product.service;

import com.campus.market.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务
 */
@Service
@Slf4j
public class FileService {
    
    @Value("${file.upload-dir:${user.home}/campus-market/uploads}")
    private String uploadDir;
    
    @Value("${file.base-url:http://localhost:8080/uploads}")
    private String baseUrl;
    
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    
    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Upload directory created: {}", uploadPath);
            } else {
                log.info("Upload directory exists: {}", uploadPath);
            }
            
            if (!Files.isWritable(uploadPath)) {
                log.error("Upload directory is not writable: {}", uploadPath);
                throw new RuntimeException("上传目录不可写: " + uploadPath);
            }
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", e.getMessage());
            throw new RuntimeException("无法创建上传目录: " + e.getMessage(), e);
        }
    }
    
    public String uploadFile(MultipartFile file) {
        validateFile(file);

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + extension;
            
            Path targetPath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("File uploaded successfully: {}", newFilename);
            
            return baseUrl + "/" + newFilename;
            
        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }
    
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
            return;
        }
        
        try {
            String filename = fileUrl.substring(baseUrl.length() + 1);
            Path filePath = Paths.get(uploadDir, filename);
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", filename);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", e.getMessage());
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("文件大小不能超过5MB");
        }
        
        String contentType = file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("只支持 JPG、PNG、GIF、WebP 格式的图片");
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
