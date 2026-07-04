package com.bangboo.marketplace.enums;

/** 任务状态机，取值对齐前端 enums.ts TaskStatus。 */
public enum TaskStatus {
    DRAFT,
    PENDING_REVIEW,
    PUBLISHED,
    ASSIGNED,
    IN_PROGRESS,
    WAIT_CONFIRM,
    COMPLETED,
    DISPUTED,
    COURT_REVIEW,
    RULED,
    CANCELLED,
    REMOVED
}
