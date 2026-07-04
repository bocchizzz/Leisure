package com.bangboo.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;
import java.util.Set;

@ConfigurationProperties(prefix = "bb.file")
public class FileProperties {
    private String uploadDir = "data/uploads";
    private long maxSizeBytes = 8 * 1024 * 1024;
    private Set<String> allowedMimeTypes = new LinkedHashSet<>(Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "application/pdf"
    ));

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public void setMaxSizeBytes(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    public Set<String> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public void setAllowedMimeTypes(Set<String> allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
    }
}
