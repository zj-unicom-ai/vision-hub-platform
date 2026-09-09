import { nextTick } from 'vue'
import type { Ref } from 'vue'
import type { FormInstance } from 'ant-design-vue'


export function clearFormValidate(formRef?: Ref<FormInstance | undefined>) {
  formRef?.value?.clearValidate()
}


export async function clearFormValidateNextTick(formRef?: Ref<FormInstance | undefined>) {
  await nextTick()
  clearFormValidate(formRef)
}


export function closeFormModal(
  visible: Ref<boolean>,
  formRef?: Ref<FormInstance | undefined>
) {
  clearFormValidate(formRef)
  visible.value = false
}
