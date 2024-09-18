package cn.master.backend.service.impl;

import cn.master.backend.constants.*;
import cn.master.backend.entity.*;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.handler.provider.BaseCaseProvider;
import cn.master.backend.mapper.CaseReviewFunctionalCaseMapper;
import cn.master.backend.mapper.CaseReviewFunctionalCaseUserMapper;
import cn.master.backend.mapper.CaseReviewHistoryMapper;
import cn.master.backend.mapper.CaseReviewMapper;
import cn.master.backend.payload.dto.BaseTreeNode;
import cn.master.backend.payload.dto.functional.*;
import cn.master.backend.payload.dto.project.ModuleCountDTO;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.request.functional.*;
import cn.master.backend.service.*;
import cn.master.backend.util.LogUtils;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.CaseReviewFunctionalCaseTableDef.CASE_REVIEW_FUNCTIONAL_CASE;
import static cn.master.backend.entity.table.CaseReviewFunctionalCaseUserTableDef.CASE_REVIEW_FUNCTIONAL_CASE_USER;
import static cn.master.backend.entity.table.CaseReviewHistoryTableDef.CASE_REVIEW_HISTORY;
import static cn.master.backend.entity.table.CaseReviewTableDef.CASE_REVIEW;
import static cn.master.backend.entity.table.CaseReviewUserTableDef.CASE_REVIEW_USER;
import static cn.master.backend.entity.table.FunctionalCaseModuleTableDef.FUNCTIONAL_CASE_MODULE;
import static cn.master.backend.entity.table.FunctionalCaseTableDef.FUNCTIONAL_CASE;
import static cn.master.backend.entity.table.ProjectTableDef.PROJECT;
import static cn.master.backend.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static cn.master.backend.entity.table.UserTableDef.USER;

/**
 * 用例评审和功能用例的中间表 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-10
 */
@Service
@RequiredArgsConstructor
public class CaseReviewFunctionalCaseServiceImpl extends ServiceImpl<CaseReviewFunctionalCaseMapper, CaseReviewFunctionalCase> implements CaseReviewFunctionalCaseService {
    private final CaseReviewHistoryMapper caseReviewHistoryMapper;
    private final CaseReviewFunctionalCaseUserMapper caseReviewFunctionalCaseUserMapper;
    private final BaseCaseProvider provider;
    private final FunctionalCaseCustomFieldService functionalCaseCustomFieldService;
    private final BaseCustomFieldService baseCustomFieldService;
    private final BaseCustomFieldOptionService baseCustomFieldOptionService;
    private final FunctionalCaseModuleServiceImpl functionalCaseModuleService;
    private final CaseReviewMapper caseReviewMapper;
    private final CaseReviewFunctionalCaseMapper caseReviewFunctionalCaseMapper;
    private final FunctionalCaseAttachmentService functionalCaseAttachmentService;
    public static final int POS_STEP = 5000;
    private static final String CASE_MODULE_COUNT_ALL = "all";

    @Override
    public void addCaseReviewFunctionalCase(String caseId, String userId, String reviewId) {
        CaseReviewFunctionalCase reviewFunctionalCase = new CaseReviewFunctionalCase();
        reviewFunctionalCase.setCaseId(caseId);
        reviewFunctionalCase.setReviewId(reviewId);
        reviewFunctionalCase.setStatus(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
        reviewFunctionalCase.setCreateUser(userId);
        reviewFunctionalCase.setPos(getCaseFunctionalCaseNextPos(reviewId));
        mapper.insertSelective(reviewFunctionalCase);
        List<CaseReviewUser> caseReviewUsers = QueryChain.of(CaseReviewUser.class).where(CASE_REVIEW_USER.REVIEW_ID.eq(reviewId)).list();
        if (CollectionUtils.isNotEmpty(caseReviewUsers)) {
            List<CaseReviewFunctionalCaseUser> list = new ArrayList<>();
            caseReviewUsers.forEach(item -> {
                CaseReviewFunctionalCaseUser caseUser = new CaseReviewFunctionalCaseUser();
                caseUser.setCaseId(caseId);
                caseUser.setReviewId(reviewId);
                caseUser.setUserId(item.getUserId());
                list.add(caseUser);
            });
            caseReviewFunctionalCaseUserMapper.insertBatch(list);
            //更新评审的整体状态
            Map<String, Integer> countMap = new HashMap<>();
            countMap.put(reviewFunctionalCase.getStatus(), 1);
            Map<String, String> statusMap = new HashMap<>();
            statusMap.put(caseId, reviewFunctionalCase.getStatus());
            Map<String, Object> param = new HashMap<>();
            param.put(CaseEvent.Param.REVIEW_ID, reviewId);
            param.put(CaseEvent.Param.USER_ID, userId);
            param.put(CaseEvent.Param.CASE_IDS, List.of(caseId));
            param.put(CaseEvent.Param.COUNT_MAP, countMap);
            param.put(CaseEvent.Param.STATUS_MAP, statusMap);
            param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.REVIEW_FUNCTIONAL_CASE);
            provider.updateCaseReview(param);
        }
    }

    @Override
    public Long getCaseFunctionalCaseNextPos(String reviewId) {
        Long pos = queryChain().select(CASE_REVIEW_FUNCTIONAL_CASE.POS)
                .from(CASE_REVIEW_FUNCTIONAL_CASE)
                .where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(reviewId)).oneAs(Long.class);
        return (pos == null ? 0 : pos) + POS_STEP;
    }

    @Override
    public void reReviewedCase(FunctionalCaseEditRequest request, FunctionalCase functionalCase, String userId) {
        List<ProjectApplication> projectApplications = QueryChain.of(ProjectApplication.class)
                .where(ProjectApplication::getProjectId).eq(request.getProjectId())
                .and(ProjectApplication::getType).eq(ProjectApplicationType.CASE.CASE_RE_REVIEW.name()).list();
        if (CollectionUtils.isNotEmpty(projectApplications) && Boolean.parseBoolean(projectApplications.getFirst().getTypeValue())) {
            if (!StringUtils.equals(functionalCase.getName(), request.getName())
                    || !StringUtils.equals(new String(functionalCase.getSteps() == null ? new byte[0] : functionalCase.getSteps(), StandardCharsets.UTF_8), request.getSteps())
                    || !StringUtils.equals(new String(functionalCase.getTextDescription() == null ? new byte[0] : functionalCase.getTextDescription(), StandardCharsets.UTF_8), request.getTextDescription())
                    || !StringUtils.equals(new String(functionalCase.getExpectedResult() == null ? new byte[0] : functionalCase.getExpectedResult(), StandardCharsets.UTF_8), request.getExpectedResult())) {
                doHandleStatusAndHistory(functionalCase, userId);
            }
        }
    }

    @Override
    public List<String> doSelectIds(BaseReviewCaseBatchRequest request) {
        if (request.isSelectAll()) {
            List<String> ids = getIds(request);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    @Override
    public List<String> getCaseIdsByReviewId(String reviewId) {
        return queryChain().select(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID)
                .where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(reviewId))
                .listAs(String.class);
    }

    @Override
    public Page<ReviewFunctionalCaseDTO> page(ReviewFunctionalCasePageRequest request, boolean deleted, String viewStatusUserId) {
        Page<ReviewFunctionalCaseDTO> page = queryChain().select(CASE_REVIEW_FUNCTIONAL_CASE.ID, CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID,
                        CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID, CASE_REVIEW_FUNCTIONAL_CASE.STATUS,
                        CASE_REVIEW_FUNCTIONAL_CASE.CREATE_TIME, FUNCTIONAL_CASE.CREATE_USER, USER.NAME.as("createUserName"),
                        FUNCTIONAL_CASE.VERSION_ID, FUNCTIONAL_CASE.MODULE_ID, FUNCTIONAL_CASE.NAME,
                        FUNCTIONAL_CASE.NUM, FUNCTIONAL_CASE.CASE_EDIT_TYPE)
                .from(CASE_REVIEW_FUNCTIONAL_CASE)
                .leftJoin(FUNCTIONAL_CASE).on(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID.eq(FUNCTIONAL_CASE.ID))
                .leftJoin(USER).on(FUNCTIONAL_CASE.CREATE_USER.eq(USER.ID))
                .where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(request.getReviewId()))
                .and(FUNCTIONAL_CASE.MODULE_ID.in(request.getModuleIds()))
                .and(FUNCTIONAL_CASE.NAME.like(request.getKeyword())
                        .or(FUNCTIONAL_CASE.NUM.like(request.getKeyword()))
                        .or(FUNCTIONAL_CASE.TAGS.like(request.getKeyword())))
                .orderBy(CASE_REVIEW_FUNCTIONAL_CASE.POS.desc())
                .pageAs(Page.of(request.getCurrent(), request.getPageSize()), ReviewFunctionalCaseDTO.class);
        return doHandleDTO(page, request, viewStatusUserId);
    }

    @Override
    public List<BaseTreeNode> getTree(String reviewId) {
        List<BaseTreeNode> returnList = new ArrayList<>();
        List<ProjectOptionDTO> rootIds = selectFunRootIdByReviewId(reviewId);
        Map<String, List<ProjectOptionDTO>> projectRootMap = rootIds.stream().collect(Collectors.groupingBy(ProjectOptionDTO::getName));
        List<FunctionalCaseModuleDTO> functionalModuleIds = selectBaseByProjectIdAndReviewId(reviewId);
        Map<String, List<FunctionalCaseModuleDTO>> projectModuleMap = functionalModuleIds.stream().collect(Collectors.groupingBy(FunctionalCaseModule::getProjectId));
        if (MapUtils.isEmpty(projectModuleMap)) {
            projectRootMap.forEach((projectId, projectOptionDTOList) -> {
                BaseTreeNode projectNode = new BaseTreeNode(projectId, projectOptionDTOList.getFirst().getProjectName(), Project.class.getName());
                returnList.add(projectNode);
                BaseTreeNode defaultNode = functionalCaseModuleService.getDefaultModule(Translator.get("functional_case.module.default.name"));
                projectNode.addChild(defaultNode);
            });
            return returnList;
        }
        projectModuleMap.forEach((projectId, moduleList) -> {
            BaseTreeNode projectNode = new BaseTreeNode(projectId, moduleList.getFirst().getProjectName(), Project.class.getName());
            returnList.add(projectNode);
            List<String> projectModuleIds = moduleList.stream().map(FunctionalCaseModule::getId).toList();
            List<BaseTreeNode> nodeByNodeIds = functionalCaseModuleService.getNodeByNodeIds(projectModuleIds);
            boolean haveVirtualRootNode = CollectionUtils.isEmpty(projectRootMap.get(projectId));
            List<BaseTreeNode> baseTreeNodes = functionalCaseModuleService.buildTreeAndCountResource(nodeByNodeIds, !haveVirtualRootNode, Translator.get("functional_case.module.default.name"));
            for (BaseTreeNode baseTreeNode : baseTreeNodes) {
                projectNode.addChild(baseTreeNode);
            }
        });
        return returnList;
    }

    @Override
    public Map<String, Long> moduleCount(ReviewFunctionalCasePageRequest request, boolean b) {
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        request.setModuleIds(null);
        List<FunctionalCaseModuleCountDTO> projectModuleCountDTOList = countModuleIdByRequest(request);
        Map<String, List<FunctionalCaseModuleCountDTO>> projectCountMap = projectModuleCountDTOList.stream().collect(Collectors.groupingBy(FunctionalCaseModuleCountDTO::getProjectId));
        Map<String, Long> projectModuleCountMap = new HashMap<>();
        projectCountMap.forEach((projectId, moduleCountDTOList) -> {
            List<ModuleCountDTO> moduleCountDTOS = new ArrayList<>();
            for (FunctionalCaseModuleCountDTO functionalCaseModuleCountDTO : moduleCountDTOList) {
                ModuleCountDTO moduleCountDTO = new ModuleCountDTO();
                BeanUtils.copyProperties(functionalCaseModuleCountDTO, moduleCountDTO);
                moduleCountDTOS.add(moduleCountDTO);
            }
            int sum = moduleCountDTOList.stream().mapToInt(FunctionalCaseModuleCountDTO::getDataCount).sum();
            Map<String, Long> moduleCountMap = getModuleCountMap(projectId, request.getReviewId(), moduleCountDTOS);
            moduleCountMap.forEach((k, v) -> {
                if (projectModuleCountMap.get(k) == null || projectModuleCountMap.get(k) == 0L) {
                    projectModuleCountMap.put(k, v);
                }
            });
            projectModuleCountMap.put(projectId, (long) sum);
        });
        //查出全部用例数量
        long allCount = QueryChain.of(CaseReviewFunctionalCase.class)
                .select(QueryMethods.count(FUNCTIONAL_CASE.ID)).from(CASE_REVIEW_FUNCTIONAL_CASE)
                .leftJoin(FUNCTIONAL_CASE).on(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID.eq(FUNCTIONAL_CASE.ID))
                .where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(request.getReviewId()))
                .and(FUNCTIONAL_CASE.MODULE_ID.in(request.getModuleIds()))
                .and(FUNCTIONAL_CASE.NAME.like(request.getKeyword())
                        .or(FUNCTIONAL_CASE.NUM.like(request.getKeyword()))
                        .or(FUNCTIONAL_CASE.TAGS.like(request.getKeyword())))
                .oneAs(Long.class);
        projectModuleCountMap.put(CASE_MODULE_COUNT_ALL, allCount);
        return projectModuleCountMap;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void disassociate(BaseReviewCaseBatchRequest request, String userId) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            QueryChain<CaseReviewFunctionalCase> queryChain = queryChain().where(CASE_REVIEW_FUNCTIONAL_CASE.ID.in(ids));
            List<CaseReviewFunctionalCase> caseReviewFunctionalCases = queryChain.list();
            List<String> caseIds = caseReviewFunctionalCases.stream().map(CaseReviewFunctionalCase::getCaseId).distinct().toList();
            LogicDeleteManager.execWithoutLogicDelete(() -> mapper.deleteByQuery(queryChain));
            Map<String, Object> param = new HashMap<>();
            param.put(CaseEvent.Param.REVIEW_ID, request.getReviewId());
            param.put(CaseEvent.Param.CASE_IDS, caseIds);
            param.put(CaseEvent.Param.USER_ID, userId);
            param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.DISASSOCIATE);
            provider.updateCaseReview(param);
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void editPos(CaseReviewFunctionalCasePosRequest request) {
        // todo
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void batchReview(BatchReviewFunctionalCaseRequest request, String userId) {
        String reviewId = request.getReviewId();
        CaseReview caseReview = caseReviewMapper.selectOneById(reviewId);
        request.setReviewPassRule(caseReview.getReviewPassRule());
        List<CaseReviewFunctionalCase> caseReviewFunctionalCaseList = doCaseReviewFunctionalCases(request);
        if (CollectionUtils.isEmpty(caseReviewFunctionalCaseList)) {
            return;
        }
        List<String> caseIds = caseReviewFunctionalCaseList.stream().map(CaseReviewFunctionalCase::getCaseId).toList();
        List<CaseReviewHistory> caseReviewHistories = QueryChain.of(CaseReviewHistory.class).where(CASE_REVIEW_HISTORY.CASE_ID.in(caseIds)
                .and(CASE_REVIEW_HISTORY.REVIEW_ID.eq(reviewId))
                .and(CASE_REVIEW_HISTORY.ABANDONED.eq(false))).list();
        Map<String, List<CaseReviewHistory>> caseHistoryMap = caseReviewHistories.stream()
                .collect(Collectors.groupingBy(CaseReviewHistory::getCaseId, Collectors.toList()));

        List<CaseReviewFunctionalCaseUser> caseReviewFunctionalCaseUsers = QueryChain.of(CaseReviewFunctionalCaseUser.class)
                .where(CASE_REVIEW_FUNCTIONAL_CASE_USER.CASE_ID.in(caseIds)
                        .and(CASE_REVIEW_FUNCTIONAL_CASE_USER.REVIEW_ID.eq(reviewId))).list();
        Map<String, List<CaseReviewFunctionalCaseUser>> reviewerMap = caseReviewFunctionalCaseUsers.stream()
                .collect(Collectors.groupingBy(CaseReviewFunctionalCaseUser::getCaseId, Collectors.toList()));

        List<FunctionalCase> functionalCases = QueryChain.of(FunctionalCase.class).where(FUNCTIONAL_CASE.ID.in(caseIds)).list();
        Map<String, String> caseProjectIdMap = functionalCases.stream().collect(Collectors.toMap(FunctionalCase::getId, FunctionalCase::getProjectId));

        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.ROLE_ID.eq(InternalUserRole.ADMIN.getValue())
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(UserRoleScope.SYSTEM))
                .and(USER_ROLE_RELATION.ORGANIZATION_ID.eq(UserRoleScope.SYSTEM))).list();
        List<String> systemUsers = userRoleRelations.stream().map(UserRoleRelation::getUserId).distinct().toList();

        Map<String, String> statusMap = new HashMap<>();
        //重新提审，作废之前的记录
        if (StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.RE_REVIEWED.toString())) {
            UpdateChain.of(CaseReviewHistory.class).set(CASE_REVIEW_HISTORY.ABANDONED, true)
                    .where(CASE_REVIEW_HISTORY.REVIEW_ID.eq(reviewId)
                            .and(CASE_REVIEW_HISTORY.ABANDONED.eq(false))
                            .and(CASE_REVIEW_HISTORY.CASE_ID.in(caseIds))).update();
        }
        for (CaseReviewFunctionalCase caseReviewFunctionalCase : caseReviewFunctionalCaseList) {
            //校验当前操作人是否是该用例的评审人或者是系统管理员，是增加评审历史，不是过滤掉
            String caseId = caseReviewFunctionalCase.getCaseId();
            List<CaseReviewFunctionalCaseUser> userList = reviewerMap.get(caseId);
            if (!systemUsers.contains(userId) && (CollectionUtils.isEmpty(userList) || CollectionUtils.isEmpty(userList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getUserId(), userId)).toList()))) {
                LogUtils.error(caseId + ": no review user, please check");
                continue;
            }
            boolean isAdmin = systemUsers.contains(userId) && (CollectionUtils.isEmpty(userList) || CollectionUtils.isEmpty(userList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getUserId(), userId)).toList()));
            CaseReviewHistory caseReviewHistory = buildCaseReviewHistory(request, userId, caseId);
            caseReviewHistoryMapper.insert(caseReviewHistory);
            if (caseHistoryMap.get(caseId) == null) {
                List<CaseReviewHistory> histories = new ArrayList<>();
                histories.add(caseReviewHistory);
                caseHistoryMap.put(caseId, histories);
            } else {
                caseHistoryMap.get(caseId).add(caseReviewHistory);
            }
            //根据评审规则更新用例评审和功能用例关系表中的状态 1.单人评审直接更新评审结果 2.多人评审需要计算 3.如果是重新评审，直接全部变成重新评审
            setStatus(request, caseReviewFunctionalCase, caseHistoryMap, reviewerMap, isAdmin);
            statusMap.put(caseReviewFunctionalCase.getCaseId(), caseReviewFunctionalCase.getStatus());
            caseReviewFunctionalCaseMapper.update(caseReviewFunctionalCase);

            // todo 检查是否有@，发送@通知
            if (StringUtils.isNotBlank(request.getNotifier())) {
                List<String> relatedUsers = Arrays.asList(request.getNotifier().split(";"));
                //reviewSendNoticeService.sendNoticeCase(relatedUsers, userId, caseId, NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.REVIEW_AT, reviewId);
            }
            //发送评审通过不通过通知（评审中不发）
            if (StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.UN_PASS.toString())) {
                //reviewSendNoticeService.sendNoticeCase(new ArrayList<>(), userId, caseId, NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.REVIEW_FAIL, reviewId);
            }
            if (StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.PASS.toString())) {
                //reviewSendNoticeService.sendNoticeCase(new ArrayList<>(), userId, caseId, NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, NoticeConstants.Event.REVIEW_PASSED, reviewId);
            }
            functionalCaseAttachmentService.uploadMinioFile(caseId, caseProjectIdMap.get(caseId), request.getReviewCommentFileIds(), userId, CaseFileSourceType.REVIEW_COMMENT.toString());
        }
        Map<String, Object> param = new HashMap<>();
        Map<String, List<CaseReviewFunctionalCase>> collect = caseReviewFunctionalCaseList.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getStatus));
        Map<String, Integer> countMap = new HashMap<>();
        collect.forEach((k, v) -> {
            countMap.put(k, v.size());
        });
        param.put(CaseEvent.Param.CASE_IDS, CollectionUtils.isNotEmpty(caseIds) ? caseIds : new ArrayList<>());
        param.put(CaseEvent.Param.REVIEW_ID, reviewId);
        param.put(CaseEvent.Param.STATUS_MAP, statusMap);
        param.put(CaseEvent.Param.USER_ID, userId);
        param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.REVIEW_FUNCTIONAL_CASE);
        param.put(CaseEvent.Param.COUNT_MAP, countMap);
        provider.updateCaseReview(param);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void batchEditReviewUser(BatchEditReviewerRequest request, String userId) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            String reviewPassRule = QueryChain.of(CaseReview.class).select(CASE_REVIEW.REVIEW_PASS_RULE)
                    .from(CASE_REVIEW).where(CASE_REVIEW.ID.eq(request.getReviewId())).oneAs(String.class);
            //评审人处理
            List<CaseReviewFunctionalCase> cases = QueryChain.of(caseReviewFunctionalCaseMapper)
                    .where(CASE_REVIEW_FUNCTIONAL_CASE.ID.in(ids)).list();
            handleReviewers(request, cases, reviewPassRule, userId);
        }
    }

    private void handleReviewers(BatchEditReviewerRequest request, List<CaseReviewFunctionalCase> cases, String reviewPassRule, String userId) {
        List<String> caseIds = cases.stream().map(CaseReviewFunctionalCase::getCaseId).toList();
        QueryChain<CaseReviewFunctionalCaseUser> queryChain = QueryChain.of(CaseReviewFunctionalCaseUser.class)
                .where(CASE_REVIEW_FUNCTIONAL_CASE_USER.CASE_ID.in(caseIds)
                        .and(CASE_REVIEW_FUNCTIONAL_CASE_USER.REVIEW_ID.eq(request.getReviewerId())));
        List<CaseReviewFunctionalCaseUser> oldReviewUsers = queryChain.list();
        Map<String, List<CaseReviewFunctionalCaseUser>> oldReviewUserMap = oldReviewUsers.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCaseUser::getCaseId));
        //处理评审人数据
        handleReviewCaseUsers(request, caseIds, oldReviewUserMap);
        if (CaseReviewPassRule.MULTIPLE.name().equals(reviewPassRule)) {
            //如果是多人评审 需要重新评估用例评审状态
            List<CaseReviewFunctionalCaseUser> newReviewers = queryChain.list();
            Map<String, List<CaseReviewFunctionalCaseUser>> newReviewersMap = newReviewers.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCaseUser::getCaseId));
            List<CaseReviewHistory> caseReviewHistories = QueryChain.of(caseReviewHistoryMapper)
                    .where(CASE_REVIEW_HISTORY.REVIEW_ID.eq(request.getReviewId()).and(CASE_REVIEW_HISTORY.CASE_ID.in(caseIds)))
                    .and(CASE_REVIEW_HISTORY.STATUS.ne("UNDER_REVIEWED").and(CASE_REVIEW_HISTORY.ABANDONED.eq(false)))
                    .orderBy(CASE_REVIEW_HISTORY.CREATE_TIME.desc())
                    .list();
            Map<String, List<CaseReviewHistory>> caseHistoryMap = caseReviewHistories.stream().collect(Collectors.groupingBy(CaseReviewHistory::getCaseId, Collectors.toList()));

            Map<String, String> statusMap = new HashMap<>();

            cases.forEach(caseReview -> {
                String status = multipleReview(caseHistoryMap.get(caseReview.getCaseId()), newReviewersMap.get(caseReview.getCaseId()));
                caseReview.setStatus(status);
                caseReviewFunctionalCaseMapper.update(caseReview);
                statusMap.put(caseReview.getCaseId(), caseReview.getStatus());
            });
            Map<String, List<CaseReviewFunctionalCase>> collect = cases.stream().collect(Collectors.groupingBy(CaseReviewFunctionalCase::getStatus));
            Map<String, Integer> countMap = new HashMap<>();
            collect.forEach((k, v) -> {
                countMap.put(k, v.size());
            });
            Map<String, Object> param = new HashMap<>();
            param.put(CaseEvent.Param.REVIEW_ID, request.getReviewId());
            param.put(CaseEvent.Param.USER_ID, userId);
            param.put(CaseEvent.Param.CASE_IDS, caseIds);
            param.put(CaseEvent.Param.COUNT_MAP, countMap);
            param.put(CaseEvent.Param.STATUS_MAP, statusMap);
            param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.REVIEW_FUNCTIONAL_CASE);
            provider.updateCaseReview(param);
        }
    }

    private String multipleReview(List<CaseReviewHistory> reviewHistories, List<CaseReviewFunctionalCaseUser> newReviewers) {
        if (CollectionUtils.isNotEmpty(reviewHistories)) {
            //历史的评审人
            List<String> historyUsers = reviewHistories.stream().map(CaseReviewHistory::getCreateUser).collect(Collectors.toList());
            //最新的评审人
            List<String> newUsers = newReviewers.stream().map(CaseReviewFunctionalCaseUser::getUserId).collect(Collectors.toList());
            return newReviewStatus(historyUsers, newUsers, reviewHistories, newReviewers);
        } else {
            return FunctionalCaseReviewStatus.UN_REVIEWED.name();
        }
    }

    private String newReviewStatus(List<String> historyUsers, List<String> newUsers, List<CaseReviewHistory> reviewHistories, List<CaseReviewFunctionalCaseUser> newReviewers) {
        CaseReviewHistory caseReviewHistory = reviewHistories.getFirst();
        if (newUsers.contains(caseReviewHistory.getCreateUser()) && FunctionalCaseReviewStatus.RE_REVIEWED.name().equals(caseReviewHistory.getStatus())) {
            return FunctionalCaseReviewStatus.RE_REVIEWED.name();
        }
        if (new HashSet<>(historyUsers).containsAll(newUsers)) {
            //新的评审人都存在过评审记录
            return getReviewStatus(newUsers, reviewHistories, FunctionalCaseReviewStatus.PASS.name());
        } else {
            //新的评审人有评审历史中不存在的用户
            newUsers.retainAll(historyUsers);
            return getReviewStatus(newUsers, reviewHistories, FunctionalCaseReviewStatus.UNDER_REVIEWED.name());
        }
    }

    @Override
    public ReviewerAndStatusDTO getUserAndStatus(String reviewId, String caseId) {
        ReviewerAndStatusDTO reviewerAndStatusDTO = new ReviewerAndStatusDTO();
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = QueryChain.of(caseReviewFunctionalCaseMapper)
                .where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(reviewId)
                        .and(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID.eq(caseId))).list();
        reviewerAndStatusDTO.setCaseId(caseId);
        reviewerAndStatusDTO.setStatus(caseReviewFunctionalCases.get(0).getStatus());
        List<OptionDTO> userStatus = getUserStatus(reviewId, caseId);
        reviewerAndStatusDTO.setReviewerStatus(userStatus);
        return reviewerAndStatusDTO;
    }

    @Override
    public List<OptionDTO> getUserStatus(String reviewId, String caseId) {
        List<CaseReviewHistoryDTO> list = resultList(caseId, reviewId);
        Map<String, List<CaseReviewHistoryDTO>> collect = list.stream()
                .sorted(Comparator.comparing(CaseReviewHistoryDTO::getCreateTime).reversed())
                .collect(Collectors.groupingBy(CaseReviewHistoryDTO::getCreateUser, Collectors.toList()));
        List<OptionDTO> optionDTOS = new ArrayList<>();
        List<CaseReviewFunctionalCaseUser> reviewerList = getReviewerList(reviewId, caseId);
        List<String> reviewerIds = reviewerList.stream().map(CaseReviewFunctionalCaseUser::getUserId).filter(t -> !collect.containsKey(t)).collect(Collectors.toList());
        List<User> users = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(reviewerIds)) {
            users = QueryChain.of(User.class).where(USER.ID.in(reviewerIds)).list();
        }
        AtomicBoolean hasReReview = new AtomicBoolean(false);
        final LocalDateTime[] createTime = new LocalDateTime[2];
        final LocalDateTime[] reReviewTime = new LocalDateTime[2];
        collect.forEach((k, v) -> {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(v.getFirst().getUserName());
            optionDTO.setName(v.getFirst().getStatus());
            if (createTime[0].isBefore(v.getFirst().getCreateTime())) {
                createTime[0] = v.getFirst().getCreateTime();
            }
            if (StringUtils.equalsIgnoreCase(v.getFirst().getStatus(), FunctionalCaseReviewStatus.RE_REVIEWED.toString())) {
                reReviewTime[0] = v.getFirst().getCreateTime();
                hasReReview.set(true);
            }
            optionDTOS.add(optionDTO);
        });
        if (CollectionUtils.isNotEmpty(users)) {
            users.forEach(t -> {
                OptionDTO optionDTO = new OptionDTO();
                optionDTO.setId(t.getName());
                optionDTO.setName(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
                optionDTOS.add(optionDTO);
            });
        }
        if (hasReReview.get() && (reReviewTime[0].isAfter(createTime[0]) || reReviewTime[0].equals(createTime[0]))) {
            for (OptionDTO optionDTO : optionDTOS) {
                optionDTO.setName(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
            }
        }
        return optionDTOS;
    }

    @Override
    public List<CaseReviewFunctionalCaseUser> getReviewerList(String reviewId, String caseId) {
        return QueryChain.of(caseReviewFunctionalCaseUserMapper)
                .where(CASE_REVIEW_FUNCTIONAL_CASE_USER.CASE_ID.eq(caseId)
                        .and(CASE_REVIEW_FUNCTIONAL_CASE_USER.REVIEW_ID.eq(reviewId)))
                .list();
    }

    private List<CaseReviewHistoryDTO> resultList(String caseId, String reviewId) {
        return QueryChain.of(CaseReviewHistory.class)
                .where(CASE_REVIEW_HISTORY.CASE_ID.eq(caseId).and(CASE_REVIEW_HISTORY.REVIEW_ID.eq(reviewId))
                        .and(CASE_REVIEW_HISTORY.STATUS.ne("UNDER_REVIEWED")))
                .listAs(CaseReviewHistoryDTO.class);
    }

    private String getReviewStatus(List<String> newUsers, List<CaseReviewHistory> reviewHistories, String reviewStatus) {
        List<String> statusList = new ArrayList<>();
        //拿到这个人评审的最后一条状态
        newUsers.forEach(item -> {
            String status = reviewHistories.stream().filter(history -> StringUtils.equalsIgnoreCase(item, history.getCreateUser())).findFirst().get().getStatus();
            statusList.add(status);
        });
        if (CollectionUtils.isEmpty(statusList)) {
            return FunctionalCaseReviewStatus.UN_REVIEWED.name();
        }
        if (statusList.stream().anyMatch(item -> FunctionalCaseReviewStatus.UN_PASS.name().equals(item))) {
            return FunctionalCaseReviewStatus.UN_PASS.name();
        }
        return reviewStatus;
    }

    private void handleReviewCaseUsers(BatchEditReviewerRequest request, List<String> caseIds, Map<String, List<CaseReviewFunctionalCaseUser>> listMap) {
        if (request.isAppend()) {
            //追加评审人
            List<CaseReviewFunctionalCaseUser> list = new ArrayList<>();
            caseIds.forEach(caseId -> {
                //原评审人
                List<CaseReviewFunctionalCaseUser> users = listMap.get(caseId);

                //新评审人
                List<String> reviewerIds = request.getReviewerId();
                if (CollectionUtils.isNotEmpty(users)) {
                    List<String> userIds = users.stream().map(CaseReviewFunctionalCaseUser::getUserId).toList();
                    reviewerIds.removeAll(userIds);
                }
                reviewerIds.forEach(reviewer -> {
                    CaseReviewFunctionalCaseUser caseUser = new CaseReviewFunctionalCaseUser();
                    caseUser.setReviewId(request.getReviewId());
                    caseUser.setCaseId(caseId);
                    caseUser.setUserId(reviewer);
                    list.add(caseUser);
                });
            });
            if (CollectionUtils.isNotEmpty(list)) {
                caseReviewFunctionalCaseUserMapper.insertBatch(list);
            }
        } else {
            //更新评审人
            //extCaseReviewFunctionalCaseUserMapper.deleteByCaseIds(caseIds, request.getReviewId());
            QueryChain<CaseReviewFunctionalCaseUser> queryChain = QueryChain.of(caseReviewFunctionalCaseUserMapper).where(CASE_REVIEW_FUNCTIONAL_CASE_USER.CASE_ID.in(caseIds)
                    .and(CASE_REVIEW_FUNCTIONAL_CASE_USER.REVIEW_ID.eq(request.getReviewId())));
            LogicDeleteManager.execWithoutLogicDelete(() -> caseReviewFunctionalCaseUserMapper.deleteByQuery(queryChain));
            List<String> reviewerIds = request.getReviewerId();
            List<CaseReviewFunctionalCaseUser> list = new ArrayList<>();
            caseIds.forEach(caseId -> {
                reviewerIds.forEach(reviewer -> {
                    CaseReviewFunctionalCaseUser caseUser = new CaseReviewFunctionalCaseUser();
                    caseUser.setReviewId(request.getReviewId());
                    caseUser.setCaseId(caseId);
                    caseUser.setUserId(reviewer);
                    list.add(caseUser);
                });
            });
            caseReviewFunctionalCaseUserMapper.insertBatch(list);
        }
    }

    private void setStatus(BatchReviewFunctionalCaseRequest request, CaseReviewFunctionalCase caseReviewFunctionalCase, Map<String, List<CaseReviewHistory>> caseHistoryMap, Map<String, List<CaseReviewFunctionalCaseUser>> reviewerMap, boolean isAdmin) {
        if (StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.RE_REVIEWED.toString())) {
            caseReviewFunctionalCase.setStatus(request.getStatus());
            return;
        }
        if (StringUtils.equals(request.getReviewPassRule(), CaseReviewPassRule.SINGLE.toString())) {
            if (!StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.UNDER_REVIEWED.toString()) && !isAdmin) {
                caseReviewFunctionalCase.setStatus(request.getStatus());
            }
        } else {
            if (isAdmin) {
                return;
            }
            //根据用例ID 查询所有评审人 再查所有评审人最后一次的评审结果（只有通过/不通过算结果）
            List<CaseReviewHistory> caseReviewHistoriesExp = caseHistoryMap.get(caseReviewFunctionalCase.getCaseId());
            Map<String, List<CaseReviewHistory>> hasReviewedUserMap = caseReviewHistoriesExp.stream().collect(Collectors.groupingBy(CaseReviewHistory::getCreateUser, Collectors.toList()));
            List<CaseReviewFunctionalCaseUser> caseReviewFunctionalCaseUsersExp = reviewerMap.get(caseReviewFunctionalCase.getCaseId());
            AtomicInteger passCount = new AtomicInteger();
            AtomicInteger unPassCount = new AtomicInteger();
            hasReviewedUserMap.forEach((k, v) -> {
                //过滤掉每个人的评审中状态，每个人的评审中为建议，建议不做评审结果，这里排除
                List<CaseReviewHistory> list = v.stream().filter(t -> !StringUtils.equalsIgnoreCase(t.getStatus(), FunctionalCaseReviewStatus.UNDER_REVIEWED.toString())).sorted(Comparator.comparing(CaseReviewHistory::getCreateTime).reversed()).toList();
                if (CollectionUtils.isNotEmpty(list) && StringUtils.equalsIgnoreCase(list.getFirst().getStatus(), FunctionalCaseReviewStatus.PASS.toString())) {
                    passCount.set(passCount.get() + 1);
                }
                if (CollectionUtils.isNotEmpty(list) && StringUtils.equalsIgnoreCase(list.getFirst().getStatus(), FunctionalCaseReviewStatus.UN_PASS.toString())) {
                    unPassCount.set(unPassCount.get() + 1);
                }
            });
            //检查是否全部是通过，全是才是PASS,否则是评审中(如果时自动重新提审，会有个system用户，这里需要排出一下)
            if (hasReviewedUserMap.get(UserRoleScope.SYSTEM) != null) {
                hasReviewedUserMap.remove(UserRoleScope.SYSTEM);
            }
            if (unPassCount.get() > 0) {
                caseReviewFunctionalCase.setStatus(FunctionalCaseReviewStatus.UN_PASS.toString());
            } else if (caseReviewFunctionalCaseUsersExp != null && (caseReviewFunctionalCaseUsersExp.size() > passCount.get()) && passCount.get() > 0) {
                //通过> 0 但不是全部通过 为评审中
                caseReviewFunctionalCase.setStatus(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
            } else if (caseReviewFunctionalCaseUsersExp != null && passCount.get() == caseReviewFunctionalCaseUsersExp.size()) {
                //检查是否全部是通过，全是才是PASS
                caseReviewFunctionalCase.setStatus(FunctionalCaseReviewStatus.PASS.toString());
            } else {
                caseReviewFunctionalCase.setStatus(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
            }
        }
    }

    private CaseReviewHistory buildCaseReviewHistory(BatchReviewFunctionalCaseRequest request, String userId, String caseId) {
        CaseReviewHistory caseReviewHistory = new CaseReviewHistory();
        caseReviewHistory.setReviewId(request.getReviewId());
        caseReviewHistory.setCaseId(caseId);
        caseReviewHistory.setStatus(request.getStatus());
        caseReviewHistory.setDeleted(false);
        caseReviewHistory.setAbandoned(false);
        if (StringUtils.equalsIgnoreCase(request.getStatus(), FunctionalCaseReviewStatus.UN_PASS.toString())) {
            if (StringUtils.isBlank(request.getContent())) {
                throw new MSException(Translator.get("case_review_content.not.exist"));
            } else {
                caseReviewHistory.setContent(request.getContent().getBytes());
            }
        } else {
            if (StringUtils.isNotBlank(request.getContent())) {
                caseReviewHistory.setContent(request.getContent().getBytes());
            }
        }
        caseReviewHistory.setNotifier(request.getNotifier());
        caseReviewHistory.setCreateUser(userId);
        return caseReviewHistory;
    }

    private List<CaseReviewFunctionalCase> doCaseReviewFunctionalCases(BaseReviewCaseBatchRequest request) {
        if (request.isSelectAll()) {
            return queryChain().select(CASE_REVIEW_FUNCTIONAL_CASE.ALL_COLUMNS)
                    .from(CASE_REVIEW_FUNCTIONAL_CASE)
                    .leftJoin(FUNCTIONAL_CASE).on(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID.eq(FUNCTIONAL_CASE.ID))
                    .where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(request.getReviewId()))
                    .and(FUNCTIONAL_CASE.MODULE_ID.in(request.getModuleIds()))
                    .and(FUNCTIONAL_CASE.NAME.like(request.getCondition().getKeyword())
                            .or(FUNCTIONAL_CASE.NUM.like(request.getCondition().getKeyword()))
                            .or(FUNCTIONAL_CASE.TAGS.like(request.getCondition().getKeyword())))
                    .and(CASE_REVIEW_FUNCTIONAL_CASE.ID.notIn(request.getExcludeIds()))
                    .list();
        } else {
            return caseReviewFunctionalCaseMapper.selectListByIds(request.getSelectIds());
        }
    }

    private Map<String, Long> getModuleCountMap(String projectId, String reviewId, List<ModuleCountDTO> moduleCountDTOList) {
        //构建模块树，并计算每个节点下的所有数量（包含子节点）
        List<BaseTreeNode> treeNodeList = getTreeOnlyIdsAndResourceCount(projectId, reviewId, moduleCountDTOList);

        //通过广度遍历的方式构建返回值
        return functionalCaseModuleService.getIdCountMapByBreadth(treeNodeList);
    }

    private List<BaseTreeNode> getTreeOnlyIdsAndResourceCount(String projectId, String reviewId, List<ModuleCountDTO> moduleCountDTOList) {
        //节点内容只有Id和parentId
        List<String> moduleIds = selectIdByProjectIdAndReviewId(projectId, reviewId);
        List<BaseTreeNode> nodeByNodeIds = functionalCaseModuleService.getNodeByNodeIds(moduleIds);
        return functionalCaseModuleService.buildTreeAndCountResource(nodeByNodeIds, moduleCountDTOList, true, Translator.get("functional_case.module.default.name"));
    }

    private List<String> selectIdByProjectIdAndReviewId(String projectId, String reviewId) {
        return QueryChain.of(FunctionalCaseModule.class)
                .select(FUNCTIONAL_CASE_MODULE.ID, FUNCTIONAL_CASE_MODULE.PROJECT_ID).from(FUNCTIONAL_CASE_MODULE)
                .where(FUNCTIONAL_CASE_MODULE.ID.in(
                        QueryChain.of(FunctionalCase.class).select(FUNCTIONAL_CASE.MODULE_ID).from(FUNCTIONAL_CASE)
                                .leftJoin(CASE_REVIEW_FUNCTIONAL_CASE).on(FUNCTIONAL_CASE.ID.eq(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID))
                                .where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(reviewId))
                                .listAs(String.class)
                ))
                .listAs(String.class);
    }

    private List<FunctionalCaseModuleCountDTO> countModuleIdByRequest(ReviewFunctionalCasePageRequest request) {
        return QueryChain.of(CaseReviewFunctionalCase.class)
                .select(FUNCTIONAL_CASE.MODULE_ID)
                .select(QueryMethods.count(FUNCTIONAL_CASE.ID).as("dataCount"))
                .select(FUNCTIONAL_CASE.PROJECT_ID, PROJECT.NAME.as("projectName"))
                .from(CASE_REVIEW_FUNCTIONAL_CASE)
                .leftJoin(FUNCTIONAL_CASE).on(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID.eq(FUNCTIONAL_CASE.ID))
                .leftJoin(PROJECT).on(FUNCTIONAL_CASE.PROJECT_ID.eq(PROJECT.ID))
                .where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(request.getReviewId()))
                .and(FUNCTIONAL_CASE.MODULE_ID.in(request.getModuleIds()))
                .and(FUNCTIONAL_CASE.NAME.like(request.getKeyword())
                        .or(FUNCTIONAL_CASE.NUM.like(request.getKeyword()))
                        .or(FUNCTIONAL_CASE.TAGS.like(request.getKeyword())))
                .groupBy(FUNCTIONAL_CASE.MODULE_ID)
                .listAs(FunctionalCaseModuleCountDTO.class);
    }

    private List<FunctionalCaseModuleDTO> selectBaseByProjectIdAndReviewId(String reviewId) {
        return QueryChain.of(FunctionalCaseModule.class)
                .select(FUNCTIONAL_CASE_MODULE.ID, FUNCTIONAL_CASE_MODULE.NAME.as("projectName"), FUNCTIONAL_CASE_MODULE.PROJECT_ID)
                .leftJoin(PROJECT).on(FUNCTIONAL_CASE_MODULE.PROJECT_ID.eq(PROJECT.ID))
                .where(FUNCTIONAL_CASE_MODULE.ID.in(
                        QueryChain.of(FunctionalCase.class).select(FUNCTIONAL_CASE.MODULE_ID)
                                .from(FUNCTIONAL_CASE)
                                .leftJoin(CASE_REVIEW_FUNCTIONAL_CASE).on(FUNCTIONAL_CASE.ID.eq(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID))
                                .where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(reviewId))
                                .listAs(String.class)
                ))
                .orderBy(FUNCTIONAL_CASE_MODULE.POS.desc())
                .listAs(FunctionalCaseModuleDTO.class);
    }

    private List<ProjectOptionDTO> selectFunRootIdByReviewId(String reviewId) {
        return QueryChain.of(FunctionalCase.class)
                .select(FUNCTIONAL_CASE.MODULE_ID.as("id"), FUNCTIONAL_CASE.PROJECT_ID.as("name"), PROJECT.NAME.as("projectName"))
                .from(FUNCTIONAL_CASE)
                .leftJoin(CASE_REVIEW_FUNCTIONAL_CASE).on(FUNCTIONAL_CASE.ID.eq(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID))
                .leftJoin(PROJECT).on(FUNCTIONAL_CASE.PROJECT_ID.eq(PROJECT.ID))
                .where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(reviewId).and(FUNCTIONAL_CASE.MODULE_ID.eq("root")))
                .orderBy(FUNCTIONAL_CASE.POS.desc())
                .listAs(ProjectOptionDTO.class);
    }

    private Page<ReviewFunctionalCaseDTO> doHandleDTO(Page<ReviewFunctionalCaseDTO> page, ReviewFunctionalCasePageRequest request, String viewStatusUserId) {
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            List<ReviewFunctionalCaseDTO> records = page.getRecords();
            List<String> moduleIds = records.stream().map(ReviewFunctionalCaseDTO::getModuleId).toList();
            List<BaseTreeNode> modules = QueryChain.of(FunctionalCaseModule.class)
                    .select(FUNCTIONAL_CASE_MODULE.ID, FUNCTIONAL_CASE_MODULE.NAME, FUNCTIONAL_CASE_MODULE.PARENT_ID)
                    .select("'module' AS type").from(FUNCTIONAL_CASE_MODULE)
                    .where(FunctionalCaseModule::getId).in(moduleIds).listAs(BaseTreeNode.class);
            Map<String, String> moduleMap = modules.stream().collect(Collectors.toMap(BaseTreeNode::getId, BaseTreeNode::getName));

            List<String> versionIds = records.stream().map(ReviewFunctionalCaseDTO::getVersionId).toList();
            List<ProjectVersion> versions = QueryChain.of(ProjectVersion.class).where(ProjectVersion::getId).in(versionIds).list();
            Map<String, String> versionMap = versions.stream().collect(Collectors.toMap(ProjectVersion::getId, ProjectVersion::getName));
            List<String> caseIds = records.stream().map(ReviewFunctionalCaseDTO::getCaseId).toList();
            List<ReviewsDTO> reviewers = selectReviewers(caseIds, request.getReviewId());
            Map<String, String> userIdMap = reviewers.stream().collect(Collectors.toMap(ReviewsDTO::getCaseId, ReviewsDTO::getUserIds));
            Map<String, String> userNameMap = reviewers.stream().collect(Collectors.toMap(ReviewsDTO::getCaseId, ReviewsDTO::getUserNames));
            LinkedHashMap<String, List<CaseReviewHistory>> caseStatusMap;
            if (request.isViewStatusFlag()) {
                List<CaseReviewHistory> histories = getReviewHistoryStatus(caseIds, request.getReviewId());
                caseStatusMap = histories.stream().collect(Collectors.groupingBy(CaseReviewHistory::getCaseId, LinkedHashMap::new, Collectors.toList()));
            } else {
                caseStatusMap = new LinkedHashMap<>();
            }
            Map<String, List<FunctionalCaseCustomFieldDTO>> collect = getCaseCustomFiledMap(caseIds);
            records.forEach(item -> {
                item.setModuleName(moduleMap.get(item.getModuleId()));
                item.setVersionName(versionMap.get(item.getVersionId()));
                String reviewer = userIdMap.get(item.getCaseId());
                if (StringUtils.isNotBlank(reviewer)) {
                    item.setReviewers(new ArrayList<>(Arrays.asList(reviewer.split(","))));
                }
                if (StringUtils.isNotBlank(reviewer)) {
                    item.setReviewers(new ArrayList<>(Arrays.asList(reviewer.split(","))));
                } else {
                    item.setReviewers(new ArrayList<>());
                }
                String reviewName = userNameMap.get(item.getCaseId());
                if (StringUtils.isNotBlank(reviewName)) {
                    item.setReviewNames(new ArrayList<>(Arrays.asList(reviewName.split(","))));
                } else {
                    item.setReviewNames(new ArrayList<>());
                }
                item.setCustomFields(collect.get(item.getCaseId()));
                if (request.isViewStatusFlag()) {
                    List<CaseReviewHistory> histories = caseStatusMap.get(item.getCaseId());
                    if (CollectionUtils.isNotEmpty(histories)) {
                        item.setMyStatus(getMyStatus(histories, viewStatusUserId));
                    } else {
                        //不存在评审历史
                        item.setMyStatus(FunctionalCaseReviewStatus.UN_REVIEWED.name());
                    }
                }
            });
        }
        return page;
    }

    private String getMyStatus(List<CaseReviewHistory> histories, String viewStatusUserId) {
        List<CaseReviewHistory> list = histories.stream().filter(history -> StringUtils.equalsIgnoreCase(history.getCreateUser(), viewStatusUserId)).toList();
        if (CollectionUtils.isNotEmpty(list)) {
            return list.getFirst().getStatus();
        }

        //重新提审记录
        List<CaseReviewHistory> reReviewed = histories.stream()
                .filter(history -> StringUtils.equalsIgnoreCase(history.getStatus(), FunctionalCaseReviewStatus.RE_REVIEWED.name())).toList();
        if (CollectionUtils.isNotEmpty(reReviewed)) {
            return FunctionalCaseReviewStatus.RE_REVIEWED.name();
        }
        return FunctionalCaseReviewStatus.UN_REVIEWED.name();
    }

    private Map<String, List<FunctionalCaseCustomFieldDTO>> getCaseCustomFiledMap(List<String> ids) {
        List<FunctionalCaseCustomFieldDTO> customFields = functionalCaseCustomFieldService.getCustomFieldsByCaseIds(ids);
        customFields.forEach(customField -> {
            if (customField.getInternal()) {
                customField.setFieldName(baseCustomFieldService.translateInternalField(customField.getFieldName()));
            }
        });
        List<String> fieldIds = customFields.stream().map(FunctionalCaseCustomFieldDTO::getFieldId).toList();
        List<CustomFieldOption> fieldOptions = baseCustomFieldOptionService.getByFieldIds(fieldIds);
        Map<String, List<CustomFieldOption>> customOptions = fieldOptions.stream().collect(Collectors.groupingBy(CustomFieldOption::getFieldId));
        customFields.forEach(customField -> {
            customField.setOptions(customOptions.get(customField.getFieldId()));
        });
        return customFields.stream().collect(Collectors.groupingBy(FunctionalCaseCustomFieldDTO::getCaseId));
    }

    private List<CaseReviewHistory> getReviewHistoryStatus(List<String> caseIds, String reviewId) {
        return QueryChain.of(CaseReviewHistory.class)
                .select(CASE_REVIEW_HISTORY.REVIEW_ID, CASE_REVIEW_HISTORY.CASE_ID, CASE_REVIEW_HISTORY.CREATE_USER, CASE_REVIEW_HISTORY.STATUS)
                .from(CASE_REVIEW_HISTORY)
                .where(CASE_REVIEW_HISTORY.CASE_ID.in(caseIds).and(CASE_REVIEW_HISTORY.REVIEW_ID.eq(reviewId)))
                .and(CASE_REVIEW_HISTORY.ABANDONED.eq(false).and(CASE_REVIEW_HISTORY.STATUS.ne("UNDER_REVIEWED")))
                .orderBy(CASE_REVIEW_HISTORY.CREATE_TIME.desc())
                .list();
    }

    private List<ReviewsDTO> selectReviewers(List<String> ids, String reviewId) {
        return QueryChain.of(CaseReviewFunctionalCaseUser.class)
                .select(CASE_REVIEW_FUNCTIONAL_CASE_USER.CASE_ID)
                .select(QueryMethods.groupConcat(CASE_REVIEW_FUNCTIONAL_CASE_USER.USER_ID).as("userIds"))
                .select(QueryMethods.groupConcat(USER.NAME).as("userNames"))
                .from(CASE_REVIEW_FUNCTIONAL_CASE_USER)
                .leftJoin(USER).on(CASE_REVIEW_FUNCTIONAL_CASE_USER.USER_ID.eq(USER.ID))
                .where(CASE_REVIEW_FUNCTIONAL_CASE_USER.REVIEW_ID.eq(reviewId)
                        .and(CASE_REVIEW_FUNCTIONAL_CASE_USER.CASE_ID.in(ids)))
                .groupBy(CASE_REVIEW_FUNCTIONAL_CASE_USER.CASE_ID)
                .listAs(ReviewsDTO.class);
    }

    private List<String> getIds(BaseReviewCaseBatchRequest request) {
        return queryChain().select(CASE_REVIEW_FUNCTIONAL_CASE.ID)
                .from(CASE_REVIEW_FUNCTIONAL_CASE)
                .leftJoin(FUNCTIONAL_CASE).on(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID.eq(FUNCTIONAL_CASE.ID))
                .where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(request.getReviewId()))
                .and(FUNCTIONAL_CASE.MODULE_ID.in(request.getModuleIds()))
                .and(FUNCTIONAL_CASE.NAME.like(request.getCondition().getKeyword())
                        .or(FUNCTIONAL_CASE.NUM.like(request.getCondition().getKeyword()))
                        .or(FUNCTIONAL_CASE.TAGS.like(request.getCondition().getKeyword())))
                .listAs(String.class);
    }

    private void doHandleStatusAndHistory(FunctionalCase functionalCase, String userId) {
        List<CaseReviewFunctionalCase> caseReviewFunctionalCases = queryChain().where(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID.eq(functionalCase.getId())).list();
        if (CollectionUtils.isNotEmpty(caseReviewFunctionalCases)) {
            //重新提审，作废之前的记录
            UpdateChain.of(CaseReviewHistory.class).set(CaseReviewHistory::getAbandoned, true)
                    .where(CaseReviewHistory::getCaseId).eq(functionalCase.getId())
                    .and(CaseReviewHistory::getAbandoned).eq(false).update();
            List<CaseReviewHistory> historyList = new ArrayList<>();
            caseReviewFunctionalCases.forEach(item -> {
                updateReviewCaseAndCaseStatus(item);
                insertHistory(item, historyList);
                //更新用例触发重新提审-需要重新计算评审的整体状态
                Map<String, Integer> countMap = new HashMap<>();
                countMap.put(item.getStatus(), 1);
                Map<String, String> statusMap = new HashMap<>();
                statusMap.put(item.getCaseId(), item.getStatus());
                Map<String, Object> param = new HashMap<>();
                param.put(CaseEvent.Param.REVIEW_ID, item.getReviewId());
                param.put(CaseEvent.Param.USER_ID, userId);
                param.put(CaseEvent.Param.CASE_IDS, List.of(item.getCaseId()));
                param.put(CaseEvent.Param.COUNT_MAP, countMap);
                param.put(CaseEvent.Param.STATUS_MAP, statusMap);
                param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.REVIEW_FUNCTIONAL_CASE);
                provider.updateCaseReview(param);
            });
            caseReviewHistoryMapper.insertBatch(historyList);
        }
    }

    private void insertHistory(CaseReviewFunctionalCase item, List<CaseReviewHistory> historyList) {
        CaseReviewHistory caseReviewHistory = new CaseReviewHistory();
        caseReviewHistory.setCaseId(item.getCaseId());
        caseReviewHistory.setReviewId(item.getReviewId());
        caseReviewHistory.setStatus(FunctionalCaseReviewStatus.RE_REVIEWED.name());
        caseReviewHistory.setCreateUser(UserRoleScope.SYSTEM);
        caseReviewHistory.setAbandoned(false);
        historyList.add(caseReviewHistory);
    }

    private void updateReviewCaseAndCaseStatus(CaseReviewFunctionalCase item) {
        item.setStatus(FunctionalCaseReviewStatus.RE_REVIEWED.name());
        mapper.update(item);
        UpdateChain.of(FunctionalCase.class)
                .set(FunctionalCase::getReviewStatus, FunctionalCaseReviewStatus.RE_REVIEWED.name())
                .where(FunctionalCase::getId).eq(item.getCaseId()).update();
    }
}
