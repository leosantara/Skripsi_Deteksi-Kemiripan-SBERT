<template>
  <div class="container-fluid p-0">
    <h1 class="h3 mb-1">Tambah Proposal</h1>
    <h6 class="text-muted mb-3">Tambahkan proposal untuk periode aktif saat ini.</h6>
    <hr class="border-secondary opacity-25 mb-4">

    <div class="mb-3">
      <button class="btn btn-outline-dark rounded-pill px-4" @click="router.push('/proposalpage/mahasiswapage/proposal-mahasiswa')" type="button">
        &#129092; Kembali
      </button>
    </div>

    <Loading v-bind:isFetching="data.isFetching"/>

    <div v-if="!data.isFetching && !isMahasiswaAktif" class="alert alert-danger text-center shadow-sm" role="alert">
      <h4 class="alert-heading fw-bold">Anda Tidak Terdaftar Tugas Akhir</h4>
      <p>Maaf, Anda tidak tercatat sebagai mahasiswa yang mendaftar matakuliah Kolokium/TA.</p>
    </div>

    <div v-if="!data.isFetching && isMahasiswaAktif">
      <div v-if="!data.currentActivePeriode" class="alert alert-warning text-center">
        <h4 class="alert-heading">Periode Aktif Tidak Ditemukan</h4>
      </div>

      <div v-else-if="alreadySubmitting" class="alert alert-info text-center">
        <h4 class="alert-heading">Proposal Sudah Diunggah</h4>
      </div>

      <div v-else-if="submitTimeOver" class="alert alert-danger text-center">
        <h4 class="alert-heading">Waktu Pengumpulan Berakhir</h4>
      </div>

      <div v-else class="row g-4">
        
        <div :class="hasSimilarityResult ? 'col-lg-8' : 'col-12'" class="transition-all d-flex flex-column">
          <div ref="formRef" class="card border-0 shadow-sm h-100">
            <div class="card-body p-4">
              <form @submit.prevent="openModal()" id="formProposal">
                
                <div class="row g-3 mb-4">
                  <div class="col-md-5">
                    <label class="form-label fw-bold text-secondary small">INFORMASI MAHASISWA</label>
                    <input type="text" class="form-control mb-2" disabled v-model="data.post.nim" placeholder="NIM">
                    <input type="text" class="form-control" disabled v-model="data.penulis.nama" placeholder="Nama Lengkap">
                  </div>
                  <div class="col-md-7">
                    <label class="form-label fw-bold text-secondary small">PERIODE & PEMBIMBING</label>
                    <div class="border rounded p-2 bg-light mb-2 small">
                      <strong>{{ data.currentActivePeriode.title }}</strong>
                      <div class="d-flex justify-content-between mt-1">
                        <span>Deadline: <span class="badge bg-danger">{{ dateFormater.formatDateTime(data.currentActivePeriode.tanggal) }}</span></span>
                      </div>
                    </div>
                    <select v-model="data.post.dosenId" class="form-select" required>
                      <option value="" selected hidden>-- Pilih Usulan Dosen Pembimbing --</option>
                      <option v-for="(dosen, index) in data.dosens" :key="index" :value="dosen.id">
                        {{ getDosenName(dosen) }}
                      </option>
                    </select>
                  </div>
                </div>

                <div class="mb-4">
                  <label class="form-label fw-bold">Dokumen Proposal <span class="text-danger">*</span></label>
                  <input type="file" @change="getUserFile($event)" class="form-control" id="inputfile" accept=".docx,.doc,.pdf,.odt,text/plain" required>
                  <div class="form-text">Unggah file proposal lengkap (PDF/DOCX).</div>
                </div>

                <div class="mb-3">
                  <label class="form-label fw-bold">Judul Proposal <span class="text-danger">*</span></label>
                  <InputTextArea name="judul" v-model="data.post.judul" :placeholderText="'Tuliskan judul proposal sesuai kaidah penulisan.'"/>
                </div>

                <div class="mb-3"><label class="form-label fw-bold">Latar Belakang <span class="text-danger">*</span></label><MyCKEditor v-model:editorData="data.post.latarBelakang" name="latarBelakang" :placeholder="'Uraikan latar belakang masalah...'"/></div>
                <div class="mb-3"><label class="form-label fw-bold">Rumusan Masalah <span class="text-danger">*</span></label><MyCKEditor v-model:editorData="data.post.rumusan" name="rumusan" :placeholder="'Rumuskan masalah penelitian...'"/></div>
                <div class="mb-3"><label class="form-label fw-bold">Batasan Masalah <span class="text-danger">*</span></label><MyCKEditor v-model:editorData="data.post.batasan" name="batasan" :placeholder="'Batasan masalah...'"/></div>
                <div class="mb-3"><label class="form-label fw-bold">Tujuan Penelitian <span class="text-danger">*</span></label><MyCKEditor v-model:editorData="data.post.tujuan" name="tujuan" :placeholder="'Tujuan yang ingin dicapai...'"/></div>
                <div class="mb-3"><label class="form-label fw-bold">Manfaat Penelitian <span class="text-danger">*</span></label><MyCKEditor v-model:editorData="data.post.manfaat" name="manfaat" :placeholder="'Manfaat penelitian...'"/></div>
                <div class="mb-4"><label class="form-label fw-bold">Daftar Pustaka <span class="text-danger">*</span></label><MyCKEditor v-model:editorData="data.post.referensi" name="referensi" :placeholder="'Referensi utama...'"/></div>

                <div v-if="areErrorsExists" class="alert alert-danger d-flex align-items-center" role="alert">
                  <i class="bi bi-exclamation-triangle-fill me-2"></i>
                  <div>Mohon lengkapi semua field bertanda bintang (*) sebelum melanjutkan.</div>
                </div>

                <div class="d-flex gap-2 mt-4 pt-3 border-top">
                  <button 
                    type="button" 
                    class="btn btn-outline-primary px-4 d-flex align-items-center"
                    @click="checkSimilarity()"
                    :disabled="data.isCheckingSimilarity || areErrorsExists"
                  >
                    <span v-if="data.isCheckingSimilarity" class="spinner-border spinner-border-sm me-2"></span>
                    <i v-else class="bi bi-robot me-2"></i>
                    {{ data.isCheckingSimilarity ? 'Menganalisis...' : 'Cek Kemiripan & Rekomendasi' }}
                  </button>

                  <div class="flex-grow-1"></div>

                  <button type="submit" class="btn btn-success px-5 fw-bold shadow-sm" :disabled="data.isSubmitting || areErrorsExists">
                    {{ !data.isSubmitting ? 'SIMPAN PROPOSAL' : 'Menyimpan...' }}
                  </button>
                </div>

              </form>
            </div>
          </div>
        </div>

<div class="col-lg-4">
      <div
        v-if="hasSimilarityResult"
        class="card border-0 shadow-sm similarity-panel"
        :style="panelStyle"
      >
        <div class="card-header bg-primary text-white py-2 d-flex justify-content-between align-items-center flex-shrink-0">
          <strong><i class="bi bi-stars"></i> Hasil Analisis AI</strong>
          <span class="badge bg-light text-primary" style="font-size:0.7rem">ScripTi-SBERT</span>
        </div>

        <div class="card-body similarity-panel-body p-3">
          
          <div v-if="similarityResult.dosenRecommendations" class="mb-4">
            <h6 class="fw-bold text-primary border-bottom pb-2 mb-2">
              <i class="bi bi-people-fill me-1"></i> Rekomendasi Pembimbing
            </h6>
            
            <div class="d-flex flex-column gap-2">
              <div 
                v-for="(rec, idx) in similarityResult.dosenRecommendations" 
                :key="idx" 
                class="card bg-light border-0"
              >
                <div class="card-body p-2">
                  <small class="text-primary fw-bold d-block mb-2 border-bottom border-primary-subtle pb-1">
                    {{ rec.topicLabel }}
                  </small>
                  
                  <div class="row g-0">
                    <div class="col-6 border-end pe-2">
                      <span class="badge bg-secondary mb-1" style="font-size: 0.6rem;">Pembimbing 1</span>
                      <ul class="mb-0 ps-3 small text-dark" style="font-size: 0.75rem;">
                        <li v-for="d in rec.dosen1Names" :key="d">{{ d }}</li>
                        <li v-if="!rec.dosen1Names || rec.dosen1Names.length === 0" class="text-muted fst-italic">-</li>
                      </ul>
                    </div>
                    
                    <div class="col-6 ps-2">
                      <span class="badge bg-secondary mb-1" style="font-size: 0.6rem;">Pembimbing 2</span>
                      <ul class="mb-0 ps-3 small text-dark" style="font-size: 0.75rem;">
                        <li v-for="d in rec.dosen2Names" :key="d">{{ d }}</li>
                        <li v-if="!rec.dosen2Names || rec.dosen2Names.length === 0" class="text-muted fst-italic">-</li>
                      </ul>
                    </div>
                  </div>

                </div>
              </div>
            </div>
          </div>

          <h6 class="fw-bold text-danger border-bottom pb-2 mb-2">
            <i class="bi bi-exclamation-triangle-fill me-1"></i> Deteksi Kemiripan
          </h6>
          
          <div class="d-flex justify-content-between align-items-center mb-2 bg-light rounded p-1 border">
            <button class="btn btn-sm btn-link text-decoration-none" @click="prevSim" :disabled="simIdx === 0">&#10094; Prev</button>
            <span class="small fw-bold text-muted">Kandidat #{{ simIdx + 1 }}</span>
            <button class="btn btn-sm btn-link text-decoration-none" @click="nextSim" :disabled="simIdx >= similarityResult.results.length - 1">Next &#10095;</button>
          </div>

          <div v-if="activeSim">
            <div class="card mb-3 border-secondary-subtle">
               <div class="card-body p-2">
                  <div class="d-flex justify-content-between align-items-start mb-1">
                    <span class="badge bg-secondary">ID: {{ activeSim.proposalId }}</span>
                    <div class="text-end lh-1">
                       <span class="fs-2 fw-bold" :class="getScoreColor(activeSim.score)">
                          {{ Math.round(activeSim.score * 100) }}%
                       </span>
                       <br><span class="text-muted" style="font-size:0.65rem">Indikasi</span>
                    </div>
                  </div>
                  <div class="p-2 bg-light rounded border border-light-subtle mt-1">
                     <strong class="d-block text-dark small mb-1" style="font-size:0.7rem">Judul Proposal:</strong>
                     <div class="text-secondary fst-italic small" style="line-height: 1.2; font-size: 0.8rem;">
                       "{{ activeSim.judul || 'Judul tidak tersedia' }}"
                     </div>
                  </div>
               </div>
            </div>

            <div class="row g-1 text-center mb-3 small">
              <div class="col-4"><div class="border p-1 rounded"><b>{{ activeSim.totalSentences }}</b><br><span style="font-size:0.65rem">Total Kalimat</span></div></div>
              <div class="col-4"><div class="border border-danger bg-danger-subtle p-1 rounded text-danger"><b>{{ activeSim.similarCount }}</b><br><span style="font-size:0.65rem">Terindikasi</span></div></div>
              <div class="col-4"><div class="border border-success bg-success-subtle p-1 rounded text-success"><b>{{ activeSim.ignoredCount }}</b><br><span style="font-size:0.65rem">Aman</span></div></div>
            </div>

            <div class="table-responsive border rounded">
              <table class="table table-sm small mb-0 table-hover">
                <thead class="table-light">
                   <tr>
                      <th width="15%" class="text-center">Skor</th>
                      <th>Detail Pasangan Kalimat</th>
                   </tr>
                </thead>
                <tbody>
                  <template v-if="activeSim.matches && activeSim.matches.length">
                    <tr v-for="(m, i) in activeSim.matches" :key="i">
                      <td class="text-center align-middle bg-white border-end">
                        <span class="badge rounded-pill" :class="m.similarity >= 0.8 ? 'bg-danger' : 'bg-warning text-dark'">
                          {{ Math.round(m.similarity*100) }}%
                        </span>
                        <div class="mt-1 text-muted fst-italic text-truncate" style="font-size: 0.6rem; max-width: 60px;">
                          {{ m.aspectInput || 'Text' }}
                        </div>
                      </td>
                      <td class="align-middle">
                        <div class="mb-2">
                          <span class="badge bg-primary-subtle text-primary border border-primary-subtle" style="font-size:0.6rem">Milik Anda</span>
                          <div class="mt-1" style="font-size:0.8rem">{{ m.inputSentence }}</div>
                        </div>
                        <div>
                          <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle" style="font-size:0.6rem">Pembanding</span>
                          <div class="mt-1 text-muted" style="font-size:0.8rem">{{ m.dbSentence }}</div>
                        </div>
                      </td>
                    </tr>
                  </template>
                  <tr v-else>
                     <td colspan="2" class="text-center py-4 text-muted">
                        <em>Tidak ada detail kalimat yang ditampilkan.</em>
                     </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

        </div>
      </div>

      <div v-else class="card w-100 bg-light border-0 h-100 d-flex align-items-center justify-content-center">
         <div class="text-center text-muted">
           <i class="bi bi-search display-4 mb-3 d-block opacity-25"></i>
           <p class="mb-0">Data kemiripan belum tersedia.</p>
         </div>
      </div>
    </div>
        </div>
    </div>

    <div class="modal fade" id="confirmation" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title fs-5">Perhatian</h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body row">
                    <span class="fs-6">Cek kembali data. Perubahan bersifat final. Yakin?</span>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-success" data-bs-dismiss="modal" @click="handleInsert()">Tambahkan</button>
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                </div>
            </div>
        </div>
    </div>

  </div>
</template>

<script setup>
// Pastikan import lengkap
import { reactive, onBeforeMount, onMounted, onBeforeUnmount, computed, ref, nextTick, watch } from 'vue';
import { router } from '../../router';
import { useRoute } from 'vue-router';
import * as yup from "yup"
import { useForm } from 'vee-validate';
import { useAuthStore } from '../../stores/auth.store';
import { useMahasiswaStore } from '../../stores/mahasiswa.store';
import { useProposalStore } from '../../stores/proposal.store';
import { useAlertStore } from '../../stores/alert.store'; 
import dateFormater from '../../helpers/date-formater';
import MyCKEditor from '../../components/texteditor/MyCKEditor.vue';
import InputTextArea from '../../components/fields/InputTextArea.vue';
import Loading from '../../components/animation/Loading.vue';

let myModal = null
const route = useRoute()
const authStore = useAuthStore()
const mahasiswaStore = useMahasiswaStore()
const proposalStore = useProposalStore()
const alertStore = useAlertStore()

// --- STATE UTAMA ---
let data = reactive({
    post:{
        nim:"", userId:"", dosenId:"", periodeId:"", judul:"",
        latarBelakang:"", rumusan:"", batasan:"", tujuan:"", referensi:"", manfaat:"",
        status:'B', file:null
    },
    penulis:{ nama:"", nim:"", status:0, ta:0 },
    publishedProposal:[],
    dosens:[],
    currentActivePeriode:null,
    similarityResult: null,      
    isCheckingSimilarity: false, 
    isFetching:true,
    isSubmitting:false
})

// --- LOGIKA PANEL KANAN & SYNC HEIGHT ---
const simIdx = ref(0)
const formRef = ref(null) // Ref ke elemen form kiri
const exactHeight = ref('auto') // Tinggi dinamis

const similarityResult = computed(() => data.similarityResult)
const hasSimilarityResult = computed(() => !!data.similarityResult)
const activeSim = computed(() => {
  if (!similarityResult.value || !similarityResult.value.results) return null
  return similarityResult.value.results[simIdx.value]
})

// Fungsi navigasi
function nextSim() { if (simIdx.value < similarityResult.value.results.length - 1) simIdx.value++ }
function prevSim() { if (simIdx.value > 0) simIdx.value-- }
function getScoreColor(s) { return s >= 0.7 ? 'text-danger' : s >= 0.4 ? 'text-warning' : 'text-success' }

function closePanel() {
  data.similarityResult = null
  simIdx.value = 0
}

// Fungsi Inisialisasi Observer
function initResizeObserver() {
  // Bersihkan observer lama jika ada
  if (resizeObserver) resizeObserver.disconnect()
  
  if (formRef.value) {
    // 1. Buat observer baru yang memantau perubahan ukuran form kiri
    resizeObserver = new ResizeObserver(() => {
      updatePanelHeight()
    })
    
    // 2. Mulai pantau elemen form
    resizeObserver.observe(formRef.value)
    
    // 3. Paksa update sekali di awal
    updatePanelHeight()
  }
}

// --- UPDATE TINGGI PANEL OTOMATIS ---
const panelStyle = computed(() => {
if (typeof window !== 'undefined' && window.innerWidth < 992) {
    return { height: 'auto' }
  }
  // Pastikan minimal height agar tidak gepeng jika form pendek
  const h = exactHeight.value === 'auto' ? 'auto' : exactHeight.value + 'px'
  return { height: h }
})

// Fungsi Update Tinggi
function updatePanelHeight() {
  if (!formRef.value) return
  const rect = formRef.value.getBoundingClientRect()
  // Tambahkan sedikit padding/buffer jika perlu, atau ambil raw height
  if (rect.height > 50) { // Safety check biar gak 0
      exactHeight.value = rect.height
  }
}

// Watcher agar tinggi update saat data berubah (misal validasi error muncul)
let resizeObserver = null
watch(() => data.similarityResult, async () => { await nextTick(); updatePanelHeight() })
watch(activeSim, async () => { await nextTick(); updatePanelHeight() })

onMounted(() => {
  window.addEventListener('resize', updatePanelHeight)
  if (formRef.value) {
    resizeObserver = new ResizeObserver(() => updatePanelHeight())
    resizeObserver.observe(formRef.value)
  }
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', updatePanelHeight)
  if (resizeObserver) resizeObserver.disconnect()
})
// -------------------------------------------


// --- VALIDASI & SUBMIT ---
const validation = yup.object({
    judul: yup.string().required(),
    latarBelakang: yup.string().required(),
    rumusan: yup.string().required(),
    batasan: yup.string().required(),
    tujuan: yup.string().required(),
    manfaat: yup.string().required(),
    referensi: yup.string().required(),
})
const {values, errors} = useForm({ validationSchema: validation })

async function handleInsert() {
    // 1. Validasi Form
    const isValid = await validation.validate(data.post) // Opsional, validasi manual
    if (areErrorsExists.value) return

    data.isSubmitting = true
    data.post.userId = authStore.user?.details.id
    data.post.periodeId = data.currentActivePeriode.id

    let formData = new FormData()
    Object.keys(data.post).forEach(
        key => formData.append(key, data.post[`${key}`])
    )

    try {
        // 2. Panggil Store
        // Pastikan store mengembalikan true/false
        const isSuccess = await proposalStore.mahasiswaAddProposal(formData)
        
        // 3. Cek Hasil
        if (isSuccess) {
            // SUKSES: Redirect ke halaman daftar proposal
            router.push('/proposalpage/mahasiswapage/proposal-mahasiswa')
        } else {
            // GAGAL: Tetap di halaman ini
            // (Pesan error sudah muncul dari alertStore di dalam proposalStore)
        }
    } catch (error) {
        console.error(error)
    } finally {
        // 4. Matikan Loading (PENTING)
        data.isSubmitting = false
    }
}

async function checkSimilarity() {
  // Validasi parsial manual
  if (!data.post.judul || !data.post.latarBelakang) {
    alertStore.error("Mohon lengkapi Judul dan Latar Belakang.")
    return
  }
  data.isCheckingSimilarity = true
  try {
    const payload = {
        nim: authStore.user.details.nim,
        judul: data.post.judul,
        latarBelakang: data.post.latarBelakang,
        rumusan: data.post.rumusan,
        batasan: data.post.batasan,
        tujuan: data.post.tujuan,
        manfaat: data.post.manfaat,
        referensi: data.post.referensi,
        dosenId: data.post.dosenId ? parseInt(data.post.dosenId) : null
    }
    const success = await proposalStore.mahasiswaCheckDraftSimilarity(payload)
    if (success) {
        data.similarityResult = proposalStore.lastSimilarityResult
        await nextTick()
        updatePanelHeight() // Paksa update tinggi setelah panel muncul
    }
  } catch (e) {
    console.error(e)
    alertStore.error('Terjadi kesalahan sistem.')
  } finally {
    data.isCheckingSimilarity = false
  }
}


function openModal(){ if(!areErrorsExists.value) myModal.show() }
function getDosenName(d){
    let n = d.nama
    if (d.gelarDepan) n = d.gelarDepan + " " + n
    if (d.gelar) n += ", " + d.gelar
    return n
}
function getUserFile(e){
    if(e.target.files && e.target.files[0]) data.post.file = e.target.files[0]
}

const areErrorsExists = computed(() => errors.value.penulis||errors.value.judul||errors.value.latarBelakang||errors.value.rumusan||errors.value.batasan||errors.value.tujuan||errors.value.referensi)
const isMahasiswaAktif = computed(() => data.penulis.status==1 && data.penulis.ta==1)
const submitTimeOver = computed(() => {
    let today = dateFormater.convertToWIB(new Date())
    let end = new Date(data.currentActivePeriode.tanggal)
    return end < today && route.fullPath.includes('/mahasiswapage/')
})
const alreadySubmitting = computed(() => {
    // SAFETY CHECK: Pastikan data.publishedProposal adalah Array
    if (!data.publishedProposal || !Array.isArray(data.publishedProposal)) {
        return false
    }

    for (let p of data.publishedProposal) {
        // Pastikan data periode ada sebelum akses .id
        if (p.periodes && data.currentActivePeriode && p.periodes.id === data.currentActivePeriode.id) {
            return true
        }
    }
    return false
})
// WATCHER PENTING: Pantau saat loading selesai
watch(
  () => data.isFetching, 
  async (newVal) => {
    if (newVal === false) {
      // Tunggu DOM selesai dirender setelah v-if berubah
      await nextTick()
      // Baru pasang observer
      initResizeObserver()
    }
  }
)

// WATCHER TAMBAHAN: Pantau saat panel kemiripan muncul/hilang
watch(
  () => data.similarityResult, 
  async (newVal) => {
    if (newVal) {
      await nextTick()
      updatePanelHeight()
      // Re-init observer jaga-jaga layout geser
      initResizeObserver() 
    }
  }
)

// Ganti onBeforeMount dengan onMounted
onMounted(async () => {
    try {
        // 1. Load Data Dosen
        await mahasiswaStore.getAllDosens()
        if (mahasiswaStore.dosens.length > 0) {
            data.dosens = mahasiswaStore.dosens
        }

        // 2. Load Periode Aktif
        let activePeriodeExists = await mahasiswaStore.getCurrentActivePeriode()
        if (activePeriodeExists && mahasiswaStore.periodesActive != 'ERROR') {
            data.currentActivePeriode = mahasiswaStore.periodesActive[0]
        }

        // 3. Load Data Mahasiswa (Penulis)
        await authStore.findUserByNim(authStore.user.details.nim)
        if (authStore.account_info) {
            data.penulis = authStore.account_info
            data.post.nim = authStore.user.details.nim
        }

        // 4. Load Proposal Sebelumnya (untuk cek already submitting)
        if (proposalStore.proposalsByMahasiswa.length == 0) {
            await proposalStore.mahasiswaGetAllProposal(authStore.user.details.nim)
        }
        // data.publishedProposal = proposalStore.proposalsByMahasiswa
        data.publishedProposal = Array.isArray(proposalStore.proposalsByMahasiswa) 
            ? proposalStore.proposalsByMahasiswa 
            : []

        // 5. Init Modal (Sekarang aman karena DOM sudah dirender)
        const modalElement = document.getElementById('confirmation')
        if (modalElement) {
            myModal = new bootstrap.Modal(modalElement)
        }

    } catch (error) {
        console.error("Terjadi error saat memuat data:", error)
        // Opsional: Tampilkan notifikasi error
    } finally {
        // 6. Matikan Loading (Apapun yang terjadi, loading harus berhenti)
        data.isFetching = false
        
        // Update panel height jika perlu
        updatePanelHeight()
    }
})
</script>

<style scoped>
.warn{ color: crimson; }
.transition-all { transition: all 0.3s ease; }
.bg-gradient-primary { background: linear-gradient(45deg, #0d6efd, #0a58ca); }

/* Panel Kanan Fixed Height */
.similarity-panel {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.similarity-panel-body {
  flex: 1; 
  overflow-y: auto;
  overflow-x: hidden;
}

/* Custom Scrollbar */
.similarity-panel-body::-webkit-scrollbar { width: 6px; }
.similarity-panel-body::-webkit-scrollbar-track { background: #f1f1f1; }
.similarity-panel-body::-webkit-scrollbar-thumb { background: #c1c1c1; border-radius: 3px; }
.similarity-panel-body::-webkit-scrollbar-thumb:hover { background: #a8a8a8; }
</style>