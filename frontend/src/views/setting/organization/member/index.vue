<script setup lang="ts">
import OCard from '/@/components/o-card/index.vue'
import {useI18n} from "vue-i18n";
import {useAppStore} from "/@/store";
import {computed, h, onBeforeMount, ref, resolveDirective, withDirectives} from "vue";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {TableQueryParams} from "/@/models/common.ts";
import {usePagination, useRequest} from "alova/client";
import {getGlobalUserGroup, getMemberList, getProjectList} from "/@/api/modules/setting/member.ts";
import {DataTableColumns, DataTableRowKey, NButton} from "naive-ui";
import {AddOrUpdateMemberModel, LinkList, MemberItem} from "/@/models/setting/member.ts";
import Pagination from "/@/components/o-pagination/index.vue";
import TagGroup from '/@/components/o-tag-group/index.vue'
import RemoveButton from '/@/components/o-remove-button/index.vue'
import {characterLimit} from "/@/utils";
import AddMemberModal from "/@/views/setting/organization/member/components/AddMemberModal.vue";

const appStore = useAppStore();
const {t} = useI18n();
const reqParam = ref<TableQueryParams>({
  current: 1,
  pageSize: 10,
  keyword: ''
})
const lastOrganizationId = computed(() => appStore.currentOrgId);

const hasOrdMemberOperationPermission = computed(() =>
    hasAnyPermission(['ORGANIZATION_MEMBER:READ+UPDATE', 'ORGANIZATION_MEMBER:READ+DELETE'])
);
const checkedRowKeys = ref<DataTableRowKey[]>([]);
const handleCheck = (rowKeys: DataTableRowKey[]) => checkedRowKeys.value = rowKeys;
const permission = resolveDirective('permission')
const columns: DataTableColumns<MemberItem> = [
  {
    type: 'selection'
  },
  {
    title: t('system.user.userName'),
    key: 'email',
    ellipsis: {tooltip: true}, width: 200
  },
  {
    title: t('organization.member.tableColumnName'),
    key: 'name',
    ellipsis: {tooltip: true}, width: 300
  },
  {
    title: t('organization.member.tableColumnPhone'),
    key: 'phone',
    ellipsis: {tooltip: true}, width: 200
  },
  {
    title: t('organization.member.tableColumnPro'),
    key: 'projectIdNameMap',
    render: (record) => {
      if (!record.showProjectSelect) {
        return h(TagGroup, {tagList: record.projectIdNameMap || []})
      }
    }
  },
  {
    title: t('organization.member.tableColumnUserGroup'),
    key: 'userRoleIdNameMap',
    render: (record) => {
      if (!record.showUserSelect) {
        return h(TagGroup, {tagList: record.userRoleIdNameMap || [], type: 'primary'})
      }
    }
  },
  {
    title: t('organization.member.tableColumnStatus'),
    key: 'enable', width: 100,
    render: (record) => {
      if (record.enable) {
        return h('div', {class: 'flex items-center'}, {
          default: () => [
            h('div', {class: 'mr-[2px] i-carbon-checkmark-filled text-green-500'}, {}),
            h('div', {}, {default: () => t('organization.member.statusEnable')})
          ]
        })
      }
      return h('div', {}, {
        default: () => [
          // h('div', {class: 'mr-[2px] i-o-icons-disable'}, {}),
          h('div', {}, {default: () => t('organization.member.statusDisable')})
        ]
      })
    }
  },
  {
    title: hasOrdMemberOperationPermission.value ? t('organization.member.tableColumnActions') : '',
    key: 'operation', fixed: 'right',
    width: hasOrdMemberOperationPermission.value ? 140 : 50,
    render: (record) => {
      return [
        withDirectives(h(NButton, {
              text: true, type: 'primary',
              onClick: () => addOrEditMember('edit', record)
            }, {default: () => t('organization.member.edit')}),
            [[permission, ['ORGANIZATION_MEMBER:READ+UPDATE']]]),
        withDirectives(h(RemoveButton, {
                  title: t('organization.member.deleteMemberTip', {name: characterLimit(record.name)}),
                  subTitleTip: t('organization.member.subTitle')
                },
                {default: () => t('organization.member.edit')}),
            [[permission, ['ORGANIZATION_MEMBER:READ+DELETE']]])
      ]
    },
  },
]
const {send: fetchData, data, page, pageSize, total} = usePagination(() => {
  reqParam.value.organizationId = lastOrganizationId.value
  return getMemberList(reqParam.value)
}, {
  immediate: false,
  initialData: {
    total: 0,
    data: []
  },
  data: response => response.records,
  total: response => response.total
})
const handleSetPage = (param: number) => page.value = param
const handleSetPageSize = (param: number) => pageSize.value = param;
const addMemberVisible = ref<boolean>(false);
const AddMemberRef = ref();
const userGroupOptions = ref<LinkList>([]);
const projectOptions = ref<LinkList>([]);
const addOrEditMember = (type: string, record: AddOrUpdateMemberModel = {}) => {
  addMemberVisible.value = true;
  AddMemberRef.value.type = type;
  if (type === 'edit') {
    AddMemberRef.value.edit(record);
  }
}
const {send: fetchGlobalUserGroup} = useRequest((orgId) => getGlobalUserGroup(orgId), {
  immediate: false,
  force: true
})
const {send: fetchProjectList} = useRequest((orgId) => getProjectList(orgId), {
  immediate: false,
  force: true
})
const getLinkList = () => {
  if (lastOrganizationId.value) {
    fetchGlobalUserGroup(lastOrganizationId.value).then(res => userGroupOptions.value = res)
    if (hasAnyPermission(['ORGANIZATION_PROJECT:READ'])) {
      fetchProjectList(lastOrganizationId.value).then(res => projectOptions.value = res)
    }
  }
}
onBeforeMount(() => {
  fetchData();
  getLinkList()
})
</script>

<template>
  <o-card simple>
    <div class="mb-4 flex items-center justify-between">
      <n-button v-permission="['ORGANIZATION_MEMBER:READ+ADD']" type="primary" secondary class="mr-3"
                @click="addOrEditMember('add')">
        {{ t('organization.member.addMember') }}
      </n-button>
      <!--        <n-button v-permission="['ORGANIZATION_MEMBER:READ+INVITE']" secondary class="mr-3">-->
      <!--          {{ t('system.user.emailInvite') }}-->
      <!--        </n-button>-->
      <div>
        <n-input v-model:value="reqParam.keyword" :placeholder="t('organization.member.searchMember')" :maxlength="255"
                 clearable
                 class="w-[230px]"/>
      </div>
    </div>
    <n-data-table :bordered="false" :columns="columns" :data="data" :row-key="(tmp:MemberItem)=>tmp.id"
                  @update:checked-row-keys="handleCheck"/>
    <div class="mt-8">
      <pagination :page-size="pageSize" :page="page" :count="total"
                  @update-page="handleSetPage"
                  @update-page-size="handleSetPageSize"/>
    </div>
  </o-card>
  <add-member-modal ref="AddMemberRef" v-model:visible="addMemberVisible" :user-group-options="userGroupOptions"
                    @success="fetchData"/>
</template>

<style scoped>

</style>