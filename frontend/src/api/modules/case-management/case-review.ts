import {alovaInstance} from "/@/api";
import {
    CopyReviewParams,
    Review, ReviewCaseItem, ReviewDetailCaseListQueryParams,
    ReviewDetailReviewersItem, ReviewerAndStatus, ReviewHistoryItem,
    ReviewItem,
    ReviewListQueryParams,
    ReviewModule,
    ReviewModuleItem,
    ReviewUserItem,
    UpdateReviewModuleParams,
    UpdateReviewParams
} from "/@/models/case-management/case-review.ts";
import {
    AddReviewModuleUrl,
    AddReviewUrl,
    CopyReviewUrl,
    EditReviewUrl,
    GetCaseReviewHistoryListUrl,
    GetReviewDetailCasePageUrl,
    GetReviewDetailModuleCountUrl,
    GetReviewDetailModuleTreeUrl,
    GetReviewDetailUrl, GetReviewerAndStatusUrl,
    GetReviewListUrl,
    GetReviewModulesUrl,
    GetReviewUsersUrl,
    ReviewModuleCountUrl,
    UpdateReviewModuleUrl
} from "/@/api/req-urls/case-management/case-review.ts";
import {CommonPage} from "/@/models/common.ts";
import {CaseManagementTable, CustomAttributes} from "/@/models/case-management/feature-case.ts";
import dayjs from "dayjs";
import {getCaseLevels} from "/@/views/case-management/case-management-feature/components/utils.ts";

/**
 * 新增评审模块
 * @param params
 */
export const addReviewModule = (params: ReviewModule) => alovaInstance.Post(AddReviewModuleUrl, params);
/**
 * 更新评审模块
 * @param params
 */
export const updateReviewModule = (params: UpdateReviewModuleParams) => alovaInstance.Post(UpdateReviewModuleUrl, params);
/**
 * 获取评审模块树
 * @param projectId
 */
export const getReviewModules = (projectId: string) => alovaInstance.Get<ReviewModuleItem[]>(`${GetReviewModulesUrl}/${projectId}`);
export const getReviewDetail = (id: string) => alovaInstance.Get<ReviewItem>(`${GetReviewDetailUrl}/${id}`);
/**
 * 评审详情-已关联用例模块树
 * @param id
 */
export const getReviewDetailModuleTree = (id: string) => alovaInstance.Get(`${GetReviewDetailModuleTreeUrl}/${id}`);
/**
 * 评审详情-获取用例评审历史
 * @param reviewId
 * @param caseId
 */
export const getCaseReviewHistoryList = (reviewId: string, caseId: string) =>
    alovaInstance.Get<ReviewHistoryItem[]>(`${GetCaseReviewHistoryListUrl}/${reviewId}/${caseId}`);
/**
 * 评审详情-模块下用例数量统计
 * @param param
 */
export const getReviewDetailModuleCount = (param: ReviewDetailCaseListQueryParams) => alovaInstance.Post<Record<string, any>>(GetReviewDetailModuleCountUrl, param);
/**
 * 评审详情-获取用例列表
 * @param param
 */
export const getReviewDetailCasePage = (param: ReviewDetailCaseListQueryParams) =>
    alovaInstance.Post<CommonPage<ReviewCaseItem>>(GetReviewDetailCasePageUrl, param,{
        transform(data: any, _headers) {
            return data.records.map((item: CaseManagementTable) => ({
                ...item,
                caseLevel: getCaseLevels(item.customFields as unknown as CustomAttributes[]),
            }));
        }
    })
/**
 * 获取评审人员列表
 * @param projectId
 * @param keyword
 */
export const getReviewUsers = (projectId: string, keyword: string) =>
    alovaInstance.Get<ReviewUserItem[]>(`${GetReviewUsersUrl}/${projectId}`, {params: {keyword}});
export const addReview = (params: Review) => alovaInstance.Post<ReviewItem>(AddReviewUrl, params);
export const copyReview = (params: CopyReviewParams) => alovaInstance.Post<ReviewItem>(CopyReviewUrl, params);
export const editReview = (params: UpdateReviewParams) => alovaInstance.Post<ReviewItem>(EditReviewUrl, params);
/**
 * 评审模块树-统计用例数量
 * @param params
 */
export const reviewModuleCount = (params: ReviewListQueryParams) => alovaInstance.Post<Record<string, number>>(ReviewModuleCountUrl, params);
/**
 * 获取评审列表
 * @param params
 */
export const getReviewList = (params: ReviewListQueryParams) => alovaInstance.Post<CommonPage<ReviewItem>>(GetReviewListUrl, params, {
    transform(data: any, _headers) {
        return data.records.map((item: CaseManagementTable) => ({
            ...item,
            tags: (item.tags || []).map((item: string, i: number) => {
                return {
                    id: `${item}-${i}`,
                    name: item,
                };
            }),
            reviewers: item.reviewers.map((e: ReviewDetailReviewersItem) => e.userName),
            cycle:
                item.startTime && item.endTime
                    ? `${dayjs(item.startTime).format('YYYY-MM-DD HH:mm:ss')} - ${dayjs(item.endTime).format(
                        'YYYY-MM-DD HH:mm:ss'
                    )}`
                    : ''
        }));
    }
});
/**
 *  脑图-获取用例评审最终结果和每个评审人最终的评审结果
 * @param reviewId
 * @param caseId
 */
export const getReviewerAndStatus = (reviewId: string, caseId: string) =>
    alovaInstance.Get<ReviewerAndStatus>(`${GetReviewerAndStatusUrl}/${reviewId}/${caseId}`);