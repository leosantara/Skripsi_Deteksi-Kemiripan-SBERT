import { Layout, Login } from '@/views/account';
import SignUpPage from '../views/account/SignUpPage.vue';

export default {
    path: '/account',
    component: Layout,
    children: [
        { path: '', redirect: 'login' },
        { path: 'login', component: Login },
        { path: 'signup', component: SignUpPage}
    ]
};
