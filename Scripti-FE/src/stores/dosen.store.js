import { defineStore } from 'pinia';
import { fetchWrapper } from '@/helpers';
import { useAlertStore } from "./alert.store";

const baseUrl = `${import.meta.env.VITE_API_URL}/dosen`;
export const useDosenStore = defineStore({
    id:'dosen',
    state: () => ({
        dosens:[],
        dosen:null,
        deskEvaluations:[],
        deskEvaluation:null,
        jadwalSidangDosen:null,
        sidangDetails:null,
        periodes:[],
        periodesActive:[],
        periode:null
    }),
    actions:{
        async getAllDosen(){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/data-dosen`)
                if(response.status==200){
                    this.dosens = response.data
                }return true
            }catch(error){
                return false
            }
        },
        async getDosenByDosenID(dosenID){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/data-dosen/id/${dosenID}`)
                if(response.status==200){
                    this.dosen = response.data
                }return true
            }catch(error){
                return false
            }
        },

        async createDeskEvaluation(deskEvaluation){
            try{
                await fetchWrapper.post(`${baseUrl}/desk-evaluation`, deskEvaluation)
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Gagal menambahkan data Desk Evaluation.`)
            }
        },

        async getDeskEvaluationsByDosen1ID(dosen1ID){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/dosen-pembimbing/1/id/${dosen1ID}`)
                if(response.status==200){
                    this.deskEvaluations = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Tidak dapat menemukan data Desk Evaluation untuk dosen dengan ID ${dosen1ID}.`)
            }
        },

        async getDeskEvaluationsByDosen2ID(dosen2ID){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/dosen-pembimbing/2/id/${dosen2ID}`)
                if(response.status==200){
                    this.deskEvaluations = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Tidak dapat menemukan data Desk Evaluation untuk dosen dengan ID ${dosen2ID}.`)
            }
        },

        async getDeskEvaluationByDosen1Nidn(dosen1nidn){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/dosen-pembimbing/1/nidn/${dosen1nidn}`)
                if(response.status==200){
                    this.deskEvaluations = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Tidak dapat menemukan data Desk Evaluation untuk dosen dengan NIDN ${dosen1nidn}.`)
            }
        },

        async getDeskEvaluationByDosen2Nidn(dosen2nidn){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/dosen-pembimbing/2/nidn/${dosen2nidn}`)
                if(response.status==200){
                    this.deskEvaluations = response.data
                }return true
            }catch(error){
                // const alertStore = useAlertStore()
                // alertStore.error(`Tidak dapat menemukan data Desk Evaluation untuk dosen dengan NIDN ${dosen2nidn}.`)
                return false
            }
        },

        async getDeskEvaluationByDosenID(dosenID){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/dosen-pembimbing/id/${dosenID}`)
                if(response.status==200){
                    this.deskEvaluations = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Tidak dapat menemukan data Desk Evaluation untuk dosen dengan ID ${dosenID}.`)
            }
        },

        async getDeskEvaluationByDosenNidn(nidn){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/dosen-pembimbing/nidn/${nidn}`)
                if(response.status==200){
                    this.deskEvaluations = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Tidak dapat menemukan data Desk Evaluation untuk dosen dengan NIDN ${nidn}.`)
            }
        },

        async getDeskEvaluationsByEvaluator(evaluatorID){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/dosen-evaluator/${evaluatorID}`)
                if(response.status==200){
                    this.deskEvaluations = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Tidak dapat menemukan data Desk Evaluation untuk dosen dengan NIDN ${evaluatorID}.`)
            }
        },
    
        async getDeskEvaluationByProposalID(proposalID){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/proposal/id/${proposalID}`)
                if(response.status==200){
                    this.deskEvaluation = response.data[0] //this works, karena 1 proposal punya tepat 1 de
                    return true
                } return false
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Desk Evaluation untuk proposal dengan ID ${proposalID} tidak dapat kami temukan.`)
                return false
            }
        },
    
        async getDeskEvaluationsBySidangID(sidangID){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/sidang/id/${sidangID}`)
                if(response.status==200){
                    this.deskEvaluations = response.data
                    return true
                } return false
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Desk Evaluation untuk sidang dengan ID ${sidangID} tidak dapat kami temukan.`)
                return false
            }
        },
    
        async updateDeskEvaluation(deskEvaluationID, newDeskEvaluation){
            try{
                await fetchWrapper.put(`${baseUrl}/desk-evaluation/${deskEvaluationID}`, newDeskEvaluation)
                return true
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Maaf, Desk Evaluation dengan ID ${deskEvaluationID} gagal di-update.`)
                return false
            }
        },
    
        //getjadwal:
        async getJadwalSidangDosenByNidnAndPeriodeID(nidn, periodeID){
            //mendapat dosen beserta tim sidangnya
            try{
                let response = await fetchWrapper.get(`${baseUrl}/group-evaluator/dosen-evaluator/periode-and-nidn?periodeId=${periodeID}&nidn=${nidn}`)
                if(response.status==200){
                    this.jadwalSidangDosen = response.data
                }
                return true
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Anda tidak termuat dalam jadwal sidang manapun pada periode ini.`)
                return false
            }
        },
    
        async getSidangDetailsByKelompokAndPeriodeID(kelompok, periodeID){
            try{
                //mendapat detail group sidang
                let response = await fetchWrapper.get(`${baseUrl}/group-evaluator/periode-and-kelompok?periodeId=${periodeID}&kelompok=${kelompok}`)
                if(response.status==200){
                    this.sidangDetails = response.data
                }
                return true
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Anda tidak termuat dalam jadwal sidang manapun pada periode ini.`)
                return false
            }
        },
        // async getJadwalSidangDosenPerKelompok(kelompok, periodeID){
        //     try{
        //         //mendapat detail group sidang
        //         let response = await fetchWrapper.get(`${baseUrl}/group-evaluator/periode-and-kelompok?periodeId=${periodeID}&kelompok=${kelompok}`)
        //         if(response.status==200){
        //             this.jadwalSidangDosen = response.data
        //         }
        //         return true
        //     }catch(error){
        //         const alertStore = useAlertStore()
        //         alertStore.error(`Anda tidak termuat dalam jadwal sidang manapun pada periode ini.`)
        //         return false
        //     }
        // },
    
        //periode:
        async getCurrentActivePeriode(){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/periodes/active`)
                if(response.status==200){
                    this.periodesActive = response.data
                    return true
                }return false
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
    
        async getAllPeriode(){
            this.fetching = true
            try{
                let response = await fetchWrapper.get(`${baseUrl}/periodes/findAll`)
                if(response.status==200){
                    this.periodes = response.data.sort((a, b) => {
                        return b.id - a.id
                    })
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Gagal memuat data periode.')
            }finally{
                this.fetching = false
            }
        },
    
        async getPeriodeById(id){
            this.fetching = true
            try{
                let response = await fetchWrapper.get(`${baseUrl}/periodes/${id}`)
                if(response.status==200){
                    this.periode = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Gagal memuat data periode.')
            }finally{
                this.fetching = false
            }
        },
        
    }
})