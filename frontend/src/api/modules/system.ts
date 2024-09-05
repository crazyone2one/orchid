import {alovaInstance} from "/@/api";
import {userHasProjectPermissionUrl} from "/@/api/req-urls/system.ts";

export const getUserHasProjectPermission = (userId: string) => {
    return alovaInstance.Get(`${userHasProjectPermissionUrl}/${userId}`);
};