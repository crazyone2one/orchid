// @ts-ignore @typescript-eslint/no-duplicate-enum-values
export enum StatusType {
  UN_REVIEWED = 'i-carbon-subtract-filled', // 未评审
  UNDER_REVIEWED = 'icon-icon_testing', // 评审中
  SUCCESS = 'icon-icon_succeed_colorful', // 成功
  PASS = 'icon-icon_succeed_colorful', // 已通过
  UN_PASS = 'icon-icon_close_colorful', // 未通过
  RE_REVIEWED = 'icon-icon_resubmit_filled', // 重新提审
  PASSED = 'icon-icon_succeed_colorful', // 已执行
  ERROR = 'icon-icon_close_colorful', // 失败
  BLOCKED = 'i-carbon-subtract-filled', // 阻塞
  PENDING = 'i-carbon-subtract-filled', // 未执行
}

// 评审，UNDER_REVIEWED：建议，PASS：通过，UN_PASS：未通过
export enum StartReviewStatus {
  PASS = 'PASS',
  UN_PASS = 'UN_PASS',
  UNDER_REVIEWED = 'UNDER_REVIEWED',
}

export enum LastExecuteResults {
  PENDING = 'PENDING',
  SUCCESS = 'SUCCESS',
  BLOCKED = 'BLOCKED',
  ERROR = 'ERROR',
}

export enum CaseLinkEnum {
  API = 'API',
  SCENARIO = 'SCENARIO',
  UI = 'UI',
  PERFORMANCE = 'PERFORMANCE',
  FUNCTIONAL = 'FUNCTIONAL',
}

export default {};
