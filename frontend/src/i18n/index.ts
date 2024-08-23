import type {I18nOptions} from 'vue-i18n';
import {createI18n} from 'vue-i18n';

import {setLoadLocalePool} from './helper';
import {App} from "vue";

export const LOCALE_OPTIONS = [
    {label: '中文', value: 'zh-CN'},
    {label: 'English', value: 'en-US'},
];
export let i18n: ReturnType<typeof createI18n>;
const createI18nOptions = async (): Promise<I18nOptions> => {
    const locale = (localStorage.getItem('o-locale') || 'zh-CN') as LocaleType;
    const defaultLocal = await import(`./${locale}/index.ts`);
    const message = defaultLocal.default?.message ?? {};
    setLoadLocalePool((loadLocalePool) => {
        loadLocalePool.push(locale);
    });
    return {
        locale,
        fallbackLocale: 'zh-CN',
        legacy: false,
        allowComposition: true,
        messages: {
            [locale]: message,
        },
        sync: true, // If you don’t want to inherit locale from global scope, you need to set sync of i18n component option to false.
        silentTranslationWarn: true, // true - warning off
        missingWarn: false,
        silentFallbackWarn: true,
    };
}

export async function setupI18n(app: App) {
    const options = await createI18nOptions();

    i18n = createI18n(options);
    app.use(i18n);
}