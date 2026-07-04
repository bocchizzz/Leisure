package com.bangboo.marketplace.config;

import com.bangboo.marketplace.entity.Task;
import com.bangboo.marketplace.entity.TaskApplication;
import com.bangboo.marketplace.entity.TaskContract;
import com.bangboo.marketplace.enums.ApplicationStatus;
import com.bangboo.marketplace.enums.BountyType;
import com.bangboo.marketplace.enums.ContractStatus;
import com.bangboo.marketplace.enums.SafetyStatus;
import com.bangboo.marketplace.enums.TaskCategory;
import com.bangboo.marketplace.enums.TaskStatus;
import com.bangboo.marketplace.repository.TaskApplicationRepository;
import com.bangboo.marketplace.repository.TaskContractRepository;
import com.bangboo.marketplace.repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

/**
 * 种子数据，复刻 test-tools/mock-b-services.js，保证 A 侧 court 服务与集成测试兼容：
 * - task 101/102/201/202
 * - application 601（task 101）
 * - contract 301（task 201, DISPUTED）/ 302（task 202, WAIT_CONFIRM）
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedMarketplaceData(
            TaskRepository taskRepository,
            TaskApplicationRepository applicationRepository,
            TaskContractRepository contractRepository,
            TransactionTemplate transactionTemplate
    ) {
        return args -> transactionTemplate.executeWithoutResult(status -> {
            if (taskRepository.count() > 0) {
                return;
            }
            seedTasks(taskRepository);
            seedApplication(applicationRepository);
            seedContracts(contractRepository);
        });
    }

    private void seedTasks(TaskRepository repo) {
        Task t101 = baseTask(101L, "帮我取一杯咖啡送到图书馆",
                "请从南门买一杯拿铁，送到图书馆三楼自习区。", TaskCategory.ERRAND, "F", 18.0,
                TaskStatus.PUBLISHED);
        t101.setLocation("南门");
        t101.setDeadline(Instant.parse("2026-07-10T10:00:00Z"));
        t101.setCompletionStandard("送达委托人手中。");
        t101.setEvidenceRequirement("如需可上传交接照片。");
        t101.setApplicationCount(1);
        t101.setViewCount(88);
        t101.setPublishedAt(Instant.parse("2026-07-01T09:00:00Z"));
        repo.save(t101);

        Task t102 = baseTask(102L, "社团招新海报设计",
                "设计一版社团招新海报草稿。", TaskCategory.DESIGN, "C", 80.0,
                TaskStatus.PENDING_REVIEW);
        t102.setLocation("线上");
        t102.setViewCount(20);
        t102.setSafetyStatus(SafetyStatus.REVIEW);
        t102.setSafetyScore(52.0);
        repo.save(t102);

        Task t201 = baseTask(201L, "海报润色与排版",
                "小法庭示例契约关联任务。", TaskCategory.DESIGN, "B", 80.0,
                TaskStatus.COURT_REVIEW);
        t201.setLocation("线上");
        t201.setAssignedHunterId(3L);
        t201.setApplicationCount(1);
        t201.setViewCount(41);
        t201.setCompletionStandard("交付最终海报文件。");
        t201.setEvidenceRequirement("上传最终海报与修改记录。");
        repo.save(t201);

        Task t202 = baseTask(202L, "交付范围核对",
                "用于创建小法庭案件的第二个契约任务。", TaskCategory.ONLINE, "D", 120.0,
                TaskStatus.WAIT_CONFIRM);
        t202.setLocation("线上");
        t202.setAssignedHunterId(3L);
        t202.setApplicationCount(1);
        t202.setViewCount(36);
        t202.setCompletionStandard("核对范围并提交书面结论。");
        t202.setEvidenceRequirement("上传结论文本。");
        repo.save(t202);
    }

    private Task baseTask(Long id, String title, String description, TaskCategory category,
                          String difficulty, Double bounty, TaskStatus status) {
        Task task = new Task();
        task.setId(id);
        task.setTaskNo("TASK-" + id);
        task.setTitle(title);
        task.setDescription(description);
        task.setCategory(category);
        task.setDifficulty(difficulty);
        task.setBountyAmount(bounty);
        task.setBountyType(BountyType.POINT);
        task.setStatus(status);
        task.setPublisherId(2L);
        task.setPublisherName("星野委托人");
        task.setPublisherAvatar("bangboo:kacha");
        task.setApplicationCount(0);
        task.setViewCount(0);
        task.setSafetyStatus(SafetyStatus.PASS);
        return task;
    }

    private void seedApplication(TaskApplicationRepository repo) {
        TaskApplication app = new TaskApplication();
        app.setId(601L);
        app.setTaskId(101L);
        app.setTaskTitle("帮我取一杯咖啡送到图书馆");
        app.setHunterId(3L);
        app.setHunterName("热心猎人");
        app.setHunterAvatar("bangboo:kacha");
        app.setHunterLevel(4);
        app.setHunterTitle("金牌猎人");
        app.setHunterReputation(920);
        app.setPublisherId(2L);
        app.setApplyMessage("我可以在 20 分钟内送达。");
        app.setExpectedFinishTime(Instant.parse("2026-07-10T10:00:00Z"));
        app.setStatus(ApplicationStatus.APPLIED);
        repo.save(app);
    }

    private void seedContracts(TaskContractRepository repo) {
        TaskContract c301 = new TaskContract();
        c301.setId(301L);
        c301.setContractNo("CONTRACT-2026-0301");
        c301.setTaskId(201L);
        c301.setTaskTitle("海报润色与排版");
        c301.setApplicationId(600L);
        c301.setPublisherId(2L);
        c301.setPublisherName("星野委托人");
        c301.setPublisherAvatar("bangboo:kacha");
        c301.setHunterId(3L);
        c301.setHunterName("热心猎人");
        c301.setHunterAvatar("bangboo:kacha");
        c301.setBountyAmount(80.0);
        c301.setBountyType(BountyType.POINT);
        c301.setStatus(ContractStatus.DISPUTED);
        c301.setCompletionStandard("交付最终海报文件。");
        c301.setEvidenceRequirement("上传最终海报与修改记录。");
        c301.setReviewedByPublisher(false);
        c301.setReviewedByHunter(false);
        c301.setAcceptedAt(Instant.parse("2026-07-01T14:00:00Z"));
        repo.save(c301);

        TaskContract c302 = new TaskContract();
        c302.setId(302L);
        c302.setContractNo("CONTRACT-2026-0302");
        c302.setTaskId(202L);
        c302.setTaskTitle("交付范围核对");
        c302.setApplicationId(602L);
        c302.setPublisherId(2L);
        c302.setPublisherName("星野委托人");
        c302.setPublisherAvatar("bangboo:kacha");
        c302.setHunterId(3L);
        c302.setHunterName("热心猎人");
        c302.setHunterAvatar("bangboo:kacha");
        c302.setBountyAmount(120.0);
        c302.setBountyType(BountyType.POINT);
        c302.setStatus(ContractStatus.WAIT_CONFIRM);
        c302.setCompletionStandard("核对范围并提交书面结论。");
        c302.setEvidenceRequirement("上传结论文本。");
        c302.setReviewedByPublisher(false);
        c302.setReviewedByHunter(false);
        c302.setAcceptedAt(Instant.parse("2026-07-01T15:00:00Z"));
        repo.save(c302);
    }
}
