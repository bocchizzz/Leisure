package com.bangboo.marketplace.dto;

import com.bangboo.marketplace.enums.BountyType;
import com.bangboo.marketplace.enums.TaskCategory;
import com.bangboo.marketplace.enums.TaskStatus;

/** 任务大厅查询参数，对齐前端 TaskQuery。 */
public class TaskQuery {
    private String keyword;
    private TaskCategory category;
    private String difficulty;
    private Double minBounty;
    private Double maxBounty;
    private TaskStatus status;
    private BountyType bountyType;
    private Integer page = 0;
    private Integer size = 10;
    private String sort;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public TaskCategory getCategory() {
        return category;
    }

    public void setCategory(TaskCategory category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Double getMinBounty() {
        return minBounty;
    }

    public void setMinBounty(Double minBounty) {
        this.minBounty = minBounty;
    }

    public Double getMaxBounty() {
        return maxBounty;
    }

    public void setMaxBounty(Double maxBounty) {
        this.maxBounty = maxBounty;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public BountyType getBountyType() {
        return bountyType;
    }

    public void setBountyType(BountyType bountyType) {
        this.bountyType = bountyType;
    }

    public Integer getPage() {
        return page == null ? 0 : page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size == null || size <= 0 ? 10 : size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
