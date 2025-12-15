from pydantic import BaseModel
from typing import List, Optional

class SimilarityMatch(BaseModel):
    aspectInput: str
    inputSentence: str
    aspectDb: str
    dbSentence: str
    similarity: float

class DosenRecommendation(BaseModel):
    clusterId: int
    topicLabel: str 
    ratio: float
    recommendedDosen1Ids: List[int]
    recommendedDosen2Ids: List[int]

class SimilarityProposal(BaseModel):
    proposalId: int
    score: float
    judul: str
    totalSentences: int
    similarCount: int
    ignoredCount: int
    matches: Optional[List[SimilarityMatch]] = []

class SimilarityResponse(BaseModel):
    queryProposalId: int
    modelName: str
    topK: int
    results: List[SimilarityProposal]
    dosenRecommendations: Optional[List[DosenRecommendation]] = None