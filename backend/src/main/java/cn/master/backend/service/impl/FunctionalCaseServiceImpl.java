package cn.master.backend.service.impl;

import cn.master.backend.constants.*;
import cn.master.backend.entity.*;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.handler.file.FileCenter;
import cn.master.backend.handler.file.FileCopyRequest;
import cn.master.backend.handler.file.FileRepository;
import cn.master.backend.handler.provider.BaseCaseProvider;
import cn.master.backend.handler.result.CaseManagementResultCode;
import cn.master.backend.handler.uid.IDGenerator;
import cn.master.backend.mapper.FunctionalCaseFollowerMapper;
import cn.master.backend.mapper.FunctionalCaseMapper;
import cn.master.backend.payload.dto.BaseFunctionalCaseBatchDTO;
import cn.master.backend.payload.dto.functional.*;
import cn.master.backend.payload.dto.project.ModuleCountDTO;
import cn.master.backend.payload.dto.system.LogDTO;
import cn.master.backend.payload.dto.system.OptionDTO;
import cn.master.backend.payload.dto.system.template.TemplateCustomFieldDTO;
import cn.master.backend.payload.dto.system.template.TemplateDTO;
import cn.master.backend.payload.dto.user.UserExtendDTO;
import cn.master.backend.payload.request.functional.FunctionalCaseAddRequest;
import cn.master.backend.payload.request.functional.FunctionalCaseDeleteRequest;
import cn.master.backend.payload.request.functional.FunctionalCaseEditRequest;
import cn.master.backend.payload.request.functional.FunctionalCasePageRequest;
import cn.master.backend.service.*;
import cn.master.backend.util.*;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.ApiScenarioTableDef.API_SCENARIO;
import static cn.master.backend.entity.table.ApiTestCaseTableDef.API_TEST_CASE;
import static cn.master.backend.entity.table.BugRelationCaseTableDef.BUG_RELATION_CASE;
import static cn.master.backend.entity.table.CaseReviewFunctionalCaseTableDef.CASE_REVIEW_FUNCTIONAL_CASE;
import static cn.master.backend.entity.table.CaseReviewHistoryTableDef.CASE_REVIEW_HISTORY;
import static cn.master.backend.entity.table.FunctionalCaseAttachmentTableDef.FUNCTIONAL_CASE_ATTACHMENT;
import static cn.master.backend.entity.table.FunctionalCaseCommentTableDef.FUNCTIONAL_CASE_COMMENT;
import static cn.master.backend.entity.table.FunctionalCaseCustomFieldTableDef.FUNCTIONAL_CASE_CUSTOM_FIELD;
import static cn.master.backend.entity.table.FunctionalCaseDemandTableDef.FUNCTIONAL_CASE_DEMAND;
import static cn.master.backend.entity.table.FunctionalCaseFollowerTableDef.FUNCTIONAL_CASE_FOLLOWER;
import static cn.master.backend.entity.table.FunctionalCaseRelationshipEdgeTableDef.FUNCTIONAL_CASE_RELATIONSHIP_EDGE;
import static cn.master.backend.entity.table.FunctionalCaseTableDef.FUNCTIONAL_CASE;
import static cn.master.backend.entity.table.FunctionalCaseTestTableDef.FUNCTIONAL_CASE_TEST;
import static cn.master.backend.entity.table.OperationHistoryTableDef.OPERATION_HISTORY;
import static cn.master.backend.entity.table.ProjectTableDef.PROJECT;
import static cn.master.backend.entity.table.ProjectVersionTableDef.PROJECT_VERSION;
import static cn.master.backend.entity.table.TestPlanCaseExecuteHistoryTableDef.TEST_PLAN_CASE_EXECUTE_HISTORY;
import static cn.master.backend.entity.table.TestPlanConfigTableDef.TEST_PLAN_CONFIG;
import static cn.master.backend.entity.table.TestPlanFunctionalCaseTableDef.TEST_PLAN_FUNCTIONAL_CASE;

/**
 * 功能用例 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FunctionalCaseServiceImpl extends ServiceImpl<FunctionalCaseMapper, FunctionalCase> implements FunctionalCaseService {
    private final ProjectVersionService projectVersionService;
    private final FunctionalCaseCustomFieldService functionalCaseCustomFieldService;
    private final FunctionalCaseAttachmentService functionalCaseAttachmentService;
    private final OperationLogService operationLogService;
    private final ProjectTemplateService projectTemplateService;
    private final ProjectService projectService;
    private final FunctionalCaseModuleServiceImpl functionalCaseModuleService;
    private final CaseReviewFunctionalCaseService caseReviewFunctionalCaseService;
    private final FunctionalCaseMapper functionalCaseMapper;
    private final FunctionalCaseFollowerMapper functionalCaseFollowerMapper;
    private final BaseCaseProvider provider;
    private final BaseCustomFieldService baseCustomFieldService;
    private final BaseCustomFieldOptionService baseCustomFieldOptionService;

    private static final String CASE_MODULE_COUNT_ALL = "all";

    private static final String ADD_FUNCTIONAL_CASE_FILE_LOG_URL = "/functional/case/add";
    private static final String UPDATE_FUNCTIONAL_CASE_FILE_LOG_URL = "/functional/case/update";
    private static final String FUNCTIONAL_CASE_BATCH_COPY_FILE_LOG_URL = "/functional/case/batch/copy";

    private static final String CASE_TABLE = "functional_case";

    @Override
    @Transactional(rollbackOn = Exception.class)
    public FunctionalCase addFunctionalCase(FunctionalCaseAddRequest request, List<MultipartFile> files, String userId, String organizationId) {
        String caseId = IDGenerator.nextStr();
        //添加功能用例
        FunctionalCase functionalCase = addCase(caseId, request, userId);
        //上传文件
        List<String> uploadFileIds = functionalCaseAttachmentService.uploadFile(request.getProjectId(), caseId, files, true, userId);
        //上传富文本里的文件
        functionalCaseAttachmentService.uploadMinioFile(caseId, request.getProjectId(), request.getCaseDetailFileIds(), userId, CaseFileSourceType.CASE_DETAIL.toString());
        //关联附件
        if (CollectionUtils.isNotEmpty(request.getRelateFileMetaIds())) {
            functionalCaseAttachmentService.association(request.getRelateFileMetaIds(), caseId, userId, ADD_FUNCTIONAL_CASE_FILE_LOG_URL, request.getProjectId());
        }
        //处理复制时的已存在的文件
        if (CollectionUtils.isNotEmpty(request.getAttachments())) {
            copyAttachment(request, userId, uploadFileIds, caseId);
        }
        log.info("保存用例的文件操作完成");
        addCaseReviewCase(request.getReviewId(), caseId, userId);

        //记录日志
        FunctionalCaseHistoryLogDTO historyLogDTO = getAddLogModule(functionalCase);
        saveAddDataLog(functionalCase, new FunctionalCaseHistoryLogDTO(), historyLogDTO, userId, organizationId, OperationLogType.ADD.name(), OperationLogModule.FUNCTIONAL_CASE);

        return functionalCase;
    }

    private void copyAttachment(FunctionalCaseAddRequest request, String userId, List<String> uploadFileIds, String caseId) {
        //获取用例已经上传的文件ID
        Map<String, FunctionalCaseAttachmentDTO> attachmentMap = request.getAttachments().stream().collect(Collectors.toMap(FunctionalCaseAttachmentDTO::getId, t -> t));
        List<String> attachmentFileIds = request.getAttachments().stream().filter(t -> !t.isDeleted()).map(FunctionalCaseAttachmentDTO::getId).filter(t -> !uploadFileIds.contains(t)).toList();
        if (CollectionUtils.isEmpty(attachmentFileIds)) {
            return;
        }
        List<FunctionalCaseAttachment> functionalCaseAttachments = QueryChain.of(FunctionalCaseAttachment.class)
                .where(FUNCTIONAL_CASE_ATTACHMENT.CASE_ID.eq(caseId)
                        .and(FUNCTIONAL_CASE_ATTACHMENT.FILE_ID.in(attachmentFileIds))).list();
        List<FunctionalCaseAttachment> oldFiles = QueryChain.of(FunctionalCaseAttachment.class)
                .where(FUNCTIONAL_CASE_ATTACHMENT.CASE_ID.ne(caseId)
                        .and(FUNCTIONAL_CASE_ATTACHMENT.FILE_ID.in(attachmentFileIds))).list();
        Map<String, List<FunctionalCaseAttachment>> oldFileMap = oldFiles.stream().collect(Collectors.groupingBy(FunctionalCaseAttachment::getFileId));
        List<String> attachmentFileIdsInDb = functionalCaseAttachments.stream().map(FunctionalCaseAttachment::getFileId).toList();
        List<String> saveAttachmentFileIds = attachmentFileIds.stream().filter(t -> !attachmentFileIdsInDb.contains(t)).toList();
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        for (String saveAttachmentFileId : saveAttachmentFileIds) {
            FunctionalCaseAttachmentDTO functionalCaseAttachmentDTO = attachmentMap.get(saveAttachmentFileId);
            FunctionalCaseAttachment caseAttachment = functionalCaseAttachmentService.creatAttachment(saveAttachmentFileId, functionalCaseAttachmentDTO.getFileName(), functionalCaseAttachmentDTO.getSize(), caseId, functionalCaseAttachmentDTO.getLocal(), userId);
            if (functionalCaseAttachmentDTO.getLocal()) {
                caseAttachment.setFileSource(CaseFileSourceType.ATTACHMENT.toString());
                LogUtils.info("开始复制文件");
                copyFile(request, caseId, saveAttachmentFileId, oldFileMap, functionalCaseAttachmentDTO, defaultRepository);
            }
            functionalCaseAttachmentService.save(caseAttachment);
        }
    }

    private static void copyFile(FunctionalCaseAddRequest request, String caseId, String saveAttachmentFileId, Map<String, List<FunctionalCaseAttachment>> oldFileMap, FunctionalCaseAttachmentDTO functionalCaseAttachmentDTO, FileRepository defaultRepository) {
        List<FunctionalCaseAttachment> oldFunctionalCaseAttachments = oldFileMap.get(saveAttachmentFileId);
        if (CollectionUtils.isNotEmpty(oldFunctionalCaseAttachments)) {
            FunctionalCaseAttachment functionalCaseAttachment = oldFunctionalCaseAttachments.getFirst();
            // 复制文件
            FileCopyRequest fileCopyRequest = new FileCopyRequest();
            fileCopyRequest.setCopyFolder(DefaultRepositoryDir.getFunctionalCaseDir(request.getProjectId(), functionalCaseAttachment.getCaseId()) + "/" + saveAttachmentFileId);
            fileCopyRequest.setCopyfileName(functionalCaseAttachmentDTO.getFileName());
            fileCopyRequest.setFileName(functionalCaseAttachmentDTO.getFileName());
            fileCopyRequest.setFolder(DefaultRepositoryDir.getFunctionalCaseDir(request.getProjectId(), caseId) + "/" + saveAttachmentFileId);
            // 将文件从上一个用例复制到当前用例资源目录
            try {
                defaultRepository.copyFile(fileCopyRequest);
            } catch (Exception e) {
                LogUtils.error("复制文件失败：{}", e);
            }
        }
    }

    private void addCaseReviewCase(String reviewId, String caseId, String userId) {
        if (StringUtils.isNotBlank(reviewId)) {
            QueryChain.of(CaseReview.class).where(CaseReview::getId).eq(reviewId).oneOpt()
                    .orElseThrow(() -> new MSException(CaseManagementResultCode.CASE_REVIEW_NOT_FOUND));
            caseReviewFunctionalCaseService.addCaseReviewFunctionalCase(caseId, userId, reviewId);
        }
    }

    private void saveAddDataLog(FunctionalCase functionalCase, FunctionalCaseHistoryLogDTO originalValue, FunctionalCaseHistoryLogDTO modifiedLogDTO, String userId, String organizationId, String type, String module) {
        LogDTO dto = new LogDTO(
                functionalCase.getProjectId(),
                organizationId,
                functionalCase.getId(),
                userId,
                type,
                module,
                functionalCase.getName());
        dto.setHistory(true);
        dto.setPath("/functional/case/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setModifiedValue(JSON.toJSONBytes(modifiedLogDTO));
        dto.setOriginalValue(JSON.toJSONBytes(originalValue));
        operationLogService.add(dto);
    }

    private FunctionalCaseHistoryLogDTO getAddLogModule(FunctionalCase functionalCase) {
        List<FunctionalCaseCustomField> customFields = QueryChain.of(FunctionalCaseCustomField.class)
                .where(FUNCTIONAL_CASE_CUSTOM_FIELD.CASE_ID.eq(functionalCase.getId())).list();
        List<FunctionalCaseAttachment> caseAttachments = QueryChain.of(FunctionalCaseAttachment.class)
                .where(FUNCTIONAL_CASE_ATTACHMENT.CASE_ID.eq(functionalCase.getId())).list();
        List<FileAssociation> fileAssociationList = QueryChain.of(FileAssociation.class).where(FileAssociation::getSourceId).eq(functionalCase.getId()).list();
        return new FunctionalCaseHistoryLogDTO(functionalCase, customFields, caseAttachments, fileAssociationList);
    }

    @Override
    public long getNextNum(String projectId) {
        return NumGenerator.nextNum(projectId, ApplicationNumScope.CASE_MANAGEMENT);
    }

    @Override
    public Long getNextOrder(String projectId) {
        Long pos = queryChain().select(FUNCTIONAL_CASE.POS).from(FUNCTIONAL_CASE)
                .where(FUNCTIONAL_CASE.PROJECT_ID.eq(projectId))
                .orderBy(FUNCTIONAL_CASE.POS.desc()).limit(1)
                .oneAs(Long.class);
        return (pos == null ? 0 : pos) + ServiceUtils.POS_STEP;
    }

    @Override
    public FunctionalCaseDetailDTO getFunctionalCaseDetail(String functionalCaseId, String userId, boolean checkDetailCount) {
        FunctionalCase functionalCase = checkFunctionalCase(functionalCaseId);
        FunctionalCaseDetailDTO functionalCaseDetailDTO = new FunctionalCaseDetailDTO();
        BeanUtils.copyProperties(functionalCase, functionalCaseDetailDTO);
        functionalCaseDetailDTO.setSteps(new String(functionalCase.getSteps() == null ? new byte[0] : functionalCase.getSteps(), StandardCharsets.UTF_8));
        functionalCaseDetailDTO.setTextDescription(new String(functionalCase.getTextDescription() == null ? new byte[0] : functionalCase.getTextDescription(), StandardCharsets.UTF_8));
        functionalCaseDetailDTO.setExpectedResult(new String(functionalCase.getExpectedResult() == null ? new byte[0] : functionalCase.getExpectedResult(), StandardCharsets.UTF_8));
        functionalCaseDetailDTO.setPrerequisite(new String(functionalCase.getPrerequisite() == null ? new byte[0] : functionalCase.getPrerequisite(), StandardCharsets.UTF_8));
        functionalCaseDetailDTO.setDescription(new String(functionalCase.getDescription() == null ? new byte[0] : functionalCase.getDescription(), StandardCharsets.UTF_8));
        //模板校验 获取自定义字段
        checkTemplateCustomField(functionalCaseDetailDTO, functionalCase);
        //是否关注用例
        Boolean isFollow = checkIsFollowCase(functionalCase.getId(), userId);
        functionalCaseDetailDTO.setFollowFlag(isFollow);
        //获取附件信息
        functionalCaseAttachmentService.getAttachmentInfo(functionalCaseDetailDTO);
        List<ProjectVersion> versions = projectVersionService.getVersionByIds(List.of(functionalCaseDetailDTO.getVersionId()));
        if (CollectionUtils.isNotEmpty(versions)) {
            functionalCaseDetailDTO.setVersionName(versions.getFirst().getName());
        }

        //模块名称
        handDTO(functionalCaseDetailDTO);

        if (checkDetailCount) {
            //处理已关联需求数量/缺陷数量/用例数量
            handleCount(functionalCaseDetailDTO);
        }
        return functionalCaseDetailDTO;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public FunctionalCase updateFunctionalCase(FunctionalCaseEditRequest request, List<MultipartFile> files, String userId) {
        FunctionalCase checked = checkFunctionalCase(request.getId());
        //对于用例模块的变更，同一用例的其他版本用例也需要变更
        if (!StringUtils.equals(checked.getModuleId(), request.getModuleId())) {
            //updateFunctionalCaseModule(checked.getRefId(), request.getModuleId());
            UpdateChain.of(FunctionalCase.class).set(FUNCTIONAL_CASE.MODULE_ID, request.getModuleId())
                    .where(FUNCTIONAL_CASE.REF_ID.eq(checked.getRefId())).update();
        }
        //基本信息
        FunctionalCase functionalCase = new FunctionalCase();
        BeanUtils.copyProperties(request, functionalCase);
        updateCase(request, userId, functionalCase);
        //处理删除文件id
        if (CollectionUtils.isNotEmpty(request.getDeleteFileMetaIds())) {
            functionalCaseAttachmentService.deleteCaseAttachment(request.getDeleteFileMetaIds(), request.getId(), request.getProjectId());
        }

        //处理取消关联文件id
        if (CollectionUtils.isNotEmpty(request.getUnLinkFilesIds())) {
            functionalCaseAttachmentService.unAssociation(request.getId(), request.getUnLinkFilesIds(), UPDATE_FUNCTIONAL_CASE_FILE_LOG_URL, userId, request.getProjectId());
        }

        //上传新文件
        functionalCaseAttachmentService.uploadFile(request.getProjectId(), request.getId(), files, true, userId);

        //上传富文本文件
        functionalCaseAttachmentService.uploadMinioFile(request.getId(), request.getProjectId(), request.getCaseDetailFileIds(), userId, CaseFileSourceType.CASE_DETAIL.toString());

        //关联新附件
        if (CollectionUtils.isNotEmpty(request.getRelateFileMetaIds())) {
            functionalCaseAttachmentService.association(request.getRelateFileMetaIds(), request.getId(), userId, UPDATE_FUNCTIONAL_CASE_FILE_LOG_URL, request.getProjectId());
        }

        //处理评审状态
        //handleReviewStatus(request, checked.getName(), userId);
        caseReviewFunctionalCaseService.reReviewedCase(request, checked, userId);

        return functionalCase;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void editFollower(String functionalCaseId, String userId) {
        checkFunctionalCase(functionalCaseId);
        QueryChain<FunctionalCaseFollower> queryChain = QueryChain.of(functionalCaseFollowerMapper)
                .where(FUNCTIONAL_CASE_FOLLOWER.CASE_ID.eq(functionalCaseId)
                        .and(FUNCTIONAL_CASE_FOLLOWER.USER_ID.eq(userId)));
        if (queryChain.exists()) {
            LogicDeleteManager.execWithoutLogicDelete(() -> functionalCaseFollowerMapper.deleteByQuery(queryChain));
        } else {
            functionalCaseFollowerMapper.insert(FunctionalCaseFollower.builder().caseId(functionalCaseId).userId(userId).build());
        }
    }

    @Override
    public List<FunctionalCaseVersionDTO> getFunctionalCaseVersion(String functionalCaseId) {
        FunctionalCase functionalCase = checkFunctionalCase(functionalCaseId);
        return QueryChain.of(functionalCaseMapper)
                .select(FUNCTIONAL_CASE.ID, FUNCTIONAL_CASE.NAME, FUNCTIONAL_CASE.VERSION_ID, FUNCTIONAL_CASE.PROJECT_ID)
                .from(FUNCTIONAL_CASE)
                .where(FUNCTIONAL_CASE.REF_ID.eq(functionalCase.getRefId()))
                .listAs(FunctionalCaseVersionDTO.class);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteFunctionalCase(FunctionalCaseDeleteRequest request, String userId) {
        handDeleteFunctionalCase(Collections.singletonList(request.getId()), request.getDeleteAll(), userId, request.getProjectId());
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void handDeleteFunctionalCase(List<String> ids, Boolean deleteAll, String userId, String projectId) {
        Map<String, Object> param = new HashMap<>();
        if (deleteAll) {
            List<String> refId = queryChain().select(FUNCTIONAL_CASE.REF_ID).from(FUNCTIONAL_CASE)
                    .where(FUNCTIONAL_CASE.ID.in(ids))
                    .groupBy(FUNCTIONAL_CASE.REF_ID).listAs(String.class);
            List<FunctionalCase> functionalCases = queryChain().where(FUNCTIONAL_CASE.REF_ID.in(refId)).list();
            List<String> caseIds = functionalCases.stream().map(FunctionalCase::getId).toList();
            param.put(CaseEvent.Param.CASE_IDS, CollectionUtils.isNotEmpty(caseIds) ? caseIds : new ArrayList<>());
            updateChain().set(FUNCTIONAL_CASE.DELETED, true)
                    .set(FUNCTIONAL_CASE.DELETE_USER, userId)
                    .set(FUNCTIONAL_CASE.DELETE_TIME, LocalDateTime.now())
                    .where(FUNCTIONAL_CASE.REF_ID.in(refId)).update();
        } else {
            param.put(CaseEvent.Param.CASE_IDS, CollectionUtils.isNotEmpty(ids) ? ids : new ArrayList<>());
            updateChain().set(FUNCTIONAL_CASE.DELETED, true)
                    .set(FUNCTIONAL_CASE.DELETE_USER, userId)
                    .set(FUNCTIONAL_CASE.DELETE_TIME, LocalDateTime.now())
                    .where(FUNCTIONAL_CASE.ID.in(ids)).update();
        }
        param.put(CaseEvent.Param.USER_ID, userId);
        param.put(CaseEvent.Param.EVENT_NAME, CaseEvent.Event.DELETE_FUNCTIONAL_CASE);
        provider.updateCaseReview(param);
    }

    @Override
    public Page<FunctionalCasePageDTO> getFunctionalCasePage(FunctionalCasePageRequest request, Boolean deleted, Boolean isRepeat) {
        Page<FunctionalCasePageDTO> page = queryChain()
                .select(FUNCTIONAL_CASE.ALL_COLUMNS, PROJECT_VERSION.NAME.as("versionName")).from(FUNCTIONAL_CASE)
                .leftJoin(PROJECT_VERSION).on(FUNCTIONAL_CASE.VERSION_ID.eq(PROJECT_VERSION.ID))
                .where(FUNCTIONAL_CASE.PROJECT_ID.eq(request.getProjectId()))
                .and(FUNCTIONAL_CASE.MODULE_ID.in(request.getModuleIds()))
                .and(FUNCTIONAL_CASE.NAME.like(request.getKeyword())
                        .or(FUNCTIONAL_CASE.NUM.like(request.getKeyword()))
                        .or(FUNCTIONAL_CASE.TAGS.like(request.getKeyword())))
                .and(FUNCTIONAL_CASE.ID.notIn(
                        QueryChain.of(CaseReviewFunctionalCase.class).select(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID)
                                .from(CASE_REVIEW_FUNCTIONAL_CASE).where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(request.getReviewId()))
                                .listAs(String.class)
                ).when(Objects.nonNull(request.getReviewId())))
                .and(FUNCTIONAL_CASE.ID.notIn(
                                QueryChain.of(TestPlanFunctionalCase.class).select(TEST_PLAN_FUNCTIONAL_CASE.FUNCTIONAL_CASE_ID)
                                        .from(TEST_PLAN_FUNCTIONAL_CASE).where(TEST_PLAN_FUNCTIONAL_CASE.TEST_PLAN_ID.eq(request.getTestPlanId()))
                                        .listAs(String.class)
                        ).when(!isRepeat)
                )
                .and(FUNCTIONAL_CASE.ID.notIn(request.getExcludeIds()))
                .orderBy(FUNCTIONAL_CASE.POS.desc())
                .pageAs(Page.of(request.getCurrent(), request.getPageSize()), FunctionalCasePageDTO.class);
        return handleCustomFields(page, request.getProjectId());
    }

    @Override
    public List<String> getIds(BaseFunctionalCaseBatchDTO request, String projectId, boolean deleted) {
        return queryChain().select(FUNCTIONAL_CASE.ID).from(FUNCTIONAL_CASE)
                .where(FUNCTIONAL_CASE.PROJECT_ID.eq(projectId))
                .and(FUNCTIONAL_CASE.MODULE_ID.in(request.getModuleIds()))
                .and(FUNCTIONAL_CASE.NAME.like(request.getCondition().getKeyword())
                        .or(FUNCTIONAL_CASE.NUM.like(request.getCondition().getKeyword()))
                        .or(FUNCTIONAL_CASE.TAGS.like(request.getCondition().getKeyword())))
                .listAs(String.class);
    }

    @Override
    public Map<String, Long> moduleCount(FunctionalCasePageRequest request, boolean delete) {
        if (StringUtils.isNotEmpty(request.getTestPlanId())) {
            checkTestPlanRepeatCase(request);
        }
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        request.setModuleIds(null);
        List<ModuleCountDTO> moduleCountDTOList = countModuleIdByRequest(request, delete);
        Map<String, Long> moduleCountMap = functionalCaseModuleService.getModuleCountMap(request.getProjectId(), moduleCountDTOList);
        //查出全部用例数量
        AtomicLong allCount = new AtomicLong(0);
        moduleCountDTOList.forEach(item -> allCount.addAndGet(item.getDataCount()));
        moduleCountMap.put(CASE_MODULE_COUNT_ALL, allCount.get());
        return moduleCountMap;
    }

    private List<ModuleCountDTO> countModuleIdByRequest(FunctionalCasePageRequest request, boolean delete) {
        return queryChain()
                .select(FUNCTIONAL_CASE.MODULE_ID, QueryMethods.count(FUNCTIONAL_CASE.ID).as("dataCount")).from(FUNCTIONAL_CASE)
                .where(FUNCTIONAL_CASE.PROJECT_ID.eq(request.getProjectId()))
                .and(FUNCTIONAL_CASE.MODULE_ID.in(request.getModuleIds()))
                .and(FUNCTIONAL_CASE.NAME.like(request.getKeyword())
                        .or(FUNCTIONAL_CASE.NUM.like(request.getKeyword()))
                        .or(FUNCTIONAL_CASE.TAGS.like(request.getKeyword())))
                .and(FUNCTIONAL_CASE.ID.notIn(
                        QueryChain.of(CaseReviewFunctionalCase.class).select(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID)
                                .from(CASE_REVIEW_FUNCTIONAL_CASE).where(CASE_REVIEW_FUNCTIONAL_CASE.REVIEW_ID.eq(request.getReviewId()))
                                .listAs(String.class)
                ).when(Objects.nonNull(request.getReviewId())))
                .and(FUNCTIONAL_CASE.ID.notIn(
                                QueryChain.of(TestPlanFunctionalCase.class).select(TEST_PLAN_FUNCTIONAL_CASE.FUNCTIONAL_CASE_ID)
                                        .from(TEST_PLAN_FUNCTIONAL_CASE).where(TEST_PLAN_FUNCTIONAL_CASE.TEST_PLAN_ID.eq(request.getTestPlanId()))
                                        .listAs(String.class)
                        ).when(Objects.nonNull(request.getTestPlanId()))
                )
                .and(FUNCTIONAL_CASE.ID.notIn(request.getExcludeIds()))
                .groupBy(FUNCTIONAL_CASE.MODULE_ID)
                .listAs(ModuleCountDTO.class);
    }

    private void checkTestPlanRepeatCase(FunctionalCasePageRequest request) {
        TestPlanConfig testPlanConfig = QueryChain.of(TestPlanConfig.class).where(TEST_PLAN_CONFIG.TEST_PLAN_ID.eq(request.getTestPlanId())).one();
        if (testPlanConfig != null && BooleanUtils.isTrue(testPlanConfig.getRepeatCase())) {
            //测试计划允许重复用例，意思就是统计不受测试计划影响。去掉这个条件，
            request.setTestPlanId(null);
        }
    }

    private Page<FunctionalCasePageDTO> handleCustomFields(Page<FunctionalCasePageDTO> page, String projectId) {
        List<String> ids = page.getRecords().stream().map(FunctionalCasePageDTO::getId).collect(Collectors.toList());
        Map<String, List<FunctionalCaseCustomFieldDTO>> collect = getCaseCustomFiledMap(ids, projectId);
        page.getRecords().forEach(functionalCasePageDTO -> functionalCasePageDTO.setCustomFields(collect.get(functionalCasePageDTO.getId())));
        return page;
    }

    private Map<String, List<FunctionalCaseCustomFieldDTO>> getCaseCustomFiledMap(List<String> ids, String projectId) {
        List<CustomFieldOption> memberCustomOption = getMemberOptions(projectId);
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
            if (StringUtils.equalsAnyIgnoreCase(customField.getType(), CustomFieldType.MEMBER.name(), CustomFieldType.MULTIPLE_MEMBER.name())) {
                customField.setOptions(memberCustomOption);
            }
        });
        return customFields.stream().collect(Collectors.groupingBy(FunctionalCaseCustomFieldDTO::getCaseId));
    }

    private void updateCase(FunctionalCaseEditRequest request, String userId, FunctionalCase functionalCase) {
        functionalCase.setUpdateUser(userId);
        functionalCase.setSteps(StringUtils.defaultIfBlank(request.getSteps(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCase.setTextDescription(StringUtils.defaultIfBlank(request.getTextDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCase.setExpectedResult(StringUtils.defaultIfBlank(request.getExpectedResult(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCase.setPrerequisite(StringUtils.defaultIfBlank(request.getPrerequisite(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCase.setDescription(StringUtils.defaultIfBlank(request.getDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCaseMapper.update(functionalCase);
        //更新自定义字段
        List<CaseCustomFieldDTO> customFields = request.getCustomFields();
        if (CollectionUtils.isNotEmpty(customFields)) {
            customFields = customFields.stream().distinct().collect(Collectors.toList());
            functionalCaseCustomFieldService.updateCustomField(request.getId(), customFields);
        }
    }

    private void handleCount(FunctionalCaseDetailDTO functionalCaseDetailDTO) {
        //获取已关联需求数量
        functionalCaseDetailDTO.setDemandCount(
                QueryChain.of(FunctionalCaseDemand.class).where(FUNCTIONAL_CASE_DEMAND.CASE_ID.eq(functionalCaseDetailDTO.getId())).count()
        );
        //获取已关联用例数量
        functionalCaseDetailDTO.setCaseCount(getOtherCaseCount(functionalCaseDetailDTO.getId()));
        //获取已关联缺陷数量
        functionalCaseDetailDTO.setBugCount(
                QueryChain.of(BugRelationCase.class).where(BUG_RELATION_CASE.CASE_ID.eq(functionalCaseDetailDTO.getId())).count()
        );
        //获取已关联依赖关系数量
        functionalCaseDetailDTO.setRelateEdgeCount(
                QueryChain.of(FunctionalCaseRelationshipEdge.class).
                        where(FUNCTIONAL_CASE_RELATIONSHIP_EDGE.SOURCE_ID.eq(functionalCaseDetailDTO.getId())
                                .or(FUNCTIONAL_CASE_RELATIONSHIP_EDGE.TARGET_ID.eq(functionalCaseDetailDTO.getId()))).count()
        );
        //获取已关联用例评审数量
        functionalCaseDetailDTO.setCaseReviewCount(
                QueryChain.of(CaseReviewFunctionalCase.class).where(CASE_REVIEW_FUNCTIONAL_CASE.CASE_ID.eq(functionalCaseDetailDTO.getId())).count()
        );
        //获取已关联测试计划数量
        List<TestPlanFunctionalCase> testPlanFunctionalCases = QueryChain.of(TestPlanFunctionalCase.class)
                .where(TEST_PLAN_FUNCTIONAL_CASE.FUNCTIONAL_CASE_ID.eq(functionalCaseDetailDTO.getId())).list();

        if (CollectionUtils.isNotEmpty(testPlanFunctionalCases)) {
            Map<String, List<TestPlanFunctionalCase>> planMap = testPlanFunctionalCases.stream().collect(Collectors.groupingBy(TestPlanFunctionalCase::getTestPlanId));
            functionalCaseDetailDTO.setTestPlanCount(planMap.size());
        } else {
            functionalCaseDetailDTO.setTestPlanCount(0);
        }
        //获取评论总数量数量
        List<OptionDTO> commentList = new ArrayList<>();
        long caseComment = QueryChain.of(FunctionalCaseComment.class).where(FUNCTIONAL_CASE_COMMENT.CASE_ID.eq(functionalCaseDetailDTO.getId())).count();
        OptionDTO caseOption = new OptionDTO();
        caseOption.setId("caseComment");
        caseOption.setName(String.valueOf(caseComment));
        commentList.addFirst(caseOption);
        long reviewComment = QueryChain.of(CaseReviewHistory.class).where(CASE_REVIEW_HISTORY.CASE_ID.eq(functionalCaseDetailDTO.getId())).count();
        OptionDTO reviewOption = new OptionDTO();
        reviewOption.setId("reviewComment");
        reviewOption.setName(String.valueOf(reviewComment));
        commentList.add(1, reviewOption);
        long testPlanExecuteComment = QueryChain.of(TestPlanCaseExecuteHistory.class).where(TEST_PLAN_CASE_EXECUTE_HISTORY.CASE_ID.eq(functionalCaseDetailDTO.getId())).count();
        OptionDTO executeOption = new OptionDTO();
        executeOption.setId("executiveComment");
        executeOption.setName(String.valueOf(testPlanExecuteComment));
        commentList.add(2, executeOption);
        functionalCaseDetailDTO.setCommentList(commentList);
        long commentCount = caseComment + reviewComment + testPlanExecuteComment;
        functionalCaseDetailDTO.setCommentCount(commentCount);
        //获取变更历史数量数量
        List<String> types = List.of(OperationLogType.ADD.name(), OperationLogType.IMPORT.name(), OperationLogType.UPDATE.name());
        long count = QueryChain.of(OperationHistory.class).where(OPERATION_HISTORY.SOURCE_ID.eq(functionalCaseDetailDTO.getId())
                .and(OPERATION_HISTORY.MODULE.eq(OperationLogModule.FUNCTIONAL_CASE))
                .and(OPERATION_HISTORY.TYPE.in(types))).count();
        functionalCaseDetailDTO.setHistoryCount(count);
    }

    private Long getOtherCaseCount(String caseId) {
        return QueryChain.of(FunctionalCaseTest.class)
                .select(QueryMethods.count(FUNCTIONAL_CASE_TEST.ID))
                .from(FUNCTIONAL_CASE_TEST)
                .leftJoin(PROJECT).on(PROJECT.ID.eq(FUNCTIONAL_CASE_TEST.PROJECT_ID))
                .leftJoin(PROJECT_VERSION).on(PROJECT_VERSION.ID.eq(FUNCTIONAL_CASE_TEST.VERSION_ID))
                .leftJoin(API_TEST_CASE).on(API_TEST_CASE.ID.eq(FUNCTIONAL_CASE_TEST.SOURCE_ID))
                .leftJoin(API_SCENARIO).on(API_SCENARIO.ID.eq(FUNCTIONAL_CASE_TEST.SOURCE_ID))
                .where(FUNCTIONAL_CASE_TEST.CASE_ID.eq(caseId))
                .count();
    }

    private void handDTO(FunctionalCaseDetailDTO functionalCaseDetailDTO) {
        String name = functionalCaseModuleService.getModuleName(functionalCaseDetailDTO.getModuleId());
        functionalCaseDetailDTO.setModuleName(name);

        User user = QueryChain.of(User.class).where(User::getId).eq(functionalCaseDetailDTO.getCreateUser()).one();
        functionalCaseDetailDTO.setCreateUserName(user.getName());
    }

    private Boolean checkIsFollowCase(String caseId, String userId) {
        return QueryChain.of(FunctionalCaseFollower.class)
                .where(FUNCTIONAL_CASE_FOLLOWER.CASE_ID.eq(caseId)
                        .and(FUNCTIONAL_CASE_FOLLOWER.USER_ID.eq(userId))).count() > 0;
    }

    private void checkTemplateCustomField(FunctionalCaseDetailDTO functionalCaseDetailDTO, FunctionalCase functionalCase) {
        TemplateDTO templateDTO = projectTemplateService.getTemplateDTOById(functionalCase.getTemplateId(), functionalCase.getProjectId(), TemplateScene.FUNCTIONAL.name());
        if (CollectionUtils.isNotEmpty(templateDTO.getCustomFields())) {
            List<TemplateCustomFieldDTO> customFields = templateDTO.getCustomFields();
            List<String> fieldIds = customFields.stream().map(TemplateCustomFieldDTO::getFieldId).distinct().toList();
            List<FunctionalCaseCustomField> functionalCaseCustomFields = QueryChain.of(FunctionalCaseCustomField.class)
                    .where(FUNCTIONAL_CASE_CUSTOM_FIELD.CASE_ID.eq(functionalCase.getId()))
                    .and(FUNCTIONAL_CASE_CUSTOM_FIELD.FIELD_ID.in(fieldIds))
                    .list();
            Map<String, FunctionalCaseCustomField> customFieldMap = functionalCaseCustomFields.stream().collect(Collectors.toMap(FunctionalCaseCustomField::getFieldId, t -> t));
            List<CustomFieldOption> memberCustomOption = getMemberOptions(functionalCase.getProjectId());
            customFields.forEach(item -> {
                if (StringUtils.equalsAnyIgnoreCase(item.getType(), CustomFieldType.MEMBER.name(), CustomFieldType.MULTIPLE_MEMBER.name())) {
                    item.setOptions(memberCustomOption);
                }
                FunctionalCaseCustomField caseCustomField = customFieldMap.get(item.getFieldId());
                Optional.ofNullable(caseCustomField).ifPresentOrElse(customField -> {
                    item.setDefaultValue(customField.getValue());
                    if (Translator.get("custom_field.functional_priority").equals(item.getFieldName())) {
                        functionalCaseDetailDTO.setFunctionalPriority(customField.getValue());
                    }
                }, () -> {
                });
            });
            functionalCaseDetailDTO.setCustomFields(customFields);
        }
    }

    private List<CustomFieldOption> getMemberOptions(String projectId) {
        List<UserExtendDTO> memberOption = projectService.getMemberOption(projectId, null);
        return memberOption.stream().map(option -> {
            CustomFieldOption customFieldOption = new CustomFieldOption();
            customFieldOption.setFieldId(option.getId());
            customFieldOption.setValue(option.getId());
            customFieldOption.setInternal(false);
            customFieldOption.setText(option.getName());
            return customFieldOption;
        }).toList();
    }

    private FunctionalCase checkFunctionalCase(String functionalCaseId) {
        return queryChain().where(FUNCTIONAL_CASE.ID.eq(functionalCaseId))
                .oneOpt().orElseThrow(() -> new MSException(CaseManagementResultCode.FUNCTIONAL_CASE_NOT_FOUND));
    }

    private FunctionalCase addCase(String caseId, FunctionalCaseAddRequest request, String userId) {
        FunctionalCase functionalCase = new FunctionalCase();
        BeanUtils.copyProperties(request, functionalCase);
        functionalCase.setId(caseId);
        functionalCase.setNum(getNextNum(request.getProjectId()));
        functionalCase.setReviewStatus(FunctionalCaseReviewStatus.UN_REVIEWED.name());
        functionalCase.setPos(getNextOrder(request.getProjectId()));
        functionalCase.setRefId(caseId);
        functionalCase.setLastExecuteResult(ExecStatus.PENDING.name());
        functionalCase.setLatest(true);
        functionalCase.setCreateUser(userId);
        functionalCase.setUpdateUser(userId);
        functionalCase.setVersionId(StringUtils.defaultIfBlank(request.getVersionId(), projectVersionService.getDefaultVersion(request.getProjectId())));
        functionalCase.setSteps(StringUtils.defaultIfBlank(request.getSteps(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCase.setTextDescription(StringUtils.defaultIfBlank(request.getTextDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCase.setExpectedResult(StringUtils.defaultIfBlank(request.getExpectedResult(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCase.setPrerequisite(StringUtils.defaultIfBlank(request.getPrerequisite(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCase.setDescription(StringUtils.defaultIfBlank(request.getDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
        functionalCase.setPublicCase(true);
        mapper.insert(functionalCase);
        //保存自定义字段
        List<CaseCustomFieldDTO> customFields = request.getCustomFields();
        if (CollectionUtils.isNotEmpty(customFields)) {
            customFields = customFields.stream().distinct().collect(Collectors.toList());
            functionalCaseCustomFieldService.saveCustomField(caseId, customFields);
        }
        return functionalCase;
    }
}
