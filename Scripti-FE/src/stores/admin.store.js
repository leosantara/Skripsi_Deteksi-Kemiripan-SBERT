import { defineStore } from "pinia"
import { useAlertStore } from '@/stores';
import { fetchWrapper } from '@/helpers';
import { router } from "../router";

const baseUrl = `${import.meta.env.VITE_API_URL}/admin`
export const useAdminStore = defineStore({
    id: 'admin',
    state: () => ({
        fetching: false,
        mahasiswas:[],
        mahasiswa:null,
        dosens:[],
        dosen:null,
        periodes:[],
        periodesActive:[],
        periode:null,
        deskEvaluations:[],
        deskEvaluation:null,
        sidangDEArr:[],
        sidangDE:null,
        sidangDetails:null,
        dosenDEArr:[],
        dosenDE:null,
        jadwalSidangDosen:null,
        jadwalSidangMahasiswa:null
    }),
    actions: {
        async getAllMahasiswas(){
            this.fetching = true
            try{
                let response = await fetchWrapper.get(`${baseUrl}/data-mahasiswa`)
                this.mahasiswas = response.data.sort((a, b) => {
                    return b.nim - a.nim
                })
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Gagal memuat data mahasiswa.')
            }finally{
                this.fetching = false
            }
        },

        async getMahasiswaByNim(nim){
            this.fetching = true
            try{
                let response = await fetchWrapper.get(`${baseUrl}/data-mahasiswa/${nim}`)
                this.mahasiswa = response.data
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error("Gagal mengambil data.")
            }finally{
                this.fetching = false
            }
        },

        async addMahasiswa(mahasiswa){
            const alertStore = useAlertStore()
            try{
                let response = await fetchWrapper.post(`${baseUrl}/data-mahasiswa`, mahasiswa)
                if(response && response.message?.includes('is already in the database')){
                    alertStore.error(`Gagal menambah data, mahasiswa dengan NIM ${mahasiswa.nim} sudah ada!`)
                    return false
                }
                return true
            }catch(error){
                alertStore.error('Gagal menambah data mahasiswa.')
                return false
            }
        },

        async updateMahasiswa(nim, newMahasiswa){
            const alertStore = useAlertStore()
            try{
                let response = await fetchWrapper.put(`${baseUrl}/data-mahasiswa/${nim}`, newMahasiswa)
                if(response.message && response.message == "Mahasiswa berhasil diupdate"){
                    alertStore.success('Data mahasiswa berhasil di-update!')
                    router.push('/adminpage/mahasiswa')
                }
            }catch(error){
                alertStore.error("Gagal meng-update data.")
            }
        },

        async deleteMahasiswaByNim(nim){
            try{
                await fetchWrapper.delete(`${baseUrl}/data-mahasiswa/${nim}`)
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Gagal menghapus data mahasiswa.')
            }
        },
        
        async getAllDosen(){
            this.fetching = true
            try{
                let response = await fetchWrapper.get(`${baseUrl}/data-dosen`)
                this.dosens = response.data.sort((a, b) => {
                    return b.id - a.id
                })
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Gagal memuat data dosen.')
            }finally{
                this.fetching = false
            }
        }, 
    
        async getDosenByNidn(nidn){
            this.fetching = true
            try{
                let response = await fetchWrapper.get(`${baseUrl}/data-dosen/${nidn}`)
                this.dosen = response.data
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Gagal memuat data dosen.')
            }finally{
                this.fetching = false
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

        async addDosen(dosen){
            const alertStore = useAlertStore()
            try{
                let response = await fetchWrapper.post(`${baseUrl}/data-dosen`, dosen)
                if(response && response.message?.includes('is already in the database')){
                    alertStore.error(`Gagal menambah data, Dosen dengan NIDN ${dosen.nidn} sudah ada!`)
                    return false
                }
                return true
            }catch(error){
                alertStore.error('Gagal menambah data dosen.')
                return false
            }
        },

        async updateDosen(nidn, newDosen){
            const alertStore = useAlertStore()
            try{
                let response = await fetchWrapper.put(`${baseUrl}/data-dosen/${nidn}`, newDosen)
                if(response.message && response.message == "Dosen berhasil diupdate"){
                    router.push('/adminpage/dosen')
                    alertStore.success('Data dosen berhasil di-update!')
                }
            }catch(error){
                alertStore.error("Gagal meng-update data.")
            }
        },
    
        async deleteDosenByNidn(nidn){
            try{
                await fetchWrapper.delete(`${baseUrl}/data-dosen/${nidn}`)
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error("Gagal menghapus data dosen.")
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
    
        async getAdjacentPeriode(id){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/periodes/${id}/adjacent`)
                if(response.status==200){
                    return response.data
                }
                return []
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Gagal menampilkan data periode terdahulu dan periode selanjutnya.')
                return []
            }
        },

        async getCurrentActivePeriode(){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/periodes/active`)
                if(response.status==200){
                    this.periodesActive = response.data
                    return true
                }
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

        async addPeriode(periode){
            const alertStore = useAlertStore()
            try{
                await fetchWrapper.post(`${baseUrl}/periodes/create`, periode)
                router.push('/adminpage/periode-proposal')
                alertStore.success('Data periode berhasil ditambahkan!')
            }catch(error){
                alertStore.error('Gagal menambah data periode.')
            }
        },

        async updatePeriode(id, newPeriode){
            const alertStore = useAlertStore()
            try{
                let response = await fetchWrapper.put(`${baseUrl}/periodes/${id}`, newPeriode)
                if(response.message && response.message == "Periode Kolokium berhasil diperbarui"){
                    router.push('/adminpage/periode-proposal')
                    alertStore.success('Data periode berhasil di-update!')
                    return
                }
                //alertStore.error("Gagal meng-update data.")
            }catch(error){
                alertStore.error("Gagal meng-update data.")
            }
        },
    
        async deletePeriodeById(id){
            try{
                await fetchWrapper.delete(`${baseUrl}/periodes/${id}`)
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error("Gagal menghapus data periode.")
            }
        },

        //used in Desk Evaluations to manipulate sidangs:
        async createSidangDE(sidangDE){
            try{
                await fetchWrapper.post(`${baseUrl}/group-evaluator/`, sidangDE)
                return true
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Gagal menambahkan sidang Desk Evaluation yang baru.')
                return false
            }
        },

        async getAllSidangDE(){
            try{
                let response = await fetchWrapper.post(`${baseUrl}/list-group-evaluator`)
                if(response.status==200){
                    this.sidangDEArr = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Gagal memuat data sidang Desk Evaluation.')
            }
        },

        async getSidangDEByPeriodeID(periodeID){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/group-evaluator/periode/${periodeID}`)
                if(response.status==200){
                    this.sidangDEArr = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Maaf, sidang dengan ID periode ${periodeID} tidak dapat kami temukan.`)
            }
        },

        async getSidangsDEByDosenID(dosenID){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/group-evaluator/dosen-evaluator/id/${dosenID}`)
                if(response.status==200){
                    this.sidangDEArr = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Maaf, sidang dengan ID dosen ${dosenID} tidak dapat kami temukan.`)
            }
        },

        async getSidangDEByID(sidangID){
            //untuk mendapat waktu sidang
            try{
                let response = await fetchWrapper.get(`${baseUrl}/group-evaluator/${sidangID}`)
                if(response.status==200){
                    this.sidangDE = response.data
                    return true
                } return false
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Maaf, sidang dengan ID ${sidangID} tidak dapat kami temukan.`)
                return false
            }
        },

        async updateSidangDE(sidangID, newSidang){
            try{
                await fetchWrapper.put(`${baseUrl}/group-evaluator/${sidangID}`, newSidang)
                return true
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Maaf, sidang dengan ID ${sidangID} gagal di-update.`)
                return false
            }
        },

        async deleteSidangDE(sidangID){
            try{
                await fetchWrapper.delete(`${baseUrl}/group-evaluator/${sidangID}`)
                this.sidangDEArr = this.sidangDEArr.filter(sidang => sidang.id != sidangID)
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Maaf, sidang dengan ID ${sidangID} gagal dihapus.`)
            }
        },

        //used to manipulate sidangs_dosens
        async getAllDosensForEachSidangs(){
            try{
                //REFACTOR
                let response = await fetchWrapper.get(`${baseUrl}/group-evaluator/dosen-evaluator`)
                this.dosenDEArr = response.data
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Maaf, gagal memuat data dosen untuk sidang Desk Evaluation.`)
            }
        },

        async getDosensBySidangID(sidangID){
            try{
                //to get time for a specific sidang, use this one
                let response = await fetchWrapper.get(`${baseUrl}/group-evaluator/dosen-evaluator/sidang/${sidangID}`)
                if(response.status==200){
                    this.dosenDE = response.data
                    return true
                }return false
            }catch(error){
                const alertStore = useAlertStore()
                let message = ""
                if(sidangID){message = `Maaf, dosen untuk group dengan ID ${sidangID} gagal dimuat.`}
                else{message = `Data tim sidang tidak ditemukan`}
                alertStore.error(message)
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

        async getTanggalSidangDE(sidangID){
            try{
                //to get time for a specific sidang, use this one
                let response = await fetchWrapper.get(`${baseUrl}/group-evaluator/${sidangID}`)
                if(response.status==200){
                    return response.data.waktu
                }
                return ""
            }catch(error){
                return ""
            }
        },

        async checkIfDosenExistsOnSidang(sidangID){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/group-evaluator/dosen-evaluator/sidang/${sidangID}`)
                if(response){
                    if(response.data.anggota.length>0 || response.data.ketua){ return false }
                }
                return true
            }catch(error){
                return false
            }
        },

        async upsertDosensBySidangID(sidangID, dosenKetuaID, anggotaArr){
            try{
                let request = {
                    sidangId:sidangID,
                    ketuaId:dosenKetuaID,
                    anggotaIds:anggotaArr
                }
                await fetchWrapper.post(`${baseUrl}/group-evaluator/dosen-evaluator`, request)
                return true
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Maaf, update dosen untuk group dengan ID ${groupID} gagal.`)
                return false
            }
        },

        //used in desk evaluation for CRUD DE:
        async createDeskEvaluation(deskEvaluation){
            try{
                await fetchWrapper.post(`${baseUrl}/desk-evaluation`, deskEvaluation)
                return true
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Gagal menambahkan data Desk Evaluation.`)
                return false
            }
        },

        async getAllDeskEvaluations(){
            try{
                let response = await fetchWrapper.post(`${baseUrl}/desk-evaluation`)
                if(response.status==200){
                    this.deskEvaluations = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Gagal memuat data Desk Evaluation.')
            }
        },

        async getDeskEvaluationByID(deskEvaluationID){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/${deskEvaluationID}`)
                if(response.status==200){
                    this.deskEvaluation = response.data
                    return true
                } return false
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Desk Evaluation dengan ID ${deskEvaluationID} tidak dapat kami temukan.`)
                return false
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

        async getDeskEvaluationsBySidangID(sidangID){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/sidang/id/${sidangID}`)
                if(response.status==200){
                    this.deskEvaluations = response.data
                    return true
                }return false
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Desk Evaluation untuk sidang dengan ID ${sidangID} tidak dapat kami temukan.`)
                return false
            }
        },

        async getDeskEvaluationsByNIM(nim){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/mahasiswa/${nim}`)
                if(response.status==200){
                    this.deskEvaluations = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Tidak dapat menemukan data Desk Evaluation untuk mahasiswa dengan NIM ${nim}.`)
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
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Tidak dapat menemukan data Desk Evaluation untuk dosen dengan NIDN ${dosen2nidn}.`)
            }
        },

        async getDeskEvaluationByYear(year){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/year/${year}`)
                if(response.status==201){
                    this.deskEvaluations = response.data
                    return true
                }
                return false
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Tidak dapat menemukan data Desk Evaluation untuk tahun ${year}.`)
                return false
            }
        },

        async getDeskEvaluationByPeriodeID(periodeID){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/periode/id/${periodeID}`)
                if(response.status==200){
                    this.deskEvaluations = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Tidak dapat menemukan data Desk Evaluation periode dengan ID ${periodeID}.`)
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

        async deleteDeskEvalutionByID(deskEvaluationID){
            const alertStore = useAlertStore()
            try{
                await fetchWrapper.delete(`${baseUrl}/desk-evaluation/${deskEvaluationID}`)
                this.deskEvaluations = this.deskEvaluations.filter(de => de.id != deskEvaluationID)
                alertStore.success(`Berhasil menghapus data Desk Evaluation dengan ID ${deskEvaluationID}`)
            }catch(error){
                alertStore.error(`Desk Evaluation dengan ID ${deskEvaluationID} gagal dihapus.`)
            }
        },

        //to generate jadwal DE
        async getJadwalSidangsDosenByNIDN(nidn){
            //mendapat semua jadwal yang diikuti dosen pada semua periode
            try{
                let response = await fetchWrapper.get(`${baseUrl}/group-evaluator/dosen-evaluator/nidn/${nidn}`)
                if(response.status==200){
                    this.jadwalSidangDosen = response.data
                }
                return true
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Anda tidak termuat dalam jadwal sidang.`)
                return false
            }
        },

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

        async getJadwalSidangMahasiswaPerKelompok(kelompok, periodeID){
            try{
                let response = await fetchWrapper.get(`${baseUrl}/desk-evaluation/periode/${periodeID}/kelompok/${kelompok}`)
                if(response.status==200){
                    this.jadwalSidangMahasiswa = response.data
                }
                return true
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Tidak dapat memuat data peserta sidang.`)
                return false
            }
        },
    }
})