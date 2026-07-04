package com.bangboo.court.service;

import com.bangboo.common.constant.ErrorCode;
import com.bangboo.common.exception.BusinessException;
import com.bangboo.common.exception.ForbiddenException;
import com.bangboo.common.exception.ResourceNotFoundException;
import com.bangboo.common.response.PageResult;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.court.client.InternalApiClient;
import com.bangboo.court.dto.ContractBriefVO;
import com.bangboo.court.dto.ContractRuleResultRequest;
import com.bangboo.court.dto.CourtCaseDetailVO;
import com.bangboo.court.dto.CourtCaseVO;
import com.bangboo.court.dto.CourtEvidenceVO;
import com.bangboo.court.dto.CourtPrecedentVO;
import com.bangboo.court.dto.CourtRulingVO;
import com.bangboo.court.dto.CourtStatementVO;
import com.bangboo.court.dto.CourtVoteVO;
import com.bangboo.court.dto.CreateAuditLogRequest;
import com.bangboo.court.dto.CreateCourtCaseRequest;
import com.bangboo.court.dto.CreateCourtEvidenceRequest;
import com.bangboo.court.dto.CreateMessageRequest;
import com.bangboo.court.dto.CreateStatementRequest;
import com.bangboo.court.dto.CreateVoteRequest;
import com.bangboo.court.dto.OpsConfigVO;
import com.bangboo.court.dto.ReputationAdjustmentRequest;
import com.bangboo.court.dto.RuleCaseRequest;
import com.bangboo.court.dto.UserBriefVO;
import com.bangboo.court.dto.VoteStats;
import com.bangboo.court.entity.CourtCase;
import com.bangboo.court.entity.CourtEvidence;
import com.bangboo.court.entity.CourtPrecedent;
import com.bangboo.court.entity.CourtRuling;
import com.bangboo.court.entity.CourtStatement;
import com.bangboo.court.entity.CourtVote;
import com.bangboo.court.enums.CourtCaseStatus;
import com.bangboo.court.enums.CourtPartyRole;
import com.bangboo.court.enums.CourtVoteOption;
import com.bangboo.court.repository.CourtCaseRepository;
import com.bangboo.court.repository.CourtEvidenceRepository;
import com.bangboo.court.repository.CourtPrecedentRepository;
import com.bangboo.court.repository.CourtRulingRepository;
import com.bangboo.court.repository.CourtStatementRepository;
import com.bangboo.court.repository.CourtVoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CourtCaseService {
    private static final DateTimeFormatter CASE_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);

    private final CourtCaseRepository caseRepository;
    private final CourtStatementRepository statementRepository;
    private final CourtEvidenceRepository evidenceRepository;
    private final CourtVoteRepository voteRepository;
    private final CourtRulingRepository rulingRepository;
    private final CourtPrecedentRepository precedentRepository;
    private final CourtAuthService authService;
    private final InternalApiClient internalApiClient;

    public CourtCaseService(
            CourtCaseRepository caseRepository,
            CourtStatementRepository statementRepository,
            CourtEvidenceRepository evidenceRepository,
            CourtVoteRepository voteRepository,
            CourtRulingRepository rulingRepository,
            CourtPrecedentRepository precedentRepository,
            CourtAuthService authService,
            InternalApiClient internalApiClient
    ) {
        this.caseRepository = caseRepository;
        this.statementRepository = statementRepository;
        this.evidenceRepository = evidenceRepository;
        this.voteRepository = voteRepository;
        this.rulingRepository = rulingRepository;
        this.precedentRepository = precedentRepository;
        this.authService = authService;
        this.internalApiClient = internalApiClient;
    }

    @Transactional
    public CourtCaseVO create(CreateCourtCaseRequest request) {
        CurrentUser user = authService.requireUser();
        ContractBriefVO contract = internalApiClient.contractBrief(request.contractId())
                .orElseThrow(() -> new ResourceNotFoundException("契约不存在"));
        if (!user.uid().equals(contract.publisherId()) && !user.uid().equals(contract.hunterId())) {
            throw new ForbiddenException("只有任务双方可发起小法庭");
        }
        if (caseRepository.existsByContractIdAndStatusNot(contract.id(), CourtCaseStatus.ARCHIVED)) {
            throw new BusinessException(ErrorCode.CONFLICT, "该契约已有进行中的小法庭案件");
        }

        Long defendantId = user.uid().equals(contract.publisherId()) ? contract.hunterId() : contract.publisherId();
        UserBriefVO plaintiff = currentUserBrief(user.uid());
        UserBriefVO defendant = userBrief(defendantId).orElse(null);

        CourtCase courtCase = new CourtCase();
        courtCase.setId(nextCaseId());
        courtCase.setCaseNo(generateCaseNo());
        courtCase.setTaskId(contract.taskId());
        courtCase.setTaskTitle(contract.taskTitle());
        courtCase.setContractId(contract.id());
        courtCase.setCaseTitle(request.caseTitle().trim());
        courtCase.setType(request.type());
        courtCase.setStatus(CourtCaseStatus.VOTING);
        courtCase.setPlaintiffId(user.uid());
        courtCase.setPlaintiffName(displayName(plaintiff, "用户" + user.uid()));
        courtCase.setDefendantId(defendantId);
        courtCase.setDefendantName(displayName(defendant, "用户" + defendantId));
        courtCase.setInitialStatement(request.content().trim());
        courtCase.setSummary("案件已立案，等待双方补充陈述、证据与陪审投票。");
        courtCase = caseRepository.save(courtCase);

        CourtStatement statement = new CourtStatement();
        statement.setCaseId(courtCase.getId());
        statement.setUserId(user.uid());
        statement.setUserName(courtCase.getPlaintiffName());
        statement.setUserAvatar(plaintiff.avatarUrl());
        statement.setRole(CourtPartyRole.PLAINTIFF);
        statement.setContent(request.content().trim());
        statementRepository.save(statement);

        internalApiClient.markContractDisputed(contract.id(), courtCase.getId());
        notifyUser(defendantId, "你被卷入小法庭", "案件《" + courtCase.getCaseTitle() + "》已立案，请及时补充陈述。", courtCase.getId());
        audit(user, "COURT_CREATE", courtCase.getId(), "发起小法庭：" + courtCase.getCaseTitle());
        return toCaseVO(courtCase, user.uid());
    }

    @Transactional(readOnly = true)
    public PageResult<CourtCaseVO> list(int page, int size, String status) {
        CurrentUser user = authService.requireUser();
        Pageable pageable = pageable(page, size);
        CourtCaseStatus statusFilter = parseStatus(status);
        List<CourtCase> source = statusFilter == null
                ? caseRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                : caseRepository.findByStatus(statusFilter).stream()
                .sorted(Comparator.comparing(CourtCase::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        List<CourtCaseVO> visible = source.stream()
                .filter(courtCase -> canView(courtCase, user))
                .map(courtCase -> toCaseVO(courtCase, user.uid()))
                .toList();
        return toPageResult(slice(visible, pageable));
    }

    @Transactional(readOnly = true)
    public CourtCaseDetailVO getById(Long id) {
        CurrentUser user = authService.requireUser();
        CourtCase courtCase = getCase(id);
        if (!canView(courtCase, user)) {
            throw new ForbiddenException("暂无权限查看该案件");
        }
        return detail(courtCase, user.uid());
    }

    @Transactional
    public CourtStatementVO addStatement(Long caseId, CreateStatementRequest request) {
        CurrentUser user = authService.requireUser();
        CourtCase courtCase = getCase(caseId);
        requireParty(courtCase, user.uid(), "只有案件双方可提交陈述");
        UserBriefVO brief = currentUserBrief(user.uid());

        CourtStatement statement = new CourtStatement();
        statement.setCaseId(caseId);
        statement.setUserId(user.uid());
        statement.setUserName(displayName(brief, "用户" + user.uid()));
        statement.setUserAvatar(brief.avatarUrl());
        statement.setRole(user.uid().equals(courtCase.getPlaintiffId()) ? CourtPartyRole.PLAINTIFF : CourtPartyRole.DEFENDANT);
        statement.setContent(request.content().trim());
        statement = statementRepository.save(statement);
        audit(user, "COURT_STATEMENT", caseId, "提交案件陈述");
        return CourtMapper.toStatementVO(statement);
    }

    @Transactional
    public CourtEvidenceVO addEvidence(Long caseId, CreateCourtEvidenceRequest request) {
        CurrentUser user = authService.requireUser();
        CourtCase courtCase = getCase(caseId);
        requireParty(courtCase, user.uid(), "只有案件双方可提交证据");
        UserBriefVO brief = currentUserBrief(user.uid());

        CourtEvidence evidence = new CourtEvidence();
        evidence.setCaseId(caseId);
        evidence.setSubmitterId(user.uid());
        evidence.setSubmitterName(displayName(brief, "用户" + user.uid()));
        evidence.setType(request.type());
        evidence.setFileUrl(trimToNull(request.fileUrl()));
        evidence.setContent(trimToNull(request.content()));
        evidence = evidenceRepository.save(evidence);
        audit(user, "COURT_EVIDENCE", caseId, "提交案件证据");
        return CourtMapper.toEvidenceVO(evidence);
    }

    @Transactional
    public CourtVoteVO vote(Long caseId, CreateVoteRequest request) {
        CurrentUser user = authService.requireUser();
        CourtCase courtCase = getCase(caseId);
        if (courtCase.getStatus() != CourtCaseStatus.VOTING) {
            throw new BusinessException(ErrorCode.CONFLICT, "投票通道已关闭");
        }
        if (isParty(courtCase, user.uid())) {
            throw new ForbiddenException("案件双方不能作为陪审员投票");
        }
        if (voteRepository.existsByCaseIdAndVoterId(caseId, user.uid())) {
            throw new BusinessException(ErrorCode.CONFLICT, "你已经投过票");
        }
        UserBriefVO brief = currentUserBrief(user.uid());
        requireQualifiedJuror(brief, internalApiClient.opsConfig());

        CourtVote vote = new CourtVote();
        vote.setCaseId(caseId);
        vote.setVoterId(user.uid());
        vote.setVoterName(displayName(brief, "用户" + user.uid()));
        vote.setOption(request.option());
        vote.setWeight(1);
        vote.setReason(trimToNull(request.reason()));
        vote = voteRepository.save(vote);

        notifyUser(courtCase.getPlaintiffId(), "案件新增投票", "案件《" + courtCase.getCaseTitle() + "》收到一张陪审票。", caseId);
        notifyUser(courtCase.getDefendantId(), "案件新增投票", "案件《" + courtCase.getCaseTitle() + "》收到一张陪审票。", caseId);
        return CourtMapper.toVoteVO(vote);
    }

    @Transactional(readOnly = true)
    public VoteStats voteStats(Long caseId) {
        CurrentUser user = authService.requireUser();
        CourtCase courtCase = getCase(caseId);
        if (!canView(courtCase, user)) {
            throw new ForbiddenException("暂无权限查看投票统计");
        }
        return calculateVoteStats(caseId);
    }

    @Transactional(readOnly = true)
    public PageResult<CourtPrecedentVO> precedents(int page, int size, String keyword) {
        Pageable pageable = pageable(page, size);
        Page<CourtPrecedent> result = keyword == null || keyword.isBlank()
                ? precedentRepository.findAll(pageable)
                : precedentRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(keyword.trim(), keyword.trim(), pageable);
        List<CourtPrecedentVO> content = result.getContent().stream().map(CourtMapper::toPrecedentVO).toList();
        return new PageResult<>(content, result.getTotalElements(), result.getTotalPages(), result.getNumber(), result.getSize(), result.isFirst(), result.isLast());
    }

    @Transactional(readOnly = true)
    public PageResult<CourtCaseVO> adminList(int page, int size, String status) {
        CurrentUser admin = authService.requireAdmin();
        Pageable pageable = pageable(page, size);
        CourtCaseStatus statusFilter = parseStatus(status);
        Page<CourtCase> result = statusFilter == null
                ? caseRepository.findAll(pageable)
                : caseRepository.findByStatus(statusFilter, pageable);
        List<CourtCaseVO> content = result.getContent().stream()
                .map(courtCase -> toCaseVO(courtCase, admin.uid()))
                .toList();
        return new PageResult<>(content, result.getTotalElements(), result.getTotalPages(), result.getNumber(), result.getSize(), result.isFirst(), result.isLast());
    }

    @Transactional(readOnly = true)
    public CourtCaseDetailVO adminGetById(Long id) {
        CurrentUser admin = authService.requireAdmin();
        return detail(getCase(id), admin.uid());
    }

    /**
     * 供服务间内部调用（AI 服务生成案件摘要/点评）获取案件完整资料。
     * 由 InternalTokenFilter 保护，不需要用户登录，也不做当事人/管理员权限校验。
     */
    public CourtCaseDetailVO internalGetById(Long id) {
        return detail(getCase(id), null);
    }

    @Transactional
    public CourtCaseVO rule(Long id, RuleCaseRequest request) {
        CurrentUser admin = authService.requireAdmin();
        CourtCase courtCase = getCase(id);
        if (rulingRepository.existsByCaseId(id)) {
            throw new BusinessException(ErrorCode.CONFLICT, "该案件已经裁决");
        }

        CourtRuling ruling = new CourtRuling();
        ruling.setCaseId(id);
        ruling.setAdminId(admin.uid());
        ruling.setAdminName(adminName(admin));
        ruling.setResult(request.result());
        ruling.setBountyReleaseRate(request.bountyReleaseRate());
        ruling.setPublisherCreditDelta(request.publisherCreditDelta());
        ruling.setHunterCreditDelta(request.hunterCreditDelta());
        ruling.setReason(request.reason().trim());
        rulingRepository.save(ruling);

        courtCase.setStatus(request.shouldArchiveAsPrecedent() ? CourtCaseStatus.ARCHIVED : CourtCaseStatus.RULED);
        courtCase.setRuledAt(Instant.now());

        internalApiClient.applyContractRuling(courtCase.getContractId(), new ContractRuleResultRequest(
                id,
                request.result(),
                request.bountyReleaseRate(),
                request.publisherCreditDelta(),
                request.hunterCreditDelta(),
                request.reason().trim()
        ));
        adjustReputation(courtCase.getPlaintiffId(), request.publisherCreditDelta(), id, request.reason());
        adjustReputation(courtCase.getDefendantId(), request.hunterCreditDelta(), id, request.reason());
        if (request.shouldArchiveAsPrecedent()) {
            archivePrecedent(courtCase, ruling);
        }
        notifyUser(courtCase.getPlaintiffId(), "小法庭已裁决", "案件《" + courtCase.getCaseTitle() + "》已完成裁决。", id);
        notifyUser(courtCase.getDefendantId(), "小法庭已裁决", "案件《" + courtCase.getCaseTitle() + "》已完成裁决。", id);
        audit(admin, "COURT_RULE", id, "管理员裁决：" + request.result());
        return toCaseVO(courtCase, admin.uid());
    }

    private CourtCaseDetailVO detail(CourtCase courtCase, Long currentUserId) {
        List<CourtStatementVO> statements = statementRepository.findByCaseIdOrderByCreatedAtAsc(courtCase.getId()).stream()
                .map(CourtMapper::toStatementVO)
                .toList();
        List<CourtEvidenceVO> evidences = evidenceRepository.findByCaseIdOrderByCreatedAtDesc(courtCase.getId()).stream()
                .map(CourtMapper::toEvidenceVO)
                .toList();
        CourtRulingVO ruling = rulingRepository.findByCaseId(courtCase.getId()).map(CourtMapper::toRulingVO).orElse(null);
        return new CourtCaseDetailVO(toCaseVO(courtCase, currentUserId), statements, evidences, ruling);
    }

    private CourtCaseVO toCaseVO(CourtCase courtCase, Long currentUserId) {
        return CourtMapper.toCaseVO(
                courtCase,
                calculateVoteStats(courtCase.getId()),
                currentUserId != null && voteRepository.existsByCaseIdAndVoterId(courtCase.getId(), currentUserId)
        );
    }

    private VoteStats calculateVoteStats(Long caseId) {
        List<CourtVote> votes = voteRepository.findByCaseId(caseId);
        int totalVotes = votes.size();
        int totalWeight = votes.stream().mapToInt(vote -> Math.max(vote.getWeight(), 1)).sum();
        if (totalWeight == 0) {
            return new VoteStats(0, 0, 0, 0, 0, 0);
        }
        return new VoteStats(
                rate(votes, CourtVoteOption.SUPPORT_PUBLISHER, totalWeight),
                rate(votes, CourtVoteOption.SUPPORT_HUNTER, totalWeight),
                rate(votes, CourtVoteOption.INSUFFICIENT_EVIDENCE, totalWeight),
                rate(votes, CourtVoteOption.SUGGEST_SETTLEMENT, totalWeight),
                totalVotes,
                totalWeight
        );
    }

    private double rate(List<CourtVote> votes, CourtVoteOption option, int totalWeight) {
        int weight = votes.stream()
                .filter(vote -> vote.getOption() == option)
                .mapToInt(vote -> Math.max(vote.getWeight(), 1))
                .sum();
        return (double) weight / totalWeight;
    }

    private boolean canView(CourtCase courtCase, CurrentUser user) {
        if (authService.isAdmin(user) || isParty(courtCase, user.uid())) {
            return true;
        }
        if (courtCase.getStatus() != CourtCaseStatus.VOTING) {
            return false;
        }
        return userBrief(user.uid()).map(brief -> isQualifiedJuror(brief, internalApiClient.opsConfig())).orElse(false);
    }

    private void requireQualifiedJuror(UserBriefVO brief, OpsConfigVO opsConfig) {
        if (!isQualifiedJuror(brief, opsConfig)) {
            throw new ForbiddenException("信誉分不足，暂不具备陪审员资格");
        }
    }

    private boolean isQualifiedJuror(UserBriefVO brief, OpsConfigVO opsConfig) {
        return brief != null
                && "ACTIVE".equals(String.valueOf(brief.status()))
                && brief.campusVerified()
                && !brief.isAdmin()
                && brief.reputation() >= opsConfig.juryMinReputation();
    }

    private CourtCase getCase(Long id) {
        return caseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("案件不存在"));
    }

    private void requireParty(CourtCase courtCase, Long userId, String message) {
        if (!isParty(courtCase, userId)) {
            throw new ForbiddenException(message);
        }
    }

    private boolean isParty(CourtCase courtCase, Long userId) {
        return courtCase.getPlaintiffId().equals(userId) || courtCase.getDefendantId().equals(userId);
    }

    private UserBriefVO currentUserBrief(Long userId) {
        return userBrief(userId).orElse(new UserBriefVO(userId, "user" + userId, "用户" + userId, null, "ACTIVE", false, 0, List.of("USER")));
    }

    private Optional<UserBriefVO> userBrief(Long userId) {
        return internalApiClient.userBrief(userId);
    }

    private String displayName(UserBriefVO brief, String fallback) {
        if (brief == null) {
            return fallback;
        }
        if (brief.nickname() != null && !brief.nickname().isBlank()) {
            return brief.nickname();
        }
        if (brief.username() != null && !brief.username().isBlank()) {
            return brief.username();
        }
        return fallback;
    }

    private String adminName(CurrentUser admin) {
        return userBrief(admin.uid()).map(brief -> displayName(brief, "管理员" + admin.uid())).orElse("管理员" + admin.uid());
    }

    private String generateCaseNo() {
        Instant now = Instant.now();
        return "COURT-" + CASE_NO_TIME.format(now) + "-" + Long.toString(now.toEpochMilli()).substring(8);
    }

    private Long nextCaseId() {
        return Math.max(caseRepository.maxId(), 400L) + 1;
    }

    private CourtCaseStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return CourtCaseStatus.valueOf(status.trim());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "案件状态不支持");
        }
    }

    private Pageable pageable(int page, int size) {
        int normalizedSize = size <= 0 ? 10 : Math.min(size, 100);
        return PageRequest.of(Math.max(page, 0), normalizedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private <T> Page<T> slice(List<T> rows, Pageable pageable) {
        int start = Math.toIntExact(Math.min(pageable.getOffset(), rows.size()));
        int end = Math.min(start + pageable.getPageSize(), rows.size());
        return new PageImpl<>(rows.subList(start, end), pageable, rows.size());
    }

    private <T> PageResult<T> toPageResult(Page<T> page) {
        return new PageResult<>(page.getContent(), page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize(), page.isFirst(), page.isLast());
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void archivePrecedent(CourtCase courtCase, CourtRuling ruling) {
        CourtPrecedent precedent = new CourtPrecedent();
        precedent.setCaseId(courtCase.getId());
        precedent.setTitle(courtCase.getCaseTitle());
        precedent.setSummary(firstNonBlank(courtCase.getAiSummary(), courtCase.getSummary(), ruling.getReason()));
        precedent.setRulingResult(ruling.getResult());
        precedent.setTags(courtCase.getType().name() + ",小法庭裁决");
        precedentRepository.save(precedent);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "小法庭裁决已归档。";
    }

    private void notifyUser(Long userId, String title, String content, Long relatedId) {
        internalApiClient.createMessage(new CreateMessageRequest(userId, "COURT", title, content, relatedId));
    }

    private void audit(CurrentUser operator, String action, Long targetId, String detail) {
        internalApiClient.createAuditLog(new CreateAuditLogRequest(
                operator.uid(),
                adminName(operator),
                action,
                "COURT_CASE",
                targetId,
                detail
        ));
    }

    private void adjustReputation(Long userId, int delta, Long sourceId, String reason) {
        if (delta == 0) {
            return;
        }
        internalApiClient.adjustReputation(userId, new ReputationAdjustmentRequest(delta, reason, "COURT_RULING", sourceId));
    }
}
