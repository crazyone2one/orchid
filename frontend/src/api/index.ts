import {createAlova} from "alova";
import {createServerTokenAuthentication} from "alova/client";
import fetchAdapter from "alova/fetch";
import vueHook from "alova/vue";
import {fetchRefreshToken} from "/@/api/modules/login";
import useUser from "/@/hooks/use-user.ts";
import {useI18n} from "/@/hooks/use-i18n.ts";
import {ContentTypeEnum} from "/@/enums/http-enum.ts";
import {useAppStore} from "/@/store";

const {onAuthRequired, onResponseRefreshToken} =
    createServerTokenAuthentication({
        refreshTokenOnSuccess: {
            // 响应时触发，可获取到response和method，并返回boolean表示token是否过期
            // 当服务端返回401时，表示token过期
            isExpired: (response, _method) => {
                return response.status === 401;
            },

            // 当token过期时触发，在此函数中触发刷新token
            handler: async (_response, _method) => {
                try {
                    const {
                        access_token
                    } = await fetchRefreshToken({refreshToken: localStorage.getItem("refresh_token") || ""});
                    localStorage.setItem("access_token", access_token);
                    // localStorage.setItem("refresh_token", refresh_token);
                } catch (error) {
                    // token刷新失败，跳转回登录页
                    // location.href = "/login";
                    const {logout} = useUser();
                    logout()
                    // 并抛出错误
                    throw error;
                }
            },
        },

        assignToken: (method) => {
            if (!method.meta?.ignoreToken) {
                method.config.headers.Authorization = `Bearer ${localStorage.getItem("access_token")}`;
            }
        },
    });
export const alovaInstance = createAlova({
    baseURL: import.meta.env.VITE_APP_BASE_API,
    timeout: 300 * 1000,
    requestAdapter: fetchAdapter(),
    statesHook: vueHook,
    cacheLogger: process.env.NODE_ENV === 'development',
    beforeRequest: onAuthRequired((method) => {
        const appStore = useAppStore()
        method.config.headers["Content-Type"] = ContentTypeEnum.JSON;
        method.config.headers["ORGANIZATION"] = appStore.currentOrgId;
        method.config.headers["PROJECT"] = appStore.currentProjectId;
    }),
    responded: onResponseRefreshToken({
        // 请求成功的拦截器
        // 当使用 `alova/fetch` 请求适配器时，第一个参数接收Response对象
        // 第二个参数为当前请求的method实例，你可以用它同步请求前后的配置信息
        onSuccess: async (response, method) => {
            const {t} = useI18n();
            const res = (response.json && (await response.json())) || response.body;
            const {message, code, data} = res;
            if (response.status >= 400) {
                switch (response.status) {
                    case 400:
                        window.$message.error(message)
                        break;
                    case 401:
                        window.$message.error(message || t('api.errMsg401'))
                        break;
                    case 403:
                        window.$message.error(t('api.errMsg403'))
                        break;
                    case 404:
                        window.$message.error(message || t('api.errMsg404'))
                        break;
                    case 500:
                        window.$message.error(t('api.errMsg500'))
                        break;
                    case 501:
                        window.$message.error(message || t('api.errMsg501'))
                        break;
                    default:
                }
                // throw new Error(response.statusText);
            }
            // const json = await response.json();
            if (code !== 100200) {
                // 抛出错误或返回reject状态的Promise实例时，此请求将抛出错误
                throw new Error(message);
            }
            return method.meta?.isDownload ? response.blob() : data;
        },
        onError: (err, method) => {
            const tip = `[${method.type}] - [${method.url}] - ${err.message}`;
            console.log("🍞 err", "color:#465975", tip);
            window.$message.error(tip);
        },
        // onComplete: (method) => {
        //     console.log("🍻 method", "color:#465975", method);
        //     // 处理请求完成逻辑
        // },
    }),
});

