import {type NavigationGuardNext, onBeforeRouteLeave} from 'vue-router';

import {useI18n} from '/@/hooks/use-i18n.ts';
import {ref} from "vue";

export interface LeaveProps {
    leaveTitle: string;
    leaveContent: string;
    // tipType: ModalType;
}

const leaveProps: LeaveProps = {
    leaveTitle: 'common.unSaveLeaveTitle',
    leaveContent: 'common.unSaveLeaveContent',
    // tipType: 'warning',
};

export default function useLeaveUnSaveTip(leaveProp = leaveProps) {
    const {t} = useI18n();
    const {leaveTitle, leaveContent} = leaveProp;
    const isSave = ref(true);

    const setIsSave = (flag: boolean) => {
        isSave.value = flag;
    };
    const openUnsavedTip = (next: NavigationGuardNext | (() => void)) => {
        window.$dialog.warning({
            title: t(leaveTitle),
            content: t(leaveContent),
            positiveText: t('common.leave'),
            negativeText: t('common.stay'),
            onPositiveClick: () => {
                isSave.value = true;
                next();
            }
        })
    }
    onBeforeRouteLeave((to, from, next) => {
        if (to.path === from.path) {
            next();
            return;
        }

        if (!isSave.value) {
            openUnsavedTip(next);
        } else {
            next();
        }
    });
    // 页面有变更时，关闭或刷新页面弹出浏览器的保存提示
    window.onbeforeunload = () => {
        if (!isSave.value) {
            return '';
        }
    };
    return {
        setIsSave,
        openUnsavedTip,
        isSave,
    };
}
