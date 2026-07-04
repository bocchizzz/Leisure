package com.bangboo.marketplace.controller;

import com.bangboo.common.response.PageResult;
import com.bangboo.marketplace.dto.CancelContractRequest;
import com.bangboo.marketplace.dto.CreateEvidenceRequest;
import com.bangboo.marketplace.dto.TaskContractVO;
import com.bangboo.marketplace.dto.TaskEvidenceVO;
import com.bangboo.marketplace.service.ContractService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/contracts")
public class ContractController {
    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping("/my-published")
    public PageResult<TaskContractVO> myPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        return contractService.myPublished(page, size, status);
    }

    @GetMapping("/my-accepted")
    public PageResult<TaskContractVO> myAccepted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        return contractService.myAccepted(page, size, status);
    }

    @GetMapping("/{id}")
    public TaskContractVO getById(@PathVariable Long id) {
        return contractService.getById(id);
    }

    @PostMapping("/{id}/evidences")
    public TaskEvidenceVO submitEvidence(@PathVariable Long id, @RequestBody CreateEvidenceRequest request) {
        return contractService.submitEvidence(id, request);
    }

    @GetMapping("/{id}/evidences")
    public List<TaskEvidenceVO> evidences(@PathVariable Long id) {
        return contractService.evidences(id);
    }

    @PutMapping("/{id}/submit-completion")
    public TaskContractVO submitCompletion(@PathVariable Long id) {
        return contractService.submitCompletion(id);
    }

    @PutMapping("/{id}/confirm-completion")
    public TaskContractVO confirmCompletion(@PathVariable Long id) {
        return contractService.confirmCompletion(id);
    }

    @PutMapping("/{id}/cancel")
    public TaskContractVO cancel(@PathVariable Long id, @RequestBody(required = false) CancelContractRequest request) {
        return contractService.cancel(id, request);
    }
}
