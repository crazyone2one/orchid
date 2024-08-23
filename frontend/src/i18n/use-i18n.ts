import {ref, unref} from 'vue';
import dayjs from 'dayjs';
import {i18n} from '/@/i18n';
import {loadLocalePool} from '/@/i18n/helper';

interface LangModule {
    message: Recordable;
    dayjsLocale: Recordable;
    dayjsLocaleName: string;
}

const setI18nLanguage = (locale: LocaleType) => {
    if (i18n.mode === 'legacy') {
        i18n.global.locale = locale;
    } else {
        (i18n.global.locale as any).value = locale;
    }
    localStorage.setItem('o-locale', locale);
}
const changeLocale = async (locale: LocaleType) => {
    const globalI18n = i18n.global;
    const currentLocale = unref(globalI18n.locale);
    if (currentLocale === locale) {
        setI18nLanguage(locale); // 初始化的时候需要设置一次本地语言
        return locale;
    }
    if (loadLocalePool.includes(locale)) {
        setI18nLanguage(locale);
        return locale;
    }
    const langModule = ((await import(`./${locale}/index.ts`)) as any).default as LangModule;
    if (!langModule) return;

    const {message, dayjsLocale, dayjsLocaleName} = langModule;
    globalI18n.setLocaleMessage(locale, message);
    dayjs.locale(dayjsLocaleName, dayjsLocale);
    loadLocalePool.push(locale);

    setI18nLanguage(locale);
    window.location.reload();
    return locale;
}
export default function useLocale() {
    const {locale} = i18n.global;
    const currentLocale = ref(locale as LocaleType);

    return {
        currentLocale,
        changeLocale,
    };
}