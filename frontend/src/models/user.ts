// 登录信息
import {UserState} from "/@/store/modules/user/types.ts";

export interface LoginData {
    username: string;
    password: string;
    authenticate?: string;
}
export interface LoginRes extends UserState {
    access_token: string;
    refresh_token: string;
}