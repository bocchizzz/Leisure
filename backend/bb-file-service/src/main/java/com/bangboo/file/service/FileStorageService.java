package com.bangboo.file.service;

import com.bangboo.common.constant.ErrorCode;
import com.bangboo.common.exception.BusinessException;
import com.bangboo.common.exception.UnauthorizedException;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.common.security.CurrentUserContext;
import com.bangboo.file.config.FileProperties;
import com.bangboo.file.dto.UploadResult;
import com.bangboo.file.entity.FileMetadata;
import com.bangboo.file.enums.FilePurpose;
import com.bangboo.file.repository.FileMetadataRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy/MM");

    private final FileProperties properties;
    private final FileMetadataRepository repository;

    public FileStorageService(FileProperties properties, FileMetadataRepository repository) {
        this.properties = properties;
        this.repository = repository;
    }

    @Transactional
    public UploadResult upload(MultipartFile file, String purposeValue) {
        CurrentUser currentUser = CurrentUserContext.get()
                .orElseThrow(() -> new UnauthorizedException("请先登录"));
        validateFile(file);
        FilePurpose purpose = parsePurpose(purposeValue);
        String contentType = normalizeContentType(file.getContentType());
        validateContentType(contentType);

        String extension = extensionFor(file.getOriginalFilename(), contentType);
        String yearMonth = YEAR_MONTH.format(LocalDate.now());
        String storedFilename = UUID.randomUUID() + extension;
        Path uploadRoot = Path.of(properties.getUploadDir()).toAbsolutePath().normalize();
        Path targetDir = uploadRoot.resolve(yearMonth).normalize();
        Path targetPath = targetDir.resolve(storedFilename).normalize();

        if (!targetPath.startsWith(uploadRoot)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件路径不合法");
        }

        try {
            Files.createDirectories(targetDir);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "文件保存失败");
        }

        String url = "/uploads/" + yearMonth + "/" + storedFilename;
        FileMetadata metadata = new FileMetadata();
        metadata.setOwnerId(currentUser.uid());
        metadata.setOriginalFilename(StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename()));
        metadata.setStoredFilename(storedFilename);
        metadata.setContentType(contentType);
        metadata.setSize(file.getSize());
        metadata.setPurpose(purpose);
        metadata.setUrl(url);
        metadata.setStoragePath(targetPath.toString());
        repository.save(metadata);

        return new UploadResult(url);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择文件");
        }
        if (file.getSize() <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件不能为空");
        }
        if (file.getSize() > properties.getMaxSizeBytes()) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "文件大小超过限制");
        }
    }

    private FilePurpose parsePurpose(String purposeValue) {
        if (purposeValue == null || purposeValue.isBlank()) {
            return null;
        }
        try {
            return FilePurpose.valueOf(purposeValue.trim());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件用途不支持");
        }
    }

    private void validateContentType(String contentType) {
        if (!properties.getAllowedMimeTypes().contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "文件类型不支持");
        }
    }

    private static String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }
        return contentType.trim().toLowerCase(Locale.ROOT);
    }

    private static String extensionFor(String filename, String contentType) {
        String extension = StringUtils.getFilenameExtension(filename);
        if (extension != null && !extension.isBlank()) {
            return "." + extension.toLowerCase(Locale.ROOT);
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }
}
