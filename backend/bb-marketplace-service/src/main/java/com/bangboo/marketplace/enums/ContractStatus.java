package com.bangboo.marketplace.enums;

/** 契约状态机，取值对齐前端 enums.ts ContractStatus。 */
public enum ContractStatus {
    IN_PROGRESS,
    WAIT_CONFIRM,
    COMPLETED,
    CANCELLED,
    DISPUTED,
    RULED
}
