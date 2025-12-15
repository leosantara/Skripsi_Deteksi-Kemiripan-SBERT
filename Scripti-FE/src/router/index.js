import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore, useAlertStore } from '@/stores';
import { Home } from '@/views';
import PageNotFound from '../views/PageNotFound.vue';
import accountRoutes from './account.routes';
import adminRoutes from './admin.routes';
import proposalRoutes from './proposal.routes'
import sidangDERoutes from './sidangDE.routes';
import dokumenRoute from './document.routes'

export const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    linkActiveClass: 'active',
    routes: [
        { path: '/', component: Home },
        { ...accountRoutes },
        { ...adminRoutes },
        { ...proposalRoutes},
        { ...sidangDERoutes},
        {...dokumenRoute},
        {path: '/page-not-found', component:PageNotFound},
        // catch all redirect to home page
        { path: '/:pathMatch(.*)*', redirect: '/' },
    ]
});

router.beforeEach(async (to) => {
    const alertStore = useAlertStore();
    const successAlertVisible = [
        '/account/login',
        '/adminpage/mahasiswa', 
        '/adminpage/dosen', 
        '/adminpage/periode-proposal', 
        '/proposalpage/adminpage/proposal-mahasiswa', 
        '/proposalpage/mahasiswapage/proposal-mahasiswa',
        '/deskevaluation/adminpage/proposal-mahasiswa/semua-proposal/'
    ]
    if(alertStore.alert){
        if(!alertStore.alert.type=='alert-success' || !successAlertVisible.includes(to.path)){
            alertStore.clear();
        }
    }

    // redirect to login page if user is not logged in and tried to access a restricted page 
    const publicPages = ['/account/login', '/account/signup'];
    const authRequired = !publicPages.includes(to.path);
    const authStore = useAuthStore();
    if (authRequired && !authStore.user) {
        authStore.returnUrl = to.fullPath;
        return '/account/login';
    }

    if(to.fullPath.includes('/adminpage/') && !authStore.user.role.isAdmin){
        return '/page-not-found'
    }
    if(to.fullPath.includes('/dosenpage/') && !authStore.user.role.isDosen){
        return '/page-not-found'
    }
    if(to.fullPath.includes('/mahasiswapage/') && !authStore.user.role.isMahasiswa){
        return '/page-not-found'
    }

    if(to.fullPath.includes('account/signup') && !sessionStorage.getItem('email')){
        return '/account/login'
    }
});
