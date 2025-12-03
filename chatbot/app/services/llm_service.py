from openai import OpenAI
from app.config import settings

class LLMService:
    def __init__(self):
        self.client = OpenAI(api_key=settings.openai_api_key)

    def generate_response(self, query: str, products: list, history: list = None) -> str:
        # 상품 정보를 간결하게 포맷
        product_info = ""
        if products:
            product_list = []
            for i, p in enumerate(products[:5], 1):
                product_list.append(f"{i}. {p['product_name']} ({p['category']})")
            product_info = "\n".join(product_list)

        system_prompt = """당신은 SonStar 쇼핑몰의 친절한 스타일 컨설턴트입니다.

응답 규칙:
1. 간결하고 친근하게 2-3문장으로 답변하세요
2. 상품 ID, 카테고리 코드 등 기술적 정보는 절대 언급하지 마세요
3. 마크다운(**굵게**, ID: 등)을 사용하지 마세요
4. 자연스러운 대화체로 추천 이유를 설명하세요
5. 상품명만 자연스럽게 언급하세요
6. 몇가지 추천해드릴게요가 아닌 정확한 숫자를 제시해서 알려주세요(최대 5개)

예시 응답:
"여름에 시원하게 입기 좋은 티셔츠를 2가지 찾아봤어요! 통기성 좋은 면 소재의 캐주얼 티셔츠와 활동하기 편한 스포츠 티셔츠를 추천드려요. 아래 상품들을 확인해보세요!"
"""

        messages = [{"role": "system", "content": system_prompt}]

        # 대화 히스토리 추가 (최근 4개만)
        if history:
            for h in history[-4:]:
                messages.append({"role": h["role"], "content": h["content"]})

        # 사용자 질문과 상품 정보
        user_content = f"고객 질문: {query}"
        if product_info:
            user_content += f"\n\n검색된 상품:\n{product_info}"
            user_content += "\n\n위 상품들을 참고하여 친근하게 추천해주세요. 상품 ID나 카테고리 코드는 언급하지 마세요."

        messages.append({"role": "user", "content": user_content})

        try:
            response = self.client.chat.completions.create(
                model="gpt-4o-mini",
                messages=messages,
                max_tokens=200,
                temperature=0.7
            )
            return response.choices[0].message.content
        except Exception as e:
            print(f"LLM Error: {e}")
            return "죄송합니다. 잠시 후 다시 시도해주세요."

llm_service = LLMService()