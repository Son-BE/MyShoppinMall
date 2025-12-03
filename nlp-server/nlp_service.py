# from fastapi import FastAPI, HTTPException
# from fastapi.middleware.cors import CORSMiddleware
# from pydantic import BaseModel, Field
# from typing import List, Dict, Optional, Union
# import logging
# from datetime import datetime
# import json
# import re
# import uvicorn
#
# # NLP 라이브러리
# from sentence_transformers import SentenceTransformer
# import numpy as np
# from sklearn.metrics.pairwise import cosine_similarity
#
# logging.basicConfig(level=logging.INFO)
# logger = logging.getLogger(__name__)
#
# app = FastAPI(title="SonStar NLP Service", version="1.0.0")
#
# # CORS 설정
# app.add_middleware(
#     CORSMiddleware,
#     allow_origins=["http://localhost:8080", "http://shop-app:8080"],
#     allow_credentials=True,
#     allow_methods=["*"],
#     allow_headers=["*"],
# )
#
# class NLPQueryRequest(BaseModel):
#     query: str
#     user_context: Optional[Dict] = None
#     limit: int = 10
#
# class QueryAnalysisResult(BaseModel):
#     intent: str
#     entities: Dict[str, List[str]]
#     keywords: List[str]
#     semantic_vector: List[float]
#     confidence: float
#     processed_query: str
#     season: Optional[str] = None
#     occasion: Optional[str] = None
#     style_preference: Optional[str] = None
#     gender_target: Optional[str] = None
#
# class ProductMatchRequest(BaseModel):
#     query_analysis: QueryAnalysisResult
#     products: List[Dict]
#
# class ProductScore(BaseModel):
#     product_id: Union[int, str]
#     score: float
#     match_reasons: List[str]
#     semantic_similarity: float
#     keyword_matches: List[str]
#
# class KoreanFashionNLPService:
#     def __init__(self):
#         self.model = SentenceTransformer('intfloat/multilingual-e5-large-instruct')
#         self.init_fashion_dictionaries()
#         logger.info("한국어 패션 NLP 서비스 초기화 완료")
#
#     def safe_first(self, lst: list) -> Optional[str]:
#         if lst and len(lst) > 0:
#             return lst[0]
#         return None
#
#     def init_fashion_dictionaries(self):
#         """패션 도메인 특화 사전 초기화"""
#
#         # ✅ 수정: 한글->영문 매핑 추가
#         self.season_mapping = {
#             '봄': 'SPRING',
#             '여름': 'SUMMER',
#             '가을': 'AUTUMN',
#             '겨울': 'WINTER'
#         }
#
#         self.season_keywords = {
#             'SPRING': ['봄', '스프링', '따뜻한', '산뜻한', '경량', '라이트', '가벼운'],
#             'SUMMER': ['여름', '섬머', '시원한', '쿨', '린넨', '면', '민소매', '반팔'],
#             'AUTUMN': ['가을', '어텀', '가을맞이', '단풍', '레이어드', '카키', '브라운'],
#             'WINTER': ['겨울', '윈터', '따뜻한', '보온', '코트', '패딩', '니트', '부츠']
#         }
#
#         self.occasion_keywords = {
#             '데이트': ['데이트', '데이트룩', '로맨틱', '특별한', '예쁜', '사랑스러운'],
#             '출근': ['출근', '오피스', '비즈니스', '직장', '업무', '미팅', '회의'],
#             '캐주얼': ['일상', '편한', '캐주얼', '데일리', '평상시', '동네'],
#             '파티': ['파티', '클럽', '나이트', '섹시', '화려한', '특별한'],
#             '여행': ['여행', '휴가', '놀러', '나들이', '외출', '피크닉'],
#             '운동': ['운동', '스포츠', '헬스', '요가', '러닝', '조깅', '활동적']
#         }
#
#         self.style_keywords = {
#             '미니멀': ['미니멀', '심플', '깔끔한', '단정한', '베이직'],
#             '페미닌': ['페미닌', '여성스러운', '우아한', '기품있는', '엘레간트'],
#             '스트릿': ['스트릿', '힙합', '유니크', '개성있는', '특이한'],
#             '빈티지': ['빈티지', '레트로', '클래식', '옛날', '고풍스러운'],
#             '보헤미안': ['보헤미안', '자유로운', '히피', '플로우'],
#             '스포티': ['스포티', '활동적', '운동', '액티브']
#         }
#
#         self.color_keywords = {
#             '블랙': ['검은', '블랙', '검정'],
#             '화이트': ['흰', '화이트', '하얀', '순백'],
#             '네이비': ['네이비', '남색', '진한파랑'],
#             '베이지': ['베이지', '아이보리', '크림'],
#             '브라운': ['브라운', '갈색', '초콜릿', '커피'],
#             '그레이': ['그레이', '회색', '그회색'],
#             '레드': ['빨간', '레드', '와인', '버건디'],
#             '블루': ['파란', '블루', '하늘'],
#             '핑크': ['분홍', '핑크', '로즈'],
#             '그린': ['초록', '그린', '카키'],
#             '옐로우': ['노란', '옐로우', '머스타드']
#         }
#
#         self.category_keywords = {
#             '상의': ['상의', '셔츠', '블라우스', '티셔츠', '후드', '맨투맨', '니트', '가디건'],
#             '하의': ['하의', '바지', '팬츠', '청바지', '슬랙스', '레깅스', '치마', '스커트'],
#             '아우터': ['아우터', '자켓', '코트', '점퍼', '패딩', '가디건', '조끼'],
#             '원피스': ['원피스', '드레스', '투피스'],
#             '신발': ['신발', '구두', '운동화', '스니커즈', '부츠', '샌들', '슬리퍼'],
#             '가방': ['가방', '백', '핸드백', '숄더백', '크로스백', '클러치', '백팩'],
#             '액세서리': ['액세서리', '모자', '목걸이', '귀걸이', '시계', '반지', '스카프']
#         }
#
#     def analyze_query(self, query: str, user_context: Optional[Dict] = None) -> QueryAnalysisResult:
#         logger.info(f"쿼리 분석 시작: {query}")
#
#         processed_query = self.preprocess_korean_text(query)
#         intent = self.classify_intent(processed_query)
#         entities = self.extract_entities(processed_query)
#         keywords = self.extract_keywords(processed_query)
#         semantic_vector = self.generate_semantic_vector(processed_query)
#         confidence = self.calculate_confidence(entities, keywords)
#
#         return QueryAnalysisResult(
#             intent=intent,
#             entities=entities,
#             keywords=keywords,
#             semantic_vector=semantic_vector.tolist(),
#             confidence=confidence,
#             processed_query=processed_query,
#             season=self.safe_first(entities.get('season', [])),
#             occasion=self.safe_first(entities.get('occasion', [])),
#             style_preference=self.safe_first(entities.get('style', [])),
#             gender_target=self.safe_first(entities.get('gender', []))
#         )
#
#     def preprocess_korean_text(self, text: str) -> str:
#         text = re.sub(r'[^\w\s가-힣]', ' ', text)
#         replacements = {
#             '코디': '코디네이션 스타일링 매칭',
#             '룩': '룩 스타일 패션',
#             '옷': '의류 패션 아이템',
#             '추천': '추천 제안 소개',
#             '어울리는': '어울리는 매칭되는 잘맞는'
#         }
#         for old, new in replacements.items():
#             text = text.replace(old, new)
#         return text.strip()
#
#     def classify_intent(self, query: str) -> str:
#         query_lower = query.lower()
#         if any(word in query_lower for word in ['추천', '제안', '알려줘', '찾아줘']):
#             if any(word in query_lower for word in ['코디', '스타일링', '매칭']):
#                 return 'style_recommendation'
#             else:
#                 return 'product_search'
#         elif any(word in query_lower for word in ['어때', '어울려', '맞을까']):
#             return 'style_advice'
#         else:
#             return 'general_search'
#
#     def extract_entities(self, query: str) -> Dict[str, List[str]]:
#         entities = {'season': [], 'occasion': [], 'style': [], 'color': [], 'category': [], 'gender': []}
#         query_lower = query.lower()
#
#         # ✅ 수정: 영문 계절명 반환
#         for season_eng, keywords in self.season_keywords.items():
#             if any(keyword in query_lower for keyword in keywords):
#                 entities['season'].append(season_eng)  # 'SPRING' 반환
#                 logger.info(f"계절 감지: {season_eng}")
#
#         for occasion, keywords in self.occasion_keywords.items():
#             if any(keyword in query_lower for keyword in keywords):
#                 entities['occasion'].append(occasion)
#
#         for style, keywords in self.style_keywords.items():
#             if any(keyword in query_lower for keyword in keywords):
#                 entities['style'].append(style)
#
#         for color, keywords in self.color_keywords.items():
#             if any(keyword in query_lower for keyword in keywords):
#                 entities['color'].append(color)
#
#         for category, keywords in self.category_keywords.items():
#             if any(keyword in query_lower for keyword in keywords):
#                 entities['category'].append(category)
#
#         if any(word in query_lower for word in ['남성', '남자', '맨즈']):
#             entities['gender'].append('M')
#         elif any(word in query_lower for word in ['여성', '여자', '우먼즈']):
#             entities['gender'].append('F')
#
#         return entities
#
#     def extract_keywords(self, query: str) -> List[str]:
#         words = query.split()
#         stopwords = {'을', '를', '이', '가', '은', '는', '의', '에', '로', '으로', '와', '과', '도'}
#         return [word for word in words if word not in stopwords and len(word) > 1]
#
#     def generate_semantic_vector(self, query: str) -> np.ndarray:
#         prompted_query = f"query: 한국어 패션 상품 검색 {query}"
#         embedding = self.model.encode(prompted_query, normalize_embeddings=True)
#         return embedding
#
#     def calculate_confidence(self, entities: Dict, keywords: List[str]) -> float:
#         confidence = 0.5
#         entity_count = sum(len(values) for values in entities.values())
#         confidence += min(entity_count * 0.1, 0.3)
#         confidence += min(len(keywords) * 0.05, 0.2)
#         return min(confidence, 1.0)
#
#     def score_products(self, query_analysis: QueryAnalysisResult, products: List[Dict]) -> List[ProductScore]:
#         results = []
#         query_vector = np.array(query_analysis.semantic_vector).reshape(1, -1)
#
#         for product in products:
#             try:
#                 if not self.validate_product_data(product):
#                     logger.warning(f"Invalid product data: {product}")
#                     continue
#
#                 product_desc = self.create_product_description(product)
#                 product_vector = self.generate_semantic_vector(product_desc).reshape(1, -1)
#                 similarity = cosine_similarity(query_vector, product_vector)[0][0]
#                 keyword_matches = self.find_keyword_matches(query_analysis, product)
#                 entity_bonus = self.calculate_entity_bonus(query_analysis, product)
#                 final_score = similarity * 0.6 + len(keyword_matches) * 0.1 + entity_bonus * 0.3
#                 match_reasons = self.generate_match_reasons(query_analysis, product, keyword_matches)
#
#                 product_id = product.get('id')
#                 if product_id is not None:
#                     if isinstance(product_id, str):
#                         try:
#                             product_id = int(product_id)
#                         except ValueError:
#                             logger.warning(f"Invalid product_id format: {product_id}")
#                             continue
#
#                 results.append(ProductScore(
#                     product_id=product_id,
#                     score=final_score,
#                     match_reasons=match_reasons,
#                     semantic_similarity=similarity,
#                     keyword_matches=keyword_matches
#                 ))
#             except Exception as e:
#                 logger.error(f"Error processing product {product.get('id', 'unknown')}: {e}")
#                 continue
#
#         results.sort(key=lambda x: x.score, reverse=True)
#         return results
#
#     def validate_product_data(self, product: Dict) -> bool:
#         required_fields = ['id']
#         return all(field in product for field in required_fields)
#
#     def create_product_description(self, product: Dict) -> str:
#         desc_parts = []
#         if product.get('itemName'):
#             desc_parts.append(product['itemName'])
#         if product.get('category'):
#             desc_parts.append(self.translate_category(product['category']))
#         if product.get('style'):
#             desc_parts.append(f"{product['style']} 스타일")
#         if product.get('season'):
#             desc_parts.append(f"{product['season']}용")
#         if product.get('gender'):
#             desc_parts.append("남성용" if product['gender'] == 'M' else "여성용")
#         return " ".join(desc_parts) if desc_parts else "패션 아이템"
#
#     def translate_category(self, category: str) -> str:
#         translations = {
#             'TOP': '상의', 'BOTTOM': '하의', 'OUTER': '아우터',
#             'DRESS': '원피스', 'SHOES': '신발', 'ACCESSORY': '액세서리',
#             'MENS_TOP': '남성 상의', 'MENS_BOTTOM': '남성 하의',
#             'MENS_OUTER': '남성 아우터', 'MENS_SHOES': '남성 신발',
#             'MENS_ACCESSORY': '남성 액세서리',
#             'WOMENS_TOP': '여성 상의', 'WOMENS_BOTTOM': '여성 하의',
#             'WOMENS_OUTER': '여성 아우터', 'WOMENS_SHOES': '여성 신발',
#             'WOMENS_ACCESSORY': '여성 액세서리'
#         }
#         return translations.get(category, category)
#
#     def find_keyword_matches(self, query_analysis: QueryAnalysisResult, product: Dict) -> List[str]:
#         matches = []
#         product_text = self.create_product_description(product).lower()
#         for keyword in query_analysis.keywords:
#             if keyword.lower() in product_text:
#                 matches.append(keyword)
#         return matches
#
#     def calculate_entity_bonus(self, query_analysis: QueryAnalysisResult, product: Dict) -> float:
#         bonus = 0.0
#
#         # ✅ 수정: 계절 매칭 (이제 양쪽 다 영문)
#         if query_analysis.season and product.get('season'):
#             product_season = str(product.get('season')).upper()
#             query_season = query_analysis.season.upper()
#
#             logger.info(f"계절 비교: query={query_season}, product={product_season}")
#
#             if query_season == product_season:
#                 bonus += 0.3
#                 logger.info(f"계절 매칭 성공! bonus={bonus}")
#
#         # Gender matching
#         if query_analysis.gender_target and product.get('gender'):
#             if query_analysis.gender_target == product.get('gender'):
#                 bonus += 0.2
#
#         # Category matching
#         if query_analysis.entities.get('category'):
#             product_category = self.translate_category(str(product.get('category', '')))
#             if any(cat in product_category for cat in query_analysis.entities['category']):
#                 bonus += 0.2
#
#         return min(bonus, 1.0)
#
#     def generate_match_reasons(self, query_analysis: QueryAnalysisResult, product: Dict, keyword_matches: List[str]) -> List[str]:
#         reasons = []
#
#         if keyword_matches:
#             reasons.append(f"키워드 매칭: {', '.join(keyword_matches[:2])}")
#
#         # ✅ 수정: 계절 표시 개선
#         if query_analysis.season and product.get('season'):
#             if query_analysis.season.upper() == str(product.get('season')).upper():
#                 season_korean = {
#                     'SPRING': '봄',
#                     'SUMMER': '여름',
#                     'AUTUMN': '가을',
#                     'WINTER': '겨울'
#                 }.get(query_analysis.season.upper(), query_analysis.season)
#                 reasons.append(f"{season_korean} 계절 상품")
#
#         if query_analysis.occasion:
#             reasons.append(f"{self.safe_first(query_analysis.occasion)} 상황에 적합")
#
#         if not reasons:
#             reasons.append("AI 시맨틱 분석 매칭")
#
#         return reasons
#
# # ================= 서버 초기화 =================
# nlp_service = None
#
# @app.on_event("startup")
# async def startup_event():
#     global nlp_service
#     nlp_service = KoreanFashionNLPService()
#     logger.info("NLP 서비스 시작 완료")
#
# @app.get("/health")
# async def health_check():
#     return {
#         "status": "healthy",
#         "model": "multilingual-e5-large-instruct",
#         "timestamp": datetime.now().isoformat()
#     }
#
# @app.post("/analyze-query")
# async def analyze_query(request: NLPQueryRequest):
#     try:
#         result = nlp_service.analyze_query(request.query, request.user_context)
#         return {"success": True, "data": result.dict()}
#     except Exception as e:
#         logger.error(f"쿼리 분석 오류: {e}")
#         raise HTTPException(status_code=500, detail=str(e))
#
# @app.post("/score-products")
# async def score_products(request: ProductMatchRequest):
#     try:
#         if not request.products:
#             logger.warning("Empty products list received")
#             return {"success": True, "data": []}
#
#         logger.info(f"Scoring {len(request.products)} products")
#         scores = nlp_service.score_products(request.query_analysis, request.products)
#         logger.info(f"Generated {len(scores)} product scores")
#
#         return {"success": True, "data": [s.dict() for s in scores]}
#     except Exception as e:
#         logger.error(f"상품 점수 계산 오류: {e}")
#         raise HTTPException(status_code=422, detail=f"Product scoring failed: {str(e)}")
#
# if __name__ == "__main__":
#     uvicorn.run(app, host="0.0.0.0", port=5001)