import { defineStore } from "pinia"
import { useAlertStore } from '@/stores';
import { fetchWrapper } from '@/helpers';
import { router } from "../router";
//import axios from 'axios';

const baseUrlMhs = `${import.meta.env.VITE_API_URL}/mahasiswa`
const baseUrlDsn = `${import.meta.env.VITE_API_URL}/dosen`
const baseUrlAdm = `${import.meta.env.VITE_API_URL}/admin`

export const useProposalStore = defineStore({
    id: 'proposal',
// state:
    state:() => ({
        proposals:[],
        proposalsByMahasiswa:[],
        proposalsByDosen:[],
        proposal:null,
        lastSimilarityResult: null,
        currentSimilarityIndex: 0,    // index proposal yang sedang ditampilkan
        similarProposals: [],       // array hasil kemiripan (results)
        bestSimilarProposal: null,  // proposal paling mirip (results[0])
        indexingStatus: {
            isRunning: false,
            progressPercent: 0,
            message: ''
        },
        _indexingInterval: null, // Variabel privat untuk timer
    }),
    actions:{
        //FOR ADMIN ONLY:
        async adminGetAllProposals(){
            try{
                let response = await fetchWrapper.get(`${baseUrlAdm}/proposal/getall`)
                if(response.status==200){
                    this.proposals = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Gagal memuat data proposal.')
            }
        },
        // 1. FUNGSI CEK STATUS (Sekali panggil)
        async adminGetIndexingStatus() {
            try {
                // Panggil Endpoint Spring Boot: GET /admin/system/indexing-status
                const response = await fetchWrapper.get(`${baseUrlAdm}/system/indexing-status`)
                
                // Update state
                if (response) {
                    this.indexingStatus = response
                }
                
                return this.indexingStatus
            } catch (error) {
                console.error("Gagal cek status indexing:", error)
                return null
            }
        },

        // 2. FUNGSI MULAI POLLING (Dipanggil saat tombol diklik atau saat halaman load)
        startIndexingPolling() {
            // Cegah double interval
            if (this._indexingInterval) return 

            console.log("Mulai polling status AI...")
            
            // Set interval setiap 2 detik (2000ms)
            this._indexingInterval = setInterval(async () => {
                const status = await this.adminGetIndexingStatus()
                
                // Jika selesai atau error (isRunning = false)
                if (status && !status.isRunning) {
                    this.stopIndexingPolling()
                    
                    // Opsional: Tampilkan notifikasi jika baru saja selesai (progress 100)
                    if (status.progressPercent === 100) {
                        const alertStore = useAlertStore()
                        alertStore.success('Proses Update Data AI Selesai!')
                    }
                }
            }, 2000)
        },

        // 3. FUNGSI STOP POLLING
        stopIndexingPolling() {
            if (this._indexingInterval) {
                clearInterval(this._indexingInterval)
                this._indexingInterval = null
                console.log("Polling status AI berhenti.")
            }
            // Set isRunning false biar UI hilang (tapi biarkan message terakhir)
            this.indexingStatus.isRunning = false 
        },

        // 4. UPDATE FUNGSI TRIGGER (Agar langsung mulai polling setelah klik)
        async adminTriggerReindex() {
            const alertStore = useAlertStore()
            try {
                // Set status dummy biar UI langsung muncul "Starting..."
                this.indexingStatus.isRunning = true
                this.indexingStatus.progressPercent = 0
                this.indexingStatus.message = "Memulai permintaan..."
                
                // Panggil Backend
                await fetchWrapper.post(`${baseUrlAdm}/system/update-ai`, {})
                
                // Langsung aktifkan polling
                this.startIndexingPolling()
                
                return true
            } catch (error) {
                console.error("Gagal reindex:", error)
                alertStore.error('Gagal memicu proses update AI.')
                this.indexingStatus.isRunning = false
                throw error 
            }
        },
        async adminGetAllProposalsDesc(){
            try{
                let response = await fetchWrapper.get(`${baseUrlAdm}/proposal/getalldesc`)
                if(response.status==200){
                    this.proposals = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Gagal memuat data proposal.')
            }
        },
        async adminGetProposalsForSidangDE(periodeId){
            try{
                let request = {periodeId:periodeId, status: 'V'}
                let response = await fetchWrapper.post(`${baseUrlAdm}/proposal/by-periode-and-status`, request)
                if(response.status==200){
                    this.proposals = response.data
                }
            }catch(error){
                this.proposals = []
            }
        },
        async adminGetProposalsByYear(year){
            try{
                let request = {year:year}
                let response = await fetchWrapper.post(`${baseUrlAdm}/proposal/by-year`, request)
                if(response.status==200){
                    this.proposals = response.data
                    return true
                }
                return false
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Gagal memuat data proposal.')
                return false
            }
        },
        async adminGetProposalByID(proposalID){
            try{
                let response = await fetchWrapper.get(`${baseUrlAdm}/proposal/${proposalID}`)
                if(response.status==200){
                    this.proposal = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Proposal yang Anda cari tidak dapat kami temukan.')
            }
        },
        async adminGetProposalByJudul(judul){
            try{
                let request = {judul:judul}
                let response = await fetchWrapper.post(`${baseUrlAdm}/proposal/search`, request)
                if(response.status==200){
                    this.proposals = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Data proposal gagal dicari.')
            }
        },
        async adminGetProposalByNim(nim){
            try{
                let request = {nim:nim}
                let response = await fetchWrapper.post(`${baseUrlAdm}/proposal/search/nim`, request)
                if(response.status==200){
                    this.proposals = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Data proposal untuk NIM ${nim} tidak dapat ditemukan.`)
            }
        },
        async adminGetProposalByNidn(nidn){
            try{
                let request = {dosenId:nidn}
                let response = await fetchWrapper.post(`${baseUrlAdm}/proposal/search-by-dosenid`, request)
                if(response.status==200){
                    this.proposals = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Data proposal untuk NIDN ${nidn} tidak dapat ditemukan.`)
            }
        },
        async adminGetProposalFile(proposalID, filename){
            try{
                let response = await fetchWrapper.get(`${baseUrlAdm}/proposal/${proposalID}/download`, new Blob([]))
                const url = window.URL.createObjectURL(new Blob([response]));

                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', filename); // Specify the file name

                document.body.appendChild(link);
                link.click();

                link.remove();
                window.URL.revokeObjectURL(url);
                return true
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Gagal mendowload file proposal dengan ID ${proposalID}.`)
                return false
            }
        },
        async adminGetProposalBlob(proposalID, mimeType){
            try{
                let response = await fetchWrapper.get(`${baseUrlAdm}/proposal/${proposalID}/download`, new Blob([]))
                return new Blob([response], { type: mimeType })
            }catch(error){
                return null
            }
        },
        async adminCheckIfProposalExists(proposalID){
            try{
                let response = await fetchWrapper.get(`${baseUrlAdm}/proposal/${proposalID}/filename`)
                return response.data
            }catch(error){
                return null
            }
        },
        async adminAddProposal(newProposal){
            const alertStore = useAlertStore()
            try{
                await fetchWrapper.post(`${baseUrlAdm}/proposal/submit`, newProposal)
                alertStore.success('Proposal baru berhasil disubmit!')
            }catch(error){
                alertStore.error('Gagal menambahkan proposal baru.')
            }
        },
        async adminUpdateProposal(proposalID, newProposal){
            const alertStore = useAlertStore()
            try{
                await fetchWrapper.put(`${baseUrlAdm}/proposal/${proposalID}`, newProposal)
                alertStore.success('Data proposal berhasil di-update!')
                return true
            }catch(error){
                alertStore.error('Gagal mengupdate proposal.')
                return false
            }
        },
        async adminUpdateProposalStatus(proposalID, newStatus){
            const alertStore = useAlertStore()
            try{
                let request = {status:newStatus}
                await fetchWrapper.patch(`${baseUrlAdm}/proposal/update-status/${proposalID}`, request)
                alertStore.success('Status proposal berhasil di-update!')
            }catch(error){
                alertStore.error('Gagal mengupdate status proposal.')
            }
        },
        async adminDeleteProposal(proposalID){
            try{
                await fetchWrapper.delete(`${baseUrlAdm}/proposal/${proposalID}`)
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error('Gagal menghapus proposal.')
            }
        },

        //FOR DOSEN ONLY:
        async dosenGetAllProposals(dosenId){
            const alertStore = useAlertStore()
            try{
                let request = {dosenId:dosenId}
                let response = await fetchWrapper.post(`${baseUrlDsn}/proposal/search-by-dosenid`, request)
                if(response.status==200){
                    this.proposalsByDosen = response.data
                }return true
            }catch(error){
                alertStore.error(`Gagal memuat proposal bagi dosen dengan NIDN ${nidn}.`)
                return false
            }
        },
        async dosenGetProposalById(proposalID, nidn){
            try{
                let request = {
                    id:proposalID,
                    nim:"",
                    nidn:nidn
                }
                let response = await fetchWrapper.post(`${baseUrlDsn}/proposal/nidn`, request)
                if(response.status==200){
                    this.proposal = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Gagal memuat proposal dengan ID ${proposalID}.`)
            }
        },
        async dosenUpdateProposalStatus(proposalID, newStatus){
            const alertStore = useAlertStore()
            try{
                let request = {status:newStatus}
                await fetchWrapper.patch(`${baseUrlDsn}/proposal/update-status/${proposalID}`, request)
                alertStore.success('Status proposal berhasil di-update!')
            }catch(error){
                alertStore.error('Gagal mengupdate status proposal.')
            }
        },
        async dosenGetProposalFile(proposalID, filename){
            try{
                //REFACTOR
                let response = await fetchWrapper.get(`${baseUrlDsn}/proposal/${proposalID}/download`, new Blob([]))
                const url = window.URL.createObjectURL(new Blob([response]));

                // Create a link element to trigger the download
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', filename); // Specify the file name

                // Append to the body and trigger click
                document.body.appendChild(link);
                link.click();

                // Clean up
                link.remove();
                window.URL.revokeObjectURL(url);
                return true
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Gagal mendownload file proposal dengan ID ${proposalID}.`)
                return false
            }
        },
        async dosenCheckIfProposalExists(proposalID){
            try{
                //REFACTOR
                let response = await fetchWrapper.get(`${baseUrlDsn}/proposal/${proposalID}/filename`)
                return response.data
            }catch(error){
                return null
            }
        },
        async dosenGetProposalBlob(proposalID){
            try{
                let response = await fetchWrapper.get(`${baseUrlDsn}/proposal/${proposalID}/download`, new Blob([]))
                return new Blob([response])
            }catch(error){
                return null
            }
        },
        async dosenUpdateProposal(proposalID, newProposal){
            const alertStore = useAlertStore()
            try{
                await fetchWrapper.put(`${baseUrlDsn}/proposal/${proposalID}`, newProposal)
                alertStore.success('Data proposal berhasil di-update!')
                return true
            }catch(error){
                alertStore.error('Gagal mengupdate proposal.')
                return false
            }
        },

        //FOR MAHASISWA ONLY:
        async mahasiswaGetAllProposal(nim){
            const alertStore = useAlertStore()
            try{
                let request = {nim:nim}
                let response = await fetchWrapper.post(`${baseUrlMhs}/proposal/search/nim`, request)
                if(response.status==200){
                    this.proposalsByMahasiswa = response.data
                }
            }catch(error){
                alertStore.error(`Data proposal untuk NIM ${nim} tidak dapat ditemukan.`)
            }
        },
async mahasiswaGetProposalSimilarity(proposalID, nim, topK = 10) {
    const alertStore = useAlertStore()
    try {
        const request = {
            id: proposalID,
            nim: nim,
            nidn: ""
        }

        const response = await fetchWrapper.post(
            `${baseUrlMhs}/proposal/nim/similarity?topK=${topK}`,
            request
        )

        if (response.status === 200) {
            // ResponseWrapper<SimilarityResponseDTO>
            this.lastSimilarityResult = response.data
        } else {
            this.lastSimilarityResult = null
            alertStore.error(response.message || 'Gagal menghitung kemiripan.')
        }
    } catch (error) {
        this.lastSimilarityResult = null
        alertStore.error(`Gagal menghitung kemiripan proposal dengan ID ${proposalID}.`)
    }
},

        async mahasiswaGetProposalById(proposalID, nim){
            try{
                let request = {
                    id:proposalID,
                    nim:nim,
                    nidn:""
                }
                let response = await fetchWrapper.post(`${baseUrlMhs}/proposal/nim`, request)
                if(response.status==200){
                    this.proposal = response.data
                }
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Gagal memuat proposal dengan ID ${proposalID}.`)
            }
        },

async mahasiswaCheckDraftSimilarity(payload) {
            const alertStore = useAlertStore()
            // Reset hasil lama
            this.lastSimilarityResult = null 
            
            try {
                // Panggil Endpoint Check Draft
                const response = await fetchWrapper.post(
                    `${baseUrlMhs}/proposal/check-draft?topK=5`, 
                    payload
                )

                if (response.status === 200 && response.data) {
                    this.lastSimilarityResult = response.data
                    alertStore.success('Pengecekan kemiripan selesai!')
                    return true
                } else {
                    alertStore.error('Gagal mendapatkan data kemiripan.')
                    return false
                }
            } catch (error) {
                this.lastSimilarityResult = null
                console.error(error)
                alertStore.error('Terjadi kesalahan koneksi saat cek kemiripan.')
                return false
            }
        },

        async adminTriggerReindex() {
            const alertStore = useAlertStore()
            try {
                // Panggil endpoint Spring Boot
                // Endpoint: POST /admin/system/update-ai
                const response = await fetchWrapper.post(`${baseUrlAdm}/system/update-ai`, {})
                
                // Cek status (biasanya fetchWrapper sudah handle error, tapi kita pastikan)
                // Jika sukses, return true agar komponen tahu
                return true
            } catch (error) {
                console.error("Gagal reindex:", error)
                alertStore.error('Gagal memicu proses update AI.')
                throw error // Lempar error agar komponen bisa menangkapnya di catch(e)
            }
        },
        async mahasiswaAddProposal(newProposal){
            const alertStore = useAlertStore()
            try{
                const response = await fetchWrapper.post(`${baseUrlMhs}/proposal/submit`, newProposal)
                
                if (response.status === 200) {
                    this.lastSimilarityResult = response.data
                    alertStore.success('Proposal baru berhasil disubmit!')
                    return true; // <--- WAJIB ADA: Return True
                } else {
                    this.lastSimilarityResult = null
                    return false; // <--- WAJIB ADA: Return False
                }
            }catch(error){
                this.lastSimilarityResult = null
                alertStore.error('Gagal menambahkan proposal baru.')
                return false; // <--- WAJIB ADA
            }
        },
        

        async mahasiswaUpdateProposal(proposalID, newProposal){
            const alertStore = useAlertStore()
            try{
                // Asumsi: Backend mengembalikan SimilarityResponseDTO setelah update sukses
                const response = await fetchWrapper.put(`${baseUrlMhs}/proposal/${proposalID}`, newProposal)
                
                if (response.status === 200) {
                    // Cek apakah response.data berisi hasil similarity
                    // (Tergantung implementasi di Java Controller-mu)
                    if (response.data && response.data.results) {
                        this.lastSimilarityResult = response.data
                    }
                    alertStore.success('Data proposal berhasil di-update!')
                    return true // Return true agar komponen Vue tahu sukses
                } else {
                    return false
                }
            }catch(error){
                alertStore.error('Gagal mengupdate proposal.')
                return false
            }
        },
        async mahasiswaGetProposalFile(proposalID, filename){
            try{
                //REFACTOR
                let response = await fetchWrapper.get(`${baseUrlMhs}/proposal/${proposalID}/download`, new Blob([]))
                const url = window.URL.createObjectURL(new Blob([response]));

                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', filename); // Specify the file name

                document.body.appendChild(link);
                link.click();

                link.remove();
                window.URL.revokeObjectURL(url);
                return true
            }catch(error){
                const alertStore = useAlertStore()
                alertStore.error(`Gagal mendownload file proposal dengan ID ${proposalID}.`)
                return false
            }
        },
        async mahasiswaCheckIfProposalExists(proposalID){
            try{
                //REFACTOR
                let response = await fetchWrapper.get(`${baseUrlMhs}/proposal/${proposalID}/filename`)
                return response.data
            }catch(error){
                return null
            }
        },
        async mahasiswaGetProposalBlob(proposalID){
            try{
                let response = await fetchWrapper.get(`${baseUrlMhs}/proposal/${proposalID}/download`, new Blob([]))
                return new Blob([response])
            }catch(error){
                return null
            }
        }
    }
})