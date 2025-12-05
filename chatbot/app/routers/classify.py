"""
이미지 분류 API 라우터
"""
from fastapi import APIRouter, UploadFile, File, HTTPException
from pydantic import BaseModel
from typing import Optional, List
import logging

from app.services.image_classifier import image_classifier
from app.services.vision_analyzer import vision_analyzer

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/classify", tags=["Image Classification"])


class ClassificationResult(BaseModel):
    # 기본 분류 (HuggingFace)
    basic_category: Optional[str] = None
    basic_type: Optional[str] = None
    basic_confidence: float = 0.0

    # 상세 분석(OpenAI Vision)
    category: str = "MENS_TOP"
    subCategory: str = "tshirt"
    gender: str = "UNISEX"
    season: str = "ALL_SEASON"
    style: str = "CASUAL"
    primaryColor: str = "BLACK"
    secondaryColor: Optional[str] = None
    ageGroup: str = "ADULT"

    # 생성된 정보
    itemName: str = "새 상품"
    itemComment: str = "상품 설명을 입력해주세요."
    suggestedPrice: int = 30000
    keywords: List[str] = []

    # 메타 정보
    analysisSuccess: bool = False
    imagePath: Optional[str] = None
    originalFileName: Optional[str] = None


@router.post("/image", response_model=ClassificationResult)
async def classify_image(file: UploadFile = File(...)):
    """
    이미지 업로드 -> 하이브리드 분류
    1. Huggingface로 기본 카테고리 분류
    2. OpenAI Vision으로 상세분석
    """
    logger.info(f"=== 이미지 분류 요청: {file.filename} ===")
    print(f"=== 이미지 분류 요청: {file.filename} ===")

    # 파일 검증
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="이미지 파일만 업로드 가능합니다.")

    # 파일 크기 제한(10MB)
    contents = await file.read()
    if len(contents) > 10 * 1024 * 1024:
        raise HTTPException(status_code=400, detail="파일 크기는 10MB 이하만 가능합니다.")

    try:
        # 1. HuggingFace 기본 분류
        logger.info("Step 1: HuggingFace 기본 분류")
        print("Step 1: HuggingFace 기본 분류")
        basic_result = image_classifier.classify(contents)
        logger.info(f"기본 분류 결과: {basic_result}")
        print(f"기본 분류 결과: {basic_result}")

        # 2. OpenAI Vision 상세 분류 (동기 함수 - await 제거!)
        logger.info("Step 2: OpenAI Vision 상세 분석")
        print("Step 2: OpenAI Vision 상세 분석")
        try:
            detailed_result = vision_analyzer.analyze(contents, basic_result)  # ✅ await 제거
            logger.info(f"Vision 분석 결과: {detailed_result}")
            print(f"Vision 분석 결과: {detailed_result}")
        except Exception as e:
            logger.error(f"Vision 분석 실패: {e}")
            print(f"Vision 분석 실패: {e}")
            detailed_result = {}

        # 결과 통합
        result = ClassificationResult(
            # 기본 분류 결과
            basic_category=basic_result.get("detected_category"),
            basic_type=basic_result.get("detected_type"),
            basic_confidence=basic_result.get("confidence", 0.0),

            # 상세 분석 결과
            category=detailed_result.get("category") or _map_basic_category(basic_result.get("detected_category")) or "MENS_TOP",
            subCategory=detailed_result.get("subCategory") or basic_result.get("detected_type") or "tshirt",
            gender=detailed_result.get("gender", "UNISEX"),
            season=detailed_result.get("season", "ALL_SEASON"),
            style=detailed_result.get("style", "CASUAL"),
            primaryColor=detailed_result.get("primaryColor", "BLACK"),
            secondaryColor=detailed_result.get("secondaryColor"),
            ageGroup=detailed_result.get("ageGroup", "ADULT"),

            # 생성된 정보
            itemName=detailed_result.get("itemName", "새 상품"),
            itemComment=detailed_result.get("itemComment", "상품 설명을 입력해주세요."),
            suggestedPrice=detailed_result.get("suggestedPrice", 30000),
            keywords=detailed_result.get("keywords", []),

            # 메타
            analysisSuccess=detailed_result.get("analysis_success", False),
        )

        logger.info(f"=== 분류 완료: {result.category}/{result.subCategory} ===")
        print(f"=== 분류 완료: {result.category}/{result.subCategory} ===")
        return result

    except Exception as e:
        logger.error(f"분류 처리 오류: {e}")
        print(f"분류 처리 오류: {e}")
        import traceback
        traceback.print_exc()

        # 에러 시 기본값 반환
        return ClassificationResult(
            category="MENS_TOP",
            subCategory="tshirt",
            gender="UNISEX",
            season="ALL_SEASON",
            style="CASUAL",
            primaryColor="BLACK",
            ageGroup="ADULT",
            itemName="새 상품",
            itemComment="상품 설명을 입력해주세요.",
            suggestedPrice=30000,
            analysisSuccess=False
        )


def _map_basic_category(basic_cat: str) -> str:
    """기본 카테고리를 상세 카테고리로 매핑"""
    if not basic_cat:
        return "MENS_TOP"

    mapping = {
        "TOP": "MENS_TOP",
        "BOTTOM": "MENS_BOTTOM",
        "OUTER": "MENS_OUTER",
        "SHOES": "MENS_SHOES",
        "DRESS": "WOMENS_TOP",
        "ACCESSORY": "MENS_ACCESSORY"
    }
    return mapping.get(basic_cat.upper(), "MENS_TOP")


@router.post("/basic")
async def classify_basic(file: UploadFile = File(...)):
    """허깅페이스 기본 분류만 수행"""
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="이미지 파일만 업로드 가능합니다.")

    contents = await file.read()
    result = image_classifier.classify(contents)

    return result


@router.post("/detailed")
async def classify_detailed(file: UploadFile = File(...)):
    """오픈AI 상세분석만 수행"""
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="이미지 파일만 업로드 가능합니다.")

    contents = await file.read()
    result = vision_analyzer.analyze(contents)  # ✅ await 제거

    return result


@router.get("/health")
async def health_check():
    """분류 서비스 헬스 체크"""
    return {
        "status": "ok",
        "classifier_loaded": image_classifier.classifier is not None,
        "fashion_classifier_loaded": image_classifier.fashion_classifier is not None
    }