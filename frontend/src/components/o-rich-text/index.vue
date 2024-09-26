<script setup lang="ts">
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import {onBeforeUnmount, ref, shallowRef} from 'vue'
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'
import {
  IDomEditor,
  IEditorConfig,
  IToolbarConfig, DomEditor
} from "@wangeditor/editor"
import useLocale from "/@/i18n/use-i18n.ts";
import {useI18n} from "vue-i18n";

const props = withDefaults(
    defineProps<{
      // content?: string;
      uploadImage?: (file: File) => Promise<any>;
      maxHeight?: string;
      autoHeight?: boolean;
      filedIds?: string[];
      commentIds?: string[];
      wrapperClass?: string;
      placeholder?: string;
      draggable?: boolean;
      previewUrl?: string;
      editable?: boolean;
      limitLength?: number;
    }>(),
    {
      // content: '',
      uploadImage: undefined,
      placeholder: 'editor.placeholder',
      draggable: false,
      autoHeight: true,
      editable: true,
    }
);
const raw = defineModel('raw')
const {currentLocale} = useLocale();

const emit = defineEmits<{
  (event: 'update:raw', value: string): void;
  (event: 'update:filedIds', value: string[]): void;
  (event: 'update', value: string): void;
  (event: 'update:commentIds', value: string): void;
  (event: 'blur', eveValue: FocusEvent): void;
  (event: 'focus', eveValue: FocusEvent): void;
}>();
const {t} = useI18n()
// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef<IDomEditor>()
// 内容 HTML
const valueHtml = ref(raw)
const toolbarConfig: Partial<IToolbarConfig> = {}
const editorConfig: Partial<IEditorConfig> = {placeholder: t(props.placeholder)}
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})
const handleCreated = (editor: IDomEditor) => {
  editorRef.value = editor // 记录 editor 实例，重要！
  const curToolbarConfig = DomEditor.getToolbar(editorRef.value)?.getConfig()
  console.log('curToolbarConfig', curToolbarConfig)
}
defineExpose({});
</script>

<template>
  <div class="rich-wrapper w-full grid grid-cols-1 gap-4">
    <Toolbar
        style="border-bottom: 1px solid #ccc"
        :editor="editorRef"
        :defaultConfig="toolbarConfig"
        mode="default"
    />
    <Editor
        style="height: 300px; overflow-y: hidden;"
        v-model="valueHtml"
        :defaultConfig="editorConfig"
        mode="default"
        @onCreated="handleCreated"
    />
  </div>
</template>

<style scoped>
.rich-wrapper {
  @apply relative overflow-hidden;

  :deep(.w-e-text-container) {
    padding: 2px 16px 16px !important;

    > p:first-child {
      margin-top: 0;
    }
  }
}
</style>