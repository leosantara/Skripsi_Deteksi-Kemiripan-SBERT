<template>
  <div class="container-fluid p-0">
    <h1 class="h3 mb-1">Update Proposal</h1>
    <h6 class="text-muted mb-3">Silakan update data proposal yang telah diupload. Hanya file revisi yang boleh diupdate!</h6>

    <div v-if="!mustBeRevised && !data.isFetching" class="alert alert-warning shadow-sm" role="alert">
        <i class="bi bi-exclamation-triangle-fill me-2"></i>
        Proposal Anda belum dievaluasi. Silakan upload file revisi setelah dosen/admin membuka akses!
    </div>

    <hr class="border-secondary opacity-25 mb-4">

    <div class="mb-3">
        <button class="btn btn-outline-dark rounded-pill px-4" @click="router.push('/proposalpage/mahasiswapage/proposal-mahasiswa')" type="button">
            &#129092; Kembali
        </button>
    </div>

    <Loading v-bind:isFetching="data.isFetching" />

    <div v-if="!data.isFetching && !data.currentProposal" class="card border-danger shadow-sm p-5 text-center">
        <h2 class="text-danger">Data Tidak Ditemukan</h2>
        <h6 class="text-muted">
            Maaf, data proposal dengan ID {{ route.params.propodalID }} tidak dapat kami temukan.
        </h6>
    </div>

    <div v-if="!data.isFetching && data.currentProposal" class="row g-4 align-items-stretch">
      
      <div :class="hasSimilarityResult ? 'col-lg-8' : 'col-12'" class="transition-all d-flex flex-column">
        <div ref="formRef" class="card border-0 shadow-sm h-100">
           <div class="card-body p-4">
                <form ref="formEl" v-on:submit.prevent="onSubmit()" action="#" method="POST" enctype="multipart/form-data" id="formProposal">
                    <div class="d-flex align-self-center justify-content-start gap-3 mb-3">
                        <div class="flex-shrink-0" style="width: 250px;">
                            <input type="text" class="form-control mb-2" disabled v-model="data.post.nim" placeholder="NIM" style="height: 42px;" />
                            
                            <div class="border rounded p-2 bg-light small">
                                <strong class="d-block mb-1 text-primary">Periode Proposal</strong>
                                <select disabled v-model="data.currentActivePeriode" class="form-select form-select-sm mb-2" required>
                                    <option :value="null" selected hidden>Pilih Periode</option>
                                    <option v-for="(periode, index) in data.allPeriodes" :key="index" :value="periode">
                                        {{ `${dateFormater.formatDate(periode.tglAwal)} s/d ${dateFormater.formatDate(periode.tglAkhir)}` }}
                                    </option>
                                </select>
                                <div v-if="data.currentActivePeriode">
                                    <div class="fw-bold text-dark">{{ data.currentActivePeriode.title || "Belum ada title." }}</div>
                                    <div class="mt-1">Deadline: <span class="badge bg-danger">{{ dateFormater.formatDateTime(data.currentActivePeriode.tanggal) }}</span></div>
                                </div>
                            </div>
                        </div>

                        <div class="flex-grow-1">
                            <input v-model="data.penulis.nama" type="text" disabled style="height: 42px;" class="form-control mb-2" placeholder="Nama Mahasiswa" />
                            <div class="warn mb-2">{{ errors.penulis }}</div>

                            <label for="inputfile" class="fw-bold small text-uppercase text-secondary">Dokumen Proposal <span class="text-danger">*</span></label>
                            <input v-if="!route.fullPath.includes('/detail/')" required type="file" @change="getUserFile($event)" class="form-control mb-1" id="inputfile" accept=".docx, .doc, .pdf, .odt, text/plain" />

                            <div class="mb-3">
                                <span class="small text-muted">File Terakhir: </span>
                                <button v-if="data.fileExists" class="btn btn-link btn-sm p-0 text-decoration-none" @click.prevent="handleDownload()">
                                    <i class="bi bi-file-earmark-text"></i> {{ data.currentProposal.filename }}
                                </button>
                                <span v-else class="badge bg-danger">File Tidak Ditemukan</span>
                            </div>

                            <label class="fw-bold small text-uppercase text-secondary">Usulan Pembimbing <span class="text-danger">*</span></label>
                            <select disabled v-model="data.post.dosenId" class="form-select" required>
                                <option value="" selected hidden>Pilih Dosen Pendamping</option>
                                <option v-for="(dosen, index) in data.dosens" :key="index" :value="dosen.id">{{ getDosenName(dosen) }}</option>
                            </select>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="fw-bold">Judul <span class="text-danger">*</span></label>
                        <InputTextArea name="judul" v-model="data.post.judul" :disabled="true" :placeholderText="'Judul proposal...'"/>
                    </div>

                    <div class="mb-3"><label class="fw-bold">Latar Belakang <span class="text-danger">*</span></label><MyCKEditor v-model:editorData="data.post.latarBelakang" name="latarBelakang" :disabled="true" :placeholder="'Isi Latar Belakang'"/></div>
                    <div class="mb-3"><label class="fw-bold">Rumusan Masalah <span class="text-danger">*</span></label><MyCKEditor v-model:editorData="data.post.rumusan" name="rumusan" :disabled="true" :placeholder="'Isi Rumusan Masalah'"/></div>
                    <div class="mb-3"><label class="fw-bold">Batasan Masalah <span class="text-danger">*</span></label><MyCKEditor v-model:editorData="data.post.batasan" name="batasan" :disabled="true" :placeholder="'Isi Batasan Masalah'"/></div>
                    <div class="mb-3"><label class="fw-bold">Tujuan Penelitian <span class="text-danger">*</span></label><MyCKEditor v-model:editorData="data.post.tujuan" name="tujuan" :disabled="true" :placeholder="'Isi Tujuan'"/></div>
                    <div class="mb-3"><label class="fw-bold">Manfaat Penelitian <span class="text-danger">*</span></label><MyCKEditor v-model:editorData="data.post.manfaat" name="manfaat" :disabled="true" :placeholder="'Isi Manfaat'"/></div>
                    <div class="mb-4"><label class="fw-bold">Daftar Pustaka <span class="text-danger">*</span></label><MyCKEditor v-model:editorData="data.post.referensi" name="referensi" :disabled="true" :placeholder="'Isi Referensi'"/></div>

                    <div class="d-flex justify-content-end border-top pt-3">
                        <input v-if="mustBeRevised" type="submit" :value="!isSubmitting ? 'UPDATE PROPOSAL' : 'Menyimpan...'" :disabled="isSubmitting" class="btn btn-success fw-bold px-5" />
                    </div>
                </form>
                <div v-if="areErrorsExists" class="alert alert-danger mt-3"><i class="bi bi-exclamation-circle"></i> Mohon lengkapi semua data!</div>
           </div>
        </div>
      </div>

      <div v-if="hasSimilarityResult" class="col-lg-4 d-flex flex-column">
          <div class="card border-0 shadow-sm similarity-panel h-100" :style="panelStyle">
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
                   <div v-for="(rec, idx) in similarityResult.dosenRecommendations" :key="idx" class="card bg-light border-0">
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
                     <thead class="table-light"><tr><th width="15%" class="text-center">Skor</th><th>Detail Pasangan Kalimat</th></tr></thead>
                     <tbody>
                       <template v-if="activeSim.matches && activeSim.matches.length">
                         <tr v-for="(m, i) in activeSim.matches" :key="i">
                           <td class="text-center align-middle bg-white border-end">
                             <span class="badge rounded-pill" :class="m.similarity >= 0.8 ? 'bg-danger' : 'bg-warning text-dark'">{{ Math.round(m.similarity*100) }}%</span>
                             <div class="mt-1 text-muted fst-italic text-truncate" style="font-size: 0.6rem; max-width: 60px;">{{ m.aspectInput || 'Text' }}</div>
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
                       <tr v-else><td colspan="2" class="text-center py-4 text-muted"><em>Tidak ada detail kalimat yang ditampilkan.</em></td></tr>
                     </tbody>
                   </table>
                 </div>
               </div>
            </div>
          </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onBeforeMount, onMounted, onBeforeUnmount, computed, nextTick, watch } from 'vue'
import { router } from '../../router'
import { useRoute } from 'vue-router'
import * as yup from 'yup'
import { useForm } from 'vee-validate'
import { useAuthStore } from '../../stores/auth.store'
import { useAlertStore } from '../../stores/alert.store'
import { useProposalStore } from '../../stores/proposal.store'
import dateFormater from '../../helpers/date-formater'

import MyCKEditor from '../../components/texteditor/MyCKEditor.vue'
import Loading from '../../components/animation/Loading.vue'
import InputTextArea from '../../components/fields/InputTextArea.vue'

const route = useRoute()
const proposalStore = useProposalStore()
const alertStore = useAlertStore()
const authStore = useAuthStore()

let data = reactive({
  post: {
    nim: '', userId: '', dosenId: '', periodeId: '', judul: '',
    latarBelakang: '', rumusan: '', batasan: '', tujuan: '', referensi: '', manfaat: '',
    status: '', file: null, revisiDari: '', uploadRevisi: '',
    similarityResult: null
  },
  penulis: { nama: '', nim: '', status: 0, ta: 0 },
  dosens: [],
  allPeriodes: [],
  currentActivePeriode: null,
  currentProposal: null,
  currentFile: null,
  fileExists: false,
  isFetching: true
})

// --- PANEL LOGIC ---
const simIdx = ref(0)
const formRef = ref(null)
const exactHeight = ref('auto')
let resizeObserver = null

const similarityResult = computed(() => data.post.similarityResult)
const hasSimilarityResult = computed(() => !!data.post.similarityResult)
const activeSim = computed(() => {
  if (!similarityResult.value || !similarityResult.value.results) return null
  return similarityResult.value.results[simIdx.value]
})

function nextSim() { if (simIdx.value < similarityResult.value.results.length - 1) simIdx.value++ }
function prevSim() { if (simIdx.value > 0) simIdx.value-- }
function getScoreColor(s) { return s >= 0.7 ? 'text-danger' : s >= 0.4 ? 'text-warning' : 'text-success' }

// --- SYNC HEIGHT ---
const panelStyle = computed(() => {
  if (typeof window !== 'undefined' && window.innerWidth < 992) return { height: 'auto' }
  return { height: exactHeight.value === 'auto' ? 'auto' : exactHeight.value + 'px' }
})

function updatePanelHeight() {
  if (!formRef.value) return
  const rect = formRef.value.getBoundingClientRect()
  if (rect.height > 50) exactHeight.value = rect.height
}

function initResizeObserver() {
  if (resizeObserver) resizeObserver.disconnect()
  if (formRef.value) {
    resizeObserver = new ResizeObserver(() => updatePanelHeight())
    resizeObserver.observe(formRef.value)
    updatePanelHeight()
  }
}

watch(() => data.isFetching, async (val) => { if (!val) { await nextTick(); initResizeObserver() } })
watch(() => data.post.similarityResult, async (val) => { if (val) { await nextTick(); initResizeObserver() } })
watch(() => data.currentProposal, async () => { await nextTick(); initResizeObserver() }) // Tambahan untuk update proposal

onBeforeUnmount(() => { if (resizeObserver) resizeObserver.disconnect() })

// --- VALIDASI & SUBMIT ---
const validation = yup.object({
  judul: yup.string().required(),
  latarBelakang: yup.string().required(),
  rumusan: yup.string().required(),
  batasan: yup.string().required(),
  tujuan: yup.string().required(),
  manfaat: yup.string().required(),
  referensi: yup.string().required()
})

const { isSubmitting, handleSubmit, errors } = useForm({ validationSchema: validation })

const onSubmit = handleSubmit(
  async () => {
    data.post.status = data.currentProposal.status
    data.post.uploadRevisi = 'T'

    const formData = new FormData()
    Object.keys(data.post).forEach((key) => {
      if (key === 'similarityResult') return
      formData.append(key, data.post[key])
    })

    const updated = await proposalStore.mahasiswaUpdateProposal(route.params.proposalID, formData)
    if (updated) {
      router.push('/proposalpage/mahasiswapage/proposal-mahasiswa')
    }
  },
  () => { alertStore.error('Mohon lengkapi semua data!') }
)

function getDosenName(dosen) {
  let name = dosen.nama
  if (dosen.gelarDepan) name = dosen.gelarDepan + ' ' + name
  if (dosen.gelar) name += ', ' + dosen.gelar
  return name
}

function getUserFile(event) {
  if (event.target.files && event.target.files[0]) data.post.file = event.target.files[0]
}

async function handleDownload() {
  const filename = data.currentProposal.filename
  await proposalStore.adminGetProposalFile(route.params.proposalID, filename)
}

const areErrorsExists = computed(() => errors.value.penulis || errors.value.judul || errors.value.latarBelakang || errors.value.rumusan || errors.value.batasan || errors.value.tujuan || errors.value.referensi)

const mustBeRevised = computed(() => data.currentProposal ? data.currentProposal.status == 'R' : false)

onBeforeMount(async () => {
  data.isFetching = true
  const proposalId = route.params.proposalID
  const nim = authStore.user.details.nim

  // 1. Load Proposal
  await proposalStore.mahasiswaGetProposalById(proposalId, nim)

  if (proposalStore.proposal) {
    data.currentProposal = proposalStore.proposal
    // Map data proposal ke form
    for (const key in data.post) {
      if (!Object.prototype.hasOwnProperty.call(data.post, key)) continue
      if (key === 'nim') data.post.nim = data.currentProposal.mahasiswa?.nim ?? ''
      else if (key === 'dosenId') {
        data.post.dosenId = data.currentProposal.dosenId?.id ?? ''
        data.dosens = data.currentProposal.dosenId ? [data.currentProposal.dosenId] : []
      } else if (key === 'userId') data.post.userId = authStore.user?.details.id ?? ''
      else if (key === 'periodeId') {
        data.post.periodeId = data.currentProposal.periodes?.id ?? ''
        data.currentActivePeriode = data.currentProposal.periodes ?? null
        data.allPeriodes = data.currentProposal.periodes ? [data.currentProposal.periodes] : []
      } else if (key !== 'similarityResult') {
        data.post[key] = data.currentProposal[key] ?? ''
      }
    }
  }

  // 2. Hitung Similarity (Load hasil existing)
  await proposalStore.mahasiswaGetProposalSimilarity(route.params.proposalID, authStore.user.details.nim)
  data.post.similarityResult = proposalStore.lastSimilarityResult || null

  // 3. Data Penulis
  await authStore.findUserByNim(nim)
  if (authStore.account_info) data.penulis = authStore.account_info

  data.fileExists = await proposalStore.mahasiswaCheckIfProposalExists(proposalId)
  data.isFetching = false
})
</script>

<style scoped>
.warn{ color: crimson; }
.transition-all { transition: all 0.3s ease; }
.bg-gradient-primary { background: linear-gradient(45deg, #0d6efd, #0a58ca); }

/* Panel Styling */
.similarity-panel {
  display: flex; flex-direction: column; overflow: hidden;
}
.similarity-panel-body {
  flex: 1; overflow-y: auto; overflow-x: hidden;
}
.similarity-panel-body::-webkit-scrollbar { width: 6px; }
.similarity-panel-body::-webkit-scrollbar-track { background: #f1f1f1; }
.similarity-panel-body::-webkit-scrollbar-thumb { background: #c1c1c1; border-radius: 3px; }
</style>  