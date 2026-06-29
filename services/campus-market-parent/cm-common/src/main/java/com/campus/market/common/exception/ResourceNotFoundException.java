package com.campus.market.common.exception;

/**
 * 资源不存在异常
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String message) {
        super(404, message);
    }
    
    public ResourceNotFoundException(String resource, Long id) {
        super(404, resource + " not found: " + id);
    }
}
