package cn.master.backend.service.impl;

import cn.master.backend.constants.ApplicationNumScope;
import cn.master.backend.constants.CaseEvent;
import cn.master.backend.constants.CaseReviewStatus;
import cn.master.backend.constants.FunctionalCaseReviewStatus;
import cn.master.backend.entity.*;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.handler.provider.BaseCaseProvider;
import cn.master.backend.handler.result.CaseManagementResultCode;
import cn.master.backend.handler.uid.IDGenerator;
import cn.master.backend.mapper.*;
import cn.master.backend.payload.dto.functional.CaseReviewDTO;
import cn.master.backend.payload.dto.functional.CaseReviewUserDTO;
import cn.master.backend.payload.dto.project.ModuleCountDTO;
import cn.master.backend.payload.dto.user.UserDTO;
import cn.master.backend.payload.request.functional.*;
import cn.master.backend.payload.request.system.PosRequest;
import cn.master.backend.service.CaseReviewModuleService;
import cn.master.backend.service.CaseReviewService;
import cn.master.backend.service.DeleteCaseReviewService;
import cn.master.backend.service.FunctionalCaseService;
import cn.master.backend.util.NumGenerator;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.CaseReviewFollowerTableDef.CASE_REVIEW_FOLLOWER;
import static cn.master.backend.entity.table.CaseReviewFunctionalCaseTableDef.CASE_REVIEW_FUNCTIONAL_CASE;
import static cn.master.backend.entity.table.CaseReviewModuleTableDef.CASE_REVIEW_MODULE;
import static cn.master.backend.entity.table.CaseReviewTableDef.CASE_REVIEW;
import static cn.master.backend.entity.table.CaseReviewUserTableDef.CASE_REVIEW_USER;
import static cn.master.backend.entity.table.FunctionalCaseTableDef.FUNCTIONAL_CASE;
import static cn.master.backend.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.backend.entity.table.UserTableDef.USER;

/**
 * 用例评审 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-10
 */
@Service
@RequiredArgsConstructor
public class CaseReviewServiceImpl extends ServiceImpl<CaseReviewMapper, CaseReview> implements CaseReviewService {
    private final CaseReviewModuleService caseReviewModuleService;
    private final FunctionalCaseService functionalCaseService;
    private final CaseReviewUserMapper caseReviewUserMapper;
    private final CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    private final CaseReviewFunctionalCaseUserMapper caseReviewFunctionalCaseUserMapper;
    private final CaseReviewFollowerMapper caseReviewFollowerMapper;
    private final BaseCaseProvider provider;
    private final DeleteCaseReviewService deleteCaseReviewService;

    public static final int POS_STEP = 5000;
    private static final String CASE_MODULE_COUNT_ALL = "all";

    @Override
    public CaseReview checkCaseReview(String caseReviewId) {
        return queryChain().where(CaseReview::getId).eq(caseReviewId).oneOpt()
                .orElseThrow(() -> new MSException(CaseManagementResultCode.CASE_REVIEW_NOT_FOUND));
    }

    @Override
    public Long getCaseFunctionalCaseNextPos(String reviewId) {

        return 0L;
    }

    @Override
    public Page<CaseReviewDTO> getCaseReviewPage(CaseReviewPageRequest request) {
        Page<CaseReviewDTO> caseReviewPage = queryChain()
                .select(CASE_REVIEW.ALL_COLUMNS, CASE_REVIEW_MODULE.NAME.as("moduleName"))
                .from(CASE_REVIEW)
                .leftJoin(CASE_REVIEW_MODULE).on(CASE_REVIEW.MODULE_ID.eq(CASE_REVIEW_MODULE.ID))
                .where(CASE_REVIEW.PROJECT_ID.eq(request.getProjectId()))
                .and(CASE_REVIEW.MODULE_ID.in(request.getModuleIds()))
                .and(CASE_REVIEW.NAME.like(request.getKeyword())
                        .or(CASE_REVIEW.NUM.like(request.getKeyword()))
                        .or(CASE_REVIEW.TAGS.like(request.getKeyword())))
                .and(CASE_REVIEW.CREATE_USER.eq(request.getCreateByMe()))
                .and(CASE_REVIEW.ID.in(
                        QueryChain.of(CaseReviewUser.class).select(CASE_REVIEW_USER.REVIEW_ID)
                                .from(CASE_REVIEW_USER).where(CASE_REVIEW_USER.USER_ID.eq(request.getReviewByMe()))
                                .listAs(String.class)
                ).when(Objects.nonNull(request.getReviewByMe())))
                .orderBy(CASE_REVIEW.POS.desc())
                .pageAs(Page.of(request.getCurrent(), request.getPageSize()), CaseReviewDTO.class);
        List<String> reviewIds = caseReviewPage.getRecords().stream().map(CaseReview::getId).toList();
        Map<String, List<CaseReviewFunctionalCase>> reviewCaseMap = getReviewCaseMap(reviewIds);
        List<CaseReviewUserDTO> reviewUsers = getReviewUsers(reviewIds);
        Map<String, List<CaseReviewUserDTO>> reviewUserMap = reviewUsers.stream().collect(Collectors.groupingBy(CaseReviewUserDTO::getReviewId));
        for (CaseReviewDTO caseReviewDTO : caseReviewPage.getRecords()) {
            buildCaseReviewDTO(caseReviewDTO, reviewCaseMap, reviewUserMap);
        }
        return caseReviewPage;
    }

    @Override
    public Map<String, Long> moduleCount(CaseReviewPageRequest request) {
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        request.setModuleIds(null);
        List<ModuleCountDTO> moduleCountDTOList = countModuleIdByKeywordAndFileType(request);
        Map<String, Long> moduleCountMap = caseReviewModuleService.getModuleCountMap(request.getProjectId(), moduleCountDTOList);
        //查出全部用例数量
        long allCount = caseCount(request);
        moduleCountMap.put(CASE_MODULE_COUNT_ALL, allCount);
        return moduleCountMap;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public CaseReview addCaseReview(CaseReviewRequest request, String userId) {
        String caseReviewId = IDGenerator.nextStr();
        BaseAssociateCaseRequest baseAssociateCaseRequest = request.getBaseAssociateCaseRequest();
        List<String> caseIds = doSelectIds(baseAssociateCaseRequest, baseAssociateCaseRequest.getProjectId());
        CaseReview caseReview = addCaseReview(request, userId, caseReviewId, caseIds);
        addAssociate(request, userId, caseReviewId, caseIds, baseAssociateCaseRequest.getReviewers());
        return caseReview;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public CaseReview copyCaseReview(CaseReviewCopyRequest request, String userId) {
        String caseReviewId = IDGenerator.nextStr();
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = QueryChain.of(CaseReviewFunctionalCase.class)
                .where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(request.getCopyId())).list();
        List<String> caseIds = caseReviewFunctionalCases.stream().map(CaseReviewFunctionalCase::getCaseId).distinct().toList();
        CaseReview caseReview = addCaseReview(request, userId, caseReviewId, caseIds);
        BaseAssociateCaseRequest baseAssociateCaseRequest = request.getBaseAssociateCaseRequest();
        if (baseAssociateCaseRequest != null) {
            addAssociate(request, userId, caseReviewId, caseIds, baseAssociateCaseRequest.getReviewers());
        } else {
            addAssociate(request, userId, caseReviewId, caseIds, request.getReviewers());
        }
        return caseReview;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void editCaseReview(CaseReviewRequest request, String userId) {
        String reviewId = request.getId();
        checkCaseReview(reviewId);
        CaseReview caseReview = new CaseReview();
        caseReview.setId(reviewId);
        caseReview.setProjectId(request.getProjectId());
        caseReview.setName(request.getName());
        caseReview.setModuleId(request.getModuleId());
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            caseReview.setTags(request.getTags());
        } else {
            caseReview.setTags(new ArrayList<>());
        }
        caseReview.setDescription(request.getDescription());
        checkAndSetStartAndEndTime(request, caseReview);
        caseReview.setUpdateUser(userId);
        mapper.update(caseReview);
        QueryChain<CaseReviewUser> queryChain = QueryChain.of(CaseReviewUser.class).where(CASE_REVIEW_USER.REVIEW_ID.eq(reviewId));
        LogicDeleteManager.execWithoutLogicDelete(() -> caseReviewUserMapper.deleteByQuery(queryChain));
        //保存评审和评审人的关系
        addCaseReviewUser(request, reviewId);
    }

    @Override
    public List<UserDTO> getReviewUserList(String projectId, String keyword) {
        return QueryChain.of(User.class).select(USER.ID, USER.NAME, USER.EMAIL)
                .from(USER)
                .leftJoin(USER_ROLE_RELATION).on(USER.ID.eq(USER_ROLE_RELATION.USER_ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(projectId))
                .and(USER.NAME.like(keyword)).groupBy(USER_ROLE_RELATION.USER_ID).limit(1000)
                .listAs(UserDTO.class);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void editFollower(String caseReviewId, String userId) {
        checkCaseReview(caseReviewId);
        QueryChain<CaseReviewFollower> queryChain = QueryChain.of(CaseReviewFollower.class).where(CASE_REVIEW_FOLLOWER.REVIEW_ID.eq(caseReviewId)
                .and(CASE_REVIEW_FOLLOWER.USER_ID.eq(userId)));
        if (queryChain.exists()) {
            LogicDeleteManager.execWithoutLogicDelete(() -> caseReviewFollowerMapper.deleteByQuery(queryChain));
        } else {
            CaseReviewFollower caseReviewFollower = new CaseReviewFollower();
            caseReviewFollower.setReviewId(caseReviewId);
            caseReviewFollower.setUserId(userId);
            caseReviewFollowerMapper.insert(caseReviewFollower);
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void associateCase(CaseReviewAssociateRequest request, String userId) {
        String caseReviewId = request.getReviewId();
        checkCaseReview(caseReviewId);
        BaseAssociateCaseRequest baseAssociateCaseRequest = request.getBaseAssociateCaseRequest();
        List<String> caseIds = doSelectIds(baseAssociateCaseRequest, baseAssociateCaseRequest.getProjectId());
        if (caseIds.isEmpty()) {
            return;
        }
        List<FunctionalCase> functionalCases = QueryChain.of(FunctionalCase.class).where(FunctionalCase::getId).in(caseIds).list();
        if (functionalCases.isEmpty()) {
            return;
        }
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = getCaseReviewFunctionalCaseList(caseReviewId, null, false);
        List<String> castIds = caseReviewFunctionalCases.stream().map(CaseReviewFunctionalCase::getCaseId).toList();
        List<String> caseRealIds = caseIds.stream().filter(t -> !castIds.contains(t)).toList();
        //保存和用例的关系
        addCaseReviewFunctionalCase(caseRealIds, userId, caseReviewId);
        //保存用例和用例评审人的关系
        addCaseReviewFunctionalCaseUser(caseRealIds, request.getReviewers(), caseReviewId);
        Map<String, Object> param = new HashMap<>();
        param.put(CaseEvent.Param.USER_ID, userId);
        param.put(CaseEvent.Param.REVIEW_ID, caseReviewId);
        param.put(CaseEvent.Param.CASE_IDS, caseRealIds);
        param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.ASSOCIATE);
        provider.updateCaseReview(param);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void disassociate(String reviewId, String caseId, String userId) {
        QueryChain.of(FunctionalCase.class).where(FunctionalCase::getId).eq(caseId).oneOpt()
                .orElseThrow(() -> new MSException(CaseManagementResultCode.FUNCTIONAL_CASE_NOT_FOUND));
        QueryChain<CaseReviewFunctionalCase> queryChain = QueryChain.of(CaseReviewFunctionalCase.class).where(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID.eq(caseId)
                .and(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(reviewId)));
        LogicDeleteManager.execWithoutLogicDelete(() -> caseReviewFunctionalCaseMapper.deleteByQuery(queryChain));
        Map<String, Object> param = new HashMap<>();
        param.put(CaseEvent.Param.REVIEW_ID, reviewId);
        param.put(CaseEvent.Param.CASE_IDS, List.of(caseId));
        param.put(CaseEvent.Param.USER_ID, userId);
        param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.DISASSOCIATE);
        provider.updateCaseReview(param);
    }

    @Override
    public void editPos(PosRequest request) {
        // todo
    }

    @Override
    public CaseReviewDTO getCaseReviewDetail(String id, String userId) {
        CaseReview caseReview = checkCaseReview(id);
        CaseReviewDTO caseReviewDTO = new CaseReviewDTO();
        BeanUtils.copyProperties(caseReview, caseReviewDTO);
        Boolean isFollow = checkFollow(id, userId);
        caseReviewDTO.setFollowFlag(isFollow);
        Map<String, List<CaseReviewFunctionalCase>> reviewCaseMap = getReviewCaseMap(List.of(id));
        List<CaseReviewUserDTO> reviewUsers = getReviewUsers(List.of(id));
        Map<String, List<CaseReviewUserDTO>> reviewUsersMap = new HashMap<>();
        reviewUsersMap.put(id, reviewUsers);
        buildCaseReviewDTO(caseReviewDTO, reviewCaseMap, reviewUsersMap);
        return caseReviewDTO;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void batchMoveCaseReview(CaseReviewBatchRequest request, String userId) {
        List<String> ids;
        if (StringUtils.isBlank(request.getMoveModuleId())) {
            throw new MSException(Translator.get("functional_case.module_id.not_blank"));
        }
        if (request.isSelectAll()) {
            ids = getIds(request, request.getProjectId());
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }

        } else {
            ids = request.getSelectIds();
        }
        if (CollectionUtils.isNotEmpty(ids)) {
            updateChain().set(CASE_REVIEW.MODULE_ID, request.getMoveModuleId())
                    .set(CASE_REVIEW.UPDATE_USER, userId)
                    .where(CASE_REVIEW.ID.in(ids)).update();
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteCaseReview(String reviewId, String projectId) {
        deleteCaseReviewService.deleteCaseReviewResource(List.of(reviewId), projectId);
    }

    private List<String> getIds(CaseReviewBatchRequest request, String projectId) {
        return queryChain().select(CASE_REVIEW.ID).from(CASE_REVIEW)
                .where(CASE_REVIEW.PROJECT_ID.eq(projectId))
                .and(CASE_REVIEW.MODULE_ID.in(request.getModuleIds()))
                .and(CASE_REVIEW.NAME.like(request.getCondition().getKeyword())
                        .or(CASE_REVIEW.NUM.like(request.getCondition().getKeyword())))
                .and(CASE_REVIEW.CREATE_USER.eq(request.getCreateByMe()))
                .and(CASE_REVIEW.ID.in(
                        QueryChain.of(CaseReviewUser.class).select(CASE_REVIEW_USER.REVIEW_ID).from(CASE_REVIEW_USER)
                                .where(CASE_REVIEW_USER.USER_ID.eq(request.getReviewByMe()))
                ).when(Objects.nonNull(request.getReviewByMe())))
                .listAs(String.class);
    }

    private Boolean checkFollow(String id, String userId) {
        return QueryChain.of(CaseReviewFollower.class).where(CASE_REVIEW_FOLLOWER.REVIEW_ID.eq(id)
                .and(CASE_REVIEW_FOLLOWER.USER_ID.eq(userId))).exists();
    }

    private void addAssociate(CaseReviewRequest request, String userId, String caseReviewId, List<String> caseIds, List<String> reviewers) {
        //保存和评审人的关系
        addCaseReviewUser(request, caseReviewId);
        //保存和用例的关系
        addCaseReviewFunctionalCase(caseIds, userId, caseReviewId);
        //保存用例和用例评审人的关系
        addCaseReviewFunctionalCaseUser(caseIds, reviewers, caseReviewId);
    }

    private void addCaseReviewFunctionalCaseUser(List<String> caseIds, List<String> reviewers, String caseReviewId) {
        if (CollectionUtils.isNotEmpty(caseIds)) {
            caseIds.forEach(caseId -> {
                reviewers.forEach(reviewer -> {
                    CaseReviewFunctionalCaseUser caseReviewFunctionalCaseUser = new CaseReviewFunctionalCaseUser();
                    caseReviewFunctionalCaseUser.setCaseId(caseId);
                    caseReviewFunctionalCaseUser.setUserId(reviewer);
                    caseReviewFunctionalCaseUser.setReviewId(caseReviewId);
                    caseReviewFunctionalCaseUserMapper.insert(caseReviewFunctionalCaseUser);
                });
            });
        }
    }

    private void addCaseReviewFunctionalCase(List<String> caseIds, String userId, String caseReviewId) {
        if (CollectionUtils.isNotEmpty(caseIds)) {
            Long nextPos = getCaseFunctionalCaseNextPos(caseReviewId);
            caseIds.forEach(caseId -> {
                CaseReviewFunctionalCase caseReviewFunctionalCase = new CaseReviewFunctionalCase();
                caseReviewFunctionalCase.setReviewId(caseReviewId);
                caseReviewFunctionalCase.setCaseId(caseId);
                caseReviewFunctionalCase.setStatus(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
                caseReviewFunctionalCase.setCreateUser(userId);
                caseReviewFunctionalCase.setId(IDGenerator.nextStr());
                caseReviewFunctionalCase.setPos(nextPos + POS_STEP);
                caseReviewFunctionalCaseMapper.insert(caseReviewFunctionalCase);
            });
        }
    }

    private void addCaseReviewUser(CaseReviewRequest request, String caseReviewId) {
        request.getReviewers().forEach(user -> {
            CaseReviewUser caseReviewUser = new CaseReviewUser();
            caseReviewUser.setReviewId(caseReviewId);
            caseReviewUser.setUserId(user);
            caseReviewUserMapper.insert(caseReviewUser);
        });
    }

    private CaseReview addCaseReview(CaseReviewRequest request, String userId, String caseReviewId, List<String> caseIds) {
        CaseReview caseReview = new CaseReview();
        caseReview.setNum(getNextNum(request.getProjectId()));
        caseReview.setProjectId(request.getProjectId());
        caseReview.setName(request.getName());
        caseReview.setModuleId(request.getModuleId());
        caseReview.setStatus(CaseReviewStatus.PREPARED.toString());
        caseReview.setReviewPassRule(request.getReviewPassRule());
        caseReview.setPos(getNextPos(request.getProjectId()));
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            caseReview.setTags(request.getTags());
        }
        caseReview.setPassRate(BigDecimal.valueOf(0.00));
        if (CollectionUtils.isEmpty(caseIds)) {
            caseReview.setCaseCount(0);
        } else {
            caseReview.setCaseCount(caseIds.size());
        }
        caseReview.setDescription(request.getDescription());
        checkAndSetStartAndEndTime(request, caseReview);
        caseReview.setCreateUser(userId);
        caseReview.setUpdateUser(userId);
        mapper.insert(caseReview);
        return caseReview;
    }

    private void checkAndSetStartAndEndTime(CaseReviewRequest request, CaseReview caseReview) {
        if (Objects.nonNull(request.getStartTime()) && request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new MSException(Translator.get("permission.case_review.start_time"));
        } else {
            caseReview.setStartTime(request.getStartTime());
        }
        if (Objects.nonNull(request.getEndTime()) && request.getEndTime().isBefore(LocalDateTime.now())) {
            throw new MSException(Translator.get("permission.case_review.end_time"));
        } else {
            caseReview.setEndTime(request.getEndTime());
        }
    }

    private Long getNextPos(String projectId) {
        Long pos = queryChain().select(CASE_REVIEW.POS)
                .from(CASE_REVIEW)
                .where(CASE_REVIEW.PROJECT_ID.eq(projectId)).orderBy(CASE_REVIEW.POS.desc()).limit(1)
                .oneAs(Long.class);
        return (pos == null ? 0 : pos) + POS_STEP;
    }

    private Long getNextNum(String projectId) {
        return NumGenerator.nextNum(projectId, ApplicationNumScope.REVIEW_CASE_MANAGEMENT);
    }

    private List<String> doSelectIds(BaseAssociateCaseRequest request, String projectId) {
        if (request.isSelectAll()) {
            List<String> ids = functionalCaseService.getIds(request, projectId, false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    private long caseCount(CaseReviewPageRequest request) {
        return queryChain().select(QueryMethods.count(CASE_REVIEW.ID)).from(CASE_REVIEW)
                .where(CASE_REVIEW.PROJECT_ID.eq(request.getProjectId()))
                .and(CASE_REVIEW.MODULE_ID.in(request.getModuleIds()))
                .and(CASE_REVIEW.NAME.like(request.getKeyword())
                        .or(CASE_REVIEW.NUM.like(request.getKeyword()))
                        .or(CASE_REVIEW.TAGS.like(request.getKeyword())))
                .and(CASE_REVIEW.CREATE_USER.eq(request.getCreateByMe()))
                .and(CASE_REVIEW.ID.in(
                        QueryChain.of(CaseReviewUser.class).select(CASE_REVIEW_USER.REVIEW_ID)
                                .from(CASE_REVIEW_USER).where(CASE_REVIEW_USER.USER_ID.eq(request.getReviewByMe()))
                                .listAs(String.class)
                ).when(Objects.nonNull(request.getReviewByMe())))
                .count();
    }

    private List<ModuleCountDTO> countModuleIdByKeywordAndFileType(CaseReviewPageRequest request) {
        return queryChain().select(CASE_REVIEW.MODULE_ID, QueryMethods.count(CASE_REVIEW.ID).as("dataCount"))
                .from(CASE_REVIEW)
                .where(CASE_REVIEW.MODULE_ID.eq(request.getModuleIds()))
                .and(CASE_REVIEW.NAME.like(request.getKeyword())
                        .or(CASE_REVIEW.NUM.like(request.getKeyword()))
                        .or(CASE_REVIEW.TAGS.like(request.getKeyword())))
                .and(CASE_REVIEW.CREATE_USER.eq(request.getCreateByMe()))
                .and(CASE_REVIEW.ID.in(
                        QueryChain.of(CaseReviewUser.class).select(CASE_REVIEW_USER.REVIEW_ID)
                                .from(CASE_REVIEW_USER).where(CASE_REVIEW_USER.USER_ID.eq(request.getReviewByMe()))
                                .listAs(String.class)
                ).when(Objects.nonNull(request.getReviewByMe())))
                .groupBy(CASE_REVIEW.MODULE_ID)
                .listAs(ModuleCountDTO.class);
    }

    private void buildCaseReviewDTO(CaseReviewDTO caseReviewDTO, Map<String, List<CaseReviewFunctionalCase>> reviewCaseMap, Map<String, List<CaseReviewUserDTO>> reviewUserMap) {
        String caseReviewId = caseReviewDTO.getId();
        List<CaseReviewFunctionalCase> caseReviewFunctionalCaseList = reviewCaseMap.get(caseReviewId);
        if (CollectionUtils.isEmpty(caseReviewFunctionalCaseList)) {
            caseReviewDTO.setPassCount(0);
            caseReviewDTO.setUnPassCount(0);
            caseReviewDTO.setReReviewedCount(0);
            caseReviewDTO.setUnderReviewedCount(0);
            caseReviewDTO.setReviewedCount(0);
            caseReviewDTO.setUnReviewCount(0);
        } else {
            buildAboutCaseCount(caseReviewDTO, caseReviewFunctionalCaseList);
        }
        caseReviewDTO.setReviewers(reviewUserMap.get(caseReviewId));
    }

    private void buildAboutCaseCount(CaseReviewDTO caseReviewDTO, List<CaseReviewFunctionalCase> caseReviewFunctionalCaseList) {
        Map<String, List<CaseReviewFunctionalCase>> statusCaseMap = caseReviewFunctionalCaseList.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getStatus));

        List<CaseReviewFunctionalCase> passList = statusCaseMap.get(FunctionalCaseReviewStatus.PASS.toString());
        if (Objects.isNull(passList)) {
            passList = new ArrayList<>();
        }
        caseReviewDTO.setPassCount(passList.size());

        BigDecimal passRate = BigDecimal.valueOf(caseReviewDTO.getPassCount()).divide(BigDecimal.valueOf(caseReviewDTO.getCaseCount()), 2, RoundingMode.HALF_UP);
        caseReviewDTO.setPassRate(passRate.multiply(BigDecimal.valueOf(100)));

        List<CaseReviewFunctionalCase> unPassList = statusCaseMap.get(FunctionalCaseReviewStatus.UN_PASS.toString());
        if (Objects.isNull(unPassList)) {
            unPassList = new ArrayList<>();
        }
        caseReviewDTO.setUnPassCount(unPassList.size());

        List<CaseReviewFunctionalCase> reReviewedList = statusCaseMap.get(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
        if (Objects.isNull(reReviewedList)) {
            reReviewedList = new ArrayList<>();
        }
        caseReviewDTO.setReReviewedCount(reReviewedList.size());

        List<CaseReviewFunctionalCase> underReviewedList = statusCaseMap.get(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
        if (Objects.isNull(underReviewedList)) {
            underReviewedList = new ArrayList<>();
        }
        caseReviewDTO.setUnderReviewedCount(underReviewedList.size());

        caseReviewDTO.setReviewedCount(caseReviewDTO.getPassCount() + caseReviewDTO.getUnPassCount());

        List<CaseReviewFunctionalCase> unReviewList = statusCaseMap.get(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
        if (Objects.isNull(unReviewList)) {
            unReviewList = new ArrayList<>();
        }
        caseReviewDTO.setUnReviewCount(unReviewList.size());
    }

    private List<CaseReviewUserDTO> getReviewUsers(List<String> reviewIds) {
        return QueryChain.of(CaseReviewUser.class).select(CASE_REVIEW_USER.USER_ID, CASE_REVIEW_USER.REVIEW_ID,
                        USER.NAME.as("userName"))
                .from(CASE_REVIEW_USER).leftJoin(USER).on(CASE_REVIEW_USER.USER_ID.eq(USER.ID))
                .where(CASE_REVIEW_USER.REVIEW_ID.in(reviewIds))
                .listAs(CaseReviewUserDTO.class);
    }

    private Map<String, List<CaseReviewFunctionalCase>> getReviewCaseMap(List<String> reviewIds) {
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = getCaseReviewFunctionalCaseList(null, reviewIds, false);
        return caseReviewFunctionalCases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getReviewId));
    }

    private List<CaseReviewFunctionalCase> getCaseReviewFunctionalCaseList(String reviewId, List<String> reviewIds, boolean deleted) {
        return QueryChain.of(CaseReviewFunctionalCase.class).select(CASE_REVIEW_FUNCTIONAL_CASE.ID, CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID,
                        CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID, CASE_REVIEW_FUNCTIONAL_CASE.STATUS,
                        CASE_REVIEW_FUNCTIONAL_CASE.CREATE_TIME, CASE_REVIEW_FUNCTIONAL_CASE.CREATE_USER)
                .from(CASE_REVIEW_FUNCTIONAL_CASE)
                .leftJoin(FUNCTIONAL_CASE).on(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID.eq(FUNCTIONAL_CASE.ID))
                .where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(reviewId)
                        .and(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.in(reviewIds))).list();
    }
}
