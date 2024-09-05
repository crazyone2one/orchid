import {alovaInstance} from "/@/api";
import {LoginRes} from "/@/models/user.ts";

export const fetchLogin = (param: { username: string, password: string }) => {
    const method = alovaInstance.Post<LoginRes>('/auth/login', param);
    method.meta = {
        ignoreToken: true
    };
    return method;
};
export const fetchUserIsLogin = () => {
    return alovaInstance.Get<LoginRes>('/auth/info');
};
export const fetchRefreshToken = (param: { refreshToken: string }) => {
    const method = alovaInstance.Post<{ access_token: string, refresh_token: string }>('/auth/refreshToken', param);
    method.meta = {
        authRole: 'refreshToken'
    };
    return method;
};
export const fetchLogout = () => {
    const method = alovaInstance.Post('/auth/logout');
    method.meta = {
        authRole: 'logout'
    };
    return method
}