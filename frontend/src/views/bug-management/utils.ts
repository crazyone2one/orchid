// 设置成员默认值
import {DetailCustomField, FieldOptions} from "/@/models/setting/template.ts";
import {useUserStore} from "/@/store";

const userStore = useUserStore();

export function getDefaultMemberValue(item: DetailCustomField, initOptions: FieldOptions[]) {
    if ((item.defaultValue as string | string[]).includes('CREATE_USER')) {
        const optionsIds = initOptions.map((e: any) => e.value);
        const userId = userStore.id as string;
        if (optionsIds.includes(userId)) {
            item.defaultValue = item.type === 'MEMBER' ? userId : [userId];
        } else {
            item.defaultValue = item.type === 'MEMBER' ? '' : [];
        }
    }
    return item.defaultValue;
}
