import {createAlova} from "alova";
import {axiosRequestAdapter} from "@alova/adapter-axios";
import {AxiosError, AxiosRequestConfig} from "axios";
import vueHook from "alova/vue";

/**
 * axios请求配置参数
 * 去掉了与method冲突的属性
 */
export type AlovaAxiosRequestConfig = Omit<
    AxiosRequestConfig,
    | 'url'
    | 'method'
    | 'baseURL'
    | 'headers'
    | 'params'
    | 'data'
    | 'timeout'
    | 'cancelToken'
    | 'signal'
    | 'onUploadProgress'
    | 'onDownloadProgress'
>;
export const uploadInstance = createAlova({
    requestAdapter: axiosRequestAdapter(),
    baseURL: import.meta.env.VITE_APP_BASE_API,
    timeout: 300 * 1000,
    cacheLogger: process.env.NODE_ENV === 'development',
    statesHook: vueHook,
    async beforeRequest(method) {
        if (!method.meta?.ignoreToken) {
            method.config.headers.Authorization = `Bearer ${localStorage.getItem("access_token")}`;
        }
    },
    responded: {
        onSuccess(response) {
            // response自动被推断为AxiosResponse类型
            const {data, status} = response
            switch (status) {
                case 400:
                    window.$message.error(data.message)
                    break;
                default:

            }
            return data;
        },
        onError(err: AxiosError) {
            // err默认为any，你可以强制转换为AxiosError处理
            const {response} = err
            const {data, status} = response as any
            switch (status) {
                case 400:
                    window.$message.error(data.message)
                    break;
                default:

            }
            throw new Error(data.message);
        }
    }
})