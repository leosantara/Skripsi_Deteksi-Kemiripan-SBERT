from pydantic import BaseModel
from typing import Optional

class ProposalPayload(BaseModel):
    proposal_id: int
    judul: str
    latar_belakang: Optional[str] = ""
    rumusan: Optional[str] = ""
    tujuan: Optional[str] = ""

class ProposalRawData(BaseModel):
    proposal_id: int
    judul: str
    latar_belakang: str
    rumusan: str
    tujuan: str
    id_dosen_1: Optional[int] = None
    id_dosen_2: Optional[int] = None