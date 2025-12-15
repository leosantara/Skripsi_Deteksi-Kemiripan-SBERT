from fastapi import APIRouter, HTTPException
from DTO.request_dto import ProposalPayload
from DTO.response_dto import SimilarityResponse
from Services.proposal_similarity_service import ProposalSimilarityService

# --- PASTIKAN BARIS INI ADA ---
router = APIRouter()
# ------------------------------

@router.post("/similarity/check", response_model=SimilarityResponse)
def check_similarity(payload: ProposalPayload, top_k: int = 5):
    try:
        return ProposalSimilarityService.check_similarity(payload, top_k)
    except Exception as e:
        print(f"Error in Similarity Check: {e}")
        raise HTTPException(status_code=500, detail=str(e))