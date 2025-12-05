from fastapi import FastAPI
from pydantic import BaseModel, Field
from typing import Optional, List
from app.services.embedding_service import embedding_service
from app.services.retrieval_service import retrieval_service
from app.services.llm_service import llm_service
from app.services.chat_history_service import chat_history_service
from app.routers import classify

app = FastAPI(title="SonStarMall AI Service")

# 라우터 등록
app.include_router(classify.router)

class ChatRequest(BaseModel):
    message: str
    session_id: Optional[str] = Field(None, alias="sessionId")

    class Config:
        populate_by_name = True

class RelatedProduct(BaseModel):
    productId: int
    productName: str
    category: str
    similarity: float

class ChatResponse(BaseModel):
    answer: str
    relatedProducts: List[RelatedProduct]

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
    session_id = request.session_id or "default"

    # 1. 대화 히스토리 조회
    history = chat_history_service.get_history(session_id)

    # 2. 관련 상품 검색
    products = retrieval_service.search_similar_products(request.message, top_k=5)

    # 3. LLM으로 답변 생성
    answer = llm_service.generate_response(request.message, products, history)

    # 4. 대화 저장
    chat_history_service.save_message(session_id, "user", request.message)
    chat_history_service.save_message(session_id, "assistant", answer)

    related_products = [
        RelatedProduct(
            productId=p["product_id"],
            productName=p["product_name"],
            category=p["category"],
            similarity=p.get("similarity", 0.0)
        )
        for p in products
    ]

    return ChatResponse(answer=answer, relatedProducts=related_products)

@app.get("/chat/history/{session_id}")
def get_chat_history(session_id: str):
    """대화 히스토리 조회"""
    history = chat_history_service.get_history(session_id, limit=20)
    return {"session_id": session_id, "history": history}

@app.get("/health")
def health_check():
    return {"status": "healthy"}