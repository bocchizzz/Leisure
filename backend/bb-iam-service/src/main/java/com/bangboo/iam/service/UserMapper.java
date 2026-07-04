package com.bangboo.iam.service;

import com.bangboo.iam.dto.UserAccessStateVO;
import com.bangboo.iam.dto.UserBriefVO;
import com.bangboo.iam.dto.UserVO;
import com.bangboo.iam.entity.IamUser;
import com.bangboo.iam.enums.UserRole;

import java.util.Comparator;
import java.util.List;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserVO toVO(IamUser user) {
        return new UserVO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getNickname(),
                user.getPhone(),
                user.getSchool(),
                user.getAvatarUrl(),
                user.getStatus(),
                user.getReputation(),
                reputationLevel(user.getReputation()),
                sortedRoles(user),
                user.isCampusVerified(),
                user.getHunterLevel(),
                user.getHunterTitle(),
                user.getCreatedAt()
        );
    }

    public static UserBriefVO toBriefVO(IamUser user) {
        return new UserBriefVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getStatus(),
                user.isCampusVerified(),
                user.getReputation(),
                sortedRoles(user)
        );
    }

    public static UserAccessStateVO toAccessStateVO(IamUser user) {
        return new UserAccessStateVO(
                user.getId(),
                user.getStatus(),
                user.isCampusVerified(),
                sortedRoles(user)
        );
    }

    private static List<UserRole> sortedRoles(IamUser user) {
        return user.getRoles().stream()
                .sorted(Comparator.comparing(Enum::name))
                .toList();
    }

    private static String reputationLevel(int reputation) {
        if (reputation >= 900) {
            return "S";
        }
        if (reputation >= 800) {
            return "A";
        }
        if (reputation >= 650) {
            return "B";
        }
        return "C";
    }
}
