export const loadLocalePool: LocaleType[] = [];

export const setLoadLocalePool = (cb: (lp: LocaleType[]) => void) => {
    cb(loadLocalePool);
}
