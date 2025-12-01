import openai
from app.config import settings

class LLMService:
    def __init__(self):
        openai.api_key = settings.openai_api_key

    def generate_response(self, query: str, products: list, history: list = None) -> str:
        """검색된 상품 정보와 대화 히스토리를 바탕으로 답변 생성"""

        # 상품 정보를 텍스트로 변환
        product_info = "\n".join([
            f"- {p['product_name']} (ID: {p['product_id']}, 카테고리: {p['category']})"
            for p in products
        ])

        # 시스템 메시지
        system_message = """당신은 SonStar 쇼핑몰의 친절한 상담 챗봇입니다.
고객의 질문에 친절하게 답변하고, 적절한 상품을 추천해주세요.
상품을 추천할 때는 상품명과 함께 왜 그 상품이 적합한지 간단히 설명해주세요.
추천 상품의 ID도 함께 언급해주세요."""

        # 메시지 구성
        messages = [{"role": "system", "content": system_message}]

        # 대화 히스토리 추가
        if history:
            for msg in history[-6:]:  # 최근 6개 메시지만
                messages.append({
                    "role": msg['role'],
                    "content": msg['content']
                })

        # 현재 질문과 컨텍스트
        current_prompt = f"""고객 질문: {query}

검색된 관련 상품:
{product_info}

위 상품들을 참고하여 고객에게 도움이 되는 답변을 작성해주세요."""

        messages.append({"role": "user", "content": current_prompt})

        response = openai.chat.completions.create(
            model="gpt-4o-mini",
            messages=messages,
            max_tokens=500,
            temperature=0.7
        )

        return response.choices[0].message.content

llm_service = LLMService()