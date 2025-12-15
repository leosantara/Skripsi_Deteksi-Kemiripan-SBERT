import { defineStore } from 'pinia';

import { fetchWrapper } from '@/helpers';
import { router } from '@/router';
import { useAlertStore } from '@/stores';

const baseUrl = `${import.meta.env.VITE_API_URL}/auth`;

export const useAuthStore = defineStore({
    id: 'auth',
    state: () => ({
        // initialize state from local storage to enable user to stay logged in
        user: JSON.parse(localStorage.getItem('user')),
        account_info:null,
        returnUrl: null
    }),
    actions: {
        async login(serverAuthCode, clientType) {
            try {
                const data = {
                    serverAuthCode: serverAuthCode,
                    clientType: clientType
                };
                const response = await fetchWrapper.post(`${baseUrl}/signin`, data);
                const userData = response.data;  
                let userRole = {
                    isAdmin:false,
                    isDosen:false,
                    isMahasiswa:false
                }

                userData.groups.forEach(group => {
                    if(group['groupname']=='ADMINISTRATOR'){userRole['isAdmin']=true}
                    else if(group['groupname']=='DOSEN'){userRole['isDosen']=true}
                    else if(group['groupname']=='MAHASISWA'){userRole['isMahasiswa']=true}
                });
                
                const user = {
                    email: userData.googleUser.googleEmail,  
                    token: userData.jwtToken,
                    role: userRole,
                    details: userData
                };
                this.user = user;
                // store user details and jwt in local storage to keep user logged in between page refreshes
                localStorage.setItem('user', JSON.stringify(user));
                // redirect to previous url or default to home page
                router.push(this.returnUrl || '/');
            } catch (error) {
                // if email is from ukdw, but user is not registered yet:
                if(error.includes("User not registered. Username:")){
                    const email = error.split(": ")[1]
                    sessionStorage.setItem("email", email)
                    router.push("/account/signup");
                    return
                }
                const alertStore = useAlertStore();
                alertStore.error(`Login Gagal!`);
            }
        },

        async loginWithEmail(email){
            try {
                const response = await fetchWrapper.post(`${baseUrl}/signin-email`, {email:`${email}`});  
                const userData = response.data;  
                let userRole = {
                    isAdmin:false,
                    isDosen:false,
                    isMahasiswa:false
                }

                userData.groups.forEach(group => {
                    if(group['groupName']=='ADMINISTRATOR'){userRole['isAdmin']=true}
                    else if(group['groupName']=='DOSEN'){userRole['isDosen']=true}
                    else if(group['groupName']=='MAHASISWA'){userRole['isMahasiswa']=true}
                });
                
                const user = {
                    email: userData.googleUser.googleEmail,  
                    token: userData.jwtToken,
                    role: userRole,
                    details: userData
                };
                this.user = user;
                localStorage.setItem('user', JSON.stringify(user));
                router.push('/')
            } catch (error) {
                const alertStore = useAlertStore();
                alertStore.error('Login Gagal!'); 
            }
        },

        async addUser(user){
            try {
                const response = await fetchWrapper.post(`${baseUrl}/add-user`, user);
            } catch (error) {
                const alertStore = useAlertStore();
                alertStore.error("User gagal didaftarkan."); 
            }
        },

        async findUserByNim(nim){
            try{
                const response = await fetchWrapper.get(`${baseUrl}/find-mahasiswa/${nim}`)
                this.account_info = response.data
            }catch(error){
                const alertStore = useAlertStore();
                let message = `Mahasiswa dengan NIM "${nim}" tidak dapat ditemukan, silakan mendaftar matakuliah kolokium atau hubungi admin`
                alertStore.error(message);
                this.account_info = null
            }
        },

        async findUserByNidn(nidn){
            try{
                const response = await fetchWrapper.get(`${baseUrl}/find-dosen/${nidn}`)
                this.account_info = response.data
            }catch(error){
                const alertStore = useAlertStore();
                let message = `Dosen dengan NIDN "${nidn}" tidak dapat ditemukan, silakan hubungi admin untuk bantuan pembuatan akun`
                alertStore.error(message);
                this.account_info = null
            }
        },

        async adminFindMahasiswaNameByNim(nim){
            try{
                const response = await fetchWrapper.get(`${baseUrl}/find-mahasiswa/${nim}`)
                return response.data.nama
            }catch(error){
                return ""
            }
        },

        async logout() {
            try{
                const user = JSON.parse(localStorage.getItem('user'))
                const data = {accessToken:user.details.googleUser.accessToken}
                await fetchWrapper.post(`${baseUrl}/signout`, data)
                
                this.user = null;
                this.account_info = null;
                sessionStorage.clear();
                localStorage.removeItem('user');
                router.push('/account/login');
                const alertStore = useAlertStore();
                alertStore.success(`Log Out sukses.`);
            }catch(error){
                const alertStore = useAlertStore();
                alertStore.error(`Proses Log Out gagal.`);
            }
        }
    }
});
