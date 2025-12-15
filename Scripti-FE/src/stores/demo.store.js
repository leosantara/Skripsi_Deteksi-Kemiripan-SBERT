import { defineStore } from 'pinia';

import { fetchWrapper } from '@/helpers';
import { router } from '@/router';
import { useAlertStore } from '@/stores';

const baseUrl = `${import.meta.env.VITE_API_URL}`;

export const useDemoStore = defineStore({
    id: 'demo',
    state: () => ({
        // initialize state from local storage to enable user to stay logged in
        user: JSON.parse(localStorage.getItem('user')),
        returnUrl: null
    }),
    actions: {        
        async testDemo() {            
            const response = await fetchWrapper.get(`${baseUrl}/restricted`)
            console.log(response)
            // router.push(this.returnUrl || '/');            
        },
        async testRoleAdmin(nim){
            try{
                const response = await fetchWrapper.get(`${baseUrl}/admin/mahasiswa/${nim}`)
                console.log(response)
            }catch(error){
                console.log('error: ', error)
            }
        }
    }
});
