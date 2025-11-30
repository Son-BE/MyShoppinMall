from fastapi import FastAPI
from pydantic import BaseModel
from app.services.embedding_service import embedding_service
from app.services.retrieval_service import retrieval_service
from app.services.llm_service import llm_service

app = FastAPI(title="SonStarMall AI Service")

class ChatRequest(BaseModel):
    message: str

class ChatResponse(BaseModel):
    answer: str
    related_products: list

@app.get("/")
def root():
    return {"message": "AI Service is running"}

@app.post("/embeddings/sync")
def sync_embeddings():
    """상품 데이터를 임베딩으로 동기화"""
    count = embedding_service.sync_all_items()
    return {"message": f"{count}개 상품 임베딩 완료"}

@app.get("/search")
def search_products(query: str, top_k: int = 5):
    """유사 상품 검색"""
    results = retrieval_service.search_similar_products(query, top_k)
    return {"query": query, "results": results}

@app.post("/chat", response_model=ChatResponse)
def chat(request: ChatRequest):
    """챗봇 대화"""
    # 1. 관련 상품 검색
    products = retrieval_service.search_similar_products(request.message, top_k=5)

    # 2. LLM으로 답변 생성
    answer = llm_service.generate_response(request.message, products)

    return ChatResponse(answer=answer, related_products=products)

@app.get("/health")
def health_check():
    return {"status": "healthy"}