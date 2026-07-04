package com.bangboo.iam.config;

import com.bangboo.iam.entity.Certification;
import com.bangboo.iam.entity.IamUser;
import com.bangboo.iam.enums.CertificationStatus;
import com.bangboo.iam.enums.UserRole;
import com.bangboo.iam.repository.CertificationRepository;
import com.bangboo.iam.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedIamData(
            UserRepository userRepository,
            CertificationRepository certificationRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            IamUser admin = ensureUser(userRepository, "admin", "Admin", true, 980, 5, "Star Hunter", passwordEncoder, UserRole.ADMIN, UserRole.SUPER_ADMIN);
            IamUser client = ensureUser(userRepository, "client01", "Client One", true, 760, 2, "Bronze Hunter", passwordEncoder);
            IamUser hunter = ensureUser(userRepository, "hunter01", "Hunter One", true, 920, 4, "Gold Hunter", passwordEncoder);
            IamUser hunter2 = ensureUser(userRepository, "hunter02", "Hunter Two", true, 875, 3, "Silver Hunter", passwordEncoder);
            IamUser jury = ensureUser(userRepository, "jury01", "Jury One", true, 890, 3, "Silver Hunter", passwordEncoder);
            ensureUser(userRepository, "newbie", "Newbie", false, 520, 0, "Trainee Hunter", passwordEncoder);

            ensureApprovedCertification(certificationRepository, client, admin.getId());
            ensureApprovedCertification(certificationRepository, hunter, admin.getId());
            ensureApprovedCertification(certificationRepository, hunter2, admin.getId());
            ensureApprovedCertification(certificationRepository, jury, admin.getId());
        };
    }

    private static IamUser ensureUser(
            UserRepository userRepository,
            String username,
            String nickname,
            boolean campusVerified,
            int reputation,
            int hunterLevel,
            String hunterTitle,
            PasswordEncoder passwordEncoder,
            UserRole... extraRoles
    ) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            IamUser user = user(username, nickname, campusVerified, reputation, hunterLevel, hunterTitle, passwordEncoder);
            for (UserRole role : extraRoles) {
                user.getRoles().add(role);
            }
            return userRepository.save(user);
        });
    }

    private static IamUser user(
            String username,
            String nickname,
            boolean campusVerified,
            int reputation,
            int hunterLevel,
            String hunterTitle,
            PasswordEncoder passwordEncoder
    ) {
        IamUser user = new IamUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode("123456"));
        user.setNickname(nickname);
        user.setEmail(username + "@campus.edu.cn");
        user.setPhone("18800000000");
        user.setSchool("Campus University");
        user.setAvatarUrl("bangboo:kacha");
        user.setCampusVerified(campusVerified);
        user.setReputation(reputation);
        user.setHunterLevel(hunterLevel);
        user.setHunterTitle(hunterTitle);
        user.getRoles().add(UserRole.USER);
        return user;
    }

    private static void ensureApprovedCertification(CertificationRepository certificationRepository, IamUser user, Long reviewerId) {
        if (!certificationRepository.existsByUserIdAndStatus(user.getId(), CertificationStatus.APPROVED)) {
            certificationRepository.save(approvedCertification(user, reviewerId));
        }
    }

    private static Certification approvedCertification(IamUser user, Long reviewerId) {
        Certification certification = new Certification();
        certification.setUserId(user.getId());
        certification.setRealName(user.getNickname());
        certification.setSchool("Campus University");
        certification.setStudentNo("202600000" + user.getId());
        certification.setMaterialUrl("/uploads/certifications/seed-" + user.getId() + ".png");
        certification.setStatus(CertificationStatus.APPROVED);
        certification.setReviewerId(reviewerId);
        certification.setReviewComment("Seed account certification approved");
        return certification;
    }
}
