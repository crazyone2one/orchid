export default {
  bugManagement: {
    index: '缺陷',
    addBug: '创建缺陷',
    editBug: '更新缺陷',
    createBug: '创建缺陷',
    copyBug: '复制缺陷',
    syncBug: '同步缺陷',
    ID: 'ID',
    project: '项目',
    bugName: '缺陷名称',
    severity: '严重程度',
    status: '状态',
    handleMan: '处理人',
    handleManTips: '"-"表示该用户成员被移除或系统中不存在',
    numberOfCase: '用例数',
    belongPlatform: '所属平台',
    tag: '标签',
    creator: '创建人',
    updateUser: '更新人',
    createTime: '创建时间',
    updateTime: '更新时间',
    sync: '同步',
    synchronizing: '正在同步中',
    syncSuccess: '同步成功',
    syncBugTipRowOne: '根据<<项目管理-应用设置-缺陷管理>>中的配置来同步第三方平台缺陷至MS',
    bugAutoSync: '系统将按照项目应用设置频率自动同步',
    syncTime: '同步时间',
    deleteLabel: '删除后, Local 的缺陷进入回收站; 第三方平台同步的缺陷将不做回收',
    nameIsIncorrect: '缺陷名称不正确',
    selectedCount: '(已选 {count} 条缺陷)',
    batchEdit: '批量编辑',
    selectProps: '选择属性',
    exportBug: '导出缺陷',
    exportBugCount: '已选 {count} 条缺陷',
    deleteCount: '确认删除 {count} 个缺陷吗？',
    deleteTipInternal: '删除后，{count} 条 MeterSphere 创建的缺陷进入回收站',
    deleteTipExternal: '{count} 条第三方平台同步的缺陷将不做回收',
    deleteTip: '删除后, Local 的缺陷进入回收站; 第三方平台同步的缺陷将不做回收',
    edit: {
      defaultSystemTemplate: '默认为系统模板',
      content: '缺陷内容',
      file: '附件',
      fileExtra: '支持任意类型文件，单个文件大小不超过 50MB',
      pleaseInputBugName: '请输入缺陷名称',
      nameIsRequired: '缺陷名称不能为空',
      pleaseInputBugContent: '请输入缺陷内容',
      tagPlaceholder: '输入内容后回车可直接添加标签',
      handleManPlaceholder: '请选择处理人',
      handleManIsRequired: '处理人不能为空',
      statusPlaceholder: '请选择缺陷状态',
      statusIsRequired: '状态不能为空',
      severityPlaceholder: '请选择严重程度',
      uploadFile: '添加附件',
      localUpload: '本地上传',
      linkFile: '关联文件',
      contentEdit: '内容编辑',
      linkCase: '关联用例',
      cannotBeNull: '不能为空',
    },
    detail: {
      notExist: '缺陷不存在',
      title: '【{id}】{name}',
      apiCase: '接口用例',
      scenarioCase: '场景用例',
      uiCase: 'UI用例',
      performanceCase: '性能用例',
      isPlanRelateCase: '关联测试计划',
      isPlanRelateCaseTip1: '在测试计划内执行用例时新建或关联的缺陷, 只可在测试计划内取消关联关系;',
      isPlanRelateCaseTip2: '缺陷在测试计划内执行用例时关联/新建, 显示为“是”;',
      isPlanRelateCaseTip3: '缺陷在缺陷页/用例详情页添加直接关联关系, 未关联上任何测试计划, 显示为“否”;',
      searchCase: '通过名称搜索',
      creator: '创建人',
      createTime: '创建时间',
      basicInfo: '基本信息',
      handleUser: '处理人',
      tag: '标签',
      detail: '详情',
      case: '用例',
      changeHistory: '变更历史',
      comment: '评论',
      shareTip: '分享链接已复制到剪贴板',
      deleteTitle: '确认删除 {name} 吗？',
      deleteContent: '删除后, Local 的缺陷进入回收站; 第三方平台同步的缺陷将不做回收',
      platform_no_active: '该缺陷平台未对接, 无法正常预览及编辑详情相关内容',
    },
    batchUpdate: {
      attribute: '选择属性',
      update: '批量更新为',
      updatePlaceholder: '请选择更新后的选项',
      appendLabel: '追加标签',
      systemFiled: '系统字段',
      customFiled: '自定义字段',
      append: '追加',
      openAppend: '开启: 追加标签',
      closeAppend: '关闭: 覆盖原有标签',
      handleUser: '处理人',
      tag: '标签',
      required: {
        attribute: '属性不能为空',
        value: '值不能为空',
      },
    },
    success: {
      countDownTip: '后回到缺陷列表，也可以手动回到缺陷列表',
      bugDetail: '缺陷详情',
      addContinueCreate: '继续创建',
      backBugList: '返回缺陷列表',
      notNextTip: '下次不再提醒',
      mightWantTo: '你可能还想',
      caseRelated: '关联用例',
    },
    recycle: {
      recycleBin: '回收站',
      recover: '恢复',
      recovering: '正在恢复中',
      recoverSuccess: '恢复成功',
      recoverError: '恢复失败',
      permanentlyDelete: '彻底删除',
      permanentlyDeleteTip: '是否彻底删除 {name} 缺陷?',
      deleteContent: '删除后，缺陷无法恢复，请谨慎操作！',
      batchDelete: '是否彻底删除{count}条缺陷?',
      searchPlaceholder: '通过 ID/名称搜索',
      deleteTime: '删除时间',
      deleteMan: '删除人',
    },
    history: {
      changeNumber: '变更序号',
      operationMan: '操作人',
      operation: '操作',
      updateTime: '更新时间',
      restore: '恢复',
      current: '当前',
    },
  },
};