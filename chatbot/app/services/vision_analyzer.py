"""
OpenAI Vision 기반 상세 분석 서비스
상품명, 설명, 색상, 스타일 등 상세 정보 생성
"""

import base64
from openai import OpenAI
from app.config import settings
import json
import re

class VisionAnalyzer:
    def __init__(self):
        self.client = OpenAI(api_key=settings.openai_api_key)

    def analyze(self, image_bytes: bytes, basic_category: dict = None) -> dict:
        """이미지 상세 분석"""

        # 이미지를 base64로 인코딩
        base64_image = base64.b64encode(image_bytes).decode('utf-8')

        # 기본 카테고리 정보 포함
        category_hint = ""
        if basic_category:
            if basic_category.get("detected_category"):
                category_hint = f"\n기본 분석 힌트: 이 상품은 '{basic_category['detected_category']}' 카테고리로 감지되었습니다. 참고만 하고 맹신하지 마세요."
            if basic_category.get("detected_type"):
                category_hint += f"\n추정 서브타입: '{basic_category['detected_type']}'."

        # 프롬프트
        prompt = f"""
당신의 임무는 패션 상품 이미지를 분석하여 **반드시 유효한 JSON 객체만** 출력하는 것입니다.
다음 규칙을 반드시 따르세요:

[출력 규칙]
- 출력은 JSON 객체 하나만 출력합니다.
- JSON 앞뒤에 설명, 문장, 코드블록, 말머리 등을 절대 포함하지 마세요.
- 키 누락 금지, 값 누락 금지.
- 선택지는 반드시 EXACT STRING만 사용.
- JSON 생성 전 내부적으로 유효성 검사를 한다고 가정하고 출력하세요.

[분석 규칙]
- 제공된 이미지 기반으로 객관적으로 판단하세요.
- 제공된 힌트(basic_category)는 참고만 하고 과도하게 신뢰하지 마세요.
- 색상은 이미지 기준 추정값만 사용, 과도한 세부 색상 금지.
- itemComment는 반드시 2~3문장으로 작성하며 과장·브랜드명 금지.

아래 JSON 스키마 정확히 일치하도록 출력하세요:

{{
    "category": "MENS_TOP | MENS_BOTTOM | MENS_OUTER | MENS_SHOES | WOMENS_TOP | WOMENS_BOTTOM | WOMENS_OUTER | WOMENS_SHOES | ACCESSORIES 중 하나",
    "subCategory": "tshirt | shirt | hoodie | sweatshirt | jacket | coat | padding | windbreaker | jeans | pants | jogger_pants | training_pants | sneakers | running_shoes | boots | dress_shoes | watch | ring | necklace 중 하나",
    "gender": "MALE | FEMALE | UNISEX 중 하나",
    "season": "SPRING | SUMMER | FALL | WINTER | ALL_SEASON 중 하나",
    "style": "CASUAL | FORMAL | SPORTY | STREET | MINIMAL | VINTAGE 중 하나",
    "primaryColor": "BLACK | WHITE | GRAY | NAVY | BLUE | RED | PINK | BEIGE | BROWN | GREEN | YELLOW | ORANGE | PURPLE 중 하나",
    "secondaryColor": "위와 동일한 색상 또는 null",
    "ageGroup": "TEEN | ADULT | MIDDLE_AGED 중 하나",
    "itemName": "브랜드 제외한 상품명 (예: 클래식 라운드넥 티셔츠)",
    "itemComment": "2~3문장의 상품 설명 (특징 + 스타일 + 추천 상황)",
    "suggestedPrice": 숫자만 (원 단위),
    "keywords": ["키워드1", "키워드2", "키워드3"]
}}

힌트:
{category_hint}
"""

        try:
            response = self.client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {
                        "role": "user",
                        "content": [
                            {"type": "text", "text": prompt},
                            {
                                "type": "image_url",
                                "image_url": {
                                    "url": f"data:image/jpeg;base64,{base64_image}",
                                    "detail": "low"
                                }
                            }
                        ]
                    }
                ],
                max_tokens=800,
                temperature=0.2
            )

            content = response.choices[0].message.content
            content = content.strip()

            # JSON 코드블록 처리 제거
            if content.startswith("```"):
                content = re.sub(r'^```json?\n?', '', content)
                content = re.sub(r'\n?```$', '', content)

            result = json.loads(content)
            result["analysis_success"] = True

            return result

        except json.JSONDecodeError as e:
            print(f"JSON 파싱 오류: {e}")
            print(f"응답 내용: {content}")
            return self._get_fallback_result(basic_category)

        except Exception as e:
            print(f"Vision 분석 오류: {e}")
            return self._get_fallback_result(basic_category)

    def _get_fallback_result(self, basic_category: dict = None) -> dict:
        """분석 실패 시 기본값 반환"""
        result = {
            "category": "MENS_TOP",
            "subCategory": "tshirt",
            "gender": "UNISEX",
            "season": "ALL_SEASON",
            "style": "CASUAL",
            "primaryColor": "BLACK",
            "secondaryColor": None,
            "ageGroup": "ADULT",
            "itemName": "새 상품",
            "itemComment": "상품 설명을 입력해주세요.",
            "suggestedPrice": 30000,
            "keywords": [],
            "analysis_success": False
        }

        # 기본 분류 결과 반영
        if basic_category:
            if basic_category.get("detected_category"):
                cat = basic_category["detected_category"]
                result["category"] = f"MENS_{cat}" if cat != "DRESS" else "WOMENS_DRESS"
            if basic_category.get("detected_type"):
                result["subCategory"] = basic_category["detected_type"]

        return result


# 싱글톤 인스턴스
vision_analyzer = VisionAnalyzer()
