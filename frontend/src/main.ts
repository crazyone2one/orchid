import "virtual:uno.css";
import {createApp} from "vue";
import App from "./App.vue";
import router from "./router";
import "./style.css";
import Loading from "/@/components/loading/loading.ts";
import pinia from "/@/store";
import {setupI18n} from "/@/i18n";
import naive from '/@/utils/naive.ts'
import permission from '/@/directive/permission/index.ts'
import outerClick from "/@/directive/outer-click";

const bootstrap = async () => {
    const app = createApp(App);
    app.use(router);
    app.use(Loading);
    app.use(pinia)
    app.use(naive)
    // 注册国际化，需要异步阻塞，确保语言包加载完毕
    await setupI18n(app);
    // 获取默认语言
    // const localLocale = localStorage.getItem('o-locale');
    // if (!localLocale) {
    //     const defaultLocale = await getDefaultLocale();
    //     const { changeLocale } = useLocale();
    //     changeLocale(defaultLocale);
    // }
    app.directive('permission', permission)
    app.directive('outer', outerClick)
    app.mount("#app");
}

bootstrap();