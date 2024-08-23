const ACCESS_TOKEN = 'access_token';
const REFRESH_TOKEN = 'refresh_token';
export const clearToken = () => {
    localStorage.removeItem(ACCESS_TOKEN);
    localStorage.removeItem(REFRESH_TOKEN);
};

export const setToken = (accessToken: string, refreshToken: string) => {
    localStorage.setItem(ACCESS_TOKEN, accessToken);
    localStorage.setItem(REFRESH_TOKEN, refreshToken);
};

export const hasToken = (_name?: string) => {
    // if (WHITE_LIST_NAME.includes(name)) {
    //     return true;
    // }
    return !!localStorage.getItem(ACCESS_TOKEN) && !!localStorage.getItem(REFRESH_TOKEN);
};