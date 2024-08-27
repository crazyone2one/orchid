<script setup lang="ts">
import {nextTick, ref} from "vue";

interface Props {
  value: string,
  onUpdateValue: Function
}

const props = withDefaults(defineProps<Props>(), {
  value: '',
  onUpdateValue: () => []
})
const isEdit = ref(false)
const inputRef = ref<HTMLInputElement | null>(null)
const inputValue = ref(props.value)
const handleOnClick = () => {
  isEdit.value = true
  nextTick(() => {
    inputRef.value?.focus()
  })
}
const handleChange = () => {
  props.onUpdateValue(inputValue.value)
  isEdit.value = false
  inputValue.value = props.value
}
</script>

<template>
  <div class="shadow-md" @click="handleOnClick">
    <n-input v-if="isEdit" v-model:value="inputValue" ref="inputRef"
             :maxlength="255"
             @blur="handleChange"/>
    <span v-else>{{ props.value }}</span>
  </div>
</template>

<style scoped>

</style>