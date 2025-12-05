# HuggingFace 기반 이미지 분류
from transformers import pipeline
from PIL import Image
import torch
import io
import logging

logger = logging.getLogger(__name__)


class ImageClassifier:
    def __init__(self):
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        self.classifier = None
        self.fashion_classifier = None
        self._load_models()

    def _load_models(self):
        """모델 로드 (최초 1회)"""
        try:
            # 일반 이미지 분류 모델
            self.classifier = pipeline(
                "image-classification",
                model="google/vit-base-patch16-224",
                device=0 if self.device == "cuda" else -1
            )
            logger.info("일반 분류 모델 로드 완료")
            print("일반 분류 모델 로드 완료")
        except Exception as e:
            logger.error(f"일반 모델 로드 실패: {e}")
            print(f"일반 모델 로드 실패: {e}")
            self.classifier = None

        # 패션 특화 모델 (실패해도 계속 진행)
        try:
            self.fashion_classifier = pipeline(
                "image-classification",
                model="nateraw/vit-base-patch16-224-cifar10",  # 대체 모델
                device=0 if self.device == "cuda" else -1
            )
            logger.info("패션 분류 모델 로드 완료")
            print("패션 분류 모델 로드 완료")
        except Exception as e:
            logger.warning(f"패션 모델 로드 실패 (무시): {e}")
            print(f"패션 모델 로드 실패 (일반 모델만 사용): {e}")
            self.fashion_classifier = None

    def classify(self, image_bytes: bytes) -> dict:
        """이미지 기본 분류"""
        try:
            image = Image.open(io.BytesIO(image_bytes)).convert("RGB")

            results = {
                "general": [],
                "fashion": [],
                "detected_category": None,
                "detected_type": None,
                "confidence": 0.0
            }

            # 일반 분류
            if self.classifier:
                general_results = self.classifier(image, top_k=5)
                results["general"] = [
                    {"label": r["label"], "score": round(r["score"], 4)}
                    for r in general_results
                ]
                logger.info(f"일반 분류 결과: {results['general'][:3]}")

            # 패션 분류 (선택적)
            if self.fashion_classifier:
                try:
                    fashion_results = self.fashion_classifier(image, top_k=5)
                    results["fashion"] = [
                        {"label": r["label"], "score": round(r["score"], 4)}
                        for r in fashion_results
                    ]
                except Exception as e:
                    logger.warning(f"패션 분류 실패: {e}")

            # 카테고리 맵핑
            category_info = self._map_to_category(results)
            results.update(category_info)
            return results

        except Exception as e:
            logger.error(f"분류 오류: {e}")
            print(f"분류 오류: {e}")
            return {
                "error": str(e),
                "detected_category": "TOP",
                "detected_type": "tshirt",
                "confidence": 0.0
            }

    def _map_to_category(self, results: dict) -> dict:
        """분류 결과를 쇼핑몰 카테고리로 매핑"""

        category_map = {
            # 상의
            "t-shirt": ("TOP", "tshirt"),
            "shirt": ("TOP", "shirt"),
            "jersey": ("TOP", "tshirt"),
            "pullover": ("TOP", "sweatshirt"),
            "coat": ("OUTER", "coat"),
            "jacket": ("OUTER", "jacket"),
            "sweatshirt": ("TOP", "sweatshirt"),
            "hoodie": ("TOP", "hoodie"),

            # 하의
            "trouser": ("BOTTOM", "slacks"),
            "pants": ("BOTTOM", "pants"),
            "jeans": ("BOTTOM", "jeans"),
            "shorts": ("BOTTOM", "shorts"),
            "skirt": ("BOTTOM", "skirt"),

            # 원피스
            "dress": ("TOP", "dress"),

            # 신발
            "sandal": ("SHOES", "sandals"),
            "sneaker": ("SHOES", "sneakers"),
            "ankle boot": ("SHOES", "boots"),
            "shoe": ("SHOES", "sneakers"),
            "boot": ("SHOES", "boots"),
            "running shoe": ("SHOES", "running_shoes"),
        }

        detected_category = None
        detected_type = None
        max_confidence = 0.0

        # 모든 결과에서 매핑 시도
        all_results = results.get("fashion", []) + results.get("general", [])

        for item in all_results:
            label = item["label"].lower()
            score = item["score"]

            for key, (cat, sub_type) in category_map.items():
                if key in label and score > max_confidence:
                    detected_category = cat
                    detected_type = sub_type
                    max_confidence = score

        # 기본값 설정
        if not detected_category:
            detected_category = "TOP"
            detected_type = "tshirt"

        return {
            "detected_category": detected_category,
            "detected_type": detected_type,
            "confidence": round(max_confidence, 4)
        }


# 싱글톤 인스턴스
image_classifier = ImageClassifier()