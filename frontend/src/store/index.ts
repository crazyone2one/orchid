import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

const pinia = createPinia().use(piniaPluginPersistedstate);

// export { useAppStore, useMinderStore, useTableStore, useUserStore, useVisitStore };
export default pinia;