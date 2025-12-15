from fastapi import APIRouter, BackgroundTasks
from typing import List
from DTO.request_dto import ProposalRawData
from Services.reindexing_service import ReindexingService
from Services.progress_bar_service import ProgressBarService
from Core.engine import ENGINE

# --- PASTIKAN BARIS INI ADA ---
router = APIRouter()
# ------------------------------

@router.post("/system/reindex")
def trigger_reindex(payload: List[ProposalRawData], background_tasks: BackgroundTasks):
    # Jalankan di background
    background_tasks.add_task(ReindexingService.run_reindexing, payload)
    return {"status": "success", "message": "Re-indexing started."}

@router.get("/system/indexing-status")
def get_status():
    return ProgressBarService.get_status()

@router.get("/system/clusters")
def get_clusters():
    return ENGINE.cluster_info