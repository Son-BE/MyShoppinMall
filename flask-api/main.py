# flask-api/main.py
from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
import uvicorn
import asyncio
import json
import os
from datetime import datetime
import logging

# 환경변수 설정 (순환 참조 방지)
try:
    from config import config
    CONFIG_AVAILABLE = True
except ImportError:
    print("⚠️ config 모듈을 불러올 수 없습니다. 환경변수를 직접 사용합니다.")
    CONFIG_AVAILABLE = False

# 기존 클래스 임포트 (지연 임포트로 순환 참조 방지)
recommender_instance = None
data_processor_instance = None

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="SonStar 추천 시스템 API",
    description="SonStar 쇼핑몰 추천 시스템 REST API",
    version="1.0.0"
)

# CORS 설정 (SpringBoot 서버 호출용)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080", "http://localhost:3000", "http://shop-app:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Pydantic 모델들
class RecommendationRequest(BaseModel):
    user_id: int
    num_recommendations: int = 10
    algorithm: str = "hybrid"

class SessionRecommendationRequest(BaseModel):
    user_id: int
    recent_views: Optional[List[int]] = None
    current_cart: Optional[List[int]] = None
    num_recommendations: int = 5

class SimilarItemsRequest(BaseModel):
    item_ids: List[int]
    num_recommendations: int = 5

class CategoryRecommendationRequest(BaseModel):
    category: str
    num_recommendations: int = 10

class ItemInfo(BaseModel):
    item_id: int
    item_name: str
    price: float
    category: str
    sub_category: Optional[str] = None
    gender: Optional[str] = None
    style: Optional[str] = None
    item_rating: float
    popularity_score: float
    recommendation_score: float
    recommendation_reason: str
    image_url: Optional[str] = ""
    description: Optional[str] = ""

class RecommendationResponse(BaseModel):
    success: bool
    message: str
    user_id: int
    algorithm: str
    recommendations: List[ItemInfo]
    timestamp: str

# 시스템 초기화
@app.on_event("startup")
async def startup_event():
    """서버 시작시 추천 시스템 초기화"""
    global recommender_instance, data_processor_instance

    logger.info("추천 시스템 초기화 시작...")

    # 환경변수 정보 출력
    if CONFIG_AVAILABLE:
        logger.info(f"MySQL 연결 정보: {config.MYSQL_HOST}:{config.MYSQL_PORT}")
        logger.info(f"Redis 연결 정보: {config.REDIS_HOST}:{config.REDIS_PORT}")
        logger.info(f"데이터 디렉토리: {config.DATA_DIR}")

    try:
        # 모듈을 여기서 임포트 (순환 참조 방지)
        from DatabaseDataProcessor import SonStarDataProcessor, run_sonstar_data_preprocessing
        from recommend_Algorithm import SonStarRecommendationSystem

        # 데이터 디렉토리 생성
        if CONFIG_AVAILABLE:
            os.makedirs(config.DATA_DIR, exist_ok=True)
        else:
            os.makedirs("./data", exist_ok=True)

        # 데이터 프로세서 초기화
        data_processor_instance = SonStarDataProcessor()

        # 추천 시스템 초기화
        recommender_instance = SonStarRecommendationSystem()

        # 전처리된 데이터 확인 및 로드
        preprocessed_file = None
        if CONFIG_AVAILABLE:
            preprocessed_file = config.get_file_path("preprocessed_items.csv")
        else:
            preprocessed_file = "./data/preprocessed_items.csv"

        if not os.path.exists(preprocessed_file):
            logger.info("전처리된 데이터가 없습니다. 데이터 전처리를 실행합니다...")
            try:
                # 데이터 전처리 실행
                run_sonstar_data_preprocessing()

                # 데이터 로드 시도
                if recommender_instance.load_preprocessed_data():
                    logger.info("데이터 전처리 및 로드 완료!")
                else:
                    logger.warning("데이터 전처리는 완료했지만 로드에 실패했습니다.")

            except Exception as preprocessing_error:
                logger.error(f"데이터 전처리 실패: {preprocessing_error}")
                logger.info("기본 추천 시스템으로 동작합니다.")
        else:
            logger.info("기존 전처리 데이터를 로드합니다...")
            if not recommender_instance.load_preprocessed_data():
                logger.warning("전처리된 데이터 로드 실패. 재전처리를 시도합니다.")
                try:
                    run_sonstar_data_preprocessing()
                    recommender_instance.load_preprocessed_data()
                except Exception as e:
                    logger.error(f"재전처리 실패: {e}")

        logger.info("추천 시스템 초기화 완료!")

    except Exception as e:
        logger.error(f"추천 시스템 초기화 실패: {e}")
        # 초기화 실패해도 서버는 시작 (기본 추천으로 동작)
        recommender_instance = None
        logger.info("기본 모드로 서버를 시작합니다.")

# 헬스체크 엔드포인트
@app.get("/health")
async def health_check():
    """시스템 상태 확인"""
    # 추천 시스템이 완전히 초기화되지 않아도 healthy로 응답
    status_info = {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "message": "서비스가 정상 작동 중입니다.",
        "services": {
            "recommender": recommender_instance is not None,
            "data_processor": data_processor_instance is not None
        }
    }

    # 설정 정보 추가
    if CONFIG_AVAILABLE:
        status_info["config"] = {
            "mysql_host": f"{config.MYSQL_HOST}:{config.MYSQL_PORT}",
            "redis_host": f"{config.REDIS_HOST}:{config.REDIS_PORT}",
            "data_dir": config.DATA_DIR
        }

    # 데이터 로드 상태 확인
    if recommender_instance is not None:
        try:
            data_loaded = {
                "items": hasattr(recommender_instance, 'items_df') and recommender_instance.items_df is not None,
                "members": hasattr(recommender_instance, 'members_df') and recommender_instance.members_df is not None,
                "interactions": hasattr(recommender_instance, 'interactions_df') and recommender_instance.interactions_df is not None
            }
            status_info["data_loaded"] = data_loaded

            # 데이터가 하나도 없으면 partial 상태로 변경
            if not any(data_loaded.values()):
                status_info["status"] = "partial"
                status_info["message"] = "데이터 로드 중입니다."

        except Exception as e:
            logger.warning(f"데이터 상태 확인 중 오류: {e}")
            status_info["status"] = "partial"
            status_info["message"] = "데이터 상태 확인 실패"

    return status_info

# 인기 상품 조회 (중복 제거)
@app.get("/api/recommendations/popular")
async def get_popular_items(num_recommendations: int = 10):
    """인기 상품 목록"""

    if recommender_instance is None:
        logger.warning("추천 시스템이 초기화되지 않았습니다. 기본 응답을 제공합니다.")

        # 기본 인기 상품 응답 (더미 데이터)
        dummy_items = []
        for i in range(min(num_recommendations, 5)):
            dummy_items.append({
                "item_id": i + 1,
                "item_name": f"인기상품 {i + 1}",
                "price": 50000.0 + (i * 10000),
                "category": "CLOTHING",
                "sub_category": "M_TSHIRT",
                "gender": "M",
                "style": "CASUAL",
                "item_rating": 4.5,
                "popularity_score": 100.0 - (i * 10),
                "recommendation_score": 95.0 - (i * 5),
                "recommendation_reason": "인기 상품",
                "image_url": None,
                "description": f"인기 상품 {i + 1} 설명"
            })

        return {
            "success": True,
            "algorithm": "fallback_popular",
            "recommendations": dummy_items,
            "timestamp": datetime.now().isoformat(),
            "message": "추천 시스템 초기화 중 - 기본 데이터 제공"
        }

    try:
        popular_items = recommender_instance.get_popular_items(num_recommendations)

        return {
            "success": True,
            "algorithm": "popular",
            "recommendations": popular_items,
            "timestamp": datetime.now().isoformat()
        }

    except Exception as e:
        logger.error(f"인기 상품 조회 오류: {e}")

        # 오류 발생시에도 기본 응답 제공
        return {
            "success": False,
            "algorithm": "error_fallback",
            "recommendations": [],
            "timestamp": datetime.now().isoformat(),
            "message": f"인기 상품 조회 중 오류 발생: {str(e)}"
        }

# 메인 추천 엔드포인트
@app.post("/api/recommendations", response_model=RecommendationResponse)
async def get_recommendations(request: RecommendationRequest):
    """사용자 맞춤 추천"""
    if recommender_instance is None:
        logger.warning("추천 시스템이 초기화되지 않았습니다. 기본 추천을 제공합니다.")
        return RecommendationResponse(
            success=False,
            message="추천 시스템 초기화 중입니다. 잠시 후 다시 시도해주세요.",
            user_id=request.user_id,
            algorithm="fallback",
            recommendations=[],
            timestamp=datetime.now().isoformat()
        )

    try:
        user_id = request.user_id
        num_recs = request.num_recommendations
        algorithm = request.algorithm.lower()

        # 알고리즘별 추천 실행
        if algorithm == "hybrid":
            recommendations = recommender_instance.hybrid_recommendation(user_id, num_recs)
        elif algorithm == "user_based":
            recommendations = recommender_instance.collaborative_filtering_user_based(user_id, num_recs)
        elif algorithm == "item_based":
            recommendations = recommender_instance.collaborative_filtering_item_based(user_id, num_recs)
        elif algorithm == "content_based":
            recommendations = recommender_instance.content_based_filtering(user_id, num_recs)
        else:
            raise HTTPException(status_code=400, detail="지원하지 않는 알고리즘입니다.")

        # 결과가 없으면 인기 상품으로 대체
        if not recommendations:
            recommendations = recommender_instance.get_popular_items(num_recs)
            algorithm = "popular"

        # Pydantic 모델로 변환
        recommendation_items = [ItemInfo(**item) for item in recommendations]

        return RecommendationResponse(
            success=True,
            message=f"{len(recommendation_items)}개의 추천 상품을 찾았습니다.",
            user_id=user_id,
            algorithm=algorithm,
            recommendations=recommendation_items,
            timestamp=datetime.now().isoformat()
        )

    except Exception as e:
        logger.error(f"추천 생성 오류: {e}")
        raise HTTPException(status_code=500, detail=f"추천 생성 중 오류가 발생했습니다: {str(e)}")

# 실시간 추천
@app.post("/api/recommendations/realtime")
async def get_realtime_recommendations(request: SessionRecommendationRequest):
    """실시간/세션 기반 추천"""
    if recommender_instance is None:
        raise HTTPException(status_code=503, detail="추천 시스템이 초기화되지 않았습니다.")

    try:
        session_data = {
            "recent_views": request.recent_views,
            "current_cart": request.current_cart
        }

        recommendations = recommender_instance.real_time_recommendation(
            request.user_id,
            session_data,
            request.num_recommendations
        )

        return {
            "success": True,
            "user_id": request.user_id,
            "algorithm": "realtime",
            "recommendations": recommendations,
            "timestamp": datetime.now().isoformat()
        }

    except Exception as e:
        logger.error(f"실시간 추천 오류: {e}")
        raise HTTPException(status_code=500, detail=str(e))

# 유사 상품 추천
@app.post("/api/recommendations/similar")
async def get_similar_items(request: SimilarItemsRequest):
    """특정 상품들과 유사한 상품 추천"""
    if recommender_instance is None:
        raise HTTPException(status_code=503, detail="추천 시스템이 초기화되지 않았습니다.")

    try:
        recommendations = recommender_instance.get_similar_items_to_list(
            request.item_ids,
            request.num_recommendations
        )

        return {
            "success": True,
            "base_items": request.item_ids,
            "algorithm": "similar_items",
            "recommendations": recommendations,
            "timestamp": datetime.now().isoformat()
        }

    except Exception as e:
        logger.error(f"유사 상품 추천 오류: {e}")
        raise HTTPException(status_code=500, detail=str(e))

# 카테고리 기반 추천
@app.post("/api/recommendations/category")
async def get_category_recommendations(request: CategoryRecommendationRequest):
    """카테고리 기반 추천"""
    if recommender_instance is None:
        raise HTTPException(status_code=503, detail="추천 시스템이 초기화되지 않았습니다.")

    try:
        recommendations = recommender_instance.category_based_recommendation(
            request.category,
            request.num_recommendations
        )

        return {
            "success": True,
            "category": request.category,
            "algorithm": "category_based",
            "recommendations": recommendations,
            "timestamp": datetime.now().isoformat()
        }

    except Exception as e:
        logger.error(f"카테고리 추천 오류: {e}")
        raise HTTPException(status_code=500, detail=str(e))

# 사용자 상호작용 기록
@app.post("/api/interactions/record")
async def record_user_interaction(user_id: int, item_id: int, action: str):
    """사용자 상호작용 기록 (Redis에 저장)"""
    if data_processor_instance is None:
        raise HTTPException(status_code=503, detail="데이터 프로세서가 초기화되지 않았습니다.")

    try:
        if data_processor_instance.redis_client:
            # Redis에 실시간 상호작용 기록
            interaction_data = {
                "item_id": item_id,
                "action": action,
                "timestamp": datetime.now().timestamp(),
                "session_id": f"session_{user_id}_{datetime.now().strftime('%Y%m%d')}"
            }

            # 최근 조회 목록에 추가
            if action == "view":
                data_processor_instance.redis_client.lpush(
                    f"member:{user_id}:recent_views",
                    json.dumps(interaction_data)
                )
                data_processor_instance.redis_client.ltrim(f"member:{user_id}:recent_views", 0, 19)  # 최근 20개만 유지

            # 인기 상품 점수 업데이트
            data_processor_instance.redis_client.zincrby("popular_items", 1, item_id)

            return {
                "success": True,
                "message": "상호작용이 기록되었습니다.",
                "user_id": user_id,
                "item_id": item_id,
                "action": action,
                "timestamp": datetime.now().isoformat()
            }
        else:
            raise HTTPException(status_code=503, detail="Redis가 연결되지 않았습니다.")

    except Exception as e:
        logger.error(f"상호작용 기록 오류: {e}")
        raise HTTPException(status_code=500, detail=str(e))

# 데이터 재처리
@app.post("/api/system/refresh")
async def refresh_data(background_tasks: BackgroundTasks):
    """데이터 재처리 및 모델 업데이트"""
    background_tasks.add_task(refresh_recommendation_data)

    return {
        "success": True,
        "message": "데이터 재처리가 백그라운드에서 시작되었습니다.",
        "timestamp": datetime.now().isoformat()
    }

async def refresh_recommendation_data():
    """백그라운드에서 데이터 재처리"""
    global recommender_instance, data_processor_instance

    try:
        logger.info("데이터 재처리 시작...")

        # 모듈 재임포트
        from DatabaseDataProcessor import run_sonstar_data_preprocessing
        from recommend_Algorithm import SonStarRecommendationSystem

        # 데이터 전처리 재실행
        run_sonstar_data_preprocessing()

        # 추천 시스템 재초기화
        recommender_instance = SonStarRecommendationSystem()
        recommender_instance.load_preprocessed_data()

        logger.info("데이터 재처리 완료!")

    except Exception as e:
        logger.error(f"데이터 재처리 오류: {e}")

# 시스템 정보
@app.get("/api/system/info")
async def get_system_info():
    """시스템 정보 조회"""
    info = {
        "status": "ready" if recommender_instance is not None else "system_not_ready",
        "data_stats": {},
        "timestamp": datetime.now().isoformat()
    }

    # 설정 정보 추가
    if CONFIG_AVAILABLE:
        info["config"] = {
            "mysql_host": config.MYSQL_HOST,
            "redis_host": config.REDIS_HOST,
            "data_dir": config.DATA_DIR
        }

    # 데이터 통계 추가
    if recommender_instance is not None:
        try:
            if hasattr(recommender_instance, 'items_df') and recommender_instance.items_df is not None:
                info["data_stats"]["items_count"] = len(recommender_instance.items_df)
            if hasattr(recommender_instance, 'members_df') and recommender_instance.members_df is not None:
                info["data_stats"]["members_count"] = len(recommender_instance.members_df)
            if hasattr(recommender_instance, 'interactions_df') and recommender_instance.interactions_df is not None:
                info["data_stats"]["interactions_count"] = len(recommender_instance.interactions_df)
        except Exception as e:
            logger.warning(f"데이터 통계 수집 실패: {e}")

    return info

# 환경변수 확인 엔드포인트
@app.get("/api/system/config")
async def get_system_config():
    """현재 설정 확인 (디버깅용)"""
    if CONFIG_AVAILABLE:
        return {
            "mysql": {
                "host": config.MYSQL_HOST,
                "port": config.MYSQL_PORT,
                "database": config.MYSQL_DATABASE,
                "user": config.MYSQL_USER
            },
            "redis": {
                "host": config.REDIS_HOST,
                "port": config.REDIS_PORT
            },
            "data_dir": config.DATA_DIR,
            "is_docker": config.is_docker
        }
    else:
        return {
            "message": "Config 모듈을 사용할 수 없습니다.",
            "env_vars": {
                "MYSQL_HOST": os.getenv("MYSQL_HOST", "localhost"),
                "MYSQL_PORT": os.getenv("MYSQL_PORT", "3306"),
                "REDIS_HOST": os.getenv("REDIS_HOST", "localhost"),
                "REDIS_PORT": os.getenv("REDIS_PORT", "6379"),
                "DATA_DIR": os.getenv("DATA_DIR", "./data")
            }
        }

if __name__ == "__main__":
    # 환경변수에서 포트 읽기
    port = int(os.getenv("PORT", "5000"))
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=port,
        reload=False,  # 프로덕션에서는 reload=False
        log_level="info"
    )