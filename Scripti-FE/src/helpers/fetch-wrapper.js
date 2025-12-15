import { useAuthStore } from '@/stores';
import axios from 'axios';
import { useAlertStore } from '../stores/alert.store';

export const fetchWrapper = {
    get: request('GET'),
    post: request('POST'),
    put: request('PUT'),
    patch: request('PATCH'),
    delete: request('DELETE')
};

function request(method) {
    return async (url, body) => {
        const requestOptions = {
            method,
            url,            
            headers: authHeader(url), 
            validateStatus: function (status) {
                return status <= 500; // Resolve only if the status code is less than 500
            },
            // signal: abortSignal(10000),
            // timeout: 10000           
        };
        if (body) {
            if(body instanceof FormData){
                // Let axios set the Content-Type (including boundary) for FormData.
                // Manually setting 'multipart/form-data' without boundary breaks parsing on the server.
                requestOptions.data = body;
            }
            else if(body instanceof Blob){
                //for download:
                requestOptions.responseType='blob'
            }
            else{
                requestOptions.headers['Content-Type'] = 'application/json';
                requestOptions.data = JSON.stringify(body);
            }            
        }
        console.log("requestOptions : ", requestOptions);
        
        try {
            const response = await axios(requestOptions);
            console.log(response);
            return handleAxiosResponse(response);
        } catch (error) {
            console.error('Request failed', error);
            throw error;
        }        
    }
}

// helper functions
function authHeader(url) {
    // MUST BE PROVIDED TO EACH REQUEST TO THE SERVER
    // return auth header with jwt if user is logged in and request is to the api url
    const { user } = useAuthStore();
    const isLoggedIn = !!user?.token;
    const isApiUrl = url.startsWith(import.meta.env.VITE_API_URL);
    if (!user) {
        console.error("User is null, unable to retrieve token");
        return {};  // No authorization header if user is null
    }

    //console.log("token : ", user.token)
    if (isLoggedIn && isApiUrl) {        
        return { 
            Authorization: `Bearer ${user.token}` 
        };
    } else {
        return {};
    }
}

async function handleAxiosResponse(response) {
    const isJson = response.headers['content-type']?.includes('application/json');
    const raw = isJson ? response.data : null;  // Axios response data is accessed via `response.data`
    // If backend wraps the real payload in a `data` field (common pattern),
    // unwrap it so callers receive the actual payload (array/object) directly.
    const data = raw && raw.data !== undefined ? raw.data : raw;

    // Check for error response
    if (response.status < 200 || response.status >= 300) {
        const { user, logout } = useAuthStore();
        if ([401, 403].includes(response.status) && user) {
            if(response.data.data.message.includes("JWT expired")){
                alert('Session Anda telah berakhir, silakan login kembali untuk melanjutkan.')
            }else if(response.data.data.message.includes("Access is denied")){
                alert("Permintaan Anda tidak dapat diproses karena Anda tidak punya hak untuk mengakses permintaan tersebut.")
            }
            await logout();  // auto logout if 401 or 403 response
        }

        const error = (data && (data.data?.message || data.message)) || response.statusText;
        return Promise.reject(error);
    }

    // Return both status and data so callers can check `response.status` and `response.data`.
    return { status: response.status, data };
}

// function abortSignal(timeoutMs){
//     //refer to official axios documentation https://axios-http.com/docs/cancellation
//     const abortController = new AbortController();
//     setTimeout(
//         () => {
//             const alertStore = useAlertStore()
//             alertStore.error("Request timeout")
//             abortController.abort()
//         }
//         , timeoutMs || 0);
//     return abortController.signal;
// }