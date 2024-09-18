package cn.master.backend.service.impl;

import cn.master.backend.constants.DefaultRepositoryDir;
import cn.master.backend.constants.StorageType;
import cn.master.backend.entity.*;
import cn.master.backend.handler.file.FileRequest;
import cn.master.backend.mapper.*;
import cn.master.backend.payload.dto.SelectOption;
import cn.master.backend.service.BugCommonService;
import cn.master.backend.service.FileService;
import cn.master.backend.service.ProjectApplicationService;
import cn.master.backend.util.FileAssociationSourceUtil;
import cn.master.backend.util.LogUtils;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 08/19/2024
 **/
@Service
@RequiredArgsConstructor
public class BugCommonServiceImpl implements BugCommonService {
    private final ProjectApplicationService projectApplicationService;
    private final FileAssociationMapper fileAssociationMapper;
    private final BugLocalAttachmentMapper bugLocalAttachmentMapper;
    private final FileService fileService;
    private final BugCustomFieldMapper bugCustomFieldMapper;
    private final BugRelationCaseMapper bugRelationCaseMapper;
    private final BugCommentMapper bugCommentMapper;
    private final BugFollowerMapper bugFollowerMapper;

    @Override
    public List<SelectOption> getHeaderHandlerOption(String projectId) {
        //todo
        return List.of();
    }

    @Override
    public List<SelectOption> getLocalHandlerOption(String projectId) {
        //todo
        return List.of();
    }

    @Override
    public Map<String, String> getAllStatusMap(String projectId) {
        //todo
        return Map.of();
    }

    @Override
    public void clearAssociateResource(String projectId, List<String> bugIds) {
        // 清空附件(关联, 本地上传, 富文本)
        QueryChain<FileAssociation> queryChain = QueryChain.of(fileAssociationMapper).where(FileAssociation::getSourceId).in(bugIds)
                .and(FileAssociation::getSourceType).eq(FileAssociationSourceUtil.SOURCE_TYPE_BUG);
        fileAssociationMapper.deleteByQuery(queryChain);
        QueryChain<BugLocalAttachment> bugLocalAttachmentQueryChain = QueryChain.of(bugLocalAttachmentMapper).where(BugLocalAttachment::getBugId).in(bugIds);
        List<BugLocalAttachment> bugLocalAttachments = bugLocalAttachmentQueryChain.list();
        // 附件类型
        bugLocalAttachments.forEach(bugLocalAttachment -> {
            FileRequest fileRequest = new FileRequest();
            fileRequest.setFolder(DefaultRepositoryDir.getBugDir(projectId, bugLocalAttachment.getBugId()) + "/" + bugLocalAttachment.getFileId());
            fileRequest.setFileName(StringUtils.isEmpty(bugLocalAttachment.getFileName()) ? null : bugLocalAttachment.getFileName());
            fileRequest.setStorage(StorageType.MINIO.name());
            try {
                fileService.deleteFile(fileRequest);
            } catch (Exception e) {
                LogUtils.info("清理缺陷相关附件发生错误: " + e.getMessage());
            }
        });
        LogicDeleteManager.execWithoutLogicDelete(() -> bugLocalAttachmentMapper.deleteByQuery(bugLocalAttachmentQueryChain));
        // 清除自定义字段关系
        QueryChain<BugCustomField> bugCustomFieldQueryChain = QueryChain.of(bugCustomFieldMapper).where(BugCustomField::getBugId).in(bugIds);
        LogicDeleteManager.execWithoutLogicDelete(() -> bugCustomFieldMapper.deleteByQuery(bugCustomFieldQueryChain));
        // 清空关联用例
        QueryChain<BugRelationCase> bugRelationCaseQueryChain = QueryChain.of(bugRelationCaseMapper).where(BugRelationCase::getBugId).in(bugIds);
        LogicDeleteManager.execWithoutLogicDelete(() -> bugCustomFieldMapper.deleteByQuery(bugRelationCaseQueryChain));
// 清空评论
        QueryChain<BugComment> bugCommentQueryChain = QueryChain.of(bugCommentMapper).where(BugComment::getBugId).in(bugIds);
        LogicDeleteManager.execWithoutLogicDelete(() -> bugCommentMapper.deleteByQuery(bugCommentQueryChain));
// 清空关注人
        QueryChain<BugFollower> bugFollowerQueryChain = QueryChain.of(bugFollowerMapper).where(BugFollower::getBugId).in(bugIds);
        LogicDeleteManager.execWithoutLogicDelete(() -> bugFollowerMapper.deleteByQuery(bugFollowerQueryChain));
    }
}
