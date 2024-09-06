<script setup lang="ts">
import initOptionsFunc, {UserRequestTypeEnum} from "/@/components/o-user-selector/utils.ts";
import {onBeforeMount, ref, watch} from "vue";
import type {SelectOption} from "naive-ui";
import {UserListItem} from "/@/models/setting/user.ts";

export interface MsUserSelectorOption {
  id: string;
  name: string;
  email: string;
  disabled?: boolean;

  [key: string]: string | number | boolean | undefined;
}

interface ILoadOptionParams {
  [key: string]: string | number | boolean | undefined;
}

interface IProps {
  mode?: 'static' | 'remote'; // 静态模式，远程模式。默认为静态模式，需要传入 options 数据；远程模式需要传入请求函数
  type?: UserRequestTypeEnum; // 加载选项的类型
  placeholder?: string;
  disabledKey?: string; // 禁用的key
  loadOptionParams?: ILoadOptionParams; // 禁用的key
  valueKey?: string; // value的key
  firstLabelKey?: string; // 首要的的字段key
  secondLabelKey?: string; // 次要的字段key
}

const props = withDefaults(defineProps<IProps>(), {
  mode: 'static',
  type: UserRequestTypeEnum.SYSTEM_USER_GROUP,
  disabledKey: 'exclude',
  valueKey: 'id',
  firstLabelKey: 'name',
  secondLabelKey: 'email',
})
const currentValue = defineModel<(string | number)[]>('modelValue', {default: []});
const innerValue = ref<string[]>([]);
const options = ref<Array<SelectOption>>([])
const loadList = async () => {
  options.value = []
  const list = (await initOptionsFunc(props.type, props.loadOptionParams!)) || [];
  if (list) {
    list.map(item => options.value.push(
        {
          label: `${item.name}(${item.email})`,
          value: item.id,
          disabled: item[props.disabledKey]
        }
    ))
  }
  if (currentValue.value.length > 0 && innerValue.value.length === 0) {
    const values: UserListItem[] = [];
    currentValue.value.forEach((item) => {
      const option = list.find((o) => o.id === item);
      if (option) {
        values.push(option);
      }
    });
    innerValue.value = values.map((item) => item.id);
  }
}
onBeforeMount(() => {
  if (props.mode === 'remote') {
    loadList()
  }
})
watch(() => innerValue.value, (value) => {
  currentValue.value = value;
})
</script>

<template>
  <n-select v-model:value="innerValue" :options="options" multiple filterable
            :placeholder="$t(props.placeholder || 'common.pleaseSelectMember')"/>
</template>

<style scoped>

</style>