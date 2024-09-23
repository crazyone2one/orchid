import {alovaInstance} from "/@/api";
import {
    CopyReviewParams,
    Review,
    ReviewDetailReviewersItem,
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
    GetReviewDetailUrl,
    GetReviewListUrl,
    GetReviewModulesUrl,
    GetReviewUsersUrl,
    UpdateReviewModuleUrl
} from "/@/api/req-urls/case-management/case-review.ts";
import {CommonPage} from "/@/models/common.ts";
import {CaseManagementTable} from "/@/models/case-management/feature-case.ts";
import dayjs from "dayjs";

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