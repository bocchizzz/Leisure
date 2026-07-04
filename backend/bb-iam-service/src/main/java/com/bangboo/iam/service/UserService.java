package com.bangboo.iam.service;

import com.bangboo.common.constant.ErrorCode;
import com.bangboo.common.exception.BusinessException;
import com.bangboo.common.exception.ForbiddenException;
import com.bangboo.common.exception.ResourceNotFoundException;
import com.bangboo.common.exception.UnauthorizedException;
import com.bangboo.common.response.PageResult;
import com.bangboo.common.security.JwtTokenProvider;
import com.bangboo.iam.dto.BanUserRequest;
import com.bangboo.iam.dto.IamStatsVO;
import com.bangboo.iam.dto.LoginRequest;
import com.bangboo.iam.dto.LoginResponse;
import com.bangboo.iam.dto.RegisterRequest;
import com.bangboo.iam.dto.ReputationAdjustmentRequest;
import com.bangboo.iam.dto.ReputationAdjustmentVO;
import com.bangboo.iam.dto.UpdateProfileRequest;
import com.bangboo.iam.dto.UserAccessStateVO;
import com.bangboo.iam.dto.UserBriefVO;
import com.bangboo.iam.dto.UserVO;
import com.bangboo.iam.entity.IamUser;
import com.bangboo.iam.enums.UserRole;
import com.bangboo.iam.enums.UserStatus;
import com.bangboo.iam.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UserService {
    private static final Set<String> ALLOWED_BUILTIN_AVATARS = Set.of(
            "bangboo:kacha",
            "bangboo:phantom",
            "bangboo:fan",
            "bangboo:elf",
            "bangboo:gentle",
            "bangboo:shark"
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public UserVO register(RegisterRequest request) {
        String username = normalizeUsername(request.username());
        if (username.isBlank() || request.password() == null || request.password().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请输入用户名和密码");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }

        IamUser user = new IamUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmail(blankToNull(request.email()));
        user.setNickname(blankToNull(request.nickname()) == null ? username : request.nickname().trim());
        user.setAvatarUrl("bangboo:kacha");
        user.setReputation(500);
        user.getRoles().add(UserRole.USER);
        return UserMapper.toVO(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String username = normalizeUsername(request.username());
        IamUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("账号或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("账号或密码错误");
        }
        if (user.getStatus() == UserStatus.BANNED) {
            throw new ForbiddenException("账号已被封禁，请联系管理员");
        }
        List<String> roles = user.getRoles().stream().map(Enum::name).toList();
        String token = jwtTokenProvider.createToken(user.getId(), roles, user.getStatus().name(), user.isCampusVerified());
        return new LoginResponse(token, UserMapper.toVO(user));
    }

    @Transactional(readOnly = true)
    public UserVO getById(Long id) {
        return UserMapper.toVO(requireUserById(id));
    }

    @Transactional(readOnly = true)
    public UserVO getProfile(Long id) {
        return UserMapper.toVO(requireUserById(id));
    }

    @Transactional
    public UserVO updateProfile(Long id, UpdateProfileRequest request) {
        IamUser user = requireUserById(id);
        if (request.nickname() != null) {
            user.setNickname(blankToNull(request.nickname()));
        }
        if (request.phone() != null) {
            user.setPhone(blankToNull(request.phone()));
        }
        if (request.school() != null) {
            user.setSchool(blankToNull(request.school()));
        }
        if (request.email() != null) {
            user.setEmail(blankToNull(request.email()));
        }
        if (request.avatarUrl() != null) {
            validateAvatarUrl(request.avatarUrl());
            user.setAvatarUrl(blankToNull(request.avatarUrl()));
        }
        return UserMapper.toVO(user);
    }

    @Transactional(readOnly = true)
    public PageResult<UserVO> adminList(int page, int size, String keyword, String status) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizeSize(size));
        Page<IamUser> result;
        if (status != null && !status.isBlank()) {
            result = userRepository.findByStatus(UserStatus.valueOf(status.trim()), pageable);
        } else {
            result = userRepository.findAll(pageable);
        }
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<UserVO> content = result.getContent().stream()
                .filter(user -> normalizedKeyword.isBlank() || matchesKeyword(user, normalizedKeyword))
                .map(UserMapper::toVO)
                .toList();
        return new PageResult<>(
                content,
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize(),
                result.isFirst(),
                result.isLast()
        );
    }

    @Transactional
    public UserVO banUser(Long id, BanUserRequest request) {
        IamUser user = requireUserById(id);
        user.setStatus(UserStatus.BANNED);
        return UserMapper.toVO(user);
    }

    @Transactional
    public UserVO unbanUser(Long id) {
        IamUser user = requireUserById(id);
        user.setStatus(UserStatus.ACTIVE);
        return UserMapper.toVO(user);
    }

    @Transactional(readOnly = true)
    public UserBriefVO getBrief(Long id) {
        return UserMapper.toBriefVO(requireUserById(id));
    }

    @Transactional(readOnly = true)
    public List<UserBriefVO> getBriefs(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userRepository.findAllById(new LinkedHashSet<>(ids)).stream()
                .map(UserMapper::toBriefVO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserAccessStateVO getAccessState(Long id) {
        return UserMapper.toAccessStateVO(requireUserById(id));
    }

    @Transactional
    public ReputationAdjustmentVO adjustReputation(Long id, ReputationAdjustmentRequest request) {
        IamUser user = requireUserById(id);
        int before = user.getReputation();
        int after = Math.max(0, before + request.delta());
        user.setReputation(after);
        return new ReputationAdjustmentVO(user.getId(), before, after);
    }

    @Transactional(readOnly = true)
    public IamStatsVO stats() {
        return new IamStatsVO(userRepository.count(), userRepository.countByCampusVerifiedTrue());
    }

    IamUser requireUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }

    private static String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static int normalizeSize(int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private static boolean matchesKeyword(IamUser user, String keyword) {
        return contains(user.getUsername(), keyword)
                || contains(user.getNickname(), keyword)
                || contains(user.getEmail(), keyword)
                || contains(user.getPhone(), keyword);
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private static void validateAvatarUrl(String avatarUrl) {
        String value = avatarUrl.trim();
        if (value.isBlank()) {
            return;
        }
        if (ALLOWED_BUILTIN_AVATARS.contains(value) || value.startsWith("bangboo-ai:")) {
            return;
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "头像格式不合法");
    }
}
