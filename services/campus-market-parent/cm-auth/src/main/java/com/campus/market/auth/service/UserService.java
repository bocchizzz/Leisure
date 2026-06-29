package com.campus.market.auth.service;

import com.campus.market.auth.dto.*;
import com.campus.market.auth.entity.User;
import com.campus.market.auth.enums.UserRole;
import com.campus.market.auth.enums.UserStatus;
import com.campus.market.auth.feign.TradeClient;
import com.campus.market.auth.repository.UserRepository;
import com.campus.market.common.exception.BusinessException;
import com.campus.market.common.exception.ResourceNotFoundException;
import com.campus.market.common.response.ApiResponse;
import com.campus.market.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Autowired(required = false)
    private TradeClient tradeClient;
    
    /**
     * 登录
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        // 允许封禁用户登录，前端会根据status跳转到申诉页面
        // if (user.getStatus() == UserStatus.BANNED) {
        //     throw new BusinessException(403, "账号已被封禁");
        // }
        
        List<String> roles = user.getRoles().stream()
                .map(UserRole::name)
                .collect(Collectors.toList());
        
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roles);
        
        return new LoginResponse(token, UserVO.fromEntity(user));
    }
    
    /**
     * 注册
     */
    @Transactional
    public UserVO register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setStatus(UserStatus.ACTIVE);
        user.setReputation(100);
        
        // 设置角色，默认为BUYER
        Set<UserRole> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                try {
                    UserRole role = UserRole.valueOf(roleName);
                    if (role != UserRole.ADMIN) { // 禁止注册时设置ADMIN角色
                        roles.add(role);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        if (roles.isEmpty()) {
            roles.add(UserRole.BUYER);
        }
        user.setRoles(roles);
        
        user = userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());
        
        return UserVO.fromEntity(user);
    }
    
    /**
     * 获取用户信息
     */
    public UserVO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return UserVO.fromEntity(user);
    }
    
    /**
     * 获取用户信息（通过用户名）
     */
    public UserVO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return UserVO.fromEntity(user);
    }
    
    /**
     * 更新用户资料
     */
    @Transactional
    public UserVO updateProfile(Long userId, String avatar, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        if (avatar != null) {
            user.setAvatarUrl(avatar);
        }
        if (email != null) {
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new BusinessException("邮箱已被使用");
            }
            user.setEmail(email);
        }
        
        user = userRepository.save(user);
        return UserVO.fromEntity(user);
    }
    
    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        // P2: 添加密码长度校验，与E4保持一致
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    /**
     * 获取信誉报告
     */
    public Map<String, Object> getReputationReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        Map<String, Object> report = new HashMap<>();
        report.put("userId", user.getId());
        report.put("username", user.getUsername());
        report.put("reputation", user.getReputation());
        report.put("reputationLevel", user.getReputationLevel());
        
        // P1-5: 从cm-trade获取卖家平均评分
        Double averageRating = 0.0;
        if (tradeClient != null) {
            try {
                ApiResponse<Double> resp = tradeClient.getSellerAverageRating(userId);
                if (resp.getCode() == 200 && resp.getData() != null) {
                    averageRating = resp.getData();
                }
            } catch (Exception e) {
                log.warn("Failed to get average rating for user {}: {}", userId, e.getMessage());
            }
        }
        report.put("averageRating", averageRating);
        
        return report;
    }
    
    // ========== 管理员接口 ==========
    
    /**
     * 分页查询用户
     */
    public Page<UserVO> listUsers(String keyword, UserStatus status, Pageable pageable) {
        return userRepository.findByKeywordAndStatus(keyword, status, pageable)
                .map(UserVO::fromEntity);
    }
    
    /**
     * 封禁用户
     */
    @Transactional
    public void banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        if (user.isAdmin()) {
            throw new BusinessException("不能封禁管理员");
        }
        
        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);
        log.info("User banned: {}", user.getUsername());
    }
    
    /**
     * 解封用户
     */
    @Transactional
    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("User unbanned: {}", user.getUsername());
    }
    
    // ========== 内部接口 ==========
    
    /**
     * 批量获取用户简要信息（内部接口）
     */
    public List<Map<String, Object>> getUserBriefs(List<Long> ids) {
        List<User> users = userRepository.findByIdIn(ids);
        return users.stream()
                .map(user -> {
                    Map<String, Object> brief = new HashMap<>();
                    brief.put("id", user.getId());
                    brief.put("username", user.getUsername());
                    brief.put("status", user.getStatus().name());
                    return brief;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户状态（内部接口）
     */
    public Map<String, Object> getUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("status", user.getStatus().name());
        return result;
    }
    
    /**
     * 更新用户信誉分（内部接口）
     */
    @Transactional
    public void updateReputation(Long userId, int delta) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        if (delta > 0) {
            user.increaseReputation(delta);
        } else {
            user.decreaseReputation(-delta);
        }
        
        userRepository.save(user);
        log.info("User {} reputation updated by {}, now {}", userId, delta, user.getReputation());
    }
    
    /**
     * 获取所有用户列表（供AI使用）
     */
    public List<Map<String, Object>> getAllUsersForAI() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", user.getId());
                    info.put("username", user.getUsername());
                    info.put("status", user.getStatus().name());
                    info.put("statusName", user.getStatus() == UserStatus.ACTIVE ? "正常" : "封禁");
                    info.put("reputation", user.getReputation());
                    info.put("roles", user.getRoles().stream().map(UserRole::name).collect(Collectors.toList()));
                    return info;
                })
                .collect(Collectors.toList());
    }
}
