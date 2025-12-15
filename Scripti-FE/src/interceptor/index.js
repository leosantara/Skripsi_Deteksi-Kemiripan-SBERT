import axios from "axios";
import { useAuthStore } from "../stores/auth.store";

axios.interceptors.response.use(
    response => response, 
    async error => {
        const {user, logout} = useAuthStore()
        const prohibited = (error.status == 401 || error.status == 403)
        if (prohibited && user) {
            //if already login BUT prohibited:
            if(error.response?.data.data.message.includes("JWT expired")){
                alert('Session Anda telah berakhir, silakan login kembali untuk melanjutkan.')
            }else if(error.response?.data.data.message.includes("Access is denied")){
                alert("Permintaan Anda tidak dapat diproses karena Anda tidak punya hak akses untuk mengakses permintaan tersebut.")
            }
            logout()
            return Promise.reject(error)
        }
    }
)