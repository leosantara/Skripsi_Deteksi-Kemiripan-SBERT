import { createApp } from 'vue';
import { createPinia } from 'pinia';
import vue3GoogleLogin from 'vue3-google-login'

import App from './App.vue';
import { router } from './router';
import "../src/interceptor"
import { CkeditorPlugin } from '@ckeditor/ckeditor5-vue';

const app = createApp(App);

app.use(vue3GoogleLogin, {
    clientId: '118435807079-p3c6j2v8buos0emcst2olthij71krin6.apps.googleusercontent.com'
})

app.use(CkeditorPlugin)
app.use(createPinia());
app.use(router);

app.mount('#app');