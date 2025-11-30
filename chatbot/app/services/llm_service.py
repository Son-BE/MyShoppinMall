import openai
from app.config import settings

class LLMService:
    def __init__(self):
        openai.api_key = settings.openai_api_key

    def generate_response(self, query: str, products: list) -> str:
        """검색된 상품 정보를 바탕으로 답변 생성"""

        # 상품 정보를 텍스트로 변환
        product_info = "\n".join([
            f"- {p['product_name']} (카테고리: {p['category']}, 유사도: {p['similarity']:.2f})"
            for p in products
        ])

        prompt = f"""쇼핑몰 상담 챗봇입니다. 고객의 질문에 친절하게 답변해주세요.

고객 질문: {query}

검색된 관련 상품:
{product_info}

위 상품들을 참고하여 고객에게 도움이 되는 답변을 작성해주세요. 
상품을 추천할 때는 왜 그 상품이 적합한지 간단히 설명해주세요."""

        response = openai.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": "당신은 친절한 쇼핑몰 상담 챗봇입니다."},
                {"role": "user", "content": prompt}
            ],
            max_tokens=500,
            temperature=0.7
        )

        return response.choices[0].message.content

llm_service = LLMService()