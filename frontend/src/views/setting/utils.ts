import {useI18n} from "/@/hooks/use-i18n.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {h} from "vue";
import {NButton} from "naive-ui";


const {t} = useI18n();
export const showUpdateOrCreateMessage = (isEdit: boolean, id: string, organizationId?: string) => {
    if (isEdit) {
        window.$message.success(t('system.project.updateProjectSuccess'))
    } else if (!hasAnyPermission(['PROJECT_BASE_INFO:READ'])) {
        window.$message.success(t('system.project.createProjectSuccess'));
    } else {
        window.$message.success('', {
            render: () => {
                h('div', {class: 'flex items-center gap-[12px]'}, [
                    h('div', t('system.project.createProjectSuccess')),
                    h(NButton, {
                        text: true,
                        onClick() {
                            enterProject(id, organizationId);
                        }
                    }, {default: () => t('system.project.enterProject')})
                ])
            }
        })
    }
}
export const enterProject = (projectId: string, organizationId?: string) => {
    window.$message.info(`(${projectId}-${organizationId})todo...`)
}