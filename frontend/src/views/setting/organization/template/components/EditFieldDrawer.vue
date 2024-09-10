<script setup lang="ts">
import ODrawer from '/@/components/o-drawer/index.vue';
import {computed, onMounted, Ref, ref, watch} from "vue";
import {
  AddOrUpdateField,
  DefinedFieldItem,
  fieldIconAndNameModal,
  FieldOptions,
  FormItemType
} from "/@/models/setting/template.ts";
import {useI18n} from "vue-i18n";
import type {FormInst} from "naive-ui";
import {useRoute} from "vue-router";
import BatchForm from '/@/components/o-batch-form/index.vue'
import {FormItemModel} from "/@/components/o-batch-form/types.ts";
import {useForm} from "alova/client";
import {cloneDeep} from "lodash-es";
import {useAppStore} from "/@/store";
import {
  dateOptions,
  fieldIconAndName,
  getFieldRequestApi,
  numberTypeOptions
} from "/@/views/setting/organization/template/components/field-setting.ts";
import {getGenerateId} from "/@/utils";


const {t} = useI18n();
const props = defineProps<{
  visible: boolean;
  mode: 'organization' | 'project';
  data: DefinedFieldItem[];
}>();
const emit = defineEmits(['success', 'update:visible']);
const route = useRoute();
const appStore = useAppStore();
const sceneType = route.query.type;
const formRef = ref<FormInst | null>(null);
const batchFormRef = ref<InstanceType<typeof BatchForm>>();
const optionsModels: Ref<FormItemModel[]> = ref([]);
const fieldOptions = ref<fieldIconAndNameModal[]>([]);
const rules = {
  name: {required: true, message: t('system.orgTemplate.fieldNameRules')},
  type: {required: true, message: t('system.orgTemplate.typeEmptyTip')},
  optionsModels: {message: t('system.orgTemplate.optionContentRules')}
}

const showDrawer = ref<boolean>(false);

const isEdit = ref<boolean>(false);

const handleDrawerCancel = () => {
  showDrawer.value = false;
}
const currentOrgId = computed(() => appStore.currentOrgId);
const currentProjectId = computed(() => appStore.currentProjectId);
const scopeId = computed(() => {
  return props.mode === 'organization' ? currentOrgId.value : currentProjectId.value;
});
const initFieldForm: AddOrUpdateField = {
  name: '',
  used: false,
  type: undefined,
  remark: '',
  scopeId: scopeId.value,
  scene: 'FUNCTIONAL',
  options: [],
  enableOptionKey: false,
};
const selectNumber = ref<FormItemType>('INT'); // 数字格式
const selectFormat = ref<FormItemType>(); // 选择格式
const isMultipleSelectMember = ref<boolean | undefined>(false); // 成员多选
// 是否展示选项添加面板
const showOptionsSelect = computed(() => {
  const showOptionsType: FormItemType[] = ['RADIO', 'CHECKBOX', 'SELECT', 'MULTIPLE_SELECT'];
  return showOptionsType.includes(form.value.type as FormItemType);
});
const {addOrUpdate, detail} = getFieldRequestApi(props.mode);
const {
  loading: submitting,
  form,
  send: addOrUpdateField,
  reset: resetForm
} = useForm(
    formData => {
      const formCopy: AddOrUpdateField = cloneDeep(formData) as AddOrUpdateField;
      formCopy.scene = route.query.type;
      formCopy.scopeId = scopeId.value;

      // 如果选择是日期
      if (selectFormat.value) {
        formCopy.type = selectFormat.value;
      }

      // 如果选择是成员（单选||多选）
      if (isMultipleSelectMember.value) {
        formCopy.type = isMultipleSelectMember.value ? 'MULTIPLE_MEMBER' : 'MEMBER';
      }

      // 如果选择是数值
      if (formCopy.type === 'NUMBER') {
        formCopy.type = selectNumber.value;
      }
      // 处理参数
      const {id, name, options, scene, type, remark, enableOptionKey} = formCopy;
      const params: AddOrUpdateField = {
        name,
        used: false,
        options,
        scopeId: scopeId.value,
        scene,
        type,
        remark,
        enableOptionKey,
      };
      if (id) {
        params.id = id;
      }
      return addOrUpdate(params);
    },
    {
      // 初始化表单数据
      initialForm: cloneDeep(initFieldForm),
      resetAfterSubmiting: true
    }
);
const handleDrawerConfirm = () => {
  userFormFiledValidate(confirmHandler);
}
const confirmHandler = (isContinue = false) => {
  addOrUpdateField().then(res => {
    window.$message.success(isEdit.value ? t('common.updateSuccess') : t('common.newSuccess'));
    if (!isContinue) {
      handleDrawerCancel();
    }
    resetForm();
    emit('success', isEdit.value, res.id);
  })
}
const fieldDefaultValues = ref<FormItemModel[]>([]);
const userFormFiledValidate = (cb: () => void) => {
  formRef.value?.validate(async (errors) => {
    if (errors) {
      return
    }
    console.log('userFormFiledValidate')
    if (showOptionsSelect.value) {
      batchFormRef.value?.formValidate(async (list: any) => {
        try {
          fieldDefaultValues.value = [...list];
          if (showOptionsSelect.value) {
            let startPos = 1;
            form.value.options = (batchFormRef.value?.getFormResult() || []).map((item: any) => {
              const currentItem: FieldOptions = {
                text: item.text,
                value: item.value ? item.value : getGenerateId(),
                pos: startPos,
              };
              if (item.fieldId) {
                currentItem.fieldId = item.fieldId;
              }
              startPos += 1;
              return currentItem;
            })
          }
          await cb();
        } catch (error) {
          console.log(error);
        }
      })
    } else {
      await cb();
    }
  })
}
watch(
    () => showDrawer.value,
    (val) => {
      emit('update:visible', val);
    }
);

watch(
    () => props.visible,
    (val) => {
      showDrawer.value = val;
    }
);
onMounted(() => {
  const excludeOptions = ['MULTIPLE_MEMBER', 'DATETIME', 'SYSTEM', 'INT', 'FLOAT'];
  fieldOptions.value = fieldIconAndName.filter((item: any) => excludeOptions.indexOf(item.key) < 0);
});
</script>

<template>
  <o-drawer v-model:visible="showDrawer" :width="800"
            :title="isEdit ? t('system.orgTemplate.update') : t('system.orgTemplate.addField')"
            :show-continue="!isEdit && data.length < 20"
            :ok-disabled="data.length >= 20 && !isEdit"
            :ok-loading="submitting"
            @cancel="handleDrawerCancel"
            @confirm="handleDrawerConfirm">
    <div class="form">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left"
              label-width="auto"
              require-mark-placement="right-hanging">
        <n-form-item :label="t('system.orgTemplate.fieldName')" path="name">
          <n-input v-model:value="form.name" :placeholder="t('system.orgTemplate.fieldNamePlaceholder')"
                   :maxlength="255"/>
        </n-form-item>
        <n-form-item :label="t('common.desc')" path="remark">
          <n-input v-model:value="form.remark" type="textarea" :placeholder="t('system.orgTemplate.resDescription')"
                   :maxlength="1000"/>
        </n-form-item>
        <n-form-item :label="t('system.orgTemplate.fieldType')" path="type">
          <n-select v-model:value="form.type" :options="fieldOptions"
                    :placeholder="t('system.orgTemplate.fieldTypePlaceholder')"
                    clearable :disabled="isEdit" class="w-[260px]"/>
        </n-form-item>
        <n-form-item v-if="form.type === 'MEMBER'" :label="t('system.orgTemplate.allowMultiMember')" path="type">
          <n-switch v-model:value="isMultipleSelectMember" size="small" :disabled="isEdit"/>
        </n-form-item>
        <n-form-item v-if="showOptionsSelect" :label="t('system.orgTemplate.optionContent')" path="optionsModels"
                     class="relative"
                     :class="[!form?.enableOptionKey ? 'max-w-[340px]' : 'w-full']">
          <div v-if="sceneType === 'BUG'">
            <n-checkbox>
              {{ t('system.orgTemplate.optionKeyValue') }}
            </n-checkbox>
          </div>
          <batch-form ref="batchFormRef" :models="optionsModels" form-mode="create"
                      add-text="system.orgTemplate.addOptions" :is-show-drag="true"/>
        </n-form-item>
        <n-form-item v-if="form.type === 'NUMBER'" :label="t('system.orgTemplate.numberFormat')" path="selectNumber">
          <n-select v-model:value="selectNumber" :options="numberTypeOptions"
                    :default-value="numberTypeOptions[0].value"
                    :placeholder="t('system.orgTemplate.formatPlaceholder')" clearable :disabled="isEdit"
                    class="w-[260px]"/>
        </n-form-item>
        <n-form-item v-if="form.type === 'DATE'" :label="t('system.orgTemplate.dateFormat')" path="selectFormat">
          <n-select v-model:value="selectFormat" :options="dateOptions" :default-value="dateOptions[0].value"
                    :placeholder="t('system.orgTemplate.formatPlaceholder')" clearable :disabled="isEdit"
                    class="w-[260px]"/>
        </n-form-item>
      </n-form>
    </div>
  </o-drawer>
</template>

<style scoped>

</style>