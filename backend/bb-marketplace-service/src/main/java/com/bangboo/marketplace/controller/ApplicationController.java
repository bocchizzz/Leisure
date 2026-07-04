package com.bangboo.marketplace.controller;

import com.bangboo.common.response.PageResult;
import com.bangboo.marketplace.dto.TaskApplicationVO;
import com.bangboo.marketplace.dto.TaskContractVO;
import com.bangboo.marketplace.dto.UpdateApplicationRequest;
import com.bangboo.marketplace.service.ApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/applications")
public class ApplicationController {
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/my")
    public PageResult<TaskApplicationVO> my(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        return applicationService.my(page, size, status);
    }

    @PutMapping("/{id}")
    public TaskApplicationVO update(@PathVariable Long id, @RequestBody(required = false) UpdateApplicationRequest request) {
        UpdateApplicationRequest body = request == null ? new UpdateApplicationRequest(null, null) : request;
        return applicationService.update(id, body);
    }

    @PutMapping("/{id}/cancel")
    public TaskApplicationVO cancel(@PathVariable Long id) {
        return applicationService.cancel(id);
    }

    @PutMapping("/{id}/accept")
    public TaskContractVO accept(@PathVariable Long id) {
        return applicationService.accept(id);
    }

    @PutMapping("/{id}/reject")
    public TaskApplicationVO reject(@PathVariable Long id) {
        return applicationService.reject(id);
    }
}
