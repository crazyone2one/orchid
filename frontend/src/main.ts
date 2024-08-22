import "virtual:uno.css";
import {createApp} from "vue";
import App from "./App.vue";
import router from "./router";
import "./style.css";
import Loading from "/@/components/loading/loading.ts";
import pinia from "/@/store";

const app = createApp(App);
app.use(router);
app.use(Loading);
app.use(pinia)
app.mount("#app");
