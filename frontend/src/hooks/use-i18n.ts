import {i18n} from "../i18n";

type I18nGlobalTranslation = {
    (key: string): string;
    (key: string, locale: string): string;
    (key: string, locale: string, list: unknown[]): string;
    (key: string, locale: string, named: Record<string, unknown>): string;
    (key: string, list: unknown[]): string;
    (key: string, named: Record<string, unknown>): string;
};

type I18nTranslationRestParameters = [string, any];

export function useI18n(namespace?: string): {
    t: I18nGlobalTranslation;
} {
    const normalFn = {
        t: (key: string) => {
            return key;
        },
    };
    if (!i18n) {
        return normalFn;
    }
    const {t, ...methods} = i18n.global;
    const tFn: I18nGlobalTranslation = (key: string, ...arg: any[]) => {
        if (!key) return '';
        if (!key.includes('.') && !namespace) return key;
        // @ts-ignore
        return t(key, ...(arg as I18nTranslationRestParameters));
    };
    return {
        ...methods,
        t: tFn,
    };
}

export const t = (key: string) => key;