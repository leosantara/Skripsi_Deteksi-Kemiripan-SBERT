import { defineStore } from 'pinia';
import { fetchWrapper } from '@/helpers';
import { useAlertStore } from '@/stores';

const baseUrl = `${import.meta.env.VITE_API_URL}/mahasiswa`;

//untuk method-method khusus role mahasiswa
export const useMahasiswaStore = defineStore({
    id:'mahasiswa',
    state: () => ({
        periodesActive:[],
        dosens:[],
        deskEvaluations:[]
    }),
    actions: {
        async getCurrentActivePeriode(){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/periodes/active`)
                if(response.status==200){
                    this.periodesActive = response.data
                }
                return true
            }catch(error){
                const alertStore = useAlertStore()
                if(error == "Periode kolokium aktif tidak ditemukan."){
                    return false
                }
                alertStore.error('Terjadi kesalahan dalam memuat data Periode Aktif')
                this.periodesActive = 'ERROR'
                return false
            }
        },
        async getAllDosens(){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/data-dosen`)
                if(response.status==200){
                    this.dosens = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Tidak dapat memuat data dosen.')
            }
        },
        async getAllDeskEvaluations(nim){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/mahasiswa/${nim}`)
                if(response.status==200){
                    this.deskEvaluations = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Tidak dapat memuat data Desk Evaluation untuk mahasiswa dengan NIM ${nim}.`)
            }
        },
        async getDeskEvaluationByProposalID(proposalID){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/proposal/id/${proposalID}`)
                if(response.status==200){
                    this.deskEvaluation = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Desk Evaluation untuk proposal dengan ID ${proposalID} tidak dapat kami temukan.`)
            }
        },
    }
})